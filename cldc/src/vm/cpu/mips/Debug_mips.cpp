# include "incls/_precompiled.incl"
# include "incls/_Debug_mips.cpp.incl"

#if !defined(PRODUCT) || ENABLE_TTY_TRACE

extern "C" bool update_java_pointers();
extern "C" void revert_java_pointers();

void ps() {
#if 0
  Thread* current = Thread::current();
  if (current == NULL) {
    tty->print_cr("cannot print stack (no current thread)");
  } else if (current->last_java_frame_exists()) {
    current->trace_stack(tty);
  } else if (update_java_pointers()) {
    current->trace_stack(tty);
    revert_java_pointers();
  } else {
    tty->print_cr("cannot print stack (inconsistent state of the topmost frame)");
  }
#endif
}

#endif
