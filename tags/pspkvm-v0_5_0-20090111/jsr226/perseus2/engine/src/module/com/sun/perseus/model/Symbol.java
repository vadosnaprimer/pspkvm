/**
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

import com.sun.perseus.platform.URLResolver;

import com.sun.perseus.util.SVGConstants;

import org.w3c.dom.DOMException;

import org.w3c.dom.svg.SVGMatrix;
import org.w3c.dom.svg.SVGRect;

import com.sun.perseus.j2d.Transform;

/**
 * Represents an SVG Tiny <code>&lt;a&gt;</code> element.
 * An symbol is a simple <code>Group</code> extension which
 * simply has a href and a target attribute.
 * 
 * @version $Id: Symbol.java,v 1.6 2006/06/29 10:47:35 ln156897 Exp $
 */
public class Symbol extends Group {
    /**
     * This SVG node's viewBox. If null, then there is
     * no viewBox.
     */
    protected float[][] viewBox;

    /**
     * Constructor.
     *
     * @param ownerDocument this element's owner <code>DocumentNode</code>
     */
    public Symbol(final DocumentNode ownerDocument) {
        super(ownerDocument);
    }

    /**
     * @return the SVGConstants.SVG_A_TAG value
     */
    public String getLocalName() {
        return SVGConstants.SVG_SYMBOL_TAG;
    }

    /**
     * Used by <code>DocumentNode</code> to create a new instance from
     * a prototype <code>SymbolNode</code>.
     *
     * @param doc the <code>DocumentNode</code> for which a new node is
     *        should be created.
     * @return a new <code>Symbol</code> for the requested document.
     */
    public ElementNode newInstance(final DocumentNode doc) {
        return new Symbol(doc);
    }

    /**
     * Apply this node's viewBox x/y translation if it is not (0,0).
     *
     * @param tx the <code>Transform</code> to apply additional node 
     *        transforms to. This may be null.
     * @param workTx a <code>Transform</code> which can be re-used if a 
     *        new <code>Transform</code> needs to be created and workTx
     *        is not the same instance as tx.
     * @return a transform with this node's transform added.
     */
    protected Transform appendTransform(Transform tx,
                                        final Transform workTx) {
        if (viewBox == null
            ||
            (viewBox[0][0] == 0 && viewBox[0][1] == 0)) {
            return tx;
        }

        tx = recycleTransform(tx, workTx);
        
        if (viewBox != null) {
            tx.mTranslate(-viewBox[0][0], -viewBox[0][1]);
        }
        
        return tx;
    }

    // JAVADOC COMMENT ELIDED
    public SVGRect getBBox() {
        if (viewBox != null) {
            Transform t = new Transform(1, 0, 0, 1, -viewBox[0][0], 
                                        -viewBox[0][1]);
            return addBBox(null, t);
        }
        
        return addBBox(null, null);
    }

    // JAVADOC COMMENT ELIDED
    public SVGMatrix getScreenCTM() {
        SVGMatrix m = super.getScreenCTM();
        if (m != null) {
            if (viewBox != null) {
                m = m.mTranslate(viewBox[0][0], viewBox[0][1]);
            }
        } 

        return m;
    }

    /**
     * Sets a new value for the viewBox. If there viewBox is
     * not null, it should be of size 4
     * @param newViewBox the new viewBox for this <tt>SVG</tt>
     * 
     * @throws IllegalArgumentException if the input viewBox is 
     *         not null and of size other than 4.
     */
    public void setViewBox(final float[][] newViewBox) {

        if (newViewBox != null) {
            if (newViewBox.length != 3 
                ||
                newViewBox[0] == null
                ||
                newViewBox[1] == null
                ||
                newViewBox[2] == null
                ||
                newViewBox[0].length != 2
                ||
                newViewBox[1][0] < 0 
                || 
                newViewBox[2][0] < 0) {
                throw new IllegalArgumentException();
            }
        }

        modifyingNode();

        if (viewBox == null) {
            viewBox = new float[3][];
            viewBox[0] = new float[2];
            viewBox[1] = new float[1];
            viewBox[2] = new float[1];
        }

        viewBox[0][0] = newViewBox[0][0];    
        viewBox[0][1] = newViewBox[0][1];    
        viewBox[1][0] = newViewBox[1][0];    
        viewBox[2][0] = newViewBox[2][0];    

        recomputeTransformState();
        recomputeProxyTransformState();
        computeCanRenderEmptyViewBoxBit(viewBox);
        modifiedNode();
    }

    /**
     * @return this Symbol's viewBox
     */
    public float[][] getViewBox() {
        return viewBox;
    }

