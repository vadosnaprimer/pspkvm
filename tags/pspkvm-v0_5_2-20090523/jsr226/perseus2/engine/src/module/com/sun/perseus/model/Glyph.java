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
import com.sun.perseus.j2d.TextRenderingProperties;

import com.sun.perseus.util.SVGConstants;

import org.w3c.dom.DOMException;

import com.sun.perseus.j2d.Box;
import com.sun.perseus.j2d.Path;
import com.sun.perseus.j2d.PathSupport;
import com.sun.perseus.j2d.Tile;
import com.sun.perseus.j2d.Transform;

/**
 * A <tt>Glyph</tt> node corresponds to an SVG <tt>&lt;glyph&gt;</tt>
 * and <tt>&lt;missing-glyph&gt;</tt> elements. If the unicode value is null,
 * then a <tt>Glyph</tt> represents a <tt>&lt;missing-glyph&gt;</tt>
 * and its length is assumed to be 1.
 * <br />
 * 
 * @version $Id: Glyph.java,v 1.11 2006/06/29 10:47:31 ln156897 Exp $
 */
public class Glyph extends ElementNode {
    /**
     * By default, use the parent Font's advance
     */
    public static final float DEFAULT_HORIZONTAL_ADVANCE_X = -1;

    /**
     * The Path representing the outline of this glyph
     */
    protected Path d;

    /**
     * This glyph's horizontal advance.
     * A negative value means that the parent Font value should be used
     */
    protected float horizontalAdvanceX = DEFAULT_HORIZONTAL_ADVANCE_X;

    /**
     * The computed glyph's horizontal advance. If this glyphs
     * advance is set, then this is equal to the glyph's advance.
     * Otherwise, this is the grand-parent font's advance if there
     * is one.
     */
    protected float computedHorizontalAdvanceX = -1; 

    /**
     * The computed glyph origin.
     */
    protected float origin = 0;

    /**
     * The em square scale factor. 
     */
    protected float emSquareScale = 1f;

    /**
     * The unicode character, or sequence of unicode character, that 
     * this glyph represents. If null, then this Glyph can be used to
     * represent missing glyphs for arbitrary unicode values.
     */
    protected String unicode;

    /**
     * Set of glyph names. May be null.
     */
    protected String[] glyphName;

    /**
     * This glyph's local name.
     */
    protected String localName = SVGConstants.SVG_GLYPH_TAG;

    /**
     * Constructor.
     *
     * @param ownerDocument this element's owner <code>DocumentNode</code>
     */
    public Glyph(final DocumentNode ownerDocument) {
        this(ownerDocument, SVGConstants.SVG_GLYPH_TAG);
    }

    /**
     * Constructor.
     *
     * @param ownerDocument this element's owner <code>DocumentNode</code>
     * @param localName this element's local name. One of 
     *        'glyph' or 'missing-glyph'.
     */
    public Glyph(final DocumentNode ownerDocument,
                 final String localName) {
        super(ownerDocument);

        if (SVGConstants.SVG_GLYPH_TAG == localName
            ||
            SVGConstants.SVG_MISSING_GLYPH_TAG == localName) {
            this.localName = localName;
        } else {
            throw new IllegalArgumentException("Illegal Glyph name : " 
                                               + localName);
        }
    }

    /**
     * @return the SVGConstants.SVG_GLYPH_TAG value
     */

    public String getLocalName() {
        return localName;
    }

    /**
     * Used by <code>DocumentNode</code> to create a new instance from
     * a prototype <code>Glyph</code>.
     *
     * @param doc the <code>DocumentNode</code> for which a new node is
     *        should be created.
     * @return a new <code>Glyph</code> for the requested document.
     */
    public ElementNode newInstance(final DocumentNode doc) {
        return new Glyph(doc, localName);
    }

    /**
     * Sets the emSquareScale. See the Font's list method adding
     * children: when the FontFace is set, the Font sets the em square
     * transform on all its children. When a Glyph is
     * set, and if there is a FontFace already, the em square transform
     * is set as well.
     * 
     * @param newEmSquareScale the new value for this glyph's em square
     *        scale, i.e., the scale between the glyph's em square and 
     *        the text's coordinate system.
     */
    public void setEmSquareScale(final float newEmSquareScale) {
        if (newEmSquareScale == emSquareScale) {
            return;
        }
        emSquareScale = newEmSquareScale;
    }

