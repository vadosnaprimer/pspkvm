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

#include <string.h>

#include <kni.h>

#include <midpError.h>
#include <midpMalloc.h>
#include <midpUtilUTF.h>

#include "javacall_pim.h"

#define OUT_BUFFER_LEN      256
#define DATA_BUFFER_LEN     JAVACALL_PIM_MAX_BUFFER_SIZE
#define CATEGORIES_MAX_LEN  (DATA_BUFFER_LEN / 3)

#define ALLOCATE_OUTPUT_BUFFER()  midpMalloc(OUT_BUFFER_LEN * sizeof(javacall_utf16))
#define ALLOCATE_DATA_BUFFER()    midpMalloc(DATA_BUFFER_LEN * sizeof(javacall_utf16))

#define PIMPROXY_FIELD_NAME_DATA    "dataHandler"
#define PIMPROXY_FIELD_NAME_COUNTER "itemCounter"

typedef enum {
    DATA_BUFFER_EMPTY,
    DATA_BUFFER_ITEM,
    DATA_BUFFER_FIELDS,
    DATA_BUFFER_ATTRIBUTES
} data_buffer_state;

static const struct list_table {
    char* field_name;
    javacall_pim_type type;
} list_types[] = {
    {"CONTACT_LIST", JAVACALL_PIM_TYPE_CONTACT},
    {"EVENT_LIST",   JAVACALL_PIM_TYPE_EVENT},
    {"TODO_LIST",    JAVACALL_PIM_TYPE_TODO}
};

static const struct mode_table {
    char* field_name;
    javacall_pim_open_mode mode;
} open_modes[] = {
    {"READ_ONLY",    JAVACALL_PIM_OPEN_MODE_READ_ONLY},
    {"WRITE_ONLY",   JAVACALL_PIM_OPEN_MODE_WRITE_ONLY},
    {"READ_WRITE",   JAVACALL_PIM_OPEN_MODE_READ_WRITE}
};

static javacall_pim_open_mode convert_open_mode(int openMode) {
    javacall_pim_open_mode mode = JAVACALL_PIM_OPEN_MODE_READ_ONLY;
    jfieldID            listConstID;
    unsigned            index;

    KNI_StartHandles(1);
    KNI_DeclareHandle(pimClass);

    KNI_FindClass("javax/microedition/pim/PIM", pimClass);
    if (!KNI_IsNullHandle(pimClass)) {
        for (index = 0;
             index < sizeof(open_modes) / sizeof(struct mode_table);
             index++) {
            listConstID = KNI_GetStaticFieldID(pimClass, open_modes[index].field_name, "I");
            if (openMode == KNI_GetStaticIntField(pimClass, listConstID)) {
                mode = open_modes[index].mode;
                break;
            }
        }
    }
    KNI_EndHandles();
    return mode;
}

static javacall_pim_type convert_list_type(int listType) {
    javacall_pim_type type = 0;
    jfieldID       listConstID;
    unsigned       index;

    KNI_StartHandles(1);
    KNI_DeclareHandle(pimClass);

    KNI_FindClass("javax/microedition/pim/PIM", pimClass);
    if (!KNI_IsNullHandle(pimClass)) {
        for (index = 0;
             index < sizeof(list_types) / sizeof(struct list_table);
             index++) {
            listConstID = KNI_GetStaticFieldID(pimClass, list_types[index].field_name, "I");
            if (listType == KNI_GetStaticIntField(pimClass, listConstID)) {
                type = list_types[index].type;
                break;
            }
        }
    }
    KNI_EndHandles();
    return type;
}
	
