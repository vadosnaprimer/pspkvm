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

import com.sun.perseus.util.SVGConstants;

import org.w3c.dom.DOMException;

import org.w3c.dom.svg.SVGRect;
import org.w3c.dom.svg.SVGMatrix;
import org.w3c.dom.svg.SVGPath;
import org.w3c.dom.svg.SVGPoint;
import org.w3c.dom.svg.SVGRGBColor;
import org.w3c.dom.svg.SVGException;
import org.w3c.dom.svg.SVGSVGElement;

import com.sun.perseus.j2d.Box;
import com.sun.perseus.j2d.Transform;
import com.sun.perseus.j2d.RGB;
import com.sun.perseus.j2d.Path;

/**
 * An <code>SVG</code> node represents an <code>&lt;code&gt;</code>
 * element. In addition to a standard <code>StructureNode</code>, an
 * <code>SVG</code> node can have a view box which causes a transform
 * to be applied before drawing its children. This transform maps children
 * viewBox coordinates into the SVG's parent <code>Viewport</code>
 * coordinate system.
 *
 * @see Viewport
 *
 * @version $Id: SVG.java,v 1.12 2006/06/29 10:47:34 ln156897 Exp $
 */
public class SVG extends StructureNode implements SVGSVGElement, SVGPoint {
    /**
     * This SVG node's viewBox. If null, then there is
     * no viewBox.
     */
    protected float[][] viewBox;

    /**
     * Controls the way the svg viewBox is aligned in
     * the parent viewport. For SVG Tiny, one of 
     * ALIGN_XMIDYMID or ALIGN_NONE.
     */
    protected int align = ALIGN_XMIDYMID;

    /**
     * The SVG document's requested width
     */
    protected float width = 100;

    /**
     * The SVG document's requested height
     */
    protected float height = 100;

    /**
     * The current scale
     */
    protected float currentScale = 1;

    /**
     * The current translate along the x axis
     */
    protected float tx;

    /**
     * The current translate along the y axis
     */
    protected float ty;

    /**
     * The current rotation angle.
     */
    protected float currentRotate;

    /**
     * Precision adjustment, needed to keep the delta between the current
     * time and the long values used internally.
     */
    protected float currentTimeDelta = 0f;

    /**
     * Constructor.
     *
     * @param ownerDocument this element's owner <code>DocumentNode</code>
     */
    public SVG(final DocumentNode ownerDocument) {
        super(ownerDocument);
    }

    /**
     * @return the SVGConstants.SVG_SVG_TAG value
     */
    public String getLocalName() {
        return SVGConstants.SVG_SVG_TAG;
    }

    /**
     * The default value for the width trait and the height trait 
     * is '100%'.
     *
     * @return an array of trait default values, used if this element
     *         requires that the default trait value be explicitly 
     *         set through a setTrait call. This happens, for example,
     *         with the begin trait value on animation elements.
     */
    public String[][] getDefaultTraits() {
        return new String[][] { {SVGConstants.SVG_WIDTH_ATTRIBUTE, "100%"},
                                {SVGConstants.SVG_HEIGHT_ATTRIBUTE, "100%"}};
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
        return new SVG(doc);
    }

    /**
     * Sets the viewport width. 
     * @param newWidth Should be greater than 0
     */
    public void setWidth(final float newWidth) {
        if (newWidth < 0) {
            throw new IllegalArgumentException();
        }

        if (newWidth == width) {
            return;
        }

        modifyingNode();
        this.width = newWidth;
        computeCanRenderWidthBit(width);

        modifiedNode();
    }

    /**
     * @return the viewport's  width
     */
    public float getWidth() {
        return this.width;
    }

    /**
     * Sets the viewport  height. 
     * @param newHeight Should be greater than 0
     */
    public void setHeight(final float newHeight) {
        if (newHeight < 0) {
            throw new IllegalArgumentException();
        }

        if (newHeight == height) {
            return;
        }

        modifyingNode();
        this.height = newHeight;
        computeCanRenderHeightBit(height);
        modifiedNode();
    }

    /**
     * @return the viewport  height
     */
    public float getHeight() {
        return this.height;
    }

