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
#include <dirent.h>
#include <sys/stat.h>
#include <sys/types.h>

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
    char subSearch[MAX_FILE_NAME_LENGTH];
    int subSearchLen;
    struct dirent *dir_data;
    struct stat stat_buf;
    int status;
    jlong contentSize = 0;
    DIR* listHandle[MAX_DIRECTORY_NESTING_LEVEL];
    int pathLen[MAX_DIRECTORY_NESTING_LEVEL];
    int nestLevel = 0;
    int nextExists;
    pcsl_string name = PCSL_STRING_NULL;

    if (PCSL_STRING_OK != pcsl_string_convert_from_utf16(pathName, pathNameLen, &name)) {
        return -1;
    }
    if (PCSL_STRING_OK != pcsl_string_convert_to_utf8(&name, (jbyte*)subSearch, MAX_FILE_NAME_LENGTH, &subSearchLen)) {
        pcsl_string_free(&name);
        return -1;
    }
    pcsl_string_free(&name);

    listHandle[0] = opendir(subSearch);
    pathLen[0] = subSearchLen;
    if (NULL == listHandle[0]) {
        /* Cannot open directory */
        return -1;
    }
    dir_data = readdir(listHandle[0]);
    nextExists = (NULL != dir_data);

    for ( ; ; ) {
        while (nextExists) {
            subSearch[pathLen[nestLevel]] = '/';
            subSearch[pathLen[nestLevel] + 1] = 0;
            strcat(subSearch, dir_data->d_name);
            status = stat(subSearch, &stat_buf);
            if (status < 0) {
                /* Failed to get file status */
                while (nestLevel >= 0) {
                    closedir(listHandle[nestLevel--]);
                }
                return -1;
            }
            if (S_ISDIR(stat_buf.st_mode)) {
                /* Found subdirectory */
                if (includeSubdirs) {
                    /* Must count subdirectory sizes */
                    if (strcmp(dir_data->d_name, ".") && strcmp(dir_data->d_name, "..")) {
                        /* The subdirectory is not "." or ".." */
                        int dirNameLen = strlen(dir_data->d_name);
                        if (nestLevel >= MAX_DIRECTORY_NESTING_LEVEL - 1) {
                            /* Nesting level overflow */
                            while (nestLevel >= 0) {
                                closedir(listHandle[nestLevel--]);
                            }
                            return -1;
                        }
                        pathLen[nestLevel + 1] = pathLen[nestLevel] + dirNameLen + 1;
                        listHandle[++nestLevel] = opendir(subSearch);
                        if (NULL == listHandle[nestLevel]) {
                            /* Cannot open subdirectory */
                            while (--nestLevel >= 0) {
                                closedir(listHandle[nestLevel]);
                            }
                            return -1;
                        }
                    }
                }
            } else {
                contentSize += stat_buf.st_size;
            }
            dir_data = readdir(listHandle[nestLevel]);
            nextExists = (NULL != dir_data);
        }
        closedir(listHandle[nestLevel]);
        if (nestLevel == 0) {
            break;
        }
        dir_data = readdir(listHandle[--nestLevel]);
        nextExists = (NULL != dir_data);
    }

    *result = contentSize;
    return 0;
}
