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

import com.sun.perseus.platform.MathSupport;

import com.sun.perseus.j2d.Box;
import com.sun.perseus.j2d.PathSupport;
import com.sun.perseus.j2d.Transform;
import com.sun.perseus.j2d.GraphicsProperties;
import com.sun.perseus.j2d.RenderGraphics;

import com.sun.perseus.util.SVGConstants;

import org.w3c.dom.DOMException;


/**
 * An <code>Ellipse</code> node models an SVG <code>&lt;ellipse&gt;</code>
 * or an <code>&lt;circle&gt;</code> element.
 * <br />
 * A negative radius along the x or y axis is illegal. A null radius
 * along the x or y axis disables rendering of the ellipes.
 * <br />
 * If the <code>Ellipse</code> is a circle, then setting the x radius
 * also sets the y radius (to the same value) and setting the y radius
 * also sets the x radius (to the same value).
 *
 * @version $Id: Ellipse.java,v 1.11 2006/06/29 10:47:31 ln156897 Exp $
 */
public class Ellipse extends AbstractShapeNode {
    /**
     * The rx and ry attributes are required for an <ellipse>
     */
    static final String[] ELLIPSE_REQUIRED_TRAITS
        = {SVGConstants.SVG_RX_ATTRIBUTE,
           SVGConstants.SVG_RY_ATTRIBUTE};

    /**
     * The r trait is required for a <circle>
     */
    static final String[] CIRCLE_REQUIRED_TRAITS
        = {SVGConstants.SVG_R_ATTRIBUTE};

    /**
     * If true, the x and y radii are constrained to always be the 
     * same value.
     */
    protected boolean isCircle = false;

    /**
     * This ellipe's origin along the x-axis
     */
    protected float x;

    /**
     * The ellipse's origin along the y-axis
     */
    protected float y;

    /**
     * The ellipse's width
     */
    protected float width;

    /**
     * The ellipse's height.
     */
    protected float height;

    /**
     * Constructor.
     *
     * @param ownerDocument this element's owner <code>DocumentNode</code>
     */
    public Ellipse(final DocumentNode ownerDocument) {
        this(ownerDocument, false);
}

    /**
     * Constructor.
     *
     * @param ownerDocument this element's owner <code>DocumentNode</code>
     * @param isCircle if true, the x and y radii will be constrained to
     *        always have the same value.
     */
    public Ellipse(final DocumentNode ownerDocument, final boolean isCircle) {
        super(ownerDocument);
        this.isCircle = isCircle;

        // Initially, the ellipse's width and height are zero, so we
        // set the corresponding bits accordingly.
        canRenderState |= CAN_RENDER_ZERO_WIDTH_BIT;
        canRenderState |= CAN_RENDER_ZERO_HEIGHT_BIT;
    }

    /**
     * @return the SVGConstants.SVG_CIRCLE_TAG if isCircle is true, 
     * SVGConstants.SVG_ELLIPSE_TAG otherwise.
     */

    public String getLocalName() {
        if (isCircle) {
            return SVGConstants.SVG_CIRCLE_TAG;
        } else {
            return SVGConstants.SVG_ELLIPSE_TAG;
        }
    }

    /**
     * Used by <code>DocumentNode</code> to create a new instance from
     * a prototype <code>Ellipse</code>.
     *
     * @param doc the <code>DocumentNode</code> for which a new node is
     *        should be created.
     * @return a new <code>Ellipse</code> for the requested document.
     */
    public ElementNode newInstance(final DocumentNode doc) {
        return new Ellipse(doc, isCircle);
    }

    /**
     * @return this ellipse's x-axis center
     */
    public float getCx() {
        return x + width / 2;
    }

    /**
     * @return this ellipse's y-axis center
     */
    public float getCy() {
        return y + height / 2;
    }

    /**
     * @return this ellipse's x-axis radius
     */
    public float getRx() {
        return width / 2;
    }

    /**
     * @return this ellipse's y-axis radius
     */
    public float getRy() {
        return height / 2;
    }

    /**
     * @param cx x-axis ellipse center coordinate
     */
    public void setCx(final float cx) {
        float newCx = cx - width / 2;
        if (newCx == x) {
            return;
        }
        modifyingNode();
        x = newCx;
        renderingDirty();
        modifiedNode();
    }

