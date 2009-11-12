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
 
/* NB: This version is built against the whole internal bitmap, placed in
	installed_tables.c. I developed it to rearrange the native CJK tables
	to the Unicode arrangement rather than doing it runtime with uni2cjk.c.
	/ AJM
*/

#include <string.h>
#include <stdio.h>
#include "installed_tables.h"

#define BUFY 20
#define BUFX 20


char buf[BUFY][BUFX];

unsigned char BitMask[8] = {0x80,0x40,0x20,0x10,0x8,0x4,0x2,0x1};

int width;
int height;

const char* LINE = "------------------------";
char line[23];

void prepline(int w) {
	strncpy(line, LINE, w); }
	
// Draw a 16 x 16 blank character, for the CJK blanks
// 
void draw_blank(unsigned int c) {
	printf("#%s\n", line);
  printf(": %x\n", c);
	printf("#%s\n", line);
	printf("*************** .\n");
	for(int i=0; i<14; i++) {
		printf("*             * .\n"); }
	printf("*************** .\n");
	printf("#%s\n", line); }

static void intdrawChar(unsigned char c, unsigned char *fontbitmap, unsigned long mapLen,
                     int fontWidth, int fontHeight, unsigned char high_byte,
										 unsigned char first_low, int ucode) {
    int i;
    int j;
    int xDest;
    int yDest;
    unsigned long byteIndex;
    int bitOffset;
    unsigned long pixelIndex;
    unsigned char bitmapByte;
    unsigned long firstPixelIndex =
        (FONT_DATA * 8) + (c * fontHeight * fontWidth);

    memset(buf,' ',sizeof(buf));

    for (i = 0; i < fontHeight; i++) {
        for (j = 0; j < fontWidth; j++) {
            pixelIndex = firstPixelIndex + (i * fontWidth) + j;
            byteIndex = pixelIndex / 8;

            if (byteIndex >= mapLen) {
                break;
            }

            bitmapByte = fontbitmap[byteIndex];
            bitOffset = pixelIndex % 8;

            /* we don't draw "background" pixels, only foreground */
            if ((bitmapByte & BitMask[bitOffset]) != 0) {
		buf[j][i]='*';
            }
        }
    }
    
    printf("#%s\n", line);
    int gn = (high_byte<<8)+(c+first_low);
    if (ucode != -1) {
			gn = ucode; }
    printf(": %x\n",gn);
    printf("#%s\n", line);
    for(i=0;i<fontHeight;i++) {
	for(j=0;j<fontWidth;j++) {
	    printf("%c",buf[j][i]);
	}
	printf(".\n");
    }
    printf("#%s\n", line);
}


static void drawChar(unsigned char c, unsigned char *fontbitmap, unsigned long mapLen,
	int fontWidth, int fontHeight, unsigned char high_byte,
		unsigned char first_low) {
	return intdrawChar(c, fontbitmap, mapLen, fontWidth, fontHeight,
		high_byte,first_low,-1); }

int table_out(int n, const char* argv[])
{
	if (n < 3) {
		printf ("Need table no.\n");
		return -1; }
	int fi;
	if (sscanf(argv[2], "%i", &fi)!=1) {
		printf ("Need table no.\n");
		return -1; }
	if ((fi<1) || (fi>130)) {
		printf("Range is 1 to 130\n");
		return -1;
	}
	// Processing here
	pfontbitmap TheFontBitmap = FontBitmaps[fi];
  width = TheFontBitmap[FONT_WIDTH];
  height = TheFontBitmap[FONT_HEIGHT];
  prepline(width);
  printf("# Font parameters:\n");
  printf("# width height ascent descent leading high_code low_code_first low_code_last\n");
  printf("@ %i %i %i %i %i\n%% %x %x %x\n",width, height,TheFontBitmap[FONT_ASCENT],TheFontBitmap[FONT_DESCENT],TheFontBitmap[FONT_LEADING]
		,TheFontBitmap[FONT_CODE_RANGE_HIGH] ,TheFontBitmap[FONT_CODE_FIRST_LOW]
		,TheFontBitmap[FONT_CODE_LAST_LOW]);
  int lastchar = 1 + TheFontBitmap[FONT_CODE_LAST_LOW]
		- TheFontBitmap[FONT_CODE_FIRST_LOW];
  int i;
  int csize = (lastchar*width*height+7)/8+FONT_DATA;
  for(i=0;i<lastchar;i++) {
      drawChar(i,TheFontBitmap,csize,
				width,height,TheFontBitmap[FONT_CODE_RANGE_HIGH],
				TheFontBitmap[FONT_CODE_FIRST_LOW]);
  }
	return 0;
}

int char_out(int ch, int u) {
	for(int i=1; i<131; i++) {
		pfontbitmap p = FontBitmaps[i];
		unsigned char hb = p[FONT_CODE_RANGE_HIGH];
		unsigned char fl = p[FONT_CODE_FIRST_LOW];
		unsigned char ll = p[FONT_CODE_LAST_LOW];
		unsigned int lc = (hb << 8) | fl;
		unsigned int hc = (hb << 8) | ll;
		if (ch < lc) {
			continue; }
		if (ch > hc) {
			continue; }
		unsigned char w = p[FONT_WIDTH];
  	unsigned char h = p[FONT_HEIGHT];
  	prepline(w);
		int lastchar = 1 + ll - fl;
		int csize = (lastchar*w*h+7)/8+FONT_DATA;
	  intdrawChar(ch-lc, p, csize, w, h, hb, fl, u);
		return 0; }
	printf("No glyph\n");
	return -1; }
	
