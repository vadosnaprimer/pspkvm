/*
 *   )
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

package com.sun.cldc.i18n.j2me;

import java.io.*;
import com.sun.cldc.i18n.*;
/**
 * Generic interface for stream conversion reading of 
 * specific character encoded input streams.
 */
public class Gen_Reader extends StreamReader {
    /** Saved encoding string from construction. */
    private String enc;
    /** Native handle for conversion routines. */
    private int id;
    /** Local buffer to read converted characters. */
    private byte[] buf;
    /** Maximum length of characters in local buffer. */
    private int maxByteLen;

    /**
     * Constructor for generic reader.
     * @param inp_enc character encoding to use for byte to character
     * conversion.
     * @exception ClassNotFoundException is thrown if the conversion
     * class is not available
     */
    Gen_Reader(String inp_enc) throws ClassNotFoundException {
        id = Conv.getHandler(inp_enc);
        if (id == -1) {
            // this lets Helper throw UnsupportedEncodingException
            throw new ClassNotFoundException();
        }
        enc = inp_enc;
        maxByteLen = Conv.getMaxByteLength(id);
        pos = maxByteLen;
        tmp = new byte[pos];
        buf = new byte[pos];
    }

    /**
     * Generic routine to open an InputStream with a specific
     * character encoding.
     * 
     * @param in the input stream to process
     * @param open_enc the character encoding of the input stream
     * @return Reader instance for converted characters
     * @throws UnsupportedEncodingException if encoding is not supported
     */
    public Reader open(InputStream in, String open_enc) 
        throws UnsupportedEncodingException {
        if (!open_enc.equals(enc)) {
            throw new UnsupportedEncodingException();
        }
        init();
        return super.open(in, open_enc);
    }

    /**
     * Read a single converted character.
     *
     * @return a single converted character
     * @exception IOException is thrown if the input stream 
     * could not be read for the raw unconverted character
     */
    synchronized public int read() throws IOException {
        int c = get();
        if (c == -1) {
            return -1;
        }

        char[] cb = {(char)0xFFFD};

        int bufLen = 0;
        buf[bufLen++] = (byte) c;

        boolean eof = false;

        for (int i = 1; i < maxByteLen; i++) {
            c = get();
            if (c == -1) {
                eof = true;
                break;
            }
            buf[bufLen++] = (byte) c;
        }

        int bytelen = Conv.getByteLength(id, buf, 0, bufLen);

        /* the byte indicates that there are tailing bytes */
        if (bytelen == -1) {
            /* if the stream is in the end, throw IOException */
            if (eof) {
                throw new IOException(/* "incomplete byte" */);
            }

            /* put the read ahead bytes back to the stream */
            for (int i = bufLen; i > 1; i--) {
                put(buf[--bufLen]);
            }
            /* return the leading byte as an unknown character */
            return cb[0];
        }

        /* the byte is invalid */
        if (bytelen == 0) {
            for (int i = bufLen; i > 1; i--) {
                put(buf[--bufLen]);
            }
            return cb[0];
        }

        /* put the read ahead bytes back to the stream */
        if (bytelen < bufLen) {
            for (int i = bufLen; i > bytelen; i--) {
                put(buf[--bufLen]);
            }
        }

        int convLen = Conv.byteToChar(id, buf, 0, bufLen, cb, 0, 1);

        if (convLen != 1) {
            throw new IOException(/* "Converter error" */);
        }

        return cb[0];
    }

    /**
     * Read a block of converted characters.
     *
     * @param cbuf output buffer for converted characters read
     * @param off initial offset into the provided buffer
     * @param len length of characters in the buffer
     * @return the number of converted characters, or -1 
     * if an error occurred in the input arguments
     * @exception IOException is thrown if the input stream 
     * could not be read for the raw unconverted character
     */
    synchronized public int read(char cbuf[], int off, int len) 
        throws IOException {
        /* first, check that the stream has been reached to eof */
        int c = get();
        if (c == -1) {
            return -1;
        }
        put(c); /* put it back */

        int maxlen = len * maxByteLen;
        if (buf.length < maxlen) {
            buf = new byte[maxlen];
        }

        int bufLen = readNumOfChars(buf, len);

        int ret = 0;
        if (bufLen > 0) {
            ret = Conv.byteToChar(id, buf, 0, bufLen, cbuf, off, len);
        }

        if (buf.length > maxByteLen) {
            buf = new byte[maxByteLen];
        }

        return ret;
    }

