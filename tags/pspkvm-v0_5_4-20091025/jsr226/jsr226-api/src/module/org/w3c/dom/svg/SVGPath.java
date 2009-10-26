/*
 * Portions Copyright  2000-2007 Sun Microsystems, Inc. All Rights
 * Reserved.  Use is subject to license terms.
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

package org.w3c.dom.svg;

import org.w3c.dom.DOMException;

/**
 *
 */
public interface SVGPath
{

    /**
     *
     */
    public static final short MOVE_TO = 77;

    /**
     *
     */
    public static final short LINE_TO = 76;

    /**
     *
     */
    public static final short CURVE_TO = 67;

    /**
     *
     */
    public static final short QUAD_TO = 81;

    /**
     *
     */
    public static final short CLOSE = 90;

    /**
     *
     */
    public int getNumberOfSegments();

    /**
     *
     */
    public short getSegment(int cmdIndex)
        throws DOMException;

    /**
     *
     */
    public float getSegmentParam(int cmdIndex, int paramIndex)
        throws DOMException;

    /**
     *
     */
    public void moveTo(float x, float y);

    /**
     *
     */
    public void lineTo(float x, float y);

    /**
     *
     */
    public void quadTo(float x1, float y1, float x2, float y2);

    /**
     *
     */
    public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3);

    /**
     *
     */
    public void close();

}
