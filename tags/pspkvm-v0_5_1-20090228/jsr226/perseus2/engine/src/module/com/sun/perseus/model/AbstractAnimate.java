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

import com.sun.perseus.util.SimpleTokenizer;
import com.sun.perseus.util.SVGConstants;

import org.w3c.dom.DOMException;

import java.util.Vector;

/**
 * <code>AbstractAnimate</code> is used as a base class for various
 * animation classes (see <code>Animate</code> and <code>AnimateMotion</code>).
 *
 * @version $Id: AbstractAnimate.java,v 1.5 2006/06/29 10:47:28 ln156897 Exp $
 */
public abstract class AbstractAnimate extends Animation {
    /**
     * calcMode value for linear interpolation.
     */
    public static final int CALC_MODE_LINEAR = 1;

    /**
     * calcMode value for discrete interpolation
     */
    public static final int CALC_MODE_DISCRETE = 2;

    /**
     * calcMode value for paced interpolation.
     */
    public static final int CALC_MODE_PACED = 3;

    /**
     * calcMode for spline interpolation.
     */
    public static final int CALC_MODE_SPLINE = 4;

    /**
     * Minimum required flatness for keySpline approximations by 
     * polylines.
     */
    public static final float MIN_FLATNESS_SQUARE = 0.01f * 0.01f;

    /**
     * End value specification
     */
    String to;

    /**
     * Starting value specification.
     */
    String from;

    /**
     * Intermediate value specification.
     */
    String by;

    /**
     * Complete values specification list.
     */
    String values;

    /**
     * The interpolation mode, one of the CALC_MODE_XYZ values.
     */
    int calcMode = CALC_MODE_LINEAR;

    /**
     * The actual calcMode. For types which do not support interpolation,
     * the calcMode is forced to be discrete.
     */
    int actualCalcMode = CALC_MODE_DISCRETE;

    /**
     * Key times, to control the animation pace. May be null or a list of
     * values between 0 and 1.
     * @see <a
     * href="http://www.w3.org/TR/smil20/smil-timing.html#animation-adef-keyTimes>SMIL
     * 2.0 Specification</a>
     */
    float[] keyTimes;

    /**
     * Key splines, used to control the speed of animation on a particular
     * time interval (interval pacing).
     *
     * May be null or an array of arrays of 4 floating point values.
     * @see <a
     * href="http://www.w3.org/TR/smil20/smil-timing.html#animation-animationNS-InterpolationKeysplines"/>SMIL
     * 2.0 Specification</a>
     */
    float[][] keySplines;

    /**
     * refSplines is an array of points which define a linear approximation of
     * the keySplines. refSplines is computed in the validate() method and 
     * used in the curve() method. There is one float array per keySpline.
     */
    float[][][] refSplines;

    /**
     * Controls whether the animation is additive or not. True maps to the 
     * SVG 'sum' value and false maps to the SVG 'replace' value.
     */
    boolean additive;

    /**
     * Controls whether the animation has a cumulative behavior or not. This
     * covers the behavior over multiple iterations of the simple duration.
     */
    boolean accumulate;

    /**
     * The RefValues corresponding to this &lt;set&gt; element. A &lt;set&gt;
     * element has a single segment with the same begin and end value.
     */
    RefValues refValues = null;
 
    /**
     * Used, temporarily, to hold the refValues for the to attribute.
     */
    RefValues toRefValues;

    /**
     * Used, temporarily, to hold the refValues for the from attribute.
     */
    RefValues fromRefValues;

    /**
     * Used, temporarily, to hold the refValues for the by attribute.
     */
    RefValues byRefValues;

    /**
     * Used, temporarily, to hold the refValues for the values attribute.
     */
    RefValues valuesRefValues;

    /**
     * refTimes holds key times for the animation.
     *
     * refTimes is computed in the validate method.
     *
     * refTimes holds the key times for the <em>begining</em> of each segment.
     * Therefore, there is as many refTimes as there are segments, in all cases.
     *
     * @see #validate
     */
    float[] refTimes;

    /**
     * A working buffer for mapping segment index and progress
     */
    float[] sisp = {0, 0};

    /**
     * Simple flag to check if we are dealing with a to-animation or
     * not. This flag is needed to control the addition and cumulative
     * behavior on to-animations, which is different than that on other
     * animations (e.g., a values or a from-to animation).
     */
    boolean isToAnimation;

    /**
     * Builds a new Animate element that belongs to the given
     * document. This <code>Animate</code> will belong 
     * to the <code>DocumentNode</code>'s time container.
     *
     * @param ownerDocument the document this node belongs to.
     * @param localName the animation element's local name.
     * @throws IllegalArgumentException if the input ownerDocument is null
     */
    public AbstractAnimate(final DocumentNode ownerDocument,
                           final String localName) {
        super(ownerDocument, localName);
    }
    
