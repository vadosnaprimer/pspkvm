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
 * Represents a single Contact entry in a PIM Contact database.
 * The supported field list for a Contact is also a subset of the fields defined
 * by the vCard specification from the Internet Mail Consortium
 * (http://www.imc.org).  This set of fields included
 * in this Contact class represents those necessary to provide the relevant
 * information about a contact without compromising platform portability.
 * 
 * <P>The Contact class has many different fields that it can support.
 * However, each individual Contact object supports only fields valid for its
 * associated list.  Its ContactList restricts what fields in a Contact are
 * retained.  This reflects that some native Contact databases do not support
 * all of the fields available in a Contact item. The methods
 * {@link AbstractPIMList#isSupportedField} and
 * {@link AbstractPIMList#getSupportedAttributes}
 * can be used to determine if particular Contact fields and types are supported
 * by a ContactList and therefore persisted when the Contact is committed to its
 * list.  Attempts to add or get data based on fields not supported in the
 * Contact's ContactList result in a
 * {@link javax.microedition.pim.UnsupportedFieldException}.
 * </P>
 * <H3>Data</H3>
 * The following table details the explicitly defined fields that may by in a
 * Contact.  Implementations may extend the field set using extended fields as
 * defined in PIMItem.
 * <h4>Table: Standard Fields</h4>
 * <table border=1>
 * <TR>
 * <th> Fields </th>
 * <th> Type of Data Associated with Field </th>
 * </tr>
 * <tr><td><code>NAME, ADDR</code></td>
 *     <td><code>PIMItem.STRING_ARRAY</code></td>
 * </tr>
 * <tr><td><code>EMAIL, FORMATTED_NAME, NICKNAME, NOTE, ORG, TEL, TITLE, UID,
 *               URL</code></td>
 *     <td><code>PIMItem.STRING</code></td>
 * </tr>
 * <tr><td><code>BIRTHDAY, REVISION</code></td>
 *     <td><code>PIMItem.DATE</code></td>
 * </tr>
 * <tr><td><code>PHOTO, PUBLIC_KEY</code></td>
 *     <td><code>PIMItem.BINARY</code></td>
 * </tr>
 * <tr><td><code>PHOTO_URL, PUBLIC_KEY_STRING</code></td>
 *     <td><code>PIMItem.STRING</code></td>
 * </tr>
 * <tr><td><code>CLASS</code></td>
 *     <td><code>PIMItem.INT</code></td>
 * </tr>
 * </table>
 *
 * <h3>Required Field Support</h3>
 * <P>All Contact fields may or may not be supported by a particular list. This
 * is due to the fact that underlying native databases may not support all of
 * the fields defined in this API.  Support for any of the fields can be
 * determined by the method {@link AbstractPIMList#isSupportedField}.
 * </p><P>
 * Native Contact databases may require some of the fields to have values
 * assigned to them in order to be persisted.  If an application does not
 * provide values for these fields, default values are provided for the Contact
 * by the VM when the Contact is persisted.
 * </P>
 * <h3>Examples</h3>
 * <h4>Explicit Field Use with Field Checking</h4>
 * This first example shows explicit field access in which each field
 * is properly checked for support prior to use.  This results in code that
 * is more portable across PIM implementations regardless of which specific
 * fields are supported on particular PIM list implementations.  If one of the
 * fields is not supported by the list, the field is not set in the Contact.
 * <pre>
 * ContactList contacts = null;
 * try {
 *    contacts = (ContactList) PIM.getInstance().openPIMList(PIM.CONTACT_LIST,
 *                                                           PIM.READ_WRITE);
 * } catch (PIMException e) {
 *    // An error occurred
 *    return;
 * }
 * Contact contact = contacts.createContact();
 * String[] addr = new String[contacts.stringArraySize(Contact.ADDR)];
 * String[] name = new String[contacts.stringArraySize(Contact.NAME)];
 *
 * if (contacts.isSupportedField(Contact.NAME_FORMATTED)
 *      contact.addString(Contact.NAME_FORMATTED, PIMItem.ATTR_NONE,
 *                        "Mr. John Q. Public, Esq.");
 * if (contacts.isSupportedArrayElement(Contact.NAME, Contact.NAME_FAMILY))
 *      name[Contact.NAME_FAMILY] = "Public";
 * if (contacts.isSupportedArrayElement(Contact.NAME, Contact.NAME_GIVEN))
 *      name[Contact.NAME_GIVEN] = "John";
 * contact.addStringArray(Contact.NAME, PIMItem.ATTR_NONE, name);
 * if (contacts.isSupportedArrayElement(Contact.ADDR, Contact.ADDR_COUNTRY))
 *      addr[Contact.ADDR_COUNTRY] = "USA";
 * if (contacts.isSupportedArrayElement(Contact.ADDR, Contact.ADDR_LOCALITY))
 *      addr[Contact.ADDR_LOCALITY] = "Coolsville";
 * if (contacts.isSupportedArrayElement(Contact.ADDR, Contact.ADDR_POSTALCODE))
 *      addr[Contact.ADDR_POSTALCODE] = "91921-1234";
 * if (contacts.isSupportedArrayElement(Contact.ADDR, Contact.ADDR_STREET))
 *      addr[Contact.ADDR_STREET] = "123 Main Street";
 * if (contacts.isSupportedField(Contact.ADDR))
 *    contact.addStringArray(Contact.ADDR, Contact.ATTR_HOME, addr);
 * if (contacts.isSupportedField(Contact.TEL))
 *    contact.addString(Contact.TEL, Contact.ATTR_HOME, "613-123-4567");
 * if (contacts.maxCategories() != 0
 *       && contacts.isCategory("Friends"))
 *    contact.addToCategory("Friends");
 * if (contacts.isSupportedField(Contact.BIRTHDAY))
 *    contact.addDate(Contact.BIRTHDAY, PIMItem.ATTR_NONE,
 *                    new Date().getTime());
 * if (contacts.isSupportedField(Contact.EMAIL)) {
 *    contact.addString(Contact.EMAIL,
 *                      Contact.ATTR_HOME | Contact.ATTR_PREFERRED,
 *                      "jqpublic@xyz.dom1.com");
 * }
 * try {
 *      contact.commit();
 * } catch (PIMException e) {
 *      // An error occured
 * }
 * try {
 *      contacts.close();
 * } catch (PIMException e) {
 * }
 * </pre>
 * <h4>Explicit Field Use with Exception Handling</h4>
 * This second example also shows explicit field access that properly handles
 * optionally supported fields by use of a try catch block with
 * <code>UnsupportedFieldException</code>.  In this case, the setting of the
 * whole Contact is rejected if any of the fields are not supported in the
 * particular list implementation.
 * <PRE>
 *  ContactList contacts = null;
 *  try {
 *    contacts = (ContactList) PIM.getInstance().openPIMList(PIM.CONTACT_LIST,
 *                                                           PIM.READ_WRITE);
 *  } catch (PIMException e) {
 *      // An error occurred
 *      return;
 *  }
 *  Contact contact = contacts.createContact();
 *
 *  String[] name = new String[contacts.stringArraySize(Contact.NAME)];
 *  name[Contact.NAME_GIVEN] = "John";
 *  name[Contact.NAME_FAMILY] = "Public";
 *
 *  String[] addr = new String[contacts.stringArraySize(Contact.ADDR)];
 *  addr[Contact.ADDR_COUNTRY] = "USA";
 *  addr[Contact.ADDR_LOCALITY] = "Coolsville";
 *  addr[Contact.ADDR_POSTALCODE] = "91921-1234";
 *  addr[Contact.ADDR_STREET] = "123 Main Street";
 *
 *  try {
 *     contact.addString(Contact.NAME_FORMATTED, PIMItem.ATTR_NONE,
 *                       "Mr. John Q. Public, Esq.");
 *     contact.addStringArray(Contact.NAME, PIMItem.ATTR_NONE, name);
 *     contact.addStringArray(Contact.ADDR, Contact.ATTR_HOME, addr);
 *     contact.addString(Contact.TEL, Contact.ATTR_HOME, "613-123-4567");
 *     contact.addToCategory("Friends");
 *     contact.addDate(Contact.BIRTHDAY, PIMItem.ATTR_NONE,
 *                     new Date().getTime());
 *     contact.addString(Contact.EMAIL,
 *                       Contact.ATTR_HOME | Contact.ATTR_PREFERRED,
 *                       "jqpublic@xyz.dom1.com");
 *
 *  } catch (UnsupportedFieldException e) {
 *    // In this case, we choose not to save the contact at all if any of the
 *    // fields are not supported on this platform.
 *    System.out.println("Contact not saved");
 *    return;
 *  }
 *
 *  try {
 *      contact.commit();
 *  } catch (PIMException e) {
 *      // An error occured
 *  }
 *  try {
 *      contacts.close();
 *  } catch (PIMException e) {
 *  }
 * </PRE>
 *
 *
 * @see <A target=_top href="http://www.imc.org/pdi">Internet
 *   Mail Consortium PDI</A>
 * @see ContactListImpl
 * @since PIM 1.0
 */

public interface Contact extends PIMItem {
    /**
     * Field specifying an address for this Contact. Data for this field is of
     * STRING_ARRAY type.
     */
    public static final int ADDR = 100;

    /**
     * Field for the birthday of the Contact.  Data for this field is
     * expressed in the same long value format as java.util.Date, which is
     * milliseconds since the epoch (00:00:00 GMT, January 1, 1970).
     * <P>
     * Note that the value provided may be rounded-down by an implementation due
     * to platform restrictions.  For example, should a native Contact database
     * only support contact date values with granularity in terms of seconds,
     * then the provided date value is rounded down to a date time with a
     * full second.
     * </p>
     */
    public static final int BIRTHDAY = 101;

    /**
     * Field specifying the desired access class for this contact.
     * Data associated with this field is of int type, and can be one of the
     * values {@link #CLASS_PRIVATE}, {@link #CLASS_PUBLIC}, or
     * {@link #CLASS_CONFIDENTIAL}.
     */
    public static final int CLASS = 102;

    /**
     * Field for an e-mail address.  Data for this field is of String type.
     */
    public static final int EMAIL = 103;

    /**
     * Field represents a formatted version of a complete address for the
     * Contact entry.  This string is typically a single string containing the
     * complete address separated with CRLF separators.  This field is typically
     * present for contact databases that support only one field for a contact's
     * address, or for specifying address label format.  Data for this field is
     * of STRING type.
     * For example:<BR>
     * "123 Main St.
     *  Anytown, CA 99999
     *  USA"
     */
    public static final int FORMATTED_ADDR = 104;

    /**
     * Field represents a formatted version of a name for the Contact
     * entry.  Data for this field is of STRING type. The string data associated
     * with this field conforms to the X.520 Common Name attribute format.
     * For example:<BR>
     * "Mr. John Q. Public, Esq."
     */
    public static final int FORMATTED_NAME = 105;

    /**
     * Field specifying the name for this contact. Data for this field is of
     * STRING_ARRAY type.
     */
    public static final int NAME = 106;

    /**
     * Field where the data represents a nickname. Data for this field is of
     * STRING type.
     * For example: <BR>
     * "Copier Man"
     */
    public static final int NICKNAME = 107;

    /**
     * Field specifying supplemental information or a comment associated
     * with a Contact. Data for this field is of
     * String type. The data associated with this field follows the
     * X.520 Description data format.  For example: <BR>
     * "The fax number is operational 0800 to 1715 EST, Mon-Fri."
     */
    public static final int NOTE = 108;

    /**
     * Field specifying the organization name or units associated with a
     * Contact. Data for this field is of
     * String type. The data associated with this field is based on the X.520
     * Organization data format.  For example: <BR>
     * "ABC Inc."
     */
    public static final int ORG = 109;

    /**
     * Field specifying a photo for a Contact.  Data associated with this field
     * is inline binary.  Manipulation of this field may affect data stored in
     * the <code>PHOTO_URL</code> field since some implementation may use the
     * same memory for both fields (e.g. one can either have PHOTO or have
     * PHOTO_URL but not both).
     *
     * @see AbstractPIMList#isSupportedField
     */
    public static final int PHOTO = 110;

    /**
     * Field specifying a photo of a Contact.  Data associated
     * with this field is of String type, representing a URL for the photo.
     * Manipulation of this field may affect data stored in the
     * <code>PHOTO</code> field since some implementation may use the same
     * memory for both fields (e.g. one can either have PHOTO or have PHOTO_URL
     * but not both).
     *
     * @see AbstractPIMList#isSupportedField
     */
    public static final int PHOTO_URL = 111;

    /**
     * Field specifying the public encryption key for a Contact.
     * Data associated with this field is inline binary encoded data.
     * Manipulation of this field may affect data stored in the
     * <code>PUBLIC_KEY_STRING</code> field since some implementation may use
     * the same memory for both fields (e.g. one can either have PUBLIC_KEY or
     * have PUBLIC_KEY_STRING but not both).
     */
    public static final int PUBLIC_KEY = 112;

    /**
     * Field specifying the public encryption key for a Contact.
     * Data associated with this field is of String type.
     * Manipulation of this field may affect data stored in the
     * <code>PUBLIC_KEY</code> field since some implementation may use the same
     * memory for both fields (e.g. one can either have PUBLIC_KEY or have
     * PUBLIC_KEY_STRING but not both).
     */
    public static final int PUBLIC_KEY_STRING = 113;

    /**
     * Field specifying the last modification date and time of a Contact
     * item.  If the Contact has ever been committed to a ContactList, then
     * this attribute becomes read only.  This field is set automatically on
     * imports and commits of a Contact.  Data for this field is expressed
     * in the same long value format as java.util.Date, which is
     * milliseconds since the epoch (00:00:00 GMT, January 1, 1970).
     * <P>
     * Note that the value provided may be rounded-down by an implementation due
     * to platform restrictions.  For example, should a native Contact database
     * only support contact date values with granularity in terms of seconds,
     * then the provided date value is rounded down to a date time with a
     * full second.
     * </p>
     */
    public static final int REVISION = 114;

    /**
     * Field for a voice telephone number. Data associated with this field is
     * of String type and can be any valid String.  No telephone formatting
     * is enforced since many native implementations allow free form text to
     * be associated with TEL fields.
     */
    public static final int TEL = 115;

    /**
     * Field specifying the job title for a Contact.  Data for this field is of
     * String type. This title is based on the X.520 Title attributes.
     * For example: <BR>
     * "Director, Research and Development"
     */
    public static final int TITLE = 116;

    /**
     * Field specifying a unique ID for a Contact.  This field can be
     * used to check for identity using <code>String.equals</code>.  UID is
     * read only if the Contact has been committed to a ContactList at least
     * once in its lifetime. The UID is not set if the
     * Contact has never been committed to a ContactList;
     * <CODE>countValues(UID)</CODE> returns 0 before a newly created
     * Contact object is committed to its list.   The attribute is valid
     * for the persistent life of the Contact and may be reused by the platform
     * once this particular Contact is deleted. Data for this field is of
     * String data type.
     */
    public static final int UID = 117;

    /**
     * Field specifying the uniform resource locator for a Contact. Data for
     * this field is of String data type.
     * For example: <BR>
     * "http://www.swbyps.restaurant.french/~chezchic.html"
     */
    public static final int URL = 118;

    /**
     * Attribute classifying a data value as related to an ASSISTANT.
     */
    public static final int ATTR_ASST = 1;

    /**
     * Attribute classifying a data value as related to AUTO.
     */
    public static final int ATTR_AUTO = 2;

    /**
     * Attribute classifying a data value as related to FAX.
     */
    public static final int ATTR_FAX = 4;

    /**
     * Attribute classifying a data value as related to HOME.
     */
    public static final int ATTR_HOME = 8;

    /**
     * Attribute classifying a data value as related to MOBILE.
     */
    public static final int ATTR_MOBILE = 16;

    /**
     * Attribute classifying a data value as "OTHER".
     */
    public static final int ATTR_OTHER = 32;

    /**
     * Attribute classifying a data value as related to PAGER.
     */
    public static final int ATTR_PAGER = 64;

    /**
     * Attribute classifying a data value with preferred status for
     * retrieval or display purposes (platform specific).  Only one value in
     * a field may be marked as preferred.  Subsequent assigning of preferred
     * status to values in a field overrides any previous preferred status
     * indications.
     */
    public static final int ATTR_PREFERRED = 128;

    /**
     * Attribute classifying a data value as related to SMS.
     */
    public static final int ATTR_SMS = 256;

    /**
     * Attribute classifying a data value as related to WORK.
     */
    public static final int ATTR_WORK = 512;

    /**
     * Index into the string array for an address field, where the data at
     * this index represents the post office box of a particular address. Data
     * for this field is of String type.
     */
    public static final int ADDR_POBOX = 0;

    /**
     * Index into the string array for an address field, where the data at
     * this index represents any extra info of a particular address.  Data for
     * this field is of String type.
     */
    public static final int ADDR_EXTRA = 1;

    /**
     * Index into the string array for an address field, where the data at
     * this index represents the street information of a particular address.
     * Data for this field is of String type.
     */
    public static final int ADDR_STREET = 2;

    /**
     * Index into the string array for an address field, where the data at
     * this index represents the locality (for example, a city) of a particular
     * address. Data for this field is of String type.
     */
    public static final int ADDR_LOCALITY = 3;

    /**
     * Index into the string array for an address field, where the data at
     * this index represents the region (for example, a province, state, or
     * territory) of a particular address.
     * Data for this field is of String type.
     */
    public static final int ADDR_REGION = 4;

    /**
     * Index into the string array for an address field, where the data at
     * this index represents the postal code (for example, a zip code) of a
     * particular address. Data for this field is of String type.
     */
    public static final int ADDR_POSTALCODE = 5;

    /**
     * Index into the string array for an address field, where the data at
     * this index represents the country of a particular address.  Data for this
     * field is of String type.
     */
    public static final int ADDR_COUNTRY = 6;

    /**
     * Index into the string array for a name field, where the data at
     * this index represents the family name.
     * For example: <BR>
     * "Stevenson"
     */
    public static final int NAME_FAMILY = 0;

    /**
     * Index into the string array for a name field, where the data at
     * this index represents the given name.
     * For example: <BR>
     * "Johnathan"
     */
    public static final int NAME_GIVEN = 1;

    /**
     * Index into the string array for a name field, where the data at
     * this index represents other alternate name or names.
     * For example: <BR>
     * "John, Johnny"
     */
    public static final int NAME_OTHER = 2;

    /**
     * Index into the string array for a name field, where the data at
     * this index represents a prefix to a name.
     * For example: <BR>
     * "Dr."
     */
    public static final int NAME_PREFIX = 3;

    /**
     * Index into the string array for a name field, where the data at
     * this index represents a suffix to a name.
     * For example: <BR>
     * "M.D., A.C.P."
     */
    public static final int NAME_SUFFIX = 4;

    /**
     * Constant indicating this contact's class of access is confidential.
     */
    public static final int CLASS_CONFIDENTIAL = 200;

    /**
     * Constant indicating this contact's class of access is private.
     */
    public static final int CLASS_PRIVATE = 201;

    /**
     * Constant indicating this contact's class of access is public.
     */
    public static final int CLASS_PUBLIC = 202;

}
