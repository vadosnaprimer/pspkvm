#!/bin/sh

# Include the environment we need
. $MIDLETROOT/env.sh

# Preverfy a classfile against the midlet toolchain
$JV $1
