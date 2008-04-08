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
#include <locale.h>

#include <kni.h>
#include <pcsl_file.h>
#include <pcsl_directory.h>
#include <pcsl_memory.h>

#include <midpMalloc.h>
#include <midpString.h>
#include <midpError.h>
#include <midpUtilKni.h>

#include <midpNativeMounts.h>
#include <midpDirSize.h>

/*
    KNI field IDs
    Note: They are MVM-safe because they are initialized only once
    and they are thread-independent as well.
*/
static jfieldID readHandleID  = NULL;
static jfieldID writeHandleID = NULL;
static jfieldID fileNameID    = NULL;
static jfieldID rootDirID     = NULL;

//-----------------------------------------------------------------------------

#if 0
#include <stdio.h>
#define DEBUG_PRINT(s) puts(s); fflush(stdout)
#else
#define DEBUG_PRINT(s)
#endif

//#ifdef DEBUG
#if 0
#define DECLARE_MSG(n, s) static const char n[] = s;
#define EXCEPTION_MSG(s) s
#else
#define DECLARE_MSG(n, s)
#define EXCEPTION_MSG(s) ""
#endif

/*
    JSR 75 exception messages
*/
DECLARE_MSG(fcFileNameIsNull,   "file name is null")
DECLARE_MSG(fcDirNameIsNull,    "directory name is null")
DECLARE_MSG(fcFileExists,       "file already exists")
DECLARE_MSG(fcFileIsDirectory,  "file is a directory")
DECLARE_MSG(fcHandleIsNull,     "handle is null")
DECLARE_MSG(fcCreateFileFailed, "cannot create file")
DECLARE_MSG(fcCreateDirFailed,  "cannot create directory")
DECLARE_MSG(fcCloseFileFailed,  "cannot close file")
DECLARE_MSG(fcFileNotExists,    "file does not exist")
DECLARE_MSG(fcDeleteFileFailed, "cannot delete file")
DECLARE_MSG(fcDeleteDirFailed,  "cannot delete directory")
DECLARE_MSG(fcRenameFileFailed, "cannot rename file")
DECLARE_MSG(fcOpenFileFailed,   "cannot open file")
DECLARE_MSG(fcTruncFileFailed,  "cannot truncate file")
DECLARE_MSG(fcCommitFileFailed, "cannot flush file write buffer")
DECLARE_MSG(fcFileClosed,       "file is closed")
DECLARE_MSG(fcFileSizeFailed,   "cannot get file size")
DECLARE_MSG(fcFileReadFailed,   "cannot read file")
DECLARE_MSG(fcFileWriteFailed,  "cannot write file")
DECLARE_MSG(fcISAlreadyOpened,  "input stream already opened for the file connection")
DECLARE_MSG(fcOSAlreadyOpened,  "output stream already opened for the file connection")
DECLARE_MSG(fcSeekFailed,       "file seek failure")
DECLARE_MSG(fcSetAttrFailed,    "cannot set file attribute")
DECLARE_MSG(fcDirOpenFailed,    "directory opening failed")
DECLARE_MSG(fcDirCloseOpenFailed,"directory closing failed")

//-----------------------------------------------------------------------------

/**
 * The method gets 'pcsl_string' from 'Java String'.
 *
 * Note: the caller is responsible for calling
 * 'pcsl_string_free(*string);
 *  pcsl_mem_free(*string);'
 * after use.
 *
 * @param stringHandle The input 'Java String' handle
 * @param string The output 'pcsl_string'
 * @return  0 if conversion passed successfully,
 *         -1 if any error occurred.
 */
static int get_pcsl_string(jobject stringHandle, pcsl_string ** string)
{
    if (KNI_IsNullHandle(stringHandle))
    {
        KNI_ThrowNew(midpNullPointerException, EXCEPTION_MSG(fcHandleIsNull));
        return -1;
    }
    else
    {
	pcsl_string * str = (pcsl_string *)pcsl_mem_malloc(sizeof(pcsl_string));    
        
	if (str == NULL) {
	    KNI_ThrowNew(midpOutOfMemoryError, NULL);
	    return -1;
	}

 	if (midp_jstring_to_pcsl_string(stringHandle, str) != PCSL_STRING_OK) {
	    pcsl_mem_free(str);
            return -1;
	}

	* string = str;	
    }
    return 0;
}

/*
 * Helper function that frees memory allocated for fileName and rootDir.
 */
static void freeNames(jobject objectHandle)
{
    pcsl_string * fileName = NULL;
    pcsl_string * rootName = NULL;

    fileName = (pcsl_string *)(long)KNI_GetLongField(objectHandle, fileNameID);    
    if (fileName != NULL)
    {
        pcsl_string_free(fileName);
        pcsl_mem_free(fileName);	
	KNI_SetLongField(objectHandle, fileNameID, (jlong)NULL);
    }
    
    rootName = (pcsl_string *)(long)KNI_GetLongField(objectHandle, rootDirID);
    if (rootName != NULL)
    {
        pcsl_string_free(rootName);
        pcsl_mem_free(rootName);
	KNI_SetLongField(objectHandle, rootDirID, (jlong)NULL);
    }
}

/*
 * Helper function that closes file for reading or/and writing.
 *
 * @param objectHandle DefaultFileHandler object reference
 * @param flags one of the open file flags (PCSL_FILE_O_RDONLY,
 *              PCSL_FILE_O_WRONLY or PCSL_FILE_O_RDWR) defined in pcsl_file.h 
 */
