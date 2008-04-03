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

#include <string.h>
#include <locale.h>

#include <kni.h>

#include <pcsl_memory.h>

#include <midpMalloc.h>
#include <midpString.h>
#include <midpError.h>
#include <midpDebug.h>
#include <midpUtilKni.h>

#include <javacall_defs.h>
#include <javacall_file.h>
#include <javacall_dir.h>

#include <commonKNIMacros.h>
#include <javacall_fileconnection.h>

/*
    KNI field IDs
    Note: They are MVM-safe because they are initialized only once
    and they are thread-independant as well.
*/
static jfieldID readHandleID  = NULL;
static jfieldID writeHandleID = NULL;
static jfieldID fileNameID    = NULL;
static jfieldID rootDirID     = NULL;

//-----------------------------------------------------------------------------

#define FILE_IO_ERROR   -1
#define FILE_OPEN_ERROR -2

//#if ENABLE_DEBUG
#if 1
#define DEBUG_PRINT(s)          javacall_print(s);
#define DEBUG_PRINT1(s, a1)     javacall_printf(s, a1);
#define DEBUG_PRINT2(s, a1, a2) javacall_printf(s, a1, a2);
#else
#define DEBUG_PRINT(s)
#define DEBUG_PRINT1(s, a1)
#define DEBUG_PRINT2(s, a1, a2)
#endif

#define DECLARE_MSG(n, s) static const char n[] = s;
#define EXCEPTION_MSG(s) s

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


char * fileSep             = NULL;
char * photosDir           = NULL;
char * videosDir           = NULL;
char * graphicsDir         = NULL;
char * tonesDir            = NULL;
char * musicDir            = NULL;
char * recordingsDir       = NULL;
char * privateDir          = NULL;
char * localizedRoots      = NULL;
char * localizedPhotos     = NULL;
char * localizedVideos     = NULL;
char * localizedGraphics   = NULL;
char * localizedTones      = NULL;
char * localizedMusic      = NULL;
char * localizedRecordings = NULL;
char * localizedPrivate    = NULL;

PCSL_DEFINE_STATIC_ASCII_STRING_LITERAL_START(filePrefix)
    {'f', 'i', 'l', 'e', ':', '/', '/', '/', '\0'}
PCSL_DEFINE_STATIC_ASCII_STRING_LITERAL_END(filePrefix);


/**
 * Returns pointer to character used as separator in file system paths.
 *
 * This method is called when the <code>file.separator</code> system
 * property is retrieved.
 *
 * @return file separator
 */
char* getFileSeparator() {
    jchar sep;
    jsize tmp_len;
    pcsl_string tmp = PCSL_STRING_NULL;
    pcsl_string_status res;

    if (NULL == fileSep) {
        sep = javacall_get_file_separator();
        if (PCSL_STRING_OK != pcsl_string_convert_from_utf16(&sep, 1, &tmp)) {
            return NULL;
        }
        fileSep = (char *)pcsl_mem_malloc(1);
        if (NULL == fileSep) {
            return NULL;
        }
        res = pcsl_string_convert_to_utf8(&tmp, fileSep, 1, &tmp_len);
        pcsl_string_free(&tmp);
        if (PCSL_STRING_OK != res) {
            pcsl_mem_free(fileSep);
            return NULL;
        }
    }

    return fileSep;
}

/**
 * Function type for javacall_fileconnection_get_xxx() property functions
 */
typedef javacall_result (*property_func)(javacall_utf16 *, int);

/**
 * Helper function, returns specified native property value.
 *
 * @param var pointer to variable where pointer to property value is stored
 * @param maxLength maximum number of characters in property value
 * @param get_prop javacall function to be used to query the property value
 * @param useFilePrefix if true, property value will start with "file:///" prefix
 *
 * @return property value if successful, NULL if any error occured
 */
static char* get_property(char** var, int maxLength, property_func get_prop, int useFilePrefix) {
    jchar * value = NULL;
    int len;
    jsize tmp_len;
    pcsl_string tmp = PCSL_STRING_NULL;
    pcsl_string_status res;

    value = (jchar *)pcsl_mem_malloc(maxLength * sizeof(jchar));
    if (NULL == value) {
        return NULL;
    }

    if (JAVACALL_OK != get_prop(value, maxLength)) {
        pcsl_mem_free(value);
        return NULL;
    }

    for (len = 0; len <= maxLength && value[len] != 0; len++);
    if (useFilePrefix) {
        if (PCSL_STRING_OK != pcsl_string_dup(&filePrefix, &tmp)) {
            pcsl_mem_free(value);
            return NULL;
        }
        pcsl_string_predict_size(&tmp, PCSL_STRING_LITERAL_LENGTH(filePrefix) + len);
        res = pcsl_string_append_buf(&tmp, value, len);
        pcsl_mem_free(value);
        if (PCSL_STRING_OK != res) {
            pcsl_string_free(&tmp);
            return NULL;
        }
    } else {
        res = pcsl_string_convert_from_utf16(value, len, &tmp);
        pcsl_mem_free(value);
        if (PCSL_STRING_OK != res) {
            return NULL;
        }
    }

    if (useFilePrefix) {
        maxLength += PCSL_STRING_LITERAL_LENGTH(filePrefix);
    }
    if (NULL == *var) {
        *var = (char *)pcsl_mem_malloc(maxLength);
        if (NULL == *var) {
            pcsl_string_free(&tmp);
            return NULL;
        }
    }
    res = pcsl_string_convert_to_utf8(&tmp, *var, maxLength, &tmp_len);
    pcsl_string_free(&tmp);
    if (PCSL_STRING_OK != res) {
        pcsl_mem_free(*var);
        return NULL;
    }

    return *var;
}

/**
 * Returns full path to photos storage, using '/' as separator.
 *
 * This method is called when the <code>fileconn.dir.photos</code> system
 * property is retrieved.
 *
 * @return path to photos/images storage directory
 */
char* getPhotosDir() {
    return get_property(&photosDir, JAVACALL_MAX_FILE_NAME_LENGTH,
                        javacall_fileconnection_get_photos_dir, 1);
}

/**
 * Returns full path to videos storage, using '/' as separator.
 *
 * This method is called when the <code>fileconn.dir.videos</code> system
 * property is retrieved.
 *
 * @return path to videos storage directory
 */
char* getVideosDir() {
    return get_property(&videosDir, JAVACALL_MAX_FILE_NAME_LENGTH,
                        javacall_fileconnection_get_videos_dir, 1);
}

/**
 * Returns full path to clip art graphics storage, using '/' as separator.
 *
 * This method is called when the <code>fileconn.dir.graphics</code> system
 * property is retrieved.
 *
 * @return path to clip art graphics storage directory
 */
char* getGraphicsDir() {
    return get_property(&graphicsDir, JAVACALL_MAX_FILE_NAME_LENGTH,
                        javacall_fileconnection_get_graphics_dir, 1);
}