    /**
     * @param cy y-axis ellipse coordinate
     */
    public void setCy(final float cy) {
        float newCy = cy - height / 2;
        if (y == newCy) {
            return;
        }
        modifyingNode();
        y = newCy;
        renderingDirty();
        modifiedNode();
    }

    /**
     * @param rx the new x-axis radius
     */
    public void setRx(final float rx) {
        if (rx < 0) {
            throw new IllegalArgumentException();
        }

        if (width == rx * 2) {
            return;
        }

        modifyingNode();
        renderingDirty();
        
        float cx = getCx();
        width = rx * 2;
        x = cx - rx;

        computeCanRenderWidthBit(width);

        if (isCircle) {
            float cy = getCy();
            height = rx * 2;
            y = cy - rx;
            computeCanRenderHeightBit(width);
        }

        modifiedNode();
    }

    /**
     * @param ry the new y-axis radius
     */
    public void setRy(final float ry) {
        if (ry < 0) {
            throw new IllegalArgumentException();
        }

        if (height == ry * 2) {
            return;
        }

        modifyingNode();
        renderingDirty();

        float cy = getCy();
        height = ry * 2;
        y = cy - ry;

        if (isCircle) {
            float cx = getCx();
            width = ry * 2;
            x = cx - ry;
        }

        computeCanRenderHeightBit(height);
        modifiedNode();
    }

    /**
     * @param rg the RenderGraphics on which to fill the shape.
     */
    public void fillShape(final RenderGraphics rg) {
        rg.fillOval(x, y, width, height);
    }

    /**
     * @param rg the RenderGraphics on which to draw the shape.
     */
    public void drawShape(final RenderGraphics rg) {
        rg.drawOval(x, y, width, height);
    }

    /**
     * @param x the hit point coordinate along the x-axis, in user space.
     * @param y the hit point coordinate along the y-axis, in user space.
     * @param fillRule the fillRule to apply when testing for containment.
     * @return true if the hit point is contained within the shape.
     */
    public boolean contains(final float x, final float y, final int fillRule) {
        // Normalize the coordinates compared to the ellipse
        // having a center at 0,0 and a radius of 0.5.
        float normx = (x - this.x) / width - 0.5f;
        float normy = (y - this.y) / height - 0.5f;
        return (normx * normx + normy * normy) < 0.25f;
    }

    /**
     * Returns the stroked shape, using the given stroke properties.
     *
     * @param gp the <code>GraphicsProperties</code> defining the rendering
     *        context.
     * @return the shape's stroked path.
     */
    Object getStrokedPath(final GraphicsProperties gp) {
        return PathSupport.getStrokedEllipse(x, 
                                             y,
                                             width, 
                                             height,
                                             gp);
    }

    /**
     * Supported traits: cx, cy, rx, ry
     *
     * @param traitName the name of the trait which the element may support.
     * @return true if this element supports the given trait in one of the
     *         trait accessor methods.
     */
    boolean supportsTrait(final String traitName) {
        if (SVGConstants.SVG_CX_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_CY_ATTRIBUTE == traitName) {
            return true;
        }

        if (isCircle && SVGConstants.SVG_R_ATTRIBUTE == traitName) {
            return true;
        } else if (!isCircle 
                   && 
                   (SVGConstants.SVG_RX_ATTRIBUTE == traitName
                    ||
                    SVGConstants.SVG_RY_ATTRIBUTE == traitName)) {
            return true;
        } else {
            return super.supportsTrait(traitName);
        }
    }

    /**
     * @return an array of traits that are required by this element.
     */
    public String[] getRequiredTraits() {
        if (isCircle) {
            return CIRCLE_REQUIRED_TRAITS;
        } else {
            return ELLIPSE_REQUIRED_TRAITS;
        }
    }

    /**
     * Supported traits: cx, cy, r, rx, ry
     *
     * @param name the requested trait name.
     * @return the requested trait value, as a string.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to a String (SVG Tiny only).
     */
    public String getTraitImpl(String name)
        throws DOMException {
        if (SVGConstants.SVG_CX_ATTRIBUTE == name) {
            return Float.toString(getCx());
        } else if (SVGConstants.SVG_CY_ATTRIBUTE == name) {
            return Float.toString(getCy());
        } else if ((!isCircle && SVGConstants.SVG_RX_ATTRIBUTE == name) 
                   || 
                   (isCircle && SVGConstants.SVG_R_ATTRIBUTE == name)) {
            return Float.toString(getRx());
        } else if (!isCircle && SVGConstants.SVG_RY_ATTRIBUTE == name) {
            return Float.toString(getRy());
        } else {
            return super.getTraitImpl(name);
        }
    }

