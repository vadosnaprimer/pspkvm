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

import com.sun.perseus.util.SVGConstants;

import com.sun.perseus.j2d.RenderGraphics;

/**
 * <code>Defs</code> are used as placeholders where proxied <tt>ModelNode</tt>
 * can be stored for reference by <tt>ElementNodeProxy</tt> instances.
 * <br />
 * Note that according to the SVG 1.1 DTD, <tt>&lt;defs&gt;</tt> should be 
 * <tt>StructureNode</tt>.
 * However, we should not apply <tt>GraphicsNode</tt> and 
 * <tt>TextNode</tt> properties on <tt>&lt;defs&gt;</tt> as it 
 * is useless: <tt>&lt;defs&gt;</tt> content is never rendered directly, 
 * so properties set on defs are _never_ inherited by <tt>&lt;defs&gt;</tt> 
 * children. Therefore, having defs support graphics and text properties is 
 * an oversight in the SVG Tiny specification.
 *
 * Note: There is a JDTS test failure when defs extends ElementNode instead of
 *       StructureNode. This is caused by the fact that in that case it breaks
 *       an inheritance chain from a child to defs's parent.
 *          
 * @version $Id: Defs.java,v 1.3 2006/04/21 06:36:16 st125089 Exp $
 */
public class Defs extends StructureNode {
    /**
     * Constructor.
     *
     * @param ownerDocument this element's owner <code>DocumentNode</code>
     */
    public Defs(final DocumentNode ownerDocument) {
        super(ownerDocument);

        // do not render this node
        canRenderState |= CAN_RENDER_RENDERABLE_BIT;
    }

    /**
     * @return the SVGConstants.SVG_DEFS_TAG value
     */
    public String getLocalName() {
        return SVGConstants.SVG_DEFS_TAG;
    }

    /**
     * Used by <code>DocumentNode</code> to create a new instance from
     * a prototype <code>Defs</code>.
     *
     * @param doc the <code>DocumentNode</code> for which a new node is
     *        should be created.
     * @return a new <code>DefsNode</code> for the requested document.
     */
    public ElementNode newInstance(final DocumentNode doc) {
        return new Defs(doc);
    }
}
