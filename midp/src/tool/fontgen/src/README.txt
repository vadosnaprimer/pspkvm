Copyright  2007  Sun Microsystems, Inc. All rights reserved.
(Additions 2009 Aug /AJ Milne)

MIDP Font Generation Tools
--------------------------

These tools are used for generating and decompiling the bitmap tables for
the built-in font.

Files:
-----

rdfont.c -- converts font bitmap from the C sources to a human-editable
MIDP-FONTDEF format file

(Note: rdfont.c is now obsolete, as it expects shorter headers than are
now standard. It will not work on the current sources. I've left it in for
historical reasons, but for current tables, see rdfont_c.2.)

rdfont_2.c -- converts font bitmap from the C sources to a human-editable
MIDP-FONTDEF format file. This is an updated and modified version--it reads
format 2 files with longer headers. See also 'Notes about table versions',
below.

wrfont.c -- converts a MIDP-FONTDEF file to C source defining the
corresponding font bitmap. By default, with the V1_1 macro defined, it
generates the current (version 2, eight byte header) bitmaps.

gxj_font_bitmap.c.0 -- taken from the MIDP sources (version 1 table, with 4
byte header, works only with rdfont.c). Note that to decompile another
version 1 (5-byte header) table, you should copy it over this file, with the
table named appropriately, then rebuild rdfont.c, as it includes this file
and only does output for such a table. See also the shell script
decompile_table.sh, which does the same thing, essentially, using rdfont_2
and current (version 2, eight byte header) tables.

gxj_intern_font_bitmap.h -- taken from the MIDP sources

decompile_table.sh -- shell script to copy the target table into
bitmap_to_decompile.c.0, compile rdfont_2 against it, and run it.

gen_blanks.pl--generates empty MIDP-FONTDEF files into which you can use a
monospaced text editor to enter new glyphs. 

Pre-requisites:
--------------

1. MIDP sources
2. gcc
3. a text editor using a monospace font
4. possibly, a calculator program that can convert decimal to hexadecimal and
   back (both Windovvs and Linux have such calculators, but you will probably
	 need to switch it to a mode where dec/hex buttons are shown).
(Re 4, see also gen_ blanks.pl, which will do the hex math for you, and the
charts at http://unicode.org/charts/PDF/, which show the canonical Unicode
sets visually and with the hex offsets for each character easily visible.)


Compiling wrfont
----------------

gcc wrfont.c -o wrfont

Compiling rdfont_2 (and rdfont)
-------------------------------

Note rdfont and rdfont_2 have to be compiled against the specific table you
want to take  apart--put it in a c file called bitmap_to_decompile.c.0 in
this directory, #include "gxj_intern_font_bitmap.h" in it, and make sure the
table is called TheFontBitmap, then compile and run. Or use the shell script
decompile_table.sh with the file as the target, and it does all this for you
(using current 7-byte header files, and rdfont_2--the script also uses sed to
replace the unique table name wrfont will give to the table with
'TheFontBitmap' so rdfont_2 can compile against it properly).

Note that the format rdfont_2 expects is identical to the format wrfont
expects, so if you were to do this:

./wrfont < fontdef.txt > fonttable.c
./decompile_table.sh fonttable.c > fontdef_out.txt

.. you'd get exactly the same table back out as you put in (fontdef.txt and 
fontdef_out.txt would be the same).

Running wrfont
--------------

./wrfont <myfont.midpfontdef >my_font_bitmap_generated.c

(Re running rdfont and rdfont_2, see 'Compilation'.)

MIDPFONTDEF Format
==================

... see also sample files in ../fontdef_ex.

General Notes
-------------

Trailing whitespace ignored: All characters with the codes ASCII 0 (nul) ..
ASCII 32 (space) at the end of the line are ignored.

No tabulation: please avoid the use of the TAB symbol (ASCII 9)

Comment format:
--------------

# text
Lines that contain only whitespace (including empty ones) are also comments.
Comment lines are ignored.

Font parameters definition:
--------------------------

The character definition file should start with the line:

@ nn nn nn nn nn

where nn stand for decimal numbers specifying correspondingly: width height
ascent descent leading. For example, the line:

@ 8 16 12 4 0

... specifies bitmaps 8 bits wide, 16 bits high, with ascent 12, descent 4,
and leading 0.

Character range definition:
--------------------------

The next non-comment line should be:

% xx xx xx

where xx stand for hexadecimal encoding of, correspondingly: the high byte,
the first character's low byte, the last character's low byte. The first
and the last characters in the range MUST have the same high byte. For
example, the line:

% 00 80 ff

specifies the character range \u0080 - \u00ff.

Character definition:
--------------------

... finally, there should follow a series of character definition blocks,
one for each character in the range. Each character definition block begins
with the line:

: xx

... which is then followed by a series of pixel data lines, one for each
row of pixels in the bitmap (therefore, the number of pixel data lines in
each character definition block shall be the same as the height of the font,
as set in the font definition line).

The xx stands for a hexadecimal number representing the character's code.

Comment lines may be used before, after, or among pixel data lines.

Pixel data lines must be of width+1 characters. The last character must be
'.' (dot). Other characters MUST be either spaces ' ' or asterisks '*'.
Spaces denote pixels of background color, asterisks denote pixels that form
the symbol.

See also the contents of fontdef_ex for examples, and the script
gen_blanks.pl, which generates blank files you can fill in to generate
bitmaps for a given range of characters.

Example:

#----------
: 61
#----------
        .
        .
        .
        .
        .
        .
        .
 ****   .
    **  .
 *****  .
**  **  .
**  **  .
 *** ** .
        .
        .
        .
#----------



Notes about table versions
==========================

Soon after creation of this tool, the binary font representation changed
in MIDP. The changes are:

 1) three additional bytes in the header specifying the character range;
 2) multiple font bitmaps covering different character ranges are used.

To support both the new and the old format, the V1_1 macro was defined 
in wrfont.c. When V1_1 is defined as 1, wrfont outputs the new format
(with the 8-byte header), and when V1_1 is defined as 0, wrfont outputs
the old format (with the 5-byte header).

As to rdfont.c, it works only with the old format (with 5-byte header). (See
now rdfont_2.c, which works with the new format, with the 8-byte header).

The macro TEST controls insertion of line breaks into the generated source.
With line breaks, the generated code is human-viewable; without line breaks, 
it may be compared to the original code that didn't have any line breaks.
