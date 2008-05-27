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


  stream()->print_cr("\t.text");

#define MIPS_TEMP_STUB(x) \
  bind_global(x); \
  stream()->print_cr("\tBREAK")
  
  // FIXME: temp stuff to make target build link
  MIPS_TEMP_STUB("interpreter_bytecode_counters");
  MIPS_TEMP_STUB("interpreter_pair_counters");
  MIPS_TEMP_STUB("fixed_interpreter_fast_method_entry_0");
  MIPS_TEMP_STUB("fixed_interpreter_fast_method_entry_1");
  MIPS_TEMP_STUB("fixed_interpreter_fast_method_entry_2");
  MIPS_TEMP_STUB("fixed_interpreter_fast_method_entry_3");
  MIPS_TEMP_STUB("fixed_interpreter_fast_method_entry_4");

  MIPS_TEMP_STUB("call_on_primordial_stack");
  MIPS_TEMP_STUB("current_thread_to_primordial");
  MIPS_TEMP_STUB("interpreter_deoptimization_entry");
  MIPS_TEMP_STUB("interpreter_fast_method_entry_0");
  MIPS_TEMP_STUB("interpreter_fast_method_entry_1");
  MIPS_TEMP_STUB("interpreter_fast_method_entry_2");
  MIPS_TEMP_STUB("interpreter_fast_method_entry_3");
  MIPS_TEMP_STUB("interpreter_fast_method_entry_4");
  MIPS_TEMP_STUB("interpreter_method_entry");
  MIPS_TEMP_STUB("invoke_pending_entries");
  MIPS_TEMP_STUB("native_integer_toString_entry");
  MIPS_TEMP_STUB("native_jvm_unchecked_byte_arraycopy_entry");
  MIPS_TEMP_STUB("native_jvm_unchecked_char_arraycopy_entry");
  MIPS_TEMP_STUB("native_jvm_unchecked_int_arraycopy_entry");
  MIPS_TEMP_STUB("native_jvm_unchecked_long_arraycopy_entry");
  MIPS_TEMP_STUB("native_jvm_unchecked_obj_arraycopy_entry");
  MIPS_TEMP_STUB("native_math_ceil_entry");
  MIPS_TEMP_STUB("native_math_cos_entry");
  MIPS_TEMP_STUB("native_math_floor_entry");
  MIPS_TEMP_STUB("native_math_sin_entry");
  MIPS_TEMP_STUB("native_math_sqrt_entry");
  MIPS_TEMP_STUB("native_math_tan_entry");
  MIPS_TEMP_STUB("native_stringbuffer_append_entry");
  MIPS_TEMP_STUB("native_string_charAt_entry");
  MIPS_TEMP_STUB("native_string_endsWith_entry");
  MIPS_TEMP_STUB("native_string_equals_entry");
  MIPS_TEMP_STUB("native_string_indexof0_entry");
  MIPS_TEMP_STUB("native_string_indexof0_string_entry");
  MIPS_TEMP_STUB("native_string_indexof_entry");
  MIPS_TEMP_STUB("native_string_indexof_string_entry");
  MIPS_TEMP_STUB("native_string_init_entry");
  MIPS_TEMP_STUB("native_string_startsWith0_entry");
  MIPS_TEMP_STUB("native_string_startsWith_entry");
  MIPS_TEMP_STUB("native_string_substringI_entry");
  MIPS_TEMP_STUB("native_string_substringII_entry");
  MIPS_TEMP_STUB("native_system_arraycopy_entry");
  MIPS_TEMP_STUB("native_vector_addElement_entry");
  MIPS_TEMP_STUB("native_vector_elementAt_entry");
