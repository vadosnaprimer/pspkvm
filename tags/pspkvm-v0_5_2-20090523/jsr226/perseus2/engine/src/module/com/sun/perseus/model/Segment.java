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
 * Represents a time interval in an animation.
 *
 * @version $Id: Segment.java,v 1.2 2006/04/21 06:38:37 st125089 Exp $
 */
interface Segment {
    /**
     * @return the start value.
     */
    Object[] getStart();

    /**
     * @return set end value.
     */
    Object[] getEnd();

    /**
     * Computes this segment's length
     */
    float getLength();

    /**
     * Collapses this segment with the one passed as a parameter.
     * Note that if the input segment is not of the same class
     * as this one, an IllegalArgumentException is thrown. The 
     * method also throws an exception if the input segment's
     * end does not have the same number of components as this 
     * segment's end.
     *
     * After this method is called, this segment's end value
     * is the one of the input <code>seg</code> parameter.
     *
     * @param seg the Segment to collapse with this one.
     * @param anim the Animation this segment is part of.
     */
    void collapse(Segment seg, Animation anim);

    /**
     * Adds the input value to this Segment's end value.
     * 
     * @param by the value to add. Throws IllegalArgumentException if this
     * Segment type is not additive or if the input value is incompatible (e.g.,
     * different number of components or different number of dimensions on a
     * component).
     */
    void addToEnd(Object[] by);

    /**
     * @return true if this segment type supports addition. false
     * otherwise.
     */
    boolean isAdditive();

    /**
     * Sets the start value to its notion of 'zero'
     */
    void setZeroStart();

    /**
     * Sets the start value. 
     *
     * @param newStart the new segment start value.
     */
    void setStart(Object[] newStart);

    /**
     * Should be called after the segment's configuration is complete
     * to give the segment's implementation a chance to initialize 
     * internal data and cache values.
     */
    void initialize();
}
