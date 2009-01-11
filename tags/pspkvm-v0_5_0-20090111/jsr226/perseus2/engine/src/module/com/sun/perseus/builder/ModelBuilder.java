/*
 *
 *
 * Portions Copyright  2000-2007 Sun Microsystems, Inc. All Rights
 * Reserved.  Use is subject to license terms.
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

/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included with this distribution in  *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package com.sun.perseus.builder;

import com.sun.perseus.platform.ThreadSupport;

import com.sun.perseus.model.CompositeNode;
import com.sun.perseus.model.DocumentNode;
import com.sun.perseus.model.ElementNode;
import com.sun.perseus.model.Font;
import com.sun.perseus.model.FontFace;
import com.sun.perseus.model.ModelNode;
import com.sun.perseus.model.UpdateAdapter;
import com.sun.perseus.model.UpdateListener;
import com.sun.perseus.model.Use;

import com.sun.perseus.util.SimpleTokenizer;

import com.sun.perseus.util.SVGConstants;

import com.sun.perseus.platform.GZIPSupport;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.w3c.dom.DOMException;

import java.util.Vector;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.ParserConfigurationException;

/**
 * NOTE: need to change currentElement management so that there is only a
 * need to do a getParent() and cast to (ElementNode).
 *
 * <code>ModelBuilder</code> is a SAX2 <code>ContentHandler</code> that
 * builds a <b>Model</b> (i.e. a tree of <code>ModelNode</code>s from 
 * the SAX2 events. <br />
 *
 * This class also offers a static method to synchronously builds a
 * <b>Model</b> given a URI: {@link ModelBuilder#loadDocument loadDocument}.
 *
 * @version $Id: ModelBuilder.java,v 1.10 2006/07/13 00:55:57 st125089 Exp $
 */
public class ModelBuilder extends DefaultHandler {
    /**
     * The default DTD subset used when resolving the DTD entities.
     */
    public static final String DTD_SUBSET =  
        "<!ATTLIST svg\n" +
        "     xmlns CDATA #FIXED \"http://www.w3.org/2000/svg\"\n" +
        "     xmlns:xlink CDATA #FIXED \"http://www.w3.org/1999/xlink\"\n" +
        ">";

    /**
     * The message used in the SAXException when the 
     * loading thread is interrupted
     */
    public static final String LOAD_INTERRUPTED = "Load Interrupted : ";

    /**
     * The accepted DTD public IDs.
     */
    protected static String dtdids = "-//W3C//DTD SVG 1.1 Tiny//EN";

    /**
     * Object keeping track of the current node, i.e., the 
     * last node that was built from an element. 
     */
    protected ElementNode currentElement;

    /**
     * Root of the model tree
     */
    protected DocumentNode modelRoot;

    /**
     * Keeps track of opened streams (entities) so that
     * they can be closed when parsing completes.
     * @see #resolveEntity
     */
    protected Vector entityStreams = new Vector();

    /**
     * Keeps pending namespaceURI to prefix mapping. This is used because
     * the prefix mapping is declared in the startPrefixMapping method
     * _before_ startElement is called. Therefore, the startElement method
     * will use this pendingPrefixMapping vector to declare prefix mappings
     * on the newly created element.
     */
    protected Vector pendingPrefixMapping = null;

    /**
     * Used to allows quick check on pendingPrefixMapping.
     */
    protected Vector pendingPrefixMappingCache = new Vector();

    /**
     * The <code>modelFactory</code> is used to build 
     * <code>ModelNode</code> instances corresponding
     * to individual nodes in the parsed XML documents.
     * This <code>ModelBuilder</code> aggregates the nodes
     * manufactured by the <code>modelFactory</code>
     *
     * @param modelFactoryIn the factory that contains element
     *        prototypes for the supported element types
     * @param modelRootIn the DocumentNode that should be populated 
     *        with the result of the build process.
     */
    ModelBuilder(Vector modelFactoryIn,
                 final DocumentNode modelRootIn) {
        if (modelFactoryIn == null) {
            modelFactoryIn = SVGTinyModelFactory.getPrototypes(modelRootIn);
        }

        this.modelRoot = modelRootIn;

        int n = modelFactoryIn.size();
        for (int i = 0; i < n; i++) {
            modelRoot.addPrototype((ElementNode) modelFactoryIn.elementAt(i));
        }
    }

