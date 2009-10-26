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

import java.util.Vector;

import com.sun.perseus.j2d.Box;
import com.sun.perseus.j2d.RGB;
import com.sun.perseus.j2d.Tile;
import com.sun.perseus.j2d.RenderGraphics;

import java.io.InputStream;

/**
 * <p>The <code>DirtyAreaManager</code> abstraction is responsible for tracking
 * areas of the document tree which need to be repainted.
 *
 * @version $Id: DirtyAreaManager.java,v 1.16 2006/06/29 10:47:30 ln156897 Exp $
 */
public class DirtyAreaManager extends UpdateAdapter {
    /**
     * TEMPORARY: GLOBAL VARIABLE ALLOWING US TO CONTROL WHETHER DIRTY AREA 
     * TRACKING IS ON OR OFF.
     */
    public static boolean ON = false;

    /**
     * The minimal tile width or height.
     */
    public static final int DEFAULT_TILE_MIN_SIZE = 40;

    /**
     * The list of modified nodes. Some may have been removed from the tree.
     */
    Vector dirtyNodes = new Vector();
    /**
     * The Root tile representing this surface.
     */
    TileElement rootTile;

    /**
     * The associated viewport, which defines the size of the rendering
     * area.
     */
    Viewport vp;

    /**
     * The minimal size, in both dimensions, for dirty area tiles.
     */
    int tileMinSize = DEFAULT_TILE_MIN_SIZE;

    /**
     * Width of the viewport last time it was painted.
     */
    int lastWidth = -1;

    /**
     * Height of the viewport last time it was painted.
     */
    int lastHeight = -1;

    /**
     * The last RenderGraphics for which dirty areas were checked.
     */
    RenderGraphics lastRG;

    /**
     * @param vp the associated viewport.
     */
    public DirtyAreaManager(Viewport vp) {
        setViewport(vp);
    }

    /**
     * @param vp the new associated viewport.
     */
    public void setViewport(Viewport vp) {
        if (vp == this.vp) {
            return;
        }

        this.vp = vp;

        // Reset the lastWidth and lastHeight values to force a full repaint
        // on the first getDirtyAreas call.
        lastWidth = -1;
        lastHeight = -1;
    }

    /**
     * @param tileMinSize the minimal size for a rendering tile.
     */
    public void setTileMinSize(final int tileMinSize) {
        if (tileMinSize < 1) {
            throw new IllegalArgumentException();
        }

        this.tileMinSize = tileMinSize;
        lastWidth = -1;
        lastHeight = -1;
    }

    // =========================================================================
    // UpdateAdapter extension
    // =========================================================================

    /**
     * Invoked when a node has been inserted into the tree
     *
     * @param node the newly inserted node
     */
    public void nodeInserted(ModelNode node) {
        // We only keep track of nodes which actually have a rendering.
        if (!dirtyNodes.contains(node) && node.hasNodeRendering()) {
            dirtyNodes.addElement(node);
        }
    }

    /**
     * Invoked when a node's Rendering is about to be modified
     *
     * @param node the node which is about to be modified
     */
    public void modifyingNodeRendering(ModelNode node) {
        if (!dirtyNodes.contains(node)) {
            dirtyNodes.addElement(node);
        }
    }

    // =========================================================================
    // End of UpdateAdapter extension
    // =========================================================================

