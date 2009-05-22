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

package com.sun.j2me.location;

import java.util.*;
import java.io.*;

import javax.microedition.rms.*;
import com.sun.midp.rms.RecordStoreImpl;
import com.sun.midp.security.SecurityToken;
import com.sun.midp.security.SecurityInitializer;
import com.sun.midp.security.ImplicitlyTrustedClass;
import com.sun.midp.midlet.MIDletSuite;
import javax.microedition.location.*;

/**
 * This class is an implementation of the persistent storage to store landmarks.
 */
// JAVADOC COMMENT ELIDED    
public class LocationPersistentStorage {

    // JAVADOC COMMENT ELIDED    
    private static final int suiteID = MIDletSuite.INTERNAL_SUITE_ID;

    // JAVADOC COMMENT ELIDED    
    private static final String prefixLandmarksFile = "lapi-lm-";

    // JAVADOC COMMENT ELIDED
    private static final String prefixCategoriesFile = "lapi-lc-";

    // JAVADOC COMMENT ELIDED
    private static final String defaultLandmarksFile = "lapi-default-lm";

    // JAVADOC COMMENT ELIDED
    private static final String defaultCategoriesFile = "lapi-default-lc";

    // JAVADOC COMMENT ELIDED
    static private class SecurityTrusted
        implements ImplicitlyTrustedClass {};

    // JAVADOC COMMENT ELIDED
    private static SecurityToken token =
        SecurityInitializer.requestToken(new SecurityTrusted());

    // JAVADOC COMMENT ELIDED
    private static LocationPersistentStorage storage = null;

    // JAVADOC COMMENT ELIDED
    public static LocationPersistentStorage getInstance() {
        if (storage == null) {
            storage = new LocationPersistentStorage();
            try {
                storage.addStoreName(null);
            } catch (IOException ex) {
            }
        }
        return storage;
    }

    // JAVADOC COMMENT ELIDED
    private LocationPersistentStorage() {
    }

    // JAVADOC COMMENT ELIDED    
    private static String getLandmarksFileName(String storeName) {
	if (storeName == null) {
	    return defaultLandmarksFile;
	}
	return prefixLandmarksFile + storeName;
    }

    // JAVADOC COMMENT ELIDED
    private static String getCategoriesFileName(String storeName) {
	if (storeName == null) {
	    return defaultCategoriesFile;
	}
	return prefixCategoriesFile + storeName;
    }

    // JAVADOC COMMENT ELIDED    
    synchronized public void addStoreName(String storeName)
	throws IOException {
	RecordStoreImpl landmarkStore = null;
	try {
            landmarkStore = RecordStoreImpl.openRecordStore(token, 
		suiteID, getLandmarksFileName(storeName), true);
	    landmarkStore.closeRecordStore();
	} catch (RecordStoreException e) {
	    throw new IOException(e.getMessage());
	}
    }

    // JAVADOC COMMENT ELIDED    
    synchronized public void removeStoreName(String storeName)
	throws IOException {
	// remove file with landmarks
	try {
	    RecordStoreImpl.deleteRecordStore(token, 
		suiteID, getLandmarksFileName(storeName));
	    RecordStoreImpl.deleteRecordStore(token, 
		suiteID, getCategoriesFileName(storeName));
	} catch (RecordStoreNotFoundException e) {
	    // quit silently
	} catch (RecordStoreException e) {
	    throw new IOException(e.getMessage());
	}
    }

    // JAVADOC COMMENT ELIDED    
    synchronized public String[] listStoreNames() throws IOException {
        String[] returnValue = null;
        String[] fileList = RecordStoreImpl.listRecordStores(token, suiteID);
        if (fileList != null) {
            Vector storeList = new Vector();
            int index = 0;
            for (int i = 0; i < fileList.length; i++) {
                String fileName = fileList[i];
                if (fileName.startsWith(prefixLandmarksFile)) {
                    String storeName =
                        fileName.substring(prefixLandmarksFile.length());
                    storeList.addElement(storeName);
                }
            }
            if (storeList.size() > 0) {
                returnValue = new String[storeList.size()];
                for (int i = 0; i < storeList.size(); i++) {
                    returnValue[i] = (String)storeList.elementAt(i);
                }
            }
        }
	return returnValue;
    }

