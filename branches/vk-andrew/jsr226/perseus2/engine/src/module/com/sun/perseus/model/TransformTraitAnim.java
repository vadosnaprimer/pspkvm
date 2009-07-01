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

import com.sun.perseus.platform.MathSupport;

import org.w3c.dom.DOMException;

/**
 * @version $Id: TransformTraitAnim.java,v 1.3 2006/06/29 10:47:36 ln156897 Exp $
 */
class TransformTraitAnim extends FloatTraitAnim {
    /**
     * Constructs a new TransformTraitAnim for a given ElementNode trait.
     *
     * @param targetElement the ElementNode whose trait is animated.
     * @param targetTrait the name of the animated trait.
     */
    public TransformTraitAnim(final ElementNode targetElement,
                              final String traitName) {
        super(targetElement, 
              traitName, 
              ElementNode.TRAIT_TYPE_SVG_MATRIX);
    }

    /**
     * Converts the input values set to a RefValues object.
     *
     * @param anim the <code>Animation</code> for which the values should be
     *        converted.
     * @param values a semi-colon seperated list of values which need to be
     * validated.
     * @param reqTraitNamespace the namespace of the trait which has the values
     *        value on the requesting element.
     * @param reqTraitName the name of the trait which has the values value on 
     *        the requesting element.
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is incompatible with the given trait.
     * @throws NullPointerException if values is null.
     */
    RefValues toRefValues(final Animation anim,
                          String[] values,
                          final String reqTraitNamespace,
                          final String reqTraitName) throws DOMException {
        // If we are dealing with a generic animation, the values should be 
        // as in the transform attribute: we can use the default FloatTraitAnim
        // toRefValues.
        if (anim.type == Animation.TYPE_GENERIC) {
            return super.toRefValues(anim, values, reqTraitNamespace, 
                                     reqTraitName);
        }

        // The interpretation of values depends on the animation type.
        TransformRefValues refValues = new TransformRefValues();
        
        if (values.length < 1) {
            throw new IllegalArgumentException();
        }

        if (values.length == 1) {
            String[] tmpValues = new String[2];
            tmpValues[0] = values[0];
            tmpValues[1] = values[0];
            values = tmpValues;
        }

        int nSegments = values.length - 1;
        TransformSegment[] segments = new TransformSegment[nSegments];

        // Build the first segment.
        segments[0] = new TransformSegment();
        segments[0].start = 
            validateValue(anim,
                          reqTraitNamespace,
                          reqTraitName,
                          values[0]);
        segments[0].end = 
            validateValue(anim,
                          reqTraitNamespace,
                          reqTraitName,
                          values[1]);
        segments[0].type = anim.type;

        TransformSegment prevSegment = segments[0];

        for (int i = 1; i < nSegments; i++) {
            segments[i] = new TransformSegment();
            segments[i].start = prevSegment.end;
            segments[i].end = 
                validateValue(anim,
                              reqTraitNamespace,
                              reqTraitName,
                              values[i + 1]);
            segments[i].type = anim.type;
            prevSegment = segments[i];
        }

        refValues.segments = segments;
        return refValues;
    }

    /**
     * Validates the input trait value.
     *
     * @param anim the animation for which the value is validated
     * @param traitNamespace the namespace trait
     * @param traitName the name of the trait to be validated.
     * @param value the value to be validated
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is incompatible with the given trait.
     */
    public float[] validateValue(final Animation anim,
                                 final String traitNamespace,
                                 final String traitName,
                                 final String value) {
        float[] v = null;

        switch (anim.type) {
        case AnimateTransform.TYPE_TRANSLATE:
            v = anim.parseFloatArrayTrait(traitName, value);
            if (v.length < 1 || v.length > 2) {
                throw anim.illegalTraitValue(traitName, value);
            }

            if (v.length == 1) {
                float[] tv = new float[2];
                tv[0] = v[0];
                tv[1] = 0;
                v = tv;
            }

            return v;

        case AnimateTransform.TYPE_SCALE:
            v = anim.parseFloatArrayTrait(traitName, value);
            if (v.length < 1 || v.length > 2) {
                throw anim.illegalTraitValue(traitName, value);
            }

            if (v.length == 1) {
                float[] tv = new float[2];
                tv[0] = v[0];
                tv[1] = v[0];
                v = tv;
            }

            return v;

        case AnimateTransform.TYPE_ROTATE:
            // This should be enhanced to support
            v = anim.parseFloatArrayTrait(traitName, value);
            if (v.length != 1 && v.length != 3) {
                throw anim.illegalTraitValue(traitName, value);
            }

            if (v.length != 3) {
                float[] tv = new float[3];
                tv[0] = v[0];
                tv[1] = 0;
                tv[2] = 0;
                v = tv;
            }

            v[0] = MathSupport.toRadians(v[0]);
            return v;

        case AnimateTransform.TYPE_SKEW_X:
        case AnimateTransform.TYPE_SKEW_Y:
        default:
            v = new float[] {anim.parseFloatTrait(traitName, value)};
            v[0] = MathSupport.toRadians(v[0]); 
            return v;
        }
    }

    /**
     * Used to sum two animated trait values.
     *
     * @param valueA the base value. May be null.
     * @param valueB the value to add to the base value. If the baseValue 
     */
    public Object[] sum(Object[] valueA, Object[] valueB) {
        if (valueA == null) {
            return valueB;
        }

        float[][] fva = (float[][]) valueA;
        float[][] fvb = (float[][]) valueB;

        // For transforms, we should do matrices multiplication, not
        // simple float additions.
        float[][] fv = new float[6][1];

        fv[0][0] = fvb[0][0] * fva[0][0] + fvb[1][0] * fva[2][0];
        fv[1][0] = fvb[0][0] * fva[1][0] + fvb[1][0] * fva[3][0];

        fv[2][0] = fvb[2][0] * fva[0][0] + fvb[3][0] * fva[2][0];
        fv[3][0] = fvb[2][0] * fva[1][0] + fvb[3][0] * fva[3][0];

        fv[4][0] = fvb[4][0] * fva[0][0] + fvb[5][0] * fva[2][0] + fva[4][0];
        fv[5][0] = fvb[4][0] * fva[1][0] + fvb[5][0] * fva[3][0] + fva[5][0];

        return fv;
    }

    /**
     * Used to multiply an animated trait value by a number of iterations.
     *
     * @param value the animated trait value to multiply.
     * @param iter the number of iteration to account for.
     * @return the multiply result.
     */
    public Object[] multiply(Object[] value, int iter) {
        for (int i = 1; i < iter; i++) {
            value = sum(value, value);
        }
                 
        return value;
    }

}
