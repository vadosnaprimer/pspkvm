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
 * An <code>OffsetCondition</code> generates a single <code>TimeInstance</code>
 * on initialization.
 *
 * @version $Id: OffsetCondition.java,v 1.2 2006/04/21 06:38:06 st125089 Exp $
 */
public final class OffsetCondition extends TimeCondition {
    /**
     * Offset from the time container's begin time.
     */
    long offset;

    /**
     * @param timedElement the associated <code>TimedElementSupport</code>. 
     *        Should not be null.
     * @param isBegin defines whether this condition is for a begin list.
     * @param offset offset compared to the time container's begin time.
     */
    public OffsetCondition(final TimedElementSupport timedElement,
                           final boolean isBegin,
                           final long offset) {
        super(timedElement, isBegin);
        this.offset = offset;

        //
        // Insert a new TimeInstance in the associated TimedElementSupport's
        // begin or end list.
        //
        new TimeInstance(timedElement,
                         new Time(offset),
                         false, // no clear on reset
                         isBegin);
    }

    /**
     * Converts this <code>OffsetCondition</code> to a String trait.
     *
     * @return a string describing this <code>TimeCondition</code>
     */
    protected String toStringTrait() {
        return (offset / 1000f) + "s";
    }

}
