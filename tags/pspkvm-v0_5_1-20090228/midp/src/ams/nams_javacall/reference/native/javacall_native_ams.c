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

#include <javautil_unicode.h>
#include <javacall_native_ams.h>
#include <midpNativeAppManager.h>
#include <midpEvents.h>
#include <midp_runtime_info.h>
#include <listeners_intern.h>

#define MAX_CLASS_NAME_LEN   256
#define MAX_PROFILE_NAME_LEN 64
#define MAX_SUPPORTED_ARGS   3
#define MAX_ARG_LEN          256

/*
 * Forward declarations.
 */
static javacall_opcode midp_operation2javacall(jint midpOpcode);

static javacall_midlet_state
midp_midlet_state2javacall(jint midpMidletState);

static javacall_midlet_ui_state
midp_midlet_ui_state2javacall(jint midpMidletUiState);

static javacall_change_reason
midp_midlet_event_reason2javacall(jint midpEventReason);

void midp_listener_ams_operation_completed(const NamsEventData* pEventData);
void midp_listener_ams_midlet_state_changed(const NamsEventData* pEventData);
void midp_listener_ams_midlet_ui_state_changed(const NamsEventData* pEventData);

/**
 * Platform invokes this function to start the MIDP system.
 * It does not return until the system is stopped.
 *
 * @return <tt>JAVACALL_OK</tt> if successful,
 *         <tt>JAVACALL_FAIL</tt> otherwise
 */
javacall_result javanotify_ams_system_start() {
    MIDPError res = midp_system_initialize();
    if (res != ALL_OK) {
        return JAVACALL_FAIL;
    }

    midp_add_event_listener(midp_listener_ams_operation_completed,
                            SYSTEM_EVENT_LISTENER);
    midp_add_event_listener(midp_listener_ams_midlet_ui_state_changed,
                            DISPLAY_EVENT_LISTENER);
    midp_add_event_listener(midp_listener_ams_midlet_state_changed,
                            MIDLET_EVENT_LISTENER);

    return midp_system_start();
}

/**
 * Platform invokes this function to inform VM to start a specific MIDlet
 * suite.
 *
 * @param suiteID      ID of the suite to start
 * @param appID        ID of runtime midlet, ID must not be Zero
 * @param className    Fully qualified name of the MIDlet class
 * @param pRuntimeInfo Quotas and profile to set for the new application
 * @return <tt>JAVACALL_OK</tt> if all parameter are valid,
 *         <tt>JAVACALL_FAIL</tt> otherwise
 * @note this function just checks the parameters accuracy,
 *       the real status of MIDlet startup will be notified
 *       by <link>javacall_ams_midlet_stateChanged</link>
 */
javacall_result
javanotify_ams_midlet_start(const javacall_suite_id suiteID,
                            const javacall_app_id appID,
                            const javacall_utf16_string className,
                            const javacall_midlet_runtime_info* pRuntimeInfo) {
    return javanotify_ams_midlet_start_with_args(suiteID, appID,
        className, NULL, 0, pRuntimeInfo);
}

/**
 * Platform invokes this function to inform VM to start a specific MIDlet
 * suite with arguments.
 *
 * @param suiteID      ID of the suite to start with
 * @param appID        ID of runtime midlet
 * @param className    Fully qualified name of the MIDlet class
 * @param args         An array containning up to 3 arguments for
 *                     the MIDlet to be run
 * @param argsNum      Number of arguments
 * @param pRuntimeInfo Quotas and profile to set for the new application
 * @return <tt>JAVACALL_OK</tt> if all parameter are valid,
 *         <tt>JAVACALL_FAIL</tt> otherwise
 * @note  this function just checks the parameters accuracy,
 *        the real status of MIDlet startup will be notified by
 *        <link>javacall_ams_midlet_stateChanged</link>
 */
