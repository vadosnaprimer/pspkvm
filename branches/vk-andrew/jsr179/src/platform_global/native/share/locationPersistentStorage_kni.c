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
#include <midpUtilKni.h>
#include <midpString.h>
#include <midpServices.h>
#include <midpError.h>

#include <jsr179_location.h>

/**
 * Field IDs of the <tt>com.sun.j2me.location.LocationInfo</tt> class
 * The fields initialized at startup and used to speedup access to the fields
 */
typedef struct{
    jfieldID name;
    jfieldID description;
    /* Coordinates fields */
    jfieldID isCoordinates;
    jfieldID latitude;
    jfieldID longitude;
    jfieldID altitude;
    jfieldID horizontalAccuracy;
    jfieldID verticalAccuracy;
    /* AddressInfo fields */
    jfieldID isAddressInfo;
    jfieldID numAddressInfoFields;
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
}LandmarkImplFieldIDs;
static LandmarkImplFieldIDs landmarkImplFieldID;

static jboolean fill_adressInfoField(jobject *landmarkObj, jfieldID fieldID, 
                jobject *stringObj,  jsr179_addressinfo_fieldinfo *fieldInfo, 
                jsr179_addressinfo_field addressInfoFieldId);
static jboolean fill_landmark(jobject *landmarkObj, jsr179_landmark **landmark, 
                                jobject *stringObj);


