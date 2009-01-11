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
 * A <code>HKern</code> corresponds to an SVG <code>&lt;hkern&gt;</code>
 * element. <br />
 *
 * @version $Id: HKern.java,v 1.5 2006/06/29 10:47:32 ln156897 Exp $
 */
public class HKern extends ElementNode {
    /**
     * Only the k trait is required on <hkern>
     */
    static final String[] REQUIRED_TRAITS
        = {SVGConstants.SVG_K_ATTRIBUTE};

    /**
     * Unicode ranges
     */
    protected int[][] u1, u2;

    /**
     * Name matches
     */
    protected String[] g1, g2;

    /**
     * Adjusted horizontal advance
     */
    protected float k;

    /**
     * For use by <code>Font</code>
     */
    protected HKern nextHKern;

    /**
     * Constructor.
     *
     * @param ownerDocument this element's owner <code>DocumentNode</code>
     */
    public HKern(final DocumentNode ownerDocument) {
        super(ownerDocument);
    }

    /**
     * @return the SVGConstants.SVG_HKERN_TAG value
     */

    public String getLocalName() {
        return SVGConstants.SVG_HKERN_TAG;
    }

    /**
     * Used by <code>DocumentNode</code> to create a new instance from
     * a prototype <code>HKern</code>.
     *
     * @param doc the <code>DocumentNode</code> for which a new node is
     *        should be created.
     * @return a new <code>HKern</code> for the requested document.
     */
    public ElementNode newInstance(final DocumentNode doc) {
        return new HKern(doc);
    }

    /**
     * Sets this kerning entry's first match
     *
     * @param u1 the new matching unicode ranges.
     *
     * @throws IllegalArgumentException if the input array has an entry which 
     *         is not of length 2.
     */
    public void setU1(final int[][] u1) {
        if (u1 != null) {
            for (int i = 0; i < u1.length; i++) {
                if (u1[i] == null || u1[i].length != 2) {
                    throw new IllegalArgumentException();
                }
            }
        }

        if (equal(u1, this.u1)) {
            return;
        }
        modifyingNode();
        this.u1 = u1;
        modifiedNode();
    }

    /**
     * Sets this kerning entry's second match
     *
     * @param u2 the new matching unicode ranges.
     *
     * @throws IllegalArgumentException if the input array has an entry which 
     *         is not of length 2.
     */
    public void setU2(final int[][] u2) {
        if (u2 != null) {
            for (int i = 0; i < u2.length; i++) {
                if (u2[i] == null || u2[i].length != 2) {
                    throw new IllegalArgumentException();
                }
            }
        }

        if (equal(u2, this.u2)) {
            return;
        }
        modifyingNode();
        this.u2 = u2;
        modifiedNode();
    }

    /**
     * Sets this kerning entry first glyph names matches.
     *
     * @param g1 the list of matching names.
     */
    public void setG1(final String[] g1) {
        if (equal(g1, this.g1)) {
            return;
        }
        modifyingNode();
        this.g1 = g1;
        modifiedNode();
    }

    /**
     * Sets this kerning entry second glyph names matches.
     *
     * @param g2 the list of matching names.
     */
    public void setG2(final String[] g2) {
        if (equal(g2, this.g2)) {
            return;
        }
        modifyingNode();
        this.g2 = g2;
        modifiedNode();
    }
    
    /**
     * Sets this kernixng entry's advance.
     *
     * @param k the new kerning advance
     */
    public void setK(final float k) {
        if (this.k == k) {
            return;
        }
        modifyingNode();
        this.k = k;
        modifiedNode();
    }

    /**
     * @return this kerning entry's advance.
     */
    public float getK() {
        return k;
    }

    /**
     * @return this node's matching unicode ranges for the first glyph
     */
    public int[][] getU1() {
        return u1;
    }

    /**
     * @return this node's matching unicode ranges for the second glyph
     */
    public int[][] getU2() {
        return u2;
    }

