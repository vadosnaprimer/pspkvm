#!/bin/sh

TGT=build/gxj_font_bitmap.c

rm -rf build
mkdir build
wrfont2/wrfont2.exe header > $TGT
for file in `ls ../fontdef_ex/page*`; do
	echo "Appending from $file ..."
	echo >> $TGT
	wrfont2/wrfont2.exe table < $file >> $TGT
	done
rm -rf build/tables.txt
grep 'unsigned char' $TGT | sed 's/build\/page_..\.c:unsigned char //' | sed 's/unsigned char //' | sed 's/\[...\]//' | sed 's/\[....\]//' | sed 's/ = {//' >> build/tables.txt
echo >> $TGT
./write_mother_table.pl >> $TGT
echo >> $TGT
cp $TGT /pspkvm/midp/src/lowlevelui/graphics/gx_putpixel/native/
