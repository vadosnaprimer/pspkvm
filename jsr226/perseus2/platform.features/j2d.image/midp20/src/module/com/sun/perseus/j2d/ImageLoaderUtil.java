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
package com.sun.perseus.j2d;

import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Graphics;

import java.io.InputStream;
import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import com.sun.perseus.util.Base64DecodeStream;

/**
 * This class contains utility methods which make <code>ImageLoader</code> 
 * implementations easier.
 *
 * @version $Id: ImageLoaderUtil.java,v 1.12 2006/04/21 06:34:56 st125089 Exp $
 */
public class ImageLoaderUtil {

    /**
     * Default, broken image returned if an image cannot be loaded.
     */
    protected RasterImage brokenImage;

    /**
     * Image used to symbolize loading state for an image.
     */
    protected RasterImage loadingImage;

    /**
     * HREF prefix for all Base64 encoded images
     */
    static final String BASE64_PREFIX = "data:";

    /**
     * Color used for broken content.
     */
    static final int BROKEN_IMAGE_COLOR = 0x00000000;

    /**
     * Color used for loading content.
     */
    static final int LOADING_IMAGE_COLOR = 0x00000000;

    /**
     * Default constructor
     */
    public ImageLoaderUtil() {

        createPlaceholderImages();

    }

    /**
     * Returns the image that should be used to represent 
     * an image which is loading.
     *
     * @return the image to use to represent a pending loading.
     */
    public RasterImage getLoadingImage() {
        return loadingImage;
    }

    /**
     * Returns the image that should be used to represent an
     * image which could not be loaded.
     *
     * @return the image to represent broken uris or content.
     */
    public RasterImage getBrokenImage() {
        return brokenImage;
    }

    /**
     * Returns true if the input uri is a data uri.
     *
     * @param uri the URI to analyze.
     * @return true if the input uri is a data uri.
     */
    public boolean isDataURI(final String uri) {
        return (uri.startsWith(BASE64_PREFIX));
    }

    /**
     * Utility method to turn an image href into an Image. This assumes
     * that the href points to an <b>external</b> resource. This can 
     * be tested on the href with the <code>isDataURI</code> method.
     *
     * @param href the address from which to load the image content.
     * @return the loaded image or <code>brokenImage</code> if the image
     *         could not be loaded.
     */
    public RasterImage getExternalImage(final String href) {

	System.out.println("getExternalImage(), Image href = " + href);

        Image img = null;
        StreamConnection c = null;
        InputStream s = null;

        try {
            c = (StreamConnection)Connector.open(href);
            s = c.openInputStream();
            img = Image.createImage(s);

        } catch (IOException ioe) {
            ioe.printStackTrace();

	    System.out.println("returning broken image");
            return brokenImage;

        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();

	    System.out.println("returning broken image");
            return brokenImage;

        } finally {

	    try {
		if (s != null) 
		    s.close();
		if (c != null)
		    c.close();
	    } catch (IOException ioe) {

		//note : we have already read the image successfully. 
		//So don't fail, simply print stacktrace.

		ioe.printStackTrace();
	    }

        }
        return (new RasterImage(img));

    }

    /**
     * Creates a RasterImage from a byte array.
     *
     * @param b the byte array containing the encoded image
     *        data.
     */
    public RasterImage createImage(final byte[] imageData) {
        Image img = Image.createImage(imageData, 0, imageData.length);

        return (new RasterImage(img));
    }

    /**
     * Creates a RasterImage from an int array containing the pixel data
     */
    public RasterImage createImage(int[] imageData, int width, int height) {
	Image img = Image.createRGBImage(imageData, width, height, true);
	return new RasterImage(img);
    }

