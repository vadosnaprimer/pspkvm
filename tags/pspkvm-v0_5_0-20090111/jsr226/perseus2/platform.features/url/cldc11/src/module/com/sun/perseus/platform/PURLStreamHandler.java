/*
 * @(#)URLStreamHandler.java	1.53 05/03/12
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
 *
 */

package com.sun.perseus.platform;

/**
 * The abstract class <code>URLStreamHandler</code> is the common
 * superclass for all stream protocol handlers. A stream protocol
 * handler knows how to make a connection for a particular protocol
 * type, such as <code>http</code>.
 * <p>
 * In most cases, an instance of a <code>URLStreamHandler</code>
 * subclass is not created directly by an application. Rather, the
 * first time a protocol name is encountered when constructing a
 * <code>URL</code>, the appropriate stream protocol handler is
 * automatically loaded.
 *
 */
class PURLStreamHandler {

    /**
     * Converts a <code>URL</code> of a specific protocol to a
     * <code>String</code>.
     *
     * @param   u   the URL.
     * @return  a string representation of the <code>URL</code> argument.
     */
    protected static String toExternalForm(PURL u) {

	// pre-compute length of StringBuffer
	int len = u.getProtocol().length() + 1;
	if (u.getAuthority() != null && u.getAuthority().length() > 0)
	    len += 2 + u.getAuthority().length();
	if (u.getPath() != null) {
	    len += u.getPath().length();
	}
	if (u.getQuery() != null) {
	    len += 1 + u.getQuery().length();
	}
	if (u.getRef() != null) 
	    len += 1 + u.getRef().length();

	StringBuffer result = new StringBuffer(len);
	result.append(u.getProtocol());
        result.append(":");
        if (u.getAuthority() != null && u.getAuthority().length() > 0) {
            result.append("//");
            result.append(u.getAuthority());
        }
        if (u.getPath() != null) {
            result.append(u.getPath());
        }
        if (u.getQuery() != null) {
            result.append('?');
            result.append(u.getQuery());
        }
	if (u.getRef() != null) {
	    result.append("#");
            result.append(u.getRef());
	}
	return result.toString();
    }

    /**
     * Sets the fields of the <code>URL</code> argument to the indicated values.
     * Only classes derived from URLStreamHandler are supposed to be able
     * to call the set method on a URL.
     *
     * @param   u         the URL to modify.
     * @param   protocol  the protocol name.
     * @param   host      the remote host value for the URL.
     * @param   port      the port on the remote machine.
     * @param   authority the authority part for the URL.
     * @param   userInfo the userInfo part of the URL.
     * @param   path      the path component of the URL. 
     * @param   query     the query part for the URL.
     * @param   ref       the reference.
     * @exception	SecurityException	if the protocol handler of the URL is 
     *					different from this one
     * @see     java.net.URL#set(java.lang.String, java.lang.String, int, java.lang.String, java.lang.String)
     */
       protected static void setURL(PURL u, String protocol, String host, int port,
		             String authority, String userInfo, String path,
                             String query, String ref) {

	// ensure that no one can reset the protocol on a given URL.
        u.set(u.getProtocol(), host, port, authority, userInfo, path, query, ref);
    }

