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

// SAXNotRecognizedException.java - unrecognized feature or value.
// 
// NO WARRANTY!  This class is in the Public Domain.

package org.xml.sax;

/**
 * Exception class for an unrecognized identifier.
 *
 * <blockquote>
 * <em>This module, both source code and documentation, is in the
 * Public Domain, and comes with <strong>NO WARRANTY</strong>.</em>
 * </blockquote>
 *
 * <p>An DefaultHandler will throw this exception when it finds an
 * unrecognized feature or property identifier; SAX applications and
 * extensions may use this class for other, similar purposes.</p>
 *
 * @since SAX 2.0
 *         
 * @version 2.0
 * @see org.xml.sax.SAXNotSupportedException
 */
public class SAXNotRecognizedException extends SAXException
{

    /**
     * Construct a new exception with the given message.
     *
     * @param message The text message of the exception.
     */
    public SAXNotRecognizedException (String message)
    {
	super(message);
    }

}

// end of SAXNotRecognizedException.java
