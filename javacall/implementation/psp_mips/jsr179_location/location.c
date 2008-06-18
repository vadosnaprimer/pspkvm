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
    
#include "javacall_location.h"
#include "javacall_logging.h"

#include <pspkernel.h>
#include <pspsdk.h>
#include <pspusb.h>
#include "pspusbgps.h"


//#define printf	pspDebugScreenPrintf
#define VERBOSE_OUTPUT 0
#define DUMMY_GPS_DEVICE 0

#if VERBOSE_OUTPUT
#define VERBOSE(x) javacall_print(x)
#else
#define VERBOSE(x)
#endif

#define JAVACALL_GPS_OPER_OPEN (0)
#define JAVACALL_GPS_OPER_UPDATE (1)
#define JAVACALL_GPS_STATUS_UNINITIALIZED (0)
#define JAVACALL_GPS_STATUS_INITIALIZED (1)
#define JAVACALL_GPS_STATUS_FAILED (-1)

#define MAX_OPEN_RETRY_TIMES 10

extern int usbgps_enabled;
static int usbgps_status = JAVACALL_GPS_STATUS_UNINITIALIZED;

static javacall_location_location providers_location =    
{JAVACALL_TRUE, 14.393338422069922, 50.098237322508126, 5510.23, 1.0, 1.0, 20.1, 1.0, JAVACALL_LOCATION_MTE_SATELLITE, 0, 0, 0};
static javacall_location_provider_info provider_info =
{JAVACALL_TRUE,  JAVACALL_TRUE, JAVACALL_FALSE, JAVACALL_TRUE,
        0, 10, 10, 2000, 5000, 10000, 1000, 1000};

static gpsdata gpsd;
static satdata  satd;

static int gps290_thread(SceSize args, void *argp) {
	if (*(int*)argp == JAVACALL_GPS_OPER_OPEN) {
		int retry = 0;
		
	    _sceUsbActivate(PSP_USBGPS_PID);
	    for (;;) {
	    	 u32 usbState, gpsState;
     
               // Get state of the USB
               usbState = _sceUsbGetState();
               //pspDebugScreenPrintf("usbState:0x%08x\n", usbState);
               if (usbState & PSP_USB_ACTIVATED) {
                   sceUsbGpsGetState(&gpsState);
                   //pspDebugScreenPrintf("gpsState:0x%08x\n", gpsState);
                   if (gpsState == 0x03) {
                   	  VERBOSE("GPS open success\n");
                   	  usbgps_status = JAVACALL_GPS_STATUS_INITIALIZED;
                       javanotify_location_event(JAVACALL_EVENT_LOCATION_OPEN_COMPLETED, 1, JAVACALL_OK);
                       break;
                   } else {
                       if (retry++ >= MAX_OPEN_RETRY_TIMES) {
                       	usbgps_status = JAVACALL_GPS_STATUS_FAILED;
                       	VERBOSE("GPS open failed\n");
                       	javanotify_location_event(JAVACALL_EVENT_LOCATION_OPEN_COMPLETED, 1, JAVACALL_FAIL);
                       	break;
                       }
                   }
               }

               sceKernelDelayThread(1000000);
	    }	    
	} else {
#if DUMMY_GPS_DEVICE
           VERBOSE("javanotify_location_event\n");
	    javanotify_location_event(JAVACALL_EVENT_LOCATION_UPDATE_ONCE, 1, JAVACALL_OK);
#else
           if (usbgps_status == JAVACALL_GPS_STATUS_INITIALIZED) {
	    
               memset(&gpsd,0x0,sizeof(gpsd));        
               memset(&satd,0,sizeof(satd));
               
               sceUsbGpsGetData(&gpsd,&satd);
               providers_location.latitude = (double)gpsd.latitude;
               providers_location.longitude = (double)gpsd.longitude;
               providers_location.altitude = gpsd.altitude;
               providers_location.speed = gpsd.speed;
               providers_location.course = gpsd.bearing;
               providers_location.isValidCoordinate = (gpsd.hdop > 0 && gpsd.hdop < 7 &&
               	                                                        gpsd.latitude > 0 && gpsd.longitude > 0)?JAVACALL_TRUE:JAVACALL_FALSE;
               //pspDebugScreenPrintf("sat: %d, latitude:%f, longtitude:%f, altitude:%f, speed:%f, bearing:%f, HDOP:%f\n", 
               //	satd.satellites_in_view, gpsd.latitude, gpsd.longitude, gpsd.altitude, gpsd.speed, gpsd.bearing, gpsd.hdop);
               javanotify_location_event(JAVACALL_EVENT_LOCATION_UPDATE_ONCE, 1, JAVACALL_OK);
	    } else {
               javanotify_location_event(JAVACALL_EVENT_LOCATION_UPDATE_ONCE, 1, JAVACALL_FAIL);
	    }
#endif
	}
	sceKernelExitDeleteThread(0);
	return 0;
}

