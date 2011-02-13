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
#include <psppower.h>

#if _PSP_FW_VERSION >= 200
#include <pspusb.h>
#include "pspusbgps.h"

#define ENABLE_GPS 1
#else
#define ENABLE_GPS 0
#endif

#include <javacall_lifecycle.h>
#include <javacall_file.h>
#include <javacall_properties.h>
#include <javacall_logging.h>

/* Define the module info section */
#if _PSP_FW_VERSION >= 200
PSP_MODULE_INFO("pspkvm", 0, 1, 1);
PSP_MAIN_THREAD_ATTR(THREAD_ATTR_USER);
#else
PSP_MODULE_INFO("pspkvm", 0x1000, 1, 1);
PSP_MAIN_THREAD_ATTR(0);
#endif

PSP_HEAP_SIZE_KB(-3048);

/* Define the main thread's attribute value (optional) */
//PSP_MAIN_THREAD_ATTR(THREAD_ATTR_USER | THREAD_ATTR_VFPU);

#define DEBUG_SCREEN_WIDTH 60
static char BLANK_DEBUG_LINE[DEBUG_SCREEN_WIDTH+1];

static void test_mem(const char* msg) {
	int ii=64*1024*1024;
	char* tmpp=NULL;
	       javacall_printf("Wait..\n");
	       javacall_printf("Sys memory free:%d(total), %d(max)\n", sceKernelTotalFreeMemSize(), sceKernelMaxFreeMemSize());
		while(ii-=1024*1024) {
			if ((tmpp=malloc(ii))!=NULL) {
				
				free(tmpp);
				break;
			}
		}
		javacall_printf("[%s]MAX HEAP:%d\n",msg, ii);
}

/* Exit callback */
int exit_callback(int arg1, int arg2, void *common)
{
	javanotify_shutdown();

	return 0;
}

// Forward declare for the power callback
void invalidate_ftc_manager();

/* Power callback */
static int powerCallback(int unknown, int powerInfo, void* common) {
	if (powerInfo & PSP_POWER_CB_RESUMING) {
		// We just invalidate it. This forces
		// everything to reload once we need it.
		invalidate_ftc_manager(); }
	return 0; }


/* Callback thread */
int CallbackThread(SceSize args, void *argp)
{
	int cbid;

	// Exit callback
	cbid = sceKernelCreateCallback("Exit Callback", exit_callback, NULL);
	sceKernelRegisterExitCallback(cbid);
	// Power callback.
	const SceUID powerCallbackID = sceKernelCreateCallback("powerCallback", powerCallback, NULL);
	scePowerRegisterCallback(0, powerCallbackID);

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

static SceUInt alarm_handler(void *common)
{
	//javacall_printf("javanotify_start...\n");
	javanotify_start();
	return 0;
}

#if 0
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
#endif

int mp3codec_enabled = 1;

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
 javacall_printf("\n\nUSB State = 0x%X %s\n",usbState,(usbState & PSP_USB_ACTIVATED) ? "[activated]    " : "[deactivated]");
 javacall_printf("GPS State = 0x%X\n",gpsState);
 javacall_printf("Satellite(s) count = %d\n\n",sat->satellites_in_view);

 javacall_printf("Data :\n------\n");
 
 javacall_printf("Date: %d/%d/%d %d:%d:%d\n", data->year, data->month, data->date,
 								data->hour, data->minute, data->second);
 javacall_printf("HDOP: %f\tSpeed: %f\n", data->hdop, data->speed);
 javacall_printf("latitude:%f, longtitude:%f, altitude:%f\n", data->latitude, data->longitude, data->altitude);
 
 javacall_printf("\n\n[CROSS to activate/deactivate USB device | TRIANGLE to quit]\n");
}

static int gpsmngr (SceSize args, void *argp) {
  javacall_printf("Initialize_gps...\n");

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
     	//javacall_printf("latitude:%f, longtitude:%f, altitude:%f\n", gpsd.latitude, gpsd.longitude, gpsd.altitude);
     	usbgps_status = JAVACALL_GPS_STATUS_INITIALIZED;
       break;
     }
   
     
    //updateDisplay(&gpsd,&satd);
    //sceKernelDelayThread(1000000);
    
  }

  javacall_printf("GPS Initialize OK\n");
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
		javacall_printf(PSP_USBBUS_DRIVERNAME" start failed\n");
		return res;
	}

	if (res = sceUsbStart("USBAccBaseDriver",0,0)) {
		javacall_printf("USBAccBaseDriver start failed\n");
		return res;
	}

	if (res = sceUsbStart(PSP_USBGPS_DRIVERNAME,0,0)) {
		javacall_printf(PSP_USBGPS_DRIVERNAME" start failed\n");
		return res;
	}

	return res;
}
#endif //ENABLE_GPS


