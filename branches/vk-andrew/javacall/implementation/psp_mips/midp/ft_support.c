#include "ft_support.h"
#include "alpha_blend.h"
/*
	FreeType2 caching support layer for the javacall_font implementations
	for the PSP
	(Vastly faster, less RAM-hungry than the legacy layer was/is, allows use of massive
	 Unicode fonts like the FreeFont project sets, somewhat less forgiving of odd metrics
	 setups in old font files, tho'.)
	 
	AJ Milne for the PSPKVM project / 2009 07
*/

/* The cache manager handles */
static FT_Library _ftc_library;
static FTC_Manager cache_manager;
static FTC_CMapCache cmap_cache;
static FTC_SBitCache sbit_cache;

/* State: lib, cache managers have been initialized */
static int _ftc_initialized=0;
/* State: use internal font flag (bypass freetype entirely) */
static int _ftc_use_internal_font = 0;
/* State: static font sizes */
static int _ftc_small = 0;
static int _ftc_medium = 0;
static int _ftc_large = 0;

/* State: current font settings */
static FTC_ScalerRec current_ic;

/** Face retrieval support */

/* Local constant array--used by get_face_filename
   these are the (constant, canonical) face IDs. */
const char* _typeface_filenames[] = {
	"sys.ttf", "prop.ttf", "mono.ttf",
	"sys_i.ttf", "prop_i.ttf", "mono_i.ttf",
	"sys_b.ttf", "prop_b.ttf", "mono_b.ttf",  
	"sys_ib.ttf", "prop_ib.ttf", "mono_ib.ttf" };

// Resolve face specs to a filename--the resulting
// constant pointer is treated as an FTC_FaceID
// for requesting faces from the cache system.
FTC_FaceID get_face_id(javacall_font_face face,
	javacall_font_style style) {
	int idx = 0;
	switch(face) {
		case JAVACALL_FONT_FACE_PROPORTIONAL: idx=1; break;
		case JAVACALL_FONT_FACE_MONOSPACE: idx=2; break; }
	if ((style&JAVACALL_FONT_STYLE_ITALIC)!=0) {
		idx+=3; }
  if ((style&JAVACALL_FONT_STYLE_BOLD)!=0) {
		idx+=6; }
	return (FTC_FaceID)(_typeface_filenames[idx]); }

// Read a font size setting -- pass in the config name, and a fallback
int ftc_pixsize_fr_config(const char* cfgname, int default_sz) {
	char* cstr;
	int r;
	if (javacall_get_property(cfgname,
		JAVACALL_INTERNAL_PROPERTY, &cstr)!=JAVACALL_OK) {
			return default_sz; }
	if (sscanf(cstr, "%i", &r)!=1) {
		return default_sz; }
	return r; }

// Translate a size parameter to a pixel size
int size_param_to_pixels(javacall_font_size s) {
	switch(s) {
		case JAVACALL_FONT_SIZE_SMALL:
			return _ftc_small;
		case JAVACALL_FONT_SIZE_MEDIUM:
			return _ftc_medium;
		case JAVACALL_FONT_SIZE_LARGE:
			return _ftc_large;
		default:
			return _ftc_medium; } }

// Call to set the local static font sizes, and the _ftc_use_internal_font flag
// from the system config.
void init_from_config() {
	char* str;
	if (JAVACALL_OK == javacall_get_property("com.pspkvm.font.internal", 
		JAVACALL_INTERNAL_PROPERTY, &str)) {
		if (str && !strcmp(str, "true")) {
			_ftc_use_internal_font = 1; } }
	if (_ftc_use_internal_font) {
		// Done
		return; }
	_ftc_small = ftc_pixsize_fr_config("com.pspkvm.fontsize.small", 14);
	_ftc_medium = ftc_pixsize_fr_config("com.pspkvm.fontsize.med", 16);
	_ftc_large = ftc_pixsize_fr_config("com.pspkvm.fontsize.large", 18); }

/** Face retrieval callback used by top level cache manager */
FT_CALLBACK_DEF( FT_Error )  
fts_face_requester(
	FTC_FaceID  face_id,
	FT_Library  lib,                     
	FT_Pointer  request_data,                     
	FT_Face*    aface)  {    

	FT_UNUSED( request_data );
	return FT_New_Face(lib, (const char*)face_id, 0, aface); }

// Utility inline to set an FTC_ScalerRec structure consistently.  
inline void set_scaler_rec(FTC_ScalerRec* r,
                     javacall_font_face face, 
                     javacall_font_style style, 
                     javacall_font_size size) {
	r->face_id = get_face_id(face, style);
	r->height = size_param_to_pixels(size);
	r->width = 0;
	r->pixel = 1; }

