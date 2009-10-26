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

import com.sun.perseus.util.SVGConstants;

import com.sun.perseus.j2d.RenderGraphics;

import org.w3c.dom.DOMException;

/**
 * A <tt>FontFace</tt> node corresponds to an SVG
 * <tt>&lt;font-face&gt;</tt> element.
 * <br />
 * Like many other classes, <tt>FontFace</tt> extends <tt>ElementNode</tt>
 * so that we can support mixed namespaces (i.e., unexpected content
 * in our model).
 * <br />
 *
 * @version $Id: FontFace.java,v 1.4 2006/04/21 06:37:12 st125089 Exp $
 */
public class FontFace extends ElementNode {
    /**
     * Values when all the font weights are used
     */
    public static final int FONT_WEIGHT_ALL =
        TextNode.FONT_WEIGHT_100 
        | TextNode.FONT_WEIGHT_200 
        | TextNode.FONT_WEIGHT_300 
        | TextNode.FONT_WEIGHT_400 
        | TextNode.FONT_WEIGHT_500 
        | TextNode.FONT_WEIGHT_600 
        | TextNode.FONT_WEIGHT_700 
        | TextNode.FONT_WEIGHT_800 
        | TextNode.FONT_WEIGHT_900;

    /**
     * Value whena all the font styles are used
     */
    public static final int FONT_STYLE_ALL =
        TextNode.FONT_STYLE_NORMAL 
        | TextNode.FONT_STYLE_ITALIC 
        | TextNode.FONT_STYLE_OBLIQUE;

    /**
     * Default value for units per em
     */
    public static final float UNITS_PER_EM_DEFAULT = 1000;

    /**
     * Value when all the font sizes match
     */
    public static final float[] FONT_SIZE_ALL = null;

    /**
     * List of font families that constitute a match
     */
    protected String[] fontFamilies;

    /**
     * Describes the styles that constitute a match for this
     * font. This can be a combination (or'd) of the various
     * FONT_STYLE_XXX values.
     */
    protected int fontStyles = FONT_STYLE_ALL;

    /**
     * Describes the weights that constitute a match for this
     * font. This can be a combination (or'd) of the various
     * FONT_WEIGHT_XXX values.
     * The values FONT_WEIGHT_BOLDER and FONT_WEIGHT_LIGHTER
     * are not allowed.
     */
    protected int fontWeights = FONT_WEIGHT_ALL;

    /**
     * Font sizes that constitute a match for this font during 
     * font selection. A null value means that all font-sizes
     * can be matched by this font
     */
    protected float[] fontSizes;

    /**
     * Number of coordinate units on the em square.
     * Defaults to 1000.
     */
    protected float unitsPerEm = UNITS_PER_EM_DEFAULT;

    /**
     * Parent Font
     * @see #setParent
     */
    protected Font font;

    /**
     * The main scale factor needed to convert from the em square
     * grid to the text's current position coordinate system.
     * The transform built from the returned value is:
     * <pre>
     * scale 0     0
     * 0    -scale 0
     * 0     0     1
     * </pre>
     */
    protected float emSquareScale 
        = 1;

    /**
     * @return the SVGConstants.SVG_FONT_FACE_TAG value
     */

    public String getLocalName() {
        return SVGConstants.SVG_FONT_FACE_TAG;
    }

    /**
     * Used by <code>DocumentNode</code> to create a new instance from
     * a prototype <code>FontFace</code>.
     *
     * @param doc the <code>DocumentNode</code> for which a new node is
     *        should be created.
     * @return a new <code>FontFace</code> for the requested document.
     */
    public ElementNode newInstance(final DocumentNode doc) {
        return new FontFace(doc);
    }

    /**
     * @param newFontFamilies the new set of font families matched by this
     *        node. Note that the input array is used by reference after
     *        this call
     */
    public void setFontFamilies(final String[] newFontFamilies) {
        if (equal(fontFamilies, newFontFamilies)) {
            return;
        }
        modifyingNode();
        this.fontFamilies = newFontFamilies;
        modifiedNode();
    }
    
    /**
     * @return the array of font families matched by this node
     */
    public String[] getFontFamilies() {
        return fontFamilies;
    }

    /**
     * @param newFontStyles the set of font styles mateched by this node.
     */
    public void setFontStyles(final int newFontStyles) {
        if (newFontStyles == fontStyles) {
            return;
        }
        modifyingNode();
        this.fontStyles = newFontStyles;
        modifiedNode();
    }

    /**
     * @return the set of font styles matched by this node. To test
     *         if a particular font style is supported, use the '&'
     *         operator.
     */
    public int getFontStyles() {
        return fontStyles;
    }
    
