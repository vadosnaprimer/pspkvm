/*
 *
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

#include <unistd.h>
#include <pcsl_file.h>
#include <pcsl_string.h>
#include <jvm.h>
#include <jsr179_location.h>
#include <pthread.h>
#include <stdio.h>

#define JSR179_MAX_PROVIDERS 2

jchar provider_name[JSR179_MAX_PROVIDERS][10] =
    {{'P','r','o','v','_','N','1',0},{'P','r','o','v','_','N','2',0}};
jint provider_name_len[JSR179_MAX_PROVIDERS] = {7, 7};
jint provider_descr[JSR179_MAX_PROVIDERS] = {1, 2};
jboolean provider_opened[JSR179_MAX_PROVIDERS] = {PCSL_FALSE, PCSL_FALSE};
jsr179_provider_info provider_info[JSR179_MAX_PROVIDERS] = {
    {PCSL_FALSE, PCSL_TRUE, PCSL_TRUE,  PCSL_TRUE, 
        0, 1,  1,  1000, 3000, 10000, 500,  1000},
    {PCSL_TRUE,  PCSL_TRUE, PCSL_FALSE, PCSL_TRUE, 
        0, 10, 10, 2000, 5000, 10000, 1000, 1000}};

jsr179_location providers_location[JSR179_MAX_PROVIDERS] = {
    {PCSL_TRUE, 1, 1, 1, 1, 1, 1, 1, 1, 0, 7, 2},
    {PCSL_TRUE, 2, 2, 2, 2, 2, 2, 2, 3, 0, 7, 0}};
jboolean update_cancel[JSR179_MAX_PROVIDERS] = {PCSL_FALSE, PCSL_FALSE};


/* Helper-functions */
static jint jsr179_file_open(jchar *fileName, jint flags, void **handle);
static jint jsr179_read_status(void *handle, jint *status);
static jint jsr179_read_location(void *handle, jdouble *latitude,
                                    jdouble *longitude, jfloat *altitude);
static jint jsr179_write_status(void *handle, jint status);
static jint jsr179_file_close(void *handle);
static jlong jsr179_get_time_millis();
static void *jsr179_open_provider(void* pArg);
static void *jsr179_update_location(void* pArg);
static void *jsr179_cancel_update_location(void* pArg);

/* JAVADOC COMMENT ELIDED */
jsr179_result jsr179_property_get(
        jsr179_property property,
        pcsl_string* /*OUT*/outPropertyValue) {
    jint i;
    switch(property) {
        case JSR179_PROVIDER_LIST:
            if (PCSL_STRING_OK != 
                pcsl_string_convert_from_utf16(provider_name[0], provider_name_len[0], outPropertyValue)) {
                return JSR179_STATUSCODE_FAIL;
            }
            for(i=1; i<JSR179_MAX_PROVIDERS; i++) {
                if (PCSL_STRING_OK != 
                    pcsl_string_append_char(outPropertyValue, JSR179_PROPERTY_SEPARATOR)) {
                    return JSR179_STATUSCODE_FAIL;
                }
                if (PCSL_STRING_OK != 
                    pcsl_string_append_buf(outPropertyValue, provider_name[i], provider_name_len[i])) {
                    return JSR179_STATUSCODE_FAIL;
                }
            }
            break;
        default:
            return JSR179_STATUSCODE_FAIL;
    }
    return JSR179_STATUSCODE_OK;
}

/* JAVADOC COMMENT ELIDED */
jsr179_result jsr179_provider_getinfo(
        const pcsl_string name,
        jsr179_provider_info* /*OUT*/pInfo) {
    
    jint i;
    jboolean found = PCSL_FALSE;
    pcsl_string pname;

    if(!pcsl_string_is_null(&name)) {
        for(i=0; i<JSR179_MAX_PROVIDERS && found == PCSL_FALSE; i++) {
            pcsl_string_convert_from_utf16(provider_name[i], provider_name_len[i], &pname);
            if (pcsl_string_equals(&name, &pname)) {
                found = PCSL_TRUE;
                pcsl_string_free(&pname);
                break;
            }
            pcsl_string_free(&pname);
        }
    } else {
        found = PCSL_TRUE;
        i = 0;
    }

    if(found == PCSL_TRUE) {
        *pInfo = provider_info[i];
        return JSR179_STATUSCODE_OK;
    }
    return JSR179_STATUSCODE_INVALID_ARGUMENT;
}

