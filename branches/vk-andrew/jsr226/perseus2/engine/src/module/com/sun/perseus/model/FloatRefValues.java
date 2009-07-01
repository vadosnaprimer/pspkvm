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
 * @version $Id: FloatRefValues.java,v 1.3 2006/04/21 06:37:00 st125089 Exp $
 */
public class FloatRefValues implements RefValues {
    /**
     * This RefValues FloatSegments.
     */
    FloatSegment[] segments;

    /**
     * A working array to return a value from the compute method.
     */
    float[][] w;

    /**
     * Used to store the length of this RefValues
     */
    float length;

    /**
     * Used to store the length of each segment.
     */
    float[] segLength;

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
     * FloatRefValues only have one component. This returns the number of
     * components in the start value of the first segment.
     *
     * @return the number of components. There is an array of float for each 
     *         component.
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
     */
    public Object[] compute(int si, float p) {
        segments[si].compute(p, w);
        return w;
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
        FloatSegment[] tmpSegments = new FloatSegment[segments.length + 1];
        System.arraycopy(segments, 0, tmpSegments, 0, segments.length);
        FloatSegment lastSeg = segments[segments.length - 1];
        FloatSegment newSeg = new FloatSegment();
        newSeg.start = lastSeg.end;
        newSeg.end = lastSeg.end;
        tmpSegments[tmpSegments.length - 1] = newSeg;
        segments = tmpSegments;
    }

    /**
     * Computes the length of the RefValues. This is meant for paced timing 
     * computation.
     *
     * @return the total length of this FloatRefValues. The distance is defined
     *         as the average distance between 
     *         r
     */
    public float getLength() {
        return length;
    }

    /**
     * Computes the length of segment at index si
     *
     * @param si the segment index.
     */
    public float getLength(final int si) {
        return segLength[si];
    }

    /**
     * Should be called after the RefValue's configuration is complete
     * to give the implementation a chance to initialize 
     * internal data and cache values.
     */
    public void initialize() {
        // Initialize the working buffer
        final int nc = segments[0].start.length;

        w = new float[nc][];

        final int ns = segments.length;

        // Initialize the segments.
        for (int si = 0; si < ns; si++) {
            segments[si].initialize();
        }

        segLength = new float[ns];

        // The length of a FloatSegment, is the average distance between each
        // component.
        for (int ci = 0; ci < nc; ci++) {
            w[ci] = new float[segments[0].start[ci].length];
        }

        length = 0;
        for (int si = 0; si < ns; si++) {
            segLength[si] = segments[si].getLength();
            length += segLength[si];
        }
    }
    
    /**
     * Debug helper.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("FloatRefValues[" + getSegments() + "]\n");
        for (int si = 0; si < getSegments(); si++) {
            Segment seg = getSegment(si);
            sb.append("seg[" + si + "] : " + seg.toString() + "\n");
        }
        return sb.toString();
    }
}
