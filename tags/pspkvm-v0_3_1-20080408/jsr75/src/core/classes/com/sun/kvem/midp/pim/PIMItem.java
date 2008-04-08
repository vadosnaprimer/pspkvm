/*
 *   
 *
 * Portions Copyright  2000-2007 Sun Microsystems, Inc. All Rights
 * Reserved.  Use is subject to license terms.
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
/*
 * Copyright (C) 2002-2003 PalmSource, Inc.  All Rights Reserved.
 */

package com.sun.kvem.midp.pim;

/**
 * Represents the common interfaces of an item for a PIM list.
 * A PIM item represents a collection of data for a single PIM entry.
 * A PIM item is created from a particular PIM list and is associated with
 * that list for the life of the item.  PIM items can have its data
 * imported and exported using standard byte based formats.  Each implementing
 * class defines what formats can be imported and exported for that item.
 * <H3>Fields</H3>
 * <P>PIMItems reference its data through <i>fields</i>.  A field is a grouping
 * of data values that all have similar characteristics.  An example of a field
 * is TEL, which indicates data values for that particular field are telephone
 * numbers. Classes implementing the PIMItem interface defines the possible
 * fields that for that specific class (e.g TEL is defined in the
 * <code>Contact</code> interface as a field that a contact may support).
 * </P><P>
 * PIM implementations are not required to support all of the possible fields
 * defined in the classes implementing the PIMItem interface.  This is because
 * no native PIM databases contain all of the fields defined in this API.
 * The PIMList that a PIMItem belongs to determines what fields a PIMItem can
 * support and store (all PIMItems in a particular PIMList support the
 * same set of fields). The {@link AbstractPIMList#getSupportedFields} method
 * from a
 * particular PIMItem's PIMList is used to find out what fields are supported
 * within this item.  Since not all possible fields are actually supported in a
 * particular PIMItem, <B>all fields should be checked for support in the item's
 * PIMList using {@link AbstractPIMList#isSupportedField} prior to being used
 * in any retrieval or storage method.</B>
 * </p><p>
 * Each field has the following pieces of information available for it:
 * <UL>
 * <LI>Zero or more <i>data values</I> associated with the Field
 * <LI><i>Attributes</I> for data values for the Field
 * <LI>Descriptive <i>label</I> for the Field
 * <LI><I>Data Type</i> of the data associated with the Field
 * </UL>
 * <h5>Data Values in a Field</h5>
 * <p>A single field can have zero or more data values associated with it at any
 * instance.  All values within a field have the same data type as dictated by
 * the field (for example, all Contact.TEL field data values must be of STRING
 * type).  The data type of a field determines the add/get/set methods to use
 * for accessing the data values (for example, if a field requires STRING data
 * types, then addString, getString, and setString methods are used to access
 * the data).
 * </p><P>
 * Data values within a field are treated as a variable-length array of values,
 * very similar to the behavior of a <code>Vector</code>.  As such, the
 * following rules apply for accessing data values for fields:<br>
 * <ul>
 * <li>Values are added using the appropriate addXXX() method.  The value is
 * appended as the last data value in the field's array, similar to
 * <code>Vector.addElement</code>.</li>
 * <li>Values are retrieved one at a time using the appropriate getXXX() method
 * with an index.  The index is an array index into the field's array of data
 * values. Values are assigned a sequential index beginning from 0 for the first
 * value in a field up to n-1, where n is the total number of values currently
 * assigned to the field. This behavior is similar to the method
 * <code>Vector.elementAt()</code>.</li>
 * <li>Values are removed from a field by using the method
 * {@link AbstractPIMItem#removeValue}.
 * All indexes in the field's array are guaranteed by the implementation to
 * contain an assigned value.  Therefore, removing fields from the middle of a
 * field's array causes compacting of the array and reindexing of the data
 * values. This is similar behavior to the method
 * <code>Vector.removeElement(Object)</code>.</li>
 * </ul>
 * <h5>Field Labels</h5>
 * <p>Each field has a human readable <i>label</i>, usually used for display
 * purposes.
 * The label can be retrieved through {@link AbstractPIMList#getFieldLabel}.
 * </p>
 * <h5>Field Data Types</h5>
 * <p>The data values for a field has a <i>data type</i>, such as {@link #INT},
 * {@link #BINARY}, {@link #BOOLEAN}, {@link #DATE}, {@link #STRING_ARRAY} or
 * {@link #STRING}.  The data type of the field's data can be retrieved
 * through {@link AbstractPIMList#getFieldDataType}.
 * All data values for a particular field have the same data type.
 * </p>
 * <h4>Standard and Extended Fields</h4>
 * <p>Fields can be classified into two logical divisions: standard fields and
 * extended fields.  This division of fields generally determines the
 * portability of the fields across implementations. Standard fields are
 * specifically defined within the javax.microedition.pim package and may be
 * available on almost all PIM implementations.  Extended fields are platform
 * specific fields defined by an individual implementation and are therefore
 * generally not portable across different devices.  Extended fields are
 * generally defined in vendor specific classes derived from this class.
 * </p>
 * <h5>Standard Fields</h5>
 * <P>Standard fields are fields that have IDs explicitly defined as part of the
 * PIM APIs in the javax.microedition.pim package. These fields are the common
 * fields among PIM lists and are more likely to be portable across PIM
 * implementations (but not guaranteed since not all platforms support the same
 * fields in a PIMItem).
 * </P>
 * <h5>Extended Fields</h5>
 * <p>Extended fields are fields that do not have a specific field explicitly
 * defined in the javax.microedition.pim package, but are defined in
 * vendor-specific classes in a separate vendor package.  These fields may or
 * may not be exposed publicly in vendor specific classes. Vendors are allowed
 * to extend the field set for any of the PIM items in this manner to address
 * any platform specific fields they wish to support.  Users can find out if a
 * field is an extended field by comparing its value against
 * {@link #EXTENDED_FIELD_MIN_VALUE}, find out the field's allowed data type
 * through the method {@link AbstractPIMList#getFieldDataType}, and find out
 * the field's label through the method {@link AbstractPIMList#getFieldLabel}.
 * </p>
 * <h4>Attributes</h4>
 * <p>Optional attributes can be provided to further describe individual data
 * values for a field.  Attributes are specified when adding data values to a
 * field.  These attributes are hints to the underlying implementation providing
 * more information about the data value than just a field can provide.  Since
 * they are hints, they may or may not be ignored by the implementation when
 * adding the data values.  The actual attributes used and associated with the
 * data values after adding can be retrieved by the method
 * {@link AbstractPIMItem#getAttributes}.
 * Attributes can also have human readable labels associated with them,
 * retrieved by the method {@link AbstractPIMList#getAttributeLabel}.
 * If no attributes are to be associated with a data value, then
 * {@link #ATTR_NONE} must be used.
 * </p><p>
 * Attributes are handled in the API using a single bit to indicate a specific
 * attribute and using int values as bit arrays to indicate a set of attributes.
 * int values can be checked to see if they contain a specific attribute by
 * using bitwise AND (&) with the attribute and the int value.
 * {@link #ATTR_NONE} is a special attribute that indicates no attributes are
 * set and has a value of 0 that erases all other attributes previously set.
 * </P>
 * <h5>Extended Attributes</h5>
 * <p>Optional attributes may also be extended by vendors and their PIM API
 * implementations.  These extended attributes also may or may not be exposed
 * publicly in vendor specific classes.  The label for these attributes can
 * be retrieved through {@link AbstractPIMList#getAttributeLabel}.
 * </p>
 * <h3>Categories</h3>
 * <p>Categories are string items assigned to an item to represent the
 * item's inclusion in a logical grouping.  The category string
 * correspond to category values already existing in the PIMItem's
 * associated PIMList.  Category support per list is optional, depending
 * on the implementing PIMList class that the
 * item is associated with.  The item's list determines if categories can be
 * assigned, and how many categories can be assigned per item.
 * </p>
 *
 * @since PIM 1.0
 */

