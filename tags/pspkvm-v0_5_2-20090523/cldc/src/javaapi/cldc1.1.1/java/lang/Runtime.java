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

package java.lang;

import java.security.*;
/**
 * Every Java application has a single instance of class 
 * <code>Runtime</code> that allows the application to interface with 
 * the environment in which the application is running. The current 
 * runtime can be obtained from the <code>getRuntime</code> method. 
 * <p>
 * An application cannot create its own instance of this class. 
 *
 * @version 12/17/01 (CLDC 1.1)
 * @see     java.lang.Runtime#getRuntime()
 * @since   JDK1.0, CLDC 1.0
 */

public class Runtime {
    private static Runtime currentRuntime = new Runtime();

    /**
     * Returns the runtime object associated with the current Java application.
     * Most of the methods of class <code>Runtime</code> are instance 
     * methods and must be invoked with respect to the current runtime object. 
     * 
     * @return  the <code>Runtime</code> object associated with the current
     *          Java application.
     */
    public static Runtime getRuntime() { 
        return currentRuntime;
    }

    /** Don't let anyone else instantiate this class */
    private Runtime() {}

    /* Helper for exit
     */
    private native void exitInternal(int status);

    /**
     * Terminates the currently running Java application. This
     * method never returns normally.
     * <p>
     * The argument serves as a status code; by convention, a nonzero
     * status code indicates abnormal termination.
     *
     * @param      status   exit status.
     * @throws  SecurityException if the caller does not have RuntimePermission("exitVM").
     * @see java.lang.SecurityException
     * @since      JDK1.0
     */
    public void exit(int status) {
        Permission required = new RuntimePermission("exitVM");
        AccessController.checkPermission(required);
        exitInternal(status);
    }

    /**
     * Returns the amount of free memory in the system. Calling the 
     * <code>gc</code> method may result in increasing the value returned 
     * by <code>freeMemory.</code>
     *
     * @return  an approximation to the total amount of memory currently
     *          available for future allocated objects, measured in bytes.
     */
    public native long freeMemory();

    /**
     * Returns the total amount of memory in the Java Virtual Machine. 
     * The value returned by this method may vary over time, depending on 
     * the host environment.
     * <p>
     * Note that the amount of memory required to hold an object of any 
     * given type may be implementation-dependent.
     * 
     * @return  the total amount of memory currently available for current 
     *          and future objects, measured in bytes.
     */
    public native long totalMemory();

    /**
     * Runs the garbage collector.
     * Calling this method suggests that the Java Virtual Machine expend 
     * effort toward recycling unused objects in order to make the memory 
     * they currently occupy available for quick reuse. When control 
     * returns from the method call, the Java Virtual Machine has made 
     * its best effort to recycle all discarded objects. 
     * <p>
     * The name <code>gc</code> stands for "garbage 
     * collector". The Java Virtual Machine performs this recycling 
     * process automatically as needed even if the 
     * <code>gc</code> method is not invoked explicitly.
     * <p>
     * The method {@link System#gc()} is the conventional and convenient 
     * means of invoking this method. 
     */
    public native void gc();

}
