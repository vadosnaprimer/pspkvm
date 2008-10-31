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
    
#include <pcsl_string.h>
#include <midpMalloc.h>
#include <javacall_location.h>
#include <javacall_landmarkstore.h>
#include <jsr179_location.h>

/* JAVADOC COMMENT ELIDED */
jsr179_result jsr179_landmark_add_to_landmarkstore(
        const pcsl_string landmarkStoreName, 
        const jsr179_landmark* landmark,
        const pcsl_string categoryName,
        jsr179_handle* /*OUT*/outLandmarkID) {

    javacall_utf16_string jLandmarkStoreName = NULL;
    javacall_utf16_string jCategoryName = NULL;
    javacall_landmarkstore_landmark *jLandmark = NULL;
    jint len;
    jint i;
    jsr179_result ret = JSR179_STATUSCODE_FAIL;
    javacall_handle jLandmarkID;

    if (pcsl_string_is_null(&(landmark->name)) || 
        pcsl_string_length(&(landmark->name)) > JAVACALL_LANDMARKSTORE_MAX_LANDMARK_NAME) {
            return JSR179_STATUSCODE_INVALID_ARGUMENT;
    }
    
    if (!pcsl_string_is_null(&landmarkStoreName)) {
        jLandmarkStoreName = (javacall_utf16_string)pcsl_string_get_utf16_data(&landmarkStoreName);
    }
    if (!pcsl_string_is_null(&categoryName)) {
        jCategoryName = (javacall_utf16_string)pcsl_string_get_utf16_data(&categoryName);
    }

    jLandmark = midpMalloc(SIZE_OF_LANDMARK_INFO(landmark->addressInfoFieldNumber));
    if (jLandmark != NULL) {
        /* create javacall data */
        memset(jLandmark, 0, SIZE_OF_LANDMARK_INFO(landmark->addressInfoFieldNumber));
        pcsl_string_convert_to_utf16(&landmark->name, jLandmark->name, JAVACALL_LANDMARKSTORE_MAX_LANDMARK_NAME, &len);
        if (pcsl_string_is_null(&(landmark->description))) {
            jLandmark->description[0] = 0x0000;
            jLandmark->description[1] = 0xFFFF;
        } else if (pcsl_string_length(&(landmark->description)) == 0){
            jLandmark->description[0] = 0x0000;
            jLandmark->description[1] = 0x0000;
        } else {
            pcsl_string_convert_to_utf16(&landmark->description, jLandmark->description, JAVACALL_LANDMARKSTORE_MAX_LANDMARK_DESCRIPTION, &len);
        }
        jLandmark->isValidCoordinate = landmark->isValidCoordinate;
        if (jLandmark->isValidCoordinate) {
            jLandmark->latitude = landmark->latitude;
            jLandmark->longitude = landmark->longitude;
            jLandmark->altitude = landmark->altitude;
            jLandmark->horizontalAccuracy = landmark->horizontalAccuracy;
            jLandmark->verticalAccuracy = landmark->verticalAccuracy;
        }
        jLandmark->addressInfoFieldNumber = landmark->addressInfoFieldNumber;

        for (i=0; i<jLandmark->addressInfoFieldNumber; i++) {
            jLandmark->fields[i].filedId = landmark->fields[i].fieldId;
            if (pcsl_string_is_null(&(landmark->fields[i].data))) {
                jLandmark->fields[i].data[0] = 0x0000;
                jLandmark->fields[i].data[1] = 0xFFFF;
            } else if (pcsl_string_length(&(landmark->fields[i].data)) == 0) {
                jLandmark->fields[i].data[0] = 0x0000;
                jLandmark->fields[i].data[1] = 0x0000;
            } else {
                pcsl_string_convert_to_utf16(&landmark->fields[i].data, jLandmark->fields[i].data, JAVACALL_LOCATION_MAX_ADDRESSINFO_FIELD, &len);
            }
        }
        /* call javacall function to add landmark */
        switch (javacall_landmarkstore_landmark_add_to_landmarkstore(jLandmarkStoreName, jLandmark, jCategoryName, &jLandmarkID)) {
            case JAVACALL_OK:
                ret = JSR179_STATUSCODE_OK;
                *outLandmarkID = jLandmarkID;
                break;
            case JAVACALL_INVALID_ARGUMENT:
                ret = JSR179_STATUSCODE_INVALID_ARGUMENT;
                break;
            default:
                ret = JSR179_STATUSCODE_FAIL;
                break;
        }
    }

    midpFree(jLandmark);
    pcsl_string_release_utf16_data(jLandmarkStoreName, &landmarkStoreName);
    pcsl_string_release_utf16_data(jCategoryName, &categoryName);

    return ret;
}

