/*
 *
 *
 */
package javax.microedition.m3g.opengl;

import java.nio.Buffer;

public class GL {
    public static final int GL_BYTE = 5120;

    public static final int GL_SHORT = 5122;

    public static final int GL_COLOR_BUFFER_BIT = 16384; 

    public static final int GL_DEPTH_BUFFER_BIT = 256;

    public static final int GL_LIGHT0 = 16384;

    public static final int GL_LIGHT1 = 16385;

    public static final int GL_LIGHT2 = 16386;

    public static final int GL_LIGHT3 = 16387;

    public static final int GL_LIGHT4 = 16388;

    public static final int GL_LIGHT5 = 16389;

    public static final int GL_LIGHT6 = 16390;

    public static final int GL_LIGHT7 = 16391;

    public static final int GL_PROJECTION = 5889;

    public static final int GL_MODELVIEW = 5888;

    public static final int GL_FLOAT = 5126;

    public static final int GL_VERTEX_ARRAY = 32884;

    public static final int GL_NORMALIZE = 2977;

    public static final int GL_NORMAL_ARRAY = 32885;

    public static final int GL_COLOR_ARRAY = 32886;

    public static final int GL_TEXTURE0 = 33984;

    public static final int GL_TEXTURE_2D = 3553;

    public static final int GL_TEXTURE_COORD_ARRAY = 32888;

    public static final int GL_TRIANGLE_STRIP = 5;

    public static final int GL_TRIANGLES = 4;

    public static final int GL_LIGHTING = 2896;

    public static final int GL_FOG = 2912;

    public static final int GL_CULL_FACE = 2884;

    public static final int GL_BLEND = 3042;

    public static final int GL_QUADS = 7;

    public static final int GL_LEQUAL = 515;

    public static final int GL_FUNC_ADD = 32774;

    public static final int GL_DEPTH_TEST = 2929;

    public static final int GL_GEQUAL = 518;

    public static final int GL_ALPHA_TEST = 3008;

    public static final int GL_SRC_ALPHA = 770;

    public static final int GL_ONE = 1;

    public static final int GL_ONE_MINUS_SRC_ALPHA = 771;

    public static final int GL_DST_COLOR = 774;

    public static final int GL_ZERO = 0;

    public static final int GL_SRC_COLOR = 768;

    public static final int GL_POLYGON_OFFSET_FILL = 32823;

    public static final int GL_FOG_MODE = 2917;

    public static final int GL_FOG_COLOR = 2918;

    public static final int GL_FOG_DENSITY = 2914;

    public static final int GL_FOG_START = 2915;

    public static final int GL_FOG_END = 2916;

    public static final int GL_EXP = 2048;

    public static final int GL_LINEAR = 9729;

    public static final int GL_LIGHT_MODEL_AMBIENT = 2899;

    public static final int GL_DIFFUSE = 4609;

    public static final int GL_AMBIENT = 4608;

    public static final int GL_SPECULAR = 4610;

    public static final int GL_POSITION = 4611;

    public static final int GL_SPOT_CUTOFF = 4614;

    public static final int GL_SPOT_EXPONENT = 4613;

    public static final int GL_SPOT_DIRECTION = 4612;

    public static final int GL_CONSTANT_ATTENUATION = 4615;

    public static final int GL_LINEAR_ATTENUATION = 4616;

    public static final int GL_QUADRATIC_ATTENUATION = 4617;

    public static final int GL_FRONT_AND_BACK = 1032;

    public static final int GL_EMISSION = 5632;

    public static final int GL_SMOOTH = 7425;

    public static final int GL_FLAT = 7424;

    public static final int GL_FRONT = 1028;

    public static final int GL_BACK = 1029;

    public static final int GL_CCW = 2305;

    public static final int GL_CW = 2304;

    public static final int GL_MODELVIEW_MATRIX = 2982;

    public static final int GL_UNSIGNED_BYTE = 5121;

    public static final int GL_TEXTURE_MIN_FILTER = 10241;

    public static final int GL_TEXTURE_MAG_FILTER = 10240;

    public static final int GL_TEXTURE_WRAP_S = 10242;

    public static final int GL_TEXTURE_WRAP_T = 10243;

    public static final int GL_TEXTURE_ENV_MODE = 8704;

    public static final int GL_TEXTURE_ENV_COLOR = 8705;


    private javax.microedition.khronos.opengles.GL10 jsr239_gl;

