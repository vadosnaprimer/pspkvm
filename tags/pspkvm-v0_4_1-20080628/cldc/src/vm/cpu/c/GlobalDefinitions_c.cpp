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
#include "incls/_GlobalDefinitions_c.cpp.incl"

#if !CROSS_GENERATOR
/*
 * Note: in cross-generator mode, these symbols are generated using the
 * ARM source assembler. Search for GP_TABLE_OBJ inside build/share/jvm.make.
 */

/// The file defines the "quick access" global variables for C interpreter. 
extern "C" {

unsigned char * _kni_parameter_base;

#if ENABLE_INTERPRETATION_LOG
OopDesc*        _interpretation_log[INTERP_LOG_SIZE];
int             _interpretation_log_idx;
#endif

OopDesc**       _old_generation_end;

address         _current_stack_limit       = NULL;
address         _compiler_stack_limit      = NULL;
int             _rt_timer_ticks            = 0;
address         _primordial_sp             = NULL;
OopDesc*        _interned_string_near_addr = NULL;
OopDesc**       _persistent_handles_addr   = NULL;
int		_jvm_in_quick_native_method= 0;
char*		_jvm_quick_native_exception= NULL;

#if ENABLE_ISOLATES
OopDesc*        _current_task;

// where the address of the being initialized marker is kept for the
// interpreter
OopDesc*         _task_class_init_marker = NULL;
OopDesc*         _task_array_class_init_marker = NULL;
#endif //ENABLE_ISOLATES

#if ENABLE_JAVA_DEBUGGER
int             _debugger_active = 0;
#endif
}

#endif
