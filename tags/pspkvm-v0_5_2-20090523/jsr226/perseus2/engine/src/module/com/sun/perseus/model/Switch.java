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

import com.sun.perseus.util.SVGConstants;

import com.sun.perseus.j2d.Transform;
import com.sun.perseus.j2d.Box;

/**
 * The <code>Switch</code> class is a simple extension of the 
 * <code>Group</code> class which stops rendering its children 
 * after one as rendered (i.e., the child's canRender method
 * returns true).
 * This implements the behavior required by the SVG Tiny
 * specification of the <code>&lt;switch&gt;</code> element.
 *
 * @version $Id: Switch.java,v 1.7 2006/06/29 10:47:35 ln156897 Exp $
 */
public class Switch extends Group {
    /**
     * Constructor.
     *
     * @param ownerDocument this element's owner <code>DocumentNode</code>
     */
    public Switch(final DocumentNode ownerDocument) {
        super(ownerDocument);
    }

    /**
     * @return the SVGConstants.SVG_SWITCH_TAG value
     */
    public String getLocalName() {
        return SVGConstants.SVG_SWITCH_TAG;
    }

    /**
     * Used by <code>DocumentNode</code> to create a new instance from
     * a prototype <code>Switch</code>.
     *
     * @param doc the <code>DocumentNode</code> for which a new node is
     *        should be created.
     * @return a new <code>Switch</code> for the requested document.
     */
    public ElementNode newInstance(final DocumentNode doc) {
        return new Switch(doc);
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
        // If a node does not render, it is never hit
        if (canRenderState != 0) {
            return null;
        }
        
        // Check for a hit on children
        ModelNode c = lastChild;
        while (c != null) {
            if ((c.canRenderState & CAN_RENDER_CONDITIONS_MET_BITS)
                == 0) {
                return c.nodeHitAt(pt);
            }
            c = c.prevSibling;
        }

        return null;
    }

    /**
     * Returns the <code>ModelNode</code>, if any, hit by the
     * point at coordinate x/y in the proxy tree starting at 
     * proxy.
     * 
     * @param pt the x/y coordinate. Should never be null and be
     *        of size two. If not, the behavior is unspecified.
     *        The coordinates are in viewport space.
     * @param proxy the root of the proxy tree to test.
     * @return the <tt>ModelNode</tt> hit at the given point or null
     *         if none was hit.
     */
    ModelNode proxyNodeHitAt(final float[] pt,
                             final ElementNodeProxy proxy) {
        // If a node does not render, it is never hit
        if (canRenderState != 0) {
            return null;
        }
        
        // Check for a hit on the proxy's expanded children
        ElementNodeProxy c = (ElementNodeProxy) proxy.getLastExpandedChild();
        while (c != null) {
            if ((c.proxied.canRenderState & CAN_RENDER_CONDITIONS_MET_BITS)
                == 0) {
                return c.nodeHitAt(pt);
            }
            c = (ElementNodeProxy) c.prevSibling;
        }

        return null;
    }

    /**
     * @param rg this method paints this node into the input 
     *        <tt>RenderGraphics</tt>
     */
    public void paint(final RenderGraphics rg) {
        if (canRenderState != 0) {
            return;
        }

        if (firstChild == null) {
            return;
        }

        //
        // Only go up to the first child which
        // renders
        //
        ElementNode c = firstChild;
        while (c != null) {
            // Paint child
            c.paint(rg);

            // Exit the loop if the child rendered
            if ((c.canRenderState & CAN_RENDER_CONDITIONS_MET_BITS)
                == 0) {
                break;
            }

            c = (ElementNode) c.nextSibling;
        }
    }

    /**
     * @param bbox the bounding box to which this node's bounding box should be
     *        appended. That bounding box is in the target coordinate space. It 
     *        may be null, in which case this node should create a new one.
     * @param t the transform to apply from the node's coordinate space to 
     *        the target coordinate space. May be null for the identity 
     *        transform.
     * @return the node's bounding box in the target coordinate space.
     */
    public Box addBBox(Box bbox, final Transform t) {
        ModelNode c = getFirstChildNode();
        while (c != null) {
            if ((c.canRenderState & CAN_RENDER_CONDITIONS_MET_BITS)
                == 0) {
                bbox = c.addBBox(bbox, c.appendTransform(t, null));
                break;
            }
            c = c.nextSibling;
        }

        return bbox;
    }


}
