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
 * Use is subject to license terms.
 */
package com.sun.perseus.model;

import com.sun.perseus.j2d.RenderGraphics;

import com.sun.perseus.j2d.Box;
import com.sun.perseus.j2d.GraphicsProperties;
import com.sun.perseus.j2d.PaintServer;
import com.sun.perseus.j2d.PaintTarget;
import com.sun.perseus.j2d.RGB;
import com.sun.perseus.j2d.Tile;
import com.sun.perseus.j2d.Transform;

import org.w3c.dom.svg.SVGRect;

/**
 * A <code>CompositeGraphicsNodeProxy</code> delegates its rendering to a 
 * proxied <code>CompositeGraphics</code> object and also keeps a cached
 * transform and inverse transform.
 *
 * @version $Id: CompositeGraphicsNodeProxy.java,v 1.4 2006/06/29 10:47:30 ln156897 Exp $
 */
public class CompositeGraphicsNodeProxy extends ElementNodeProxy 
    implements GraphicsProperties, PaintTarget {
    /**
     * Cached Transform
     */
    protected Transform txf = null;

    /**
     * Cached inverse transform
     */
    protected Transform inverseTxf = null;

    // ====================================================================
    // Property values
    // ====================================================================
    /**
     * The current color. The fill and stroke colors may be
     * relative to the current color
     */
    protected RGB color = CompositeGraphicsNode.INITIAL_COLOR;

    /**
     * The fill paint used to fill this node
     */
    protected PaintServer fill = CompositeGraphicsNode.INITIAL_FILL;

    /**
     * The stroke paint used to stroke the outline of
     * this ShapeNode
     */
    protected PaintServer stroke = CompositeGraphicsNode.INITIAL_STROKE;

    /**
     * The winding rule for this node
     */
    protected int fillRule = CompositeGraphicsNode.INITIAL_FILL_RULE;

    /**
     * The stroke width
     */
    protected float strokeWidth = CompositeGraphicsNode.INITIAL_STROKE_WIDTH;

    /**
     * The stroke miter limit
     */
    protected float strokeMiterLimit = 
            CompositeGraphicsNode.INITIAL_STROKE_MITER_LIMIT;

    /**
     * The stroke dash array
     */
    protected float[] strokeDashArray = 
            CompositeGraphicsNode.INITIAL_STROKE_DASH_ARRAY;

    /**
     * The stroke dash offset
     */
    protected float strokeDashOffset = 
            CompositeGraphicsNode.INITIAL_STROKE_DASH_OFFSET;

    /**
     * Same structure as the CompositeGraphicsNode pack and pack2 fields.
     */
    protected int pack = CompositeGraphicsNode.INITIAL_PACK;
    protected int pack2 = CompositeGraphicsNode.INITIAL_PACK2;

    /**
     * @param proxiedNode <tt>ElementNode</tt> to proxy
     */
    protected CompositeGraphicsNodeProxy(
            final CompositeGraphicsNode proxiedNode) {
        super(proxiedNode);

        // By default, a CompositeGraphicsNodeProxy is renderable
        canRenderState &= CAN_RENDER_RENDERABLE_MASK;
        canRenderState |= (proxiedNode.canRenderState 
                            & CAN_RENDER_PROXY_BITS_MASK);
        
        // We copy the computed value for all properties upon initialization. 
        // When the node is hooked in the tree, inherited properties will be
        // recomputed.
        fill = proxiedNode.fill;
        stroke = proxiedNode.stroke;
        color = proxiedNode.color;
        pack = proxiedNode.pack;
        pack2 = proxiedNode.pack2;
        strokeWidth = proxiedNode.strokeWidth;
        strokeMiterLimit = proxiedNode.strokeMiterLimit;
        strokeDashArray = proxiedNode.strokeDashArray;
        strokeDashOffset = proxiedNode.strokeDashOffset;
    }
    
    /**
     * A <code>CompositeGraphicsNodeProxy</code> contributes to its parent 
     * bounding box only if its display property is turned on.
     *
     * @return true if the node's bounding box should be accounted for.
     */
    protected boolean contributeBBox() {
        return (pack & CompositeGraphicsNode.DISPLAY_MASK) != 0;
    }
    
    /**
     * Recomputes all inherited properties.
     */
    void recomputeInheritedProperties() {
        ModelNode p = ownerDocument;
        if (parent != null) {
            p = parent;
        }
        recomputePropertyState(
                GraphicsNode.PROPERTY_FILL, 
                p.getPropertyState(GraphicsNode.PROPERTY_FILL));
        recomputePropertyState(
                GraphicsNode.PROPERTY_STROKE, 
                p.getPropertyState(GraphicsNode.PROPERTY_STROKE));
        recomputePropertyState(
                GraphicsNode.PROPERTY_COLOR, 
                p.getPropertyState(GraphicsNode.PROPERTY_COLOR));
        recomputePackedPropertyState(
                GraphicsNode.PROPERTY_FILL_RULE, 
                p.getPackedPropertyState(GraphicsNode.PROPERTY_FILL_RULE));
        recomputeFloatPropertyState(
                GraphicsNode.PROPERTY_STROKE_WIDTH,
                p.getFloatPropertyState(GraphicsNode.PROPERTY_STROKE_WIDTH));
        recomputePackedPropertyState(
                GraphicsNode.PROPERTY_STROKE_LINE_JOIN, 
                p.getPackedPropertyState(
                    GraphicsNode.PROPERTY_STROKE_LINE_JOIN));
        recomputePackedPropertyState(
                GraphicsNode.PROPERTY_STROKE_LINE_CAP,
                p.getPackedPropertyState(
                    GraphicsNode.PROPERTY_STROKE_LINE_CAP));
        recomputeFloatPropertyState(
                GraphicsNode.PROPERTY_STROKE_MITER_LIMIT,
                p.getFloatPropertyState(
                    GraphicsNode.PROPERTY_STROKE_MITER_LIMIT));
        recomputePropertyState(
                GraphicsNode.PROPERTY_STROKE_DASH_ARRAY,
                p.getPropertyState(GraphicsNode.PROPERTY_STROKE_DASH_ARRAY));
        recomputeFloatPropertyState(
                GraphicsNode.PROPERTY_STROKE_DASH_OFFSET, 
                p.getFloatPropertyState(
                    GraphicsNode.PROPERTY_STROKE_DASH_OFFSET));
        recomputePackedPropertyState(
                GraphicsNode.PROPERTY_DISPLAY, 
                p.getPackedPropertyState(GraphicsNode.PROPERTY_DISPLAY));
        recomputePackedPropertyState(
                GraphicsNode.PROPERTY_VISIBILITY, 
                p.getPackedPropertyState(GraphicsNode.PROPERTY_VISIBILITY));
        recomputePackedPropertyState(
                GraphicsNode.PROPERTY_FILL_OPACITY, 
                p.getPackedPropertyState(GraphicsNode.PROPERTY_FILL_OPACITY));
        recomputePackedPropertyState(
                GraphicsNode.PROPERTY_STROKE_OPACITY, 
                p.getPackedPropertyState(GraphicsNode.PROPERTY_STROKE_OPACITY));
        recomputePackedPropertyState(
                GraphicsNode.PROPERTY_OPACITY, 
                p.getPackedPropertyState(GraphicsNode.PROPERTY_OPACITY));
    }
    
    // JAVADOC COMMENT ELIDED
    public SVGRect getBBox() {
        return null;
    }

    /**
     * Returns the value of the given Object-valued property.
     *
     * @return the value of the given property.
     */
    protected Object getPropertyState(final int propertyIndex) {
        switch (propertyIndex) {
        case GraphicsNode.PROPERTY_FILL:
            return fill;
        case GraphicsNode.PROPERTY_STROKE:
            return stroke;
        case GraphicsNode.PROPERTY_COLOR:
            return color;
        case GraphicsNode.PROPERTY_STROKE_DASH_ARRAY:            
            return getStrokeDashArray();
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
        case GraphicsNode.PROPERTY_STROKE_WIDTH:
            return strokeWidth;
        case GraphicsNode.PROPERTY_STROKE_MITER_LIMIT:
            return strokeMiterLimit;
        case GraphicsNode.PROPERTY_STROKE_DASH_OFFSET:
            return strokeDashOffset;
        default: 
            return super.getFloatPropertyState(propertyIndex);
        }
    }

    /**
     * Returns the value of the given packed property.
     *
     * @return the value of the given property.
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
     * Checks the state of the Object type property value.
     *
     * @param propertyIndex the property index
     * @param propertyValue the computed value for the property.
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
     * Checks the state of the float type property value.
     *
     * @param propertyIndex the property index
     * @param propertyValue the computed value for the property.
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
     * @param propertyValue the computed value for the property.
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
                    (pack & CompositeGraphicsNode.DISPLAY_MASK));
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
     * Sets the computed value of the given Object-valued property.
     *
     * @param propertyIndex the property index
     * @param propertyValue the computed value of the property.
     */
    protected void setPropertyState(final int propertyIndex,
                                    final Object propertyValue) {
        switch (propertyIndex) {
        case GraphicsNode.PROPERTY_FILL:
            setFill((PaintServer) propertyValue);
            break;
        case GraphicsNode.PROPERTY_STROKE:
            setStroke((PaintServer) propertyValue);
            break;
        case GraphicsNode.PROPERTY_COLOR:
            setColor((RGB) propertyValue);
            break;
        case GraphicsNode.PROPERTY_STROKE_DASH_ARRAY:            
            setStrokeDashArray((float[]) propertyValue);
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
        case GraphicsNode.PROPERTY_STROKE_WIDTH:
            setStrokeWidth(propertyValue);
            break;
        case GraphicsNode.PROPERTY_STROKE_MITER_LIMIT:
            setStrokeMiterLimit(propertyValue);
            break;
        case GraphicsNode.PROPERTY_STROKE_DASH_OFFSET:
            setStrokeDashOffset(propertyValue);
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
     * @param propertyValue the computed value for the property.
     */
    protected void setPackedPropertyState(final int propertyIndex,
                                          final int propertyValue) {
        switch (propertyIndex) {
        case GraphicsNode.PROPERTY_FILL_RULE:
            if (propertyValue == 0) {
                setFillRule(WIND_EVEN_ODD);
            } else {
                setFillRule(WIND_NON_ZERO);
            }
            break;
        case GraphicsNode.PROPERTY_STROKE_LINE_JOIN:
            switch (propertyValue) {
                case CompositeGraphicsNode.JOIN_MITER_IMPL:
                    setStrokeLineJoin(JOIN_MITER);
                    break;
                case CompositeGraphicsNode.JOIN_ROUND_IMPL:
                    setStrokeLineJoin(JOIN_ROUND);
                    break;
                case CompositeGraphicsNode.JOIN_BEVEL_IMPL:
                default:
                    setStrokeLineJoin(JOIN_BEVEL);
                    break;
            }
            break;
        case GraphicsNode.PROPERTY_STROKE_LINE_CAP:
            switch (propertyValue) {
                case CompositeGraphicsNode.CAP_BUTT_IMPL:
                    setStrokeLineCap(CAP_BUTT);
                    break;
                case CompositeGraphicsNode.CAP_ROUND_IMPL:
                    setStrokeLineCap(CAP_ROUND);
                    break;
                case CompositeGraphicsNode.CAP_SQUARE_IMPL:
                default:
                    setStrokeLineCap(CAP_SQUARE);
                    break;
            }
            break;
        case GraphicsNode.PROPERTY_DISPLAY:
            if (propertyValue != 0) {
                setDisplay(true);
            } else {
                setDisplay(false);
            }
            break;
        case GraphicsNode.PROPERTY_VISIBILITY:
            if (propertyValue != 0) {
                setVisibility(true);
            } else {
                setVisibility(false);
            }
            break;
        case GraphicsNode.PROPERTY_FILL_OPACITY:
            setFillOpacity((propertyValue >> 7) / 200.0f);
            break;
        case GraphicsNode.PROPERTY_STROKE_OPACITY:
            setStrokeOpacity((propertyValue >> 15) / 200.0f);
            break;
        case GraphicsNode.PROPERTY_OPACITY:
            setOpacity(propertyValue / 200.0f);
            break;
        default: 
            super.setPackedPropertyState(propertyIndex, propertyValue);
            break;
        }
    }

    /**
     * Recomputes the given Object-valued property's state given the new
     * parent property.
     *
     * @param propertyIndex index of the property whose value is changing.
     * @param parentPropertyValue the value that children of this node should 
     *        now inherit.
     */
    protected void recomputePropertyState(final int propertyIndex,
                                          final Object parentPropertyValue) {
        // We do not need to recompute the property value if:
        // - the property is _not_ inherited
        // or
        // - the property is inherited by the new parent property computed value
        //   is the same as the current value.
        if (!((CompositeGraphicsNode) proxied).isInherited(propertyIndex) 
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
     * Recomputes the given float-valued property's state given the new
     * parent property.
     *
     * @param propertyIndex index of the property whose value is changing.
     * @param parentPropertyValue the value that children of this node should 
     *        now inherit.
     */
    protected void recomputeFloatPropertyState(final int propertyIndex,
                                          final float parentPropertyValue) {
        // We do not need to recompute the property value if:
        // - the property is _not_ inherited
        // or
        // - the property is inherited by the new parent property computed value
        //   is the same as the current value.
        if (!((CompositeGraphicsNode) proxied).isInherited(propertyIndex) 
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
     * @param propertyIndex index of the property whose value is changing.
     * @param parentPropertyValue the value that children of this node should 
     *        now inherit.
     */
    protected void recomputePackedPropertyState(final int propertyIndex,
                                          final int parentPropertyValue) {
        // We do not need to recompute the property value if:
        // - the property is _not_ inherited
        // or
        // - the property is inherited by the new parent property computed value
        //   is the same as the current value.
        if (!((CompositeGraphicsNode) proxied).isInherited(propertyIndex) 
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
     * @param propertyIndex index of the property whose value has changed.
     * @param parentPropertyValue the value that children of this node should 
     *        now inherit.
     * 
     */
    protected void propagatePropertyState(final int propertyIndex,
                                          final Object parentPropertyValue) {
        // Propagate to expanded children.
        ModelNode node = firstExpandedChild;
        while (node != null) {
            node.recomputePropertyState(propertyIndex, parentPropertyValue);
            node = node.nextSibling;
        }
    }

    /**
     * Called when the computed value of the given float-valued property
     * has changed.
     *
     * @param propertyIndex index of the property whose value has changed.
     * @param parentPropertyValue the value that children of this node should 
     *        now inherit.
     * 
     */
    protected void propagateFloatPropertyState(
            final int propertyIndex,
            final float parentPropertyValue) {
        // Propagate to expanded children.
        ModelNode node = firstExpandedChild;
        while (node != null) {
            node.recomputeFloatPropertyState(propertyIndex, 
                                             parentPropertyValue);
            node = node.nextSibling;
        }
    }

    /**
     * Called when the computed value of the given packed property has changed.
     *
     * @param propertyIndex index of the property whose value has changed.
     * @param parentPropertyValue the value that children of this node should 
     *        now inherit.
     * 
     */
    protected void propagatePackedPropertyState(final int propertyIndex,
                                                final int parentPropertyValue) {
        // Propagate to expanded children.
        ModelNode node = firstExpandedChild;
        while (node != null) {
            node.recomputePackedPropertyState(propertyIndex, 
                                              parentPropertyValue);
            node = node.nextSibling;
        }
    }

    /**
     * Called by the proxied node when the given Object-valued property's
     * computed value has changed.
     *
     * @param propertyIndex index of the property whose value is changing.
     * @param proxiedComputedValue computed value for the proxied node.
     * 
     */
    protected void proxiedPropertyStateChange(
            final int propertyIndex,
            final Object proxiedComputedValue) {
        if (!((CompositeGraphicsNode) proxied).isInherited(propertyIndex)) {
            // The property is specified on the proxied node, update the 
            // state with that specified value.
            setPropertyState(propertyIndex, proxiedComputedValue);
        } else {
            // The property is unspecified on the proxied node. Inherit from
            // the proxy's parent (and not the proxied's parent).
            setPropertyState(propertyIndex, 
                             getInheritedPropertyState(propertyIndex));
        }

        // Do not propagate changes to the proxy children: propagation happens 
        // through the proxied's tree, so children will be notified if needed.
    }

    /**
     * Called by the proxied node when the given float-valued property's
     * computed value has changed.
     *
     * @param propertyIndex index of the property whose value is changing.
     * @param proxiedComputedValue computed value for the proxied node.
     * 
     */
    protected final void proxiedFloatPropertyStateChange(
            final int propertyIndex,
            final float proxiedComputedValue) {
        if (!((CompositeGraphicsNode) proxied).isInherited(propertyIndex)) {
            // The property is specified on the proxied node, update the 
            // state with that specified value.
            setFloatPropertyState(propertyIndex, proxiedComputedValue);
        } else {
            // The property is unspecified on the proxied node. Inherit from
            // the proxy's parent (and not the proxied's parent).
            setFloatPropertyState(
                    propertyIndex, 
                    getInheritedFloatPropertyState(propertyIndex));
        }

        // Do not propagate changes to the proxy children: propagation happens 
        // through the proxied's tree, so children will be notified if needed.
    }

    /**
     * Called by the proxied node when the given packed property's computed 
     * value has changed.
     *
     * @param propertyIndex index of the property whose value is changing.
     * @param proxiedComputedValue computed value for the proxied node.
     * 
     */
    protected final void proxiedPackedPropertyStateChange(
            final int propertyIndex,
            final int proxiedComputedValue) {
        if (!((CompositeGraphicsNode) proxied).isInherited(propertyIndex)) {
            // The property is specified on the proxied node, update the 
            // state with that specified value.
            setPackedPropertyState(propertyIndex, proxiedComputedValue);
        } else {
            // The property is unspecified on the proxied node. Inherit from
            // the proxy's parent (and not the proxied's parent).
            setPackedPropertyState(propertyIndex, 
                    getInheritedPackedPropertyState(propertyIndex));
        }

        // Do not propagate changes to the proxy children: propagation happens 
        // through the proxied's tree, so children will be notified if needed.
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
        txf = proxied.appendTransform(parentTransform, txf);        
        computeCanRenderTransformBit(txf);
        inverseTxf = null;
        // inverseTxf = computeInverseTransform(txf, parentTransform, 
        //                                      inverseTxf);
        recomputeTransformState(txf, firstExpandedChild);
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
    public Transform getInverseTransformState() {
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
     * @return the context's fill property value
     */
    public PaintServer getFill() {
        return fill;
    }

    /**
     * @param newFill the new computed fill property.
     */
    public void setFill(final PaintServer newFill) {
        this.fill = newFill;
    }   

    /**
     * @return the context's stroke property value
     */
    public PaintServer getStroke() {
        return stroke;
    }

    /**
     * @param newStroke the new computed stroke property.
     */
    public void setStroke(final PaintServer newStroke) {
        this.stroke = newStroke;
    }

    /**
     * @return the context's color property value
     */
    public RGB getColor() {
        return this.color;
    }

    /**
     * @param newColor the new computed color property.
     */
    public void setColor(final RGB newColor) {
        color = newColor;
    }

    /**
     * @return the context's fill rule property value
     */
    public int getFillRule() {
        if ((pack & CompositeGraphicsNode.FILL_RULE_MASK) 
                == 
            CompositeGraphicsNode.FILL_RULE_MASK) {
            return WIND_NON_ZERO;
        }
        return WIND_EVEN_ODD;
    }

    /**
     * @param newFillRule the new computed fillRule property value.
     */
    public final void setFillRule(final int newFillRule) {
        if (newFillRule == WIND_NON_ZERO) {
            pack |= CompositeGraphicsNode.FILL_RULE_MASK;
        } else {
            pack &= ~CompositeGraphicsNode.FILL_RULE_MASK;
        }
    }

    /**
     * @return the context's stroke-width.
     */
    public float getStrokeWidth() {
        return strokeWidth;
    }

    /**
     * @param newStrokeWidth the new computed stroke-width property value.
     */
    public void setStrokeWidth(final float newStrokeWidth) {
        strokeWidth = newStrokeWidth;
    }

    /**
     * @return the context's stroke-linecap.
     */
    public int getStrokeLineCap() {
        switch (pack & CompositeGraphicsNode.STROKE_LINE_CAP_MASK) {
        case CompositeGraphicsNode.CAP_BUTT_IMPL:
            return CAP_BUTT;
        case CompositeGraphicsNode.CAP_ROUND_IMPL:
            return CAP_ROUND;
        default:
            return CAP_SQUARE;
        }
    }

    /**
     * @param newStrokeLineCap the new value for the stroke-linecap property.
     */
    public void setStrokeLineCap(final int newStrokeLineCap) {
        // Clear stroke-linecap
        pack &= ~CompositeGraphicsNode.STROKE_LINE_CAP_MASK;

        switch (newStrokeLineCap) {
        case CAP_BUTT:
            pack |= CompositeGraphicsNode.CAP_BUTT_IMPL;
            break;
        case CAP_ROUND:
            pack |= CompositeGraphicsNode.CAP_ROUND_IMPL;
            break;
        default:
            pack |= CompositeGraphicsNode.CAP_SQUARE_IMPL;
            break;
        }
    }

    /**
     * @return the stroke line join property value
     */
    public int getStrokeLineJoin() {
        switch (pack & CompositeGraphicsNode.STROKE_LINE_JOIN_MASK) {
        case CompositeGraphicsNode.JOIN_MITER_IMPL:
            return JOIN_MITER;
        case CompositeGraphicsNode.JOIN_ROUND_IMPL:
            return JOIN_ROUND;
        default:
            return JOIN_BEVEL;
        }
    }

    /**
     * @param newStrokeLineJoin the new computed value for stroke-line-join
     */
    public void setStrokeLineJoin(final int newStrokeLineJoin) {
        // Clear stroke-linejoin
        pack &= ~CompositeGraphicsNode.STROKE_LINE_JOIN_MASK;

        switch (newStrokeLineJoin) {
        case JOIN_MITER:
            pack |= CompositeGraphicsNode.JOIN_MITER_IMPL;
            break;
        case JOIN_ROUND:
            pack |= CompositeGraphicsNode.JOIN_ROUND_IMPL;
            break;
        default:
            pack |= CompositeGraphicsNode.JOIN_BEVEL_IMPL;
            break;
        }
    }

    /**
     * @return the context's stroke miter limit property value
     */
    public float getStrokeMiterLimit() {
        return strokeMiterLimit;
    }
            
    /**
     * @param newStrokeMiterLimit the new computed stroke-miterlimit property.
     */
    public void setStrokeMiterLimit(final float newStrokeMiterLimit) {
        strokeMiterLimit = newStrokeMiterLimit;        
    }

    /**
     * @return the stroke dash offset property
     */
    public float getStrokeDashOffset() {
        return strokeDashOffset;
    }

    /**
     * @param newStrokeDashOffset the new stroke-dashoffset computed property 
     *        value.
     */
    public void setStrokeDashOffset(final float newStrokeDashOffset) {
        strokeDashOffset = newStrokeDashOffset;
    }

    /**
     * @return the stroke dash array property
     */
    public float[] getStrokeDashArray() {
        return strokeDashArray;
    }

    /**
     * @param newStrokeDashArray the new computed stroke-dasharray property 
     *        value.
     */
    public void setStrokeDashArray(final float[] newStrokeDashArray) {
        strokeDashArray = newStrokeDashArray;
    }

    /**
     * @return the context' display property value
     */
    public boolean getDisplay() {
        return ((pack & CompositeGraphicsNode.DISPLAY_MASK) 
                == CompositeGraphicsNode.DISPLAY_MASK);
    }

    /**
     * @param newDisplay the new computed display value
     */
    public void setDisplay(final boolean newDisplay) {
        if (newDisplay) {
            pack |= CompositeGraphicsNode.DISPLAY_MASK;
        } else {
            pack &= ~CompositeGraphicsNode.DISPLAY_MASK;
        }

        computeCanRenderDisplayBit(newDisplay);
    }

    /**
     * @return the visibility property value.
     */
    public boolean getVisibility() {
        return ((pack & CompositeGraphicsNode.VISIBILITY_MASK) 
                == CompositeGraphicsNode.VISIBILITY_MASK);
    }

    /**
     * @param newVisibility the new computed visibility property.
     */
    public void setVisibility(final boolean newVisibility) {
        if (newVisibility) {
            pack |= CompositeGraphicsNode.VISIBILITY_MASK;
        } else {
            pack &= ~CompositeGraphicsNode.VISIBILITY_MASK;
        }        
    }

    /**
     * @return the context's fill opacity property value
     */
    public float getFillOpacity() {
        return ((pack & CompositeGraphicsNode.FILL_OPACITY_MASK) >> 7) / 200.0f;
    }

    /**
     * @param newFillOpacity the new computed value for the fill opacity 
     *        property.
     */
    public void setFillOpacity(final float newFillOpacity) {                
        pack &= ~CompositeGraphicsNode.FILL_OPACITY_MASK;
        pack |= ((((int) (newFillOpacity * 200)) << 7) 
                & CompositeGraphicsNode.FILL_OPACITY_MASK);
    }

    /**
     * @return the stroke opacity value
     */
    public float getStrokeOpacity() {
        return ((pack & CompositeGraphicsNode.STROKE_OPACITY_MASK) >> 15) 
                / 200.0f;
    }

    /**
     * @param newStrokeOpacity the new computed stroke-opacity property.
     */
    public void setStrokeOpacity(final float newStrokeOpacity) {
        pack &= ~CompositeGraphicsNode.STROKE_OPACITY_MASK;
        pack |= ((((int) (newStrokeOpacity * 200)) << 15) 
                & CompositeGraphicsNode.STROKE_OPACITY_MASK);
    }

    /**
     * @return the opacity value
     */
    public float getOpacity() {
        return (pack2 & CompositeGraphicsNode.OPACITY_MASK) / 200.0f;
    }

    /**
     * @param newOpacity the new computed opacity property.
     */
    public void setOpacity(final float newOpacity) {
        pack2 &= ~CompositeGraphicsNode.OPACITY_MASK;
        pack2 |= (((int) (newOpacity * 200)) 
                & CompositeGraphicsNode.OPACITY_MASK);
    }

    /**
     * This method is used to implement the PaintTarget interface which is used
     * to compute the rendering area. However, the paint server updates are 
     * handled by the proxied node, so this should _never_ be called.
     *
     * @param paintType the key provided by the PaintTarget when it subscribed 
     *        to associated PaintServer.
     * @param paintServer the PaintServer generating the update.
     */
    public void onPaintServerUpdate(final String paintType,
                                    final PaintServer paintServer) {
        // Do nothing.
    }
}
