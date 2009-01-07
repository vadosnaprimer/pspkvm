/*
 *
 * Copyright  1990-2007 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, CA 95054 or visit www.sun.com if you need additional
 * information or have any questions.
 */


#ifdef __cplusplus
extern "C" {
#endif
    
    
#include "javacall_font.h"
#include "javacall_properties.h"
#include <ft2build.h>
#include FT_FREETYPE_H    
#include FT_GLYPH_H
#include FT_CACHE_H

#define CACHE_SIZE 128
#define CACHE2_SIZE 1024
#define WCACHE_SIZE 4096

typedef struct {
    int width;
    int top;
    FT_Bitmap* bitmap;
    FT_Pixel_Mode mode;
}font_cache;

typedef struct {
    javacall_utf16 charcode;
    unsigned short width;
}wcache2;

typedef struct {
    javacall_utf16 charcode;
    int width;
    int top;
    FT_Bitmap* bitmap;    
    FT_Pixel_Mode mode;
}bcache2;

typedef struct {
    FT_Face face;
    //int width_cache[CACHE_SIZE];
    //FT_BitmapGlyph bitmap_cache[CACHE_SIZE];
    font_cache asc_cache[CACHE_SIZE];
    wcache2 width_cache2[WCACHE_SIZE];
    bcache2 bitmap_cache2[CACHE2_SIZE];
    int wcache2_p;
    int bcache2_p;
    javacall_font_face iface;
    javacall_font_style istyle;
    javacall_font_size isize;
    int facesize;
}javacall_font;

static javacall_font* current_font = NULL;
static FT_Library  library = NULL;
//static FTC_Manager cache_manager;
//static FTC_CMapCache cmap_cache;
//static FTC_ImageCache image_cache;
//static FTC_SBitCache sbit_cache;

#define ALPHA_BLEND \
*point = *fontpoint>128?color:(*fontpoint>1?(javacall_pixel)((((unsigned long) color + (unsigned long) *point) ^  \
    	                                                    (unsigned long)((color ^ *point) & 0x0821))  \
    	                                                     >> 1):*point);point++;fontpoint++;

FT_CALLBACK_DEF( FT_Error )  
my_face_requester( FTC_FaceID  face_id,
	FT_Library  lib,                     
	FT_Pointer  request_data,                     
	FT_Face*    aface )  {    

	FT_Error error;
	javacall_font*  font = (javacall_font*)face_id;    
	FT_UNUSED( request_data );    
	
	*aface = font->face;
	return 0;
}

//static FT_UInt  get_glyph_index( FTC_FaceID font, FT_UInt32  charcode )  {
//	return FTC_CMapCache_Lookup( cmap_cache, font, 0, charcode );  
//}

static javacall_font* getFont( javacall_font_face face, 
                                        javacall_font_style style, 
                                        javacall_font_size size) {
    static javacall_font* fonts[3][4][3] = {NULL};
    FT_Face     newface;
    javacall_font* newfont;
    int iface, istyle, isize;
    char* facename;
    int facestyle, facesize;
    char* str;
    static int initialized = 0;
    static int use_internal_font = -1;

    if (use_internal_font < 0) {
    	 use_internal_font = 0;
        if (JAVACALL_OK == javacall_get_property("com.pspkvm.font.internal", JAVACALL_INTERNAL_PROPERTY, &str)) {
    	    if (str && !strcmp(str, "true")) {
    	        use_internal_font = 1;
    	    }
    	}
    }
    
    if (use_internal_font == 1) {
        return NULL;
    }
    
    switch(face) {
    	case JAVACALL_FONT_FACE_SYSTEM: iface = 0;
    	break;
    	case JAVACALL_FONT_FACE_MONOSPACE: iface = 1;
    	break;
    	case JAVACALL_FONT_FACE_PROPORTIONAL: iface = 2;
    	break;
    	default:
    	return NULL;
    }
    switch(style) {
    	case JAVACALL_FONT_STYLE_PLAIN: istyle = 0;
    	break;
    	case JAVACALL_FONT_STYLE_BOLD: istyle = 0;   //Don't support BOLD for now
    	break;
    	case JAVACALL_FONT_STYLE_ITALIC: istyle = 0;  //Don't support Italic for now
    	break;
    	case JAVACALL_FONT_STYLE_UNDERLINE: istyle = 0;  //Don't support Underline for now
    	break;
    	default:
    	return NULL;
    }
    switch(size) {
    	case JAVACALL_FONT_SIZE_SMALL: isize = 0;
    	break;
    	case JAVACALL_FONT_SIZE_MEDIUM: isize = 1;
    	break;
    	case JAVACALL_FONT_SIZE_LARGE: isize = 2;
    	break;
    	default:
    	return NULL;
    }

    newfont = fonts[iface][istyle][isize];

    if (newfont == (javacall_font*)-1) {
    	return NULL;
    }

    if (newfont != NULL) {
    	return newfont;
    }

    
    switch(face) {
    	case JAVACALL_FONT_FACE_SYSTEM: facename = "sys.ttf";
    	break;
    	case JAVACALL_FONT_FACE_MONOSPACE: facename = "mono.ttf";
    	break;
    	case JAVACALL_FONT_FACE_PROPORTIONAL: facename = "pro.ttf";
    	break;
    	default:
    	return NULL;
    }
    switch(style) {
    	case JAVACALL_FONT_STYLE_PLAIN: facestyle = 0;
    	break;
    	case JAVACALL_FONT_STYLE_BOLD: facestyle = 1;
    	break;
    	case JAVACALL_FONT_STYLE_ITALIC: facestyle = 2;
    	break;
    	case JAVACALL_FONT_STYLE_UNDERLINE: facestyle = 3;
    	break;
    	default:
    	return NULL;
    }

    {
    	int smallsize = 0;
    	int medsize = 0;
    	int largesize = 0;
    	
    	if (JAVACALL_OK == javacall_get_property("com.pspkvm.fontsize.small", JAVACALL_INTERNAL_PROPERTY, &str)) {
    	    if (str) smallsize = atoi(str);
    	}
    	if (JAVACALL_OK == javacall_get_property("com.pspkvm.fontsize.med", JAVACALL_INTERNAL_PROPERTY, &str)) {
    	    if (str) medsize = atoi(str);
    	}
    	if (JAVACALL_OK == javacall_get_property("com.pspkvm.fontsize.large", JAVACALL_INTERNAL_PROPERTY, &str)) {
    	    if (str) largesize = atoi(str);
    	}
    	
       switch(size) {
    	case JAVACALL_FONT_SIZE_SMALL: facesize = smallsize>0 ? smallsize:14;
    	break;
    	case JAVACALL_FONT_SIZE_MEDIUM: facesize = medsize>0 ? medsize:16;
    	break;
    	case JAVACALL_FONT_SIZE_LARGE: facesize = largesize>0 ? largesize:18;
    	break;
    	default:
    	return NULL;
       }
    }


    if (!initialized) { 
        if (FT_Init_FreeType( &library )) {
        	javacall_print("Freetype2 initialize failed\n");
        	goto fail;
        }
/*
        if (FTC_Manager_New( library, 0, 0, 0,  
        	                                   my_face_requester, 0, &cache_manager )) {                            
        	javacall_print( "could not initialize cache manager\n" );    
        	goto fail;
        }

        if (FTC_SBitCache_New( cache_manager, &sbit_cache )) {        
        	javacall_print( "could not initialize small bitmaps cache\n" );    
        	goto fail;
        }

        if (FTC_ImageCache_New( cache_manager, &image_cache )) {   
        	javacall_print( "could not initialize glyph image cache\n" );    
        	goto fail;
        }
        
        if (FTC_CMapCache_New( cache_manager, &cmap_cache )) {
        	javacall_print( "could not initialize charmap cache\n" );
        	goto fail;
        }
*/
        initialized = 1;
    }

    if (FT_New_Face( library, facename, 0, &newface )) {
        javacall_printf("Freetype2: Create font face failed: %s\n", facename);
        goto fail;
    }
    
    if (FT_Set_Pixel_Sizes( newface, 0, facesize)) {
    	 javacall_printf("Freetype2: Set font size failed: %s, %d\n", facename, facesize);
    	 FT_Done_Face(newface);        
    	 goto fail;
    }
    
    int i;
    newfont = (javacall_font*)malloc(sizeof(javacall_font));

    for (i = 0; i < CACHE_SIZE; i++) {
    	 FT_UInt  glyph_index;
    	 FT_Glyph  glyph;
    	 
        if (((glyph_index = FT_Get_Char_Index( newface, i )) == 0) && (i == (int)'a')) {
        	free(newfont);
        	FT_Done_Face(newface);
        	javacall_printf("FT_Get_Char_Index returns 0: %s\n", facename);
        	goto fail;
        }
        FT_Load_Glyph( newface, glyph_index, FT_LOAD_DEFAULT );
        
        newfont->asc_cache[i].width = newface->glyph->metrics.horiAdvance / 64;
        
        //if (newface->glyph->format == FT_GLYPH_FORMAT_BITMAP) {
        //    FT_Bitmap* p = (FT_Bitmap*)malloc(sizeof(FT_Bitmap));
        //    memcpy(p, &newface->glyph->bitmap, sizeof(FT_Bitmap));                        
        //    newfont->asc_cache[i].bitmap = p;
        //    newfont->asc_cache[i].top = newface->glyph->bitmap_top;
        //    if (i == (int)'a') printf("FT_GLYPH_FORMAT_BITMAP:%s, %d, %d, %d, %d\n", facename, facesize, p->rows, p->width, p->pitch);
        //} else {
        	{
            FT_Get_Glyph( newface->glyph, &glyph );
            
            FT_Glyph_To_Bitmap(&glyph, FT_RENDER_MODE_NORMAL, NULL, 1);
            newfont->asc_cache[i].top = ((FT_BitmapGlyph)glyph)->top;
            newfont->asc_cache[i].bitmap = &((FT_BitmapGlyph)glyph)->bitmap;
            newfont->asc_cache[i].mode = ((FT_BitmapGlyph)glyph)->bitmap.pixel_mode;
            //FT_Bitmap_Done(library, &((FT_BitmapGlyph)glyph)->bitmap);
            //FT_Done_Glyph(glyph);
        }
    }

    for (i = 0; i < CACHE2_SIZE; i++) {
        newfont->bitmap_cache2[i].bitmap = (FT_Bitmap*)0;
        newfont->bitmap_cache2[i].charcode = (javacall_utf16)0;
        newfont->bitmap_cache2[i].width = 0;
        newfont->bitmap_cache2[i].mode = 0;
    }

    for (i = 0; i < WCACHE_SIZE; i++) {
    	 newfont->width_cache2[i].width = 0;
        newfont->width_cache2[i].charcode = (javacall_utf16)0;
    }

    newfont->face = newface;
    newfont->iface = face;
    newfont->istyle = style;
    newfont->isize = size;
    newfont->facesize = facesize;
    newfont->wcache2_p = 0;
    newfont->bcache2_p = 0;
    
    fonts[iface][istyle][isize] = newfont;
    return newfont;

fail:
    fonts[iface][istyle][isize] = (javacall_font*)(-1);
    return NULL;
}

static void draw_bitmap( FT_Bitmap*  bitmap, javacall_pixel color,
	                             int clipX1, int clipY1, int clipX2, int clipY2,
	                             javacall_pixel* destbuf, 
	                             int w, int h, int x, int y, FT_Pixel_Mode mode) {
    static unsigned char BitMask[8] = {0x80,0x40,0x20,0x10,0x8,0x4,0x2,0x1};
    FT_Int  i, j;  
    FT_Int  x_max = x + bitmap->width;  
    FT_Int  y_max = y + bitmap->rows;
    FT_Int xoffset, yoffset, xnum, ynum;

    if ((clipX1 > x_max) || (clipX2 < x) || (clipY1 > y_max) || (clipY2 < y)) {
    	return;
    }

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
    	         	bitOffset %= 8;
    	         }
    	         if (*fontpoint & BitMask[bitOffset]) {
    	             *point = color;
    	         }
    	         point++;
    	     }
    	     point += (w - xnum);
    	     bitOffset += (pitch - xnum);
    	 }
    } else {
        unsigned char* fontpoint = &bitmap->buffer[0] + yoffset * bitmap->width + xoffset;
    
        int block = xnum / 8;
        int rem  = xnum % 8;
        for ( j = 0; j < ynum; j++ )  {    	
        	for ( i = 0; i < block ; i++)    {
        		ALPHA_BLEND
        		ALPHA_BLEND
        		ALPHA_BLEND
        		ALPHA_BLEND
        		ALPHA_BLEND
        		ALPHA_BLEND
        		ALPHA_BLEND
        		ALPHA_BLEND
        	}
        	
        	switch (rem) {
        		case 7: ALPHA_BLEND
        	       case 6: ALPHA_BLEND
        	       case 5: ALPHA_BLEND
        	       case 4: ALPHA_BLEND
        	       case 3: ALPHA_BLEND
        	       case 2: ALPHA_BLEND
        	       case 1: ALPHA_BLEND
        	}
        	
        	point += (w - xnum);
        	fontpoint += (bitmap->width - xnum);
        }
    }
}
    
