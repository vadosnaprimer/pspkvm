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
#ifdef __cplusplus
extern "C" {
#endif
    
#include <kni.h>

#include <jsr179_location.h>

/* JAVADOC COMMENT ELIDED */
jsr179_result jsr179_property_get(
        jsr179_property property,
        jchar* /*OUT*/outPropertyValue) {

    (void)property;
    (void)outPropertyValue;

    return JSR179_STATUSCODE_FAIL;
}

/* JAVADOC COMMENT ELIDED */
jsr179_result jsr179_provider_getinfo(
        const jchar* name,
        jsr179_provider_info* /*OUT*/pInfo) {
    
    (void)name;
    (void)pInfo;

    return JSR179_STATUSCODE_FAIL;
}
        
/* JAVADOC COMMENT ELIDED */
jsr179_result jsr179_provider_open(
        const jchar* name,
        /*OUT*/ jsr179_handle* pProvider) {

    (void)name;
    (void)pProvider;

    return JSR179_STATUSCODE_FAIL;
}

/* JAVADOC COMMENT ELIDED */
jsr179_result jsr179_provider_close(
    jsr179_handle provider) {

    (void)provider;

    return JSR179_STATUSCODE_FAIL;
}

/* JAVADOC COMMENT ELIDED */
jsr179_result jsr179_provider_state(
        jsr179_handle provider,
        /*OUT*/ jsr179_state* pState) {

    (void)provider;
    (void)pState;

    return JSR179_STATUSCODE_FAIL;
}

/* JAVADOC COMMENT ELIDED */
jsr179_result jsr179_update_set(
        jsr179_handle provider, 
        int timeout) {

    (void)provider;
    (void)timeout;

    return JSR179_STATUSCODE_FAIL;
}        

/* JAVADOC COMMENT ELIDED */
jsr179_result jsr179_update_cancel(
        jsr179_handle provider) {
    
    (void)provider;

    return JSR179_STATUSCODE_FAIL;
}

/* JAVADOC COMMENT ELIDED */
jsr179_result jsr179_location_get(
        jsr179_handle provider, 
        /*OUT*/ jsr179_location* pLocationInfo) {

    (void)provider;
    (void)pLocationInfo;

    return JSR179_STATUSCODE_FAIL;
}

/******************************************************************************
 ******************************************************************************
 ******************************************************************************
    OPTIONAL FUNCTIONS
 ******************************************************************************
 ******************************************************************************
 ******************************************************************************/
    
/* JAVADOC COMMENT ELIDED */
jsr179_result /*OPTIONAL*/ jsr179_get_extrainfo(
        jsr179_handle provider,
        jsr179_extrainfo_mimetype mimetype,
        int maxUnicodeStringBufferLen,
        /*OUT*/jchar* outUnicodeStringBuffer) {

    (void)provider;
    (void)mimetype;
    (void)maxUnicodeStringBufferLen;
    (void)outUnicodeStringBuffer;

    return JSR179_STATUSCODE_FAIL;
}
    
/* JAVADOC COMMENT ELIDED */
jsr179_result /*OPTIONAL*/ jsr179_get_addressinfo(
        jsr179_handle provider,
        /*IN and OUT*/ int* pAddresInfoFieldNumber,
        /*OUT*/jsr179_addressinfo_fieldinfo fields[]) {

    (void)provider;
    (void)pAddresInfoFieldNumber;
    (void)fields;

    return JSR179_STATUSCODE_FAIL;
}

#ifdef __cplusplus
} //extern "C"
#endif