/* Gets item's data and categories from Java objects. */
static javacall_result
extract_item_data(jobject data_array,
                             unsigned char **data_buffer,
                             jobject categories,
                             javacall_utf16 **cats_buffer) {
    int data_length;
    int cats_length;
    
    if (KNI_IsNullHandle(data_array)) {
        return JAVACALL_FAIL;
    }
    data_length = KNI_GetArrayLength(data_array);
    *data_buffer = midpMalloc(data_length + 1);
    if (*data_buffer == NULL) {
        return JAVACALL_FAIL;
    }

    KNI_GetRawArrayRegion(data_array, 0, data_length, *data_buffer);
    *(*data_buffer + data_length) = 0;
    
    if (!KNI_IsNullHandle(categories)) {
        cats_length = KNI_GetStringLength(categories);
        *cats_buffer = midpMalloc((cats_length + 1) * sizeof(javacall_utf16));
    }
    else {
        cats_length = 0;
    }
    if (cats_length && *cats_buffer == NULL) {
        return JAVACALL_FAIL;
    }
    if (cats_length) {
        KNI_GetStringRegion(categories, 0, cats_length, *cats_buffer);
        *(*cats_buffer + cats_length) = 0;
    }
    return 1;
}

static int javacall_string_len(javacall_utf16* string) {
    javacall_utf16* cur;
    
    for (cur = string; *cur; cur++) {}
    return (cur - string);
}

static int
getIndexOfDelimeter(javacall_utf16* string) {
    javacall_utf16* current;
    
    for (current = string;
         *current != 0 && *current != JAVACALL_PIM_STRING_DELIMITER;
         current++) {
    }
    return current - string;
}

static char *
extractDefaultListName(javacall_utf16 *output_buffer) {
    char* defaultListName = NULL;
    int defaultNameLength = getIndexOfDelimeter(output_buffer);
    int utf8NameLen;

    if (defaultNameLength == -1) {
        return NULL;
    }
    if (OUT_BUFFER_LEN < defaultNameLength  +
            (defaultNameLength * UTF_8_PER_UCS_2_CHAR) / sizeof(javacall_utf16) + 1) {
        return NULL;
    }
    defaultListName = (unsigned char *)(output_buffer + defaultNameLength);
    utf8NameLen = midpUTF8Encode(output_buffer, defaultNameLength,
                                    defaultListName,
                                    (OUT_BUFFER_LEN - defaultNameLength) * sizeof(javacall_utf16));
    if (utf8NameLen <= 0) {
        return NULL;
    }
    *(defaultListName + utf8NameLen) = 0;
    return defaultListName;
}

/**
 * Returns default list name for a given list type
 */
static char*
getDefaultListName(javacall_pim_type listType) {
    char* defaultName = NULL;
    javacall_utf16* output_buffer = ALLOCATE_OUTPUT_BUFFER();

    if (output_buffer != NULL) {
        if (javacall_pim_get_lists(listType,
                                output_buffer, OUT_BUFFER_LEN) == JAVACALL_OK) {
            defaultName = extractDefaultListName(output_buffer);
        }
        midpFree(output_buffer);
    }
    return defaultName;
}

/**
 * Returns defaut contact list name when Java calls
 * Configuration.getProperty("DefaultContactList")
 * @retval default name for contact list
 */
char*
getDefaultContactList(void) {
    return getDefaultListName(JAVACALL_PIM_TYPE_CONTACT);
}

/**
 * Returns defaut event list name when Java calls
 * Configuration.getProperty("DefaultEventList")
 * @retval default name for event list
 */
char*
getDefaultEventList(void) {
    return getDefaultListName(JAVACALL_PIM_TYPE_EVENT);
}

/**
 * Returns defaut todo list name when Java calls
 * Configuration.getProperty("DefaultTodoList")
 * @retval default name for todo list
 */
char*
getDefaultTodoList(void) {
    return getDefaultListName(JAVACALL_PIM_TYPE_TODO);
}

/**
 * Returns field ID for a given field
 * @param name name of a field
 * @param pObject pointer to object
 * @return field ID
 */
static jfieldID
getFieldID(char* name, jobject* pObject) {
    jfieldID fieldID;
    KNI_StartHandles(1);
    KNI_DeclareHandle(classHandle);
    
    KNI_GetThisPointer(*pObject);
    KNI_GetObjectClass(*pObject, classHandle);
    fieldID = KNI_GetFieldID(classHandle, name, "I");
    
    KNI_EndHandles();
    return fieldID;
}

/**
 * Sets value of field
 * @param name name of a field
 * @param value value to be set
 */
static void
setFieldValue(char* name, int value) {
    jfieldID fieldID;
    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);
    
    fieldID = getFieldID(name, &objectHandle);
    KNI_SetIntField(objectHandle, fieldID, (jint) value);
    
    KNI_EndHandles();
}

