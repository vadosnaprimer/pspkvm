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

package com.sun.perseus.platform;

/**
 * This class is used to provide support for formating messages.
 *
 * @version $Id: MessagesSupport.java,v 1.4 2006/04/21 06:33:46 st125089 Exp $
 */
public class MessagesSupport {
    /**
     * @param s the name of resource where messages are stored.
     */
    public MessagesSupport(final String s) {
    }

    /**
     * @param key the message key in the bundle resource
     * @param args the arguments used to format the message.
     * @return the message formatted with the input arguments
     */
    public String formatMessage(final String key, 
                                final Object[] args) {
        String msg = key;
        if (args != null) {
            msg += "(";
            for (int i = 0; i < args.length; i++) {
                msg += "[" + i + "] " + args[i];
                if (i < args.length - 1) {
                    msg += ", ";
                }
            }
            msg += ")";
        }
        return msg;
    }
}
