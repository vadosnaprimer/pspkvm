/**
 *
 *
 */

package javax.microedition.m3g.opengl;

import javax.microedition.lcdui.Graphics;
/**
 * This class provides some help methods to prepare 
 * OpenGL context
 *
 */

public class GLME {
    private static GLME instance;

    private GLCanvas canvas ;

    public static GLME getInstance() {
        if(instance == null)
	    instance = new GLME();

	return instance;
    }
    
    public GLCanvas getRenderTarget(Graphics g){
        if(canvas == null)
	    canvas = new GLCanvas(g);
        return canvas;
    }

    public GL getGL(){
       return canvas.getContext().getGL(); 
    }



}
