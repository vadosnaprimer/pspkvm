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

#include <lfjport_export.h>


/**
 * @file
 * Additional porting API for Java Widgets based port of abstract
 * command manager.
 */

/**
 * Initializes the lfjport_ui_ native resources.
 *
 * @return <tt>0</tt> upon successful initialization, or
 *         <tt>other value</tt> otherwise
 */
int lfjport_ui_init() {
    return 1;
}

/**
 * Finalize the lfjport_ui_ native resources.
 */
void lfjport_ui_finalize() {
    
}

/**
 * Bridge function to request a repaint 
 * of the area specified.
 *
 * @param x1 top-left x coordinate of the area to refresh
 * @param y1 top-left y coordinate of the area to refresh
 * @param x2 bottom-right x coordinate of the area to refresh
 * @param y2 bottom-right y coordinate of the area to refresh
 */
void lfjport_refresh(int x1, int y1, int x2, int y2)
{
  (int)x1, y1, x2, y2;
}

/**
 * Porting API function to update scroll bar.
 *
 * @param scrollPosition current scroll position
 * @param scrollProportion maximum scroll position
 * @return status of this call
 */
int lfjport_set_vertical_scroll(int scrollPosition, int scrollProportion)
{

    /* Suppress unused parameter warnings */
    (void)scrollPosition;
    (void)scrollProportion;

    return 0;
}


/**
 * Turn on or off the full screen mode
 *
 * @param mode true for full screen mode
 *             false for normal
 */
void lfjport_set_fullscreen_mode(jboolean mode) {

    (void)mode;
    return;
}

/**
 * Resets native resources when foreground is gained by a new display.
 */
void lfjport_gained_foreground() {

}

/**
 * Change screen orientation flag
 */
jboolean lfjport_reverse_orientation() {
    return 0;
}

/**
 * Bridge function to get screen orientation flag
 */
jboolean lfjport_get_reverse_orientation()
{
    return 0;
}

/**
 * Return screen width
 */
int lfjport_get_screen_width() {
  /* TODO need to be ported */
    return 176;
}

/**
 *  Return screen height
 */
int lfjport_get_screen_height() {
  /* TODO need to be ported */
    return 210;
}

/**
 * Flushes the offscreen buffer directly to the device screen.
 * The size of the buffer flushed is defined by offscreen buffer width
 * and passed in height. 
 * Offscreen_buffer must be aligned to the top-left of the screen and
 * its width must be the same as the device screen width.
 * @param graphics The Graphics handle associated with the screen.
 * @param offscreen_buffer The ImageData handle associated with 
 *                         the offscreen buffer to be flushed
 * @param h The height to be flushed
 * @return KNI_TRUE if direct_flush was successful, KNI_FALSE - otherwise
 */
jboolean lfjport_direct_flush(const java_graphics *g, 
		  	      const java_imagedata *offscreen_buffer, int h) {
   return KNI_FALSE;
}
