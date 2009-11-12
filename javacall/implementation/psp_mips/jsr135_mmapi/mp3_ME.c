#include <stdio.h>
#include <stdlib.h>
#include <pspkernel.h>
#include <pspsdk.h>
#include <pspaudiocodec.h>
#include <pspaudio.h>
#include <string.h>
#include <malloc.h>
#include <pspmpeg.h>
#include "psp_mmapi.h"
#include "javacall_logging.h"
#include "mp3_ME.h"

//#define MP3_DEBUG
//#define MP3_DEBUG_VERBOSE

#ifdef MP3_DEBUG
#define MP3_DEBUG_STR(_x_) \
    javacall_printf((_x_))
#define MP3_DEBUG_STR1(_x_, _p1_) \
    javacall_printf((_x_), (_p1_))
#define MP3_DEBUG_STR2(_x_, _p1_, _p2_) \
    javacall_printf((_x_), (_p1_), (_p2_))
#define MP3_DEBUG_STR3(_x_, _p1_, _p2_, _p3_) \
    javacall_printf((_x_), (_p1_), (_p2_), (_p3_))
#define MP3_DEBUG_STR4(_x_, _p1_, _p2_, _p3_, _p4_) \
    javacall_printf((_x_), (_p1_), (_p2_), (_p3_), (_p4_))
#else
#define MP3_DEBUG_STR(_x_)
#define MP3_DEBUG_STR1(_x_, _p1_)
#define MP3_DEBUG_STR2(_x_, _p1_, _p2_)
#define MP3_DEBUG_STR3(_x_, _p1_, _p2_, _p3_)
#define MP3_DEBUG_STR4(_x_, _p1_, _p2_, _p3_, _p4_)
#endif

static int bitrates[] =
  { 0, 32, 40, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320 };
static int bitrates_v2[] =
  { 0, 8, 16, 24, 32, 40, 48, 56, 64, 80, 96, 112, 128, 144, 160 };

static int samplerates[4][3] = {
  {11025, 12000, 8000,},	//mpeg 2.5
  {0, 0, 0,},			//reserved
  {22050, 24000, 16000,},	//mpeg 2
  {44100, 48000, 32000}		//mpeg 1
};

static unsigned long mp3_codec_buffer[65] __attribute__ ((aligned (64)));
static short mp3_mix_buffer[1152 * 2] __attribute__ ((aligned (64)));
static short mp3_output_buffer[4][1152 * 2] __attribute__ ((aligned (64)));
static char frame_buf[1024] __attribute__ ((aligned (64)));

static int mp3_output_index = 0;
static mp3_player_handle* currentMp3 = NULL;

static int SeekNextFrame (mp3_player_handle* mp, SceUID fd);


static int
Init_codec_buffer ()
{
  int ret = 0;

  memset (mp3_codec_buffer, 0, sizeof (mp3_codec_buffer));
  if (sceAudiocodecCheckNeedMem (mp3_codec_buffer, 0x1002) < 0)
    {
      MP3_DEBUG_STR ("sceAudiocodecCheckNeedMem fail\n");
      ret = -1;
    }

  if (sceAudiocodecGetEDRAM (mp3_codec_buffer, 0x1002) < 0)
    {
      MP3_DEBUG_STR ("sceAudiocodecGetEDRAM fail\n");
      ret = -2;
    }

  if (sceAudiocodecInit (mp3_codec_buffer, 0x1002) < 0)
    {
      MP3_DEBUG_STR ("sceAudiocodecInit fail\n");
      ret = -3;
    }

  return ret;
}

