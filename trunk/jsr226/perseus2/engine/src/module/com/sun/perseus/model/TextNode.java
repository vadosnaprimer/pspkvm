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
package com.sun.perseus.model;

import com.sun.perseus.j2d.TextProperties;

import com.sun.perseus.util.SVGConstants;

/**
 * <code>TextNode</code> is the interface that all <code>ModelNode</code>
 * (see {@link com.sun.perseus.model.ModelNode ModelNode}) which support the 
 * definition of text properties (such as text-anchor or font-size) implement.
 *
 * IMPORTANT NOTE: setting a property automatically sets the inherited flag 
 * to false.
 *
 * @see ModelNode
 *
 * @version $Id: TextNode.java,v 1.3 2006/04/21 06:39:12 st125089 Exp $
 */
public interface TextNode extends DecoratedNode, TextProperties {
    // ===================================================================
    // Property indices. Values are used as masks in operations
    // ===================================================================

    /**
     * Controls the font's family
     */
    int PROPERTY_FONT_FAMILY        = 1 << 16;

    /**
     * Controls the font's height
     */
    int PROPERTY_FONT_SIZE          = 1 << 17;

    /**
     * Controls the font's slant style, oblique or plain
     */
    int PROPERTY_FONT_STYLE         = 1 << 18;

    /**
     * Controls the font's boldness
     */
    int PROPERTY_FONT_WEIGHT        = 1 << 19;

    /**
     * Controls how text is laid out about the anchor point
     */
    int PROPERTY_TEXT_ANCHOR        = 1 << 20;

    // ====================================================================
    // Constants for property values
    // ====================================================================
    
    /**
     * See the CSS 2 specification of a definition of bolder
     */
    int FONT_WEIGHT_BOLDER =  0x200;

    /**
     * See the CSS 2 specification for a definition of lighter
     */
    int FONT_WEIGHT_LIGHTER = 0x400;

    /**
     * Default inheritance settings (from right to left) (Y=Yes, N=No):
     *
     * <pre>
     * - Y font-family
     * - Y font-size
     * - Y font-style
     * - Y font-weight
     *
     * - Y text-anchor
     * </pre>
     *
     * The value is shifted by 2 bytes to the left so that it
     * can be combined with GraphicsNode.DEFAULT_INHERITANCE
     */
    int DEFAULT_INHERITANCE = 0x1F0000;

    /**
     * Number of properties in a TextNode
     */
    int NUMBER_OF_PROPERTIES = 5;
}

