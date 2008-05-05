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

package com.sun.kvem.midp.pim;

import javax.microedition.pim.PIMException;
import javax.microedition.pim.PIM;
import java.io.*;
import java.util.Hashtable;
import java.util.Enumeration;
import com.sun.midp.log.*;
import com.sun.midp.main.Configuration;
import com.sun.kvem.midp.pim.formats.FormatSupport;


/**
 * Implementations of shared PIM code.
 */
public class PIMBridge extends PIMHandler {

    /**
     * Contact table.
     */
    private static Hashtable contactListFields = new Hashtable();

    /**
     * Event table.
     */
    private static Hashtable eventListFields   = new Hashtable();

    /**
     * Todo table.
     */
    private static Hashtable todoListFields    = new Hashtable();

    /**
     * PIMItem data storage.
     */
    private static PIMDatabase database;

    /**
     * Constant representing a Contact List.
     */
    public static final int CONTACT_LIST = 1;

    /**
     * Constant representing an Event List.
     */
    public static final int EVENT_LIST = 2;

    /**
     * Constant representing a ToDo List.
     */
    public static final int TODO_LIST = 3;

    /**
     * Initialization flag.
     */
    private static boolean initialized = false;


    /**
     * This class holds information about a single list.
     */
    private class List {
        /** Type of the list: CONTACT_LIST, EVENT_LIST or TODO_LIST */
        int type;

        /** Name of the list */
        String name;

        /**
         * The only constructor for this list descriptor.
         *
         * @param listType CONTACT_LIST, EVENT_LIST or TODO_LIST
         * @param listName name of the list
         */
        List(int listType, String listName) {
            type = listType;
            name = listName;
        }
    }

    /**
     * Set up data structures.
     */
    public synchronized void initialize() {
        if (!initialized) {
            initializeMap(contactListFields,
            SupportedPIMFields.CONTACT_LIST_FIELDS);
            initializeMap(eventListFields,
            SupportedPIMFields.EVENT_LIST_FIELDS);
            initializeMap(todoListFields,
            SupportedPIMFields.TODO_LIST_FIELDS);
        try {
                database = new PIMDatabase(
            Configuration.getProperty("PIMRootDir") + "pim");
                initialized = true;
        } catch (IOException e) {
            if (Logging.TRACE_ENABLED) {
                    Logging.trace(e, "Unable to create PIMDatabase");
        }
            }
    }
    }

    /**
     *  Initialization one structure.
     *
     * @param map          Hashtable for initialization
     * @param descriptors  array of the field descriptors
     */
    private void initializeMap(Hashtable map,
        PIMFieldDescriptor[] descriptors) {
        for (int i = 0; i < descriptors.length; i++) {
            map.put(new Integer(descriptors[i].getField()), descriptors[i]);
        }
    }

    /**
     *  Gets the table of fields for given list type.
     *
     * @param listType    CONTACT_LIST, EVENT_LIST or  TODO_LIST
     *
     * @return hashtable of fields
     */
    private Hashtable getFields(int listType) {
        Hashtable map;
        switch (listType) {
            case CONTACT_LIST:
                map = contactListFields;
                break;
            case EVENT_LIST:
                map = eventListFields;
                break;
            case TODO_LIST:
                map = todoListFields;
                break;
            default:
                throw new IllegalArgumentException("List type " + listType
                    + " is not valid");
        }
        return map;
    }

    /**
     *  Gets the descriptor for given field.
     *
     * @param listType    CONTACT_LIST, EVENT_LIST or  TODO_LIST
     * @param field the field ID
     *
     * @return field descriptor
     */
    private PIMFieldDescriptor getFieldDescriptor(int listType, int field) {
        return (PIMFieldDescriptor)getFields(listType).get(new Integer(field));
    }

    /**
     * Gets all fields that are supported in this list.  All fields supported by
     * this list, including both standard and extended, are returned in this
     * array.
     *
     * @param listHandle handle of list
     * @return  an int array containing all fields supported by this list.  The
     *          order of the fields returned is unspecified.  If there are no
     *          supported fields, a zero-length array is returned.
     */
    public int[] getSupportedFields(Object listHandle) {
        initialize();

        Hashtable map = getFields(((List)listHandle).type);
        Enumeration fieldNumbers = map.keys();
        int len = map.size();
        int[] result = new int[len];

        for (int i = 0; i < len; i++) {
            result[i] = (fieldNumbers.nextElement()).hashCode();
        }

        return result;
    }

    /**
     * Checks if field is supported in list.
     * @param listHandle handle of the list
     * @param field identifier of field
     * @return <code>true</code> if field supported
     */
    public boolean isSupportedField(Object listHandle, int field) {
        initialize();
        return getFieldDescriptor(((List)listHandle).type, field) != null;
    }

