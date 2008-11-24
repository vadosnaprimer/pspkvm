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

import com.sun.perseus.j2d.PaintTarget;
import com.sun.perseus.j2d.RenderGraphics;

import com.sun.perseus.j2d.Box;
import com.sun.perseus.j2d.Tile;
import com.sun.perseus.j2d.Transform;

import org.w3c.dom.svg.SVGRect;

/**
 * An <code>AbstractRenderingNodeProxy</code> delegates its rendering to a 
 * proxied <code>AbstractRenderingNode</code> and also has its own rendering
 * manager.
 *
 * @version $Id: AbstractRenderingNodeProxy.java,v 1.4 2006/06/29 10:47:29 ln156897 Exp $
 */
public class AbstractRenderingNodeProxy extends CompositeGraphicsNodeProxy {
    /**
     * Used to track the node's rendering area and the rendered areas.
     */
    protected RenderingManager renderingManager;

    /**
     * @param proxiedNode <tt>AbstractRenderingNode</tt> to proxy
     */
    protected AbstractRenderingNodeProxy(
            final AbstractRenderingNode proxiedNode) {
        super(proxiedNode);
        if (DirtyAreaManager.ON) {
            renderingManager = new RenderingManager(this);
        }
    }

    /**
     * Clears the text layouts, if any exist. This is typically
     * called when the font selection has changed and nodes such
     * as <code>Text</code> should recompute their layouts.
     * This should recursively call clearLayouts on children 
     * node or expanded content, if any.
     */
    protected void clearLayouts() {
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
     * @param t the transform to apply from the node's coordinate space to the
     *        target coordinate space. May be null for the identity 
     *        transform.
     * @return the node's bounding box in the target coordinate space.
     */
    Box addBBox(Box bbox, final Transform t) {
        return addNodeBBox(bbox, t);
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
     * Computes this node's rendering tile.
     *
     * @param tile the Tile instance whose bounds should be set.
     * @return the device space rendering tile.
     */
    protected final void computeRenderingTile(final Tile tile) {
        ((AbstractRenderingNode) proxied).computeRenderingTile(tile, txf, this);
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
     * Recomputes the transform cache, if one exists. 
     *
     * @param parentTransform the Transform applied to this node's parent.
     */
    protected void recomputeTransformState(final Transform parentTransform) {
        super.recomputeTransformState(parentTransform);
        renderingDirty();
    }

    /**
     * Modifies the node proxied by this proxy.
     *
     * @param newProxied this node's new proxied node
     */
    protected void setProxied(final ElementNode newProxied) {
        if (this.proxied == newProxied) {
            return;
        }

        super.setProxied(newProxied);
        renderingDirty();
    }

    /**
     * An <code>AbstractNodeRendering</code> has something to render.
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
        if (canRenderState != 0) {
            return;
        }

        if (DirtyAreaManager.ON) {
            Tile primitiveTile = getRenderingTile();
            if (primitiveTile == null 
                || 
                rg.getRenderingTile().isHit(primitiveTile)) {
                // rg.setPrimitiveTile(primitiveTile);
                ((AbstractRenderingNode) proxied).paintRendered(rg, this, this, 
                                                                txf);

                // nodeRendered is called seperately from paintRendered
                // because paintRendered is used in different contexts,
                // for example by proxy nodes to render, using their
                // proxied node's paintRendered method.
                nodeRendered();
            }
        } else {
            ((AbstractRenderingNode) proxied).paintRendered(rg, this, this, 
                                                            txf);
        }
    }

    /**
     * Simply notifies the RenderingManager.
     */
    protected void nodeRendered() {
        if (DirtyAreaManager.ON) {
            renderingManager.rendered();
        }
    }

    /**
     * Proxied nodes should call this method when they are being modified.
     */
    public void modifyingProxied() {
        super.modifyingProxied();
        renderingDirty();
    }

    /**
     * @return the bounding box, in screen coordinate, which encompasses the
     * node's rendering.
     */
    protected final Tile getRenderingTile() {
        return renderingManager.getRenderingTile();
    }

    /**
     * @return the tile which encompasses the node's last actual rendering. 
     */
    protected Tile getLastRenderedTile() {
        return renderingManager.getLastRenderedTile();
    }

    /**
     * After calling this method, getLastRenderedTile should always return null.
     */
    protected void clearLastRenderedTile() {
        renderingManager.clearLastRenderedTile();
    }

    /**
     * To be overriddent by derived classes, such as TimedElementNode,
     * if they need to do special operations when hooked into the 
     * document tree.
     */
    void nodeHookedInDocumentTree() {
        renderingDirty();
    }

    /**
     * To be overriddent by derived classes, such as TimedElementNode,
     * if they need to do special operations when unhooked from the 
     * document tree.
     */
    void nodeUnhookedFromDocumentTree() {
        renderingDirty();
    }

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

}
