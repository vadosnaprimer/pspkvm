package com.sun.midp.installer;

import java.util.Vector;

import java.io.InputStreamReader;
import java.io.IOException;

class SuiteFileDiscover {
	
	private SuiteFileDiscover() {
		
	}

	private static native int openDir(String dir);
	private static native String nextFileInDir(int handle);
	private static native void closeDir(int handle);

	static Vector getDownloadInfoFromDir(String dir) 
            throws IOException {
          Vector suites = new Vector();
          String name;
          int handle = openDir("ms0:/PSP");
          while (handle != null && (name = nextFileInDir(handle)) != null) {
            if (name.endsWith(".jad") ||
                    name.url.endsWith(".jar")) {
                suites.addElement(name);
            }
          }
          closeDir(handle);

          return suites;
       }
}

