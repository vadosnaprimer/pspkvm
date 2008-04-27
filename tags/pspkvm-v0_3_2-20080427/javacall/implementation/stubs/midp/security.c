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
#include "javacall_security.h"

#ifdef __cplusplus
extern "C" {
#endif

/**
 * create a new keystore iterator instance 
 * See example in the documentation for javacall_security_keystore_get_next()
 *
 * @param handle address of pointer to file identifier
 *        on successful completion, file identifier is returned in this 
 *        argument. This identifier is platform specific and is opaque
 *        to the caller.  
 * @return <tt>JAVACALL_OK</tt> on success, 
 *         <tt>JAVACALL_FAIL</tt>
 */
javacall_result javacall_security_keystore_start(
                                                 /*OUT*/ javacall_handle* handle) {
    return JAVACALL_FAIL;
}

/**
 * check if further keystore entries exist for the current iterator
 * See example in the documentation for javacall_security_keystore_get_next()
 *
 * @param keyStoreHandle the handle of the iterator 
 *
 * @retval JAVACALL_OK if more entries exist
 * @retval JAVACALL_FAIL if no more entries exist
 */
javacall_result javacall_security_keystore_has_next(
                                                    javacall_handle keyStoreHandle) {
    return JAVACALL_FAIL;
}

/**
 * get the next keystore entry record and advance the iterator
 * The implementation should supply pointers to platform-alloacated 
 * parameters and buffers.
 * The platform is responsible for any allocations of deallocations
 * of the returned pointers of all parameters:
 * Caller of this function will not free the passed pointers.
 * The out parameters are valid for usage by the caller of this function 
 * only until calling javacall_security_keystore_end() or subsequent calls
 * to javacall_security_keystore_get_next().
 *
 * Deallocation of necessary pointers can be performed by the platform in
 * the implementation of javacall_security_keystore_end() and
 * javacall_security_keystore_get_next().
 *
 * A sample usage of this function is the following:
 *
 *      void foo(void) {
 *         unsigned short*  owner;
 *         int              ownerSize,modulusSize,exponentSize,domainSize;
 *         javacall_int64   validityStartMillissec, validityEndMillisec;
 *         unsigned char*   modulus;
 *         unsigned char*   exponentSize;,
 *         char*            domain,
 *         javacall_handle h=javacall_security_iterator_start();
 *         while(javacall_security_iterator_has_next(h)==JAVACALL_OK) {
 *
 *           javacall_security_iterator_get_next(h,
 *               &owner,&ownerSize,&validityStartMillissec,
 *               &validityEndMillisec,&modulus,&modulusSize,
 *               &exponent,&exponentSize,&domain,&domainSize);
 *           //...
 *         }
 *         javacall_security_iterator_end(h);
 *
 * @param keyStoreHandle the handle of the iterator 
 * @param owner a poiner to the distinguished name of the owner
 * @param ownerSize length of the distinguished name of the owner
 * @param validityStartMillisec start time of the key's validity period 
 *                  in milliseconds since Jan 1, 1970
 * @param validityEndMillisec end time of the key's validity period 
 *                  in milliseconds since Jan 1, 1970
 * @param modulus RSA modulus for the public key
 * @param modulusSize length of RSA modulus 
 * @param exponent RSA exponent for the public key
 * @param exponentSize length of RSA exponent
 * @param domain name of the security domain
 * @param domainSize length of the security domain
 * @retval JAVACALL_OK if more entries exist
 * @retval JAVACALL_FAIL if no more entries exist
 */
javacall_result javacall_security_keystore_get_next(
                            javacall_handle      keyStoreHandle,
                            unsigned short**     owner,
                            int*                 ownerSize,
                            javacall_int64*      validityStartMillissec,
                            javacall_int64*      validityEndMillisec,
                            unsigned char**      modulus,
                            int*                 modulusSize,
                            unsigned char**      exponent,
                            int*                 exponentSize,
                            char**               domain,
                            int*                 domainSize) {
    return JAVACALL_FAIL;
}

/**
 * free a keystore iterator. 
 * After calling this function, the keyStoreHandle handle will not be used.
 * The implementation may perform any platfrom-sepcific deallocations.
 *
 * @param keyStoreHandle the handle of the iterator 
 * 
 * @retval JAVACALL_OK if successful
 * @retval JAVACALL_FAIL on error
 */
javacall_result javacall_security_keystore_end(javacall_handle keyStoreHandle) {
    return JAVACALL_FAIL;
}

/**
 * Invoke the native permission dialog.
 * When the native permission dialog is displayed, Java guarantees
 * no attempt will be made to refresh the screen from Java and the 
 * LCD control will be passed to the platform.
 * 
 * This function is asynchronous.
 * Return JAVACALL_WOULD_BLOCK. The notification for the dismissal
 * of the permission dialog will be sent later via notify function,
 * see javanotify_security_permission_dialog_finish().
 *
 * @param message the message the platform should display to the user.
 *                The platform must copy the message string to its own buffer.
 * @param messageLength length of message string
 * @param options the combination of permission level options 
 *                to present to the user.
 *                The options flags are any combination (bitwise-OR) 
 *                of the following:
 *                <ul>
 *                  <li> JAVACALL_SECURITY_ALLOW_SESSION </li>
 *                  <li> JAVACALL_SECURITY_ALLOW_ ONCE </li>
 *                  <li> JAVACALL_SECURITY_ALLOW_ALWAYS </li>
 *                  <li> JAVACALL_SECURITY_DENY_SESSION </li>
 *                  <li> JAVACALL_SECURITY_DENY_ ONCE </li>
 *                  <li> JAVACALL_SECURITY_DENY_ALWAYS </li>
 *                </ul>
 * 
 * The platform is responsible for providing the coresponding strings 
 * for each permission level option according to the locale.
 *       
 * @retval JAVACALL_WOULD_BLOCK this indicates that the permission
 *         dialog will be displayed by the platform.
 * @retval JAVACALL_FAIL in case prompting the permission dialog failed.
 * @retval JAVACALL_NOT_IMPLEMENTED in case the native permission dialog
 *         is not implemented by the platform. 
 */
javacall_result javacall_security_permission_dialog_display(javacall_utf16* message,
                                                            int messageLength,
                                                            int options) {
    return JAVACALL_NOT_IMPLEMENTED;
}

#ifdef __cplusplus
}
#endif


