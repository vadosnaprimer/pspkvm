#if ENABLE_INTERPRETER_GENERATOR

#ifndef PRODUCT

class SourceMacros: public SourceAssembler {
 public:
  SourceMacros(Stream* output): SourceAssembler(output) {}
};

#endif
#endif
