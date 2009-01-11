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

import org.w3c.dom.svg.SVGRect;

import com.sun.perseus.j2d.Box;
import com.sun.perseus.j2d.PathSupport;
import com.sun.perseus.j2d.Transform;

/**
 * <code>Rect</code> represents and SVG Tiny <code>&lt;rect&gt;</code>
 * element.
 * <br />
 * Negative width, height, rx or ry value is illegal. A value of zero
 * for the rectangle's width or height disables its rendering.
 *
 * @version $Id: Rect.java,v 1.11 2006/06/29 10:47:33 ln156897 Exp $
 */
public class Rect extends AbstractShapeNode {
    /**
     * width and height are required on <rect>
     */
    static final String[] REQUIRED_TRAITS
        = {SVGConstants.SVG_WIDTH_ATTRIBUTE,
           SVGConstants.SVG_HEIGHT_ATTRIBUTE};

    /**
     * The rect's width.
     */
    protected float width = 0;

    /**
     * The rect's height.
     */
    protected float height = 0;

    /**
     * The rect's x-axis origin.
     */
    protected float x = 0;
    
    /**
     * The rect's y-axis origin.
     */
    protected float y = 0;

    /**
     * The rect's x-axis arcwidth.
     */
    protected float aw = 0;

    /**
     * The rect's y-axis archeight.
     */
    protected float ah = 0;

    /**
     * Constructor.
     *
     * @param ownerDocument this element's owner <code>DocumentNode</code>
     */
    public Rect(final DocumentNode ownerDocument) {
        super(ownerDocument);

        // Initially, the rect's width and height are zero, so we
        // set the corresponding bits accordingly.
        canRenderState |= CAN_RENDER_ZERO_WIDTH_BIT;
        canRenderState |= CAN_RENDER_ZERO_HEIGHT_BIT;
}

    /**
     * @return the SVGConstants.SVG_RECT_TAG value
     */
    public String getLocalName() {
        return SVGConstants.SVG_RECT_TAG;
    }


    /**
     * Used by <code>DocumentNode</code> to create a new instance from
     * a prototype <code>Rect</code>.
     *
     * @param doc the <code>DocumentNode</code> for which a new node is
     *        should be created.
     * @return a new <code>Rect</code> for the requested document.
     */
    public ElementNode newInstance(final DocumentNode doc) {
        return new Rect(doc);
    }

    /**
     * @return this rectangle's x-axis origin
     */
    public float getX() {
        return x;
    }

    /**
     * @return this rectangle's y-axis origin
     */
    public float getY() {
        return y;
    }

    /**
     * @return this rectangle's width
     */
    public float getWidth() {
        return width;
    }

    /**
     * @return this rectangle's height
     */
    public float getHeight() {
        return height;
    }

    /**
     * @return x-axis corner radius
     */
    public float getRx() {
        return aw / 2;
    }

    /**
     * @return y-axis corner radius
     */
    public float getRy() {
        return ah / 2;
    }

    /**
     * @param x new rectangle x-axis origin
     */
    public void setX(final float x) {
        if (this.x == x) {
            return;
        }

        modifyingNode();
        renderingDirty();
        this.x = x;
        modifiedNode();
    }

    /**
     * @param y new rectangle y-axis origin
     */
    public void setY(final float y) {
        if (this.y == y) {
            return;
        }
        modifyingNode();
        renderingDirty();
        this.y = y;
        modifiedNode();
    }

    /**
     * @param width new rectangle width. Should be strictly positive.
     */
    public void setWidth(final float width) {
        if (width < 0) {
            throw new IllegalArgumentException();
        }

        if (this.width == width) {
            return;
        }

        modifyingNode();
        renderingDirty();
        this.width = width;
        computeCanRenderWidthBit(width);
        modifiedNode();
    }

    /**
     * @param height new rectangle height. Should be strictly positive.
     */
    public void setHeight(final float height) {
        if (height < 0) {
            throw new IllegalArgumentException();
        }

        if (this.height == height) {
            return;
        }

        modifyingNode();
        renderingDirty();
        this.height = height;
        computeCanRenderHeightBit(height);
        modifiedNode();
    }

