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
import javax.microedition.location.Landmark;
import javax.microedition.location.LandmarkException;
import javax.microedition.location.QualifiedCoordinates;
import javax.microedition.location.Coordinates;
import javax.microedition.location.AddressInfo;

/**
 * Tests for Landmark implementation.
 *
 */
public class TestLandmark extends TestCase {
    
    /**
     * Body of the test 1.
     *
     * This test emulates a situation when the name 
     * is null when Landmark is created.
     * Should be caused a NullPointerException.
     */
    void Test1() {
	    QualifiedCoordinates testCoord = new QualifiedCoordinates(
	        0, 0, 0f, 0f, 0f);
	    AddressInfo testAddr = new AddressInfo();
	    try {
		Landmark testLandmark = new Landmark(null, "Descr",
		    testCoord, testAddr);
		fail("NullPointerException wasn't caused");
	    } catch (java.lang.NullPointerException e) {
	        assertTrue("NullPointerException was caused", true);
	    } catch (Throwable e) {
		fail("Wrong exception was caused");
	    }
    }
    
    /**
     * Body of the test 2.
     *
     * This test sets and checks the name
     * of landmark.
     */
    void Test2() {
        QualifiedCoordinates testCoord = new QualifiedCoordinates(
	    0, 0, 0f, 0f, 0f);
	AddressInfo testAddr = new AddressInfo();
	Landmark testLandmark = new Landmark("testName", "Descr",
	    testCoord, testAddr);
	assertTrue(testLandmark.getName() == "testName");
    }
    
    /**
     * Body of the test 3.
     *
     * This test sets and checks the description
     * of landmark.
     */
    void Test3() {
        QualifiedCoordinates testCoord = new QualifiedCoordinates(
	    0, 0, 0f, 0f, 0f);
	AddressInfo testAddr = new AddressInfo();
	Landmark testLandmark = new Landmark("Name", "testDescr",
	    testCoord, testAddr);
	assertTrue(testLandmark.getDescription() == "testDescr");
    }
    
    /**
     * Body of the test 4.
     *
     * This test sets and checks the coordinates
     * of landmark.
     */
    void Test4() {
        QualifiedCoordinates testCoord = new QualifiedCoordinates(
	    1, 2, 3f, 4f, 5f);
	AddressInfo testAddr = new AddressInfo();
	Landmark testLandmark = new Landmark("Name", "testDescr",
	    testCoord, testAddr);
	QualifiedCoordinates savedCoord = 
	    testLandmark.getQualifiedCoordinates();
	assertTrue(savedCoord.getLatitude() ==
	    testCoord.getLatitude());
	assertTrue(savedCoord.getLongitude() ==
	    testCoord.getLongitude());
	assertTrue(savedCoord.getAltitude() ==
	    testCoord.getAltitude());
	assertTrue(savedCoord.getHorizontalAccuracy() ==
	    testCoord.getHorizontalAccuracy());
	assertTrue(savedCoord.getVerticalAccuracy() ==
	    testCoord.getVerticalAccuracy());
    }
    
    /**
     * Tests execute
     *
     */
    public void runTests() {
        declare("Name is null when landmark is created");
	Test1();
        declare("Set/get name");
	Test2();
        declare("Set/get description");
	Test3();
        declare("Set/get coordinates");
	Test4();
    }
    
}