    /**
     * Checks if field has default value.
     * @param listHandle handle of the list
     * @param field identifier of field
     * @return <code>true</code> if field supported
     */
    public boolean hasDefaultValue(Object listHandle, int field) {
        initialize();
        return getFieldDescriptor(((List)listHandle).type, field)
                .hasDefaultValue();
    }

    /**
     * Gets the data type of the field.
     * @param listHandle handle of the list
     * @param field identifier of field
     * @return data type identifier
     */
    public int getFieldDataType(Object listHandle, int field) {
        initialize();
        try {
            return getFieldDescriptor(((List)listHandle).type, field)
                    .getDataType();
        } catch (NullPointerException npe) {
            return -1;
        }
    }

    /**
     * Gets the label of the field.
     * @param listHandle handle of the list
     * @param field identifier of field
     * @return label of the field
     */
    public String getFieldLabel(Object listHandle, int field) {
        initialize();
        try {
            return getFieldDescriptor(((List)listHandle).type, field)
                    .getLabel();
        } catch (NullPointerException npe) {
            return null;
        }
    }

    /**
     * Gets the default integer value for the given field. This will
     *  only
     * return a valid value if hasDefaultValue(listType, field) returns true.
     * @param listHandle handle of the list
     * @param field identifier of field
     * @return default value of the field
     */
    public int getDefaultIntValue(Object listHandle, int field) {
        initialize();
        PIMFieldDescriptor descriptor =
            getFieldDescriptor(((List)listHandle).type, field);
        return ((Integer) descriptor.getDefaultValue()).intValue();
    }

    /**
     * Gets the default string value for the given field. This will
     * only
     * return a valid value if hasDefaultValue(listType, field) returns true.
     * @param listHandle handle of the list
     * @param field identifier of field
     * @return default value of the field
     */
    public String getDefaultStringValue(Object listHandle, int field) {
        return null;
    }

    /**
     * Gets the default String[] value for the given field. This will
     * only
     * return a valid value if hasDefaultValue(listType, field) returns true.
     * @param listHandle handle of the list
     * @param field identifier of field
     * @return default value of the field
     */
    public String[] getDefaultStringArrayValue(Object listHandle, int field) {
        int length = getStringArraySize(listHandle, field);
        return new String[length];
    }

    /**
     * Gets the default date value for the given field. This will only
     * return a valid value if hasDefaultValue(listType, field) returns true.
     * @param listHandle handle of the list
     * @param field identifier of field
     * @return default value of the field
     */
    public long getDefaultDateValue(Object listHandle, int field) {
        return 0;
    }

    /**
     * Gets the default byte[] value for the given field. This will
     * only
     * return a valid value if hasDefaultValue(listType, field) returns true.
     * @param listHandle handle of the list
     * @param field identifier of field
     * @return default value of the field
     */
    public byte[] getDefaultBinaryValue(Object listHandle, int field) {
        return null;
    }

    /**
     * Gets the default boolean value for the given field. This will
     * only
     * return a valid value if hasDefaultValue(listType, field) returns true.
     * @param listHandle handle of the list
     * @param field identifier of field
     * @return default value of the field
     */
    public boolean getDefaultBooleanValue(Object listHandle, int field) {
        return false;
    }

    /**
     * Gets the supported attributes for the given field.
     * @param listHandle handle of the list
     * @param field identifier of field
     * @return array of supported attributes of the field
     */
    public int[] getSupportedAttributes(Object listHandle, int field) {
        initialize();
        int listType = ((List)listHandle).type;
        long attributes = getFieldDescriptor(listType, field)
            .getSupportedAttributes();
        int elementCount = listType == PIM.CONTACT_LIST ? 1 : 0;
        for (long a = attributes; a > 0; a >>= 1) {
            if ((a & 1) == 1) {
                elementCount++;
            }
        }
        int[] result = new int[elementCount];
        if (elementCount > 0) {
            int a = 1;
            int i;
            if (listType == PIM.CONTACT_LIST) {
                result[0] = PIMItem.ATTR_NONE;
                i = 1;
            } else {
                i = 0;
            }
            for (; i < elementCount; i++) {
                while ((attributes & a) == 0) a <<= 1;
                result[i] = a;
                a <<= 1;
            }
        }
        return result;
    }

    /**
     * Gets a mask containing all possible attributes for the given field.
     *
     * @param listHandle handle of the list
     * @param field the field number
     * @return supported attribute mask
     */
    public int getSupportedAttributesMask(Object listHandle, int field) {
        initialize();
        return (int)getFieldDescriptor(((List)listHandle).type, field)
                .getSupportedAttributes();
    }