/**
 * Returns full path to ring tones storage, using '/' as separator.
 *
 * This method is called when the <code>fileconn.dir.tones</code> system
 * property is retrieved.
 *
 * @return path to ring tones storage directory
 */
char* getTonesDir() {
    return get_property(&tonesDir, JAVACALL_MAX_FILE_NAME_LENGTH,
                        javacall_fileconnection_get_tones_dir, 1);
}

/**
 * Returns full path to music storage, using '/' as separator.
 *
 * This method is called when the <code>fileconn.dir.music</code> system
 * property is retrieved.
 *
 * @return path to music storage directory
 */
char* getMusicDir() {
    return get_property(&musicDir, JAVACALL_MAX_FILE_NAME_LENGTH,
                        javacall_fileconnection_get_music_dir, 1);
}

/**
 * Returns full path to voice recordings storage, using '/' as separator.
 *
 * This method is called when the <code>fileconn.dir.recordings</code> system
 * property is retrieved.
 *
 * @return path to voice recordings storage directory
 */
char* getRecordingsDir() {
    return get_property(&recordingsDir, JAVACALL_MAX_FILE_NAME_LENGTH,
                        javacall_fileconnection_get_recordings_dir, 1);
}

/**
 * Returns full path to private directory, using '/' as separator.
 *
 * This method is called when the <code>fileconn.dir.private</code> system
 * property is retrieved.
 *
 * @return path to the top-level private directory for all MIDlet suites
 */
char* getPrivateDir() {
    return get_property(&privateDir, JAVACALL_MAX_FILE_NAME_LENGTH,
                        javacall_fileconnection_get_private_dir, 1);
}

/**
 * Returns localized names for file system roots, corresponding to the roots list returned by
 * <code>FileSystemRegistry.listRoots()</code>.
 *
 * This method is called when the <code>fileconn.dir.roots.names</code> system
 * property is retrieved.
 *
 * @return the localized names for all roots, separated by ';' character
 */
char* getLocalizedMountedRoots() {
    return get_property(&localizedRoots, JAVACALL_MAX_LOCALIZED_ROOTS_LIST_LENGTH,
                        javacall_fileconnection_get_localized_mounted_roots, 0);
}

/**
 * Returns localized photos directory name corresponding to the directory returned by
 * <code>System.getProperty("fileconn.dir.photos")</code>.
 *
 * This method is called when the <code>fileconn.dir.photos.name</code> system
 * property is retrieved.
 *
 * @return the localized name for photos/images storage directory
 */
char* getLocalizedPhotosDir() {
    return get_property(&localizedPhotos, JAVACALL_MAX_LOCALIZED_DIR_NAME_LENGTH,
                        javacall_fileconnection_get_localized_photos_dir, 0);
}

/**
 * Returns localized videos directory name corresponding to the directory returned by
 * <code>System.getProperty("fileconn.dir.videos")</code>.
 *
 * This method is called when the <code>fileconn.dir.videos.name</code> system
 * property is retrieved.
 *
 * @return the localized name for videos storage directory
 */
char* getLocalizedVideosDir() {
    return get_property(&localizedVideos, JAVACALL_MAX_LOCALIZED_DIR_NAME_LENGTH,
                        javacall_fileconnection_get_localized_videos_dir, 0);
}

/**
 * Returns localized clip art graphics directory name corresponding to the directory returned by
 * <code>System.getProperty("fileconn.dir.graphics")</code>.
 *
 * This method is called when the <code>fileconn.dir.graphics.name</code> system
 * property is retrieved.
 *
 * @return the localized name for clip art graphics storage directory
 */
char* getLocalizedGraphicsDir() {
    return get_property(&localizedGraphics, JAVACALL_MAX_LOCALIZED_DIR_NAME_LENGTH,
                        javacall_fileconnection_get_localized_graphics_dir, 0);
}

/**
 * Returns localized ring tones directory name corresponding to the directory returned by
 * <code>System.getProperty("fileconn.dir.tones")</code>.
 *
 * This method is called when the <code>fileconn.dir.tones.name</code> system
 * property is retrieved.
 *
 * @return the localized name for ring tones storage directory
 */
char* getLocalizedTonesDir() {
    return get_property(&localizedTones, JAVACALL_MAX_LOCALIZED_DIR_NAME_LENGTH,
                        javacall_fileconnection_get_localized_tones_dir, 0);
}

/**
 * Returns localized music directory name corresponding to the directory returned by
 * <code>System.getProperty("fileconn.dir.music")</code>.
 *
 * This method is called when the <code>fileconn.dir.music.name</code> system
 * property is retrieved.
 *
 * @return the localized name for music storage directory
 */
char* getLocalizedMusicDir() {
    return get_property(&localizedMusic, JAVACALL_MAX_LOCALIZED_DIR_NAME_LENGTH,
                        javacall_fileconnection_get_localized_music_dir, 0);
}

/**
 * Returns localized voice recordings directory name corresponding to the directory returned by
 * <code>System.getProperty("fileconn.dir.recordings")</code>.
 *
 * This method is called when the <code>fileconn.dir.recordings.name</code> system
 * property is retrieved.
 *
 * @return the localized name for voice recordings storage directory
 */
char* getLocalizedRecordingsDir() {
    return get_property(&localizedRecordings, JAVACALL_MAX_LOCALIZED_DIR_NAME_LENGTH,
                        javacall_fileconnection_get_localized_recordings_dir, 0);
}

/**
 * Returns localized private directory name corresponding to the directory returned by
 * <code>System.getProperty("fileconn.dir.private")</code>.
 *
 * This method is called when the <code>fileconn.dir.private.name</code> system
 * property is retrieved.
 *
 * @return the localized name for the private directory for all MIDlet suites
 */