    /**
     * This is the Animate element's animation function. 
     *
     * f(t) {
     *     a. Compute the 'simple duration penetration' p
     *       p = t / dur
     *       where dur is the simple duration.
     *
     *     b. Compute the 'current time segment' i
     *        refTimes[i] <= p < refTimes[i+1]
     *
     *     c. Compute the 'segment penetration' sp
     *        sp = (p - refTimes[i]) 
     *             / 
     *             (refTimes[i+1] - refTimes[i])
     *        Note: 0 <= sp <= 1
     *
     *     d. Compute the 'interpolated interval 
     *        penetration'  isp
     *
     *        isp = calcMode(sp)
     *        Note: 0 <= isp <= 1
     *
     *     e. Compute the animated value:
     *        v = refValues.compute(isp)
     *        v has the same number of components as refValues.
     * }
     *
     * @param t the animation's simple time.
     */
    Object[] f(final long t) {
        // a. Compute the simple duration penetration p

        // See: http://www.w3.org/TR/smil20/smil-timing.html#animation-animationNS-InterpolationAndIndefSimpleDur
        float p = 0;
        if (timedElementSupport.simpleDur != null 
            && 
            timedElementSupport.simpleDur.isResolved()) {
            p = t / (float) timedElementSupport.simpleDur.value;
        }

        // Iterate for each component.
        int nc = refValues.getComponents();
        float sp = 0;
        int si = 0;
        float endTime = 1;
        float beginTime = 0;
        int i = 0;

        // b. Compute the 'current time segment' index si[ci] 
            
        // We iterate from 1 because the first value in refTimes
        // is always 0. 
        for (i = 1; i < refTimes.length; i++) {
            if (p < refTimes[i]) {
                endTime = refTimes[i];
                break;
            }
            beginTime = refTimes[i];
        }

        si = i - 1;

        // c. Compute the segment penetration
        if (endTime == beginTime) {
            sp = 1;
        } else {
            sp = (p - beginTime) / (endTime - beginTime);
        }
        
        // d. Compute the 'interpolated segment penetration'
        sp = calcMode(sp, si);

        // At this point, we have computed:
        // a. the array of time segment indices corresponding to 't'
        // b. the array of penetration into the time segments. 
        //
        // The following call lets the animate implementation map 
        // the time segment indices and the time segment penetration
        // into refValues indices and penetration, in case these are
        // different. Typically, these are the same, but they may be
        // different, for example in the case of animateMotion with
        // keyPoints.
        sisp[0] = si;
        sisp[1] = sp;
        mapToSegmentProgress(si, sp, sisp);
        si = (int) sisp[0];
        sp = sisp[1];

        // If we are dealing with a to-animation, we need to get the
        // value for the single segment's start value.
        if (isToAnimation) {
            Object[] baseValue = baseVal.getBaseValue();
            refValues.getSegment(0).setStart(baseValue);
            return refValues.compute(si, sp);
        }

        // The reminder of this method is for non-additive animations
        
        // Compute the simple function value
        Object[] v = refValues.compute(si, sp);

        // Account for cumulative behavior.
        if (!isToAnimation && accumulate && timedElementSupport.curIter > 0) {
            // Last simple time value, lv
            Object[] lv = 
                refValues.getSegment(refValues.getSegments() - 1).getEnd();
            Object[] r = traitAnim.multiply(lv, timedElementSupport.curIter);
            v = traitAnim.sum(r, v);
        }

        // Now, account for additive behavior.
        if (!additive) {
            return v;
        } 

        return traitAnim.sum(baseVal.getBaseValue(), v);
    }

    /**
     * The following call lets the animate implementation map 
     * the time segment indices and the time segment penetration
     * into refValues indices and penetration, in case these are
     * different. Typically, these are the same, but they may be
     * different, for example in the case of animateMotion with
     * keyPoints.
     */
    protected void mapToSegmentProgress(final int si, 
                                        final float sp, 
                                        final float[] sisp) {
    }

    /**
     * Intepolates the input value depending on the calcMode attribute
     * and, in the case of spline interpolation, depending on the
     * keySplines value.
     *
     * @param p the value to interpolate. Should be in the [0, 1] range.
     * @param si the time segment from which the computation is done. This
     *        is needed to identify the correct keySplines to use in 
     *        case of spline animations.
     */
    float calcMode(final float p, final int si) {
        switch (actualCalcMode) {
        case CALC_MODE_DISCRETE:
            return 0;
        case CALC_MODE_LINEAR:
            // Linear does not modify the current penetration
            // because we are on an x = y line.
            case CALC_MODE_PACED:
            // Paced does not modify the current penetration
            // because we have computed the time intervals 
            // so as to have constant velocity. There is no
            // further interpolation in this method.
            return p;
        case CALC_MODE_SPLINE:
        default:
            return curve(p, refSplines[si]);
        }
    }

