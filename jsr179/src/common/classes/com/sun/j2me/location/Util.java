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

import com.sun.midp.security.Permissions;
import com.sun.midp.midlet.MIDletSuite;
import com.sun.midp.midlet.Scheduler;

/**
 * Set of static utility functions for simple repetitive operations.
 */
public class Util {
    // JAVADOC COMMENT ELIDED
    public static void checkRange(double val, double minimum, double maximum,
				  String exceptionComment) {
        if (val < minimum || val > maximum) {
            throw new IllegalArgumentException(exceptionComment + val);
        }
        if (Double.isNaN(val)) {
            throw new IllegalArgumentException(exceptionComment + "NaN");
        }
    }

    // JAVADOC COMMENT ELIDED
    public static void checkForPermission(int permission,
					  boolean ignoreInterruptedException) {
        MIDletSuite suite = Scheduler.getScheduler().getMIDletSuite();
        try {
            suite.checkForPermission(permission, null);
        } catch (InterruptedException ie) {
	    if (!ignoreInterruptedException) {
                throw new SecurityException(
                    "Interrupted while trying to ask the user permission");
	    }
        }
    }

    // JAVADOC COMMENT ELIDED
    public static void checkForPermission(int permission) {
	checkForPermission(permission, true);
    }

}