/** Init call--initializes the lib and the various cache managers */
FT_Error init_font_cache_subsystem() {
	FT_Error err;
	init_from_config();
	set_scaler_rec(&current_ic, JAVACALL_FONT_FACE_SYSTEM,
					JAVACALL_FONT_STYLE_PLAIN,
					JAVACALL_FONT_SIZE_MEDIUM);
	if (_ftc_use_internal_font) {
		// Don't need all this cache stuff, in this case
		return 1; }
	// Lib
	err = FT_Init_FreeType(&_ftc_library);
	if (err) {
		return err; }
	// Top level cache manager
	err = FTC_Manager_New(_ftc_library, 0, 0, 0,
		fts_face_requester, (FT_Pointer)NULL, &cache_manager);
	if (err) {
		return err; }
	// Small bitmap cache manager
 	err = FTC_SBitCache_New(cache_manager, &sbit_cache);
	if (err) {
		return err; }
	// Glyph(char)map cache manager
	err = FTC_CMapCache_New(cache_manager, &cmap_cache);
	if (err==0) {
		_ftc_initialized=1; }
	return err; }

/** Local alpha blend macros
 */
	
// Setup macro. We decompose the color components of the font color
// per call to draw_bitmap(...). Saves a bit of work per pixel and component.
#define ALPHA_BLEND_PREP \
		javacall_pixel color_r = color & _sab_redmask; \
		javacall_pixel color_g = color & _sab_greenmask; \
		javacall_pixel color_b = color & _sab_bluemask;

// Drop-in-replacement macro for the legacy alpha blend
// Short-circuits around pure intensities/simple copies,
// and then calls alpha_blend_smooth(...)    	                                                     
#define ALPHA_BLEND \
	if (*fontpoint != 0) { \
		if (*fontpoint == 255) { \
			*point = color; } \
		else { \
				alpha_blend_smooth(*fontpoint, color_r, color_g, color_b, point); } } \
		 point++; fontpoint++;

/***
 *** End alpha blending support
 ***/

/** Draw a small bitmap */
static void draw_small_bitmap(FTC_SBit bitmap, javacall_pixel color,
	                             int clipX1, int clipY1, int clipX2, int clipY2,
	                             javacall_pixel* destbuf, 
	                             int w, int h, int x, int y, FT_Pixel_Mode mode) {
  static unsigned char BitMask[8] = {0x80,0x40,0x20,0x10,0x8,0x4,0x2,0x1};
  FT_Int  i, j;  
  FT_Int  x_max = x + bitmap->width;  
  FT_Int  y_max = y + bitmap->height;
  FT_Int xoffset, yoffset, xnum, ynum;

  if ((clipX1 > x_max) || (clipX2 < x) || (clipY1 > y_max) || (clipY2 < y)) {
  	return; }

  clipX1 = clipX1<0?0:clipX1;
  clipY1 = clipY1<0?0:clipY1;
  clipX2 = clipX2>=w?w-1:clipX2;
  clipY2 = clipY2>=h?h-1:clipY2;

  x_max = x_max > clipX2?clipX2+1:x_max;
  y_max = y_max > clipY2?clipY2+1:y_max;

  xoffset = clipX1 - x;
  if (xoffset < 0) xoffset = 0;

  yoffset = clipY1 - y;
  if (yoffset < 0) yoffset = 0;

  xnum = x_max - x - xoffset;
  ynum = y_max - y - yoffset;

  if (xnum <= 0 || ynum <= 0)
  	return;
  
  javacall_pixel* point = destbuf + (y + yoffset) * w + x + xoffset;

  if (mode == FT_PIXEL_MODE_MONO) {
		unsigned char* fontpoint = &bitmap->buffer[0] + yoffset * bitmap->width/8;
		
		int bitOffset = xoffset%8;
		int pitch = bitmap->pitch*8;
		for ( j = 0; j < ynum; j++) {    	 
			for ( i = 0; i < xnum; i++, bitOffset++) {
				if (bitOffset >= 8) {    	         	
					fontpoint+= bitOffset / 8;
					bitOffset %= 8; }
				if (*fontpoint & BitMask[bitOffset]) {
					*point = color; }
				point++; }
			point += (w - xnum);
			bitOffset += (pitch - xnum); }
		// End mono rendering
		return; }
  // Handle rendering anti-aliased bitmaps
	ALPHA_BLEND_PREP
  unsigned char* fontpoint = &bitmap->buffer[0] + yoffset * bitmap->width + xoffset;

	int block = xnum / 8;
	int rem  = xnum % 8;
	for ( j = 0; j < ynum; j++ ) {    	
		for ( i = 0; i < block ; i++) {
			ALPHA_BLEND
			ALPHA_BLEND
			ALPHA_BLEND
			ALPHA_BLEND
			ALPHA_BLEND
			ALPHA_BLEND
			ALPHA_BLEND
			ALPHA_BLEND }
	
		switch (rem) {
			case 7: ALPHA_BLEND
			case 6: ALPHA_BLEND
			case 5: ALPHA_BLEND
			case 4: ALPHA_BLEND
			case 3: ALPHA_BLEND
			case 2: ALPHA_BLEND
			case 1: ALPHA_BLEND }
	
		point += (w - xnum);
		fontpoint += (bitmap->width - xnum); } }

