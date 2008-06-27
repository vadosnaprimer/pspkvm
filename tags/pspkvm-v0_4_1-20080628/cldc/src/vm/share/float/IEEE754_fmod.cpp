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
 * __ieee754_fmod(x,y)
 * Return x mod y in exact arithmetic
 * Method: shift and subtract
 */

#include "incls/_precompiled.incl"
#include "incls/_IEEE754_fmod.cpp.incl"

#if ENABLE_FLOAT

#if ARM

#ifdef __cplusplus
extern "C" {
#endif

static const double one_fmod = 1.0,
                    Zero[] = {0.0, -0.0,};

double ieee754_fmod(double x, double y) {
  int n, hx, hy, hz, ix, iy, sx, i;
  unsigned lx, ly, lz;

  hx = __JHI(x);   /* high word of x */
  lx = __JLO(x);   /* low  word of x */
  hy = __JHI(y);   /* high word of y */
  ly = __JLO(y);   /* low  word of y */
  sx = hx & 0x80000000;   /* sign of x */
  hx ^= sx;    /* |x| */
  hy &= 0x7fffffff; /* |y| */

    /* purge off exception values */
  if ((hy | ly) == 0 || (hx >= 0x7ff00000) || /* y=0,or x not finite */
     ((hy | ((ly | (0-ly)) >> 31)) > 0x7ff00000)) {/* or y is NaN */
    return jvm_ddiv(jvm_dmul(x, y), jvm_dmul(x, y));
  }
  if (hx <= hy) {
    if ((hx < hy) || (lx < ly)) {
      return x;  /* |x|<|y| return x */
    }
    if (lx == ly) {
      return Zero[(unsigned)sx >> 31];  /* |x|=|y| return x*0*/
    }
  }

    /* determine ix = ilogb(x) */
  if (hx < 0x00100000) { /* subnormal x */
    if (hx == 0) {
      for (ix = -1043, i = lx; i > 0; i <<= 1) {
        ix -= 1;
      }
    } else {
      for (ix = -1022, i = (hx << 11); i > 0; i <<= 1) {
        ix -= 1;
      }
    }
  } else {
    ix = (hx >> 20) - 1023;
  }

    /* determine iy = ilogb(y) */
  if (hy < 0x00100000) { /* subnormal y */
    if (hy == 0) {
      for (iy = -1043, i = ly; i > 0; i <<= 1) {
        iy -= 1;
      }
    } else {
      for (iy = -1022, i = (hy << 11); i > 0; i <<= 1) {
        iy -= 1;
      }
    }
  } else {
    iy = (hy >> 20) - 1023;
  }

    /* set up {hx,lx}, {hy,ly} and align y to x */
  if (ix >= -1022) {
    hx = 0x00100000 | (0x000fffff & hx);
  } else {    /* subnormal x, shift x to normal */
    n = -1022 - ix;
    if (n <= 31) {
      hx = (hx << n) | (lx >> (32 - n));
      lx <<= n;
    } else {
      hx = lx << (n - 32);
      lx = 0;
    }
  }
  if (iy >= -1022) {
    hy = 0x00100000 | (0x000fffff & hy);
  } else {    /* subnormal y, shift y to normal */
    n = -1022 - iy;
    if (n <= 31) {
      hy = (hy << n) | (ly >> (32 - n));
      ly <<= n;
    } else {
      hy = ly << (n - 32);
      ly = 0;
    }
  }

    /* fix point fmod */
  n = ix - iy;
  while (n--) {
    hz = hx - hy;
    lz = lx - ly;
    if (lx < ly) {
      hz -= 1;
    }
    if (hz < 0) {
      hx = hx + hx + (lx >> 31);
      lx = lx + lx;
    } else {
      if ((hz | lz) == 0) {    /* return sign(x)*0 */
        return Zero[(unsigned)sx >> 31];
      }
      hx = hz + hz + (lz >> 31);
      lx = lz + lz;
    }
  }
  hz = hx - hy;
  lz = lx - ly;
  if (lx < ly) {
    hz -= 1;
  }
  if (hz >= 0) {
    hx = hz;
    lx = lz;
  }

    /* convert back to floating value and restore the sign */
  if ((hx | lx) == 0) {      /* return sign(x)*0 */
    return Zero[(unsigned)sx >> 31];
  }
  while (hx < 0x00100000) {    /* normalize x */
    hx = hx + hx + (lx >> 31);
    lx = lx + lx;
    iy -= 1;
  }
  if (iy >= -1022) {  /* normalize output */
    hx = ((hx - 0x00100000) | ((iy + 1023) << 20));
    x = jdouble_from_msw_lsw(hx | sx, lx);
  } else {    /* subnormal output */
    n = -1022 - iy;
    if (n <= 20) {
      lx = (lx >> n) | ((unsigned)hx << (32 - n));
      hx >>= n;
    } else if (n <= 31) {
      lx = (hx << (32 - n)) | (lx >> n);
      hx = sx;
    } else {
      lx = hx >> (n - 32);
      hx = sx;
    }
    x = jdouble_from_msw_lsw(hx | sx, lx);
    x = jvm_dmul( x, one_fmod);   /* create necessary signal */
  }
  return x;   /* exact output */
}

#ifdef __cplusplus
}
#endif

#endif // ARM

#endif // ENABLE_FLOAT