    /**
     * Computes an array of points which do a linear approximation of the 
     * input splines.
     *
     * @param splines the array of splines to approximate with a polyline.
     *        The splines array should be an made of four element floats.
     *        if any of the elements is null, a NullPointerException will
     *        be thrown. If any of the element has a length less than four,
     *        an ArrayIndexOutOfBoundsException will be thrown.
     * @return an array of point arrays. Each point array is a polyline
     *         approximation of the input spline.
     */
    static float[][][] toRefSplines(final float[][] splines) {
        float[][][] rSplines = new float[splines.length][][];

        float[][] splineDef = new float[4][2];
        for (int i = 0; i < splines.length; i++) {
            Vector v = new Vector();
            v.addElement(new float[] {0, 0});
            splineDef[0][0] = 0;
            splineDef[0][1] = 0;
            splineDef[1][0] = splines[i][0];
            splineDef[1][1] = splines[i][1];
            splineDef[2][0] = splines[i][2];
            splineDef[2][1] = splines[i][3];
            splineDef[3][0] = 1;
            splineDef[3][1] = 1;
            toRefSpline(splineDef, v);
            float[][] polyline = new float[v.size()][];
            v.copyInto(polyline);
            rSplines[i] = polyline;
        }

        return rSplines;
    }

    /**
     * Converts the input spline curve (defined by its four control points)
     * into a polyline approximation (i.e., an array of points). If the curve
     * is flat enough (see the <code>isFlat</code> method), then the curve is
     * approximated to a line between its two end control points and the
     * last control point is added to the input <code>segment</code> vector. 
     * If the curve is not flat enough, then it is sub-divided and the 
     * subdivided curves are, recursively, tested for flatness.
     *
     * The flatness used for flatness test is controlled by the 
     * <code>MIN_FLATNESS_SQUARE</code> constant.
     * 
     * @param curve the spline curve to approximate. Should not be null. Should
     *        be an array of four arrays of two elements. If there are less than
     *        four arrays or if there are less than two elements in these arrays
     *        then an ArrayIndexOutOfBoundsException is thrown. If any element
     *        in the curve array is null, a NullPointerException is thrown.
     * @param segments the vector to which polyline points should be added. 
     *        Should not be null.
     */
    static void toRefSpline(final float[][] curve, final Vector segments) {
        if (isFlat(curve, MIN_FLATNESS_SQUARE)) {
            segments.addElement(new float[] {curve[3][0], curve[3][1]});
            return;
        }

        float[][] lcurve = new float[4][2];
        float[][] rcurve = new float[4][2];

        // L1 = P1
        lcurve[0] = curve[0];

        // L2 = (P1 + P2) / 2
        lcurve[1][0] = (curve[0][0] + curve[1][0]) / 2;
        lcurve[1][1] = (curve[0][1] + curve[1][1]) / 2;

        // H = (P2 + P3) / 2
        float[] h = new float[2];
        h[0] = (curve[1][0] + curve[2][0]) / 2;
        h[1] = (curve[1][1] + curve[2][1]) / 2;

        // L3 = (L2 + H) / 2
        lcurve[2][0] = (lcurve[1][0] + h[0]) / 2;
        lcurve[2][1] = (lcurve[1][1] + h[1]) / 2;

        // R3 = (P3 + P4) / 2
        rcurve[2][0] = (curve[2][0] + curve[3][0]) / 2;
        rcurve[2][1] = (curve[2][1] + curve[3][1]) / 2;

        // R2 = (H + R3) / 2
        rcurve[1][0] = (h[0] + rcurve[2][0]) / 2;
        rcurve[1][1] = (h[1] + rcurve[2][1]) / 2;

        // L4 = R1 = (L3 + R2) / 2
        lcurve[3][0] = (lcurve[2][0] + rcurve[1][0]) / 2;
        lcurve[3][1] = (lcurve[2][1] + rcurve[1][1]) / 2;
        rcurve[0] = lcurve[3];

        // R4 = P4
        rcurve[3] = curve[3];

        toRefSpline(lcurve, segments);
        toRefSpline(rcurve, segments);
    }

