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

import com.sun.midp.main.*;
import com.sun.midp.security.Permissions;

/**
 * Implementation of Location Provider.
 */
public class PlatformLocationProvider extends LocationProviderImpl {
    /** The name of Location Provider */
    private String providerName;
    /** The handler of Location Provider */
    private int    provider;
    
    /** max size of Location Info in bytes */
    private final static int MAX_LOCATION_BUFEER_SIZE = 1000;

    /** Input buffer for retrieve Location Info */
    private static byte[] lastKnownLocationBuffer = 
            new byte[MAX_LOCATION_BUFEER_SIZE];

    /** Location Provider properties */
    LocationProviderInfo providerInfo = new LocationProviderInfo();

    /**
     * Set of I3test variables
     */
     /** I3Test variable - provider state */
    private int i3testProviderState = 0; /* undefined by default */
     /** I3Test location - last location */    
    private LocationImpl i3testLocation = null; /* undefined by default */
     
     
    // JAVADOC COMMENT ELIDED
    public PlatformLocationProvider(String name) throws IllegalAccessException {
    System.out.println("PlatformLocationProvider:"+name);
        /* open connection to the provider */
        provider = open(name);

        if (provider == 0) {
            throw new IllegalAccessException("Provider " + name + 
                    " is not supported");
        }
            
        /* create and fill criteria */
        if (getCriteria(name, providerInfo)) {
            criteria.setHorizontalAccuracy(providerInfo.horizontalAccuracy);
            criteria.setVerticalAccuracy(providerInfo.verticalAccuracy);
            criteria.setPreferredResponseTime(providerInfo.averageResponseTime);
            criteria.setPreferredPowerConsumption(
                    providerInfo.powerConsumption);
            criteria.setCostAllowed(providerInfo.incurCost);
            criteria.setSpeedAndCourseRequired(
                    providerInfo.canReportSpeedCource);
            criteria.setAltitudeRequired(providerInfo.canReportAltitude);
            criteria.setAddressInfoRequired(providerInfo.canReportAddressInfo);
        }
    }


    // JAVADOC COMMENT ELIDED
    protected LocationImpl updateLocation(long timeout) 
            throws LocationException {
        LocationImpl location = null;
        
        /* request location update */
        long startTimestamp = System.currentTimeMillis();
        if (i3testLocation == null) {
            if (waitForNewLocation(provider, timeout)) {
                /* get location and calculate timestamp  */
                long endTimestamp = System.currentTimeMillis();
                long timestamp = 
                        endTimestamp - providerInfo.averageResponseTime;
                if (timestamp < startTimestamp) {
                    timestamp = endTimestamp;
                }
                location = getNewLocationImpl(timestamp);
            }
        } else {
            location = i3testLocation;
        }
        return location;
    } 

    // JAVADOC COMMENT ELIDED
    synchronized public LocationImpl getLastLocation() {
        LocationInfo locationInfo = new LocationInfo();
        LocationImpl location = null;
        
        if (getLastLocationImpl(provider, locationInfo)) {
            location = locationInfo2Location(locationInfo);
        }
        return location;
    }

