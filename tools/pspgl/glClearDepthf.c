#include <GLES/gl.h>
#include "pspgl_internal.h"

void glClearDepthf (GLclampf depth)
{
	/* The PSP's Z coord is backwards from OpenGL's, so the clear
	   depth has the opposite sense. */
	pspgl_curctx->clear.depth = 65535 - (unsigned)(65535.0f * CLAMPF((GLfloat) depth));
}