    /**
     * Parses the string representation of a <code>URL</code> into a
     * <code>URL</code> object.
     * <p>
     * If there is any inherited context, then it has already been
     * copied into the <code>URL</code> argument.
     * <p>
     * The <code>parseURL</code> method of <code>URLStreamHandler</code>
     * parses the string representation as if it were an
     * <code>http</code> specification. Most URL protocol families have a
     * similar parsing. A stream protocol handler for a protocol that has
     * a different syntax must override this routine.
     *
     * @param   u       the <code>URL</code> to receive the result of parsing
     *                  the spec.
     * @param   spec    the <code>String</code> representing the URL that
     *                  must be parsed.
     * @param   start   the character index at which to begin parsing. This is
     *                  just past the '<code>:</code>' (if there is one) that
     *                  specifies the determination of the protocol name.
     * @param   limit   the character position to stop parsing at. This is the
     *                  end of the string or the position of the
     *                  "<code>#</code>" character, if present. All information
     *                  after the sharp sign indicates an anchor.
     */
    public static void parseURL(PURL u, String spec, int start, int limit) {
        // These fields may receive context content if this was relative URL
        String protocol = u.getProtocol();
        String authority = u.getAuthority(); 
        String userInfo = u.getUserInfo();
        String host = u.getHost();
        int port = u.getPort();
        String path = u.getPath();
	String query = u.getQuery();

        // This field has already been parsed
        String ref = u.getRef();

	boolean isRelPath = false;
	boolean queryOnly = false;

// FIX: should not assume query if opaque
        // Strip off the query part
	if (start < limit) {
            int queryStart = spec.indexOf('?');
            queryOnly = queryStart == start;
            if ((queryStart != -1) && (queryStart < limit)) {
                query = spec.substring(queryStart+1, limit);
                if (limit > queryStart)
                    limit = queryStart;
                spec = spec.substring(0, queryStart);
            }
	}

	int i = 0;
        // Parse the authority part if any
	if ((start <= limit - 2) && (spec.charAt(start) == '/') &&
	    (spec.charAt(start + 1) == '/')) {
	    start += 2;
	    i = spec.indexOf('/', start);
            if (i < 0) {
	        i = spec.indexOf('?', start);
		if (i < 0)
                    i = limit;
	    }

            host = authority = spec.substring(start, i);

            int ind = authority.indexOf('@');
            if (ind != -1) {
                userInfo = authority.substring(0, ind);
                host = authority.substring(ind+1);
	    } else {
		userInfo = null;
	    }
	    if (host != null) {
		// If the host is surrounded by [ and ] then its an IPv6 
		// literal address as specified in RFC2732
		if (host.length()>0 && (host.charAt(0) == '[')) {
		    //PURLStreamHandler has been modified - it does not
		    //handle IPV6 addresses
		    //this is an ipv6 address

		    throw new Error("Can't handle IPv6 addresses!");

		} else {
		    ind = host.indexOf(':');
		    port = -1;
		    if (ind >= 0) {
			// port can be null according to RFC2396
			if (host.length() > (ind + 1)) {
			    port = Integer.parseInt(host.substring(ind + 1));
			}
			host = host.substring(0, ind);
		    }
		}
	    } else {
		host = "";
	    }
	    if (port < -1)
		throw new IllegalArgumentException("Invalid port number :" +
						   port);
	    start = i;
	    // If the authority is defined then the path is defined by the
            // spec only; See RFC 2396 Section 5.2.4.
            if (authority != null && authority.length() > 0)
                path = "";
	} 

 	if (host == null) {
 	    host = "";
 	}

        // Parse the file path if any
	if (start < limit) {
	    if (spec.charAt(start) == '/') {
		path = spec.substring(start, limit);
	    } else if (path != null && path.length() > 0) {
		isRelPath = true;
		int ind = path.lastIndexOf('/');
		String seperator = "";
		if (ind == -1 && authority != null)
		    seperator = "/";
		path = path.substring(0, ind + 1) + seperator +
		         spec.substring(start, limit);
					
	    } else {
		String seperator = (authority != null) ? "/" : "";
		path = seperator + spec.substring(start, limit);
	    }
	} else if (queryOnly && path != null) {
            int ind = path.lastIndexOf('/');
            if (ind < 0)
                ind = 0;
            path = path.substring(0, ind) + "/";
        }
	if (path == null)
	    path = "";

	if (isRelPath) {
            // Remove embedded /./
            while ((i = path.indexOf("/./")) >= 0) {
	        path = path.substring(0, i) + path.substring(i + 2);
	    }
            // Remove embedded /../ if possible
	    i = 0;
	    while ((i = path.indexOf("/../", i)) > 0) {
	        if ((limit = path.lastIndexOf('/', i - 1)) >= 0) {
		    path = path.substring(0, limit) + path.substring(i + 3);
		    i = 0;
	        } else {
		    i = i + 3;
		}
	    }
            // Remove trailing .. if possible
            while (path.endsWith("/..")) {
                i = path.indexOf("/..");
	        if ((limit = path.lastIndexOf('/', i - 1)) >= 0) {
		    path = path.substring(0, limit+1);
	        } else {
		    break;
		}
	    }
	    // Remove starting .
            if (path.startsWith("./") && path.length() > 2)
                path = path.substring(2);

            // Remove trailing .
            if (path.endsWith("/."))
                path = path.substring(0, path.length() -1);
	}

	setURL(u, protocol, host, port, authority, userInfo, path, query, ref);
    }

}
