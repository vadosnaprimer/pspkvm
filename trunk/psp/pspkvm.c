#include <pspkernel.h>
#include <pspdebug.h>
#include <pspctrl.h>
#include <pspthreadman.h>
#include <psprtc.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <dirent.h>
#include <time.h>
#include <sys/time.h>

#include <pspiofilemgr.h>
#include <pspiofilemgr_fcntl.h>
#include <psputility_sysparam.h>

#include <javacall_keypress.h>
#include <javacall_lifecycle.h>
#include <javacall_file.h>
#include <javacall_keymap.h>

/* Define the module info section */
PSP_MODULE_INFO("pspME", 0x1000, 1, 1);

/* Define the main thread's attribute value (optional) */
//PSP_MAIN_THREAD_ATTR(THREAD_ATTR_USER | THREAD_ATTR_VFPU);
PSP_MAIN_THREAD_ATTR(0);

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


#define PSP_CTRL_ANALOG_LEFT 	0x10000000
#define PSP_CTRL_ANALOG_RIGHT 	0x20000000
#define PSP_CTRL_ANALOG_UP 		0x40000000
#define PSP_CTRL_ANALOG_DOWN 	0x80000000

#if 0

u32 oldAnalogKeys = 0;
u32 oldDigitalKeys = 0;
void processKey(javacall_keypress_type type, int pspkey) {
	u32 comboKey;
	u32 allKeyDown = oldDigitalKeys|oldAnalogKeys|pspkey;
	javacall_key javacallkey;
	static int allKeyReleased = 1;

	if (allKeyDown == 0) {
		allKeyReleased = 1;
	}

	comboKey = PSP_CTRL_LTRIGGER|PSP_CTRL_RTRIGGER|PSP_CTRL_CROSS;
	if ((allKeyDown&comboKey)==comboKey && allKeyReleased) {
		javanotify_shutdown_current();
		pspkey &= ~comboKey;
		allKeyReleased = 0;
	}
	comboKey = PSP_CTRL_LTRIGGER|PSP_CTRL_RTRIGGER|PSP_CTRL_TRIANGLE;
	if ((allKeyDown&comboKey)==comboKey && allKeyReleased) {
		javanotify_switch_to_ams();
		pspkey &= ~comboKey;
		allKeyReleased = 0;
	}
	comboKey = PSP_CTRL_LTRIGGER|PSP_CTRL_SQUARE;
	if ((allKeyDown&comboKey)==comboKey && (pspkey&PSP_CTRL_SQUARE)) {
		javacallkey = JAVACALL_KEY_7;
	       javanotify_key_event(javacallkey, type);
		pspkey &= ~comboKey;
	}
	comboKey = PSP_CTRL_LTRIGGER|PSP_CTRL_TRIANGLE;
	if ((allKeyDown&comboKey)==comboKey && (pspkey&PSP_CTRL_TRIANGLE)) {
		javacallkey = JAVACALL_KEY_9;
		javanotify_key_event(javacallkey, type);
		pspkey &= ~comboKey;
	}
	comboKey = PSP_CTRL_LTRIGGER|PSP_CTRL_CIRCLE;
	if ((allKeyDown&comboKey)==comboKey && (pspkey&PSP_CTRL_CIRCLE)) {
		javacallkey = JAVACALL_KEY_SELECT;
		javanotify_key_event(javacallkey, type);
		pspkey &= ~comboKey;
	}	
	comboKey = PSP_CTRL_LTRIGGER|PSP_CTRL_CROSS;
	if ((allKeyDown&comboKey)==comboKey && (pspkey&PSP_CTRL_CROSS)) {
		javacallkey = JAVACALL_KEY_CLEAR;
		javanotify_key_event(javacallkey, type);
		pspkey &= ~comboKey;
	}	
	if (pspkey&PSP_CTRL_UP) {
		javacallkey = JAVACALL_KEY_2;
		javanotify_key_event(javacallkey, type);
	}
	if (pspkey&PSP_CTRL_RIGHT) {
		javacallkey = JAVACALL_KEY_6;
		javanotify_key_event(javacallkey, type);
	}
	if (pspkey&PSP_CTRL_DOWN) {
		javacallkey = JAVACALL_KEY_8;
		javanotify_key_event(javacallkey, type);
	}
	if (pspkey&PSP_CTRL_LEFT) {
		javacallkey = JAVACALL_KEY_4;
		javanotify_key_event(javacallkey, type);
	}
	if (pspkey&PSP_CTRL_CROSS) {
		javacallkey = JAVACALL_KEY_0;
		javanotify_key_event(javacallkey, type);
	}	
	if (pspkey&PSP_CTRL_CIRCLE) {
		javacallkey = JAVACALL_KEY_5;
		javanotify_key_event(javacallkey, type);
		//pspDebugScreenPrintf("pspkey pressed: PSP_CTRL_CIRCLE\n"); 
	}
	if (pspkey&PSP_CTRL_SQUARE) {
		javacallkey = JAVACALL_KEY_1;
		javanotify_key_event(javacallkey, type);
		//pspDebugScreenPrintf("pspkey pressed: PSP_CTRL_CIRCLE\n"); 
	}
	if (pspkey&PSP_CTRL_TRIANGLE) {
		javacallkey = JAVACALL_KEY_3;
		javanotify_key_event(javacallkey, type);
		//pspDebugScreenPrintf("pspkey pressed: PSP_CTRL_CIRCLE\n"); 
	}	
	if (pspkey&PSP_CTRL_LTRIGGER) {
		javacallkey = JAVACALL_KEY_ASTERISK;
		javanotify_key_event(javacallkey, type);
	}
	if (pspkey&PSP_CTRL_RTRIGGER) {
		javacallkey = JAVACALL_KEY_POUND;
		javanotify_key_event(javacallkey, type);
	}
	if (pspkey&PSP_CTRL_SELECT) {
		javacallkey = JAVACALL_KEY_SOFT1;
		javanotify_key_event(javacallkey, type);
	}
	if (pspkey&PSP_CTRL_START) {
		javacallkey = JAVACALL_KEY_SOFT2;
		javanotify_key_event(javacallkey, type);
	}
	if (pspkey&PSP_CTRL_ANALOG_LEFT) {
		javacallkey = JAVACALL_KEY_LEFT;
		javanotify_key_event(javacallkey, type);
	}
	if (pspkey&PSP_CTRL_ANALOG_RIGHT) {
		javacallkey = JAVACALL_KEY_RIGHT;
		javanotify_key_event(javacallkey, type);
	}
	if (pspkey&PSP_CTRL_ANALOG_UP) {
		javacallkey = JAVACALL_KEY_UP;
		javanotify_key_event(javacallkey, type);
	}
	if (pspkey&PSP_CTRL_ANALOG_DOWN) {
		javacallkey = JAVACALL_KEY_DOWN;
		javanotify_key_event(javacallkey, type);
	}	

}

