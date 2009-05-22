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

import javax.microedition.lcdui.Image;

/**
 * Class for 2D Raster Images.
 *
 * @version $Id: RasterImage.java,v 1.4 2006/04/21 06:34:58 st125089 Exp $
 */
public class RasterImage {

    Image image;

    /**
     * The cached pixel array.
     */
    int[] argb;

    /**
     */
    RasterImage(Image img) {
        if (img == null) {
            throw new NullPointerException();
        }

        image = img;
    }

    /**
     * @return the image width.
     */
    public int getWidth() {
        return image.getWidth();
    }

    /**
     * @return the image height.
     */
    public int getHeight() {
        return image.getHeight();
    }

    /**
     * @return a pixel array where the image data is stored in 
     *         single pixel packed format 0xaarrggbb, with a 
     *         scanline stride equal to the image width and a
     *         zero offset in the returned array. The returned
     *         array is of size width * height.
     */
    public int[] getRGB() {

        if (argb != null) {
            return argb;
        }

	int w = image.getWidth();
	int h = image.getHeight();

	argb  = new int[w*h];

        image.getRGB(argb,
		     0, w,
		     0, 0,
		     w, h);

	return argb;

    }

}