/**
 * Set font appearance params 
 * 
 * @param face The font face to be used
 * @param style The font style to be used
 * @param size The font size to be used
 * @return <tt>JAVACALL_OK</tt> if font set successfully, 
 *         <tt>JAVACALL_FAIL</tt> or negative value otherwise
 */
javacall_result javacall_font_set_font( javacall_font_face face, 
                                        javacall_font_style style, 
                                        javacall_font_size size) {
    javacall_font* font = getFont(face, style, size);
    if (font == NULL) {
    	font = getFont(JAVACALL_FONT_FACE_SYSTEM,
    		                 JAVACALL_FONT_STYLE_PLAIN,
    		                 JAVACALL_FONT_SIZE_MEDIUM);
    	if (font == NULL) {
    	    return JAVACALL_FAIL;
    	}
    }
    current_font = font;
    return JAVACALL_OK;                                        
}
    
    
    
/**
 * Draws the first n characters to Offscreen memory image specified using the current font,
 * color.
 * 
 *
 * @param color color of font
 * @param clipX1 top left X coordinate of the clipping area where the pixel 
 *               (clipX1,clipY1) is the first top-left pixel in the clip area.
 *               clipX1 is guaranteeded to be larger or equal 0 and smaller or equal 
 *               than clipX2.
 * @param clipY1 top left Y coordinate of the clipping area where the pixel 
 *               (clipX1,clipY1) is the first top-left pixel in the clip area.
 *               clipY1 is guaranteeded to be larger or equal 0 and smaller or equal 
 *               than clipY2
 * @param clipX2 bottom right X coordinate of the clipping area where the pixel 
 *               (clipX2,clipY2) is the last bottom right pixel in the clip area.
 *               clipX2 is guaranteeded to be larger or equal than clipX1 and 
 *               smaller or equal than destBufferHoriz.
 * @param clipY2 bottom right Y coordinate of the clipping area where the pixel 
 *               (clipX2,clipY2) is the last bottom right pixel in the clip area
 *               clipY2 is guaranteeded to be larger or equal than clipY1 and 
 *               smaller or equal than destBufferVert.
 * @param destBuffer  where to draw the chars
 * @param destBufferHoriz horizontal size of destination buffer
 * @param destBufferVert  vertical size of destination buffer
 * @param x The x coordinate of the top left font coordinate 
 * @param y The y coordinate of the top left font coordinate 
 * @param text Pointer to the characters to be drawn
 * @param textLen The number of characters to be drawn
 * @return <tt>JAVACALL_OK</tt> if font rendered successfully, 
 *         <tt>JAVACALL_FAIL</tt> or negative value on error or not supported
 */
