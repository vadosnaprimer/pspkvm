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

/**
 * @file
 *
 * @ingroup AMS
 *
 * This is reference implementation of the common MIDlet suite storage
 * functions.
 */

#include <string.h>
#include <kni.h>
#include <pcsl_memory.h>
#include <midpInit.h>
#include <suitestore_common.h>

#include <suitestore_intern.h>
#include <suitestore_locks.h>

#if ENABLE_MONET
/**
 * Filename to save the application image file of suite.
 */
PCSL_DEFINE_ASCII_STRING_LITERAL_START(APP_IMAGE_EXTENSION)
#ifdef PRODUCT
    {'.', 'b', 'u', 'n', '\0'}
#elif defined(AZZERT)
    {'_', 'g', '.', 'b', 'u', 'n', '\0'}
#else
    {'_', 'r', '.', 'b', 'u', 'n', '\0'}
#endif
PCSL_DEFINE_ASCII_STRING_LITERAL_END(APP_IMAGE_EXTENSION);
#endif

/* Description of these internal functions can be found bellow in the code. */
static MIDPError suite_in_list(SuiteIdType suiteId);
static MIDPError get_string_list(char** ppszError, const pcsl_string* pFilename,
                                 pcsl_string** paList, int* pStringNum);
static MIDPError get_class_path_impl(SuiteIdType suiteId,
                                     jint storageId,
                                     pcsl_string* classPath,
                                     const pcsl_string* extension);

/**
 * Initializes the subsystem.
 */
MIDPError
midp_suite_storage_init() {
    return suite_storage_init_impl();
}

/**
 * Resets any persistent resources allocated by MIDlet suite storage functions.
 */
void
midp_suite_storage_cleanup() {
    suite_storage_cleanup_impl();
}

#define SUITESTORE_COUNTOF(x) (sizeof(x) / sizeof(x[0]))
/**
 * Converts the given suite ID to pcsl_string.
 * NOTE: this function returns a pointer to the static buffer!
 *
 * @param value suite id to convert
 *
 * @return pcsl_string representation of the given suite ID
 */
const pcsl_string* midp_suiteid2pcsl_string(SuiteIdType value) {
    int i;
    jchar digits[] = {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };
    unsigned long unsignedValue = (unsigned long) value;
    static jchar resChars[GET_SUITE_ID_LEN(value) + 1]; /* +1 for last zero */
    static pcsl_string resString = {
        resChars, SUITESTORE_COUNTOF(resChars), 0
    };

    for (i = (int)SUITESTORE_COUNTOF(resChars) - 2; i >= 0; i--) {
        resChars[i] = digits[unsignedValue & 15];
        unsignedValue >>= 4;
    }

    resChars[SUITESTORE_COUNTOF(resChars) - 1] = (jchar)0;
    return &resString;
}

/**
 * Converts the given suite ID to array of chars.
 * NOTE: this function returns a pointer to the static buffer!
 *
 * @param value suite id to convert
 *
 * @return char[] representation of the given suite ID
 * or NULL in case of error.
 */
const char* midp_suiteid2chars(SuiteIdType value) {
    jsize resLen;
    static jbyte resChars[GET_SUITE_ID_LEN(value) + 1]; /* +1 for last zero */
    const pcsl_string* pResString = midp_suiteid2pcsl_string(value);
    pcsl_string_status rc;

    rc = pcsl_string_convert_to_utf8(pResString, resChars,
        (jsize)SUITESTORE_COUNTOF(resChars), &resLen);

    return (rc == PCSL_STRING_OK) ? (char*)&resChars : NULL;
}
#undef SUITESTORE_COUNTOF

/**
 * Tells if a suite exists.
 *
 * @param suiteId ID of a suite
 *
 * @return ALL_OK if a suite exists,
 *         NOT_FOUND if not,
 *         OUT_OF_MEMORY if out of memory or IO error,
 *         IO_ERROR if IO error,
 *         SUITE_CORRUPTED_ERROR is suite is found in the list,
 *         but it's corrupted
 */
MIDPError
midp_suite_exists(SuiteIdType suiteId) {
    MIDPError status;

    if (UNUSED_SUITE_ID == suiteId) {
        return NOT_FOUND;
    }

    if (suiteId == g_lastSuiteExistsID) {
        /* suite exists - we have already checked */
        return ALL_OK;
    }

    g_lastSuiteExistsID = UNUSED_SUITE_ID;

    /* The internal romized suite will not be found in appdb. */
    if (suiteId == INTERNAL_SUITE_ID) {
        g_lastSuiteExistsID = suiteId;
        return ALL_OK;
    }

    status = suite_in_list(suiteId);

    if (status == ALL_OK) {
        g_lastSuiteExistsID = suiteId;
    }

    return status;
}

