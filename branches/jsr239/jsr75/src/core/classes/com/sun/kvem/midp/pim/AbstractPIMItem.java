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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.microedition.pim.Contact;
import javax.microedition.pim.FieldFullException;
import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMException;
import javax.microedition.pim.PIMItem;
import javax.microedition.pim.PIMList;
import javax.microedition.pim.UnsupportedFieldException;

/**
 * Partial implementation of PIMItem.
 * Extended by ContactImpl, EventImpl and ToDoImpl.
 *
 */
public abstract class AbstractPIMItem implements PIMItem {

    /** Sorted list of defined field codes. */
    private int[] fieldKeys = new int[0];

    /** List of defined PIMFields. Indexed by fieldKeys. */

    private PIMField[] fieldValues = new PIMField[0];

    /** List of categories to which this item belongs */
    private String[] categories = null;

    /** Has this item been modified since it was last committed? */
    private boolean modified = true;

    /** List which this item belongs to. May be null for a deserialized item. */
    private AbstractPIMList pimList;

    /**
     * Type of list this item belongs to (e.g. PIM.CONTACT_LIST).
     * Note: valid even if pimList is null.
     */
    private final int listType;

    /**
     * Internal list used for item description getting. May not be null.
     * For a deserialized item represents default dummy list
     */
    private Object pimListHandle;

    /** Key for the use of commit() */
    private Object key = null;

    /** Instance of PIMHandler to simplify access to PIM storage */
    private PIMHandler pimHandler;

    /**
     * Constructs a PIM list.
     * @param pimList initial list
     * @param type type of list
     */
    protected AbstractPIMItem(AbstractPIMList pimList, int type) {
        this.pimList = pimList;
        this.listType = type;
        pimHandler = PIMHandler.getInstance();

        try {
            pimListHandle = pimList != null ? pimList.getHandle() :
                pimHandler.openList(type, null, PIM.READ_ONLY);
        } catch (PIMException e) {
            throw new RuntimeException("Error while opening default list");
        }
    }

    /**
     * This constructor is used when importing an item.
     * @param pimList initial list
     * @param baseItem initial entry
     */
    protected AbstractPIMItem(AbstractPIMList pimList, PIMItem baseItem) {
        this(pimList, pimList.getType());
        // copy fields
        int[] fields = baseItem.getFields();
        for (int i = 0; i < fields.length; i++) {
            int field = fields[i];
            if (!pimList.isSupportedField(field)) {
                // skip field
                continue;
            }
            int dataType = pimList.getFieldDataType(field);
            int indices = baseItem.countValues(field);
            for (int index = 0; index < indices; index++) {
                int attributes = baseItem.getAttributes(field, index);
                Object value = null;
                switch (dataType) {
                    case PIMItem.BINARY: {
                        value = baseItem.getBinary(field, index);
                        break;
                    }
                    case PIMItem.BOOLEAN: {
                        value = new Boolean(baseItem.getBoolean(field, index));
                        break;
                    }
                    case PIMItem.DATE: {
                        value = new Long(baseItem.getDate(field, index));
                        break;
                    }
                    case PIMItem.INT: {
                        value = new Integer(baseItem.getInt(field, index));
                        break;
                    }
                    case PIMItem.STRING: {
                        value = baseItem.getString(field, index);
                        break;
                    }
                    case PIMItem.STRING_ARRAY: {
                        value = baseItem.getStringArray(field, index);
                        break;
                    }
                    default: {
                        // cannot import this data. Not a problem, since
                        // this method is called when importing a PIMItem
                        // from another list. In this case, it is valid
                        // to ignore data of an unknown type.
                    }
                } // end switch (dataType)
                try {
                    addValue(field, attributes, value, true);
                } catch (FieldFullException ffe) {
                    // Too many values. It's OK to ignore values that
                    // don't fit.
                } catch (IllegalArgumentException iae) {
                    // illegal data in this field. It's OK not to import it.
                }
            } // finish iterating over indices
        } // finish iterating over fields
        updateRevision();
    }

