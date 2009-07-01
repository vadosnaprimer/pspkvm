/*
 *
 *
 * Sun Proprietary Confidential, Internal Use Only
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

/**
 * This interface is used to access and set computed property values
 * for graphical characteristics such as fill or stroke color. 
 *
 */
public interface GraphicsProperties {
    // =======================================================================
    // Initial Property values
    // =======================================================================

    /**
     * Default value for the display property
     */
    boolean INITIAL_DISPLAY = true;

    /**
     * Default value for the visibility property
     */
    boolean INITIAL_VISIBILITY = true;

    /**
     * Default value for the current color
     */
    RGB INITIAL_COLOR = RGB.black;

    /**
     * Default value for the fill paint
     */
    RGB INITIAL_FILL = RGB.black;

    /**
     * Default value for fill opacity
     */
    float INITIAL_FILL_OPACITY = 1;

    /**
     * Default value for the stroke paint
     */
    RGB INITIAL_STROKE = null;

    /**
     * Default value for stroke opacity
     */
    float INITIAL_STROKE_OPACITY = 1;

    /**
     * Default stroke-width value
     */
    int INITIAL_FILL_RULE = RenderContext.WIND_NON_ZERO;

    /**
     * Default stroke width
     */
    float INITIAL_STROKE_WIDTH = 1;

    /**
     * Default line join
     */
    int INITIAL_STROKE_LINE_JOIN = RenderContext.JOIN_MITER;

    /**
     * Default line cap
     */
    int INITIAL_STROKE_LINE_CAP = RenderContext.CAP_BUTT;

    /**
     * Default miter limit
     */
    float INITIAL_STROKE_MITER_LIMIT = 4;

    /**
     * Default dash array
     */
    float[] INITIAL_STROKE_DASH_ARRAY = null;

    /**
     * Default dash offset
     */
    float INITIAL_STROKE_DASH_OFFSET = 0;

    /**
     * Default value for opacity
     */
    float INITIAL_OPACITY = 1;

    // =======================================================================
    // Value constants
    // =======================================================================

    /**
     * The even-odd rule specifies that a point lies inside the
     * path if a ray drawn in any direction from that point to
     * infinity is crossed by path segments an odd number of times.
     */
    int WIND_EVEN_ODD = 0;

    /**
     * The non-zero rule specifies that a point lies inside the
     * path if a ray drawn in any direction from that point to
     * infinity is crossed by path segments a different number
     * of times in the counter-clockwise direction than the
     * clockwise direction.
     */
    int WIND_NON_ZERO = 1;

    /**
     * Joins path segments by extending their outside edges until
     * they meet.
     */
    int JOIN_MITER = 0;

    /**
     * Joins path segments by rounding off the corner at a radius
     * of half the line width.
     */
    int JOIN_ROUND = 1;

    /**
     * Joins path segments by connecting the outer corners of their
     * wide outlines with a straight segment.
     */
    int JOIN_BEVEL = 2;

    /**
     * Ends unclosed subpaths and dash segments with no added
     * decoration.
     */
    int CAP_BUTT = 0;

    /**
     * Ends unclosed subpaths and dash segments with a round
     * decoration that has a radius equal to half of the width
     * of the pen.
     */
    int CAP_ROUND = 1;

    /**
     * Ends unclosed subpaths and dash segments with a square
     * projection that extends beyond the end of the segment
     * to a distance equal to half of the line width.
     */
    int CAP_SQUARE = 2;

    // =======================================================================
    // Property access
    // =======================================================================

    /**
     * @param fill the new paint to use for fill operations
     */
    void setFill(PaintServer fill);    

    /**
     * @return the paint used for fill operations
     */
    PaintServer getFill();

    /**
     * @param fillOpacity the new opacity to use for fill operations.
     *        The value is clamped to the [0, 1] range.
     */
    void setFillOpacity(float fillOpacity);

    /**
     * @return the opacity used for fill operations, in the [0, 1] range.
     */
    float getFillOpacity();
    
