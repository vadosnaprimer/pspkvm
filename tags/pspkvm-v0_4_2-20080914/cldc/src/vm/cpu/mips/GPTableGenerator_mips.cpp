#include "incls/_precompiled.incl"

#if ENABLE_INTERPRETER_GENERATOR
#include "incls/_GPTableGenerator_mips.cpp.incl"

void GPTableGenerator::generate_protected_page() {
#if 0
  if (!GenerateGPTableOnly && ENABLE_PAGE_PROTECTION) {
    GUARANTEE(!ENABLE_THUMB_GP_TABLE, "Not supported");
    align(PROTECTED_PAGE_SIZE);
    bind("_protected_page");
    // Fill the page with pointers to gp_base_label to allow ldr gp, [gp, -##]
    Label gp_base_label("gp_base_label");
    for (int i = 0; i < PROTECTED_PAGE_SIZE; i += BytesPerWord) {
      define_long(gp_base_label);
    }
  }
#endif
}

void GPTableGenerator::generate_bytecode_dispatch_table() {
  if (!GenerateGPTableOnly && ENABLE_DISPATCH_TABLE_ALIGNMENT) {
    // This is needed only for real interpreter, not the fake interpreter
    // needed by AOT compiler.
    align(1024);
  }

  int i;
  bind("gp_base_label");
  set_current_commented_offset(0);
  set_use_offset_comments(true);

  for (i = 0; i <= 253; i++) {
    Bytecodes::Code bc = (Bytecodes::Code) i;
    if (Bytecodes::is_defined(bc) && !GenerateGPTableOnly) {
      char buffer[256];
      if (GenerateGNUCode) {
        jvm_sprintf(buffer, "bc_impl_%-30s /* 0x%x */", Bytecodes::name(bc),
                    (int)bc);
      } else {
        jvm_sprintf(buffer, "bc_impl_%-30s ; 0x%x", Bytecodes::name(bc), 
                    (int)bc);
      }
      define_long(buffer);
    } else {
      eol_comment("undefined bytecode 0x%x", bc);
      define_long(0);
    }
  }

  if (ENABLE_DISPATCH_TABLE_PADDING) {
    define_long("bytecode_dispatch_0x0FE");
    define_long("bytecode_dispatch_0x0FF");
    define_long("bytecode_dispatch_0x100");
    define_long("bytecode_dispatch_0x101");
    define_long("bytecode_dispatch_0x102");
    define_long("bytecode_dispatch_0x103");
    define_long("bytecode_dispatch_0x104");
    define_long("bytecode_dispatch_0x105");
  }
  define_long(0);                           // 255 or 262
  define_long(0);                           // 256 or 263
}

void GPTableGenerator::generate_constants_table() {
  int i;

  bind("gp_constants");

  static const GPTemplate gp_templates[] = {
    GP_SYMBOLS_DO(DEFINE_GP_POINTER, DEFINE_GP_VALUE)
    {NULL, 0, 0, 0}
  };

  for (const GPTemplate* tmpl = gp_templates; tmpl->name; tmpl++) {
    if (tmpl->is_pointer) {
      char buff[120];
      jvm_sprintf(buff, "gp_%s_ptr", tmpl->name);
      bind(buff);

      Label L(tmpl->name);
      if (!tmpl->is_asm) {
        import(L);
      }
      define_long(L);
    } else {
      if (jvm_strcmp(tmpl->name, "current_thread") == 0) {
         bind("jvm_fast_globals");
      }
      char buff[120];
      jvm_sprintf(buff, "_%s", tmpl->name);
      bind(buff);
      if (jvm_strcmp(tmpl->name, "bit_selector") == 0) {
        // IMPL_NOTE: create a common framework to define initial values
        define_long(0x80808080);
      } else {
        define_zeros(tmpl->size);
      }
    }
  }

  if (!GenerateGPTableOnly) {
    // Some constants to check we've linked with the right ROM
    Label L1(XSTR(_ROM_LINKCHECK_HLE));
    import(L1);
    define_long(L1);

    Label L2(XSTR(_ROM_LINKCHECK_MFFD));
    import(L2);
    define_long(L2);

    Label L3(XSTR(_ROM_LINKCHECK_MFFL));
    import(L3);
    define_long(L3);
  }

  if (ENABLE_THUMB_GP_TABLE && GenerateGPTableOnly) {
    // These symbols are necessary to link romgen.
    // We define a long for each entry, since they 
    // should be bound to different addresses.
    bind("jvm_ladd");
    define_long(0);
    bind("jvm_lsub");
    define_long(0);
    bind("jvm_land");
    define_long(0);
    bind("jvm_lor");
    define_long(0);
    bind("jvm_lxor");
    define_long(0);
    bind("jvm_lcmp");
    define_long(0);
    bind("jvm_lmin");
    define_long(0);
    bind("jvm_lmax");
    define_long(0);
    bind("jvm_lmul");
    define_long(0);
    bind("jvm_lshl");
    define_long(0);
    bind("jvm_lshr");
    define_long(0);
    bind("jvm_lushr");
    define_long(0);
  }

  bind("gp_constants_end");
}

