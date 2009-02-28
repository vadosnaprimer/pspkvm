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

/**
 * {@link com.sun.perseus.model.ModelNode ModelNode} are rendered into a 
 * {@link RenderGraphics RenderGraphics} which extends
 * the <tt>RenderContext</tt> class. A <tt>RenderContext</tt> captures
 * the current state of the properties used in painting operations. When a 
 * <tt>ModelNode</tt> tree is rendered, the <tt>RenderGraphics</tt> 
 * is passed down the tree to children nodes as they are painted. This 
 * provides the solution for computing CSS inherited values: if a 
 * <tt>ModelNode</tt> inherits one of its properties, it will simply use
 * the one currently set in the <tt>RenderContext</tt>.
 * <br />
 * Initially (i.e., before any painting operation happens), the 
 * <tt>RenderContext</tt> holds values which correspond to the CSS 2 concept of
 * <a href="http://www.w3.org/TR/REC-CSS2/about.html#q7">initial</a> values. 
 * <br />
 * At any time during a rendering operation, the <tt>RenderContext</tt> holds 
 * values which correspond to the CSS 2 
 * <a href="http://www.w3.org/TR/REC-CSS2/cascade.html#computed-value">
 * computed</a> values.
 *
 * Note that in addition to properties that correspond to CSS2 properties, the 
 * <tt>RenderContext</tt> also keeps an <tt>AffineTransform</tt> which is
 * not a CSS2 property. The transform represents the current user space to 
 * device space transformation.
 *
 * @see com.sun.perseus.model.ModelNode
 * @see com.sun.perseus.model.ElementNode#paint
 *
 * @version $Id: RenderContext.java,v 1.5 2006/04/21 06:35:38 st125089 Exp $
 */
public class RenderContext implements TextRenderingProperties {
    // =======================================================================
    // Initial Property values used internally
    // =======================================================================
    /**
     * Default stroke width
     */
    protected static int INITIAL_STROKE_WIDTH_IMPL = 1 << 16;

    /**
     * Default miter limit
     */
    protected static int INITIAL_STROKE_MITER_LIMIT_IMPL = 4 << 16;

    /**
     * Default dash array
     */
    protected static int[] INITIAL_STROKE_DASH_ARRAY_IMPL = null;

    /**
     * Default dash offset
     */
    protected static int INITIAL_STROKE_DASH_OFFSET_IMPL = 0;

    // =======================================================================
    // Constants used for packing and unpacking data in the pack member
    // =======================================================================
    
    protected static final int FONT_STYLE_MASK =       0x60000000;    /* 30-29 */
    protected static final int FONT_WEIGHT_MASK =      0x1E000000;    /* 28-27-26-25 */
    protected static final int TEXT_ANCHOR_MASK =      0x01800000;    /* 24-23 */
    protected static final int STROKE_OPACITY_MASK =   0x007F8000;    /* 22-21-20-19-18-17-16-15 */
    protected static final int FILL_OPACITY_MASK =     0x00007F80;    /* 14-13-12-11-10-09-08-07 */
    protected static final int FILL_RULE_MASK =        0x00000040;    /* 06 */
    protected static final int STROKE_LINE_CAP_MASK =  0x00000030;    /* 05-04 */
    protected static final int STROKE_LINE_JOIN_MASK = 0x0000000C;    /* 03-02 */
    protected static final int VISIBILITY_MASK =       0x00000002;    /* 01 */
    protected static final int DISPLAY_MASK =          0x00000001;    /* 00 */

    // =======================================================================
    // Constants used for packing and unpacking data in the pack2 member
    // =======================================================================
    protected static final int OPACITY_MASK =   
            0x000000FF;    /* 7-6-5-4-3-2-1-0 */

    protected static final int FONT_STYLE_NORMAL_IMPL =  0x00000000;
    protected static final int FONT_STYLE_ITALIC_IMPL =  0x40000000;
    protected static final int FONT_STYLE_OBLIQUE_IMPL = 0x60000000;

    protected static final int FONT_WEIGHT_100_IMPL = 0x00000000;
    protected static final int FONT_WEIGHT_200_IMPL = 0x02000000;
    protected static final int FONT_WEIGHT_300_IMPL = 0x04000000;
    protected static final int FONT_WEIGHT_400_IMPL = 0x06000000;
    protected static final int FONT_WEIGHT_500_IMPL = 0x08000000;
    protected static final int FONT_WEIGHT_600_IMPL = 0x0A000000;
    protected static final int FONT_WEIGHT_700_IMPL = 0x0C000000;
    protected static final int FONT_WEIGHT_800_IMPL = 0x0E000000;
    protected static final int FONT_WEIGHT_900_IMPL = 0x10000000;

