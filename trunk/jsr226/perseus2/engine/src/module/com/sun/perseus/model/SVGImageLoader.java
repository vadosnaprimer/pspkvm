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

import com.sun.perseus.platform.URLResolver;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.m2g.ExternalResourceHandler;

import com.sun.perseus.j2d.ImageLoaderUtil;
import com.sun.perseus.j2d.RasterImage;

/**
 * JSR 226 implementation of the <code>ImageLoader</code> interface.
 *
 * @author <a href="mailto:kevin.wong@sun.com">Kevin Wong</a>
 * @version $Id: SVGImageLoader.java,v 1.14 2006/06/29 10:47:34 ln156897 Exp $
 */
public class SVGImageLoader extends DefaultImageLoader {
    /**
     * Constant used to indicate that an image has been requested to the 
     * ExternalResourceHandler but that the handler has not delivered the 
     * result yet.
     */
    protected final static String IMAGE_REQUESTED = "Image Requested.";

    /**
     * Handles any external resource referenced in the image document.
     */
    protected ExternalResourceHandler handler;

    /**
     * The SVGImage associated to this SVGImageLoader
     */
    protected SVGImageImpl svgImage;

    /**
     * Simple hashtable used to track ImageNode objects.
     */
    protected Hashtable rasterImageConsumerTable = new Hashtable();

    /**
     * ImageLoaderUtil contains helper methods which make this 
     * implementation easier.
     */
    protected ImageLoaderUtil loaderUtil = new ImageLoaderUtil();

    /**
     * Set to true once the associated DocumentNode has fully loaded.
     */
    protected boolean documentLoaded;

    /**
     * Keeps track of pending absoluteURI requests.
     */
    protected Vector pendingNeedsURI = new Vector();

    /**
     * Constructor
     *
     * @param svgImage the associated SVGImage, should not be null.
     * @param handler the ExternalResourceHandler which will get the images 
     *        data.
     */
    public SVGImageLoader(SVGImageImpl svgImage, 
                          ExternalResourceHandler handler) {
        if (svgImage == null || handler == null) {
            throw new NullPointerException();
        }

        this.svgImage = svgImage;
        this.handler = handler;
    }

    /**
     * Notifies the URILoader that the given uri will be needed.
     *
     * @param absoluteURI the requested URI content. 
     */
    public void needsURI(final String absoluteURI) {
        // SVGImageLoader waits until the document has loaded before actually
        // requesting any image from the ResourceHandler.
        if (documentLoaded) {
            needsURIDocumentLoaded(absoluteURI);
        } else {
            if (!loaderUtil.isDataURI(absoluteURI) && 
                !pendingNeedsURI.contains(absoluteURI)) {
                // Cache the request for future use (see documentLoaded method)
                pendingNeedsURI.addElement(absoluteURI);
            }
        }
    }

    /**
     * In SVGImageLoader, we wait until the document has been loaded before 
     * acting on required raster images.
     *
     * @param absoluteURI the requested URI content. 
     */
    protected void needsURIDocumentLoaded(final String absoluteURI) {
        // Do not load base64 images as we do not want to 
        // store the base64 string in the cache, because it
        // might be huge.
        //
        // Correct content should not have the same base64 
        // string duplicated. Rather, the same image element
        // can be referenced by a use element.
        if (!loaderUtil.isDataURI(absoluteURI)) {
            boolean isRequested = true;
            synchronized (cache) {
                // First check if the image is already available (implies that
                // images has been requested and obtained successfully)
                Object imgObj = cache.get(absoluteURI);
                if (null == imgObj) {
                    // The image has not been requested yet
                    isRequested  = false;
                    addToCache(absoluteURI, IMAGE_REQUESTED);
                } else if (IMAGE_REQUESTED != imgObj) {
                    // The image has been requested and retrieved
                    setRasterImageConsumerImage(absoluteURI, 
                                                (RasterImage) imgObj);
                    return;
                }
            }

            // requestResource() called outside of synchronized block
            // so that the cache is not unnecessarily locked.  handler is an
            // external implementation and the behavior is unpredictable.
            if (!isRequested) {
                System.err.println("handler.requestResource: " + absoluteURI);
                handler.requestResource(svgImage, absoluteURI);
            }
        }
    }

    /**
     * Requests the given image. This call blocks until an image is
     * returned.
     *
     * @param uri the requested URI.
     * @return the loaded image or the same image as returned by
     *         a <code>getBrokenImage</code> call if the image could
     *         not be loaded.
     */
    public RasterImage getImageAndWait(final String uri) {
        // If we are dealing with a data URI, decode the image
        // now. Data URIs do not go in the cache.
        if (loaderUtil.isDataURI(uri)) {
            return loaderUtil.getEmbededImage(uri);
        }

        Object img;

        // We are dealing with a regular URI which requires IO.
        // The image might already be in the loading queue from
        // a call in needsURI. 
        synchronized (cache) {
            img = cache.get(uri);
        }

        if ((img != null) && (img != IMAGE_REQUESTED)) {
            return (RasterImage) img;
        }

        // Make the sure the image is requested
        if (img == null) {
            needsURI(uri);
        }

        // The URI has not been retrieved yet...
        img = ((SVGImageImpl) svgImage).waitOnRequestCompleted(uri);

        return (RasterImage) img;
    }