/**
 * Gets location of the class path for the suite with the specified suiteId.
 *
 * Note that memory for the in/out parameter classPath is
 * allocated by the callee. The caller is responsible for
 * freeing it using pcsl_mem_free().
 *
 * @param suiteId The application suite ID
 * @param storageId storage ID, INTERNAL_STORAGE_ID for the internal storage
 * @param checkSuiteExists true if suite should be checked for existence or not
 * @param classPath The in/out parameter that contains returned class path
 * @return error code that should be one of the following:
 * <pre>
 *     ALL_OK, OUT_OF_MEMORY, NOT_FOUND,
 *     SUITE_CORRUPTED_ERROR, BAD_PARAMS
 * </pre>
 */
MIDPError
midp_suite_get_class_path(SuiteIdType suiteId,
                          StorageIdType storageId,
                          jboolean checkSuiteExists,
                          pcsl_string *classPath) {
    MIDPError status = ALL_OK;
    int suiteExistsOrNotChecked;

    if (checkSuiteExists) {
        status = midp_suite_exists(suiteId);
        suiteExistsOrNotChecked = (status == ALL_OK ||
                                   status == SUITE_CORRUPTED_ERROR);
    } else {
        /*
         * Don't need to check is the suite exist,
         * just construct the classpath for the given suite ID.
         */
        suiteExistsOrNotChecked = 1;
    }

    if (suiteExistsOrNotChecked) {
        status = get_class_path_impl(suiteId, storageId, classPath,
                                     &JAR_EXTENSION);
    } else {
        *classPath = PCSL_STRING_NULL;
    }

    return status;
}

#if ENABLE_MONET
/**
 * Only for MONET--Gets the class path to binary application image
 * for the suite with the specified MIDlet suite id.
 *
 * It is different from "usual" class path in that class path points to a
 * jar file, while this binary application image path points to a MONET bundle.
 *
 * Note that memory for the in/out parameter classPath is
 * allocated by the callee. The caller is responsible for
 * freeing it using pcsl_mem_free().
 *
 * @param suiteId The application suite ID
 * @param storageId storage ID, INTERNAL_STORAGE_ID for the internal storage
 * @param checkSuiteExists true if suite should be checked for existence or not
 * @param classPath The in/out parameter that contains returned class path
 * @return  error code that should be one of the following:
 * <pre>
 *     ALL_OK, OUT_OF_MEMORY, NOT_FOUND,
 *     SUITE_CORRUPTED_ERROR, BAD_PARAMS
 * </pre>
*/
MIDPError
midp_suite_get_bin_app_path(SuiteIdType suiteId,
                            StorageIdType storageId,
                            pcsl_string *classPath) {
    return get_class_path_impl(suiteId, storageId, classPath,
                               &APP_IMAGE_EXTENSION);
}
#endif

/**
 * Gets location of the cached resource with specified name
 * for the suite with the specified suiteId.
 *
 * Note that when porting memory for the in/out parameter
 * filename MUST be allocated  using pcsl_mem_malloc().
 * The caller is responsible for freeing the memory associated
 * with filename parameter.
 *
 * @param suiteId       The application suite ID
 * @param storageId     storage ID, INTERNAL_STORAGE_ID for the internal storage
 * @param pResourceName Name of the cached resource
 * @param pFileName     The in/out parameter that contains returned filename
 * @return error code that should be one of the following:
 * <pre>
 *     ALL_OK, OUT_OF_MEMORY, NOT_FOUND,
 *     SUITE_CORRUPTED_ERROR, BAD_PARAMS
 * </pre>
 */
