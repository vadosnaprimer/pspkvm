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

import com.sun.perseus.j2d.Path;

/**
 * The <code>PathParser</code> class converts attributes conforming to the
 * SVG <a href="http://www.w3.org/TR/SVG11/paths.html#PathDataBNF">path
 * syntax</a> with the
 * <a href="http://www.w3.org/TR/SVGMobile/#sec-shapes">limitation</a> of SVG
 * Tiny which says that SVG Tiny does not support <code>arc to</code> commands.
 *
 * @version $Id: PathParser.java,v 1.4 2006/04/21 06:40:37 st125089 Exp $
 */
public class PathParser extends NumberParser {
    /**
     * Current x and y positions in the path, set by
     * commands such as moveTo or lineTo.
     */
    private float currentX, currentY;

    /**
     * Last moveTo command.
     */
    private float lastMoveToX, lastMoveToY;

    /**
     * The smoothQCenter point is used for smootg quad curves
     */
    private float smoothQCenterX, smoothQCenterY;

    /**
     * The smoothQCenter point is used for smooth cubic curves
     */
    private float smoothCCenterX, smoothCCenterY;

    /**
     * The GeneralPath under construction
     */
    private Path p;

    /**
     * Returns the current working path. This can be used,
     * for example, when the parsePath method throws an error
     * to retrieve the state of the path at the time the error
     * occured
     * @return the <code>Path</code> built from the parsed
     *         <code>String</code>
     */
    public Path getPath() {
        return p;
    }

    /**
     * Parses the input <code>String</code> and returns the corresponding
     * <code>Path</code>. 
     *
     * @param s the <code>String</code> to parse.
     * @return the <code>GeneralPath</code> built from the parsed
     * <code>String</code>.
     */
    public Path parsePoints(final String s) {
        setString(s);
        p = preparePath();

        setString(s);
        
        current = read();

        skipSpaces();
        if (current == -1) {
            // No coordinate pair
            return p;
        }

        // Initial moveTo
        float x = parseNumber();
        skipCommaSpaces();
        float y = parseNumber();
        p.moveTo(x, y);
        lastMoveToX = x;
        lastMoveToY = y;

        while (current != -1) {
            skipSpaces();
            if (current != -1) {
                skipCommaSpaces();
                x = parseNumber();
                skipCommaSpaces();
                y = parseNumber();
                p.lineTo(x, y);
            }
        }

        return p;
    }

    /**
     * Pre-parses the path data to allocate the optimal number of commands and data
     * for the path.
     *
     * @return p a path with the proper capacity for the comming path.
     */
    protected Path preparePath() {
        int commandCapacity = 0;
        int dataCapacity = 0;

        current = read();
        
        while (current != -1) {
            skipCommaSpaces();
            switch (current) {
            case 'z':
            case 'Z':
                commandCapacity++;
                break;
            case 'm':
            case 'l':
            case 'M':
            case 'L':
            case 'h':
            case 'H':
            case 'v':
            case 'V':
                commandCapacity++;
                dataCapacity += 1;
                break;
            case 'c':
            case 'C':
            case 's':
            case 'S':
                commandCapacity++;
                dataCapacity += 3;
                break;
            case 'q':
            case 'Q':
            case 't':
            case 'T':
                commandCapacity++;
                dataCapacity += 2;
                break;
            default:
                break;
            }
            current = read();
        }

        return new Path(commandCapacity, dataCapacity);
    }

