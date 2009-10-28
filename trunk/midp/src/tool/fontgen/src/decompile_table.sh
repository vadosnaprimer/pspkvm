#!/bin/sh

sed 's/unsigned char page_...._..../unsigned char TheFontBitmap/' $1 > bitmap_to_decompile.c.0 
g++ -o rdfont_2.exe rdfont_2.c
./rdfont_2.exe
