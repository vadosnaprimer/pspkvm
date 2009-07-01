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

import com.sun.perseus.util.SimpleTokenizer;
import com.sun.perseus.util.SVGConstants;
import com.sun.perseus.parser.Length;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.w3c.dom.events.EventListener;

import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGLocatableElement;
import org.w3c.dom.svg.SVGMatrix;
import org.w3c.dom.svg.SVGPath;
import org.w3c.dom.svg.SVGRect;
import org.w3c.dom.svg.SVGRGBColor;

import java.util.Hashtable;
import java.util.Vector;

import com.sun.perseus.j2d.RGB;
import com.sun.perseus.j2d.Path;
import com.sun.perseus.j2d.PaintDef;
import com.sun.perseus.j2d.PaintTarget;
import com.sun.perseus.j2d.PaintServer;
import com.sun.perseus.j2d.Transform;
import com.sun.perseus.j2d.Box;

/**
 * <code>ElementNode</code> models <code>ModelNodes</code which 
 * cna have children.
 *
 * <p>A <code>ElementNode</code> can have either <code>ElementNode</code>
 * children and text content children 
 * (see the {@link #appendTextChild} appendTextChild} method).</p>
 *
 * <p>In addition, <code>Element</code>s can have proxies (see
 * {@link com.sun.perseus.model.ElementNodeProxy ElementNodeProxy}).
 * The proxies are used for implementing the behavior of the 
 * <code>&lt;use&gt;</code> element and the <code>&lt;text&gt;</code>
 * element. The <code>buildProxy</code> and <code>removeProxy</code>
 * methods are used to add or remove proxies from a node.</p>
 *
 * <p>Finally, the <code>ElementNode</code> class provides support
 * for the common XML attributes supported by elements, such as
 * the <code>id</code>, <code>uriBase</code> and conditional attributes.</p>
 * 
 * @version $Id: ElementNode.java,v 1.27 2006/06/29 10:47:30 ln156897 Exp $
 */
