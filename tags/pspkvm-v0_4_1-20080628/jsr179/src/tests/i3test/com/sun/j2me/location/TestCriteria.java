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
import javax.microedition.location.Criteria;

/**
 * Tests for selecting location provider.
 *
 */
public class TestCriteria extends TestCase {
    
    /** Test value of the horisontal accuracy */
    private final int testHorAcc = 500;
    /** Test value of the vertical accuracy */
    private final int testVertAcc = 300;
    /** Test value of the preferred response time */
    private final int testPrefTime = 6000;
    /** Test value of the power consumption */
    private final int testPowCons = Criteria.POWER_USAGE_HIGH;
    /**
     * Body of the test 1
     *
     */
    void Test1() {
            Criteria testCriteria = new Criteria();
	    if (testCriteria == null) {
                fail("Criteria create error");
	    }
	    // Test default values
	    if (!(testCriteria.getHorizontalAccuracy() == 
	        Criteria.NO_REQUIREMENT)) {
                fail("Criteria: default value of horisontal accuracy" +
		    " is wrong");
	    }
	    if (!(testCriteria.getVerticalAccuracy() == 
	        Criteria.NO_REQUIREMENT)) {
                fail("Criteria: default value of vertical accuracy" +
		    " is wrong");
	    }
	    if (!(testCriteria.getPreferredResponseTime() == 
	        Criteria.NO_REQUIREMENT)) {
                fail("Criteria: default value of preferred response time" +
		    " is wrong");
	    }
	    if (!(testCriteria.getPreferredPowerConsumption() == 
	        Criteria.NO_REQUIREMENT)) {
                fail("Criteria: default value of power consumption" +
		    " is wrong");
	    }
	    if (!(testCriteria.isAllowedToCost() == true)) {
                fail("Criteria: on create should be allowed to cost");
	    }
	    if (!(testCriteria.isSpeedAndCourseRequired() == false)) {
                fail("Criteria: on create the speed and the course" +
		    " shouldn't be required");
	    }
	    if (!(testCriteria.isAltitudeRequired() == false)) {
                fail("Criteria: on create the altitude" +
		    " shouldn't be required");
	    }
	    if (!(testCriteria.isAddressInfoRequired() == false)) {
                fail("Criteria: on create the textual address info" +
		    " shouldn't be required");
            }
	    assertTrue("OK", true);
    }
    
    /**
     * Body of the test 2
     *
     */
    void Test2() {
            Criteria testCriteria = new Criteria();
	    assertTrue("OK", true);
            testCriteria.setHorizontalAccuracy(testHorAcc);
	    if (!(testCriteria.getHorizontalAccuracy() == testHorAcc)) {
                fail("Criteria: setting the new value of horisontal" +
		    " accuracy is wrong");
	    }
            testCriteria.setVerticalAccuracy(testVertAcc);
	    if (!(testCriteria.getVerticalAccuracy() == testVertAcc)) {
                fail("Criteria: setting the new value of vertical" +
		    " accuracy is wrong");
	    }
            testCriteria.setPreferredResponseTime(testPrefTime);
	    if (!(testCriteria.getPreferredResponseTime() == testPrefTime)) {
                fail("Criteria: setting the new value of preferred" +
		    " time is wrong");
	    }
            testCriteria.setPreferredPowerConsumption(testPowCons);
	    if (!(testCriteria.getPreferredPowerConsumption() 
	        == testPowCons)) {
                fail("Criteria: setting the new value of power" +
		    " consumption is wrong");
	    }
            testCriteria.setCostAllowed(false);
	    if (testCriteria.isAllowedToCost()) {
                fail("Criteria: switching off cost allowed" +
		    " setting is wrong");
	    }
            testCriteria.setSpeedAndCourseRequired(true);
	    if (!testCriteria.isSpeedAndCourseRequired()) {
                fail("Criteria: switching on requirement of" +
		    " course and speed is wrong");
	    }
            testCriteria.setAltitudeRequired(true);
	    if (!testCriteria.isAltitudeRequired()) {
                fail("Criteria: switching on requirement of" +
		    " altitude is wrong");
	    }
            testCriteria.setAddressInfoRequired(true);
	    if (!testCriteria.isAddressInfoRequired()) {
                fail("Criteria: switching on requirement of" +
		    " textual address information is wrong");
	    }
    }
    
    
    /**
     * Tests execute
     *
     */
    public void runTests() {
        declare("TestCriteriaDefault");
	Test1();
        declare("TestCriteriaParameters");
	Test2();
    }
    
}