    /**
     * @return the root of the tree built by this builder.
     * null is returned if no tree was built yet
     */
    final DocumentNode getModelRoot() {
        return modelRoot;
    }

    /**
     * Utility method. Invokes the <code>Runnable</code> on the
     * <code>modelRoot.invokeAndWait</code>.
     *
     * @param runnable the <code>Runnable</code> to run
     * @throws SAXException if the input <code>Runnable</code> is 
     *         interrupted while pending execution or while running.
     */
    protected void invokeAndWait(final Runnable runnable) throws SAXException {
        try {
            if (!ThreadSupport.isInterrupted(Thread.currentThread())) {
                modelRoot.invokeAndWait(runnable);
            } else {
                throw new InterruptedException();
            }
        } catch (InterruptedException ie) {
            throw new SAXException(LOAD_INTERRUPTED
                                   + Thread.currentThread());
        }
    }

    ////////////////////////////////////////////////////////////////////
    // Default implementation of the EntityResolver interface.
    ////////////////////////////////////////////////////////////////////
    /**
     * Resolve an external entity.
     *
     * @param publicId The public identifer, or null if none is
     *                 available.
     * @param systemId The system identifier provided in the XML 
     *                 document.
     * @return The new input source, or null to require the
     *         default behaviour.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see org.xml.sax.EntityResolver#resolveEntity
     */
    public final InputSource resolveEntity(final String publicId, 
                                           final String systemId) 
        throws SAXException {
        if (publicId == null || dtdids.indexOf(publicId) != -1) {
            // If there is no declared publicId or if the publicId is
            // one of the supported ones (the test is very lose but quick)
            // we assume the input source is an SVG Tiny view and we just
            // process a small DTD subset that includes the default 
            // namespace and xlink namespace declarations. Attribute defaulting
            // is handled by the code, so are default attributes, so there is
            // no need to process the DTDs.
            InputSource is = new InputSource();
            Reader reader = new InputStreamReader(
                    new ByteArrayInputStream(DTD_SUBSET.getBytes()));
            is.setCharacterStream(reader);
            
            // Keep track of opened streams as some SAX 
            // implementations do not close that stream.
            entityStreams.addElement(reader);
            
            return is;
        }
        
        // Let the SAX parser find the entity.
        return null;
    }    

    ////////////////////////////////////////////////////////////////////
    // Default implementation of DTDHandler interface.
    ////////////////////////////////////////////////////////////////////
    
    
    /**
     * Receive notification of a notation declaration.
     *
     * <p>By default, do nothing.  Application writers may override this
     * method in a subclass if they wish to keep track of the notations
     * declared in a document.</p>
     *
     * @param name The notation name.
     * @param publicId The notation public identifier, or null if not
     *                 available.
     * @param systemId The notation system identifier.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see org.xml.sax.DTDHandler#notationDecl
     */
    /*
    public void notationDecl(String name, String publicId, String systemId)
            throws SAXException {
        // no op
    } 
    */
    
    
    /**
     * Receive notification of an unparsed entity declaration.
     *
     * <p>By default, do nothing.  Application writers may override this
     * method in a subclass to keep track of the unparsed entities
     * declared in a document.</p>
     *
     * @param name The entity name.
     * @param publicId The entity public identifier, or null if not
     *                 available.
     * @param systemId The entity system identifier.
     * @param notationName The name of the associated notation.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see org.xml.sax.DTDHandler#unparsedEntityDecl
     */
    /*
    public void unparsedEntityDecl(String name, String publicId,
            String systemId, String notationName) throws SAXException {
        // no op
    }
    */
    
    

    ////////////////////////////////////////////////////////////////////
    // Default implementation of ContentHandler interface.
    ////////////////////////////////////////////////////////////////////
    
    
    /**
     * Receive a Locator object for document events.
     *
     * <p>By default, do nothing.  Application writers may override this
     * method in a subclass if they wish to store the locator for use
     * with other document events.</p>
     *
     * @param locator A locator for all SAX document events.
     * @see org.xml.sax.ContentHandler#setDocumentLocator
     * @see org.xml.sax.Locator
     */
    public final void setDocumentLocator(final Locator locator) {
        // ctx.setLocator(locator);
    }
    
    
    /**
     * <b>SAX</b>: Implements {@link
     * org.xml.sax.ContentHandler#startDocument() ContentHander.startDocument}.
     */
    public final void startDocument() {
    }
    
    
    /**
     * Receive notification of the end of the document.
     *
     * <p>By default, do nothing.  Application writers may override this
     * method in a subclass to take specific actions at the end
     * of a document (such as finalising a tree or closing an output
     * file).</p>
     *
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see org.xml.sax.ContentHandler#endDocument
     */
    public final void endDocument() throws SAXException {
        // Validate the document.
        try {
            modelRoot.validate();
        } catch (DOMException de) {
            de.printStackTrace();
            throw new SAXException(de.getMessage());
        }

        invokeAndWait(new Runnable() {
                public void run() {
                    UpdateListener um = modelRoot.getUpdateListener();
                    modelRoot.setLoaded(true);
                    um.loadComplete(modelRoot);
                }
            });
    }


