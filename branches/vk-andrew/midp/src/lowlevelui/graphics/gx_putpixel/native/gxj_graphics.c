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
#include <midp_logging.h>

#include <gx_graphics.h>
#include <gxapi_constants.h>

#include "gxj_intern_graphics.h"
#include "gxj_intern_putpixel.h"
#include "gxj_intern_image.h"

#if ENABLE_BOUNDS_CHECKS
#include <gxapi_graphics.h>
#endif

#ifdef PSP
#define USE_PSP_VFPU 1
#endif
/**
 * @file
 *
 * putpixel primitive graphics. 
 */

/**
 * Create native representation for a image.
 *
 * @param jimg Java Image ROM structure to convert from
 * @param sbuf pointer to Screen buffer structure to populate
 * @param g optional Graphics object for debugging clip code.
 *	    give NULL if don't care.
 *
 * @return the given 'sbuf' pointer for convenient usage,
 *	   or NULL if the image is null.
 */
gxj_screen_buffer* gxj_get_image_screen_buffer_impl(const java_imagedata *img,
						    gxj_screen_buffer *sbuf,
						    jobject graphics) {

    /* NOTE:
     * Since this routine is called by every graphics operations
     * We use ROMStruct directly instead of macros
     * like JavaByteArray, etc, for max performance.
     */
    if (img == NULL) {
	return NULL;
    }

    sbuf->width  = img->width;
    sbuf->height = img->height;

    /* Only use nativePixelData and nativeAlphaData if
     * pixelData is null */
    if (img->pixelData != NULL) {
	sbuf->pixelData = (gxj_pixel_type *)&(img->pixelData->elements[0]);
	sbuf->alphaData = (img->alphaData != NULL)
			    ? (gxj_alpha_type *)&(img->alphaData->elements[0])
			    : NULL;
    } else {
	sbuf->pixelData = (gxj_pixel_type *)img->nativePixelData;
	sbuf->alphaData = (gxj_alpha_type *)img->nativeAlphaData;
    }

#if ENABLE_BOUNDS_CHECKS
    sbuf->g = (graphics != NULL) ? GXAPI_GET_GRAPHICS_PTR(graphics) : NULL;
#else
    (void)graphics; /* Surpress unused parameter warning */
#endif

    return sbuf;
}


/**
 * Draw triangle
 */
void
gx_fill_triangle(int color, const jshort *clip, 
		  const java_imagedata *dst, int dotted, 
                  int x1, int y1, int x2, int y2, int x3, int y3) {
  gxj_screen_buffer screen_buffer;
  gxj_screen_buffer *sbuf = gxj_get_image_screen_buffer_impl(dst, &screen_buffer, NULL);
  sbuf = (gxj_screen_buffer *)getScreenBuffer(sbuf);

  REPORT_CALL_TRACE(LC_LOWUI, "gx_fill_triangle()\n");

  /* Surpress unused parameter warnings */
  (void)dotted;

  fill_triangle(sbuf, GXJ_RGB24TORGB16(color), 
		clip, x1, y1, x2, y2, x3, y3);
}

/**
 * Copy from a specify region to other region
 */
void
gx_copy_area(const jshort *clip, 
	      const java_imagedata *dst, int x_src, int y_src, 
              int width, int height, int x_dest, int y_dest) {
  gxj_screen_buffer screen_buffer;
  gxj_screen_buffer *sbuf = gxj_get_image_screen_buffer_impl(dst, &screen_buffer, NULL);
  sbuf = (gxj_screen_buffer *)getScreenBuffer(sbuf);

  copy_imageregion(sbuf, sbuf, clip, x_dest, y_dest, width, height,
		   x_src, y_src, 0);
}

/**
 * Premultiply color components by it's corresponding alpha component.
 *
 * Formula: Cs = Csr * As (for source pixel),
 *          Cd = Cdr * Ad (analog for destination pixel).
 *
 * @param C one of the raw color components of the pixel (Csr or Cdr in the formula).
 * @param A the alpha component of the source pixel (As or Ad in the formula).
 * @return color component in premultiplied form.
 */
#define PREMULTUPLY_ALPHA(C, A) \
    (unsigned char)( ((int)(C)) * (A) / 0xff )

/**
 * The source is composited over the destination (Porter-Duff Source Over 
 * Destination rule).
 *
 * Formula: Cr = Cs + Cd*(1-As)
 *
 * Note: the result is always equal or less than 0xff, i.e. overflow is impossible.
 *
 * @param Cs a color component of the source pixel in premultiplied form
 * @param As the alpha component of the source pixel
 * @param Cd a color component of the destination pixel in premultiplied form
 * @return a color component of the result in premultiplied form
 */
#define ADD_PREMULTIPLIEDCOLORS_SRCOVER(Cs, As, Cd) \
    (unsigned char)( ((int)(Cs)) + ((int)(Cd)) * (0xff - (As)) / 0xff )

/**
 * Combine separate source and destination color components.
 *
 * Note: all backround pixels are treated as full opaque.
 *
 * @param Csr one of the raw color components of the source pixel
 * @param As the alpha component of the source pixel
 * @param Cdr one of the raw color components of the destination pixel
 * @return a color component of the result in premultiplied form
 */
#define ADD_COLORS(Csr, As, Cdr) \
    ADD_PREMULTIPLIEDCOLORS_SRCOVER( \
            PREMULTUPLY_ALPHA(Csr, As), \
            As, \
            PREMULTUPLY_ALPHA(Cdr, 0xff) )


#ifdef USE_PSP_VFPU
static float falpha_table[256];
static int falpha_table_initilized = 0;
static void falpha_table_init() {
    int a;
    	for (a = 0; a < 256; a++) {
           falpha_table[a] = (float)a/255.0f;
    	}
    falpha_table_initilized = 1;
}
#endif
static unsigned char alpha_table[64][256];
static int alpha_table_initilized = 0;

static void alpha_table_init() {
    int c, a;
    for (c = 0; c < 64; c++) {
    	for (a = 0; a < 256; a++) {
           alpha_table[c][a] = PREMULTUPLY_ALPHA(c, a);
    	}
    }
    alpha_table_initilized = 1;
}

/**
 * Combine source and destination colors to achieve blending and transparency
 * effects.
 *
 * @param src source pixel value in 32bit ARGB format.
 * @param dst destination pixel value in 32bit RGB format.
 * @return result pixel value in 32bit RGB format.
 */
static jint alphaComposition(jint src, jint dst) {
    unsigned char As = (unsigned char)(src >> 24);

    unsigned char Rr = ADD_COLORS(
            (unsigned char)(src >> 16), As, (unsigned char)(dst >> 16) );

    unsigned char Gr = ADD_COLORS(
            (unsigned char)(src >> 8), As, (unsigned char)(dst >> 8) );

    unsigned char Br = ADD_COLORS(
            (unsigned char)src, As, (unsigned char)dst );

    /* compose RGB from separate color components */
    return (((jint)Rr) << 16) | (((jint)Gr) << 8) | Br;
}

