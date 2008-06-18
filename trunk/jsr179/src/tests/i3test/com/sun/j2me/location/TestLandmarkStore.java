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

import com.sun.midp.i3test.TestCase;
import javax.microedition.location.LandmarkStore;
import javax.microedition.location.Landmark;
import javax.microedition.location.LandmarkException;
import javax.microedition.location.QualifiedCoordinates;
import javax.microedition.location.Coordinates;
import javax.microedition.location.AddressInfo;
import java.util.Enumeration;

/**
 * Tests for LandmarkStore implementation.
 *
 */
public class TestLandmarkStore extends TestCase {
    
    /**
     * Body of the test 1.
     *
     * This test creates and removes landmark stores 
     */
    void Test1() {
            String[] listStores = null;
            try {
                listStores = LandmarkStore.listLandmarkStores();
                assertTrue("No exceptions", true);
            } catch (Throwable e) {
                fail("" +e +" was caused");
            }
            if ((listStores != null) && (listStores.length > 0)) { 
                // remove all previous stores
                for (int i = 0; i < listStores.length; i++) {
                    try {
                        LandmarkStore.deleteLandmarkStore(listStores[i]);
                    } catch (Throwable e) {
                        fail("" +e +" was caused");
                    }
                }
            }
            // create and delete a new store
            String testStoreName = "Name1";
            try {
                LandmarkStore.createLandmarkStore(testStoreName);
                listStores = LandmarkStore.listLandmarkStores();
                if (listStores != null) {
                    assertEquals("Wrong length of ListStores", 
                        listStores.length, 1);
                    assertTrue("Wrong name was saved in ListStores",  
                        listStores[0].equals(testStoreName)); 
                } else { // Landmark store wasn't create
                    fail("Landmark store wasn't create");
                }
                // delete Landmark store
                LandmarkStore.deleteLandmarkStore(listStores[0]);
                // check the list
                listStores = LandmarkStore.listLandmarkStores();
                assertTrue("ListStores is not empty", 
                        listStores == null);
            } catch (Throwable e) {
                fail("" +e +" was caused");
            }
            // try to create a landmark store with null name
            try {
                LandmarkStore.createLandmarkStore(null);
                fail("No exception was caused");
            } catch (java.lang.NullPointerException e) {
                assertTrue("NullPointerException was caused", true);
            } catch (Throwable e) {
                fail("" +e +" was caused");
            }
            // try to create twice a landmark store with same name
            try {
                LandmarkStore.createLandmarkStore(testStoreName);
            } catch (Throwable e) {
                fail("" +e +" was caused");
            }
            try {
                LandmarkStore.createLandmarkStore(testStoreName);
                fail("No exception was caused");
            } catch (java.lang.IllegalArgumentException e) {
                assertTrue("IllegalArgumentException was caused", true);
            } catch (Throwable e) {
                fail("" +e +" was caused");
            }
            // try to delete a landmark store with null name
            try {
                LandmarkStore.deleteLandmarkStore(null);
                fail("No exception was caused");
            } catch (java.lang.NullPointerException e) {
                assertTrue("NullPointerException was caused", true);
            } catch (Throwable e) {
                fail("" +e +" was caused");
            }
            // remove created landmark store
            deleteLandStore(testStoreName);
         
    }
    
