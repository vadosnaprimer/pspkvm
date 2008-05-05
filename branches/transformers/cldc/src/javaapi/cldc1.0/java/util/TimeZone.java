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

package java.util;

import com.sun.cldc.util.j2me.TimeZoneImpl;

/**
 * <code>TimeZone</code> represents a time zone offset, and also figures out daylight
 * savings.
 *
 * <p>
 * Typically, you get a <code>TimeZone</code> using <code>getDefault</code>
 * which creates a <code>TimeZone</code> based on the time zone where the program
 * is running. For example, for a program running in Japan, <code>getDefault</code>
 * creates a <code>TimeZone</code> object based on Japanese Standard Time.
 *
 * <p>
 * You can also get a <code>TimeZone</code> using <code>getTimeZone</code> along
 * with a time zone ID. For instance, the time zone ID for the Pacific
 * Standard Time zone is "PST". So, you can get a PST <code>TimeZone</code> object
 * with:
 * <blockquote>
 * <pre>
 * TimeZone tz = TimeZone.getTimeZone("PST");
 * </pre>
 * </blockquote>
 *
 * <p> This class is a pure subset of the java.util.TimeZone class in J2SE.
 * <p> The only time zone ID that is required to be supported is "GMT".
 * <p>
 * Apart from the methods and variables being subset, the semantics of the
 * getTimeZone() method may also be subset: custom IDs such as "GMT-8:00"
 * are not required to be supported.
 *
 * @see          Calendar
 * @version      1.0 (J2ME MIDP)
 */
public abstract class TimeZone {

  private static TimeZoneImpl defaultZone = null;
  private static String platform = null;
  private static String classRoot = null;

  public TimeZone() {
  }

  /**
   * Gets offset, for current date, modified in case of
   * daylight savings. This is the offset to add *to* GMT to get local time.
   * Gets the time zone offset, for current date, modified in case of daylight
   * savings. This is the offset to add *to* GMT to get local time. Assume
   * that the start and end month are distinct. This method may return incorrect
   * results for rules that start at the end of February (e.g., last Sunday in
   * February) or the beginning of March (e.g., March 1).
   *
   * @param era           The era of the given date (0 = BC, 1 = AD).
   * @param year          The year in the given date.
   * @param month         The month in the given date. Month is 0-based. e.g.,
   *                      0 for January.
   * @param day           The day-in-month of the given date.
   * @param dayOfWeek     The day-of-week of the given date.
   * @param millis        The milliseconds in day in <em>standard</em> local time.
   * @return              The offset to add *to* GMT to get local time.
   * @exception IllegalArgumentException the era, month, day,
   * dayOfWeek, or millis parameters are out of range
   */
  public abstract int getOffset(int era, int year, int month, int day,
                                int dayOfWeek, int millis);

  /**
   * Gets the GMT offset for this time zone.
   *
   * @return the GMT offset for this time zone.
   */
  public abstract int getRawOffset();

  /**
   * Queries if this time zone uses Daylight Savings Time.
   *
   * @return if this time zone uses Daylight Savings Time.
   */
  public abstract boolean useDaylightTime();

  /**
   * Gets the ID of this time zone.
   * @return the ID of this time zone.
   */
  public String getID() {
    return null;
  }

  /**
   * Gets the <code>TimeZone</code> for the given ID.
   * @param ID the ID for a <code>TimeZone</code>, either an abbreviation such as
   * "GMT", or a full name such as "America/Los_Angeles".
   * <p> The only time zone ID that is required to be supported is "GMT".
   *
   * @return the specified TimeZone, or the GMT zone if the given ID cannot be
   * understood.
   */

  /* <p>
   * The following is information for implementers. Applications should not
   * need to be aware of this or rely on it, because each implementation
   * may do it differently:
   * <p>
   * The system will look up a class the name of which includes the platform name.
   * The class name will take the form: <p>
   *
   * <code>{classRoot}.util.{platform}.TimeZoneImpl</code>
   *
   * <p>
   * The classRoot is derived from the system by looking up the system property
   * "microedition.implpath".  If this property key is not found or the 
   * associated class is not present then "com.sun.cldc" is used.
   */
  public static synchronized TimeZone getTimeZone(String ID) {
    if (ID == null) {
        throw new NullPointerException();
    }
    getDefault();
    TimeZone tz = defaultZone.getInstance(ID);
    if (tz == null) {
      tz = defaultZone.getInstance("GMT");
    }
    return tz;
  }

  /**
   * Gets the default <code>TimeZone</code> for this host.
   * The source of the default <code>TimeZone</code>
   * may vary with implementation.
   * @return a default <code>TimeZone</code>.
   */
  public static synchronized TimeZone getDefault() {
    if (defaultZone == null) {
      if (platform == null) {
        /* Setup the platform name */

        platform = "j2me";

        /* See if there is an alternate class root */
        classRoot = System.getProperty("microedition.implpath");
        if (classRoot == null) {
          classRoot = "com.sun.cldc";
        }
      }

      try {
        /* Obtain the time zone implementation class */
        Class clazz = Class.forName(classRoot+".util."+platform+".TimeZoneImpl");

        /* Construct a new instance */
        defaultZone = (TimeZoneImpl)clazz.newInstance();
        defaultZone = (TimeZoneImpl)defaultZone.getInstance(null);
      }
      catch (Exception x) {}
    }

    return defaultZone;
  }

  /** Gets all the available IDs supported.
   * @return  an array of IDs.
   */
  public static String[] getAvailableIDs() {
    getDefault();
    return defaultZone.getIDs();
  }
}
