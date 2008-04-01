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
import com.sun.midp.chameleon.skins.ChoiceGroupSkin;

import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Font;

public class ChoiceGroupResources {
    private static boolean init;
    
    // private constructor
    private ChoiceGroupResources() {
    }
    
    public static void load() {
        load(false);
    }
        
    public static void load(boolean reload) {
        if (init && !reload) {
            return;
        }

        ChoiceGroupSkin.WIDTH_IMAGE = SkinResources.getInt(
                SkinPropertiesIDs.CHOICE_WIDTH_IMAGE);
        ChoiceGroupSkin.HEIGHT_IMAGE = SkinResources.getInt(
                SkinPropertiesIDs.CHOICE_HEIGHT_IMAGE);
        ChoiceGroupSkin.WIDTH_SCROLL = SkinResources.getInt(
                SkinPropertiesIDs.CHOICE_WIDTH_SCROLL);
        ChoiceGroupSkin.WIDTH_THUMB = SkinResources.getInt(
                SkinPropertiesIDs.CHOICE_WIDTH_THUMB);
        ChoiceGroupSkin.HEIGHT_THUMB = SkinResources.getInt(
                SkinPropertiesIDs.CHOICE_HEIGHT_THUMB);
        ChoiceGroupSkin.PAD_H = SkinResources.getInt(
                SkinPropertiesIDs.CHOICE_PAD_H);
        ChoiceGroupSkin.PAD_V = SkinResources.getInt(
                SkinPropertiesIDs.CHOICE_PAD_V);
        ChoiceGroupSkin.COLOR_FG = SkinResources.getInt(
                SkinPropertiesIDs.CHOICE_COLOR_FG);
        ChoiceGroupSkin.COLOR_BG = SkinResources.getInt(
                SkinPropertiesIDs.CHOICE_COLOR_BG);
        ChoiceGroupSkin.COLOR_BORDER = SkinResources.getInt(
                SkinPropertiesIDs.CHOICE_COLOR_BRDR);
        ChoiceGroupSkin.COLOR_BORDER_SHD = SkinResources.getInt(
                SkinPropertiesIDs.CHOICE_COLOR_BRDR_SHD);
        ChoiceGroupSkin.COLOR_SCROLL = SkinResources.getInt(
                SkinPropertiesIDs.CHOICE_COLOR_SCROLL);
        ChoiceGroupSkin.COLOR_THUMB = SkinResources.getInt(
                SkinPropertiesIDs.CHOICE_COLOR_THUMB);
        ChoiceGroupSkin.FONT = SkinResources.getFont(
                SkinPropertiesIDs.CHOICE_FONT);
        ChoiceGroupSkin.FONT_FOCUS = SkinResources.getFont(
                SkinPropertiesIDs.CHOICE_FONT_FOCUS);
        ChoiceGroupSkin.IMAGE_RADIO = SkinResources.getCompositeImage(
                SkinPropertiesIDs.CHOICE_IMAGE_RADIO, 2);
        ChoiceGroupSkin.IMAGE_CHKBOX = SkinResources.getCompositeImage(
                SkinPropertiesIDs.CHOICE_IMAGE_CHKBX, 2);
        /*
        Uncomment if background image is used
        ChoiceGroupSkin.IMAGE_BG = SkinResources.getCompositeImage(
                SkinPropertiesIDs.CHOICE_IMAGE_BG, 9);
        */
        ChoiceGroupSkin.IMAGE_BUTTON_BG = SkinResources.getCompositeImage(
                SkinPropertiesIDs.CHOICE_IMAGE_BTN_BG, 9);
        ChoiceGroupSkin.IMAGE_BUTTON_ICON = SkinResources.getImage(
                SkinPropertiesIDs.CHOICE_IMAGE_BTN_ICON);
        ChoiceGroupSkin.IMAGE_POPUP_BG = SkinResources.getCompositeImage(
                SkinPropertiesIDs.CHOICE_IMAGE_POPUP_BG, 9);

        init = true;
    }
}