    /**
     * Symbol handles the target trait.
     *
     * @param traitName the name of the trait which the element may support.
     * @return true if this element supports the given trait in one of the
     *         trait accessor methods.
     */
    boolean supportsTrait(final String traitName) {
        if (SVGConstants.SVG_VIEW_BOX_ATTRIBUTE == traitName) {
            return true;
        } else {
            return super.supportsTrait(traitName);
        }
    }

    /**
     * Symbol handles the viewBox trait.
     *
     * @param name the requested trait name
     * @return the requested trait's value.
     * 
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to a String (SVG Tiny only).
     */
    public String getTraitImpl(final String name)
        throws DOMException {
        if (SVGConstants.SVG_VIEW_BOX_ATTRIBUTE == name) {
            if (viewBox == null) {
                return "";
            } else {
                return "" + viewBox[0][0] + SVGConstants.COMMA 
                    + viewBox[0][1] + SVGConstants.COMMA 
                    + viewBox[1][0] + SVGConstants.COMMA 
                    + viewBox[2][0];
            }
        } else {
            return super.getTraitImpl(name);
        }
    }

    /**
     * @param name the name of the trait to convert.
     * @param value the float trait value to convert.
     */
    String toStringTrait(final String name, final float[][] value) {
        if (SVGConstants.SVG_VIEW_BOX_ATTRIBUTE == name) {
            float[] vb = {value[0][0], value[0][1], value[1][0], value[2][0]};
            return toStringTrait(vb);
        } else {
            return super.toStringTrait(name, value);
        }
    }

    /**
     * Symbol handles the viewBox Rect trait.
     *
     * @param name the requested trait name (e.g., "viewBox")
     * @return the requested trait SVGRect value.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to {@link
     * org.w3c.dom.svg.SVGRect SVGRect}
     * @throws SecurityException if the application does not have the necessary
     * privilege rights to access this (SVG) content.
     */
    SVGRect getRectTraitImpl(final String name)
        throws DOMException {
        if (SVGConstants.SVG_VIEW_BOX_ATTRIBUTE.equals(name)) {
            return toSVGRect(viewBox);
        } else {
            return super.getRectTraitImpl(name);
        }
    }

    /**
     * @param traitName the trait name.
     */
    TraitAnim createTraitAnimImpl(final String traitName) {
        if (SVGConstants.SVG_VIEW_BOX_ATTRIBUTE == traitName) {
            return new FloatTraitAnim(this, traitName, TRAIT_TYPE_SVG_RECT);
        } else {
            return super.createTraitAnimImpl(traitName);
        }
    }

    /**
     * Symbol handles the viewBox trait.
     *
     * @param name the name of the trait to set.
     * @param value the value of the trait to set.
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
    public void setTraitImpl(final String name, 
                         final String value)
        throws DOMException {
        if (SVGConstants.SVG_VIEW_BOX_ATTRIBUTE == name) {
            setViewBox(toViewBox(name, value));
        } else {
            super.setTraitImpl(name, value);
        }
    }

    /**
     * Symbol handles the viewBox Rect trait.
     *
     * @param name the trait name (e.g., "viewBox"
     * @param rect the trait value
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if the requested
     * trait's value cannot be specified as an {@link org.w3c.dom.svg.SVGRect
     * SVGRect}
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is an invalid value for the given trait or null.  SVGRect is
     * invalid if the width or height values are set to negative.
     * @throws SecurityException if the application does not have the necessary
     * privilege rights to access this (SVG) content.
     */
    public void setRectTraitImpl(final String name, final SVGRect rect)
        throws DOMException {
        // Note that here, we use equals because the string 
        // has not been interned.
        if (SVGConstants.SVG_VIEW_BOX_ATTRIBUTE.equals(name)) {
            if (rect == null) {
                throw illegalTraitValue(name, null);
            }
            
            if (rect.getWidth() < 0 || rect.getHeight() < 0) {
                throw illegalTraitValue(name, toStringTrait(new float[]
                    {rect.getX(), 
                     rect.getY(), 
                     rect.getWidth(), 
                     rect.getHeight()}));
            }
                
            setViewBox(new float[][] {
                new float[] {rect.getX(), rect.getY()}, 
                new float[] {rect.getWidth()}, 
                new float[] {rect.getHeight()}
            });
            
        } else {
            super.setRectTraitImpl(name, rect);
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
        if (SVGConstants.SVG_VIEW_BOX_ATTRIBUTE == name) {
            setViewBox(value);
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
        if (SVGConstants.SVG_VIEW_BOX_ATTRIBUTE == traitName) {
            return ownerDocument.viewBoxParser.parseViewBox(value);
        } else {
            return super.validateFloatArrayTrait(traitName,
                                                 value,
                                                 reqNamespaceURI,
                                                 reqLocalName,
                                                 reqTraitNamespace,
                                                 reqTraitName);
        }
    }

}
