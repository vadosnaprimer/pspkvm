#include "ft_support.h"
#include "alpha_blend.h"
/*
	FreeType2 caching support layer for the javacall_font* implementations
	for the PSP
	(Vastly faster, less RAM-hungry than the legacy layer was/is, allows use of massive
	 Unicode fonts like the FreeFont project sets, but less tolerant of bad
	 typeface metrics than some approaches.)
	AJ Milne for the PSPKVM project / 2009 07
*/

/* The cache manager handles */
static FT_Library _ftc_library;
static FTC_Manager cache_manager;
static FTC_CMapCache cmap_cache;
static FTC_SBitCache sbit_cache;

/* Defines for the cache manager storage size */
#define _FTC_MAX_FACES 12
#define _FTC_MAX_SIZES 36
#define _FTC_MAX_BYTES 48*1024

/* Cache, etc. have been initialized */
static int _ftc_initialized=0;
/* Use internal font flag */
static int _ftc_use_internal_font = 0;
/* Static font sizes */
static int _ftc_small = 0;
static int _ftc_medium = 0;
static int _ftc_large = 0;

/* Current font settings */
static FTC_ScalerRec current_ic;

/***
 *** Predeclares to make this a bit easier to compile
*/


/* Retrieve the face ID corresponding to a face spec */
FTC_FaceID get_face_id(javacall_font_face face,
	javacall_font_style style);
/** Library init */
FT_Error init_font_cache_subsystem();
/** Rendering support **/
static void draw_small_bitmap(FTC_SBit bitmap, javacall_pixel color,
	                             int clipX1, int clipY1, int clipX2, int clipY2,
	                             javacall_pixel* destbuf, 
	                             int w, int h, int x, int y, FT_Pixel_Mode mode);
/***
 *** End predeclares
 */ 

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
			
/** I have no idea where this number comes from -- was in the legacy source,
 ** appears to be necessary here, but then a lot of this rendering comes
 ** from there, too, and is a bit opaque
 */
 
#define _MYSTERY_YPAD 14  
			
javacall_result ftc_javacall_font_draw(javacall_pixel   color, 
                        int                         clipX1, 
                        int                         clipY1, 
                        int                         clipX2, 
                        int                         clipY2,
                        javacall_pixel*             destBuffer, 
                        int                         destBufferHoriz, 
                        int                         destBufferVert,
                        int                         x, 
                        int                         y, 
                        const javacall_utf16*     text, 
                        int                         textLen){

  int i;
	if (_ftc_use_internal_font) {
		return JAVACALL_FAIL; }
	if (!_ftc_initialized) {
		if (init_font_cache_subsystem()) {
			return JAVACALL_FAIL; } }  
  if (current_ic.face_id==NULL) {
  	return JAVACALL_FAIL; }
	unsigned int glyph_idx;
	int spc_gap = (current_ic.width==0 ? current_ic.height : current_ic.width) / 2;
  for(i=0; i<textLen; i++) {
		FTC_SBit irec;
		if (text[i]==0x0020) {
			// Space. Add half an M-width (for now)
			// TODO: Handle other whitespace codes
			x+=spc_gap;
			continue; }
		glyph_idx = FTC_CMapCache_Lookup(cmap_cache, current_ic.face_id, 0, text[i]);
		if (!glyph_idx) {
			continue; }
		if (FTC_SBitCache_LookupScaler(sbit_cache, &current_ic, FT_LOAD_RENDER,
			glyph_idx, &irec, (FTC_Node*)NULL)) {
			continue; }
		draw_small_bitmap(irec, color, clipX1, clipY1, clipX2, clipY2,
        	               destBuffer,
        	               destBufferHoriz, destBufferVert, x, 
        	               y - irec->top + _MYSTERY_YPAD, irec->format);
		x += irec->width; }
	return JAVACALL_OK; }

#define _MIN_LEADING 2			

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
	r.face_id = get_face_id(face, style);
	int pix_sz = size_param_to_pixels(size);
	if (!pix_sz) {
		return JAVACALL_FAIL; }
	r.height = pix_sz;
	r.width = 0;
	r.pixel = 1;
	FT_Size sz;
	if (FTC_Manager_LookupSize(cache_manager, &r, &sz)) {
		return JAVACALL_FAIL; }
	*descent= sz->metrics.descender / 64;
	if ((*descent)<0) {
		(*descent) *= -1; }	
	*ascent = (sz->metrics.ascender / 64)+(*descent);	
	*leading=_MIN_LEADING;
	return JAVACALL_OK; }

