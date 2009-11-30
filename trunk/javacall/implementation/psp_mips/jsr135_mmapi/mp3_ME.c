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

static int mp3_output_index = 0, audio_output_index = 0;
static mp3_player_handle* currentMp3 = NULL;
static SceUID _mp3_audio_put_sema, _mp3_audio_get_sema;
static SceUID mp3_audio_thread = -1;
static int mp3_audio_th_exit = 0;

static int SeekNextFrame (mp3_player_handle* mp, SceUID fd);

static int readfrombuffer(mp3_player_handle* mp, SceUID fd, char* target, int number) {
	if (mp->frame_buffer_size == 0) {
		int pos = sceIoLseek32(fd, 0, PSP_SEEK_CUR);
		int b = sceIoRead(fd, mp->frame_buffer, sizeof(mp->frame_buffer));
		if (b >= number) {
			memcpy(target, mp->frame_buffer, number);
			mp->frame_buffer_size = b;
			mp->frame_buffer_start = pos;
			mp->frame_buffer_pos = pos + number;
			return number;
		} else {
			if (b > 0) {
				memcpy(target, mp->frame_buffer, b);
			}
			return b;
		}
	} else {
		if (mp->frame_buffer_pos+ number > mp->frame_buffer_start + mp->frame_buffer_size) {
			//not in buffer
			sceIoLseek32(fd, mp->frame_buffer_pos, PSP_SEEK_SET);
			mp->frame_buffer_size = 0;
			return readfrombuffer(mp, fd, target, number);
		} else {
			//in buffer
			memcpy(target, mp->frame_buffer+mp->frame_buffer_pos-mp->frame_buffer_start, number);
			mp->frame_buffer_pos += number;
			return number;
		}
	}
}

static int seekinbuffer (mp3_player_handle* mp, SceUID fd, int offset, int whence) {
	int target;
	if (mp->frame_buffer_size == 0) {
		return sceIoLseek32(fd, offset, whence);
	} else {
		switch (whence) {
			case PSP_SEEK_SET:
				target = offset;
				break;
			case PSP_SEEK_CUR:
				target = mp->frame_buffer_pos + offset;
				break;
			default:
				target = mp->mp3_file_size + offset;
		}
		if (target >= mp->frame_buffer_start && target <= mp->frame_buffer_start + mp->frame_buffer_size) {
			//seek in buffer
			mp->frame_buffer_pos = target;
			return target;
		} else {
			//out of buffer
			mp->frame_buffer_size = 0;
			return sceIoLseek32(fd, target, PSP_SEEK_SET);
		}
	}
}

static SceUID processMp3Header(mp3_player_handle* mp) {
  int fileok = 0;
  SceUID mp3_handle = sceIoOpen (&(mp->filename[0]), PSP_O_RDONLY, 0777);
  
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
      MP3_DEBUG_STR1("file size=%d\n", mp->mp3_file_size);
      fileok = 1;
    }

  if (fileok)
    {
      mp->mp3_data_start = SeekNextFrame (mp, mp3_handle);
      if (mp->mp3_data_start < 0)
	{
	  MP3_DEBUG_STR("Mp3_load:SeekNextFrame return fail\n");
	  mp->isOpen = -1;
	  mp->isPlaying = 0;
	}
    }

  return mp3_handle;
}

