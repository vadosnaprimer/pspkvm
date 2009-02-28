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
import com.sun.perseus.j2d.TextRenderingProperties;
import com.sun.perseus.j2d.RenderGraphics;
import com.sun.perseus.j2d.Tile;
import com.sun.perseus.j2d.Transform;

/**
 * <code>GlyphLayout</code> is used to represent successing text 'chunks' in
 * a <code>Text</code> layout of glyphs. The notion of 'chunk' is that of the
 * SVG specification.
 *
 * @version $Id: GlyphLayout.java,v 1.4 2006/06/29 10:47:31 ln156897 Exp $
 */
class GlyphLayout {
    /**
     * The overally layout's advance
     */
    protected float advance;

    /**
     * The chunk's x-axis position
     */
    protected float x;

    /**
     * The chunk's y-axis position
     */
    protected float y;

    /**
     * The first child
     */
    protected GlyphProxy firstChild;
        
    /**
     * The last child
     */
    protected GlyphProxy lastChild;

    /**
     * Previous sibling.
     */
    protected GlyphLayout prevSibling;

    /**
     * Next sibling
     */
    protected GlyphLayout nextSibling;

    /**
     * Owner Document, to get cache objects.
     */
    protected DocumentNode ownerDocument;

    /**
     * Used to scale dash array values.
     */         
    protected float[] helperDashArray;

    /**
     * @param doc the <tt>DocumentNode</tt> scope.
     */
    GlyphLayout(final DocumentNode doc) {
        this.ownerDocument = doc;
    }
        
    /**
     * @param bbox the bounding box to which this node's bounding box should be
     *        appended. That bounding box is in the target coordinate space. It 
     *        may be null, in which case this node should create a new one.
     * @param t the transform from the node coordinate system to the coordinate
     *        system into which the bounds should be computed.
     * @return the bounding box of this node, in the target coordinate space, 
     */
    Box addBBox(Box bbox, final Transform t) {
        GlyphProxy gp = firstChild;
        while (gp != null) {
            ownerDocument.bboxGlyphTxf.setTransform(t);
            gp.applyTransform(ownerDocument.bboxGlyphTxf);
            bbox = gp.addNodeBBox(bbox, ownerDocument.bboxGlyphTxf);
            gp = gp.nextSibling;
        }
        return bbox;
    }
        
    /**
     * Add this GlyphLayout rendering tile to the input Tile.
     *
     * @param tile the Tile instance whose bounds should be expanded. May be
     *        null.
     * @param trp the TextRenderingProperties describing the nodes rendering 
     *        characteristics.
     * @param t the Transform to the requested tile space, from this node's user
     *        space.
     * @return the expanded tile.
     */
    final protected Tile addRenderingTile(Tile tile, 
                                          final TextRenderingProperties trp,
                                          final Transform t) {
        if (trp.getStroke() != null) {
            GlyphProxy gp = firstChild;
            while (gp != null) {
                ownerDocument.bboxGlyphTxf.setTransform(t);
                gp.applyTransform(ownerDocument.bboxGlyphTxf);
                tile = gp.addRenderingTile(tile, trp, t);
                gp = (GlyphProxy) gp.nextSibling;
            }
        } else {
            GlyphProxy gp = firstChild;
            float strokeWidth = trp.getStrokeWidth();
            while (gp != null) {
                ownerDocument.bboxGlyphTxf.setTransform(t);
                gp.applyTransform(ownerDocument.bboxGlyphTxf);
                trp.setStrokeWidth(strokeWidth / gp.proxied.emSquareScale);
                tile = gp.addRenderingTile(tile, trp, t);
                gp = (GlyphProxy) gp.nextSibling;
            }
        }
        return tile;
    }

    /**
     * Appends an element at the end of the list
     *
     * @param proxy the proxy to add to this <tt>GlyphLayout</tt>
     * @throws NullPointerException if the input argument is null.
     */
    public void add(final GlyphProxy proxy) {
        if (proxy == null) {
            throw new NullPointerException();
        }
            
        if (firstChild == null) {
            firstChild = proxy;
            lastChild = proxy;
            proxy.nextSibling = null;
            proxy.prevSibling = null;
        } else {
            lastChild.nextSibling = proxy;
            proxy.nextSibling = null;
            proxy.prevSibling = lastChild;
            lastChild = proxy;                
        }
    }

    /**
     * Applies the chunk's position adjustment due to the text anchor's
     * value.
     *
     * @param trp the <code>TextRenderingProperties</code> on which the 
     *        font-size property is defined.
     * @param tx the <code>Transform</code> to apply additional node 
     *        transforms to. This is guaranteed to no be null.
     */
    protected void applyTransform(final TextRenderingProperties trp, 
                                  final Transform tx) {
        // Position the text chunk on the current 
        // text position.
        tx.mTranslate(x, y);

        // Scale according to the fontSize
        float fontSize = trp.getFontSize();
        if (fontSize > 0) {
            tx.mScale(fontSize);
        }

        // Account for the text-anchor translation
        switch (trp.getTextAnchor()) {
        default:
            break;
        case TextNode.TEXT_ANCHOR_MIDDLE:
            tx.mTranslate(-advance / 2f, 0);
            break;
        case TextNode.TEXT_ANCHOR_END:
            tx.mTranslate(-advance, 0);
            break;
        }
    }