    /**
     * Gets the field entry.
     * @param field identifier for the field
     * @param create if <code>true</code> create the field
     * if it doesn't already exist
     * @param check if <code>true</code> check that the field is
     * supported before attempting to get it
     * @return the request field entry
     */
    PIMField getField(int field, boolean create, boolean check) {
        PIMField f = getField(field);
        if (f == null) {
            if (check && !pimHandler.isSupportedField(pimListHandle, field)) {
                throw complaintAboutField(listType, field);
            }
            if (create) {
                f = new EmptyPIMField();
                putField(field, f);
            }
        }
        return f;
    }

    /**
     * Sets the field value.
     * @param field identifier of field
     * @param index value offset
     * @param attributes field properties
     * @param value field to update
     * @param force if <code>true</code> create the value
     */
    private void setValue(
        int field, int index, int attributes, Object value, boolean force) {
        try {
            checkType(field, value);
            PIMField pimField = getField(field, false, true);
            if (pimField == null) {
                throw new IndexOutOfBoundsException("Empty field: " + field);
            }
            int currentValues = pimField.getValueCount();
            if (index < 0 || index >= currentValues) {
                throw new IndexOutOfBoundsException("0 <= index < "
                    + currentValues + ", " + index + " not in range");
            }
            if (!force) {
                checkReadOnlyFields(field);
            }
            if (value instanceof Integer) {
                checkIntValue(field, ((Integer)value).intValue());
            }
            attributes = filterAttributes(field, attributes);
            pimField.setValue(attributes, value, index);
            modified = true;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Wrong type for field");
        }
    }

    /**
     * Adds a value to a field.
     *
     * @param field identifier of field
     * @param attributes field properties
     * @param value field to update
     * @param force if <code>true</code> create the value
     * @throws FieldFullException if no more values can be added to the field
     */
    private void addValue(
        int field, int attributes, Object value, boolean force) {

        checkType(field, value);
        PIMField pimField = getField(field, true, true);
        int maxValues =
            pimHandler.getMaximumValues(pimListHandle, field);
        int currentValues = pimField.getValueCount();
        if (maxValues != -1 && currentValues >= maxValues) {
            throw new FieldFullException("Can only store "
                + maxValues + " in field", field);
        }
        if (!force) {
            checkReadOnlyFields(field);
        }
        if (value instanceof Integer) {
            checkIntValue(field, ((Integer)value).intValue());
        }
        if (pimField.isScalar()) {
            // upgrade PIM field
            if (currentValues == 0) {
                pimField = new ScalarPIMField();
                putField(field, pimField);
            } else {
                Object value0 = pimField.getValue(0);
                int attributes0 = pimField.getAttributes(0);
                pimField = new VectorPIMField();
                pimField.addValue(attributes0, value0);
                putField(field, pimField);
            }
        }
        attributes = filterAttributes(field, attributes);
        pimField.addValue(attributes, value);
        modified = true;
    }

    private void checkIntValue(int field, int value) {
        if ((listType == PIM.CONTACT_LIST && field == Contact.CLASS) ||
            (listType == PIM.EVENT_LIST && field == Event.CLASS) ||
            (listType == PIM.TODO_LIST && field == ToDo.CLASS)) {
            validateClass(value);
        }
        if (listType == PIM.TODO_LIST && field == ToDo.PRIORITY) {
            validatePriority(value);
        }
    }

    /**
     * Filters attributes to include only the supported ones.
     * @param field identifier of field
     * @param attributes field properties
     * @return filtered attributes
     */
    private int filterAttributes(int field, int attributes) {
        if (attributes == 0) {
            return 0;
        } else {
            return attributes
                & pimHandler
                    .getSupportedAttributesMask(pimListHandle, field);
        }
    }

