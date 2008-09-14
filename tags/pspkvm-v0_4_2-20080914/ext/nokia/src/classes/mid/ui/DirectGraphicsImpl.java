
package com.nokia.mid.ui;


import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;


/**
 * 
 * @author liang.wu
 *
 */
public class DirectGraphicsImpl
    implements DirectGraphics
{

    Graphics graphics;
    
    final static int MIDP2Manip2NokiaManip[][] = 
    { 
        { 0,                        FLIP_VERTICAL|FLIP_HORIZONTAL|ROTATE_180 },         //Sprite.TRANS_NONE
        { FLIP_VERTICAL,            FLIP_HORIZONTAL|ROTATE_180 },                       //Sprite.TRANS_MIRROR_ROT180
        { FLIP_HORIZONTAL,          FLIP_VERTICAL|ROTATE_180 },                         //Sprite.TRANS_MIRROR
        { ROTATE_180,               FLIP_VERTICAL|FLIP_HORIZONTAL },                    //Sprite.TRANS_ROT180
        { FLIP_VERTICAL|ROTATE_90,  FLIP_HORIZONTAL|ROTATE_270 },                       //Sprite.TRANS_MIRROR_ROT270
        { ROTATE_90,                FLIP_VERTICAL|FLIP_HORIZONTAL|ROTATE_270 },         //Sprite.TRANS_ROT90
        { ROTATE_270,               FLIP_VERTICAL|FLIP_HORIZONTAL|ROTATE_90 },          //Sprite.TRANS_ROT270
        { FLIP_HORIZONTAL|ROTATE_90,FLIP_VERTICAL|ROTATE_270 }                          //Sprite.TRANS_MIRROR_ROT90
    };

    public DirectGraphicsImpl(Graphics g)
    {
        graphics = g;
    }

    /**
     * 
     * @param manip flip or rotate value or a combination of values
     * @return
     */
    public int nokiaManip2MIDP2Manip(int manip)
    {
        for(int i = 0; i < MIDP2Manip2NokiaManip.length; i++)
        {
            for(int j = 0; j < MIDP2Manip2NokiaManip[i].length; j++)
                if(MIDP2Manip2NokiaManip[i][j] == manip)
                    return i;

        }
        return 0;
    }

    public void drawImage(Image image, int x, int y, int anchor, int manipulation)
    {
        int width = image.getWidth();
        int height = image.getHeight();
        int transform = nokiaManip2MIDP2Manip(manipulation);
        graphics.drawRegion(image, 0, 0, width, height, transform, x, y, anchor);
    }

    native public void drawPixels(short pixels[], boolean transparency, int offset, int scanlength, int x, int y, int width, 
            int height, int manipulation, int format);
    
    native public void drawPixels(byte pixels[], byte transparencyMask[], int offset, int scanlength, int x, int y, int width, 
            int height, int manipulation, int format);
    
    native public void drawPixels(int pixels[], boolean transparency, int offset, int scanlength, int x, int y, int width, 
            int height, int manipulation, int format);

    native public void drawPolygon(int xPoints[], int xOffset, int yPoints[], int yOffset, int nPoints, int argbColor);
//    {
//        int xpoints[] = new int[nPoints];
//        int ypoints[] = new int[nPoints];
//        System.arraycopy(xPoints, xOffset, xpoints, 0, nPoints);
//        System.arraycopy(yPoints, yOffset, ypoints, 0, nPoints);
//               
//        int[] pointArray = new int[nPoints << 1];
//        
//        for(int i = 0; i < nPoints; i++)
//        {
//            pointArray[i << 1] = xpoints[i];
//            pointArray[(i << 1) + 1] = ypoints[i];
//        }
//        
//        int color = graphics.getColor();
//        graphics.setColor(argbColor);
//        graphics.drawPolygon(pointArray);
//        graphics.setColor(color);
//    }

    public void drawTriangle(int x1, int y1, int x2, int y2, int x3, int y3, int argbColor)
    {
        drawPolygon(new int[] { x1, x2, x3 }, 0, new int[] { y1, y2, y3 }, 0, 3, argbColor);
    }

    native public void fillPolygon(int xPoints[], int xOffset, int yPoints[], int yOffset, int nPoints, int argbColor);
//    {
//        int xpoints[] = new int[nPoints];
//        int ypoints[] = new int[nPoints];
//        System.arraycopy(xPoints, xOffset, xpoints, 0, nPoints);
//        System.arraycopy(yPoints, yOffset, ypoints, 0, nPoints);
//               
//        int[] pointArray = new int[nPoints << 1];
//        
//        for(int i = 0; i < nPoints; i++)
//        {
//            pointArray[i << 1] = xpoints[i];
//            pointArray[(i << 1) + 1] = ypoints[i];
//        }
//        
//        int color = graphics.getColor();
//        graphics.setColor(argbColor);
//        graphics.fillPolygon(pointArray);
//        graphics.setColor(color);
//    }

    public void fillTriangle(int x1, int y1, int x2, int y2, int x3, int y3, int argbColor)
    {
        fillPolygon(new int[] { x1, x2, x3 }, 0, new int[] { y1, y2, y3 }, 0, 3, argbColor);
    }

    public int getAlphaComponent()
    {
        return (graphics.getColor() >> 24) & 0xFF;
    }

    public int getNativePixelFormat()
    {
        return TYPE_USHORT_4444_ARGB;
    }

    native public void getPixels(int pixels[], int offset, int scanlength, 
            int x, int y, int width, int height, int format);

    native public void getPixels(short pixels[], int offset, int scanlength, 
            int x, int y, int width, int height, int format);

    native public void getPixels(byte[] pixels, byte[] transparencyMask, int offset, int scanlength, 
            int x, int y, int width, int height, int format);

    public void setARGBColor(int argbColor)
    {
    	graphics.setColor(argbColor);
    }
}