#if (UNDER_CE)
extern void asm_draw_rgb(jint* src, int srcSpan, unsigned short* dst,
    int dstSpan, int width, int height);
#endif
/*
#define GXJ_RGB24TORGB16VFPU(pFrom, pTo) \
	do {  \
	__asm__ __volatile__ ( \
    	" ulv.q C000.q,0(%1)\n" \
    	" ulv.q C020.q, 16(%1)\n" \       
    	" vt5650.q C010.p,C000.q\n" \
    	" vt5650.q C030.p,C020.q\n" \
    	" sv.s S010, 0(%0)\n" \
       " sv.s S011, 4(%0)\n" \
    	" sv.s S030, 8(%0)\n" \
       " sv.s S031, 16(%0)\n" \
       " vsync\n" \
       " vflush\n" \
    	:: "r"(pTo), "r"(pFrom) \
    	); \
	} while(0);
	*/
/** Draw image in RGB format */
void
gx_draw_rgb(const jshort *clip,
	     const java_imagedata *dst, jint *rgbData,
             jint offset, jint scanlen, jint x, jint y,
             jint width, jint height, jboolean processAlpha) {
    int a, b, diff;
    int dataRowIndex, sbufRowIndex;
    int blocks, remains;

    gxj_screen_buffer screen_buffer;
    gxj_screen_buffer* sbuf = (gxj_screen_buffer*) getScreenBuffer(
      gxj_get_image_screen_buffer_impl(dst, &screen_buffer, NULL));
    int sbufWidth = sbuf->width;
    int sbufHeight = sbuf->height;

    jshort clipX1 = clip[0];
    jshort clipY1 = clip[1];
    jshort clipX2 = clip[2];
    jshort clipY2 = clip[3];

    if (clipX1 >= sbufWidth || clipX2 < 0 || clipY1 >= sbufHeight || clipY2 < 0) return;

    if (clipX1 < 0) clipX1 = 0;
    if (clipY1 < 0) clipY1 = 0;
    if (clipX2 > sbufWidth) clipX2 = sbufWidth;
    if (clipY2 > sbufHeight) clipY2 = sbufHeight;

    REPORT_CALL_TRACE(LC_LOWUI, "gx_draw_rgb()\n");

    diff = clipX1 - x;
    if (diff > 0) {
        width -= diff;
        offset += diff;
        x = clipX1;
    }
    if (x + width > clipX2) {
        width = clipX2 - x;
    }
    diff = clipY1 - y;
    if (diff > 0) {
        height -= diff;
        offset += diff * scanlen;
        y = clipY1;
    }
    if (y + height > clipY2) {
        height = clipY2 - y;
    }
    if (width <= 0 || height <= 0) {
        return;
    }

#if (UNDER_CE)
    if (!processAlpha) {
        asm_draw_rgb(rgbData + offset, scanlen - width,
            sbuf->pixelData + sbufWidth * y + x,
            sbufWidth - width, width, height);
        return;
    }
#endif

    CHECK_SBUF_CLIP_BOUNDS(sbuf, clip);
    dataRowIndex = 0;
    sbufRowIndex = y * sbufWidth;

    blocks = width / 8;
    remains = width % 8;

    if (!alpha_table_initilized) alpha_table_init();
#if USE_PSP_VFPU
    if (!falpha_table_initilized) falpha_table_init();
#endif

    for (b = y; b < y + height;
        b++, dataRowIndex += scanlen,
        sbufRowIndex += sbufWidth) {

        if (!processAlpha) {
            int i;
            register jint* pFrom = &rgbData[offset + dataRowIndex];
            register gxj_pixel_type* pTo = &sbuf->pixelData[sbufRowIndex + x];
/*
            if ((unsigned int)pTo & 3) {
                *pTo++ = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
            }
    */    
            for (i = 0; i< blocks; i++) {
#if 1
            	//if (((unsigned int)pTo & 3) || ((unsigned int)pFrom & 0xf)) {
		{

            	  *pTo++ = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
            	  *pTo++ = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
            	  *pTo++ = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
            	  *pTo++ = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
            	  *pTo++ = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
            	  *pTo++ = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
            	  *pTo++ = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
            	  *pTo++ = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
            	  
            	//} else {
                //GXJ_RGB24TORGB16VFPU(pTo, pFrom);
                /*
                __asm__ __volatile__ ( 
              	" ulv.q C000.q,0(%1)\n" 
              	" ulv.q C020.q, 16(%1)\n" 
              	" ulv.q C100.q,32(%1)\n" 
              	" ulv.q C120.q, 48(%1)\n" 
              	" vt5650.q C010.p,C000.q\n" 
              	" vt5650.q C030.p,C020.q\n" 
                     " vt5650.q C110.p,C100.q\n" 
              	" vt5650.q C130.p,C120.q\n" 
              	" sv.s S010, 0(%0)\n" 
                     " sv.s S011, 4(%0)\n" 
              	" sv.s S030, 8(%0)\n" 
                     " sv.s S031, 12(%0)\n"
                     " sv.s S110, 16(%0)\n" 
                     " sv.s S111, 20(%0)\n" 
              	" sv.s S130, 24(%0)\n" 
                     " sv.s S131, 28(%0)\n"
              	:: "r"(pTo), "r"(pFrom) 
              	);
                pTo += 16;
                pFrom += 16;
                */
            	}
#else
            	  __asm__ __volatile__ (
            	  " .set noreorder\n"
            	  
            	  " move $8, %0\n"
            	  " lw $10, 0($8)\n"
            	  " lw $11, 4($8)\n"
            	  " lw $12, 8($8)\n"
            	  " lw $13, 12($8)\n"
            	  " lw $14, 16($8)\n"
            	  " lw $15, 20($8)\n"
            	  " lw $24, 24($8)\n"
            	  " lw $25, 28($8)\n"
            	  
            	  " srl $8, $10, 19\n"
            	  " srl $9, $10, 5\n"
             	  " andi $8, $8, 0x001F\n"
             	  " andi $9, $9, 0x07E0\n"
            	  " sll $10, $10, 8\n"
            	  " or  $8, $8, $9\n"
            	  " andi $10, $10, 0xF800\n"
            	  " or  $10, $10, $8\n"
            	  
            	  " srl $8, $11, 19\n"
            	  " srl $9, $11, 5\n"
            	  " andi $8, $8, 0x001F\n"            	  
            	  " andi $9, $9, 0x07E0\n"
            	  " sll $11, $11, 8\n"
            	  " or  $8, $8, $9\n"
            	  " andi $11, $11, 0xF800\n"            	  
            	  " or  $11, $11, $8\n"
            	  
            	  " srl $8, $12, 19\n"
            	  " srl $9, $12, 5\n"
            	  " andi $8, $8, 0x001F\n"            	  
            	  " andi $9, $9, 0x07E0\n"
            	  " sll $12, $12, 8\n"
            	  " or  $8, $8, $9\n"
            	  " andi $12, $12, 0xF800\n"            	  
            	  " or  $12, $12, $8\n"
            	  
            	  " srl $8, $13, 19\n"
            	  " srl $9, $13, 5\n"
            	  " andi $8, $8, 0x001F\n"            	  
            	  " andi $9, $9, 0x07E0\n"
            	  " sll $13, $13, 8\n"
            	  " or  $8, $8, $9\n"
            	  " andi $13, $13, 0xF800\n"            	  
            	  " or  $13, $13, $8\n"
            	  
            	  " srl $8, $14, 19\n"
            	  " srl $9, $14, 5\n"
            	  " andi $8, $8, 0x001F\n"
            	  " andi $9, $9, 0x07E0\n"
            	  " sll $14, $14, 8\n"
            	  " or  $8, $8, $9\n"
            	  " andi $14, $14, 0xF800\n"
            	  " or  $14, $14, $8\n"
            	  
            	  " srl $8, $15, 19\n"
            	  " srl $9, $15, 5\n"
            	  " andi $8, $8, 0x001F\n"
            	  " andi $9, $9, 0x07E0\n"
            	  " sll $15, $15, 8\n"
            	  " or  $8, $8, $9\n"
            	  " andi $15, $15, 0xF800\n"
            	  " or  $15, $15, $8\n"
            	  
            	  " srl $8, $24, 19\n"
            	  " srl $9, $24, 5\n"
            	  " andi $8, $8, 0x001F\n"
            	  " andi $9, $9, 0x07E0\n"
            	  " sll $24, $24, 8\n"
            	  " or  $8, $8, $9\n"
            	  " andi $24, $24, 0xF800\n"
            	  " or  $24, $24, $8\n"
            	  
            	  " srl $8, $25, 19\n"
            	  " srl $9, $25, 5\n"
            	  " andi $8, $8, 0x001F\n"
            	  " andi $9, $9, 0x07E0\n"
            	  " sll $25, $25, 8\n"
            	  " or  $8, $8, $9\n"
            	  " andi $25, $25, 0xF800\n"
            	  " or  $25, $25, $8\n"
            	  
            	  " move $8, %1\n"
            	  " sh $10, 0($8)\n"
            	  " sh $11, 2($8)\n"
            	  " sh $12, 4($8)\n"
            	  " sh $13, 6($8)\n"
            	  " sh $14, 8($8)\n"
            	  " sh $15, 10($8)\n"
            	  " sh $24, 12($8)\n"
            	  " sh $25, 14($8)\n"
            	  
            	  " .set reorder\n"
            	  
            	  ::"r" (pFrom), "r" (pTo)
            	  :"$8", "$9", "$10", "$11", "$12", "$13", "$14", "$15", "$24", "$25", "memory"
            	  );
            	  pTo += 8;
            	  pFrom += 8;
#endif 
            }
            switch (remains) {            	
            	/*
            	case 15:  *pTo++ = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
            	case 14:  *pTo++ = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
            	case 13:  *pTo++ = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
            	case 12:  *pTo++ = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
            	case 11:  *pTo++ = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
            	case 10:  *pTo++ = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
            	case 9:  *pTo++ = GXJ_RGB24TORGB16((*pFrom)); pFrom++;    
            	case 8:  *pTo++ = GXJ_RGB24TORGB16((*pFrom)); pFrom++;    
            	*/
            	case 7:  *pTo++ = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
            	case 6:  *pTo++ = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
            	case 5:  *pTo++ = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
            	case 4:  *pTo++ = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
            	case 3:  *pTo++ = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
            	case 2:  *pTo++ = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
            	case 1:  *pTo++ = GXJ_RGB24TORGB16((*pFrom)); pFrom++;    
            }
        } else {
        
            jint* psrc = rgbData+offset + dataRowIndex;
            jint* pend = psrc + width;
            gxj_pixel_type* pdst = sbuf->pixelData+sbufRowIndex+x;

#if USE_PSP_VFPU
            static float __attribute__((aligned(16))) s[24];
            static float __attribute__((aligned(16))) d[24];
            static float __attribute__((aligned(16))) res[24];

            float* ps = &s[0];
            float* pd = &d[0];

            while (psrc <pend-5) {
            	unsigned char* point = (unsigned char*)psrc;
            	unsigned short* bg = (unsigned short*)pdst;
            	//float fr, fg, fb;
            	//unsigned short cr, cg, cb;
            	
            s[3] = falpha_table[point[3]];
            s[2] = (float)point[0];
            s[1] = (float)point[1];
            s[0] = (float)point[2];
            s[7] = falpha_table[point[7]];
            s[6] = (float)point[4];
            s[5] = (float)point[5];
            s[4] = (float)point[6];
            s[11] = falpha_table[point[11]];
            s[10] = (float)point[8];
            s[9] = (float)point[9];
            s[8] = (float)point[10];
            s[15] = falpha_table[point[15]];
            s[14] = (float)point[12];
            s[13] = (float)point[13];
            s[12] = (float)point[14];
            s[19] = falpha_table[point[19]];
            s[18] = (float)point[16];
            s[17] = (float)point[17];
            s[16] = (float)point[18];
            s[23] = falpha_table[point[23]];
            s[22] = (float)point[20];
            s[21] = (float)point[21];
            s[20] = (float)point[22];



            //d[3] = (float)0;
            d[2] = (float)((bg[0] >> 8) & 0xF8);
            d[1] = (float)((bg[0] >> 3) & 0xFC);
            d[0] = (float)((bg[0] << 3) & 0xF8);
            //d[7] = (float)0;
            d[6] = (float)((bg[1] >> 8) & 0xF8);
            d[5] = (float)((bg[1] >> 3) & 0xFC);
            d[4] = (float)((bg[1] << 3) & 0xF8);
            //d[11] = (float)0;
            d[10] = (float)((bg[2] >> 8) & 0xF8);
            d[9] = (float)((bg[2] >> 3) & 0xFC);
            d[8] = (float)((bg[2] << 3) & 0xF8);
            //d[15] = (float)0;
            d[14] = (float)((bg[3] >> 8) & 0xF8);
            d[13] = (float)((bg[3] >> 3) & 0xFC);
            d[12] = (float)((bg[3] << 3) & 0xF8);
            //d[19] = (float)0;
            d[18] = (float)((bg[4] >> 8) & 0xF8);
            d[17] = (float)((bg[4] >> 3) & 0xFC);
            d[16] = (float)((bg[4] << 3) & 0xF8);
            //d[23] = (float)0;
            d[22] = (float)((bg[5] >> 8) & 0xF8);
            d[21] = (float)((bg[5] >> 3) & 0xFC);
            d[20] = (float)((bg[5] << 3) & 0xF8);


            
            __asm__ __volatile__ (
            	  "lv.q   C000.q, 0(%1)\n"
            	  "lv.q   C010.q, 0(%2)\n"            	  
            	  "lv.q   C100.q, 16(%1)\n"
            	  "lv.q   C110.q, 16(%2)\n"
            	  "lv.q   C200.q, 32(%1)\n"
            	  "lv.q   C210.q, 32(%2)\n"
            	  "lv.q   C300.q, 48(%1)\n"
            	  "lv.q   C310.q, 48(%2)\n"
            	  "lv.q   C400.q, 64(%1)\n"
            	  "lv.q   C410.q, 64(%2)\n"
            	  "lv.q   C500.q, 80(%1)\n"
            	  "lv.q   C510.q, 80(%2)\n"
            	  
            	  "vocp.s S003, S013\n"
            	  "vocp.s S103, S113\n"
            	  "vocp.s S203, S213\n"
            	  "vocp.s S303, S313\n"    
            	  "vocp.s S403, S413\n"
            	  "vocp.s S503, S513\n"    
            	  
            	  "vscl.t C000, C000, S003\n"
            	  "vscl.t C010, C010, S013\n"
            	  "vscl.t C100, C100, S103\n"
                "vscl.t C110, C110, S113\n"
                "vscl.t C200, C200, S203\n"
                "vscl.t C210, C210, S213\n"
            	  "vscl.t C300, C300, S303\n"
            	  "vscl.t C310, C310, S313\n"
            	  "vscl.t C400, C400, S403\n"
                "vscl.t C410, C410, S413\n"
            	  "vscl.t C500, C500, S503\n"
            	  "vscl.t C510, C510, S513\n"
            	  
            	  "vadd.t C000, C000, C010\n"
            	  "vadd.t C100, C100, C110\n"
            	  "vadd.t C200, C200, C210\n"
            	  "vadd.t C300, C300, C310\n"
            	  "vadd.t C400, C400, C410\n"
            	  "vadd.t C500, C500, C510\n"
            	  
            	  "sv.q   C000, 0(%0)\n"
            	  "sv.q   C100, 16(%0)\n"
            	  "sv.q   C200, 32(%0)\n"
            	  "sv.q   C300, 48(%0)\n"
            	  "sv.q   C400, 64(%0)\n"
            	  "sv.q   C500, 80(%0)\n"
            	  :: "r"(res), "r"(pd), "r"(ps));

                  psrc += 6;
                  
                *pdst++ = ((((unsigned short)res[2]) << 8) & 0xF800) | ((((unsigned short)res[1]) << 3) & 0x07E0) | ((((unsigned short)res[0]) >> 3) & 0x001F);                
                *pdst++ = ((((unsigned short)res[6]) << 8) & 0xF800) | ((((unsigned short)res[5]) << 3) & 0x07E0) | ((((unsigned short)res[4]) >> 3) & 0x001F);
                *pdst++ = ((((unsigned short)res[10]) << 8) & 0xF800) | ((((unsigned short)res[9]) << 3) & 0x07E0) | ((((unsigned short)res[8]) >> 3) & 0x001F);
                *pdst++ = ((((unsigned short)res[14]) << 8) & 0xF800) | ((((unsigned short)res[13]) << 3) & 0x07E0) | ((((unsigned short)res[12]) >> 3) & 0x001F);
                *pdst++ = ((((unsigned short)res[18]) << 8) & 0xF800) | ((((unsigned short)res[17]) << 3) & 0x07E0) | ((((unsigned short)res[16]) >> 3) & 0x001F);
                *pdst++ = ((((unsigned short)res[22]) << 8) & 0xF800) | ((((unsigned short)res[21]) << 3) & 0x07E0) | ((((unsigned short)res[20]) >> 3) & 0x001F);
            }
/*
            while (psrc <pend) {
            	unsigned char* point = (unsigned char*)psrc++;
            	unsigned short bg = (unsigned short)*pdst;
            	float fr, fg, fb;
            	unsigned short cr, cg, cb;
            	
            s[3] = falpha_table[point[3]];
            s[2] = (float)point[0];
            s[1] = (float)point[1];
            s[0] = (float)point[2];

            d[3] = (float)0;
            d[2] = (float)((bg >> 8) & 0xF8);
            d[1] = (float)((bg >> 3) & 0xFC);
            d[0] = (float)((bg << 3) & 0xF8);
            
            __asm__ __volatile__ (
            	  " .set noreorder\n"
            	  "lv.q   C000, %0\n"
            	  "lv.q   C010, %4\n"
            	  "vocp.s S003, S013\n"
            	  "vscl.t C000, C000, S003\n"
            	  "vscl.t C010, C010, S013\n"
            	  "vadd.t C000, C000, C010\n"
            	  "mfv   %3, S000\n"
            	  "mfv   %2, S001\n"
            	  "mfv   %1, S002\n"
            	  : "+m"(*d), "=r"(fr), "=r"(fg), "=r"(fb): "m"(*s));
 
                cr = (unsigned short)fr;
                cg = (unsigned short)fg;
                cb = (unsigned short)fb;
                *pdst++ = ((cr << 8) & 0xF800) | ((cg << 3) & 0x07E0) | ((cb >> 3) & 0x001F);                
            }
*/
#endif
            while (psrc <pend) {
            	  register jint value;
            	  register unsigned char alpha;
            	  //CHECK_PTR_CLIP(sbuf, pdst);
                while (psrc < pend - 8) {
                    register jint* p = psrc;
                    if (*p >> 24== 0)
                    if (*++p >> 24== 0)
                    if (*++p >> 24== 0)
                    if (*++p >> 24== 0)
                    if (*++p >> 24== 0)
                    if (*++p >> 24== 0)
                    if (*++p >> 24== 0)
                    if (*++p >> 24== 0) {
                    	  pdst += 8;
                    	  psrc = p+1;
                    	  continue;
                    }

                    pdst += p - psrc;
                    psrc = p;
                    break;
                }

                value = *psrc++;
                alpha = value >> 24;    
                   
                if (alpha == 0xff) {
                    // Pixel has no alpha or no transparency
                    *pdst = GXJ_RGB24TORGB16(value);
                } else if (alpha != 0) {
                    	   /*
                    	   jint background = GXJ_RGB16TORGB24(*pdst);
                        jint composition = alphaComposition(value, background);
                        *pdst = GXJ_RGB24TORGB16(composition);
                        */
                        register unsigned short background = *pdst;
                    	   register unsigned char alpha1 = 0xff - alpha;
                    	   register unsigned short r = alpha_table[(value & 0x00F80000) >> 19][alpha] + alpha_table[background & 0x1F][alpha1];
                    	   register unsigned short g = alpha_table[(value & 0x0000FC00) >> 10][alpha] + alpha_table[(background >> 5) & 0x3F][alpha1];
                    	   register unsigned short b = alpha_table[(value & 0x000000F8) >> 3][alpha] + alpha_table[background >> 11][alpha1];
                        if (r > 0x1F) r = 0x1F;
                        if (g > 0x3F) g = 0x3F;
                        if (b > 0x1F) b = 0x1F;
                    	   *pdst = (b << 11) | (g << 5) | r;                    
                }
                pdst++;
            } /* loop by rgb data columns */
        }
    } /* loop by rgb data rows */
}