    /**
     * Gets current value.
     * @param field identifier of field
     * @param index field identifier
     * @return requested field
     */
    private Object getValue(int field, int index) {
        PIMField pimField = getField(field, false, true);
        if (pimField == null) {
            throw new IndexOutOfBoundsException("Empty field: " + field);
        }
        int currentValues = pimField.getValueCount();
        if (index < 0 || index >= currentValues) {
            throw new IndexOutOfBoundsException("0 <= index < "
                + currentValues + ", " + index + " not in range");
        }
        return pimField.getValue(index);
    }

    // JAVADOC COMMENT ELIDED
    public void addStringArray(int field, int attributes, String[] value) {
        checkType(field, STRING_ARRAY);
        validateStringArray(field, value);
        addValue(field, attributes, value, false);
    }

    // JAVADOC COMMENT ELIDED
    public void addBoolean(int field, int attributes, boolean value) {
        addValue(field, attributes, new Boolean(value), false);
    }

    // JAVADOC COMMENT ELIDED
    public void removeFromCategory(String category) {
        if (category == null) {
            throw new NullPointerException("Null category");
        }
        if (categories != null) {
            for (int i = 0; i < categories.length; i++) {
                if (category.equals(categories[i])) {
                    if (categories.length == 1) {
                        this.categories = null;
                    } else {
                        String[] a = new String[categories.length - 1];
                        System.arraycopy(categories, 0, a, 0, i);
                        System.arraycopy(categories, i + 1, a, i,
                            a.length - i);
                        this.categories = a;
                    }
                    this.modified = true;
                    return;
                }
            }
        }
    }

    // JAVADOC COMMENT ELIDED
    public int[] getFields() {
        int emptyFields = 0;
        // make sure all these fields have defined values
        for (int i = 0; i < fieldValues.length; i++) {
            if (fieldValues[i].getValueCount() == 0) {
                emptyFields ++;
            }
        }
        int[] keys = new int[fieldKeys.length - emptyFields];
        for (int i = 0, j = 0; i < keys.length; i++) {
            if (emptyFields == 0 || fieldValues[i].getValueCount() != 0) {
                keys[j++] = fieldKeys[i];
            } else {
                emptyFields --;
            }
        }
        return keys;
    }

    // JAVADOC COMMENT ELIDED
    public boolean getBoolean(int field, int index) {
        checkType(field, BOOLEAN);
        return ((Boolean) getValue(field, index)).booleanValue();
    }

    // JAVADOC COMMENT ELIDED
    public void addDate(int field, int attributes, long value) {
        addValue(field, attributes, new Long(value), false);
    }

    // JAVADOC COMMENT ELIDED
    public int maxCategories() {
        return -1;
    }

    // JAVADOC COMMENT ELIDED
    public void setDate(int field, int index, int attributes, long value) {
        setValue(field, index, attributes, new Long(value), false);
    }

    // JAVADOC COMMENT ELIDED
    public int getInt(int field, int index) {
        checkType(field, INT);
        try {
            return ((Integer) getValue(field, index)).intValue();
        } catch (ClassCastException e) {
            String message = "Cannot convert to integer on field "
                + field + ": "
                + getValue(field, index).getClass();
            throw new ClassCastException(message);
        }
    }

    // JAVADOC COMMENT ELIDED
    public void setBinary(int field, int index, int attributes, byte[] value,
            int offset, int length) {
        validateBinaryValue(value, offset, length);
        length = Math.min(length, value.length - offset);
        byte[] b = new byte[length];
        System.arraycopy(value, offset, b, 0, length);
        setValue(field, index, attributes, b, false);
    }

    // JAVADOC COMMENT ELIDED
    public int getAttributes(int field, int index) {
        return getField(field, true, true).getAttributes(index);
    }

    // JAVADOC COMMENT ELIDED
    public int countValues(int field) {
        PIMField pimField = getField(field, false, true);
        return pimField == null ? 0 : pimField.getValueCount();
    }

    // JAVADOC COMMENT ELIDED
    public void addString(int field, int attributes, String value) {
        validateString(value);
        addValue(field, attributes, value, false);
    }

