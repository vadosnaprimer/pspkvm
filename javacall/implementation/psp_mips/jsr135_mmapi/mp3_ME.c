/*
 * PSP Software Development Kit - http://www.pspdev.org
 * -----------------------------------------------------------------------
 * Licensed under the BSD license, see LICENSE in PSPSDK root for details.
 *
 * main.c - Sample to demonstrate proper use of the mp3 library
 *
 * Copyright (c) 2008 Alexander Berl <raphael@fx-world.org>
 *
 * $Id: $
 * 
 * Modified by M@x. http://www.pspkvm.org
 */
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <malloc.h>

#include <pspkernel.h>
#include <pspdebug.h>
#include <pspctrl.h>
#include <pspdisplay.h>
#include <pspaudio.h>
#include <pspmp3.h>
#include <psputility.h>
#include <pspiofilemgr.h>

#include "id3.h"

#include "psp_mmapi.h"
#include "javacall_logging.h"

static char mp3Buf[32*1024]  __attribute__((aligned(64)));
static short pcmBuf[32*(1152/2)]  __attribute__((aligned(64)));

static mp3_player_handle* currentMp3 = NULL;

int fillStreamBuffer( int fd, int handle ){	
	char* dst = NULL;	
	int write = 0;	
	int pos = -1;	

	// Get Info on the stream (where to fill to, how much to fill, where to fill from)	
	int status = sceMp3GetInfoToAddStreamData( handle, &dst, &write, &pos);	
	if (status<0)	{		
		javacall_printf("ERROR: sceMp3GetInfoToAddStreamData returned 0x%08X\n", status);
		return -1;
	}
	
	if (pos < 0 || write == 0) {
		javacall_printf("fillStreamBuffer: end of media1\n");
		return 0;
	}
	
	// Seek file to position requested	
	status = sceIoLseek32( fd, pos, SEEK_SET );	
	if (status<0)	{		
		javacall_printf("ERROR: sceIoLseek32 returned 0x%08X\n", status);
		return -2;
	}

	// Read the amount of data	
	int read = sceIoRead( fd, dst, write );	
	if (read < 0)	{		
		javacall_printf("ERROR: Could not read from file - 0x%08X\n", read);
		return -3;
	}		
	if (read==0)	{
		// End of file?		
		javacall_printf("fillStreamBuffer: end of media\n");
		return 0;	
	}		

	// Notify mp3 library about how much we really wrote to the stream buffer	
	status = sceMp3NotifyAddStreamData( handle, read );	
	if (status<0)	{		
		javacall_printf("ERROR: sceMp3NotifyAddStreamData returned 0x%08X\n", status);	
		return -4;
	}

	return write;
}