    /**
     * Supported traits: cx, cy, rx, ry
     *
     * @param name the requested trait name.
     * @param return the requested trait value.
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
            return getCx();
        } else if (SVGConstants.SVG_CY_ATTRIBUTE == name) {
            return getCy();
        } else if ((!isCircle && SVGConstants.SVG_RX_ATTRIBUTE == name) 
                   || 
                   (isCircle && SVGConstants.SVG_R_ATTRIBUTE == name)) {
            return getRx();
        } else if (SVGConstants.SVG_RY_ATTRIBUTE == name) {
            return getRy();
        } else {
            return super.getFloatTraitImpl(name);
        }
    }

    /**
     * @param traitName the trait name.
     */
    TraitAnim createTraitAnimImpl(final String traitName) {
        if (SVGConstants.SVG_CX_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_CY_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_RX_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_RY_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_R_ATTRIBUTE == traitName) {
            return new FloatTraitAnim(this, traitName, TRAIT_TYPE_FLOAT);
        } else {
            return super.createTraitAnimImpl(traitName);
        }
    }

    /**
     * Set the trait value as float.
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
        } else if (SVGConstants.SVG_RX_ATTRIBUTE == name) {
            checkPositive(name, value[0][0]);
            setRx(value[0][0]);
        } else if (SVGConstants.SVG_RY_ATTRIBUTE == name) {
            checkPositive(name, value[0][0]);
            setRy(value[0][0]);
        } else if (SVGConstants.SVG_R_ATTRIBUTE == name) {
            checkPositive(name, value[0][0]);
            setRx(value[0][0]);
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
        if (SVGConstants.SVG_CX_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_CY_ATTRIBUTE == traitName) {
            return toAnimatedFloatArray(parseFloatTrait(traitName, value));
        } else if (SVGConstants.SVG_RX_ATTRIBUTE == traitName
                   || 
                   SVGConstants.SVG_R_ATTRIBUTE == traitName
                   ||
                   SVGConstants.SVG_RY_ATTRIBUTE == traitName) {
            return toAnimatedFloatArray(parsePositiveFloatTrait(traitName, 
                                                                value));
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
     * Supported traits: cx, cy, rx, ry
     *
     * @param name the trait name.
     * @param value the trait's string value.
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
        } else if ((!isCircle && SVGConstants.SVG_RX_ATTRIBUTE == name) 
                   || 
                   (isCircle && SVGConstants.SVG_R_ATTRIBUTE == name)) {
            setRx(parsePositiveFloatTrait(name, value));
        } else if (SVGConstants.SVG_RY_ATTRIBUTE == name) {
            setRy(parsePositiveFloatTrait(name, value));
        } else {
            super.setTraitImpl(name, value);
        }
    }

    /**
     * Supported traits: cx, cy, rx, ry
     *
     * @param name the trait name.
     * @param value the trait float value.
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
        } else if ((!isCircle 
                    && SVGConstants.SVG_RX_ATTRIBUTE == name) 
                   || 
                   (isCircle 
                    && 
                    SVGConstants.SVG_R_ATTRIBUTE == name)) {
            checkPositive(name, value);
            setRx(value);
        } else if (SVGConstants.SVG_RY_ATTRIBUTE == name) {
            checkPositive(name, value);
            setRy(value);
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
            SVGConstants.SVG_CY_ATTRIBUTE == name) {
            return Float.toString(value[0][0]);
        } else if ((!isCircle 
                    && SVGConstants.SVG_RX_ATTRIBUTE == name) 
                   || 
                   (isCircle 
                    && 
                    SVGConstants.SVG_R_ATTRIBUTE == name)) {
            return Float.toString(value[0][0]);
        } else if (SVGConstants.SVG_RY_ATTRIBUTE == name) {
            return Float.toString(value[0][0]);
        } else {
            return super.toStringTrait(name, value);
        }
    }

    /**
     * @param bbox the bounding box to which this node's bounding box should be
     *        appended. That bounding box is in the target coordinate space. It 
     *        may be null, in which case this node should create a new one.
     * @param t the transform from the node coordinate system to the coordinate
     *        system into which the bounds should be computed.
     * @return the bounding box of this node, in the target coordinate space, 
     */
    Box addNodeBBox(final Box bbox, 
                    final Transform t) {
        
        float rx = getRx();
        float ry = getRy();

        if (t == null 
            || (t.getComponent(1) == 0 && t.getComponent(2) == 0)
            || width == 0 
            || height == 0) {
            // If we are dealing with no transform or if the 
            // transform is a simple zoom/pan
            return addTransformedBBox(bbox, x, y, width, height, t);
        }

        //
        // The ellipse's equations are:
        //
        // x = cx + rx * cos (t)
        // y = cy + ry * sin (t)
        //
        // When transformed through t, the equation becomes:
        //
        // [x']   [m0 m2 m4]   [x]
        // [y'] = [m1 m3 m5] * [y]
        // [1 ]   [ 0 0   1]   [1]
        //
        // x' = m0 * x + m2 * y + m4
        // y' = m1 * x + m3 * y + m5
        //
        // x' = m0 * cx + m0 * rx * cos(t) + m2 * cy + m2 * ry * sin(t) + m4
        // y' = m1 * cx + m1 * rx * cos(t) + m3 * cy + m3 * ry * sin(t) + m5
        //
        // x' = m0 * cx + m2 * cy + m4 + m0 * rx * cos(t) + m2 * ry * sin(t)
        // y' = m1 * cx + m3 * cy + m5 + m1 * rx * cos(t) + m3 * ry * sin(t)
        //
        // fx = m0 * cx + m2 * cy + m4 
        // fy = m1 * cx + m3 * cy + m5
        //
        // vx(t) = m0 * rx * cos(t) + m2 * ry * sin(t)
        // vy(t) = m1 * rx * cos(t) + m3 * ry * sin(t)
        //
        // The maximum and minimum are computed by the derivative functions:
        //
        // vx(t)' = -m0 * rx * sin(t) + m2 * ry * cos(t)
        // vy(t)' = -m1 * rx * sin(t) + m3 * ry * cos(t)
        //
        // vx(t)' = 0 for tan(t) = sin(t) / cos(t) 
        //                       = m2 * ry / m0 * rx
        // vy(t)' = 0 for tan(t) = sin(t) / cos(t) 
        //                       = m3 * ry / m1 * rx
        //
        float m0 = t.getComponent(0);
        float m1 = t.getComponent(1);
        float m2 = t.getComponent(2);
        float m3 = t.getComponent(3);
        float m4 = t.getComponent(4);
        float m5 = t.getComponent(5);
        float cx = getCx();
        float cy = getCy();

        float m0rx = m0 * rx;
        float m2ry = m2 * ry;
        float m1rx = m1 * rx;
        float m3ry = m3 * ry;

        float theta = MathSupport.atan2(m2ry, m0rx);
        float theta2 = theta + MathSupport.PI;
        float cost = MathSupport.cos(theta);
        float sint = MathSupport.sin(theta);
        float cost2 = MathSupport.cos(theta2);
        float sint2 = MathSupport.sin(theta2);

        float maxX = m0rx * cost + m2ry * sint;
        float minX = m0rx * cost2 + m2ry * sint2;
        float width = maxX - minX;

        if (minX > maxX) {
            minX = maxX;
            width = -width;
        }

        theta = MathSupport.atan2(m3ry, m1rx);
        theta2 = theta + MathSupport.PI;
        cost = MathSupport.cos(theta);
        sint = MathSupport.sin(theta);
        cost2 = MathSupport.cos(theta2);
        sint2 = MathSupport.sin(theta2);

        float maxY = m1rx * cost + m3ry * sint;
        float minY = m1rx * cost2 + m3ry * sint2;
        float height = maxY - minY;

        if (minY > maxY) {
            minY = maxY;
            height = -height;
        }

        float fx = m0 * cx + m2 * cy + m4;
        float fy = m1 * cx + m3 * cy + m5;
        
        // (fx, fy) is the upper left corner of the bounding box
        return addBBox(bbox, fx + minX, fy + minY, width, height);
    }


}
