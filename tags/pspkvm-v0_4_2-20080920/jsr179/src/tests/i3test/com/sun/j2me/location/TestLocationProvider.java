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
import javax.microedition.location.LocationProvider;
import javax.microedition.location.LocationException;
import javax.microedition.location.Criteria;
import javax.microedition.location.QualifiedCoordinates;
import javax.microedition.location.Coordinates;
import javax.microedition.location.AddressInfo;
import javax.microedition.location.Location;

import com.sun.j2me.location.StubLocationListener;
import com.sun.j2me.location.StubProximityListener;

/**
 * Tests for selecting location provider.
 *
 */
public class TestLocationProvider extends TestCase {
    
    /**
     * Body of the test 1.
     *
     * This test emulates a situation when no providers
     * are available. LocationProvider.getInstance()
     * should cause a LocationException.
     */
    void Test1() {
	    // reset the Stub provider
            LocationProviderImpl[] providers = 
                LocationProviderImpl.getProviders();
            Criteria cr = null;
            PlatformLocationProvider pr = null;
            for (int i = 0; i < providers.length; i++) {
                cr = providers[i].criteria;
                pr = (PlatformLocationProvider) providers[i];
                pr.i3test_setState(LocationProvider.OUT_OF_SERVICE);
            }
	    try {
	        LocationProvider testLP = 
		    LocationProvider.getInstance(cr);
		fail("LocationException wasn't caused");
	    } catch (LocationException e) {
	        assertTrue("LocationException was caused", true);
	    } catch (Throwable e) {
                fail("" +e +" was caused");
	    }
    }
    
    /**
     * Body of the test 2.
     *
     * This test emulates a situation when all providers
     * are not match for current criteria but they are not 
     * out of service. LocationProvider.getInstance()
     * should return null.
     */
    void Test2() {
	    // reset the Stub provider
	    // criteria is not matching
            LocationProviderImpl[] providers = 
                LocationProviderImpl.getProviders();
            for (int i = 0; i < providers.length; i++) {
                PlatformLocationProvider pr = 
                        (PlatformLocationProvider) providers[i];
                pr.criteria.setCostAllowed(true);
                pr.i3test_setState(
                        LocationProvider.TEMPORARILY_UNAVAILABLE);
            }
	    try {
	        Criteria cr1 = new Criteria();
	        cr1.setCostAllowed(false);
		LocationProvider testLP = 
		    LocationProvider.getInstance(cr1);
		assertTrue(testLP == null);
	    } catch (Throwable e) {
                fail("" +e +" was caused");
	    }
    }
    
    /**
     * Body of the test 3.
     *
     * This test emulates a situation when the provider
     * which matched for current criteria is available. 
     * LocationProvider.getInstance() should return an instance.
     */
    void Test3() {
	    // reset the Stub provider
	    // criteria is matching
            LocationProviderImpl[] providers = 
                LocationProviderImpl.getProviders();
            Criteria cr = null;
            for (int i = 0; i < providers.length; i++) {
                PlatformLocationProvider pr = 
                        (PlatformLocationProvider) providers[i];
                cr = pr.criteria;
                pr.i3test_setState(
                        LocationProvider.TEMPORARILY_UNAVAILABLE);
            }
	    try {
		LocationProvider testLP = 
		    LocationProvider.getInstance(cr);
		assertTrue(testLP != null);
	    } catch (Throwable e) {
                fail("" +e +" was caused");
	    }
    }
    
    /**
     * Body of the test 4.
     *
     * This test requests a location from location provider
     * with wrong timeout value. java.lang.IllegalArgumentException 
     * should be caused in this case
     */
    void Test4() {
	    // reset the Stub provider
	    // criteria is matching
            LocationProviderImpl[] providers = 
                LocationProviderImpl.getProviders();
            Criteria cr = null;
            for (int i = 0; i < providers.length; i++) {
                PlatformLocationProvider pr = 
                        (PlatformLocationProvider) providers[i];
                cr = pr.criteria;
                pr.i3test_setState(
                        LocationProvider.TEMPORARILY_UNAVAILABLE);
            }

	    LocationProvider testLP;
            try {
                testLP = LocationProvider.getInstance(cr);
		testLP.getLocation(0);
		fail("No IllegalArgumentException when timeout is 0");
	    } catch (java.lang.IllegalArgumentException e) {
		assertTrue("IllegalArgumentException was caused", true);
	    } catch (Throwable e) {
                fail("" +e +" was caused");
	    }
            try {
                testLP = LocationProvider.getInstance(null);
		testLP.getLocation(-2);
		fail("No IllegalArgumentException when timeout is -2");
	    } catch (java.lang.IllegalArgumentException e) {
		assertTrue("IllegalArgumentException was caused", true);
	    } catch (Throwable e) {
                fail("" +e +" was caused");
	    }
    }
    
