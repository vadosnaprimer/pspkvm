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
package javax.microedition.m2g;

import java.io.InputStream;

import com.sun.perseus.midp.SVGAnimatorImpl;

/**
 *
 */
public abstract class SVGAnimator {

    /**
     *
     */
    public static SVGAnimator createAnimator(SVGImage svgImage) {
        return SVGAnimatorImpl.createAnimator(svgImage, null);
    }

    /**
     *
     */
    public static SVGAnimator createAnimator(SVGImage svgImage,
                                             String componentBaseClass) {
        return SVGAnimatorImpl.createAnimator(svgImage, componentBaseClass);
    }

    /**
     *
     */
    public abstract void setSVGEventListener(SVGEventListener svgEventListener);

    /**
     *
     */
    public abstract void setTimeIncrement(float timeIncrement);

    /**
     *
     */
    public abstract float getTimeIncrement();

    /**
     *
     */
    public abstract void play();

    /**
     *
     */
    public abstract void pause();

    /**
     *
     */
    public abstract void stop();

    /**
     *
     */
    public abstract Object getTargetComponent();

    /**
     *
     */
    public abstract void invokeAndWait(java.lang.Runnable runnable) throws InterruptedException;

    /**
     *
     */
    public abstract void invokeLater(java.lang.Runnable runnable);

}