    /**
     * @param curve the spline curve to test for flatness. The curve is 
     *        considered flat if the distance from the two intermediate control
     *        points from the line between the first and the last control points
     *        is less than the desired flatness maximum. The input array must 
     *        have at least four elements and each one must be at least 
     *        2 elements long. If not, an ArrayOutOfBoundsException is thrown. 
     *        The curve array should not be null.
     * @param flatness. The maximum distance allowed for the intermediate curve
     *        control points.
     */
    static boolean isFlat(float[][] curve, float flatness) {
        // Compute the square distance of P2 and P3 to the (P1, P4) line.
        float dx = curve[3][0] - curve[0][0];
        float dy = curve[3][1] - curve[0][1];
        float div = dx * dx + dy * dy;
        
        if (div == 0) {
            return true;
        }
        
        float den = (dx * (curve[1][1] - curve[0][1])
                     -
                     dy * (curve[1][0] - curve[0][0]));

        float dP2Sq =  (den * den) / div;

        den = (dx * (curve[2][1] - curve[0][1])
               -
               dy * (curve[2][0] - curve[0][0]));

        float dP3Sq =  (den * den) / div;

        return (dP2Sq <= flatness && dP3Sq <= flatness);
        
    }

    /**
     * Computes the curve value for the requested value on the specified
     * curves, defined by a polyline approximation.
     *
     * The method assumes that the input polyline points are between 0 and 1 and
     * in increasing order along the x axis. The method considers the p value to
     * be on the polyline's x-axis and finds the two points between which it 
     * lies and returns a linear approximation for that segment.
     *
     * For degenerate cases (e.g., if the polyline x-axis values are not in the
     * [0, 1] interval or if p is not in the [0, 1] interval either), the method
     * returns 0 if the input penetration p is less than the first polyline 
     * point. The method returns 1 if the input penetration is more than the 
     * last polyline x-axis coordinate.
     *
     * @param p the value for which the curve polynomial should be computed.
     * @param polyline the polyline curve approximation. Should not be null.
     *        each element in the polyline array should not be null. Each
     *        element in the array should be at least of length 2.
     */
    static float curve(final float p,
                       final float[][] polyline) {
        int i = 0;
        for (; i < polyline.length; i++) {
            if (p < polyline[i][0]) {
                break;
            }
        }

        if (i == polyline.length || i == 0) {
            // Degenerate cases.
            //
            // This should never happen:
            //
            // Should not be polyline.length because the last entry in polyline
            // should be '1' and the maximum input value for p should be '1'.
            //
            // Should not get 0 because the first entry should be zero and the
            // input parameter should be greater or equal to zero.
            // 
            return i / (float) polyline.length; // returns 0 for 0 and 1 for 1
        }

        float[] from = polyline[i - 1];
        float[] to   = polyline[i];

        // Compute the progress between from and to:
        //
        // p = (1 - t) * from[0] + t.to[0]
        // p = t * (t[0] - from[0]) + from[0]
        // t = (p - from[0]) / (to[0] - from[0])
        //
        // So, the interpolated progress is:
        //
        // ip = (1 - t) * from[1] + t.to[1]
        // ip = (1 - (p - from[0]) / (to[0] - from[0])) * from[1] 
        //      + ((p - from[0]) / (to[0] - from[0])) * to[1]
        // ip = ((to[0] - from[0] - p + from[0]) / (to[0] - from[0])) * from[1]
        //      + (to[1] * p - to[1] * from[0]) / (to[0] - from[0])
        // ip = ((to[0] - p) / (to[0] - from[0]) * from[1] 
        //      + (to[1] * p - to[1] * from[0]) / (to[0] - from[0])
        // ip = (to[0] * from[1] - p * from[1]) / (to[0] - from[0]) 
        //      + (to[1] * p - to[1] * from[0]) / (to[0] - from[0])
        // ip = (to[0] * from[1] - p * from[1] + to[1] * p - to[1] * from[0]) 
        //      / (to[0] - from[0])
        // ip = (to[0] * from[1] - to[1] * from[0] + p * (to[1] - from[1])) 
        //      / (to[0] - from[0])
        return (to[0] * from[1] - to[1] * from[0] + p * (to[1] - from[1])) 
                / (to[0] - from[0]);
    }
     

    /**
     * Supported traits: to, attributeName
     *
     * @param traitName the name of the trait which the element may support.
     * @return true if this element supports the given trait in one of the
     *         trait accessor methods.
     */
    boolean supportsTrait(final String traitName) {
        if (SVGConstants.SVG_TO_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_FROM_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_BY_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_VALUES_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_CALC_MODE_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_KEY_TIMES_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_KEY_SPLINES_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_ADDITIVE_ATTRIBUTE == traitName
            ||
            SVGConstants.SVG_ACCUMULATE_ATTRIBUTE == traitName) {
            return true;
        } else {
            return super.supportsTrait(traitName);
        }
    }

