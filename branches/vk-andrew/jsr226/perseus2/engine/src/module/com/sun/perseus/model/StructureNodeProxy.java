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
import com.sun.perseus.j2d.TextRenderingProperties;
import com.sun.perseus.j2d.Transform;
import org.w3c.dom.svg.SVGRect;

/**
 * A <code>TextProxy</code> proxies a <code>Text</code> node and 
 * computes its expanded content from the proxied text's data
 * content.
 *
 * @version $Id: StructureNodeProxy.java,v 1.4 2006/06/29 10:47:35 ln156897 Exp $
 */
public class StructureNodeProxy extends CompositeGraphicsNodeProxy
    implements TextRenderingProperties {    
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
     * @param proxiedNode <code>StructureNode</code> to proxy
     */
    protected StructureNodeProxy(final StructureNode proxiedNode) {
        super(proxiedNode);

        // We copy the computed value for all properties upon initialization. 
        // When the node is hooked in the tree, inherited properties will be
        // recomputed. Note that the fontStyle, fontWeight and textAnchor
        // properties are in the pack value that is already copied in the 
        // parent constructor.
        fontFamily = proxiedNode.fontFamily;
        fontSize = proxiedNode.fontSize;
    }

    /**
     * Returns the value for the given property.
     *
     * @return the value for the given property.
     */
    protected Object getPropertyState(final int propertyIndex) {
        switch (propertyIndex) {
        case TextNode.PROPERTY_FONT_FAMILY:
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
        case TextNode.PROPERTY_FONT_SIZE:
            return fontSize;
        default:
            return super.getFloatPropertyState(propertyIndex);
        }
    }
    
    /**
     * Returns the value for the given packed property.
     *
     * @return the value for the given property.
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
        if (propertyIndex != StructureNode.PROPERTY_FONT_WEIGHT) {
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
            if ((!((StructureNode) proxied).isInherited(propertyIndex) 
                 && 
                 !((StructureNode) proxied).isMarkerSet(
                        StructureNode.FONT_WEIGHT_BOLDER_MARKER)
                 &&
                 !((StructureNode) proxied).isMarkerSet(
                        StructureNode.FONT_WEIGHT_LIGHTER_MARKER))
                 ||
                 isPackedPropertyState(propertyIndex, parentPropertyValue)) {
                return;
            }
            
            setPackedPropertyState(propertyIndex, parentPropertyValue);
            propagatePackedPropertyState(propertyIndex, parentPropertyValue);
        }
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
        
        recomputePropertyState(
                TextNode.PROPERTY_FONT_FAMILY, 
                p.getPropertyState(TextNode.PROPERTY_FONT_FAMILY));
        recomputeFloatPropertyState(
                TextNode.PROPERTY_FONT_SIZE, 
                p.getFloatPropertyState(TextNode.PROPERTY_FONT_SIZE));
        recomputePackedPropertyState(
                TextNode.PROPERTY_FONT_STYLE, 
                p.getPackedPropertyState(TextNode.PROPERTY_FONT_STYLE));
        recomputePackedPropertyState(
                TextNode.PROPERTY_FONT_WEIGHT,
                p.getPackedPropertyState(TextNode.PROPERTY_FONT_WEIGHT));
        recomputePackedPropertyState(
                TextNode.PROPERTY_TEXT_ANCHOR,
                p.getPackedPropertyState(TextNode.PROPERTY_TEXT_ANCHOR));
    }
    
    /**
     * @return the tight bounding box in current user coordinate
     * space. 
     */
    public SVGRect getBBox() {
        return addBBox(null, null);
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
        ModelNode c = getFirstExpandedChild();
        while (c != null) {
            if (c.canRenderState == 0) {
                bbox = c.addBBox(bbox, c.appendTransform(t, null));
            }
            c = c.nextSibling;
        }

        return bbox;
    }

    /**
     * By default, an <code>ElementNodeProxy</code> does not paint anything.
     *
     * @param rg the <tt>RenderGraphics</tt> where the node should paint itself
     */
    public void paint(final RenderGraphics rg) {
        if (canRenderState != 0) {
            return;
        }

        paint(getFirstExpandedChild(), rg);
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
        case TextNode.PROPERTY_FONT_FAMILY:
            setFontFamily((String[]) propertyValue);
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
        case TextNode.PROPERTY_FONT_SIZE:
            setFontSize(propertyValue);
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
        case StructureNode.PROPERTY_FONT_STYLE:
            switch (propertyValue) {
                case StructureNode.FONT_STYLE_NORMAL_IMPL:
                    setFontStyle(FONT_STYLE_NORMAL);
                    break;
                case StructureNode.FONT_STYLE_ITALIC_IMPL:
                    setFontStyle(FONT_STYLE_ITALIC);
                    break;
                case StructureNode.FONT_STYLE_OBLIQUE_IMPL:
                default:
                    setFontStyle(FONT_STYLE_OBLIQUE);
                    break;
            }
            break;
        case StructureNode.PROPERTY_FONT_WEIGHT:
            switch (propertyValue) {
                case StructureNode.FONT_WEIGHT_100_IMPL:
                    setFontWeight(FONT_WEIGHT_100);
                    break;
                case StructureNode.FONT_WEIGHT_200_IMPL:
                    setFontWeight(FONT_WEIGHT_200);
                    break;
                case StructureNode.FONT_WEIGHT_300_IMPL:
                    setFontWeight(FONT_WEIGHT_300);
                    break;
                case StructureNode.FONT_WEIGHT_400_IMPL:
                    setFontWeight(FONT_WEIGHT_400);
                    break;
                case StructureNode.FONT_WEIGHT_500_IMPL:
                    setFontWeight(FONT_WEIGHT_500);
                    break;
                case StructureNode.FONT_WEIGHT_600_IMPL:
                    setFontWeight(FONT_WEIGHT_600);
                    break;
                case StructureNode.FONT_WEIGHT_700_IMPL:
                    setFontWeight(FONT_WEIGHT_700);
                    break;
                case StructureNode.FONT_WEIGHT_800_IMPL:
                    setFontWeight(FONT_WEIGHT_800);
                    break;
                case StructureNode.FONT_WEIGHT_900_IMPL:
                    setFontWeight(FONT_WEIGHT_900);
                    break;
            }
            break;
        case StructureNode.PROPERTY_TEXT_ANCHOR:
            switch (propertyValue) {
                case StructureNode.TEXT_ANCHOR_START_IMPL:
                    setTextAnchor(TEXT_ANCHOR_START);
                    break;
                case StructureNode.TEXT_ANCHOR_MIDDLE_IMPL:
                    setTextAnchor(TEXT_ANCHOR_MIDDLE);
                    break;
                case StructureNode.TEXT_ANCHOR_END_IMPL:
                default:
                    setTextAnchor(TEXT_ANCHOR_END);
                    break;
            }
            break;
        default:
            super.setPackedPropertyState(propertyIndex, propertyValue);
        }
    }
    
    /**
     * @return this node's computed font-family
     */
    public String[] getFontFamily() {
        return fontFamily;
    }

    /**
     * @param newFontFamily the new computed font-family property value.
     */
    public void setFontFamily(final String[] newFontFamily) {
        this.fontFamily = newFontFamily;
    }

    /**
     * @return this node's computed fontSize
     */
    public float getFontSize() {
        return fontSize;
    }
    
    /**
     * @param newFontSize the new computed font-size property value.
     */
    public void setFontSize(final float newFontSize) {
        this.fontSize = newFontSize;
    }

    /**
     * @return this node's computed fontStyle
     */
    public int getFontStyle() {
        switch (pack & StructureNode.FONT_STYLE_MASK) {
        case StructureNode.FONT_STYLE_NORMAL_IMPL:
            return FONT_STYLE_NORMAL;
        case StructureNode.FONT_STYLE_ITALIC_IMPL:
            return FONT_STYLE_ITALIC;
        default:
            return FONT_STYLE_OBLIQUE;
        }
    }

    /**
     * @param newFontStyle the new computed font-style property.
     */
    public void setFontStyle(final int newFontStyle) {
        pack &= ~StructureNode.FONT_STYLE_MASK;
        switch (newFontStyle) {
        case FONT_STYLE_NORMAL:
            pack |= StructureNode.FONT_STYLE_NORMAL_IMPL;
            break;
        case FONT_STYLE_ITALIC:
            pack |= StructureNode.FONT_STYLE_ITALIC_IMPL;
            break;
        default:
            pack |= StructureNode.FONT_STYLE_OBLIQUE_IMPL;
            break;
        }
    }

    /**
     * @return this node's computed fontWeight
     */
    public int getFontWeight() {
        switch (pack & StructureNode.FONT_WEIGHT_MASK) {
        case StructureNode.FONT_WEIGHT_100_IMPL:
            return FONT_WEIGHT_100;
        case StructureNode.FONT_WEIGHT_200_IMPL:
            return FONT_WEIGHT_200;
        case StructureNode.FONT_WEIGHT_300_IMPL:
            return FONT_WEIGHT_300;
        case StructureNode.FONT_WEIGHT_400_IMPL:
            return FONT_WEIGHT_400;
        case StructureNode.FONT_WEIGHT_500_IMPL:
            return FONT_WEIGHT_500;
        case StructureNode.FONT_WEIGHT_600_IMPL:
            return FONT_WEIGHT_600;
        case StructureNode.FONT_WEIGHT_700_IMPL:
            return FONT_WEIGHT_700;
        case StructureNode.FONT_WEIGHT_800_IMPL:
            return FONT_WEIGHT_800;
        case StructureNode.FONT_WEIGHT_900_IMPL:
            return FONT_WEIGHT_900;
        case StructureNode.FONT_WEIGHT_LIGHTER_IMPL:
            return TextNode.FONT_WEIGHT_LIGHTER;
        default:
            return TextNode.FONT_WEIGHT_BOLDER;
        }
    }

    /**
     * @param newFontWeight new computed value for the font-weight property.
     */
    public void setFontWeight(final int newFontWeight) {
        pack &= ~StructureNode.FONT_WEIGHT_MASK;

        switch (newFontWeight) {
        case FONT_WEIGHT_100:
            pack |= StructureNode.FONT_WEIGHT_100_IMPL;
            break;
        case FONT_WEIGHT_200:
            pack |= StructureNode.FONT_WEIGHT_200_IMPL;
            break;
        case FONT_WEIGHT_300:
            pack |= StructureNode.FONT_WEIGHT_300_IMPL;
            break;
        case FONT_WEIGHT_400:
            pack |= StructureNode.FONT_WEIGHT_400_IMPL;
            break;
        case FONT_WEIGHT_500:
            pack |= StructureNode.FONT_WEIGHT_500_IMPL;
            break;
        case FONT_WEIGHT_600:
            pack |= StructureNode.FONT_WEIGHT_600_IMPL;
            break;
        case FONT_WEIGHT_700:
            pack |= StructureNode.FONT_WEIGHT_700_IMPL;
            break;
        case FONT_WEIGHT_800:
            pack |= StructureNode.FONT_WEIGHT_800_IMPL;
            break;
        case FONT_WEIGHT_900:
            pack |= StructureNode.FONT_WEIGHT_900_IMPL;
            break;
        case StructureNode.FONT_WEIGHT_LIGHTER:
            pack |= StructureNode.FONT_WEIGHT_LIGHTER_IMPL;
            break;
        default:
            pack |= StructureNode.FONT_WEIGHT_BOLDER_IMPL;
            break;
        }
    }

    /**
     * @return this node's computed textAnchor
     */
    public int getTextAnchor() {
        switch (pack & StructureNode.TEXT_ANCHOR_MASK) {
        case StructureNode.TEXT_ANCHOR_START_IMPL:
            return TEXT_ANCHOR_START;
        case StructureNode.TEXT_ANCHOR_MIDDLE_IMPL:
            return TEXT_ANCHOR_MIDDLE;
        default:
            return TEXT_ANCHOR_END;
        }
    }

    /**
     * Sets the value of the computed text anchor property.
     *
     * @param newTextAnchor the new value for the computed text anchor property.
     */
    public void setTextAnchor(final int newTextAnchor) {
        pack &= ~StructureNode.TEXT_ANCHOR_MASK;
        switch (newTextAnchor) {
        case TEXT_ANCHOR_START:
            pack |= StructureNode.TEXT_ANCHOR_START_IMPL;
            break;
        case TEXT_ANCHOR_MIDDLE:
            pack |= StructureNode.TEXT_ANCHOR_MIDDLE_IMPL;
            break;
        default:
            pack |= StructureNode.TEXT_ANCHOR_END_IMPL;
            break;
        }
    }


}
