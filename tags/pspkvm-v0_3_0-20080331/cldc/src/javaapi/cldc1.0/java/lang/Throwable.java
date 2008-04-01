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
 * The <code>Throwable</code> class is the superclass of all errors
 * and exceptions in the Java language. Only objects that are
 * instances of this class (or of one of its subclasses) are thrown
 * by the Java Virtual Machine or can be thrown by the Java
 * <code>throw</code> statement. Similarly, only this class or one of
 * its subclasses can be the argument type in a <code>catch</code>
 * clause.
 * <p>
 * Instances of two subclasses, {@link java.lang.Error} and
 * {@link java.lang.Exception}, are conventionally used to indicate
 * that exceptional situations have occurred. Typically, these instances
 * are freshly created in the context of the exceptional situation so
 * as to include relevant information (such as stack trace data).
 * <p>
 * By convention, class <code>Throwable</code> and its subclasses have
 * two constructors, one that takes no arguments and one that takes a
 * <code>String</code> argument that can be used to produce an error
 * message.
 * <p>
 * A <code>Throwable</code> class contains a snapshot of the
 * execution stack of its thread at the time it was created. It can
 * also contain a message string that gives more information about
 * the error.
 * <p>
 * Here is one example of catching an exception:
 * <p><blockquote><pre>
 *     try {
 *         int a[] = new int[2];
 *         a[4];
 *     } catch (ArrayIndexOutOfBoundsException e) {
 *         System.out.println("exception: " + e.getMessage());
 *         e.printStackTrace();
 *     }
 * </pre></blockquote>
 *
 * @version 1.43, 12/04/99 (CLDC 1.0, Spring 2000)
 * @since   JDK1.0
 */
public class Throwable {

    /** WARNING: this must be the first variable.
     * Specific details about the Throwable.  For example,
     * for FileNotFoundThrowables, this contains the name of
     * the file that could not be found.
     *
     * @serial
     */
    private String detailMessage;

    /** WARNING: this must be the second variable.
     * Native code saves some indication of the stack backtrace in this
     * slot.
     */
    private transient Object backtrace;

    /**
     * Constructs a new <code>Throwable</code> with <code>null</code> as
     * its error message string.
     */
    public Throwable() {
        fillInStackTrace(); // Added for this VM
    }

    /**
     * Constructs a new <code>Throwable</code> with the specified error
     * message.
     *
     * @param   message   the error message. The error message is saved for
     *          later retrieval by the {@link #getMessage()} method.
     */
    public Throwable(String message) {
        fillInStackTrace(); // Added for this VM
        detailMessage = message;
    }

    /**
     * Returns the error message string of this throwable object.
     *
     * @return  the error message string of this <code>Throwable</code>
     *          object if it was {@link #Throwable(String) created} with an
     *          error message string; or <code>null</code> if it was
     *          {@link #Throwable() created} with no error message.
     *
     */
    public String getMessage() {
        return detailMessage;
    }

    /**
     * Returns a short description of this throwable object.
     * If this <code>Throwable</code> object was
     * {@link #Throwable(String) created} with an error message string,
     * then the result is the concatenation of three strings:
     * <ul>
     * <li>The name of the actual class of this object
     * <li>": " (a colon and a space)
     * <li>The result of the {@link #getMessage} method for this object
     * </ul>
     * If this <code>Throwable</code> object was {@link #Throwable() created}
     * with no error message string, then the name of the actual class of
     * this object is returned.
     *
     * @return  a string representation of this <code>Throwable</code>.
     */
    public String toString() {
        String s = getClass().getName();
        String message = getMessage();
        return (message != null) ? (s + ": " + message) : s;
    }

    /**
     * Prints this <code>Throwable</code> and its backtrace to the
     * standard error stream. This method prints a stack trace for this
     * <code>Throwable</code> object on the error output stream that is
     * the value of the field <code>System.err</code>. The first line of
     * output contains the result of the {@link #toString()} method for
     * this object. <p>
     *
     * The format of the backtrace information depends on the implementation.
     */
/* 
    public void printStackTrace() {
        java.io.PrintStream err = System.err;
        String message = getMessage();
        err.print(this.getClass().getName());

        if (message != null) { 
            err.print(": ");
            err.println(message);
        } else { 
            err.println();
        }

        if (backtrace != null) { 
            printStackTrace0(System.err);
        }
    }
*/

    public native void printStackTrace();

    /* Added for this VM but made private since */
    /* the CLDC standard does not support this method */

    /**
     * Fills in the execution stack trace. This method records within this
     * <code>Throwable</code> object information about the current state of
     * the stack frames for the current thread. This method is useful when
     * an application is re-throwing an error or exception. For example:
     * <p><blockquote><pre>
     *     try {
     *         a = b / c;
     *     } catch(ArithmeticThrowable e) {
     *         a = Double.MAX_VALUE;
     *         throw e.fillInStackTrace();
     *     }
     * </pre></blockquote>
     *
     * @return  this <code>Throwable</code> object.
     * @see     java.lang.Throwable#printStackTrace()
     */
    private native void fillInStackTrace();

}
