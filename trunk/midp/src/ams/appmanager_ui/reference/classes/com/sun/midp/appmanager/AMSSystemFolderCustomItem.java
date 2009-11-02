/*
 * CustomItem representing the system folder in the AMS. 
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

import java.util.Vector;
import java.io.*;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import com.sun.midp.midletsuite.*;
import com.sun.midp.main.*;

class AMSSystemFolderCustomItem extends AMSFolderCustomItem {

	// Handy reference to the discovery midlet--having this
	// explicitly available speeds a few things along in the AMS UI
	AMSMidletCustomItem discoveryMidlet, wifiMidlet, vmConfigMidlet;
	
	// Constructor for creation in UI, sans content
	AMSSystemFolderCustomItem(String n, AMSFolderCustomItem p, AppManagerUI ams) {
		super(n, p, ams, 0);
		removeCommand(moveHereCmd);
		removeCommand(createSubfolderCmd);
		removeCommand(markAllCmd);
		createBuiltInMidlets(); }

	// Create the built-in midlets
	void createBuiltInMidlets() {
		discoveryMidlet = AMSMidletCustomItem_Installer.getDiscoveryMidletCI(owner, this);
		wifiMidlet = AMSMidletCustomItem_WifiManager.getWiFiMidletCI(owner, this);
		vmConfigMidlet = AMSMidletCustomItem_VMConfigPanel.getVMSPanelCI(owner, this); }

	// Create the system folder
	static AMSSystemFolderCustomItem createSystemRoot(AppManagerUI ams) {
		AMSSystemFolderCustomItem sysroot =
			new AMSSystemFolderCustomItem("System midlets", null, ams);
		sysroot.open=true;
		return sysroot; }

	// Convenience method--called from AMS UI
	boolean isInstallerRunning() {
		RunningMIDletSuiteInfo msi = discoveryMidlet.msi;
		if (msi.proxy != null && 
			(AMSMidletCustomItem_Installer.DISCOVERY_APP.equals(msi.midletToRun) ||
			AMSMidletCustomItem_Installer.INSTALLER.equals(msi.midletToRun))) {
				return true; }
		return false; }
		
	// Override
	void insertContentsNoCheck() {
		int idx = owner.getIndexOf(this);
		if (idx==-1) {
			// This really shouldn't happen
			return; }
		idx++;
		owner.insertAt(idx, discoveryMidlet);
		idx++;
		owner.insertAt(idx, wifiMidlet);
		idx++;
		owner.insertAt(idx, vmConfigMidlet);
		int c = subfolders.length;
		for(int i=0; i<c; i++) {
			subfolders[i].updateDisplay();
			idx++;
			owner.insertFolderAt(idx, subfolders[i]); }
		c = items.length;
		for(int i=0; i<c; i++) {
			items[i].updateDisplay();
			idx++;
			owner.insertAt(idx, items[i]); }
		open=true; }

	// Override
	void setClosed() {
		if (!open) {
			return; }
		discoveryMidlet.hide();
		wifiMidlet.hide();
		vmConfigMidlet.hide();
		super.setClosed(); }
		
	// Override
	void updateDisplayForContents() {
		if (!open) {
			return; }
		discoveryMidlet.updateDisplay();
		wifiMidlet.updateDisplay();
		vmConfigMidlet.updateDisplay();
		super.updateDisplayForContents(); }

	// Override
	AMSMidletCustomItem find(MIDletProxy midlet) {
			if (discoveryMidlet.msi.equals(midlet)) {
				return discoveryMidlet; }
			if (wifiMidlet.msi.equals(midlet)) {
				return wifiMidlet; }
			if (vmConfigMidlet.msi.equals(midlet)) {
				return vmConfigMidlet; }
		return super.find(midlet); }

}
