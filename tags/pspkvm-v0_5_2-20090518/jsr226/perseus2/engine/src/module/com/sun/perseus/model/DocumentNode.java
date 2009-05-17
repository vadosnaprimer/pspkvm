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

import com.sun.perseus.util.RunnableQueue;
import com.sun.perseus.util.RunnableQueue.RunnableHandler;

import com.sun.perseus.j2d.GraphicsProperties;
import com.sun.perseus.j2d.RenderGraphics;
import com.sun.perseus.j2d.TextProperties;
import com.sun.perseus.j2d.Transform;

import com.sun.perseus.builder.DefaultFontFace;

import com.sun.perseus.parser.ColorParser;
import com.sun.perseus.parser.ClockParser;
import com.sun.perseus.parser.LengthParser;
import com.sun.perseus.parser.NumberListParser;
import com.sun.perseus.parser.PathParser;
import com.sun.perseus.parser.TimeConditionParser;
import com.sun.perseus.parser.TransformListParser;
import com.sun.perseus.parser.UnicodeParser;
import com.sun.perseus.parser.ViewBoxParser;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.DOMException;

import java.io.PrintStream;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Hashtable;
import java.util.Vector;

import com.sun.perseus.util.SVGConstants;

/**
 * A <code>DocumentNode</code> represents the root of an SVG document model.
 * A DocumentNode is a <code>Viewport</code> and centralizes some functions such
 * as the font data base, tree updates management and event dispatching.
 * The <code>DocumentNode</code> also manages the <code>RunnableQueue</code>
 * used to synchronize all access to the SVG document.
 *
 * @version $Id: DocumentNode.java,v 1.24 2006/06/29 10:47:30 ln156897 Exp $
 */
public class DocumentNode extends Viewport implements Document {
    /**
     * Default spatial resolution value (96dpi);
     */
    public static final float DEFAULT_PIXEL_MM_SIZE 
        = 0.26458333333333333333333333333333f; // 96dpi

    /**
     * Size of a pixel, in millimeters. This is needed in unit
     * conversion as well.
     */
    protected float pxMMSize = DEFAULT_PIXEL_MM_SIZE;

    /**
     * Coordinate in user space coordinates. This value is used as a working
     * array for all nodes in the document tree.
     */
    protected float[] upt = {0, 0};

    /**
     * A map itself containing map of ElementHandlers
     * namespaceURI -> Hashtable
     *
     * the contained Hashtable maps:
     * localName -> ElementNode prototype.
     */
    protected Hashtable namespaceMap = new Hashtable();

    /**
     * Used to store additional traits NS., i.e., traits
     * which are not naturally supported by the element.
     * elt -> Hashtable.
     * 
     * The contained Hashtable maps:
     * namespace -> Hashtable.
     *
     * The contained Hashtable maps:
     * localname -> value.
     */
    protected Hashtable unknownTraitsNS = null;

    /**
     * Parser used to convert clock values
     */
    protected final ClockParser clockParser = new ClockParser();

    /**
     * Parser used to convert unit and unitless floating point
     * values to floats. 
     */
    protected final LengthParser lengthParser = new LengthParser();

    /**
     * Parser used to convert time condition values
     */
    protected final TimeConditionParser timeConditionParser =
        new TimeConditionParser();

    /**
     * Parser used to convert number list values
     */
    protected NumberListParser numberListParser
        = new NumberListParser();

    /**
     * Parser used to convert transform values
     */
    protected TransformListParser transformListParser 
        = new TransformListParser();

    /**
     * Parser used to convert color values
     */
    protected final ColorParser colorParser = new ColorParser();

    /**
     * Parser used to convert viewBox values
     */
    protected final ViewBoxParser viewBoxParser = new ViewBoxParser();

    /**
     * Parser used to convert path data
     */
    protected final PathParser pathParser = new PathParser();

    /**
     * Parser used to convert unicode ranges.
     */
    protected UnicodeParser unicodeParser
        = new UnicodeParser();

    /**
     * The <code>ImageLoader</code> handles the desired policy
     * for loading image resources.
     */
    protected ImageLoader imageLoader;

    /**
     * The default FontFace used by this RenderGraphics. 
     * @see com.sun.perseus.builder.DefaultFontFace
     */
    protected FontFace defaultFontFace;

    /**
     * Exceptions can be delayed and later thrown. This is used, for example,
     * to keep processing an invalid d attribute on a <path> element and only
     * throw the exception after the corresponding model node has been hooked
     * to the tree (while the exception was thrown by the path parser during the
     * processing of the d attribute). This is only used when loading a document
     * to comply to the error processing required for <path>, <polyline> and 
     * <polygon> processing of the 'd' and 'points' attributes.
     */
    protected DOMException delayedException;

    /**
     * The default namespace URI for elements created in that document,
     * in case the namespace URI in createElementNS is empty.
     */
    protected String defaultNamespaceURI = SVGConstants.SVG_NAMESPACE_URI;

    /**
     * The Document URI
     */
    protected String docURI;

    /**
     * The set of initial <code>FontFace</code> used by this 
     * <code>RenderingGraphics</code>.
     * <br />
     * <b>NOTE</b><br />It is the responsibility of the user of the 
     * Perseus software to make sure that the <code>RenderGraphics</code>
     * initial <code>fontFamiliy</code> value is indeed the same as that 
     * of the various fonts in the <code>initialFontFaces</code> array.
     * <br />
     * If there is a mismatch, then text content that does not
     * specify the <code>font-family</code> property will not match against the
     * <code>initialFontFaces</code> instances and only the defaultFont will be
     * used.
     *
     * @see com.sun.perseus.builder.DefaultFontFace
     */
    protected FontFace[] initialFontFaces;

    /**
     * The FontFace data base used by this RenderGraphics.
     * The Hashtable maps font-family names to a Vector 
     * of fonts with that font-family value.
     */
    protected Hashtable fontFaceDB = null;

    /**
     * Set of currently active TraitAnimations.
     */
    protected Vector activeTraitAnims = new Vector(0);

    /**
     * Set of currently active MediaElements.
     */
    protected Vector activeMediaElements = new Vector(0);

    /**
     * Controls whether the document is 'playing'. This is used to disable the 
     * media elements like audio when the document is not playing and allows
     * sampling the document without having the audio stream played.
     */
    protected boolean playing;

    /**
     * EventSupport is used for listener registration and event
     * dispatching.
     */
    protected EventSupport eventSupport = new EventSupport();

    /**
     * The associated <code>UpdateListener</code> is notified of
     * all mutation events on this <code>DocumentNode</code> tree
     */
    protected UpdateListener updateListener;

    /**
     * The associated <code>RunnableQueue</code>, if any, is
     * managing updates to the <code>DocumentNode</code> and any of
     * its descendants. In effect, the updateQueue provides the 
     * synchronization needed for updates to a document tree.
     */
    protected RunnableQueue updateQueue;

    /**
     * The associated <code>RunnableHandler</code> which should be 
     * notified when Runnable acting on this DocumentNode tree are
     * run.
     */
    protected RunnableHandler runHandler;
    
    /**
     * Maps ids to ElementNode instances
     */
    protected Hashtable idToElement = new Hashtable();

    /**
     * Used to store nodes with ids before they are inserted into 
     * the document tree.
     */
    protected Hashtable reservedIds = new Hashtable();

    /**
     * Maps prefixes to namespace entries.
     */
    protected Hashtable prefixes = new Hashtable();

    /**
     * Maps namespaces to prefixes.
     */
    protected Hashtable namespaces = new Hashtable();

    /**
     * A DocumentNode is a root container. This object provides
     * support for root time container behavior.
     */
    protected TimeContainerRootSupport timeContainerRootSupport
        = new TimeContainerRootSupport();

    /**
     * Map of unresolved IDRefs (id -> Vector of referencing IDRefs)
     */
    protected Hashtable unresolvedIDRefs = new Hashtable();

    /**
     * Vector of animations. This is used only at parse time.
     * @see #validate
     */
    protected Vector animations = new Vector(0);

    /**
     * We use a single instance of ModelEvent per DocumentNode instance.
     * This allows multiple instances of DocumentNodes to co-exist.
     */
    protected ModelEvent engineEvent = null;

    /**
     * Transform used to perform hit detection on text chunks.
     */
    protected Transform hitChunkTxf = new Transform(null);

    /**
     * Transform used to compute bounding boxes on text chunks.
     */
    protected Transform bboxChunkTxf = new Transform(null);

    /**
     * Transform used to render text chunks.
     */
    protected Transform paintChunkTxf = new Transform(null);

    /**
     * Transform used to render glyphs.
     */
    protected Transform paintGlyphTxf = new Transform(null);

    /**
     * Transform used to compute bounding boxes on glyphs.
     */
    protected Transform bboxGlyphTxf = new Transform(null);

