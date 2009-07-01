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

import javax.microedition.location.*;

/**
 * This class is an implementation of the persistent storage to store landmarks.
 */
// JAVADOC COMMENT ELIDED
public class LocationPersistentStorage {

    private static final String SEPARATOR = ",";

    private static LocationPersistentStorage storage = null;

    public static LocationPersistentStorage getInstance() {
        if(storage == null) {
            storage = new LocationPersistentStorage();
        }
        return storage;
    }

    // JAVADOC COMMENT ELIDED    
    private LocationPersistentStorage() {
    }

    // JAVADOC COMMENT ELIDED    
    synchronized public void addStoreName(String storeName)
	throws IOException {
	try {
            createLandmarkStore(storeName);
	} catch (IllegalArgumentException e) {
            throw new IOException(e.getMessage());
	}
    }

    // JAVADOC COMMENT ELIDED    
    synchronized public void removeStoreName(String storeName)
	throws IOException {
        // cannot delete default LandmarkStore
        if(storeName == null) {
            throw new 
                    NullPointerException("the default store can't be deleted");
        }

        // remove file with landmarks
        removeLandmarkStore(storeName);
    }

    // JAVADOC COMMENT ELIDED    
    synchronized public static String[] listStoreNames() 
                                        throws IOException {
        Vector vectStores = new Vector();
        String storeName;
        int hndl = openLandmarkStoreList();
        do {
            storeName = landmarkStoreGetNext(hndl);
            if ((storeName != null) && (storeName.length()>0)) {
                vectStores.addElement(storeName);
            } else {
                closeLandmarkStoreList(hndl);
                storeName = null;
            }
        } while(storeName != null);
        String[] arrStores = null;
        if(vectStores.size()>0) {
            arrStores = new String[vectStores.size()];
            vectStores.copyInto(arrStores);
        }
        return arrStores;
    }

    // JAVADOC COMMENT ELIDED
    synchronized public Vector getCategories(String storeName)
	throws IOException {
        Vector categories = new Vector();
        int listHandle;
        String category;
        listHandle = openCategoryList(storeName);
        if (listHandle != 0) {
            try {
                while ((category = categoryGetNext(listHandle)) != null) {
                    if (category.length() == 0) {
                        break;
                    }
                    categories.addElement(category);
                }
            } finally {
                closeCategoryList(listHandle);
            }
        }
        return categories;
    }

    // JAVADOC COMMENT ELIDED
    synchronized public void addCategory(String categoryName, String storeName) 
                throws IOException, IllegalArgumentException {
        addCategoryImpl(storeName, categoryName);
    }

    // JAVADOC COMMENT ELIDED
    synchronized public void deleteCategory(String categoryName,
                            String storeName) throws IOException{
        deleteCategoryImpl(storeName, categoryName);
    }

    // JAVADOC COMMENT ELIDED    
    synchronized public void addLandmark(String storeName, 
            LandmarkImpl landmark, String category)
            throws IOException,  IllegalArgumentException {
        try {
            if ((landmark.getRecordId()) > 0 &&
                (landmark.getStoreName() == storeName) &&
                (category != null)) {
                try {
                    updateLandmark(storeName, landmark);
                } catch (LandmarkException ex) {
                    landmark.setRecordId(0);
                }
                if (landmark.getRecordId() > 0) {
                    addLandmarkToCategoryImpl(storeName, landmark.getRecordId(), 
                            category);
                    return;
                }
            }
        } catch (IllegalArgumentException e) {
            // if it is happened, the landmark is deleted from store. 
            // Just continue to add Landmark
        }
        landmark.setRecordId(
                addLandmarkToStoreImpl(storeName, landmark, category));
        landmark.setStoreName(storeName);
    }

    // JAVADOC COMMENT ELIDED    
    synchronized public void deleteLandmark(String storeName, 
            LandmarkImpl lm) throws IOException, LandmarkException {

        // If the store names are not the same or
        // they are the same and both are null (for the default LMS)
        // but the landmark does not belong to a LMS
        
        if ((lm.getStoreName() != storeName) || (lm.getRecordId() == 0)) {
            throw new LandmarkException("This landmark belongs to a " +
		"different store: " + lm.getStoreName());
	}
        deleteLandmarkFromStoreImpl(storeName, lm.getRecordId());
        
    }