/* JAVADOC COMMENT ELIDED */
jsr179_result jsr179_landmark_add_to_category(
        const pcsl_string landmarkStoreName, 
        jsr179_handle landmarkID,
        const pcsl_string categoryName)  {

    javacall_utf16_string jLandmarkStoreName = NULL;
    javacall_utf16_string jCategoryName = NULL;
    javacall_handle jLandmarkID = (jsr179_handle)landmarkID;
    jsr179_result ret = JSR179_STATUSCODE_FAIL;

    jLandmarkStoreName = (javacall_utf16_string)pcsl_string_get_utf16_data(&landmarkStoreName);
    jCategoryName = (javacall_utf16_string)pcsl_string_get_utf16_data(&categoryName);

    switch (javacall_landmarkstore_landmark_add_to_category(jLandmarkStoreName, 
        jLandmarkID, jCategoryName)) {
        case JAVACALL_OK:
            ret = JSR179_STATUSCODE_OK;
            break;
        case JAVACALL_INVALID_ARGUMENT:
            ret = JSR179_STATUSCODE_INVALID_ARGUMENT;
            break;
    }

    pcsl_string_release_utf16_data(jLandmarkStoreName, &landmarkStoreName);
    pcsl_string_release_utf16_data(jCategoryName, &categoryName);

    return ret;
}

/* JAVADOC COMMENT ELIDED */
jsr179_result jsr179_landmark_update(
        const pcsl_string landmarkStoreName, 
        jsr179_handle landmarkID,
        const jsr179_landmark* landmark) {

    javacall_utf16_string jLandmarkStoreName = NULL;
    javacall_landmarkstore_landmark *jLandmark = midpMalloc(SIZE_OF_LANDMARK_INFO(landmark->addressInfoFieldNumber));
    jint len;
    jint i;
    jsr179_result ret = JSR179_STATUSCODE_FAIL;
    javacall_handle jLandmarkID = (javacall_handle)landmarkID;

    if (!pcsl_string_is_null(&landmarkStoreName)) {
        jLandmarkStoreName = (javacall_utf16_string)pcsl_string_get_utf16_data(&landmarkStoreName);
    }

    if (jLandmark != NULL) {
        memset(jLandmark, 0, SIZE_OF_LANDMARK_INFO(landmark->addressInfoFieldNumber));
        /* create javacall data */
        pcsl_string_convert_to_utf16(&landmark->name, jLandmark->name, JAVACALL_LANDMARKSTORE_MAX_LANDMARK_NAME, &len);
        if (pcsl_string_is_null(&(landmark->description))) {
            jLandmark->description[0] = 0x0000;
            jLandmark->description[1] = 0xFFFF;
        } else if (pcsl_string_length(&(landmark->description)) == 0) {
            jLandmark->description[0] = 0x0000;
            jLandmark->description[0] = 0x0000;
        } else {
            pcsl_string_convert_to_utf16(&landmark->description, jLandmark->description, JAVACALL_LANDMARKSTORE_MAX_LANDMARK_DESCRIPTION, &len);
        }
        jLandmark->isValidCoordinate = landmark->isValidCoordinate;
        if (jLandmark->isValidCoordinate) {
            jLandmark->latitude = landmark->latitude;
            jLandmark->longitude = landmark->longitude;
            jLandmark->altitude = landmark->altitude;
            jLandmark->horizontalAccuracy = landmark->horizontalAccuracy;
            jLandmark->verticalAccuracy = landmark->verticalAccuracy;
        }
        jLandmark->addressInfoFieldNumber = landmark->addressInfoFieldNumber;

        for (i=0; i<jLandmark->addressInfoFieldNumber; i++) {
            jLandmark->fields[i].filedId = landmark->fields[i].fieldId;
            if (pcsl_string_is_null(&(landmark->fields[i].data))) {
                jLandmark->fields[i].data[0] = 0x0000;
                jLandmark->fields[i].data[1] = 0xFFFF;
            } else if (pcsl_string_length(&(landmark->fields[i].data)) == 0) {
                jLandmark->fields[i].data[0] = 0x0000;
                jLandmark->fields[i].data[1] = 0x0000;
            } else {
                pcsl_string_convert_to_utf16(&landmark->fields[i].data, jLandmark->fields[i].data, JAVACALL_LOCATION_MAX_ADDRESSINFO_FIELD, &len);
            }
        }
        /* call javacall function to add landmark */
        switch (javacall_landmarkstore_landmark_update(jLandmarkStoreName, jLandmarkID, jLandmark)) {
            case JAVACALL_OK:
                ret = JSR179_STATUSCODE_OK;
                break;
            case JAVACALL_INVALID_ARGUMENT:
                ret = JSR179_STATUSCODE_INVALID_ARGUMENT;
                break;
        }
    }

    midpFree(jLandmark);
    pcsl_string_release_utf16_data(jLandmarkStoreName, &landmarkStoreName);
    
    return ret;
}

