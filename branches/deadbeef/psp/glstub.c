#include "GLES/gl.h"
#include "GLES/egl.h"

#define DEBUG 1

GLAPI EGLBoolean APIENTRY eglQueryContext (EGLDisplay dpy, EGLContext ctx, EGLint attribute, EGLint *value) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return EGL_FALSE;
}
GLAPI EGLBoolean APIENTRY eglQuerySurface (EGLDisplay dpy, EGLSurface surface, EGLint attribute, EGLint *value) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return EGL_FALSE;
}
GLAPI EGLBoolean APIENTRY eglSurfaceAttrib (EGLDisplay dpy, EGLSurface surface, EGLint attribute, EGLint value) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return EGL_FALSE;
}
GLAPI EGLContext APIENTRY eglGetCurrentContext (void) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return EGL_NO_CONTEXT;
}
GLAPI EGLDisplay APIENTRY eglGetCurrentDisplay (void) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return EGL_NO_DISPLAY;
}
GLAPI EGLSurface APIENTRY eglGetCurrentSurface (EGLint readdraw) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return EGL_NO_SURFACE;
}
GL_API GLboolean GL_APIENTRY glIsBuffer (GLuint buffer) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return EGL_FALSE;
}
GL_API GLboolean GL_APIENTRY glIsEnabled (GLenum cap) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return EGL_FALSE;
}
GL_API void GL_APIENTRY glActiveTexture (GLenum texture) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glAlphaFuncx (GLenum func, GLclampx ref) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glClearColorx (GLclampx red, GLclampx green, GLclampx blue, GLclampx alpha) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glClearDepthx (GLclampx depth) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glClientActiveTexture (GLenum texture) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glClipPlanef (GLenum plane, const GLfloat *equation) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glClipPlanex (GLenum plane, const GLfixed *equation) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glColor4x (GLfixed red, GLfixed green, GLfixed blue, GLfixed alpha) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glCompressedTexSubImage2D (GLenum target, GLint level, GLint xoffset, GLint yoffset, GLsizei width, GLsizei height, GLenum format, GLsizei imageSize, const GLvoid *data) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glCopyTexSubImage2D (GLenum target, GLint level, GLint xoffset, GLint yoffset, GLint x, GLint y, GLsizei width, GLsizei height) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glCurrentPaletteMatrixOES (GLuint matrixpaletteindex) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glDepthRangex (GLclampx zNear, GLclampx zFar) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glDrawTexfOES (GLfloat x, GLfloat y, GLfloat z, GLfloat width, GLfloat height) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glDrawTexfvOES (const GLfloat *coords) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glDrawTexiOES (GLint x, GLint y, GLint z, GLint width, GLint height) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glDrawTexivOES (const GLint *coords) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glDrawTexsOES (GLshort x, GLshort y, GLshort z, GLshort width, GLshort height) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glDrawTexsvOES (const GLshort *coords) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glDrawTexxOES (GLfixed x, GLfixed y, GLfixed z, GLfixed width, GLfixed height) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glDrawTexxvOES (const GLfixed *coords) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glFogx (GLenum pname, GLfixed param) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glFogxv (GLenum pname, const GLfixed *params) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glFrustumx (GLfixed left, GLfixed right, GLfixed bottom, GLfixed top, GLfixed zNear, GLfixed zFar) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glGetBooleanv (GLenum pname, GLboolean *params) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glGetBufferParameteriv (GLenum target, GLenum pname, GLint *params) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glGetClipPlanef (GLenum pname, GLfloat eqn[4]) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glGetClipPlanex (GLenum pname, GLfixed eqn[4]) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glGetFixedv (GLenum pname, GLfixed *params) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glGetLightfv (GLenum light, GLenum pname, GLfloat *params) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glGetLightxv (GLenum light, GLenum pname, GLfixed *params) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glGetMaterialfv (GLenum face, GLenum pname, GLfloat *params) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glGetMaterialxv (GLenum face, GLenum pname, GLfixed *params) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glGetTexEnvfv (GLenum env, GLenum pname, GLfloat *params) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glGetTexEnviv (GLenum env, GLenum pname, GLint *params) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glGetTexEnvxv (GLenum env, GLenum pname, GLfixed *params) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glGetTexParameterfv (GLenum target, GLenum pname, GLfloat *params) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glGetTexParameteriv (GLenum target, GLenum pname, GLint *params) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glGetTexParameterxv (GLenum target, GLenum pname, GLfixed *params) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glHint (GLenum target, GLenum mode) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glLightModelx (GLenum pname, GLfixed param) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glLightModelxv (GLenum pname, const GLfixed *params) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glLightx (GLenum light, GLenum pname, GLfixed param) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glLightxv (GLenum light, GLenum pname, const GLfixed *params) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glLineWidthx (GLfixed width) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glLoadMatrixx (const GLfixed *m) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glLoadPaletteFromModelViewMatrixOES (void) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glMaterialx (GLenum face, GLenum pname, GLfixed param) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glMaterialxv (GLenum face, GLenum pname, const GLfixed *params) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glMatrixIndexPointerOES (GLint size, GLenum type, GLsizei stride, const GLvoid *pointer) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glMultMatrixx (const GLfixed *m) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glMultiTexCoord4f (GLenum target, GLfloat s, GLfloat t, GLfloat r, GLfloat q) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glMultiTexCoord4x (GLenum target, GLfixed s, GLfixed t, GLfixed r, GLfixed q) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glNormal3x (GLfixed nx, GLfixed ny, GLfixed nz) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glOrthox (GLfixed left, GLfixed right, GLfixed bottom, GLfixed top, GLfixed zNear, GLfixed zFar) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glPointParameterf (GLenum pname, GLfloat param) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glPointParameterfv (GLenum pname, const GLfloat *params) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glPointParameterx (GLenum pname, GLfixed param) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glPointParameterxv (GLenum pname, const GLfixed *params) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glPointSize (GLfloat size) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glPointSizePointerOES (GLenum type, GLsizei stride, const GLvoid *pointer) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glPointSizex (GLfixed size) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glPolygonOffsetx (GLfixed factor, GLfixed units) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glRotatex (GLfixed angle, GLfixed x, GLfixed y, GLfixed z) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glSampleCoverage (GLclampf value, GLboolean invert) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glSampleCoveragex (GLclampx value, GLboolean invert) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glScalex (GLfixed x, GLfixed y, GLfixed z) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glTexEnviv (GLenum target, GLenum pname, const GLint *params) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glTexEnvx (GLenum target, GLenum pname, GLfixed param) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glTexEnvxv (GLenum target, GLenum pname, const GLfixed *params) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glTexParameteriv (GLenum target, GLenum pname, const GLint *params) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glTexParameterx (GLenum target, GLenum pname, GLfixed param) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glTexParameterxv (GLenum target, GLenum pname, const GLfixed *params) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glTranslatex (GLfixed x, GLfixed y, GLfixed z) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
GL_API void GL_APIENTRY glWeightPointerOES (GLint size, GLenum type, GLsizei stride, const GLvoid *pointer) {
#if DEBUG
	printf("[GL Stub]%s\n", __FUNCTION__);
#endif
	return;
}