#ifdef BIG_ENDIAN_FRAMEBUFFER
#define GXJ_RGB444TORGB565(x) (((( x ) & 0x000F) << 12) | \
                             ((( x ) & 0x00F0) << 3) | \
                             ((( x ) & 0x0F00) >> 7) )

static jshort alphaComposition_4444to565(jshort src, jshort dst) {
    unsigned char As = (unsigned char)(src >> 24);

    unsigned char Rr =  (((src&0x0F00)>>4)*As + (((dst&0xF800)>>8) * (0xff - As))) / 0xff;
	//if (c > 0xff) c = 0xff;
	//*d0 = (unsigned char)c;

	unsigned char Gr =  (((src&0x00F0))*As + (((dst&0x07E0)>>3) * (0xff - As))) / 0xff;
	//if (c > 0xff) c = 0xff;
	//*d1 = (unsigned char)c;

	unsigned char Br =  (((src&0x000F)<<4)*As + (((dst&0x001F)<<3) * (0xff - As))) / 0xff;
	//if (c > 0xff) c = 0xff;
	//*d2 = (unsigned char)c;
	
    /* compose RGB from separate color components */
    return (((jshort)Br&0xF8) << 8) | (((jshort)Gr&0xFC) << 3) | (((jshort)Rr&0xF8) >> 3);
}

