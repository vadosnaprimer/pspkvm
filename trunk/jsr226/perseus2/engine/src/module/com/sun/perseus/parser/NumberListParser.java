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
 * The <code>NumberListParser</code> class converts attributes
 * conforming to the SVG Tiny definition of coordinate or number
 * list (see <a href="http://www.w3.org/TR/SVG11/types.html#BasicDataTypes">
 * Basic Data Types</a>)..
 *
 * @version $Id: NumberListParser.java,v 1.2 2006/04/21 06:40:19 st125089 Exp $
 */
public class NumberListParser extends NumberParser {
    /**
     * @param listStr the string containing the list of numbers
     * @param sep the separator between number values
     * @return An array of numbers
     */
    public float[] parseNumberList(final String listStr, final char sep) {
        setString(listStr);

        current = read();
        skipSpaces();
        
        boolean requireMore = false;
        float[] numbers = null;
        int cur = 0;
        for (;;) {
            if (current != -1) {
                float v = parseNumber(false);
                if (numbers == null) {
                    numbers = new float[1];
                } else if (numbers.length <= cur) {
                    float[] tmpNumbers = new float[numbers.length * 2];
                    System.arraycopy(numbers, 0, tmpNumbers, 0, numbers.length);
                    numbers = tmpNumbers;
                }
                numbers[cur++] = v;
            } else {
                if (!requireMore) {
                    break;
                } else {
                    throw new IllegalArgumentException();
                }
            }
            skipSpaces();
            requireMore = (current == sep);
            skipSepSpaces(sep);
        }
      
        if (numbers != null && cur != numbers.length) {
            float[] tmpNumbers = new float[cur];
            System.arraycopy(numbers, 0, tmpNumbers, 0, cur);
            numbers = tmpNumbers;
        }

        return numbers;
    }        
    /**
     * @param listStr the string containing the list of numbers
     * @return An array of numbers
     */
    public float[] parseNumberList(final String listStr) {
        return parseNumberList(listStr, ',');
    }
}