    /**
     * Receive notification of the start of a Namespace mapping.
     *
     * <p>By default, do nothing.  Application writers may override this
     * method in a subclass to take specific actions at the start of
     * each Namespace prefix scope (such as storing the prefix mapping).</p>
     *
     * @param prefix The Namespace prefix being declared.
     * @param uri The Namespace URI mapped to the prefix.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see org.xml.sax.ContentHandler#startPrefixMapping
     */
    public void startPrefixMapping(String prefix, String uri)
        throws SAXException {
        pendingPrefixMappingCache.addElement(new String[] {prefix, uri});
        pendingPrefixMapping = pendingPrefixMappingCache;
    }


    /**
     * Receive notification of the end of a Namespace mapping.
     *
     * <p>By default, do nothing.  Application writers may override this
     * method in a subclass to take specific actions at the end of
     * each prefix mapping.</p>
     *
     * @param prefix The Namespace prefix being declared.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see org.xml.sax.ContentHandler#endPrefixMapping
     */
    /*
    public void endPrefixMapping(String prefix) throws SAXException {
        // no op
    }
    */
    
    /**
     * Receive notification of the start of an element.
     *
     * @param uri The element's namespace uri
     * @param localName The element's local name, i.e., within the given 
     *        namespace
     * @param qName The element's qualified name, i.e., including the namespace 
     *        prefix
     * @param attributes The specified or defaulted attributes.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see org.xml.sax.ContentHandler#startElement
     */
    public final void startElement(final String uri, 
                                    final String localName,
                                    final String qName, 
                                    final Attributes attributes) 
        throws SAXException {
        // ====================================================================
        // First, build a new element from its XML descriptor.
        // ====================================================================
        ElementNode modelNode = null;
        try {
            modelNode = (ElementNode) modelRoot.createElementNS(uri, localName);
            
            // Handle prefix mappings
            if (pendingPrefixMapping != null) {
                int n = pendingPrefixMapping.size();
                for (int i = 0; i < n; i++) {
                    String[] mapping = (String[]) 
                            pendingPrefixMapping.elementAt(i);
                    modelRoot.addNamespacePrefix(mapping[0], 
                            mapping[1], 
                            modelNode);
                }
                pendingPrefixMapping.removeAllElements();
                pendingPrefixMapping = null;
            }

            // Make sure we mark the node as _not_ loaded.
            // This is important because we use the loaded bit to 
            // control certain behaviors, such as the progressive rendering
            // behavior.
            modelNode.setLoaded(false);

            // ================================================
            // Check that required traits have been specified.
            // ================================================
            String[] requiredTraits = modelNode.getRequiredTraits();
            if (requiredTraits != null) {
                for (int i = 0; i < requiredTraits.length; i++) {
                    if (attributes.getValue(requiredTraits[i]) == null) {
                        throw new SAXException(Messages.formatMessage(
                                Messages.ERROR_MISSING_ATTRIBUTE,
                                new Object[] {
                                    requiredTraits[i],
                                    modelNode.getNamespaceURI(),
                                    modelNode.getLocalName()
                                }));
                    }
                }
            }

            String[][] requiredTraitsNS = modelNode.getRequiredTraitsNS();
            if (requiredTraitsNS != null) {
                for (int i = 0; i < requiredTraitsNS.length; i++) {
                    if (attributes.getValue(requiredTraitsNS[i][0],
                                            requiredTraitsNS[i][1]) == null) {
                        throw new SAXException(Messages.formatMessage(
                                Messages.ERROR_MISSING_ATTRIBUTE_NS,
                                new Object[] {
                                    requiredTraitsNS[i][1],
                                    requiredTraitsNS[i][0],
                                    modelNode.getNamespaceURI(),
                                    modelNode.getLocalName()
                                }));
                    }
                }
            }

            // ================================================
            // End of required traits check
            // ================================================

            // ================================================
            // Apply all specified traits.
            // ================================================
            int n = attributes.getLength();
            for (int i = 0; i < n; i++) {
                modelNode.setAttributeNS(attributes.getURI(i),
                                     attributes.getLocalName(i),
                                     attributes.getValue(i));
            }

            // ================================================
            // Apply default traits if they were not specified.
            // ================================================
            String[][] defaultTraits = modelNode.getDefaultTraits();
            if (defaultTraits != null) {
                for (int i = 0; i < defaultTraits.length; i++) {
                    if (attributes.getValue(defaultTraits[i][0]) == null) {
                        modelNode.setAttribute(defaultTraits[i][0],
                                               defaultTraits[i][1]);
                    }
                }
            }

            // ================================================
            // IMPL NOTE
            //
            // If the style attribute is specified, apply the 
            // traits it specified. The reason for handling the
            // style attribute at the parser level is that it 
            // is not a regular trait. It is an in-line 
            // stylesheet. We only support it in order to 
            // support imports from Adobe Illustrator which
            // uses the style attribute on gradients, even
            // when the option to export without the style 
            // attribute is selected.
            // ================================================
            String styleAttribute = 
                attributes.getValue(SVGConstants.SVG_STYLE_ATTRIBUTE);
            if (styleAttribute != null) {
                parseStyleAttribute(styleAttribute, modelNode);
            }

            // ================================================
            // Apply trait aliases
            // ================================================
            String[][] traitAliases = modelNode.getTraitAliases();
            if (traitAliases != null) {
                for (int i = 0; i < traitAliases.length; i++) {
                    if (attributes.getValue(traitAliases[i][0]) == null) {
                        // The trait with alias was not specified.
                        // Check if its alias was specified.
                        String v = attributes.getValue(traitAliases[i][1]);
                        if (v != null) {
                            modelNode.setAttribute(traitAliases[i][0], v);
                        }
                    }
                }
            }
        } catch (DOMException e) {
            e.printStackTrace();
            throw new SAXException(e.getMessage());
        }

        // ====================================================================
        // Append new element to the tree
        // ====================================================================
        if (currentElement != null) {
            addToParent(modelNode, currentElement);
        } else {
            addToParent(modelNode, modelRoot);
        }
        currentElement = modelNode;

        // ====================================================================
        // Notify application that load has begun on a new element
        // ====================================================================
        final ModelNode startedNode = modelNode;
        final UpdateListener ul = modelRoot.getUpdateListener();
        invokeAndWait(new Runnable() {
                public void run() {
                    ul.loadBegun(startedNode);
                    
                }
            });

        // ====================================================================
        // Check if there were any delayed exception. A delayed exception 
        // allows progressive rendering of bad path to happen before the
        // exception which captured the bad path data is actually thrown (below)
        // ====================================================================
        try { 
            modelRoot.checkDelayedException();
        } catch (DOMException e) {
            throw new SAXException(e.getMessage());
        }
    }