javacall_result javacall_font_draw(javacall_pixel   color, 
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
    FT_GlyphSlot  slot;
    
    if (current_font == NULL) {
    	return JAVACALL_FAIL;
    }

    //printf("javacall_font_get_draw\n");

    slot = current_font->face->glyph;

    for (i = 0; i < textLen; i++) {
    	 int top, width;
    	 FT_Bitmap* bitmap;
    	 FT_Render_Mode mode;
    	 
        FT_UInt  glyph_index;
    	 FT_Glyph  glyph = NULL;
    	 int cached = 1;
    	         
    	 if (text[i] < CACHE_SIZE) {
    	     top = current_font->asc_cache[text[i]].top;
    	     bitmap = current_font->asc_cache[text[i]].bitmap;
    	     width = current_font->asc_cache[text[i]].width;
    	     mode = current_font->asc_cache[text[i]].mode;
    	 } else {
    	     int j;
    	     javacall_utf16 cc = text[i];
            for (j = 0; j < CACHE2_SIZE; j++) {
            	 if (current_font->bitmap_cache2[j].charcode == 0) {
            	 	j = CACHE2_SIZE;
            	 	break;
            	 }
            	 
    	        if (current_font->bitmap_cache2[j].charcode == cc) {
    	        	top = current_font->bitmap_cache2[j].top;
    	        	bitmap = current_font->bitmap_cache2[j].bitmap;
    	        	width = current_font->bitmap_cache2[j].width;
    	        	mode = current_font->bitmap_cache2[j].mode;
    	        	break;
    	        }
            }
           
            if ( j >= CACHE2_SIZE) {
    	 
                glyph_index = FT_Get_Char_Index( current_font->face, text[i] );
                if (FT_Load_Glyph( current_font->face, glyph_index, FT_LOAD_DEFAULT )) {
                    return JAVACALL_FAIL;
                }

                //if (current_font->face->glyph->format == FT_GLYPH_FORMAT_BITMAP) {
                //    top = current_font->face->glyph->bitmap_top;
                //    bitmap = &current_font->face->glyph->bitmap;
                //    width = current_font->face->glyph->metrics.horiAdvance / 64;
                //} else {
                	{
                    if (FT_Get_Glyph( current_font->face->glyph, &glyph )) {
                        return JAVACALL_FAIL;
                    }
                    if (FT_Glyph_To_Bitmap(&glyph, FT_RENDER_MODE_NORMAL, NULL, 1)) {
                        FT_Done_Glyph(glyph);
            	          return JAVACALL_FAIL;
                    }
    
                    top = ((FT_BitmapGlyph)glyph)->top;
                    bitmap = &((FT_BitmapGlyph)glyph)->bitmap;
                    width = current_font->face->glyph->metrics.horiAdvance / 64;
                }

                mode = bitmap->pixel_mode;

                int icache = current_font->bcache2_p;
                if (icache < CACHE2_SIZE) {
                  current_font->bcache2_p++;
                  current_font->bitmap_cache2[icache].charcode = cc;
                  current_font->bitmap_cache2[icache].width = width;
                  current_font->bitmap_cache2[icache].top = top;
/*
                  if (current_font->face->glyph->format == FT_GLYPH_FORMAT_BITMAP) {                  
                      FT_Bitmap* pbmp = (FT_Bitmap*)malloc(sizeof(FT_Bitmap));
                      if (!pbmp) {
                      	if (glyph) {
                      	    FT_Done_Glyph(glyph);
                      	    FT_Bitmap_Done(library, bitmap); //we can only free bitmap which created by FT_Glyph_To_Bitmap()
                      	}
                      	return JAVACALL_FAIL; //out of memory
                      }

                      memcpy(pbmp, bitmap, sizeof(FT_Bitmap));                  
                      current_font->bitmap_cache2[icache].bitmap = pbmp;              
                      
                  } else {                  
                  */
			{
                      current_font->bitmap_cache2[icache].bitmap = bitmap;
                  }
                  current_font->bitmap_cache2[icache].mode = mode;
                } else {
                  cached = 0; //cache full
                }
            }            
            
    	 }
        draw_bitmap( bitmap, color,
        	               clipX1, clipY1, clipX2, clipY2,
        	               destBuffer,
        	               destBufferHoriz, destBufferVert, x, 
        	               y - top + 12, mode);
        x += width;
        
        if (!cached) {
        	if (glyph) 
        	    FT_Done_Glyph(glyph);
        	
        	//if (current_font->face->glyph->format != FT_GLYPH_FORMAT_BITMAP)
        	    FT_Bitmap_Done(library, bitmap);
        }
    }
    return JAVACALL_OK;
}
    
    
    