#else
#define GXJ_RGB444TORGB565(x) (((( x ) & 0x0F00) << 4) | \
                             ((( x ) & 0x00F0) << 3) | \
                             ((( x ) & 0x000F) << 1) )

static jshort alphaComposition_4444to565(jshort src, jshort dst) {
    unsigned char As = (unsigned char)(src >> 24);

    unsigned char Rr =  (((src&0x0F00)>>4)*As + (((dst&0xF800)>>8) * (0xff - As))) / 0xff;
	//if (c > 0xff) c = 0xff;
	//*d0 = (unsigned char)c;

	unsigned char Gr =  (((src&0x00F0))*As + (((dst&0x07E0)>>3) * (0xff - As))) / 0xff;
	//if (c > 0xff) c = 0xff;
	//*d1 = (unsigned char)c;

	unsigned char Br =  (((src&0x000F)<<4)*As + (((dst&0x001F)<<3) * (0xff - As))) / 0xff;
	//if (c > 0xff) c = 0xff;
	//*d2 = (unsigned char)c;
	
    /* compose RGB from separate color components */
    return (((jshort)Rr&0xF8) << 8) | (((jshort)Gr&0xFC) << 3) | (((jshort)Br&0xF8) >> 3);
}
#endif

void
gx_draw_pixels_4444_to_565(const java_imagedata * imgDest, int nXOriginDest, int nYOriginDest, 
		 jshort * dataSrc,  int nScanLen, int nXOriginSrc,  int nYOriginSrc,  
		 int nWidth,  int nHeight, int transform, jboolean processAlpha) {
    const int pix_size_dest = 2;
    const int pix_size_src = 2;
    unsigned char *destBitsPtr   = NULL,
                  *srcBitsPtr    = NULL,
                  *destBitsLimit = NULL;
    int x_dest = nXOriginDest, y_dest = nYOriginDest;
    int x_dest_incr = 1, y_dest_incr = 1;
    int x_src = nXOriginSrc, y_src = nYOriginSrc; 
	//int x1_src = nXOriginSrc + nWidth, y1_src = nYOriginSrc + nHeight;
    int x_src_incr = 1, y_src_incr = 1;
    int i, j;
    int alpha;

    int t_width, width_src_incr, width_dest_incr;
    int t_height;
    int blocks, remains;
    
    gxj_screen_buffer screen_buffer;
    gxj_screen_buffer* sbuf = (gxj_screen_buffer*) getScreenBuffer(
                                            gxj_get_image_screen_buffer_impl(imgDest, &screen_buffer, NULL));
    int sbufWidth = sbuf->width;

    REPORT_CALL_TRACE(LC_LOWUI, "gx_draw_pixels_4444_to_565()\n");	    

    if(transform & TRANSFORM_INVERTED_AXES) {
        t_width  = nHeight;
        t_height = nWidth;
	 y_dest = y_dest + (t_height - 1)*y_dest_incr;
	 y_dest_incr = -y_dest_incr;
	 x_dest = x_dest + (t_width - 1)*x_dest_incr;
	 x_dest_incr = -x_dest_incr;		
	 x_src_incr = nScanLen;
	 y_src_incr = 1;
    } else {
        t_width  = nWidth;
        t_height = nHeight;
    }

    if (transform & TRANSFORM_Y_FLIP) {
        y_dest = y_dest + (t_height - 1)*y_dest_incr;
        y_dest_incr = -y_dest_incr;
    } 


    if (x_dest+t_width >= sbufWidth) {
	     t_width = sbufWidth - x_dest; //ensure not draw out of screen
    }
    
    if (transform & TRANSFORM_X_FLIP) {
        x_dest = x_dest + (t_width - 1)*x_dest_incr;
        x_dest_incr = -x_dest_incr;
    }
    
    y_dest_incr = y_dest_incr * sbufWidth;
    if(!(transform & TRANSFORM_INVERTED_AXES)){
	y_src_incr = y_src_incr * nScanLen;
    }
    
    x_dest_incr *= pix_size_dest;
    y_dest_incr *= pix_size_dest;
    x_src_incr *= pix_size_src;
    y_src_incr *= pix_size_src;
    width_dest_incr = t_width*x_dest_incr;
    width_src_incr = t_width*x_src_incr;

    destBitsPtr = ((unsigned char *)sbuf->pixelData) + ((y_dest * sbufWidth + x_dest) * pix_size_dest);
    srcBitsPtr = (unsigned char *)(dataSrc + ((y_src * nScanLen + x_src)/* * pix_size_src*/));	

    destBitsLimit = ((unsigned char *)sbuf->pixelData) + (sbuf->width * sbuf->height * pix_size_dest);

/*
    if (x_dest < 0) {
		t_width += x_dest;
		srcBitsPtr -= x_dest*pix_size_dest;
		destBitsPtr -= x_dest*pix_size_dest;
    }
*/
    blocks = t_width / 8;
    remains = t_width % 8;
	
    for (j = 0; j < t_height; j++) {
         if (!processAlpha) {
         	 
               if ((destBitsPtr < destBitsLimit)&& (destBitsPtr + width_dest_incr) < destBitsLimit) {
                   int i;
                   jshort* pFrom = (jshort*)srcBitsPtr;
                   gxj_pixel_type* pTo = (gxj_pixel_type*)destBitsPtr;
                   
                   if (x_dest_incr > 0) {
                   	
                       for (i = 0; i< blocks; i++) {
                       	  *pTo++ = GXJ_RGB444TORGB565((*pFrom)); pFrom++;
                       	  *pTo++ = GXJ_RGB444TORGB565((*pFrom)); pFrom++;
                       	  *pTo++ = GXJ_RGB444TORGB565((*pFrom)); pFrom++;
                       	  *pTo++ = GXJ_RGB444TORGB565((*pFrom)); pFrom++;
                       	  *pTo++ = GXJ_RGB444TORGB565((*pFrom)); pFrom++;
                       	  *pTo++ = GXJ_RGB444TORGB565((*pFrom)); pFrom++;
                       	  *pTo++ = GXJ_RGB444TORGB565((*pFrom)); pFrom++;
                       	  *pTo++ = GXJ_RGB444TORGB565((*pFrom)); pFrom++;
                       }
                       switch (remains) {
                       	case 7:  *pTo++ = GXJ_RGB444TORGB565((*pFrom)); pFrom++;
                       	case 6:  *pTo++ = GXJ_RGB444TORGB565((*pFrom)); pFrom++;
                       	case 5:  *pTo++ = GXJ_RGB444TORGB565((*pFrom)); pFrom++;
                       	case 4:  *pTo++ = GXJ_RGB444TORGB565((*pFrom)); pFrom++;
                       	case 3:  *pTo++ = GXJ_RGB444TORGB565((*pFrom)); pFrom++;
                       	case 2:  *pTo++ = GXJ_RGB444TORGB565((*pFrom)); pFrom++;
                       	case 1:  *pTo++ = GXJ_RGB444TORGB565((*pFrom)); pFrom++;
                       }
                       
                   } else {
                   
                       for (i = 0; i< blocks; i++) {
                       	  *pTo-- = GXJ_RGB444TORGB565((*pFrom)); pFrom++;
                       	  *pTo-- = GXJ_RGB444TORGB565((*pFrom)); pFrom++;
                       	  *pTo-- = GXJ_RGB444TORGB565((*pFrom)); pFrom++;
                       	  *pTo-- = GXJ_RGB444TORGB565((*pFrom)); pFrom++;
                       	  *pTo-- = GXJ_RGB444TORGB565((*pFrom)); pFrom++;
                       	  *pTo-- = GXJ_RGB444TORGB565((*pFrom)); pFrom++;
                       	  *pTo-- = GXJ_RGB444TORGB565((*pFrom)); pFrom++;
                       	  *pTo-- = GXJ_RGB444TORGB565((*pFrom)); pFrom++;
                       }
                       switch (remains) {
                       	case 7:  *pTo-- = GXJ_RGB444TORGB565((*pFrom)); pFrom++;
                       	case 6:  *pTo-- = GXJ_RGB444TORGB565((*pFrom)); pFrom++;
                       	case 5:  *pTo-- = GXJ_RGB444TORGB565((*pFrom)); pFrom++;
                       	case 4:  *pTo-- = GXJ_RGB444TORGB565((*pFrom)); pFrom++;
                       	case 3:  *pTo-- = GXJ_RGB444TORGB565((*pFrom)); pFrom++;
                       	case 2:  *pTo-- = GXJ_RGB444TORGB565((*pFrom)); pFrom++;
                       	case 1:  *pTo-- = GXJ_RGB444TORGB565((*pFrom)); pFrom++;
                       }
                       
                   }
             	 }
               
               destBitsPtr += y_dest_incr;
   		 srcBitsPtr += y_src_incr;
   		 
           } else {
   		
   		for (i = 0; i < t_width; i++) {
   			//M@x: never draw out of screen buffer
   			if (destBitsPtr < destBitsLimit) {
   			
       			alpha = srcBitsPtr[1]&0xF0;
       			if (alpha == 0xf0) {
       			    *(gxj_pixel_type*)destBitsPtr = GXJ_RGB444TORGB565(*(jshort*)srcBitsPtr);
       			} else if (alpha != 0x00) {  /* needs blending */
                                *(gxj_pixel_type*)destBitsPtr = alphaComposition_4444to565(*(jshort*)srcBitsPtr, *(gxj_pixel_type*)destBitsPtr);
       			}		
   			}
   			destBitsPtr += x_dest_incr;//*pix_size_dest;
   			srcBitsPtr += x_src_incr;//*pix_size_src;
   		}
   		
   		destBitsPtr += y_dest_incr - (width_dest_incr);
   		srcBitsPtr += y_src_incr - (width_src_incr);
   	}
    }
}

