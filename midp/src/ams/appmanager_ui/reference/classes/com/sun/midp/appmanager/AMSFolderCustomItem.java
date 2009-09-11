/*
	CustomItem representing a folder in the AMS. 
*/

package com.sun.midp.appmanager;

import java.util.Vector;
import java.io.*;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import com.sun.midp.installer.GraphicalInstaller;

class AMSFolderCustomItem extends AMSCustomItem {
	Vector contents;
	boolean open;
	AMSFolderCustomItem parent;
	
	// Constructor for creation in UI, sans content
	AMSFolderCustomItem(String n, AMSFolderCustomItem p, AppManagerUI ams) {
		super(null, ams);
		text = n.toCharArray();
		textLen = n.length();
		open = false;
		contents = new Vector();
		parent=p; }
		
	// Read the root from storage
	static AMSFolderCustomItem readRoot(DataInputStream di, Vector mcis, AppManagerUI ams)
		throws IOException {
		int t = (int)(di.readByte());
		if (t != TYPE_FOLDER) {
			throw new IOException("Root was not folder--unexpected type field: " + t); }
		return new AMSFolderCustomItem(null, di, mcis, ams); }
		
	// Create the root from a list of midlets--usually just called first time
	// the folder system runs.
	static AMSFolderCustomItem createRoot(Vector mcis, AppManagerUI ams) {
		AMSFolderCustomItem root = new AMSFolderCustomItem("root", null, ams);
		root.open=true;
		root.contents=mcis;
		return root; }

	// Constructor for creation from storage		
	AMSFolderCustomItem(AMSFolderCustomItem p, DataInputStream di,
		Vector mcis, AppManagerUI ams)
		throws IOException {
		super(null, ams);
		text=di.readUTF().toCharArray();
		textLen = text.length;
		open = false;
		parent=p;
		int csize = di.readInt();
		contents = new Vector(csize);
		for(int i=0; i<csize; i++) {
			contents.addElement(readNextCI(di, mcis)); } }

	// Write to storage		
	void write(DataOutputStream ostream) throws IOException {
		ostream.writeByte((byte)(AMSCustomItem.TYPE_FOLDER));
		ostream.writeUTF(new String(text));
		int c = contents.size();
		ostream.writeInt(c);
		for(int i=0; i<c; i++) {
			AMSCustomItem wamci = (AMSCustomItem)(contents.elementAt(i));
			wamci.write(ostream); } }
	
	// Helper for construction from storage
	AMSCustomItem readNextCI(DataInputStream di, Vector mcis)
		throws IOException {
		int t = (int)(di.readByte());
		switch(t) {
				case TYPE_FOLDER:
					AMSFolderCustomItem afci = new AMSFolderCustomItem(this, di, mcis, owner);
					return afci;
				case TYPE_MIDLET:
					int suiteID = di.readInt();
					return getMidletSuiteByID(mcis, suiteID);
				default:
					throw new IOException("Unexpected type field in AMSFolderCustomItem:" + t); } }
	
	// Finds a AMSCustomItem in a Vector containing these, by its suiteID
	static AMSCustomItem getMidletSuiteByID(Vector mcis, int suiteID) {
		int s = mcis.size();
		for (int i=0; i<s; i++) {
			AMSMidletCustomItem amci = (AMSMidletCustomItem)(mcis.elementAt(i));
			if (amci.getSuiteID()==suiteID) {
				return amci; } }
		// Error. Throw.
		throw new ArrayIndexOutOfBoundsException("No such suiteID"); }
	
	/* Override */
	void setLabelColor(Graphics g) {
		g.setColor(hasFocus ? ICON_HL_TEXT : ICON_TEXT); }

	/* Override */
	void drawIcons(Graphics g) {
		Image i = open ? folderOpenImg : folderImg;
		g.drawImage(i, (bgIconW - i.getWidth())/2,
			(bgIconH - i.getHeight())/2,
			Graphics.TOP | Graphics.LEFT); }

}