/* JAVADOC COMMENT ELIDED */
jsr179_result jsr179_provider_open(
        const pcsl_string name,
        /*OUT*/ jsr179_handle* pProvider) {

    jint i;
    jboolean found = PCSL_FALSE;
    pthread_t pHandle;
    pcsl_string pname;

    if(!pcsl_string_is_null(&name)) {
        for(i=0; i<JSR179_MAX_PROVIDERS && found == PCSL_FALSE; i++) {
            pcsl_string_convert_from_utf16(provider_name[i], provider_name_len[i], &pname);
            if (pcsl_string_equals(&name, &pname)) {
                found = PCSL_TRUE;
                pcsl_string_free(&pname);
                break;
            }
            pcsl_string_free(&pname);
        }
    } else {
        found = PCSL_TRUE;
        i = 0;
    }

    if(found == PCSL_TRUE) {
        if(provider_opened[i] == PCSL_FALSE) {
            /* Create thread to open Provider - non blocking */
            pthread_create(&pHandle, 0, jsr179_open_provider, (void *)i);
            *pProvider = (jsr179_handle)provider_descr[i];
            return JSR179_STATUSCODE_WOULD_BLOCK;
        }
        *pProvider = (jsr179_handle)provider_descr[i];
        return JSR179_STATUSCODE_OK;
    }
    return JSR179_STATUSCODE_FAIL;
}

/* JAVADOC COMMENT ELIDED */
jsr179_result jsr179_provider_close(
    jsr179_handle provider) {
    (void) provider;
    sleep(1);
    return JSR179_STATUSCODE_OK;
}

/* JAVADOC COMMENT ELIDED */
jsr179_result jsr179_provider_state(
        jsr179_handle provider,
        /*OUT*/ jsr179_state* pState) {
    jint pstate = JSR179_AVAILABLE;
    jint i;
    jsr179_handle fd;
    jboolean found = PCSL_FALSE;

    for(i=0; i<JSR179_MAX_PROVIDERS && found == PCSL_FALSE; i++) {
        if(provider_descr[i] == (int)provider) {
            found = PCSL_TRUE;
            break;
        }
    }
    if(found == PCSL_TRUE) {
        jsr179_file_open((jchar *)provider_name[i], PCSL_FILE_O_RDWR, &fd);
        jsr179_read_status(fd, &pstate);
        jsr179_file_close(fd);
    }
    *pState = pstate;
    return JSR179_STATUSCODE_OK;
}

/* JAVADOC COMMENT ELIDED */
jsr179_result jsr179_update_set(
        jsr179_handle provider,
        jlong timeout) {
    jint i;
    jboolean found = PCSL_FALSE;
    pthread_t pHandle;

    (void)timeout;

    for(i=0; i<JSR179_MAX_PROVIDERS && found == PCSL_FALSE; i++) {
        if(provider_descr[i] == (int)provider) {
            found = PCSL_TRUE;
            break;
        }
    }
    if(found == PCSL_TRUE) {
        /* Create thread to open Provider - non blocking */
        pthread_create(&pHandle, 0, jsr179_update_location, (void *)i);
        return JSR179_STATUSCODE_WOULD_BLOCK;
    }

    return JSR179_STATUSCODE_FAIL;
}

/* JAVADOC COMMENT ELIDED */
jsr179_result jsr179_update_cancel(
        jsr179_handle provider) {
    jint i;
    jboolean found = PCSL_FALSE;
    pthread_t pHandle;

    for(i=0; i<JSR179_MAX_PROVIDERS && found == PCSL_FALSE; i++) {
        if(provider_descr[i] == (int)provider) {
            found = PCSL_TRUE;
            break;
        }
    }
    if(found == PCSL_TRUE) {
        pthread_create(&pHandle, 0, jsr179_cancel_update_location, (void *)i);
        return JSR179_STATUSCODE_WOULD_BLOCK;
    }

    return JSR179_STATUSCODE_FAIL;
}

/* JAVADOC COMMENT ELIDED */
jsr179_result jsr179_location_get(
        jsr179_handle provider,
        /*OUT*/ jsr179_location* pLocationInfo) {
    jint i;
    jboolean found = PCSL_FALSE;

    for(i=0; i<JSR179_MAX_PROVIDERS && found == PCSL_FALSE; i++) {
        if(provider_descr[i] == (int)provider) {
            found = PCSL_TRUE;
            break;
        }
    }
    if(found == PCSL_TRUE) {
        if(providers_location[i].timestamp == 0) {
            return JSR179_STATUSCODE_FAIL;
        } else {
            *pLocationInfo = providers_location[i];
            return JSR179_STATUSCODE_OK;
        }
    }
    return JSR179_STATUSCODE_FAIL;
}


/******************************************************************************
 ******************************************************************************
 ******************************************************************************
    OPTIONAL FUNCTIONS
 ******************************************************************************
 ******************************************************************************
 ******************************************************************************/

