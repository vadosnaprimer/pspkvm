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
 * This class is used to represent an area of the viewport.  It supports both
 * clipping areas, in viewport coordinates (which is why it uses integers), and
 * dirty area management.
 *
 * @author <a href="mailto:kevin.wong@sun.com">Kevin Wong</a>
 * @version $Id: Tile.java,v 1.6 2006/04/21 06:35:19 st125089 Exp $
 */
public class Tile {
    /**
     * Start and end x tile coordinates.
     */
    public int x, maxX;
    
    /**
     * Start and end y tile coordinates.
     */
    public int y, maxY;

    /**
     * Default constructor.
     */
    public Tile() {
    }

    /**
     * Copy constructor
     *
     * @param t the Tile to copy. Should not be null.
     */
    public Tile(Tile t) {
        this();
        setTile(t);
    }

    /**
     * Constructor with the Tile's coordinates.
     *
     * @param x the tile's origin along the x-axis
     * @param y the tile's origin along the y-axis
     * @param width the tile's width
     * @param height the tile's height
     */
    public Tile(int x, int y, int width, int height) {
        setTile(x, y, width, height);
    }
 
    /**
     * Sets the tile's dimension and origin.
     *
     * @param x the tile's origin along the x-axis
     * @param y the tile's origin along the y-axis
     * @param width the tile's width
     * @param height the tile's height
     */
    public void setTile(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.maxX = x + width - 1;
        this.maxY = y + height - 1;
    }

    /**
     * Sets this tile to the same x/y/maxX/maxY as t.
     *
     * @param t the Tile to copy.
     */
    public void setTile(final Tile t) {
        this.x = t.x;
        this.y = t.y;
        this.maxX = t.maxX;
        this.maxY = t.maxY;
    }

    /**
     * Sets this tile to be empty. Actually, this sets the tile to a one pixel tile
     * in the top left corner of the integer coordinate grid (i.e, in Integer.MIN_VALUE,
     * Integer.MIN_VALUE). This tile will never intersect with any other tile.
     */
    public void setEmptyTile() {
        this.x = Integer.MIN_VALUE;
        this.y = Integer.MIN_VALUE;
        this.maxX = Integer.MIN_VALUE;
        this.maxY = Integer.MIN_VALUE;
    }

    /**
     * Adds the input tile to this tile.
     *
     * @param tile the Tile to add.
     */
    public void addTile(final Tile t) {
        addTile(t.x, t.y, t.maxX, t.maxY);
    }

    /**
     * Adds the input tile, defined by its x, y, maxX, maxY
     * values, to this tile.
     *
     * @param tx the tile's x-axis origin
     * @param ty the tile's y-axis origin
     * @param tmaxX the tile's x-axis bottom right coordinate.
     * @param tmaxY the tile's y-axis bottom right coordinate.
     */
    public void addTile(final int tx,
                        final int ty,
                        final int tmaxX,
                        final int tmaxY) {
        if (tx < x) {
            x = tx;
        }
        if (ty < y) {
            y = ty;
        }
        if (tmaxX > maxX) {
            maxX = tmaxX;
        }
        if (tmaxY > maxY) {
            maxY = tmaxY;
        }
    }

    /**
     * Adds the input Box to the tile, after snapping it to the integer grid.
     *
     * @param b the Box instance to snap to the grid and add to the tile. Should
     *        not be null.
     */
    public void addSnapBox(final Box b) {
        b.snap();
        addTile((int) b.x, 
                (int) b.y, 
                (int) (b.x + b.width - 1), 
                (int) (b.y + b.height - 1));
    }

    /**
     * Sets the tile so that it snaps to the smallest pixel grid which completely
     * contains the input Box.
     *
     * @param b the Box instance to snap to the grid. If null, the tile is set to 
     *        have all its values set to Integer.MIN_VALUE.
     */
    public void snapBox(final Box b) {
        if (b == null) {
            x = Integer.MIN_VALUE;
            y = Integer.MIN_VALUE;
            maxX = Integer.MIN_VALUE;
            maxY = Integer.MIN_VALUE;
        } else {
            b.snap();
            x = (int) b.x;
            y = (int) b.y;
            maxX = (int) (b.x + b.width - 1);
            maxY = (int) (b.y + b.height - 1);
        }

        
    }

    /**
     * @return true if the tile is hit by the input tile.
     * @param t the tile to check. Should not be null.
     */
    public boolean isHit(final Tile t) {
        return 
            (t.maxX >= x)  // Leave out tiles to the left of this tile
            &&
            (t.maxY >= y)  // Leave out tiles to the top of this tile
            &&
            (t.x <= maxX)  // Leave out tiles to the right of this tile
            &&
            (t.y <= maxY); // Leave out tiles to the bottom of this tile
    }

    /**
     * Debug.
     */
    public String toString() {
        return "minX = " + x + " minY = " + y + " maxX = " + maxX + " maxY = " + maxY;
    }
}
