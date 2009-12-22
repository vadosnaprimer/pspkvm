/**
 *
 *  CustomItem representing a midlet in the AMS.
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
	/** Command object for "Info". */
	static final Command infoCmd =
		new Command(Resource.getString(ResourceConstants.INFO),
			Command.ITEM, 2);
	/** Command object for "Remove". */
	static final Command removeCmd =
		new Command(Resource.getString(ResourceConstants.REMOVE),
			Command.ITEM, 3);
	/** Command object for "Update". */
	static final Command updateCmd =
		new Command(Resource.getString(ResourceConstants.UPDATE),
			Command.ITEM, 4);
	/** Command object for "Application settings". */
	static final Command appSettingsCmd =
		new Command(Resource.getString(ResourceConstants.APPLICATION_SETTINGS),
			Command.ITEM, 5);
	/** Command object for "Select device". */
	static final Command deviceSettingCmd =
		new Command(Resource.getString(ResourceConstants.SELECT_DEVICE),
			Command.ITEM, 6);

	/** The MIDletSuiteInfo associated with this MidletCustomItem */
  RunningMIDletSuiteInfo msi; // = null
  /** The parent folder */
  AMSFolderCustomItem parent;
  
	/**
	 * The icon to be used to draw this midlet representation.
	 */
	Image icon; // = null

	/**
	* Constructs a midlet representation for the App Selector Screen.
	* @param msi The MIDletSuiteInfo for which representation has
	*            to be created
	*/
	AMSMidletCustomItem(RunningMIDletSuiteInfo msi, AppManagerUI ams, AMSFolderCustomItem p) {
		super(null, ams, p.depth+1);
		this.msi = msi;
		parent=p;
		icon = msi.icon;
		text = msi.displayName.toCharArray();
		textLen = msi.displayName.length(); }

	// Same from just the suiteID--used by initial folder creation code.
	AMSMidletCustomItem(int suiteID, AppManagerUI ams, AMSFolderCustomItem p) throws IOException {
		super(null, ams, p.depth+1);
		createMSI(suiteID);
		icon = msi.icon;
		parent=p;
		text = msi.displayName.toCharArray();
		textLen = msi.displayName.length(); }
		
	// Helper for constructors--creates and inits the msi
	void createMSI(int suiteID) throws IOException, IllegalArgumentException {
		MIDletSuiteInfo temp =
			MIDletSuiteStorage.getMIDletSuiteStorage().getMIDletSuiteInfo(suiteID);
		msi = new RunningMIDletSuiteInfo(temp, MIDletSuiteStorage.getMIDletSuiteStorage(), true) {
					public boolean equals(MIDletProxy midlet) {
						return super.equals(midlet); } } ; }
						
	// Used to read from the stream (assumes type specifier has already
	// been read)
	AMSMidletCustomItem(DataInputStream istream, AppManagerUI ams,
		AMSFolderCustomItem p) throws IOException, IllegalArgumentException {
		super(null, ams, p.depth+1);
		String n = istream.readUTF();
		int sid = istream.readInt();
		createMSI(sid);
		icon = msi.icon;
		parent=p;
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
			g.drawImage(icon, indent + (bgIconW - icon.getWidth())/2,
			(bgIconH - icon.getHeight())/2,
			Graphics.TOP | Graphics.LEFT); }

		// Draw special icon if user attention is requested and
		// that midlet needs to be brought into foreground by the user
		if (msi.proxy != null && msi.proxy.isAlertWaiting()) {
			g.drawImage(FG_REQUESTED,
			indent + bgIconW - FG_REQUESTED.getWidth(), 0,
			Graphics.TOP | Graphics.LEFT); }
	
		if (!msi.enabled) {
			// indicate that this suite is disabled
			g.drawImage(DISABLED_IMAGE,
			indent + (bgIconW - DISABLED_IMAGE.getWidth())/2,
			(bgIconH - DISABLED_IMAGE.getHeight())/2,
			Graphics.TOP | Graphics.LEFT); } }
			
	// Determine if the associated midlet is running
	boolean isRunning() {
		return (msi.proxy != null); }
		
	// Remove this item from its parent
	void remove() {
		hide();
		parent.remove(this); }
		
	/* These commands don't come and go--they're set at
		construction */
	void setFixedCommands() {
		super.setFixedCommands();
		addCommand(infoCmd);
		addCommand(removeCmd);
		addCommand(updateCmd);
		addCommand(appSettingsCmd);
		addCommand(deviceSettingCmd); }
			
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
		
	void ensureVisible() {
		parent.openFromBottom(); }
		
	void select() {
		ensureVisible();
		owner.display.setCurrentItem(this); }
		
}