/* JAVADOC COMMENT ELIDED */
jsr179_result jsr179_landmark_delete_from_landmarkstore(
        const pcsl_string landmarkStoreName,
        jsr179_handle landmarkID) {

    javacall_utf16_string jLandmarkStoreName = NULL;
    javacall_handle jLandmarkID = (jsr179_handle)landmarkID;
    jsr179_result ret = JSR179_STATUSCODE_FAIL;

    jLandmarkStoreName = (javacall_utf16_string)pcsl_string_get_utf16_data(&landmarkStoreName);

    if (JAVACALL_OK == javacall_landmarkstore_landmark_delete_from_landmarkstore(
                jLandmarkStoreName, jLandmarkID)) {
            ret = JSR179_STATUSCODE_OK;
    }

    pcsl_string_release_utf16_data(jLandmarkStoreName, &landmarkStoreName);

    return ret;
}
        
/* JAVADOC COMMENT ELIDED */
jsr179_result jsr179_landmark_delete_from_category(
        const pcsl_string landmarkStoreName,
        jsr179_handle landmarkID,
        const pcsl_string categoryName) {

    javacall_utf16_string jLandmarkStoreName = NULL;
    javacall_utf16_string jCategoryName = NULL;
    javacall_handle jLandmarkID = (jsr179_handle)landmarkID;
    jsr179_result ret = JSR179_STATUSCODE_FAIL;

    jLandmarkStoreName = (javacall_utf16_string)pcsl_string_get_utf16_data(&landmarkStoreName);
    jCategoryName = (javacall_utf16_string)pcsl_string_get_utf16_data(&categoryName);

    if (JAVACALL_OK == javacall_landmarkstore_landmark_delete_from_category(
                jLandmarkStoreName, jLandmarkID, jCategoryName)) {
        ret = JSR179_STATUSCODE_OK;
    }

    pcsl_string_release_utf16_data(jLandmarkStoreName, &landmarkStoreName);
    pcsl_string_release_utf16_data(jCategoryName, &categoryName);

    return ret;
}

/**
 * tempStore holds LandmarkStore name from javacall_landmarkstore_list_next
 */
static pcsl_string tempStore;