/* Call the set the current font settings */
javacall_result ftc_javacall_font_set_font( javacall_font_face face, 
                                        javacall_font_style style, 
                                        javacall_font_size size) {
	if (_ftc_use_internal_font) {
		return JAVACALL_FAIL; }
	if (!_ftc_initialized) {
		if (init_font_cache_subsystem()) {
			return JAVACALL_FAIL; } }
	current_ic.face_id = get_face_id(face, style);
	int pix_sz = size_param_to_pixels(size);
	if (!pix_sz) {
		return JAVACALL_FAIL; }
	current_ic.height = pix_sz;
	current_ic.width = 0;
	return JAVACALL_OK; }


int ftc_javacall_font_get_width(javacall_font_face     face, 
                            javacall_font_style    style, 
                            javacall_font_size     size,
                            const javacall_utf16* charArray, 
                            int                    charArraySize) {
	if (_ftc_use_internal_font) {
		return -1; }
	if (!_ftc_initialized) {
		if (init_font_cache_subsystem()) {
			return -1; } }

	FTC_ScalerRec tmp_ic;
	tmp_ic.face_id = get_face_id(face, style);
	tmp_ic.pixel=1;
	int pix_sz = size_param_to_pixels(size);
	if (!pix_sz) {
		return -1; }
	tmp_ic.height = pix_sz;
	tmp_ic.width = 0;
	int i, res;
	unsigned int glyph_idx;
	res=0;
	int spc_gap = (tmp_ic.width==0 ? tmp_ic.height : tmp_ic.width) / 2;
	for(i=0; i<charArraySize; i++) {
		if (charArray[i]==0x0020) {
			// Space. Add half an M-width (for now)
			// TODO: Handle other whitespace codes
			res+=spc_gap;
			continue; }
		FTC_SBit irec;
		glyph_idx = FTC_CMapCache_Lookup(cmap_cache, tmp_ic.face_id, -1, charArray[i]);
		if (!glyph_idx) {
			continue; }
		if (FTC_SBitCache_LookupScaler(sbit_cache, &tmp_ic, FT_LOAD_RENDER, glyph_idx, &irec, NULL)) {
			res += tmp_ic.width;
			continue; }
		res += irec->width; }
	return res; }

// Call to set the local static font sizes, use internal
void init_from_config() {
	char* str;
	if (JAVACALL_OK == javacall_get_property("com.pspkvm.font.internal", 
		JAVACALL_INTERNAL_PROPERTY, &str)) {
		if (str && !strcmp(str, "true")) {
			_ftc_use_internal_font = 1; } }
	if (_ftc_use_internal_font) {
		// Done
		return; }
	if (JAVACALL_OK == javacall_get_property("com.pspkvm.fontsize.small",
			JAVACALL_INTERNAL_PROPERTY, &str)) {
	  if (str) {
			_ftc_small = atoi(str); } }
  if (JAVACALL_OK == javacall_get_property("com.pspkvm.fontsize.med",
		JAVACALL_INTERNAL_PROPERTY, &str)) {
	  if (str) {
			_ftc_medium = atoi(str); } }
	if (JAVACALL_OK == javacall_get_property("com.pspkvm.fontsize.large",
		JAVACALL_INTERNAL_PROPERTY, &str)) {
		if (str) {
			_ftc_large = atoi(str); } }
	_ftc_small = (_ftc_small>0 ? _ftc_small : 14); 
	_ftc_medium = (_ftc_medium>0 ? _ftc_medium : 16);
	_ftc_large = (_ftc_large>0 ? _ftc_large : 18); }

/***
 *** Top level manager support 
 ***/

/** Face retrieval callback */
FT_CALLBACK_DEF( FT_Error )  
fts_face_requester(
	FTC_FaceID  face_id,
	FT_Library  lib,                     
	FT_Pointer  request_data,                     
	FT_Face*    aface)  {    

	FT_Error error;

	FT_UNUSED( request_data );
	return FT_New_Face(lib, (const char*)face_id, 0, aface); }

/** Init call */
FT_Error init_font_cache_subsystem() {
	FT_Error err;
	init_from_config();
	current_ic.face_id=NULL;
	current_ic.height=_ftc_medium;
	current_ic.pixel=1;
	if (_ftc_use_internal_font) {
		// Don't need all this cache stuff, in this case
		return 1; }
	err = FT_Init_FreeType(&_ftc_library);
	if (err) {
		return err; }
	err = FTC_Manager_New(_ftc_library, 0, 0, 0,
		fts_face_requester, (FT_Pointer)NULL, &cache_manager);
	if (err) {
		return err; }
 	err = FTC_SBitCache_New(cache_manager, &sbit_cache);
	if (err) {
		return err; }
	err = FTC_CMapCache_New(cache_manager, &cmap_cache);
	if (err==0) {
		_ftc_initialized=1; }
	return err; }

/** Face retrieval support */

/* Local constant array--used by get_face_filename
   these are the (inconstant, canonical) face IDs, as well*/
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
		return; }
  // Handle rendering anti-aliased fonts
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