    // JAVADOC COMMENT ELIDED
    public String getTraitImpl(final String name)
        throws DOMException {
        if (SVGConstants.SVG_TO_ATTRIBUTE == name) {
            return to;
        } else if (SVGConstants.SVG_FROM_ATTRIBUTE == name) {
            return from;
        } else if (SVGConstants.SVG_BY_ATTRIBUTE == name) {
            return by;
        } else if (SVGConstants.SVG_VALUES_ATTRIBUTE == name) {
            return values;
        } else if (SVGConstants.SVG_CALC_MODE_ATTRIBUTE == name) {
            switch (calcMode) {
            case CALC_MODE_DISCRETE:
                return SVGConstants.SVG_DISCRETE_VALUE;
            case CALC_MODE_LINEAR:
                return SVGConstants.SVG_LINEAR_VALUE;
            case CALC_MODE_PACED:
                return SVGConstants.SVG_PACED_VALUE;
            case CALC_MODE_SPLINE:
            default:
                return SVGConstants.SVG_SPLINE_VALUE;
            }
        } else if (SVGConstants.SVG_KEY_TIMES_ATTRIBUTE == name) {
            return toStringTrait(keyTimes);
        } else if (SVGConstants.SVG_KEY_SPLINES_ATTRIBUTE == name) {
            return toStringTrait(keySplines);
        } else if (SVGConstants.SVG_ADDITIVE_ATTRIBUTE == name) {
            if (additive) {
                return SVGConstants.SVG_SUM_VALUE;
            }
            return SVGConstants.SVG_REPLACE_VALUE;
        } else if (SVGConstants.SVG_ACCUMULATE_ATTRIBUTE == name) {
            if (accumulate) {
                return SVGConstants.SVG_SUM_VALUE;
            }
            return SVGConstants.SVG_NONE_VALUE;
        } else {
            return super.getTraitImpl(name);
        }
    }

    // JAVADOC COMMENT ELIDED
    public void setTraitImpl(final String name, 
                             final String value)
        throws DOMException {
        if (SVGConstants.SVG_TO_ATTRIBUTE == name) {
            checkWriteLoading(name);
            to = value;
        } else if (SVGConstants.SVG_FROM_ATTRIBUTE == name) {
            checkWriteLoading(name);
            from = value;
        } else if (SVGConstants.SVG_BY_ATTRIBUTE == name) {
            checkWriteLoading(name);
            by = value;
        } else if (SVGConstants.SVG_VALUES_ATTRIBUTE == name) {
            checkWriteLoading(name);
            values = value;
        } else if (SVGConstants.SVG_KEY_TIMES_ATTRIBUTE == name) {
            checkWriteLoading(name);

            // Generic float array parsing
            keyTimes = parseFloatArrayTrait(name, value.replace(';', ','));

            // Now, check that the keyTimes values are from 0 to 1 and
            // in increasing order. Note that this only performs the 
            // validations which can be made prior to a call to validate.
            if (keyTimes[0] < 0 || keyTimes[0] > 1) {
                throw illegalTraitValue(name, value);
            }

            for (int i = 1; i < keyTimes.length; i++) {
                if (keyTimes[i] < 0 || keyTimes[i] > 1) {
                    throw illegalTraitValue(name, value);
                }

                if (keyTimes[i] < keyTimes[i - 1]) {
                    throw illegalTraitValue(name, value);
                }
            }
        } else if (SVGConstants.SVG_KEY_SPLINES_ATTRIBUTE == name) {
            checkWriteLoading(name);

            SimpleTokenizer st = new SimpleTokenizer(value, ";");
            int n = st.countTokens();
            keySplines = new float[n][];
            
            for (int i = 0; i < n; i++) {
                String splineDef = st.nextToken();
                keySplines[i] = parseFloatArrayTrait(name, splineDef);
                if (keySplines[i].length != 4 ||
                    keySplines[i][0] < 0 ||
                    keySplines[i][0] > 1 ||
                    keySplines[i][1] < 0 ||
                    keySplines[i][1] > 1 ||
                    keySplines[i][2] < 0 ||
                    keySplines[i][2] > 1 ||
                    keySplines[i][3] < 0 ||
                    keySplines[i][3] > 1) {
                    throw illegalTraitValue(name, value);
                }
            }
        } else if (SVGConstants.SVG_ADDITIVE_ATTRIBUTE == name) {
            checkWriteLoading(name);

            if (SVGConstants.SVG_REPLACE_VALUE.equals(value)) {
                additive = false;
            } else if (SVGConstants.SVG_SUM_VALUE.equals(value)) {
                additive = true;
            } else {
                throw illegalTraitValue(name, value);
            }
        } else if (SVGConstants.SVG_ACCUMULATE_ATTRIBUTE == name) {
            checkWriteLoading(name);

            if (SVGConstants.SVG_NONE_VALUE.equals(value)) {
                accumulate = false;
            } else if (SVGConstants.SVG_SUM_VALUE.equals(value)) {
                accumulate = true;
            } else {
                throw illegalTraitValue(name, value);
            }
        } else if (SVGConstants.SVG_CALC_MODE_ATTRIBUTE == name) {
            if (SVGConstants.SVG_DISCRETE_VALUE.equals(value)) {
                calcMode = CALC_MODE_DISCRETE;
            } else if (SVGConstants.SVG_LINEAR_VALUE.equals(value)) {
                calcMode = CALC_MODE_LINEAR;
            } else if (SVGConstants.SVG_PACED_VALUE.equals(value)) {
                calcMode = CALC_MODE_PACED;
            } else if (SVGConstants.SVG_SPLINE_VALUE.equals(value)) {
                calcMode = CALC_MODE_SPLINE;
            } else {
                throw illegalTraitValue(name, value);
            }
        } else {
            super.setTraitImpl(name, value);
        }
    }