/* JAVADOC COMMENT ELIDED */
jsr179_result jsr179_landmarkstorelist_open(
        jsr179_handle* /*OUT*/pHandle) {

    javacall_handle jHandle;
    jsr179_result ret = JSR179_STATUSCODE_FAIL;

    if (JAVACALL_OK == javacall_landmarkstore_list_open(
                &jHandle)) {
        *pHandle = jHandle;
        return JSR179_STATUSCODE_OK;
    }

    return JSR179_STATUSCODE_FAIL;
}

/* JAVADOC COMMENT ELIDED */
void jsr179_landmarkstorelist_close(
        jsr179_handle handle) {

    pcsl_string_free(&tempStore);

    javacall_landmarkstore_list_close(handle);
}

/* JAVADOC COMMENT ELIDED */
jsr179_result jsr179_landmarkstorelist_next(
        jsr179_handle handle,
        pcsl_string** /*OUT*/pStoreName) {

    javacall_utf16_string storeName;
    jsr179_result ret = JSR179_STATUSCODE_FAIL;
    jint len;

    if (JAVACALL_OK == javacall_landmarkstore_list_next(
                (javacall_handle)handle, &storeName)) {
        pcsl_string_free(&tempStore);
        if (storeName != NULL) {
            for (len = 0; storeName[len] != 0; len++);
            pcsl_string_convert_from_utf16(storeName, len+1, &tempStore);
            *pStoreName = &tempStore;
        } else {
            *pStoreName = NULL;
        }
        ret = JSR179_STATUSCODE_OK;
    }
    return ret;
}

/**
 * tempLandmark holds landmark data received from javacall_landmarkstore_landmarklist_next
 */
jbyte tempLandmarkBuffer[SIZE_OF_LANDMARK_INFO(JAVACALL_LOCATION_MAX_ADDRESSINFO_FIELD)];
jsr179_landmark *tempLandmark = (jsr179_landmark *)tempLandmarkBuffer;

/* JAVADOC COMMENT ELIDED */
jsr179_result jsr179_landmarklist_open(
        const pcsl_string landmarkStoreName, 
        const pcsl_string categoryName, 
        jsr179_handle* /*OUT*/pHandle) {

    javacall_utf16_string jLandmarkStoreName = NULL;
    javacall_utf16_string jCategoryName = NULL;
    javacall_handle jHandle;
    jsr179_result ret = JSR179_STATUSCODE_FAIL;

    jLandmarkStoreName = (javacall_utf16_string)pcsl_string_get_utf16_data(&landmarkStoreName);
    jCategoryName = (javacall_utf16_string)pcsl_string_get_utf16_data(&categoryName);

    switch (javacall_landmarkstore_landmarklist_open(
                jLandmarkStoreName, jCategoryName, &jHandle)) {
        case JAVACALL_OK:
            *pHandle = jHandle;
            ret = JSR179_STATUSCODE_OK;
            break;
        case JAVACALL_INVALID_ARGUMENT:
            ret = JSR179_STATUSCODE_INVALID_ARGUMENT;
            break;
    }

    pcsl_string_release_utf16_data(jLandmarkStoreName, &landmarkStoreName);
    pcsl_string_release_utf16_data(jCategoryName, &categoryName);

    return ret;
}

/* JAVADOC COMMENT ELIDED */
void jsr179_landmarklist_close(
        jsr179_handle handle) {
    javacall_landmarkstore_landmarklist_close(handle);
}