/**
 * Gets the values of the specified property.
 *
 * If there are more than one items in a property string, the items are separated by comma.
 *
 * The following properties should be provided:
 *  - JAVACALL_LOCATION_PROVIDER_LIST
 *   The lists of location providers.
 *  - JAVACALL_LOCATION_ORIENTATION_LIST
 *   The lists of orientation providers. An empty string means that orientation is not supported.
 *
 * @param property id of property
 * @param outPropertyValue UNICODE string value.
 *        Size of this buffer should be JAVACALL_LOCATION_MAX_PROPERTY_LENGTH
 * @retval JAVACALL_OK      success
 * @retval JAVACALL_FAIL    fail
 */
javacall_result javacall_location_property_get(
        javacall_location_property property,
        javacall_utf16_string /*OUT*/outPropertyValue) {
    outPropertyValue[0] = (javacall_utf16)'G';
    outPropertyValue[1] = (javacall_utf16)'P';
    outPropertyValue[2] = (javacall_utf16)'S';
    outPropertyValue[3] = (javacall_utf16)'2';
    outPropertyValue[4] = (javacall_utf16)'9';
    outPropertyValue[5] = (javacall_utf16)'0';
    outPropertyValue[6] = (javacall_utf16)0;
    return JAVACALL_OK;
}

/**
 * Get the information for the given name of location provider.
 *
 * This function only gets information and is intended to return it quickly. 
 *
 * The valid provider name is listed in JAVACALL_LOCATION_PROVIDER_LIST property.
 * 
 * @param name of the location provider, NULL implies the default location provider
 * @param pInfo  the information of the location provider
 * 
 * @retval JAVACALL_OK      success
 * @retval JAVACALL_INVALID_ARGUMENT  if the given name of location provider is not found.
 * @retval JAVACALL_FAIL    otherwise, fail
 */
javacall_result javacall_location_provider_getinfo(
        const javacall_utf16_string name,
        javacall_location_provider_info* /*OUT*/pInfo) {
    *pInfo = provider_info;
    return JAVACALL_OK;
}
        
/**
 * Initializes a provider.
 *
 * The name will be the loaction or orientation provider.
 * The name of the location provider is in JAVACALL_LOCATION_PROVIDER_LIST property. 
 * Orientation device name is in JAVACALL_LOCATION_ORIENTATION_LIST property. 
 *
 * see javanotify_location_event
 *
 * @param name  of the location provider
 * @param pProvider handle of the location provider
 *
 * @retval JAVACALL_OK      success
 * @retval JAVACALL_WOULD_BLOCK  javanotify_location_event needs to be called to notify completion
 * @retval JAVACALL_INVALID_ARGUMENT  if the specified provider is not found.
 *
 * @retval JAVACALL_FAIL    out of service or other error
 */
