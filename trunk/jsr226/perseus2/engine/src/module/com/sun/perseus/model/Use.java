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

import org.w3c.dom.svg.SVGMatrix;
import org.w3c.dom.svg.SVGRect;

import com.sun.perseus.j2d.Box;
import com.sun.perseus.j2d.RenderGraphics;
import com.sun.perseus.j2d.Transform;

/**
 * Represents an SVG Tiny <code>&lt;use&gt;</code> element.
 * 
 * @version $Id: Use.java,v 1.13 2006/06/29 10:47:36 ln156897 Exp $
 */
public class Use extends Group implements IDRef {
    /**
     * xlink:href is required on <use>
     */
    static final String[][] REQUIRED_TRAITS_NS
        = { {SVGConstants.XLINK_NAMESPACE_URI, 
             SVGConstants.SVG_HREF_ATTRIBUTE} };

    /**
     * The x and y coordinates of the use element
     */
    protected float x, y;

    /**
     * Expanded content tree: the root <code>ElementNodeProxy</code>
     */
    protected ElementNodeProxy proxy;

    /**
     * The identifier of the referenced node
     */
    protected String idRef;

    /**
     * The referenced element.
     */
    protected ElementNode ref;

    /**
     * Constructor.
     *
     * @param ownerDocument this element's owner <code>DocumentNode</code>
     */
    public Use(final DocumentNode ownerDocument) {
        super(ownerDocument);
    }

    /**
     * Clears the text layouts, if any exist. This is typically
     * called when the font selection has changed and nodes such
     * as <code>Text</code> should recompute their layouts.
     * This should recursively call clearLayouts on children 
     * node or expanded content, if any.
     */
    protected void clearLayouts() {
        clearLayouts(proxy);
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
        if (canRenderState != 0) {
            return null;
        }
        
        // Check for a hit on expanded content
        return nodeHitAt(getLastExpandedChild(), pt);
    }

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
        if (canRenderState != 0) {
            return null;
        }
        