/** I have no idea where this number comes from -- was in the legacy source,
 ** appears to be necessary here, but then a lot of this rendering comes
 ** from there, too, and is a bit opaque
 */
#define _MYSTERY_YPAD 12

// Direct interface implementation
javacall_result ftc_javacall_font_draw(javacall_pixel   color, 
                        int clipX1, 
                        int clipY1, 
                        int clipX2, 
                        int clipY2,
                        javacall_pixel* destBuffer, 
                        int destBufferHoriz, 
                        int destBufferVert,
                        int x, 
                        int y, 
                        const javacall_utf16* text, 
                        int textLen){

  int i;
	if (_ftc_use_internal_font) {
		return JAVACALL_FAIL; }
	if (!_ftc_initialized) {
		if (init_font_cache_subsystem()) {
			return JAVACALL_FAIL; } }  
  if (current_ic.face_id==NULL) {
  	return JAVACALL_FAIL; }
	unsigned int glyph_idx;
	FTC_SBit irec;
  for(i=0; i<textLen; i++) {
		glyph_idx = FTC_CMapCache_Lookup(cmap_cache, current_ic.face_id, -1, text[i]);
		if (!glyph_idx) {
			return JAVACALL_FAIL; }
		if (FTC_SBitCache_LookupScaler(sbit_cache, &current_ic, FT_LOAD_DEFAULT,
			glyph_idx, &irec, (FTC_Node*)NULL)) {
			return JAVACALL_FAIL; }
		draw_small_bitmap(irec, color, clipX1, clipY1, clipX2, clipY2,
        	               destBuffer,
        	               destBufferHoriz, destBufferVert, x, 
        	               y - irec->top + _MYSTERY_YPAD, irec->format);
		x += irec->xadvance; }
	return JAVACALL_OK; }

#define _MIN_LEADING 2			

// Direct interface implementation
javacall_result ftc_javacall_font_get_info( javacall_font_face  face, 
                                        javacall_font_style style, 
                                        javacall_font_size  size, 
                                        /*out*/ int* ascent,
                                        /*out*/ int* descent,
                                        /*out*/ int* leading) {
	if (_ftc_use_internal_font) {
		return JAVACALL_FAIL; }
	if (!_ftc_initialized) {
		if (init_font_cache_subsystem()) {
			return JAVACALL_FAIL; } }
			
	FTC_ScalerRec r;
	set_scaler_rec(&r, face, style, size);
	FT_Size sz;
	if (FTC_Manager_LookupSize(cache_manager, &r, &sz)) {
		return JAVACALL_FAIL; }
	*descent= FT_MulFix( sz->metrics.descender, sz->metrics.y_scale ) / 64;
	if ((*descent)<0) {
		(*descent) *= -1; }
	*ascent = FT_MulFix( sz->metrics.ascender, sz->metrics.y_scale) / 64;	
	*leading=_MIN_LEADING;
	return JAVACALL_OK; }

// Direct interface implementation
javacall_result ftc_javacall_font_set_font( javacall_font_face face, 
                                        javacall_font_style style, 
                                        javacall_font_size size) {
	if (_ftc_use_internal_font) {
		return JAVACALL_FAIL; }
	if (!_ftc_initialized) {
		if (init_font_cache_subsystem()) {
			return JAVACALL_FAIL; } }
	set_scaler_rec(&current_ic, face, style, size);
	return JAVACALL_OK; }

// Direct interface implementation
int ftc_javacall_font_get_width(javacall_font_face face, 
                            javacall_font_style style, 
                            javacall_font_size size,
                            const javacall_utf16* charArray, 
                            int charArraySize) {
	if (_ftc_use_internal_font) {
		return -1; }
	if (!_ftc_initialized) {
		if (init_font_cache_subsystem()) {
			return -1; } }

	FTC_ScalerRec tmp_ic;
	set_scaler_rec(&tmp_ic, face, style, size);
	int i, res;
	unsigned int glyph_idx;
	res=0;
	FTC_SBit irec;
	for(i=0; i<charArraySize; i++) {
		glyph_idx = FTC_CMapCache_Lookup(cmap_cache, tmp_ic.face_id, -1, charArray[i]);
		if (!glyph_idx) {
			return -1; }
		if (FTC_SBitCache_LookupScaler(sbit_cache, &tmp_ic, FT_LOAD_DEFAULT,
			glyph_idx, &irec, (FTC_Node*)NULL)) {
			return -1; }
		res += irec->xadvance; }
	return res; }

