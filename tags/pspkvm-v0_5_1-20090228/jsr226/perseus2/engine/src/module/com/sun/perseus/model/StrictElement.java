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

/**
 * The purpose of the <code>StrictElement</code> class is to have a way to 
 * create elements with a list of required attributes but no specific 
 * rendering behavior.
 *
 * @version $Id: StrictElement.java,v 1.2 2006/04/21 06:38:49 st125089 Exp $
 */
public class StrictElement extends ElementNode {
    /**
     * The set of required traits in the per-element partition namespace.
     */
    String[] requiredTraits;

    /**
     * The set of required namespaced traits.
     */
    String[][] requiredTraitsNS;

    /**
     * This node's local name.
     */
    String localName;

    /**
     * This node's namespace URI
     */
    String namespaceURI;

    /**
     * Constructs a new StrictElement with the requested list of required
     * attributes.
     *
     * @param doc the owner document.
     * @param localName the element's local name.
     * @param namespaceURI the element's namespace uri.
     * @param requiredTraits the list of required traits for the per-element
     *        partition namespace.
     * @param requiredTraitsNS the list of required namespaced traits.
     */
    public StrictElement(final DocumentNode doc,
                         final String localName,
                         final String namespaceURI,
                         final String[] requiredTraits,
                         final String[][] requiredTraitsNS) {
        super(doc);

        if (localName == null) {
            throw new IllegalArgumentException();
        }
        
        this.localName = localName;
        this.namespaceURI = namespaceURI;
        this.requiredTraitsNS = requiredTraitsNS;
        this.requiredTraits = requiredTraits;
    }

    /**
     * @return the namespace URI of the Node.
     *
     * @throws SecurityException if the application does not have the necessary
     * privilege rights to access this (SVG) content.
     */
    public String getNamespaceURI() {
        return namespaceURI;
    }

    /**
     * @return unprefixed node name. For an SVGElement, this returns the tag
     * name without a prefix.  In case of the Document node, string
     * <code>#document</code> is returned.
     * @throws SecurityException if the application does not have the necessary
     * privilege rights to access this (SVG) content.
     */

    public String getLocalName() {
        return localName;
    }

    /**
     * @return an array of traits that are required by this element.
     */
    public String[] getRequiredTraits() {
        return requiredTraits;
    }

    /**
     * @return an array of namespaceURI, localName trait pairs required by
     *         this element.
     */
    public String[][] getRequiredTraitsNS() {
        return requiredTraitsNS;
    }

    /**
     * Used by <code>DocumentNode</code> to create a new instance from
     * a prototype <code>StrictElement</code>.
     *
     * @param doc the <code>DocumentNode</code> for which a new node is
     *        should be created.
     * @return a new <code>StrictElement</code> for the requested document.
     */
    public ElementNode newInstance(final DocumentNode doc) {
        return new StrictElement(doc, 
                                 localName, 
                                 namespaceURI, 
                                 requiredTraits, 
                                 requiredTraitsNS);
    }


}