    /**
     * Computes refTimes so that each animation segment lasts the same length
     * of time.
     */
    float[] getDefaultTiming(RefValues refValues) {
        int ns = refValues.getSegments();
        float[] refTimes = new float[ns];
        float startTime = 0;
        float segLength = 1 / (float) ns;
        for (int i = 0; i < ns; i++) {
            refTimes[i] = startTime;
            startTime += segLength;
        }

        return refTimes;
    }

    /**
     * Computes refTimes so that there is a paced speed on each values segment.
     */
    float[] getPacedTiming(RefValues refValues) {
        // a) Compute the refValues length, D
        //    D = sum(from 0 to n; seg(i).length()); 
        //
        // b) Compute the overall paced velocity, V
        //    V = D / dur; 
        //
        // c) For each segment i, compute its refTime:
        //    refTime[j][i] = refTime[j][i-1] + (seg(i).length(j) / V(j)) / dur;
        
        float D = refValues.getLength();

        int ns = refValues.getSegments();
        refTimes = new float[ns];
        float prevRefTime = 0;

        if (D > 0) {
            // For each segment index si
            for (int si = 1; si < ns; si++) {
                refTimes[si] = prevRefTime + refValues.getLength(si -1) / D;
                prevRefTime = refTimes[si];
            }
        } else {
            // Segments are all of zero length, they should be evenly spaced
            // in time.
            float sl = 1f / ns;
            for (int si = 1; si < ns; si++) {
                refTimes[si] = si * sl;
            }
        }

        return refTimes;
    }

    /**
     * Validating an Animate consists in:
     * a) Setting its target element. If there was no idRef, then targetElement
     *    is still null and will be positioned to the parent node.
     * 
     * b) Validating the from, to, by and values traits with the targetElement,
     *    using the target trait name, namespace and value.
     *
     * c) Validating the keyTimes and the keySplines trait values to check they
     *    are compatible with the values specification.
     *
     * @throws DOMException if there is a validation error, for example if the
     *         to value is incompatible with the target trait or if the target
     *         trait is not animatable.
     */
    void validate() throws DOMException {
        // =====================================================================
        // a) Set the target element.
        if (targetElement == null) {
            targetElement = (ElementNode) parent;
        }
        
        // =====================================================================
        // Check that the traitName attribute was specified.
        if (traitName == null) {
            throw illegalTraitValue(SVGConstants.SVG_ATTRIBUTE_NAME_ATTRIBUTE, 
                                    traitName);
        }

        // =====================================================================
        // b) Validate the to/from/by/values traits with the target element.  

        // Note that traitName should _never_ be null when validate() is 
        // invoked. It is either required (e.g., for Animate) or fixed (e.g., 
        // for AnimateMotion. If, for example in some unit test configutations,
        // this method is called without a specified traitName, the method 
        // generates a NullPointerException.
        traitAnim = targetElement.getSafeTraitAnimNS(traitNamespace, traitName);

        // If the traitAnim type does not support interpolation, force the 
        // calcMode to be discrete.
        if (traitAnim.supportsInterpolation()) {
            actualCalcMode = calcMode;
        } else {
            actualCalcMode = CALC_MODE_DISCRETE;
        }

        validateValues();

        // Now, apply precedence rules to compute the actual RefValues.
        selectRefValues();

        // If the animation has no effect, stop here.
        if (hasNoEffect) {
            return;
        }

        // =====================================================================
        // If we are dealing with discrete timing, we need to add a last time
        // segment so that we implement the desired behavior for discrete 
        // timing. With a single interval with start/end values, only the start
        // value will be shown. If we add a new interval with the end value, 
        // then the end value will be shown for the last time interval.
        if (actualCalcMode == CALC_MODE_DISCRETE) {
            refValues.makeDiscrete();
        }

        // Initialize the refValues
        refValues.initialize();

        // =====================================================================
        // c. Validate that keyTimes and keySplines trait values are compatible
        //    with the values specification.
        computeRefTimes();

        // c.1 Validate that keySplines is compatible with the animation set-up
        //
        // See:
        // http://www.w3.org/TR/2001/REC-smil20-20010807/smil20.html#animation-animationNS-InterpolationKeysplines
        //
        // The attribute is ignored unless the CALC_MODE is spline. For spline
        // interpolation, there must be one fewer sets of control points in the
        // keySplines attribute than there are keyTimes.
        //
        if (actualCalcMode == CALC_MODE_SPLINE) {
            if (keySplines == null 
                ||
                refTimes.length != keySplines.length) {
                throw animationError(idRef,
                        traitNamespace,
                        traitName,
                        targetElement.getNamespaceURI(),
                        targetElement.getLocalName(),
                        getId(),
                        getNamespaceURI(),
                        getLocalName(),
                        Messages.formatMessage(
                            Messages.ERROR_INVALID_ANIMATION_KEY_SPLINES, 
                            new Object[] {
                                getTrait(
                                    SVGConstants.SVG_KEY_SPLINES_ATTRIBUTE),
                                getTrait(SVGConstants.SVG_KEY_TIMES_ATTRIBUTE)
                            }));
            }

            // Turn the keySplines attribute into a set of (x, y) points
            // which can be interpolated between.
            refSplines = toRefSplines(keySplines);
        }
    }

