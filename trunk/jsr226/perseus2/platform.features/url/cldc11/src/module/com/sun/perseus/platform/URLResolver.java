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
 * This class is used to provide URL resolution. 
 *
 * @version $Id: URLResolver.java,v 1.4 2006/04/21 06:34:48 st125089 Exp $
 */
public class URLResolver {
    /**
     * @param contect the context URL string, also called the base URL.
     * @param spec the URL specification. May be a relative URL.
     * @return an absolute URL value.
     * @throws IllegalArgumentException if an absolute URL cannot be computed.
     */
    public static String resolve(String context, String spec) 
        throws IllegalArgumentException {
        
        try {
            if (context != null) {
                return (new PURL(new PURL(context), spec)).toString();
            } else {
                // We are dealing with a URL and we do not have a base
                // URI. Check if there is any protocol specified. If there is,
                // then we use the URL class to make the URL absolute. Otherwise,
                // we simply return the relative URL.
                if (spec.indexOf(':') != -1) {
                    return (new PURL(spec)).toString();
                } else {
                    return spec;
                }
            }
        } catch (Error e) {
            e.printStackTrace();
            throw new IllegalArgumentException();
        }
    }
}
