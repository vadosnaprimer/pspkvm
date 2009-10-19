#!/bin/sh

# mkjad.sh
# syntax ./mkjad.sh midletname entryclass description > somename.jad

echo "MIDlet-Name: $1" 
echo 'MIDlet-Version: 1.0.0'
echo 'MIDlet-Vendor: andrew.j.milne@gmail.com'
echo "MIDlet-Description: $3"
echo 'MIDlet-Info-URL: http://accidentalweblog.org/'
echo 'MicroEdition-Profile: MIDP-2.0'
echo 'MicroEdition-Configuration: CLDC-1.0'
echo "MIDlet-1: $1,, $2"

 