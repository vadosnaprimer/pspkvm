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

import com.sun.perseus.util.SVGConstants;

import org.w3c.dom.DOMException;

import org.w3c.dom.svg.SVGElement;

import java.util.Vector;

/**
 * <code>AnimateMotion</code> represents an SVG Tiny 
 * <code>&lt;animateMotion&gt;</code> element.
 *
 * @version $Id: AnimateMotion.java,v 1.5 2006/06/29 10:47:29 ln156897 Exp $
 */
public class AnimateMotion extends AbstractAnimate {
    /**
     * Rotate type when an angle is specified in the rotate attribute.
     */
    static final int ROTATE_ANGLE = 1;

    /**
     * Rotate type when the rotate attribute is set to auto.
     */
    static final int ROTATE_AUTO = 2;

    /**
     * Rotate type when the rotate attribute is set to auto-reverse
     */
    static final int ROTATE_AUTO_REVERSE = 3;

    /**
     * The path attribute value.
     */
    String path;

    /**
     * The keyPoints attribute value
     */
    float[] keyPoints;

    /**
     * The rotate angle. Used if rotateType is ROTATE_ANGLE
     */
    float rotate;

    /**
     * The rotation's cos value
     */
    float cosRotate = 1;

    /**
     * The rotation angle's sin value.
     */
    float sinRotate;

    /**
     * The rotate type. One of ROTATE_ANGLE (an angle was specified) or
     * ROTATE_AUTO, ROTATE_AUTO_REVERSE
     */
    int rotateType = ROTATE_ANGLE;

    /**
     * Used, temporarily, to hold the refValues for the path attribute.
     */
    RefValues pathRefValues;

    /**
     * Used, temporarily, to hold the refValues for mpath child.
     */
    RefValues mpathRefValues;

    /**
     * Builds a new AnimateMotion element that belongs to the given
     * document. This <code>AnimateMotion</code> will belong 
     * to the <code>DocumentNode</code>'s time container.
     *
     * @param ownerDocument the document this node belongs to.
     * @throws IllegalArgumentException if the input ownerDocument is null
     */
    public AnimateMotion(final DocumentNode ownerDocument) {
        super(ownerDocument, SVGConstants.SVG_ANIMATE_MOTION_TAG);

        // Default calcMode for animateMotion is paced.
        calcMode = CALC_MODE_PACED;

        // AnimateMotion operates on a fixed trait name
        traitName = SVGConstants.SVG_MOTION_PSEUDO_ATTRIBUTE;
    }

    /**
     * Used by <code>DocumentNode</code> to create a new instance from
     * a prototype <code>TimedElementNode</code>.
     *
     * @param doc the <code>DocumentNode</code> for which a new node is
     *        should be created.
     * @return a new <code>TimedElementNode</code> for the requested document.
     */
    public ElementNode newInstance(final DocumentNode doc) {
        return new AnimateMotion(doc);
    }

    /**
     * @param traitName the name of the trait which the element may support.
     * @return true if this element supports the given trait in one of the
     *         trait accessor methods.
     */
    boolean supportsTrait(final String traitName) {
        if (SVGConstants.SVG_PATH_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_KEY_POINTS_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_ROTATE_ATTRIBUTE == traitName) {
            return true;
        } else {
            return super.supportsTrait(traitName);
        }
    }

    // JAVADOC COMMENT ELIDED
    public String getTraitImpl(final String name)
        throws DOMException {
        if (SVGConstants.SVG_PATH_ATTRIBUTE == name) {
            return path;
        } else if (SVGConstants.SVG_KEY_POINTS_ATTRIBUTE == name) {
            return toStringTrait(keyPoints);
        } else if (SVGConstants.SVG_ROTATE_ATTRIBUTE == name) {
            switch (rotateType) {
            case ROTATE_ANGLE:
                return Float.toString(rotate);
            case ROTATE_AUTO:
                return SVGConstants.SVG_AUTO_VALUE;
            case ROTATE_AUTO_REVERSE:
            default:
                return SVGConstants.SVG_AUTO_REVERSE_VALUE;
            }
        } else {
            return super.getTraitImpl(name);
        }
    }

