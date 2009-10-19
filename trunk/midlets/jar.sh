#!/bin/sh

# Syntax: ./jar.sh jarfile.jar manifest.jad files...

# Include the environment we need
. $MIDLETROOT/env.sh

# Jar it
$JARTOOL cvfm $*
   
