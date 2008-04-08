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

#include <midpMountedRoots.h>

#include <midpNativeMounts.h>
#include <midpSupportedDisks.h>
#include <fcNotifyIsolates.h>

#include <pcsl_directory.h>

#include <string.h>

#include <midpMalloc.h>
#include <midpStorage.h>


MidpMountedRoots*      mr = NULL;

char* localizedNames   = NULL;

#if 0
#include <stdio.h>
#define DEBUG_PRINT(s) puts(s); fflush(stdout);
#else
#define DEBUG_PRINT(s)
#endif

//-----------------------------------------------------------------------------

extern "C" void initNativeFileSystemMonitor()
{
    DEBUG_PRINT("initNativeFileSystemMonitor")

    if (mr == NULL ) {
        mr = new MidpMountedRoots();
    }
}

// Note: the caller is responsible for calling 'midpFree' after use
extern "C" char* getMountedRoots()
{
    DEBUG_PRINT("getMountedRoots")

    if (mr != NULL) {
        char* roots = NULL;
        for (int i = 0; i < mr->getSize(); i++) {
            const char* root = (mr->getFileRoot(i) != NULL) ?
                mr->getFileRoot(i)->getRoot() : NULL;
            if (root != NULL) {
                if (roots != NULL) {
                    char* tmp =
                        (char*)midpMalloc(strlen(roots) + strlen(root) + 2);
                    strcpy(tmp, roots);
                    strcat(tmp, "\n");
                    strcat(tmp, root);
                    midpFree(roots);
                    roots = tmp;
                } else {
                    roots = (char*)midpMalloc(strlen(root) + 1);
                    strcpy(roots, root);
                }
            }
        }
        return roots;
   }
   return NULL;
}

// Note: the caller is responsible for calling 'midpFree' after use
extern "C" char* getNativePathForRoot(const char* root)
{
    DEBUG_PRINT("getNativePathForRoot")

    if (mr != NULL) {
        const char* path = mr->getPathForRoot(root);
        const int len = strlen(path);
        if (len > 0) {
            char* res = (char*)midpMalloc(len + 1);
            return strcpy(res, path);
        }
    }
    return NULL;
}

//-----------------------------------------------------------------------------

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
extern "C" char* getLocalizedMountedRoots()
{
    DEBUG_PRINT("getLocalizedMountedRoots")

    // if this is a first call then allocate memory for localized names
    if (localizedNames == NULL) {
        const int disksSize = getValidDisksSize();
        int size = 0;
        for (int i = 0; i < disksSize; i++) {
            const char* local = validDisks[i][2];
            if (local != NULL) {
               size += strlen(local);
              size++; // reserve space for ';' delimiter
           }
        }
        localizedNames = (char*)midpMalloc(size + 1);
    }

    // clear buffer content
    strcpy(localizedNames, "");

    // it could be no calls to FileConnection API before getting this system
    // property so the subsystem could be not initialized.
    initNativeFileSystemMonitor();

    for (int i = 0; i < mr->getSize(); i++) {
        const char* local = (mr->getFileRoot(i) != NULL) ?
            mr->getFileRoot(i)->getLocalRoot() : NULL;

        if (local != NULL) {
           if (strlen(localizedNames) > 0) {
               strcat(localizedNames, ";");
           }
           strcat(localizedNames, local);
        }
    }

    return !strcmp(localizedNames, "") ? NULL : localizedNames;
}

//-----------------------------------------------------------------------------

MidpMountedRoots::MidpMountedRoots() : mRootsSize(0)
{
    DEBUG_PRINT("MidpMountedRoots")

    mMaxRootsSize = getValidDisksSize();
    mRoots = (MidpFileRoot**)midpMalloc(mMaxRootsSize * sizeof(MidpFileRoot*));

    update(false);
}

MidpMountedRoots::~MidpMountedRoots()
{
    DEBUG_PRINT("~MidpMountedRoots")

    for (int i = 0; i < mRootsSize; i++) {
        /* free memory using 'delete' since it is allocated by 'new' */
        delete(mRoots[i]);
    }
    midpFree(mRoots);
}

const char* MidpMountedRoots::getPathForRoot(const char* root) const
{
    DEBUG_PRINT("getPathForRoot")

    if (root == NULL) {
        return NULL;
    }

    for (int i = 0; i < getSize(); i++) {
        const char* r = (mr->getFileRoot(i) != NULL) ?
            mr->getFileRoot(i)->getRoot() : NULL;
        if (r != NULL && !strcmp(r, root)) {
            return mr->getFileRoot(i)->getPath();
        }
    }
    return NULL;
}

int MidpMountedRoots::getDiskIndex(const char* disk) const
{
    DEBUG_PRINT("getDiskIndex")

    for (int i = 0; i < mMaxRootsSize; i++) {
        if (!strcmp(disk, validDisks[i][0])) {
            return i;
        }
    }
    return -1;
}

bool MidpMountedRoots::isCachedDisk(int diskIndex)
{
    DEBUG_PRINT("isCachedDisk")

    if (diskIndex < 0  || diskIndex >= mMaxRootsSize) {
        return false;
    }

    for (int i = 0; i < getSize(); i++) {
        int idx = (mr->getFileRoot(i) != NULL) ?
            mr->getFileRoot(i)->getDiskIndex() : -1;
        if (diskIndex == idx) {
            return true;
        }
    }
    return false;
}

void MidpMountedRoots::resetCache()
{
    DEBUG_PRINT("resetCache")

    for (int i = 0; i < mRootsSize; i++) {
        /* free memory using 'delete' since it is allocated by 'new' */
        delete(mRoots[i]);
    }
    mRootsSize = 0;
}

