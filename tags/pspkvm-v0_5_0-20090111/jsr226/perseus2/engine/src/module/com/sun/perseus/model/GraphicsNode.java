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

import com.sun.perseus.j2d.GraphicsProperties;

import com.sun.perseus.util.SVGConstants;

/**
 * <code>GraphicsNode</code> is the interface that all <code>ModelNode</code>
 * (see {@link com.sun.perseus.model.ModelNode ModelNode}) which correspond
 * to graphical content implement.
 * <br />
 * <code>GraphicsNode</code>s have a notion of visibility and display. If a
 * node is not <em>displayed</em>, then it does not paint and none of its 
 * children (if present) are painted. If a node is invisible, then it does 
 * no paint itself, but it paints it's children. In other words, it a node
 * is invisible, its chidren may be visible (depending on the children's 
 * own visibility setting), but if a node is not displayed,
 * then its children are not displayed either, no matter what their own 
 * display setting is. This corresponds to the CSS 2 concept of visibility
 * and display.
 * <br />
 * IMPORTANT NOTE: Setting a property on a <code>GraphicsNode</code> 
 * automatically sets the inherited and color relative flags to false.
 *
 * @see ModelNode
 *
 * @version $Id: GraphicsNode.java,v 1.5 2006/06/29 10:47:31 ln156897 Exp $
 */
public interface GraphicsNode extends DecoratedNode, GraphicsProperties {
    /**
     * The fill property controls the color of the fill operation
     * @see com.sun.perseus.j2d.GraphicsProperties#setFill
     */ 
    int PROPERTY_FILL                = 1;

    /**
     * The stroke property controls the color of the stroke
     * @see com.sun.perseus.j2d.GraphicsProperties#setStroke
     */
    int PROPERTY_STROKE              = 1 << 1;
    
    /**
     * The color property controls the 'current color'. The current 
     * color defines the value of the fill or stroke properties when they 
     * are 'color relative'.
     * @see #setColorRelative
     * @see com.sun.perseus.j2d.GraphicsProperties#setColor
     */
    int PROPERTY_COLOR               = 1 << 2;

    /**
     * The fill rule property controls how the interior of shapes is computed.
     * @see com.sun.perseus.j2d.GraphicsProperties#WIND_EVEN_ODD
     * @see com.sun.perseus.j2d.GraphicsProperties#WIND_NON_ZERO
     * @see com.sun.perseus.j2d.GraphicsProperties#setFillRule
     */
    int PROPERTY_FILL_RULE           = 1 << 3;

    /**
     * The stroke width property controls how wide the stroke is. 
     * @see com.sun.perseus.j2d.GraphicsProperties#setStrokeWidth
     */
    int PROPERTY_STROKE_WIDTH        = 1 << 4;

    /**
     * The stroke line join property defines the style of miter, i.e., 
     * the style of elbows between segments.
     *
     * @see com.sun.perseus.j2d.GraphicsProperties#JOIN_MITER
     * @see com.sun.perseus.j2d.GraphicsProperties#JOIN_ROUND
     * @see com.sun.perseus.j2d.GraphicsProperties#JOIN_BEVEL
     * @see com.sun.perseus.j2d.GraphicsProperties#setStrokeLineJoin
     * 
     */
    int PROPERTY_STROKE_LINE_JOIN    = 1 << 5;

    /**
     * The stroke line cap property defines the style of line caps, 
     * i.e., the style of the end and begining of line strokes.
     * @see com.sun.perseus.j2d.GraphicsProperties#CAP_BUTT
     * @see com.sun.perseus.j2d.GraphicsProperties#CAP_ROUND
     * @see com.sun.perseus.j2d.GraphicsProperties#CAP_SQUARE
     * @see com.sun.perseus.j2d.GraphicsProperties#setStrokeLineCap
     */
    int PROPERTY_STROKE_LINE_CAP     = 1 << 6;

    /**
     * Provides a way to limit the extent of 'spikes' out
     * of angle elbows.
     * @see com.sun.perseus.j2d.GraphicsProperties#setStrokeMiterLimit
     */
    int PROPERTY_STROKE_MITER_LIMIT  = 1 << 7;