javacall_result
javanotify_ams_midlet_start_with_args(const javacall_suite_id suiteID,
                                      const javacall_app_id appID,
                                      const javacall_utf16_string className,
                                      const javacall_utf16_string *args,
                                      int argsNum,
                                      const javacall_midlet_runtime_info*
                                          pRuntimeInfo) {
    MIDPError res;                                          
    MidletRuntimeInfo mri, *pMri = NULL;
    javacall_result jcRes;
    javacall_int32 utf16Len;
    static jchar pProfileNameBuf[MAX_PROFILE_NAME_LEN];
    static jchar pClassName[MAX_CLASS_NAME_LEN];
    static jchar chArgs[MAX_SUPPORTED_ARGS][MAX_ARG_LEN];
    jint classNameLen = 0, argsLen[MAX_SUPPORTED_ARGS] = {0, 0, 0};
    int i;

    if (className == NULL || argsNum < 0 || (argsNum > 0 && args == NULL) ||
            (argsNum > MAX_SUPPORTED_ARGS)) {
        return JAVACALL_FAIL;
    }

    /* converting the class name from javacall_utf16_string to jchar* */
    jcRes = javautil_unicode_utf16_ulength(className, &utf16Len);
    if (jcRes != JAVACALL_OK) {
        return JAVACALL_FAIL;
    }

    jcRes = javautil_unicode_utf16_to_utf8(className, classNameLen,
        (unsigned char*) pClassName, sizeof(pClassName) / sizeof(jchar) - 1,
            (javacall_int32*) &classNameLen);
    if (jcRes != JAVACALL_OK) {
        return JAVACALL_FAIL;
    }

    pClassName[classNameLen] = 0;

    /* converting the midlet's arguments */
    for (i = 0; i < argsNum; i++) {
        if (args[i] == 0) {
            return JAVACALL_FAIL;
        }

        jcRes = javautil_unicode_utf16_utf8length(args[i], &utf16Len);
        if (jcRes != JAVACALL_OK) {
            return JAVACALL_FAIL;
        }

        jcRes = javautil_unicode_utf16_to_utf8(args[i],
            utf16Len, (unsigned char*) chArgs[i],
                MAX_ARG_LEN - 1, (javacall_int32*) &argsLen[i]);
        if (jcRes != JAVACALL_OK) {
            return JAVACALL_FAIL;
        }

        chArgs[i][argsLen[i]] = 0;
    }

    /*
     * converting the structure with the runtime information from
     * javacall to MIDP format
     */
    if (pRuntimeInfo != NULL) {
        mri.memoryReserved = (jint) pRuntimeInfo->memoryReserved;
        mri.memoryTotal    = (jint) pRuntimeInfo->memoryTotal;
        mri.usedMemory     = (jint) pRuntimeInfo->usedMemory;
        mri.priority       = (jint) pRuntimeInfo->priority;

        /* converting profileName from javacall_utf16_string to jchar* */
        jcRes = javautil_unicode_utf16_ulength(pRuntimeInfo->profileName,
            &utf16Len);
        if (jcRes != JAVACALL_OK) {
            return JAVACALL_FAIL;
        }

        if (utf16Len > 0 && pRuntimeInfo->profileName != NULL) {
            jcRes = javautil_unicode_utf16_to_utf8(pRuntimeInfo->profileName,
                (javacall_int32) mri.profileNameLen,
                (unsigned char*) mri.profileName,
                sizeof(pProfileNameBuf) / sizeof(jchar) - 1,
                (javacall_int32*) &mri.profileNameLen);

            if (jcRes != JAVACALL_OK) {
                return JAVACALL_FAIL;
            }

            pProfileNameBuf[mri.profileNameLen] = 0;
            mri.profileName = pProfileNameBuf;
        } else {
            mri.profileNameLen = 0;
            mri.profileName = NULL;
        }

        pMri = &mri;
    }
    
    res = midp_midlet_create_start_with_args((SuiteIdType)suiteID,
        (const jchar*)pClassName, classNameLen, (const jchar**)args, argsLen,
            (jint)argsNum, (jint)appID, pMri);
    return (res == ALL_OK) ? JAVACALL_OK : JAVACALL_FAIL;
}