MIDPError
midp_suite_get_cached_resource_filename(SuiteIdType suiteId,
                                        StorageIdType storageId,
                                        const pcsl_string * pResourceName,
                                        pcsl_string * pFileName) {
    const pcsl_string* root = storage_get_root(storageId);
    pcsl_string returnPath = PCSL_STRING_NULL;
    pcsl_string resourceFileName = PCSL_STRING_NULL;
    jint suiteIdLen = GET_SUITE_ID_LEN(suiteId);
    jsize resourceNameLen = pcsl_string_length(pResourceName);

    *pFileName = PCSL_STRING_NULL;

    if (resourceNameLen > 0) {
        /* performance hint: predict buffer capacity */
        int fileNameLen = PCSL_STRING_ESCAPED_BUFFER_SIZE(
            resourceNameLen + pcsl_string_length(&TMP_EXT));
        pcsl_string_predict_size(&resourceFileName, fileNameLen);

        if ( /* Convert any slashes */
            pcsl_string_append_escaped_ascii(&resourceFileName,
                pResourceName) != PCSL_STRING_OK ||
            /* Add the extension */
            pcsl_string_append(&resourceFileName, &TMP_EXT) !=
                PCSL_STRING_OK) {
            pcsl_string_free(&resourceFileName);
            return OUT_OF_MEMORY;
        }
    }

    /* performance hint: predict buffer capacity */
    pcsl_string_predict_size(&returnPath, pcsl_string_length(root) +
        suiteIdLen + pcsl_string_length(&resourceFileName));

    if (PCSL_STRING_OK != pcsl_string_append(&returnPath, root) ||
            PCSL_STRING_OK != pcsl_string_append(&returnPath,
                midp_suiteid2pcsl_string(suiteId)) ||
            PCSL_STRING_OK != pcsl_string_append(&returnPath,
                &resourceFileName)) {
        pcsl_string_free(&resourceFileName);
        pcsl_string_free(&returnPath);
        return OUT_OF_MEMORY;
    }

    pcsl_string_free(&resourceFileName);

    *pFileName = returnPath;

    return ALL_OK;
}

/**
 * Retrieves an ID of the storage where the midlet suite with the given suite ID
 * is stored.
 *
 * @param suiteId The application suite ID
 * @param pSuiteId [out] receives an ID of the storage where the suite is stored
 *
 * @return error code (ALL_OK if successful)
 */
MIDPError
midp_suite_get_suite_storage(SuiteIdType suiteId, StorageIdType* pStorageId) {
    MIDPError status;
    MidletSuiteData* pData;
    char* pszError;

    if (pStorageId == NULL) {
        return BAD_PARAMS;
    }

    if (suiteId == INTERNAL_SUITE_ID) {
        /* handle a special case: predefined suite ID is given */
        *pStorageId = INTERNAL_STORAGE_ID;
        return ALL_OK;
    }

    /* load _suites.dat */
    status = read_suites_data(&pszError);
    storageFreeError(pszError);

    if (status == ALL_OK) {
        pData = get_suite_data(suiteId);
        if (pData) {
            *pStorageId = pData->storageId;
        } else {
            *pStorageId = UNUSED_STORAGE_ID;
            status = NOT_FOUND;
        }
    }

    return status;
}

/**
 * If the suite exists, this function returns a unique identifier of
 * MIDlet suite. Note that suite may be corrupted even if it exists.
 * If the suite doesn't exist, a new suite ID is created.
 *
 * @param vendor name of the vendor that created the application, as
 *          given in a JAD file
 * @param name name of the suite, as given in a JAD file
 * @param pSuiteId [out] receives the platform-specific suite ID of the
 *          application given by vendorName and appName, or string with
 *          a null data if suite does not exist, or
 *          out of memory error occured, or suite is corrupted.
 *
 * @return  ALL_OK if suite found,
 *          NOT_FOUND if suite does not exist (so a new ID was created),
 *          other error code in case of error
 */
MIDPError
midp_get_suite_id(const pcsl_string* vendor, const pcsl_string* name,
                  SuiteIdType* pSuiteId) {
    MIDPError status;
    char *pszError;
    MidletSuiteData* pData;

    *pSuiteId = UNUSED_SUITE_ID;

    /* load _suites.dat */
    status = read_suites_data(&pszError);
    storageFreeError(pszError);
    if (status != ALL_OK) {
        return status;
    }

    pData = g_pSuitesData;

    /* try to find a suite */
    while (pData != NULL) {
        if (pcsl_string_equals(&pData->varSuiteData.suiteName, name) &&
                pcsl_string_equals(&pData->varSuiteData.suiteVendor, vendor)) {
            *pSuiteId = pData->suiteId;
            return ALL_OK; /* IMPL_NOTE: consider SUITE_CORRUPTED_ERROR */
        }

        pData = pData->nextEntry;
    }

    /* suite was not found - create a new suite ID */
    status = midp_create_suite_id(pSuiteId);

    return (status == ALL_OK) ? NOT_FOUND : status;
}