    /**
     * Body of the test 2.
     *
     * Add and remove categories to landmark store 
     */
    void Test2() {
        String landmarkStoreName = "StoreName";
        String categoryName1      = "Category1";
        String categoryName2      = "Category2";
        LandmarkStore store       = null;
        
        // create a new landmark store
        store = createLandStore(landmarkStoreName);
        // Test the method "addCategory"
        // Name of category is null - NullPointerException
        try {
            store.addCategory(null);
            fail("No exception was caused");
        } catch (java.lang.NullPointerException e) {
            assertTrue("NullPointerException was caused", true);
        } catch (Throwable e) {
            fail("" +e +" was caused");
        }
        // Category is already exsists - IllegalArgumentException
        try {
            store.addCategory(categoryName1);
            assertTrue("OK", true);
        } catch (Throwable e) {
            fail("" +e +" was caused");
        }
        try {
            store.addCategory(categoryName1);
            fail("No exception was caused");
        } catch (java.lang.IllegalArgumentException e) {
            assertTrue("IllegalArgumentException was caused", true);
        } catch (Throwable e) {
            fail("" +e +" was caused");
        }
        // Test the method "deleteCategory"
        // Name of category is null - NullPointerException
        try {
            store.deleteCategory(null);
            fail("No exception was caused");
        } catch (java.lang.NullPointerException e) {
            assertTrue("NullPointerException was caused", true);
        } catch (Throwable e) {
            fail("" +e +" was caused");
        }
        // delete category
        try {
            store.deleteCategory(categoryName1);
            assertTrue("OK", true);
        } catch (Throwable e) {
            fail("" +e +" was caused");
        }
        // check the category list
        Enumeration enumCat = null;
        try {
            enumCat = store.getCategories();
        } catch (Throwable e) {
            fail("" +e +" was caused");
        }
        // Category list should be empty
        if (enumCat.hasMoreElements()) {
            fail("Categories list is not empty");
        } else { // OK
            assertTrue("OK", true);
        }
        try {
            store.addCategory(categoryName1);
            store.addCategory(categoryName2);
        } catch (Throwable e) {
            fail("" +e +" was caused");
        }
        try {
            enumCat = store.getCategories();
        } catch (Throwable e) {
            fail("" +e +" was caused");
        }
        // Category list should consists of 2 categories
        try {
            enumCat = store.getCategories();
            if (!enumCat.hasMoreElements()) {
                fail("Categories list is empty");
            }
            Object currName = enumCat.nextElement();
            if (categoryName1.equals(currName)) {
                assertTrue("OK", true);
            } else {
                fail("Wrong category name: \"" + (String)currName + 
                    "\" on place \"" + categoryName1 + "\"");
            }
            if (!enumCat.hasMoreElements()) {
                fail("Categories list is too small");
            }
            currName = enumCat.nextElement();
            if (categoryName2.equals(currName)) {
                assertTrue("OK", true);
            } else {
                fail("Wrong category name: \"" + (String)currName + 
                    "\" on place \"" + categoryName2 + "\"");
            }
        } catch (Throwable e) {
            fail("" +e +" was caused");
        }
        // delete all categories
        try {
            store.deleteCategory(categoryName1);
            store.deleteCategory(categoryName2);
            assertTrue("OK", true);
        } catch (Throwable e) {
            fail("Exception was caused");
        }
        // remove created landmark store
        deleteLandStore(landmarkStoreName);
    }
    