void GPTableGenerator::start() {
  if (GenerateGNUCode) {
#ifdef SPARC
    stream()->print_cr(".section\t\".data\"");
    stream()->print_cr(".align 4");
#else
    stream()->print_cr("\t.arch i486");
    stream()->print_cr(".data");
#endif
  } else {
    stream()->print_cr("\t.486P");
    stream()->print_cr("\t.MODEL flat, C");
    stream()->print_cr("DataSegment segment  para  public   'DATA'");
  }
}

void GPTableGenerator::stop()  {
  if (GenerateGNUCode) {
    // Nothing to do.
  } else {
    stream()->print_cr("DataSegment ends");
    stream()->print_cr("\tEND");
  }
}

void GPTableGenerator::import(SourceAssembler::Label &l) {
  if (!GenerateGPTableOnly) {
    SourceMacros::import(l);
  } else {
    if (GenerateGNUCode) {
#ifndef SPARC
      stream()->print_cr("\t.extern %s", l.symbol());
#endif
    } else {
      stream()->print_cr("\tEXTERNDEF %s:PROC", l.symbol());
    }
  }
}

void GPTableGenerator::bind(const char *name) {
  if (!GenerateGPTableOnly) {
    SourceMacros::bind(name);
  } else {
    if (GenerateGNUCode) {
      const char *under = AddExternCUnderscore ? "_" : "";
      stream()->print_cr("\t.global\t%s%s", under, name);
      stream()->print_cr("%s%s:", under, name);
    } else {
      stream()->print_cr("\tPUBLIC\t%s", name);
      stream()->print_cr("%s:", name);
    }
  }
}

void GPTableGenerator::define_long(const char *name) {
  if (!GenerateGPTableOnly) {
    SourceMacros::define_long(name);
  } else {
    if (GenerateGNUCode) {
      const char *under = AddExternCUnderscore ? "_" : "";
      stream()->print_cr("\t.long\t%s%s", under, name);
    } else {
      stream()->print_cr("\tEXTERNDEF %s:PROC", name);
      stream()->print_cr("\tDWORD\t%s", name);
      stream()->cr();
    }
  }
}

void GPTableGenerator::define_long(int num) {
  if (!GenerateGPTableOnly) {
    SourceMacros::define_long(num);
  } else {
    if (GenerateGNUCode) {
      stream()->print_cr("\t.long\t0x%x", num);
    } else {
      stream()->print_cr("\tDWORD\t%d", num);
    }
  }
}
void GPTableGenerator::define_long(SourceAssembler::Label &l) {
  if (!GenerateGPTableOnly) {
    SourceMacros::define_long(l);
  } else {
    if (GenerateGNUCode) {
      const char *under = AddExternCUnderscore ? "_" : "";
      stream()->print_cr("\t.long\t%s%s", under, l.symbol());
    } else {
      stream()->print_cr("\tEXTERNDEF %s:PROC", l.symbol());
      stream()->print_cr("\tDWORD\t%s", l.symbol());
    }
  }
}

void GPTableGenerator::align(int alignment) {
  if (!GenerateGPTableOnly) {
    SourceMacros::align(alignment);
  } else {
    if (alignment > 0) { 
      if (GenerateGNUCode) { 
          stream()->print_cr("\t.align\t%d", alignment);
      } else { 
          stream()->print_cr("\tALIGN\t%d", alignment);
      }
    }
  }
}

void GPTableGenerator::define_zeros(int size) {
  if (!GenerateGPTableOnly) {
    SourceMacros::define_zeros(size);
  } else {
    if (GenerateGNUCode) {
#ifdef SPARC
      GUARANTEE((size % 4) == 0, "sanity");
      for (int i=0; i<size; i+=4) {
        stream()->print_cr("\t.long\t0");
      }
#else
      stream()->print_cr("\t%s\t%d", ".space", size);
#endif
    } else {
      // generating the GP table on Win32 for MASM
      if ((size % 4) == 0) {
        for (int i=0; i<size; i+=4) {
          stream()->print_cr("\tDWORD 0; %d bytes", i+4);
        }
      } else {
        // We support only define_zero of bytes divisible by 4!
        UNIMPLEMENTED();
      }
    }
  }
}

void GPTableGenerator::generate() {
  Segment seg(this, data_segment, "Global pool");
  //generate_protected_page();
  generate_bytecode_dispatch_table();
  generate_constants_table();
  set_use_offset_comments(false);
}

#endif // ENABLE_INTERPRETER_GENERATOR
