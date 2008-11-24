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

import com.sun.perseus.j2d.RasterImage;

/**
 * Interface for handling RasterImage resources loading. Implementations can 
 * vary in the way they handle security restrictions, caching or advanced 
 * loading policies.
 *
 * @version $Id: ImageLoader.java,v 1.4 2006/06/29 10:47:32 ln156897 Exp $
 */
public interface ImageLoader {
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
    String resolveURI(final String uri, final String baseURI);

    /**
     * Notifies the URILoader that the given uri will be needed.
     *
     * @param absoluteURI the requested URI content. 
     */
    void needsURI(final String absoluteURI);

    /**
     * Requests the given image. This call blocks until an image is
     * returned.
     *
     * @param uri the requested URI. Should be a resolved URI.
     * @return the loaded image or the same image as returned by
     *         a <code>getBrokenImage</code> call if the image could
     *         not be loaded.
     */
    RasterImage getImageAndWait(final String uri); 

    /**
     * Requests the given image. This call returns immediately and 
     * the image is set on the input <code>ImageNode</code> when the
     * image becomes available.
     *
     * @param uri the requested URI. Should be a resolved URI.
     * @param imageNode the <code>ImageNode</code> whose image 
     *        member should be set as a result of loading the 
     *        image.
     */
    void getImageLater(final String uri, final RasterImageConsumer imageNode);

    /**
     * Returns the image that should be used to represent 
     * an image which is loading.
     *
     * @return the image to use to represent a pending loading.
     */
    RasterImage getLoadingImage();

    /**
     * Returns the image that should be used to represent an
     * image which could not be loaded.
     *
     * @return the image to represent broken uris or content.
     */
    RasterImage getBrokenImage();
    
    /**
     * Determines whether this ImageLoader can handle relative uri's
     *
     * @return true if this ImageLoader can handle relative uri's;
     *         false otherwise.
     */
    boolean allowsRelativeURI();

    /**
     * Some ImageLoader implementations may wish to wait until the end of the
     * Document load to start retrieving resources. This method notifies the
     * implementation that the associated DocumentNode completed loading
     * successfully.
     *
     * @param doc the DocumentNode which just finised loading.
     */
    void documentLoaded(final DocumentNode doc);

    /**
     * In cases where the ImageLoader may update the images associated to a URI,
     * RasterImageConsumer interested in updates need to register their interest
     * throught this method.
     *
     * @param absoluteURI the URI the RasterImageConsumer is interested in.
     * @param imageNode the RasterImageConsumer interested in the URI.
     */
    void addRasterImageConsumer(final String absoluteURI, 
                                final RasterImageConsumer imageNode);
    
    /**
     * In cases where the ImageLoader may update the images associated to a URI,
     * RasterImageConsumer interested in updates need to de-register their 
     * interest throught this method.
     *
     * @param absoluteURI the URI the RasterImageConsumer is interested in.
     * @param imageNode the RasterImageConsumer interested in the URI.
     */
    void removeRasterImageConsumer(final String absoluteURI, 
                                   final RasterImageConsumer imageNode);
    
}
