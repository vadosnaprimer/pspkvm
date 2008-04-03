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


#include <jsr75_pim.h>


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
  ) {
    (void)listType;
    (void)pimList;
    (void)pimListLen;

    return JSR75_STATUSCODE_NOT_IMPLEMENTED;
}



/**
 * Open the requested pim list in the given mode.
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
  ) {
    (void)listType;
    (void)pimList;
    (void)mode;
    (void)listHandle;

    return JSR75_STATUSCODE_NOT_IMPLEMENTED;
}

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
  ) {
    (void)listHandle;

    return JSR75_STATUSCODE_NOT_IMPLEMENTED;
}


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
  ) {
    (void)listHandle;
    (void)item;
    (void)maxItemLen;
    (void)categories;
    (void)maxCategoriesLen;
    (void)itemHandle;

    return JSR75_STATUSCODE_NOT_IMPLEMENTED;
}

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
  ) {
    (void)listHandle;
    (void)itemHandle;
    (void)item;
    (void)categories;

    return JSR75_STATUSCODE_NOT_IMPLEMENTED;
}

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
  ) {
    (void)listHandle;
    (void)item;
    (void)categories;
    (void)itemHandle;

    return JSR75_STATUSCODE_NOT_IMPLEMENTED;
}

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
  ) {
    (void)listHandle;
    (void)itemHandle;

    return JSR75_STATUSCODE_NOT_IMPLEMENTED;
}

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
  ) {
    (void)listHandle;
    (void)categoryName;

    return JSR75_STATUSCODE_NOT_IMPLEMENTED;
}

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
  ) {
    (void)listHandle;
    (void)categoryName;

    return JSR75_STATUSCODE_NOT_IMPLEMENTED;
}


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
  ) {
    (void)listHandle;
    (void)oldCategoryName;
    (void)newCategoryName;

    return JSR75_STATUSCODE_NOT_IMPLEMENTED;
}

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
  ) {
    (void)listHandle;

    return 0;
}

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
 * @retval JSR75_STATUSCODE_FAIL  in case no categories found or incase of an error.
 */
JSR75_STATUSCODE 
jsr75_pim_list_get_categories(
  jsr75_handle listHandle,
  jchar /* OUT */ *categoriesName,
  jsize maxCategoriesLen
  ) {
    (void)listHandle;
    (void)categoriesName;
    (void)maxCategoriesLen;

    return JSR75_STATUSCODE_NOT_IMPLEMENTED;
}

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
  ) {
    (void)listHandle;
    (void)fields;
    (void)maxFields;

    return JSR75_STATUSCODE_NOT_IMPLEMENTED;
}

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
  ) {
    (void)listHandle;
    (void)attributes;
    (void)maxAttributes;

    return JSR75_STATUSCODE_NOT_IMPLEMENTED;
}
