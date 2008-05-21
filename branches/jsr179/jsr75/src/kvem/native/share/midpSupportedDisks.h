/*
 *   
 *
 * Copyright  1990-2007 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, CA 95054 or visit www.sun.com if you need additional
 * information or have any questions.
 */

#ifndef _MIDP_SUPPORTED_DISKS_H_
#define _MIDP_SUPPORTED_DISKS_H_

#ifdef __cplusplus
extern "C" {
#endif

/**
 * @def VD_DIMENSIONS
 * The dimensions of valid disks array.
 * Dimensions usage: 0 - disk, 1 - root name, 2 - localized root name.
 */
#define VD_DIMENSIONS 3

/**
 * Array storing information about all supported disks.
 */
extern const char* validDisks [][VD_DIMENSIONS];

/**
 * Retrieves number of valid disks.
 *
 * @return total amount of valid disks.
 */
int getValidDisksSize();

#ifdef __cplusplus
}
#endif

#endif /* _MIDP_SUPPORTED_DISKS_H_ */