static void closeHandles(jobject objectHandle, int flags)
{
    // IMPL_NOTE: pcsl file flags cannot be used as a mask due their values.
    if ((flags == PCSL_FILE_O_RDONLY) || (flags == PCSL_FILE_O_RDWR))
    {
        void* readHandle  = (void*)KNI_GetIntField(objectHandle, readHandleID);
        if (readHandle != NULL)
        {
            if (pcsl_file_close(readHandle) == -1)
            {
                KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcCloseFileFailed));
            }
            /* anyway reset the read handle */
            KNI_SetIntField(objectHandle, readHandleID, (jint)NULL);
        }
    }

    // IMPL_NOTE: pcsl file flags cannot be used as a mask due their values.
    if ((flags == PCSL_FILE_O_WRONLY) || (flags == PCSL_FILE_O_RDWR))
    {
        void* writeHandle = (void*)KNI_GetIntField(objectHandle, writeHandleID);
        if (writeHandle != NULL)
        {
            if (pcsl_file_close(writeHandle) == -1)
            {
                KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcCloseFileFailed));
            }
            /* anyway reset the write handle */
            KNI_SetIntField(objectHandle, writeHandleID, (jint)NULL);
        }
    }    
}

//-----------------------------------------------------------------------------

/*
 * public native void create() throws IOException;
 *
 * Note: 'file is not directory' is checked in J2ME code
 */
KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_create()
{
    const pcsl_string * fileName   = NULL;
    void*       handle     = NULL;
    int         existStatus =  0;
    int         openStatus  = -1;
    int         closeStatus = -1;
    const int   flags = PCSL_FILE_O_CREAT | PCSL_FILE_O_RDWR;

    DEBUG_PRINT("create");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);

    KNI_GetThisPointer(objectHandle);

    fileName = (const pcsl_string *)(long)KNI_GetLongField(objectHandle, fileNameID);

    if (fileName == NULL)
    {
        KNI_ThrowNew(midpNullPointerException, EXCEPTION_MSG(fcFileNameIsNull));
    }
    else
    {  
        /* Note: 'file is not directory' is checked in J2ME code */
        
        existStatus = pcsl_file_exist(fileName);
        if (existStatus == 1)
        {
            KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcFileExists));
        }
        else
        {
            openStatus = pcsl_file_open(fileName, flags, &handle);
            if (openStatus == -1)
            {
                KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcCreateFileFailed));
            }
            else
            {
                closeStatus = pcsl_file_close(handle);
                if (closeStatus == -1)
                {
                    KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcCloseFileFailed));
                }
            }
        }
    }

    KNI_EndHandles();
    KNI_ReturnVoid();
}

/*
 * public native void delete() throws IOException;
 *
 * Note: input stream, output stream and file itself are closed in J2ME code
 */
KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_delete()
{
    const pcsl_string * fileName = NULL;
    int existStatus      = 0;
    int deleteStatus     = -1;

    DEBUG_PRINT("delete");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);

    KNI_GetThisPointer(objectHandle);

    fileName = (const pcsl_string *)(long)KNI_GetLongField(objectHandle, fileNameID);

    if (fileName == NULL)
    {
        KNI_ThrowNew(midpNullPointerException, EXCEPTION_MSG(fcFileNameIsNull));
    }
    else
    {        
        existStatus = pcsl_file_exist(fileName);
        if (existStatus == 1)
        {            
            deleteStatus = pcsl_file_unlink(fileName);
            if (deleteStatus == -1)
            {
                KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcDeleteFileFailed));
            }
        }
        else
        {
            existStatus = pcsl_file_is_directory(fileName);
            if (existStatus == 1)
            {            
                deleteStatus = pcsl_file_rmdir(fileName);
                if (deleteStatus == -1)
                {
                    KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcDeleteDirFailed));
                }
            }
            else
            {            
                KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcFileNotExists));
            }
        }
    }

    KNI_EndHandles();
    KNI_ReturnVoid();
}

/*
 * private native void rename0(String newName) throws IOException;
 *
 * Note: input stream, output stream and file itself are closed in J2ME code.
 * newName is not null, newName contains no path specification and newName
 * is an valid filename for the platform are also checked in J2ME code.
 */
KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_rename0()
{
    pcsl_string * fileName      = NULL;
    pcsl_string * newFileName   = NULL;
    int         convertStatus = -1;
    int         existStatus   = 0;
    int         renameStatus  = -1;

    DEBUG_PRINT("rename0");

    KNI_StartHandles(2);
    KNI_DeclareHandle(objectHandle);
    KNI_DeclareHandle(newNameHandle);
    
    KNI_GetThisPointer(objectHandle);

    KNI_GetParameterAsObject(1, newNameHandle);

    fileName = (pcsl_string *)(long)KNI_GetLongField(objectHandle, fileNameID);

    if (fileName == NULL)
    {
        KNI_ThrowNew(midpNullPointerException, EXCEPTION_MSG(fcFileNameIsNull));
    }
    else
    {
        /* Note: 'newFileName is not null' is checked in J2ME code */
                
        convertStatus = get_pcsl_string(newNameHandle, &newFileName);
        if (convertStatus == 0)
        {
            /* 
               We need to check whether file/directory named 'newFileName' 
               exists because rename implementation can override it without prompting.
               Existance of 'fileName' file/directory is checked by rename function
            */
            
            existStatus = pcsl_file_exist(newFileName);
            if (existStatus == 0)
            {
                existStatus = pcsl_file_is_directory(newFileName);
            }
                        
            if (existStatus == 1)
            {
                pcsl_string_free(newFileName);
                pcsl_mem_free(newFileName);
                KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcFileExists));
            }
            else
            {                
                renameStatus = pcsl_file_rename(fileName, newFileName);
                if (renameStatus != 0)
                {
                    pcsl_string_free(newFileName);
                    pcsl_mem_free(newFileName);                    
                    KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcRenameFileFailed));
                }
                else
                {
                    KNI_SetLongField(objectHandle,
                                     fileNameID, (jlong)(long)newFileName);
                    pcsl_string_free(fileName);
                    pcsl_mem_free(fileName);
                }

            }
        }
    }

    KNI_EndHandles();
    KNI_ReturnVoid();
}

