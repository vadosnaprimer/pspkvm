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

import org.w3c.dom.Node;
import org.w3c.dom.DOMException;

/**
 * <code>CompositeNode</code> models <code>ModelNodes</code which 
 * can have children {@link ElementNode ElementNodes}.
 *
 * <p>A <code>CompositeNode</code> can have either <code>ModelNode</code>
 * children and text content children (see the 
 * {@link #appendTextChild appendTextChild} method).
 *
 * @version $Id: CompositeNode.java,v 1.9 2006/06/29 10:47:30 ln156897 Exp $
 */
public abstract class CompositeNode extends ModelNode {
    /**
     * The first child
     */
    protected ElementNode firstChild;

    /**
     * The last child
     */
    protected ElementNode lastChild;


    /**
     * Clears the text layouts, if any exist. This is typically
     * called when the font selection has changed and nodes such
     * as <code>Text</code> should recompute their layouts.
     * This should recursively call clearLayouts on children 
     * node or expanded content, if any.
     */
    protected void clearLayouts() {
        clearLayouts(firstChild);
    }
    
    /**
     * @return this node's first child.
     */
    public ModelNode getFirstChildNode() {
        return firstChild;
    }

    /**
     * @return this node's last child.
     */
    public ModelNode getLastChildNode() {
        return lastChild;
    }

    /**
     * Utility method. Unhooks the children.
     */
    protected void unhookChildrenQuiet() {
        unhookQuiet(firstChild);
        firstChild = null;
        lastChild = null;
    }

    /**
     * Appends an element at the end of the list
     *
     * @param element the node to add to this <tt>CompositeNode</tt>
     * @throws NullPointerException if the input argument is null.
     */
    public void add(final ElementNode element) {
        if (element == null) {
            throw new NullPointerException();
        }

        element.preValidate();

        if (firstChild == null) {
            firstChild = element;
            lastChild = element;
            element.nextSibling = null;
            element.prevSibling = null;
        } else {
            lastChild.nextSibling = element;
            element.nextSibling = null;
            element.prevSibling = lastChild;
            lastChild = element;
            
        }

        // An insertion event is triggered from the setParent call.
        element.setParent(this);
    }

    // =========================================================================
    // Node interface implementation.
    // =========================================================================

    /**
     * @return the namespace URI of the Node.
     *
     * @throws SecurityException if the application does not have the necessary
     * privilege rights to access this (SVG) content.
     */
    public abstract String getNamespaceURI();

    /**
     * @return unprefixed node name. For an SVGElement, this returns the tag
     * name without a prefix.  In case of the Document node, string
     * <code>#document</code> is returned.
     * @throws SecurityException if the application does not have the necessary
     * privilege rights to access this (SVG) content.
     */
    public abstract String getLocalName();

    /**
     * Returns the parent <code>Node</code> of this <code>Node</code>.
     *
     * @return the parent node or null if there is no parent (i.e. if a node has
     * just been created and not yet added to the tree, or if it has been
     * removed from the tree, this is null).
     * @throws SecurityException if the application does not have the necessary
     * privilege rights to access this (SVG) content.
     */
    public abstract Node getParentNode();

    // JAVADOC COMMENT ELIDED
    public Node appendChild(final Node newChild) throws DOMException {
        return insertBefore(newChild, null);
    }