    /**
     * The following call lets the animate implementation map 
     * the time segment indices and the time segment penetration
     * into refValues indices and penetration, in case these are
     * different. Typically, these are the same, but they may be
     * different, for example in the case of animateMotion with
     * keyPoints.
     *
     * @param si the time segment index.
     * @param sp the segment penetration.
     * @param sisp an array where the mapped si and sp value should
     *        be stored. si is at index 0 and sp at index 1.
     */
    protected void mapToSegmentProgress(final int si, final float sp, 
                                        final float[] sisp) {
        // animateMotion also maps to segments directly unless there
        // is a keyPoints attribute specified, in which case it 
        // overrides the default behavior.
        if (keyPoints == null || actualCalcMode == CALC_MODE_PACED) {
            super.mapToSegmentProgress(si, sp, sisp);
            return;
        }

        // We are dealing with a keyPoints animateMotion.
        // We only do the computation on the first component, because
        // the input and output indices for animateMotion with keyTimes
        // should be the same on all components (by definition).
        float startDist = keyPoints[si];
        float endDist = keyPoints[si + 1];

        float dist = sp * endDist + (1 - sp) * startDist;

        // Now that we know how far along we should be on the path, 
        // find the corresponding segment.
        ((MotionRefValues) refValues).getSegmentAtDist(sisp, dist);
    }

    /**
     * @param name the trait's name.
     * @param value the trait's value.
     *
     * @throws DOMException with error code NOT_SUPPORTED_ERR if the requested
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
        if (SVGConstants.SVG_PATH_ATTRIBUTE == name) {
            checkWriteLoading(name);
            // path values are validated in the validate() method,
            // just like to/from/by and values are
            path = value;
        } else if (SVGConstants.SVG_KEY_POINTS_ATTRIBUTE == name) {
            checkWriteLoading(name);
            keyPoints = parseFloatArrayTrait(name, value, ';');
        } else if (SVGConstants.SVG_ROTATE_ATTRIBUTE == name) {
            checkWriteLoading(name);
            
            if (SVGConstants.SVG_AUTO_VALUE.equals(value)) {
                rotate = 0;
                rotateType = ROTATE_AUTO;
            } else if (SVGConstants.SVG_AUTO_REVERSE_VALUE.equals(value)) {
                rotate = 0;
                rotateType = ROTATE_AUTO_REVERSE;
            } else {
                // The value is neither 'auto' nor 'auto-reverse'. It 
                // must be an angle value.
                rotate = parseFloatTrait(name, value);
                cosRotate = MathSupport.cos(MathSupport.toRadians(rotate));
                sinRotate = MathSupport.sin(MathSupport.toRadians(rotate));
                rotateType = ROTATE_ANGLE;
            }
        } else {
            super.setTraitImpl(name, value);
        }
    }

    /**
     * Computes refTimes from the calcMode and keyTimes attributes. Validates
     * that the keyTimes attribute is compatible with the animate set up. This
     * may be overridden by subclasses (e.g., animateMotion), when there are 
     * special rules for checking keyTimes compatiblity.
     */
    protected void computeRefTimes() throws DOMException {
        // if there is no keyPoints defined or if the calcMode is
        // paced, we use the default behavior.
        if (keyPoints == null || actualCalcMode == CALC_MODE_PACED) {
            super.computeRefTimes();
            return;
        }

        // Check keyTimes is compatible with the animation specification.
        // 
        // a) keyTimes should be specified (i.e., not null)
        //
        // b) In all cases, the first keyTime must be zero.
        // 
        // c) For non-discrete animations, the last keyTime must be one.
        //
        // d) There should be as many keyTimes as there are keyPoints
        //
        if (/* a) */ keyTimes == null
            ||
            /* b) */ keyTimes.length < 1 || keyTimes[0] != 0
            ||
            /* c) */ (actualCalcMode != CALC_MODE_DISCRETE 
                      && 
                      keyTimes[keyTimes.length - 1] != 1)
            ||
            /* d) */ keyTimes.length != keyPoints.length) {
            throw animationError(
                    idRef,
                    traitNamespace,
                    traitName,
                    targetElement.getNamespaceURI(),
                    targetElement.getLocalName(),
                    getId(),
                    getNamespaceURI(),
                    getLocalName(),
                    Messages.formatMessage(
                        Messages.ERROR_INVALID_ANIMATION_KEY_TIMES, 
                        new Object[] {
                            getTrait(SVGConstants.SVG_KEY_TIMES_ATTRIBUTE)
                        }));
        }
        
        // If the calcMode is _not_ discrete, we trim the last '1' 
        // value.
        if (actualCalcMode != CALC_MODE_DISCRETE) {
            refTimes = new float[keyTimes.length - 1];
            System.arraycopy(keyTimes, 0, refTimes, 0, refTimes.length);
        } else {
            refTimes = keyTimes;
        }
    }

