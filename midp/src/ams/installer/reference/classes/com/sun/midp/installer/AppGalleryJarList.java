package com.sun.midp.installer;

import javax.microedition.lcdui.List;
import java.util.Vector;

class AppGalleryJarList extends List {
	private Vector entries = new Vector(10);

	public AppGalleryJarList(String title, int listType) {
		super(title, listType);
	}
	
	public void appendJarList(JarEntry e) {
		append(e.getTitle(), null);
		entries.addElement(e);
	}

	public JarEntry getJarEntry(int index) {
		return (JarEntry)entries.elementAt(index);
	}

	public void reset() {
		deleteAll();
		entries.removeAllElements();
	}
}