    /**
     * Utility method to parse a trait value.
     *
     * @param styleValue the value of the style attribute to parse.
     * @param elt the ElementNode on which trait values should be set.
     */
    private void parseStyleAttribute(final String styleValue, 
                                     final ElementNode elt) {
        SimpleTokenizer st = new SimpleTokenizer(styleValue, 
                                                 SVGConstants.COMMA_STR);
        while (st.hasMoreTokens()) {
            String traitSpec = st.nextToken();
            int ci = traitSpec.indexOf(':');
            String traitName = null;
            String traitValue = null;
            if (ci != -1) {
                traitName = traitSpec.substring(0, ci);
                traitValue = traitSpec.substring(ci + 1);
            } else {
                traitName = "";
                traitValue = "";
            }
            elt.setAttribute(traitName, traitValue);
        }
    }

    /**
     * Adds the input node to the given parent. If there is no
     * associated <code>RunnableQueue</code>, the child is simply
     * added to the parent in the calling thread. If there is
     * a <code>RunnableQueue</code>, the child is added to the parent 
     * the <code>RunnableQueue</code> thread, by invoking a 
     * <code>Runnable</code> on the queue.
     *
     * @param child node to add to the parent
     * @param parent node to which the child is added.
     *
     * @throws SAXException if the child cannot be added to the parent
     *         because the thread was interrupted or if the thread 
     *         was interrupted to begin with.
     */
    void addToParent(final ElementNode child,
                     final CompositeNode parent) 
        throws SAXException {

        invokeAndWait(new Runnable() {
                public void run() {
                    parent.add(child);
                }
            });

        // This may happen, for example, if the loading thread
        // is interrupted by an update listener.
        if (ThreadSupport.isInterrupted(Thread.currentThread())) {
            throw new SAXException(LOAD_INTERRUPTED 
                                   + Thread.currentThread());
        }
    }

