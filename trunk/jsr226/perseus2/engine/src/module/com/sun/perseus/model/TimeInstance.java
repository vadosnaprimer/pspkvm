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
package com.sun.perseus.model;

/**
 * Base class for representing time instances in a 
 * <code>TimedElementSupport</code>'s begin and end instance times lists.
 *
 * @version $Id: TimeInstance.java,v 1.2 2006/04/21 06:39:25 st125089 Exp $
 */
class TimeInstance {
    /**
     * The associated <code>TimeElement</code>
     */
    TimedElementSupport timedElement;

    /**
     * If true, this <code>TimeInstance</code> should be cleared from
     * the <code>TimedElementSupport</code> intance list on reset.
     */
    boolean clearOnReset;

    /**
     * This instance's time.
     */
    Time time;

    /**
     * True if this instance is in the begin instance list.
     * Fals if it is part of an end instance list.
     */
    boolean isBegin;

    /**
     * Builds an instance time for the input <code>TimedElementSupport</code>
     * and time. The constructor will insert the <code>Instance</code>
     * automatically into the <code>TimedElementSupport</code> corresponding
     * instance list.
     *
     * @param timedElement the associated <code>TimedElementSupport</code>
     * @param time the instance time value.
     * @param clearOnReset defines whether or not this instance should
     *        be cleared from instance times lists on reset.
     * @param isBegin true if this object is part of the 
     *        timedElement's begin instance list.
     * @throws NullPointerException if time or timedElement is null
     */
    public TimeInstance(final TimedElementSupport timedElement,
                        final Time time,
                        final boolean clearOnReset,
                        final boolean isBegin) {
        if (timedElement == null || time == null) {
            throw new NullPointerException();
        }
        
        this.timedElement = timedElement;
        this.time = time;
        this.clearOnReset = clearOnReset;
        this.isBegin = isBegin;

        timedElement.addTimeInstance(this);
    }
    
    /**
     * Updates this instance time. This notifies the associated 
     * <code>TimedElementSupport</code> through its <code>instanceUpdate</code>
     * method.
     *
     * @param newTime the new instance time. Should not be null. 
     * @throws NullPointerException if newTime is null.
     */
    void setTime(final Time newTime) {
        if (newTime == null) {
            throw new NullPointerException();
        }

        time = newTime;
        timedElement.onTimeInstanceUpdate(this);
    }
}

