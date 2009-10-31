#include "ft_support.h"
#include "alpha_blend.h"
#include <unistd.h>

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
static FTC_Manager cache_manager = 0;
static FTC_CMapCache cmap_cache;
static FTC_SBitCache sbit_cache;

/* State: lib, cache managers have been initialized */
static int _ftc_initialized=0;
/* State: use internal font flag (bypass freetype entirely) */
static int _ftc_use_internal_font = 0;
/* State: need utility font (important if _ftc_use_internal_font is 0) */
static int _ftc_need_utility_font = 0;
/* State -- utility font present */
static int _ftc_utility_font_present = 0;
/* State -- set to 1 if no system fonts whatsoever 
	(not including utility, fallback) are present */
static int _ftc_no_sys_font_present = 0;
/* State: static font sizes */
static int _ftc_small = 0;
static int _ftc_medium = 0;
static int _ftc_large = 0;
/* State--configuration already read (we don't reread while running ...
	even after sleep/wake--changing this stuff really upsets some MIDlets) */
static int _ftc_config_read = 0;

/* Reserve space in the cache manager for as many faces
	as can possibly be in flight (13)
	(Memory card I/O on the PSP can be painfully slow) */
static const int _ftc_face_max = 13;
/* Marker to indicate if the fallback face is present */
static int fallback_font_present = 0;

/* State: current font settings */
static FTC_ScalerRec current_ic;
static FTC_ScalerRec current_fallback_ic;

/** Face retrieval support */

/* Local constant array--used by initialize_face_ids()
	and assigned (if the corresponding file is present)
	to ftc_faceids[x] slots, then returned by get_face_id(...)
	on demand. These are the (constant, canonical) face IDs. */
const char* _typeface_filenames[] = {
	"sys.ttf", "prop.ttf", "mono.ttf",
	"sys_i.ttf", "prop_i.ttf", "mono_i.ttf",
	"sys_b.ttf", "prop_b.ttf", "mono_b.ttf",  
	"sys_ib.ttf", "prop_ib.ttf", "mono_ib.ttf" };
/* Backup font filename */
const char* _fallback_fn = "fallback.ttf";

/** Utility font filename */
const char* _utility_typeface_fn = "utility.sym";
/** Utility font size */
const int _UTILITY_PIXEL_SIZE = 13;

// Useful constant -- 3 faces x 4 variations
#define _FTC_FACEID_COUNT 12
	
/* Ptr aliases, after resolving for fallback due to absent files--
	point to _typeface_filenames values, according to which
	are actually there. */
FTC_FaceID ftc_faceids[_FTC_FACEID_COUNT];
	
// Support for initialize_face_ids() ... picks closest present font to i
// using the fallback array.
FTC_FaceID pick_closest_present_font(int i, int font_file_present[]) {
	/* Fallback routing for each of the canonical faces--
			each points to the next one to try from here.
			When you route back to 0 (sys plain) and it's not
			there either, you're done */
	static const int ftc_filename_fallbacks[] = {
		0, 0, 0,
		0, 3, 2,
		0, 6, 2,
		3, 4, 5 };
	if ((font_file_present[i])!=0) { 
		return (FTC_FaceID)(_typeface_filenames[i]); }
	if (i==0) {
		// No fallback from here
		return (FTC_FaceID)NULL; }
	// Otherwise, recurse
	return pick_closest_present_font(ftc_filename_fallbacks[i], font_file_present); }

// Call to set up the face IDs--called from call initing caches--if they're
// present, so is this lookup array.
void initialize_face_ids() {
	// Field for which font files are present--just do one quick lookup for each
	int font_file_present[_FTC_FACEID_COUNT];
	int i;
	_ftc_no_sys_font_present = 1;
	for(i=0;i<_FTC_FACEID_COUNT;i++) {
		font_file_present[i]=(access((_typeface_filenames[i]), R_OK) == 0) ? 1 : 0;
		if (font_file_present[i]) {
			_ftc_no_sys_font_present = 0; } }
	if (_ftc_no_sys_font_present) {
		// None present anyway. Not much point to doing all this.
		return; }
	// Now, for each of the members of ftc_faceids, go through the fallback
	// routing, and pick the closest match (or NULL, if none)
	for(i=0;i<_FTC_FACEID_COUNT;i++) {
		ftc_faceids[i]=pick_closest_present_font(i, font_file_present); } }
		