    // JAVADOC COMMENT ELIDED
    public String[] getCategories() {
        if (categories == null) {
            return new String[0];
        }
        String[] cs = new String[categories.length];
        System.arraycopy(categories, 0, cs, 0, categories.length);
        return cs;
    }

    // JAVADOC COMMENT ELIDED
    String[] getCategoriesRaw() {
        return categories;
    }

    // JAVADOC COMMENT ELIDED
    public void setInt(int field, int index, int attributes, int value) {
        setValue(field, index, attributes, new Integer(value), false);
    }

    // JAVADOC COMMENT ELIDED
    public void setStringArray(int field, int index, int attributes,
            String[] value) {
        checkType(field, STRING_ARRAY);
        validateStringArray(field, value);
        setValue(field, index, attributes, value, false);
    }

    /**
     * Makes sure that
     * <ul>
     * <li>The string array is not null
     * <li>At least one string in the array is not null
     * <li>The string array has the correct length
     * </ul>
     * @param field identifier for field
     * @param a string array to be checked
     */
    private void validateStringArray(int field, String[] a) {
        int requiredLength =
            pimHandler.getStringArraySize(pimListHandle, field);
        if (a.length != requiredLength) {
            throw new IllegalArgumentException(
                "String array length incorrect: should be " + requiredLength);
        }
        for (int i = 0; i < a.length; i++) {
            if (a[i] != null) {
                return;
            }
        }
        throw new IllegalArgumentException("No non-null elements in array");
    }

    /**
     * Makes sure that a string is not null.
     * @param value string to be checked
     */
    private void validateString(String value) {
        if (value == null) {
            throw new NullPointerException(
                "String field value should not be null");
        }
    }

    // JAVADOC COMMENT ELIDED
    public long getDate(int field, int index) {
        checkType(field, DATE);
        try {
            return ((Long) getValue(field, index)).longValue();
        } catch (ClassCastException e) {
            throw e;
        }
    }

    // JAVADOC COMMENT ELIDED
    public void addToCategory(String category) throws PIMException {
        if (category == null) {
            throw new NullPointerException("Null category");
        }
        if (categories == null) {
            this.categories = new String[] { category };
            this.modified = true;
        } else {
            for (int i = 0; i < categories.length; i++) {
                if (categories[i].equals(category)) {
                    return;
                }
            }
            String[] a = new String[categories.length + 1];
            System.arraycopy(categories, 0, a, 0, categories.length);
            a[categories.length] = category;
            this.categories = a;
            this.modified = true;
        }
    }

    // JAVADOC COMMENT ELIDED
    public void addInt(int field, int attributes, int value) {
        addValue(field, attributes, new Integer(value), false);
    }

    // JAVADOC COMMENT ELIDED
    public byte[] getBinary(int field, int index) {
        checkType(field, BINARY);
        return (byte[]) getValue(field, index);
    }

    // JAVADOC COMMENT ELIDED
    public void addBinary(int field, int attributes, byte[] value,
            int offset, int length) {
        validateBinaryValue(value, offset, length);
        length = Math.min(length, value.length - offset);
        byte[] b = new byte[length];
        System.arraycopy(value, offset, b, 0, length);
        addValue(field, attributes, b, false);
    }

