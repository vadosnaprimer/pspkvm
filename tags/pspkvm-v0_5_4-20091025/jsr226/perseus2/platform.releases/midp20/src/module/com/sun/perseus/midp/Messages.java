/*
 *
 *
 * Portions Copyright  2000-2007 Sun Microsystems, Inc. All Rights
 * Reserved.  Use is subject to license terms.
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

/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included with this distribution in  *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package com.sun.perseus.midp;

import com.sun.perseus.platform.MessagesSupport;

/**
 *
 * @version $Id: Messages.java,v 1.2 2006/04/21 06:40:52 st125089 Exp $
 */
final class Messages {
    /*
     * Error message codes.
     *
     */

    /**
     * Error when a method is called while the target object is in the
     * wrong state.
     *
     * {0} : the target object class name.
     * {1} : the target object's current state.
     * {2} : the method name
     * {3} : the valid state(s) for the method.
     */
    static String ERROR_INVALID_STATE
        = "error.invalid.state";

    /**
     * This class does not need to be instantiated.
     */
    private Messages() {}

    /**
     * The error messages bundle class name.
     */
    protected static final String RESOURCES =
        "com.sun.perseus.awt.resources.Messages";

    /**
     * The localizable support for the error messages.
     */
    protected static MessagesSupport messagesSupport =
        new MessagesSupport(RESOURCES);

    /**
     * Formats the message identified by <tt>key</tt> with the input
     * arguments.
     * 
     * @param key the message's key
     * @param args the arguments used to format the message
     * @return the formatted message
     */
    public static String formatMessage(final String key, final Object[] args) {
        return messagesSupport.formatMessage(key, args);
    }
}