    /**
     * Sets a new value for the viewBox.
     * @param newViewBox the new viewBox for this <tt>SVG</tt>
     * 
     * @throws IllegalArgumentException if the input viewBox is 
     *         not null and not of a float {float[2], float[1], float[1]} array.
     */
    public void setViewBox(final float[][] newViewBox) {

        if (newViewBox != null) {
            if (newViewBox.length != 3 
                ||
                newViewBox[0] == null
                ||
                newViewBox[1] == null
                ||
                newViewBox[2] == null
                ||
                newViewBox[0].length != 2
                ||
                newViewBox[1][0] < 0 
                || 
                newViewBox[2][0] < 0) {
                throw new IllegalArgumentException();
            }
        }

        if (equal(newViewBox, viewBox)) {
            return;
        }

        modifyingNode();

        if (newViewBox == null) {
            viewBox = null;
        } else {
            if (viewBox == null) {
                viewBox = new float[3][];
                viewBox[0] = new float[2];
                viewBox[1] = new float[1];
                viewBox[2] = new float[1];
            }
            
            viewBox[0][0] = newViewBox[0][0];    
            viewBox[0][1] = newViewBox[0][1];    
            viewBox[1][0] = newViewBox[1][0];    
            viewBox[2][0] = newViewBox[2][0];    
        }

        recomputeTransformState();
        computeCanRenderEmptyViewBoxBit(viewBox);
        modifiedNode();
    }

    /**
     * @return this SVG's viewBox
     */
    public float[][] getViewBox() {
        return viewBox;
    }

    /**
     * @param newAlign new alignment property
     */
    public void setAlign(final int newAlign) {
        if (newAlign != ALIGN_XMIDYMID
            &&
            newAlign != ALIGN_NONE) {
            throw new IllegalArgumentException();
        }

        if (newAlign == align) {
            return;
        }

        modifyingNode();
        this.align = newAlign;
        recomputeTransformState();
        modifiedNode();
    }

    /**
     * @return this node's align property
     */
    public int getAlign() {
        return align;
    }

    /**
     * Appends the viewBox to viewport transform, if there is a viewBox.
     *
     * @param tx the <code>Transform</code> to apply additional node 
     *        transforms to. This may be null.
     * @param workTx a <code>Transform</code> which can be re-used if a 
     *        new <code>Transform</code> needs to be created and workTx
     *        is not the same instance as tx.
     * @return a transform with this node's transform added.
     */
    protected Transform appendTransform(Transform tx,
                                        final Transform workTx) {
        if (viewBox == null) {
            return tx;
        }

        tx = recycleTransform(tx, workTx);

        Viewport vp = (Viewport) getOwnerDocument();
        float w = vp.getWidth();
        float h = vp.getHeight();
        getFittingTransform(viewBox[0][0], viewBox[0][1],
                            viewBox[1][0], viewBox[2][0],
                            0, 0, w, h, align, tx);
        
        return tx;
    }

    /**
     * SVG handles the version, baseProfile, viewBox, zoomAndPan,
     * width, height and preserveAspectRatio traits.
     *
     * @param traitName the name of the trait which the element may support.
     * @return true if this element supports the given trait in one of the
     *         trait accessor methods.
     */
    boolean supportsTrait(final String traitName) {
        if (SVGConstants.SVG_BASE_PROFILE_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_VERSION_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_VIEW_BOX_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_ZOOM_AND_PAN_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_WIDTH_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_HEIGHT_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE == traitName) {
            return true;
        } else {
            return super.supportsTrait(traitName);
        }
    }

