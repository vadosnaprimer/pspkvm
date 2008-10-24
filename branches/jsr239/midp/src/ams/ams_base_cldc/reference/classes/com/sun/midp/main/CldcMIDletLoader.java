/*
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

package com.sun.midp.main;

import javax.microedition.midlet.*;

import com.sun.midp.midlet.*;

import com.sun.midp.security.SecurityToken;

/**
 * The class implements the MIDlet loader for the CLDC VM.
 */
class CldcMIDletLoader implements MIDletLoader {
    /** This class has a different security domain than the application. */
    private SecurityToken classSecurityToken;

    /**
     * Initializes this object.
     *
     * @param token security token for this class.
     */
    CldcMIDletLoader(SecurityToken token) {
        classSecurityToken = token;
    }

    /**
     * Loads a MIDlet from a suite's JAR.
     *
     * @param suite reference to the suite
     * @param className class name of the MIDlet to be created
     *
     * @return new instance of a MIDlet
     *
     * @exception ClassNotFoundException if the MIDlet class is
     * not found
     * @exception InstantiationException if the MIDlet cannot be
     * created or is not subclass of MIDlet
     * @exception IllegalAccessException if the MIDlet is not
     * permitted to perform a specific operation
     */
    public MIDlet newInstance(MIDletSuite suite, String className) throws
           ClassNotFoundException, InstantiationException,
           IllegalAccessException {
        Class midletClass;

        midletClass = Class.forName(className);
        if (!MIDlet.class.isAssignableFrom(midletClass)) {
            throw new InstantiationException("Class not a MIDlet");
        }

        return (MIDlet)midletClass.newInstance();
    }
}
