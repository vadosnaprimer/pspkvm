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

import com.sun.pisces.PiscesRenderer;


/**
 * All rendering in Perseus is done through the <tt>RenderGraphics</tt> class. 
 * <br />
 * <tt>RenderGraphics</tt> is the combination of the traditional 
 * <tt>Graphics2D</tt> API to the rendering engine and the notion of 
 * graphical context found in SVG.<br />
 * <br />
 * A <tt>RenderGraphics</tt> object proxies invocation to a <tt>Graphics2D</tt>
 * instance through its <tt>draw</tt> or <tt>fill</tt> method while capturing 
 * the current rendering context state by implementing the 
 * <tt>RenderContext</tt> interface.
 * <br />
 * <b>Note A</b>: the Java 2D graphic context values passed by the 
 * <tt>RenderGraphics</tt> to the proxied <tt>Graphics2D</tt> correspond to 
 * the CSS 2 <a href="http://www.w3.org/TR/REC-CSS2/cascade.html#actual-value">
 * actual</a> values.
 * <br />
 * <b>Note B</b>: the initial values for the context properties (such 
 * as <tt>color</tt> or <tt>fill</tt>) correspond to the CSS 2 
 * <a href="http://www.w3.org/TR/REC-CSS2/about.html#q7">
 * initial</a> values for these properties.
 *
 * @see RenderContext
 * @see java.awt.Graphics2D
 *
 * @version $Id: RenderGraphics.java,v 1.5 2006/04/21 06:35:43 st125089 Exp $
 */
public class RenderGraphics extends PiscesRenderGraphics {
    /**
     * Constructs a new <code>RenderGraphics</code> which will delegate painting
     * operations to a <code>Graphics2D</code> built for the input
     * BufferedImage.  
     * 
     * @param pr the <tt>PiscesRenderer</tt> to render to.
     * @param width the rendering surface width. Should be greater than zero.
     * @param height the rendering surface height. Should be greater than zero.
     * @throws NullPointerException if bi is null
     * @throws NullPointerException if bi is null
     */
    public RenderGraphics(final PiscesRenderer pr, final int width, final int height) {
        super(pr, width, height);
    }

    /** 
     * Clears the specified rectangle. IMPORTANT NOTE: the coordinates are in
     * device space. This method does not account for the current transformation
     * set on the RenderGraphics. It operates on the target pixels.
     * 
     * @param x - the x coordinate of the rectangle to clear.
     * @param y - the y coordinate of the rectangle to clear.
     * @param width - the width of the rectangle to clear.
     * @param height - the height of the rectangle to clear.
     * @param clearColor - the color to use to clear the rectangle.
     */
    public void clearRect(int x,
                          int y,
                          int width,
                          int height,
                          RGB clearColor) {
        pr.setColor(clearColor.getRed(), 
                    clearColor.getGreen(), 
                    clearColor.getBlue(),
                    clearColor.getAlpha());
        pr.clearRect(x, y, width, height);
    }

}
