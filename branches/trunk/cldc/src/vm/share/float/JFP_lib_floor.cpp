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

/*
 * floor(x)
 * Return x rounded toward -inf to integral value
 * Method:
 *  Bit twiddling.
 * Exception:
 *  Inexact flag raised if x not equal to floor(x).
 */

#include "incls/_precompiled.incl"
#include "incls/_JFP_lib_floor.cpp.incl"

#if ENABLE_FLOAT

#ifdef __cplusplus
extern "C" {
#endif

static const double huge_number_f = 1.0e300;

double jvm_fplib_floor(double x) {
  int i0, i1, j0;
  unsigned i, j;
  i0 =  __JHI(x);
  i1 =  __JLO(x);
  j0 = ((i0 >> 20) & 0x7ff) - 0x3ff;
  if (j0 < 20) {
    if (j0 < 0) {  /* raise inexact if x != 0 */
      if (jvm_dcmpl(jvm_dadd(huge_number_f, x), 0.0) > 0) {/* return 0*sign(x) if |x|<1 */
        if (i0 >= 0) {
          i0 = i1 = 0;
        } else if(((i0 & 0x7fffffff) | i1) != 0) {
          i0 = 0xbff00000;
          i1 = 0;
        }
      }
    } else {
      i = (0x000fffff) >> j0;
      if (((i0 & i) | i1) == 0) {
        return x; /* x is integral */
      }
      if (jvm_dcmpl(jvm_dadd(huge_number_f, x), 0.0) > 0) {    /* raise inexact flag */
        if (i0 < 0) {
          i0 += (0x00100000) >> j0;
        }
        i0 &= (~i);
        i1 = 0;
      }
    }
  } else if (j0 > 51) {
    if (j0 == 0x400) {
      return jvm_dadd(x, x);   /* inf or NaN */
    } else {
      return x;      /* x is integral */
    }
  } else {
    i = ((unsigned)(0xffffffff)) >> (j0 - 20);
    if ((i1 & i) == 0) {
      return x; /* x is integral */
    }
    if (jvm_dcmpl(jvm_dadd(huge_number_f, x), 0.0) > 0) {        /* raise inexact flag */
      if (i0 < 0) {
        if (j0 == 20) {
          i0 += 1;
        } else {
          j = i1 + (1 << (52 - j0));
          if((int)j < i1) {
            i0 += 1 ;   /* got a carry */
          }
          i1 = j;
        }
      }
      i1 &= (~i);
    }
  }
  return jdouble_from_msw_lsw(i0, i1);
}

#ifdef __cplusplus
}
#endif

#endif // ENABLE_FLOAT
