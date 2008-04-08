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

package com.sun.midp.installer;

import java.io.*;

import javax.microedition.rms.*;

import com.sun.midp.i18n.Resource;

import com.sun.midp.i18n.ResourceConstants;

import com.sun.midp.main.MIDletSuiteUtils;

import com.sun.midp.midlet.MIDletStateHandler;

import com.sun.midp.midletsuite.MIDletInfo;
import com.sun.midp.midletsuite.MIDletSuiteStorage;
import com.sun.midp.midlet.MIDletSuite;
import com.sun.midp.configurator.Constants;

import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;

/**
 * Installs/Updates a test suite, runs the first MIDlet in the suite in a loop
 * specified number of iterations or until the new version of the suite is not
 * found, then removes the suite.
 * <p>
 * The MIDlet uses these application properties as arguments: </p>
 * <ol>
 *   <li>arg-0: URL for the test suite
 *   <li>arg-1: Used to override the default domain used when installing
 *    an unsigned suite. The default is maximum to allow the runtime API tests
 *    be performed automatically without tester interaction. The domain name
 *    may be followed by a colon and a list of permissions that must be allowed
 *    even if they are not listed in the MIDlet-Permissions attribute in the
 *    application descriptor file. Instead of the list a keyword "all" can be
 *    specified indicating that all permissions must be allowed, for example:
 *    operator:all.
 *    <li>arg-2: Integer number, specifying how many iterations to run
 *    the suite. If argument is not given or less then zero, then suite
 *    will be run until the new version of the suite is not found.
 * </ol>
 * <p>
 * If arg-0 is not given then a form will be used to query the tester for
 * the arguments.</p>
 */
public class AutoTester extends AutoTesterBase implements AutoTesterInterface {

    /** Settings database name. */
    private static final String AUTOTEST_STORE = "autotest";
    /** Record ID of URL. */
    private static final int URL_RECORD_ID = 1;
    /** Record ID of the security domain for unsigned suites. */
    private static final int DOMAIN_RECORD_ID = 2;
    /** Record ID of suite ID. */
    private static final int SUITE_ID_RECORD_ID = 3;
    /** Record ID of loopCount */
    private static final int LOOP_COUNT_RECORD_ID = 4;
    /** ID of installed test suite. */
    int suiteId;

    /**
     * Create and initialize a new auto tester MIDlet.
     */
    public AutoTester() {
        super();

        if (url != null) {
            startBackgroundTester();
        } else if (restoreSession()) {
            // continuation of a previous session
            startBackgroundTester();
        } else {
            /**
             * No URL has been provided, ask the user.
             *
             * commandAction will subsequently call startBackgroundTester.
             */
            getUrl();
        }
    }

    /** Run the installer. */
    public void run() {
        installAndPerformTests(midletSuiteStorage, installer, url);
    }

    /**
     * Restore the data from the last session.
     *
     * @return true if there was data saved from the last session
     */
    public boolean restoreSession() {
        RecordStore settings = null;
        ByteArrayInputStream bas;
        DataInputStream dis;
        byte[] data;

        try {
            settings = RecordStore.openRecordStore(AUTOTEST_STORE, false);

            data = settings.getRecord(URL_RECORD_ID);
            if (data == null) {
                return false;
            }

            bas = new ByteArrayInputStream(data);
            dis = new DataInputStream(bas);
            url = dis.readUTF();

            data = settings.getRecord(DOMAIN_RECORD_ID);
            if (data != null && data.length > 0) {
                bas = new ByteArrayInputStream(data);
                dis = new DataInputStream(bas);
                domain = dis.readUTF();
            }

            data = settings.getRecord(SUITE_ID_RECORD_ID);
            if (data != null && data.length > 0) {
                bas = new ByteArrayInputStream(data);
                dis = new DataInputStream(bas);
                suiteId = dis.readInt();
            }

            data = settings.getRecord(LOOP_COUNT_RECORD_ID);
            if (data != null && data.length > 0) {
                bas = new ByteArrayInputStream(data);
                dis = new DataInputStream(bas);
                loopCount = dis.readInt();
            }

            return true;
        } catch (RecordStoreNotFoundException rsnfe) {
            // This normal when no initial args are given, ignore
        } catch (Exception ex) {
            displayException(Resource.getString
                             (ResourceConstants.EXCEPTION), ex.toString());
        } finally {
            if (settings != null) {
                try {
                    settings.closeRecordStore();
                } catch (Exception ex) {
                    if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                        Logging.report(Logging.WARNING, LogChannels.LC_AMS,
                        "closeRecordStore threw an Exception");
                    }
                }
            }
        }