    /**
     * @return this node's matching glyphs names for the first glyph
     */
    public String[] getG1() {
        return g1;
    }

    /**
     * @return this node's matching glyph names for the second glyph
     */
    public String[] getG2() {
        return g2;
    }

    /**
     * Checks if the input glyph is a match for u1 or g1.
     *
     * @param g the glyph to match.
     * @return true if the unicode range or glyph name of the input
     *         <code>Glyph</code> matches the kerning's u1 or g1 
     *         setting.
     */
    public boolean matchesFirst(final Glyph g) {
        return matches(g, u1, g1);
    }

    /**
     * Checks if the input glyph is a match for u2 or g2.
     *
     * @param g the glyph to match.
     * @return true if the unicode range or glyph name of the input
     *         <code>Glyph</code> matches the kerning's u2 or g2
     *         setting.
     */
    public boolean matchesSecond(final Glyph g) {
        return matches(g, u2, g2);
    }

    /**
     * Checks if the input glyph is a match for the input
     * set of unicode ranges and/or glyph names.
     *
     * @param glyph the glyph to match.
     * @param u the set of unicode ranges
     * @param g the set of glyph names.
     * @return true if there is a match.
     */
    boolean matches(final Glyph glyph, final int[][] u, final String[] g) {
        String gu = glyph.getUnicode();
        String[] gg = glyph.getGlyphName();

        if (u != null && gu != null && gu.length() == 1) {
            char c = gu.charAt(0);
            for (int i = 0; i < u.length; i++) {
                if (c >= u[i][0] && c <= u[i][1]) {
                    return true;
                }
            }
        }

        if (gg != null && g != null) {
            for (int i = 0; i < gg.length; i++) {
                String gn = gg[i];
                for (int j = 0; j < g.length; j++) {
                    if (g[j].equals(gn)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * HKern handles the u1, u2, g1, g2 and k traits.
     *
     * @param traitName the name of the trait which the element may support.
     * @return true if this element supports the given trait in one of the
     *         trait accessor methods.
     */
    boolean supportsTrait(final String traitName) {
        if (SVGConstants.SVG_U1_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_U2_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_G1_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_G2_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_K_ATTRIBUTE == traitName) {
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
     * HKern handles the u1, u2, g1, g2 and k traits.
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
        if (SVGConstants.SVG_U1_ATTRIBUTE == name) {
            return unicodeRangeToStringTrait(u1);
        } else if (SVGConstants.SVG_U2_ATTRIBUTE == name) {
            return unicodeRangeToStringTrait(u2);
        } else if (SVGConstants.SVG_G1_ATTRIBUTE == name) {
            return toStringTrait(g1);
        } else if (SVGConstants.SVG_G2_ATTRIBUTE == name) {
            return toStringTrait(g2);
        } else if (SVGConstants.SVG_K_ATTRIBUTE == name) {
            return Float.toString(k);
        } else {
            return super.getTraitImpl(name);
        }
    }

    /**
     * HKern handles the u1, u2, g1, g2 and k traits.
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
        if (SVGConstants.SVG_U1_ATTRIBUTE == name) {
            checkWriteLoading(name);
            setU1(parseUnicodeRangeTrait(name, value));
        } else if (SVGConstants.SVG_U2_ATTRIBUTE == name) {
            checkWriteLoading(name);
            setU2(parseUnicodeRangeTrait(name, value));
        } else if (SVGConstants.SVG_G1_ATTRIBUTE == name) {
            checkWriteLoading(name);
            setG1(parseStringArrayTrait(name, value, SVGConstants.COMMA_STR));
        } else if (SVGConstants.SVG_G2_ATTRIBUTE == name) {
            checkWriteLoading(name);
            setG2(parseStringArrayTrait(name, value, SVGConstants.COMMA_STR));
        } else if (SVGConstants.SVG_K_ATTRIBUTE == name) {
            checkWriteLoading(name);
            setK(parseFloatTrait(name, value));
        } else {
            super.setTraitImpl(name, value);
        }
    }

}
