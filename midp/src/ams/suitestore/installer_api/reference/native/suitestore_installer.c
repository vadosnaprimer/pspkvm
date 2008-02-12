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
 * This is reference implementation of the Installer API of the MIDlet suite
 * storage subsystem. The functions implemented in this file need not to be
 * ported if using a native installer (what is probably the case if using NAMS).
 */

#include <string.h>

#ifndef UNDER_CE
#include <time.h> /* IMPL_NOTE: can we use it? */
#endif

#include <kni.h>
#include <pcsl_memory.h>
#include <midpInit.h>
#include <midpStorage.h>
#include <imageCache.h>

#include <suitestore_intern.h>
#include <suitestore_listeners.h>
#include <suitestore_installer.h>
#include <suitestore_task_manager.h>

#if ENABLE_ICON_CACHE
#include <suitestore_icon_cache.h>

/**
 * Loads a native image from cache or jar file into memory.
 *
 * @param suiteId       The suite id
 * @param pImageName    The image resource name
 * @param **ppImageData Pointer where a buffer will be allocated and data stored
 * @return              -1 if failed, else length of buffer
 */
static int
getImageForIconCache(SuiteIdType suiteId, const pcsl_string* pImageName,
                     unsigned char **ppImageData);
#endif

/* Description of these internal functions can be found bellow in the code. */
static MIDPError store_install_properties(SuiteIdType suiteId,
    const MidpProperties* pJadProps, const MidpProperties* pJarProps);

static MIDPError store_jar(char** ppszError, SuiteIdType suiteId,
    StorageIdType storageId, const pcsl_string* jarName,
        pcsl_string* pNewFileName);

static MIDPError add_to_suite_list_and_save(MidletSuiteData* pMsd);

static MIDPError write_install_info(char** ppszError, SuiteIdType suiteId,
                                    const MidpInstallInfo* pInstallInfo);

/* ------------------------------------------------------------ */
/*                           Public API                         */
/* ------------------------------------------------------------ */

/**
 * Returns a unique identifier of MIDlet suite.
 * Constructed from the combination
 * of the values of the <code>MIDlet-Name</code> and
 * <code>MIDlet-Vendor</code> attributes.
 *
 * @param vendor name of the vendor that created the application, as
 *          given in a JAD file
 * @param name name of the suite, as given in a JAD file
 * @param pSuiteId [out] receives the platform-specific storage name of the
 *          application given by vendorName and appName
 *
 * @return ALL_OK if success, else an error code
 */
MIDPError
midp_create_suite_id(SuiteIdType* pSuiteId) {
    char* pszError;
    SuiteIdType newSuiteId;
    MidletSuiteData* pData;
    int maxSuiteId;
    MIDPError status;

    *pSuiteId = UNUSED_SUITE_ID;

    /*
     * This is a public API which can be called without the VM running
     * so we need automatically init anything needed, to make the
     * caller's code less complex.
     *
     * Initialization is performed in steps so that we do use any
     * extra resources such as the VM for the operation being performed.
     */
    if (midpInit(LIST_LEVEL) != 0) {
        return OUT_OF_MEMORY;
    }

    /* load _suites.dat */
    status = read_suites_data(&pszError);
    storageFreeError(pszError);
    if (status != ALL_OK) {
        return status;
    }

    pData = g_pSuitesData;

    /* find a maximal existing suite id */
    maxSuiteId = 1;

    while (pData != NULL) {
        if (pData->suiteId > maxSuiteId) {
            maxSuiteId = pData->suiteId;
        }

        pData = pData->nextEntry;
    }

    newSuiteId = maxSuiteId + 1;

    /* check for overflow */
    if (newSuiteId == MAX_SUITE_ID) {
        int suiteIdExist;
        newSuiteId = 0;
        do {
            newSuiteId++;
            suiteIdExist = 0;
            pData = g_pSuitesData;

            while (pData != NULL) {
                if (pData->suiteId == newSuiteId) {
                    suiteIdExist = 1;
                    break;
                }

                pData = pData->nextEntry;
            }

            if (newSuiteId == MAX_SUITE_ID) {
                /* there are no free suite IDs! */
                return OUT_OF_STORAGE;
            }
        } while (suiteIdExist || newSuiteId == UNUSED_SUITE_ID ||
                 newSuiteId == INTERNAL_SUITE_ID);
    }

    *pSuiteId = newSuiteId;

    return ALL_OK;
}