    /**
     * Body of the test 5.
     *
     * This test sets a location listener with wrong time parameters.
     * java.lang.IllegalArgumentException should be caused in this case
     */
    void Test5() {
	    // reset the Stub provider
	    // criteria is matching
            LocationProviderImpl[] providers = 
                LocationProviderImpl.getProviders();
            Criteria cr = null;
            for (int i = 0; i < providers.length; i++) {
                PlatformLocationProvider pr = 
                        (PlatformLocationProvider) providers[i];
                cr = pr.criteria;
                pr.i3test_setState(
                        LocationProvider.TEMPORARILY_UNAVAILABLE);
            }
	    LocationProvider testLP;
	    StubLocationListener testListener = 
	        new StubLocationListener();
            try {
                testLP = LocationProvider.getInstance(cr);
		// interval < 1
		testLP.setLocationListener(testListener, -5, 5, 5);
		fail("No IllegalArgumentException when interval < 1");
	    } catch (java.lang.IllegalArgumentException e) {
		assertTrue("IllegalArgumentException was caused", true);
	    } catch (Throwable e) {
                fail("" +e +" was caused");
	    }
            try {
                testLP = LocationProvider.getInstance(cr);
		// interval != -1 and timeout > interval 
		testLP.setLocationListener(testListener, 5, 10, 5);
		fail("No IllegalArgumentException when " +
		    "interval != -1 and timeout > interval");
	    } catch (java.lang.IllegalArgumentException e) {
		assertTrue("IllegalArgumentException was caused", true);
	    } catch (Throwable e) {
                fail("" +e +" was caused");
	    }
            try {
                testLP = LocationProvider.getInstance(cr);
		// interval != -1 and maxAge > interval 
		testLP.setLocationListener(testListener, 5, 3, 10);
		fail("No IllegalArgumentException when " +
		    "interval != -1 and maxAge > interval");
	    } catch (java.lang.IllegalArgumentException e) {
		assertTrue("IllegalArgumentException was caused", true);
	    } catch (Throwable e) {
                fail("" +e +" was caused");
	    }
            try {
                testLP = LocationProvider.getInstance(cr);
		// interval != -1 and timeout < 1 and timeout != -1 
		testLP.setLocationListener(testListener, 5, -3, 1);
		fail("No IllegalArgumentException when " +
		    "interval != -1 and timeout < 1 and timeout != -1");
	    } catch (java.lang.IllegalArgumentException e) {
		assertTrue("IllegalArgumentException was caused", true);
	    } catch (Throwable e) {
                fail("" +e +" was caused");
	    }
            try {
                testLP = LocationProvider.getInstance(cr);
		// interval != -1 and maxAge < 1 and maxAge != -1 
		testLP.setLocationListener(testListener, 5, 3, -3);
		fail("No IllegalArgumentException when " +
		    "interval != -1 and maxAge < 1 and maxAge != -1");
	    } catch (java.lang.IllegalArgumentException e) {
		assertTrue("IllegalArgumentException was caused", true);
	    } catch (Throwable e) {
                fail("" +e +" was caused");
	    }
    }
    