    /**
     * Requests the given image. This call returns immediately and 
     * the image is set on the input <code>ImageNode</code> when the
     * image becomes available.
     *
     * @param uri the requested URI.
     * @param rasterImageConsumer the <code>RasterImageConsumer</code> whose 
     *        image member should be set as a result of loading the image.
     */
    public void getImageLater(final String uri, 
                              final RasterImageConsumer rasterImageConsumer) {
        // Only load later images which have not been loaded yet
        // and which are not data URIs.
        if (loaderUtil.isDataURI(uri)) {
            rasterImageConsumer.setImage(loaderUtil.getEmbededImage(uri), uri);
            return;
        }

        Object img = null;
        synchronized (cache) {
            img = cache.get(uri);
        }

        if ((img != null) && (img != IMAGE_REQUESTED)) {
            rasterImageConsumer.setImage((RasterImage) img, uri);
            return;
        }

        // Save ImageNode associated with the uri for use with SVGImage's
        // requestCompleted().
        addRasterImageConsumer(uri, rasterImageConsumer);
    }

    /**
     * Determines whether this ImageLoader can handle relative uri's
     *
     * @return true if this ImageLoader can handle relative uri's;
     *         false otherwise.
     */
    public boolean allowsRelativeURI() {
        return true;
    }

    /**
     * Some ImageLoader implementations may wish to wait until the end of the
     * Document load to start retrieving resources. This method notifies
     * the implementation that the DocumentNode completed loading successfully.
     *
     * @param doc the DocumentNode which just finised loading.
     */
    public void documentLoaded(final DocumentNode doc) {
        // Place a needsURIDocumentLoaded call for each entry in the 
        // pendingNeedsURI vector. See the needsURI method.
        documentLoaded = true;
        int n = pendingNeedsURI.size();
        for (int i = 0; i < n; i++) {
            needsURIDocumentLoaded((String) pendingNeedsURI.elementAt(i));
        }

        // Empty pending requests
        pendingNeedsURI.removeAllElements();
    }
    
    /**
     * In cases where the ImageLoader may update the images associated to a URI,
     * RasterImageConsumer interested in updates need to register their interest
     * throught this method.
     *
     * @param absoluteURI the URI the RasterImageConsumer is interested in.
     * @param imageNode the RasterImageConsumer interested in the URI.
     */
    public void addRasterImageConsumer(final String absoluteURI, 
                                       final RasterImageConsumer imageNode) {
        if (absoluteURI == null) {
            return;
        }

        synchronized (rasterImageConsumerTable) {
            Vector v = (Vector) rasterImageConsumerTable.get(absoluteURI);
            if (v == null) {
                v = new Vector(1);
                v.addElement(imageNode);
                rasterImageConsumerTable.put(absoluteURI, v);
            } else {
                if (!v.contains(imageNode)) {
                    v.addElement(imageNode);
                }
            }
        }
    }
    
    /**
     * In cases where the ImageLoader may update the images associated to a URI,
     * RasterImageConsumer interested in updates need to de-register their 
     * interest throught this method.
     *
     * @param absoluteURI the URI the RasterImageConsumer is interested in.
     * @param imageNode the RasterImageConsumer interested in the URI.
     */
    public void removeRasterImageConsumer(final String absoluteURI, 
                                          final RasterImageConsumer imageNode) {
        if (absoluteURI != null) {
            synchronized (rasterImageConsumerTable) {
                Vector v = (Vector) rasterImageConsumerTable.get(absoluteURI);
                if (v != null) {
                    v.removeElement(imageNode);
                }
            }
        }
    }

    /**
     * Returns the Image that was previously loaded.
     *
     * @param uri the key for the Image cache.
     * @return the Image associated with the key.
     */
    public RasterImage getImageFromCache(final String uri) {
        Object img;
        synchronized (cache) {
            img = cache.get(uri);
            if (IMAGE_REQUESTED == img) {
                img = null;
            }
        }
        return (RasterImage) img;
    }

    /**
     * Adds image to the Image cache.
     *
     * @param uri the key for the Image cache.
     * @param image the Image to store.
     */
    void addToCache(final String uri, final Object image) {
        synchronized (cache) {
            cache.put(uri, image);
        }
    }

    /**
     * Implementation helper.
     *
     * @param uri the uri identifying the image resource.
     * @param image the RasterImage to send to the consumers.
     */
    void setRasterImageConsumerImage(final String uri,
                                     final RasterImage image) {
        ((DocumentNode) svgImage.getDocument()).safeInvokeAndWait(
                new Runnable() {
                    public void run() {
                        synchronized (rasterImageConsumerTable) {
                            Vector v = 
                                (Vector) rasterImageConsumerTable.get(uri);
                            if (v != null) {
                                int n = v.size();
                                for (int i = 0; i < n; i++) {
                                    final RasterImageConsumer in 
                                        = (RasterImageConsumer) v.elementAt(i);
                                    in.setImage(image, uri);
                                }
                            }
                        }
                    }
                });
    }

}
