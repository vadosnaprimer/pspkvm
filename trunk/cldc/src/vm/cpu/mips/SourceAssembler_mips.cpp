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
}

void SourceAssembler::stop() {
#if 0
  if (GenerateGNUCode) {
    stream()->print_cr("\t.end");
  } else { 
    stream()->print_cr("\tEND");
  }
#endif
}

void SourceAssembler::comment(const char* fmt, ...) {
#if 0
  if (!GenerateGPTableOnly) {
    if (jvm_strlen(fmt) == 0) { 
      stream()->cr();
    } else { 
      va_list ap;
      va_start(ap, fmt);
        stream()->print("\t%s ", GenerateGNUCode ? "@ " : ";");
        stream()->vprint(fmt, ap);
        stream()->cr();
      va_end(ap);
    }
  }
#endif
}

void SourceAssembler::eol_comment(const char* fmt, ...) {
#if 0
  if (_eol_comment[0] == 0) {
    va_list ap;
    va_start(ap, fmt);
      jvm_vsprintf(_eol_comment, fmt, ap);
    va_end(ap);
  } else { 
    // comments from inner macros shouldn't override already created ones.
  }
#endif
}


void SourceAssembler::comment_section(const char* fmt, ...) {
#if 0
  stream()->cr();
  stream()->cr();
  comment("------------------------------------------------------");
  va_list ap;
  va_start(ap, fmt);
    stream()->print("\t%s ", GenerateGNUCode ? "@ " : ";");
    stream()->vprint(fmt, ap);
    stream()->cr();
  va_end(ap);
#endif
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


#endif
