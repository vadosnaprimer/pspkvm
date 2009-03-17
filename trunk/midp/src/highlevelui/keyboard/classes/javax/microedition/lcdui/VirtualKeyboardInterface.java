/*
 * $LastChangedDate: 2005-11-21 02:11:20 +0900 (ì›? 21 11 2005) $  
 *
 * Copyright  1990-2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.microedition.lcdui;

/**
 * This is a popup layer that handles a sub-popup within the text tfContext
 * @author Amir Uval
 */

class VirtualKeyboardInterface {

    /** indicates whether the virtual keyboard is enabled */
    public static boolean USE_VIRTUAL_KEYBOARD = true;

    /** indicates whether the virtual keypad is enabled */
    public static final boolean USE_VIRTUAL_KEYPAD = false;

    /** indicates whether the virtual keyboard is opened automatically */
    public static boolean USE_VIRTUAL_KEYBOARD_OPEN_AUTO = false;

    /**
     * Checks if the virtual keyboard is enabled.
     * @return <code>true</code> if the virtual keyboard is enabled,
     *         <code>false</code> otherwise.
     */
    static boolean isKeyboardEnabled(){
        return USE_VIRTUAL_KEYBOARD;
    }

    /**
     * Checks if the virtual keyboard is enabled.
     * @return <code>true</code> if the virtual keyboard is enabled,
     *         <code>false</code> otherwise.
     */
    static boolean isKeypadEnabled(){
        return USE_VIRTUAL_KEYPAD;
    }

    /**
     * Checks if the virtual keyboard is opened automatically.
     * @return <code>true</code> if the virtual keyboard is opened automatically,
     *         <code>false</code> otherwise.
     */
    static boolean isAutoOpen(){
        return USE_VIRTUAL_KEYBOARD_OPEN_AUTO;
    }
}
