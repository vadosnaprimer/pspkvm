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

import org.w3c.dom.svg.SVGRect;

import com.sun.perseus.j2d.Transform;

/**
 * A <code>UseProxy</code> proxies a <code>Use</code> node and 
 * computes the expanded content from the proxied use's children
 * and its proxy expanded child.
 * 
 * <p>A <code>UseProxy</code> has a first expanded child corresponding
 * to the proxied <code>Use</code>'s proxy content. Follow on expanded
 * children come from the <code>Use</code>'s children.</p>
 *
 * @version $Id: UseProxy.java,v 1.6 2006/06/29 10:47:36 ln156897 Exp $
 */
public class UseProxy extends StructureNodeProxy {
    /**
     * @param proxiedUse <code>Use</code> node to proxy
     */
    UseProxy(final Use proxiedUse) {
        super(proxiedUse);
    }

    /**
     * Called by the proxied node when the given property's computed value has
     * changed.
     *
     * @param propertyIndex index for the property whose value is changing.
     * @param proxiedComputedValue computed value for the proxied node.
     * 
     */
    protected void proxiedPropertyStateChange(
            final int propertyIndex,
            final Object proxiedComputedValue) {
        Object inheritValue = proxiedComputedValue;
        if (!((CompositeGraphicsNode) proxied).isInherited(propertyIndex)) {
            // The property is specified on the proxied node, update the 
            // state with that specified value.
            setPropertyState(propertyIndex, proxiedComputedValue);
        } else {
            // The property is unspecified on the proxied node. Inherit from
            // the proxy's parent (and not the proxied's parent).
            inheritValue = getInheritedPropertyState(propertyIndex);
            setPropertyState(propertyIndex, inheritValue);
        }

        // Propagate to children.
        ModelNode c = firstExpandedChild;
        while (c != null) {
            propagatePropertyState(propertyIndex, inheritValue);
            c = c.nextSibling;
        }
    }

    /**
     * Disallow proxying of anything else than <code>Use</code> nodes.
     *
     * @param newProxied this node's new proxied node
     * @throws IllegalArgumentException if the input new proxy is not
     *         a <code>Use</code> node.
     * @see ElementNodeProxy#setProxied
     */
    protected void setProxied(final ElementNode newProxied) {
        if (newProxied != null && !(newProxied instanceof Use)) {
            throw new IllegalArgumentException();
        }

        super.setProxied(newProxied);
    }

    /**
     * @return the tight bounding box in current user coordinate
     * space. 
     */
    public SVGRect getBBox() {
        Transform t = null;
        Use proxiedUse = (Use) proxied;
        
        if (proxiedUse != null && (proxiedUse.x != 0 || proxiedUse.y != 0)) {
            t = new Transform(1, 0, 0, 1, proxiedUse.x, proxiedUse.y);
        }
        
        return addBBox(null, t);
    }

    /**
     * Expand the content. For a use node, we create additional expanded 
     * content to account for the use proxy.
     */
    protected void expand() {
        if (expanded) {
            return;
        }
        if (proxied != null) {
            //
            // The first expanded content comes from the proxied <use>'s
            // proxy.
            // At the end of this first computation, the firstExpandedChild
            // points to a proxy for the <use>'s proxy or is null and the
            // lastExpandedChild is equal to the firstExpandedChild
            //
            Use proxiedUse = (Use) proxied;
            if (proxiedUse.proxy != null) {
                firstExpandedChild = buildProxiedProxy();
            } else {
                firstExpandedChild = null;
            }
            
            lastExpandedChild = firstExpandedChild;

            //
            // Now, do the regular expansion, in case the <use> element
            // has regular element children.
            //
            ElementNodeProxy firstRegularProxies =
                computeProxiesChain((ElementNode) proxied.getFirstChildNode());

            if (firstRegularProxies != null) {
                lastExpandedChild = firstRegularProxies.prevSibling;
                if (firstExpandedChild == null) {
                    // The proxied <use> has no proxy
                    firstExpandedChild = firstRegularProxies;
                    firstRegularProxies.prevSibling = null;
                } else {
                    // The proxied <use> had a proxy
                    firstExpandedChild.nextSibling = firstRegularProxies;
                    firstRegularProxies.prevSibling = firstExpandedChild;
                }
            }

        } 
        
        expanded = true;
    }

    /**
     * Notifies this proxy that the proxied Use's proxy has just been set.
     */
    void useProxySet() {
        if (!expanded) {
            expand();
        } else {
            ElementNodeProxy newFirstExpandedChild 
                = buildProxiedProxy();
            newFirstExpandedChild.nextSibling = firstExpandedChild;
            if (firstExpandedChild != null) {
                firstExpandedChild.prevSibling = newFirstExpandedChild;
            }
            firstExpandedChild = newFirstExpandedChild;
            if (lastExpandedChild == null) {
                lastExpandedChild = firstExpandedChild;
            }
        }
    }

    /**
     * Implementation helper. This handles getting the proxy from the 
     * proxied <code>Use</code>'s proxy's proxied (see comments in the
     * code).
     *
     * @return an expanded <code>ElementNodeProxy</code> for the proxied 
     *         <code>Use</code>'s proxy.
     */
    ElementNodeProxy buildProxiedProxy() {
        // 
        // Here we are in a situation like:
        //
        // + u -> u2 -> rect
        //
        // Which we expand into
        //
        // + rect
        // + u2
        //    +~~> ElementNodeProxy(rect)
        // + u
        //    +~~> UseProxy(u2)
        //         +~~> ElementNodeProxy(rect)
        //
        // To understand the following code, we are building
        // the equivalent of UseProxy(u2)
        //
        // proxied.expandedContent[0] is a reference to 
        // u2's ElementNodeProxy(rect)
        //
        Use proxiedUse = (Use) proxied;
        ElementNodeProxy proxy
            = proxiedUse.proxy.getProxied().buildExpandedProxy();
        proxy.setParentQuiet(this);
        return proxy;
    }
}
