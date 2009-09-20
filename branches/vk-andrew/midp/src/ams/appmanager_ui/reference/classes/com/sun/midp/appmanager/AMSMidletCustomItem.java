/*
	CustomItem representing a midlet in the AMS. 
*/

package com.sun.midp.appmanager;

import java.io.*;
import com.sun.midp.midletsuite.*;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import com.sun.midp.midlet.MIDletSuite;
import com.sun.midp.i18n.Resource;
import com.sun.midp.i18n.ResourceConstants;
import com.sun.midp.main.MIDletProxy;
import javax.microedition.lcdui.Command;

class AMSMidletCustomItem extends AMSCustomItem {

	/** Constant for the graphical installer class name. */
	static final String INSTALLER =
		"com.sun.midp.installer.GraphicalInstaller";
	/** Constant for the discovery application class name. */
	static final String DISCOVERY_APP =
		"com.sun.midp.installer.DiscoveryApp";
	/** Constant for the wifi setup application class name. */
	private static final String WIFI_SELECTOR_APP =
		"com.sun.midp.appmanager.WifiSelector";
	/** Command object for "Launch" install app. */
	static Command launchInstallCmd =
		new Command(Resource.getString(ResourceConstants.LAUNCH),
		Command.ITEM, 1);
	/** Command object for "Launch" WiFi setup app. */
	static final Command launchWifiSetupCmd =
		new Command(Resource.getString(ResourceConstants.LAUNCH),
		Command.ITEM, 1);
	/** General command object for "Launch" */
	static final Command launchCmd =
		new Command(Resource.getString(ResourceConstants.LAUNCH),
			Command.ITEM, 1);
	/** Command object for "Bring to foreground". */
	static final Command fgCmd = new Command(Resource.getString
	(ResourceConstants.FOREGROUND), Command.ITEM, 1);
	/** Command object for "End" midlet. */
	static final Command endCmd = new Command(Resource.getString
	 (ResourceConstants.END), Command.ITEM, 1);

	/** The MIDletSuiteInfo associated with this MidletCustomItem */
  RunningMIDletSuiteInfo msi; // = null

	/**
	 * The icon to be used to draw this midlet representation.
	 */
	Image icon; // = null

	// Constructs the AMSMidletCustomItem containing the discovery midlet
	static AMSMidletCustomItem getDiscoveryMidletCI(AppManagerUI ams) {
		RunningMIDletSuiteInfo msi =
			new RunningMIDletSuiteInfo(MIDletSuite.INTERNAL_SUITE_ID,
		    DISCOVERY_APP,
		    Resource.getString(ResourceConstants.INSTALL_APPLICATION),
				true) {
					public boolean equals(MIDletProxy midlet) {
						if (super.equals(midlet)) {
							return true; }
						return (INSTALLER.equals(midlet.getClassName())); } };
		AMSMidletCustomItem r = new AMSMidletCustomItem(msi, ams);
		r.setDefaultCommand(launchInstallCmd);
		return r; }

	// Constructs the AMSMidletCustomItem containing the wifi setup midlet
	static AMSMidletCustomItem getWiFiMidletCI(AppManagerUI ams) {
		RunningMIDletSuiteInfo msi =
			new RunningMIDletSuiteInfo(MIDletSuite.INTERNAL_SUITE_ID,
				WIFI_SELECTOR_APP,
				"Network Setup",
				true);
		AMSMidletCustomItem r = new AMSMidletCustomItem(msi, ams);
		r.setDefaultCommand(launchWifiSetupCmd);
		return r; }

	/**
	* Constructs a midlet representation for the App Selector Screen.
	* @param msi The MIDletSuiteInfo for which representation has
	*            to be created
	*/
	AMSMidletCustomItem(RunningMIDletSuiteInfo msi, AppManagerUI ams) {
		super(null, ams);
		this.msi = msi;
		icon = msi.icon;
		text = msi.displayName.toCharArray();
		textLen = msi.displayName.length(); }

	// Same from just the suiteID--used by initial folder creation code.
	AMSMidletCustomItem(int suiteID, AppManagerUI ams) throws IOException {
		super(null, ams);
		createMSI(suiteID);
		text = msi.displayName.toCharArray();
		textLen = msi.displayName.length(); }
		
