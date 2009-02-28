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

import org.w3c.dom.svg.SVGRGBColor;

import com.sun.perseus.j2d.PaintDef;
import com.sun.perseus.j2d.RGB;
import com.sun.perseus.j2d.Transform;

/**
 * <code>SolidColor</code> represents an SVG Tiny 1.2 
 * <code>&lt;solidColor&gt;</code> element. 
 * <br />
 *
 * @version $Id: SolidColor.java,v 1.5 2006/06/29 10:47:34 ln156897 Exp $
 */
public class SolidColor extends PaintElement {
    /**
     * solid-color is required on <solidColor
     */
    static final String[] REQUIRED_TRAITS
        = {SVGConstants.SVG_SOLID_COLOR_ATTRIBUTE};

    /**
     * The initial solid color value.
     */
    static final RGB DEFAULT_SOLID_COLOR = RGB.black;

    /**
     * The default solid opacity
     */
    static final float DEFAULT_SOLID_OPACITY = 1f;

    /**
     * The solid color value.
     */
    RGB solidColor = DEFAULT_SOLID_COLOR;

    /**
     * The solid opacity value
     */
    float solidOpacity = DEFAULT_SOLID_OPACITY;

    /**
     * The actual solid color value, which combines the solidColor
     * and the solidOpacity
     */
    RGB compoundColor = DEFAULT_SOLID_COLOR;

    /**
     * Constructor.
     *
     * @param ownerDocument this element's owner <code>DocumentNode</code>
     */
    public SolidColor(final DocumentNode ownerDocument) {
        super(ownerDocument);

        // Turn off objectBBox space so that the PaintServerReference
        // does not concatenate an extra transform unnecessariliy.
        isObjectBBox = false;
    }

    /**
     * @return the SVGConstants.SVG_SOLID_COLOR_TAG value
     */
    public String getLocalName() {
        return SVGConstants.SVG_SOLID_COLOR_TAG;
    }

    /**
     * Used by <code>DocumentNode</code> to create a new instance from
     * a prototype <code>Rect</code>.
     *
     * @param doc the <code>DocumentNode</code> for which a new node is
     *        should be created.
     * @return a new <code>SolidColor</code> for the requested document.
     */
    public ElementNode newInstance(final DocumentNode doc) {
        return new SolidColor(doc);
    }

    /**
     * Sets the solid-color property.
     *
     * @param newSolidColor the new solid-color property
     */
    public void setSolidColor(final RGB newSolidColor) {
        if (solidColor.equals(newSolidColor)) {
            return;
        }

        solidColor = newSolidColor;
        onPaintChange();
    }

    /**
     * Sets the solid-opacity property.
     *
     * @param newSolidOpacity the new solid-opacity property
     */
    public void setSolidOpacity(final float newSolidOpacity) {
        if (newSolidOpacity == solidOpacity) {
            return;
        }

        solidOpacity = newSolidOpacity;

        if (solidOpacity < 0) {
            solidOpacity = 0;
        } else if (solidOpacity > 1) {
            solidOpacity = 1;
        }
        onPaintChange();
    }

    /**
     * Updates the compound color value and notfies related
     * PaintServerReferences.
     */
    void onPaintChange() {
        compoundColor = null;
        notifyPaintChange();
    }

    /**
     * Computes the paint in user space on use.
     *
     * @return the computed PaintDef.
     */
    protected PaintDef computePaint() {
        if (compoundColor == null) {
            compoundColor = new RGB((int) (solidOpacity * 255f),
                                    solidColor.getRed(),
                                    solidColor.getGreen(),
                                    solidColor.getBlue());
        }      
        return compoundColor;
    }