    // JAVADOC COMMENT ELIDED
    private static LocationImpl locationInfo2Location(LocationInfo locationInfo) {
        QualifiedCoordinates coordinates = new QualifiedCoordinates(
                locationInfo.latitude, locationInfo.longitude,
                locationInfo.altitude, locationInfo.horizontalAccuracy,
                locationInfo.verticalAccuracy);

        AddressInfo address = new AddressInfo();

        if (locationInfo.isAddressInfo) {
            if (locationInfo.AddressInfo_EXTENSION != null) {
                address.setField(AddressInfo.EXTENSION, 
                        locationInfo.AddressInfo_EXTENSION);
            }
            if (locationInfo.AddressInfo_STREET != null) {
                address.setField(AddressInfo.STREET, 
                        locationInfo.AddressInfo_STREET);
            }
            if (locationInfo.AddressInfo_POSTAL_CODE != null) {
                address.setField(AddressInfo.POSTAL_CODE, 
                        locationInfo.AddressInfo_POSTAL_CODE);
            }
            if (locationInfo.AddressInfo_CITY != null) {
                address.setField(AddressInfo.CITY, 
                        locationInfo.AddressInfo_CITY);
            }
            if (locationInfo.AddressInfo_COUNTY != null) {
                address.setField(AddressInfo.COUNTY, 
                        locationInfo.AddressInfo_COUNTY);
            }
            if (locationInfo.AddressInfo_STATE != null) {
                address.setField(AddressInfo.STATE, 
                        locationInfo.AddressInfo_STATE);
            }
            if (locationInfo.AddressInfo_COUNTRY != null) {
                address.setField(AddressInfo.COUNTRY, 
                        locationInfo.AddressInfo_COUNTRY);
            }
            if (locationInfo.AddressInfo_COUNTRY_CODE != null) {
                address.setField(AddressInfo.COUNTRY_CODE, 
                        locationInfo.AddressInfo_COUNTRY_CODE);
            }
            if (locationInfo.AddressInfo_DISTRICT != null) {
                address.setField(AddressInfo.DISTRICT, 
                        locationInfo.AddressInfo_DISTRICT);
            }
            if (locationInfo.AddressInfo_BUILDING_NAME != null) {
                address.setField(AddressInfo.BUILDING_NAME, 
                        locationInfo.AddressInfo_BUILDING_NAME);
            }
            if (locationInfo.AddressInfo_BUILDING_FLOOR != null) {
                address.setField(AddressInfo.BUILDING_FLOOR, 
                        locationInfo.AddressInfo_BUILDING_FLOOR);
            }
            if (locationInfo.AddressInfo_BUILDING_ROOM != null) {
                address.setField(AddressInfo.BUILDING_ROOM, 
                        locationInfo.AddressInfo_BUILDING_ROOM);
            }
            if (locationInfo.AddressInfo_BUILDING_ZONE != null) {
                address.setField(AddressInfo.BUILDING_ZONE, 
                        locationInfo.AddressInfo_BUILDING_ZONE);
            }
            if (locationInfo.AddressInfo_CROSSING1 != null) {
                address.setField(AddressInfo.CROSSING1, 
                        locationInfo.AddressInfo_CROSSING1);
            }
            if (locationInfo.AddressInfo_CROSSING2 != null) {
                address.setField(AddressInfo.CROSSING2, 
                        locationInfo.AddressInfo_CROSSING2);
            }
            if (locationInfo.AddressInfo_URL != null) {
                address.setField(AddressInfo.URL, 
                        locationInfo.AddressInfo_URL);
            }
            if (locationInfo.AddressInfo_PHONE_NUMBER != null) {
                address.setField(AddressInfo.PHONE_NUMBER, 
                        locationInfo.AddressInfo_PHONE_NUMBER);
            }
        }
        LocationImpl location = new LocationImpl(coordinates, locationInfo.speed,
             locationInfo.course, locationInfo.method, address,
             locationInfo.isValid);
        location.extraInfoNMEA  = locationInfo.extraInfoNMEA;
        location.extraInfoLIF   = locationInfo.extraInfoLIF;
        location.extraInfoPlain = locationInfo.extraInfoPlain;
        location.extraInfoOther = locationInfo.extraInfoOther;
        location.extraInfoOtherMIMEType = locationInfo.extraInfoOtherMIMEType;
        location.setTimestamp(locationInfo.timestamp);        
        return location;
    }

    // JAVADOC COMMENT ELIDED
    private native boolean getLastLocationImpl(int provider, 
            LocationInfo locationInfo); 

    // JAVADOC COMMENT ELIDED
    synchronized public static Location getLastKnownLocation() {
        LocationInfo locationInfo = new LocationInfo();
        if (getLastKnownLocationImpl(locationInfo)) {
            return  locationInfo2Location(locationInfo);
        }
        return null;
    }

    // JAVADOC COMMENT ELIDED
    private native static boolean getLastKnownLocationImpl(
                                                LocationInfo locationInfo);

    // JAVADOC COMMENT ELIDED
    public int getDefaultInterval() {
        return (providerInfo.defaultInterval >= 1000) ?
            (providerInfo.defaultInterval / 1000) : 1;
    }

    // JAVADOC COMMENT ELIDED
    public int getDefaultMaxAge() {
        return (providerInfo.defaultMaxAge >= 1000) ?
            (providerInfo.defaultMaxAge / 1000) : 1;
    }


    // JAVADOC COMMENT ELIDED
    public int getDefaultTimeout() {
        return (providerInfo.defaultTimeout >= 1000) ?
            (providerInfo.defaultTimeout / 1000) : 1;
    }

    // JAVADOC COMMENT ELIDED
    public int getResponseTime() {
        return (providerInfo.averageResponseTime >= 1000) ?
            (providerInfo.averageResponseTime / 1000) : 1;
    }

    // JAVADOC COMMENT ELIDED
    public int getStateInterval() {
        return (providerInfo.defaultStateInterval >= 1000) ?
            (providerInfo.defaultStateInterval / 1000) : 1;
    }
    
    // JAVADOC COMMENT ELIDED
    public int getState() {
        /* I3Test use only */
        if (i3testProviderState != 0) {
            return i3testProviderState;
        }
        /* Real mode */
        return getStateImpl(provider);
    }

    // JAVADOC COMMENT ELIDED
    LocationImpl getNewLocationImpl(long timestamp) {
        if (receiveNewLocationImpl(provider, timestamp)) {
            return getLastLocation();
        }
        return null;
    }

    // JAVADOC COMMENT ELIDED
    private native boolean receiveNewLocationImpl(int provider, long timestamp);

    // JAVADOC COMMENT ELIDED
    public void reset() {
        resetImpl(provider);
        super.reset();
    }

    // JAVADOC COMMENT ELIDED
    static native String getListOfLocationProviders();

    // JAVADOC COMMENT ELIDED
    private native int open(String name);
    
    // JAVADOC COMMENT ELIDED
    private native boolean getCriteria(String name, 
                LocationProviderInfo criteria);
        
