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
import com.sun.perseus.j2d.PaintServer;
import com.sun.perseus.j2d.PaintTarget;
import com.sun.perseus.j2d.PathSupport;
import com.sun.perseus.j2d.RenderGraphics;
import com.sun.perseus.j2d.Tile;

import com.sun.perseus.util.SVGConstants;

import org.w3c.dom.DOMException;

import org.w3c.dom.svg.SVGMatrix;

import com.sun.perseus.j2d.Transform;

/**
 * All nodes which represent geometry (complex or simple shapes)
 * are represented by descendants of this class.
 *
 * @version $Id: AbstractShapeNode.java,v 1.16 2006/06/29 10:47:29 ln156897 Exp $
 */
public abstract class AbstractShapeNode extends AbstractRenderingNode {
    /**
     * Constructor.
     *
     * @param ownerDocument this element's owner <code>DocumentNode</code>
     */
    public AbstractShapeNode(final DocumentNode ownerDocument) {
        super(ownerDocument);
    }

    /**
     * @return an adequate <code>ElementNodeProxy</code> for this node.
     */
    ElementNodeProxy buildProxy() {
        return new AbstractShapeNodeProxy(this);
    }

    /**
     * @param rg the RenderGraphics on which to fill the shape.
     */
    public abstract void fillShape(final RenderGraphics rg);

    /**
     * @param rg the RenderGraphics on which to draw the shape.
     */
    public abstract void drawShape(final RenderGraphics rg);

    /**
     * @param x the hit point coordinate along the x-axis, in user space.
     * @param y the hit point coordinate along the y-axis, in user space.
     * @param fillRule the fillRule to apply when testing for containment.
     * @return true if the hit point is contained within the shape.
     */
    public abstract boolean contains(final float x, 
                                     final float y,
                                     final int fillRule);

    /**
     * @param x the hit point coordinate along the x-axis, in user space.
     * @param y the hit point coordinate along the y-axis, in user space.
     * @param gp the <code>GraphicsProperties</code> instance defining the 
     *        rendering context.
     * @return true if the hit point is contained within the shape.
     */
    public final boolean strokedContains(final float x, 
                                         final float y,
                                         final GraphicsProperties gp) {
        Object strokedPath = getStrokedPath(this);
        return PathSupport.isStrokedPathHit(strokedPath, gp.getFillRule(), x, 
                                            y);
    }

    /**
     * Returns the stroked shape, using the given stroke properties.
     *
     * @param gp the <code>GraphicsProperties</code> defining the rendering
     *        context.
     * @return the shape's stroked path.
     */
    abstract Object getStrokedPath(final GraphicsProperties gp);

    /**
     * Computes the rendering tile for the given set of GraphicsProperties.
     *
     * @param tile the Tile instance whose bounds should be set.
     * @param t the Transform to the requested tile space, from this node's user
     * space.
     * @param gp the <code>GraphicsProperties</code> for which the tile
     *        should be computed.
     * @return the screen bounding box when this node is rendered with the 
     * given render context.
     */
    protected void computeRenderingTile(final Tile tile, 
                                        final Transform t,
                                        final GraphicsProperties gp) {
        if (gp.getStroke() == null) {
            // No stroking on the shape, we can use the geometrical bounding 
            // box.
            tile.snapBox(addNodeBBox(null, t));
        } else {
            // Need to account for stroking, with a more costly operation to 
            // compute the stroked bounds.
            Object strokedPath = getStrokedPath(gp);
            PathSupport.computeStrokedPathTile(tile, strokedPath, t);
        }
    }

    /**
     * Paints this node into the input RenderGraphics, assuming the node
     * is rendered.
     *
     * @param rg the <code>RenderGraphics</code> where the node should paint
     * itself.
     * @param gp the <code>GraphicsProperties</code> controlling the operation's
     * rendering
     * @param pt the <code>PaintTarget</code> for the paint operation.
     * @param txf the <code>Transform</code> from user space to device space for
     * the paint operation.
     */
    protected void paintRendered(final RenderGraphics rg,
                                 final GraphicsProperties gp,
                                 final PaintTarget pt,
                                 final Transform tx) {
        if (!gp.getVisibility()) {
            return;
        }
        
        rg.setPaintTarget(pt);
        rg.setPaintTransform(tx);
        rg.setTransform(tx);

        // Fill the shape. Only apply the fill property
        if (gp.getFill() != null) {
            rg.setFillRule(gp.getFillRule());
            rg.setFill(gp.getFill());
            rg.setFillOpacity(gp.getFillOpacity());
            fillShape(rg);
        }

        // Stroke the shape. Only apply the stroke properties
        if (gp.getStroke() != null) {
            rg.setStroke(gp.getStroke());
            rg.setStrokeOpacity(gp.getStrokeOpacity());
            rg.setStrokeWidth(gp.getStrokeWidth());
            rg.setStrokeLineCap(gp.getStrokeLineCap());
            rg.setStrokeLineJoin(gp.getStrokeLineJoin());
            rg.setStrokeDashArray(gp.getStrokeDashArray());
            rg.setStrokeMiterLimit(gp.getStrokeMiterLimit());
            rg.setStrokeDashOffset(gp.getStrokeDashOffset());
            drawShape(rg);
        }
    }