    /**
     * Array defining the stroke's dash pattern.
     * @see com.sun.perseus.j2d.GraphicsProperties#setStrokeDashArray
     */
    int PROPERTY_STROKE_DASH_ARRAY   = 1 << 8;

    /**
     * Offset in the stroke dash array
     * @see #PROPERTY_STROKE_DASH_ARRAY
     * @see com.sun.perseus.j2d.GraphicsProperties#setStrokeDashOffset
     */
    int PROPERTY_STROKE_DASH_OFFSET = 1 << 9;

    /**
     * Controls the offset in the dash array, in
     * user space
     * @see com.sun.perseus.j2d.GraphicsProperties#setDisplay
     */
    int PROPERTY_DISPLAY             = 1 << 10;

    /**
     * Controls whether a node is visible or not. Children of
     * a node which is not visible can still be visible.
     * @see com.sun.perseus.j2d.GraphicsProperties#setVisibility
     */
    int PROPERTY_VISIBILITY          = 1 << 11;

    /**
     * Controls the opacity used in a fill operation.
     * @see com.sun.perseus.j2d.GraphicsProperties#setFillOpacity
     */
    int PROPERTY_FILL_OPACITY        = 1 << 12;

    /**
     * Controls the opacity used in a stroking operation.
     * @see com.sun.perseus.j2d.GraphicsProperties#setStrokeOpacity
     */
    int PROPERTY_STROKE_OPACITY      = 1 << 13;

    /**
     * Controls the opacity used in blending the offscreen image 
     * into the current background.
     * @see com.sun.perseus.j2d.GraphicsProperties#setOpacity
     */
    int PROPERTY_OPACITY      = 1 << 14;

    /**
     * @param propertyIndex index of the property to check
     * @return true if the input property is color relative. False
     * otherwise
     * @see #setColorRelative
     */
    boolean isColorRelative(int propertyIndex);

    /**
     * Returns true if the input property can be color-relative.
     * 
     * @param propertyIndex the index of the property which may be 
     *        color-relative.
     * @return true if the input property can be color relative. False 
     *         otherwise.
     */
    boolean isColorRelativeProperty(final int propertyIndex);

    /**
     * Sets the input property as a color-relative property. There are
     * two color-relative properties on a <code>GraphicsNode</code>:
     * <code>fill</code> and <code>stroke</code>. For all other properties,
     * setting the property as color relative should not have effect.
     * For <code>fill</code> and <code>stroke</code>, setting them as
     * color relative means that the <code>color</code> property should 
     * be used for the corresponding fill or draw operations.
     *
     * @param propertyIndex index of the property
     * @param isColorRelative the new state fot the property's color relative
     *        value.
     */
    void setColorRelative(int propertyIndex, boolean isColorRelative);

    /**
     * Default inheritance setting (Y=yes, N=no):
     * <pre>
     * - Y fill
     * - Y stroke
     * - Y color
     * - Y fill rule
     *
     * - Y stroke width
     * - Y line join
     * - Y line cap
     * - Y miter limit
     *
     * - Y dash array
     * - Y dash offset
     * - N display
     * - Y visibility
     *
     * - Y fill opacity
     * - Y stroke opacity
     * - N opacity
     * </pre>
     */
    int DEFAULT_INHERITANCE = 0x3BFF;

    /**
     * Default color relative (Y=yes, N=no):
     * <pre>
     * - N fill
     * - N stroke
     * - N color
     * - N fill rule
     *
     * - N stroke width
     * - N line join
     * - N line cap
     * - N miter limit
     *
     * - N dash array
     * - N dash offset
     * - N display
     * - N visibility
     *
     * - N fill opacity
     * - N stroke opacity
     * - N opacity
     * </pre>
     */
    int DEFAULT_COLOR_RELATIVE = 0x0000;

    /**
     * Number of properties in a GraphicsNode
     */
    int NUMBER_OF_PROPERTIES = 15;
}