int
Mp3thread (SceSize args, void *argp)
{
  SceUID mp3_handle = -1;
  int mp3_samplerate, mp3_sample_per_frame, frame_size;
  int mp3_output_index = 0;
  int samplesdecoded = 0;
  int last_frame_size = 0, last_mp3_sample_per_frame = 0;
  char *mp3_data_buffer = NULL;
  int codec_init;
  int audio_channel_set = -1;
  unsigned int total_time;

  mp3_player_handle *mp = *(mp3_player_handle **) argp;

  if ((codec_init = Init_codec_buffer ()) != 0)
    {
      mp->isOpen = -1;
      mp->isPlaying = 0;
    }

  mp3_handle = sceIoOpen (&(mp->filename[0]), PSP_O_RDONLY, 0777);
  if (!mp3_handle)
    {
      MP3_DEBUG_STR1("%s open failed\n", &(mp->filename[0]));
      mp->isOpen = -1;
      mp->isPlaying = 0;
    }
  else
    {    
      MP3_DEBUG_STR1("%s open ok\n", &(mp->filename[0]));
      mp->mp3_file_size = sceIoLseek32(mp3_handle, 0, PSP_SEEK_END);
      sceIoLseek32(mp3_handle, 0, PSP_SEEK_SET);
      MP3_DEBUG_STR1("file size=%d", mp->mp3_file_size);
    }

  if (mp->isOpen > 0)
    {
      mp->mp3_data_start = SeekNextFrame (mp, mp3_handle);
      if (mp->mp3_data_start < 0)
	{
	  MP3_DEBUG_STR("Mp3_load:SeekNextFrame return fail\n");
	  mp->isOpen = -1;
	  mp->isPlaying = 0;
	}
    }

  while (mp->isOpen > 0)
    {

      while (mp->isPlaying > 0)
	{
	  unsigned char mp3_header_buf[4];

	  if (mp->set2time >= 0) {
             int offset;
             unsigned int percentage = mp->set2time * 100 / mp->totalTime;
             offset = (mp->mp3_file_size - mp->id3HeaderSize - mp->ea3HeaderSize) * percentage / 100;
             offset += mp->id3HeaderSize + mp->ea3HeaderSize;
             sceIoLseek32 (mp3_handle, offset, PSP_SEEK_SET);
             mp->mp3_data_start = SeekNextFrame (mp, mp3_handle);
             mp->set2time = -1;
        }
	  
	  memset (mp3_mix_buffer, 0, sizeof (mp3_mix_buffer));

	  if (sceIoRead (mp3_handle, mp3_header_buf, 4) != 4)
	    {
	      MP3_DEBUG_STR("Mp3thread:End of media\n");
	      mp->isPlaying = 0;
	      continue;
	    }
	  int mp3_header = mp3_header_buf[0];
	  mp3_header = (mp3_header << 8) | mp3_header_buf[1];
	  mp3_header = (mp3_header << 8) | mp3_header_buf[2];
	  mp3_header = (mp3_header << 8) | mp3_header_buf[3];

	  int bitrate_value;
	  int bitrate = (mp3_header & 0xf000) >> 12;
	  int padding = (mp3_header & 0x200) >> 9;
	  int version = (mp3_header & 0x180000) >> 19;
	  int numChannel = ((mp3_header & 0xC0) >> 6)==3?1:2;
	  mp3_samplerate = samplerates[version][(mp3_header & 0xC00) >> 10];

#ifdef MP3_DEBUG_VERBOSE
	  MP3_DEBUG_STR4("numChannel: %d, bitrate:%d, samplerate:%d, version:%d\n", numChannel, bitrate,
	   mp3_samplerate, version);
#endif

	  if ((bitrate > 14) || (version == 1) || (mp3_samplerate <= 0) || (bitrate <= 0))	//invalid frame, look for the next one
	    {
	      mp->mp3_data_start = SeekNextFrame (mp, mp3_handle);
	      if (mp->mp3_data_start < 0)
		{
		  MP3_DEBUG_STR("Mp3thread:SeekNextFrame return fail\n");
		  mp->isPlaying = 0;
		  continue;
		}
	      continue;
	    }

	  if ( version == 3) //mpeg 1
	    {
	      mp3_sample_per_frame = 1152;
	      bitrate_value = bitrates[bitrate];
	      frame_size =
		144000 * bitrates[bitrate] / mp3_samplerate + padding;
	    }
	  else
	    {
	      mp3_sample_per_frame = 576;
	      bitrate_value = bitrates_v2[bitrate] * 1000;
	      frame_size =
	         72000 * bitrate_value / mp3_samplerate + padding;
	    }

	  if (audio_channel_set < 0 || mp3_sample_per_frame != last_mp3_sample_per_frame)
	    {
	      if (audio_channel_set >= 0) {
	      	  MP3_DEBUG_STR("sceAudioSRCChRelease\n");
		  sceAudioSRCChRelease ();
		  audio_channel_set = 0;
             }
	      
	      if ((audio_channel_set= sceAudioSRCChReserve (mp3_sample_per_frame, mp3_samplerate, 2)) >= 0) {
	      	  MP3_DEBUG_STR4("sceAudioSRCChReserve OK: %x. mp3_sample_per_frame=%d, mp3_samplerate=%d, numChannel=%d\n",
	         	audio_channel_set, mp3_sample_per_frame, mp3_samplerate, numChannel);
	      } else {
	         MP3_DEBUG_STR4("sceAudioSRCChReserve failed: %x. mp3_sample_per_frame=%d, mp3_samplerate=%d, numChannel=%d\n",
	         	audio_channel_set, mp3_sample_per_frame, mp3_samplerate, numChannel);
	      }

	      last_mp3_sample_per_frame = mp3_sample_per_frame;
	    }

	  if (frame_size != last_frame_size) {

       	  if (mp3_data_buffer)
       	    {
       	      free (mp3_data_buffer);
       	    }
       
       	  if (NULL == (mp3_data_buffer = (char *) memalign (64, frame_size)))
       	    {
       	      mp->isPlaying = 0;
       	      last_frame_size = 0;
       	      continue;
       	    }
	  	      
                  last_frame_size = frame_size;

                  total_time = (mp->mp3_file_size - mp->id3HeaderSize - mp->ea3HeaderSize); //Data size
                  total_time = total_time/(bitrate_value>>3);

                  if (total_time != mp->totalTime) {
                  	if (mp->totalTime == 0) {
                  	    mp->totalTime = total_time;
                  	    javanotify_on_media_notification(JAVACALL_EVENT_MEDIA_DURATION_UPDATED, mp->playerId, (void*)total_time);
                  	    MP3_DEBUG_STR1("javanotify_on_media_notification: Media time updated: %d\n", total_time);
                  	    MP3_DEBUG_STR4("padding: %d, bitrate:%d, samplerate:%d, version:%d\n", padding, bitrate_value,
	   mp3_samplerate, version);
                  	} else if (mp->totalTime != (unsigned int)-1 && ((total_time < mp->totalTime*99/100) || (total_time > mp->totalTime*101/100))) {
                  	    mp->totalTime = (unsigned int)-1;
                  	    javanotify_on_media_notification(JAVACALL_EVENT_MEDIA_DURATION_UPDATED, mp->playerId, (void*)-1);
                  	    MP3_DEBUG_STR1("javanotify_on_media_notification: Media time updated to TIME_UNKNOWN (%d)\n", total_time);
                  	    MP3_DEBUG_STR4("padding: %d, bitrate:%d, samplerate:%d, version:%d\n", padding, bitrate_value,
	   mp3_samplerate, version);
                  	}                  	
                  }
	  }

	  sceIoLseek32 (mp3_handle, mp->mp3_data_start, PSP_SEEK_SET);	//seek back
	  //javacall_printf ("Mp3thread: frame_size=%d, mp3_handle=%x\n",
	  //	  frame_size, mp3_handle);
	  if (sceIoRead (mp3_handle, mp3_data_buffer, frame_size) !=
	      frame_size)
	    {
	      MP3_DEBUG_STR("Mp3thread:No more frames. End of media.\n");
	      mp->isPlaying = 0;
	      continue;
	    }

	  mp->mp3_data_start += frame_size;
	  mp3_codec_buffer[6] = (unsigned long) mp3_data_buffer;
	  mp3_codec_buffer[8] = (unsigned long) mp3_mix_buffer;
	  mp3_codec_buffer[7] = mp3_codec_buffer[10] = frame_size;
	  mp3_codec_buffer[9] = mp3_sample_per_frame * 4;
	  int res = sceAudiocodecDecode (mp3_codec_buffer, 0x1002);
	  if (res < 0)
	    {
	      mp->mp3_data_start = SeekNextFrame (mp, mp3_handle);
	      if (mp->mp3_data_start < 0)
		{
		  MP3_DEBUG_STR("SeekNextFrame failed\n");
		  mp->isPlaying = 0;
		  continue;
		}
	      continue;
	    }

	  memcpy (mp3_output_buffer[mp3_output_index], mp3_mix_buffer,
		  mp3_sample_per_frame * 4);

	  sceAudioSRCOutputBlocking (PSP_AUDIO_VOLUME_MAX,
				     mp3_output_buffer[mp3_output_index]);

	  mp3_output_index = (mp3_output_index + 1) % 4;
	  samplesdecoded = mp3_sample_per_frame;
	}

 
        if (!mp->paused && mp->isOpen > 0) {
            javacall_print("MP3 stop notification sent.\n");
            javanotify_on_media_notification(JAVACALL_EVENT_MEDIA_END_OF_MEDIA, mp->playerId, 
            	                                                       (void*)GetTimeMp3(mp));
            mp->isPlaying = 0;
            sceIoLseek32 (mp3_handle, 0, PSP_SEEK_SET);
            mp->mp3_data_start = SeekNextFrame (mp, mp3_handle);
            mp->paused = 1;
        } else {
            if (audio_channel_set >= 0)
           {
              int res;
              res = sceAudioSRCChRelease ();
              MP3_DEBUG_STR1("sceAudioSRCChRelease return %x\n", res);
              if (res == 0) {
                  audio_channel_set = -1;
              }
           }
            sceKernelDelayThread(50000);
        }
    }

  if (mp3_data_buffer)
    {
      free (mp3_data_buffer);
    }



  if (mp3_handle >= 0)
    {
      sceIoClose (mp3_handle);
    }

  if (codec_init != -1 && codec_init != -2)
    {
      sceAudiocodecReleaseEDRAM (mp3_codec_buffer);
    }
  
  if (mp->isPlaying == -1) {
    javacall_print("MP3 stop notification sent. Failed to play\n");
    javanotify_on_media_notification(JAVACALL_EVENT_MEDIA_END_OF_MEDIA, mp->playerId, 
    	                                                     (void*)GetTimeMp3(mp));
  }

  mp->mp3thread = -1;

  MP3_DEBUG_STR("Mp3thread exiting...\n");

  sceKernelExitDeleteThread(1);

  return 1;
}

