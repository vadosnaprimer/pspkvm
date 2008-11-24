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

package com.sun.perseus.util;

import java.io.InputStream;
import java.io.IOException;

/**
 * This class implements a Base64 Character decoder as specified in RFC1113.
 * Unlike some other encoding schemes there is nothing in this encoding that
 * tells the decoder where a buffer starts or stops, so to use it you will need
 * to isolate your encoded data into a single chunk and then feed them
 * this decoder. The simplest way to do that is to read all of the encoded
 * data into a string and then use:
 * <pre>
 *      byte    data[];
 *      InputStream is = new ByteArrayInputStream(data);
 *      is = new Base64DecodeStream(is);
 * </pre>
 *
 * On errors, this class throws a IOException with the following detail
 * strings:
 * <pre>
 *    "Base64DecodeStream: Bad Padding byte (2)."
 *    "Base64DecodeStream: Bad Padding byte (1)."
 * </pre>
 *
 * @author <a href="thomas.deweese@kodak.com">Thomas DeWeese</a>
 * @author      Chuck McManis
 * @version $Id: Base64DecodeStream.java,v 1.2 2006/04/21 06:35:45 st125089 Exp $
 */

public class Base64DecodeStream extends InputStream {
    /**
     * The stream to be decoded
     */
    protected InputStream src;

    /**
     * @param src the Base64 encoded input stream
     */
    public Base64DecodeStream(final InputStream src) {
        this.src = src;
    }

    /**
     * Decode table
     */
    private static final byte[] PEM_ARRAY = new byte[256];
    static {
        for (int i = 0; i < PEM_ARRAY.length; i++) {
            PEM_ARRAY[i] = -1;
        }

        int idx = 0;
        for (char c = 'A'; c <= 'Z'; c++) {
            PEM_ARRAY[c] = (byte) idx++;
        }
        for (char c = 'a'; c <= 'z'; c++) {
            PEM_ARRAY[c] = (byte) idx++;
        }

        for (char c = '0'; c <= '9'; c++) {
            PEM_ARRAY[c] = (byte) idx++;
        }

        PEM_ARRAY['+'] = (byte) idx++;
        PEM_ARRAY['/'] = (byte) idx++;
    }

    /**
     * Closes the stream. Note that this <b>does not</b> close
     * the associated <tt>InputStream</tt>
     */
    public void close() {
        eof = true;
    }

    /**
     * @return the number of available bytes
     * @throws IOException if an I/O error occurs
     */
    public int available() 
        throws IOException {
        return 3 - outOffset;
    }

    /**
     * Encoded data buffer
     */
    protected byte[] decodeBuffer = new byte[4];

    /**
     * Output buffer
     */
    protected byte[] outBuffer = new byte[3];

    /**
     * Offset in the out buffer
     */
    protected int  outOffset = 3;

    /**
     * Controls whether the end of the input stream has been reached
     */
    protected boolean eof = false;

    /**
     * @return the next byte of data or -1 if the end of the 
     *         stream is reached.
     * @throws IOException if an I/O error occurs.
     */
    public int read() throws IOException {

        if (outOffset == 3) {
            if (eof || getNextAtom()) {
                eof = true;
                return -1;
            }
        }

        return ((int) outBuffer[outOffset++]) & 0xFF;
    }

    /**
     * @param out where the decoded content should go
     * @param offset in the out array
     * @param len the number of atoms to read
     * @return the number of atoms that were actually read
     * @throws IOException if an I/O error happens
     */
    public int read(final byte[] out, final int offset, final int len)
        throws IOException {

        int idx = 0;
        while (idx < len) {
            if (outOffset == 3) {
                if (eof || getNextAtom()) {
                    eof = true;
                    if (idx == 0) {
                        return -1;
                    } else {
                        return idx;
                    }
                }
            }

            out[offset + idx] = outBuffer[outOffset++];

            idx++;
        }
        return idx;
    }

    /**
     * @return true if the next atom has been read
     * @throws IOException if an I/O error happens
     */
    final boolean getNextAtom() throws IOException {
        int count, a, b, c, d;

        int off = 0;
        while (off != 4) {
            count = src.read(decodeBuffer, off, 4 - off);
            if (count == -1) {
                return true;
            }

            int in = off, out = off;
            while (in < off + count) {
                if ((decodeBuffer[in] != '\n') 
                    && (decodeBuffer[in] != '\r')
                    && (decodeBuffer[in] != ' ')) {
                    decodeBuffer[out++] = decodeBuffer[in];
                }
                in++;
            }

            off = out;
        }

        a = PEM_ARRAY[((int) decodeBuffer[0]) & 0xFF];
        b = PEM_ARRAY[((int) decodeBuffer[1]) & 0xFF];
        c = PEM_ARRAY[((int) decodeBuffer[2]) & 0xFF];
        d = PEM_ARRAY[((int) decodeBuffer[3]) & 0xFF];

        outBuffer[0] = (byte) ((a << 2) | (b >>> 4));
        outBuffer[1] = (byte) ((b << 4) | (c >>> 2));
        outBuffer[2] = (byte) ((c << 6) |  d);

        if (decodeBuffer[3] != '=') {
            // All three bytes are good.
            outOffset = 0;
        } else if (decodeBuffer[2] == '=') {
            // Only one byte of output.
            outBuffer[2] = outBuffer[0];
            outOffset = 2;
            eof = true;
        } else {
            // Only two bytes of output.
            outBuffer[2] = outBuffer[1];
            outBuffer[1] = outBuffer[0];
            outOffset = 1;
            eof = true;
        }
            
        return false;
    }
}
