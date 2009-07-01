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

import com.sun.perseus.platform.URLResolver;

import com.sun.perseus.util.SVGConstants;

import org.w3c.dom.DOMException;

/**
 * Represents an SVG Tiny <code>&lt;a&gt;</code> element.
 * An anchor is a simple <code>Group</code> extension which
 * simply has a href and a target attribute.
 * 
 * @version $Id: Anchor.java,v 1.4 2006/04/21 06:36:17 st125089 Exp $
 */
public class Anchor extends Group {
    /**
     * The anchor's hyperlink reference
     */
    protected String href = "";

    /**
     * The anchor's target
     */
    protected String target = "";

    /**
     * Constructor.
     *
     * @param ownerDocument this element's owner <code>DocumentNode</code>
     */
    public Anchor(final DocumentNode ownerDocument) {
        super(ownerDocument);
    }

    /**
     * @return the SVGConstants.SVG_A_TAG value
     */
    public String getLocalName() {
        return SVGConstants.SVG_A_TAG;
    }

    /**
     * Used by <code>DocumentNode</code> to create a new instance from
     * a prototype <code>AnchorNode</code>.
     *
     * @param doc the <code>DocumentNode</code> for which a new node is
     *        should be created.
     * @return a new <code>Anchor</code> for the requested document.
     */
    public ElementNode newInstance(final DocumentNode doc) {
        return new Anchor(doc);
    }

    /**
     * This returns the <b>absolute</b> URI, even though
     * the href may have been a relative URI
     *
     * @return this anchor's href, as an absolute URL or
     *         null if the href set was null or if the 
     *         absolute URL could not be computed.
     */
    public String getHref() {
        String uriBase = getURIBase();
        String docBase = ownerDocument.getURIBase();
        if (uriBase != null 
            && uriBase.equals(docBase) 
            && href != null 
            && href.length() > 0 
            && href.charAt(0) == '#') {
            // IMPORTANT: this prevents prepending the document URI
            // to a relative URI if the reference is a local one.
            // See:
            // http://www.ietf.org/rfc/rfc2396.txt
            // Paragraph 4.2 Same-Document References.
            uriBase = null;
        }
        try {
            if (uriBase != null) {
                return URLResolver.resolve(uriBase, href);
            } else {
                return href;
            }
        } catch (IllegalArgumentException iae) {
            return null;
        }
    }

    /**
     * @param href the new anchor's href
     */
    public void setHref(final String href) {
        if (href == null) {
            throw new IllegalArgumentException();
        }

        if (href.equals(this.href)) {
            return;
        }

        modifyingNode();
        this.href = href;
        modifiedNode();
    }

    /**
     * @return the anchor's target
     */
    public String getTarget() {
        return target;
    }

    /**
     * @param target the new anchor target. Should not be null.
     */
    public void setTarget(final String target) {
        if (target == null) {
            throw new IllegalArgumentException();
        }

        if (target.equals(this.target)) {
            return;
        }

        modifyingNode();
        this.target = target;
        modifiedNode();
    }

    /**
     * Anchor handles the target trait.
     *
     * @param traitName the name of the trait which the element may support.
     * @return true if this element supports the given trait in one of the
     *         trait accessor methods.
     */
    boolean supportsTrait(final String traitName) {
        if (SVGConstants.SVG_TARGET_ATTRIBUTE == traitName) {
            return true;
        } else {
            return super.supportsTrait(traitName);
        }
    }

    /**
     * Supported traits: xlink:href
     *
     * @param namespaceURI the trait's namespace.
     * @param traitName the name of the trait which the element may support.
     * @return true if this element supports the given trait in one of the
     *         trait accessor methods.
     */
    boolean supportsTraitNS(final String namespaceURI,
                            final String traitName) {
        if (SVGConstants.XLINK_NAMESPACE_URI == namespaceURI
            &&
            SVGConstants.SVG_HREF_ATTRIBUTE == traitName) {
            return true;
        } else {
            return super.supportsTraitNS(namespaceURI, traitName);
        }
    }

