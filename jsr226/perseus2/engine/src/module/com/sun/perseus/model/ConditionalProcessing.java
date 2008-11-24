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

package com.sun.perseus.model;

/**
 * <code>ConditionalProcessing</code> is a helper class which handles the 
 * conditional processing decisions for Perseus classes.
 *
 * @version $Id: ConditionalProcessing.java,v 1.3 2006/06/29 10:47:30 ln156897 Exp $
 */
public final class ConditionalProcessing {
    /**
     * Required Features for SVG Tiny
     */
    public static final String[] SUPPORTED_FEATURES = {
        "http://www.w3.org/TR/SVG11/feature#CoreAttribute",
        "http://www.w3.org/TR/SVG11/feature#BasicStructure",
        "http://www.w3.org/TR/SVG11/feature#BasicPaintAttribute",
        "http://www.w3.org/TR/SVG11/feature#BasicGraphicsAttribute",
        "http://www.w3.org/TR/SVG11/feature#Hyperlinking",
        "http://www.w3.org/TR/SVG11/feature#XlinkAttribute",
        "http://www.w3.org/TR/SVG11/feature#ConditionalProcessing",
        "http://www.w3.org/TR/SVG11/feature#Shape",
        "http://www.w3.org/TR/SVG11/feature#Image",
        "http://www.w3.org/TR/SVG11/feature#BasicText",
        "http://www.w3.org/TR/SVG11/feature#BasicFont",
        "http://www.w3.org/TR/SVGMobile/Tiny/feature#base",
        /*
        "http://www.w3.org/TR/SVG11/feature#Animation",
        "http://www.w3.org/TR/SVG11/feature#Extensibility",
        */
        "http://www.w3.org/TR/SVGMobile/Tiny/feature#interactivity",
        /*
        "http://www.w3.org/TR/SVGMobile/Tiny/feature#all"
        */
    };
    
    /**
     * The user language
     * <!> IMPL NOTE : Make this configurable, at least at Perseus
     *     load time.
     */
    public static final String USER_LANGUAGE = "en";

    /**
     * Disallow instanciation
     */
    private ConditionalProcessing() {
    }

    /**
     * The list of supported features:
     * http://www.w3.org/TR/SVG11/feature#CoreAttribute
     * http://www.w3.org/TR/SVG11/feature#BasicStructure
     * http://www.w3.org/TR/SVG11/feature#BasicPaintAttribute
     * http://www.w3.org/TR/SVG11/feature#BasicGraphicsAttribute
     * http://www.w3.org/TR/SVG11/feature#XlinkAttribute
     * http://www.w3.org/TR/SVG11/feature#ConditionalProcessing
     * http://www.w3.org/TR/SVG11/feature#Shape
     * http://www.w3.org/TR/SVG11/feature#Image
     * http://www.w3.org/TR/SVG11/feature#BasicText
     * http://www.w3.org/TR/SVG11/feature#BasicFont
     * http://www.w3.org/TR/SVGMobile/Tiny/feature#base
     *
     * The list of unsupported features:
     * http://www.w3.org/TR/SVG11/feature#Hyperlinking
     * http://www.w3.org/TR/SVG11/feature#Animation
     * http://www.w3.org/TR/SVG11/feature#Extensibility
     * http://www.w3.org/TR/SVGMobile/Tiny/feature#interactivity
     * http://www.w3.org/TR/SVGMobile/Tiny/feature#all
     *
     * @param requiredFeatures the set of features to check
     * @return true if all the features in the input array are supported.
     */
    public static boolean checkFeatures(final String[] requiredFeatures) {
        for (int i = 0; i < requiredFeatures.length; i++) {
            // Check that the feature is supported
            if (!isFeatureSupported(requiredFeatures[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param requiredFeature the feature to check
     * @return true if the input feature is supported, i.e. if it is one
     *         of the supported features
     * @see #SUPPORTED_FEATURES
     */
    public static boolean isFeatureSupported(final String requiredFeature) {
        for (int i = 0; i < SUPPORTED_FEATURES.length; i++) {
            if (requiredFeature == SUPPORTED_FEATURES[i]) {
                return true;
            }
        }
        return false;
    }

    /**
     * There are currently no supported extensions in Perseus
     * 
     * @param requiredExtensions the array of required extensions to test
     * @return false
     */
    public static boolean checkExtensions(final String[] requiredExtensions) {
        return false;
    }

    /**
     * The user language preference is controlled by this class
     *
     * @param systemLanguage the set of languages to check
     * @return true if the user language matches one of the systemLanguage
     *         item.s
     */
    public static boolean checkLanguage(final String[] systemLanguage) {
        for (int i = 0; i < systemLanguage.length; i++) {
            if (USER_LANGUAGE == systemLanguage[i]) {
                return true;
            }
        }
        return false;
    }
}