/*
 * public native void truncate(long byteOffset) throws IOException;
 *
 * Note: byteOffset is greater than zero is checked in J2ME code.
 * Open write stream is also flushed in J2ME code.
 */
KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_truncate()
{
    const pcsl_string * fileName    = NULL;
    int         existStatus = -1;
    int         openStatus  = -1;
    int         truncStatus = 0;
    void*       handle      = NULL;
    long        len         = 0;
    long        origlen     = 0;
    const int   flags       = PCSL_FILE_O_WRONLY;

    DEBUG_PRINT("truncate");    

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);
  
    KNI_GetThisPointer(objectHandle);
    
    len      = (long)KNI_GetParameterAsLong(1);
    fileName = (const pcsl_string *)(long)KNI_GetLongField(objectHandle, fileNameID);

    if (fileName == NULL)
    {
        KNI_ThrowNew(midpNullPointerException, EXCEPTION_MSG(fcFileNameIsNull));
    }
    else
    {
        /* Check the file is not directory and exists */
        existStatus = pcsl_file_exist(fileName);
        if (existStatus == 1)
        {
            openStatus = pcsl_file_open(fileName, flags, &handle);
            if (openStatus == 0)
            {
                origlen = pcsl_file_sizeofopenfile(handle);
                if (len < origlen)
                {                                    
                    truncStatus = pcsl_file_truncate(handle, len);
                }
                if (handle != NULL)
                {
                    pcsl_file_close(handle);
                }

                if (truncStatus != 0)
                {
                    KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcTruncFileFailed));
                }
            }
            else
            {
                KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcOpenFileFailed));
            }
        }
        else
        {
            KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcFileNotExists));
        }
    }

    KNI_EndHandles();
    KNI_ReturnVoid();
}

/*
 * public native long fileSize() throws IOException;
 * 
 * Note: 'file is not directory' is checked in J2ME code.
 */
KNIEXPORT KNI_RETURNTYPE_LONG
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_fileSize()
{
    const pcsl_string * fileName = NULL;
    jlong       res      = -1L;

    DEBUG_PRINT("fileSize");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);

    KNI_GetThisPointer(objectHandle);

    fileName = (const pcsl_string *)(long)KNI_GetLongField(objectHandle, fileNameID);

    if (fileName == NULL)
    {
        KNI_ThrowNew(midpNullPointerException, EXCEPTION_MSG(fcFileNameIsNull));
    }
    else
    {
        res = (jlong)pcsl_file_sizeof(fileName);
    }

    KNI_EndHandles();
    KNI_ReturnLong(res);
}

/*
 * public native long directorySize(boolean includeSubDirs) throws IOException;
 * 
 * Note: 'file is a directory' is checked in J2ME code.
 */
KNIEXPORT KNI_RETURNTYPE_LONG
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_directorySize()
{
    const pcsl_string * fileName = NULL;
    int                 recurse  = 0;
    jlong               res      = (jlong)(-1);

    DEBUG_PRINT("directorySize()");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);

    KNI_GetThisPointer(objectHandle);

    recurse = (KNI_GetParameterAsBoolean(1) == KNI_TRUE) ? 1 : 0;
    fileName = (const pcsl_string *)(long)KNI_GetLongField(objectHandle, fileNameID);

    if (NULL == fileName)
    {
        KNI_ThrowNew(midpNullPointerException, EXCEPTION_MSG(fcFileNameIsNull));
    }
    else
    {
        GET_PCSL_STRING_DATA_AND_LENGTH(fileName)
        if (PCSL_STRING_PARAMETER_ERROR(fileName)) {
            KNI_ThrowNew(midpOutOfMemoryError, NULL);
        } else {
            if (0 != fileconnection_dir_content_size(fileName_data, fileName_len, recurse, &res))
            {
                res = (jlong)(-1);
            }
        }
        RELEASE_PCSL_STRING_DATA_AND_LENGTH
    }

    KNI_EndHandles();
    KNI_ReturnLong(res);
}

/*
 * public native void mkdir() throws IOException;    
 */
KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_mkdir()
{
    const pcsl_string * fileName = NULL;
    int         res      = -1;

    DEBUG_PRINT("mkdir");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);
    
    KNI_GetThisPointer(objectHandle);

    fileName = (const pcsl_string *)(long)KNI_GetLongField(objectHandle, fileNameID);

    if (fileName == NULL)
    {
        KNI_ThrowNew(midpNullPointerException, EXCEPTION_MSG(fcDirNameIsNull));
    }
    else
    {    
        /* 
           Note: invokation on an existing directory or on any file is
           not checked because it's detected by the 'pcsl_file_mkdir'
         */
        res = pcsl_file_mkdir(fileName);
        if (res == -1)
        {
            KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcCreateDirFailed));
        }
    }

    KNI_EndHandles();
    KNI_ReturnVoid();
}

/*
 * public native boolean exists();
 */
KNIEXPORT KNI_RETURNTYPE_BOOLEAN
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_exists()
{
    const pcsl_string * fileName = NULL;
    int         exist    = 0;
    jboolean    res      = KNI_FALSE;

    DEBUG_PRINT("exists");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);
    
    KNI_GetThisPointer(objectHandle);

    fileName = (const pcsl_string *)(long)KNI_GetLongField(objectHandle, fileNameID);

    if (fileName == NULL)
    {
        KNI_ThrowNew(midpNullPointerException, EXCEPTION_MSG(fcFileNameIsNull));
    }
    else
    {
        exist = pcsl_file_exist(fileName);
        if (exist == 0)
        {
            exist = pcsl_file_is_directory(fileName);
        }
                
        res = (exist == 1) ? KNI_TRUE : KNI_FALSE;
    }

    KNI_EndHandles();
    KNI_ReturnBoolean(res);
}

