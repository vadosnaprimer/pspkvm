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

#include <fcNotifyIsolates.h>

#include <midpEventUtil.h>
#include <jvm.h>
#include <pcsl_memory.h>
#include <midpServices.h>

/**
 * The list of notified isolates.
 * All isolates in the list receive <code>FC_DISKS_CHANGED_EVENT</code> event
 * if a disk is mounted/unmounted.
 */
static int* notifiedIsolates = NULL;

/**
 * Maximum number of concurrent isolates.
 * Note: will be reset in MVM mode in <code>registerNotifiedIsolate</code>
 * function.
 */
static int maxIsolates = 1;

/**
 * The function is called by <code>MidpMountedRoots::update</code> if some disk
 * has been mounted or unmounted.
 *
 * It uses internal <code>notifiedIsolates</code> array updated by
 * <code>registerNotifiedIsolate</code> and
 * <code>UnregisterNotifiedIsolate</code> internal functions to notify all
 * interested isolates.
 */
void notifyDisksChanged() {
    int i;
    MidpEvent event;

    MIDP_EVENT_INITIALIZE(event);
    event.type = FC_DISKS_CHANGED_EVENT;

    if (notifiedIsolates == NULL) {
        return; /* no isolates are registered */
    }

    /*
     * all interested isolates should be notified about mount/unmount
     * events, i.e. all isolates that registered <code>FileSystemListener</code>
     * listeners using <code>FileSystemRegistry.addFileSystemListener()</code>.
     */
    for (i = 0; i < maxIsolates; i++) {
        if (notifiedIsolates[i] != -1) {
            StoreMIDPEventInVmThread(event, notifiedIsolates[i]);
        }
    }
}

/**
 * Registers the isolate as interested in receiving mount/unmount events.
 *
 * @param isolate isolate ID
 */
void registerNotifiedIsolate(int isolate) {
    int i;

    if (notifiedIsolates == NULL) {
        int sizeInBytes;

#if ENABLE_MULTIPLE_ISOLATES
        maxIsolates = JVM_MaxIsolates();
#endif
        sizeInBytes = maxIsolates * sizeof(int);
        notifiedIsolates = (int*)pcsl_mem_malloc(sizeInBytes);
        if (notifiedIsolates == NULL) {
            return; /* out of memory */
        }
        memset(notifiedIsolates, -1, sizeInBytes);
    }

    /* check whether the isolate is already registered */
    for (i = 0; i < maxIsolates; i++) {
        if (notifiedIsolates[i] == isolate) {
            return;
        }
    }

    for (i = 0; i < maxIsolates; i++) {
        if (notifiedIsolates[i] == -1) {
            notifiedIsolates[i] = isolate;
            return;
        }
    }
}

/**
 * Unregisters current isolate as interested in receiving mount/unmount events.
 *
 * @param isolate isolate ID
 */
void UnregisterNotifiedIsolate(int isolate) {
    int i;

    if (notifiedIsolates == NULL) {
        return;
    }

    for (i = 0; i < maxIsolates; i++) {
        if (notifiedIsolates[i] == isolate) {
            notifiedIsolates[i] = -1;
            return;
        }
    }
}


/*
 * private native void finalize();
 */
KNIEXPORT KNI_RETURNTYPE_VOID
Java_javax_microedition_io_file_FileSystemEventHandlerBase_finalize()
{
    int isolate = getCurrentIsolateId();
    UnregisterNotifiedIsolate(isolate);
}

/*
 * private native void registerListener();
 */
KNIEXPORT KNI_RETURNTYPE_VOID
Java_javax_microedition_io_file_FileSystemEventHandlerBase_registerListener()
{
    int isolate = getCurrentIsolateId();
    registerNotifiedIsolate(isolate);
}