#define analogThresholdValue 85
#define analogZeroValue 128
int analogLeft = analogZeroValue - analogThresholdValue;
int analogRight = analogZeroValue + analogThresholdValue;
int analogUp = analogZeroValue - analogThresholdValue;
int analogDown = analogZeroValue + analogThresholdValue;
unsigned char oldLx = analogZeroValue, oldLy = analogZeroValue;
static void processAnalogKey(unsigned char Lx, unsigned char Ly) {
	u32 newAnalogKeys = 0;
	u32 keyDown, keyUp;
	if (Lx < analogLeft){
		newAnalogKeys |= PSP_CTRL_ANALOG_LEFT;
	}
	if (Lx > analogRight){
		newAnalogKeys |= PSP_CTRL_ANALOG_RIGHT;
	}
	if (Ly < analogUp){
		newAnalogKeys |= PSP_CTRL_ANALOG_UP;
	}
	if (Ly > analogDown){
		newAnalogKeys |= PSP_CTRL_ANALOG_DOWN;
	}
	keyUp = oldAnalogKeys&(~newAnalogKeys);
	processKey(JAVACALL_KEYRELEASED, keyUp);
	keyDown = newAnalogKeys&(~oldAnalogKeys);
	processKey(JAVACALL_KEYPRESSED, keyDown);
	oldAnalogKeys = newAnalogKeys;
}

