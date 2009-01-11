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
import com.sun.perseus.j2d.PaintServer;
import com.sun.perseus.j2d.PaintTarget;
import com.sun.perseus.j2d.RenderGraphics;
import com.sun.perseus.j2d.Tile;
import com.sun.perseus.j2d.Transform;

import org.w3c.dom.svg.SVGRect;

/**
 * A <code>TextProxy</code> proxies a <code>Text</code> node and 
 * computes its expanded content from the proxied text's data
 * content.
 *
 * @version $Id: TextProxy.java,v 1.10 2006/06/29 10:47:35 ln156897 Exp $
 */
public class TextProxy extends StructureNodeProxy {
    /**
     * Used to track the node's rendering area and the rendered areas.
     */
    protected RenderingManager renderingManager;

    /**
     * Points to the first text chunk.
     */
    protected GlyphLayout firstChunk;

    /**
     * Points to the last text chunk.
     */
    protected GlyphLayout lastChunk;

    /**
     * @param proxiedText <code>Text</code> node to proxy.
     */
    public TextProxy(final Text proxiedText) {
        super(proxiedText);
        if (DirtyAreaManager.ON) {
            renderingManager = new RenderingManager(this);
        }
    }

    /**
     * @return the tight bounding box in current user coordinate
     * space. 
     */
    public SVGRect getBBox() {
        return addNodeBBox(null, null);
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
        checkLayout();
        return ((Text) proxied).addNodeBBox(bbox, t, firstChunk);
    }

    /**
     * Should be called whenever this node's rendering becomes dirty.
     */
    final void renderingDirty() {
        if (DirtyAreaManager.ON) {
            renderingManager.dirty();
        }
    }

    /**
     * An <code>TextProxy</code> has something to render 
     *
     * @return true
     */
    public boolean hasNodeRendering() {
        return true;
    }

    /**
     * Paints this node into the input <code>RenderGraphics</code>.
     *
     * @param rg the <tt>RenderGraphics</tt> where the node should paint itself
     */
    public void paint(final RenderGraphics rg) {
        checkLayout();

        if (canRenderState != 0) {
            return;
        }

        if (DirtyAreaManager.ON) {
            Tile primitiveTile = getRenderingTile();
            if (primitiveTile == null 
                || 
                rg.getRenderingTile().isHit(primitiveTile)) {
                // rg.setPrimitiveTile(primitiveTile);
                ((Text) proxied).paintRendered(rg, this, txf, firstChunk);

                // nodeRendered is called seperately from paintRendered
                // because paintRendered is used in different contexts,
                // for example by proxy nodes to render, using their
                // proxied node's paintRendered method.
                nodeRendered();
            }
        } else {
            ((Text) proxied).paintRendered(rg, this, txf, firstChunk);
        }
    }

    /**
     * Checks that the text's layout has been computed and computes it in 
     * case it was not.    
     */
    void checkLayout() {
        if (firstChunk == null) {
            firstChunk = ((Text) proxied).layoutText(this);
            GlyphLayout cur = firstChunk;
            while (cur.nextSibling != null) {
                cur = cur.nextSibling;
            }
            lastChunk = (GlyphLayout) cur;
        }
    }

    /**
     * This method is overridden for elements which has special renderings, 
     * such as the ShapeNodes.
     *
     * @param tile the Tile instance whose bounds should be set.
     */
    protected void computeRenderingTile(final Tile tile) {
        checkLayout();
        ((Text) proxied).computeRenderingTile(tile, txf, this, firstChunk);
    }

    /**
     * Disallow proxing of anything else than <code>Text</code> nodes.
     *
     * @param newProxied this node's new proxied node
     * @throws IllegalArgumentException if the input new proxy is not
     *         a <code>Text</code> node.
     * @see ElementNodeProxy#setProxied
     */
    protected void setProxied(final ElementNode newProxied) {
        if (newProxied != null && !(newProxied instanceof Text)) {
            throw new IllegalArgumentException();
        }

        super.setProxied(newProxied);
        clearLayoutsQuiet();
    }

    /**
     * Clears the node's layout cache. For ElementNodeProxy,
     * we just reset the node so that expanded content be
     * computed again and we request the proxied node to 
     * also clear its layout cache. This is to ensure that
     * Font Data Base changes are covered.
     */
    public void clearLayouts() {
        modifyingNode();
        clearLayoutsQuiet();
        super.clearLayouts();
        modifiedNode();
    }

    /**
     * Clears all cached layout information
     * but does not generate modification
     * events for this node.
     */
    public void clearLayoutsQuiet() {
        firstChunk = null;
        lastChunk = null;
    }