    protected static final int TEXT_ANCHOR_MIDDLE_IMPL = 0x00000000;
    protected static final int TEXT_ANCHOR_START_IMPL  = 0x00800000;
    protected static final int TEXT_ANCHOR_END_IMPL =    0x01000000;

    protected static final int CAP_BUTT_IMPL =   0x00000000;
    protected static final int CAP_ROUND_IMPL =  0x00000010;
    protected static final int CAP_SQUARE_IMPL = 0x00000020;

    protected static final int JOIN_MITER_IMPL = 0x00000000;
    protected static final int JOIN_ROUND_IMPL = 0x00000004;
    protected static final int JOIN_BEVEL_IMPL = 0x00000008;

    // =======================================================================
    // Property values, some are packed.
    // =======================================================================
    /**
     * Color used for fill operations
     */
    protected PaintServer fill = INITIAL_FILL;

    /**
     * Color used for fill operations
     */
    protected PaintServer stroke = INITIAL_STROKE;

    /**
     * Context Color. Corresponds to the concept of 
     * 'current color' in CSS
     */
    protected RGB color = INITIAL_COLOR;

    /**
     * Controls the strokeDash pattern for stroke operations.
     * If the array is null, a solid line is used
     */
    protected int[] strokeDashArray = INITIAL_STROKE_DASH_ARRAY_IMPL;

    /**
     * Controls the width of the stroke. 15.16 format.
     */
    protected int strokeWidth = INITIAL_STROKE_WIDTH_IMPL;

    /**
     * Controls the miter limit for stroke operations. 15.16 format.
     */
    protected int strokeMiterLimit = INITIAL_STROKE_MITER_LIMIT_IMPL;

    /**
     * Controls the offset in the dash array for
     * stroke operations. 15.16 format.
     */
    protected int strokeDashOffset = INITIAL_STROKE_DASH_OFFSET_IMPL;

    /**
     * Controls the current font family. The font family is 
     * a list, which means that a set of fonts may be used
     * to render a given text string. The fonts are queried in
     * order to check which ones can render characters in the
     * drawn text.
     */
    protected String[] fontFamily = INITIAL_FONT_FAMILY;

    /**
     * Controls the current font size
     */
    protected float fontSize = INITIAL_FONT_SIZE;

    // 
    // Property pack
    //
    // fontStyle:     3 styles,  2 bits
    // fontWeight:    9 weights, 4 bits
    // textAnchor     3 values,  2 bits
    // strokeOpacity:            8 bits [0-200]
    // fillOpacity:              8 bits [0-200]
    // fillRule:                 1 bits
    // strokeLineCap: 3 values,  2 bits
    // strokeLineJoin 3 values,  2 bits
    // visibility: 2 values,     1 bit
    // display: 2 values,        1 bit
    // Total:                   31 bits
    // 
    // Encoding:
    //
    // fontStyle: 30-29
    // fontWeight: 28-27-26-25
    // textAnchor: 24-23
    // strokeOpacity: 22-21-20-19-18-17-16-15
    // fillOpacity: 14-13-12-11-10-09-08-07
    // fillRule: 06
    // strokeLineCap: 05-04
    // strokeLineJoin: 03-02
    // visibility: 01
    // display: 00
    //
    protected int pack = INITIAL_PACK;
    
