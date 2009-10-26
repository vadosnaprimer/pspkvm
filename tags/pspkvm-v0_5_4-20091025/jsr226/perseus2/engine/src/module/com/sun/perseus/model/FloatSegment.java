/*
 *  $RCSfile: FloatSegment.java,v $
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
 * Represents float segment in an animation. A float segment may have 
 * multiple components and multiple dimensions.
 *
 * For example, an rgb color segment is represented with one component
 * and 3 dimensions. A stroke dash array value is represented with
 * as many components as there are dashes and one dimension for each
 * component.
 *
 * @version $Id: FloatSegment.java,v 1.3 2006/06/29 10:47:31 ln156897 Exp $
 */
class FloatSegment implements Segment {
    /**
     * The segment's begin value.
     * There is an array for each component and an value in the
     * array for each dimension.
     */
    float[][] start;

    /**
     * The segment's end value.
     * @see #start
     */
    float[][] end;

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
     * Sets the start value to its notion of 'zero'.
     * For a FloatSegment, a 'zero' start means zero all all
     * dimensions for all components.
     */
    public void setZeroStart() {
        for (int ci = 0; ci < start.length; ci++) {
            for (int di = 0; di < start[ci].length; di++) {
                start[ci][di] = 0;
            }
        }
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
        FloatSegment mseg = (FloatSegment) seg;
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
        if (by == null || !(by instanceof float[][])) {
            throw new IllegalArgumentException();
        }

        float[][] add = (float[][]) by;
        if (add.length != end.length) {
            throw new IllegalArgumentException();
        }

        for (int ci = 0; ci < add.length; ci++) {
            float[] v = end[ci];
            float[] av = add[ci];
            int vl = v != null ? v.length : 0;
            int avl = av != null ? av.length : 0;
            if (vl != avl) {
                throw new IllegalArgumentException();
            }

            for (int di = 0; di < vl; di++) {
                v[di] += av[di];
            }
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
     * @return the length of the segment.
     */
    public float getLength() {
        float length = 0;
        final int nc = start.length;

        for (int ci = 0; ci < nc; ci++) {
            // Start value for the requested component.
            float[] s = start[ci];
            
            // End value for the requested component.
            float[] e = end[ci];
            
            // Number of dimensions.
            int nd = s.length;
            
            float clength = 0;
            for (int di = 0; di < nd; di++) {
                clength += (e[di] - s[di]) * (e[di] - s[di]);
            }
        
            length += MathSupport.sqrt(clength);
        }

        return length;
    }

    /**
     * Computes an interpolated value for the given penetration in the 
     * segment. Note that the start and end segment values must be set
     * <em>before</em> calling this method. Otherwise, a NullPointerException
     * is thrown.
     *
     * @param p the segment penetration. Should be in the [0, 1] range.
     * @param w array where the computed value should be stored.
     */
    public void compute(final float p, final float[][] w) {
        // For each component
        int nc = w.length;
        int nd = 0;
        for (int ci = 0; ci < nc; ci++) {
            // For each dimension 
            nd = w[ci].length;
            for (int di = 0; di < nd; di++) {
                w[ci][di] = p * end[ci][di] + (1 - p) * start[ci][di]; 
            }
        }
    }

    /**
     * Debug helper.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("FloatSegment[");
        if (start == null) {
            sb.append("null");
        } else {
            sb.append("start[" + start.length + "] : {");
            for (int ci = 0; ci < start.length; ci++) {
                float[] fc = start[ci];
                if (fc == null) {
                    sb.append("null");
                } else {
                    sb.append("float[" + fc.length + "{");
                    for (int di = 0; di < fc.length; di++) {
                        sb.append(fc[di]);
                        if (di < fc.length - 1) {
                            sb.append(", ");
                        }
                    }
                    sb.append("}");
                }
                if (ci < start.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append("} ");
        }

        if (end == null) {
            sb.append("null");
        } else {
            sb.append("end[" + end.length + "] : {");
            for (int ci = 0; ci < end.length; ci++) {
                float[] fc = end[ci];
                if (fc == null) {
                    sb.append("null");
                } else {
                    sb.append("float[" + fc.length + "{");
                    for (int di = 0; di < fc.length; di++) {
                        sb.append(fc[di]);
                        if (di < fc.length - 1) {
                            sb.append(", ");
                        }
                    }
                    sb.append("}");
                }
                if (ci < end.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append("}");
        }

        return sb.toString();
    }


    /**
     * Should be called after the segment's configuration is complete
     * to give the segment's implementation a chance to initialize 
     * internal data and cache values.
     */
    public void initialize() {
    }

}
