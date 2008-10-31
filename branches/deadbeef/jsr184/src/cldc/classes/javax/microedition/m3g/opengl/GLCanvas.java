/**
 *
 *
 */

package javax.microedition.m3g.opengl;

import javax.microedition.lcdui.Graphics;
import javax.microedition.m3g.RenderEventListener;
import java.util.Vector;
import javax.microedition.lcdui.Display;

/**
 *  
 * .
 *
 */
public class GLCanvas {
    private Vector renderListeners;
    
    private GLContext glContext;

    private Graphics graphics2D;

    GLCanvas(Graphics g2D){
        graphics2D = g2D;
	glContext = new GLContext(g2D);
    }

    

    public void addGLEventListener(RenderEventListener listener) {
       renderListeners.addElement(listener); 
    }

    public void removeGLEventListener(RenderEventListener listener){
       renderListeners.removeElement(listener); 
    }

    public void display() {
    }

    public GL getGL() {
        return GLME.getInstance().getGL();
    }

    public GLContext getContext() {
       return glContext;
    }

   
    //FIXME: define KNI functions to do this. At this moment, using MIDP API to get displayable dimension
    //using native method to retrive pixel buffer dimension included in target 
    //graphics target for viewport
    //private native int getTargetWidth(Graphics g2D);
    //private native int getTargetHeight(Graphics g2D);


    public int getWidth() {
        //return getTargetWidth(graphics2D); 
	return glContext.getWidth();
    }

    public int getHeight() {
        //return getTargetHeight(graphics2D);
	return glContext.getHeight();
    }
}
