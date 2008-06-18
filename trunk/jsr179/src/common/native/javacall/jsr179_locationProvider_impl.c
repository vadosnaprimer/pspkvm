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
    
#include <kni.h>
#include <pcsl_string.h>
#include <pcsl_memory.h>

#include <javacall_location.h>
#include <jsr179_location.h>

/* JAVADOC COMMENT ELIDED */
jsr179_result jsr179_property_get(
        jsr179_property property,
        pcsl_string* /*OUT*/outPropertyValue) {

    javacall_location_property javacall_property;
    static javacall_utf16 list[JAVACALL_LOCATION_MAX_PROPERTY_LENGTH] = {0};

    switch (property) {
        case JSR179_PROVIDER_LIST:
            javacall_property = JAVACALL_LOCATION_PROVIDER_LIST;
            break;
        case JSR179_ORIENTATION_LIST:
            javacall_property = JAVACALL_LOCATION_ORIENTATION_LIST;
            break;
        default:
            return JSR179_STATUSCODE_FAIL;
    }
    
    if (javacall_location_property_get(javacall_property, list) != JAVACALL_OK) {
        return JSR179_STATUSCODE_FAIL;
    }

    if (PCSL_STRING_OK != 
        pcsl_string_convert_from_utf16(list, JAVACALL_LOCATION_MAX_PROPERTY_LENGTH, outPropertyValue)) {
        return JSR179_STATUSCODE_FAIL;
    }
    
    return JSR179_STATUSCODE_OK;
}

/* JAVADOC COMMENT ELIDED */
jsr179_result jsr179_provider_getinfo(
        const pcsl_string name,
        jsr179_provider_info* /*OUT*/pInfo) {
    
    javacall_location_provider_info jpInfo;
    javacall_utf16 pname[JAVACALL_LOCATION_MAX_PROPERTY_LENGTH];
    jsize len = JAVACALL_LOCATION_MAX_PROPERTY_LENGTH;
    javacall_result res;
    
    if (PCSL_STRING_OK == pcsl_string_convert_to_utf16(&name, pname, JAVACALL_LOCATION_MAX_PROPERTY_LENGTH, &len)) {
        res = javacall_location_provider_getinfo(pname, &jpInfo);
        switch(res) {
            case JAVACALL_OK:
                pInfo->incurCost = jpInfo.incurCost;
                pInfo->canReportAltitude = jpInfo.canReportAltitude;
                pInfo->canReportAddressInfo = jpInfo.canReportAddressInfo;
                pInfo->canReportSpeedCource = jpInfo.canReportSpeedCource;
                pInfo->powerConsumption = jpInfo.powerConsumption;
                pInfo->horizontalAccuracy = jpInfo.horizontalAccuracy;
                pInfo->verticalAccuracy = jpInfo.verticalAccuracy;
                pInfo->defaultTimeout = jpInfo.defaultTimeout;
                pInfo->defaultMaxAge = jpInfo.defaultMaxAge;
                pInfo->defaultInterval = jpInfo.defaultInterval;
                pInfo->averageResponseTime = jpInfo.averageResponseTime;
                pInfo->defaultStateInterval = jpInfo.defaultStateInterval;
                return JSR179_STATUSCODE_OK;
            case JAVACALL_INVALID_ARGUMENT:
                return JSR179_STATUSCODE_INVALID_ARGUMENT;
            default:
                return JSR179_STATUSCODE_FAIL;
        }
    }

    return JSR179_STATUSCODE_FAIL;
}
        