/* JAVADOC COMMENT ELIDED */
jsr179_result jsr179_landmarklist_next(
        jsr179_handle handle,
        jint* /*OUT*/pLandmarkID,
        jsr179_landmark** /*OUT*/pLandmark) {

    jint jLandmarkID = 0;
    javacall_handle jHandle = handle;
    javacall_landmarkstore_landmark *jLandmark;
    int i;
    jsr179_result ret = JSR179_STATUSCODE_FAIL;

    switch (javacall_landmarkstore_landmarklist_next(
                jHandle, &jLandmarkID, &jLandmark)) {
        case JAVACALL_OK:
            /* Deallocate old temporary landmark buffer */
            if (jLandmark != NULL) {
                /* Allocate and fill temporary buffer. 
                   It will be deallocated immediatelly in the calling function */
                pcsl_string_convert_from_utf16(jLandmark->name, JAVACALL_LANDMARKSTORE_MAX_LANDMARK_NAME, &tempLandmark->name);
                if ((jLandmark->description[0] == 0x0000) && (jLandmark->description[1] == 0xFFFF)) {
                    tempLandmark->description = PCSL_STRING_NULL;
                } else {
                    pcsl_string_convert_from_utf16(jLandmark->description, JAVACALL_LANDMARKSTORE_MAX_LANDMARK_DESCRIPTION, &tempLandmark->description);
                }
                tempLandmark->isValidCoordinate = jLandmark->isValidCoordinate;
                if (tempLandmark->isValidCoordinate) {
                    tempLandmark->latitude = jLandmark->latitude;
                    tempLandmark->longitude = jLandmark->longitude;
                    tempLandmark->altitude = jLandmark->altitude;
                    tempLandmark->horizontalAccuracy = jLandmark->horizontalAccuracy;
                    tempLandmark->verticalAccuracy = jLandmark->verticalAccuracy;
                }
                tempLandmark->addressInfoFieldNumber = jLandmark->addressInfoFieldNumber;
                for (i=0; i<tempLandmark->addressInfoFieldNumber; i++) {
                    tempLandmark->fields[i].fieldId = jLandmark->fields[i].filedId;
                    if ((jLandmark->fields[i].data[0] == 0x0000) && (jLandmark->fields[i].data[1] == 0xFFFF)) {
                        tempLandmark->fields[i].data = PCSL_STRING_NULL;
                    } else {
                        pcsl_string_convert_from_utf16(jLandmark->fields[i].data, JAVACALL_LOCATION_MAX_ADDRESSINFO_FIELD, &tempLandmark->fields[i].data);
                    }
                }
                *pLandmark = tempLandmark;
            } else {
                *pLandmark = NULL;
            }
            *pLandmarkID = jLandmarkID;
            ret = JSR179_STATUSCODE_OK;
            break;
        case JAVACALL_INVALID_ARGUMENT:
            ret = JSR179_STATUSCODE_INVALID_ARGUMENT;
            break;
    }

    return ret;
}

/**
 * tempCategory holds Category name from javacall_landmarkstore_categorylist_next
 */
static pcsl_string tempCategory;

/* JAVADOC COMMENT ELIDED */
jsr179_result jsr179_categorylist_open(
        const pcsl_string landmarkStoreName,
        jsr179_handle* /*OUT*/pHandle) {

    javacall_utf16_string jLandmarkStoreName = NULL;
    javacall_handle jHandle;
    jsr179_result ret = JSR179_STATUSCODE_FAIL;

    jLandmarkStoreName = (javacall_utf16_string)pcsl_string_get_utf16_data(&landmarkStoreName);
    if (JAVACALL_OK == javacall_landmarkstore_categorylist_open(
                jLandmarkStoreName, &jHandle)) {
            *pHandle = jHandle;
            ret = JSR179_STATUSCODE_OK;
    }

    pcsl_string_release_utf16_data(jLandmarkStoreName, &landmarkStoreName);
 
    return ret;
}

/* JAVADOC COMMENT ELIDED */
void jsr179_categorylist_close(
        jsr179_handle handle) {

    pcsl_string_free(&tempCategory);

    javacall_landmarkstore_categorylist_close(handle);
}

/* JAVADOC COMMENT ELIDED */
jsr179_result jsr179_categorylist_next(
        jsr179_handle handle,
        pcsl_string** /*OUT*/pCategoryName) {

    javacall_utf16_string categoryName;
    jsr179_result ret = JSR179_STATUSCODE_FAIL;

    if (JAVACALL_OK == javacall_landmarkstore_categorylist_next(
                (javacall_handle)handle, &categoryName)) {
        pcsl_string_free(&tempCategory);
        pcsl_string_convert_from_utf16(categoryName, JAVACALL_LANDMARKSTORE_MAX_CATEGORY_NAME, &tempCategory);
        *pCategoryName = &tempCategory;
        ret = JSR179_STATUSCODE_OK;
    }
    return ret;
}

