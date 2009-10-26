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

import org.w3c.dom.DOMException;

import org.w3c.dom.svg.SVGRect;
import org.w3c.dom.svg.SVGRGBColor;
import org.w3c.dom.svg.SVGLocatableElement;

import com.sun.perseus.j2d.GraphicsProperties;
import com.sun.perseus.j2d.Transform;

import com.sun.perseus.util.SVGConstants;

import com.sun.perseus.j2d.RGB;
import com.sun.perseus.j2d.PaintDef;
import com.sun.perseus.j2d.PaintTarget;
import com.sun.perseus.j2d.PaintServer;

/**
 * <code>CompositeGraphicsNode</code> is the base class for all nodes which are
 * both composites (i.e., they can have children) and have graphic properties
 * (such as <code>fill</code> or <code>stroke</code>).
 *
 * @version $Id: CompositeGraphicsNode.java,v 1.16 2006/06/29 10:47:29 ln156897 Exp $
 */
public abstract class CompositeGraphicsNode extends ElementNode 
        implements GraphicsNode, PaintTarget, SVGLocatableElement {
    // =======================================================================
    // Constants used for packing and unpacking data in the pack member
    // =======================================================================
    protected static final int FONT_STYLE_MASK =       
            0x60000000;    /* 30-29 */
    protected static final int FONT_WEIGHT_MASK =      
            0x1E000000;    /* 28-27-26-25 */
    protected static final int TEXT_ANCHOR_MASK =      
            0x01800000;    /* 24-23 */
    protected static final int STROKE_OPACITY_MASK =   
            0x007F8000;    /* 22-21-20-19-18-17-16-15 */
    protected static final int FILL_OPACITY_MASK =     
            0x00007F80;    /* 14-13-12-11-10-09-08-07 */
    protected static final int FILL_RULE_MASK =        
            0x00000040;    /* 06 */
    protected static final int STROKE_LINE_CAP_MASK =  
            0x00000030;    /* 05-04 */
    protected static final int STROKE_LINE_JOIN_MASK = 
            0x0000000C;    /* 03-02 */
    protected static final int VISIBILITY_MASK =       
            0x00000002;    /* 01 */
    protected static final int DISPLAY_MASK =         
            0x00000001;    /* 00 */

    // =======================================================================
    // Constants used for packing and unpacking data in the pack2 member
    // =======================================================================
    protected static final int OPACITY_MASK =   
            0x000000FF;    /* 7-6-5-4-3-2-1-0 */

    protected static final int FONT_STYLE_NORMAL_IMPL =  0x00000000;
    protected static final int FONT_STYLE_ITALIC_IMPL =  0x40000000;
    protected static final int FONT_STYLE_OBLIQUE_IMPL = 0x60000000;

    protected static final int FONT_WEIGHT_100_IMPL     = 0x00000000;
    protected static final int FONT_WEIGHT_200_IMPL     = 0x02000000;
    protected static final int FONT_WEIGHT_300_IMPL     = 0x04000000;
    protected static final int FONT_WEIGHT_400_IMPL     = 0x06000000;
    protected static final int FONT_WEIGHT_500_IMPL     = 0x08000000;
    protected static final int FONT_WEIGHT_600_IMPL     = 0x0A000000;
    protected static final int FONT_WEIGHT_700_IMPL     = 0x0C000000;
    protected static final int FONT_WEIGHT_800_IMPL     = 0x0E000000;
    protected static final int FONT_WEIGHT_900_IMPL     = 0x10000000;
    protected static final int FONT_WEIGHT_LIGHTER_IMPL = 0x12000000;
    protected static final int FONT_WEIGHT_BOLDER_IMPL  = 0x14000000;

    protected static final int TEXT_ANCHOR_MIDDLE_IMPL = 0x00000000;
    protected static final int TEXT_ANCHOR_START_IMPL  = 0x00800000;
    protected static final int TEXT_ANCHOR_END_IMPL =    0x01000000;

    protected static final int CAP_BUTT_IMPL =   0x00000000;
    protected static final int CAP_ROUND_IMPL =  0x00000010;
    protected static final int CAP_SQUARE_IMPL = 0x00000020;

    protected static final int JOIN_MITER_IMPL = 0x00000000;
    protected static final int JOIN_ROUND_IMPL = 0x00000004;
    protected static final int JOIN_BEVEL_IMPL = 0x00000008;

    // ====================================================================
    // Property values
    // ====================================================================
    /**
     * The current color. The fill and stroke colors may be
     * relative to the current color
     */
    protected RGB color = INITIAL_COLOR;

    /**
     * The fill paint used to fill this node
     */
    protected PaintServer fill = INITIAL_FILL;

    /**
     * The stroke paint used to stroke the outline of
     * this ShapeNode
     */
    protected PaintServer stroke = INITIAL_STROKE;

    /**
     * The stroke width
     */
    protected float strokeWidth = INITIAL_STROKE_WIDTH;

    /**
     * The stroke miter limit
     */
    protected float strokeMiterLimit = INITIAL_STROKE_MITER_LIMIT;

    /**
     * The stroke dash array
     */
    protected float[] strokeDashArray = INITIAL_STROKE_DASH_ARRAY;

    /**
     * The stroke dash offset
     */
    protected float strokeDashOffset = INITIAL_STROKE_DASH_OFFSET;

    // 
    // Property pack
    //
    // fontStyle:     3 styles,  2 bits
    // fontWeight:    9 weights, 4 bits
    // textAnchor     3 values,  2 bits
    // strokeOpacity:            8 bits [0-255]
    // fillOpacity:              8 bits [0-255]
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
    protected static final int INITIAL_TEXT_ANCHOR_IMPL = 
            TEXT_ANCHOR_START_IMPL;
    protected static final int INITIAL_STROKE_OPACITY_IMPL = 200 << 15;
    protected static final int INITIAL_FILL_OPACITY_IMPL = 200 << 7;
    protected static final int INITIAL_FILL_RULE_IMPL = FILL_RULE_MASK;
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

    // ====================================================================
    // Property value types (inherited, relative, none, specific)
    // ====================================================================

    /**
     * Markers are used to keep track of inherited properties, color relative
     * properties and bolder/lighter font weights.
     *
     * 0-20  : property inheritance
     * 21-22 : color relative
     * 23    : is bolder marker
     * 24    : is lighter marker
     */
    protected int markers = DEFAULT_INHERITANCE 
                            | TextNode.DEFAULT_INHERITANCE 
                            | DEFAULT_COLOR_RELATIVE;
    
    // ====================================================================
    // ModelNode Implementation
    // ====================================================================

    /**
     * Cached Transform. May point to the parent transform.
     */
    protected Transform txf = null;

    /**
     * Cached inverse transform. May point to the parent inverse transform.
     */
    protected Transform inverseTxf = null;

    /**
     * Constructor.
     *
     * @param ownerDocument this element's owner <code>DocumentNode</code>
     */
    public CompositeGraphicsNode(final DocumentNode ownerDocument) {
        super(ownerDocument);
        
        // By default, a CompositeGraphicsNode is renderable
        canRenderState &= CAN_RENDER_RENDERABLE_MASK;
    }

    /**
     * A <code>CompositeGraphicsNode</code> contributes to its parent bounding
     * box only if its display property is turned on.
     *
     * @return true if the node's bounding box should be accounted for.
     */
    protected boolean contributeBBox() {
        return (pack & DISPLAY_MASK) != 0;
    }

    // JAVADOC COMMENT ELIDED
    public SVGRect getBBox() {
        return null;
    }

    // JAVADOC COMMENT ELIDED
    public SVGRect getScreenBBox() {
        return null;
    }

    /**
     * Returns the value of the given Object-valued property.
     *
     * @return the value of the given Object-valued property.
     */
    protected Object getPropertyState(final int propertyIndex) {
        switch (propertyIndex) {
        case PROPERTY_FILL:
            return fill;
        case PROPERTY_STROKE:
            return stroke;
        case PROPERTY_COLOR:
            return color;
        case PROPERTY_STROKE_DASH_ARRAY:            
            return strokeDashArray;
        default: 
            return super.getPropertyState(propertyIndex);
        }
    }

    /**
     * Returns the value of the given float-valued property.
     *
     * @return the value of the given property.
     */
    protected float getFloatPropertyState(final int propertyIndex) {
        switch (propertyIndex) {
        case PROPERTY_STROKE_WIDTH:
            return strokeWidth;
        case PROPERTY_STROKE_MITER_LIMIT:
            return strokeMiterLimit;
        case PROPERTY_STROKE_DASH_OFFSET:
            return strokeDashOffset;
        default: 
            return super.getFloatPropertyState(propertyIndex);
        }
    }

    /**
     * Returns the value for the given packed property.
     *
     * @return the value for the given property.
     */
    protected int getPackedPropertyState(final int propertyIndex) {
        switch (propertyIndex) {
        case GraphicsNode.PROPERTY_FILL_RULE:
            return pack & CompositeGraphicsNode.FILL_RULE_MASK;
        case GraphicsNode.PROPERTY_STROKE_LINE_JOIN:
            return pack & CompositeGraphicsNode.STROKE_LINE_JOIN_MASK;
        case GraphicsNode.PROPERTY_STROKE_LINE_CAP:
            return pack & CompositeGraphicsNode.STROKE_LINE_CAP_MASK;
        case GraphicsNode.PROPERTY_DISPLAY:
            return pack & CompositeGraphicsNode.DISPLAY_MASK;
        case GraphicsNode.PROPERTY_VISIBILITY:
            return pack & CompositeGraphicsNode.VISIBILITY_MASK;
        case GraphicsNode.PROPERTY_FILL_OPACITY:
            return pack & CompositeGraphicsNode.FILL_OPACITY_MASK;
        case GraphicsNode.PROPERTY_STROKE_OPACITY:
            return pack & CompositeGraphicsNode.STROKE_OPACITY_MASK;
        case GraphicsNode.PROPERTY_OPACITY:
            return pack2 & CompositeGraphicsNode.OPACITY_MASK;
        default: 
            return super.getPackedPropertyState(propertyIndex);
        }
    }

    /**
     * Sets the computed value of the given Object-valued property.
     *
     * @param propertyIndex the property index
     * @param propertyValue the computed value of the property.
     */
    protected void setPropertyState(final int propertyIndex,
                                    final Object propertyValue) {
        switch (propertyIndex) {
        case PROPERTY_FILL:
            setComputedFill((PaintServer) propertyValue);
            break;
        case PROPERTY_STROKE:
            setComputedStroke((PaintServer) propertyValue);
            break;
        case PROPERTY_COLOR:
            setComputedColor((RGB) propertyValue);
            break;
        case PROPERTY_STROKE_DASH_ARRAY:            
            setComputedStrokeDashArray((float[]) propertyValue);
            break;
        default: 
            super.setPropertyState(propertyIndex, propertyValue);
            break;
        }
    }

    /**
     * Sets the computed value of the given float-valued property.
     *
     * @param propertyIndex the property index
     * @param propertyValue the computed value of the property.
     */
    protected void setFloatPropertyState(final int propertyIndex,
                                          final float propertyValue) {
        switch (propertyIndex) {
        case PROPERTY_STROKE_WIDTH:
            setComputedStrokeWidth(propertyValue);
            break;
        case PROPERTY_STROKE_MITER_LIMIT:
            setComputedStrokeMiterLimit(propertyValue);
            break;
        case PROPERTY_STROKE_DASH_OFFSET:
            setComputedStrokeDashOffset(propertyValue);
            break;
        default: 
            super.setFloatPropertyState(propertyIndex, propertyValue);
            break;
        }
    }

    /**
     * Sets the computed value of the given packed property.
     *
     * @param propertyIndex the property index
     * @param propertyValue the computed value of the property.
     */
    protected void setPackedPropertyState(final int propertyIndex,
                                          final int propertyValue) {
        switch (propertyIndex) {
        case GraphicsNode.PROPERTY_FILL_RULE:
            if (propertyValue == 0) {
                setComputedFillRule(WIND_EVEN_ODD);
            } else {
                setComputedFillRule(WIND_NON_ZERO);
            }
            break;
        case GraphicsNode.PROPERTY_STROKE_LINE_JOIN:
            switch (propertyValue) {
                case CompositeGraphicsNode.JOIN_MITER_IMPL:
                    setComputedStrokeLineJoin(JOIN_MITER);
                    break;
                case CompositeGraphicsNode.JOIN_ROUND_IMPL:
                    setComputedStrokeLineJoin(JOIN_ROUND);
                    break;
                case CompositeGraphicsNode.JOIN_BEVEL_IMPL:
                default:
                    setComputedStrokeLineJoin(JOIN_BEVEL);
                    break;
            }
            break;
        case GraphicsNode.PROPERTY_STROKE_LINE_CAP:
            switch (propertyValue) {
                case CompositeGraphicsNode.CAP_BUTT_IMPL:
                    setComputedStrokeLineCap(CAP_BUTT);
                    break;
                case CompositeGraphicsNode.CAP_ROUND_IMPL:
                    setComputedStrokeLineCap(CAP_ROUND);
                    break;
                case CompositeGraphicsNode.CAP_SQUARE_IMPL:
                default:
                    setComputedStrokeLineCap(CAP_SQUARE);
                    break;
            }
            break;
        case GraphicsNode.PROPERTY_DISPLAY:
            if (propertyValue != 0) {
                setComputedDisplay(true);
            } else {
                setComputedDisplay(false);
            }
            break;
        case GraphicsNode.PROPERTY_VISIBILITY:
            if (propertyValue != 0) {
                setComputedVisibility(true);
            } else {
                setComputedVisibility(false);
            }
            break;
        case GraphicsNode.PROPERTY_FILL_OPACITY:
            setComputedFillOpacity((propertyValue >> 7) / 200.0f);
            break;
        case GraphicsNode.PROPERTY_STROKE_OPACITY:
            setComputedStrokeOpacity((propertyValue >> 15) / 200.0f);
            break;
        case GraphicsNode.PROPERTY_OPACITY:
            setComputedOpacity(propertyValue / 200.0f);
            break;
        default: 
            super.setPackedPropertyState(propertyIndex, propertyValue);
            break;
        }
    }

    /**
     * Checks the state of the Object-valued property.
     *
     * @param propertyIndex the property index
     * @param propertyValue the computed value of the property.
     */
    protected boolean isPropertyState(final int propertyIndex,
                                      final Object propertyValue) {
        switch (propertyIndex) {
        case GraphicsNode.PROPERTY_FILL:
            return fill == propertyValue;
        case GraphicsNode.PROPERTY_STROKE:
            return stroke == propertyValue;
        case GraphicsNode.PROPERTY_COLOR:
            return color == propertyValue;
        case GraphicsNode.PROPERTY_STROKE_DASH_ARRAY:            
            return strokeDashArray == propertyValue;
        default: 
            return super.isPropertyState(propertyIndex, propertyValue);
        }
    }
    
    /**
     * Checks the state of the float property value.
     *
     * @param propertyIndex the property index
     * @param propertyValue the computed value of the property.
     */
    protected boolean isFloatPropertyState(final int propertyIndex,
                                           final float propertyValue) {
        switch (propertyIndex) {
        case GraphicsNode.PROPERTY_STROKE_WIDTH:
            return strokeWidth == propertyValue;
        case GraphicsNode.PROPERTY_STROKE_MITER_LIMIT:
            return strokeMiterLimit == propertyValue;
        case GraphicsNode.PROPERTY_STROKE_DASH_OFFSET:
            return strokeDashOffset == propertyValue;
        default: 
            return super.isFloatPropertyState(propertyIndex, propertyValue);
        }
    }
    
    /**
     * Checks the state of the packed property value.
     *
     * @param propertyIndex the property index
     * @param propertyValue the computed value of the property.
     */
    protected boolean isPackedPropertyState(final int propertyIndex,
                                            final int propertyValue) {
        switch (propertyIndex) {
        case GraphicsNode.PROPERTY_FILL_RULE:
            return (propertyValue
                    ==
                    (pack & CompositeGraphicsNode.FILL_RULE_MASK));
        case GraphicsNode.PROPERTY_STROKE_LINE_JOIN:
            return (propertyValue
                    ==
                    (pack & CompositeGraphicsNode.STROKE_LINE_JOIN_MASK));
        case GraphicsNode.PROPERTY_STROKE_LINE_CAP:
            return (propertyValue
                    ==
                    (pack & CompositeGraphicsNode.STROKE_LINE_CAP_MASK));
        case GraphicsNode.PROPERTY_DISPLAY:
            return (propertyValue
                    ==
                    (pack & CompositeGraphicsNode.DISPLAY_MASK));
        case GraphicsNode.PROPERTY_VISIBILITY:
            return (propertyValue
                    ==
                    (pack & CompositeGraphicsNode.VISIBILITY_MASK));
        case GraphicsNode.PROPERTY_FILL_OPACITY:
            return (propertyValue
                    ==
                    (pack & CompositeGraphicsNode.FILL_OPACITY_MASK));
        case GraphicsNode.PROPERTY_STROKE_OPACITY:
            return (propertyValue
                    ==
                    (pack & CompositeGraphicsNode.STROKE_OPACITY_MASK));
        case GraphicsNode.PROPERTY_OPACITY:
            return (propertyValue
                    ==
                    (pack2 & CompositeGraphicsNode.OPACITY_MASK));
        default: 
            return super.isPackedPropertyState(propertyIndex, propertyValue);
        }
    }

    /**
     * Recomputes all inherited properties.
     */
    void recomputeInheritedProperties() {
        ModelNode p = ownerDocument;
        if (parent != null) {
            p = parent;
        }
        recomputePropertyState(PROPERTY_FILL, 
                               p.getPropertyState(PROPERTY_FILL));
        recomputePropertyState(PROPERTY_STROKE, 
                               p.getPropertyState(PROPERTY_STROKE));
        recomputePropertyState(PROPERTY_COLOR, 
                               p.getPropertyState(PROPERTY_COLOR));
        recomputePackedPropertyState(PROPERTY_FILL_RULE, 
                               p.getPackedPropertyState(PROPERTY_FILL_RULE));
        recomputeFloatPropertyState(PROPERTY_STROKE_WIDTH,
                               p.getFloatPropertyState(PROPERTY_STROKE_WIDTH));
        recomputePackedPropertyState(PROPERTY_STROKE_LINE_JOIN, 
                               p.getPackedPropertyState(
                                    PROPERTY_STROKE_LINE_JOIN));
        recomputePackedPropertyState(PROPERTY_STROKE_LINE_CAP,
                               p.getPackedPropertyState(
                                    PROPERTY_STROKE_LINE_CAP));
        recomputeFloatPropertyState(PROPERTY_STROKE_MITER_LIMIT,
                               p.getFloatPropertyState(
                                    PROPERTY_STROKE_MITER_LIMIT));
        recomputePropertyState(PROPERTY_STROKE_DASH_ARRAY,
                               p.getPropertyState(PROPERTY_STROKE_DASH_ARRAY));
        recomputeFloatPropertyState(PROPERTY_STROKE_DASH_OFFSET, 
                               p.getFloatPropertyState(
                                    PROPERTY_STROKE_DASH_OFFSET));
        recomputePackedPropertyState(PROPERTY_DISPLAY, 
                               p.getPackedPropertyState(PROPERTY_DISPLAY));
        recomputePackedPropertyState(PROPERTY_VISIBILITY, 
                               p.getPackedPropertyState(PROPERTY_VISIBILITY));
        recomputePackedPropertyState(PROPERTY_FILL_OPACITY, 
                               p.getPackedPropertyState(PROPERTY_FILL_OPACITY));
        recomputePackedPropertyState(PROPERTY_STROKE_OPACITY, 
                               p.getPackedPropertyState(
                                     PROPERTY_STROKE_OPACITY));
        recomputePackedPropertyState(PROPERTY_OPACITY, 
                               p.getPackedPropertyState(
                                     PROPERTY_OPACITY));
    }

    /**
     * Recomputes the given Object-valued property's state given the
     * new parent property.
     *
     * @param propertyIndex index for the property whose value is changing.
     * @param parentPropertyValue the value that children of this node should 
     *        now inherit.
     * 
     */
    protected void recomputePropertyState(final int propertyIndex,
                                          final Object parentPropertyValue) {
        // We do not need to recompute the property value if:
        // - the property is _not_ inherited
        // or
        // - the property is inherited by the new parent property computed value
        //   is the same as the current value.
        if (!isInherited(propertyIndex) 
            || 
            isPropertyState(propertyIndex, parentPropertyValue)) {
            // If the property is color relative, the propagation happens
            // through the color property changes.  This means that with
            // currentColor, we inherit the computed value, not the specified
            // currentColor indirection.
            return;
        }

        setPropertyState(propertyIndex, parentPropertyValue);
        propagatePropertyState(propertyIndex, parentPropertyValue);
    }

    /**
     * Recomputes the given float-valued property's state given the new parent 
     * property.
     *
     * @param propertyIndex index for the property whose value is changing.
     * @param parentPropertyValue the value that children of this node should 
     *        now inherit.
     * 
     */
    protected void recomputeFloatPropertyState(final int propertyIndex,
                                          final float parentPropertyValue) {
        // We do not need to recompute the property value if:
        // - the property is _not_ inherited
        // or
        // - the property is inherited by the new parent property computed value
        //   is the same as the current value.
        if (!isInherited(propertyIndex) 
            || 
            isFloatPropertyState(propertyIndex, parentPropertyValue)) {
            // If the property is color relative, the propagation happens
            // through the color property changes.  This means that with
            // currentColor, we inherit the computed value, not the specified
            // currentColor indirection.
            return;
        }

        setFloatPropertyState(propertyIndex, parentPropertyValue);
        propagateFloatPropertyState(propertyIndex, parentPropertyValue);
    }

    /**
     * Recomputes the given packed property's state given the new parent 
     * property.
     *
     * @param propertyIndex index for the property whose value is changing.
     * @param parentPropertyValue the value that children of this node should 
     *        now inherit.
     * 
     */
    protected void recomputePackedPropertyState(final int propertyIndex,
                                                final int parentPropertyValue) {
        // We do not need to recompute the property value if:
        // - the property is _not_ inherited
        // or
        // - the property is inherited by the new parent property computed value
        //   is the same as the current value.
        if (!isInherited(propertyIndex) 
            || 
            isPackedPropertyState(propertyIndex, parentPropertyValue)) {
            // If the property is color relative, the propagation happens
            // through the color property changes.  This means that with
            // currentColor, we inherit the computed value, not the specified
            // currentColor indirection.
            return;
        }

        setPackedPropertyState(propertyIndex, parentPropertyValue);
        propagatePackedPropertyState(propertyIndex, parentPropertyValue);
    }
    
    /**
     * Called when the computed value of the given Object-valued property
     * has changed.
     *
     * @param propertyIndex index for the property whose value has changed.
     * @param parentPropertyValue the value that children of this node should 
     *        now inherit.
     * 
     */
    protected void propagatePropertyState(final int propertyIndex,
                                          final Object parentPropertyValue) {
        // Propagate to proxies.
        if (firstProxy != null) {
            ElementNodeProxy proxy = firstProxy;
            while (proxy != null) {
                ((CompositeGraphicsNodeProxy) proxy).proxiedPropertyStateChange(
                        propertyIndex, parentPropertyValue);
                proxy = proxy.nextProxy;
            }
        }

        // Propagate to regular children.
        ModelNode node = getFirstChildNode();
        while (node != null) {
            node.recomputePropertyState(propertyIndex, parentPropertyValue);
            node = node.nextSibling;
        }
    }

    /**
     * Called when the computed value of the given float-valued property has
     * changed.
     *
     * @param propertyIndex index for the property whose value has changed.
     * @param parentPropertyValue the value that children of this node should 
     *        now inherit.
     * 
     */
    protected void propagateFloatPropertyState(
            final int propertyIndex,
            final float parentPropertyValue) {
        // Propagate to proxies.
        if (firstProxy != null) {
            ElementNodeProxy proxy = firstProxy;
            while (proxy != null) {
                ((CompositeGraphicsNodeProxy) 
                        proxy).proxiedFloatPropertyStateChange(
                            propertyIndex, parentPropertyValue);
                proxy = proxy.nextProxy;
            }
        }

        // Propagate to regular children.
        ModelNode node = getFirstChildNode();
        while (node != null) {
            node.recomputeFloatPropertyState(propertyIndex, 
                                             parentPropertyValue);
            node = node.nextSibling;
        }
    }

    /**
     * Called when the computed value of the given packed property has changed.
     *
     * @param propertyIndex index for the property whose value has changed.
     * @param parentPropertyValue the value that children of this node should 
     *        now inherit.
     * 
     */
    protected void propagatePackedPropertyState(final int propertyIndex,
                                                final int parentPropertyValue) {
        // Propagate to proxies.
        if (firstProxy != null) {
            ElementNodeProxy proxy = firstProxy;
            while (proxy != null) {
                ((CompositeGraphicsNodeProxy) 
                        proxy).proxiedPackedPropertyStateChange(
                            propertyIndex, parentPropertyValue);
                proxy = proxy.nextProxy;
            }
        }

        // Propagate to regular children.
        ModelNode node = getFirstChildNode();
        while (node != null) {
            node.recomputePackedPropertyState(propertyIndex, 
                                              parentPropertyValue);
            node = node.nextSibling;
        }
    }

    /**
     * Recomputes the transform cache, if one exists. This should recursively
     * call recomputeTransformState on children node or expanded content, if
     * any.
     *
     * By default, because a ModelNode has no transform and no cached transform,
     * this only does a pass down.
     *
     * @param parentTransform the Transform applied to this node's parent.
     */
    protected void recomputeTransformState(final Transform parentTransform) {
        txf = appendTransform(parentTransform, txf);
        computeCanRenderTransformBit(txf);
        inverseTxf = null;
        // inverseTxf = computeInverseTransform(txf, parentTransform, 
        //                                      inverseTxf);
        recomputeTransformState(txf, getFirstChildNode());
    }

    /**
     * @return this node's cached transform. 
     */
    public Transform getTransformState() {
        return txf;
    }

    /**
     * @return this node's cached inverse transform. 
     */
    Transform getInverseTransformState() {
        if (((canRenderState & CAN_RENDER_NON_INVERTIBLE_TXF_BIT) == 0)) {
            if (inverseTxf == null) {
                // If there is a parent, check if this node's transform is the 
                // same as the parent's in which cahse
                if (parent != null && txf == parent.getTransformState()) {
                    inverseTxf = parent.getInverseTransformState();
                } else {
                    inverseTxf = new Transform(null);
                    try {
                        inverseTxf = (Transform) txf.inverse(inverseTxf);
                    } catch (Exception e) {
                        // If we get an exception, then we have a real error
                        // condition, because we just checked that the 
                        // transform was invertible.
                        throw new Error();
                    }
                }
            }
        } else {
            inverseTxf = null;
        }
        return inverseTxf;
    }
    
    /** 
     * Check if the property is inherited.
     *
     * @param propertyIndex the index of the property for which the 
     *        inherited state should be returned.
     * @return true if the input property is inherited. False
     * otherwise
     */
    public final boolean isInherited(final int propertyIndex) {
        return isMarkerSet(propertyIndex);
    }

    /**
     * Sets the given Object-valued property's inheritance status
     * @param propertyIndex the index for the property whose inherited state 
     *        is set
     * @param inherit the new property's state
     */
    public void setInherited(final int propertyIndex, 
                             final boolean inherit) {
        if (isInherited(propertyIndex) == inherit) {
            return;
        }
        modifyingNode();
        setInheritedQuiet(propertyIndex, inherit);

        if (inherit) {
            // The property is now inherited. We store the inherited
            // value on the node, which means we keep the computed value
            // on the node.
            Object inheritedValue = getInheritedPropertyState(propertyIndex);
            setPropertyState(propertyIndex, inheritedValue);

            // Notify children that the inherited value has changed.
            propagatePropertyState(propertyIndex, inheritedValue);
        }

        // If the value is not inherited, it means that we are in the middle of
        // specifying a value on the node. So we do not notify descendants, 
        // because this is done in the corresponding methods, e.g., setFill.

        modifiedNode();
    }

    /**
     * Sets the given float-valued property's inheritance status
     * @param propertyIndex the index for the property whose inherited state 
     *        is set
     * @param inherit the new property's state
     */
    public void setFloatInherited(final int propertyIndex, 
				  final boolean inherit) {
        if (isInherited(propertyIndex) == inherit) {
            return;
        }
        modifyingNode();
        setInheritedQuiet(propertyIndex, inherit);

        if (inherit) {
            // The property is now inherited. We store the inherited
            // value on the node, which means we keep the computed value
            // on the node.
            float inheritedValue = 
                    getInheritedFloatPropertyState(propertyIndex);
            setFloatPropertyState(propertyIndex, inheritedValue);

            // Notify children that the inherited value has changed.
            propagateFloatPropertyState(propertyIndex, inheritedValue);
        }

        // If the value is not inherited, it means that we are in the middle of
        // specifying a value on the node. So we do not notify descendants, 
        // because this is done in the corresponding methods, e.g., setFill.

        modifiedNode();
    }

    /**
     * Sets the input packed property's inheritance status
     * @param propertyIndex the index for the property whose inherited state 
     *        is set
     * @param inherit the new property's state
     */
    public void setPackedInherited(final int propertyIndex, 
                                   final boolean inherit) {
        if (isInherited(propertyIndex) == inherit) {
            return;
        }
        modifyingNode();
        setInheritedQuiet(propertyIndex, inherit);

        if (inherit) {
            // The property is now inherited. We store the inherited
            // value on the node, which means we keep the computed value
            // on the node.
            int inheritedValue = getInheritedPackedPropertyState(propertyIndex);
            setPackedPropertyState(propertyIndex, inheritedValue);

            // Notify children that the inherited value has changed.
            propagatePackedPropertyState(propertyIndex, inheritedValue);
        }

        // If the value is not inherited, it means that we are in the middle of
        // specifying a value on the node. So we do not notify descendants, 
        // because this is done in the corresponding methods, e.g., setFill.

        modifiedNode();
    }

    /**
     * Implementation. Sets the input property's inheritance status,
     * but does not send modification events.
     *
     * @param propertyIndex the index for the property whose inherited state 
     *        is set
     * @param inherit the new property's state
     */
    protected void setInheritedQuiet(final int propertyIndex, 
                                     final boolean inherit) {
        if (inherit) {
            setMarker(propertyIndex);
        } else {
            clearMarker(propertyIndex);
        }
    }

    /**
     * Returns true if the input property is color relative. False
     * otherwise
     * @param propertyIndex index for the property whose color relative
     *        state should be returned.
     * @return true if the property at index propertyIndex is relative to 
     *         the color property.
     */
    public boolean isColorRelative(final int propertyIndex) {
        return isMarkerSet(propertyIndex << 21);
    }
    
    /**
     * Returns true if the input property can be color-relative.
     * 
     * @param propertyIndex the index of the property which may be 
     *        color-relative.
     * @return true if the input property can be color relative. False 
     *         otherwise.
     */
    public boolean isColorRelativeProperty(final int propertyIndex) {
        switch (propertyIndex) {
            case PROPERTY_FILL:
                return true;
            case PROPERTY_STROKE:
                return true;
            default:
                return false;
        }
    }

    /**
     * Sets the input property as a color-relative property
     *
     * @param propertyIndex index of the property for which the color relative
     *        state is set.
     * @param isColorRelative the new color relative state for the property
     *        at propertyIndex.
     */
    public void setColorRelative(final int propertyIndex, 
                                 final boolean isColorRelative) {
        if (isColorRelative && !isColorRelativeProperty(propertyIndex)) {
            throw new IllegalArgumentException();
        }
        
        if (isColorRelative(propertyIndex) == isColorRelative) {
            return;
        }

        modifyingNode();
        setColorRelativeQuiet(propertyIndex,
                              isColorRelative);

        if (isColorRelative) {
            // The property is now color relative. We store the relative
            // value in the on the node, i.e., the computed value.
            setPropertyState(propertyIndex, color);

            // Notify children that the inherited value has changed.
            propagatePropertyState(propertyIndex, color);
        }

        modifiedNode();
    }
    
    /**
     * Sets the input marker.
     *
     * @param marker the marker to set.
     */
    void setMarker(final int marker) {
        markers |= marker;
    }
    
    /**
     * Clears the input marker.
     *
     * @param marker the marker to clear.
     */
    void clearMarker(final int marker) {
        markers &= ~marker;
    }
    
    /**
     * @return true if the input marker is set.
     */
    final boolean isMarkerSet(final int marker) {
        return (markers & marker) != 0;
    }

    /**
     * Implementation. Sets the input property as a color-relative property
     * but does not generate modification events.
     *
     * @param propertyIndex index of the property for which the color relative
     *        state is set.
     * @param isColorRelative the new color relative state for the property
     *        at propertyIndex.
     */
    protected void setColorRelativeQuiet(final int propertyIndex, 
                                         final boolean isColorRelative) {
        if (isColorRelative) {
            setMarker(propertyIndex << 21);
        } else {
            clearMarker(propertyIndex << 21);
        }
    }

    // ====================================================================
    // GraphicsNode implementation
    // ====================================================================

    /**
     * Setting the fill property clears the inherited and color relative
     * states (they are set to false).
     *
     * @param newFill the new fill property
     */
    public void setFill(final PaintServer newFill) {
        if (!isInherited(PROPERTY_FILL) && equal(newFill, fill)) {
            return;
        }

        modifyingNode();
        if (fill != null) {
            fill.dispose();
        }
        
        setComputedFill(newFill);
        setInheritedQuiet(PROPERTY_FILL, false);
        setColorRelativeQuiet(PROPERTY_FILL, false);
        propagatePropertyState(PROPERTY_FILL, fill);
        modifiedNode();
    }

    /**
     * @param newFill the new computed fill property.
     */
    void setComputedFill(final PaintServer newFill) {
        this.fill = newFill;
    }

    /**
     * @return the current fill property. This is not the computed values,
     *         i.e., it does not account for the inherited or color
     *         relative states.
     */
    public PaintServer getFill() {
        return fill;
    }

    /**
     * Setting the fillOpacity property clears the inherited and color relative
     * states (they are set to false).
     *
     * @param newFillOpacity the new fill property
     */
    public void setFillOpacity(float newFillOpacity) {
        if (!isInherited(PROPERTY_FILL_OPACITY) 
            && 
            newFillOpacity == getFillOpacity()) {
            return;
        }
        modifyingNode();
        if (newFillOpacity > 1) {
            newFillOpacity = 1;
        } else if (newFillOpacity < 0) {
            newFillOpacity = 0;
        }
        setInheritedQuiet(PROPERTY_FILL_OPACITY, false);
        setComputedFillOpacity(newFillOpacity);
        propagatePackedPropertyState(PROPERTY_FILL_OPACITY, 
                                     pack & FILL_OPACITY_MASK);
        modifiedNode();
    }

    /**
     * @param newFillOpacity the new computed value of the fill opacity 
     *        property.
     */
    void setComputedFillOpacity(final float newFillOpacity) {                
        pack &= ~FILL_OPACITY_MASK;
        pack |= ((((int) (newFillOpacity * 200)) << 7) & FILL_OPACITY_MASK);
    }

    /**
     * @return the current fillOpacity property. 
     */
    public float getFillOpacity() {
        return ((pack & FILL_OPACITY_MASK) >> 7) / 200.0f;
    }

    /**
     * Setting the stroke clears the inherited and color relative states
     * (i.e., they are set to false).
     *
     * @param newStroke new stroke property. 
     */
    public void setStroke(final PaintServer newStroke) {
        if (!isInherited(PROPERTY_STROKE) && equal(newStroke, stroke)) {
            return;
        }

        modifyingNode();
        if (newStroke != stroke) {
            if (stroke != null) {
                stroke.dispose();
            }
        }
        setInheritedQuiet(PROPERTY_STROKE, false);
        setColorRelativeQuiet(PROPERTY_STROKE, false);
        setComputedStroke(newStroke);
        propagatePropertyState(PROPERTY_STROKE, stroke);
        modifiedNode();
    }

    /**
     * @param newStroke the new computed stroke property.
     */
    void setComputedStroke(final PaintServer newStroke) {
        this.stroke = newStroke;
    }

    /**
     * @return the stroke property. This is not the computed value as
     *         it does not account for the inherited and color relative
     *         states.
     */
    public PaintServer getStroke() {
        return stroke;
    }

    /**
     * Setting the strokeOpacity property clears the inherited and color 
     * relative states (they are set to false).
     *
     * @param newStrokeOpacity the new stroke property
     */
    public void setStrokeOpacity(float newStrokeOpacity) {
        if (!isInherited(PROPERTY_STROKE_OPACITY) && 
            newStrokeOpacity == getStrokeOpacity()) {
            return;
        }

        modifyingNode();
        if (newStrokeOpacity > 1) {
            newStrokeOpacity = 1;
        } else if (newStrokeOpacity < 0) {
            newStrokeOpacity = 0;
        }
        setInheritedQuiet(PROPERTY_STROKE_OPACITY, false);
        setComputedStrokeOpacity(newStrokeOpacity);
        propagatePackedPropertyState(PROPERTY_STROKE_OPACITY, 
                                     pack & STROKE_OPACITY_MASK);
        modifiedNode();
    }

    /**
     * @param newStrokeOpacity the new computed stroke-opacity property.
     */
    void setComputedStrokeOpacity(final float newStrokeOpacity) {
        pack &= ~STROKE_OPACITY_MASK;
        pack |= ((((int) (newStrokeOpacity * 200)) << 15) 
                & STROKE_OPACITY_MASK);
    }

    /**
     * @return the current strokeOpacity property. 
     */
    public float getStrokeOpacity() {
        return ((pack & STROKE_OPACITY_MASK) >> 15) / 200.0f;
    }

    /**
     * Setting the color property clears this property's inherited flag.
     * @param newColor new color property.
     */
    public void setColor(final RGB newColor) {
        if (!isInherited(PROPERTY_COLOR) && equal(newColor, color)) {
            return;
        }
        modifyingNode();
        setComputedColor(newColor);
        setInheritedQuiet(PROPERTY_COLOR, false);
        propagatePropertyState(PROPERTY_COLOR, color);
        modifiedNode();
    }

    /**
     * @param newColor the new computed color property.
     */
    void setComputedColor(final RGB newColor) {
        color = newColor;

        // We need to recompute the fill and stroke colors if they are 
        // color-relative.
        if (isColorRelative(PROPERTY_FILL)) {
            setComputedFill(newColor);
            propagatePropertyState(PROPERTY_FILL, fill);
        }

        if (isColorRelative(PROPERTY_STROKE)) {
            setComputedStroke(newColor);
            propagatePropertyState(PROPERTY_STROKE, stroke);
        }
    }

    /**
     * @return the current color property. This is not the computed
     *         value and does not account for the inherited state.
     */
    public RGB getColor() {
        return color;
    }

    /**
     * Setting the fillRule property clears its inherited flag
     * @param newFillRule new fillRule property
     */
    public void setFillRule(final int newFillRule) {
        if (!isInherited(PROPERTY_FILL_RULE) 
            && 
            newFillRule == getFillRule()) {
            return;
        }

        modifyingNode();
        setInheritedQuiet(PROPERTY_FILL_RULE, false);
        setComputedFillRule(newFillRule);
        propagatePackedPropertyState(PROPERTY_FILL_RULE, pack & FILL_RULE_MASK);
        modifiedNode();
    }

    /**
     * @param newFillRule the new computed fillRule property value.
     */
    final void setComputedFillRule(final int newFillRule) {
        if (newFillRule == WIND_NON_ZERO) {
            pack |= FILL_RULE_MASK;
        } else {
            pack &= ~FILL_RULE_MASK;
        }
    }

    /**
     * @return the current fillRule property, exclusive of
     *         the inherited flag.
     */
    public int getFillRule() {
        if ((pack & FILL_RULE_MASK) == FILL_RULE_MASK) {
            return WIND_NON_ZERO;
        }
        return WIND_EVEN_ODD;
    }

    /**
     * Setting the strokeDashArray property clears its inherited flag.
     *
     * @param newStrokeDashArray new strokeDashArray property.
     */
    public void setStrokeDashArray(final float[] newStrokeDashArray) {
        if (!isInherited(PROPERTY_STROKE_DASH_ARRAY) 
                && equal(newStrokeDashArray, strokeDashArray)) {
            return;
        }
        modifyingNode();
        setComputedStrokeDashArray(newStrokeDashArray);
        setInheritedQuiet(PROPERTY_STROKE_DASH_ARRAY, false);
        propagatePropertyState(PROPERTY_STROKE_DASH_ARRAY, newStrokeDashArray);
        modifiedNode();
    }

    /**
     * @param newStrokeDashArray the new computed stroke-dasharray property 
     *        value.
     */
    void setComputedStrokeDashArray(final float[] newStrokeDashArray) {
        strokeDashArray = newStrokeDashArray;
    }

    /**
     * @return current strokeDashArray, exclusive of the inherited 
     *         state.
     */
    public float[] getStrokeDashArray() {
        return strokeDashArray;
    }

    /**
     * Setting the strokeLineCap property clears the inherited flag
     *
     * @param newStrokeLineCap new strokeLineCap property
     */
    public void setStrokeLineCap(final int newStrokeLineCap) {
        if (!isInherited(PROPERTY_STROKE_LINE_CAP) 
            && newStrokeLineCap == getStrokeLineCap()) {
            return;
        }
        modifyingNode();        
        setInheritedQuiet(PROPERTY_STROKE_LINE_CAP, false);
        setComputedStrokeLineCap(newStrokeLineCap);
        propagatePackedPropertyState(PROPERTY_STROKE_LINE_CAP, 
                                     pack & STROKE_LINE_CAP_MASK);
        modifiedNode();
    }

    /**
     * @param newStrokeLineCap the new value for the stroke-linecap property.
     */
    void setComputedStrokeLineCap(final int newStrokeLineCap) {
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
     * @return the strokeLineCap property exclusive of its inherited state
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
     * Setting the strokeLineJoin property clears the inherited flag
     *
     * @param newStrokeLineJoin new strokeLineJoin property
     */
    public void setStrokeLineJoin(final int newStrokeLineJoin) {
        if (!isInherited(PROPERTY_STROKE_LINE_JOIN) 
            && newStrokeLineJoin == getStrokeLineJoin()) {
            return;
        }
        modifyingNode();
        setInheritedQuiet(PROPERTY_STROKE_LINE_JOIN, false);
        setComputedStrokeLineJoin(newStrokeLineJoin);
        propagatePackedPropertyState(PROPERTY_STROKE_LINE_JOIN, 
                                     pack & STROKE_LINE_JOIN_MASK);
        modifiedNode();
    }

    /**
     * @param newStrokeLineJoin the new computed value of stroke-line-join
     */
    void setComputedStrokeLineJoin(final int newStrokeLineJoin) {
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
     * @return current strokeLineJoin exclusive of the inherited state
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
     * Setting the strokeWidth property clears its inherited flag.
     *
     * @param newStrokeWidth new strokeWidth property. Should be 
     *        positive or zero.
     */
    public void setStrokeWidth(final float newStrokeWidth) {
        if (newStrokeWidth < 0) {
            throw new IllegalArgumentException();
        }

        if (!isInherited(PROPERTY_STROKE_WIDTH) 
                && newStrokeWidth == strokeWidth) {
            return;
        }

        modifyingNode();
        setInheritedQuiet(PROPERTY_STROKE_WIDTH, false);
        setComputedStrokeWidth(newStrokeWidth);
        propagateFloatPropertyState(PROPERTY_STROKE_WIDTH, newStrokeWidth);
        modifiedNode();
    }

    /**
     * @param newStrokeWidth the new computed stroke-width property value.
     */
    void setComputedStrokeWidth(final float newStrokeWidth) {
        strokeWidth = newStrokeWidth;
    }

    /**
     * @return current strokeWidth, exclusive of inheritance
     */
    public float getStrokeWidth() {
        return strokeWidth;
    }

    /**
     * Setting the strokeMiterLimit clears its inherited flag.
     *
     * @param newStrokeMiterLimit new strokeMiterLimit property
     */
    public void setStrokeMiterLimit(final float newStrokeMiterLimit) {
        if (newStrokeMiterLimit < 1) {
            throw new IllegalArgumentException();
        }

        if (!isInherited(PROPERTY_STROKE_MITER_LIMIT) 
                && newStrokeMiterLimit == strokeMiterLimit) {
            return;
        }

        modifyingNode();
        setComputedStrokeMiterLimit(newStrokeMiterLimit);
        setInheritedQuiet(PROPERTY_STROKE_MITER_LIMIT, false);
        propagateFloatPropertyState(PROPERTY_STROKE_MITER_LIMIT, 
                                    strokeMiterLimit);
        modifiedNode();
    }

    /**
     * @param newStrokeMiterLimit the new computed stroke-miterlimit property.
     */
    void setComputedStrokeMiterLimit(final float newStrokeMiterLimit) {
        strokeMiterLimit = newStrokeMiterLimit;        
    }

    /**
     * @return current strokeMiterLimit, exclusive of the inherited state
     */
    public float getStrokeMiterLimit() {
        return strokeMiterLimit;
    }

    /**
     * Setting the strokeDashOffset property clears its inherited flag.
     *
     * @param newStrokeDashOffset new strokeDashOffset value
     */
    public void setStrokeDashOffset(final float newStrokeDashOffset) {
        if (!isInherited(PROPERTY_STROKE_DASH_OFFSET) 
                && newStrokeDashOffset == strokeDashOffset) {
            return;
        }

        modifyingNode();
        setComputedStrokeDashOffset(newStrokeDashOffset);
        setInheritedQuiet(PROPERTY_STROKE_DASH_OFFSET, false);
        propagateFloatPropertyState(PROPERTY_STROKE_DASH_OFFSET, 
                                    strokeDashOffset);
        modifiedNode();
    }

    /**
     * @param newStrokeDashOffset the new stroke-dashoffset computed property 
     *        value.
     */
    void setComputedStrokeDashOffset(final float newStrokeDashOffset) {
        strokeDashOffset = newStrokeDashOffset;
    }

    /**
     * @return current strokeDashOffset property value, exclusive of its
     *         inherited state.
     */
    public float getStrokeDashOffset() {
        return strokeDashOffset;
    }

    /**
     * Setting the visibility clears its inherited flag.
     *
     * @param newVisibility the new visibility value
     */
    public void setVisibility(final boolean newVisibility) {
        if (!isInherited(PROPERTY_VISIBILITY) 
            && newVisibility == getVisibility()) {
            return;
        }
        modifyingNode();
        setComputedVisibility(newVisibility);
        setInheritedQuiet(PROPERTY_VISIBILITY, false);
        propagatePackedPropertyState(PROPERTY_VISIBILITY, 
                                     pack & VISIBILITY_MASK);
        modifiedNode();
    }

    /**
     * @param newVisibility the new computed visibility property.
     */
    void setComputedVisibility(final boolean newVisibility) {
        if (newVisibility) {
            pack |= VISIBILITY_MASK;
        } else {
            pack &= ~VISIBILITY_MASK;
        }        
    }

    /**
     * @return current visibility property, exclusive of inheritance.
     */
    public boolean getVisibility() {
        return ((pack & VISIBILITY_MASK) == VISIBILITY_MASK);
    }

    /**
     * Setting the display property clears its inherited flag.
     * 
     * @param newDisplay new display property value
     */
    public void setDisplay(final boolean newDisplay) {
        if (!isInherited(PROPERTY_DISPLAY) 
            && newDisplay == getDisplay()) {
            return;
        }
        modifyingNode();
        setInheritedQuiet(PROPERTY_DISPLAY, false);
        setComputedDisplay(newDisplay);
        propagatePackedPropertyState(PROPERTY_DISPLAY, pack & DISPLAY_MASK);
        modifiedNode();
    }

    /**
     * @param newDisplay the new computed display value
     */
    void setComputedDisplay(final boolean newDisplay) {
        if (newDisplay) {
            pack |= DISPLAY_MASK;
        } else {
            pack &= ~DISPLAY_MASK;
        }

        computeCanRenderDisplayBit(newDisplay);
    }

    /**
     * @return current display property value, exclusive of inheritance
     */
    public boolean getDisplay() {
        return ((pack & DISPLAY_MASK) == DISPLAY_MASK);
    }

    /**
     * Setting the opacity property clears the inherited and color 
     * relative states (they are set to false).
     *
     * @param newOpacity the new opacity property
     */
    public void setOpacity(float newOpacity) {

        if (!isInherited(PROPERTY_OPACITY) && newOpacity == getOpacity()) {
            return;
        }

        modifyingNode();
        if (newOpacity > 1) {
            newOpacity = 1;
        } else if (newOpacity < 0) {
            newOpacity = 0;
        }

        setInheritedQuiet(PROPERTY_OPACITY, false);
        setComputedOpacity(newOpacity);
        propagatePackedPropertyState(PROPERTY_OPACITY, 
                                     pack2 & OPACITY_MASK);
        modifiedNode();
    }

    /**
     * @param newOpacity the new computed opacity property.
     */
    void setComputedOpacity(final float newOpacity) {
        pack2 &= ~OPACITY_MASK;
        pack2 |= (((int) (newOpacity * 200)) & OPACITY_MASK);
    }

    /**
     * @return the current opacity property value. 
     */
    public float getOpacity() {
        return (pack2 & OPACITY_MASK) / 200.0f;
    }

    /**
     * Supported traits: stroke-width, stroke-miterlimit, stroke-dashoffset,
     * fill-rule, stroke-linejoin, stroke-linecap, display, visibility, 
     * color, fill, stroke, fill-opacity, stroke-opacity, stroke-dasharray,
     * opacity
     *
     * @param traitName the name of the trait which the element may support.
     * @return true if this element supports the given trait in one of the
     *         trait accessor methods.
     */
    boolean supportsTrait(final String traitName) {
        if (SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_STROKE_MITERLIMIT_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_STROKE_DASHOFFSET_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_FILL_RULE_ATTRIBUTE == traitName
            || 
            SVGConstants.SVG_STROKE_LINEJOIN_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_STROKE_LINECAP_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_DISPLAY_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_VISIBILITY_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_COLOR_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_FILL_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_STROKE_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_FILL_OPACITY_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_STROKE_OPACITY_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_STROKE_DASHARRAY_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_OPACITY_ATTRIBUTE == traitName)  {
            return true;
        } else {
            return super.supportsTrait(traitName);
        }
    }

    /**
     * Returns the specified trait value as String. In SVG Tiny only certain
     * traits can be obtained as a String value. Syntax of the returned String
     * matches the syntax of the corresponding attribute. This element is
     * exactly equivalent to {@link org.w3c.dom.svg.SVGElement#getTraitNS
     * getTraitNS} with namespaceURI set to null.
     *
     * The method is meant to be overridden by derived classes. The 
     * implementation pattern is that derived classes will override the method 
     * and call their super class' implementation. If the ElementNode 
     * implementation is called, it means that the trait is either not supported
     * or that it cannot be seen as a String.
     *
     * @param name the requested trait name.
     * @return the trait value.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to a String (SVG Tiny only).
     */
    String getSpecifiedTraitImpl(final String name) throws DOMException {
        if ((SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE == name
             &&
             isInherited(PROPERTY_STROKE_WIDTH))
            ||
            (SVGConstants.SVG_STROKE_MITERLIMIT_ATTRIBUTE == name
             &&
             isInherited(PROPERTY_STROKE_MITER_LIMIT))
            ||
            (SVGConstants.SVG_STROKE_DASHOFFSET_ATTRIBUTE == name
             &&
             isInherited(PROPERTY_STROKE_DASH_OFFSET))
            ||
            (SVGConstants.SVG_FILL_RULE_ATTRIBUTE == name
             &&
             isInherited(PROPERTY_FILL_RULE))
            ||
            (SVGConstants.SVG_STROKE_LINEJOIN_ATTRIBUTE == name
             &&
             isInherited(PROPERTY_STROKE_LINE_JOIN))
            ||
            (SVGConstants.SVG_STROKE_LINECAP_ATTRIBUTE == name
             &&
             isInherited(PROPERTY_STROKE_LINE_CAP))
            ||
            (SVGConstants.SVG_DISPLAY_ATTRIBUTE == name
             &&
             isInherited(PROPERTY_DISPLAY))
            ||
            (SVGConstants.SVG_VISIBILITY_ATTRIBUTE == name
             &&
             isInherited(PROPERTY_VISIBILITY))
            ||
            (SVGConstants.SVG_COLOR_ATTRIBUTE == name
             &&
             isInherited(PROPERTY_COLOR))
            ||
            (SVGConstants.SVG_FILL_ATTRIBUTE == name
             &&
             isInherited(PROPERTY_FILL))
            ||
            (SVGConstants.SVG_STROKE_ATTRIBUTE == name
             &&
             isInherited(PROPERTY_STROKE))
            ||
            (SVGConstants.SVG_FILL_OPACITY_ATTRIBUTE == name
             &&
             isInherited(PROPERTY_FILL_OPACITY))
            ||
            (SVGConstants.SVG_STROKE_OPACITY_ATTRIBUTE == name
             &&
             isInherited(PROPERTY_STROKE_OPACITY))
            ||
            (SVGConstants.SVG_OPACITY_ATTRIBUTE == name
             &&
             isInherited(PROPERTY_OPACITY))
            ||
            (SVGConstants.SVG_STROKE_DASHARRAY_ATTRIBUTE == name
             &&
             isInherited(PROPERTY_STROKE_DASH_ARRAY))) {
            return SVGConstants.CSS_INHERIT_VALUE;
        } else if ((SVGConstants.SVG_FILL_ATTRIBUTE == name
                    &&
                    isColorRelative(PROPERTY_FILL)) 
                   ||
                   (SVGConstants.SVG_STROKE_ATTRIBUTE == name
                    &&
                    isColorRelative(PROPERTY_STROKE))) {
            return SVGConstants.CSS_CURRENTCOLOR_VALUE;
        } else if (SVGConstants.SVG_DISPLAY_ATTRIBUTE == name) {
            if (getDisplay()) {
                return SVGConstants.CSS_INLINE_VALUE;
            } else {
                return SVGConstants.CSS_NONE_VALUE;
            }
        } else {
            return super.getSpecifiedTraitImpl(name);
        }
    }

    /**
     * Converts the input fill-rule value to a string trait value.
     *
     * @param fillRule the fill-rule value to convert. In packed form, but
     *        with mask applied.
     * @return the converted value.
     */
    String fillRuleToStringTrait(final int fillRule) {
        if (fillRule == CompositeGraphicsNode.FILL_RULE_MASK) {
            return SVGConstants.CSS_NONZERO_VALUE;
        }
        return SVGConstants.CSS_EVENODD_VALUE;
    }

    /**
     * @param strokeLineJoin the value to convert. In packed form, but with
     *        mask applied.
     * @return the converted value.
     */
    String strokeLineJoinToStringTrait(final int strokeLineJoin) {
        switch (strokeLineJoin) {
        case CompositeGraphicsNode.JOIN_MITER_IMPL:
            return SVGConstants.CSS_MITER_VALUE;
        case CompositeGraphicsNode.JOIN_ROUND_IMPL:
            return SVGConstants.CSS_ROUND_VALUE;
        default:
            return SVGConstants.CSS_BEVEL_VALUE;
        }
    }

    /**
     * @param strokeLineCap the value to convert.
     * @return the converted value.
     */
    String strokeLineCapToStringTrait(final int strokeLineCap) {
        switch (strokeLineCap) {
        case CompositeGraphicsNode.CAP_BUTT_IMPL:
            return SVGConstants.CSS_BUTT_VALUE;
        case CompositeGraphicsNode.CAP_ROUND_IMPL:
            return SVGConstants.CSS_ROUND_VALUE;
        case CompositeGraphicsNode.CAP_SQUARE_IMPL:
        default:
            return SVGConstants.CSS_SQUARE_VALUE;
        }
    }

    /**
     * Supported traits: stroke-width, stroke-miterlimit, stroke-dashoffset,
     * fill-rule, stroke-linejoin, stroke-linecap, display, visibility, 
     * color, fill, stroke, fill-opacity, stroke-opacity, stroke-dasharray,
     * opacity
     *
     * @param name the requested trait's name
     * @return the requested trait value, as a string.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to a String (SVG Tiny only).
     */
    public String getTraitImpl(final String name)
        throws DOMException {
        if (SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE == name) {
            return Float.toString(getStrokeWidth());
        } else if (SVGConstants.SVG_STROKE_MITERLIMIT_ATTRIBUTE == name) {
            return Float.toString
                (getStrokeMiterLimit());
        } else if (SVGConstants.SVG_STROKE_DASHOFFSET_ATTRIBUTE == name) {
            return Float.toString
                (getStrokeDashOffset());
        } else if (SVGConstants.SVG_FILL_RULE_ATTRIBUTE == name) { 
            return fillRuleToStringTrait(pack & FILL_RULE_MASK);
        } else if (SVGConstants.SVG_STROKE_LINEJOIN_ATTRIBUTE == name) {
            return strokeLineJoinToStringTrait
                (pack & STROKE_LINE_JOIN_MASK);
        } else if (SVGConstants.SVG_STROKE_LINECAP_ATTRIBUTE == name) {
            return strokeLineCapToStringTrait
                (pack & STROKE_LINE_CAP_MASK);
        } else if (SVGConstants.SVG_DISPLAY_ATTRIBUTE == name) {
            if (getDisplay()) {
                return SVGConstants.CSS_INLINE_VALUE;
            } else {
                return SVGConstants.CSS_NONE_VALUE;
            }
        } else if (SVGConstants.SVG_VISIBILITY_ATTRIBUTE == name) {
            if (getVisibility()) {
                return SVGConstants.CSS_VISIBLE_VALUE;
            } else {
                return SVGConstants.CSS_HIDDEN_VALUE;
            }
        } else if (SVGConstants.SVG_COLOR_ATTRIBUTE == name) {
            return getColor().toString();
        } else if (SVGConstants.SVG_FILL_ATTRIBUTE == name) {
            return toString(getFill());
        } else if (SVGConstants.SVG_STROKE_ATTRIBUTE == name) {
            return toString(getStroke());
        } else if (SVGConstants.SVG_FILL_OPACITY_ATTRIBUTE == name) {
            return Float.toString(getFillOpacity());
        } else if (SVGConstants.SVG_STROKE_OPACITY_ATTRIBUTE == name) {
            return Float.toString(getStrokeOpacity());
        } else if (SVGConstants.SVG_STROKE_DASHARRAY_ATTRIBUTE == name) {
            return toStringTrait(getStrokeDashArray());
        } else if (SVGConstants.SVG_OPACITY_ATTRIBUTE == name) {
            return Float.toString(getOpacity());
        } else {
            return super.getTraitImpl(name);
        }
    }

    /**
     * Supported float traits: stroke-width, stroke-miterlimit, 
     * stroke-dashoffset, fill-opacity, stroke-opacity, opacity.
     *
     * @param name the requested trait's name
     * @return the requested trait's value, as a floating point.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to a float
     * @throws SecurityException if the application does not have the necessary
     * privilege rights to access this (SVG) content.
     */
    float getFloatTraitImpl(final String name)
        throws DOMException {
        if (SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE == name) {
            return getStrokeWidth();
        } else if (SVGConstants.SVG_STROKE_MITERLIMIT_ATTRIBUTE == name) {
            return getStrokeMiterLimit();
        } else if (SVGConstants.SVG_STROKE_DASHOFFSET_ATTRIBUTE == name) {
            return getStrokeDashOffset();
        } else if (SVGConstants.SVG_FILL_OPACITY_ATTRIBUTE == name) {
            return getFillOpacity();
        } else if (SVGConstants.SVG_STROKE_OPACITY_ATTRIBUTE == name) {
            return getStrokeOpacity();
        } else if (SVGConstants.SVG_OPACITY_ATTRIBUTE == name) {
            return getOpacity();
        } else {
            return super.getFloatTraitImpl(name);
        }
    }

    /**
     * Supported color traits: color, fill, stroke
     *
     * @param name the requested trait's name.
     * @return the requested trait's value, as an <code>SVGRGBColor</code>.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to {@link
     * org.w3c.dom.svg.SVGRGBColor SVGRGBColor}
     * @throws SecurityException if the application does not have the necessary
     * privilege rights to access this (SVG) content.
     */
    SVGRGBColor getRGBColorTraitImpl(String name)
        throws DOMException {
        if (SVGConstants.SVG_FILL_ATTRIBUTE.equals(name)) {
            return toSVGRGBColor(SVGConstants.SVG_FILL_ATTRIBUTE,
                                 getFill());
        } else if (SVGConstants.SVG_STROKE_ATTRIBUTE.equals(name)) {
            return toSVGRGBColor(SVGConstants.SVG_STROKE_ATTRIBUTE,
                                 getStroke());
        } else if (SVGConstants.SVG_COLOR_ATTRIBUTE.equals(name)) {
            return toSVGRGBColor(SVGConstants.SVG_COLOR_ATTRIBUTE,
                                 getColor());
        } else {
            return super.getRGBColorTraitImpl(name);
        }
    }

    /**
     * @param traitName the trait name.
     */
    TraitAnim createTraitAnimImpl(final String traitName) {
        if (SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_STROKE_MITERLIMIT_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_STROKE_DASHOFFSET_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_FILL_OPACITY_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_STROKE_OPACITY_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_OPACITY_ATTRIBUTE == traitName) {
            return new FloatTraitAnim(this, traitName, TRAIT_TYPE_FLOAT);
        } else if (SVGConstants.SVG_COLOR_ATTRIBUTE == traitName
                   ||
                   SVGConstants.SVG_FILL_ATTRIBUTE == traitName
                   ||
                   SVGConstants.SVG_STROKE_ATTRIBUTE == traitName) {
            return new FloatTraitAnim(this, traitName, 
                                      TRAIT_TYPE_SVG_RGB_COLOR);
        } else if (SVGConstants.SVG_STROKE_DASHARRAY_ATTRIBUTE == traitName) {
            return new FloatTraitAnim(this, traitName, TRAIT_TYPE_STRING);
        } else if (SVGConstants.SVG_DISPLAY_ATTRIBUTE == traitName
                   ||
                   SVGConstants.SVG_FILL_RULE_ATTRIBUTE == traitName
                   ||
                   SVGConstants.SVG_STROKE_LINECAP_ATTRIBUTE == traitName
                   ||
                   SVGConstants.SVG_STROKE_LINEJOIN_ATTRIBUTE == traitName
                   ||
                   SVGConstants.SVG_VISIBILITY_ATTRIBUTE == traitName) {
            return new StringTraitAnim(this, NULL_NS, traitName);
        } else {
            return super.createTraitAnimImpl(traitName);
        }
    }

    /**
     * Set the trait value as float array.
     *
     * @param name the trait's name.
     * @param value the trait's value.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if the requested
     * trait's value cannot be specified as a float
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is an invalid value for the given trait.
     */
    void setFloatArrayTrait(final String name, final float[][] value)
        throws DOMException {
        if (SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE == name) {
            checkPositive(name, value[0][0]);
            setStrokeWidth(value[0][0]);
        } else if (SVGConstants.SVG_STROKE_MITERLIMIT_ATTRIBUTE
                   .equals(name)) {
            if (value[0][0] < 1) {
                throw illegalTraitValue(name, Float.toString(value[0][0]));
            }

            setStrokeMiterLimit(value[0][0]);
        } else if (SVGConstants.SVG_STROKE_DASHOFFSET_ATTRIBUTE
                   .equals(name)) {
            setStrokeDashOffset(value[0][0]);
        } else if (SVGConstants.SVG_COLOR_ATTRIBUTE == name) {
            setColor(toRGB(name, value));
        } else if (SVGConstants.SVG_FILL_ATTRIBUTE == name) {
            setFill(toRGB(name, value));
        } else if (SVGConstants.SVG_STROKE_ATTRIBUTE == name) {
            setStroke(toRGB(name, value));
        } else if (SVGConstants.SVG_FILL_OPACITY_ATTRIBUTE == name) {
            setFillOpacity(value[0][0]);
        } else if (SVGConstants.SVG_STROKE_OPACITY_ATTRIBUTE == name) {
            setStrokeOpacity(value[0][0]);
        } else if (SVGConstants.SVG_STROKE_DASHARRAY_ATTRIBUTE
                   .equals(name)) {
            setStrokeDashArray(value[0]);
        } else if (SVGConstants.SVG_OPACITY_ATTRIBUTE == name) {
            setOpacity(value[0][0]);
         } else {
            super.setFloatArrayTrait(name, value);
        }    
    }
    
    /**
     * Validates the input trait value.
     *
     * @param namespaceURI the trait's namespace URI.
     * @param traitName the name of the trait to be validated.
     * @param value the value to be validated
     * @param reqNamespaceURI the namespace of the element requesting 
     *        validation.
     * @param reqLocalName the local name of the element requesting validation.
     * @param reqTraitNamespace the namespace of the trait which has the values
     *        value on the requesting element.
     * @param reqTraitName the name of the trait which has the values value on 
     *        the requesting element.
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is incompatible with the given trait.
     */
    String validateTraitNS(final String namespaceURI,
                           final String traitName,
                           final String value,
                           final String reqNamespaceURI,
                           final String reqLocalName,
                           final String reqTraitNamespace,
                           final String reqTraitName) throws DOMException {
        if (namespaceURI != null && namespaceURI != NULL_NS) {
            return super.validateTraitNS(namespaceURI,
                                         traitName,
                                         value,
                                         reqNamespaceURI,
                                         reqLocalName,
                                         reqTraitNamespace,
                                         reqTraitName);
        }

        if (SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_STROKE_MITERLIMIT_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_STROKE_DASHOFFSET_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_COLOR_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_FILL_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_STROKE_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_FILL_OPACITY_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_STROKE_OPACITY_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_STROKE_DASHARRAY_ATTRIBUTE
            .equals(traitName)
            ||
            SVGConstants.SVG_OPACITY_ATTRIBUTE == traitName) {
            throw unsupportedTraitType(traitName, TRAIT_TYPE_FLOAT);
        } else if (SVGConstants.SVG_FILL_RULE_ATTRIBUTE == traitName) {
            if (SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                return fillRuleToStringTrait
                        (getInheritedPackedPropertyState(PROPERTY_FILL_RULE));
            }

            if (!SVGConstants.CSS_NONZERO_VALUE.equals(value)
                &&
                !SVGConstants.CSS_EVENODD_VALUE.equals(value)) {
                throw illegalTraitValue(traitName, value);
            }

            return value;
        } else if (SVGConstants.SVG_STROKE_LINEJOIN_ATTRIBUTE == traitName) {
            if (SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                return strokeLineJoinToStringTrait(
                        getInheritedPackedPropertyState(
                            PROPERTY_STROKE_LINE_JOIN));
            }

            if (!SVGConstants.CSS_MITER_VALUE.equals(value)
                &&
                !SVGConstants.CSS_ROUND_VALUE.equals(value)
                &&
                !SVGConstants.CSS_BEVEL_VALUE.equals(value)) {
                throw illegalTraitValue(traitName, value);
            }

            return value;
        } else if (SVGConstants.SVG_STROKE_LINECAP_ATTRIBUTE == traitName) {
            if (SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                return strokeLineCapToStringTrait(
                        getInheritedPackedPropertyState(
                            PROPERTY_STROKE_LINE_CAP));
            }

            if (!SVGConstants.CSS_BUTT_VALUE.equals(value)
                &&
                !SVGConstants.CSS_ROUND_VALUE.equals(value)
                &&
                !SVGConstants.CSS_SQUARE_VALUE.equals(value)) {
                throw illegalTraitValue(traitName, value);
            }

            return value;
        } else if (SVGConstants.SVG_DISPLAY_ATTRIBUTE == traitName) {
            if (SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                if (getInheritedPackedPropertyState(PROPERTY_DISPLAY) != 0) {
                    return SVGConstants.CSS_INLINE_VALUE;
                }
                return SVGConstants.CSS_NONE_VALUE;
            }

            if (!SVGConstants.CSS_INLINE_VALUE.equals(value)
                &&
                !SVGConstants.CSS_NONE_VALUE.equals(value)) {
                throw illegalTraitValue(traitName, value);
            }

            return value;
        } else if (SVGConstants.SVG_VISIBILITY_ATTRIBUTE == traitName) {
            if (SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                if (getInheritedPackedPropertyState(PROPERTY_VISIBILITY) != 0) {
                    return SVGConstants.CSS_VISIBLE_VALUE;
                } else {
                    return SVGConstants.CSS_HIDDEN_VALUE;
                }
            }

            if (!SVGConstants.CSS_VISIBLE_VALUE.equals(value)
                &&
                !SVGConstants.CSS_HIDDEN_VALUE.equals(value)) {
                throw illegalTraitValue(traitName, value);
            }

            return value;
        } 

        return super.validateTraitNS(namespaceURI,
                                     traitName,
                                     value,
                                     reqNamespaceURI,
                                     reqLocalName,
                                     reqTraitNamespace,
                                     reqTraitName);
    }
 
    /**
     * Validates the float input trait value.
     *
     * @param traitName the name of the trait to be validated.
     * @param value the value to be validated
     * @param reqNamespaceURI the namespace of the element requesting 
     *        validation.
     * @param reqLocalName the local name of the element requesting validation.
     * @param reqTraitNamespace the namespace of the trait which has the values
     *        value on the requesting element.
     * @param reqTraitName the name of the trait which has the values value on 
     *        the requesting element.
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is incompatible with the given trait.
     */
    public float[][] validateFloatArrayTrait(
            final String traitName,
            final String value,
            final String reqNamespaceURI,
            final String reqLocalName,
            final String reqTraitNamespace,
            final String reqTraitName) throws DOMException {
        if (SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE == traitName) {
            return new float[][] { 
                        {parsePositiveFloatTrait(traitName, value)} 
                    };
        } else if (SVGConstants.SVG_STROKE_MITERLIMIT_ATTRIBUTE
                   .equals(traitName)) {
            float miter = parseFloatTrait(traitName, value);
            if (miter < 1) {
                throw illegalTraitValue(traitName, value);
            }
            return new float[][] { {miter} };
        } else if (SVGConstants.SVG_STROKE_DASHOFFSET_ATTRIBUTE
                   .equals(traitName)) {
            return new float[][] { {parseFloatTrait(traitName, value)} };
        } else if (SVGConstants.SVG_FILL_RULE_ATTRIBUTE == traitName
                   ||
                   SVGConstants.SVG_STROKE_LINEJOIN_ATTRIBUTE == traitName
                   ||
                   SVGConstants.SVG_STROKE_LINECAP_ATTRIBUTE == traitName
                   ||
                   SVGConstants.SVG_DISPLAY_ATTRIBUTE == traitName
                   ||
                   SVGConstants.SVG_VISIBILITY_ATTRIBUTE == traitName)  {
            throw unsupportedTraitType(traitName, TRAIT_TYPE_FLOAT);
        } else if (SVGConstants.SVG_COLOR_ATTRIBUTE == traitName) {
            RGB color = GraphicsProperties.INITIAL_COLOR;
            if (SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                color = (RGB) getInheritedPropertyState(PROPERTY_COLOR);
            } else {
                color = parseColorTrait
                    (SVGConstants.SVG_COLOR_ATTRIBUTE, value);
            }

            if (color == null) {
                throw illegalTraitValue(traitName, value);
            }
            return new float[][] {
                        {color.getRed(), color.getGreen(), color.getBlue()}
                    };
        } else if (SVGConstants.SVG_FILL_ATTRIBUTE == traitName) {
            RGB color = GraphicsProperties.INITIAL_FILL;
            if (SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                color = (RGB) getInheritedPropertyState(PROPERTY_FILL);
            } else if (SVGConstants.CSS_CURRENTCOLOR_VALUE.equals(value)) {
                color = this.color;
            } else {
                color = parseColorTrait
                    (SVGConstants.SVG_FILL_ATTRIBUTE, value);
            }

            if (color == null) {
                throw illegalTraitValue(traitName, value);
            }
            return new float[][] {
                        {color.getRed(), color.getGreen(), color.getBlue()}
                    };
        } else if (SVGConstants.SVG_STROKE_ATTRIBUTE == traitName) {
            RGB color = GraphicsProperties.INITIAL_STROKE;
            if (SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                color = (RGB) getInheritedPropertyState(PROPERTY_STROKE);
            } else if (SVGConstants.CSS_CURRENTCOLOR_VALUE.equals(value)) {
                color = getColor();
            } else {
                color = parseColorTrait
                    (SVGConstants.SVG_STROKE_ATTRIBUTE, value);
            }

            if (color == null) {
                throw illegalTraitValue(traitName, value);
            }
            return new float[][] {
                        {color.getRed(), color.getGreen(), color.getBlue()}
                    };
        } else if (SVGConstants.SVG_FILL_OPACITY_ATTRIBUTE == traitName) {
            float v = GraphicsNode.INITIAL_FILL_OPACITY;
            if (SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                if (parent != null) {
                    v = (getInheritedPackedPropertyState(PROPERTY_FILL_OPACITY)
                            >> 7) / 200.0f;
                }
            } else {
                v = parseFloatTrait(traitName, value);
                if (v < 0) {
                    v = 0;
                } else if (v > 1) {
                    v = 1;
                }
            }
            return new float[][] {{v}};
        } else if (SVGConstants.SVG_STROKE_OPACITY_ATTRIBUTE == traitName) {
            float v = GraphicsProperties.INITIAL_STROKE_OPACITY;
            if (SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                v = (getInheritedPackedPropertyState(PROPERTY_STROKE_OPACITY) 
                        >> 15) / 200.0f;
            } else {
                v = parseFloatTrait(traitName, value);
                if (v < 0) {
                    v = 0;
                } else if (v > 1) {
                    v = 1;
                }
            }
            return new float[][] {{v}};
        } else if (SVGConstants.SVG_STROKE_DASHARRAY_ATTRIBUTE
                   .equals(traitName)) {
            float[] da = parsePositiveFloatArrayTrait(traitName, value);
            if (da == null) {
                throw illegalTraitValue(traitName, value);
            } else {
                return new float[][] {da};
            }
        } else if (SVGConstants.SVG_OPACITY_ATTRIBUTE == traitName) {
            float v = GraphicsProperties.INITIAL_OPACITY;
            if (SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                v = getInheritedPackedPropertyState(PROPERTY_OPACITY) / 
                        200.0f;
            } else {
                v = parseFloatTrait(traitName, value);
                if (v < 0) {
                    v = 0;
                } else if (v > 1) {
                    v = 1;
                }
            }
            return new float[][] {{v}};
        } else {
            return super.validateFloatArrayTrait(traitName,
                                                 value,
                                                 reqNamespaceURI,
                                                 reqLocalName,
                                                 reqTraitNamespace,
                                                 reqTraitName);
        }
                      
    }

    /**
     * CompositeGraphicsNode handles the graphics node traits.
     * Other attributes are handled by the super class.
     *
     * Supported traits: stroke-width, stroke-miterlimit, stroke-dashoffset,
     * fill-rule, stroke-linejoin, stroke-linecap, display, visibility, 
     * color, fill, stroke, fill-opacity, stroke-opacity, stroke-dasharray,
     * opacity
     *
     * @param name the name of the trait to set.
     * @param value the value of the trait to set.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if the requested
     * trait's value cannot be specified as a String
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is an invalid value for the given trait or null.
     * @throws DOMException with error code NO_MODIFICATION_ALLOWED_ERR: if
     * attempt is made to change readonly trait.
     */
    public void setTraitImpl(final String name, final String value)
        throws DOMException {
        if (SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE == name) {

            // ======================= stroke-width ===================== //

            if (SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                setFloatInherited(PROPERTY_STROKE_WIDTH, true);
            } else {
                setStrokeWidth(parsePositiveFloatTrait(name, value));
            }
        } else if (SVGConstants.SVG_STROKE_MITERLIMIT_ATTRIBUTE
                   .equals(name)) {

            // ===================== stroke-miterlimit ================== //

            if (SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                setFloatInherited(PROPERTY_STROKE_MITER_LIMIT, true);
            } else {
                float miter = parseFloatTrait(name, value);
                if (miter < 1) {
                    throw illegalTraitValue(name, value);
                }
                setStrokeMiterLimit(miter);
            }

        } else if (SVGConstants.SVG_STROKE_DASHOFFSET_ATTRIBUTE
                   .equals(name)) {

            // ===================== stroke-dashoffset ================== //

            if (SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                setFloatInherited(PROPERTY_STROKE_DASH_OFFSET, true);
            } else {
                setStrokeDashOffset(parseFloatTrait(name, value));
            }
        } else if (SVGConstants.SVG_FILL_RULE_ATTRIBUTE == name) { 

            // ========================= fill-rule ====================== //

            if (SVGConstants.CSS_NONZERO_VALUE.equals(value)) {
                setFillRule(GraphicsProperties.WIND_NON_ZERO);
            } else if (SVGConstants.CSS_EVENODD_VALUE.equals(value)) {
                setFillRule(GraphicsProperties.WIND_EVEN_ODD);
            } else if (SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                setPackedInherited(PROPERTY_FILL_RULE, true);
            } else {
                throw illegalTraitValue(name, value);
            }
        } else if (SVGConstants.SVG_STROKE_LINEJOIN_ATTRIBUTE == name) {

            // ==================== stroke-linejoin ===================== //

            if (SVGConstants.CSS_MITER_VALUE.equals(value)) {
                setStrokeLineJoin(GraphicsProperties.JOIN_MITER);
            } else if (SVGConstants.CSS_ROUND_VALUE.equals(value)) {
                setStrokeLineJoin(GraphicsProperties.JOIN_ROUND);
            } else if (SVGConstants.CSS_BEVEL_VALUE.equals(value)) {
                setStrokeLineJoin(GraphicsProperties.JOIN_BEVEL);
            } else if (SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                setPackedInherited(PROPERTY_STROKE_LINE_JOIN, true);
            } else {
                throw illegalTraitValue(name, value);
            }
        } else if (SVGConstants.SVG_STROKE_LINECAP_ATTRIBUTE == name) {

            // ====================== stroke-linecap ==================== //

            if (SVGConstants.CSS_BUTT_VALUE.equals(value)) {
                setStrokeLineCap(GraphicsProperties.CAP_BUTT);
            } else if (SVGConstants.CSS_ROUND_VALUE.equals(value)) {
                setStrokeLineCap(GraphicsProperties.CAP_ROUND);
            } else if (SVGConstants.CSS_SQUARE_VALUE.equals(value)) {
                setStrokeLineCap(GraphicsProperties.CAP_SQUARE);
            } else if (SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                setPackedInherited(PROPERTY_STROKE_LINE_CAP, true);
            } else {
                throw illegalTraitValue(name, value);
            }
        } else if (SVGConstants.SVG_DISPLAY_ATTRIBUTE == name) {

            // ======================== display ========================= //

            if (SVGConstants.CSS_NONE_VALUE.equals(value)) {
                setDisplay(false);
            } else if (SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                if (!isInherited(PROPERTY_DISPLAY)) {
                    setPackedInherited(PROPERTY_DISPLAY, true);
                }
            } else if (SVGConstants.CSS_BLOCK_VALUE.equals(value)
                       ||
                       SVGConstants.CSS_COMPACT_VALUE.equals(value)
                       ||
                       SVGConstants.CSS_INLINE_TABLE_VALUE.equals(value)
                       ||
                       SVGConstants.CSS_INLINE_VALUE.equals(value)
                       ||
                       SVGConstants.CSS_LIST_ITEM_VALUE.equals(value)
                       ||
                       SVGConstants.CSS_MARKER_VALUE.equals(value)
                       ||
                       SVGConstants.CSS_RUN_IN_VALUE.equals(value)
                       ||
                       SVGConstants.CSS_TABLE_VALUE.equals(value)
                       ||
                       SVGConstants.CSS_TABLE_ROW_GROUP_VALUE.equals(value)
                       ||
                       SVGConstants.CSS_TABLE_HEADER_GROUP_VALUE.equals(value)
                       ||
                       SVGConstants.CSS_TABLE_FOOTER_GROUP_VALUE.equals(value)
                       ||
                       SVGConstants.CSS_TABLE_ROW_VALUE.equals(value)
                       ||
                       SVGConstants.CSS_TABLE_COLUMN_GROUP_VALUE.equals(value)
                       ||
                       SVGConstants.CSS_TABLE_COLUMN_VALUE.equals(value)
                       ||
                       SVGConstants.CSS_TABLE_CELL_VALUE.equals(value)
                       ||
                       SVGConstants.CSS_TABLE_CAPTION_VALUE.equals(value)) {
                setDisplay(true);
            } else {
                throw illegalTraitValue(name, value);
            }
        } else if (SVGConstants.SVG_VISIBILITY_ATTRIBUTE == name) {

            // ======================= visibility ======================= //

            if (SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                setPackedInherited(PROPERTY_VISIBILITY, true);
            } else if (SVGConstants.CSS_HIDDEN_VALUE.equals(value)
                       ||
                       SVGConstants.CSS_COLLAPSE_VALUE.equals(value)) {
                setVisibility(false);
            } else if (SVGConstants.CSS_VISIBLE_VALUE.equals(value)) {
                setVisibility(true);
            } else {
                throw illegalTraitValue(name, value);
            }
        } else if (SVGConstants.SVG_COLOR_ATTRIBUTE == name) {

            // ========================= color ========================== //

            if (SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                setInherited(PROPERTY_COLOR, true);
            } else if (SVGConstants.CSS_NONE_VALUE.equals(value)) {
                // 'none' is not a legal value for 'color'
                throw illegalTraitValue(name, value);
            } else {
                RGB color = parseColorTrait
                    (SVGConstants.SVG_COLOR_ATTRIBUTE, value);
                setColor(color);
            }
        } else if (SVGConstants.SVG_FILL_ATTRIBUTE == name) {

            // ========================= fill =========================== //

            if (SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                setInherited(PROPERTY_FILL, true);
                setColorRelative(PROPERTY_FILL, false);
            } else if (SVGConstants.CSS_NONE_VALUE.equals(value)) {
                setFill(null);
            } else if (SVGConstants.CSS_CURRENTCOLOR_VALUE.equals(value)) {
                setColorRelative(PROPERTY_FILL, true);
                setInherited(PROPERTY_FILL, false);
            } else {
                PaintServer fill = parsePaintTrait
                    (SVGConstants.SVG_FILL_ATTRIBUTE, this, value);
                if (fill != null) {
                    setFill(fill);
                }
            }
        } else if (SVGConstants.SVG_STROKE_ATTRIBUTE == name) {

            // ========================= stroke ========================= //

            if (SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                setInherited(PROPERTY_STROKE, true);
                setColorRelative(PROPERTY_STROKE, false);
            } else if (SVGConstants.CSS_NONE_VALUE.equals(value)) {
                setStroke(null);
            } else if (SVGConstants.CSS_CURRENTCOLOR_VALUE.equals(value)) {
                setColorRelative(PROPERTY_STROKE, true);
                setInherited(PROPERTY_STROKE, false);
            } else {
                PaintServer stroke = parsePaintTrait
                    (SVGConstants.SVG_STROKE_ATTRIBUTE, this, value);
                if (stroke != null) {
                    setStroke(stroke);
                }
            }
        } else if (SVGConstants.SVG_FILL_OPACITY_ATTRIBUTE == name) {

            // ====================== fill-opacity ======================= //

            if (SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                setPackedInherited(PROPERTY_FILL_OPACITY, true);
            } else {
                setFillOpacity(parseFloatTrait(name, value));
            }
        } else if (SVGConstants.SVG_STROKE_OPACITY_ATTRIBUTE
                   .equals(name)) {

            // ================= stroke-opacity ========================= //

            if (SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                setPackedInherited(PROPERTY_STROKE_OPACITY, true);
            } else {
                setStrokeOpacity(parseFloatTrait(name, value));
            }
        } else if (SVGConstants.SVG_STROKE_DASHARRAY_ATTRIBUTE
                   .equals(name)) {

            // ==================== stroke-dasharray ==================== //

            if (SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                setInherited(PROPERTY_STROKE_DASH_ARRAY, true);
            } else if (SVGConstants.CSS_NONE_VALUE.equals(value)) {
                setStrokeDashArray(null);
            } else {
                setStrokeDashArray(parsePositiveFloatArrayTrait(name, value));
            }
        } else if (SVGConstants.SVG_OPACITY_ATTRIBUTE
                   .equals(name)) {

            // ================= opacity ========================= //

            if (SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                setPackedInherited(PROPERTY_OPACITY, true);
            } else {
                setOpacity(parseFloatTrait(name, value));
            }
        } else {
            super.setTraitImpl(name, value);
        }
    }

    /**
     * @param name the name of the trait to convert.
     * @param value the float trait value to convert.
     */
    String toStringTrait(final String name, final float[][] value) {
        if (SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE == name
            ||
            SVGConstants.SVG_STROKE_MITERLIMIT_ATTRIBUTE == name
            ||
            SVGConstants.SVG_STROKE_DASHOFFSET_ATTRIBUTE == name
            ||
            SVGConstants.SVG_FILL_OPACITY_ATTRIBUTE == name
            ||
            SVGConstants.SVG_STROKE_OPACITY_ATTRIBUTE == name
            ||
            SVGConstants.SVG_OPACITY_ATTRIBUTE == name) {
            return Float.toString(value[0][0]);
        } else if (SVGConstants.SVG_COLOR_ATTRIBUTE == name
                   ||
                   SVGConstants.SVG_FILL_ATTRIBUTE == name
                   ||
                   SVGConstants.SVG_STROKE_ATTRIBUTE == name) {
            return toRGBString(name, value);
        } else if (SVGConstants.SVG_STROKE_DASHARRAY_ATTRIBUTE == name) {
            return toStringTrait(value[0]);
        } else {
            return super.toStringTrait(name, value);
        }
    }

    /**
     * Set the trait value as float.  
     *
     * Supported float traits: stroke-width, stroke-miterlimit,
     * stroke-dashoffset, fill-opacity, stroke-opacity, opacity.
     *
     * @param name the name of the trait to set.
     * @param value the value of the trait to set.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if the requested
     * trait's value cannot be specified as a float
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is an invalid value for the given trait.
     * @throws SecurityException if the application does not have the necessary
     * privilege rights to access this (SVG) content.
     */
    public void setFloatTraitImpl(final String name, final float value)
        throws DOMException {
        if (SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE == name) {
            checkPositive(name, value);
            setStrokeWidth(value);
        } else if (SVGConstants.SVG_STROKE_MITERLIMIT_ATTRIBUTE == name) {
            if (value < 1) {
                throw illegalTraitValue(name, Float.toString(value));
            }
            setStrokeMiterLimit(value);
        } else if (SVGConstants.SVG_STROKE_DASHOFFSET_ATTRIBUTE == name) {
            setStrokeDashOffset(value);
        } else if (SVGConstants.SVG_FILL_OPACITY_ATTRIBUTE == name) {
            setFillOpacity(value);
        } else if (SVGConstants.SVG_STROKE_OPACITY_ATTRIBUTE == name) {
            setStrokeOpacity(value);
        } else if (SVGConstants.SVG_OPACITY_ATTRIBUTE == name) {
            setOpacity(value);
        } else {
            super.setFloatTraitImpl(name, value);
        }
    }

    /**
     * Set the trait value as {@link org.w3c.dom.svg.SVGRGBColor SVGRGBColor}.
     *
     * Supported color traits: color, fill, stroke
     *
     * @param name the name of the trait to set.
     * @param value the value of the trait to set.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if the requested
     * trait's value cannot be specified as an {@link
     * org.w3c.dom.svg.SVGRGBColor SVGRGBColor}
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is null.
     * @throws SecurityException if the application does not have the necessary
     * privilege rights to access this (SVG) content.
     */
    void setRGBColorTraitImpl(final String name, final SVGRGBColor color)
        throws DOMException {
        try {
            // We use .equals here because the name string may not have been
            // interned.
            if (SVGConstants.SVG_FILL_ATTRIBUTE.equals(name)) {
                setFill((RGB) color);
            } else if (SVGConstants.SVG_STROKE_ATTRIBUTE .equals(name)) {
                setStroke((RGB) color);
            } else if (SVGConstants.SVG_COLOR_ATTRIBUTE .equals(name)) {
                setColor((RGB) color);
            } else {
                super.setRGBColorTraitImpl(name, color);
            } 
        } catch (IllegalArgumentException iae) {
            throw new DOMException(DOMException.INVALID_ACCESS_ERR, 
                                   iae.getMessage());
        }
    }

    /**
     * @param paintType the key provided by the PaintTarget when it subscribed 
     *        to associated PaintServer.
     * @param paintServer the PaintServer generating the update.
     */
    public void onPaintServerUpdate(final String paintType,
                                    final PaintServer paintServer) {
        if (paintType == SVGConstants.SVG_FILL_ATTRIBUTE) {
            setFill(paintServer);
        } else if (paintType == SVGConstants.SVG_STROKE_ATTRIBUTE) {
            setStroke(paintServer);
        } else {
            throw new Error();
        }
    }
}
