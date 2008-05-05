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
 * __kernel_cos( x,  y )
 * kernel cos function on [-pi/4, pi/4], pi/4 ~ 0.785398164
 * Input x is assumed to be bounded by ~pi/4 in magnitude.
 * Input y is the tail of x.
 *
 * Algorithm
 *  1. Since cos(-x) = cos(x), we need only to consider positive x.
 *  2. if x < 2^-27 (hx<0x3e400000 0), return 1 with inexact if x!=0.
 *  3. cos(x) is approximated by a polynomial of degree 14 on
 *     [0,pi/4]
 *                           4            14
 *      cos(x) ~ 1 - x*x/2 + C1*x + ... + C6*x
 *     where the remez error is
 *
 *  |              2     4     6     8     10    12     14 |     -58
 *  |cos(x)-(1-.5*x +C1*x +C2*x +C3*x +C4*x +C5*x  +C6*x  )| <= 2
 *  |                                      |
 *
 *                 4     6     8     10    12     14
 *  4. let r = C1*x +C2*x +C3*x +C4*x +C5*x  +C6*x  , then
 *         cos(x) = 1 - x*x/2 + r
 *     since cos(x+y) ~ cos(x) - sin(x)*y
 *            ~ cos(x) - x*y,
 *     a correction term is necessary in cos(x) and hence
 *      cos(x+y) = 1 - (x*x/2 - (r - x*y))
 *     For better accuracy when x > 0.3, let qx = |x|/4 with
 *     the last 32 bits mask off, and if x > 0.78125, let qx = 0.28125.
 *     Then
 *      cos(x+y) = (1-qx) - ((x*x/2-qx) - (r-x*y)).
 *     Note that 1-qx and (x*x/2-qx) is EXACT here, and the
 *     magnitude of the latter is at least a quarter of x*x/2,
 *     thus, reducing the rounding error in the subtraction.
 */

#include "incls/_precompiled.incl"
#include "incls/_Cosine_kernel.cpp.incl"

#if ENABLE_FLOAT

#ifdef __cplusplus
extern "C" {
#endif

static const double
one_c =  1.00000000000000000000e+00, /* 0x3FF00000, 0x00000000 */
C1  =  4.16666666666666019037e-02, /* 0x3FA55555, 0x5555554C */
C2  = -1.38888888888741095749e-03, /* 0xBF56C16C, 0x16C15177 */
C3  =  2.48015872894767294178e-05, /* 0x3EFA01A0, 0x19CB1590 */
C4  = -2.75573143513906633035e-07, /* 0xBE927E4F, 0x809C52AD */
C5  =  2.08757232129817482790e-09, /* 0x3E21EE9E, 0xBDB4B1C4 */
C6  = -1.13596475577881948265e-11; /* 0xBDA8FAE9, 0xBE8838D4 */

double cosine_kernel(double x, double y) {
  double a, hz, z, r, qx;
  int ix;
  ix = __JHI(x) & 0x7fffffff;    /* ix = |x|'s high word*/
  if (ix < 0x3e400000) {         /* if x < 2**27 */
    if (jvm_d2i(x) == 0 ) {
      return one_c;     /* generate inexact */
    }
  }
  z  = jvm_dmul(x, x);
  r  = jvm_dmul(z, jvm_dadd(C1, jvm_dmul(z, jvm_dadd(C2, jvm_dmul(z, jvm_dadd(C3, jvm_dmul(z, jvm_dadd(C4, jvm_dmul(z, jvm_dadd(C5, jvm_dmul(z,C6)))))))))));
  if (ix < 0x3FD33333) {           /* if |x| < 0.3 */
    return jvm_dsub(one_c, jvm_dsub(jvm_dmul(0.5,z), jvm_dsub(jvm_dmul(z,r), jvm_dmul(x,y))));
  } else {
    if(ix > 0x3fe90000) {       /* x > 0.78125 */
      qx = 0.28125;
    } else {
      qx = jdouble_from_msw_lsw(ix - 0x00200000 /* x/4 */, 0);
    }
    hz = jvm_dsub(jvm_dmul(0.5, z), qx);
    a  = jvm_dsub(one_c, qx);
    return jvm_dsub(a, jvm_dsub(hz, jvm_dsub(jvm_dmul(z, r), jvm_dmul(x, y))));
  }
}

#ifdef __cplusplus
}
#endif

#endif // ENABLE_FLOAT
