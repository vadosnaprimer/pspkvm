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

#if ENABLE_INTERPRETER_GENERATOR

class GPTableGenerator: public SourceMacros
{
public:
  void generate_protected_page();
  void generate_bytecode_dispatch_table();
  void generate_constants_table();

private:
  void generate_arm_bytecode_dispatch_table();
  void generate_thumb_bytecode_dispatch_table();

public:
  GPTableGenerator(Stream* output) : SourceMacros(output) {}
  void start();
  void generate();
  void stop();

private:
  void import(Label &l);
  void bind(const char *name);
  void define_long(int num);
  void define_long(const char *name);
  void define_long(Label &l);
  void align(int alignment);
  void define_zeros(int size);
};
#endif // ENABLE_INTERPRETER_GENERATOR
