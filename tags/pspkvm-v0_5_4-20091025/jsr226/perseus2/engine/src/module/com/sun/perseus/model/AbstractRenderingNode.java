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
import com.sun.perseus.j2d.PathSupport;
import com.sun.perseus.j2d.PaintTarget;
import com.sun.perseus.j2d.RenderGraphics;
import com.sun.perseus.j2d.Tile;

import com.sun.perseus.util.SVGConstants;

import org.w3c.dom.DOMException;

import org.w3c.dom.svg.SVGMatrix;
import org.w3c.dom.svg.SVGRect;

import com.sun.perseus.j2d.Transform;

/**
 * Typical base class for nodes which render something (shapes and images).
 *
 * @version $Id: AbstractRenderingNode.java,v 1.13 2006/06/29 10:47:28 ln156897 Exp $
 */
public abstract class AbstractRenderingNode 
    extends CompositeGraphicsNode implements Transformable {
    /**
     * The Transform applied to this node. 
     */
    protected Transform transform;

    /**
     * The motion transform applied to this node. This is typically used for 
     * animateMotion, but it can be used as a regular trait as well.
     */
    protected Transform motion;

    /**
     * Used to track the node's rendering area and the rendered areas.
     */
    protected RenderingManager renderingManager;

    /**
     * Constructor.
     *
     * @param ownerDocument this element's owner <code>DocumentNode</code>
     */
    public AbstractRenderingNode(final DocumentNode ownerDocument) {
        super(ownerDocument);

        if (DirtyAreaManager.ON) {
            renderingManager = new RenderingManager(this);
        }
    }

    /**
     * Clears the text layouts, if any exist. This is typically
     * called when the font selection has changed and nodes such
     * as <code>Text</code> should recompute their layouts.
     * This should recursively call clearLayouts on children 
     * node or expanded content, if any.
     */
    protected void clearLayouts() {
    }

    /**
     * Computes this node's rendering tile.
     *
     * @param tile the Tile instance whose bounds should be set.
     * @return the device space rendering tile.
     */
    protected final void computeRenderingTile(final Tile tile) {
        computeRenderingTile(tile, txf, this);
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
    abstract void computeRenderingTile(final Tile tile, 
                                       final Transform t,
                                       final GraphicsProperties gp);

    /**
     * @return an adequate <code>ElementNodeProxy</code> for this node.
     */
    ElementNodeProxy buildProxy() {
        return new AbstractRenderingNodeProxy(this);
    }

    /**
     * Returns the <code>ModelNode</code>, if any, hit by the
     * point at coordinate x/y. 
     * 
     * @param pt the x/y coordinate. Should never be null and be
     *        of size two. If not, the behavior is unspecified.
     *        The coordinates are in viewport space.
     * @return the <tt>ModelNode</tt> hit at the given point or null
     *         if none was hit.
     */
    public ModelNode nodeHitAt(final float[] pt) {
        // If a node does not render, it is never hit
        if ((canRenderState != 0) || !isHitVP(pt)) {
            return null;
        }

        return this;
    }

    /**
     * Returns true if this node is hit by the input point. The input point
     * is in viewport space. 
     *
     * @return true if the node is hit by the input point. 
     * @see #nodeHitAt
     */
    abstract boolean isHitVP(float[] pt);

    /**
     * Returns true if this proxy node is hit by the input point. The input 
     * point is in viewport space. 
     *
     * @param pt the x/y coordinate. Should never be null and be
     *        of size two. If not, the behavior is unspecified.
     *        The x/y coordinate is in viewport space.
     * @param proxy the tested ElementNodeProxy.
     * @return true if the node is hit by the input point. 
     * @see #isHitVP
     */
    abstract boolean isProxyHitVP(float[] pt, 
                                   final AbstractRenderingNodeProxy proxy);

    /**
     * Returns the <code>ModelNode</code>, if any, hit by the
     * point at coordinate x/y in the proxy tree starting at 
     * proxy.
     * 
     * @param pt the x/y coordinate. Should never be null and be
     *        of size two. If not, the behavior is unspecified.
     *        The coordinates are in viewport space.
     * @param proxy the root of the proxy tree to test.
     * @return the <tt>ModelNode</tt> hit at the given point or null
     *         if none was hit.
     */
    ModelNode proxyNodeHitAt(final float[] pt,
                             final ElementNodeProxy proxy) {
        // If a node does not render, it is never hit
        if ((canRenderState != 0) || 
                !isProxyHitVP(pt, (AbstractRenderingNodeProxy) proxy)) {
            return null;
        }

        return proxy;
    }

    /**
     * @param newDisplay the new computed display value
     */
    void setComputedDisplay(final boolean newDisplay) {
        super.setComputedDisplay(newDisplay);

        renderingDirty();
    }

    /**
     * @param newVisibility the new computed visibility property.
     */
    void setComputedVisibility(final boolean newVisibility) {
        super.setComputedVisibility(newVisibility);

        renderingDirty();
    }

    /**
     * Paints this node into the input <code>RenderGraphics</code>.
     *
     * @param rg the <tt>RenderGraphics</tt> where the node should paint itself
     */
    public void paint(final RenderGraphics rg) {
        if ((canRenderState != 0)) {
            return;
        }

        if (DirtyAreaManager.ON) {
            Tile primitiveTile = getRenderingTile();
            if (primitiveTile == null 
                || 
                rg.getRenderingTile().isHit(primitiveTile)) {
                // rg.setPrimitiveTile(primitiveTile);
                paintRendered(rg, this, this, txf);

                // nodeRendered is called seperately from paintRendered
                // because paintRendered is used in different contexts,
                // for example by proxy nodes to render, using their
                // proxied node's paintRendered method.
                nodeRendered();
            }
        } else {
            paintRendered(rg, this, this, txf);
        }
    }

    /**
     * @param bbox the bounding box to which this node's bounding box should be
     *        appended. That bounding box is in the target coordinate space. It 
     *        may be null, in which case this node should create a new one.
     * @param t the transform to apply from the node's coordinate space to the
     *        target coordinate space. May be null for the identity 
     *        transform.
     * @return the node's bounding box in the target coordinate space.
     */
    Box addBBox(Box bbox, final Transform t) {
        return addNodeBBox(bbox, t);
    }

    /**
     * @return the tight bounding box in current user coordinate
     * space. 
     */
    public SVGRect getBBox() {
        return addNodeBBox(null, null);
    }

    /**
     * @return the tight bounding box in screen coordinate space.
     */
    public SVGRect getScreenBBox() {
        // There is no screen bounding box if the element is not hooked
        // into the main tree.
        if (!inDocumentTree()) {
            return null;
        }

        return addNodeBBox(null, txf);
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
    abstract void paintRendered(final RenderGraphics rg,
                                final GraphicsProperties gp,
                                final PaintTarget pt,
                                final Transform tx);

    /**
     * Should be called whenever this node's rendering becomes dirty.
     */
    final void renderingDirty() {
        if (DirtyAreaManager.ON) {
            renderingManager.dirty();
        }
    }

    /**
     * @return the number of properties on this node.
     */
    public int getNumberOfProperties() {
        return NUMBER_OF_PROPERTIES;
    }

    /**
     * Called when the computed value of the given property has changed.
     * On a rendering node, as we do not render regular children nor expanded
     * content, we do not propagate property state changes.
     *
     * @param propertyIndex index for the property whose value has changed.
     * @param parentPropertyValue the value that children of this node should 
     *        now inherit.
     */
    protected void propagatePropertyState(final int propertyIndex,
                                          final Object parentPropertyValue) {
        // Propagate to proxies.
        if (firstProxy != null) {
            ElementNodeProxy proxy = firstProxy;
            while (proxy != null) {
                ((CompositeGraphicsNodeProxy) proxy).proxiedPropertyStateChange(
                        propertyIndex, 
                        parentPropertyValue);
                proxy = proxy.nextProxy;
            }
        }
    }

    /**
     * Called when the computed value of the given float property has changed.
     * On a rendering node, as we do not render regular children nor expanded
     * content, we do not propagate property state changes.
     *
     * @param propertyIndex index for the property whose value has changed.
     * @param parentPropertyValue the value that children of this node should 
     *        now inherit.
     */
    protected void propagateFloatPropertyState(
            final int propertyIndex, 
            final float parentPropertyValue) {
        // Propagate to proxies.
        if (firstProxy != null) {
            ElementNodeProxy proxy = firstProxy;
            while (proxy != null) {
                ((CompositeGraphicsNodeProxy) proxy)
                    .proxiedFloatPropertyStateChange(propertyIndex, 
                                                     parentPropertyValue);
                proxy = proxy.nextProxy;
            }
        }
    }

    /**
     * Called when the computed value of the given packed property has changed.
     * On a rendering node, as we do not render regular children nor expanded
     * content, we do not propagate property state changes.
     *
     * @param propertyIndex index for the property whose value has changed.
     * @param parentPropertyValue the value that children of this node should 
     *        now inherit.
     */
    protected void propagatePackedPropertyState(final int propertyIndex,
                                                final int parentPropertyValue) {
        // Propagate to proxies.
        if (firstProxy != null) {
            ElementNodeProxy proxy = firstProxy;
            while (proxy != null) {
                ((CompositeGraphicsNodeProxy) proxy)
                    .proxiedPackedPropertyStateChange(propertyIndex, 
                                                      parentPropertyValue);
                proxy = proxy.nextProxy;
            }
        }
    }

    /**
     * Recomputes the transform cache, if one exists. This should recursively
     * call recomputeTransformState on children node or expanded content, if
     * any child is rendered down below.
     *
     * @param parentTransform the Transform applied to this node's parent.
     */
    protected void recomputeTransformState(final Transform parentTransform) {
        txf = appendTransform(parentTransform, txf);
        inverseTxf = null;
        computeCanRenderTransformBit(txf);
        renderingDirty();
    }

    /**
     * @param newTransform The new <code>Transformable</code>'s transform.
     */
    public void setTransform(final Transform newTransform) {
        if (equal(transform, newTransform)) {
            return;
        }
        modifyingNode();
        this.transform = newTransform;
        recomputeTransformState();
        recomputeProxyTransformState();
        modifiedNode();
    }

    /**
     * @param newMotion The new motion transform.
     */
    public void setMotion(final Transform newMotion) {
        if (equal(newMotion, motion)) {
            return;
        }

        modifyingNode();
        this.motion = newMotion;
        recomputeTransformState();
        recomputeProxyTransformState();
        modifiedNode();
    }

    /**
     * @return This <code>Transformable</code>'s transform.
     */
    public Transform getTransform() {
        return transform;
    }

    /**
     * @return This node's motion transform.
     */
    public Transform getMotion() {
        return motion;
    }

    /**
     * @return the bounding box, in screen coordinate, which encompasses the
     * node's rendering.
     */
    protected Tile getRenderingTile() {
        return renderingManager.getRenderingTile();
    }

    /**
     * @return the tile which encompasses the node's last actual rendering. If
     * this node's hasRendering method returns false, then this method should
     * return null. By default, this method returns null because
     * hasNodeRendering returns null by default.
     */
    protected Tile getLastRenderedTile() {
        return renderingManager.getLastRenderedTile();
    }

    /**
     * After calling this method, getLastRenderedTile should always return null.
     */
    protected void clearLastRenderedTile() {
        renderingManager.clearLastRenderedTile();
    }

    /**
     * To be overriddent by derived classes, such as TimedElementNode,
     * if they need to do special operations when hooked into the 
     * document tree.
     */
    void nodeHookedInDocumentTree() {
        super.nodeHookedInDocumentTree();
        renderingDirty();
    }

    /**
     * To be overriddent by derived classes, such as TimedElementNode,
     * if they need to do special operations when unhooked from the 
     * document tree.
     */
    void nodeUnhookedFromDocumentTree() {
        super.nodeUnhookedFromDocumentTree();
        renderingDirty();
    }

    /**
     * Appends this node's transform, if it is not null.
     *
     * @param tx the <code>Transform</code> to apply additional node 
     *        transforms to. This may be null.
     * @param workTx a <code>Transform</code> which can be re-used if a 
     *        new <code>Transform</code> needs to be created and workTx
     *        is not the same instance as tx.
     * @return a transform with this node's transform added.
     */
    protected Transform appendTransform(Transform tx,
                                        Transform workTx) {
        if (transform == null && motion == null) {
            return tx;
        } 

        tx = recycleTransform(tx, workTx);
        
        if (motion != null) {
            tx.mMultiply(motion);
        }

        if (transform != null) {
            tx.mMultiply(transform);
        }

        return tx;
    }

    /**
     * An <code>AbstractRenderingNode</code> has something to render 
     *
     * @return true
     */
    public boolean hasNodeRendering() {
        return true;
    }

    /**
     * Simply notifies the RenderingManager.
     */
    protected void nodeRendered() {
        if (DirtyAreaManager.ON) {
            renderingManager.rendered();
        }
    }

    /**
     * AbstractShapeNode handles the transform attribute.
     *
     * @param traitName the name of the trait which the element may support.
     * @return true if this element supports the given trait in one of the
     *         trait accessor methods.
     */
    boolean supportsTrait(final String traitName) {
        if (SVGConstants.SVG_TRANSFORM_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_MOTION_PSEUDO_ATTRIBUTE == traitName) {
            return true;
        } else {
            return super.supportsTrait(traitName);
        }
    }

    /**
     * AbstractShapeNode handles the transform attribute.
     * Other attributes are handled by the super class.
     *
     * @param name the name of the requested trait.
     * @return the value of the requested trait, as a string.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to a String (SVG Tiny only).
     */
    public String getTraitImpl(final String name)
        throws DOMException {
        if (SVGConstants.SVG_TRANSFORM_ATTRIBUTE == name) {
            return toStringTrait(transform);
        } else if (SVGConstants.SVG_MOTION_PSEUDO_ATTRIBUTE == name) {
            return toStringTrait(motion);
        } else {
            return super.getTraitImpl(name);
        }
    }

    /**
     * AbstractShapeNode handles the transform attribute.
     * Other attributes are handled by the super class.
     *
     * @param name matrix trait name.
     * @return the trait value corresponding to name as SVGMatrix.
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to {@link
     * org.w3c.dom.svg.SVGMatrix SVGMatrix}
     */
    SVGMatrix getMatrixTraitImpl(String name)throws DOMException {
        if (SVGConstants.SVG_TRANSFORM_ATTRIBUTE.equals(name)) {
            return toSVGMatrixTrait(transform);
        } else if (SVGConstants.SVG_MOTION_PSEUDO_ATTRIBUTE.equals(name)) {
            return toSVGMatrixTrait(motion);
        } else {
            return super.getMatrixTraitImpl(name);
        }
    }

    /**
     * @param traitName the trait name.
     */
    TraitAnim createTraitAnimImpl(final String traitName) {
        if (SVGConstants.SVG_TRANSFORM_ATTRIBUTE == traitName) {
            return new TransformTraitAnim(this, traitName);
        } else if (SVGConstants.SVG_MOTION_PSEUDO_ATTRIBUTE == traitName) {
            return new MotionTraitAnim(this, traitName);
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
        if (SVGConstants.SVG_TRANSFORM_ATTRIBUTE == name) {
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
            recomputeTransformState();
            recomputeProxyTransformState();
            modifiedNode();
        } else if (SVGConstants.SVG_MOTION_PSEUDO_ATTRIBUTE == name) {
            if (motion == null) {
                modifyingNode();
                motion = new Transform(value[0][0],
                                       value[1][0],
                                       value[2][0],
                                       value[3][0],
                                       value[4][0],
                                       value[5][0]);
            } else {
                if (!motion.equals(value)) {
                    modifyingNode();
                    motion.setTransform(value[0][0],
                                        value[1][0],
                                        value[2][0],
                                        value[3][0],
                                        value[4][0],
                                        value[5][0]);
                } else {
                    return;
                }
            }
            recomputeTransformState();
            recomputeProxyTransformState();
            modifiedNode();
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
    public float[][] validateFloatArrayTrait(final String traitName,
                                             final String value,
                                             final String reqNamespaceURI,
                                             final String reqLocalName,
                                             final String reqTraitNamespace,
                                             final String reqTraitName) 
                                                throws DOMException {
        if (SVGConstants.SVG_TRANSFORM_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_MOTION_PSEUDO_ATTRIBUTE == traitName) {
            Transform txf = parseTransformTrait(traitName, value);
            return new float[][] {{(float) txf.getComponent(0)},
                                  {(float) txf.getComponent(1)},
                                  {(float) txf.getComponent(2)},
                                  {(float) txf.getComponent(3)},
                                  {(float) txf.getComponent(4)},
                                  {(float) txf.getComponent(5)}};
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
     * AbstractShapeNode handles the transform attribute.
     * Other attributes are handled by the super class.
     *
     * @param name the name of the trait to set
     * @param value the string value for the trait to set.
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
        if (SVGConstants.SVG_TRANSFORM_ATTRIBUTE == name) {
            setTransform(parseTransformTrait(name, value));
        } else if (SVGConstants.SVG_MOTION_PSEUDO_ATTRIBUTE == name) {
            setMotion(parseTransformTrait(name, value));
        } else {
            super.setTraitImpl(name, value);
        }
    }

    /**
     * AbstractShapeNode handles the transform attribute.
     * Other attributes are handled by the super class.
     *
     * @param name name of trait to set
     * @param matrix Transform value of trait
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
        // We use .equals for the transform attribute as the string may not
        // have been interned. We use == for the motion pseudo attribute because
        // it is only used internally and from the SVGConstants strings.
        if (SVGConstants.SVG_TRANSFORM_ATTRIBUTE.equals(name)) {
            setTransform(matrix);
        } else if (SVGConstants.SVG_MOTION_PSEUDO_ATTRIBUTE == name) {
            setMotion(matrix);
        } else {
            super.setMatrixTraitImpl(name, matrix);
        }
    }

    /**
     * @param name the name of the trait to convert.
     * @param value the float trait value to convert.
     */
    String toStringTrait(final String name, final float[][] value) {
        if (SVGConstants.SVG_TRANSFORM_ATTRIBUTE == name) {
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

}