/**
 * Read a the install information of a suite from persistent storage.
 * The caller should have make sure the suite ID is valid.
 * The caller should call midp_free_install_info when done with the information.
 * jadProps and jarProps fields are NOT filled by this function.
 * <pre>
 * The fields are
 *   jadUrl
 *   jarUrl
 *   ca
 *   domain
 *   trusted
 *
 * Unicode strings are written as an int length and a jchar array.
 * </pre>
 * @param ppszError pointer to character string pointer to accept an error
 * @param suiteId ID of a suite
 *
 * @return an InstallInfo struct, use the status macros to check for
 * error conditions in the struct
 */
MidpInstallInfo
read_install_info(char** ppszError, SuiteIdType suiteId) {
    MidpInstallInfo installInfo;
    pcsl_string filename;
    char* pszTemp;
    int handle;
    int no_errors = 0;
    int i;
    int rc;

    *ppszError = NULL;
    memset((unsigned char*)&installInfo, 0, sizeof (MidpInstallInfo));

    rc = build_suite_filename(suiteId, &INSTALL_INFO_FILENAME, &filename);

    if (rc < 0) {
        installInfo.status = rc;
        return installInfo;
    }

    handle = storage_open(ppszError, &filename, OPEN_READ);
    pcsl_string_free(&filename);

    if (*ppszError != NULL) {
        installInfo.status = IO_ERROR;
        return installInfo;
    }

    do {
        storage_read_utf16_string(ppszError, handle, &installInfo.jadUrl_s);
        storage_read_utf16_string(ppszError, handle, &installInfo.jarUrl_s);
        storage_read_utf16_string(ppszError, handle, &installInfo.domain_s);

        storageRead(ppszError, handle, (char*)(&installInfo.trusted),
            sizeof (installInfo.trusted));
        if (*ppszError != NULL) {
            break;
        }

        installInfo.authPathLen = 0;
        installInfo.authPath_as = NULL;
        storageRead(ppszError, handle, (char*)(&installInfo.authPathLen),
            sizeof (installInfo.authPathLen));
        if (*ppszError != NULL) {
            break;
        }

        if (installInfo.authPathLen <= 0) {
            no_errors = 1;
            break;
        }

        installInfo.authPath_as = alloc_pcsl_string_list(installInfo.authPathLen);
        if (NULL == installInfo.authPath_as) {
            installInfo.authPathLen = 0;
            break;
        }

        for (i = 0; i < installInfo.authPathLen; i++) {
            storage_read_utf16_string(ppszError, handle, &installInfo.authPath_as[i]);
        }

        if (i != installInfo.authPathLen) {
            installInfo.authPathLen = i;
            break;
        }

        no_errors = 1;
    } while (0);

    storageClose(&pszTemp, handle);
    storageFreeError(pszTemp);

    if (no_errors) {
        return installInfo;
    }

    midp_free_install_info(&installInfo);

    memset((unsigned char*)&installInfo, 0, sizeof (MidpInstallInfo));

    if (*ppszError != NULL) {
        installInfo.status = IO_ERROR;
    } else {
        installInfo.status = OUT_OF_MEMORY;
    }

    return installInfo;
}

/**
 * Frees an InstallInfo struct. Does nothing if passed NULL.
 *
 * @param pInstallInfo installation information returned from read_install_info.
 */