int char_out_untransformed(int n, const char* argv[])
{
	if (n < 3) {
		printf ("Need char no.\n");
		return -1; }
	int ch;
	if (sscanf(argv[2], "%04x", &ch)!=1) {
		printf ("Need char no.\n");
		return -1; }
	if ((ch<0) || (ch>0xffff)) {
		printf("Range is 0 to 0xffff\n");
		return -1; }
	return char_out(ch, -1); }
	
extern const unsigned char UNI_CJK[];

bool in_non_16bit_sxn(unsigned short ch) {
	ch >>= 8;
	switch(ch) {
		case 0x00:
		case 0x01:
		case 0x04:
		case 0x20:
		case 0xe0:
			return true;
		default:
			return false; } }

int char_out_unicode(int ch) {
	unsigned short c0 = (unsigned short)ch;
	unsigned short cjk = c0;
	if (!in_non_16bit_sxn(c0)) {
		cjk = ((short*)UNI_CJK)[c0] < 256 && ((short*)UNI_CJK)[c0] > 0?
			((short*)UNI_CJK)[c0]:
				((((short*)UNI_CJK)[c0] >> 8) & 0xff) | (((short*)UNI_CJK)[c0] << 8); }
	return char_out((int)cjk, c0); }

int char_out_unicode(int n, const char* argv[])
{
	if (n < 3) {
		printf ("Need char no.\n");
		return -1; }
	int ch;
	if (sscanf(argv[2], "%04x", &ch)!=1) {
		printf ("Need char no.\n");
		return -1; }
	if ((ch<0) || (ch>0xffff)) {
		printf("Range is 0 to 0xffff\n");
		return -1; }
	return char_out_unicode(ch);
}

bool have_glyph(int ch) {
	unsigned short c0 = (unsigned short)ch;
	unsigned short cjk = c0;
	if (!in_non_16bit_sxn(c0)) {
		cjk = ((short*)UNI_CJK)[c0] < 256 && ((short*)UNI_CJK)[c0] > 0?
			((short*)UNI_CJK)[c0]:
				((((short*)UNI_CJK)[c0] >> 8) & 0xff) | (((short*)UNI_CJK)[c0] << 8);
		if (cjk == 0x3f) {
			return false; } }
	for(int i=1; i<131; i++) {
		pfontbitmap p = FontBitmaps[i];
		unsigned char hb = p[FONT_CODE_RANGE_HIGH];
		unsigned char fl = p[FONT_CODE_FIRST_LOW];
		unsigned char ll = p[FONT_CODE_LAST_LOW];
		unsigned int lc = (hb << 8) | fl;
		unsigned int hc = (hb << 8) | ll;
		if (cjk < lc) {
			continue; }
		if (cjk > hc) {
			continue; }
		return true; }
	return false; }

bool table_range(int ch, int& l, int& h) {
	l = ch+0x100;
	for(int i = ch; i <= ch+0xff; i++) {
		if (have_glyph(i)) {
			l = i;
			break; } }
	if (l == (ch+0x100)) {
		return false; }
	h = l;
	for(int i = ch+0xff; i > l; i--) {
		if (have_glyph(i)) {
			h = i;
			break; } }
	return true; }

int table_range(int n, const char* argv[])
{
	if (n < 3) {
		printf ("Need table no.\n");
		return -1; }
	int ch;
	if (sscanf(argv[2], "%04x", &ch)!=1) {
		printf ("Need table no.\n");
		return -1; }
	ch<<=8;
	if (in_non_16bit_sxn(ch)) {
		printf ("This dumper is only for 16 bit-wide tables.\n");
		return false; }
	int l, h;
	bool r = table_range(ch, l, h);
	if (!r) {
		printf("No glyphs in range.\n");
		return 0; }
	printf("Table defined from 0x%04x-0x%04x.\n", l, h);
	return 0; }

int utable(int n, const char* argv[])
{
	if (n < 3) {
		printf ("Need table no.\n");
		return -1; }
	int ch;
	if (sscanf(argv[2], "%04x", &ch)!=1) {
		printf ("Need table no.\n");
		return -1; }
	ch<<=8;
	if (in_non_16bit_sxn(ch)) {
		printf("Known non-Han range.\n");
		return -1; }
	int l, h;
	bool r = table_range(ch, l, h);
	if (!r) {
		printf("No glyphs in range 0x%04x-0x%04x.\n", ch, ch+0xff);
		return -1; }
	printf("# Font parameters:\n");
  printf("# width height ascent descent leading high_code low_code_first low_code_last\n");
  printf("@ 16 16 16 0 0\n%% %x %x %x\n\n", (l&0xff00)>>8, (l&0x00ff), (h&0x00ff));
	for(int i=l; i<=h; i++) {
		if (have_glyph(i)) {
			char_out_unicode(i); }
		else {
			printf("# WARNING: No glyph for codepoint %04x\n", i);
			draw_blank(i); } }
	return 0;
}

int main(int n, const char* argv[]) {
	if (n < 2) {
		printf ("Need command.\n");
		return -1; }
	// Dump a table by legacy index
	if (strcmp(argv[1], "table")==0) {
		return table_out(n, argv); }
	// Dump a char by legacy codepoint (non-CJK-to-Unicode transformed)
	if (strcmp(argv[1], "char")==0) {
		return char_out_untransformed(n, argv); }
	// Dump a char by Unicode codepoint
	if (strcmp(argv[1], "uchar")==0) {
		return char_out_unicode(n, argv); }
	// Find out which characters we have from a given Unicode table
	if (strcmp(argv[1], "trange")==0) {
		return table_range(n, argv); }
	// Dump a Unicode table (in the Han range)
	if (strcmp(argv[1], "utable")==0) {
		return utable(n, argv); }
	printf("Unrecognized command\n");
	return -1;
}
