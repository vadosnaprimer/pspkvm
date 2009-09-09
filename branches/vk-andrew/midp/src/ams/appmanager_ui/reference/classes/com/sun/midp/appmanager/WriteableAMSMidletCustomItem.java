package com.sun.midp.appmanager;

import java.io.*;

abstract class WriteableAMSMidletCustomItem extends WriteableAMSCustomItem {

	WriteableAMSMidletCustomItem(String s) {
		super(s); }

	abstract int getSuiteID();
}