    /**
     * Anchor handles the target trait.
     *
     * @param name the requested trait name
     * @return the requested trait's value.
     * 
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to a String (SVG Tiny only).
     */
    public String getTraitImpl(final String name)
        throws DOMException {
        if (SVGConstants.SVG_TARGET_ATTRIBUTE == name) {
            return getTarget();
        } else {
            return super.getTraitImpl(name);
        }
    }

    /**
     * Anchor handles the xlink:href attribute
     *
     * @param namespaceURI the URI for the requested trait.
     * @param name the requested trait's local name (i.e., un-prefixed).
     * 
     * @return the requested trait's value, as a string.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to a String (SVG Tiny only).
     * @throws SecurityException if the application does not have the necessary
     * privilege rights to access this (SVG) content.
     */
    String getTraitNSImpl(final String namespaceURI, 
                          final String name)
        throws DOMException {
        if (SVGConstants.XLINK_NAMESPACE_URI == namespaceURI
            &&
            SVGConstants.SVG_HREF_ATTRIBUTE == name) {
            String res = getHref();
            if (res == null) {
                res = href;
            }

            return res;
        } else {
            return super.getTraitNSImpl(namespaceURI, name);
        }
    }

    /**
     * Anchor handles the target trait.
     *
     * @param name the name of the trait to set.
     * @param value the value of the trait to set.
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
    public void setTraitImpl(final String name, 
                         final String value)
        throws DOMException {
        if (SVGConstants.SVG_TARGET_ATTRIBUTE == name) {
            if (value == null) {
                throw illegalTraitValue(name, value);
            }
            setTarget(value);
        } else {
            super.setTraitImpl(name, value);
        }
    }

    /**
     * Anchor supports the xlink:href trait.
     *
     * @param namespaceURI the URI for the trait's namespace.
     * @param name the trait's local name (i.e., un-prefixed).
     * @param value the trait's value.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if the requested
     * trait's value cannot be specified as a String
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is an invalid value for the given trait or null.
     * @throws DOMException with error code NO_MODIFICATION_ALLOWED_ERR: if
     * attempt is made to change readonly trait.
     * @throws SecurityException if the application does not have the necessary
     * privilege rights to access this (SVG) content.
     */

    public void setTraitNSImpl(final String namespaceURI, 
                               final String name, 
                               final String value)
        throws DOMException {
        try {
            if (SVGConstants.XLINK_NAMESPACE_URI == namespaceURI
                &&
                SVGConstants.SVG_HREF_ATTRIBUTE == name) {
                setHref(value);
            } else {
                super.setTraitNSImpl(namespaceURI, name, value);
            }
        } catch (IllegalArgumentException iae) {
            throw new DOMException(DOMException.INVALID_ACCESS_ERR,
                                   iae.getMessage());
        }
    }

    /**
     * @param traitName the trait name.
     */
    TraitAnim createTraitAnimImpl(final String traitName) {
        if (SVGConstants.SVG_TARGET_ATTRIBUTE == traitName) {
            return new StringTraitAnim(this, NULL_NS, traitName);
        } else {
            return super.createTraitAnimImpl(traitName);
        }
    }

    /**
     * @param traitName the trait name.
     * @param traitNamespace the trait's namespace. Should not be null.
     */
    TraitAnim createTraitAnimNSImpl(final String traitNamespace, 
                                    final String traitName) {
        if (traitNamespace == SVGConstants.XLINK_NAMESPACE_URI
            &&
            traitName == SVGConstants.SVG_HREF_ATTRIBUTE) {
            return new StringTraitAnim(this, traitNamespace, traitName);
        }

        return super.createTraitAnimNSImpl(traitNamespace, traitName);
    }

    public String toString() {
        return "Anchor[href=(" + href + ") absolute href=(" + getHref() + ")]";
    }
}
