#ifndef __egltypes_h_
#define __egltypes_h_

/*
** Types and resources
*/
typedef int EGLBoolean;
typedef int EGLint;

typedef void *EGLDisplay;
typedef void *EGLSurface;
typedef void *EGLContext;

typedef int EGLConfig;
typedef int NativeDisplayType;
typedef int NativeWindowType;
typedef struct {
  int width;
  int height;
  int stride;
  unsigned char rSize;
  unsigned char gSize;
  unsigned char bSize;
  unsigned char aSize;
  unsigned char rOffset;
  unsigned char gOffset;
  unsigned char bOffset;
  unsigned char aOffset;
  void *pixels;        /* 8888 RGBA buffer, used by OpenGL ES */
  int    pixels_by_gl;
  void(*free)(void*);
  void *screen_buffer; /* 565 RGB or 8888 ARGB buffer, used in another implementation */
  int pixelBytes;
  int atab[256], rtab[256], gtab[256], btab[256];
} *NativePixmapType;

/*
** EGL and native handle values
*/
#define EGL_DEFAULT_DISPLAY ((NativeDisplayType)0)
#define EGL_NO_CONTEXT ((EGLContext)0)
#define EGL_NO_DISPLAY ((EGLDisplay)0)
#define EGL_NO_SURFACE ((EGLSurface)0)

#ifndef APIENTRY
#define APIENTRY
#endif

#endif