/**
 * Platform invokes this function to inform VM to shutdown a specific
 * running MIDlet. If it doesn't exit in the specified amount of milliseconds,
 * it will be forcefully terminated.
 *
 * @param appID appID of the suite to shutdown
 * @param timeoutMillSecond shutdown the suite in timeout millseconds
 * @return <tt>JAVACALL_OK</tt> if <code>suiteID</code> has a proper value
 *         <tt>JAVACALL_FAIL</tt> otherwise
 * @note the real status of operation will be notified by
 *       <link>javacall_ams_midlet_stateChanged</link>
 */
javacall_result
javanotify_ams_midlet_shutdown(const javacall_app_id appID,
                               int timeoutMillSeconds) {
    MIDPError res = midp_midlet_destroy((jint)appID, (jint)timeoutMillSeconds);
    return (res == ALL_OK) ? JAVACALL_OK : JAVACALL_FAIL;                                   
}

/**
 * Platform invokes this function to inform VM to switch a specific MIDlet
 * suite to foreground.
 *
 * @param appID appID of the suite to switch
 *
 * @return <tt>JAVACALL_OK</tt> if <code>suiteID</code> has a proper value
 *         <tt>JAVACALL_FAIL</tt> otherwise
 * @note the real status of operation will be notified by
 *       <link>javacall_ams_midlet_stateChanged</link>
 */
javacall_result
javanotify_ams_midlet_switch_foreground(const javacall_app_id appID) {
    MIDPError res = midp_midlet_set_foreground((jint)appID);
    return (res == ALL_OK) ? JAVACALL_OK : JAVACALL_FAIL;
}

/**
 * Platform invokes this function to inform VM to switch current MIDlet
 * suite to background, and no MIDlet will switch to foregound.
 *
 * @return <tt>JAVACALL_OK</tt> if <code>suiteID</code> has a proper value
 *         <tt>JAVACALL_FAIL</tt> otherwise
 * @note the real status of operation will be notified by
 *       <link>javacall_ams_midlet_stateChanged</link>
 */
javacall_result javanotify_ams_midlet_switch_background() {
    MIDPError res = midp_midlet_set_foreground(MIDLET_APPID_NO_FOREGROUND);
    return (res == ALL_OK) ? JAVACALL_OK : JAVACALL_FAIL;
}

/**
 * Platform invokes this function to inform VM to pause a specific MIDlet
 *
 * @param appID appID of the suite to pause
 * @return <tt>JAVACALL_OK</tt> if <code>suiteID</code> has a proper value
 *         <tt>JAVACALL_FAIL</tt> otherwise
 * @note the real status of operation will be notified by
 *       <link>javacall_ams_midlet_stateChanged</link>
 */
javacall_result javanotify_ams_midlet_pause(const javacall_app_id appID) {
    MIDPError res = midp_midlet_pause((jint)appID);
    return (res == ALL_OK) ? JAVACALL_OK : JAVACALL_FAIL;
}

/**
 * Platform invokes this function to inform VM to resume a specific MIDlet
 *
 * @param appID appID of the suite to resume
 * @return <tt>JAVACALL_OK</tt> if <code>suiteID</code> has a proper value
 *         <tt>JAVACALL_FAIL</tt> otherwise
 * @note the real status of operation will be notified by
 *       <link>javacall_ams_midlet_stateChanged</link>
 */
javacall_result javanotify_ams_midlet_resume(const javacall_app_id appID) {
    MIDPError res = midp_midlet_resume((jint)appID);
    return (res == ALL_OK) ? JAVACALL_OK : JAVACALL_FAIL;
}

