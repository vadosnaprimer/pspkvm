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
 * Thrown to indicate that the application has attempted to convert 
 * a string to one of the numeric types, but that the string does not 
 * have the appropriate format. 
 *
 * @version 1.15, 12/04/99 (CLDC 1.0, Spring 2000)
 * @see     java.lang.Integer#toString()
 * @since   JDK1.0
 */
public
class NumberFormatException extends IllegalArgumentException {
    /**
     * Constructs a <code>NumberFormatException</code> with no detail message.
     */
    public NumberFormatException () {
    super();
    }

    /**
     * Constructs a <code>NumberFormatException</code> with the 
     * specified detail message. 
     *
     * @param   s   the detail message.
     */
    public NumberFormatException (String s) {
    super (s);
    }
}