/*
 * public native boolean isDirectory();
 */
KNIEXPORT KNI_RETURNTYPE_BOOLEAN
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_isDirectory()
{
    const pcsl_string * fileName = NULL;
    int         dirExist = 0;
    jboolean    res      = KNI_FALSE;

    DEBUG_PRINT("isDirectory");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);
    
    KNI_GetThisPointer(objectHandle);

    fileName = (const pcsl_string *)(long)KNI_GetLongField(objectHandle, fileNameID);

    if (fileName == NULL)
    {
        KNI_ThrowNew(midpNullPointerException, EXCEPTION_MSG(fcFileNameIsNull));
    }
    else
    {
        dirExist = pcsl_file_is_directory(fileName);                
        res = (dirExist == 1) ? KNI_TRUE : KNI_FALSE;
    }

    KNI_EndHandles();
    KNI_ReturnBoolean(res);
}

/*
 * public native boolean canRead();
 */
KNIEXPORT KNI_RETURNTYPE_BOOLEAN
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_canRead()
{
    const pcsl_string * fileName = NULL;
    int         value    = 0;
    int         status   = -1;
    jboolean    res      = KNI_FALSE;

    DEBUG_PRINT("canRead");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);

    KNI_GetThisPointer(objectHandle);

    fileName = (const pcsl_string *)(long)KNI_GetLongField(objectHandle, fileNameID);

    if (fileName == NULL)
    {
        KNI_ThrowNew(midpNullPointerException, EXCEPTION_MSG(fcFileNameIsNull));
    }
    else
    {        
        status = pcsl_file_get_attribute(fileName,
                                            PCSL_FILE_ATTR_READ, &value);
        res = (status != -1 && value == 1) ? KNI_TRUE : KNI_FALSE;        
    }

    KNI_EndHandles();
    KNI_ReturnBoolean(res);
}

/*
 * public native void setReadable(boolean readable);
 */
KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_setReadable()
{
    const pcsl_string * fileName = NULL;
    int         status   = -1;
    int         newvalue = 0;

    DEBUG_PRINT("setReadable");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);

    KNI_GetThisPointer(objectHandle);

    newvalue = (KNI_GetParameterAsBoolean(1) == KNI_TRUE) ? 1 : 0;
    fileName = (const pcsl_string *)(long)KNI_GetLongField(objectHandle, fileNameID);

    if (fileName == NULL)
    {
        KNI_ThrowNew(midpNullPointerException, EXCEPTION_MSG(fcFileNameIsNull));
    }
    else
    {
       status = pcsl_file_set_attribute(fileName,
                                            PCSL_FILE_ATTR_READ, newvalue);
       if (status == -1)
       {
            KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcSetAttrFailed));
       }
    }

    KNI_EndHandles();
    KNI_ReturnVoid();
}

/*
 * public native boolean canWrite();
 */
KNIEXPORT KNI_RETURNTYPE_BOOLEAN
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_canWrite()
{
    const pcsl_string * fileName = NULL;
    int         value    = 0;
    int         status   = -1;
    jboolean    res      = KNI_FALSE;

    DEBUG_PRINT("canWrite");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);

    KNI_GetThisPointer(objectHandle);

    fileName = (const pcsl_string *)(long)KNI_GetLongField(objectHandle, fileNameID);

    if (fileName == NULL)
    {
        KNI_ThrowNew(midpNullPointerException, EXCEPTION_MSG(fcFileNameIsNull));
    }
    else
    {        
        status = pcsl_file_get_attribute(fileName,
                                            PCSL_FILE_ATTR_WRITE, &value);
        res = (status != -1 && value == 1) ? KNI_TRUE : KNI_FALSE;
    }

    KNI_EndHandles();
    KNI_ReturnBoolean(res);
}

/*
 * public native void setWritable(boolean writable);
 */
KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_setWritable()
{
    const pcsl_string * fileName = NULL;
    int         status   = -1;
    int         newvalue = 0;

    DEBUG_PRINT("setWritable");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);

    KNI_GetThisPointer(objectHandle);

    newvalue = (KNI_GetParameterAsBoolean(1) == KNI_TRUE) ? 1 : 0;
    fileName = (const pcsl_string *)(long)KNI_GetLongField(objectHandle, fileNameID);

    if (fileName == NULL)
    {
        KNI_ThrowNew(midpNullPointerException, EXCEPTION_MSG(fcFileNameIsNull));
    }
    else
    {
       status = pcsl_file_set_attribute(fileName,
                                            PCSL_FILE_ATTR_WRITE, newvalue);
       if (status == -1)
       {
            KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcSetAttrFailed));
       }
    }

    KNI_EndHandles();
    KNI_ReturnVoid();
}

/*
 * private native boolean isHidden();
 */
KNIEXPORT KNI_RETURNTYPE_BOOLEAN
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_isHidden0()
{
    const pcsl_string * fileName = NULL;
    jboolean    res      = KNI_FALSE;
    int         status   = -1;
    int         value    = 0;

    DEBUG_PRINT("isHidden0");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);
    
    KNI_GetThisPointer(objectHandle);

    fileName = (const pcsl_string *)(long)KNI_GetLongField(objectHandle, fileNameID);

    if (fileName == NULL)
    {
        KNI_ThrowNew(midpNullPointerException, EXCEPTION_MSG(fcFileNameIsNull));
    }
    else
    {
        status = pcsl_file_get_attribute(fileName,
                                            PCSL_FILE_ATTR_HIDDEN, &value);
        res = (status != -1 && value == 1) ? KNI_TRUE : KNI_FALSE;
    }

    KNI_EndHandles();
    KNI_ReturnBoolean(res);
}

