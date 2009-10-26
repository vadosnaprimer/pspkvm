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
import com.sun.perseus.j2d.Path;
import com.sun.perseus.j2d.RenderGraphics;
import com.sun.perseus.j2d.Tile;
import com.sun.perseus.j2d.Transform;

import org.w3c.dom.DOMException;

import org.w3c.dom.svg.SVGRect;
import org.w3c.dom.svg.SVGMatrix;

import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

/**
 * The central structure manipulated in Perseus is called the <em>Model</em>. It
 * is used for the in-memory representation of an SVG Tiny document structure.
 * 
 * <p>The <code>ModelNode</code> abstraction represents an atomic node in the
 * <b><code>Model</code></b>'s tree structure. </p>
 * 
 * <p>A <code>ModelNode</code> may have regular children (which can be added on
 * <code>CompositeNode</code> implementations, for example), and expanded
 * children (which are automatically computed by the node, for example on
 * <code>Text</code> or on <code>Use</code>). In rendering order, expanded
 * content comes second.</p>
 *
 * <p>A <code>ModelNode</code> can have a parent, which may be null, but it
 * always belongs to a <code>DocumentNode</code> instance which is guaranteed to
 * no be null.</p>
 * 
 * <p>A <code>ModelNode</code> can be painted into a 
 * @link {com.sun.perseus.j2d.RenderGraphics RenderGraphics}. 
 *
 * <p>A <code>ModelNode</code> is subjet to interactivity and its
 * @link {#nodeHitAt nodeHitAt} method can be used to perform hit detection.</p>
 *
 * @version $Id: ModelNode.java,v 1.24 2006/06/29 10:47:33 ln156897 Exp $
 */
public abstract class ModelNode implements EventTarget {
    /**
     * Used in cases the parent node is null.
     */
    protected final Transform IDENTITY = new Transform(null);
    
    /**
     * The parent node
     */
    protected ModelNode parent;

    /**
     * The next sibling
     */
    protected ModelNode nextSibling;

    /**
     * The next sibling
     */
    protected ModelNode prevSibling;

    /**
     * Mask for the renderable bit.
     */
    public static final int CAN_RENDER_RENDERABLE_MASK = ~0x1;

    /**
     * Mask for the required features bit
     */
    public static final int CAN_RENDER_REQUIRED_FEATURES_MASK = ~(1 << 1);

    /**
     * Mask for the required extensions bit
     */
    public static final int CAN_RENDER_REQUIRED_EXTENSIONS_MASK = ~(1 << 2);

    /**
     * Mask for the required languages bit
     */
    public static final int CAN_RENDER_SYSTEM_LANGUAGE_MASK = ~(1 << 3);

    /**
     * Mask for conditions met, i.e., requiredFeatures, requiredExtensions,
     * and systemLanguage.
     */
    public static final int CAN_RENDER_CONDITIONS_MET_MASK =
        CAN_RENDER_REQUIRED_FEATURES_MASK 
        & CAN_RENDER_REQUIRED_EXTENSIONS_MASK
        & CAN_RENDER_SYSTEM_LANGUAGE_MASK;

    /**
     * Mask defining which bits from the canRenderState value are copied over
     * to newly created proxies. See the description of the canRenderState
     * bits below.
     */
    public static final int CAN_RENDER_PROXY_BITS_MASK = 0x7EE;
    /**
     * Mask for the non-invertible transform bit
     */
    public static final int CAN_RENDER_NON_INVERTIBLE_TXF_MASK = ~(1 << 4);

    /**
     * Mask for the display bit
     */
    public static final int CAN_RENDER_DISPLAY_MASK = ~(1 << 5);

    /**
     * Mask for the zero-width bit
     */
    public static final int CAN_RENDER_ZERO_WIDTH_MASK = ~(1 << 6);

    /**
     * Mask for the zero-height bit
     */
    public static final int CAN_RENDER_ZERO_HEIGHT_MASK = ~(1 << 7);

    /**
     * Mask for the empty viewbox bit
     */
    public static final int CAN_RENDER_EMPTY_VIEWBOX_MASK = ~(1 << 8);

    /**
     * Mask for the empty path bit
     */
    public static final int CAN_RENDER_EMPTY_PATH_MASK = ~(1 << 9);

    /**
     * Mask for the zero font-size bit
     */
    public static final int CAN_RENDER_ZERO_FONT_SIZE_MASK = ~(1 << 10);

    /**
     * Mask for the in-document-tree bit
     */
    public static final int CAN_RENDER_IN_DOCUMENT_TREE_MASK = ~(1 << 11);

    /**
     * Mask for the paren canRenderState bit
     */
    public static final int CAN_RENDER_PARENT_STATE_MASK = ~(1 << 12);


    /**
     * Mask for the renderable bit.
     */
    public static final int CAN_RENDER_RENDERABLE_BIT 
        = ~CAN_RENDER_RENDERABLE_MASK;

    /**
     * Mask for the required features bit
     */
    public static final int CAN_RENDER_REQUIRED_FEATURES_BIT 
        = ~CAN_RENDER_REQUIRED_FEATURES_MASK;

    /**
     * Mask for the required extensions bit
     */
    public static final int CAN_RENDER_REQUIRED_EXTENSIONS_BIT 
        = ~CAN_RENDER_REQUIRED_EXTENSIONS_MASK;

    /**
     * Mask for the system language bit
     */
    public static final int CAN_RENDER_SYSTEM_LANGUAGE_BIT 
        = ~CAN_RENDER_SYSTEM_LANGUAGE_MASK;

    /**
     * Mask for the conditionsMet bits
     */
    public static final int CAN_RENDER_CONDITIONS_MET_BITS 
        = ~CAN_RENDER_CONDITIONS_MET_MASK;

    /**
     * Mask for the non-invertible transform bit
     */
    public static final int CAN_RENDER_NON_INVERTIBLE_TXF_BIT 
        = ~CAN_RENDER_NON_INVERTIBLE_TXF_MASK;