void
midp_free_install_info(MidpInstallInfo* pInstallInfo) {
    if (pInstallInfo != NULL) {
        pcsl_string_free(&pInstallInfo->jadUrl_s);
        pcsl_string_free(&pInstallInfo->jarUrl_s);
        pcsl_string_free(&pInstallInfo->domain_s);

        if (pInstallInfo->authPathLen > 0) {
            free_pcsl_string_list(pInstallInfo->authPath_as,
                                  pInstallInfo->authPathLen);
        }

        pInstallInfo->authPath_as = NULL;
        pInstallInfo->authPathLen = 0;
    }
}


/**
 * Gets the install information of a MIDlet suite.
 *
 * Note that memory for the strings inside the returned MidpInstallInfo
 * structure is allocated by the callee, and the caller is
 * responsible for freeing it using midp_free_install_info().
 *
 * @param suiteId  ID of the suite
 *
 * @return Installation information, use status macros to check the result
 * A SUITE_CORRUPTED_ERROR is returned as a status in InstallInfo
 * when suite is corrupted
 */
MidpInstallInfo
midp_get_suite_install_info(SuiteIdType suiteId) {
    MidpInstallInfo installInfo;
    char* pszError;

    /*
     * This is a public API which can be called without the VM running
     * so we need automatically init anything needed, to make the
     * caller's code less complex.
     *
     * Initialization is performed in steps so that we do use any
     * extra resources such as the VM for the operation being performed.
     */
    if (midpInit(LIST_LEVEL) != 0) {
        installInfo.status = OUT_OF_MEMORY;
        return installInfo;
    }

    /*
    if (check_for_corrupted_suite(suiteId) == SUITE_CORRUPTED_ERROR) {
        installInfo.status = SUITE_CORRUPTED_ERROR;
        return installInfo;
    }
    */

    installInfo = read_install_info(&pszError, suiteId);
    if (pszError != NULL) {
        storageFreeError(pszError);
    }

#if VERIFY_ONCE
    /* Read verify hash info */
    installInfo.status = readVerifyHash(suiteId,
        &installInfo.pVerifyHash, &installInfo.verifyHashLen);
#endif /* VERIFY_ONCE */

    return installInfo;
}

/**
 * Stores or updates a midlet suite.
 *
 * @param pInstallInfo structure containing the following information:<br>
 * <pre>
 *     id - unique ID of the suite;
 *     jadUrl - where the JAD came from, can be null;
 *     jarUrl - where the JAR came from;
 *     jarFilename - name of the downloaded MIDlet suite jar file;
 *     suiteName - name of the suite;
 *     suiteVendor - vendor of the suite;
 *     authPath - authPath if signed, the authorization path starting
 *                with the most trusted authority;
 *     domain - security domain of the suite;
 *     trusted - true if suite is trusted;
 *     verifyHash - may contain hash value of the suite with
 *                  preverified classes or may be NULL;
 *     jadProps - properties given in the JAD as an array of strings in
 *                key/value pair order, can be null if jadUrl is null
 *     jarProps - properties of the manifest as an array of strings
 *                in key/value pair order
 * </pre>
 *
 * @param pSuiteSettings structure containing the following information:<br>
 * <pre>
 *     permissions - permissions for the suite;
 *     pushInterruptSetting - defines if this MIDlet suite interrupt
 *                            other suites;
 *     pushOptions - user options for push interrupts;
 *     suiteId - unique ID of the suite, must be equal to the one given
 *               in installInfo;
 *     boolean enabled - if true, MIDlet from this suite can be run;
 * </pre>
 *
 * @param pSuiteData structure containing the following information:<br>
 * <pre>
 *     suiteId - unique ID of the suite, must be equal to the value given
 *               in installInfo and suiteSettings parameters;
 *     storageId - ID of the storage where the MIDlet should be installed;
 *     numberOfMidlets - number of midlets in the suite;
 *     displayName - the suite's name to display to the user;
 *     midletToRunClassName - the midlet's class name if the suite contains
 *                            only one midlet, ignored otherwise;
 *     iconName - name of the icon for this suite.
 * </pre>
 *
 * @return status ALL_OK for success else an error code
 */
