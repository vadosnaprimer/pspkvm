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

package com.sun.kvem.midp.pim;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.io.file.*;
import java.io.*;
import javax.microedition.io.Connector;
import javax.microedition.pim.PIMException;

import com.sun.midp.log.*;
import com.sun.midp.security.*;
import com.sun.midp.main.Configuration;

/**
 * Handles reading and writing PIM data.
 *
 */
public class PIMDatabase {

    /**
     * Inner class to request security token from SecurityInitializer.
     * SecurityInitializer should be able to check this inner class name.
     */
    static private class SecurityTrusted
        implements ImplicitlyTrustedClass {};

    /** Security token to allow access to implementation APIs */
    private static SecurityToken classSecurityToken =
        SecurityInitializer.requestToken(new SecurityTrusted());

    /** Name of root dir. */
    private String dir;
    /** Hashtable of categories. */
    private Hashtable categoriesMap = new Hashtable();
    /** File separator symbol. */
    private final String fileSep = "/";

    // private static final Debug debug = Debug.create(PIMDatabase.class);

    /** Directory structure of lists. */
    private static final String[] DIRECTORIES = {
        "contacts",
        "events",
        "todo"
    };

    /** Default names of lists. */
    String[][] LISTS = {
        { "DefaultContactList" },
        { "DefaultEventList" },
        { "DefaultTodoList" }
    };

    /**
     * Default constructor.
     *
     */
    public PIMDatabase() {
    }

    /**
     * Constructor for creating a database object for the given path.
     *
     * @param inpDir path of root directory
     * @throws IOException if an error occurs accessing the file
     */
    public PIMDatabase(String inpDir) throws IOException {
	this.dir = inpDir;
	String fcPrefix = "file:";
	if (dir.startsWith(fcPrefix)) { // remove file prefix
	    dir = dir.substring(fcPrefix.length());
	}
	FileConnection tmpDir;
	boolean tmpCond;
	String tmpStr, tmpStr1;

	com.sun.midp.io.j2me.file.Protocol conn =
	    new com.sun.midp.io.j2me.file.Protocol();

	tmpDir = (FileConnection)conn.openPrim(classSecurityToken, dir);
	tmpCond = tmpDir.exists();
	tmpDir.close();

        if (!tmpCond) { // Create directories if need
	    synchronized (this) {
	        tmpDir = (FileConnection)conn.openPrim
		    (classSecurityToken, dir);
                tmpDir.mkdir();
	        tmpStr = tmpDir.getURL().substring(fcPrefix.length());
	        tmpDir.close();
                // create list type directories with default lists
                for (int i = 0; i < DIRECTORIES.length; i++) {
                    tmpDir =
		        (FileConnection)conn.openPrim(classSecurityToken,
			    tmpStr + DIRECTORIES[i]);
                    tmpDir.mkdir();
 	            tmpStr1 = tmpDir.getURL().substring(fcPrefix.length());
	            tmpDir.close();
                    for (int j = 0; j < LISTS[i].length; j++) {
                        String listName =
			    Configuration.getProperty(LISTS[i][j]);
		        tmpDir = (FileConnection)conn.openPrim
			    (classSecurityToken, tmpStr1 + listName);
		        tmpDir.mkdir();
	                tmpDir.close();
                    }
                }
	    }
        }
    }

    /**
     *  Gets the path for given list type.
     *
     * @param listType - CONTACT_LIST, EVENT_LIST or  TODO_LIST
     *
     * @return the path
     */
    private String getTypeDir(int listType) {
        return dir + fileSep + DIRECTORIES[listType - 1];
    }

    /**
     *  Gets the path for given list name.
     *
     * @param listType - CONTACT_LIST, EVENT_LIST or  TODO_LIST
     * @param listName    name of list
     *
     * @return the path
     */
    private String getListDir(int listType, String listName) {
        return getTypeDir(listType) + fileSep + listName;
    }

