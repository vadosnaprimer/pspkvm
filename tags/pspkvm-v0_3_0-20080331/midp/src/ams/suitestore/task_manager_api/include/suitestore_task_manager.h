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
 * This header file contains Task Manager API of the MIDlet suite storage
 * subsystem.
 * The functions declared in this header need not to be ported if using NAMS.
 */

#ifndef _SUITESTORE_TASK_MANAGER_H_
#define _SUITESTORE_TASK_MANAGER_H_

#include <suitestore_common.h>

#ifdef __cplusplus
extern "C" {
#endif

/**
 * Retrieves the number of installed midlet suites.
 *
 * @param pNumOfSuites [out] pointer to variable to accept the number of suites
 *
 * @returns error code (ALL_OK if no errors)
 *
 */
MIDPError
midp_get_number_of_suites(int* pNumOfSuites);

/**
 * Get the list installed of MIDlet suite IDs.
 *
 * Note that memory for the suite IDs is allocated by the callee,
 * and the caller is responsible for freeing it using midp_free_suite_ids().
 *
 * @param ppSuites empty array of jints to fill with suite IDs
 * @param pNumOfSuites [out] pointer to variable to accept the number
 * of suites in the returned array
 *
 * @returns error code: ALL_OK if no errors,
 *          OUT_OF_MEMORY if for out of memory,
 *          IO_ERROR if an IO error
 */
MIDPError
midp_get_suite_ids(SuiteIdType** ppSuites, jint* pNumOfSuites);

/**
 * Frees a list of suite IDs.
 *
 * @param pSuites point to an array of suite IDs
 * @param numberOfSuites number of elements in pSuites
 */
void midp_free_suite_ids(SuiteIdType* pSuites, int numberOfSuites);

/**
 * Disables a suite given its suite ID.
 * <p>
 * The method does not stop the suite if is in use. However any future
 * attepts to run a MIDlet from this suite while disabled should fail.
 *
 * @param suiteId ID of the suite
 *
 * @return ALL_OK if no errors,
 *         NOT_FOUND if the suite does not exist,
 *         SUITE_LOCKED if the suite is locked,
 *         IO_ERROR if IO error has occured,
 *         OUT_OF_MEMORY if out of memory
 */
MIDPError midp_disable_suite(SuiteIdType suiteId);

/**
 * Enables a suite given its suite ID.
 * <p>
 * The method does update an suites that are currently loaded for
 * settings or of application management purposes.
 *
 * @param suiteId  ID of the suite
 *
 * @return ALL_OK if no errors,
 *         NOT_FOUND if the suite does not exist,
 *         SUITE_LOCKED if the suite is locked,
 *         IO_ERROR if IO error has occured,
 *         OUT_OF_MEMORY if out of memory
 */
MIDPError midp_enable_suite(SuiteIdType suiteId);

/**
 * Removes a software package given its suite ID
 * <p>
 * If the component is in use it must continue to be available
 * to the other components that are using it.  The resources it
 * consumes must not be released until it is not in use.
 *
 * @param suiteId  ID of the suite
 *
 * @return 0 if the suite does not exist SUITE_LOCKED if the
 * suite is locked
 */
MIDPError midp_remove_suite(SuiteIdType suiteId);

/**
 * Gets the amount of storage on the device that this suite is using.
 * This includes the JAD, JAR, management data, and RMS.
 *
 * @param suiteId ID of the suite
 *
 * @return number of bytes of storage the suite is using or less than
 * 0 if out of memory
 */
long midp_get_suite_storage_size(SuiteIdType suiteId);

/**
 * Checks the integrity of the suite storage database and of the
 * installed suites.
 *
 * @param fullCheck 0 to check just an integrity of the database,
 *                    other value for full check
 * @param delCorruptedSuites != 0 to delete the corrupted suites,
 *                           0 - to keep them (for re-installation).
 *
 * @return ALL_OK if no errors or an error code
 */
MIDPError midp_check_suites_integrity(int fullCheck, int delCorruptedSuites);

#ifdef __cplusplus
}
#endif

#endif /* _SUITESTORE_TASK_MANAGER_H_ */
