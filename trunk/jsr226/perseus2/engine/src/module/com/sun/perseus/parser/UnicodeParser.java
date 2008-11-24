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
 * The <code>UnicodeParser</code> class converts attributes conforming to the
 * <code>&lt;hkern&gt;</code>'s <code>u1/u2</code> attributes syntax.
 *
 * @version $Id: UnicodeParser.java,v 1.2 2006/04/21 06:40:32 st125089 Exp $
 */
public class UnicodeParser extends AbstractParser {
    /**
     * Parses the input unicode range value and turns it into a 
     * set of two (possibly identical) unicode range values.
     *
     * @param unicode unicode range string to parse
     * @return an array of unicode ranges of size 2.
     * @throws IllegalArgumentException if unicode is null.
     */
    public int[][] parseUnicode(final String unicode) 
        throws IllegalArgumentException {

        setString(unicode);
        
        if (unicode.length() == 0) {
            throw new IllegalArgumentException();
        }
        
        // Slow motion parser to simplify memory allocation

        // First count the number of ranges (',' seperated)
        int ranges = 1;
        while ((current = read()) != -1) {
            if (current == ',') {
                ranges++;
            }
        }

        setString(unicode);
        current = read();

        int[][] result = new int[ranges][];
        int cur = 0;
        while (current != -1) {
            if (current == 'U') {
                result[cur] = parseUnicodeRange(',');
            } else {
                if (current == ',') {
                    throw new IllegalArgumentException();
                }
                result[cur] = new int[2];
                result[cur][0] = current;
                result[cur][1] = current;
                current = read();
                if (current != ',') {
                    if (current != -1) {
                        throw new IllegalArgumentException();
                    }
                } else {
                    current = read();
                }
            }
            
            cur++;
        }

        return result;
    }

    /**
     * @param endOn specifies the character value that defines the end of the 
     *        unicode value.
     * @return an array of two integers defining the lower and upper values in
     *         the unicode range.
     */
    protected int[] parseUnicodeRange(final char endOn) {
        current = read();
        if (current != '+') {
            throw new IllegalArgumentException();
        }

        // Now, read the first unicode value. The acceptable 
        // values are: [0-9A-Fa-f?]
        StringBuffer sb = new StringBuffer();
        current = read();
        loop : while (current != -1 && current != ',') {
            switch (current) {
            case '0': case '1': case '2': case '3': case '4': case '5':
            case '6': case '7': case '8': case '9': case 'a': case 'b':
            case 'c': case 'd': case 'e': case 'f': case 'A': case 'B':
            case 'C': case 'D': case 'E': case 'F': case '?':
                break;
            case '-':
                break loop;
            default:
                throw new IllegalArgumentException();
            }
            sb.append((char) current);
            current = read();
        }

        // If we hit a '-', it means that the 
        // first value is a plain hex value
        int[] result = new int[2];
        try {
            if (current == '-') {
                result[0] = Integer.parseInt(sb.toString(), 16);
                current = read();
                int v = 0;
                int c = 0;
                int cur = 0;
                while (current != -1 && current != ',') {
                    switch (current) {
                    case '0': case '1': case '2': case '3': case '4':
                    case '5': case '6': case '7': case '8': case '9':
                        v = current - 0x30;
                                break;
                    case 'a': case 'b': case 'c': case 'd': case 'e':case 'f':
                        v = current - 0x57;
                        break;
                    case 'A': case 'B': case 'C': case 'D': case 'E': case 'F':
                        v = current - 0x37;
                        break;
                    default:
                        throw new IllegalArgumentException();
                    }
                    if (c > 0) {
                        cur <<= 4;
                    }
                    cur |= (0xff & v);
                    c++;
                    current = read();
                }
                if (c == 0) {
                    throw new IllegalArgumentException();
                }
                result[1] = cur;
            } else {
                String low = sb.toString();
                String high = low;
                low = low.replace('?', '0');
                high = high.replace('?', 'F');
                result[0] = Integer.parseInt(low, 16);
                result[1] = Integer.parseInt(high, 16);
            }
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException();
        }
        
        return result;
    }

}