    /**
     * @param newFontSizes the new set of font sizes matched by this node
     */
    public void setFontSizes(final float[] newFontSizes) {
        if (equal(newFontSizes, fontSizes)) {
            return;
        }
        modifyingNode();
        this.fontSizes = newFontSizes;
        modifiedNode();
    }

    /**
     * @return the array of font sizes matched by this node
     */
    public float[] getFontSizes() {
        return fontSizes;
    }

    /**
     * @return the set of font weights matched by this node
     */
    public int getFontWeights() {
        return fontWeights;
    }

    /**
     * @param newFontWeights the set of font weight value matched by this 
     *        <tt>FontFace</tt>
     */
    public void setFontWeights(final int newFontWeights) {
        if (newFontWeights == fontWeights) {
            return;
        }
        modifyingNode();
        this.fontWeights = newFontWeights;
        modifiedNode();
    }

    /**
     * @param newUnitsPerEm this node's new <tt>unitsPerEm</tt> property
     */
    public void setUnitsPerEm(final float newUnitsPerEm) {
        if (newUnitsPerEm == unitsPerEm) {
            return;
        }
        modifyingNode();
        this.unitsPerEm = newUnitsPerEm;
        computeEmSquareScale();
        modifiedNode();
    }

    /**
     * @return this node's <tt>unitsPerEm</tt> property
     */
    public float getUnitsPerEm() {
        return unitsPerEm;
    }

    /**
     * The constructor computes the inital emSquareScale
     *
     * @param ownerDocument this element's owner <code>DocumentNode</code>
     */
    public FontFace(final DocumentNode ownerDocument) {
        super(ownerDocument);
        computeEmSquareScale();
    }

    /**
     * @return the current scale factor from the em grid
     * to the text coordinate system. The scale is 
     * recomputed every time the unitsPerEm value changes.
     * <pre>
     * scale  0    0
     *   0  -scale 0
     *   0    0    1 
     * </pre>
     */
    public float getEmSquareScale() {
        return emSquareScale;
    }

    /**
     * Computes the scale that should be applied to glyphs to 
     * transform glyph coordinates into the text coordiate system.
     */
    protected void computeEmSquareScale() {
        // The number of units per em defines the number of 
        // units in 1x1 em square grid. Hence, the 1/unitsPerEm
        // scale factor.
        // The Y axis points up for the em square grid, which
        // is why the Y axis scale is negative.
        emSquareScale = 1f / unitsPerEm; 
    }

    /**
     * If the parent is a <tt>Font</tt> object, keep track of it in the
     * font member.
     * @param parent this node's new parent
     */
    public void setParent(final ModelNode parent) {
        super.setParent(parent);

        if (parent instanceof Font) {
            this.font = (Font) parent;
        } else {
            this.font = null;
        }
    }

    /**
     * @param s the character string
     * @param index the index of the character for which to
     *        find a <tt>Glyph</tt>
     * @return a Glyph if the FontFace can display the 
     * character at the requested index. Returns null
     * if no matching glyph can be found.
     */
    Glyph canDisplay(final char[] s, final int index) {
        return font.canDisplay(s, index);
    }

    /**
     * @return the Glyph that represents missing glyphs.
     * This should *not* be null.
     */
    Glyph getMissingGlyph() {
        return font.getMissingGlyph();
    }

    /**
     * The distance between the input <tt>refFontWeight</tt> and this
     * <tt>FontFace</tt>'s <tt>fontWeights</tt>
     * 
     * @param refFontWeight this method computes the distance between this 
     *        <tt>FontFace</tt>'s fontWeight and the input <tt>fontWeight</tt>
     * @return the distance to the input <tt>fontWeight</tt>
     */
    public int fontWeightDistance(final int refFontWeight) {
        // This FontFace matches all, this is a zero distance
        if (fontWeights == FONT_WEIGHT_ALL) {
            return 0;
        }

        // This FontFace matches the requested fontWeight,
        // this is a zero distance.
        if ((fontWeights & refFontWeight) != 0) {
            return 0;
        }

        // The requested fontWeight is not part of the ones
        // matching exactly this FontFace. Compute how far is 
        // the closes match.
        int dA = fontWeights;
        int i = 0;
        while ((dA & 0x01) == 0) {
            dA >>= 1;
            i++;
        }
        
        dA = i;

        i = 0;
        int curD = 100; // Infinity in this algorithm
        int d = 100;

        for (i = 0; i < 9; i++) {
            if (((refFontWeight >> i) & 0x01) != 0) {
                d = (dA - i) * (dA - i);
                if (d < curD) {
                    curD = d;
                }
            }
        }

        return curD;
    }

