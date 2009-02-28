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
import com.sun.midp.log.*;

/**
 * This class is an implementation of the <code>LocationProvider</code> class
 * defined by the JSR-179 specification.
 */
// JAVADOC COMMENT ELIDED
public abstract class LocationProviderImpl extends LocationProvider {

    // JAVADOC COMMENT ELIDED
    private static Vector proximityListeners = new Vector();
    
    // JAVADOC COMMENT ELIDED
    protected LocationListener locationListener;

    // JAVADOC COMMENT ELIDED    
    protected Criteria criteria = new Criteria();
    
    // JAVADOC COMMENT ELIDED
    private LocationThread locationThread = null;
    
    // JAVADOC COMMENT ELIDED
    private StateThread stateThread = null;
    
    // JAVADOC COMMENT ELIDED
    private int locationQueries = 0;

    // JAVADOC COMMENT ELIDED
    private boolean resetRequested = false;
    
    // JAVADOC COMMENT ELIDED
    private static final String SEPARATOR = ",";

    // JAVADOC COMMENT ELIDED
    private static LocationProviderImpl[] providers;

    // JAVADOC COMMENT ELIDED
    public boolean matchesCriteria(Criteria criteria) {
	return compareCriterias(criteria, this.criteria);
    }

    // JAVADOC COMMENT ELIDED
    boolean compareCriterias(Criteria c1, Criteria c2) {
        if (!c1.isAllowedToCost() && c2.isAllowedToCost()) {
            return false;
        }
        if (c1.isSpeedAndCourseRequired() && !c2.isSpeedAndCourseRequired()) {
            return false;
        }
        if (c1.isAltitudeRequired() && !c2.isAltitudeRequired()) {
            return false;
        }
        if (c1.isAddressInfoRequired() && !c2.isAddressInfoRequired()) {
            return false;
        }
        if (c1.getHorizontalAccuracy() != Criteria.NO_REQUIREMENT &&
            c1.getHorizontalAccuracy() < c2.getHorizontalAccuracy()) {
            return false;
        }
        if (c1.getVerticalAccuracy() != Criteria.NO_REQUIREMENT &&
            c1.getVerticalAccuracy() < c2.getVerticalAccuracy()) {
            return false;
        }
        if (c1.getPreferredResponseTime() != Criteria.NO_REQUIREMENT &&
            c1.getPreferredResponseTime() < c2.getPreferredResponseTime()) {
            return false;
        }
        if (c1.getPreferredPowerConsumption() != Criteria.NO_REQUIREMENT
            && c1.getPreferredPowerConsumption() <
            c2.getPreferredPowerConsumption()) {
            return false;
        }
        return true;
    }

    // JAVADOC COMMENT ELIDED
    public abstract int getDefaultInterval();

    // JAVADOC COMMENT ELIDED
    public abstract int getDefaultMaxAge();

    // JAVADOC COMMENT ELIDED
    public abstract int getDefaultTimeout();

    // JAVADOC COMMENT ELIDED
    public abstract int getResponseTime();

    // JAVADOC COMMENT ELIDED
    public abstract int getStateInterval();

    // JAVADOC COMMENT ELIDED
    abstract LocationImpl getLastLocation();

    // JAVADOC COMMENT ELIDED
    synchronized public static Location getLastKnownLocation() {
        return PlatformLocationProvider.getLastKnownLocation();
    }

