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

import com.sun.perseus.j2d.Box;
import com.sun.perseus.j2d.Tile;
import com.sun.perseus.j2d.Transform;

/**
 * A <code>ElementNodeProxy</code> delegates its rendering to a 
 * proxied <code>ElementNode</code> object. This class is used to 
 * model expanded content. SVG markup defines content that needs to 
 * be expanded before it is rendered. For example, the <code>&lt;use&gt;</code>
 * element is expanded with <code>ElementNodeProxy</code> children 
 * to represent the expansion expressed by the element. 
 *
 * @version $Id: ElementNodeProxy.java,v 1.14 2006/06/29 10:47:30 ln156897 Exp $
 */
public  class ElementNodeProxy extends ModelNode {
    /**
     * The proxied ModelNode
     */
    protected ElementNode proxied;

    /**
     * Controls whether content is expanded or not
     */
    protected boolean expanded;

    /**
     * The next proxy, if any.
     */
    protected ElementNodeProxy nextProxy;

    /**
     * The previous proxy, if any.
     */
    protected ElementNodeProxy prevProxy;

    /**
     * The first expanded child, if content has been expanded.
     */
    protected ModelNode firstExpandedChild;

    /**
     * The last expanded child, if content has been expanded
     */
    protected ModelNode lastExpandedChild;

    /**
     * @param proxiedNode <tt>ElementNode</tt> to proxy
     */
    protected ElementNodeProxy(final ElementNode proxiedNode) {
        super();
        this.proxied = proxiedNode;
        proxiedNode.addProxy(this);
        ownerDocument = proxiedNode.ownerDocument;
    }