    protected static final int INITIAL_FONT_STYLE_IMPL = FONT_STYLE_NORMAL_IMPL;
    protected static final int INITIAL_FONT_WEIGHT_IMPL = FONT_WEIGHT_400_IMPL;
    protected static final int INITIAL_TEXT_ANCHOR_IMPL = TEXT_ANCHOR_START_IMPL;
    protected static final int INITIAL_STROKE_OPACITY_IMPL = 200 << 15;
    protected static final int INITIAL_FILL_OPACITY_IMPL = 200 << 7;
    protected static final int INITIAL_FILL_RULE_IMPL = 0;
    protected static final int INITIAL_STROKE_LINE_CAP_IMPL = CAP_BUTT_IMPL;
    protected static final int INITIAL_STROKE_LINE_JOIN_IMPL = JOIN_MITER_IMPL;
    protected static final int INITIAL_VISIBILITY_IMPL = 0x2;
    protected static final int INITIAL_DISPLAY_IMPL = 0x1;
    protected static final int INITIAL_PACK = 
        INITIAL_FONT_STYLE_IMPL
        |
        INITIAL_FONT_WEIGHT_IMPL
        |
        INITIAL_TEXT_ANCHOR_IMPL
        |
        INITIAL_STROKE_OPACITY_IMPL
        |
        INITIAL_FILL_OPACITY_IMPL
        |
        INITIAL_FILL_RULE_IMPL
        |
        INITIAL_STROKE_LINE_CAP_IMPL
        |
        INITIAL_STROKE_LINE_JOIN_IMPL
        |
        INITIAL_VISIBILITY_IMPL
        |
        INITIAL_DISPLAY_IMPL;

    // 
    // Property pack2
    //
    // opacity:                  8 bits [0-255]
    // Total:                    8 bits
    // 
    // Encoding:
    //
    // opacity: 07-06-05-04-03-02-01-00
    //
    protected int pack2 = INITIAL_PACK2;    

    protected static final int INITIAL_OPACITY_IMPL = 200;
    protected static final int INITIAL_PACK2 = 
        INITIAL_OPACITY_IMPL;

    /**
     * Default constructor
     */
    public RenderContext() {
    }

    /**
     * Copy constructor
     * @param rcs the <tt>RenderContext</tt> to copy values from. Should
     *        not be null.
     */
    public RenderContext(final RenderContext rcs) {
        restore(rcs);
    }

    /**
     * @param rcs the <tt>RenderContext</tt> state to restore. Should
     *        not be null.
     */
    public void restore(final RenderContext rcs) {
        this.fill = rcs.fill;
        this.stroke = rcs.stroke;
        this.color = rcs.color;
        this.strokeDashArray = rcs.strokeDashArray;
        this.strokeWidth = rcs.strokeWidth;
        this.strokeMiterLimit = rcs.strokeMiterLimit;
        this.strokeDashOffset = rcs.strokeDashOffset;
        this.fontFamily = rcs.fontFamily;
        this.fontSize = rcs.fontSize;
        this.pack = rcs.pack;
        this.pack2 = rcs.pack2;
    }

    /**
     * Resets context to initial values
     */
    public void reset() {
        this.fill = INITIAL_FILL;
        this.stroke = INITIAL_STROKE;
        this.color = INITIAL_COLOR;
        this.strokeDashArray = INITIAL_STROKE_DASH_ARRAY_IMPL;
        this.strokeWidth = INITIAL_STROKE_WIDTH_IMPL;
        this.strokeMiterLimit = INITIAL_STROKE_MITER_LIMIT_IMPL;
        this.strokeDashOffset = INITIAL_STROKE_DASH_OFFSET_IMPL;
        this.fontFamily = INITIAL_FONT_FAMILY;
        this.fontSize = INITIAL_FONT_SIZE;
        this.pack = INITIAL_PACK;
        this.pack2 = INITIAL_PACK2;
    }

    /**
     * @param newFill the new value for the fill property
     */
    public void setFill(final PaintServer newFill) {
        this.fill = newFill;
    }

    /**
     * @return the context's fill property value
     */
    public PaintServer getFill() {
        return fill;
    }

    /**
     * @param newFillOpacity the new value for the fill opacity property. The 
     *        input value is clamped to the [0, 1] range.
     */
    public void setFillOpacity(float newFillOpacity) {
        if (newFillOpacity < 0) {
            newFillOpacity = 0;
        } else if (newFillOpacity > 1) {
            newFillOpacity = 1;
        }
        
        pack &= ~FILL_OPACITY_MASK;
        pack |= ((((int) (newFillOpacity * 200)) << 7) & FILL_OPACITY_MASK);
    }

    /**
     * @return the context's fill opacity property value
     */
    public float getFillOpacity() {
        return ((pack & FILL_OPACITY_MASK) >> 7) / 200.0f;
    }

    /**
     * @return the context's fill opacity property value
     */
    int getFillOpacityImpl() {
        return (int) (((pack & FILL_OPACITY_MASK) >> 7) * 255f / 200f);
    }