int decodeThread2(SceSize args, void *argp){
   
   int fd = 0, handle = -1;
   mp3_player_handle* mp = *(mp3_player_handle**)argp;
   int data_starthm, endpos, numChannels, samplingRate;
   short* buf;
   SceMp3InitArg mp3Init;
   SceInt64 res;
   int channel = -1, volume;
   int bytesDecoded = -1, lastDecoded = -1;
   int retries = 0;

   javacall_printf("[mp3_ME.c]decodeThread2: File to open %s\n", &mp->filename[0]);

   data_starthm = ID3v2TagSize(&(mp->filename[0])) + 10;
   printf("returned from ID3v2TagSize:%d\n", data_starthm);
   fd = sceIoOpen(&mp->filename[0], PSP_O_RDONLY, 0777 );

   if (fd <= 0) {
   	javacall_printf("[mp3_ME.c]decodeThread2: File open failed %s\n", &mp->filename[0]);
   	mp->isOpen = -1;
   	mp->isPlaying = -1;
   	goto fail;
   }
   
   //sceIoChangeAsyncPriority(fd, 0x10);

  
   sceIoLseek32(fd, 0, PSP_SEEK_SET);
   //sceIoWaitAsync(fd, &res);
   endpos = sceIoLseek32( fd, 0, PSP_SEEK_END );
   //sceIoWaitAsync(fd, &res);

   printf("mp3 file open ok\n");
   //endpos = res;

   // Reserve a mp3 handle for our playback
   mp3Init.mp3StreamStart = data_starthm;
   mp3Init.mp3StreamEnd = endpos;
   mp3Init.unk1 = 0;
   mp3Init.unk2 = 0;
   mp3Init.mp3Buf = mp3Buf;
   mp3Init.mp3BufSize = sizeof(mp3Buf);
   mp3Init.pcmBuf = pcmBuf;
   mp3Init.pcmBufSize = sizeof(pcmBuf);

   handle = sceMp3ReserveMp3Handle( &mp3Init );
   printf("sceMp3ReserveMp3Handle return: %d\n", handle);
   if (handle < 0) {
   	mp->isPlaying = 0;
   	mp->isOpen = -1;
   	goto fail;
   }
   
   // Fill the stream buffer with some data so that sceMp3Init has something to work with
   if (fillStreamBuffer( fd, handle ) <= 0) {
   	printf("failed when fillStreamBuffer\n");
   	mp->isPlaying = 0;
   	mp->isOpen = -1;
   	goto fail;   	
   }
   
   printf("sceMp3Init...\n");
   sceMp3Init( handle );
   printf("sceMp3Init ok\n");
   

   

   //kbit = sceMp3GetBitRate(handle);
   //samplingRate = sceMp3GetSamplingRate(handle);
   //lastDecoded = 0;
   //volume = PSP_AUDIO_VOLUME_MAX;
   //numPlayed = 0;
   //loop = 0;
   volume = PSP_AUDIO_VOLUME_MAX;
   sceMp3SetLoopNum(handle, 0);
   printf("sceMp3SetLoopNum ok\n");

   while(mp->isOpen > 0)
   {

      
      while (mp->isPlaying > 0)
      {
         
                // Check if we need to fill our stream buffer
      if (sceMp3CheckStreamDataNeeded( handle )>0)
      {
          if (fillStreamBuffer( fd, handle ) <= 0) {
          	mp->isPlaying = 0;
          	break;
          }
      }
      
      
         
          // We retry in case it's just that we reached the end of the stream and need to loop
         for (;retries<1;retries++)
         {
            //printf("sceMp3Decode...\n");
            bytesDecoded = sceMp3Decode( handle, &buf );
            //printf("bytesDecoded=0x%x\n", bytesDecoded);
            if (bytesDecoded>0){
               break;
            }
         
            if (sceMp3CheckStreamDataNeeded( handle )<=0)
               break;
         
            if (fillStreamBuffer( fd, handle ) <= 0)
            {
               mp->isPlaying = 0;
               break;
            }
         }

         
         // Reserve the Audio channel for our output if not yet done
         if (channel<0 || bytesDecoded != lastDecoded)
         {
            if (channel>=0) {
               sceAudioSRCChRelease();
            }

            if (bytesDecoded > 0) {
               //printf("sceMp3GetSamplingRate\n");
               samplingRate = sceMp3GetSamplingRate(handle);
               //printf("sceMp3GetSamplingRate return %d\n", samplingRate);
               if (samplingRate < 0) {
               	    mp->isPlaying = 0;
               	    printf("sceMp3GetSamplingRate return %d\n", samplingRate);
               	    javacall_printf("Invalid mp3 file?\n");
               	    break;
               }
      
               numChannels = sceMp3GetMp3ChannelNum( handle );
               //printf("sceMp3GetMp3ChannelNum:%d\n", numChannels);
               if (numChannels <= 0) {
         	      mp->isPlaying = 0;
         	      printf("sceMp3GetMp3ChannelNum:%d\n", numChannels);
         	      javacall_printf("Invalid mp3 file?\n");
         	      break;
               }
               channel = sceAudioSRCChReserve( bytesDecoded/(2*numChannels), samplingRate, numChannels );
            }
            lastDecoded = bytesDecoded;
         }

         if (bytesDecoded > 0) {
             // Output the decoded samples and accumulate the number of played samples to get the playtime
             sceAudioSRCOutputBlocking( volume, buf );

             mp->numPlayed += bytesDecoded*1000/samplingRate;
             
         } else {
             printf("sceMp3Decode:0x%x\n", bytesDecoded);
             mp->isPlaying = 0;
         }
         
      }
      
      if (channel>=0) {
      	   printf("end of mp3\n");
          sceAudioSRCChRelease();
          channel = -1;
      }

      if (!mp->paused) {
      	   //sceMp3ResetPlayPosition(handle);
      	   //mp->numPlayed = 0;
      	   mp->isOpen = 0;
      	   mp->paused = 1;
      	   javanotify_on_media_notification(JAVACALL_EVENT_MEDIA_END_OF_MEDIA, mp->playerId, mp->numPlayed);
      }

      //printf("sceKernelDelayThread...\n");
      sceKernelDelayThread(10000);
      //printf("sceKernelDelayThread ok\n");
   }


fail:
   if (handle >= 0) {
   	   sceMp3ReleaseMp3Handle( handle );
   }
   
   if (fd > 0) {
       sceIoClose( fd );
   }

   if (!mp->paused) {
       javanotify_on_media_notification(JAVACALL_EVENT_MEDIA_END_OF_MEDIA, mp->playerId, mp->numPlayed);
   }
   mp->mp3thread = -1;
   printf("mp3 Thread end\n");
   sceKernelExitDeleteThread(0);

   return 0;
}
/* main routine */

