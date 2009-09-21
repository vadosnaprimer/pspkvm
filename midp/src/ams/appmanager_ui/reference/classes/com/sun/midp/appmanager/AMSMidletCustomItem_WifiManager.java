/*
 * CustomItem representing the wifi manager in the AMS. 
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
 * Original code AJ Milne / 2009
 *  
 **/

package com.sun.midp.appmanager;

import com.sun.midp.midlet.MIDletSuite;
import javax.microedition.lcdui.Command;
import com.sun.midp.i18n.Resource;
import com.sun.midp.i18n.ResourceConstants;

class AMSMidletCustomItem_WifiManager extends AMSMidletCustomItem {
	// Just has a slightly different set of commands--otherwise, it's a standard
	// AMSCustomItem
	/** Command object for "Launch" WiFi setup app. */
	static final Command launchWifiSetupCmd =
		new Command(Resource.getString(ResourceConstants.LAUNCH),
		Command.ITEM, 1);
	/** Constant for the wifi setup application class name. */
	private static final String WIFI_SELECTOR_APP =
		"com.sun.midp.appmanager.WifiSelector";
		
	AMSMidletCustomItem_WifiManager(RunningMIDletSuiteInfo msi, AppManagerUI ams,
		AMSFolderCustomItem p) {
		super(msi, ams, p); }

	/* Override */		
	void updateCommands() {
		if (isRunning()) {
			removeCommand(launchWifiSetupCmd);
			return; }
		if (msi.enabled) {
			addCommand(launchWifiSetupCmd);
			setDefaultCommand(launchWifiSetupCmd);
			return; }
		removeCommand(launchWifiSetupCmd); }

	// Constructs the AMSMidletCustomItem containing the wifi setup midlet
	static AMSMidletCustomItem getWiFiMidletCI(AppManagerUI ams, AMSFolderCustomItem p) {
		RunningMIDletSuiteInfo msi =
			new RunningMIDletSuiteInfo(MIDletSuite.INTERNAL_SUITE_ID,
				WIFI_SELECTOR_APP,
				"Network Setup",
				true);
		AMSMidletCustomItem r = new AMSMidletCustomItem_WifiManager(msi, ams, p);
		r.setDefaultCommand(launchWifiSetupCmd);
		return r; }
}
