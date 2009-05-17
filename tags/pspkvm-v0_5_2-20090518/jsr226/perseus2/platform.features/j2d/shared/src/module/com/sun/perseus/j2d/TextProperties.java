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
package com.sun.perseus.j2d;

/**
 * 
 * @version $Id: TextProperties.java,v 1.2 2006/04/21 06:35:09 st125089 Exp $
 */
public interface TextProperties {

    // =======================================================================
    // Value constants
    // =======================================================================

    /**
     * Refer to the CSS 2 specification for a definition of the
     * various font-styles
     */
    int FONT_STYLE_NORMAL = 0x01;

    /**
     * Refer to the CSS 2 specification for a definition of the
     * various font-styles
     */
    int FONT_STYLE_OBLIQUE = 0x02;

    /**
     * Refer to the CSS 2 specification for a definition of the
     * various font-styles
     */
    int FONT_STYLE_ITALIC = 0x04;

    /**
     * Refer to the CSS 2 specification for a definition of the
     * font-weights
     */
    int FONT_WEIGHT_100 = 0x01;

    /**
     * Refer to the CSS 2 specification for a definition of the
     * font-weights
     */
    int FONT_WEIGHT_200 = 0x02;

    /**
     * Refer to the CSS 2 specification for a definition of the
     * font-weights
     */
    int FONT_WEIGHT_300 = 0x04;

    /**
     * Refer to the CSS 2 specification for a definition of the
     * font-weights
     */
    int FONT_WEIGHT_400 = 0x08;

    /**
     * Refer to the CSS 2 specification for a definition of the
     * font-weights
     */
    int FONT_WEIGHT_NORMAL = FONT_WEIGHT_400;

    /**
     * Refer to the CSS 2 specification for a definition of the
     * font-weights
     */
    int FONT_WEIGHT_500 = 0x10;

    /**
     * Refer to the CSS 2 specification for a definition of the
     * font-weights
     */
    int FONT_WEIGHT_600 = 0x20;

    /**
     * Refer to the CSS 2 specification for a definition of the
     * font-weights
     */
    int FONT_WEIGHT_700 = 0x40;

    /**
     * Refer to the CSS 2 specification for a definition of the
     * font-weights
     */
    int FONT_WEIGHT_BOLD = FONT_WEIGHT_700;

    /**
     * Refer to the CSS 2 specification for a definition of the
     * font-weights
     */
    int FONT_WEIGHT_800 = 0x80;

    /**
     * Refer to the CSS 2 specification for a definition of the
     * font-weights
     */
    int FONT_WEIGHT_900 = 0x100;

    /**
     * Refer to the CSS 2 specification for a definition of the 
     * font-strech values
     */
    int FONT_STRETCH_NORMAL = 0x01;

    /**
     * Refer to the CSS 2 specification for a definition of the 
     * font-strech values
     */
    int FONT_STRETCH_ULTRA_CONDENSED = 0x02;

    /**
     * Refer to the CSS 2 specification for a definition of the 
     * font-strech values
     */
    int FONT_STRETCH_EXTRA_CONDENSED = 0x04;

    /**
     * Refer to the CSS 2 specification for a definition of the 
     * font-strech values
     */
    int FONT_STRETCH_CONDENSED = 0x08;

    /**
     * Refer to the CSS 2 specification for a definition of the 
     * font-strech values
     */
    int FONT_STRETCH_SEMI_CONDENSED = 0x10;

    /**
     * Refer to the CSS 2 specification for a definition of the 
     * font-strech values
     */
    int FONT_STRETCH_SEMI_EXPANDED = 0x20;

    /**
     * Refer to the CSS 2 specification for a definition of the 
     * font-strech values
     */
    int FONT_STRETCH_EXPANDED = 0x40;

    /**
     * Refer to the CSS 2 specification for a definition of the 
     * font-strech values
     */
    int FONT_STRETCH_EXTRA_EXPANDED = 0x80;

    /**
     * Refer to the CSS 2 specification for a definition of the 
     * font-strech values
     */
    int FONT_STRETCH_ULTRA_EXPANDED = 0x100;

    /**
     * Text is anchored about its starting point
     */
    int TEXT_ANCHOR_START = 0;

    /**
     * Text is anchored about its mid point
     */
    int TEXT_ANCHOR_MIDDLE = 1;

    /**
     * Text is anchored about its end point
     */
    int TEXT_ANCHOR_END = 2;

    // ====================================================================
    // Initial Property Values
    // ====================================================================

    /**
     * Default font family array
     */
    String[] INITIAL_FONT_FAMILY = {"sans-serif"};

    /**
     * Default font size value
     */
    int INITIAL_FONT_SIZE   = 10;

    /**
     * By default, the font style is normal
     */
    int INITIAL_FONT_STYLE  = FONT_STYLE_NORMAL;

    /**
     * By default, the font weight is normal
     */
    int INITIAL_FONT_WEIGHT = FONT_WEIGHT_NORMAL;

    /**
     * By default, text starts on the anchor point
     */
    int INITIAL_TEXT_ANCHOR = TEXT_ANCHOR_START;

    // =======================================================================
    // Property access
    // =======================================================================

    /**
     * @return the font family property
     */
    String[] getFontFamily();

    /**
     * @param fontFamily the new font family property value. The fontFamily
     *        describes the list of names to use during font matching.
     */
    void setFontFamily(String[] fontFamily);

    /**
     * @return the font size property, a positive number
     */
    float getFontSize();

    /**
     * @param fontSize the new font size property value. Should be a positive
     *        value.
     */
    void setFontSize(float fontSize);

    /**
     * @return one of FONT_STYLE_NORMAL, FONT_STYLE_OBLIQUE, FONT_STYLE_ITALIC.
     */
    int getFontStyle();

    /**
     * @param fontStyle the new font style property value. Should be one of
     *        FONT_STYLE_NORMAL, FONT_STYLE_ITALIC, FONT_STYLE_OBLIQUE
     */
    void setFontStyle(int fontStyle);

    /**
     * @return one of the FONT_WEIGHT_XXX values
     */
    int getFontWeight();

    /**
     * @param fontWeight the new font weight property value. Should be one 
     * of the FONT_WEIGHT_XXX values
     */
    void setFontWeight(int fontWeight);

    /**
     * @return one of TEXT_ANCHOR_START, TEXT_ANCHOR_END or TEXT_ANCHOR_MIDDLE
     */
    int getTextAnchor();

    /**
     * @param textAnchor the new text anchor property value. Should be one
     *        of TEXT_ANCHOR_START, TEXT_ANCHOR_MIDDLE or TEXT_ANCHOR_MIDDLE.
     */
    void setTextAnchor(int textAnchor);

}
