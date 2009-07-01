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
 * @version $Id: ThreadSupport.java,v 1.4 2006/04/21 06:34:53 st125089 Exp $
 */
public final class ThreadSupport {
    /**
     * Tests whether this thread has been interrupted. The interrupted status of
     * the thread is unaffected by this method.
     *
     * @returns true if this thread has been interrupted; false otherwise.
     */
    public static boolean isInterrupted(final Thread thread) {
        // IMPL NOTE : If there is no isInterrupted on the Thread class (as in CLDC 1.1),
        // we cannot check for this condition.
        return false;
    }

    /**
     * Sets the input thread as a daemon thread, if the platform supports that feature.
     *
     * @param thread the thread on which the isDaemon flag should be set.
     * @param isDaemon the daemon flag.
     */
    public static void setDaemon(final Thread th, final boolean isDaemon) {
        // Because CLDC 1.1 does not support daemon thread, we do not set this flag.
    }
}
