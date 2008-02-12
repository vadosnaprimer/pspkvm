/*
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

package javax.microedition.media.control;


/**
 * <code>StopTimeControl</code> allows one to specify a preset stop time for
 * a <code>Player</code>.
 * <p>
 */
public interface StopTimeControl extends javax.microedition.media.Control {

    /**
     * Returned by <CODE>getStopTime</CODE> if no stop-time is set.
     * <p>
     * Value <code>Long.MAX_VALUE</code> is assigned to <code>RESET</code>.
     */
    long RESET = Long.MAX_VALUE;
    
    /**
     *
     * Sets the <i>media time</i> at which you want the <code>Player</code>
     * to stop.
     * The <code>Player</code> will stop when its <i>media time</i>
     * reaches the stop-time.  
     * A <code>STOPPED_AT_TIME</code> event
     * will be delivered through the <code>PlayerListener</code>.
     * <p>
     * The <code>Player</code> is guaranteed
     * to stop within one second past the preset stop-time
     * (i.e. <code>stop-time <= current-media-time <= stop-time + 1 sec.</code>); 
     * unless the current media time is already passed the preset stop time
     * when the stop time is set.
     * If the current media time is already past the stop time set,
     * the <code>Player</code> will stop immediately.  A 
     * <code>STOPPED_AT_TIME</code> event will be delivered.
     * After the <code>Player</code> stops due to the stop-time set,
     * the previously set stop-time will be cleared automatically.
     * Alternatively, the stop time can be explicitly removed by
     * setting it to: <code>RESET</code>.
     * <p>
     *
     * You can always call <code>setStopTime</code> on a stopped
     * <code>Player</code>.
     * To avoid a potential race condition, it is illegal to
     * call <code>setStopTime</code> on a started <code>Player</code> if a
     * <i>media stop-time</i> has already been set.
     *
     * @param stopTime The time in microseconds at which you want the
     * <code>Player</code> to stop, in <i>media time</i>.
     * @exception IllegalStateException Thrown if
     * <code>setStopTime</code> is called on a started
     * <code>Player</code> and the
     * <i>media stop-time</i> has already been set.
     * @see #getStopTime
     */
    void setStopTime(long stopTime);

    /**
     * Gets the last value successfully set by <CODE>setStopTime</CODE>.
     * 
     * Returns the constant <CODE>RESET</CODE> if no stop time is set.
     * This is the default.
     *
     * @return The current stop time in microseconds.
     * @see #setStopTime
     */
    long getStopTime();
}