    /**
     * Parses the input <code>String</code> and returns the corresponding
     * <code>Path</code> if no error is found. If an error occurs,
     * this method throws an <code>IllegalArgumentException</code>.
     *
     * @param s the <code>String</code> to parse.
     * @return the <code>Path</code> built from the parsed
     *         <code>String</code>
     */
    public Path parsePath(final String s) {
        setString(s);
        p = preparePath();

        setString(s);

        currentX = 0;
        currentY = 0;
        smoothQCenterX = 0;
        smoothQCenterY = 0;
        smoothCCenterX = 0;
        smoothCCenterY = 0;

        current = read();
        skipSpaces();

        // Multiple coordinate pairs after a moveto
        // are like a moveto followed by lineto
        switch(current) {
        case 'm':
            parsem();
            parsel();
            break;
        case 'M':
            parseM();
            parseL();
            break;
	case -1:
 	    //an empty path is valid.
 	    break;        
        default:
            throw new IllegalArgumentException();
        }

        loop: for (;;) {
            switch (current) {
            case 0xD:
            case 0xA: 
            case 0x20:
            case 0x9:
                current = read();
                break;
            case 'z':
            case 'Z':
                current = read();
                p.close();
                currentX = lastMoveToX;
                currentY = lastMoveToY;
                break;
            case 'm':
                parsem();
            case 'l':
                parsel();
                break;
            case 'M':
                parseM();
            case 'L':
                parseL();
                break;
            case 'h':
                parseh();
                break;
            case 'H':
                parseH();
                break;
            case 'v':
                parsev();
                break;
            case 'V':
                parseV();
                break;
            case 'c':
                parsec();
                break;
            case 'C':
                parseC();
                break;
            case 'q':
                parseq();
                break;
            case 'Q':
                parseQ();
                break;
            case 's':
                parses();
                break;
            case 'S':
                parseS();
                break;
            case 't':
                parset();
                break;
            case 'T':
                parseT();
                break;
            case -1:
                break loop;
            default:
                throw new IllegalArgumentException();
            }
          
        }

        skipSpaces();
        if (current != -1) {
            throw new IllegalArgumentException();
        }

        return p;
    }

    /**
     * Parses a 'm' command.
     */
    protected final void parsem() {
        current = read();
        skipSpaces();

        final float x = parseNumber();
        skipCommaSpaces();
        final float y = parseNumber();

        currentX += x;
        smoothQCenterX = currentX;
        smoothCCenterX = currentX;
        currentY += y;
        smoothQCenterY = currentY;
        smoothCCenterY = currentY;
        p.moveTo(smoothCCenterX, smoothCCenterY);
        lastMoveToX = smoothCCenterX;
        lastMoveToY = smoothCCenterY;

        skipCommaSpaces();
    }

    /**
     * Parses a 'l' command.
     */
    protected final void parsel() {
        if (current == 'l') {
            current = read();
        }
        skipSpaces();
        for (;;) {
            switch (current) {
            case '+': case '-': case '.':
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
                float x = parseNumber();
                skipCommaSpaces();
                float y = parseNumber();

                currentX += x;
                smoothQCenterX = currentX;
                smoothCCenterX = currentX;

                currentY += y;
                smoothQCenterY = currentY;
                smoothCCenterY = currentY;
                p.lineTo(smoothCCenterX, smoothCCenterY);
                break;
            default:
                return;
            }
            skipCommaSpaces();
        }
    }

    /**
     * Parses a 'M' command.
     */
    protected final void parseM() {
        current = read();
        skipSpaces();

        float x = parseNumber();
        skipCommaSpaces();
        float y = parseNumber();
          
        currentX = x;
        smoothQCenterX = x;
        smoothCCenterX = x;

        currentY = y;
        smoothQCenterY = y;
        smoothCCenterY = y;
        p.moveTo(x, y);
        lastMoveToX = x;
        lastMoveToY = y;

        skipCommaSpaces();
    }

    /**
     * Parses a 'L' command.
     */
    protected final void parseL() {
        if (current == 'L') {
            current = read();
        }
        skipSpaces();
        for (;;) {
            switch (current) {
            case '+': case '-': case '.':
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
                float x = parseNumber();
                skipCommaSpaces();
                float y = parseNumber();

                currentX = x;
                smoothQCenterX = x;
                smoothCCenterX = x;

                currentY = y;
                smoothQCenterY = y;
                smoothCCenterY = y;

                p.lineTo(smoothCCenterX, smoothCCenterY);
                break;
            default:
                return;
            }
            skipCommaSpaces();
        }
    }

    /**
     * Parses a 'h' command.
     */
    protected final void parseh() {
        current = read();
        skipSpaces();

        for (;;) {
            switch (current) {
            case '+': case '-': case '.':
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
                float x = parseNumber();
                currentX += x;
                smoothQCenterX = currentX;
                smoothCCenterX = currentX;

                smoothQCenterY = currentY;
                smoothCCenterY = currentY;
                p.lineTo(smoothCCenterX, smoothCCenterY);
                break;
            default:
                return;
            }
            skipCommaSpaces();
        }
    }