    /**
     * Gets attribute label for the given field attribute.
     *
     * @param listHandle handle of the list
     * @param attribute identifier of attribute
     * @return attribute label
     */
    public String getAttributeLabel(Object listHandle, int attribute) {
        initialize();
        StringBuffer tag = new StringBuffer("PIM.Attributes.");
        if (attribute == 0) {
            tag.append("None");
        } else {
            switch (((List)listHandle).type) {
                case CONTACT_LIST: tag.append("ContactList."); break;
                case EVENT_LIST: tag.append("EventList."); break;
                case TODO_LIST: tag.append("ToDoList."); break;
                default: return null;
            }
            int index = 0;
            while (attribute > 1) {
                index++;
                attribute = attribute >> 1;
            }
            tag.append(index);
        }
        String tagString = tag.toString();
        String returnValue = Configuration.getPropertyDefault(tagString,
            "Label_" + tagString);
        return returnValue;
    }

    /**
     * Checks if attribute is supported.
     *
     * @param listHandle handle of the list
     * @param field the field number
     * @param attribute identifier of attribute
     * @return <code>true</code> if attribute is supported
     */
    public boolean isSupportedAttribute(Object listHandle, int field,
        int attribute) {
        initialize();
        if (attribute == PIMItem.ATTR_NONE) {
            return true;
        } else {
            long attributes = getFieldDescriptor(((List)listHandle).type, field)
                .getSupportedAttributes();
            return (attributes & attribute) != 0;
        }
    }

    /**
     * Checks if size of the string array.
     *
     * @param listHandle handle of the list
     * @param field the field number
     * @return size of the string array
     */
    public int getStringArraySize(Object listHandle, int field) {
        initialize();
        try {
            return getFieldDescriptor(((List)listHandle).type, field)
                .getStringArraySize();
        } catch (NullPointerException npe) {
            // debug.exception(Debug.LIGHT, npe);
            return 0;
        }
    }

    /**
     * Gets the array of supported elements.
     *
     * @param listHandle handle of the list
     * @param field the field number
     * @return array of supported elements
     */
    public int[] getSupportedArrayElements(Object listHandle, int field) {
        int size = getStringArraySize(listHandle, field);
        int[] result = new int[size];
        for (int i = 0; i < size; i++) {
            result[i] = i;
        }
        return result;
    }

    /**
     * Gets the array element label.
     *
     * @param listHandle handle of the list
     * @param field the field number
     * @param arrayElement the element identifier
     * @return label fro the array element
     */
    public String getArrayElementLabel(Object listHandle, int field,
        int arrayElement) {
        initialize();
        return getFieldDescriptor(((List)listHandle).type, field)
            .getElementlabel(arrayElement);
    }

    /**
     * Checks if the array element is supported.
     *
     * @param listHandle handle of the list
     * @param field the field number
     * @param arrayElement the element identifier
     * @return <code>true</code> if attribute element is supported
     */
    public boolean isSupportedArrayElement(Object listHandle, int field,
            int arrayElement) {
        return arrayElement >= 0 &&
            arrayElement < getStringArraySize(listHandle, field);
    }

    /**
     * Get the maximum number of values that can be stored in the given field.
     *
     * @param listHandle handle of the list
     * @param field the field type
     * @return the maximum value
     */
    public int getMaximumValues(Object listHandle, int field) {
        initialize();
        return getFieldDescriptor(((List)listHandle).type, field)
            .getMaximumValues();
    }

    /**
     * Get the supported list names for the given list type. All list elements
     * must be unique within the list.
     *
     * @param listType the type of the list
     * @return a non-null array of supported list names. A copy of this array is
     * returned by PIM.listPIMLists()
     */
    synchronized public String[] getListNames(int listType) {
        initialize();
        int length = database.getListNames(listType).length;
        String[] names = new String[length];
        for (int i = 0; i < length; i++) {
            names[i] = database.getListNames(listType)[i];
        }
        return names;
    }

    /**
     * Get the name of the default list for the given type.
     *
     * @param listType the type of the list
     * @return the name of the default list, or null if no list of this type
     * is supported.
     */
    public String getDefaultListName(int listType) {
        initialize();
        return database.getDefaultListName(listType);
    }

    /**
     * Opens list.
     *
     * @param listType the type of the list
     * @param listName the name of the list
     * @param openMode open mode
     * @return list handle that will be used to access this list
     * @throws PIMException  in case of I/O error.
     */
    public Object openList(int listType, String listName, int openMode)
        throws PIMException {
        return new List(listType, listName);
    }

    /**
     * Closes list.
     *
     * @param listHandle handle of list
     * @throws PIMException  in case of I/O error.
     */
    public void closeList(Object listHandle)
        throws PIMException {
    }

