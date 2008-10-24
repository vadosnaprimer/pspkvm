#include <stdlib.h>
#include <string.h>

#include "pspgl_internal.h"
#include "pspgl_buffers.h"

#define MASK(bits)	((1 << (bits)) - 1)

EGLSurface eglCreatePixmapSurface (EGLDisplay dpy, EGLConfig config, NativePixmapType pixmap, const EGLint *attrib_list) {
 	struct pspgl_surface *s;
	unsigned long bytesperpixel;
	unsigned long bufferlen;
	const struct pspgl_pixconfig *pixconf;
	int has_depthbuffer = EGLCFG_HASDEPTH(config);

	pixconf = &__pspgl_pixconfigs[EGLCFG_PIXIDX(config)];

	s = malloc(sizeof(struct pspgl_surface));
	if (!s)
		goto bad_alloc;
	memset(s, 0, sizeof(*s));

	if (pixconf->hwformat == GE_RGBA_8888)	
		bytesperpixel = 4;
	else
		bytesperpixel = 2;

	if ( attrib_list && attrib_list[0] != EGL_NONE) {
	    return EGL_NO_SURFACE;
	}

	s->refcount = 1;
	s->width = pixmap->width;
	s->height = pixmap->height;
	s->pixelperline = pow2(s->width);
	s->flags = 0 ;
	s->pixfmt = pixconf->hwformat;

	s->alpha_mask	= MASK(pixconf->alpha_bits);
	s->stencil_mask	= MASK(pixconf->stencil_bits);

	s->read = &s->color_back;
	s->draw = &s->color_back;

	bufferlen = s->height * s->pixelperline * bytesperpixel;

	psp_log("width = %lu\n", s->width);
	psp_log("height = %lu\n", s->height);
	psp_log("pixelperline = %lu\n", s->pixelperline);
	psp_log("pixelformat = 0x%04x\n", s->pixfmt);
	psp_log("masks: alpha=%02x stencil=%02x\n",
		s->alpha_mask, s->stencil_mask);

	if (!(s->color_front = __pspgl_buffer_new(bufferlen, GL_STATIC_COPY_ARB)))
		goto bad_alloc;
	if (pixmap->pixels && !pixmap->pixels_by_gl) {
		(*(pixmap->free))(pixmap->pixels);
	}
	pixmap->pixels = s->color_front->base;
	pixmap->stride = s->pixelperline*bytesperpixel;
	pixmap->pixels_by_gl = 1;
	s->color_front->flags |= (BF_PINNED_FIXED/* | BF_UNMANAGED*/);


	/* If we're single buffered, set back buffer == front
	   buffer; this way all the other code can assume
	   double-buffering without needing to check, but we
	   still get single-buffered behaviour. */
	s->color_back = s->color_front;
	s->color_back->refcount++;
	
	bufferlen = s->height * s->pixelperline * 2;

	if (has_depthbuffer) {
		if (!(s->depth_buffer = __pspgl_buffer_new(bufferlen, GL_STATIC_COPY_ARB)))
			goto bad_alloc;
		s->depth_buffer->flags |= BF_PINNED_FIXED;
	}

	return (EGLSurface) s;

bad_alloc:
	eglDestroySurface(dpy, s);
	EGLERROR(EGL_BAD_ALLOC);
	return EGL_NO_SURFACE;
}
