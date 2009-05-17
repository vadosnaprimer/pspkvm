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
 * The <code>TraitAnim</code> class is the link between animation targets
 * (i.e., traits on elements) and animation elements.
 * 
 * <p>When an animation becomes active on a target (i.e., on a trait or pseudo
 * trait for an element), it invokes getAnimTrait(traitName) on the
 * corresponding element to get the trait's TraitAnim. One is created if the
 * trait is not currently animated. Then, the animation adds itself to the
 * TraitAnim through a call to addAnimation(). As an animation adds itself, it
 * becomes the TraitAnim's rootAnim. If there was no animation, the new
 * animation's baseVal becomes the TraitAnim itself. If there was already an
 * animation, the new Animation's baseVal becomes the previous rootAnim.</p>
 * 
 * <p>When an animation becomes inactive, it removes itself from the TraitAnim
 * by calling the removeAnimation method. When the last active animation removes
 * itself from the TraitAnim, the TraitAnim removes itself from the
 * ElementNode's TraitAnim list and it restores the trait's original base value
 * (stored as baseValue).</p>
 *
 * <p>This achieves the sandwich model behavior described in the SMIL Animation
 * specification (section 3.5). In particular, because the tree is sampled in
 * document order in Perseus, the animation that appears first in document order
 * will have lower priority (i.e., it will be added first to the TraitAnim). If
 * an animation is a time dependent of another one, then it will become active
 * after its time sync and will have higher priority than its time sync.</p>
 *
 * @version $Id: TraitAnim.java,v 1.4 2006/06/29 10:47:36 ln156897 Exp $
 */
abstract class TraitAnim implements BaseValue {
    /**
     * The TraitAnim underlying type. One of ElementNode.TRAIT_TYPE 
     * constants.
     */
    String traitType;

    /**
     * This animation's root.
     */
    Animation rootAnim;

    /**
     * The target element.
     */
    ElementNode targetElement;

    /**
     * The target trait name.
     */
    String traitName;

    /**
     * The target trait namespace.
     */
    String traitNamespace;

    /**
     * The trait's specified value, as a String.
     */
    String specifiedTraitValue;
    
    /**
     * True when the TraitAnim has at least one active animation.
     */
    boolean active;

    /**
     * Constructs a new TraitAnim for a given ElementNode trait
     * in the given namespace.
     *
     * @param targetElement the ElementNode whose trait is animated.
     *        Should not be null.
     * @param targetNamespace the target trait's namespace. Should not 
     *        be null. The per-element partition namespace should be 
     *        represented by the ElementNode.NULL_NS value.
     * @param targetTrait the name of the animated trait. Should not
     *        be null.
     */
    TraitAnim(final ElementNode targetElement,
              final String traitNamespace,
              final String traitName,
              final String traitType) {
        if (targetElement == null 
            || 
            traitName == null
            || 
            traitNamespace == null
            ||
            traitType == null) {
            throw new NullPointerException();
        }

        this.targetElement = targetElement;
        this.traitNamespace = traitNamespace;
        this.traitName = traitName;
        this.traitType = traitType;
    } 

    /**
     * @return the trait's specified base value, as a String.
     */
    public String getSpecifiedTraitNS() {
        if (specifiedTraitValue == null) {
            specifiedTraitValue = targetElement.getSpecifiedTraitNSImpl(
                    traitNamespace, traitName);
        }

        return specifiedTraitValue;
    }

    /**
     * Restores the base value. This is invoked when there are not more 
     * animations and the original base value needs to be restored.
     */
    final void restore() {
        // Now, restore the specified trait value
        if (traitNamespace == ElementNode.NULL_NS) {
            targetElement.setTraitImpl(traitName, specifiedTraitValue);
        } else {
            targetElement.setTraitNSImpl(traitNamespace, traitName, 
                                         specifiedTraitValue);
        }
    }

    /**
     * Adds a new animation to this TraitAnim. The new animation
     * becomes the highest priority animation. If this is the 
     * first animation added to the TraitAnim, the new animation's
     * base value becomes the TraitAnim itself and the TraitAnim
     * registers with the DocumentNode. If there is already
     * one or more animation in the TraitAnim, the baseValue for the
     * new animation becomes the previous animation root. In all
     * cases, the new animation becomes the rootAnim.
     *
     * @param newAnim the new highest priority animation for this TraitAnim.
     *        Should not be null.
     */
    void addAnimation(final Animation newAnim) {
        // Reject null values
        if (newAnim == null) {
            throw new NullPointerException();
        }

        if (rootAnim == null) {
            // This is the first animation.
            // Set the animation as the root animation and set its
            // baseValue. 
            rootAnim = newAnim;
            newAnim.baseVal = this;
            targetElement.ownerDocument.activeTraitAnims.addElement(this);

            // We need to recompute the specifiedTraitAnim at this point
            // Otherwise, the specifiedTrait value may be off (i.e., different
            // from what it was set to originally, when the TraitAnim was 
            // created.
            this.specifiedTraitValue = targetElement.getSpecifiedTraitNSImpl(
                    traitNamespace, traitName);
        } else {
            // This is a new animation in the sandwich.
            // The new animation becomes the highest priority animation and
            // its baseValue is the previous rootAnim.
            newAnim.baseVal = rootAnim;
            rootAnim = newAnim;
        }

        active = true;
    }

