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

import java.util.Vector;

/**
 * <code>Animate</code> represents an SVG Tiny <code>&lt;animate&gt;</code>
 * element.
 *
 * @version $Id: Animate.java,v 1.4 2006/06/29 10:47:29 ln156897 Exp $
 */
public class Animate extends AbstractAnimate {
    /**
     * Required Animate traits
     */
    static final String[] REQUIRED_TRAITS
        = {SVGConstants.SVG_ATTRIBUTE_NAME_ATTRIBUTE};

    /**
     * The trait's qualified name.
     */
    String traitQName = null;

    /**
     * Builds a new Animate element that belongs to the given
     * document. This <code>Animate</code> will belong 
     * to the <code>DocumentNode</code>'s time container.
     *
     * @param ownerDocument the document this node belongs to.
     * @throws IllegalArgumentException if the input ownerDocument is null
     */
    public Animate(final DocumentNode ownerDocument) {
        super(ownerDocument, SVGConstants.SVG_ANIMATE_TAG);
    }

    /**
     * Builds a new Animate element that belongs to the given
     * document. This <code>Animate</code> will belong 
     * to the <code>DocumentNode</code>'s time container.
     *
     * @param ownerDocument the document this node belongs to.
     * @param localName the animation element's local name.
     * @throws IllegalArgumentException if the input ownerDocument is null
     */
    public Animate(final DocumentNode ownerDocument,
                   final String localName) {
        super(ownerDocument, localName);
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
        return new Animate(doc, getLocalName());
    }

    /**
     * @param traitName the name of the trait which the element may support.
     * @return true if this element supports the given trait in one of the
     *         trait accessor methods.
     */
    boolean supportsTrait(final String traitName) {
        if (SVGConstants.SVG_ATTRIBUTE_NAME_ATTRIBUTE == traitName) {
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

    // JAVADOC COMMENT ELIDED
    public String getTraitImpl(final String name)
        throws DOMException {
        if (SVGConstants.SVG_ATTRIBUTE_NAME_ATTRIBUTE == name) {
            return traitQName;
        } else {
            return super.getTraitImpl(name);
        }
    }

    /**
     * Animate supports the to, from, by, values, keyTimes, keySplines,
     * and attributeName traits.
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
        if (SVGConstants.SVG_ATTRIBUTE_NAME_ATTRIBUTE == name) {
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


}