/**
 * Query for the font info structure for a given font specs
 *
 * @param face The font face to be used (Defined in <B>Font.java</B>)
 * @param style The font style to be used (Defined in
 * <B>Font.java</B>)
 * @param size The font size to be used. (Defined in <B>Font.java</B>)
 *
 * @param ascent return value of font's ascent
 * @param descent return value of font's descent
 * @param leading return value of font's leading 
 * 
 * @return <tt>JAVACALL_OK</tt> if successful, <tt>JAVACALL_FAIL</tt> or 
 *         <tt>JAVACALL_FAIL</tt> or negative value on error
 *
 */
javacall_result javacall_font_get_info( javacall_font_face  face, 
                                        javacall_font_style style, 
                                        javacall_font_size  size, 
                                        /*out*/ int* ascent,
                                        /*out*/ int* descent,
                                        /*out*/ int* leading) 
{
    javacall_font* font = getFont(face, style, size);
    if (font == NULL) {
    	font = getFont(JAVACALL_FONT_FACE_SYSTEM,
    		                 JAVACALL_FONT_STYLE_PLAIN,
    		                 JAVACALL_FONT_SIZE_MEDIUM);
    	if (font == NULL) {
    	    return JAVACALL_FAIL;
    	}
    }

    *ascent = font->face->size->metrics.ascender / 64;
    *descent= font->face->size->metrics.descender / 64;
    *leading=-*descent;
    return JAVACALL_OK;
}
        