javacall_result javacall_location_provider_open(
        const javacall_utf16_string name,
        /*OUT*/ javacall_handle* pProvider){
    static int type = JAVACALL_GPS_OPER_OPEN;
    *pProvider = 1;

    VERBOSE("javacall_location_provider_open\n");

    if (DUMMY_GPS_DEVICE) {
        return JAVACALL_OK;
    }
    
    if (!usbgps_enabled) {
    	  javacall_print("javacall_location_provider_open failed\n");
    	  return JAVACALL_FAIL;
    }

    

    switch (usbgps_status) {
    	case JAVACALL_GPS_STATUS_UNINITIALIZED:
    	{
           SceUID thid = sceKernelCreateThread("gps290_thread", gps290_thread, 0x2a, 16 * 1024, PSP_THREAD_ATTR_USER, NULL);	
           if(thid < 0) {	
               javacall_printf("Error, could not create thread\n");	
               return JAVACALL_FAIL;	
           }
           sceKernelStartThread(thid, sizeof(int), &type);
           return JAVACALL_WOULD_BLOCK;
       }
    	break;
    	case JAVACALL_GPS_STATUS_INITIALIZED:
    	    return JAVACALL_OK;
    	case JAVACALL_GPS_STATUS_FAILED:
    	    usbgps_status = JAVACALL_GPS_STATUS_UNINITIALIZED;
    	    return JAVACALL_FAIL;
    }    
}

/**
 * Closes the opened provider.
 *
 * This function must free all resources allocated for the specified provider.
 *
 * @param pProvider handle of a provider
 *
 * @retval JAVACALL_OK          success
 * @retval JAVACALL_FAIL        if there was an error
 */
javacall_result javacall_location_provider_close(
    javacall_handle provider) {
    VERBOSE("javacall_location_provider_close\n");
    if (!DUMMY_GPS_DEVICE) {
        _sceUsbDeactivate(PSP_USBGPS_PID);
    }
    usbgps_status = JAVACALL_GPS_STATUS_UNINITIALIZED;
    return JAVACALL_OK;
}

/**
 * Gets the status of the location provider.
 * This function only get the current state and is intended to return it quickly. 
 *
 * @param provider handle of the location provider
 * @param pState returns state of the specified provider.
 *
 * @retval JAVACALL_OK          success
 * @retval JAVACALL_FAIL        if there was an error
 */
javacall_result javacall_location_provider_state(
        javacall_handle provider,
        /*OUT*/ javacall_location_state* pState) {
    if (DUMMY_GPS_DEVICE || usbgps_status == JAVACALL_GPS_STATUS_INITIALIZED) {
        *pState = JAVACALL_LOCATION_AVAILABLE;
    } else {
        *pState = JAVACALL_LOCATION_OUT_OF_SERVICE;
    }
    return JAVACALL_OK;
}


/**
 * Requests a location acquisition.
 *
 * This function only requests location update and is intended to return it quickly. 
 * The location update will be get through javanotify_location_event() with JAVACALL_EVENT_LOCATION_UPDATE_ONCE type.
 * This function will not be called again before javanotify_location_event is called for the previous request completion.
 * If timeout expires before obtaining the location result, javanotify_location_event() will be called with JAVACALL_LOCATION_RESULT_TIMEOUT reason.
 *
 * see javanotify_location_event
 *
 * @param provider handle of the location provider
 * @param timeout timeout in milliseconds. -1 implies default value.
 *
 * @retval JAVACALL_OK                  success
 * @retval JAVACALL_FAIL                if gets a invalid location or other error
 */
javacall_result javacall_location_update_set(javacall_handle provider, javacall_int64 timeout) {
    static int type = JAVACALL_GPS_OPER_UPDATE;
    
    VERBOSE("javacall_location_update_set\n");

    if (DUMMY_GPS_DEVICE) {
        return JAVACALL_OK;
    }
    
    SceUID thid = sceKernelCreateThread("gps290_thread", gps290_thread, 0x2a, 16 * 1024, PSP_THREAD_ATTR_USER, NULL);	
    if(thid < 0) {	
        javacall_printf("Error, could not create thread\n");	
        return JAVACALL_FAIL;	
    }
    sceKernelStartThread(thid, sizeof(int), &type);
    return JAVACALL_WOULD_BLOCK;
}