    /**
     * Removes the input animation from this TraitAnim. If the removed 
     * animation's baseValue is the TraitAnim itself, it means this is 
     * the last active animation on the trait and the TraitAnim will 
     * mark itself as inactive. If this is not the last animation,
     * then this animation's baseValue becomes the rootAnim.
     *
     * If removedAnim is not part of this TraitAnim, this method
     * does nothing.
     *
     * @param removedAnim the animation to remove from the TraitAnim.
     *        should not be null. 
     */
    void removeAnimation(final Animation removedAnim) {
        // Reject null values.
        if (removedAnim == null) {
            throw new NullPointerException();
        }

        if (removedAnim == rootAnim) {
            // Removing the root animatoin
            if (removedAnim.baseVal == this) {
                // This is the last animation in the TraitAnim.
                // Unregister from the Document.
                targetElement.ownerDocument.activeTraitAnims.removeElement(
                        this);
                rootAnim = null;

                // Mark the animation as inactive.
                active = false;
                restore();
            } else {
                rootAnim = (Animation) removedAnim.baseVal;
            }
        } else {
            if (rootAnim != null) {
                // Removing an animation other than the root.
                // Find the preceding animation.
                Animation prevAnim = null;
                Animation curAnim = rootAnim;
                while (curAnim.baseVal != this) {
                    if (curAnim.baseVal == removedAnim) {
                        prevAnim = curAnim;
                        break;
                    }
                    curAnim = (Animation) curAnim.baseVal;
                }
                
                // If removedAnimat was indeed part of the sandwich.
                if (prevAnim != null) {
                    prevAnim.baseVal = removedAnim.baseVal;
                }
            }
        }
    }

    /**
     * @param traitType the expected type for this trait. One of the 
     *        ElementNode.TRAIT_TYPE_.... constants (e.g., TRAIT_TYPE_STRING).
     *        All TraitAnim implementations must support TRAIT_TYPE_STRING.
     *        They may support additional types (for example, FloatTraitAnim
     *        may support TRAIT_TYPE_FLOAT).
     * @return the trait's computed value, as a String.
     *
     * @throws DOMException with error code TYPE_MISMATCH_ERR if the requested
     * trait's value cannot be specified as a requested type value.
     */
    final String getTrait(final String traitType) throws DOMException {
        if (ElementNode.TRAIT_TYPE_STRING.equals(traitType)
            ||
            this.traitType.equals(traitType)) {
            return getTraitImpl();
        } else {
            throw targetElement.unsupportedTraitTypeNS(traitName, 
                                                       traitNamespace, 
                                                       traitType);
        }
    }

    /**
     * @return the trait's value, as a String.
     */
    abstract protected String getTraitImpl();

    /**
     * Sets the trait's base value, as a String.
     * 
     * @param value the new trait base value.
     * @param traitType the requested trait type.
     *
     * @throws DOMException with error code TYPE_MISMATCH_ERR if the requested
     * trait's value cannot be specified as the requested traitType.
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is an invalid value for the given trait or null.
     */
    final void setTrait(String value, String traitType) throws DOMException {
        if (ElementNode.TRAIT_TYPE_STRING.equals(traitType)
            ||
            this.traitType.equals(traitType)) {
            setTraitImpl(value);
        } else {
            throw targetElement.unsupportedTraitTypeNS(traitName, 
                                                       traitNamespace, 
                                                       traitType);
        }
    }

    /**
     * Sets the trait's base value, as a String.
     * 
     * @param value the new trait base value.
     *
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is an invalid value for the given trait or null.
     */
    abstract void setTraitImpl(String value) throws DOMException;

    /**
     * Applies the animation effect. The implementation makes sure it 
     * implements the sandwich model by 'pulling' values from the 
     * root animation (i.e., the animation with the highest priority).
     */
    abstract void apply();

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
     */
    abstract RefValues toRefValues(
            final Animation anim,
            final String[] values,
            final String reqTraitNamespace,
            final String reqTraitName) throws DOMException;

    /**
     * Used to sum two animated trait values.
     *
     * @param valueA the base value. May be null.
     * @param valueB the value to add to the base value. If the baseValue 
     * @return the sum result.
     */
    abstract Object[] sum(Object[] valueA, Object[] valueB);

    /**
     * Used to multiply an animated trait value by a number of iterations.
     *
     * @param value the animated trait value to multiply.
     * @param iter the number of iteration to account for.
     * @return the multiply result.
     */
    abstract Object[] multiply(Object[] value, int iter);

    /**
     * @return true if this trait supports interpolation. false otherwise.
     */
    boolean supportsInterpolation() {
        return false;
    }
}
