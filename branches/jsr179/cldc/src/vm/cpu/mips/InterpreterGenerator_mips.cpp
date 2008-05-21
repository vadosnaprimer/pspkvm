#include "incls/_precompiled.incl"
#include "incls/_InterpreterGenerator_mips.cpp.incl"

#if ENABLE_INTERPRETER_GENERATOR

void InterpreterGenerator::generate() {
#if 0
  generate_interpreter_signature();
  // generate_test_code();
  generate_interpreter_method_entry();
  for (int i = 0; i < 5; i++) {
    generate_interpreter_fast_method_entry(i);
  }
  generate_quick_native_method_entry(T_VOID);
  generate_quick_native_method_entry(T_INT);
  generate_quick_native_method_entry(T_OBJECT);

#if GENERATE_LIBC_GLUE
  if (GenerateGNUCode) {
    // These are used only for GCC/linux/Thumb
    generate_libc_glue();
    generate_fast_routines();
  }
#endif

  if (ENABLE_FAST_MEM_ROUTINES) {
    generate_fast_memroutines();
  }

  generate_interpreter_grow_stack();
  generate_interpreter_bytecode_templates();

  generate_interpreter_dispatch_table();

  generate_interpreter_bytecode_counters_table();
  generate_interpreter_pair_counters_table();
#endif
}

void InterpreterGenerator::generate_interpreter_signature() {
#if 0
  Segment seg(this, code_segment, "Interpreter signature area");
  comment_section("Interpreter signature");
  if (GenerateDebugAssembly) {
    comment("This is (never executed) code that uses data only "
            "available in debug builds.");
    static Label please("please_use_optimized_interpreter_with_release_"
                        "or_product_builds");
    import(please);
    define_long(please);
  }
  // Create the constant 1 to ind
  bind("assembler_loop_type");
  define_long(AssemblerLoopFlags::get_flags());
#endif
}

#define PRINT_REGISTER(x) \
    tty->print_cr(((x<10) ? "    r%d   %s" : "    r%d  %s"), x, STR(x))

void InterpreterGenerator::print_register_definitions() {
#if 0
  PRINT_REGISTER(gp);
  PRINT_REGISTER(cpool);
  PRINT_REGISTER(locals);
  PRINT_REGISTER(fp);
  PRINT_REGISTER(sp);
  PRINT_REGISTER(jsp);
  PRINT_REGISTER(lr);
  PRINT_REGISTER(pc);

  PRINT_REGISTER(method_return_type);
  PRINT_REGISTER(return_register);
  PRINT_REGISTER(stack_lock_register);

  PRINT_REGISTER(sbz);
  PRINT_REGISTER(sbo);

  PRINT_REGISTER(tos_val);
  PRINT_REGISTER(tos_tag);

  PRINT_REGISTER(tmp0);
  PRINT_REGISTER(tmp1);
  PRINT_REGISTER(tmp2);
  PRINT_REGISTER(tmp3);
  PRINT_REGISTER(tmp4);
  PRINT_REGISTER(tmp5);

  PRINT_REGISTER(bcode);
  PRINT_REGISTER(bcp);

  PRINT_REGISTER(first_register);
  PRINT_REGISTER(last_register);
#endif
}


#endif