    /**
     * @return a string description of this ElementNodeProxy
     */
    /*
    public String toString() {
        return "TextProxy[proxied=" + proxied + "]" + " text-anchor : " 
            + rcs.getTextAnchor();
    }
    */

    /**
     * @param newDisplay the new computed display value
     */
    public void setDisplay(final boolean newDisplay) {
        super.setDisplay(newDisplay);

        renderingDirty();
    }

    /**
     * @param newVisibility the new computed visibility property.
     */
    public void setVisibility(final boolean newVisibility) {
        super.setVisibility(newVisibility);

        renderingDirty();
    }

    /**
     * @param newFill the new computed fill property.
     */
    public void setFill(final PaintServer newFill) {
        this.fill = newFill;
        renderingDirty();
    }

    /**
     * @param newStroke the new computed stroke property.
     */
    public void setStroke(final PaintServer newStroke) {
        this.stroke = newStroke;
        renderingDirty();
    }

    /**
     * @param newStrokeWidth the new computed stroke-width property value.
     */
    public void setStrokeWidth(final float newStrokeWidth) {
        strokeWidth = newStrokeWidth;

        // Only dirty rendering if the object is actually stroked.
        if (stroke != null) {
            renderingDirty();
        }
    }

    /**
     * @param newStrokeLineJoin the new computed value for stroke-line-join
     */
    public void setStrokeLineJoin(final int newStrokeLineJoin) {
        super.setStrokeLineJoin(newStrokeLineJoin);

        if (stroke != null) {
            renderingDirty();
        }
    }

    /**
     * @param newStrokeLineCap the new value for the stroke-linecap property.
     */
    public void setStrokeLineCap(final int newStrokeLineCap) {
        super.setStrokeLineCap(newStrokeLineCap);

        if (stroke != null) {
            renderingDirty();
        }
    }

    /**
     * @param newStrokeMiterLimit the new computed stroke-miterlimit property.
     */
    public void setStrokeMiterLimit(final float newStrokeMiterLimit) {
        strokeMiterLimit = newStrokeMiterLimit; 

        if (stroke != null && getStrokeLineJoin() == JOIN_MITER) {
            renderingDirty();
        }
    }

    /**
     * @param newStrokeDashArray the new computed stroke-dasharray property 
     *        value.
     */
    public void setStrokeDashArray(final float[] newStrokeDashArray) {
        strokeDashArray = newStrokeDashArray;

        if (stroke != null) {
            renderingDirty();
        }
    }

    /**
     * @param newStrokeDashOffset the new stroke-dashoffset computed property 
     *        value.
     */
    public void setStrokeDashOffset(final float newStrokeDashOffset) {
        strokeDashOffset = newStrokeDashOffset;

        if (stroke != null && strokeDashArray != null) {
            renderingDirty();
        }
    }

    /**
     * @param newFillOpacity the new computed value for the fill opacity 
     *        property.
     */
    public void setFillOpacity(final float newFillOpacity) {                
        super.setFillOpacity(newFillOpacity);
        
        if (fill != null) {
            renderingDirty();
        }
    }

    /**
     * @param newStrokeOpacity the new computed stroke-opacity property.
     */
    public void setStrokeOpacity(final float newStrokeOpacity) {
        super.setStrokeOpacity(newStrokeOpacity);
        
        if (stroke != null) {
            renderingDirty();
        }
    }

    /**
     * @param newFontSize the new computed font-size property value.
     */
    public void setFontSize(final float newFontSize) {
        this.fontSize = newFontSize;
        
        if (stroke != null || fill != null) {
            renderingDirty();
        }

        computeCanRenderFontSizeBit(newFontSize);
    }

    /**
     * @param newFontFamily the new computed font-family property value.
     */
    public void setFontFamily(final String[] newFontFamily) {
        this.fontFamily = newFontFamily;

        clearLayoutsQuiet();

        if (stroke != null || fill != null) {
            renderingDirty();
        }
    }

    /**
     * Sets the value of the computed text anchor property.
     *
     * @param newTextAnchor the new value for the computed text anchor property.
     */
    public void setTextAnchor(final int newTextAnchor) {
        super.setTextAnchor(newTextAnchor);

        if (stroke != null || fill != null) {            
            renderingDirty();
        }
    }

    /**
     * @param newFontWeight new computed value for the font-weight property.
     */
    public void setFontWeight(final int newFontWeight) {
        super.setFontWeight(newFontWeight);

        if (stroke != null || fill != null) {
            renderingDirty();
        }

        clearLayoutsQuiet();
    }

    /**
     * @param newFontStyle the new computed font-style property.
     */
    public void setFontStyle(final int newFontStyle) {
        super.setFontStyle(newFontStyle);
        if (stroke != null || fill != null) {
            renderingDirty();
        }

        clearLayoutsQuiet();
    }

}
