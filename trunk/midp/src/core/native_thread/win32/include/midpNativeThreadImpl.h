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

#ifndef _MIDP_NATIVE_THREAD_MD
#define _MIDP_NATIVE_THREAD_MD

#include <kni.h>

#include <windows.h>

/**
 * @file
 *
 * Platform specific implementation for basic system services 
 * for working with native threads.
 */

#ifdef __cplusplus
extern "C" {
#endif

/** platform independent thread handle type */
typedef HANDLE midp_ThreadId;

/** platform invalid thread id */
#define MIDP_INVALID_NATIVE_THREAD_ID ((midp_ThreadId)INVALID_HANDLE_VALUE)

/** platform independent thread routine parameter type */
typedef LPVOID midp_ThreadRoutineParameter;

/** platform independent thread routine type */
typedef DWORD WINAPI midp_ThreadRoutine(midp_ThreadRoutineParameter param);


#ifdef __cplusplus
}
#endif

#endif /* _MIDP_NATIVE_THREAD_MD */
