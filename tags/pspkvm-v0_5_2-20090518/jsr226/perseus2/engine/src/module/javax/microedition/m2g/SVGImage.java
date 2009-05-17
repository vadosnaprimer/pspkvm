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

import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.DOMException;
import com.sun.perseus.model.SVGImageImpl;
import java.io.IOException;

/**
 *
 */
public class SVGImage extends ScalableImage {

    /**
     *
     */
    protected SVGImage() {
    }

    /**
     *
     */
    public Document getDocument() {
        return null;
    }

    /**
     *
     */
    public static SVGImage createEmptyImage(ExternalResourceHandler handler) {
        return SVGImageImpl.createEmptyImage(handler);
    }

    /**
     *
     */
    public void dispatchMouseEvent(String type, int x, int y) throws DOMException {
    };
    
    /**
     *
     */
    public void activate() {
    };

    /**
     *
     */
    public void focusOn( SVGElement element ) throws DOMException {
    };

    /**
     *
     */
    public void incrementTime(float seconds) {
    };

    // Inherited from ScalableImage...

    /**
     *
     */
    public static ScalableImage createImage(java.io.InputStream stream, 
                                            ExternalResourceHandler handler) throws IOException {
        return SVGImageImpl.createImage(stream, handler);
    }

    /**
     *
     */
    public static ScalableImage createImage(String URL, ExternalResourceHandler handler)
                                       throws IOException {
        return SVGImageImpl.createImage(URL, handler);
    }

    /**
     *
     */
    public void setViewportWidth(int width) {
    }

    /**
     *
     */
    public void setViewportHeight(int height) {
    }

    /**
     *
     */
    public int getViewportWidth() {
        return 0;
    }

    /**
     *
     */
    public int getViewportHeight() {
        return 0;
    }

    /**
     *
     */
    public void requestCompleted( String URI, java.io.InputStream resourceData ) throws IOException {
    }

}