    // JAVADOC COMMENT ELIDED
    private native int getStateImpl(int provider);

    // JAVADOC COMMENT ELIDED
    private native boolean waitForNewLocation(int provider, long timeout);

    // JAVADOC COMMENT ELIDED
    private native void resetImpl(int provider);

    // JAVADOC COMMENT ELIDED
    protected native void finalize();
    
    /**
     * Set of I3test helper functions
     */
    /**
     * Set provider state.
     * I3test use only !!!
     *
     * @param newState new state of Location Provider
     */
    void i3test_setState(int newState) {
        i3testProviderState = newState;
    }

    /**
     * Set Last Location
     * I3test use only !!!
     *
     * @param location 
     */
    void i3test_setLocation(LocationImpl location) {
        i3testLocation = location;
    }
    
}

/**
 * The class contains information about Platform Location Provider
 */
class LocationProviderInfo {
    // JAVADOC COMMENT ELIDED
    boolean incurCost;
    // JAVADOC COMMENT ELIDED
    boolean canReportAltitude;
    // JAVADOC COMMENT ELIDED
    boolean canReportAddressInfo;
    // JAVADOC COMMENT ELIDED
    boolean canReportSpeedCource;
    // JAVADOC COMMENT ELIDED
    int powerConsumption;
    // JAVADOC COMMENT ELIDED
    int horizontalAccuracy;     
    // JAVADOC COMMENT ELIDED
    int verticalAccuracy;       
    // JAVADOC COMMENT ELIDED
    int defaultTimeout;         
    // JAVADOC COMMENT ELIDED
    int defaultMaxAge;          
    // JAVADOC COMMENT ELIDED
    int defaultInterval;        
    // JAVADOC COMMENT ELIDED
    int averageResponseTime;    
    // JAVADOC COMMENT ELIDED
    int defaultStateInterval;
    /**
     * Init class info in the native code
     */
    static {
        initNativeClass();
    }
    /**
     * Initializes native file handler.
     */
    private native static void initNativeClass();
};

/**
 * The class contains information about Platform Location 
 */
class LocationInfo {
    // JAVADOC COMMENT ELIDED
    boolean isValid;
    // JAVADOC COMMENT ELIDED
    long timestamp;
    // JAVADOC COMMENT ELIDED
    double latitude;
    // JAVADOC COMMENT ELIDED
    double longitude;
    // JAVADOC COMMENT ELIDED
    float altitude;
    // JAVADOC COMMENT ELIDED
    float horizontalAccuracy;
    // JAVADOC COMMENT ELIDED
    float verticalAccuracy;
    // JAVADOC COMMENT ELIDED
    float speed;
    // JAVADOC COMMENT ELIDED
    float course;
    // JAVADOC COMMENT ELIDED
    int method;
    // JAVADOC COMMENT ELIDED
    boolean isAddressInfo = false;
    // JAVADOC COMMENT ELIDED
    String AddressInfo_EXTENSION = null;
    // JAVADOC COMMENT ELIDED
    String AddressInfo_STREET = null;
    // JAVADOC COMMENT ELIDED
    String AddressInfo_POSTAL_CODE = null;
    // JAVADOC COMMENT ELIDED
    String AddressInfo_CITY = null;
    // JAVADOC COMMENT ELIDED
    String AddressInfo_COUNTY = null;
    // JAVADOC COMMENT ELIDED
    String AddressInfo_STATE = null;
    // JAVADOC COMMENT ELIDED
    String AddressInfo_COUNTRY = null;
    // JAVADOC COMMENT ELIDED
    String AddressInfo_COUNTRY_CODE = null;
    // JAVADOC COMMENT ELIDED
    String AddressInfo_DISTRICT = null;
    // JAVADOC COMMENT ELIDED
    String AddressInfo_BUILDING_NAME = null;
    // JAVADOC COMMENT ELIDED
    String AddressInfo_BUILDING_FLOOR = null;
    // JAVADOC COMMENT ELIDED
    String AddressInfo_BUILDING_ROOM = null;
    // JAVADOC COMMENT ELIDED
    String AddressInfo_BUILDING_ZONE = null;
    // JAVADOC COMMENT ELIDED
    String AddressInfo_CROSSING1 = null;
    // JAVADOC COMMENT ELIDED
    String AddressInfo_CROSSING2 = null;
    // JAVADOC COMMENT ELIDED
    String AddressInfo_URL = null;
    // JAVADOC COMMENT ELIDED
    String AddressInfo_PHONE_NUMBER = null;
    // JAVADOC COMMENT ELIDED
    String extraInfoNMEA;
    // JAVADOC COMMENT ELIDED
    String extraInfoLIF;
    // JAVADOC COMMENT ELIDED
    String extraInfoPlain;
    // JAVADOC COMMENT ELIDED
    String extraInfoOther;
    // JAVADOC COMMENT ELIDED
    String extraInfoOtherMIMEType = null;
    /**
     * Init class info in the native code
     */
    static {
        initNativeClass();
    }
    /**
     * Initializes native file handler.
     */
    private native static void initNativeClass();
}
