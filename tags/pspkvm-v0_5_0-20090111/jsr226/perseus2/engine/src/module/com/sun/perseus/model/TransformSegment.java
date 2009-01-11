/*
 *  $RCSfile: TransformSegment.java,v $
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
 * Represents a segment in an animateTransform. 
 *
 *
 * @version $Id: TransformSegment.java,v 1.3 2006/06/29 10:47:36 ln156897 Exp $
 */
public class TransformSegment implements Segment {
    /**
     * Holds the minimal information for the segment's start.
     * The interpretation depends on the segment type.
     * TYPE_TRANSLATE: start[0] = tx, start[1] = ty
     * TYPE_SCALE: start[0] = sx, start[1] = sy
     * TYPE_ROTATE: start[0] = rotate, start[1] = cx, start[2] = cy;
     * TYPE_SKEW_X: start[0] = skewX
     * TYPE_SKEW_Y: start[0] = skewY
     */
    float[] start;

    /**
     * Holds the minimal information for the segment's end.
     * @see #start
     */
    float[] end;

    /**
     * The segment type.
     */
    int type;

    /**
     * @return the start value.
     */
    public Object[] getStart() {
        float[][] v = new float[6][1];
        compute(0, v);
        return v;
    }

    /**
     * @return set end value.
     */
    public Object[] getEnd() {
        float[][] v = new float[6][1];
        compute(1, v);
        return v;
    }

    /**
     * Sets the start value to its notion of 'zero'.
     * For a FloatSegment, a 'zero' start means zero all all
     * dimensions for all components.
     */
    public void setZeroStart() {
        switch (type) {
        case AnimateTransform.TYPE_TRANSLATE:
            start[0] = 0;
            start[1] = 0;
            break;
        case AnimateTransform.TYPE_SCALE:
            start[0] = 1;
            start[1] = 1;
            break;
        case AnimateTransform.TYPE_ROTATE:
            start[0] = 0;
            break;
        case AnimateTransform.TYPE_SKEW_X:
        case AnimateTransform.TYPE_SKEW_Y:
        default:
            start[0] = 0;
            break;
        }
    }

    /**
     * Sets the start value. 
     *
     * @param newStart the new segment start value.
     */
    public void setStart(Object[] newStart) {
        float[][] ns = (float[][]) newStart;
        switch (type) {
        case AnimateTransform.TYPE_TRANSLATE:
            start[0] = ns[4][0];
            start[1] = ns[5][0];
            break;
        case AnimateTransform.TYPE_SCALE:
            start[0] = ns[0][0];
            start[1] = ns[3][0];
            break;
        case AnimateTransform.TYPE_ROTATE:
            // This assumes that the input matrix is a simple rotate.
            // We are not trying to extract the rotation from a complex
            // matrix.
            start[0] = MathSupport.atan2(ns[1][0], ns[0][0]);
            break;
        case AnimateTransform.TYPE_SKEW_X:
            start[0] = MathSupport.atan(ns[2][0]);
            break;
        case AnimateTransform.TYPE_SKEW_Y:
        default:
            start[0] = MathSupport.atan(ns[1][0]);
            break;
        }
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
        TransformSegment mseg = (TransformSegment) seg;
        if (mseg.end.length != end.length) {
            throw new IllegalArgumentException();
        }

        end = mseg.end;
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
        float[][] ns = (float[][]) by;
        switch (type) {
        case AnimateTransform.TYPE_TRANSLATE:
            end[0] += ns[4][0];
            end[1] += ns[5][0];
            break;
        case AnimateTransform.TYPE_SCALE:
            end[0] += ns[0][0];
            end[1] += ns[3][0];
            break;
        case AnimateTransform.TYPE_ROTATE:
            // Here, we need to add a value that has the form
            // [1 0 x] [cos -sin 0] [1 0 -x]   [cos -sin x-x.cos+y.sin]
            // [0 1 y] [sin  cos 0] [0 1 -y] = [sin  cos y-x.sin-y.cos]
            // [0 0 1] [0    0   1] [0 0  1]   [0    0   1            ]
            //
            // We have:
            // x.(1 - cos) + y.sin = a
            // y.(1 - cos) - x.sin = b
            //
            // So:
            // I  : x.(1 - cos).sin + y.sin.sin = a.sin
            // II : y.(1 - cos).(1 - cos) - x.sin.(1 - cos) = b.(1 - cos)
            //
            // Doing I + II yields:
            //
            // y.sin.sin + y.(1 - cos).(1 - cos) = a.sin + b.(1 - cos)
            //
            // y.(sin.sin + 1 - 2.cos + cos.cos) = a.sin + b.(1 - cos)
            //
            // y.2.(1 - cos) = a.sin + b.(1 - cos)
            //

            // First, extract the rotation angle
            float cos = ns[0][0];
            float sin = ns[1][0];

            end[0] += MathSupport.atan2(ns[1][0], ns[0][0]);
            
            // Now extract the translation components.
            float a = ns[4][0];
            float b = ns[5][0];

            if (a != 0 && b != 0) {
                // There is a translation component
                float div = 2 * (1 - cos);
                if (div != 0) {
                    float y = (a * sin + b * (1 - cos)) / div;
                    float x = (a - y * sin) / (1 - cos);
                    end[1] += x;
                    end[2] += y;
                } 
                // else div = 0, there is no rotation, so we do not further
                // process the translation component.
            }
            break;
        case AnimateTransform.TYPE_SKEW_X:
            end[0] += MathSupport.atan(ns[2][0]);
            break;
        case AnimateTransform.TYPE_SKEW_Y:
        default:
            end[0] += MathSupport.atan(ns[1][0]);
            break;
        }
    }