	// Helper for constructors--creates and inits the msi
	void createMSI(int suiteID) throws IOException {
		MIDletSuiteInfo temp =
			MIDletSuiteStorage.getMIDletSuiteStorage().getMIDletSuiteInfo(suiteID);
		msi = new RunningMIDletSuiteInfo(temp, MIDletSuiteStorage.getMIDletSuiteStorage(), true); }
		
	// Used to read from the stream (assumes type specifier has already
	// been read)
	AMSMidletCustomItem(DataInputStream istream, AppManagerUI ams) throws IOException {
		super(null, ams);
		String n = istream.readUTF();
		int sid = istream.readInt();
		createMSI(sid);
		text = n.toCharArray();
		textLen = text.length; }

	// Accessor--TODO--See if it's now obsolete
	int getSuiteID() {
		return msi.suiteId; }

	// Write to stream
	void write(DataOutputStream ostream) throws IOException {
		// We save the text so we can allow 
		// users to rename Midlets
		ostream.writeUTF(new String(text));
		ostream.writeInt(msi.suiteId); }

	protected boolean traverse(int dir,
		int viewportWidth, int viewportHeight,
		int visRect_inout[]) {
		// entirely visible and hasFocus
		if (!hasFocus) {
			hasFocus = true;
			owner.setLastSelectedMsi(this.msi); }
		return super.traverse(dir, viewportWidth, viewportHeight, visRect_inout); }

	/**
	* Called when MidletCustomItem is shown.
	*/
	public void showNotify() {
	
		// Unfortunately there is no Form.showNotify  method where
		// this could have been done.
		
		// When icon for the Installer
		// is shown we want to make sure
		// that there are no running midlets from the "internal" suite.
		// The only 2 midlets that can run in bg from
		// "internal" suite are the DiscoveryApp and the Installer.
		// Icon for the Installer will be shown each time
		// the AppSelector is made current since it is the top
		// most icon and we reset the traversal to start from the top
		if (msi.suiteId == MIDletSuite.INTERNAL_SUITE_ID &&
			owner.appManagerMidlet != null) {
			owner.appManagerMidlet.destroyMidlet(); } }
			
	/* Override--called by paint(...) */
	void setLabelColor(Graphics g) {
		int color;
		if (msi.proxy == null) {
			color = hasFocus ? ICON_HL_TEXT : ICON_TEXT; }
		else {
			color = hasFocus ?
				ICON_RUNNING_HL_TEXT : ICON_RUNNING_TEXT; }
		g.setColor(color); }
		
	/* Override--called by paint(...) */
	void drawIcons(Graphics g) {
	
		// TODO: Provide system icon for those we don't have
		if (icon != null) {
			g.drawImage(icon, (bgIconW - icon.getWidth())/2,
			(bgIconH - icon.getHeight())/2,
			Graphics.TOP | Graphics.LEFT); }

		// Draw special icon if user attention is requested and
		// that midlet needs to be brought into foreground by the user
		if (msi.proxy != null && msi.proxy.isAlertWaiting()) {
			g.drawImage(FG_REQUESTED,
			bgIconW - FG_REQUESTED.getWidth(), 0,
			Graphics.TOP | Graphics.LEFT); }
	
		if (!msi.enabled) {
			// indicate that this suite is disabled
			g.drawImage(DISABLED_IMAGE,
			(bgIconW - DISABLED_IMAGE.getWidth())/2,
			(bgIconH - DISABLED_IMAGE.getHeight())/2,
			Graphics.TOP | Graphics.LEFT); } }
			
	// Determine if the associated midlet is running
	boolean isRunning() {
		return (msi.proxy != null); }
			
	/* Override */		
	void updateCommands() {
		if (isRunning()) {
			addCommand(fgCmd);
			setDefaultCommand(fgCmd);
			addCommand(endCmd);
			removeCommand(launchCmd);
			return; }
		removeCommand(fgCmd);
		removeCommand(endCmd);
		if (msi.enabled) {
			addCommand(launchCmd);
			setDefaultCommand(launchCmd);
			return; }
		removeCommand(launchCmd); }
	
}