/**
 * Find and return the property the matches the given key.
 * The returned value need not be freed because it resides
 * in an internal data structure.
 *
 * @param pProperties property list
 * @param key key of property to find
 *
 * @return a pointer to the property value,
 *        or to PCSL_STRING_NULL if not found.
 */
pcsl_string*
midp_find_property(MidpProperties* pProperties, const pcsl_string* key) {
    int i;

    /* Properties are stored as key, value pairs. */
    for (i = 0; i < pProperties->numberOfProperties; i++) {
        if (pcsl_string_equals(&pProperties->pStringArr[i * 2], key)) {
            return &pProperties->pStringArr[(i * 2) + 1];
        }
    }

    return (pcsl_string*)&PCSL_STRING_NULL;
}

/**
 * Free a list of properties. Does nothing if passed NULL.
 *
 * @param pProperties property list
 */
void
midp_free_properties(MidpProperties* pProperties) {
    /* Properties are stored as key, value pairs. */
    if (!pProperties || pProperties->numberOfProperties <= 0) {
        return;
    }

    free_pcsl_string_list(pProperties->pStringArr,
        pProperties->numberOfProperties * 2);
    pProperties->pStringArr = NULL;
}

/**
 * Gets the properties of a MIDlet suite to persistent storage.
 * <pre>
 * The format of the properties file will be:
 * <number of strings as int (2 strings per property)>
 *    {repeated for each property}
 *    <length of a property key as int>
 *    <property key as jchars>
 *    <length of property value as int>
 *    <property value as jchars>
 * </pre>
 *
 *
 * Note that memory for the strings inside the returned MidpProperties
 * structure is allocated by the callee, and the caller is
 * responsible for freeing it using midp_free_properties().
 *
 * @param suiteId ID of the suite
 *
 * @return properties in a pair pattern of key and value,
 * use the status macros to check the result. A SUITE_CORRUPTED_ERROR
 * is returned as a status of MidpProperties when suite is corrupted
 */
MidpProperties
midp_get_suite_properties(SuiteIdType suiteId) {
    pcsl_string filename;
    MidpProperties result = { 0, ALL_OK, NULL };
    int len;
    char* pszError;
    MIDPError status;

    /*
     * This is a public API which can be called without the VM running
     * so we need automatically init anything needed, to make the
     * caller's code less complex.
     *
     * Initialization is performed in steps so that we do use any
     * extra resources such as the VM for the operation being performed.
     */
    if (midpInit(LIST_LEVEL) != 0) {
        result.numberOfProperties = 0;
        result.status =  OUT_OF_MEMORY;
        return result;
    }

    /*
    if (check_for_corrupted_suite(suiteId) == SUITE_CORRUPTED_ERROR) {
        result.numberOfProperties = 0;
        result.status = SUITE_CORRUPTED_ERROR;
        return result;
    }
    */

    if (get_property_file(suiteId, KNI_TRUE, &filename) != ALL_OK) {
        result.numberOfProperties = 0;
        result.status = NOT_FOUND;
        return result;
    }

    status = get_string_list(&pszError, &filename, &result.pStringArr, &len);
    pcsl_string_free(&filename);
    if (status != ALL_OK) {
        result.numberOfProperties = 0;
        result.status = status;
        storageFreeError(pszError);
        return result;
    }

    if (len < 0) {
        /* error */
        result.numberOfProperties = 0;
        result.status = GENERAL_ERROR;
    } else {
        /* each property is 2 strings (key and value) */
        result.numberOfProperties = len / 2;
    }

    return result;
}

/* ------------------------------------------------------------ */
/*                          Implementation                      */
/* ------------------------------------------------------------ */

/**
 * Tells if a given suite is in a list of the installed suites.
 *
 * @param suiteId unique ID of the midlet suite
 *
 * @return ALL_OK if the suite is in the list of the installed suites,
 *         NOT_FOUND if not,
 *         IO_ERROR if an i/o error occured when reading the information
 *         about the installed suites,
 *         OUT_OF_MEMORY if out of memory or IO error,
 *         SUITE_CORRUPTED_ERROR is suite is found in the list, but it's
 *         corrupted.
 */
