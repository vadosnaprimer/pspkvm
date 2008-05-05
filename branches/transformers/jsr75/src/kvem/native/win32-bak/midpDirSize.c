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

#include <string.h>
#include <windows.h>

#include <pcsl_string.h>
#include <midpDirSize.h>

#define MAX_FILE_NAME_LENGTH 4096
#define MAX_DIRECTORY_NESTING_LEVEL 2048

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
                                    jlong* /* OUT */ result) {
    wchar_t subSearch[MAX_FILE_NAME_LENGTH + 3];
    WIN32_FIND_DATAW dir_data;
    jlong contentSize = 0;
    HANDLE listHandle[MAX_DIRECTORY_NESTING_LEVEL];
    int pathLen[MAX_DIRECTORY_NESTING_LEVEL];
    int nestLevel = 0;
    int nextExists = 1;

    memcpy(subSearch, pathName, pathNameLen * sizeof(jchar));
    subSearch[pathNameLen++] = '\\';
    subSearch[pathNameLen++] = '*';
    subSearch[pathNameLen] = 0;

    listHandle[0] = FindFirstFileW(subSearch, &dir_data);
    pathLen[0] = pathNameLen - 1;
    if (INVALID_HANDLE_VALUE == listHandle[0]) {
        return -1;
    }

    for ( ; ; ) {
        while (nextExists) {
            if ((dir_data.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY) != 0) {
                // found subdirectory
                if (includeSubdirs) {
                    // must count subdirectory sizes
                    int dirNameLen = wcslen(dir_data.cFileName);
                    if (wcscmp(dir_data.cFileName, L".") && wcscmp(dir_data.cFileName, L"..")) {
                        // the subdirectory is not "." or ".."
                        if (nestLevel >= MAX_DIRECTORY_NESTING_LEVEL - 1) {
                            // nesting level overflow
                            while (nestLevel >= 0) {
                                FindClose(listHandle[nestLevel--]);
                            }
                            return -1;
                        }
                        subSearch[pathLen[nestLevel]] = 0;
                        wcscat(subSearch, dir_data.cFileName);
                        pathLen[nestLevel + 1] = pathLen[nestLevel] + dirNameLen;
                        subSearch[pathLen[++nestLevel]++] = '\\';
                        subSearch[pathLen[nestLevel]] = '*';
                        subSearch[pathLen[nestLevel] + 1] = 0;
                        listHandle[nestLevel] = FindFirstFileW(subSearch, &dir_data);
                        if (INVALID_HANDLE_VALUE == listHandle[nestLevel]) {
                            while (--nestLevel >= 0) {
                                FindClose(listHandle[nestLevel]);
                            }
                            return -1;
                        }
                        nextExists = 1;
                        continue;
                    }
                }
            } else {
                contentSize += ((jlong)(dir_data.nFileSizeHigh) << 32) + dir_data.nFileSizeLow;
            }
            nextExists = FindNextFileW(listHandle[nestLevel], &dir_data);
        }
        FindClose(listHandle[nestLevel]);
        if (nestLevel > 0) {
            nextExists = FindNextFileW(listHandle[--nestLevel], &dir_data);
        } else {
            break;
        }
    }

    *result = contentSize;
    return 0;
}
