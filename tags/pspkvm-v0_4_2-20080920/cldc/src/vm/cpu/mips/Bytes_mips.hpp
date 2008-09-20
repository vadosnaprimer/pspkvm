/*
 *   
 *
 * Copyright  1990-2007 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, CA 95054 or visit www.sun.com if you need additional
 * information or have any questions.
 */

class Bytes : public AllStatic {

 public:
  // Returns true if the byte ordering used by Java is different from the
  // native byte ordering of the underlying machine. For example, this is
  // true for Intel x86, but false for Solaris on Sparc.

  static inline bool is_Java_byte_ordering_different(){ 
    return HARDWARE_LITTLE_ENDIAN != 0; 
  }

  // Efficient reading and writing of unaligned unsigned data in
  // platform-specific byte ordering 
  // (no special code is needed since x86 CPUs can access unaligned data)

  static inline jushort get_Java_u2(const_address p){ 
      return (jushort) (jushort(p[0] << 8) | ( jushort(p[1]) ));
  }

  static inline juint get_Java_u4(const_address p) {
      return juint(p[0] << 24) | (juint(p[1]) << 16) | (juint(p[2]) << 8) |
            (juint(p[3]));
  }

  static inline julong get_Java_u8(const_address p) {
      return ((julong)get_Java_u4(p) << 32 | (julong)get_Java_u4(p + 4));
  }

  static inline jushort get_native_u2(const_address p){ 
    if (HARDWARE_LITTLE_ENDIAN) { 
      return (jushort) (jushort(p[0]) | ( jushort(p[1]) << 8 ));
    } else { 
      return get_Java_u2(p);
    }
  }

  static inline juint get_native_u4(const_address p) {
    if (HARDWARE_LITTLE_ENDIAN) { 
      return juint(p[0]) | (juint(p[1]) << 8) | (juint(p[2]) << 16) |
            (juint(p[3]) << 24);
    } else {
      return get_Java_u4(p);
    }
  }

  static inline void put_Java_u2(address p, jushort x)   { 
      p[0] = (unsigned char)(x >> 8);
      p[1] = (unsigned char)x;
  }

  static inline void put_Java_u4(address p, juint x) {
     ((jubyte*)p)[0] = (jubyte)(x >> 24);
     ((jubyte*)p)[1] = (jubyte)(x >> 16); 
     ((jubyte*)p)[2] = (jubyte)(x >> 8); 
     ((jubyte*)p)[3] = (jubyte)x; 
  }

  static inline void put_Java_u8(address p, julong x) {
    put_Java_u4(p, (juint)(x >> 32));
    put_Java_u4(p + 4, (juint)(x & 0xFFFFFFFFL));
  }

  static inline void put_native_u2(address p, jushort x)   { 
    if (HARDWARE_LITTLE_ENDIAN) { 
      p[0] = (unsigned char)x;
      p[1] = (unsigned char)(x >> 8);
    } else {
      put_Java_u2(p, x);
    }
  }

  static inline void put_native_u4(address p, juint x) {
    if (HARDWARE_LITTLE_ENDIAN) { 
     ((jubyte*)p)[0] = (jubyte)x; 
     ((jubyte*)p)[1] = (jubyte)(x >> 8); 
     ((jubyte*)p)[2] = (jubyte)(x >> 16); 
     ((jubyte*)p)[3] = (jubyte)(x >> 24);
    } else { 
      put_Java_u4(p, x);
    }
  }

  static inline jushort swap_u2(jushort x) {
    // Note: make an assembly version for better performance
     return (jushort) ((x & 0x00ff) << 8 | (x & 0xff00) >> 8);
  }

  static inline juint swap_u4(juint x) {
    // From: ARM Architecture Reference Manual, 2nd Ed.
    //       by David Seal, Addison-Wesley, Sect. 9.1.4
    // 
    // Compare with SourceMacros::swap_bytes which uses 4 instructions!
                                                 // x   =  A    B    C    D
    juint t1 = (x ^ _rotr(x, 16)) & 0xFF00FFFF;  // t1  =  A^C  0    C^A  D^B
    juint t2 = t1 >> 8;                          // t2  =  0    A^C  0    C^A 
    juint t3 = _rotr(x, 8);                      // t3  =  D    A    B    C
    return t2 ^ t3;                              //     =  D    C    B    A
  }
};
