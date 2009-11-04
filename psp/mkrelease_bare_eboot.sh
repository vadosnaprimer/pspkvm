#!/bin/sh
# Make release script for bare eboot releases--use for test releases
# in which the only changes are in the binary anyway.

BARERELEASE=pspkvm-bin-$1-cfw-upgrade.zip

MANIFEST=pspkvm-bin-$1-manifest.txt

TGT_DIR=pspkvm

# Build CFW
./clean.sh
./build.sh
rm -rf $TGT_DIR
mkdir $TGT_DIR
# Install
cp EBOOT.PBP $TGT_DIR
cp ../Readme.txt $TGT_DIR
cp ../CHANGELOG.TXT $TGT_DIR
cp ../INSTALL_BARE.TXT $TGT_DIR
# Package 
zip -r $BARERELEASE $TGT_DIR
# Clear out the release manifest file
rm -f $MANIFEST
# Report file contents to manifest
unzip -l $BARERELEASE > $MANIFEST
