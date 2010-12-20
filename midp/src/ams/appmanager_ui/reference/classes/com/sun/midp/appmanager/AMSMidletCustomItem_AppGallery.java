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
 * Original code M@x / 2010
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

class AMSMidletCustomItem_AppGallery extends AMSMidletCustomItem {
	// Just has a slightly different set of commands--otherwise, it's a standard
	// AMSCustomItem
	/** Command object for Launch vm config panel app. */
	static final Command launchAppGalleryCmd =
		new Command(Resource.getString(ResourceConstants.LAUNCH),
		Command.ITEM, 1);
	/** Constant for the vm config panel application class name. */
	private static final String APPGALLERY_APP =
		"com.sun.midp.installer.AppGallery";

	final static Image vmsImg =
		GraphicalInstaller.getImageFromInternalStorage("appgal");
	static int vmsImgW = 0;
	static int vmsImgH = 0;

	AMSMidletCustomItem_AppGallery(RunningMIDletSuiteInfo msi, AppManagerUI ams,
		AMSFolderCustomItem p) {
		super(msi, ams, p);
		if (vmsImg != null) {
			vmsImgW = vmsImg.getWidth();
			vmsImgH = vmsImg.getHeight();
		}
	}

	/* Override */		
	void updateCommands() {
		if (isRunning()) {
			addCommand(fgCmd);
			setDefaultCommand(fgCmd);
			removeCommand(launchAppGalleryCmd);
			return; }
		removeCommand(fgCmd);
		removeCommand(endCmd);
		if (msi.enabled) {
			addCommand(launchAppGalleryCmd);
			setDefaultCommand(launchAppGalleryCmd);
			return; }
		removeCommand(launchAppGalleryCmd); }

	// None of the standard commands are allowed here
	void setFixedCommands() {}
		
	// You can't mark this, either
	boolean allowsMark() {
		return false; }

	// Constructs the AMSMidletCustomItem containing the appgallery midlet
	static AMSMidletCustomItem getAppGalleryCI(AppManagerUI ams, AMSFolderCustomItem p) {
		RunningMIDletSuiteInfo msi =
			new RunningMIDletSuiteInfo(MIDletSuite.INTERNAL_SUITE_ID,
				APPGALLERY_APP,
				Resource.getString(ResourceConstants.APP_GALLERY),
				true);
		AMSMidletCustomItem r = new AMSMidletCustomItem_AppGallery(msi, ams, p);
		r.setDefaultCommand(launchAppGalleryCmd);
		return r; }
	
	// Override, to draw pretty wrench icon
	void drawIcons(Graphics g) {
		if (vmsImg == null) return;
		
		g.drawImage(vmsImg, indent + (bgIconW - vmsImgW)/2,
			(bgIconH - vmsImgH)/2,
			Graphics.TOP | Graphics.LEFT);
		// Draw alert icon
		if (msi.proxy != null && msi.proxy.isAlertWaiting()) {
			g.drawImage(FG_REQUESTED,
			indent + bgIconW - FG_REQUESTED.getWidth(), 0,
			Graphics.TOP | Graphics.LEFT); } }
}