    /**
     *  Gets the list of names for given list type.
     *
     * @param listType - CONTACT_LIST, EVENT_LIST or  TODO_LIST
     *
     * @return array of names
     */
    public String[] getListNames(int listType) {
	Vector vect_names = new Vector();
	Enumeration enListType = null;
	FileConnection tmpDir;

	com.sun.midp.io.j2me.file.Protocol conn =
	    new com.sun.midp.io.j2me.file.Protocol();

	try {
	    tmpDir = (FileConnection)conn.openPrim(classSecurityToken,
	        getTypeDir(listType));
	    enListType = tmpDir.list();
            tmpDir.close();
        } catch (IOException e) {
	    if (Logging.TRACE_ENABLED) {
                Logging.trace(e, "getListNames: FileConnection problem");
	    }
	}
	while (enListType.hasMoreElements()) {
	    String curr_name = (enListType.nextElement()).toString();
	    if (curr_name.endsWith(fileSep)) { // The last symbol is "/"
	        vect_names.addElement(curr_name.substring(0,
		    curr_name.length() - 1)); // save the list name
	    }
	}
        String[] names = new String[vect_names.size()];
	Enumeration dir_names = vect_names.elements();
	int i;
	for (i = 0; i < names.length; i++) {
	    names[i] = (dir_names.nextElement()).toString();
	}
        return names;
    }

    /**
     *  Gets the default list name for given list type.
     *
     * @param listType - CONTACT_LIST, EVENT_LIST or  TODO_LIST
     *
     * @return default name
     */
    public String getDefaultListName(int listType) {
        return Configuration.getProperty(LISTS[listType - 1][0]);
    }

    /**
     *  Gets the key table for given list name.
     *
     * @param listType - CONTACT_LIST, EVENT_LIST or  TODO_LIST
     * @param listName    name of list
     *
     * @return hashkey of keys
     * @throws PIMException in case of I/O error
     */
    public Hashtable getKeys(int listType, String listName)
        throws PIMException {
        Hashtable keys = new Hashtable();
	Enumeration enListDir = null;
	FileConnection tmpDir;

	com.sun.midp.io.j2me.file.Protocol conn =
	    new com.sun.midp.io.j2me.file.Protocol();

	try {
	    tmpDir = (FileConnection)conn.openPrim(classSecurityToken,
	        getTypeDir(listType) + "/" + listName);
	    enListDir = tmpDir.list();
            tmpDir.close();
        } catch (IOException e) {
            if (Logging.TRACE_ENABLED) {
                Logging.trace(e, "getKeys: FileConnection problem");
	    }
            throw new PIMException("getKeys: " + e.getMessage());
	}
	while (enListDir.hasMoreElements()) {
	    String curr_name = (enListDir.nextElement()).toString();
	    curr_name = curr_name.toLowerCase();
	    if (curr_name.endsWith(".vcf") || curr_name.endsWith(".vcs")) {
	        keys.put(curr_name, curr_name);
	    }
	}
        return keys;
    }

    /**
     *  Gets the element by key.
     *
     * @param listType - CONTACT_LIST, EVENT_LIST or  TODO_LIST
     * @param listName    name of list
     * @param key         key value
     *
     * @return data by key
     * @throws PIMException in case of I/O error
     */
    public byte[] getElement(int listType, String listName, String key)
        throws PIMException {
	FileConnection file = null;
	byte[] ret_v = null;

	com.sun.midp.io.j2me.file.Protocol conn =
	    new com.sun.midp.io.j2me.file.Protocol();

	try {
            file = (FileConnection)conn.openPrim(classSecurityToken,
	        getListDir(listType, listName) + fileSep + key,
		Connector.READ);
            long elemSize = file.fileSize();
            if (elemSize <= 0) { // no categories
                ret_v = new byte[0];
            } else { // read categories
                byte[] buffer = new byte[(int)elemSize];
                DataInputStream in = file.openDataInputStream();
                in.readFully(buffer);
                in.close();
                ret_v = buffer;
            }
            file.close();
        } catch (IOException e) {
            if (Logging.TRACE_ENABLED) {
                Logging.trace(e, "getElement: FileConnection problem");
	    }
            throw new PIMException("getElement: " + e.getMessage());
        } finally {
	    try {
	        if (file.isOpen()) {
	            file.close();
                }
	    } catch (IOException e) {
	        // do nothing
	    }
	}
	return ret_v;
    }

