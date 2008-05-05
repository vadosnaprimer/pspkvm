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
#include <jsr75_pim.h>

#define OUT_BUFFER_LEN      256
#define DATA_BUFFER_LEN     JSR75_PIM_MAX_BUFFER_SIZE
#define CATEGORIES_MAX_LEN  (DATA_BUFFER_LEN / 3)

typedef enum {
    data_buffer_empty,
    data_buffer_item,
    data_buffer_fields,
    data_buffer_attributes
} data_buffer_state;

static jchar output_buffer[OUT_BUFFER_LEN];
static unsigned char data_buffer[DATA_BUFFER_LEN];
static jsr75_handle last_handle = NULL;
static data_buffer_state current_buffer_state = data_buffer_empty;
static int fieldsCount = 0;
static int attributesCount = 0;

static const struct list_table {
    char* field_name;
    jsr75_pim_type type;
} list_types[] = {
    {"CONTACT_LIST", jsr75_pim_type_contact},
    {"EVENT_LIST",   jsr75_pim_type_event},
    {"TODO_LIST",    jsr75_pim_type_todo}
};

static const struct mode_table {
    char* field_name;
    jsr75_pim_open_mode mode;
} open_modes[] = {
    {"READ_ONLY",    jsr75_pim_open_mode_read_only},
    {"WRITE_ONLY",   jsr75_pim_open_mode_write_only},
    {"READ_WRITE",   jsr75_pim_open_mode_read_write}
};