/**
 * Gets value of a filed
 * @param name name of a field
 * @return field value
 */
static int
getFieldValue(char* name) {
    jfieldID fieldID;
    int value;
    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);
    
    fieldID = getFieldID(name, &objectHandle);
    value = KNI_GetIntField(objectHandle, fieldID);
    
    KNI_EndHandles();
    return value;
}

KNIEXPORT KNI_RETURNTYPE_INT
Java_com_sun_kvem_midp_pim_PIMProxy_getListNamesCount0(void) {
    javacall_pim_type listType;
    javacall_utf16 *current;
    int namesCount = 0;
    javacall_utf16* output_buffer = ALLOCATE_OUTPUT_BUFFER();
    
    if (output_buffer != NULL) {
        listType = convert_list_type(KNI_GetParameterAsInt(1));
        if (listType && javacall_pim_get_lists(listType, output_buffer, OUT_BUFFER_LEN) == JAVACALL_OK) {
            for (current = output_buffer, namesCount = 1;
                *current;
                current++) {
                if (*current == JAVACALL_PIM_STRING_DELIMITER) {
                    namesCount++;
                }
            }
            if (current == output_buffer) {
                namesCount = 0;
            }
        }
    }
    else {
        KNI_ThrowNew(midpOutOfMemoryError, NULL);
        setFieldValue(PIMPROXY_FIELD_NAME_DATA, 0);
    }
    if (output_buffer != NULL) {
        if (namesCount > 0) {
            setFieldValue(PIMPROXY_FIELD_NAME_COUNTER, namesCount);
            setFieldValue(PIMPROXY_FIELD_NAME_DATA, (int)output_buffer);
        }
        else {
            midpFree(output_buffer);
        }
    }

    KNI_ReturnInt(namesCount);
}

KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_kvem_midp_pim_PIMProxy_getListNames0(void) {
    int namesCount = getFieldValue(PIMPROXY_FIELD_NAME_COUNTER);
    javacall_utf16* output_buffer = 
        (javacall_utf16 *)getFieldValue(PIMPROXY_FIELD_NAME_DATA);
    
    KNI_StartHandles(2);
    KNI_DeclareHandle(listNames);
    KNI_DeclareHandle(name);
    
    KNI_GetParameterAsObject(1, listNames);
    
    if (namesCount > 0 &&
        namesCount == KNI_GetArrayLength(listNames) &&
        output_buffer) {
        javacall_utf16 *first, *current;
        int filledCount = 0;

        first = current = output_buffer;
        do {
            if (*current == JAVACALL_PIM_STRING_DELIMITER || 
                *(current + 1) == 0) {
                KNI_NewString(first, current - first + 1, name);
                KNI_SetObjectArrayElement(listNames, filledCount, name);
                first = current + 1;
                filledCount ++;
            }
          current++;
        } while(*current && filledCount < namesCount);
    }
    if (output_buffer) {
        midpFree(output_buffer);
    }
    setFieldValue(PIMPROXY_FIELD_NAME_DATA, 0);
    setFieldValue(PIMPROXY_FIELD_NAME_COUNTER, 0);

    KNI_EndHandles();
}