/**
 * Cancels the current location acquisition.
 * 
 * This function will incur calling javanotify_location_event() with JAVACALL_LOCATION_RESULT_CANCELED reason.
 *
 * see javanotify_location_event
 *
 * @param provider handle of the location provider
 *
 * @retval JAVACALL_OK          success
 * @retval JAVACALL_FAIL        if there is an  error
 */
javacall_result javacall_location_update_cancel(javacall_handle provider) {
    
    return JAVACALL_OK;
}

/**
 * Gets a location information after location update notification.
 *
 * The location update will be notified through javanotify_location_event() with 
 * JAVACALL_EVENT_LOCATION_UPDATE_ONCE or JAVACALL_EVENT_LOCATION_UPDATE_PERIODICALLY type. 
 *
 * see javanotify_location_event
 *
 * @param provider handle of the location provider
 * @param pLocationInfo location info
 *
 * @retval JAVACALL_OK                  success
 * @retval JAVACALL_FAIL                if gets a invalid location or other error
 */
javacall_result javacall_location_get(javacall_handle provider, 
    /*OUT*/ javacall_location_location* pLocationInfo) {
    VERBOSE("javacall_location_get\n");
    *pLocationInfo = providers_location;
    return JAVACALL_OK;
}


/******************************************************************************
 ******************************************************************************
 ******************************************************************************
    OPTIONAL FUNCTIONS
 ******************************************************************************
 ******************************************************************************
 ******************************************************************************/
    
/**
 * @defgroup jsrOptionalLocation OPTIONAL misc location API
 * @ingroup Location
 * 
 * @{
 */

/**
 * Gets the extra info
 *
 * This information is from the current location result. 
 * This function will be called in the callback function of location updates,
 * so the implementation should be fast enough.
 *
 * @param provider handle of the location provider
 * @param mimetype MIME type of extra info
 * @param maxUnicodeStringBufferLen length of value. The length
 *          should be equal or larger than extraInfoSize 
 *          of the acquired location.
 * @param outUnicodeStringBuffer contents of extrainfo 
 * @param outMimeTypeBuffer name of Other MIME type extraInfo
 *
 * @retval JAVACALL_OK      success
 * @retval JAVACALL_FAIL    fail
 */
javacall_result /*OPTIONAL*/ javacall_location_get_extrainfo(
        javacall_handle provider,
        javacall_location_extrainfo_mimetype mimetype,
        int maxUnicodeStringBufferLen,
        /*OUT*/javacall_utf16_string outUnicodeStringBuffer,
        /*OUT*/javacall_utf16_string outMimeTypeBuffer) {
    javacall_printf("STUB: javacall_location_get_extrainfo\n");
    return JAVACALL_FAIL;
}
    
/**
 * Gets the address info
 *
 * This information is from the current location result. 
 *
 * @param provider handle of the location provider
 * @param pAddresInfoFieldNumber used for both input and output number of array elements. 
 *          Input number should be equal or larger than
 *          addressInfoFieldNumber of the acquired location.
 * @param fields array of address info field
 *
 * @retval JAVACALL_OK      success
 * @retval JAVACALL_FAIL    fail
 */
javacall_result /*OPTIONAL*/ javacall_location_get_addressinfo(
        javacall_handle provider,
        /*IN and OUT*/ int* pAddresInfoFieldNumber,
        /*OUT*/javacall_location_addressinfo_fieldinfo fields[]) {
    javacall_printf("STUB:javacall_location_get_addressinfo\n");
    return JAVACALL_FAIL;
};

/**
 * Computes atan2(y, x) for the two double values.
 *
 * The atan2 math function is used to perform azimuth
 * and distance calculations.
 *
 * @param x first double
 * @param y second double
 *
 * @retval atan2 for the two double values
 */
double /*OPTIONAL*/ javacall_location_atan2(
      double x, double y) {
    return atan2(x, y);
};

#ifdef __cplusplus
} //extern "C"
#endif