    /**
     * Transform used to perform hit testing on glyphs
     */
    protected Transform hitGlyphTxf = new Transform(null);

    /**
     * Default constructor
     */
    public DocumentNode() {
        ownerDocument = this;
        engineEvent = new ModelEvent("", this);
        
        // Clear the 'in document tree' bit
        canRenderState &= CAN_RENDER_IN_DOCUMENT_TREE_MASK;
        
        // Clear the renderable bit
        canRenderState &= CAN_RENDER_RENDERABLE_MASK;
    }

    /**
     * Returns the initial value of the given Object-valued property.
     * @return the initial value of the given property, null if the property is
     * unknown.
     */
    protected Object getInitialPropertyState(final int propertyIndex) {
        switch (propertyIndex) {
            case GraphicsNode.PROPERTY_FILL:
                return GraphicsProperties.INITIAL_FILL;
            case GraphicsNode.PROPERTY_STROKE:
                return GraphicsProperties.INITIAL_STROKE;
            case GraphicsNode.PROPERTY_COLOR:
                return GraphicsProperties.INITIAL_COLOR;
            case GraphicsNode.PROPERTY_STROKE_DASH_ARRAY:
                return GraphicsProperties.INITIAL_STROKE_DASH_ARRAY;
            case TextNode.PROPERTY_FONT_FAMILY:
                return TextNode.INITIAL_FONT_FAMILY;
            default:
                return null;
        }
    }

    /**
     * Returns the initial value of the given float-valued property.
     * @return the initial value of the given property, 0 if the property is
     * unknown.
     */
    protected float getInitialFloatPropertyState(final int propertyIndex) {
        switch (propertyIndex) {
            case GraphicsNode.PROPERTY_STROKE_WIDTH:
                return GraphicsNode.INITIAL_STROKE_WIDTH;
            case GraphicsNode.PROPERTY_STROKE_MITER_LIMIT:
                return GraphicsNode.INITIAL_STROKE_MITER_LIMIT;
            case GraphicsNode.PROPERTY_STROKE_DASH_OFFSET:
                return GraphicsNode.INITIAL_STROKE_DASH_OFFSET;
            case TextNode.PROPERTY_FONT_SIZE:
                return TextNode.INITIAL_FONT_SIZE;
            default:
                return 0;
        }
    }

    /**
     * Returns the initial value of the given packed property.
     * @return the initial value of the given packed property, zero if the 
     *         property is unknown.
     */
    protected int getInitialPackedPropertyState(final int propertyIndex) {
        switch (propertyIndex) {
            case GraphicsNode.PROPERTY_FILL_RULE:
                return CompositeGraphicsNode.INITIAL_FILL_RULE_IMPL;
            case GraphicsNode.PROPERTY_STROKE_LINE_JOIN:
                return CompositeGraphicsNode.INITIAL_STROKE_LINE_JOIN_IMPL;
            case GraphicsNode.PROPERTY_STROKE_LINE_CAP:
                return CompositeGraphicsNode.INITIAL_STROKE_LINE_CAP_IMPL;
            case GraphicsNode.PROPERTY_DISPLAY:
                return CompositeGraphicsNode.INITIAL_DISPLAY_IMPL;
            case GraphicsNode.PROPERTY_VISIBILITY:
                return CompositeGraphicsNode.INITIAL_VISIBILITY_IMPL;
            case GraphicsNode.PROPERTY_FILL_OPACITY:
                return CompositeGraphicsNode.INITIAL_FILL_OPACITY_IMPL;
            case GraphicsNode.PROPERTY_STROKE_OPACITY:
                return CompositeGraphicsNode.INITIAL_STROKE_OPACITY_IMPL;
            case GraphicsNode.PROPERTY_OPACITY:
                return CompositeGraphicsNode.INITIAL_OPACITY_IMPL;
            case TextNode.PROPERTY_FONT_STYLE:
                return StructureNode.INITIAL_FONT_STYLE_IMPL;
            case TextNode.PROPERTY_FONT_WEIGHT:
                return StructureNode.INITIAL_FONT_WEIGHT_IMPL;
            case TextNode.PROPERTY_TEXT_ANCHOR:
                return StructureNode.INITIAL_TEXT_ANCHOR_IMPL;
                
            default:
                return 0;
        }
    }

    /**
     * Returns the value of the given Object-valued property.
     *
     * @return the value of the given property, null if the property is 
     *         unknown.
     */
    protected Object getPropertyState(final int propertyIndex) {
        return getInitialPropertyState(propertyIndex);
    }

    /**
     * Returns the value of the given float property.
     *
     * @return the value of the given property, null if the property is 
     *         unknown.
     */
    protected float getFloatPropertyState(final int propertyIndex) {
        return getInitialFloatPropertyState(propertyIndex);
    }

    /**
     * Returns the value of the given packed property.
     *
     * @return the value of the given property, null if the property is 
     *         unknown.
     */
    protected int getPackedPropertyState(final int propertyIndex) {
        return getInitialPackedPropertyState(propertyIndex);
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

        paint(getFirstChildNode(), rg);
    }

    /**
     * Initializes the ModelEvent's singleton object with the input data.
     *
     * @param type the event type
     * @param target the event target
     * @param time the event time
     */
    ModelEvent initEngineEvent(final String type,
                               final ModelNode target) {
        ModelEvent engineEvent = new ModelEvent(type, target);
        engineEvent.type = type;
        engineEvent.target = target;
        engineEvent.currentTarget = null;
        engineEvent.anchor = null;
        engineEvent.stopPropagation = false;
        engineEvent.repeatCount = 0;
        engineEvent.keyChar = ModelEvent.CHAR_UNDEFINED;
        
        return engineEvent;
    }

    /**
     * When the document finishes loading, it notifies the ImageLoader so that 
     * it can start loading raster images, in case it waited for the load 
     * completion to start (e.g., as in SVGImageLoader).
     *
     * @param isLoaded the new loaded state
     */
    public final void setLoaded(final boolean isLoaded) {
        super.setLoaded(isLoaded);
        if (isLoaded == true) {
            getImageLoader().documentLoaded(this);
        }
    }

    /**
     * @return true if the DocumentNode is in the playing state.
     */
    public boolean isPlaying() {
        return playing;
    }

    /**
     * @param isPlaying the new playing state.
     */
    public void setPlaying(final boolean isPlaying) {
        this.playing = isPlaying;
    }

    /**
     * @throws DOMException the delayed exception if one was set.
     */
    public void checkDelayedException() throws DOMException {
        if (delayedException != null) {
            throw delayedException;
        }
    }

    /**
     * @return the delayed exception, if any
     */
    public DOMException getDelayedException() {
        return delayedException;
    }

    /**
     * @param de the new delayedException
     */
    protected void setDelayedException(DOMException de) {
        delayedException = de;
    }

    /**
     * @return the size of a px CSS unit in millimeters.
     */
    public float getPixelMMSize() {
        return pxMMSize;
    }

    /**
     * Controls the size of a pixel in millimeters
     * @param newPxMMSize the new pixel size value, in millimeter
     */
    public void setPixelMMSize(final float newPxMMSize) {
        this.pxMMSize = newPxMMSize;
    }

    /**
     * @return true if this node is hooked to the document tree, i.e., if it top
     * most ancestor is the DocumentNode.
     */
    boolean inDocumentTree() {
        return true;
    }

    /**
     * Invoked by <code>IDRef</code> instances when they need the given
     * input id reference to be resolved to an <code>ElementNode</code>
     * reference. If there is a known <code>ElementNode</code> with the
     * requested id, the <code>IDRef</code>'s <code>resolve()</code> 
     * method is invoked immediately. Otherwise, the method will be called
     * as soon as the id reference is resolved.
     *
     * @param idRef the IDRef which needs to be resolved.
     * @param id the id the IDRef needs to resolve to an
     *        <code>ElementNode</code> reference.
     */
    public void resolveIDRef(final IDRef idRef,
                             final String id) {
        ElementNode ref = (ElementNode) getElementById(id);
        if (ref != null) {
            idRef.resolveTo(ref);
        } else {
            if (unresolvedIDRefs != null) {
                Vector idRefs = (Vector) unresolvedIDRefs.get(id);
                if (idRefs == null) {
                    idRefs = new Vector(1);
                    unresolvedIDRefs.put(id, idRefs);
                }
                idRefs.addElement(idRef);
            }
        }
    }

    /**
     * Should be called when a document node is no longer needed 
     * and could be garbage collected.
     */
    public void dispose() {
        clearLayouts();
    }

