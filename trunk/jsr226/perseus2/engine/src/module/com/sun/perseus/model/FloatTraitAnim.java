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

import org.w3c.dom.DOMException;

/**
 * @version $Id: FloatTraitAnim.java,v 1.3 2006/06/29 10:47:31 ln156897 Exp $
 */
class FloatTraitAnim extends TraitAnim {
    /**
     * Constructs a new FloatTraitAnim for a given ElementNode trait.
     *
     * @param targetElement the ElementNode whose trait is animated.
     * @param targetTrait the name of the animated trait.
     * @param traitType the trait type.
     */
    public FloatTraitAnim(final ElementNode targetElement,
                          final String traitName,
                          final String traitType) {
        super(targetElement, ElementNode.NULL_NS, traitName, traitType);
    }

    /**
     * Returns the BaseValue as an array of objects.
     *
     * @return the base value as an object array. The dimensions of the
     *         returned array depend on the trait.
     * @see com.sun.perseus.model.BaseValue
     */
    public Object[] getBaseValue() {
        return targetElement.validateFloatArrayTrait(
                traitName, 
                getSpecifiedTraitNS(),
                targetElement.getNamespaceURI(),
                targetElement.getLocalName(),
                traitNamespace,
                traitName);
    }

    /**
     * Applies the animation effect. The implementation makes sure it 
     * implements the sandwich model by 'pulling' values from the 
     * root animation (i.e., the animation with the highest priority).
     */
    void apply() {
        float[][] v = (float[][]) rootAnim.compute();
        targetElement.setFloatArrayTrait(traitName, v);
    }

    /**
     * @return the trait's value, as a String.
     */
    protected String getTraitImpl() {
        // This returns the computed trait value, using the specified 
        // trait value.
        float[][] v = targetElement.validateFloatArrayTrait(
                traitName, 
                specifiedTraitValue,
                targetElement.getNamespaceURI(),
                targetElement.getLocalName(),
                traitNamespace,
                traitName);

        // Now, convert that value to a string.
        return targetElement.toStringTrait(traitName, v);
    }

    /**
     * Sets the trait's base value, as a String.
     * 
     * @param value the new trait base value.
     *
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is an invalid value for the given trait or null.
     */
    void setTraitImpl(String value) throws DOMException {
        targetElement.validateFloatArrayTrait(traitName,
                                              value,
                                              targetElement.getNamespaceURI(),
                                              targetElement.getLocalName(),
                                              traitNamespace,
                                              traitName);

        specifiedTraitValue = value;
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
        FloatRefValues refValues = new FloatRefValues();
        
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
        refValues.segments = new FloatSegment[nSegments];

        // Build the first segment.
        refValues.segments[0] = new FloatSegment();
        refValues.segments[0].start = 
            targetElement.validateFloatArrayTrait(anim.traitName,
                                                  values[0],
                                                  anim.getNamespaceURI(),
                                                  anim.getLocalName(),
                                                  reqTraitNamespace,
                                                  reqTraitName);
        refValues.segments[0].end = 
            targetElement.validateFloatArrayTrait(anim.traitName,
                                                  values[1],
                                                  anim.getNamespaceURI(),
                                                  anim.getLocalName(),
                                                  reqTraitNamespace,
                                                  reqTraitName);
       
        FloatSegment prevSegment = refValues.segments[0];

        for (int i = 1; i < nSegments; i++) {
            refValues.segments[i] = new FloatSegment();
            refValues.segments[i].start = prevSegment.end;
            refValues.segments[i].end = 
                targetElement.validateFloatArrayTrait(anim.traitName,
                                                      values[i + 1],
                                                      anim.getNamespaceURI(),
                                                      anim.getLocalName(),
                                                      reqTraitNamespace,
                                                      reqTraitName);
            prevSegment = refValues.segments[i];
        }

        return refValues;
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

        for (int ci = 0; ci < fva.length; ci++) {
            for (int di = 0; di < fva[ci].length; di++) {
                fvb[ci][di] += fva[ci][di];
            }
        }

        return valueB;
    }

    /**
     * Used to multiply an animated trait value by a number of iterations.
     *
     * @param value the animated trait value to multiply.
     * @param iter the number of iteration to account for.
     * @return the multiply result.
     */
    public Object[] multiply(Object[] value, int iter) {
        float[][] fv = (float[][]) value;
        float[][] r = new float[fv.length][];

        for (int ci = 0; ci < fv.length; ci++) {
            r[ci] = new float[fv[ci].length];
            for (int di = 0; di < fv[ci].length; di++) {
                r[ci][di] = fv[ci][di] * iter;
            }
        }
                 
        return r;
    }

    /**
     * @return true, because FloatTraitAnim support interpolation.
     */
    boolean supportsInterpolation() {
        return true;
    }

}
