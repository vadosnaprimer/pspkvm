/*
	CustomItem representing a midlet in the AMS. 
*/

package com.sun.midp.appmanager;

import java.io.*;
import com.sun.midp.midletsuite.*;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import com.sun.midp.midlet.MIDletSuite;

class AMSMidletCustomItem extends AMSCustomItem {

	/** The MIDletSuiteInfo associated with this MidletCustomItem */
  RunningMIDletSuiteInfo msi; // = null

	/**
	 * The icon to be used to draw this midlet representation.
	 */
	Image icon; // = null

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

	int getSuiteID() {
		return msi.suiteId; }

	void write(DataOutputStream ostream) throws IOException {
		ostream.writeByte((byte)TYPE_MIDLET);
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

}