    /**
     * Body of the test 3.
     *
     * Add and remove landmarks to landmark store 
     */
    void Test3() {
        String landmarkStoreName = "StoreName";
        LandmarkStore store       = null;
        String categoryName1      = "Category1";
        String categoryName2      = "Category2";
        QualifiedCoordinates testCoord = new QualifiedCoordinates(
            1, 2, 3f, 4f, 5f);
        AddressInfo testAddr = new AddressInfo();
        testAddr.setField(AddressInfo.EXTENSION, "EXTENSION");
        testAddr.setField(AddressInfo.STREET, "STREET");
        testAddr.setField(AddressInfo.POSTAL_CODE, "POSTAL_CODE");
        testAddr.setField(AddressInfo.CITY, "CITY");
        testAddr.setField(AddressInfo.COUNTY, "COUNTY");
        testAddr.setField(AddressInfo.STATE, "STATE");
        testAddr.setField(AddressInfo.COUNTRY, "COUNTRY");
        testAddr.setField(AddressInfo.COUNTRY_CODE, "COUNTRY_CODE");
        testAddr.setField(AddressInfo.DISTRICT, "DISTRICT");
        testAddr.setField(AddressInfo.BUILDING_NAME, "BUILDING_NAME");
        testAddr.setField(AddressInfo.BUILDING_FLOOR, "BUILDING_FLOOR");
        testAddr.setField(AddressInfo.BUILDING_ROOM, "BUILDING_ROOM");
        testAddr.setField(AddressInfo.BUILDING_ZONE, "BUILDING_ZONE");
        testAddr.setField(AddressInfo.CROSSING1, "CROSSING1");
        testAddr.setField(AddressInfo.CROSSING2, "CROSSING2");
        testAddr.setField(AddressInfo.URL, "URL");
        testAddr.setField(AddressInfo.PHONE_NUMBER, "PHONE_NUMBER");
        Landmark testLandmark = new Landmark("Name", "testDescr",
            testCoord, testAddr);
        
        // create a new landmark store
        store = createLandStore(landmarkStoreName);
        addCategory(categoryName1, store);
        // try to add a landmark with wrong category name
        try {
            // categoryName2 was not added to store
            store.addLandmark(testLandmark, categoryName2);
            fail("No exception was caused");
        } catch (java.lang.IllegalArgumentException e) {
            assertTrue("IllegalArgumentException was caused", true);
        } catch (Throwable e) {
            fail("" +e +" was caused");
        }
        // try to add a null landmark 
        try {
            store.addLandmark(null, categoryName1);
                fail("No exception was caused");
            } catch (java.lang.NullPointerException e) {
            assertTrue("NullPointerException was caused", true);
        } catch (Throwable e) {
            fail("" +e +" was caused");
        }
        // add a landmark
        try {
            store.addLandmark(testLandmark, categoryName1);
        } catch (Throwable e) {
            fail("" +e +" was caused");
        }
        // get the list of landmarks
        Enumeration enumLandmarks = null;
        try {
            enumLandmarks = store.getLandmarks();
        } catch (Throwable e) {
            fail("" +e +" was caused");
        }
        if ((enumLandmarks == null) || 
                (!enumLandmarks.hasMoreElements())) { // no landmarks
            fail("Landmarks enumeration is empty");
        }
        Landmark testLandmark1 = (Landmark)enumLandmarks.nextElement();
        if ((testLandmark1 == null) || (enumLandmarks.hasMoreElements())) {
            // wrong number of landmarks
            fail("Landmarks enumeration contains wrong number of elements");
        }
        AddressInfo testAddr1 = testLandmark1.getAddressInfo();
        if (!compareAddrInfo(testAddr, testAddr1)) {
            fail("Address info wasn't stored correctly");
        }
        QualifiedCoordinates testCoord1 = 
            testLandmark1.getQualifiedCoordinates();
        if (!compareQualiCoord(testCoord, testCoord1)) {
            fail("Qualified coordinates weren't stored correctly");
        }
        if (!(testLandmark.getName().equals(testLandmark1.getName()))) {
            fail("Landmark namewasn't stored correctly");
        }
        if (!(testLandmark.getName().equals(testLandmark1.getName()))) {
            fail("Landmark name wasn't stored correctly");
        }
        if (!(testLandmark.getDescription().equals(
            testLandmark1.getDescription()))) {
            fail("Landmark description wasn't stored correctly");
        }
        // get landmarks by the category
        try {
            addCategory(categoryName2, store);
            Landmark testLandmark2 = new Landmark("Name2", "testDescr2",
                testCoord, testAddr);
            store.addLandmark(testLandmark2, categoryName2);
            enumLandmarks = store.getLandmarks(categoryName1, null);
            if ((enumLandmarks == null) || 
                    (!enumLandmarks.hasMoreElements())) { // no landmarks
                fail("Landmarks enumeration is empty");
            }
            if (!((Landmark)enumLandmarks.nextElement()).getName().
                    equals("Name")) {
                fail("Category filter doesn't work");
            }
            enumLandmarks = store.getLandmarks(categoryName2, null);
            if ((enumLandmarks == null) || 
                    (!enumLandmarks.hasMoreElements())) { // no landmarks
                fail("Landmarks enumeration is empty");
            }
            if (!((Landmark)enumLandmarks.nextElement()).getName().
                    equals("Name2")) {
                fail("Category filter doesn't work");
            }
            enumLandmarks = store.getLandmarks(null, "Name");
            if ((enumLandmarks == null) || 
                    (!enumLandmarks.hasMoreElements())) { // no landmarks
                fail("Landmarks enumeration is empty");
            }
            if (!((Landmark)enumLandmarks.nextElement()).getName().
                    equals("Name")) {
                fail("Name filter doesn't work");
            }
            enumLandmarks = store.getLandmarks(null, "Name2");
            if ((enumLandmarks == null) || 
                    (!enumLandmarks.hasMoreElements())) { // no landmarks
                fail("Landmarks enumeration is empty");
            }
            if (!((Landmark)enumLandmarks.nextElement()).getName().
                    equals("Name2")) {
                fail("Name filter doesn't work");
            }
        } catch (Throwable e) {
            fail("" +e +" was caused");
        }
        // remove landmark from the category with null parameters
        try {
            store.removeLandmarkFromCategory(null, categoryName1);
            fail("No exception was caused");
        } catch (java.lang.NullPointerException e) {
            assertTrue("NullPointerException was caused", true);
        } catch (Throwable e) {
            fail("" +e +" was caused");
        }
        try {
            store.removeLandmarkFromCategory(testLandmark, null);
            fail("No exception was caused");
        } catch (java.lang.NullPointerException e) {
            assertTrue("NullPointerException was caused", true);
        } catch (Throwable e) {
            fail("" +e +" was caused");
        }
        // remove landmark from the category
        try {
            enumLandmarks = store.getLandmarks(categoryName1, null);
            store.removeLandmarkFromCategory(
                    (Landmark)enumLandmarks.nextElement(),
                    categoryName1);
            // should return null
            enumLandmarks = store.getLandmarks(categoryName1, null); 
            if (!(enumLandmarks == null)) {
                fail("Enumeration should be null");
            }
        } catch (Throwable e) {
            fail("" +e +" was caused");
        }
        // update landmark with null parameter
        try {
            store.updateLandmark(null);
            fail("No exception was caused");
        } catch (java.lang.NullPointerException e) {
            assertTrue("NullPointerException was caused", true);
        } catch (Throwable e) {
            fail("" +e +" was caused");
        }
        // update landmark that is not belong to storage
        Landmark testLandmark3 = new Landmark("Name3", "testDescr3",
            testCoord, testAddr);
        try {
            store.updateLandmark(testLandmark3);
            fail("No exception was caused");
        } catch (LandmarkException e) {
            assertTrue("LandmarkException was caused", true);
        } catch (Throwable e) {
            fail("" +e +" was caused");
        }
        // change landmark name and update landmark
        try {
            enumLandmarks = store.getLandmarks(null, "Name2");
            testLandmark3 = (Landmark)enumLandmarks.nextElement();
            testLandmark3.setName("Name3");
            store.updateLandmark(testLandmark3);
            enumLandmarks = store.getLandmarks(null, "Name3");
            if (enumLandmarks == null) {
                fail("Landmark name wasn't changed");
            }
        } catch (Throwable e) {
            fail("" +e +" was caused");
        }
        // remove created landmark store
        deleteLandStore(landmarkStoreName);
    }
    
