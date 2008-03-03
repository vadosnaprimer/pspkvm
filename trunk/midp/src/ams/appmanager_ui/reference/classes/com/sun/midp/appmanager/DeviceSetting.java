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

package com.sun.midp.appmanager;

import javax.microedition.lcdui.*;

import com.sun.midp.i18n.Resource;
import com.sun.midp.i18n.ResourceConstants;

import com.sun.midp.installer.*;

import com.sun.midp.midletsuite.*;

import com.sun.midp.security.*;


import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;

/**
 * The Graphical MIDlet suite settings form.
 */
public class DeviceSetting extends Form
    implements CommandListener {

    /** Command object for "Select device". */
    private Command okCmd =
        new Command(Resource.getString(ResourceConstants.SAVE), 
                    Command.OK, 1);

    /** Command object for "Cancel" command for the form. */
    private Command cancelCmd =
        new Command(Resource.getString(ResourceConstants.CANCEL),
                    Command.CANCEL, 2);

    
    private int suiteId;
    private Display display;
    private Displayable nextScreen;

    public DeviceSetting(int suiteId, Display display, Displayable nextScreen) {
    	 super("Device Setting");
        this.suiteId = suiteId;
        this.display = display;
        this.nextScreen = nextScreen;
        append(createDeviceSelector(suiteId));
    }
    
    public void commandAction(Command c, Displayable s) {
    }

    private ChoiceGroup createDeviceSelector(int midletId) {
        final ChoiceGroup selector = new ChoiceGroup("Select preferred device", Choice.POPUP);
        final int mid = midletId;
        int dev = 0;
        String name;

        while ((name = GraphicalInstaller.getDeviceName(dev++)) != null) {        	
            selector.append(name, null);
        }

        addCommand(okCmd);
        addCommand(cancelCmd);
        
        setCommandListener(
            new CommandListener() {
                public void commandAction(Command c, Displayable d) { 
                	if (c == okCmd) {
                	    GraphicalInstaller.saveDeviceSettings(selector.getSelectedIndex(), mid);       
                	    display.setCurrent(nextScreen);
                	} else if ( c == cancelCmd) {
                	    display.setCurrent(nextScreen);
                	}                	
                }
            }
        );

        return selector;
    }

}

