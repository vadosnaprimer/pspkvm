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

package java.io;

/**
 * Signals that a malformed UTF-8 string has been read in a data
 * input stream or by any class that implements the data input
 * interface. See the <code>writeUTF</code> method for the format in
 * which UTF-8 strings are read and written.
 *
 * @version 12/17/01 (CLDC 1.1)
 * @see     java.io.DataInput
 * @see     java.io.DataInputStream#readUTF(java.io.DataInput)
 * @see     java.io.IOException
 * @since   JDK1.0, CLDC 1.0
 */
public
class UTFDataFormatException extends IOException {
    /**
     * Constructs a <code>UTFDataFormatException</code> with
     * <code>null</code> as its error detail message.
     */
    public UTFDataFormatException() {
        super();
    }

    /**
     * Constructs a <code>UTFDataFormatException</code> with the
     * specified detail message. The string <code>s</code> can be
     * retrieved later by the
     * <code>{@link java.lang.Throwable#getMessage}</code>
     * method of class <code>java.lang.Throwable</code>.
     *
     * @param   s   the detail message.
     */
    public UTFDataFormatException(String s) {
        super(s);
    }
}
