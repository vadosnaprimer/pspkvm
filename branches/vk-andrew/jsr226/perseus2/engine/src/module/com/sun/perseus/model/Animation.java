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
import org.w3c.dom.events.Event;

import com.sun.perseus.util.SVGConstants;

/**
 * The <code>Animation</code> class is the base class for all the
 * SVG animation elements. Note that this is <em>not</em> the base
 * class for media elements such as <code>&lt;audio&gt;</code> or
 * <code>&lt;video&gt;</code>.
 *
 * The <code>Animation</code> class is the abstraction manipulated
 * by <code>TraitAnim</code> to compose the chain of currently 
 * active animations in priority order.
 *
 * @version $Id: Animation.java,v 1.5 2006/06/29 10:47:29 ln156897 Exp $
 */
public abstract class Animation extends TimedElementNode 
    implements BaseValue, IDRef {
    /**
     * Constant used by default to identify the type of animation values.
     * Derived classes, such as AnimateTransform, may use other values.
     */
    public static final int TYPE_GENERIC = 1;
    
    /**
     * Controls whether the animation has an effect on its target or not.
     * There are situations where the animation may have no effect, for
     * example when none of the values/to/from/by attributes are specified
     * on an <animateTranform> or <animate>.
     */
    boolean hasNoEffect;

    /**
     * The associated TraitAnim
     */
    TraitAnim traitAnim;

    /**
     * This animation's base value, if it needs a base value.
     * The base value is needed in some scenarios, for example
     * in the case of 'to-animations'.
     */
    BaseValue baseVal;

    /**
     * This animation's target element.
     */
    ElementNode targetElement;

    /**
     * This animation's target trait name.
     */
    String traitName;

    /**
     * This animation's target trait namespace.
     */
    String traitNamespace;

    /**
     * The identifier of the target animation element.
     */
    String idRef;

    /**
     * The type of animation.
     */
    int type = TYPE_GENERIC;

    /**
     * Builds a new animation element that belongs to the given
     * document. This <code>Animation</code> will belong 
     * to the <code>DocumentNode</code>'s time container.
     *
     * @param ownerDocument the document this node belongs to.
     * @param localName the element's local name
     * @throws IllegalArgumentException if the input ownerDocument is null
     */
    public Animation(final DocumentNode ownerDocument,
                     final String localName) {
        super(ownerDocument, localName);
        
        // If the document is _not_ loaded (i.e., we are at parse time),
        // add this animation to the document's animations vector for
        // validation at the end of the loading phase.
        if (!ownerDocument.loaded) {
            ownerDocument.animations.addElement(this);
        }
    }

    /**
     * Should be called when the animation is the target of an hyperlink.
     */
    public void activate() {
        timedElementSupport.activate();
    }

    /**
     * Sets this <code>Animation</code> targetElement's idRef
     *
     * @param idRef the identifier of the animation's target element.
     *        Should not be null
     */
    public void setIdRef(final String idRef) {
        this.idRef = idRef;
        ownerDocument.resolveIDRef(this, idRef);
    }

    /**
     * @return the <code>Use</code> element's idRef.
     */
    public String getIdRef() {
        return idRef;
    }

    /**
     * <code>IDRef</code> implementation.
     *
     * @param ref the resolved reference (mapped from the
     *        id passed to the setIdRef method).
     */
    public void resolveTo(final ElementNode ref) {
        targetElement = ref;
    }

    /**
     * Computes the animation value for the current animation's
     * simple time.
     *
     * @return the current animation value.
     */
    final Object[] compute() {
        return f(timedElementSupport.lastSampleTime);
    }

    /**
     * This is the animation function, the heart of the animation 
     * engine. There are multiple implementations of that function,
     * and no good default one, so this method is abstract.
     *
     * @param t the animation's simple time.
     */
    abstract Object[] f(final long t);

    /**
     * Returns the BaseValue as an array of objects.
     *
     * @return the base value as an object array. The dimensions of the
     *         returned array depend on the trait.
     * @see com.sun.perseus.model.BaseValue
     */
    public Object[] getBaseValue() {
        return compute();
    }

    /**
     * Event dispatching override, to capture begin and end events.
     *
     * When a begin event occurs, the animation adds itself to the target
     * element and trait's TraitAnim.
     *
     * When an end event occurs, the animation removes itself from the target
     * element and trait's TraitAnim if the animation is not in the frozen
     * state.
     *
     * @param  evt the event that occured
     */
    public void dispatchEvent(final ModelEvent evt) {
        super.dispatchEvent(evt);

        if (targetElement != null) {
            if (TimedElementSupport.BEGIN_EVENT_TYPE.equals(evt.getType())
                ||
                TimedElementSupport.SEEK_BEGIN_EVENT_TYPE.equals(
                        evt.getType())) {
                // Remove the animation, just in case the animation was 
                // previously frozen.
                if (traitAnim != null && !hasNoEffect) {
                    traitAnim.removeAnimation(this);
                    
                    // Now, add the animation.
                    traitAnim.addAnimation(this);
                }
            } else if (TimedElementSupport.LAST_DUR_END_EVENT_TYPE.equals(
                            evt.getType())) {
                // Only remove the animation if it is _not_ frozen.
                if ((timedElementSupport.fillBehavior 
                     != 
                     TimedElementSupport.FILL_BEHAVIOR_FREEZE)
                    &&
                    traitAnim != null && !hasNoEffect) {
                    traitAnim.removeAnimation(this);
                }
            } else if (TimedElementSupport.SEEK_END_EVENT_TYPE.equals(
                            evt.getType())
                       &&
                       traitAnim != null && !hasNoEffect) {
                // Remove the animation no matter what
                traitAnim.removeAnimation(this);
            }
        }
    }

    /**
     * Supported NS traits: xlink:href
     *
     * @param namespaceURI the trait's namespace.
     * @param traitName the name of the trait which the element may support.
     * @return true if this element supports the given trait in one of the
     *         trait accessor methods.
     */
    boolean supportsTraitNS(final String namespaceURI,
                            final String traitName) {
        if (SVGConstants.XLINK_NAMESPACE_URI.equals(namespaceURI)
            &&
            SVGConstants.SVG_HREF_ATTRIBUTE.equals(traitName)) {
            return true;
        } else {
            return super.supportsTraitNS(namespaceURI, traitName);
        }
    }

    /**
     * Set handles the xlink href attribute
     *
     * @param namespaceURI the requested trait's namespace URI.
     * @param name the requested trait's local name (i.e., un-prefixed, as 
     *        "href")
     * @return the requested trait's string value.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if requested
     * trait's computed value cannot be converted to a String (SVG Tiny only).
     * @throws SecurityException if the application does not have the necessary
     * privilege rights to access this (SVG) content.
     */
    public String getTraitNSImpl(String namespaceURI, String name)
        throws DOMException {
        if (SVGConstants.XLINK_NAMESPACE_URI.equals(namespaceURI)
            &&
            SVGConstants.SVG_HREF_ATTRIBUTE.equals(name)) {
            if (idRef == null) {
                return "";
            } 
            return "#" + idRef;
        } else {
            return super.getTraitNSImpl(namespaceURI, name);
        }
    }

    /**
     * Set supports the xlink:href trait.
     *
     * @param namespaceURI the trait's namespace.
     * @param name the trait's local name (un-prefixed, e.g., "href");
     * @param value the new trait value (e.g., "http://www.sun.com/mypng.png")
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERROR if the requested
     * trait is not supported on this element or null.
     * @throws DOMException with error code TYPE_MISMATCH_ERR if the requested
     * trait's value cannot be specified as a String
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is an invalid value for the given trait or null.
     * @throws DOMException with error code NO_MODIFICATION_ALLOWED_ERR: if
     * attempt is made to change readonly trait.
     * @throws SecurityException if the application does not have the necessary
     * privilege rights to access this (SVG) content.
     */
    public void setTraitNSImpl(final String namespaceURI, 
                               final String name, 
                               final String value)
        throws DOMException {
        if (SVGConstants.XLINK_NAMESPACE_URI.equals(namespaceURI)
            &&
            SVGConstants.SVG_HREF_ATTRIBUTE.equals(name)) {
            if (value == null || !value.startsWith("#")) {
                throw illegalTraitValue(name, value);
            }
            setIdRef(value.substring(1));
        } else {
            super.setTraitNSImpl(namespaceURI, name, value);
        }
    }

    /**
     * This method can be overridden by specific implementations to validate
     * that the various values attributes (values, to, from, by or any subset of
     * these) are valid i.e., compatible with the target element and target
     * trait.
     *
     * There are two situations when this method is called:
     * - parse time. When the document has been fully loaded, it validates all
     *   animations.
     * - run time. When a new animation is created, it validates itself when
     *   it is hooked into the tree (i.e., when its parent node is set).
     */
    abstract void validate();

    /**
     * When an Animation is hooked into the document tree, it needs
     * to validate (only if the Document is loaded).
     */
    final void nodeHookedInDocumentTree() {
        super.nodeHookedInDocumentTree();

        if (ownerDocument.loaded) {
            validate();
        }
    }

    /**
     * When an Animation is removed from the document tree, it needs
     * to remove itself from its associated traitAnim.
     */
    final void nodeUnhookedFromDocumentTree() {
        super.nodeUnhookedFromDocumentTree();

        if (traitAnim != null) {
            traitAnim.removeAnimation(this);
        }
    }

}