        // Check for a hit on expanded content
        return nodeHitAt(proxy.getLastExpandedChild(), pt);
    }

    /**
     * Called when the computed value of the given property has changed.
     * On a use element, this propagates only to expanded content.
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
                ((CompositeGraphicsNodeProxy) proxy)
                        .proxiedPropertyStateChange(propertyIndex, 
                                                    parentPropertyValue);
                proxy = proxy.nextProxy;
            }
        }

        // Propagate to expanded children.
        ModelNode node = getFirstExpandedChild();
        while (node != null) {
            node.recomputePropertyState(propertyIndex, parentPropertyValue);
            node = node.nextSibling;
        }
    }

    /**
     * Called when the computed value of the given float property has changed.
     * On a use element, this propagates only to expanded content.
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

        // Propagate to expanded children.
        ModelNode node = getFirstExpandedChild();
        while (node != null) {
            node.recomputeFloatPropertyState(propertyIndex, 
                                             parentPropertyValue);
            node = node.nextSibling;
        }
    }

    /**
     * Called when the computed value of the given packed property has changed.
     * On a use element, this propagates only to expanded content.
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

        // Propagate to expanded children.
        ModelNode node = getFirstExpandedChild();
        while (node != null) {
            node.recomputePackedPropertyState(propertyIndex, 
                                              parentPropertyValue);
            node = node.nextSibling;
        }
    }

    /**
     * Recomputes the transform cache, if one exists. This should recursively
     * call recomputeTransformState on children node or expanded content, if
     * any.
     *
     * By default, because a ModelNode has no transform and no cached transform,
     * this only does a pass down.
     *
     * @param parentTransform the Transform applied to this node's parent.
     */
    protected void recomputeTransformState(final Transform parentTransform) {
        txf = appendTransform(parentTransform, txf);
        inverseTxf = null;
        computeCanRenderTransformBit(txf);
        // inverseTxf = computeInverseTransform(txf, parentTransform, 
        //                                      inverseTxf);
        recomputeTransformState(txf, getFirstExpandedChild());
    }

    /**
     * Paints this node into the input <code>RenderGraphics</code>.
     *
     * @param rg the <tt>RenderGraphics</tt> where the node should paint itself
     */
    public void paint(final RenderGraphics rg) {
        if (canRenderState != 0) {
            return;
        }

        paint(getFirstExpandedChild(), rg);
    }

    /**
     * This method is called before an element is hooked into the tree to
     * validate it is in a state where it can be added. For a Use element, the
     * reference must have been set and resolved or must still be null (i.e.,
     * not set).
     */
    protected void preValidate() {
        if (loaded && proxy == null) { 
            if (ref == null && (idRef != null) && !"".equals(idRef)) {
                throw new DOMException(DOMException.INVALID_STATE_ERR,
                                       Messages.formatMessage
                                       (Messages.ERROR_MISSING_REFERENCE, 
                                        new String[] {
                                            getNamespaceURI(),
                                            getLocalName(),
                                            getId()}));
            }
        }
    }

    /**
     * Sets this <code>Use</code> element's idRef
     *
     * @param idRef the identifier of the referenced node. Should not be null
     */
    public void setIdRef(final String idRef) {
        if (idRef == this.idRef
            ||
            (idRef != null && idRef.equals(this.idRef))) {
            // No change
            return;
        }

        this.idRef = idRef;
        ownerDocument.resolveIDRef(this, idRef);

        // If the node is hooked into the document tree and the idRef has not
        // been resolved as a result of ownerDocument.resolveIDRef, then
        // we go into error.
        if (isInDocumentTree()) {
            if (proxy == null && (idRef != null) && !"".equals(idRef)) {
                throw new DOMException(DOMException.INVALID_ACCESS_ERR,
                                       Messages.formatMessage
                                       (Messages.ERROR_MISSING_REFERENCE, 
                                        new String[] {
                                            getNamespaceURI(),
                                            getLocalName(),
                                            getId()}));
            }
        }
    }

    /**
     * @return the SVGConstants.SVG_USE_TAG value
     */
    public String getLocalName() {
        return SVGConstants.SVG_USE_TAG;
    }

    /**
     * Used by <code>DocumentNode</code> to create a new instance from
     * a prototype <code>Use</code>.
     *
     * @param doc the <code>DocumentNode</code> for which a new node is
     *        should be created.
     * @return a new <code>Use</code> for the requested document.
     */
    public ElementNode newInstance(final DocumentNode doc) {
        return new Use(doc);
    }

    /**
     * @return the <code>Use</code> element's idRef.
     */
    public String getIdRef() {
        return idRef;
    }

    /**
     * <code>IDRef</code> implementation.
     *
     * @param ref the resolved reference (mapped from the
     *        id passed to the setIdRef method).
     */
    public void resolveTo(final ElementNode ref) {
        // Only set the proxy if the use is in the document tree.
        if (isInDocumentTree()) {
            setProxy(ref.buildProxy());
        } else {
            this.ref = ref;
        }
    }

    /**
     * When a Use element is hooked into the document tree, it may 
     * expand immediately if its reference has already been set.
     */
    void nodeHookedInDocumentTree() {
        super.nodeHookedInDocumentTree();

        if (ref != null) {
            setProxy(ref.buildProxy());
        }
    }

    /**
     * When a Use in unhooked from the document tree, it needs to set its
     * proxy to null and cleanly remove references to itself.
     */
    final void nodeUnhookedFromDocumentTree() {
        super.nodeUnhookedFromDocumentTree();

        if (proxy != null) {
            unhookExpandedQuiet();
            proxy = null;
        }
    }

    /**
     * @return an adequate <code>ElementNodeProxy</code> for this node.
     */
    ElementNodeProxy buildProxy() {
        return new UseProxy(this);
    }

    /**
     * Apply this node's x/y translation if it is not (0,0).
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
        if (transform == null 
            && 
            motion == null
            &&
            x == 0
            &&
            y == 0) {
            return tx;
        } 

        tx = recycleTransform(tx, workTx);
        
        if (motion != null) {
            tx.mMultiply(motion);
        }

        if (transform != null) {
            tx.mMultiply(transform);
        }

        tx.mTranslate(x, y);
        
        return tx;
    }

    /**
     * @param newX new x-axis origin
     */
    public void setX(final float newX) {
        if (newX == x) {
            return;
        }

        modifyingNode();
        this.x = newX;
        recomputeTransformState();
        recomputeProxyTransformState();        
        modifiedNode();
    }

    /**
     * @param newY new y-axis origin
     */
    public void setY(final float newY) {
        if (newY == y) {
            return;
        }

        modifyingNode();
        this.y = newY;
        recomputeTransformState();
        recomputeProxyTransformState();
        modifiedNode();
    }

    /**
     * @return x-axis use origin
     */
    public float getX() {
        return x;
    }

    /**
     * @return y-axis use origin
     */
    public float getY() {
        return y;
    }

    /**
     * @return true if proxy is set
     */
    public boolean hasDescendants() {
        return super.hasDescendants() 
            || 
            proxy != null;
    }

    /**
     * Sets the <code>ElementNodeProxy</code> as this use's expanded
     * content.
     *
     * @param proxy the proxy that references the <code>&lt;use&gt;</code>'s
     *        expanded content. 
     * @throws NullPointerException if the input proxy node is null
     */
    void setProxy(final ElementNodeProxy proxy) {
        if (proxy == null) {
            throw new IllegalArgumentException();
        }

        // If this node already had a proxy, make sure we remove that
        // proxy from the node's expanded content.
        if (this.proxy != null) {
            modifyingNode();
            unhookExpandedQuiet();
            modifiedNode();
        } 

        this.proxy = proxy;
        this.proxy.setParentQuiet(this);
        nodeInserted(this.proxy);
        this.proxy.nextSibling = null;

        // Now, notify potential proxies that the proxy has been set.
        ElementNodeProxy useProxy = firstProxy;
        while (useProxy != null) {
            ((UseProxy) useProxy).useProxySet();
            useProxy = useProxy.nextProxy;
        }
    }

    /**
     * Some node types (such as <code>ElementNodeProxy</code>) have
     * expanded children that they compute in some specific
     * way depending on the implementation.     
     *
     * @return a reference to the node's first expanded child, or null if there
     *         are no expanded children. This forces the computation of expanded
     *         content if needed.
     */
    protected ModelNode getFirstExpandedChild() {
        return proxy;
    }

    /**
     * Some node types (such as <code>ElementNodeProxy</code>) have
     * expanded children that they compute in some specific
     * way depending on the implementation.     
     *
     * @return a reference to the node's first expanded child, or null if there
     *         are no expanded children. 
     */
    public ModelNode getFirstComputedExpandedChild() {
        return proxy;
    }

    /**
     * Some node types (such as <code>ElementNodeProxy</code>) have
     * expanded children that they compute in some specific
     * way depending on the implementation.     
     *
     * @return a reference to the node's last expanded child, or null if there
     *         are no expanded children. This forces the computation of expanded
     *         content if needed.
     */
    protected ModelNode getLastExpandedChild() {
        return proxy;
    }

    /**
     * Utility method. Unhooks the expanded content.
     */
    protected void unhookExpandedQuiet() {
        unhookQuiet(proxy);
        proxy = null;

        ElementNodeProxy p = firstProxy;
        while (p != null) {
            p.unhookExpandedQuiet();
            p = p.nextProxy;
        }
        
    }

    // JAVADOC COMMENT ELIDED
    public SVGRect getBBox() {
        Transform t = null;
        
        if (x != 0 || y != 0) {
            t = new Transform(1, 0, 0, 1, x, y);
        }
        
        return addBBox(null, t);
    }

    // JAVADOC COMMENT ELIDED
    Box addBBox(Box bbox, Transform t) {
        return proxy.addBBox(bbox, proxy.appendTransform(t, null));
    }

    // JAVADOC COMMENT ELIDED
    public SVGMatrix getScreenCTM() {
        SVGMatrix m = super.getScreenCTM();
        if (m != null) {
            m = m.mTranslate(-x, -y);
        } 

        return m;
    }

    /**
     * Supported traits: x, y
     *
     * @param traitName the name of the trait which the element may support.
     * @return true if this element supports the given trait in one of the
     *         trait accessor methods.
     */
    boolean supportsTrait(final String traitName) {
        if (SVGConstants.SVG_X_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_Y_ATTRIBUTE == traitName) {
            return true;
        } else {
            return super.supportsTrait(traitName);
        }
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
     * @return an array of namespaceURI, localName trait pairs required by
     *         this element.
     */
    public String[][] getRequiredTraitsNS() {
        return REQUIRED_TRAITS_NS;
    }

    /**
     * Use handles x and y traits.
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
        } else {
            return super.getTraitImpl(name);
        }
    }

    /**
     * Use handles the xlink href attribute
     *
     * @param namespaceURI the requested trait's namespace URI.
     * @param name the requested trait's local name (i.e., un-prefixed, as 
     *        "href")
     * @return the requested trait's string value.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to a String (SVG Tiny only).
     * @throws SecurityException if the application does not have the necessary
     * privilege rights to access this (SVG) content.
     */
    String getTraitNSImpl(String namespaceURI, String name)
        throws DOMException {
        if (SVGConstants.XLINK_NAMESPACE_URI == namespaceURI
            &&
            SVGConstants.SVG_HREF_ATTRIBUTE == name) {
            if (idRef == null) {
                return "";
            } 
            return "#" + idRef;
        } else {
            return super.getTraitNSImpl(namespaceURI, name);
        }
    }

    /**
     * Use handles x and y traits.
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
            SVGConstants.SVG_Y_ATTRIBUTE == traitName) {
            return new FloatTraitAnim(this, traitName, TRAIT_TYPE_FLOAT);
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
        if (SVGConstants.SVG_X_ATTRIBUTE == traitName) {
            return toAnimatedFloatArray(parseFloatTrait(traitName, value));
        } else if (SVGConstants.SVG_Y_ATTRIBUTE == traitName) {
            return toAnimatedFloatArray(parseFloatTrait(traitName, value));
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
        if (SVGConstants.XLINK_NAMESPACE_URI == namespaceURI
            &&
            SVGConstants.SVG_HREF_ATTRIBUTE == traitName) {
            if (value == null || !value.startsWith("#")) {
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
     * Use handles x and y traits.
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
        } else {
            super.setTraitImpl(name, value);
        }
    }

    /**
     * Use supports the xlink:href trait.
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
        if (SVGConstants.XLINK_NAMESPACE_URI == namespaceURI
            &&
            SVGConstants.SVG_HREF_ATTRIBUTE == name) {
            if (value == null || !value.startsWith("#")) {
                throw illegalTraitValue(name, value);
            }
            setIdRef(value.substring(1));
        } else {
            super.setTraitNSImpl(namespaceURI, name, value);
        }
    }

    /**
     * Use handles x and y traits.
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
        if (SVGConstants.SVG_X_ATTRIBUTE == name) {
            setX(value);
        } else if (SVGConstants.SVG_Y_ATTRIBUTE == name) {
            setY(value);
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
            SVGConstants.SVG_Y_ATTRIBUTE == name) {
            return Float.toString(value[0][0]);
        } else {
            return super.toStringTrait(name, value);
        }
    }

}
