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

import com.sun.perseus.j2d.RGB;

/**
 * <code>Stop</code> class represents the <code>&lt;stop&gt;</code>
 * SVG Tiny 1.2 element.
 *
 * @version $Id: Stop.java,v 1.5 2006/06/29 10:47:34 ln156897 Exp $
 */
public class Stop extends CompositeGraphicsNode {
    /**
     * offset is required on &lt;stop&gt;
     */
    static final String[] REQUIRED_TRAITS
        = {SVGConstants.SVG_OFFSET_ATTRIBUTE};

    /**
     * The default stop color.
     */
    static final RGB DEFAULT_STOP_COLOR = RGB.black;

    /**
     * The default stop opacity.
     */
    static final float DEFAULT_STOP_OPACITY = 1;
    /**
     * The stop's color
     */
    RGB stopColor = DEFAULT_STOP_COLOR;

    /**
     * The stop's opacity
     */
    float stopOpacity = DEFAULT_STOP_OPACITY;

    /**
     * The stop offset value.
     */
    float offset;

    /**
     * Constructor.
     *
     * @param ownerDocument this element's owner <code>DocumentNode</code>
     */
    public Stop(final DocumentNode ownerDocument) {
        super(ownerDocument);
    }

    /**
     * @return an adequate <code>ElementNodeProxy</code> for this node.
     */
    ElementNodeProxy buildProxy() {
        return new CompositeGraphicsNodeProxy(this);
    }
    
    /**
     * @return the SVGConstants.SVG_STOP_TAG value
     */
    public String getLocalName() {
        return SVGConstants.SVG_STOP_TAG;
    }

    /**
     * Used by <code>DocumentNode</code> to create a new instance from
     * a prototype <code>Stop</code>.
     *
     * @param doc the <code>DocumentNode</code> for which a new node is
     *        should be created.
     * @return a new <code>SolidColor</code> for the requested document.
     */
    public ElementNode newInstance(final DocumentNode doc) {
        return new Stop(doc);
    }

    /**
     * Sets the stop-color property.
     *
     * @param newStopColor the new stop-color property
     */
    public void setStopColor(final RGB newStopColor) {
        if (stopColor.equals(newStopColor)) {
            return;
        }

        stopColor = newStopColor;
        updateGradient();
    }

    /**
     * @return the current stopColor property. 
     */
    public RGB getStopColor() {
        return stopColor;
    }

    /**
     * Sets the stop-opacity property.
     *
     * @param newStopOpacity the new stop-opacity property
     */
    public void setStopOpacity(final float newStopOpacity) {
        if (newStopOpacity == stopOpacity) {
            return;
        }

        stopOpacity = newStopOpacity;
        updateGradient();
    }

    /**
     * @return the current stopOpacity property. 
     */
    public float getStopOpacity() {
        return stopOpacity;
    }

    /**
     * Sets the stop offset
     *
     * @param newOffset the new offset
     */
    public void setOffset(final float newOffset) {
        if (newOffset == offset) {
            return;
        }

        offset = newOffset;
        updateGradient();
    }

    /**
     * @return the stop's offset
     */
    public float getOffset() {
        return offset;
    }

    /**
     * Should be called by the stop every time its parent gradient
     * should recompute its state.
     */
    void updateGradient() {
        if (parent != null) {
            if (parent instanceof GradientElement) {
                ((GradientElement) parent).onPaintChange();
            }
        }
    }

    /**
     * @return the number of properties supported by this node
     */
    public int getNumberOfProperties() {
        return GraphicsNode.NUMBER_OF_PROPERTIES;
    }