    /**
     * @param newStroke the new value for the stroke property
     */
    public void setStroke(final PaintServer newStroke) {
        this.stroke = newStroke;
    }

    /**
     * @return the context's stroke property value
     */
    public PaintServer getStroke() {
        return stroke;
    }

    /**
     * @param newStrokeOpacity the new value for the stroke opacity property.
     *         The input value is clamped to the [0, 1] range.
     */
    public void setStrokeOpacity(float newStrokeOpacity) {
        if (newStrokeOpacity < 0) {
            newStrokeOpacity = 0;
        } else if (newStrokeOpacity > 1) {
            newStrokeOpacity = 1;
        }
        
        pack &= ~STROKE_OPACITY_MASK;
        pack |= ((((int) (newStrokeOpacity * 200)) << 15) & STROKE_OPACITY_MASK);
    }

    /**
     * @return the context's stroke opacity property value
     */
    public float getStrokeOpacity() {
        return ((pack & STROKE_OPACITY_MASK) >> 15) / 200.0f;
    }

    /**
     * @return the context's stroke opacity property value
     */
    public int getStrokeOpacityImpl() {
        return (int) (((pack & STROKE_OPACITY_MASK) >> 15) * 255f / 200f);
    }

    /**
     * @param newColor the new value for the color property
     */
    public void setColor(final RGB newColor) {
        this.color = newColor;
    }

    /**
     * @return the context's color property value
     */
    public RGB getColor() {
        return this.color;
    }

    /**
     * @param newFillRule the new value for the fill rule property
     */
    public void setFillRule(final int newFillRule) {
        if (newFillRule == WIND_EVEN_ODD) {
            pack |= FILL_RULE_MASK;
        } else {
            pack &= ~FILL_RULE_MASK;
        }
    }

    /**
     * @return the context's fill rule property value
     */
    public int getFillRule() {
        if ((pack & FILL_RULE_MASK) == FILL_RULE_MASK) {
            return WIND_EVEN_ODD;
        }
        return WIND_NON_ZERO;
    }

    /**
     * @return the context's stroke dash array property value
     */
    public float[] getStrokeDashArray() {
        if (strokeDashArray == null) {
            return null;
        }

        float[] sda = new float[strokeDashArray.length];
        for (int i = 0; i < sda.length; i++) {
            sda[i] = strokeDashArray[i] / 65536.0f;
        }

        return sda;
    }

    /**
     * @param newStrokeDashArray the new stroke dash array property value
     */
    public void setStrokeDashArray(final float[] newStrokeDashArray) {
        if (newStrokeDashArray == null) {
            strokeDashArray = null;
        } else {
            strokeDashArray = new int[newStrokeDashArray.length];
            for (int i = 0; i < strokeDashArray.length; i++) {
                strokeDashArray[i] = (int) (newStrokeDashArray[i] * 65536);
            }
        }
    }

    /**
     * @return the context's stroke line cap property value
     */
    public int getStrokeLineCap() {
        switch (pack & STROKE_LINE_CAP_MASK) {
        case CAP_BUTT_IMPL:
            return CAP_BUTT;
        case CAP_ROUND_IMPL:
            return CAP_ROUND;
        default:
            return CAP_SQUARE;
        }
    }

    /**
     * @param newStrokeLineCap the new stroke line cap property value
     */
    public void setStrokeLineCap(final int newStrokeLineCap) {
        // Clear stroke-linecap
        pack &= ~STROKE_LINE_CAP_MASK;

        switch (newStrokeLineCap) {
        case CAP_BUTT:
            pack |= CAP_BUTT_IMPL;
            break;
        case CAP_ROUND:
            pack |= CAP_ROUND_IMPL;
            break;
        default:
            pack |= CAP_SQUARE_IMPL;
            break;
        }
    }

    /**
     * @return the context's stroke line join property value
     */
    public int getStrokeLineJoin() {
        switch (pack & STROKE_LINE_JOIN_MASK) {
        case JOIN_MITER_IMPL:
            return JOIN_MITER;
        case JOIN_ROUND_IMPL:
            return JOIN_ROUND;
        default:
            return JOIN_BEVEL;
        }
    }

