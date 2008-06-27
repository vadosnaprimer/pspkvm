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

#include "jvm.h"
#include "fdlibm.h"

/* JAVADOC COMMENT ELIDED */
#ifdef __STDC__
static const double
#else
static double
#endif
tiny  = 1.0e-300,
zero  = 0.0,
pi_o_4  = 7.8539816339744827900E-01, /* 0x3FE921FB, 0x54442D18 */
pi_o_2  = 1.5707963267948965580E+00, /* 0x3FF921FB, 0x54442D18 */
pi      = 3.1415926535897931160E+00, /* 0x400921FB, 0x54442D18 */
pi_lo   = 1.2246467991473531772E-16; /* 0x3CA1A626, 0x33145C07 */

#ifdef __STDC__
	double jsr179_atan2(double y, double x)
#else
	double jsr179_atan2(y,x)
	double  y,x;
#endif
{
	double z;
	int k,m,hx,hy,ix,iy;
	unsigned lx,ly;

	hx = jvm_double_msw(x); ix = hx&0x7fffffff;
	lx = jvm_double_lsw(x);
	hy = jvm_double_msw(y); iy = hy&0x7fffffff;
	ly = jvm_double_lsw(y);
	if(((ix|((lx|-(signed)lx)>>31))>0x7ff00000)||
	   ((iy|((ly|-(signed)ly)>>31))>0x7ff00000))	/* x or y is NaN */
	    return jvm_dadd(x, y); 
	if(((hx-0x3ff00000)|lx)==0) return jsr179_atan(y);   /* x=1.0 */
	m = ((hy>>31)&1)|((hx>>30)&2);	/* 2*sign(x) + sign(y) */

    /* when y = 0 */
	if((iy|ly)==0) {
	    switch(m) {
	    case 0:
	    case 1: return y; 	                /* atan(+-0,+anything) = +-0 */
	    case 2: return jvm_dadd(pi, tiny);  /* atan(+0,-anything)  =  pi */
	    case 3: return jvm_dsub(-pi, tiny); /* atan(-0,-anything)  = -pi */
	    }
	}
    /* when x = 0 */
	if((ix|lx)==0) return (hy<0)?  
	    jvm_dsub(-pi_o_2, tiny) :
	    jvm_dadd(pi_o_2, tiny);

    /* when x is INF */
	if(ix==0x7ff00000) {
	    if(iy==0x7ff00000) {
		switch(m) {
		case 0: return jvm_dadd(pi_o_4, tiny);     /* atan(+INF,+INF) */
		case 1: return jvm_dsub(-pi_o_4, tiny);    /* atan(-INF,+INF) */
		case 2: return jvm_dadd(3.0*pi_o_4, tiny); /* atan(+INF,-INF) */
		case 3: return jvm_dsub(-3.0*pi_o_4,tiny); /* atan(-INF,-INF) */
		}
	    } else {
		switch(m) {
		case 0: return zero  ;	                /* atan(+...,+INF) */
		case 1: return -1.0*zero  ;	        /* atan(-...,+INF) */
		case 2: return jvm_dadd(pi, tiny) ;	/* atan(+...,-INF) */
		case 3: return jvm_dsub(-pi, tiny) ;	/* atan(-...,-INF) */
		}
	    }
	}
    /* when y is INF */
	if(iy==0x7ff00000) return (hy<0)? -pi_o_2-tiny: pi_o_2+tiny;

    /* compute y/x */
	k = (iy-ix)>>20;
	if(k > 60) 
	    z=jvm_dadd(pi_o_2, 0.5*pi_lo); 	/* |y/x| >  2**60 */
	else if(hx < 0 && k < -60) 
	    z=0.0; 	                        /* |y|/x < -2**60 */
	else 
	    z=jsr179_atan(jvm_fabs(jvm_ddiv(y, x)));	/* safe to do y/x */
	switch (m) {
	case 0: 
	    return       z  ;	                      /* atan(+,+) */
	case 1: 
            hx = jvm_double_msw(z); hx ^= 0x80000000;
            lx = jvm_double_lsw(z);
	    return jvm_double_from_msw_lsw(hx, lx);   /* atan(-,+) */
	case 2: 
	    return  jvm_dsub(pi, jvm_dsub(z, pi_lo)); /* atan(+,-) */
	default: /* case 3 */
	    return  jvm_dsub(jvm_dsub(z, pi_lo), pi); /* atan(-,-) */
	}
}