/**
 * Platform invokes this function to get information about the suite containing
 * the specified running MIDlet. This call is synchronous.
 *
 * @param appId The ID used to identify the application
 *
 * @param pSuiteData [out] pointer to a structure where static information
 *                         about the midlet will be stored
 *
 * @return error code: <tt>JAVACALL_OK</tt> if successful,
 *                     <tt>JAVACALL_FAIL</tt> otherwise
 */
javacall_result
javanotify_ams_midlet_get_suite_info(const javacall_app_id appID,
                                     javacall_ams_suite_data* pSuiteData) {
    MidletSuiteData midpSuiteData;
    MIDPError res;

    if (pSuiteData == NULL) {
        return JAVACALL_FAIL;
    }

    res = midp_midlet_get_suite_info((jint)appID, &midpSuiteData);
    if (res != JAVACALL_OK) {
        return JAVACALL_FAIL;
    }

    /* copy data from the midp structure to the javacall one */
    pSuiteData->suiteId   = (javacall_suite_id) midpSuiteData.suiteId;
    pSuiteData->storageId = (javacall_int32) midpSuiteData.storageId;
    pSuiteData->isEnabled = (javacall_bool) midpSuiteData.isEnabled;
    pSuiteData->isEnabled = (javacall_bool) midpSuiteData.isTrusted;
    pSuiteData->numberOfMidlets = (javacall_int32) midpSuiteData.numberOfMidlets;
    pSuiteData->installTime = (long) midpSuiteData.installTime;
    pSuiteData->jadSize = (javacall_int32) midpSuiteData.jadSize;
    pSuiteData->jarSize = (javacall_int32) midpSuiteData.jarSize;
    pSuiteData->jarHashLen = (javacall_int32) midpSuiteData.jarHashLen;
    pSuiteData->isPreinstalled = (javacall_bool) midpSuiteData.isPreinstalled;

    pSuiteData->varSuiteData.pJarHash = midpSuiteData.varSuiteData.pJarHash;

    /*
     * IMPL_NOTE: the strings from midpSuiteData should be converted from
     *            pcsl_string and copied into the bellowing strings.
     */
    pSuiteData->varSuiteData.midletClassName = NULL;
    pSuiteData->varSuiteData.displayName = NULL;
    pSuiteData->varSuiteData.iconName = NULL;
    pSuiteData->varSuiteData.suiteVendor = NULL;
    pSuiteData->varSuiteData.suiteName = NULL;
    pSuiteData->varSuiteData.pathToJar = NULL;
    pSuiteData->varSuiteData.pathToSettings = NULL;

    return JAVACALL_OK;
}

/**
 * Platform invokes this function to get runtime information
 * about the specified MIDlet.
 *
 * This call is asynchronous, the result will be reported later through
 * passing a MIDLET_INFO_READY_EVENT event to SYSTEM_EVENT_LISTENER.
 *
 * @param appID The ID used to identify the application
 *
 * @return error code: <tt>JAVACALL_OK<tt> if successful (operation started),
 *                     <tt>JAVACALL_FAIL</tt> otherwise
 */
javacall_result
javanotify_ams_midlet_request_runtime_info(const javacall_app_id appID) {
    MIDPError res = midp_midlet_request_runtime_info((jint)appID);
    return (res == ALL_OK) ? JAVACALL_OK : JAVACALL_FAIL;
}

/**
 * Platform inform the VM to create the images cache.
 * @param suiteID  unique ID of the MIDlet suite
 *
 * @return <tt>JAVACALL_OK</tt> on success,
 *         <tt>JAVACALL_FAIL</tt>
 */
javacall_result
javanotify_ams_create_resource_cache(const javacall_suite_id suiteID) {
    (void) suiteID;
    return JAVACALL_FAIL;
}

/**
 * MIDP proxy for the javacall_ams_operation_completed() listener.
 *
 * @param pEventData full data about the event and about the application
 *                   caused this event
 */
