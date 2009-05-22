/*
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

#include <fcCleanup.h>

#include <javacall_file.h>
#include <javacall_dir.h>
#include <javacall_fileconnection.h>
#include <midpStorage.h>

#include <javacall_memory.h>
#include <string.h>
#include <midpUtilKni.h>


/** Use NULL to be most consistent with the MIDP and CLDC workspaces. */
#ifndef NULL
#define NULL 0
#endif

/**
 * This helper function deletes the specified directory and all
 * its contents.
 *
 * @param pDirName the directory to delete
 */
void do_cleanup(const pcsl_string* pDirName)
{
    pcsl_string name1 = PCSL_STRING_NULL;
    pcsl_string * pSubDir = &name1;
    pcsl_string name2 = PCSL_STRING_NULL;
    pcsl_string * pDirEntry = &name2;
    pcsl_string * tmp;
    javacall_handle listHandle;
    int nestLevel = 0;
    int pathLen;
    javacall_utf16 * fileName = NULL;
    int fileNameLen = 0;
    pcsl_string_status status;
    javacall_result res;
    int finish = 0;
    jchar sep = javacall_get_file_separator();

    // initialize current directory
    if (PCSL_STRING_OK != pcsl_string_dup(pDirName, pSubDir)) {
        return;
    }

    for ( ; ; ) {
        // open upper-level directory for listing
        GET_PCSL_STRING_DATA_AND_LENGTH(pSubDir)
        if (PCSL_STRING_PARAMETER_ERROR(pSubDir)) {
            listHandle = NULL;
        } else {
            listHandle = javacall_dir_open(pSubDir_data, pSubDir_len);
        }
        RELEASE_PCSL_STRING_DATA_AND_LENGTH
        if (NULL == listHandle) {
            pcsl_string_free(pSubDir);
            return;
        }

        // get the first entry in the current directory
        fileName = javacall_dir_get_next(listHandle, &fileNameLen);

        while (NULL != fileName) {
            // compose full path for the directory entry
            if (PCSL_STRING_OK != pcsl_string_dup(pSubDir, pDirEntry)) {
                javacall_dir_close(listHandle);
                pcsl_string_free(pSubDir);
                return;
            }
            pcsl_string_predict_size(pDirEntry, pcsl_string_length(pDirEntry) + 1 + fileNameLen);
            if (PCSL_STRING_OK != pcsl_string_append_char(pDirEntry, sep)
             || PCSL_STRING_OK != pcsl_string_append_buf(pDirEntry, fileName, fileNameLen)) {
                javacall_dir_close(listHandle);
                pcsl_string_free(pSubDir);
                pcsl_string_free(pDirEntry);
                return;
            }

            // check if directory entry is a subdirectory
            GET_PCSL_STRING_DATA_AND_LENGTH(pDirEntry)
            if (PCSL_STRING_PARAMETER_ERROR(pDirEntry)) {
                finish = 1;
            } else {
                res = javacall_fileconnection_dir_exists(pDirEntry_data, pDirEntry_len);
            }
            RELEASE_PCSL_STRING_DATA_AND_LENGTH
            if (finish) {
                javacall_dir_close(listHandle);
                pcsl_string_free(pSubDir);
                pcsl_string_free(pDirEntry);
                return;
            }
            if (JAVACALL_OK == res) {
                // found subdirectory, open it for listing
                javacall_dir_close(listHandle);
                pcsl_string_free(pSubDir);
                tmp = pDirEntry;
                pDirEntry = pSubDir;
                pSubDir = tmp;
                GET_PCSL_STRING_DATA_AND_LENGTH(pSubDir)
                if (PCSL_STRING_PARAMETER_ERROR(pSubDir)) {
                    listHandle = NULL;
                } else {
                    listHandle = javacall_dir_open(pSubDir_data, pSubDir_len);
                }
                RELEASE_PCSL_STRING_DATA_AND_LENGTH
                if (NULL == listHandle) {
                    pcsl_string_free(pSubDir);
                    return;
                }
                nestLevel++;
            } else {
                // found regular file, simply remove it
                GET_PCSL_STRING_DATA_AND_LENGTH(pDirEntry)
                if (PCSL_STRING_PARAMETER_ERROR(pDirEntry)) {
                    res = JAVACALL_FAIL;
                } else {
                    // ensure that the file is not read-only
                    javacall_fileconnection_set_writable(pDirEntry_data, pDirEntry_len, JAVACALL_TRUE);
                    res = javacall_file_delete(pDirEntry_data, pDirEntry_len);
                }
                RELEASE_PCSL_STRING_DATA_AND_LENGTH
                pcsl_string_free(pDirEntry);
                if (JAVACALL_OK != res) {
                    javacall_dir_close(listHandle);
                    pcsl_string_free(pSubDir);
                    return;
                }
            }
            // iterate through the current directory
            fileName = javacall_dir_get_next(listHandle, &fileNameLen);
        }
        javacall_dir_close(listHandle);

        // current directory is empty by now, so we can remove it
        GET_PCSL_STRING_DATA_AND_LENGTH(pSubDir)
        if (PCSL_STRING_PARAMETER_ERROR(pSubDir)) {
            res = JAVACALL_FAIL;
        } else {
            // ensure that the directory is not read-only
            javacall_fileconnection_set_writable(pSubDir_data, pSubDir_len, JAVACALL_TRUE);
            res = javacall_fileconnection_delete_dir(pSubDir_data, pSubDir_len);
        }
        RELEASE_PCSL_STRING_DATA_AND_LENGTH
        if (JAVACALL_OK != res) {
            pcsl_string_free(pSubDir);
            return;
        }

        if (nestLevel > 0) {
            // return to parent directory
            pathLen = pcsl_string_last_index_of(pSubDir, sep);
            if (pathLen < 0) {
                pcsl_string_free(pSubDir);
                return;
            }
            status = pcsl_string_substring(pSubDir, 0, pathLen, pDirEntry);
            pcsl_string_free(pSubDir);
            if (PCSL_STRING_OK != status) {
                return;
            }
            tmp = pDirEntry;
            pDirEntry = pSubDir;
            pSubDir = tmp;
            nestLevel--;
        } else {
            // the initial (top-level) directory has been processed
            pcsl_string_free(pSubDir);
            return;
        }
    }
}

