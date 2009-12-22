/*
 * CustomItem representing the vm configuration panel in the AMS. 
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
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import com.sun.midp.installer.GraphicalInstaller;

class AMSMidletCustomItem_VMConfigPanel extends AMSMidletCustomItem {
	// Just has a slightly different set of commands--otherwise, it's a standard
	// AMSCustomItem
	/** Command object for Launch vm config panel app. */
	static final Command launchVMCPanelCmd =
		new Command(Resource.getString(ResourceConstants.LAUNCH),
		Command.ITEM, 1);
	/** Constant for the vm config panel application class name. */
	private static final String VM_CONFIG_APP =
		"com.pspkvm.system.VMConfigMidlet";

	final static Image vmsImg =
		GraphicalInstaller.getImageFromInternalStorage("vmsettings");
	final static int vmsImgW = vmsImg.getWidth();
	final static int vmsImgH = vmsImg.getHeight();

	AMSMidletCustomItem_VMConfigPanel(RunningMIDletSuiteInfo msi, AppManagerUI ams,
		AMSFolderCustomItem p) {
		super(msi, ams, p); }

	/* Override */		
	void updateCommands() {
		if (isRunning()) {
			addCommand(fgCmd);
			setDefaultCommand(fgCmd);
			removeCommand(launchVMCPanelCmd);
			return; }
		removeCommand(fgCmd);
		removeCommand(endCmd);
		if (msi.enabled) {
			addCommand(launchVMCPanelCmd);
			setDefaultCommand(launchVMCPanelCmd);
			return; }
		removeCommand(launchVMCPanelCmd); }

	// None of the standard commands are allowed here
	void setFixedCommands() {}
		
	// You can't mark this, either
	boolean allowsMark() {
		return false; }

	// Constructs the AMSMidletCustomItem containing the vm config panel midlet
	static AMSMidletCustomItem getVMSPanelCI(AppManagerUI ams, AMSFolderCustomItem p) {
		RunningMIDletSuiteInfo msi =
			new RunningMIDletSuiteInfo(MIDletSuite.INTERNAL_SUITE_ID,
				VM_CONFIG_APP,
				Resource.getString(ResourceConstants.CONFIGURE_VM),
				true);
		AMSMidletCustomItem r = new AMSMidletCustomItem_VMConfigPanel(msi, ams, p);
		r.setDefaultCommand(launchVMCPanelCmd);
		return r; }
	
	// Override, to draw pretty wrench icon
	void drawIcons(Graphics g) {
		g.drawImage(vmsImg, indent + (bgIconW - vmsImgW)/2,
			(bgIconH - vmsImgH)/2,
			Graphics.TOP | Graphics.LEFT);
		// Draw alert icon
		if (msi.proxy != null && msi.proxy.isAlertWaiting()) {
			g.drawImage(FG_REQUESTED,
			indent + bgIconW - FG_REQUESTED.getWidth(), 0,
			Graphics.TOP | Graphics.LEFT); } }
}
