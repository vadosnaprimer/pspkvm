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

#include "incls/_precompiled.incl"
#include "incls/_Frame_arm.cpp.incl"

#if ENABLE_EMBEDDED_CALLINFO
CallInfo* JavaFrame::raw_call_info( void ) const {
#if !ENABLE_THUMB_COMPILER
  return (CallInfo*) (pc() + callinfo_offset_from_return_address());
#else
  // In thumb mode, return addresses into compiled code have low bit
  // set by default. This is make sure that the callee automatically
  // switches to thumb mode irrespective of its machine state when
  // control returns back to the caller(thumb code)
  unsigned int pc_val = (unsigned int)pc();
  return (CallInfo*) ((pc_val & ~1) + callinfo_offset_from_return_address());
#endif
}
#endif // ENABLE_EMBEDDED_CALLINFO