MIDPError
midp_store_suite(const MidpInstallInfo* pInstallInfo,
                 const MidpSuiteSettings* pSuiteSettings,
                 const MidletSuiteData* pSuiteData) {
    MIDPError status;
    char* pszError;
    lockStorageList *node;
    SuiteIdType suiteId;
#if ENABLE_ICON_CACHE
    unsigned char* pIconData = NULL;
    int iconBufLen = -1;
#endif

    if (pInstallInfo == NULL || pSuiteSettings == NULL || pSuiteData == NULL) {
        return BAD_PARAMS;
    }

    suiteId = pSuiteData->suiteId;

    node = find_storage_lock(suiteId);
    if (node != NULL) {
        if (node->update != KNI_TRUE) {
            return SUITE_LOCKED;
        }
    }

    /*
     * This is a public API which can be called without the VM running
     * so we need automatically init anything needed, to make the
     * caller's code less complex.
     *
     * Initialization is performed in steps so that we do use any
     * extra resources such as the VM for the operation being performed.
     */
    if (midpInit(LIST_LEVEL) != 0) {
        return OUT_OF_MEMORY;
    }

    /* notify the registered listeners that we starting to install the suite */
    suite_listeners_notify(SUITESTORE_LISTENER_TYPE_INSTALL,
        SUITESTORE_OPERATION_START, ALL_OK, pSuiteData);

    do {
        /* create a structure describing the new midlet suite */
        MidletSuiteData* pMsd = pcsl_mem_malloc(sizeof(MidletSuiteData));
        if (pMsd == NULL) {
            status = OUT_OF_MEMORY;
            break;
        }

        memcpy((char*)pMsd, (char*)pSuiteData, sizeof(MidletSuiteData));

        pMsd->nextEntry = NULL;

        status = begin_transaction(TRANSACTION_INSTALL, UNUSED_SUITE_ID,
            &pSuiteData->varSuiteData.pathToJar);
        if (status != ALL_OK) {
            break;
        }

        status = store_jar(&pszError, suiteId, pMsd->storageId,
            /* holds the temporary name of the file with the suite */
            &pSuiteData->varSuiteData.pathToJar,
            /* the new (permanent) name of the suite will be returned here */
            &pMsd->varSuiteData.pathToJar);

        if (status != ALL_OK) {
            pcsl_mem_free(pMsd);        
            if (pszError != NULL) {
                storageFreeError(pszError);
            }
            break;
        }

#if ENABLE_CONTROL_ARGS_FROM_JAD
        /*
         * Set all permissions for the suite to ALLOWED if
         * MIDP_ARGS=allow_all_permissions
         * property is found in the suite's jad file.
         */
        {
            #include "perm_number.h"

            MIDP_JAD_CONTROL_ARGS pJadArgs[MAX_JAD_CONTROL_ARGS];
            MIDPError found;
            MidpSuiteSettings trustedSuiteSettings;
            jbyte pPermissions[NUMBER_OF_PERMISSIONS];

            parse_control_args_from_jad(&pInstallInfo->jadProps,
                                        pJadArgs, MAX_JAD_CONTROL_ARGS);
            found = get_jad_control_arg_value(pJadArgs,
                                              "allow_all_permissions", NULL);
            if (found == ALL_OK) {
                /* set pushInterruptSetting to Permissions.ALLOWED */
                trustedSuiteSettings.pushInterruptSetting = 1;
                trustedSuiteSettings.pushOptions = 0; /* no push options */
                trustedSuiteSettings.suiteId = suiteId;
                trustedSuiteSettings.enabled = 1;
                /* IMPL_NOTE: must be set to the value of Permissions.ALLOWED */
                memset((char*)pPermissions, 1, NUMBER_OF_PERMISSIONS);
                trustedSuiteSettings.pPermissions   = pPermissions;
                trustedSuiteSettings.permissionsLen = NUMBER_OF_PERMISSIONS;

                pSuiteSettings = &trustedSuiteSettings;
            }
        }
#endif /* ENABLE_CONTROL_ARGS_FROM_JAD */

        /* add the new structure to the suite list */
        status = add_to_suite_list_and_save(pMsd);
        if ((status != ALL_OK) && (status != SUITE_CORRUPTED_ERROR)) {
            pcsl_mem_free(pMsd);
            /* notify the listeners that the installation has failed */
            suite_listeners_notify(SUITESTORE_LISTENER_TYPE_INSTALL,
                SUITESTORE_OPERATION_END, status, pSuiteData);
            /* can't break here because midp_remove_suite must not be called */
            (void)finish_transaction();
            return status;
        }

        status = write_install_info(&pszError, suiteId, pInstallInfo);
        if (status != ALL_OK) {
            if (pszError != NULL) {
                storageFreeError(pszError);
            }
            break;
        }

        status = store_install_properties(suiteId, &pInstallInfo->jadProps,
                                          &pInstallInfo->jarProps);
        if (status != ALL_OK) {
            break;
        }

        /* Suites start off as enabled. */
        status = write_settings(&pszError, suiteId, KNI_TRUE,
            pSuiteSettings->pushInterruptSetting, pSuiteSettings->pushOptions,
                pSuiteSettings->pPermissions, pSuiteSettings->permissionsLen);
        if (status != ALL_OK) {
            if (pszError != NULL) {
                storageFreeError(pszError);
            }
            break;
        }

#if ENABLE_IMAGE_CACHE
        createImageCache(suiteId, pMsd->storageId);
#endif

#if ENABLE_ICON_CACHE
        iconBufLen = getImageForIconCache(suiteId, &pMsd->varSuiteData.iconName,
                                          &pIconData);
        if (iconBufLen > 0) {
            midp_add_suite_icon(suiteId, &pMsd->varSuiteData.iconName,
                                pIconData, iconBufLen);
        }
#endif
    } while (0);

    (void)finish_transaction();

    if (status != ALL_OK) {
        midp_remove_suite(suiteId);
    }

    /* notify the listeners that the installation finished */
    suite_listeners_notify(SUITESTORE_LISTENER_TYPE_INSTALL,
                           SUITESTORE_OPERATION_END, status, pSuiteData);

    return status;
}

