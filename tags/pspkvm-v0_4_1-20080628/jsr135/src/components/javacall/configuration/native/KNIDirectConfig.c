/*
 * 
 * Copyright  1990-2006 Sun Microsystems, Inc. All Rights Reserved.
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

#include <kni.h>
//#include "midp_logging.h"
//#include "midpEvents.h"
//#include "midpMalloc.h"
//#include "pcsl_print.h"
#include "javacall_defs.h"
#include "javacall_multimedia.h"

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
Java_com_sun_mmedia_DefaultConfiguration_nIsAmrSupported() {
    int res = 0;
    int i   = 0;

    const javacall_media_caps* caps = javacall_media_get_caps();

    while( NULL != caps[ i ].mimeType )
    {
        if( 0 == strcmp( caps[ i ].mimeType, JAVACALL_AUDIO_AMR_MIME ) )
        {
            res = 1;
            break;
        }
        i++;
    }

    KNI_ReturnBoolean( res ); 
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
Java_com_sun_mmedia_DefaultConfiguration_nIsJtsSupported() {
    int res = 0;
    int i   = 0;

    const javacall_media_caps* caps = javacall_media_get_caps();

    while( NULL != caps[ i ].mimeType )
    {
        if( 0 == strcmp( caps[ i ].mimeType, JAVACALL_AUDIO_TONE_MIME ) )
        {
            res = 1;
            break;
        }
        i++;
    }

    KNI_ReturnBoolean( res ); 
}
