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

package com.sun.midp.chameleon.layers;

import com.sun.midp.chameleon.CLayer;
import com.sun.midp.chameleon.MIDPWindow;
import com.sun.midp.chameleon.skins.*;

/**
 *
 */
public class WashLayer extends CLayer {
    
    public WashLayer() {
        super(ScreenSkin.IMAGE_WASH, -1);
        setVisible(true);
        setOpaque(false);
    }
    
    public void setVisible(boolean visible) {
        if (ScreenSkin.IMAGE_WASH != null) {
            super.setVisible(visible);
        } else {
            super.setVisible(false);
        }
    }
    
    protected void initialize() {
        super.initialize();
        bounds[H] = ScreenSkin.HEIGHT - SoftButtonSkin.HEIGHT;        
    }

    /**
     * Update bounds of layer
     *
     * @param layers - current layer can be dependant on this parameter
     */
    public void update(CLayer[] layers) {
        super.update(layers);
        bounds[W] = ScreenSkin.WIDTH;
        if (layers[MIDPWindow.BTN_LAYER].isVisible()) {
            bounds[H] = ScreenSkin.HEIGHT - SoftButtonSkin.HEIGHT;    
        } else {
            bounds[H] = ScreenSkin.HEIGHT;
        }
    }
}

