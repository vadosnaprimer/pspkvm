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
package com.sun.perseus.midp;

import java.io.InputStream;

import javax.microedition.m2g.SVGAnimator;
import javax.microedition.m2g.SVGEventListener;
import javax.microedition.m2g.SVGImage;

import com.sun.perseus.model.DocumentNode;

/**
 * The <code>SVGAnimatorImpl</code> class implements the <code>SVGAnimator</code>
 * JSR 226 class. It mostly delegates to the <code>SVGCanvas</code> class.
 *
 * @version $Id: SVGAnimatorImpl.java,v 1.3 2006/04/21 06:40:54 st125089 Exp $
 */
public final class SVGAnimatorImpl extends SVGAnimator {
    /**
     * The class name for a MIDP Canvas.
     */
    static final String MIDP_COMPONENT_CLASS = "javax.microedition.lcdui.Canvas";

    /**
     * The associated SVG canvas.
     */
    SVGCanvas svgCanvas;

    /**
     * Creates a new SVGAnimatorImpl associated with the given SVGImage
     * instance.
     *
     * @param svgImage the SVGImage this animator implementation should play in
     * a MIDP Canvas.
     */
    private SVGAnimatorImpl(final SVGImage svgImage) {
        svgCanvas = new SVGCanvas((DocumentNode) svgImage.getDocument());
    }

    // JAVADOC COMMENT ELIDED
    public static SVGAnimator createAnimator(SVGImage svgImage,
                                             String componentBaseClass) {
        if (svgImage == null) {
            throw new NullPointerException();
        }

        if (componentBaseClass != null 
            && 
            !MIDP_COMPONENT_CLASS.equals(componentBaseClass)) {
            throw new IllegalArgumentException();
        }

        return new SVGAnimatorImpl(svgImage);
    }

    // JAVADOC COMMENT ELIDED
    public void setSVGEventListener(SVGEventListener svgEventListener) {
        svgCanvas.setSVGEventListener(svgEventListener);
    }

    // JAVADOC COMMENT ELIDED
    public void setTimeIncrement(float timeIncrement) {
        svgCanvas.setTimeIncrement(timeIncrement);
    }

    // JAVADOC COMMENT ELIDED
    public float getTimeIncrement() {
        return svgCanvas.getTimeIncrement();
    }

    // JAVADOC COMMENT ELIDED
    public void play() {
        svgCanvas.play();
    }

    // JAVADOC COMMENT ELIDED
    public void pause() {
        svgCanvas.pause();
    }

    // JAVADOC COMMENT ELIDED
    public void stop() {
        svgCanvas.stop();
    }

    // JAVADOC COMMENT ELIDED
    public Object getTargetComponent() {
        return svgCanvas;
    }

    // JAVADOC COMMENT ELIDED
    public void invokeAndWait(Runnable runnable) throws InterruptedException {
        svgCanvas.invokeAndWait(runnable);
    }

    // JAVADOC COMMENT ELIDED
    public void invokeLater(Runnable runnable) {
        svgCanvas.invokeLater(runnable);
    }

}

