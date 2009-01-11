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

import com.sun.perseus.j2d.Box;
import com.sun.perseus.j2d.GraphicsProperties;
import com.sun.perseus.j2d.PaintTarget;
import com.sun.perseus.j2d.RasterImage;
import com.sun.perseus.j2d.RenderGraphics;
import com.sun.perseus.j2d.Tile;
import com.sun.perseus.j2d.Transform;

import com.sun.perseus.util.SVGConstants;
import com.sun.perseus.util.RunnableQueue;
import com.sun.perseus.util.RunnableQueue.RunnableHandler;

import org.w3c.dom.DOMException;


/**
 * This class models the SVG Tiny <code>&lt;image&gt;</code>.
 *
 * A value of 0 on the width and height attributes disables the 
 * rendering of the Image object. Negative width or height values
 * on the <code>ImageNode</code> are illegal.
 *
 * If the referenced <code>Image</code> is null, this node will draw
 * a gray box using the current fill value
 *
 * @version $Id: ImageNode.java,v 1.17 2006/06/29 10:47:32 ln156897 Exp $
 */
public class ImageNode extends AbstractRenderingNode 
        implements RasterImageConsumer {
    /**
     * width and height are required on <image>
     */
    static final String[] REQUIRED_TRAITS
        = {SVGConstants.SVG_WIDTH_ATTRIBUTE,
           SVGConstants.SVG_HEIGHT_ATTRIBUTE};

    /**
     * xlink:href is required on <image>
     */
    static final String[][] REQUIRED_TRAITS_NS
        = { {SVGConstants.XLINK_NAMESPACE_URI, 
             SVGConstants.SVG_HREF_ATTRIBUTE} };

    /**
     * The viewport defined by the image element
     */
    protected float x, y, width, height;

    /**
     * The painted Image. The image is built from the 
     * href attribute.
     */
    protected RasterImage image;

    /**
     * Controls whether the image has been loaded or
     * not.
     */
    protected boolean imageLoaded;

    /**
     * The reference to the image data. This can be using 
     * base64 PNG or JPEG encoding or an absolute or relative
     * URI.
     */
    protected String href;

    /**
     * The absolute URI is computed from the href and the
     * node's base URI.
     */
    protected String absoluteURI;

    /**
     * Controls whether the image is aligned in the viewport
     * or if it is just scaled to the viewport.
     */
    protected int align = StructureNode.ALIGN_XMIDYMID;

    /**
     * Constant used to identify broken URI values
     */
    protected static final String BROKEN_URI = "broken uri";

    // =========================================================================
    // Image specific
    // =========================================================================

    /**
     * Constructor.
     *
     * @param ownerDocument this element's owner <code>DocumentNode</code>
     */
    public ImageNode(final DocumentNode ownerDocument) {
        super(ownerDocument);
        image = ownerDocument.getImageLoader().getLoadingImage();
        
        // Initially, the image's width and height are zero, so we
        // set the corresponding bits accordingly.
        canRenderState |= CAN_RENDER_ZERO_WIDTH_BIT;
        canRenderState |= CAN_RENDER_ZERO_HEIGHT_BIT;
    }

    /**
     * @return the SVGConstants.SVG_IMAGE_TAG value
     */
    public String getLocalName() {
        return SVGConstants.SVG_IMAGE_TAG;
    }


    /**
     * Used by <code>DocumentNode</code> to create a new instance from
     * a prototype <code>ImageNode</code>.
     *
     * @param doc the <code>DocumentNode</code> for which a new node is
     *        should be created.
     * @return a new <code>ImageNode</code> for the requested document.
     */
    public ElementNode newInstance(final DocumentNode doc) {
        return new ImageNode(doc);
    }

    /**
     * Computes the rendering tile for the given set of GraphicsProperties.
     *
     * @param tile the Tile instance whose bounds should be set.
     * @param t the Transform to the requested tile space, from this node's user
     * space.
     * @param gp the <code>GraphicsProperties</code> for which the tile
     *        should be computed.
     * @return the screen bounding box when this node is rendered with the 
     * given render context.
     */
    protected void computeRenderingTile(final Tile tile, 
                                        final Transform t,
                                        final GraphicsProperties gp) {
        tile.snapBox(addNodeBBox(null, t));
    }

    /**
     * @param newX the new position of this image's upper left
     *        corner on the x-axis
     */
    public void setX(final float newX) {
        if (newX == x) {
            return;
        }
        modifyingNode();
        renderingDirty();
        this.x = newX;
        modifiedNode();
    }

    /**
     * @param newY the new postion of this image's upper left 
     *        corner on the y-axis
     */
    public void setY(final float newY) {
        if (newY == y) {
            return;
        }
        modifyingNode();
        renderingDirty();
        this.y = newY;
        modifiedNode();
    }

    /**
     * @return this image's upper left corner position on the x-axis
     */
    public float getX() {
        return x;
    }

    /**
     * @return this image's upper left corner position on the y-axis
     */
    public float getY() {
        return y;
    }

    /**
     * @param newWidth this node's new width. Should be strictly positive
     */
    public void setWidth(final float newWidth) {
        if (newWidth < 0) {
            throw new IllegalArgumentException();
        }

        if (newWidth == width) {
            return;
        }

        modifyingNode();
        renderingDirty();
        this.width = newWidth;
        computeCanRenderWidthBit(width);
        modifiedNode();
    }

    /**
     * @return this node's width
     */
    public float getWidth() {
        return width;
    }

    /**
     * @param newHeight the new height for this image's node
     *        Should be strictly positive.
     */
    public void setHeight(final float newHeight) {
        if (newHeight < 0) {
            throw new IllegalArgumentException();
        }

        if (newHeight == height) {
            return;
        }

        modifyingNode();
        renderingDirty();
        this.height = newHeight;
        computeCanRenderHeightBit(height);
        modifiedNode();
    }

    /**
     * @return this image's height
     */
    public float getHeight() {
        return height;
    }

    /**
     * Sets this node's parent but does not generate change 
     * events.
     * 
     * @param newParent the node's new parent node.
     */
    protected void setParentQuiet(final ModelNode newParent) {
        super.setParentQuiet(newParent);

        // Reset the image if this node has a different parent that
        // impacts the computation of the absoluteURI
        if (href != null && !href.equals(absoluteURI)) {
            willNeedImage();
        }
    }

    /**
     * @param newHref the new reference for this node's image.
     * @throws IllegalArgumentException if the href cannot be resolved
     *         into an absoluteURI and paintNeedsLoad is set to true.
     */
    public void setHref(final String newHref) {
        if (newHref == null) {
            throw new IllegalArgumentException();
        }

        if (equal(newHref, href)) {
            return;
        }

        modifyingNode();
        renderingDirty();
        this.href = newHref;
        this.image = ownerDocument.getImageLoader().getLoadingImage();
        ownerDocument.getImageLoader().removeRasterImageConsumer(absoluteURI, 
                                                                 this);
        this.absoluteURI = null;
        this.imageLoaded = false;

        // Only declare the new URI to load if this node is
        // part of the document tree and if its href is set 
        // to a non-null value.
        if (isInDocumentTree() && href != null) {
            willNeedImage();
        }
        modifiedNode();
    }

    /**
     * Implementation. Declares to the ImageLoader that an image will be 
     * needed.
     */
    void willNeedImage() {
        imageLoaded = false;
        image = ownerDocument.getImageLoader().getLoadingImage();

        absoluteURI
            = ownerDocument.getImageLoader().resolveURI(href, getURIBase());
        
        if (absoluteURI == null) {
            if (paintNeedsLoad) {
                throw new IllegalArgumentException();
            } else {
                absoluteURI = BROKEN_URI;
                imageLoaded = true;
                image = ownerDocument.getImageLoader().getBrokenImage();
                return;
            }
        } else {
            ownerDocument.getImageLoader().addRasterImageConsumer(absoluteURI, 
                                                                  this);
            ownerDocument.getImageLoader().needsURI(absoluteURI);
        }
    }

    /**
     * @return this node's image reference. This should be a URI
     */
    public String getHref() {
        return href;
    }

    /**
     * @return this node's <tt>Image</tt> resource. May be null
     *         in case no uri has been set or if loading the 
     *         image data failed.
     */
    public RasterImage getImage() {
        return image;
    }

    /**
     * Sets the node's image if the computed absolute URI
     * is equal to the input uri.
     *
     * @param image the new node image. Should not be null.
     * @param uri the uri <code>image</code> corresponds to. This is
     *        passed in case the node's uri changed between a call
     *        to the <code>ImageLoader</code>'s <code>needsURI</code>
     *        and a call to <code>setImage</code>.
     */
    public void setImage(final RasterImage image, final String uri) {
        // This is done to handle situations where the href/parent
        // or other change occur while the image is loading.
        if (absoluteURI != null && absoluteURI.equals(uri)) {
            if (this.image != image) {
                modifyingNode();
                this.image = image;
                modifiedNode();
            }
        }
    }

    /**
     * @return the associated RunnableQueue. If not null, setImage should
     *         be called from within the runnable queue.
     */
    public RunnableQueue getUpdateQueue() {
        return ownerDocument.getUpdateQueue();
    }

    /**
     * @return the associated RunnableHandler, in case the associated 
     *         RunnableQueue is not null.
     */
    public RunnableHandler getRunnableHandler() {
        return ownerDocument.getRunnableHandler();
    }

    /**
     * @param newAlign one of StructureNode.ALIGN_NONE or 
     *        StructureNode.ALIGN_XMIDYMID
     */
    public void setAlign(final int newAlign) {
        if (newAlign == align) {
            return;
        }
        modifyingNode();
        this.align = newAlign;
        modifiedNode();
    }

    /**
     * @return the image's align property
     * @see StructureNode#ALIGN_NONE
     * @see StructureNode#ALIGN_XMIDYMID
     */
    public int getAlign() {
        return align;
    }


    /**
     * Paints this node into the input RenderGraphics. 
     *
     * @param rg this node is painted into this <tt>RenderGraphics</tt>
     * @param gp the <code>GraphicsProperties</code> controlling the operation's
     *        rendering
     * @param pt the <code>PaintTarget</code> for the paint operation.
     * @param txf the <code>Transform</code> from user space to device space for
     *        the paint operation.
     */
    void paintRendered(final RenderGraphics rg,
                       final GraphicsProperties gp,
                       final PaintTarget pt,
                       final Transform tx) {
        if (!gp.getVisibility()) {
            return;
        }

        rg.setTransform(tx);
	rg.setOpacity(getOpacity());

        if (!imageLoaded) {
            loadImage();
        }

        //
        // Scale the image so that it fits into width/height
        // and is centered
        //
        int iw = image.getWidth();
        int ih = image.getHeight();

        if (align == StructureNode.ALIGN_NONE) {
            rg.drawImage(image, x, y, width, height);
        } else {
            float ws = width / iw;
            float hs = height / ih;
            float is = ws;
            if (hs < ws) {
                is = hs;
            }

            float oh = ih * is;
            float ow = iw * is;
            float dx = (width - ow) / 2;
            float dy = (height - oh) / 2;

            rg.drawImage(image, (x + dx), (y + dy), ow, oh);
        }
    }
    
    /**
     * Implementation. Loads the image through the ownerDocument's 
     * ImageLoader. If this image's paintNeedsLoad is true, this
     * will block until the image data has been retrieved. Otherwise,
     * a request to asynchronously load the image is placed.
     */
    public void loadImage() {
        // Note that if absoluteURI is null, it is because either href is null
        // or this node is not hooked into the tree. In these cases, we stick
        // to the loadingImage
        if (absoluteURI != null) {
            if (paintNeedsLoad) {
                image = ownerDocument.getImageLoader()
                    .getImageAndWait(absoluteURI);
                if (image == ownerDocument.getImageLoader().getBrokenImage()) {
                    throw new IllegalStateException();
                }
            } else {
                ownerDocument.getImageLoader().getImageLater(absoluteURI, this);
            }
        }
        imageLoaded = true;
    }

    /**
     * An <code>ImageNode</code> has something to render 
     *
     * @return true
     */
    public boolean hasNodeRendering() {
        return true;
    }

    /**
     * Returns true if this node is hit by the input point. The input point
     * is in viewport space. 
     *  
     * @param pt the x/y coordinate. Should never be null and be
     *        of size two. If not, the behavior is unspecified.
     *        The x/y coordinate is in viewport space.
     *
     * @return true if the node is hit by the input point. 
     * @see #nodeHitAt
     */
    protected boolean isHitVP(float[] pt) {
        if (!getVisibility()) {
                return false;
        }
        getInverseTransformState().transformPoint(pt, ownerDocument.upt);
        pt = ownerDocument.upt;
        return (pt[0] > x && pt[0] < (x + width)
                && pt[1] > y && pt[1] < (y + height));
    }

    /**
     * Returns true if the proxy node is hit by the input point. The input point
     * is in viewport space.
     *  
     * @param pt the x/y coordinate. Should never be null and be
     *        of size two. If not, the behavior is unspecified.
     *        The x/y coordinate is in viewport space.
     * @param proxy the tested <code>ElementNodeProxy</code>.
     * @return true if the node is hit by the input point. 
     * @see #nodeHitAt
     */
    protected boolean isProxyHitVP(float[] pt, 
                                   AbstractRenderingNodeProxy proxy) {
        proxy.getInverseTransformState().transformPoint(pt, ownerDocument.upt);
        pt = ownerDocument.upt;
        return (pt[0] > x && pt[0] < (x + width)
                && pt[1] > y && pt[1] < (y + height));
    }

    /**
     * Supported traits: x, y, width, height
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
     * @return an array of traits that are required by this element.
     */
    public String[] getRequiredTraits() {
        return REQUIRED_TRAITS;
    }

    /**
     * @return an array of namespaceURI, localName trait pairs required by
     *         this element.
     */
    public String[][] getRequiredTraitsNS() {
        return REQUIRED_TRAITS_NS;
    }

    /**
     * Supported traits: xlink:href
     *
     * @param namespaceURI the trait's namespace.
     * @param traitName the name of the trait which the element may support.
     * @return true if this element supports the given trait in one of the
     *         trait accessor methods.
     */
    boolean supportsTraitNS(final String namespaceURI,
                            final String traitName) {
        if (SVGConstants.XLINK_NAMESPACE_URI == namespaceURI
            &&
            SVGConstants.SVG_HREF_ATTRIBUTE == traitName) {
            return true;
        } else {
            return super.supportsTraitNS(namespaceURI, traitName);
        }
    }

    /**
     * ImageNode handles x, y, width and height traits.
     * Other traits are handled by the super class.
     *
     * @param name the requested trait's name.
     * @return the requested trait's value.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to a String (SVG Tiny only).
     */
    public String getTraitImpl(final String name)
        throws DOMException {
        if (SVGConstants.SVG_X_ATTRIBUTE == name) {
            return Float.toString(getX());
        } else if (SVGConstants.SVG_Y_ATTRIBUTE == name) {
            return Float.toString(getY());
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
     * ImageNode handles the xlink href attribute
     *
     * @param namespaceURI the requested trait's namespace URI.
     * @param name the requested trait's local name (i.e., un-prefixed, as 
     *        "href")
     * @return the requested trait's string value.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     *         trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     *         trait's computed value cannot be converted to a String (SVG Tiny
     *         only).
     * @throws SecurityException if the application does not have the necessary
     *         privilege rights to access this (SVG) content.
     */
    String getTraitNSImpl(String namespaceURI, String name)
        throws DOMException {
        if (SVGConstants.XLINK_NAMESPACE_URI == namespaceURI
            &&
            SVGConstants.SVG_HREF_ATTRIBUTE == name) {
            if (href == null) {
                return "";
            } 
            return href;
        } else {
            return super.getTraitNSImpl(namespaceURI, name);
        }
    }

    /**
     * ImageNode handles x, y, width and height traits.
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
        if (SVGConstants.SVG_X_ATTRIBUTE == name) {
            return getX();
        } else if (SVGConstants.SVG_Y_ATTRIBUTE == name) {
            return getY();
        } else if (SVGConstants.SVG_WIDTH_ATTRIBUTE == name) {
            return getWidth();
        } else if (SVGConstants.SVG_HEIGHT_ATTRIBUTE == name) {
            return getHeight();
        } else {
            return super.getFloatTraitImpl(name);
        }
    }

    /**
     * @param traitName the trait name.
     */
    TraitAnim createTraitAnimImpl(final String traitName) {
        if (SVGConstants.SVG_X_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_Y_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_WIDTH_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_HEIGHT_ATTRIBUTE == traitName) {
            return new FloatTraitAnim(this, traitName, TRAIT_TYPE_FLOAT);
        } else if (SVGConstants.SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE 
                == traitName) {
            return new StringTraitAnim(this, NULL_NS, traitName);
        } else {
            return super.createTraitAnimImpl(traitName);
        }
    }

    /**
     * @param traitName the trait name.
     * @param traitNamespace the trait's namespace. Should not be null.
     */
    TraitAnim createTraitAnimNSImpl(final String traitNamespace, 
                                    final String traitName) {
        if (traitNamespace == SVGConstants.XLINK_NAMESPACE_URI
            &&
            traitName == SVGConstants.SVG_HREF_ATTRIBUTE) {
            return new StringTraitAnim(this, traitNamespace, traitName);
        }

        return super.createTraitAnimNSImpl(traitNamespace, traitName);
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
            return new float[][]{{parseFloatTrait(traitName, value)}};
        } else if (SVGConstants.SVG_WIDTH_ATTRIBUTE == traitName
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

        if (SVGConstants.SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE
                .equals(traitName)) {
            if (!SVGConstants
                .SVG_SVG_PRESERVE_ASPECT_RATIO_DEFAULT_VALUE.equals(value)
                &&
                !SVGConstants.SVG_NONE_VALUE.equals(value)) {
                throw illegalTraitValue(traitName, value);
            }
            return value;
        } else {
            return super.validateTraitNS(namespaceURI,
                                         traitName,
                                         value,
                                         reqNamespaceURI,
                                         reqLocalName,
                                         reqTraitNamespace,
                                         reqTraitName);
        }
    }

    /**
     * ImageNode handles x, y, rx, ry, width and height traits.
     * Other traits are handled by the super class.
     *
     * @param name the trait's name.
     * @param value the new trait string value.
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
        } else if (SVGConstants.SVG_WIDTH_ATTRIBUTE == name) {
            setWidth(parsePositiveFloatTrait(name, value));
        } else if (SVGConstants.SVG_HEIGHT_ATTRIBUTE == name) {
            setHeight(parsePositiveFloatTrait(name, value));
        } else if (SVGConstants
                   .SVG_PRESERVE_ASPECT_RATIO_ATTRIBUTE.equals(name)) {
            if (SVGConstants
                .SVG_SVG_PRESERVE_ASPECT_RATIO_DEFAULT_VALUE.equals(value)) {
                setAlign(StructureNode.ALIGN_XMIDYMID);
            } else if (SVGConstants.SVG_NONE_VALUE.equals(value)) {
                setAlign(StructureNode.ALIGN_NONE);
            } else {
                throw illegalTraitValue(name, value);
            }
        } else {
            super.setTraitImpl(name, value);
        }
    }

    /**
     * ImageNode supports the xlink:href trait.
     *
     * @param namespaceURI the trait's namespace.
     * @param name the trait's local name (un-prefixed, e.g., "href");
     * @param value the new trait value (e.g., "http://www.sun.com/mypng.png")
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if the requested
     * trait's value cannot be specified as a String
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is an invalid value for the given trait or null.
     * @throws DOMException with error code NO_MODIFICATION_ALLOWED_ERR: if
     * attempt is made to change readonly trait.
     * @throws SecurityException if the application does not have the necessary
     * privilege rights to access this (SVG) content.
     */
    public void setTraitNSImpl(final String namespaceURI, 
                               final String name, 
                               final String value)
        throws DOMException {
        try {
            if (SVGConstants.XLINK_NAMESPACE_URI == namespaceURI
                &&
                SVGConstants.SVG_HREF_ATTRIBUTE == name) {
                setHref(value);
            } else {
                super.setTraitNSImpl(namespaceURI, name, value);
            }
        } catch (IllegalArgumentException iae) {
            throw illegalTraitValue(name, value);
        }
    }

    /**
     * ImageNode handles x, y, rx, ry, width and height traits.
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
            if (SVGConstants.SVG_X_ATTRIBUTE == name) {
                setX(value);
            } else if (SVGConstants.SVG_Y_ATTRIBUTE == name) {
                setY(value);
            } else if (SVGConstants.SVG_WIDTH_ATTRIBUTE == name) {
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
        if (SVGConstants.SVG_X_ATTRIBUTE == name
            ||
            SVGConstants.SVG_Y_ATTRIBUTE == name
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
     * @param bbox the bounding box to which this node's bounding box should be
     *        appended. That bounding box is in the target coordinate space. It 
     *        may be null, in which case this node should create a new one.
     * @param t the transform from the node coordinate system to the coordinate
     *        system into which the bounds should be computed.
     * @return the bounding box of this node, in the target coordinate space, 
     */
    Box addNodeBBox(Box bbox, 
                    final Transform t) {
        return addTransformedBBox(bbox, x, y, width, height, t);
    }
}
