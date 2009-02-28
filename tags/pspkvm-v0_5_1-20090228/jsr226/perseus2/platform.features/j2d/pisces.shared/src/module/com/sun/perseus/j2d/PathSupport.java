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

import com.sun.pisces.Dasher;
import com.sun.pisces.Flattener;
import com.sun.pisces.PathSink;
import com.sun.pisces.PathStore;
import com.sun.pisces.Stroker;
import com.sun.pisces.Transformer;
import com.sun.pisces.Transform4;
import com.sun.pisces.Transform6;

/**
 * @version $Id: PathSupport.java,v 1.12 2006/04/21 06:35:29 st125089 Exp $
 */
public class PathSupport {
    /**
     *  Identity Transform4
     */
    private static final Transform4 identity = new Transform4();

    /**
     *  Identity Transform6
     */
    private static final Transform6 identity6 = new Transform6();

    /**
     * A transform used in the implementation of computeStrokedPathTile
     */
    private static final Transform6 txf = new Transform6();

    /**
     * Used to compute a stroked path's outline.
     */
    private static TileSink tileSink = new TileSink();

    /**
     * Used to transform the coordinates of stroked path.
     */
    private static Transformer transformerSink = new Transformer(tileSink, identity6);

    private static int toFixed(float f) {
        return (int) (f*65536.0f);
    }

    private static void emitPath(Path path, PathSink output) {
        int numSegments = path.getNumberOfSegments();
        byte[] pathCommands = path.getCommands();
        float[] pathData = path.getData();

        int x1 = -1, y1 = -1, x2, y2, x3, y3;
        float[] pt = null;

        int offset = 0;

        for (int seg = 0; seg < numSegments; seg++) {
            if (pathCommands[seg] != Path.CLOSE_IMPL) {
                x1 = toFixed(pathData[offset]);
                y1 = toFixed(pathData[offset + 1]);
            }

            switch (pathCommands[seg]) {
            case Path.MOVE_TO_IMPL:
                output.moveTo(x1, y1);
                offset += 2;
                break;
            case Path.LINE_TO_IMPL:
                output.lineTo(x1, y1);
                offset += 2;
                break;
            case Path.QUAD_TO_IMPL:
                x2 = toFixed(pathData[offset + 2]);
                y2 = toFixed(pathData[offset + 3]);
                output.quadTo(x1, y1, x2, y2);
                offset += 4;
                break;
            case Path.CURVE_TO_IMPL:
                x2 = toFixed(pathData[offset + 2]);
                y2 = toFixed(pathData[offset + 3]);
                x3 = toFixed(pathData[offset + 4]);
                y3 = toFixed(pathData[offset + 5]);
                output.cubicTo(x1, y1, x2, y2, x3, y3);
                offset += 6;
                break;
            case Path.CLOSE_IMPL:
                output.close();
                break;
            }
        }

        output.end();
    }

    /**
     * Returns true if the shape is hit by the given point.
     *
     * @param path the shape on which we do hit testing. Should not be null.
     * @param windingRule the winding rule fo the path.
     * @param hx the hit point x-axis coordinate.
     * @param hy the hit point y-axis coordinate.
     *
     * @return true if the input shape is hit. false otherwise.
     */
    public static boolean isHit(final Path path,
                                final int windingRule,
                                final float hx,
                                final float hy) {
        HitTester hitTester = new HitTester(windingRule,
                                            toFixed(hx), toFixed(hy));
        emitPath(path, hitTester);
        boolean hit = hitTester.containsPoint();
        return hit;
    }

    /**
     * Returns true if the input object is hit by the given point.
     *
     * @param strokedPath the shape on which we do hit testing.
     *        Should not be null.
     * @param windingRule the winding rule fo the path.
     * @param hx the hit point x-axis coordinate.
     * @param hy the hit point y-axis coordinate.
     *
     * @return true if the input shape is hit. false otherwise.
     */
    public static boolean isStrokedPathHit(final Object strokedPath,
                                           final int windingRule,
                                           final float hx,
                                           final float hy) {
        HitTester hitTester = new HitTester(Path.WIND_NON_ZERO,
                                            toFixed(hx), toFixed(hy));
        PathStore path = (PathStore) strokedPath;
        path.produce(hitTester);
        boolean hit = hitTester.containsPoint();
        return hit;
    }