char* getLocalizedPrivateDir() {
    return get_property(&localizedPrivate, JAVACALL_MAX_LOCALIZED_DIR_NAME_LENGTH,
                        javacall_fileconnection_get_localized_private_dir, 0);
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
 * @param flags one of the open file flags (JAVACALL_FILE_O_RDONLY,
 *              (JAVACALL_FILE_O_WRONLY or (JAVACALL_FILE_O_RDWR) 
 *              defined in javacall_file.h
 */
static void closeHandles(jobject objectHandle, int flags)
{
    // IMPL_NOTE: javacall file flags cannot be used as a mask due their values.
    if ((JAVACALL_FILE_O_RDONLY == flags) || (JAVACALL_FILE_O_RDWR == flags))
    {
        javacall_handle readHandle  = (javacall_handle)KNI_GetIntField(objectHandle, readHandleID);
        if (NULL != readHandle)
        {
            if (JAVACALL_OK != javacall_file_close(readHandle))
            {
                KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcCloseFileFailed));
            }
            /* anyway reset the read handle */
            KNI_SetIntField(objectHandle, readHandleID, (jint)NULL);
        }
    }

    // IMPL_NOTE: javacall file flags cannot be used as a mask due their values.
    if ((JAVACALL_FILE_O_WRONLY == flags) || (JAVACALL_FILE_O_RDWR == flags))
    {
        javacall_handle writeHandle = (javacall_handle)KNI_GetIntField(objectHandle, writeHandleID);
        if (NULL != writeHandle)
        {
            if (JAVACALL_OK != javacall_file_close(writeHandle))
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
    const pcsl_string * fileName = NULL;
    javacall_handle     handle   = NULL;
    const int           flags    = JAVACALL_FILE_O_CREAT | JAVACALL_FILE_O_RDWR;

    DEBUG_PRINT("File.create() >>");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);

    KNI_GetThisPointer(objectHandle);

    fileName = (const pcsl_string *)(long)KNI_GetLongField(objectHandle, fileNameID);

    printPcslString(fileName);
	
    if (NULL == fileName)
    {
        KNI_ThrowNew(midpNullPointerException, EXCEPTION_MSG(fcFileNameIsNull));
    }
    else
    {   
        /* Note: 'file is not directory' is checked in J2ME code */
        GET_PCSL_STRING_DATA_AND_LENGTH(fileName)
        if (PCSL_STRING_PARAMETER_ERROR(fileName)) {
            KNI_ThrowNew(midpOutOfMemoryError, NULL);
        } else {
            if (JAVACALL_OK == javacall_file_exist(fileName_data, fileName_len))
            {
                KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcFileExists));
            }
            else
            {
                if (JAVACALL_OK != javacall_file_open(fileName_data, fileName_len, flags, &handle))
                {
                    KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcCreateFileFailed));
                }
                else
                {
                    if (JAVACALL_OK != javacall_file_close(handle))
                    {
                        KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcCloseFileFailed));
                    }
                }
            }
        }
        RELEASE_PCSL_STRING_DATA_AND_LENGTH
    }
    DEBUG_PRINT("File.create() <<\n");    
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

    DEBUG_PRINT("File.delete() >>  ");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);

    KNI_GetThisPointer(objectHandle);

    fileName = (const pcsl_string *)(long)KNI_GetLongField(objectHandle, fileNameID);

    printPcslString(fileName);

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
            if (JAVACALL_OK == javacall_file_exist(fileName_data, fileName_len))
            {            
                if (JAVACALL_OK != javacall_file_delete(fileName_data, fileName_len))
                {
                    KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcDeleteFileFailed));
                }
            }
            else
            {
                if (JAVACALL_OK == javacall_fileconnection_dir_exists(fileName_data, fileName_len))
                {            
                    if (JAVACALL_OK != javacall_fileconnection_delete_dir(fileName_data, fileName_len))
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
        RELEASE_PCSL_STRING_DATA_AND_LENGTH
    }
    DEBUG_PRINT("File.delete() <<\n");
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
    pcsl_string *   fileName      = NULL;
    pcsl_string *   newFileName   = NULL;
    int             convertStatus = -1;
    javacall_result existStatus   = JAVACALL_FAIL;
    javacall_result renameStatus  = JAVACALL_FAIL;

    DEBUG_PRINT("File.rename() >> old file name: ");

    KNI_StartHandles(2);
    KNI_DeclareHandle(objectHandle);
    KNI_DeclareHandle(newNameHandle);
    
    KNI_GetThisPointer(objectHandle);

    KNI_GetParameterAsObject(1, newNameHandle);

    fileName = (pcsl_string *)(long)KNI_GetLongField(objectHandle, fileNameID);

    printPcslString(fileName);

    if (NULL == fileName)
    {
        KNI_ThrowNew(midpNullPointerException, EXCEPTION_MSG(fcFileNameIsNull));
    }
    else
    {
        /* Note: 'newFileName is not null' is checked in J2ME code */
                
        convertStatus = get_pcsl_string(newNameHandle, &newFileName);

        DEBUG_PRINT("File.rename() >> new file name: ");
        printPcslString(newFileName);

        if (0 == convertStatus)
        {
            GET_PCSL_STRING_DATA_AND_LENGTH(fileName)
            GET_PCSL_STRING_DATA_AND_LENGTH(newFileName)
            if (PCSL_STRING_PARAMETER_ERROR(fileName)
             || PCSL_STRING_PARAMETER_ERROR(newFileName)) {
                KNI_ThrowNew(midpOutOfMemoryError, NULL);
            } else {
                /* 
                   We need to check whether file/directory named 'newFileName' 
                   exists because rename implementation can override it without prompting.
                   Existence of 'fileName' file/directory is checked by rename function
                */

                existStatus = javacall_file_exist(newFileName_data, newFileName_len);
                if (JAVACALL_OK != existStatus)
                {
                    existStatus = javacall_fileconnection_dir_exists(newFileName_data, newFileName_len);
                }

                if (JAVACALL_OK == existStatus)
                {
                    pcsl_string_free(newFileName);
                    pcsl_mem_free(newFileName);
                    KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcFileExists));
                }
                else
                {
                    if (JAVACALL_OK == javacall_fileconnection_dir_exists(fileName_data, fileName_len))
                    {
                        renameStatus = javacall_fileconnection_rename_dir(fileName_data, fileName_len,
                                newFileName_data, newFileName_len);
                    }
                    else
                    {
                        renameStatus = javacall_file_rename(fileName_data, fileName_len,
                                newFileName_data, newFileName_len);
                    }

                    if (JAVACALL_OK != renameStatus)
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
            RELEASE_PCSL_STRING_DATA_AND_LENGTH
            RELEASE_PCSL_STRING_DATA_AND_LENGTH
        }
    }
    DEBUG_PRINT("File.rename() <<\n");
    KNI_EndHandles();
    KNI_ReturnVoid();
}

/*
 * public native void truncate(long byteOffset) throws IOException;
 *
 * Note: byteOffset is geater than zero is checked in J2ME code.
 * Open write stream is also flushed in J2ME code.
 */
KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_truncate()
{
    const pcsl_string * fileName    = NULL;
    javacall_result     truncStatus = JAVACALL_OK;
    javacall_handle     handle      = NULL;
    jlong               len         = 0;
    jlong               origlen     = 0;
    const int           flags       = JAVACALL_FILE_O_WRONLY;

    
    DEBUG_PRINT("File.truncate() >>\n");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);
  
    KNI_GetThisPointer(objectHandle);
    
    len      = KNI_GetParameterAsLong(1);
    fileName = (const pcsl_string *)(long)KNI_GetLongField(objectHandle, fileNameID);

    printPcslString(fileName);

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
            /* Check the file is not directory and exists */
            if (JAVACALL_OK == javacall_file_exist(fileName_data, fileName_len))
            {
                if (JAVACALL_OK == javacall_file_open(fileName_data, fileName_len,
                                            flags, &handle))
                {
                    origlen = javacall_file_sizeofopenfile(handle);
                    if (len < origlen) {
                        truncStatus = javacall_file_truncate(handle, len);
                    }

                    if (JAVACALL_OK != javacall_file_close(handle)) {
                        KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcCloseFileFailed));
                    } else {
                        if (JAVACALL_OK != truncStatus) {
                            KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcTruncFileFailed));
                        }
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
        RELEASE_PCSL_STRING_DATA_AND_LENGTH
    }
    
    DEBUG_PRINT("File.truncate() <<\n");

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
    jlong res                    = (jlong)(-1);

    
    DEBUG_PRINT("File.fileSize() >>\n");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);

    KNI_GetThisPointer(objectHandle);

    fileName = (const pcsl_string *)(long)KNI_GetLongField(objectHandle, fileNameID);

    printPcslString(fileName);

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
            res = javacall_file_sizeof(fileName_data, fileName_len);
        }
        RELEASE_PCSL_STRING_DATA_AND_LENGTH
    }

    DEBUG_PRINT("File.fileSize() <<\n");

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
    javacall_bool       recurse  = JAVACALL_FALSE;
    jlong               res      = (jlong)(-1);

    DEBUG_PRINT("File.directorySize() >>\n");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);

    KNI_GetThisPointer(objectHandle);

    recurse = (KNI_GetParameterAsBoolean(1) == KNI_TRUE) ? JAVACALL_TRUE : JAVACALL_FALSE;
    fileName = (const pcsl_string *)(long)KNI_GetLongField(objectHandle, fileNameID);

    printPcslString(fileName);

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
            if (JAVACALL_OK != javacall_fileconnection_dir_content_size(fileName_data, fileName_len, recurse, &res))
            {
                res = (jlong)(-1);
            }
        }
        RELEASE_PCSL_STRING_DATA_AND_LENGTH
    }
    
    DEBUG_PRINT("File.directorySize() <<\n");

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


    DEBUG_PRINT("File.mkdir() >> will make dir ");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);
    
    KNI_GetThisPointer(objectHandle);

    fileName = (const pcsl_string *)(long)KNI_GetLongField(objectHandle, fileNameID);

    printPcslString(fileName);
	
    if (NULL == fileName)
    {
        KNI_ThrowNew(midpNullPointerException, EXCEPTION_MSG(fcDirNameIsNull));
    }
    else
    {    
        GET_PCSL_STRING_DATA_AND_LENGTH(fileName)
        if (PCSL_STRING_PARAMETER_ERROR(fileName)) {
            KNI_ThrowNew(midpOutOfMemoryError, NULL);
        } else {
            /* 
               Note: invokation on an existing directory or on any file is
               not checked because it's detected by the 'javacall_file_mkdir'
             */
            if (JAVACALL_OK != javacall_fileconnection_create_dir(fileName_data, fileName_len))
            {
                KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcCreateDirFailed));
            }
        }
        RELEASE_PCSL_STRING_DATA_AND_LENGTH
    }

    DEBUG_PRINT("File.mkdir() <<\n");

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
    javacall_result     exist    = JAVACALL_FAIL;
    jboolean            res      = KNI_FALSE;

    
    DEBUG_PRINT("File.exists() >>\n");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);
    
    KNI_GetThisPointer(objectHandle);

    fileName = (const pcsl_string *)(long)KNI_GetLongField(objectHandle, fileNameID);

    printPcslString(fileName);

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
            exist = javacall_file_exist(fileName_data, fileName_len);
            if (JAVACALL_OK != exist)
            {
                exist = javacall_fileconnection_dir_exists(fileName_data, fileName_len);
            }

            res = (JAVACALL_OK == exist) ? KNI_TRUE : KNI_FALSE;
        }
        RELEASE_PCSL_STRING_DATA_AND_LENGTH
    }

    DEBUG_PRINT("File.exists() <<\n");

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
    jboolean            res      = KNI_FALSE;

    DEBUG_PRINT("File.isDirectory() >>\n");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);
    
    KNI_GetThisPointer(objectHandle);

    fileName = (const pcsl_string *)(long)KNI_GetLongField(objectHandle, fileNameID);

    printPcslString(fileName);

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
            res = (JAVACALL_OK == javacall_fileconnection_dir_exists(fileName_data, fileName_len)) ? KNI_TRUE : KNI_FALSE;
        }
        RELEASE_PCSL_STRING_DATA_AND_LENGTH
    }
    DEBUG_PRINT("File.isDirectory() <<\n");
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
    javacall_bool       value    = JAVACALL_FALSE;
    javacall_result     status   = JAVACALL_FAIL;
    jboolean            res      = KNI_FALSE;


    DEBUG_PRINT("File.canRead() >>\n");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);

    KNI_GetThisPointer(objectHandle);

    fileName = (const pcsl_string *)(long)KNI_GetLongField(objectHandle, fileNameID);

    printPcslString(fileName);

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
            status = javacall_fileconnection_is_readable(fileName_data, fileName_len, &value);
            res = (JAVACALL_OK == status && JAVACALL_TRUE == value) ? KNI_TRUE : KNI_FALSE;
        }
        RELEASE_PCSL_STRING_DATA_AND_LENGTH
    }

    DEBUG_PRINT("File.canRead() <<\n");

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
    javacall_bool       newvalue = JAVACALL_FALSE;
    javacall_result     status   = JAVACALL_FAIL;

    DEBUG_PRINT("File.setReadable() >>\n");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);

    KNI_GetThisPointer(objectHandle);

    newvalue = (KNI_GetParameterAsBoolean(1) == KNI_TRUE) ? JAVACALL_TRUE : JAVACALL_FALSE;
    fileName = (const pcsl_string *)(long)KNI_GetLongField(objectHandle, fileNameID);

    printPcslString(fileName);

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
            status = javacall_fileconnection_set_readable(fileName_data, fileName_len, newvalue);
            if (JAVACALL_OK != status)
            {
                KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcSetAttrFailed));
            }
        }
        RELEASE_PCSL_STRING_DATA_AND_LENGTH
    }

    DEBUG_PRINT("File.setReadable() <<\n");
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
    javacall_bool       value    = JAVACALL_FALSE;
    javacall_result     status   = JAVACALL_FAIL;
    jboolean            res      = KNI_FALSE;

    DEBUG_PRINT("File.canWrite() >>\n");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);

    KNI_GetThisPointer(objectHandle);

    fileName = (const pcsl_string *)(long)KNI_GetLongField(objectHandle, fileNameID);

    printPcslString(fileName);

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
            status = javacall_fileconnection_is_writable(fileName_data, fileName_len, &value);
            res = (JAVACALL_OK == status && JAVACALL_TRUE == value) ? KNI_TRUE : KNI_FALSE;
        }
        RELEASE_PCSL_STRING_DATA_AND_LENGTH
    }

    DEBUG_PRINT("File.canWrite() <<\n");
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
    javacall_bool       newvalue = JAVACALL_FALSE;
    javacall_result     status   = JAVACALL_FAIL;


    DEBUG_PRINT("File.setWrite() >>\n");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);

    KNI_GetThisPointer(objectHandle);

    newvalue = (KNI_GetParameterAsBoolean(1) == KNI_TRUE) ? JAVACALL_TRUE : JAVACALL_FALSE;
    fileName = (const pcsl_string *)(long)KNI_GetLongField(objectHandle, fileNameID);

    printPcslString(fileName);

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
            status = javacall_fileconnection_set_writable(fileName_data, fileName_len, newvalue);
            if (JAVACALL_OK != status)
            {
                KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcSetAttrFailed));
            }
        }
        RELEASE_PCSL_STRING_DATA_AND_LENGTH
    }

    DEBUG_PRINT("File.setWrite() <<\n");

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
    jboolean            res      = KNI_FALSE;
    javacall_bool       value    = JAVACALL_FALSE;
    javacall_result     status   = JAVACALL_FAIL;

    DEBUG_PRINT("File.isHidden() >>\n");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);
    
    KNI_GetThisPointer(objectHandle);

    fileName = (const pcsl_string *)(long)KNI_GetLongField(objectHandle, fileNameID);

    printPcslString(fileName);

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
            status = javacall_fileconnection_is_hidden(fileName_data, fileName_len, &value);
            res = (JAVACALL_OK == status && JAVACALL_TRUE == value) ? KNI_TRUE : KNI_FALSE;
        }
        RELEASE_PCSL_STRING_DATA_AND_LENGTH
    }

    DEBUG_PRINT("File.isHidden() <<\n");

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
    javacall_bool       newvalue = JAVACALL_FALSE;
    javacall_result     status   = JAVACALL_FAIL;


    DEBUG_PRINT("File.setHidden() >>\n");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);

    KNI_GetThisPointer(objectHandle);

    newvalue = (KNI_GetParameterAsBoolean(1) == KNI_TRUE) ? JAVACALL_TRUE : JAVACALL_FALSE;
    fileName = (const pcsl_string *)(long)KNI_GetLongField(objectHandle, fileNameID);

    printPcslString(fileName);

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
            status = javacall_fileconnection_set_hidden(fileName_data, fileName_len, newvalue);
            if (JAVACALL_OK != status)
            {
                KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcSetAttrFailed));
            }
        }
        RELEASE_PCSL_STRING_DATA_AND_LENGTH
    }

    DEBUG_PRINT("File.setHidden() <<\n");

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
    javacall_handle     fileList = NULL;
    jlong               res      = 0;    

    
    DEBUG_PRINT("File.openDir() >>\n");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);
    
    KNI_GetThisPointer(objectHandle);

    fileName = (const pcsl_string *)(long)KNI_GetLongField(objectHandle, fileNameID);

    printPcslString(fileName);

    if (NULL == fileName)
    {        
        KNI_ThrowNew(midpNullPointerException, EXCEPTION_MSG(fcDirNameIsNull));
    }
    else
    {
        GET_PCSL_STRING_DATA_AND_LENGTH(fileName)
        if (PCSL_STRING_PARAMETER_ERROR(fileName)) {
            KNI_ThrowNew(midpOutOfMemoryError, NULL);
        } else {
            fileList = javacall_dir_open(fileName_data, fileName_len);
            if (NULL == fileList)
            {
                KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcDirOpenFailed));
            }
            else
            {
                res = (jlong)(long)fileList;
            }
        }
        RELEASE_PCSL_STRING_DATA_AND_LENGTH
    }

    DEBUG_PRINT("File.openDir() <<\n");
    KNI_EndHandles();
    KNI_ReturnLong(res);
}

