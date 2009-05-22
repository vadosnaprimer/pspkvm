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

import org.w3c.dom.DOMException;

/**
 * A <code>GenericElementNode</code> is used to model elements which are either
 * unknown to Perseus or have no special behavior or attributes.
 *
 * Note that <code>GenericElementNode</code> is needed because we want to avoid
 * storing a namespaceURI and localName references for nodes which always return
 * constants for these values. Another option was to have namespaceURI and
 * localName be attributes of <code>ElementNode</code>, but that would have
 * forced us to carry two extra references on all nodes, which is superfluous.
 *
 * @version $Id: GenericElementNode.java,v 1.3 2006/04/21 06:37:21 st125089 Exp $
 */
public class GenericElementNode extends ElementNode {
    /**
     * The element's namespace URI.
     */
    protected String namespaceURI;

    /**
     * The element's local name.
     */
    protected String localName;

    /**
     * The text content
     */
    protected String content;

    /**
     * Constructor, providing the element's namespace and local name
     *
     * @param namespaceURI the element's namespace. May be null.
     * @param localName the element's localName. Should not be null.
     * @param doc the <code>DocumentNode</code> this node belongs to. Should
     *        not be null.
     * @throws IllegalArgumentException if one of the arguments is null.
     */
    public GenericElementNode(final String namespaceURI,
                              final String localName,
                              final DocumentNode doc) {
        super(doc);

        if (localName == null) {
            throw new IllegalArgumentException();
        }

        this.namespaceURI = namespaceURI;
        this.localName = localName;
    }

    /**
     * @return the namespaceURI provided in the constructor.
     */
    public String getNamespaceURI() {
        return namespaceURI;
    }

    /**
     * @return the localName provided in the constructor.
     */

    public String getLocalName() {
        return localName;
    }

    /**
     * Used by <code>DocumentNode</code> to create a new instance from
     * a prototype <code>GenericElementNode</code>.
     *
     * @param doc the <code>DocumentNode</code> for which a new node is
     *        should be created.
     * @return a new <code>GenericElementNode</code> for the requested document.
     */
    public ElementNode newInstance(final DocumentNode doc) {
        return new GenericElementNode(namespaceURI, localName, doc);
    }

    /**
     * @param text the text to append to this node's content.
     *        If text is null or empty, this does nothing.
     */
    public void appendTextChild(final String text) {
        if (text == null || text.length() == 0) {
            return;
        }

        if (content == null) {
            setContent(text);
        } else {
            setContent(content + text);
        }
    }

    /**
     * @param newContent this node's new content string
     */
    public void setContent(final String newContent) {
        if (equal(newContent, content)) {
            return;
        }
        modifyingNode();
        this.content = newContent;
        modifiedNode();
    }

    /**
     * @return this node's text content as a string
     */
    public String getContent() {
        return content;
    }

    /**
     * Handles #text traits.
     *
     * @param name the requested trait's name (e.g., "#text")
     * @return the requested trait's string value (e.g., "Hello SVG Text")
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to a String (SVG Tiny only).
     */
    public String getTraitImpl(final String name)
        throws DOMException {
        if (SVGConstants.SVG_TEXT_PSEUDO_ATTRIBUTE == name) {
            if (content == null) {
                return "";
            }
            return getContent();
        } else {
            return super.getTraitImpl(name);
        }
    }

    /**
     * @param traitName the trait name.
     */
    TraitAnim createTraitAnimImpl(final String traitName) {
        if (SVGConstants.SVG_TEXT_PSEUDO_ATTRIBUTE == traitName) {
            return new StringTraitAnim(this, NULL_NS, traitName);
        } else {
            return super.createTraitAnimImpl(traitName);
        }
    }

    /**
     * Handles the #text traits.
     *
     * @param name the trait's name (e.g., "#text")
     * @param value the trait's value (e.g, "Hello SVG Text")
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if the requested
     * trait's value cannot be specified as a String
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is an invalid value for the given trait or null.
     * @throws DOMException with error code NO_MODIFICATION_ALLOWED_ERR: if
     * attempt is made to change readonly trait.
     */
    public void setTraitImpl(final String name, final String value)
        throws DOMException {
        if (SVGConstants.SVG_TEXT_PSEUDO_ATTRIBUTE == name) {
            if (value == null) {
                throw illegalTraitValue(name, value);
            }
            setContent(value);
        } else {
            super.setTraitImpl(name, value);
        }
    }


}