    /**
     * @param strokedPath the object returned from a previous getStrokedPath 
     *        call. Should not be null.
     * @param t the transform from the strokedPath space to the requested
     *        tile space. 
     * @param tile the bounds of the given stroked outline.
     */
    public static void computeStrokedPathTile(final Tile tile,
                                              final Object strokedPath,
                                              final Transform t) {
        // Reuse the same TileSink over and over again.
        PathStore sp = (PathStore) strokedPath;

        // We use out own TileSink, chained with a Transformer to compute the 
        // stroked shape bounds.

        // Start from initial values
        tileSink.reset();

        // Transfer the input Transform value to the working transform txf.
        RenderGraphics.setTransform(t, txf);

        // Compute now. This call starts pulling data.
        transformerSink.setTransform(txf);
        sp.produce(transformerSink);

        // Compute the tile from the data that was just computed.
        tileSink.setTile(tile);
    }

    /**
     * Returns a PathSink that will accept path commands and feed them
     * into a stroking pipeline based on the stroke parameters of a
     * given GraphicsProperties.  The stroked output will be fed into the
     * given output PathSink.
     *
     * @param output a PathSink that will receive the stroked outline
     * @param gp a GraphicsProperties containing stroking parameters
     * @return a new PathSink that can accept the path to be stroked
     */
    private static PathSink createStroker(PathSink output,
                                          final GraphicsProperties gp) {
        Stroker stroker = new Stroker();
        stroker.setParameters((int) (gp.getStrokeWidth() * 65536),
                              gp.getStrokeLineCap(), 
                              gp.getStrokeLineJoin(),
                              (int) (gp.getStrokeMiterLimit() * 65536),
                              identity);
        stroker.setOutput(output);           
        Flattener flattener = new Flattener();
        flattener.setFlatness(1 << 16);
        
        float[] strokeDashArray = gp.getStrokeDashArray();
        if (strokeDashArray != null) {            
            int[] intStrokeDashArray = new int[strokeDashArray.length];
            for (int i = 0; i < strokeDashArray.length; i++) {
                intStrokeDashArray[i] = (int) (strokeDashArray[i] * 65536);
            }
            
            // flattener -> dasher -> stroker -> output
            Dasher dasher = new Dasher();
            dasher.setParameters(intStrokeDashArray,
                                 computeStrokeDashOffset(gp.getStrokeDashOffset(),
                                                         gp.getStrokeDashArray()),
                                 identity);
            dasher.setOutput(stroker);
            flattener.setOutput(dasher);
        } else {
            // flattener -> stroker -> output
            flattener.setOutput(stroker);
        }
        
       return flattener;
    }

    /**
     * Implemnetation: Handles negative strokeDashOffset
     *
     * @param strokeDashOffset the possibly negative dash offset value.
     * @param strokeDashArray the applicable dash array.
     * @return a positive strokeDashOffset.
     */
    static int computeStrokeDashOffset(final float strokeDashOffset,
                                final float[] strokeDashArray) {
        if (strokeDashArray == null) {
            // The stroke dash offset does not matter, simply return 0
            return 0;
        }

        if (strokeDashOffset >= 0) {
            return (int) (strokeDashOffset * 65536);
        }

        int length = 0;
        for (int i = 0; i < strokeDashArray.length; i++) {
            length += strokeDashArray[i];
        }

        if (length <= 0) {
            return 0;
        }

        float sdo = strokeDashOffset;
        while (sdo < 0) {
            sdo += length;
        }

        return (int) (sdo * 65536);
    }

    /**
     * @param path the Path to stroke.
     * @param gp the GraphicsProperties defining the rendering conditions.
     *
     * @return the stroked outline.
     */
    public static Object getStrokedPath(final Path path,
                                        final GraphicsProperties gp) {
        PathStore strokedPath = new PathStore();
        PathSink stroker = createStroker(strokedPath, gp);

        emitPath(path, stroker);
        
        return strokedPath;
    }