    /**
     * Debug: trace element to console
     *
     * @param uri the element's namespace uri
     * @param localName the element's local name
     * @param qName the element's qualified name
     * @param attributes the element's attributes
     */
    /*
    public final void traceAttributes(final String uri, 
                                      final String localName,
                                      final String qName, 
                                      final Attributes attributes) {
        System.out.println(">>>>> startElement <" + localName 
                           + "> \n\turi = " + uri + "\n\tqName = " + qName);

        int n = attributes.getLength();
        for (int i = 0; i < n; i++) {
            System.out.println("=============>");
            System.out.println(" uri[" + i + "] = " + attributes.getURI(i));
            System.out.println(" name[" + i + "] = local(" 
                               + attributes.getLocalName(i) + ") qname(" 
                               + attributes.getQName(i) + ")");
            System.out.println(" value[" + i + "] = ivalue(" 
                               + attributes.getValue(i) 
                               + ") qvalue(" 
                               + attributes.getValue(attributes.getQName(i)) 
                               + ") urivalue(" 
                               + attributes.getValue(attributes.getLocalName(i),
                                                     attributes.getURI(i)) 
                               + ")");
            System.out.println(attributes.getQName(i) + " = " 
                               + attributes.getValue(i));
            System.out.println("<=============");
        }
    }
    */

    /**
     * Updates the <tt>currentElement</tt>.
     *
     * @param uri The element's namespace uri.
     * @param localName The element's local name
     * @param qName The element's qualified name
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see org.xml.sax.ContentHandler#endElement
     */
    public final void endElement(final String uri, 
                                 final String localName, 
                                 final String qName) throws SAXException {
        //
        // See beginElement: currentElement _cannot_ be null
        // 

        // Update the Font data base if a new FontFace was loaded
        if (currentElement instanceof Font) {
            invokeAndWait(new Runnable() {
                    public void run() {
                        ModelNode fc 
                            = currentElement.getFirstChildNode();
                        if (fc != null && fc instanceof FontFace) {
                            modelRoot.addFontFace
                                ((FontFace) fc);
                        }
                    }
                });
        }
        
        invokeAndWait(new Runnable() {
                public void run() {
                    UpdateListener um = modelRoot.getUpdateListener();
                    currentElement.setLoaded(true);
                    um.loadComplete(currentElement);
                }
            });
        
        // Move up the next content node
        ModelNode parent = currentElement.getParent();
        if (parent instanceof ElementNode) {
            currentElement = (ElementNode) parent;
        } else {
            currentElement = null;
        }
    }
    
    /**
     * Receive notification of character data inside an element.
     *
     * <p>By default, do nothing.  Application writers may override this
     * method to take specific actions for each chunk of character data
     * (such as adding the data to a node or buffer, or printing it to
     * a file).</p>
     *
     * @param ch The characters.
     * @param start The start position in the character array.
     * @param length The number of characters to use from the
     *               character array.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see org.xml.sax.ContentHandler#characters
     */
    public final void characters(final char[] ch, 
                                 final int start, 
                                 final int length) throws SAXException {
        if (currentElement != null) {
            final String text = new String(ch, start, length);
            final UpdateListener ul = modelRoot.getUpdateListener();
            invokeAndWait(new Runnable() {
                    public void run() {
                        currentElement.appendTextChild(text);
                        ul.textInserted(currentElement);
                    }
                });
        } else {
            System.err.println(">>>>>>>>>>>>>> currentElement is null!!!!!!!");
        }
    }
    
    
    /**
     * Receive notification of ignorable whitespace in element content.
     *
     * <p>By default, do nothing.  Application writers may override this
     * method to take specific actions for each chunk of ignorable
     * whitespace (such as adding data to a node or buffer, or printing
     * it to a file).</p>
     *
     * @param ch The whitespace characters.
     * @param start The start position in the character array.
     * @param length The number of characters to use from the
     *               character array.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see org.xml.sax.ContentHandler#ignorableWhitespace
     */
    /*
    public void ignorableWhitespace(char ch[], int start, int length)
            throws SAXException {
        // no op
    } 
    */
    
    
    /**
     * Receive notification of a processing instruction.
     *
     * <p>By default, do nothing.  Application writers may override this
     * method in a subclass to take specific actions for each
     * processing instruction, such as setting status variables or
     * invoking other methods.</p>
     *
     * @param target The processing instruction target.
     * @param data The processing instruction data, or null if
     *             none is supplied.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see org.xml.sax.ContentHandler#processingInstruction
     */
    /*
    public void processingInstruction(String target, String data)
            throws SAXException {
        // no op
    }
    */


