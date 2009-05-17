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

package com.sun.perseus.platform;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.InputConnection;

import java.io.InputStream;

import java.io.IOException;

/**
 * This class is used to provide GZIP support to the Perseus engine by using 
 * a custom implementation of GZIPInputStream
 * <code>com.sun.perseus.platform.GZIPInputStream</code>
 * 
 *
 */
public class GZIPSupport {

    /**
     * Used for HTTP request headers
     */
    public static final String HTTP_ACCEPT_ENCODING = "Accept-Encoding";

    /**
     * GZIP encoding
     */
    static final String HTTP_GZIP_ENCODING = "gzip";

    /**
     * If GZIP encoding is supported, this method should setup the
     * HTTP Request Header to declare that GZIP encoding is supported.
     *
     * @param svgURI the url of the requested SVG resource.
     * @return a stream that does not handles GZIP uncompression.
     * @throws IOException if an I/O error happens while opening the 
     *         requested URI.
     */
    public static InputStream openHandleGZIP(String svgURI) throws IOException {

        InputConnection svgURLConnection = 
            (InputConnection)Connector.open(svgURI, Connector.READ);

        try {
            if (svgURLConnection instanceof HttpConnection) {
                setupHttpEncoding((HttpConnection)svgURLConnection);
            }

            return(svgURLConnection.openInputStream());

        } finally {
            svgURLConnection.close();
        }
    }

        /**
         *
         */
        static void setupHttpEncoding(final HttpConnection httpC) throws IOException {
            String encodings = 
                httpC.getRequestProperty(HTTP_ACCEPT_ENCODING);
        
            if (encodings == null) {
                encodings = "";
            }
        
            if (encodings.trim().length() > 0) {
                encodings += ",";
            }
            encodings += HTTP_GZIP_ENCODING;
            httpC.setRequestProperty(HTTP_ACCEPT_ENCODING, encodings);
        }

        /**
         * This method checks if the input stream is a GZIP stream.
         * It reads the first two bytes of the stream and compares
         * the short it reads with the GZIP magic number
         * (see <code>java.util.zip.GZIPInputStream.GZIP_MAGIC).
         * 
         * If the magic number matches, the method returns a 
         * <code>java.util.zip.GZIPInputStream</code> which wraps the
         * input stream. Otherwise, the method returns either the 
         * unchanged stream or a <code>BufferedInputStream</code>
         * if the input stream did not support marking, as defined
         * by the <code>java.io.InputStream.mark</code> method.
         *
         * @param is the input stream that might be GZIPPed.
         * @return a stream that handles GZIP uncompression, if any is 
         *         needed.
         * @throws IOException if an I/O error happens while building
         *         a GZIPInputStream.
         */
        public static InputStream handleGZIP(InputStream is) throws IOException {
            // Wrap the stream if it does not support marking.
            // BufferedInputStream supports marking.

            //Temporary: wrap all input streams to workaround bug in WTK (CR 6335742)
            //Temporary: //if (!is.markSupported())
            is = new BufferedInputStream(is);

            int magicIn = 0;
            try {
                is.mark(2);
                magicIn = 0x0000ffff & (is.read() | is.read() << 8);
            } catch (IOException ex) {
                // We were not able to read at least two bytes,
                // this cannot be a GZIP stream as it does not 
                // even have the magic number header
                is.reset();

                return is;

            } finally {
                try {

                    is.reset();

                } catch (IOException ioe) {
                    // This should _never_ happen because we do not fall
                    // into _any_ of the conditions that might cause a 
                    // reset() to throw an IOException. If we got into that
                    // situation, it means we are in serious troubles.
                    throw new Error();
                }
            }
            if (magicIn == GZIPInputStream.GZIP_MAGIC) {

                return new GZIPInputStream(is);

            }

            return is;

        }

    }
