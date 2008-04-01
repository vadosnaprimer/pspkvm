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
#ifndef _MIDP_EVENTS_H_
#define _MIDP_EVENTS_H_

/**
 * @defgroup events Event Handling
 * @ingroup subsystems
 */

/**
 * @defgroup events_queue Event Queue External Interface
 * @ingroup events
 */

/**
 * @file
 * @ingroup events_queue
 *
 * @brief Interface to the event queue.
 * These functions are NOT to be ported. They are implemented in 
 * shared layer already. Platform dependent layer should call them
 * to send events to Java platform system.
 */

#include <string.h>
#include <kni.h>
#include <midpport_eventqueue.h>

#ifdef __cplusplus
extern "C" {
#endif

/**
 * @name Reserved event type IDs.
 *
 * NOTE: Keep the numbers low since there will be
 * an array of references the size of the largest ID. For example
 * if the largest ID is 100 there will be a Java platform array created when
 * the event listener is registered to hold 100 objects.
 * Refer to <tt>com.sun.midp.events.EventTypes</tt> for documentation on
 * the parameters used with each event type.
 * @{
 */
#define MIDP_INVALID_EVENT 0

/* LCDUI events */
#define MIDP_KEY_EVENT                  1
#define MIDP_PEN_EVENT                  2
#define MIDP_COMMAND_EVENT              3
#define MIDP_REPAINT_EVENT              4
#define MIDP_SCREEN_CHANGE_EVENT        5
#define MIDP_INVALIDATE_EVENT           6
#define MIDP_ITEM_CHANGED_EVENT         7
#define MIDP_PEER_CHANGED_EVENT         8
#define MIDP_CALL_SERIALLY_EVENT        9
#define FOREGROUND_NOTIFY_EVENT         10
#define BACKGROUND_NOTIFY_EVENT         11
#define ACTIVATE_MIDLET_EVENT           12
#define PAUSE_MIDLET_EVENT              13
#define DESTROY_MIDLET_EVENT            14
#define SHUTDOWN_EVENT                  15
#define ACTIVATE_ALL_EVENT              16
#define PAUSE_ALL_EVENT                 17
#define MIDLET_CREATED_NOTIFICATION     18
#define MIDLET_ACTIVE_NOTIFICATION      19
#define MIDLET_PAUSED_NOTIFICATION      20
#define MIDLET_DESTROYED_NOTIFICATION   21
#define DISPLAY_CREATED_NOTIFICATION    22
#define FOREGROUND_REQUEST_EVENT        23
#define BACKGROUND_REQUEST_EVENT        24
#define SELECT_FOREGROUND_EVENT         25
#define PREEMPT_EVENT                   26
#define MIDLET_START_ERROR_EVENT        27
#define EXECUTE_MIDLET_EVENT            28
#define MIDLET_DESTROY_REQUEST_EVENT    29
#define FOREGROUND_TRANSFER_EVENT       30
#define EVENT_QUEUE_SHUTDOWN            31
#define FATAL_ERROR_NOTIFICATION        32

/* JSR-75 events */
#define FC_DISKS_CHANGED_EVENT          33

#define TEST_EVENT                      34

#define MIDLET_RESUME_REQUEST           35

#define NATIVE_MIDLET_EXECUTE_REQUEST   36
#define NATIVE_MIDLET_RESUME_REQUEST    37
#define NATIVE_MIDLET_PAUSE_REQUEST     38
#define NATIVE_MIDLET_DESTROY_REQUEST   39
#define NATIVE_MIDLET_GETINFO_REQUEST   40
#define NATIVE_SET_FOREGROUND_REQUEST   41

/* Automation API events */
#define SET_FOREGROUND_BY_NAME_REQUEST  42

#define ROTATION_EVENT                  43

/* MIDlet resources paused notification */
#define MIDLET_RS_PAUSED_NOTIFICATION   44

/* JSR-135 event */
#define MMAPI_EVENT                     45

/** JSR-234 event */ 
#define AMMS_EVENT                      46

/** The event to repaint entire screen */
#define SCREEN_REPAINT_EVENT            47

/** @} */

/**
 * @name The event parameter access macros.
 *
 * These macros provide descriptive names for the MidpEvent fields.
 * @see MidpEvent
 * @{
 */
#define ACTION          intParam1
#define CHR             intParam2
#define X_POS           intParam2
#define Y_POS           intParam3
#define COMMAND         intParam1
#define SYSTEM_EVENT_ID intParam1
#define DISPLAY         intParam4

#define MM_PLAYER_ID    intParam1
#define MM_DATA         intParam2
#define MM_ISOLATE      intParam3
#define MM_EVT_TYPE     intParam4
/** @} */

/**
 * Enqueues an event to be processed by the Java event thread for a given
 * Isolate, or all isolates if isolateId is -1.
 * Only safe to call from VM thread.
 * Any other threads should call StoreMIDPEvent. 
 *
 *   MidpEvent event;
 *
 *   MIDP_EVENT_INITIALIZE(event);
 *
 *   event.type = COOL_NEW_EVENT;  // this constant is in midpEvents.h
 *   event.intParam1 = x;
 *   event.intParam2 = y;
 *
 *   StoreMIDPEventInVmThread(event, 0);
 *
 * @param event      The event to enqueue.
 *
 * @param isolateId  ID of an Isolate 
 *                   -1 for broadcast to all isolates
 *                   0 for SVM mode
 */
void StoreMIDPEventInVmThread(MidpEvent event, int isolateId);

/** Initialize an event. For use with StoreMIDPEvent. */
#define MIDP_EVENT_INITIALIZE(E) { \
    memset(&(E), 0, sizeof (E)); \
    (E).stringParam1 = PCSL_STRING_NULL; \
    (E).stringParam2 = PCSL_STRING_NULL; \
    (E).stringParam3 = PCSL_STRING_NULL; \
    (E).stringParam4 = PCSL_STRING_NULL; \
    (E).stringParam5 = PCSL_STRING_NULL; \
    (E).stringParam6 = PCSL_STRING_NULL; \
}

/**
 * Initialize event sub-system, not for general use.
 *
 * @return 0 for success, or non-zero if the MIDP implementation is
 * out of memory
 */
int InitializeEvents();

/** Finalize event sub-system, not for general use. */
void FinalizeEvents();

/** Clear any pending events, not for general use. */
void midp_resetEvents();

/** Handles fatal error */
void handleFatalError();

#ifdef __cplusplus
}
#endif

#endif /* _MIDP_EVENTS_H_ */
