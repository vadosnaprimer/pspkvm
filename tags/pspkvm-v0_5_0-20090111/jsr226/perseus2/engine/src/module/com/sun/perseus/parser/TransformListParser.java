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

import com.sun.perseus.platform.MathSupport;

import com.sun.perseus.j2d.Transform;

/**
 * The <code>TransformListParser</code> class converts attributes
 * conforming to the SVG
 * <a href="http://www.w3.org/TR/SVG11/coords.html#TransformAttribute">
 * transform</a>
 * syntax into <code>AffineTransform</code> objects.
 *
 * @version $Id: TransformListParser.java,v 1.2 2006/04/21 06:40:42 st125089 Exp $
 */
public class TransformListParser extends NumberParser {
    /**
     * Captures the transform built by this parser
     */
    private Transform transform;

    /**
     * @param txfStr the string containing the set of transform commands
     * @return An <code>AffineTransform</code> object corresponding to the
     *         input transform list.
     */
    public Transform parseTransformList(final String txfStr) {
        setString(txfStr);

        transform = new Transform(1, 0, 0, 1, 0, 0);

        // Parse leading wsp*
        current = read();
        skipSpaces();

        // Now, iterate on 'transforms?'
        loop2: for (;;) {
            switch (current) {
            case 'm':
                parseMatrix();
                break;
            case 'r':
                parseRotate();
                break;
            case 't':
                parseTranslate();
                break;
            case 's':
                current = read();
                switch (current) {
                case 'c':
                    parseScale();
                    break;
                case 'k':
                    parseSkew();
                    break;
                default:
                    throw new IllegalArgumentException();
                }
                break;
            case -1:
                break loop2;
            default:
                throw new IllegalArgumentException();
            }
            current = read();
            skipCommaSpaces();
        }
      
        return transform;
    }

    /**
     * Parses a matrix transform. 'm' is assumed to be the current character.
     */
    protected final void parseMatrix() {
        current = read();

        // Parse 'atrix wsp? ( wsp?'
        if (current != 'a') {
            throw new IllegalArgumentException();
        }
        current = read();
        if (current != 't') {
            throw new IllegalArgumentException();
        }
        current = read();
        if (current != 'r') {
            throw new IllegalArgumentException();
        }
        current = read();
        if (current != 'i') {
            throw new IllegalArgumentException();
        }
        current = read();
        if (current != 'x') {
            throw new IllegalArgumentException();
        }
        current = read();
        skipSpaces();
        if (current != '(') {
            throw new IllegalArgumentException();
        }
        current = read();
        skipSpaces();

        float a = parseNumber();
        skipCommaSpaces();
        float b = parseNumber();
        skipCommaSpaces();
        float c = parseNumber();
        skipCommaSpaces();
        float d = parseNumber();
        skipCommaSpaces();
        float e = parseNumber();
        skipCommaSpaces();
        float f = parseNumber();
       
        skipSpaces();

        if (current != ')') {
            throw new IllegalArgumentException("Expected ')' and got >"
                                               + (char) current + "<");
        }

        transform.mMultiply(new Transform(a, b, c, d, e, f));
    }

    /**
     * Parses a rotate transform. 'r' is assumed to be the current character.
     */
    protected final void parseRotate() {
        current = read();

        // Parse 'otate wsp? ( wsp?'
        if (current != 'o') {
            throw new IllegalArgumentException();
        }
        current = read();
        if (current != 't') {
            throw new IllegalArgumentException();
        }
        current = read();
        if (current != 'a') {
            throw new IllegalArgumentException();
        }
        current = read();
        if (current != 't') {
            throw new IllegalArgumentException();
        }
        current = read();
        if (current != 'e') {
            throw new IllegalArgumentException();
        }
        current = read();
        skipSpaces();

        if (current != '(') {
            throw new IllegalArgumentException();
        }
        current = read();
        skipSpaces();

        float theta = parseNumber();
        skipSpaces();
      
        switch (current) {
        case ')':
            transform.mRotate(theta);
            return;
        case ',':
            current = read();
            skipSpaces();
        default:
            // nothing.
        }
      
        float cx = parseNumber();
        skipCommaSpaces();
        float cy = parseNumber();
      
        skipSpaces();
        if (current != ')') {
            throw new IllegalArgumentException();
        }

        transform.mTranslate(cx, cy);
        transform.mRotate(theta);
        transform.mTranslate(-cx, -cy);
    }

