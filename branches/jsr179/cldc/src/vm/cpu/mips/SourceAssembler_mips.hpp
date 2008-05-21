
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
  virtual void comment        (const char* fmt, ...);
  void eol_comment    (const char* fmt, ...);
  void comment_section(const char* fmt, ...);
  void set_use_offset_comments(bool value) {
    _use_offset_comments = value;
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

#endif
};