    /**
     * Parses a 'H' command.
     */
    protected final void parseH() {
        current = read();
        skipSpaces();

        for (;;) {
            switch (current) {
            case '+': case '-': case '.':
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
                float x = parseNumber();
                currentX = x;
                smoothQCenterX = x;
                smoothCCenterX = x;

                smoothQCenterY = currentY;
                smoothCCenterY = currentY;
                p.lineTo(smoothCCenterX, smoothCCenterY);
                break;
            default:
                return;
            }
            skipCommaSpaces();
        }
    }

    /**
     * Parses a 'v' command.
     */
    protected final void parsev() {
        current = read();
        skipSpaces();

        for (;;) {
            switch (current) {
            case '+': case '-': case '.':
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
                float y = parseNumber();
                smoothQCenterX = currentX;
                smoothCCenterX = currentX;

                currentY += y;
                smoothQCenterY = currentY;
                smoothCCenterY = currentY;
                p.lineTo(smoothCCenterX, smoothCCenterY);
                break;
            default:
                return;
            }
            skipCommaSpaces();
        }
    }

    /**
     * Parses a 'V' command.
     */
    protected final void parseV() {
        current = read();
        skipSpaces();

        for (;;) {
            switch (current) {
            case '+': case '-': case '.':
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
                float y = parseNumber();
                smoothQCenterX = currentX;
                smoothCCenterX = currentX;

                currentY = y;
                smoothQCenterY = y;
                smoothCCenterY = y;
                p.lineTo(smoothCCenterX, smoothCCenterY);
                break;
            default:
                return;
            }
            skipCommaSpaces();
        }
    }

    /**
     * Parses a 'c' command.
     */
    protected final void parsec() {
        current = read();
        skipSpaces();
       
        for (;;) {
            switch (current) {
            default:
                return;
            case '+': case '-': case '.':
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
            }

            float x1 = parseNumber();
            skipCommaSpaces();
            float y1 = parseNumber();
            skipCommaSpaces();
            float x2 = parseNumber();
            skipCommaSpaces();
            float y2 = parseNumber();
            skipCommaSpaces();
            float x = parseNumber();
            skipCommaSpaces();
            float y = parseNumber();

            smoothCCenterX = currentX + x2;
            smoothCCenterY = currentY + y2;
            smoothQCenterX = currentX + x;
            smoothQCenterY = currentY + y;
            p.curveTo(currentX + x1, currentY + y1,
                      smoothCCenterX, smoothCCenterY,
                      smoothQCenterX, smoothQCenterY);
            currentX = smoothQCenterX;
            currentY = smoothQCenterY;
            skipCommaSpaces();
        }
    }               

    /**
     * Parses a 'C' command.
     */
    protected final void parseC() {
        current = read();
        skipSpaces();
       
        for (;;) {
            switch (current) {
            default:
                return;
            case '+': case '-': case '.':
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
            }

            float x1 = parseNumber();
            skipCommaSpaces();
            float y1 = parseNumber();
            skipCommaSpaces();
            float x2 = parseNumber();
            skipCommaSpaces();
            float y2 = parseNumber();
            skipCommaSpaces();
            float x = parseNumber();
            skipCommaSpaces();
            float y = parseNumber();

            smoothCCenterX = x2;
            smoothCCenterY = y2;
            currentX = x;
            currentY = y;
            p.curveTo(x1, y1, smoothCCenterX, smoothCCenterY, 
                      currentX, currentY);
            smoothQCenterX = currentX;
            smoothQCenterY = currentY;
            skipCommaSpaces();
        }
    }

    /**
     * Parses a 'q' command.
     */
    protected final void parseq() {
        current = read();
        skipSpaces();

        for (;;) {
            switch (current) {
            default:
                return;
            case '+': case '-': case '.':
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
            }

            float x1 = parseNumber();
            skipCommaSpaces();
            float y1 = parseNumber();
            skipCommaSpaces();
            float x = parseNumber();
            skipCommaSpaces();
            float y = parseNumber();

            smoothQCenterX = currentX + x1;
            smoothQCenterY = currentY + y1;
            currentX += x; 
            currentY += y;
            p.quadTo(smoothQCenterX, smoothQCenterY, currentX, currentY);
            smoothCCenterX = currentX;
            smoothCCenterY = currentY;

            skipCommaSpaces();
        }
    }

