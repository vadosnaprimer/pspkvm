/* 
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
 *
 */


#ifndef JABSTRACT_SURFACE_H
#define JABSTRACT_SURFACE_H

#include <PiscesSurface.h>

typedef struct _AbstractSurface {
    Surface super;
    void (*acquire)(struct _AbstractSurface* surface, JNIEnv* env,
                    jobject surfaceHandle);
    void (*release)(struct _AbstractSurface* surface, JNIEnv* env,
                    jobject surfaceHandle);
    void (*cleanup)();
}
AbstractSurface;

#define ACQUIRE_SURFACE(surface, env, surfaceHandle)                         \
        ((AbstractSurface*)(surface))->acquire((AbstractSurface*)(surface),  \
                                               (env),                        \
                                               (surfaceHandle));             \
        if (!readMemErrorFlag()) {

#define RELEASE_SURFACE(surface, env, surfaceHandle)                         \
            ((AbstractSurface*)(surface))->release(                          \
                    (AbstractSurface*)(surface),                             \
                    (env),                                                   \
                    (surfaceHandle));                                        \
        }

AbstractSurface* surface_get(JNIEnv* env, jobject surfaceHandle);
jboolean surface_initialize(JNIEnv* env, jobject surfaceHandle);

#endif