public interface PIMItem {

    /**
     * Data type indicating data is binary in a byte array.
     * Data associated with <code>BINARY</code> is
     * retrieved via {@link AbstractPIMItem#getBinary}
     * and added via {@link AbstractPIMItem#addBinary}.
     */
    public static final int BINARY = 0;
    
    /**
     * Data type indicating data is of boolean primitive data type.
     * Data associated with <code>BOOLEAN</code> is
     * retrieved via {@link AbstractPIMItem#getBoolean}
     * and added via {@link AbstractPIMItem#addBoolean}.
     */
    public static final int BOOLEAN = 1;
    
    /**
     * Data type indicating data is a Date in long primitive data type format
     * expressed in the same long value format as java.util.Date, which is
     * milliseconds since the epoch (00:00:00 GMT, January 1, 1970).
     * Data associated with <code>DATE</code> is
     * retrieved via {@link AbstractPIMItem#getDate}
     * and added via {@link AbstractPIMItem#addDate}.
     */
    public static final int DATE = 2;
    
    /**
     * Data type indicating data is of int primitive data type.
     * Data associated with <code>INT</code> is
     * retrieved via {@link AbstractPIMItem#getInt}
     * and added via {@link AbstractPIMItem#addInt}.
     */
    public static final int INT = 3;
    
    /**
     * Data type indicating data is a String object.
     * Data associated with <code>STRING</code> is
     * retrieved via {@link AbstractPIMItem#getString}
     * and added via {@link AbstractPIMItem#addString}.
     */
    public static final int STRING = 4;
    
    /**
     * Data type indicating data is a array of related fields returned in a
     * string array. Data associated with <code>STRING_ARRAY</code> is
     * retrieved via {@link AbstractPIMItem#getStringArray}
     * and added via {@link AbstractPIMItem#addStringArray}.
     */
    public static final int STRING_ARRAY = 5;
    
    /**
     * Constant indicating that no additional attributes are applicable to
     * a data value for a field.
     */
    public static final int ATTR_NONE = 0;
    
    /**
     * Constant indicating the minimum possible value for an extended field
     * constant.
     */
    public static final int EXTENDED_FIELD_MIN_VALUE = 0x1000000;
    
    /**
     * Constant indicating the minimum possible value for an extended attribute
     * constant.
     */
    public static final int EXTENDED_ATTRIBUTE_MIN_VALUE = 0x1000000;
}
