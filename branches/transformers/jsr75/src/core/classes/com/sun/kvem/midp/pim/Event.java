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
 * Represents a single Event entry in a PIM Event database.
 * The fields are a subset of the fields in the <code>vEvent</code> object
 * defined by the vCalendar 1.0 specification from the Internet Mail Consortium
 * (http://www.imc.org).  The subset represents those fields necessary to
 * provide the relevant information about an Event entry without compromising
 * platform portability.
 * <P>
 * A single event may have multiple occurrences; i.e. the event may be a
 * recurring event that is repeated at specified intervals.  Each occurrence of
 * an event is determined by using a {@link javax.microedition.pim.RepeatRule}
 * to calculate when the
 * event should have additional occurrences, besides the one defined by the
 * <code>Event.START</code> field.
 * </P>
 * <P>The Event class has many different fields that it can support.
 * However, each individual Event object supports only fields valid for its
 * associated list.  Its EventList restricts what fields in a Event are
 * retained.  This reflects that some native Event databases do not support all
 * of the fields available in a Event item.  The methods
 * {@link AbstractPIMList#isSupportedField}
 * and {@link AbstractPIMList#getSupportedFields}
 * can be used to determine if a particular Event field is supported by an
 * EventList and therefore persisted when the Event is committed to its list.
 * Attempts to set or get data based on field IDs not supported in the Event's
 * EventList result in a
 * {@link javax.microedition.pim.UnsupportedFieldException}.
 * </P>
 * <H3>Data</H3>
 * <P>The following table details the explicitly defined fields that may by in
 * an Event.  Implementations may extend the field set using extended fields as
 * defined in PIMItem.
 * </P>
 * <h4>Table: Predefined Fields</h4>
 * <table border=1>
 * <TR>
 * <th> Fields </th><th> Type of Data Associated with Field </th>
 * </tr>
 * <tr><td><code>LOCATION, NOTE, SUMMARY, UID</code></td>
 *     <td><code>PIMItem.STRING</code></td>
 * </tr>
 * <tr><td><code>END, REVISION, START </code></td>
 *     <td><code>PIMItem.DATE</code></td>
 * </tr>
 * <tr><td><code>ALARM, CLASS</code></td>
 *     <td><code>PIMItem.INT</code></td>
 * </tr>
 * </table>
 *
 * <h3>Required Field Support</h3>
 * <P>All Event fields may or may not be supported by a particular list.   This
 * is due to the fact that underlying native databases may not support all of
 * the fields defined in this API.  Support for any of the fields can be
 * determined by the method {@link AbstractPIMList#isSupportedField}.
 * </p><P>
 * Native Event databases may require some of the fields to have values
 * assigned to them in order to be persisted.  If an application does not
 * provide values for these fields, default values are provided for the Event
 * by the VM when the Event is persisted.
 * </P>
 * <h3>Examples</h3>
 * <h4>Explicit Field Use with Field Checking</h4>
 * This first example shows explicit field access in which each field and type
 * ID is properly checked for support prior to use.  This results in code that
 * is more portable across PIM implementations regardless of which specific
 * fields are supported on particular PIM list implementations.  If one of the
 * fields is not supported by the list, the field is not set in the Event.
 * <pre>
 * EventList events = null;
 * try {
 *    events = (EventList) PIM.getInstance().openPIMList(PIM.EVENT_LIST,
 *                                                       PIM.READ_WRITE);
 * } catch (PIMException e) {
 *    // An error occurred
 *    return;
 * }
 * Event event = events.createEvent();
 * if (events.isSupportedField(Event.SUMMARY))
 *      event.addString(Event.SUMMARY, PIMItem.ATTR_NONE, "Meeting with John");
 * if (events.isSupportedField(Event.START))
 *      event.addDate(Event.START, PIMItem.ATTR_NONE, aDate.getTime());
 * if (events.isSupportedField(Event.END))
 *      event.addDate(Event.END, PIMItem.ATTR_NONE, aDate.getTime());
 * if (events.isSupportedField(Event.ALARM))
 *      event.addInt(Event.ALARM, PIMItem.ATTR_NONE, aDate.getTime() - 60000);
 * if (events.isSupportedField(Event.NOTE))
 *      event.addString(Event.NOTE, PIMItem.ATTR_NONE,
 *                      "I phoned on Monday to book this meeting");
 * if (events.maxCategories() != 0 && events.isCategory("Work"))
 *      event.addToCategory("Work");
 * }
 * try {
 *      event.commit();
 * } catch (PIMException e) {
 *      // An error occured
 * }
 * try {
 *      events.close();
 * } catch (PIMException e) {
 * }
 * </pre>
 * <h4>Explicit Field Use with Exception Handling</h4>
 * This second example also shows explicit field access that properly handles
 * optionally supported fields by use of a try catch block with
 * <code>UnsupportedFieldException</code>.  In this case, the setting of the
 * whole Event is rejected if any of the fields are not supported in the
 * particular list implementation.
 * <PRE>
 *  EventList events = null;
 *  try {
 *    events = (EventList) PIM.getInstance().openPIMList(PIM.EVENT_LIST,
 *                                                       PIM.READ_WRITE);
 *  } catch (PIMException e) {
 *      // An error occurred
 *      return;
 *  }
 *  Event event = events.createEvent();
 *
 *  try {
 *      Date aDate = new Date();
 *      event.addString(Event.SUMMARY, PIMItem.ATTR_NONE, "Meeting with John");
 *      event.addDate(Event.START, PIMItem.ATTR_NONE, aDate.getTime());
 *      event.addDate(Event.END, PIMItem.ATTR_NONE, aDate.getTime());
 *      event.addDate(Event.ALARM, PIMItem.ATTR_NONE, aDate.getTime() - 60000);
 *      event.addString(Event.NOTE, PIMItem.ATTR_NONE,
 *                      "I phoned on Monday to book this meeting");
 *      event.addToCategory("Work");
 *
 *  } catch (UnsupportedFieldException e) {
 *    // In this case, we choose not to save the contact at all if any of the
 *    // fields are not supported on this platform.
 *    System.out.println("Event not saved");
 *    return;
 *  }
 *
 *  try {
 *      event.commit();
 *  } catch (PIMException e) {
 *      // An error occured
 *  }
 *  try {
 *      events.close();
 *  } catch (PIMException e) {
 *  }
 * </PRE>
 *
 * @see <A target=_top href="http://www.imc.org/pdi">
 *       Internet Mail Consortium PDI</A>
 * @see EventListImpl
 * @since PIM 1.0
 */

public interface Event extends PIMItem {
    /**
     * Field specifying a relative time for an Alarm for this Event.  Data
     * for this field is expressed with an int data type.  The alarm is
     * expressed in seconds and derived by subtracting the alarm value from 
     * every date/time occurrence of this Event.  For example, if this field has
     * a value of 600, then the alarm first occurs 600 seconds before the 
     * date/time value specified by <code>Event.START</code>.  For 
     * reoccurrences of the event, the alarm is calculated by subtracting the
     * stored value from the date/time of the specific event occurrence.
     * <P>
     * Note that the value provided may be rounded-down by an implementation due
     * to platform restrictions.  For example, should a native Event database
     * only support alarm values with granularity in terms of minutes, then the
     * provided alarm value is rounded down to the nearest minute (e.g.
     * 636 seconds would become 600 seconds).
     * </p>
     */
    public static final int ALARM = 100;

    /**
     * Field specifying the desired access class for this contact.
     * Data associated with this field is of int type, and can be one of the
     * values {@link #CLASS_PRIVATE}, {@link #CLASS_PUBLIC}, or
     * {@link #CLASS_CONFIDENTIAL}.
     */
    public static final int CLASS = 101;

    /**
     * Field specifying the non-inclusive date and time a single Event
     * ends. Data for this field is expressed in the same long value
     * format as java.util.Date, which is milliseconds since the epoch
     * (00:00:00 GMT, January 1, 1970).
     * <P>
     * If <CODE>START</CODE> and <CODE>END</CODE> are the same this event is
     * an all day event. If <CODE>END</code> is specified but <CODE>START</code>
     * is not, the event occurs only at the instance specified by
     * <CODE>END</code>.
     * </P><P>
     * Note that the value provided may be rounded-down by an implementation due
     * to platform restrictions.  For example, should a native Event database
     * only support event date values with granularity in terms of seconds, then
     * the provided date value is rounded down to a date time with a
     * full second.
     * </p>
     *
     * @see #START
     */
    public static final int END = 102;

    /**
     * Field identifying the venue for this Event. Data for this field is a
     * string value.  For example: <BR>
     * "Conference Room - F123, Bldg. 002"
     */
    public static final int LOCATION = 103;

    /**
     * A String specifying a more complete description than the
     * <CODE>SUMMARY</CODE> for this Event.  Data for this field is a string
     * value.
     * For example: <BR>
     * "I phoned John on Friday to book this meeting, he better show"
     */
    public static final int NOTE = 104;

    /**
     * Field specifying the last modification date and time of an Event
     * item.  If the Event has ever been committed to an EventList, then this
     * attribute becomes read only.  This field is set automatically on imports
     * and commits of an Event.  The data for this field is expressed
     * in the same long value format as java.util.Date, which is
     * milliseconds since the epoch (00:00:00 GMT, January 1, 1970).
     * <P>
     * Note that the value provided may be rounded-down by an implementation due
     * to platform restrictions.  For example, should a native Event database
     * only support event date values with granularity in terms of seconds, then
     * the provided date value is rounded down to a date time with a
     * full second.
     * </p>
     */
    public static final int REVISION = 105;

    /**
     * Field specifying the inclusive date and time a single Event
     * starts. The data for this field is expressed
     * in the same long value format as java.util.Date, which is
     * milliseconds since the epoch (00:00:00 GMT, January 1, 1970).
     * <P>
     * If <CODE>START</CODE> and <CODE>END</CODE> are the same this event is
     * an all day event. If <CODE>START</code> is specified but <CODE>END</code>
     * is not, the event occurs only at the instance specified by
     * <CODE>START</code>.
     * <P>
     * Note that the value provided may be rounded-down by an implementation due
     * to platform restrictions.  For example, should a native Event database
     * only support event date values with granularity in terms of seconds, then
     * the provided date value is rounded down to a date time with a
     * full second.
     * </p>
     *
     * @see #END
     */
    public static final int START = 106;

    /**
     * Field specifying the summary or subject for this Event. Data for this
     * field is a string type.  For example: <BR>
     * "Meeting with John"
     */
    public static final int SUMMARY = 107;

    /**
     * Field specifying a unique ID for an Event.  This field can be
     * used to check for identity using <code>String.equals</code>.  UID is
     * read only if the Event has been committed to an EventList at least
     * once in its lifetime. The UID is not set if the
     * Event has never been committed to an EventList;
     * <CODE>countValues(UID)</CODE> returns 0 before a newly
     * created Event object is committed to its list.  The attribute is valid
     * for the persistent life of the Event and may be reused by the platform
     * once this particular Event is deleted.  The data for this field is of
     * string type.
     */
    public static final int UID = 108;

    /**
     * Constant indicating this event's class of access is confidential.
     */
    public static final int CLASS_CONFIDENTIAL = 200;

    /**
     * Constant indicating this event's class of access is private.
     */
    public static final int CLASS_PRIVATE = 201;

    /**
     * Constant indicating this event's class of access is public.
     */
    public static final int CLASS_PUBLIC = 202;
}
