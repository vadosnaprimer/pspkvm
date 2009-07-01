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

#include <stdlib.h>
#include <midpNativeMounts.h>


/**
 * Initialize native mount/unmount events listener.
 */
void initNativeFileSystemMonitor() {
}

/**
 * Gets the mounted root file systems.
 *
 * Note: the caller is responsible for calling 'midpFree' after use.
 * @return A string containing currently mounted roots
 *          separated by '\n' character
 */
char* getMountedRoots() {
    return NULL;
}

/**
 * Gets OS path for the specified file system root.
 *
 * Note: the caller is responsible for calling 'midpFree' after use.
 * @param root Root name
 * @return The system-dependent path to access the root
 */ 
char* getNativePathForRoot(const char* root) {
    (void)root;

    return NULL;
}

/**
 * Gets localized names corresponding to roots returned by
 * FileSystemRegistry.listRoots() method. There is one localized name
 * corresponding to each root returned by method. Localized names are in the
 * same order as returned by method and are separated by ';' symbol.
 * If there is no localized name for root, non-localized (logical) name is
 * returned in the property for this root. Root names returned through this
 * property cannot contain ';' symbol.
 *
 * This method is called when the <code>fileconn.dir.roots.names</code> system
 * property is retrieved.
 *
 * @return the localized names for mounted roots separated by ';' symbol
 */
char* getLocalizedMountedRoots() {
    return NULL;
}
