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

import com.sun.perseus.j2d.RenderGraphics;

import com.sun.perseus.util.SVGConstants;

import java.util.Vector;

import org.w3c.dom.DOMException;

/**
 * A <code>Font</code> node models an SVG <code>&lt;code&gt;</code>
 * element. 
 *
 * @version $Id: Font.java,v 1.7 2006/06/29 10:47:31 ln156897 Exp $
 */
public class Font extends ElementNode {
    /**
     * Only the horiz-adv-x trait is required on <font>
     */
    static final String[] REQUIRED_TRAITS
        = {SVGConstants.SVG_HORIZ_ADV_X_ATTRIBUTE};

    /**
     * The X-coordinate in the font coordinate system of the origin 
     * of a glyph to be used when drawing horizontally oriented text. 
     * (Note that the origin applies to all glyphs in the font.)
     */
    protected float horizontalOriginX;

    /**
     * The default horizontal advance after rendering a glyph in 
     * horizontal orientation. Glyph widths are required to be non-negative, 
     * even if the glyph is typically rendered right-to-left, as in
     * Hebrew and Arabic scripts.
     */
    protected float horizontalAdvanceX;

    /**
     * The current fontFace describing this font, if any
     */
    protected FontFace fontFace;

    /**
     * The Font's missing glyph, used to render unknown characters
     */
    protected Glyph missingGlyph;

    /**
     * The Font's first horizontal kerning pair. May be null.
     */
    protected HKern firstHKern;

    /**
     * The Font's last horizontal kerning pair. May be null.
     */
    protected HKern lastHKern;

    /**
     * Constructor.
     *
     * @param ownerDocument this element's owner <code>DocumentNode</code>
     */
    public Font(final DocumentNode ownerDocument) {
        super(ownerDocument);
    }

    /**
     * @return the SVGConstants.SVG_FONT_TAG value
     */
    public String getLocalName() {
        return SVGConstants.SVG_FONT_TAG;
    }

    /**
     * Used by <code>DocumentNode</code> to create a new instance from
     * a prototype <code>Font</code>.
     *
     * @param doc the <code>DocumentNode</code> for which a new node is
     *        should be created.
     * @return a new <code>Font</code> for the requested document.
     */
    public ElementNode newInstance(final DocumentNode doc) {
        return new Font(doc);
    }

    /**
     * @param newHorizontalOriginX the new value for the <tt>Font</tt>'s
     *        origin along the x-axis.
     */
    public void setHorizontalOriginX(final float newHorizontalOriginX) {
        if (newHorizontalOriginX == horizontalOriginX) {
            return;
        }
        modifyingNode();
        horizontalOriginX = newHorizontalOriginX;
        modifiedNode();
    }

    /**
     * @return this <tt>Font</tt>'s origin along the x-axis
     */
    public float getHorizontalOriginX() {
        return horizontalOriginX;
    }

    /**
     * @param newHorizontalAdvanceX the new horizontal advance along the
     *        x-axis for this <tt>Font</tt>
     */
    public void setHorizontalAdvanceX(final float newHorizontalAdvanceX) {
        if (newHorizontalAdvanceX == horizontalAdvanceX) {
            return;
        }
        modifyingNode();
        horizontalAdvanceX = newHorizontalAdvanceX;
        modifiedNode();
    }

    /**
     * @return this <tt>Font</tt>'s horizontal adavance along the x-axis
     */
    public float getHorizontalAdvanceX() {
        return horizontalAdvanceX;
    }

    /**
     * Returns a Glyph if the FontFace can display the 
     * character at the requested index. Returns null
     * if no matching glyph can be found.
     *
     * @param s the character sequence to display
     * @param index the index of the first character in <tt>s</tt> to
     *        display.
     * @return the <tt>Glyph</tt> used to represent the input 
     *         character at <tt>index</tt> in <tt>s</tt>
     */
    public Glyph canDisplay(final char[] s, final int index) {
        int max = s.length - index;
        int glLength = 0;
        int j = 0;

        Glyph gl = null;
        ModelNode c = firstChild;
        while (c != null) {
            if (c instanceof Glyph) {
                gl = (Glyph) c;
                glLength = gl.getLength();
                if (gl.getUnicode() != null 
                    && glLength > 0 
                    && index + glLength <= s.length) {
                    for (j = 0; j < glLength; j++) {
                        if (s[index + j] != gl.getCharAt(j)) {
                            break;
                        }
                    }
                    if (j == glLength) {
                        return gl;
                    }
                }
            }
            c = c.nextSibling;
        }

        // No matching glyph
        return null;
    }