    /**
     * FontFace handles the font-family, font-style, font-weight, font-size,
     * and unitsPerEm attributes.
     *
     * @param traitName the name of the trait which the element may support.
     * @return true if this element supports the given trait in one of the
     *         trait accessor methods.
     */
    boolean supportsTrait(final String traitName) {
        if (SVGConstants.SVG_FONT_FAMILY_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_FONT_STYLE_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_FONT_WEIGHT_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_FONT_SIZE_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_UNITS_PER_EM_ATTRIBUTE == traitName) {
            return true;
        }
        
        return super.supportsTrait(traitName);
    }


    /**
     * FontFace handles the font-family, font-style, font-weight, font-size,
     * and unitsPerEm attributes.
     * Other traits are handled by the super class.
     *
     * @param name the requested trait name (e.g., "horiz-adv-x")
     * @return the trait's value, as a string.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to a String (SVG Tiny only).
     */
    public String getTraitImpl(final String name)
        throws DOMException {
        if (SVGConstants.SVG_FONT_FAMILY_ATTRIBUTE == name) {
            return toStringTraitQuote(getFontFamilies());
        } else if (SVGConstants.SVG_FONT_STYLE_ATTRIBUTE == name) {
            return fontStylesToStringTrait(getFontStyles());
        } else if (SVGConstants.SVG_FONT_WEIGHT_ATTRIBUTE == name) {
            return fontWeightsToStringTrait(getFontWeights());
        } else if (SVGConstants.SVG_FONT_SIZE_ATTRIBUTE == name) {
            if (getFontSizes() == FONT_SIZE_ALL) {
                return SVGConstants.CSS_ALL_VALUE;
            }
            return toStringTrait(getFontSizes());
        } else if (SVGConstants.SVG_UNITS_PER_EM_ATTRIBUTE == name) {
            return Float.toString(getUnitsPerEm());
        } else {
            return super.getTraitImpl(name);
        }
    }


    /**
     * FontFace handles the font-family, font-style, font-weight, font-size,
     * and unitsPerEm attributes.
     * Other traits are handled by the super class.
     *
     * @param name the trait's name (e.g., "units-per-em")
     * @param value the new trait string value (e.g., "1000")
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if the requested
     * trait's value cannot be specified as a String
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is an invalid value for the given trait or null.
     * @throws DOMException with error code NO_MODIFICATION_ALLOWED_ERR: if
     * attempt is made to change readonly trait.
     */
    public void setTraitImpl(final String name, final String value)
        throws DOMException {
        if (SVGConstants.SVG_FONT_FAMILY_ATTRIBUTE == name) {
            checkWriteLoading(name);
            setFontFamilies(parseFontFamilyTrait(name, value));
        } else if (SVGConstants.SVG_FONT_STYLE_ATTRIBUTE == name) {
            checkWriteLoading(name);
            setFontStyles(parseFontStylesTrait(name, value));
        } else if (SVGConstants.SVG_FONT_WEIGHT_ATTRIBUTE == name) {
            checkWriteLoading(name);
            setFontWeights(parseFontWeightsTrait(name, value));
        } else if (SVGConstants.SVG_FONT_SIZE_ATTRIBUTE == name) {
            checkWriteLoading(name);
            if (SVGConstants.CSS_ALL_VALUE.equals(value)) {
                setFontSizes(FONT_SIZE_ALL);
            } else {
                setFontSizes(parseFloatArrayTrait(name, value));
            }
        } else if (SVGConstants.SVG_UNITS_PER_EM_ATTRIBUTE == name) {
            checkWriteLoading(name);
            setUnitsPerEm(parseFloatTrait(name, value));
        } else {
            super.setTraitImpl(name, value);
        }
    }


    // =========================================================================

    /**
     * The FontFace.Match class is used to return a chain of 
     * sorted FontFace, according to the CSS2 Font Matching
     * algorithm.
     *
     * @see DocumentNode#resolveFontFaces
     */
    static class Match {
        /**
         * The matching <code>FontFace</code>.
         */
        public FontFace fontFace;

        /**
         * The font-weight distance. See the font matching algorithm
         * in <code>DocumentNode</code>
         */
        public int distance;

        /**
         * The next match in the chain.
         */
        public Match next;
        
        /**
         * Constructor
         *
         * @param fontFace the matching <code>FontFace</code>
         */
        public Match(final FontFace fontFace) {
            this.fontFace = fontFace;
        }
    }

    // =========================================================================

}
