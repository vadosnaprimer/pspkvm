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

import com.sun.perseus.platform.MathSupport;

/**
 * Represents a time interval in an animation.
 *
 * @version $Id: LeafMotionSegment.java,v 1.3 2006/06/29 10:47:32 ln156897 Exp $
 */
final class LeafMotionSegment implements MotionSegment {
    /**
     * The segment's begin value.
     */
    float[][] start;

    /**
     * The segment's end value.
     */
    float[][] end;

    /**
     * Builds a new motion segment for the given previous segment and 
     * end value.
     *
     * @param prevSegment the preceding segment. Should not be null.
     * @param x2 the motion's end point, on the x-axis
     * @param y2 the motion's end point, on the y-axis
     * @param motion the corresponding AnimateMotion
     */
    public LeafMotionSegment(final LeafMotionSegment prevSegment,
                             final float x2,
                             final float y2,
                             final AnimateMotion motion) {
        start = prevSegment.end;
        end = new float[6][1];
        end[4][0] = x2;
        end[5][0] = y2;

        computeRotate(motion);
    }

    /**
     * Builds a new motion segment for the given start and end 
     * points.
     *
     * @param x1 the motion's starting point on the x-axis
     * @param y1 the motion's stating point, on the y-axis
     * @param x2 the motion's end point, on the x-axis
     * @param y2 the motion's end point, on the y-axis
     * @param motion the corresponding AnimateMotion
     */
    public LeafMotionSegment(final float x1, 
                             final float y1,
                             final float x2,
                             final float y2,
                             final AnimateMotion motion) {
        start = new float[6][1];
        end = new float[6][1];

        start[0][0] = 1;
        start[3][0] = 1;
        start[4][0] = x1;
        start[5][0] = y1;

        end[4][0] = x2;
        end[5][0] = y2;

        computeRotate(motion);
    }

    /**
     * Computes the rotation component on the end value.
     *
     * @param motion the associated AnimateMotion.
     */
    void computeRotate(final AnimateMotion motion) {
        float cosRotate = motion.cosRotate;
        float sinRotate = motion.sinRotate;

        if (motion.rotateType != AnimateMotion.ROTATE_ANGLE) {
            float dx = end[4][0] - start[4][0];
            float dy = end[5][0] - start[5][0];
            float theta = MathSupport.atan2(dy, dx);
            if (motion.rotateType == AnimateMotion.ROTATE_AUTO_REVERSE) {
                theta += MathSupport.PI;
            }

            cosRotate = MathSupport.cos(theta);
            sinRotate = MathSupport.sin(theta);
        } 

        end[0][0] = cosRotate;
        end[1][0] = sinRotate;
        end[2][0] = -sinRotate;
        end[3][0] = cosRotate;
    }

    /**
     * @return the start value.
     */
    public Object[] getStart() {
        return start;
    }

    /**
     * @return set end value.
     */
    public Object[] getEnd() {
        return end;
    }

    /**
     * Computes an interpolated value for the given penetration in the 
     * segment.
     *
     * @param p the segment penetration. Should be in the [0, 1] range.
     * @param w array where the computed value should be stored.
     */
    public void compute(final float p, final float[][] w) {
        // The rotation component is kept in the end value. 
        // The translation is an interpolation of the begin and 
        // end values.
        w[0][0] = end[0][0];
        w[1][0] = end[1][0];
        w[2][0] = end[2][0];
        w[3][0] = end[3][0];
        w[4][0] = p * end[4][0] + (1 - p) * start[4][0];
        w[5][0] = p * end[5][0] + (1 - p) * start[5][0];
    }

    /**
     * Computes the 'motion' length for this segment
     */
    public float getLength() {
        float dx = end[4][0] - start[4][0];
        float dy = end[5][0] - start[5][0];
        
        return MathSupport.sqrt(dx * dx + dy * dy);
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
        LeafMotionSegment mseg = (LeafMotionSegment) seg;
        end = mseg.end;
        computeRotate((AnimateMotion) anim);
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
        float[][] add = (float[][]) by;

        // Adding motion segments is equivalent to concatenating transforms.
        // So the result of adding the input value to the end is equivalent
        // to concatenating the by value matrix to the end value matrix.
        float[][] tmpEnd = new float[6][1];
        tmpEnd[0][0] = end[0][0] * add[0][0] + end[2][0] * add[1][0];
        tmpEnd[1][0] = end[1][0] * add[0][0] + end[3][0] * add[1][0];

        tmpEnd[2][0] = end[0][0] * add[2][0] + end[2][0] * add[3][0];
        tmpEnd[3][0] = end[1][0] * add[2][0] + end[3][0] * add[3][0];

        tmpEnd[4][0] = end[0][0] * add[4][0] + end[2][0] * add[5][0] 
                        + end[4][0];
        tmpEnd[5][0] = end[1][0] * add[4][0] + end[3][0] * add[5][0] 
                        + end[5][0];

        end = tmpEnd;
    }

    /**
     * @return true if this segment type supports addition. false
     * otherwise.
     */
    public boolean isAdditive() {
        return true;
    }

    /**
     * Sets the start value to its notion of 'zero'
     */
    public void setZeroStart() {
        start[4][0] = 0;
        start[5][0] = 0;
    }

    /**
     * Sets the start value. 
     *
     * @param newStart the new segment start value.
     */
    public void setStart(Object[] newStart) {
        start = (float[][]) newStart;
    }

    /**
     * Should be called after the segment's configuration is complete
     * to give the segment's implementation a chance to initialize 
     * internal data and cache values.
     */
    public void initialize() {
    }
}