//MIPS_TEMP_STUB("primordial_to_current_thread");
  MIPS_TEMP_STUB("quick_int_native_method_entry");
  MIPS_TEMP_STUB("quick_obj_native_method_entry");
  MIPS_TEMP_STUB("quick_void_native_method_entry");
  MIPS_TEMP_STUB("shared_fast_getbyte_accessor");
  MIPS_TEMP_STUB("shared_fast_getbyte_static_accessor");
  MIPS_TEMP_STUB("shared_fast_getchar_accessor");
  MIPS_TEMP_STUB("shared_fast_getchar_static_accessor");
  MIPS_TEMP_STUB("shared_fast_getdouble_accessor");
  MIPS_TEMP_STUB("shared_fast_getdouble_static_accessor");
  MIPS_TEMP_STUB("shared_fast_getfloat_accessor");
  MIPS_TEMP_STUB("shared_fast_getfloat_static_accessor");
  MIPS_TEMP_STUB("shared_fast_getint_accessor");
  MIPS_TEMP_STUB("shared_fast_getint_static_accessor");
  MIPS_TEMP_STUB("shared_fast_getlong_accessor");
  MIPS_TEMP_STUB("shared_fast_getlong_static_accessor");
  MIPS_TEMP_STUB("shared_fast_getshort_accessor");
  MIPS_TEMP_STUB("shared_fast_getshort_static_accessor");

  MIPS_TEMP_STUB("bc_impl_nop");
  MIPS_TEMP_STUB("bc_impl_aconst_null");
  MIPS_TEMP_STUB("bc_impl_iconst_m1");
  MIPS_TEMP_STUB("bc_impl_iconst_0");
  MIPS_TEMP_STUB("bc_impl_iconst_1");
  MIPS_TEMP_STUB("bc_impl_iconst_2");
  MIPS_TEMP_STUB("bc_impl_iconst_3");
  MIPS_TEMP_STUB("bc_impl_iconst_4");
  MIPS_TEMP_STUB("bc_impl_iconst_5");
  MIPS_TEMP_STUB("bc_impl_lconst_0");
  MIPS_TEMP_STUB("bc_impl_lconst_1");
  MIPS_TEMP_STUB("bc_impl_fconst_0");
  MIPS_TEMP_STUB("bc_impl_fconst_1");
  MIPS_TEMP_STUB("bc_impl_fconst_2");
  MIPS_TEMP_STUB("bc_impl_dconst_0");
  MIPS_TEMP_STUB("bc_impl_dconst_1");
  MIPS_TEMP_STUB("bc_impl_bipush");
  MIPS_TEMP_STUB("bc_impl_sipush");
  MIPS_TEMP_STUB("bc_impl_ldc");
  MIPS_TEMP_STUB("bc_impl_ldc_w");
  MIPS_TEMP_STUB("bc_impl_ldc2_w");
  MIPS_TEMP_STUB("bc_impl_iload");
  MIPS_TEMP_STUB("bc_impl_lload");
  MIPS_TEMP_STUB("bc_impl_fload");
  MIPS_TEMP_STUB("bc_impl_dload");
  MIPS_TEMP_STUB("bc_impl_aload");
  MIPS_TEMP_STUB("bc_impl_iload_0");
  MIPS_TEMP_STUB("bc_impl_iload_1");
  MIPS_TEMP_STUB("bc_impl_iload_2");
  MIPS_TEMP_STUB("bc_impl_iload_3");
  MIPS_TEMP_STUB("bc_impl_lload_0");
  MIPS_TEMP_STUB("bc_impl_lload_1");
  MIPS_TEMP_STUB("bc_impl_lload_2");
  MIPS_TEMP_STUB("bc_impl_lload_3");
  MIPS_TEMP_STUB("bc_impl_fload_0");
  MIPS_TEMP_STUB("bc_impl_fload_1");
  MIPS_TEMP_STUB("bc_impl_fload_2");
  MIPS_TEMP_STUB("bc_impl_fload_3");
  MIPS_TEMP_STUB("bc_impl_dload_0");
  MIPS_TEMP_STUB("bc_impl_dload_1");
  MIPS_TEMP_STUB("bc_impl_dload_2");
  MIPS_TEMP_STUB("bc_impl_dload_3");
  MIPS_TEMP_STUB("bc_impl_aload_0");
  MIPS_TEMP_STUB("bc_impl_aload_1");
  MIPS_TEMP_STUB("bc_impl_aload_2");
  MIPS_TEMP_STUB("bc_impl_aload_3");
  MIPS_TEMP_STUB("bc_impl_iaload");
  MIPS_TEMP_STUB("bc_impl_laload");
  MIPS_TEMP_STUB("bc_impl_faload");
  MIPS_TEMP_STUB("bc_impl_daload");
  MIPS_TEMP_STUB("bc_impl_aaload");
  MIPS_TEMP_STUB("bc_impl_baload");
  MIPS_TEMP_STUB("bc_impl_caload");
  MIPS_TEMP_STUB("bc_impl_saload");
  MIPS_TEMP_STUB("bc_impl_istore");
  MIPS_TEMP_STUB("bc_impl_lstore");
  MIPS_TEMP_STUB("bc_impl_fstore");
  MIPS_TEMP_STUB("bc_impl_dstore");
  MIPS_TEMP_STUB("bc_impl_astore");
  MIPS_TEMP_STUB("bc_impl_istore_0");
  MIPS_TEMP_STUB("bc_impl_istore_1");
  MIPS_TEMP_STUB("bc_impl_istore_2");
  MIPS_TEMP_STUB("bc_impl_istore_3");
  MIPS_TEMP_STUB("bc_impl_lstore_0");
  MIPS_TEMP_STUB("bc_impl_lstore_1");
  MIPS_TEMP_STUB("bc_impl_lstore_2");
  MIPS_TEMP_STUB("bc_impl_lstore_3");
  MIPS_TEMP_STUB("bc_impl_fstore_0");
  MIPS_TEMP_STUB("bc_impl_fstore_1");
  MIPS_TEMP_STUB("bc_impl_fstore_2");
  MIPS_TEMP_STUB("bc_impl_fstore_3");
  MIPS_TEMP_STUB("bc_impl_dstore_0");
  MIPS_TEMP_STUB("bc_impl_dstore_1");
  MIPS_TEMP_STUB("bc_impl_dstore_2");
  MIPS_TEMP_STUB("bc_impl_dstore_3");
  MIPS_TEMP_STUB("bc_impl_astore_0");
  MIPS_TEMP_STUB("bc_impl_astore_1");
  MIPS_TEMP_STUB("bc_impl_astore_2");
  MIPS_TEMP_STUB("bc_impl_astore_3");
  MIPS_TEMP_STUB("bc_impl_iastore");
  MIPS_TEMP_STUB("bc_impl_lastore");
  MIPS_TEMP_STUB("bc_impl_fastore");
  MIPS_TEMP_STUB("bc_impl_dastore");
  MIPS_TEMP_STUB("bc_impl_aastore");
  MIPS_TEMP_STUB("bc_impl_bastore");
  MIPS_TEMP_STUB("bc_impl_castore");
  MIPS_TEMP_STUB("bc_impl_sastore");
  MIPS_TEMP_STUB("bc_impl_pop");
  MIPS_TEMP_STUB("bc_impl_pop2");
  MIPS_TEMP_STUB("bc_impl_dup");
  MIPS_TEMP_STUB("bc_impl_dup_x1");
  MIPS_TEMP_STUB("bc_impl_dup_x2");
  MIPS_TEMP_STUB("bc_impl_dup2");
  MIPS_TEMP_STUB("bc_impl_dup2_x1");
  MIPS_TEMP_STUB("bc_impl_dup2_x2");
  MIPS_TEMP_STUB("bc_impl_swap");
  MIPS_TEMP_STUB("bc_impl_iadd");
  MIPS_TEMP_STUB("bc_impl_ladd");
  MIPS_TEMP_STUB("bc_impl_fadd");
  MIPS_TEMP_STUB("bc_impl_dadd");
  MIPS_TEMP_STUB("bc_impl_isub");
  MIPS_TEMP_STUB("bc_impl_lsub");
  MIPS_TEMP_STUB("bc_impl_fsub");
  MIPS_TEMP_STUB("bc_impl_dsub");
  MIPS_TEMP_STUB("bc_impl_imul");
  MIPS_TEMP_STUB("bc_impl_lmul");
  MIPS_TEMP_STUB("bc_impl_fmul");
  MIPS_TEMP_STUB("bc_impl_dmul");
  MIPS_TEMP_STUB("bc_impl_idiv");
  MIPS_TEMP_STUB("bc_impl_ldiv");
  MIPS_TEMP_STUB("bc_impl_fdiv");
  MIPS_TEMP_STUB("bc_impl_ddiv");
  MIPS_TEMP_STUB("bc_impl_irem");
  MIPS_TEMP_STUB("bc_impl_lrem");
  MIPS_TEMP_STUB("bc_impl_frem");
  MIPS_TEMP_STUB("bc_impl_drem");
  MIPS_TEMP_STUB("bc_impl_ineg");
  MIPS_TEMP_STUB("bc_impl_lneg");
  MIPS_TEMP_STUB("bc_impl_fneg");
  MIPS_TEMP_STUB("bc_impl_dneg");
  MIPS_TEMP_STUB("bc_impl_ishl");
  MIPS_TEMP_STUB("bc_impl_lshl");
  MIPS_TEMP_STUB("bc_impl_ishr");
  MIPS_TEMP_STUB("bc_impl_lshr");
  MIPS_TEMP_STUB("bc_impl_iushr");
  MIPS_TEMP_STUB("bc_impl_lushr");
  MIPS_TEMP_STUB("bc_impl_iand");
  MIPS_TEMP_STUB("bc_impl_land");
  MIPS_TEMP_STUB("bc_impl_ior");
  MIPS_TEMP_STUB("bc_impl_lor");
  MIPS_TEMP_STUB("bc_impl_ixor");
  MIPS_TEMP_STUB("bc_impl_lxor");
  MIPS_TEMP_STUB("bc_impl_iinc");
  MIPS_TEMP_STUB("bc_impl_i2l");
  MIPS_TEMP_STUB("bc_impl_i2f");
  MIPS_TEMP_STUB("bc_impl_i2d");
  MIPS_TEMP_STUB("bc_impl_l2i");
  MIPS_TEMP_STUB("bc_impl_l2f");
  MIPS_TEMP_STUB("bc_impl_l2d");
  MIPS_TEMP_STUB("bc_impl_f2i");
  MIPS_TEMP_STUB("bc_impl_f2l");
  MIPS_TEMP_STUB("bc_impl_f2d");
  MIPS_TEMP_STUB("bc_impl_d2i");
  MIPS_TEMP_STUB("bc_impl_d2l");
  MIPS_TEMP_STUB("bc_impl_d2f");
  MIPS_TEMP_STUB("bc_impl_i2b");
  MIPS_TEMP_STUB("bc_impl_i2c");
  MIPS_TEMP_STUB("bc_impl_i2s");
  MIPS_TEMP_STUB("bc_impl_lcmp");
  MIPS_TEMP_STUB("bc_impl_fcmpl");
  MIPS_TEMP_STUB("bc_impl_fcmpg");
  MIPS_TEMP_STUB("bc_impl_dcmpl");
  MIPS_TEMP_STUB("bc_impl_dcmpg");
  MIPS_TEMP_STUB("bc_impl_ifeq");
  MIPS_TEMP_STUB("bc_impl_ifne");
  MIPS_TEMP_STUB("bc_impl_iflt");
  MIPS_TEMP_STUB("bc_impl_ifge");
  MIPS_TEMP_STUB("bc_impl_ifgt");
  MIPS_TEMP_STUB("bc_impl_ifle");
  MIPS_TEMP_STUB("bc_impl_if_icmpeq");
  MIPS_TEMP_STUB("bc_impl_if_icmpne");
  MIPS_TEMP_STUB("bc_impl_if_icmplt");
  MIPS_TEMP_STUB("bc_impl_if_icmpge");
  MIPS_TEMP_STUB("bc_impl_if_icmpgt");
  MIPS_TEMP_STUB("bc_impl_if_icmple");
  MIPS_TEMP_STUB("bc_impl_if_acmpeq");
  MIPS_TEMP_STUB("bc_impl_if_acmpne");
  MIPS_TEMP_STUB("bc_impl_goto");
  MIPS_TEMP_STUB("bc_impl_tableswitch");
  MIPS_TEMP_STUB("bc_impl_lookupswitch");
  MIPS_TEMP_STUB("bc_impl_ireturn");
  MIPS_TEMP_STUB("bc_impl_lreturn");
  MIPS_TEMP_STUB("bc_impl_freturn");
  MIPS_TEMP_STUB("bc_impl_dreturn");
  MIPS_TEMP_STUB("bc_impl_areturn");
  MIPS_TEMP_STUB("bc_impl_return");
  MIPS_TEMP_STUB("bc_impl_getstatic");
  MIPS_TEMP_STUB("bc_impl_putstatic");
  MIPS_TEMP_STUB("bc_impl_getfield");
  MIPS_TEMP_STUB("bc_impl_putfield");
  MIPS_TEMP_STUB("bc_impl_invokevirtual");
  MIPS_TEMP_STUB("bc_impl_invokespecial");
  MIPS_TEMP_STUB("bc_impl_invokestatic");
  MIPS_TEMP_STUB("bc_impl_invokeinterface");
  MIPS_TEMP_STUB("bc_impl_new");
  MIPS_TEMP_STUB("bc_impl_newarray");
  MIPS_TEMP_STUB("bc_impl_anewarray");
  MIPS_TEMP_STUB("bc_impl_arraylength");
  MIPS_TEMP_STUB("bc_impl_athrow");
  MIPS_TEMP_STUB("bc_impl_checkcast");
  MIPS_TEMP_STUB("bc_impl_instanceof");
  MIPS_TEMP_STUB("bc_impl_monitorenter");
  MIPS_TEMP_STUB("bc_impl_monitorexit");
  MIPS_TEMP_STUB("bc_impl_wide");
  MIPS_TEMP_STUB("bc_impl_multianewarray");
  MIPS_TEMP_STUB("bc_impl_ifnull");
  MIPS_TEMP_STUB("bc_impl_ifnonnull");
  MIPS_TEMP_STUB("bc_impl_goto_w");
  MIPS_TEMP_STUB("bc_impl_breakpoint");
  MIPS_TEMP_STUB("bc_impl_fast_1_ldc");
  MIPS_TEMP_STUB("bc_impl_fast_1_ldc_w");
  MIPS_TEMP_STUB("bc_impl_fast_2_ldc_w");
  MIPS_TEMP_STUB("bc_impl_fast_1_putstatic");
  MIPS_TEMP_STUB("bc_impl_fast_2_putstatic");
  MIPS_TEMP_STUB("bc_impl_fast_a_putstatic");
  MIPS_TEMP_STUB("bc_impl_fast_1_getstatic");
  MIPS_TEMP_STUB("bc_impl_fast_2_getstatic");
  MIPS_TEMP_STUB("bc_impl_fast_bputfield");
  MIPS_TEMP_STUB("bc_impl_fast_sputfield");
  MIPS_TEMP_STUB("bc_impl_fast_iputfield");
  MIPS_TEMP_STUB("bc_impl_fast_lputfield");
  MIPS_TEMP_STUB("bc_impl_fast_fputfield");
  MIPS_TEMP_STUB("bc_impl_fast_dputfield");
  MIPS_TEMP_STUB("bc_impl_fast_aputfield");
  MIPS_TEMP_STUB("bc_impl_fast_bgetfield");
  MIPS_TEMP_STUB("bc_impl_fast_sgetfield");
  MIPS_TEMP_STUB("bc_impl_fast_igetfield");
  MIPS_TEMP_STUB("bc_impl_fast_lgetfield");
  MIPS_TEMP_STUB("bc_impl_fast_fgetfield");
  MIPS_TEMP_STUB("bc_impl_fast_dgetfield");
  MIPS_TEMP_STUB("bc_impl_fast_agetfield");
  MIPS_TEMP_STUB("bc_impl_fast_cgetfield");
  MIPS_TEMP_STUB("bc_impl_fast_invokevirtual");
  MIPS_TEMP_STUB("bc_impl_fast_invokestatic");
  MIPS_TEMP_STUB("bc_impl_fast_invokeinterface");
  MIPS_TEMP_STUB("bc_impl_fast_invokenative");
  MIPS_TEMP_STUB("bc_impl_fast_invokevirtual_final");
  MIPS_TEMP_STUB("bc_impl_fast_invokespecial");
  MIPS_TEMP_STUB("bc_impl_fast_new");
  MIPS_TEMP_STUB("bc_impl_fast_anewarray");
  MIPS_TEMP_STUB("bc_impl_fast_checkcast");
  MIPS_TEMP_STUB("bc_impl_fast_instanceof");
  MIPS_TEMP_STUB("bc_impl_fast_igetfield_1");
  MIPS_TEMP_STUB("bc_impl_fast_agetfield_1");
  MIPS_TEMP_STUB("bc_impl_aload_0_fast_agetfield_1");
  MIPS_TEMP_STUB("bc_impl_aload_0_fast_igetfield_1");
  MIPS_TEMP_STUB("bc_impl_pop_and_npe_if_null");
  MIPS_TEMP_STUB("bc_impl_init_static_array");
  MIPS_TEMP_STUB("bc_impl_aload_0_fast_agetfield_4");
  MIPS_TEMP_STUB("bc_impl_aload_0_fast_igetfield_4");
  MIPS_TEMP_STUB("bc_impl_aload_0_fast_agetfield_8");
  MIPS_TEMP_STUB("bc_impl_aload_0_fast_igetfield_8");
  MIPS_TEMP_STUB("bc_impl_fast_init_1_putstatic");
  MIPS_TEMP_STUB("bc_impl_fast_init_2_putstatic");
  MIPS_TEMP_STUB("bc_impl_fast_init_a_putstatic");
  MIPS_TEMP_STUB("bc_impl_fast_init_1_getstatic");
  MIPS_TEMP_STUB("bc_impl_fast_init_2_getstatic");
  MIPS_TEMP_STUB("bc_impl_fast_init_invokestatic");
  MIPS_TEMP_STUB("bc_impl_fast_init_new");
}

