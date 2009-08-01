README.TXT for fontdef_ex in fontgen
------------------------------------

This is a set of additional fonts we've been adding/are adding to the built-in typeface for the KVM. Currently in progress are Unicode page 0x0100-0x01ff (Latin Extended 1) and page 0x0400-0x04ff (Cyrillic and extensions). See the rdfont_2.c and wrfont.c tools in ../src for converting these files to and from the bit tables found in  midp/src/lowlevelui/graphics/gx_putpixel/native/gxj_font_bitmap.c. These tables create the bitmasks for the built-in font.

It also contains page_00.txt -- this is recovered from the original built-in font's page 0 bitfield, and can be used as a rough design reference on which to base character appearance/proportion, etc.

See also ../src/gen_blanks.pl for a Perl script that generates blank pages in this format for filling in.

Contents (as of this writing)

page_00.txt -- Unicode page 0x0000-0x00ff (recovered from original bitfields using rdfont_2.c)
page_01.txt -- Unicode page 0x0100-0x01ff (hand-drawn glyphs)
page_04.txt -- Unicode page 0x0400-0x04ff (hand-drawn glyphs)
