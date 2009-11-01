README.TXT for fontgen utilities
--------------------------------

Updated 2009 October 31 / AJM

This directory (pspkvm/midp/src/tools/fontgen) contains tools for editing
and generating the built-in (fixed-size, mono bitmap) font. As of this
writing, src contains a script (build_tables.sh) which should do the entire
job of compiling the tables from font definition files in fontdef_ex. See
src/README.TXT and fontdef_ex/README.TXT for additional information.

Subdirectories
--------------

fontdef_ex -- contains font definition tables for the built-in font. Edit
these to modify or add to the built-in font.

src -- contains tools for reading and writing fonts. Build
src/wrfont2/wrfont2 and then use the script src/build_tables.sh
to generate the file
pspkvm/midp/src/lowlevelui/graphics/gx_putpixel/native/gxj_font_bitmap.c,
prior to building PSPKVM.

Obsolete subdirectories
-----------------------

work -- OBSOLETE -- used to be the work directory for building the tables.

fontdef -- OBSOLETE -- contains legacy font definition files--superseded
by fontdef_ex.