/* ------------------------------------------------------------ */
/*                          Implementation                      */
/* ------------------------------------------------------------ */

/**
 * Stores the JAR of a MIDlet suite to persistent storage.
 *
 * @param ppszError pointer to character string pointer to accept an error
 * @param suiteId ID of the suite
 * @param storageId ID of the storage where the jar should be saved
 * @param pJarName filename of the temporary Jar
 * @param pNewFileName [out] permanent filename of the suite's jar.
 * May point to the same string as jarName, in this case this string
 * will be freed before storing the new value.
 * Caller is responsible for freeing it with pcsl_string_free().
 *
 * @returns error code (ALL_OK if successful)
 */
static MIDPError
store_jar(char** ppszError, SuiteIdType suiteId, StorageIdType storageId,
          const pcsl_string* pJarName, pcsl_string* pNewFileName) {
    MIDPError status;
    pcsl_string filename = PCSL_STRING_NULL;

    *pNewFileName = PCSL_STRING_NULL;

    status = midp_suite_get_class_path(suiteId, storageId,
                                       KNI_FALSE, &filename);
    if (status != ALL_OK) {
        return status;
    }

    *ppszError = NULL;
    storage_rename_file(ppszError, pJarName, &filename);

    if (*ppszError != NULL) {
        pcsl_string_free(&filename);
        return NOT_FOUND; /* probably invalid path to the file */
    }

    /*
     * If source and destination points to the same location, release
     * the string before saving a new value.
     */
    if (pNewFileName == pJarName) {
        pcsl_string_free(pNewFileName);
    }

    *pNewFileName = filename;

    return ALL_OK;
}

