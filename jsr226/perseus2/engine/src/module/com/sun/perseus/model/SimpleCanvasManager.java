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

import com.sun.perseus.j2d.RenderGraphics;

import java.io.InputStream;

import com.sun.perseus.util.RunnableQueue;
import com.sun.perseus.util.RunnableQueue.RunnableHandler;

import com.sun.perseus.model.DirtyAreaManager.TileElement;

import com.sun.perseus.j2d.RGB;
import com.sun.perseus.j2d.Transform;

/**
 * <p>The <code>SimpleCanvasManager</code> class is responsible for
 * keeping the rendering of a <code>ModelNode</code> tree on a 
 * <code>RenderGraphics</code> current.</p>
 * 
 * <p>Specifically, the <code>SimpleCanvasManager</code> listens to 
 * update events in a <code>ModelNode</code> tree and 
 * triggers repaint into the <code>RenderGraphics</code> when
 * necessary.</p>
 * 
 * <p>The <code>SimpleCanvasManager</code> does not handle documents
 * that have not been loaded and does nothing on <code>loadComplete</code>
 * calls.</p>
 *
 * @version $Id: SimpleCanvasManager.java,v 1.15 2006/06/29 10:47:34 ln156897 Exp $
 */
public class SimpleCanvasManager implements UpdateListener, RunnableHandler {
    /**
     * The object used as a lock to synchronize access 
     * to the paint surface.
     */
    public final Object lock = new Object();

    /**
     * This flag should be used by the consumer of the offscreen
     * buffer to let the SimpleCanvasManager know when the updated
     * offscreen has been consumed.
     */
    protected boolean canvasConsumed = true;

    /**
     * The <code>RenderGraphics</code> which this 
     * <code>SimpleCanvasManager</code> keeps up to date
     * with its associated model changes.
     */
    protected RenderGraphics rg;

    /**
     * Model which this <code>SimpleCanvasManager</code> renders to
     * the associated <code>RenderGraphics</code>
     */
    protected DocumentNode documentNode;

    /**
     * Listens to canvas updates
     */
    protected CanvasUpdateListener canvasUpdateListener;

    /**
     * Controls whether a repaint is needed or not
     */
    protected boolean needRepaint;

    /**
     * The color used to clear the canvas.
     */
    protected RGB clearPaint = RGB.white;

    /**
     * Dirty area manager. May be null.
     */
    protected DirtyAreaManager dirtyAreaManager;

    /**
     * When off, no updates are made to the rendering surface.
     */
    protected boolean isOff = false;

    /**
     * @param rg the <code>RenderGraphics</code> which this 
     *        instance will keep up to date with the 
     *        model changes.
     * @param documentNode the <code>DocumentNode</code>, root of the 
     *        tree that this <code>SimpleCanvasManager</code> will
     *        draw and keep current on the <code>RenderGraphics</code>
     * @param canvasUpdateListener the <code>CanvasUpdateListener</code>
     *        which listens to completed updates on the associated
     *        <code>RenderGraphics</code>
     *
     * @throws IllegalArgumentException if rg, documentNode or listener is null.
     */
    public SimpleCanvasManager(
            final RenderGraphics rg,
            final DocumentNode documentNode,
            final CanvasUpdateListener canvasUpdateListener) {

        if (rg == null 
            || documentNode == null 
            || canvasUpdateListener == null) {
            throw new IllegalArgumentException(
                    "RenderGraphics : " + rg 
                    + " DocumentNode : " + documentNode 
                    + " CanvasUpdateListener : " + canvasUpdateListener);
        }

        this.rg = rg;
        this.documentNode = documentNode;
        this.canvasUpdateListener = canvasUpdateListener;
        this.documentNode.setUpdateListener(this);
        this.dirtyAreaManager = new DirtyAreaManager(documentNode);
    }

    /**
     * @param rg the <code>RenderGraphics</code> which this 
     *        instance should now update. Setting the 
     *        <code>RenderGraphics</code> causes a full repaint
     *        to be scheduled.
     * @throws IllegalArgumentException if rg is null.
     */
    public void setRenderGraphics(final RenderGraphics rg) {
        if (rg == null) {
            throw new IllegalArgumentException();
        }

        synchronized (lock) {
            this.rg = rg;
            
            // Repaint the documentNode into the new graphics
            needRepaint = true;

            // The new buffer has not been painted and has not been
            // consumed.
            canvasConsumed = false;
        }
    }

    /**
     * @return the RenderGraphics to the canvas managed by this 
     *         SimpleCanvasManager.
     */
    public RenderGraphics getRenderGraphics() {
        return rg;
    }

    /**
     * Should be called by the SimpleCanvasManager user to notify the 
     * SimpleCanvasManager when an update to the canvas has been consumed.
     */
    public void consume() {
        synchronized (lock) {
            canvasConsumed = true;
            lock.notifyAll();
        }
    }

    /**
     * Invoked when a node has been inserted into the tree
     *
     * @param node the newly inserted node
     */
    public void nodeInserted(final ModelNode node) {
        if (DirtyAreaManager.ON) {
            dirtyAreaManager.nodeInserted(node);
        }
        needRepaint = true;
    }