    // JAVADOC COMMENT ELIDED
    public Enumeration getLandmarksEnumeration(String storeName, 
                                    String category, String name,
                                    double minLatitude,
				    double maxLatitude, double minLongitude,
				    double maxLongitude) throws IOException {
        
        Vector landmarks = new Vector();
        int landmarkID = 0;
        int listHandle;
        listHandle = openLandmarkList(storeName, category);
        if (listHandle != 0) {
            try {
                boolean val = true;
                do {
                    LandmarkImpl landmark = new LandmarkImpl("", null, 
                                                                null, null);
                    landmarkID = landmarkGetNext(listHandle, landmark);
                    if (landmarkID != 0) { 
                        if (name != null) {
                            if (!name.equals(landmark.getName())) {
                                continue;
                            }
                        }
                        if (landmark.getQualifiedCoordinates() != null) {
                            double lat = landmark.getQualifiedCoordinates().
                                    getLatitude();
                            double lon = landmark.getQualifiedCoordinates().
                                    getLongitude();
                            if (minLongitude > maxLongitude) {
                                val = (minLatitude <= lat) && (maxLatitude >= lat)
                                    && ((minLongitude < lon) || (maxLongitude > lon));
                            } else {
                                val = (minLatitude <= lat) && (minLongitude <= lon) &&
                                    (maxLongitude >= lon) && (maxLatitude >= lat);
                            }
                        }
                        if (val) {
                            landmark.setRecordId(landmarkID);
                            landmark.setStoreName(storeName);
                            landmarks.addElement(landmark);
                        }
                    }
                } while (landmarkID != 0);
            } finally {
                closeLandmarkList(listHandle);
            }

            Enumeration en = landmarks.elements();

            if (en.hasMoreElements()) {
                return en;
            }
        }
        
        return null;
    }

    // JAVADOC COMMENT ELIDED
    public void removeLandmarkFromCategory(String storeName, 
            LandmarkImpl lm, String category) throws IOException {
        if ((lm.getRecordId() > 0) && (lm.getStoreName() == storeName)) {
            deleteLandmarkFromCategoryImpl(
                    storeName, lm.getRecordId(), category);
        }
    }

    // JAVADOC COMMENT ELIDED    
    synchronized public void updateLandmark(String storeName, 
            LandmarkImpl landmark) throws IOException, LandmarkException {
        if ((landmark.getRecordId()) > 0 &&
            (landmark.getStoreName() == storeName)) {
            try {
                updateLandmarkImpl(storeName, landmark.getRecordId(), 
                        landmark);
            } catch (IllegalArgumentException ex) {
                throw new LandmarkException(ex.getMessage());
            }
            return;
        } else {
            throw new LandmarkException(
                    "Landmark does not belong to this store");
        }
    }

    // JAVADOC COMMENT ELIDED
    private static native void createLandmarkStore(String name) 
        throws IOException;

    // JAVADOC COMMENT ELIDED    
    private static native void removeLandmarkStore(String name) 
        throws IOException;

    // JAVADOC COMMENT ELIDED
    private static native int openLandmarkStoreList();

    // JAVADOC COMMENT ELIDED
    private static native void closeLandmarkStoreList(int listHandle);

    // JAVADOC COMMENT ELIDED
    private static native String landmarkStoreGetNext(int listHandle);

    // JAVADOC COMMENT ELIDED
    private static native int openCategoryList(String name);

    // JAVADOC COMMENT ELIDED
    private static native void closeCategoryList(int listHandle);

    // JAVADOC COMMENT ELIDED
    private static native String categoryGetNext(int listHandle);

    // JAVADOC COMMENT ELIDED    
    private static native void addCategoryImpl(String storeName,
                                                        String categoryName);

    // JAVADOC COMMENT ELIDED    
    private static native void deleteCategoryImpl(String storeName, 
                                                        String categoryName);

    // JAVADOC COMMENT ELIDED
    private static native void addLandmarkToCategoryImpl(String storeName,
            int landmarkID, String categoryName);

    // JAVADOC COMMENT ELIDED    
    private static native int addLandmarkToStoreImpl(String storeName,
            LandmarkImpl landmark, String categoryName);

    // JAVADOC COMMENT ELIDED    
    private static native void deleteLandmarkFromStoreImpl(String storeName,
            int landmarkID);

    // JAVADOC COMMENT ELIDED
    private static native int openLandmarkList(String storeName, 
            String categoryName);

    // JAVADOC COMMENT ELIDED
    private static native void closeLandmarkList(int listHandle);

    // JAVADOC COMMENT ELIDED
    private static native int landmarkGetNext(int listHandle, 
            LandmarkImpl landmark);

    // JAVADOC COMMENT ELIDED 
    private static native void deleteLandmarkFromCategoryImpl(String storeName,
            int landmarkID, String category);

    // JAVADOC COMMENT ELIDED    
    private static native void updateLandmarkImpl(String storeName, 
            int landmarkID, LandmarkImpl landmark);

}
