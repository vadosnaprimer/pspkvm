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

import com.sun.perseus.j2d.GraphicsProperties;
import com.sun.perseus.j2d.RenderGraphics;

import com.sun.perseus.util.SVGConstants;

import org.w3c.dom.DOMException;

import com.sun.perseus.j2d.Box;
import com.sun.perseus.j2d.PathSupport;
import com.sun.perseus.j2d.Transform;

/**
 * A <code>Line</code> node models an SVG <code>&lt;line&gt;</code>
 * element.
 * 
 * @version $Id: Line.java,v 1.9 2006/06/29 10:47:32 ln156897 Exp $
 */
public class Line extends AbstractShapeNode {
    /**
     * Starting position along the x-axis
     */
    protected float x1;

    /**
     * Starting position along the y-axis
     */
    protected float y1;

    /**
     * Ending position along the x-axis
     */
    protected float x2;

    /**
     * Ending position along the y-axis
     */
    protected float y2;

    /**
     * Constructor.
     *
     * @param ownerDocument this element's owner <code>DocumentNode</code>
     */
    public Line(final DocumentNode ownerDocument) {
        super(ownerDocument);
    }

    /**
     * @return the SVGConstants.SVG_LINE_TAG value
     */
    public String getLocalName() {
        return SVGConstants.SVG_LINE_TAG;
    }


    /**
     * Used by <code>DocumentNode</code> to create a new instance from
     * a prototype <code>Line</code>.
     *
     * @param doc the <code>DocumentNode</code> for which a new node is
     *        should be created.
     * @return a new <code>Line</code> for the requested document.
     */
    public ElementNode newInstance(final DocumentNode doc) {
        return new Line(doc);
    }

    /**
     * @return x-axis coordinate of the starting point
     */
    public float getX1() {
        return x1;
    }

    /**
     * @return y-axis coordinate of the starting point
     */
    public float getY1() {
        return y1;
    }

    /**
     * @return x-axis coordinate of the termination point
     */
    public float getX2() {
        return x2;
    }

    /**
     * @return y-axis coordinate of the termination point
     */
    public float getY2() {
        return y2;
    }

    /**
     * @param newX1 new value for the termination point on the x-axis
     */
    public void setX1(final float newX1) {
        if (newX1 == x1) {
            return;
        }
        modifyingNode();
        renderingDirty();
        x1 = newX1;
        modifiedNode();
    }

    /**
     * @param newX2 new value for the termination point on the x-axis
     */
    public void setX2(final float newX2) {
        if (newX2 == x2) {
            return;
        }
        modifyingNode();
        renderingDirty();
        x2 = newX2;
        modifiedNode();
    }

    /**
     * @param newY1 new value for the starting point on the y-axis
     */
    public void setY1(final float newY1) {
        if (newY1 == y1) {
            return;
        }
        modifyingNode();
        renderingDirty();
        y1 = newY1;
        modifiedNode();
    }

    /**
     * @param newY2 new value for the termination point on the y-axis
     */
    public void setY2(final float newY2) {
        if (newY2 == y2) {
            return;
        }
        modifyingNode();
        renderingDirty();
        y2 = newY2;
        modifiedNode();
    }

    /**
     * @param rg the RenderGraphics on which to fill the shape.
     */
    public void fillShape(final RenderGraphics rg) {
    }

    /**
     * @param rg the RenderGraphics on which to draw the shape.
     */
    public void drawShape(final RenderGraphics rg) {
        rg.drawLine(x1, y1, x2, y2);
    }

    /**
     * @param x the hit point coordinate along the x-axis, in user space.
     * @param y the hit point coordinate along the y-axis, in user space.
     * @param fillRule the fillRule to apply when testing for containment.
     * @return true if the hit point is contained within the shape.
     */
    public boolean contains(final float x, final float y, final int fillRule) {
        return false;
    }

    /**
     * Returns the stroked shape, using the given stroke properties.
     *
     * @param gp the <code>GraphicsProperties</code> defining the rendering
     *        context.
     * @return the shape's stroked path.
     */
    Object getStrokedPath(final GraphicsProperties gp) {
        return PathSupport.getStrokedLine(x1, 
                                          y1, 
                                          x2, 
                                          y2,
                                          gp);
    }

