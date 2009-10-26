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

// SAX exception class.

package org.xml.sax;

/**
 * Encapsulate an XML parse error or warning.
 *
 * <blockquote>
 * <em>This module, both source code and documentation, is in the
 * Public Domain, and comes with <strong>NO WARRANTY</strong>.</em>
 * </blockquote>
 *
 * <p>This exception will include information for locating the error
 * in the original XML document.  Note that although the application
 * will receive a SAXParseException,
 * the application is not actually required to throw the exception; 
 * instead, it can simply read the information in it and take a 
 * different action.</p>
 *
 * <p>Since this exception is a subclass of {@link org.xml.sax.SAXException 
 * SAXException}, it inherits the ability to wrap another exception.</p>
 *
 * @since SAX 1.0
 *         
 * @version 2.0
 * @see org.xml.sax.SAXException
 * @see org.xml.sax.Locator
 */
public class SAXParseException extends SAXException {
    
    
    //////////////////////////////////////////////////////////////////////
    // Constructors.
    //////////////////////////////////////////////////////////////////////


    /**
     * Create a new SAXParseException from a message and a Locator.
     *
     * @param message The error or warning message.
     * @param locator The locator object for the error or warning (may be
     *        null).
     * @see org.xml.sax.Locator
     */
    
    public SAXParseException (String message, Locator locator) {
  	  super(message);
      
      if (locator != null) {
          this.publicId = locator.getPublicId();
          this.systemId = locator.getSystemId();
          this.lineNumber = locator.getLineNumber();
          this.columnNumber = locator.getColumnNumber();
      } else {
          this.lineNumber   = -1;
          this.columnNumber = -1;
      }
    }

    
    /**
     * Get the public identifier of the entity where the exception occurred.
     *
     * @return A string containing the public identifier, or null
     *         if none is available.
     * @see org.xml.sax.Locator#getPublicId
     */
    
    public String getPublicId ()
    {
	return this.publicId;
    }
    
    
    /**
     * Get the system identifier of the entity where the exception occurred.
     *
     * <p>If the system identifier is a URL, it will be resolved
     * fully.</p>
     *
     * @return A string containing the system identifier, or null
     *         if none is available.
     * @see org.xml.sax.Locator#getSystemId
     */
    
    public String getSystemId ()
    {
	return this.systemId;
    }
    
    
    /**
     * The line number of the end of the text where the exception occurred.
     *
     * @return An integer representing the line number, or -1
     *         if none is available.
     * @see org.xml.sax.Locator#getLineNumber
     */
    
    public int getLineNumber ()
    {
	return this.lineNumber;
    }
    
    
    /**
     * The column number of the end of the text where the exception occurred.
     *
     * <p>The first column in a line is position 1.</p>
     *
     * @return An integer representing the column number, or -1
     *         if none is available.
     * @see org.xml.sax.Locator#getColumnNumber
     */
    
    public int getColumnNumber ()
    {
	return this.columnNumber;
    }
    
    
    
    //////////////////////////////////////////////////////////////////////
    // Internal state.
    //////////////////////////////////////////////////////////////////////


    /**
     * @serial The public identifier, or null.
     * @see #getPublicId
     */    
    private String publicId;


    /**
     * @serial The system identifier, or null.
     * @see #getSystemId
     */
    private String systemId;


    /**
     * @serial The line number, or -1.
     * @see #getLineNumber
     */
    private int lineNumber;


    /**
     * @serial The column number, or -1.
     * @see #getColumnNumber
     */
    private int columnNumber;
    
}

// end of SAXParseException.java
