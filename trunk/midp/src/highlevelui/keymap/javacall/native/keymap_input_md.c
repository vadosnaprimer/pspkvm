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

#include <kni.h>
#include <keymap_input.h>
#include <midp_logging.h>
#include "javacall_keypress.h"
#include "javacall_devemu.h"

/**
 * @file
 *
 * Platform key mapping and input mode handling functions.
 *
 * This file contains all the platform input related
 * code, including all the key binding functions.
 */


/**
 * Platform specific code to name mapping table.
 */
static const KeymapKey Keys[] = {
    {JAVACALL_KEY_POWER,      "POWER"         }, /* 0 */
    {JAVACALL_KEY_SOFT1,      "SOFT1"         }, /* 1 */
    {JAVACALL_KEY_SOFT2,      "SOFT2"         }, /* 2 */
    {JAVACALL_KEY_UP,         "Up"            }, /* 3 */
    {JAVACALL_KEY_DOWN,       "Down"          }, /* 4 */
    {JAVACALL_KEY_LEFT,       "Left"          }, /* 5 */
    {JAVACALL_KEY_RIGHT,      "Right"         }, /* 6 */
    {JAVACALL_KEY_SELECT,     "Select"        }, /* 7 */
    {JAVACALL_KEY_SEND,       "Send"          }, /* 8 */
    {JAVACALL_KEY_END,        "End"           }, /* 9 */
    {JAVACALL_KEY_CLEAR,      "Clear"         }, /* 10 */
    {JAVACALL_KEY_1,          "1"             }, /* 11 */
    {JAVACALL_KEY_2,          "2"             }, /* 12 */
    {JAVACALL_KEY_3,          "3"             }, /* 13 */
    {JAVACALL_KEY_4,          "4"             }, /* 14 */
    {JAVACALL_KEY_5,          "5"             }, /* 15 */
    {JAVACALL_KEY_6,          "6"             }, /* 16 */
    {JAVACALL_KEY_7,          "7"             }, /* 17 */
    {JAVACALL_KEY_8,          "8"             }, /* 18 */
    {JAVACALL_KEY_9,          "9"             }, /* 19 */
    {JAVACALL_KEY_ASTERISK,   "*"             }, /* 20 */
    {JAVACALL_KEY_0,          "0"             }, /* 21 */
    {JAVACALL_KEY_POUND,      "#"             }, /* 22 */
    {JAVACALL_KEY_GAMEA,      "Calendar"      }, /* 23 */
    {JAVACALL_KEY_GAMEB,      "Addressbook"   }, /* 24 */
    {JAVACALL_KEY_GAMEC,      "Menu"          }, /* 25 */
    {JAVACALL_KEY_GAMED,      "Mail"          }, /* 26 */
    {JAVACALL_KEY_SPACE,      "Space"         }, /* 27 */
    {JAVACALL_KEY_BACKSPACE,  "BackSpace"     }, /* 28 */


    /**
     * These set of keys and the key events available to a 
     * CustomItem for UP, DOWN, LEFT and RIGHT game actions.
     * This is different from what is available on a Canvas.  
     * In this particular case the system has traversal so the system uses 
     * directional keys for traversal.
     * This is the mapping between key codes and UP, DOWN, LEFT and RIGHT 
     * game actions in a CustomItem.
     */

    /* GAME KEY_UP CustomItem KEY_UP */
    {JAVACALL_KEY_GAME_UP,    "SHIFT_UP"      }, /* 29 */
    /* GAME KEY_DOWN CustomItem KEY_DOWN */
    {JAVACALL_KEY_GAME_DOWN,  "SHIFT_DOWN"    }, /* 30 */
    /* GAME KEY_LEFT CustomItem KEY_LEFT */
    {JAVACALL_KEY_GAME_LEFT,  "SHIFT_LEFT"    }, /* 31 */
    /* GAME KEY_RIGHT CustomItem KEY_RIGHT */
    {JAVACALL_KEY_GAME_RIGHT, "SHIFT_RIGHT"   }, /* 32 */    
    /* Indicator of end of table */
    {JAVACALL_KEY_INVALID,    ""              }, /* 33 */
};

/**
 * Return the key code corresponding to the given abstract
 * game action.
 *
 * @param gameAction game action value
 */
int 
keymap_get_key_code(int gameAction)
{
    int id;

    REPORT_CALL_TRACE1(LC_LOWUI, "LF:keymap_get_key_code(%d)\n", gameAction);

    id = javacall_devemu_get_current_device();

    switch (gameAction) {
    case  1: /* Canvas.UP */
        return javacall_devemu_get_keycode(id, JAVACALL_KEY_UP);

    case  6: /* Canvas.DOWN */
        return javacall_devemu_get_keycode(id, JAVACALL_KEY_DOWN);

    case  2: /* Canvas.LEFT */
        return javacall_devemu_get_keycode(id, JAVACALL_KEY_LEFT);

    case  5: /* Canvas.RIGHT */
        return javacall_devemu_get_keycode(id, JAVACALL_KEY_RIGHT);

    case  8: /* Canvas.FIRE */
        return javacall_devemu_get_keycode(id, JAVACALL_KEY_SELECT);

    case  9: /* Canvas.GAME_A */
        return javacall_devemu_get_keycode(id, JAVACALL_KEY_5);

    case 10: /* Canvas.GAME_B */
        return javacall_devemu_get_keycode(id, JAVACALL_KEY_0);

    case 11: /* Canvas.GAME_C */
        return javacall_devemu_get_keycode(id, JAVACALL_KEY_ASTERISK);

    case 12: /* Canvas.GAME_D */
        return javacall_devemu_get_keycode(id, JAVACALL_KEY_POUND);

    default: return 0;
    }
}

