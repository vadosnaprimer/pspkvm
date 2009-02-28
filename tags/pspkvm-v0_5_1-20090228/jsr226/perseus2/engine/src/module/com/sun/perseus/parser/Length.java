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
 * The <code>LengthParser</code> returns <code>Length</code> objects
 * from its <code>parserLength</code> method.
 * <br />
 * The <code>Length</code> class captures the value of the length
 * valut along with its unit. The resolution into a user space
 * value depends on the context (e.g., in Perseus, the
 * {@link com.sun.perseus.builder.BuildContext} class holds the context)
 * defining things like the size of a pixel.
 *
 * @see LengthParser
 * @see com.sun.perseus.builder.BuilderUtil
 *
 * @version $Id: Length.java,v 1.2 2006/04/21 06:40:28 st125089 Exp $
 */
public class Length {
    /*
     * Possible Length unit types
     */

    /**
     * Plain length, no unit
     */
    public static final int SVG_LENGTHTYPE_NUMBER = 1;

    /**
     * Percentage unit, usually used for percentage of
     * viewport width or height.
     */
    public static final int SVG_LENGTHTYPE_PERCENTAGE = 2;

    /**
     * Centimeter unit
     */
    public static final int SVG_LENGTHTYPE_CM = 6;

    /**
     * Millimeter unit.
     */
    public static final int SVG_LENGTHTYPE_MM = 7;

    /**
     * Inch unit (2.54cm)
     */
    public static final int SVG_LENGTHTYPE_IN = 8;

    /**
     * Point unit. A point is 1/72th of an inch as
     * defined in the CSS 2 specification.
     */
    public static final int SVG_LENGTHTYPE_PT = 9;

    /**
     * Pica unit. A pica is 12 points as defined in
     * the CSS 2 specification.
     */
    public static final int SVG_LENGTHTYPE_PC = 10;

    /**
     * The unit value. One of the SVG_LENGTHTYPE_XXX values.
     */
    public int unit;

    /**
     * The actual length value, prior to unit convertion
     */
    public float value;

}
