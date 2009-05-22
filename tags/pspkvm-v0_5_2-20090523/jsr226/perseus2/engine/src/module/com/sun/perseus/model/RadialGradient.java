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

import com.sun.perseus.j2d.PaintDef;
import com.sun.perseus.j2d.PaintTarget;
import com.sun.perseus.j2d.RadialGradientPaintDef;
import com.sun.perseus.j2d.Transform;

import org.w3c.dom.DOMException;

import org.w3c.dom.svg.SVGRect;

/**
 * <code>LienarGraident</code> represents an SVG Tiny 1.2 
 * <code>&lt;radialGradient&gt;</code> element. 
 * <br />
 *
 * @version $Id: RadialGradient.java,v 1.6 2006/06/29 10:47:33 ln156897 Exp $
 */
public class RadialGradient extends GradientElement {
    /**
     * Gradient center on the x-axis
     */
    float cx = 0.5f;

    /**
     * Gradient center on the y-axis
     */
    float cy = 0.5f;

    /**
     * Gradient radius
     */
    float r = 0.5f;

    /**
     * Constructor.
     *
     * @param ownerDocument this element's owner <code>DocumentNode</code>
     */
    public RadialGradient(final DocumentNode ownerDocument) {
        super(ownerDocument);
    }

    /**
     * @return the SVGConstants.SVG_RADIAL_GRADIENT_TAG value
     */
    public String getLocalName() {
        return SVGConstants.SVG_RADIAL_GRADIENT_TAG;
    }

    /**
     * Used by <code>DocumentNode</code> to create a new instance from
     * a prototype <code>Rect</code>.
     *
     * @param doc the <code>DocumentNode</code> for which a new node is
     *        should be created.
     * @return a new <code>RadialGradient</code> for the requested document.
     */
    public ElementNode newInstance(final DocumentNode doc) {
        return new RadialGradient(doc);
    }

    /**
     * Sets the cx property.
     *
     * @param newCX the new origin along the x-axis
     */
    public void setCx(final float newCX) {
        if (newCX == cx) {
            return;
        }

        this.cx = newCX;
        onPaintChange();
    }

    /**
     * @return the cx property.
     */
    public float getCx() {
        return cx;
    }

    /**
     * Sets the cy property.
     *
     * @param newCY the new origin along the y-axis
     */
    public void setCy(final float newCY) {
        if (newCY == cy) {
            return;
        }

        this.cy = newCY;
        onPaintChange();
    }

    /**
     * @return the cy property.
     */
    public float getCy() {
        return cy;
    }

    /**
     * Sets the r property.
     *
     * @param newR the new end along the x-axis
     */
    public void setR(final float newR) {
        if (newR == r) {
            return;
        }

        if (r < 0) {
            throw new IllegalArgumentException();
        }

        this.r = newR;
        onPaintChange();
    }

    /**
     * @return the gradient's radius.
     */
    public float getR() {
        return r;
    }

    /**
     * @param traitName the name of the trait which the element may support.
     * @return true if this element supports the given trait in one of the
     *         trait accessor methods.
     */
    boolean supportsTrait(final String traitName) {
        if (SVGConstants.SVG_CX_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_R_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_CY_ATTRIBUTE == traitName) {
            return true;
        } else {
            return super.supportsTrait(traitName);
        }
    }

    /**
     * RadialGradient handles cx, cy, r traits as 
     * FloatTraitAnims. 
     *
     * @param traitName the trait name.
     */
    TraitAnim createTraitAnimImpl(final String traitName) {
        if (SVGConstants.SVG_CX_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_R_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_CY_ATTRIBUTE == traitName) {
            return new FloatTraitAnim(this, traitName, TRAIT_TYPE_FLOAT);
        } else {
            return super.createTraitAnimImpl(traitName);
        }
    }

    /**
     * @param name the requested trait name (e.g., "ry")
     * @return the trait's value, as a string.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to a String (SVG Tiny only).
     */
    public String getTraitImpl(final String name)
        throws DOMException {
        if (SVGConstants.SVG_CX_ATTRIBUTE == name) {
            return Float.toString(cx);
        } else if (SVGConstants.SVG_CY_ATTRIBUTE == name) {
            return Float.toString(cy);
        } else if (SVGConstants.SVG_R_ATTRIBUTE == name) {
            return Float.toString(r);
        } else {
            return super.getTraitImpl(name);
        }
    }

    /**
     * @param name the requested trait name (e.g., "y")
     * @return the requested trait value
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to a float
     * @throws SecurityException if the application does not have the necessary
     * privilege rights to access this (SVG) content.
     */
    float getFloatTraitImpl(final String name)
        throws DOMException {
        if (SVGConstants.SVG_CX_ATTRIBUTE == name) {
            return cx;
        } else if (SVGConstants.SVG_CY_ATTRIBUTE == name) {
            return cy;
        } else if (SVGConstants.SVG_R_ATTRIBUTE == name) {
            return r;
        } else {
            return super.getFloatTraitImpl(name);
        }
    }

