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
 * @version $Id: RefValues.java,v 1.2 2006/04/21 06:38:26 st125089 Exp $
 */
public interface RefValues {
    /**
     * @param i requested segment index.
     * @return Segment at index i
     */
    Segment getSegment(int i);

    /**
     * @return the number of segments in refValues
     */
    int getSegments();

    /**
     * @return the number of components in the refValues
     */
    int getComponents();

    /**
     * Computes the value for the input interpolated values.
     * There should be as many entries in the return array as there
     * are components in the RefValues.
     *
     * @param si the current segment index
     * @param p the current penetration
     */
    Object[] compute(int si, float p);

    /**
     * Adds a new time segment so accomodate for discreet behavior.
     * If there is only one segment for discreet animations, the
     * last value is never shown. To accomodate for that, this 
     * method should add a segment to the RefValues so that the 
     * last animation value is shown during the last value interval
     * of a discreet animation.
     */
    void makeDiscrete();

    /**
     * Computes the length of the RefValues. This is meant for paced timing 
     * computation.
     *
     * @return the total length of refValues.
     */
    float getLength();

    /**
     * Computes the length of segment at index si.
     *
     * @param si the segment index.
     * @param ci the component index.
     */
    float getLength(final int si);

    /**
     * Should be called after the RefValue's configuration is complete
     * to give the implementation a chance to initialize 
     * internal data and cache values.
     */
    void initialize();

}