    /*FIXME: manage Light sources by ourselves*/
    private boolean[] lights_enable = new boolean[8];

    public GL(javax.microedition.khronos.opengles.GL jsr239GL){
        jsr239_gl = jsr239GL;
    }

    public void glClearColor(float red, float green, float blue, float alpha){
        jsr239_gl.glClearColor(red, green, blue, alpha);
    }

    public void glPushMatrix() {
        jsr239_gl.glPushMatrix();
    }

    public void glPopMatrix() {
        jsr239_gl.glPopMatrix();
    }


    public void glViewport(int x, int y, int width, int height) {
        jsr239_gl.glViewport(x, y, width, height);
    }

    public void glMatrixMode(int mode) {
        jsr239_gl.glMatrixMode(mode);
    }

    public void glVertexPointer(int size, int type, int stride, Buffer ptr){
        jsr239_gl.glVertexPointer(size, type, stride, ptr);
    }

    public void glEnableClientState(int cap){
        jsr239_gl.glEnableClientState(cap);
    }

    public void glEnable(int cap) {
        switch(cap){
            case GL_LIGHT0:
	    case GL_LIGHT1:
	    case GL_LIGHT2:
	    case GL_LIGHT3:
	    case GL_LIGHT4:
	    case GL_LIGHT5:
	    case GL_LIGHT6:
	    case GL_LIGHT7:
	        lights_enable[cap - GL_LIGHT0] = true;
		break;
	    default:
	        break;

	}
        jsr239_gl.glEnable(cap);
    }

    public void glNormalPointer(int type, int stride, Buffer ptr) {
        jsr239_gl.glNormalPointer(type ,stride, ptr);
    }

    public void glDisable(int cap) {
         switch(cap){
            case GL_LIGHT0:
	    case GL_LIGHT1:
	    case GL_LIGHT2:
	    case GL_LIGHT3:
	    case GL_LIGHT4:
	    case GL_LIGHT5:
	    case GL_LIGHT6:
	    case GL_LIGHT7:
	        lights_enable[cap - GL_LIGHT0] = false;
		break;
	    default:
	        break;

	}
       jsr239_gl.glDisable(cap);
    }
   
    public void glDisableClientState(int cap) {
        jsr239_gl.glDisableClientState(cap);
    }

    public void glColorPointer(int size, int type, int stride, Buffer ptr) {
        jsr239_gl.glColorPointer(size, type, stride, ptr);
    }

    public void glActiveTexture(int mode) {
        jsr239_gl.glActiveTexture(mode);
    }

    public void glClientActiveTexture(int mode) {
        jsr239_gl.glClientActiveTexture(mode);
    }

    public void glTexCoordPointer(int size, int type, int stride, Buffer ptr){
        jsr239_gl.glTexCoordPointer(size, type, stride ,ptr);
    }

    public void glTranslatef(float x, float y, float z) {
        jsr239_gl.glTranslatef(x, y ,z);
    }

    public void glScalef(float x, float y, float z) {
        jsr239_gl.glScalef(x, y ,z);
    }

    public void glDrawElements(int mode, int count, int type, Buffer indices) {
        jsr239_gl.glDrawElements(mode, count, type, indices);
    }

    public void glClear(int mask) {
        jsr239_gl.glClear(mask);
    }

    public void glLoadIdentity( ) {
        jsr239_gl.glLoadIdentity();
    }

    public void glBegin(int mode) {
        jsr239_gl.glBegin(mode);
    }


    public void glVertex2f(float x, float y){

    }

    public void glEnd(){
        jsr239_gl.glEnd();
    }

    public void glDepthFunc(int func){
        jsr239_gl.glDepthFunc(func);
    }

    public void glBlendEquation(int mode){

    }

    public void glDepthMask(boolean flag){
        jsr239_gl.glDepthMask(flag);
    }

    public void glColorMask(boolean red, boolean green, boolean blue, boolean alpha){
        jsr239_gl.glColorMask(red, green, blue, alpha);
    }

    public void glAlphaFunc(int func, float ref){
        jsr239_gl.glAlphaFunc(func, ref);
    }

    public void glBlendFunc(int sfactor, int dfactor){
        jsr239_gl.glBlendFunc(sfactor, dfactor);
    }
    
    public void glPolygonOffset(float factor, float units){
        jsr239_gl.glPolygonOffset(factor, units);
    }

    public void glFogi(int pname, int param){
        glFogf(pname, (float)param);
    }

