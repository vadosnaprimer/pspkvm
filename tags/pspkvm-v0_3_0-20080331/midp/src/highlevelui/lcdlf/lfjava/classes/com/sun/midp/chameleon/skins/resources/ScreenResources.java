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

package com.sun.midp.chameleon.skins.resources;

import com.sun.midp.chameleon.skins.SkinPropertiesIDs;
import com.sun.midp.chameleon.skins.ScreenSkin;
import com.sun.midp.log.*;

import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Display;

import com.sun.midp.configurator.Constants;

public class ScreenResources {
    private static boolean init;
    
    private ScreenResources() {
    }
    
    public static void load() {
        load(false);
    }
        
    public static void load(boolean reload) {
        if (init && !reload) {
            return;
        }

        if ((ScreenSkin.WIDTH <= 0) || (ScreenSkin.HEIGHT <= 0)) {
            if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                Logging.report(Logging.WARNING, LogChannels.LC_HIGHUI,
                        "Screen dimentions not set.");
            }
        }

        int textOrient = SkinResources.getInt(
                SkinPropertiesIDs.SCREEN_TEXT_ORIENT);
        ScreenSkin.TEXT_ORIENT = SkinResources.resourceConstantsToGraphics(
                textOrient);

        ScreenSkin.PAD_FORM_ITEMS = SkinResources.getInt(
                SkinPropertiesIDs.SCREEN_PAD_FORM_ITEMS);
        ScreenSkin.PAD_LABEL_VERT = SkinResources.getInt(
                SkinPropertiesIDs.SCREEN_PAD_LABEL_VERT);
        ScreenSkin.PAD_LABEL_HORIZ = SkinResources.getInt(
                SkinPropertiesIDs.SCREEN_PAD_LABEL_HORIZ);
        ScreenSkin.COLOR_BG = SkinResources.getInt(
                SkinPropertiesIDs.SCREEN_COLOR_BG);
        ScreenSkin.COLOR_HS_BG = SkinResources.getInt(
                SkinPropertiesIDs.SCREEN_COLOR_HS_BG);
        ScreenSkin.COLOR_FG = SkinResources.getInt(
                SkinPropertiesIDs.SCREEN_COLOR_FG);
        ScreenSkin.COLOR_BG_HL = SkinResources.getInt(
                SkinPropertiesIDs.SCREEN_COLOR_BG_HL);
        ScreenSkin.COLOR_FG_HL = SkinResources.getInt(
                SkinPropertiesIDs.SCREEN_COLOR_FG_HL);
        ScreenSkin.COLOR_BORDER = SkinResources.getInt(
                SkinPropertiesIDs.SCREEN_COLOR_BORDER);
        ScreenSkin.COLOR_BORDER_HL = SkinResources.getInt(
                SkinPropertiesIDs.SCREEN_COLOR_BORDER_HL);
        ScreenSkin.COLOR_TRAVERSE_IND = SkinResources.getInt(
                SkinPropertiesIDs.SCREEN_COLOR_TRAVERSE_IND);

        int borderStyle = SkinResources.getInt(
                SkinPropertiesIDs.SCREEN_BORDER_STYLE);
        ScreenSkin.BORDER_STYLE = SkinResources.resourceConstantsToGraphics(
                borderStyle);

        ScreenSkin.SCROLL_AMOUNT = SkinResources.getInt(
                SkinPropertiesIDs.SCREEN_SCROLL_AMOUNT);
        ScreenSkin.FONT_LABEL = SkinResources.getFont(
                SkinPropertiesIDs.SCREEN_FONT_LABEL);
        ScreenSkin.FONT_INPUT_TEXT = SkinResources.getFont(
                SkinPropertiesIDs.SCREEN_FONT_INPUT_TEXT);
        ScreenSkin.FONT_STATIC_TEXT = SkinResources.getFont(
                SkinPropertiesIDs.SCREEN_FONT_STATIC_TEXT);

        ScreenSkin.IMAGE_WASH = SkinResources.getImage(
                SkinPropertiesIDs.SCREEN_IMAGE_WASH);
        ScreenSkin.IMAGE_BG = SkinResources.getImage(
                SkinPropertiesIDs.SCREEN_IMAGE_BG);

        /*
        Uncomment if 9 pc screen background images are used
        ScreenSkin.IMAGE_BG_W_TITLE = SkinResources.getCompositeImage(
                SkinPropertiesIDs.SCREEN_IMAGE_BG_W_TITLE, 9);
        ScreenSkin.IMAGE_BG_WO_TITLE = SkinResources.getCompositeImage(
                SkinPropertiesIDs.SCREEN_IMAGE_BG_WO_TITLE, 9);
        */

        // IMPL NOTE: There shouldn't be any "Homescreen" resources in
        // ScreenResources. There should be a new window altogether which
        // handles the app manager stuff and its resources should be in a
        // separate resource class all together.

        /*
        Uncomment if background images for "home" screen are used
        ScreenSkin.IMAGE_HS_BG_TILE = SkinResources.getImage(
                SkinPropertiesIDs.SCREEN_IMAGE_HS_BG_TILE);
        ScreenSkin.IMAGE_HS_BG_W_TITLE = SkinResources.getCompositeImage(
                SkinPropertiesIDs.SCREEN_IMAGE_HS_BG_W_TITLE, 9);
        ScreenSkin.IMAGE_HS_BG_WO_TITLE = SkinResources.getCompositeImage(
                SkinPropertiesIDs.SCREEN_IMAGE_HS_BG_WO_TITLE, 9);
        */

        init = true;
    }

}