    /**
     * @return the Glyph that represents missing glyphs.
     *         This should *not* be null.
     */
    public Glyph getMissingGlyph() {
        return missingGlyph;
    }

    // ====================================================================
    // List Implementation overrides to track special children
    // ====================================================================
    
    /**
     * @param node <tt>ElementNode</tt> to add to this <tt>ElementNode</tt>
     */
    public void add(final ElementNode node) {
        super.add(node);
        addSpecial(node);
    }
    
    /**
     * Called in case special handling of children is required.
     *
     * @param node the <tt>ModelNode</tt> added to this list
     *
     * @see #addFontFace
     * @see #addGlyph
     */
    protected void addSpecial(final ModelNode node) {
        if (!(node instanceof Glyph)) {
            if (!(node instanceof FontFace)) {
                if (!(node instanceof HKern)) {
                    return;
                } else {
                    addHKern((HKern) node);
                }
            } else {
                addFontFace((FontFace) node);
            }
        } else {
            Glyph gl = (Glyph) node;
            if (gl.getUnicode() == null) {
                addMissingGlyph(gl);
            } else {
                addGlyph(gl);
            }
        }
    }

    /**
     * @param newFontFace the <tt>FontFace</tt> child to add
     */
    protected void addFontFace(final FontFace newFontFace) {
        if (fontFace != null) {
            // If there is already a FontFace child, we ignore the second
            // one. Only the first child is accounted for.
            return;
        }

        // A Font can only have a single font-face child. If there is alre
        fontFace = newFontFace;
        updateGlyphEmSquare();
    }

    /**
     * Simply chain the kerning pairs so that they can be 
     * looked up easily later.
     *
     * @param hkern the new <code>HKern</code> entry to add
     *        to this node.
     */
    protected void addHKern(final HKern hkern) {
        if (firstHKern != null) {
            lastHKern.nextHKern = hkern;
            lastHKern = hkern;
        } else {
            firstHKern = hkern;
            lastHKern = hkern;
            hkern.nextHKern = null;
        }
    }

    /**
     * Used by the Text class: looks up kerning pairs to check if 
     * there is any horizontal kerning for the input pair.
     *
     * @param g1 this first glyph is assumed to be part of this
     *        Font.
     * @param g2 this second glyph may come from a different font, in
     *        which case this method returns 0.
     * @return the kerning adjustment for the given pair of <code>Glyph</code>s.
     */
    float getHKern(final Glyph g1, final Glyph g2) {
        if (g2.parent != this) {
            return 0;
        }

        HKern k = firstHKern;
        while (k != null) {
            if (k.matchesFirst(g1)) {
                if (k.matchesSecond(g2)) {
                    break;
                }
            }
            k = k.nextHKern;
        }

        if (k != null) {
            if (fontFace != null) {
                return k.k * fontFace.getEmSquareScale();
            } else {
                return k.k;
            }
        } else {
            return 0;
        }
    }

    /**
     * Update the em square on all the <tt>Font</tt>'s glyphs
     */
    protected void updateGlyphEmSquare() {
        float emSquareScale = 1;
        if (fontFace != null) {
            emSquareScale = fontFace.getEmSquareScale();
        }

        ElementNode c = firstChild;
        while (c != null) {
            if (c instanceof Glyph) {
                ((Glyph) c).setEmSquareScale(emSquareScale);
            }
            c = (ElementNode) c.nextSibling;
        }
    }
    
    /**
     * @param gl set the input <tt>Glyph</tt>'s em square.
     */
    protected void updateGlyphEmSquare(final Glyph gl) {
        if (fontFace != null) {
            gl.setEmSquareScale(fontFace.getEmSquareScale());
        } else {
            gl.setEmSquareScale(1);
        }
    }