void
gx_draw_pixels_8888_to_565(const java_imagedata * imgDest, int nXOriginDest, int nYOriginDest, 
		 jint * dataSrc,  int nScanLen, int nXOriginSrc,  int nYOriginSrc,  
		 int nWidth,  int nHeight, int transform, jboolean processAlpha) {
	const int pix_size_dest = 2;
	const int pix_size_src = 4;
    unsigned char *destBitsPtr   = NULL,
                  *srcBitsPtr    = NULL,
                  *destBitsLimit = NULL;
    int x_dest = nXOriginDest, y_dest = nYOriginDest;
    int x_dest_incr = 1, y_dest_incr = 1;
	int x_src = nXOriginSrc, y_src = nYOriginSrc; 
	//int x1_src = nXOriginSrc + nWidth, y1_src = nYOriginSrc + nHeight;
	int x_src_incr = 1, y_src_incr = 1;
	int i, j;
    int alpha;

    int t_width, width_src_incr, width_dest_incr;
    int t_height;

    int blocks, remains;

    
    gxj_screen_buffer screen_buffer;
    gxj_screen_buffer* sbuf = (gxj_screen_buffer*) getScreenBuffer(
      gxj_get_image_screen_buffer_impl(imgDest, &screen_buffer, NULL));
    int sbufWidth = sbuf->width;

    REPORT_CALL_TRACE(LC_LOWUI, "gx_draw_pixels_8888_to_565()\n");	    

    if(transform & TRANSFORM_INVERTED_AXES) {
        t_width  = nHeight;
        t_height = nWidth;
		y_dest = y_dest + (t_height - 1)*y_dest_incr;
		y_dest_incr = -y_dest_incr;
		x_dest = x_dest + (t_width - 1)*x_dest_incr;
		x_dest_incr = -x_dest_incr;		
		x_src_incr = nScanLen;
		y_src_incr = 1;
    } else {
        t_width  = nWidth;
        t_height = nHeight;
    }

    if (transform & TRANSFORM_Y_FLIP) {
        y_dest = y_dest + (t_height - 1)*y_dest_incr;
        y_dest_incr = -y_dest_incr;
    } 

    
    if (x_dest+t_width >= sbufWidth) {
	     t_width = sbufWidth - x_dest; //ensure not draw out of screen
    }

    if (transform & TRANSFORM_X_FLIP) {
        x_dest = x_dest + (t_width - 1)*x_dest_incr;
        x_dest_incr = -x_dest_incr;
    } 

	y_dest_incr = y_dest_incr * sbufWidth;
	if(!(transform & TRANSFORM_INVERTED_AXES)){
		y_src_incr = y_src_incr * nScanLen;
	}
	x_dest_incr *= pix_size_dest;
	y_dest_incr *= pix_size_dest;
	x_src_incr *= pix_size_src;
	y_src_incr *= pix_size_src;
	width_dest_incr = t_width*x_dest_incr;
	width_src_incr = t_width*x_src_incr;

	destBitsPtr = ((unsigned char *)sbuf->pixelData) + ((y_dest * sbufWidth + x_dest) * pix_size_dest);
	srcBitsPtr = (unsigned char *)(dataSrc + ((y_src * nScanLen + x_src)/* * pix_size_src*/));	

	destBitsLimit = ((unsigned char *)sbuf->pixelData) + (sbuf->width * sbuf->height * pix_size_dest);

	blocks = t_width / 8;
       remains = t_width % 8;
	
	for (j = 0; j < t_height; j++) {
           if (!processAlpha) {
               if ((destBitsPtr < destBitsLimit)&& (destBitsPtr + width_dest_incr) < destBitsLimit) {
                   int i;
                   jint* pFrom = (jint*)srcBitsPtr;
                   gxj_pixel_type* pTo = (gxj_pixel_type*)destBitsPtr;
                 if (x_dest_incr > 0) {
                   for (i = 0; i< blocks; i++) {
                   	  *pTo++ = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
                   	  *pTo++ = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
                   	  *pTo++ = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
                   	  *pTo++ = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
                   	  *pTo++ = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
                   	  *pTo++ = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
                   	  *pTo++ = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
                   	  *pTo++ = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
                   }
                   switch (remains) {
                   	case 7:  *pTo++ = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
                   	case 6:  *pTo++ = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
                   	case 5:  *pTo++ = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
                   	case 4:  *pTo++ = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
                   	case 3:  *pTo++ = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
                   	case 2:  *pTo++ = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
                   	case 1:  *pTo++ = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
                   }
                 } else {
                   for (i = 0; i< blocks; i++) {
                   	  *pTo-- = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
                   	  *pTo-- = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
                   	  *pTo-- = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
                   	  *pTo-- = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
                   	  *pTo-- = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
                   	  *pTo-- = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
                   	  *pTo-- = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
                   	  *pTo-- = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
                   }
                   switch (remains) {
                   	case 7:  *pTo-- = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
                   	case 6:  *pTo-- = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
                   	case 5:  *pTo-- = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
                   	case 4:  *pTo-- = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
                   	case 3:  *pTo-- = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
                   	case 2:  *pTo-- = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
                   	case 1:  *pTo-- = GXJ_RGB24TORGB16((*pFrom)); pFrom++;
                   }
                 }
           	 }
               destBitsPtr += y_dest_incr;
   		 srcBitsPtr += y_src_incr;
        } else {

		for (i = 0; i < t_width; i++) {
			
			//M@x: never draw out of screen buffer
			if (destBitsPtr < destBitsLimit) {
				
        			alpha = srcBitsPtr[3];
        			if (alpha == 0xff || (processAlpha == KNI_FALSE)) {
        				*(gxj_pixel_type*)destBitsPtr = GXJ_RGB24TORGB16(*(jint*)srcBitsPtr);
        			} else if (alpha != 0x00) {  /* needs blending */
                        		jint background = GXJ_RGB16TORGB24(*(gxj_pixel_type*)destBitsPtr);
                        		jint composition = alphaComposition(*(jint*)srcBitsPtr, background);
                        		*(gxj_pixel_type*)destBitsPtr = GXJ_RGB24TORGB16(composition);
        			}		
			}
			destBitsPtr += x_dest_incr;//*pix_size_dest;
			srcBitsPtr += x_src_incr;//*pix_size_src;
		}
		destBitsPtr += y_dest_incr - (width_dest_incr);
		srcBitsPtr += y_src_incr - (width_src_incr);
	}
    }
}
/**
 * Obtain the color that will be final shown 
 * on the screen after the system processed it.
 */
