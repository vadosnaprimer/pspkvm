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

/**
 * An <code>Error</code> is a subclass of <code>Throwable</code>
 * that indicates serious problems that a reasonable application
 * should not try to catch. Most such errors are abnormal conditions.
 * <p>
 * A method is not required to declare in its <code>throws</code>
 * clause any subclasses of <code>Error</code> that might be thrown
 * during the execution of the method but not caught, since these
 * errors are abnormal conditions that should never occur.
 *
 * @version 1.11, 12/04/99 (CLDC 1.0, Spring 2000)
 * @since   JDK1.0
 */
public
class Error extends Throwable {
    /**
     * Constructs an <code>Error</code> with no specified detail message.
     */
    public Error() {
        super();
    }

    /**
     * Constructs an Error with the specified detail message.
     *
     * @param   s   the detail message.
     */
    public Error(String s) {
        super(s);
    }
}