    /**
     * Mask for the display bit
     */
    public static final int CAN_RENDER_DISPLAY_BIT 
        = ~CAN_RENDER_DISPLAY_MASK;

    /**
     * Mask for the zero-width bit
     */
    public static final int CAN_RENDER_ZERO_WIDTH_BIT 
        = ~CAN_RENDER_ZERO_WIDTH_MASK;

    /**
     * Mask for the zero-height bit
     */
    public static final int CAN_RENDER_ZERO_HEIGHT_BIT 
        = ~CAN_RENDER_ZERO_HEIGHT_MASK;

    /**
     * Mask for the empty viewbox bit
     */
    public static final int CAN_RENDER_EMPTY_VIEWBOX_BIT 
        = ~CAN_RENDER_EMPTY_VIEWBOX_MASK;

    /**
     * Mask for the empty path bit
     */
    public static final int CAN_RENDER_EMPTY_PATH_BIT 
        = ~CAN_RENDER_EMPTY_PATH_MASK;

    /**
     * Mask for the zero font-size bit
     */
    public static final int CAN_RENDER_ZERO_FONT_SIZE_BIT 
        = ~CAN_RENDER_ZERO_FONT_SIZE_MASK;

    /**
     * Mask for the in document tree bit
     */
    public static final int CAN_RENDER_IN_DOCUMENT_TREE_BIT 
        = ~CAN_RENDER_IN_DOCUMENT_TREE_MASK;

    /**
     * Mask for the parent cansRenderState bit
     */
    public static final int CAN_RENDER_PARENT_STATE_BIT 
        = ~CAN_RENDER_PARENT_STATE_MASK;

    /**
     * Pre-computes whether or not this node can render. This is a bit-mask.
     *
     * 0  : renderable (i.e., initialized in the class constructor). All 
     *      classes.
     * 1  : required features. ElementNode
     * 2  : required extensions. ElementNode
     * 3  : required languages. ElementNode.
     * 4  : non-invertible transform. StructureNode, AbstractRenderingNode.
     * 5  : display. CompositeGraphicsNode.
     * 6  : width is zero. Ellipse, ImageNode, Rect, SVG, Viewport
     * 7  : height is zero. Ellipse, ImageNode, Rect, SVG, Viewport
     * 8  : viewBox width or height is zero or negative.
     * 9  : path is null or has no segments. ShapeNode.
     * 10 : font-size is negative or zero. Text.
     * 11 : node in document tree.
     * 12 : parent canRenderState. This 1 bit value is set if any of the bits
     *      in the parent node's canRenderState is set.
     *
     * Only bits 1/2/3 are special in that they need to be applied from a node
     * to its proxies. Other bits (e.g., bit 5 (display)) do not need to be
     * propagated because they are set when the property changes on the node and
     * there is already propagation of property changes to the proxies.
     *
     * By default, ModelNodes are not renderable and are not in the document 
     * tree.
     */
    protected int canRenderState 
        = CAN_RENDER_RENDERABLE_BIT | CAN_RENDER_IN_DOCUMENT_TREE_BIT;

    /**
     * Used to track if this node is loaded or not. A node becomes loaded after
     * its <code>markLoaded</code> method is called.  Note: the default is
     * <b>true</b> because when used through scripting, nodes are considered
     * fully loaded and operational.  When used by the builder module or a
     * parser, the flag needs to be first turned off (see the
     * <code>setLoaded</code> method to control the progressive rendering
     * behavior (see the <code>CanvasManager</code> class).
     */
    protected boolean loaded = true;

    /**
     * The owner <code>DocumentNode</code>
     */
    protected DocumentNode ownerDocument;
    
    /**
     * By default, a node's bounding box is not accounted for. This is 
     * overridden by children classes to specify when and under which condition
     * their bounding box should be accounted for.
     *
     * @return true if the node's bounding box should be accounted for.
     */
    protected boolean contributeBBox() {
        return false;
    }

    /**
     * @return the <code>UpdateListener</code> associated with the nearest
     *         <code>DocumentNode</code> ancestor or null if there is no
     *         <code>Viewporpt</code> ancestor
     */
    protected UpdateListener getUpdateListener() {
        //
        // It is important to _not_ use ownerDocument.getUpdateListener
        // because we do not report changes to elements which are not 
        // hooked into the document tree.
        //
        // May be that should be changed in the future: we could also
        // always report updates to the listener and let it figure out
        // whether or not the reporting node is in the Document tree
        // or not yet.
        //
        if (parent == null) {
            return null;
        } else {
            return parent.getUpdateListener();
        }
    }

    /**
     * Returns the next sibling object of this object, or
     * <code>null</code> if this is the last sibling.
     *
     * @return the next sibling object.
     */
    public ModelNode getNextSiblingNode() {
        return nextSibling;
    }

    /**
     * Returns the previous sibling object of this object, or
     * <code>null</code> if this is the first child node and there
     * is no previous sibling.
     *
     * @return the next sibling object.
     */
    public ModelNode getPreviousSiblingNode() {
        return prevSibling;
    }

    /**
     * @return a reference to the parent <code>ModelNode</code>
     */
    public ModelNode getParent() {
        return parent;
    }

    /**
     * @param newParent this node's new parent
     */
    protected void setParent(final ModelNode newParent) {
        modifyingNode();
        setParentQuiet(newParent);
        nodeInserted(this);
    }
    
    /**
     * Sets this node's parent but does not generate change 
     * events.
     * 
     * @param newParent the node's new parent node.
     */
    protected void setParentQuiet(final ModelNode newParent) {
        if (parent == newParent) {
            return;
        }

        // If the new parent is not null, check whether or not 
        // it is in the document tree. In all cases, we need 
        // to unhook and then hook again.
        if (parent != null) {
            onUnhookedFromDocumentTree();
        }
        
        parent = newParent;
        
        // If the parent is not null and in the document tree,
        // we process that condition again.
        if (parent != null) {
            if (parent.isInDocumentTree()) {
                onHookedInDocumentTree();
            }
        }
    }

