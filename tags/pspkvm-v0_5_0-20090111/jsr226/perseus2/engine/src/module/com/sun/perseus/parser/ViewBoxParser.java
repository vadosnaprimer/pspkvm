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

package com.sun.perseus.parser;

/**
 * The <code>ViewBoxParser</code> class converts attributes conforming to the
 * SVG <a href="http://www.w3.org/TR/SVG11/paths.html#PathDataBNF">viewbox
 * syntax</a>
 * into an array of four floating point values corresponding to the input
 * string.
 *
 * @version $Id: ViewBoxParser.java,v 1.3 2006/04/21 06:40:44 st125089 Exp $
 */
public class ViewBoxParser extends NumberParser {
    /**
     * @param value the string containing the viewbox specification
     * @return an array of four float corresponding to the input
     *         viewbox specification.
     *         The method throws an <code>IllegalArgumentException</code>
     *         if the viewbox string is malformed.
     */
    public float[][] parseViewBox(final String value) {
        setString(value);
        current = read();

        if (current == -1) {
            return null;
        }

        float[][] vb = new float[3][];
        vb[0] = new float[2];
        vb[1] = new float[1];
        vb[2] = new float[1];
        skipSpaces();
        vb[0][0] = parseNumber();
        skipCommaSpaces();
        vb[0][1] = parseNumber();
        skipCommaSpaces();
        vb[1][0] = parseNumber();
        skipCommaSpaces();
        vb[2][0] = parseNumber();
        skipSpaces();
        if (current != -1) {
            throw new IllegalArgumentException();
        }

        // A negative value for <width> or <height> is an error
        if (vb[1][0] < 0 || vb[2][0] < 0) {
            throw new IllegalArgumentException();
        }
      
        return vb;
    }
}