/**
 * return the char width for the first n characters in charArray if
 * they were to be drawn in the font indicated by the parameters.
 *
 *
 * @param face The font face to be used
 * @param style The font style to be used
 * @param size The font size to be used
 * @param charArray The string to be measured
 * @param charArraySize The number of character to be measured
 * @return total advance width in pixels (a non-negative value)
 */
int javacall_font_get_width(javacall_font_face     face, 
                            javacall_font_style    style, 
                            javacall_font_size     size,
                            const javacall_utf16* charArray, 
                            int                    charArraySize) {

    int i, res;

    //printf("javacall_font_get_width\n");
    javacall_font* font = getFont(face, style, size);
    if (font == NULL) {
    	font = getFont(JAVACALL_FONT_FACE_SYSTEM,
    		                 JAVACALL_FONT_STYLE_PLAIN,
    		                 JAVACALL_FONT_SIZE_MEDIUM);
    	if (font == NULL) {
    	    return -1;
    	}
    }

    res = 0;
    
    for (i = 0; i < charArraySize; i++) {
    	if (charArray[i] < CACHE_SIZE) {
           res += font->asc_cache[charArray[i]].width;
    	} else {
    	    int j;
    	    javacall_utf16 cc = charArray[i];
           for (j = 0; j < WCACHE_SIZE; j++) {
           	 if (font->width_cache2[j].charcode == 0) {
           	 	j = WCACHE_SIZE;
           	 	break;
           	 }
           	 
    	        if (font->width_cache2[j].charcode == cc) {
    	        	res += font->width_cache2[j].width;
    	        	break;
    	        }
           }
           
           if ( j < WCACHE_SIZE) {
           	continue;
           }
           
    	    FT_UInt  glyph_index;
    	    int w;
    	    int icache;
    	
           glyph_index = FT_Get_Char_Index( font->face, cc);
           FT_Load_Glyph( font->face, glyph_index, FT_LOAD_DEFAULT );
           w = font->face->glyph->metrics.horiAdvance / 64;
           res += w;

           icache = font->wcache2_p;
           if (icache < WCACHE_SIZE) {
           	font->wcache2_p++;           
              font->width_cache2[icache].charcode = cc;
              font->width_cache2[icache].width = w;
           }
           
        /*
            FTC_ImageTypeRec image_type;
    	     FT_Glyph glyf;
    	     image_type.flags = FT_LOAD_DEFAULT;
    	     image_type.face_id = (FTC_FaceID)font;
    	     image_type.width = font->facesize;
    	     image_type.height = font->facesize;
    	 
            glyph_index = get_glyph_index((FTC_FaceID)font, charArray[i]);
            FTC_ImageCache_Lookup( image_cache,                                     
                                                   &image_type,
                                                   glyph_index,
                                                   &glyf,
                                                   NULL );
            res += glyf->advance.x / 64;
            */
    	}
    }

    return res;
}
    
    
#ifdef __cplusplus
} //extern "C"
#endif