    // JAVADOC COMMENT ELIDED    
    synchronized public Vector getCategories(String storeName)
	throws IOException {
        Vector categories = new Vector();
	RecordStoreImpl categoryStore = null;
	try {
	    // open a file contains categories
	    categoryStore = RecordStoreImpl.openRecordStore(token, 
	        suiteID, getCategoriesFileName(storeName), false);
	    int[] recordIDs = categoryStore.getRecordIDs();
	    for (int i = 0; i < recordIDs.length; i++) {
	        int recID = recordIDs[i];
		int recSize = categoryStore.getRecordSize(recID);
		byte[] record = new byte[recSize];
		categoryStore.getRecord(recID, record, 0);
		categories.addElement(new String(record));
	    }
	    categoryStore.closeRecordStore();
	} catch (RecordStoreNotFoundException e) {
	    // categories were not found; return empty vector
	} catch (RecordStoreException e) {
	    throw new IOException(e.getMessage());
	}
        return categories;
    }

    // JAVADOC COMMENT ELIDED
    synchronized public void addCategory(String categoryName, String storeName)
        throws IOException, IllegalArgumentException {
        Vector catVector = getCategories(storeName);
	if (catVector.indexOf(categoryName) != -1) {
            throw new IllegalArgumentException("Category `" + categoryName +
                            "' already exists in LandmarkStore " + storeName);
        }
	try {
	    RecordStoreImpl categoryStore =
	        RecordStoreImpl.openRecordStore(token, suiteID,
		    getCategoriesFileName(storeName), true);
	    byte[] bytesCatName = categoryName.getBytes();
	    categoryStore.addRecord(bytesCatName, 0, bytesCatName.length);
	    categoryStore.closeRecordStore();
	} catch (RecordStoreException e) {
	    throw new IOException(e.getMessage());
	}
    }

    // JAVADOC COMMENT ELIDED
    synchronized public void deleteCategory(String categoryName,
					    String storeName)
        throws IOException {
        Enumeration e = getLandmarksEnumeration(storeName, categoryName, null, 
						 -90, 90, -180, 180);
        if (e != null) {
            while (e.hasMoreElements()) {
                LandmarkImpl m = (LandmarkImpl)e.nextElement();
                removeLandmarkFromCategory(storeName, m, categoryName);
            }
        }
        Vector catVector = getCategories(storeName);
        int ind = catVector.indexOf(categoryName);
        if (ind != -1) { // remove a category
            try {
                RecordStoreImpl categoryStore =
                    RecordStoreImpl.openRecordStore(token, suiteID,
                        getCategoriesFileName(storeName), false);
                int[] recordIDs = categoryStore.getRecordIDs();
                for (int i = 0; i < recordIDs.length; i++) {
                    int recID = recordIDs[i];
                    int recSize = categoryStore.getRecordSize(recID);
                    byte[] record = new byte[recSize];
                    categoryStore.getRecord(recID, record, 0);
                    if (categoryName.equals(new String(record))) {
                        // category for deleting is found - delete it
                        categoryStore.deleteRecord(recID);
                        break;
                    }
                }
                categoryStore.closeRecordStore();
            } catch (RecordStoreException ex) {
                throw new IOException(ex.getMessage());
            }
        }
    }

    // JAVADOC COMMENT ELIDED    
    synchronized public void addLandmark(String storeName, 
            LandmarkImpl landmark, String category)
	throws IOException,  IllegalArgumentException {
        try {
            if ((landmark.getRecordId()) > -1 &&
                (landmark.getStoreName() == storeName)) {
                landmark.addCategory(category);
                updateLandmark(storeName, landmark);
                return;
            }
        } catch (LandmarkException e) {
            // if it is happened, just continue to add Landmark
        }

        // new landmark will belong to specified category only
        landmark.removeCategories();
        if (category != null) {
            if (!getCategories(storeName).contains(category)) {
                throw new IllegalArgumentException("The category: " +
                    category + " was not added to this landmark store");
            }
            landmark.addCategory(category);
        } else {
            landmark.addCategory("");
        }
        byte[] data = landmark.serialize();
        landmark.setRecordId(addLandmark(storeName, data));
        landmark.setStoreName(storeName);
    }

