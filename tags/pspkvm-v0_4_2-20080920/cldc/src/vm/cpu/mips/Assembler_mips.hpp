class Assembler: public GlobalObj {
 public:
  enum Condition {
    foobar = 0,
  };
  enum Register {
    zero  =  0,
    at    =  1,
    v0    =  2,
    v1    =  3,
    a0    =  4,
    a1    =  5,
    a2    =  6,
    a3    =  7,
    t0    =  8,
    t1    =  9,
    t2    = 10,
    t3    = 11,
    t4    = 12,
    t5    = 13,
    t6    = 14,
    t7    = 15,
    s0    = 16,
    s1    = 17,
    s2    = 18,
    s3    = 19,
    s4    = 20,
    s5    = 21,
    s6    = 22,
    s7    = 23,
    t8    = 24,
    t9    = 25,
    k0    = 26,
    k1    = 27,
    gp    = 28,
    sp    = 29,
    fp    = 30,
    ra    = 31,
    number_of_registers = 32,

    // Registers used by the Interpreter
    callee  =  a0,  // stores MethodDesc on method entry
    tmp0    =  t0,
    tmp1    =  t1,
    tmp2    =  t2,
    tmp3    =  t3,
    tmp4    =  t4,
    tmp5    =  t5,
    jgp     =  s0,   // Java global pointer
    jsp     =  s1,   // Java stack pointer
    locals  =  s2,   // Points to locals on an interpreter frame
    cpool   =  s3,   // Points to constants (when in an interpreter method)
    bcode   =  s4,   // The current bytecode to be interpreted.
    bcp     =  s5,   // Points to the current bytecode to be interpreted.
    tos_val =  s6,   // Top of stack value (cached in register)
  };
  enum RegisterRange {
    dummy,
  };

  static RegisterRange range(Register beg, Register end) {
    // Need to use julong as range(.., ra) may temporarily overflow 32-bit.
    GUARANTEE(beg <= end, "illegal range");
    return (RegisterRange)
      (((julong(1) << (end + 1)) - (julong(1) << beg)) & 0xffffffff);
  }
  static RegisterRange join(RegisterRange a, RegisterRange b) {
    return (RegisterRange)(a | b);
  }
};

// GP_ASM: the entry in the GP table is defined in the assembler loop
// GP_C:   the entry in the GP table is defined in C code.
#define GP_ASM 1
#define GP_C   2

#define FOR_ALL_ISOLATE_GP_GLOBALS(value)
#define FOR_ALL_DEBUGGER_GP_GLOBALS(value)

#define DECLARE_FAST_GLOBAL_FOR_MIPS(func, type, name) \
     func(name, sizeof(OopDesc*))

#define GP_GLOBAL_SYMBOLS_DO_PART1(pointer, value) \
     value(current_stack_limit,           sizeof(OopDesc*))                   \
     value(compiler_stack_limit,          sizeof(OopDesc*))                   \
     value(rt_timer_ticks,                sizeof(int))

#define GP_GLOBAL_SYMBOLS_DO_PART2(pointer, value) \
     FORALL_JVM_FAST_GLOBALS(DECLARE_FAST_GLOBAL_FOR_MIPS, value) \
     \
     value(interpretation_log,            sizeof(OopDesc*) * INTERP_LOG_SIZE) \
     value(interpretation_log_idx,        sizeof(int))                        \
     value(primordial_sp,                 sizeof(OopDesc*))                   \
     value(old_generation_end,            sizeof(OopDesc*))                   \
     value(bytecode_counter,              sizeof(OopDesc*))                   \
     value(kni_parameter_base,            sizeof(OopDesc*))                   \
     value(rom_constant_pool_fast,        sizeof(int))                        \
     value(gp_bytecode_counter,           sizeof(int) )                       \
     value(jvm_in_quick_native_method,    sizeof(int) )                       \
     value(jvm_quick_native_exception,    sizeof(int) )                       \
     value(method_execution_sensor,       method_execution_sensor_size)       \
     value(interned_string_near_addr,     sizeof(OopDesc*))                   \
     value(persistent_handles_addr,       sizeof(OopDesc*))                   \
     FOR_ALL_ISOLATE_GP_GLOBALS(value)                                        \
     FOR_ALL_DEBUGGER_GP_GLOBALS(value)

#define GP_GLOBAL_SYMBOLS_DO(pointer, value) \
        GP_GLOBAL_SYMBOLS_DO_PART1(pointer, value) \
        GP_GLOBAL_SYMBOLS_DO_PART2(pointer, value)

#define GP_SYMBOLS_DO(pointer, value) \
        GP_GLOBAL_SYMBOLS_DO(pointer, value)


struct GPTemplate {
  const char *name;
  bool  is_pointer;
  bool  is_asm;
  int   size;
};

#define DEFINE_GP_POINTER(type, name) {STR(name), 1, (type == GP_ASM), 4},
#define DEFINE_GP_VALUE(name, size)   {STR(name), 0, 0, size},

#define DECLARE_GP_PTR_FOR_POINTER(type, name) \
        extern address gp_ ## name ## _ptr;
#define DECLARE_GP_PTR_FOR_VALUE(type, name)

extern "C" {
  extern address gp_base_label;
  GP_SYMBOLS_DO(DECLARE_GP_PTR_FOR_POINTER, DECLARE_GP_PTR_FOR_VALUE)
}



class Macros: public Assembler {

};