static MIDPError
suite_in_list(SuiteIdType suiteId) {
    MIDPError status;
    char* pszError;
    MidletSuiteData* pData;

    /* load _suites.dat */
    status = read_suites_data(&pszError);
    storageFreeError(pszError);
    if (status != ALL_OK) {
        return status;
    }

    pData = get_suite_data(suiteId);

    if (pData != NULL) {
        /*
         * Make sure that suite is not corrupted. Return
         * SUITE_CORRUPTED_ERROR if the suite is corrupted.
         * Remove the suite before returning the status.
         */
        status = check_for_corrupted_suite(suiteId);
    } else {
        status = NOT_FOUND;
    }

    return status;
}

/**
 * Gets the classpath for the specified MIDlet suite id.
 *
 * Note that memory for the in/out parameter classPath is
 * allocated by the callee. The caller is responsible for
 * freeing it using pcsl_mem_free().
 *
 * @param suiteId   The suite id used to identify the MIDlet suite
 * @param storageId storage ID, INTERNAL_STORAGE_ID for the internal storage
 * @param classPath The in/out parameter that contains returned class path
 * @param extension Extension of the file (
 *
 * @return one of the error codes:
 * <pre>
 *     ALL_OK, OUT_OF_MEMORY
 * </pre>
 */
static MIDPError
get_class_path_impl(SuiteIdType suiteId,
                    jint storageId,
                    pcsl_string * classPath,
                    const pcsl_string * extension) {
    const pcsl_string* root = storage_get_root(storageId);
    pcsl_string path = PCSL_STRING_NULL;
    jint suiteIdLen = GET_SUITE_ID_LEN(suiteId);

    *classPath = PCSL_STRING_NULL;

    /* performance hint: predict buffer capacity */
    pcsl_string_predict_size(&path, pcsl_string_length(root)
                                    + suiteIdLen
                                    + pcsl_string_length(extension));
    if (PCSL_STRING_OK != pcsl_string_append(&path, root) ||
            PCSL_STRING_OK != pcsl_string_append(&path,
                midp_suiteid2pcsl_string(suiteId)) ||
            PCSL_STRING_OK != pcsl_string_append(&path, extension)) {
        pcsl_string_free(&path);
        return OUT_OF_MEMORY;
    }

    *classPath = path;

    return ALL_OK;
}

/**
 * Retrieves the list of strings in a file.
 * The file has the number of strings at the front, each string
 * is a length and the jchars.
 *
 * @param ppszError pointer to character string pointer to accept an error
 * @param pFilename name of the file of strings
 * @param paList pointer to an array of pcsl_strings, free with
 *               free_pcsl_string_list
 * @param pStringNum number of strings if successful (can be 0)
 *
 * @return error code (ALL_OK if successful)
 */
static MIDPError
get_string_list(char** ppszError, const pcsl_string* pFilename,
                pcsl_string** paList, int* pStringNum) {
    char* pszTemp;
    int i = 0;
    int handle;
    int numberOfStrings = 0;
    pcsl_string* pStrings = NULL;
    MIDPError status = ALL_OK;

    *ppszError = NULL;
    *paList = NULL;
    *pStringNum = 0;

    handle = storage_open(ppszError, pFilename, OPEN_READ);
    if (*ppszError != NULL) {
        return IO_ERROR;
    }

    do {
        storageRead(ppszError, handle, (char*)&numberOfStrings,
            sizeof (numberOfStrings));
        if (*ppszError != NULL) {
            status = IO_ERROR;
            break;
        }
        if (numberOfStrings == 0) {
            break;
        }

        pStrings = alloc_pcsl_string_list(numberOfStrings);
        if (pStrings == NULL) {
            status = OUT_OF_MEMORY;
            break;
        }

        for (i = 0; i < numberOfStrings; i++) {
            pStrings[i] = PCSL_STRING_NULL;
        }

        for (i = 0; i < numberOfStrings; i++) {
            storage_read_utf16_string(ppszError, handle, &pStrings[i]);
            if (*ppszError != NULL) {
                status = IO_ERROR;
                break;
            }

        }

        if (i != numberOfStrings) {
            status = SUITE_CORRUPTED_ERROR;
            break;
        }
    } while (0);

    storageClose(&pszTemp, handle);
    storageFreeError(pszTemp);

    if (status == ALL_OK) {
        *paList = pStrings;
        *pStringNum = numberOfStrings;
    } else if (pStrings != NULL) {
        free_pcsl_string_list(pStrings, i);
    }

    return status;
}