    /**
     * Invoked when a node is about to be modified. This will
     * be used in the future to track dirty areas.
     *
     * @param node the node which is about to be modified
     */
    public void modifyingNode(final ModelNode node) {
        if ((node.hasNodeRendering() || node.hasDescendants())
            && (node.canRenderState == 0)) {
            needRepaint = true;
        }
    }

    /**
     * Invoked when a node's rendering is about to be modified
     *
     * @param node the node which is about to be modified
     */
    public void modifyingNodeRendering(ModelNode node) {
        // Note that this is redundant with the the check done in 
        // DirtyAreaManager. However, this is needed because DirtyAreaManager
        // is sometimes used stand-alone (e.g. from ScalableGraphics).
        // Having the call in the if statement makes the check redundant only 
        // when evaluating to true.
        if (DirtyAreaManager.ON) {
            dirtyAreaManager.modifyingNodeRendering(node);
        }
    }

    /**
     * Invoked when a node modification completed.
     *
     * @param node the node which was just modified.
     */
    public void modifiedNode(final ModelNode node) {
        if (!needRepaint 
            &&
            (node.hasNodeRendering() 
             || 
             node.hasDescendants())) {
            needRepaint = true;
        }
    }

    /**
     * Invoked when the input node has finished loading. 
     *
     * @param node the <code>node</code> for which loading
     *        is complete.
     */
    public void loadComplete(final ModelNode node) {
        // Do nothing.
    }

    /**
     * Invoked when a document error happened before finishing loading.
     *
     * @param documentNode the <code>DocumentNode</code> for which loading
     *        has failed.
     * @param error the exception which describes the reason why loading
     *        failed.
     */
    public void loadingFailed(final DocumentNode documentNode, 
                              final Exception error) {
        // Do nothing.
    }

    /**
     * Invoked when the document starts loading
     *
     * @param documentNode the <code>DocumentNode</code> for which loading
     *        is starting
     * @param is the <code>InputStream</code> from which SVG content
     *        is loaded.
     */
    public void loadStarting(final DocumentNode documentNode, 
                             final InputStream is) {
    }

    /**
     * Invoked when the input node has started loading
     *
     * @param node the <code>ModelNode</code> for which loading
     *        has started.
     */
    public void loadBegun(final ModelNode node) {
    }

    /**
     * Invoked when a string has been appended, during a load
     * phase. This is only used when parsing a document and is
     * used in support of progressive download, like the other
     * loadXXX methods.
     *
     * @param node the <code>ModelNode</code> on which text has been
     *        inserted.
     */
    public void textInserted(final ModelNode node) {
    }

    /**
     * Utility method used to update the canvas appropriately
     * depending on what is needed.
     * 
     * During the loading phase, while we do progressive
     * rendering, the canvas will only redraw nodes in the
     * progressiveNodes list, unless a repaint has been 
     * requested.
     *
     * Important Note: this method should only be called from
     * the update thread, i.e., the thread that also manages
     * the model node tree.
     */
    public void updateCanvas() {
        if (needRepaint) {
            if (canvasConsumed) {
                fullPaint();
                needRepaint = false;
            } else {
                // There is a request to update the canvas
                // (likely after a Runnable was invoked),
                // but the last update was not consumed.
                // If there is a Runnable in the RunnableQueue,
                // we just skip this rendering update. Otherwise,
                // schedule a fake Runnable to force a later repaint.
                if (documentNode.getUpdateQueue().getSize() == 0) {
                    documentNode.getUpdateQueue()
                        .preemptLater(new Runnable() {
                                public void run() {
                                }
                            }, this);
                }
            }
        } 
    }

    /**
     * Utility method used to do a full repaint. This method should be called
     * from the update thread only.
     */
    protected void fullPaint() {
        synchronized (lock) {
            if (DirtyAreaManager.ON) {
                dirtyAreaManager.refresh(documentNode, rg, clearPaint);
            } else {
                rg.setRenderingTile(null);
                rg.setFill(clearPaint);
                rg.setTransform(null);
                rg.setFillOpacity(1);
                rg.fillRect(0, 0, documentNode.getWidth(), 
                            documentNode.getHeight(), 0, 0);
                documentNode.paint(rg);
            }
            canvasConsumed = false;
        }

        canvasUpdateListener.updateComplete(this);
    }

    /**
     * Sets the paint used to clear the canvas.
     *
     * @param clearPaint the new paint.
     */
    public void setClearPaint(final RGB clearPaint) {
        if (clearPaint == null) {
            throw new NullPointerException();
        }

        this.clearPaint = clearPaint;
        needRepaint = true;
    }

    /**
     * Turns off any rendering updates.
     */
    public void turnOff() {
        isOff = true;
    }

    /**
     * Turns rendering updates on.
     */
    public void turnOn() {
        isOff = false;
    }

    /**
     * @return true if the SimpleCanvasManager is currently bypassing canvas
     * updates.
     */
    public boolean isOff() {
        return isOff;
    }

    // ========================================================================
    // RunnableHandler implementation
    // ========================================================================

    /**
     * Called when the given Runnable has just been invoked and
     * has returned.
     * @param r the <code>Runnable</code> which just got executed
     * @param rq the <code>RunnableQueue</code> which executed the
     *        input <code>Runnable</code>
     */
    public void runnableInvoked(final RunnableQueue rq, final Runnable r) {
        if (!isOff) {
            updateCanvas();
        }
    }
}