void midp_listener_ams_operation_completed(const NamsEventData* pEventData) {
    void* pResult = NULL;

    if (pEventData == NULL) {
        return;
    }

    if (pEventData->state == ALL_OK) {
        if (pEventData->reason == NATIVE_MIDLET_GETINFO_REQUEST) {
            pResult = pEventData->pRuntimeInfo;
        }
    }
    
    javacall_ams_operation_completed(
        midp_operation2javacall(pEventData->reason),
        (javacall_app_id)pEventData->appId,
        pResult);
}


/**
 * MIDP proxy for the javacall_ams_midlet_state_changed() listener.
 *
 * @param pEventData full data about the event and about the application
 *                   caused this event
 */
void midp_listener_ams_midlet_state_changed(const NamsEventData* pEventData) {
    if (pEventData == NULL) {
        return;
    }

    javacall_ams_midlet_state_changed(
        midp_midlet_state2javacall(pEventData->state),
        (javacall_app_id)pEventData->appId,
        midp_midlet_event_reason2javacall(pEventData->reason));
}

/**
 * MIDP proxy for the javacall_ams_midlet_ui_state_changed() listener.
 *
 * @param pEventData full data about the event and about the application
 *                   caused this event
 */
void midp_listener_ams_ui_state_changed(const NamsEventData* pEventData) {
    if (pEventData == NULL) {
        return;
    }

    javacall_ams_midlet_state_changed(
        midp_midlet_ui_state2javacall(pEventData->state),
        (javacall_app_id)pEventData->appId,
        midp_midlet_event_reason2javacall(pEventData->reason));
}

/*
 * File-private utility functions.
 */

/**
 * Converts the given MIDP event reason constant to the Javacall
 * event reason constant.
 *
 * @param midpMidletState MIDP event reason constant
 *
 * @return Javacall event reason constant corresponding to the given MIDP one
 */
static javacall_change_reason
midp_midlet_event_reason2javacall(jint midpEventReason) {
    javacall_change_reason jcEventReason;

    /*
     * IMPL_NOTE: handle reason depending on the event.
     * MIDP_REASON_TERMINATED
     * MIDP_REASON_EXIT
     * MIDP_REASON_OTHER
     *
     * JAVACALL_MIDP_REASON_TERMINATED
     * JAVACALL_MIDP_REASON_EXIT
     */

    /*
     * We can't assume that the MIDP and Javacall constants are identical,
     * so this switch is used.
     */
    switch (midpEventReason) {
        case MIDLET_CONSTRUCTOR_FAILED: {
            jcEventReason = JAVACALL_MIDLET_CONSTRUCTOR_FAILED;
            break;
        }
        case MIDLET_SUITE_NOT_FOUND: {
            jcEventReason = JAVACALL_MIDLET_SUITE_NOT_FOUND;
            break;
        }
        case MIDLET_CLASS_NOT_FOUND: {
            jcEventReason = JAVACALL_MIDLET_STATE_DESTROYED;
            break;
        }
        case MIDLET_INSTANTIATION_EXCEPTION: {
            jcEventReason = JAVACALL_MIDLET_INSTANTIATION_EXCEPTION;
            break;
        }
        case MIDLET_ILLEGAL_ACCESS_EXCEPTION: {
            jcEventReason = JAVACALL_MIDLET_ILLEGAL_ACCESS_EXCEPTION;
            break;
        }
        case MIDLET_OUT_OF_MEM_ERROR: {
            jcEventReason = JAVACALL_MIDLET_OUT_OF_MEM_ERROR;
            break;
        }
        case MIDLET_RESOURCE_LIMIT: {
            jcEventReason = JAVACALL_MIDLET_STATE_ERROR;
            break;
        }
        case MIDLET_ISOLATE_RESOURCE_LIMIT: {
            jcEventReason = JAVACALL_MIDLET_ISOLATE_RESOURCE_LIMIT;
            break;
        }
        case MIDLET_ISOLATE_CONSTRUCTOR_FAILED: {
            jcEventReason = JAVACALL_MIDLET_ISOLATE_CONSTRUCTOR_FAILED;
            break;
        }
        default: {
            jcEventReason = JAVACALL_MIDP_REASON_OTHER;
        }
    }

    return jcEventReason;
}