    /**
     * @param stroke the new paint to use to fill stroked path
     */
    void setStroke(PaintServer stroke);    

    /**
     * @return the paint used to fill the stroked path
     */
    PaintServer getStroke();
    
    /**
     * @param strokeOpacity the new opacity to use for stroke operations.
     *        The value is clamped to the [0, 1] range.
     */
    void setStrokeOpacity(float strokeOpacity);

    /**
     * @return the opacity used for stroke operations, in the [0, 1] range.
     */
    float getStrokeOpacity();
    
    /**
     * @param color the new color property value
     */
    void setColor(RGB color);    

    /**
     * @return the current color property value.
     */
    RGB getColor();

    /**
     * @param fillRule the new rull to fill shapes
     * @see #getFillRule
     */
    void setFillRule(int fillRule);    

    /**
     * @return the rule used to fill shapes. One of WIND_NON_ZERO or
     *         WIND_EVEN_ODD.
     * @see #WIND_EVEN_ODD
     * @see #WIND_NON_ZERO
     */
    int getFillRule();

    /**
     * @param strokeDashArray the new set of dashes and gaps to use for 
     *        stroking operations.
     * @see #getStrokeDashArray
     */
    void setStrokeDashArray(float[] strokeDashArray);
    
    /**
     * @return an array of float value describing alternance of 
     *         solid and transparent sections on stroked paths, starting
     *         with dash, followed by a gap.
     * @see #getStrokeDashOffset
     */
    float[] getStrokeDashArray();

    /**
     * @param strokeLineCap the new line cap decoration style
     */
    void setStrokeLineCap(int strokeLineCap);    

    /**
     * @return the style used to decorate the ends of unclosed
     *         path segments. One of CAP_BUTT, CAP_ROUND or CAP_SQUARE.
     */
    int getStrokeLineCap();

    /**
     * @param strokeLineJoin the new line join style
     * @see #getStrokeLineJoin
     */
    void setStrokeLineJoin(int strokeLineJoin);
    
    /**
     * @return the style used to decorate line segment intersections.
     *         Can be one of JOIN_BEVEL, JOIN_MITER, JOIN_ROUND
     */
    int getStrokeLineJoin();

    /**
     * @param strokeWidth the new stroke width
     * @see #getStrokeWidth
     */
    void setStrokeWidth(float strokeWidth);    

    /**
     * @return the width to use for stroking. This is expressed in user 
     *         space and is the width of the line measured on a 
     *         perpendicular to the stroked path.
     */
    float getStrokeWidth();

    /**
     * @param strokeMiterLimit the new value for the miter limit
     * @see #getStrokeMiterLimit
     */
    void setStrokeMiterLimit(float strokeMiterLimit);    

    /**
     * @return the limit of miter joins. Line joins which exceed this 
     *         value are trimmed. The miter is expressed as the ratio
     *         of the miter length (i.e, the distance between the inner
     *         and the outer elbows) to the stroke width.
     */
    float getStrokeMiterLimit();

    /**
     * @param strokeDashOffset the offset, in user space, in the 
     *        stroke's dash array. This is ignored if the dash array
     *        is null
     */
    void setStrokeDashOffset(float strokeDashOffset);   

    /**
     * @return the offset in the dash array.
     */
    float getStrokeDashOffset();

    /**
     * @param visibility the new visibility property value
     */
    void setVisibility(boolean visibility);    

    /**
     * @return true if the node is visible. false otherwise
     */
    boolean getVisibility();

    /**
     * @param display the new display property value
     */
    void setDisplay(boolean display);    

    /**
     * @return true if the display is on. false otherwise
     */
    boolean getDisplay();

    /**
     * @param opacity the new opacity to use for blending operations.
     *        The value is clamped to the [0, 1] range.
     */
    void setOpacity(float opacity);

    /**
     * @return the opacity used for blending operations, in the [0, 1] range.
     */
    float getOpacity();

}
