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
package com.sun.perseus.parser;

/**
 * <code>AbstractParser</code> is the base class for parsers found
 * in this package. <br />
 * All parsers work on a <code>String</code> and the <code>AbstractParser</code>
 * keeps a reference to that string along with the current position
 * (@see #currentPos) and current character (@see current). <br />
 * The key methods for this class are <code>read</code> which reads the next
 * character in the parsed string, <code>setString</code> which sets the string
 * to be parsed, and the utility methods <code>skipCommaSpaces</code>,
 * <code>skipSpaces</code> and <code>skipSpacesCommaSpaces</code> which can
 * be used by descendants to skip common separators.
 * <br />
 * For an implementation example, see {@link TransformListParser}.
 *
 * @version $Id: AbstractParser.java,v 1.2 2006/04/21 06:40:21 st125089 Exp $
 */
public abstract class AbstractParser {
    /**
     * The current position in the string
     */
    protected int currentPos;

    /**
     * The String being parsed
     */
    protected String s;

    /**
     * The current character being parsed
     * This is accessible by sub-classes
     */
    protected int current;


    /**
     * @return the next character. Returns -1 when the
     * end of the String has been reached.
     */
    protected final int read() {
        if (currentPos < s.length()) {
            return s.charAt(currentPos++);
        }
        return -1;
    }

    /**
     * Sets this parser's String. This also resets the
     * current position to 0
     *
     * @param str the string this parser should parse. Should
     *        not be null.
     */
    protected final void setString(final String str) {
        if (str == null) {
            throw new IllegalArgumentException();
        }

        this.s = str;
        this.currentPos = 0;
        this.current = -1;
    }

    /**
     * Skips the whitespaces in the current reader.
     */
    protected final void skipSpaces() {
        for (;;) {
            switch (current) {
            default:
                return;
            case 0x20:
            case 0x09:
            case 0x0D:
            case 0x0A:
            }
            current = read();
        }
    }

    /**
     * Skips the whitespaces and an optional comma.
     */
    protected final void skipCommaSpaces() {
        skipSepSpaces(',');
    }

    /**
     * Skips the whitespaces and an optional comma.
     *
     * @param sep seperator to skip in addition to spaces.
     */
    protected final void skipSepSpaces(final char sep) {
        wsp1: for (;;) {
            switch (current) {
            default:
                break wsp1;
            case 0x20:
            case 0x9:
            case 0xD:
            case 0xA:
            }
            current = read();
        }
        if (current == sep) {
            wsp2: for (;;) {
                switch (current = read()) {
                default:
                    break wsp2;
                case 0x20:
                case 0x9:
                case 0xD:
                case 0xA:
                }
            }
        }
    }

    /**
     * Skips wsp*,wsp* and throws an IllegalArgumentException
     * if no comma is found.
     */
    protected final void skipSpacesCommaSpaces() {
        skipSpaces();

        if (current != ',') {
            throw new IllegalArgumentException();
        }

        current = read();
        skipSpaces();
    }

    /**
     * Tests if the current substring (i.e. the substring beginning at the
     * current position) starts with the specified prefix.  If the current
     * substring starts with the specified prefix, the current character will
     * be updated to point to the character immediately following the last
     * character in the prefix; otherwise, the <code>currentPos</code> will
     * not be affected.  For example, if the string being parsed is
     * "timingAttr", and the current character is 'A':
     * <pre>
     *   currentStartsWith("Att") returns true, current == 'r'
     *   currentStartsWith("Attr") returns true, current == -1
     *   currentStartsWith("Attx") returns false, current == 'A'
     * </pre>
     *
     * @param str the prefix to be tested
     * @return <code>true</code> if the current substring starts with the
     * specified prefix.  The result is <code>false</code> if
     * <code>currentPos</code> is non-positive, or if the current substring
     * does not start with the specified prefix.
     */
    protected final boolean currentStartsWith(final String str) {
        if (currentPos <= 0) {
            return false;
        }
        if (s.startsWith(str, currentPos - 1)) {
            currentPos += str.length() - 1;
            current = read();
            return true;
        }
        return false;
    }
}

