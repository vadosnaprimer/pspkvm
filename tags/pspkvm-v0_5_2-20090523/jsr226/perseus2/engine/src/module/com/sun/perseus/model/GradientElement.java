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

import com.sun.perseus.j2d.LinearGradientPaintDef;

import org.w3c.dom.DOMException;

import org.w3c.dom.svg.SVGRGBColor;
import org.w3c.dom.svg.SVGMatrix;

import com.sun.perseus.j2d.RGB;
import com.sun.perseus.j2d.PaintDef;
import com.sun.perseus.j2d.PaintTarget;
import com.sun.perseus.j2d.PaintServer;
import com.sun.perseus.j2d.Transform;

/**
 * <code>GradientElement</code> abstract class is a helper base
 * class for <code>LinearGradient</code> and <code>RadialGradient</code>.
 * <br />
 *
 * @version $Id: GradientElement.java,v 1.10 2006/06/29 10:47:31 ln156897 Exp $
 */
public abstract class GradientElement extends PaintElement {
    /**
     * Computed PaintDef, in userSpaceOnUse.
     */
    PaintDef computedPaint;
    /**
     * The last computed GradientColorMap fractions
     */
    float[] lastColorMapFractions;

    /**
     * The last computed GradientColorMap colors
     */
    int[] lastColorMapRGBA;

    /**
     * Addintional paint transform, like the gradientTransform on gradients.
     */
    Transform transform = new Transform(null);

    /**
     * Constructor.
     *
     * @param ownerDocument this element's owner <code>DocumentNode</code>
     */
    public GradientElement(final DocumentNode ownerDocument) {
        super(ownerDocument);

        isObjectBBox = true;
    }

    /**
     * @param newTransform this node's new transform. Note that the
     *        input value is used by reference.
     */
    public void setTransform(final Transform newTransform) {
        if (equal(newTransform, transform)) {
            return;
        }
        modifyingNode();
        this.transform = newTransform;
        onPaintChange();
        modifiedNode();
    }

    /**
     * @return this node's transform
     */
    public Transform getTransform() {
        return transform;
    }

    /**
     * Sets the isObjectBBox state.
     *
     * @param newIsObjectBBox the new value for the isObjectBBox
     *        property.
     */
    public void setIsObjectBBox(final boolean newIsObjectBBox) {
        if (newIsObjectBBox == isObjectBBox) {
            return;
        }

        isObjectBBox = newIsObjectBBox;
        onPaintChange();
    }

    /**
     * Supported traits: transform.
     *
     * @param traitName the name of the trait which the element may support.
     * @return true if this element supports the given trait in one of the
     *         trait accessor methods.
     */
    boolean supportsTrait(final String traitName) {
        if (SVGConstants.SVG_GRADIENT_TRANSFORM_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_GRADIENT_UNITS_ATTRIBUTE == traitName) {
            return true;
        } else {
            return super.supportsTrait(traitName);
        }
    }

    /**
     * @param name the requested trait name.
     * @return the requested trait value.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to a String (SVG Tiny only).
     */
    public String getTraitImpl(final String name)
        throws DOMException {
        if (SVGConstants.SVG_GRADIENT_TRANSFORM_ATTRIBUTE == name) {
            return toStringTrait(transform);
        } if (SVGConstants.SVG_GRADIENT_UNITS_ATTRIBUTE == name) {
            if (isObjectBBox) {
                return SVGConstants.SVG_OBJECT_BOUND_BOX_VALUE;
            } else {
                return SVGConstants.SVG_USER_SPACE_ON_USE_VALUE;
            }
        } else {
            return super.getTraitImpl(name);
        }
    }


    /**
     * @param name the trait's name.
     * @param value the new trait value, as a string.
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
        if (SVGConstants.SVG_GRADIENT_TRANSFORM_ATTRIBUTE == name) {
            setTransform(parseTransformTrait(name, value));
        } else if (SVGConstants.SVG_GRADIENT_UNITS_ATTRIBUTE
                   .equals(name)) {
            if (SVGConstants.SVG_OBJECT_BOUND_BOX_VALUE.equals(value)) {
                setIsObjectBBox(true);
            } else if (SVGConstants.SVG_USER_SPACE_ON_USE_VALUE.equals(value)) {
                setIsObjectBBox(false);
            } else {
                throw illegalTraitValue(name, value);
            }
        } else {
            super.setTraitImpl(name, value);
        }
    }

    /**
     * @param name matrix trait name.
     * @return the trait value corresponding to name as SVGMatrix.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to {@link
     * org.w3c.dom.svg.SVGMatrix SVGMatrix}
     */
    SVGMatrix getMatrixTraitImpl(final String name)throws DOMException {
        if (SVGConstants.SVG_GRADIENT_TRANSFORM_ATTRIBUTE.equals(name)) {
            return toSVGMatrixTrait(transform);
        } else {
            return super.getMatrixTraitImpl(name);
        }
    }

