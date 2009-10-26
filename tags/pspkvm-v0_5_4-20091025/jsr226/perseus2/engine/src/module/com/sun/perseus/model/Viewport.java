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

import com.sun.perseus.j2d.Transform;

/**
 * A <code>Viewport</code> describes a <code>ModelNode</code> into which 
 * rendering may happen. 
 *
 * <p>A <code>ViewportNode</code> has a width and height
 * which a child <code>SVG</code> uses to compute the viewbox to viewport
 * transform.</p>
 * 
 * <p>In addition, a <code>Viewport</code> has a user transform limited to 
 * its scale and translation components.</p>
 *
 * <p>The <code>Viewport</code> is the root of any SVG fragment hierarchy.
 * </p>
 *
 * @version $Id: Viewport.java,v 1.9 2006/06/29 10:47:36 ln156897 Exp $
 */
public abstract class Viewport extends CompositeNode implements Transformable {
    /**
     * Default width for viewports.
     */
    public static final int DEFAULT_VIEWPORT_WIDTH = 100;

    /**
     * Default height for viewports
     */
    public static final int DEFAULT_VIEWPORT_HEIGHT = 100;

    /**
     * As in the SVG 1.1 specification
     */
    public static final int ZOOM_PAN_MAGNIFY = 0;

    /**
     * As in the SVG 1.1 specification
     */
    public static final int ZOOM_PAN_DISABLE = 1;

    /**
     * As in the SVG 1.1 specification
     */
    public static final int ZOOM_PAN_UNKNOWN = 2;

    /**
     * Viewport width 
     */
    protected int width = DEFAULT_VIEWPORT_WIDTH;

    /**
     * Viewport height
     */
    protected int height = DEFAULT_VIEWPORT_HEIGHT;

    /**
     * The Transform applied to this node. 
     */
    protected Transform transform = new Transform(null);

    /**
     * The inverse of the Transform applied to this node. 
     */
    protected Transform inverseTxf = new Transform(null);


    /**
     * The zoomAndPan setting
     */
    protected int zoomAndPan = ZOOM_PAN_MAGNIFY;

    /**
     * Default constructor
     */
    public Viewport() {
    }

    /**
     * Returns the <code>ModelNode</code>, if any, hit by the
     * point at coordinate x/y. 
     * 
     * @param pt the x/y coordinate. Should never be null and be
     *        of size two. If not, the behavior is unspecified.
     *        The coordinates are in viewport space.
     * @return the <tt>ModelNode</tt> hit at the given point or null
     *         if none was hit.
     */
    public ModelNode nodeHitAt(final float[] pt) {
        // Check for a hit on children
        return nodeHitAt(getLastChildNode(), pt);
    }

    /**
     * @return The zoomAndPan setting for this viewport (read-only)
     */
    public int getZoomAndPan() {
        return zoomAndPan;
    }

    /**
     * @param newZoomAndPan the new value for the zoom and pan setting
     */
    public void setZoomAndPan(final int newZoomAndPan) {
        if (newZoomAndPan != ZOOM_PAN_MAGNIFY
            &&
            newZoomAndPan != ZOOM_PAN_DISABLE
            &&
            newZoomAndPan != ZOOM_PAN_UNKNOWN) {
            throw new IllegalArgumentException();
        }

        if (newZoomAndPan == zoomAndPan) {
            return;
        }
        modifyingNode();
        this.zoomAndPan = newZoomAndPan;
        modifiedNode();
    }

    ///////////////////// 

    /**
     * @return the viewport width
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * @return the viewport height
     */
    public int getHeight() {
        return this.height;
    }

    /**
     * Sets the viewport size
     *
     * @param newWidth the new viewport width. Should be greater than 0
     * @param newHeight the new viewport height. Should be greater than 0.
     */
    public void setSize(final int newWidth, final int newHeight) {
        if (newWidth < 0 || newHeight < 0) {
            throw new IllegalArgumentException();
        }

        if (newWidth == width && newHeight == height) {
            return;
        }

        modifyingNode();
        this.width = newWidth;
        this.height = newHeight;
        recomputeTransformState(null);
        computeCanRenderWidthBit(width);
        computeCanRenderHeightBit(height);
        modifiedNode();
    }

    /**
     * @return this node's cached transform. 
     */
    public Transform getTransformState() {
        return transform;
    }

    /**
     * @return this node's cached inverse transform. 
     */
    public Transform getInverseTransformState() {
        if (((canRenderState & CAN_RENDER_NON_INVERTIBLE_TXF_BIT) == 0)) {
            if (inverseTxf == null) {
                inverseTxf = new Transform(null);
                try {
                    inverseTxf = (Transform) transform.inverse(inverseTxf);
                } catch (Exception e) {
                    // If we get an exception, then we have a real error
                    // condition, because we just checked that the
                    // transform was invertible.
                    throw new Error();
                }
            }
        } else {
        inverseTxf = null;
        }
        return inverseTxf;
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
        if (parentTransform != null) {
            throw new IllegalArgumentException();
        }

        computeCanRenderTransformBit(transform);
        inverseTxf = null;
        recomputeTransformState(transform, getFirstChildNode());
    }

    /**
     * @param newTransform The new <code>Transformable</code>'s transform.
     */
    public void setTransform(final Transform newTransform) {
        if (ElementNode.equal(newTransform, this.transform)) {
            return;
        }

        modifyingNode();
        this.transform = newTransform;
        recomputeTransformState(null);
        modifiedNode();
    }

    /**
     * @return This <code>Transformable</code>'s transform.
     */
    public Transform getTransform() {
        return transform;
    }

    /**
     * Appends this node's transform, if it is not null.
     *
     * @param tx the <code>Transform</code> to apply additional node 
     *        transforms to. This may be null.
     * @param workTx a <code>Transform</code> which can be re-used if a 
     *        new <code>Transform</code> needs to be created and workTx
     *        is not the same instance as tx.
     * @return a transform with this node's transform added.
     */
    protected Transform appendTransform(Transform tx,
                                        final Transform workTx) {
        if (transform == null) {
            return tx;
        }

        tx = recycleTransform(tx, workTx);
        tx.mMultiply(transform);
        
        return tx;
    }

    /**
     * Debug helper
     * 
     * @return a textual description of this viewport object
     */
    /*
    public String toString() {
        return "[Viewport(zoomPan=" + zoomAndPan + ", width=" 
            + width + " height=" + height 
            + " txf=" + transform
            + "]";
    }
    */

}