/* JAVADOC COMMENT ELIDED */
jsr179_result /*OPTIONAL*/ jsr179_get_extrainfo(
        jsr179_handle provider,
        jint maxUnicodeStringBufferLen,
        /*OUT*/pcsl_string outUnicodeStringBuffer[],
        /*OUT*/pcsl_string outOtherMIMETypeBuffer) {

    jchar extrainfo[] = {'H','e','l','l','o','_',0};
    jchar extrainfo_mimeType[] = {'O','t','h','e','r',0};
    
    (void) maxUnicodeStringBufferLen;
    extrainfo[5] = (int)provider + '0';

	pcsl_string_free(&outUnicodeStringBuffer[JSR179_EXTRAINFO_NMEA]);
	pcsl_string_free(&outUnicodeStringBuffer[JSR179_EXTRAINFO_LIF]);
    pcsl_string_free(&outUnicodeStringBuffer[JSR179_EXTRAINFO_PLAINTEXT]);
	pcsl_string_free(&outUnicodeStringBuffer[JSR179_EXTRAINFO_OTHER]);
	pcsl_string_free(&outOtherMIMETypeBuffer);

    if (PCSL_STRING_OK != 
        pcsl_string_convert_from_utf16(extrainfo, 6, &outUnicodeStringBuffer[JSR179_EXTRAINFO_PLAINTEXT])) {
        return JSR179_STATUSCODE_FAIL;
    }
    if((int)provider == provider_descr[0]) {
        if (PCSL_STRING_OK != 
            pcsl_string_convert_from_utf16(extrainfo, 6, &outUnicodeStringBuffer[JSR179_EXTRAINFO_OTHER])) {
            return JSR179_STATUSCODE_FAIL;
        }
        if (PCSL_STRING_OK != 
            pcsl_string_convert_from_utf16(extrainfo_mimeType, 5, &outOtherMIMETypeBuffer)) {
            return JSR179_STATUSCODE_FAIL;
        }
    }
    return JSR179_STATUSCODE_OK;
};

/* JAVADOC COMMENT ELIDED */
jsr179_result /*OPTIONAL*/ jsr179_get_addressinfo(
        jsr179_handle provider,
        /*IN and OUT*/ jint* pAddresInfoFieldNumber,
        /*OUT*/jsr179_addressinfo_fieldinfo fields[]) {

    jchar country[] = {'R','u','s','s','i','a',0};
    jchar city[] = {'S','-','P','e','t','e','r','s','b','u','r','g',0};

    if((int)provider == provider_descr[0]) {
        if(*pAddresInfoFieldNumber >= 2){
            fields[0].fieldId = JSR179_ADDRESSINFO_COUNTRY;
            if (PCSL_STRING_OK != 
                pcsl_string_convert_from_utf16(country, 6, &fields[0].data)) {
                return JSR179_STATUSCODE_FAIL;
            }
            fields[1].fieldId = JSR179_ADDRESSINFO_CITY;
            if (PCSL_STRING_OK != 
                pcsl_string_convert_from_utf16(city, 6, &fields[1].data)) {
                return JSR179_STATUSCODE_FAIL;
            }
            *pAddresInfoFieldNumber = 2;
            return JSR179_STATUSCODE_OK;
        }
    }

    *pAddresInfoFieldNumber = 0;
    return JSR179_STATUSCODE_OK;
};

/* Helper-functions */

/**
 * (Internal) Open file.
 * fileName - File name.
 * flags - flags for open file.
 * handle - pointer for the file handler.
 */
static jint jsr179_file_open(jchar *fileName, jint flags, void **handle)
{
    pcsl_string fileNameStr;
    jint len=0;

    *handle = NULL;
    while (fileName[len]!=0) {
        len++;
    }

    pcsl_string_convert_from_utf16(fileName, len, &fileNameStr);

    if (pcsl_file_open(&fileNameStr, flags, handle)!= 0) {
        pcsl_string_free(&fileNameStr);
        return -1;
    }

    pcsl_string_free(&fileNameStr);
    if (*handle != NULL){
       return 0;
    }

    return -1;
}

/**
 * (Internal)
 * Read byte from a text file,
 * handle - file handler,
 * status - buffer for input,
 * return number of read symbols or -1 if something wrong
 */
static jint jsr179_read_status(void *handle, jint *status)
{
    unsigned char byte;
    if (pcsl_file_read(handle, &byte, 1) <= 0) {
        return -1;
    }
    if (byte >= '0' && byte <= '9') {
        *status = byte - '0';
    }
    return 0;
}

/**
 * (Internal)
 * Read location from a text file,
 * handle - file handler,
 * return number of read symbols or -1 if something wrong
 */