    /**
     * @param rx new x-axis corner radius. Should be strictly positive.
     */
    public void setRx(final float rx) {
        if (rx < 0) {
            throw new IllegalArgumentException();
        }
        
        if (2 * rx == aw) {
            return;
        }

        modifyingNode();
        renderingDirty();

        if (rx > 0) {
            aw = 2 * rx;
        } else {
            aw = 0;
        }
        modifiedNode();
    }

    /**
     * @param ry new y-axis radius. Shoud be strictly positive.
     */
    public void setRy(final float ry) {
        if (ry < 0) {
            throw new IllegalArgumentException();
        }
        
        if (2 * ry == ah) {
            return;
        }
        
        modifyingNode();
        renderingDirty();
        
        if (ry > 0) {
            ah = 2 * ry;
        } else {
            ah = 0;
        }
        modifiedNode();
    }

    /**
     * @param x new x-axis origin
     * @param y new y-axis origin
     * @param width new width
     * @param height new height
     */
    public void setRect(final float x, final float y, 
                        final float width, final float height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException();
        }

        if (this.x == x 
            && this.y == y 
            && this.width == width 
            && this.height == height) {
            return;
        }

        modifyingNode();
        renderingDirty();

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        
        computeCanRenderWidthBit(width);
        computeCanRenderHeightBit(height);
        modifiedNode();
    }

    /**
     * @param rg the RenderGraphics on which to fill the shape.
     */
    public void fillShape(final RenderGraphics rg) {
        rg.fillRect(x, y, width, height, aw, ah);
    }

    /**
     * @param rg the RenderGraphics on which to draw the shape.
     */
    public void drawShape(final RenderGraphics rg) {
        rg.drawRect(x, y, width, height, aw, ah);
    }

    /**
     * @param x the hit point coordinate along the x-axis, in user space.
     * @param y the hit point coordinate along the y-axis, in user space.
     * @param fillRule the fillRule to apply when testing for containment.
     * @return true if the hit point is contained within the shape.
     */
    public boolean contains(float x, float y, final int fillRule) {
        if (aw == 0 && ah == 0) {
            return x >= this.x &&
		y >= this.y &&
		x < this.x + width &&
		y < this.y + height;
        } else {
            // This code is derived from the java.awt.geom.Rectangle2D and 
            // java.awt.geom.RoundRectangle2D
            float rrx0 = this.x;
            float rry0 = this.y;
            float rrx1 = rrx0 + width;
            float rry1 = rry0 + height;

            // Check for trivial rejection - point is outside bounding rectangle
            if (x < rrx0 || y < rry0 || x >= rrx1 || y >= rry1) {
                return false;
            }

            float aw = Math.min(width, this.aw) / 2.0f;
            float ah = Math.min(height, this.ah) / 2.0f;

            // Check which corner point is in and do circular containment
            // test - otherwise simple acceptance
            if (x >= (rrx0 += aw) && x < (rrx0 = rrx1 - aw)) {
                return true;
            }

            if (y >= (rry0 += ah) && y < (rry0 = rry1 - ah)) {
                return true;
            }

            x = (x - rrx0) / aw;
            y = (y - rry0) / ah;
            return (x * x + y * y <= 1.0);
        }
    }

    /**
     * Returns the stroked shape, using the given stroke properties.
     *
     * @param gp the <code>GraphicsProperties</code> defining the rendering
     *        context.
     * @return the shape's stroked path.
     */
    Object getStrokedPath(final GraphicsProperties gp) {
        if (aw > 0 || ah > 0) {
            return PathSupport.getStrokedRect(x, 
                                              y, 
                                              width, 
                                              height,
                                              aw,
                                              ah,
                                              gp);
        } 

        return PathSupport.getStrokedRect(x, 
                                          y,
                                          width, 
                                          height,
                                          gp);
    }

    /**
     * Rect handles x, y, rx, ry, width and height traits.
     *
     * @param traitName the name of the trait which the element may support.
     * @return true if this element supports the given trait in one of the
     *         trait accessor methods.
     */
    boolean supportsTrait(final String traitName) {
        if (SVGConstants.SVG_X_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_Y_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_RX_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_RY_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_WIDTH_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_HEIGHT_ATTRIBUTE == traitName) {
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
     * @return an array of trait aliases. These are used when the 
     * value of a trait can be used to set the value of another trait.
     * For example, on a <rect>, if the rx trait is not specified in the 
     * original XML document, the value fot eh ry trait should be used.
     */
    public String[][] getTraitAliases() {
        return new String[][] {
            {SVGConstants.SVG_RX_ATTRIBUTE, SVGConstants.SVG_RY_ATTRIBUTE},
            {SVGConstants.SVG_RY_ATTRIBUTE, SVGConstants.SVG_RX_ATTRIBUTE} };
    }

    /**
     * Rect handles x, y, rx, ry, width and height traits as
     * FloatTraitAnims
     *
     * @param traitName the trait name.
     */
    TraitAnim createTraitAnimImpl(final String traitName) {
        if (SVGConstants.SVG_X_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_Y_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_RX_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_RY_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_WIDTH_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_HEIGHT_ATTRIBUTE == traitName) {
            return new FloatTraitAnim(this, traitName, TRAIT_TYPE_FLOAT);
        } else {
            return super.createTraitAnimImpl(traitName);
        }
    }

    /**
     * Rect handles x, y, rx, ry, width and height traits.
     * Other traits are handled by the super class.
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
        if (SVGConstants.SVG_X_ATTRIBUTE == name) {
            return Float.toString(x);
        } else if (SVGConstants.SVG_Y_ATTRIBUTE == name) {
            return Float.toString(y);
        } else if (SVGConstants.SVG_RX_ATTRIBUTE == name) {
            return Float.toString(aw / 2);
        } else if (SVGConstants.SVG_RY_ATTRIBUTE == name) {
            return Float.toString(ah / 2);
        } else if (SVGConstants.SVG_WIDTH_ATTRIBUTE == name) {
            return Float.toString(width);
        } else if (SVGConstants.SVG_HEIGHT_ATTRIBUTE == name) {
            return Float.toString(height);
        } else {
            return super.getTraitImpl(name);
        }
    }

    /**
     * Rect handles x, y, rx, ry, width and height traits.
     * Other attributes are handled by the super class.
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
        if (SVGConstants.SVG_X_ATTRIBUTE == name) {
            return x;
        } else if (SVGConstants.SVG_Y_ATTRIBUTE == name) {
            return y;
        } else if (SVGConstants.SVG_RX_ATTRIBUTE == name) {
            return aw / 2;
        } else if (SVGConstants.SVG_RY_ATTRIBUTE == name) {
            return ah / 2;
        } else if (SVGConstants.SVG_WIDTH_ATTRIBUTE == name) {
            return width;
        } else if (SVGConstants.SVG_HEIGHT_ATTRIBUTE == name) {
            return height;
        } else {
            return super.getFloatTraitImpl(name);
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
        if (SVGConstants.SVG_X_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_Y_ATTRIBUTE == traitName) {
            return new float[][] {{parseFloatTrait(traitName, value)}};
        } else if (SVGConstants.SVG_RX_ATTRIBUTE == traitName
                   ||
                   SVGConstants.SVG_RY_ATTRIBUTE == traitName
                   ||
                   SVGConstants.SVG_WIDTH_ATTRIBUTE == traitName
                   ||
                   SVGConstants.SVG_HEIGHT_ATTRIBUTE == traitName) {
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
     * Rect handles x, y, rx, ry, width and height traits.
     * Other traits are handled by the super class.
     *
     * @param name the trait's name (e.g., "rx")
     * @param value the new trait string value (e.g., "10")
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
        if (SVGConstants.SVG_X_ATTRIBUTE == name) {
            setX(parseFloatTrait(name, value));
        } else if (SVGConstants.SVG_Y_ATTRIBUTE == name) {
            setY(parseFloatTrait(name, value));
        } else if (SVGConstants.SVG_RX_ATTRIBUTE == name) {
            setRx(parsePositiveFloatTrait(name, value));
        } else if (SVGConstants.SVG_RY_ATTRIBUTE == name) {
            setRy(parsePositiveFloatTrait(name, value));
        } else if (SVGConstants.SVG_WIDTH_ATTRIBUTE == name) {
            setWidth(parsePositiveFloatTrait(name, value));
        } else if (SVGConstants.SVG_HEIGHT_ATTRIBUTE == name) {
            setHeight(parsePositiveFloatTrait(name, value));
        } else {
            super.setTraitImpl(name, value);
        }
    }

    /**
     * Rect handles x, y, rx, ry, width and height traits.
     * Other traits are handled by the super class.
     *
     * @param name the trait's name (e.g., "x")
     * @param value the new trait value (e.g., 20f)
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
        if (SVGConstants.SVG_X_ATTRIBUTE == name) {
            setX(value);
        } else if (SVGConstants.SVG_Y_ATTRIBUTE == name) {
            setY(value);
        } else if (SVGConstants.SVG_RX_ATTRIBUTE == name) {
            checkPositive(name, value);
            setRx(value);
        } else if (SVGConstants.SVG_RY_ATTRIBUTE == name) {
            checkPositive(name, value);
            setRy(value);
        } else if (SVGConstants.SVG_WIDTH_ATTRIBUTE == name) {
            checkPositive(name, value);
            setWidth(value);
        } else if (SVGConstants.SVG_HEIGHT_ATTRIBUTE == name) {
            checkPositive(name, value);
            setHeight(value);
        } else {
            super.setFloatTraitImpl(name, value);
        }
    }

    /**
     * @param name the name of the trait to convert.
     * @param value the float trait value to convert.
     */
    String toStringTrait(final String name, final float[][] value) {
        if (SVGConstants.SVG_X_ATTRIBUTE == name
            ||
            SVGConstants.SVG_Y_ATTRIBUTE == name
            ||
            SVGConstants.SVG_RX_ATTRIBUTE == name
            ||
            SVGConstants.SVG_RY_ATTRIBUTE == name
            ||
            SVGConstants.SVG_WIDTH_ATTRIBUTE == name
            ||
            SVGConstants.SVG_HEIGHT_ATTRIBUTE == name) {
            return Float.toString(value[0][0]);
        } else {
            return super.toStringTrait(name, value);
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
        if (SVGConstants.SVG_X_ATTRIBUTE == name) {
            setX(value[0][0]);
        } else if (SVGConstants.SVG_Y_ATTRIBUTE == name) {
            setY(value[0][0]);
        } else if (SVGConstants.SVG_RX_ATTRIBUTE == name) {
            checkPositive(name, value[0][0]);
            setRx(value[0][0]);
        } else if (SVGConstants.SVG_RY_ATTRIBUTE == name) {
            checkPositive(name, value[0][0]);
            setRy(value[0][0]);
        } else if (SVGConstants.SVG_WIDTH_ATTRIBUTE == name) {
            checkPositive(name, value[0][0]);
            setWidth(value[0][0]);
        } else if (SVGConstants.SVG_HEIGHT_ATTRIBUTE == name) {
            checkPositive(name, value[0][0]);
            setHeight(value[0][0]);
        } else {
            super.setFloatArrayTrait(name, value);
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
        return addTransformedBBox(bbox, x, y, width, height, t);
    }

    /**
     * Debug helper.
     * 
     * @return a textual description of the rectangle including id 
     *         and geometry information.
     */
    /*
    public String toString() {
        if (isRounded) {
            return "RoundedRect[id(" + getId() + ") " + rr.getX() + ", " 
                + rr.getY() + ", " + rr.getWidth() + ", " + rr.getHeight() 
                + ", aw(" + rr.getArcWidth() + ") ah(" + rr.getArcHeight() 
                + ")]";
        } else {
            return "Rect[id(" + getId() + ") " + r.getX() + ", " 
                + r.getY() + ", " + r.getWidth() + ", " + r.getHeight() 
                + "]";
        }
    }
    */

}