    /**
     * Utility method. Unhooks a node and all its following siblings, 
     * quietly setting the parent and or proxy to null.
     *
     * @param node the root of the branch to unhook.
     */
    protected final void unhookQuiet(ModelNode node) {
        if (node != null) {
            if (node.prevSibling != null) {
                node.prevSibling.nextSibling = null;
            }
        }

        while (node != null) {
            node.setParentQuiet(null);
            if (node instanceof ElementNodeProxy) {
                ((ElementNodeProxy) node).setProxied(null);
            }
            node.unhookChildrenQuiet();
            node.unhookExpandedQuiet();
            node = node.nextSibling;
        }
    }

    /**
     * Utilitty method. Returns true if the input node is part of the 
     * Document tree, i.e., if its top most ancestor is equal to its
     * ownerDocument.
     *
     * @return true if this node is a <code>DocumentNode</code> or if 
     *         one of its ancestors is a <code>DocumentNode</code>
     */
    protected boolean isInDocumentTree() {
        return ((canRenderState & CAN_RENDER_IN_DOCUMENT_TREE_BIT) == 0);
    }

    /**
     * Returns the <code>DocumentNode</code> this node is attached
     * to.
     *
     * @return the <code>DocumentNode</code> this node belongs to. The return
     *         value should never be null.
     */
    public DocumentNode getOwnerDocument() {
        return ownerDocument;
    }

    /**
     * By default, this node returns true if there are no children.
     * <code>ModelNode</code> implementations with expanded content
     * should override this method.
     *
     * @return true if this node has, or may have children or expanded
     *         children. A node may not know in advance if it has expanded
     *         children because in some cases, the computation of expanded
     *         content is done lazily. So this returns false if the node
     *         does not have children and will never have expanded content.
     */
    public boolean hasDescendants() {
        return getFirstChildNode() != null;
    }

    /**
     * Paints the input node and all its siblings into the  
     * <code>RenderGraphics</code>
     *
     * @param node the <code>ModelNode</code> to paint.
     * @param rg the <code>RenderGraphics</code> where the nodes should 
     *           be painted
     */
    static void paint(ModelNode node, final RenderGraphics rg) {
        while (node != null) {
            node.paint(rg);
            node = node.nextSibling;
        }
    }

    /**
     * @return the inherited value for the requested property.
     */
    protected final Object getInheritedPropertyState(final int propertyIndex) {
        if (parent == null) {
            return ownerDocument.getInitialPropertyState(propertyIndex);
        }

        return parent.getPropertyState(propertyIndex);
    }

    /**
     * @return the inherited value for the requested float property.
     */
    protected final float getInheritedFloatPropertyState(
            final int propertyIndex) {
        if (parent == null) {
            return ownerDocument.getInitialFloatPropertyState(propertyIndex);
        }

        return parent.getFloatPropertyState(propertyIndex);
    }

    /**
     * @return the inherited value for the requested packed property.
     */
    protected final int getInheritedPackedPropertyState(
            final int propertyIndex) {
        if (parent == null) {
            return ownerDocument.getInitialPackedPropertyState(propertyIndex);
        }

        return parent.getPackedPropertyState(propertyIndex);
    }

    /**
     * Returns the value for the given property.
     *
     * @return the value for the given property, null if the property is 
     *         unknown.
     */
    protected Object getPropertyState(final int propertyIndex) {
        return ownerDocument.getInitialPropertyState(propertyIndex);
    }
    
    /**
     * Returns the value for the given packed property.
     *
     * @return the value of the given property.
     */
    protected int getPackedPropertyState(final int propertyIndex) {
        return ownerDocument.getInitialPackedPropertyState(propertyIndex);
    }
    
    /**
     * Returns the value for the given float property.
     *
     * @return the value of the given property.
     */
    protected float getFloatPropertyState(final int propertyIndex) {
        return ownerDocument.getInitialFloatPropertyState(propertyIndex);
    }
    
    /**
     * Sets the computed value for the given property.
     *
     * @param propertyIndex the property index
     * @param propertyValue the computed value for the property.
     */
    protected void setPropertyState(final int propertyIndex,
                                    final Object propertyValue) {
    }

    /**
     * Sets the computed value for the given float property.
     *
     * @param propertyIndex the property index
     * @param propertyValue the computed value for the property.
     */
    protected void setFloatPropertyState(final int propertyIndex,
                                         final float propertyValue) {
    }

    /**
     * Sets the computed value for the given packed property.
     *
     * @param propertyIndex the property index
     * @param propertyValue the computed value for the property.
     */
    protected void setPackedPropertyState(final int propertyIndex,
                                          final int propertyValue) {
    }

    /**
     * Checks the state of the property value.
     *
     * @param propertyIndex the property index
     * @param propertyValue the computed value for the property.
     */
    protected boolean isPropertyState(final int propertyIndex,
                                      final Object propertyValue) {
        // By default, return true so that we don't bother setting an unknown 
        // property.
        return true;
    }

    /**
     * Checks the state of the float property value.
     *
     * @param propertyIndex the property index
     * @param propertyValue the computed value for the property.
     */
    protected boolean isFloatPropertyState(final int propertyIndex,
                                           final float propertyValue) {
        // By default, return true so that we don't bother setting an unknown 
        // property.
        return true;
    }

    /**
     * Checks the state of the packed property value.
     *
     * @param propertyIndex the property index
     * @param propertyValue the computed value for the property.
     */
    protected boolean isPackedPropertyState(final int propertyIndex,
                                            final int propertyValue) {
        // By default, return true so that we don't bother setting an unknown 
        // property.
        return true;
    }

    /**
     * Recomputes the given property's state given the new parent property.
     * By default, this simply propagates to children.
     *
     * @param propertyIndex index for the property whose value is changing.
     * @param parentPropertyValue the value that children of this node should 
     *        now inherit.
     * 
     */
    protected void recomputePropertyState(final int propertyIndex,
                                          final Object parentPropertyValue) {
        propagatePropertyState(propertyIndex, parentPropertyValue);
    }