    /**
     * Ensures that binary parameters are in range.
     * @param value binary data to be checked
     * @param offset index into byte array
     * @param length of data to be checked
     * @throws NullPointerException if value is null
     * @throws IllegalArgumentException if offset or length
     * are not valid
     */
    private void validateBinaryValue(byte[] value, int offset, int length) {
        if (value == null) {
            throw new NullPointerException("Binary field value"
                +" should not be null");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("Negative offset");
        }
        if (offset + length > value.length) {
            throw new IllegalArgumentException("Offset out of range");
        }
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be at least 1");
        }
        if (value.length == 0) {
            throw new IllegalArgumentException("Binary array value "
                + "has zero length");
        }
    }

    // JAVADOC COMMENT ELIDED
    public String[] getStringArray(int field, int index) {
        checkType(field, STRING_ARRAY);
        return (String[]) getValue(field, index);
    }

    // JAVADOC COMMENT ELIDED
    public void setBoolean(int field, int index, int attributes,
            boolean value) {
        setValue(field, index, attributes, new Boolean(value), false);
    }

    // JAVADOC COMMENT ELIDED
    public PIMList getPIMList() {
        return pimList;
    }

    /**
     * Returns the handle of the PIMList associated with this item.
     *
     * @return   the handle of the PIMList that this item belongs to.  If the
     *           item does not belong to any list, handle of default dummy
     *           list is returned.
     */
    public Object getPIMListHandle() {
        return pimListHandle;
    }

    /**
     * Set the PIMList of this item.
     * @param list data to be saved
     */
    void setPIMList(AbstractPIMList list) {
        this.pimList = list;
        pimListHandle = list.getHandle();
    }

    // JAVADOC COMMENT ELIDED
    public void removeValue(int field, int index) {
        PIMField pimField = getField(field, false, true);
        if (pimField == null) {
            throw new IndexOutOfBoundsException("Empty field: " + field);
        }
        int currentValues = pimField.getValueCount();
        if (index < 0 || index >= currentValues) {
            throw new IndexOutOfBoundsException("0 <= index < "
                + currentValues + ", " + index + " not in range");
        }
        checkReadOnlyFields(field);
        pimField.removeValue(index);
        currentValues --;
        if (currentValues == 0) {
            removeField(field);
        } else if (currentValues == 1) {
            // downgrade field
            Object value = pimField.getValue(0);
            int attributes = pimField.getAttributes(0);
            pimField = new ScalarPIMField();
            pimField.addValue(attributes, value);
            putField(field, pimField);
        }
        modified = true;
    }

    // JAVADOC COMMENT ELIDED
    public String getString(int field, int index) {
        checkType(field, STRING);
        return (String) getValue(field, index);
    }

    // JAVADOC COMMENT ELIDED
    public void setString(int field, int index, int attributes, String value) {
        validateString(value);
        setValue(field, index, attributes, value, false);
    }

    // JAVADOC COMMENT ELIDED
    public boolean isModified() {
        return modified;
    }
    /**
     * Sets the modified flag.
     * @param modified flag to be saved
     */
    void setModified(boolean modified) {
        this.modified = modified;
    }

    // JAVADOC COMMENT ELIDED
    public void commit() throws PIMException {
        if (pimList == null) {
            throw new PIMException("Item is not in a list");
        }
        pimList.checkWritePermission();
        pimList.checkOpen();
        updateRevision();
        setDefaultValues();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PIMFormat format = getEncodingFormat();
            format.encode(baos, "UTF-8", this);
            Object newKey = pimList.commit(key, baos.toByteArray(), categories);
            if (key == null) {
                pimList.addItem(this);
            }
            setKey(newKey);
            updateUID();
            modified = false;
        } catch (IOException e) {
            throw new PIMException("Error persisting PIMItem");
        }
    }

    /**
     * Gets the format codec used to encode and decode this object
     * for storage.
     * @return an instance of PIMFormat that can deal with this item
     */
    abstract PIMFormat getEncodingFormat();

    /**
     * Checks the category is valid.
     * @param category name of category to check
     * @return <code>true</code> if is int category
     */
    boolean isInCategory(String category) {
        if (categories == null) {
            return false;
        } else {
            for (int i = 0; i < categories.length; i++) {
                if (categories[i].equals(category)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Sets the key that identifies this item in the PIM database
     * @param key an Object used to index this item
     */
    void setKey(Object key) {
        this.key = key;
        if (key != null) {
            updateUID();
        }
    }

    /**
     * Gets the key that identifies this item in the PIM database
     * @return an Object used to index this item
     */
    Object getKey() {
        return key;
    }

    /**
     * Removed this PIMItem from its list
     */
    void remove() throws PIMException {
        if (pimList == null) {
            throw new PIMException("Item is not in a list");
        }
        pimList.checkWritePermission();
        pimList.commit(key, null, null);
        setKey(null);
        pimList = null;
    }

    /**
     * Sets default values for this item.
     */
    protected void setDefaultValues() {
        int[] supportedFields = pimList.getSupportedFields();
        for (int i = 0; i < supportedFields.length; i++) {
            int field = supportedFields[i];
            PIMField pimField = getField(field, false, true);
            if ((pimField == null || pimField.getValueCount() == 0)
                && pimHandler.hasDefaultValue(pimListHandle, field)) {

                Object value = null;
                switch (pimList.getFieldDataType(field)) {
                    case PIMItem.BOOLEAN:
                        value = new Boolean(pimHandler
                            .getDefaultBooleanValue(pimListHandle, field));
                        break;
                    case PIMItem.BINARY:
                        value = pimHandler
                            .getDefaultBinaryValue(pimListHandle, field);
                        break;
                    case PIMItem.DATE:
                        value = new Long(pimHandler
                            .getDefaultDateValue(pimListHandle, field));
                        break;
                    case PIMItem.INT:
                        value = new Integer(pimHandler
                            .getDefaultIntValue(pimListHandle, field));
                        break;
                    case PIMItem.STRING:
                        value = pimHandler
                            .getDefaultStringValue(pimListHandle, field);
                        break;
                    case PIMItem.STRING_ARRAY:
                        value = pimHandler
                            .getDefaultStringArrayValue(pimListHandle, field);
                        break;
                    default: continue;
                }
                addValue(field, PIMItem.ATTR_NONE, value, false);
            }
        }
    }

    /**
     * Checks for valid PIM field.
     * @param type list type
     * @param field identifier for field
     * @return <code>true</code> if field is valid
     */
    static boolean isValidPIMField(int type, int field) {
        switch (type) {
            case PIM.CONTACT_LIST:
                return ContactImpl.isValidPIMField(field);
            case PIM.EVENT_LIST:
                return EventImpl.isValidPIMField(field);
            case PIM.TODO_LIST:
                return ToDoImpl.isValidPIMField(field);
            default:
                return false;
        }
    }

    /**
     * Checks the type of a field, throwing an IllegalArgumentException
     * if given or if the field number is invalid.
     * @param field identifier for field
     * @param value data to be checked
     * @throws IllegalArgumentException if data type is not known
     */
    private void checkType(int field, Object value) {
        try {
            int dataType =
                pimHandler.getFieldDataType(pimListHandle, field);
            switch (dataType) {
                case PIMItem.BINARY: {
                    byte[] b = (byte[]) value;
                    break;
                }
                case PIMItem.BOOLEAN: {
                    Boolean b = (Boolean) value;
                    break;
                }
                case PIMItem.DATE: {
                    Long l = (Long) value;
                    break;
                }
                case PIMItem.INT: {
                    Integer i = (Integer) value;
                    break;
                }
                case PIMItem.STRING: {
                    String s = (String) value;
                    break;
                }
                case PIMItem.STRING_ARRAY: {
                    String[] s = (String[]) value;
                    break;
                }
                default:
                    throw complaintAboutField(listType, field);
            }
        } catch (ClassCastException cce) {
            throw new IllegalArgumentException(cce.getMessage());
        }
    }

    /**
     * Checks the type of a field, throwing an IllegalArgumentException
     * if given or if the field number is invalid.
     * @param field identifier of field
     * @param dataType data type for field value
     */
    private void checkType(int field, int dataType) {
        int correctDataType =
            pimHandler.getFieldDataType(pimListHandle, field);
        if (dataType != correctDataType && correctDataType != -1) {
            throw new IllegalArgumentException("Wrong data type");
        }
        if (correctDataType == -1) {
            throw complaintAboutField(listType, field);
        }
    }
    /**
     * Throw an exception based on field failure type.
     * @param type list type
     * @param field identifier of field
     * @return UnsupportedFieldException if the field value is not supported
     * in the field or IllegalArgumentException, if the field is not valid
     */
    static RuntimeException complaintAboutField(int type, int field) {
        if (isValidPIMField(type, field)) {
            return new UnsupportedFieldException(String.valueOf(field));
        } else {
            return new IllegalArgumentException("Invalid field " + field);
        }
    }

    /**
     * Returns the index of the given key, if it is present in
     * fieldKeys[]. If it is not present, returns the binary
     *  complement of the index before which
     * the key could be inserted. O(log fieldKeys.length) in time.
     * @param key property key for requested field
     * @return the index of the field key
     */
    private int findFieldKey(int key) {
        int lowerBound = 0;
        int upperBound = fieldKeys.length;
        while (lowerBound != upperBound) {
            int index = lowerBound + (upperBound - lowerBound) / 2;
            int indexKey = fieldKeys[index];
            if (indexKey > key) {
                if (index == upperBound) {
                    upperBound --;
                } else {
                    upperBound = index;
                }
            } else if (indexKey == key) {
                return index;
            } else {
                if (index == lowerBound) {
                    lowerBound ++;
                } else {
                    lowerBound = index;
                }
            }
        }
        return ~lowerBound;
    }

    /**
     * Stores a PIMField. O(fieldKeys.length) in space and time.
     * @param key property key for requested field
     * @param field identifier of field
     */
    public void putField(int key, PIMField field) {
        int index = findFieldKey(key);
        if (index >= 0) {
            fieldValues[index] = field;
        } else {
            index = ~index;
            int[] newKeys = new int[fieldKeys.length + 1];
            PIMField[] newFields = new PIMField[fieldValues.length + 1];
            System.arraycopy(fieldKeys, 0, newKeys, 0, index);
            System.arraycopy(fieldValues, 0, newFields, 0, index);
            newKeys[index] = key;
            newFields[index] = field;
            System.arraycopy(fieldKeys, index, newKeys, index + 1,
                fieldKeys.length - index);
            System.arraycopy(fieldValues, index, newFields, index + 1,
                fieldKeys.length - index);
            this.fieldKeys = newKeys;
            this.fieldValues = newFields;
        }
    }

    /**
     * Looks up a PIMField. O(log fieldKeys.length) in time.
     * @param key property key for requested field
     * @return PIM field requested
     */
    public PIMField getField(int key) {
        int index = findFieldKey(key);
        if (index >= 0) {
            return fieldValues[index];
        } else {
            return null;
        }
    }

    /**
     * Removes a PIMField. O(fieldKeys.length) in space and time.
     * @param key property key for requested field
     */
    public void removeField(int key) {
        int index = findFieldKey(key);
        if (index >= 0) {
            int[] newKeys = new int[fieldKeys.length - 1];
            PIMField[] newFields = new PIMField[fieldValues.length - 1];
            System.arraycopy(fieldKeys, 0, newKeys, 0, index);
            System.arraycopy(fieldValues, 0, newFields, 0, index);
            System.arraycopy(fieldKeys, index + 1, newKeys, index,
                newKeys.length - index);
            System.arraycopy(fieldValues, index + 1, newFields, index,
                newKeys.length - index);
            this.fieldKeys = newKeys;
            this.fieldValues = newFields;
        }
    }

    /**
     * Checks the read only fields.
     * @param field identifier of the field
     * @throws IllegalArgumentException if field is not read only
     */
    private void checkReadOnlyFields(int field) {
        if (key != null) {
            if (field == getRevisionField()) {
                throw new IllegalArgumentException(
                    "REVISION field is read only"
                        + " except on newly created PIMItems");
            } else if (field == getUIDField()) {
                throw new IllegalArgumentException(
                    "UID field is read only except on newly created PIMItems");
            }
        }
    }

    /**
     * Update the revision time.
     */
    private void updateRevision() {
        Long value = new Long(System.currentTimeMillis());
        int field = getRevisionField();
        if (countValues(field) == 0) {
            addValue(field, 0, value, true);
        } else {
            setValue(field, 0, 0, value, true);
        }
    }

    /**
     * Update the UID field.
     */
    private void updateUID() {
        String value = key.toString();
        int field = getUIDField();
        if (countValues(field) == 0) {
            addValue(field, 0, value, true);
        } // don't change UID value, it can be set by user
    }

    /**
     * Gets the field that corresponds to the REVISION of this PIMItem.
     *
     * REVISION fields have special handling; they are set automatically
     * on commit and import, and are read only to the application once
     * the item has been committed.
     * @return revision field
     */
    protected abstract int getRevisionField();

    /**
     * Gets the field that corresponds to the UID of this PIMItem.
     *
     * UIDfields have special handling; they are set automatically
     * on commit and import.
     * @return UID field
     */
    protected abstract int getUIDField();

    /**
     * Format the data for output.
     * @return formatted data
     */
    protected String formatData() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < fieldValues.length; i++) {
            if (fieldValues[i].getValueCount() != 0) {
                PIMField pimField = fieldValues[i];
                int field = fieldKeys[i];
                int valueCount = pimField.getValueCount();
                if (valueCount == 0) {
                    continue;
                }
                if (i != 0) {
                    sb.append(", ");
                }
                String label =
                    pimHandler.getFieldLabel(pimListHandle, field);
                int dataType =
                    pimHandler.getFieldDataType(pimListHandle, field);
                for (int j = 0; j < valueCount; j++) {
                    sb.append(label);
                    if (valueCount != 1) {
                        sb.append("[");
                        sb.append(j);
                        sb.append("]");
                    }
                    sb.append("=");
                    Object value = pimField.getValue(j);
                    if (value == null) {
                        sb.append("null");
                        continue;
                    }
                    switch (dataType) {
                        case STRING_ARRAY: {
                            String[] aValue = (String[]) value;
                            sb.append("[");
                            for (int k = 0; k < aValue.length; k++) {
                                if (k != 0) {
                                    sb.append(",");
                                }
                                sb.append(aValue[k]);
                            }
                            sb.append("]");
                            break;
                        }
                        case BINARY: {
                            byte[] bValue = (byte[]) value;
                            sb.append("<" + bValue.length + " bytes>");
                            break;
                        }
                        case DATE: {
                            long dValue = ((Long) value).longValue();
                            sb.append(pimHandler.composeDateTime(dValue));
                            break;
                        }
                        default:
                            sb.append(value);
                    }
                }
            }
        }
        if (categories != null && categories.length != 0) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("Categories=[");
            for (int i = 0; i < categories.length; i++) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append(categories[i]);
            }
            sb.append("]");
        }
        return sb.toString();
    }

    /**
     * Converts the record to a printable format.
     * @return formatted record
     */
    protected abstract String toDisplayableString();

    /**
     * Convert the data to a String.
     * @return formatted data
     */
    public String toString() {
        return "true".equals(System.getProperty("pim.debug"))
            ? toDisplayableString()
            : super.toString();
    }

    /**
     * Ensures valid class identifier.
     * @param value class identifier to validate
     * @throws IllegalArgumentException if value is not supported
     */
    private void validateClass(int value) {
        switch (value) {
            case javax.microedition.pim.Contact.CLASS_CONFIDENTIAL:
            case Contact.CLASS_PRIVATE:
            case Contact.CLASS_PUBLIC:
                return;
            default:
                throw new IllegalArgumentException(
                    "Invalid CLASS value: " + value);
        }
    }

    /**
     * Ensures valid priority identifier.
     * @param value priority identifier to validate
     * @throws IllegalArgumentException if value is not supported
     */
    private void validatePriority(int value) {
        if (value < 0 || value > 9) {
            throw new IllegalArgumentException(
                "Invalid PRIORITY value: " + value);
        }
    }
}
