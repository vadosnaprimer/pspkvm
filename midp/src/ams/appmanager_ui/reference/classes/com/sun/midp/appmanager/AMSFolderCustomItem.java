/*
 * CustomItem representing a folder in the AMS. 
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
import com.sun.midp.installer.GraphicalInstaller;
import com.sun.midp.midletsuite.*;
import com.sun.midp.main.*;
import javax.microedition.lcdui.Command;

class AMSFolderCustomItem extends AMSCustomItem {
	AMSFolderCustomItem[] subfolders;
	AMSMidletCustomItem[] items;
	boolean open;
	AMSFolderCustomItem parent;
	
	static final Command openFolderCmd =
		new Command("Open", Command.ITEM, 7);
	static final Command closeFolderCmd =
		new Command("Close", Command.ITEM, 7);
	static final Command createSubfolderCmd =
		new Command("New subfolder", Command.ITEM, 7);
	static final Command removeCmd = 
		new Command("Remove", Command.ITEM, 7);

	// Constructor for creation in UI, sans content
	AMSFolderCustomItem(String n, AMSFolderCustomItem p, AppManagerUI ams) {
		super(null, ams);
		text = n.toCharArray();
		textLen = n.length();
		open = false;
		subfolders = new AMSFolderCustomItem[0];
		items = new AMSMidletCustomItem[0];
		parent=p;
		if (parent == null) {
			// You can't remove or rename the root
			removeCommand(removeCmd);
			removeCommand(renameCmd); } }
	
	// Open the folder in the AMS UI--displays all its content (if any)
	void setOpen() {
		if (open) {
			return; }
		insertContentsNoCheck();
		open=true;
		updateDisplay(); }

	// Called when contents change (add/delete) to make sure
	// the proper items get added to the parent
	void updateContentDisplay() {
		if (!open) {
			return; }
		hideContents();
		insertContentsNoCheck(); }

	// Special method called by AMS when inserting folders--
	// since if they're open already, you should also insert
	// their contents
	void insertContents() {
		if (!open) {
			return; }
		insertContentsNoCheck(); }
		
	void insertContentsNoCheck() {
		int idx = owner.getIndexOf(this);
		if (idx==-1) {
			// This really shouldn't happen. But really,
			// the logical assumption is this item is itself
			// hidden. So do nothing.
			return; }
		int c = subfolders.length;
		for(int i=0; i<c; i++) {
			subfolders[i].updateDisplay();
			idx++;
			owner.insertFolderAt(idx, subfolders[i]); }
		c = items.length;
		for(int i=0; i<c; i++) {
			items[i].updateDisplay();
			idx++;
			owner.insertAt(idx, items[i]); } }
		
	// Close the folder in the AMS UI--hides all its content (if any)
	void setClosed() {
		if (!open) {
			return; }
		hideContents();
		open=false;
		updateDisplay(); }
		
	void hideContents() {
		int c = subfolders.length;
		for(int i=0; i<c; i++) {
			subfolders[i].setClosed();
			subfolders[i].hide(); }
		c = items.length;
		for(int i=0; i<c; i++) {
			items[i].hide(); } }
			
	void hide() {
		if (open) {
			hideContents(); }
		super.hide(); }

	// Read the root from storage
	static AMSFolderCustomItem readRoot(DataInputStream di, AppManagerUI ams)
		throws IOException {
		return new AMSFolderCustomItem(null, di, ams); }

	// Create the root from a list of installed midlets--usually
	// just called the first time the folder system runs.
	static AMSFolderCustomItem createRoot(AppManagerUI ams) {
		AMSFolderCustomItem userroot = new AMSFolderCustomItem("Installed midlets", null, ams);
		userroot.open=false;
		userroot.subfolders=new AMSFolderCustomItem[0];
		userroot.populateRootFromIntalledMidlets();
		userroot.sort();
		return userroot; }
		
	// Sort the contents of the folder alphabetically
	void sort() {
		QuicksortAlg.sort(subfolders);
		QuicksortAlg.sort(items); }

	// Populate the root (user) list from the list of installed midlets--
	// helper for createRoot(..).
	void populateRootFromIntalledMidlets() {
		int[] suiteIds = MIDletSuiteStorage.getMIDletSuiteStorage().getListOfSuites();
		installFromList(suiteIds); }
		
	void installFromList(int[] suiteIds) {
		Vector r = new Vector(suiteIds.length);
		// Add the rest of the installed midlets
		for (int i = 0; i < suiteIds.length; i++) {
			try {
				r.addElement(new AMSMidletCustomItem(suiteIds[i], owner, this)); }
			catch (Exception e) {
				// move on to the next suite
				// TODO? Why would this fail?
			} }
		setItemsFromVector(r); }
		
	// Find midlets for which we have install information, but no
	// point of attachment on the tree (typically, after install)
	int[] findUnattachedMidlets() {
		int[] suiteIds = MIDletSuiteStorage.getMIDletSuiteStorage().getListOfSuites();
		Vector rt = new Vector(2);
		for(int i=0; i<suiteIds.length; i++) {
			if (find(suiteIds[i])==null) {
				rt.addElement(new Integer(suiteIds[i])); } }
		int[] r = new int[rt.size()];
		for(int i=0; i<r.length; i++) {
			r[i] = ((Integer)rt.elementAt(i)).intValue(); }
		return r; }
		
	boolean addUnattachedMidlets() {
		int[] suiteIds=findUnattachedMidlets();
		boolean r = (suiteIds.length > 0);
		installFromList(suiteIds);
		return r; }

	// Constructor for creation from storage		
	AMSFolderCustomItem(AMSFolderCustomItem p, DataInputStream di,
	AppManagerUI ams)
		throws IOException {
		super(null, ams);
		text=di.readUTF().toCharArray();
		textLen = text.length;
		open = di.readBoolean();
		parent=p;
		int sfsize = di.readInt();
		subfolders = new AMSFolderCustomItem[sfsize];
		for(int i=0; i<sfsize; i++) {
			subfolders[i] = new AMSFolderCustomItem(this, di, owner); }
		int isize = di.readInt();
		items = new AMSMidletCustomItem[isize];
		for(int i=0; i<isize; i++) {
			items[i] = new AMSMidletCustomItem(di, owner, this); }
		if (parent == null) {
			// You can't remove the root
			removeCommand(removeCmd);
			removeCommand(renameCmd); } }

	// Write to storage		
	void write(DataOutputStream ostream) throws IOException {
		ostream.writeUTF(new String(text));
		ostream.writeBoolean(open);
		int c = subfolders.length;
		ostream.writeInt(c);
		for(int i=0; i<c; i++) {
			subfolders[i].write(ostream); }
		c = items.length;
		ostream.writeInt(c);
		for(int i=0; i<c; i++) {
			items[i].write(ostream); } }
	
	/* Override */
	void setLabelColor(Graphics g) {
		g.setColor(hasFocus ? ICON_HL_TEXT : ICON_TEXT); }

	/* Override */
	void drawIcons(Graphics g) {
		Image i = open ? folderOpenImg : folderImg;
		g.drawImage(i, (bgIconW - i.getWidth())/2,
 			(bgIconH - i.getHeight())/2,
			Graphics.TOP | Graphics.LEFT); }
	
	/* Override */		
	void updateCommands() {
		if (open) {
			removeCommand(openFolderCmd);
			addCommand(closeFolderCmd);
			setDefaultCommand(closeFolderCmd);
			return; }
		// Closed ...
		removeCommand(closeFolderCmd);
		addCommand(openFolderCmd);
		setDefaultCommand(openFolderCmd); }
	
	// Find the AMSMCI matching a given MIDletProxy in the tree
	// below this folder, or null, if it's not there.
	// (Thus, searching from the root initially usually makes sense.)
	AMSMidletCustomItem find(MIDletProxy midlet) {
		int c = items.length;
		for(int i=0; i<c; i++) {
			if (items[i].msi.equals(midlet)) {
				return items[i]; } }
		c = subfolders.length;
		for(int i=0; i<c; i++) {
			AMSMidletCustomItem a = subfolders[i].find(midlet);
			if (a != null) {
				return a; } }
		return null; }
		
	// Find the AMSMCI matching a given suiteID in the tree
	// below this folder, or null, if it's not there.
	// (Thus, searching from the root initially usually makes sense.)
	AMSMidletCustomItem find(int suiteID) {
		int c = items.length;
		for(int i=0; i<c; i++) {
			if (items[i].getSuiteID()==suiteID) {
				return items[i]; } }
		c = subfolders.length;
		for(int i=0; i<c; i++) {
			AMSMidletCustomItem a = subfolders[i].find(suiteID);
			if (a != null) {
				return a; } }
		return null; }
		
	// Find the AMSMCI matching a given RunningMidletSuiteInfo in the tree
	// below this folder, or null, if it's not there.
	// (Thus, searching from the root initially usually makes sense.)
	AMSMidletCustomItem find(RunningMIDletSuiteInfo rmsi) {
		int c = items.length;
		for(int i=0; i<c; i++) {
			if (items[i].msi==rmsi) {
				return items[i]; } }
		c = subfolders.length;
		for(int i=0; i<c; i++) {
			AMSMidletCustomItem a = subfolders[i].find(rmsi);
			if (a != null) {
				return a; } }
		return null; }
		
	// We keep the subfolders and items in arrays (not resizeable
	// containers) for efficiency, but it does mean some work to 
	// to do inserts/removes, so on. Stuff below makes it easier.
	protected Vector itemsAsVector() {
		int c = items.length;
		Vector r = new Vector(c);
		for(int i=0; i<c; i++) {
			r.addElement(items[i]); }
		return r; }
		
	protected void setItemsFromVector(Vector v) {
		int c = v.size();
		items = new AMSMidletCustomItem[c];
		for(int i=0; i<c; i++) {
			items[i]=(AMSMidletCustomItem)(v.elementAt(i)); } }
	
	void insert(AMSMidletCustomItem m, int pos) {
		Vector v = itemsAsVector();
		if (pos<0) {
			return; }
		if (pos>=items.length) {
			v.addElement(m); }
		else {
			v.insertElementAt(m, pos); }
		setItemsFromVector(v);
		// TODO: Additional stuff?
	}
	
	void append(AMSMidletCustomItem m) {
		insert(m, items.length); }
		
	void remove(AMSMidletCustomItem m) {
		Vector v = itemsAsVector();
		v.removeElement(m);
		setItemsFromVector(v); }
		
	int getPos(AMSMidletCustomItem m) {
		int c=items.length;
		for(int i=0; i<c; i++) {
			if (m==items[i]) {
				return i; } }
		return -1; }
		
	void moveDown(AMSMidletCustomItem m) {
		int p = getPos(m);
		if (p==-1) {
			return; }
		int c = items.length;
		if (p>=(c-1)) {
			return; }
		remove(m);
		insert(m, p+1); }

	void moveUp(AMSMidletCustomItem m) {
		int p = getPos(m);
		if (p<=1) {
			return; }
		remove(m);
		insert(m, p-1); }

	protected Vector subfoldersAsVector() {
		int c = subfolders.length;
		Vector r = new Vector(c);
		for(int i=0; i<c; i++) {
			r.addElement(subfolders[i]); }
		return r; }
		
	protected void setSubfoldersFromVector(Vector v) {
		int c = v.size();
		subfolders = new AMSFolderCustomItem[c];
		for(int i=0; i<c; i++) {
			subfolders[i]=(AMSFolderCustomItem)(v.elementAt(i)); } }
	
	void insert(AMSFolderCustomItem f, int pos) {
		Vector v = subfoldersAsVector();
		if (pos<0) {
			return; }
		if (pos>=subfolders.length) {
			v.addElement(f); }
		else {
			v.insertElementAt(f, pos); }
		setSubfoldersFromVector(v);
		// TODO: Additional stuff?
	}

	void append(AMSFolderCustomItem f) {
		insert(f, subfolders.length); }

	void remove(AMSFolderCustomItem m) {
		Vector v = subfoldersAsVector();
		v.removeElement(m);
		setSubfoldersFromVector(v); }

	// Overrides std implementation to make sure
	// contained items currently displayed
	// also get their update
	void updateDisplay() {
		super.updateDisplay();
		updateDisplayForContents(); }
		
	void updateDisplayForContents() {
		if (!open) {
			return; }
		int c = subfolders.length;
		for(int i=0; i<c; i++) {
			subfolders[i].updateDisplay(); }
		c = items.length;
		for(int i=0; i<c; i++) {
			items[i].updateDisplay(); } }
			
	void setFixedCommands() {
		super.setFixedCommands();
		addCommand(createSubfolderCmd);
		addCommand(removeCmd); }
}