    // JAVADOC COMMENT ELIDED
    public void reset() {
        if (locationQueries > 0) {
            resetRequested = true;
            int attemptCount = Integer
                .parseInt(Configuration
                          .getProperty("com.sun.j2me.location.ResetTimeout"))
                * 10;
            while (locationQueries > 0 && attemptCount-- > 0) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }
            resetRequested = false;
        }
    }

    // JAVADOC COMMENT ELIDED
    public Location getLocation(int timeout)
	throws LocationException, InterruptedException {
        Util.checkForPermission(Permissions.LOCATION, false);
        if (timeout == 0 || timeout < -1) {
            throw new IllegalArgumentException("Illegal timeout value");
        }
        if (timeout == -1) {
            timeout = getDefaultTimeout();
        }
        LocationImpl location = getLocationImpl(timeout);
        return location;
    }

    // JAVADOC COMMENT ELIDED
    protected LocationImpl getLocationImpl(int timeout)
	throws LocationException, InterruptedException {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + timeout * 1000;
        LocationImpl newLocation = null;
        if (getState() == OUT_OF_SERVICE) {
            throw new LocationException("Provider is out of service");
        }
        try {
            locationQueries++;
            while (!resetRequested && System.currentTimeMillis() < endTime) {
                if (getState() == AVAILABLE) {
                    newLocation = updateLocation(endTime - 
                                                    System.currentTimeMillis());
                    if (resetRequested) {
                        break;
                    }
                    if (newLocation != null) {
                        return newLocation;
                    }
                } else {
                    Thread.sleep(getStateInterval() * 1000);
                }
                long delay = Math.min(getResponseTime() * 1000,
                                      endTime - System.currentTimeMillis());
                if (delay <= 0) {
                    break;
                }
                while (!resetRequested && delay > 0) {
                    Thread.sleep(100);
                    delay -= 100;
                }
            }
            if (!resetRequested) {
                if (getState() == TEMPORARILY_UNAVAILABLE) {
                    throw new LocationException("Provider is temporarily unavailable");
                }
                // try one last time
                newLocation = updateLocation(getResponseTime() * 1000);
                if (!resetRequested) {
                    if (newLocation != null) {
                        return newLocation;
                    } 

                    throw new LocationException("Could not acquire location");
                }
            }
        } finally {
            locationQueries--;
        }
        throw new InterruptedException("Location query was interrupted");
    }

    // JAVADOC COMMENT ELIDED
    abstract protected LocationImpl updateLocation(long timeout) 
        throws LocationException; 
    
    // JAVADOC COMMENT ELIDED
    public static LocationProviderImpl getInstanceImpl(Criteria criteria)
	throws LocationException {
        LocationProviderImpl[] found = new LocationProviderImpl[2];
        int state;
        if (criteria == null) {
            criteria = new Criteria();
        }
        boolean allOutOfService = true;
        LocationProviderImpl[] providers = getProviders();
        // loop over all providers and set the ones that match the criteria
        // in their proper state, to give the one available preference over
        // the unavailable one
        for (int i = 0; i < providers.length; i++) {
            LocationProviderImpl provider = (LocationProviderImpl)providers[i];
            state = provider.getState();
            if ((state == AVAILABLE) || (state == TEMPORARILY_UNAVAILABLE)) {
                allOutOfService = false;
                if (provider.matchesCriteria(criteria)) {
                    found[state - 1] = provider;
                }
            }
        }
        if (allOutOfService) {
            throw new LocationException("All providers are out of service");
        }
        // first try to get the available one
        if (found[AVAILABLE - 1] != null) {
            return found[AVAILABLE - 1];
        }
        if (found[TEMPORARILY_UNAVAILABLE - 1] != null) {
            return found[TEMPORARILY_UNAVAILABLE - 1];
        }
        return null;
    }

    // JAVADOC COMMENT ELIDED
    static LocationProviderImpl[] getProviders() {
	if (providers == null) {
	    Vector vectProviders = new Vector();
            String listProviders = PlatformLocationProvider.
                                        getListOfLocationProviders();
            if (listProviders != null &&
                (listProviders = listProviders.trim()) != "") {
                /* parsing the list of providers */
                while (listProviders.length() > 0) {
                    int posSpace = listProviders.indexOf(SEPARATOR);
                    String newProviderName;
                    if (posSpace == -1) { // last provider name
                        newProviderName = listProviders;
                        listProviders = "";
                    } else { // not last name
                        newProviderName = listProviders.substring(0, posSpace);
                        listProviders = listProviders.substring(posSpace + 1);
                    }
                    try {
                        LocationProviderImpl providerInstance = new 
                            PlatformLocationProvider(newProviderName);
                        vectProviders.addElement(providerInstance);
                    } catch (IllegalAccessException e) {
                        if (Logging.TRACE_ENABLED) {
                            Logging.trace(e, "Illegal access to provider");
                        }
                    }
                }
            }

            providers = new LocationProviderImpl[vectProviders.size()];
            vectProviders.copyInto(providers);
        }
        return providers;
    }
    
    // JAVADOC COMMENT ELIDED
    public LocationListener getLocationListener() {
	return locationListener;
    }
    
    // JAVADOC COMMENT ELIDED
    public void setLocationListener(LocationListener listener,
				    int interval, int timeout, int maxAge)
	throws IllegalArgumentException, SecurityException {
        Util.checkForPermission(Permissions.LOCATION, true);
        if (interval < -1 ||
            (interval != -1 && (timeout > interval || maxAge > interval ||
                                timeout < 1 && timeout != -1 || 
                                maxAge < 1 && maxAge != -1))) {
            if (listener != null) {
                throw new IllegalArgumentException("Timeout value is invalid");
            }
        }
        // stop the current locationThread and stateThread
        if (locationThread != null) {
            locationThread.terminate();
            try { // wait for thread to die
                locationThread.join();
            } catch (InterruptedException e) { // do nothing
                if (Logging.TRACE_ENABLED) {
                    Logging.trace(e, "Wrong thread exception.");
                }
            }
            locationThread = null;
        }
        if (stateThread != null) {
            stateThread.terminate();
            try { // wait for thread to die
                stateThread.join();
            } catch (InterruptedException e) { // do nothing
                if (Logging.TRACE_ENABLED) {
                    Logging.trace(e, "Wrong thread exception.");
                }
            }
            stateThread = null;
        }
        if (listener == null) {
            locationListener = null;
            return;
        }
        if (interval == -1) {
            interval = getDefaultInterval();
            maxAge = getDefaultMaxAge();
            timeout = getDefaultTimeout();
        }
        if (maxAge == -1) {
            maxAge = getDefaultMaxAge();
        }
        if (timeout == -1) {
            timeout = getDefaultTimeout();
        }
        this.locationListener = listener;
        // Start the location thread when interval > 0
        if (interval > 0) {
            locationThread = new LocationThread(this, listener, interval,
                                                timeout, maxAge);
            locationThread.start();
        }
        // Start the state update thread
        stateThread = new StateThread(this, listener);
        stateThread.start();
    }
}

