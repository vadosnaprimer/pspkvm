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
 * @version $Id: StringRefValues.java,v 1.2 2006/04/21 06:38:51 st125089 Exp $
 */
public class StringRefValues implements RefValues {
    /**
     * The RefValues StringSegments
     */
    StringSegment[] segments;

    /**
     * Used to store the length of this RefValues
     */
    float[] length;

    /**
     * @param i requested segment index.
     * @return Segment at index i
     */
    public Segment getSegment(int i) {
        return segments[i];
    }

    /**
     * @return the number of segments in refValues
     */
    public int getSegments() {
        return segments.length;
    }

    /**
     * StringRefValues only have one component.
     *
     * @return the number of string components
     */
    public int getComponents() {
        return segments[0].start.length;
    }

    /**
     * Computes the value for the input interpolated values.
     * There should be as many entries in the return array as there
     * are components in the RefValues.
     *
     * @param si the current segment index
     * @param p the current penetration
     * @param the interpolated value.
     */
    public Object[] compute(int si, float p) {
        return segments[si].compute(p);
    }

    /**
     * Adds a new time segment so accomodate for discreet behavior.
     * If there is only one segment for discreet animations, the
     * last value is never shown. To accomodate for that, this 
     * method should add a segment to the RefValues so that the 
     * last animation value is shown during the last value interval
     * of a discreet animation.
     */
    public void makeDiscrete() {
        StringSegment[] tmpSegments = new StringSegment[segments.length + 1];
        System.arraycopy(segments, 0, tmpSegments, 0, segments.length);
        StringSegment lastSeg = segments[segments.length - 1];
        StringSegment newSeg = new StringSegment();
        newSeg.start = lastSeg.end;
        newSeg.end = lastSeg.end;
        tmpSegments[tmpSegments.length - 1] = newSeg;
        segments = tmpSegments;        
    }

    /**
     * Computes the length of the RefValues. This is meant for paced timing 
     * computation.
     *
     * @return the length between the various ref values. For strings, we 
     *         consider that each segment is of length 1, so this simply 
     *         returns the number of segments.
     */
    public float getLength() {
        return segments.length;
    }

    /**
     * Computes the length of segment at index si.
     *
     * @param si the segment index.
     */
    public float getLength(final int si) {
        // The length of a StringSegment is _always_ 1.
        return 1;
    }

    /**
     * Debug helper.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("StringRefValues[" + getSegments() + "]\n");
        for (int si = 0; si < getSegments(); si++) {
            Segment seg = getSegment(si);
            sb.append("seg[" + si + "] : " + seg.toString() + "\n");
        }
        return sb.toString();
    }

    /**
     * Should be called after the RefValue's configuration is complete
     * to give the implementation a chance to initialize 
     * internal data and cache values.
     */
    public void initialize() {
        // Initialize segments.
        final int ns = segments.length;

        // Initialize length cache
        length = new float[segments[0].start.length];
        
        // The length of a StringSegment is 1, in all cases.
        for (int ci = 0; ci < length.length; ci++) {
            length[ci] = segments.length;
        }
    }

}
