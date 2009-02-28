
class Segment;
class SourceAssembler: public Macros {
#ifndef PRODUCT
 protected:
  Stream* _stream;
  Disassembler _disasm;
  char _eol_comment[1024];

 public:
  class Label {
   private:
    static int _next_id;

    enum { anonymous_unused, anonymous_used, anonymous_bound,
           undefined, imported, exported, internal } _state;

    bool is_anonymous() const { return _state <= anonymous_bound; }

    union {
      int         _id;
      const char* _symbol;
    };

    friend class SourceAssembler;
    friend class Literal;

    void import  (Stream* s);
    void make_local(Stream* s);   // should be the default!
    void global  (Stream* s);
    void bind    (Stream* s, bool is_global = false);
    void print_on(Stream* s) const;

   public:
    Label(const char* symbol = NULL);
    ~Label();

   
    int id() const                               { return _id; }
    const char* symbol() const                   { return _symbol; }
  };

  void start();
  void stop();

  // segment control
  enum SegmentType {
    no_segment,
    code_segment,
    data_segment,
    gp_segment,     /* For global pointer info */
    bss_segment     /* Can only contains zeros */
  };
  void beg_segment(Segment *segment, SegmentType segment_type);
  void end_segment();


  virtual void comment        (const char* fmt, ...);
  void eol_comment    (const char* fmt, ...);
  void comment_section(const char* fmt, ...);
  void set_use_offset_comments(bool value) {
    _use_offset_comments = value;
  }
  void set_current_commented_offset(int value) {
    _current_commented_offset = value;
  }

 private:
  // Note: The InterpreterGenerator needs to be a friend only because
  //       of a misdesign that requires the InterpreterGenerator to
  //       mess with the SourceAssembler's state directly. This needs
  //       to be fixed at some point.
  friend class InterpreterGenerator;
  Segment*      _segment;
  SegmentType   _segment_type;
  bool          _use_offset_comments;
  int           _current_commented_offset;

  friend class Segment;

 public:
  SourceAssembler(Stream* stream) :
    _stream(stream), _disasm(stream), _segment(NULL), _segment_type(no_segment)
  {
    _eol_comment[0] = '\0';
    _use_offset_comments = false;
  }

  // support for labels
  void import(Label& L)                { L.import(stream()); }
  void import(const char* name)        { Label L(name); import(L); }
  void global(Label& L)                { L.global(stream()); }
  void make_local(Label& L)            { L.make_local(stream()); }
  void bind  (Label& L)                { L.bind(stream()); }
  void bind_global(Label& L);
  void bind  (const char *name)        { Label L(name); bind(L); }
  void bind_global(const char *name)   { Label L(name); bind_global(L); }
        

 public:
  // accessors
  Stream* stream() const {
    return _stream;
  } 
  Disassembler& disassembler() {
    return _disasm;
  }


public:
  // The instructions for source assembler
  // In the future, these might be moved to Disassembler ...

  int count_range(RegisterRange range);
  void push(Register stk, RegisterRange range);
  void addiu(Register rd, Register rn, int imm);
  void sw(Register rn, Register rd, int offset);
  void lw(Register rn, Register rd, int offset);
  void la(Register rn, char *label);
  void move(Register rd, Register rn);
  void jr(Register rn);

  const char* reg_name(Register reg);

  int  find_gp_offset(const char *name);
  void ldr_using_gp(Register reg, const char *name);
  void str_using_gp(Register reg, const char *name);
  void add_using_gp(Register reg, const char *name);

  void emit_comment_and_cr();

  // directives
  void align(int offset);

  // data definition
  void define_byte(int    x);
  void define_word(int    x);
  void define_long(int    x);
  void define_long(const Label& L);

  void define_bytes(const char* s, bool word_align = true);
  void define_zeros(int size); // zeroed area of (size) bytes

#define DEFINE_GP_FOR_SOURCE(name, size) \
  virtual void get_ ## name(Register reg) {           \
     ldr_using_gp(reg, #name);                        \
  }                                                   \
  virtual void set_ ## name(Register reg) {           \
     str_using_gp(reg, #name);                        \
  }                                                   \
  virtual void get_ ## name ## _addr(Register reg) {  \
     add_using_gp(reg, #name);                        \
  }

  GP_GLOBAL_SYMBOLS_DO(_xx_pointers, DEFINE_GP_FOR_SOURCE)



#endif
};

#ifndef PRODUCT
class Segment: public StackObj {
 private:
  SourceAssembler* _sasm;
  const char *_title;
  bool _in_global;
 public:
  Segment(SourceAssembler* sasm, SourceAssembler::SegmentType segment, 
          const char* title = NULL);
  ~Segment();
  void set_in_global() {
    _in_global = true;
  }
  void flush_global();
};
#endif // PRODUCT
