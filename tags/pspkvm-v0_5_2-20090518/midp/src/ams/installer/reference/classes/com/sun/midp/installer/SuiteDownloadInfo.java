/*
 *   
 *
 * Copyright  1990-2007 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
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
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, CA 95054 or visit www.sun.com if you need additional
 * information or have any questions.
 */

package com.sun.midp.installer;

import java.util.Vector;

import java.io.InputStreamReader;
import java.io.IOException;

/**
 * This class represents the Information need to download a MIDlet suite and
 * display it to a user, in a list.
 */
class SuiteDownloadInfo {
    /** URL for the JAD of this suite */
    String url;
    /** label to display to the User for this suite */
    String label;

    /** If it's a directory of local fs*/
    boolean dir;
    
    /** If it's an entry refer to http url */
    boolean http;

    /**
     * Read a HTML page and pickout the links for MIDlet suites.
     * A MIDlet suite links end with .jad
     *
     * @param page HTML page to be read
     *
     * @return vector of URL/Label pairs
     *
     * @exception IOException is thrown if any error prevents the 
     *   download of the HTML page.
     */
    static Vector getDownloadInfoFromPage(InputStreamReader page) 
            throws IOException {
        Vector suites = new Vector();
        SuiteDownloadInfo info;
        
        info = getNextJadLink(page);
        while (info != null) {
            if (info.url.toLowerCase().endsWith(".jad") ||
                    info.url.toLowerCase().endsWith(".jar")) {
                info.http = true;
                suites.addElement(info);
            }

            info = getNextJadLink(page);
        }

        return suites;
    }

    private static native int openDir(String dir);
    private static native String nextFileInDir0(String dir, int handle);
    private static native void closeDir(int handle);
    private static native String convert2lable0(String str);

    private static SuiteDownloadInfo getNextFileInDir(String dir, int handle) {
    	 String fullpath, lable;
    	 boolean isDir = false;
    	 
    	 fullpath = nextFileInDir0(dir, handle);
        if ( fullpath== null) {
        	return null;
        }
        if (fullpath.lastIndexOf('/') == (fullpath.length()- 1)) {
        	isDir = true;
        	fullpath = fullpath.substring(0, fullpath.length()- 1);
        }
        lable = fullpath.substring(fullpath.lastIndexOf('/') + 1);
        System.out.println("getNextFileInDir:"+fullpath+" "+lable+(isDir?"[DIR]":""));
        lable = convert2lable0(lable);
        return new SuiteDownloadInfo(fullpath, lable, isDir);
    }

    static Vector getDownloadInfoFromDir(String dir) 
            throws IOException {
          Vector suites = new Vector();
          SuiteDownloadInfo info;
          
          //info = new SuiteDownloadInfo(dir, dir);
          //suites.addElement(info);
          
          
          int handle = openDir(dir);
          while (handle != 0 && (info = getNextFileInDir(dir, handle)) != null) {
            if (info.url.toLowerCase().endsWith(".jad") ||
                    info.url.toLowerCase().endsWith(".jar") ||
                    (info.dir && !info.label.equals("."))) {
                info.http = false;
                suites.addElement(info);
            }
          }
          if (handle != 0) {
              closeDir(handle);
          }
          

          return suites;
    }
      
    /**
     * Read a HTML page and pickout next link.
     *
     * @param page HTML page to be read
     *
     * @return URL/Label pair
     *
     * @exception IOException is thrown if any error prevents the 
     *   download of the HTML page.
     */
    private static SuiteDownloadInfo getNextJadLink(InputStreamReader page)
            throws IOException {
        String url;
        String label;

        url = getNextUrl(page);
        if (url == null) {
            return null;
        }

        label = getNextLabel(page);
        if (label == null) {
            label = url;
        }

        return new SuiteDownloadInfo(url, label);
    }

    /**
     * Read a HTML page and pickout next href.
     *
     * @param page HTML page to be read
     *
     * @return URL
     *
     * @exception IOException is thrown if any error prevents the 
     *   download of the HTML page.
     */
    private static String getNextUrl(InputStreamReader page)
            throws IOException {
        int currentChar;
        StringBuffer url;

        if (!findString(page, "href=\"")) {
            return null;
        }

        url = new StringBuffer();

        currentChar = page.read();
        while (currentChar != '"') {
            if (currentChar == -1) {
                return null;
            }

            url.append((char)currentChar);
            currentChar = page.read();
        }

        if (url.length() == 0) {
            return null;
        }

        return url.toString();
    }

    /**
     * Read a HTML page and pickout the text after the beginning anchor.
     *
     * @param page HTML page to be read
     *
     * @return label
     *
     * @exception IOException is thrown if any error prevents the 
     *   download of the HTML page.
     */
    private static String getNextLabel(InputStreamReader page)
            throws IOException {
        int currentChar;
        StringBuffer label;

        if (!findChar(page, '>')) {
            return null;
        }

        label = new StringBuffer();

        currentChar = page.read();
        while (currentChar != '<') {
            if (currentChar == -1) {
                return null;
            }

            label.append((char)currentChar);
            currentChar = page.read();
        }

        if (label.length() == 0) {
            return null;
        }

        return label.toString();
    }

    /**
     * Find the next given string in an HTML page move past it.
     *
     * @param page HTML page to be read
     * @param targetString string to move past
     *
     * @return true if string found, else false
     *
     * @exception IOException is thrown if any error prevents the 
     *   download of the HTML page.
     */
    private static boolean findString(InputStreamReader page,
                                      String targetString) throws IOException {
        for (int i = 0; i < targetString.length(); i++) {
            if (!findChar(page, targetString.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Find the next given char in an HTML page move past it.
     *
     * @param page HTML page to be read
     * @param targetChar char to move past
     *
     * @return true if string found, else false
     *
     * @exception IOException is thrown if any error prevents the 
     *   download of the HTML page.
     */
    private static boolean findChar(InputStreamReader page, char targetChar)
            throws IOException {
        int currentChar;

        currentChar = page.read();
        while (Character.toLowerCase((char)currentChar) != targetChar) {
            if (currentChar == -1) {
                return false;
            }

            currentChar = page.read();
        }

        return true;
    }

    /**
     * Constructs a SuiteDownloadInfo.
     *
     * @param theUrl URL for this suite
     * @param theLabel label for this suite
     * @param isDir true if the entry is a directory
     */
    SuiteDownloadInfo(String theUrl, String theLabel, boolean isDir) {
        url = theUrl;
        label = theLabel;
        dir = isDir;
    }

    /**
     * Constructs a SuiteDownloadInfo.
     *
     * @param theUrl URL for this suite
     * @param theLabel label for this suite
     */
    SuiteDownloadInfo(String theUrl, String theLabel) {
        url = theUrl;
        label = theLabel;
        dir = false;
    }
}
