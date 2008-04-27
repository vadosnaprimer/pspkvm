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

package com.sun.midp.automation;

import java.util.*;
import java.io.*;
import com.sun.midp.midletsuite.*;
import com.sun.midp.installer.*;

/** 
 * Represents suites storage.
 */
public abstract class AutoSuiteStorage {
    /**
     * Installs suite from specified location.
     *
     * @param location URI pointing to suite jad/jar
     * @return AutoSuiteDescriptor representing installed suit
     */
    public abstract AutoSuiteDescriptor installSuite(String location)
        throws IOException, MIDletSuiteLockedException, 
               MIDletSuiteCorruptedException, InvalidJadException,
               SecurityException;

    /**
     * Uninstalls specified suite.
     *
     * @param suite suite to uninstall
     */
    public abstract void uninstallSuite(AutoSuiteDescriptor suite) 
        throws MIDletSuiteLockedException;

    /**
     * Gets a vector of installed suites.
     *
     * @return vector with suites descriptors
     */
    public abstract Vector getInstalledSuites();


    /**
     * Gets instance of AutoSuiteStorage class.
     *
     * @return instance of AutoSuiteStorage class
     * @throws IllegalStateException if Automation API hasn't been
     * initialized or is not permitted to use
     */
    public final static AutoSuiteStorage getStorage() 
        throws IllegalStateException {

        AutomationInitializer.guaranteeAutomationInitialized();
        return AutoSuiteStorageImpl.getInstance();
    }
}