static int processMp3FrameHeader(char* mp3_header_buf, int* mp3_samplerate, int* mp3_sample_per_frame, int* frame_size, int* mp3_numChannel, int* mp3_version, int* mp3_bitrate_value) {
	  int samplerate;
	  int mp3_header = mp3_header_buf[0];
	  mp3_header = mp3_header & 0xff;
	  mp3_header = (mp3_header << 8) | (((int)mp3_header_buf[1]) & 0xff);
	  mp3_header = (mp3_header << 8) | (((int)mp3_header_buf[2]) & 0xff);
	  mp3_header = (mp3_header << 8) | (((int)mp3_header_buf[3]) & 0xff);
	  
	  int bitrate_value;
	  int bitrate = (mp3_header & 0xf000) >> 12;
	  int padding = (mp3_header & 0x200) >> 9;
	  int version = (mp3_header & 0x180000) >> 19;
	  int numChannel = ((mp3_header & 0xC0) >> 6)==3?1:2;
	  samplerate = samplerates[version][(mp3_header & 0xC00) >> 10];

#ifdef MP3_DEBUG_VERBOSE
	  MP3_DEBUG_STR4("numChannel: %d, bitrate:%d, samplerate:%d, version:%d\n", numChannel, bitrate,
	   samplerate, version);
#endif

	  if ((bitrate > 14) || (version == 1) || (samplerate <= 0) || (bitrate <= 0))	//invalid frame, look for the next one
	    {
	      return -1;
	    }

	  if ( version == 3) //mpeg 1
	    {
	      *mp3_sample_per_frame = 1152;
	      bitrate_value = bitrates[bitrate];
	      *frame_size =
		144000 * bitrate_value / samplerate + padding;
	    }
	  else
	    {
	      *mp3_sample_per_frame = 576;
	      bitrate_value = bitrates_v2[bitrate];
	      *frame_size =
	         72000 * bitrate_value / samplerate + padding;
	    } 

	  *mp3_samplerate = samplerate;
         if (mp3_version) *mp3_version = version;
         if (mp3_numChannel) *mp3_numChannel = numChannel;
         if (mp3_bitrate_value) *mp3_bitrate_value = bitrate_value;

	  return 0;
}

static VBR* 
parseVBR(mp3_player_handle* mp, SceUID mp3_handle, int off, int mp3_sample_per_frame, int mp3_samplerate) {
    char tmp[4];
    int frames, bytes;
    VBR* v;
    int pos = mp->frame_buffer_pos;
    MP3_DEBUG_STR2("parseVBR>>:pos=%d, off=%d\n",pos, off);
    seekinbuffer(mp, mp3_handle, pos + off, PSP_SEEK_SET);
    readfrombuffer(mp, mp3_handle, tmp, 4);
    if ((tmp[0] == 'X' && tmp[1] == 'i' && tmp[2] == 'n' && tmp[3] == 'g') || 
    	 (tmp[0] == 'I' && tmp[1] == 'n' && tmp[2] == 'f' && tmp[3] == 'o')) {
    	 v = malloc(sizeof(VBR));
    	 if (v == NULL) {
    	 	return NULL;
    	 }
        readfrombuffer(mp, mp3_handle, tmp, 4);
        if (tmp[3] & 1) {
        	readfrombuffer(mp, mp3_handle, &frames, 4);
        	frames = ((frames & 0x000000ff) << 24) |
        		       ((frames & 0x0000ff00) << 8) |
        		       ((frames & 0x00ff0000) >> 8) |
        		       ((frames & 0xff000000) >> 24);
        	MP3_DEBUG_STR1("parseVBR:frames=%d\n", frames);
        	v->total_time = frames * mp3_sample_per_frame / mp3_samplerate * 1000;
        } else {
              v->total_time = -1;
        }

        if (tmp[3] & 2) {
        	readfrombuffer(mp, mp3_handle, &bytes, 4);
        	bytes = ((bytes & 0x000000ff) << 24) |
        		       ((bytes & 0x0000ff00) << 8) |
        		       ((bytes & 0x00ff0000) >> 8) |
        		       ((bytes & 0xff000000) >> 24);
        	MP3_DEBUG_STR1("parseVBR:bytes=%d\n", bytes);
        }

        if (tmp[3] & 4) {
        	readfrombuffer(mp, mp3_handle, &(v->toc[0]), 100);
        }
        MP3_DEBUG_STR1("return VBR:%d\n", v->total_time);

    } else {
        MP3_DEBUG_STR("VBR is NULL\n");
        v = NULL;
    }

    seekinbuffer(mp, mp3_handle, pos, PSP_SEEK_SET);

    return v;
}

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
Mp3Audiothread(SceSize args, void *argp)
{
    while(!mp3_audio_th_exit) {
    	 if (mp3_output_index == audio_output_index) {
    	 	sceKernelWaitSemaCB(_mp3_audio_get_sema, 1, NULL);
    	 	continue;
    	 }
        sceAudioSRCOutputBlocking (PSP_AUDIO_VOLUME_MAX,
				     mp3_output_buffer[audio_output_index]);
        audio_output_index = (audio_output_index + 1) % 4;
        sceKernelSignalSema(_mp3_audio_put_sema, 1);
    }

    MP3_DEBUG_STR("Mp3Audiothread: exit\n");

    sceKernelExitDeleteThread(1);
    return 1;
}