    /**
     * Recomputes the given float property's state given the new parent
     * property. By default, this simply propagates to children.
     *
     * @param propertyIndex index for the property whose value is changing.
     * @param parentPropertyValue the value that children of this node should 
     *        now inherit.
     * 
     */
    protected void recomputeFloatPropertyState(
            final int propertyIndex,
            final float parentPropertyValue) {
        propagateFloatPropertyState(propertyIndex, parentPropertyValue);
    }

    /**
     * Recomputes the given packed property's state given the new parent 
     * property. By default, this simply propagates to children.
     *
     * @param propertyIndex index for the property whose value is changing.
     * @param parentPropertyValue the value that children of this node should 
     *        now inherit.
     * 
     */
    protected void recomputePackedPropertyState(final int propertyIndex,
                                                final int parentPropertyValue) {
        propagatePackedPropertyState(propertyIndex, parentPropertyValue);
    }

    /**
     * Recomputes all inherited properties and propages the recomputation to 
     * children.
     */
    void recomputeInheritedProperties() {
        ModelNode c = getFirstChildNode();
        while (c != null) {
            c.recomputeInheritedProperties();
            c = c.nextSibling;
        }
    }
    
    /**
     * Called when the canRenderState changes. If both values are zero, or
     * if both values are non zero, then children need to recompute their
     * canRenderState and propagate if need be.
     *
     * @param oldCanRenderState the old value for canRenderState.
     * @param newCanRenderState the new value for canRenderState
     */
    protected void propagateCanRenderState(final int oldCanRenderState,
                                           final int newCanRenderState) {
        if (oldCanRenderState != newCanRenderState
            &&
            ((oldCanRenderState == 0) || (newCanRenderState == 0))) {
            if (newCanRenderState == 0) {
                // Clear the parent can render state bit and propagate.
                // Propagate to regular children.
                ModelNode node = getFirstChildNode();
                boolean nodeDisplay = false;
                int nodeOldState = 0;
                while (node != null) {                    
                    nodeOldState = node.canRenderState;
                    node.canRenderState &= CAN_RENDER_PARENT_STATE_MASK;
                    node.propagateCanRenderState(nodeOldState, 
                                                 node.canRenderState);
                    node = node.nextSibling;
                }
                
                // Propagate to expanded children.
                node = getFirstComputedExpandedChild();
                while (node != null) {
                    nodeOldState = node.canRenderState;
                    node.canRenderState &= CAN_RENDER_PARENT_STATE_MASK;
                    node.propagateCanRenderState(nodeOldState, 
                                                 node.canRenderState);
                    node = node.nextSibling;
                }

            } else {
                // Set the parent can render state bit and propagate.
                // Propagate to regular children.
                ModelNode node = getFirstChildNode();
                boolean nodeDisplay = false;
                int nodeOldState = 0;
                while (node != null) {                    
                    nodeOldState = node.canRenderState;
                    node.canRenderState |= CAN_RENDER_PARENT_STATE_BIT;
                    node.propagateCanRenderState(nodeOldState, 
                                                 node.canRenderState);
                    node = node.nextSibling;
                }
                
                // Propagate to expanded children.
                node = getFirstComputedExpandedChild();
                while (node != null) {
                    nodeOldState = node.canRenderState;
                    node.canRenderState |= CAN_RENDER_PARENT_STATE_BIT;
                    node.propagateCanRenderState(nodeOldState, 
                                                 node.canRenderState);
                    node = node.nextSibling;
                }

            }
        }
    }

    /**
     * Called when the computed value of the given property has changed.
     * By default, propagate both to regular content and expanded content.
     *
     * @param propertyIndex index for the property whose value has changed.
     * @param parentPropertyValue the value that children of this node should 
     *        now inherit.
     */
    protected void propagatePropertyState(final int propertyIndex,
                                          final Object parentPropertyValue) {
        // Propagate to regular children.
        ModelNode node = getFirstChildNode();
        while (node != null) {
            node.recomputePropertyState(propertyIndex, parentPropertyValue);
            node = node.nextSibling;
        }

        // Propagate to expanded children.
        node = getFirstExpandedChild();
        while (node != null) {
            node.recomputePropertyState(propertyIndex, parentPropertyValue);
            node = node.nextSibling;
        }
    }

    /**
     * Called when the computed value of the given float property has changed.
     * By default, propagate both to regular content and expanded content.
     *
     * @param propertyIndex index for the property whose value has changed.
     * @param parentPropertyValue the value that children of this node should 
     *        now inherit.
     */
    protected void propagateFloatPropertyState(
            final int propertyIndex,
            final float parentPropertyValue) {
        // Propagate to regular children.
        ModelNode node = getFirstChildNode();
        while (node != null) {
            node.recomputeFloatPropertyState(propertyIndex, 
                                             parentPropertyValue);
            node = node.nextSibling;
        }

        // Propagate to expanded children.
        node = getFirstExpandedChild();
        while (node != null) {
            node.recomputeFloatPropertyState(propertyIndex, 
                                             parentPropertyValue);
            node = node.nextSibling;
        }
    }

    /**
     * Called when the computed value of the given packed property has changed.
     * By default, propagate both to regular content and expanded content.
     *
     * @param propertyIndex index for the property whose value has changed.
     * @param parentPropertyValue the value that children of this node should 
     *        now inherit.
     */
    protected void propagatePackedPropertyState(final int propertyIndex,
                                                final int parentPropertyValue) {
        // Propagate to regular children.
        ModelNode node = getFirstChildNode();
        while (node != null) {
            node.recomputePackedPropertyState(propertyIndex, 
                                              parentPropertyValue);
            node = node.nextSibling;
        }

        // Propagate to expanded children.
        node = getFirstExpandedChild();
        while (node != null) {
            node.recomputePackedPropertyState(propertyIndex, 
                                              parentPropertyValue);
            node = node.nextSibling;
        }
    }

    /**
     * @return this node's cached transform. 
     */
    public Transform getTransformState() {
        // By default, a ModelNode does not add any new transform, so its
        // cached transform state is the same as its parent node.
        if (parent != null) {
            return parent.getTransformState();
        }
        
        return IDENTITY;
    }

