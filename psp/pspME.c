#include <pspkernel.h>
#include <pspdebug.h>
#include <pspctrl.h>
#include <pspthreadman.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <dirent.h>

#include <pspiofilemgr.h>
#include <pspiofilemgr_fcntl.h>

#include <javacall_keypress.h>
#include <javacall_lifecycle.h>
#include <javacall_file.h>

/* Define the module info section */
PSP_MODULE_INFO("pspME", 0, 1, 1);

/* Define the main thread's attribute value (optional) */
//PSP_MAIN_THREAD_ATTR(THREAD_ATTR_USER | THREAD_ATTR_VFPU);

/* Define printf, just to make typing easier */
//#define printf	pspDebugScreenPrintf
//#define printf(x)

int done = 0;

/* Exit callback */
int exit_callback(int arg1, int arg2, void *common)
{
	done = 1;
	javanotify_shutdown();

	return 0;
}

/* Callback thread */
int CallbackThread(SceSize args, void *argp)
{
	int cbid;

	cbid = sceKernelCreateCallback("Exit Callback", exit_callback, NULL);
	sceKernelRegisterExitCallback(cbid);
	sceKernelSleepThreadCB();

	return 0;
}

/* Sets up the callback thread and returns its thread id */
int SetupCallbacks(void)
{
	int thid = 0;

	thid = sceKernelCreateThread("update_thread", CallbackThread,
				     0x11, 0xFA0, 0, 0);
	if(thid >= 0)
	{
		sceKernelStartThread(thid, 0, 0);
	}

	return thid;
}

SceUInt alarm_handler(void *common)
{
	//printf("javanotify_start...\n");
	javanotify_start();
	return 0;
}


#define MULTITASK_KEY (PSP_CTRL_LTRIGGER | PSP_CTRL_RTRIGGER)

typedef struct {
	unsigned int pspKey;
	int javaKey;
} tKeymap;
tKeymap keymap[] = {
	{MULTITASK_KEY,  -999 /*dummy*/},
	{PSP_CTRL_SELECT, JAVACALL_KEY_ASTERISK},
	{PSP_CTRL_START, JAVACALL_KEY_POUND},
	{PSP_CTRL_UP , JAVACALL_KEY_UP},
	{PSP_CTRL_RIGHT , JAVACALL_KEY_RIGHT},
	{PSP_CTRL_DOWN , JAVACALL_KEY_DOWN},
	{PSP_CTRL_LEFT , JAVACALL_KEY_LEFT},
	{PSP_CTRL_LTRIGGER, JAVACALL_KEY_SOFT1},
	{PSP_CTRL_RTRIGGER , JAVACALL_KEY_SOFT2},
	{PSP_CTRL_TRIANGLE, JAVACALL_KEY_CLEAR},
	{PSP_CTRL_CIRCLE , JAVACALL_KEY_SELECT},
	{PSP_CTRL_CROSS, JAVACALL_KEY_5},
	{PSP_CTRL_SQUARE, JAVACALL_KEY_0}
};

int KeyThread(SceSize args, void *argp)
{
	SceCtrlData pad;
	unsigned int lastButtons = 0;
	javacall_key numKey = 0, lastNumKey = 0;
	int lx, ly;
	int keys = sizeof(keymap)/sizeof(tKeymap);
	sceCtrlSetSamplingCycle(0);
	sceCtrlSetSamplingMode(PSP_CTRL_MODE_ANALOG);
	while(!done){
		
		//pspDebugScreenSetXY(0, 2);

    		sceCtrlReadBufferPositive(&pad, 3);
		numKey = 0;
		lx = pad.Lx;
		ly = pad.Ly;
		if (lx < 85) {
			if (ly < 85) {
				numKey = JAVACALL_KEY_1;
			} else if (ly > 170) {
				numKey = JAVACALL_KEY_7;
			} else if (lx < 5) {
				numKey = JAVACALL_KEY_4;
			}
		} else if (lx > 170) {
			if (ly < 85) {
				numKey = JAVACALL_KEY_3;
			} else if (ly > 170) {
				numKey = JAVACALL_KEY_9;
			} else if (lx > 251) {
				numKey = JAVACALL_KEY_6;
			}
		} else {
			if (ly < 5) {
				numKey = JAVACALL_KEY_2;
			} else if (ly > 251) {
				numKey = JAVACALL_KEY_8;
			} else {
				//Staying in center, nothing happen
			}
		}
		
		if (numKey != lastNumKey) {
			//printf("numKey:%d\n", numKey);
			if (lastNumKey != 0) {
				javanotify_key_event(lastNumKey, JAVACALL_KEYRELEASED);
			} else {
				javanotify_key_event(numKey, JAVACALL_KEYPRESSED);
			}
			lastNumKey = numKey;
		}

		if (pad.Buttons != lastButtons){
		    int i;
		    if (lastButtons == MULTITASK_KEY) {
		    	if (pad.Buttons == 0) {
		    		lastButtons = 0;
		    	}
		    	continue;	    	
		    }
		    
		    for (i = 0 ; i < keys; i++) {
			if ((pad.Buttons & keymap[i].pspKey)!=
			    (lastButtons & keymap[i].pspKey)){
			    	if ((pad.Buttons & keymap[i].pspKey) == keymap[i].pspKey) {
			    		if (keymap[i].pspKey != MULTITASK_KEY){
			    			javanotify_key_event(keymap[i].javaKey, JAVACALL_KEYPRESSED);
			    		} else {
			    			javanotify_switch_to_ams();
			    			pad.Buttons = MULTITASK_KEY;
			    			break;
			    		}
				    //printf("key %d pressed\n", keymap[i].javaKey);
				} else {
					if (keymap[i].pspKey != MULTITASK_KEY) {
				    		javanotify_key_event(keymap[i].javaKey, JAVACALL_KEYRELEASED);
				    		// printf("key %d released\n", keymap[i].javaKey);
				    	}
				}
			}
		    }
		    lastButtons = pad.Buttons;
		}
	}
	return 0;
}

int main(void)
{
	SceUID id;

	//pspDebugScreenInit();
	
	SetupCallbacks();
	
	id = sceKernelSetAlarm(1000000, alarm_handler, (void*)0);
	if (id < 0) {
		printf("sceKernelSetAlarm error!\n");
	}
	
	printf("Wait..\n");
	int thid = sceKernelCreateThread("key_thread", KeyThread,
				     0x11, 0xFA0, 0, 0);
	if(thid >= 0)
	{		
		sceKernelStartThread(thid, 0, 0);
		JavaTask();
		done = 1;
		sceKernelWaitThreadEndCB(thid, NULL);
	}
	
	javacall_media_finalize();
	sceKernelExitGame();	
	return 0;
}