void SourceAssembler::stop() {
  if (GenerateGNUCode) {
    stream()->print_cr("\t.end");
  } else { 
    stream()->print_cr("\tEND");
  }
}

void SourceAssembler::beg_segment(Segment *segment, SegmentType segment_type) {
  GUARANTEE(_segment == NULL, "no nested segments");
  _segment = segment;
  int segment_number = 1;

  GUARANTEE(segment_type != no_segment, "must specify segment");
  if (_segment_type != segment_type) {
    if (!GenerateSDTCode && !GenerateGNUCode && !GenerateMicrosoftCode) {
      // Generating code for ADS or RVCT. Need to add PRESERVE8
      // if we're building for RVCT 2.0 or later
      stream()->print_cr("\tIF {ARMASM_VERSION} >= 200000");
      stream()->print_cr("\tPRESERVE8");
      stream()->print_cr("\tENDIF");
    }
    _segment_type = segment_type;   // Since this controls the stream() below
    switch (segment_type) {
      case code_segment:
        if (GenerateSDTCode) {
          stream()->print_cr("\tAREA |.text%d|, CODE", segment_number++);
        } else {
          stream()->print_cr(GenerateGNUCode? ".text":"\tAREA |.text|, CODE");
        }
        break;

      case data_segment:
        if (GenerateSDTCode) {
          stream()->print_cr("\tAREA |.data%d|, DATA", segment_number++);
        } else if (GenerateGNUCode) {
          stream()->print_cr(".data");
        } else {
          stream()->print_cr("\tAREA |.data|, DATA");
        }
        break;

      case bss_segment:
        if (GenerateSDTCode) {
          stream()->print_cr("\tAREA |.data%d|, DATA", segment_number++);
        } else {
          stream()->print_cr(GenerateGNUCode ? ".bss":"\tAREA |.data|, DATA");
        }
        break;

      case gp_segment:
        break;

      default:
        SHOULD_NOT_REACH_HERE();
    }
  }
}

