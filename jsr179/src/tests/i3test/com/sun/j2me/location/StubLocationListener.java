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

import javax.microedition.location.*;

/**
 *
 *  This class contains the simplified code for testing the JSR179 
 *  implementation.
 */
public class StubLocationListener implements LocationListener {

    /** Received location from listener */
    private Location currLocation = null;

    /** Received location provider state from listener */
    private int currState;
    
    /**
     * Return the current state.
     * @return current state
     */
    public int getCurrState() {
        return currState;
    }
    
    /**
     * Sets the current state.
     * @param newState new state value
     */
    public void setCurrState(int newState) {
        currState = newState;
    }
    
    /**
     * Return the current location.
     * @return current location
     */
    public Location getCurrLocation() {
        return currLocation;
    }
    
    /**
     * Sets the current location.
     * @param newLocation new location for setting
     */
    public void setCurrLocation(Location newLocation) {
        currLocation = newLocation;
    }
         
    /**
     * locationUpdated implementation.
     * @param provider the source of the event
     * @param location the location to which the 
     * event relates, i.e. the new position
     */
    public void locationUpdated(LocationProvider provider,
                            Location location) {
        currLocation = location; // save the location
    }

    /**
     * providerStateChanged implementation.
     * @param provider the source of the event
     * @param newState the new state of the LocationProvider.
     */
    public void providerStateChanged(LocationProvider provider,
                            int newState) {
        currState = newState; // save the location
    }
}
