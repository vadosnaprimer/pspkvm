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

import javax.microedition.lcdui.Graphics;

import com.sun.perseus.model.DocumentNode;
import com.sun.perseus.model.DirtyAreaManager;

import com.sun.perseus.j2d.RGB;
import com.sun.perseus.j2d.RenderGraphics;
import com.sun.perseus.j2d.Transform;

import com.sun.pisces.GraphicsSurfaceDestination;
import com.sun.pisces.NativeSurface;
import com.sun.pisces.PiscesRenderer;
import com.sun.pisces.RendererBase;

/**
 *
 */
public class ScalableGraphics  {
    /**
     * Paint used to clear offscreens.
     */
    static final RGB CLEAR_PAINT = new RGB(0, 0, 0, 0);

    /**
     * The Graphics object bound to this class
     */
    Graphics g = null;

    /**
     * The GraphicsSurfaceDestination used to blit the native surface to the
     * Graphics.
     */
    GraphicsSurfaceDestination gsd = null;

    /**
     * The current quality mode.
     */
    int qualityMode = RENDERING_QUALITY_HIGH;

    /**
     * The DirtyAreaManager used to minimize renderings.
     */
    DirtyAreaManager dirtyAreaManager = new DirtyAreaManager(null);

    /**
     * The current transparency for rendering images.
     */
    float alpha = 1f;

    /**
     * The offscreen buffer, used for temporary rendering of the 
     * image.
     */
    NativeSurface offscreen;

    /**
     * The PiscesRenderer associated with the offscreen.
     */
    PiscesRenderer pr;

    /**
     * The offscreen width.
     */
    int offscreenWidth;

    /**
     * The offscreen height.
     */
    int offscreenHeight;

    /**
     * The RenderGraphics used to draw to the PiscesRenderer
     */
    RenderGraphics rg;

    /**
     *
     */
    public static final int RENDERING_QUALITY_LOW = 1;

    /**
     *
     */
    public static final int RENDERING_QUALITY_HIGH = 2;

    /**
    * Constructor
    */
    private ScalableGraphics() {
    }
	
    /**
     *
     */
    public void bindTarget(java.lang.Object target) {
        if (target == null) {
            throw new NullPointerException();
        }
        
        if (!(target instanceof Graphics)) {
            throw new IllegalArgumentException();
        }
        
        if (g != null) {
            throw new IllegalStateException("bindTarget(" + target + ") with g : " + g);
        }
        
        g = (Graphics) target;
        gsd = new GraphicsSurfaceDestination(g);
    }
    
    /**
     *
     */
    public void releaseTarget() {
        if (g == null) {
            throw new IllegalStateException("releaseTarget() with null current target");
        }

        g = null;
        gsd = null;
    }

    /**
     *
     */
    public void render(int x, int y, ScalableImage image) {
        if (image == null) {
            throw new NullPointerException();
        }

        if (g == null) {
            throw new IllegalStateException();
        }

        DocumentNode documentNode = 
            (DocumentNode) ((SVGImage) image).getDocument();

        int vpw = image.getViewportWidth();
        int vph = image.getViewportHeight();
        checkOffscreen(vpw, vph);

        if (DirtyAreaManager.ON) {
            dirtyAreaManager.setViewport(documentNode);
            documentNode.setUpdateListener(dirtyAreaManager);
        }

        rg.setRenderingQuality(qualityMode == RENDERING_QUALITY_HIGH);

        documentNode.sample(documentNode.getCurrentTime());
        documentNode.applyAnimations();

        if (DirtyAreaManager.ON) {
            dirtyAreaManager.refresh(documentNode, rg, CLEAR_PAINT);
        } else {
            // Clear offscreen and paint
            pr.setColor(0, 0, 0, 0);
            pr.setClip(0, 0, offscreenWidth, offscreenHeight);
            pr.clearRect(0, 0, offscreenWidth, offscreenHeight);
            documentNode.paint(rg);
        }
        
        // Now, render the image with alpha.
        gsd.drawSurface(offscreen, 0, 0, x, y, offscreenWidth, offscreenHeight, alpha);
    }

    /**
     * Get an offscreen buffer big enough to draw a widht by height
     * image.
     *
     * @param width the desired minimal width
     * @param height the desired minimal height.
     */
    void checkOffscreen(final int width,
                        final int height) {
        int w = width;
        int h = height;
        if (w <= 0) {
            w = 1;
        }
        
        if (h <= 0) {
            h = 1;
        }
        
        if (offscreen == null
            ||
            offscreenWidth != w
            ||
            offscreenHeight != h) {
            offscreen = new NativeSurface(w, h);
            offscreenWidth = w;
            offscreenHeight = h;

            pr = new PiscesRenderer(offscreen, w, h, 0, w, 1, 
                                    RendererBase.TYPE_INT_ARGB);
            rg = new RenderGraphics(pr, offscreenWidth, offscreenHeight);
        } 
    }

    /**
     *
     */
    public void setRenderingQuality(int mode) {
        if (mode != RENDERING_QUALITY_LOW && mode != RENDERING_QUALITY_HIGH) {
            throw new IllegalArgumentException("" + mode);
        }

        this.qualityMode = mode;
    }

    /**
     *
     */
    public void setTransparency(float alpha) {
        if (alpha < 0f || alpha > 1f) {
            throw new IllegalArgumentException();
        }

        this.alpha = alpha;
    }

    /**
     *
     */
    public static ScalableGraphics createInstance() {
        return new ScalableGraphics();
    }

}
