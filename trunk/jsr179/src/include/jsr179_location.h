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

#ifndef __JSR179_LOCATION_H
#define __JSR179_LOCATION_H
 
#ifdef __cplusplus
extern "C" {
#endif


/**
 * @defgroup JSR179Location JSR179 Location
 *
 * This API provide porting layer APIs to obtaine information about the present
 * geographic location and orientation of the terminal and access a database of
 * known landmarks stored in the terminal.
 *
 * <b> Acquisition of present geographic location of the terminal</b>
 *
 * To calculate distance/azimuth, the platform independent atan2 
 * function implementation will be provided. However, 
 * if the target platform provides atan2 functionality 
 * by means of a native math library, it will be more efficient to utilize it.
 *
 * The acquired location should include following information:
 *  - Only Latitude and Longitude are mandatory.
 *  - Altitude - Float.NAN if unknown    
 *  - HorizontalAccuracy and VerticalAccuracy in meters - Float.NaN if unknown
 *  - LocationMethod - zero if unknown
 *  - Speed, Course - Float.NAN if unknown
 *  - AddressInfo - provided if supported.
 *  - ExtraInfo - provided if supported.
 *
 * For the specified location provider, the following infomation should be provided:
 *  - Default time related properties of a location provider
 *  - The default interval - how often updates for listener.
 *  - The default maximum age - acceptable age for cached info for listener
 *  - The default timeout  - how late update can be from interval for listener
 *  - The average time of receiving next location
 *  - The recommended time interval for asking a new state
 *  - The other properties for Critera.
 * 
 * Optional feature whose availability depends on the used location methods.
 *  - provide altitude information
 *  - provide accuracy of altitude
 *  - provide course and speed information
 *  - provide textual address information related to the location.
 *  - provide proximity monitoring.
 *
 * The porting layer includes:
 *  - Opens a location provider and gets the information   
 * 	  - jsr179_provider_open
 *    - jsr179_provider_close
 *    - jsr179_provider_getinfo
 *    - jsr179_notify_location_event
 *  - Gets the current Status of a location provider.
 *    - jsr179_provider_state
 *  - Gets the location info on request basis.
 *    - jsr179_update_set
 *    - jsr179_update_cancel
 *  - Gets the proximity events - Optional
 *    - jsr179_proximity_monitoring_add
 *    - jsr179_proximity_monitoring_cancel
 *    - jsr179_notify_location_proximity
 *
 * <b> Acquisition of orientation of the terminal</b>
 *
 * The following features are optionally supported:
 *  - compass azimuth of the terminal orientation
 *  - pitch and roll 3D terminal orientation information
 * If the implementation chooses to support orientation, it must 
 * provide the azimuth information. Providing pitch and roll is optional.
 *
 * The porting layer includes:
 *  - Opens and closes a orientation provider
 *    - jsr179_provider_open
 *    - jsr179_provider_close
 *  - Gets a orientation information
 *    - jsr179_orientation_start
 *    - jsr179_orientation_finish
 *    - jsr179_notify_location_event
 *
 * <b> Accessing a database of known landmarks of the terminal</b>
 *
 * You can use our internal implementation of landmark database because the 
 * JSR 179 specification only requires that a landmark database is shared among
 * Java platform applications.  In this case, all landmark database APIs could
 * be optional. If the device provides a landmark database used by native application, 
 * the implementation is encouraged to use that database. 
 *
 * Optional features whose availability depends on the landmark store 
 * implementation of the terminal and it's possible relation to landmark 
 * stores shared with native applications:
 *  - creating and deleting landmark stores
 *  - adding and removing landmark categories
 *
 * The porting layer includes:
 *  - Gets landmark Stores
 *    - jsr179_landmarkstorelist_open
 *    - jsr179_landmarkstorelist_close
 *    - jsr179_landmarkstorelist_next
 *  - Gets landmarks from native landmark store
 *    - jsr179_landmarklist_open
 *    - jsr179_landmarklist_close
 *    - jsr179_landmarklist_next
 *  - Gets categories from native landmark store
 *    - jsr179_categorylist_open
 *    - jsr179_categorylist_close
 *    - jsr179_categorylist_next
 *  - Adds/deletes a landmark to landmark store or category
 *    - jsr179_landmark_add_to_landmarkstore
 *    - jsr179_landmark_add_to_category
 *    - jsr179_landmark_delete_from_landmarkstore
 *    - jsr179_landmark_delete_from_category
 *  - Creates/deletes a landmark store or category - Optional
 *    - jsr179_landmarkstore_create
 *    - jsr179_landmarkstore_delete
 *    - jsr179_category_add
 *    - jsr179_category_delete
 *
 * <b> Implementation Notes </b>
 *
 *  - asynchronous operation
 *
 * In the asynchronous operation, the operation returns quickly with 
 * JSR179_STATUSCODE_WOULD_BLOCK. When the operation has been completed, an
 * event is required to be sent from Platform. See section on event for related
 * information.
 *
 * The asynchronous functions are
 *       opening location provider,
 *       orientation update,
 *       location update once,
 *       proximity update and
 *
 *  - buffer allocation
 *
 * The buffer for output parameter should be allocated by the caller. 
 * There should be a parameter for the size of the buffer if there is 
 * no predefined maximum size. 
 * \par
 */

/**
 * @defgroup JSR179MandatoryLocation MANDATORY misc JSR179 location API
 * @ingroup JSR179Location
 * @{
 */

/** Maximum length of a landmark name */
#define JSR179_MAX_LANDMARK_NAME         (32 +1)
/** Maximum length of a category name */
#define JSR179_MAX_CATEGORY_NAME         (32 +1)
/** Maximum length of a landmark description */
#define JSR179_MAX_LANDMARK_DESCRIPTION  (256 +1)
/** Maximum size of an addressinfo field */
#define JSR179_MAX_ADDRESSINFO_FIELD     (32 +1)
/** Maximum length of a list of providers */
#define JSR179_MAX_PROPERTY_LENGTH       (64 +1)

/** Maximum number of an addressinfo fields */
#define JSR179_MAX_ADDRESSINFO_FIELDS    (17)
/** Maximum number of extra info mimetypes */
#define JSR179_MAX_EXTRAINFO_TYPES    (4)

/**
 * Property separator. 
 * Shall be used in the jsr179_property_get to sparate providers.
 */
#define JSR179_PROPERTY_SEPARATOR ','

/**
 * @def JSR179_SIZE_OF_LANDMARK_INFO( NumberOfAddressInfoFields )
 * 
 * Calculates the size of a landmark information considering the number 
 * of address info fields.
 * This macro should be used instead of sizeof(jsr179_landmark) 
 * because the structure size will be varing with the number of address 
 * fields.
 */
#define JSR179_SIZE_OF_LANDMARK_INFO(NumberOfAddressInfoFields) ( (int)(&((jsr179_landmark*)(NULL))->fields) + sizeof(jsr179_addressinfo_fieldinfo)*NumberOfAddressInfoFields )

/**
 * @enum jsr179_property
 * Property ids
 */
typedef enum {
    /** location provider name list */
    JSR179_PROVIDER_LIST               = 0, 
    /** orientation device name list */
    JSR179_ORIENTATION_LIST            = 1, 
    /** landmarkstores name list */
    JSR179_LANDMARKSTORE_LIST          = 2, 
} jsr179_property;

/**
 * @enum jsr179_power_consumption
 * Power consumption level
 */
typedef enum {
    /** Constant indicating no requirements for the parameter. */
    JSR179_NO_IDEA               = 0,
    /** Level indicating only low power consumption allowed. */
    JSR179_USAGE_LOW             = 1,
    /** Level indicating average power consumption allowed. */
    JSR179_USAGE_MEDIUM          = 2,
    /** Level indicating high power consumption allowed. */
    JSR179_USAGE_HIGH            = 3
} jsr179_power_consumption;

/**
 * @enum jsr179_state
 * State of a location provider
 */
typedef enum {
    /** Availability status code: the location provider is available. */
    JSR179_AVAILABLE               = 0,
    /** Availability status code: the location provider is out of service. */
    JSR179_OUT_OF_SERVICE          = 1,
    /** Availability status code: the location provider is temporarily unavailable. */
    JSR179_TEMPORARILY_UNAVAILABLE = 2,
    /** Availability status code: service not allowed for location provider */
    JSR179_SERVICE_NOT_ALLOWED     = 3
} jsr179_state;

/**
 * @enum jsr179_extrainfo_mimetype
 * MIME type id of extra information
 */
typedef enum {
    /** Mime type is "application/X-jsr179-location-nmea" */
    JSR179_EXTRAINFO_NMEA               = 0,
    /** Mime type is "application/X-jsr179-location-lif" */
    JSR179_EXTRAINFO_LIF,
    /** Mime type is "text/plain" */
    JSR179_EXTRAINFO_PLAINTEXT,
    /** Other mime types */
    JSR179_EXTRAINFO_OTHER
} jsr179_extrainfo_mimetype;

/**
 * @enum jsr179_method 
 * location acquisition method
 */
typedef enum {
    /** Location method using satellites. */
    JSR179_MTE_SATELLITE = 0x00000001,
    /** Location method Time Difference for cellular / terrestrial RF system. */
    JSR179_MTE_TIMEDIFFERENCE = 0x00000002,
    /** Location method Time of Arrival (TOA) for cellular / terrestrial RF system. */
    JSR179_MTE_TIMEOFARRIVAL = 0x00000004,
    /** Location method Cell-ID for cellular. */
    JSR179_MTE_CELLID = 0x00000008,
    /** Location method Short-range positioning system (for example, Bluetooth LP). */
    JSR179_MTE_SHORTRANGE = 0x00000010,
    /** Location method Angle of Arrival for cellular / terrestrial RF system. */
    JSR179_MTE_ANGLEOFARRIVAL = 0x00000020,
    /** Location method is of type terminal based. */
    JSR179_MTY_TERMINALBASED = 0x00010000,
    /** Location method is of type network based. */
    JSR179_MTY_NETWORKBASED = 0x00020000,
    /** Location method is assisted by the other party 
        (Terminal assisted for Network based, Network assisted for terminal based). */
    JSR179_MTA_ASSISTED = 0x00040000,
    /** Location method is unassisted. */
    JSR179_MTA_UNASSISTED = 0x00080000,
} jsr179_method;

/**
 * @enum jsr179_addressinfo_field
 * field ids of location's address information 
 */
typedef enum {
    /** Address extension, e.g. flat number. */
    JSR179_ADDRESSINFO_EXTENSION = 1,
    /** Street name and number. */
    JSR179_ADDRESSINFO_STREET = 2,
    /** Zip or postal code. */
    JSR179_ADDRESSINFO_POSTAL_CODE = 3,
    /** Town or city name. */
    JSR179_ADDRESSINFO_CITY = 4,
    /** Country. */
    JSR179_ADDRESSINFO_COUNTY = 5,
    /** State or province. */
    JSR179_ADDRESSINFO_STATE = 6,
    /** Country. */
    JSR179_ADDRESSINFO_COUNTRY = 7,
    /** Country as a two-letter ISO 3166-1 code. */
    JSR179_ADDRESSINFO_COUNTRY_CODE = 8,
    /** Municipal district. */
    JSR179_ADDRESSINFO_DISTRICT = 9,
    /** Building name. */
    JSR179_ADDRESSINFO_BUILDING_NAME = 10,
    /** Building floor. */
    JSR179_ADDRESSINFO_BUILDING_FLOOR = 11,
    /** Building room. */
    JSR179_ADDRESSINFO_BUILDING_ROOM = 12,
    /** Building zone */
    JSR179_ADDRESSINFO_BUILDING_ZONE = 13,
    /** Street in a crossing. */
    JSR179_ADDRESSINFO_CROSSING1 = 14,
    /** Street in a crossing. */
    JSR179_ADDRESSINFO_CROSSING2 = 15,
    /** URL for this place. */
    JSR179_ADDRESSINFO_URL = 16,
    /** Phone number for this place. */
    JSR179_ADDRESSINFO_PHONE_NUMBER = 17
} jsr179_addressinfo_field;

/**
 * @enum jsr179_result
 * result code extention to jsr179_result
 */
typedef enum {
    /** Generic success */    
    JSR179_STATUSCODE_OK = 0,                    
    /** Generic failure */
    JSR179_STATUSCODE_FAIL = -1,                 
    /** Not implemented */
    JSR179_STATUSCODE_NOT_IMPLEMENTED = -2,
    /** Out of memory */
    JSR179_STATUSCODE_OUT_OF_MEMORY = -3,
    /** Invalid argument */
    JSR179_STATUSCODE_INVALID_ARGUMENT = -4,
    /** Would block */
    JSR179_STATUSCODE_WOULD_BLOCK = -5,          
    /** Operation is canceled */
    JSR179_STATUSCODE_CANCELED = -10,  
    /** Operation is timeout */
    JSR179_STATUSCODE_TIMEOUT = -11,
    /** Provider is out of service */
    JSR179_STATUSCODE_OUT_OF_SERVICE = -12,
    /** Provider is temporarily unavailable */
    JSR179_STATUSCODE_TEMPORARILY_UNAVAILABLE = -13 
} jsr179_result;

/** Float Infinity */
#define JSR179_FLOAT_NAN   ((jfloat)(0x7fc00000))

/**
 * @typedef jsr179_handle
 * general handle type
 */
typedef void* jsr179_handle;

/**
 * struct jsr179_provider_info
 * 
 * The information of location provider.
 */
typedef struct {
    /** Preferred cost setting */
    jboolean incurCost;
    /** Possibility to report altitude */
    jboolean canReportAltitude;
    /** Possibility to report AddressInfo */
    jboolean canReportAddressInfo;
    /** Possibility to report speed and course  */
    jboolean canReportSpeedCource;
    /** Preferred power consumption */
    jsr179_power_consumption powerConsumption;
    /** Horizontal Accuracy in meters */
    jint horizontalAccuracy;
    /** Vertical Accuracy in meters */
    jint verticalAccuracy;
    /** The default listener timeout to obtain a new location result in milliseconds. */
    jint defaultTimeout;
    /** The default listener timeout to obtain a new location result in milliseconds. */
    jint defaultMaxAge;
    /** The default listener interval between cyclic location updates in milliseconds. */ 
    jint defaultInterval;
    /** The average response time for obtaining a new location in milliseconds. */
    jint averageResponseTime;
    /** The recommended interval for querying provider's state in milliseconds. */
    jint defaultStateInterval;
} jsr179_provider_info;

/**
 * struct jsr179_addressinfo_fieldinfo
 * Address infomation's field value.
 */
typedef struct {
    /** Address Info fieldIs (see jsr179_addressinfo_field) */
    jsr179_addressinfo_field fieldId;
    /** Address Info data */
    pcsl_string data;
} jsr179_addressinfo_fieldinfo;

/**
 * struct jsr179_location
 * Location info
 */
typedef struct {
    /** whether a valid location with coordinates or an invalid one, invalid may have the extra information for the reason  */ 
    jboolean isValidCoordinate;
    /** latitude in [-90.0, 90,0] */
    jdouble latitude;
    /** longitude in [-180.0, 180,0) */
    jdouble longitude;
    /** defined as height in meters above the WGS84 ellipsoid, JSR179_FLOAT_NAN if unknown */
    jfloat altitude;
    /** in meters(1-sigma standard deviation), JSR179_FLOAT_NAN if unknown */
    jfloat horizontalAccuracy;
    /** in meters(1-sigma standard deviation), JSR179_FLOAT_NAN if unknown */
    jfloat verticalAccuracy;
    /** in meters per second, JSR179_FLOAT_NAN if unknown */
    jfloat speed;
    /** in degree, JSR179_FLOAT_NAN if unknown */
    jfloat course;
    /** bitwise combination of jsr179_method, 0 if unknown */
    jint method;
    /** location timestamp, -1 if unknown */
    jlong timestamp;
    /** 0 if none or the length of the extra infomation string which includes NULL termination */
    jint extraInfoSize;
    /** number of address info fields which have value, zero if none */
    jint addressInfoFieldNumber;
} jsr179_location;


/**
 * struct jsr179_landmark
 * 
 * Landmark info
 */
typedef struct {
    /** name of landmark, minimum 32 characters */
    pcsl_string name;
    /** description of landmark, maximum 256 characters */
    pcsl_string description;
    /** 
     * Flag indicating whether a coordinates are present or not
     */     
    jboolean isValidCoordinate;
    /** latitude in [-90.0, 90,0] */
    jdouble latitude;
    /** longitude in [-180.0, 180,0) */
    jdouble longitude;           
    /** defined as height in meters above the WGS84 ellipsoid, JSR179_FLOAT_NAN if unknown */
    jfloat altitude;
    /** in meters(1-sigma standard deviation), JSR179_FLOAT_NAN if unknown */
    jfloat horizontalAccuracy;
    /** in meters(1-sigma standard deviation), JSR179_FLOAT_NAN if unknown */
    jfloat verticalAccuracy;
    /** number of fields which have value, zero if none */
    jint addressInfoFieldNumber;
    /** array of field value */
    jsr179_addressinfo_fieldinfo fields[1]; 
} jsr179_landmark;

/**
 * struct jsr179_orientation
 * 
 * Orientation info
 */
typedef struct {
    /** Azimuth in degree within [0.0, 360.0) */
    jfloat compassAzimuth;   
    /** Pitch in degree within [-90.0, 90.0], JSR179_FLOAT_NAN if unknown */
    jfloat pitch;
    /** Roll in degree within [-180.0, 180.0), JSR179_FLOAT_NAN if unknown */
    jfloat roll;
    /** If true, the compassAzimuth and pitch are relative to the magnetic field of the Earth. 
        If false, the compassAzimuth is relative to true north and pitch is relative to gravity. */
    jboolean isMagnetic;
} jsr179_orientation;


/**
 * Gets the values of the specified property.
 *
 * If there are more than one items in a property string, the items are separated by comma.
 *
 * The following properties should be provided:
 *  - JSR179_PROVIDER_LIST
 *   The lists of location providers.
 *  - JSR179_ORIENTATION_LIST
 *   The lists of orientation providers. An empty string means that orientation is not supported.
 *
 * @param property id of property
 * @param outPropertyValue string value.
 * @retval JSR179_STATUSCODE_OK      success
 * @retval JSR179_STATUSCODE_FAIL    fail
 */
jsr179_result jsr179_property_get(
        jsr179_property property,
        pcsl_string /*OUT*/*outPropertyValue);

/**
 * Get the information for the given name of location provider.
 *
 * This function only gets information and is intended to return it quickly. 
 *
 * The valid provider name is listed in JSR179_PROVIDER_LIST property.
 * 
 * @param name of the location provider, NULL implies the default location provider
 * @param pInfo  the information of the location provider
 * 
 * @retval JSR179_STATUSCODE_OK      success
 * @retval JSR179_STATUSCODE_INVALID_ARGUMENT  if the given name of location provider is not found.
 * @retval JSR179_STATUSCODE_FAIL    otherwise, fail
 */
jsr179_result jsr179_provider_getinfo(
        const pcsl_string name,
        jsr179_provider_info* /*OUT*/pInfo);

/**
 * Initializes a provider.
 *
 * The name will be the loaction or orientation provider.
 * The name of the location provider is in JSR179_PROVIDER_LIST property. 
 * Orientation device name is in JSR179_ORIENTATION_LIST property. 
 *
 * see jsr179_notify_location_event
 *
 * @param name  of the location provider
 * @param pProvider handle of the location provider
 *
 * @retval JSR179_STATUSCODE_OK      success
 * @retval JSR179_STATUSCODE_WOULD_BLOCK  jsr179_notify_location_event needs to be called to notify completion
 * @retval JSR179_STATUSCODE_INVALID_ARGUMENT  if the specified provider is not found.
 *
 * @retval JSR179_STATUSCODE_FAIL    out of service or other error
 */
jsr179_result jsr179_provider_open(
        const pcsl_string name,
        /*OUT*/ jsr179_handle* pProvider);

/**
 * Closes the opened provider.
 *
 * This function must free all resources allocated for the specified provider.
 *
 * @param provider handle of a provider
 *
 * @retval JSR179_STATUSCODE_OK          success
 * @retval JSR179_STATUSCODE_FAIL        if there was an error
 */
jsr179_result jsr179_provider_close(
        jsr179_handle provider);

/**
 * Gets the status of the location provider.
 * This function only get the current state and is intended to return it quickly. 
 *
 * @param provider handle of the location provider
 * @param pState returns state of the specified provider.
 *
 * @retval JSR179_STATUSCODE_OK          success
 * @retval JSR179_STATUSCODE_FAIL        if there was an error
 */
jsr179_result jsr179_provider_state(
        jsr179_handle provider,
        /*OUT*/ jsr179_state* pState);

/**
 * Requests a location acquisition.
 *
 * This function only requests location update and is intended to return it quickly. 
 * The location update will be get through jsr179_notify_location_event() with JSR179_EVENT_UPDATE_ONCE type.
 * This function will not be called again before jsr179_notify_location_event is called for the previous request completion.
 * If timeout expires before obtaining the location result, jsr179_notify_location_event() will be called with JSR179_STATUSCODE_TIMEOUT reason.
 *
 * see jsr179_notify_location_event
 *
 * @param provider handle of the location provider
 * @param timeout timeout in milliseconds. -1 implies default value.
 *
 * @retval JSR179_STATUSCODE_OK                  success
 * @retval JSR179_STATUSCODE_FAIL                if gets a invalid location or other error
 */
jsr179_result jsr179_update_set(jsr179_handle provider, jlong timeout);

/**
 * Cancels the current location acquisition.
 * 
 * This function will incur calling jsr179_notify_location_event() with JSR179_STATUSCODE_CANCELED reason.
 *
 * see jsr179_notify_location_event
 *
 * @param provider handle of the location provider
 *
 * @retval JSR179_STATUSCODE_OK          success
 * @retval JSR179_STATUSCODE_FAIL        if there is an  error
 */
jsr179_result jsr179_update_cancel(jsr179_handle provider);

/**
 * Gets a location information after location update notification.
 *
 * The location update will be notified through jsr179_notify_location_event() with 
 * JSR179_EVENT_UPDATE_ONCE type. 
 *
 * see jsr179_notify_location_event
 *
 * @param provider handle of the location provider
 * @param pLocationInfo location info
 *
 * @retval JSR179_STATUSCODE_OK                  success
 * @retval JSR179_STATUSCODE_FAIL                if gets a invalid location or other error
 */
jsr179_result jsr179_location_get(jsr179_handle provider, 
        /*OUT*/ jsr179_location* pLocationInfo);

/** @} */
    


/******************************************************************************
 ******************************************************************************
 ******************************************************************************

  NOTIFICATION FUNCTIONS
  - - - -  - - - - - - -  
  The following functions are implemented by Sun.
  Platform is required to invoke these function for each occurrence of the
  undelying event.
  The functions need to be executed in platform's task/thread

 ******************************************************************************
 ******************************************************************************
 ******************************************************************************/
    
/**
 * @defgroup JSR179MiscNotification notification for misc JSR179 location API
 * @ingroup JSR179Location
 * @{
 */


/**
 * @enum jsr179_callback_type
 * Notifications events
 */
typedef enum {
    /** Provider opened event */
    JSR179_EVENT_OPEN_COMPLETED,
    /** Orientation acquired event*/
    JSR179_EVENT_ORIENTATION_COMPLETED,
    /** Location updated event */
    JSR179_EVENT_UPDATE_ONCE,
} jsr179_callback_type;
    
/**
 * A callback function to be called for notification of non-blocking 
 * location related events.
 * The platform will invoke the call back in platform context for
 * each provider related occurrence. 
 *
 * @param event type of indication: Either
 *          - JSR179_EVENT_OPEN_COMPLETED
 *          - JSR179_EVENT_ORIENTATION_COMPLETED
 *          - JSR179_EVENT_UPDATE_ONCE
 * @param provider handle of provider related to the notification
 * @param operation_result operation result: Either
 *      - JSR179_STATUSCODE_OK if operation completed successfully, 
 *      - JSR179_STATUSCODE_CANCELED if operation is canceled 
 *      - JSR179_STATUSCODE_TIMEOUT  if operation is timeout 
 *      - JSR179_STATUSCODE_OUT_OF_SERVICE if provider is out of service
 *      - JSR179_STATUSCODE_TEMPORARILY_UNAVAILABLE if provider is temporarily unavailable
 *      - otherwise, JSR179_STATUSCODE_FAIL
 */
void jsr179_notify_location_event(
        jsr179_callback_type event,
        jsr179_handle provider,
        jsr179_result operation_result);

/**
 * A callback function to be called for notification of proximity monitoring updates.
 *
 * This function will be called only once when the terminal enters the proximity of the registered coordinate. 
 *
 * @param provider handle of provider related to the notification
 * @param latitude of registered coordinate.
 * @param longitude of registered coordinate.
 * @param proximityRadius of registered coordinate.
 * @param pLocationInfo location info
 * @param operation_result operation result: Either
 *      - JSR179_STATUSCODE_OK if operation completed successfully, 
 *      - JSR179_STATUSCODE_CANCELED if operation is canceled 
 *      - JSR179_STATUSCODE_OUT_OF_SERVICE if provider is out of service
 *      - JSR179_STATUSCODE_TEMPORARILY_UNAVAILABLE if provider is temporarily unavailable
 *      - otherwise, JSR179_STATUSCODE_FAIL
 */
void /*OPTIONAL*/jsr179_notify_location_proximity(
        jsr179_handle provider,
        jdouble latitude,
        jdouble longitude,
        jfloat proximityRadius,
        jsr179_location* pLocationInfo,
        jsr179_result operation_result);

/** @} */
    
/******************************************************************************
 ******************************************************************************
 ******************************************************************************
    OPTIONAL FUNCTIONS
 ******************************************************************************
 ******************************************************************************
 ******************************************************************************/
    
/**
 * @defgroup JSR179OptionalLocation OPTIONAL misc JSR179 location API
 * @ingroup JSR179Location
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
 * @retval JSR179_STATUSCODE_OK      success
 * @retval JSR179_STATUSCODE_FAIL    fail
 */
jsr179_result /*OPTIONAL*/ jsr179_get_extrainfo(
        jsr179_handle provider,
        jint maxUnicodeStringBufferLen,
        /*OUT*/pcsl_string outUnicodeStringBuffer[],
        /*OUT*/pcsl_string outOtherMIMETypeBuffer);
    
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
 * @retval JSR179_STATUSCODE_OK      success
 * @retval JSR179_STATUSCODE_FAIL    fail
 */
jsr179_result /*OPTIONAL*/ jsr179_get_addressinfo(
        jsr179_handle provider,
        /*IN and OUT*/ int* pAddresInfoFieldNumber,
        /*OUT*/jsr179_addressinfo_fieldinfo fields[]);

/**
 * Initiates getting orientation
 *
 * see jsr179_notify_location_event
 *
 * @param provider handle of a location provider
 * @param pOrientationInfo orientation info
 * @param pContext address of a pointer variable to receive the context;
 *        this is set only when the function returns JSR179_STATUSCODE_WOULD_BLOCK.
 *
 * @retval JSR179_STATUSCODE_OK                  success
 * @retval JSR179_STATUSCODE_WOULD_BLOCK  jsr179_notify_location_event needs to be called to notify completion. 
 *                                               The type of event will be JSR179_EVENT_ORIENTATION_COMPLETED
 * @retval JSR179_STATUSCODE_FAIL                if gets a invalid orientation or other error
 */
jsr179_result /*OPTIONAL*/ jsr179_orientation_start(
        jsr179_handle provider,
        /*OUT*/ jsr179_orientation* pOrientationInfo,
        /*OUT*/ void **pContext);

/**
 * Finishes a pending orientation operation.
 * 
 * see jsr179_notify_location_event
 *
 * @param provider handle of a location provider
 * @param pOrientationInfo orientation info
 * @param context the context returned by the orientation_start function
 *
 * @retval JSR179_STATUSCODE_OK          success
 * @retval JSR179_STATUSCODE_WOULD_BLOCK if the caller must call the finish function 
 *                                       again to complete the operation
 * @retval JSR179_STATUSCODE_FAIL        if there is an  error
 */
jsr179_result /*OPTIONAL*/ jsr179_orientation_finish(
        jsr179_handle provider,
        /*OUT*/ jsr179_orientation* pOrientationInfo,
        void *context);

/**
 * Adds proximity monitoring for the specified coordinate
 * 
 * This function could monitor more than one coordinate. 
 * jsr179_notify_location_proximity() is called only once when the terminal enters the proximity of the resigered coordinate. 
 * The monitoring state change is reported as well through the operation_result parameter of jsr179_notify_location_proximity(). 
 *
 * @param provider handle of the location provider
 * @param latitude of the location to monitor
 * @param longitude of the location to monitor
 * @param proximityRadius the radius in meters that is considered to be the threshold for being in the proximity of the specified location. ( >= 0 )
 *
 * @retval JSR179_STATUSCODE_OK                  success
 * @retval JSR179_STATUSCODE_FAIL                the already monitored location is provided, or if the platform does not have resources to add a new location to be monitored.
 * @retval JSR179_STATUSCODE_INVALID_ARGUMENT    if latitude or longitude are not within the valid range. if proximityRadius < 0
 * 
 */
jsr179_result /*OPTIONAL*/ jsr179_proximity_monitoring_add(
        jsr179_handle provider,
        jdouble latitude,
        jdouble longitude,
        jfloat proximityRadius);

/**
 * Removes a proximity monitoring for the specified coordinate
 * 
 * @param provider handle of the location provider
 * @param latitude of the location to monitor
 * @param longitude of the location to monitor
 * @param proximityRadius the radius in meters that is considered to be the threshold for being in the proximity of the specified location. ( >= 0 )
 *
 * @retval JSR179_STATUSCODE_OK                 success
 * @retval JSR179_STATUSCODE_FAIL               the other error
 * @retval JSR179_STATUSCODE_INVALID_ARGUMENT   if the given location is not found
 */
jsr179_result /*OPTIONAL*/ jsr179_proximity_monitoring_cancel(
        jsr179_handle provider,
        jdouble latitude,
        jdouble longitude,
        jfloat proximityRadius);

/**
 * Adds a landmark to a landmark store.
 *
 * @param landmarkStoreName where the landmark will be added
 * @param landmark to add
 * @param categoryName where the landmark will belong to, NULL implies that the landmark does not belong to any category.
 * @param outLandmarkID returned id of added landmark
 *
 * @retval JSR179_STATUSCODE_OK                 success
 * @retval JSR179_STATUSCODE_FAIL               on error
 * @retval JSR179_STATUSCODE_INVALID_ARGUMENT   if the category name is invalid or the landmark has a longer name field than the implementation can support.
 */
jsr179_result jsr179_landmark_add_to_landmarkstore(
        const pcsl_string landmarkStoreName, 
        const jsr179_landmark* landmark,
        const pcsl_string categoryName,
        jsr179_handle* /*OUT*/outLandmarkID);

/**
 * Adds a landmark to a category.
 *
 * @param landmarkStoreName where this landmark belongs
 * @param landmarkID landmark id to add 
 * @param categoryName which the landmark will be added to
 *
 * @retval JSR179_STATUSCODE_OK                 success
 * @retval JSR179_STATUSCODE_FAIL               on error
 * @retval JSR179_STATUSCODE_INVALID_ARGUMENT   if the category name is invalid
 */
jsr179_result jsr179_landmark_add_to_category(
        const pcsl_string landmarkStoreName, 
        jsr179_handle landmarkID,
        const pcsl_string categoryName);

/**
 * Update existing landmark in the landmark store.
 *
 * @param landmarkStoreName where this landmark belongs
 * @param landmarkID landmark id to update 
 * @param landmark to update
 *
 * @retval JSR179_STATUSCODE_OK                 success
 * @retval JSR179_STATUSCODE_FAIL               on error
 * @retval JSR179_STATUSCODE_INVALID_ARGUMENT   if the landmarkID is invalid
 */
jsr179_result jsr179_landmark_update(
        const pcsl_string landmarkStoreName, 
        jsr179_handle landmarkID,
        const jsr179_landmark* landmark);

/**
 * Deletes a landmark from a landmark store.
 *
 * This function removes the specified landmark from all categories and deletes the information from this landmark store.
 * If the specified landmark does not belong to this landmark store, then the request is silently ignored and this function call returns without error.
 *
 * @param landmarkStoreName where this landmark belongs
 * @param landmarkID        id of a landmark to delete
 *
 * @retval JSR179_STATUSCODE_OK          success
 * @retval JSR179_STATUSCODE_FAIL        on error
 */
jsr179_result jsr179_landmark_delete_from_landmarkstore(
        const pcsl_string landmarkStoreName,
        jsr179_handle landmarkID);
        
/**
 * Deletes a landmark from a category.
 *
 * If the specified landmark does not belong to the specified landmark store or category, then the request is silently ignored and this function call returns without error.
 *
 * @param landmarkStoreName where this landmark belongs
 * @param landmarkID id of a landmark to delete
 * @param categoryName from which the landmark to be removed
 *
 * @retval JSR179_STATUSCODE_OK          success
 * @retval JSR179_STATUSCODE_FAIL        on error
 */
jsr179_result jsr179_landmark_delete_from_category(
        const pcsl_string landmarkStoreName,
        jsr179_handle landmarkID,
        const pcsl_string categoryName);

/**
 * Gets a handle for LandmarkStore list.
 *
 * @param pHandle that can be used in jsr179_landmarkstorelist_next
 *
 * @retval JSR179_STATUSCODE_OK          success
 * @retval JSR179_STATUSCODE_FAIL        on other error
 */
jsr179_result jsr179_landmarkstorelist_open(
        jsr179_handle* /*OUT*/pHandle);

/**
 * Closes the specified landmarkstore list. 
 *
 * This handle will no longer be associated with this landmarkstore list.
 *
 * @param handle that is returned by jsr179_landmarkstorelist_open
 *
 */
void jsr179_landmarkstorelist_close(
        jsr179_handle handle);

/**
 * Returns the next landmark store name
 *
 * Assumes that the returned landmarkstore memory block is valid until the next this function call
 *
 * @param handle of landmark store
 * @param pLandmarkStore pointer to UNICODE string for the next landmark store name on success, NULL otherwise
 *      returned param is a pointer to platform specific memory block.
 *      platform MUST BE responsible for allocating and freeing it.
 *
 * @retval JSR179_STATUSCODE_OK          success
 * @retval JSR179_STATUSCODE_FAIL        on other error
 */
jsr179_result jsr179_landmarkstorelist_next(
        jsr179_handle handle,
        pcsl_string** /*OUT*/pStoreName);

/**
 * Gets a handle for Landmark list.
 *
 * @param landmarkStoreName landmark store to get the landmark from
 * @param categoryName of the landmark to get, NULL implies a wildcard that matches all categories.
 * @param pHandle that can be used in jsr179_landmarkstorelist_next
 *
 * @retval JSR179_STATUSCODE_OK          success
 * @retval JSR179_STATUSCODE_INVALID_ARGUMENT  if the category or landmarkStore name is invalid 
 * @retval JSR179_STATUSCODE_FAIL        on other error
 */
jsr179_result jsr179_landmarklist_open(
        const pcsl_string landmarkStoreName, 
        const pcsl_string categoryName, 
        jsr179_handle* /*OUT*/pHandle);

/**
 * Closes the specified landmark list. 
 *
 * This handle will no longer be associated with this landmark list.
 *
 * @param handle that is returned by jsr179_landmarklist_open
 *
 */
void jsr179_landmarklist_close(
        jsr179_handle handle);

/**
 * Returns the next landmark from a landmark store.
 *
 * Assumes that the returned landmark memory block is valid until the next this function call
 *
 * @param handle of landmark store
 * @param pLandmarkID id of returned landmark.
 * @param pLandmark pointer to landmark on sucess, NULL otherwise
 *      returned param is a pointer to platform specific memory block.
 *      platform MUST BE responsible for allocating and freeing it.
 *
 * @retval JSR179_STATUSCODE_OK          success
 * @retval JSR179_STATUSCODE_FAIL        on other error
 */
jsr179_result jsr179_landmarklist_next(
        jsr179_handle handle,
        int* /*OUT*/pLandmarkID,
        jsr179_landmark** /*OUT*/pLandmark);

/**
 * Gets a handle to get Category list in specified landmark store.
 *
 * @param landmarkStoreName landmark store to get the categories from
 * @param pHandle that can be used in jsr179_categorylist_next
 *
 * @retval JSR179_STATUSCODE_OK          success
 * @retval JSR179_STATUSCODE_FAIL        the other error
 * @retval JSR179_STATUSCODE_INVALID_ARGUMENT  if the landmarkStore name is invalid 
 */
jsr179_result jsr179_categorylist_open(
        const pcsl_string landmarkStoreName,
        jsr179_handle* /*OUT*/pHandle);

/**
 * Closes the specified category list. 
 * This handle will no longer be associated with this category list.
 *
 * @param handle that is returned by jsr179_categorylist_open
 *
 */
void jsr179_categorylist_close(
        jsr179_handle handle);

/**
 * Returns the next category name in specified landmark store.
 *
 * Assumes that the returned landmark memory block is valid until the next this function call
 *
 * @param handle of landmark store
 * @param pCategoryName pointer to UNICODE string for the next category name on success, NULL otherwise
 *      returned param is a pointer to platform specific memory block.
 *      platform MUST BE responsible for allocating and freeing it.
 *
 * @retval JSR179_STATUSCODE_OK          success
 * @retval JSR179_STATUSCODE_FAIL        the other error
 *
 */
jsr179_result jsr179_categorylist_next(
        jsr179_handle handle,
        pcsl_string** /*OUT*/pCategoryName);

/**
 * Creates a landmark store.
 *
 * If the implementation supports several storage media, this function may e.g. prompt the end user to make the choice.
 *
 * @param landmarkStoreName name of a landmark store.
 *
 * @retval JSR179_STATUSCODE_OK          success
 * @retval JSR179_STATUSCODE_FAIL        the other error
 * @retval JSR179_STATUSCODE_INVALID_ARGUMENT   if the name is too long or if a landmark store with specified name already exists.
 */
jsr179_result /*OPTIONAL*/ jsr179_landmarkstore_create(
        const pcsl_string landmarkStoreName);

/**
 * Deletes a landmark store.
 *
 * All the landmarks and categories defined in named landmark store are removed.
 * If a landmark store with the specified name does not exist, this function returns silently without any error.
 *
 * @param landmarkStoreName name of a landmark store.
 *
 * @retval JSR179_STATUSCODE_OK          success
 * @retval JSR179_STATUSCODE_FAIL        the other error
 * @retval JSR179_STATUSCODE_INVALID_ARGUMENT   if the name is too long 
 */
jsr179_result /*OPTIONAL*/ jsr179_landmarkstore_delete(
        const pcsl_string landmarkStoreName);

/**
 * Adds a category to a landmark store.
 *
 * This function must support category name that have length up to and 
 * including 32 chracters.
 *
 * @param landmarkStoreName the name of the landmark store.
 * @param categoryName category name - UNICODE string
 *
 * @retval JSR179_STATUSCODE_OK          success
 * @retval JSR179_STATUSCODE_FAIL        the other error
 * @retval JSR179_STATUSCODE_INVALID_ARGUMENT   if a category name already exists
 */
jsr179_result /*OPTIONAL*/ jsr179_category_add(
        const pcsl_string landmarkStoreName,
        const pcsl_string categoryName);

/**
 * Removes a category from a landmark store.
 *
 * The category will be removed from all landmarks that are in that category. However, this function will not remove any of the landmarks. Only the associated category information from the landmarks are removed.
 * If a category with the supplied name does not exist in the specified landmark store, this function returns silently without error.
 *
 * @param landmarkStoreName the name of the landmark store.
 * @param categoryName category name - UNICODE string
 *
 * @retval JSR179_STATUSCODE_OK          success
 * @retval JSR179_STATUSCODE_FAIL        the other error
 */
jsr179_result /*OPTIONAL*/ jsr179_category_delete(
        const pcsl_string landmarkStoreName,
        const pcsl_string categoryName);

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
jdouble /*OPTIONAL*/ jsr179_atan2(
        jdouble x, jdouble y);

/** @} */
    
#ifdef __cplusplus
}
#endif

#endif /* _JSR179_LOCATION_H */


