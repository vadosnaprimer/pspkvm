#!/bin/sh

# Script to write the
# midp/src/lowlevelui/graphics/gx_putpixel/native/gxj_font_bitmap.c file
# (containing the built-in font's bitmaps) directly from the contents of
# ../fontdef_ex. Uses wrfont2 and write_mother_table.pl.

TGT=build/gxj_font_bitmap.c

# Build the tool
cd wrfont2
make
cd ..

# Build the tables
rm -rf build
mkdir build
wrfont2/wrfont2.exe header > $TGT
for file in `ls ../fontdef_ex/page*`; do
	echo "Appending from $file ..."
	echo >> $TGT
	wrfont2/wrfont2.exe table < $file >> $TGT
	done
echo >> $TGT
./write_mother_table.pl $TGT >> $TGT
echo >> $TGT
cp $TGT ../../../../src/lowlevelui/graphics/gx_putpixel/native/
