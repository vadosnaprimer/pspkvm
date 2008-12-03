/**
 *
 *
 */

package javax.microedition.m3g.opengl;

import javax.microedition.lcdui.Graphics;
import java.util.Hashtable;
/**
 * This class provides some help methods to prepare 
 * OpenGL context
 *
 */

public class GLME {
    private static GLME instance;

    private Hashtable graphicsToCanvas = new Hashtable();

    private GLContext glContext;

    private GLME(){
	//create singleton GLContext for this device
	glContext = new GLContext();
    }

    public static GLME getInstance() {
	if(instance == null)
	    instance = new GLME();

	return instance;
    }

    public GLCanvas getRenderTarget(Graphics g){
	GLCanvas canvas = (GLCanvas)graphicsToCanvas.get(g);
	if(canvas == null){
	    canvas = new GLCanvas(g);
	    graphicsToCanvas.put(g, canvas);
	}
	if(!glContext.isAttached())
	    glContext.attach(g);
	return canvas;
    }

    public GL getGL(){
	return glContext.getGL(); 
    }

    public GLContext getGLContext(){
	return glContext;
    }



}