    /**
     * Computes refTimes from the calcMode and keyTimes attributes. Validates
     * that the keyTimes attribute is compatible with the animate set up. This
     * may be overridden by subclasses (e.g., animateMotion), when there are 
     * special rules for checking keyTimes compatiblity.
     */
    protected void computeRefTimes() throws DOMException {
        // c.1 Validate that keyTimes is compatible with the animation set-up
        //
        // if (calcMode == paced)
        //    refTimes such that change velocity is constant
        //    refTimes = getPacedTiming(refValues) // see follow on slides
        //
        // else if (keyTimes defined) 
        //    refTimes = keyTimes
        //    if (calcMode == discreet)
        //        refTimes[n] = 1 // Accounts for the added nth value 
        //                        // in refValues
        //
        // else refTimes = f(refValues)
        //     the simple duration is divided into n-1 intervals, 
        //     where n = refValues.length
        //     refTimes = defaultTiming(refValues)
        if (actualCalcMode == CALC_MODE_PACED) {
            // keyTimes are ignored
            // See: http://www.w3.org/TR/smil20/smil-timing.html#animation-adef-keyTimes
            refTimes = getPacedTiming(refValues);
        } else if (keyTimes != null) {
            // Check keyTimes is compatible with the animation specification.
            // 
            // a) In all cases, the first keyTime must be zero.
            // 
            // b) For non-discrete animations,
            //    b.1) the last keyTime must be one.
            //    b.2) there should be one more keyTimes than there are 
            //         segments.
            // 
            // c) For discrete animations,
            //    c.1) there should be as many keyTimes as there are segments.
            //
            if (/* a) */ keyTimes.length < 1 || keyTimes[0] != 0
                ||
                /* b) */ (actualCalcMode != CALC_MODE_DISCRETE 
                          && 
                          (/* b.1) */ keyTimes[keyTimes.length - 1] != 1
                          ||
                           /* b.2) */ keyTimes.length != 
                                            refValues.getSegments() + 1))
                ||
                /* c) */ (actualCalcMode == CALC_MODE_DISCRETE
                          &&
                          /* c.1) */ keyTimes.length != 
                                            refValues.getSegments())) {
                throw animationError(idRef, 
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


            // Validate that there are as many refTimes as there are refValues.
            // We do the check using the first component, as we know there is at
            // least one component no matter what value type we are dealing 
            // with.
            if (refTimes.length != refValues.getSegments()) {
                throw animationError(idRef, 
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
        } else {
            // Simple case, give the same time length to each segment.
            refTimes = getDefaultTiming(refValues);
        }

    }

    /**
     * Validates the different so-called values attributes, such as the 
     * to/from/by and values attributes. Derived classes may have more 'values'
     * attributes, and may override this method to validate these additional 
     * attributes.
     *
     * @throws DOMException if there is a validation error, for example if the
     *         to value is incompatible with the target trait or if the target
     *         trait is not animatable.
     */
    final void validateValues() throws DOMException {
        // b.1) Validate the to traits value
        toRefValues = null;
        if (to != null) {
            toRefValues = traitAnim.toRefValues(this,
                                                new String[] {to},
                                                null,
                                                SVGConstants.SVG_TO_ATTRIBUTE);
        }

        // b.2) Validate the by traits value
        byRefValues = null;
        if (by != null) {
            byRefValues = traitAnim.toRefValues(this,
                                                new String[] {by},
                                                null,
                                                SVGConstants.SVG_BY_ATTRIBUTE);
        }

        // b.3) Validate the from traits value
        fromRefValues = null;
        if (from != null) {
            fromRefValues = traitAnim.toRefValues(this, 
                    new String[] {from},
                    null,
                    SVGConstants.SVG_FROM_ATTRIBUTE);
        }

        // b.4) Validate the values trait value
        valuesRefValues = null;
        if (values != null) {
            String[] v = parseStringArrayTrait(
                    SVGConstants.SVG_VALUES_ATTRIBUTE,
                    values,
                    ";");
            valuesRefValues = traitAnim.toRefValues(this, 
                    v,
                    null,
                    SVGConstants.SVG_VALUES_ATTRIBUTE);
        }

        validateValuesExtra();
    }

    /**
     * Allows extension classes to validate addition values sources.
     *
     * @throws DOMException if there is a validation error, for example if the
     *         to value is incompatible with the target trait or if the target
     *         trait is not animatable.
     */
    void validateValuesExtra() throws DOMException {
    }
    
    /**
     * Allows extensions to select a different source for refValues, in case
     * the extension has addition values sources that have higher precedence
     * than the default. For example, animateMotion has the path attribute and
     * the mpath children which have higher precedence.
     *
     * @throws DOMException if there is no way to compute a set of reference
     *         values, for example if none of the values sources is specified.
     */
    void selectRefValuesExtra() throws DOMException {
    }

    /**
     * Computes the 'right' source for reference values, depending on the 
     * precedence rules for the different values sources. 
     *
     * @throws DOMException if there is no way to compute a set of reference
     *         values, for example if none of the values sources is specified.
     */
    final void selectRefValues() throws DOMException {
        refValues = null;
        selectRefValuesExtra();
        if (refValues != null) {
            return;
        }

        // Pseudo-code for accounting for precedence rules.
        //
        // If (values defined)
        //     // values anim
        //     refValues = values; 
        // else if (from defined)
        //     if (to defined)
        //         // from-to anim
        //         refValues = (from; to)
        //     else if (by defined)
        //         // from-by anim
        //        refValues = (from; from+by) 
        //     else
        //     // no effect, there is nothing like a from anim
        // else if (by defined)
        //     // by anim
        //     refValues = (0; by) and force additive anim
        // else if (to defined)
        //     // to anim
        //     refValues = (baseVal; to)
        // else
        //    // no effect
        //
        isToAnimation = false;
        if (values != null) {
            refValues = valuesRefValues;
        } else if (from != null) {
            refValues = fromRefValues;
            
            if (to != null) {
                // from-to animatin
                refValues.getSegment(0).collapse(toRefValues.getSegment(0), 
                        this);
            } else if (by != null) {
                // from-by animation
                if (refValues.getSegment(0).isAdditive()) {
                    try {
                        refValues.getSegment(0).addToEnd(
                                byRefValues.getSegment(0).getStart());
                    } catch (IllegalArgumentException iae) {
                        // Incompatible by value
                        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, 
                                Messages.formatMessage(
                                    Messages.ERROR_INCOMPATIBLE_FROM_BY,
                                    new Object[] {
                                        traitName,
                                        traitNamespace,
                                        getLocalName(),
                                        getNamespaceURI(),
                                        from,
                                        to
                                    }));
                    }
                } else {
                    throw new DOMException(
                            DOMException.NOT_SUPPORTED_ERR,
                            Messages.formatMessage(
                                Messages.ERROR_ATTRIBUTE_NOT_ADDITIVE_FROM_BY,
                                new Object[] {
                                    traitName,
                                    traitNamespace,
                                    getLocalName(),
                                    getNamespaceURI() 
                                }));
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
                        Messages.formatMessage(
                            Messages.ERROR_INVALID_ANIMATION_FROM_ANIM, 
                            null));
            }
        } else if (by != null) {
            // by animation
            if (!byRefValues.getSegment(0).isAdditive()) {
                throw new DOMException(
                        DOMException.NOT_SUPPORTED_ERR,
                        Messages.formatMessage(
                            Messages.ERROR_ATTRIBUTE_NOT_ADDITIVE_BY,
                            new Object[] {
                                traitName,
                                traitNamespace,
                                getLocalName(),
                                getNamespaceURI()
                            }));
            }
            refValues = byRefValues;
            refValues.getSegment(0).setZeroStart();
            additive = true;
        } else if (to != null) {
            // to animation
            // We cannot compute the base value yet so we can only set the 
            // refValues to the toRefValues and delay computing the segment's 
            // begin until we actually have a baseValue. This will have to be 
            // updated on each computing cycle because the base value may change
            // over time.
            isToAnimation = true;
            refValues = toRefValues;
        } else {
            // SMIL Animation
            // specification:
            // http://www.w3.org/TR/2001/REC-smil-animation-20010904/#AnimFuncValues
            // "similarly, if none of the from, to, by or values attributes are
            // specified, the animation will have no effect."
            hasNoEffect = true;
        }        
    }

}