    /**
     * @param x the rectangle's x-axis origin
     * @param y the rectangle's y-axis origin
     * @param w the rectangle's length along the x-axis
     * @param h the rectangle's length along the y-axis
     * @param gp the GraphicsProperties defining rendering conditions.
     *
     * @return the stroked outline.
     */
    public static Object getStrokedRect(final float x, 
                                        final float y,
                                        final float w,
                                        final float h,
                                        final GraphicsProperties gp) {
        PathStore strokedPath = new PathStore();
        PathSink stroker = createStroker(strokedPath, gp);

        stroker.moveTo(toFixed(x), toFixed(y));
        stroker.lineTo(toFixed(x + w), toFixed(y));
        stroker.lineTo(toFixed(x + w), toFixed(y + h));
        stroker.lineTo(toFixed(x), toFixed(y + h));
        stroker.close();
        stroker.end();

        return strokedPath;
    }

    private static long acv = (long)(65536.0*0.22385762508460333);

    /**
     * @param fx the rectangle's x-axis origin
     * @param fy the rectangle's y-axis origin
     * @param fw the rectangle's length along the x-axis
     * @param fh the rectangle's length along the y-axis
     * @param rx the rectangle's rounded corner diameter along the x-axis
     * @param ry the rectangle's rounded corner diameter along the y-axis.
     * @param gp the GraphicsProperties defining rendering conditions.
     *
     * @return the stroked outline.
     */
    public static Object getStrokedRect(final float fx, 
                                        final float fy,
                                        final float fw,
                                        final float fh,
                                        final float rx,
                                        final float ry,
                                        final GraphicsProperties gp) {
        int x = toFixed(fx);
        int y = toFixed(fy);
        int w = toFixed(fw);
        int h = toFixed(fh);
        int aw = toFixed(rx);
        int ah = toFixed(ry);

        int xw = x + w;
        int yh = y + h;
        int aw2 = aw >> 1;
        int ah2 = ah >> 1;
        int acvaw = (int)(acv*aw >> 16);
        int acvah = (int)(acv*ah >> 16);
        int xacvaw = x + acvaw;
        int xw_acvaw = xw - acvaw;
        int yacvah = y + acvah;
        int yh_acvah = yh - acvah;
        int xaw2 = x + aw2;
        int xw_aw2 = xw - aw2;
        int yah2 = y + ah2;
        int yh_ah2 = yh - ah2;
        
        PathStore strokedPath = new PathStore();
        PathSink stroker = createStroker(strokedPath, gp);

        stroker.moveTo(x, yah2);
        stroker.lineTo(x, yh_ah2);
        stroker.cubicTo(x, yh_acvah, xacvaw, yh, xaw2, yh);
        stroker.lineTo(xw_aw2, yh);
        stroker.cubicTo(xw_acvaw, yh, xw, yh_acvah, xw, yh_ah2);
        stroker.lineTo(xw, yah2);
        stroker.cubicTo(xw, yacvah, xw_acvaw, y, xw_aw2, y);
        stroker.lineTo(xaw2, y);
        stroker.cubicTo(xacvaw, y, x, yacvah, x, yah2);
        stroker.close();
        stroker.end();

        return strokedPath;
    }

    /**
     * @param x1 the line's x-axis starting position.
     * @param y1 the line's y-axis starting position.
     * @param x2 the line's x-axis end position.
     * @param y2 the line's y-axis end position.
     * @param gp the GraphicsProperties defining rendering conditions.
     *
     * @return the stroked outline.
     */
    public static Object getStrokedLine(final float x1,
                                        final float y1,
                                        final float x2,
                                        final float y2,
                                        final GraphicsProperties gp) {
        PathStore strokedPath = new PathStore();
        PathSink stroker = createStroker(strokedPath, gp);

        stroker.moveTo(toFixed(x1), toFixed(y1));
        stroker.lineTo(toFixed(x2), toFixed(y2));
        stroker.end();

        return strokedPath;
    }

