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

import java.io.IOException;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import com.sun.midp.configurator.Constants;

import com.sun.midp.io.Base64;

import com.sun.midp.midlet.MIDletSuite;

import com.sun.midp.midlet.MIDletStateHandler;

import com.sun.midp.midletsuite.MIDletSuiteStorage;

import com.sun.midp.security.Permissions;
import com.sun.midp.security.SecurityToken;

/**
 * This class handles sending installation and deletion notifications as
 * specified by the OTA section of the MIDP 2.0 specification.
 * The delete notifications are only sent when an install notification is
 * sent. The installer will call this class to send the initial install
 * notification, if the notification fails and is a success notification,
 * the notification will be queued, so that the next time a MIDlet from the
 * suite is run, that suite's notification will be retried. The MIDlet
 * state handler will call this class to process install notification retries.
 */
public final class OtaNotifier {
    /** Retry delay. */
    static final int RETRY_DELAY = 3000; // 3 seconds

    /** MIDlet property for the install notify URL. */
    public static final String NOTIFY_PROP = "MIDlet-Install-Notify";

    /** Success message for the suite provider. */
    public static final String SUCCESS_MSG = "900 Success";

    /** Error message for the suite provider. */
    public static final String INSUFFICIENT_MEM_MSG =
        "901 Insufficient Memory";

    /** Error message for the suite provider. */
    public static final String USER_CANCELLED_MSG = "902 User Cancelled";

    /** Error message for the suite provider. */
    public static final String JAR_SIZE_MISMATCH_MSG = "904 JAR size mismatch";

    /** Error message for the suite provider. */
    public static final String ATTRIBUTE_MISMATCH_MSG =
        "905 Attribute Mismatch";

    /** Error message for the suite provider. */
    public static final String INVALID_JAD_MSG = "906 Invalid Descriptor";

    /** Error message for the suite provider. */
    public static final String INVALID_JAR_MSG = "907 Invalid JAR";

    /** Error message for the suite provider. */
    public static final String INCOMPATIBLE_MSG =
        "908 Incompatible Configuration or Profile";

    /** Error message for authentication failure. */
    public static final String AUTHENTICATION_FAILURE_MSG =
        "909 Application authentication failure";

    /** Error message for authorization failure. */
    public static final String AUTHORIZATION_FAILURE_MSG =
        "910 Application authorization failure";

    /** Error message for push registration failure. */
    public static final String PUSH_REG_FAILURE_MSG =
        "911 Push registration failure";

    /** Error message for push registration failure. */
    public static final String DELETE_NOTIFICATION_MSG =
        "912 Deletion Notification";

    /** Message to send when a content handler install fails. */
    public static final String CONTENT_HANDLER_CONFLICT =
        "938 Content handler conflicts with other handlers";

    /** Message to send when a content handler install fails. */
    public static final String INVALID_CONTENT_HANDLER =
        "939 Content handler install failed";

    /**
     * Posts a status message back to the provider's URL in JAD.
     * This method will also retry ALL pending delete notifications.
     *
     * @param message status message to post
     * @param suite MIDlet suite object
     * @param proxyUsername if not null, it will be put in the post
     * @param proxyPassword if not null, it will be put in the post
     */
    public static void postInstallMsgBackToProvider(String message,
            MIDletSuite suite, String proxyUsername, String proxyPassword) {
    }

    /**
     * Retry the pending install status message for this suite only.
     * This method will also retry ALL pending delete notifications,
     * if the install notification was retried.
     *
     * @param token security token of the caller
     * @param suite MIDlet suite object
     */
    public static void retryInstallNotification(SecurityToken token,
            MIDletSuite suite) {
    }

    /**
     * Posts all queued delete notification messages
     *
     * @param proxyUsername if not null, it will be put in the post
     * @param proxyPassword if not null, it will be put in the post
     */
    public static void postQueuedDeleteMsgsBackToProvider(
            String proxyUsername, String proxyPassword) {
    }
}