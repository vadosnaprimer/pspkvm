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

import org.w3c.dom.svg.SVGRect;

import com.sun.perseus.j2d.LinearGradientPaintDef;
import com.sun.perseus.j2d.PaintDef;
import com.sun.perseus.j2d.PaintTarget;
import com.sun.perseus.j2d.Transform;

/**
 * <code>LienarGraident</code> represents an SVG Tiny 1.2 
 * <code>&lt;linearGradient&gt;</code> element. 
 * <br />
 *
 * @version $Id: LinearGradient.java,v 1.6 2006/06/29 10:47:32 ln156897 Exp $
 */
public class LinearGradient extends GradientElement {
    /**
     * Gradient start on the x-axis
     */
    float x1;

    /**
     * Gradient start on the y-axis
     */
    float y1;

    /**
     * Gradient end on the x-axis
     */
    float x2 = 1;

    /**
     * Gradient end on the y-axis
     */
    float y2 = 0;

    /**
     * Constructor.
     *
     * @param ownerDocument this element's owner <code>DocumentNode</code>
     */
    public LinearGradient(final DocumentNode ownerDocument) {
        super(ownerDocument);

        isObjectBBox = true;
    }

    /**
     * @return the SVGConstants.SVG_LINEAR_GRADIENT_TAG value
     */
    public String getLocalName() {
        return SVGConstants.SVG_LINEAR_GRADIENT_TAG;
    }

    /**
     * Used by <code>DocumentNode</code> to create a new instance from
     * a prototype <code>Rect</code>.
     *
     * @param doc the <code>DocumentNode</code> for which a new node is
     *        should be created.
     * @return a new <code>LinearGradient</code> for the requested document.
     */
    public ElementNode newInstance(final DocumentNode doc) {
        return new LinearGradient(doc);
    }

    /**
     * Sets the x1 property.
     *
     * @param newX1 the new origin along the x-axis
     */
    public void setX1(final float newX1) {
        if (newX1 == x1) {
            return;
        }

        this.x1 = newX1;
        onPaintChange();
    }

    /**
     * @return the origin of the gradient starting point on the x-axis.
     */
    public float getX1() {
        return x1;
    }

    /**
     * Sets the y1 property.
     *
     * @param newY1 the new origin along the y-axis
     */
    public void setY1(final float newY1) {
        if (newY1 == y1) {
            return;
        }

        this.y1 = newY1;
        onPaintChange();
    }

    /**
     * @return the origin of the gradient starting point on the y-axis.
     */
    public float getY1() {
        return y1;
    }

    /**
     * Sets the x2 property.
     *
     * @param newX2 the new end along the x-axis
     */
    public void setX2(final float newX2) {
        if (newX2 == x2) {
            return;
        }

        this.x2 = newX2;
        onPaintChange();
    }

    /**
     * @return the end of the gradient starting point on the x-axis.
     */
    public float getX2() {
        return x2;
    }

    /**
     * Sets the y2 property.
     *
     * @param newY2 the new end along the y-axis
     */
    public void setY2(final float newY2) {
        if (newY2 == y2) {
            return;
        }

        this.y2 = newY2;
        onPaintChange();
    }

    /**
     * @return the end of the gradient starting point on the y-axis.
     */
    public float getY2() {
        return y2;
    }

    /**
     * LinearGradient handles x1, x2, y1, y2.
     *
     * @param traitName the name of the trait which the element may support.
     * @return true if this element supports the given trait in one of the
     *         trait accessor methods.
     */
    boolean supportsTrait(final String traitName) {
        if (SVGConstants.SVG_X1_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_X2_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_Y1_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_Y2_ATTRIBUTE == traitName) {
            return true;
        } else {
            return super.supportsTrait(traitName);
        }
    }

    /**
     * LinearGradient handles x1, y1, x2, y2 traits as 
     * FloatTraitAnims. 
     *
     * @param traitName the trait name.
     */
    TraitAnim createTraitAnimImpl(final String traitName) {
        if (SVGConstants.SVG_X1_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_X2_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_Y1_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_Y2_ATTRIBUTE == traitName) {
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
        if (SVGConstants.SVG_X1_ATTRIBUTE == name) {
            return Float.toString(x1);
        } else if (SVGConstants.SVG_Y1_ATTRIBUTE == name) {
            return Float.toString(y1);
        } else if (SVGConstants.SVG_X2_ATTRIBUTE == name) {
            return Float.toString(x2);
        } else if (SVGConstants.SVG_Y2_ATTRIBUTE == name) {
            return Float.toString(y2);
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
        if (SVGConstants.SVG_X1_ATTRIBUTE == name) {
            return x1;
        } else if (SVGConstants.SVG_Y1_ATTRIBUTE == name) {
            return y1;
        } else if (SVGConstants.SVG_X2_ATTRIBUTE == name) {
            return x2;
        } else if (SVGConstants.SVG_Y2_ATTRIBUTE == name) {
            return y2;
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
        if (SVGConstants.SVG_X1_ATTRIBUTE == name) {
            setX1(value[0][0]);
        } else if (SVGConstants.SVG_Y1_ATTRIBUTE == name) {
            setY1(value[0][0]);
        } else if (SVGConstants.SVG_X2_ATTRIBUTE == name) {
            setX2(value[0][0]);
        } else if (SVGConstants.SVG_Y2_ATTRIBUTE == name) {
            setY2(value[0][0]);
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
     * value is incompatible with the given trait.
     */
    public float[][] validateFloatArrayTrait(
            final String traitName,
            final String value,
            final String reqNamespaceURI,
            final String reqLocalName,
            final String reqTraitNamespace,
            final String reqTraitName) throws DOMException {
        if (SVGConstants.SVG_X1_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_X2_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_Y1_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_Y2_ATTRIBUTE == traitName) {
            return new float[][] {{parseFloatTrait(traitName, value)}};
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
        if (SVGConstants.SVG_X1_ATTRIBUTE == name) {
            setX1(parseFloatTrait(name, value));
        } else if (SVGConstants.SVG_Y1_ATTRIBUTE == name) {
            setY1(parseFloatTrait(name, value));
        } else if (SVGConstants.SVG_X2_ATTRIBUTE == name) {
            setX2(parseFloatTrait(name, value));
        } else if (SVGConstants.SVG_Y2_ATTRIBUTE == name) {
            setY2(parseFloatTrait(name, value));
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
        if (SVGConstants.SVG_X1_ATTRIBUTE == name) {
            setX1(value);
        } else if (SVGConstants.SVG_Y1_ATTRIBUTE == name) {
            setY1(value);
        } else if (SVGConstants.SVG_X2_ATTRIBUTE == name) {
            setX2(value);
        } else if (SVGConstants.SVG_Y2_ATTRIBUTE == name) {
            setY2(value);
        } else {
            super.setFloatTraitImpl(name, value);
        }
    }

    /**
     * @param name the name of the trait to convert.
     * @param value the float trait value to convert.
     */
    String toStringTrait(final String name, final float[][] value) {
        if (SVGConstants.SVG_X1_ATTRIBUTE == name
            ||
            SVGConstants.SVG_Y1_ATTRIBUTE == name
            ||
            SVGConstants.SVG_X2_ATTRIBUTE == name
            ||
            SVGConstants.SVG_Y2_ATTRIBUTE == name) {
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
                new LinearGradientPaintDef(x1, 
                                           y1,
                                           x2, 
                                           y2,
                                           lastColorMapFractions,
                                           lastColorMapRGBA,
                                           LinearGradientPaintDef.CYCLE_NONE,
                                           isObjectBBox,
                                           transform);
        }      
        return computedPaint;
    }
}
