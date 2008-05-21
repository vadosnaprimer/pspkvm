#include "incls/_precompiled.incl"
#include "incls/_InterpreterStubs_mips.cpp.incl"

#if ENABLE_INTERPRETER_GENERATOR

void InterpreterStubs::generate() {
#if 0
  generate_interpreter_throw_exceptions();
#if ENABLE_EMBEDDED_CALLINFO
  if (TaggedJavaStack) { 
    generate_interpreter_fill_in_tags();
  }
#else
  GUARANTEE(!TaggedJavaStack, "Tagged stack not supported");
#endif // ENABLE_EMBEDDED_CALLINFO
  generate_interpreter_deoptimization_entry();
  generate_interpreter_timer_tick();

  generate_primordial_to_current_thread();
  generate_current_thread_to_primordial();
#endif
}


#endif