/*
 * private native void setHidden0(boolean hidden);
 */
KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_setHidden0()
{
    const pcsl_string * fileName = NULL;
    int         status   = -1;
    int         newvalue = 0;

    DEBUG_PRINT("setHidden0");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);

    KNI_GetThisPointer(objectHandle);

    newvalue = (KNI_GetParameterAsBoolean(1) == KNI_TRUE) ? 1 : 0;
    fileName = (const pcsl_string *)(long)KNI_GetLongField(objectHandle, fileNameID);

    if (fileName == NULL)
    {
        KNI_ThrowNew(midpNullPointerException, EXCEPTION_MSG(fcFileNameIsNull));
    }
    else
    {
       status = pcsl_file_set_attribute(fileName,
                                            PCSL_FILE_ATTR_HIDDEN, newvalue);
       if (status == -1)
       {
            KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcSetAttrFailed));
       }
    }


    KNI_EndHandles();
    KNI_ReturnVoid();
}

/*
 * private native long openDir(); 
 */
KNIEXPORT KNI_RETURNTYPE_LONG
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_openDir()
{
    const pcsl_string * fileName = NULL;
    PCSL_DEFINE_ASCII_STRING_LITERAL_START(sep)
     {'/', '\0' }
    PCSL_DEFINE_ASCII_STRING_LITERAL_END(sep);
    pcsl_string dirName  = PCSL_STRING_NULL;
    void*       fileList = NULL;
    jlong       res      = 0;    

    DEBUG_PRINT("openDir");
    
    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);
    
    KNI_GetThisPointer(objectHandle);

    fileName = (const pcsl_string *)(long)KNI_GetLongField(objectHandle, fileNameID);

    if (fileName == NULL) {        
        KNI_ThrowNew(midpNullPointerException, EXCEPTION_MSG(fcDirNameIsNull));
    } else {
	if (pcsl_string_cat(fileName, &sep, &dirName) != PCSL_STRING_OK) {
            KNI_ThrowNew(midpOutOfMemoryError, NULL);	
	} else {
	  fileList = pcsl_file_openfilelist(&dirName);        
	  pcsl_string_free(&dirName);
	  if (fileList == NULL) {
	      KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcDirOpenFailed));
	  } else {
	      res = (jlong)(long)fileList;
	  }
	}
    }

    KNI_EndHandles();
    KNI_ReturnLong(res);
}

/*
 * private native void closeDir(long dirHandle);
 */
KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_closeDir()
{    
    void* fileList = NULL;
    int   status   = -1;

    DEBUG_PRINT("closeDir");        
    
    fileList = (void*)(long)KNI_GetParameterAsLong(1);
    
    status = pcsl_file_closefilelist(fileList);
    if (status == -1)
    {
        KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcDirCloseFailed));
    }
    
    KNI_ReturnVoid();
}


/*
 * private native String dirGetNextFile(long dirHandle, boolean includeHidden);
 *
 * Note: file filter is validated and applied in J2ME code
 */
KNIEXPORT KNI_RETURNTYPE_OBJECT
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_dirGetNextFile()
{
    const pcsl_string * fileName      = NULL;
    PCSL_DEFINE_ASCII_STRING_LITERAL_START(sep)
      {'/', '\0' }
    PCSL_DEFINE_ASCII_STRING_LITERAL_END(sep);
    pcsl_string dirName       = PCSL_STRING_NULL;
    pcsl_string found         = PCSL_STRING_NULL;
    pcsl_string shortName     = PCSL_STRING_NULL;
    pcsl_string resultName    = PCSL_STRING_NULL;    
    void*       fileList      = NULL;
    int         includeHidden = 0;
    int         statusNext    = -1;
    int         statusHidden  = -1;
    int         statusDir     = -1;
    int         hidden        = 0;
    int         finish        = 0;
    
    DEBUG_PRINT("dirGetNextFile");

    KNI_StartHandles(2);
    KNI_DeclareHandle(fnameHandle);        
    KNI_DeclareHandle(objectHandle);
    
    KNI_GetThisPointer(objectHandle);

    fileList = (void*)(long)KNI_GetParameterAsLong(1);
    /* 
     * The order number of the second paramter is 3 not 2
     * because it's located after a parameter of type long
     * (parameters of type long or double take up two entries
     * on the operand stack).
     */
    includeHidden = (KNI_GetParameterAsBoolean(3) == KNI_TRUE) ? 1 : 0;
    
    fileName = (const pcsl_string *)(long)KNI_GetLongField(objectHandle, fileNameID);

    if (fileName == NULL)
    {        
        KNI_ThrowNew(midpNullPointerException, EXCEPTION_MSG(fcDirNameIsNull));
    }
    else
    {
        if (pcsl_string_cat(fileName, &sep, &dirName) != PCSL_STRING_OK) {
            KNI_ThrowNew(midpOutOfMemoryError, NULL);
        } else {
	  const jsize dirNameLen = pcsl_string_length(&dirName);
	  while (!finish) {   
            statusNext = pcsl_file_getnextentry(fileList, &dirName, &found);
                            
            if (statusNext == 0)
            {
                statusHidden = pcsl_file_get_attribute(&found,
                                            PCSL_FILE_ATTR_HIDDEN, &hidden);
                if (statusHidden == 0)		
                {
		    if (hidden == 0 || includeHidden == 1)
		    {
		        const jsize foundLen = pcsl_string_length(&found);  
                        /* remove path prefix */
			if (pcsl_string_substring(&found, 
						  dirNameLen, foundLen,
						  &shortName) != PCSL_STRING_OK) {
		            KNI_ThrowNew(midpOutOfMemoryError, NULL);
	                } else {                       
			  /* add trailing '/' if the file is a directory */
			  statusDir = pcsl_file_is_directory(&found);
			  if (statusDir == 1)
			  {
			    if (pcsl_string_cat(&shortName, &sep, &resultName) != PCSL_STRING_OK) {
                              KNI_ThrowNew(midpOutOfMemoryError, NULL);
			    }
			    pcsl_string_free(&shortName);
			  }  
			  else
                          {
                            resultName = shortName;
                          }

			  if (midp_jstring_from_pcsl_string(&resultName, fnameHandle)) {
                              KNI_ThrowNew(midpOutOfMemoryError, NULL);
			  }

			  pcsl_string_free(&resultName);
			}
                        finish = 1;
		    }
                }
            }
            else
            {                                
                KNI_ReleaseHandle(fnameHandle);
                finish = 1;                
            }
            
            pcsl_string_free(&found);
	  }
	  pcsl_string_free(&dirName);
	}
    }

    KNI_EndHandlesAndReturnObject(fnameHandle);
}