/**
 * Adds a new midlet suite to the list of installed suites.
 * If the suite with the same ID exists in the list, it will be overwritten.
 *
 * @param pMsd a structure containing the information about the midlet suite
 *
 * @return error code (ALL_OK if successful)
 */
static MIDPError
add_to_suite_list_and_save(MidletSuiteData* pMsd) {
    MIDPError status;
    char* pszError;
    MidletSuiteData* pExistingSuite;
    MidletSuiteData* pPrev = g_pSuitesData;

    if (!pMsd || pMsd->suiteId == UNUSED_SUITE_ID ||
            pMsd->suiteId == INTERNAL_SUITE_ID) {
        return ALL_OK;
    }

    /*
     * This function is called from midp_store_suite(),
     * so read_suites_data() was already called.
     */

    /* check if the suite with the same ID already exists */
    pExistingSuite = get_suite_data(pMsd->suiteId);

    if (pExistingSuite == NULL) {
        /* if this is a new suite, add a new structure to the list */
        g_pSuitesData = pMsd;
        g_pSuitesData->nextEntry = pPrev;
        g_numberOfSuites++;
    } else {
        /* if the suite with such ID already exists, overwrite it */
        MidletSuiteData* pData = g_pSuitesData;
        pPrev = NULL;
        pMsd->nextEntry = pExistingSuite->nextEntry;

        /* finding the entry preceding the pExistingSuite */
        while (pData != NULL) {
            if (pData->suiteId == pMsd->suiteId) {
                break;
            }
            pPrev = pData;
            pData = pData->nextEntry;
        }

        if (pPrev == NULL) {
            /* pExistingSuite is the first entry in the list */
            g_pSuitesData = pMsd;
        } else {
            pPrev->nextEntry = pMsd;
        }

        free_suite_data_entry(pExistingSuite);
    }

    status = write_suites_data(&pszError);
    storageFreeError(pszError);

    if (status != ALL_OK) {
        g_numberOfSuites--;
        pMsd->nextEntry = NULL;
        g_pSuitesData = pPrev;
    }

    return status;
}

/**
 * Write the install information of a suite to persistent storage.
 * The caller should have make sure the suite ID is valid.
 * <pre>
 * The fields are
 *   jadUrl
 *   jarUrl
 *   domain
 *   trusted
 *   authPathLen
 *   authPath
 *
 * Unicode strings are written as an int and jshort array.
 * </pre>
 * @param ppszError pointer to character string pointer to accept an error
 * @param suiteId ID of a suite
 * @param pInstallInfo a pointer to allocated InstallInfo struct
 *
 * @return error code (ALL_OK if successful)
 */
static MIDPError
write_install_info(char** ppszError, SuiteIdType suiteId,
                   const MidpInstallInfo* pInstallInfo) {
    pcsl_string filename;
    char* pszTemp;
    int handle;
    int i;
    MIDPError status;
    *ppszError = NULL;

    if (pInstallInfo == NULL) {
        return BAD_PARAMS;
    }

    status = build_suite_filename(suiteId, &INSTALL_INFO_FILENAME, &filename);
    if (status != ALL_OK) {
        return status;
    }

    handle = storage_open(ppszError, &filename, OPEN_READ_WRITE_TRUNCATE);
    pcsl_string_free(&filename);
    if (*ppszError != NULL) {
        return IO_ERROR;
    }

    status = ALL_OK;
    do {
        storage_write_utf16_string(ppszError, handle, &pInstallInfo->jadUrl_s);
        storage_write_utf16_string(ppszError, handle, &pInstallInfo->jarUrl_s);
        storage_write_utf16_string(ppszError, handle, &pInstallInfo->domain_s);

        storageWrite(ppszError, handle, (char*)(&pInstallInfo->trusted),
            sizeof (pInstallInfo->trusted));
        if (*ppszError != NULL) {
            break;
        }

        storageWrite(ppszError, handle, (char*)(&pInstallInfo->authPathLen),
            sizeof (pInstallInfo->authPathLen));
        if (*ppszError != NULL) {
            break;
        }

        for (i = 0; i < pInstallInfo->authPathLen; i++) {
            storage_write_utf16_string(ppszError, handle,
                &pInstallInfo->authPath_as[i]);
            if (*ppszError != NULL) {
                break;
            }
        }
    } while (0);

    if (*ppszError != NULL) {
        status = IO_ERROR;
    }

    storageClose(&pszTemp, handle);
    storageFreeError(pszTemp);

    return status;
}