static void UpdateEvents() {
	u32 keyDown, keyUp;
	SceCtrlData ctrlPad;
	//sceKernelDelayThread(0);
       if (!sceCtrlReadBufferPositive(&ctrlPad, 1)){
         return;
       }
	//sceCtrlPeekBufferPositive(&ctrlPad, 1);
	keyUp = oldDigitalKeys&(~ctrlPad.Buttons);
	processKey(JAVACALL_KEYRELEASED, keyUp);
	keyDown = ctrlPad.Buttons&(~oldDigitalKeys);
	processKey(JAVACALL_KEYPRESSED, keyDown);
	oldDigitalKeys = ctrlPad.Buttons;
	processAnalogKey(ctrlPad.Lx, ctrlPad.Ly);
}

int KeyThread(SceSize args, void *argp)
{
    	sceCtrlSetSamplingCycle(10);
	sceCtrlSetSamplingMode(PSP_CTRL_MODE_ANALOG);
	while(!done){
		UpdateEvents();
	}
}

#else

#define MULTITASK_KEY (PSP_CTRL_LTRIGGER | PSP_CTRL_RTRIGGER | PSP_CTRL_TRIANGLE)
#define EXIT_KEY (PSP_CTRL_LTRIGGER | PSP_CTRL_RTRIGGER | PSP_CTRL_CROSS)
#define DEBUG_TRACE_KEY (PSP_CTRL_LTRIGGER | PSP_CTRL_RTRIGGER | PSP_CTRL_START)
#define SHIFT_KEY1 PSP_CTRL_LTRIGGER
#define SHIFT_KEY2 PSP_CTRL_RTRIGGER

#define REPEAT_THRESHOLD 20
#define REPEAT_THRESHOLD1 5

