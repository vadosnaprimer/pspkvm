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

import org.w3c.dom.svg.SVGRect;


/**
 * This class represents an "SVGRect" datatype, consisting of a minimum X, 
 * minimum Y, width and height values.
 *
 * @author <a href="mailto:kevin.wong@sun.com">Kevin Wong</a>
 * @version $Id: Box.java,v 1.4 2006/04/21 06:35:00 st125089 Exp $
 */
public class Box implements SVGRect {
    /**
     * The smallest x-axis value of the rectangle
     */
    public float x;

    /**
     * The smallest y-axis value of the rectangle
     */
    public float y;

    /**
     * The width value of the rectangle
     */
    public float width;

    /**
     * The height value of the rectangle
     */
    public float height;

    /**
     * @param x the rect's x-axis origin.
     * @param y the rect's y-axis origin.
     * @param w the rect's x-axis width.
     * @param h the rect's y-axis height.
     */
    public Box(final float x,
               final float y,
               final float w,
               final float h) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
    }

    /**
     * Copy constructor
     *
     * @param b the Box to copy. Should not be null.
     */
    public Box(Box b) {
        this(b.x, b.y, b.width, b.height);
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
    public void setWidth(final float value) {
        width = value;
    }

    /**
     *
     */
    public void setHeight(final float value) {
        height = value;
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

    /**
     *
     */
    public float getWidth() {
        return width;
    }

    /**
     *
     */
    public float getHeight() {
        return height;
    }

    /**
     * Snaps this Box instance to the integer grid.
     */
    public void snap() {
        float fmaxX = x + width;
        float fmaxY = y + height;
        
        // x and y need to snap to the left of the integer grid.
        // We cannot simply cast as we need pixel accurate results.
        
        // Narrowing rounds towards zero
        int x = (int) this.x; 
        if (this.x < 0 && (this.x - x != 0)) {
            x -= 1;
        }
        
        int y = (int) this.y; // Narrowing
        if (this.y < 0 && (this.y - y) != 0) {
            y -= 1;
        }
        
        // maxX and maxY need to snap to the right of the integer grid.
        int maxX = (int) fmaxX;
        if (fmaxX > 0 && (fmaxX - maxX) != 0) {
            maxX += 1;
        }
        
        int maxY = (int) fmaxY;
        if (fmaxY > 0 && (fmaxY - maxY) != 0) {
            maxY += 1;
        }
        
        this.x = x;
        this.y = y;
        this.width = (maxX - x);
        this.height = (maxY - y);
    }

    /**
     * @return true if the input object is this Box or if x, y, width and height
     *         are equals.
     */
    public boolean equals(final Object cmp) {
        if (cmp == this) {
            return true;
        }

        if (cmp == null) {
            return false;
        }

        if (cmp instanceof Box) {
            Box b = (Box) cmp;
            return b.x == x
                &&
                b.y == y
                &&
                b.width == width
                &&
                b.height == height;
        }
        
        return false;
    }

    /**
     * Debugging helper.
     */
    public String toString() {
        return "Box(" + x + ", " + y + ", " + width + ", " + height + ")";
    }

}
