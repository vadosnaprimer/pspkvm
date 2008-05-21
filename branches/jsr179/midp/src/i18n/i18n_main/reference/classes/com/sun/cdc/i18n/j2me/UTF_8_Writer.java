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

package com.sun.cdc.i18n.j2me;

import java.io.*;

/**
 * Writer for UTF-8 encoded output streams. NOTE: The UTF-8 writer only
 * supports UCS-2, or Unicode, to UTF-8 conversion. There is no support
 * for UTF-16 encoded characters outside of the Basic Multilingual Plane
 * (BMP). These are encoded in UTF-16 using previously reserved values
 * between U+D800 and U+DFFF. Additionally, the UTF-8 writer does not
 * support any character that requires 4 or more UTF-8 encoded bytes.
 */
public class UTF_8_Writer extends com.sun.cdc.i18n.StreamWriter {

    /**
     * Write a portion of an array of characters.
     *
     * @param  cbuf  Array of characters
     * @param  off   Offset from which to start writing characters
     * @param  len   Number of characters to write
     *
     * @exception  IOException  If an I/O error occurs
     */
    public void write(char cbuf[], int off, int len) throws IOException {
	byte[] outputByte = new byte[3];     // Never more than 3 encoded bytes
	char   inputChar;
	int    outputSize;
        int count = 0;

	while (count < len) {
	    inputChar = cbuf[off + count];
	    if (inputChar < 0x80) {
		outputByte[0] = (byte)inputChar;
		outputSize = 1;
	    } else if (inputChar < 0x800) {
		outputByte[0] = (byte)(0xc0 | ((inputChar >> 6) & 0x1f));
		outputByte[1] = (byte)(0x80 | (inputChar & 0x3f));
		outputSize = 2;
	    } else {
		outputByte[0] = (byte)(0xe0 | ((inputChar >> 12)) & 0x0f);
		outputByte[1] = (byte)(0x80 | ((inputChar >> 6) & 0x3f));
		outputByte[2] = (byte)(0x80 | (inputChar & 0x3f));
		outputSize = 3;
	    } 
	    out.write(outputByte, 0, outputSize);
	    count++;
	}
    }

    /**
     * Get the size in chars of an array of bytes.
     *
     * @param      array  Source buffer
     * @param      offset Offset at which to start counting characters
     * @param      length number of bytes to use for counting
     *
     * @return     number of characters that would be converted
     */
    public int sizeOf(char[] array, int offset, int length) {
	int outputSize = 0;
	int inputChar;

	while (offset < length) {
	    inputChar = array[offset];
	    if (inputChar < 0x80) {
		outputSize++;
	    } else if (inputChar < 0x800) {
		outputSize += 2;
	    } else {
		outputSize += 3;
	    } 
	    offset++;
	}
        return outputSize;
    }
}



