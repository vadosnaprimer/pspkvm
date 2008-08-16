/*
 *   
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

#include <kni.h>
#include <gxj_putpixel.h>
#include <javacall_font.h>

/* convert color to 16bit color */
/*
#define RGB24TORGB16(x) (((( x ) & 0x00F80000) >> 8) + \
                         ((( x ) & 0x0000FC00) >> 5) + \
			 ((( x ) & 0x000000F8) >> 3) )
*/
#define RGB24TORGB16(x) (((( x ) & 0x00F80000) >> 19) + \
                             ((( x ) & 0x0000FC00) >> 5) + \
                             ((( x ) & 0x000000F8) << 8) )

/**
 * @file
 *
 * platform dependent character drawing
 */
 
int gxjport_draw_chars(int pixel, const jshort *clip, void *dst, int dotted,
                       int face, int style, int size,
                       int x, int y, int anchor,
                       const jchar *chararray, int n) {
    gxj_screen_buffer * dest = (gxj_screen_buffer *)dst;
    
    if (JAVACALL_OK != javacall_font_set_font(face, style, size))
    	return KNI_FALSE;
    
    if (JAVACALL_OK != javacall_font_draw(RGB24TORGB16(pixel), clip[0], clip[1], clip[2], clip[3], 
    	                              dest->pixelData, dest->width, dest->height, x, y,
    	                              chararray, n))
    	return KNI_FALSE;
    
    return KNI_TRUE;
}                           

int gxjport_get_font_info(int face, int style, int size,
                          int *ascent, int *descent, int *leading) {
    if (JAVACALL_OK == javacall_font_get_info(face, style, size, ascent, descent, leading))
    	return KNI_TRUE;
    else
    	return KNI_FALSE;
}                            

int gxjport_get_chars_width(int face, int style, int size,
                            const jchar *charArray, int n) {
    return javacall_font_get_width(face, style, size, charArray, n);
}