void SourceAssembler::end_segment() {
  if (_segment_type == code_segment) { 
    //FIXME:PSP _literals.empty(this);
  } else {
    // We should guarantee that no literals were created. . .
    if (_segment_type == data_segment) {
    }
  }
  _segment = NULL;
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

int SourceAssembler::find_gp_offset(const char *name) {
#if !ENABLE_THUMB_GP_TABLE
  int offset = 256 * sizeof(OopDesc*); // skip the bytecode table
  if (ENABLE_DISPATCH_TABLE_PADDING) {
    offset += 8 * sizeof(OopDesc*);    // 8 extra bytecodes.
  }
#else
  int offset = 1 * sizeof(OopDesc*); // skip the nop bytecode
#endif

  static const GPTemplate gp_templates[] = {
    GP_SYMBOLS_DO(DEFINE_GP_POINTER, DEFINE_GP_VALUE)
    {NULL, 0, 0, 0}
  };

  for (const GPTemplate* tmpl = gp_templates; tmpl->name; tmpl++) {
    if (jvm_strcmp(name, tmpl->name) == 0) {
      return offset;
    }
    offset += tmpl->size;
    GUARANTEE((offset % 4) == 0, "must be word aligned");
  }

  SHOULD_NOT_REACH_HERE();
  return 0;
}

void SourceAssembler::ldr_using_gp(Register reg, const char *name) {
  int offset = find_gp_offset(name);
  lw(reg, jgp, offset);
}                  

void SourceAssembler::str_using_gp(Register reg, const char *name) {
  int offset = find_gp_offset(name);
  sw(reg, jgp, offset);
}                  

void SourceAssembler::add_using_gp(Register reg, const char *name) {
  UNIMPLEMENTED();
#if 0
  int offset = find_gp_offset(name);
  if (is_rotated_imm(offset)) {
    eol_comment(name);
    add(reg, gp, imm(offset), cond);
  } else {
    char buff[128];
    jvm_sprintf(buff, "slow add_gp_imm %s %d", name, offset);
    eol_comment(buff);
    offset -= 1024;
    GUARANTEE(is_rotated_imm(1024), "sanity");
    GUARANTEE(is_rotated_imm(offset), "sanity");
    add(reg, gp, imm(1024), cond);
    add(reg, reg, imm(offset), cond);
  }
#endif
}                  

void SourceAssembler::bind_global(Label& L) {
  //_segment->flush_global();

  L.bind(stream(), true);
  //_segment->set_in_global();
}

int SourceAssembler::count_range(RegisterRange range) {
  int n = 0;
  for (int i=0; i<32; i++) {
    if (range & (1 << i)) {
      n++;
    }
  }
  return n;
}

void SourceAssembler::push(Register stk, RegisterRange range) {
  int n = 0;
  addiu(sp, sp, -count_range(range) * 4);
  for (int i=0; i<32; i++) {
    if (range & (1 << i)) {
      sw((Register)i, sp, n); n+= 4;
    }
  }
}

void SourceAssembler::addiu(Assembler::Register rd, Assembler::Register rn,
			    int imm) {
  stream()->print_cr("\taddiu\t%s, %s, %d", reg_name(rd), reg_name(rn), imm);
}

void SourceAssembler::sw(Assembler::Register rn,
			 Assembler::Register rd, int offset) {
  stream()->print_cr("\tsw\t%s, %d(%s)", reg_name(rn), offset, reg_name(rd));
}

void SourceAssembler::lw(Assembler::Register rn,
			 Assembler::Register rd, int offset) {
  stream()->print_cr("\tlw\t%s, %d(%s)", reg_name(rn), offset, reg_name(rd));
}

void SourceAssembler::la(Assembler::Register rn, char *label) {
  stream()->print_cr("\tla\t%s, %s", reg_name(rn), label);
}
void SourceAssembler::move(Assembler::Register rd, Assembler::Register rn) {
  stream()->print_cr("\tmove\t%s, %s", reg_name(rd), reg_name(rn));
}


void SourceAssembler::jr(Assembler::Register r) {
  stream()->print_cr("\tjr\t%s", reg_name(r));
}

void SourceAssembler::emit_comment_and_cr() {
  if (!GenerateGPTableOnly) {
    if (_eol_comment[0] != 0 || _use_offset_comments) {
      stream()->print(" ");
      while (stream()->position() <= 40) {
        stream()->print(" ");
      }
      stream()->print("%c", (GenerateGNUCode ? '#' : ';'));
    }
    if (_eol_comment[0] != 0) {
      stream()->print(" %s", _eol_comment);
    }
    if (_use_offset_comments) {
      stream()->print(" offset=%d (0x%x)",
                      _current_commented_offset, _current_commented_offset);
    }
    _eol_comment[0] = 0;
  }
  stream()->cr();
}


void SourceAssembler::align(int alignment) {
  if (alignment > 0) { 
    if (GenerateGNUCode) { 
        stream()->print_cr("\t.align\t%d", jvm_log2(alignment));
    } else { 
        stream()->print_cr("\tALIGN\t%d", alignment);
    }
  }
}
void SourceAssembler::define_byte(int x) {
  stream()->print("\t%s\t0x%x", (GenerateGNUCode ? ".byte" : "DCB"), x);
  emit_comment_and_cr();
  _current_commented_offset += x;
}

void SourceAssembler::define_long(int x) {
  char *tag, *spec = "0x%x";
  if (GenerateGNUCode) {
    tag = ".long";
  } else {
    tag = "DCD";
  }
  stream()->print("\t%s\t", tag);
  stream()->print(spec, x);
  emit_comment_and_cr();
  _current_commented_offset += sizeof(jint);
}

void SourceAssembler::define_long(const Label& L) {
  char *tag;
  if (GenerateGNUCode) {
    tag = ".long";
  } else {
    tag = "DCD";
  }
  stream()->print("\t%s\t", tag);
  L.print_on(stream()); 
  emit_comment_and_cr();
  _current_commented_offset += sizeof(jint);
}

void SourceAssembler::define_bytes(const char* s, bool word_align) {
  GUARANTEE(s != NULL, "string must exist");
  int d = word_align ? (-(int)(jvm_strlen(s) + 1) & 3) : 0;
  int numbytes = (int)jvm_strlen(s) + d;
  if (GenerateGNUCode) { 
    stream()->print("\t.ascii\t\"%s\\0", s);
    while (d-- > 0) stream()->print("\\0");
    stream()->print("\"");
  } else {
    stream()->print("\tDCB\t\"%s\", 0", s);
    while (d-- > 0) stream()->print(", 0");
  }
  emit_comment_and_cr();
  _current_commented_offset += numbytes;
}

void SourceAssembler::define_zeros(int size) {
  stream()->print("\t%s\t%d", (GenerateGNUCode ? ".space" : "%"), size);
  emit_comment_and_cr();
  _current_commented_offset += size;
}



const char* SourceAssembler::reg_name(Assembler::Register reg) {
  static const char* const reg_names[] = {
#if 0
    "$zero", "$at",
    "$v0", "$v1",
    "$a0", "$a1", "$a2", "$a3",
    "$t0", "$t1", "$t2", "$t3", "$t4", "$t5", "$t6", "$t7",
    "$s0", "$s1", "$s2", "$s3", "$s4", "$s5", "$s6", "$s7",
    "$t8", "$t9",
    "$k0", "$k1",
    "$gp",
    "$sp",
    "$fp",
    "$ra"
#else
    "$0",
    "$1",
    "$2",
    "$3",
    "$4",
    "$5",
    "$6",
    "$7",
    "$8",
    "$9",
    "$10",
    "$11",
    "$12",
    "$13",
    "$14",
    "$15",
    "$16",
    "$17",
    "$18",
    "$19",
    "$20",
    "$21",
    "$22",
    "$23",
    "$24",
    "$25",
    "$26",
    "$27",
    "$gp",
    "$sp",
    "$fp",
    "$31",
#endif
  };
  GUARANTEE( unsigned(reg) < unsigned(sizeof reg_names/sizeof(reg_names[0])),
             "Invalid register" );
  return reg_names[reg];
}


// Implementation of Segment

Segment::Segment(SourceAssembler* sasm,
                 SourceAssembler::SegmentType segment_type,
                 const char* title)
  : _sasm(sasm), _title(title), _in_global(false)
{
#if 0
  // FIXME:PSP
  if (SourceAssembler::in_glue_code()) {
    // If glue code contains a data segment, it makes it hard to relocate 
    // the code segment to the Java heap. However, it's OK for the glue code
    // to use "ldr reg, =xxx" to refer to a variable in the data segment.
    GUARANTEE(segment_type == SourceAssembler::code_segment,
              "glue code cannot contain data");
  }
#endif

  if (GenerateGPTableOnly) {
    return;
  }
  sasm->beg_segment(this, segment_type);
  sasm->comment("------------------------------------------------------");
  if (title) {
    sasm->comment("Start segment: %s", title);
  } else {
    sasm->comment("Start segment:  unnamed");
  }
}

void Segment::flush_global() {
  if (GenerateGPTableOnly) {
    return;
  }
  if (_in_global) {
    _in_global = false;
    if (!GenerateGNUCode && !GenerateSDTCode) {
      _sasm->stream()->print_cr("\n\tENDP");
    }
  }
}

Segment::~Segment() {
  if (GenerateGPTableOnly) {
    return;
  }
  _sasm->end_segment();
  flush_global();
  if (_title) {
    _sasm->comment("End segment: %s", _title);
  } else {
    _sasm->comment("End segment: unnamed");
  }
  _sasm->stream()->cr();
  _sasm->stream()->cr();
}

#endif

