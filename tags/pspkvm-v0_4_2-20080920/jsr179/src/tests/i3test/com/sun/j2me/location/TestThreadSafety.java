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
import java.util.*;
import javax.microedition.location.*;
import com.sun.j2me.location.*;

/**
 * Thread safety tests.
 *
 */
public class TestThreadSafety extends TestCase {
    
    /**
     * Body of the test 1.
     *
     * This test creates several threads querying locations simultaneously.
     * No deadlock situations or exception throws are expected.
     */
    void testMultipleGetLocation() {
        LocationProviderImpl[] providers = 
            LocationProviderImpl.getProviders();
        for (int i = 0; i < providers.length; i++) {
            ((PlatformLocationProvider) providers[i]).
                    i3test_setState(LocationProvider.AVAILABLE);
        }
        
	LocationQueryThread t1, t2, t3;
	t1 = t2 = t3 = null;
	try {
	    // t1 and t2 use the same timeout, t3 uses a different one
	    t1 = new LocationQueryThread(null, 2, 10000);
	    t2 = new LocationQueryThread(null, 2, 10000);
	    t3 = new LocationQueryThread(null, 3, 15000);
	    // start threads
	    t1.start();
	    t2.start();
	    t3.start();
	    // wait
	    Thread.sleep(5000);
	    // no exceptions should be thrown within threads
	    assertNull("Exception occured in thread 1", t1.getException());
	    if (t1.getException() != null) {
		t1.getException().printStackTrace();
	    }
	    assertNull("Exception occured in thread 2", t2.getException());
	    if (t2.getException() != null) {
		t2.getException().printStackTrace();
	    }
	    assertNull("Exception occured in thread 3", t3.getException());
	    if (t3.getException() != null) {
		t3.getException().printStackTrace();
	    }
	    // by this time, each thread should have at least 2 locations
	    assertTrue("Thread 1 did not receive any location",
		       t1.getLocationCount() > 0);
	    assertTrue("Thread 2 did not receive any location",
		       t2.getLocationCount() > 0);
	    assertTrue("Thread 3 did not receive any location",
		       t3.getLocationCount() > 0);
	} catch (InterruptedException e) {
	    fail("Interrupted Exception caught");
	    e.printStackTrace();
	} finally {
	    t1.interrupt();
	    t2.interrupt();
	    t3.interrupt();
	}
    }

    /**
     * Body of the test 2.
     *
     * This test resets the provider while several threads querying locations.
     * This should result in InterruptedException being thrown in all threads.
     */
    void testResetInterruptedException() {
        LocationProviderImpl[] providers = 
            LocationProviderImpl.getProviders();
        for (int i = 0; i < providers.length; i++) {
            ((PlatformLocationProvider) providers[i]).
                    i3test_setState(LocationProvider.AVAILABLE);
        }
	LocationQueryThread t1, t2;
	t1 = t2 = null;
	try {
	    LocationProvider lp = LocationProvider.getInstance(null);
	    // two threads seem enough
	    t1 = new LocationQueryThread(lp, 2, 10000);
	    t2 = new LocationQueryThread(lp, 3, 15000);
	    // start threads
	    t1.start();
	    t2.start();
	    // wait 1/2 of standard response time
	    Thread.sleep(
		((PlatformLocationProvider)lp).getResponseTime() * 1000 / 2);
	    lp.reset();
	    // wait for exceptions to be caught in threads
	    Thread.sleep(100);
	    assertTrue("Exception did not occur in thread 1",
			 t1.getException() instanceof InterruptedException);
	    assertTrue("Exception did not occur in thread 2",
			 t2.getException() instanceof InterruptedException);
	} catch (Exception e) {
	    assertFalse("Unexpected exception caught: " + e, true);
	} finally {
	    t1.interrupt();
	    t2.interrupt();
	}
    }

    /**
     * Body of the test 3.
     *
     * This test starts two thread which access the same landmark store
     * and perform various actions on that store. No deadlocks should occur.
     */
    void testMultipleStoreAccess() {
	StoreAccessThread t1, t2;
	t1 = t2 = null;
	try {
	    t1 = new StoreAccessThread(5000);
	    t2 = new StoreAccessThread(5000);
	    t1.start();
	    t2.start();
	    Thread.sleep(5500);
	    assertNull("Exception occured in thread 1", t1.getException());
	    if (t1.getException() != null) {
		t1.getException().printStackTrace();
	    }
	    assertNull("Exception occured in thread 2", t2.getException());
	    if (t2.getException() != null) {
		t2.getException().printStackTrace();
	    }
	} catch (Exception e) {
	    fail("Unexpected exception caught");
	    e.printStackTrace();
	} finally {
	    t1.interrupt();
	    t2.interrupt();
	}
    }
    
    /**
     * Entry point.
     */
    public void runTests() {
	declare("Multiple threads querying locations.");
        testMultipleGetLocation();
	declare("Provider reset while querying locations.");
	testResetInterruptedException();
	declare("Accessing landmark store from multiple threads.");
	testMultipleStoreAccess();
    }
    
}

/**
 * The GenericThread class provides facilities for storing illegal exception
 * instance if it is occured during the execution. Descendants of this class
 * should override the execute() method, and not run().
 */
abstract class GenericThread extends Thread {

