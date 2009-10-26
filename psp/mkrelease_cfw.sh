#!/bin/sh

# Make release script for test builds

FULLRELEASE=pspkvm-bin-$1-cfw-allinone.zip
MINIMALRELEASE=pspkvm-bin-$1-cfw-allinone-minimal.zip
UPGRADERELEASE=pspkvm-bin-$1-cfw-upgrade.zip

MANIFEST=pspkvm-bin-$1-manifest.txt

TGT_DIR=pspkvm

# Full release

# Build CFW
./clean.sh
./build.sh
rm -rf $TGT_DIR
mkdir $TGT_DIR
# Install CFW
./install_tgt.sh $TGT_DIR
# Additional stuff for release package
cp ../Readme.txt $TGT_DIR
cp ../CHANGELOG.TXT $TGT_DIR
cp default/vmsettings.cfg $TGT_DIR
# Package CFW full
zip -r $FULLRELEASE $TGT_DIR

# Remove fonts
rm $TGT_DIR/*.ttf
rm $TGT_DIR/LICENSE_DEJA_VU.TXT
rm $TGT_DIR/LICENSE_FIREFLY.TXT
# Remove MIDI
rm $TGT_DIR/timidity.cfg
rm -rf $TGT_DIR/inst
# Package CFW minimal
zip -r $MINIMALRELEASE $TGT_DIR

# Remove .ini file
rm $TGT_DIR/pspkvm.ini
# Package CFW upgrade
zip -r $UPGRADERELEASE $TGT_DIR

# Clear out the release manifest file
rm -f $MANIFEST
# Report file contents to manifest
unzip -l $FULLRELEASE > $MANIFEST
echo >> $MANIFEST
unzip -l $MINIMALRELEASE >> $MANIFEST
echo >> $MANIFEST
unzip -l $UPGRADERELEASE >> $MANIFEST