    /**
     * Validates the path and mpath values sources.
     *
     * @throws DOMException if there is a validation error, for example if the
     *         to value is incompatible with the target trait or if the target
     *         trait is not animatable.
     */
    final void validateValuesExtra() throws DOMException {
        // Validate the path attribute value
        pathRefValues = null;
        if (path != null) {
            pathRefValues = traitAnim.toRefValues(
                    this,
                    new String[] {path},
                    null,
                    SVGConstants.SVG_PATH_ATTRIBUTE);
        }

        // Now, validate the mpath child, if any.
        mpathRefValues = null;
        SVGElement c = (SVGElement) getFirstElementChild();
        SVGElement mpath = null;
        while (c != null) {
            if (SVGConstants.SVG_MPATH_TAG.equals(c.getLocalName())
                &&
                SVGConstants.SVG_NAMESPACE_URI.equals(c.getNamespaceURI())) {
                mpath = c;
                break;
            }
            c = (SVGElement) c.getNextElementSibling();
        }

        if (mpath != null) {
            String pathHref = ((ElementNode) mpath).getTraitNSImpl(
                    SVGConstants.XLINK_NAMESPACE_URI,
                    SVGConstants.SVG_HREF_ATTRIBUTE);
            if (pathHref != null) {
                boolean pathHrefError = false;
                if (pathHref.startsWith("#")) {
                    String pathId = pathHref.substring(1);
                    ElementNode path = 
                            (ElementNode) ownerDocument.getElementById(pathId);
                    if (path != null) {
                        mpathRefValues = traitAnim.toRefValues(
                                this,
                                new String[] {
                                    path.getTraitImpl(
                                        SVGConstants.SVG_D_ATTRIBUTE)
                                },
                                null,
                                SVGConstants.SVG_D_ATTRIBUTE);
                    } else {
                        pathHrefError = true;
                    }
                } else {
                    pathHrefError = true;
                }

                if (pathHrefError) {
                    throw animationError(idRef,
                                         traitNamespace,
                                         traitName,
                                         targetElement.getNamespaceURI(),
                                         targetElement.getLocalName(),
                                         getId(),
                                         getNamespaceURI(),
                                         getLocalName(),
                                         Messages.formatMessage
                                         (Messages.ERROR_INVALID_MPATH_HREF, 
                                          new Object[] {pathHref}));
                }
            } else {
                throw animationError(idRef,
                                     traitNamespace,
                                     traitName,
                                     targetElement.getNamespaceURI(),
                                     targetElement.getLocalName(),
                                     getId(),
                                     getNamespaceURI(),
                                     getLocalName(),
                                     Messages.formatMessage
                                     (Messages.ERROR_MISSING_MPATH_HREF, null));
            }
        }
    }

    /**
     * Computes the 'right' source for reference values, depending on the 
     * precedence rules for the different values sources. 
     *
     * @throws DOMException if there is no way to compute a set of reference
     *         values, for example if none of the values sources is specified.
     */
    final void selectRefValuesExtra() throws DOMException {
        if (mpathRefValues != null) {
            refValues = mpathRefValues;
        } else if (pathRefValues != null) {
            refValues = pathRefValues;
        }
    }

}