    // JAVADOC COMMENT ELIDED
    public Node removeChild(final Node oldChild) throws DOMException {
        // First, check if this is indeed a child of this node.
        if (!isChild(oldChild)) {
            if (oldChild == null) {
                throw new NullPointerException();
            }

            throw new DOMException(
                    DOMException.NOT_FOUND_ERR, 
                    Messages.formatMessage(Messages.ERROR_NOT_A_CHILD, null));
        }

        // Now, check if this is a supported operation
        if (!isRemoveChildSupported()) {
            throw new DOMException
                (DOMException.NOT_SUPPORTED_ERR,
                 Messages.formatMessage(
                        Messages.ERROR_REMOVE_CHILD_NOT_SUPPORTED,
                        new String[] {
                            getLocalName(),
                            getNamespaceURI()
                        }));
        }

        // Check if the removed child, or one of its descendants, has
        // an identifier.
        ElementNode oldNode = (ElementNode) oldChild;
        if (isIdBranch(oldNode)) {
            throw new DOMException(
                    DOMException.INVALID_ACCESS_ERR, 
                    Messages.formatMessage(
                        Messages.ERROR_CANNOT_REMOVE_NODE_WITH_ID,
                        null));
        }

        if (oldNode.nextSibling != null) {
            oldNode.nextSibling.prevSibling = oldNode.prevSibling;
        } else {
            lastChild = (ElementNode) oldNode.prevSibling;
        }

        if (oldNode.prevSibling != null) {
            oldNode.prevSibling.nextSibling = oldNode.nextSibling;
        } else {
            firstChild = (ElementNode) oldNode.nextSibling;
        }

        oldNode.nextSibling = null;
        oldNode.prevSibling = null;
        oldNode.setParent(null);
        
        return oldChild;
    }

    // JAVADOC COMMENT ELIDED
    public Node insertBefore(final Node newChild, 
                             final Node refChild) throws DOMException {
        if (newChild == null) {
            throw new NullPointerException();
        }

        if (newChild == ownerDocument) {
            throw new DOMException
                (DOMException.HIERARCHY_REQUEST_ERR, 
                 Messages.formatMessage
                 (Messages.ERROR_CANNOT_INSERT_DOCUMENT_NODE,
                                        null));
        }

        // The DocumentNode class can only create ElementNode instances.  If we
        // are dealing with an object which is not an ElementNode instance, we
        // know we are dealing with something in the wrong document.
        if (!(newChild instanceof ElementNode)) {
            throw new DOMException
                (DOMException.WRONG_DOCUMENT_ERR, 
                 Messages.formatMessage
                 (Messages.ERROR_CANNOT_INSERT_FROM_OTHER_DOCUMENT,
                                        null));
        }

        ElementNode newNode = (ElementNode) newChild;
        
        // Check if newNode belongs to the same document.
        if (newNode.ownerDocument != ownerDocument) {
            throw new DOMException
                (DOMException.WRONG_DOCUMENT_ERR,
                 Messages.formatMessage
                 (Messages.ERROR_CANNOT_INSERT_FROM_OTHER_DOCUMENT,
                                        null));
        }

        // Check if the newNode is of a type allowed by this Node
        // implementation.
        if (!isAllowedChild(newNode)) {
            throw new DOMException
                (DOMException.HIERARCHY_REQUEST_ERR, 
                 Messages.formatMessage
                 (Messages.ERROR_CHILD_NOT_ALLOWED,
                  new String[] {newNode.getLocalName(),
                                newNode.getNamespaceURI(),
                                getLocalName(),
                                getNamespaceURI()}));
        }

        // Check if the newNode is one of this node's ancestor, or the 
        // node itself.
        if (isAncestor(newNode)) {
            throw new DOMException
                (DOMException.HIERARCHY_REQUEST_ERR, 
                 Messages.formatMessage
                 (Messages.ERROR_INSERTING_ANCESTOR,
                  null));
        }

        // Check if this node is of type document and already has a child.
        if (this == ownerDocument) {
            if (firstChild != null) {
                throw new DOMException
                    (DOMException.HIERARCHY_REQUEST_ERR,
                     Messages.formatMessage
                     (Messages.ERROR_INSERTING_UNDER_DOCUMENT, null));
            }
        }

        // Check refChild if refChild is not null
        if (refChild != null) {
            if (!isChild(refChild)) {
                throw new DOMException
                    (DOMException.NOT_FOUND_ERR,
                     Messages.formatMessage
                     (Messages.ERROR_REF_NODE_NOT_A_CHILD, null));
            }
        }

        // Check that newChild is _not_ a child of the DocumentNode
        if (newNode.parent == ownerDocument) {
            throw new DOMException
                (DOMException.NOT_SUPPORTED_ERR, 
                 Messages.formatMessage
                 (Messages.ERROR_INSERTING_DOCUMENT_ELEMENT,
                  null));
        }

        // If the inserted node already has a parent, remove the node
        // from its current parent.
        if (newNode.parent != null) {
            ((CompositeNode) newNode.parent).removeChild(newNode);
        }

        if (refChild == null) {
	    // Performs the Use xlink:href attribute validation
            add(newNode);
        } else {
            // Because refChild's parent is this node, we know it has to 
            // be an ElementNode. So the following cast is safe. See 
            // isChild call above.
            ElementNode refNode = (ElementNode) refChild;
            newNode.prevSibling = refNode.prevSibling;
            newNode.nextSibling = refNode;
            if (refNode.prevSibling != null) {
                // refNode is _not_ the first node
                refNode.prevSibling.nextSibling = newNode;
            } else {
                // refNode _is_ the first node
                firstChild = newNode;
            }
            refNode.prevSibling = newNode;
            newNode.nextSibling = refNode;

            // An insertion event is triggered from the setParent call
            newNode.setParent(this);
        }

        return newChild;
    }

