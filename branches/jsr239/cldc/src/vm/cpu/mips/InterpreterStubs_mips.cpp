#include "incls/_precompiled.incl"
#include "incls/_InterpreterStubs_mips.cpp.incl"

#if ENABLE_INTERPRETER_GENERATOR

void InterpreterStubs::generate() {
  //generate_interpreter_throw_exceptions();

#if 0 // FIXME -- needed for JIT
#if ENABLE_EMBEDDED_CALLINFO
  if (TaggedJavaStack) { 
    generate_interpreter_fill_in_tags();
  }
#else
  GUARANTEE(!TaggedJavaStack, "Tagged stack not supported");
#endif // ENABLE_EMBEDDED_CALLINFO
#endif

  //generate_interpreter_deoptimization_entry();
  //generate_interpreter_timer_tick();

  generate_primordial_to_current_thread();
  //generate_current_thread_to_primordial();
}


void InterpreterStubs::generate_primordial_to_current_thread() {
  //Segment seg(this, code_segment, "Primordial to current thread");

bind_global("primordial_to_current_thread");
  comment("save permanent registers (including return address)");
  push(sp, join(range(s0, s7), range(gp, ra)));
  move(fp, sp);

  comment("Set up global pointer");
  la(jgp, "gp_base_label");

  comment("Get current thread");
  get_current_thread(tmp0);

  comment("Save primordial stack pointer");
  set_primordial_sp(sp);

  comment("Get new stack pointer");
  lw(jsp, tmp0, Thread::stack_pointer_offset());
  
  comment("Go to code");
  lw(tmp1, jsp, -JavaStackDirection * BytesPerWord);
  //FIXME: what does this do? ldr(fp, imm_index(jsp, -JavaStackDirection * 2 * BytesPerWord, post_indexed));
  addiu(jsp, jsp, -JavaStackDirection * 2 * BytesPerWord);
  jr(tmp1);

bind_global("start_lightweight_thread_asm");
  Label testing_compiler; 
  const int SignedBytesPerWord = JavaStackDirection * BytesPerWord;

  stream()->print_cr("\tBREAK");

#if 0
  comment("Set up global pointer");
  ldr_gp_base(gp);
  // jsp       => Thread::lightweight_thread_exit
  // jsp +- 4   => Thread::lightweight_thread_uncaught_exception
  // jsp +- 8   => Thread::finish
  // jsp +- 12  => force_terminated
  // jsp +- 16  => TestCompiler

  comment("Invoke pending entries unless the thread is being terminated");
  get_thread(r0);
  comment("r1 = THREAD->status();");
  ldr(r1, imm_index(r0, Thread::status_offset()));
  mov(r2, zero);
  comment("if ((r1 & THREAD_TERMINATING) != 0) {");
  tst(r1, imm(THREAD_TERMINATING));
  comment("  THREAD->pending_entries = NULL;");
  str(r2, imm_index(r0, Thread::pending_entries_offset()), ne);
  comment("} else {");
  comment("  invoke_pending_entries(THREAD)");
  bl("invoke_pending_entries", eq);
  comment("}");

  comment("if (!TestCompiler) {");
  ldr(r0, imm_index(jsp, -4 * SignedBytesPerWord));
  get_current_pending_exception(r1);
  cmp(r0, zero);
  b(testing_compiler, ne);

  comment("  if (Thread::current_has_pending_exception()) {");
  comment("    call_on_primordial_stack(lightweight_thread_uncaught_exception);");
  comment("  }");
  cmp(r1, zero);
  ldr(r0, imm_index(jsp, -1 * SignedBytesPerWord), ne);
  bl("call_on_primordial_stack",                   ne); 

  comment("  call_on_primordial_stack(finish);");
  ldr(r0, imm_index(jsp, -2 * SignedBytesPerWord));
  bl("call_on_primordial_stack");   

  comment("  invoke_pending_entries(THREAD);");
  get_thread(r0);
  bl("invoke_pending_entries");

  comment("  force_terminated()");
  ldr(r0, imm_index(jsp, -3 * SignedBytesPerWord));  
  bl("call_on_primordial_stack");
  comment("}");  
#if ENABLE_ISOLATES
  comment("Terminate the task if no other threads on it");
  ldr_label(r0, "thread_task_cleanup");
  bl("call_on_primordial_stack");   
  comment("  invoke_pending_entries(THREAD);");
  get_thread(r0);
  bl("invoke_pending_entries");
#endif

bind_global(testing_compiler);
  comment("call_on_primordial_stack(lightweight_thread_exit);");
  ldr(r0, imm_index(jsp));
  bl("call_on_primordial_stack");

  comment("GUARANTEE(Scheduler::_next_runnable_thread == NULL");

  ldr_label(r0, "_next_runnable_thread");
  ldr(r0, imm_index(r0));
  cmp(r0, zero);
  breakpoint(ne);

  comment("current_thread_to_primordial();");
  bl("current_thread_to_primordial_fast");
  breakpoint();
#endif
}


#endif