KNIEXPORT KNI_RETURNTYPE_INT
Java_com_sun_kvem_midp_pim_PIMProxy_listOpen0(void) {
    int listType, openMode;
    int listNameLength;
    javacall_handle listHandle = (javacall_handle)0;
    javacall_utf16 *list_name = NULL;
    
    KNI_StartHandles(1);
    KNI_DeclareHandle(listName);
    
    listType = convert_list_type(KNI_GetParameterAsInt(1));
    KNI_GetParameterAsObject(2, listName);
    openMode = convert_open_mode(KNI_GetParameterAsInt(3));
    
    if (!KNI_IsNullHandle(listName)) {
        listNameLength = KNI_GetStringLength(listName);
        list_name = midpMalloc((listNameLength + 1) * sizeof(javacall_utf16));
        if (list_name != NULL) {
            KNI_GetStringRegion(listName, 0, listNameLength, list_name);
            list_name[listNameLength] = 0;
        }
        else {
            KNI_ThrowNew(midpOutOfMemoryError, NULL);
        }
    }
    javacall_pim_list_open(listType, list_name, openMode, &listHandle);
    if (list_name != NULL) {
        midpFree(list_name);
    }

    KNI_EndHandles();
    KNI_ReturnInt(listHandle);
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
Java_com_sun_kvem_midp_pim_PIMProxy_getNextItemDescription0(void) {
    int listHandle;
    javacall_result result = JAVACALL_FAIL;
    char* data_buffer = ALLOCATE_DATA_BUFFER();
    javacall_handle item_handle;

    KNI_StartHandles(1);
    KNI_DeclareHandle(dscrArray);

    listHandle = KNI_GetParameterAsInt(1);
    KNI_GetParameterAsObject(2, dscrArray);

    if (KNI_GetArrayLength(dscrArray) >= 4 &&
        data_buffer != NULL) {
        result = javacall_pim_list_get_next_item((javacall_handle)listHandle, 
                                                 data_buffer,
                                                 DATA_BUFFER_LEN - CATEGORIES_MAX_LEN,
                                                 (javacall_utf16 *)(data_buffer + DATA_BUFFER_LEN - CATEGORIES_MAX_LEN),
                                                 CATEGORIES_MAX_LEN / sizeof(javacall_utf16),
                                                 &item_handle);
        if (result == JAVACALL_OK) {
            KNI_SetIntArrayElement(dscrArray, 0, (jint)item_handle);
            KNI_SetIntArrayElement(dscrArray, 1, strlen(data_buffer));
            KNI_SetIntArrayElement(dscrArray, 2, 1);
            KNI_SetIntArrayElement(dscrArray, 3, (jint)data_buffer);
        }
        else {
            midpFree(data_buffer);
        }
    }
    
    KNI_EndHandles();
    KNI_ReturnBoolean(result == JAVACALL_OK ? 1 : 0);
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
Java_com_sun_kvem_midp_pim_PIMProxy_listClose0(void) {
    int listHandle;
    javacall_result result;

    listHandle = KNI_GetParameterAsInt(1);
    result = javacall_pim_list_close((javacall_handle)listHandle);
    
    KNI_ReturnBoolean(result == JAVACALL_OK ? 1 : 0);
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
Java_com_sun_kvem_midp_pim_PIMProxy_getNextItemData0(void) {
    javacall_handle itemHandle;
    int result = 0;
    char *data_buffer;

    KNI_StartHandles(1);
    KNI_DeclareHandle(dataArray);
    KNI_GetParameterAsObject(2, dataArray);
    itemHandle = (javacall_handle)KNI_GetParameterAsInt(1);
    data_buffer = (char *)KNI_GetParameterAsInt(3);
    
    if (data_buffer != NULL) {
        KNI_SetRawArrayRegion(dataArray, 0, KNI_GetArrayLength(dataArray), data_buffer);
        result = 1;
    }
    
    KNI_EndHandles();
    KNI_ReturnBoolean(result);
}

KNIEXPORT KNI_RETURNTYPE_OBJECT
Java_com_sun_kvem_midp_pim_PIMProxy_getItemCategories0(void) {
    javacall_handle itemHandle;
    char *data_buffer;    

    KNI_StartHandles(1);
    KNI_DeclareHandle(categories);
    itemHandle = (javacall_handle)KNI_GetParameterAsInt(1);
    data_buffer = (char *)KNI_GetParameterAsInt(2);
    
    if (data_buffer != NULL) {
        javacall_utf16* last_cats = (javacall_utf16 *)(data_buffer + DATA_BUFFER_LEN - CATEGORIES_MAX_LEN);
    
        KNI_NewString(last_cats, javacall_string_len(last_cats), categories);
        midpFree(data_buffer);
    }
    else {
        KNI_ReleaseHandle(categories);
    }
    
    KNI_EndHandlesAndReturnObject(categories);
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
Java_com_sun_kvem_midp_pim_PIMProxy_commitItemData0(void) {
    javacall_handle  listHandle, itemHandle;
    javacall_result  result = JAVACALL_FAIL;
    javacall_utf16 *cats = NULL;
    char* data_buffer = NULL;

    KNI_StartHandles(2);
    KNI_DeclareHandle(dataArray);
    KNI_DeclareHandle(categories);
    
    listHandle = (javacall_handle)KNI_GetParameterAsInt(1);
    itemHandle = (javacall_handle)KNI_GetParameterAsInt(2);
    KNI_GetParameterAsObject(3, dataArray);
    KNI_GetParameterAsObject(4, categories);
            
    if (extract_item_data(dataArray, &data_buffer, categories, &cats)) {
        result = javacall_pim_list_modify_item(listHandle, itemHandle, data_buffer, cats);
    }
    if (data_buffer != NULL) {
        midpFree(data_buffer);
    }
    if (cats != NULL) {
        midpFree(cats);
    }
    
    KNI_EndHandles();
    KNI_ReturnBoolean(result == JAVACALL_OK);
}

KNIEXPORT KNI_RETURNTYPE_INT
Java_com_sun_kvem_midp_pim_PIMProxy_addItem0(void) {
    javacall_handle listHandle;
    javacall_handle itemHandle = NULL;
    javacall_utf16 *cats = NULL;
    char* data_buffer = NULL;

    KNI_StartHandles(2);
    KNI_DeclareHandle(dataArray);
    KNI_DeclareHandle(categories);
    
    listHandle = (javacall_handle)KNI_GetParameterAsInt(1);
    KNI_GetParameterAsObject(2, dataArray);
    KNI_GetParameterAsObject(3, categories);
        
    if (extract_item_data(dataArray, &data_buffer,
                                categories, &cats)) {    
        javacall_pim_list_add_item(listHandle, data_buffer, cats, &itemHandle);
    }

    if (data_buffer != NULL) {
        midpFree(data_buffer);
    }
    if (cats != NULL) {
        midpFree(cats);
    }
    
    KNI_EndHandles();
    KNI_ReturnInt(itemHandle);
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
Java_com_sun_kvem_midp_pim_PIMProxy_removeItem0(void) {
    javacall_handle listHandle, itemHandle;
    javacall_result result;

    listHandle = (javacall_handle)KNI_GetParameterAsInt(1);
    itemHandle = (javacall_handle)KNI_GetParameterAsInt(2);
        
    result = javacall_pim_list_remove_item(listHandle, itemHandle);

    KNI_ReturnBoolean(result == JAVACALL_OK);
}

KNIEXPORT KNI_RETURNTYPE_OBJECT
Java_com_sun_kvem_midp_pim_PIMProxy_getListCategories0(void) {
    javacall_handle listHandle;
    javacall_utf16* data_buffer = ALLOCATE_OUTPUT_BUFFER();

    KNI_StartHandles(1);
    KNI_DeclareHandle(categories);

    listHandle = (javacall_handle)KNI_GetParameterAsInt(1);
    
    if (data_buffer != NULL) {
        if (javacall_pim_list_get_categories(listHandle, data_buffer, OUT_BUFFER_LEN) == JAVACALL_OK) {
            KNI_NewString(data_buffer, javacall_string_len(data_buffer), categories);
        }
        else {
            KNI_ReleaseHandle(categories);
        }
        midpFree(data_buffer);
    }
    else {
        KNI_ThrowNew(midpOutOfMemoryError, NULL);
    }
    KNI_EndHandlesAndReturnObject(categories);
}

KNIEXPORT KNI_RETURNTYPE_INT
Java_com_sun_kvem_midp_pim_PIMProxy_getListMaxCategories0(void) {
    javacall_handle listHandle;
    int max_categories;

    listHandle = (javacall_handle)KNI_GetParameterAsInt(1);
        
    max_categories = javacall_pim_list_max_categories(listHandle);

    KNI_ReturnInt(max_categories);
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
Java_com_sun_kvem_midp_pim_PIMProxy_addListCategory0(void) {
    javacall_handle listHandle;
    javacall_result result = JAVACALL_FAIL;
    int             cat_len;

    KNI_StartHandles(1);
    KNI_DeclareHandle(category);

    listHandle = (javacall_handle)KNI_GetParameterAsInt(1);
    KNI_GetParameterAsObject(2, category);
        
    if (!KNI_IsNullHandle(category)) {
        javacall_utf16* category_str;
        
        cat_len = KNI_GetStringLength(category);
        category_str = midpMalloc((cat_len + 1) * sizeof(javacall_utf16));
        if (category_str != NULL) {
            KNI_GetStringRegion(category, 0, cat_len, category_str);
            category_str[cat_len] = 0;

            result = javacall_pim_list_add_category(listHandle, category_str);
            midpFree(category_str);
        }
        else {
            KNI_ThrowNew(midpOutOfMemoryError, NULL);
        }
    }

    KNI_EndHandles();
    KNI_ReturnBoolean(result == JAVACALL_OK);
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
Java_com_sun_kvem_midp_pim_PIMProxy_deleteListCategory0(void) {
    javacall_handle listHandle;
    javacall_result result = JAVACALL_FAIL;
    int             cat_len;

    KNI_StartHandles(1);
    KNI_DeclareHandle(category);

    listHandle = (javacall_handle)KNI_GetParameterAsInt(1);
    KNI_GetParameterAsObject(2, category);
        
    if (!KNI_IsNullHandle(category)) {
        javacall_utf16* category_str;
        
        cat_len = KNI_GetStringLength(category);
        category_str = midpMalloc((cat_len + 1) * sizeof(javacall_utf16));
        if (category_str != NULL) {
            KNI_GetStringRegion(category, 0, cat_len, category_str);
            category_str[cat_len] = 0;

            result = javacall_pim_list_remove_category(listHandle, category_str);
            midpFree(category_str);
        }
        else {
            KNI_ThrowNew(midpOutOfMemoryError, NULL);
        }
    }

    KNI_EndHandles();
    KNI_ReturnBoolean(result == JAVACALL_OK);
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
Java_com_sun_kvem_midp_pim_PIMProxy_renameListCategory0(void) {
    javacall_handle listHandle;
    javacall_result result = JAVACALL_FAIL;
    int             cur_cat_len, new_cat_len;

    KNI_StartHandles(2);
    KNI_DeclareHandle(cur_category);
    KNI_DeclareHandle(new_category);

    listHandle = (javacall_handle)KNI_GetParameterAsInt(1);
    KNI_GetParameterAsObject(2, cur_category);
    KNI_GetParameterAsObject(3, new_category);
        
    if (!KNI_IsNullHandle(cur_category) && !KNI_IsNullHandle(new_category)) {
        javacall_utf16* cur_category_str;
        javacall_utf16* new_category_str;
    
        cur_cat_len = KNI_GetStringLength(cur_category);
        cur_category_str = midpMalloc((cur_cat_len + 1) * sizeof(javacall_utf16));
        new_cat_len = KNI_GetStringLength(new_category);
        new_category_str = midpMalloc((new_cat_len + 1) * sizeof(javacall_utf16));
        if (cur_category_str != NULL &&
            new_category_str != NULL) {
            KNI_GetStringRegion(cur_category, 0, cur_cat_len, cur_category_str);
            KNI_GetStringRegion(new_category, 0, new_cat_len, new_category_str);
            cur_category_str[cur_cat_len] = 0;
            new_category_str[new_cat_len] = 0;

            result = javacall_pim_list_rename_category(listHandle, cur_category_str, new_category_str);
            
            
        }
        else {
            KNI_ThrowNew(midpOutOfMemoryError, NULL);
        }
        if (cur_category_str != NULL) {
            midpFree(cur_category_str);
        }
        if (new_category_str != NULL) {
            midpFree(new_category_str);
        }
    }

    KNI_EndHandles();
    KNI_ReturnBoolean(result == JAVACALL_OK);
}

KNIEXPORT KNI_RETURNTYPE_INT
Java_com_sun_kvem_midp_pim_PIMProxy_getFieldsCount0(void) {
    int fieldsCount = 0;
    javacall_pim_field* fields;

    fields = midpMalloc(JAVACALL_PIM_MAX_FIELDS * sizeof(javacall_pim_field));
    if (fields != NULL) {
        javacall_handle list_handle;
        KNI_StartHandles(1);
        KNI_DeclareHandle(arrayHandle);
        
        list_handle = (javacall_handle)KNI_GetParameterAsInt(1);
        KNI_GetParameterAsObject(2, arrayHandle);
        if (!KNI_IsNullHandle(arrayHandle) &&
             KNI_GetArrayLength(arrayHandle) > 0) {
            if (JAVACALL_OK == javacall_pim_list_get_fields(list_handle, fields, JAVACALL_PIM_MAX_FIELDS)) {
                while (fieldsCount < JAVACALL_PIM_MAX_FIELDS &&
                    fields[fieldsCount].id != JAVACALL_PIM_INVALID_ID) {
                    fieldsCount++;
                }
                KNI_SetIntArrayElement(arrayHandle, 0, (jint)fields);
            }
            else {
                midpFree(fields);
            }
        }
        else {
            midpFree(fields);
        }
        KNI_EndHandles();
    }
    else {
        KNI_ThrowNew(midpOutOfMemoryError, NULL);
    }

    KNI_ReturnInt(fieldsCount);
}

KNIEXPORT KNI_RETURNTYPE_INT
Java_com_sun_kvem_midp_pim_PIMProxy_getFieldLabelsCount0(void) {
    javacall_pim_field *fields = (javacall_pim_field *)KNI_GetParameterAsInt(3);
    int fieldIndex = KNI_GetParameterAsInt(2);
    int labelsCount = 0;

    if (fields == NULL) {
        KNI_ThrowNew(midpRuntimeException, NULL);
    } else {
        while (labelsCount < JAVACALL_PIM_MAX_ARRAY_ELEMENTS &&
               JAVACALL_PIM_INVALID_ID != fields[fieldIndex].arrayElements[labelsCount].id) {
            labelsCount++;
        }
    }

    KNI_ReturnInt(labelsCount);
}

#define GET_FIELDID(classHandle, id, fieldName, fieldType)      \
    if (id == NULL)                                             \
    {                                                           \
        id = KNI_GetFieldID(classHandle, fieldName, fieldType); \
        if (id == 0) {                                          \
            KNI_ThrowNew(midpNullPointerException,              \
                "Invalid romizer settings");                    \
        }                                                       \
    }

KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_kvem_midp_pim_PIMProxy_getFields0(void) {
    javacall_pim_field *fields = 
        (javacall_pim_field *)KNI_GetParameterAsInt(3);
    int descArraySize;
    int labelArraySize;
    int i, j;
    javacall_utf16 *str;
    jfieldID descFieldID          = NULL;
    jfieldID descDataTypeID       = NULL;
    jfieldID descLabelID          = NULL;
    jfieldID descLabelResourcesID = NULL;
    jfieldID descAttributesID     = NULL;
    jfieldID descMaxValuesID      = NULL;

    KNI_StartHandles(5);
    KNI_DeclareHandle(descArray);
    KNI_DeclareHandle(desc);
    KNI_DeclareHandle(descClass);
    KNI_DeclareHandle(label);
    KNI_DeclareHandle(labelArray);

    if (fields == NULL) {
        KNI_ThrowNew(midpRuntimeException, NULL);
    } else {
        KNI_GetParameterAsObject(2, descArray);
        descArraySize = KNI_GetArrayLength(descArray);
        KNI_FindClass("com/sun/kvem/midp/pim/PIMFieldDescriptor", descClass);
        if (KNI_IsNullHandle(descClass)) {
            KNI_ThrowNew(midpNullPointerException, "Invalid romizer settings");
        }
        GET_FIELDID(descClass, descFieldID,          "field",          "I")
        GET_FIELDID(descClass, descDataTypeID,       "dataType",       "I")
        GET_FIELDID(descClass, descLabelID,          "label",          "Ljava/lang/String;")
        GET_FIELDID(descClass, descLabelResourcesID, "labelResources", "[Ljava/lang/String;")
        GET_FIELDID(descClass, descAttributesID,     "attributes",     "J")
        GET_FIELDID(descClass, descMaxValuesID,      "maxValues",      "I")

        for (i = 0; i < descArraySize; i++) {
            KNI_GetObjectArrayElement(descArray, i, desc);
            KNI_SetIntField(desc, descFieldID, fields[i].id);
            KNI_SetIntField(desc, descDataTypeID, fields[i].type);
            KNI_SetIntField(desc, descAttributesID, fields[i].attributes);
            KNI_SetIntField(desc, descMaxValuesID, fields[i].maxValues);
            str = fields[i].label;
            KNI_NewString(str, javacall_string_len(str), label);
            KNI_SetObjectField(desc, descLabelID, label);
            KNI_GetObjectField(desc, descLabelResourcesID, labelArray);
            labelArraySize = KNI_GetArrayLength(labelArray);
            // should check array size and labels count here!!!
            for (j = 0; j < labelArraySize; j++) {
                str = fields[i].arrayElements[j].label;
                KNI_NewString(str, javacall_string_len(str), label);
                KNI_SetObjectArrayElement(labelArray, j, label);
            }
        }
        midpFree(fields);
    }
    KNI_EndHandles();
    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_INT
Java_com_sun_kvem_midp_pim_PIMProxy_getAttributesCount0(void) {
    javacall_pim_field_attribute *attributes;
    int attributesCount = 0;
    javacall_handle list_handle;

    attributes = midpMalloc(JAVACALL_PIM_MAX_ATTRIBUTES * sizeof(javacall_pim_field_attribute));
    if (attributes != NULL) {
        list_handle = (javacall_handle)KNI_GetParameterAsInt(1);
        if (JAVACALL_OK == javacall_pim_list_get_attributes(list_handle,
                attributes, JAVACALL_PIM_MAX_ATTRIBUTES)) {
            KNI_StartHandles(1);
            KNI_DeclareHandle(arrayHandle);
            
            KNI_GetParameterAsObject(2, arrayHandle);
            if (!KNI_IsNullHandle(arrayHandle) &&
                KNI_GetArrayLength(arrayHandle) > 0) {
                while (attributesCount < JAVACALL_PIM_MAX_ATTRIBUTES &&
                    attributes[attributesCount].id != JAVACALL_PIM_INVALID_ID) {
                    attributesCount++;
                }
                KNI_SetIntArrayElement(arrayHandle, 0, (jint)attributes);
            }
            else {
                midpFree(attributes);
            }
            KNI_EndHandles();
        }
        else {
            midpFree(attributes);
        }
    }
    else {
        KNI_ThrowNew(midpOutOfMemoryError, NULL);
    }

    KNI_ReturnInt(attributesCount);
}

KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_kvem_midp_pim_PIMProxy_getAttributes0(void) {
    javacall_pim_field_attribute *attributes =
        (javacall_pim_field_attribute *)KNI_GetParameterAsInt(3);
    int attrArraySize;
    int i;
    javacall_utf16 *str;
    jfieldID attrAttrID  = NULL;
    jfieldID attrLabelID = NULL;

    KNI_StartHandles(5);
    KNI_DeclareHandle(attrArray);
    KNI_DeclareHandle(attr);
    KNI_DeclareHandle(attrClass);
    KNI_DeclareHandle(label);
    KNI_DeclareHandle(attrObj);

    if (attributes == NULL) {
        KNI_ThrowNew(midpRuntimeException, NULL);
    } else {
        KNI_GetParameterAsObject(2, attrArray);
        attrArraySize = KNI_GetArrayLength(attrArray);
        if (attrArraySize > 0) {
            KNI_GetObjectArrayElement(attrArray, 0, attrObj);
            KNI_GetObjectClass(attrObj, attrClass);
            if (KNI_IsNullHandle(attrClass)) {
                KNI_ThrowNew(midpNullPointerException, "Invalid romizer settings");
            }
            else {
                GET_FIELDID(attrClass, attrAttrID,  "attr",  "I")
                GET_FIELDID(attrClass, attrLabelID, "label", "Ljava/lang/String;")

                for (i = 0; i < attrArraySize; i++) {
                    KNI_GetObjectArrayElement(attrArray, i, attr);
                    KNI_SetIntField(attr, attrAttrID, attributes[i].id);
                    str = attributes[i].label;
                    KNI_NewString(str, javacall_string_len(str), label);
                    KNI_SetObjectField(attr, attrLabelID, label);
                }
            }
        }
        midpFree(attributes);
    }
    KNI_EndHandles();
    KNI_ReturnVoid();
}
