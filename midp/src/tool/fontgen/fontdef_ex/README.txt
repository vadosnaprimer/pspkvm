README.TXT for fontdef_ex in fontgen
------------------------------------

This is a set of additional fonts we've been adding/are adding to the built-in typeface for the KVM. Currently in progress are Unicode page 0x0100-0x01ff (Latin Extended 1) and page 0x0400-0x04ff (Cyrillic and extensions). See the rdfont_2.c and wrfont.c tools in ../src for converting these files to and from the bit tables found in  midp/src/lowlevelui/graphics/gx_putpixel/native/gxj_font_bitmap.c. These tables create the bitmasks for the built-in font.

It also contains page_00.txt -- this is recovered from the original built-in font's page 0 bitfield, and can be used as a rough design reference on which to base character appearance/proportion, etc.

See also ../src/gen_blanks.pl for a Perl script that generates blank pages in this format for filling in.

Contents (as of this writing)

page_00.txt -- Unicode page 0x0000-0x00ff (recovered from original bitfields using rdfont_2.c)
page_01.txt -- Unicode page 0x0100-0x01ff (hand-drawn glyphs)
page_04.txt -- Unicode page 0x0400-0x045f (hand-drawn glyphs)
page_20.txt -- Unicode page 0x2010-0x20ac (hand-drawn glyphs) 
page_4e-page_99.txt -- Unicode pages 0x4e00 to 0x9fa5 (extracted from 0.5.4 release original bitfields--formerly in CJK-map order--and converted to Unicode order using rdfont_3)
page_e0.txt -- Unicode page 0xe000-x0e030 (hand-drawn glyphs)

Note that while the gen_blanks.pl tool generates full pages (range 00 to ff in the low bit), the framework for these fonts does permit smaller ranges as of this writing (note the 0x0400-0x045f range). Edit low_code_first and low_code_last in the header by hand and delete glyphs outside this range to generate valid tables containing smaller sets. 

Note also that, as of this writing, each generated page must have all glyphs the same width, and the CHAR_WIDTH macro in midp/src/lowlevelui/graphics/gx_putpixel/native/gxj_text.c must be adjusted as necessary to calculate properly which width is correct for the character in question (since, between pages, the widths are not all the same).
 
