#include "incls/_precompiled.incl"

#ifndef PRODUCT
#include "incls/_SourceAssembler_mips.cpp.incl"

// Implementation of SourceAssembler::Label

int SourceAssembler::Label::_next_id = 0;
//bool SourceAssembler::_in_glue_code = false;
static int GenerateSDTCode = 0;

void SourceAssembler::Label::import(Stream* s) {
  char *cmd;
  if (GenerateGNUCode) {
    cmd = ".extern";
  } else {
    cmd = "IMPORT";
  }

  GUARANTEE(_state == undefined || _state == imported, "bad label to import");
  if (_state == undefined) {
    s->print("\t%s\t", cmd);
    print_on(s);
    s->cr();
    _state = imported;
  }
}

void SourceAssembler::Label::global(Stream* s) {
  char *cmd;
  if (GenerateGNUCode) {
    cmd = ".global";
  } else {
    cmd = "EXPORT";
  }
  GUARANTEE(_state == undefined, "bad label to make global");
  s->print("\n\t%s\t", cmd);
  print_on(s);
  s->cr();
  _state = exported;
}

void SourceAssembler::Label::make_local(Stream* s) {
  GUARANTEE(_state == undefined, "bad label to make local");
  if (GenerateGNUCode) { 
    _state = internal;
  } else {
    global(s);
  }
}

void SourceAssembler::Label::bind(Stream* s, bool is_global) {
  switch(_state) {
    case undefined:           global(s);                break;
    case anonymous_unused:   /* Fall through */
    case anonymous_used:       _state = anonymous_bound; break;
    case anonymous_bound:     SHOULD_NOT_REACH_HERE();  break;
    default:                                            break; 
  }
  // start a new line if we are not at the beginning
  if (s->position() > 0) {
    s->cr();
  }

  GUARANTEE(s->position() == 0, "wrong label position");
  print_on(s);
  if (GenerateGNUCode) {
    s->print(":");
  } else {
    if (is_global && !GenerateSDTCode) {
      s->print(" PROC");
    }
  }
  s->cr();
}

void SourceAssembler::Label::print_on(Stream* s) const {
  if (is_anonymous()) {
    s->print("L%d", _id);
    if (_state < anonymous_used) {
     ((SourceAssembler::Label*)this)-> _state = anonymous_used;
    }
  } else {
    s->print("%s", _symbol);
  } 
}

SourceAssembler::Label::Label(const char* symbol) {
  if (symbol == NULL) {
    _state  = anonymous_unused;
    _id     = _next_id++;
  } else {
    _state  = undefined;
    _symbol = symbol;
  }
}

SourceAssembler::Label::~Label() {
  // Ideally, all symbolic labels should be defined centrally only once.
  // Then we could also check that a symbolic label has been imported or
  // exported. For now, don't do the check.
  //
  // (The problem right now is that interpreter_call_vm(_redo) really is a
  // 'local' label (not imported or exported) and thus we really should either
  // use an anonymous label or have a 3rd kind of symbolic labels that are
  // local; and than we need to differentiate between export and bind as well).

  GUARANTEE(_state != anonymous_used, "undefined anonymous label");
  if (_state == anonymous_unused) { 
    tty->print("Unused label: ");
    print_on(tty);
    tty->cr();
  }
}

