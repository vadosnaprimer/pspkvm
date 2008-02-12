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

# include "incls/_precompiled.incl"
# include "incls/_ConstantPoolDesc.cpp.incl"

void ConstantPoolDesc::variable_oops_do(void do_oop(OopDesc**)) {
  // Returns if the tags array has not yet been allocated.
  // This happens if garbage collection kicks in when allocating the
  // tags array.
  if (_tags == NULL) {
    return;
  }

  OopDesc** base = (OopDesc**)((jubyte*)this + header_size());
  jubyte* tags = (jubyte*)_tags + sizeof(ArrayDesc);

  // Note: index 0 may be used during romization to rename non-public symbols.
  for (int i = 0; i < _length; i++) {
    jubyte tag = *tags++;
    if (ConstantTag::is_oop(tag)) {
      GUARANTEE(*base != NULL, "constant pool cannot contain null entries");
      do_oop(base);
    }
    base++;
  }
}
