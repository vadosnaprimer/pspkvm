#include "incls/_precompiled.incl"

#if ENABLE_INTERPRETER_GENERATOR
#include "incls/_CompilerStubs_mips.cpp.incl"

void CompilerStubs::generate() {
#if 0
#if ENABLE_COMPILER
  generate_compiler_new_obj_array();
  generate_compiler_new_type_array();
  generate_compiler_throw_exceptions();
  generate_compiler_timer_tick();
  generate_compiler_checkcast();
  generate_compiler_instanceof();
  generate_compiler_callvm_stubs();

#if USE_INDIRECT_EXECUTION_SENSOR_UPDATE
  generate_indirect_execution_sensor_update();
#endif // USE_INDIRECT_EXECUTION_SENSOR_UPDATE

#else  // !ENABLE_COMPILER
  Label newobject("_newobject");
  import(newobject);
#endif // ENABLE_COMPILER

  generate_compiler_idiv_irem();
#endif
}

#endif
