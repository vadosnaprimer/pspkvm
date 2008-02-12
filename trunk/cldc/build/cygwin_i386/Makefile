#
#   
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

# Master Makefile for win32_i386 build.

# See ../../docs/HowToBuild.txt for build instructions. See
# ../share/README for an explanation of the build system and how to
# create a build system for a new configuration.
ifeq ($(ENABLE_PCSL), true)
PCSL_DIST_DIR           = $(PCSL_OUTPUT_DIR)/javacall_i386

CPP_INCLUDE_DIRS       += -I"$(PCSL_DIST_DIR)/inc"

PCSL_LIBS               = $(PCSL_DIST_DIR)/lib/libpcsl_memory.lib    \
                          $(PCSL_DIST_DIR)/lib/libpcsl_print.lib     \
                          $(PCSL_DIST_DIR)/lib/libpcsl_network.lib   \
                          $(PCSL_DIST_DIR)/lib/libpcsl_string.lib    \
                          $(PCSL_DIST_DIR)/lib/libpcsl_file.lib
LINK_FLAGS             += $(PCSL_LIBS)
MAKE_EXPORT_EXTRA_LIBS += $(PCSL_LIBS)
endif

IsSelfHosted = true
include ../share/root.make