void SourceAssembler::start() {
  // header
  comment("Copyright 2008 PSPKVM Project");
  comment("http://http://sourceforge.net/projects/pspkvm");
  comment("DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER");
  comment("");
  comment("This program is free software; you can redistribute it and/or");
  comment("modify it under the terms of the GNU General Public License version");
  comment("2 only, as published by the Free Software Foundation.");
  comment("");
  comment("This program is distributed in the hope that it will be useful, but");
  comment("WITHOUT ANY WARRANTY; without even the implied warranty of");
  comment("MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU");
  comment("General Public License version 2 for more details (a copy is");
  comment("included at /legal/license.txt).");
  comment("");
  comment("You should have received a copy of the GNU General Public License");
  comment("version 2 along with this work; if not, write to the Free Software");
  comment("Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA");
  comment("02110-1301 USA");
  comment("");

  // make sure people know that this file shouldn't be edited
  comment_section("Generated assembly file -- do *not* edit");


  // FIXME: temp stuff to make target build link
  bind_global("call_on_primordial_stack");
  bind_global("current_thread_to_primordial");
  bind_global("interpreter_deoptimization_entry");
  bind_global("interpreter_fast_method_entry_0");
  bind_global("interpreter_fast_method_entry_1");
  bind_global("interpreter_fast_method_entry_2");
  bind_global("interpreter_fast_method_entry_3");
  bind_global("interpreter_fast_method_entry_4");
  bind_global("interpreter_method_entry");
  bind_global("invoke_pending_entries");
  bind_global("jvm_fast_globals");
  bind_global("native_integer_toString_entry");
  bind_global("native_jvm_unchecked_byte_arraycopy_entry");
  bind_global("native_jvm_unchecked_char_arraycopy_entry");
  bind_global("native_jvm_unchecked_int_arraycopy_entry");
  bind_global("native_jvm_unchecked_long_arraycopy_entry");
  bind_global("native_jvm_unchecked_obj_arraycopy_entry");
  bind_global("native_math_ceil_entry");
  bind_global("native_math_cos_entry");
  bind_global("native_math_floor_entry");
  bind_global("native_math_sin_entry");
  bind_global("native_math_sqrt_entry");
  bind_global("native_math_tan_entry");
  bind_global("native_stringbuffer_append_entry");
  bind_global("native_string_charAt_entry");
  bind_global("native_string_endsWith_entry");
  bind_global("native_string_equals_entry");
  bind_global("native_string_indexof0_entry");
  bind_global("native_string_indexof0_string_entry");
  bind_global("native_string_indexof_entry");
  bind_global("native_string_indexof_string_entry");
  bind_global("native_string_init_entry");
  bind_global("native_string_startsWith0_entry");
  bind_global("native_string_startsWith_entry");
  bind_global("native_string_substringI_entry");
  bind_global("native_string_substringII_entry");
  bind_global("native_system_arraycopy_entry");
  bind_global("native_vector_addElement_entry");
  bind_global("native_vector_elementAt_entry");
  bind_global("primordial_to_current_thread");
  bind_global("quick_int_native_method_entry");
  bind_global("quick_obj_native_method_entry");
  bind_global("quick_void_native_method_entry");
  bind_global("shared_fast_getbyte_accessor");
  bind_global("shared_fast_getbyte_static_accessor");
  bind_global("shared_fast_getchar_accessor");
  bind_global("shared_fast_getchar_static_accessor");
  bind_global("shared_fast_getdouble_accessor");
  bind_global("shared_fast_getdouble_static_accessor");
  bind_global("shared_fast_getfloat_accessor");
  bind_global("shared_fast_getfloat_static_accessor");
  bind_global("shared_fast_getint_accessor");
  bind_global("shared_fast_getint_static_accessor");
  bind_global("shared_fast_getlong_accessor");
  bind_global("shared_fast_getlong_static_accessor");
  bind_global("shared_fast_getshort_accessor");
  bind_global("shared_fast_getshort_static_accessor");
}

void SourceAssembler::stop() {
  if (GenerateGNUCode) {
    stream()->print_cr("\t.end");
  } else { 
    stream()->print_cr("\tEND");
  }
}

void SourceAssembler::comment(const char* fmt, ...) {
  if (!GenerateGPTableOnly) {
    if (jvm_strlen(fmt) == 0) { 
      stream()->cr();
    } else { 
      va_list ap;
      va_start(ap, fmt);
        stream()->print("\t%s ", GenerateGNUCode ? "# " : ";");
        stream()->vprint(fmt, ap);
        stream()->cr();
      va_end(ap);
    }
  }
}

void SourceAssembler::eol_comment(const char* fmt, ...) {
  if (_eol_comment[0] == 0) {
    va_list ap;
    va_start(ap, fmt);
      jvm_vsprintf(_eol_comment, fmt, ap);
    va_end(ap);
  } else { 
    // comments from inner macros shouldn't override already created ones.
  }
}


void SourceAssembler::comment_section(const char* fmt, ...) {
  stream()->cr();
  stream()->cr();
  comment("------------------------------------------------------");
  va_list ap;
  va_start(ap, fmt);
    stream()->print("\t%s ", GenerateGNUCode ? "# " : ";");
    stream()->vprint(fmt, ap);
    stream()->cr();
  va_end(ap);
}

#if 0
void SourceAssembler::align(int alignment) {
  if (alignment > 0) { 
    if (GenerateGNUCode) { 
        stream()->print_cr("\t.align\t%d", jvm_log2(alignment));
    } else { 
        stream()->print_cr("\tALIGN\t%d", alignment);
    }
  }
}
#endif

void SourceAssembler::bind_global(Label& L) {
  //_segment->flush_global();

  L.bind(stream(), true);
  //_segment->set_in_global();
}


#endif