static int
SeekNextFrame (mp3_player_handle* mp, SceUID fd)
{
  int offset = 0;
  unsigned char* buf = frame_buf;
  unsigned char *pBuffer;
  int i;
  int size = 0;
  
#ifdef MP3_DEBUG_VERBOSE
  MP3_DEBUG_STR("SeekNextFrame>>\n");
#endif
  offset = sceIoLseek32 (fd, 0, PSP_SEEK_CUR);
  sceIoRead (fd, buf, sizeof (frame_buf));
  if (!strncmp ((char *) buf, "ID3", 3) || !strncmp ((char *) buf, "ea3", 3))	//skip past id3v2 header, which can cause a false sync to be found
    {
      MP3_DEBUG_STR("Got IDv3 tag\n");
      //get the real size from the syncsafe int
      size = buf[6];
      size = (size << 7) | buf[7];
      size = (size << 7) | buf[8];
      size = (size << 7) | buf[9];
      size += 10;
      if (buf[5] & 0x10)	//has footer
	size += 10;

      mp->id3HeaderSize = size;
      MP3_DEBUG_STR1("IDv3 size:%d\n", size);
    }

  sceIoLseek32 (fd, offset + size, PSP_SEEK_SET);	//now seek for a sync
  while (1)
    {
      offset = sceIoLseek32 (fd, 0, PSP_SEEK_CUR);
      size = sceIoRead (fd, buf, sizeof (frame_buf));
      if (size <= 6)		//at end of file
	return -1;
      if (!strncmp ((char *) buf, "EA3", 3))	//oma mp3 files have non-safe ints in the EA3 header
	{
	  unsigned int ea3_size = ((unsigned int)buf[4] << 8) + (unsigned int)buf[5];
	  sceIoLseek32 (fd, offset + ea3_size, PSP_SEEK_SET);
	  mp->ea3HeaderSize = ea3_size;
	  continue;
	}

      pBuffer = buf;
      for (i = 0; i < size; i++)
	{
	  //if this is a valid frame sync (0xe0 is for mpeg version 2.5,2+1)
	  if ((pBuffer[i] == 0xff) && ((pBuffer[i + 1] & 0xE0) == 0xE0))
	    {
	      offset += i;
	      sceIoLseek32 (fd, offset, PSP_SEEK_SET);
#ifdef MP3_DEBUG_VERBOSE
	      MP3_DEBUG_STR1("SeekNextFrame<< %d\n", offset);
#endif
	      return offset;
	    }
	}
      //go back two bytes to catch any syncs that on the boundary
      sceIoLseek32 (fd, -2, PSP_SEEK_CUR);
    }
}


