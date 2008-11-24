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

package com.sun.perseus.j2d;

import com.sun.perseus.platform.MessagesSupport;

/**
 *
 * @version $Id: Messages.java,v 1.2 2006/04/21 06:35:04 st125089 Exp $
 */
class Messages {
    /**
     * Used when invoking the given method on the given interface with
     * an out of bound parameter value.
     *
     * {0} : interface name.
     * {1} : method name.
     * {2} : parameter name.
     * {3} : value.
     */
    public static final String ERROR_OUT_OF_BOUND_PARAMETER_VALUE
        = "error.out.of.bound.parameter.value";

    /**
     * The error messages bundle class name.
     */
    protected static final String RESOURCES =
        "com.sun.perseus.j2d.resources.Messages";

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
    static String formatMessage(final String key, final Object[] args) {
        return messagesSupport.formatMessage(key, args);
    }
}
