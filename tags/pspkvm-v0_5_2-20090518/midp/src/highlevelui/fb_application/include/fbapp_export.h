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

#ifndef _FBAPP_EXPORT_H_
#define _FBAPP_EXPORT_H_

/**
 * @defgroup highui_fbapp Linux/framebuffer application library
 * @ingroup highui
 */

/**
 * @file
 * @ingroup highui_fbapp
 *
 * @brief Linux/framebuffer application exported native interface
 */

#include <fbapp_device_type.h>
#include <java_types.h>
#include <midpEvents.h>
#include <midpServices.h>

#ifdef __cplusplus
extern "C" {
#endif

/**
 * Initializes the FB native resources.
 */
extern void fbapp_init();

/**
 * Finalize the FB native resources.
 */
extern void fbapp_finalize();

/**
 * Refresh the given area.  For double buffering purposes.
 */
extern void fbapp_refresh(int x, int y, int w, int h);

/**
 * Invert screen orientation flag
 */
extern jboolean fbapp_reverse_orientation();

/*
 * Return screen orientation flag
 */
extern jboolean fbapp_get_reverse_orientation();

/**
 * Set full screen mode on/off
 */
extern void fbapp_set_fullscreen_mode(int mode);

/**
 * Returns the file descriptor for reading the mouse. 
 */
extern int fbapp_get_mouse_fd();

/**
  * Returns the file descriptor for reading the keyboard.
  */
extern int fbapp_get_keyboard_fd();

/**
 * Returns the type of the frame buffer device.
 */
extern int fbapp_get_fb_device_type();


/**
 * Map MIDP keycode value into proper MIDP event parameters
 * and platform signal attributes to unblock Java threads
 * waiting for this input event
 *
 * IMPL_NOTE: In general it is not specific for frame buffer application,
 *   however applications of other type can use rather different events
 *   mappings not based on MIDP keycodes or input keyboard events at all.
 *
 * @param pNewSignal reentry data to unblock threads waiting for a signal
 * @param pNewMidpEvent a native MIDP event to be stored to Java event queue
 * @param midpKeyCode MIDP keycode of the input event
 * @param isPressed true if the key is pressed, false if released
 * @param repeatedKeySupport true if MIDP should support repeated key
 *   presses on its own, false if platform supports repeated keys itself
 */
extern void fbapp_map_keycode_to_event(
    MidpReentryData* pNewSignal, MidpEvent* pNewMidpEvent,
    int midpKeyCode, jboolean isPressed, jboolean repeatedKeySupport);

/**
 * Query frame buffer device for screen width
 */ 
extern int fbapp_get_screen_width();

/**
 * Query frame buffer device for screen height
 */ 
extern int fbapp_get_screen_height();

/**
 * Query frame buffer device for screen x
 */ 
extern int fbapp_get_screen_x();

/**
 * Query frame buffer device for screen y
 */ 
extern int fbapp_get_screen_y();

#ifdef __cplusplus
}
#endif

#endif /* _FBAPP_APPLICATION_H_ */
