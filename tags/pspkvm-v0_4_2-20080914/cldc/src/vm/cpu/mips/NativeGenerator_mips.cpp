#include "incls/_precompiled.incl"

#if ENABLE_INTERPRETER_GENERATOR
#include "incls/_NativeGenerator_mips.cpp.incl"


void NativeGenerator::generate() {
#if 0
  int oldGenerateDebugAssembly = GenerateDebugAssembly;

  GenerateDebugAssembly = false;

  generate_native_math_entries();
  generate_native_string_entries();
  generate_native_system_entries();
  generate_native_thread_entries();
  generate_native_misc_entries();

  GenerateDebugAssembly = oldGenerateDebugAssembly;
#endif
}

#endif