    /**
     * Supported traits: x1, x2, y1, y2
     *
     * @param traitName the name of the trait which the element may support.
     * @return true if this element supports the given trait in one of the
     *         trait accessor methods.
     */
    boolean supportsTrait(final String traitName) {
        if (SVGConstants.SVG_X1_ATTRIBUTE.equals(traitName)
            ||
            SVGConstants.SVG_X2_ATTRIBUTE.equals(traitName)
            ||
            SVGConstants.SVG_Y1_ATTRIBUTE.equals(traitName)
            ||
            SVGConstants.SVG_Y2_ATTRIBUTE.equals(traitName)) {
            return true;
        } else {
            return super.supportsTrait(traitName);
        }
    }

    /**
     * Supported traits: x1, x2, y1, y2
     *
     * @param name the requested trait's name.
     * @return the trait's string value.
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
        } else if (SVGConstants.SVG_X2_ATTRIBUTE == name) {
            return Float.toString(x2);
        } else if (SVGConstants.SVG_Y1_ATTRIBUTE == name) {
            return Float.toString(y1);
        } else if (SVGConstants.SVG_Y2_ATTRIBUTE == name) {
            return Float.toString(y2);
        } else {
            return super.getTraitImpl(name);
        }
    }

    /**
     * Supported traits: x1, x2, y1, y2
     *
     * @param name the requested trait's name.
     * @return the requested trait's float value.
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
        } else if (SVGConstants.SVG_X2_ATTRIBUTE == name) {
            return x2;
        } else if (SVGConstants.SVG_Y1_ATTRIBUTE == name) {
            return y1;
        } else if (SVGConstants.SVG_Y2_ATTRIBUTE == name) {
            return y2;
        } else {
            return super.getFloatTraitImpl(name);
        }
    }

    /**
     * @param traitName the trait name.
     */
    TraitAnim createTraitAnimImpl(final String traitName) {
        if (SVGConstants.SVG_X1_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_Y1_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_X2_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_Y2_ATTRIBUTE == traitName) {
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
        if (SVGConstants.SVG_X1_ATTRIBUTE == name) {
            setX1(value[0][0]);
        } else if (SVGConstants.SVG_Y1_ATTRIBUTE == name) {
            setY1(value[0][0]);
        } else if (SVGConstants.SVG_X2_ATTRIBUTE == name) {
            checkPositive(name, value[0][0]);
            setX2(value[0][0]);
        } else if (SVGConstants.SVG_Y2_ATTRIBUTE == name) {
            checkPositive(name, value[0][0]);
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
     *         value is incompatible with the given trait.
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
     * Supported traits: x1, x2, y1, y2
     *
     * @param name the trait's name.
     * @param value the trait's new string value.
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
        } else if (SVGConstants.SVG_X2_ATTRIBUTE == name) {
            setX2(parseFloatTrait(name, value));
        } else if (SVGConstants.SVG_Y1_ATTRIBUTE == name) {
            setY1(parseFloatTrait(name, value));
        } else if (SVGConstants.SVG_Y2_ATTRIBUTE == name) {
            setY2(parseFloatTrait(name, value));
        } else {
            super.setTraitImpl(name, value);
        }
    }

    /**
     * Supported traits: x1, x2, y1, y2
     *
     * @param name the trait's name.
     * @param value the new trait float value.
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
        } else if (SVGConstants.SVG_X2_ATTRIBUTE == name) {
            setX2(value);
        } else if (SVGConstants.SVG_Y1_ATTRIBUTE == name) {
            setY1(value);
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
            SVGConstants.SVG_X2_ATTRIBUTE == name
            ||
            SVGConstants.SVG_Y1_ATTRIBUTE == name
            ||
            SVGConstants.SVG_Y2_ATTRIBUTE == name) {
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
    Box addNodeBBox(Box bbox, 
                    final Transform t) {
        float x1 = this.x1;
        float x2 = this.x2;
        float y1 = this.y1;
        float y2 = this.y2;

        if (t != null) {
            x1 = t.getComponent(0) * this.x1 
                + t.getComponent(2) * this.y1 
                + t.getComponent(4);
            y1 = t.getComponent(1) * this.x1 
                + t.getComponent(3) * this.y1 
                + t.getComponent(5);
            x2 = t.getComponent(0) * this.x2 
                + t.getComponent(2) * this.y2 
                + t.getComponent(4);
            y2 = t.getComponent(1) * this.x2 
                + t.getComponent(3) * this.y2 
                + t.getComponent(5);
        }
        
        if (x1 > x2) {
            float x = x2;
            x2 = x1;
            x1 = x;
        }

        if (y1 > y2) {
            float y = y2;
            y2 = y1;
            y1 = y;
        }

        return addBBox(bbox, x1, y1, x2 - x1, y2 - y1);
    }
}