    /**
     * Parses a translate transform. 't' is assumed to be
     * the current character.
     */
    protected final void parseTranslate() {
        current = read();

        // Parse 'ranslate wsp? ( wsp?'
        if (current != 'r') {
            throw new IllegalArgumentException();
        }
        current = read();
        if (current != 'a') {
            throw new IllegalArgumentException();
        }
        current = read();
        if (current != 'n') {
            throw new IllegalArgumentException();
        }
        current = read();
        if (current != 's') {
            throw new IllegalArgumentException();
        }
        current = read();
        if (current != 'l') {
            throw new IllegalArgumentException();
        }
        current = read();
        if (current != 'a') {
            throw new IllegalArgumentException();
        }
        current = read();
        if (current != 't') {
            throw new IllegalArgumentException();
        }
        current = read();
        if (current != 'e') {
            throw new IllegalArgumentException();
        }
        current = read();
        skipSpaces();
        if (current != '(') {
            throw new IllegalArgumentException();
        }
        current = read();
        skipSpaces();

        float tx = parseNumber();
        skipSpaces();

        switch (current) {
        case ')':
            transform.mTranslate(tx, 0);
            return;
        case ',':
            current = read();
            skipSpaces();
        default:
            // nothing
        }

        float ty = parseNumber();

        skipSpaces();
        if (current != ')') {
            throw new IllegalArgumentException();
        }

        transform.mTranslate(tx, ty);
    }

    /**
     * Parses a scale transform. 'c' is assumed to be the current character.
     */
    protected final void parseScale() {
        current = read();

        // Parse 'ale wsp? ( wsp?'
        if (current != 'a') {
            throw new IllegalArgumentException();
        }
        current = read();
        if (current != 'l') {
            throw new IllegalArgumentException();
        }
        current = read();
        if (current != 'e') {
            throw new IllegalArgumentException();
        }
        current = read();
        skipSpaces();
        if (current != '(') {
            throw new IllegalArgumentException();
        }
        current = read();
        skipSpaces();

        float sx = parseNumber();
        skipSpaces();

        switch (current) {
        case ')':
            transform.mScale(sx);
            return;
        case ',':
            current = read();
            skipSpaces();
        default:
            // nothing
        }

        float sy = parseNumber();

        skipSpaces();
        if (current != ')') {
            throw new IllegalArgumentException();
        }
      
        transform.mScale(sx, sy);
    }

    /**
     * Parses a skew transform. 'e' is assumed to be the current character.
     */
    protected final void parseSkew() {
        current = read();

        // Parse 'ew[XY] wsp? ( wsp?'
        if (current != 'e') {
            throw new IllegalArgumentException();
        }
        current = read();
        if (current != 'w') {
            throw new IllegalArgumentException();
        }
        current = read();

        boolean skewX = false;
        switch (current) {
        case 'X':
            skewX = true;
        case 'Y':
            break;
        default:
            throw new IllegalArgumentException();
        }
        current = read();
        skipSpaces();
        if (current != '(') {
            throw new IllegalArgumentException();
        }
        current = read();
        skipSpaces();

        float sk = parseNumber();

        skipSpaces();
        if (current != ')') {
            throw new IllegalArgumentException();
        }
      
        float tan = MathSupport.tan(MathSupport.toRadians(sk));

        if (skewX) {
            Transform shear = new Transform(1, 0, tan, 1, 0, 0);
            transform.mMultiply(shear);
            // transform.shear(tan, 0);
        } else {
            Transform shear = new Transform(1, tan, 0, 1, 0, 0);
            transform.mMultiply(shear);
            // transform.shear(0, tan);
        }
    }

}