    /**
     * It is the responsibility of the dirty area manager to compute the list
     * of dirty areas which need to be re-painted. The minimum includes the
     * area covered by the modified node's old and new bounds. For efficiency
     * purposes, the implementation may collapse the rectangles into a few ones.
     *
     * @param rg the RenderGraphics for which dirty areas are queried. The first
     *        tile a RenderGraphics is passed to this method, the full area is 
     *        considered dirty.
     * @return the set of rectangles to render, based on the 
     *         list of node whose rendering has changed.
     */
    public TileElement getDirtyAreas(RenderGraphics rg) {
        int vw = vp.width;
        int vh = vp.height;

        if (lastWidth != vw || lastHeight != vh) {
            // System.err.println(">>>>>>> lastWidth was : " + lastWidth 
            //                      + " lastHeight was " + lastHeight);
            // System.err.println(">>>>>>> new width is  : " + vw 
            //                      + " new height is " + vh);

            lastWidth = vw;
            lastHeight = vh;
            if (vw >= (2 * tileMinSize) 
                && 
                vh >= (2 * tileMinSize)) {
                rootTile = new TileQuadrant(null, tileMinSize, 0, 0, vw, vh);
            } else {
                rootTile = new TileElement(null, 0, 0, vw, vh);
            }

            // This is the first rendering or the viewport dimensions have
            // changed.  Everything is dirty. We need to repaint the full
            // canvas.
            dirtyNodes.removeAllElements();
            lastRG = rg;
            return rootTile;
        }

        // If it is the first time we render in this RenderGraphics, we consider
        // the complete area dirty.
        if (lastRG != rg) {
            System.err.println(">>>> RenderGraphics changed");
            lastRG = rg;
            dirtyNodes.removeAllElements();
            return rootTile;
        }

        // Unmark all areas
        rootTile.clear();

        final int nn = dirtyNodes.size();
        Tile nb = null;
        ModelNode n = null;
        for (int i = 0; i < nn && !rootTile.hit; i++) {
            n = (ModelNode) dirtyNodes.elementAt(i);

            // First, check the previous rendering's tile.
            rootTile.checkHit(n.getLastRenderedTile());

            // Clear the last rendered tile so that the area is not
            // unnecessarily repainted in the future, in case this node is, for
            // example, hidden for a while before being repainted.
            n.clearLastRenderedTile();

            // Now, compute the current rendering tile and check the 
            // area it hits.
            if (n.parent != null && (n.canRenderState == 0)) {
                rootTile.checkHit(n.getRenderingTile());
            } 
        }

        dirtyNodes.removeAllElements();

        return rootTile.getHitTiles(null); 
    }

    /**
     * Asks the DirtyAreaManager to repaint all the diry areas on the given 
     * RenderGraphics
     *
     * @param mn the model node to render, typically, the DocumentNode.
     * @param rg the RenderGraphics to render to.
     * @param clearPaint the color to use to clear the background
     */
    public void refresh(final ModelNode mn, final RenderGraphics rg, 
                        final RGB clearPaint) {
        TileElement dirtyAreaList = getDirtyAreas(rg);
        TileElement curTile = dirtyAreaList;
        int nTiles = 0;
        while (curTile != null) {
            // System.err.println("Painting tile : " + curTile 
            //         + " in thread " + Thread.currentThread());
            // System.err.println("clearing with paint : " + clearPaint);
            // System.err.println("Painting on buffer of size : " 
            //         + rg.bi.getWidth() + "/" + rg.bi.getHeight());
            rg.setRenderingTile(curTile);
            rg.clearRect(curTile.x, 
                         curTile.y, 
                         curTile.maxX - curTile.x + 1, 
                         curTile.maxY - curTile.y + 1,
                         clearPaint);
            mn.paint(rg);
            curTile = curTile.next;

            /*
            javax.swing.JOptionPane.showMessageDialog(
                    null, 
                    "Current Offscreen : " + rg.bi.getWidth() + " / " 
                        + rg.bi.getHeight(), 
                    "Debug",
                    0, 
                    new javax.swing.ImageIcon(rg.bi));
            */
        }
    }

    public static class TileElement extends Tile {
        /**
         * Parent tile.
         */
        TileQuadrant parent;

        /**
         * Boolean marker showing the tile is fully hit.
         * If true, it means the tile and all its children have been hit.
         */
        boolean hit;

        /**
         * The next tile in the rendering tile chain. Tiles are chained to 
         * describe the list of rendering areas.
         */
        public TileElement next;

        /**
         * @param parent the parent TileQuadrant.
         * @param x the tile's origin along the x-axis. Should be positive or 
         *        zero.
         * @param y the tile's origin along the y-axis. Should be positive or 
         *        zero.
         * @param width the tile's width
         * @param height the tile's height
         */
        TileElement(final TileQuadrant parent, 
                    final int x,
                    final int y,
                    final int width,
                    final int height) {
            this.parent = parent;
            if (x < 0 || y < 0) {
                throw new IllegalArgumentException();
            }
            setTile(x, y, width, height);
        }

