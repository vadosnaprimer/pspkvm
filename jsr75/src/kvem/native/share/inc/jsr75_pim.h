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
 
#ifndef __JSR75_PIM_H
#define __JSR75_PIM_H

#include <java_types.h>

#ifdef __cplusplus
extern "C" {
#endif

/**
 * @defgroup PIM JSR-75 Personal Information Management API
 * @ingroup stack
 */

/**
 *
 * @file
 * Header file for PIM native porting API of JSR-75.
 * @ingroup PIM
 * @brief #include <jsr75_pim.h>
 *
 * This API provide porting layer functions to access the handset address book, calendar and todo list.
 *
 * Compliant PIM API implementations:
 *   - MUST include all packages, classes, and interfaces described in this specification.
 *   - MUST provide support for at least one of the types of PIM lists defined in this 
 *   - API: either ContactList, EventList, or ToDoList.
 *   - MUST provide access to one or more actual PIM lists for each supported PIM list type.
 *   - MAY provide support for all types of PIM lists defined in this API: ContactList, EventList, and ToDoList.
 *   - MUST provide a security model for accessing the PIM APIs.
 *   - MUST support at least vCard 2.1 and vCalendar 1.0 data formats for importing and exporting items 
 *     via serial formats mechanism defined in javax.microedition.pim.PIM class. Implementations MUST 
 *     support Quoted-Printable and BASE64 encoding formats in import and export.
 *   - MUST support at least the UTF-8 character encoding for APIs that allow the application to define 
 *     character encodings.
 *   - MAY support other character encodings.
 *
 * The following functionality is provided by the porting layer:
 * 1.PIM List access:
 * Using the jsr75_pim_get_lists() user can retrieve the list of supported PIM list and according
 * to that to access the relevant PIM list using the jsr75_pim_list_open() and to close the PIM list
 * using the jsr75_pim_list_close().
 *
 * 2.Getting PIM items:
 * In order to retrieve the items from the PIM list that user need to open the relevant PIM list using
 * jsr75_pim_list_open() with the return list handle the user would than have to call 
 * jsr75_pim_list_get_next_item() in order to retrieve the next item in the list. 
 * Note: jsr75_pim_list_get_next_item() returns the following information for each item:
 *        - item's data - in vCard/vCalender format.
 *        - a list of categories that the current item is bind to.
 *        - a item handle which is a unique identifier to the item.
 * 
 * 3.Adding new items: 
 * Adding new items to the list also require to open the list first. with the returned list handle the user
 * will than need to call jsr75_pim_list_add_item() with the following information:
 *   - item's data - in vCard/vCalender format.
 *   - a list of categories that the current item is bind to.
 * upon successful completion the function will return a unique identifier to detect the new item.
 *
 * 4.Removing existing items: 
 * Existing item removal requires to open the PIM list first and with the return handle to call 
 * jsr75_pim_list_remove_item with both the PIM list and the item handles.
 *
 *
 * 5.Getting field information:
 * Field information is retrieve using the jsr75_pim_list_get_fields which returns the following
 * information about each field:
 *   - Field ID - A unique identifier for a field that matches one of the values of 
 *    (jsr75_pim_contact_field,jsr75_pim_event_field,jsr75_pim_todo)
 *   - Field Type - The storage type of the field us defined in (jsr75_pim_field_type).
 *   - Field Label - String label associated with the given field. 
 *   - Field Attributes - For contact field only all the field's attributes will be returned. 
 *   - Field Array element - For contact field only of type string all the information regarding array element
 *                           will be returned.
 *
 *
 *
 * 6.Getting categories information:
 * The porting layer API also defines set of APIs to query the categories for a given list 
 * and also APIs for adding a new category ,removing a category and renaming a category.
 *
 *
 */


/**
 * @def JSR75_PIM_MAX_ARRAY_ELEMENTS
 *
 * Maximum number of field array elements.
 * Used in jsr75_pim_field structure declaration.
 */
#define JSR75_PIM_MAX_ARRAY_ELEMENTS (7)

/**
 * @def JSR75_PIM_MAX_ATTRIBUTES
 *
 * Maximum number of attributes.
 */
#define JSR75_PIM_MAX_ATTRIBUTES     (10)

/**
 * @def JSR75_PIM_MAX_LABEL_SIZE
 *
 * Maximum length of attribute/element/field label.
 * Used in jsr75_pim_field_attribute, jsr75_pim_field_array_element 
 * and jsr75_pim_field structure declarations.
 */
#define JSR75_PIM_MAX_LABEL_SIZE     (128)

/**
 * @def JSR75_PIM_INVALID_ID
 *
 * The ID value indicating the members that aren't in use.
 */
#define JSR75_PIM_INVALID_ID         (-1)

/**
 * @def JSR75_PIM_STRING_DELIMITER
 *
 * The delimiter separates particular strings in list.
 */
#define JSR75_PIM_STRING_DELIMITER   ('\n')

/**
 * @def JSR75_PIM_MAX_FIELDS
 *
 * Maximum number of fields.
 */
#define JSR75_PIM_MAX_FIELDS         (19)

/**
 * @def JSR75_PIM_MAX_BUFFER_SIZE
 *
 * Maximum size of PIM data buffer.
 */
#define JSR75_PIM_MAX_BUFFER_SIZE    (65535)


/** Type for storing list and item handles. */
typedef void* jsr75_handle;


/**
 * @enum JSR75_STATUSCODE
 * Defines possible result codes for native functions
 */
typedef enum {
    JSR75_STATUSCODE_OK = 0,
    JSR75_STATUSCODE_FAIL = -1,
    JSR75_STATUSCODE_NOT_IMPLEMENTED = -2,
    JSR75_STATUSCODE_OUT_OF_MEMORY = -3,
    JSR75_STATUSCODE_INVALID_ARGUMENT = -4
} JSR75_STATUSCODE;

/**
 * @enum jsr75_pim_type
 * Indicates which personal information the user would like to manage (Contact, Event, Todo)
 */
typedef enum {
  jsr75_pim_type_contact = 0x10000000,
  jsr75_pim_type_event = 0x20000000,
  jsr75_pim_type_todo = 0x40000000,
} jsr75_pim_type;

/**
 * @enum jsr75_pim_contact_field
 * List all the possible contact fields.
 */
typedef enum {
  jsr75_pim_contact_field_addr = 100,
  jsr75_pim_contact_field_birthday, 
  jsr75_pim_contact_field_class,
  jsr75_pim_contact_field_email,
  jsr75_pim_contact_field_formatted_addr,
  jsr75_pim_contact_field_formatted_name,
  jsr75_pim_contact_field_name,
  jsr75_pim_contact_field_nickname,
  jsr75_pim_contact_field_note,
  jsr75_pim_contact_field_org,
  jsr75_pim_contact_field_photo,
  jsr75_pim_contact_field_photo_url,
  jsr75_pim_contact_field_public_key,
  jsr75_pim_contact_field_public_key_string,
  jsr75_pim_contact_field_revision,
  jsr75_pim_contact_field_tel,
  jsr75_pim_contact_field_title,
  jsr75_pim_contact_field_uid,
  jsr75_pim_contact_field_url
} jsr75_pim_contact_field;

/**
 * @enum jsr75_pim_contact_field_attr
 * List all the possible contact fields attributes.
 */
typedef enum {
  jsr75_pim_contact_field_attr_asst = 1,
  jsr75_pim_contact_field_attr_auto = 2,
  jsr75_pim_contact_field_attr_fax = 4,
  jsr75_pim_contact_field_attr_home = 8,  
  jsr75_pim_contact_field_attr_mobile = 16,
  jsr75_pim_contact_field_attr_other = 32, 
  jsr75_pim_contact_field_attr_pager = 64,
  jsr75_pim_contact_field_attr_preferred = 128,
  jsr75_pim_contact_field_attr_sms = 256,
  jsr75_pim_contact_field_attr_work = 512
} jsr75_pim_contact_field_attr;

/**
 * @enum jsr75_pim_contact_field_array_element_addr
 * List all the possible contact fields address array elements.
 */
typedef enum {
  jsr75_pim_contact_field_array_element_addr_pobox,
  jsr75_pim_contact_field_array_element_addr_extra,
  jsr75_pim_contact_field_array_element_addr_street,
  jsr75_pim_contact_field_array_element_addr_locality,  
  jsr75_pim_contact_field_array_element_addr_region,
  jsr75_pim_contact_field_array_element_addr_postalcode,
  jsr75_pim_contact_field_array_element_addr_country
} jsr75_pim_contact_field_array_element_addr;

/**
 * @enum jsr75_pim_contact_field_array_element_name
 * List all the possible contact fields name array elements.
 */
typedef enum {
  jsr75_pim_contact_field_array_element_name_family,
  jsr75_pim_contact_field_array_element_name_given,
  jsr75_pim_contact_field_array_element_name_other,
  jsr75_pim_contact_field_array_element_name_prefix,
  jsr75_pim_contact_field_array_element_name_suffix,  
} jsr75_pim_contact_field_array_element_name;

/**
 * @enum jsr75_pim_event_field
 * List all the possible event fields.
 */
typedef enum {
  jsr75_pim_event_field_alarm = 100,
  jsr75_pim_event_field_class,
  jsr75_pim_event_field_end,
  jsr75_pim_event_field_location,
  jsr75_pim_event_field_note,
  jsr75_pim_event_field_revision,
  jsr75_pim_event_field_start,
  jsr75_pim_event_field_summary,
  jsr75_pim_event_field_uid
} jsr75_pim_event_field;

/**
 * @enum jsr75_pim_todo_field
 * List all the possible todo fields.
 */
typedef enum {  
  jsr75_pim_todo_field_class = 100,
  jsr75_pim_todo_field_completed,
  jsr75_pim_todo_field_completion_date,
  jsr75_pim_todo_field_due,
  jsr75_pim_todo_field_note,
  jsr75_pim_todo_field_priority,  
  jsr75_pim_todo_field_revision,
  jsr75_pim_todo_field_summary,
  jsr75_pim_todo_field_uid
} jsr75_pim_todo_field;



/**
 * @enum jsr75_pim_field_type
 * The type of field
 */
typedef enum {
  jsr75_pim_field_type_binary,
  jsr75_pim_field_type_boolean,
  jsr75_pim_field_type_date,
  jsr75_pim_field_type_int,
  jsr75_pim_field_type_string,
  jsr75_pim_field_type_string_array
} jsr75_pim_field_type;

/**
 * @struct jsr75_pim_field_attribute
 * Holds the field attribute information.
 */
typedef struct {
  jint id;                               /**< attribute ID. */
  jchar label[JSR75_PIM_MAX_LABEL_SIZE]; /**< attribute label. */
} jsr75_pim_field_attribute;

/**
 * @struct jsr75_pim_field_array_element
 * Holds the field array element information information.
 */
typedef struct {
  jint id;                               /**< element ID. */
  jchar label[JSR75_PIM_MAX_LABEL_SIZE]; /**< element label. */
} jsr75_pim_field_array_element;

/**
 * @struct jsr75_pim_field
 * Holds all the required information regarding a field.
 */
typedef struct {
  jint id;                               /**< field ID. */
  /** indicates the total number of values that can be stored in the field. */
  jint maxValues;
  jsr75_pim_field_type type;             /**< field type. */
  jchar label[JSR75_PIM_MAX_LABEL_SIZE]; /**< field label. */
  jint attributes;                       /**< field attributes. */
  /** field array elements. */
  jsr75_pim_field_array_element arrayElements[JSR75_PIM_MAX_ARRAY_ELEMENTS];
} jsr75_pim_field;


/**
 * @enum jsr75_pim_open_mode
 * The access mode to the relevant pim list.
 */
typedef enum {
  jsr75_pim_open_mode_read_only,
  jsr75_pim_open_mode_write_only,
  jsr75_pim_open_mode_read_write,
} jsr75_pim_open_mode;


/**
 * @defgroup jsrMandatoryPIM Mandatory PIM API
 * @ingroup PIM
 * @{
 */


/**
 * Return a JSR75_PIM_STRING_DELIMITER separated list that contains the names of PIM list
 * the matches the given listType ("Contact" Or "JohnContact(\n)
                                                 SuziContact")
 *
 * @param listType the pim list type the user wishes to obtain
 * @param pimList pointer to where to store the returned list
 *                (the list must be delimited by JSR75_PIM_STRING_DELIMITER,
 *                the default list name should appear in the first place)
 * @param pimListLen the length of the pim list buffer
 *
 * @retval JSR75_STATUSCODE_OK  on success
 * @retval JSR75_STATUSCODE_FAIL  when no list exists or when the buffer size is too small 
 */
JSR75_STATUSCODE 
jsr75_pim_get_lists(
  jsr75_pim_type listType,
  jchar /*OUT*/ *pimList,
  jsize pimListLen
  );



/**
 * Open the request pim list in the given mode.
 *
 * @param listType the pim list type to open
 * @param pimList the name of the list to open,
 *        if pimList is null the handle of default dummy
 *        list will be returned; this kind of list may
 *        be used only for getting default list structure
 * @param mode the open mode for the list
 * @param listHandle pointer to where to store the list handle
 *
 * @retval JSR75_STATUSCODE_OK  on success
 * @retval JSR75_STATUSCODE_INVALID_ARGUMENT  If an invalid mode is provided as a parameter or 
                                      if pimListType is not a valid PIM list type
 * @retval JSR75_STATUSCODE_FAIL  on other error
 */
JSR75_STATUSCODE 
jsr75_pim_list_open(
  jsr75_pim_type listType,
  jchar *pimList,
  jsr75_pim_open_mode mode,
  jsr75_handle /* OUT */ *listHandle
  );

/**
 * Close the opened pim list
 *
 * @param listHandle handle of the list to close
 *
 * @retval JSR75_STATUSCODE_OK  on success   
 * @retval JSR75_STATUSCODE_FAIL  in case the list is no longer accessible
 */
JSR75_STATUSCODE 
jsr75_pim_list_close(
  jsr75_handle listHandle
  );


/**
 * Returns the next item in the given pim list 
 * For Contact item the item will be in vCard 2.1 / 3.0 format
 * For Event Todo item the item will be in vCalendar 1.0 format
 *
 * @param listHandle handle of the list the get the item from
 * @param item a pointer to where to store the item, NULL otherwise
 * @param maxItemLen the maximum size of the item
 * @param categories pointer to where to store the item's categories
 *                   separated by JSR75_PIM_STRING_DELIMITER, NULL otherwise
 * @param maxCategoriesLen the maximum size of the categories buffer
 * @param itemHandle pointer to where to store a unique identifier
 *                   for the returned item
 *
 * @retval JSR75_STATUSCODE_OK  on success   
 * @retval JSR75_STATUSCODE_INVALID_ARGUMENT  maxItemLen is too small 
 * @retval JSR75_STATUSCODE_FAIL  in case of reaching the last item in the list
 */
JSR75_STATUSCODE 
jsr75_pim_list_get_next_item(
  jsr75_handle listHandle,
  unsigned char /* OUT */ *item,
  jsize maxItemLen,
  jchar /* OUT */ *categories,
  jsize maxCategoriesLen,
  jsr75_handle /* OUT */ *itemHandle
  );

/**
 * Modify an item.
 * For Contact item the item will be in vCard 2.1 / 3.0 format
 * For Event Todo item the item will be in vCalendar 1.0 format
 *
 * @param listHandle handle of the list where the item is located
 * @param itemHandle handle of the item to modify
 * @param item pointer to the item data
 * @param categories pointer to the item's categories separated by a comma
 *
 * @retval JSR75_STATUSCODE_OK  on success   
 * @retval JSR75_STATUSCODE_FAIL  in case of an error
 */
JSR75_STATUSCODE 
jsr75_pim_list_modify_item(
  jsr75_handle listHandle,
  jsr75_handle itemHandle,
  const unsigned char *item,
  const jchar *categories
  );

/**
 * Add a new item to the given item list
 * For Contact item the item will be in vCard 2.1 / 3.0 format
 * For Event Todo item the item will be in vCalendar 1.0 format
 *
 * @param listHandle handle of the list to add the new item to
 * @param item pointer to the item to add to the list
 * @param categories pointer to the item's categories separate by a comma
 * @param itemHandle pointer to where to store a unique identifier 
 *                   for the new item
 *
 * @retval JSR75_STATUSCODE_OK  on success   
 * @retval JSR75_STATUSCODE_FAIL  in case of an error
 */
JSR75_STATUSCODE 
jsr75_pim_list_add_item(
  jsr75_handle listHandle,
  const unsigned char *item,
  const jchar *categories,
  jsr75_handle *itemHandle
  );

/**
 * Removes an item from the list
 *
 * @param listHandle handle of the list to delete the item from.
 * @param itemHandle handle of the item
 *
 * @retval JSR75_STATUSCODE_OK  on success   
 * @retval JSR75_STATUSCODE_FAIL  in case of reaching the last item in the list
 */
JSR75_STATUSCODE 
jsr75_pim_list_remove_item(
  jsr75_handle listHandle,
  jsr75_handle itemHandle
  );

/**
 * Adds the provided category to the PIM list. If the given category already exists 
 * for the list, the method does not add another category and considers that this 
 * method call is successful and returns.
 *
 * The category names are case sensitive in this API, but not necessarily in the 
 * underlying implementation. For example, "Work" and "WORK" map to the same 
 * underlying category if the platform's implementation of categories is case-insensitive; 
 * adding both separately would result in only one category being created in this case.
 *
 * A string with no characters ("") may or may not be a valid category on a particular platform. 
 * If the string is not a valid category as defined by the platform, JSR75_STATUSCODE_FAIL is returned
 * when trying to add it. 
 *
 * @param listHandle handle of the list to add the new category to
 * @param categoryName the name of the category to be added
 *
 * @retval JSR75_STATUSCODE_OK  on success   
 * @retval JSR75_STATUSCODE_FAIL   If categories are unsupported, an error occurs, 
 *                         or the list is no longer accessible or closed.
 */
JSR75_STATUSCODE 
jsr75_pim_list_add_category(
  jsr75_handle listHandle,
  jchar *categoryName
  );

/**
 * Removes the indicated category from the PIM list. If the indicated category is 
 * not in the PIM list, this method is treated as successfully completing.
 * The category names are case sensitive in this API, but not necessarily in the 
 * underlying implementation. For example, "Work" and "WORK" map to the same underlying 
 * category if the platform's implementation of categories is case-insensitive; 
 * removing both separately would result in only one category being removed in this case. 
 *
 * @param listHandle handle of the list to remove the new category from
 * @param categoryName the name of the category to be removed
 *
 * @retval JSR75_STATUSCODE_OK  on success   
 * @retval JSR75_STATUSCODE_FAIL   if categories are unsupported, an error occurs, 
 *                         or the list is no longer accessible or closed.
 */
JSR75_STATUSCODE 
jsr75_pim_list_remove_category(
  jsr75_handle listHandle,
  jchar *categoryName
  );


/**
 * Renames a category from an old name to a new name. All items associated with 
 * the old category name are changed to reference the new category name after 
 * this method is invoked. If the new category name is already an existing category, 
 * then the items associated with the old category name are associated with the existing category.
 * A string with no characters ("") may or may not be a valid category on a particular platform. 
 * If the string is not a category on a platform, a JSR75_STATUSCODE_FAIL should returned when trying 
 * to rename a category to it. 
 *
 * @param listHandle handle of the list to remove the new category from
 * @param oldCategoryName the old category name
 * @param newCategoryName the new category name
 *
 * @retval JSR75_STATUSCODE_OK  on success   
 * @retval JSR75_STATUSCODE_FAIL   in case of an error
 */
JSR75_STATUSCODE 
jsr75_pim_list_rename_category(
  jsr75_handle listHandle,
  jchar *oldCategoryName,
  jchar *newCategoryName
  );

/**
 *  Returns the maximum number of categories that this list can have.
 *
 * @param listHandle handle of the list the get the number from
 *
 * @retval -1 - indicates there is no limit the the number of categories that 
 *              this list can have
 * @retval 0  - indicates no category support
 * @retval 0 > - in case of a limitation
 */
jint 
jsr75_pim_list_max_categories(
  jsr75_handle listHandle
  );

/**
 * Returns the categories defined for the PIM list in JSR75_PIM_STRING_DELIMITER separated
 * format ("Work,HOME,Friends"). 
 * If there are no categories defined for the PIM list or categories are 
 * unsupported for the list, then JSR75_STATUSCODE_FAIL should be returned
 *
 * @param listHandle handle of the list the get the item from
 * @param categoriesName pointer to where to store the categories
 * @param maxCategoriesLen the maximum size of the categoriesName
 *
 * @retval JSR75_STATUSCODE_OK  on success   
 * @retval JSR75_STATUSCODE_FAIL  in case no categories found or in case of an error.
 */
JSR75_STATUSCODE 
jsr75_pim_list_get_categories(
  jsr75_handle listHandle,
  jchar /* OUT */ *categoriesName,
  jsize maxCategoriesLen
  );


/**
 * Gets all fields that are supported in this list. 
 * All fields supported by this list, including both standard 
 * and extended, are returned in this array.
 *
 * in order to identify field, field attributes , field array element
 * that aren't in use the JSR75_PIM_INVALID_ID should be set for the
 * member that aren't in use.
 *
 * @param listHandle handle of the list to get the fields from
 * @param fields pointer to where to store the fields to
 * @param maxFields the maximum fields the field buffer can hold
 *
 * @retval JSR75_STATUSCODE_OK  on success   
 * @retval JSR75_STATUSCODE_FAIL  in case of reaching the last item in the list
 */
JSR75_STATUSCODE 
jsr75_pim_list_get_fields(
  jsr75_handle listHandle,
  jsr75_pim_field /* OUT */ *fields,
  jint maxFields
  );

/**
 * Gets all attributes supported by the list.
 *
 * @param listHandle handle of the list to get the attributes from
 * @param attributes pointer to buffer to store the attributes in
 * @param maxAttributes maximum number of attributes the buffer can hold
 *
 * @retval JSR75_STATUSCODE_OK  on success   
 * @retval JSR75_STATUSCODE_FAIL  in case of reaching the last item in the list
 */
JSR75_STATUSCODE 
jsr75_pim_list_get_attributes(
  jsr75_handle listHandle,
  jsr75_pim_field_attribute /* OUT */ *attributes,
  jint maxAttributes
  );

/** @} */

#ifdef __cplusplus
}
#endif

#endif  /* __JSR75_PIM_H */