int
gx_get_displaycolor(int color) {
    int newColor = GXJ_RGB16TORGB24(GXJ_RGB24TORGB16(color));

    REPORT_CALL_TRACE1(LC_LOWUI, "gx_getDisplayColor(%d)\n", color);

    /*
     * JAVA_TRACE("color %x  -->  %x\n", color, newColor);
     */

    return newColor;
}


/**
 * Draw a line between two points (x1,y1) and (x2,y2).
 */
void
gx_draw_line(int color, const jshort *clip, 
	      const java_imagedata *dst, int dotted, 
              int x1, int y1, int x2, int y2)
{
  int lineStyle = (dotted ? DOTTED : SOLID);
  gxj_pixel_type pixelColor = GXJ_RGB24TORGB16(color);
  gxj_screen_buffer screen_buffer;
  gxj_screen_buffer *sbuf = gxj_get_image_screen_buffer_impl(dst, &screen_buffer, NULL);
  sbuf = (gxj_screen_buffer *)getScreenBuffer(sbuf);
  
  REPORT_CALL_TRACE(LC_LOWUI, "gx_draw_line()\n");
  
  draw_clipped_line(sbuf, pixelColor, lineStyle, clip, x1, y1, x2, y2);
}

/**
 * Draw a rectangle at (x,y) with the given width and height.
 *
 * @note x, y sure to be >=0
 *       since x,y is quan. to be positive (>=0), we don't
 *       need to test for special case anymore.
 */
