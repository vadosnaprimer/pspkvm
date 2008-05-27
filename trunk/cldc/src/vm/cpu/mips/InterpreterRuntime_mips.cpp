// These are placeholders, and may be moved into Interpreter_mips.S soon.

#include "incls/_precompiled.incl"
#include "incls/_InterpreterRuntime_mips.cpp.incl"

extern "C" {

//#if !ENABLE_INTERPRETER_GENERATOR
//  JVMFastGlobals  jvm_fast_globals;
//#endif


OopDesc* _newobject(Thread*thread, OopDesc* raw_klass JVM_TRAPS) {
  UsingFastOops fast_oops;
  InstanceClass::Fast klass = raw_klass;
  return klass().new_instance(ErrorOnFailure JVM_NO_CHECK_AT_BOTTOM_0);
}

jint _instanceof(Thread*thread, OopDesc* raw_object_class,      
                 OopDesc* raw_test_class) {
  (void)thread;
  InstanceClass::Raw object_class = raw_object_class;
  InstanceClass::Raw test_class = raw_test_class;

  return (object_class().is_subtype_of(&test_class) ? 1 : 0);
}

OopDesc* _anewarray(Thread *thread, OopDesc* raw_base_klass, int length
                    JVM_TRAPS) {
  UsingFastOops fast_oops;
  // Called from compiled code when it can't do the allocation
#ifdef AZZERT
  JavaFrame frame(thread);
  frame.verify();
#endif
  JavaClass::Fast base_klass = raw_base_klass;
  return Universe::new_obj_array(&base_klass, length JVM_NO_CHECK_AT_BOTTOM_0);
}

address setup_stack_asm(address sp) {
  jint* xsp = (jint*)sp;
  *xsp = TestCompiler;
    xsp += JavaStackDirection;
  *xsp = (jint)force_terminated;
    xsp += JavaStackDirection;
  *xsp = (jint)Thread::finish;
    xsp += JavaStackDirection;
  *xsp = (jint)Thread::lightweight_thread_uncaught_exception;
    xsp += JavaStackDirection;
  *xsp = (jint)Thread::lightweight_thread_exit;
    xsp += JavaStackDirection;
  *xsp = (jint)start_lightweight_thread_asm;
    xsp += JavaStackDirection;
  *xsp = 0;                   // value of frame pointer
  return (address)xsp;
}

}
// extern "C"