    /**
     * SVG handles the version, baseProfile, viewBox and zoomAndPan traits.
     *
     * @param name the requested trait's name (e.g., "zoomAndPan")
     * @return the requested trait string value (e.g., "disable")
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to a String (SVG Tiny only).
     */
    public String getTraitImpl(final String name)
        throws DOMException {
        if (SVGConstants.SVG_VERSION_ATTRIBUTE == name) {
            return SVGConstants.SVG_VERSION_1_1_VALUE;
        } else if (SVGConstants.SVG_VIEW_BOX_ATTRIBUTE == name) {
            if (viewBox == null) {
                return "";
            } else {
                return "" + viewBox[0][0] + SVGConstants.COMMA 
                    + viewBox[0][1] + SVGConstants.COMMA 
                    + viewBox[1][0] + SVGConstants.COMMA 
                    + viewBox[2][0];
            }
        } else if (SVGConstants.SVG_ZOOM_AND_PAN_ATTRIBUTE == name) {
            switch (ownerDocument.zoomAndPan) {
            case Viewport.ZOOM_PAN_MAGNIFY:
                return SVGConstants.SVG_MAGNIFY_VALUE;
            case Viewport.ZOOM_PAN_DISABLE:
                return SVGConstants.SVG_DISABLE_VALUE;
            default:
                throw new Error();
            }
        } else if (SVGConstants.SVG_BASE_PROFILE_ATTRIBUTE == name) {
            return SVGConstants.SVG_BASE_PROFILE_TINY_VALUE;
        } else if (SVGConstants.SVG_WIDTH_ATTRIBUTE == name) {
            return Float.toString(getWidth());
        } else if (SVGConstants.SVG_HEIGHT_ATTRIBUTE == name) {
            return Float.toString(getHeight());
        } else if (SVGConstants.SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE == name) {
            return alignToStringTrait(align);
        } else {
            return super.getTraitImpl(name);
        }
    }

    /**
     * SVG handles width and height float traits.
     * Other attributes are handled by the super class.
     *
     * @param name the requested trait name.
     * @param the requested trait's floating point value.
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
        if (SVGConstants.SVG_WIDTH_ATTRIBUTE == name) {
            return getWidth();
        } else if (SVGConstants.SVG_HEIGHT_ATTRIBUTE == name) {
            return getHeight();
        } else {
            return super.getFloatTraitImpl(name);
        }
    }

    /**
     * SVG handles the viewBox Rect trait.
     *
     * @param name the requested trait name (e.g., "viewBox")
     * @return the requested trait SVGRect value.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to {@link
     * org.w3c.dom.svg.SVGRect SVGRect}
     * @throws SecurityException if the application does not have the necessary
     * privilege rights to access this (SVG) content.
     */
    SVGRect getRectTraitImpl(final String name)
        throws DOMException {
        if (SVGConstants.SVG_VIEW_BOX_ATTRIBUTE.equals(name)) {
            return toSVGRect(viewBox);
        } else {
            return super.getRectTraitImpl(name);
        }
    }

