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

package com.sun.midp.content;

import javax.microedition.midlet.MIDlet;

import com.sun.midp.installer.InvalidJadException;
import com.sun.midp.installer.Installer;
import com.sun.midp.installer.InstallState;

import com.sun.midp.midlet.MIDletSuite;

import com.sun.midp.security.Permissions;
import com.sun.midp.security.SecurityToken;

/**
 * Stub interface to handle ContentHandlers functions.
 * This version has no dependentcie to the CLDC AMS classes. It is needed by
 * com.sun.midp.installer.Installer and com.sun.midp.midletsuite.MIDletStorage.
 * When these classes not longer reference this class, it can be removed.
 */
public class CHManager {
    /**
     * Creates a new instance of CHInstaller.
     */
    private CHManager() {
    }

    /**
     * Get the Content Handler manager if the system is configured
     * to implement content handlers.  A dummy handler is returned
     * if content handlers are not supported.
     * @param token the SecurityToken to get the CHManager
     * @return the CHInstaller instance to be used to handle
     *  management and installation of content handlers.
     * @see com.sun.midp.content.CHInstaller
     * @exception SecurityException if the token or suite is not allowed
     */
    public static CHManager getManager(SecurityToken token) {
        return new CHManager();
    }

    /**
     * Install the content handlers found and verified by preInstall.
     * Register any content handlers parsed from the JAD/Manifest
     * attributes.
     * @exception InvalidJadException thrown if the application
     *   descriptor is invalid
     */
    public void install() throws InvalidJadException {
    }

    /**
     * Parse the ContentHandler attributes and check for errors.
     * <ul>
     * <li> Parse attributes into set of ContentHandlers.
     * <li> If none, return
     * <li> Check for permission to install handlers
     * <li> Check each for simple invalid arguments
     * <li> Check each for MIDlet is registered
     * <li> Check each for conflicts with other application registrations
     * <li> Find any current registrations
     * <li> Merge current dynamic current registrations into set of new
     * <li> Check and resolve any conflicts between static and curr dynamic
     * <li> Retain current set and new set for registration step.
     * </ul>
     * @param installer the installer with access to the JAR, etc.
     * @param state the InstallState with the attributes and other context
     * @param msuite access to information about the suite
     * @param authority the authority, if any, that authorized the trust level
     * @exception InvalidJadException if there is no classname field,
     * the option field is not "true", "false" or blank or if there are
     * more than five comma separated fields on the line.
     */
    public void preInstall(Installer installer,
			   InstallState state,
			   MIDletSuite msuite,
			   String authority)
	throws InvalidJadException {
    }

    /**
     * Uninstall the Content handler specific information for
     * the specified suiteId.
     * @param suiteId the suite ID
     */
    public void uninstall(int suiteId) {
    }

    /**
     * Check for a URL to install from the Invocation mechanism,
     * if one has been queued.
     * @param midlet the MIDlet that is the content handler.
     * @return the URL to install; <code>null</code> if none is available
     * @see com.sun.midp.content.CHInstallerImpl
     */
    public String getInstallURL(MIDlet midlet) {
	return null;
    }

    /**
     * Notify the invocation mechanism that the install
     * of the URL provided by {@link #getURLToInstall}
     * succeeded or failed.
     * @param success <code>true</code> if the install was a success;
     *  <code>false</code> otherwise
     * @see com.sun.midp.content.CHInstallerImpl
     */
    public void installDone(boolean success) {
    }

    /**
     * Notification that a MIDlet is about to be created.
     * Stub implementation used when CHAPI is not present.
     *
     * @param suiteId the storage name of the MIDlet suite
     * @param classname the midlet classname
     * @see com.sun.midp.midlet.MIDletState
     */
    public void midletInit(int suiteId, String classname) {
    }
}

