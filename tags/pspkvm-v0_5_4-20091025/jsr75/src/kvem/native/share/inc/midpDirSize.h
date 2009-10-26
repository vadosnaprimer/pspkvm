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

#ifndef _MIDP_DIR_SIZE_H_
#define _MIDP_DIR_SIZE_H_

#include <pcsl_memory.h>

/**
 * Get size in bytes of all files and possibly subdirectories contained
 * in the specified dir.
 *
 * @param pathName          full directory path
 * @param pathNameLen       length of path name
 * @param includeSubdirs    if 0, do not include subdirectories,
 *                          otherwise include subdirectories size too
 * @param result            returned value: size in bytes of all files contained in
 *                          the specified directory and possibly its subdirectories
 * @return 0 on success, -1 otherwise
 */
int fileconnection_dir_content_size(const jchar* pathName,
                                    int pathNameLen,
                                    int includeSubdirs,
                                    jlong* /* OUT */ result);

#endif /* _MIDP_DIR_SIZE_H_ */
