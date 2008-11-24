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

import org.w3c.dom.svg.SVGPath;

import com.sun.pisces.RendererBase;
import com.sun.pisces.PiscesRenderer;
import com.sun.pisces.Transform6;

/**
 * All rendering in Perseus is done through the <tt>RenderGraphics</tt> class. 
 * <br />
 * <tt>RenderGraphics</tt> is the combination of the traditional 
 * <tt>Graphics2D</tt> API to the rendering engine and the notion of 
 * graphical context found in SVG.<br />
 * <br />
 * A <tt>RenderGraphics</tt> object proxies invocation to a <tt>Graphics2D</tt>
 * instance through its <tt>draw</tt> or <tt>fill</tt> method while capturing 
 * the current rendering context state by implementing the 
 * <tt>RenderContext</tt> interface.
 * <br />
 * <b>Note A</b>: the Java 2D graphic context values passed by the 
 * <tt>RenderGraphics</tt> to the proxied <tt>Graphics2D</tt> correspond to 
 * the CSS 2 <a href="http://www.w3.org/TR/REC-CSS2/cascade.html#actual-value">
 * actual</a> values.
 * <br />
 * <b>Note B</b>: the initial values for the context properties (such 
 * as <tt>color</tt> or <tt>fill</tt>) correspond to the CSS 2 
 * <a href="http://www.w3.org/TR/REC-CSS2/about.html#q7">
 * initial</a> values for these properties.
 *
 * @see RenderContext
 * @see java.awt.Graphics2D
 *
 */
public abstract class PiscesRenderGraphics extends RenderContext {
    /**
     * Constant used to handle setTransform(null)
     * @see #setTransform
     */
    protected static final Transform IDENTITY = new Transform(null);

    /**
     * The PaintTarget is the object defining the extent of the target rendered
     * area. In some situations, this may be different than the primitive being
     * drawn. For example, the same paint target may apply to multiple
     * consecutive rendering calls.
     */
    protected PaintTarget paintTarget;

    /**
     * The paintTransform defines the coordinate space into which the PaintDef
     * should do its computation. Note that a PaintDef may add additional transform
     * to the paintTransform, for example to account for objectBoundingBox paints
     * or for paints which accept additional transforms (such as LinearGradientPaintDef
     * which accepts a gradientTransform).
     */
    protected Transform paintTransform = null;

    /**
     * The associated PiscesRenderer.
     */
    protected PiscesRenderer pr;

    /**
     * The current transform.
     */
    protected Transform6 transform = new Transform6();

    /**
     * Tracks whether or not the current transform needs to be set.
     */
    protected boolean needSetTransform = true;

    /**
     * The image transform, used in drawImage
     */
    protected Transform6 imageTransform = new Transform6();

    /**
     * The rendering extent along the x axis.
     */
    protected int width;

    /**
     * The rendering extent along the y axis.
     */
    protected int height;

    /**
     * The current rendering tile.
     */
    protected Tile renderingTile = new Tile();

    /**
     * The current primitive tile, i.e., the one that should encompass the 
     * rendering of the following rendering primitive(s). This is used to 
     * account for round-off errors in bounds computation and cut-off rendering
     * to the computed bouds for each primitive.
     */
    protected Tile primitiveTile = new Tile();

    /**
     * Constructs a new <code>PiscesRenderGraphics</code> which will delegate painting
     * operations to a <code>PiscesRenderer</code>.
     * 
     * @param pr the <tt>PiscesRenderer</tt> to render to.
     * @param width the rendering surface width. Should be greater than zero.
     * @param height the rendering surface height. Should be greater than zero.
     * @throws NullPointerException if bi is null
     */
    public PiscesRenderGraphics(final PiscesRenderer pr, final int width, final int height) {
        if (pr == null) {
            throw new NullPointerException();
        }

        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException();
        }

        this.width = width;
        this.height = height;

