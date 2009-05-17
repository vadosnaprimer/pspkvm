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

import java.util.Vector;

/**
 * A <code>TimeCondition</code> represents a value in a 
 * <code>TimedElementSupport</code> begin or end conditions list.
 *
 * @version $Id: TimeCondition.java,v 1.2 2006/04/21 06:39:19 st125089 Exp $
 */
public abstract class TimeCondition {
    /**
     * Controls whether this condition is part of a begin or end list
     */
    boolean isBegin;

    /**
     * The associated <code>TimedElement</code>
     */
    TimedElementSupport timedElement;

    /**
     * As a result of constructing a new <code>TimeCondition</code>, the
     * condition is added to the associated <code>TimedElementSupport</code>'s
     * begin or end list of conditions.
     *
     * @param timedElement the associated <code>TimedElementSupport</code>. 
     *        Should not be null.
     * @param isBegin defines whether this condition is for a begin list.
     */
    public TimeCondition(final TimedElementSupport timedElement,
                         final boolean isBegin) {
        if (timedElement == null) {
            throw new NullPointerException();
        }

        this.timedElement = timedElement;
        this.isBegin = isBegin;

        timedElement.addTimeCondition(this);
    }

    /**
     * Converts a vector of TimeCondition instances to a string trait value.
     *
     * @param v the TimeCondition vector to convert. Should not be null.
     *        This should be called with a TimedElementSupport begin or
     *        end condition vector, which should <b>never</b> be null.
     * @return the string trait value.
     */
    protected static String toStringTrait(final Vector v) {
        final int n = v.size();
        StringBuffer sb = new StringBuffer();
        
        for (int i = 0; i < n - 1; i++) {
            TimeCondition tc = (TimeCondition) v.elementAt(i);
            sb.append(tc.toStringTrait());
            sb.append(SVGConstants.SEMI_COLON);
        }

        sb.append(((TimeCondition) v.elementAt(n - 1)).toStringTrait());
        return sb.toString();
    }

    /**
     * Converts this <code>TimeCondition</code> to a String trait.
     *
     * @return a string describing this <code>TimeCondition</code>
     */
    protected abstract String toStringTrait();
}