void StopMp3(mp3_player_handle* mp)
{
   MP3_DEBUG_STR1("StopMp3... %x\n", mp);
   if (mp->isOpen > 0)    mp->isOpen = 0;
   if (mp->isPlaying > 0) mp->isPlaying = 0;
   if (mp->mp3thread >= 0) {
   	sceKernelWaitThreadEnd(mp->mp3thread, NULL);
   	mp->mp3thread = -1;
       //sceKernelDeleteThread(mp->mp3thread);
   }

   if (0x80268002== sceAudioSRCChRelease ()) { //Need retry
   	sceKernelDelayThread(100000);
   	sceAudioSRCChRelease();
   }
   
   if (mp == currentMp3) {
   	currentMp3 = NULL;
   }
   
   MP3_DEBUG_STR("StopMp3 return\n");
}

int
LoadMp3 (mp3_player_handle * mp)
{

  MP3_DEBUG_STR("LoadMp3>>\n");
  if (currentMp3 != NULL && currentMp3 != mp)
    {

      StopMp3 (currentMp3);
    }

  if (mp->mp3thread >= 0)
    {

      MP3_DEBUG_STR1("The mp3 player is already in playing: %d\n",
		       mp->mp3thread);
      return 0;
    }

  mp->mp3thread =
    sceKernelCreateThread ("Mp3thread", Mp3thread, 12, 16 * 1024,
			   PSP_THREAD_ATTR_USER, NULL);
  if (mp->mp3thread >= 0)
    {

      mp->isOpen = 1;
      mp->paused = 1;
      MP3_DEBUG_STR2("LoadMp3: mp=%p, filename=%s\n", mp,
		       &(mp->filename[0]));
      if (sceKernelStartThread (mp->mp3thread, 4, &mp) < 0)
	{

	  MP3_DEBUG_STR("LoadMp3: sceKernelStartThread error\n");
	  mp->isOpen = -1;
	  mp->isPlaying = -1;
	  return -1;
	}

    }
  else
    {

      MP3_DEBUG_STR("LoadMp3: sceKernelCreateThread error\n");
      mp->isOpen = -1;
      mp->isPlaying = -1;
      return -1;
    }

  currentMp3 = mp;
  return 0;
}

