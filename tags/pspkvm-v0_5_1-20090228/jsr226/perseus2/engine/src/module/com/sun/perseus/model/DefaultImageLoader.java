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

import com.sun.perseus.util.RunnableQueue;
import com.sun.perseus.util.RunnableQueue.RunnableHandler;
import com.sun.perseus.util.RunnableQueue.RunnableQueueHandler;

import com.sun.perseus.j2d.ImageLoaderUtil;
import com.sun.perseus.j2d.RasterImage;

/**
 * Default implementation of the <code>ImageLoader</code> interface.
 *
 * @version $Id: DefaultImageLoader.java,v 1.4 2006/06/29 10:47:30 ln156897 Exp $
 */
public class DefaultImageLoader implements ImageLoader {
    /**
     * Simple hashtable used to cache image objects.
     */
    protected Hashtable cache = new Hashtable();

    /**
     * RunnableQueue used to load image asynchronously
     * @see #getImageLater
     */
    protected static RunnableQueue loadingQueue;

    /**
     * ImageLoaderUtil contains helper methods which make this 
     * implementation easier.
     */
    protected ImageLoaderUtil loaderUtil = new ImageLoaderUtil();

    /**
     * Use a single loading queue for the implementation.
     */
    static {
        loadingQueue =  RunnableQueue.createRunnableQueue(null);
        loadingQueue.resumeExecution();
    }

    /**
     * Default constructor
     */
    public DefaultImageLoader() {
    }

    /**
     * Returns the image that should be used to represent 
     * an image which is loading.
     *
     * @return the image to use to represent a pending loading.
     */
    public RasterImage getLoadingImage() {
        return loaderUtil.getLoadingImage();
    }

    /**
     * Returns the image that should be used to represent an
     * image which could not be loaded.
     *
     * @return the image to represent broken uris or content.
     */
    public RasterImage getBrokenImage() {
        return loaderUtil.getBrokenImage();
    }

    /**
     * Resolves the input relative and base URI into an absolute URI
     * which can be used in subsequent calls to needsURI, getImageAndWait
     * or getImageLater calls.
     * @param uri the requested URI content. 
     * @param baseURI the base URI. Needed in case uri is relative.
     * @return the resolved URI that should be requested in follow on 
     *         needsURI, getImageAndWait or getImageLater calls or null if
     *         the URI cannot be resolved.
     */
    public String resolveURI(final String uri, final String baseURI) {
        if (uri == null) {
            return null;
        }

        // Do not load base64 images as we do not want to 
        // store the base64 string in the cache, because it
        // might be huge.
        if (loaderUtil.isDataURI(uri)) {
            return uri;
        }

        try {
            return URLResolver.resolve(baseURI, uri);
        } catch (IllegalArgumentException iae) {
            return null;
        }
    }

    /**
     * Notifies the URILoader that the given uri will be needed.
     *
     * @param absoluteURI the requested URI content. 
     */
    public void needsURI(final String absoluteURI) {
        // Do not load base64 images as we do not want to 
        // store the base64 string in the cache, because it
        // might be huge.
        //
        // Correct content should not have the same base64 
        // string duplicated. Rather, the same image element
        // can be referenced by a use element.
        if (!loaderUtil.isDataURI(absoluteURI)) {
            // Check if the image is currently loaded
            synchronized (cache) {
                if (cache.get(absoluteURI) == null) {
                    // Start loading the image now so that it is ready when
                    // we need it for rendering.
                    loadingQueue.invokeLater
                        (new ImageLoadRunnable(absoluteURI), null);
                }
            }
        }
    }

    /**
     * Requests the given image. This call blocks until an image is
     * returned.
     *
     * @param uri the requested URI. Should be a resolved URI returned
     *            from an earlier call to <code>needsURI</code>.
     * @return the image after it has been loaded. If the image could
     *         not be loaded, this returns the same image as returned
     *         by a call to <code>getBrokenImage</code>.
     */
    public RasterImage getImageAndWait(final String uri) {
        // If we are dealing with a data URI, decode the image
        // now. Data URIs do not go in the cache.
        if (loaderUtil.isDataURI(uri)) {
            return loaderUtil.getEmbededImage(uri);
        }

        // We are dealing with a regular URI which requires IO.
        // The image might already be in the loading queue if
        // a call to needsURI was made. 
        synchronized (cache) {
            RasterImage img = (RasterImage) cache.get(uri);
            if (img != null) {
                return img;
            }
        }

        // The URI has not been retrieved at all or the 
        // ImageLoadRunnable has not completed yet. We
        // simply preempt a new ImageLoadRunnable. When that
        // one complete, we will be sure the image is in
        // the cache.
        ImageLoadRunnable loader = 
            new ImageLoadRunnable(uri);

        try {
            loadingQueue.preemptAndWait(loader, null);
        } catch (InterruptedException ie) {
            // We were interrupted while waiting for the image.
            // Return brokenImage
            return loaderUtil.getBrokenImage();
        }

        synchronized (cache) {
            return (RasterImage) cache.get(uri);
        }
    }

