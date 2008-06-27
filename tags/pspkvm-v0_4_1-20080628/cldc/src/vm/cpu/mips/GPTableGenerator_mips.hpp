#if ENABLE_INTERPRETER_GENERATOR

/**
 * GPTableGenerator is used to support AOT-compilation for the ARM platform.
 * Since AOT is not (yet) support on MIPS, this class just has dummy
 * functions on c to make things compile.
 */
class GPTableGenerator: public SourceMacros
{
public:
  void generate_protected_page();
  void generate_bytecode_dispatch_table();
  void generate_constants_table();

public:
  GPTableGenerator(Stream* output) : SourceMacros(output) {}
  void start();
  void generate();
  void stop();

private:
  void import(Label &l);
  void bind(const char *name);
  void define_long(int num);
  void define_long(const char *name);
  void define_long(Label &l);
  void align(int alignment);
  void define_zeros(int size);
};

#endif
