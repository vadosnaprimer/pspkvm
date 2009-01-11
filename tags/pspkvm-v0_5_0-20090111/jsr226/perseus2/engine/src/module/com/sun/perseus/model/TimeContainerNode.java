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

/**
 * <code>TimedElementNode</code> models <code>ModelNodes</code which 
 * represent Timed Elements.
 *
 *
 * @version $Id: TimeContainerNode.java,v 1.3 2006/06/29 10:47:35 ln156897 Exp $
 */
public class TimeContainerNode extends TimedElementNode {
    /**
     * The timing support is added by composition rather than inheritance
     * because Java only allows single inheritance
     */
    protected TimeContainerSupport timeContainerSupport;

    /**
     * Builds a new time container child of the <code>DocumentNode</code> root
     * time container.
     *
     * @param ownerDocument the document this node belongs to.
     * @throws IllegalArgumentException if the input ownerDocument is null
     */
    public TimeContainerNode(final DocumentNode ownerDocument) {
        super(ownerDocument, new TimeContainerSupport(), 
              SVGConstants.SVG_PAR_TAG);

        timeContainerSupport = (TimeContainerSupport) timedElementSupport;
    }

    /**
     * Used by <code>DocumentNode</code> to create a new instance from
     * a prototype <code>TimeContainerNode</code>.
     *
     * @param doc the <code>DocumentNode</code> for which a new node is
     *        should be created.
     * @return a new <code>TimeContainerNode</code> for the requested document.
     */
    public ElementNode newInstance(final DocumentNode doc) {
        return new TimeContainerNode(doc);
    }


}