    /**
     * SolidColor handles solid-color and solid-opacity traits.
     *
     * @param traitName the name of the trait which the element may support.
     * @return true if this element supports the given trait in one of the
     *         trait accessor methods.
     */
    boolean supportsTrait(final String traitName) {
        if (SVGConstants.SVG_SOLID_COLOR_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_SOLID_OPACITY_ATTRIBUTE == traitName) {
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
     * SolidColor handles solid-color and solid-opacity traits as 
     * FloatTraitAnims
     *
     * @param traitName the trait name.
     */
    TraitAnim createTraitAnimImpl(final String traitName) {
        if (SVGConstants.SVG_SOLID_OPACITY_ATTRIBUTE == traitName) {
            return new FloatTraitAnim(this, traitName, TRAIT_TYPE_FLOAT);
        } else if (SVGConstants.SVG_SOLID_COLOR_ATTRIBUTE == traitName) {
            return new FloatTraitAnim(this, traitName, 
                                      TRAIT_TYPE_SVG_RGB_COLOR);
        } else {
            return super.createTraitAnimImpl(traitName);
        }
    }

    /**
     * SolidColor handles solid-color and solid-opacity traits.
     *
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
        if (SVGConstants.SVG_SOLID_COLOR_ATTRIBUTE == name) {
            return solidColor.toString();
        } else if (SVGConstants.SVG_SOLID_OPACITY_ATTRIBUTE == name) {
            return Float.toString(solidOpacity);
        } else {
            return super.getTraitImpl(name);
        }
    }

    /**
     * SolidColor handles the solid-opacity as a float trait
     *
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
        if (SVGConstants.SVG_SOLID_OPACITY_ATTRIBUTE == name) {
            return solidOpacity;
        } else {
            return super.getFloatTraitImpl(name);
        }
    }

    /**
     * Supported color traits: solid-color
     *
     * @param name the requested trait's name.
     * @return the requested trait's value, as an <code>SVGRGBColor</code>.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to {@link
     * org.w3c.dom.svg.SVGRGBColor SVGRGBColor}
     * @throws SecurityException if the application does not have the necessary
     * privilege rights to access this (SVG) content.
     */
    SVGRGBColor getRGBColorTraitImpl(final String name)
        throws DOMException {
        // We use .equals here because the name is not interned.
        if (SVGConstants.SVG_SOLID_COLOR_ATTRIBUTE.equals(name)) {
            return solidColor;
        } else {
            return super.getRGBColorTraitImpl(name);
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
        if (SVGConstants.SVG_SOLID_COLOR_ATTRIBUTE == name) {
            setSolidColor(new RGB((int) value[0][0], (int) value[0][1], 
                                  (int) value[0][2]));
        } else if (SVGConstants.SVG_SOLID_OPACITY_ATTRIBUTE == name) {
            setSolidOpacity(value[0][0]);
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
        if (SVGConstants.SVG_SOLID_COLOR_ATTRIBUTE == traitName) {
            RGB color = DEFAULT_SOLID_COLOR;
            if (!SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                color = parseColorTrait
                    (SVGConstants.SVG_COLOR_ATTRIBUTE, value);
            }
            if (color == null) {
                throw illegalTraitValue(traitName, value);
            }
            return new float[][] {
                        {color.getRed(), color.getGreen(), color.getBlue()}
                    };
        } else if (SVGConstants.SVG_SOLID_OPACITY_ATTRIBUTE == traitName) {
            float so = DEFAULT_SOLID_OPACITY;
            if (!SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                so = parseFloatTrait(traitName, value);
            }

            if (so < 0) {
                so = 0;
            } else if (so > 1) {
                so = 1;
            }
            return new float[][] {{so}};
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
     * CompositeGraphicsNode handles the graphics node traits.
     * Other attributes are handled by the super class.
     *
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
        if (SVGConstants.SVG_SOLID_OPACITY_ATTRIBUTE == name) {

            // ======================= solid-opacity ===================== //

            if (SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                setSolidOpacity(DEFAULT_SOLID_OPACITY);
            } else {
                setSolidOpacity(parseFloatTrait(name, value));
            }
        } else if (SVGConstants.SVG_SOLID_COLOR_ATTRIBUTE == name) {

            // ======================== solid-color ===================== //

            if (SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                setSolidColor(DEFAULT_SOLID_COLOR);
            } else {
                setSolidColor(parseColorTrait
                              (SVGConstants.SVG_SOLID_COLOR_ATTRIBUTE, value));
            }
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
        if (SVGConstants.SVG_SOLID_OPACITY_ATTRIBUTE == name) {
            setSolidOpacity(value);
        } else {
            super.setFloatTraitImpl(name, value);
        }
    }

    /**
     * @param name the name of the trait to convert.
     * @param value the float trait value to convert.
     */
    String toStringTrait(final String name, final float[][] value) {
        if (SVGConstants.SVG_SOLID_OPACITY_ATTRIBUTE == name) {
            return Float.toString(value[0][0]);
        } else if (SVGConstants.SVG_SOLID_COLOR_ATTRIBUTE == name) {
            return toRGBString(name, value);
        } else {
            return super.toStringTrait(name, value);
        }
    }

    /**
     * Set the trait value as {@link org.w3c.dom.svg.SVGRGBColor SVGRGBColor}.
     *
     * Supported color traits: color, fill, stroke
     *
     * @param name the name of the trait to set.
     * @param value the value of the trait to set.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if the requested
     * trait's value cannot be specified as an {@link
     * org.w3c.dom.svg.SVGRGBColor SVGRGBColor}
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is null.
     * @throws SecurityException if the application does not have the necessary
     * privilege rights to access this (SVG) content.
     */
    void setRGBColorTraitImpl(final String name, final SVGRGBColor color)
        throws DOMException {
        try {
            if (SVGConstants.SVG_SOLID_COLOR_ATTRIBUTE.equals(name)) {
                setSolidColor((RGB) color);
            } else {
                super.setRGBColorTraitImpl(name, color);
            } 
        } catch (IllegalArgumentException iae) {
            throw new DOMException(DOMException.INVALID_ACCESS_ERR, 
                                   iae.getMessage());
        }
    }
}

            