void 
gx_draw_rect(int color, const jshort *clip, 
	      const java_imagedata *dst, int dotted, 
              int x, int y, int width, int height)
{

  int lineStyle = (dotted ? DOTTED : SOLID);
  gxj_pixel_type pixelColor = GXJ_RGB24TORGB16(color);
  gxj_screen_buffer screen_buffer;
  gxj_screen_buffer *sbuf = gxj_get_image_screen_buffer_impl(dst, &screen_buffer, NULL);
  sbuf = (gxj_screen_buffer *)getScreenBuffer(sbuf);

  REPORT_CALL_TRACE(LC_LOWUI, "gx_draw_rect()\n");

  draw_roundrect(pixelColor, clip, sbuf, lineStyle, x,  y, 
		 width, height, 0, 0, 0);
}


#if (UNDER_ADS || UNDER_CE) || (defined(__GNUC__) && defined(ARM))
extern void fast_pixel_set(unsigned * mem, unsigned value, int number_of_pixels);
#else
void fast_pixel_set(unsigned * mem, unsigned value, int number_of_pixels)
{
   int i;
   gxj_pixel_type* pBuf = (gxj_pixel_type*)mem;

   for (i = 0; i < number_of_pixels; ++i) {
      *(pBuf + i) = (gxj_pixel_type)value;
   }
}
#endif