int java_main(void)
{
	SceUID id;

	SetupCallbacks();
#if 1
//#if ENABLE_GPS
#if 0
	SceUID thid_gpsmngr = sceKernelCreateThread("gps_manager_thread",gpsmngr, 0x40, 10000, 0, 0);
	if(thid_gpsmngr >= 0) {                
		sceKernelStartThread(thid_gpsmngr, 0, 0);        
	}
#endif

	id = sceKernelSetAlarm(3000000, alarm_handler, (void*)0);
	if (id < 0) {
		javacall_printf("sceKernelSetAlarm error!\n");
		return -1;
	}

	javacall_printf("timezone:%s\n", javacall_time_get_local_timezone());
		test_mem("JavaTask");

		
	JavaTask();
	
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
 javacall_printf("PSP_CTRL_TRIANGLE\n");
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

int suspend_key_input = 0;

// Declared extern. TODO: Use proper header
extern int vmsettings_key_equals(const char* k, const char* cmp);

int netDialog(int status)
{
	int state;
	int buttonSwap = PSP_UTILITY_ACCEPT_CIRCLE;
	char* res;
	if (vmsettings_key_equals("com.pspkvm.default_keymap", "western")) {
		buttonSwap = PSP_UTILITY_ACCEPT_CROSS; }

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
	data.base.buttonSwap = buttonSwap;
	data.base.graphicsThread = 17;
	data.base.accessThread = 19;
	data.base.fontThread = 18;
	data.base.soundThread = 16;
	data.action = status?PSP_NETCONF_ACTION_DISPLAYSTATUS:PSP_NETCONF_ACTION_CONNECTAP;
	data.hotspot = 0;

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

int oskDialog(unsigned short* in, int inlen, unsigned short* title, int titlelen, unsigned short* out, int maxoutlen) {
	int done=0;
	int i;
	
	unsigned short* intext;
	unsigned short* titletext;

	int buttonSwap = PSP_UTILITY_ACCEPT_CIRCLE;
	char* res;
	
	if (vmsettings_key_equals("com.pspkvm.default_keymap", "western")) {
		buttonSwap = PSP_UTILITY_ACCEPT_CROSS; }

	suspend_key_input = 1;
	//javacall_printf("oskDialog: inlen=%d\n", inlen);

	if (inlen >= maxoutlen) {
		inlen = maxoutlen - 1;
	}

	intext = malloc((inlen + 1) * 2);
	if (!intext) {
		printf("oskDialog: not enough memory\n");
		memcpy(out, in, inlen*2);
		return inlen;
	}
	
	titletext = malloc((titlelen + 1) * 2);
	if (!titletext) {
		free(intext);
		printf("oskDialog: not enough memory\n");
		memcpy(out, in, inlen*2);
		return inlen;
	}

	memset(out, 0, maxoutlen);
	
	memcpy(intext, in, inlen*2);
	memcpy(titletext, title, titlelen*2);
	intext[inlen] = 0;
	titletext[titlelen] = 0;

	SceUtilityOskData data;
	memset(&data, 0, sizeof(data));
	data.language = PSP_UTILITY_OSK_LANGUAGE_DEFAULT;
	data.lines = 1;				// just one line
	data.unk_24 = 1;			// set to 1
	data.desc = titletext;
	data.intext = intext;
	data.outtextlength = maxoutlen + 1;	// sizeof(outtext) / sizeof(unsigned short)
	data.outtextlimit = maxoutlen;		
	data.outtext = out;

	SceUtilityOskParams osk;
	memset(&osk, 0, sizeof(osk));
	osk.base.size = sizeof(osk);
	// dialog language: 0=Japanese, 1=English, 2=French, 3=Spanish, 4=German,
	// 5=Italian, 6=Dutch, 7=Portuguese, 8=Russian, 9=Korean, 10-11=Chinese, 12+=default
	sceUtilityGetSystemParamInt(PSP_SYSTEMPARAM_ID_INT_LANGUAGE, &osk.base.language);
	osk.base.buttonSwap = buttonSwap;		// X button: 1
	osk.base.graphicsThread = 17;	// gfx thread pri
	osk.base.accessThread = 19;			
	osk.base.fontThread = 18;
	osk.base.soundThread = 16;
	osk.datacount = 1;
	osk.data = &data;

	int rc = sceUtilityOskInitStart(&osk);
	if (rc) return 0;

	while(!done) {
		int i,j=0;

		sceGuStart(GU_DIRECT,_gu_list);

		// clear screen
		sceGuClearColor(0x33333333);
		sceGuClearDepth(0);
		sceGuClear(GU_COLOR_BUFFER_BIT|GU_DEPTH_BUFFER_BIT);

		sceGuFinish();
		sceGuSync(0,0);

		switch(sceUtilityOskGetStatus()){
			case PSP_UTILITY_DIALOG_INIT :
			break;
			case PSP_UTILITY_DIALOG_VISIBLE :
			sceUtilityOskUpdate(2); // 2 is taken from ps2dev.org recommendation
			break;
			case PSP_UTILITY_DIALOG_QUIT :
				printf("PSP_UTILITY_DIALOG_QUIT\n");
			sceUtilityOskShutdownStart();
			break;
			case PSP_UTILITY_DIALOG_FINISHED :
				printf("PSP_UTILITY_DIALOG_FINISHED\n");
			break;
			case PSP_UTILITY_DIALOG_NONE :
			done = 1;
			printf("PSP_UTILITY_DIALOG_NONE\n");
			break;
			default :
			break;
		}

		// wait TWO vblanks because one makes the input "twitchy"
		sceDisplayWaitVblankStart();
		sceDisplayWaitVblankStart();
		sceGuSwapBuffers();
	}

	for(i = 0; data.outtext[i]; i++);

	if (data.result == 1) {
	    //cancelled
	    memcpy(out, in, inlen*2);
	    i = inlen;
	}

	printf("end of osk: %d\n", data.result);
	sceKernelDelayThread(500000); 

	suspend_key_input = 0;

	free(intext);
	free(titletext);
	
	return i;
}


int main(void)
{
	SceUID thid;
	int res = 0;
	int ret_val = 0;
	int i;

	pspDebugScreenInit();
	for (i = 0; i < DEBUG_SCREEN_WIDTH; i++) BLANK_DEBUG_LINE[i] = ' ';
       BLANK_DEBUG_LINE[DEBUG_SCREEN_WIDTH] = '\0';

       javacall_logging_channel(JAVACALL_LOG_CHANNEL_STARTUP);


       javacall_printf("Setup GU\n");
	setup_gu();

	javacall_lcd_show_splash();

	javacall_printf("Loading network modules\n");
	
#if _PSP_FW_VERSION >= 200
       if ((ret_val = sceUtilityLoadNetModule(PSP_NET_MODULE_COMMON)) < 0) {
    		javacall_printf("Error, could not load inet modules %d\n", PSP_NET_MODULE_COMMON);    		
    		goto fail;
    	}

	if ((ret_val = sceUtilityLoadNetModule(PSP_NET_MODULE_INET)) < 0) {
		javacall_printf("Error, could not load inet modules %d\n", PSP_NET_MODULE_INET);
		goto fail;
	}

#if ENABLE_GPS
       usbgps_enabled = 0;

	if (res = moduleLoadStart("usbacc.prx")) {
		javacall_printf("Error, could not load usbacc.prx: %x\n", res);
	}

	if (res == 0 && (res = moduleLoadStart("usbgps.prx"))) {
		javacall_printf("Error, could not load usbgps.prx: %x\n", res);		
	}

	if (res == 0 && (res = startUSBGPSDrivers())) {
		javacall_printf("Error, could not start USB GPS drivers: %x\n", res);
	} else if (res == 0) {
		if (res = sceUsbGpsOpen()) {
			javacall_printf("Error, sceUsbGpsOpen: %x\n",  res);
		} else {
			javacall_printf("USB GPS enabled\n");
			usbgps_enabled = 1;
		}
	} else {
		javacall_printf("Module loading failed. Can not use GPS device!\n");
	}
#endif //ENABLE_GPS

#else	
    	if(ret_val = pspSdkLoadInetModules() < 0)	{	
    		javacall_printf("Error, could not load inet modules\n");
    		goto fail;
    	}
#endif
	

    	javacall_printf("Network module loaded\n");
    	
    	mp3codec_enabled = 1;
	ret_val = sceUtilityLoadModule(PSP_MODULE_AV_AVCODEC);
	if (ret_val<0)
	{
		javacall_printf("ERROR: sceUtilityLoadModule(PSP_MODULE_AV_AVCODEC) returned 0x%08X\n", ret_val);
		mp3codec_enabled = 0;
	}
	
	ret_val = sceUtilityLoadModule(PSP_MODULE_AV_MPEGBASE);
	if (ret_val<0)
	{
		javacall_printf("ERROR: sceUtilityLoadModule(PSP_MODULE_AV_MP3) returned 0x%08X\n", ret_val);
		mp3codec_enabled = 0;
	}
	

	ret_val = 0;
    	
	/* create user thread */
	thid = sceKernelCreateThread("Java Thread", java_main,
											0x25, // default priority
											512 * 1024, // stack size
											PSP_THREAD_ATTR_USER | PSP_THREAD_ATTR_VFPU, NULL); //# user mode

	// start user thread, then wait for it to do everything else
	sceKernelStartThread(thid, 0, 0);
	test_mem("java thread started");
	javacall_printf("sceKernelWaitThreadEnd...\n");
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
  sceUsbActivate(id);
}

void _sceUsbDeactivate(u32 id) {
  sceUsbDeactivate(id);
}

int mk_dir(const char* dir, SceMode mode)
{
    	return sceIoMkdir(dir, mode);
}

int rm_dir(const char* dir)
{
    	return sceIoRmdir(dir);
}


void pspkvm_screen_log(char* s) {
	static int x = 0;
	const int y = 2;
	static char buf[2] = {0, 0};
	while (*s) {
		
		if (x == 0) {
			pspDebugScreenSetXY(0, y);
			pspDebugScreenPrintf(BLANK_DEBUG_LINE);	
		}
		
		if (*s != '\r' && *s != '\n') {
			pspDebugScreenSetXY(x++, y);
			if (x >= DEBUG_SCREEN_WIDTH) x = 0;
			buf[0] = *s;
			pspDebugScreenPrintf(&buf[0]);
		} else {
			x = 0;
		}
		
		s++;
	}
}

