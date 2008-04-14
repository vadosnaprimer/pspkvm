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

#define USE_PSP_GU 1
#define ALWAYS_USE_PSP_GU 1

#include "javacall_lcd.h" 
#include <pspdisplay.h>
#include <stdlib.h>
#if USE_PSP_GU
#include <pspge.h>
#include <pspgu.h>
#endif

#ifdef __cplusplus
extern "C" {
#endif

extern const unsigned short DukeTango[];

static unsigned short* vram = (unsigned short*) (0x40000000 | 0x04000000);

static int vscr_w = 480;
static int vscr_h = 272;

static int resized = 0;

static unsigned short int __attribute__((aligned(16))) _offscreen[512*512];
static javacall_pixel* scbuff = _offscreen;

static int _enable_lcd_flush = 1;

#if USE_PSP_GU

struct Vertex
{
	float u, v;
	float color;
	float x, y, z;
};

extern unsigned int * _gu_list;

static unsigned short int __attribute__((aligned(16))) swizzled_pixels [512*512];


void swizzle_fast(u8* out, const u8* in, unsigned int width, unsigned int height)
{
   unsigned int blockx, blocky;
   unsigned int j;
   unsigned int cur_row;
 
   unsigned int width_blocks = (width / 16);
   unsigned int height_blocks = (height / 8);
 
   unsigned int src_pitch = (width-16)/4;
   unsigned int src_row = width * 8;
 
   const u8* ysrc = in;
   u32* dst = (u32*)out;
 
   for (blocky = 0, cur_row = 0; blocky <= height_blocks; ++blocky)
   {
      int block_height = height - cur_row;
      block_height = block_height>8?8:block_height;
      const u8* xsrc = ysrc;
      for (blockx = 0; blockx < width_blocks; ++blockx)
      {
         const u32* src = (u32*)xsrc;
         for (j = 0; j < block_height; ++j)
         {
            *(dst++) = *(src++);
            *(dst++) = *(src++);
            *(dst++) = *(src++);
            *(dst++) = *(src++);
            src += src_pitch;
         }
         for (; j < 8; ++j)
         {
            *(dst++) = 0;
            *(dst++) = 0;
            *(dst++) = 0;
            *(dst++) = 0;
         }         
         xsrc += 16;
     }
     ysrc += src_row;
     dst = (u32*)out + 4*512*(blocky+1);     
     cur_row += 8;
   }
   sceKernelDcacheWritebackAll();
}

static void pspFrameStart(int use_psp_gu) {
	if (use_psp_gu) {
		sceGuStart(GU_DIRECT, _gu_list);
		//sceGuStart(GU_SEND, _gu_list);
		//sceGuClear(GU_COLOR_BUFFER_BIT|GU_STENCIL_BUFFER_BIT|GU_DEPTH_BUFFER_BIT);
	}	
}
extern int _stop_lcd_flush;
static void pspFrameEnd(int use_psp_gu) {
	if (use_psp_gu) {   	    
		//sceGuTexSync();
		sceGuFinish();
		//sceGuSendList(GU_TAIL, _gu_list,&tempGeContext);
		//sceGuSync(0, GU_SYNC_DONE);	 
		sceGuSync(0, 0);
		//sceDisplayWaitVblankStart();
		//fbp = sceGuSwapBuffers();
		//screen.image = (unsigned char*)(0x04000000+(u32)fbp);
		if(_enable_lcd_flush)
		sceGuSwapBuffers();
	}
}

static void advancedBlit(int sx, int sy, int sw, int sh, int dx, int dy, int dw, int dh, int slice, int rot)
{
	int start, end;
	float xScale = ((float)dw)/((float)sw);
	float dxSlice = xScale * slice;
	float dx_f = dx;
	// blit maximizing the use of the texture-cache
	if (rot == 0) {
	for (start = sx, end = sx+sw; start < end; start += slice, dx_f += dxSlice)
	{
		struct Vertex* vertices = (struct Vertex*)sceGuGetMemory(2 * sizeof(struct Vertex));
		int width = (start + slice) < end ? slice : end-start;

		vertices[0].u = start; vertices[0].v = sy;
		vertices[0].color = 0;
		vertices[0].x = dx_f; vertices[0].y = dy; vertices[0].z = 0;

		vertices[1].u = start + width; vertices[1].v = sy + sh;
		vertices[1].color = 0;
		vertices[1].x = dx_f + xScale*width; vertices[1].y = dy + dh; vertices[1].z = 0;
		
		sceGuDrawArray(GU_SPRITES,GU_TEXTURE_32BITF|GU_COLOR_5650|GU_VERTEX_32BITF|GU_TRANSFORM_2D,2,0,vertices);
	}
	} else {
	
	for (start = sx, end = sx+sw; start < end; start += slice, dx_f += dxSlice)
	{
		struct Vertex* vertices = (struct Vertex*)sceGuGetMemory(2 * sizeof(struct Vertex));
		int width = (start + slice) < end ? slice : end-start;

		vertices[0].u = start; vertices[0].v = sy;
		vertices[0].color = 0;
		vertices[0].x = 480 - dy; vertices[0].y = dx_f; vertices[0].z = 0;

		vertices[1].u = start + width; vertices[1].v = sy + sh;
		vertices[1].color = 0;
		vertices[1].x = 480 - dy- dh; vertices[1].y = dx_f + xScale*width; vertices[1].z = 0;
		
		sceGuDrawArray(GU_SPRITES,GU_TEXTURE_32BITF|GU_COLOR_5650|GU_VERTEX_32BITF|GU_TRANSFORM_2D,2,0,vertices);
	}
	}
}

#endif //USE_PSP_GU

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

       if (!ALWAYS_USE_PSP_GU && vscr_h <= 272) {
           sceGuDisplay(0);
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
    
    
          //scbuff = (javacall_pixel*)malloc(vscr_w*vscr_h*sizeof(javacall_pixel));
    	   //if (scbuff == NULL) {
          //    return JAVACALL_FAIL;
          //}
       } else {
           //printf("Setup GU\n");
	    //setup_gu();
	    memset(scbuff, 0, sizeof(_offscreen));
          sceGuDisplay(1);
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
    //if (scbuff) free(scbuff);
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
    if (resized) {
        javacall_lcd_finalize();
        javacall_lcd_init();
        resized = 0;
    }
    *screenWidth   = vscr_w;
    *screenHeight  = vscr_h;
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
    return javacall_lcd_flush_partial(0, vscr_h);
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
    if (ALWAYS_USE_PSP_GU || vscr_h > 272) {
        //Need scale
        swizzle_fast((u8*)swizzled_pixels,scbuff,vscr_w*2,vscr_h); // 512*2 because swizzle operates in bytes, and each pixel in a 16-bit texture is 2 bytes
        pspFrameStart(1);
        sceGuClearColor(0);
	 sceGuClearDepth(0);
	
        sceGuClear(GU_COLOR_BUFFER_BIT|GU_DEPTH_BUFFER_BIT);
        sceGuTexMode(GU_PSM_5650 ,0,0,1); // 16-bit RGBA
        sceGuTexImage(0,512,512,512,swizzled_pixels); // setup texture by framebuff
        sceGuTexFunc(GU_TFX_REPLACE,GU_TCC_RGB); // don't get influenced by any vertex colors
        sceGuTexFilter(GU_LINEAR,GU_LINEAR); // point-filtered sampling
    
    	 {
    		int scr_x = 0;
    		int scr_y = 0;
    		int scr_w = vscr_w;
    		int scr_h = vscr_h;
    		if (javacall_devemu_get_rotation(javacall_devemu_get_current_device()) == 90) {
    			/*
    			CopyImageCW90(screen.image, screen.width,
    					(480-scr_h)/2,(272-scr_w)/2,
    					screenImage.image, screenImage.width,
    					0, 0, scr_w, scr_h);
    			*/
    			if (scr_w <= 272 && scr_h <= 480){
    				advancedBlit(scr_x,scr_y,scr_w,scr_h,(272-scr_w)/2,(480-scr_h)/2,scr_w,scr_h,16, 90);
    			}
    			else if (scr_w/272.0f >= scr_h/480.0f){
    				const int des_h = scr_h*272/scr_w;
    				const int des_w = 272;
    				advancedBlit(scr_x,scr_y,scr_w,scr_h,0,(480-des_h)/2,des_w,des_h,16, 90);
    			}
    			else {
    				const int des_h = 480;
    				const int des_w = scr_w*480/scr_h;
    				advancedBlit(scr_x,scr_y,scr_w,scr_h,(272-des_w)/2,0,des_w,des_h,16, 90);
    			}
    		}
    		else {
    			/*if (scr_w <= 480 && scr_h <= 272){
    				advancedBlit(scr_x,scr_y,scr_w,scr_h,(480-scr_w)/2,(272-scr_h)/2,scr_w,scr_h,16, 0);
    			}
    			else */if (scr_h/272.0f >= scr_w/480.0f){
    				const int des_w = scr_w*272/scr_h;
    				const int des_h = 272;
    				advancedBlit(scr_x,scr_y,scr_w,scr_h,(480-des_w)/2,(272-des_h)/2,des_w,des_h,16, 0);
    			}
    			else {
    				const int des_w = 480;
    				const int des_h = scr_h*480/scr_w;
    				advancedBlit(scr_x,scr_y,scr_w,scr_h,(480-des_w)/2,(272-des_h)/2,des_w,des_h,16, 0);
    			}
    		}
    		pspFrameEnd(1);
    	}
    } else {
        int x,y;
        int xstart = (480 - vscr_w) / 2;
        unsigned short *ps, *pd; 
        ps = scbuff + vscr_w * ystart;
        pd = vram + ystart * 512 + xstart;
        for (y = ystart; y < yend; y++) {
        		x = vscr_w;
        	while(x--) {
    			unsigned short c = *ps++;
     			*pd++ = c;//(c >> 11) | (c << 11) | (c & 0x07e0); 
     		} 
    		
    		pd += (512 - vscr_w);
        }
    }
    return JAVACALL_OK;
}
    
javacall_bool javacall_lcd_reverse_orientation() {
    return JAVACALL_FALSE;
}
 
javacall_bool javacall_lcd_get_reverse_orientation() {
    return JAVACALL_FALSE;
}
  
int javacall_lcd_get_screen_width() {
    return vscr_w;
}
 
int javacall_lcd_get_screen_height() {
    return vscr_h;
}

void javacall_set_new_screen_size(int w, int h) {
    if (w == vscr_w && h == vscr_h) {
        return;
    }

    printf("javacall_set_new_screen_size: %d, %d\n",w,h);
    vscr_w = w;
    vscr_h = h;
    resized = 1;
    javanotify_rotation();
}

void javacall_lcd_enable_flush(int enable) {
    _enable_lcd_flush = enable;
}

#ifdef __cplusplus
} //extern "C"
#endif