    /**
     * Applies the inverse of the chunk's position adjustment due to the
     * text anchor's value.
     *
     * @param trp the <code>TextRenderingProperties</code> on which the 
     *        font-size property is defined.
     * @param tx the <code>Transform</code> to apply the inverse transform
     *        to.
     */
    protected void applyInverseTransform(final TextRenderingProperties trp, 
                                         final Transform tx) {
        // Account for the text-anchor translation
        switch (trp.getTextAnchor()) {
        default:
            break;
        case TextNode.TEXT_ANCHOR_MIDDLE:
            tx.mTranslate(advance / 2f, 0);
            break;
        case TextNode.TEXT_ANCHOR_END:
            tx.mTranslate(advance, 0);
            break;
        }

        // Scale according to the fontSize
        float fontSize = trp.getFontSize();
        if (fontSize > 0) {
            tx.mScale(1 / fontSize);
        }

        // Position the text chunk on the current 
        // text position.
        tx.mTranslate(-x, -y);
    }

    /**
     * Fills text.
     *
     * @param rg the <code>RenderGraphics</code> where the node should paint
     *        itself.
     * @param tx the rendering transform.
     */
    void fillText(final RenderGraphics rg, final Transform tx) {
        GlyphProxy c = firstChild;            
        while (c != null) {
            ownerDocument.paintGlyphTxf.setTransform(tx);
            c.applyTransform(ownerDocument.paintGlyphTxf);
            rg.setTransform(ownerDocument.paintGlyphTxf);
            if (c.proxied.d != null) {
                rg.fill(c.proxied.d);
            }
            c = c.nextSibling;
        }
    }

    /**
     * Draws text.
     *
     * @param rg the <code>RenderGraphics</code> where the node should paint
     *        itself.
     * @param tx the rendering transform.
     */
    void drawText(final RenderGraphics rg, final Transform tx) {
        GlyphProxy c = firstChild;
        float strokeWidth = rg.getStrokeWidth();
        float dashOffset = rg.getStrokeDashOffset();
        float[] dashArray = rg.getStrokeDashArray();
        if ((dashArray != null) && 
                ((helperDashArray == null) || 
                 (helperDashArray.length != dashArray.length))) {
            helperDashArray = new float[dashArray.length];
        }
        float lastEMSquareScale = 0;
        while (c != null) {
            ownerDocument.paintGlyphTxf.setTransform(tx);
            c.applyTransform(ownerDocument.paintGlyphTxf);
            rg.setTransform(ownerDocument.paintGlyphTxf);
            if (lastEMSquareScale != c.proxied.emSquareScale) {
                rg.setStrokeWidth(strokeWidth / c.proxied.emSquareScale);
                if (dashArray != null) {
                    float emSquareScale = c.proxied.emSquareScale;
                    float lengthSum = 0;
                    for (int i = 0; i < dashArray.length; ++i) {
                        helperDashArray[i] = dashArray[i] / emSquareScale;
                        lengthSum += dashArray[i];
                    }
                    float reducedDashOffset = dashOffset;
                    if (lengthSum > 0) {
                        int r = (int)(dashOffset / lengthSum);
                        reducedDashOffset -= r * lengthSum;
                    }
                    rg.setStrokeDashArray(helperDashArray);
                    rg.setStrokeDashOffset(reducedDashOffset / emSquareScale);
                }
                lastEMSquareScale = c.proxied.emSquareScale;
            }
            
            if (c.proxied.d != null) {
                rg.draw(c.proxied.d);
            }
            c = c.nextSibling;
        }
            
        // Restore stroke-width property.
        rg.setStrokeWidth(strokeWidth);
        
        if (dashArray != null) {
            // Restore dash array & offset
            rg.setStrokeDashArray(dashArray);
            rg.setStrokeDashOffset(dashOffset);
        }
    }

    /**
     * Returns true if this node is hit by the input point. The input point
     * is in viewport space. By default, a node is not hit, not
     * matter what the input coordinate is.
     *  
     * @param pt the x/y coordinate. Should never be null and be of size two. If
     * not, the behavior is unspecified.  The x/y coordinate is in the node's
     * user space.
     * @param trp the <tt>TextRenderingProperties</tt> containing the properties
     * applicable to the hit detection operation. This is used because the same
     * node may be referenced multiple times by different proxies.
     *
     * @return true if the node is hit by the input point. 
     * @see #nodeHitAt
     */
    protected boolean isHitVP(final float[] pt, 
                              final TextRenderingProperties trp,
                              final Transform chunkTxf) {
        GlyphProxy c = lastChild;            
        while (c != null) {
            ownerDocument.hitGlyphTxf.setTransform(1, 0, 0, 1, 0, 0);
            c.applyInverseTransform(ownerDocument.hitGlyphTxf);
            ownerDocument.hitGlyphTxf.mMultiply(chunkTxf);
            ownerDocument.hitGlyphTxf.transformPoint(pt, ownerDocument.upt);
            if (c.isHit(ownerDocument.upt, trp)) {
                return true;
            }
            c = c.prevSibling;
        }

        return false;
    }

    /**
     * @return the string description of this layout.
     */
    public String toString() {
        return "GlyphLayout[x=" + x + ", y=" + y 
            + " advance=" + advance + "]";
    }
}