int
Mp3thread (SceSize args, void *argp)
{
  SceUID mp3_handle = -1;
  int mp3_samplerate, mp3_sample_per_frame, frame_size;
  int samplesdecoded = 0;
  int last_frame_size = 0;
  char *mp3_data_buffer = NULL;
  int codec_init;
  int audio_channel_set = -1;
  
  mp3_player_handle *mp = *(mp3_player_handle **) argp;

  if ((codec_init = Init_codec_buffer ()) != 0)
    {
      mp->isOpen = -1;
      mp->isPlaying = 0;
    }

  mp3_handle = processMp3Header(mp);

  while (mp->isOpen > 0)
    {

      while (mp->isPlaying > 0)
	{
	  unsigned char mp3_header_buf[4];

	  if (mp->set2time >= 0 && mp->totalTime > 0) {
             int offset;
             MP3_DEBUG_STR2("set2time:%d / %d\n", mp->set2time, mp->totalTime);
             float percentage;
             percentage = (float)mp->set2time  / (float)mp->totalTime;
             if (mp->vbr) {
             	   int per = (int)(percentage*100);
             	   if (per >= 100) per = 99;
                 percentage = (float)mp->vbr->toc[per]/256.0f;
             }
             offset = (mp->mp3_file_size - mp->id3HeaderSize - mp->ea3HeaderSize) * percentage;
             offset += mp->id3HeaderSize + mp->ea3HeaderSize;
             seekinbuffer(mp, mp3_handle, offset, PSP_SEEK_SET);
             mp->mp3_data_start = SeekNextFrame (mp, mp3_handle);
             mp->set2time = -1;
        }
	  
	  memset (mp3_mix_buffer, 0, sizeof (mp3_mix_buffer));

	  if (readfrombuffer (mp, mp3_handle, mp3_header_buf, 4) != 4)
	    {
	      MP3_DEBUG_STR("Mp3thread:End of media\n");
	      mp->isPlaying = 0;
	      continue;
	    }

	  if (processMp3FrameHeader(mp3_header_buf, &mp3_samplerate, 
	  	                                          &mp3_sample_per_frame, &frame_size, NULL, NULL, NULL) < 0) //invalid frame, look for the next one
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

	  
	  if (audio_channel_set < 0)
	    {
	      int retry = 3;
	      while (retry > 0 && (audio_channel_set= sceAudioSRCChReserve (mp3_sample_per_frame, mp3_samplerate, 2)) < 0) {
	      	   retry --;
	      	   sceKernelDelayThread(50000);
	      	   continue;
	      }

	      if (audio_channel_set >= 0) {
      	      	  MP3_DEBUG_STR3("sceAudioSRCChReserve OK: %x. mp3_sample_per_frame=%d, mp3_samplerate=%d\n",
      	         	audio_channel_set, mp3_sample_per_frame, mp3_samplerate);
      	      } else {
      	         MP3_DEBUG_STR3("sceAudioSRCChReserve failed: %x. mp3_sample_per_frame=%d, mp3_samplerate=%d\n",
      	         	audio_channel_set, mp3_sample_per_frame, mp3_samplerate);
      	      }

	    }

	  if (frame_size != last_frame_size) {

       	  if (mp3_data_buffer)
       	    {
       	      free (mp3_data_buffer);
       	    }
       
       	  if (NULL == (mp3_data_buffer = (char *) memalign (64, frame_size)))
       	    {
       	      mp->isPlaying = 0;
       	      MP3_DEBUG_STR("Mp3thread: Memory not enough\n");
       	      last_frame_size = 0;
       	      continue;
       	    }
	  	      
                  last_frame_size = frame_size;
                  /*
                  if (total_time != mp->totalTime) {
                  	if (mp->totalTime == (unsigned int)-1) {
                  	    mp->totalTime = total_time;
                  	    javanotify_on_media_notification(JAVACALL_EVENT_MEDIA_DURATION_UPDATED, mp->playerId, (void*)total_time);
                  	    MP3_DEBUG_STR1("javanotify_on_media_notification: Media time updated: %d\n", total_time);
                  	    MP3_DEBUG_STR4("padding: %d, bitrate:%d, samplerate:%d, version:%d\n", padding, bitrate_value,
	   mp3_samplerate, version);
                  	}
                  	else if (mp->totalTime != (unsigned int)-1 && total_time == (unsigned int)-1) {
                  	    mp->totalTime = (unsigned int)-1;
                  	    javanotify_on_media_notification(JAVACALL_EVENT_MEDIA_DURATION_UPDATED, mp->playerId, (void*)-1);
                  	    MP3_DEBUG_STR1("javanotify_on_media_notification: Media time updated to TIME_UNKNOWN (%d)\n", total_time);
                  	    MP3_DEBUG_STR4("padding: %d, bitrate:%d, samplerate:%d, version:%d\n", padding, bitrate_value,
	   mp3_samplerate, version);
                  	}
                  }*/
	  }

	  seekinbuffer (mp, mp3_handle, mp->mp3_data_start, PSP_SEEK_SET);	//seek back
	  //javacall_printf ("Mp3thread: frame_size=%d, mp3_handle=%x, mp->mp3_data_start=%d\n",
	  //	  frame_size, mp3_handle, mp->mp3_data_start);
	  if (readfrombuffer (mp, mp3_handle, mp3_data_buffer, frame_size) !=
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

	  while (mp3_output_index == audio_output_index -1 || 
	  	(mp3_output_index == 3 && audio_output_index == 0)) {
	  	sceKernelWaitSemaCB(_mp3_audio_put_sema, 1, NULL);
	  }

	  memcpy (mp3_output_buffer[mp3_output_index], mp3_mix_buffer,
		  mp3_sample_per_frame * 4);

	  //sceAudioSRCOutputBlocking (PSP_AUDIO_VOLUME_MAX,
	  //			     mp3_output_buffer[mp3_output_index]);

	  mp3_output_index = (mp3_output_index + 1) % 4;

	  sceKernelSignalSema(_mp3_audio_get_sema, 1);
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

int GetInfoMp3(mp3_player_handle* mp) {
      int off;
      unsigned char mp3_header_buf[4];
      int mp3_samplerate, mp3_sample_per_frame, frame_size, version, numChannel, bitrate_value;
      SceUID mp3_handle = processMp3Header(mp);

      if (!mp3_handle) {
      	  javacall_printf("GetInfoMp3:File error\n");
      	  return -1;
      }

      
      if (readfrombuffer (mp, mp3_handle, mp3_header_buf, 4) != 4) {
	  javacall_printf("GetInfoMp3:End of media\n");
	  sceIoClose (mp3_handle);
	  return -2;
      }

      if (processMp3FrameHeader(mp3_header_buf, &mp3_samplerate, 
	  	                                          &mp3_sample_per_frame, &frame_size, 
	  	                                          &numChannel, &version, &bitrate_value) < 0) {
	  javacall_printf("GetInfoMp3:First frame is invalid\n");
	  sceIoClose (mp3_handle);
	  return -3;
      }

      if (version == 3) {
      	   off = numChannel==1?17:32;
      } else {
          off = numChannel==1?9:17;
      }
      
      VBR* vbr = parseVBR(mp, mp3_handle, off, mp3_sample_per_frame, mp3_samplerate);
      if (vbr) {
          mp->totalTime = vbr->total_time;
          free(vbr);
      } else {
          mp->totalTime = (mp->mp3_file_size - mp->id3HeaderSize - mp->ea3HeaderSize); //Data size
          mp->totalTime = mp->totalTime/(bitrate_value>>3);
      }

      MP3_DEBUG_STR1("GetInfoMp3:mp_totalTime=%d\n", mp->totalTime);

      sceIoClose (mp3_handle);
      mp->frame_buffer_size = 0;

      return 0;
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

  if (mp->frame_buffer_size > 0) {
  	MP3_DEBUG_STR1("SeekNextFrame:mp->frame_buffer_pos=%d\n", mp->frame_buffer_pos);
       offset = sceIoLseek32 (fd, mp->frame_buffer_pos, PSP_SEEK_SET);
  } else {
  	MP3_DEBUG_STR("SeekNextFrame:No frame buffer\n");
       offset = sceIoLseek32 (fd, 0, PSP_SEEK_CUR);
  }
  
  mp->frame_buffer_size = 0; //discard frame buffer

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
   sceKernelSignalSema(_mp3_audio_get_sema, 1);
   if (mp->mp3thread >= 0) {
   	sceKernelWaitThreadEnd(mp->mp3thread, NULL);
   	mp->mp3thread = -1;
       //sceKernelDeleteThread(mp->mp3thread);
   }

   if (0x80268002== sceAudioSRCChRelease ()) { //Need retry
   	sceKernelDelayThread(100000);
   	sceAudioSRCChRelease();
   }

   if (mp->vbr) {
   	free(mp->vbr);
   	mp->vbr = NULL;
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
    MP3_DEBUG_STR1("SeekMp3 want to seek to %dms\n", (int)ms);
    if (mp->totalTime == 0 || mp->totalTime == (unsigned int)-1) {
    	return -1;
    }
    if (ms < 0) ms = 0;
    if (ms > mp->totalTime) ms = mp->totalTime;
    if (mp->vbr) {
    	float percentage;
    	int per;
       percentage = (float)ms  / (float)mp->totalTime;
       per = (int)(percentage*100);
       if (per >= 100) per = 99;
       percentage = (float)per/100.0f;
       ms = (int)((float)mp->totalTime * percentage);
    }
    mp->set2time = ms;
    MP3_DEBUG_STR1("SeekMp3 actually seek to %dms\n", (int)ms);
    return ms;
}

long GetTimeMp3(mp3_player_handle* mp) {
    float percentage, pos;
    long value;
	
    if (mp->totalTime == 0 || mp->totalTime == (unsigned int)-1) {
    	return -1;
    }

    if (mp->set2time >= 0) {
    	MP3_DEBUG_STR1("GetTimeMp3 return (set2time): %d\n", (int)mp->set2time);
    	return mp->set2time;
    }

    if (mp->frame_buffer_size == 0) {
    	pos = (float)mp->mp3_data_start;
    } else {
       pos = (float)mp->frame_buffer_pos;
    }

    percentage = pos  / (float)(mp->mp3_file_size - mp->id3HeaderSize - mp->ea3HeaderSize);
    value = mp->totalTime * percentage;
    MP3_DEBUG_STR1("GetTimeMp3 return: %d\n", (int)value);
    
    return value;
}

long GetDurationMp3(mp3_player_handle* mp) {
	//MP3_DEBUG_STR1("GetDurationMp3 return: %d\n", mp->totalTime);
	if (mp->totalTime == (unsigned int)-1) {
		return (long)-1;
	}
	return (long)mp->totalTime;
}

void InitializeMp3() {
	if (mp3_audio_thread < 0) {
         sceMpegInit();
         _mp3_audio_put_sema = sceKernelCreateSema("Mp3AudioPutMutex", 0, 1, 1, 0);
         _mp3_audio_get_sema = sceKernelCreateSema("Mp3AudioGetMutex", 0, 1, 1, 0);
	  mp3_audio_th_exit = 0;
	  mp3_audio_thread = sceKernelCreateThread ("Mp3Audiothread", Mp3Audiothread, 11, 16 * 1024,
			   PSP_THREAD_ATTR_USER, NULL);
	  sceKernelStartThread(mp3_audio_thread, 0, NULL);
       }
}
	
void
FinalizeMp3 ()
{
   if (currentMp3)
    {
      StopMp3 (currentMp3);
    }

   if (mp3_audio_thread >= 0) {   	
   	mp3_audio_th_exit = 1;
   	sceKernelSignalSema(_mp3_audio_get_sema, 1);
   	sceKernelWaitThreadEnd(mp3_audio_thread, NULL);
   	mp3_audio_thread = -1;
   }

   sceKernelDeleteSema(_mp3_audio_get_sema);
   sceKernelDeleteSema(_mp3_audio_put_sema);

   sceMpegFinish ();
}