int KeyThread(SceSize args, void *argp)
{
	SceCtrlData pad;
	unsigned int pspKey = 0, lastPspKey = 0;
	int lx, ly;
	static int hold = 0;
	static int repeat_threshold = REPEAT_THRESHOLD;
	static javacall_key lastPressedJavakey = 0;
	
	sceCtrlSetSamplingCycle(25);
	sceCtrlSetSamplingMode(PSP_CTRL_MODE_ANALOG);
	while(!done){
		
		//pspDebugScreenSetXY(0, 2);

    		sceCtrlReadBufferPositive(&pad, 3);
		pspKey = 0;
		lx = pad.Lx;
		ly = pad.Ly;
		if (lx < 5) {
			if (ly < 55) {
				pspKey = PSP_CTRL_ANALOG_LEFT | PSP_CTRL_ANALOG_UP;
			} else if (ly > 200) {
				pspKey = PSP_CTRL_ANALOG_LEFT | PSP_CTRL_ANALOG_DOWN;
			} else if (lx < 5) {
				pspKey = PSP_CTRL_ANALOG_LEFT;
			}
		} else if (lx > 251) {
			if (ly < 55) {
				pspKey = PSP_CTRL_ANALOG_RIGHT | PSP_CTRL_ANALOG_UP;
			} else if (ly > 200) {
				pspKey = PSP_CTRL_ANALOG_RIGHT | PSP_CTRL_ANALOG_DOWN;
			} else if (lx > 251) {
				pspKey = PSP_CTRL_ANALOG_RIGHT;
			}
		} else {
			if (ly < 5) {
				pspKey = PSP_CTRL_ANALOG_UP;
			} else if (ly > 251) {
				pspKey = PSP_CTRL_ANALOG_DOWN;
			} else {
				//Staying in center, nothing happen
			}
		}
		

              pspKey |= pad.Buttons;
              
		if (pspKey != lastPspKey || hold == repeat_threshold){
		    int i;
		    int id;
		    int keys;
		    int shift, lastShift; // non-zero if "shift" key is pressed
		    javacall_keymap* keymap;

                  if ((pspKey == lastPspKey) && lastPressedJavakey != 0) {
                  	//repeat
                  	javanotify_key_event(lastPressedJavakey, JAVACALL_KEYREPEATED);
                  	hold = 0;
                  	repeat_threshold = REPEAT_THRESHOLD1;
                  	continue;
                  } else {
                     lastPressedJavakey = 0;
		       hold = 0;
		       repeat_threshold = REPEAT_THRESHOLD;
                  }

                  if (lastPspKey == DEBUG_TRACE_KEY) {
                  	if (pspKey == 0) {
		    		lastPspKey = 0;
		    	}
		    	continue;
                  }

		    if (lastPspKey == EXIT_KEY) {
		    	if (pspKey == 0) {
		    		lastPspKey = 0;
		    	}
		    	continue;	    	
		    }
		    
		    if (lastPspKey == MULTITASK_KEY) {
		    	if (pspKey == 0) {
		    		lastPspKey = 0;
		    	}
		    	continue;	    	
		    }

		    if (pspKey == DEBUG_TRACE_KEY) {
		    	javanotify_key_event(JAVACALL_KEY_DEBUG_TRACE, JAVACALL_KEYPRESSED);
		    	lastPspKey = pspKey;
		    	continue;
		    }

		    if (pspKey == EXIT_KEY) {
		    	javanotify_shutdown_current();
		    	lastPspKey = pspKey;
		    	continue;
		    }		    

		    if (pspKey == MULTITASK_KEY) {
		    	javanotify_switch_to_ams();
		    	lastPspKey = pspKey;
		    	continue;
		    }

		    keymap = javacall_keymap_current();
		    keys = javacall_keymap_size();
		    shift = pspKey & (SHIFT_KEY1 | SHIFT_KEY2);
		    lastShift = lastPspKey & (SHIFT_KEY1 | SHIFT_KEY2);

		    for (i = 0 ; i < keys; i++) {
		    	//printf("keymap[i].nativekey:%08x, javakey:%d\n", keymap[i].nativeKey, keymap[i].javaKey);
		    	if (keymap[i].javaKey == JAVACALL_KEY_INVALID) {
		    		continue;
		    	}
		    	int pressed = ((keymap[i].nativeKey & pspKey) && (keymap[i].shift?shift:!shift));
		    	int lastPressed = ((keymap[i].nativeKey& lastPspKey) && (keymap[i].shift?lastShift:!lastShift));
		    	if (pressed!= lastPressed) {
			    	if (pressed) {			    		
			    		javanotify_key_event(keymap[i].javaKey, JAVACALL_KEYPRESSED);
			    		lastPressedJavakey = keymap[i].javaKey;
				    //printf("key %d pressed\n", keymap[i].javaKey);
				} else {
				    	javanotify_key_event(keymap[i].javaKey, JAVACALL_KEYRELEASED);
				    	lastPressedJavakey = 0;
				   	 //printf("key %d released\n", keymap[i].javaKey); 	
				}
			}
		    }
		    
		} else {
		    hold++;
		}
		
		lastPspKey = pspKey;
	}
	return 0;
}
#endif

int java_main(void)
{
	SceUID id;

	//pspDebugScreenInit();
	
	SetupCallbacks();

	id = sceKernelSetAlarm(1000000, alarm_handler, (void*)0);
	if (id < 0) {
		printf("sceKernelSetAlarm error!\n");
		return -1;
	}

	printf("timezone:%s\n", javacall_time_get_local_timezone());
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
	return 0;
}

int main(void)
{
	SceUID thid;
	printf("Loading network modules\n");
    	if(pspSdkLoadInetModules() < 0)	{	
    		printf("Error, could not load inet modules\n");	
    		sceKernelExitGame();	
    		return -1;
    	}

    	printf("Network module loaded\n");
    	
	/* create user thread */
	thid = sceKernelCreateThread("Java Thread", java_main,
											0x2f, // default priority
											256 * 1024, // stack size (256KB is regular default)
											PSP_THREAD_ATTR_USER, NULL); //# user mode

	// start user thread, then wait for it to do everything else
	sceKernelStartThread(thid, 0, 0);
	sceKernelWaitThreadEnd(thid, NULL);

	/* quick clean exit */
	sceKernelExitGame();

	return 0;

}