void
PlayMp3 (mp3_player_handle * mp)
{

  if (mp->isPlaying == 0)
    {

      mp->isPlaying = 1;
    }

  mp->paused = 0;
}

void
PauseMp3 (mp3_player_handle * mp)
{

  if (mp->isPlaying > 0)
    {

      mp->isPlaying = 0;
    }

}

long SeekMp3(mp3_player_handle* mp, long ms)
{
    printf("SeekMp3 to %dms\n", (int)ms);
    if (mp->totalTime == 0 || mp->totalTime == (unsigned int)-1) {
    	return -1;
    }
    if (ms < 0) ms = 0;
    if (ms > mp->totalTime) ms = mp->totalTime;
    mp->set2time = ms;
    return ms;
}

long GetTimeMp3(mp3_player_handle* mp) {
    int percentage;
    long value;
	
    if (mp->totalTime == 0 || mp->totalTime == (unsigned int)-1) {
    	return -1;
    }

    percentage = mp->mp3_data_start * 100 / (mp->mp3_file_size - mp->id3HeaderSize - mp->ea3HeaderSize);
    value = mp->totalTime * percentage / 100;
    //MP3_DEBUG_STR1("GetTimeMp3 return: %d\n", (int)value);

    return value;
}
	
void
FinalizeMp3 ()
{
  if (currentMp3)
    {

      StopMp3 (currentMp3);
    }

  sceMpegFinish ();
}
