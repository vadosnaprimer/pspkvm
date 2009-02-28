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

package com.sun.perseus.util;

/**
 * This simple tokenizer is able to break down a String into tokens,
 * given a single delimiter character.
 *
 * @version $Id: SimpleTokenizer.java,v 1.3 2006/04/21 06:35:58 st125089 Exp $
 */
public final class SimpleTokenizer {
    /**
     * The data to parse.
     */
    protected String data;

    /**
     * The length of the string to parse.
     */
    protected int length;

    /**
     * The array of delimiters.
     */
    protected char[] del;

    /**
     * The current parse index.
     */
    protected int cur = 0;

    /**
     * @param data the string to tokenizer. Should not be null.
     * @param delimiters each character in the string is considered to be a 
     *        delimiter. Should not be null.
     * @return an array of tokens.
     */
    public SimpleTokenizer(String data, String delimiters) {
        if (data == null || delimiters == null) {
            throw new IllegalArgumentException();
        }

        this.data = data;
        this.length = data.length();
        del = delimiters.toCharArray();

        // Initialize by skipping delimiters.
        skipDelimiters();
    }

    /**
     * Moves the current position to the first next character which is
     * not a delimiter.
     */
    void skipToken() {
        while (cur < length && !curIsDelimiter()) {
            cur++;
        }
    }

    /**
     * Moves the current position to the first next character which is
     * a delimiter.
     */
    void skipDelimiters() {
        while (cur < length && curIsDelimiter()) {
            cur++;
        }
    }

    /**
     * @return true if the current character is a delimiter.
     */
    boolean curIsDelimiter() {
        char c = data.charAt(cur);
        for (int i = 0; i < del.length; i++) {
            if (c == del[i]) {
                return true;
            }
        }
        return false;
    }

    /**
     * @returns the nextToken
     */
    public String nextToken() {
        if (!hasMoreTokens()) {
            return null;
        }

        // Now, build the new token.
        int s = cur;
        cur++;
        skipToken();
        int e = cur;

        // Skip all characters, starting at the current position, which
        // match one of the delimiters.
        skipDelimiters();

        return data.substring(s, e);
    }

    /**
     * @return true if there are more tokens available.
     */
    public boolean hasMoreTokens() {
        return cur < length;
    }

    /**
     * @return the number of tokens
     */
    public int countTokens() {
        int n = 0;
        int tmpCur = cur;

        cur = 0;
        while (cur < length) {
            skipDelimiters();
            if (cur < length) {
                n++;
            }
            skipToken();
        }

        cur = tmpCur;

        return n;
    }
}