    /**
     * @param traitName the name of the trait which the element may support.
     * @return true if this element supports the given trait in one of the
     *         trait accessor methods.
     */
    boolean supportsTrait(final String traitName) {
        if (SVGConstants.SVG_STOP_COLOR_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_STOP_OPACITY_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_OFFSET_ATTRIBUTE == traitName) {
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
     * @param traitName the trait name.
     */
    TraitAnim createTraitAnimImpl(final String traitName) {
        if (SVGConstants.SVG_STOP_OPACITY_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_OFFSET_ATTRIBUTE == traitName) {
            return new FloatTraitAnim(this, traitName, TRAIT_TYPE_FLOAT);
        } else if (SVGConstants.SVG_STOP_COLOR_ATTRIBUTE == traitName) {
            return new FloatTraitAnim(this, traitName, 
                                      TRAIT_TYPE_SVG_RGB_COLOR);
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
        if (SVGConstants.SVG_STOP_COLOR_ATTRIBUTE == name) {
            return stopColor.toString();
        } else if (SVGConstants.SVG_STOP_OPACITY_ATTRIBUTE == name) {
            return Float.toString(stopOpacity);
        } else if (SVGConstants.SVG_OFFSET_ATTRIBUTE == name) {
            return Float.toString(offset);
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
        if (SVGConstants.SVG_STOP_OPACITY_ATTRIBUTE == name) {
            return stopOpacity;
        } else if (SVGConstants.SVG_OFFSET_ATTRIBUTE == name) {
            return offset;
        } else {
            return super.getFloatTraitImpl(name);
        }
    }

    /**
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
        // We use .equals here because name is not interned.
        if (SVGConstants.SVG_STOP_COLOR_ATTRIBUTE.equals(name)) {
            return stopColor;
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
        if (SVGConstants.SVG_STOP_COLOR_ATTRIBUTE == name) {
            setStopColor(toRGB(name, value));
        } else if (SVGConstants.SVG_STOP_OPACITY_ATTRIBUTE == name) {
            if (value[0][0] < 0 || value[0][0] > 1) {
                throw illegalTraitValue(name, Float.toString(value[0][0]));
            }
            setStopOpacity(value[0][0]);
        } else if (SVGConstants.SVG_OFFSET_ATTRIBUTE == name) {
            setOffset(value[0][0]);
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
        if (SVGConstants.SVG_STOP_COLOR_ATTRIBUTE == traitName) {
            RGB color = parseColorTrait
                (SVGConstants.SVG_COLOR_ATTRIBUTE, value);
            if (color == null) {
                throw illegalTraitValue(traitName, value);
            }
            return new float[][] {
                        {color.getRed(), color.getGreen(), color.getBlue()}
                    };
        } else if (SVGConstants.SVG_STOP_OPACITY_ATTRIBUTE == traitName) {
            return new float[][] {{parseFloatTrait(traitName, value)}};
        } else if (SVGConstants.SVG_OFFSET_ATTRIBUTE == traitName) {
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
        if (SVGConstants.SVG_STOP_OPACITY_ATTRIBUTE == name) {

            // ======================= stop-opacity ===================== //

            if (SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                setStopOpacity(DEFAULT_STOP_OPACITY);
            } else {
                setStopOpacity(parsePositiveFloatTrait(name, value));
            }
        } else if (SVGConstants.SVG_STOP_COLOR_ATTRIBUTE
                   .equals(name)) {

            // ======================== stop-color ===================== //

            if (SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                setStopColor(DEFAULT_STOP_COLOR);
            } else if (SVGConstants.CSS_CURRENTCOLOR_VALUE.equals(value)) {
                setStopColor(getColor());
            } else {
                setStopColor(parseColorTrait
                              (SVGConstants.SVG_STOP_COLOR_ATTRIBUTE, value));
            }
        } else if (SVGConstants.SVG_OFFSET_ATTRIBUTE == name) {
            setOffset(parseFloatTrait(name, value));
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
        if (SVGConstants.SVG_STOP_OPACITY_ATTRIBUTE == name) {
            setStopOpacity(value);
        } else if (SVGConstants.SVG_OFFSET_ATTRIBUTE == name) {
            setOffset(value);
        } else {
            super.setFloatTraitImpl(name, value);
        }
    }

    /**
     * @param name the name of the trait to convert.
     * @param value the float trait value to convert.
     */
    String toStringTrait(final String name, final float[][] value) {
        if (SVGConstants.SVG_STOP_OPACITY_ATTRIBUTE == name
            ||
            SVGConstants.SVG_OFFSET_ATTRIBUTE == name) {
            return Float.toString(value[0][0]);
        } else if (SVGConstants.SVG_STOP_COLOR_ATTRIBUTE == name) {
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
            if (SVGConstants.SVG_STOP_COLOR_ATTRIBUTE.equals(name)) {
                setStopColor((RGB) color);
            } else {
                super.setRGBColorTraitImpl(name, color);
            }
        } catch (IllegalArgumentException iae) {
            throw new DOMException(DOMException.INVALID_ACCESS_ERR, 
                                   iae.getMessage());
        }
    }



}
