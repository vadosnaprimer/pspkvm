
package com.nokia.mid.ui;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * 
 * @author liang.wu
 *
 */
public class DirectUtils
{

    public DirectUtils()
    {
    }

    public static Image createImage(byte[] imageData, int imageOffset, int imageLength)
    {
        return Image.createImage(imageData, imageOffset, imageLength);
    }

    public static Image createImage(int width, int height, int ARGBcolor)
    {
        return Image.createImage(width, height);
    }

    public static DirectGraphics getDirectGraphics(Graphics g)
    {
        return new DirectGraphicsImpl(g);
    }
}
