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


#ifdef __cplusplus
extern "C" {
#endif
    
#include "javacall_dir.h"
#include "pspiofilemgr.h"
#include "stdlib.h"
#include "string.h"
#include "dirent.h"

extern char* javacall_UNICODEsToUtf8(const javacall_utf16* fileName, int fileNameLen);

typedef struct {
    SceUID handle;
    javacall_utf16 name[256];
    SceIoDirent* dirent;
} slot;

/**
 * returns a handle to a file list. This handle can be used in
 * subsequent calls to javacall_dir_get_next() to iterate through
 * the file list and get the names of files that match the given string.
 * 
 * @param path the name of a directory, but it can be a
 *             partial file name
 * @param pathLen length of directory name
 * @return pointer to an opaque filelist structure, that can be used in
 *         javacall_dir_get_next() and javacall_dir_close
 *         NULL returned on error, for example if root directory of the
 *         input 'string' cannot be found.
 */
javacall_handle javacall_dir_open(const javacall_utf16* path, int pathLen) {
#if 0
    SceUID dfd;
    slot *handle = NULL;
    char* szPath = javacall_UNICODEsToUtf8(path, pathLen);
    if (!szPath) {
        return NULL;
    }

   javacall_print(szPath);
   javacall_print("\n");
    
    dfd = (SceUID)sceIoDopen(szPath);
    if(dfd >= 0) {
    	handle = (slot*)malloc(sizeof(slot));
    	handle->handle = dfd;
    	handle->dirent = (SceIoDirent*)malloc(sizeof(SceIoDirent));
    	memset(handle->dirent, 0, sizeof(SceIoDirent));
    } else {
        handle = NULL;
    }
#endif
    javacall_print("javacall_dir_open:\n");
    char* szPath = javacall_UNICODEsToUtf8(path, pathLen);
    if (!szPath) {
        return NULL;
    }

    
   javacall_print(szPath);
   javacall_print("\n");
    
    DIR* handle = opendir(szPath);
    printf("return %x\n", handle);
    return (javacall_handle)handle;
}
    
/**
 * closes the specified file list. This handle will no longer be
 * associated with this file list.
 * @param handle pointer to opaque filelist struct returned by
 *               javacall_dir_open 
 */
void javacall_dir_close(javacall_handle handle) {
#if 0
    sceIoDclose(((slot*)handle)->handle);
    free(((slot*)handle)->dirent);
    free(handle);
#endif
    closedir((DIR*)handle);
}
    
/**
 * return the next filename in directory path (UNICODE format)
 * The order is defined by the underlying file system.
 * 
 * On success, the resulting file will be copied to user supplied buffer.
 * The filename returned will omit the file's path
 * 
 * @param handle pointer to filelist struct returned by javacall_dir_open
 * @param outFilenameLength will be filled with number of chars written 
 * 
 * 
 * @return pointer to UNICODE string for next file on success, 0 otherwise
 * returned param is a pointer to platform specific memory block
 * platform MUST BE responsible for allocating and freeing it.
 */
javacall_utf16* javacall_dir_get_next(javacall_handle handle, int* /*OUT*/ outFilenameLength) {
#if 0
    int result;
    slot* h = (slot*)handle;
 
    result = sceIoDread(h->handle, h->dirent); 
    if (result > 0) {
    	
    	int len = strlen(h->dirent->d_name);
    	if (len >= 256) {
    	    return NULL;
    	} else {
    	    if(h->dirent->d_stat.st_attr & FIO_SO_IFDIR) {
    	    	  h->name[len] = (javacall_utf16)'/';
    	    	  *outFilenameLength = len+1;
    	    	  h->name[len+1] = 0;
    	    } else {
    	         *outFilenameLength = len;
                h->name[len] = 0;
    	    }
           while (len--) {
               h->name[len] = h->dirent->d_name[len];
           }
    	}
    	//printf("javacall_dir_get_next: %s, len=%d\n", dir.d_name, *outFilenameLength);
    	return h->name;
    } else {
        //javacall_print("failed\n");
        return NULL;
    }
#endif
    static javacall_utf16 name[JAVACALL_MAX_FILE_NAME_LENGTH];
    
    struct dirent* d = readdir((DIR*)handle);
    if (d != NULL) {
        int len = strlen(d->d_name);
    	if (len >= 256) {
    	    return NULL;
    	} else {
    	    if(d->d_stat.st_attr & FIO_SO_IFDIR) {
    	         name[len] = (javacall_utf16)'/';
    	    	  *outFilenameLength = len+1;
    	    	  name[len+1] = 0;
    	    } else {
    	         *outFilenameLength = len;
                name[len] = 0;
    	    }
           while (len--) {
               name[len] = (javacall_utf16)d->d_name[len] & 0xff;
           }
    	}
    	return name;
    } else {
       return NULL;
    }
}
    
/**
 * Checks the size of free space in storage. 
 * @return size of free space
 */
javacall_int64 javacall_dir_get_free_space_for_java(void){
    return 0;
}
    
    
/**
 * Returns the root path of java's home directory.
 * 
 * @param rootPath returned value: pointer to unicode buffer, allocated 
 *        by the VM, to be filled with the root path.
 * @param rootPathLen IN  : lenght of max rootPath buffer
 *                    OUT : lenght of set rootPath
 * @return <tt>JAVACALL_OK</tt> if operation completed successfully
 *         <tt>JAVACALL_FAIL</tt> if an error occured
 */
javacall_result javacall_dir_get_root_path(javacall_utf16* /* OUT */ rootPath,
                                           int* /* IN | OUT */ rootPathLen) {
    static char* root = ".";
    int i;
    int len = strlen(root);
    if (*rootPathLen < len) {
    	return JAVACALL_FAIL;
    }
    
    for (i = 0; i < len; i++) {
    	rootPath[i] = (javacall_utf16)root[i];
    }
    *rootPathLen = i;
    
    return JAVACALL_OK;
}  

/**
 *  Returns file separator character used by the underlying file system
 * (usually this function will return '\\';)
 * @return 16-bit Unicode encoded file separator
 */
javacall_utf16 javacall_get_file_separator(void) {
    return '/';
}
    
    
/**
 * Check if the given path is located on secure storage
 * The function should return JAVACALL_TRUE only in the given path
 * is located on non-removable storage, and cannot be accessed by the 
 * user or overwritten by unsecure applications.
 * @return <tt>JAVACALL_TRUE</tt> if the given path is guaranteed to be on 
 *         secure storage
 *         <tt>JAVACALL_FALSE</tt> otherwise
 */
javacall_bool javacall_dir_is_secure_storage(javacall_utf16* classPath, int pathLen) {
    return JAVACALL_FALSE;
}

#ifdef __cplusplus
}
#endif