    /** Exception occured during thread run. */
    private Exception exception;
    
    /** Indicates whether the thread should be terminated */
    protected boolean terminated;

    /** Time in milliseconds the thread instance should be executing. */
    private int duration;

    /** Indicates whether the theread's execute() method has returned. */
    private boolean finished;

    /** The system time after which the thread should stop executing. */
    protected long endTime;

    /**
     * Default constructor.
     */
    GenericThread() {
	duration = 0;
    }

    /**
     * Constructor with the duration argument.
     *
     * @param duration time in milliseconds the thread should execute.
     */
    GenericThread(int duration) {
	this.duration = duration;
    }

    /**
     * Returns the saved exception object.
     *
     * @return recorded exception instance.
     */
    public Exception getException() {
	return exception;
    }

    /**
     * Requests the thread to terminate.
     */
    public void terminate() {
	terminated = true;
	synchronized (this) {
	    notify();
	}
    }

    /**
     * Checks whether the thread's execute() method has returned.
     *
     * @return true if the thread has finished execution, false otherwise.
     */
    public boolean isFinished() {
	return finished;
    }

    /**
     * Thread functionality should be implemented in this method. If an
     * exception occurs during the execution, it will be stored by the
     * caller run() method.
     */
    abstract protected void execute() throws Exception;

    /**
     * Runs the thread.
     */
    public void run() {
	exception = null;
	terminated = false;
	finished = false;
	endTime = System.currentTimeMillis() + duration;
	try {
	    execute();
	} catch (Exception e) {
	    exception = e;
	}
	finished = true;
    }

}

/**
 * This thread queries for locations for the specified amount of time.
 */
class LocationQueryThread extends GenericThread {

    /** Location provider to be used. null if to be determined on run. */
    private LocationProvider provider;

    /** Timeout value to be passed to the getLocation method. */
    private int timeout;

    /** Number of locations acquired. */
    private int locationCount = 0;

    /**
     * Constructor.
     *
     * @param provider Location provider to be used. null if to be
     *                 determined automatically when the thread starts
     * @param timeout  timeout value to be passed to the getLocation method
     * @param duration time in seconds the thread instance should be running.
     *                 This is the minimum value; the actual time may be longer
     */
    public LocationQueryThread(LocationProvider provider,
			       int timeout, int duration) {
	super(duration);
	this.provider = provider;
	this.timeout = timeout;
    }

    /**
     * Returns the number of locations acquired during run.
     *
     * @return number of locations retrieved.
     */
    public int getLocationCount() {
	return locationCount;
    }

    /**
     * Provides core functionality.
     */
    protected void execute() throws Exception {
	if (provider == null) {
	    provider = LocationProvider.getInstance(null);
	}
	while (!terminated && System.currentTimeMillis() < endTime) {
	    Location loc = provider.getLocation(timeout);
	    ++locationCount;
	}
    }

}

/**
 * This thread performs random operations on a landmark store.
 */
class StoreAccessThread extends GenericThread {

    /** Store name to be accessed. */
    private final String storeName = "TestStore";
    
    /** Category name to be used. */
    private final String categoryName = "TestCategory";

    /**
     * Class constructor.
     *
     * @param duration time in milliseconds the thread should execute.
     */
    StoreAccessThread(int duration) {
	super(duration);
    }
    
    /**
     * Provides core functionality.
     */
    protected void execute() throws Exception {
	try {
	    LandmarkStore.createLandmarkStore(storeName);
	} catch (IllegalArgumentException e) {
	    // OK, store already exists
	}
	LandmarkStore store = LandmarkStore.getInstance(storeName);
	Landmark l1, l2;
	l1 = l2 = null;
	Enumeration enum;
	Random rand = new Random();
	while (System.currentTimeMillis() < endTime) {
	    // the following code does seem a little bit ..., but...
	    switch (rand.nextInt(10)) {
	    case 0:
		try {
		    store.addCategory(categoryName);
		} catch (IllegalArgumentException e) {
		    // OK, category already exists
		}
		break;
	    case 1:
		l1 = new Landmark("Landmark1", null, null, null);
		try {
		    store.addLandmark(l1, categoryName);
		} catch (IllegalArgumentException e) {
		    // OK, category was not found
		}
		break;
	    case 2:
		l2 = new Landmark("Landmark2", null, null, null);
		store.addLandmark(l2, null);
		break;
	    case 3:
		store.deleteCategory(categoryName);
		break;
	    case 4:
		enum = store.getCategories();
		break;
	    case 5:
		enum = store.getLandmarks();
		break;
	    case 6:
		enum = store.getLandmarks(categoryName, "Landmark");
		break;
	    case 7:
		String[] list = store.listLandmarkStores();
		break;
	    case 8:
		if (l1 != null) {
		    store.removeLandmarkFromCategory(l1, categoryName);
		}
		break;
	    case 9:
		if (l1 != null) {
		    try {
			store.updateLandmark(l1);
		    } catch (LandmarkException e) {
			// OK, landmark was not found
		    }
		}
		if (l2 != null) {
		    try {
			store.updateLandmark(l2);
		    } catch (LandmarkException e) {
			// OK, landmark was not found
		    }
		}
		break;
	    }
	    synchronized (this) {
		wait(rand.nextInt(10));
	    }
	}
    }

}