// Call to initialize the 'last resort' fallback system
void initialize_fallback_system() {
	fallback_font_present = (access((_fallback_fn), R_OK) == 0) ? 1 : 0; }

// Resolve face specs to an FTC_FaceID
// for requesting faces from the cache system.
FTC_FaceID get_face_id(javacall_font_face face,
	javacall_font_style style) {
	if (face==JAVACALL_FONT_FACE_UTILITY) {
		return (FTC_FaceID)_utility_typeface_fn; }
	int idx = 0;
	switch(face) {
		case JAVACALL_FONT_FACE_PROPORTIONAL: idx=1; break;
		case JAVACALL_FONT_FACE_MONOSPACE: idx=2; break; }
	if ((style&JAVACALL_FONT_STYLE_ITALIC)!=0) {
		idx+=3; }
  if ((style&JAVACALL_FONT_STYLE_BOLD)!=0) {
		idx+=6; }
	return ftc_faceids[idx]; }
	
/* Call to reset the cache manager--called to 
	prevent dropped glyphs after sleep/resume cycle */
void invalidate_ftc_manager() {
	// Setting these to zero forces a whole
	// init/config read regardless of the entry point
	// into these calls after this one.
	_ftc_use_internal_font = 0;
	_ftc_need_utility_font = 0;
	_ftc_config_read = 0;
	if (!_ftc_initialized) {
		return; }
	FTC_Manager_Done(cache_manager);
	FT_Done_FreeType(_ftc_library);
	_ftc_initialized=0; }
	
// Externs below are from the ext/pspkvm vmsettings store
// TODO: Do a proper include. But it's a pain.

// Get an integer or default (if there's no such key or it
// doesn't parse as an int)
extern int vmsettings_getint(const char* k, int d);

// Returns true if value v exists for key k and v equals cmp	
extern int vmsettings_key_equals(const char* k, const char* cmp);

// End ext/pspkvm vmsettings store interface

// Read a font size setting (from vmconfig)-- pass in the config name,
// and a fallback
int ftc_pixsize_fr_vmconfig(const char* cfgname, int default_sz) {
	return vmsettings_getint(cfgname, default_sz); }

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
			
// Call to find out if we need the utility font
// Right now, this is just if they're using the semichordal board
int get_need_utility_font_vmconfig() {
	if (vmsettings_key_equals("com.pspkvm.inputmethod", "semichordal")) {
		return 1; }
	return vmsettings_key_equals("com.pspkvm.inputmethod", "danzeff"); }

// Call to find out if the internal font is set
int get_internal_font_set_vmconfig() {
	return vmsettings_key_equals("com.pspkvm.font.internal", "on"); }

// Call to set the local static font sizes, and the _ftc_use_internal_font flag
// from the system config.
void init_from_config() {
	if (_ftc_config_read) {
		return; }
	_ftc_use_internal_font = get_internal_font_set_vmconfig();
	_ftc_need_utility_font = get_need_utility_font_vmconfig();
	if (_ftc_use_internal_font) {
		// Done
		_ftc_config_read = 1;
		return; }
	_ftc_small = ftc_pixsize_fr_vmconfig("com.pspkvm.fontsize.small", 14);
	_ftc_medium = ftc_pixsize_fr_vmconfig("com.pspkvm.fontsize.med", 16);
	_ftc_large = ftc_pixsize_fr_vmconfig("com.pspkvm.fontsize.large", 18);
	_ftc_config_read = 1; }

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
	r->height = face==JAVACALL_FONT_FACE_UTILITY ?
		_UTILITY_PIXEL_SIZE :  
		size_param_to_pixels(size);
	r->width = 0;
	r->pixel = 1; }

