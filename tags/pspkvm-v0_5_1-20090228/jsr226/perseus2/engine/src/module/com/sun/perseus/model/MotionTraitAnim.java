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

import com.sun.perseus.util.SVGConstants;

import org.w3c.dom.DOMException;

import java.util.Vector;

import com.sun.perseus.j2d.Path;

/**
 * @version $Id: MotionTraitAnim.java,v 1.4 2006/06/29 10:47:33 ln156897 Exp $
 */
class MotionTraitAnim extends TransformTraitAnim {
    /**
     * Constructs a new TransformTraitAnim for a given ElementNode trait.
     *
     * @param targetElement the ElementNode whose trait is animated.
     * @param targetTrait the name of the animated trait.
     */
    public MotionTraitAnim(final ElementNode targetElement,
                           final String traitName) {
        super(targetElement, traitName);
    }
    
    /**
     * Converts the input values set to a RefValues object. For 
     * a MotionTraitAnim, the input values may be the to/from/by/values values
     * or the path attribute value.
     *
     * @param anim the <code>Animation</code> for which the values should be
     *        converted.
     * @param values a semi-colon seperated list of values which need to be
     *        validated.
     * @param reqTraitNamespace the namespace of the trait which has the values
     *        value on the requesting element.
     * @param reqTraitName the name of the trait which has the values value on 
     *        the requesting element.
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     *         value is incompatible with the given trait.
     * @throws NullPointerException if values is null.
     */
    RefValues toRefValues(final Animation anim,
                          String[] values,
                          final String reqTraitNamespace,
                          final String reqTraitName) throws DOMException {
        AnimateMotion motion = (AnimateMotion) anim;

        // The interpretation of values depends on the attribute type.
        MotionRefValues refValues = new MotionRefValues(motion);
        
        if (values.length < 1) {
            throw new IllegalArgumentException();
        }

        if (!SVGConstants.SVG_PATH_ATTRIBUTE.equals(reqTraitName) &&
            !SVGConstants.SVG_D_ATTRIBUTE.equals(reqTraitName)) {

            //
            // We are dealing with a to/from/by or values attribute
            //

            if (values.length == 1) {
                String[] tmpValues = new String[2];
                tmpValues[0] = values[0];
                tmpValues[1] = values[0];
                values = tmpValues;
            }
            
            int nSegments = values.length - 1;
            refValues.segments = new MotionSegment[nSegments];
            
            // Build the first segment.
            float[] startPt = new float[2];
            float[] endPt = new float[2];
            
            validateCoordinate(anim, 
                               reqTraitNamespace,
                               reqTraitName,
                               values[0],
                               startPt);

            validateCoordinate(anim, 
                               reqTraitNamespace,
                               reqTraitName,
                               values[1],
                               endPt);

            refValues.segments[0] = new LeafMotionSegment(startPt[0],
                                                          startPt[1],
                                                          endPt[0],
                                                          endPt[1],
                                                          motion);
            
            for (int i = 1; i < nSegments; i++) {
                validateCoordinate(anim,
                                   reqTraitNamespace,
                                   reqTraitNamespace,
                                   values[i + 1],
                                   endPt);
                refValues.segments[i] = 
                    new LeafMotionSegment
                    ((LeafMotionSegment) refValues.segments[i - 1],
                     endPt[0],
                     endPt[1],
                     motion);
            }
        } else {
            //
            // We are dealing with the path attribute on animateMotion
            // or with the d attribute on a path element referenced from
            // an mpath element.
            //

            // a) convert the path attribute to a Path
            Path path = anim.parsePathTrait(anim.traitName, values[0]);

            // b) turn the path into a set of segments after linear 
            //    approximation.
            int type = 0;
            float[] coords = new float[6];
            float[] curPos = new float[2];
            float[] endPos = new float[2];
            float[][] curve = new float[4][2];
            float[] lastMove = new float[2];

            Vector segments = new Vector();
            Vector curves = new Vector();
            LeafMotionSegment prevSegment = null;
            
            int nSegments = path.getNumberOfSegments();
            for (int i = 0; i < nSegments; i++) {
                type = path.getSegment(i);
                switch (type) {
                case Path.MOVE_TO:
                    curPos[0] = path.getSegmentParam(i, 0);
                    curPos[1] = path.getSegmentParam(i, 1);
                    lastMove[0] = curPos[0];
                    lastMove[1] = curPos[1];
                    break;
                case Path.LINE_TO:
                    if (prevSegment == null) {
                        prevSegment = new LeafMotionSegment(
                                curPos[0],
                                curPos[1],
                                path.getSegmentParam(i, 0),
                                path.getSegmentParam(i, 1),
                                motion);
                    } else {
                        prevSegment = new LeafMotionSegment(
                                prevSegment,
                                path.getSegmentParam(i, 0),
                                path.getSegmentParam(i, 1),
                                motion);
                    }
                    segments.addElement(prevSegment);
                    curPos[0] = path.getSegmentParam(i, 0);
                    curPos[1] = path.getSegmentParam(i, 1);
                    break;
                case Path.QUAD_TO:
                    // First, linearize the curve.
                    // This is an overkill because we use the same code for
                    // quad curves as for cubic curves.
                    curve[0][0] = curPos[0];
                    curve[0][1] = curPos[1];
                    curve[1][0] = path.getSegmentParam(i, 0);
                    curve[1][1] = path.getSegmentParam(i, 1);
                    curve[2][0] = path.getSegmentParam(i, 0);
                    curve[2][1] = path.getSegmentParam(i, 1);
                    curve[3][0] = path.getSegmentParam(i, 2);
                    curve[3][1] = path.getSegmentParam(i, 3);
                    prevSegment = addCubicSegment(motion, curve, segments, 
                                                  curves, prevSegment);
                    curPos[0] = path.getSegmentParam(i, 2);
                    curPos[1] = path.getSegmentParam(i, 3);
                    break;
                case Path.CURVE_TO:
                    curve[0][0] = curPos[0];
                    curve[0][1] = curPos[1];
                    curve[1][0] = path.getSegmentParam(i, 0);
                    curve[1][1] = path.getSegmentParam(i, 1);
                    curve[2][0] = path.getSegmentParam(i, 2);
                    curve[2][1] = path.getSegmentParam(i, 3);
                    curve[3][0] = path.getSegmentParam(i, 4);
                    curve[3][1] = path.getSegmentParam(i, 5);
                    prevSegment = addCubicSegment(motion, curve, segments, 
                                                  curves, prevSegment);
                    curPos[0] = path.getSegmentParam(i, 4);
                    curPos[1] = path.getSegmentParam(i, 5);
                    break;
                case Path.CLOSE:
                default:
                    if (prevSegment == null) {
                        prevSegment = new LeafMotionSegment(curPos[0],
                                                            curPos[1],
                                                            lastMove[0],
                                                            lastMove[1],
                                                            motion);
                    } else {
                        prevSegment = new LeafMotionSegment(prevSegment,
                                                            lastMove[0],
                                                            lastMove[1],
                                                            motion);
                    }
                    segments.addElement(prevSegment);
                    curPos[0] = lastMove[0];
                    curPos[1] = lastMove[1];
                    break;
                }
            }

            if (segments.size() == 0) {
                // This is a degenerate case, for a path with only a moveto or 
                // empty.
                segments.addElement(new LeafMotionSegment(curPos[0],
                                                          curPos[1],
                                                          curPos[0],
                                                          curPos[1],
                                                          motion));
            } 

            refValues.segments = new MotionSegment[segments.size()];
            segments.copyInto(refValues.segments);
        }

        return refValues;
    }