        return false;
    }

    /**
     * Save session data for next time.
     *
     * @exception if an exception occurs
     */
    private void saveSession() throws Exception {
        RecordStore settings = null;
        boolean newStore = false;
        ByteArrayOutputStream bas;
        DataOutputStream dos;
        byte[] data;

        if (url == null) {
            return;
        }

        try {
            settings = RecordStore.openRecordStore(AUTOTEST_STORE, true);

            if (settings.getNextRecordID() == URL_RECORD_ID) {
                newStore = true;
            }

            bas = new ByteArrayOutputStream();
            dos = new DataOutputStream(bas);
            dos.writeUTF(url);
            data = bas.toByteArray();

            if (newStore) {
                settings.addRecord(data, 0, data.length);
            } else {
                settings.setRecord(URL_RECORD_ID, data, 0, data.length);
            }

            bas.reset();
            dos.writeUTF(domain);
            data = bas.toByteArray();

            if (newStore) {
                settings.addRecord(data, 0, data.length);
            } else {
                settings.setRecord(DOMAIN_RECORD_ID, data, 0, data.length);
            }

            bas.reset();
            dos.writeInt(suiteId);
            data = bas.toByteArray();

            if (newStore) {
                settings.addRecord(data, 0, data.length);
            } else {
                settings.setRecord(SUITE_ID_RECORD_ID, data, 0, data.length);
            }

            bas.reset();
            dos.writeInt(loopCount);
            data = bas.toByteArray();

            if (newStore) {
                settings.addRecord(data, 0, data.length);
            } else {
                settings.setRecord(LOOP_COUNT_RECORD_ID, data, 0, data.length);
            }
        } finally {
            if (settings != null) {
                try {
                    settings.closeRecordStore();
                } catch (Exception ex) {
                    if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                        Logging.report(Logging.WARNING, LogChannels.LC_AMS,
                        "closeRecordStore threw an exception");
                    }
                }
            }
        }
    }

    /** End the testing session. */
    private void endSession() {
        try {
            RecordStore.deleteRecordStore(AUTOTEST_STORE);
        } catch (Throwable ex) {
            // ignore
        }

        notifyDestroyed();
    }

    /**
     * Installs and performs the tests.
     *
     * @param midletSuiteStorage MIDletSuiteStorage object
     * @param inp_installer Installer object
     * @param inp_url URL of the test suite
     */
    public void installAndPerformTests(
        MIDletSuiteStorage midletSuiteStorage,
        Installer inp_installer, String inp_url) {

        MIDletInfo midletInfo;
        String message = null;

        if (loopCount != 0) {
            try {
                // force an overwrite and remove the RMS data
                suiteId = inp_installer.installJad(inp_url,
                    Constants.INTERNAL_STORAGE_ID, true, true, installListener);

                midletInfo = getFirstMIDletOfSuite(suiteId,
                        midletSuiteStorage);
                MIDletSuiteUtils.execute(suiteId,
                        midletInfo.classname, midletInfo.name);

                // We want auto tester MIDlet to run after the test is run.
                MIDletSuiteUtils.setLastSuiteToRun(
                    MIDletStateHandler.getMidletStateHandler().
                    getMIDletSuite().getID(),
                    getClass().getName(), null, null);

                if (loopCount > 0) {
                    loopCount -= 1;
                }
                saveSession();
                notifyDestroyed();
                return;
            } catch (Throwable t) {
                handleInstallerException(suiteId, t);
            }
        }

        if (midletSuiteStorage != null &&
                suiteId != MIDletSuite.UNUSED_SUITE_ID) {
            try {
                midletSuiteStorage.remove(suiteId);
            } catch (Throwable ex) {
                if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                    Logging.report(Logging.WARNING, LogChannels.LC_AMS,
                                   "Throwable in remove");
                }
            }
        }

        endSession();
    }
}
