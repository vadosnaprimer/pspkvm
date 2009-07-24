#ifndef _ft_support_h
#define _ft_support_h

#include "javacall_font.h"
#include "javacall_properties.h"
#include <ft2build.h>
#include FT_FREETYPE_H    
#include FT_GLYPH_H
#include FT_CACHE_H

#ifdef __cplusplus
extern "C" {
#endif

/***
 *** The critical interfaces:
 */
 
javacall_result ftc_javacall_font_set_font( javacall_font_face face, 
                                        javacall_font_style style, 
                                        javacall_font_size size);
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
                        int                         textLen);                                      
javacall_result ftc_javacall_font_get_info( javacall_font_face  face, 
                                        javacall_font_style style, 
                                        javacall_font_size  size, 
                                        /*out*/ int* ascent,
                                        /*out*/ int* descent,
                                        /*out*/ int* leading);
int ftc_javacall_font_get_width(javacall_font_face     face, 
                            javacall_font_style    style, 
                            javacall_font_size     size,
                            const javacall_utf16* charArray, 
                            int                    charArraySize);
															                             
#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* ifndef _ft_support_h */