/* JAVADOC COMMENT ELIDED */
jsr179_result jsr179_provider_open(
        const pcsl_string name,
        /*OUT*/ jsr179_handle* pProvider) {
    javacall_utf16 pname[JAVACALL_LOCATION_MAX_PROPERTY_LENGTH];
    jsize len;

    pcsl_string_status res;

    printf("jsr179_provider_open: len=%d\n", name.length);

    if (PCSL_STRING_OK == (res = pcsl_string_convert_to_utf16(&name, pname, JAVACALL_LOCATION_MAX_PROPERTY_LENGTH, &len))) {
    	 printf("javacall_location_provider_open is called...\n");
        switch (javacall_location_provider_open(pname, pProvider)) {
            case JAVACALL_OK:
                return JSR179_STATUSCODE_OK;
            case JAVACALL_WOULD_BLOCK:
                return JSR179_STATUSCODE_WOULD_BLOCK;
            case JAVACALL_INVALID_ARGUMENT:
                return JSR179_STATUSCODE_INVALID_ARGUMENT;
            default:
                return JSR179_STATUSCODE_FAIL;
        }
    }

    printf("pcsl_string_convert_to_utf16 returns %d\n", res);

    return JSR179_STATUSCODE_FAIL;
}

/* JAVADOC COMMENT ELIDED */
jsr179_result jsr179_provider_close(
    jsr179_handle provider) {

	if (javacall_location_provider_close(provider) == JAVACALL_OK) {
		return JSR179_STATUSCODE_OK;
	}

    return JSR179_STATUSCODE_FAIL;
}

/* JAVADOC COMMENT ELIDED */
jsr179_result jsr179_provider_state(
        jsr179_handle provider,
        /*OUT*/ jsr179_state* pState) {

	javacall_location_state state;
	if (javacall_location_provider_state(provider, &state) == JAVACALL_OK) {
		*pState = state;
		return JSR179_STATUSCODE_OK;
	}

    return JSR179_STATUSCODE_FAIL;
}

/* JAVADOC COMMENT ELIDED */
jsr179_result jsr179_update_set(
        jsr179_handle provider, 
        jlong timeout) {

    switch (javacall_location_update_set(provider, timeout)) {
    	 case JAVACALL_WOULD_BLOCK:
    	     return JSR179_STATUSCODE_WOULD_BLOCK;
        case JAVACALL_OK:
            return JSR179_STATUSCODE_OK;
        default:
            return JSR179_STATUSCODE_FAIL;
    }

    return JSR179_STATUSCODE_FAIL;
}        

/* JAVADOC COMMENT ELIDED */
jsr179_result jsr179_update_cancel(
        jsr179_handle provider) {
    
    if (javacall_location_update_cancel(provider) == JAVACALL_OK) {
		return JSR179_STATUSCODE_OK;
	}

    return JSR179_STATUSCODE_FAIL;
}