void StopMp3(mp3_player_handle* mp)
{
   javacall_printf("StopMp3... %x\n", mp);
   if (mp->isOpen > 0)    mp->isOpen = 0;
   if (mp->isPlaying > 0) mp->isPlaying = 0;
   if (mp->mp3thread >= 0) {
   	sceKernelWaitThreadEnd(mp->mp3thread, NULL);
   	mp->mp3thread = -1;
       //sceKernelDeleteThread(mp->mp3thread);
   }

   if (mp == currentMp3) {
   	currentMp3 = NULL;
   }
   
   javacall_printf("StopMp3 return\n");
}

int LoadMp3(mp3_player_handle* mp)
{
   printf("LoadMp3>>\n");
   if (currentMp3 != NULL && currentMp3 != mp) {
   	StopMp3(currentMp3);
   }

   if (mp->mp3thread >= 0) {
   	javacall_printf("The mp3 player is already in playing: %d\n", mp->mp3thread);
   	return 0;
   }
   
   mp->mp3thread = sceKernelCreateThread("decodeThread2", decodeThread2, 12, 16*1024, PSP_THREAD_ATTR_USER, NULL);
   if (mp->mp3thread >= 0) {
   	mp->isOpen = 1;
   	mp->paused = 1;
   	javacall_printf("LoadMp3: mp=%p, filename=%s\n", mp, &(mp->filename[0]));
       if (sceKernelStartThread(mp->mp3thread, 4, &mp) < 0) {
       	javacall_printf("LoadMp3: sceKernelStartThread error\n");
           mp->isOpen = -1;
           mp->isPlaying = -1;
           return -1;
       }
   } else {
       javacall_printf("LoadMp3: sceKernelCreateThread error\n");
       mp->isOpen = -1;
       mp->isPlaying = -1;
       return -1;
   }

   currentMp3 = mp;

   return 0;
}
void PlayMp3(mp3_player_handle* mp)
{
   if (mp->isPlaying == 0) {
       mp->isPlaying = 1;
   }

   mp->paused = 0;
}
void PauseMp3(mp3_player_handle* mp)
{
    if (mp->isPlaying > 0) {
       mp->isPlaying = 0;
    }
}

void FinalizeMp3() {
    if (currentMp3) {
    	StopMp3(currentMp3);
    }
    
    sceMp3TermResource();
}

/*
static void getMP3TagInfo(char *filename, struct fileInfo *targetInfo){

    //ID3:

    struct ID3Tag ID3;

    ID3 = ParseID3(filename);

    strcpy(targetInfo->title, ID3.ID3Title);

    strcpy(targetInfo->artist, ID3.ID3Artist);

    strcpy(targetInfo->album, ID3.ID3Album);

    strcpy(targetInfo->year, ID3.ID3Year);

    strcpy(targetInfo->genre, ID3.ID3GenreText);

    strcpy(targetInfo->trackNumber, ID3.ID3TrackText);

    targetInfo->encapsulatedPictureType = ID3.ID3EncapsulatedPictureType;

    targetInfo->encapsulatedPictureOffset = ID3.ID3EncapsulatedPictureOffset;

    targetInfo->encapsulatedPictureLength = ID3.ID3EncapsulatedPictureLength;

}
struct fileInfo Taginfo2(char *filename){

    struct fileInfo tempInfo;

    initFileInfo(&tempInfo);
    getMP3TagInfo(filename, &tempInfo);

    return tempInfo;

}

struct fileInfo Taginfo(char *filename){

    struct fileInfo tempInfo;

    initFileInfo(&tempInfo);
    getMP3TagInfo(filename, &tempInfo);
    tempInfo.kbit = kbit;
    tempInfo.hz = samplingRate;
   if(numChannels < 2){
      sprintf(tempInfo.mode,"mono");
   }else{
      sprintf(tempInfo.mode,"stereo");
   }
   int secs = (size*8) / (kbit*1024);
   int h = secs / 3600;

   int m = (secs - h * 3600) / 60;

   int s = secs - h * 3600 - m * 60;

   snprintf(tempInfo.strLength, sizeof(tempInfo.strLength), "%2.2i:%2.2i:%2.2i", h, m, s);

   return tempInfo;

}*/