        /**
         * Checks if this tile is hit by the input tile. This must set the hit
         * flag to true if this tile, and all its children (if any) are hit or
         * have been hit by previous calls to this method.
         *
         * @param t the tile to check against.
         */
        void checkHit(final Tile t) {
            if (t == null) {
                return;
            }

            if (isHit(t)) {
                hit = true;
                if (parent != null) {
                    parent.notifyTileHit(this);
                }
            }
        }

        /**
         * Clears the hit flag.
         */
        public void clear() {
            hit = false;
            next = null;
        }

        /**
         * @param rt the TileElement to chain to.
         * @return the dirty area, i..e. the chained list of hit children.
         */
        TileElement getHitTiles(TileElement rt) {
            if (hit) {
                next = rt;
                return this;
            }

            return rt;
        }
    }

    public static class TileQuadrant extends TileElement {
        /**
         * Children tiles.
         */
        TileElement[] children;

        /**
         * @param parentTile the parent TileQuadrant
         * @param tileMinSize the minimal size for tiles.
         * @param x the tile's origin along the x-axis
         * @param y the tile's origin along the y-axis
         * @param width the tile's width
         * @param height the tile's height
         */
        public TileQuadrant(final TileQuadrant parent, 
                            final int tileMinSize,
                            final int x,
                            final int y,
                            final int width,
                            final int height) {
            super(parent, x, y, width, height);

            if (width < (2 * tileMinSize)
                ||
                height < (2 * tileMinSize)) {
                throw new IllegalArgumentException();
            }

            int cw = width / 2;
            int ch = height / 2;

            if (((width / 4) < tileMinSize)
                || 
                ((height / 4) < tileMinSize)) {
                // We should not subdivide any more because this quadrant's tile
                // cannot be subdivided without going below the minimal tile
                // size.
                children = new TileElement[4];
                children[0] = new TileElement(this, x, y, cw, ch);
                children[1] = new TileElement(this, x + cw, y, width - cw, ch);
                children[2] = new TileElement(this, x, y + ch, cw, height - ch);
                children[3] = new TileElement(this, x + cw, y + ch, width - cw,
                                              height - ch);
            } else {
                children = new TileQuadrant[4];
                children[0] = new TileQuadrant(this, tileMinSize, x, y, cw, ch);
                children[1] = new TileQuadrant(this, tileMinSize, x + cw, y, 
                                               width - cw, ch);
                children[2] = new TileQuadrant(this, tileMinSize, x, y + ch, 
                                               cw, height - ch);
                children[3] = new TileQuadrant(this, tileMinSize, x + cw, 
                                               y + ch, width - cw, height - ch);
            }
        }

        /**
         * Checks if this tile is hit by the input tile.
         *
         * @param t the tile to check against.
         */
        void checkHit(final Tile t) {
            if (t == null) {
                return;
            }

            if (hit) {
                // The tile is already hit, no need to check
                // any further.
                return;
            }

            if (isHit(t)) {
                for (int i = 0; i < 4; i++) {
                    children[i].checkHit(t);
                }
            }
        }

        /**
         * Called by a child tile when it is hit.
         */
        void notifyTileHit(Tile child) {
            // Simply check if all the quadrants have been hit. If so, notify 
            // parent in turn.
            if (children[0].hit && 
                children[1].hit &&
                children[2].hit &&
                children[3].hit) {
                hit = true;
                if (parent != null) {
                    parent.notifyTileHit(this);
                }
            }
        }

        /**
         * Clears the hit flag.
         */
        public void clear() {
            super.clear();
            children[0].clear();
            children[1].clear();
            children[2].clear();
            children[3].clear();
        }

        /**
         * @param rt the TileElement to chain to.
         * @return the dirty area, i..e. the chained list of hit children.
         */
        TileElement getHitTiles(TileElement rt) {
            if (hit) {
                next = rt;
                return this;
            }

            for (int i = 0; i < 4; i++) {
                rt = children[i].getHitTiles(rt);
            }

            return rt;
        }

    }
}
