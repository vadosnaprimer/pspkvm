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
#include <kni.h>
#include <sni.h>
#include <commonKNIMacros.h>
#include <midpMalloc.h>
#include <midpString.h>
#include <midpUtilKni.h>
#include <midpServices.h>
#include <midpError.h>

#include <stdio.h>
#include <jsr179_location.h>

#include <midp_thread.h>
#include <midpEvents.h>

#define MAX_PROVIDERS   5   /* max number of Platform providers */
/**
 * internal information about opened Location Provider
 */
typedef struct {
    jboolean locked;
	jboolean newLocationAvailable;
    jint num_instances;
    jint id;
    jlong lastLocationTimestamp;
    jsr179_location lastLocation;
    jsr179_addressinfo_fieldinfo lastAddressInfo[JSR179_MAX_ADDRESSINFO_FIELDS];
    pcsl_string lastExtraInfo[JSR179_MAX_EXTRAINFO_TYPES];
    pcsl_string otherExtraInfoMimeType;
}ProviderInfo;
static ProviderInfo providerInfo[MAX_PROVIDERS];
static jint numOpenedProviders = 0;

/** ID of last updated Location Provider */
static jint lastUpdatedProvider = -1;


/**
 * Field IDs of the <tt>com.sun.j2me.location.LocationInfo</tt> class
 * The fields initialized at startup and used to speedup access to the fields
 */
typedef struct{
    jfieldID isValid;
    jfieldID timestamp;
    jfieldID latitude;
    jfieldID longitude;
    jfieldID altitude;
    jfieldID horizontalAccuracy;
    jfieldID verticalAccuracy;
    jfieldID speed;
    jfieldID course;
    jfieldID method;
    /* AddressInfo fields */
    jfieldID isAddressInfo;
    jfieldID AddressInfo_EXTENSION;
    jfieldID AddressInfo_STREET;
    jfieldID AddressInfo_POSTAL_CODE;
    jfieldID AddressInfo_CITY;
    jfieldID AddressInfo_COUNTY;
    jfieldID AddressInfo_STATE;
    jfieldID AddressInfo_COUNTRY;
    jfieldID AddressInfo_COUNTRY_CODE;
    jfieldID AddressInfo_DISTRICT;
    jfieldID AddressInfo_BUILDING_NAME;
    jfieldID AddressInfo_BUILDING_FLOOR;
    jfieldID AddressInfo_BUILDING_ROOM;
    jfieldID AddressInfo_BUILDING_ZONE;
    jfieldID AddressInfo_CROSSING1;
    jfieldID AddressInfo_CROSSING2;
    jfieldID AddressInfo_URL;
    jfieldID AddressInfo_PHONE_NUMBER;
    /* ExtraInfo field */
    jfieldID extraInfoNMEA;
    jfieldID extraInfoLIF;
    jfieldID extraInfoPlain;
    jfieldID extraInfoOther;
    jfieldID extraInfoOtherMIMEType;
}LocationInfoFieldIDs;
static LocationInfoFieldIDs locationInfoFieldID;

/**
 * Field IDs of the <tt>com.sun.j2me.location.LocationProviderInfo</tt> class
 * The fields initialized at startup and used to speedup access to the fields
 */
typedef struct{
    jfieldID incurCost;
    jfieldID canReportAltitude;
    jfieldID canReportAddressInfo;
    jfieldID canReportSpeedCource;
    jfieldID powerConsumption;
    jfieldID horizontalAccuracy;     
    jfieldID verticalAccuracy;       
    jfieldID defaultTimeout;         
    jfieldID defaultMaxAge;          
    jfieldID defaultInterval;        
    jfieldID averageResponseTime;    
    jfieldID defaultStateInterval;
}LocationProviderInfoFieldIDs;
static LocationProviderInfoFieldIDs locationProviderInfoFieldID;

