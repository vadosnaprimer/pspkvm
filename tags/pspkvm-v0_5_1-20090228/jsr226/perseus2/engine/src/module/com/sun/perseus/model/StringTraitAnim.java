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
 * @version $Id: StringTraitAnim.java,v 1.2 2006/04/21 06:38:55 st125089 Exp $
 */
final class StringTraitAnim extends TraitAnim {
    /**
     * The trait's base value.
     */
    String[] baseValue = new String[1];

    /**
     * Constructs a new StringTraitAnim for a given ElementNode trait
     * in the given namespace.
     *
     * @param targetElement the ElementNode whose trait is animated.
     * @param targetNamespace the target trait's namespace. Should not 
     *        be null. The per-element partition namespace should be 
     *        represented by the ElementNode.NULL_NS value.
     * @param targetTrait the name of the animated trait.
     */
    public StringTraitAnim(final ElementNode targetElement,
                           final String traitNamespace,
                           final String traitName) {
        super(targetElement, 
              traitNamespace, 
              traitName, 
              ElementNode.TRAIT_TYPE_STRING);
    } 

    /**
     * @return the trait's value, as a String.
     */
    protected String getTraitImpl() {
        // This returns the computed trait value, using the specified 
        // trait value.
        return targetElement.validateTraitNS(traitNamespace,
                                             traitName, 
                                             getSpecifiedTraitNS(),
                                             targetElement.getNamespaceURI(),
                                             targetElement.getLocalName(),
                                             traitNamespace,
                                             traitName);
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
        // Validate on set so that we do not get an error later.
        // However, we keep the specified value around, for properties 
        // that may have relative or inherited values.
        targetElement.validateTraitNS(traitNamespace,
                                      traitName,
                                      value,
                                      targetElement.getNamespaceURI(),
                                      targetElement.getLocalName(),
                                      traitNamespace,
                                      traitName);
        specifiedTraitValue = value;
    }

    /**
     * Returns the BaseValue as an array of objects.
     *
     * @return the base value as an object array. The dimensions of the
     *         returned array depend on the trait.
     * @see com.sun.perseus.model.BaseValue
     */
    public Object[] getBaseValue() {
        baseValue[0] = getTraitImpl();
        return baseValue;
    }

    /**
     * Applies the animation effect. The implementation makes sure it 
     * implements the sandwich model by 'pulling' values from the 
     * root animation (i.e., the animation with the highest priority).
     */
    void apply() {
        String value = (String) rootAnim.compute()[0];

        if (traitNamespace == ElementNode.NULL_NS) {
            targetElement.setTraitImpl(traitName, value);
        } else {
            targetElement.setTraitNSImpl(traitNamespace, traitName, value);
        }
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
        StringRefValues refValues = new StringRefValues();
        
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
        refValues.segments = new StringSegment[nSegments];

        // Build the first segment.
        refValues.segments[0] = new StringSegment();
        refValues.segments[0].start = new String[] {values[0]};
        refValues.segments[0].end = new String[] {values[1]};

        targetElement.validateTraitNS(anim.traitNamespace, 
                                      anim.traitName,
                                      refValues.segments[0].start[0],
                                      anim.getNamespaceURI(),
                                      anim.getLocalName(),
                                      reqTraitNamespace,
                                      reqTraitName);

        targetElement.validateTraitNS(anim.traitNamespace, 
                                      anim.traitName,
                                      refValues.segments[0].end[0],
                                      anim.getNamespaceURI(),
                                      anim.getLocalName(),
                                      reqTraitNamespace,
                                      reqTraitName);
        
        StringSegment prevSegment = refValues.segments[0];

        for (int i = 1; i < nSegments; i++) {
            refValues.segments[i] = new StringSegment();
            refValues.segments[i].start = prevSegment.end;
            refValues.segments[i].end = new String[] {values[i + 1]};
            targetElement.validateTraitNS(anim.traitNamespace, 
                                          anim.traitName,
                                          refValues.segments[i].end[0],
                                          anim.getNamespaceURI(),
                                          anim.getLocalName(),
                                          reqTraitNamespace,
                                          reqTraitName);
            prevSegment = refValues.segments[i];
        }

        return refValues;
    }

    /**
     * Used to sum two animated trait values. Because String traits cannot 
     * be summed, this always returns the second parameter value.
     *
     * @param valueA the base value. May be null.
     * @param valueB the value to add to the base value. If the baseValue 
     */
    public Object[] sum(Object[] valueA, Object[] valueB) {
        // String animations do not support additive behavior.
        throw new Error();
    }

    /**
     * Used to multiply an animated trait value by a number of iterations.
     *
     * @param value the animated trait value to multiply.
     * @param iter the number of iteration to account for.
     * @return the multiply result.
     */
    public Object[] multiply(Object[] value, int iter) {
        // String animations do not support additive behavior.
        throw new Error();
    }


}

