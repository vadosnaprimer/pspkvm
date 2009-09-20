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
		
	AMSMidletCustomItem_WifiManager(RunningMIDletSuiteInfo msi, AppManagerUI ams) {
		super(msi, ams); }

	/* Override */		
	// TODO--See if this is how it used to work. Does it do fg/bg?
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
	static AMSMidletCustomItem getWiFiMidletCI(AppManagerUI ams) {
		RunningMIDletSuiteInfo msi =
			new RunningMIDletSuiteInfo(MIDletSuite.INTERNAL_SUITE_ID,
				WIFI_SELECTOR_APP,
				"Network Setup",
				true);
		AMSMidletCustomItem r = new AMSMidletCustomItem_WifiManager(msi, ams);
		r.setDefaultCommand(launchWifiSetupCmd);
		return r; }
}