/**
 * Converts the given MIDP operation code to the Javacall operation code.
 *
 * @param midpOpcode MIDP code of the operation
 *
 * @return Javacall opeartion corresponding to the given MIDP one
 */
static javacall_opcode midp_operation2javacall(jint midpOpcode) {
    javacall_opcode jcOperation;

    /*
     * We can't assume that the MIDP and Javacall constants are identical,
     * so this switch is used.
     */
    switch (midpOpcode) {
        case NATIVE_MIDLET_GETINFO_REQUEST: {
            jcOperation = JAVACALL_OPCODE_REQUEST_RUNTIME_INFO;
            break;
        }
        default: {
            jcOperation = JAVACALL_OPCODE_INVALID;
        }
    }

    return jcOperation;
}

/**
 * Converts the given MIDP midlet state constant
 * to the Javacall midlet state constant.
 *
 * @param midpMidletState MIDP midlet state constant
 *
 * @return Javacall midlet state constant corresponding to the given MIDP one
 */
static javacall_midlet_state midp_midlet_state2javacall(jint midpMidletState) {
    javacall_midlet_state jcMidletState;

    /*
     * We can't assume that the MIDP and Javacall constants are identical,
     * so this switch is used.
     */
    switch (midpMidletState) {
        case MIDP_MIDLET_STATE_ACTIVE: {
            jcMidletState = JAVACALL_MIDLET_STATE_ACTIVE;
            break;
        }
        case MIDP_MIDLET_STATE_PAUSED: {
            jcMidletState = JAVACALL_MIDLET_STATE_PAUSED;
            break;
        }
        case MIDP_MIDLET_STATE_DESTROYED: {
            jcMidletState = JAVACALL_MIDLET_STATE_DESTROYED;
            break;
        }
        case MIDP_MIDLET_STATE_ERROR: {
            jcMidletState = JAVACALL_MIDLET_STATE_ERROR;
            break;
        }
        default: {
            jcMidletState = JAVACALL_MIDLET_STATE_ERROR;
        }
    }

    return jcMidletState;
}

/**
 * Converts the given MIDP midlet state constant
 * to the Javacall midlet state constant.
 *
 * @param midpMidletState MIDP midlet state constant
 *
 * @return Javacall midlet state constant corresponding to the given MIDP one
 */
static javacall_midlet_ui_state
midp_midlet_ui_state2javacall(jint midpMidletUiState) {
    javacall_midlet_ui_state jcMidletUiState;

    /*
     * We can't assume that the MIDP and Javacall constants are identical,
     * so this switch is used.
     */
    switch (midpMidletUiState) {
        case MIDP_DISPLAY_STATE_FOREGROUND: {
            jcMidletUiState = JAVACALL_MIDLET_UI_STATE_FOREGROUND;
            break;
        }
        case MIDP_DISPLAY_STATE_BACKGROUND: {
            jcMidletUiState = JAVACALL_MIDLET_UI_STATE_BACKGROUND;
            break;
        }
        case MIDP_DISPLAY_STATE_FOREGROUND_REQUEST: {
            jcMidletUiState = JAVACALL_MIDLET_UI_STATE_FOREGROUND_REQUEST;
            break;
        }
        case MIDP_DISPLAY_STATE_BACKGROUND_REQUEST: {
            jcMidletUiState = JAVACALL_MIDLET_UI_STATE_BACKGROUND_REQUEST;
            break;
        }
        default: {
            /** Invalid state! Add to log? */
            jcMidletUiState = (javacall_midlet_ui_state) midpMidletUiState;
        }
    }

    return jcMidletUiState;
}