/**
 * Class LocationThread provides location updates through location listener.
 */
class LocationThread extends Thread {
    
    /** Location provider listener is registered to. */
    private LocationProviderImpl provider;
    /** Last known location. */
    private Location LastKnownLocation;
    /** Current location listener. */
    private LocationListener listener;
    /** Current interval for location sampling. */
    private int interval;
    /** Current timeout for sampling. */
    private int timeout;
    /** Current limit for old samples. */
    private int maxAge;
    /** Flag indicating if the thread should terminate. */
    private boolean terminated = false;
    
    // JAVADOC COMMENT ELIDED
    LocationThread(LocationProviderImpl provider, LocationListener listener,
		   int interval, int timeout, int maxAge) {
        this.provider = provider;
        this.listener = listener;
        this.interval = interval;
        this.timeout = timeout;
        this.maxAge = maxAge;
    }
    
    /**
     * Terminates the thread.
     */
    void terminate() {
        terminated = true;
        synchronized (this) {
            notify();
        }
    }
    
    // JAVADOC COMMENT ELIDED
    public void run() {
        int responseTime = Math.min(provider.getResponseTime(), interval);
        long lastUpdate = System.currentTimeMillis() - interval * 1000;
        try {
            while (!terminated) {
                Location location = provider.getLastLocation();
                if (location == null || System.currentTimeMillis() +
                    responseTime * 1000 -
                    location.getTimestamp() > maxAge) {
                    // need to update location
                    try {
                        location =
                            provider.getLocation(responseTime + timeout);
                    } catch (LocationException e) {
                        // couldn't get location, send the invalid one
                        location = new LocationImpl(null, 0, 0, 0,
                                                    null, false);
                    } catch (InterruptedException e) {
                        // reset() was called on the provider
                        // should the thread terminate? most probably not
                    }
                }
                long delay = lastUpdate + interval * 1000 -
                    System.currentTimeMillis();
                if (delay > 0) {
                    synchronized (this) {
                        wait(delay); // wait for the right timing
                    }
                }
                if (terminated) { // thread was stopped
                    break;
                }
                // send the new location to location listener
                lastUpdate = System.currentTimeMillis();
                listener.locationUpdated(provider, location);
                delay = (interval - responseTime) * 1000;
                if (delay > 0) {
                    synchronized (this) {
                        wait(delay);
                    }
                }
            }
        } catch (InterruptedException e) {
            if (Logging.TRACE_ENABLED) {
                Logging.trace(e, "Wrong thread exception.");
            }
        }
    }
}

/**
 * Class StateThread checks the current provider state every 
 * <code>interval</code> seconds and sends the state update
 * when state is changed.
 */
class StateThread extends Thread {
    
    /** Current location provider. */
    private LocationProviderImpl provider;
    /** Current location listener. */
    private LocationListener listener;
    /** Flag indicating if the thread should terminate. */
    private boolean terminated = false;
   
    // JAVADOC COMMENT ELIDED
    StateThread(LocationProviderImpl provider, LocationListener listener) {
        this.provider = provider;
        this.listener = listener;
    }
    
    /**
     * Terminates the thread.
     */
    void terminate() {
        terminated = true;
        synchronized (this) {
            notify();
        }
    }
    
    // JAVADOC COMMENT ELIDED
    public void run() {
        // get the current provider state
        int interval = provider.getStateInterval() * 1000;
        int state = provider.getState();
        try {
            while (!terminated) {
                synchronized (this) {
                    // wait before querying the current state
                    wait(interval);
                }
                if (terminated) { // thread was stopped
                    break;
                }
                // check the new provider state
                int newState = provider.getState();
                if (newState != state) { // state was changed
                    state = newState;
                    // send the state update
                    listener.providerStateChanged(provider, state);
                }
            }
        } catch (InterruptedException e) {
            if (Logging.TRACE_ENABLED) {
                Logging.trace(e, "Wrong thread exception.");
            }
        }
    }
}    