static jint jsr179_read_location(void *handle,
                       jdouble *latitude, jdouble *longitude, jfloat *altitude)
{
    jint status;

    unsigned char next_byte;
    jdouble loc[3]={0, 0, 0};
    jint ind=0;
    jint sign=1;
    jint irr_part = 0;

    /* read status */
    if (jsr179_read_status(handle, &status) < 0 ) {
        return -1;
    }

    if(status != JSR179_AVAILABLE) {
        return -1;
    }

    /* read location */
    while((pcsl_file_read(handle, &next_byte, 1) == 1) && (ind < 3) ) {
        if(next_byte == ':') {
            loc[ind] = loc[ind] * sign;
            ind++;
            sign = 1;
            irr_part = 0;
            continue;
        }
        if(next_byte == '-') {
            sign = -1;
            continue;
        }
        if(next_byte == '.') {
            irr_part = 1;
            continue;
        }
        if((next_byte >= '0') && (next_byte <= '9')) {
           next_byte -= '0';
           if(loc[ind] == JSR179_FLOAT_NAN) {
               loc[ind] = 0;
           }
           if(irr_part == 0) {
               loc[ind] = loc[ind]*(jdouble)10 + next_byte;
           } else {
               loc[ind] += (jdouble)next_byte/(jdouble)(10*irr_part);
               irr_part++;
           }
           continue;
        }
    }

    if (ind < 3) {
        loc[ind] = loc[ind] * sign;
        ind++;
    }

    if (ind == 3) {
        *latitude = loc[0];
        *longitude = loc[1];
        *altitude = loc[2];
        return 3;
    }

    return 0;
}

/**
 * (Internal)
 * Write byte to a text file,
 * handle - file handler,
 * status - buffer for output,
 * return number of read symbols or -1 if something wrong
 */
static jint jsr179_write_status(void *handle, jint status)
{
    unsigned char byte = (unsigned char)status;
    byte += '0';
    if (pcsl_file_write(handle, &byte, 1) <= 0) {
        return -1;
    }
    return 0;
}

/**
 * (Internal) Close file.
 * handle - file handler.
 */
static jint jsr179_file_close(void *handle)
{
    if (pcsl_file_close(handle) == 0) {
        return 0;
    }

    return -1;
}

/**
 * (Internal) Get time in milliseconds since midnight 1970.
 * handle - file handler.
 */
static jlong jsr179_get_time_millis()
{
    return 1;
}

/**
 * Open Provider thread
 */
static void *jsr179_open_provider(void* pArg)
{
    jint ind = (int)pArg;
    void *fd;
    if (jsr179_file_open((jchar *)provider_name[ind],
        PCSL_FILE_O_RDWR, &fd) != 0) {
        jsr179_file_open((jchar *)provider_name[ind],
                            PCSL_FILE_O_CREAT | PCSL_FILE_O_RDWR, &fd);
        jsr179_write_status(fd, JSR179_AVAILABLE);
    }

    jsr179_file_close(fd);
    sleep((provider_info[ind].averageResponseTime/1000)*2);
    provider_opened[ind] = PCSL_TRUE;
    update_cancel[ind] = PCSL_FALSE;

    jsr179_notify_location_event( JSR179_EVENT_OPEN_COMPLETED,
        (jsr179_handle)provider_descr[ind], JSR179_STATUSCODE_OK);
    return 0;
}

/**
 * Update Location thread
 */
static void *jsr179_update_location(void* pArg)
{
    jint ind = (int)pArg;
    void *fd = NULL;
    int ret = 0;
    jdouble latitude;
    jdouble longitude;
    jfloat altitude;
    sleep(provider_info[ind].averageResponseTime/1000);
    if (update_cancel[ind] != PCSL_TRUE) {
        if (jsr179_file_open((jchar *)provider_name[ind],
            PCSL_FILE_O_RDWR, &fd) == 0) {
            ret = jsr179_read_location(fd, &latitude, &longitude, &altitude);

            if (ret == 3) {
                providers_location[ind].latitude = latitude;
                providers_location[ind].longitude = longitude;
                providers_location[ind].altitude = altitude;
            }
            jsr179_file_close(fd);
            if (ret >=0) {
                /*(jlong)*/providers_location[ind].timestamp = jsr179_get_time_millis();
                jsr179_notify_location_event( JSR179_EVENT_UPDATE_ONCE,
                            (jsr179_handle)provider_descr[ind], JSR179_STATUSCODE_OK);
            } else {
                jsr179_notify_location_event( JSR179_EVENT_UPDATE_ONCE,
                            (jsr179_handle)provider_descr[ind], JSR179_STATUSCODE_FAIL);
            }
        }
    }
    return 0;
}

/**
 * Cancel Update Location thread
 */
static void *jsr179_cancel_update_location(void* pArg)
{
    jint ind = (int)pArg;
    update_cancel[ind] = PCSL_TRUE;
    sleep(1); /* 1 sec */
    jsr179_notify_location_event( JSR179_EVENT_UPDATE_ONCE,
             (jsr179_handle)provider_descr[ind], JSR179_STATUSCODE_CANCELED);
    update_cancel[ind] = PCSL_FALSE;
    return 0;
}

#ifdef __cplusplus
} //extern "C"
#endif


