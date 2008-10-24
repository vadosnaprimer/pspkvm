#include "pspgl_internal.h"


EGLBoolean eglGetConfigAttrib (EGLDisplay dpy, EGLConfig config, EGLint attribute, EGLint *value)
{
       const struct pspgl_pixconfig *pixconf;
	
       pixconf = &__pspgl_pixconfigs[EGLCFG_PIXIDX(config)];
       
	switch (attribute) {
	case EGL_WIDTH:
		*value = 480;
		break;
	case EGL_HEIGHT:
		*value = 272;
		break;
	case EGL_SURFACE_TYPE:
		*value = EGL_PBUFFER_BIT | EGL_PIXMAP_BIT | EGL_WINDOW_BIT;
		break;
	case EGL_BLUE_SIZE:
		*value = pixconf->blue_bits;
		break;
	case EGL_GREEN_SIZE:
		*value = pixconf->green_bits;
		break;
	case EGL_RED_SIZE:
		*value = pixconf->red_bits;
		break;
	case EGL_STENCIL_SIZE:
		*value = pixconf->stencil_bits;
		break;
	case EGL_ALPHA_SIZE:
		*value = pixconf->alpha_bits;
		break;
	case EGL_BUFFER_SIZE:
		*value = pixconf->alpha_bits + pixconf->blue_bits + pixconf->green_bits + pixconf->red_bits;
		break;
	default:
		printf("eglGetConfigAttrib: attr:%x = [stub]!\n", attribute);
		return EGL_FALSE;
	}

	return EGL_TRUE;
}