    /**
     * Create the landmark store.
     *
     * @param  name the name of new landmark store
     * @return the instance of landmark store
     */
    private LandmarkStore createLandStore(String name) {
        LandmarkStore store = null;
        try {
            LandmarkStore.createLandmarkStore(name);
            store = LandmarkStore.getInstance(name);
        } catch (Throwable e) {
            fail("" +e +" was caused");
        }
        return store;
    }
    
    /**
     * Delete the landmark store.
     *
     * @param  name the name of new landmark store
     */
    private void deleteLandStore(String name) {
        LandmarkStore store = null;
        try {
            LandmarkStore.deleteLandmarkStore(name);
        } catch (Throwable e) {
            fail("" +e +" was caused");
        }
    }
    
    /**
     * Add a category to landmark store.
     *
     * @param  category the name category for adding
     * @param  store the landmark store object
     */
    private void addCategory(String category, LandmarkStore store) {
        try {
            store.addCategory(category);
        } catch (Throwable e) {
            fail("" +e +" was caused");
        }
    }
    
    /**
     * Delete a category from landmark store.
     *
     * @param  category the name category for deleting
     * @param  store the landmark store object
     */
    private void deleteCategory(String category, LandmarkStore store) {
        try {
            store.deleteCategory(category);
        } catch (Throwable e) {
            fail("" +e +" was caused");
        }
    }
    