    /**
     * @param name name of trait to set
     * @param matrix Transform value of trait
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if the requested
     * trait's value cannot be specified as an {@link org.w3c.dom.svg.SVGMatrix
     * SVGMatrix}
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is an invalid value for the given trait or null.
     * @throws DOMException with error code NO_MODIFICATION_ALLOWED_ERR: if
     * attempt is made to change readonly trait.
     */
    void setMatrixTraitImpl(final String name, 
                            final Transform matrix) throws DOMException {
        if (SVGConstants.SVG_GRADIENT_TRANSFORM_ATTRIBUTE.equals(name)) {
            setTransform(matrix);
        } else {
            super.setMatrixTraitImpl(name, matrix);
        }
    }

    /**
     * @param name the name of the trait to convert.
     * @param value the float trait value to convert.
     */
    String toStringTrait(final String name, final float[][] value) {
        if (SVGConstants.SVG_GRADIENT_TRANSFORM_ATTRIBUTE == name) {
            Transform transform = new Transform(value[0][0],
                                                value[1][0],
                                                value[2][0],
                                                value[3][0],
                                                value[4][0],
                                                value[5][0]);
            return toStringTrait(transform);
        } else {
            return super.toStringTrait(name, value);
        }
    }

    /**
     * GradientElement handles the gradientTransform as a TransformTraitAnim and
     * the gradientUnits as a StringTraitAnim.
     *
     * @param traitName the trait name.
     */
    TraitAnim createTraitAnimImpl(final String traitName) {
        if (SVGConstants.SVG_GRADIENT_TRANSFORM_ATTRIBUTE == traitName) {
            return new TransformTraitAnim(this, traitName);
        } else if (SVGConstants.SVG_GRADIENT_UNITS_ATTRIBUTE == traitName) {
            return new StringTraitAnim(this, NULL_NS, traitName);
        } else {
            return super.createTraitAnimImpl(traitName);
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
        // We use .equals for the transform attribute as the string may not
        // have been interned. We use == for the motion pseudo attribute because
        // it is only used internally and from the SVGConstants strings.
        if (SVGConstants.SVG_GRADIENT_TRANSFORM_ATTRIBUTE.equals(name)) {
            if (transform == null) {
                modifyingNode();
                transform = new Transform(value[0][0],
                                          value[1][0],
                                          value[2][0],
                                          value[3][0],
                                          value[4][0],
                                          value[5][0]);
            } else {
                if (!transform.equals(value)) {
                    modifyingNode();
                    transform.setTransform(value[0][0],
                                           value[1][0],
                                           value[2][0],
                                           value[3][0],
                                           value[4][0],
                                           value[5][0]);
                } else {
                    return;
                }
            }
            modifiedNode();
        } else {
            super.setFloatArrayTrait(name, value);
        }    
    }
    
    /**
     * Validates the input trait value.
     *
     * @param namespaceURI the trait's namespace URI.
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
    String validateTraitNS(final String namespaceURI,
                           final String traitName,
                           final String value,
                           final String reqNamespaceURI,
                           final String reqLocalName,
                           final String reqTraitNamespace,
                           final String reqTraitName) throws DOMException {
        if (namespaceURI != null) {
            return super.validateTraitNS(namespaceURI,
                                         traitName,
                                         value,
                                         reqNamespaceURI,
                                         reqLocalName,
                                         reqTraitNamespace,
                                         reqTraitName);
        }

        if (SVGConstants.SVG_GRADIENT_UNITS_ATTRIBUTE
            .equals(traitName)) {
            if (!SVGConstants.SVG_USER_SPACE_ON_USE_VALUE.equals(value)
                &&
                !SVGConstants.SVG_OBJECT_BOUND_BOX_VALUE.equals(value)) {
                throw illegalTraitValue(traitName, value);
            }
            return value;
        } 

        return super.validateTraitNS(namespaceURI,
                                     traitName,
                                     value,
                                     reqNamespaceURI,
                                     reqLocalName,
                                     reqTraitNamespace,
                                     reqTraitName);
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
        if (SVGConstants.SVG_GRADIENT_TRANSFORM_ATTRIBUTE == traitName) {
            Transform txf = parseTransformTrait(traitName, value);
            return new float[][] {{txf.getComponent(0)},
                                  {txf.getComponent(1)},
                                  {txf.getComponent(2)},
                                  {txf.getComponent(3)},
                                  {txf.getComponent(4)},
                                  {txf.getComponent(5)}};
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
     * Builds a GradientColorMap from the children &lt;stop&gt;
     * elements.
     */
    final void buildGradientColorMap() {
        // First, compute the number of stop children.
        ElementNode c = (ElementNode) getFirstElementChild();
        int n = 0;
        Stop[] stop = new Stop[5];

        while (c != null) {
            if (c.getLocalName() == SVGConstants.SVG_STOP_TAG
                &&
                c.getNamespaceURI() == SVGConstants.SVG_NAMESPACE_URI) {
                stop[n] = (Stop) c;
                n++;
                if (n > stop.length - 1) {
                    Stop[] tmpStop = new Stop[stop.length + 5];
                    System.arraycopy(stop, 0, tmpStop, 0, stop.length);
                    stop = tmpStop;
                }
            }

            c = (ElementNode) c.getNextElementSibling();
        }

        if (n == 0) {
            // To obtain the same result as a 'none' fill, we just use two
            // stops with fully transparent fill.
            lastColorMapFractions = new float[] {0, 1};
            lastColorMapRGBA = new int[] {0x00000000, 0x00000000};
            return;
        } if (n == 1) {
            // We duplicate the single gradient to provide the effect of 
            // a solid color.
            RGB color = stop[0].getStopColor();
            int a = (int) (stop[0].getStopOpacity() * 255);
            
            lastColorMapFractions = new float[] {0, 1};
            lastColorMapRGBA = new int[] {a << 24 
                                          | color.getRed() << 16
                                          | color.getGreen() << 8
                                          | color.getBlue(),
                                          a << 24 
                                          | color.getRed() << 16
                                          | color.getGreen() << 8
                                          | color.getBlue()
            };
            return;
        }

        float[] fractions = new float[n];
        int[] rgba = new int[n];
        RGB col = null;

        for (int i = 0; i < n; i++) {
            fractions[i] = stop[i].getOffset();
            if (i > 0 && fractions[i] <= fractions[i - 1]) {
                fractions[i] = fractions[i - 1];
            }
            
            col = stop[i].getStopColor();
            
            rgba[i] = ((int) (stop[i].getStopOpacity() * 255) << 24) 
                | col.getRed() << 16
                | col.getGreen() << 8
                | col.getBlue();
        }

        // Check that the first stop is zero. If not, we need to dupplicate the
        // first stop and give it fraction zero.
        if (fractions[0] != 0) {
            float[] tmpFractions = new float[fractions.length + 1];
            int[] tmpRgba = new int[rgba.length + 1];
            tmpFractions[0] = 0;
            tmpRgba[0] = rgba[0];
            System.arraycopy(fractions, 0, tmpFractions, 1, fractions.length);
            System.arraycopy(rgba, 0, tmpRgba, 1, rgba.length);
            fractions = tmpFractions;
            rgba = tmpRgba;
        }

        // Check that the last stop is 1. If not we duplicate the last stop.
        if (fractions[fractions.length - 1] != 1) {
            float[] tmpFractions = new float[fractions.length + 1];
            int[] tmpRgba = new int[rgba.length + 1];
            tmpFractions[tmpFractions.length - 1] = 1;
            tmpRgba[tmpRgba.length - 1] = rgba[rgba.length - 1];
            System.arraycopy(fractions, 0, tmpFractions, 0, fractions.length);
            System.arraycopy(rgba, 0, tmpRgba, 0, rgba.length);
            fractions = tmpFractions;
            rgba = tmpRgba;
        }

        lastColorMapFractions = fractions;
        lastColorMapRGBA = rgba;
    }

    /**
     * Should be called when the paint should recompute itself and 
     * notify its references.
     */
    protected void onPaintChange() {
        computedPaint = null;
        lastColorMapFractions = null;
        lastColorMapRGBA = null;

        if (loaded) {
            notifyPaintChange();
        }
    }
}