/*
 * private native void closeDir(long dirHandle);
 */
KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_closeDir()
{    
    javacall_handle fileList = NULL;
//    int   status   = -1;

    DEBUG_PRINT("File.closeDir() >>\n");
   
    
    fileList = (javacall_handle)(long)KNI_GetParameterAsLong(1);
    
    javacall_dir_close(fileList); // doesn't have any failure indicator

    DEBUG_PRINT("File.closeDir() <<\n");
    
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
    const pcsl_string * fileName = NULL;
    const jchar sep = javacall_get_file_separator();
    jchar *         found_data;
    int             found_len;
    pcsl_string     longName      = PCSL_STRING_NULL;
    pcsl_string     resultName    = PCSL_STRING_NULL;
    pcsl_string *   pLongName     = &longName;
    javacall_handle fileList      = NULL;
    javacall_bool   includeHidden = JAVACALL_FALSE;
    javacall_bool   hidden        = JAVACALL_FALSE;
    int             finish        = 0;

    DEBUG_PRINT("File.dirGetNextFile() >>\n");

    KNI_StartHandles(2);
    KNI_DeclareHandle(fnameHandle);        
    KNI_DeclareHandle(objectHandle);
    
    KNI_GetThisPointer(objectHandle);

    fileList = (javacall_handle)(long)KNI_GetParameterAsLong(1);
    /* 
     * The order number of the second paramter is 3 not 2
     * because it's located after a parameter of type long
     * (parameters of type long or double take up two entries
     * on the operand stack).
     */
    includeHidden = (KNI_GetParameterAsBoolean(3) == KNI_TRUE) ? JAVACALL_TRUE : JAVACALL_FALSE;

    fileName = (const pcsl_string *)(long)KNI_GetLongField(objectHandle, fileNameID);

    printPcslString(fileName);

    if (fileName == NULL)
    {        
        KNI_ThrowNew(midpNullPointerException, EXCEPTION_MSG(fcDirNameIsNull));
    }
    else
    {
        while (!finish)
        {   
            found_data = (jchar *)javacall_dir_get_next(fileList, &found_len);
            if (NULL != found_data)
            {
                /* add path prefix */
                pcsl_string_predict_size(pLongName, pcsl_string_length(fileName) + 1 + found_len);
                if (PCSL_STRING_OK != pcsl_string_append(pLongName, fileName)
                 || PCSL_STRING_OK != pcsl_string_append_char(pLongName, sep)
                 || PCSL_STRING_OK != pcsl_string_append_buf(pLongName, found_data, found_len)) {
                    KNI_ReleaseHandle(fnameHandle);
                    KNI_ThrowNew(midpOutOfMemoryError, NULL);
                } else {
                    GET_PCSL_STRING_DATA_AND_LENGTH(pLongName)
                    if (PCSL_STRING_PARAMETER_ERROR(pLongName)) {
                        KNI_ReleaseHandle(fnameHandle);
                        KNI_ThrowNew(midpOutOfMemoryError, NULL);
                    } else {
                        if (JAVACALL_OK == javacall_fileconnection_is_hidden(pLongName_data, pLongName_len, &hidden))
                        {
                            if (JAVACALL_FALSE == hidden || JAVACALL_TRUE == includeHidden)
                            {
                               #if 0
                                /* add trailing '/' if the file is a directory */
                                if (JAVACALL_OK == javacall_fileconnection_dir_exists(pLongName_data, pLongName_len))
                                {
                                    pcsl_string_predict_size(&resultName, found_len + 1);
                                    if (PCSL_STRING_OK != pcsl_string_append_buf(&resultName, found_data, found_len)
                                     || PCSL_STRING_OK != pcsl_string_append_char(&resultName, '/')) {
                                        KNI_ReleaseHandle(fnameHandle);
                                        KNI_ThrowNew(midpOutOfMemoryError, NULL);
                                    } else {
                                        if (PCSL_STRING_OK != midp_jstring_from_pcsl_string(&resultName, fnameHandle)) {
                                            KNI_ReleaseHandle(fnameHandle);
                                            KNI_ThrowNew(midpOutOfMemoryError, NULL);
                                        }
                                    }
                                    pcsl_string_free(&resultName);
                                }
                                else
                                #endif
                                {
                                    KNI_NewString(found_data, found_len, fnameHandle);
                                }
                                finish = 1;
                            }
                        }
                    }
                    RELEASE_PCSL_STRING_DATA_AND_LENGTH
                }
                pcsl_string_free(pLongName);
            }
            else
            {
                KNI_ReleaseHandle(fnameHandle);
                finish = 1;
            }
        }
    }

    DEBUG_PRINT("File.dirGetNextFile() <<\n");
    KNI_EndHandlesAndReturnObject(fnameHandle);
}