    /**
     * @param newStrokeLineJoin the new stroke line join property value
     */
    public void setStrokeLineJoin(final int newStrokeLineJoin) {
        // Clear stroke-linejoin
        pack &= ~STROKE_LINE_JOIN_MASK;

        switch (newStrokeLineJoin) {
        case JOIN_MITER:
            pack |= JOIN_MITER_IMPL;
            break;
        case JOIN_ROUND:
            pack |= JOIN_ROUND_IMPL;
            break;
        default:
            pack |= JOIN_BEVEL_IMPL;
            break;
        }
    }

    /**
     * @return the context's stroke width property value
     */
    public float getStrokeWidth() {
        return strokeWidth / 65536.0f;
    }

    /**
     * @param newStrokeWidth the new stroke width property value
     */
    public void setStrokeWidth(final float newStrokeWidth) {
        strokeWidth = (int) (newStrokeWidth * 65536);
    }

    /**
     * @return the context's stroke miter limit property value
     */
    public float getStrokeMiterLimit() {
        return strokeMiterLimit / 65536.0f;
    }

    /**
     * @param newStrokeMiterLimit the new stroke miter limit property value
     */
    public void setStrokeMiterLimit(final float newStrokeMiterLimit) {
        strokeMiterLimit = (int) (newStrokeMiterLimit * 65536);
    }

    /**
     * @return the context's stroke dash offset property
     */
    public float getStrokeDashOffset() {
        return strokeDashOffset / 65536.0f;
    }

    /**
     * @param newStrokeDashOffset the new stroke dash offset property value
     */
    public void setStrokeDashOffset(final float newStrokeDashOffset) {
        strokeDashOffset = (int) (newStrokeDashOffset * 65536);
    }

    /**
     * @param newVisibility the new visibility property value
     */
    public void setVisibility(final boolean newVisibility) {
        if (newVisibility) {
            pack |= VISIBILITY_MASK;
        } else {
            pack &= ~VISIBILITY_MASK;
        }
    }

    /**
     * @return the context's visibility
     */
    public boolean getVisibility() {
        return ((pack & VISIBILITY_MASK) == VISIBILITY_MASK);
    }

    /**
     * @param newDisplay the new value for the display property
     */
    public void setDisplay(final boolean newDisplay) {
        if (newDisplay) {
            pack |= DISPLAY_MASK;
        } else {
            pack &= ~DISPLAY_MASK;
        }
    }

    /**
     * @return the context' display property value
     */
    public boolean getDisplay() {
        return ((pack & DISPLAY_MASK) == DISPLAY_MASK);
    }

    /**
     * @return the context's set of font families
     */
    public String[] getFontFamily() {
        return fontFamily;
    }

    /**
     * @param newFontFamily the new set of font families to use for font
     *        matching.
     */
    public void setFontFamily(final String[] newFontFamily) {
        this.fontFamily = newFontFamily;
    }

    /**
     * @return the context's font size property
     */
    public float getFontSize() {
        return fontSize;
    }

    /**
     * @param newFontSize the new value for the font size property
     */
    public void setFontSize(final float newFontSize) {
        this.fontSize = newFontSize;
    }

    /**
     * @return the context's font style property value
     */
    public int getFontStyle() {
        switch(pack & FONT_STYLE_MASK) {
        case FONT_STYLE_NORMAL_IMPL:
            return FONT_STYLE_NORMAL;
        case FONT_STYLE_ITALIC_IMPL:
            return FONT_STYLE_ITALIC;
        default:
            return FONT_STYLE_OBLIQUE;
        }
    }

    /**
     * @param newFontStyle the new value for the font style property
     */
    public void setFontStyle(final int newFontStyle) {
        // Clear font-style.
        pack &= ~FONT_STYLE_MASK;

        switch(newFontStyle) {
        case FONT_STYLE_NORMAL:
            pack |= FONT_STYLE_NORMAL_IMPL;
            break;
        case FONT_STYLE_ITALIC:
            pack |= FONT_STYLE_ITALIC_IMPL;
            break;
        default:
            pack |= FONT_STYLE_OBLIQUE_IMPL;
            break;
        }
    }

    /**
     * @return the current context's font weight property
     */
    public int getFontWeight() {
        switch(pack & FONT_WEIGHT_MASK) {
        case FONT_WEIGHT_100_IMPL:
            return FONT_WEIGHT_100;
        case FONT_WEIGHT_200_IMPL:
            return FONT_WEIGHT_200;
        case FONT_WEIGHT_300_IMPL:
            return FONT_WEIGHT_300;
        case FONT_WEIGHT_400_IMPL:
            return FONT_WEIGHT_400;
        case FONT_WEIGHT_500_IMPL:
            return FONT_WEIGHT_500;
        case FONT_WEIGHT_600_IMPL:
            return FONT_WEIGHT_600;
        case FONT_WEIGHT_700_IMPL:
            return FONT_WEIGHT_700;
        case FONT_WEIGHT_800_IMPL:
            return FONT_WEIGHT_800;
        default:
            return FONT_WEIGHT_900;
        }
    }

