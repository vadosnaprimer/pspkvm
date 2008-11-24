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

import com.sun.perseus.platform.GZIPSupport;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.DOMException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.Vector;

import com.sun.perseus.util.SVGConstants;

import com.sun.perseus.builder.ModelBuilder;
import com.sun.perseus.builder.SVGTinyModelFactory;

import javax.microedition.m2g.SVGImage;
import javax.microedition.m2g.ScalableImage;
import javax.microedition.m2g.ExternalResourceHandler;

import org.xml.sax.SAXParseException;


import com.sun.perseus.j2d.ImageLoaderUtil;
import com.sun.perseus.j2d.RasterImage;

import com.sun.perseus.model.DocumentNode;
import com.sun.perseus.model.ElementNode;
import com.sun.perseus.model.ModelEvent;
import com.sun.perseus.model.ModelNode;
import com.sun.perseus.model.SVG;
import com.sun.perseus.model.Time;
import com.sun.perseus.model.UpdateAdapter;

/**
 *
 */
public class SVGImageImpl extends SVGImage {
    /**
     * Size of the byte buffer used to read image input streams handed
     * by ExternalResourceHandler implementations.
     */
    static final int DEFAULT_IMAGE_READ_BUFFER_LENGTH = 64;

    /**
    * The image document
    */
    DocumentNode documentNode = null;

    /**
    * The default width for an empty SVG image
    */
    final static public int DEFAULT_WIDTH = 100;

    /**
    * The default height for an empty SVG image
    */
    final static public int DEFAULT_HEIGHT = 100;

    /**
     * The SVGImageLoader
     */
    SVGImageLoader svgImageLoader;

    /**
     * The current SVGElement with the focus
     */
    SVGElement lastElement = null;

    /**
     * URI requested by SVGImageLoader's getImageAndWait().
     */
    String waitURI = new String();

    /**
     * Received null resourceData in requestCompleted().  Used in
     * getImageAndWait() case.
     */
    boolean isBrokenImage = false;

    /**
     * Returns the associated <code>Document</code>.
     *
     * @return the associated <code>Document</code>.
     */
    public Document getDocument() {
        return (Document) documentNode;
    }

    /**
     * Private constructor. Requires a non null DocumentNode.
     *
     * @param documentNode the associated DocumentNode. Should not be null.
     * @param ExternalResourceHandler the associated handler. 
     */
    private SVGImageImpl(final DocumentNode documentNode,
                         final ExternalResourceHandler handler) {
        this.documentNode = documentNode;

        if (handler != null) {
            svgImageLoader = new SVGImageLoader(this, handler);
            documentNode.setImageLoader(svgImageLoader);
        } else {
            documentNode.setImageLoader(new DefaultImageLoader());
        }
    }

    // JAVADOC COMMENT ELIDED
    public static SVGImage createEmptyImage(ExternalResourceHandler handler) {
        DocumentNode documentNode = new DocumentNode();

        SVG svg = new SVG(documentNode);
        svg.setWidth((float) DEFAULT_WIDTH);
        svg.setHeight((float) DEFAULT_HEIGHT);
        documentNode.add(svg);

        documentNode.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);

        SVGImageImpl sii = new SVGImageImpl(documentNode, handler);

        // Add prototypes for the SVG Tiny elements.
        Vector prototypes = SVGTinyModelFactory.getPrototypes(documentNode);
        int n = prototypes.size();
        for (int i = 0; i < n; i++) {
            documentNode.addPrototype((ElementNode) prototypes.elementAt(i));
        }

        // Initialize the timing engine and sample at time zero.
        documentNode.initializeTimingEngine();
        documentNode.sample(new Time(0));
        documentNode.setLoaded(true);

