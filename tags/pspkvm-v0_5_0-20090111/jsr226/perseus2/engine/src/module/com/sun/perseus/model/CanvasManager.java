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

import com.sun.perseus.j2d.RenderContext;
import com.sun.perseus.j2d.RenderGraphics;

import java.io.InputStream;

import com.sun.perseus.util.RunnableQueue;
import com.sun.perseus.util.RunnableQueue.RunnableHandler;

import com.sun.perseus.j2d.RGB;
import com.sun.perseus.j2d.Transform;

/**
 * <p>The <code>CanvasManager</code> class is responsible for
 * keeping the rendering of a <code>ModelNode</code> tree on a 
 * <code>RenderGraphics</code> current.</p>
 * 
 * <p>Specifically, the <code>CanvasManager</code> listens to 
 * update events in a <code>ModelNode</code> tree and 
 * triggers repaint into the <code>RenderGraphics</code> when
 * necessary.</p>
 * 
 * <p>The <code>CanvasManager</code> optimizes rendering
 * of the tree while the document is in loading phase.</p>
 *
 * @version $Id: CanvasManager.java,v 1.17 2006/07/13 00:55:57 st125089 Exp $
 */
public class CanvasManager extends SimpleCanvasManager {
    /**
     * True while the component is processing a document 
     * which is in the loading phase, i.e., between
     * the <code>UpdateListener</code>'s <code>loadStarting</code>
     * and <code>loadComplete</code> calls.
     */
    protected boolean loading;

    /**
     * Progressive painting is needed when a node has
     * started loading and has been inserted into the tree.
     * This is only used during the loading phase of 
     * a document when doing progressive rendering.
     * The next node to paint progressively
     */
    protected ModelNode progressiveNode = null;

    /**
     * Tracks the highest level node whose load completion
     * is needed to proceed with progressive rendering.
     * When loading this node completes, then the node
     * is painted.
     */
    protected ModelNode needLoadNode = null;

    /**
     * The associated SMILSampler, if animations are run.
     */
    protected SMILSample sampler = null;

    /**
     * The rate for SMIL animation. The smilRate is the minimum time between
     * SMIL samples.
     */
    protected long smilRate = 40;

    /**
     * @param rg the <code>RenderGraphics</code> which this 
     *        instance will keep up to date with the 
     *        model changes.
     * @param documentNode the <code>DocumentNode</code>, root of the 
     *        tree that this <code>CanvasManager</code> will
     *        draw and keep current on the <code>RenderGraphics</code>
     * @param canvasUpdateListener the <code>CanvasUpdateListener</code>
     *        which listens to completed updates on the associated
     *        <code>RenderGraphics</code>
     *
     * @throws IllegalArgumentException if rg, documentNode or listener is null.
     */
    public CanvasManager(final RenderGraphics rg,
                         final DocumentNode documentNode,
                         final CanvasUpdateListener canvasUpdateListener) {
        super(rg, documentNode, canvasUpdateListener);
    }

    /**
     * Invoked when a node has been inserted into the tree
     *
     * @param node the newly inserted node
     */
    public void nodeInserted(final ModelNode node) {
        if (loading) {
            if (needLoadNode == null) {
                // Progressive rendering is _not_ suspended

                // If this node's parent is already loaded,
                // it means we are dealing with a node insertion
                // resulting from reference resolution. We need
                // to repaint the document in its current state.
                if (node.parent.loaded) {
                    fullPaint();
                } else {
                    // Check if this node suspends progressive
                    // rendering.
                    if (!node.getPaintNeedsLoad()) {
                        if (progressiveNode != null) {
                            needRepaint = true;
                        } else {
                            progressiveNode = node;
                        }
                    } else {
                        needLoadNode = node;
                    }
                }
            } else {
                // Progressive rendering _is_ suspended

                // We are loading a document and progressive 
                // repaint is disabled. However, the newly 
                // inserted node might be a ElementNodeProxy
                // child of a Use element which is referencing
                // content under the current needLoadNode. 
                // In that situation, we need to do a repaint
                // of the document up to, but not including
                // the needLoadNode.
                ModelNode parent = node;
                while (parent != null) {
                    if (parent == needLoadNode) {
                        // We are under the disabled node, no
                        // problem
                        break;
                    }
                    parent = parent.parent;
                }
                if (parent == null) {
                    // Re-render the document up to the current
                    // needLoadNode
                    needRepaint = true;
                }
            }
        } else {
            needRepaint = true;
        }
    }

    /**
     * @param node the node to test.
     * @return true if <code>node</code> is
     * a chid of the node currently holding up progressive
     * rendering. The caller must make sure <code>needNodeLoad</code>
     * is not null before calling this utility method. If called
     * when <code>needLoadNode</code> is null, the method returns 
     * true.
     */
    boolean isNeedLoadNodeOrChild(final ModelNode node) {
        ModelNode parent = node;

        while (parent != null) {
            if (parent == needLoadNode) {
                break;
            }
            parent = parent.parent;
        }

        if (parent == null) {
            return false;
        }

        return true;
    }

    /**
     * Invoked when a node is about to be modified. 
     *
     * @param node the node which is about to be modified
     */
    public void modifyingNode(final ModelNode node) {
        if (!isNeedLoadNodeOrChild(node)
            &&
            ((node.hasNodeRendering() || node.hasDescendants())
             && (node.canRenderState == 0))) {
            needRepaint = true;
        }
    }