void fastFill_rect(unsigned short color, gxj_screen_buffer *sbuf, int x, int y, int width, int height, int cliptop, int clipbottom) {
	int screen_horiz=sbuf->width;
	unsigned short* raster;

    if (width<=0) {return;}
	if (x > screen_horiz) { return; }
	if (y > sbuf->height) { return; }
	if (x < 0) { width+=x; x=0; }
	if (y < cliptop) { height+=y-cliptop; y=cliptop; }
	if (x+width  > screen_horiz) { width=screen_horiz - x; }
	if (y+height > clipbottom) { height= clipbottom - y; }


	raster=sbuf->pixelData + y*screen_horiz+x;
	for(;height>0;height--) {
		fast_pixel_set((unsigned *)raster, color, width);
		raster+=screen_horiz;
	}
}

/**
 * Fill a rectangle at (x,y) with the given width and height.
 */
void 
gx_fill_rect(int color, const jshort *clip, 
	      const java_imagedata *dst, int dotted, 
              int x, int y, int width, int height) {

  gxj_pixel_type pixelColor = GXJ_RGB24TORGB16(color);
  gxj_screen_buffer screen_buffer;
  const jshort clipX1 = clip[0];
  const jshort clipY1 = clip[1];
  const jshort clipX2 = clip[2];
  const jshort clipY2 = clip[3];
  gxj_screen_buffer *sbuf = gxj_get_image_screen_buffer_impl(dst, &screen_buffer, NULL);
  sbuf = (gxj_screen_buffer *)getScreenBuffer(sbuf);


  if ((clipX1==0)&&(clipX2==sbuf->width)&&(dotted!=DOTTED)) {
    fastFill_rect(pixelColor, sbuf, x, y, width, height, clipY1, clipY2 );
    return;
  }

  
  REPORT_CALL_TRACE(LC_LOWUI, "gx_fill_rect()\n");

  draw_roundrect(pixelColor, clip, sbuf, dotted?DOTTED:SOLID, 
		 x, y, width, height, 1, 0, 0);
}

/**
 * Draw a rectangle at (x,y) with the given width and height. arcWidth and
 * arcHeight, if nonzero, indicate how much of the corners to round off.
 */
void 
gx_draw_roundrect(int color, const jshort *clip, 
		   const java_imagedata *dst, int dotted, 
                   int x, int y, int width, int height,
                   int arcWidth, int arcHeight)
{
  int lineStyle = (dotted?DOTTED:SOLID);
  gxj_pixel_type pixelColor = GXJ_RGB24TORGB16(color);
  gxj_screen_buffer screen_buffer;
  gxj_screen_buffer *sbuf = gxj_get_image_screen_buffer_impl(dst, &screen_buffer, NULL);
  sbuf = (gxj_screen_buffer *)getScreenBuffer(sbuf);

  REPORT_CALL_TRACE(LC_LOWUI, "gx_draw_roundrect()\n");

  /* API of the draw_roundrect requests radius of the arc at the four */
  draw_roundrect(pixelColor, clip, sbuf, lineStyle, 
		 x, y, width, height,
		 0, arcWidth >> 1, arcHeight >> 1);
}

/**
 * Fill a rectangle at (x,y) with the given width and height. arcWidth and
 * arcHeight, if nonzero, indicate how much of the corners to round off.
 */
void 
gx_fill_roundrect(int color, const jshort *clip, 
		   const java_imagedata *dst, int dotted, 
                   int x, int y, int width, int height,
                   int arcWidth, int arcHeight)
{
  int lineStyle = (dotted?DOTTED:SOLID);
  gxj_pixel_type pixelColor = GXJ_RGB24TORGB16(color);
  gxj_screen_buffer screen_buffer;
  gxj_screen_buffer *sbuf = gxj_get_image_screen_buffer_impl(dst, &screen_buffer, NULL);
  sbuf = (gxj_screen_buffer *)getScreenBuffer(sbuf);

  REPORT_CALL_TRACE(LC_LOWUI, "gx_fillround_rect()\n");

  draw_roundrect(pixelColor, clip, sbuf, lineStyle, 
		 x,  y,  width,  height,
		 1, arcWidth >> 1, arcHeight >> 1);
}

/**
 *
 * Draw an elliptical arc centered in the given rectangle. The
 * portion of the arc to be drawn starts at startAngle (with 0 at the
 * 3 o'clock position) and proceeds counterclockwise by <arcAngle> 
 * degrees.  arcAngle may not be negative.
 *
 * @note: check for width, height <0 is done in share layer
 */
void 
gx_draw_arc(int color, const jshort *clip, 
	     const java_imagedata *dst, int dotted, 
             int x, int y, int width, int height,
             int startAngle, int arcAngle)
{
  int lineStyle = (dotted?DOTTED:SOLID);
  gxj_pixel_type pixelColor = GXJ_RGB24TORGB16(color);
  gxj_screen_buffer screen_buffer;
  gxj_screen_buffer *sbuf = gxj_get_image_screen_buffer_impl(dst, &screen_buffer, NULL);
  sbuf = (gxj_screen_buffer *)getScreenBuffer(sbuf);

  draw_arc(pixelColor, clip, sbuf, lineStyle, x, y, 
	   width, height, 0, startAngle, arcAngle);
}

/**
 * Fill an elliptical arc centered in the given rectangle. The
 * portion of the arc to be drawn starts at startAngle (with 0 at the
 * 3 o'clock position) and proceeds counterclockwise by <arcAngle> 
 * degrees.  arcAngle may not be negative.
 */
void 
gx_fill_arc(int color, const jshort *clip, 
	     const java_imagedata *dst, int dotted, 
             int x, int y, int width, int height,
             int startAngle, int arcAngle)
{
  int lineStyle = (dotted?DOTTED:SOLID);
  gxj_pixel_type pixelColor = GXJ_RGB24TORGB16(color);
  gxj_screen_buffer screen_buffer;
  gxj_screen_buffer *sbuf = 
      gxj_get_image_screen_buffer_impl(dst, &screen_buffer, NULL);
  sbuf = (gxj_screen_buffer *)getScreenBuffer(sbuf);

  REPORT_CALL_TRACE(LC_LOWUI, "gx_fill_arc()\n");

  draw_arc(pixelColor, clip, sbuf, lineStyle, 
	   x, y, width, height, 1, startAngle, arcAngle);
}

/**
 * Return the pixel value.
 */
int
gx_get_pixel(int rgb, int gray, int isGray) {

    REPORT_CALL_TRACE3(LC_LOWUI, "gx_getPixel(%x, %x, %d)\n",
            rgb, gray, isGray);

    /* Surpress unused parameter warnings */
    (void)gray;
    (void)isGray;

    return rgb;
}