    /**
     * @param traitName the trait name.
     */
    TraitAnim createTraitAnimImpl(final String traitName) {
        if (SVGConstants.SVG_VIEW_BOX_ATTRIBUTE == traitName) {
            return new FloatTraitAnim(this, traitName, TRAIT_TYPE_SVG_RECT);
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
        if (SVGConstants.SVG_WIDTH_ATTRIBUTE == name) {
            checkPositive(name, value[0][0]);
            setWidth(value[0][0]);
        } else if (SVGConstants.SVG_HEIGHT_ATTRIBUTE == name) {
            checkPositive(name, value[0][0]);
            setHeight(value[0][0]);
        } else if (SVGConstants.SVG_VIEW_BOX_ATTRIBUTE == name) {
            setViewBox(value);
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
        if (SVGConstants.SVG_VIEW_BOX_ATTRIBUTE == traitName) {
            return ownerDocument.viewBoxParser.parseViewBox(value);
        } else if (SVGConstants.SVG_WIDTH_ATTRIBUTE == traitName
                   ||
                   SVGConstants.SVG_HEIGHT_ATTRIBUTE == traitName) {
            return new float[][] {
                        {parsePositiveLengthTrait(traitName, value, false)}
                    };
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
     * SVG handles the version, baseProfile, viewBox and zoomAndPan traits.
     *
     * @param name the trait's name (e.g., "viewBox")
     * @param value the new trait's string value (e.g., "0 0 400 300")
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is an invalid value for the given trait or null.
     * @throws DOMException with error code NO_MODIFICATION_ALLOWED_ERR: if
     * attempt is made to change readonly trait.
     */
    public void setTraitImpl(final String name, final String value)
        throws DOMException {
        if (SVGConstants.SVG_VERSION_ATTRIBUTE == name) {
            checkWriteLoading(name);
            if (!SVGConstants.SVG_VERSION_1_1_VALUE.equals(value)
                &&
                !SVGConstants.SVG_VERSION_1_2_VALUE.equals(value)) {
                throw illegalTraitValue(name, value);
            }
        } else if (SVGConstants.SVG_BASE_PROFILE_ATTRIBUTE == name) {
            checkWriteLoading(name);
            if (!SVGConstants.SVG_BASE_PROFILE_TINY_VALUE.equals(value)) {
                throw illegalTraitValue(name, value);
            }
        } else if (SVGConstants.SVG_VIEW_BOX_ATTRIBUTE == name) {
            setViewBox(toViewBox(name, value));
        } else if (SVGConstants.SVG_ZOOM_AND_PAN_ATTRIBUTE == name) {
            if (SVGConstants.SVG_MAGNIFY_VALUE.equals(value)) {
                ownerDocument.setZoomAndPan(Viewport.ZOOM_PAN_MAGNIFY);
                applyUserTransform();
            } else if (SVGConstants.SVG_DISABLE_VALUE.equals(value)) {
                ownerDocument.setZoomAndPan(Viewport.ZOOM_PAN_DISABLE);
                applyUserTransform();
            } else {
                throw illegalTraitValue(name, value);
            }
        } else if (SVGConstants.SVG_WIDTH_ATTRIBUTE == name) {
            checkWriteLoading(name);
            setWidth(parsePositiveLengthTrait(name, value, true));
        } else if (SVGConstants.SVG_HEIGHT_ATTRIBUTE == name) {
            checkWriteLoading(name);
            setHeight(parsePositiveLengthTrait(name, value, false));
        } else if (SVGConstants.SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE == name) {
            checkWriteLoading(name);
            if (SVGConstants
                .SVG_SVG_PRESERVE_ASPECT_RATIO_DEFAULT_VALUE.equals(value)) {
                setAlign(ALIGN_XMIDYMID);
            } else if (SVGConstants.SVG_NONE_VALUE.equals(value)) {
                setAlign(ALIGN_NONE);
            } else {
                throw illegalTraitValue(name, value);
            }
        } else {
            super.setTraitImpl(name, value);
        }
    }

    /**
     * SVG handles the viewBox Rect trait.
     *
     * @param name the trait name (e.g., "viewBox"
     * @param rect the trait value
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if the requested
     * trait's value cannot be specified as an {@link org.w3c.dom.svg.SVGRect
     * SVGRect}
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is an invalid value for the given trait or null.  SVGRect is
     * invalid if the width or height values are set to negative.
     * @throws SecurityException if the application does not have the necessary
     * privilege rights to access this (SVG) content.
     */
    public void setRectTraitImpl(final String name, final SVGRect rect)
        throws DOMException {
        // Note that here, we use equals because the string 
        // has not been interned.
        if (SVGConstants.SVG_VIEW_BOX_ATTRIBUTE.equals(name)) {
            if (rect == null) {
                throw illegalTraitValue(name, null);
            }
            
            if (rect.getWidth() < 0 || rect.getHeight() < 0) {
                throw illegalTraitValue(name, toStringTrait(new float[]
                    {rect.getX(), 
                     rect.getY(), 
                     rect.getWidth(), 
                     rect.getHeight()}));
            }
                
            setViewBox(new float[][]
                {new float[] {rect.getX(), rect.getY()}, 
                 new float[] {rect.getWidth()}, 
                 new float[] {rect.getHeight()}
                });
            
        } else {
            super.setRectTraitImpl(name, rect);
        }
    }

    /**
     * SVG handles width and height traits.
     * Other traits are handled by the super class.
     *
     * @param name the trait's name.
     * @param value the new trait's floating point value.
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
        try {
            if (SVGConstants.SVG_WIDTH_ATTRIBUTE == name) {
                setWidth(value);
            } else if (SVGConstants.SVG_HEIGHT_ATTRIBUTE == name) {
                setHeight(value);
            } else {
                super.setFloatTraitImpl(name, value);
            }
        } catch (IllegalArgumentException iae) {
            throw illegalTraitValue(name, Float.toString(value));
        }
    }

    /**
     * @param name the name of the trait to convert.
     * @param value the float trait value to convert.
     */
    String toStringTrait(final String name, final float[][] value) {
        if (SVGConstants.SVG_WIDTH_ATTRIBUTE == name
            ||
            SVGConstants.SVG_HEIGHT_ATTRIBUTE == name) {
            return Float.toString(value[0][0]);
        } else if (SVGConstants.SVG_VIEW_BOX_ATTRIBUTE == name) {
            float[] vb = {value[0][0], value[0][1], value[1][0], value[2][0]};
            return toStringTrait(vb);
        } else {
            return super.toStringTrait(name, value);
        }
    }

    // ========================================================================

    /**
     *
     */
    public void setCurrentScale(final float value)
        throws DOMException {
        if (value == 0) {
            throw new DOMException
                (DOMException.INVALID_ACCESS_ERR, 
                 Messages.formatMessage
                 (Messages.ERROR_INVALID_PARAMETER_VALUE,
                  new String[] {"SVGSVGElement",
                                "setCurrentScale",
                                "value",
                                Float.toString(value)}));
        } else {
            currentScale = value;
            applyUserTransform();
        }
    }

    
    /**
     * @return the state of the animation timeline.
     */
    public boolean animationsPaused() {
        throw new Error("NOT IMPLEMENTED");
    }

    /**
     * Pauses animation timeline. If user agent has pause/play control that
     * control is changed to paused state.  Has no effect if timeline already
     * paused.
     */
    public void pauseAnimations() {
        throw new Error("NOT IMPLEMENTED");
    }

    /**
     * Resumes animation timeline. If user agent has pause/play control that
     * control is changed to playing state. Has no effect if timeline is already
     * playing.
     */
    public void unpauseAnimations() {
        throw new Error("NOT IMPLEMENTED");
    }

    /**
     *
     */
    public float getCurrentScale() {
        return currentScale;
    }

    /**
     * <The position and size of the viewport (implicit or explicit) that
     * corresponds to this 'svg' element. When the user agent is actually
     * rendering the content, then the position and size values represent the
     * actual values when rendering.  If this SVG document is embedded as part
     * of another document (e.g., via the HTML 'object' element), then the
     * position and size are unitless values in the coordinate system of the
     * parent document. (If the parent uses CSS or XSL layout, then unitless
     * values represent pixel units for the current CSS or XSL viewport, as
     * described in the CSS2 specification.)  If the parent element does not
     * have a coordinate system, then the user agent should provide reasonable
     * default values for this attribute.
     *
     * <p> The object itself and its contents are both readonly. {@link
     * org.w3c.dom.DOMException DOMException} with error code
     * NO_MODIFICATION_ALLOWED_ERR is raised if attempt is made to modify
     * it. The returned SVGRect object is "live", i.e. its x, y, width, height
     * is automatically updated if viewport size or position changes.  </p>
     */
    public SVGRect getViewport() {
        throw new Error("NOT IMPLEMENTED");
    }

    /**
     *
     */
    public void setCurrentRotate(final float value) {
        currentRotate = value;
        applyUserTransform();
    }

    /**
     *
     */
    public float getCurrentRotate() {
        return currentRotate;
    }

    /**
     * The initial values for currentTranslate is SVGPoint(0,0).
     */
    public SVGPoint getCurrentTranslate() {
        return this;
    }

    /**
     *
     */
    public float getCurrentTime() {
        return ownerDocument.timeContainerRootSupport
            .lastSampleTime.value / 1000f + currentTimeDelta;
    }

    // JAVADOC COMMENT ELIDED
    public void setCurrentTime(final float seconds) {
        currentTimeDelta = seconds - ((long) (seconds * 1000)) / 1000f;
        ownerDocument.timeContainerRootSupport.seekTo
            (new Time((long) (seconds * 1000)));
        ownerDocument.applyAnimations();
    }

    /**
     * This
     * object can be used to modify value of traits which are compatible with
     * {@link org.w3c.dom.svg.SVGMatrix SVGMatrix} type using {@link
     * org.w3c.dom.svg.SVGElement#setMatrixTrait setMatrixTrait} method. The
     * internal representation of the matrix is as follows: <p>
     *
     * <pre>
     *  [  a  c  e  ]
     *  [  b  d  f  ]
     *  [  0  0  1  ]
     * </pre>
     * </p>
     *
     * @see org.w3c.dom.svg.SVGMatrix
     * @param a the 0,0 matrix parameter
     * @param b the 1,0 matrix parameter
     * @param c the 0,1 matrix parameter
     * @param d the 1,1 matrix parameter
     * @param e the 0,2 matrix parameter
     * @param f the 1,2 matrix parameter
     * @return a new <code>SVGMatrix</code> object with the requested 
     *         components.
     */

    public SVGMatrix createSVGMatrixComponents
        (final float a, 
         final float b, 
         final float c, 
         final float d, 
         final float e, 
         final float f) {
        return new Transform(a, b, c, d, e, f);
    }

    /**
     * @return a new {@link org.w3c.dom.svg.SVGRect SVGRect} object. This object
     * can be used to modify value of traits which are compatible with {@link
     * org.w3c.dom.svg.SVGRect SVGRect} type using {@link
     * org.w3c.dom.svg.SVGElement#setRectTrait setRectTrait} method. The intial
     * values for x, y, width, height of this new SVGRect are zero.
     */
    public SVGRect createSVGRect() {
        return new Box(0, 0, 0, 0);
    }

    /**
     * @return new {@link org.w3c.dom.svg.SVGPath SVGPath} object. This object
     * can be used to modify value of traits which are compatible with {@link
     * org.w3c.dom.svg.SVGPath SVGPath} type using {@link
     * org.w3c.dom.svg.SVGElement#setPathTrait setPathTrait} method.
     */
    public SVGPath createSVGPath() {
        return new Path();
    }

    /**
     * @return new {@link org.w3c.dom.svg.SVGRGBColor SVGRGBColor} object. This
     * object can be used to modify value of traits which are compatible with
     * {@link org.w3c.dom.svg.SVGRGBColor SVGRGBColor} type using {@link
     * org.w3c.dom.svg.SVGElement#setRGBColorTrait setRGBColorTrait} method.
     * @throws SVGException with error code SVG_INVALID_VALUE_ERR: if any of the
     * parameters is not in the 0..255 range.</li>
     * @param red the red rgb component.
     * @param green the green rgb component.
     * @param blue the blue rgb component.
     */
    public SVGRGBColor createSVGRGBColor(final int red, 
                                         final int green, 
                                         final int blue)
        throws SVGException {
        if (red < 0 
            || red > 255 
            || green < 0 
            || green > 255 
            || blue < 0 
            || blue > 255) {
            throw new SVGException(SVGException.SVG_INVALID_VALUE_ERR, null);
        }

        return new RGB(red, green, blue);
    }


    /**
     * Sets the x component of the point to the specified float value.
     *
     * @param value the x component value
     *
     */
    public void setX(final float value) {
        tx = value;
        applyUserTransform();
    }

    /**
     * Sets the y component of the point to the specified float value.
     *
     * @param value the y component value
     *
     */

    public void setY(final float value) {
        ty = value;
        applyUserTransform();
    }


    /**
     * Returns the x component of the point.
     *
     * @return the x component of the point.
     *
     */
    public float getX() {
        return tx;
    }

    /**
     * Returns the y component of the point.
     *
     * @return the y component of the point.
     *
     */
    public float getY() {
        return ty;
    }

    /**
     * Uses currentScale, currentRotate, tx and ty to compute the 
     * owner document's transform.
     */
    void applyUserTransform() {
        if (ownerDocument.zoomAndPan == Viewport.ZOOM_PAN_MAGNIFY) {
            Transform txf = new Transform(currentScale, 0,
                                          0, currentScale,
                                          tx, ty);
            txf.mRotate(currentRotate);

            ownerDocument.setTransform(txf);

        } else {
            if (ownerDocument.getTransform() != null) {
                // ownerDocument's transform has been touched
                // so, set it to identity
                ownerDocument.setTransform(new Transform(null));
            }
        }
    }

}