/* Helper-functions */
static void lock_thread(jint waitingFor, jint provider);
/*static void unlock_threads(jint waitingFor, jint provider);*/
static ProviderInfo* register_provider(jint provider_id);
static jboolean unregister_provider(jint provider_id);
static ProviderInfo* getProviderInfo(jint provider_id);
void notifyLocationEvent(jsr179_handle provider_id, jint status, jint event);

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_OBJECT
Java_com_sun_j2me_location_PlatformLocationProvider_getListOfLocationProviders() {

    pcsl_string listOfProviders = PCSL_STRING_NULL;

    KNI_StartHandles(1);
    KNI_DeclareHandle(tempHandle);

    if (jsr179_property_get(JSR179_PROVIDER_LIST, 
        &listOfProviders) == JSR179_STATUSCODE_OK) {
        midp_jstring_from_pcsl_string(&listOfProviders, tempHandle);
    }
	pcsl_string_free(&listOfProviders);
    KNI_EndHandlesAndReturnObject(tempHandle);
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_INT
    Java_com_sun_j2me_location_PlatformLocationProvider_open() {

    MidpReentryData *info = NULL;
    ProviderInfo *pInfo = NULL;
    jsr179_result res;
    jsr179_handle pProvider = NULL;
    jint provider = 0;
    
    KNI_StartHandles(1);
    GET_PARAMETER_AS_PCSL_STRING(1, name)
    info = (MidpReentryData*)SNI_GetReentryData(NULL);
    if (info == NULL) {
        res = jsr179_provider_open(name, &pProvider);
        switch (res) {
            case JSR179_STATUSCODE_OK:
                /* handler returned immediatelly */
                provider = (jint)pProvider;
                register_provider(provider);
                break;
            case JSR179_STATUSCODE_INVALID_ARGUMENT:
                /* wrong provider name */
                KNI_ThrowNew(midpIllegalArgumentException, 
                            "wrong provider name");
                break;
            case JSR179_STATUSCODE_FAIL:
                /* fail */
                KNI_ThrowNew(midpIOException, "open failed");
                break;
            case JSR179_STATUSCODE_WOULD_BLOCK:
                /* wait for javanotify */
                provider = (jint)pProvider;
                pInfo = register_provider(provider);
                if (pInfo != NULL) {
                    pInfo->locked = KNI_TRUE;
                }
                lock_thread(JSR179_EVENT_OPEN_COMPLETED, provider);
                break;
            default:
                break;
        }
    } else {
        /* Second call for this thread - finish open */
        if (info->status == JSR179_STATUSCODE_OK) {
            /* Provider opened successfully */
            provider = info->descriptor;
            pInfo = getProviderInfo(provider);
            if (pInfo != NULL) {
                pInfo->locked = KNI_FALSE;
            }
        } else {
            /* Provider open failed*/
            unregister_provider(provider);
            provider = 0;
        }
    }

    RELEASE_PCSL_STRING_PARAMETER
    KNI_EndHandles();
    KNI_ReturnInt(provider);
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_BOOLEAN
    Java_com_sun_j2me_location_PlatformLocationProvider_getCriteria() {

    jsr179_provider_info provider_info;
    jsr179_result res;
    jboolean ret = KNI_FALSE;

    KNI_StartHandles(3);
    /* get NULL terminated provider name */
    KNI_DeclareHandle(criteria);
    KNI_DeclareHandle(class_obj);
    
    GET_PARAMETER_AS_PCSL_STRING(1, name)

    /* call provider_open to get provider handler */
    res = jsr179_provider_getinfo(name, &provider_info);
    if (res == JSR179_STATUSCODE_OK) {
        KNI_GetParameterAsObject(2, criteria);
        KNI_GetObjectClass(criteria, class_obj);
        KNI_SetBooleanField(criteria, 
            locationProviderInfoFieldID.incurCost, 
            provider_info.incurCost);
        KNI_SetBooleanField(criteria, 
            locationProviderInfoFieldID.canReportAltitude, 
            provider_info.canReportAltitude);
        KNI_SetBooleanField(criteria, 
            locationProviderInfoFieldID.canReportAddressInfo, 
            provider_info.canReportAddressInfo);
        KNI_SetBooleanField(criteria, 
            locationProviderInfoFieldID.canReportSpeedCource, 
            provider_info.canReportSpeedCource);
        KNI_SetIntField(criteria, 
            locationProviderInfoFieldID.powerConsumption, 
            provider_info.powerConsumption);
        KNI_SetIntField(criteria, 
            locationProviderInfoFieldID.horizontalAccuracy, 
            provider_info.horizontalAccuracy);
        KNI_SetIntField(criteria, 
            locationProviderInfoFieldID.verticalAccuracy, 
            provider_info.verticalAccuracy);
        KNI_SetIntField(criteria, 
            locationProviderInfoFieldID.defaultTimeout, 
            provider_info.defaultTimeout);
        KNI_SetIntField(criteria, 
            locationProviderInfoFieldID.defaultMaxAge, 
            provider_info.defaultMaxAge);
        KNI_SetIntField(criteria, 
            locationProviderInfoFieldID.defaultInterval, 
            provider_info.defaultInterval);
        KNI_SetIntField(criteria, 
            locationProviderInfoFieldID.averageResponseTime, 
            provider_info.averageResponseTime);
        KNI_SetIntField(criteria, 
            locationProviderInfoFieldID.defaultStateInterval, 
            provider_info.defaultStateInterval);

        ret = KNI_TRUE;
    } else if (res == JSR179_STATUSCODE_INVALID_ARGUMENT) {
        /* wrong provider name */
        KNI_ThrowNew(midpIllegalArgumentException, "wrong provider name");
    }

    RELEASE_PCSL_STRING_PARAMETER
    KNI_EndHandles();
    KNI_ReturnBoolean(ret);
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_BOOLEAN
    Java_com_sun_j2me_location_PlatformLocationProvider_waitForNewLocation() {

    jboolean ret = KNI_FALSE;
    MidpReentryData *info = NULL;
    ProviderInfo *pInfo = NULL;
    jsr179_result res;
    jint provider;
    jlong timeout;
    KNI_StartHandles(1);
    provider = KNI_GetParameterAsInt(1);
    timeout = KNI_GetParameterAsLong(2);
    pInfo = getProviderInfo(provider);
    if(pInfo != NULL) {
        info = (MidpReentryData*)SNI_GetReentryData(NULL);
        if (info == NULL) {
            /* First call -request */
            if(pInfo->locked == KNI_TRUE) {
                lock_thread(JSR179_EVENT_UPDATE_ONCE, provider);
            } else {
                /* request new location */
                res = jsr179_update_set((jsr179_handle)provider, timeout);
                switch (res) {
                    case JSR179_STATUSCODE_WOULD_BLOCK:
                        /* wait for javanotify */
                        pInfo->locked = KNI_TRUE;
                        lock_thread(JSR179_EVENT_UPDATE_ONCE, provider);
                        break;
                    case JSR179_STATUSCODE_OK:
                        /* location updated successfully */
                        pInfo->locked = KNI_FALSE;
			            pInfo->newLocationAvailable = KNI_TRUE;
                        ret = KNI_TRUE;
                        break;
                    case JSR179_STATUSCODE_FAIL:
                        /* fail */
                        pInfo->locked = KNI_FALSE;
                        /* wrong provider name */
                        KNI_ThrowNew(midpIllegalArgumentException, 
                                    "wrong provider");
                        break;
                    default:
                        /* fail */
                        pInfo->locked = KNI_FALSE;
                        break;
                }
            }
        } else {
            /* Response */
            if (info->status == JSR179_STATUSCODE_OK) {
                /* location updated successfully */
				pInfo->newLocationAvailable = KNI_TRUE;
                ret = KNI_TRUE;
            } else {
                /* location updated failed */
                ret = KNI_FALSE;
            }
            pInfo->locked = KNI_FALSE;
        }
    }
    KNI_EndHandles();
    KNI_ReturnBoolean(ret);
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_BOOLEAN
    Java_com_sun_j2me_location_PlatformLocationProvider_receiveNewLocationImpl() {

    ProviderInfo *pInfo = NULL;
    jlong timestamp = 0;
    jboolean ret = KNI_FALSE;
    jint provider;

    provider = KNI_GetParameterAsInt(1);
    timestamp = KNI_GetParameterAsLong(2);
    pInfo = getProviderInfo(provider);
    if(pInfo != NULL) {
		if (pInfo->newLocationAvailable == KNI_TRUE) {
			if(jsr179_location_get((jsr179_handle)provider, &pInfo->lastLocation) ==
					JSR179_STATUSCODE_OK) {
				/* get addressInfo if it is present */
				if (pInfo->lastLocation.addressInfoFieldNumber > 0) {
					if(jsr179_get_addressinfo((jsr179_handle)provider,
						&pInfo->lastLocation.addressInfoFieldNumber, 
						pInfo->lastAddressInfo) != JSR179_STATUSCODE_OK) {
						/* drop Address Info for this location */
						pInfo->lastLocation.addressInfoFieldNumber = 0;
					}
				}

				/* get extraInfo if it is present */
				if (pInfo->lastLocation.extraInfoSize > 0) {
					if (jsr179_get_extrainfo((jsr179_handle)provider, 
								pInfo->lastLocation.extraInfoSize, 
								pInfo->lastExtraInfo,
                                pInfo->otherExtraInfoMimeType) != JSR179_STATUSCODE_OK) {
						pInfo->lastLocation.extraInfoSize = 0;
					}
				}
				
				pInfo->lastLocationTimestamp = timestamp;
				pInfo->newLocationAvailable = KNI_FALSE;

				lastUpdatedProvider = provider;

		        ret = KNI_TRUE;
			}
		} else {
			ret = KNI_TRUE;
		}
    }

    KNI_ReturnBoolean(ret);
}

static jboolean getLocation(jobject locationInfo, jobject string_obj, ProviderInfo *pInfo) {
    jint i;
    jfieldID fid;
    jboolean ret = KNI_FALSE;

    if(pInfo != NULL) {
		if (pInfo->lastLocationTimestamp != 0) {
			/* Get location parameters */
			KNI_SetBooleanField(locationInfo,   
				locationInfoFieldID.isValid,    
				pInfo->lastLocation.isValidCoordinate);
			KNI_SetDoubleField(locationInfo,    
				locationInfoFieldID.latitude,   
				pInfo->lastLocation.latitude);
			KNI_SetDoubleField(locationInfo,    
				locationInfoFieldID.longitude,  
				pInfo->lastLocation.longitude);
			KNI_SetFloatField(locationInfo,     
				locationInfoFieldID.altitude,   
				pInfo->lastLocation.altitude);
			KNI_SetFloatField(locationInfo,     
				locationInfoFieldID.horizontalAccuracy, 
				pInfo->lastLocation.horizontalAccuracy);
			KNI_SetFloatField(locationInfo,     
				locationInfoFieldID.verticalAccuracy, 
				pInfo->lastLocation.verticalAccuracy);
			KNI_SetFloatField(locationInfo,     
				locationInfoFieldID.speed,      
				pInfo->lastLocation.speed);
			KNI_SetFloatField(locationInfo,     
				locationInfoFieldID.course,     
				pInfo->lastLocation.course);
			KNI_SetIntField(locationInfo,       
				locationInfoFieldID.method,     
				pInfo->lastLocation.method);
			KNI_SetLongField(locationInfo,      
				locationInfoFieldID.timestamp,  
				pInfo->lastLocationTimestamp);
			if (pInfo->lastLocation.addressInfoFieldNumber == 0) {
				KNI_SetBooleanField(locationInfo,   
					locationInfoFieldID.isAddressInfo, KNI_FALSE);
			} else {
				KNI_SetBooleanField(locationInfo,   
					locationInfoFieldID.isAddressInfo, KNI_TRUE);
			
				for(i=0; i<pInfo->lastLocation.addressInfoFieldNumber; i++) {
			        midp_jstring_from_pcsl_string(&pInfo->lastAddressInfo[i].data, string_obj);

					switch(pInfo->lastAddressInfo[i].fieldId) {
						case JSR179_ADDRESSINFO_EXTENSION:
							fid = locationInfoFieldID.
									AddressInfo_EXTENSION;
							break;
						case JSR179_ADDRESSINFO_STREET:
							fid = locationInfoFieldID.
									AddressInfo_STREET;
							break;
						case JSR179_ADDRESSINFO_POSTAL_CODE:
							fid = locationInfoFieldID.
									AddressInfo_POSTAL_CODE;
							break;
						case JSR179_ADDRESSINFO_CITY:
							fid = locationInfoFieldID.
									AddressInfo_CITY;
							break;
						case JSR179_ADDRESSINFO_COUNTY:
							fid = locationInfoFieldID.
									AddressInfo_COUNTY;
							break;
						case JSR179_ADDRESSINFO_STATE:
							fid = locationInfoFieldID.
									AddressInfo_STATE;
							break;
						case JSR179_ADDRESSINFO_COUNTRY:
							fid = locationInfoFieldID.
									AddressInfo_COUNTRY;
							break;
						case JSR179_ADDRESSINFO_COUNTRY_CODE:
							fid = locationInfoFieldID.
									AddressInfo_COUNTRY_CODE;
							break;
						case JSR179_ADDRESSINFO_DISTRICT:
							fid = locationInfoFieldID.
									AddressInfo_DISTRICT;
							break;
						case JSR179_ADDRESSINFO_BUILDING_NAME:
							fid = locationInfoFieldID.
									AddressInfo_BUILDING_NAME;
							break;
						case JSR179_ADDRESSINFO_BUILDING_FLOOR:
							fid = locationInfoFieldID.
									AddressInfo_BUILDING_FLOOR;
							break;
						case JSR179_ADDRESSINFO_BUILDING_ROOM:
							fid = locationInfoFieldID.
									AddressInfo_BUILDING_ROOM;
							break;
						case JSR179_ADDRESSINFO_BUILDING_ZONE:
							fid = locationInfoFieldID.
									AddressInfo_BUILDING_ZONE;
							break;
						case JSR179_ADDRESSINFO_CROSSING1:
							fid = locationInfoFieldID.
									AddressInfo_CROSSING1;
							break;
						case JSR179_ADDRESSINFO_CROSSING2:
							fid = locationInfoFieldID.
									AddressInfo_CROSSING2;
							break;
						case JSR179_ADDRESSINFO_URL:
							fid = locationInfoFieldID.
									AddressInfo_URL;
							break;
						case JSR179_ADDRESSINFO_PHONE_NUMBER:
							fid = locationInfoFieldID.
									AddressInfo_PHONE_NUMBER;
							break;
						default:
							fid = 0;
							break;
					}
					if(fid != 0) {
						KNI_SetObjectField(locationInfo, fid, 
							string_obj);
					}
				}
			}			
			if (pInfo->lastLocation.extraInfoSize > 0) {
		        midp_jstring_from_pcsl_string(&pInfo->lastExtraInfo[0], string_obj);
				KNI_SetObjectField(locationInfo, locationInfoFieldID.extraInfoNMEA, 
					string_obj);
		        midp_jstring_from_pcsl_string(&pInfo->lastExtraInfo[1], string_obj);
				KNI_SetObjectField(locationInfo, locationInfoFieldID.extraInfoLIF, 
					string_obj);
		        midp_jstring_from_pcsl_string(&pInfo->lastExtraInfo[2], string_obj);
				KNI_SetObjectField(locationInfo, locationInfoFieldID.extraInfoPlain, 
					string_obj);
		        midp_jstring_from_pcsl_string(&pInfo->lastExtraInfo[3], string_obj);
				KNI_SetObjectField(locationInfo, locationInfoFieldID.extraInfoOther, 
					string_obj);
		        midp_jstring_from_pcsl_string(&pInfo->otherExtraInfoMimeType, string_obj);
				KNI_SetObjectField(locationInfo, locationInfoFieldID.extraInfoOtherMIMEType, 
					string_obj);


			}
			ret = KNI_TRUE;
        }
    }
    return ret;
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_INT
    Java_com_sun_j2me_location_PlatformLocationProvider_getLastLocationImpl() {

    jint provider;
    jboolean ret;
    
    KNI_StartHandles(2);
    KNI_DeclareHandle(string_obj);
    KNI_DeclareHandle(locationInfo);

    provider = KNI_GetParameterAsInt(1);
    KNI_GetParameterAsObject(2, locationInfo);

    ret = getLocation(locationInfo, string_obj, getProviderInfo(provider));    

    KNI_EndHandles();
    KNI_ReturnBoolean(ret);
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_INT
    Java_com_sun_j2me_location_PlatformLocationProvider_getLastKnownLocationImpl() {

    jboolean ret = KNI_FALSE;
    
    KNI_StartHandles(2);
    KNI_DeclareHandle(string_obj);
    KNI_DeclareHandle(locationInfo);

    KNI_GetParameterAsObject(1, locationInfo);

    ret = getLocation(locationInfo, string_obj, getProviderInfo(lastUpdatedProvider));    

    KNI_EndHandles();
    KNI_ReturnBoolean(ret);
}

/* JAVADOC COMMENT ELIDED */
typedef struct {
    jboolean filled;
    jint available;
    jint temporarilyUnavailable;
    jint outOfService;
} ProviderStateValue;
static ProviderStateValue stateValue = {KNI_FALSE, 0, 0, 0};

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_INT
    Java_com_sun_j2me_location_PlatformLocationProvider_getStateImpl() {

    jsr179_state state = JSR179_OUT_OF_SERVICE;
    jint provider = KNI_GetParameterAsInt(1);
    jint ret=0; /* out of service */
    
    if (stateValue.filled == KNI_FALSE) {

        KNI_StartHandles(1);
        KNI_DeclareHandle(clazz);
    
        KNI_FindClass("javax/microedition/location/LocationProvider", clazz);
        if(!KNI_IsNullHandle(clazz)) {
            stateValue.available = KNI_GetStaticIntField(clazz, 
                KNI_GetStaticFieldID(clazz, "AVAILABLE", "I"));
            stateValue.temporarilyUnavailable = KNI_GetStaticIntField(clazz, 
                KNI_GetStaticFieldID(clazz, "TEMPORARILY_UNAVAILABLE", "I"));
            stateValue.outOfService = KNI_GetStaticIntField(clazz, 
                KNI_GetStaticFieldID(clazz, "OUT_OF_SERVICE", "I"));
            stateValue.filled = KNI_TRUE;
        }
        KNI_EndHandles();

    }        

    if (stateValue.filled == KNI_TRUE) {
        jsr179_provider_state((jsr179_handle)provider, &state);
        switch(state) {
            case JSR179_AVAILABLE:
                ret = stateValue.available;
                break;
            case JSR179_TEMPORARILY_UNAVAILABLE:
                ret = stateValue.temporarilyUnavailable;
                break;
            case JSR179_OUT_OF_SERVICE:
            default:
                ret = stateValue.outOfService;
                break;
        }
    }

    KNI_ReturnInt(ret);
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_VOID
    Java_com_sun_j2me_location_PlatformLocationProvider_resetImpl() {

    MidpReentryData *info = NULL;
    ProviderInfo *pInfo = NULL;
    jint provider = KNI_GetParameterAsInt(1);
    jsr179_result res;

    info = (MidpReentryData*)SNI_GetReentryData(NULL);
    if (info == NULL) {
        /* reset provider */
        res = jsr179_update_cancel((jsr179_handle)provider);
        switch (res) {
            case JSR179_STATUSCODE_OK:
            case JSR179_STATUSCODE_FAIL:
                break;
            case JSR179_STATUSCODE_INVALID_ARGUMENT:
                /* wrong provider name */
                KNI_ThrowNew(midpIllegalArgumentException, "wrong provider");
                break;
            case JSR179_STATUSCODE_WOULD_BLOCK:
                /* wait for javanotify */
                pInfo = getProviderInfo(provider);
                if(pInfo != NULL) {
                    pInfo->locked = KNI_FALSE;
                }
                lock_thread(JSR179_EVENT_UPDATE_ONCE, provider);
                break;
            default:
                break;
        }
    }

    KNI_ReturnVoid();
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_VOID
    Java_com_sun_j2me_location_PlatformLocationProvider_finalize() {

    jint provider;

    KNI_StartHandles(2);
    KNI_DeclareHandle(this_obj);
    KNI_DeclareHandle(class_obj);

    KNI_GetThisPointer(this_obj);
    KNI_GetObjectClass(this_obj, class_obj);

    provider = KNI_GetIntField(this_obj, 
                    KNI_GetFieldID(class_obj, "provider", "I"));

    if (unregister_provider(provider) == KNI_TRUE) {
        jsr179_provider_close((jsr179_handle)provider);
    }

    KNI_EndHandles();
    KNI_ReturnVoid();
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_VOID
    Java_com_sun_j2me_location_LocationProviderInfo_initNativeClass() {

    KNI_StartHandles(1);
    KNI_DeclareHandle(class_obj);

    KNI_GetClassPointer(class_obj);
    if(!KNI_IsNullHandle(class_obj)) {
        locationProviderInfoFieldID.incurCost = 
            KNI_GetFieldID(class_obj,"incurCost","Z");
        locationProviderInfoFieldID.canReportAltitude = 
            KNI_GetFieldID(class_obj,"canReportAltitude","Z");
        locationProviderInfoFieldID.canReportAddressInfo = 
            KNI_GetFieldID(class_obj,"canReportAddressInfo","Z");
        locationProviderInfoFieldID.canReportSpeedCource = 
            KNI_GetFieldID(class_obj,"canReportSpeedCource","Z");

        locationProviderInfoFieldID.powerConsumption = 
            KNI_GetFieldID(class_obj,"powerConsumption","I");
        locationProviderInfoFieldID.horizontalAccuracy = 
            KNI_GetFieldID(class_obj,"horizontalAccuracy","I");
        locationProviderInfoFieldID.verticalAccuracy = 
            KNI_GetFieldID(class_obj,"verticalAccuracy","I");
        locationProviderInfoFieldID.defaultTimeout = 
            KNI_GetFieldID(class_obj,"defaultTimeout","I");
        locationProviderInfoFieldID.defaultMaxAge = 
            KNI_GetFieldID(class_obj,"defaultMaxAge","I");
        locationProviderInfoFieldID.defaultInterval = 
            KNI_GetFieldID(class_obj,"defaultInterval","I");
        locationProviderInfoFieldID.averageResponseTime = 
            KNI_GetFieldID(class_obj,"averageResponseTime","I");
        locationProviderInfoFieldID.defaultStateInterval = 
            KNI_GetFieldID(class_obj,"defaultStateInterval","I");
    } else {
        KNI_ThrowNew(midpNullPointerException, NULL);
    }

    KNI_EndHandles();
    KNI_ReturnVoid();
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_VOID
    Java_com_sun_j2me_location_LocationInfo_initNativeClass() {

    KNI_StartHandles(1);
    KNI_DeclareHandle(class_obj);

    KNI_GetClassPointer(class_obj);
    if(!KNI_IsNullHandle(class_obj)) {
        locationInfoFieldID.isValid = 
            KNI_GetFieldID(class_obj, "isValid", "Z");
        locationInfoFieldID.timestamp = 
            KNI_GetFieldID(class_obj, "timestamp", "J");
        locationInfoFieldID.latitude = 
            KNI_GetFieldID(class_obj, "latitude", "D");
        locationInfoFieldID.longitude = 
            KNI_GetFieldID(class_obj, "longitude", "D");
        locationInfoFieldID.altitude = 
            KNI_GetFieldID(class_obj, "altitude", "F");
        locationInfoFieldID.horizontalAccuracy = 
            KNI_GetFieldID(class_obj, "horizontalAccuracy", "F");
        locationInfoFieldID.verticalAccuracy = 
            KNI_GetFieldID(class_obj, "verticalAccuracy", "F");
        locationInfoFieldID.speed = 
            KNI_GetFieldID(class_obj, "speed", "F");
        locationInfoFieldID.course = 
            KNI_GetFieldID(class_obj, "course", "F");
        locationInfoFieldID.method = 
            KNI_GetFieldID(class_obj, "method", "I");
        locationInfoFieldID.isAddressInfo = 
            KNI_GetFieldID(class_obj, "isAddressInfo", "Z");
        locationInfoFieldID.AddressInfo_EXTENSION = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_EXTENSION", "Ljava/lang/String;");
        locationInfoFieldID.AddressInfo_STREET = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_STREET", "Ljava/lang/String;");
        locationInfoFieldID.AddressInfo_POSTAL_CODE = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_POSTAL_CODE", "Ljava/lang/String;");
        locationInfoFieldID.AddressInfo_CITY = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_CITY", "Ljava/lang/String;");
        locationInfoFieldID.AddressInfo_COUNTY = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_COUNTY", "Ljava/lang/String;");
        locationInfoFieldID.AddressInfo_STATE = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_STATE", "Ljava/lang/String;");
        locationInfoFieldID.AddressInfo_COUNTRY = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_COUNTRY", "Ljava/lang/String;");
        locationInfoFieldID.AddressInfo_COUNTRY_CODE = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_COUNTRY_CODE", "Ljava/lang/String;");
        locationInfoFieldID.AddressInfo_DISTRICT = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_DISTRICT", "Ljava/lang/String;");
        locationInfoFieldID.AddressInfo_BUILDING_NAME = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_BUILDING_NAME", "Ljava/lang/String;");
        locationInfoFieldID.AddressInfo_BUILDING_FLOOR = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_BUILDING_FLOOR", "Ljava/lang/String;");
        locationInfoFieldID.AddressInfo_BUILDING_ROOM = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_BUILDING_ROOM", "Ljava/lang/String;");
        locationInfoFieldID.AddressInfo_BUILDING_ZONE = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_BUILDING_ZONE", "Ljava/lang/String;");
        locationInfoFieldID.AddressInfo_CROSSING1 = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_CROSSING1", "Ljava/lang/String;");
        locationInfoFieldID.AddressInfo_CROSSING2 = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_CROSSING2", "Ljava/lang/String;");
        locationInfoFieldID.AddressInfo_URL = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_URL", "Ljava/lang/String;");
        locationInfoFieldID.AddressInfo_PHONE_NUMBER = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_PHONE_NUMBER", "Ljava/lang/String;");
        locationInfoFieldID.extraInfoNMEA = 
            KNI_GetFieldID(class_obj, "extraInfoNMEA", "Ljava/lang/String;");
        locationInfoFieldID.extraInfoLIF = 
            KNI_GetFieldID(class_obj, "extraInfoLIF", "Ljava/lang/String;");
        locationInfoFieldID.extraInfoPlain = 
            KNI_GetFieldID(class_obj, "extraInfoPlain", "Ljava/lang/String;");
        locationInfoFieldID.extraInfoOther = 
            KNI_GetFieldID(class_obj, "extraInfoOther", "Ljava/lang/String;");
        locationInfoFieldID.extraInfoOtherMIMEType = 
            KNI_GetFieldID(class_obj, "extraInfoOtherMIMEType", "Ljava/lang/String;");
    } else {
        KNI_ThrowNew(midpNullPointerException, NULL);
    }

    KNI_EndHandles();
    KNI_ReturnVoid();
}

/* Helper-functions */

/* JAVADOC COMMENT ELIDED */
static void lock_thread(jint waitingFor, jint provider) {
    (void)waitingFor;
    midp_thread_wait(JSR179_LOCATION_SIGNAL, provider, NULL);
}

/* JAVADOC COMMENT ELIDED */
static ProviderInfo* register_provider(jint provider_id) {
    jint i;
    /* try to find provider in the database */
    for(i=0; i<numOpenedProviders; i++) {
        if(providerInfo[i].id == provider_id) {
            /* provider already registered */
            providerInfo[i].num_instances++;
            return &providerInfo[i];
        }
    }
    if(numOpenedProviders < MAX_PROVIDERS) {
        /* new provider */
        providerInfo[numOpenedProviders].id = provider_id;
        providerInfo[numOpenedProviders].locked = KNI_FALSE;
        providerInfo[numOpenedProviders].num_instances = 1;
        providerInfo[numOpenedProviders].newLocationAvailable = KNI_FALSE;
        providerInfo[numOpenedProviders].lastLocationTimestamp = 0;
		/* Initialize lastLocation data */
		providerInfo[numOpenedProviders].lastLocation.addressInfoFieldNumber = 0;
		for (i=0; i<JSR179_MAX_ADDRESSINFO_FIELDS; i++) {
			providerInfo[numOpenedProviders].lastAddressInfo[i].data = PCSL_STRING_NULL;
		}
		for (i=0; i<JSR179_MAX_EXTRAINFO_TYPES; i++) {
			providerInfo[numOpenedProviders].lastExtraInfo[i] = PCSL_STRING_NULL;
		}
        providerInfo[numOpenedProviders].otherExtraInfoMimeType = PCSL_STRING_NULL;

        return &providerInfo[numOpenedProviders++];
    }
    return NULL;
}

/* JAVADOC COMMENT ELIDED */
static jboolean unregister_provider(jint provider_id) {
    jint i, j, k;
    /* try to find provider in the database */
    for(i=0; i<numOpenedProviders; i++) {
        if(providerInfo[i].id == provider_id) {
            /* provider found */
            if(providerInfo[i].num_instances > 1) {
                /* it is not last instance of the provider */
                /* can not close provider */
                providerInfo[i].num_instances--;
                return KNI_FALSE;
            } else {
				/* Clear lastLocation data */
				providerInfo[i].lastLocation.addressInfoFieldNumber = 0;
				for (j=0; j<JSR179_MAX_ADDRESSINFO_FIELDS; j++) {
					pcsl_string_free(&providerInfo[i].lastAddressInfo[j].data);
				}
				for (j=0; j<JSR179_MAX_EXTRAINFO_TYPES; j++) {
					pcsl_string_free(&providerInfo[i].lastExtraInfo[j]);
				}
				pcsl_string_free(&providerInfo[i].otherExtraInfoMimeType);
                
                /* last instance of the provider */
                /* remove provider from the database */
                numOpenedProviders--;
                for(j=i; j<numOpenedProviders; j++) {
                    providerInfo[j].locked = providerInfo[j+1].locked;
                    providerInfo[j].num_instances = 
                        providerInfo[j+1].num_instances;
                    providerInfo[j].id = providerInfo[j+1].id;
					providerInfo[j].lastLocationTimestamp = providerInfo[j+1].lastLocationTimestamp;
                    providerInfo[j].lastLocation = providerInfo[j+1].lastLocation;
					for (k=0; k<JSR179_MAX_ADDRESSINFO_FIELDS; k++) {
						providerInfo[j].lastAddressInfo[k] = providerInfo[j+1].lastAddressInfo[k];
					}
					for (k=0; k<JSR179_MAX_EXTRAINFO_TYPES; k++) {
						providerInfo[j].lastExtraInfo[k] = providerInfo[j+1].lastExtraInfo[k];
					}
                    providerInfo[j].otherExtraInfoMimeType = providerInfo[j+1].otherExtraInfoMimeType;
                }
                return KNI_TRUE;
            }
        }
    }
    /* did not find provider */
    return KNI_FALSE;
}

/* JAVADOC COMMENT ELIDED */
static ProviderInfo* getProviderInfo(jint provider_id) {
    jint i;
    for(i=0; i<numOpenedProviders; i++) {
        if(providerInfo[i].id == provider_id) {
            return &providerInfo[i];
        }
    }
    return NULL;
}
