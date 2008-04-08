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

#include <fcCleanup.h>

#include <pcsl_file.h>
#include <pcsl_directory.h>
#include <midpStorage.h>

#include <midpSupportedDisks.h>
#include <pcsl_memory.h>
#include <string.h>


/** Use NULL to be most consistent with the MIDP and CLDC workspaces. */
#ifndef NULL
#define NULL 0
#endif

/**
 * A directory in 'appdb' storing private directories for MIDlet suites.
 *
 * The value is "private".
 */
PCSL_DEFINE_STATIC_ASCII_STRING_LITERAL_START(privateDir)
 {'p','r','i','v','a','t','e','\0'}
PCSL_DEFINE_STATIC_ASCII_STRING_LITERAL_END(privateDir);


char* localizedPrivate = NULL;


/**
 * The recursive helper function. It deletes all files inside specified
 * directory and calls itself to delete subdirectories.
 *
 * @param pDirName the directory to delete
 * @param pSep system-dependent file separator
 */
void do_cleanup(const pcsl_string* pDirName, const pcsl_string* pSep)
{
    void* fileList = NULL;
    pcsl_string fileName = PCSL_STRING_NULL;
    pcsl_string dirName  = PCSL_STRING_NULL;

    // add tailing file separator to directory name
    if (pcsl_string_cat(pDirName, pSep, &dirName) != PCSL_STRING_OK) {
        return;
    }

    fileList = pcsl_file_openfilelist(&dirName);
    if (fileList == NULL) {
        pcsl_string_free(&dirName);
        return;
    }

    // iterate over the directory's content
    while (!pcsl_file_getnextentry(fileList, &dirName, &fileName)) {
        int isDir = pcsl_file_is_directory(&fileName);
        if (isDir == 1) {
            // make recursion
            do_cleanup(&fileName, pSep);
        } else {
            // remove file
            pcsl_file_unlink(&fileName);
        }
        pcsl_string_free(&fileName);
    };

    pcsl_string_free(&dirName);
    pcsl_file_closefilelist(fileList);
    // remove empty directory
    pcsl_file_rmdir(pDirName);
}

/**
 * The function is called upon MIDlet suite removal.
 * It deletes suites's private directory and all it's content.
 *
 * @param suiteId the ID of MIDlet suite
 */
void jsr75_suite_remove_cleanup(SuiteIdType suiteId)
{
    const pcsl_string* pStg = storage_get_root(INTERNAL_STORAGE_ID);
    const jchar jsep = pcsl_file_getfileseparator();

    pcsl_string dirName1 = PCSL_STRING_NULL;
    pcsl_string dirName2 = PCSL_STRING_NULL;
    pcsl_string dirName  = PCSL_STRING_NULL;
    pcsl_string sep      = PCSL_STRING_NULL;

    if (pcsl_string_convert_from_utf16(&jsep, 1, &sep) != PCSL_STRING_OK) {
        return;
    }
    if (pcsl_string_cat(pStg, &privateDir, &dirName1) != PCSL_STRING_OK) {
        pcsl_string_free(&sep);
        return;
    }
    if (pcsl_string_cat(&dirName1, &sep, &dirName2) != PCSL_STRING_OK) {
        pcsl_string_free(&sep);
        pcsl_string_free(&dirName1);
        return;
    }
    if (pcsl_string_cat(&dirName2, midp_suiteid2pcsl_string(suiteId),
            &dirName) != PCSL_STRING_OK) {
        pcsl_string_free(&sep);
        pcsl_string_free(&dirName1);
        pcsl_string_free(&dirName2);
        return;
    }
    pcsl_string_free(&dirName1);
    pcsl_string_free(&dirName2);

    do_cleanup(&dirName, &sep);

    pcsl_string_free(&sep);
    pcsl_string_free(&dirName);
}

//------------------------------------------------------------------------------

/**
 * Gets localized private root name corresponding to the root returned by
 * <code>System.getProperty("fileconn.dir.private")</code>.
 *
 * This method is called when the <code>fileconn.dir.private.name</code> system
 * property is retrieved.
 *
 * @return the localized name for the suite's private root
 */
char* getLocalizedPrivateDir()
{
    const int disks_num = getValidDisksSize();
    int i = 0;

    // since the value doesnt change, return cached value if exists
    if (localizedPrivate != NULL) {
        return localizedPrivate;
    }

    // find and copy private root name
    for (i = 0; i < disks_num; i++) {
        if (!strcmp("private", validDisks[i][0])) {
            localizedPrivate = (char*)pcsl_mem_malloc(strlen(validDisks[i][2]) + 1);
            strcpy(localizedPrivate, validDisks[i][2]);
        }
    }
    return localizedPrivate;
}
