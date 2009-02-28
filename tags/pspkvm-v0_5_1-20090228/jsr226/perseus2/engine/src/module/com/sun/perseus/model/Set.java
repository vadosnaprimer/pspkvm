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
 * <code>Set</code> represents an SVG Tiny <code>&lt;set&gt;</code>
 * element.
 *
 * @version $Id: Set.java,v 1.5 2006/06/29 10:47:34 ln156897 Exp $
 */
public final class Set extends Animation {
    /**
     * Required Set traits
     */
    static final String[] REQUIRED_TRAITS
        = {SVGConstants.SVG_ATTRIBUTE_NAME_ATTRIBUTE};

    /**
     * The target Set value.
     */
    String[] to = new String[1];

    /**
     * The RefValues corresponding to this &lt;set&gt; element. A &lt;set&gt;
     * element has a single segment with the same begin and end value.
     */
    RefValues refValues = null;

    /**
     * The trait's qualified name.
     */
    String traitQName = null;

    /**
     * Builds a new Set element that belongs to the given
     * document. This <code>Set</code> will belong 
     * to the <code>DocumentNode</code>'s time container.
     *
     * @param ownerDocument the document this node belongs to.
     * @throws IllegalArgumentException if the input ownerDocument is null
     */
    public Set(final DocumentNode ownerDocument) {
        super(ownerDocument, SVGConstants.SVG_SET_TAG);
    }

    /**
     * Used by <code>DocumentNode</code> to create a new instance from
     * a prototype <code>TimedElementNode</code>.
     *
     * @param doc the <code>DocumentNode</code> for which a new node is
     *        should be created.
     * @return a new <code>TimedElementNode</code> for the requested document.
     */
    public ElementNode newInstance(final DocumentNode doc) {
        return new Set(doc);
    }

    /**
     * Supported traits: to, attributeName
     *
     * @param traitName the name of the trait which the element may support.
     * @return true if this element supports the given trait in one of the
     *         trait accessor methods.
     */
    boolean supportsTrait(final String traitName) {
        if (SVGConstants.SVG_TO_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_ATTRIBUTE_NAME_ATTRIBUTE == traitName) {
            return true;
        } else {
            return super.supportsTrait(traitName);
        }
    }

    /**
     * @return an array of traits that are required by this element.
     */
    public String[] getRequiredTraits() {
        return REQUIRED_TRAITS;
    }

    /**
     * Set supports the to and attributeName traits.
     *
     * Returns the trait value as String. In SVG Tiny only certain traits can be
     * obtained as a String value. Syntax of the returned String matches the
     * syntax of the corresponding attribute. This element is exactly equivalent
     * to {@link org.w3c.dom.svg.SVGElement#getTraitNS getTraitNS} with
     * namespaceURI set to null.
     *
     * The method is meant to be overridden by derived classes. The 
     * implementation pattern is that derived classes will override the method 
     * and call their super class' implementation. If the ElementNode 
     * implementation is called, it means that the trait is either not supported
     * or that it cannot be seen as a String.
     *
     * @param name the requested trait name.
     * @return the trait value.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to a String (SVG Tiny only).
     */
    public String getTraitImpl(final String name)
        throws DOMException {
        if (SVGConstants.SVG_TO_ATTRIBUTE == name) {
            return to[0];
        } else if (SVGConstants.SVG_ATTRIBUTE_NAME_ATTRIBUTE == name) {
            return traitQName;
        } else {
            return super.getTraitImpl(name);
        }
    }

    /**
     * Set supports the attributeName and to traits.
     *
     * @param name the trait's name.
     * @param value the trait's value.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERR if the requested
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
        if (SVGConstants.SVG_TO_ATTRIBUTE == name) {
            checkWriteLoading(name);
            if (value == null) {
                throw illegalTraitValue(name, value);
            }
            to[0] = value;
        } else if (SVGConstants.SVG_ATTRIBUTE_NAME_ATTRIBUTE == name) {
            checkWriteLoading(name);

            if (value == null) {
                throw illegalTraitValue(name, value);
            }

            // Now, if this is a QName, we need to use the namespace prefix map.
            int i = value.indexOf(':');
            if (i == -1) {
                this.traitName = value.intern();
                this.traitQName = value.intern();
            } else {
                if (i == value.length() - 1) {
                    // ':' is the last character, this is invalid.
                    throw illegalTraitValue(name, value);
                }

                String prefix = value.substring(0, i);
                String tName = value.substring(i+1);

                String ns = ownerDocument.toNamespace(prefix, this);
                if (ns == null) {
                    throw illegalTraitValue(name, value);
                }

                traitName = tName.intern();
                traitNamespace = ns.intern();
                traitQName = value;
            }
        } else {
            super.setTraitImpl(name, value);
        }
    }

    /**
     * This is the Set element's animation function. It is very
     * simple as it simply returns the 'to' value, no matter
     * what time t is.
     *
     * @param t the animation's simple time.
     */
    Object[] f(final long t) {
        return refValues.getSegment(0).getStart();
    }

    /**
     * Validating a Set consists in:
     * a) Setting its target element. If there was no idRef, then targetElement
     *    is still null and will be positioned to the parent node.
     * b) Validating the to value with the targetElement, using the target trait
     *    name, namespace and value.
     *
     * @throws DOMException if there is a validation error, for example if the
     *         to value is incompatible with the target trait or if the target
     *         trait is not animatable.
     */
    void validate() throws DOMException {
        // a) Set the target element.
        if (targetElement == null) {
            targetElement = (ElementNode) parent;
        }

        // b) Validate the to value with the target element.
        traitAnim = targetElement.getSafeTraitAnimNS(traitNamespace, traitName);

        if (to[0] != null) {
            refValues = traitAnim.toRefValues(this,
                                              to,
                                              null,
                                              SVGConstants.SVG_TO_ATTRIBUTE);
        } else {
            hasNoEffect = true;
        }
    }

}
