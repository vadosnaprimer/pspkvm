package com.sun.midp.appmanager;

import java.io.*;

abstract class WriteableAMSMidletCustomItem extends WriteableAMSCustomItem {

	/** The MIDletSuiteInfo associated with this MidletCustomItem */
  RunningMIDletSuiteInfo msi; // = null

	WriteableAMSMidletCustomItem(String s) {
		super(s); }

	int getSuiteID() {
		return msi.suiteId; }

	void write(DataOutputStream ostream) throws IOException {
		ostream.writeByte((byte)TYPE_MIDLET);
		ostream.writeInt(msi.suiteId); }

}
