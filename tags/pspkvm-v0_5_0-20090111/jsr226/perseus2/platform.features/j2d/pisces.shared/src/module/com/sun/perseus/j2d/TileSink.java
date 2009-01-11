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

import com.sun.pisces.PathSink;
import com.sun.pisces.Transformer;

/**
 * @version $Id: TileSink.java,v 1.3 2006/04/21 06:35:41 st125089 Exp $
 */
class TileSink extends PathSink {
    /**
     * The minimum x coordinate in S15.16 format.
     */
    protected int minX;

    /**
     * The minimum y coordinate in S15.16 format.
     */
    protected int minY;

    /**
     * The maximum x coordinate in S15.16 format.
     */
    protected int maxX;

    /**
     * The maximum y coordinate in S15.16 format.
     */
    protected int maxY;

    /**
     * Resets the bounds to their initial values.
     */
    public void reset() {
        minX = Integer.MAX_VALUE;
        minY = Integer.MAX_VALUE;
        maxX = Integer.MIN_VALUE;
        maxY = Integer.MIN_VALUE;
    }

    /**
     * Moves the current drawing position to the point <code>(x0,
     * y0)</code>.
     *
     * @param x0 the X coordinate in S15.16 format
     * @param y0 the Y coordinate in S15.16 format
     */
    public void moveTo(int x0, int y0) {
        checkPoint(x0, y0);
    }

    /**
     * Checks whether this point falls within the current region. If not,
     * adjusts minX, minY, maxX, maxY.
     */
    void checkPoint(final int x0, final int y0) {
        if (x0 < minX) {
            minX = x0;
        } else if (x0 > maxX) {
            maxX = x0;
        }
            
        if (y0 < minY) {
            minY = y0;
        } else if (y0 > maxY) {
            maxY = y0;
        }
    }

    /**
     * Provides a hint that the current segment should be joined to
     * the following segment using an explicit miter or round join if
     * required.
     *
     * <p> An application-generated path will generally have no need
     * to contain calls to this method; they are typically introduced
     * by a <code>Flattener</code> to mark segment divisions that
     * appear in its input, and consumed by a <code>Stroker</code>
     * that is responsible for emitting the miter or round join
     * segments.
     *
     * <p> Other <code>LineSink</code> classes should simply pass this
     * hint to their output sink as needed.
     */
    public void lineJoin() {
    }

    /**
     * Draws a line from the current drawing position to the point
     * <code>(x1, y1)</code> and sets the current drawing position to
     * <code>(x1, y1)</code>.
     *
     * @param x1 the X coordinate in S15.16 format
     * @param y1 the Y coordinate in S15.16 format
     */
    public void lineTo(int x1, int y1) {
        checkPoint(x1, y1);
    }

    /**
     * Closes the current path by drawing a line from the current
     * drawing position to the point specified by the moset recent
     * <code>moveTo</code> command.
     */
    public void close() {
    }

    /**
     * Ends the current path.  It may be necessary to end a path in
     * order to allow end caps to be drawn.
     */
    public void end() {
    }

    /**
     * Draws a quadratic Bezier curve starting at the current drawing
     * position and ending at the point <code>(x2, y2)</code>
     * according to the formulas:
     *
     * <pre>
     * x(t) = (1 - t)^2*x0 + 2*(1 - t)*t*x1 + t^2*x2
     * y(t) = (1 - t)^2*y0 + 2*(1 - t)*t*y1 + t^2*x2
     * 
     * 0 <= t <= 1
     * </pre>
     *
     * where <code>(x0, y0)</code> is the current drawing position.
     * Finally, the current drawing position is set to <code>(x2,
     * y2)</code>.
     *
     * @param x1 the X coordinate of the control point in S15.16 format
     * @param y1 the Y coordinate of the control point in S15.16 format
     * @param x2 the final X coordinate in S15.16 format
     * @param y2 the final Y coordinate in S15.16 format
     */
    public void quadTo(final int x1, final int y1, final int x2, final int y2) {
        checkPoint(x1, y1);
        checkPoint(x2, y2);
    }

    /**
     * Draws a cubic Bezier curve starting at the current drawing
     * position and ending at the point <code>(x3, y3)</code>
     * according to the formulas:
     *
     * <pre>
     * x(t) = (1 - t)^3*x0 + 3*(1 - t)^2*t*x1 + 3*(1 - t)*t^2*x2 + t^3*x3
     * y(t) = (1 - t)^3*y0 + 3*(1 - t)^2*t*y1 + 3*(1 - t)*t^2*y2 + t^3*x3
     * 
     * 0 <= t <= 1
     * </pre>
     *
     * where <code>(x0, y0)</code> is the current drawing position.
     * Finally, the current drawing position is set to <code>(x3,
     * y3)</code>.
     *
     * @param x1 the X coordinate of the first control point in S15.16 format
     * @param y1 the Y coordinate of the first control point in S15.16 format
     * @param x2 the X coordinate of the second control point in S15.16 format
     * @param y2 the Y coordinate of the second control point in S15.16 format
     * @param x3 the final X coordinate in S15.16 format
     * @param y3 the final Y coordinate in S15.16 format
     */
    public void cubicTo(final int x1, final int y1, final int x2, final int y2, final int x3, final int y3) {
        checkPoint(x1, y1);
        checkPoint(x2, y2);
        checkPoint(x3, y3);
    }

    /**
     * Sets the tile to the current values.
     *
     * @param tile the tile to set.
     */
    public void setTile(final Tile tile) {
        tile.x = minX >> 16;
        tile.y = minY >> 16;
        tile.maxX = (maxX >> 16) - 1;
        tile.maxY = (maxY >> 16) - 1;

        // Precision adjustments.

        // If x and y are positive, then, the int value for minX is 
        // smaller than or equal to the fixed point value, so there
        // is no adjustment needed.

        // If x and y are netative, then, the int value for minX is
        // greater than the fixed point value. We need to be at least
        // as large as the bounds, so we decrease the value by one
        // to encompass the origin.

        if (minX < 0 && (minX & 0xffff) != 0) {
            tile.x -= 1;
        }

        if (minY < 0 && (minX & 0xffff) != 0) {
            tile.y -= 1;
        }

        // If there is a fractional part in the maxX and maxY values, 
        // we adjust them to be at least as big as the rendering area.
        if (maxX > 0 && (maxX & 0xffff) != 0) {
            tile.maxX += 1;
        }

        if (maxY > 0 && (maxY & 0xffff) != 0) {
            tile.maxY += 1;
        }
        
    }
}