    // JAVADOC COMMENT ELIDED
    synchronized public int addLandmark(String storeName, byte[] lm)
	throws IOException {
	try {
	    int recordID = -1;
	    RecordStoreImpl landmarkStore =
		RecordStoreImpl.openRecordStore(token, suiteID,
		    getLandmarksFileName(storeName), false);
	    recordID = landmarkStore.addRecord(lm, 0, lm.length);
	    landmarkStore.closeRecordStore();
	    return recordID;
	} catch (RecordStoreException e) {
	    throw new IOException(e.getMessage());
	}
    }

    // JAVADOC COMMENT ELIDED
    synchronized public void deleteLandmark(String storeName, 
            LandmarkImpl lm) throws IOException, LandmarkException {

        // If the store names are not the same or
        // they are the same and both are null (for the default LMS)
        // but the landmark does not belong to a LMS
        if ((lm.getStoreName() != storeName) ||
                    (lm.getStoreName() == null && lm.getRecordId() == -2)) {
            throw new LandmarkException("This landmark belongs to a " +
		"different store: " + lm.getStoreName());
	}
        if (lm.getRecordId() > -1) {
            deleteLandmark(storeName, lm.getRecordId());
        }
        lm.setRecordId(-1);
        
    }

    // JAVADOC COMMENT ELIDED
    synchronized public void deleteLandmark(String storeName, int recordId) 
        throws IOException {
	RecordStoreImpl landmarkStore = null;
	try {
	    landmarkStore = RecordStoreImpl.openRecordStore(token, suiteID,
		getLandmarksFileName(storeName), false);
	    landmarkStore.deleteRecord(recordId);
	    landmarkStore.closeRecordStore();
	} catch (InvalidRecordIDException e) {
	    // landmark was not found; quit silently
	    try {
		landmarkStore.closeRecordStore();
	    } catch (RecordStoreException x) {
		throw new IOException(x.getMessage());
	    }
	} catch (RecordStoreException e) {
	    throw new IOException(e.getMessage());
	}
    }

    // JAVADOC COMMENT ELIDED
    public void removeLandmarkFromCategory(String storeName, 
            LandmarkImpl lm, String category) throws IOException {

        try {
            if (lm.getRecordId() > -1 && lm.isInCategory(category)) {
                lm.removeCategory(category);
                updateLandmark(storeName, lm);
            }
        } catch (LandmarkException e) {
            // if it is happened, silently exit
        }
    }

    // JAVADOC COMMENT ELIDED
    public void updateLandmark(String storeName, LandmarkImpl lm) 
                throws IOException, LandmarkException {
        if (lm.getRecordId() < 0) {
	    throw new LandmarkException(
		"The landmark was not loaded from a landmark store");
	}
	byte[] data = lm.serialize();
	lm.setRecordId(updateLandmark(storeName, lm.getRecordId(), data));
    }

    // JAVADOC COMMENT ELIDED    
    synchronized public byte[] getLandmarks(String storeName)
	throws IOException {
	try {
	    byte[] returnValue = null;
	    RecordStoreImpl landmarkStore = null;
	    landmarkStore = RecordStoreImpl.openRecordStore(token, suiteID,
	        getLandmarksFileName(storeName), false);
	    ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
	    DataOutputStream stream = 
		new DataOutputStream((OutputStream)byteArrayOut);
	    int[] recordIDs = landmarkStore.getRecordIDs();
	    for (int i = 0; i < recordIDs.length; i++) {
		int recID = recordIDs[i];
		stream.writeInt(recID); // save record ID
		// save the length of landmark
		int recSize = landmarkStore.getRecordSize(recID);
		stream.writeInt(recSize);
		byte[] record = new byte[recSize];
		landmarkStore.getRecord(recID, record, 0);
		stream.write(record, 0, recSize);
	    }
	    landmarkStore.closeRecordStore();
	    returnValue = byteArrayOut.toByteArray();
	    return returnValue;
	} catch (RecordStoreException e) {
	    throw new IOException(e.getMessage());
	}
    }

