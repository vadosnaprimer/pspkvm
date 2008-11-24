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
 * RadialGradientPaint support.
 *
 * @version $Id: RadialGradientPaintDef.java,v 1.4 2006/04/21 06:35:33 st125089 Exp $
 */
public class RadialGradientPaintDef implements PaintDef {
    public static final int CYCLE_NONE = 0;
    public static final int CYCLE_REPEAT = 1;
    public static final int CYCLE_REFLECT = 2;
    
    private Transform6 IDENTITY = new Transform6();
    
    /**
     * The gradient x-axis center origin
     */
    float cx;

    /**
     * The gradient y-axis center origin
     */
    float cy;

    /**
     * The gradient x-axis focal
     */
    float fx;

    /**
     * The gradient y-axis focal
     */
    float fy;

    /**
     * The gradient radius
     */
    float r;

    /**
     * The gradient stops.
     */
    float[] fractions;

    /**
     * The gradient stops, as fixed point values.
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
     * Constructs an <code>RadialGradientPaint</code>.
     *
     * @param cx the gradient x-axis origin
     * @param cy the gradient y-axis orign
     * @param fx the gradient x-axis focal point
     * @param fy the gradient y-axis focal point
     * @param r the gradient radius
     * @param fractions the array of stop values
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
    public RadialGradientPaintDef (final float cx,
                                   final float cy,
                                   final float fx,
                                   final float fy,
                                   final float r,
                                   final float[] fractions,
                                   final int[] rgba,
                                   final int cycleMethod,
                                   final boolean isObjectBBox,
                                   final Transform gradientTransform) {
        this.cx = cx;
        this.cy = cy;
        this.fx = fx;
        this.fy = fy;
        this.r = r;
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
        
        pr.setRadialGradient((int) (cx * 65536),
                             (int) (cy * 65536),
                             (int) (fx * 65536),
                             (int) (fy * 65536),
                             (int) (r * 65536),
                             frac, 
                             rgba, 
                             cycleMethod, 
                             t);
    }
}
