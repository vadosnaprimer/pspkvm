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

#include "javacall_lcd.h" 
#include "pspdisplay.h"
#include <stdlib.h>

#ifdef __cplusplus
extern "C" {
#endif

extern const unsigned short DukeTango[];

static unsigned short* vram = (unsigned short*) (0x40000000 | 0x04000000);
static javacall_pixel* scbuff = NULL;
/**
 * The function javacall_lcd_init is called by during Java VM startup, allowing the
 * platform to perform device specific initializations. The function is required to
 * provide the supported screen capabilities:
 * - Display Width
 * - Display Height
 * - Color encoding: Either 32bit ARGB format, 15 bit 565 encoding or 24 bit RGB encoding
 * 
 * \par
 * 
 * Once this API call is invoked, the VM will receive display focus.\n
 * <b>Note:</b> Consider the case where the platform tries to assume control over the
 * display while the VM is running by pausing the Java platform. In this case, the
 * platform is required to save the VRAM screen buffer: Whenever the Java
 * platform is resumed, the stored screen buffers must be restored to original
 * state.
 * 
 * @param screenWidth width of screen
 * @param screenHeight width of screen
 * @param colorEncoding color encoding, one of the following:
 *              -# JAVACALL_LCD_COLOR_RGB565
 *              -# JAVACALL_LCD_COLOR_ARGB
 *              -# JAVACALL_LCD_COLOR_RGB888   
 *              -# JAVACALL_LCD_COLOR_OTHER    
 *                
 * @retval JAVACALL_OK      success
 * @retval JAVACALL_FAIL    fail
 */
javacall_result javacall_lcd_init(void) {
       int x,y;
       unsigned short *p;
       
       sceDisplaySetMode (0, 480, 272);
	sceDisplaySetFrameBuf(vram, 512, PSP_DISPLAY_PIXEL_FORMAT_565, PSP_DISPLAY_SETBUF_NEXTFRAME);


	/*
	if (DukeTango[0] != 480 || DukeTango[1] != 272) {
		p = NULL;
	} else {
		p = (unsigned short*)DukeTango + 2;
	}
	*/

	p = NULL;
	
	for (y = 0; y < 272; y++) { 
		for (x = 0; x < 480; x++) { 
 			vram[x + y * 512] = p?p[x+y*480]:0x0000; 
 		} 
 	}
	
      	scbuff = (javacall_pixel*)malloc(480*272*sizeof(javacall_pixel));
	if (scbuff == NULL) {
		return JAVACALL_FAIL;
	}
	
    return JAVACALL_OK;
}


/**
 * The function javacall_lcd_finalize is called by during Java VM shutdown, 
 * allowing the  * platform to perform device specific lcd-related shutdown
 * operations.  
 * The VM guarantees not to call other lcd functions before calling 
 * javacall_lcd_init( ) again.
 *                
 * @retval JAVACALL_OK      success
 * @retval JAVACALL_FAIL    fail
 */
javacall_result javacall_lcd_finalize(void){
    if (scbuff) free(scbuff);
    return JAVACALL_OK;
} 
    
/**
 * Get screen raster pointer
 *
 * @return pointer to video ram mapped memory region of size  
 *         ( LCDSGetScreenWidth() * LCDSGetScreenHeight() )  
 */
javacall_pixel* javacall_lcd_get_screen(javacall_lcd_screen_type screenType,
                                        int* screenWidth,
                                        int* screenHeight,
                                        javacall_lcd_color_encoding_type* colorEncoding){
    *screenWidth   = 480;
    *screenHeight  = 272;
    *colorEncoding = JAVACALL_LCD_COLOR_RGB565;
    return (javacall_pixel* )scbuff;
}
    
/**
 * The following function is used to flush the image from the Video RAM raster to
 * the LCD display. \n
 * The function call should not be CPU time expensive, and should return
 * immediately. It should avoid memory bulk memory copying of the entire raster.
 *
 * @retval JAVACALL_OK      success
 * @retval JAVACALL_FAIL    fail
 */
javacall_result javacall_lcd_flush(void) {
#if 1
    int x,y;
    for (y = 0; y < 272; y++) { 
		for (x = 0; x < 480; x++) { 
			unsigned short c = scbuff[x+y*480];
 			vram[x + y * 512] = (c >> 11) | (c << 11) | (c & 0x07e0); 
 		} 
    }
#endif
    return JAVACALL_OK;
}
    
/**
 * Set or unset full screen mode.
 * 
 * This function should return <code>JAVACALL_FAIL</code> if full screen mode
 * is not supported.
 * Subsequent calls to <code>javacall_lcd_get_screen()</code> will return
 * a pointer to the relevant offscreen pixel buffer of the corresponding screen
 * mode as well s the corresponding screen dimensions, after the screen mode has
 * changed.
 * 
 * @param useFullScreen if <code>JAVACALL_TRUE</code>, turn on full screen mode.
 *                      if <code>JAVACALL_FALSE</code>, use normal screen mode.

 * @retval JAVACALL_OK   success
 * @retval JAVACALL_FAIL failure
 */
javacall_result javacall_lcd_set_full_screen_mode(javacall_bool useFullScreen) {
    return JAVACALL_OK;
}

   
/**
 * Flush the screen raster to the display. 
 * This function should not be CPU intensive and should not perform bulk memory
 * copy operations.
 * The following API uses partial flushing of the VRAM, thus may reduce the
 * runtime of the expensive flush operation: It should be implemented on
 * platforms that support it
 * 
 * @param ystart start vertical scan line to start from
 * @param yend last vertical scan line to refresh
 *
 * @retval JAVACALL_OK      success
 * @retval JAVACALL_FAIL    fail 
 */
javacall_result javacall_lcd_flush_partial(int ystart, int yend){
#if 1
    int x,y;
    unsigned short *ps, *pd;
    ps = scbuff + 480 * ystart;
    pd = vram + ystart * 512;
    for (y = ystart; y < yend; y++) {
    		x = 480;
    		while(x--) {
			unsigned short c = *ps++;
 			*pd++ = (c >> 11) | (c << 11) | (c & 0x07e0); 
 		} 
		
		pd += (512 - 480);
    }
#endif
    return JAVACALL_OK;
}
    
javacall_bool javacall_lcd_reverse_orientation() {
    return JAVACALL_FALSE;
}
 
javacall_bool javacall_lcd_get_reverse_orientation() {
    return JAVACALL_FALSE;
}
  
int javacall_lcd_get_screen_width() {
    return 480;
}
 
int javacall_lcd_get_screen_height() {
    return 272;
}
    
#ifdef __cplusplus
} //extern "C"
#endif