void MidpMountedRoots::addFileRoot(MidpFileRoot* fr)
{
  DEBUG_PRINT("addFileRoot")

  mRoots[mRootsSize] = fr;
  mRootsSize++;
}

void MidpMountedRoots::update(bool notify)
{
    DEBUG_PRINT("update")

    bool rebuild = false;
    int  count   = 0;

    int*   indexes = (int*)midpMalloc(mMaxRootsSize * sizeof(int));
    char** paths   = (char**)midpMalloc(mMaxRootsSize * sizeof(char*));

    // add special roots for PIM and FC
    const pcsl_string* stg = storage_get_root(INTERNAL_STORAGE_ID);
    if (pcsl_string_length(stg) >= 0) {
        const char* cstg = (const char*)pcsl_string_get_utf8_data(stg);


        /* FC root */

        const char* fcSubPath = "storage/";
        char* fcPath = (char*)midpMalloc(strlen(cstg) + strlen(fcSubPath) + 1);
        strcpy(fcPath, cstg);
        strcat(fcPath, fcSubPath);

        pcsl_string pcslFcPath;
        if (pcsl_string_convert_from_utf8(
            (jbyte*)fcPath, strlen(fcPath), &pcslFcPath) == PCSL_STRING_OK) {
            // determine whether the path exists and it's a directory
            // Note: the path is created by the build system.
            if (pcsl_file_is_directory(&pcslFcPath)) {
                int idx = getDiskIndex("storage");
                if (idx >= 0) {
                    count++;

                    indexes[count - 1] = idx;
                    paths  [count - 1] = fcPath;
		    
		    if (!isCachedDisk(idx)) {
                        rebuild = true;
                    }
                }
            }
            pcsl_string_free(&pcslFcPath);
        }

        /* PIM root */

        const char* pimSubPath = "pimdb/";
        char* pimPath =
            (char*)midpMalloc(strlen(cstg) + strlen(pimSubPath) + 1);
        strcpy(pimPath, cstg);
        strcat(pimPath, pimSubPath);

        pcsl_string pcslPimPath;
        if (pcsl_string_convert_from_utf8(
            (jbyte*)pimPath, strlen(pimPath), &pcslPimPath) == PCSL_STRING_OK) {
            // determine whether the path exists and it's a directory
            // Note: the path is created by the build system.
            if (pcsl_file_is_directory(&pcslPimPath)) {
                int idx = getDiskIndex("pimdb");
                if (idx >= 0) {
                   count++;

                   indexes[count - 1] = idx;
                   paths  [count - 1] = pimPath;
		   
		   if (!isCachedDisk(idx)) {
                       rebuild = true;
                   }
               }
            }
            pcsl_string_free(&pcslPimPath);
        }


        /* Private root */

        const char* privSubPath = "private/";
        char* privPath =
            (char*)midpMalloc(strlen(cstg) + strlen(privSubPath) + 1);
        strcpy(privPath, cstg);
        strcat(privPath, privSubPath);

        pcsl_string pcslPrivPath;
        if (pcsl_string_convert_from_utf8((jbyte*)privPath, strlen(privPath),
                &pcslPrivPath) == PCSL_STRING_OK) {
            // determine whether the path exists and it's a directory
            // Note: the path is created by the build system.
            if (pcsl_file_is_directory(&pcslPrivPath)) {
                int idx = getDiskIndex("private");
                if (idx >= 0) {
                   count++;

                   indexes[count - 1] = idx;
                   paths  [count - 1] = privPath;
		   
		   if (!isCachedDisk(idx)) {
                       rebuild = true;
                   }
               }
            }
            pcsl_string_free(&pcslPrivPath);
        }


        pcsl_string_release_utf8_data((const jbyte*)cstg, stg);
    }

    // refill cache if necessary
    if (rebuild || count != getSize()) {
        resetCache();
        for (int i = 0; i < count; i++) {
            MidpFileRoot* fs = new MidpFileRoot(indexes[i], paths[i]);
            addFileRoot(fs);
        }
        // call the handler
        if (notify) {
            disksChanged();
        }
    } else {
        // free unused strings
        for (int i = 0; i < count; i++) {
            midpFree(paths[i]);
        }
    }
    
    midpFree(indexes);
    midpFree(paths);
}

void MidpMountedRoots::disksChanged()
{
    DEBUG_PRINT("disksChanged")

    notifyDisksChanged();
}

const MidpFileRoot* MidpMountedRoots::getFileRoot(int index) const
{
    DEBUG_PRINT("getFileRoot")

    if (index < 0 || index >= mRootsSize) {
        return NULL;
    }
    return mRoots[index];
}

int  MidpMountedRoots::getSize() const
{
    DEBUG_PRINT("getSize")

    return mRootsSize;
}

//-----------------------------------------------------------------------------

MidpFileRoot::MidpFileRoot(int diskIndex, char* path)
        : mDiskIndex(diskIndex), mPath(path)
{
}

MidpFileRoot::~MidpFileRoot()
{
    if (mPath != NULL) {
        midpFree(mPath);
    }
}

const int MidpFileRoot::getDiskIndex() const
{
    return mDiskIndex;
}

const char* MidpFileRoot::getDisk() const
{
    return validDisks[mDiskIndex][0];
}

const char* MidpFileRoot::getPath() const
{
    return mPath;
}

const char* MidpFileRoot::getRoot() const
{
    return validDisks[mDiskIndex][1];
}

const char* MidpFileRoot::getLocalRoot() const
{
    return validDisks[mDiskIndex][2];
}
