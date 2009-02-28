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

import com.sun.perseus.j2d.Tile;

/**
 * The <code>RenderingManager</code> class provides support for efficiently 
 * managing rendered areas in ModelNode implementations, such as
 * AbstractShapeNode.
 *
 * @version $Id: RenderingManager.java,v 1.4 2006/06/29 10:47:33 ln156897 Exp $
 */
class RenderingManager { 
    /**
     * The associated ModelNode, the one for which RenderingManager
     * tracks regions.
     */
    protected ModelNode node;

    /**
     * True when the rendering tile needs to be recomputed.
     */
    protected boolean rTileDirty = true;
        
    /**
     * The node's current rendering bounds
     */
    protected Tile rTile = new Tile();
        
    /**
     * The node's most recent rendered tile.
     */
    protected Tile lastRenderedTile = null;
        
    /**
     * A value cached so that we don't reallocate tiles all the time.
     */
    protected Tile lrtCache = new Tile();

    /**
     * @node the associated <code>ModeNode</code>
     */
    protected RenderingManager(final ModelNode node) {
        this.node = node;
    }

    /**
     * @return the bounding box, in screen coordinate, which encompasses the
     * node's rendering.
     */
    protected Tile getRenderingTile() {
        if (rTileDirty) {
            node.computeRenderingTile(rTile);
            rTileDirty = false;
        }
            
        return rTile;
    }

    /**
     * Marks the rendering as dirty, meaning that the rendering tile cached
     * value cannot be reused, if any value has been cached.
     */
    protected final void dirty() {
        rTileDirty = true;
        node.modifyingNodeRendering();
    }

    /**
     * @return the set node's last actual rendering. If this node's
     * hasRendering method returns false, then this method should return
     * null. 
     */
    protected Tile getLastRenderedTile() {
        return lastRenderedTile;
    }

    /**
     * After calling this method, getLastRendered should always return null.
     */
    protected void clearLastRenderedTile() {
        lastRenderedTile = null;
    }
        
    /**
     * Should be called by ModelNode implementations when they have just 
     * rendered so that the rendered area is captured in the lastRenderedTile.
     *
     * Important: it is the responsibility of the classes using this method
     * to make sure the rendering tile is not dirty when calling this
     * method.  If the rendering tile is dirty, then the captured value will
     * not correspond to the actual rendering area.
     */
    protected void rendered() {
        if (lastRenderedTile == null) {
            lastRenderedTile = lrtCache;
        } 
        lastRenderedTile.x = rTile.x;
        lastRenderedTile.y = rTile.y;
        lastRenderedTile.maxX = rTile.maxX;
        lastRenderedTile.maxY = rTile.maxY;
    }
}

