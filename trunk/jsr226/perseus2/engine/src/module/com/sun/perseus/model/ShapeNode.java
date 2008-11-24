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

import org.w3c.dom.svg.SVGPath;
import org.w3c.dom.svg.SVGRect;

import com.sun.perseus.j2d.Box;
import com.sun.perseus.j2d.Transform;
import com.sun.perseus.j2d.Path;
import com.sun.perseus.j2d.PathSupport;

/**
 * A <tt>ShapesNode</tt> is an <tt>AbstractShapeNode</tt>
 * which draws a <tt>java.awt.geom.GeneralPath</tt>.
 * <br />
 * Note that the <tt>paint</tt> method throws a <tt>NullPointerException</tt>
 * when called before setting the <tt>shape</tt> property to a non-null
 * value.
 * <br />
 *
 * @version $Id: ShapeNode.java,v 1.17 2006/06/29 10:47:34 ln156897 Exp $
 */
public class ShapeNode 
    extends AbstractShapeNode {
    /**
     * EMPTY_PATH is used initially by the ShapeNode class.
     * This constant is used so that a new ShapeNode, right upon creation,
     * may be rendered. The only place where the path is mutated is in
     * the animation code. But the animation can only work if the 
     * path has real data. Therefore, it is safe to use this final static
     * constant to initialize all ShapeNode instances (and avoid instantiating
     * Path objects that are always tossed away later on).
     */
    static final Path EMPTY_PATH = new Path();

    /**
     * The d attribute is required on <path>
     */
    static final String[] PATH_REQUIRED_TRAITS
        = {SVGConstants.SVG_D_ATTRIBUTE};

    /**
     * The points attribute is required on <polygon> and <polyline>
     */
    static final String[] POLY_ALL_REQUIRED_TRAITS
        = {SVGConstants.SVG_POINTS_ATTRIBUTE};

    /**
     * The Path painted by this node. Should _never_ be set to null.
     */
    protected Path path = EMPTY_PATH;

    /**
     * The path's local name. One of 
     * SVGConstants.SVG_PATH_TAG, SVGConstants.SVG_POLYGON_TAG,
     * or SVGConstants.SVG_POLYLINE_TAG.
     */
    protected String localName;

    /**
     * Constructor.
     *
     * @param ownerDocument this element's owner <code>DocumentNode</code>
     */
    public ShapeNode(final DocumentNode ownerDocument) {
        this(ownerDocument, SVGConstants.SVG_PATH_TAG);
        
        // The ShapeNode initially has an empty path, so that is reflected in 
        // the canRenderState.
        canRenderState |= CAN_RENDER_EMPTY_PATH_BIT;
    }

    /**
     * Constructs a new ShapeNode to represent an path, polyline,
     * or polygon (depending on the localName value).
     *
     * @param ownerDocument this element's owner <code>DocumentNode</code>
     * @param localName the element's local name. One of 
     * SVGConstants.SVG_PATH_TAG, SVGConstants.SVG_POLYGON_TAG,
     * or SVGConstants.SVG_POLYLINE_TAG.
     */
    public ShapeNode(final DocumentNode ownerDocument,
                     final String localName) {
        super(ownerDocument);

        if (SVGConstants.SVG_POLYLINE_TAG == localName
            ||
            SVGConstants.SVG_POLYGON_TAG == localName
            ||
            SVGConstants.SVG_PATH_TAG == localName) {
            this.localName = localName;
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * @return the SVGConstants.SVG_PATH_TAG value
     */
    public String getLocalName() {
        return localName;
    }

    /**
     * Used by <code>DocumentNode</code> to create a new instance from
     * a prototype <code>SVG</code>.
     *
     * @param doc the <code>DocumentNode</code> for which a new node is
     *        should be created.
     * @return a new <code>SVG</code> for the requested document.
     */
    public ElementNode newInstance(final DocumentNode doc) {
        return new ShapeNode(doc, localName);
    }

    /**
     * @return the Path drawn by this node
     */
    public Path getShape() {
        return path;
    }

    /**
     * @param newPath the new path for this node
     */
    public void setPath(final Path newPath) {
        if (equal(newPath, path)) {
            return;
        }

        modifyingNode();
        renderingDirty();
        if (newPath != null) {
            this.path = newPath;
        } else {
            this.path = new Path();
        }
        computeCanRenderEmptyPathBit(this.path);
        modifiedNode();
    }

    /**
     * @param rg the RenderGraphics on which to fill the shape.
     */
    public void fillShape(final RenderGraphics rg) {
        rg.fill(path);
    }

    /**
     * @param rg the RenderGraphics on which to draw the shape.
     */
    public void drawShape(final RenderGraphics rg) {
        rg.draw(path);
    }

    /**
     * @param x the hit point coordinate along the x-axis, in user space.
     * @param y the hit point coordinate along the y-axis, in user space.
     * @param fillRule the fillRule to apply when testing for containment.
     * @return true if the hit point is contained within the shape.
     */
    public boolean contains(final float x, final float y, final int fillRule) {
        return PathSupport.isHit(path, fillRule, x, y);
    }

    /**
     * Returns the stroked shape, using the given stroke properties.
     *
     * @param gp the <code>GraphicsProperties</code> defining the rendering
     *        context.
     * @return the shape's stroked path.
     */
    Object getStrokedPath(final GraphicsProperties gp) {
        return PathSupport.getStrokedPath(path, gp);
    }

    /**
     * ShapeNode handles the 'd' trait.
     *
     * @param traitName the name of the trait which the element may support.
     * @return true if this element supports the given trait in one of the
     *         trait accessor methods.
     */
    boolean supportsTrait(final String traitName) {
        if (SVGConstants.SVG_PATH_TAG == localName) {
            if (SVGConstants.SVG_D_ATTRIBUTE == traitName) {
                return true;
            }
        } else if (SVGConstants.SVG_POINTS_ATTRIBUTE == traitName) {
            // For polygon and polyline, the points trait is
            // supported.
            return true;
        }
        
        return super.supportsTrait(traitName);
    }

    /**
     * @return an array of traits that are required by this element.
     */
    public String[] getRequiredTraits() {
        if (SVGConstants.SVG_PATH_TAG == localName) {
            return PATH_REQUIRED_TRAITS;
        } else {
            return POLY_ALL_REQUIRED_TRAITS;
        }
    }

    /**
     * ShapeNode handles the 'd' trait.
     *
     * @param name the trait name (e.g., "d")
     * @return the trait's value as a string (e.g., "M0,0L50,20Z")
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to a String (SVG Tiny only).
     */
    public String getTraitImpl(final String name)
        throws DOMException {
        if (SVGConstants.SVG_PATH_TAG == localName) {
            if (SVGConstants.SVG_D_ATTRIBUTE == name) {
                return path.toString();
            }
        } else if (SVGConstants.SVG_POINTS_ATTRIBUTE == name) {
            return path.toPointsString();
        }
        return super.getTraitImpl(name);
    }

    /**
     * ShapeNode handles the 'd' trait.
     *
     * @param name the trait's name (e.g, "d")
     * @return the trait's SVGPath value.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to {@link
     * org.w3c.dom.svg.SVGPath SVGPath}
     * @throws SecurityException if the application does not have the necessary
     * privilege rights to access this (SVG) content.
     */
    SVGPath getPathTraitImpl(final String name)
        throws DOMException {
        if (SVGConstants.SVG_PATH_TAG.equals(localName)
            && SVGConstants.SVG_D_ATTRIBUTE.equals(name)) {
            return new Path(path);
        } else {
            return super.getPathTraitImpl(name);
        }
    }

    /**
     * @param traitName the trait name.
     */
    TraitAnim createTraitAnimImpl(final String traitName) {
        if (SVGConstants.SVG_D_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_POINTS_ATTRIBUTE == traitName) {
            return new FloatTraitAnim(this, traitName, TRAIT_TYPE_SVG_PATH);
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
        if (SVGConstants.SVG_D_ATTRIBUTE == name
            ||
            SVGConstants.SVG_POINTS_ATTRIBUTE == name) {
            if (!path.equals(value)) {
                modifyingNode();
                renderingDirty();
                path.setData(value[0]);
                modifiedNode();
            }
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
        if (SVGConstants.SVG_D_ATTRIBUTE == traitName) {
            Path path = parsePathTrait(traitName, value);
            return toAnimatedFloatArray(path);
        } else if (SVGConstants.SVG_POINTS_ATTRIBUTE == traitName) {
            Path path = parsePointsTrait(traitName, value);
            if (SVGConstants.SVG_POLYGON_TAG == localName) {
                path.close();
            }
            return toAnimatedFloatArray(path);
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
     * ShapeNode handles the 'd' trait if it is a path and the 
     * 'points' trait if it is a polygon or a polyline.
     *
     * @param name the trait's name (e.g, "d")
     * @param value the trait's new value (e.g., "M0, 0 L50, 50 Z"), using the
     *        SVG path syntax.
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is an invalid value for the given trait or null.
     * @throws DOMException with error code NO_MODIFICATION_ALLOWED_ERR: if
     * attempt is made to change readonly trait.
     */
    public void setTraitImpl(final String name, final String value)
        throws DOMException {
        if (SVGConstants.SVG_PATH_TAG == localName) {
            if (SVGConstants.SVG_D_ATTRIBUTE == name) {
                setPath(parsePathTrait(name, value));
            } else {
                super.setTraitImpl(name, value);
            }
        } else if (SVGConstants.SVG_POINTS_ATTRIBUTE == name) {
            setPath(parsePointsTrait(name, value));
            
            // As per the specification, section 9.7 of the SVG 1.1 spec.,
            // we need to close the path if we are dealing with a polygon.
            // Only do so if the document does not have a delayed exception
            // which means this element was loading and an error was detected 
            // in the points data.
            if (ownerDocument.getDelayedException() == null) {
                if (SVGConstants.SVG_POLYGON_TAG == localName) {
                    path.close();
                }
            }
        } else {
            super.setTraitImpl(name, value);
        }
    }

    /**
     * @param name the name of the trait to convert.
     * @param value the float trait value to convert.
     */
    String toStringTrait(final String name, final float[][] value) {
        if (SVGConstants.SVG_PATH_TAG.equals(localName)
            &&
            SVGConstants.SVG_D_ATTRIBUTE.equals(name)) {
            return path.toString(value[0]);
        } else if (SVGConstants.SVG_POINTS_ATTRIBUTE.equals(name)) {
            return path.toPointsString(value[0]);
        } else {
            return super.toStringTrait(name, value);
        }
    }

    /**
     * ShapeNode handles the 'd' trait.
     *
     * @param name the trait's name (e.g., "d")
     * @param value the trait's new SVGPath value.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if the requested
     * trait's value cannot be specified as an {@link org.w3c.dom.svg.SVGPath
     * SVGPath}
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is an invalid value for the given trait or null.  SVGPath is
     * invalid if it does not begin with a MOVE_TO segment.
     */
    void setPathTraitImpl(final String name, final SVGPath path)
        throws DOMException {
        // Note that here, we use equals because the strings 
        // have not been interned.
        if (SVGConstants.SVG_PATH_TAG.equals(localName)
            &&
            SVGConstants.SVG_D_ATTRIBUTE.equals(name)) {
            if (path.getNumberOfSegments() > 0
                &&
                path.getSegment(0) != SVGPath.MOVE_TO) {
                // The first command _must_ be a moveTo.
                // However, note than an empty path is accepted.
                throw illegalTraitValue(name, path.toString());
            }
            setPath(new Path((Path) path));
        } else {
            super.setPathTraitImpl(name, path);
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
        return addShapeBBox(bbox, path, t);
    }
}
