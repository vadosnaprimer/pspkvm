/**
 *
 *
 */

package javax.microedition.m3g.opengl;

import javax.microedition.lcdui.Graphics;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.opengles.GL10;

/**
 * GLContext for the GLCanvas(Surface)
 */

public class GLContext {

    private Graphics gc;

    private GL gl;

    //JSR239 context
    private EGL10 egl;
    private GL10 jsr239_gl;
    private EGLConfig eglConfig;
    private EGLSurface eglWindowSurface;
    private EGLContext eglContext;
    private EGLDisplay eglDisplay;



    public GLContext(Graphics g){
        gc = g;
	init();
    }

    public void makeCurrent() {
        egl.eglMakeCurrent(eglDisplay, eglWindowSurface, eglWindowSurface, eglContext);

        //wait for MIDP rendering
	egl.eglWaitNative(EGL10.EGL_CORE_NATIVE_ENGINE, gc);
    }

    public void swapBuffers() {
        jsr239_gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
	jsr239_gl.glFinish();

	egl.eglWaitGL();

	egl.eglMakeCurrent(eglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);

    }

    public GL getGL() {
        if(gl == null){
            gl = new GL(jsr239_gl); 
	}
    }

    public void init() {
        egl = (EGL10)EGLContext.getEGL();

	eglDisplay = egl.eglGetDisplay(egl.EGL_DEFAULT_DISPLAY);

	int[] major_minor = new int[2];
	egl.eglInitialize(eglDisplay, major_minor);

	int[] num_config = new int[1];

	egl.eglGetConfigs(eglDisplay, null, 0, num_config);

	int redSize = 8;
	int greenSize = 8;
	int blueSize = 8;
	int alphaSize = 0;
	int depthSize = 32;
	int stencilSize = EGL10.EGL_DONT_CARE;

	int[] s_configAttribs = {
            EGL10.EGL_RED_SIZE, redSize,
	    EGL10.EGL_GREEN_SIZE, greenSize,
	    EGL10.EGL_BLUE_SIZE, blueSize,
	    EGL10.EGL_ALPHA_SIZE, alphaSize,
	    EGL10.EGL_DEPTH_SIZE, depthSize,
	    EGL10.EGL_STENCIL_SIZE, stencilSize,
	    EGL10.EGL_NONE
	};

	EGLConfig[] eglConfigs = new EGLConfig[num_config[0]];
	egl.eglChooseConfig(eglDisplay, s_configAttribs, eglConfigs, eglConfigs.length, num_config);
	eglConfig = eglConfigs[0];

	eglContext = egl.eglCreateContext(eglDisplay, eglConfig, EGL10.EGL_NO_CONTEXT, null);

	jsr239_gl = (GL10)eglContext.getGL();

	eglWindowSurface = egl.eglCreateWindowSurface(eglDisplay, eglConfig, gc, null);
    }

    public void release() {
        egl.eglMakeCurrent(eglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
	egl.eglDestroyContext(eglDisplay, eglContext);
	egl.eglDestroySurface(eglDisplay, eglWindowSurface);
	egl.eglTerminate(eglDisplay);
    }

    int getWidth(){
        int[] val = new int[1];
	egl.eglQuerySurface(eglDisplay, eglWindowSurface, EGL10.EGL_WIDTH, val);
	return val[0];
    }

    int getHeight(){
        int[] val = new int[1];
	egl.eglQuerySurface(eglDisplay, eglWindowSurface, EGL10.EGL_HEIGHT, val);
	return val[0];
    }

}