        return sii;
    }

    /**
     * This method is used to dispatch a mouse event of the specified
     * <code>type</code> to the document. The mouse position is given as screen
     * coordinates <code>x, y</code>. The only mouse event type supported is
     * "click". Note that when a "click" event is dispatched, a "DOMActivate" is
     * automatically dispatched by the underlying implementation. If a different
     * type is specified, a DOMException with error code NOT_SUPPORTED_ERR is
     * thrown. In the case, where x, y values are outside the viewport area or
     * no target is available for the x, y coordinates, the event is not
     * dispatched.
     *
     *
     * @param type the type of mouse event.
     * @param x the x location of the mouse/pointer in viewport coordinate
     *        system.
     * @param y the y location of the mouse/pointer in viewport coordinate
     *        system.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERR: if the event 
     * <code>type</code> is not supported.
     * @throws NullPointerException if <code>type</code> is null.
     * @throws IllegalArgumentException if the x or y values are negative.
     *
     */
    public void dispatchMouseEvent(String type, int x, int y)
        throws NullPointerException, IllegalArgumentException, DOMException {

        if (type == null) {
            throw new NullPointerException();
        }

        if (x < 0 || y < 0) {
            throw new IllegalArgumentException();
        }

        if (!type.equals(SVGConstants.SVG_CLICK_EVENT_TYPE)) {
            throw new DOMException(DOMException.NOT_SUPPORTED_ERR, 
                                   "Event type is NOT click.");
        }

        // CHECK x,y outside the viewport area
        if (x > getViewportWidth() || y > getViewportHeight()) {
            return;
        }

        // Get the target hit by the mouse event
        float[] pt = {x, y};
        ModelNode target = documentNode.nodeHitAt(pt);

        if (target == null) {
            return;
        }

        // Dispatch an activate on the element that was clicked on.
        documentNode.dispatchEvent(documentNode.initEngineEvent(
                SVGConstants.SVG_DOMACTIVATE_EVENT_TYPE, 
                target));


        // Now, dispatch the click event.
        documentNode.dispatchEvent
            (documentNode.initEngineEvent(SVGConstants.SVG_CLICK_EVENT_TYPE, 
                                          target));
    };

    /**
     *
     */
    public void activate() {
        if (lastElement == null) {
            return;
        }

        documentNode.dispatchEvent(documentNode.initEngineEvent(
                SVGConstants.SVG_DOMACTIVATE_EVENT_TYPE, 
                (ModelNode) lastElement));
    };

    /**
     *
     */
    public void focusOn(SVGElement element) throws DOMException {
        if (element == null) {
            if (lastElement != null) {
                // remove current focus
                documentNode.dispatchEvent(documentNode.initEngineEvent(
                        SVGConstants.SVG_DOMFOCUSOUT_EVENT_TYPE, 
                        (ModelNode) lastElement));
                lastElement = null;
            }
            return;
        }

        DocumentNode ownerDocument = ((ModelNode) element).getOwnerDocument();
        if (!ownerDocument.equals(documentNode)) {
            throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, 
                                   "Invalid element.");
        }

        // If this element is different from the previous element focused,
        // dispatch a "DOMFocusOut" event to the previous element.
        if (lastElement != element) {
            if (lastElement != null) {
                documentNode.dispatchEvent(documentNode.initEngineEvent(
                        SVGConstants.SVG_DOMFOCUSOUT_EVENT_TYPE, 
                        (ModelNode) lastElement));
            }

            documentNode.dispatchEvent(documentNode.initEngineEvent(
                    SVGConstants.SVG_DOMFOCUSIN_EVENT_TYPE, 
                    (ModelNode) element));

            lastElement = element;
        }
    };

    /**
     *
     */
    public void incrementTime(final float seconds) {
        if (documentNode.updateQueue == null) {
            // We are not running with an update queue, which means we are 
            // not running withing an SVGAnimator. Therefore, we simply
            // increment time.
            documentNode.incrementTime(seconds);
        } else {
            // The impact of changing the time must be made visible immediately
            // because the document is being displayed in an
            // SVGAnimator. Therefore, we force the document to sample and apply
            // animations so that resulting rendering changes get displayed
            // immediately.

            // We are running in an update queue. Check whether or not we are
            // already in the update thread.
            if (Thread.currentThread() 
                == documentNode.updateQueue.getThread()) {
                // We are already in the right thread. 
                documentNode.incrementTime(seconds);
                documentNode.applyAnimations();
            } else {
                // We are in the wrong thread. Move to the right thread.
                documentNode.safeInvokeAndWait(new Runnable() {
                        public void run() {
                            documentNode.incrementTime(seconds);
                            documentNode.applyAnimations();
                        }
                    });
            }
        }
    }


    // Inherited from ScalableImage...

    /**
     *
     */
    public static ScalableImage createImage
        (InputStream stream, 
         ExternalResourceHandler handler) throws IOException {
        if (stream == null) {
            throw new NullPointerException();
        }

        DocumentNode documentNode = new DocumentNode();

        return loadDocument(documentNode, stream, handler);
    }

    /**
     * Implementation helper.
     *
     * @param documentNode the <code>DocumentNode</code> object into which the 
     *        content of the stream should be loaded.
     * @param is the <code>InputStream</code> to the content to be loaded.
     * @param handler the <code>ExternalResourceHandler</code>. May be null.
     */
    private static ScalableImage loadDocument(
            final DocumentNode documentNode,
            final InputStream is,
            final ExternalResourceHandler handler) 
        throws IOException {
        UpdateAdapter updateAdapter = new UpdateAdapter();
        documentNode.setUpdateListener(updateAdapter);

        SVGImageImpl sii = new SVGImageImpl(documentNode, handler);

        ModelBuilder.loadDocument(is, documentNode);

        if (updateAdapter.hasLoadingFailed()) {
            if (updateAdapter.getLoadingFailedException() != null) {
                throw new IOException
                    (updateAdapter.getLoadingFailedException().getMessage());
            }
            throw new IOException();
        }

        // Now, get image width/height from <svg> element and set it in
        // DocumentNode
        Element root = documentNode.getDocumentElement();
        if (!(root instanceof SVG)) {
            throw new IOException(Messages.formatMessage(
                    Messages.ERROR_NON_SVG_RESOURCE,
                    new String[] {documentNode.getURIBase()}));
        }

        SVG svg = (SVG) root;
        int width = (int) svg.getWidth();
        int height = (int) svg.getHeight();
        documentNode.setSize(width, height);

        // Now, initialize the timing engine and sample at zero.
        documentNode.initializeTimingEngine();
        documentNode.sample(new Time(0));
        return sii;        
    }

    /**
     *
     */
    public static ScalableImage createImage(
            String URL, 
            ExternalResourceHandler handler) throws IOException, 
                                                    SecurityException {
        if (URL == null) {
            throw new NullPointerException();
        }

        DocumentNode documentNode = new DocumentNode();
        documentNode.setDocumentURI(URL);

        InputStream is = null;

        try {
            is = GZIPSupport.openHandleGZIP(URL);
        } catch (IOException ioe) {
            throw new IllegalArgumentException(ioe.getMessage());
        }

        return loadDocument(documentNode, is, handler);
    }

    /**
     * An area where the ScalableImage is rendered is called viewport.
     * If a part of the viewport lays outside of the target clipping
     * rectangle it is clipped. The viewport coordinates are given
     * relative to the target rendering surface.
     *
     */
    public void setViewportWidth(int width) {
        if (width < 0) {
            throw new IllegalArgumentException();
        }

        documentNode.setSize(width, documentNode.getHeight());
    }

    /**
     *
     */
    public void setViewportHeight(int height) {
        if (height < 0) {
            throw new IllegalArgumentException();
        }

        documentNode.setSize(documentNode.getWidth(), height);
    }

    // JAVADOC COMMENT ELIDED
    public int getViewportWidth() {
        return documentNode.getWidth();
    }

    // JAVADOC COMMENT ELIDED
    public int getViewportHeight() {
        return documentNode.getHeight();
    }

    /**
     *
     */
    public void requestCompleted(String uri, 
                                 InputStream resourceData) throws IOException {
        System.err.println(">>>>> requestCompleted : " + uri + " / " 
                            + resourceData);
        if (uri == null) {
            throw new NullPointerException();
        }

        synchronized (this) {
            // set in getImageAndWait()
            boolean isWaitURI = waitURI.equals(uri);

            RasterImage img;

            // null resourceData...
            if (resourceData == null) {
                img = svgImageLoader.getBrokenImage();
                isBrokenImage = true;
            } else {
                // we got a fresh, new image...
                ByteArrayOutputStream bos = new ByteArrayOutputStream(
                        DEFAULT_IMAGE_READ_BUFFER_LENGTH);
                byte[] ib = new byte[DEFAULT_IMAGE_READ_BUFFER_LENGTH];
                int byteRead = -1;
                int totalByteRead = 0;
                while ((byteRead = resourceData.read(ib, 0, ib.length)) != -1) {
                    bos.write(ib, 0, byteRead);
                    totalByteRead += byteRead;
                }
                
                img = svgImageLoader.loaderUtil.createImage(bos.toByteArray());
            }

            // the new image is added to cache...
            svgImageLoader.addToCache(uri, img);

            svgImageLoader.setRasterImageConsumerImage(uri, img);
            
            // request was initiated by getImageAndWait
            if (isWaitURI) {
                notifyAll();
            }
        }
    };


    synchronized RasterImage waitOnRequestCompleted(final String uri) {
        waitURI = uri;

        try {
            while (isBrokenImage == false &&
                   svgImageLoader.getImageFromCache(uri) == null) {
                wait();
            }
        } catch (InterruptedException ie) {
            return svgImageLoader.getBrokenImage();
        }

        waitURI = new String();
        if (isBrokenImage) {
            isBrokenImage = false;
            return svgImageLoader.getBrokenImage();
        } else {
            return svgImageLoader.getImageFromCache(uri);
        }
    }
}