    private static final double CtrlVal = 0.5522847498307933;
    private static long pcv_ = (long)(65536.0*(0.5 + CtrlVal*0.5));
    private static long ncv_ = (long)(65536.0*(0.5 - CtrlVal*0.5));

    /**
     * @param x the ellipse's x-axis origin
     * @param y the ellipse's y-axis origin
     * @param width the ellipse's x-axis length
     * @param height the ellipse's y-axis length.
     * @param gp the GraphicsProperties defining rendering conditions.
     *
     * @return the stroked outline.
     */
    public static Object getStrokedEllipse(final float x,
                                           final float y,
                                           final float width,
                                           final float height,
                                           final GraphicsProperties gp) {

        PathStore strokedPath = new PathStore();
        PathSink stroker = createStroker(strokedPath, gp);

        int x_ = toFixed(x);
        int y_ = toFixed(y);
        int width_ = toFixed(width);
        int height_ = toFixed(height);

        int xw_ = x_ + width_;
        int xw2_ = x_ + (width_ >> 1);
        int yh_ = y_ + height_;
        int yh2_ = y_ + (height_ >> 1);
        int xpcvw_ = x_ + (int)(pcv_*width_ >> 16);
        int ypcvh_ = y_ + (int)(pcv_*height_ >> 16);
        int xncvw_ = x_ + (int)(ncv_*width_ >> 16);
        int yncvh_ = y_ + (int)(ncv_*height_ >> 16);
        
        stroker.moveTo(xw_, yh2_);
        stroker.cubicTo(xw_, ypcvh_, xpcvw_, yh_, xw2_, yh_);
        stroker.cubicTo(xncvw_, yh_, x_, ypcvh_, x_, yh2_);
        stroker.cubicTo(x_, yncvh_, xncvw_, y_, xw2_, y_);
        stroker.cubicTo(xpcvw_, y_, xw_, yncvh_, xw_, yh2_);
        stroker.close();
        stroker.end();

        return strokedPath;
   }
}

class HitTester extends PathSink {

    int windingRule;
    int px, py;
    int x0, y0, sx0, sy0;

    int crossings = 0;

    public HitTester(int windingRule, int px, int py) {
        this.windingRule = windingRule;
        this.px = px;
        this.py = py;
    }

    public void moveTo(int x0, int y0) {
        this.x0 = this.sx0 = x0;
        this.y0 = this.sy0 = y0;
    }

    public void lineJoin() {
    }

    public void lineTo(int x1, int y1) {
        int orientation;
        int hity;

        int dy = y1 - y0;
        if (dy > 0) {
            orientation =  1;
            hity = py - y0;
        } else {
            orientation = -1;
            hity = py - y1;
            dy = -dy;
        }

        // If line's Y extent includes py, find the X value where the line
        // intersects the horizontal line y = py.  If the intersection lies
        // to the left of px, accumulate the line orientation into the
        // count of crossings.

        if (hity >= 0 && hity < dy) {
            int hitx, dx;

            if (orientation == 1) {
                hitx = px - x0;
                dx = x1 - x0;
            } else {
                hitx = px - x1;
                dx = x0 - x1;
            }

            if ((long)hity*dx < (long)hitx*dy) {
                crossings += orientation;
            }
        }

        this.x0 = x1;
        this.y0 = y1;
    }

    public void quadTo(int x1, int y1, int x2, int y2) {
        System.err.println(">>>>>>>>>>>> path for hit testing should not contain quadTo!");
    }

    public void cubicTo(int x1, int y1, int x2, int y2, int x3, int y3) {
        System.err.println(">>>>>>>>>>>> path for hit testing should not contain cubicTo!"); 
    }

    public void close() {
        lineTo(sx0, sy0);
    }

    public void end() {
    }

    public boolean containsPoint() {
        return (windingRule == Path.WIND_NON_ZERO) ?
            (crossings != 0) : ((crossings & 0x1) == 0x1);
    }
}
