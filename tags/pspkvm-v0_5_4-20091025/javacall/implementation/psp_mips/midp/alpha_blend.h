
#ifndef _alpha_blend_h
#define _alpha_blend_h

#ifdef __cplusplus
extern "C" {
#endif

#ifdef __cplusplus
} /* extern "C" */
#endif

#include "javacall_font.h"
#include "javacall_properties.h"

/*
	Fast(ish) and clean alpha blending for the FreeType-supported typeface backend
	/AJM
*/

// Channel masks for component blends
extern const javacall_pixel _sab_redmask;
extern const javacall_pixel _sab_greenmask;
extern const javacall_pixel _sab_bluemask;

// Blends preshifted/separated color in _r, _g, _b onto tgt according to
// intensity (0 to 255). Note: do not call for 0 or 255 (pure intensities)
// this is inefficient. See the ALPHA_BLEND macro for filtering these out.
void alpha_blend_smooth(unsigned char alpha_intensity,
	javacall_pixel color_r,
	javacall_pixel color_g,
	javacall_pixel color_b,
	javacall_pixel* tgt);


#endif /* ifndef _alpha_blend_h */