    /**
     * Skip over a number of bytes in the input stream.
     * 
     * @param n number of bytes to bypass from the input stream.
     * @return the number of characters skipped
     * @throws IOException if an I/O error occurs
     */
    public long skip(long n) throws IOException {
        if (n < 0L) {
            throw new IllegalArgumentException("skip value is negative");
        }

        /* see the stream has been reached to eof */
        int c = get();
        if (c == -1) {
            return 0;
        }
        put(c);

        return readNumOfChars(null, (int) n);
    }

    /** 
     * Get the size of the converted bytes as a Unicode 
     * byte array.
     *
     * @param c array of bytes to compute size
     * @param offset offset in the provided buffer
     * @param length length of bytes to process
     * @return length of converted characters.
     */
    public int sizeOf(byte[] c, int offset, int length) {
        return Conv.sizeOfByteInUnicode(id, c, offset, length);
    }

    /**
     * Reset the stream.
     *
     * @exception  IOException  If an I/O error occurs
     */
    public void reset() throws IOException {
        init();
    }

    /**
     * Read a specific number of characters from the 
     * input stream.
     * 
     * @param b array of bytes to read
     * @param num count of bytes to read
     * @return number of characters read
     * @exception IOException is thrown, if an error occurred 
     * reading the raw input stream
     */
    private int readNumOfChars(byte[] b, int num) throws IOException {
        int charsRead = 0;
        int bbLen = 0;
        byte bb[];

        if (b == null) {
            bb = new byte[num * maxByteLen];
        } else {
            bb = b;
        }

        boolean eof = false;

        while (charsRead < num) {

            int c = get();
            if (c == -1) {
                break;
            }

            int offset = bbLen;

            bb[bbLen++] = (byte) c;

            for (int i = 1; i < maxByteLen; i++) {
                c = get();
                if (c == -1) {
                    eof = true;
                    break;
                }
                bb[bbLen++] = (byte) c;
            }

            int readNum = bbLen - offset;
            int bytelen = Conv.getByteLength(id, bb, offset, readNum);

            /* the byte indicates that there are tailing bytes */
            if (bytelen == -1) {
                /* save the leading byte for the next read */
                if (eof) {
                    for (int i = readNum; i > 0; i--) {
                        put(bb[--bbLen]);
                    }
                    break;
                }
                /* put the rest of bytes back into the stream */
                for (int i = readNum; i > 1; i--) {
                    put(bb[--bbLen]);
                }
            /* the byte is invalid */
            } else if (bytelen == 0) {
                for (int i = readNum; i > 1; i--) {
                    put(bb[--bbLen]);
                }
            } else if (bytelen < readNum) {
                for (int i = readNum; i > bytelen; i--) {
                    put(bb[--bbLen]);
                }
            }

            charsRead++;
        }

        if (b != null) {
            return bbLen;
        } else {
            return charsRead;
        }
    }
    /** Local buffer for conversion routines. */
    private byte[] tmp;
    /** Current position in the temporary buffer. */
    private int pos;

    /**
     * Get a byte from the temporary buffer or from the 
     * input stream.
     * @return the next byte from the input stream or local buffer
     * @exception IOException is thrown, if an error occurred 
     * reading the raw input stream
     */
    private int get() throws IOException {
	if (pos < tmp.length) {
	    return tmp[pos++] & 0xff;
	}
	return in.read();
    }

    /**
     * Put a byte back into the temporary buffer.
     * @param b the character to put back in the temporary buffer
     * @exception IOException is thrown, if putting a character back 
     * past the beginning of the local temporary buffer
     */
    private void put(int b) throws IOException {
	if (pos == 0) {
	    throw new IOException();
	}
	tmp[--pos] = (byte) b;
    }

    /** reset the state */
    private void init() {
        pos = maxByteLen;
    }
}