    /**
     * Adds the input curve as a CompositeSegment to the segments vector.
     *
     * @param motion the AnimateMotion element for which the curve is added.
     * @param curve a float array with the curve definition.
     * @param segments the vector holding all the animation segments.
     * @param motionCoords a working vector where coordinates for the various 
     *        linear approximations can be stored.
     * @param prevSegment the previous LeafMotionSegment. May be null.
     * @return the last LeafMotionSegment added to into the segments vector.
     */
    LeafMotionSegment addCubicSegment(final AnimateMotion motion,
                                      final float[][] curve,
                                      final Vector segments,
                                      final Vector motionCoords,
                                      LeafMotionSegment prevSegment) {
        motionCoords.removeAllElements();
        motionCoords.addElement(new float[] {
                                    curve[0][0], 
                                    curve[0][1]
                                }); // Adds the current position.
        AbstractAnimate.toRefSpline(curve, motionCoords);
        
        int nPoints = motionCoords.size();
        float[] curPos = (float[]) motionCoords.elementAt(0);
        LeafMotionSegment[] subSegments = new LeafMotionSegment[nPoints - 1];

        if (nPoints > 1) {
            float[] endPos = (float[]) motionCoords.elementAt(1);
            if (prevSegment == null) {
                subSegments[0] = new LeafMotionSegment(curPos[0],
                                                       curPos[1],
                                                       endPos[0],
                                                       endPos[1],
                                                       motion);
            } else {
                subSegments[0] = new LeafMotionSegment(prevSegment,
                                                       endPos[0],
                                                       endPos[1],
                                                       motion);
            }
            curPos = endPos;
            prevSegment = subSegments[0];
        }

        for (int i = 2; i < nPoints; i++) {
            float[] endPos = (float[]) motionCoords.elementAt(i);
            subSegments[i - 1] = new LeafMotionSegment(prevSegment,
                                                       endPos[0],
                                                       endPos[1],
                                                       motion);
            curPos = endPos;
            prevSegment = subSegments[i - 1];
        }
        
        CompositeMotionSegment cSeg = new CompositeMotionSegment();
        cSeg.segments = subSegments;
        segments.addElement(cSeg);
        return prevSegment;
    }

    /**
     * Parses a coordinate pair.
     *
     * @throws DOMException with error code INVALID_ACCESS_ERR if the input
     * value is incompatible with the given trait.
     */
    public void validateCoordinate(final Animation anim,
                                   final String traitNamespace,
                                   final String traitName,
                                   final String value,
                                   final float[] pt) throws DOMException {
        float[] v = anim.parseFloatArrayTrait(traitName, value);
        if (v.length < 1 || v.length > 2) {
            throw anim.illegalTraitValue(traitName, value);
        }
        
        // x
        pt[0] = v[0];
        
        // translate y
        if (v.length > 1) {
            pt[1] = v[1];
        } else {
            pt[1] = v[0];
        }
    }
}
