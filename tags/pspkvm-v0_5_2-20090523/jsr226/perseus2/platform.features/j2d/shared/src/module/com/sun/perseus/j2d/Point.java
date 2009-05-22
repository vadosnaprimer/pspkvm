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
package com.sun.perseus.j2d;

import org.w3c.dom.svg.SVGPoint;

/**
 * This class represents an <code>SVGPoint</code> datatype, identified by 
 * its x and y components.
 *
 * @author <a href="mailto:kevin.wong@sun.com">Kevin Wong</a>
 * @version $Id: Point.java,v 1.2 2006/04/21 06:35:14 st125089 Exp $
 */
public class Point implements SVGPoint {

    /**
     * The x-axis coordinate.
     */
    private float x;

    /**
     * The y-axis coordinate.
     */
    private float y;

    /**
     * Constructs an <code>SVGPoint</code> with given X and Y
     * coordinates.
     *
     * @param x the x-axis coordinate.
     * @param y the y-axis coordinate.
     */
    public Point(final float x, final float y) {
        this.x = x;
        this.y = y;
    }

    /**
     *
     */
    public void setX(final float value) {
        x = value;
    }

    /**
     *
     */
    public void setY(final float value) {
        y = value;
    }

    /**
     *
     */
    public float getX() {
        return x;
    }

    /**
     *
     */
    public float getY() {
        return y;
    }
}