    /**
     * Get list element keys.
     *
     * @param listHandle handle of the list
     * @return an array of objects representing PIM element keys. These keys
     * are to be passed to getListElement() and commitListElement().
     * @throws PIMException  in case of I/O error.
     */
    synchronized public Object[] getListKeys(Object listHandle)
            throws PIMException {
        initialize();

        Hashtable hash_keys =
            database.getKeys(((List)listHandle).type, ((List)listHandle).name);
        int len = hash_keys.size();
        Object[] keys = new Object[len];
        Enumeration en_keys = hash_keys.keys();

        for (int i = 0; i < len; i++) {
            keys[i] = en_keys.nextElement().toString();
        }

        return keys;
    }

    /**
     * Get the data for a list element.
     * @param listHandle handle of the list
     * @param elementKey the key of the requested element
     * @return a byte array containing the element data in a supported format
     * @throws PIMException  in case of I/O error.
     */
    public byte[] getListElement(Object listHandle,
        Object elementKey) throws PIMException {
        initialize();
        return database.getElement(((List)listHandle).type,
            ((List)listHandle).name, (String)elementKey);

    }

    /**
     * Get categories for the specified list element.
     * @param listHandle handle of list
     * @param elementKey the key of the requested element
     * @return an array of categories names
     * @throws PIMException  in case of I/O error.
     */
    public String[] getListElementCategories(Object listHandle,
        Object elementKey) throws PIMException {

        return new String[0];
    }

    /**
     * Commit a list element.
     *
     * @param listHandle handle of the list
     * @param elementKey the key of the element to be stored, or null if this
     * is a new element.
     * @param element element data in a form that can be interpreted
     * by getListElement()
     * @param categories list of categories which the list element belongs to
     * @return a non-null key for this element, to be used in future calls
     * to commitListElement() and getListElement()
     * @throws PIMException  in case of I/O error.
     */
    synchronized public Object commitListElement(Object listHandle,
        Object elementKey, byte[] element, String[] categories)
        throws PIMException {
        initialize();
        elementKey = database.commitElement(((List)listHandle).type,
            ((List)listHandle).name, (String)elementKey, element);
        return elementKey;
    }

    /**
     * Gets the set of categories defined for a list.
     *
     * @param listHandle handle of the list
     * @return the set of defined categories
     * @throws PIMException  If an error occurs or
     * the list is no longer accessible or closed.
     */
    public String[] getCategories(Object listHandle)
        throws PIMException {
        initialize();
        String result = database.getCategories(((List)listHandle).type,
            ((List)listHandle).name);
        return FormatSupport.split(result, '\n', 0);
    }

    /**
     * Adds a category to the categories defined for a list.
     *
     * @param listHandle handle of list
     * @param category category name
     * @throws PIMException  If an error occurs or
     * the list is no longer accessible or closed.
     * @see #getCategories
     */
    public void addCategory(Object listHandle,
        String category) throws PIMException {
        initialize();
        String cats = database.getCategories(((List)listHandle).type,
            ((List)listHandle).name);
        // the implementation expects that '\n' is never escaped in categories
        String cat_add = "\n" + category;
        if (cats.indexOf(cat_add + '\n') != -1 ||
            cats.startsWith(category + '\n') ||
            cats.endsWith(cat_add) ||
            ((cats.length() > 0) && cats.equals(category))) {
            return;
        }
        if (cats.length() == 0 && category.length() > 0) {
            cats = category;
        } else {
            cats += cat_add;
        }
        database.setCategories(((List)listHandle).type,
            ((List)listHandle).name, cats);
    }

    /**
     * Deletes a category from the categories defined for a list.
     *
     * @param listHandle handle of list
     * @param category category name
     * @throws PIMException  If an error occurs or
     * the list is no longer accessible or closed.
     * @see #getCategories
     */
    public void deleteCategory(Object listHandle,
        String category) throws PIMException {
        initialize();
        String cats = database.getCategories(((List)listHandle).type,
            ((List)listHandle).name);
        // the implementation expects that '\n' is never escaped in categories
        String cat_add = "\n" + category;
        int pos;
        if ((pos = cats.indexOf(cat_add + '\n')) != -1) {
            cats = cats.substring(0, pos)
                + cats.substring(pos + cat_add.length());
        } else if (cats.startsWith(category + '\n')) {
            cats = cats.substring(cat_add.length());
        } else if (cats.endsWith(cat_add)) {
            cats = cats.substring(0, cats.length() - cat_add.length());
        } else if (cats.equals(category)) {
            cats = "";
        } else {
            return;
        }
        database.setCategories(((List)listHandle).type,
            ((List)listHandle).name, cats);
    }

    /**
     * Rename a category.
     *
     * @param listHandle handle of list
     * @param currentCategory current category name
     * @param newCategory new category name
     * @throws PIMException  If an error occurs or
     * the list is no longer accessible or closed.
     * @see #getCategories
     */
    public void renameCategory(Object listHandle,
        String currentCategory, String newCategory) throws PIMException {
        deleteCategory(listHandle, currentCategory);
        addCategory(listHandle, newCategory);
    }
}
