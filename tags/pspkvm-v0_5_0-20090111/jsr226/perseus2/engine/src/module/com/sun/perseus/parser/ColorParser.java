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

import com.sun.perseus.platform.MathSupport;

import java.util.Hashtable;

import com.sun.perseus.util.SVGConstants;

import com.sun.perseus.j2d.RGB;

/**
 * The <code>ColorParser</code> class parses CSS 2 color values as
 * defined in the SVG specification for SVG Tiny:<br />
 * <pre>
 * color: hex3Color | hex6Color | rgbColor | predefinedColor
 * hex3Color: '#' hexDigit hexDigit hexDigit
 * hexDigit: 0|1|2|3|4|5|6|7|8|9|A|a|B|b|C|c|D|d|E|e|F|f
 * hex6Color: '#' hexDigit hexDigit hexDigi hexDigit hexDigit hexDigit
 * rgbColor: rgbColorInt | rgbColorPercent
 * rgbColorInt: rgbPrefix wsp* colInt wsp* comma wsp* colInt wsp*
 *              comma wsp* colInt wsp* rgbSuffix
 * rgbPrefix: 'rgb('
 * wsp: ' '
 * comma: ','
 * colInt: int[0-255]
 * rgbColorPercent: rgbPrefix wsp* colPct wsp* comma wsp* colPct wsp*
 *                  comma wsp* colPct wsp* rgbSuffix
 * colPct: pctInt '%'
 * pctInt: int[0-100]
 * predefinedColor: 'black' | 'silver' | 'gray' | 'white' | 'maroon' | 'red'
 *                  | 'purple' | 'fuchia' | 'green' | 'lime' | 'olive'
 *                  | 'yellow' | 'navy' | 'blue' | 'teal' | 'aqua'
 * </pre>
 *
 * The above captures the verbal description of the CSS 2 specification
 * for <a href="http://www.w3.org/TR/REC-CSS2/syndata.html#color-units">CSS 2
 * Color Values</a>.
 *
 * @version $Id: ColorParser.java,v 1.2 2006/04/21 06:40:26 st125089 Exp $
 */
public class ColorParser extends NumberParser implements SVGConstants {
    /**
     * Contains the 16 predefined XHTML color values supported by
     * SVG Tiny
     * @see <a href="http://www.w3.org/TR/SVGMobile/#sec-data-types">SVG
     *      Tiny Data Types</a>
     */
    private static Hashtable predefinedCssColors = new Hashtable();

    /**
     * Initialize the predefined color map
     * @see #predefinedCssColors
     */
    static {
        predefinedCssColors.put(CSS_BLACK_VALUE,
                                RGB.black);
        predefinedCssColors.put(CSS_SILVER_VALUE,
                                new RGB(192, 192, 192));
        predefinedCssColors.put(CSS_GRAY_VALUE,
                                new RGB(128, 128, 128));
        predefinedCssColors.put(CSS_WHITE_VALUE,
                                RGB.white);
        predefinedCssColors.put(CSS_MAROON_VALUE,
                                new RGB(128, 0, 0));
        predefinedCssColors.put(CSS_RED_VALUE,
                                RGB.red);
        predefinedCssColors.put(CSS_PURPLE_VALUE,
                                new RGB(128, 0, 128));
        predefinedCssColors.put(CSS_FUCHSIA_VALUE,
                                new RGB(255, 0, 255));
        predefinedCssColors.put(CSS_GREEN_VALUE,
                                new RGB(0, 128, 0));
        predefinedCssColors.put(CSS_LIME_VALUE,
                                RGB.green);
        predefinedCssColors.put(CSS_OLIVE_VALUE,
                                new RGB(128, 128, 0));
        predefinedCssColors.put(CSS_YELLOW_VALUE,
                                RGB.yellow);
        predefinedCssColors.put(CSS_NAVY_VALUE,
                                new RGB(0, 0, 128));
        predefinedCssColors.put(CSS_BLUE_VALUE,
                                RGB.blue);
        predefinedCssColors.put(CSS_TEAL_VALUE,
                                new RGB(0, 128, 128));
        predefinedCssColors.put(CSS_AQUA_VALUE,
                                new RGB(0, 255, 255));
    }

