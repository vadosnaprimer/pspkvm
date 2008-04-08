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
 * Represents a single To Do item in a PIM To Do database.
 * The fields are a subset of the fields in <code>VTODO</code> defined by the
 * vCalendar specification from the Internet Mail Consortium
 * (http://www.imc.org).  The subset represents those fields necessary to
 * provide enough information about a ToDo item without compromising platform
 * portability.
 * <P>The ToDo class has many different field IDs that it can support.  However,
 * each individual ToDo object supports only fields valid for its associated
 * list.  Its ToDoList restricts what fields in a ToDo are retained.  This
 * reflects that some native  ToDo databases do not support all of the fields
 * available in a ToDo item.
 * The methods {@link AbstractPIMList#isSupportedField}
 * and {@link AbstractPIMList#getSupportedFields} can be used to determine if a
 * particular ToDo field is supported by a ToDoList and therefore persisted when
 * the ToDo is committed to its list.  Attempts to set or get data based on
 * field IDs not supported in the ToDo's ToDoList result in a
 * {@link javax.microedition.pim.UnsupportedFieldException}.
 * </P>
 * <H3>Data</H3>
 * <P>
 * </P>
 * <h4>Table: Standard Fields</h4>
 * <table border=1>
 * <TR>
 * <th> Fields </th><th> Type of Data Associated with Field </th>
 * </tr>
 * <tr><td><code>NOTE, SUMMARY, UID</code></td>
 *     <td><code>PIMItem.STRING</code></td>
 * </tr>
 * <tr><td><code>CLASS, PRIORITY</code></td>
 *     <td><code>PIMItem.INT</code></td>
 * </tr>
 * <tr><td><code>COMPLETION_DATE, DUE, REVISION </code></td>
 *     <td><code>PIMItem.DATE</code></td>
 * </tr>
 * <tr><td><code>COMPLETED</code></td>
 *     <td><code>PIMItem.BOOLEAN</code></td>
 * </tr>
 * </table>
 *
 * <h3>Required Field Support</h3>
 * <P>All ToDo fields may or may not be supported by a particular list.   This
 * is due to the fact that underlying native databases may not support all of
 * the fields defined in this API.  Support for any of the fields can be
 * determined by the method {@link AbstractPIMList#isSupportedField}.
 * </p><P>
 * Native ToDo databases may require some of the fields to have values
 * assigned to them in order to be persisted.  If an application does not
 * provide values for these fields, default values are provided for the ToDo
 * by the VM when the ToDo is persisted.
 * </P>
 * <h3>Examples</h3>
 * <h4>Explicit Field Use with Field Checking</h4>
 * This first example shows explicit field access in which each field and type
 * ID is properly checked for support prior to use.  This results in code that
 * is more portable across PIM implementations regardless of which specific
 * fields are supported on particular PIM list implementations.  If one of the
 * fields is not supported by the list, the field is not set in the ToDo.
 * <pre>
 * ToDoList todos = null;
 * try {
 *    todos = (ToDoList) PIM.getInstance().openPIMList(PIM.TODO_LIST,
 *                                                     PIM.READ_WRITE);
 * } catch (PIMException e) {
 *    // An error occurred
 *    return;
 * }
 * ToDo todo = todos.createToDo();
 * if (todos.isSupportedField(Event.SUMMARY))
 *      todo.addString(ToDo.SUMMARY, PIMItem.ATTR_NONE,
 *                     "Buy going away present for Judy");
 * if (todos.isSupportedField(Event.DUE))
 *      todo.addDate(ToDo.DUE, PIMItem.ATTR_NONE, new Date().getTime());
 * if (todos.isSupportedField(Event.NOTE))
 *      todo.addString(ToDo.NOTE, PIMItem.ATTR_NONE,
 *                     "Judy really likes stained glass and expensive pens");
 * if (todos.isSupportedField(Event.PRIORITY))
 *      todo.addInt(ToDo.PRIORITY, PIMItem.ATTR_NONE, 2);
 * if (todos.maxCategories() != 0 && todos.isCategory("Work"))
 *      todo.addToCategory("Work");
 * }
 * try {
 *      todo.commit();
 * } catch (PIMException e) {
 *      // An error occured
 * }
 * try {
 *      todos.close();
 * } catch (PIMException e) {
 * }
 * </pre>
 * <h4>Explicit Field Use with Exception Handling</h4>
 * This second example also shows explicit field access that properly handles
 * optionally supported fields by use of a try catch block with
 * <code>UnsupportedFieldException</code>.  In this case, the setting of the
 * whole ToDo is rejected if any of the fields are not supported in the
 * particular list implementation.
 * <PRE>
 *  ToDoList todos = null;
 *  try {
 *    todos = (ToDoList) PIM.getInstance().openPIMList(PIM.TODO_LIST,
 *                                                     PIM.READ_WRITE);
 *  } catch (PIMException e) {
 *      // An error occurred
 *      return;
 *  }
 *  ToDo todo = todos.createToDo();
 *
 *  try {
 *      todo.addString(ToDo.SUMMARY, PIMItem.ATTR_NONE,
 *          "Buy going away present for Judy");
 *      todo.addDate(ToDo.DUE, PIMItem.ATTR_NONE, new Date().getTime());
 *      todo.addString(ToDo.NOTE, PIMItem.ATTR_NONE,
 *          "Judy really likes stained glass and expensive pens");
 *      todo.addInt(ToDo.PRIORITY, PIMItem.ATTR_NONE, 2);
 *      todo.addToCategory("Work");
 *
 *  } catch (UnsupportedFieldException e) {
 *    // In this case, we choose not to save the ToDo at all if any of the
 *    // fields are not supported on this platform.
 *    System.out.println("Todo not saved");
 *    return;
 *  }
 *
 *  try {
 *      todo.commit();
 *  } catch (PIMException e) {
 *      // An error occured
 *  }
 *  try {
 *      todos.close();
 *  } catch (PIMException e) {
 *  }
 * </PRE>
 *
 * @see <A target=_top href="http://www.imc.org/pdi">
 *      Internet Mail Consortium PDI</A>
 * @see ToDoListImpl
 * @since PIM 1.0
 */

public interface ToDo extends PIMItem {
    /**
     * Field specifying the desired access class for this contact.
     * Data associated with this field is of int type, and can be one of the
     * values {@link #CLASS_PRIVATE}, {@link #CLASS_PUBLIC}, or
     * {@link #CLASS_CONFIDENTIAL}.
     */
    public static final int CLASS = 100;

    /**
     * Field ID indicating a ToDo has been completed.  Data for this field is
     * of boolean type.
     */
    public static final int COMPLETED = 101;

    /**
     * Field ID indicating a ToDo has been completed on the date indicated by
     * this field. The data for this field is expressed in the same long value
     * format as java.util.Date, which is milliseconds since the epoch
     * (00:00:00 GMT, January 1, 1970).
     * <P>
     * Note that the value provided may be rounded-down by an implementation due
     * to platform restrictions.  For example, should a native ToDo database
     * only support todo date values with granularity in terms of seconds, then
     * the provided date value is rounded down to a date time with a
     * full second.
     * </p>
     */
    public static final int COMPLETION_DATE = 102;

    /**
     * The date a ToDo is due.The data for this field is expressed in the same
     * long value format as java.util.Date, which is milliseconds since the
     * epoch (00:00:00 GMT, January 1, 1970).
     * <P>
     * Note that the value provided may be rounded-down by an implementation due
     * to platform restrictions.  For example, should a native ToDo database
     * only support todo date values with granularity in terms of seconds, then
     * the provided date value is rounded down to a date time with a
     * full second.
     * </p>
     */
    public static final int DUE = 103;

    /**
     * Field specifying a more complete description than the SUMMARY for this
     * ToDo.  Data for this field is of string type.  For example: <BR>
     * "Judy really likes stained glass and expensive pens"
     */
    public static final int NOTE = 104;

    /**
     * Field specifying the priority of this ToDo. The priority is a value
     * from zero to nine. Zero specifies an undefined priority, one specifies
     * the highest priority and nine the lowest priority. It is not guaranteed
     * that this value remains unchanged after setting the value and/or
     * persistence of the ToDo item due to underlying native database
     * priority support and mappings to the native priority values.  Data for
     * this field is of int type.
     */
    public static final int PRIORITY = 105;

    /**
     * Field specifying the last modification date and time of a ToDo
     * item.  If the ToDo has ever been committed to a ToDoList, then this
     * attribute becomes read only.  This field is set automatically on imports
     * and commits of a ToDo. Data for this field is expressed
     * in the same long value format as java.util.Date, which is
     *  milliseconds since the epoch (00:00:00 GMT, January 1, 1970).
     * <P>
     * Note that the value provided may be rounded-down by an implementation
     * due to platform restrictions. For example, should a native ToDo database
     * only support todo date values with granularity in terms of seconds, then
     * the provided date value is rounded down to a date time with a
     * full second.
     * </p>
     */
    public static final int REVISION = 106;

    /**
     * Field specifying the summary or subject for this ToDo.
     * Data for this field is of string type. For example: <BR>
     * "Buy going away present for Judy"
     */
    public static final int SUMMARY = 107;

    /**
     * Field specifying a unique ID for a ToDo.  This field can be
     * used to check for identity using <code>String.equals</code>.  UID is
     * read only if the ToDo has been committed to a ToDoList at least
     * once in its lifetime. The UID is not set if the
     * ToDo has never been committed to a ToDoList;
     * <CODE>countValues(UID)</CODE> returns 0 before a newly
     * created ToDo object is committed to its list.  The attribute is valid
     * for the persistent life of the ToDo and may be reused by the platform
     * once this particular ToDo is deleted.  Data for this field is of string
     * type.
     */
    public static final int UID = 108;

    /**
     * Constant indicating this todo's class of access is confidential.
     */
    public static final int CLASS_CONFIDENTIAL = 200;

    /**
     * Constant indicating this todo's class of access is private.
     */
    public static final int CLASS_PRIVATE = 201;

    /**
     * Constant indicating this todo's class of access is public.
     */
    public static final int CLASS_PUBLIC = 202;


}
