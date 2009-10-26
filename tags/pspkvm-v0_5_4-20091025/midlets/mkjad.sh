#!/bin/sh

# mkjad.sh
# syntax ./mkjad.sh midletname entryclass description version author url > somename.jad

echo "MIDlet-Name: $1" 
echo "MIDlet-Version: $4"
echo "MIDlet-Vendor: $5"
echo "MIDlet-Description: $3"
echo "MIDlet-Info-URL: $6"
echo 'MicroEdition-Profile: MIDP-2.0'
echo 'MicroEdition-Configuration: CLDC-1.0'
echo "MIDlet-1: $1,, $2"

 