package com.sun.midp.appmanager;

import java.util.Vector;
import java.io.*;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import com.sun.midp.installer.GraphicalInstaller;

/*
	A folder within the AMS, containing (potentially) additional folders and/or
	additional MidletCustomItems
*/

class AMSFolderCustomItem extends WriteableAMSCustomItem {
	Vector contents;
	boolean open;
	AMSFolderCustomItem parent;
	
	// Constructor for creation in UI, sans content
	AMSFolderCustomItem(String n, AMSFolderCustomItem p) {
		super(null);
		text = n.toCharArray();
		icon = folderImg;
		open = false;
		contents = new Vector();
		parent=p; }
		
	// Read the root from storage
	static AMSFolderCustomItem readRoot(DataInputStream di, Vector mcis)
		throws IOException {
		int t = (int)(di.readByte());
		if (t != TYPE_FOLDER) {
			throw new IOException("Root was not folder--unexpected type field: " + t); }
		return new AMSFolderCustomItem(null, di, mcis); }
		
	// Create the root from a list of midlets--usually just called first time
	// the folder system runs.
	static AMSFolderCustomItem createRoot(Vector mcis) {
		AMSFolderCustomItem root = new AMSFolderCustomItem("root", null);
		root.open=true;
		root.contents=mcis;
		return root; }

	// Constructor for creation from storage		
	AMSFolderCustomItem(AMSFolderCustomItem p, DataInputStream di, Vector mcis)
		throws IOException {
		super(null);
		text=di.readUTF().toCharArray();
		open = false;
		parent=p;
		int csize = di.readInt();
		contents = new Vector(csize);
		for(int i=0; i<csize; i++) {
			contents.addElement(readNextCI(di, mcis)); } }

	// Write to storage		
	void write(DataOutputStream ostream) throws IOException {
		ostream.writeByte((byte)(WriteableAMSCustomItem.TYPE_FOLDER));
		ostream.writeUTF(new String(text));
		int c = contents.size();
		ostream.writeInt(c);
		for(int i=0; i<c; i++) {
			WriteableAMSCustomItem wamci = (WriteableAMSCustomItem)(contents.elementAt(i));
			wamci.write(ostream); } }
	
	// Helper for construction from storage
	WriteableAMSCustomItem readNextCI(DataInputStream di, Vector mcis)
		throws IOException {
		int t = (int)(di.readByte());
		switch(t) {
				case TYPE_FOLDER:
					AMSFolderCustomItem afci = new AMSFolderCustomItem(this, di, mcis);
					return afci;
				case TYPE_MIDLET:
					int suiteID = di.readInt();
					return getMidletSuiteByID(mcis, suiteID);
				default:
					throw new IOException("Unexpected type field in AMSFolderCustomItem:" + t); } }
	
	// Finds a WriteableAMSCustomItem in a Vector containing these, by its suiteID
	static WriteableAMSCustomItem getMidletSuiteByID(Vector mcis, int suiteID) {
		int s = mcis.size();
		for (int i=0; i<s; i++) {
			WriteableAMSMidletCustomItem wamci = (WriteableAMSMidletCustomItem)(mcis.elementAt(i));
			if (wamci.getSuiteID()==suiteID) {
				return wamci; } }
		// Error. Throw.
		throw new ArrayIndexOutOfBoundsException("No such suiteID"); }
	
	// Required overrides
	protected int	getMinContentWidth() { 
    // TODO
    return -1; }
          
	protected int	getPrefContentWidth(int height) {
		// TODO
		return -1; }
		
	protected void paint(Graphics g, int w, int h) {
		// TODO
		}
}
