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
 * @version $Id: MotionRefValues.java,v 1.3 2006/06/29 10:47:33 ln156897 Exp $
 */
public class MotionRefValues implements RefValues {
    /**
     * This RefValues Segments. They can be CompositeSegment or MotionSegment
     * instances.
     */
    MotionSegment[] segments;

    /**
     * A working array to return a value from the compute method.
     */
    float[][] w;

    /**
     * Used to store the length of this RefValues
     */
    float length;

    /**
     * Used to store the normalized length of each segment
     */
    float[] nSegLength;

    /**
     * Used to store the length of each segment
     */
    float[] segLength;

    /**
     * The associated AnimateMotion
     */
    AnimateMotion motion;

    /**
     * Builds a new MotionRefValues associated with the given AnimateMotion 
     * instance.
     *
     * @param motion the associated AnimateMotion
     */
    public MotionRefValues(final AnimateMotion motion) {
        this.motion = motion;
    }

    /**
     * Computes the segment index for the given normalized distance.
     *
     * @param sisp a float array where the mapped si and sp values should be 
     *        stored.
     * @param dist the distance for which the segment indices should be
     *        computed. This value is normalized to the [0, 1] interval.
     */
    public void getSegmentAtDist(float[] sisp, float dist) {
        if (dist >= 1) {
            sisp[0] = segments.length - 1;
            sisp[1] = 1;
            return;
        }

        if (dist < 0) {
            sisp[0] = 0;
            sisp[1] = 0;
            return;
        }

        int i = 0;
        float prevSegLength = 0;
        for (; i < nSegLength.length - 1; i++) {
            if (dist < nSegLength[i]) {
                break;
            }
            prevSegLength = nSegLength[i];
        }
        
        // Now, compute the penetration in the segment.
        float p = 1;

        if (nSegLength[i] > prevSegLength) {
            p = 
                (dist - prevSegLength) 
                / 
                (nSegLength[i] - prevSegLength);
        }
        
        sisp[0] = i;
        sisp[1] = p;
    }

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
     * @return the number of components. There is an array of float for each 
     *         component.
     */
    public int getComponents() {
        return segments[0].getStart().length;
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
        MotionSegment[] tmpSegments = new MotionSegment[segments.length + 1];
        System.arraycopy(segments, 0, tmpSegments, 0, segments.length);
        Segment lastSeg = segments[segments.length - 1];
        float[][] end = (float[][]) lastSeg.getEnd();
        LeafMotionSegment newSeg = new LeafMotionSegment(end[4][0],
                                                         end[5][0],
                                                         end[4][0],
                                                         end[5][0],
                                                         motion);
        tmpSegments[tmpSegments.length - 1] = newSeg;
        segments = tmpSegments;
    }

    /**
     * Computes the length of the RefValues. This is meant for paced timing 
     * computation.
     *
     * @return the total length of refValues, along each component. Therefore, 
     *         there are as many entries in the returned array as there are
     *         components in RefValues.
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
        final int nc = segments[0].getStart().length;
        w = new float[nc][];
        for (int ci = 0; ci < nc; ci++) {
            // There is one dimension per component for motion
            // values, which are matrix values.
            w[ci] = new float[1];
        }

        // Initialize sub-segments.        
        final int ns = segments.length;
        for (int si = 0; si < ns; si++) {
            segments[si].initialize();
        }

        // Initialize cached length values.
        nSegLength = new float[ns];
        segLength = new float[ns];
        
        for (int si = 0; si < segments.length; si++) {
            segLength[si] = segments[si].getLength();
            length += segLength[si];
        }

        // Now, initialize the normalized segment lengths array
        if (length > 0) {
            float curLength = 0;
            for (int si = 0; si < ns - 1; si++) {
                curLength += segLength[si];
                nSegLength[si] = curLength / length;
            }
        } else {
            for (int si = 0; si < ns - 1; si++) {
                nSegLength[si] = 0;
            }
        }
        
        // Make sure that, in all cases, the last value is 1.
        nSegLength[nSegLength.length - 1] = 1;        
    }

    /**
     * Debug helper.
     */
    /*
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("MotionRefValues[" + getSegments() + "]\n");
        for (int si = 0; si < getSegments(); si++) {
            Segment seg = getSegment(si);
            sb.append("seg[" + si + "] : " + seg.toString() + "\n");
        }
        return sb.toString();
        } 
    */
}