    /**
     * @return this node's cached inverse transform. 
     */
    Transform getInverseTransformState() {
        // By default, a ModelNode does not add any new transform, so its
        // cached inverse transform state is the same as its parent node.
        if (parent != null) {
            return parent.getInverseTransformState();
        }
        
        return IDENTITY;
    }

    /**
     * Recomputes the transform cache, if one exists. This should recursively
     * call recomputeTransformState on children node or expanded content, if
     * any.
     *
     * By default, because a ModelNode has no transform and no cached transform,
     * this only does a pass down.
     */
    protected void recomputeTransformState() {
        if (parent == null) {
            recomputeTransformState(new Transform(null));
        } else {
            recomputeTransformState(parent.getTransformState());
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
        recomputeTransformState(getTransformState(), getFirstChildNode());
        recomputeTransformState(getTransformState(), getFirstExpandedChild());
        computeCanRenderTransformBit(getTransformState());
    }

    /**
     * Recomputes the transform cache of the input <code>ModelNode</code>
     * and all its siblings. Implementation helper.
     *
     * @param parentTransform the parent transform.
     * @param node first node whose transform cache should be 
     *        cleared.
     */
    void recomputeTransformState(final Transform parentTransform, 
                                 ModelNode node) {
        while (node != null) {
            node.recomputeTransformState(parentTransform);
            node = node.nextSibling;
        }
    }
    
    /**
     * @return true if this node is hooked to the document tree, i.e., if it top
     * most ancestor is the DocumentNode.
     */
    boolean inDocumentTree() {
        return parent != null && parent.inDocumentTree();
    }

    // JAVADOC COMMENT ELIDED
    public SVGMatrix getScreenCTM() {
        // The CTM is null if the element is not in the document tree.
        if (!inDocumentTree()) {
            return null;
        }

        return new Transform(getTransformState());
    }

    /**
     * Computes this node's rendering tile.
     *
     * @param tile the Tile instance whose bounds should be set.
     * @return the device space rendering tile.
     */
    protected void computeRenderingTile(final Tile tile) {
        // By default, there is no rendering and the tile is set to reflect
        // 'no rendering'
        tile.x = Integer.MIN_VALUE;
        tile.y = Integer.MIN_VALUE;
        tile.maxX = Integer.MIN_VALUE;
        tile.maxY = Integer.MIN_VALUE;
    }

    /**
     * @return the bounding box, in screen coordinate, which encompasses the
     * node's rendering. If this node's hasRendering method returns false, then
     * this method should return null. By default, this method returns null
     * because hasNodeRendering returns null by default.
     */
    protected Tile getRenderingTile() {
        return null;
    }

    /**
     * @return the tile which encompasses the node's last actual rendering. If
     * this node's hasRendering method returns false, then this method should
     * return null. By default, this method returns null because
     * hasNodeRendering returns null by default.
     */
    protected Tile getLastRenderedTile() {
        return null;
    }

    /**
     * After calling this method, getLastRenderedTile should always return null.
     */
    protected void clearLastRenderedTile() {
    }

    /**
     * Utility method to recycle a <code>Transform</code> instance when 
     * possible.
     *
     * @param tx the <code>Transform</code> to use to initialize the returned
     *        value.
     * @param workTx the candidate <code>Transform</code> instance which may be
     *        re-cycled. The instance can be recycled if it is different 
     *        (i.e., a different reference) from the input base transform 
     *        <code>tx</code>.
     */
    protected final Transform recycleTransform(final Transform tx,
                                               final Transform workTx) {
        if (workTx != null && workTx != tx) {
            // We recycle workTx
            workTx.setTransform(tx);
            return workTx;
        } else {
            // System.err.println(">>>>>>> creating new Transform instance...");
            // We cannot use workTx because it is null or 
            // it is the same as tx.
            return new Transform(tx);
        }        
    }
    
    /**
     * Should be overridden by derived classes to apply any node specific 
     * transform to the input transform.
     *
     * If the input transform is null and this node does not add any 
     * transform, the return value should be null.
     *
     * If the input transform is null and this node adds a transform, the
     * return value should be an object equal to this node's transform,
     * but not be the same instance.
     *
     * If the input transform is not null and this node does not add any
     * transform, the return value should be the same as the input transform.
     *
     * If the input transform is not null and this node adds a transform,
     * the return value should be a new (different) transform.
     *
     * The second parameter, workTxf can be re-used to hold the result 
     * only if it is different from tx. If it is referencing the same instance
     * as tx and a new Transform needs to be created, then it should not
     * be reused. 
     * IMPL NOTE : This is a coding style to recycle <code>Transform</code>
     * instances in animation loops.
     *
     * @param tx the <code>Transform</code> to apply additional node 
     *        transforms to. This may be null.
     * @param workTx a <code>Transform</code> which can be re-used if a 
     *        new <code>Transform</code> needs to be created and workTx
     *        is not the same instance as tx.
     * @return a transform with this node's transform added.
     */
    protected Transform appendTransform(final Transform tx, 
                                        final Transform workTx) {
        // Does nothing by default. See derived classes.
        return tx;
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
        return bbox;
    }

    /**
     * Implementation helper. Adds the input shape's bounding box to the
     * input bounding box rect. If the shape is null, nothing is added.
     *
     * @param path the shape whose bounds, transformed through t, should be
     *        added to bbox.
     * @param t the transform to the target bounds space.
     * @param bbox the bounding box to which the shape's bounds should be
     *        added.
     * @return a rectangle that encompasses bbox and the path's bounding box,
     *         after transformation to the target coordinate space.
     */
    static Box addShapeBBox(Box bbox,
                            final Path path,
                            final Transform t) {
        if (path == null || path.getNumberOfSegments() == 0) {
            return bbox;
        }

        Box b = null;
        if (t == null) {
            b = path.getBounds();
        } else {
            b = path.getTransformedBounds(t);
        }

        return addBBox(bbox, 
                       b.getX(),
                       b.getY(),
                       b.getWidth(),
                       b.getHeight());
    }

    /**
     * Implementation helper. Adds the input rectangle to the input bounding box
     * rect.
     *
     * @param bbox the rectangle to which the new box should be added.
     * @param x the x-axis origin of the new rect to add
     * @param y the y-axis origin of the new rect to add
     * @param width the width of the rect to add
     * @param height the height of the rect to add
     * @return a bounding box that encompasses bbox and the (x, y, width, 
     *         height) rectangle.
     */
    static Box addBBox(Box bbox, 
                       final float x,
                       final float y,
                       final float width,
                       final float height) {
        if (bbox == null) {
            bbox = new Box(x, y, width, height);
            return bbox;
        }

        bbox.width = bbox.x + bbox.width;
        bbox.height = bbox.y + bbox.height;
        
        if (bbox.x > x) {
            bbox.x = x;
        }

        if (bbox.y > y) {
            bbox.y = y;
        }

        if (bbox.width < x + width) {
            bbox.width = x + width;
        }

        if (bbox.height < y + height) {
            bbox.height = y + height;
        }

        bbox.width -= bbox.x;
        bbox.height -= bbox.y;

        return bbox;
    }

    /**
     * Implementation helper. Computes the bounding box of the rectangle
     * transformed by the input matrix.
     *
     * @param bbox the bounding box to which this node's bounds should be 
     *        added.
     * @param x the rectangle's x-axis origin
     * @param y the rectangle's y-axis origin
     * @param width the rectangle's width
     * @param height the rectangle's height
     * @param m the matrix transforming to the target coordinate space
     *        into which the returned rectangle should be.
     * @return the matrix from the input rectangle's coordinate space to 
     *         the target coordinate space.
     */
    static Box addTransformedBBox(Box bbox,
                                  final float x,
                                  final float y,
                                  final float width,
                                  final float height,
                                  final Transform m) {
        
        if (m == null) {
            return addBBox(bbox, x, y, width, height);
        }

        float tx = (float) (m.getComponent(0) * x 
                            + m.getComponent(2) * y + m.getComponent(4));
        float ty = (float) (m.getComponent(1) * x 
                            + m.getComponent(3) * y + m.getComponent(5));

        float dx1 = (float) (m.getComponent(0) * width);
        float dy1 = (float) (m.getComponent(1) * width);

        float dx2 = (float) (m.getComponent(2) * height);
        float dy2 = (float) (m.getComponent(3) * height);

        if (dx1 < 0) {
            tx += dx1;
            dx1 = -dx1;
        }

        if (dx2 > 0) {
            dx1 += dx2;
        } else {
            dx1 -= dx2;
            tx += dx2;
        }

        if (dy1 < 0) {
            ty += dy1;
            dy1 = -dy1;
        }

        if (dy2 > 0) {
            dy1 += dy2;
        } else {
            dy1 -= dy2;
            ty += dy2;
        }

        return addBBox(bbox, tx, ty, dx1, dy1);
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
        return bbox;
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
        return null;
    }

    /**
     * Returns the <code>ModelNode</code>, if any, hit by the
     * point at coordinate x/y, in viewport space. The input 
     * node, and all its siblings, are tested.
     *
     * @param node the first node on the set of nodes to test.
     * @param pt the point which is hit
     * @return the node hit at the given coordinate, or null if
     *         no node was hit.
     */
    protected final ModelNode nodeHitAt(ModelNode node, final float[] pt) {
        ModelNode hit = null;
        while (node != null) {
            hit = node.nodeHitAt(pt);
            if (hit != null) {
                return hit;
            }
            node = node.prevSibling;
        }
        return null;
    }

    /**
     * The node's URI base to use to resolve URI references
     * If a URI base value was set on this node, then that value
     * is returned. Otherwise, this method returns the parent's 
     * URI base. If there is not URI base on this node and if there
     * is not parent, then this method returns null.
     *
     * @return the node's URI base to use to resolve relative URI references.
     */
    protected String getURIBase() {
        if (parent != null) {
            return parent.getURIBase();
        }
        return null;
    }
    
    /**
     * Recomputes the requiredFeatures bit in the canRenderState bit mask.
     *
     * @param requiredFeatures if ConditionalProcessing.checkFeatures returns
     * true, the bit is cleared. Otherwise, the bit is set.
     */
    final void computeCanRenderRequiredFeaturesBit(
            final String[] requiredFeatures) {
        int oldCanRenderState = canRenderState;
        if (requiredFeatures == null
            ||
            ConditionalProcessing.checkFeatures(requiredFeatures)) {
            canRenderState &= CAN_RENDER_REQUIRED_FEATURES_MASK;
        } else {
            canRenderState |= CAN_RENDER_REQUIRED_FEATURES_BIT;
        }
        
        propagateCanRenderState(oldCanRenderState, canRenderState);
    }

    /**
     * Recomputes the requiredExtensions bit in the canRenderState bit mask.
     *
     * @param requiredExtensions if ConditionalProcessing.checkExtensions 
     *        returns true, the bit is cleared. Otherwise, the bit is set.
     */
    final void computeCanRenderRequiredExtensionsBit(
            final String[] requiredExtensions) {
        int oldCanRenderState = canRenderState;
        if (requiredExtensions == null
            ||
            ConditionalProcessing.checkExtensions(requiredExtensions)) {
            canRenderState &= CAN_RENDER_REQUIRED_EXTENSIONS_MASK;
        } else {
            canRenderState |= CAN_RENDER_REQUIRED_EXTENSIONS_BIT;
        }
        
        propagateCanRenderState(oldCanRenderState, canRenderState);
    }

    /**
     * Recomputes the systemLanguage bit in the canRenderState bit mask.
     *
     * @param systemLanguage if ConditionalProcessing.checkLanguage returns
     * true, the bit is cleared. Otherwise, the bit is set.
     */
    final void computeCanRenderSystemLanguageBit(
            final String[] systemLanguage) {
        int oldCanRenderState = canRenderState;
        if (systemLanguage == null
            ||
            ConditionalProcessing.checkLanguage(systemLanguage)) {
            canRenderState &= CAN_RENDER_SYSTEM_LANGUAGE_MASK;
        } else {
            canRenderState |= CAN_RENDER_SYSTEM_LANGUAGE_BIT;
        }
        
        propagateCanRenderState(oldCanRenderState, canRenderState);
    }

    /**
     * Recomputes the non-invertible transform bit in the canRenderState 
     * bit mask.
     *
     * @param transform the transform which drives the non-invertible 
     * transform bit. If non invertible, the bit is set. Otherwise, the 
     * bit is cleared.
     */
    final void computeCanRenderTransformBit(final Transform transform) {
        int oldCanRenderState = canRenderState;
        if (transform == null || transform.isInvertible()) {
            canRenderState &= CAN_RENDER_NON_INVERTIBLE_TXF_MASK;
        } else {
            canRenderState |= CAN_RENDER_NON_INVERTIBLE_TXF_BIT;
        }
        
        propagateCanRenderState(oldCanRenderState, canRenderState);
    }

    /**
     * Recomputes the empty viewBox bit in the canRenderState 
     * bit mask.
     *
     * @param viewBox the viewBox value which drives the empty viewBox
     * bit. If null or if positive width/height, the bit is cleared. 
     * Otherwise, the bit is set.
     */
    final void computeCanRenderEmptyViewBoxBit(final float[][] viewBox) {
        int oldCanRenderState = canRenderState;
        if (viewBox == null || (viewBox[1][0] > 0 && viewBox[2][0] > 0)) {
            canRenderState &= CAN_RENDER_EMPTY_VIEWBOX_MASK;
        } else {
            canRenderState |= CAN_RENDER_EMPTY_VIEWBOX_BIT;
        }
        
        propagateCanRenderState(oldCanRenderState, canRenderState);
    }

    /**
     * Recomputes the display bit in the canRenderState bit mask.
     *
     * @param display the display value. If true, the bit is cleared. If
     * false, the bit is set.
     */
    final void computeCanRenderDisplayBit(final boolean display) {
        int oldCanRenderState = canRenderState;
        if (display) {
            canRenderState &= CAN_RENDER_DISPLAY_MASK;
        } else {
            canRenderState |= CAN_RENDER_DISPLAY_BIT;
        }
        
        propagateCanRenderState(oldCanRenderState, canRenderState);
    }
    
    /**
     * Recomputes the zero-width bit in the canRenderState bit mask.
     *
     * @param width the new width value. If zero or negative, the bit
     * is set. Otherwise, the bit is cleared.
     */
    final void computeCanRenderWidthBit(final float width) {
        int oldCanRenderState = canRenderState;
        if (width > 0) {
            canRenderState &= CAN_RENDER_ZERO_WIDTH_MASK;
        } else {
            canRenderState |= CAN_RENDER_ZERO_WIDTH_BIT;
        }
        
        propagateCanRenderState(oldCanRenderState, canRenderState);
    }

    /**
     * Recomputes the zero-height bit in the canRenderState bit mask.
     *
     * @param height the new height value. If zero or negative, the bit
     * is set. Otherwise, the bit is cleared.
     */
    final void computeCanRenderHeightBit(final float height) {
        int oldCanRenderState = canRenderState;
        if (height > 0) {
            canRenderState &= CAN_RENDER_ZERO_HEIGHT_MASK;
        } else {
            canRenderState |= CAN_RENDER_ZERO_HEIGHT_BIT;
        }
        
        propagateCanRenderState(oldCanRenderState, canRenderState);
    }

    /**
     * Recomputes the zero-fontSize bit in the canRenderState bit mask.
     *
     * @param fontSize the new fontSize value. If zero or negative, the bit
     * is set. Otherwise, the bit is cleared.
     */
    final void computeCanRenderFontSizeBit(final float fontSize) {
        int oldCanRenderState = canRenderState;
        if (fontSize > 0) {
            canRenderState &= CAN_RENDER_ZERO_FONT_SIZE_MASK;
        } else {
            canRenderState |= CAN_RENDER_ZERO_FONT_SIZE_BIT;
        }
        
        propagateCanRenderState(oldCanRenderState, canRenderState);
    }

    /**
     * Recomputes the empty path bit in the canRenderStaet bit mask.
     *
     * @param path the value that determines the bit state. If null or
     * if no segments, the bit is set. Otherwise, the bit is cleared.
     */
    final void computeCanRenderEmptyPathBit(final Path path) {
        int oldCanRenderState = canRenderState;
        if (path == null || path.getNumberOfSegments() == 0) {
            canRenderState |= CAN_RENDER_EMPTY_PATH_BIT;
        } else {
            canRenderState &= CAN_RENDER_EMPTY_PATH_MASK;
        }
        
        propagateCanRenderState(oldCanRenderState, canRenderState);
    }

    /**
     * By default, there is no node rendering.
     *
     * @return true if the node has some rendering of its own,
     *         i.e., rendering beyond what its children or
     *         expanded content render.
     */
    protected boolean hasNodeRendering() {
        return false;
    }

    /**
     * Clears the text layouts, if any exist. This is typically
     * called when the font selection has changed and nodes such
     * as <code>Text</code> should recompute their layouts.
     * This should recursively call clearLayouts on children 
     * node or expanded content, if any.
     */
    protected abstract void clearLayouts();

    /**
     * Clears the text layouts in the input node and all its
     * siblings. Implementation helper.
     *
     * @param node the first node whose text layouts should be cleared.
     */
    void clearLayouts(ModelNode node) {
        while (node != null) {
            node.clearLayouts();
            node = node.nextSibling;
        }
    }
    
    /**
     * @return true if this node is considered loaded. This is used
     *         in support of progressive rendering.
     */
    public final boolean isLoaded() {
        return loaded;
    }
    
    /**
     * @param isLoaded the new loaded state
     */
    public void setLoaded(final boolean isLoaded) {
        loaded = isLoaded;
    }

    /**
     * @return true if the node needs to be fully loaded before it
     *         can be painted
     */
    boolean getPaintNeedsLoad() {
        return false;
    }

    /**
     * Used to notify the <code>UpdateListener</code>, if any, of
     * an upcoming node modification
     *
     */
    protected void modifyingNode() {
        UpdateListener updateListener = getUpdateListener();
        if (updateListener != null) {
            updateListener.modifyingNode(this);
        }
    }

    /**
     * Used to notify the <code>UpdateListener</code>, if any, of
     * an  change in a node's rendering
     *
     */
    protected void modifyingNodeRendering() {
        UpdateListener updateListener = getUpdateListener();
        if (updateListener != null) {
            updateListener.modifyingNodeRendering(this);
        }
    }

    /**
     * Used to notify the <code>UpdateListener</code>, if any, of
     * a completed node modification
     *
     */
    protected void modifiedNode() {
        UpdateListener updateListener = getUpdateListener();
        if (updateListener != null) {
            updateListener.modifiedNode(this);
        }
    }

    /**
     * Used to notify the <code>UpdateListener</code>, if any, of
     * a node insertion
     *
     * @param node the node which was just inserted
     */
    protected static void nodeInserted(final ModelNode node) {
        UpdateListener updateListener = node.getUpdateListener();
        if (updateListener != null) {
            updateListener.nodeInserted(node);
        }
    }

    // JAVADOC COMMENT ELIDED
    public void addEventListener(final String type, 
                                 final EventListener listener, 
                                 final boolean useCapture) throws DOMException {
        ownerDocument.eventSupport
            .addEventListener(this, 
                              type, 
                              useCapture 
                              ? 
                              EventSupport.CAPTURE_PHASE : 
                              EventSupport.BUBBLE_PHASE,
                              listener);
        
    }

    // JAVADOC COMMENT ELIDED
    public void removeEventListener(
            final String type, 
            final EventListener listener, 
            final boolean useCapture) throws DOMException {
        ownerDocument.eventSupport
            .removeEventListener(this, 
                                 type, 
                                 useCapture 
                                 ? 
                                 EventSupport.CAPTURE_PHASE : 
                                 EventSupport.BUBBLE_PHASE,
                                 listener);
    }

    /**
     * Delegates to the associated <code>EventSupport</code> class.
     *
     * @param evt the event to dispatch
     */
    public void dispatchEvent(final ModelEvent evt) {
        ownerDocument.eventSupport.dispatchEvent(evt);
    }

    // =========================================================================
    // Methods with default implementations which are typically overridden in
    // extension.
    // =========================================================================

    /**
     * To be overriddent by derived classes, such as TimedElementNode,
     * if they need to perform special operations when hooked into the 
     * document tree.
     */
    void onHookedInDocumentTree() {
        // Clear the 'in document tree' can render bit
        int oldCanRenderState = canRenderState;
        canRenderState &= CAN_RENDER_IN_DOCUMENT_TREE_MASK;
        
        // Set the parent's canRenderState bit
        if (parent.canRenderState == 0) {
            canRenderState &= CAN_RENDER_PARENT_STATE_MASK;
        } else {
            canRenderState |= CAN_RENDER_PARENT_STATE_BIT;
        }
        propagateCanRenderState(oldCanRenderState, canRenderState);
        
        recomputeTransformState();
        recomputeInheritedProperties();
    }

    /**
     * To be overriddent by derived classes, such as TimedElementNode,
     * if they need to perform special operations when unhooked from the 
     * document tree.
     */
    void onUnhookedFromDocumentTree() {
        // Set the 'in document tree' can render bit
        int oldCanRenderState = canRenderState;
        canRenderState |= CAN_RENDER_IN_DOCUMENT_TREE_BIT;
        
        // Clear the parent's canRenderState bit
        canRenderState &= CAN_RENDER_PARENT_STATE_MASK;
        
        propagateCanRenderState(oldCanRenderState, canRenderState);

        recomputeTransformState();
        recomputeInheritedProperties();        
    }

    /**
     * Paints this node into the input <code>RenderGraphics</code>.
     *
     * @param rg the <tt>RenderGraphics</tt> where the node should paint itself
     */
    public void paint(final RenderGraphics rg) {
        // By default, do not paint anything.
    }

    /**
     * To be overridden by nodes which may have a rendering if they need
     * to do something specific (e.g., notify their RenderingManager) when
     * they have been rendered.
     */
    protected void nodeRendered() {
        // By default, do nothing.
    }


    // =========================================================================
    // Abstract method to override in extensions.
    // =========================================================================
 
    /**
     * Utility method. Unhooks the children.
     */
    protected abstract void unhookChildrenQuiet();

    /**
     * Utility method. Unhooks the expanded content.
     */
    protected abstract void unhookExpandedQuiet();

    /**
     * @return a reference to the node's first child, or null if there
     *         are no children.
     */
    public abstract ModelNode getFirstChildNode();

    /**
     * @return a reference to the node's last child, or null if there
     *         are no children.
     */
    public abstract ModelNode getLastChildNode();

    /**
     * Some node types (such as <code>ElementNodeProxy</code>) have
     * expanded children that they compute in some specific
     * way depending on the implementation.     
     *
     * @return a reference to the node's first expanded child, or null if there
     *         are no expanded children. This forces the computation of expanded
     *         content if needed.
     */
    abstract ModelNode getFirstExpandedChild();

    /**
     * Some node types (such as <code>ElementNodeProxy</code>) have
     * expanded children that they compute in some specific
     * way depending on the implementation.     
     *
     * @return a reference to the node's first expanded child, or null if there
     *         are no expanded children. This forces the computation of expanded
     *         content if needed.
     */
    abstract ModelNode getFirstComputedExpandedChild();

    /**
     * Some node types (such as <code>ElementNodeProxy</code>) have
     * expanded children that they compute in some specific
     * way depending on the implementation.     
     *
     * @return a reference to the node's last expanded child, or null if there
     *         are no expanded children. This forces the computation of expanded
     *         content if needed.
     */
    abstract ModelNode getLastExpandedChild();


}
