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

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.sun.midp.security.SecurityToken;
import com.sun.midp.security.SecurityInitializer;
import com.sun.midp.security.SecurityInitializerImpl;
import com.sun.midp.security.Permissions;
import com.sun.midp.security.ImplicitlyTrustedClass;

import com.sun.midp.io.j2me.storage.RandomAccessStream;
import com.sun.midp.io.j2me.storage.File;
import javax.microedition.io.Connector;
import com.sun.midp.configurator.Constants;

/**
 * This class provides a way to securely access platform resources.
 * On some versions of the Java platform, there is a need to specify
 * a security token to access resources. This allows different 
 * versions of the security features.
 *
 * @version $Id: ResourceHandler.java,v 1.5 2006/07/17 00:35:44 st125089 Exp $
 */
public final class ResourceHandler {

    /**
     * Location of the default font face resource file
     */
    private static final String DEFAULT_FONT_FACE_FILE =
        "/com/sun/perseus/render/resources/defaultFont.svg";

    /**
     * Location of the intial font face resource file
     */
    private static final String INITIAL_FONT_FACE_FILE =
        "/com/sun/perseus/render/resources/initialFont.svg";

    /**
     * Inner class to request security token from SecurityInitializer. 
     * SecurityInitializer should be able to check this inner class name. 
     */
    static private class SecurityTrusted 
        implements ImplicitlyTrustedClass {}; 

    private static SecurityToken securityToken;

    static {
        try {
            securityToken = 
                com.sun.midp.security.SecurityInitializer.requestToken(new SecurityTrusted());
        } catch (SecurityException se) {
            // Just log the error. This may happen in development context
            // when running a midlet which bundles the SVG engine.
            se.printStackTrace();
        } catch (NoClassDefFoundError e) {
            // Just log the error. This may happen in development context
            // when running a midlet which bundles the SVG engine.
            e.printStackTrace();
        }
    }
    
    /**
     * Don't allow instances to be created.
     */
    private ResourceHandler() {
    }

    final public static InputStream getInitialFontResource() {
        InputStream is = null;
        
        try {
            is = getSystemResource(INITIAL_FONT_FACE_FILE, securityToken);
        } catch (SecurityException se) {
            //log security exceptions
            se.printStackTrace();
        }

        return is;
    }

    final public static InputStream getDefaultFontResource() {
        InputStream is = null;

        try {
            is = getSystemResource(DEFAULT_FONT_FACE_FILE, securityToken);
        } catch (SecurityException se) {
            //log security exceptions
            se.printStackTrace();
        }

        return is;
    }

    /**
     * @param resourceName the name of the resource to be retrieved. This has the 
     * same semantic and syntax as the java.lang.Class#getrResourceAsStream's name 
     * parameter.
     * @param securityToken opaque object which the caller must provide to grant
     * access to the system resource. Note that on some platform, this securityToken
     * is ignored because the access to the platform resources are secured through other
     * mechanisms.
     * @return null if the resource is not found. Otherwise, an stream to the 
     * requested resource.
     */
    private static InputStream getSystemResource(final String resourceName,
                                                final Object securityToken) {
        if (securityToken == null) {
            return ResourceHandler.class.getResourceAsStream(resourceName);
        } else {
            InputStream is = null;
            RandomAccessStream storage =
                new RandomAccessStream((SecurityToken) securityToken);

            try {
                // extract the file name part of the full resource name
                int namePartIdx = resourceName.lastIndexOf('/');
                String namePart = (namePartIdx != -1) ?
                    resourceName.substring(namePartIdx + 1) : resourceName;
                storage.connect(File.getConfigRoot(
                    Constants.INTERNAL_STORAGE_ID) + namePart,
                    Connector.READ);
                byte[] data = new byte[storage.getSizeOf()];
                storage.readBytes(data, 0, data.length);
                is = new ByteArrayInputStream(data);
            } catch (IOException e) {
                System.out.println("Error in getSystemResource");
            } finally {
                try {
                    storage.disconnect();
                } catch (IOException ignored) {
                }
            }
            return is;
        }
    }
}