    /**
     * When a CompositeNode is hooked into the document tree, by default,
     * it notifies its children and calls its own nodeHookedInDocumentTree
     * method.
     */
    final void onHookedInDocumentTree() {
        super.onHookedInDocumentTree();

        ModelNode c = firstExpandedChild;
        while (c != null) {
            c.onHookedInDocumentTree();
            c = c.nextSibling;
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
        clearLayouts(firstExpandedChild);
    }

    /**
     * @return a reference to the node's first child, or null if there
     *         are no children.
     */
    public ModelNode getFirstChildNode() {
        return null;
    }

    /**
     * @return a reference to the node's last child, or null if there
     *         are no children.
     */
    public ModelNode getLastChildNode() {
        return null;
    }

    /**
     * Does nothing, as there are no children.
     */
    protected void unhookChildrenQuiet() {
    }

    /**
     * Some node types (such as <code>ElementNodeProxy</code>) have
     * expanded children that they compute in some specific
     * way depending on the implementation.     
     *
     * @return a reference to the node's first expanded child, or null if there
     *         are no expanded children. This forces the computation of expanded
     *         content if needed.
     */
    public ModelNode getFirstExpandedChild() {
        expand();
        return firstExpandedChild;
    }

    /**
     * Some node types (such as <code>ElementNodeProxy</code>) have
     * expanded children that they compute in some specific
     * way depending on the implementation.     
     *
     * @return a reference to the node's first expanded child, or null if there
     *         are no expanded children. 
     */
    public ModelNode getFirstComputedExpandedChild() {
        return firstExpandedChild;
    }

    /**
     * Some node types (such as <code>ElementNodeProxy</code>) have
     * expanded children that they compute in some specific
     * way depending on the implementation.     
     *
     * @return a reference to the node's last expanded child, or null if there
     *         are no expanded children. This forces the computation of expanded
     *         content if needed.
     */
    public ModelNode getLastExpandedChild() {
        expand();
        return lastExpandedChild;
    }

    /**
     * Utility method. Unhooks the expanded content.
     */
    protected void unhookExpandedQuiet() {
        unhookQuiet(firstExpandedChild);
        firstExpandedChild = null;
        lastExpandedChild = null;
        expanded = false;
    }

    /**
     * @return true if the ElementNodeProxy has children or if it has
     *         expanded content.
     */
    public boolean hasDescendants() {
        if (super.hasDescendants()) {
            return true;
        } else {
            expand();
            return firstExpandedChild != null;
        }
            
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
        return proxied.proxyNodeHitAt(pt, this);
    }

    /**
     * Appends the proxied node's transform, if there is a proxied node.
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
        if (proxied != null) {
            return proxied.appendTransform(tx, workTx);
        }

        return tx;
    }

    /**
     * @return a reference to the proxied <code>ModelNode</code>
     */
    public ElementNode getProxied() {
        return proxied;
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

        // We call modifyingNode because this node's rendering
        // may change as a result of changing the proxy.
        modifyingNode();

        if (this.proxied != null) {
            this.proxied.removeProxy(this);
        }

        unhookQuiet(firstExpandedChild);

        this.proxied = newProxied;
        firstExpandedChild = null;
        lastExpandedChild = null;
        expanded = false;

        if (newProxied != null) {
            newProxied.addProxy(this);
        }

        // Initialize the requiredFeatures/requiredExtensions/systemLanguage
        // bits to the same value as the proxied node.
        int oldCanRenderState = canRenderState;
        canRenderState &= CAN_RENDER_REQUIRED_FEATURES_MASK;
        canRenderState &= CAN_RENDER_REQUIRED_EXTENSIONS_MASK;
        canRenderState &= CAN_RENDER_SYSTEM_LANGUAGE_MASK;
        
        if (newProxied != null) {
            canRenderState |= (newProxied.canRenderState 
                                & CAN_RENDER_REQUIRED_FEATURES_BIT);
            canRenderState |= (newProxied.canRenderState 
                                & CAN_RENDER_REQUIRED_EXTENSIONS_BIT);
            canRenderState |= (newProxied.canRenderState 
                                & CAN_RENDER_SYSTEM_LANGUAGE_BIT);        
        }

        propagateCanRenderState(oldCanRenderState, canRenderState);
        
        // We call modifiedNode because this node's rendering
        // may have changed as a result of changing the proxy.
        modifiedNode();
    }

    /**
     * Expand the content. This is done lazilly
     */
    protected void expand() {
        if (expanded) {
            return;
        }

        // Expand proxy tree. 
        //
        // NOTE: This implements the SVGElementInstance tree structure,
        // as described in the SVG 1.1 specification, section 5.17.
        //
        if (proxied != null) {
            firstExpandedChild = computeProxiesChain(
                    (ElementNode) proxied.getFirstChildNode());

            if (firstExpandedChild != null) {
                lastExpandedChild
                    = firstExpandedChild.prevSibling;

                // The prevSibling was set as a way to return both the first
                // and last element of the chain. We need to break the circular
                // reference.
                firstExpandedChild.prevSibling = null;
            } else {
                firstExpandedChild = null;
            }
        } 

        expanded = true;
    }

    /**
     * Proxied nodes should call this method when they are being modified.
     */
    public void modifyingProxied() {
        modifyingNode();
    }

    /**
     * Proxied nodes should call this method when they have been modified.
     */
    public void modifiedProxied() {
        modifiedNode();
    }

    /**
     * Proxied nodes should call this method they got a new added child.
     * This is an optimization of the more generic insertion
     * case. Appending a child is a recursive process which avoids
     * recomputing all the proxies recursively (a proxy referencing a proxy
     * referencing a proxy .... referencing a composite on which nodes are
     * appended). It might be advantageous to consider doing a generic 
     * optimized insertion into the children list.
     *
     * @param child the <code>ElementNode</code> which was just added under
     *        the proxied node.
     * @see #proxiedExpandedChildAdded
     */
    public void proxiedChildAdded(final ElementNode child) {
        // If this node is not expanded at all, expand it now
        if (!expanded) {
            expand();
        } else {
            ElementNodeProxy newChildProxy = child.buildProxy();

            if (firstExpandedChild == null) {
                firstExpandedChild = newChildProxy;
                lastExpandedChild = newChildProxy;
                newChildProxy.nextSibling = null;
                newChildProxy.prevSibling = null;
            } else {
                lastExpandedChild.nextSibling = newChildProxy;
                newChildProxy.nextSibling = null;
                newChildProxy.prevSibling = lastExpandedChild;
                lastExpandedChild = newChildProxy;
            }

            newChildProxy.setParentQuiet(this);
            newChildProxy.expand();
            nodeInserted(newChildProxy);
        }
    }

    /**
     * Implementation helper: computes the set of chained proxies
     * for the input node and return the head of the chain.
     *
     * @param proxiedChild the <code>ElementNode</code> for which the chain of 
     *        proxies should be computed.
     *
     * @return the head of the proxies chaing. <b>NOTE</b> that the prevSibling
     *         is set on the head to point to the last element of the chain,
     *         creating a circular list for the 'prevSibling' reference. This
     *         circular reference should be broken by the code using this 
     *         method.
     */
    protected ElementNodeProxy computeProxiesChain(ElementNode proxiedChild) {
        ElementNodeProxy firstProxy = null;
        ElementNodeProxy proxy = null;
        ElementNodeProxy previousProxy = null;
        while (proxiedChild != null) {
            proxy = proxiedChild.buildProxy();
            proxy.setParentQuiet(this);
            proxy.expand();
            if (previousProxy == null) {
                firstProxy = proxy;
            } else {
                previousProxy.nextSibling = proxy;
                proxy.prevSibling = previousProxy;
            }
            firstProxy.prevSibling = proxy;
            previousProxy = proxy;
            proxiedChild = (ElementNode) proxiedChild.nextSibling;
        }
        return firstProxy;
    }

}