    /**
     * Gets the <code>ImageLoader</code> instance.
     *
     * @return the <code>ImageLoader</code> associated to this
     *         <code>DocumentNode</code>.
     */
    public ImageLoader getImageLoader() {
        if (imageLoader == null) {
            imageLoader = new DefaultImageLoader();
        }
        return imageLoader;
    }

    /**
     * Sets the <code>ImageLoader</code> for this document.
     *
     * @param imageLoader the new <code>ImageLoader</code> this
     *        <code>DocumentNode</code> should use.
     */
    public void setImageLoader(final ImageLoader imageLoader) {
        this.imageLoader = imageLoader;
    }

    /**
     * A <code>DocumentNode</code> has no expanded content, so this 
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
     * A <code>DocumentNode</code> has no expanded content, so this 
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
     * Utility method. Unhooks the expanded content.
     */
    protected void unhookExpandedQuiet() {
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
    public String getURIBase() {
        return docURI;
    }

    /**
     * Sets this document's URI
     * 
     * @param docURI the new document URI
     */
    public void setDocumentURI(final String docURI) {
        if (ElementNode.equal(docURI, this.docURI)) {
            return;
        }
        modifyingNode();
        this.docURI = docURI;
        modifiedNode();
    }

    /**
     * @return the <code>UpdateListener</code> associated with this viewport.
     * @see #setUpdateListener
     */
    public UpdateListener getUpdateListener() {
        if (parent == null || parent == this) {
            return updateListener;
        } else {
            return parent.getUpdateListener();
        }
    }

    /**
     * @return the <code>RunnableQueue</code> which managers updates to this
     *         <code>DocumentNode</code> hierarchy.
     */
    public RunnableQueue getUpdateQueue() {
        return updateQueue;
    }

    /**
     * @return the <code>RunnableQueue.RunnableHandler</code> which is notified
     *         of <code>Runnable</code> instances ran against this DocumentNode.
     */
    public RunnableHandler getRunnableHandler() {
        return runHandler;
    }

    /**
     * @param updateQueue the <code>RunnableQueue</code> which manages 
     *        updates to this document tree.
     */
    public void setUpdateQueue(final RunnableQueue updateQueue) {
        this.updateQueue = updateQueue;
    }

    /**
     * @param runHandler the <code>RunnableHandler</code> which listens to 
     *        <code>Runnable</code> execution for this 
     *        <code>DocumentNode</code>.
     */
    public void setRunnableHandler(final RunnableHandler runHandler) {
        this.runHandler = runHandler;
    }

    /**
     * Sets the <code>UpdateListener</code> associated with this viewport.
     * All updates made to this tree will be reported to the input
     * <code>UpdateListener</code>
     *
     * @param updateListener the new <code>UpdateListener</code> which will
     *        receive notifications for updates on this tree.
     */
    public void setUpdateListener(final UpdateListener updateListener) {
        this.updateListener = updateListener;
    }

    /**
     * @return The <code>EventSupport</code> instance associated
     *         with this <code>DocumentNode</code>
     */
    public EventSupport getEventSupport() {
        return eventSupport;
    }

    /**
     * If no such element exists, this returns null.
     * If more than one element has an id attribute with that value, what
     * is returned is undefined.
     *
     */
    public Element getElementById(final String id) {
        return (ElementNode) idToElement.get(id);
    }

    /**
     * Return the <code>Element</code> in the current document with
     * the given unique ID. The difference with getElementById is 
     * that this method may return a node that is not inserted in
     * the document tree. This is used in ElementNode.setId().
     *
     * @param id the ID of the object to be retrieved.
     * @return the Element that matches with the given ID or
     * <code>null</code> if the ID is not present.
     *
     * @throws NullPointerException if id is null
     */
    Element getElementByIdAll(final String id) {
        ElementNode n = (ElementNode) idToElement.get(id);
        if (n == null) {
            n = (ElementNode) reservedIds.get(id);
        }

        return n;
    }

    /**
     * Adds the input element to the list of identified nodes.
     *
     * @param element the new element with a non-null identifier
     * 
     * @throws NullPointerException if the input element is null or if its
     *         id is null.
     */
    void addIdentifiedNode(final ElementNode element) {
        // Add element to id map
        idToElement.put(element.getId(), element);

        // Check if there are any unresolved references for 
        // the newly identified node.
        if (unresolvedIDRefs != null) {
            Vector idRefs = (Vector) unresolvedIDRefs.get(element.getId());
            if (idRefs != null) {
                int n = idRefs.size();
                for (int i = 0; i < n; i++) {
                    IDRef idRef = (IDRef) idRefs.elementAt(i);
                    idRef.resolveTo(element);
                }
                unresolvedIDRefs.remove(element.getId());
            }
        }

        // If the id was in the reserved map, remove it.
        reservedIds.remove(element.getId());
    }

    /**
     * Reserves the given id. This is used to be able to check for 
     * duplicate identifiers.
     *
     * @param element the element reserving the id. Should not be null.
     *        The element id should not be null.
     */
    void reserveId(final ElementNode element) {
        reservedIds.put(element.getId(), element);
    }

    /**
     * Remove element from the list of identified nodes
     *
     * @param element the element to remove from the list of identified nodes
     * @throws NullPointerException if element is null or if its id is null.
     */
    void removeIdentifiedNode(final ElementNode element) {
        String id = element.getId();
        if (idToElement.get(id) == element) {
            idToElement.remove(element.getId());
        }
    }

    /**
     * Schedules the given Runnable object for a later invocation in
     * the document's update thread, and returns.
     * <br />
     * If there is no <code>updateQueue</code> the <code>Runnable</code>
     * is run before returning. Otherwise, the <code>Runnable</code>
     * is scheduled in the associated <code>updateQueue</code>
     *
     * @param r the <code>Runnable</code> to put at the end of the
     *        execution list.
     * @throws IllegalStateException if there is an associated 
     *         <code>RunnableQueue</code> but that one has exited
     *         or was not started.
     */
    public void invokeLater(final Runnable r) {
        if (updateQueue == null) {
            r.run();
        } else {
            updateQueue.invokeLater(r, runHandler);
        }
    }

    /**
     * Waits until the given Runnable's <tt>run()</tt> has returned.
     * <em>Note: <tt>invokeAndWait()</tt> must not be called from the
     * current thread (for example from the <tt>run()</tt> method of the
     * argument)</em>.
     *
     * @param r the <code>Runnable</code> to put at the end of the 
     *        execution list.
     * @throws IllegalStateException if there is an associated RunnableQueue
     *         which has exited or was not started.
     * @throws InterruptedException if the thread is interrupted while 
     *         waiting for the input <code>Runnable</code> to complete
     *         its execution.
     */
    public void invokeAndWait(final Runnable r) 
        throws InterruptedException {

        if (updateQueue == null) {
            r.run();
        } else {
            updateQueue.invokeAndWait(r, runHandler);
        }
    }

    /**
     * Waits until the given Runnable's <tt>run()</tt> has returned.
     * <em>Note: <tt>safeInvokeAndWait()</tt> may be called from any thread.
     * This method checks if this thread is the update thread, in which case
     * the Runnable is invoked directly. Otherwise, it delegates to the 
     * invokeAndWait method.
     *
     * @param r the <code>Runnable</code> to put at the end of the 
     *        execution list. Should not be null.
     * @param runHandler the <code>RunnableHandler</code> to notify 
     *        once the <code>Runnable</code> has finished executing.
     *        Should not be null.
     * @throws IllegalStateException if getThread() is null or if the
     *         thread returned by getThread() is the current one.
     */
    public void safeInvokeAndWait(final Runnable r) {
        if (updateQueue == null) {
            r.run();
        } else {
            updateQueue.safeInvokeAndWait(r, runHandler);
        }
    }

    /**
     * This is where font matching happens. This method compares the 
     * font attributes (such as 'font-family' or 'font-weight') with
     * the corresponding attributes in the FontFace set. At a minimum,
     * this method returns a single font in the list: the defaultFontFace.
     *
     * This process follows the algorithm described in section
     * 15.5 of the CSS2 specification (http://www.w3.org/TR/REC-CSS2/)
     *
     * In SVG Tiny, the font attributes on Text are:
     * - font-family
     * - font-size
     * - font-style
     * - font-weight
     *
     * Therefore, font matching is limited to these attributes (i.e.,
     * font-variant is not used). 
     *
     * IMPORTANT NOTE: The FontFaces data base is built when the 
     * setFontFaceSet method is invoked, from the input array. The
     * input array is expected to be in document order and the 
     * fontFaceDB hashtable's value Vectors contain values which are
     * in the same order as in the array passed to setFontFaceSet.
     * Therefore, the resolveFontFaces returns matches in document 
     * order. This is important for situations where a less specific
     * FontFace (font-family:Arial, font-style: any) would appear 
     * before (in document order) a more specific FontFace (font-family:Arial,
     * font-style: italic). In that case, only the less specific FontFace
     * will be the match for a text with font-family set to Arial
     * and font-style set to italic.
     *
     * @param tp The <code>TextProperties</code> containing the font selection
     *        properties.
     * @return a chain of <code>FontFace.Match</code>. The first element is
     *         <b>always</b> the default font face. The next element (if any),
     *         is the first match, ordered according to the CSS2 font matching
     *         rules.
     */
    protected FontFace.Match resolveFontFaces(final TextProperties tp) {
        // If no default font face has been set, set the default one now.
        if (defaultFontFace == null) {
            setDefaultFontFace(DefaultFontFace.getDefaultFontFace());
        }

        // If no initial font face has been set, set the default one now.
        if (initialFontFaces == null) {
            setInitialFontFaces(DefaultFontFace.getInitialFontFaces());
        } 

        // Build the font face data base if it has not been
        // built already
        if (fontFaceDB == null) {
            setFontFaceSetSilent(null);
        }

        // Iterate over each matching font-family name
        Vector fontFamilyMatch = null;
        String[] fontFamily = tp.getFontFamily();
        int nff = 0;
        if (fontFamily != null) {
            nff = fontFamily.length;
        }

        FontFace.Match firstMatch = new FontFace.Match(defaultFontFace);
        FontFace.Match lastMatch = firstMatch;
        for (int i = 0; i < nff; i++) {
            fontFamilyMatch = (Vector) fontFaceDB.get(fontFamily[i]);
            lastMatch = matchFontFaces(fontFamilyMatch, lastMatch, tp);
        }

        return firstMatch;
    }

    /**
     * Matches values in the input fontFamily map against the 
     * current context values.
     * 
     * @param fontFamily the 'candidate' <tt>FontFace</tt>s
     * @param lastMatch the last <code>FontFace.Match</code>. Any new
     *        match sub-chain (i.e., for the input fontFamily) will be 
     *        chained after <code>lastMatch</code>
     * @param tp the <tt>TextProperties</tt> defining the applicable
     *        font selection properties
     *
     * @return the first <code>FontFace.Match</code> node, head of
     *         the chain of matches, linked in precedence order.
     *
     * @see #resolveFontFaces
     */
    protected FontFace.Match matchFontFaces(final Vector fontFamily, 
                                            final FontFace.Match lastMatch,
                                            final TextProperties tp) {
        if (fontFamily == null) {
            return lastMatch;
        }

        int n = fontFamily.size();
        int fontStyle = tp.getFontStyle();
        float fontSize = tp.getFontSize();
        int fontWeight = tp.getFontWeight();

        FontFace.Match match = null, firstMatch = null;
        for (int i = 0; i < n; i++) {
            FontFace ff = (FontFace) fontFamily.elementAt(i);

            // First, match on font-style. 
            if ((ff.getFontStyles() & fontStyle) != 0
                ||
                (
                 (fontStyle == TextNode.FONT_STYLE_ITALIC)
                 &&
                 ((ff.getFontStyles() & TextNode.FONT_STYLE_OBLIQUE) != 0))) {
                     
                // Match on font-size
                if (ff.getFontSizes() == null) {
                    match = new FontFace.Match(ff);
                    match.distance = ff.fontWeightDistance(fontWeight);
                    firstMatch = addMatch(firstMatch, match);
                } else {
                    float[] fs = ff.getFontSizes();
                    for (int j = 0; j < fs.length; j++) {
                        if (fs[j] == fontSize) {
                            match = new FontFace.Match(ff);
                            match.distance = ff.fontWeightDistance(fontWeight);
                            firstMatch = addMatch(firstMatch, match);
                            break;
                        }
                    }
                }
            }
        }

        if (match == null) {
            return lastMatch;
        } else {
            if (lastMatch != null) {
                lastMatch.next = firstMatch;
            }

            while (match.next != null) {
                match = match.next;
            }
            return match;
        }
    }

    /**
     * Chains <code>newMatch</code> into the chain starting with
     * <code>firstMatch. This inserts the new matching font face
     * according to the CSS2 font matching rules.
     *
     * @param firstMatch the first match in the chain of matching
     *        <code>FontFace.Match</code> instances.
     * @param newMatch the new <code>FontFace.Match</code> object to
     *        insert into the chain.
     * @return the head of the <code>FontFace.Match</code> chain after
     *         insertion of the new match.
     */
    protected FontFace.Match addMatch(final FontFace.Match firstMatch,
                                      final FontFace.Match newMatch) {
        if (firstMatch == null) {
            return newMatch;
        }

        FontFace.Match curMatch = firstMatch;
        FontFace.Match prevMatch = null;
        while (curMatch != null && curMatch.distance <= newMatch.distance) {
            prevMatch = curMatch;
            curMatch = curMatch.next;
        }

        if (curMatch == null) {
            // We reached the end of the list, simply append newMatch
            prevMatch.next = newMatch;
            return firstMatch;
        } else {
            // We need to insert newMatch before curMatch
            if (prevMatch == null) {
                // Inserted at the start of the list
                newMatch.next = firstMatch;
                return newMatch;
            } else {
                // Insert somewhere in the middle of the list
                prevMatch.next = newMatch;
                newMatch.next = curMatch;
                return firstMatch;
            }
        }
    }

    /**
     * Sets the default FontFace, i.e., the FontFace which 
     * will always be the last element in the 
     * FontFace array returned from <code>resolveFontFaces</code>
     *
     * @param newDefaultFontFace the fall back font
     * @throws IllegalArgumentException if defaultFontFace is null
     */
    public void setDefaultFontFace(final FontFace newDefaultFontFace) {
        if (newDefaultFontFace == null) {
            throw new IllegalArgumentException();
        }

        if (defaultFontFace == newDefaultFontFace) {
            return;
        }

        this.defaultFontFace = newDefaultFontFace;
        clearLayouts();
    }

    /**
     * Sets the intial set of FontFaces. The intent for this property
     * is to provide a set of FontFaces that match the initial 
     * font-family property for varying values of font-weight and 
     * font-style. However, this can also be used to provide support
     * for the logical font-faces.
     *
     * The RenderGraphics will keep a reference to the input array which
     * should not be modified after it has been passed to this RenderGraphics.
     *
     * @param newInitialFontFaces should not be null and should not contain
     *        null values
     *
     * @throws IllegalArgumentException if initialFontFaces is null or if
     *         one of the array values is null.
     */
    public void setInitialFontFaces(final FontFace[] newInitialFontFaces) {
        if (newInitialFontFaces == null) {
            throw new IllegalArgumentException();
        }

        if (initialFontFaces == newInitialFontFaces) {
            return;
        }

        for (int i = 0; i < newInitialFontFaces.length; i++) {
            if (newInitialFontFaces[i] == null) {
                throw new IllegalArgumentException();
            }
        }

        this.initialFontFaces = newInitialFontFaces;
        clearLayouts();
    }

    /**
     * @return The default FontFace used by this RenderGraphics
     */
    public FontFace getDefaultFontFace() {
        return defaultFontFace;
    }

    /**
     * @return The set of FontFaces used to match the initial font-family
     * value
     */
    public FontFace[] getInitialFontFaces() {
        return initialFontFaces;
    }

    /**
     * Sets the set of FontFaces that this Document uses
     * to display text.
     *
     * @param fontFaceSet the set of font faces to use
     */
    public void setFontFaceSet(final FontFace[] fontFaceSet) {
        setFontFaceSetSilent(fontFaceSet);
        clearLayouts();
    }

    /**
     * Sets the set of FontFaces this document uses but does
     * not generate a modifyingNode notification.
     *
     * @param fontFaceSet the set of font faces to use
     */
    protected void setFontFaceSetSilent(final FontFace[] fontFaceSet) {
        fontFaceDB = new Hashtable();
        growFontFaceDB(fontFaceDB, fontFaceSet);
        growFontFaceDB(fontFaceDB, initialFontFaces);
    }

    /**
     * Adds a new <code>FontFace</code> to the data base
     * available to the document.
     *
     * @param fontFace the new <code>FontFace</code> which is 
     *        now available.
     */
    public void addFontFace(final FontFace fontFace) {
        if (fontFaceDB == null) {
            // This initializes the data base
            setFontFaceSetSilent(null);
        }
        
        growFontFaceDB(fontFaceDB, new FontFace[] {fontFace});
        clearLayouts();
    }

    /**
     * Builds a Hashtable of font faces to map font family 
     * names to FontFace instances.
     * 
     * @param ffDB the font data base to grow
     * @param fontFaceSet array of <tt>FontFace</tt> instances to add
     *        to the font data base.
     * @see #setFontFaceSet
     */
    protected void growFontFaceDB(final Hashtable ffDB, 
                                  final FontFace[] fontFaceSet) {
        if (fontFaceSet == null) {
            return;
        }

        // Grow the font data base from the new fontFaceSet
        for (int i = 0; i < fontFaceSet.length; i++) {
            FontFace ff = fontFaceSet[i];
            String[] fontFamilies = ff.getFontFamilies();
            if (fontFamilies != null) {
                // null is actually a CSS error. But CSS is silent
                // about errors, so we should not break or halt on
                // such a condition which should be expected
                for (int j = 0; j < fontFamilies.length; j++) {
                    Vector v = (Vector) ffDB.get(fontFamilies[j]);
                    if (v == null) {
                        v = new Vector(1);
                        ffDB.put(fontFamilies[j], v);
                        v.addElement(ff);
                    } else {
                        if (!v.contains(ff)) {
                            v.addElement(ff);
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns the current time for the document, i.e., the time at which the 
     * document was last sampled.
     *
     * @return the time at which the container was last sampled.
     */
    public Time getCurrentTime() {
        return timeContainerRootSupport.lastSampleTime;
    }

    /**
     * Applies all currently active animations.
     */
    public void applyAnimations() {
        int n = activeTraitAnims.size();
        // System.err.println(">>>>>>>>>>>>>>>>>> There are : " + n 
        //                      + " active animations");
        for (int i = 0; i < n; i++) {
            // System.err.println("Applying TraitAnim [" + i + "]");
            ((TraitAnim) activeTraitAnims.elementAt(i)).apply();
        }
    }

    /**
     * Applies all currently active media if this document is playing.
     */
    void applyMedia() {
        /*
        if (playing) {
            int n = activeMediaElements.size();
            for (int i = n - 1; i >= 0; i--) {
                ((MediaElement) activeMediaElements.elementAt(i)).playMedia();
            }
        } else {
            int n = activeMediaElements.size();
            for (int i = n - 1; i >= 0; i--) {
                ((MediaElement) activeMediaElements.elementAt(i)).endMedia();
            }
        }
        */
    }

    /**
     * This method is typically called by this element's time container
     * when it samples.
     *
     * Note that if this element is not in the waiting or playing
     * state, this does nothing. This method assumes that successive
     * calls are made with increasing time values.
     *
     * @param currentTime the time at which this element should be 
     *        sampled. 
     */
    public void sample(final Time currentTime) {
        timeContainerRootSupport.sample(currentTime);
        // System.err.println(">>>>>>>>>>>>>>>>>> currentTime : " 
        //                      + currentTime);
        // timeContainerRootSupport.dump();
    }

    /**
     * Increments the animation or media timeline for this SVGImage (in
     * seconds). As the name implies, this method is intended to move only
     * forward in the timeline and typically should be used to animate SVG
     * content in the "one-shot" rendering mode. Setting negative values will
     * throw an Exception. It is important to note that setting large increments
     * of time would result in dropping or skipping of frames as per the SVG
     * animation model.
     *
     * @throws IllegalArgumentException if the specified time is negative.
     */
    public void incrementTime(float seconds) {
        if (seconds < 0) {
            throw new IllegalArgumentException();
        }

        long lastSampleTime = timeContainerRootSupport
            .lastSampleTime.value;
        timeContainerRootSupport.sample
            (new Time(lastSampleTime + (long) (seconds*1000)));
    }


    /**
     * Initializes the timing engine.
     */
    public void initializeTimingEngine() {
        timeContainerRootSupport.initialize();
    }

    /**
     * Debug helper
     * 
     * @return a textual description of this viewport object
     */
    /*
    public String toString() {
        return "[Document(zoomPan=" + zoomAndPan + ", width=" 
            + width + " height=" + height 
            + " txf=" + transform
            + "]";
    }
    */

    /**
     * Traces this viewport tree
     */
    public void dump() {
        dump(this, "", System.err);
    }

    /**
     * Debug: traces the input ModelNode, using the input prefix
     * 
     * @param n the node to dump
     * @param prefix the string used to prefix the node information
     * @param out the stream where the node structure is dumped.
     */
    static void dump(final ModelNode n, final String prefix,
                            final PrintStream out) {
        out.print(prefix + " " + n);
        if (n instanceof ElementNode) {
            ElementNode e = (ElementNode) n;
            String pfx = n.ownerDocument.toPrefix(e.getNamespaceURI(), e);
            if (pfx == null || pfx.length() == 0) {
                out.println(" <" + e.getLocalName() + ">");
            } else {
                out.println(" <" + pfx + ":" + e.getLocalName() + ">");
            }
        } else {
            out.println();
        }
        ModelNode child = n.getFirstChildNode();
        while (child != null) {
            dump(child, prefix + "+-->", out);
            child = child.nextSibling;
        }

        child = n.getFirstExpandedChild();
        while (child != null) {
            dump(child, prefix + "*~~>", out);
            child = child.nextSibling;
        }
    }

    /**
     * @return null as per the DOM Level 2 specification for Document
     *         nodes.
     */
    public String getNamespaceURI() {
        return null;
    }

    /**
     * @return returns the unprefixed node name. For an SVGElement, this returns
     * the tag name without a prefix.  In case of the Document node, string
     * <code>null/code> is returned.
     */
    public String getLocalName() {
        return null;
    }

    // JAVADOC COMMENT ELIDED
    public Element createElementNS(String namespaceURI, 
                                   final String qualifiedName)
        throws DOMException {
        if (namespaceURI == null || qualifiedName == null) {
            throw new NullPointerException();
        }

        // Extract qualified name from the local name.
        String localName = qualifiedName;
        int pi = qualifiedName.indexOf(':');
        if (pi != -1) {
            if (pi == localName.length() - 1) {
                // Namepace prefix separator is at the end of the qualified name
                localName = "";
            } else {
                localName = qualifiedName.substring(pi + 1);
            }
        }

        //
        // If the namespaceURI is empty, we have two cases:
        // 1. (extremely rare) the content maps some prefix, or the default
        //    namespace, to the "" namespace URI.
        // 2. (extremely common) the content does not have an xmlns declaration
        //    on its root element and the parser reported the root element 
        //    and children as being in the "" namespace URI.
        //
        // The following code means that the situation 1. is _not_ handled. If
        // the author mapped a prefix or the default prefix to the 
        // empty string URI, that will be overriddent by the default namespace.
        if (namespaceURI.length() == 0) {
            namespaceURI = defaultNamespaceURI;
        }

        Hashtable lmap = (Hashtable) namespaceMap.get(namespaceURI);
        ElementNode en = null;
        if (lmap != null) {
            en = (ElementNode) lmap.get(localName);
            if (en != null) {
                en = en.newInstance(this);
            } 
        } 

        if (en == null) {
            checkNCName(localName);
            en = new GenericElementNode(namespaceURI, localName, this);
        }

        return en;
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

        // NCName        ::=  (Letter | '_') (NCNameChar)*
        // NCNameChar 	 ::=  Letter | Digit | '.' | '-' | '_' | CombiningChar 
        //                      | Extender
        char c = name.charAt(0);
        if (!isLetter(c) && c != '_') {
            throw unsupportedTrait(name);
        }

        for (int i = 1; i < name.length(); i++) {
            c = name.charAt(i);
            if (!isNCNameChar(c)) {
                throw unsupportedTrait(name);
            }
        }
    }

    final static boolean isNCNameChar(final char c) {
        return isLetter(c) 
            || Character.isDigit(c) 
            || c == '.'
            || c == '-'
            || c == '_'
            || isCombiningChar(c)
            || isExtender(c);    
    }
    
    final static boolean isExtender(final int c) {
        return c == 0x00B7 
            || c == 0x02D0 
            || c == 0x02D1 
            || c == 0x0387 
            || c == 0x0640 
            || c == 0x0E46 
            || c == 0x0EC6 
            || c == 0x3005 
            || (c >= 0x3031 && c <= 0x3035) 
            || (c >= 0x309D && c <= 0x309E) 
            || (c >= 0x30FC && c <= 0x30FE);
    }

    final static boolean isCombiningChar(final int c) {
        return (c >= 0x0300 && c <= 0x0345) 
            || (c >= 0x0360 && c <= 0x0361) 
            || (c >= 0x0483 && c <= 0x0486) 
            || (c >= 0x0591 && c <= 0x05A1) 
            || (c >= 0x05A3 && c <= 0x05B9) 
            || (c >= 0x05BB && c <= 0x05BD) 
            || c == 0x05BF 
            || (c >= 0x05C1 && c <= 0x05C2) 
            || c == 0x05C4 
            || (c >= 0x064B && c <= 0x0652) 
            || c == 0x0670 
            || (c >= 0x06D6 && c <= 0x06DC) 
            || (c >= 0x06DD && c <= 0x06DF) 
            || (c >= 0x06E0 && c <= 0x06E4) 
            || (c >= 0x06E7 && c <= 0x06E8) 
            || (c >= 0x06EA && c <= 0x06ED) 
            || (c >= 0x0901 && c <= 0x0903) 
            || c == 0x093C 
            || (c >= 0x093E && c <= 0x094C) 
            || c == 0x094D 
            || (c >= 0x0951 && c <= 0x0954) 
            || (c >= 0x0962 && c <= 0x0963) 
            || (c >= 0x0981 && c <= 0x0983) 
            || c == 0x09BC 
            || c == 0x09BE 
            || c == 0x09BF 
            || (c >= 0x09C0 && c <= 0x09C4) 
            || (c >= 0x09C7 && c <= 0x09C8) 
            || (c >= 0x09CB && c <= 0x09CD) 
            || c == 0x09D7 
            || (c >= 0x09E2 && c <= 0x09E3) 
            || c == 0x0A02 
            || c == 0x0A3C 
            || c == 0x0A3E 
            || c == 0x0A3F 
            || (c >= 0x0A40 && c <= 0x0A42) 
            || (c >= 0x0A47 && c <= 0x0A48) 
            || (c >= 0x0A4B && c <= 0x0A4D) 
            || (c >= 0x0A70 && c <= 0x0A71) 
            || (c >= 0x0A81 && c <= 0x0A83) 
            || c == 0x0ABC 
            || (c >= 0x0ABE && c <= 0x0AC5) 
            || (c >= 0x0AC7 && c <= 0x0AC9) 
            || (c >= 0x0ACB && c <= 0x0ACD) 
            || (c >= 0x0B01 && c <= 0x0B03) 
            || c == 0x0B3C 
            || (c >= 0x0B3E && c <= 0x0B43) 
            || (c >= 0x0B47 && c <= 0x0B48) 
            || (c >= 0x0B4B && c <= 0x0B4D) 
            || (c >= 0x0B56 && c <= 0x0B57) 
            || (c >= 0x0B82 && c <= 0x0B83) 
            || (c >= 0x0BBE && c <= 0x0BC2) 
            || (c >= 0x0BC6 && c <= 0x0BC8) 
            || (c >= 0x0BCA && c <= 0x0BCD) 
            || c == 0x0BD7 
            || (c >= 0x0C01 && c <= 0x0C03) 
            || (c >= 0x0C3E && c <= 0x0C44) 
            || (c >= 0x0C46 && c <= 0x0C48) 
            || (c >= 0x0C4A && c <= 0x0C4D) 
            || (c >= 0x0C55 && c <= 0x0C56) 
            || (c >= 0x0C82 && c <= 0x0C83) 
            || (c >= 0x0CBE && c <= 0x0CC4) 
            || (c >= 0x0CC6 && c <= 0x0CC8) 
            || (c >= 0x0CCA && c <= 0x0CCD) 
            || (c >= 0x0CD5 && c <= 0x0CD6) 
            || (c >= 0x0D02 && c <= 0x0D03) 
            || (c >= 0x0D3E && c <= 0x0D43) 
            || (c >= 0x0D46 && c <= 0x0D48) 
            || (c >= 0x0D4A && c <= 0x0D4D) 
            || c == 0x0D57 
            || c == 0x0E31 
            || (c >= 0x0E34 && c <= 0x0E3A) 
            || (c >= 0x0E47 && c <= 0x0E4E) 
            || c == 0x0EB1 
            || (c >= 0x0EB4 && c <= 0x0EB9) 
            || (c >= 0x0EBB && c <= 0x0EBC) 
            || (c >= 0x0EC8 && c <= 0x0ECD) 
            || (c >= 0x0F18 && c <= 0x0F19) 
            || c == 0x0F35 
            || c == 0x0F37 
            || c == 0x0F39 
            || c == 0x0F3E 
            || c == 0x0F3F 
            || (c >= 0x0F71 && c <= 0x0F84) 
            || (c >= 0x0F86 && c <= 0x0F8B) 
            || (c >= 0x0F90 && c <= 0x0F95) 
            || c == 0x0F97 
            || (c >= 0x0F99 && c <= 0x0FAD) 
            || (c >= 0x0FB1 && c <= 0x0FB7) 
            || c == 0x0FB9 
            || (c >= 0x20D0 && c <= 0x20DC) 
            || c == 0x20E1 
            || (c >= 0x302A && c <= 0x302F) 
            || c == 0x3099 
            || c == 0x309A;
    }

    final static boolean isLetter(final int c) {
        return isIdeographic(c) || isBaseChar(c);
    }

    final static boolean isIdeographic(final int c) {
	return (c >= 0x4E00 && c <= 0x9FA5) 
            || c == 0x3007 
            || (c >= 0x3021 && c <= 0x3029);
    }

    final static boolean isBaseChar(final int c) {
 	return (c >= 0x0041 && c <= 0x005A) 
            || (c >= 0x0061 && c <= 0x007A) 
            || (c >= 0x00C0 && c <= 0x00D6) 
            || (c >= 0x00D8 && c <= 0x00F6) 
            || (c >= 0x00F8 && c <= 0x00FF) 
            || (c >= 0x0100 && c <= 0x0131) 
            || (c >= 0x0134 && c <= 0x013E) 
            || (c >= 0x0141 && c <= 0x0148) 
            || (c >= 0x014A && c <= 0x017E) 
            || (c >= 0x0180 && c <= 0x01C3) 
            || (c >= 0x01CD && c <= 0x01F0) 
            || (c >= 0x01F4 && c <= 0x01F5) 
            || (c >= 0x01FA && c <= 0x0217) 
            || (c >= 0x0250 && c <= 0x02A8) 
            || (c >= 0x02BB && c <= 0x02C1) 
            || c == 0x0386 
            || (c >= 0x0388 && c <= 0x038A) 
            || c == 0x038C 
            || (c >= 0x038E && c <= 0x03A1) 
            || (c >= 0x03A3 && c <= 0x03CE) 
            || (c >= 0x03D0 && c <= 0x03D6) 
            || c == 0x03DA 
            || c == 0x03DC 
            || c == 0x03DE 
            || c == 0x03E0 
            || (c >= 0x03E2 && c <= 0x03F3) 
            || (c >= 0x0401 && c <= 0x040C) 
            || (c >= 0x040E && c <= 0x044F) 
            || (c >= 0x0451 && c <= 0x045C) 
            || (c >= 0x045E && c <= 0x0481) 
            || (c >= 0x0490 && c <= 0x04C4) 
            || (c >= 0x04C7 && c <= 0x04C8) 
            || (c >= 0x04CB && c <= 0x04CC) 
            || (c >= 0x04D0 && c <= 0x04EB) 
            || (c >= 0x04EE && c <= 0x04F5) 
            || (c >= 0x04F8 && c <= 0x04F9) 
            || (c >= 0x0531 && c <= 0x0556) 
            || c == 0x0559 
            || (c >= 0x0561 && c <= 0x0586) 
            || (c >= 0x05D0 && c <= 0x05EA) 
            || (c >= 0x05F0 && c <= 0x05F2) 
            || (c >= 0x0621 && c <= 0x063A) 
            || (c >= 0x0641 && c <= 0x064A) 
            || (c >= 0x0671 && c <= 0x06B7) 
            || (c >= 0x06BA && c <= 0x06BE) 
            || (c >= 0x06C0 && c <= 0x06CE) 
            || (c >= 0x06D0 && c <= 0x06D3) 
            || c == 0x06D5 
            || (c >= 0x06E5 && c <= 0x06E6) 
            || (c >= 0x0905 && c <= 0x0939) 
            || c == 0x093D 
            || (c >= 0x0958 && c <= 0x0961) 
            || (c >= 0x0985 && c <= 0x098C) 
            || (c >= 0x098F && c <= 0x0990) 
            || (c >= 0x0993 && c <= 0x09A8) 
            || (c >= 0x09AA && c <= 0x09B0) 
            || c == 0x09B2 
            || (c >= 0x09B6 && c <= 0x09B9) 
            || (c >= 0x09DC && c <= 0x09DD) 
            || (c >= 0x09DF && c <= 0x09E1) 
            || (c >= 0x09F0 && c <= 0x09F1) 
            || (c >= 0x0A05 && c <= 0x0A0A) 
            || (c >= 0x0A0F && c <= 0x0A10) 
            || (c >= 0x0A13 && c <= 0x0A28) 
            || (c >= 0x0A2A && c <= 0x0A30) 
            || (c >= 0x0A32 && c <= 0x0A33) 
            || (c >= 0x0A35 && c <= 0x0A36) 
            || (c >= 0x0A38 && c <= 0x0A39) 
            || (c >= 0x0A59 && c <= 0x0A5C) 
            || c == 0x0A5E 
            || (c >= 0x0A72 && c <= 0x0A74) 
            || (c >= 0x0A85 && c <= 0x0A8B) 
            || c == 0x0A8D 
            || (c >= 0x0A8F && c <= 0x0A91) 
            || (c >= 0x0A93 && c <= 0x0AA8) 
            || (c >= 0x0AAA && c <= 0x0AB0) 
            || (c >= 0x0AB2 && c <= 0x0AB3) 
            || (c >= 0x0AB5 && c <= 0x0AB9) 
            || c == 0x0ABD 
            || c == 0x0AE0 
            || (c >= 0x0B05 && c <= 0x0B0C) 
            || (c >= 0x0B0F && c <= 0x0B10) 
            || (c >= 0x0B13 && c <= 0x0B28) 
            || (c >= 0x0B2A && c <= 0x0B30) 
            || (c >= 0x0B32 && c <= 0x0B33) 
            || (c >= 0x0B36 && c <= 0x0B39) 
            || c == 0x0B3D 
            || (c >= 0x0B5C && c <= 0x0B5D) 
            || (c >= 0x0B5F && c <= 0x0B61) 
            || (c >= 0x0B85 && c <= 0x0B8A) 
            || (c >= 0x0B8E && c <= 0x0B90) 
            || (c >= 0x0B92 && c <= 0x0B95) 
            || (c >= 0x0B99 && c <= 0x0B9A) 
            || c == 0x0B9C 
            || (c >= 0x0B9E && c <= 0x0B9F) 
            || (c >= 0x0BA3 && c <= 0x0BA4) 
            || (c >= 0x0BA8 && c <= 0x0BAA) 
            || (c >= 0x0BAE && c <= 0x0BB5) 
            || (c >= 0x0BB7 && c <= 0x0BB9) 
            || (c >= 0x0C05 && c <= 0x0C0C) 
            || (c >= 0x0C0E && c <= 0x0C10) 
            || (c >= 0x0C12 && c <= 0x0C28) 
            || (c >= 0x0C2A && c <= 0x0C33) 
            || (c >= 0x0C35 && c <= 0x0C39) 
            || (c >= 0x0C60 && c <= 0x0C61) 
            || (c >= 0x0C85 && c <= 0x0C8C) 
            || (c >= 0x0C8E && c <= 0x0C90) 
            || (c >= 0x0C92 && c <= 0x0CA8) 
            || (c >= 0x0CAA && c <= 0x0CB3) 
            || (c >= 0x0CB5 && c <= 0x0CB9) 
            || c == 0x0CDE 
            || (c >= 0x0CE0 && c <= 0x0CE1) 
            || (c >= 0x0D05 && c <= 0x0D0C) 
            || (c >= 0x0D0E && c <= 0x0D10) 
            || (c >= 0x0D12 && c <= 0x0D28) 
            || (c >= 0x0D2A && c <= 0x0D39) 
            || (c >= 0x0D60 && c <= 0x0D61) 
            || (c >= 0x0E01 && c <= 0x0E2E) 
            || c == 0x0E30 
            || (c >= 0x0E32 && c <= 0x0E33) 
            || (c >= 0x0E40 && c <= 0x0E45) 
            || (c >= 0x0E81 && c <= 0x0E82) 
            || c == 0x0E84 
            || (c >= 0x0E87 && c <= 0x0E88) 
            || c == 0x0E8A 
            || c == 0x0E8D 
            || (c >= 0x0E94 && c <= 0x0E97) 
            || (c >= 0x0E99 && c <= 0x0E9F) 
            || (c >= 0x0EA1 && c <= 0x0EA3) 
            || c == 0x0EA5 
            || c == 0x0EA7 
            || (c >= 0x0EAA && c <= 0x0EAB) 
            || (c >= 0x0EAD && c <= 0x0EAE) 
            || c == 0x0EB0 
            || (c >= 0x0EB2 && c <= 0x0EB3) 
            || c == 0x0EBD 
            || (c >= 0x0EC0 && c <= 0x0EC4) 
            || (c >= 0x0F40 && c <= 0x0F47) 
            || (c >= 0x0F49 && c <= 0x0F69) 
            || (c >= 0x10A0 && c <= 0x10C5) 
            || (c >= 0x10D0 && c <= 0x10F6) 
            || c == 0x1100 
            || (c >= 0x1102 && c <= 0x1103) 
            || (c >= 0x1105 && c <= 0x1107) 
            || c == 0x1109 
            || (c >= 0x110B && c <= 0x110C) 
            || (c >= 0x110E && c <= 0x1112) 
            || c == 0x113C 
            || c == 0x113E 
            || c == 0x1140 
            || c == 0x114C 
            || c == 0x114E 
            || c == 0x1150 
            || (c >= 0x1154 && c <= 0x1155) 
            || c == 0x1159 
            || (c >= 0x115F && c <= 0x1161) 
            || c == 0x1163 
            || c == 0x1165 
            || c == 0x1167 
            || c == 0x1169 
            || (c >= 0x116D && c <= 0x116E) 
            || (c >= 0x1172 && c <= 0x1173) 
            || c == 0x1175 
            || c == 0x119E 
            || c == 0x11A8 
            || c == 0x11AB 
            || (c >= 0x11AE && c <= 0x11AF) 
            || (c >= 0x11B7 && c <= 0x11B8) 
            || c == 0x11BA 
            || (c >= 0x11BC && c <= 0x11C2) 
            || c == 0x11EB 
            || c == 0x11F0 
            || c == 0x11F9 
            || (c >= 0x1E00 && c <= 0x1E9B) 
            || (c >= 0x1EA0 && c <= 0x1EF9) 
            || (c >= 0x1F00 && c <= 0x1F15) 
            || (c >= 0x1F18 && c <= 0x1F1D) 
            || (c >= 0x1F20 && c <= 0x1F45) 
            || (c >= 0x1F48 && c <= 0x1F4D) 
            || (c >= 0x1F50 && c <= 0x1F57) 
            || c == 0x1F59 
            || c == 0x1F5B 
            || c == 0x1F5D 
            || (c >= 0x1F5F && c <= 0x1F7D) 
            || (c >= 0x1F80 && c <= 0x1FB4) 
            || (c >= 0x1FB6 && c <= 0x1FBC) 
            || c == 0x1FBE 
            || (c >= 0x1FC2 && c <= 0x1FC4) 
            || (c >= 0x1FC6 && c <= 0x1FCC) 
            || (c >= 0x1FD0 && c <= 0x1FD3) 
            || (c >= 0x1FD6 && c <= 0x1FDB) 
            || (c >= 0x1FE0 && c <= 0x1FEC) 
            || (c >= 0x1FF2 && c <= 0x1FF4) 
            || (c >= 0x1FF6 && c <= 0x1FFC) 
            || c == 0x2126 
            || (c >= 0x212A && c <= 0x212B) 
            || c == 0x212E 
            || (c >= 0x2180 && c <= 0x2182) 
            || (c >= 0x3041 && c <= 0x3094) 
            || (c >= 0x30A1 && c <= 0x30FA) 
            || (c >= 0x3105 && c <= 0x312C) 
            || (c >= 0xAC00 && c <= 0xD7A3);
    }

    /**
     * To support the creation of elements in the <code>createElementNS</code>
     * method, we supply prototypes to the <code>DocumentNode</code> so that it
     * can create nodes of specific data types for the prototype's namespaceURI
     * and localName.
     *
     * @param prototypeElement the <code>ElementNode</code> which will be used
     * as a prototype in <code>createElementNS</code>. Should not be null.
     * @throws IllegalArgumentException If there is already a prototype node for
     * the given namespace and local name.
     */
    public void addPrototype(final ElementNode prototypeElement) {
        // Get the namespace map
        String namespaceURI = prototypeElement.getNamespaceURI();

        Hashtable lmap = (Hashtable) namespaceMap.get(namespaceURI);

        if (lmap == null) {
            lmap = new Hashtable();
            namespaceMap.put(namespaceURI, lmap);
        }

        lmap.put(prototypeElement.getLocalName(), prototypeElement);
    }

    /**
     * For SVG files this must be
     * <code>SVGSVGElement</code>, but return type is Element for DOM Core
     * compatibility and to allow for future extensions.  Return null if
     * document does not have an element child.
     *
     */

    public Element getDocumentElement() {
        return firstChild;
    }


    /**
     * Returns the parent <code>Node</code> of this <code>Node</code>.
     *
     * @return the parent node or null if there is no parent (i.e. if a node has
     * just been created and not yet added to the tree, or if it has been
     * removed from the tree, this is null).
     */
    public Node getParentNode() {
        return null;
    }

    /**
     * @return false, as DocumentNode does not support removing children.
     */
    protected boolean isRemoveChildSupported() {
        return false;
    }

    /**
     * Only and SVG child is allowed under a DocumentNode.
     *
     * @param node the candidate child node.
     * @return true if the input node can be inserted under this CompositeNode
     */
    protected boolean isAllowedChild(final ElementNode node) {
        if (node instanceof SVG) {
            return true;
        }
        return false;
    }

    // =========================================================================
    // Namespace prefix management. Note that this is designed to be minimal
    // in terms of memory and is not optimized for speed.
    // =========================================================================

    /**
     * Adds a new prefix to namespace mapping. The scope is provided by 
     * the node parameter.
     *
     * @param prefix the new namespace prefix.
     * @param uri the new namespace URI which maps to the prefix.
     * @param node the scope of the namespace prefix mapping. The mapping 
     *        applies to all children, unless overridden.
     */
    public void addNamespacePrefix(final String prefix,
                                   final String namespaceURI,
                                   final ModelNode node) {
        if (prefix == null) {
            throw new NullPointerException();
        }

        // First, check if there are already mappings for the 
        // prefix.
        Object[][] namespaceEntry = (Object[][]) prefixes.get(prefix);

        if (namespaceEntry == null) {
            // Simple case: there is not entry yet. Create a new one.
            namespaceEntry = new Object[][] {
                {namespaceURI, node} };
        } else {
            // There is an existing entry. Add the new one ahead of the
            // other ones.
            Object[][] newNamespaceEntry = 
                    new Object[namespaceEntry.length + 1][];
            newNamespaceEntry[0] = new Object[] {namespaceURI, node};
            System.arraycopy(namespaceEntry, 0, newNamespaceEntry, 1, 
                             namespaceEntry.length);
            namespaceEntry = newNamespaceEntry;
        }

        prefixes.put(prefix, namespaceEntry);

        // Now, update the namespaces map.
        Object[][] prefixEntry = (Object[][]) namespaces.get(namespaceURI);
        
        if (prefixEntry == null) {
            // Simple case: there is no entry yet. Create a new one.
            prefixEntry = new Object[][] {
                {prefix, node} };
        } else {
            // There is an existing entry. Add the new one ahead of the other
            // ones.
            Object[][] newPrefixEntry = new Object[prefixEntry.length + 1][];
            newPrefixEntry[0] = new Object[] {prefix, node};
            System.arraycopy(prefixEntry, 0, newPrefixEntry, 1, 
                             prefixEntry.length);
            prefixEntry = newPrefixEntry;
        }

        namespaces.put(namespaceURI, prefixEntry);
    }

    /**
     * Maps the input prefix name to a namespace value.
     *
     * @param prefix the prefix to map.
     * @param node the node for which the prefix needs to be mapped.
     * @return the namespace the prefix maps to for the node, or null if 
     *         there is no such namespace prefix.
     */
    String toNamespace(final String prefix, final ModelNode node) {
        Object[][] namespaceEntry = (Object[][]) prefixes.get(prefix);
        if (namespaceEntry == null) {
            // No namespace entry
            return null;
        } else {
            // Note that namespaceEntry.length == 0 should _never_ happen.

            // There are multiple prefix that map to entries. 
            // Walk up the parent tree and match with the 
            // first parent that is found in an entry.
            ModelNode cur = node;
            final int n = namespaceEntry.length;
            while (cur != null && cur != this) {
                for (int i = 0; i < n; i++) {
                    if (namespaceEntry[i][1] == cur) {
                        return (String) namespaceEntry[i][0];
                    }
                }
                cur = cur.parent;
            }

            // If we are here, it means we have not found a 
            // matching namespace in the document tree. Check
            // if there are any default mapping on the document
            // node itself.
            for (int i = 0; i < n; i++) {
                if (namespaceEntry[i][1] == this) {
                    return (String) namespaceEntry[i][0];
                }
            }
            
            // Did not find any matching namespace prefix.
            return null;
        }
    }


    /**
     * Maps the input namespace value to a prefix.
     *
     * @param namespaceURI the URI to map.
     * @param node the node for which the namespace needs to be mapped.
     * @return the namespace the prefix maps to for the node, or null if 
     *         there is no such namespace prefix.
     */
    public String toPrefix(final String namespaceURI, final Element node) {
        Object[][] prefixEntry = (Object[][]) namespaces.get(namespaceURI);
        if (prefixEntry == null) {
            // No prefix entry
            return null;
        } else {
            // Note that prefixEntry.length == 0 should _never_ happen.

            // There are multiple prefixEntries that map to entries. 
            // Walk up the parent tree and match with the 
            // first parent that is found in an entry.
            ModelNode cur = (ElementNode) node;
            final int n = prefixEntry.length;
            while (cur != null && cur != this) {
                for (int i = 0; i < n; i++) {
                    if (prefixEntry[i][1] == cur) {
                        return (String) prefixEntry[i][0];
                    }
                }
                cur = cur.parent;
            }

            // If we are here, it means we have not found a 
            // matching prefix in the document tree. Check
            // if there are any default mapping on the document
            // node itself.
            for (int i = 0; i < n; i++) {
                if (prefixEntry[i][1] == this) {
                    return (String) prefixEntry[i][0];
                }
            }
            
            // Did not find any matching prefix.
            return null;
        }
    }

    /**
     * Invoked at the end of the parsing stage to validate things which cannot
     * be validated earlier, such as unresolved use references or invalid
     * animation settings.
     *
     * @throws DOMException if there are validation errors.
     */
    public void validate() throws DOMException {
        // First, check unresolved ID references.
        if (unresolvedIDRefs != null && unresolvedIDRefs.size() > 0) {
            // There are unresolved ID references, this is a validation error.
            Enumeration iter = unresolvedIDRefs.keys();
            StringBuffer buf = new StringBuffer();
            while (iter.hasMoreElements()) {
                buf.append('[');
                buf.append(iter.nextElement());
                buf.append(']');
            }

            String message 
                = Messages.formatMessage
                (Messages.ERROR_UNRESOLVED_REFERENCES, 
                 new Object[] {buf.toString()});

            throw new DOMException(DOMException.INVALID_ACCESS_ERR,
                                   message);
        }

        unresolvedIDRefs = null;

        // Now, validate animation elements. At this stage, we know that the 
        // animation elements either had no xlink:href or had one which has
        // been resolved.
        if (animations != null) {
            int n = animations.size();
            for (int i = 0; i < n; i++) {
                Animation animation = (Animation) animations.elementAt(i);
                if (animation.parent != null) {
                    // The prototypes Set may have a null parent, so we account
                    // for that situation here.
                    animation.validate();
                }
            }
        }
        
        animations = null;
    }

    /**
     * Implementation helper. Checks that the unknownTraitNS map is not null for
     * the given ElementNode before using it, for the requested namespaceURI.
     *
     * @param element the ElementNode for which a table should be created for 
     *        the given namespace URI.
     * @param namespaceURI the trait's namespace URI
     * @param traitName the trait's local name.
     * @param value the trait value.
     * @return the namespaceURI's unknown traits table.
     */
    void setUnknownTraitsNS(final ElementNode element, 
                            final String namespaceURI,
                            final String traitName,
                            final String value) {
        // Make sure we do have a table for storing unknown traits.        
        if (unknownTraitsNS == null) {
            unknownTraitsNS = new Hashtable();
        }

        // Make sure we do have a table for storing unknown traits for
        // the requested element.
        Hashtable eltMap = (Hashtable) unknownTraitsNS.get(element);
        if (eltMap == null) {
            eltMap = new Hashtable();
            unknownTraitsNS.put(element, eltMap);
        }

        // If there is already a map for the given namespace, use that.
        // Otherwise, create a new map.
        Hashtable nsMap = (Hashtable) eltMap.get(namespaceURI);
        if (nsMap == null) {
            nsMap = new Hashtable();
            eltMap.put(namespaceURI, nsMap);
        }

        nsMap.put(traitName, value);
    }

    /**
     * Implementation helper. Returns the ElementNode's trait value if
     * it was ever set.
     *
     * @param element the ElementNode on which the trait might be set.
     * @param namespaceURI the trait's namespace URI
     * @param traitName the trait's local name.
     * @return the trait value or null if the value was never set.
     */
    String getUnknownTraitsNS(final ElementNode element, 
                              final String namespaceURI,
                              final String traitName) {
        if (unknownTraitsNS == null) {
            return null;
        }

        Hashtable eltMap = (Hashtable) unknownTraitsNS.get(element);
        if (eltMap == null) {
            return null;
        }

        Hashtable nsMap = (Hashtable) eltMap.get(namespaceURI);
        if (nsMap == null) {
            return null;
        }

        return (String) nsMap.get(traitName);
    }


}