    /**
     * Invoked when a node modification completed.
     *
     * @param node the node which was just modified.
     */
    public void modifiedNode(final ModelNode node) {
        if (!loading) {
            if (!needRepaint 
                &&
                (node.hasNodeRendering() 
                 || 
                 node.hasDescendants())) {
                needRepaint = true;
            }
        } else {
            // Ignore modifications on nodes which have no 
            // rendering and no descendants
            if (!node.hasNodeRendering() && !node.hasDescendants()) {
                return;
            }

            // We are doing progressive rendering. Check if
            // the modified node is the one currently suspended
            // or one of its children.
            // Modifications will be picked up when we 
            // paint the node after it has finished 
            // loading.
            if (needLoadNode != null) {
                if (node == needLoadNode) {
                    return;
                } else {
                    ModelNode parent = node.parent;
                    while (parent != null) {
                        if (parent == needLoadNode) {
                            return;
                        }
                        parent = parent.parent;
                    }
                    needRepaint = true;
                }
            } else {
                if (!needRepaint) {
                    // We modified a node which did not have node 
                    // rendering. 
                    if (progressiveNode != null && progressiveNode != node) {
                        needRepaint = true;
                    }
                    progressiveNode = node;
                }
            }
        }
    }

    /**
     * Invoked when the input node has finished loading. 
     *
     * @param node the <code>node</code> for which loading
     *        is complete.
     */
    public void loadComplete(final ModelNode node) {
        // System.err.println(">>>>>>>>>>>>>> loadComplete : " + node);
        if (node instanceof DocumentNode) {
            // We are finished with the loading phase.
            // Progressive rendering can stop
            loading = false;
            canvasUpdateListener.initialLoadComplete(null);

            // At this point, we are ready to start the animation loop.
            // We set the document's scheduled Runnable.

            // IMPL NOTE : We disable animations if there are no initial animations.
            // We should really only sample when there are 
            // active animations, but animations can be added by scripts, so
            // we will need a more sophisticated mechanism.
            if (documentNode.updateQueue != null 
                && 
                documentNode.timeContainerRootSupport
                .timedElementChildren.size() > 0) {
                SMILSample.DocumentWallClock clock 
                    = new SMILSample.DocumentWallClock(documentNode);
                sampler 
                    = new SMILSample(documentNode,
                                     clock);
                documentNode.updateQueue.scheduleAtFixedRate(sampler, this, 
                                                             smilRate);
                documentNode.timeContainerRootSupport.initialize();
                clock.start();
            }
        } else if (node == needLoadNode) {
            // We loaded a node fully. We can now display that
            // node and its children and proceed with progressive
            // rendering
            if (progressiveNode != null) {
                throw new Error();
            }
            progressiveNode = node;
            needLoadNode = null;
        }
        updateCanvas();
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
        loading = false;
        canvasUpdateListener.initialLoadComplete(error);
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
        loading = true;
    }

    /**
     * Invoked when the input node has started loading
     *
     * @param node the <code>ModelNode</code> for which loading
     *        has started.
     */
    public void loadBegun(final ModelNode node) {
        updateCanvas();
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
     * @return the associated SMILSampler, if animations are run.
     */
    public SMILSample getSampler() {
        return sampler;
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
        if (!loading) {
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
        } else {
            if (needRepaint) {
                // A full repaint was requested. If there is no
                // suspended node, just do a full repaint.
                // Otherwise, do a partial paint
                if (needLoadNode == null) {
                    fullPaint();
                } else {
                    partialPaint(documentNode);
                    canvasUpdateListener.updateComplete(this);
                }
            } else if (progressiveNode != null) {
                progressivePaint(progressiveNode);
            }
            needRepaint = false;
            progressiveNode = null;
        }
    }

    /**
     * Utility method invoked when an incremental painting is needed
     * on a node. This may be invoked when a node was just inserted
     * into the tree or when a node which required full loading of
     * its children has been completely loaded.
     *
     * @param node the node to paint incrementally on the canvas
     */
    protected void progressivePaint(final ModelNode node) {
        // If this node already has children, we need to do a fullNodePaint.
        // This happens for the <use> element when the <use> references
        // an element which appeared before in the document.
        if (node.hasDescendants()) {
            fullNodePaint(node);
        } else if (node.hasNodeRendering()
                   && (node.canRenderState == 0)) {
            synchronized (lock) {
                if (!canvasConsumed) {
                    try {
                        lock.wait();
                    } catch (InterruptedException ie) {
                    }
                }
                node.paint(rg);
                canvasConsumed = false;
                canvasUpdateListener.updateComplete(this);
            }
        }
    }

    /**
     * Utility method invoked when a node and its children need
     * to be painted. This is used, for example, when a node
     * which requires full loading before rendering is finally
     * fully loaded.
     * 
     * @param node the node to paint fully, i.e, including its 
     *        children.
     */
    protected void fullNodePaint(final ModelNode node) {
        if (node.canRenderState == 0) {
            synchronized (lock) {
                if (!canvasConsumed) {
                    try {
                        lock.wait();
                    } catch (InterruptedException ie) {
                    }
                }
                node.paint(rg);
                canvasConsumed = false;
                canvasUpdateListener.updateComplete(this);
            }
        }
    }

    /**
     * Utility method to paint the input tree up to, but not
     * including the needLoadNode. This is a recursive method
     * which should be called with the root of the tree to 
     * be painted.
     *
     * @param node the node to paint next.
     */
    protected void partialPaint(final ModelNode node) {
        if (node == needLoadNode
            ||
            (node.canRenderState != 0)) {
            return;
        }
        
        if (node.hasNodeRendering()) {
            synchronized (lock) {
                node.paint(rg);
            }
        } else {
            ModelNode child = node.getFirstExpandedChild();
            while (child != null) {
                partialPaint(child);
                child = child.nextSibling;
            }
            
            child = node.getFirstChildNode();
            while (child != null) {
                partialPaint(child);
                child = child.nextSibling;
            }
        }
    }
}