    /**
     * Body of the test 6.
     *
     * This test adds a proximity listener with wrong parameters.
     */
    void Test6() {
	    // reset the Stub provider
	    // criteria is matching
            LocationProviderImpl[] providers = 
                LocationProviderImpl.getProviders();
            Criteria cr = null;
            for (int i = 0; i < providers.length; i++) {
                PlatformLocationProvider pr = 
                        (PlatformLocationProvider) providers[i];
                cr = pr.criteria;
                pr.i3test_setState(
                        LocationProvider.TEMPORARILY_UNAVAILABLE);
            }
	    LocationProvider testLP;
	    Coordinates testCoord = new Coordinates(3.5, 4.5, 5.5f);
	    StubProximityListener testListener = 
	        new StubProximityListener();
            try {
                testLP = LocationProvider.getInstance(cr);
		// proximity radius is 0
		testLP.addProximityListener(testListener, testCoord, 0f);
		fail("No IllegalArgumentException when " +
		    "proximity radius is 0");
	    } catch (java.lang.IllegalArgumentException e) {
		assertTrue("IllegalArgumentException was caused", true);
	    } catch (Throwable e) {
                fail("" +e +" was caused");
	    }
            try {
                testLP = LocationProvider.getInstance(cr);
		// proximity radius is negative
		testLP.addProximityListener(testListener, testCoord, -5.5f);
		fail("No IllegalArgumentException when " +
		    "proximity radius is negative");
	    } catch (java.lang.IllegalArgumentException e) {
		assertTrue("IllegalArgumentException was caused", true);
	    } catch (Throwable e) {
                fail("" +e +" was caused");
	    }
            try {
                testLP = LocationProvider.getInstance(cr);
		// proximity radius is Float.NaN
		testLP.addProximityListener(testListener, testCoord, Float.NaN);
		fail("No IllegalArgumentException when " +
		    "proximity radius is Float.NaN");
	    } catch (java.lang.IllegalArgumentException e) {
		assertTrue("IllegalArgumentException was caused", true);
	    } catch (Throwable e) {
                fail("" +e +" was caused");
	    }
            try {
                testLP = LocationProvider.getInstance(cr);
		// proximity listener is null
		testLP.addProximityListener(null, testCoord, 10.35f);
		fail("No NullPointerException when " +
		    "proximity listener is null");
	    } catch (java.lang.NullPointerException e) {
		assertTrue("NullPointerException was caused", true);
	    } catch (Throwable e) {
                fail("" +e +" was caused");
	    }
            try {
                testLP = LocationProvider.getInstance(cr);
		// Coordinate object is null
		testLP.addProximityListener(testListener, null, 10.35f);
		fail("No NullPointerException when " +
		    "coordinate object is null");
	    } catch (java.lang.NullPointerException e) {
		assertTrue("NullPointerException was caused", true);
	    } catch (Throwable e) {
                fail("" +e +" was caused");
	    }
    }
    
    /**
     * Body of the test 7.
     *
     * This test removes a proximity listener with wrong parameter.
     */
    void Test7() {
	    // reset the Stub provider
	    // criteria is matching
            LocationProviderImpl[] providers = 
                LocationProviderImpl.getProviders();
            Criteria cr = null;
            for (int i = 0; i < providers.length; i++) {
                PlatformLocationProvider pr = 
                        (PlatformLocationProvider) providers[i];
                cr = pr.criteria;
                pr.i3test_setState(
                        LocationProvider.TEMPORARILY_UNAVAILABLE);
            }
	    LocationProvider testLP;
            try {
                testLP = LocationProvider.getInstance(cr);
		// proximity listener is null
		testLP.removeProximityListener(null);
		fail("No NullPointerException when " +
		    "proximity listener is null");
	    } catch (java.lang.NullPointerException e) {
		assertTrue("NullPointerException was caused", true);
	    } catch (Throwable e) {
                fail("" +e +" was caused");
	    }
    }
    
    /**
     * Body of the test 8.
     *
     * Catch events by location listener.
     */
    void Test8() {
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
	
        // save the example of location into stub provider
        StubLocationListener testListener = new StubLocationListener();
	testListener.setCurrLocation(null); // internal location is null

        LocationProviderImpl[] providers = 
            LocationProviderImpl.getProviders();
	try {
            for (int i = 0; i < providers.length; i++) {
                PlatformLocationProvider pr = 
                        (PlatformLocationProvider) providers[i];
                pr.i3test_setState(
                        LocationProvider.AVAILABLE);

                pr.i3test_setLocation(new LocationImpl(testCoord, 
                        300f, 100f, Location.MTE_SATELLITE, testAddr, 
                        true));
                
                pr.setLocationListener(testListener, -1, -1, -1);
                // wait the timeout value to receive the first location
                Thread.currentThread().sleep(
                        pr.getDefaultInterval() * 1000 + 500);
                // check that new location is arrived
                assertTrue("1", compareLocations(
                    pr.getLocation(pr.getResponseTime()*10), 
                    testListener.getCurrLocation()));
                testListener.setCurrLocation(null); // once more clear 
                                                // internal location
                // wait an interval
                Thread.currentThread().sleep(
                        pr.getDefaultInterval() * 1000 + 500);
                // check that new location is arrived
                assertTrue("2", 
                    compareLocations(pr.getLocation(pr.getResponseTime()*10),
                    testListener.getCurrLocation()));
                // stop listener
                pr.setLocationListener(null, -1, -1, -1);
                testListener.setCurrLocation(null); // once more clear 
                                                // internal location
                // wait an interval
                Thread.currentThread().sleep(
                        pr.getDefaultInterval() * 1000 + 500);
                // check that new location is not arrived
                assertTrue("3",
                    !compareLocations(pr.getLocation(pr.getResponseTime()*10),
                    testListener.getCurrLocation()));
                // ask state every 5 seconds

                pr.i3test_setState(
                        LocationProvider.AVAILABLE);
                // set listener to send state updates only - interval is 0
                pr.setLocationListener(testListener, 0, -1, -1);
                // wait 5 seconds for starting listener
                Thread.currentThread().sleep(
                        pr.getStateInterval() * 1000 + 500);
                // change state
                pr.i3test_setState(
                    LocationProvider.TEMPORARILY_UNAVAILABLE);
                // wait 5 seconds
                Thread.currentThread().sleep(
                        pr.getStateInterval() * 1000 + 500);
                // check the internal state of listener
                assertTrue("4", testListener.getCurrState() == 
                    LocationProvider.TEMPORARILY_UNAVAILABLE);
            }
        } catch (Throwable e) {
                fail("" +e +" was caused");
	}
	
    }
    