public abstract class ElementNode extends CompositeNode 
    implements SVGElement {
    /*
     * Constants for trait types.
     */

    /**
     * String trait type.
     */
    static final String TRAIT_TYPE_STRING = "string";

    /**
     * Float trait type.
     */
    static final String TRAIT_TYPE_FLOAT = "float";

    /**
     * SVGMatrix trait type.
     */
    static final String TRAIT_TYPE_SVG_MATRIX = "SVGMatrix";

    /**
     * SVGPath trait type.
     */
    static final String TRAIT_TYPE_SVG_PATH = "SVGPath";

    /**
     * SVGRect trait type
     */
    static final String TRAIT_TYPE_SVG_RECT = "SVGRect";

    /**
     * SVGRGBColor trait type.
     */
    static final String TRAIT_TYPE_SVG_RGB_COLOR = "SVGRGBColor";

    /**
     * See the SVG 1.1 specification
     */
    public static final int XML_SPACE_PRESERVE = 0;

    /**
     * See the SVG 1.1 specification
     */
    public static final int XML_SPACE_DEFAULT = 1;

    /**
     * Use the parent node's xml:space setting
     */
    public static final int XML_SPACE_INHERIT = 2;

    /**
     * Constant used to identify the per-element partition
     * (sometimes called anonymous) namespace.
     */
    static final String NULL_NS = "#!null/ns@!";

    /**
     * This node's id
     */
    protected String id = null;

    /**
     * This node's URI base
     */
    protected String uriBase = null;

    /**
     * This node's conditional attributes.
     */
    protected String[][] conditionalAttributes;
    
    /**
     * Index for requiredFeatures in conditionalAttributes.
     */
    public static final int REQUIRED_FEATURES_INDEX = 0;
    
    /**
     * Index for requiredExtensions in conditionalAttributes
     */
    public static final int REQUIRED_EXTENSIONS_INDEX = 1;
    
    /**
     * Index for systemLanguage in conditionalAttributes
     */
    public static final int SYSTEM_LANGUAGE_INDEX = 2;

    /**
     * Number of conditional attributes.
     */
    public static final int CONDITIONAl_ATTRIBUTES_LENGTH = 3;
    
    /**
     * The node's text white space handling policy
     */
    protected int xmlSpace = XML_SPACE_INHERIT;

    /**
     * Controls whether or not the node needs to be fully loaded
     * before it can be painted.
     */
    protected boolean paintNeedsLoad = false;

    /**
     * First node proxy. May be null.
     */
    protected ElementNodeProxy firstProxy;

    /**
     * Last node proxy. May be null.
     */
    protected ElementNodeProxy lastProxy;

    /**
     * Used to detect circular references when building
     * proxy chains.
     */
    protected boolean buildingProxy = false;

    /**
     * Maps namespaces to a map of (localName, TraitAnim)
     */
    protected Hashtable traitAnimsNS = null;

    /**
     * Only constructor.
     *
     * @param ownerDocument the document this node belongs to.
     * @throws IllegalArgumentException if the input ownerDocument is null
     */
    public ElementNode(final DocumentNode ownerDocument) {

        if (ownerDocument == null) {
            throw new IllegalArgumentException();
        }

        this.ownerDocument = ownerDocument;
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
        return null;
    }

    /**
     * Recomputes the transform cache on proxy nodes.
     */
    protected void recomputeProxyTransformState() {
        ElementNodeProxy proxy = firstProxy;
        while (proxy != null) {
            proxy.recomputeTransformState();
            proxy = proxy.nextProxy;
        }
    }

    /**
     * Invoked before an element is added to the document tree, to let the 
     * element perform any validation it needs. For example, the use element
     * overrides this method to check that its reference is resolved.
     *
     * Note that the validate() method defined in some element implementations
     * is meant to perform validation after the nodes has been inserted into the
     * document tree.
     */
    protected void preValidate() {
        // By default, do nothing.
    }

    /**
     * By default, an <code>ElementNode</code> has no expanded content, so this 
     * returns null.    
     *
     * @return a reference to the node's first expanded child, or null if there
     *         are no expanded children. This forces the computation of expanded
     *         content if needed.
     */
    ModelNode getFirstExpandedChild() {
        return null;
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
        return null;
    }

    /**
     * By default, an <code>ElementNode</code> has no expanded content, so this 
     * returns null.    
     *
     * @return a reference to the node's last expanded child, or null if there
     *         are no expanded children. This forces the computation of expanded
     *         content if needed.
     */
    ModelNode getLastExpandedChild() {
        return null;
    }

    /**
     * Utility method. Unhooks the expanded content.
     */
    protected void unhookExpandedQuiet() {
        // No expanded content by default.
    }

    /**
     * Utility method. Unhooks the children.
     */
    protected void unhookChildrenQuiet() {
        super.unhookChildrenQuiet();

        // Need to clear the expanded content on proxies
        ElementNodeProxy proxy = firstProxy;
        while (proxy != null) {
            proxy.unhookExpandedQuiet();
            proxy = proxy.nextProxy;
        }
    }

    /**
     * By default, appending a text child does not do anything.
     *
     * @param text the text child to append to this node.
     */
    public void appendTextChild(final String text) {
    }

    /**
     * Adds a proxy to this node. When this node is modified, the
     * <code>ElementNodeProxy</code>'s corresponding modification
     * methods will be called so that modifications also gets reported
     * on the proxy.
     *
     * @param proxy new <code>ElementNodeProxy</code>
     * @throws NullPointerException if the input proxy is null.
     *
     * @see UpdateListener#modifiedNode
     * @see UpdateListener#modifyingNode
     * @see UpdateListener#nodeInserted
     */
    protected void addProxy(final ElementNodeProxy proxy) {

        if (proxy == null) {
            throw new NullPointerException();
        }

        if (firstProxy == null) {
            firstProxy = proxy;
            lastProxy = proxy;
        } else {
            lastProxy.nextProxy = proxy;
            proxy.prevProxy = lastProxy;
            lastProxy = proxy;
        }
    }

    /**
     * Removes a proxy from this node. If the input proxy is null,
     * or is not an existing proxy, this does nothing.
     *
     * @param proxy the <code>ElementNodeProxy</code> to remove.
     *
     * @see #addProxy
     */
    void removeProxy(final ElementNodeProxy proxy) {
        if (proxy == null || firstProxy == null) {
            return;
        }

        // The proxy may be the first one, the last
        // one, or in the middle
        if (proxy == firstProxy) {
            firstProxy = proxy.nextProxy;
        }
        if (proxy == lastProxy) {
            lastProxy = proxy.prevProxy;
        }
        if (proxy.prevProxy != null) {
            proxy.prevProxy.nextProxy = proxy.nextProxy;
        }
        if (proxy.nextProxy != null) {
            proxy.nextProxy.prevProxy = proxy.prevProxy;
        }
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
        
        // During progressive rendering, a proxy may be hooked into
        // the tree while the referenced node is not. This is why we
        // need to notify proxies even if the node is not hooked into 
        // the tree yet.
        ElementNodeProxy proxy = firstProxy;
        while (proxy != null) {
            proxy.modifyingProxied();
            proxy = proxy.nextProxy;
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
        
        // See comment in #modifyingNode.
        ElementNodeProxy proxy = firstProxy;
        while (proxy != null) {
            proxy.modifiedProxied();
            proxy = proxy.nextProxy;
        }
    }

    /**
     * Appends an element at the end of the list
     *
     * @param element the node to add to this <tt>CompositeNode</tt>
     * @throws NullPointerException if the input argument is null.
     */
    public void add(final ElementNode element) {
        super.add(element);

        ElementNodeProxy proxy = firstProxy;
        while (proxy != null) {
            proxy.proxiedChildAdded(element);
            proxy = proxy.nextProxy;
        }
    }

    /**
     * @return an adequate <code>ElementNodeProxy</code> for this node.
     */
    ElementNodeProxy buildProxy() {
        return new ElementNodeProxy(this);
    }

    /**
     * @return an adequate <code>ElementNodeProxy</code> for this node and
     *         makes sure the content is expanded before returning.
     */
    protected ElementNodeProxy buildExpandedProxy() {
        if (buildingProxy) {
            // We ran into a circular reference.
            throw new IllegalStateException();
        }
        buildingProxy = true;
        ElementNodeProxy proxy = buildProxy();
        proxy.expand();
        buildingProxy = false;
        return proxy;
    }

    /**
     * @return the node's identifier
     */
    public String getId() {
        return id;
    }

    /**
     * @param newId the node's identifier
     *
     * @throws DOMException - with error code NO_MODIFICATION_ALLOWED_ERR is
     * raised if an attempt is made to change an existing Id.
     * @throws DOMException - with error code INVALID_ACCESS_ERR is raised if
     * the Id is not unique i.e. if this Id already exists in the document. 
     * 
     * @throws java.lang.NullPointerException - if Id is null.
     */
    public void setId(final String newId) {
        // Null ids are disallowed.
        if (newId == null) {
            throw new NullPointerException();
        }

        // If the id was already set, we cannot let it be modified.
        if (id != null) {
            throw new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                   Messages.formatMessage
                                   (Messages.ERROR_CANNOT_MODIFY_ID,
                                    new String[] {newId,
                                                  id,
                                                  getLocalName(),
                                                  getNamespaceURI()}));
        }

        // Now, check if there is any element with that id already.
        if (ownerDocument.getElementByIdAll(newId) != null) {
            ElementNode duplicateElement = 
                (ElementNode) ownerDocument.getElementByIdAll(newId);
            throw new DOMException(
                    DOMException.INVALID_ACCESS_ERR,
                    Messages.formatMessage(
                        Messages.ERROR_DUPLICATE_ID_VALUE,
                        new String[] {
                            newId,
                            getLocalName(),
                            getNamespaceURI(),
                            duplicateElement.getLocalName(),
                            duplicateElement.getNamespaceURI()
                        }));
        }

        modifyingNode();
        id = newId;

        // We only declare the id in the global document
        // scope, i.e., we only consider the id to be
        // resolved, once the element is loaded, i.e., ready
        // to render. If the element is not loaded, 
        // see the buildComplete method: it adds the element
        // to the list of identified nodes when loaded is
        // set to true.
        if (loaded && isInDocumentTree()) {
            ownerDocument.addIdentifiedNode(this);
        } else {
            ownerDocument.reserveId(this);
        }
        modifiedNode();
    }

    /**
     * When an Element is hooked into the document tree, it needs
     * to register as an identified node if it does have an id.
     */
    void nodeHookedInDocumentTree() {
        super.nodeHookedInDocumentTree();

        if (id != null) {
            ownerDocument.addIdentifiedNode(this);
        }
    }

    /**
     * The node's URI base to use to resolve URI references
     * If a URI base value was set on this node, then that value
     * is returned. Otherwise, this method returns the parent's 
     * URI base.
     *
     * @return the node's URI base to use to resolve relative URI references.
     */
    public String getURIBase() {
        if (uriBase == null) {
            if (parent != null) {
                return parent.getURIBase();
            }
            return null;
        } else {
            if (uriBase.indexOf(":") != -1 || parent == null) {
                // This is not a relative URI, we can return this
                // value
                // - or -
                // There is no parent, return this relative URI
                // as the baseURI
                return uriBase;
            } else {
                // There is no scheme in this node's uri base. 
                // We concatenate the uriBase to the one of the 
                // parent. 
                // This is done according to RFC 2396 
                // (http://www.faqs.org/rfcs/rfc2396.html)
                String parentURIBase = parent.getURIBase();
                if (parentURIBase != null) {
                    int lastSlashIndex = parentURIBase.lastIndexOf('/');
                    if (lastSlashIndex != -1) {
                        parentURIBase 
                            = parentURIBase.substring(0, lastSlashIndex);
                    }
                    return parentURIBase + '/' + uriBase;
                } else {
                    return uriBase;
                }
            }
        }
    }

    /**
     * @param newUriBase the node's new URI base. The uriBase is used
     *        to resolve relative URIs
     */
    public void setURIBase(final String newUriBase) {
        if (equal(newUriBase, uriBase)) {
            return;
        }
        modifyingNode();
        uriBase = newUriBase;
        modifiedNode();
    }

    /**
     * Controls how the node handles white spaces
     *
     * @param newXmlSpace should be one of XML_SPACE_DEFAULT, 
     *        XML_SPACE_PRESERVE, or XML_SPACE_INHERIT. Otherwise, an 
     *        IllegalArgumentException is thrown.
     */
    public void setXMLSpace(final int newXmlSpace) {
        if (newXmlSpace == xmlSpace) {
            return;
        }
        switch (newXmlSpace) {
        case XML_SPACE_DEFAULT:
        case XML_SPACE_PRESERVE:
        case XML_SPACE_INHERIT:
            modifyingNode();
            xmlSpace = newXmlSpace;
            modifiedNode();
            break;
        default:
            throw new IllegalArgumentException();
        }
    }

    /**
     * Defines how the node handles white spaces. Note that
     * if the value is set to null, the node should return 
     * the value of its parent, as the xml:space attribute is
     * inherited.
     * 
     * @return one of XML_SPACE_DEFAULT, XML_SPACE_PRESERVE
     */
    public int getXMLSpace() {
        if (xmlSpace != XML_SPACE_INHERIT) {
            return xmlSpace;
        } else {
            ModelNode ancestor = parent;
            while (ancestor != null) {
                if (ancestor instanceof ElementNode) {
                    return ((ElementNode) parent).getXMLSpace();
                }
                ancestor = ancestor.parent;
            }
            return XML_SPACE_DEFAULT;
        }
    }
    
    /**
     * Returns true if the condition at the given index is equal to the input 
     * value.
     */
    final boolean conditionEquals(final int index,
                                  final String[] conditionValue) {
        if (conditionValue == null) {
            return (conditionalAttributes == null) 
                    ||
                   (conditionalAttributes[index] == null);
        } else {
            return (conditionalAttributes != null)
                    &&
                   (equal(conditionalAttributes[index], conditionValue));
        }
    }

    /**
     * The node will only render if the requiredFeatures string
     * evaluates to true. A null value will evaluate to true.
     *
     * @param newRequiredFeatures the set of features required for rendering
     *        this node.
     */
    public void setRequiredFeatures(final String[] newRequiredFeatures) {
        setConditionalAttribute(REQUIRED_FEATURES_INDEX, newRequiredFeatures);
    }
    
    /**
     * Sets the new value for the given conditional attribute.
     *
     * @param index the conditional attribute index.
     * @param value the new conditional attribute value.
     */
    void setConditionalAttribute(final int index, 
                                 final String[] newValue) {
        if (conditionEquals(index, newValue)) {
            return;
        }
        modifyingNode();
        
        if (newValue == null) {
            if (conditionalAttributes != null) {
                conditionalAttributes[index] = null;
            }
        } else {
            if (conditionalAttributes == null) {
                conditionalAttributes = 
                    new String[CONDITIONAl_ATTRIBUTES_LENGTH][];
            }
            conditionalAttributes[index] 
                = newValue;
        }
        
        switch (index) {
            case REQUIRED_FEATURES_INDEX: 
            {
                computeCanRenderRequiredFeaturesBit(newValue);
                ElementNodeProxy p = firstProxy;
                while (p != null) {
                    p.computeCanRenderRequiredFeaturesBit(newValue);
                    p = p.nextProxy;
                }
            }
                break;
            case REQUIRED_EXTENSIONS_INDEX:   
            {
                computeCanRenderRequiredExtensionsBit(newValue);
                ElementNodeProxy p = firstProxy;
                while (p != null) {
                    p.computeCanRenderRequiredExtensionsBit(newValue);
                    p = p.nextProxy;
                }
            }
                break;
            default:     
            {
                computeCanRenderSystemLanguageBit(newValue);
                ElementNodeProxy p = firstProxy;
                while (p != null) {
                    p.computeCanRenderSystemLanguageBit(newValue);
                    p = p.nextProxy;
                }
            }
                break;
        }
        
        modifiedNode();
    }

    /**
     * The node will only render if the required feature is 
     * supported by Perseus.
     * 
     * @return the array of features required for this node to 
     *         render.
     * @see #setRequiredFeatures
     */
    public String[] getRequiredFeatures() { 
        return getConditionalAttribute(REQUIRED_FEATURES_INDEX);
    }
    
    /**
     * Returns the value of the conditional attribute with the given 
     * index.
     *
     * @param index the conditional attribute index.
     * @return the conditional attribute value.
     */
    String[] getConditionalAttribute(final int index) {
        if (conditionalAttributes != null) {
            return conditionalAttributes[index];
        } else {
            return null;
        }
    }

    /**
     * The node will only render if the requiredExtensions string
     * evaluates to true. A null value evaluates to true.
     * 
     * @param newRequiredExtensions the extensions which will be considered
     *        a match for any document required extension.
     * @see #getRequiredExtensions
     */
    public void setRequiredExtensions
        (final String[] newRequiredExtensions) {
        setConditionalAttribute(REQUIRED_EXTENSIONS_INDEX, 
                                newRequiredExtensions);
    }

    /**
     * The node will only render if the required extension is supported
     * by Perseus.
     *
     * @return the extensions required to render this node
     * @see #setRequiredExtensions
     */
    public String[] getRequiredExtensions() {
        return getConditionalAttribute(REQUIRED_EXTENSIONS_INDEX);
    }

    /**
     * The node will only render if the Perseus user language matches
     * one of the values in this comma separated list. A null value is 
     * a match.
     *
     * @param newSystemLanguage an array of languages which will be matched
     *        against any document system language value
     */
    public void setSystemLanguage(final String[] newSystemLanguage) {
        setConditionalAttribute(SYSTEM_LANGUAGE_INDEX, newSystemLanguage);
    }

    /**
     * The node will only render if the Perseus user language matches
     * one of the values in this comma separated list. A null value is a 
     * match.
     * @return the set of system languages.
     * @see #setSystemLanguage
     */
    public String[] getSystemLanguage() {
        return getConditionalAttribute(SYSTEM_LANGUAGE_INDEX);
    }

    /**
     * @return true if the node needs to be fully loaded before it
     *         can be painted
     */
    public boolean getPaintNeedsLoad() {
        return paintNeedsLoad;
    }

    /**
     * @param paintNeedsLoad if true, the node can only be painted after its
     *        children have been loaded. This can be used by a renderer
     *        implementing progressive rendering of an SVG document.
     */
    public void setPreferedPaintNeedsLoad(final boolean paintNeedsLoad) {
        this.paintNeedsLoad = paintNeedsLoad;
    }

    /**
     * @return the namespace URI of the Node. By default, this returns
     * SVGConstants.SVG_NAMESPACE_URI.
     */
    public String getNamespaceURI() {
        return SVGConstants.SVG_NAMESPACE_URI;
    }

    /**
     * Returns the parent <code>Node</code> of this <code>Node</code>.
     *
     * @return the parent node or null if there is no parent (i.e. if a node has
     * just been created and not yet added to the tree, or if it has been
     * removed from the tree, this is null).
     * @throws SecurityException if the application does not have the necessary
     * privilege rights to access this (SVG) content.
     */
    public Node getParentNode() {
        return (Node) parent;
    }

    /**
     * Used by <code>DocumentNode</code> to create a new instance from
     * a prototype <code>ElementNode</code>.
     *
     * @param doc the <code>DocumentNode</code> for which a new node is
     *        should be created.
     * @return a new <code>ElementNode</code> for the requested document.
     */
    public abstract ElementNode newInstance(final DocumentNode doc);

    /**
     * @return the first child element node of this element. <code>null</code>
     * if this element has no child elements.
     */
    public Element getFirstElementChild() {
        return (Element) firstChild;
    }

    /**
     * @return the next sibling element node of this element. <code>null</code>
     * if this element has no element sibling nodes that come after this one in
     * the document tree.
     */
    public Element getNextElementSibling() {
        // Casting is safe here because ElementNodes can only have ElementNode
        // siblings.
        return (Element) nextSibling;
    }

    /**
     * @return the previous sibling element node of this element.  null if this
     * element has no element sibling nodes that come before this one in the
     * document tree.
     */
    public Element getPreviousElementSibling() {
        // Casting is safe here because ElementNodes can only have ElementNode
        // siblings.
        return (Element) prevSibling;
    }

    /** 
     * @return the last child element node of this element. null if this element
     * has no child elements.
     */
    public Element getLastElementChild() {
        return (Element) lastChild;
    }

    /**
     * @param traitName the name of the trait which the element may support.
     * @return true if this element supports the given trait in one of the
     *         trait accessor methods (such as <code>getTrait</code> or 
     *         <code>setFloatTrait</code>.
     */
    boolean supportsTrait(final String traitName) {
        if (SVGConstants.SVG_ID_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_REQUIRED_FEATURES_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_REQUIRED_EXTENSIONS_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_SYSTEM_LANGUAGE_ATTRIBUTE == traitName) {
            return true;
        }

        return false;
    }

    /**
     * Checks if the input trait name is valid and throws a DOMException 
     * with error code NOT_SUPPORTED_ERR if not.
     *
     * @param name the name whose syntax should be checked.
     * @throws DOMException with error code NOT_SUPPORTED_ERR if the 
     * trait name is syntactically incorrect (e.g., null or containing
     * characters not conforming to the Namespaces in XML specification.
     *
     * @see http://www.w3.org/TR/1999/REC-xml-names-19990114/#NT-NCName
     */
    final void checkNCName(final String name) throws DOMException {
        if (name == null || name.length() == 0) {
            throw unsupportedTrait(name);
        }

        // We should really validate that the name has the conforming syntax
        // But this slows down the load time considerably.
        //
        // NCName        ::=  (Letter | '_') (NCNameChar)*
        // NCNameChar 	 ::=  Letter | Digit | '.' | '-' | '_' | CombiningChar 
        //                      | Extender
    }

    /**
     * @param traitNamespace the namespace of the trait for which a TraitAnim
     *        is requested.
     * @param traitName the trait for which there may be a TraitAnimation.
     * @return the TraitAnim for the requested trait name.
     */
    TraitAnim getSafeTraitAnimNS(final String traitNamespace,
                                 final String traitName) {
        TraitAnim traitAnim = getTraitAnimNS(traitNamespace, traitName);
        if (traitAnim == null) {
            traitAnim = createTraitAnimNS(traitNamespace, traitName);
        }

        return traitAnim;
    }

    /**
     * @param traitNamespace the namespace of the trait for which a TraitAnim
     *        is requested.
     * @param traitName the trait for which there may be a TraitAnimation.
     * @return the TraitAnim for the requested trait name.
     */
    TraitAnim getTraitAnimNS(String traitNamespace,
                             final String traitName) {
        if (traitName == null) {
            throw new NullPointerException();
        }

        if (traitAnimsNS == null) {
            return null;
        }

        if (traitNamespace == null || traitNamespace.length() == 0) {
            traitNamespace = NULL_NS;
        }
        
        Hashtable nsTraitAnims = (Hashtable) traitAnimsNS.get(traitNamespace);
        if (nsTraitAnims != null) {
            return (TraitAnim) nsTraitAnims.get(traitName);
        }
        return null;
    }

    /**
     * @return an array of traits that are required by this element.
     */
    public String[] getRequiredTraits() {
        return null;
    }

    /**
     * @return an array of namespaceURI, localName trait pairs required by
     *         this element.
     */
    public String[][] getRequiredTraitsNS() {
        return null;
    }

    /**
     * @return an array of trait default values, used if this element
     *         requires that the default trait value be explicitly 
     *         set through a setTrait call. This happens, for example,
     *         with the begin trait value on animation elements.
     */
    public String[][] getDefaultTraits() {
        return null;
    }

    /**
     * @return an array of trait aliases. These are used when the 
     * value of a trait can be used to set the value of another trait.
     * For example, on a <rect>, if the rx trait is not specified in the 
     * original XML document, the value fot eh ry trait should be used.
     */
    public String[][] getTraitAliases() {
        return null;
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
     * value is incompatible with the given trait.
     *
     * @return the computed trait value.
     */
    String validateTraitNS(final String namespaceURI,
                           final String traitName,
                           final String value,
                           final String reqNamespaceURI,
                           final String reqLocalName,
                           final String reqTraitNamespace,
                           final String reqTraitName) throws DOMException {
        /* 
        throw new InternalError(
                "Trying to validate unknown trait: " + 
                "namespaceURI : " + namespaceURI + "\n" +
                "traitName : " + traitName + "\n" +
                "value : " + value + "\n" +
                "reqNamespaceURI : " + reqNamespaceURI + "\n" +
                "reqLocalName : " + reqLocalName + "\n" +
                "reqTraitNamespace : " + reqTraitNamespace + "\n" +
                "reqTraitName : " + reqTraitName); 
        */
        return value;
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
    float[][] validateFloatArrayTrait(
            final String traitName,
            final String value,
            final String reqNamespaceURI,
            final String reqLocalName,
            final String reqTraitNamespace,
            final String reqTraitName) throws DOMException {
        // Throw an error because this should _never_ happen, as a float
        // array anim should only happen on a known trait. If validation is
        // requested, the element implementation should know the trait.
        throw new Error();
    }
    
    /**
     * @param namespaceURI the trait's namespace URI.
     * @param traitName the name of the trait which the element may support.
     * @return true if this element supports the given trait in one of the
     *         trait accessor methods.
     */
    boolean supportsTraitNS(final String namespaceURI,
                            final String traitName) {
        if (SVGConstants.PERSEUS_NAMESPACE_URI == namespaceURI
            &&
            SVGConstants.PERSEUS_CHILDREN_REQUIRED_ATTRIBUTE == traitName) {
            return true;
        } else if (SVGConstants.XML_NAMESPACE_URI == namespaceURI
                   &&
                   (SVGConstants.XML_BASE_ATTRIBUTE_LOCAL_NAME == traitName
                    || 
                    SVGConstants.XML_SPACE_ATTRIBUTE_LOCAL_NAME == traitName)) {
            return true;
        }

        if (namespaceURI == null) {
            return supportsTrait(traitName);
        } else {
            return false;
        }
    }

    /**
     * The traits supported by default are: externalResourcesRequired,
     * xml:base, xml:space, requiredFeatures, requiredExtensions and 
     * systemLanguage.
     *
     * Returns the trait value as String. In SVG Tiny only certain traits can be
     * obtained as a String value. Syntax of the returned String matches the
     * syntax of the corresponding attribute. This element is exactly equivalent
     * to {@link org.w3c.dom.svg.SVGElement#getTraitNS getTraitNS} with
     * namespaceURI set to null.
     *
     * The method is meant to be overridden by derived classes. The 
     * implementation pattern is that derived classes will override the method 
     * and call their super class' implementation. If the ElementNode 
     * implementation is called, it means that the trait is either not supported
     * or that it cannot be seen as a String.
     *
     * @param name the requested trait name.
     * @return the trait value.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to a String (SVG Tiny only).
     */
    public final String getTrait(String traitName) throws DOMException {
        traitName = intern(traitName);

        if (!supportsTrait(traitName)) {
            throw unsupportedTrait(traitName);
        }

        TraitAnim anim = getTraitAnimNS(NULL_NS, traitName);
        if (anim == null || !anim.active) {
            return getTraitImpl(traitName);
        }
        
        // Get the computed trait value from the trait animation.
        return anim.getTrait(TRAIT_TYPE_STRING);
    }

    /**
     * Returns the specified trait value as String. In SVG Tiny only certain
     * traits can be obtained as a String value. Syntax of the returned String
     * matches the syntax of the corresponding attribute. This element is
     * exactly equivalent to {@link org.w3c.dom.svg.SVGElement#getTraitNS
     * getTraitNS} with namespaceURI set to null.
     *
     * The method is meant to be overridden by derived classes. The 
     * implementation pattern is that derived classes will override the method 
     * and call their super class' implementation. If the ElementNode 
     * implementation is called, it means that the trait is either not supported
     * or that it cannot be seen as a String.
     *
     * @param traitNamespace the requested trait's namespace.
     * @param traitName the requested trait name.
     * @return the trait value.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to a String (SVG Tiny only).
     */
    String getSpecifiedTraitNSImpl(final String traitNamespace,
                                   final String traitName) throws DOMException {
        if (traitNamespace == null || traitNamespace == NULL_NS) {
            return getSpecifiedTraitImpl(traitName);
        }

        // Only xml:base behaves differently because the computed value
        // may be different from the specified value.
        if (SVGConstants.XML_NAMESPACE_URI == traitNamespace
            &&
            SVGConstants.XML_BASE_ATTRIBUTE_LOCAL_NAME == traitName) {
            return uriBase;
        }
        
        return getTraitNSImpl(traitNamespace, traitName);
    }

    /**
     * Returns the specified trait value as String. In SVG Tiny only certain
     * traits can be obtained as a String value. Syntax of the returned String
     * matches the syntax of the corresponding attribute. This element is
     * exactly equivalent to {@link org.w3c.dom.svg.SVGElement#getTraitNS
     * getTraitNS} with namespaceURI set to null.
     *
     * The method is meant to be overridden by derived classes. The 
     * implementation pattern is that derived classes will override the method 
     * and call their super class' implementation. If the ElementNode 
     * implementation is called, it means that the trait is either not supported
     * or that it cannot be seen as a String.
     *
     * @param traitName the requested trait name.
     * @return the trait value.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to a String (SVG Tiny only).
     */
    String getSpecifiedTraitImpl(final String traitName) throws DOMException {
        return getTraitImpl(traitName);
    }

    /**
     * The traits supported by default are: externalResourcesRequired,
     * xml:base, xml:space, requiredFeatures, requiredExtensions and 
     * systemLanguage.
     *
     * Returns the trait value as String. In SVG Tiny only certain traits can be
     * obtained as a String value. Syntax of the returned String matches the
     * syntax of the corresponding attribute. This element is exactly equivalent
     * to {@link org.w3c.dom.svg.SVGElement#getTraitNS getTraitNS} with
     * namespaceURI set to null.
     *
     * The method is meant to be overridden by derived classes. The 
     * implementation pattern is that derived classes will override the method 
     * and call their super class' implementation. If the ElementNode 
     * implementation is called, it means that the trait is either not supported
     * or that it cannot be seen as a String.
     *
     * @param name the requested trait name.
     * @return the trait value.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to a String (SVG Tiny only).
     */
    public String getTraitImpl(final String name)
        throws DOMException {
        if (SVGConstants.SVG_ID_ATTRIBUTE == name) {
            return getId();
        } else 
            if (SVGConstants.SVG_REQUIRED_FEATURES_ATTRIBUTE == name) {
                return toStringTrait(getRequiredFeatures(), " ");
        } else 
            if (SVGConstants.SVG_REQUIRED_EXTENSIONS_ATTRIBUTE == name) {
                return toStringTrait(getRequiredExtensions(), " ");
        } else 
            if (SVGConstants.SVG_SYSTEM_LANGUAGE_ATTRIBUTE == name) {
                return toStringTrait(getSystemLanguage(), ",");
        } else {
            if (!supportsTrait(name)) {
                if (name == null) {
                    throw unsupportedTrait(name);
                }

                String unknownTraitValue =
                    ownerDocument.getUnknownTraitsNS(this, NULL_NS, name);
                if (unknownTraitValue != null) {
                    return unknownTraitValue;
                } else {
                    return "";
                }
            } else {
                throw unsupportedTraitType(name, TRAIT_TYPE_STRING);
            }
        }
    }

    /**
     * Implementation method.
     *
     * Creates a TraitAnim for the requested trait. This method does not 
     * check whether or not there is an existing TraitAnim for the trait.
     * Instead, it creates a new TraitAnim and associates it with the 
     * given trait. After this call, any call to getSafeTraitAnimNS or
     * getTraitAnimNS will return this new object.
     *
     * @param traitName the trait name.
     * @param traitNamespace the trait's namespace. Should not be null.
     */
    TraitAnim createTraitAnimNS(String traitNamespace,
                                final String traitName) {
        if (traitNamespace == null || traitNamespace.length() == 0) {
            traitNamespace = NULL_NS;
        }

        TraitAnim traitAnim = null;
        if (NULL_NS == traitNamespace) {
            traitAnim = createTraitAnimImpl(traitName);
        } else {
            traitAnim = createTraitAnimNSImpl(traitNamespace, traitName);
        }

        if (traitAnimsNS == null) {
            traitAnimsNS = new Hashtable();
        }

        Hashtable nsTraitAnims = (Hashtable) traitAnimsNS.get(traitNamespace);
        if (nsTraitAnims == null) {
            nsTraitAnims = new Hashtable();
            traitAnimsNS.put(traitNamespace, nsTraitAnims);
        }

        nsTraitAnims.put(traitName, traitAnim);
        return traitAnim;
    }

    /**
     * To be overridden by derived classes. Should create the proper 
     * TraitAnim type for the given trait in the anonymous namespace.
     *
     * @param traitName the trait name.
     */
    TraitAnim createTraitAnimImpl(final String traitName) {
        //
        // If the trait is supported but the element did not create
        // a TraitAnim in its implementation of createTraitAnimImpl,
        // it means the trait is not animatable.
        //
        if (supportsTrait(traitName)) {
            throw notAnimatable(null, 
                                traitName);
        }

        return new StringTraitAnim(this, NULL_NS, traitName);
    }

    /**
     * To be overridden by derived classes. Should create the proper 
     * TraitAnim type for the given trait in the desired namespace.
     *
     * @param traitName the trait name.
     */
    TraitAnim createTraitAnimNSImpl(final String traitNamespace,
                                    final String traitName) {
        //
        // If the trait is supported but the element did not create
        // a TraitAnim in its implementation of createTraitAnimImpl,
        // it means the trait is not animatable.
        //
        if (supportsTraitNS(traitNamespace, traitName)) {
            throw notAnimatable(traitNamespace, 
                                traitName);
        }

        return new StringTraitAnim(this, traitNamespace, traitName);
    }

    /**
     * Same as {@link org.w3c.dom.svg.SVGElement#getTrait getTrait}, but for
     * namespaced traits. Parameter name must be a non-qualified trait name,
     * i.e. without prefix.
     *
     * @param namespaceURI the requested trait's namespace.
     * @param name the requested trait's local name (un-prefixed, e.g, 'href')
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to a String (SVG Tiny only).
     * @throws SecurityException if the application does not have the necessary
     * privilege rights to access this (SVG) content.
     */
    public final String getTraitNS(String namespaceURI, String name)
        throws DOMException {
        if (namespaceURI == null) {
            return getTrait(name);
        }

        name = intern(name);
        namespaceURI = intern(namespaceURI);

        if (!supportsTraitNS(namespaceURI, name)) {
            throw unsupportedTraitNS(name, namespaceURI);
        }

        StringTraitAnim anim = (StringTraitAnim) getTraitAnimNS(namespaceURI, 
                                                                name);
        if (anim == null || !anim.active) {
            return getTraitNSImpl(namespaceURI, name);
        } 

        return anim.getTrait(TRAIT_TYPE_STRING);
    }
            
    /**
     * Same as {@link org.w3c.dom.svg.SVGElement#getTrait getTrait}, but for
     * namespaced traits. Parameter name must be a non-qualified trait name,
     * i.e. without prefix.
     *
     * @param namespaceURI the requested trait's namespace.
     * @param name the requested trait's local name (un-prefixed, e.g, 'href')
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to a String (SVG Tiny only).
     * @throws SecurityException if the application does not have the necessary
     * privilege rights to access this (SVG) content.
     */
    String getTraitNSImpl(String namespaceURI, String name) {
        if (SVGConstants.PERSEUS_NAMESPACE_URI == namespaceURI) {
            if (SVGConstants.PERSEUS_CHILDREN_REQUIRED_ATTRIBUTE == name) {
                if (paintNeedsLoad) {
                    return SVGConstants.SVG_TRUE_VALUE;
                } else {
                    return SVGConstants.SVG_FALSE_VALUE;
                }
            }
        } else if (SVGConstants.XML_NAMESPACE_URI == namespaceURI) {
            if (SVGConstants.XML_BASE_ATTRIBUTE_LOCAL_NAME == name) {
                return getURIBase();
            } else 
                if (SVGConstants.XML_SPACE_ATTRIBUTE_LOCAL_NAME == name) {
                switch (getXMLSpace()) {
                case XML_SPACE_DEFAULT:
                    return SVGConstants.XML_DEFAULT_VALUE;
                case XML_SPACE_PRESERVE:
                default:
                    return SVGConstants.XML_PRESERVE_VALUE;
                }
            }
        } 

        if (!supportsTraitNS(namespaceURI, name)) {
            String unknownTraitValue = 
                ownerDocument.getUnknownTraitsNS(this, namespaceURI, name);
            
            if (unknownTraitValue != null) {
                return unknownTraitValue;
            }
            
            return "";
        } else {
            throw unsupportedTraitTypeNS(name, namespaceURI, TRAIT_TYPE_STRING);
        }
    }

    /**
     * @param name the requested trait's name.
     * @return the trait value as float.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to a float
     */
    public final float getFloatTrait(String name)
        throws DOMException {        
        name = intern(name);

        if (!supportsTrait(name)) {
            throw unsupportedTrait(name);
        }

        TraitAnim anim = getTraitAnimNS(NULL_NS, name);
        if (anim == null || !anim.active) {
            return getFloatTraitImpl(name);
        }

        // Get the computed value from the trait animation.
        return parseFloatTrait(name, anim.getTrait(TRAIT_TYPE_FLOAT));
    }

    /**
     * @param name the requested trait's name.
     * @return the trait value as float.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to a float
     */
    float getFloatTraitImpl(final String name) {
        throw unsupportedTraitType(name, TRAIT_TYPE_FLOAT);
    }

    /**
     * Returns the trait value as {@link org.w3c.dom.svg.SVGMatrix SVGMatrix}.
     * The returned object is a copy of the actual trait value and will not
     * change ifthe corresponding trait changes.
     *
     * @param name matrix trait name.
     * @return the trait value corresponding to name as SVGMatrix.
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to {@link
     * org.w3c.dom.svg.SVGMatrix SVGMatrix}
     */
    public final SVGMatrix getMatrixTrait(String name) 
        throws DOMException {
        name = intern(name);

        if (!supportsTrait(name)) {
            throw unsupportedTrait(name);
        }

        TraitAnim anim = getTraitAnimNS(NULL_NS, name);
        if (anim == null || !anim.active) {
            return getMatrixTraitImpl(name);
        }

        // Get the computed value from the trait animation
        SVGMatrix m = 
            parseTransformTrait(name, anim.getTrait(TRAIT_TYPE_SVG_MATRIX));

        if (m == null) {
            m = new Transform(1, 0, 0, 1, 0, 0);
        }

        return m;
    }

    /**
     * Returns the trait value as {@link org.w3c.dom.svg.SVGMatrix SVGMatrix}.
     * The returned object is a copy of the actual trait value and will not
     * change ifthe corresponding trait changes.
     *
     * @param name matrix trait name.
     * @return the trait value corresponding to name as SVGMatrix.
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to {@link
     * org.w3c.dom.svg.SVGMatrix SVGMatrix}
     */
    SVGMatrix getMatrixTraitImpl(final String name) 
        throws DOMException {
        throw unsupportedTraitType(name, TRAIT_TYPE_SVG_MATRIX);
    }

    /**
     * @param name the trait's name.
     * @return the trait value as {@link org.w3c.dom.svg.SVGRect SVGRect}. The
     * returned object is a copy of the actual trait value and will not change
     * if the corresponding trait changes.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to {@link
     * org.w3c.dom.svg.SVGRect SVGRect}
     */
    public final SVGRect getRectTrait(String name)
        throws DOMException {
        name = intern(name);

        if (!supportsTrait(name)) {
            throw unsupportedTrait(name);
        }

        TraitAnim anim = getTraitAnimNS(NULL_NS, name);
        if (anim == null || !anim.active) {
            return getRectTraitImpl(name);
        }

        // Get the computed value from the trait animation.
        return toSVGRect(toViewBox(name, anim.getTrait(TRAIT_TYPE_SVG_RECT)));
    }            

    /**
     * @param name the trait's name.
     * @return the trait value as {@link org.w3c.dom.svg.SVGRect SVGRect}. The
     * returned object is a copy of the actual trait value and will not change
     * if the corresponding trait changes.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to {@link
     * org.w3c.dom.svg.SVGRect SVGRect}
     */
    SVGRect getRectTraitImpl(final String name) throws DOMException {
        throw unsupportedTraitType(name, TRAIT_TYPE_SVG_RECT);
    }

    /**
     * Returns the trait value as {@link org.w3c.dom.svg.SVGPath SVGPath}. The
     * returned object is a copy of the actual trait value and will not change
     * if the corresponding trait changes.
     *
     * @param name the trait's name.
     * @return the trait's value, as an <code>SVGPath</code> object.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to {@link
     * org.w3c.dom.svg.SVGPath SVGPath}
     */
    public final SVGPath getPathTrait(String name)
        throws DOMException {
        name = intern(name);

        if (!supportsTrait(name)) {
            throw unsupportedTrait(name);
        }

        TraitAnim anim = getTraitAnimNS(NULL_NS, name);
        if (anim == null || !anim.active) {
            return getPathTraitImpl(name);
        }

        // Get the computed value from the trait animation.
        return parsePathTrait(name, anim.getTrait(TRAIT_TYPE_SVG_PATH));
    }


    /**
     * Returns the trait value as {@link org.w3c.dom.svg.SVGPath SVGPath}. The
     * returned object is a copy of the actual trait value and will not change
     * if the corresponding trait changes.
     *
     * @param name the trait's name.
     * @return the trait's value, as an <code>SVGPath</code> object.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to {@link
     * org.w3c.dom.svg.SVGPath SVGPath}
     */
    SVGPath getPathTraitImpl(final String name)
        throws DOMException {
        throw unsupportedTraitType(name, TRAIT_TYPE_SVG_PATH);
    }

    /**
     * Returns the trait value as {@link org.w3c.dom.svg.SVGRGBColor
     * SVGRGBColor}. The returned object is a copy of the trait value and will
     * not change if the corresponding trait changes. If the actual trait value
     * is not an RGBColor (i.e. "none"), this method will return null.
     *
     * @param name the requested trait name.
     * @return the trait value, as an <code>SVGRGBColor</code> object.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to {@link
     * org.w3c.dom.svg.SVGRGBColor SVGRGBColor}
     */
    public final SVGRGBColor getRGBColorTrait(String name)
        throws DOMException {
        name = intern(name);

        if (!supportsTrait(name)) {
            throw unsupportedTrait(name);
        }

        TraitAnim anim = getTraitAnimNS(NULL_NS, name);
        if (anim == null || !anim.active) {
            return getRGBColorTraitImpl(name);
        }

        // Get the computed value from the trait animation
        return toSVGRGBColor(
                name, 
                parseColorTrait(name, anim.getTrait(TRAIT_TYPE_SVG_RGB_COLOR)));
    }

    /**
     * Returns the trait value as {@link org.w3c.dom.svg.SVGRGBColor
     * SVGRGBColor}. The returned object is a copy of the trait value and will
     * not change if the corresponding trait changes. If the actual trait value
     * is not an RGBColor (i.e. "none"), this method will return null.
     *
     * @param name the requested trait name.
     * @return the trait value, as an <code>SVGRGBColor</code> object.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to {@link
     * org.w3c.dom.svg.SVGRGBColor SVGRGBColor}
     */
    SVGRGBColor getRGBColorTraitImpl(final String name)
        throws DOMException {
        throw unsupportedTraitType(name, TRAIT_TYPE_SVG_RGB_COLOR);
    }

    /**
     * Adds a new attribute. 
     *
     * @param name - the name of the attribute to add.
     * @param value - the value to set.
     * @throws DOMException INVALID_CHARACTER_ERR: Raised if the specified name
     * contains an illegal character.  NO_MODIFICATION_ALLOWED_ERR: Raised if
     * this node is readonly.
     */
    public final void setAttribute(String name, String value) 
            throws DOMException {
        checkNCName(name);

        if (value == null) {
            throw illegalTraitValue(name, value);
        }

        name = name.intern();
        setTraitImpl(name, value);
    }

    /**
     * Returns the requested attribute. 
     *
     * @param name - the name of the attribute to add.
     * @return the attribute value or empty string if the attribute is not 
     *         specified.
     * @throws DOMException INVALID_CHARACTER_ERR: Raised if the specified name
     *         contains an illegal character.  
     */
    public final String getAttribute(String name) throws DOMException {
        checkNCName(name);

        name = name.intern();
        return getTraitImpl(name);
    }

    /**
     * The traits supported by default are: externalResourcesRequired,
     * xml:base, xml:space, requiredFeatures, requiredExtensions and 
     * systemLanguage.
     *
     * @param name the trait's name.
     * @param value the trait's value.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if the requested
     * trait's value cannot be specified as a String
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is an invalid value for the given trait or null.
     * @throws DOMException with error code NO_MODIFICATION_ALLOWED_ERR: if
     * attempt is made to change readonly trait.
     */
    public final void setTrait(String name, 
                               String value)
        throws DOMException {
        name = intern(name);

        if (!supportsTrait(name)) {
            throw unsupportedTrait(name);
        }

        if (value == null) {
            throw illegalTraitValue(name, value);
        }


        TraitAnim anim = (TraitAnim) getTraitAnimNS(NULL_NS, name);

        if (anim == null || !anim.active) {
            setTraitImpl(name, value);
        } else {
            anim.setTrait(value, TRAIT_TYPE_STRING);
        }
    }

    /**
     * The traits supported by default are: externalResourcesRequired,
     * xml:base, xml:space, requiredFeatures, requiredExtensions and 
     * systemLanguage.
     *
     * @param name the trait's name.
     * @param value the trait's value.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if the requested
     * trait's value cannot be specified as a String
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is an invalid value for the given trait or null.
     * @throws DOMException with error code NO_MODIFICATION_ALLOWED_ERR: if
     * attempt is made to change readonly trait.
     */
    public void setTraitImpl(final String name, 
                             final String value)
        throws DOMException {
        if (SVGConstants.SVG_ID_ATTRIBUTE == name) {
            try {
                setId(value);
            } catch (IllegalArgumentException iae) {
                iae.printStackTrace();
                throw illegalTraitValue(name, value);
            }
        } else if (SVGConstants.SVG_REQUIRED_FEATURES_ATTRIBUTE == name) {
            if (value == null) {
                throw illegalTraitValue(name, value);
            }
            setRequiredFeatures(parseStringArrayTrait(name, value, " "));
        } else if (SVGConstants.SVG_REQUIRED_EXTENSIONS_ATTRIBUTE == name) {
            if (value == null) {
                throw illegalTraitValue(name, value);
            }
            setRequiredExtensions(parseStringArrayTrait(name, value, " "));
        } else if (SVGConstants.SVG_SYSTEM_LANGUAGE_ATTRIBUTE == name) {
            if (value == null) {
                throw illegalTraitValue(name, value);
            }
            setSystemLanguage(parseStringArrayTrait(name, value, ","));
        } else {
            // The trait is not handled by this element as a string.
            // There are two situations. If this trait is supported 
            // by the element, then it means that the String type is 
            // not supported for the trait, and the following throws an
            // exception. Otherwise, this means the trait is just unknown,
            // and it goes into the generic trait map.
            if (supportsTrait(name)) {
                throw unsupportedTraitType(name, TRAIT_TYPE_STRING);
            } else {
                if (name == null) {
                    throw unsupportedTrait(name);
                } 

                if (value == null) {
                    throw illegalTraitValue(name, value);
                }

                ownerDocument.setUnknownTraitsNS(this, NULL_NS, name, value);
            }
        }
    }

    /**
     * Adds a new attribute. 
     * 
     * @param namespaceURI - the namespace URI of the attribute to create or 
     *        alter.
     * @param name - the local name of the attribute to create or alter.
     * @param value - the value to set in string form.
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     *         value is an invalid value for the given trait or null.
     * @throws DOMException with error code NO_MODIFICATION_ALLOWED_ERR: if
     *         attempt is made to change readonly trait.
     */
    public final void setAttributeNS(String namespaceURI,
                                 String name,
                                 String value) throws DOMException {
        if (namespaceURI == null || namespaceURI.length() == 0) {
            setAttribute(name, value);
            return;
        }

        checkNCName(name);

        if (value == null) {
            throw illegalTraitValue(name, namespaceURI, value);
        }

        namespaceURI = namespaceURI.intern();
        name = name.intern();

        setTraitNSImpl(namespaceURI, name, value);
    }

    /**
     * Returns the requested attribute in the specified namespace.
     * 
     * @param namespaceURI - the namespace URI of the attribute to create or 
     *        alter.
     * @param name - the local name of the attribute to create or alter.
     * @return the attribute value as a string, or the empty string if the 
     *         attribute was not specified.
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     *         value is an invalid value for the given trait or null.
     */
    public final String getAttributeNS(String namespaceURI,
            String name) throws DOMException {
        if (namespaceURI == null || namespaceURI.length() == 0) {
            return getAttribute(name);
        }

        checkNCName(name);

        namespaceURI = namespaceURI.intern();
        name = name.intern();

        return getTraitNSImpl(namespaceURI, name);
    }


    /*
     * Same as {@link org.w3c.dom.svg.SVGElement#setTrait setTrait}, but for
     * namespaced traits. Parameter name must be a non-qualified trait name,
     * i.e. without prefix.
     *
     * @param namespaceURI the trait's namespace.
     * @param name the trait's local name (un-prefixed).
     * @param value the trait's value.
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

    public final void setTraitNS(String namespaceURI, 
                                 String name, 
                                 String value)
        throws DOMException {
        if (namespaceURI == null || namespaceURI.length() == 0) {
            setTrait(name, value);
            return;
        }

        namespaceURI = intern(namespaceURI);
        name = intern(name);

        if (!supportsTraitNS(namespaceURI, name)) {
            throw unsupportedTraitNS(name, namespaceURI);
        }

        StringTraitAnim anim = 
            (StringTraitAnim) getTraitAnimNS(namespaceURI, name);

        if (anim == null || !anim.active) {
            setTraitNSImpl(namespaceURI, name, value);
        } else {
            anim.setTrait(value, TRAIT_TYPE_STRING);
        }
    }

    /**
     * Same as {@link org.w3c.dom.svg.SVGElement#setTrait setTrait}, but for
     * namespaced traits. Parameter name must be a non-qualified trait name,
     * i.e. without prefix.
     *
     * @param namespaceURI the trait's namespace.
     * @param name the trait's local name (un-prefixed).
     * @param value the trait's value.
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
    public void setTraitNSImpl(final String namespaceURI, 
                               final String name, 
                               final String value)
        throws DOMException {
        if (SVGConstants.PERSEUS_NAMESPACE_URI == namespaceURI) {
            if (SVGConstants.PERSEUS_CHILDREN_REQUIRED_ATTRIBUTE == name) {
                if (SVGConstants.SVG_TRUE_VALUE.equals(value)) {
                    setPreferedPaintNeedsLoad(true);
                    return;
                } else if (SVGConstants.SVG_FALSE_VALUE.equals(value)) {
                    setPreferedPaintNeedsLoad(false);
                    return;
                } else {
                    throw illegalTraitValue(namespaceURI, name, value);
                }
            }
        } else if (SVGConstants.XML_NAMESPACE_URI == namespaceURI) {
            if (SVGConstants.XML_BASE_ATTRIBUTE_LOCAL_NAME == name) {
                if (value == null) {
                    throw illegalTraitValue(name, value);
                }
                setURIBase(value);
            } else if (SVGConstants.XML_SPACE_ATTRIBUTE_LOCAL_NAME == name) {
                if (SVGConstants.XML_DEFAULT_VALUE.equals(value) 
                    || 
                    (value != null && value.length() == 0)) {
                    setXMLSpace(XML_SPACE_DEFAULT);
                    return;
                } else if (SVGConstants.XML_PRESERVE_VALUE.equals(value)) {
                    setXMLSpace(XML_SPACE_PRESERVE);
                    return;
                } else {
                    throw illegalTraitValue(name, value);
                }
            }
        }

        // The trait is not handled by this element as a string.
        // There are two situations. If this trait is supported 
        // by the element, then it means that the String type is 
        // not supported for the trait which should never happen
        // because all traits have to be supported as a string.
        // So that causes an internal error.
        // Otherwise, this means the trait is just unknown,
        // and it goes into the generic trait map.
        if (supportsTraitNS(name, value)) {
            throw new Error();
        } 
          
        // Trait is unknown, treat as a generic string trait.
        ownerDocument.setUnknownTraitsNS(this, namespaceURI, name, value);
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
    public final void setFloatTrait(String name, float value)
        throws DOMException {
        name = intern(name);

        if (!supportsTrait(name)) {
            throw unsupportedTrait(name);
        }

        TraitAnim anim = (TraitAnim) getTraitAnimNS(NULL_NS, name);
        if (anim == null || !anim.active) {
            setFloatTraitImpl(name, value);
        } else {
            anim.setTrait(Float.toString(value), TRAIT_TYPE_FLOAT);
        }
    }

    /**
     * Converts the input float array value to a String value.
     *
     * @param traitName the name of the trait to convert.
     * @param value the float trait value to convert.
     */
    String toStringTrait(final String traitName, final float[][] value) {
        throw new Error(traitName);
    }

    /**
     * Conversts the input PaintServer value to a String trait value.
     *
     * @param value the PaintServer value to convert.
     */
    String toString(final PaintServer paintServer) {
        if (paintServer == null) {
            return SVGConstants.CSS_NONE_VALUE;
        }

        return paintServer.toString();
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
        throw new Error(name);
    }

    /**
     * Set the trait value as a float.
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
    void setFloatTraitImpl(final String name, final float value) {
        throw unsupportedTraitType(name, TRAIT_TYPE_FLOAT);
    }

    /**
     * Set the trait value as {@link org.w3c.dom.svg.SVGMatrix SVGMatrix}.
     * Values in SVGMarix are copied in the trait so subsequent changes to the
     * givenSVGMatrix have no effect on the value of the trait.
     *
     * @param name name of trait to set
     * @param matrix SVGMatrix value of trait
     *
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
    public final void setMatrixTrait(String name, final SVGMatrix matrix) 
        throws DOMException {
        name = intern(name);

        if (!supportsTrait(name)) {
            throw unsupportedTrait(name);
        }

        if (matrix == null) {
            throw illegalTraitValue(name, null);
        }

        TraitAnim anim = (TraitAnim) getTraitAnimNS(NULL_NS, name);
        if (anim == null || !anim.active) {
            setMatrixTraitImpl(name, new Transform(matrix));
        } else {
            anim.setTrait(toStringTrait((Transform) matrix), 
                          TRAIT_TYPE_SVG_MATRIX);
        }
    }

    /**
     * Set the trait value as {@link org.w3c.dom.svg.SVGMatrix SVGMatrix}.
     * Values in SVGMarix are copied in the trait so subsequent changes to the
     * givenSVGMatrix have no effect on the value of the trait.
     *
     * @param name name of trait to set
     * @param matrix Transform value of trait
     *
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
    void setMatrixTraitImpl(final String name, final Transform matrix) 
        throws DOMException {
        throw unsupportedTraitType(name, TRAIT_TYPE_SVG_MATRIX);
    }

    /**
     * Set the trait value as {@link org.w3c.dom.svg.SVGRect SVGRect}. Values in
     * SVGRect are copied in the trait so subsequent changes to the given
     * SVGRect have no effect on the value of the trait.
     *
     * @param name the trait name.
     * @param rect the trait value, as an <code>SVGRect</code> object.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if the requested
     * trait's value cannot be specified as an {@link org.w3c.dom.svg.SVGRect
     * SVGRect}
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is an invalid value for the given trait or null.  SVGRect is
     * invalid if the width or height values are set to negative.
     */
    public final void setRectTrait(String name, final SVGRect rect)
        throws DOMException {
        name = intern(name);

        if (!supportsTrait(name)) {
            throw unsupportedTrait(name);
        }
        
        TraitAnim anim = (TraitAnim) getTraitAnimNS(NULL_NS, name);
        if (anim == null || !anim.active) {
            setRectTraitImpl(name, rect);
        } else {
            if (rect == null) {
                throw illegalTraitValue(name, null);
            }
            
            float[] vb = 
                {rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight()};
            
            anim.setTrait(toStringTrait(vb), TRAIT_TYPE_SVG_RECT);
        }
    }

    /**
     * Set the trait value as {@link org.w3c.dom.svg.SVGRect SVGRect}. Values in
     * SVGRect are copied in the trait so subsequent changes to the given
     * SVGRect have no effect on the value of the trait.
     *
     * @param name the trait name.
     * @param rect the trait value, as an <code>SVGRect</code> object.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if the requested
     * trait's value cannot be specified as an {@link org.w3c.dom.svg.SVGRect
     * SVGRect}
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is an invalid value for the given trait or null.  SVGRect is
     * invalid if the width or height values are set to negative.
     */
    public void setRectTraitImpl(final String name, final SVGRect rect) 
        throws DOMException {
        throw unsupportedTraitType(name, TRAIT_TYPE_SVG_RECT);
    }

    /**
     * Set the trait value as {@link org.w3c.dom.svg.SVGPath SVGPath}. Values in
     * SVGPath are copied in the trait so subsequent changes to the given
     * SVGPath have no effect on the value of the trait.
     *
     * @param name the trait name.
     * @param path the trait value, as an <code>SVGPath</code> object.
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
    public final void setPathTrait(String name, final SVGPath path)
        throws DOMException {
        name = intern(name);

        if (!supportsTrait(name)) {
            throw unsupportedTrait(name);
        }

        if (path == null) {
            throw illegalTraitValue(name, null);
        }

        TraitAnim anim = (TraitAnim) getTraitAnimNS(NULL_NS, name);
        if (anim == null || !anim.active) {
            setPathTraitImpl(name, path);
        } else {
            if (path == null) {
                throw illegalTraitValue(name, null);
            }
            anim.setTrait(((Path) path).toString(), 
                          TRAIT_TYPE_SVG_PATH);
        }
    }

    /**
     * Set the trait value as {@link org.w3c.dom.svg.SVGPath SVGPath}. Values in
     * SVGPath are copied in the trait so subsequent changes to the given
     * SVGPath have no effect on the value of the trait.
     *
     * @param name the trait name.
     * @param path the trait value, as an <code>SVGPath</code> object.
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
        throw unsupportedTraitType(name, TRAIT_TYPE_SVG_PATH);
    }

    /**
     * Set the trait value as {@link org.w3c.dom.svg.SVGRGBColor SVGRGBColor}.
     *
     * @param name the trait name.
     * @param color the trait value, as a color.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if the requested
     * trait's value cannot be specified as an {@link
     * org.w3c.dom.svg.SVGRGBColor SVGRGBColor}
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is null.
     */
    public final void setRGBColorTrait(String name, final SVGRGBColor color)
        throws DOMException {
        name = intern(name);

        if (!supportsTrait(name)) {
            throw unsupportedTrait(name);
        }

        if (color == null) {
            throw illegalTraitValue(name, null);
        }

        TraitAnim anim = (TraitAnim) getTraitAnimNS(NULL_NS, name);
        if (anim == null || !anim.active) {
            setRGBColorTraitImpl(name, color);
        } else {
            anim.setTrait(color.toString(),
                          TRAIT_TYPE_SVG_RGB_COLOR);
        }
 
    }

    /**
     * Set the trait value as {@link org.w3c.dom.svg.SVGRGBColor SVGRGBColor}.
     *
     * @param name the trait name.
     * @param color the trait value, as a color.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if the requested
     * trait's value cannot be specified as an {@link
     * org.w3c.dom.svg.SVGRGBColor SVGRGBColor}
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is null.
     */
    void setRGBColorTraitImpl(final String name, final SVGRGBColor color)
        throws DOMException {
        throw unsupportedTraitType(name, TRAIT_TYPE_SVG_RGB_COLOR);
    }

    /**
     * Parses the input value and converts it to a String array.
     *
     * @param value the value to convert to a String array
     * @param name the name of the trait being converted.
     * @param seperators the string of characters which are seperators 
     *        between array values.
     */
    protected final String[] parseStringArrayTrait(final String name,
                                                   final String value,
                                                   final String seperators) 
        throws DOMException {
        // Don't accept null trait values.
        if (value == null) {
            throw illegalTraitValue(name, value);
        }

        SimpleTokenizer st = new SimpleTokenizer(value, seperators);
        int n = st.countTokens();
        String[] result = new String[n];
        for (int i = 0; i < n; i++) {
            result[i] = st.nextToken().trim().intern();
        }

        return result;        
    }

    /**
     * Parses the input value and converts it to a float value.
     *
     * @param name the name of the trait to convert to a float.
     * @param value the value to convert to a float.
     * @return the value converted to a float value.
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is an invalid float value or null.
     */
    protected final float parseFloatTrait(
            final String name, 
            final String value) throws DOMException {
        try {
            return ownerDocument.lengthParser.parseNumber(value);
        } catch (IllegalArgumentException iae) {
            throw illegalTraitValue(name, value);
        }
    }

    /**
     * Parses the input value and converts it to a float array value.
     *
     * @param name the name of the trait to convert to a float array.
     * @param value the value to convert to a float.
     * @return the value converted to a float array value.
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is an invalid float value or null.
     */
    protected final float[] parsePositiveFloatArrayTrait(
            final String name, 
            final String value) throws DOMException {
        try {
            SimpleTokenizer st = new SimpleTokenizer(value, ", ");
            float[] da = null;
            int n = st.countTokens();
            float totalLength = 0;
            
            if ((n % 2) == 0) {
                da = new float[n];
                for (int i = 0; i < da.length; i++) {
                    da[i] = ownerDocument
                        .lengthParser.parseNumber(st.nextToken());
                    
                    if (Float.isNaN(da[i]) || da[i] < 0) {
                        // The CSS number was invalid. 
                        // Do not set the value
                        throw new IllegalArgumentException();
                    }
                    
                    totalLength += da[i];
                }
            } else {
                da = new float[2 * n];
                for (int i = 0; i < n; i++) {
                    da[i] = ownerDocument
                        .lengthParser.parseNumber(st.nextToken());
                    da[n + i] = da[i];
                    
                    if (Float.isNaN(da[i]) || da[i] < 0) {
                        // The CSS number was invalid. 
                        // Do not set the value
                        throw new IllegalArgumentException();
                    }
                    
                    totalLength += da[i];
                }
            }
            
            if (totalLength > 0) {
                return da;
            } else {
                return null;
            }
        } catch (IllegalArgumentException iae) {
            throw illegalTraitValue(name, value);
        } catch (NullPointerException iae) {
            throw illegalTraitValue(name, value);
        }
    }

    /**
     * Parses the input value and converts it to a positive float value.
     *
     * @param name the trait name.
     * @param value the value to convert to a float.
     * @return the value converted to a float value.
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is an invalid positive float value or null.
     */
    protected final float parsePositiveFloatTrait(
            final String name,
            final String value) throws DOMException {
        try {
            float v = ownerDocument.lengthParser.parseNumber(value);
            if (v < 0) {
                throw new IllegalArgumentException();
            }
            return v;
        } catch (IllegalArgumentException iae) {
            throw illegalTraitValue(name, value);
        }
    }

    /**
     * Parses the input value and converts it to a float.
     *
     * @param name the trait name.
     * @param value the value to convert to a float.
     * @param isHorizontal controls whether this is a horizontal length or not.
     * @return the value converted to a float.
     * @throws DOMException if the trait value is invalid.
     */
    protected final float parseLengthTrait(
            final String name,
            final String value,
            boolean isHorizontal) throws DOMException {
        try {
            Length l = ownerDocument.lengthParser.parseLength(value);
            switch (l.unit) {
            case Length.SVG_LENGTHTYPE_NUMBER:
                return l.value;
            case Length.SVG_LENGTHTYPE_IN:
                return (l.value * 25.4f / ownerDocument.getPixelMMSize());
            case Length.SVG_LENGTHTYPE_CM:
                return (l.value * 10f / ownerDocument.getPixelMMSize());
            case Length.SVG_LENGTHTYPE_MM:
                return (l.value / ownerDocument.getPixelMMSize());
            case Length.SVG_LENGTHTYPE_PT:
                return (l.value * 25.4f / 
                        (72f * ownerDocument.getPixelMMSize()));
            case Length.SVG_LENGTHTYPE_PC:
                return (l.value * 25.4f / 
                        (6f * ownerDocument.getPixelMMSize()));
            case Length.SVG_LENGTHTYPE_PERCENTAGE:
                if (isHorizontal) {
                    return ownerDocument.width * l.value / 100f;
                } else {
                    return ownerDocument.height * l.value / 100f;
                } 
            default:
                // This should never happen.
                throw new Error();
            }
        } catch (IllegalArgumentException iae) {
            throw illegalTraitValue(name, value);
        }
    }

    /**
     * Parses the input value and converts it to a float.
     * @param name the trait name.
     * @param value the value to convert to a float.
     * @param isHorizontal controls whether this is a horizontal length or not.
     * @return the value converted to a float.
     * @throws DOMException if the trait value is invalid.
     */
    protected final float parsePositiveLengthTrait(
            final String name,
            final String value,
            final boolean isHorizontal) throws DOMException {
        float v = parseLengthTrait(name, value, isHorizontal);
        if (v < 0) {
            throw illegalTraitValue(name, value);
        }
        return v;
    }

    /**
     * Parses the input value and converts it to a Path value.
     *
     * @param name the trait name.
     * @param value the value to convert.
     * @throws DOMException if the input value is invalid.
     */
    protected final Path parsePathTrait(final String name,
                                        final String value) 
        throws DOMException {
        try {
            return ownerDocument.pathParser.parsePath(value);
        } catch (IllegalArgumentException iae) {
            DOMException de = illegalTraitValue(name, value);
            if (!loaded) {
                ownerDocument.setDelayedException(de);
                return ownerDocument.pathParser.getPath();
            } else {
                throw de;
            }
        }
    }

    /**
     * Parses the input points value and converts it to a Path value.
     *
     * @param name the trait name.
     * @param value the value to convert.
     * @throws DOMException if the input value is invalid.
     */
    protected final Path parsePointsTrait(final String name,
                                          final String value) 
        throws DOMException {
        try {
            return ownerDocument.pathParser.parsePoints(value);
        } catch (IllegalArgumentException iae) {
            DOMException de = illegalTraitValue(name, value);
            if (!loaded) {
                ownerDocument.setDelayedException(de);
                return ownerDocument.pathParser.getPath();
            } else {
                throw de;
            }
        }
    }

    /**
     * Parses the input value and converts it to an Transform value.
     *
     * @param name the trait's name.
     * @param value the value to convert to a transform.
     * @return the value converted to an Transform object.
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is an invalid transform trait value.
     */
    protected final Transform parseTransformTrait(final String name,
                                                        final String value) 
        throws DOMException {
        try {
            return ownerDocument.transformListParser.parseTransformList(value);
        } catch (IllegalArgumentException iae) {
            throw illegalTraitValue(name, value);
        }
    }

    /**
     * Parses the input value and converts it to an RGB value
     *
     * @param traitName the name of the color trait being parsed.
     * @param value the value to convert to an RGB
     * @return the value converted to a RGB object
     *
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is an invalid color trait value.
     */
    protected final RGB parseColorTrait(
            final String traitName, 
            final String value) throws DOMException {
        try {
            return ownerDocument.colorParser.parseColor(value);
        } catch (IllegalArgumentException e) {
            throw illegalTraitValue(traitName, value);
        }
    }

    /**
     * Parses the input value and converts it to a Paint value
     *
     * @param traitName the name of the color trait being parsed.
     * @param paintTarget the PaintTarget requesting the PaintServer.
     * @param value the value to convert to a Paint
     * @return the value converted to a Paint object
     *
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is an invalid color trait value.
     */
    protected final PaintServer parsePaintTrait(
            final String traitName, 
            final PaintTarget paintTarget,
            final String value) throws DOMException {
        if (value == null) {
            throw illegalTraitValue(traitName, value);
        }

        if (value.startsWith("url(#")) {
            if (value.length() < 7 
                || 
                value.charAt(value.length() - 1) != ')') {
                throw illegalTraitValue(traitName, value);
            }
            
            String idRef = value.substring(5, value.length() - 1);

            return PaintServerReference.resolve(ownerDocument,
                                                paintTarget,
                                                traitName,
                                                idRef);
        } else {
            try {
                return ownerDocument.colorParser.parseColor(value);
            } catch (IllegalArgumentException e) {
                throw illegalTraitValue(traitName, value);
            }
        } 
    }

    /**
     * Parses the input value and converts it to a Time value.
     *
     * @param traitName the name of the clock trait being parsed.
     * @param value the value to convert to a Time instance.
     * @return the value converted to a Time object.
     * @throws DOMException if the input value is invalid.
     */
    protected final Time parseClockTrait(
            final String traitName,
            final String value) throws DOMException {
        if (SVGConstants.SVG_INDEFINITE_VALUE.equals(value)) {
            return Time.INDEFINITE;
        }

        try {
            return new Time(ownerDocument.clockParser.parseClock(value));
        } catch (IllegalArgumentException iae) {
            throw illegalTraitValue(traitName, value);
        }
    }

    /**
     * Parses the input value and converts it to a Time value. If there
     * is a syntax error or if the value is invalid for the given usage
     * (has to be [0, infinite[ for min, and ]0, infinite[ for max,
     * then the default is used. For min, the default is 0. For max, the 
     * default is 'indefinite'
     *
     * @param traitName the name of the clock trait being parsed.
     * @param value the value to convert to a Time instance.
     * @param isMin should 
     * @return the value converted to a Time object.
     * @throws DOMException if the input value is invalid.
     */
    protected final Time parseMinMaxClock(
            final String traitName,
            final String value,
            final boolean isMin) throws DOMException {
        if (SVGConstants.SVG_INDEFINITE_VALUE.equals(value)) {
            return Time.INDEFINITE;
        }

        if (value == null) {
            throw illegalTraitValue(traitName, value);
        }

        try {
            long v = ownerDocument.clockParser.parseClock(value);
            if (v < 0) {
                throw new IllegalArgumentException();
            }

            if (v == 0 && !isMin) {
                throw new IllegalArgumentException();
            }

            return new Time(v);
        } catch (IllegalArgumentException iae) {
            if (isMin) {
                return new Time(0);
            }
            return Time.INDEFINITE;
        }
    }

    /**
     * Utility method. This should be used for XML attribute values converted
     * to float arrays.
     *
     * @param traitName the name of the trait to parse
     * @param ctx the build context
     * @return the attribute value, converted to float
     * @throws DOMException if the value represents an invalid 
     *         floating point array value.
     */
    public final float[] parseFloatArrayTrait(final String traitName,
                                                     final String value) 
        throws DOMException {
        return parseFloatArrayTrait(traitName, value, ',');
    }

    /**
     * Utility method. This should be used for XML attribute values converted
     * to float arrays.
     *
     * @param traitName the name of the trait to parse
     * @param value the value to parse
     * @param sep the number separator in the input value list of numbers.
     * @return the attribute value, converted to float
     * @throws DOMException if the value represents an invalid 
     *         floating point array value.
     */
    public final float[] parseFloatArrayTrait(final String traitName,
                                              final String value,
                                              final char sep) 
        throws DOMException {
        try {
            return ownerDocument.numberListParser.parseNumberList(value, sep);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw illegalTraitValue(traitName, value);
        }
    }

    /**
     * CSS 2 Specification (section 15.3.2) and SVG 1.1 specification
     * (20.8.3):
     *
     * all | [ normal | italic | oblique ] [, [normal | italic | oblique] ]*
     *
     * @param name the name of the trait to parse
     * @param value the trait value
     * @return the font style in the FontFace.FONT_STYLE_XXX value set.
     * @throws DOMException if the value is not a legal one for this trait.
     */
    public final int parseFontStylesTrait(final String name,
                                          final String value) {
        if (value == null) {
            throw illegalTraitValue(name, value);
        }

        if (SVGConstants.CSS_ALL_VALUE.equals(value)) {
            return FontFace.FONT_STYLE_ALL;
        }

        SimpleTokenizer st = new SimpleTokenizer(value, SVGConstants.COMMA_STR);

        if (st.countTokens() < 1) {
            throw illegalTraitValue(name, value);
        }

        int styles = 0;
        while (st.hasMoreTokens()) {
            String t = st.nextToken().trim();
            if (SVGConstants.CSS_NORMAL_VALUE.equals(t)) {
                styles |= TextNode.FONT_STYLE_NORMAL;
            } else if (SVGConstants.CSS_ITALIC_VALUE.equals(t)) {
                styles |= TextNode.FONT_STYLE_ITALIC;
            } else if (SVGConstants.CSS_OBLIQUE_VALUE.equals(t)) {
                styles |= TextNode.FONT_STYLE_OBLIQUE;
            } else {
                throw illegalTraitValue(name, value);
            }
        }

        return styles;
    }

    /**
     * CSS 2 specification ((section 15.3.2) and SVG 1.1 specification
     * (20.8.3):
     *
     * all | [normal | bold |100 | 200 | 300 | 400 | 500 | 600 | 
     * 700 | 800 | 900] [, [normal | bold |100 | 200 | 300 |
     * 400 | 500 | 600 | 700 | 800 | 900]]*
     *
     * @param name the name of the trait to parse
     * @param value the trait value.
     * @return the font weight as an int value in the 
     *         FontFace.FONT_WEIGHT_XXX set.
     * @throws DOMException if the value is not a legal one for this trait.
     */
    protected final int parseFontWeightsTrait(final String name,
                                              final String value) {
        if (value == null) {
            throw illegalTraitValue(name, value);
        }

        if (SVGConstants.CSS_ALL_VALUE.equals(value)) {
            return FontFace.FONT_WEIGHT_ALL;
        }

        SimpleTokenizer st = new SimpleTokenizer(value, SVGConstants.COMMA_STR);

        if (st.countTokens() < 1) {
            throw illegalTraitValue(name, value);
        }

        int weights = 0;
        while (st.hasMoreTokens()) {
            String t = st.nextToken().trim();
            if (SVGConstants.CSS_NORMAL_VALUE.equals(t)) {
                weights |= TextNode.FONT_WEIGHT_NORMAL;
            } else if (SVGConstants.CSS_BOLD_VALUE.equals(t)) {
                weights |= TextNode.FONT_WEIGHT_BOLD;
            } else if (SVGConstants.CSS_100_VALUE.equals(t)) {
                weights |= TextNode.FONT_WEIGHT_100;
            } else if (SVGConstants.CSS_200_VALUE.equals(t)) {
                weights |= TextNode.FONT_WEIGHT_200;
            } else if (SVGConstants.CSS_300_VALUE.equals(t)) {
                weights |= TextNode.FONT_WEIGHT_300;
            } else if (SVGConstants.CSS_400_VALUE.equals(t)) {
                weights |= TextNode.FONT_WEIGHT_400;
            } else if (SVGConstants.CSS_500_VALUE.equals(t)) {
                weights |= TextNode.FONT_WEIGHT_500;
            } else if (SVGConstants.CSS_600_VALUE.equals(t)) {
                weights |= TextNode.FONT_WEIGHT_600;
            } else if (SVGConstants.CSS_700_VALUE.equals(t)) {
                weights |= TextNode.FONT_WEIGHT_700;
            } else if (SVGConstants.CSS_800_VALUE.equals(t)) {
                weights |= TextNode.FONT_WEIGHT_800;
            } else if (SVGConstants.CSS_900_VALUE.equals(t)) {
                weights |= TextNode.FONT_WEIGHT_900;
            } else {
                throw illegalTraitValue(name, value);
            }
        }

        return weights;
    }

    /**
     * Parses the input value assuming it has the folloing syntax:
     * Value:   [ <family-name> | <generic-family> ] [, [<family-name> |
     *          <generic-family> ]]*
     * @param name the name of the trait being parsed.
     * @param value the font family value to be parsed. Should not be null.
     * @return an array of font-family strings
     */
    public String[] parseFontFamilyTrait(final String name,
                                         final String value) {
        if (value == null) {
            throw illegalTraitValue(name, value);
        }

        SimpleTokenizer st = new SimpleTokenizer(value, SVGConstants.COMMA_STR);
        String[] fontFamily = new String[st.countTokens()];
        int i = 0;
        while (st.hasMoreTokens()) {
            fontFamily[i] = st.nextToken();
            
            // Remove leading and trailing white spaces
            fontFamily[i] = fontFamily[i].trim();
            
            //
            // Now, take care of quotes
            //
            
            // <!> NOTE
            //
            // According to the CSS spec., if font family values are not 
            // quoted, then the spaces should be consolidated. The following
            // code does not do that. 
            //
            if (fontFamily[i].length() > 0) {
                if (fontFamily[i].charAt(0) == '\'') {
                    // If there is a trailing quote, remove the quotes
                    if (fontFamily[i].charAt(fontFamily[i].length() - 1) 
                        == 
                        '\'') {
                        fontFamily[i] = 
                            fontFamily[i].substring(1,
                                                    fontFamily[i].length() - 1);
                    }
                }
            } 
            i++;
        }

        return fontFamily;
    }

    /**
     * Parses the input value, assuming a unicode range syntax, as for the
     * <code>&lt;hkern&gt;</code> element's u1 and u2 attributes.
     *
     * @param name the trait name.
     * @param value the trait value.
     * @return an array of unicode range pairs, as integer pairs.
     * @throws DOMException if the input trait value is invalid.
     */
    protected final int[][] parseUnicodeRangeTrait(final String name,
                                                   final String value)
        throws DOMException {
        try {
            return ownerDocument.unicodeParser.parseUnicode(value);
        } catch (IllegalArgumentException iae) {
            throw illegalTraitValue(name, value);
        }
    }

    /**
     * Throws a DOMException if the element is not loading, i.e., if
     * its loaded field is set to true.
     *
     * @param name the name of the trait that is accessed.
     */
    protected void checkWriteLoading(final String name) throws DOMException {
        if (loaded && isInDocumentTree()) {
            throw readOnlyTraitError(name);
        }
    }

    /**
     * Throws a DOMException if the input float trait value is
     * strictly negative.
     *
     * @param name the trait name.
     * @param value the trait float value.
     */
    protected void checkPositive(final String name,
                                 final float value) {
        if (value < 0) {
            throw illegalTraitValue(name, Float.toString(value));
        }
    }

    /**
     * @param name the trait name.
     * @return a DOMException describing the type mismatch error.
     */
    protected DOMException unsupportedTraitType(final String name,
                                                final String type) {
        if (name == null) {
            return unsupportedTrait(name);
        }

        return new DOMException(DOMException.TYPE_MISMATCH_ERR,
                                Messages.formatMessage
                                (Messages.ERROR_TRAIT_TYPE_MISMATCH,
                                 new String[] {name,
                                               type,
                                               getLocalName(),
                                               getNamespaceURI()}));
    }

    /**
     * @param name the trait name.
     * @return a DOMException describing the type mismatch error.
     */
    protected DOMException unsupportedTraitTypeNS(final String name,
                                                  final String namespaceURI,
                                                  final String type) {
        return new DOMException(DOMException.TYPE_MISMATCH_ERR,
                                Messages.formatMessage
                                (Messages.ERROR_TRAIT_TYPE_NS_MISMATCH,
                                 new String[] {name,
                                               namespaceURI,
                                               type,
                                               getLocalName(),
                                               getNamespaceURI()}));
    }

    /**
     * @param name the trait name
     * @return a DOMException describing the unsupported trait error.
     */
    protected DOMException unsupportedTrait(final String name) {
        return new DOMException(DOMException.NOT_SUPPORTED_ERR,
                                Messages.formatMessage
                                (Messages.ERROR_UNSUPPORTED_TRAIT,
                                 new String[] {name,
                                               null,
                                               getLocalName(),
                                               getNamespaceURI()}));
    }
                                
    /**
     * @param name the trait name
     * @param namespaceURI the trait's namespace URI.
     * @return a DOMException describing the unsupported trait error.
     */
    protected DOMException unsupportedTraitNS(final String name,
                                              final String namespaceURI) {
        return new DOMException(DOMException.NOT_SUPPORTED_ERR,
                                Messages.formatMessage
                                (Messages.ERROR_UNSUPPORTED_TRAIT,
                                 new String[] {name,
                                               namespaceURI,
                                               getLocalName(),
                                               getNamespaceURI()}));
    }
                                
    /**
     * @param name the name of the trait
     * @param value the illegal value.
     * @return a DOMException describing an illegal trait value
     */
    DOMException illegalTraitValue(final String name,
                                   final String value) {
        return new DOMException(
                DOMException.INVALID_ACCESS_ERR,
                Messages.formatMessage(
                    Messages.ERROR_INVALID_TRAIT_VALUE,
                    new String[] {
                        name,
                        value,
                        getLocalName() + "(" + getId() + ")",
                        getNamespaceURI()
                    }));
    }

    /**
     * @param name the name of the trait
     * @param value the illegal value.
     * @return a DOMException describing an illegal trait value
     */
    DOMException notAnimatable(final String traitNamespace,
                               final String traitName) {
        return new DOMException(DOMException.NOT_SUPPORTED_ERR,
                                Messages.formatMessage
                                (Messages.ERROR_TRAIT_NOT_ANIMATABLE,
                                 new String[] {traitNamespace,
                                               traitName,
                                               getLocalName(),
                                               getNamespaceURI()}));
    }

    /**
     * @param targetId the target element's id (may be null)
     * @param traitNamespace the animated trait's namespace.
     * @param traitName the animated trait's name.
     * @param targetNamespace the target element's namespace.
     * @param targetName the target element's name
     * @param animationId the animation id (may be null)
     * @param animationNamespace the animation's namespace
     * @param animationLocalName the animation's local name.
     * @param errorDescription the animation error's description.
     */
    protected DOMException animationError(final String targetId,
                                          final String traitNamespace,
                                          final String traitName,
                                          final String targetNamespace,
                                          final String targetName,
                                          final String animationId,
                                          final String animationNamespace,
                                          final String animationLocalName,
                                          final String errorDescription) {
        return new DOMException(DOMException.INVALID_STATE_ERR,
                                Messages.formatMessage
                                (Messages.ERROR_INVALID_ANIMATION_CONFIGURATION,
                                 new String[] {targetId,
                                               traitNamespace,
                                               traitName,
                                               targetNamespace,
                                               targetName,
                                               animationId,
                                               animationNamespace,
                                               animationLocalName,
                                               animationLocalName,
                                               errorDescription}));
    }

    /**
     * @param name the name of the trait
     * @param namespaceURI the trait's namespace URI.
     * @param value the illegal value.
     * @return a DOMException describing an illegal trait value
     */
    protected DOMException illegalTraitValue(final String name,
                                             final String namespaceURI,
                                             final String value) {
        return illegalTraitValue(name + "(" + namespaceURI + ")",
                                 value);
    }

    /**
     * @param name the trait name.
     * @return DOMException describing the write error on a read-only attribute.
     */
    protected DOMException readOnlyTraitError(final String name) {
        return new DOMException(DOMException.NO_MODIFICATION_ALLOWED_ERR,
                                Messages.formatMessage
                                (Messages.ERROR_READ_ONLY_TRAIT,
                                 new String[] {name,
                                               getLocalName(),
                                               getNamespaceURI()}));
    }

    /**
     * Converts an animated float[][] value into an AWT color value.
     *
     * @param name the trait's name.
     * @param value the float[][] to convert.
     * @return the color converted to an AWT <code>Color</code> object.
     */
    protected RGB toRGB(final String name,
                        final float[][] v) throws DOMException {
        if (v != null) {
            return new RGB((int) v[0][0], (int) v[0][1], (int) v[0][2]);
        } else {
            return null;
        }
    }
     
    /**
     * Converts an animated float[][] value into an String rgb value.
     *
     * @param name the trait's name.
     * @param value the float[][] to convert.
     * @return the color converted to a string.
     */
    protected String toRGBString(final String name,
                                 final float[][] v) throws DOMException {
        if (v != null) {
            return "rgb(" + ((int) v[0][0]) + "," + ((int) v[0][1]) + "," 
                    + ((int) v[0][2]) + ")";
        } else {
            return "none";
        }
    }
     
    /**
     * Converts an J2D RGB to an SVG DOM RGBColor
     *
     * @param traitName the name of the trait whose value is convereted.
     * @param paint the PaintServer to convert. Should not be null.
     * @return the color, converted to an <code>SVGRGBColor</code> instance.
     *
     * @throws DOMException with error code TYPE_MISMATCH_ERR if the 
     *         paint is not an instance of Colorl
     */
    protected SVGRGBColor toSVGRGBColor(final String traitName, 
                                        final PaintServer paint) {
        if (paint == null) {
            return null;
        }
        
        if (!(paint instanceof SVGRGBColor)) {
            throw unsupportedTraitType(traitName, TRAIT_TYPE_SVG_RGB_COLOR);
        }

        return (SVGRGBColor) paint;
    }

    /**
     * Converts a viewBox array to an SVGRect value.
     *
     * @param name the name of the trait.
     * @param viewBox the viewbox value to convet.
     */
    protected SVGRect toSVGRect(final float[][] viewBox) {
        if (viewBox == null) {
            return null;
        } 
        
        SVGRect r = new Box(viewBox[0][0],
                            viewBox[0][1],
                            viewBox[1][0],
                            viewBox[2][0]);

        return r;
    }

    /**
     * Converts an comma seperated list of floats to a viewBox array
     *
     * @param name the trait name
     * @param value the trait value to be converted to an SVGRect. 
     *        The expected syntax is 
     *        "float comma-wsp float comma-wsp float comma-wsp float comma-wsp"
     * @return an array of four floats.
     *
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is an invalid value for the given trait or null.
     */
    protected float[][] toViewBox(final String name,
                                  final String value) throws DOMException {
        if (value == null) {
            throw illegalTraitValue(name, value);
        }

        try {
            return ownerDocument.viewBoxParser.parseViewBox(value);
        } catch (IllegalArgumentException iae) {
            throw illegalTraitValue(name, value);
        }
    }

    /**
     * Converts a Java String array to a trait string array with the format:
     * "str1, str2, .., strN"
     *
     * @param array the string array to be converted.
     * @return a string with the value "" if the array is null or 
     *         "float1,float2,..,floatN"
     */
    protected String toStringTrait(final String[] array) {
        return toStringTrait(array, ",");
    }

    /**
     * Converts a Java String array to a trait string array with the format:
     * "str1, str2, .., strN"
     *
     * @param array the string array to be converted.
     * @param sep seperator to use in the output string array.
     *
     * @return a string with the value "" if the array is null or 
     *         "float1,float2,..,floatN"
     */
    protected String toStringTrait(final String[] array, final String sep) {
        if (array == null || array.length < 1) {
            return "";
        }

        StringBuffer sb = new StringBuffer();
        
        for (int i = 0; i < array.length - 1; i++) {
            sb.append(array[i]);
            sb.append(sep);
        }

        sb.append(array[array.length - 1]);
        return sb.toString();
    }
     
    /**
     * Converts a Java String array to a trait string array with the format:
     * "str1, str2, .., strN". In addition, the string values are put inside
     * single quotes if there are spaces in the string values. This is needed
     * for values such as the CSS 2 font-family attribute.
     *
     * @param array the string array to be converted.
     *
     * @return a string with the value "" if the array is null or 
     *         "float1,float2,..,floatN"
     */
    protected String toStringTraitQuote(final String[] array) {
        if (array == null || array.length < 1) {
            return "";
        }

        StringBuffer sb = new StringBuffer();
        
        for (int i = 0; i < array.length - 1; i++) {
            if (array[i].indexOf(' ') != -1) {
                sb.append('\'');
                sb.append(array[i]);
                sb.append('\'');
            } else {
                sb.append(array[i]);
            }
            sb.append(',');
        }

        if (array[array.length - 1].indexOf(' ') != -1) {
            sb.append('\'');            
            sb.append(array[array.length - 1]);
            sb.append('\'');            
        } else {
            sb.append(array[array.length - 1]);
        }

        return sb.toString();
    }
     

    /**
     * Helper method. Converts the input array value to a string trait value.
     *
     * @param array the float array to be converted.
     * @return a string with the value "none" if the array is null or 
     *         "float1,float2,..,floatN"
     */
    protected String toStringTrait(final float[] array) {
        return toStringTrait(array, ',');
    }


    /**
     * Helper method. Converts the input array value to a string trait value.
     *
     * @param array the float array to be converted.
     * @return a string with the value "none" if the array is null or 
     *         "float1,float2,..,floatN"
     */
    protected String toStringTrait(final float[] array, final char sep) {
        if (array == null) {
            return SVGConstants.CSS_NONE_VALUE;
        }

        StringBuffer sb = new StringBuffer();
        
        for (int i = 0; i < array.length - 1; i++) {
            sb.append(array[i]);
            sb.append(sep);
        }

        sb.append(array[array.length - 1]);
        return sb.toString();
    }

    /**
     * Helper method. Convertst the input array of float arrays into a 
     * string trait value.
     *
     * @param array the array of float arrays to convert.
     * @return a string the the value "" if the array is null or 
     *         "f01 f02 f03 f04; f11 f12 f13 f14; ...;fn1 fn2 fn3 fn4"
     * @throws NullPointerException if one of the array values is null
     * @throws ArrayIndexOutOfBoundsException if one of the array values is of
     *         length 0.
     */
    protected String toStringTrait(final float[][] array) {
        if (array == null || array.length == 0) {
            return "";
        }

        StringBuffer sb = new StringBuffer();
        
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length - 1; j++) {
                sb.append(array[i][j]);
                sb.append(SVGConstants.COMMA);
            }
            sb.append(array[i][array[i].length - 1]);
            sb.append(SVGConstants.SEMI_COLON);
        }
        
        String value = sb.toString();

        // Trim trailing ';'
        return value.substring(0, value.length() - 1);
    }

    /**
     * Helper method. Converts the input <code>Transform</code>
     * to an SVGMatrix trait value.
     *
     * @param transform the transform to convert.
     * @return the SVGMatrix equivalent value.
     */
    protected SVGMatrix toSVGMatrixTrait(final Transform transform) {
        if (transform == null) {
            return new Transform(1, 0, 0, 1, 0, 0);
        } else {
            return new Transform(transform);
        }
    }

    /**
     * Converts an align value to a preserveAspectRatio string trait
     * value.
     * 
     * @param align one of StructureNode.ALIGN_NONE, 
     *        StructureNode.ALIGN_XMIDYMID
     */
    protected static String alignToStringTrait(final int align) {
        switch (align) {
        case StructureNode.ALIGN_XMIDYMID:
            return SVGConstants.SVG_IMAGE_PRESERVE_ASPECT_RATIO_DEFAULT_VALUE;
        default:
            return SVGConstants.SVG_NONE_VALUE;
        }
    }

    /**
     * Converts a FontFace's font-styles to a String trait.
     *
     * @param styles the FontFace type styles value.
     */
    protected String fontStylesToStringTrait(final int styles) {
        if (styles == FontFace.FONT_STYLE_ALL) {
            return SVGConstants.CSS_ALL_VALUE;
        }

        StringBuffer sb = new StringBuffer();
        
        if ((styles & TextNode.FONT_STYLE_NORMAL) != 0) {
            sb.append(SVGConstants.CSS_NORMAL_VALUE);
            sb.append(SVGConstants.COMMA);
        }

        if ((styles & TextNode.FONT_STYLE_ITALIC) != 0) {
            sb.append(SVGConstants.CSS_ITALIC_VALUE);
            sb.append(SVGConstants.COMMA);
        }

        if ((styles & TextNode.FONT_STYLE_OBLIQUE) != 0) {
            sb.append(SVGConstants.CSS_OBLIQUE_VALUE);
            sb.append(SVGConstants.COMMA);
        }

        if (sb.length() > 0) {
            return sb.toString().substring(0, sb.length() - 1);
        }
        
        return sb.toString();
    }

    /**
     * Converts an FontFace's font-weights to a String trait.
     *
     * @param weight the FontFace type' weights value.
     */
    protected String fontWeightsToStringTrait(final int weight) {
        if (weight == FontFace.FONT_WEIGHT_ALL) {
            return SVGConstants.CSS_ALL_VALUE;
        }

        StringBuffer sb = new StringBuffer();
        if ((weight & TextNode.FONT_WEIGHT_100) != 0) {
            sb.append(SVGConstants.CSS_100_VALUE);
            sb.append(SVGConstants.COMMA);
        }

        if ((weight & TextNode.FONT_WEIGHT_200) != 0) {
            sb.append(SVGConstants.CSS_200_VALUE);
            sb.append(SVGConstants.COMMA);
        }

        if ((weight & TextNode.FONT_WEIGHT_300) != 0) {
            sb.append(SVGConstants.CSS_300_VALUE);
            sb.append(SVGConstants.COMMA);
        }

        if ((weight & TextNode.FONT_WEIGHT_400) != 0) {
            sb.append(SVGConstants.CSS_400_VALUE);
            sb.append(SVGConstants.COMMA);
        }

        if ((weight & TextNode.FONT_WEIGHT_500) != 0) {
            sb.append(SVGConstants.CSS_500_VALUE);
            sb.append(SVGConstants.COMMA);
        }

        if ((weight & TextNode.FONT_WEIGHT_600) != 0) {
            sb.append(SVGConstants.CSS_600_VALUE);
            sb.append(SVGConstants.COMMA);
        }

        if ((weight & TextNode.FONT_WEIGHT_700) != 0) {
            sb.append(SVGConstants.CSS_700_VALUE);
            sb.append(SVGConstants.COMMA);
        }

        if ((weight & TextNode.FONT_WEIGHT_800) != 0) {
            sb.append(SVGConstants.CSS_800_VALUE);
            sb.append(SVGConstants.COMMA);
        }

        if ((weight & TextNode.FONT_WEIGHT_900) != 0) {
            sb.append(SVGConstants.CSS_900_VALUE);
            sb.append(SVGConstants.COMMA);
        }

        if (sb.length() > 0) {
            return sb.toString().substring(0, sb.length() - 1);
        }
        
        return sb.toString();
    }

    /**
     * Helper method. Converts the input <code>Transform</code>
     * to a string trait value.
     *
     * @param transform the transform to convert.
     * @return the string trait value.
     */
    protected static String toStringTrait(final Transform transform) {
        if (transform == null) {
            return Transformable.IDENTITY_TRANSFORM_TRAIT;
        } else {
            StringBuffer sb = new StringBuffer();
            sb.append("matrix(");
            sb.append(transform.getComponent(0));
            sb.append(",");
            sb.append(transform.getComponent(1));
            sb.append(",");
            sb.append(transform.getComponent(2));
            sb.append(",");
            sb.append(transform.getComponent(3));
            sb.append(",");
            sb.append(transform.getComponent(4));
            sb.append(",");
            sb.append(transform.getComponent(5));
            sb.append(")");
            return sb.toString();
        }
    }
 
    /**
     * Helper method. Converts the input unicode range into a String
     * trait, with the following syntax:
     * "U+b1-e1,U+b2-e2,...,U+bn-en"
     *
     * @param u the unicode range to convert.
     * @return the string value with the unicode range syntax.
     */
    protected String unicodeRangeToStringTrait(final int[][] u) {
        if (u == null) {
            return null;
        }

        if (u.length == 0) {
            return SVGConstants.EMPTY;
        }

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < u.length - 1; i++) {
            if (u[i] == null || u[i].length != 2) {
                throw new IllegalArgumentException();
            }
            sb.append("U+");
            sb.append(Integer.toHexString(u[i][0]));
            sb.append('-');
            sb.append(Integer.toHexString(u[i][1]));
            sb.append(',');
        }

        if (u[u.length - 1] == null || u[u.length - 1].length != 2) {
            throw new IllegalArgumentException();
        }
        sb.append("U+");
        sb.append(Integer.toHexString(u[u.length - 1][0]));
        sb.append('-');
        sb.append(Integer.toHexString(u[u.length - 1][1]));
        
        return sb.toString();
    }

    /**
     * Converts the input float value to an animated float array trait.
     *
     * @param v the float value to wrap in a float[][] array.
     * @return the wrapped value.
     */
    float[][] toAnimatedFloatArray(final float v) {
        return new float[][] {{v}};
    }

    /**
     * Converts the input float[] array to an animated float array trait.
     *
     * @param a the float array to wrap in a float[][] array. Should not
     *        be null.
     * @return the wrapped value.
     */
    float[][] toAnimatedFloatArray(final float[] a) {
        float[][] v = new float[a.length][];
        // This assumes that each value in the input array are separate
        // components.
        for (int i = 0; i < a.length; i++) {
            v[i] = new float[] {a[i]};
        }
        return v;
    }

    /**
     * Utility method to convert a Path to an animatable float array.
     *
     * @param p the path to convert
     * @return the converted value.
     */
    float[][] toAnimatedFloatArray(final Path path) {
        return new float[][] {path.getData()};
    }

    /**
     * Converts an animated float array to a trait float array value.
     * This is used for multi-component trait values, such as 
     * stroke-dasharray, or the text's x, y or rotate values.
     *
     * @param value the animated value to convert.
     * @return an float array trait value.
     */
    float[] toTraitFloatArray(final float[][] value) {
        float[] v = new float[value.length];
        for (int i = 0; i < v.length; i++) {
            v[i] = value[i][0];
        }
        
        return v;
    }
    
    /**
     * @return a text description of this node including the node
     *         ID if one was set.
     */
    public String toString() {
        if (getId() != null && getId().length() > 0) {
            return "ElementNode[" + getId() + "] [" + super.toString() + "]";
        } else {
            return super.toString();
        }
    }        

    // =========================================================================
    // Type comparison utilities.
    //
    
    /**
     * @param objA first object to compare.
     * @param objB second object to compare. 
     * @return true if the objects are both null or if they are Object.equals()
     */
    public static boolean equal(final Object objA, final Object objB) {
        if (objA == objB) {
            return true;
        }

        if (objA == null || objB == null) {
            return false;
        }

        return objA.equals(objB);
    }
    
    
    /**
     * @param faa first float array to compare
     * @param faab second float array to compare
     * @return true if the objects are both null or if they are equal
     */
    public static boolean equal(final float[] faa, final float[] fab) {
        if (faa == fab) {
            return true;
        }

        if (faa == null || fab == null || faa.length != fab.length) {
            return false;
        }

        int n = faa.length;
        for (int i = 0; i < n; i++) {
            if (faa[i] != fab[i]) {
                return false;
            }
        }

        return true;
    }
  
    /**
     * @param saa first string array to compare
     * @param sab second string array to compare
     * @return true if the objects are both null or if they are equal
     */
    public static boolean equal(final String[] saa, final String[] sab) {
        if (saa == sab) {
            return true;
        }

        if (saa == null || sab == null || saa.length != sab.length) {
            return false;
        }

        int n = saa.length;
        for (int i = 0; i < n; i++) {
            if (!equal(saa[i], sab[i])) {
                return false;
            }
        }

        return true;
    }
  
    /**
     * @param iaa first int array to compare
     * @param iab second int array to compare
     * @return true if the objects are both null or if they are equal
     */
    public static boolean equal(final int[][] iaa, final int[][] iab) {
        if (iaa == iab) {
            return true;
        }

        if (iaa == null || iab == null || iaa.length != iab.length) {
            return false;
        }

        int n = iaa.length;
        for (int i = 0; i < n; i++) {
            if (iaa[i] != iab[i]) {
                if (iaa[i] == null || iab[i] == null 
                        || iaa[i].length != iab[i].length) {
                    return false;
                }

                int m = iaa[i].length;
                for (int j = 0; j < m; j++) {
                    if (iaa[i][j] != iab[i][j]) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
   
  
    /**
     * @param faa first float array to compare
     * @param fab second float array to compare
     * @return true if the objects are both null or if they are equal
     */
    public static boolean equal(final float[][] faa, final float[][] fab) {
        if (faa == fab) {
            return true;
        }

        if (faa == null || fab == null || faa.length != fab.length) {
            return false;
        }

        int n = faa.length;
        for (int i = 0; i < n; i++) {
            if (faa[i] != fab[i]) {
                if (faa[i] == null || fab[i] == null 
                        || faa[i].length != fab[i].length) {
                    return false;
                }

                int m = faa[i].length;
                for (int j = 0; j < m; j++) {
                    if (faa[i][j] != fab[i][j]) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Utility method.
     *
     * @param str the String object to intern.
     * @return null if the input string is null. The interned string otherwise.
     */
    public static String intern(final String str) {
        if (str == null) {
            return null;
        }

        return str.intern();
    }
   
}