/* JAVADOC COMMENT ELIDED */
jsr179_result jsr179_location_get(
        jsr179_handle provider, 
        /*OUT*/ jsr179_location* pLocationInfo) {

	javacall_location_location location;
	if (javacall_location_get(provider, &location) == JAVACALL_OK) {
		pLocationInfo->isValidCoordinate = location.isValidCoordinate;
		pLocationInfo->latitude = location.latitude;
		pLocationInfo->longitude = location.longitude;
		pLocationInfo->altitude = location.altitude;
		pLocationInfo->horizontalAccuracy = location.horizontalAccuracy;
		pLocationInfo->verticalAccuracy = location.verticalAccuracy;
		pLocationInfo->speed = location.speed;
		pLocationInfo->course = location.course;
		pLocationInfo->method = location.method;
		pLocationInfo->timestamp = location.timestamp;
		pLocationInfo->extraInfoSize = location.extraInfoSize;
		pLocationInfo->addressInfoFieldNumber = location.addressInfoFieldNumber;
		return JSR179_STATUSCODE_OK;
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

	javacall_utf16_string str_buffer;
	javacall_utf16 mimetype_buffer[JAVACALL_LOCATION_MAX_MIMETYPE_LENGTH];
    int len;

    str_buffer = (javacall_utf16_string)
        pcsl_mem_malloc(maxUnicodeStringBufferLen * sizeof(javacall_utf16));
    if (str_buffer == NULL) {
        return JSR179_STATUSCODE_FAIL;
    }
	
	pcsl_string_free(&outUnicodeStringBuffer[JSR179_EXTRAINFO_NMEA]);
	if (javacall_location_get_extrainfo(provider, JAVACALL_LOCATION_EXTRAINFO_NMEA,
					maxUnicodeStringBufferLen, str_buffer, mimetype_buffer) == JAVACALL_OK) {
        for (len=0; len<maxUnicodeStringBufferLen && str_buffer[len] != 0; len++);
        pcsl_string_convert_from_utf16(str_buffer, len, 
			&outUnicodeStringBuffer[JSR179_EXTRAINFO_NMEA]);
	}
	pcsl_string_free(&outUnicodeStringBuffer[JSR179_EXTRAINFO_LIF]);
	if (javacall_location_get_extrainfo(provider, JAVACALL_LOCATION_EXTRAINFO_LIF,
					maxUnicodeStringBufferLen, str_buffer, mimetype_buffer) == JAVACALL_OK) {
        for (len=0; len<maxUnicodeStringBufferLen && str_buffer[len] != 0; len++);
        pcsl_string_convert_from_utf16(str_buffer, len, 
			&outUnicodeStringBuffer[JSR179_EXTRAINFO_LIF]);
	}
	pcsl_string_free(&outUnicodeStringBuffer[JSR179_EXTRAINFO_PLAINTEXT]);
	if (javacall_location_get_extrainfo(provider, JAVACALL_LOCATION_EXTRAINFO_PLAINTEXT,
					maxUnicodeStringBufferLen, str_buffer, mimetype_buffer) == JAVACALL_OK) {
        for (len=0; len<maxUnicodeStringBufferLen && str_buffer[len] != 0; len++);
        pcsl_string_convert_from_utf16(str_buffer, len, 
			&outUnicodeStringBuffer[JSR179_EXTRAINFO_PLAINTEXT]);
	}
	pcsl_string_free(&outUnicodeStringBuffer[JSR179_EXTRAINFO_OTHER]);
	pcsl_string_free(&outOtherMIMETypeBuffer);
	if (javacall_location_get_extrainfo(provider, JAVACALL_LOCATION_EXTRAINFO_OTHER,
					maxUnicodeStringBufferLen, str_buffer, mimetype_buffer) == JAVACALL_OK) {
        for (len=0; len<maxUnicodeStringBufferLen && str_buffer[len] != 0; len++);
        pcsl_string_convert_from_utf16(str_buffer, len, 
			&outUnicodeStringBuffer[JSR179_EXTRAINFO_OTHER]);
        for (len=0; len<JAVACALL_LOCATION_MAX_MIMETYPE_LENGTH && mimetype_buffer[len] != 0; len++);
        pcsl_string_convert_from_utf16(mimetype_buffer, len, 
			&outOtherMIMETypeBuffer);
	}

    pcsl_mem_free(str_buffer);

    return JSR179_STATUSCODE_OK;
}
    
/* JAVADOC COMMENT ELIDED */
jsr179_result /*OPTIONAL*/ jsr179_get_addressinfo(
        jsr179_handle provider,
        /*IN and OUT*/ int* pAddresInfoFieldNumber,
        /*OUT*/jsr179_addressinfo_fieldinfo fields[]) {

	javacall_location_addressinfo_fieldinfo ai_fields[JSR179_MAX_ADDRESSINFO_FIELDS];
	jint numFields = JSR179_MAX_ADDRESSINFO_FIELDS;
	jint i;

	/* Clear AddressInfo first */
	for (i=0; i<numFields; i++) {
	    pcsl_string_free(&fields[i].data);
	}
	/* Fill AddressInfo */
    if (javacall_location_get_addressinfo(provider, &numFields,	ai_fields) == 
		JAVACALL_OK) {
		for (i=0; i<numFields; i++) {
			fields[i].fieldId = ai_fields[i].filedId;
	        pcsl_string_convert_from_utf16(ai_fields[i].data, JAVACALL_LOCATION_MAX_ADDRESSINFO_FIELD, 
				&fields[i].data);
		}
		*pAddresInfoFieldNumber = numFields;
	    return JSR179_STATUSCODE_OK;
	} else {
		*pAddresInfoFieldNumber = 0;
	}
    return JSR179_STATUSCODE_FAIL;
}

#ifdef __cplusplus
} //extern "C"
#endif