    // JAVADOC COMMENT ELIDED
    synchronized public int 
            updateLandmark(String storeName, int recordId, byte[] lmData)
        throws IOException {
	try {
            deleteLandmark(storeName, recordId);
	    return addLandmark(storeName, lmData);
	} catch (Throwable e) {
	    throw new IOException("Error while updating landmark");
	}
    }

    // JAVADOC COMMENT ELIDED
    public Enumeration getLandmarksEnumeration(String storeName, 
                                    String category, String name,
                                    double minLatitude,
				    double maxLatitude, double minLongitude,
				    double maxLongitude) throws IOException {
        Enumeration en = new LandmarkEnumeration(storeName, category, name, 
						 minLatitude, maxLatitude,
						 minLongitude, maxLongitude);
        
        if (!en.hasMoreElements()) {
            return null;
        }
        return en;
    }
    
    /**
     * This class allows us to traverse the landmarks in the store
     */
    class LandmarkEnumeration implements Enumeration {
        // JAVADOC COMMENT ELIDED
        private String category;
        // JAVADOC COMMENT ELIDED
        private String name;
        // JAVADOC COMMENT ELIDED
        private double minLatitude;
        // JAVADOC COMMENT ELIDED
        private double maxLatitude;
        // JAVADOC COMMENT ELIDED
        private double minLongitude;
        // JAVADOC COMMENT ELIDED
        private double maxLongitude;
        // JAVADOC COMMENT ELIDED
        private Enumeration enumeration;

        // JAVADOC COMMENT ELIDED
        LandmarkEnumeration(String storeName, String category, String name, 
                double minLatitude, double maxLatitude, double minLongitude, 
                double maxLongitude) throws IOException {
            this.category = category;
            this.name = name;
            this.minLatitude = minLatitude;
            this.maxLatitude = maxLatitude;
            this.minLongitude = minLongitude;
            this.maxLongitude = maxLongitude;
            byte[] result =
                LocationPersistentStorage.getInstance().
                    getLandmarks(storeName);
            DataInputStream stream =
                new DataInputStream(new ByteArrayInputStream(result));
            Vector cleanVec = new Vector();
            try {
                while (true) {
                    int id = stream.readInt();
                    int lmSize = stream.readInt();
                    byte[] serializedLm = new byte[lmSize];
                    stream.read(serializedLm);
                    if (matches(serializedLm)) {
                        cleanVec.addElement(
                            new LandmarkImpl(serializedLm, id, storeName));
                    }
                }
            }
            catch (EOFException eofe) {
            }
            this.enumeration = cleanVec.elements();
	}

        // JAVADOC COMMENT ELIDED
        public Object nextElement() {
            return enumeration.nextElement();
        }

        // JAVADOC COMMENT ELIDED
        public boolean hasMoreElements() {
            return enumeration.hasMoreElements();
        }

        // JAVADOC COMMENT ELIDED
        public boolean matches(byte[] candidate) {
            LandmarkImpl l = new LandmarkImpl(candidate, -1, null);
            if (category != null) {
                if (!l.isInCategory(category)) {
                    return false;
                }
            }
            if (name != null) {
                if (!name.equals(l.getName())) {
                    return false;
                }
            }
            if (l.getQualifiedCoordinates() == null) {
                return true;
            }
	    double lat = l.getQualifiedCoordinates().getLatitude();
	    double lon = l.getQualifiedCoordinates().getLongitude();
            boolean val;
            if (minLongitude > maxLongitude) {
                val = (minLatitude <= lat) && (maxLatitude >= lat)
		    && ((minLongitude < lon) || (maxLongitude > lon));
            } else {
                val = (minLatitude <= lat) && (minLongitude <= lon) &&
                    (maxLongitude >= lon) && (maxLatitude >= lat);
            }
            return val;
        }
    }
}