/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_INT
    Java_com_sun_j2me_location_LocationPersistentStorage_openLandmarkStoreList() {
    
    jsr179_handle pHandle;
    jint hndl = 0;
    jsr179_result res;
    
    /* call provider_open to get provider handler */
    res = jsr179_landmarkstorelist_open(&pHandle);
    switch (res) {
        case JSR179_STATUSCODE_OK:
            /* landmarkStore list open successfully */
            hndl = (jint)pHandle;
            break;
        default:
            /* operation Failed */
            KNI_ThrowNew(midpIOException, "I/O error");
            break;
    }

    KNI_ReturnInt(hndl);
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_OBJECT
    Java_com_sun_j2me_location_LocationPersistentStorage_landmarkStoreGetNext() {
    
    jint hndl;
    jsr179_result res;
    pcsl_string *storeName;

    KNI_StartHandles(1);
    KNI_DeclareHandle(stringObj);
    hndl = KNI_GetParameterAsInt(1);

    res = jsr179_landmarkstorelist_next((jsr179_handle)hndl, &storeName);
    switch (res) {
        case JSR179_STATUSCODE_OK:
            /* LandmarkStore name returned successfully */
            if (!pcsl_string_is_null(storeName)) {
                midp_jstring_from_pcsl_string(storeName, stringObj);
            } else {
                /* Category name returned successfully */
                KNI_NewString(NULL, 0, stringObj);
            }
            break;
        default:
            /* operation Failed */
            KNI_ThrowNew(midpIOException, "I/O error");
            break;
    }

    KNI_EndHandlesAndReturnObject(stringObj);    
}
    
/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_VOID
    Java_com_sun_j2me_location_LocationPersistentStorage_closeLandmarkStoreList() {
    
    jint hndl = KNI_GetParameterAsInt(1);

    jsr179_landmarkstorelist_close((jsr179_handle)hndl);

    KNI_ReturnVoid();
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_VOID
    Java_com_sun_j2me_location_LocationPersistentStorage_createLandmarkStore() {
    
    jsr179_result res;
    
    KNI_StartHandles(1);
    GET_PARAMETER_AS_PCSL_STRING(1, storeName)

    /* call provider_open to get provider handler */
    res = jsr179_landmarkstore_create(storeName);
    switch (res) {
      case JSR179_STATUSCODE_OK:
            /* LandmarkStore created successfully */
            break;
        case JSR179_STATUSCODE_FAIL:
            /* operation Failed */
            KNI_ThrowNew(midpIOException, "I/O error");
            break;
        case JSR179_STATUSCODE_INVALID_ARGUMENT:
            /* operation Failed */
            KNI_ThrowNew(midpIllegalArgumentException, 
                "name is too long or Landmarkstore already exists");
            break;
        default:
            /* operation Failed */
            KNI_ThrowNew(midpIOException, "I/O error");
            break;
    }

    RELEASE_PCSL_STRING_PARAMETER
    KNI_EndHandles();
    KNI_ReturnVoid();
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_VOID
    Java_com_sun_j2me_location_LocationPersistentStorage_removeLandmarkStore() {
    
    jsr179_result res;
    
    KNI_StartHandles(1);
    GET_PARAMETER_AS_PCSL_STRING(1, storeName)

    /* call provider_open to get provider handler */
    res = jsr179_landmarkstore_delete(storeName);
    switch (res) {
        case JSR179_STATUSCODE_OK:
            /* LandmarkStore created successfully */
            break;
        case JSR179_STATUSCODE_FAIL:
            /* operation Failed */
            KNI_ThrowNew(midpIOException, "I/O error");
            break;
        case JSR179_STATUSCODE_INVALID_ARGUMENT:
            /* operation Failed */
            KNI_ThrowNew(midpIOException, "name is too long");
            break;
        default:
            /* operation Failed */
            KNI_ThrowNew(midpIOException, "I/O error");
            break;
    }

    RELEASE_PCSL_STRING_PARAMETER
    KNI_EndHandles();
    KNI_ReturnVoid();
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_INT
    Java_com_sun_j2me_location_LocationPersistentStorage_openCategoryList() {
    
    jsr179_handle pHandle;
    jint hndl = 0;
    jsr179_result res;
    
    KNI_StartHandles(1);
    GET_PARAMETER_AS_PCSL_STRING(1, storeName)
    /* call provider_open to get provider handler */
    res = jsr179_categorylist_open(storeName, &pHandle);
    switch (res) {
        case JSR179_STATUSCODE_OK:
            /* Category list open successfully */
            hndl = (jint)pHandle;
            break;
        default:
            /* operation Failed */
            KNI_ThrowNew(midpIOException, "I/O error");
            break;
    }

    RELEASE_PCSL_STRING_PARAMETER
    KNI_EndHandles();
    KNI_ReturnInt(hndl);
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_OBJECT
    Java_com_sun_j2me_location_LocationPersistentStorage_categoryGetNext() {
    
    jint hndl;
    jsr179_result res;
    pcsl_string *categoryName;

    KNI_StartHandles(1);
    KNI_DeclareHandle(stringObj);
    hndl = KNI_GetParameterAsInt(1);

    res = jsr179_categorylist_next((jsr179_handle)hndl, &categoryName);
    switch (res) {
        case JSR179_STATUSCODE_OK:
            /* Category name returned successfully */
            midp_jstring_from_pcsl_string(categoryName, stringObj);
            break;
        case JSR179_STATUSCODE_FAIL:
            /* Category name returned successfully */
            KNI_NewString(NULL, 0, stringObj);
            break;
        default:
            /* operation Failed */
            KNI_ThrowNew(midpIOException, "I/O error");
            break;
    }

    KNI_EndHandlesAndReturnObject(stringObj);    
}
    
/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_VOID
    Java_com_sun_j2me_location_LocationPersistentStorage_closeCategoryList() {
    
    jint hndl = KNI_GetParameterAsInt(1);

    jsr179_categorylist_close((jsr179_handle)hndl);

    KNI_ReturnVoid();
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_VOID
    Java_com_sun_j2me_location_LocationPersistentStorage_addCategoryImpl() {
    
    jsr179_result res;
    
    KNI_StartHandles(2);
    GET_PARAMETER_AS_PCSL_STRING(1, storeName)
    GET_PARAMETER_AS_PCSL_STRING(2, categoryName)

    res = jsr179_category_add(storeName, categoryName);
    switch (res) {
        case JSR179_STATUSCODE_OK:
            /* Category added successfully */
            break;
        case JSR179_STATUSCODE_INVALID_ARGUMENT:
            /* wrong provider name */
            KNI_ThrowNew(midpIllegalArgumentException, 
                        "category name already exist");
            break;
        default:
            /* operation Failed */
            KNI_ThrowNew(midpIOException, "I/O error");
            break;
    }

    RELEASE_PCSL_STRING_PARAMETER
    RELEASE_PCSL_STRING_PARAMETER
    KNI_EndHandles();
    KNI_ReturnVoid();
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_VOID
    Java_com_sun_j2me_location_LocationPersistentStorage_deleteCategoryImpl() {
    
    jsr179_result res;
    
    KNI_StartHandles(2);
    GET_PARAMETER_AS_PCSL_STRING(1, storeName)
    GET_PARAMETER_AS_PCSL_STRING(2, categoryName)

    res = jsr179_category_delete(storeName, categoryName);
    switch (res) {
        case JSR179_STATUSCODE_OK:
            /* Category deleted successfully */
            break;
        default:
            /* operation Failed */
            KNI_ThrowNew(midpIOException, "I/O error");
            break;
    }

    RELEASE_PCSL_STRING_PARAMETER
    RELEASE_PCSL_STRING_PARAMETER
    KNI_EndHandles();
    KNI_ReturnVoid();
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_VOID
    Java_com_sun_j2me_location_LocationPersistentStorage_addLandmarkToCategoryImpl() {
    
    jint landmarkID;
    jsr179_result res;
    
    KNI_StartHandles(2);
    GET_PARAMETER_AS_PCSL_STRING(1, storeName)
    landmarkID = KNI_GetParameterAsInt(2);
    GET_PARAMETER_AS_PCSL_STRING(3, categoryName)

    res = jsr179_landmark_add_to_category(storeName, 
                            (jsr179_handle)landmarkID, categoryName);
    switch (res) {
        case JSR179_STATUSCODE_OK:
            /* Category added successfully */
            break;
        case JSR179_STATUSCODE_INVALID_ARGUMENT:
            /* wrong category name */
            KNI_ThrowNew(midpIllegalArgumentException, 
                        "category name is invalid");
            break;
        default:
            /* operation Failed */
            KNI_ThrowNew(midpIOException, "I/O error");
            break;
    }

    RELEASE_PCSL_STRING_PARAMETER
    RELEASE_PCSL_STRING_PARAMETER
    KNI_EndHandles();
    KNI_ReturnVoid();
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_INT
    Java_com_sun_j2me_location_LocationPersistentStorage_addLandmarkToStoreImpl() {
    
    jsr179_handle landmarkID = 0;
    jsr179_result res;
    pcsl_string categoryName = PCSL_STRING_NULL;
    jsr179_landmark *landmark;
    
    KNI_StartHandles(3);
    KNI_DeclareHandle(landmarkObj);
    KNI_DeclareHandle(stringObj);

    GET_PARAMETER_AS_PCSL_STRING(1, storeName)
    KNI_GetParameterAsObject(2, landmarkObj);

    /* CategoryName can be NULL -> check it and extract */
    KNI_GetParameterAsObject(3, stringObj);
    if (!KNI_IsNullHandle(stringObj)) {
        midp_jstring_to_pcsl_string(stringObj, &categoryName);
    }

    if ( fill_landmark(&landmarkObj, &landmark, &stringObj) == KNI_TRUE ) {
        res = jsr179_landmark_add_to_landmarkstore(storeName, 
                                landmark, categoryName, &landmarkID);
        switch (res) {
            case JSR179_STATUSCODE_OK:
                /* Category added successfully */
                break;
            case JSR179_STATUSCODE_INVALID_ARGUMENT:
                /* wrong category name */
                KNI_ThrowNew(midpIllegalArgumentException, 
                            "category name is invalid");
                break;
            default:
                /* operation Failed */
                KNI_ThrowNew(midpIOException, "I/O error");
                break;
        }
    }

    RELEASE_PCSL_STRING_PARAMETER
    KNI_EndHandles();
    KNI_ReturnInt((jint)landmarkID);
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_VOID
    Java_com_sun_j2me_location_LocationPersistentStorage_deleteLandmarkFromStoreImpl() {
    
    jint landmarkID;
    jsr179_result res;
    
    KNI_StartHandles(1);
    GET_PARAMETER_AS_PCSL_STRING(1, storeName)
    landmarkID = KNI_GetParameterAsInt(2);

    res = jsr179_landmark_delete_from_landmarkstore(
                                storeName, (jsr179_handle)landmarkID);
    switch (res) {
        case JSR179_STATUSCODE_OK:
            /* Category added successfully */
            break;
        default:
            /* operation Failed */
            KNI_ThrowNew(midpIOException, "I/O error");
            break;
    }

    RELEASE_PCSL_STRING_PARAMETER
    KNI_EndHandles();
    KNI_ReturnVoid();
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_VOID
    Java_com_sun_j2me_location_LocationPersistentStorage_deleteLandmarkFromCategoryImpl() {
    
    jint landmarkID;
    jsr179_result res;
    
    KNI_StartHandles(2);
    GET_PARAMETER_AS_PCSL_STRING(1, storeName)
    landmarkID = KNI_GetParameterAsInt(2);
    GET_PARAMETER_AS_PCSL_STRING(3, categoryName)

    res = jsr179_landmark_delete_from_category(storeName, 
                        (jsr179_handle)landmarkID, categoryName);
    switch (res) {
        case JSR179_STATUSCODE_OK:
            /* Category added successfully */
            break;
        default:
            /* operation Failed */
            KNI_ThrowNew(midpIOException, "I/O error");
            break;
    }

    RELEASE_PCSL_STRING_PARAMETER
    RELEASE_PCSL_STRING_PARAMETER
    KNI_EndHandles();
    KNI_ReturnVoid();
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_INT
    Java_com_sun_j2me_location_LocationPersistentStorage_openLandmarkList() {
    
    jsr179_handle hndl = 0;
    jsr179_result res;
    
    KNI_StartHandles(2);
    GET_PARAMETER_AS_PCSL_STRING(1, storeName)
    GET_PARAMETER_AS_PCSL_STRING(2, categoryName)

    res =  jsr179_landmarklist_open(storeName, categoryName, &hndl);
    switch (res) {
        case JSR179_STATUSCODE_OK:
            /* Category list open successfully */
            break;
        case JSR179_STATUSCODE_INVALID_ARGUMENT:
            /* wrong category name */
            break;
        default:
            /* operation Failed */
            KNI_ThrowNew(midpIOException, "I/O error");
            break;
    }

    RELEASE_PCSL_STRING_PARAMETER
    RELEASE_PCSL_STRING_PARAMETER
    KNI_EndHandles();
    KNI_ReturnInt((jint)hndl);
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_INT
    Java_com_sun_j2me_location_LocationPersistentStorage_landmarkGetNext() {
    
    jint hndl;
    jint landmarkID = 0;
    jsr179_result res;
    jsr179_landmark *landmark;
    jfieldID fid;
    jint i;

    KNI_StartHandles(2);
    KNI_DeclareHandle(landmarkObj);
    KNI_DeclareHandle(stringObj);
    hndl = KNI_GetParameterAsInt(1);
    KNI_GetParameterAsObject(2, landmarkObj);

    res = jsr179_landmarklist_next((jsr179_handle)hndl, &landmarkID, &landmark);
    switch (res) {
        case JSR179_STATUSCODE_OK:
            if (landmark != NULL) {
                /* landmark.name */
                midp_jstring_from_pcsl_string(&landmark->name, stringObj);
                KNI_SetObjectField(landmarkObj, landmarkImplFieldID.name, stringObj);

                /* landmark.description */
                if (!pcsl_string_is_null(&(landmark->description))) {
                    midp_jstring_from_pcsl_string(&landmark->description, stringObj);
                    KNI_SetObjectField(landmarkObj, landmarkImplFieldID.description, stringObj);
                }

                if (!landmark->isValidCoordinate)
                {
                    /* landmark.isCoordinates */
                    KNI_SetBooleanField(landmarkObj, 
                                      landmarkImplFieldID.isCoordinates, KNI_FALSE);
                } else {
                    /* landmark.latitude */
                    KNI_SetDoubleField(landmarkObj, landmarkImplFieldID.latitude,
                                        landmark->latitude);
                    /* landmark.longitude */
                    KNI_SetDoubleField(landmarkObj, landmarkImplFieldID.longitude,
                                        landmark->longitude);
                    /* landmark.altitude */
                    KNI_SetFloatField(landmarkObj, landmarkImplFieldID.altitude,
                                        landmark->altitude);
                    /* landmark.horizontalAccuracy */
                    KNI_SetFloatField(landmarkObj, landmarkImplFieldID.horizontalAccuracy,
                                        landmark->horizontalAccuracy);
                    /* landmark.verticalAccuracy */
                    KNI_SetFloatField(landmarkObj, landmarkImplFieldID.verticalAccuracy,
                                        landmark->verticalAccuracy);
                    /* landmark.isCoordinates */
                    KNI_SetBooleanField(landmarkObj, 
                                      landmarkImplFieldID.isCoordinates, KNI_TRUE);
                }
                /* landmark.addressInfoFieldNumber */
                KNI_SetIntField(landmarkObj, 
                                landmarkImplFieldID.numAddressInfoFields,
                                landmark->addressInfoFieldNumber);
                /* landmark.isAddressInfo */
                KNI_SetBooleanField(landmarkObj, 
                                  landmarkImplFieldID.isAddressInfo, 
                 (landmark->addressInfoFieldNumber > 0) ? KNI_TRUE : KNI_FALSE);
                for (i=0; i < landmark->addressInfoFieldNumber; i++) {
                    switch (landmark->fields[i].fieldId) {
                        case JSR179_ADDRESSINFO_EXTENSION:
                            fid = landmarkImplFieldID.
                                    AddressInfo_EXTENSION;
                            break;
                        case JSR179_ADDRESSINFO_STREET:
                            fid = landmarkImplFieldID.
                                    AddressInfo_STREET;
                            break;
                        case JSR179_ADDRESSINFO_POSTAL_CODE:
                            fid = landmarkImplFieldID.
                                    AddressInfo_POSTAL_CODE;
                            break;
                        case JSR179_ADDRESSINFO_CITY:
                            fid = landmarkImplFieldID.
                                    AddressInfo_CITY;
                            break;
                        case JSR179_ADDRESSINFO_COUNTY:
                            fid = landmarkImplFieldID.
                                    AddressInfo_COUNTY;
                            break;
                        case JSR179_ADDRESSINFO_STATE:
                            fid = landmarkImplFieldID.
                                    AddressInfo_STATE;
                            break;
                        case JSR179_ADDRESSINFO_COUNTRY:
                            fid = landmarkImplFieldID.
                                    AddressInfo_COUNTRY;
                            break;
                        case JSR179_ADDRESSINFO_COUNTRY_CODE:
                            fid = landmarkImplFieldID.
                                    AddressInfo_COUNTRY_CODE;
                            break;
                        case JSR179_ADDRESSINFO_DISTRICT:
                            fid = landmarkImplFieldID.
                                    AddressInfo_DISTRICT;
                            break;
                        case JSR179_ADDRESSINFO_BUILDING_NAME:
                            fid = landmarkImplFieldID.
                                    AddressInfo_BUILDING_NAME;
                            break;
                        case JSR179_ADDRESSINFO_BUILDING_FLOOR:
                            fid = landmarkImplFieldID.
                                    AddressInfo_BUILDING_FLOOR;
                            break;
                        case JSR179_ADDRESSINFO_BUILDING_ROOM:
                            fid = landmarkImplFieldID.
                                    AddressInfo_BUILDING_ROOM;
                            break;
                        case JSR179_ADDRESSINFO_BUILDING_ZONE:
                            fid = landmarkImplFieldID.
                                    AddressInfo_BUILDING_ZONE;
                            break;
                        case JSR179_ADDRESSINFO_CROSSING1:
                            fid = landmarkImplFieldID.
                                    AddressInfo_CROSSING1;
                            break;
                        case JSR179_ADDRESSINFO_CROSSING2:
                            fid = landmarkImplFieldID.
                                    AddressInfo_CROSSING2;
                            break;
                        case JSR179_ADDRESSINFO_URL:
                            fid = landmarkImplFieldID.
                                    AddressInfo_URL;
                            break;
                        case JSR179_ADDRESSINFO_PHONE_NUMBER:
                            fid = landmarkImplFieldID.
                                    AddressInfo_PHONE_NUMBER;
                            break;
                        default:
                            fid = 0;
                            break;
                    }
                    if (fid != 0) {
                        /* addressInfo */
                        if (!pcsl_string_is_null(&(landmark->fields[i].data))) {
                            midp_jstring_from_pcsl_string(&landmark->fields[i].data, stringObj);
                            KNI_SetObjectField(landmarkObj, fid, stringObj);
                        }
                    }
                }
            }
            break;
        default:
            /* operation Failed */
            KNI_ThrowNew(midpIOException, "I/O error");
            break;
    }

    KNI_EndHandles();
    KNI_ReturnInt(landmarkID);    
}
    
/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_VOID
    Java_com_sun_j2me_location_LocationPersistentStorage_closeLandmarkList() {
    
    jint hndl = KNI_GetParameterAsInt(1);

    jsr179_landmarklist_close((jsr179_handle)hndl);

    KNI_ReturnVoid();
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_VOID
    Java_com_sun_j2me_location_LocationPersistentStorage_updateLandmarkImpl() {
    
    jint landmarkID;
    jsr179_result res;
    jsr179_landmark *landmark;
    
    KNI_StartHandles(3);
    KNI_DeclareHandle(landmarkObj);
    KNI_DeclareHandle(stringObj);
    GET_PARAMETER_AS_PCSL_STRING(1, storeName)
    landmarkID = KNI_GetParameterAsInt(2);
    KNI_GetParameterAsObject(3, landmarkObj);

    if (fill_landmark(&landmarkObj, &landmark, &stringObj) == KNI_TRUE) {
        res = jsr179_landmark_update(storeName, 
                                (jsr179_handle)landmarkID, landmark);
        switch (res) {
            case JSR179_STATUSCODE_OK:
                /* Landmark updated successfully */
                break;
            case JSR179_STATUSCODE_INVALID_ARGUMENT:
                /* wrong landmark ID */
                KNI_ThrowNew(midpIllegalArgumentException, 
                            "Landmark does not belong to this store");
                break;
            default:
                /* operation Failed */
                KNI_ThrowNew(midpIOException, "I/O error");
                break;
        }
    }

    RELEASE_PCSL_STRING_PARAMETER
    KNI_EndHandles();
    KNI_ReturnVoid();
}

/* JAVADOC COMMENT ELIDED */
KNIEXPORT KNI_RETURNTYPE_VOID
    Java_com_sun_j2me_location_LandmarkImpl_initNativeClass() {

    KNI_StartHandles(1);
    KNI_DeclareHandle(class_obj);

    KNI_GetClassPointer(class_obj);
    if(!KNI_IsNullHandle(class_obj)) {
        landmarkImplFieldID.name = 
            KNI_GetFieldID(class_obj, "name", "Ljava/lang/String;");
        landmarkImplFieldID.description = 
            KNI_GetFieldID(class_obj, "description", "Ljava/lang/String;");
        landmarkImplFieldID.isCoordinates = 
            KNI_GetFieldID(class_obj, "isCoordinates", "Z");
        landmarkImplFieldID.latitude = 
            KNI_GetFieldID(class_obj, "latitude", "D");
        landmarkImplFieldID.longitude = 
            KNI_GetFieldID(class_obj, "longitude", "D");
        landmarkImplFieldID.altitude = 
            KNI_GetFieldID(class_obj, "altitude", "F");
        landmarkImplFieldID.horizontalAccuracy = 
            KNI_GetFieldID(class_obj, "horizontalAccuracy", "F");
        landmarkImplFieldID.verticalAccuracy = 
            KNI_GetFieldID(class_obj, "verticalAccuracy", "F");
        landmarkImplFieldID.isAddressInfo = 
            KNI_GetFieldID(class_obj, "isAddressInfo", "Z");
        landmarkImplFieldID.numAddressInfoFields = 
            KNI_GetFieldID(class_obj, "numAddressInfoFields", "I");
        landmarkImplFieldID.AddressInfo_EXTENSION = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_EXTENSION", "Ljava/lang/String;");
        landmarkImplFieldID.AddressInfo_STREET = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_STREET", "Ljava/lang/String;");
        landmarkImplFieldID.AddressInfo_POSTAL_CODE = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_POSTAL_CODE", "Ljava/lang/String;");
        landmarkImplFieldID.AddressInfo_CITY = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_CITY", "Ljava/lang/String;");
        landmarkImplFieldID.AddressInfo_COUNTY = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_COUNTY", "Ljava/lang/String;");
        landmarkImplFieldID.AddressInfo_STATE = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_STATE", "Ljava/lang/String;");
        landmarkImplFieldID.AddressInfo_COUNTRY = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_COUNTRY", "Ljava/lang/String;");
        landmarkImplFieldID.AddressInfo_COUNTRY_CODE = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_COUNTRY_CODE", "Ljava/lang/String;");
        landmarkImplFieldID.AddressInfo_DISTRICT = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_DISTRICT", "Ljava/lang/String;");
        landmarkImplFieldID.AddressInfo_BUILDING_NAME = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_BUILDING_NAME", "Ljava/lang/String;");
        landmarkImplFieldID.AddressInfo_BUILDING_FLOOR = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_BUILDING_FLOOR", "Ljava/lang/String;");
        landmarkImplFieldID.AddressInfo_BUILDING_ROOM = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_BUILDING_ROOM", "Ljava/lang/String;");
        landmarkImplFieldID.AddressInfo_BUILDING_ZONE = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_BUILDING_ZONE", "Ljava/lang/String;");
        landmarkImplFieldID.AddressInfo_CROSSING1 = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_CROSSING1", "Ljava/lang/String;");
        landmarkImplFieldID.AddressInfo_CROSSING2 = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_CROSSING2", "Ljava/lang/String;");
        landmarkImplFieldID.AddressInfo_URL = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_URL", "Ljava/lang/String;");
        landmarkImplFieldID.AddressInfo_PHONE_NUMBER = 
            KNI_GetFieldID(class_obj, 
                "AddressInfo_PHONE_NUMBER", "Ljava/lang/String;");
    } else {
        KNI_ThrowNew(midpNullPointerException, NULL);
    }

    KNI_EndHandles();
    KNI_ReturnVoid();
}


/* Helper-functions */

/**
 * (Internal) Fill addressInfo Field.
 */
static jboolean fill_adressInfoField(jobject *landmarkObj, jfieldID fieldID, 
                jobject *stringObj,  jsr179_addressinfo_fieldinfo *fieldInfo, 
                jsr179_addressinfo_field addressInfoFieldId) {
    KNI_GetObjectField(*landmarkObj, fieldID, *stringObj);
    fieldInfo->fieldId = addressInfoFieldId;
    if (!KNI_IsNullHandle(*stringObj)) {
        if (PCSL_STRING_OK != 
                midp_jstring_to_pcsl_string(*stringObj, &fieldInfo->data)) {
            return KNI_FALSE;
        }
        return KNI_TRUE;
    } else {
        fieldInfo->data = PCSL_STRING_NULL;
        return KNI_TRUE;
    }
    return KNI_FALSE;
}

/**
 * (Internal) Fill landmark structure from the Landmark Object.
 */
static jboolean fill_landmark(jobject *landmarkObj, jsr179_landmark **landmark, 
                                jobject *stringObj) {
    jint len;
    jint numAddressFields = KNI_GetIntField(*landmarkObj, 
            landmarkImplFieldID.numAddressInfoFields);
    *landmark = midpMalloc(JSR179_SIZE_OF_LANDMARK_INFO(numAddressFields));
    /* Fill jsr179_landmark structure */
    /* landmark.name */
    KNI_GetObjectField(*landmarkObj, landmarkImplFieldID.name, *stringObj);
    if (!KNI_IsNullHandle(*stringObj)) {
        if (PCSL_STRING_OK != 
                midp_jstring_to_pcsl_string(*stringObj, &(*landmark)->name)) {
            return KNI_FALSE;
        }
    } else {
        (*landmark)->name = PCSL_STRING_NULL;
    }
    /* landmark.description */
    KNI_GetObjectField(*landmarkObj, landmarkImplFieldID.description, 
        *stringObj);
    if (!KNI_IsNullHandle(*stringObj)) {
        if (PCSL_STRING_OK != 
                midp_jstring_to_pcsl_string(*stringObj, &(*landmark)->description)) {
            return KNI_FALSE;
        }
    } else {
        (*landmark)->description = PCSL_STRING_NULL;
    }

    (*landmark)->isValidCoordinate = KNI_GetBooleanField(*landmarkObj, 
                                landmarkImplFieldID.isCoordinates);
    if ((*landmark)->isValidCoordinate) {
        /* landmark.latitude */
        (*landmark)->latitude = KNI_GetDoubleField(*landmarkObj, 
                                    landmarkImplFieldID.latitude);
        /* landmark.longitude */
        (*landmark)->longitude = KNI_GetDoubleField(*landmarkObj, 
                                    landmarkImplFieldID.longitude);
        /* landmark.altitude */
        (*landmark)->altitude = KNI_GetFloatField(*landmarkObj, 
                                    landmarkImplFieldID.altitude);
        /* landmark.horizontalAccuracy */
        (*landmark)->horizontalAccuracy = KNI_GetFloatField(*landmarkObj, 
                                    landmarkImplFieldID.horizontalAccuracy);
        /* landmark.altitude */
        (*landmark)->verticalAccuracy = KNI_GetFloatField(*landmarkObj, 
                                    landmarkImplFieldID.verticalAccuracy);
    }

    /* AddressInfo */
    (*landmark)->addressInfoFieldNumber = KNI_GetIntField(*landmarkObj, 
            landmarkImplFieldID.numAddressInfoFields);
    if ((*landmark)->addressInfoFieldNumber > 0) {
            jint counter = 0;
            /* AddressInfo_EXTENSION */
            if ((counter < (*landmark)->addressInfoFieldNumber) &&
                (fill_adressInfoField(landmarkObj, 
                            landmarkImplFieldID.AddressInfo_EXTENSION,
                            stringObj, 
                            &(*landmark)->fields[counter],
                            JSR179_ADDRESSINFO_EXTENSION) == KNI_TRUE)) {
                counter++;
            }
            /* AddressInfo_STREET */
            if ((counter < (*landmark)->addressInfoFieldNumber) &&
                (fill_adressInfoField(landmarkObj, 
                            landmarkImplFieldID.AddressInfo_STREET,
                            stringObj, 
                            &(*landmark)->fields[counter],
                            JSR179_ADDRESSINFO_STREET) == KNI_TRUE)) {
                counter++;
            }
            /* AddressInfo_POSTAL_CODE */
            if ((counter < (*landmark)->addressInfoFieldNumber) &&
                (fill_adressInfoField(landmarkObj, 
                            landmarkImplFieldID.AddressInfo_POSTAL_CODE,
                            stringObj, 
                            &(*landmark)->fields[counter],
                            JSR179_ADDRESSINFO_POSTAL_CODE) == KNI_TRUE)) {
                counter++;
            }
            /* AddressInfo_CITY */
            if ((counter < (*landmark)->addressInfoFieldNumber) &&
                (fill_adressInfoField(landmarkObj, 
                            landmarkImplFieldID.AddressInfo_CITY,
                            stringObj, 
                            &(*landmark)->fields[counter],
                            JSR179_ADDRESSINFO_CITY) == KNI_TRUE)) {
                counter++;
            }
            /* AddressInfo_COUNTY */
            if ((counter < (*landmark)->addressInfoFieldNumber) &&
                (fill_adressInfoField(landmarkObj, 
                            landmarkImplFieldID.AddressInfo_COUNTY,
                            stringObj, 
                            &(*landmark)->fields[counter],
                            JSR179_ADDRESSINFO_COUNTY) == KNI_TRUE)) {
                counter++;
            }
            /* AddressInfo_STATE */
            if ((counter < (*landmark)->addressInfoFieldNumber) &&
                (fill_adressInfoField(landmarkObj, 
                            landmarkImplFieldID.AddressInfo_STATE,
                            stringObj, 
                            &(*landmark)->fields[counter],
                            JSR179_ADDRESSINFO_STATE) == KNI_TRUE)) {
                counter++;
            }
            /* AddressInfo_COUNTRY */
            if ((counter < (*landmark)->addressInfoFieldNumber) &&
                (fill_adressInfoField(landmarkObj, 
                            landmarkImplFieldID.AddressInfo_COUNTRY,
                            stringObj, 
                            &(*landmark)->fields[counter],
                            JSR179_ADDRESSINFO_COUNTRY) == KNI_TRUE)) {
                counter++;
            }
            /* AddressInfo_COUNTRY_CODE */
            if ((counter < (*landmark)->addressInfoFieldNumber) &&
                (fill_adressInfoField(landmarkObj, 
                            landmarkImplFieldID.AddressInfo_COUNTRY_CODE,
                            stringObj, 
                            &(*landmark)->fields[counter],
                            JSR179_ADDRESSINFO_COUNTRY_CODE) == KNI_TRUE)) {
                counter++;
            }
            /* AddressInfo_DISTRICT */
            if ((counter < (*landmark)->addressInfoFieldNumber) &&
                (fill_adressInfoField(landmarkObj, 
                            landmarkImplFieldID.AddressInfo_DISTRICT,
                            stringObj, 
                            &(*landmark)->fields[counter],
                            JSR179_ADDRESSINFO_DISTRICT) == KNI_TRUE)) {
                counter++;
            }
            /* AddressInfo_BUILDING_NAME */
            if ((counter < (*landmark)->addressInfoFieldNumber) &&
                (fill_adressInfoField(landmarkObj, 
                            landmarkImplFieldID.AddressInfo_BUILDING_NAME,
                            stringObj, 
                            &(*landmark)->fields[counter],
                            JSR179_ADDRESSINFO_BUILDING_NAME) == KNI_TRUE)) {
                counter++;
            }
            /* AddressInfo_BUILDING_FLOOR */
            if ((counter < (*landmark)->addressInfoFieldNumber) &&
                (fill_adressInfoField(landmarkObj, 
                            landmarkImplFieldID.AddressInfo_BUILDING_FLOOR,
                            stringObj, 
                            &(*landmark)->fields[counter],
                            JSR179_ADDRESSINFO_BUILDING_FLOOR) == KNI_TRUE)) {
                counter++;
            }
            /* AddressInfo_BUILDING_ROOM */
            if ((counter < (*landmark)->addressInfoFieldNumber) &&
                (fill_adressInfoField(landmarkObj, 
                            landmarkImplFieldID.AddressInfo_BUILDING_ROOM,
                            stringObj, 
                            &(*landmark)->fields[counter],
                            JSR179_ADDRESSINFO_BUILDING_ROOM) == KNI_TRUE)) {
                counter++;
            }
            /* AddressInfo_BUILDING_ZONE */
            if ((counter < (*landmark)->addressInfoFieldNumber) &&
                (fill_adressInfoField(landmarkObj, 
                            landmarkImplFieldID.AddressInfo_BUILDING_ZONE,
                            stringObj, 
                            &(*landmark)->fields[counter],
                            JSR179_ADDRESSINFO_BUILDING_ZONE) == KNI_TRUE)) {
                counter++;
            }
            /* AddressInfo_CROSSING1 */
            if ((counter < (*landmark)->addressInfoFieldNumber) &&
                (fill_adressInfoField(landmarkObj, 
                            landmarkImplFieldID.AddressInfo_CROSSING1,
                            stringObj, 
                            &(*landmark)->fields[counter],
                            JSR179_ADDRESSINFO_CROSSING1) == KNI_TRUE)) {
                counter++;
            }
            /* AddressInfo_CROSSING2 */
            if ((counter < (*landmark)->addressInfoFieldNumber) &&
                (fill_adressInfoField(landmarkObj, 
                            landmarkImplFieldID.AddressInfo_CROSSING2,
                            stringObj, 
                            &(*landmark)->fields[counter],
                            JSR179_ADDRESSINFO_CROSSING2) == KNI_TRUE)) {
                counter++;
            }
            /* AddressInfo_URL */
            if ((counter < (*landmark)->addressInfoFieldNumber) &&
                (fill_adressInfoField(landmarkObj, 
                            landmarkImplFieldID.AddressInfo_URL,
                            stringObj, 
                            &(*landmark)->fields[counter],
                            JSR179_ADDRESSINFO_URL) == KNI_TRUE)) {
                counter++;
            }
            /* AddressInfo_PHONE_NUMBER */
            if ((counter < (*landmark)->addressInfoFieldNumber) &&
                (fill_adressInfoField(landmarkObj, 
                            landmarkImplFieldID.AddressInfo_PHONE_NUMBER,
                            stringObj, 
                            &(*landmark)->fields[counter],
                            JSR179_ADDRESSINFO_PHONE_NUMBER) == KNI_TRUE)) {
                counter++;
            }
    }
    return KNI_TRUE;
}