/**
 * The function is called upon MIDlet suite removal.
 * It deletes suites's private directory and all it's content.
 *
 * @param pSuiteID the ID of MIDlet suite
 */

void jsr75_suite_remove_cleanup(SuiteIdType suiteId)
{
    jchar * dir;
    int len;
    jchar sep = javacall_get_file_separator();
    pcsl_string dirName = PCSL_STRING_NULL;
    pcsl_string_status res;
    dir = (jchar *)javacall_malloc(JAVACALL_MAX_FILE_NAME_LENGTH * sizeof(jchar));
    if (NULL == dir) {
        return;
    }

    if (JAVACALL_OK != javacall_fileconnection_get_private_dir(dir, JAVACALL_MAX_FILE_NAME_LENGTH)) {
        javacall_free(dir);
        return;
    }

    /* IMPL_NOTE: what about separators escaping? */
    for (len = 0; len <= JAVACALL_MAX_FILE_NAME_LENGTH && dir[len] != 0; len++) {
        if ('/' == dir[len]) {
            dir[len] = sep;
        }
    }

    res = pcsl_string_convert_from_utf16(dir, len, &dirName);
    javacall_free(dir);
    if (PCSL_STRING_OK != res) {
        return;
    }

    if (PCSL_STRING_OK != pcsl_string_append(&dirName, midp_suiteid2pcsl_string(suiteId))) {
        pcsl_string_free(&dirName);
        return;
    }

    do_cleanup(&dirName);

    pcsl_string_free(&dirName);
}
