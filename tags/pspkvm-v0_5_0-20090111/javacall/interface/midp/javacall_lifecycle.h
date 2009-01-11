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
#ifndef __JAVACALL_LIFECYCLE_H_
#define __JAVACALL_LIFECYCLE_H_

/**
 * @file javacall_lifecycle.h
 * @ingroup Lifecycle
 * @brief Javacall interfaces for lifecycle
 */

#ifdef __cplusplus
extern "C" {
#endif

#include "javacall_defs.h"

/******************************************************************************
 ******************************************************************************
 ******************************************************************************

  NOTIFICATION FUNCTIONS
  - - - -  - - - - - - -
  The following functions are implemented by Sun.
  Platform is required to invoke these function for each occurence of the
  undelying event.
  The functions need to be executed in platform's task/thread

 ******************************************************************************
 ******************************************************************************
 ******************************************************************************/

/**
 * @defgroup Lifecycle Lifecycle API
 * @ingroup JTWI
 * @{
 */

/**
 * @defgroup NotificationLifecycle Notification API for Lifecycle
 * @ingroup Lifecycle
 * @{
 */ 

/**
 * The platform should invoke this function in platform context to start
 * Java.
 */
void javanotify_start(void);

/**
 * The platform should invoke this function in platform context to pause
 * Java.
 */
void javanotify_pause(void);

/**
 * The platform should invoke this function in platform context to end pause
 * and resume Java.
 */
void javanotify_resume(void);
/**
 * The platform should invoke this function in platform context to pause
 * Java bytecode execution (without invoking pauseApp)
 */
void javanotify_internal_pause(void);

/**
 * The platform should invoke this function in platform context to end
 * an internal pause and resume Java bytecode processing
 */
void javanotify_internal_resume(void);

/**
 * The platform should invoke this function in platform context to end Java.
 */
void javanotify_shutdown(void);

/**
 * A notification function for telling Java to perform installation of
 * a MIDlet via http,
 *
 * The given url should be of the form http://www.sun.com/a/b/c/d.jad then
 * Java will start a graphical installer will download the MIDlet
 * fom the internet.
 *
 * @param httpUrl null-terminated http url string of MIDlet's jad file.
 *      The url is of the following form:
 *      http://www.website.com/a/b/c/d.jad
 *
 */
void javanotify_install_midlet(const char * httpUrl);

/**
 * A notification function for telling Java to perform installation of
 * a content via http, for SprintAMS.
 *
 * This function requires that the descriptor (JADfile, or GCDfile)
 * has already been downloaded and resides somewhere on the file system.
 * The function also requires the full URL that was used to download the
 * file.
 * 
 * The given URL should be of the form http://www.sun.com/a/b/c/d.jad
 * or http://www.sun.com/a/b/c/d.gcd.  
 * Java will start a graphical installer which will download the content
 * fom the Internet.
 *
 * @param httpUrl null-terminated http URL string of the content 
 *        descriptor. The URL is of the following form:
 *        http://www.website.com/a/b/c/d.jad
 * @param descFilePath full path of the descriptor file which is of the 
 *        form:
 *        /a/b/c/d.jad  or /a/b/c/d.gcd
 * @param descFilePathLen length of the file path
 * @param isJadFile set to TRUE if the mime type of of the downloaded
 *        descriptor file is <tt>text/vnd.sun.j2me.app-descriptor</tt>. If 
 *        the mime type is anything else (e.g., <tt>text/x-pcs-gcd</tt>), 
 *        this must be set to FALSE.
 * @param isSilent set to TRUE if the content is to be installed silently,
 *        without intervention from the user. (e.g., in the case of SL
 *        or SI messages)
 * 
 */
void javanotify_install_content(const char * httpUrl,
                                const javacall_utf16* descFilePath,
                                int descFilePathLen,
                                javacall_bool isJadFile,
                                javacall_bool isSilent);

/**
 * A notification function for telling Java to perform installation of
 * a MIDlet from filesystem,
 *
 * The installation will be performed in the background without launching
 * the graphic installer application.
 *
 * The given path is the full path to MIDlet's jad file or jad.
 * In case the MIDlet's jad file is specified, then
 * the MIDlet's jar file muts reside in the same directory as the jad
 * file.
 *
 * @param jadFilePath full path the jad (or jar) file which is of the form:
 *        file://a/b/c/d.jad
 * @param jadFilePathLen length of the file path
 * @param userWasAsked a flag indicating whether the platform already asked
 *        the user for permission to download and install the application
 *        so there's no need to ask again and we can immediately install.
 */
void javanotify_install_midlet_from_filesystem(const javacall_utf16* jadFilePath,
                                               int jadFilePathLen,
                                               int userWasAsked);

/**
 * @enum javacall_lifecycle_state
 * @brief TCK domain type
 */
typedef enum {
    JAVACALL_LIFECYCLE_TCK_DOMAIN_UNTRUSTED,
    JAVACALL_LIFECYCLE_TCK_DOMAIN_UNTRUSTED_MIN,
    JAVACALL_LIFECYCLE_TCK_DOMAIN_UNTRUSTED_MAX,
    JAVACALL_LIFECYCLE_TCK_DOMAIN_TRUSTED
} javacall_lifecycle_tck_domain;

/**
 * The platform should invoke this function in platform context to start
 * the Java VM and run TCK.
 *
 * @param url the http location of the TCK server
 *            the url should be of the form: "http://host:port"
 * @param domain the TCK execution domain
 */
void javanotify_start_tck(char* url, javacall_lifecycle_tck_domain domain);

/**
 * The platform should invoke this function in platform context to start
 * the Java VM and run i3test framework.
 *
 * @param arg1 optional argument 1
 * @param arg2 optional argument 2
 *
 * @note allowed argument description can be obtained by '-help' value as arg1.
 */
void javanotify_start_i3test(char* arg1, char* arg2);

/**
 * The platform should invoke this function in platform context to start
 * the Java VM and run installed Java Content Handler.
 *
 * @param handlerID launched Content Handler ID
 * @param url Invocation parameter: URL
 * @param action optional Invocation parameter: Action
 *
 * @note allowed argument description can be obtained by '-help' value as arg1.
 */
void javanotify_start_handler(char* handlerID, char* url, char* action);

/**
 * The platform should invoke this function in platform context to start
 * the Java VM with arbitrary arguments.
 *
 * @param argc number of command-line arguments
 * @param argv array of command-line arguments
 *
 * @note This is a service function and it is introduced in the javacall
 *       interface for debug purposes. Please DO NOT CALL this function without
 *       being EXPLICITLY INSTRUCTED to do so.
 */
void javanotify_start_java_with_arbitrary_args(int argc, char* argv[]);

/** @} */

/** 
 * @defgroup MandatoryLifecycle Mandatory Lifecycle API
 * @ingroup Lifecycle
 * 
 * Lifecycle APIs define the functionality for:
 *   Announcing Lifecycle state change
 *   Platform request
 *
 * @{
 */
 
/**
 * @enum javacall_lifecycle_state
 * @brief Java lifecycle state
 */
typedef enum {
    /** MIDlet started */
    JAVACALL_LIFECYCLE_MIDLET_STARTED           =10,
    /** MIDlet paused */
    JAVACALL_LIFECYCLE_MIDLET_PAUSED            =11,
    /** MIDlet resumed */
    JAVACALL_LIFECYCLE_MIDLET_RESUMED           =12,
    /** MIDlet shutdown */
    JAVACALL_LIFECYCLE_MIDLET_SHUTDOWN          =13,
    /** MIDlet install completed */
    JAVACALL_LIFECYCLE_MIDLET_INSTALL_COMPLETED =15,
    /** MIDlet paused internally */
    JAVACALL_LIFECYCLE_MIDLET_INTERNAL_PAUSED   =16,
    /** MIDlet resumed internally */
    JAVACALL_LIFECYCLE_MIDLET_INTERNAL_RESUMED  =17
} javacall_lifecycle_state ;

/**
 * Inform on change of the lifecycle status of the VM
 *
 * Java will invoke this function whenever the lifecycle status of the running
 * MIDlet is changes, for example when the running MIDlet has entered paused
 * status, the MIDlet has shut down etc.
 *
 * @param state new state of the running MIDlet. can be either,
 *        - JAVACALL_LIFECYCLE_MIDLET_STARTED
 *        - JAVACALL_LIFECYCLE_MIDLET_PAUSED
 *        - JAVACALL_LIFECYCLE_MIDLET_RESUMED
 *        - JAVACALL_LIFECYCLE_MIDLET_SHUTDOWN
 *        - JAVACALL_LIFECYCLE_MIDLET_INSTALL_COMPLETED
 * @param status return code of the state change
 *        If state is JAVACALL_LIFECYCLE_MIDLET_SHUTDOWN, the status
 *        param will be set to <tt>JAVACALL_OK</tt> if MIDlet closed
 *        gracefully or <tt>JAVACALL_FAIL</tt> if MIDlet was killed
 *        If state is JAVACALL_LIFECYCLE_MIDLET_INSTALL_COMPLETED,
 *        status param will be set to <tt>JAVACALL_OK</tt> if MIDlet
 *        was installed successfully, or <tt>JAVACALL_FAIL</tt> if
 *        installation failed
 *        For states other than JAVACALL_LIFECYCLE_MIDLET_SHUTDOWN and
 *        JAVACALL_LIFECYCLE_MIDLET_INSTALL_COMPLETED the parameter
 *        status is not used.
 */
void javacall_lifecycle_state_changed(javacall_lifecycle_state state,
                                      javacall_result status);

/**
 * Starts a new process to handle the given URL. The new process executes
 * the value of the <tt>com.sun.midp.midlet.platformRequestCommand</tt>
 * system property. The URL is passed as this process' sole command-line
 * argument.
 *
 * If the platform has the appropriate capabilities and resources available,
 * it SHOULD bring the appropriate application to the foreground and let the
 * user interact with the content, while keeping the MIDlet suite running in
 * the background. If the platform does not have appropriate capabilities or
 * resources available, it MAY wait to handle the URL request until after the
 * MIDlet suite exits. In this case, when the requesting MIDlet suite exits,
 * the platform MUST then bring the appropriate application (if one exists) to
 * the foreground to let the user interact with the content.
 *
 * This is a non-blocking method. In addition, this method does NOT queue multiple
 * requests. On platforms where the MIDlet suite must exit before the request
 * is handled, the platform MUST handle only the last request made. On platforms
 * where the MIDlet suite and the request can be handled concurrently, each
 * request that the MIDlet suite makes MUST be passed to the platform software
 * for handling in a timely fashion.
 *
 * If the URL specified is of the form tel:555555555, as specified in RFC2806
 * (http://www.ietf.org/rfc/rfc2806.txt), then the platform MUST interpret this as
 * a request to initiate a voice call. The request MUST be passed to the phone
 * application to handle if one is present in the platform. The phone application,
 * if present, MUST be able to set up local and global phone calls and also perform
 * DTMF post dialing. Not all elements of RFC2806 need be implemented, especially
 * the area-specifier or any other requirement on the terminal to know its context.
 * The isdn-subaddress, service-provider and future-extension may also be ignored.
 * Pauses during dialing are not relevant in some telephony services. Devices MAY
 * choose to support additional URL schemes beyond the requirements listed above.
 *
 * @param urlString An ascii URL string
 *
 * @return <tt>JAVACALL_OK</tt> if the platform request is configured, and the MIDlet
 *                             suite MUST first exit before the content can be fetched.
 *         <tt>JAVACALL_FAIL</tt> if the platform request is configured, and the MIDlet
 *                             suite don't need to exit while the content can be fetched.
 *         <tt>JAVACALL_CONNECTION_NOT_FOUND</tt> if the platform request URL is not supported.
 */
javacall_result javacall_lifecycle_platform_request(char* urlString);

/**
 * external event loop
 */
void JavaTask(void);

/**
 * The function signals the underlying platform that JVM needs to execute
 * one timeslice. Used in slave mode only.
 */
void javacall_schedule_vm_timeslice(void);

/**
 * In slave mode executes one JVM time slice.
 * @return <tt>-2</tt> if JVM has exited
 *         <tt>-1</tt> if all the Java threads are blocked waiting for events 
 *         <tt>timeout value</tt>  the nearest timeout of all blocked Java threads
 */
javacall_int64 javanotify_vm_timeslice(void); 

/**
 * The platform should invoke this function in platform context 
 * to select another running application to be the foreground.
 */
void javanotify_select_foreground_app(void);

/**
 * The platform should invoke this function in platform context 
 * to bring the Application Manager Screen to foreground.
 */
void javanotify_switch_to_ams(void);


void javacall_global_init(void);
/** @} */

/** @} */

#ifdef __cplusplus
}
#endif

#endif


