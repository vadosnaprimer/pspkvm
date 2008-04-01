/*
 *
 * Copyright  1990-2007 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, CA 95054 or visit www.sun.com if you need additional
 * information or have any questions.
 */ 


#ifdef __cplusplus
extern "C" {
#endif

#include <stdlib.h>
#include <string.h>
#include "javacall_multimedia.h"
#include "javacall_file.h"
#include "SDL/SDL.h"
#include "SDL/SDL_mixer.h" 

extern int max_patch_memory;
extern char* javacall_UNICODEsToUtf8(const javacall_utf16* str, int strlen);

static const javacall_utf16 midi_cfg_fn[] = {'t','i','m','i','d','i','t','y','.','c','f','g'};

#define TMP_MUSIC_FILENAME_LEN_MAX 64
#define DEFAULT_WAVE_BUFFER_LENGTH (64*1024)
#define CHANNEL_NUM 16

#define MEDIA_TYPE_MIDI  1
#define MEDIA_TYPE_WAVE 2

typedef struct {
	int type;
	int needBuffer;
	int contentLength;
	Mix_Music* music;
	int occupied;
	char filename[256];
	javacall_utf16 tmpfilename[64];
	int tmpfilenamelen;
	javacall_handle fp;
	char* buffer;
	int buffered_len;
	int paused;
	javacall_int64 playerId;
} midi_player_handle;

struct _wav_player_handle;
typedef struct _wav_player_handle {
	int type;
	int needBuffer;
	int contentLength;
	Mix_Chunk* chunk;
	int channel;
	char filename[256];
	javacall_utf16 tmpfilename[64];
	int tmpfilenamelen;
	javacall_handle fp;	
	javacall_int64 playerId;
	char* buffer;
	int buffered_len;
	int paused;
	struct _wav_player_handle* prev;
	struct _wav_player_handle* next;
} wav_player_handle;

typedef union {
	int type;
	midi_player_handle midi;
	wav_player_handle wav;
} mmplayer_handle;

#define INVALID_PLAYER_ID (0LL)
static int is_midi_support = 0;
static int music_handle_occupied = 0;
static javacall_int64 current_playing = INVALID_PLAYER_ID;
static wav_player_handle* wav_players = NULL;

/**
 * NOTE: Example of javacall_media_caps value
 *
 * OEM should list all of supported content types as a MIME string
 * (for example, audio/x-wav, video/mpeg, and etc...)
 * And, list supported protocols per content types 
 * (for example, http, file, rtsp, device, or capture)
 *
 * If OEM support interactive MIDI player, Add 'device' protocol
 * to the MIDI type
 *
 * If OEM support audio recording, Add 'capture' protocol
 * to the audio recording format MIME type
 *
 * If OEM support video recording, Add 'capture' protocol
 * to the video recording format MIME type
 *
 * At the end of item, content type string should be NULL
 * And, list all of supported protocols from this item
 *
 * <mime string>, <protocol count>, <protocol strings>
 */

static int _javacall_media_initilized = 0;

// IMPL_NOTE: the only usage for this data now is
// to determine if we have support for JTS and AMR.
// actual supported protocols/types are defined in Java.

static const javacall_media_caps _media_caps[] = {
    /* End of caps => mimeType should be NULL and list all of 
       protocols from here ! */ 
    {NULL, 3, {"device", "capture", "http"}} 
};

/** Declaration of MIDI functions **/
static mmplayer_handle* create_midi(javacall_int64 playerId, const javacall_utf16* uri, long uriLength, long contentLength);
static void close_midi(midi_player_handle* mp);
static long buffer_midi(midi_player_handle* mp, const void* buffer, long length, long offset);
static void clear_buffer_midi(midi_player_handle* mp);
static javacall_result start_midi(midi_player_handle* mp);
static javacall_result pause_midi(midi_player_handle* mp);

/** Declaration of WAVE functions **/
static mmplayer_handle* create_wav(javacall_int64 playerId, const javacall_utf16* uri, long uriLength, long contentLength);
static void close_wav(wav_player_handle* mp);
static long buffer_wav(wav_player_handle* mp, const void* buffer, long length, long offset);
static void clear_buffer_wav(wav_player_handle* mp);
static javacall_result start_wav(wav_player_handle* mp);
static javacall_result pause_wav(wav_player_handle* mp);


/** MIDI music finish callback **/
static void musicFinished() {
    printf("musicFinished.\n");
    if (current_playing) {
        javanotify_on_media_notification(JAVACALL_EVENT_MEDIA_END_OF_MEDIA, current_playing, 0);
        current_playing = INVALID_PLAYER_ID;
    }
}

static void channelDone(int channel)
{
    int i;
    wav_player_handle* player;
    
    printf("channel %d finished playing.\n", channel);
    player = wav_players;
    while (player != NULL) {
        if (player->channel == channel) {
            break;
        }
        player = player->next;
    }

    if (player) {
        javanotify_on_media_notification(JAVACALL_EVENT_MEDIA_END_OF_MEDIA, player->playerId, 0);
        player->channel = -1;
    }
}


/**
 * Get multimedia capabilities of the device.
 * This function should return pointer to static array of javacall_media_caps value
 * The last item of javacall_media_caps array should hold NULL mimeType value
 * Java layer will use this NULL value as a end of item mark
 */
const javacall_media_caps* javacall_media_get_caps(void) {
       javacall_print("javacall_media_get_caps\n");
	return _media_caps;
}

/**
 * Query whether audio mixing is supported or not
 */
javacall_bool javacall_media_supports_mixing() {
    return JAVACALL_FALSE;
}

/**
 * Call this function when VM starts
 * Perform global initialization operation
 * 
 * @retval JOK      success
 * @retval JFAIL    fail
 */
javacall_result javacall_media_initialize(void) {
    javacall_print("javacall_media_initialize\n");
    if (_javacall_media_initilized == 1) {
    	return JAVACALL_OK;
    }

    if (_javacall_media_initilized == -1) {
    	return JAVACALL_FAIL;
    }
    

    if (JAVACALL_FAIL == javacall_file_exist(midi_cfg_fn, sizeof(midi_cfg_fn)/sizeof(javacall_utf16))) {
    	javacall_print("Can't find midi config file\n");
    	is_midi_support = 0;
    } else {
       is_midi_support = 1;
    }
    
    max_patch_memory = 4*1024*1024;
    
    if(SDL_Init(SDL_INIT_AUDIO)==-1) {
        javacall_print("SDL_Init: %s\n", SDL_GetError());
        _javacall_media_initilized = -1;
        return JAVACALL_FAIL;
    }

    if(Mix_OpenAudio(MIX_DEFAULT_FREQUENCY, MIX_DEFAULT_FORMAT, 2, 1024)==-1) {
        javacall_print("Mix_OpenAudio error: %s\n", SDL_GetError());
        _javacall_media_initilized = -1;
        return JAVACALL_FAIL;
    }

    music_handle_occupied = 0;
    Mix_HookMusicFinished(musicFinished);

    Mix_AllocateChannels(CHANNEL_NUM);
    Mix_ChannelFinished(channelDone);
    
    
    _javacall_media_initilized = 1;
    return JAVACALL_OK;
}


/**
 * Call this function when VM ends 
 * Perfrom global free operaiton
 * 
 * @retval JOK      success
 * @retval JFAIL    fail 
 */
javacall_result javacall_media_finalize(void) {
    javacall_print("javacall_media_finalize\n");
    if (_javacall_media_initilized == 1) {
    	 _javacall_media_initilized = 0;
        Mix_CloseAudio();
        SDL_Quit();
    music_handle_occupied = 0;
    }

    return JAVACALL_OK;
}

/**
 * Java MMAPI call this function to create native media handler.
 * This function is called at the first time to initialize native library.
 * You can do your own initializatio job from this function.
 * 
 * @param playerId      Unique player object ID for this playing
 *                      This unique ID is generated by Java MMAPI library.
 * @param mime          Mime unicode string
 * @param mimeLength    String length of mimeType
 * @param uri           URI unicode string to media data
 * @param uriLength     String length of URI
 * @param contentLength Content length in bytes
 *                      If Java MMAPI couldn't determine contenet length, this value should be -1
 * 
 * @return              Handle of native library. if fail return NULL.
 */
javacall_handle javacall_media_create(javacall_int64 playerId, 
                                      const javacall_utf16* mime, 
                                      long mimeLength,
                                      const javacall_utf16* uri, 
                                      long uriLength,
                                      long contentLength)
{
    printf("javacall_media_create, contentLength=%d\n", contentLength);
    
    char *pszMime = NULL;
    
    if (_javacall_media_initilized != 1) {
    	if (javacall_media_initialize() != JAVACALL_OK) {
    		return NULL;
    	}
    }

    pszMime = javacall_UNICODEsToUtf8(mime, mimeLength);
    if (!strcmp(pszMime, JAVACALL_AUDIO_MIDI_MIME) ||
    	  !strcmp(pszMime, JAVACALL_AUDIO_MIDI_MIME_2)) {
    	 return create_midi(playerId, uri, uriLength, contentLength);
    } else if (!strcmp(pszMime, JAVACALL_AUDIO_WAV_MIME)) {
        return create_wav(playerId, uri, uriLength, contentLength);
    }

    return NULL;    
}

/**
 * Java MMAPI call this function to create native media handler.
 * This function is called at the first time to initialize native library.
 * You can do your own initializatio job from this function.
 * 
 * @param playerId      Unique player object ID for this playing
 * @param mediaType     Media content type
 * @param fileName      File name to play (device's native path format)
 * @return              Handle of native library. if fail return NULL.
 */
javacall_handle javacall_media_create2(int playerId, javacall_media_type mediaType, const char* fileName) {
     javacall_print("javacall_media_create2\n");
    return 0;
}

/**
 * Close native media player that created by creat or creat2 API call
 * After this call, you can't use any other function in this library
 * 
 * @param handle  Handle to the library.
 * @retval JAVACALL_OK      success
 * @retval JAVACALL_FAIL    fail
 */
javacall_result javacall_media_close(javacall_handle handle) {
    mmplayer_handle* mp = (mmplayer_handle*)handle;
    if (mp) {
    	 if (mp->type == MEDIA_TYPE_MIDI) {
    	     close_midi((midi_player_handle*)mp);
    	 } else if (mp->type == MEDIA_TYPE_WAVE) {
    	     close_wav((wav_player_handle*)mp);
    	 }
    }

    return JAVACALL_OK;
}

/**
 * finally destroy native media player previously closed by
 * javacall_media_close. intended to be used by finalizer
 * 
 * @param handle  Handle to the library.
 * 
 * @retval JAVACALL_OK      Java VM will proceed as if there is no problem
 * @retval JAVACALL_FAIL    Java VM will raise the media exception
 */
javacall_result javacall_media_destroy(javacall_handle handle){
    (void)handle;
    return JAVACALL_OK;
}


/**
 * Request to acquire device resources used to play media data.
 * You could implement this function to control device resource usage.
 * If there is no valid device resource to play media data, return JAVACALL_FAIL.
 * 
 * @param handle  Handle to the library.
 * @retval JAVACALL_OK      Can acquire device
 * @retval JAVACALL_FAIL    There is no valid device resource
 */
javacall_result javacall_media_acquire_device(javacall_handle handle) {
    /*
    if (music_handle_occupied) {
    	javacall_print("javacall_media_acquire_device: can't create the 2nd music player.\n");
    	return JAVACALL_FAIL;
    } else {
        music_handle_occupied = 1;
        return JAVACALL_OK;
    }
    */
    return JAVACALL_OK;
}

/**
 * Release device resource. 
 * Java MMAPI call this function to release limited device resources.
 * 
 * @param handle  Handle to the library.
 * @retval JAVACALL_OK      Can release device
 * @retval JAVACALL_FAIL    Can't release device
 */
javacall_result javacall_media_release_device(javacall_handle handle) {
    music_handle_occupied = 0;
    return JAVACALL_OK;
}

/**
 * Ask to the native layer.
 * Is this protocol handled by native layer or Java layer?
 * 
 * @retval JAVACALL_OK      Yes, this protocol handled by device.
 * @retval JAVACALL_FAIL    No, please handle this protocol from Java.
 */
javacall_result javacall_media_protocol_handled_by_device(javacall_handle handle) {
    int needBuffer = 0;
    if(handle == NULL) {
        return JAVACALL_FAIL;
    }
    
    mmplayer_handle* mp = (mmplayer_handle*)handle;
    if (mp->type == MEDIA_TYPE_MIDI) {
        needBuffer = ((midi_player_handle*)mp)->needBuffer;
    } else if (mp->type == MEDIA_TYPE_WAVE) {
        needBuffer = ((wav_player_handle*)mp)->needBuffer;
    }

    return needBuffer?JAVACALL_FAIL:JAVACALL_OK;
}

/**
 * Java MMAPI call this function to send media data to this library
 * This function can be called multiple time to send large media data
 * Native library could implement buffering by using any method (for example: file, heap and etc...)
 * And, buffering occured in sequentially. not randomly.
 * 
 * @param handle    Handle to the library
 * @param buffer    Media data buffer pointer
 * @param length    Length of media data
 * @param offset    Offset. If offset value is 0, it means start of buffering
 *                  It'll be incremented as buffering progress
 *                  You can determine your internal buffer's writting position by using this value
 * @return          If success return 'length of buffered data' else return -1
 */
long javacall_media_do_buffering(javacall_handle handle, const void* buffer, long length, long offset) {
    if ( !handle) {
        return JAVACALL_FAIL;
    }

    mmplayer_handle* mp = handle;
    if (mp->type == MEDIA_TYPE_MIDI) {
    	return buffer_midi((midi_player_handle*)mp, buffer, length, offset);
    } else if (mp->type == MEDIA_TYPE_WAVE) {
       return buffer_wav((wav_player_handle*)mp, buffer, length, offset);
    }
    return -1;
}

/**
 * MMAPI call this function to clear(delete) buffered media data
 * You have to clear any resources created from previous buffering
 * 
 * @param handle    Handle to the library
 * @retval JAVACALL_OK      Can clear buffer
 * @retval JAVACALL_FAIL    Can't clear buffer
 */
javacall_result javacall_media_clear_buffer(javacall_handle handle) {
    if ( !handle) {
        return JAVACALL_FAIL;
    }

    mmplayer_handle* mp = handle;
    if (mp->type == MEDIA_TYPE_MIDI) {
    	 clear_buffer_midi((midi_player_handle*)mp);
    } else if (mp->type == MEDIA_TYPE_WAVE) {
        clear_buffer_wav((wav_player_handle*)mp);
    }
    return JAVACALL_OK;
}

/**
 * Start media playing
 * 
 * @param handle    Handle to the library
 * @retval JAVACALL_OK      Success
 * @retval JAVACALL_FAIL    Fail
 */
javacall_result javacall_media_start(javacall_handle handle) {
    if ( !handle) {
        return JAVACALL_FAIL;
    }
    
    mmplayer_handle* mp = handle;
    if (mp->type == MEDIA_TYPE_MIDI) {
    	 return start_midi((midi_player_handle*)mp);
    } else if (mp->type == MEDIA_TYPE_WAVE) {
        return start_wav((wav_player_handle*)mp);
    }
    return JAVACALL_FAIL;
}

/**
 * Stop media playing
 * 
 * @param handle      Handle to the library
 * @retval JAVACALL_OK      Success
 * @retval JAVACALL_FAIL    Fail
 */
javacall_result javacall_media_stop(javacall_handle handle) {
    return javacall_media_pause(handle);
}

/**
 * Pause media playing
 * 
 * @param handle      Handle to the library
 * @retval JAVACALL_OK      Success
 * @retval JAVACALL_FAIL    Fail
 */
javacall_result javacall_media_pause(javacall_handle handle) {
    if ( !handle) {
        return JAVACALL_FAIL;
    }
    
    mmplayer_handle* mp = handle;
    if (mp->type == MEDIA_TYPE_MIDI) {
    	 return pause_midi((midi_player_handle*)mp);
    } else if (mp->type == MEDIA_TYPE_WAVE) {
        return pause_wav((wav_player_handle*)mp);
    }
    return JAVACALL_FAIL;
}

/**
 * Resume media playing
 * 
 * @param handle      Handle to the library
 * @retval JAVACALL_OK      Success
 * @retval JAVACALL_FAIL    Fail
 */
javacall_result javacall_media_resume(javacall_handle handle) {
    if (((mmplayer_handle*)handle)->type == MEDIA_TYPE_WAVE && 
    	  !((wav_player_handle*)handle)->paused) {
    	  return JAVACALL_OK;
    }

    if (((mmplayer_handle*)handle)->type == MEDIA_TYPE_MIDI && 
    	  !((midi_player_handle*)handle)->paused) {
    	  return JAVACALL_OK;
    }
    
    return javacall_media_start(handle);
}

/**
 * Get current playing media time
 * 
 * @param handle      Handle to the library
 * @return          If success return time in ms else return -1
 */
long javacall_media_get_time(javacall_handle handle) {
    return -1;
}

/**
 * Seek to specified time
 * 
 * @param handle      Handle to the library
 * @param ms        Seek position as ms time
 * @return          If success return time in ms else return -1
 */
long javacall_media_set_time(javacall_handle handle, long ms) {
    return -1;
}
 
/**
 * Get whole media time in ms
 * 
 * @param handle      Handle to the library
 * @return          If success return time in ms else return -1
 */
long javacall_media_get_duration(javacall_handle handle) {
    return -1;
}

/**
 * Get current audio volume
 * Audio volume range have to be in 0 to 100 inclusive
 * 
 * @param mediaType Media type. 
 *                  It could be used to determine H/W audio path for specific audio data type.
 * @return          Volume value
 */
long javacall_media_get_volume(javacall_handle handle) {
    return 100;
}

/**
 * Set audio volume
 * Audio volume range have to be in 0 to 100 inclusive
 * 
 * @param mediaType Media type. 
 *                  It could be used to determine H/W audio path for specific data type.
 * @param level     Volume value
 * @return          if success return volume level else return -1
 */
long javacall_media_set_volume(javacall_handle handle, long level) {
    return -1;
}

/**
 * Is audio muted now?
 * 
 * @param mediaType Media type. 
 *                  It could be used to determine H/W audio path for specific data type.
 * @retval JAVACALL_TRUE    Now in mute state
 * @retval JAVACALL_FALSE   Now in un-mute state
 */
javacall_bool javacall_media_is_mute(javacall_handle handle) {
    return JAVACALL_FALSE;
}

/**
 * Mute, Unmute audio
 * 
 * @param mediaType Media type. 
 *                  It could be used to determine H/W audio path for specific data type.
 * @param mute      JAVACALL_TRUE to mute, JAVACALL_FALSE to unmute
 * @retval JAVACALL_OK      Success
 * @retval JAVACALL_FAIL    Fail
 */ 
javacall_result javacall_media_set_mute(javacall_handle handle, javacall_bool mute) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * Turn on or off video rendering alpha channel.
 * If this is on OEM native layer video renderer SHOULD use this mask color
 * and draw on only the region that is filled with this color value.
 * 
 * @param on    Alpha channel is on?
 * @param color Color of alpha channel
 * 
 * @retval JAVACALL_OK      Success
 * @retval JAVACALL_FAIL    Fail
 */
javacall_result javacall_media_set_video_alpha(javacall_bool on, javacall_pixel color) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * Get original video width
 * 
 * @param handle      Handle to the library 
 * @param width     Pointer to int variable to get width of video
 *                  Caller can assign NULL value if you don't care about width value.
 * @param height    Pointer to int variable to get height of video
 *                  Caller can assign NULL value if youe don't care about height value.
 * @retval JAVACALL_OK      Success
 * @retval JAVACALL_FAIL    Fail
 */
javacall_result javacall_media_get_video_size(javacall_handle handle, 
                                              /*OUT*/ long* width, /*OUT*/ long* height) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * Set video rendering position in physical screen
 * 
 * @param handle    Handle to the library 
 * @param x         X position of rendering in pixels
 * @param y         Y position of rendering in pixels
 * @param w         Width of rendering
 * @param h         Height of rendering
 * 
 * @retval JAVACALL_OK      Success
 * @retval JAVACALL_FAIL    Fail
 */
javacall_result javacall_media_set_video_location(javacall_handle handle, long x, long y, long w, long h)
{
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * Set video preview visible state to show or hide
 * 
 * @param handle    Handle to the library
 * @param visible   JAVACALL_TRUE to show or JAVACALL_FALSE to hide
 * 
 * @retval JAVACALL_OK      Success
 * @retval JAVACALL_FAIL    Fail
 */
javacall_result javacall_media_set_video_visible(javacall_handle handle, javacall_bool visible)
{
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * Start get current snapshot of video data
 * When snapshot operation doen, call callback function to provide snapshot image data to Java.
 *
 * @param handle            Handle to the library
 * @param imageType         Snapshot image type format as unicode string. For example, "jpeg", "png" or "rgb565".
 * @param length            imageType unicode string length
 * 
 * @retval JAVACALL_OK          Success.
 * @retval JAVACALL_WOULD_BLOCK This operation could takes long time. 
 *                              After this operation finished, MUST send - IMPL_NOTE: EVENT NAME - by using 
 *                              "javanotify_on_media_notification" function call
 * @retval JAVACALL_FAIL        Fail. Invalid encodingFormat or some errors.
 */
javacall_result javacall_media_start_video_snapshot(javacall_handle handle, 
                                                    const javacall_utf16* imageType, long length)
{
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * Get snapshot data size
 * 
 * @param handle    Handle to the library
 * @param size      Size of snapshot data
 * 
 * @retval JAVACALL_OK      Success
 * @retval JAVACALL_FAIL    Fail
 */
javacall_result javacall_media_get_video_snapshot_data_size(javacall_handle handle, /*OUT*/ long* size)
{
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * Get snapshot data
 * 
 * @param handle    Handle to the library
 * @param buffer    Buffer will contains the snapshot data
 * @param size      Size of snapshot data
 * 
 * @retval JAVACALL_OK      Success
 * @retval JAVACALL_FAIL    Fail
 */
javacall_result javacall_media_get_video_snapshot_data(javacall_handle handle, /*OUT*/ char* buffer, long size)
{
    return JAVACALL_NOT_IMPLEMENTED;
}


/**
 * play simple tone
 *
 * @param note     the note to be played. From 0 to 127 inclusive.
 *                 The frequency of the note can be calculated from the following formula:
 *                    SEMITONE_CONST = 17.31234049066755 = 1/(ln(2^(1/12)))
 *                    note = ln(freq/8.176)*SEMITONE_CONST
 *                    The musical note A = MIDI note 69 (0x45) = 440 Hz.
 * @param duration the duration of the note in ms 
 * @param volume   volume of this play. From 0 to 100 inclusive.
 * @retval JAVACALL_OK      Success
 * @retval JAVACALL_FAIL    Fail
 */
javacall_result javacall_media_play_tone(long note, long duration, long volume) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * stop simple tone
 * 
 * @retval JAVACALL_OK      Success
 * @retval JAVACALL_FAIL    Fail
 */
javacall_result javacall_media_stop_tone(void) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * Get supported meta data key counts for specefic media type
 * 
 * @param mediaType [in]    Media type
 * @param keyCounts [out]   Return meta data key string counts
 * 
 * @retval JAVACALL_OK              Success
 * @retval JAVACALL_FAIL            Fail
 * @retval JAVACALL_NOT_IMPLEMENTED MetaDataControl is not implemented for this media type
 */
javacall_result javacall_media_get_metadata_key_counts(javacall_handle handle, /*OUT*/ long* keyCounts) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * Get meta data key strings by using media type and index value
 * 
 * @param mediaType [in]    Media type
 * @param index     [in]    Meta data key string's index value. from 0 to 'key counts - 1'.
 * @param bufLength [in]    keyBuf buffer's size in bytes. 
 * @param keyBuf    [out]   Buffer that used to return key strings. 
 *                          NULL value should be appended to the end of string.
 * 
 * @retval JAVACALL_OK              Success
 * @retval JAVACALL_OUT_OF_MEMORY     keyBuf size is too small
 * @retval JAVACALL_NOT_IMPLEMENTED  MetaDataControl is not implemented for this media type
 * @retval JAVACALL_FAIL            Fail
 */
javacall_result javacall_media_get_metadata_key(javacall_handle handle, 
                                                long index, long bufLength, 
                                                /*OUT*/ char* keyBuf) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * Get meta data value strings by using meta data key string
 * 
 * @param hLIB      [in]    Handle to the library 
 * @param key       [in]    Meta data key string
 * @param bufLength [in]    dataBuf buffer's size in bytes. 
 * @param dataBuf   [out]   Buffer that used to return meta data strings. 
 *                          NULL value should be appended to the end of string.
 * 
 * @retval JAVACALL_OK              Success
 * @retval JAVACALL_OUT_OF_MEMORY     dataBuf size is too small
 * @retval JAVACALL_NOT_IMPLEMENTED  MetaDataControl is not implemented for this media type
 * @retval JAVACALL_FAIL            Fail
 */
javacall_result javacall_media_get_metadata(javacall_handle handle, 
                                            const char* key, long bufLength, 
                                            /*OUT*/ char* dataBuf) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * Get volume for the given channel. 
 * The return value is independent of the master volume, which is set and retrieved with VolumeControl.
 * 
 * @param handle    Handle to the library 
 * @param channel   0-15
 * @param volume    channel volume, 0-127, or -1 if not known
 * 
 * @retval JAVACALL_OK      Success
 * @retval JAVACALL_FAIL    Fail
 */
javacall_result javacall_media_get_channel_volume(javacall_handle handle, 
                                                  long channel, /*OUT*/ long* volume) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * Set volume for the given channel. To mute, set to 0. 
 * This sets the current volume for the channel and may be overwritten during playback by events in a MIDI sequence.
 * 
 * @param handle    Handle to the library 
 * @param channel   0-15
 * @param volume    channel volume, 0-127
 * 
 * @retval JAVACALL_OK      Success
 * @retval JAVACALL_FAIL    Fail
 */
javacall_result javacall_media_set_channel_volume(javacall_handle handle, 
                                                  long channel, long volume) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * Set program of a channel. 
 * This sets the current program for the channel and may be overwritten during playback by events in a MIDI sequence.
 * 
 * @param handle    Handle to the library 
 * @param channel   0-15
 * @param bank      0-16383, or -1 for default bank
 * @param program   0-127
 * 
 * @retval JAVACALL_OK      Success
 * @retval JAVACALL_FAIL    Fail
 */
javacall_result javacall_media_set_program(javacall_handle handle, 
                                           long channel, long bank, long program) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * Sends a short MIDI event to the device.
 * 
 * @param handle    Handle to the library 
 * @param type      0x80..0xFF, excluding 0xF0 and 0xF7, which are reserved for system exclusive
 * @param data1     for 2 and 3-byte events: first data byte, 0..127
 * @param data2     for 3-byte events: second data byte, 0..127
 * 
 * @retval JAVACALL_OK      Success
 * @retval JAVACALL_FAIL    Fail
 */
javacall_result javacall_media_short_midi_event(javacall_handle handle,
                                                long type, long data1, long data2) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * Sends a long MIDI event to the device, typically a system exclusive message.
 * 
 * @param handle    Handle to the library 
 * @param data      array of the bytes to send. 
 *                  This memory buffer will be freed after this function returned.
 *                  So, you should copy this data to the other internal memory buffer
 *                  if this function needs data after return.
 * @param offset    start offset in data array
 * @param length    number of bytes to be sent
 * 
 * @retval JAVACALL_OK      Success
 * @retval JAVACALL_FAIL    Fail
 */
javacall_result javacall_media_long_midi_event(javacall_handle handle,
                                               const char* data, long offset, /*INOUT*/ long* length) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * Get maximum rate of media type
 * 
 * @param mediaType [in]    Media type
 * @param maxRate   [out]   Maximum rate value for this media type
 * 
 * @retval JAVACALL_OK              Success
 * @retval JAVACALL_NOT_IMPLEMENTED  RateControl is not implemented for this media type
 * @retval JAVACALL_FAIL            Fail
 */
javacall_result javacall_media_get_max_rate(javacall_handle handle, /*OUT*/ long* maxRate) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * Get minimum rate of media type
 * 
 * @param mediaType [in]    Media type
 * @param minRate   [out]   Minimum rate value for this media type
 * 
 * @retval JAVACALL_OK              Success
 * @retval JAVACALL_FAIL            Fail
 */
javacall_result javacall_media_get_min_rate(javacall_handle handle, /*OUT*/ long* minRate) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * Set media's current playing rate
 * 
 * @param hLIB [in] Handle to the library 
 * @param rate [in] Rate to set
 * 
 * @retval JAVACALL_OK      Success
 * @retval JAVACALL_FAIL    Fail
 */
javacall_result javacall_media_set_rate(javacall_handle handle, long rate) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * Get media's current playing rate
 * 
 * @param hLIB [in]     Handle to the library 
 * @param rate [out]    Current playing rate
 * 
 * @retval JAVACALL_OK          Success
 * @retval JAVACALL_FAIL        Fail
 */
javacall_result javacall_media_get_rate(javacall_handle handle, /*OUT*/ long* rate) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/** 
 * Get media's current playing tempo.
 * 
 * @param hLIB [in]     Handle to the library
 * @param tempo [out]   Current playing tempo
 * 
 * @retval JAVACALL_OK          Success
 * @retval JAVACALL_FAIL        Fail
 */
javacall_result javacall_media_get_tempo(javacall_handle handle, /*OUT*/ long* tempo) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * Set media's current playing tempo
 * 
 * @param hLIB [in]     Handle to the library
 * @param tempo [in]    Tempo to set
 * 
 * @retval JAVACALL_OK          Success
 * @retval JAVACALL_FAIL        Fail
 */
javacall_result javacall_media_set_tempo(javacall_handle handle, long tempo) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/******************************************************************************/

/**
 * Gets the maximum playback pitch raise supported by the Player
 * 
 * @param handle    Handle to the library 
 * @param maxPitch  The maximum pitch raise in "milli-semitones".
 * 
 * @retval JAVACALL_OK      Success
 * @retval JAVACALL_FAIL    Fail
 */
javacall_result javacall_media_get_max_pitch(javacall_handle handle, /*OUT*/ long* maxPitch) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * Gets the minimum playback pitch raise supported by the Player
 * 
 * @param handle    Handle to the library 
 * @param minPitch  The minimum pitch raise in "milli-semitones"
 * 
 * @retval JAVACALL_OK      Success
 * @retval JAVACALL_FAIL    Fail
 */
javacall_result javacall_media_get_min_pitch(javacall_handle handle, /*OUT*/ long* minPitch) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * Set media's current playing rate
 * 
 * @param handle    Handle to the library 
 * @param pitch     The number of semi tones to raise the playback pitch. It is specified in "milli-semitones"
 * 
 * @retval JAVACALL_OK      Success
 * @retval JAVACALL_FAIL    Fail
 */
javacall_result javacall_media_set_pitch(javacall_handle handle, long pitch) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * Get media's current playing rate
 * 
 * @param handle    Handle to the library 
 * @param pitch     The current playback pitch raise in "milli-semitones"
 * 
 * @retval JAVACALL_OK          Success
 * @retval JAVACALL_FAIL        Fail
 */
javacall_result javacall_media_get_pitch(javacall_handle handle, /*OUT*/ long* pitch) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/******************************************************************************/

/**
 * Query if recording is supported based on the player's content-type
 * 
 * @param handle  Handle to the library 
 * 
 * @retval JAVACALL_OK          Success
 * @retval JAVACALL_FAIL        Fail
 */
javacall_result javacall_media_supports_recording(javacall_handle handle) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * Is this recording transaction is handled by native layer or Java layer?
 * 
 * @param handle    Handle to the library 
 * @param locator   URL locator string for recording data (ex: file:///root/test.wav)
 * 
 * @retval JAVACALL_OK      This recording transaction will be handled by native layer
 * @retval JAVACALL_FAIL    This recording transaction should be handled by Java layer
 */
javacall_result javacall_media_recording_handled_by_native(javacall_handle handle, 
                                                           const javacall_utf16* locator,
                                                           long locatorLength) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * Is javacall_media_set_recordsize_limit function is working for this player?
 * 
 * @retval JAVACALL_TRUE    Yes. Supported.
 * @retval JAVACALL_FALSE   No. Not supported.
 */
javacall_bool javacall_media_set_recordsize_limit_supported(javacall_handle handle) {
    return JAVACALL_FALSE;
}

/**
 * Specify the maximum size of the recording including any headers.
 * If a size of -1 is passed then the record size limit should be removed.
 * 
 * @param handle    Handle to the library 
 * @param size      The maximum size bytes of the recording requested as input parameter.
 *                  The supported maximum size bytes of the recording which is less than or 
 *                  equal to the requested size as output parameter.
 * 
 * @retval JAVACALL_OK          Success
 * @retval JAVACALL_FAIL        Fail
 */
javacall_result javacall_media_set_recordsize_limit(javacall_handle handle, 
                                                    /*INOUT*/ long* size) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * Starts the recording. records all the data of the player ( video / audio )
 * 
 * @param handle  Handle to the library 
 * 
 * @retval JAVACALL_OK          Success
 * @retval JAVACALL_FAIL        Fail
 */
javacall_result javacall_media_start_recording(javacall_handle handle) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * Pause the recording. this should enable a future call to javacall_media_start_recording. 
 * Another call to javacall_media_start_recording after pause has been called will result 
 * in recording the new data and concatanating it to the previously recorded data.
 * 
 * @param handle  Handle to the library 
 * 
 * @retval JAVACALL_OK          Success
 * @retval JAVACALL_FAIL        Fail
 */
javacall_result javacall_media_pause_recording(javacall_handle handle) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * Stop the recording.
 * 
 * @param handle  Handle to the library 
 * 
 * @retval JAVACALL_OK          Success
 * @retval JAVACALL_FAIL        Fail
 */
javacall_result javacall_media_stop_recording(javacall_handle handle) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The recording that has been done so far should be discarded. (deleted)
 * Recording will be paused before this method is called. 
 * If javacall_media_start_recording is called after this method is called, recording should resume.
 * Calling reset after javacall_media_finish_recording will have no effect on the current recording.
 * If the Player that is associated with this RecordControl is closed, 
 * javacall_media_reset_recording will be called implicitly. 
 * 
 * @param handle  Handle to the library 
 * 
 * @retval JAVACALL_OK          Success
 * @retval JAVACALL_FAIL        Fail
 */
javacall_result javacall_media_reset_recording(javacall_handle handle) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * The recording should be completed; 
 * this may involve updating the header,flushing buffers and closing the temporary file if it is used
 * by the implementation.
 * javacall_media_pause_recording will be called before this method is called.
 * 
 * @param handle  Handle to the library 
 * 
 * @retval JAVACALL_OK          Success
 * @retval JAVACALL_FAIL        Fail
 */
javacall_result javacall_media_commit_recording(javacall_handle handle) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * Get how much data was returned. 
 * This function can be called after a successful call to javacall_media_finish_recording.
 * 
 * @param handle    Handle to the library 
 * @param size      How much data was recorded
 * 
 * @retval JAVACALL_OK          Success
 * @retval JAVACALL_FAIL        Fail
 */
javacall_result javacall_media_get_recorded_data_size(javacall_handle handle, 
                                                      /*OUT*/ long* size) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * Gets the recorded data.
 * This function can be called after a successful call to javacall_media_finish_recording.
 * It receives the data recorded from offset till the size.
 * 
 * @param handle    Handle to the library 
 * @param buffer    Buffer will contains the recorded data
 * @param offset    An offset to the start of the required recorded data
 * @param size      How much data will be copied to buffer
 * 
 * @retval JAVACALL_OK          Success
 * @retval JAVACALL_FAIL        Fail
 */
javacall_result javacall_media_get_recorded_data(javacall_handle handle, 
                                                 /*OUT*/ char* buffer, long offset, long size) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * Get the current recording data content type mime string length
 *
 * @return  If success return length of string else return 0
 */
int javacall_media_get_record_content_type_length(javacall_handle handle) {
    return 0;
}

/**
 * Get the current recording data content type mime string length
 * For example : 'audio/x-wav' for audio recording
 *
 * @param handle                Handle of native player
 * @param contentTypeBuf        Buffer to return content type unicode string
 * @param contentTypeBufLength  Lenght of contentTypeBuf buffer (in unicode metrics)
 *
 * @return  Length of content type string stored in contentTypeBuf
 */
int javacall_media_get_record_content_type(javacall_handle handle, 
                                           /*OUT*/ javacall_utf16* contentTypeBuf,
                                           int contentTypeBufLength) {
    return 0;
}

/**
 * Close the recording. Delete all resources related with this recording.
 * 
 * @param handle    Handle to the library 
 * 
 * @retval JAVACALL_OK      Success
 * @retval JAVACALL_FAIL    Fail
 */
javacall_result javacall_media_close_recording(javacall_handle handle) {
    return JAVACALL_NOT_IMPLEMENTED;
}


/**
 * This function called by JVM when this player goes to foreground.
 * There is only one foreground midlets but, multiple player can be exits at this midlets.
 * So, there could be multiple players from JVM.
 * Device resource handling policy is not part of Java implementation. It is totally depends on
 * native layer's implementation.
 * 
 * @param handle    Handle to the native player
 * @param option    MVM options. Check about javacall_media_mvm_option type definition.
 * 
 * @retval JAVACALL_OK  Somthing happened
 * @retval JAVACALL_OK  Nothing happened
 */
javacall_result javacall_media_to_foreground(javacall_handle handle,
                                             javacall_media_mvm_option option) {
    if (option != JAVACALL_MEDIA_PLAYABLE_FROM_BACKGROUND) {
    	printf("javacall_media_to_foreground\n");
        javacall_media_resume(handle);
    }
    return JAVACALL_OK;
}

/**
 * This function called by JVM when this player goes to background.
 * There could be multiple background midlets. Also, multiple player can be exits at this midlets.
 * Device resource handling policy is not part of Java implementation. It is totally depends on
 * native layer's implementation.
 * 
 * @param handle    Handle to the native player
 * @param option    MVM options. Check about javacall_media_mvm_option type definition.
 * 
 * @retval JAVACALL_OK  Somthing happened
 * @retval JAVACALL_OK  Nothing happened
 */
javacall_result javacall_media_to_background(javacall_handle handle,
                                             javacall_media_mvm_option option) {
    if (option != JAVACALL_MEDIA_PLAYABLE_FROM_BACKGROUND) {
        javacall_media_pause(handle);
    }                           
    return JAVACALL_OK;
}


/**
 * This function is used to ascertain the availability of MIDI bank support
 *
 * @param handle     Handle to the native player
 * @param supported  return of support availability
 *
 * @retval JAVACALL_OK      MIDI Bank Query support is available
 * @retval JAVACALL_FAIL    NO MIDI Bank Query support is available
 */
javacall_result javacall_media_is_midibank_query_supported(javacall_handle handle,
                                             long* supported) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * This function is used to get a list of installed banks. If the custom
 * parameter is true, a list of custom banks is returned. Otherwise, a list of
 * all banks (custom and internal) is returned. This function can be left empty.
 *
 * @param handle    Handle to the native player
 * @param custom    a flag indicating whether to return just custom banks, or
 *                  all banks.
 * @param banklist  an array which will be filled out with the banklist
 * @param numlist   the length of the array to be filled out, and on return
 *                  contains the number of values written to the array.
 *
 * @retval JAVACALL_OK      Bank List is available
 * @retval JAVACALL_FAIL    Bank List is NOT available
 */
javacall_result javacall_media_get_midibank_list(javacall_handle handle,
                                             long custom,
                                             /*OUT*/short* banklist,
                                             /*INOUT*/long* numlist) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * Given a bank, program and key, get name of key. This function applies to
 * key-mapped banks (i.e. percussive banks or effect banks) only. If the returned
 * keyname length is 0, then the key is not mapped to a sound. For melodic banks,
 * where each key (=note) produces the same sound at different pitch, this
 * function always returns a 0 length string. For space saving reasons an
 * implementation may return a 0 length string instead of the keyname. This
 * can be left empty.
 *
 * @param handle    Handle to the native player
 * @param bank      The bank to query
 * @param program   The program to query
 * @param key       The key to query
 * @param keyname   The name of the key returned.
 * @param keynameLen    The length of the keyname array, and on return the
 *                      length of the keyname.
 *
 * @retval JAVACALL_OK      Keyname available
 * @retval JAVACALL_FAIL    Keyname not supported
 */
javacall_result javacall_media_get_midibank_key_name(javacall_handle handle,
                                            long bank,
                                            long program,
                                            long key,
                                            /*OUT*/char* keyname,
                                            /*INOUT*/long* keynameLen) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * Given the bank and program, get name of program. For space-saving reasons
 * a 0 length string may be returned.
 *
 * @param handle    Handle to the native player
 * @param bank      The bank being queried
 * @param program   The program being queried
 * @param progname  The name of the program returned
 * @param prognameLen    The length of the progname array, and on return the
 *                       length of the progname
 *
 * @retval JAVACALL_OK      Program name available
 * @retval JAVACALL_FAIL    Program name not supported
 */
javacall_result javacall_media_get_midibank_program_name(javacall_handle handle,
                                                long bank,
                                                long program,
                                                /*OUT*/char* progname,
                                                /*INOUT*/long* prognameLen) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * Given bank, get list of program numbers. If and only if this bank is not
 * installed, an empty array is returned.
 *
 * @param handle    Handle to the native player
 * @param bank      The bank being queried
 * @param proglist  The Program List being returned
 * @param proglistLen     The length of the proglist, and on return the number
 *                        of program numbers in the list
 *
 * @retval JAVACALL_OK     Program list available
 * @retval JAVACALL_FAIL   Program list unsupported
 */
javacall_result javacall_media_get_midibank_program_list(javacall_handle handle,
                                                long bank,
                                                /*OUT*/char* proglist,
                                                /*INOUT*/long* proglistLen) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/**
 * Returns the program assigned to the channel. It represents the current state
 * of the channel. During playbank of the MIDI file, the program may change due
 * to program change events in the MIDI file. The returned array is represented
 * by an array {bank, program}. The support of this function is optional.
 *
 * @param handle    Handle to the native player
 * @param channel   The channel being queried
 * @param prog      The return array (size 2) in the form {bank, program}
 *
 * @retval JAVACALL_OK    Program available
 * @retval JAVACALL_FAIL  Get Program unsupported
 */
javacall_result javacall_media_get_midibank_program(javacall_handle handle,
                                                long channel,
                                                /*OUT*/long* prog) {
    return JAVACALL_NOT_IMPLEMENTED;
}

/*****************
 *  MIDI functions
 *
 *****************/
static mmplayer_handle* create_midi(javacall_int64 playerId, const javacall_utf16* uri, long uriLength, long contentLength) {
    int needBuffer;
    midi_player_handle* handle;
    char *pszUri = NULL;
    
    //Check if midi is supported by platform
    if (!is_midi_support) {
    	return 0;
    }

    if (uri == NULL || uriLength <= 0) {
    	//Read from stream, need buffer
    	needBuffer = 1;
    } else {
       pszUri = javacall_UNICODEsToUtf8(uri, uriLength);
       if (!strncmp(pszUri, "http://", 7) && strlen(pszUri) < 256) {
           needBuffer = 1;
       } else if (strlen(pszUri) >= 256 || strncmp(pszUri, "file://", 7)) {
           printf("Not supported URI:%s\n", pszUri);
           return 0;
       } else {
           needBuffer = 0;
       }
    }

    handle = malloc(sizeof(midi_player_handle));

    handle->needBuffer = needBuffer;

#if 0
    /***
      * Currently, Mix_LoadMUS_RW is not supported MIDI, we have to comment this code out,
      * to forcely use file-buffer midi playing.
      ***/
    if (contentLength > 0 && contentLength < DEFAULT_WAVE_BUFFER_LENGTH) {
    	 handle->buffer = malloc(contentLength);
    	 if (!handle->buffer) {
    	    free(handle);
    	    return 0;
        }
    	 handle->contentLength = contentLength;
    } else if (contentLength < 0) {
        handle->buffer = malloc(DEFAULT_WAVE_BUFFER_LENGTH);
        if (!handle->buffer) {
    	    free(handle);
    	    return 0;
        }
        handle->contentLength = DEFAULT_WAVE_BUFFER_LENGTH;
    } else
#endif
    {
        handle->buffer = NULL;
        handle->contentLength = contentLength;
    }    

    handle->buffered_len = 0;
    
    handle->music = NULL;
    if (pszUri == NULL) {
        handle->filename[0] = '\0';
    } else {
        strcpy(handle->filename, pszUri);
    }
    handle->tmpfilename[0] = (javacall_utf16)0;
    handle->tmpfilenamelen = 0;
    handle->occupied = 1;
    handle->fp = NULL;
    handle->playerId = playerId;
    handle->type = MEDIA_TYPE_MIDI;
    handle->paused = 0;
    
    return (mmplayer_handle*)handle;
}


static void close_midi(midi_player_handle* mp) {
	printf("close_midi\n");
	if (mp->music) {
            Mix_FreeMusic(mp->music);
    	 }

	 if (mp->buffer) {
	     free(mp->buffer);
	 }
        
        mp->needBuffer = 0;
        mp->contentLength = 0;
        mp->buffer = 0;
        mp->buffered_len = 0;
        mp->music = NULL;
        mp->occupied = 0;
        mp->filename[0] = '\0';
        if (mp->fp) {
        	javacall_file_close(mp->fp);
        	mp->fp = NULL;
        }
        if (mp->tmpfilenamelen > 0) {
        	javacall_file_delete(mp->tmpfilename, mp->tmpfilenamelen);
        }
        mp->tmpfilename[0] = (javacall_utf16)0;
        mp->tmpfilenamelen = 0;
        mp->playerId = INVALID_PLAYER_ID;
        mp->paused = 0;
        free(mp);
        
        current_playing = INVALID_PLAYER_ID;
}

static long buffer_midi(midi_player_handle* mp, const void* buffer, long length, long offset) {
    javacall_handle hFile;
    int ret;
    static char tmpfilename[64];

    if (mp->music) {
        return length>0?length:0;
    }

    if (buffer == NULL || length <= 0) {
    	Mix_Music *music = NULL;
    	
    	//end of buffering
    	javacall_print("endof buffering\n");
    	if (mp->buffer) {
    		SDL_RWops * rw = SDL_RWFromMem(mp->buffer, mp->buffered_len);
    		music = Mix_LoadMUS_RW(rw);
    	} else if (mp->fp) {
    		
    		javacall_print("close tmp buffer file\n");
    		javacall_file_close(mp->fp);
    		mp->fp = NULL;
    		strcpy(mp->filename, javacall_UNICODEsToUtf8(mp->tmpfilename, mp->tmpfilenamelen));
    		    		
    		music=Mix_LoadMUS(mp->filename);
   		
    	}

    	if(!music) {
        	printf("Mix_LoadMUS(): %s\n", Mix_GetError());
        	// this might be a critical error...
      		return -1;
    	}

    	mp->music = music;
    	
    	return 0;
    }
    
    if (mp->fp) {
    	 javacall_print("Continue buffering to tmp file\n");
    	 ret = javacall_file_write(mp->fp, buffer, length);
    } else if (mp->buffer && ((length + offset) <= mp->contentLength)) {
        javacall_print("Buffer midi %d bytes from %d\n", length, offset);
        memcpy(mp->buffer+offset, buffer, length);
        mp->buffered_len += length;
        ret = length;
    }  else {
        int i;
        sprintf(tmpfilename, "%08x_tmp_mus.mid", mp);

        if (mp->buffer) {
            free(mp->buffer);
            mp->buffer = NULL;
            mp->buffered_len = 0;
            mp->contentLength = 0;
        }
        
        for (i = 0; i < TMP_MUSIC_FILENAME_LEN_MAX; i++) {
        	mp->tmpfilename[i] = (javacall_utf16)tmpfilename[i];
        	if (!tmpfilename[i]) {
        		mp->tmpfilenamelen = i;
        		break;
        	}
        }
        
        if (JAVACALL_OK != javacall_file_open(mp->tmpfilename, mp->tmpfilenamelen, JAVACALL_FILE_O_WRONLY | JAVACALL_FILE_O_CREAT | JAVACALL_FILE_O_TRUNC, &hFile)) {
            javacall_print("Create file temp music file failed\n");
            return -1;
        } else {
            javacall_print("Buffering to tmp file\n");
            mp->fp = hFile;
            ret = javacall_file_write(mp->fp, buffer, length);
        }
    }

    printf("%d bytes buffered\n", ret);
    return ret;
}

static void clear_buffer_midi(midi_player_handle* mp) {
    /*
    javacall_print("Clear buffer\n");
    if (mp && mp->fp) {    	
    	javacall_file_close(mp->fp);
    	mp->fp = NULL;
    }
    mp->filename[0] = '\0';
    */
}

static javacall_result start_midi(midi_player_handle* mp) {
    if (current_playing != INVALID_PLAYER_ID &&
    	  current_playing != mp->playerId) {
    	javanotify_on_media_notification(JAVACALL_EVENT_MEDIA_END_OF_MEDIA, current_playing, 0);
    }

    Mix_Music * music;
    if (!mp->music && !mp->needBuffer) {
        music = Mix_LoadMUS(mp->filename);
    } else { 
        music = mp->music;
    }
    
    if (music) {
    	Mix_PlayMusic(music, 1);
    	current_playing = mp->playerId;
    	return JAVACALL_OK;
    } else {
       return JAVACALL_FAIL;
    }

}

static javacall_result pause_midi(midi_player_handle* mp) {
     if (mp->music &&
    	 (current_playing == mp->playerId)) {

    	current_playing = INVALID_PLAYER_ID;
    	Mix_PauseMusic();
    	mp->paused = 1;
    	return JAVACALL_OK;
    } else {
       return JAVACALL_FAIL;
    }

}

/*****************
 *  WAV functions
 *
 *****************/
static mmplayer_handle* create_wav(javacall_int64 playerId, const javacall_utf16* uri, long uriLength, long contentLength) {
    int needBuffer;
    wav_player_handle* handle;
    char* pszUri = NULL;
    
    if (uri == NULL || uriLength <= 0) {
    	//Read from stream, need buffer
    	needBuffer = 1;
    } else {
       pszUri = javacall_UNICODEsToUtf8(uri, uriLength);
       if (!strncmp(pszUri, "http://", 7) && strlen(pszUri) < 256) {
           needBuffer = 1;
       } else if (strlen(pszUri) >= 256 || strncmp(pszUri, "file://", 7)) {
           printf("Not supported URI:%s\n", pszUri);
           return 0;
       } else {
           needBuffer = 0;
       }
    }

    handle = malloc(sizeof(wav_player_handle));
    if (!handle) {
        return 0;
    }

    handle->needBuffer = needBuffer;
    
    if (contentLength > 0 && contentLength < DEFAULT_WAVE_BUFFER_LENGTH) {
    	 handle->buffer = malloc(contentLength);
    	 if (!handle->buffer) {
    	    free(handle);
    	    return 0;
        }
    	 handle->contentLength = contentLength;
    } else if (contentLength < 0) {
        handle->buffer = malloc(DEFAULT_WAVE_BUFFER_LENGTH);
        if (!handle->buffer) {
    	    free(handle);
    	    return 0;
        }
        handle->contentLength = DEFAULT_WAVE_BUFFER_LENGTH;
    } else {
        handle->buffer = NULL;
        handle->contentLength = contentLength;
    }    
    
    handle->buffered_len = 0;
    
    handle->chunk = NULL;
    if (pszUri == NULL) {
        handle->filename[0] = '\0';
    } else {
        strcpy(handle->filename, pszUri);
    }
    
    handle->tmpfilename[0] = (javacall_utf16)0;
    handle->tmpfilenamelen = 0;
    handle->fp = NULL;
    
    handle->playerId = playerId;
    handle->type = MEDIA_TYPE_WAVE;
    handle->channel = -1;
    handle->paused = 0;

    /* add player to the list*/
    handle->prev = NULL;
    handle->next = wav_players;
    if (wav_players) {
    	wav_players->prev = handle;
    }
    wav_players = handle;
    
    return (mmplayer_handle*)handle;
}

static void close_wav(wav_player_handle* mp) {
	 printf("close_wav\n");
	 if (mp->channel >= 0) {
	     Mix_HaltChannel(mp->channel);
	 }
	 if (mp->chunk) {
            Mix_FreeChunk(mp->chunk);
    	 }
	 if (mp->buffer) {
	     free(mp->buffer);
	 }
	 
        mp->channel = -1;
        mp->needBuffer = 0;
        mp->contentLength = 0;
        mp->buffered_len = 0;
        mp->chunk = NULL;
        mp->buffer = NULL;
        mp->filename[0] = '\0';
        
        if (mp->fp) {
        	javacall_file_close(mp->fp);
        	mp->fp = NULL;
        }
        if (mp->tmpfilenamelen > 0) {
        	javacall_file_delete(mp->tmpfilename, mp->tmpfilenamelen);
        }
        mp->tmpfilename[0] = (javacall_utf16)0;
        mp->tmpfilenamelen = 0;
        
        mp->playerId = INVALID_PLAYER_ID;
        mp->paused = 0;

        /* dequeue mp from the list*/
        if (mp->prev) {
            mp->prev->next = mp->next;
        } else {
            wav_players = mp->next;
        }
        if (mp->next) {
            mp->next->prev = mp->prev;
        }
        
        free(mp);	
}

static long buffer_wav(wav_player_handle* mp, const void* buffer, long length, long offset) {
    javacall_handle hFile;
    int ret;
    static char tmpfilename[64];

    if (mp->chunk) {
        return length>0?length:0;
    }

    if (buffer == NULL || length <= 0) {
    	Mix_Chunk *chunk = NULL;
    		
    	//end of buffering
    	javacall_print("endof buffering\n");
    	if (mp->buffer) {
    		SDL_RWops * rw = SDL_RWFromMem(mp->buffer, mp->buffered_len);
    		chunk = Mix_LoadWAV_RW(rw, 1);
    	} else if (mp->fp) {
    		
    		javacall_print("close tmp buffer file\n");
    		javacall_file_close(mp->fp);
    		mp->fp = NULL;
    		strcpy(mp->filename, javacall_UNICODEsToUtf8(mp->tmpfilename, mp->tmpfilenamelen));
    		    		
    		chunk=Mix_LoadWAV(mp->filename);
    	}
    	
   	if(!chunk) {
       	printf("Mix_LoadWAV(): %s\n", Mix_GetError());
       	// this might be a critical error...
       	return -1;
    	}
    
    	mp->chunk = chunk;
    	
    	return 0;
    }
    
    if (mp->fp) {
    	 javacall_print("Continue buffering to tmp file\n");
    	 ret = javacall_file_write(mp->fp, buffer, length);
    } else if (mp->buffer && ((length + offset) <= mp->contentLength)) {
        javacall_print("Buffer wav %d bytes from %d\n", length, offset);
        memcpy(mp->buffer+offset, buffer, length);
        mp->buffered_len += length;
        ret = length;
    } else {
        int i;
        sprintf(tmpfilename, "%08x_tmp_wav.wav", mp);

        if (mp->buffer) {
            free(mp->buffer);
            mp->buffer = NULL;
            mp->buffered_len = 0;
            mp->contentLength = 0;
        }
        
        for (i = 0; i < TMP_MUSIC_FILENAME_LEN_MAX; i++) {
        	mp->tmpfilename[i] = (javacall_utf16)tmpfilename[i];
        	if (!tmpfilename[i]) {
        		mp->tmpfilenamelen = i;
        		break;
        	}
        }
        
        if (JAVACALL_OK != javacall_file_open(mp->tmpfilename, mp->tmpfilenamelen, JAVACALL_FILE_O_WRONLY | JAVACALL_FILE_O_CREAT | JAVACALL_FILE_O_TRUNC, &hFile)) {
            javacall_print("Create file temp music file failed\n");
            return -1;
        } else {
            javacall_print("Buffering to tmp file\n");
            mp->fp = hFile;
            ret = javacall_file_write(mp->fp, buffer, length);
        }
    }

    printf("%d bytes buffered\n", ret);
    return ret;

}

static void clear_buffer_wav(wav_player_handle* mp) {
/*
    javacall_print("Clear buffer\n");
    if (mp && mp->fp) {    	
    	javacall_file_close(mp->fp);
    	mp->fp = NULL;
    }
    mp->filename[0] = '\0';
*/
}

static javacall_result start_wav(wav_player_handle* mp) {
    Mix_Chunk * chunk;
    printf("start_wav\n");
    if (!mp->chunk && !mp->needBuffer) {
    	printf("Mix_LoadWAV...\n");
        chunk = Mix_LoadWAV(mp->filename);
    } else { 
        chunk = mp->chunk;
    }
    
    if (chunk) {
    	printf("Mix_PlayChannel %d...\n", mp->channel);
    	if ((mp->channel = Mix_PlayChannel(mp->channel, chunk, 0)) == -1) {
    	    printf("Mix_PlayChannel: %s\n",Mix_GetError());
    	    return JAVACALL_FAIL;
    	} else {
    	    return JAVACALL_OK;
    	}
    } else {
       return JAVACALL_FAIL;
    }
}

static javacall_result pause_wav(wav_player_handle* mp) {
     if (mp->chunk && mp->channel >= 0) {
    	Mix_Pause(mp->channel);
    	//mp->channel = -1;
    	mp->paused = 1;
    	return JAVACALL_OK;
    } else {
       return JAVACALL_FAIL;
    }

}

#ifdef __cplusplus
}
#endif 
 
