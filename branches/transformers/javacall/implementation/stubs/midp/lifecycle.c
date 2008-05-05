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

/**
 * @file
 *
 *
 */

#ifdef __cplusplus
extern "C" {
#endif
    
#include "javacall_lifecycle.h"





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
 * If the URL specified is of the form tel:<number>, as specified in RFC2806 
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
 * @param pszUrl An ascii URL string 
 *
 * @return <tt>JAVACALL_OK</tt> if the platform request is configured, and the MIDlet
 *                             suite MUST first exit before the content can be fetched.
 *         <tt>JAVACALL_FAIL</tt> if the platform request is configured, and the MIDlet
 *                             suite don't need to exit while the content can be fetched.
 *         <tt>JAVACALL_CONNECTION_NOT_FOUND</tt> if the platform request URL is not supported.
 */
javacall_result javacall_lifecycle_platform_request(char* urlString){
    return JAVACALL_FAIL;
}
    
    
/**
 * Inform on change of the lifecycle status of the VM
 *
 * Java will invoke this function whenever the lifecycle status of the running
 * MIDlet is changes, for example when the running MIDlet has entered paused
 * status, the MIDlet has shut down etc.
 *
 * @param state new state of the running MIDlet. can be either,
 *        <tt>JAVACALL_LIFECYCLE_MIDLET_STARTED</tt>
 *        <tt>JAVACALL_LIFECYCLE_MIDLET_PAUSED</tt>
 *        <tt>JAVACALL_LIFECYCLE_MIDLET_RESUMED</tt>
 *        <tt>JAVACALL_LIFECYCLE_MIDLET_SHUTDOWN</tt>
 *        <tt>JAVACALL_LIFECYCLE_MIDLET_INSTALL_COMPLETED</tt>
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
                                      javacall_result status){

}

#ifdef __cplusplus
}
#endif