/**
 * Return the abstract game action corresponding to the
 * given key code.
 *
 * @param keyCode key code value
 */
int 
keymap_get_game_action(int keyCode)
{
    int id;
    
    REPORT_CALL_TRACE1(LC_LOWUI, "LF:keymap_get_game_action(%d)\n", keyCode);

    id = javacall_devemu_get_current_device();
    
     if (keyCode == javacall_devemu_get_keycode(id, JAVACALL_KEY_UP)) {
        return 1; /* Canvas.UP */
     } else if (keyCode == javacall_devemu_get_keycode(id, JAVACALL_KEY_DOWN)) {
        return 6; /* Canvas.DOWN */
     } else if (keyCode == javacall_devemu_get_keycode(id, JAVACALL_KEY_LEFT)) {
        return 2; /* Canvas.LEFT */
     } else if (keyCode == javacall_devemu_get_keycode(id, JAVACALL_KEY_RIGHT)) {
        return 5; /* Canvas.RIGHT */
     } else if (keyCode == javacall_devemu_get_keycode(id, JAVACALL_KEY_SELECT)) {
        return 8; /* Canvas.FIRE */
     } else if (keyCode == javacall_devemu_get_keycode(id, JAVACALL_KEY_GAMEA) ||
     	   keyCode == javacall_devemu_get_keycode(id, JAVACALL_KEY_5)) {
        return 9;  /* Canvas.GAME_A */
     } else if (keyCode == javacall_devemu_get_keycode(id, JAVACALL_KEY_GAMEB) ||
     	   keyCode == javacall_devemu_get_keycode(id, JAVACALL_KEY_0)) {
        return 10; /* Canvas.GAME_B */
     } else if (keyCode == javacall_devemu_get_keycode(id, JAVACALL_KEY_GAMEC) ||
     	   keyCode == javacall_devemu_get_keycode(id, JAVACALL_KEY_ASTERISK)) {
        return 11; /* Canvas.GAME_C */
     } else if (keyCode == javacall_devemu_get_keycode(id, JAVACALL_KEY_GAMED) ||
     	   keyCode == javacall_devemu_get_keycode(id, JAVACALL_KEY_POUND)) {
        return 12; /* Canvas.GAME_D */
     } else if(keymap_is_invalid_key_code(keyCode)) {
            /* Invalid key code */
            return -1;
     } else {

        /* No game action available for this key */
        return 0;
     }
}

/**
 * Return the system key corresponding to the given key
 * code.
 *
 * @param keyCode key code value
 */
int
keymap_get_system_key(int keyCode)
{
    int id;
    
    REPORT_CALL_TRACE1(LC_LOWUI, "LF:keymap_get_system_key(%d)\n", keyCode);

    id = javacall_devemu_get_current_device();

    if (keyCode == javacall_devemu_get_keycode(id, JAVACALL_KEY_POWER)) {
    	return 1;
    }
    if (keyCode == javacall_devemu_get_keycode(id, JAVACALL_KEY_SEND)) {
    	return 2;
    }
    if (keyCode == javacall_devemu_get_keycode(id, JAVACALL_KEY_END)) {
    	return 3;
    }
    if (keyCode == javacall_devemu_get_keycode(id, JAVACALL_KEY_BACKSPACE) ||
    	  keyCode == javacall_devemu_get_keycode(id, JAVACALL_KEY_CLEAR)) {
    	return 4;
    }
    if (keyCode == javacall_devemu_get_keycode(id, JAVACALL_KEY_SELECT)) {
    	return 5;
    }
    
    return 0;
}


/**
 * Return the key string to the given key code.
 *
 * @param keyCode key code value
 *
 * @return C pointer to char or NULL if the keyCode does not
 * correspond to any name.
 */
char *
keymap_get_key_name(int keyCode)
{
    KeymapKey *map;
    REPORT_CALL_TRACE1(LC_LOWUI, "LF:keymap_get_key_name(%d)\n", keyCode);
    for (map = (KeymapKey *)Keys; map->keyCode != KEYMAP_KEY_INVALID; map++) {
        if (map->keyCode == keyCode) {
            return map->name;
        }
    }
    return 0;
}

/**
 * Return whether the keycode given is correct for
 * this platform.
 *
 * @param keyCode key code value
 */
jboolean
keymap_is_invalid_key_code(int keyCode)
{
    REPORT_CALL_TRACE1(LC_LOWUI, "LF:keymap_is_invalid_key_code(%d)\n", 
                       keyCode);

    /* 
     * Valid within UNICODE and not 0x0 and 0xffff 
     * since they are defined to be invalid
     */
    if ((keyCode <= 0x0) || (keyCode >= 0xFFFF) ) {
        return KNI_TRUE;
    }

    return KNI_FALSE;
}
