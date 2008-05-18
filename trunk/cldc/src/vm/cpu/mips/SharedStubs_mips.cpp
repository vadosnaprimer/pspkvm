#include "incls/_precompiled.incl"

#if ENABLE_INTERPRETER_GENERATOR
#include "incls/_SharedStubs_mips.cpp.incl"

void SharedStubs::generate() {
#if 0
  Label shared_entry_return_point("shared_entry_return_point");
  make_local(shared_entry_return_point);
  generate_shared_entry(shared_entry_return_point);
  generate_shared_invoke_compiler();
#if ENABLE_JAVA_DEBUGGER
  generate_shared_invoke_debug();
#endif
  generate_shared_fast_accessors();

  generate_shared_call_vm(shared_entry_return_point, T_VOID);
  generate_shared_call_vm(shared_entry_return_point, T_OBJECT);
  generate_shared_call_vm(shared_entry_return_point, T_ILLEGAL);

  generate_shared_monitor_enter();
  generate_shared_monitor_exit();

  generate_invoke_pending_entries();
  generate_call_on_primordial_stack();

#if ENABLE_METHOD_TRAPS
  generate_cautious_invoke();
#endif

  generate_fast_memclear();
  generate_brute_force_icache_flush();
#endif
}
#endif
