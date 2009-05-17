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
package com.sun.perseus.j2d;

import com.sun.pisces.PiscesRenderer;
import com.sun.pisces.Transform6;

import org.w3c.dom.svg.SVGRect;

/**
 * LinearGradientPaint support.
 *
 * @version $Id: LinearGradientPaintDef.java,v 1.4 2006/04/21 06:35:26 st125089 Exp $
 */
public class LinearGradientPaintDef implements PaintDef {
    public static final int CYCLE_NONE = 0;
    public static final int CYCLE_REPEAT = 1;
    public static final int CYCLE_REFLECT = 2;

    private Transform6 IDENTITY = new Transform6();
    
    /**
     * The gradient starting point along the x-axis
     */
    float x0;

    /**
     * The gradient starting point along the y-axis
     */
    float y0;

    /**
     * The gradient end point along the x-axis
     */
    float x1;

    /**
     * The gradient end point along the y-axis.
     */
    float y1;

    /**
     * The array of stop values.
     */
    float[] fractions;

    /**
     * The array of stop values, as fixed point values.
     */
    int[] frac;

    /**
     * The array of ARGB color values.
     */
    int[] rgba;

    /**
     * The last used rgba array, accounting for operation opacity.
     */
    int[] lrgba;

    /**
     * The last paintOpacity.
     */
    int lastPaintOpacity = 255;

    /**
     * One of the cycle methods (CYCLE_NONE,
     *        CYCLE_REPEAT, CYCLE_REFLECT
     */
    int cycleMethod;

    /**
     * Set to true if this gradient is in objectBoundingBox space.
     */
    protected boolean isObjectBBox = false;

    /**
     * An additional transform from the gradient space. This corresponds to a
     * SVG gradientTransform attribute.
     */
    protected Transform gradientTransform;

    /**
     * Constructs an <code>LinearGradientPaint</code>.
     *
     * @param x0 the gradient starting point along the x-axis
     * @param y0 the gradient starting point along the y-axis
     * @param x1 the gradient end point along the x-axis
     * @param y1 the gradient end point along the y-axis.
     * @param fractions the array of stop values.
     * @param rgba the array of ARGB color values
     * @param cycleMethod one of the cycle methods (CYCLE_NONE,
     *        CYCLE_REPEAT, CYCLE_REFLECT
     * @param isObjectBBox if set to true, the RenderGraphic's current 
     *        paintTarget object bounding box should be used to append
     *        and additional transform to the gradientTransform. The objectBoundingBox
     *        transform is appended to the right of the gradientTransform.
     * @param gradientTransform an additional transform to add between the
     *        device coordinate space and the gradient's coordinate space.
     */
    public LinearGradientPaintDef (final float x0,
                                   final float y0,
                                   final float x1,
                                   final float y1,
                                   final float[] fractions,
                                   final int[] rgba,
                                   final int cycleMethod,
                                   final boolean isObjectBBox,
                                   final Transform gradientTransform) {
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
        this.fractions = fractions;
        this.rgba = rgba;
        this.cycleMethod = cycleMethod;
        this.isObjectBBox = isObjectBBox;
        this.gradientTransform = gradientTransform;
    }

    /**
     * Sets the paint on a PiscesRender.
     *
     * @param rg the RenderGraphics on requesting the paint to be set.
     * @param renderer the PiscesRender on which to set the paint.
     * @param paintOpacity additional paint opacity.
     */
    public void setPaint(final PiscesRenderGraphics rg,
                         final PiscesRenderer pr,
                         final int paintOpacity) {
        // First, lazilly compute the fractions in fixed point.
        if (frac == null) {
            frac = new int[fractions.length];
            for (int i = 0; i < fractions.length; i++) {
                frac[i] = (int) (fractions[i] * 65536);
            }
        }

        // Now, lazilly compute the offset colors accounting for the current 
        // paint opacity.
        int[] c = rgba;
        if (paintOpacity != 255) {
            c = lrgba;
            if (paintOpacity != lastPaintOpacity) {
                if (lrgba == null) {
                    lrgba = new int[rgba.length];
                }
                int a = 0;
                for (int i = 0; i < rgba.length; i++) {
                    lrgba[i] = (rgba[i] & 0x00ffffff);
                    a = (paintOpacity * (0xff & (rgba[i] >> 24)) / 255);
                    lrgba[i] |= (a << 24);
                }
                lastPaintOpacity = paintOpacity;
            }
        }

        // Finally, compute the paint transform.

        // Start with the paintTarget's user space coordinate system.
        Transform txf = null;
        if (rg.paintTransform != null) {
            txf = new Transform(rg.paintTransform);
        } else {
            txf = new Transform(rg.transform.m00 / 65536f,
                                rg.transform.m10 / 65536f,
                                rg.transform.m01 / 65536f,
                                rg.transform.m11 / 65536f,
                                rg.transform.m02 / 65536f,
                                rg.transform.m12 / 65536f);
        }

        // Append the objectBoundingBox space to user space transform.
        if (isObjectBBox) {
            SVGRect bbox = rg.paintTarget.getBBox();
            txf.mTranslate(bbox.getX(), bbox.getY());
            txf.mScale(bbox.getWidth(), bbox.getHeight());
        } 

        // Now, append the gradient transform.
        if (gradientTransform != null) {
            txf.mMultiply(gradientTransform);
        }
        
        Transform6 t = new Transform6();
        t.m00 = (int) (txf.m0 * 65536);
        t.m10 = (int) (txf.m1 * 65536);
        t.m01 = (int) (txf.m2 * 65536);
        t.m11 = (int) (txf.m3 * 65536);
        t.m02 = (int) (txf.m4 * 65536);
        t.m12 = (int) (txf.m5 * 65536);
        
        pr.setLinearGradient((int) (x0 * 65536), 
                             (int) (y0 * 65536), 
                             (int) (x1 * 65536), 
                             (int) (y1 * 65536), 
                             frac, 
                             c, 
                             cycleMethod, 
                             t);
    }
}