/*
 * private native String getMountedRoots();
 */
KNIEXPORT KNI_RETURNTYPE_OBJECT
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_getMountedRoots(void)
{
    jchar* roots = NULL;
    int len;

    DEBUG_PRINT("File.getMountedRoots() >>\n");

    KNI_StartHandles(1);
    KNI_DeclareHandle(returnValueHandle);

    roots = (jchar *)pcsl_mem_malloc(JAVACALL_MAX_ROOTS_LIST_LENGTH * sizeof(jchar));
    if (NULL == roots)
    {
        KNI_ReleaseHandle(returnValueHandle);
        KNI_ThrowNew(midpOutOfMemoryError, NULL);
    }
    else
    {
        if (JAVACALL_OK != javacall_fileconnection_get_mounted_roots(roots, JAVACALL_MAX_ROOTS_LIST_LENGTH))
        {
            KNI_ReleaseHandle(returnValueHandle);
        }
        else
        {
            for (len = 0; len <= JAVACALL_MAX_ROOTS_LIST_LENGTH && roots[len] != 0; len++);
            KNI_NewString(roots, len, returnValueHandle);
        }
        pcsl_mem_free(roots);
    }

    DEBUG_PRINT("File.getMountedRoots() <<\n");
    KNI_EndHandlesAndReturnObject(returnValueHandle);
}


/*
 * private native String getNativePathForRoot(String root);
 */
KNIEXPORT KNI_RETURNTYPE_OBJECT
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_getNativePathForRoot()
{    
    pcsl_string * mroot         = NULL;
    int           convertStatus = -1;
    jchar*        path          = NULL;
    int           len;

    DEBUG_PRINT("File.getNativePathForRoot() >>\n");

    KNI_StartHandles(2);
    KNI_DeclareHandle(rootHandle);
    KNI_DeclareHandle(returnValueHandle);

    KNI_GetParameterAsObject(1, rootHandle);

    convertStatus = get_pcsl_string(rootHandle, &mroot);

    printPcslString(mroot);

    if (0 == convertStatus)
    {
        path = (jchar *)pcsl_mem_malloc(JAVACALL_MAX_ROOT_PATH_LENGTH * sizeof(jchar));
        if (NULL == path)
        {
            KNI_ReleaseHandle(returnValueHandle);
            KNI_ThrowNew(midpOutOfMemoryError, NULL);
        }
        else
        {
            GET_PCSL_STRING_DATA_AND_LENGTH(mroot)
            if (PCSL_STRING_PARAMETER_ERROR(mroot)) {
                KNI_ReleaseHandle(returnValueHandle);
                KNI_ThrowNew(midpOutOfMemoryError, NULL);
            } else {
                if (JAVACALL_OK != javacall_fileconnection_get_path_for_root(mroot_data, mroot_len, path, JAVACALL_MAX_ROOT_PATH_LENGTH))
                {
                    KNI_ReleaseHandle(returnValueHandle);
                }
                else
                {
                    DEBUG_PRINT("Native path for root:[");
                    for (len = 0; len <= JAVACALL_MAX_ROOT_PATH_LENGTH && path[len] != 0; len++) {
                  //      DEBUG_PRINT1("%c", (char)path[len]);
                    }
                    DEBUG_PRINT("]\n");
                    KNI_NewString(path, len, returnValueHandle);
                }
            }
            RELEASE_PCSL_STRING_DATA_AND_LENGTH
            pcsl_mem_free(path);
        }
        pcsl_string_free(mroot);
        pcsl_mem_free(mroot);
    }    

    DEBUG_PRINT("File.getNativePathForRoot() <<\n");
    KNI_EndHandlesAndReturnObject(returnValueHandle);
}


/*
 * public native long lastModified();
 */
