Copyright  2007  Sun Microsystems, Inc. All rights reserved.
(Additions 2008 Aug /AJ Milne)

	MIDP Font Generation Tool

Files:
-----
rdfont.c -- converts font bitmap from the C sources to a human-editable MIDP-FONTDEF format file
	(Note: rdfont.c is now obsolete, as it expects shorter headers than are now standard. It will not work on the current sources.)

rdfont_2.c -- converts font bitmap from the C sources to a human-editable MIDP-FONTDEF format file. This is an updated and modified version--it reads format 2 files with longer headers. See also 'Notes about the code', below.b

wrfont.c -- converts a MIDP-FONTDEF file to C source defining the corresponding font bitmap

gxj_font_bitmap.c -- taken from the MIDP sources (version 1 table, with 4 byte header, works only with rdfont.c)

gxj_intern_font_bitmap.h -- taken from the MIDP sources

decompile_table.sh -- shell script to copy the target table into bitmap_to_decompile.c.0, compile rdfont_2 against it, and run it.

gen_blanks.pl--generates empty MIDP-FONTDEF files into which you can use a monospaced text editor to enter new glyphs. 

Pre-requisites:
--------------

1. MIDP sources
2. gcc (I used gcc version 4.0.4 20060507 (prerelease) (Debian 4.0.3-3))
3. a text editor using a monospace font
4. probably, a calculator program that can convert decimal to hexadecimal and
   back (both Windovvs and Linux have such calculators, but you will probably need
   to switch it to a mode where dec/hex buttons are shown).
(Re 4, see also gen_ blanks.pl, which will do the hex math for you, and the charts at http://unicode.org/charts/PDF/, which show the canonical Unicode sets visually and with the hex offsets for each character easily visible.)


Compiling wrfont
----------------
gcc wrfont.c -o wrfont

Compiling rdfont_2 (and rdfont)
-------------------------------
Note rdfont and rdfont_2 have to be compiled against the specific table you want to take it apart--put it in a c file called bitmap_to_decompile.c.0 in this directory, #include "gxj_intern_font_bitmap.h" in it, and make sure the table is called TheFontBitmap, then compile and run. Or use the shell script decompile_table.sh with the file as the target, and it does all this for you (using current 7-byte header files, and rdfont_2).

Note that the format rdfont_2 expects is identical to the format wrfont expects, so if you were to do this:

./wrfont < fontdef.txt > fonttable.c
./decompile_table.sh fonttable.c

.. you'd get exactly the same table back out as you put in.

Running wrfont
--------------

./wrfont <myfont.midpfontdef >my_font_bitmap_generated.c

(Re running rdfont and rdfont_2, see 'Compilation'.)

MIDPFONTDEF Format
==================

... see also sample files in ../fontdef_ex.

General Notes
-------------

Trailing whitespace ignored: All characters with the 
codes ASCII 0 (nul) .. ASCII 32 (space) at the end of the line
are ignored.

No tabulation: please avoid the use of the TAB symbol (ASCII 9)

Comments:
#text
Lines that contain only whitespace (including empty ones) are also comments.
Comment lines are just ignored.

Font parameters definition:

@ nn nn nn nn nn

where nn stand for decimal numbers specifying correspondingly: width height ascent descent leading

The space after @ is not obligatory. The number of spaces before nn's is not important, but must be at least 1.

Character range definition:

% xx xx xx

where xx stand for hexadecimal encoding of, correspondingly:
the high byte, the first character's low byte, the last character's low byte.
The first and the last characters in the range MUST have the same high byte.
For example,
% 00 80 ff
specifies the range \u0080 - \u00ff.

Character definition:

: xx
<pixel data lines, number of them = height>

where xx stands for a HEXADECIMAL number representing the character's code.

The number of spaces between : and xx may be zero or more.

Comment lines MAY be used before, after, or among pixel data lines.

Pixel data lines MUST be of width+1 characters. The last character must be '.' (dot).
Other characters MUST be either spaces ' ' or asterisks '*'.
Spaces denote pixels of background color, asterisks denote pixels that form the symbol.

See also the contents 


Notes about the Code
====================

Soon after creation of this tool, the binary font representation has changed in MIDP.
The changes are:
1) three additional bytes in the header specifying the character range;
2) multiple font bitmaps covering different character ranges are used.

To support both the new and the old format, the V1_1 macro was defined 
in wrfont.c.
When V1_1 is defined as 1, wrfont outputs the new format (with 8-byte header),
and when V1_1 is defined as 0, wrfont outputs the old format (with 5-byte 
header).
But as to rdfont.c, it works only with the old format (with 5-byte header).
(See now rdfont_2.c, which works with the new format, with the 8-byte header).

The macro TEST controls insertion of line breaks into the generated source.
With line breaks, the generated code is human-viewable; without line breaks, 
it may be compared to the original code that didn't have any line breaks.