    /**
     * Utility method to get an <tt>Image</tt> from Base64 
     * encoded image data
     *
     * @param uri the uri with encoded image data
     * @return the decoded <tt>Image</tt> or brokenImage if the encoded data
     *         is invalid and could not be decoded.
     */
    public RasterImage getEmbededImage(final String uri) {
        int startAt = 0;
        if (uri.startsWith(BASE64_PNG_HREF_PREFIX)) {
            startAt = BASE64_PNG_HREF_PREFIX_LENGTH;
        } else if (uri.startsWith(BASE64_JPG_HREF_PREFIX)) {
            startAt = BASE64_JPG_HREF_PREFIX_LENGTH;
        } else if (uri.startsWith(BASE64_JPG_HREF_PREFIX2)) {
            startAt = BASE64_JPG_HREF_PREFIX2_LENGTH;
        } else if (uri.startsWith(BASE64_HREF_PREFIX)) {
            startAt = BASE64_HREF_PREFIX_LENGTH;
        } else {
            return brokenImage;
        }

        InputStream is = new Base64StringStream(uri, startAt);
        is = new Base64DecodeStream(is);

        // Base64 encodes 3 bytes with 4 characters
        byte[] data = new byte[(uri.length() - startAt) * 3 / 4];
        try {
	    Image img = Image.createImage(is);
	    return (new RasterImage(img));
        } catch (java.io.IOException ioe) {
	    ioe.printStackTrace();

            return brokenImage;
        }  catch (java.lang.IllegalArgumentException iae) {
            iae.printStackTrace();

            return brokenImage;

        } finally {
            try {
                is.close();
            } catch (java.io.IOException ioe) {
            }
        }
    }

    protected void createPlaceholderImages() {
        int[] argb = {BROKEN_IMAGE_COLOR};
        Image image = Image.createRGBImage(argb, 1, 1, true);
        brokenImage  = new RasterImage(image);

        argb = new int[] {LOADING_IMAGE_COLOR};
        image = Image.createRGBImage(argb, 1, 1, true);
        loadingImage = new RasterImage(image); 
    }


    /**
     * HREF prefix for Base64 encoded JPEG files
     */
    static final String BASE64_JPG_HREF_PREFIX 
        = "data:image/jpg;base64,";

    /**
     * HREF prefix for Base64 encoded JPEG files
     */
    static final String BASE64_JPG_HREF_PREFIX2 
        = "data:image/jpeg;base64,";

    /**
     * HREF prefix for Base64 encoded PNG files
     */
    static final String BASE64_PNG_HREF_PREFIX 
        = "data:image/png;base64,";

    /**
     * HREF prefix for Base64 encoded images with unspecified media type
     */
    static final String BASE64_HREF_PREFIX = "data:;base64,";

    /**
     * Length of the HREF prefix for Base64 encoded JPEG files
     */
    static final int BASE64_JPG_HREF_PREFIX_LENGTH
        = BASE64_JPG_HREF_PREFIX.getBytes().length;

    /**
     * Length of the HREF prefix for Base64 encoded JPEG files
     */
    static final int BASE64_JPG_HREF_PREFIX2_LENGTH
        = BASE64_JPG_HREF_PREFIX2.getBytes().length;

    /**
     * Length of the HREF prefix for Base64 encoded PNG files
     */
    static final int BASE64_PNG_HREF_PREFIX_LENGTH
        = BASE64_PNG_HREF_PREFIX.getBytes().length;

    /**
     * Length of the HREF prefix for Base64 encoded images with 
     * unspecified media type
     */
    static final int BASE64_HREF_PREFIX_LENGTH  
        = BASE64_HREF_PREFIX.getBytes().length;

    // =========================================================================

    /**
     * Specialized InputStream to read bytes from a String containing a 
     * Base64 encoded value. Because we know only the last 8 bits of each
     * character are used in Base64 encoded strings, we can safely map each
     * string character to a byte of data for the Base64 decoded. This saves
     * memory allocation as it avoids having to get a byte array from the 
     * String (i.e., we do not need to use getBytes).
     */
    static class Base64StringStream extends InputStream {
        /**
         * The <code>String</code> from which bytes are read.
         */
        private String str;

        /**
         * The length of the string
         */
        private int len = 0;

        /**
         * Bytes are read starting at this index.
         */
        private int offset = 0;

        /**
         * Reads the next byte of data from the input stream. The value byte 
         * is returned as an int in the range 0 to 255. If no byte is available
         * because the end of the stream has been reached, the value -1 is 
         * returned. This method blocks until input data is available, the 
         * end of the stream is detected, or an exception is thrown.
         *
         * @return the next byte of data, or -1 if the end of the stream is 
         *         reached.
         */
        public int read() {
            return offset < len ? str.charAt(offset++) : -1;
        }

        /**
         * Constructor.
         *
         * @param str the String where base64 values are read
         * @param offset the offset from which values are read
         */
        public Base64StringStream(final String str,
                                  final int offset) {
            this.str = str;
            this.offset = offset;
            this.len = str.length();
        }
    }

    // =========================================================================

}
