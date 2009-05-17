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
 * This class represents a parser with support for numbers. Note that
 * the parameter-less form of the <code>parseNumber</code> methods is meant
 * for use by subclasses (e.g., <code>TransformListParser</code>).
 *
 * @version $Id: NumberParser.java,v 1.2 2006/04/21 06:40:35 st125089 Exp $
 */
public class NumberParser extends AbstractParser {
    /**
     * Parses the content of the input String and converts it to a float.
     *
     * @param numberString the value to parse
     * @return the corresponding single precision floating point value.
     */
    public float parseNumber(final String numberString) {
        setString(numberString);
        return parseNumber(true);
    }

    /**
     * Parses a float value from the current position in the string
     *
     * @return floating point value corresponding to the parsed string.
     */
    public float parseNumber() {
        return parseNumber(false);
    }

    /**
     * Parses the next float value in the string.
     *
     * @param eos If eos is set to true, then there should be no more
     * characters at the end of the string.
     * @return floating point value corresponding to the parsed string.
     *         An <code>IllegalArgumentException</code> is thrown if
     *         the next number in the string
     *         does not have a valid number syntax or if eos is true
     *         and there are more characters in the string after the
     *         number.
     */
    public float parseNumber(final boolean eos) {
        int     mant     = 0;
        int     mantDig  = 0;
        boolean mantPos  = true;
        boolean mantRead = false;

        int     exp      = 0;
        int     expDig   = 0;
        int     expAdj   = 0;
        boolean expPos   = true;

        // Only read the next character if the
        // current one is -1
        if (current == -1) {
            current  = read();
        }

        // Parse the initial +/- sign if any
        switch (current) {
        case '-':
            mantPos = false;
        case '+':
            current = read();
        default:
            // nothing
        }

        // Now, parse the mantisse
        m1: switch (current) {
        default:
            throw new IllegalArgumentException("" + (char) current);

        case '.':
            break;

        case '0':
            mantRead = true;
            l: for (;;) {
                current = read();
                switch (current) {
                case '1': case '2': case '3': case '4':
                case '5': case '6': case '7': case '8': case '9':
                    break l;
                case '.': case 'e': case 'E':
                    break m1;
                case -1:
                    break m1;
                default:
                    if (eos) {
                        throw new IllegalArgumentException
                            (">" + (char) current + "<");
                    } else {
                        return 0;
                    }
                case '0': // <!>
                }
            }

        case '1': case '2': case '3': case '4':
        case '5': case '6': case '7': case '8': case '9':
            mantRead = true;
            l: for (;;) {
                if (mantDig < 9) {
                    mantDig++;
                    mant = mant * 10 + (current - '0');
                } else {
                    expAdj++;
                }
                current = read();
                switch (current) {
                default:
                    break l;
                case '0': case '1': case '2': case '3': case '4':
                case '5': case '6': case '7': case '8': case '9':
                }              
            }
        }
      
        // If we hit a point, parse the fractional part
        if (current == '.') {
            current = read();
            m2: switch (current) {
            default:
            case 'e': case 'E':
                if (!mantRead) {
                    throw new IllegalArgumentException();
                }
                break;

            case '0':
                if (mantDig == 0) {
                    l: for (;;) {
                        current = read();
                        expAdj--;
                        switch (current) {
                        case '1': case '2': case '3': case '4':
                        case '5': case '6': case '7': case '8': case '9':
                            break l;
                        default:
                            break m2;
                        case '0':
                        }
                    }
                }
            case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
                l: for (;;) {
                    if (mantDig < 9) {
                        mantDig++;
                        mant = mant * 10 + (current - '0');
                        expAdj--;
                    }
                    current = read();
                    switch (current) {
                    default:
                        break l;
                    case '0': case '1': case '2': case '3': case '4':
                    case '5': case '6': case '7': case '8': case '9':
                    }
                }
            }
        }

        // Parse the exponent
        switch (current) {
        case 'e': case 'E':
            current = read();
            switch (current) {
            default:
                throw new IllegalArgumentException();
            case '-':
                expPos = false;
            case '+':
                current = read();
                switch (current) {
                default:
                    throw new IllegalArgumentException();
                case '0': case '1': case '2': case '3': case '4':
                case '5': case '6': case '7': case '8': case '9':
                }
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
            }
          
            en: switch (current) {
            case '0':
                l: for (;;) {
                    current = read();
                    switch (current) {
                    case '1': case '2': case '3': case '4':
                    case '5': case '6': case '7': case '8': case '9':
                        break l;
                    default:
                        break en;
                    case '0':
                    }
                }

            case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
                l: for (;;) {
                    if (expDig < 3) {
                        expDig++;
                        exp = exp * 10 + (current - '0');
                    }
                    current = read();
                    switch (current) {
                    default:
                        break l;
                    case '0': case '1': case '2': case '3': case '4':
                    case '5': case '6': case '7': case '8': case '9':
                    }
                }
            }
        default:
        }

        if (eos && current != -1) {
            throw new IllegalArgumentException();
        }

        if (!expPos) {
            exp = -exp;
        }
        exp += expAdj;
        if (!mantPos) {
            mant = -mant;
        }

        return buildFloat(mant, exp);
    }

    /**
     * Computes a float from mantissa and exponent.
     *
     * @param mant the mantissa for the floating point value to create
     * @param exp the exponent for the floating point value to create
     * @return a single precision floating point value
     *         corresponding to the input mantissa/exponent
     *         pair.
     */
    public static float buildFloat(int mant, final int exp) {
        if (exp < -37 || mant == 0) {
            return 0f;
        }

        if (exp >  37) {
            if (mant > 0) {
                return Float.POSITIVE_INFINITY;
            } else {
                return Float.NEGATIVE_INFINITY;
            }
        }

        if (exp == 0) {
            return mant;
        }
          
        if (mant >= 1 << 26) {
            mant++;  // round up trailing bits if they will be dropped.
        }

        if (exp > 0) {
            return mant * POW_10[exp];
        } else {
            return mant / POW_10[-exp];
        }
    }

    /**
     * Array of powers of ten.
     */
    private static final float[] POW_10 = new float [38];
    static {
        float cur = 0.1f;
        for (int i = 0; i < POW_10.length; i++) {
            POW_10[i] = cur * 10;
            cur = POW_10[i];
        }
    };
}

