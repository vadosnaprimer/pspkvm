#if ENABLE_INTERPRETER_GENERATOR

/**
 * GPTableGenerator is used to support AOT-compilation for the ARM platform.
 * Since AOT is not (yet) support on MIPS, this class just has dummy
 * functions on c to make things compile.
 */
class GPTableGenerator {
public:
  GPTableGenerator(Stream* output) {
  }
  void start() {
  }
  void generate() {
  }
  void stop() {
  }
};

#endif