    /**
     * @param newFontWeight new font weight property value
     */
    public void setFontWeight(final int newFontWeight) {
        // Clear font-weight
        pack &= ~FONT_WEIGHT_MASK;

        switch(newFontWeight) {
        case FONT_WEIGHT_100:
            pack |= FONT_WEIGHT_100_IMPL;
            break;
        case FONT_WEIGHT_200:
            pack |= FONT_WEIGHT_200_IMPL;
            break;
        case FONT_WEIGHT_300:
            pack |= FONT_WEIGHT_300_IMPL;
            break;
        case FONT_WEIGHT_400:
            pack |= FONT_WEIGHT_400_IMPL;
            break;
        case FONT_WEIGHT_500:
            pack |= FONT_WEIGHT_500_IMPL;
            break;
        case FONT_WEIGHT_600:
            pack |= FONT_WEIGHT_600_IMPL;
            break;
        case FONT_WEIGHT_700:
            pack |= FONT_WEIGHT_700_IMPL;
            break;
        case FONT_WEIGHT_800:
            pack |= FONT_WEIGHT_800_IMPL;
            break;
        default:
            pack |= FONT_WEIGHT_900_IMPL;
            break;
        }
    }

    /**
     * @return the text anchor property
     */
    public int getTextAnchor() {
        switch(pack & TEXT_ANCHOR_MASK) {
        case TEXT_ANCHOR_START_IMPL:
            return TEXT_ANCHOR_START;
        case TEXT_ANCHOR_MIDDLE_IMPL:
            return TEXT_ANCHOR_MIDDLE;
        default:
            return TEXT_ANCHOR_END;
        }
    }

    /**
     * @param newTextAnchor the new text anchor property value
     */
    public void setTextAnchor(final int newTextAnchor) {
        // Clear text-anchor.
        pack &= ~TEXT_ANCHOR_MASK;

        switch(newTextAnchor) {
        case TEXT_ANCHOR_START:
            pack |= TEXT_ANCHOR_START_IMPL;
            break;
        case TEXT_ANCHOR_MIDDLE:
            pack |= TEXT_ANCHOR_MIDDLE_IMPL;
            break;
        default:
            pack |= TEXT_ANCHOR_END_IMPL;
            break;
        }
    }

    /**
     * @param newOpacity the new value for the opacity property. The 
     *        input value is clamped to the [0, 1] range.
     *
     * @param newOpacity the new opacity value to set
     */
    public void setOpacity(float newOpacity) {
        if (newOpacity < 0) {
            newOpacity = 0;
        } else if (newOpacity > 1) {
            newOpacity = 1;
        }
        
        pack2 &= ~OPACITY_MASK;
        pack2 |= (((int) (newOpacity * 200)) & OPACITY_MASK);
    }

    /**
     * @return the context's opacity property value
     */
    public float getOpacity() {
        return (pack2 & OPACITY_MASK) / 200.0f;
    }

    /**
     * @return the context's opacity property value
     */
    int getOpacityImpl() {
        return (int) ((pack2 & OPACITY_MASK) * 255f / 200f);
    }

    /**
     * @return a copy of this <tt>RenderContext</tt>
     */
    public RenderContext save() {
        return new RenderContext(this);
    }

    /**
     * Implemnetation: Handles negative strokeDashOffset
     *
     * @return a positive strokeDashOffset.
     */
    int computeStrokeDashOffset() {
        if (strokeDashArray == null) {
            // The stroke dash offset does not matter, simply return 0
            return 0;
        }

        if (strokeDashOffset >= 0) {
            return strokeDashOffset;
        }

        int length = 0;
        for (int i = 0; i < strokeDashArray.length; i++) {
            length += strokeDashArray[i];
        }

        if (length <= 0) {
            return 0;
        }

        int sdo = strokeDashOffset;
        while (sdo < 0) {
            sdo += length;
        }

        return sdo;
    }
}