    /**
     * Requests the given image. This call returns immediately and 
     * the image is set on the input <code>ImageNode</code> when the
     * image becomes available.
     *
     * @param uri the requested URI. Should be a resolved URI returned
     *        from an earlier call to <code>needsURI</code>.
     * @param rasterImageConsumer the <code>ImageNode</code> whose image 
     *        member should be set as a result of loading the 
     *        image.
     */
    public void getImageLater(final String uri, 
                              final RasterImageConsumer imageNode) {
        // Only load later images which have not been loaded yet
        // and which are not data URIs.
        if (loaderUtil.isDataURI(uri)) {
            imageNode.setImage(loaderUtil.getEmbededImage(uri), uri);
            return;
        }

        RasterImage img = null;
        synchronized (cache) {
            img = (RasterImage) cache.get(uri);
        }
        
        if (img != null) {
            imageNode.setImage(img, uri);
            return;
        }

        ImageLoadRunnable loader = new ImageLoadRunnable(uri, imageNode);
        loadingQueue.invokeLater(loader, null);
    }


    /**
     * Determines whether this ImageLoader can handle relative uri's
     *
     * @return true if this ImageLoader can handle relative uri's;
     *         false otherwise.
     */
    public boolean allowsRelativeURI() {
        return false;
    }


    /**
     * Some ImageLoader implementations may wish to wait until the end of the
     * Document load to start retrieving resources. This method notifies
     * the implementation that the DocumentNode completed loading successfully.
     *
     * @param doc the DocumentNode which just finised loading.
     */
    public void documentLoaded(final DocumentNode doc) {
        // Do nothing. The DefaultImageLoader implementation loads image
        // as soon as they are notified in needsURI calls.
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
    }

    /**
     * Utility method. Used to wait until all pending load operations are
     * complete.
     *
     * @throws InterruptedException if the thread is interrupted while 
     *         waiting in this method call.
     */
    public void waitForAll() throws InterruptedException {
        loadingQueue.invokeAndWait(new Runnable() { public void run() { } }, 
                                   null);
    }

    // =========================================================================

    /**
     * Simple Runnables used to load images asynchronously.
     */
    class ImageLoadRunnable implements Runnable {
        /**
         * The uri from which image content is loaded.
         */
        private String uri;

        /**
         * The <code>ImageNode</code> for which image content is
         * loaded.
         */
        private RasterImageConsumer node;

        /**
         * Construct with an absolute URI
         *
         * @param uri the uri to load
         */
        public ImageLoadRunnable(final String uri) {
            this.uri = uri;
        }

        /**
         * Construct with an absolute URI and a node on which
         * the loaded image should be set.
         *
         * @param uri the uri to load
         * @param node the image consumer on which the image should be
         *        set.
         */
        public ImageLoadRunnable(final String uri, 
                                 final RasterImageConsumer node) {
            this.uri = uri;
            this.node = node;
        }

        /**
         * <code>Runnable</code> implementation. Loads the image and
         * sets the resulting image on the associated <code>ImageNode</code>
         */
        public void run() {
            RasterImage img = null;
            synchronized (cache) {
                img = (RasterImage) cache.get(uri);
            }
            
            if (img == null) {
                // If the image was not loaded before, load it now
                // and put it in the cache.
                img = loaderUtil.getExternalImage(uri);
                synchronized (cache) {
                    cache.put(uri, img);
                }
            }
            
            if (node != null) {
                // Make sure we update the image content in the 
                // update thread, if there is one
                if (node.getUpdateQueue() != null) {
                    ImageSetter setter = new ImageSetter(img, node, uri);
                    try {
                        node.getUpdateQueue().invokeAndWait
                                (setter, 
                                 node.getRunnableHandler());
                    } catch (InterruptedException ie) {
                        // We were interrupted while setting the image. 
                        // This means this image loader's loadingQueue thread
                        // has been interrupted... Not much we can do as we 
                        // do not know if the setter has been invoked or not.
                        // Just end gracefully.
                        return;
                    }
                } else {
                    node.setImage(img, uri);
                }
            }
        }
    }
    
    /**
     * Simple <code>Runnable</code> implementation used to set
     * an <code>ImageNode</code>'s image in the update thread.
     */
    static class ImageSetter implements Runnable {
        /**
         * The image to set
         */
        private RasterImage img;

        /**
         * The node on which the image should be set
         */
        private RasterImageConsumer node;

        /**
         * The uri from which the image was loaded.
         */
        private String uri;

        /**
         * Construct with an image, an image node and uri
         *
         * @param img the image to set on the node
         * @param node the image node to modify
         * @param uri the uri that was retrieved
         */
        public ImageSetter(final RasterImage img,
                           final RasterImageConsumer node,
                           final String uri) {
            this.img = img;
            this.node = node;
            this.uri = uri;
        }

        /**
         * <code>Runnable</code> implementation. We simply set the 
         * <code>ImageNode</code> image.
         */
        public void run() {
            node.setImage(img, uri);
        }
    }
}

