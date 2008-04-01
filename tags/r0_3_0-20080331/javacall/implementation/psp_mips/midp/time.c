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

#include "javacall_time.h"
#include <pspthreadman.h>
#include <psputility_sysparam.h>

#include <time.h>
#include <sys/time.h>
#include <errno.h>
#include <stdio.h>
#include <stdlib.h>

#define MAX_TIMERS 5

typedef struct _timer{
    SceUID alarmID;
    javacall_bool cyclic;
    SceUInt uSecToWait;
    javacall_callback_func cbFunc;
} timer;

static timer timers[MAX_TIMERS];
static int timer_slots[MAX_TIMERS] = {0};

static javacall_int64 time_offset = 0;

static SceUInt alarm_handler(void *common)
{
    int i = (int)common;
    if (i < 0 || i >= MAX_TIMERS) {
    	//javacall_print("Callback: timer has invalid param!\n");
    	return 0;
    }
    javacall_callback_func func = timers[i].cbFunc;
    func((javacall_handle *)i);
    //javacall_print("alarm_handler\n");
    return timers[i].cyclic?timers[i].uSecToWait:0;
}

static int  get_free_timer_slot() {
    int i;
    for (i = 0; i < MAX_TIMERS; i++) {
    	if (!timer_slots[i]) {
           timer_slots[i] = -1;
           return i;
    	}
    }
    return -1;
}
/**
 *
 * Create a native timer to expire in wakeupInSeconds or less seconds.
 *
 * @param wakeupInMilliSecondsFromNow time to wakeup in milli-seconds
 *                              relative to current time
 *                              if -1, then ignore the call
 * @param cyclic <tt>JAVACALL_TRUE</tt>  indicates that the timer should be 
 *               repeated cuclically, 
 *               <tt>JAVACALL_FALSE</tt> indicates that this is a one-shot 
 *               timer that should call the callback function once
 * @param func callback function should be called in platform's context once the timer
 *			   expires
 * @param handle A pointer to the returned handle that on success will be 
 *               associated with this timer.
 *
 * @return on success returns <tt>JAVACALL_OK</tt>, 
 *         or <tt>JAVACALL_FAIL</tt> or negative value on failure
 */
javacall_result javacall_time_initialize_timer(
                    int                      wakeupInMilliSecondsFromNow, 
                    javacall_bool            cyclic, 
                    javacall_callback_func   func,
					/*OUT*/ javacall_handle	*handle){
    if (wakeupInMilliSecondsFromNow <= 0) {
        javacall_print("javacall_time_initialize_timer invalid param!\n");
        return JAVACALL_FAIL;
    }
    int slot = get_free_timer_slot();
    if (slot < 0) {
    	 javacall_print("[Javacall Error]No available timer!\n");
        return JAVACALL_FAIL;
    }

    SceUInt uSec = (SceUInt)wakeupInMilliSecondsFromNow*1000;
    SceUID id = sceKernelSetAlarm(uSec, alarm_handler, (void*)slot);
    if (id < 0) {
    	javacall_print("[Javacall Error]sceKernelSetAlarm failed!\n");
    	timer_slots[slot] = 0;
    	return JAVACALL_FAIL;
    }
    timers[slot].alarmID = id;
    timers[slot].cyclic = cyclic;
    timers[slot].cbFunc = func;
    timers[slot].uSecToWait = uSec;
    *handle = (javacall_handle)slot;
//    printf("javacall_time_initialize_timer:%d, %d\n", cyclic, uSec);
    return JAVACALL_OK;
}

/**
 *
 * Disable a set native timer 
 * @param handle The handle of the timer to be finalized
 *
 * @return on success returns <tt>JAVACALL_OK</tt>, 
 *         <tt>JAVACALL_FAIL</tt> or negative value on failure
 */
