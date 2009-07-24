#include "alpha_blend.h"


/***
 *** Begin alpha blending support
 ***/

// Channel masks for component blends
const javacall_pixel _sab_redmask = 0xf800;
const javacall_pixel _sab_greenmask = 0x7e0;
const javacall_pixel _sab_bluemask = 0x1f;

// Blends preshifted/separated color in _r, _g, _b onto tgt according to
// intensity (0 to 255). Note: do not call for 0 or 255 (pure intensities)
// this is inefficient. See the ALPHA_BLEND macro for filtering these out.
void alpha_blend_smooth(unsigned char alpha_intensity,
	javacall_pixel color_r,
	javacall_pixel color_g,
	javacall_pixel color_b,
	javacall_pixel* tgt) {
		javacall_pixel tgt_cpy = * tgt;
		*tgt = 0;
		unsigned char n_alpha = 255-alpha_intensity;		
		// This is about as prettily as it can be done.
		blend_component(alpha_intensity, n_alpha, color_r, tgt_cpy, tgt, _sab_redmask);
		blend_component(alpha_intensity, n_alpha, color_g, tgt_cpy, tgt, _sab_greenmask);
		blend_component(alpha_intensity, n_alpha, color_b, tgt_cpy, tgt, _sab_bluemask); }

// Blend a single component--needs the alpha intensity (0-255), 255-this value, a premasked 
// color component (see ALPHA_BLEND_PREP), the tgt pixel (prezeroed), a copy of the tgt
// pixel (with source colour values intact) and the mask for this color component.

// Note: This method 'cheats' a bit -- does an 8-bit shift on each component instead of
// dividing by 255, but the shift is faster. Note also that since direct copies (for pure
// intensities) are done short circuit, we still do get true colours at either end of
// the blend--just miss a few values just beneath full intensity. Should be imperceptible.
inline void blend_component(unsigned char alpha_intensity,
	unsigned char n_alpha_intensity,
	javacall_pixel color_c,
	javacall_pixel tgt_cpy,
	javacall_pixel* tgt,
	javacall_pixel cmask) {
		// We don't shift to zero prior to the multiply because it appears these are done
		// 32-bit on the PSP anyway, so there's room at the top of the bitfield for the product
		// even for the red component. 
		javacall_pixel c2 = ((tgt_cpy & cmask) * n_alpha_intensity) >> 8;
		javacall_pixel c1 = (color_c * alpha_intensity) >> 8;
		javacall_pixel blend = c1+c2;
		blend &= cmask;
		*tgt |= blend; }

