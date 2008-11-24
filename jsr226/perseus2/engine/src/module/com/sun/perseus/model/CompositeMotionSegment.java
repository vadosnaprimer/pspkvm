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
 * A composite implementation of the <code>Segment</code> interface.
 * This is used in AnimateMotion to handle linearization of 
 * quadratic and cubic segments.
 *
 * @version $Id: CompositeMotionSegment.java,v 1.3 2006/04/21 06:36:43 st125089 Exp $
 */
class CompositeMotionSegment implements MotionSegment {
    /**
     * The list of segment components.
     */
    MotionSegment[] segments;

    /**
     * The cached segment length.
     */
    float length;

    /**
     * The cached, normalized segment length
     */
    float[] nSegLength;

    /**
     * @return the start value.
     */
    public Object[] getStart() {
        return segments[0].getStart();
    }

    /**
     * @return set end value.
     */
    public Object[] getEnd() {
        return segments[segments.length - 1].getEnd();
    }

    /**
     * Computes an interpolated value for the given penetration in the 
     * segment.
     *
     * @param p the segment penetration. Should be in the [0, 1] range.
     * @param w array where the computed value should be stored.
     * @return the interpolated value.
     */
    public void compute(float p, final float[][] w) {
        // First, identify the child MotionSegment at the desired 
        // normalized distance.
        int si = segments.length - 1;
        float prevSegLength = 0;

        if (p < 1) {
            for (si = 0; si < segments.length; si++) {
                if (p < nSegLength[si]) {
                    break;
                }
                prevSegLength = nSegLength[si];
            }
        } else {
            if (si > 0) {
                prevSegLength = nSegLength[si - 1];
            }
        }

        // The sub-segment is at index si. Now, we need to 
        // compute the penetration in that segment.
        if (nSegLength[si] > prevSegLength) {
            p = (p - prevSegLength) / (nSegLength[si] - prevSegLength);
        } else {
            p = 1;
        }

        segments[si].compute(p, w);
    }

    /**
     * Computes this segment's length
     */
    public float getLength() {
        return length;
    }

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
    public void collapse(final Segment seg, final Animation anim) {
        CompositeMotionSegment cseg = (CompositeMotionSegment) seg;
        MotionSegment[] newSegments = 
            new MotionSegment[segments.length + cseg.segments.length];

        System.arraycopy(segments, 
                         0, 
                         newSegments, 
                         0, 
                         segments.length);

        System.arraycopy(cseg.segments, 
                         0, 
                         newSegments, 
                         segments.length, 
                         cseg.segments.length);

        segments = newSegments;
    }

    /**
     * Adds the input value to this Segment's end value.
     * 
     * @param by the value to add. Throws IllegalArgumentException if this
     * Segment type is not additive or if the input value is incompatible (e.g.,
     * different number of components or different number of dimensions on a
     * component).
     */
    public void addToEnd(Object[] by) {
        segments[segments.length - 1].addToEnd(by);
    }

    /**
     * @return true if this segment type supports addition. false
     * otherwise.
     */
    public boolean isAdditive() {
        return segments[0].isAdditive();
    }

    /**
     * Sets the start value to its notion of 'zero'
     */
    public void setZeroStart() {
        segments[0].setZeroStart();
    }

    /**
     * Sets the start value. 
     *
     * @param newStart the new segment start value.
     */
    public void setStart(Object[] newStart) {
        segments[0].setStart(newStart);
    }

    /**
     * Should be called after the segment's configuration is complete
     * to give the segment's implementation a chance to initialize 
     * internal data and cache values.
     */
    public void initialize() {
        // Initialize the component segments.
        final int ns = segments.length;
        for (int si = 0; si < ns; si++) {
            segments[si].initialize();
        }

        // Now, initialize the length cache.
        length = 0;
        nSegLength = new float[ns];        
        for (int si = 0; si < segments.length; si++) {
            nSegLength[si] = segments[si].getLength();
            length += nSegLength[si];
        }
        
        // Now, initialize the normalized segment lengths array
        if (length > 0) {
            float curLength = 0;
            for (int si = 0; si < ns - 1; si++) {
                curLength += nSegLength[si];
                nSegLength[si] = curLength / length;
            }
        } else {
            for (int si = 0; si < ns - 1; si++) {
                nSegLength[si] = 0;
            }
        }
        
        // Make sure that, in all cases, the last value is 1.
        nSegLength[ns - 1] = 1;
    }

}