// Utility inline to set an FTC_ScalerRec structure consistently
// from the 'last resort' fallback  
inline void set_fallback_scaler_rec(FTC_ScalerRec* r,
                     javacall_font_face face, 
                     javacall_font_size size) {
	r->face_id = (FTC_FaceID)_fallback_fn;
	r->height = face==JAVACALL_FONT_FACE_UTILITY ?
		_UTILITY_PIXEL_SIZE :  
		size_param_to_pixels(size);
	r->width = 0;
	r->pixel = 1; }
	
// Find out how many faces we need to allocate
int get_faces_to_allocate() {
	if (_ftc_use_internal_font) {
		// Just need the utility font--that's the only way to get here
		return 1; }
	int r = _ftc_face_max;
	if (fallback_font_present) {
		r++; }
	if (_ftc_need_utility_font) {
		r++; }
	return r; }

/** Init call--initializes the lib and the various cache managers */
FT_Error init_font_cache_subsystem() {
	FT_Error err;
	init_from_config();
	if ((_ftc_use_internal_font) && (!_ftc_need_utility_font)) {
		// Don't need all this cache stuff, in this case
		return 1; }
	initialize_face_ids();
	if ((_ftc_no_sys_font_present) && (!_ftc_need_utility_font)) {
		// Don't need them in this case either.
		return 1; }
	initialize_fallback_system();
	_ftc_utility_font_present = (access(_utility_typeface_fn, R_OK) == 0) ? 1 : 0;
	set_scaler_rec(&current_ic, JAVACALL_FONT_FACE_SYSTEM,
					JAVACALL_FONT_STYLE_PLAIN,
					JAVACALL_FONT_SIZE_MEDIUM);
	if (fallback_font_present) {
		set_fallback_scaler_rec(&current_fallback_ic, JAVACALL_FONT_FACE_SYSTEM,
				JAVACALL_FONT_SIZE_MEDIUM); }
	// Lib inits
	err = FT_Init_FreeType(&_ftc_library);
	if (err) {
		return err; }
	// Top level cache manager
	err = FTC_Manager_New(_ftc_library, get_faces_to_allocate(), 0, 0,
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
		
// Init/check init--returns 1 only if we're good to go
int _ftc_check_init(javacall_font_face lface) {
	if ((_ftc_use_internal_font) && (!_ftc_need_utility_font)) {
		return 0; }
	if ((_ftc_use_internal_font) && (lface != JAVACALL_FONT_FACE_UTILITY)) {
		return 0; }
	if ((_ftc_no_sys_font_present) && (lface != JAVACALL_FONT_FACE_UTILITY)) {
		return 0; }
	if (!_ftc_initialized) {
		if (init_font_cache_subsystem()) {
			return 0; } }
	if ((_ftc_use_internal_font) && (lface != JAVACALL_FONT_FACE_UTILITY)) {
		return 0; }
	if ((!_ftc_utility_font_present) && (lface == JAVACALL_FONT_FACE_UTILITY)) {
		return 0; }
	return 1; }

// Same as above, when we don't know the face
int _ftc_check_init_noface() {
	if ((_ftc_use_internal_font || _ftc_no_sys_font_present) && (!_ftc_need_utility_font)) {
		return 0; }
	if (!_ftc_initialized) {
		if (init_font_cache_subsystem()) {
			return 0; } }
	return 1; }

// Convenient local--takes two FTC_ScalerRec pointers (first preferred, second fallback),
// sets a pointer to a pointer to indicate which one you call FTC_SBitCache_LookupScaler
// with after, and returns the glyph index to use.
FT_UInt cmap_cache_lookup_fb(FTC_CMapCache  cache,
	FTC_ScalerRec* primary,
	FTC_ScalerRec* fallback,
	FTC_ScalerRec** selected_rec,
	FT_UInt32 char_code) {
	
	FT_UInt r = FTC_CMapCache_Lookup(cache, primary->face_id, -1, char_code);
	if (r) {
		*selected_rec = primary;
		return r; }
	if (!fallback_font_present) {
		*selected_rec = primary;
		return r; }
	*selected_rec = fallback;
	return FTC_CMapCache_Lookup(cmap_cache, fallback->face_id, -1, char_code); }

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
  if (!_ftc_check_init_noface()) {
		return JAVACALL_FAIL; }
  if ((current_ic.face_id)==NULL) {
  	return JAVACALL_FAIL; }
  if ((current_ic.face_id == _utility_typeface_fn) &&
  	(!_ftc_utility_font_present)) {
			return JAVACALL_FAIL; }
	if ((current_ic.face_id != _utility_typeface_fn) &&
		(_ftc_use_internal_font)) {
			return JAVACALL_FAIL; }
	unsigned int glyph_idx;
	FTC_SBit irec;
	FTC_ScalerRec* selected_ic;
  for(i=0; i<textLen; i++) {
		glyph_idx = cmap_cache_lookup_fb(cmap_cache, &current_ic, &current_fallback_ic,
			&selected_ic, text[i]);
		if (FTC_SBitCache_LookupScaler(sbit_cache, selected_ic, FT_LOAD_DEFAULT,
			glyph_idx, &irec, (FTC_Node*)NULL)) {
			return JAVACALL_FAIL; }
		draw_small_bitmap(irec, color, clipX1, clipY1, clipX2, clipY2,
       destBuffer,
       destBufferHoriz, destBufferVert, x, 
       y - irec->top + current_ic.height - 2, irec->format);
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
	if (!_ftc_check_init(face)) {
		return JAVACALL_FAIL; }
	FTC_ScalerRec r;
	set_scaler_rec(&r, face, style, size);
	if ((r.face_id)==NULL) {
		return JAVACALL_FAIL; }
	FT_Size sz;
	if (FTC_Manager_LookupSize(cache_manager, &r, &sz)) {
		return JAVACALL_FAIL; }
	int a = FT_MulFix( sz->face->ascender, sz->metrics.y_scale ) / 64;
	int d = FT_MulFix( sz->face->descender, sz->metrics.y_scale) / 64;	
	if (d<0) {
		d *= -1; }
	if (a<0) {
		a *= -1; }
	(*ascent)=a;
	(*descent)=d;
	(*leading)=(_MIN_LEADING+r.height)-(a+d);
	return JAVACALL_OK; }

// Direct interface implementation
javacall_result ftc_javacall_font_set_font( javacall_font_face face, 
                                        javacall_font_style style, 
                                        javacall_font_size size) {
	if (!_ftc_check_init(face)) {
		return JAVACALL_FAIL; }
	set_scaler_rec(&current_ic, face, style, size);
	if ((current_ic.face_id)==NULL) {
		return JAVACALL_FAIL; }
	if (fallback_font_present) {
		set_fallback_scaler_rec(&current_fallback_ic, face, size);
		if ((current_fallback_ic.face_id)==NULL) {
			return JAVACALL_FAIL; } }
	return JAVACALL_OK; }
	
// Direct interface implementation
int ftc_javacall_font_get_width(javacall_font_face face, 
                            javacall_font_style style, 
                            javacall_font_size size,
                            const javacall_utf16* charArray, 
                            int charArraySize) {
	if (!_ftc_check_init(face)) {
		return -1; }
	FTC_ScalerRec tmp_ic, tmp_fallback_ic;
	FTC_ScalerRec* selected_ic;
	set_scaler_rec(&tmp_ic, face, style, size);
	if ((tmp_ic.face_id)==NULL) {
		return -1; }
	if (fallback_font_present) {
		set_fallback_scaler_rec(&tmp_fallback_ic, face, size);
		if (tmp_fallback_ic.face_id==NULL) {
			return -1; } }
	int i, res;
	unsigned int glyph_idx;
	res=0;
	FTC_SBit irec;
	for(i=0; i<charArraySize; i++) {
		glyph_idx = cmap_cache_lookup_fb(cmap_cache, &tmp_ic, &tmp_fallback_ic,
			&selected_ic, charArray[i]);
		if (FTC_SBitCache_LookupScaler(sbit_cache, selected_ic, FT_LOAD_DEFAULT,
			glyph_idx, &irec, (FTC_Node*)NULL)) {
			return -1; }
		res += irec->xadvance; }
	return res; }