static jsr75_pim_open_mode convert_open_mode(int openMode) {
    jsr75_pim_open_mode mode = jsr75_pim_open_mode_read_only;
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

static jsr75_pim_type convert_list_type(int listType) {
    jsr75_pim_type type = 0;
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
static int extract_item_data(jobject data_array, unsigned char *data_buffer, int data_buffer_len,
                             jobject categories, jchar *cats_buffer, int cats_buffer_len) {
    int data_length;
    int cats_length;
    
    if (KNI_IsNullHandle(data_array)) {
        return 0;
    }
    data_length = KNI_GetArrayLength(data_array);
    if (data_length >= data_buffer_len) {
        return 0;
    }

    KNI_GetRawArrayRegion(data_array, 0, data_length, data_buffer);
    data_buffer[data_length] = 0;
    
    if (!KNI_IsNullHandle(categories)) {
        cats_length = KNI_GetStringLength(categories);
    }
    else {
        cats_length = 0;
    }
    if (cats_length >= cats_buffer_len) {
        return 0;
    }
    if (cats_length) {
        KNI_GetStringRegion(categories, 0, cats_length, cats_buffer);
    }
    cats_buffer[cats_length] = 0;
    return 1;
}

static int jsr75_string_len(jchar* string) {
    jchar* cur;
    
    for (cur = string; *cur; cur++) {}
    return (cur - string);
}

KNIEXPORT KNI_RETURNTYPE_INT Java_com_sun_kvem_midp_pim_PIMProxy_getListNamesCount0(void) {
    jsr75_pim_type listType;
    jchar *current;
    int namesCount = 0;
    
    listType = convert_list_type(KNI_GetParameterAsInt(1));
    if (listType && jsr75_pim_get_lists(listType, output_buffer, OUT_BUFFER_LEN) == JSR75_STATUSCODE_OK) {
        for (current = output_buffer, namesCount = 1;
             *current;
             current++) {
            if (*current == JSR75_PIM_STRING_DELIMITER) {
                namesCount++;
            }
        }
        if (current == output_buffer) {
            namesCount = 0;
        }
    }

    KNI_ReturnInt(namesCount);
}

KNIEXPORT KNI_RETURNTYPE_VOID Java_com_sun_kvem_midp_pim_PIMProxy_getListNames0(void) {
    int namesCount;
    
    KNI_StartHandles(2);
    KNI_DeclareHandle(listNames);
    KNI_DeclareHandle(name);
    
    KNI_GetParameterAsObject(1, listNames);
    namesCount = KNI_GetArrayLength(listNames);
    
    if (namesCount > 0 && output_buffer) {
        jchar *first, *current;
        int filledCount = 0;

        first = current = output_buffer;
        do {
            if (*current == JSR75_PIM_STRING_DELIMITER || 
                *(current + 1) == 0) {
                KNI_NewString(first, current - first + 1, name);
                KNI_SetObjectArrayElement(listNames, filledCount, name);
                first = current + 1;
                filledCount ++;
            }
          current++;
        } while(*current && filledCount < namesCount);
    }

    KNI_EndHandles();
}

KNIEXPORT KNI_RETURNTYPE_INT Java_com_sun_kvem_midp_pim_PIMProxy_listOpen0(void) {
    int listType, openMode;
    int listNameLength;
    jsr75_handle listHandle = (jsr75_handle)0;
    jchar *list_name = NULL;
    
    KNI_StartHandles(1);
    KNI_DeclareHandle(listName);
    
    listType = convert_list_type(KNI_GetParameterAsInt(1));
    KNI_GetParameterAsObject(2, listName);
    openMode = convert_open_mode(KNI_GetParameterAsInt(3));
    
    if (!KNI_IsNullHandle(listName)) {
        listNameLength = KNI_GetStringLength(listName);
        if (listNameLength >= OUT_BUFFER_LEN) {
            KNI_ReturnInt(listHandle);
        }
        KNI_GetStringRegion(listName, 0, listNameLength, output_buffer);
        output_buffer[listNameLength] = 0;
        list_name = output_buffer;
    }

    jsr75_pim_list_open(listType, list_name, openMode, &listHandle);

    KNI_EndHandles();
    KNI_ReturnInt(listHandle);
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN Java_com_sun_kvem_midp_pim_PIMProxy_getNextItemDescription0(void) {
    int listHandle;
    JSR75_STATUSCODE result = JSR75_STATUSCODE_FAIL;

    KNI_StartHandles(1);
    KNI_DeclareHandle(dscrArray);

    listHandle = KNI_GetParameterAsInt(1);
    KNI_GetParameterAsObject(2, dscrArray);

    if (KNI_GetArrayLength(dscrArray) >= 3) {
        result = jsr75_pim_list_get_next_item((jsr75_handle)listHandle, 
                                                 data_buffer,
                                                 DATA_BUFFER_LEN - CATEGORIES_MAX_LEN,
                                                 (jchar *)(data_buffer + DATA_BUFFER_LEN - CATEGORIES_MAX_LEN),
                                                 CATEGORIES_MAX_LEN / sizeof(jchar),
                                                 &last_handle);
        if (result == JSR75_STATUSCODE_OK) {
            current_buffer_state = data_buffer_item;        
            KNI_SetIntArrayElement(dscrArray, 0, (jint)last_handle);
            KNI_SetIntArrayElement(dscrArray, 1, strlen(data_buffer));
            KNI_SetIntArrayElement(dscrArray, 2, 1);
        }
        else {
            current_buffer_state = data_buffer_empty;
        }
    }
    
    KNI_EndHandles();
    KNI_ReturnBoolean(result == JSR75_STATUSCODE_OK ? 1 : 0);
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN Java_com_sun_kvem_midp_pim_PIMProxy_listClose0(void) {
    int listHandle;
    JSR75_STATUSCODE result;

    listHandle = KNI_GetParameterAsInt(1);
    result = jsr75_pim_list_close((jsr75_handle)listHandle);
    
    KNI_ReturnBoolean(result == JSR75_STATUSCODE_OK ? 1 : 0);
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN Java_com_sun_kvem_midp_pim_PIMProxy_getNextItemData0(void) {
    jsr75_handle itemHandle;
    int result = 0;

    KNI_StartHandles(1);
    KNI_DeclareHandle(dataArray);
    KNI_GetParameterAsObject(2, dataArray);
    itemHandle = (jsr75_handle)KNI_GetParameterAsInt(1);
    
    if (itemHandle == last_handle && current_buffer_state == data_buffer_item) {
        KNI_SetRawArrayRegion(dataArray, 0, KNI_GetArrayLength(dataArray), data_buffer);
        result = 1;
    }
    
    KNI_EndHandles();
    KNI_ReturnBoolean(result);
}

KNIEXPORT KNI_RETURNTYPE_OBJECT Java_com_sun_kvem_midp_pim_PIMProxy_getItemCategories0(void) {
    jsr75_handle itemHandle;

    KNI_StartHandles(1);
    KNI_DeclareHandle(categories);
    itemHandle = (jsr75_handle)KNI_GetParameterAsInt(1);
    
    if (itemHandle == last_handle && current_buffer_state == data_buffer_item) {
        jchar* last_cats = (jchar *)(data_buffer + DATA_BUFFER_LEN - CATEGORIES_MAX_LEN);
    
        KNI_NewString(last_cats, jsr75_string_len(last_cats), categories);
    }
    else {
        KNI_ReleaseHandle(categories);
    }
    
    KNI_EndHandlesAndReturnObject(categories);
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN Java_com_sun_kvem_midp_pim_PIMProxy_commitItemData0(void) {
    jsr75_handle  listHandle, itemHandle;
    JSR75_STATUSCODE  result = JSR75_STATUSCODE_FAIL;
    jchar *cats = (jchar *)data_buffer + DATA_BUFFER_LEN - CATEGORIES_MAX_LEN;

    KNI_StartHandles(2);
    KNI_DeclareHandle(dataArray);
    KNI_DeclareHandle(categories);
    
    listHandle = (jsr75_handle)KNI_GetParameterAsInt(1);
    itemHandle = (jsr75_handle)KNI_GetParameterAsInt(2);
    KNI_GetParameterAsObject(3, dataArray);
    KNI_GetParameterAsObject(4, categories);
        
    current_buffer_state = data_buffer_empty;
    if (extract_item_data(dataArray, data_buffer, DATA_BUFFER_LEN - CATEGORIES_MAX_LEN,
                          categories, cats, CATEGORIES_MAX_LEN)) {
        result = jsr75_pim_list_modify_item(listHandle, itemHandle, data_buffer, cats);
    }

    KNI_EndHandles();
    KNI_ReturnBoolean(result == JSR75_STATUSCODE_OK);
}

KNIEXPORT KNI_RETURNTYPE_INT Java_com_sun_kvem_midp_pim_PIMProxy_addItem0(void) {
    jsr75_handle listHandle;
    jsr75_handle itemHandle = NULL;
    jchar *cats = (jchar *)data_buffer + DATA_BUFFER_LEN - CATEGORIES_MAX_LEN;

    KNI_StartHandles(2);
    KNI_DeclareHandle(dataArray);
    KNI_DeclareHandle(categories);
    
    listHandle = (jsr75_handle)KNI_GetParameterAsInt(1);
    KNI_GetParameterAsObject(2, dataArray);
        
    current_buffer_state = data_buffer_empty;
    if (extract_item_data(dataArray, data_buffer, DATA_BUFFER_LEN - CATEGORIES_MAX_LEN,
                          categories, cats, CATEGORIES_MAX_LEN)) {    
        jsr75_pim_list_add_item(listHandle, data_buffer, cats, &itemHandle);
    }

    KNI_EndHandles();
    KNI_ReturnInt(itemHandle);
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN Java_com_sun_kvem_midp_pim_PIMProxy_removeItem0(void) {
    jsr75_handle listHandle, itemHandle;
    JSR75_STATUSCODE result;

    listHandle = (jsr75_handle)KNI_GetParameterAsInt(1);
    itemHandle = (jsr75_handle)KNI_GetParameterAsInt(2);
        
    result = jsr75_pim_list_remove_item(listHandle, itemHandle);

    KNI_ReturnBoolean(result == JSR75_STATUSCODE_OK);
}

KNIEXPORT KNI_RETURNTYPE_OBJECT Java_com_sun_kvem_midp_pim_PIMProxy_getListCategories0(void) {
    jsr75_handle listHandle;

    KNI_StartHandles(1);
    KNI_DeclareHandle(categories);

    listHandle = (jsr75_handle)KNI_GetParameterAsInt(1);
    
    current_buffer_state = data_buffer_empty;
    if (jsr75_pim_list_get_categories(listHandle, (jchar *)data_buffer, DATA_BUFFER_LEN) == JSR75_STATUSCODE_OK) {
        KNI_NewString((jchar *)data_buffer, jsr75_string_len((jchar *)data_buffer), categories);
    }
    else {
        KNI_ReleaseHandle(categories);
    }
    KNI_EndHandlesAndReturnObject(categories);
}

KNIEXPORT KNI_RETURNTYPE_INT Java_com_sun_kvem_midp_pim_PIMProxy_getListMaxCategories0(void) {
    jsr75_handle listHandle;
    int max_categories;

    listHandle = (jsr75_handle)KNI_GetParameterAsInt(1);
        
    max_categories = jsr75_pim_list_max_categories(listHandle);

    KNI_ReturnInt(max_categories);
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN Java_com_sun_kvem_midp_pim_PIMProxy_addListCategory0(void) {
    jsr75_handle listHandle;
    JSR75_STATUSCODE result = JSR75_STATUSCODE_FAIL;
    int             cat_len;

    KNI_StartHandles(1);
    KNI_DeclareHandle(category);

    listHandle = (jsr75_handle)KNI_GetParameterAsInt(1);
    KNI_GetParameterAsObject(2, category);
        
    if (!KNI_IsNullHandle(category)) {
        cat_len = KNI_GetStringLength(category);
        if (cat_len * sizeof(jchar) < OUT_BUFFER_LEN) {
            jchar *category_str = (jchar *)output_buffer;
            KNI_GetStringRegion(category, 0, cat_len, category_str);
            category_str[cat_len] = 0;

            result = jsr75_pim_list_add_category(listHandle, category_str);
        }
    }

    KNI_EndHandles();
    KNI_ReturnBoolean(result == JSR75_STATUSCODE_OK);
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN Java_com_sun_kvem_midp_pim_PIMProxy_deleteListCategory0(void) {
    jsr75_handle listHandle;
    JSR75_STATUSCODE result = JSR75_STATUSCODE_FAIL;
    int             cat_len;

    KNI_StartHandles(1);
    KNI_DeclareHandle(category);

    listHandle = (jsr75_handle)KNI_GetParameterAsInt(1);
    KNI_GetParameterAsObject(2, category);
        
    if (!KNI_IsNullHandle(category)) {
        cat_len = KNI_GetStringLength(category);
        if (cat_len * sizeof(jchar) < OUT_BUFFER_LEN) {
            jchar *category_str = (jchar *)output_buffer;
            KNI_GetStringRegion(category, 0, cat_len, category_str);
            category_str[cat_len] = 0;

            result = jsr75_pim_list_remove_category(listHandle, category_str);
        }
    }

    KNI_EndHandles();
    KNI_ReturnBoolean(result == JSR75_STATUSCODE_OK);
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN Java_com_sun_kvem_midp_pim_PIMProxy_renameListCategory0(void) {
    jsr75_handle listHandle;
    JSR75_STATUSCODE result = JSR75_STATUSCODE_FAIL;
    int             cur_cat_len, new_cat_len;

    KNI_StartHandles(2);
    KNI_DeclareHandle(cur_category);
    KNI_DeclareHandle(new_category);

    listHandle = (jsr75_handle)KNI_GetParameterAsInt(1);
    KNI_GetParameterAsObject(2, cur_category);
    KNI_GetParameterAsObject(3, new_category);
        
    if (!KNI_IsNullHandle(cur_category) && !KNI_IsNullHandle(new_category)) {
        cur_cat_len = KNI_GetStringLength(cur_category);
        new_cat_len = KNI_GetStringLength(new_category);
        if (cur_cat_len * sizeof(jchar) < OUT_BUFFER_LEN &&
            new_cat_len * sizeof(jchar) < DATA_BUFFER_LEN) {
            jchar *cur_category_str = (jchar *)output_buffer;
            jchar *new_category_str = (jchar *)data_buffer;
            KNI_GetStringRegion(cur_category, 0, cur_cat_len, cur_category_str);
            KNI_GetStringRegion(new_category, 0, new_cat_len, new_category_str);
            cur_category_str[cur_cat_len] = 0;
            new_category_str[new_cat_len] = 0;

            result = jsr75_pim_list_rename_category(listHandle, cur_category_str, new_category_str);
        }
    }

    KNI_EndHandles();
    KNI_ReturnBoolean(result == JSR75_STATUSCODE_OK);
}

KNIEXPORT KNI_RETURNTYPE_INT Java_com_sun_kvem_midp_pim_PIMProxy_getFieldsCount0(void) {
    jsr75_pim_field *fields;

    fieldsCount = 0;

    last_handle = (jsr75_handle)KNI_GetParameterAsInt(1);
    if (JSR75_STATUSCODE_OK == jsr75_pim_list_get_fields(last_handle, (jsr75_pim_field *)data_buffer, JSR75_PIM_MAX_FIELDS)) {
        current_buffer_state = data_buffer_fields;
        fields = (jsr75_pim_field *)data_buffer;
        while (fieldsCount < JSR75_PIM_MAX_FIELDS &&
               fields[fieldsCount].id != JSR75_PIM_INVALID_ID) {
            fieldsCount++;
        }
    }
    else {
        current_buffer_state = data_buffer_empty;
    }

    KNI_ReturnInt(fieldsCount);
}

KNIEXPORT KNI_RETURNTYPE_INT Java_com_sun_kvem_midp_pim_PIMProxy_getFieldLabelsCount0(void) {
    jsr75_pim_field *fields;
    int fieldIndex = KNI_GetParameterAsInt(2);
    int labelsCount = 0;

    if ((jsr75_handle)KNI_GetParameterAsInt(1) != last_handle || 
         current_buffer_state != data_buffer_fields) {
        KNI_ThrowNew(midpRuntimeException, NULL);
    } else {
        fields = (jsr75_pim_field *)data_buffer;
        while (labelsCount < JSR75_PIM_MAX_ARRAY_ELEMENTS &&
               JSR75_PIM_INVALID_ID != fields[fieldIndex].arrayElements[labelsCount].id) {
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

KNIEXPORT KNI_RETURNTYPE_VOID Java_com_sun_kvem_midp_pim_PIMProxy_getFields0(void) {
    jsr75_pim_field *fields;
    int descArraySize;
    int labelArraySize;
    int i, j;
    jchar *str;
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

    if ((jsr75_handle)KNI_GetParameterAsInt(1) != last_handle ||
        current_buffer_state != data_buffer_fields) {
        KNI_ThrowNew(midpRuntimeException, NULL);
    } else {
        KNI_GetParameterAsObject(2, descArray);
        descArraySize = KNI_GetArrayLength(descArray);
        if (descArraySize < fieldsCount) {
            KNI_ThrowNew(midpRuntimeException, NULL);
        } else {
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

            fields = (jsr75_pim_field *)data_buffer;
            for (i = 0; i < fieldsCount; i++) {
                KNI_GetObjectArrayElement(descArray, i, desc);
                KNI_SetIntField(desc, descFieldID, fields[i].id);
                KNI_SetIntField(desc, descDataTypeID, fields[i].type);
                KNI_SetIntField(desc, descAttributesID, fields[i].attributes);
                KNI_SetIntField(desc, descMaxValuesID, fields[i].maxValues);
                str = fields[i].label;
                KNI_NewString(str, jsr75_string_len(str), label);
                KNI_SetObjectField(desc, descLabelID, label);
                KNI_GetObjectField(desc, descLabelResourcesID, labelArray);
                labelArraySize = KNI_GetArrayLength(labelArray);
                // should check array size and labels count here!!!
                for (j = 0; j < labelArraySize; j++) {
                    str = fields[i].arrayElements[j].label;
                    KNI_NewString(str, jsr75_string_len(str), label);
                    KNI_SetObjectArrayElement(labelArray, j, label);
                }
            }
        }
    }
    KNI_EndHandles();
    KNI_ReturnVoid();
}

KNIEXPORT KNI_RETURNTYPE_INT Java_com_sun_kvem_midp_pim_PIMProxy_getAttributesCount0(void) {
    jsr75_pim_field_attribute *attributes;

    attributesCount = 0;

    last_handle = (jsr75_handle)KNI_GetParameterAsInt(1);
    if (JSR75_STATUSCODE_OK == jsr75_pim_list_get_attributes(last_handle,
            (jsr75_pim_field_attribute *)data_buffer, JSR75_PIM_MAX_ATTRIBUTES)) {
        current_buffer_state = data_buffer_attributes;
        attributes = (jsr75_pim_field_attribute *)data_buffer;
        while (attributesCount < JSR75_PIM_MAX_FIELDS &&
               attributes[attributesCount].id != JSR75_PIM_INVALID_ID) {
            attributesCount++;
        }
    }
    else {
        current_buffer_state = data_buffer_empty;
    }

    KNI_ReturnInt(attributesCount);
}

KNIEXPORT KNI_RETURNTYPE_VOID Java_com_sun_kvem_midp_pim_PIMProxy_getAttributes0(void) {
    jsr75_pim_field_attribute *attributes;
    int attrArraySize;
    int i;
    jchar *str;
    jfieldID attrAttrID  = NULL;
    jfieldID attrLabelID = NULL;

    KNI_StartHandles(4);
    KNI_DeclareHandle(attrArray);
    KNI_DeclareHandle(attr);
    KNI_DeclareHandle(attrClass);
    KNI_DeclareHandle(label);

    if ((jsr75_handle)KNI_GetParameterAsInt(1) != last_handle ||
         current_buffer_state != data_buffer_attributes) {
        KNI_ThrowNew(midpRuntimeException, NULL);
    } else {
        KNI_GetParameterAsObject(2, attrArray);
        attrArraySize = KNI_GetArrayLength(attrArray);
        if (attrArraySize < attributesCount) {
            KNI_ThrowNew(midpRuntimeException, NULL);
        } else {
            KNI_FindClass("com/sun/kvem/midp/pim/PIMAttribute", attrClass);
            if (KNI_IsNullHandle(attrClass)) {
                KNI_ThrowNew(midpNullPointerException, "Invalid romizer settings");
            }
            GET_FIELDID(attrClass, attrAttrID,  "attr",  "I")
            GET_FIELDID(attrClass, attrLabelID, "label", "Ljava/lang/String;")

            attributes = (jsr75_pim_field_attribute *)data_buffer;
            for (i = 0; i < attributesCount; i++) {
                KNI_GetObjectArrayElement(attrArray, i, attr);
                KNI_SetIntField(attr, attrAttrID, attributes[i].id);
                str = attributes[i].label;
                KNI_NewString(str, jsr75_string_len(str), label);
                KNI_SetObjectField(attr, attrLabelID, label);
            }
        }
    }
    KNI_EndHandles();
    KNI_ReturnVoid();
}
