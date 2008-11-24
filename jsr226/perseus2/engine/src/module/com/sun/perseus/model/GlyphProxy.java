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

import com.sun.perseus.j2d.Box;
import com.sun.perseus.j2d.RenderGraphics;
import com.sun.perseus.j2d.TextRenderingProperties;
import com.sun.perseus.j2d.Tile;
import com.sun.perseus.j2d.Transform;

/**
 * A <code>GlyphNodeProxy</code> delegates its rendering to a 
 * proxied <code>Glyph</code> object. 
 *
 * @version $Id: GlyphProxy.java,v 1.10 2006/06/29 10:47:31 ln156897 Exp $
 */
public final class GlyphProxy {
    /**
     * The origin of the glyph on the x axis.
     */
    protected float x;

    /**
     * The glyph's rotation
     */
    protected float rotate;

    /**
     * The proxied glyph.
     */
    protected Glyph proxied;

    /**
     * The next GlyphProxy sibling.
     */
    protected GlyphProxy nextSibling;

    /**
     * The previous GlyphProxy sibling.
     */
    protected GlyphProxy prevSibling;

    /**
     * @param proxied <code>Glyph</code> node to proxy. Should not
     * be null.
     */
    public GlyphProxy(final Glyph proxied) {
        this.proxied = proxied;
    }

    /**
     * @param pt the x/y coordinate. Should never be null and be
     *        of size two. If not, the behavior is unspecified. The point
     *        should be in user space.
     * @param trp the <tt>TextRenderingProperties</tt> containing the properties
     *        applicable to the hit detection operation. This is used
     *        because the same node may be referenced multiple times
     *        by different proxies. 
     * @return true if this node is hit by the input point. The input point
     * is in viewport space. This is invoked in the hit detection
     * process after the node's properties have been applied and the 
     * display value has been checked (i.e., the node is actually rendered).
     *  
     * @see #nodeHitAt
     */
    protected boolean isHit(final float[] pt, 
                            final TextRenderingProperties trp) {
        return proxied.isHit(pt, trp);
    }
    
    /**
     * @param bbox the bounding box to which this node's bounding box should be
     *        appended. That bounding box is in the target coordinate space. It 
     *        may be null, in which case this node should create a new one.
     * @param t the transform from the node coordinate system to the coordinate
     *        system into which the bounds should be computed.
     * @return the bounding box of this node, in the target coordinate space, 
     */
    Box addNodeBBox(final Box bbox, 
                    final Transform t) {
        return proxied.addNodeBBox(bbox, t);
    }

    /**
     * @param newX the new origin on the x-axis
     */
    public void setX(final float newX) {
        this.x = newX;
    }

    /**
     * @param newRotate the new glyph rotation
     */
    public void setRotate(final float newRotate) {
        this.rotate = newRotate;
    }

    /**
     * @return this node's rotation
     */
    public float getRotate() {
        return rotate;
    }

    /**
     * @return this node's origin on the x-axis
     */
    public float getX() {
        return x;
    }

    /**
     * Adds this glyph's bounds to the input tile. If the input tile.
     *
     * @param tile the Tile instance whose bounds should be set.
     * @param trp the TextRenderingProperties describing the nodes rendering 
     *        characteristics.
     * @param t the Transform to the requested tile space, from this node's user
     *        space.
     * @return the expanded tile.
     */
    protected Tile addRenderingTile(Tile tile, 
                                    final TextRenderingProperties trp,
                                    final Transform t) {
        return proxied.addRenderingTile(tile, trp, t);
    }

    /**
     * Apply this node's x/y translation.
     *
     * @param tx the <code>Transform</code> to add node transform to.
     *        This is guaranteed to be not null.
     */
    protected void applyTransform(final Transform tx) {
        // Add this node's x/y translation.
        tx.mTranslate(x, 0);

        // Rotate the glyph
        tx.mRotate(rotate);

        proxied.applyTransform(tx);
    }

    /**
     * Apply this node inverse additional transform.
     *
     * @param tx the <code>Transform</code> to add node transform to.
     *        This is guaranteed to be not null.
     */
    protected void applyInverseTransform(final Transform tx) {
        proxied.applyInverseTransform(tx);

        // Rotate the glyph
        tx.mRotate(-rotate);

        // Add this node's x/y translation.
        tx.mTranslate(-x, 0);
    }

    /**
     * @return a string description of this ElementNodeProxy
     */
    public String toString() {
        return "GlyphProxy[x=" + x + " rotate=" + rotate
            + " proxied=" + proxied + "][" + super.toString() + "]";
    }
}