    /**
     * Returns true if this node is hit by the input point. The input point
     * is in viewport space. 
     *  
     * For an <tt>AbstractShapeNode</tt> this method returns true if:
     * <ul>
     * <li>the node is visible <b>and</b><ul>
     * <li>the node's fill is not NONE and the associated shape contains the
     *     input point, <b>or</b></li>
     * <li>the node's stroke is not NONE and the associated stroked shape
     *     contains the input point.</li></ul></li>
     * </ul>
     * This implements the equivalent of the visiblePainted value for the 
     * pointerEvents attribute. That attribute is not part of SVG Tiny,
     * but the default behavior in SVG Tiny is that of visiblePainted.
     *
     * @param pt the x/y coordinate. Should never be null and be
     *        of size two. If not, the behavior is unspecified.
     *        The x/y coordinate is in viewport space.
     *
     * @return true if the node is hit by the input point. 
     * @see #nodeHitAt
     */
    protected boolean isHitVP(float[] pt) {
        // Node has to be visible to be a hit target
        if (!getVisibility() 
            ||
            (fill == null && stroke == null)) {
            return false;
        }
        
        getInverseTransformState().transformPoint(pt, ownerDocument.upt);
        pt = ownerDocument.upt;
        
        // If the node is filled, see if the shape is hit
        if (fill != null) {
            if (contains(pt[0], pt[1], getFillRule())) {
                return true;
            }
        }

        // Test detection on the edge if the stroke color
        // is set.
        if (stroke != null) {
            if (strokedContains(pt[0], pt[1], this)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if this proxy node is hit by the input point. The input 
     * point is in viewport space. 
     *
     * @param pt the x/y coordinate. Should never be null and be
     *        of size two. If not, the behavior is unspecified.
     *        The x/y coordinate is in viewport space.
     * @param proxy the tested ElementNodeProxy.
     * @return true if the node is hit by the input point. 
     * @see #isHitVP
     */
    protected boolean isProxyHitVP(float[] pt, 
                                   final AbstractRenderingNodeProxy proxy) {
        // Node has to be visible to be a hit target
        if (!proxy.getVisibility() 
            ||
            (proxy.fill == null && proxy.stroke == null)) {
            return false;
        }

        proxy.getInverseTransformState().transformPoint(pt, ownerDocument.upt);
        pt = ownerDocument.upt;
        
        // If the node is filled, see if the shape is hit
        if (proxy.fill != null) {
            if (contains(pt[0], pt[1], proxy.getFillRule())) {
                return true;
            }
        }

        // Test detection on the edge if the stroke color
        // is set.
        if (((AbstractShapeNodeProxy) proxy).stroke != null) {
            if (strokedContains(pt[0], pt[1], proxy)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @param newFill the new computed fill property.
     */
    void setComputedFill(final PaintServer newFill) {
        this.fill = newFill;
        renderingDirty();
    }

    /**
     * @param newStroke the new computed stroke property.
     */
    void setComputedStroke(final PaintServer newStroke) {
        this.stroke = newStroke;
        renderingDirty();
    }

    /**
     * @param newStrokeWidth the new computed stroke-width property value.
     */
    void setComputedStrokeWidth(final float newStrokeWidth) {
        strokeWidth = newStrokeWidth;

        // Only dirty rendering if the object is actually stroked.
        if (stroke != null) {
            renderingDirty();
        }
    }

    /**
     * @param newStrokeLineJoin the new computed value for stroke-line-join
     */
    void setComputedStrokeLineJoin(final int newStrokeLineJoin) {
        super.setComputedStrokeLineJoin(newStrokeLineJoin);

        if (stroke != null) {
            renderingDirty();
        }
    }

    /**
     * @param newStrokeLineCap the new value for the stroke-linecap property.
     */
    void setComputedStrokeLineCap(final int newStrokeLineCap) {
        super.setComputedStrokeLineCap(newStrokeLineCap);

        if (stroke != null) {
            renderingDirty();
        }
    }

    /**
     * @param newStrokeMiterLimit the new computed stroke-miterlimit property.
     */
    void setComputedStrokeMiterLimit(final float newStrokeMiterLimit) {
        strokeMiterLimit = newStrokeMiterLimit; 

        if (stroke != null && getStrokeLineJoin() == JOIN_MITER) {
            renderingDirty();
        }
    }

    /**
     * @param newStrokeDashArray the new computed stroke-dasharray property 
     *        value.
     */
    void setComputedStrokeDashArray(final float[] newStrokeDashArray) {
        strokeDashArray = newStrokeDashArray;

        if (stroke != null) {
            renderingDirty();
        }
    }

    /**
     * @param newStrokeDashOffset the new stroke-dashoffset computed property 
     *        value.
     */
    void setComputedStrokeDashOffset(final float newStrokeDashOffset) {
        strokeDashOffset = newStrokeDashOffset;

        if (stroke != null && strokeDashArray != null) {
            renderingDirty();
        }
    }

    /**
     * @param newFillOpacity the new computed value for the fill opacity 
     *        property.
     */
    void setComputedFillOpacity(final float newFillOpacity) {                
        super.setComputedFillOpacity(newFillOpacity);
        
        if (fill != null) {
            renderingDirty();
        }
    }

    /**
     * @param newStrokeOpacity the new computed stroke-opacity property.
     */
    void setComputedStrokeOpacity(final float newStrokeOpacity) {
        super.setComputedStrokeOpacity(newStrokeOpacity);
        
        if (stroke != null) {
            renderingDirty();
        }
    }

}
