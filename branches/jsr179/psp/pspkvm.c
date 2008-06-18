#include <pspkernel.h>
#include <pspdebug.h>
#include <pspctrl.h>
#include <pspthreadman.h>
#include <psprtc.h>
#include <psputility.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <dirent.h>
#include <time.h>
#include <sys/time.h>

#include <pspiofilemgr.h>
#include <pspiofilemgr_fcntl.h>
#include <psputility_sysparam.h>

#include <pspnet.h>
#include <pspnet_inet.h>
#include <pspnet_apctl.h>
#include <pspnet_resolver.h>
#include <psputility.h>
#include <psputility_netparam.h>
#include <pspgu.h>

#if _PSP_FW_VERSION >= 200
#include <pspusb.h>
#include "pspusbgps.h"

#define ENABLE_GPS 1
#else
#define ENABLE_GPS 0
#endif

#include <javacall_keypress.h>
#include <javacall_lifecycle.h>
#include <javacall_file.h>
#include <javacall_keymap.h>

/* Define the module info section */
#if _PSP_FW_VERSION >= 200
PSP_MODULE_INFO("pspkvm", 0, 1, 1);
PSP_MAIN_THREAD_ATTR(THREAD_ATTR_USER);
#else
PSP_MODULE_INFO("pspkvm", 0x1000, 1, 1);
PSP_MAIN_THREAD_ATTR(0);
#endif

PSP_HEAP_SIZE_KB(7000);

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

#define log_buf_size 65536
static char logs[log_buf_size]={0};
static int log_cur = 0;
void print_log(const char* str) {
	char* p= &logs[log_cur];
	strncpy(p, str, log_buf_size - log_cur);
	log_cur += strlen(p);
	if (log_cur >= log_buf_size) log_cur = 0;
	pspDebugScreenPrintf(str);
}

static void display_log(int show) {
	if (show) {
		int i;
		//sceGuDisplay(0);
		pspDebugScreenPrintf(logs);
		
	} else {
		//sceGuDisplay(1);
	}
}