    /**
     * Receive notification of a skipped entity.
     *
     * <p>By default, do nothing.  Application writers may override this
     * method in a subclass to take specific actions for each
     * processing instruction, such as setting status variables or
     * invoking other methods.</p>
     *
     * @param name The name of the skipped entity.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see org.xml.sax.ContentHandler#processingInstruction
     */
    /*
    public void skippedEntity(String name) throws SAXException {
        // no op
    }
    */
    
    

    ////////////////////////////////////////////////////////////////////
    // Default implementation of the ErrorHandler interface.
    ////////////////////////////////////////////////////////////////////
    
    /**
     * Report a fatal XML parsing error.
     *
     * <p>The default implementation throws a SAXParseException.
     * Application writers may override this method in a subclass if
     * they need to take specific actions for each fatal error (such as
     * collecting all of the errors into a single report): in any case,
     * the application must stop all regular processing when this
     * method is invoked, since the document is no longer reliable, and
     * the parser may no longer report parsing events.</p>
     *
     * @param e The error information encoded as an exception.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see org.xml.sax.ErrorHandler#fatalError
     * @see org.xml.sax.SAXParseException
     */
    public final void fatalError(final SAXParseException e)
        throws SAXException {
        throw e;
    }

    // =========================================================================
    // Utility method
    // =========================================================================

    /**
     * Loads an SVG Tiny document from a given URI
     *
     * @param svgURI the URI of the SVG document to load.
     * @return the <code>DocumentNode</code> built from the requested svgURI.
     * @throws IOException if the file cannot be loaded.
     */
    public static DocumentNode loadDocument(final String svgURI) 
        throws IOException {

        InputStream is = GZIPSupport.openHandleGZIP(svgURI);
        DocumentNode doc = new DocumentNode();
        doc.setLoaded(false);
        doc.setDocumentURI(svgURI);

        UpdateAdapter updateAdapter = new UpdateAdapter();
        doc.setUpdateListener(updateAdapter);

        loadDocument(is, doc);

        if (!updateAdapter.loadSuccess()) {
            Exception cause = updateAdapter.getLoadingFailedException();
            if (cause == null) {
                throw new IOException();
            } else {
                throw new IOException(cause.getMessage());
            }
        }

        return doc;
    }

    /**
     * Load an SVG Tiny document at the given input stream.
     *
     * This method uses JAXP to define the SAX parser to use.
     *
     * Any error is reported to the input <code>DocumentNode</code>'s 
     * <code>UpdateListener</code>'s <code>loadFailed</code> method.
     *
     * @param is the <code>InputStream</code> from which the SVG content is
     *        read. This might be GZIPed compressed stream. If the input stream
     *        is null, an input stream is opened from the root's document URI.
     * @param root the documentNode to populate with the document's content
     *
     *
     * @throws IllegalArgumentException if the root's URI is null, 
     *         if root is null or if the root's <code>UpdateListener</code> is 
     *         null
     */
    public static void loadDocument(final InputStream is,
                                    final DocumentNode root) {
        loadDocument(is, root, null);
    }