/**
 * Stores the properties of a MIDlet suite to persistent storage.
 * Take 2 property lists for convenience. The JAD properties are written
 * first then the JAR properties.
 * <pre>
 * The format of the properties file will be:
 * <number of strings as int (2 strings per property)>
 *    {repeated for each property}
 *    <length of a property key as int>
 *    <property key as jchars>
 *    <length of property value as int>
 *    <property value as jchars>
 * </pre>
 * @param suiteId  ID of the suite
 * @param pJadProps an array of strings, in a pair pattern of key and
 *  value
 * @param pJarProps an array of strings, in a pair pattern of key and
 *  value
 *
 * @return error code (ALL_OK for success)
 */
static MIDPError
store_install_properties(SuiteIdType suiteId, const MidpProperties* pJadProps,
                         const MidpProperties* pJarProps) {
    pcsl_string filename;
    char* pszError = NULL;
    int handle;
    int numberOfStrings;
    int i;
    MIDPError status;
    int numOfJadProps = pJadProps ? pJadProps->numberOfProperties : 0;
    int numOfJarProps = pJarProps ? pJarProps->numberOfProperties : 0;

    status = get_property_file(suiteId, KNI_TRUE, &filename);
    if (status != ALL_OK) {
        return status;
    }

    handle = storage_open(&pszError, &filename, OPEN_READ_WRITE_TRUNCATE);
    pcsl_string_free(&filename);
    if (pszError != NULL) {
        storageFreeError(pszError);
        return IO_ERROR;
    }

    status = ALL_OK;
    do {
        numberOfStrings = (numOfJadProps + numOfJarProps) * 2;
        storageWrite(&pszError, handle, (char*)&numberOfStrings,
                     sizeof (numberOfStrings));
        if (pszError != NULL) {
            break;
        }

        /* The JAD properties override the JAR properties. Write them first. */
        for (i = 0; i < numOfJadProps * 2; i++) {
            storage_write_utf16_string(&pszError, handle,
                                       &pJadProps->pStringArr[i]);
            if (pszError != NULL) {
                break;
            }
        }

        if (pszError != NULL) {
            break;
        }

        for (i = 0; i < numOfJarProps * 2; i++) {
            storage_write_utf16_string(&pszError, handle,
                                       &pJarProps->pStringArr[i]);
            if (pszError != NULL) {
                break;
            }
        }
    } while(0);

    if (pszError != NULL) {
        status = IO_ERROR;
        storageFreeError(pszError);
    }

    storageClose(&pszError, handle);
    storageFreeError(pszError);

    return status;
}

#if ENABLE_ICON_CACHE
/**
 * Loads a native image from cache or jar file into memory.
 *
 * @param suiteId       The suite id
 * @param pImageName    The image resource name
 * @param **ppImageData Pointer where a buffer will be allocated and data stored
 * @return              -1 if failed, else length of buffer
 */
static int
getImageForIconCache(SuiteIdType suiteId, const pcsl_string* pImageName,
                     unsigned char **ppImageData) {
    int iconBufLen;

#if ENABLE_IMAGE_CACHE
    iconBufLen = loadImageFromCache(suiteId, pImageName,
                                    ppImageData);
#else
    /* IMPL_NOTE: the entry could be read using midpGetJarEntry(). */
    iconBufLen = -1;

    (void)suiteId;
    (void)pImageName;
    (void)ppImageData;
#endif /* ENABLE_IMAGE_CACHE */

    return iconBufLen;
}
#endif