    /**
     * Body of the test 9.
     *
     * Catch events by proximity listener.
     */
    void Test9() {
        QualifiedCoordinates testCoord = new QualifiedCoordinates(
	    0, 0, 0f, 2f, 2f); // point on Equator
	Coordinates newCoord = null;
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
        LocationProvider testLP;
        StubLocationListener testListener = new StubLocationListener();
	testListener.setCurrLocation(null); // internal location is null
	StubProximityListener testProxyListener1 = 
	    new StubProximityListener();

        LocationProviderImpl[] providers = 
            LocationProviderImpl.getProviders();

	try {
            for (int i = 0; i < providers.length; i++) {
                PlatformLocationProvider pr = 
                        (PlatformLocationProvider) providers[i];
                // current state - available
                pr.i3test_setState(
                        LocationProvider.AVAILABLE);
                
                pr.i3test_setLocation(new LocationImpl(testCoord, 
                        300f, 100f, Location.MTE_SATELLITE, testAddr, 
                        true));

                pr.setLocationListener(testListener, -1, -1, -1);
                // wait the timeout value to receive the first location
                Thread.currentThread().sleep(
                        pr.getDefaultInterval() * 1000 + 500);

                newCoord = new Coordinates(30.0, 30.0, 0f);

                pr.addProximityListener(testProxyListener1, 
                    newCoord, 300000f);
                // wait for interval that proximity event should be called
                Thread.currentThread().sleep(
                        pr.getDefaultInterval() * 1000 + 500);
                // proximity event shouldn't be called
                assertTrue("1", testProxyListener1.getCurrLocation() == null);
                // We arrived into proximity radius
                testCoord = new QualifiedCoordinates(29.0, 29.0, 0f, 2f, 2f);
                pr.i3test_setLocation(new LocationImpl(testCoord, 
                        300f, 100f, Location.MTE_SATELLITE, testAddr, 
                        true));
                // wait for interval that proximity event should be called
                Thread.currentThread().sleep(
                        pr.getDefaultInterval() * 1000 + 500);
                // proximity event should be called
                assertTrue("2", compareLocations(
                    testProxyListener1.getCurrLocation(),
                    pr.getLocation(pr.getResponseTime()*10)));
                // clear location in listener
                testProxyListener1.setCurrLocation(null);
                // wait for interval that proximity event should be called
                Thread.currentThread().sleep(
                        pr.getDefaultInterval() * 1000 + 500);
                // proximity event shouldn't be called - listener was removed
                assertTrue("3", testProxyListener1.getCurrLocation() == null);
                // add proximity listener nex time
                newCoord = new Coordinates(0.0, 0.0, 0f);
                pr.addProximityListener(testProxyListener1, 
                    newCoord, 300000f);
                // wait for interval that proximity event should be called
                Thread.currentThread().sleep(
                        pr.getDefaultInterval() * 1000 + 500);
                // proximity event shouldn't be called
                assertTrue("4", testProxyListener1.getCurrLocation() == null);
                // remove proximity listener
                pr.removeProximityListener(testProxyListener1);
                // move to area near new point
                testCoord = new QualifiedCoordinates(1.0, 1.0, 0f, 2f, 2f);

                pr.i3test_setLocation(new LocationImpl(testCoord, 
                        300f, 100f, Location.MTE_SATELLITE, testAddr, 
                        true));
                // wait for interval that proximity event should be called
                Thread.currentThread().sleep(
                        pr.getDefaultInterval() * 1000 + 500);
                // proximity event shouldn't be called
                assertTrue("5", testProxyListener1.getCurrLocation() == null);
            }
        } catch (Throwable e) {
                fail("" +e +" was caused");
	}
	    
    }
    
    
    /**
     * Compare two locations.
     *
     * @param loc1 location 1 for comparing.
     * @param loc2 location 2 for comparing.
     * @return comparing result: true or false
     */
    private boolean compareLocations(Location loc1, Location loc2) {
        boolean returnValue = true;
	returnValue &= (loc1 != null) && (loc2 != null);
	if (returnValue) {
	    returnValue &= (loc1.getSpeed() == loc2.getSpeed());
	}
	if (returnValue) {
	    returnValue &= (loc1.getCourse() == loc2.getCourse());
	}
	if (returnValue) {
	    returnValue &= 
	        (loc1.getLocationMethod() == loc2.getLocationMethod());
	}
	if (returnValue) {
	    QualifiedCoordinates coord1 = loc1.getQualifiedCoordinates();
	    QualifiedCoordinates coord2 = loc2.getQualifiedCoordinates();
	    returnValue &= (coord1.getLatitude() == coord2.getLatitude());
	    returnValue &= (coord1.getLongitude() == coord2.getLongitude());
	    returnValue &= (coord1.getAltitude() == coord2.getAltitude());
	    returnValue &= (coord1.getHorizontalAccuracy() == 
	        coord2.getHorizontalAccuracy());
	    returnValue &= (coord1.getVerticalAccuracy() == 
	        coord2.getVerticalAccuracy());
	}
	if (returnValue) {
	    AddressInfo addr1 = loc1.getAddressInfo();
	    AddressInfo addr2 = loc2.getAddressInfo();
	    returnValue &= (addr1.getField(AddressInfo.EXTENSION).equals(
	        addr2.getField(AddressInfo.EXTENSION)));
	    returnValue &= (addr1.getField(AddressInfo.STREET).equals(
	        addr2.getField(AddressInfo.STREET)));
	    returnValue &= (addr1.getField(AddressInfo.POSTAL_CODE).equals(
	        addr2.getField(AddressInfo.POSTAL_CODE)));
	    returnValue &= (addr1.getField(AddressInfo.CITY).equals(
	        addr2.getField(AddressInfo.CITY)));
	    returnValue &= (addr1.getField(AddressInfo.COUNTY).equals(
	        addr2.getField(AddressInfo.COUNTY)));
	    returnValue &= (addr1.getField(AddressInfo.STATE).equals(
	        addr2.getField(AddressInfo.STATE)));
	    returnValue &= (addr1.getField(AddressInfo.COUNTRY).equals(
	        addr2.getField(AddressInfo.COUNTRY)));
	    returnValue &= (addr1.getField(AddressInfo.COUNTRY_CODE).equals(
	        addr2.getField(AddressInfo.COUNTRY_CODE)));
	    returnValue &= (addr1.getField(AddressInfo.DISTRICT).equals(
	        addr2.getField(AddressInfo.DISTRICT)));
	    returnValue &= (addr1.getField(AddressInfo.BUILDING_NAME).equals(
	        addr2.getField(AddressInfo.BUILDING_NAME)));
	    returnValue &= (addr1.getField(AddressInfo.BUILDING_FLOOR).equals(
	        addr2.getField(AddressInfo.BUILDING_FLOOR)));
	    returnValue &= (addr1.getField(AddressInfo.BUILDING_ROOM).equals(
	        addr2.getField(AddressInfo.BUILDING_ROOM)));
	    returnValue &= (addr1.getField(AddressInfo.BUILDING_ZONE).equals(
	        addr2.getField(AddressInfo.BUILDING_ZONE)));
	    returnValue &= (addr1.getField(AddressInfo.CROSSING1).equals(
	        addr2.getField(AddressInfo.CROSSING1)));
	    returnValue &= (addr1.getField(AddressInfo.CROSSING2).equals(
	        addr2.getField(AddressInfo.CROSSING2)));
	    returnValue &= (addr1.getField(AddressInfo.URL).equals(
	        addr2.getField(AddressInfo.URL)));
	    returnValue &= (addr1.getField(AddressInfo.PHONE_NUMBER).equals(
	        addr2.getField(AddressInfo.PHONE_NUMBER)));
	}
	return returnValue;
    }
    
    /**
     * Tests execute
     *
     */
    public void runTests() {
	declare("Location providers are unavaible");
        Test1();
        declare("Criteria is not match");
	Test2();
        declare("Criteria is match");
	Test3();
        declare("Timeout value of getLocation() method is wrong");
	Test4();
        declare("Time parameters of setLocationListener() method are wrong");
	Test5();
        declare("Parameters of addProximityListener() method are wrong");
	Test6();
        declare("Parameters of removeProximityListener() method are wrong");
	Test7();
        declare("Catch events by location listener");
	Test8();
	declare("Catch events by proximity listener");
	Test9();
    }
    
}