    /**
     * Load an SVG Tiny document at the given input stream.
     *
     * This method uses JAXP to define the SAX parser to use.
     *
     * Any error is reported to the input <code>DocumentNode</code>'s 
     * <code>UpdateListener</code>'s <code>loadFailed</code> method.
     *
     * @param is the <code>InputStream</code> from which the SVG content is
     *        read. This might be GZIPed compressed stream. If the input stream
     *        is null, an input stream is opened from the root's document URI.
     * @param root the documentNode to populate with the document's content
     * @param modelFactory the <code>ModelFactory</code> used to turn XML 
     *        elements into <code>ModelNode</code> instances.
     *
     * @throws IllegalArgumentException if the root's URI is null, 
     *         if root is null or if the root's <code>UpdateListener</code> is 
     *         null
     */
    public static void loadDocument(InputStream is,
                                    final DocumentNode root,
                                    Vector modelFactory) {

        if (root == null) {
            throw new IllegalArgumentException();
        }

        root.setLoaded(false);

        String svgURI = root.getURIBase();

        if (is == null && svgURI == null) {
            throw new IllegalArgumentException();
        }

        final UpdateListener updateListener = root.getUpdateListener();
        if (updateListener == null) {
            throw new IllegalArgumentException();
        }

        // Before parsing the file, we add a default mapping for the
        // SVG and XLink namespaces.
        root.addNamespacePrefix(SVGConstants.XLINK_PREFIX, 
                                SVGConstants.XLINK_NAMESPACE_URI,
                                root);
        root.addNamespacePrefix("",
                                SVGConstants.SVG_NAMESPACE_URI,
                                root);

        ModelBuilder modelBuilder = null;
        InputStream gzipIS = null;

        try {
            // Get a SAX parser through the JAXP API. The 
            // parser does not do validation and is namespace aware
            SAXParserFactory factory = SAXParserFactory.newInstance();
            // System.err.println(">>>>>>>>>>>>>>>> SAXParserFactory class: " 
            //         + factory.getClass().getName());
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            
            SAXParser parser = null;
            parser = factory.newSAXParser();
            final SAXParser saxParser = parser;
            
            // Check the input stream. If the input stream is not null, we
            // load that stream. Otherwise, we build an input stream from
            // the root's URI.
            if (is == null) {
                is = GZIPSupport.openHandleGZIP(svgURI);
            }

            // The following wraps the input stream, if necessary, to handle
            // GZIP compression.
            gzipIS = GZIPSupport.handleGZIP(is);
            final InputStream fgzipIS = gzipIS;

            root.invokeAndWait(new Runnable() {
                    public void run() {
                        // Parse the document now. Our modelBuilder 
                        // handles the parser's SAX events.
                        updateListener.loadStarting(root, fgzipIS);
                    }
                });
            
            modelBuilder 
                = new ModelBuilder(modelFactory, root);
            
            saxParser.parse(gzipIS, modelBuilder);
        } catch (ParserConfigurationException pce) {
            loadingFailed(updateListener, root, pce);
        } catch (SAXParseException spe) {
            loadingFailed(updateListener, root, spe);
        } catch (SAXException se) {
            loadingFailed(updateListener, root, se);
        } catch (IOException ioe) {
            loadingFailed(updateListener, root, ioe);
        } catch (Exception e) {
            loadingFailed(updateListener, root, e);
        } finally {
            try {
                if (gzipIS != null) {
                    gzipIS.close();
                }
            } catch (IOException ioe) {
                // Don't do anything if we got an exception
                // while trying to close the stream.
            }

            if (modelBuilder != null) {
                int n = modelBuilder.entityStreams.size();
                for (int i = 0; i < n; i++) {
                    Reader r = (Reader) modelBuilder.entityStreams.elementAt(i);
                    try {
                        r.close();
                    } catch (IOException ioe) {
                        // Do nothing: this means the stream was 
                        // closed by the SAX parser.
                    }
                }
            }
        }
    }

    /**
     * Utility method to report an exception to the UpdateListener
     * in the proper thread.
     *
     * @param updateListener the <code>UpdateListener</code> to which 
     *        the error should be reported.
     * @param root the <code>DocumentNode</code> which was being loaded.
     * @param e the <code>Exception</code> which caused the failure.
     */
    protected static void loadingFailed(final UpdateListener updateListener,
                                        final DocumentNode root,
                                        final Exception e) {
        System.err.println(">>>>>>>>>>>>>>>>>>> +++++ Loading failed ...");
        e.printStackTrace();
        try {
            root.invokeAndWait(new Runnable() {
                    public void run() {
                        updateListener.loadingFailed(root, e);
                    }
                });
        } catch (InterruptedException ie) {
            // The current thread was interrupted. Loading Failed will
            // not be reported...
            return;
        }
    }
}

