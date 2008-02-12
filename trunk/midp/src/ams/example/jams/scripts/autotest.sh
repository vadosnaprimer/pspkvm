#!/bin/sh
#
# Copyright  1990-2007 Sun Microsystems, Inc. All Rights Reserved.
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
# 
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License version
# 2 only, as published by the Free Software Foundation.
# 
# This program is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# General Public License version 2 for more details (a copy is
# included at /legal/license.txt).
# 
# You should have received a copy of the GNU General Public License
# version 2 along with this work; if not, write to the Free Software
# Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
# 02110-1301 USA
# 
# Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
# Clara, CA 95054 or visit www.sun.com if you need additional
# information or have any questions.
#

#
# Sun TCK Auto Tester Script
#
# 1. Install/Update test suite
# 2. if suite installed successfully, then run suite and loop back to step 1
# 3. remove installed test suite
#

# Change the directory to the one of this script
DIR=${0%/*}
cd $DIR

if [ "x$1" = "x" ]
then
  echo "usage: autotest.sh <HTTP URL> [<domain for unsigned suites>]"
  exit
fi

prev_midlet_args="0"

while :
do
  # Extract the midlet arguments from the "[id class]" installer output
  install_output=`./runMidlet -1 com.sun.midp.scriptutil.HttpJadInstaller \
    $* | sed -e "/\[/!d;s/\[//;s/\]//"`

  # When installation is not successful, cleanup and exit.
  if [ "x${install_output}" = "x" ] ; then 
     break
  fi

  # Run the midlet specified.
  ./runMidlet $install_output

  prev_midlet_args=$install_output
done

# Remove the last installed suite
./runMidlet -1 com.sun.midp.scriptutil.SuiteRemover ${prev_midlet_args}