javacall_result javacall_time_finalize_timer(javacall_handle handle){
    int i = (int)handle;
    if (i < 0 || i >=MAX_TIMERS) {
    	javacall_print("javacall_time_finalize_timer try to finalize invalid timer\n");
    	return JAVACALL_FAIL;
    }
//    javacall_print("javacall_time_finalize_timer:ok\n");
    sceKernelCancelAlarm(timers[i].alarmID);
    timer_slots[i] = 0;
    return JAVACALL_OK;
}

/**
 *
 * Create a native timer to expire in wakeupInSeconds or less seconds.
 * At least one native timer can be used concurrently.
 * If a later timer exists, cancel it and create a new timer
 *
 * @param type type of alarm to set, either JAVACALL_TIMER_PUSH, JAVACALL_TIMER_EVENT 
 *                              or JAVACALL_TIMER_WATCHDOG 
 * @param wakeupInMilliSecondsFromNow time to wakeup in milli-seconds
 *                              relative to current time
 *                              if -1, then ignore the call
 *
 * @return <tt>JAVACALL_OK</tt> on success, <tt>JAVACALL_FAIL</tt> on failure
 */
////javacall_result	javacall_time_create_timer(javacall_timer_type type, int wakeupInMilliSecondsFromNow){
//    return JAVACALL_FAIL;
//}

/**
 * Return local timezone ID string. This string is maintained by this 
 * function internally. Caller must NOT try to free it.
 *
 * This function should handle daylight saving time properly. For example,
 * for time zone America/Los_Angeles, during summer time, this function
 * should return GMT-07:00 and GMT-08:00 during winter time.
 *
 * @return Local timezone ID string pointer. The ID string should be in the
 *         format of GMT+/-??:??. For example, GMT-08:00 for PST.
 */
char* javacall_time_get_local_timezone(void){
    //return (char*) "GMT+08:00";
    int tzOffset = 0;
    sceUtilityGetSystemParamInt(PSP_SYSTEMPARAM_ID_INT_TIMEZONE, &tzOffset);
    int tzOffsetAbs = tzOffset < 0 ? -tzOffset : tzOffset;
    int hours = tzOffsetAbs / 60;
    int minutes = tzOffsetAbs - hours * 60;
    static char tz[11];
    sprintf(tz, "GMT%s%02i:%02i", tzOffset < 0 ? "-" : "+", hours, minutes);
    return tz;
}


static void set_time_offset() {
    javacall_int64 lt;
    int ret = sceRtcGetCurrentTick(&lt);    
    if (ret == 0) {
    	 javacall_int64 base = sceKernelGetSystemTimeLow() / 1000L;
        javacall_int64 pers = sceRtcGetTickResolution();
        lt = lt / pers;
        lt -= 62135596800LL;
        time_offset = lt * 1000LL;
        time_offset -= base;
    }
}

/**
 * returns number of milliseconds elapsed since midnight(00:00:00), January 1, 1970,
 *
 * @return milliseconds elapsed since midnight (00:00:00), January 1, 1970
 */
javacall_int64 /*OPTIONAL*/ javacall_time_get_milliseconds_since_1970(void){
    if (time_offset == 0) {
    	 set_time_offset();
    }
    javacall_int64 ret = (javacall_int64)sceKernelGetSystemTimeLow() / 1000LL + time_offset;
    //printf("javacall_time_get_milliseconds_since_1970:%d\n",ret);
    return ret;
}
 
/**
 * returns the number of seconds elapsed since midnight (00:00:00), January 1, 1970,
 *
 * @return seconds elapsed since midnight (00:00:00), January 1, 1970
 */
javacall_time_seconds /*OPTIONAL*/ javacall_time_get_seconds_since_1970(void){
    if (time_offset == 0) {
    	 set_time_offset();
    }
    return (javacall_time_seconds)((javacall_int64)sceKernelGetSystemTimeLow() / 1000000LL + time_offset / 1000LL);
}

/**
 * returns the milliseconds elapsed time counter
 *
 * @return elapsed time in milliseconds
 */
javacall_time_milliseconds /*OPTIONAL*/ javacall_time_get_clock_milliseconds(void){
    return 0;
}

#ifdef __cplusplus
}
#endif

