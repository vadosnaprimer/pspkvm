
package com.nokia.mid.ui;

import javax.microedition.lcdui.Image;

/**
 * 
 * @author liang.wu
 *
 */
public interface DirectGraphics
{

    public static final int FLIP_HORIZONTAL     = 0x2000;   //8192;
    public static final int FLIP_VERTICAL       = 0x4000;   //16384;
    
    public static final int ROTATE_90           = 90;
    public static final int ROTATE_180          = 180;
    public static final int ROTATE_270          = 270;
    
    public static final int TYPE_BYTE_1_GRAY            = 1;
    public static final int TYPE_BYTE_1_GRAY_VERTICAL   = -1;
    public static final int TYPE_BYTE_2_GRAY            = 2;
    public static final int TYPE_BYTE_4_GRAY            = 4;
    public static final int TYPE_BYTE_8_GRAY            = 8;
    public static final int TYPE_BYTE_332_RGB           = 332;
    public static final int TYPE_USHORT_4444_ARGB       = 4444;
    public static final int TYPE_USHORT_444_RGB         = 444;
    public static final int TYPE_USHORT_555_RGB         = 555;
    public static final int TYPE_USHORT_1555_ARGB       = 1555;
    public static final int TYPE_USHORT_565_RGB         = 565;
    public static final int TYPE_INT_888_RGB            = 888;
    public static final int TYPE_INT_8888_ARGB          = 8888;
    

    /**
     * Deprecated. As of Nokia UI API 1.1 in devices with MIDP 2.0 or higher, 
     * replaced by javax.microedition.lcdui.Graphics.drawRegion(javax.microedition.lcdui.Image, int, int, int, int, int, int, int, int). 
     * @param image
     * @param x
     * @param y
     * @param anchor        the anchor point for positioning the image
     * @param manipulation  flip or rotate value or a combination of values, 0 if none 
     */
    public abstract void drawImage(Image image, int x, int y, int anchor, int manipulation);

    public abstract void drawPixels(byte[] pixels, byte[] transparencyMask, int offset, int scanlength, 
            int x, int y, int width, int height, int manipulation, int format);

    public abstract void drawPixels(int[] pixels, boolean transparency, int offset, int scanlength, 
            int x, int y, int width, int height, int manipulation, int format);

    public abstract void drawPixels(short[] pixels, boolean transparency, int offset, int scanlength, 
            int x, int y, int width, int height, int manipulation, int format);

    /**
     * Draws a closed polygon defined by the arrays of the x- and y-coordinates. 
     * @param xPoints
     * @param xOffset
     * @param yPoints
     * @param yOffset
     * @param nPoints
     * @param argbColor
     */
    public abstract void drawPolygon(int[] xPoints, int xOffset, int[] yPoints, int yOffset, int nPoints, int argbColor);

    public abstract void drawTriangle(int x1, int y1, int x2, int y2, int x3, int y3, int argbColor);

    public abstract void fillPolygon(int[] xPoints, int xOffset, int[] yPoints, int yOffset, int nPoints, int argbColor);

    public abstract void fillTriangle(int x1, int y1, int x2, int y2, int x3, int y3, int argbColor);

    public abstract int getAlphaComponent();

    public abstract int getNativePixelFormat();

    public abstract void getPixels(byte[] pixels, byte[] transparencyMask, int offset, int scanlength, 
            int x, int y, int width, int height, int format);

    public abstract void getPixels(int[] pixels, int offset, int scanlength, 
            int x, int y, int width, int height, int format);

    public abstract void getPixels(short[] pixels, int offset, int scanlength, 
            int x, int y, int width, int height, int format);

    public abstract void setARGBColor(int argbColor);
}