    /**
     * Sets the horizontal advance along the x axis. This is
     * in the em square coordinate space.
     *
     * @param newHorizontalAdvanceX the new advance along the x axis
     */
    public void setHorizontalAdvanceX(final float newHorizontalAdvanceX) {
        if (newHorizontalAdvanceX == horizontalAdvanceX) {
            return;
        }

        this.horizontalAdvanceX = newHorizontalAdvanceX;
        if (newHorizontalAdvanceX == -1
            &&
            parent != null
            && parent instanceof Font) {
            computedHorizontalAdvanceX 
                = ((Font) parent).getHorizontalAdvanceX();
            origin = ((Font) parent).getHorizontalOriginX();
        } else {
            computedHorizontalAdvanceX = newHorizontalAdvanceX;
        }
    }

    /**
     * Override set parent to capture the default advance
     * 
     * @param parent this node's new parent
     */
    public void setParent(final ModelNode parent) {
        super.setParent(parent);

        if (parent != null && parent instanceof Font) {
            if (horizontalAdvanceX == -1) {
                computedHorizontalAdvanceX 
                    = ((Font) parent).getHorizontalAdvanceX();
            }
            origin = ((Font) parent).getHorizontalOriginX();
        } else {
            origin = 0;
        }
    }
            
    /**
     * @param tx the <code>Transform</code> to add node transform to.
     */
    protected void applyTransform(final Transform tx) {
        tx.mScale(emSquareScale, -emSquareScale);
        tx.mTranslate(-origin, 0);
    }

    /**
     * @param tx the inverse <code>Transform</code> between this node and its
     * parent space.
     */
    protected void applyInverseTransform(final Transform tx) {
        tx.mTranslate(origin, 0);
        tx.mScale(1 / emSquareScale, -1 / emSquareScale);
    }

    /**
     * @return the horizontal advance along the x axis for this 
     * glyph. This advance is in the em square coordinate space.
     * To get the advance in text coordinate space, use the 
     * getTextHorizontalAdvanceX method.
     * Note that if the horizontalAdvanceX of this glyph is -1,
     * this method returns the horizontal advance of the parent
     * FontFace element if one is set.
     *
     * @see #getTextHorizontalAdvanceX
     */
    public float getHorizontalAdvanceX() {
        return computedHorizontalAdvanceX;
    }
    
    /**
     * @return the advance along the x axis in the text
     * coordinate space, i.e., after applying the 
     * emSquare transform.
     */
    public float getTextHorizontalAdvanceX() {
        return computedHorizontalAdvanceX * emSquareScale;
    }

    /**
     * @return this glyph's geometry, as a <tt>Path</tt>.
     *         The returned path is in the Font's coordinate system.
     */
    public Path getD() {
        return d;
    }

    /**
     * @param newD the new <tt>Path</tt> for this <tt>Glyph</tt>
     */
    public void setD(final Path newD) {
        if (equal(newD, d)) {
            return;
        }
        this.d = newD;
    }

    /**
     * Returns this glyph's name(s)
     *
     * @return thsi glyph's name set. Null if none.
     */
    public String[] getGlyphName() {
        return glyphName;
    }

    /**
     * Sets this glyph's name(s). May be null.
     *
     * @param glyphName the new name for the glyph
     */
    public void setGlyphName(final String[] glyphName) {
        if (equal(glyphName, this.glyphName)) {
            return;
        }
        this.glyphName = glyphName;
    }

    /**
     * @param tile the Tile instance whose bounds should be appended.
     * @param trp the TextRenderingProperties describing the nodes rendering 
     *        characteristics.
     * @param t the Transform to the requested tile space, from this node's user
     *        space.
     * @return the expanded tile.
     */
    protected Tile addRenderingTile(Tile tile, 
                                    final TextRenderingProperties trp, 
                                    final Transform t) {
        if (d != null) {
            if (trp.getStroke() == null) {
                // No stroking on the shape, we can use the geometrical bounding
                // box.
                Box renderingBox = addNodeBBox(null, t);
                if (renderingBox != null) {
                    if (tile == null) {
                        tile = new Tile();
                        tile.snapBox(renderingBox);
                    } else {
                        tile.addSnapBox(renderingBox);
                    }
                }
            } else {
                // Need to account for stroking, with a more costly operation 
                // to compute the stroked bounds.
                Object strokedPath = PathSupport.getStrokedPath(d, trp);
                if (tile == null) {
                    tile = new Tile();
                    PathSupport.computeStrokedPathTile(tile, strokedPath, t);
                } else {
                    Tile st = new Tile();
                    PathSupport.computeStrokedPathTile(st, strokedPath, t);
                    tile.addTile(st);
                }
            }
        }
        return tile;
    }
    
    /**
     * @return this <tt>Glyph</tt>'s unicode value
     */
    public String getUnicode() {
        return unicode;
    }

    /**
     * @param newUnicode this <tt>Glyph</tt>'s new unicode value
     */
    public void setUnicode(final String newUnicode) {
        if (equal(newUnicode, unicode)) {
            return;
        }
        this.unicode = newUnicode;
    }