static int suspend_key_input = 0;

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
#define NETWORK_KEY (PSP_CTRL_LTRIGGER | PSP_CTRL_RTRIGGER | PSP_CTRL_SQUARE)
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
		if (suspend_key_input) {
			continue;
		}
		
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
		    int rotate;
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

		    if (lastPspKey == NETWORK_KEY) {
		    	if (pspKey == 0) {
		    		lastPspKey = 0;
		    	}
		    	continue;	    	
		    }

		    if (pspKey == NETWORK_KEY) {
		    	javanotify_network_connect();
		    	lastPspKey = pspKey;
		    	continue;
		    }

		    if (pspKey == DEBUG_TRACE_KEY) {
		    	javanotify_key_event(JAVACALL_KEY_DEBUG_TRACE, JAVACALL_KEYPRESSED);
		    	//display_log(1);
		    	
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
		    rotate = javacall_devemu_get_rotation(javacall_devemu_get_current_device());

		    for (i = 0 ; i < keys; i++) {
		    	//printf("keymap[i].nativekey:%08x, javakey:%d\n", keymap[i].nativeKey, keymap[i].javaKey);
		    	if (keymap[i].javaKey == JAVACALL_KEY_INVALID) {
		    		continue;
		    	}

		    	int javaKey = keymap[i].javaKey;
		    	if (rotate == 90) {
		    		switch (javaKey) {
		    			case JAVACALL_KEY_UP:
		    				javaKey = JAVACALL_KEY_LEFT;
		    				break;
		    			case JAVACALL_KEY_DOWN:
		    				javaKey = JAVACALL_KEY_RIGHT;
		    				break;
		    			case JAVACALL_KEY_LEFT:
		    				javaKey = JAVACALL_KEY_DOWN;
		    				break;
		    			case JAVACALL_KEY_RIGHT:
		    				javaKey = JAVACALL_KEY_UP;
		    				break;
		    			case JAVACALL_KEY_2:
		    				javaKey = JAVACALL_KEY_4;
		    				break;
		    			case JAVACALL_KEY_8:
		    				javaKey = JAVACALL_KEY_6;
		    				break;
		    			case JAVACALL_KEY_4:
		    				javaKey = JAVACALL_KEY_8;
		    				break;
		    			case JAVACALL_KEY_6:
		    				javaKey = JAVACALL_KEY_2;		    				
		    		}
		    	}
		    	int pressed = ((keymap[i].nativeKey & pspKey) && (keymap[i].shift?shift:!shift));
		    	int lastPressed = ((keymap[i].nativeKey& lastPspKey) && (keymap[i].shift?lastShift:!lastShift));
		    	if (pressed!= lastPressed) {
			    	if (pressed) {			    		
			    		javanotify_key_event(javaKey, JAVACALL_KEYPRESSED);
			    		lastPressedJavakey = javaKey;
				    //printf("key %d pressed\n", keymap[i].javaKey);
				} else {
				    	javanotify_key_event(javaKey, JAVACALL_KEYRELEASED);
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

int netDialog(int status)
{
	int state;

#if _PSP_FW_VERSION >= 200

	int running = 1;
	
#ifdef DEBUG_JAVACALL_NETWORK
    	javacall_printf("netDialog: enter %d\n", status);
#endif

       if (status == 0) {
       	sceNetApctlGetState(&state);
       	if (state > 0) {
       		status = 1;
       	}
       }
	
   	pspUtilityNetconfData data;

	memset(&data, 0, sizeof(data));
	data.base.size = sizeof(data);
	data.base.language = PSP_SYSTEMPARAM_LANGUAGE_ENGLISH;
	data.base.buttonSwap = PSP_UTILITY_ACCEPT_CIRCLE;
	data.base.graphicsThread = 17;
	data.base.accessThread = 19;
	data.base.fontThread = 18;
	data.base.soundThread = 16;
	data.action = status?PSP_NETCONF_ACTION_DISPLAYSTATUS:PSP_NETCONF_ACTION_CONNECTAP;

	suspend_key_input = 1;

	sceUtilityNetconfInitStart(&data);

	while(running)
	{
	      //sceGuDisplay(0);
		switch(sceUtilityNetconfGetStatus())
		{
			case PSP_UTILITY_DIALOG_NONE:
				running = 0;
				break;

			case PSP_UTILITY_DIALOG_VISIBLE:
				javacall_lcd_enable_flush(0);
	      			javacall_lcd_flush();
				sceUtilityNetconfUpdate(1);				
				sceDisplayWaitVblankStart();
				sceGuSwapBuffers();
				break;
			case PSP_UTILITY_DIALOG_FINISHED:
				javacall_lcd_enable_flush(1);				
				break;

			case PSP_UTILITY_DIALOG_QUIT:
				sceUtilityNetconfShutdownStart();				
				break;

			default:
				break;
		}
	}
	javacall_lcd_enable_flush(1);
#endif /*_PSP_FW_VERSION >= 200*/

       sceKernelDelayThread(500000); 

	sceNetApctlGetState(&state);
#ifdef DEBUG_JAVACALL_NETWORK
    	javacall_printf("netDialog: return %d\n", state);
#endif
	sceKernelDelayThread(500000); 

	suspend_key_input = 0;
	
	return state==4?1:0;
}


int usbgps_enabled = 0;

#if ENABLE_GPS
#if 0
static u32 usbState = 0, gpsState = -1;


static void updateDisplay (gpsdata* data, satdata* sat)
{
 static u32 currentY = 0;

 u32 x;


 // Wait vblank
 sceDisplayWaitVblankStart();

 // Set current Y
 pspDebugScreenSetXY(0,currentY);

 // Show texts
 printf("\n\nUSB State = 0x%X %s\n",usbState,(usbState & PSP_USB_ACTIVATED) ? "[activated]    " : "[deactivated]");
 printf("GPS State = 0x%X\n",gpsState);
 printf("Satellite(s) count = %d\n\n",sat->satellites_in_view);

 printf("Data :\n------\n");
 
 printf("Date: %d/%d/%d %d:%d:%d\n", data->year, data->month, data->date,
 								data->hour, data->minute, data->second);
 printf("HDOP: %f\tSpeed: %f\n", data->hdop, data->speed);
 printf("latitude:%f, longtitude:%f, altitude:%f\n", data->latitude, data->longitude, data->altitude);
 
 printf("\n\n[CROSS to activate/deactivate USB device | TRIANGLE to quit]\n");
}

static int gpsmngr (SceSize args, void *argp) {
  printf("Initialize_gps...\n");

  sceUsbActivate(PSP_USBGPS_PID);
  for (;;) {
 
    // Get state of the USB
    usbState = sceUsbGetState();
 
     // Get state of GPS
     if (usbState & PSP_USB_ACTIVATED)
      sceUsbGpsGetState(&gpsState);
     else
      gpsState = -1;
   
     // Get data of GPS
     if (gpsState == 0x3) {
     	//sceUsbGpsGetData(&gpsd,&satd);
     	//printf("latitude:%f, longtitude:%f, altitude:%f\n", gpsd.latitude, gpsd.longitude, gpsd.altitude);
     	usbgps_status = JAVACALL_GPS_STATUS_INITIALIZED;
       break;
     }
   
     
    //updateDisplay(&gpsd,&satd);
    //sceKernelDelayThread(1000000);
    
  }

  printf("GPS Initialize OK\n");
  sceKernelExitDeleteThread(0);

  return 0;
}
#endif
static int moduleLoadStart (const char *path)
{
 SceUID loadResult, startResult;
 int status;


 loadResult = sceKernelLoadModule(path,0,NULL);
 if (loadResult & 0x80000000) return loadResult;

 startResult = sceKernelStartModule(loadResult,0,NULL,&status,NULL);
 if (loadResult != startResult) return startResult;

 return 0;
}

static int startUSBGPSDrivers() {
	int res = 0;
	
	if (res = sceUsbStart(PSP_USBBUS_DRIVERNAME,0,0)) {
		printf(PSP_USBBUS_DRIVERNAME" start failed\n");
		return res;
	}

	if (res = sceUsbStart("USBAccBaseDriver",0,0)) {
		printf("USBAccBaseDriver start failed\n");
		return res;
	}

	if (res = sceUsbStart(PSP_USBGPS_DRIVERNAME,0,0)) {
		printf(PSP_USBGPS_DRIVERNAME" start failed\n");
		return res;
	}

	return res;
}
#endif //ENABLE_GPS


int java_main(void)
{
	SceUID id;

	//pspDebugScreenInit();
	
	SetupCallbacks();
#if 1
//#if ENABLE_GPS
#if 0
	SceUID thid_gpsmngr = sceKernelCreateThread("gps_manager_thread",gpsmngr, 0x40, 10000, 0, 0);
	if(thid_gpsmngr >= 0) {                
		sceKernelStartThread(thid_gpsmngr, 0, 0);        
	}
#endif

	id = sceKernelSetAlarm(1000000, alarm_handler, (void*)0);
	if (id < 0) {
		printf("sceKernelSetAlarm error!\n");
		return -1;
	}

	printf("timezone:%s\n", javacall_time_get_local_timezone());
	int thid = sceKernelCreateThread("key_thread", KeyThread,
				     0x11, 0xFA0, PSP_THREAD_ATTR_USER, 0);
	if(thid >= 0)
	{
	       printf("Wait..\n");
	
		sceKernelStartThread(thid, 0, 0);
		JavaTask();
		done = 1;
		sceKernelWaitThreadEndCB(thid, NULL);
	} else {
		printf("KeyThread create error. exit.\n");
	}
	
	javacall_media_finalize();
#else 
 SceCtrlData pad;
 u32 oldButtons = 0;

 // Must be local ?
 gpsdata gpsd;
 satdata  satd;
 
for (;;)
 {
  // Get state of GPS
  if (usbState & PSP_USB_ACTIVATED)
   sceUsbGpsGetState(&gpsState);
  else
   gpsState = -1;

  // Get data of GPS
  if (gpsState == 0x3) sceUsbGpsGetData(&gpsd,&satd);

  // Read the buttons
  sceCtrlReadBufferPositive(&pad,1);

  if (oldButtons != pad.Buttons)
  {
   // Save the buttons
   oldButtons = pad.Buttons;

   // Exit if TRIANGLE
   if (pad.Buttons & PSP_CTRL_TRIANGLE) break;

   // Start / Stop GPS if CROSS
   if (pad.Buttons & PSP_CTRL_CROSS)
   {
    if (usbState & PSP_USB_ACTIVATED)
	 sceUsbDeactivate(PSP_USBGPS_PID);
    else
 	 sceUsbActivate(PSP_USBGPS_PID);

    sceUsbWaitCancel();

    // Get state of the USB
    usbState = sceUsbGetState();
   }
  }

  // Update display
  updateDisplay(&gpsd,&satd);
 }

 //sceUsbGpsClose();

 // USB Deactivate
 //sceUsbDeactivate(PSP_USBGPS_PID);
 //sceUsbWaitCancel();
 printf("PSP_CTRL_TRIANGLE\n");
 sceKernelExitGame();
#endif
	return 0;
}

static unsigned int __attribute__((aligned(16))) _list[262144];
unsigned int * _gu_list;
void* _gu_fb;

#define BUF_WIDTH (512)
#define SCR_WIDTH (480)
#define SCR_HEIGHT (272)

static void setup_gu() {

	void* fbp0 = getStaticVramBuffer(BUF_WIDTH,SCR_HEIGHT,GU_PSM_8888);
	void* fbp1 = getStaticVramBuffer(BUF_WIDTH,SCR_HEIGHT,GU_PSM_8888);
	void* zbp = getStaticVramBuffer(BUF_WIDTH,SCR_HEIGHT,GU_PSM_4444);

	sceGuInit();
	_gu_list = _list;
	sceGuStart(GU_DIRECT,_gu_list);
	sceGuDrawBuffer(GU_PSM_8888,fbp0,BUF_WIDTH);
	sceGuDispBuffer(SCR_WIDTH,SCR_HEIGHT,fbp1,BUF_WIDTH);
	sceGuDepthBuffer(zbp,BUF_WIDTH);
	sceGuOffset(2048 - (SCR_WIDTH/2),2048 - (SCR_HEIGHT/2));
	sceGuViewport(2048,2048,SCR_WIDTH,SCR_HEIGHT);
	//sceGuDepthRange(65535,0);
	sceGuDepthRange(0xc350,0x2710);    	
	sceGuScissor(0,0,SCR_WIDTH,SCR_HEIGHT);
	sceGuEnable(GU_SCISSOR_TEST);
	sceGuFrontFace(GU_CW);
	sceGuEnable(GU_TEXTURE_2D);

	sceGuDepthFunc(GU_GEQUAL);
    	sceGuEnable(GU_DEPTH_TEST);
    	
       sceGuEnable(GU_CULL_FACE);
    	sceGuEnable(GU_CLIP_PLANES);
    	
	
	sceGuClear(GU_COLOR_BUFFER_BIT|GU_DEPTH_BUFFER_BIT);
	sceGuFinish();
	sceGuSync(0,0);
	_gu_fb = fbp0;

	sceDisplayWaitVblankStart();
	
	sceKernelDcacheWritebackAll();
}

int main(void)
{
	SceUID thid;
	int res = 0;
	int ret_val = 0;
	pspDebugScreenInit();

       printf("Setup GU\n");
	setup_gu();       



	printf("Loading network modules\n");
#if _PSP_FW_VERSION >= 200
       if ((ret_val = sceUtilityLoadNetModule(PSP_NET_MODULE_COMMON)) < 0) {
    		printf("Error, could not load inet modules %d\n", PSP_NET_MODULE_COMMON);    		
    		goto fail;
    	}

	if ((ret_val = sceUtilityLoadNetModule(PSP_NET_MODULE_INET)) < 0) {
		printf("Error, could not load inet modules %d\n", PSP_NET_MODULE_INET);
		goto fail;
	}

#if ENABLE_GPS
       usbgps_enabled = 0;

	if (res = moduleLoadStart("usbacc.prx")) {
		printf("Error, could not load usbacc.prx: %x\n", res);
	}

	if (res == 0 && (res = moduleLoadStart("usbgps.prx"))) {
		printf("Error, could not load usbgps.prx: %x\n", res);		
	}

	if (res == 0 && (res = startUSBGPSDrivers())) {
		printf("Error, could not start USB GPS drivers: %x\n", res);
	} else if (res == 0) {
		if (res = sceUsbGpsOpen()) {
			printf("Error, sceUsbGpsOpen: %x\n",  res);
		} else {
			printf("USB GPS enabled\n");
			usbgps_enabled = 1;
		}
	} else {
		printf("Module loading failed. Can not use GPS device!\n");
	}
#endif //ENABLE_GPS

#else	
    	if(ret_val = pspSdkLoadInetModules() < 0)	{	
    		printf("Error, could not load inet modules\n");
    		goto fail;
    	}
#endif
	

    	printf("Network module loaded\n");

	ret_val = 0;
    	
	/* create user thread */
	thid = sceKernelCreateThread("Java Thread", java_main,
											0x2f, // default priority
											512 * 1024, // stack size
											PSP_THREAD_ATTR_USER | PSP_THREAD_ATTR_VFPU, NULL); //# user mode

	// start user thread, then wait for it to do everything else
	sceKernelStartThread(thid, 0, 0);
	printf("sceKernelWaitThreadEnd...\n");
	sceKernelWaitThreadEnd(thid, NULL);
	printf("sceKernelWaitThreadEnd OK\n");

fail:
       sceGuTerm();
       printf("sceKernelExitGame...\n");
	/* quick clean exit */
	sceKernelExitGame();
	printf("sceKernelExitGame OK\n");

	return ret_val;

}

/**
 * System stubs for workaround linking order problems
 **/
u32 _sceUsbGetState() {
  return sceUsbGetState();
}

void _sceUsbActivate(u32 id) {
  return sceUsbActivate(id);
}

void _sceUsbDeactivate(u32 id) {
  return sceUsbDeactivate(id);
}

int mk_dir(const char* dir, SceMode mode)
{
    	return sceIoMkdir(dir, mode);
}

int rm_dir(const char* dir)
{
    	return sceIoRmdir(dir);
}