KNIEXPORT KNI_RETURNTYPE_LONG
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_lastModified()
{
    const pcsl_string * fileName = NULL;
    jlong               res      = 0;

    DEBUG_PRINT("File.lastModified() >>\n");


    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);

    KNI_GetThisPointer(objectHandle);

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
            if (JAVACALL_OK == javacall_fileconnection_get_last_modified(fileName_data, fileName_len, &res))
            {
                /* convert the time from seconds to milliseconds */
                res *= 1000;
            }
            else
            {
                res = 0;
            }
        }
        RELEASE_PCSL_STRING_DATA_AND_LENGTH
    }

    DEBUG_PRINT("File.lastModified() <<\n");

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
    javacall_handle     handle     = NULL;
    const int           flags      = JAVACALL_FILE_O_RDONLY;

    DEBUG_PRINT("File.openForRead() >>\n");


    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);
    
    KNI_GetThisPointer(objectHandle);
    
    fileName = (const pcsl_string *)(long)KNI_GetLongField(objectHandle, fileNameID);
    handle   = (javacall_handle*)KNI_GetIntField(objectHandle, readHandleID);

    printPcslString(fileName);

    if (NULL == fileName)
    {
        KNI_ThrowNew(midpNullPointerException, EXCEPTION_MSG(fcFileNameIsNull));
    }
    else
    {
        if (NULL == handle)
        {
            GET_PCSL_STRING_DATA_AND_LENGTH(fileName)
            if (PCSL_STRING_PARAMETER_ERROR(fileName)) {
                KNI_ThrowNew(midpOutOfMemoryError, NULL);
            } else {
                if(JAVACALL_OK == javacall_fileconnection_dir_exists(fileName_data, fileName_len))
                {
                    KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcFileIsDirectory));
                }
                else
                {
                    if (JAVACALL_OK != javacall_file_open(fileName_data, fileName_len,
                            flags, &handle))
                    {
                        KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcOpenFileFailed));
                    }
                    else
                    {
                        KNI_SetIntField(objectHandle, readHandleID, (jint)handle);
                    }
                }
            }
            RELEASE_PCSL_STRING_DATA_AND_LENGTH
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
    DEBUG_PRINT("File.openForRead() <<\n");

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
    javacall_handle     handle     = NULL;
    const int           flags      = JAVACALL_FILE_O_WRONLY;

    DEBUG_PRINT("File.openForWrite() >>\n");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);
    
    KNI_GetThisPointer(objectHandle);

    handle   = (javacall_handle)KNI_GetIntField(objectHandle, writeHandleID);
    fileName = (const pcsl_string *)(long)KNI_GetLongField(objectHandle, fileNameID);

    printPcslString(fileName);

    if (NULL == fileName)
    {
        KNI_ThrowNew(midpNullPointerException, EXCEPTION_MSG(fcFileNameIsNull));
    }
    else
    {
        if (NULL == handle)
        {
            GET_PCSL_STRING_DATA_AND_LENGTH(fileName)
            if (PCSL_STRING_PARAMETER_ERROR(fileName)) {
                KNI_ThrowNew(midpOutOfMemoryError, NULL);
            } else {
                if(JAVACALL_OK == javacall_fileconnection_dir_exists(fileName_data, fileName_len))
                {
                    KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcFileIsDirectory));
                }
                else
                {
                    if (JAVACALL_OK != javacall_file_open(fileName_data, fileName_len,
                            flags, &handle))
                    {
                        KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcOpenFileFailed));
                    }
                    else
                    {
                        KNI_SetIntField(objectHandle, writeHandleID, (jint)handle);
                    }
                }
            }
            RELEASE_PCSL_STRING_DATA_AND_LENGTH
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

    DEBUG_PRINT("File.openForWrite() <<\n");

    KNI_EndHandles();
    KNI_ReturnVoid();
}

/*
 * public native int read(byte b[], int off, int len) throws IOException;
 */
KNIEXPORT KNI_RETURNTYPE_INT
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_read()
{
    javacall_handle handle = NULL;
    jint            res    = 0;
    jbyte*          data   = NULL;
    jint offset, length;

    DEBUG_PRINT("read");

    offset = KNI_GetParameterAsInt(2);
    length = KNI_GetParameterAsInt(3);

    KNI_StartHandles(2);
    KNI_DeclareHandle(objectHandle);
    KNI_DeclareHandle(byteArrHandle);

    KNI_GetThisPointer(objectHandle);

    handle = (javacall_handle)KNI_GetIntField(objectHandle, readHandleID);

    if (NULL != handle)
    {
        data = (jbyte*)pcsl_mem_malloc(length * sizeof(jbyte));

        if (NULL == data)
        {
            KNI_ThrowNew(midpOutOfMemoryError, NULL);
        }
        else
        {            
            res = javacall_file_read(handle, (unsigned char *)data, length);
            if (res < 0)
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
            pcsl_mem_free(data);
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
    javacall_handle handle = NULL;
    jint            res    = 0;
    jbyte*          data   = NULL;
    jint offset, length;    

    DEBUG_PRINT("write");

    offset = KNI_GetParameterAsInt(2);
    length = KNI_GetParameterAsInt(3);

    KNI_StartHandles(2);
    KNI_DeclareHandle(objectHandle);
    KNI_DeclareHandle(byteArrHandle);
    
    KNI_GetThisPointer(objectHandle);

    handle = (javacall_handle)KNI_GetIntField(objectHandle, writeHandleID);

    if (NULL != handle)
    {
        data = (jbyte*)pcsl_mem_malloc(length * sizeof(jbyte));

        if (NULL == data)
        {
			DEBUG_PRINT("File.write() << Error midpOutOfMemoryError\n");
            KNI_ThrowNew(midpOutOfMemoryError, NULL);
        }
        else
        {
            /* Get write data. Bounds check was performed at J2ME level */
            KNI_GetParameterAsObject(1, byteArrHandle);
            KNI_GetRawArrayRegion(byteArrHandle, offset * sizeof(jbyte),
                                  length * sizeof(jbyte), data);

            res = javacall_file_write(handle, (unsigned char *)data, length);
            if (res < 0)
            {
                KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcFileWriteFailed));
            }
            
            pcsl_mem_free(data);
        }
    }
    else
    {
        DEBUG_PRINT("File.write() << Error midpIOException\n");
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
    javacall_handle handle = NULL;
    jlong           offset = 0;
    jlong           seek   = 0;
    jlong           size   = 0;


    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);

    KNI_GetThisPointer(objectHandle);

    offset = KNI_GetParameterAsLong(1);
    handle = (javacall_handle)KNI_GetIntField(objectHandle, writeHandleID);

    if (NULL != handle)
    {
        size = javacall_file_sizeofopenfile(handle);
        if ((jlong)(-1) == size)
        {
            DEBUG_PRINT("File.positionForWrite() << Error midpIOException\n");
            KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcFileSizeFailed));
        }
        else
        {
            if (size < offset)
            {
                offset = size;
            }
            seek = javacall_file_seek(handle, offset, JAVACALL_FILE_SEEK_SET);
            if (seek != offset)
            {
                KNI_ThrowNew(midpIOException, EXCEPTION_MSG(fcSeekFailed));
            }
        }
    }
    else
    {
        DEBUG_PRINT("File.positionForWrite() << Error fcFileClosed\n");
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
//    int             status = -1;
    javacall_handle handle = NULL;

    DEBUG_PRINT("flush");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);
    
    KNI_GetThisPointer(objectHandle);

    handle = (javacall_handle)KNI_GetIntField(objectHandle, writeHandleID);

    if (NULL != handle)
    {
        if (JAVACALL_OK != javacall_file_flush(handle))
        {
            DEBUG_PRINT("File.flush() << Error fcCommitFileFailed\n");
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
    jlong               res      = (jlong)(-1); /* -1 if the file system is not accessible */

    DEBUG_PRINT("File.availableSize() >>\n");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);
    
    KNI_GetThisPointer(objectHandle);

    rootName = (const pcsl_string *)(long)KNI_GetLongField(objectHandle, rootDirID);

    printPcslString(rootName);

    if (NULL == rootName)
    {
        KNI_ThrowNew(midpNullPointerException, EXCEPTION_MSG(fcFileNameIsNull));
    }
    else
    {
        GET_PCSL_STRING_DATA_AND_LENGTH(rootName)
        if (PCSL_STRING_PARAMETER_ERROR(rootName)) {
            KNI_ThrowNew(midpOutOfMemoryError, NULL);
        } else {
            if (JAVACALL_OK != javacall_fileconnection_get_free_size(rootName_data, rootName_len, &res))
            {
                res = (jlong)(-1);
            }
        }
        RELEASE_PCSL_STRING_DATA_AND_LENGTH
    }
    DEBUG_PRINT("File.availableSize() <<\n");

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
    jlong               res      = (jlong)(-1); /* -1 if the file system is not accessible */

    DEBUG_PRINT("File.totalSize() >>\n");


    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);
    
    KNI_GetThisPointer(objectHandle);

    rootName = (const pcsl_string *)(long)KNI_GetLongField(objectHandle, rootDirID);

    printPcslString(rootName);

    if (NULL == rootName)
    {
        KNI_ThrowNew(midpNullPointerException, EXCEPTION_MSG(fcFileNameIsNull));
    }
    else
    {
        GET_PCSL_STRING_DATA_AND_LENGTH(rootName)
        if (PCSL_STRING_PARAMETER_ERROR(rootName)) {
            KNI_ThrowNew(midpOutOfMemoryError, NULL);
        } else {
            if (JAVACALL_OK != javacall_fileconnection_get_total_size(rootName_data, rootName_len, &res))
            {
                res = (jlong)(-1);
            }
        }
        RELEASE_PCSL_STRING_DATA_AND_LENGTH
    }

    DEBUG_PRINT("File.totalSize() <<\n");
    KNI_EndHandles();
    KNI_ReturnLong(res);
}