    /**
     *  Commits the PIM item by key.
     *
     * @param listType    CONTACT_LIST, EVENT_LIST or  TODO_LIST
     * @param listName    name of list
     * @param key         key of PIM item
     * @param data        data of PIM item
     *
     * @return key of PIM item
     * @throws PIMException in case of I/O error
     */
    public synchronized String commitElement(int listType, String listName,
			 String key, byte[] data) throws PIMException {
        String ret_v = null;
	if (key == null) {
            // make a new key
            Hashtable keySet = getKeys(listType, listName);
            String extension;
            switch (listType) {
                case PIMBridge.CONTACT_LIST: extension = ".vcf"; break;
                default: extension = ".vcs"; break;
            }
            key = 1 + extension;
	    int i = 1;
            while (keySet.containsKey(i + extension)) {
                key = (++i) + extension;
                // keep looking for a free file name
            }
        }
        String listDir = getListDir(listType, listName);
        FileConnection file = null;
	com.sun.midp.io.j2me.file.Protocol conn =
	    new com.sun.midp.io.j2me.file.Protocol();
        try {
            file = (FileConnection)conn.openPrim(classSecurityToken,
	        listDir + fileSep + key);
            if (data == null) {
	        if (file.exists()) {
                    file.delete();
		}
		file.close();
            } else {
	        if (!file.exists()) {
                    file.create();
		}
                DataOutputStream out = file.openDataOutputStream();
                out.write(data);
                out.close();
		file.close();
                ret_v = new String(key);
            }
        } catch (IOException e) {
            if (Logging.TRACE_ENABLED) {
                Logging.trace(e, "commitElement: FileConnection problem");
	    }
            throw new PIMException("commitElement: " + e.getMessage());
        } finally {
	    try {
	        if (file.isOpen()) {
	            file.close();
                }
	    } catch (IOException e) {
	        // do nothing
	    }
	}
	return ret_v;
    }

    /**
     *  Create the key string by the given PIM list.
     *
     * @param listType    CONTACT_LIST, EVENT_LIST or  TODO_LIST
     * @param listName    name of list
     *
     * @return key string
     */
    private String categoryKey(int listType, String listName) {
        return "" + listType + "." + listName;
    }

    /**
     *  Sets the new categories.
     *
     * @param listType    CONTACT_LIST, EVENT_LIST or  TODO_LIST
     * @param listName    name of list
     * @param categories  string of new categories
     * @throws PIMException in case of I/O error
     * @see #getCategories
     *
     */
    public synchronized void setCategories(int listType, String listName,
                            String categories) throws PIMException {
        FileConnection file = null;

	com.sun.midp.io.j2me.file.Protocol conn =
	    new com.sun.midp.io.j2me.file.Protocol();

	try {
            file = (FileConnection)conn.openPrim(classSecurityToken,
	        getListDir(listType, listName) + fileSep + "categories.txt");
	    if (!file.exists()) {
	        file.create();
	    } else {
                file.truncate(0);
            }
            DataOutputStream out = file.openDataOutputStream();
            out.write(categories.getBytes());
            out.close();
            file.close();
            categoriesMap.put(categoryKey(listType, listName), categories);
        } catch (IOException e) {
            if (Logging.TRACE_ENABLED) {
                Logging.trace(e, "setCategories: FileConnection problem");
	    }
            throw new PIMException("setCategories: " + e.getMessage());
        } finally {
	    try {
	        if (file.isOpen()) {
	            file.close();
                }
	    } catch (IOException e) {
	        // do nothing
	    }
	}
    }

    /**
     *  Gets the string of supported categories.
     *
     * @param listType    CONTACT_LIST, EVENT_LIST or  TODO_LIST
     * @param listName    name of list
     *
     * @return string of supported categories
     * @throws PIMException in case of I/O error
     * @see #setCategories
     */
    public synchronized String getCategories(int listType, String listName)
        throws PIMException {
        String key = categoryKey(listType, listName);
        String categories = (String) categoriesMap.get(key);

	com.sun.midp.io.j2me.file.Protocol conn =
	    new com.sun.midp.io.j2me.file.Protocol();

	if (categories == null) {
            FileConnection file = null;
	    try {
                file =  (FileConnection)conn.openPrim(classSecurityToken,
		    getListDir(listType, listName) + fileSep +
		    "categories.txt");
                if (!file.exists()) {
                    file.create();
		}
                long catSize = file.fileSize();
                if (catSize <= 0) { // no categories
                    categories = "";
                } else { // read categories
                    byte[] buffer = new byte[(int)catSize];
                    DataInputStream in = file.openDataInputStream();
                    in.readFully(buffer);
                    in.close();
                    categories = new String(buffer);
                }
                file.close();
                categoriesMap.put(key, categories);
            } catch (IOException e) {
		throw new PIMException("getCategories: " + e.getMessage());
            } finally {
	        try {
	            if (file.isOpen()) {
	                file.close();
                    }
	        } catch (IOException e) {
		    throw new PIMException("getCategories: " +
		        e.getMessage());
	        }
	    }
        }
        return categories;
    }

}