    /**
     * If there is already a missing glyph, any new call
     * to <tt>addMissingGlyph</tt> has no effect on the 
     * missing glyph. 
     *
     * @param gl <tt>Glyph</tt> to add as a missing glyph.
     */
    protected void addMissingGlyph(final Glyph gl) {
        // Only use gl as a missing glyph if it is the 
        // first missing glyph
        if (missingGlyph == null) {
            missingGlyph = gl;
        } 
        updateGlyphEmSquare(gl);
    }

    /**
     * @param gl <tt>Glyph</tt> to add to this font
     */
    protected void addGlyph(final Glyph gl) {
        updateGlyphEmSquare(gl);
    }

    /**
     * Font handlers the horiz-adv-x and horiz-origin-x traits.
     *
     * @param traitName the name of the trait which the element may support.
     * @return true if this element supports the given trait in one of the
     *         trait accessor methods.
     */
    boolean supportsTrait(final String traitName) {
        if (SVGConstants.SVG_HORIZ_ORIGIN_X_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_HORIZ_ADV_X_ATTRIBUTE == traitName) {
            return true;
        }

        return super.supportsTrait(traitName);
    }

    /**
     * @return an array of traits that are required by this element.
     */
    public String[] getRequiredTraits() {
        return REQUIRED_TRAITS;
    }

    /**
     * Font handles the horiz-adv-x and horiz-origin-x traits.
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
        if (SVGConstants.SVG_HORIZ_ADV_X_ATTRIBUTE == name) {
            return Float.toString(getHorizontalAdvanceX());
        } else if (SVGConstants.SVG_HORIZ_ORIGIN_X_ATTRIBUTE == name) {
            return Float.toString(getHorizontalOriginX());
        } else {
            return super.getTraitImpl(name);
        }
    }

    /**
     * Font handles the horiz-adv-x and horiz-origin-x traits.
     * Other traits are handled by the super class.
     *
     * @param name the requested trait name (e.g., "horiz-adv-x")
     * @return the trait's value, as a float.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to a String (SVG Tiny only).
     */
    float getFloatTraitImpl(final String name)
        throws DOMException {
        if (SVGConstants.SVG_HORIZ_ADV_X_ATTRIBUTE == name) {
            return getHorizontalAdvanceX();
        } else if (SVGConstants.SVG_HORIZ_ORIGIN_X_ATTRIBUTE == name) {
            return getHorizontalOriginX();
        } else {
            return super.getFloatTraitImpl(name);
        }
    }

    /**
     * Font handles the horiz-adv-x and horiz-origin-x traits.
     * Other traits are handled by the super class.
     *
     * @param name the trait's name (e.g., "horiz-adv-x")
     * @param value the new trait string value (e.g., "10")
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
        if (SVGConstants.SVG_HORIZ_ADV_X_ATTRIBUTE == name) {
            checkWriteLoading(name);
            setHorizontalAdvanceX(parseFloatTrait(name, value));
        } else if (SVGConstants.SVG_HORIZ_ORIGIN_X_ATTRIBUTE == name) {
            checkWriteLoading(name);
            setHorizontalOriginX(parseFloatTrait(name, value));
        } else {
            super.setTraitImpl(name, value);
        }
    }

    /**
     * Font handles the horiz-adv-x and horiz-origin-x traits.
     * Other traits are handled by the super class.
     *
     * @param name the trait's name (e.g., "horiz-adv-x")
     * @param value the new trait float value (e.g., 10f)
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
    public void setFloatTraitImpl(final String name, final float value)
        throws DOMException {
        if (SVGConstants.SVG_HORIZ_ADV_X_ATTRIBUTE == name) {
            setHorizontalAdvanceX(value);
        } else if (SVGConstants.SVG_HORIZ_ORIGIN_X_ATTRIBUTE == name) {
            setHorizontalOriginX(value);
        } else {
            super.setFloatTraitImpl(name, value);
        }
    }

}