/*
 * private native String getMountedRoots();
 */
KNIEXPORT KNI_RETURNTYPE_OBJECT
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_getMountedRoots(void)
{
    char* roots = NULL;

    DEBUG_PRINT("getMountedRoots");

    KNI_StartHandles(1);
    KNI_DeclareHandle(returnValueHandle);

    roots = getMountedRoots();

    if (roots == NULL || strlen(roots) <= 0 ) {
        KNI_ReleaseHandle(returnValueHandle);
    } else {
        KNI_NewStringUTF(roots, returnValueHandle);
    }
    midpFree(roots);

    KNI_EndHandlesAndReturnObject(returnValueHandle);
}


/*
 * private native String getNativePathForRoot(String root);
 */
KNIEXPORT KNI_RETURNTYPE_OBJECT
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_getNativePathForRoot()
{    
    pcsl_string * mroot         = NULL;
    const char*   root          = NULL;
    int         convertStatus = -1;
    char*       path          = NULL;

    DEBUG_PRINT("getNativePathForRoot");

    KNI_StartHandles(2);
    KNI_DeclareHandle(rootHandle);
    KNI_DeclareHandle(returnValueHandle);

    KNI_GetParameterAsObject(1, rootHandle);

    convertStatus = get_pcsl_string(rootHandle, &mroot);
    if (convertStatus == 0)
    {
        root = (const char*)pcsl_string_get_utf8_data(mroot);
            
        if (root == NULL)
        {
            KNI_ThrowNew(midpOutOfMemoryError, NULL);
        }
        else
        {
            path = getNativePathForRoot(root);
            pcsl_string_release_utf8_data((jbyte*)root, mroot);

            if (path == NULL || strlen(path) <= 0 ) {
                KNI_ReleaseHandle(returnValueHandle);
            } else {
                KNI_NewStringUTF(path, returnValueHandle);
            }
            pcsl_mem_free(path);
        }            
	pcsl_string_free(mroot);
	pcsl_mem_free(mroot);
    }    

    KNI_EndHandlesAndReturnObject(returnValueHandle);
}


/*
 * public native long lastModified();
 */
KNIEXPORT KNI_RETURNTYPE_LONG
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_lastModified()
{
    const pcsl_string * fileName = NULL;
    jlong       res      = 0;
    long        time     = 0;
    int         status   = -1;

    DEBUG_PRINT("lastModified");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);
    
    KNI_GetThisPointer(objectHandle);

    fileName = (const pcsl_string *)(long)KNI_GetLongField(objectHandle, fileNameID);

    if (fileName == NULL)
    {
        KNI_ThrowNew(midpNullPointerException, EXCEPTION_MSG(fcFileNameIsNull));
    }
    else
    {
        status = pcsl_file_get_time(fileName,
                                     PCSL_FILE_TIME_LAST_MODIFIED, &time);
        if (status == 0)
        {
            /* convert the time from seconds to milliseconds */
            res = (jlong)time * 1000;
        }
        else
        {
            res = 0;
        }
    }

    KNI_EndHandles();
    KNI_ReturnLong(res);
}

/*
 * public native void openForRead() throws IOException;
 */
KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_openForRead()
{    
    const pcsl_string * fileName   = NULL;
    void*       handle     = NULL;
    int         dirStatus  = 0;
    int         openStatus = -1;
    const int   flags      = PCSL_FILE_O_RDONLY;

    DEBUG_PRINT("openForRead");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);
    
    KNI_GetThisPointer(objectHandle);
    
    fileName = (const pcsl_string *)(long)KNI_GetLongField(objectHandle, fileNameID);
    handle   = (void*)KNI_GetIntField(objectHandle, readHandleID);

    if (fileName == NULL)
    {
        KNI_ThrowNew(midpNullPointerException, EXCEPTION_MSG(fcFileNameIsNull));
    }
    else
    {
        if (handle == NULL)
        {
            dirStatus = pcsl_file_is_directory(fileName);
            if(dirStatus == 1)
            {
                KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcFileIsDirectory));
            }
            else
            {
                openStatus = pcsl_file_open(fileName,
                                        flags, &handle);

                if (openStatus == -1)
                {
                    KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcOpenFileFailed));
                }
                else
                {
                    KNI_SetIntField(objectHandle, readHandleID, (jint)handle);
                }
            }
        }
        else
        {
            /**
             * The specification forbids opening more than one input stream
             * for each file connection.
             */
            KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcISAlreadyOpened));
        }
    }

    KNI_EndHandles();
    KNI_ReturnVoid();
}

/*
 * public native void openForWrite() throws IOException;
 */
KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_openForWrite()
{
    const pcsl_string * fileName   = NULL;
    void*       handle     = NULL;
    int         dirStatus  = 0;
    int         openStatus = -1;
    const int   flags      = PCSL_FILE_O_WRONLY;

    DEBUG_PRINT("openForWrite");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);
    
    KNI_GetThisPointer(objectHandle);

    handle   = (void*)KNI_GetIntField(objectHandle, writeHandleID);
    fileName = (const pcsl_string *)(long)KNI_GetLongField(objectHandle, fileNameID);

    if (fileName == NULL)
    {
        KNI_ThrowNew(midpNullPointerException, EXCEPTION_MSG(fcFileNameIsNull));
    }
    else
    {
        if (handle == NULL)
        {
            dirStatus = pcsl_file_is_directory(fileName);
            if(dirStatus == 1)
            {
                KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcFileIsDirectory));
            }
            else
            {
                openStatus = pcsl_file_open(fileName,
                                        flags, &handle);

                if (openStatus == -1)
                {
                    KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcOpenFileFailed));
                }
                else
                {
                    KNI_SetIntField(objectHandle, writeHandleID, (jint)handle);
                }
            }
        }
        else
        {
            /**
             * The specification forbids opening more than one output stream
             * for each file connection.
             */
            KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcOSAlreadyOpened));
        }
    }

    KNI_EndHandles();
    KNI_ReturnVoid();
}

/*
 * public native int read(byte b[], int off, int len) throws IOException;
 */
KNIEXPORT KNI_RETURNTYPE_INT
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_read()
{
    void*  handle = NULL;    
    jint   res    = 0;    
    jbyte* data  = NULL;
    jint   offset, length;

    DEBUG_PRINT("read");

    offset = KNI_GetParameterAsInt(2);
    length = KNI_GetParameterAsInt(3);

    KNI_StartHandles(2);
    KNI_DeclareHandle(objectHandle);
    KNI_DeclareHandle(byteArrHandle);

    KNI_GetThisPointer(objectHandle);

    handle = (void*)KNI_GetIntField(objectHandle, readHandleID);

    if (handle != NULL)
    {
        data = (jbyte*)midpMalloc(length);

        if (data == NULL)
        {
            KNI_ThrowNew(midpOutOfMemoryError, NULL);
        }
        else
        {            
            res = pcsl_file_read(handle, (unsigned char*)data, length);
            if (res == -1)
            {
                KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcFileReadFailed));
            }
            else
            {
                /* Set read data. Bounds check was performed at J2ME level */
                KNI_GetParameterAsObject(1, byteArrHandle);
                KNI_SetRawArrayRegion(byteArrHandle, offset * sizeof(jbyte),
                                      res * sizeof(jbyte), data);
            }
            midpFree(data);
        }
    }
    else
    {
        KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcFileClosed));
    }

    KNI_EndHandles();
    KNI_ReturnInt(res);
}

/*
 * public native int write(byte b[], int off, int len) throws IOException;
 */
KNIEXPORT KNI_RETURNTYPE_INT
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_write()
{
    void*  handle = NULL;
    jint   res    = 0;
    jbyte* data   = NULL;
    jint offset, length;    

    DEBUG_PRINT("write");

    offset = KNI_GetParameterAsInt(2);
    length = KNI_GetParameterAsInt(3);

    KNI_StartHandles(2);
    KNI_DeclareHandle(objectHandle);
    KNI_DeclareHandle(byteArrHandle);
    
    KNI_GetThisPointer(objectHandle);

    handle = (void*)KNI_GetIntField(objectHandle, writeHandleID);

    if (handle != NULL)
    {
        data = (jbyte*)midpMalloc(length);

        if (data == NULL)
        {
            KNI_ThrowNew(midpOutOfMemoryError, NULL);
        }
        else
        {
            /* Get write data. Bounds check was performed at J2ME level */
            KNI_GetParameterAsObject(1, byteArrHandle);
            KNI_GetRawArrayRegion(byteArrHandle, offset * sizeof(jbyte),
                                  length * sizeof(jbyte), data);

            res = pcsl_file_write(handle, (unsigned char*)data, length);
            if (res == -1)
            {
                KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcFileWriteFailed));
            }
            
            midpFree(data);
        }
    }
    else
    {
        KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcFileClosed));
    }

    KNI_EndHandles();
    KNI_ReturnInt(res);
}

/*
 * public native void positionForWrite(long offset) throws IOException;
 *
 * Note: 'Offset has a negative value' is checked in J2ME code.
 */
KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_positionForWrite()
{    
    void*       handle   = NULL;
    long        offset   = 0;
    long        seek     = 0;
    long        size     = 0;

    DEBUG_PRINT("positionForWrite");    

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);
    
    KNI_GetThisPointer(objectHandle);

    offset = KNI_GetParameterAsLong(1);    
    handle = (void*)KNI_GetIntField(objectHandle, writeHandleID);    

    if (handle != NULL)
    {
        size = pcsl_file_sizeofopenfile(handle);
        if (size == -1L)
        {
            KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcFileSizeFailed));
        }
        else
        {                
            offset = (size < offset) ? size : offset;        
            seek = pcsl_file_seek(handle, offset, PCSL_FILE_SEEK_SET);
            if (seek == -1L || seek != offset)
            {
                KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcSeekFailed));
            }
        }
    }
    else
    {
        KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcFileClosed));
    }    

    KNI_EndHandles();
    KNI_ReturnVoid();
}


/*
 * public native void flush() throws IOException;
 */
KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_flush()
{
    int   status = -1;
    void* handle = NULL;

    DEBUG_PRINT("flush");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);
    
    KNI_GetThisPointer(objectHandle);

    handle = (void*)KNI_GetIntField(objectHandle, writeHandleID);

    if (handle != NULL)
    {
        status = pcsl_file_commitwrite(handle);
        if (status == -1)
        {
            KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcCommitFileFailed));
        }
    }
    
    KNI_EndHandles();
    KNI_ReturnVoid();
}

/*
 * public native long availableSize
 */
