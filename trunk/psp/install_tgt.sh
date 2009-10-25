#!/bin/sh

# Install script for CFW builds

TGT_DIR=$1

cp EBOOT.PBP $TGT_DIR
cp pspkvm.ini $TGT_DIR
cp -r ../midp/build/javacall_psp/output/lib $TGT_DIR
cp -r ../midp/build/javacall_psp/output/appdb $TGT_DIR
cp usbacc.prx $TGT_DIR
cp usbgps.prx $TGT_DIR
cp utility.sym $TGT_DIR
cp -r midi_install/* $TGT_DIR
cp fontpkgs/1/* $TGT_DIR
