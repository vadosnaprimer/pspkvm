#if !defined(PRODUCT) || USE_COMPILER_DISASSEMBLER

class Disassembler: public StackObj {
 private:
  Stream* _stream;
 public:
  // creation
  Disassembler(Stream* stream) : _stream(stream) {}
  
  // accessors
  Stream* stream() const { return _stream; }
};

#endif