KNIEXPORT KNI_RETURNTYPE_LONG
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_availableSize()
{
    const pcsl_string * rootName = NULL;
    jlong       res      = -1L;  /* -1 if the file system is not accessible*/   

    DEBUG_PRINT("availableSize");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);
    
    KNI_GetThisPointer(objectHandle);

    rootName = (const pcsl_string *)(long)KNI_GetLongField(objectHandle, rootDirID);

    if (rootName == NULL)
    {
        KNI_ThrowNew(midpNullPointerException, EXCEPTION_MSG(fcFileNameIsNull));
    }
    else
    {
        res = (jlong)pcsl_file_getfreesize(rootName);
    }

    KNI_EndHandles();
    KNI_ReturnLong(res);
}

/*
 * public native long totalSize
 */
KNIEXPORT KNI_RETURNTYPE_LONG
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_totalSize()
{
    const pcsl_string * rootName = NULL;
    jlong       res      = -1L; /* -1 if the file system is not accessible*/

    DEBUG_PRINT("totalSize");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);
    
    KNI_GetThisPointer(objectHandle);

    rootName = (const pcsl_string *)(long)KNI_GetLongField(objectHandle, rootDirID);

    if (rootName == NULL)
    {
        KNI_ThrowNew(midpNullPointerException, EXCEPTION_MSG(fcFileNameIsNull));
    }
    else
    {
        res = pcsl_file_gettotalsize(rootName);
    }

    KNI_EndHandles();
    KNI_ReturnLong(res);
}

/*
 * public native void closeForRead() throws IOException;
 */
KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_closeForRead()
{
    DEBUG_PRINT("closeForRead");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);
    
    KNI_GetThisPointer(objectHandle);
    
    closeHandles(objectHandle, PCSL_FILE_O_RDONLY);

    KNI_EndHandles();
    KNI_ReturnVoid();
}

/*
 * public native void closeForWrite() throws IOException;
 */
KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_closeForWrite()
{    
    DEBUG_PRINT("closeForWrite");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);
    
    KNI_GetThisPointer(objectHandle);

    closeHandles(objectHandle, PCSL_FILE_O_WRONLY);

    KNI_EndHandles();
    KNI_ReturnVoid();
}

/*
 * public native void closeForReadWrite() throws IOException;
 */
KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_closeForReadWrite()
{
    DEBUG_PRINT("closeForReadWrite");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);
    
    KNI_GetThisPointer(objectHandle);

    closeHandles(objectHandle, PCSL_FILE_O_RDWR);

    KNI_EndHandles();
    KNI_ReturnVoid();
}

/*
 * public native void close() throws IOException;
 */
KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_close()
{
    DEBUG_PRINT("close");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);
    
    KNI_GetThisPointer(objectHandle);

    closeHandles(objectHandle, PCSL_FILE_O_RDWR);
    freeNames(objectHandle);

    KNI_EndHandles();
    KNI_ReturnVoid();
}

/*
 * private static native long getNativeName(String name);
 */
KNIEXPORT KNI_RETURNTYPE_LONG
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_getNativeName()
{
    jlong       lname         = 0;
    pcsl_string * mname       = NULL;
    int         convertStatus = -1;

    DEBUG_PRINT("getNativeName");
    
    KNI_StartHandles(1);
    KNI_DeclareHandle(nameHandle);

    KNI_GetParameterAsObject(1, nameHandle);

    convertStatus = get_pcsl_string(nameHandle, &mname);
    if (convertStatus == 0)
    {
        lname = (jlong)(long)mname;
    }
    
    KNI_EndHandles();
    KNI_ReturnLong(lname);
}

KNIEXPORT KNI_RETURNTYPE_OBJECT
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_illegalFileNameChars0()
{
    DEBUG_PRINT("illegalFileNameChars0");
    
    KNI_StartHandles(1);
    KNI_DeclareHandle(illegalChars);

#ifdef WIN32
    KNI_NewStringUTF("<>:\"\\|", illegalChars);
#else
    KNI_NewStringUTF(":\\", illegalChars);
#endif

    KNI_EndHandlesAndReturnObject(illegalChars);
}

/*
 * private static native char getFileSeparator();
 */
KNIEXPORT KNI_RETURNTYPE_CHAR
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_getFileSeparator()
{    
    DEBUG_PRINT("getFileSeparator");
   
    KNI_ReturnChar((jchar)pcsl_file_getfileseparator());
}

/*
 * private native void finalize();
 */
KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_finalize()
{
    DEBUG_PRINT("finalize");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);

    KNI_GetThisPointer(objectHandle);

    closeHandles(objectHandle, PCSL_FILE_O_RDWR);
    freeNames(objectHandle);

    KNI_EndHandles();
    KNI_ReturnVoid();
}

#define GET_FIELDID(id, fieldName, fieldType)                   \
    if (id == NULL)                                             \
    {                                                           \
        id = KNI_GetFieldID(classHandle, fieldName, fieldType); \
        if (id == 0) {                                          \
            KNI_ThrowNew(midpNullPointerException,              \
                "Invalid romizer settings");                    \
        }                                                       \
    }

/*
 * private native static void initialize();
 */
KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_initialize()
{
    DEBUG_PRINT("initialize");
    
#ifdef UNIX
    setlocale(LC_ALL, "");
#endif    

    KNI_StartHandles(1);
    KNI_DeclareHandle(classHandle);
    KNI_GetClassPointer(classHandle);

    GET_FIELDID(fileNameID,    "fileName",    "J")
    GET_FIELDID(rootDirID,     "rootDir",     "J")
    GET_FIELDID(writeHandleID, "writeHandle", "I")
    GET_FIELDID(readHandleID,  "readHandle",  "I")

    /* initialize native mount/unmount events listener */
    initNativeFileSystemMonitor();

    KNI_EndHandles();
    KNI_ReturnVoid();
}