        this.pr = pr;
        setRenderingTile(null);
        setPrimitiveTile(null);
    }

    /** 
     * Clears the specified rectangle. IMPORTANT NOTE: the coordinates are in
     * device space. This method does not account for the current transformation
     * set on the RenderGraphics. It operates on the target pixels.
     * 
     * @param x - the x coordinate of the rectangle to clear.
     * @param y - the y coordinate of the rectangle to clear.
     * @param width - the width of the rectangle to clear.
     * @param height - the height of the rectangle to clear.
     * @param clearColor - the color to use to clear the rectangle.
     */
    public abstract void clearRect(int x,
                                   int y,
                                   int width,
                                   int height,
                                   RGB clearColor);
        
    /**
     * Sets the current rendering tile to the rectangle specified by the given
     * tile. IMPORTANT NOTE: the tile is _not_ subject to the RenderGraphics'
     * transform. The clip is defined in device coordinates.
     *
     * @param renderingTile the Tile defining the clipping area. May be null.
     */
    public void setRenderingTile(final Tile renderingTile) {
        if (renderingTile != null) {
            this.renderingTile.x = renderingTile.x;
            this.renderingTile.y = renderingTile.y;
            this.renderingTile.maxX = renderingTile.maxX;
            this.renderingTile.maxY = renderingTile.maxY;
        } else {
            this.renderingTile.x = 0;
            this.renderingTile.y = 0;
            this.renderingTile.maxX = width - 1;
            this.renderingTile.maxY = height - 1;
        }

        setPrimitiveTile(renderingTile);
    }

    /**
     * Sets the primitive tile, which is intersected with the rendering tile.
     * 
     * @param primitiveTile if null, this sets the primitiveTile to the same value
     *        as the renderingTile
     */
    public void setPrimitiveTile(final Tile primitiveTile) {
        if (primitiveTile != null) {
            this.primitiveTile.x = primitiveTile.x;
            this.primitiveTile.y = primitiveTile.y;
            this.primitiveTile.maxX = primitiveTile.maxX;
            this.primitiveTile.maxY = primitiveTile.maxY;
        } else {
            this.primitiveTile.x = renderingTile.x;
            this.primitiveTile.y = renderingTile.y;
            this.primitiveTile.maxX = renderingTile.maxX;
            this.primitiveTile.maxY = renderingTile.maxY;
        }

        applyClip();
    }

    /**
     * Applies the intersection of the rendering tile and the primitive tile
     */
    void applyClip() {
        int x = primitiveTile.x;
        int y = primitiveTile.y;
        int mx = primitiveTile.maxX;
        int my = primitiveTile.maxY;

        if (x < renderingTile.x) {
            x = renderingTile.x;
        }
        if (y < renderingTile.y) {
            y = renderingTile.y;
        }
        if (mx > renderingTile.maxX) {
            mx = renderingTile.maxX;
        }
        if (my > renderingTile.maxY) {
            my = renderingTile.maxY;
        }
        
        final int w = mx - x + 1;
        final int h = my - y + 1;
        if (w <= 0 || h <= 0) {
            throw new IllegalArgumentException();
        }

        pr.setClip(x, y, w, h);
    }

    /**
     * @return the current rendering tile. This value is _never_ null.
     */
    public Tile getRenderingTile() {
        return renderingTile;
    }

    /**
     * @return the current user clip tile. This value is _never_ null.
     */
    public Tile getPrimitiveTile() {
        return primitiveTile;
    }

    /**
     * Sets the current PaintTarget.
     *
     * @param paintTarget the new PaintTarget.
     */
    public void setPaintTarget(final PaintTarget paintTarget) {
        this.paintTarget = paintTarget;
    }

    /**
     * Sets the current paintTransform.
     *
     * @param paintTransform the new paintTransform.
     */
    public void setPaintTransform(final Transform paintTransform) {
        this.paintTransform = paintTransform;
    }

    /**
     * Turns the high quality rendering on or off.
     *
     * @param isHigh true if the rendering quality should be high.
     */
    public void setRenderingQuality(boolean isHigh) {
        if (isHigh) {
            pr.setAntialiasing(true);
        } else {
            pr.setAntialiasing(false);
        }
    }

    /**
     * Setting the transform to null is equivalent to setting it 
     * to identity.
     * @param newTransform the new value of the transform. This value is 
     *        copied, not referenced by the context.
     */
    public void setTransform(final Transform newTransform) {
        needSetTransform = (setTransform(newTransform, transform) || needSetTransform);
        needSetTransform = true;
     }

    /**
     * Transfers the transform values from the input Perseus Transform
     * the the input Pisces Transform6.
     *
     * @param newTransform the Perseus transform to transfer.
     * @param outTransform the Pisces transform destination.
     * @return true if the transform was indeed different.
     */
    static boolean setTransform(final Transform newTransform,
                                final Transform6 transform) {
        if (newTransform == null) {
            return setTransform(IDENTITY, transform);
        }

        int m00 = (int) (newTransform.m0 * 65536);
        int m10 = (int) (newTransform.m1 * 65536);
        int m01 = (int) (newTransform.m2 * 65536);
        int m11 = (int) (newTransform.m3 * 65536);
        int m02 = (int) (newTransform.m4 * 65536);
        int m12 = (int) (newTransform.m5 * 65536);
        
        // Check if new value is actually different from current
        // transform setting.
        if (m00 != transform.m00
            ||
            m10 != transform.m10
            ||
            m01 != transform.m01
            ||
            m11 != transform.m11
            ||
            m02 != transform.m02
            ||
            m12 != transform.m12) {
            // There is a change            
            transform.m00 = m00;
            transform.m10 = m10;
            transform.m01 = m01;
            transform.m11 = m11;
            transform.m02 = m02;
            transform.m12 = m12;
            return true;
        }

        return false;
    }

    /**
     * fills the input shape with the current fill color.
     * 
     * @param path the <tt>Path</tt> to fill
     */
    public void fill(final Path path) {
        fillOrDraw(path, fill, getFillOpacityImpl(), true);
    }

    /**
     * @param path the Path to fill or draw.
     * @param paint the paint to use for the operation.
     * @param opOpacity the opacity to use for the operation.
     * @param isFill if true, this is a fill operation. Otherwise, it is 
     *        a stroke operation.
     */
    void fillOrDraw(final Path path,
                    final PaintServer paint,
                    final int opOpacity,
                    final boolean isFill) {
        if (needSetTransform) {
            pr.setTransform(transform);
            needSetTransform = false;
        }

        paint.getPaintDef().setPaint(this, pr, opOpacity);
        if (isFill) {
            pr.setFill();
            pr.beginRendering(getFillRule());
        } else {
            pr.setStroke(strokeWidth,
                         getStrokeLineCap(),
                         getStrokeLineJoin(),
                         strokeMiterLimit,
                         strokeDashArray,
                         computeStrokeDashOffset());
            pr.beginRendering(WIND_NON_ZERO);
        }

        pr.setPathData(path.data, path.commands, path.nSegments);

        pr.endRendering();
    }

    /**
     * Draws the input shape using a stroke
     * derived from the following properties:
     * <ul>
     *  <li>strokeWidth</li>
     *  <li>strokeDashArray</li>
     *  <li>strokeDashOffset</li>
     *  <li>strokeLineJoin</li>
     *  <li>strokeLineCap</li>
     * </ul>
     *
     * @param path the <tt>Path</tt> to fill
     */
    public void draw(final Path path) {
        fillOrDraw(path, stroke, getStrokeOpacityImpl(), false);
    }

    /**
     * Fills a rectangle.
     *
     * @param x the rectangle's x-axis origin
     * @param y the rectangle's y-axis origin
     * @param w the rectangle's length along the x-axis
     * @param h the rectangle's length along the y-axis
     * @param aw the rectangle's rounded corner diameter along the x-axis
     * @param ah the rectangle's rounded corner diameter along the y-axis.
     */
    public void fillRect(float x, float y, float w, float h, float aw, float ah) {
        if (needSetTransform) {
            pr.setTransform(transform);
            needSetTransform = false;
        }

        fill.getPaintDef().setPaint(this, pr, getFillOpacityImpl());

        if (aw > 0 || ah > 0) {
            pr.fillRoundRect((int) (x * 65536),
                             (int) (y * 65536),
                             (int) (w * 65536),
                             (int) (h * 65536),
                             (int) (aw * 65536),
                             (int) (ah * 65536));
        } else {
            pr.fillRect((int) (x * 65536),
                        (int) (y * 65536),
                        (int) (w * 65536),
                        (int) (h * 65536));
        }
    }

    /*
     * Draws a rectangle.
     *
     * @param x the rectangle's x-axis origin
     * @param y the rectangle's y-axis origin
     * @param w the rectangle's length along the x-axis
     * @param h the rectangle's length along the y-axis
     * @param aw the rectangle's rounded corner diameter along the x-axis
     * @param ah the rectangle's rounded corner diameter along the y-axis.
     * @param rc the RenderContext defining rendering conditions.
     */
    public void drawRect(float x, float y, float w, float h, float aw, float ah) {
        if (needSetTransform) {
            pr.setTransform(transform);
            needSetTransform = false;
        }

        stroke.getPaintDef().setPaint(this, pr, getStrokeOpacityImpl());
        pr.setStroke(strokeWidth,
                     getStrokeLineCap(),
                     getStrokeLineJoin(),
                     strokeMiterLimit,
                     strokeDashArray,
                     computeStrokeDashOffset());
        
        if (aw > 0 || ah > 0) {
            pr.drawRoundRect((int) (x * 65536),
                             (int) (y * 65536),
                             (int) (w * 65536),
                             (int) (h * 65536),
                             (int) (aw * 65536),
                             (int) (ah * 65536));

        } else {
                         
            pr.drawRect((int) (x * 65536),
                        (int) (y * 65536),
                        (int) (w * 65536),
                        (int) (h * 65536));
        }
    }

    /*
     * @param x the ellipse's x-axis origin
     * @param y the ellipse's y-axis origin
     * @param width the ellipse's x-axis length
     * @param height the ellipse's y-axis length.
     */
    public void drawOval(float x, float y, float w, float h) {
        if (needSetTransform) {
            pr.setTransform(transform);
            needSetTransform = false;
        }

        stroke.getPaintDef().setPaint(this, pr, getStrokeOpacityImpl());

        pr.setStroke(strokeWidth,
                     getStrokeLineCap(),
                     getStrokeLineJoin(),
                     strokeMiterLimit,
                     strokeDashArray,
                     computeStrokeDashOffset());
                         
        pr.drawOval((int) (x * 65536),
                    (int) (y * 65536),
                    (int) (w * 65536),
                    (int) (h * 65536));
    }

    /*
     * @param x the ellipse's x-axis origin
     * @param y the ellipse's y-axis origin
     * @param width the ellipse's x-axis length
     * @param height the ellipse's y-axis length.
     */
    public void fillOval(float x, float y, float w, float h) {
        if (needSetTransform) {
            pr.setTransform(transform);
            needSetTransform = false;
        }

        fill.getPaintDef().setPaint(this, pr, getFillOpacityImpl());
        pr.fillOval((int) (x * 65536),
                    (int) (y * 65536),
                    (int) (w * 65536),
                    (int) (h * 65536));
    }

    /**
     * @param x1 the line's x-axis starting position.
     * @param y1 the line's y-axis starting position.
     * @param x2 the line's x-axis end position.
     * @param y2 the line's y-axis end position.
     */
    public void drawLine(float x1, float y1, float x2, float y2) {
        if (needSetTransform) {
            pr.setTransform(transform);
            needSetTransform = false;
        }

        stroke.getPaintDef().setPaint(this, pr, getStrokeOpacityImpl());

        pr.setStroke(strokeWidth,
                     getStrokeLineCap(),
                     getStrokeLineJoin(),
                     strokeMiterLimit,
                     strokeDashArray,
                     computeStrokeDashOffset());
                         
        pr.drawLine((int) (x1 * 65536),
                    (int) (y1 * 65536),
                    (int) (x2 * 65536),
                    (int) (y2 * 65536));
    }

    /**
     * Draws the input Image at the specified location applying the
     * input transform to the image before drawing it onto the 
     * proxied <tt>Graphics2D</tt>
     * 
     * @param image the <tt>Image</tt> to draw
     * @param dx the coordinate, along the x-axis, where the image should be drawn, in 
     *        user space.
     * @param dy the coordinate, along the y-axis, where the image should be drawn, in 
     *        user space.
     * @param dw the width, in the destination user space, of the image when drawn.
     * @param dh the height, in the destination user space, of the image when drawn.
     */
    public void drawImage(final RasterImage image, float dx, float dy, float dw, float dh) {
        // Don't process degenerate cases.
        if (image == null 
            || 
            image.getWidth() <= 0 
            || 
            image.getHeight() <= 0 
            || 
            dw <= 0 
            || 
            dh <= 0) {
            return;
        }

        int sw = image.getWidth();
        int sh = image.getHeight();

        if (needSetTransform) {
            pr.setTransform(transform);
            needSetTransform = false;
        }

        // We compute the transform so that the rectangle (0, 0, sw, sh) is
        // mapped to (dx, dy, dw, dh).
        float scaleX = dw / sw;
        float scaleY = dh / sh;

        Transform6 imageTransform = new Transform6();

        imageTransform.m00 = (int) (scaleX * 65536.0f);
        imageTransform.m11 = (int) (scaleY * 65536.0f);
        imageTransform.m02 = (int) (dx * 65536.0f);
        imageTransform.m12 = (int) (dy * 65536.0f);

	if (getOpacity() != 0.0f) {

	    pr.setTextureOpacity(getOpacity());
	    
	    pr.setTexture(RendererBase.TYPE_INT_RGB,
			  image.getRGB(),
			  sw,
			  sh,
			  0, 
			  sw,
			  imageTransform,
			  false);
	    
	    pr.fillRect((int) (dx * 65536),
			(int) (dy * 65536),
			(int) (dw * 65536),
			(int) (dh * 65536));
	}
    }
}
