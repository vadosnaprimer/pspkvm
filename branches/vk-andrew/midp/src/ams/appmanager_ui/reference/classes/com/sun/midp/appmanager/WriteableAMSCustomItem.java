package com.sun.midp.appmanager;

import java.io.*;
import javax.microedition.lcdui.CustomItem;

abstract class WriteableAMSCustomItem extends CustomItem {

	// Field specifiers for reading/writing in a stream
	protected static final int TYPE_FOLDER=0x00;
	protected static final int TYPE_MIDLET=0x01;
	
	WriteableAMSCustomItem(String s) {
		super(s); }

	// Write to storage 
	abstract void write(DataOutputStream ostream) throws IOException;
}