    /**
     * @return the number of characters represented by this glyph
     */
    public int getLength() {
        if (unicode == null) {
            return 1;
        }
        
        return unicode.length();
    }

    /**
     * It is the responsibility of the caller to check that the 
     * requested index is between the unicode's value index range
     * (i.e., between 0 and unicode.length())
     *
     * @param i the index of the unicode value for the given index.
     * @return the character at the requested index 
     */
    public int getCharAt(final int i) {
        return unicode.charAt(i);
    }

    /**
     * A <tt>Glyph</tt> is hit if the <tt>Path</tt> it paints is
     * hit by the given point.
     *  
     * @param pt the x/y coordinate. Should never be null and be
     *        of size two. If not, the behavior is unspecified. The 
     *        point is in the node's user space.
     * @param trp the <tt>TextRenderingProperties</tt> containing the properties
     *        applicable to the hit detection operation. This is used
     *        because the same node may be referenced multiple times
     *        by different proxies. 
     * @return true if this node is hit at the given point. false otherwise.
     */
    public boolean isHit(final float[] pt, final TextRenderingProperties trp) {
        if (d == null) {
            return false;
        }

        // If the node is filled, see if the shape is hit
        if (trp.getFill() != null) {
            if (PathSupport.isHit(d, trp.getFillRule(), pt[0], pt[1])) {
                return true;
            }
        }

        // Test detection on the edge if the stroke color
        // is set.
        if (trp.getStroke() != null) {
            Object strokedPath = PathSupport.getStrokedPath(d, trp);
            if (PathSupport.isStrokedPathHit(strokedPath, trp.getFillRule(), 
                                             pt[0], pt[1])) {
                return true;
            }
        }

        return false;
    }

    /**
     * @param bbox the bounding box to which this node's bounding box should be
     *        appended. That bounding box is in the target coordinate space. It 
     *        may be null, in which case this node should create a new one.
     * @param t the transform from the node coordinate system to the coordinate
     *        system into which the bounds should be computed.
     * @return the bounding box of this node, in the target coordinate space, 
     */
    Box addNodeBBox(final Box bbox, 
                    final Transform t) {
        return addShapeBBox(bbox, d, t);
    }

    /**
     * Supported traits: d, horiz-adv-x
     *
     * @param traitName the name of the trait which the element may support.
     * @return true if this element supports the given trait in one of the
     *         trait accessor methods.
     */
    boolean supportsTrait(final String traitName) {
        if (SVGConstants.SVG_D_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_HORIZ_ADV_X_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_GLYPH_NAME_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_UNICODE_ATTRIBUTE == traitName) {
            return true;
        }
        return super.supportsTrait(traitName);
    }

    /**
     * Supported traits: d, horiz-adv-x
     *
     * @param name the requested trait name.
     * @return the requested trait value, as a string.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to a String (SVG Tiny only).
     */
    public String getTraitImpl(String name)
        throws DOMException {
        if (SVGConstants.SVG_D_ATTRIBUTE == name) {
            if (d == null) {
                return "";
            } else {
                return d.toString();
            }
        } else if (SVGConstants.SVG_HORIZ_ADV_X_ATTRIBUTE == name) {
            return Float.toString(getHorizontalAdvanceX());
        } else if (SVGConstants.SVG_UNICODE_ATTRIBUTE == name) {
            if (unicode == null) {
                return "";
            }
            return unicode;
        } else if (SVGConstants.SVG_GLYPH_NAME_ATTRIBUTE == name) {
            return toStringTrait(getGlyphName());
        }

        return super.getTraitImpl(name);
    }

    /**
     * Supported traits: d, horiz-adv-x
     *
     * @param name the trait name.
     * @param value the trait's string value.
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
        if (SVGConstants.SVG_D_ATTRIBUTE == name) {
            checkWriteLoading(name);
            setD(parsePathTrait(name, value));
        } else if (SVGConstants.SVG_HORIZ_ADV_X_ATTRIBUTE == name) {
            checkWriteLoading(name);
            setHorizontalAdvanceX(parsePositiveFloatTrait(name, value));
        } else if (SVGConstants.SVG_UNICODE_ATTRIBUTE == name) {
            checkWriteLoading(name);
            if (value == null) {
                throw illegalTraitValue(name, value);
            }
            setUnicode(value);
        } else if (SVGConstants.SVG_GLYPH_NAME_ATTRIBUTE == name) {
            checkWriteLoading(name);
            setGlyphName(parseStringArrayTrait(name, value, 
                                               SVGConstants.COMMA_STR));
        } else {
            super.setTraitImpl(name, value);
        }
    }


    /**
     * @return a text description of the glyph including it's unicode value
     */
    public String toString() {
        return "com.sun.perseus.model.Glyph[" + unicode + "]";
    }
}