    /**
     * Set the trait value as float array.
     *
     * @param name the trait's name.
     * @param value the trait's value.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if the requested
     * trait's value cannot be specified as a float
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is an invalid value for the given trait.
     */
    void setFloatArrayTrait(final String name, final float[][] value)
        throws DOMException {
        if (SVGConstants.SVG_CX_ATTRIBUTE == name) {
            setCx(value[0][0]);
        } else if (SVGConstants.SVG_CY_ATTRIBUTE == name) {
            setCy(value[0][0]);
        } else if (SVGConstants.SVG_R_ATTRIBUTE == name) {
            setR(value[0][0]);
        } else {
            super.setFloatArrayTrait(name, value);
        }    
    }
   
    /**
     * Validates the input trait value.
     *
     * @param traitName the name of the trait to be validated.
     * @param value the value to be validated
     * @param reqNamespaceURI the namespace of the element requesting 
     *        validation.
     * @param reqLocalName the local name of the element requesting validation.
     * @param reqTraitNamespace the namespace of the trait which has the values
     *        value on the requesting element.
     * @param reqTraitName the name of the trait which has the values value on 
     *        the requesting element.
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     *         value is incompatible with the given trait.
     */
    public float[][] validateFloatArrayTrait(
            final String traitName,
            final String value,
            final String reqNamespaceURI,
            final String reqLocalName,
            final String reqTraitNamespace,
            final String reqTraitName) throws DOMException {
        if (SVGConstants.SVG_CX_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_CY_ATTRIBUTE == traitName) {
            return new float[][] {{parseFloatTrait(traitName, value)}};
        } else if (SVGConstants.SVG_R_ATTRIBUTE == traitName) {
            return new float[][] {{parsePositiveFloatTrait(traitName, value)}};
        } else {
            return super.validateFloatArrayTrait(traitName,
                                                 value,
                                                 reqNamespaceURI,
                                                 reqLocalName,
                                                 reqTraitNamespace,
                                                 reqTraitName);
        }
                      
    }

    /**
     * Supported traits: stroke-width, stroke-miterlimit, stroke-dashoffset,
     * fill-rule, stroke-linejoin, stroke-linecap, display, visibility, 
     * color, fill, stroke, fill-opacity, stroke-opacity, stroke-dasharray
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
    public void setTraitImpl(final String name, final String value)
        throws DOMException {
        if (SVGConstants.SVG_CX_ATTRIBUTE == name) {
            setCx(parseFloatTrait(name, value));
        } else if (SVGConstants.SVG_CY_ATTRIBUTE == name) {
            setCy(parseFloatTrait(name, value));
        } else if (SVGConstants.SVG_R_ATTRIBUTE == name) {
            setR(parsePositiveFloatTrait(name, value));
        } else {
            super.setTraitImpl(name, value);
        }
    }


    /**
     * Set the trait value as float.  
     *
     * Supported float traits: stroke-width, stroke-miterlimit,
     * stroke-dashoffset, fill-opacity, stroke-opacity.
     *
     * @param name the name of the trait to set.
     * @param value the value of the trait to set.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if the requested
     * trait's value cannot be specified as a float
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is an invalid value for the given trait.
     * @throws SecurityException if the application does not have the necessary
     * privilege rights to access this (SVG) content.
     */
    public void setFloatTraitImpl(final String name, final float value)
        throws DOMException {
        if (SVGConstants.SVG_CX_ATTRIBUTE == name) {
            setCx(value);
        } else if (SVGConstants.SVG_CY_ATTRIBUTE == name) {
            setCy(value);
        } else if (SVGConstants.SVG_R_ATTRIBUTE == name) {
            checkPositive(name, value);
            setR(value);
        } else {
            super.setFloatTraitImpl(name, value);
        }
    }

    /**
     * @param name the name of the trait to convert.
     * @param value the float trait value to convert.
     */
    String toStringTrait(final String name, final float[][] value) {
        if (SVGConstants.SVG_CX_ATTRIBUTE == name
            ||
            SVGConstants.SVG_CY_ATTRIBUTE == name
            ||
            SVGConstants.SVG_R_ATTRIBUTE == name) {
            return Float.toString(value[0][0]);
        } else {
            return super.toStringTrait(name, value);
        }
    }

    /**
     * Computes the paint in user space on use.
     *
     * @return the computed PaintDef.
     */
    protected PaintDef computePaint() {
        if (computedPaint == null) {
            buildGradientColorMap();
            computedPaint = 
                new RadialGradientPaintDef(cx, 
                                           cy,
                                           cx,
                                           cy,
                                           r, 
                                           lastColorMapFractions,
                                           lastColorMapRGBA,
                                           RadialGradientPaintDef.CYCLE_NONE,
                                           isObjectBBox,
                                           transform);
        }      
        return computedPaint;
    }
}
