/*
 * CustomItem representing the installer in the AMS. 
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
import com.sun.midp.installer.GraphicalInstaller;
import com.sun.midp.main.MIDletProxy;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

class AMSMidletCustomItem_Installer extends AMSMidletCustomItem {
	// Just has a slightly different set of commands--otherwise, it's a standard
	// AMSCustomItem
	/** Command object for "Launch" WiFi setup app. */
	/** Constant for the graphical installer class name. */
	static final String INSTALLER =
		"com.sun.midp.installer.GraphicalInstaller";
	/** Constant for the discovery application class name. */
	static final String DISCOVERY_APP =
		"com.sun.midp.installer.DiscoveryApp";
	/** Command object for "Launch" install app. */
	static final Command launchInstallCmd =
		new Command(Resource.getString(ResourceConstants.LAUNCH),
		Command.ITEM, 1);

	final static Image installerImg =
		GraphicalInstaller.getImageFromInternalStorage("_installer");
	final static int installerImgW = installerImg.getWidth();
	final static int installerImgH = installerImg.getHeight();
		
	AMSMidletCustomItem_Installer(RunningMIDletSuiteInfo msi, AppManagerUI ams,
		AMSFolderCustomItem p) {
		super(msi, ams, p); }

	// None of the standard commands are allowed here
	void setFixedCommands() {}

	// You can't mark this, either
	boolean allowsMark() {
		return false; }

	/* Override */		
	void updateCommands() {
		if (isRunning()) {
			addCommand(fgCmd);
			setDefaultCommand(fgCmd);
			addCommand(endCmd);
			removeCommand(launchInstallCmd);
			return; }
		removeCommand(fgCmd);
		removeCommand(endCmd);
		if (msi.enabled) {
			addCommand(launchInstallCmd);
			setDefaultCommand(launchInstallCmd);
			return; }
		removeCommand(launchInstallCmd); }

	// Constructs the AMSMidletCustomItem containing the discovery midlet
	static AMSMidletCustomItem getDiscoveryMidletCI(AppManagerUI ams,
		AMSFolderCustomItem p) {
		RunningMIDletSuiteInfo msi =
			new RunningMIDletSuiteInfo(MIDletSuite.INTERNAL_SUITE_ID,
		    DISCOVERY_APP,
		    Resource.getString(ResourceConstants.INSTALL_APPLICATION),
				true) {
					public boolean equals(MIDletProxy midlet) {
						if (super.equals(midlet)) {
							return true; }
						return (INSTALLER.equals(midlet.getClassName())); } };
		AMSMidletCustomItem r = new AMSMidletCustomItem_Installer(msi, ams, p);
		r.setDefaultCommand(launchInstallCmd);
		return r; }

	// Override, to draw pretty installer icon
	void drawIcons(Graphics g) {
		g.drawImage(installerImg, indent + (bgIconW - installerImgW)/2,
			(bgIconH - installerImgH)/2,
			Graphics.TOP | Graphics.LEFT);
		// Draw alert icon
		if (msi.proxy != null && msi.proxy.isAlertWaiting()) {
			g.drawImage(FG_REQUESTED,
			indent + bgIconW - FG_REQUESTED.getWidth(), 0,
			Graphics.TOP | Graphics.LEFT); } }

}
