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

import com.sun.perseus.j2d.Box;
import com.sun.perseus.j2d.RenderGraphics;
import com.sun.perseus.j2d.TextProperties;
import com.sun.perseus.j2d.TextRenderingProperties;
import com.sun.perseus.util.SVGConstants;

import org.w3c.dom.DOMException;
import org.w3c.dom.svg.SVGRect;

import com.sun.perseus.j2d.Transform;

/**
 * <code>StructureNode</code> is the base class for all nodes that
 * represent structure content, such as images, defs or svg elements.
 *
 * @version $Id: StructureNode.java,v 1.8 2006/06/29 10:47:35 ln156897 Exp $
 */
public abstract class StructureNode extends CompositeGraphicsNode 
    implements TextNode, TextRenderingProperties {
    /**
     * Marker used to mark that the fontWeight property was set to bolder.
     */
    public static final int FONT_WEIGHT_BOLDER_MARKER = 1 << 23;
    
    /**
     * Marker used to mark that the fontWeight property was set to lighter.
     */
    public static final int FONT_WEIGHT_LIGHTER_MARKER = 1 << 24;
    
    /**
     * When align is set to ALIGN_NONE, the SVG content
     * is rescaled so that the widht and height of the 
     * viewBox map to the viewport width and height and 
     * the origin of the viewBox maps to the viewport 
     * origin.
     */
    public static final int ALIGN_NONE = 0;

    /**
     * When align is set to ALIGN_XMIDYMID, the SVG 
     * content is rescaled so that the aspect ratio
     * of the viewBox is preserved. With that constraint,
     * either the width or height of the viewBox is
     * mapped to the width or height of the viewPort.
     * The rescaled content is centered into the viewport.
     */
    public static final int ALIGN_XMIDYMID = 1;

    // ====================================================================
    // Properties
    // ====================================================================
    /**
     * Controls this node's fontFamily
     */
    protected String[] fontFamily = INITIAL_FONT_FAMILY;

    /**
     * Controls this node's fontSize
     */
    protected float fontSize = INITIAL_FONT_SIZE;

    /**
     * Constructor.
     *
     * @param ownerDocument this element's owner <code>DocumentNode</code>
     */
    public StructureNode(final DocumentNode ownerDocument) {
        super(ownerDocument);
    }

    /**
     * @param bbox the bounding box to which this node's bounding box should be
     *        appended. That bounding box is in the target coordinate space. It 
     *        may be null, in which case this node should create a new one.
     * @param t the transform to apply from the node's coordinate space to the
     *        target coordinate space. May be null for the identity 
     *        transform.
     * @return the node's bounding box in the target coordinate space.
     */
    Box addBBox(Box bbox, final Transform t) {
        ModelNode c = getFirstChildNode();
        while (c != null) {
            if (c.contributeBBox()) {
                bbox = c.addBBox(bbox, c.appendTransform(t, null));
            }
            c = c.nextSibling;
        }

        return bbox;
    }

    /**
     * @return the tight bounding box in current user coordinate
     * space. 
     */
    public SVGRect getBBox() {
        return addBBox(null, null);
    }

    /**
     * @return the tight bounding box in screen coordinate space.
     */
    public SVGRect getScreenBBox() {
        // There is no screen bounding box if the element is not hooked
        // into the main tree.
        if (!inDocumentTree()) {
            return null;
        }

        return addBBox(null, txf);
    }

    /**
     * Returns the <code>ModelNode</code>, if any, hit by the
     * point at coordinate x/y. 
     * 
     * @param pt the x/y coordinate. Should never be null and be
     *        of size two. If not, the behavior is unspecified.
     *        The coordinates are in viewport space.
     * @return the <tt>ModelNode</tt> hit at the given point or null
     *         if none was hit.
     */
    public ModelNode nodeHitAt(final float[] pt) {
        // If a node does not render, it is never hit
        if (canRenderState != 0) {
            return null;
        }
        
        // Check for a hit on children
        return nodeHitAt(getLastChildNode(), pt);
    }

    /**
     * Returns the <code>ModelNode</code>, if any, hit by the
     * point at coordinate x/y in the proxy tree starting at 
     * proxy.
     * 
     * @param pt the x/y coordinate. Should never be null and be
     *        of size two. If not, the behavior is unspecified.
     *        The coordinates are in viewport space.
     * @param proxy the root of the proxy tree to test.
     * @return the <tt>ModelNode</tt> hit at the given point or null
     *         if none was hit.
     */
    ModelNode proxyNodeHitAt(final float[] pt,
                             final ElementNodeProxy proxy) {
        // If a node does not render, it is never hit
        if (canRenderState != 0) {
            return null;
        }
        
        // Check for a hit on expanded children
        return nodeHitAt(proxy.getLastExpandedChild(), pt);
    }

    /**
     * @return the number of properties supported by this node
     */
    public int getNumberOfProperties() {
        return GraphicsNode.NUMBER_OF_PROPERTIES 
            + TextNode.NUMBER_OF_PROPERTIES;
    }

    /**
     * Recomputes all inherited properties.
     */
    void recomputeInheritedProperties() {
        super.recomputeInheritedProperties();
        ModelNode p = ownerDocument;
        if (parent != null) {
            p = parent;
        }
        recomputePropertyState(PROPERTY_FONT_FAMILY, 
                               p.getPropertyState(PROPERTY_FONT_FAMILY));
        recomputeFloatPropertyState(PROPERTY_FONT_SIZE, 
                               p.getFloatPropertyState(PROPERTY_FONT_SIZE));
        recomputePackedPropertyState(PROPERTY_FONT_STYLE, 
                               p.getPackedPropertyState(PROPERTY_FONT_STYLE));
        recomputePackedPropertyState(PROPERTY_FONT_WEIGHT,
                               p.getPackedPropertyState(PROPERTY_FONT_WEIGHT));
        recomputePackedPropertyState(PROPERTY_TEXT_ANCHOR,
                               p.getPackedPropertyState(PROPERTY_TEXT_ANCHOR));
    }

    /**
     * Recomputes the given packed property's state given the new parent 
     * property.
     *
     * @param propertyIndex index for the property whose value is changing.
     * @param parentPropertyValue the value that children of this node should 
     *        now inherit.
     * 
     */
    protected void recomputePackedPropertyState(final int propertyIndex,
                                                final int parentPropertyValue) {
        if (propertyIndex != PROPERTY_FONT_WEIGHT) {
            super.recomputePackedPropertyState(propertyIndex, 
                                               parentPropertyValue);
        } else {
            // We do not need to recompute the fontWeight value if:
            // - the fontWeight is _not_ inherited & not relative (i.e. lighter
            //   or bolder)
            // or
            // - the property is inherited but the new parent property computed
            //   value is the same as the current value & the bolder and lighter
            //   markes are in the same state
            boolean isBolder = isMarkerSet(FONT_WEIGHT_BOLDER_MARKER);
            boolean isLighter = isMarkerSet(FONT_WEIGHT_LIGHTER_MARKER);
            if ((!isInherited(propertyIndex) 
                 && 
                 !isBolder 
                 &&
                 !isLighter)
                 ||
                 isPackedPropertyState(propertyIndex, parentPropertyValue)) {
                return;
            }

            int packedFontWeight = parentPropertyValue;
            if (isBolder) {
                packedFontWeight = computeFontWeight(parentPropertyValue, 
                                                     FONT_WEIGHT_BOLDER);
            } else if (isLighter) {
                packedFontWeight = computeFontWeight(parentPropertyValue, 
                                                     FONT_WEIGHT_LIGHTER);
            } 
            
            setPackedPropertyState(propertyIndex, packedFontWeight);
            
            propagatePackedPropertyState(propertyIndex, parentPropertyValue);
        }
    }
    
    /**
     * Returns the value for the given property.
     *
     * @return the value for the given property.
     */
    protected Object getPropertyState(final int propertyIndex) {
        switch (propertyIndex) {
        case PROPERTY_FONT_FAMILY:
            return fontFamily;
        default:
            return super.getPropertyState(propertyIndex);
        }
    }

    /**
     * Returns the value for the given float property.
     *
     * @return the value for the given property.
     */
    protected float getFloatPropertyState(final int propertyIndex) {
        switch (propertyIndex) {
        case PROPERTY_FONT_SIZE:
            return fontSize;
        default:
            return super.getFloatPropertyState(propertyIndex);
        }
    }

    /**
     * Returns the value for the given packed property.
     *
     * @return the value for the given packed property.
     */
    protected int getPackedPropertyState(final int propertyIndex) {
        switch (propertyIndex) {
        case TextNode.PROPERTY_FONT_STYLE:
            return pack & StructureNode.FONT_STYLE_MASK;
        case TextNode.PROPERTY_FONT_WEIGHT:
            return pack & StructureNode.FONT_WEIGHT_MASK;
        case TextNode.PROPERTY_TEXT_ANCHOR:
            return pack & StructureNode.TEXT_ANCHOR_MASK;
        default:
            return super.getPackedPropertyState(propertyIndex);
        }
    }

    /**
     * Sets the computed value for the given property.
     *
     * @param propertyIndex the property index
     * @param propertyValue the computed value for the property.
     */
    protected void setPropertyState(final int propertyIndex,
                                    final Object propertyValue) {
        switch (propertyIndex) {
        case PROPERTY_FONT_FAMILY:
            setComputedFontFamily((String[]) propertyValue);
            break;
        default:
            super.setPropertyState(propertyIndex, propertyValue);
        }
    }
    
    /**
     * Sets the computed value for the given float property.
     *
     * @param propertyIndex the property index
     * @param propertyValue the computed value for the property.
     */
    protected void setFloatPropertyState(final int propertyIndex,
                                    final float propertyValue) {
        switch (propertyIndex) {
        case PROPERTY_FONT_SIZE:
            setComputedFontSize(propertyValue);
            break;
        default:
            super.setFloatPropertyState(propertyIndex, propertyValue);
        }
    }
    
    /**
     * Sets the computed value for the given packed property.
     *
     * @param propertyIndex the property index
     * @param propertyValue the computed value for the property.
     */
    protected void setPackedPropertyState(final int propertyIndex,
                                    final int propertyValue) {
        switch (propertyIndex) {
        case PROPERTY_FONT_STYLE:
            switch (propertyValue) {
                case FONT_STYLE_NORMAL_IMPL:
                    setComputedFontStyle(FONT_STYLE_NORMAL);
                    break;
                case FONT_STYLE_ITALIC_IMPL:
                    setComputedFontStyle(FONT_STYLE_ITALIC);
                    break;
                case FONT_STYLE_OBLIQUE_IMPL:
                default:
                    setComputedFontStyle(FONT_STYLE_OBLIQUE);
                    break;
            }
            break;
        case PROPERTY_FONT_WEIGHT:
            setComputedFontWeight(propertyValue);
            break;
        case PROPERTY_TEXT_ANCHOR:
            switch (propertyValue) {
                case TEXT_ANCHOR_START_IMPL:
                    setComputedTextAnchor(TEXT_ANCHOR_START);
                    break;
                case TEXT_ANCHOR_MIDDLE_IMPL:
                    setComputedTextAnchor(TEXT_ANCHOR_MIDDLE);
                    break;
                case TEXT_ANCHOR_END_IMPL:
                default:
                    setComputedTextAnchor(TEXT_ANCHOR_END);
                    break;
            }
            break;
        default:
            super.setPackedPropertyState(propertyIndex, propertyValue);
        }
    }
    
    /**
     * Checks the state of the property value.
     *
     * @param propertyIndex the property index
     * @param propertyValue the computed value for the property.
     */
    protected boolean isPropertyState(final int propertyIndex,
                                      final Object propertyValue) {
        switch (propertyIndex) {
        case TextNode.PROPERTY_FONT_FAMILY:
            return fontFamily == propertyValue;
        default:
            return super.isPropertyState(propertyIndex, propertyValue);
        }
    }

    /**
     * Checks the state of the float property value.
     *
     * @param propertyIndex the property index
     * @param propertyValue the computed value for the property.
     */
    protected boolean isFloatPropertyState(final int propertyIndex,
                                      final float propertyValue) {
        switch (propertyIndex) {
        case TextNode.PROPERTY_FONT_SIZE:
            return fontSize == propertyValue;
        default:
            return super.isFloatPropertyState(propertyIndex, propertyValue);
        }
    }

    /**
     * Checks the state of the property value.
     *
     * @param propertyIndex the property index
     * @param propertyValue the computed value for the property.
     */
    protected boolean isPackedPropertyState(final int propertyIndex,
                                            final int propertyValue) {
        switch (propertyIndex) {
        case TextNode.PROPERTY_FONT_STYLE:
            return (propertyValue
                    ==
                    (pack & StructureNode.FONT_STYLE_MASK));
        case TextNode.PROPERTY_FONT_WEIGHT:
            return (propertyValue
                    ==
                    (pack & StructureNode.FONT_WEIGHT_MASK));
        case TextNode.PROPERTY_TEXT_ANCHOR:
            return (propertyValue
                    ==
                    (pack & StructureNode.TEXT_ANCHOR_MASK));
        default:
            return super.isPackedPropertyState(propertyIndex, propertyValue);
        }
    }
                
    // ====================================================================
    // TextNodeProperties implementation
    // ====================================================================

    /**
     * @return this node's computed font-family
     */
    public String[] getFontFamily() {
        return fontFamily;
    }

    /**
     * @param newFontFamily The fontFamily is used when the property is 
     *        not inherited
     */
    public void setFontFamily(final String[] newFontFamily) {
        if (!isInherited(PROPERTY_FONT_FAMILY) 
                && equal(newFontFamily, fontFamily)) {
            return;
        } 
        modifyingNode();
        setInheritedQuiet(PROPERTY_FONT_FAMILY, false);
        setComputedFontFamily(newFontFamily);
        propagatePropertyState(PROPERTY_FONT_FAMILY, newFontFamily);
        modifiedNode();
    }

    /**
     * @param newFontFamily the new computed font-family property value.
     */
    void setComputedFontFamily(final String[] newFontFamily) {
        this.fontFamily = newFontFamily;
    }

    /**
     * @return this node's computed fontSize
     */
    public float getFontSize() {
        return fontSize;
    }
    
    /**
     * @param newFontSize the fontSize is used when the property is 
     *        not inherited. This should not be less than zero.
     */
    public void setFontSize(final float newFontSize) {
        if (newFontSize < 0) {
            throw new IllegalArgumentException();
        }

        if (!isInherited(PROPERTY_FONT_SIZE) && newFontSize == fontSize) {
            return;
        }

        modifyingNode();
        setInheritedQuiet(PROPERTY_FONT_SIZE, false);
        setComputedFontSize(newFontSize);
        propagateFloatPropertyState(PROPERTY_FONT_SIZE, newFontSize);
        modifiedNode();
    }

    /**
     * @param newFontSize the new computed font-size property value.
     */
    void setComputedFontSize(final float newFontSize) {
        this.fontSize = newFontSize;
    }

    /**
     * @return this node's computed fontStyle
     */
    public int getFontStyle() {
        switch (pack & FONT_STYLE_MASK) {
        case FONT_STYLE_NORMAL_IMPL:
            return FONT_STYLE_NORMAL;
        case FONT_STYLE_ITALIC_IMPL:
            return FONT_STYLE_ITALIC;
        default:
            return FONT_STYLE_OBLIQUE;
        }
    }

    /**
     * @param newFontStyle The fontStyle is used when the property is 
     *        not inherited
     */
    public void setFontStyle(final int newFontStyle) {
        if (!isInherited(PROPERTY_FONT_STYLE) 
            && newFontStyle == getFontStyle()) {
            return;
        }

        modifyingNode();
        setInheritedQuiet(PROPERTY_FONT_STYLE, false);
        setComputedFontStyle(newFontStyle);
        propagatePackedPropertyState(PROPERTY_FONT_STYLE, 
                                     pack & FONT_STYLE_MASK);
        modifiedNode();
    }

    /**
     * @param newFontStyle the new computed font-style property.
     */
    void setComputedFontStyle(final int newFontStyle) {
        pack &= ~FONT_STYLE_MASK;
        switch (newFontStyle) {
        case FONT_STYLE_NORMAL:
            pack |= FONT_STYLE_NORMAL_IMPL;
            break;
        case FONT_STYLE_ITALIC:
            pack |= FONT_STYLE_ITALIC_IMPL;
            break;
        default:
            pack |= FONT_STYLE_OBLIQUE_IMPL;
            break;
        }
    }

    /**
     * @return this node's computed fontWeight
     */
    public int getFontWeight() {
        switch (pack & FONT_WEIGHT_MASK) {
        case FONT_WEIGHT_100_IMPL:
            return FONT_WEIGHT_100;
        case FONT_WEIGHT_200_IMPL:
            return FONT_WEIGHT_200;
        case FONT_WEIGHT_300_IMPL:
            return FONT_WEIGHT_300;
        case FONT_WEIGHT_400_IMPL:
            return FONT_WEIGHT_400;
        case FONT_WEIGHT_500_IMPL:
            return FONT_WEIGHT_500;
        case FONT_WEIGHT_600_IMPL:
            return FONT_WEIGHT_600;
        case FONT_WEIGHT_700_IMPL:
            return FONT_WEIGHT_700;
        case FONT_WEIGHT_800_IMPL:
            return FONT_WEIGHT_800;
        case FONT_WEIGHT_900_IMPL:
            return FONT_WEIGHT_900;
        case FONT_WEIGHT_LIGHTER_IMPL:
            return FONT_WEIGHT_LIGHTER;
        default:
            return FONT_WEIGHT_BOLDER;
        }
    }

    /**
     * @param newFontWeight The fontWeight is used when the property is 
     *        not inherited
     */
    public void setFontWeight(final int newFontWeight) {
        if (!isInherited(PROPERTY_FONT_WEIGHT) 
            && newFontWeight == getFontWeight()) {
            return;
        }

        modifyingNode();
        setInheritedQuiet(PROPERTY_FONT_WEIGHT, false);

        // Font weight is special: we need to account for relative values
        // bolder and lighter.
        int packedParentFontWeight 
                = getInheritedPackedPropertyState(PROPERTY_FONT_WEIGHT);
        setComputedFontWeight
            (computeFontWeight(packedParentFontWeight, newFontWeight));
        propagatePackedPropertyState
            (PROPERTY_FONT_WEIGHT, pack & FONT_WEIGHT_MASK);
        modifiedNode();
    }

    /**
     * @param newFontWeight new computed value for the font-weight property.
     */
    void setComputedFontWeight(final int newFontWeight) {
        pack &= ~FONT_WEIGHT_MASK;
        pack |= newFontWeight;
    }

    /**
     * @return this node's computed textAnchor
     */
    public int getTextAnchor() {
        switch (pack & TEXT_ANCHOR_MASK) {
        case TEXT_ANCHOR_START_IMPL:
            return TEXT_ANCHOR_START;
        case TEXT_ANCHOR_MIDDLE_IMPL:
            return TEXT_ANCHOR_MIDDLE;
        default:
            return TEXT_ANCHOR_END;
        }
    }

    /**
     * @param newTextAnchor The textAnchor is used when the property is not 
     *        inherited
     */
    public void setTextAnchor(final int newTextAnchor) {
        if (!isInherited(PROPERTY_TEXT_ANCHOR) 
            && newTextAnchor == getTextAnchor()) {
            return;
        }

        modifyingNode();
        setComputedTextAnchor(newTextAnchor);
        setInheritedQuiet(PROPERTY_TEXT_ANCHOR, false);
        propagatePackedPropertyState(PROPERTY_TEXT_ANCHOR, 
                                     pack & TEXT_ANCHOR_MASK);
        modifiedNode();
    }

    /**
     * Sets the value of the computed text anchor property.
     *
     * @param newTextAnchor the new value for the computed text anchor property.
     */
    void setComputedTextAnchor(final int newTextAnchor) {
        pack &= ~TEXT_ANCHOR_MASK;
        switch (newTextAnchor) {
        case TEXT_ANCHOR_START:
            pack |= TEXT_ANCHOR_START_IMPL;
            break;
        case TEXT_ANCHOR_MIDDLE:
            pack |= TEXT_ANCHOR_MIDDLE_IMPL;
            break;
        default:
            pack |= TEXT_ANCHOR_END_IMPL;
            break;
        }
    }

    /**
     * Handles the 'bolder' and 'lighter' values as follows:
     *
     * - bolder computes to font-weight + 300. If the result
     *   is more than 900, then the computed value is 900.
     * - lighter computes to font-weight - 300. If the result
     *   is less than 100, then the comuted value is 100.
     *
     * *NOTE*
     *
     * This is not exactly implementing the CSS2 specification for bolder but
     * that part of the CSS 2 specification is known to be
     * underspecified. Handling of 'bolder' is being respecified for CSS
     * 2.1. Furthermore, the CSS 2 specification does not address the needs for
     * SVG Fonts well. For example, if you have the following fonts in the font
     * data base:
     *
     * font A:  Arial, 100 to 500
     * font B:  Arial, 600 to 900
     *
     * element 1: font-family="Arial" font-weight="normal" 
     *     |
     *     + - element 2: font-family="Arial" font-weight="bolder"
     *
     * font-weight for element 1 is 400. The font weight for element B should be
     * that of the font for which there is a next bolder weight.  In our
     * example, it is font A which has font weight 500. However, it is not the
     * desired behavior in most cases.
     *
     * Secondly, the SVG Font format allows non consecutive font-weight values
     * in the <font-face> element. For example:
     *
     * font C: Arial, 100,900
     * font D: Arial, 200,400
     *
     * While this does not make a lot of sense, it is possible to specify such
     * fonts and it is unclear what the behavior of bolder should be in that
     * case. It seems that taking the matching font which has the most distance
     * from the current font-weight could work, but that would be limiting
     * 'bolder' to a one step change....
     *
     * The approach taken in the Perseus implementation is such that bolder will
     * work for common cases with common Font specifications.
     *
     * @param refPackedFontWeight the reference font weight.
     * @param relFontWeight the possibly relative font weight.
     * @return the computed font weight.
     */
    int computeFontWeight(final int refPackedFontWeight,
                          final int relFontWeight) {
        // Handle bolder and lighter
        if (relFontWeight == FONT_WEIGHT_BOLDER) {
            setMarker(FONT_WEIGHT_BOLDER_MARKER);
            clearMarker(FONT_WEIGHT_LIGHTER_MARKER);
            switch (refPackedFontWeight) {
            case FONT_WEIGHT_100_IMPL:
                return FONT_WEIGHT_400_IMPL;
            case FONT_WEIGHT_200_IMPL:
                return FONT_WEIGHT_500_IMPL;
            case FONT_WEIGHT_300_IMPL:
                return FONT_WEIGHT_600_IMPL;
            case FONT_WEIGHT_400_IMPL:
                return FONT_WEIGHT_700_IMPL;
            case FONT_WEIGHT_500_IMPL:
                return FONT_WEIGHT_800_IMPL;
            case FONT_WEIGHT_600_IMPL:
            case FONT_WEIGHT_700_IMPL:
            case FONT_WEIGHT_800_IMPL:
            case FONT_WEIGHT_900_IMPL:
            default:
                return FONT_WEIGHT_900_IMPL;
            }
        } else if (relFontWeight == FONT_WEIGHT_LIGHTER) {
            setMarker(FONT_WEIGHT_LIGHTER_MARKER);
            clearMarker(FONT_WEIGHT_BOLDER_MARKER);
            switch (refPackedFontWeight) {
            default:
            case FONT_WEIGHT_100_IMPL:
            case FONT_WEIGHT_200_IMPL:
            case FONT_WEIGHT_300_IMPL:
            case FONT_WEIGHT_400_IMPL:
                return FONT_WEIGHT_100_IMPL;
            case FONT_WEIGHT_500_IMPL:
                return FONT_WEIGHT_200_IMPL;
            case FONT_WEIGHT_600_IMPL:
                return FONT_WEIGHT_300_IMPL;
            case FONT_WEIGHT_700_IMPL:
                return FONT_WEIGHT_400_IMPL;
            case FONT_WEIGHT_800_IMPL:
                return FONT_WEIGHT_500_IMPL;
            case FONT_WEIGHT_900_IMPL:
                return FONT_WEIGHT_600_IMPL;
            }
        } 
            
        clearMarker(FONT_WEIGHT_BOLDER_MARKER);
        clearMarker(FONT_WEIGHT_LIGHTER_MARKER);
        switch (relFontWeight) {
            default:
            case FONT_WEIGHT_100:
                return FONT_WEIGHT_100_IMPL;
            case FONT_WEIGHT_200:
                return FONT_WEIGHT_200_IMPL;
            case FONT_WEIGHT_300:
                return FONT_WEIGHT_300_IMPL;
            case FONT_WEIGHT_400:
                return FONT_WEIGHT_400_IMPL;
            case FONT_WEIGHT_500:
                return FONT_WEIGHT_500_IMPL;
            case FONT_WEIGHT_600:
                return FONT_WEIGHT_600_IMPL;
            case FONT_WEIGHT_700:
                return FONT_WEIGHT_700_IMPL;
            case FONT_WEIGHT_800:
                return FONT_WEIGHT_800_IMPL;
            case FONT_WEIGHT_900:
                return FONT_WEIGHT_900_IMPL;
        }
    }

    /**
     * Utility method: computes the transform needed to fit the input 
     * box into the given output box for the given input alignment 
     * property.
     *
     * @param ix the x origin of the input box
     * @param iy the y origin of the input box
     * @param iw the width of the input box
     * @param ih the height of the input box
     * @param ox the x origin of the output box
     * @param oy the y origin of the output box
     * @param ow the width of the output box
     * @param oh the height of the output box
     * @param align the desired alignment. One of ALIGN_NONE, or ALIGN_XMIDYMID
     * @param result the transform to which the fitting transform is 
     *        concatenated
     *
     */
    public static void getFittingTransform(final float ix, final float iy,
                                           final float iw, final float ih,
                                           final float ox, final float oy,
                                           final float ow, final float oh,
                                           final int align, 
                                           final Transform result) {
        float inAR  = iw / ih;
        float outAR = ow / oh;

        // Move to output box location (ox,oy)
        result.mTranslate(ox, oy);

        if (align == ALIGN_NONE) {
            // Scale without preserving aspect ratio
            result.mScale(ow / iw, oh / ih);
            // Move to (0,0)
            result.mTranslate(-ix, -iy);
        } else if (inAR < outAR) {
            float sf = oh / ih;
            result.mScale(sf);
            result.mTranslate(-ix - (iw - ow * ih / oh) / 2, -iy);
        } else {
            float sf = ow / iw;
            result.mScale(sf);
            result.mTranslate(-ix, -iy - (ih - oh * iw / ow) / 2);
        }
    }

    /**
     * @return an adequate <code>ElementNodeProxy</code> for this node.
     */
    ElementNodeProxy buildProxy() {
        return new StructureNodeProxy(this);
    }

    /**
     * Paints this node into the input <code>RenderGraphics</code>.  By default,
     * a <code>StructureNode</code> only renders its regular (DOM) children and
     * it does not perform any rendering itself.
     *
     * @param rg the <tt>RenderGraphics</tt> where the node should paint itself
     */
    public void paint(final RenderGraphics rg) {
        if (canRenderState != 0) {
            return;
        }

        paint(getFirstChildNode(), rg);
    }

    /**
     * Supported traits: font-family, font-size, font-style, font-weight, 
     * text-anchor.
     *
     * @param traitName the name of the trait which the element may support.
     * @return true if this element supports the given trait in one of the
     *         trait accessor methods.
     */
    boolean supportsTrait(final String traitName) {
        if (SVGConstants.SVG_FONT_FAMILY_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_FONT_SIZE_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_FONT_STYLE_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_FONT_WEIGHT_ATTRIBUTE == traitName
            || 
            SVGConstants.SVG_TEXT_ANCHOR_ATTRIBUTE == traitName) {
            return true;
        } else {
            return super.supportsTrait(traitName);
        }
    }

    /**
     * Supported traits: font-family, font-size, font-style, font-weight, 
     * text-anchor.
     *
     * @param name the requested trait name (e.g., "font-family").
     * @return the trait's value, as a string (e.g., "serif").
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to a String (SVG Tiny only).
     */
    String getSpecifiedTraitImpl(final String name)
        throws DOMException {
        if ((SVGConstants.SVG_FONT_FAMILY_ATTRIBUTE == name
             &&
             isInherited(PROPERTY_FONT_FAMILY))
            ||
            (SVGConstants.SVG_FONT_SIZE_ATTRIBUTE == name
             &&
             isInherited(PROPERTY_FONT_SIZE))
            ||
            (SVGConstants.SVG_FONT_STYLE_ATTRIBUTE == name
             &&
             isInherited(PROPERTY_FONT_STYLE))
            ||
            (SVGConstants.SVG_FONT_WEIGHT_ATTRIBUTE == name
             &&
             isInherited(PROPERTY_FONT_WEIGHT))
            ||
            (SVGConstants.SVG_TEXT_ANCHOR_ATTRIBUTE == name
             &&
             isInherited(PROPERTY_TEXT_ANCHOR))) {
            return SVGConstants.CSS_INHERIT_VALUE;
        } else {
            return super.getSpecifiedTraitImpl(name);
        }
    }

    /**
     * Converts the input font-style value to a string trait value.
     *
     * @param fontStyle the font-style value to convert.
     * @return the corresponding string trait value.
     */
    String fontWeightToStringTrait(final int fontWeight) {
        switch (fontWeight) {
        case StructureNode.FONT_WEIGHT_100_IMPL:
            return SVGConstants.CSS_100_VALUE;
        case StructureNode.FONT_WEIGHT_200_IMPL:
            return SVGConstants.CSS_200_VALUE;
        case StructureNode.FONT_WEIGHT_300_IMPL:
            return SVGConstants.CSS_300_VALUE;
        case StructureNode.FONT_WEIGHT_400_IMPL:
            return SVGConstants.CSS_400_VALUE;
        case StructureNode.FONT_WEIGHT_500_IMPL:
            return SVGConstants.CSS_500_VALUE;
        case StructureNode.FONT_WEIGHT_600_IMPL:
            return SVGConstants.CSS_600_VALUE;
        case StructureNode.FONT_WEIGHT_700_IMPL:
            return SVGConstants.CSS_700_VALUE;
        case StructureNode.FONT_WEIGHT_800_IMPL:
            return SVGConstants.CSS_800_VALUE;
        case StructureNode.FONT_WEIGHT_900_IMPL:
        default:
            return SVGConstants.CSS_900_VALUE;
        }
    }

    /**
     * Converts the input text-anchor value to a string trait value.
     *
     * @param textAnchor the text-anchor value to convert.
     * @return the corresponding string trait value.
     */
    String textAnchorToStringTrait(final int textAnchor) {
        switch (textAnchor) {
        case StructureNode.TEXT_ANCHOR_START_IMPL:
            return SVGConstants.CSS_START_VALUE;
        case StructureNode.TEXT_ANCHOR_MIDDLE_IMPL:
            return SVGConstants.CSS_MIDDLE_VALUE;
        case StructureNode.TEXT_ANCHOR_END_IMPL:
            return SVGConstants.CSS_END_VALUE;
        default:
            // Should _never_ happen. 
            throw new Error();
        }
    }

    /**
     * Converts the input font-style value to a string trait value.
     *
     * @param fontStyle the font-style value to convert. In packed form, with
     *        mask applied.
     * @return the corresponding string trait value.
     */
    String fontStyleToStringTrait(final int fontStyle) {
        switch (fontStyle) {
        case StructureNode.FONT_STYLE_NORMAL_IMPL:
            return SVGConstants.CSS_NORMAL_VALUE;
        case StructureNode.FONT_STYLE_OBLIQUE_IMPL:
            return SVGConstants.CSS_OBLIQUE_VALUE;
        case StructureNode.FONT_STYLE_ITALIC_IMPL:
        default:
            return SVGConstants.CSS_ITALIC_VALUE;
        }
    }
    
    /**
     * Supported traits: font-family, font-size, font-style, font-weight, 
     * text-anchor.
     *
     * @param name the requested trait name (e.g., "font-family").
     * @return the trait's value, as a string (e.g., "serif").
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to a String (SVG Tiny only).
     */
    public String getTraitImpl(final String name)
        throws DOMException {
        if (SVGConstants.SVG_FONT_FAMILY_ATTRIBUTE == name) {
            return toStringTraitQuote(getFontFamily());
        } else if (SVGConstants.SVG_FONT_SIZE_ATTRIBUTE == name) {
            return Float.toString(getFontSize());
        } else if (SVGConstants.SVG_FONT_STYLE_ATTRIBUTE == name) {
            return fontStyleToStringTrait(pack & FONT_STYLE_MASK);
        } else if (SVGConstants.SVG_FONT_WEIGHT_ATTRIBUTE == name) {
            return fontWeightToStringTrait(pack & FONT_WEIGHT_MASK);
        } else if (SVGConstants.SVG_TEXT_ANCHOR_ATTRIBUTE == name) {
            return textAnchorToStringTrait(pack & TEXT_ANCHOR_MASK);
        } else {
            return super.getTraitImpl(name);
        }
    }

    /**
     * Supported float traits: font-size
     *
     * @param name the requested trait name (e.g, "font-size")
     * @return the trait's value, as a float.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to a float
     * @throws SecurityException if the application does not have the necessary
     * privilege rights to access this (SVG) content.
     */
    float getFloatTraitImpl(final String name)
        throws DOMException {
        if (SVGConstants.SVG_FONT_SIZE_ATTRIBUTE == name) {
            return getFontSize();
        } else {
            return super.getFloatTraitImpl(name);
        }
    }

    /**
     * @param traitName the trait name.
     */
    TraitAnim createTraitAnimImpl(final String traitName) {
        if (SVGConstants.SVG_FONT_SIZE_ATTRIBUTE == traitName) {
            return new FloatTraitAnim(this, traitName, TRAIT_TYPE_FLOAT);
        } else if (SVGConstants.SVG_FONT_FAMILY_ATTRIBUTE == traitName
                   ||
                   SVGConstants.SVG_FONT_STYLE_ATTRIBUTE == traitName
                   ||
                   SVGConstants.SVG_FONT_WEIGHT_ATTRIBUTE == traitName
                   ||
                   SVGConstants.SVG_TEXT_ANCHOR_ATTRIBUTE == traitName) {
            return new StringTraitAnim(this, NULL_NS, traitName);
        } else {
            return super.createTraitAnimImpl(traitName);
        }
    }

    /**
     * Set the trait value as float.
     *
     * @param name the trait's name.
     * @param value the trait's value.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if the requested
     * trait's value cannot be specified as a float
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is an invalid value for the given trait.
     */
    void setFloatArrayTrait(final String name, final float[][] value)
        throws DOMException {
        if (SVGConstants.SVG_FONT_SIZE_ATTRIBUTE == name) {
            checkPositive(name, value[0][0]);
            setFontSize(value[0][0]);
        } else {
            super.setFloatArrayTrait(name, value);
        }
    }

    /**
     * Validates the input trait value.
     *
     * @param traitName the name of the trait to be validated.
     * @param value the value to be validated
     * @param reqNamespaceURI the namespace of the element requesting 
     *        validation.
     * @param reqLocalName the local name of the element requesting validation.
     * @param reqTraitNamespace the namespace of the trait which has the values
     *        value on the requesting element.
     * @param reqTraitName the name of the trait which has the values value on 
     *        the requesting element.
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     *         value is incompatible with the given trait.
     */
    public float[][] validateFloatArrayTrait(
            final String traitName,
            final String value,
            final String reqNamespaceURI,
            final String reqLocalName,
            final String reqTraitNamespace,
            final String reqTraitName) throws DOMException {
        if (SVGConstants.SVG_FONT_SIZE_ATTRIBUTE == traitName) {
            return new float[][] {{parsePositiveFloatTrait(traitName, value)}};
        } else {
            return super.validateFloatArrayTrait(traitName,
                                                 value,
                                                 reqNamespaceURI,
                                                 reqLocalName,
                                                 reqTraitNamespace,
                                                 reqTraitName);
        }
    }

    /**
     * Validates the input trait value.
     *
     * @param namespaceURI the trait's namespace URI.
     * @param traitName the name of the trait to be validated.
     * @param value the value to be validated
     * @param reqNamespaceURI the namespace of the element requesting 
     *        validation.
     * @param reqLocalName the local name of the element requesting validation.
     * @param reqTraitNamespace the namespace of the trait which has the values
     *        value on the requesting element.
     * @param reqTraitName the name of the trait which has the values value on 
     *        the requesting element.
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     *         value is incompatible with the given trait.
     */
    String validateTraitNS(final String namespaceURI,
                           final String traitName,
                           final String value,
                           final String reqNamespaceURI,
                           final String reqLocalName,
                           final String reqTraitNamespace,
                           final String reqTraitName) throws DOMException {
        if (namespaceURI != null && namespaceURI != NULL_NS) {
            return super.validateTraitNS(namespaceURI,
                                         traitName,
                                         value,
                                         reqNamespaceURI,
                                         reqLocalName,
                                         reqTraitNamespace,
                                         reqTraitName);
        }

        if (SVGConstants.SVG_FONT_SIZE_ATTRIBUTE == traitName) {
            throw unsupportedTraitType(traitName, TRAIT_TYPE_FLOAT);
        }

        if (SVGConstants.SVG_FONT_FAMILY_ATTRIBUTE == traitName) {
            if (SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                return toStringTraitQuote
                        ((String[]) 
                        getInheritedPropertyState(PROPERTY_FONT_FAMILY));
            }

            return value;
        } else if (SVGConstants.SVG_FONT_STYLE_ATTRIBUTE == traitName) {
            if (SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                return fontStyleToStringTrait
                        (getInheritedPackedPropertyState(PROPERTY_FONT_STYLE));
            } else if (!SVGConstants.CSS_NORMAL_VALUE.equals(value)
                       &&
                       !SVGConstants.CSS_ITALIC_VALUE.equals(value)
                       &&
                       !SVGConstants.CSS_OBLIQUE_VALUE.equals(value)) {
                throw illegalTraitValue(traitName, value);
            }
            
            return value;
        } else if (SVGConstants.SVG_FONT_WEIGHT_ATTRIBUTE == traitName) {
            if (SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                return fontWeightToStringTrait
                        (getInheritedPackedPropertyState(PROPERTY_FONT_WEIGHT));
            } else if (SVGConstants.CSS_NORMAL_VALUE.equals(value)) {
                return SVGConstants.CSS_400_VALUE;
            } else if (SVGConstants.CSS_BOLD_VALUE.equals(value)) {
                return SVGConstants.CSS_700_VALUE;
            } else if (SVGConstants.CSS_BOLDER_VALUE.equals(value)) {
                // We need to base on the parent's animated value.
                int packedFontWeight = 
                        getInheritedPackedPropertyState(PROPERTY_FONT_WEIGHT);
                int fontWeight 
                    = computeFontWeight(packedFontWeight, FONT_WEIGHT_BOLDER);
                return fontWeightToStringTrait(fontWeight);
            } else if (SVGConstants.CSS_LIGHTER_VALUE.equals(value)) {
                // We need to base on the parent's animated value.
                int packedFontWeight = 
                        getInheritedPackedPropertyState(PROPERTY_FONT_WEIGHT);
                int fontWeight = 
                        computeFontWeight(packedFontWeight, 
                                          FONT_WEIGHT_LIGHTER);
                return fontWeightToStringTrait(fontWeight);
            } else if (!SVGConstants.CSS_100_VALUE.equals(value)
                       &&
                       !SVGConstants.CSS_200_VALUE.equals(value)
                       &&
                       !SVGConstants.CSS_300_VALUE.equals(value)
                       &&
                       !SVGConstants.CSS_400_VALUE.equals(value)
                       &&
                       !SVGConstants.CSS_NORMAL_VALUE.equals(value)
                       &&
                       !SVGConstants.CSS_500_VALUE.equals(value)
                       &&
                       !SVGConstants.CSS_600_VALUE.equals(value)
                       &&
                       !SVGConstants.CSS_700_VALUE.equals(value)
                       &&
                       !SVGConstants.CSS_800_VALUE.equals(value)
                       &&
                       !SVGConstants.CSS_900_VALUE.equals(value)) {
                throw illegalTraitValue(traitName, value);
            }
            return value;
        } else if (SVGConstants.SVG_TEXT_ANCHOR_ATTRIBUTE == traitName) {
            if (SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                return textAnchorToStringTrait
                        (getInheritedPackedPropertyState(PROPERTY_TEXT_ANCHOR));
            }
            if (!SVGConstants.CSS_START_VALUE.equals(value)
                &&
                !SVGConstants.CSS_MIDDLE_VALUE.equals(value)
                &&
                !SVGConstants.CSS_END_VALUE.equals(value)) {
                throw illegalTraitValue(traitName, value);
            }
            return value;
        }

        return super.validateTraitNS(namespaceURI,
                                     traitName,
                                     value,
                                     reqNamespaceURI,
                                     reqLocalName,
                                     reqTraitNamespace,
                                     reqTraitName);
    }

    /**
     * Supported traits: font-family, font-size, font-style, font-weight, 
     * text-anchor.
     *
     * Supported traits: stroke-width, stroke-miterlimit, stroke-dashoffset,
     * fill-rule, stroke-linejoin, stroke-linecap, display, visibility, 
     * color, fill, stroke, fill-opacity, stroke-opacity, stroke-dasharray
     *
     * @param name the trait name (e.g., "stroke-width")
     * @param value the trait's string value (e.g, "3")
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
            
            // ======================= font-family ====================== //
            
            if (SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                setInherited(PROPERTY_FONT_FAMILY, true);
            } else {
                setFontFamily(parseFontFamilyTrait(name, value));
            }
        } else if (SVGConstants.SVG_FONT_SIZE_ATTRIBUTE == name) {
            
            // ======================= font-size ======================== //
            
            if (SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                setFloatInherited(PROPERTY_FONT_SIZE, true);
            } else {
                setFontSize(parsePositiveFloatTrait(name, value));
            }
        } else if (SVGConstants.SVG_FONT_STYLE_ATTRIBUTE == name) {
            
            // ======================= font-style ======================= //

            if (SVGConstants.CSS_NORMAL_VALUE.equals(value)) {
                setFontStyle(TextProperties.FONT_STYLE_NORMAL);
            } else if (SVGConstants.CSS_ITALIC_VALUE.equals(value)) {
                setFontStyle(TextProperties.FONT_STYLE_ITALIC);
            } else if (SVGConstants.CSS_OBLIQUE_VALUE.equals(value)) {
                setFontStyle(TextProperties.FONT_STYLE_OBLIQUE);
            } else if (SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                setPackedInherited(PROPERTY_FONT_STYLE, true);
            } else {
                throw illegalTraitValue(name, value);
            }
                
        } else if (SVGConstants.SVG_FONT_WEIGHT_ATTRIBUTE == name) {

            // ======================= font-weight ======================= //

            if (SVGConstants.CSS_100_VALUE.equals(value)) {
                setFontWeight(TextNode.FONT_WEIGHT_100);
            } else if (SVGConstants.CSS_200_VALUE.equals(value)) {
                setFontWeight(TextNode.FONT_WEIGHT_200);
            } else if (SVGConstants.CSS_300_VALUE.equals(value)) {
                setFontWeight(TextNode.FONT_WEIGHT_300);
            } else if (SVGConstants.CSS_400_VALUE.equals(value)) {
                setFontWeight(TextNode.FONT_WEIGHT_400);
            } else if (SVGConstants.CSS_NORMAL_VALUE.equals(value)) {
                setFontWeight(TextNode.FONT_WEIGHT_NORMAL);
            } else if (SVGConstants.CSS_500_VALUE.equals(value)) {
                setFontWeight(TextNode.FONT_WEIGHT_500);
            } else if (SVGConstants.CSS_600_VALUE.equals(value)) {
                setFontWeight(TextNode.FONT_WEIGHT_600);
            } else if (SVGConstants.CSS_700_VALUE.equals(value)) {
                setFontWeight(TextNode.FONT_WEIGHT_700);
            } else if (SVGConstants.CSS_BOLD_VALUE.equals(value)) {
                setFontWeight(TextNode.FONT_WEIGHT_BOLD);
            } else if (SVGConstants.CSS_800_VALUE.equals(value)) {
                setFontWeight(TextNode.FONT_WEIGHT_800);
            } else if (SVGConstants.CSS_900_VALUE.equals(value)) {
                setFontWeight(TextNode.FONT_WEIGHT_900);
            } else if (SVGConstants.CSS_BOLDER_VALUE.equals(value)) {
                setFontWeight(TextNode.FONT_WEIGHT_BOLDER);
            } else if (SVGConstants.CSS_LIGHTER_VALUE.equals(value)) {
                setFontWeight(TextNode.FONT_WEIGHT_LIGHTER);
            } else if (SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                setPackedInherited(PROPERTY_FONT_WEIGHT, true);
                clearMarker(FONT_WEIGHT_BOLDER_MARKER);
                clearMarker(FONT_WEIGHT_LIGHTER_MARKER);
            } else {
                throw illegalTraitValue(name, value);
            }
        } else if (SVGConstants.SVG_TEXT_ANCHOR_ATTRIBUTE == name) {

            // ======================= text-anchor ====================== //

            if (SVGConstants.CSS_START_VALUE.equals(value)) {
                setTextAnchor(TextProperties.TEXT_ANCHOR_START);
            } else if (SVGConstants.CSS_MIDDLE_VALUE.equals(value)) {
                setTextAnchor(TextProperties.TEXT_ANCHOR_MIDDLE);
            } else if (SVGConstants.CSS_END_VALUE.equals(value)) {
                setTextAnchor(TextProperties.TEXT_ANCHOR_END);
            } else if (SVGConstants.CSS_INHERIT_VALUE.equals(value)) {
                setPackedInherited(PROPERTY_TEXT_ANCHOR, true);
            } else {
                throw illegalTraitValue(name, value);
            }
        } else {
            super.setTraitImpl(name, value);
        }
    }

    /**
     * @param name the name of the trait to convert.
     * @param value the float trait value to convert.
     */
    String toStringTrait(final String name, final float[][] value) {
        if (SVGConstants.SVG_FONT_SIZE_ATTRIBUTE == name) {
            return Float.toString(value[0][0]);
        } else {
            return super.toStringTrait(name, value);
        }
    }

    /**
     * Set the trait value as float.
     * Supported float traits: font-size.
     *
     * @param name the trait name (e.g, "font-size")
     * @param value the trait value (e.g, 10f)
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if the requested
     * trait's value cannot be specified as a float
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is an invalid value for the given trait.
     * @throws SecurityException if the application does not have the necessary
     * privilege rights to access this (SVG) content.
     */
    public void setFloatTraitImpl(final String name, final float value)
        throws DOMException {
        if (SVGConstants.SVG_FONT_SIZE_ATTRIBUTE == name) {
            checkPositive(name, value);
            setFontSize(value);
        } else {
            super.setFloatTraitImpl(name, value);
        }
    }

}
