/*
 *  $RCSfile: StringSegment.java,v $
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
 * Represents String segment in an animation.
 *
 * @version $Id: StringSegment.java,v 1.3 2006/06/29 10:47:34 ln156897 Exp $
 */
class StringSegment implements Segment {
    /**
     * The segment's begin value.
     */
    String[] start;

    /**
     * The segment's end value.
     */
    String[] end;

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
     * For a StringSegment, a 'zero' start means empty strings
     * on all components.
     */
    public void setZeroStart() {
        for (int i = 0; i < start.length; i++) {
            start[i] = "";
        }
    }

    /**
     * Sets the start value. 
     *
     * @param newStart the new segment start value.
     */
    public void setStart(Object[] newStart) {
        start = (String[]) newStart;
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
        StringSegment mseg = (StringSegment) seg;
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
        throw new IllegalArgumentException();
    }

    /**
     * @return true if this segment type supports addition. false
     * otherwise.
     */
    public boolean isAdditive() {
        return false;
    }

    /**
     * Computes an interpolated value for the given penetration in the 
     * segment.
     *
     * @param p the segment penetration. Should be in the [0, 1] range.
     * @return the interpolated value.
     */
    public String[] compute(final float p) {
        if (p == 1) {
            return end;
        } else {
            return start;
        }
    }

    /**
     * Debug helper.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("StringSegment[");
        if (start == null) {
            sb.append("null");
        } else {
            sb.append("start[" + start.length + "] : {");
            for (int ci = 0; ci < start.length; ci++) {
                sb.append("\"" + start[ci] + "\"");
                if (ci < start.length - 1) {
                    sb.append(",");
                }
            }
            sb.append("}");
        }

        if (end == null) {
            sb.append(" null");
        } else {
            sb.append(" end[" + end.length + "] : {");
            for (int ci = 0; ci < end.length; ci++) {
                sb.append("\"" + end[ci] + "\"");
                if (ci < end.length - 1) {
                    sb.append(",");
                }
            }
            sb.append("}");
        }


        return sb.toString();
    }

    /**
     * Computes this segment's length. This is always the value
     * '1' for a string segment.
     */
    public final float getLength() {
        return 1;
    }


    /**
     * Should be called after the segment's configuration is complete
     * to give the segment's implementation a chance to initialize 
     * internal data and cache values.
     */
    public final void initialize() {
    }

}