    /**
     * Converts the input value to a Color object. See the class documentation
     * for a description of the expected input syntax.
     *
     * @param colorString Should be a string which is not null, not empty and
     *              interned. If the input value does not meet these
     *              criterias, the behavior is unspecified. An
     *              IllegalArgumentException exception is thrown if
     *              the input value's syntax does not conform to the color
     *              syntax for SVG Tiny.
     * @return A <code>Color</code> object corresponding to the input string
     *         value.
     */
    public RGB parseColor(final String colorString) {
        setString(colorString);
        current = read();
        RGB c = null;

        if (current == '#') {
            c = parseHexCSSColor();
        } else if (current == 'r'
                   &&
                   (current = read()) == 'g'
                   &&
                   (current = read()) == 'b'
                   &&
                   (current = read()) == '(') {
            c = parseRgbCSSColor();
        } else if (current == 'n'
                   &&
                   (current = read()) == 'o'
                   &&
                   (current = read()) == 'n'
                   &&
                   (current = read()) == 'e') {
            return null;
        } else {
            c = (RGB) predefinedCssColors.get(s);
        }

        if (c == null) {
            throw new IllegalArgumentException();
        }

        return c;
    }

    /**
     * This helper method assumes the input 's' value is:
     * - trimmed, interned and lower case
     * - not 'inherit' nor 'none' nor 'currentColor'
     * - starts with '#' (it is a CSS Hex value)
     *
     * @return parsed <code>Color</code> value.
     */
    private RGB parseHexCSSColor() {
        if (s.length() == 4) {
            int r = hexCharToInt(s.charAt(1));
            int g = hexCharToInt(s.charAt(2));
            int b = hexCharToInt(s.charAt(3));
            return new RGB(r << 4 | r,
                             g << 4 | g,
                             b << 4 | b);
        } else if (s.length() == 7) {
            int r = hexCharToInt(s.charAt(1));
            int r2 = hexCharToInt(s.charAt(2));
            int g = hexCharToInt(s.charAt(3));
            int g2 = hexCharToInt(s.charAt(4));
            int b = hexCharToInt(s.charAt(5));
            int b2 = hexCharToInt(s.charAt(6));
            return new RGB(r << 4 | r2,
                             g << 4 | g2,
                             b << 4 | b2);
        }

        throw new IllegalArgumentException();
    }

    /**
     * Converts an hex character to its integer value
     *
     * @param c the character to be mapped to an int value.
     * @return int value corresponding to the input 'char' value.
     */
    static int hexCharToInt(final char c) {
        switch(c) {
        case '0': return 0;
        case '1': return 1;
        case '2': return 2;
        case '3': return 3;
        case '4': return 4;
        case '5': return 5;
        case '6': return 6;
        case '7': return 7;
        case '8': return 8;
        case '9': return 9;
        case 'a': case 'A': return 0xA;
        case 'b': case 'B': return 0xB;
        case 'c': case 'C': return 0xC;
        case 'd': case 'D': return 0xD;
        case 'e': case 'E': return 0xE;
        case 'f': case 'F': return 0xF;
        default:
            throw new IllegalArgumentException();
        }
    }

    /**
     * This helper method assumes the input value
     * starts with 'rgb(' (it is a CSS rgb() function value)
     *
     * @return parsed <code>Color</code> value
     */
    private RGB parseRgbCSSColor() {
        // Read the next character to move
        // to the first char after 'rgb('
        current = read();

        // Parse the red component
        float r = parseNumber();
        boolean isPercent = false;

        if (current == '%') {
            isPercent = true;
            current = read();
        }

        skipSpacesCommaSpaces();

        float g = parseNumber();

        if (current == '%') {
            if (!isPercent) {
                throw new IllegalArgumentException();
            }
            current = read();
        } else {
            if (isPercent) {
                throw new IllegalArgumentException();
            }
        }

        skipSpacesCommaSpaces();

        float b = parseNumber();

        if (current == '%') {
            if (!isPercent) {
                throw new IllegalArgumentException();
            }
            current = read();
        } else {
            if (isPercent) {
                throw new IllegalArgumentException();
            }
        }

        skipSpaces();
        if (current != ')') {
            String msg = ">";
            if (current == -1) {
                msg += "-1";
            } else {
                msg += ((char) current) + "< " + r + " " + g + " " + b;
            }
            throw new IllegalArgumentException (msg);
        }

        if (isPercent) {
            r = r < 0 ? 0 : r;
            r = r > 100 ? 100 : r;
            g = g < 0 ? 0 : g;
            g = g > 100 ? 100 : g;
            b = b < 0 ? 0 : b;
            b = b > 100 ? 100 : b;

            r = MathSupport.round(r * 2.55f);
            g = MathSupport.round(g * 2.55f);
            b = MathSupport.round(b * 2.55f);

            return new RGB((int) r, (int) g, (int) b);
        } else {
            r = r < 0 ? 0 : r;
            r = r > 255 ? 255 : r;
            g = g < 0 ? 0 : g;
            g = g > 255 ? 255 : g;
            b = b < 0 ? 0 : b;
            b = b > 255 ? 255 : b;

            return new RGB((int) r, (int) g, (int) b);
        }
    }
}