    /**
     * Compare two adress info objects.
     *
     * @param  addr1 the first address info object
     * @param  addr2 the second address info object
     * @return the comparing result
     */
    private boolean compareAddrInfo(AddressInfo addr1, AddressInfo addr2) {
    return 
            addr1.getField(AddressInfo.EXTENSION).equals(
                addr2.getField(AddressInfo.EXTENSION)) &&
            addr1.getField(AddressInfo.STREET).equals(
                addr2.getField(AddressInfo.STREET)) &&
            addr1.getField(AddressInfo.POSTAL_CODE).equals(
                addr2.getField(AddressInfo.POSTAL_CODE)) &&
            addr1.getField(AddressInfo.CITY).equals(
                addr2.getField(AddressInfo.CITY)) &&
            addr1.getField(AddressInfo.COUNTY).equals(
                addr2.getField(AddressInfo.COUNTY)) &&
            addr1.getField(AddressInfo.STATE).equals(
                addr2.getField(AddressInfo.STATE)) &&
            addr1.getField(AddressInfo.COUNTRY).equals(
                addr2.getField(AddressInfo.COUNTRY)) &&
            addr1.getField(AddressInfo.COUNTRY_CODE).equals(
                addr2.getField(AddressInfo.COUNTRY_CODE)) &&
            addr1.getField(AddressInfo.BUILDING_NAME).equals(
                addr2.getField(AddressInfo.BUILDING_NAME)) &&
            addr1.getField(AddressInfo.DISTRICT).equals(
                addr2.getField(AddressInfo.DISTRICT)) &&
            addr1.getField(AddressInfo.BUILDING_FLOOR).equals(
                addr2.getField(AddressInfo.BUILDING_FLOOR)) &&
            addr1.getField(AddressInfo.BUILDING_ROOM).equals(
                addr2.getField(AddressInfo.BUILDING_ROOM)) &&
            addr1.getField(AddressInfo.BUILDING_ZONE).equals(
                addr2.getField(AddressInfo.BUILDING_ZONE)) &&
            addr1.getField(AddressInfo.CROSSING1).equals(
                addr2.getField(AddressInfo.CROSSING1)) &&
            addr1.getField(AddressInfo.CROSSING2).equals(
                addr2.getField(AddressInfo.CROSSING2)) &&
            addr1.getField(AddressInfo.URL).equals(
                addr2.getField(AddressInfo.URL)) &&
            addr1.getField(AddressInfo.PHONE_NUMBER).equals(
                addr2.getField(AddressInfo.PHONE_NUMBER));
    }
    
    /**
     * Compare two qualified coordinates objects.
     *
     * @param  qualifCoord1 the first object
     * @param  qualifCoord2 the second info object
     * @return the comparing result
     */
    private boolean compareQualiCoord(QualifiedCoordinates qualifCoord1,
        QualifiedCoordinates qualifCoord2) {
    return 
            (qualifCoord1.getHorizontalAccuracy() ==
                qualifCoord2.getHorizontalAccuracy()) &&
            (qualifCoord1.getVerticalAccuracy() ==
                qualifCoord2.getVerticalAccuracy()) &&
            (qualifCoord1.getLatitude() ==
                qualifCoord2.getLatitude()) &&
            (qualifCoord1.getLongitude() ==
                qualifCoord2.getLongitude()) &&
            (qualifCoord1.getAltitude() ==
                qualifCoord2.getAltitude());
    }
    
    /**
     * Tests execute
     *
     */
    public void runTests() {
        declare("Create/delete landmark store");
        Test1();
        declare("Add/remove categories");
        Test2();
        declare("Add/remove landmarks");
        Test3();
    }
    
}