    /**
     * @return true if this segment type supports addition. false
     * otherwise.
     */
    public boolean isAdditive() {
        return true;
    }

    /**
     * @return the length of the segment
     */
    public float getLength() {
        float length = 0;
        switch (type) {
        case AnimateTransform.TYPE_TRANSLATE:
            if (end[0] > start[0]) {
                    length += end[0] - start[0];
            } else {
                length += start[0] - end[0];
            }

            if (end[1] > start[1]) {
                length += end[1] - start[1];
            } else {
                length += start[1] - end[1];
            }
            return length;
            
        case AnimateTransform.TYPE_SCALE:
            if (end[0] > start[0]) {
                length += end[0] - start[0];
            } else {
                length += start[0] - end[0];
            } 

            if (end[1] > start[1]) {
                length += end[1] - start[1];
            } else {
                length += start[1] - end[1];
            } 
            
            return length;

        case AnimateTransform.TYPE_ROTATE:
            if (end[0] > start[0]) {
                length += end[0] - start[0];
            } else {
                length += start[0] - end[0];
            } 

            if (end[1] > start[1]) {
                length += end[1] - start[1];
            } else {
                length += start[1] - end[1];
            }

            if (end[2] > start[2]) {
                length += end[2] - start[2];
            } else {
                length += start[2] - end[2];
            }

            return length;
        case AnimateTransform.TYPE_SKEW_X:
            if (end[0] > start[0]) {
                length += end[0] - start[0];
            } else {
                length += start[0] - end[0];
            } 

            return length;
        case AnimateTransform.TYPE_SKEW_Y:
        default:
            if (end[0] > start[0]) {
                length += end[0] - start[0];
            } else {
                length += start[0] - end[0];
            } 
            
            return length;
        }
    }

    /**
     * Computes an interpolated value for the given penetration in the 
     * segment. Note that the start and end segment values must be set
     * <em>before</em> calling this method. Otherwise, a NullPointerException
     * is thrown.
     *
     * @param p the segment penetration. Should be in the [0, 1] range.
     * @param w array where the computed value should be stored.
     * @return the interpolated value.
     */
    public Object[] compute(final float p, final float[][] w) {
        switch (type) {
        case AnimateTransform.TYPE_TRANSLATE:
            w[0][0] = 1;
            w[1][0] = 0;
            w[2][0] = 0;
            w[3][0] = 1;
            w[4][0] = (1 - p) * start[0] + p * end[0];
            w[5][0] = (1 - p) * start[1] + p * end[1];
            break;

        case AnimateTransform.TYPE_SCALE:
            w[0][0] = (1 - p) * start[0] + p * end[0];
            w[1][0] = 0;
            w[2][0] = 0;
            w[3][0] = (1 - p) * start[1] + p * end[1];
            w[4][0] = 0;
            w[5][0] = 0;
            break;

        case AnimateTransform.TYPE_ROTATE:
            {
                float x = (1 - p) * start[1] + p * end[1];
                float y = (1 - p) * start[2] + p * end[2];
                float theta = (1 - p) * start[0] + p * end[0];
                float sin = MathSupport.sin(theta);
                float cos = MathSupport.cos(theta);
                
                w[0][0] = cos;
                w[1][0] = sin;
                w[2][0] = -sin;
                w[3][0] = cos;
                w[4][0] = (x - x * cos + y * sin);
                w[5][0] = (y - x * sin - y * cos);
            }
            break;
        case AnimateTransform.TYPE_SKEW_X:
            {
                float theta = (1 - p) * start[0] + p * end[0];
                w[0][0] = 1;
                w[1][0] = 0;
                w[2][0] = MathSupport.tan(theta);
                w[3][0] = 1;
                w[4][0] = 0;
                w[5][0] = 0;
            }
            break;
            
        case AnimateTransform.TYPE_SKEW_Y:
        default:
            {
                float theta = (1 - p) * start[0] + p * end[0];
                w[0][0] = 1;
                w[1][0] = MathSupport.tan(theta);
                w[2][0] = 0;
                w[3][0] = 1;
                w[4][0] = 0;
                w[5][0] = 0;
            }
            break;
        }

        return w;
    }
    /**
     * Should be called after the segment's configuration is complete
     * to give the segment's implementation a chance to initialize 
     * internal data and cache values.
     */
    public void initialize() {
    }

}
