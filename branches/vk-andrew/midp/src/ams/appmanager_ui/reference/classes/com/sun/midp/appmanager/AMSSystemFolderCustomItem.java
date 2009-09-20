/*
	CustomItem representing a folder in the AMS. 
*/

package com.sun.midp.appmanager;

import java.util.Vector;
import java.io.*;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import com.sun.midp.installer.GraphicalInstaller;
import com.sun.midp.midletsuite.*;
import com.sun.midp.main.*;

class AMSSystemFolderCustomItem extends AMSFolderCustomItem {

	// Handy reference to the discovery midlet--having this
	// explicitly available speeds a few things
	// along in the AMS UI
	AMSMidletCustomItem discoveryMidlet, wifiMidlet;
	
	// Constructor for creation in UI, sans content
	AMSSystemFolderCustomItem(String n, AMSFolderCustomItem p, AppManagerUI ams) {
		super(n, p, ams);
		createBuiltInMidlets(); }

	// Create the built-in midlets
	void createBuiltInMidlets() {
		discoveryMidlet = AMSMidletCustomItem.getDiscoveryMidletCI(owner);
		wifiMidlet = AMSMidletCustomItem_WifiManager.getWiFiMidletCI(owner); }

	// Create the system folder
	static AMSSystemFolderCustomItem createSystemRoot(AppManagerUI ams) {
		AMSSystemFolderCustomItem sysroot =
			new AMSSystemFolderCustomItem("System midlets", null, ams);
		// sysroot.open=false;
		// TODO: Test code
		sysroot.open=true;
		return sysroot; }

	// Convenience method--called from AMS UI
	boolean isInstallerRunning() {
		RunningMIDletSuiteInfo msi = discoveryMidlet.msi;
		if (msi.proxy != null && 
			(AMSMidletCustomItem.DISCOVERY_APP.equals(msi.midletToRun) ||
			AMSMidletCustomItem.INSTALLER.equals(msi.midletToRun))) {
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
		super.setClosed(); }
		
	// Override
	void updateDisplayForContents() {
		if (!open) {
			return; }
		discoveryMidlet.updateDisplay();
		wifiMidlet.updateDisplay();
		super.updateDisplayForContents(); }

	// Override
	AMSMidletCustomItem find(MIDletProxy midlet) {
		if (discoveryMidlet.msi.proxy != null) {
			if (discoveryMidlet.msi.proxy.equals(midlet)) {
				return discoveryMidlet; } }
		if (wifiMidlet.msi.proxy != null) {
			if (wifiMidlet.msi.proxy.equals(midlet)) {
				return wifiMidlet; } }
		return super.find(midlet); }

}