    public void glFogfv(int pname, float[] params, int param_offset){
        jsr239_gl.glFogfv(pname, params, param_offset);
    }

    public void glFogf(int pname, float param){
        jsr239_gl.glFogf(pname, param); 
    }

    public void glLightModelf(int pname, float param){
        jsr239_gl.glLightModelf(pname, param);
    }
    
    public void glLightModelfv(int pname, float[] params, int offset){
        jsr239_gl.glLightModelfv(pname, params, offset);
    }
    
    public void glLightfv(int light, int pname, float[] params, int params_offset) {
        jsr239_gl.glLightfv(light, pname, params, params_offset);
    }

    public void glLightf(int light, int pname, float param){
        jsr239_gl.glLightf(light, pname, param);
    }

    public void glIsEnabled(int cap){
        switch(cap){
            case GL_LIGHT0:
	    case GL_LIGHT1:
	    case GL_LIGHT2:
	    case GL_LIGHT3:
	    case GL_LIGHT4:
	    case GL_LIGHT5:
	    case GL_LIGHT6:
	    case GL_LIGHT7:
	        return lights_enable[cap - GL_LIGHT0];
	    default:
	        break;

	}
	return false;

    }
   
    public void glMaterialfv(int face, int pname, float[] params, int params_offset){
        jsr239_gl.glMaterialfv(face, pname, params, params, params_offset);
    }

    public void glShadeModel(int mode){
        jsr239_gl.glShadeModel(mode);
    }

    public void glFrontFace(int mode){
        jsr239_gl.glFrontFace(mode);
    }

    public void glCullFace(int mode){
        jsr239_gl.glCullFace(mode);
    }

    public void glGetFloatv(int pname, float[] params, int params_offset){
        //OpenGL ES1.1 supports this
	((EGL11)jsr239_gl).glGetFloatv(pname, params, params_offset);
    }

    public void glGenTextures(int n, int[] textures, int texture_offset){
        jsr239_gl.glGenTextures(n, textures, texture_offset);
    }

    public void glBindTexture(int target, int texture){
        jsr239_gl.glBindTexture(target, texture);
    }

    public void glTexImage2D(int target, int level, int internalFormat, int width, int height, int border, int format, int type, Buffer pixels){
        jsr239_gl.glTexImage2D(target, level, internalFormat, width, height, border, format, type, pixels);
    }

    public void glTexParameteri(int target, int pname, int param){
        //FIXME: force converting to float 
        jsr239_gl.glTexParameterf(target, pname, (float)param);
    }

    public void glTexEnvi(int target, int pname, int param){
        //FIXME: force converting to float
	jsr239_gl.TexEnvf(target, pname, (float)param);
    }

    public void glTexEnvfv(int target, int pname ,float[] params, int params_offset){
        jsr239_gl.glTexEnvfv(target, pname, params, params_offset);
    }

    public void glLoadTransposeMatrixf(float[] m, int m_offset){
        float[] v = new float[m.length];
	System.arrayCopy(m, 0, v, 0, m.length);
        //restore the transpose matrix to original
	//FIXME: assume containing only 16 elements
	for(int i=0,n=3; i < 16 && n > 0; i += 5, n--){
            for(int j = 1; j <= n; j++){
                float temp = v[i + j];
		v[i + j] = v[i + j + 3*j];
		v[i + j + 3*j] = temp;
	    }
	}
	int col = m_offset % 4;
	int row = m_offset / 4;
	int m_offset1 = col * 4 + row;
	jsr239_gl.glLoadMatrixf(v, m_offset1);
    }

    public void glDrawArrays(int mode, int first, int count) {
    	jsr239_gl.glDrawArrays(mode, first, count);
    }

    public void glMultTransposeMatrixf(float[] m, int m_offset){
        float[] v = new float[m.length];
	System.arrayCopy(m, 0, v, 0, m.length);
        //restore the transpose matrix to original
	//FIXME: assume containing only 16 elements
	for(int i=0, n=3; i < 16 && n > 0; i += 5, n--){
            for(int j = 1; j <= n; j++){
                float temp = v[i + j];
		v[i + j] = v[i + j + 3*j];
		v[i + j + 3*j] = temp;
	    }
	}
	int col = m_offset % 4;
	int row = m_offset / 4;
	int m_offset1 = col * 4 + row;
        jsr239_gl.glMultMatrixf(v, m_offset1);
    }

}
