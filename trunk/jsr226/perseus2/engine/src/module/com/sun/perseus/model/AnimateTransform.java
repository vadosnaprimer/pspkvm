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
 * <code>Animate</code> represents an SVG Tiny 
 * <code>&lt;animateTransform&gt;</code> element.
 *
 * @version $Id: AnimateTransform.java,v 1.5 2006/06/29 10:47:29 ln156897 Exp $
 */
public final class AnimateTransform extends Animate {
    
    /**
     * translate | scale | rotate | skewX | skewY
     * Animation on the transform's translate components
     */
    public static final int TYPE_TRANSLATE = 2;

    /**
     * Animation on the transform's scale components
     */
    public static final int TYPE_SCALE = 3;

    /**
     * Animation on the transform's rotation.
     */
    public static final int TYPE_ROTATE = 4;

    /**
     * Animation on the transform's skew x component
     */
    public static final int TYPE_SKEW_X = 5;

    /**
     * Animation on the transform's skew y component.
     */
    public static final int TYPE_SKEW_Y = 6;

    /**
     * Builds a new Animate element that belongs to the given
     * document. This <code>Animate</code> will belong 
     * to the <code>DocumentNode</code>'s time container.
     *
     * @param ownerDocument the document this node belongs to.
     * @throws IllegalArgumentException if the input ownerDocument is null
     */
    public AnimateTransform(final DocumentNode ownerDocument) {
        super(ownerDocument, SVGConstants.SVG_ANIMATE_TRANSFORM_TAG);
        type = TYPE_TRANSLATE;
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
        return new AnimateTransform(doc);
    }

    /**
     * Supported traits: to, attributeName
     *
     * @param traitName the name of the trait which the element may support.
     * @return true if this element supports the given trait in one of the
     *         trait accessor methods.
     */
    boolean supportsTrait(final String traitName) {
        if (SVGConstants.SVG_TYPE_ATTRIBUTE == traitName) {
            return true;
        } else {
            return super.supportsTrait(traitName);
        }
    }

    /**
     * AnimateTransform supports the type trait.
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
        if (SVGConstants.SVG_TYPE_ATTRIBUTE == name) {
            switch (type) {
            case TYPE_TRANSLATE:
                return SVGConstants.SVG_TRANSLATE_VALUE;
            case TYPE_SCALE:
                return SVGConstants.SVG_SCALE_VALUE;
            case TYPE_ROTATE:
                return SVGConstants.SVG_ROTATE_VALUE;
            case TYPE_SKEW_X:
                return SVGConstants.SVG_SKEW_X;
            case TYPE_SKEW_Y:
            default:
                return SVGConstants.SVG_SKEW_Y;
            }
        } else {
            return super.getTraitImpl(name);
        }
    }

    /**
     * AnimateTransform supports the type trait.
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
        if (SVGConstants.SVG_TYPE_ATTRIBUTE == name) {
            checkWriteLoading(name);
            if (SVGConstants.SVG_TRANSLATE_VALUE.equals(value)) {
                type = TYPE_TRANSLATE;
            } else if (SVGConstants.SVG_SCALE_VALUE.equals(value)) {
                type = TYPE_SCALE;
            } else if (SVGConstants.SVG_ROTATE_VALUE.equals(value)) {
                type = TYPE_ROTATE;
            } else if (SVGConstants.SVG_SKEW_X.equals(value)) {
                type = TYPE_SKEW_X;
            } else if (SVGConstants.SVG_SKEW_Y.equals(value)) {
                type = TYPE_SKEW_Y;
            } else {
                throw illegalTraitValue(name, value);
            }
        } else {
            super.setTraitImpl(name, value);
        }
    }

}