    /**
     * Parses a 'Q' command.
     */
    protected final void parseQ() {
        current = read();
        skipSpaces();

        for (;;) {
            switch (current) {
            default:
                return;
            case '+': case '-': case '.':
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
            }

            float x1 = parseNumber();
            skipCommaSpaces();
            float y1 = parseNumber();
            skipCommaSpaces();
            float x = parseNumber();
            skipCommaSpaces();
            float y = parseNumber();

            smoothQCenterX = x1;
            smoothQCenterY = y1;
            currentX = x;
            currentY = y;
            p.quadTo(smoothQCenterX, smoothQCenterY, currentX, currentY);
            smoothCCenterX = currentX;
            smoothCCenterY = currentY;
            skipCommaSpaces();
        }
    }

    /**
     * Parses a 's' command.
     */
    protected final void parses() {
        current = read();
        skipSpaces();

        for (;;) {
            switch (current) {
            default:
                return;
            case '+': case '-': case '.':
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
            }
          
            float x2 = parseNumber();
            skipCommaSpaces();
            float y2 = parseNumber();
            skipCommaSpaces();
            float x = parseNumber();
            skipCommaSpaces();
            float y = parseNumber();

            float smoothX = currentX * 2 - smoothCCenterX;
            float smoothY = currentY * 2 - smoothCCenterY;
            smoothCCenterX = currentX + x2;
            smoothCCenterY = currentY + y2;
            currentX += x;
            currentY += y;

            p.curveTo(smoothX, smoothY,
                      smoothCCenterX, smoothCCenterY,
                      currentX, currentY);

            smoothQCenterX = currentX;
            smoothQCenterY = currentY;
            skipCommaSpaces();
        }
    }               

    /**
     * Parses a 'S' command.
     */
    protected final void parseS() {
        current = read();
        skipSpaces();

        for (;;) {
            switch (current) {
            default:
                return;
            case '+': case '-': case '.':
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
            }
          
            float x2 = parseNumber();
            skipCommaSpaces();
            float y2 = parseNumber();
            skipCommaSpaces();
            float x = parseNumber();
            skipCommaSpaces();
            float y = parseNumber();

            float smoothX = currentX * 2 - smoothCCenterX;
            float smoothY = currentY * 2 - smoothCCenterY;
            currentX = x;
            currentY = y;
            p.curveTo(smoothX, smoothY,
                      x2, y2,
                      currentX, currentY);
            smoothCCenterX = x2;
            smoothCCenterY = y2;
            smoothQCenterX = currentX;
            smoothQCenterY = currentY;

            skipCommaSpaces();
        }
    }               

    /**
     * Parses a 't' command.
     */
    protected final void parset() {
        current = read();
        skipSpaces();

        for (;;) {
            switch (current) {
            default:
                return;
            case '+': case '-': case '.':
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
            }

            float x = parseNumber();
            skipCommaSpaces();
            float y = parseNumber();
          
            smoothQCenterX = currentX * 2 - smoothQCenterX;
            smoothQCenterY = currentY * 2 - smoothQCenterY;
            currentX += x;
            currentY += y;
            p.quadTo(smoothQCenterX, smoothQCenterY, currentX, currentY);
            smoothCCenterX = currentX;
            smoothCCenterY = currentY;
            skipCommaSpaces();
        }               
    }

    /**
     * Parses a 'T' command.
     */
    protected final void parseT() {
        current = read();
        skipSpaces();

        for (;;) {
            switch (current) {
            default:
                return;
            case '+': case '-': case '.':
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
            }

            float x = parseNumber();
            skipCommaSpaces();
            float y = parseNumber();

            smoothQCenterX = currentX * 2 - smoothQCenterX;
            smoothQCenterY = currentY * 2 - smoothQCenterY;
            currentX = x;
            currentY = y;
            p.quadTo(smoothQCenterX, smoothQCenterY,
                     currentX, currentY);
            smoothCCenterX = currentX;
            smoothCCenterY = currentY;
            skipCommaSpaces();
        }               
    }
}