    /**
     * Implementation helper. By default, we only disallow SVG nodes under
     * all nodes except DocumentNode.
     *
     * @param node the candidate child node.
     * @return true if the input node can be inserted under this CompositeNode
     */
    protected boolean isAllowedChild(final ElementNode node) {
        if (node instanceof SVG) {
            return false;
        }
        return true;
    }

    /**
     * Implementation helper.
     *
     * @param node the node which may be an ancestor of this CompositeNode.
     * @return true if the input node is this node or if it is one of its 
     *         ancestors.
     */
    final boolean isAncestor(final ElementNode node) {
        if (node == this) {
            return true;
        } else if (parent != null) {
            return ((CompositeNode) parent).isAncestor(node);
        } else {
            return false;
        }
    }

    /**
     * Implementation helper.
     *
     * @param node the node which may be a child of this composite node.
     * @return true if the input node is one of this composite's children.
     */
    final boolean isChild(final Node node) {
        ElementNode c = firstChild;
        while (c != null) {
            if (c == node) {
                return true;
            } 
            c = (ElementNode) c.nextSibling;
        }
        return false;
    }

    /**
     * @return true if this node supports removing children. By default, this
     * returns true.
     */
    protected boolean isRemoveChildSupported() {
        return true;
    }

    /**
     * @param node the root of the branch to check for ids. Should not be null.
     * @return true if the input node, or any of its descendant, has an id.
     */
    protected static boolean isIdBranch(final ElementNode node) {
        if (node.id != null) {
            return true;
        } 

        ElementNode c = node.firstChild;
        while (c != null) {
            if (isIdBranch(c)) {
                return true;
            } else {
                c = (ElementNode) c.nextSibling;
            }
        }
        
        return false;
    }

    /**
     * When a CompositeNode is hooked into the document tree, by default,
     * it notifies its children and calls its own nodeHookedInDocumentTree
     * method.
     */
    final void onHookedInDocumentTree() {
        super.onHookedInDocumentTree();

        nodeHookedInDocumentTree();

        ModelNode c = getFirstExpandedChild();
        while (c != null) {
            c.onHookedInDocumentTree();
            c = c.nextSibling;
        }

        c = getFirstChildNode();
        while (c != null) {
            c.onHookedInDocumentTree();
            c = c.nextSibling;
        }
    }

    /**
     * When a CompositeNode is hooked into the document tree, by default,
     * it notifies its children and calls its own nodeUnhookedFromDocumentTree
     * method.
     */
    final void onUnhookedFromDocumentTree() {
        super.onUnhookedFromDocumentTree();

        nodeUnhookedFromDocumentTree();

        ModelNode c = getFirstExpandedChild();
        while (c != null) {
            c.onUnhookedFromDocumentTree();
            c = c.nextSibling;
        }

        c = getFirstChildNode();
        while (c != null) {
            c.onUnhookedFromDocumentTree();
            c = c.nextSibling;
        }
    }

    /**
     * To be overriddent by derived classes, such as TimedElementNode,
     * if they need to do special operations when hooked into the 
     * document tree.
     */
    void nodeHookedInDocumentTree() {
    }

    /**
     * To be overriddent by derived classes, such as TimedElementNode,
     * if they need to do special operations when unhooked from the 
     * document tree.
     */
    void nodeUnhookedFromDocumentTree() {
    }

}
