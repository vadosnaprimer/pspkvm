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
    implements CommandListener, ItemStateListener {

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
    private int selectedDevice;
    private int selectedCPUSpeed;
    
    private final static String[] keys = {
    		"---", "UP", "DOWN", "LEFT", "RIGHT", "SQUARE", "TRIANGLE", "CROSS", "CIRCLE", "SELECT", "START",
    		"SHIFT+UP", "SHIFT+DOWN", "SHIFT+LEFT", "SHIFT+RIGHT", "SHIFT+SQUARE", "SHIFT+TRIANGLE",
    		"SHIFT+CROSS", "SHIFT+CIRCLE", "SHIFT+SELECT", "SHIFT+START",
    		"ANALOG UP", "ANALOG DOWN", "ANALOG LEFT", "ANALOG RIGHT"
    };
    private final static String[] keyname = {
    		"UP", "DOWN", "LEFT", "RIGHT", 
    		"NUM1", "NUM2", "NUM3", "NUM4",
    		"NUM5", "NUM6", "NUM7", "NUM8",
    		"NUM9", "NUM0", "*", "#",
    		"SELECT", "CLEAR", "SOFT1", "SOFT2",
    		"SPACE", "BACKSPACE"
    };
    	
    private final static String CPU_SPEED_SELECTOR_LABEL = "Select preferred CPU freq.";
    private final static String DEVICE_SELECTOR_LABEL = "Select preferred device";
    
    public DeviceSetting(int suiteId, Display display, Displayable nextScreen) {
    	 super("Device Setting");
        this.suiteId = suiteId;
        this.display = display;
        this.nextScreen = nextScreen;
        
        selectedDevice = GraphicalInstaller.getGameDevSetting(suiteId);
        if (selectedDevice < 0) {
            selectedDevice = 0;
        }
        
        append(createDeviceSelector());        

        selectedCPUSpeed = GraphicalInstaller.getPrefferedCPUSpeed(suiteId);
        append(createCPUSpeedSelector());
        
        int[] keymap = GraphicalInstaller.getKeymap(suiteId);
        if (keymap == null) {
            keymap = DeviceDesc.getDefaultKeymap();
        }

        if (keymap != null) {
            int i;

            append(new StringItem("Set keys assignment:", "\n"));
            
            for (i = 0; i < keymap.length; i++) {
            	if (keymap[i] < 0 || keymap[i] >= keys.length) {
            	    keymap[i] = 0; // Invalid key mapping
            	}
            	    append(new KeySettingItem(keyname[i], keymap[i]));
            }
        }
       
        setItemStateListener(this);
        
        addCommand(okCmd);
        addCommand(cancelCmd);
        
        setCommandListener(this);


    }
    
    public void commandAction(Command c, Displayable s) {
        if (c == okCmd) {
            GraphicalInstaller.saveDeviceSettings(selectedDevice, selectedCPUSpeed, suiteId);
            GraphicalInstaller.saveKeymap(getKeymap(), suiteId);
            display.setCurrent(nextScreen);
        } else if ( c == cancelCmd) {
            display.setCurrent(nextScreen);
        }                	
          
    }

    public void itemStateChanged(Item item) {
       int i;
    	if (item instanceof KeySettingItem) {
    	    ((KeySettingItem)item).setKeyValue();
    	    for (i = 2; i < 22; i++) {
    	        KeySettingItem tmpitem = (KeySettingItem)get(i);
    	        if ((tmpitem != item) && 
    	             (tmpitem.getKeyValue() == ((KeySettingItem)item).getKeyValue())) {
    	            tmpitem.setKeyValue(0);
    	        }
    	    }
    	} else if (item instanceof ChoiceGroup) {
    	    if (item.getLabel().equals(CPU_SPEED_SELECTOR_LABEL)) {
    	        i = ((ChoiceGroup)item).getSelectedIndex();
    	        if (i == 0) {
    	            selectedCPUSpeed = 111;
    	        } else if (i == 2) {
    	            selectedCPUSpeed = 333;
    	        } else {
    	            selectedCPUSpeed = 222;
    	        }
    	    } else {
    	        selectedDevice = DeviceDesc.dispIdToDevId(((ChoiceGroup)item).getSelectedIndex());
    	    }
    	}
    }

    private ChoiceGroup createDeviceSelector() {
        ChoiceGroup selector = new ChoiceGroup(DEVICE_SELECTOR_LABEL, Choice.POPUP);
        int dev = 0;
        String name;

        while ((name = GraphicalInstaller.getDeviceName(DeviceDesc.dispIdToDevId(dev++))) != null) {        	
            selector.append(name, null);
        }

        try {
            selector.setSelectedIndex(DeviceDesc.devIdToDispId(selectedDevice), true);
        } catch (IndexOutOfBoundsException e) {
            System.err.println("createDeviceSelector: selected device is out of list!");
            selectedDevice = 0;
            selector.setSelectedIndex(DeviceDesc.devIdToDispId(selectedDevice), true);
        }
        selector.setLayout(Item.LAYOUT_NEWLINE_AFTER);

        return selector;
    }

    private ChoiceGroup createCPUSpeedSelector() {
    	 int index;
        ChoiceGroup selector = new ChoiceGroup(CPU_SPEED_SELECTOR_LABEL, Choice.POPUP);

        selector.append("111MHz", null);
        selector.append("222MHz", null);
        selector.append("333MHz", null);

        if (selectedCPUSpeed == 111) {
        	index = 0;
        } else if (selectedCPUSpeed == 333) {
        	index = 2;
        } else {
        	index = 1;
        }
        
        try {
            selector.setSelectedIndex(index, true);
        } catch (IndexOutOfBoundsException e) {
            selector.setSelectedIndex(1, true);
        }
        selector.setLayout(Item.LAYOUT_NEWLINE_AFTER);

        return selector;
    }


    int[] getKeymap() {
    	 int i;
    	 int[] ret = new int[22];
    	 try {
            for (i = 3; i < 25; i++) {
        	  KeySettingItem tmpitem = (KeySettingItem)get(i);
        	  ret[i-3] = tmpitem.getKeyValue();
            }
    	 } catch (IndexOutOfBoundsException e) {
    	     ret = null;
    	 }
        return ret;
    }

    class KeySettingItem extends ChoiceGroup {
    	
	String name;
	int value;
	
	public KeySettingItem(String keyName, int keyValue) {
		super(keyName, Choice.POPUP, keys, null);
		name = keyName;
		value = keyValue;
		setLayout(Item.LAYOUT_2);
		setSelectedIndex(keyValue, true);
	}

	protected int getMinContentHeight() {
		return 10;
	}
	
	protected int getMinContentWidth() {
		return 10;
	}
	
	protected int getPrefContentHeight(int width) {
		return 30;
	}
	
	protected int getPrefContentWidth(int height) {
		return 100;
	}

	public int getKeyValue() {
		return value;
	}

	public void setKeyValue(int val) {
		value = val;
		setSelectedIndex(0, true);
	}

	public void setKeyValue() {
		value = getSelectedIndex();
	}
    }
}

