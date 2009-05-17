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

import com.sun.perseus.util.SVGConstants;

/**
 * This simple class represents the concept of time in the 
 * context of SMIL timing. 
 * 
 * <p>A <code>Time</code> can be <code>RESOLVED</code>, 
 * <code>UNRESOLVED</code> or <code>INDEFINITE</code>. When
 * times are sorted, times are compared according to their type
 * and value. The predefined <code>UNRESOLVED</code> and 
 * <code>INDEFINITE</code> values are used to characterize
 * the special values and sorting is done with the 
 * <code>greaterThan</code> method.
 * 
 * @see <a href="http://www.w3.org/TR/smil20/smil-timing.html">
 *      SMIL 2.0 Timing and Synchronization Module"</a>
 * @version $Id: Time.java,v 1.2 2006/04/21 06:39:16 st125089 Exp $
 */
public final class Time {
    /**
     * Un-mutable Time instance used to represent the 'indefinite' 
     * time.
     */
    public static final Time INDEFINITE = new Time(-1);

    /**
     * Un-mutable Time instance used to represent the 'unresolved'
     * time.
     */
    public static final Time UNRESOLVED = new Time(-2);

    /**
     * This Time's value.
     */
    long value;

    /**
     * Creates a new <code>RESOLVED</code> time with the given value.
     *
     * @param value the new time's value.
     */
    public Time(final long value) {
        this.value = value;
    }

    /**
     * Compares the input time with this instance.
     * The <code>UNRESOLVED</code> value is greater than any
     * other <code>Time</code> value. The <code>INDEFINITE</code>
     * value is greater than any resolved value.
     * A resolved time is greater than another resolved time 
     * if its <code>value</code> is greater.
     *
     * @param cmp the time to compare with this instance.
     * @return true if this <code>Time</code> instance is 
     *         greater than cmp.
     * @throws NullPointerException if cmp is null.
     */
    public boolean greaterThan(final Time cmp) {
        if (this == UNRESOLVED) {
            return true;
        } else if (this == INDEFINITE) {
            if (cmp == UNRESOLVED) {
                return false;
            } 
            return true;
        } else {
            if (cmp == UNRESOLVED 
                ||
                cmp == INDEFINITE) {
                return false;
            }
            return value >= cmp.value;
        }
    }

    /**
     * @return true if this time is resolved, i.e., if it is neither
     *         equal to UNRESOLVED nor equal to INDEFINITE
     */
    public boolean isResolved() {
        return this != INDEFINITE && this != UNRESOLVED;
    }

    /**
     * Checks if the input time is the same as this one.
     *
     * @param cmp the object to compare to.
     * @return true if cmp is the same object as this one or if cmp
     *         has the same value as tis object.
     */
    public boolean isSameTime(final Time cmp) {
        if (cmp == null) {
            return false;
        }

        if (cmp == this) {
            return true;
        }

        if (cmp.value == value) {
            return true;
        }

        return false;
    }

    /**
     * Debug
     * @return a string describing this TimeInterval
     */
    public String toString() {
        if (this == UNRESOLVED) {
            return "Time[UNRESOLVED]";
        } else if (this == INDEFINITE) {
            return "Time[INDEFINITE]";
        } else {
            return "Time[RESOLVED, " + value + "]";
        }
    }

    /**
     * Converst a Time instance to a String trait value.
     *
     * @param t the time instance to convert. If null, the value 
     *        'indefinite' is returned.
     * @return a string trait value. 
     */
    protected static String toStringTrait(final Time t) {
        if (t == null || Time.INDEFINITE == t) {
            return SVGConstants.SVG_INDEFINITE_VALUE;
        }

        // This should never happen because Times converted to traits
        // are times specified on timed element attributes and should
        // never be unresolved times.
        if (Time.UNRESOLVED.isSameTime(t)) {
            throw new IllegalArgumentException();
        }

        // At this point, we know we are dealing with a resolved time.
        // Returns the value in seconds.
        return (t.value / 1000f) + "s";
    }


}