/* JAVADOC COMMENT ELIDED */
jsr179_result /*OPTIONAL*/ jsr179_landmarkstore_create(
        const pcsl_string landmarkStoreName) {

    javacall_utf16_string jLandmarkStoreName = (javacall_utf16_string)pcsl_string_get_utf16_data(&landmarkStoreName);
    jsr179_result ret = JSR179_STATUSCODE_FAIL;

    switch (javacall_landmarkstore_create(jLandmarkStoreName)) {
        case JAVACALL_OK:
            ret = JSR179_STATUSCODE_OK;
            break;
        case JAVACALL_INVALID_ARGUMENT:
            ret = JSR179_STATUSCODE_INVALID_ARGUMENT;
            break;
    }
        
    pcsl_string_release_utf16_data(jLandmarkStoreName, &landmarkStoreName);
    return ret;
}

/* JAVADOC COMMENT ELIDED */
jsr179_result /*OPTIONAL*/ jsr179_landmarkstore_delete(
        const pcsl_string landmarkStoreName) {

    javacall_utf16_string jLandmarkStoreName = (javacall_utf16_string)pcsl_string_get_utf16_data(&landmarkStoreName);
    jsr179_result ret = JSR179_STATUSCODE_FAIL;

    switch (javacall_landmarkstore_delete(jLandmarkStoreName)) {
        case JAVACALL_OK:
            ret = JSR179_STATUSCODE_OK;
            break;
        case JAVACALL_INVALID_ARGUMENT:
            ret = JSR179_STATUSCODE_INVALID_ARGUMENT;
            break;
    }
        
    pcsl_string_release_utf16_data(jLandmarkStoreName, &landmarkStoreName);
    return ret;
}

/* JAVADOC COMMENT ELIDED */
jsr179_result /*OPTIONAL*/ jsr179_category_add(
        const pcsl_string landmarkStoreName,
        const pcsl_string categoryName) {

    javacall_utf16_string jLandmarkStoreName = (javacall_utf16_string)pcsl_string_get_utf16_data(&landmarkStoreName);
    javacall_utf16_string jCategoryName = (javacall_utf16_string)pcsl_string_get_utf16_data(&categoryName);
    jsr179_result ret = JSR179_STATUSCODE_FAIL;

    switch (javacall_landmarkstore_category_add(jLandmarkStoreName, jCategoryName)) {
        case JAVACALL_OK:
            ret = JSR179_STATUSCODE_OK;
            break;
        case JAVACALL_INVALID_ARGUMENT:
            ret = JSR179_STATUSCODE_INVALID_ARGUMENT;
            break;
    }

    pcsl_string_release_utf16_data(jLandmarkStoreName, &landmarkStoreName);
    pcsl_string_release_utf16_data(jCategoryName, &categoryName);

    return ret;
}

/* JAVADOC COMMENT ELIDED */
jsr179_result /*OPTIONAL*/ jsr179_category_delete(
        const pcsl_string landmarkStoreName,
        const pcsl_string categoryName) {

    javacall_utf16_string jLandmarkStoreName = (javacall_utf16_string)pcsl_string_get_utf16_data(&landmarkStoreName);
    javacall_utf16_string jCategoryName = (javacall_utf16_string)pcsl_string_get_utf16_data(&categoryName);
    jsr179_result ret = JSR179_STATUSCODE_FAIL;

    switch (javacall_landmarkstore_category_delete(jLandmarkStoreName, jCategoryName)) {
        case JAVACALL_OK:
            ret = JSR179_STATUSCODE_OK;
            break;
        case JAVACALL_INVALID_ARGUMENT:
            ret = JSR179_STATUSCODE_INVALID_ARGUMENT;
            break;
    }

    pcsl_string_release_utf16_data(jLandmarkStoreName, &landmarkStoreName);
    pcsl_string_release_utf16_data(jCategoryName, &categoryName);

    return ret;
}

#ifdef __cplusplus
} //extern "C"
#endif