/*
 * public native void closeForRead() throws IOException;
 */
KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_closeForRead()
{
    DEBUG_PRINT("File.closeForRead() >>\n");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);
    
    KNI_GetThisPointer(objectHandle);
    
    closeHandles(objectHandle, JAVACALL_FILE_O_RDONLY);

    DEBUG_PRINT("File.closeForRead() <<\n");

    KNI_EndHandles();
    KNI_ReturnVoid();
}

/*
 * public native void closeForWrite() throws IOException;
 */
KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_closeForWrite()
{
    DEBUG_PRINT("File.closeForWrite() >>\n");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);
    
    KNI_GetThisPointer(objectHandle);

    closeHandles(objectHandle, JAVACALL_FILE_O_WRONLY);

    DEBUG_PRINT("File.closeForWrite() <<\n");
    KNI_EndHandles();
    KNI_ReturnVoid();
}

/*
 * public native void closeForReadWrite() throws IOException;
 */
KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_closeForReadWrite()
{
    DEBUG_PRINT("File.closeForReadWrite() >>\n");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);

    KNI_GetThisPointer(objectHandle);

    closeHandles(objectHandle, JAVACALL_FILE_O_RDWR);

    DEBUG_PRINT("File.closeForReadWrite() <<\n");
    KNI_EndHandles();
    KNI_ReturnVoid();
}

/*
 * public native void close() throws IOException;
 */
KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_close()
{
    DEBUG_PRINT("File.close() >>\n");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);
    
    KNI_GetThisPointer(objectHandle);

    closeHandles(objectHandle, JAVACALL_FILE_O_RDWR);
    freeNames(objectHandle);

    DEBUG_PRINT("File.close() <<\n");
    KNI_EndHandles();
    KNI_ReturnVoid();
}

/*
 * private static native long getNativeName(String name, long oldName);
 */
KNIEXPORT KNI_RETURNTYPE_LONG
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_getNativeName()
{
    jlong         lname         = 0;
    pcsl_string * mname         = NULL;
    pcsl_string * oldName       = (pcsl_string *)(long)KNI_GetParameterAsLong(2);
    int           convertStatus = -1;

    DEBUG_PRINT("File.getNativeName() >>\n");

    KNI_StartHandles(1);
    KNI_DeclareHandle(nameHandle);
    KNI_GetParameterAsObject(1, nameHandle);

    /* Free previous midp string */
    if (oldName != NULL) {
        pcsl_string_free(oldName);
        pcsl_mem_free(oldName);
    }
    
    convertStatus = get_pcsl_string(nameHandle, &mname);
    printPcslString(mname);

    if (convertStatus == 0)
    {
        lname = (jlong)(long)mname;
    }
    
    DEBUG_PRINT("File.getNativeName() <<\n");
    KNI_EndHandles();
    KNI_ReturnLong(lname);
}

KNIEXPORT KNI_RETURNTYPE_OBJECT
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_illegalFileNameChars0()
{
    jchar* chars = NULL;
    int len;

	DEBUG_PRINT("File.illegalFileNameChars() >>\n");

    
    KNI_StartHandles(1);
    KNI_DeclareHandle(illegalChars);
    
    chars = (jchar *)pcsl_mem_malloc(JAVACALL_MAX_ILLEGAL_FILE_NAME_CHARS * sizeof(jchar));
    if (NULL == chars)
    {
        KNI_ReleaseHandle(illegalChars);
        KNI_ThrowNew(midpOutOfMemoryError, NULL);
    }
    else 
    {
        if (JAVACALL_OK != javacall_fileconnection_get_illegal_filename_chars(chars, JAVACALL_MAX_ILLEGAL_FILE_NAME_CHARS)) {
            KNI_ReleaseHandle(illegalChars);
        } else {
            for (len = 0; len <= JAVACALL_MAX_ILLEGAL_FILE_NAME_CHARS && chars[len] != 0; len++);
            KNI_NewString(chars, len, illegalChars);
        }
        pcsl_mem_free(chars);
    }

    DEBUG_PRINT("File.illegalFileNameChars() <<\n");
    KNI_EndHandlesAndReturnObject(illegalChars);
}

/*
 * private static native char getFileSeparator();
 */
KNIEXPORT KNI_RETURNTYPE_CHAR
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_getFileSeparator()
{    
    DEBUG_PRINT("File.getFileSeparator()\n");
   
    KNI_ReturnChar((jchar)javacall_get_file_separator());
}

/*
 * private native void finalize();
 */
KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_midp_io_j2me_file_DefaultFileHandler_finalize()
{
    DEBUG_PRINT("File.finalize() >>\n");

    KNI_StartHandles(1);
    KNI_DeclareHandle(objectHandle);

    KNI_GetThisPointer(objectHandle);

    closeHandles(objectHandle, JAVACALL_FILE_O_RDWR);
    freeNames(objectHandle);

    DEBUG_PRINT("File.finalize() <<\n");
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
    
    KNI_StartHandles(1);
    KNI_DeclareHandle(classHandle);
    KNI_GetClassPointer(classHandle);

    GET_FIELDID(fileNameID,    "fileName",    "J")
    GET_FIELDID(rootDirID,     "rootDir",     "J")
    GET_FIELDID(writeHandleID, "writeHandle", "I")
    GET_FIELDID(readHandleID,  "readHandle",  "I")

    /* initialize native mount/unmount events listener */
    javacall_fileconnection_init();

    KNI_EndHandles();
    KNI_ReturnVoid();
}
