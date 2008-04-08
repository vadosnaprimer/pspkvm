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

/*
 * OsMisc_generic.cpp:
 */

#include "incls/_precompiled.incl"
#include "incls/_OsMisc_generic.cpp.incl"

#ifdef __cplusplus
extern "C" {
#endif

const JvmPathChar *OsMisc_get_classpath() {
  /* 
   * Returns a system-wide defined classpath. This function is called only
   * if a classpath is not given in parameters to JVM_Start() or JVM_Start2().
   * On Win32 and Linux this function calls getenv("CLASSPATH"). This function
   * is generally not needed in real devices.
   */
  return NULL;
}


void OsMisc_flush_icache(address start, int size) {
  /* 
   * flush_icache is used, for example, to flush any caches used by a
   * code segment that is deoptimized or moved during a garbage
   * collection.
   */
}

#if !defined(PRODUCT) || ENABLE_TTY_TRACE

const char *OsMisc_jlong_format_specifier() {
  /* 
   * Return jlong-specifier prefixes are used with type characters in
   * printf functions or wprintf functions to specify interpretation
   * of jlong e.g. for win32 is "%I64d", for linux is "%lld"
   */
  return "";
}

const char *OsMisc_julong_format_specifier() {
  /* 
   * Return julong-specifier prefixes are used with type characters in
   * printf functions or wprintf functions to specify interpretation
   * of julong e.g. for win32 is "%I64u", for linux is "%llu"
   */
  return "";
}

#endif // PRODUCT

#if ENABLE_PAGE_PROTECTION
void OsMisc_page_protect() {
  UNIMPLEMENTED();
}

void OsMisc_page_unprotect() {
  UNIMPLEMENTED();
}
#endif // ENABLE_PAGE_PROTECTION

#ifdef __cplusplus
}
#endif
