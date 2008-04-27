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

package com.sun.kvem.midp.pim;

/**
 * Specification of a PIM attribute.
 */
public class PIMAttribute {
    
    /** PIM: field. */
    private int attr;

    /** PIM: label. */
    private String label;
    
    /**
     * Constructor: attribute initialization.
     *
     * @param attr            ID
     * @param labelResource   label
     */
    public PIMAttribute(int attr, String labelResource) {
        this.attr = attr;
        this.label = labelResource;
    }

    /**
     * Default constructor: attribute initialization.
     */
    public PIMAttribute() {
        this(0, null);
    }

    /**
     * Gets attribute ID.
     *
     * @return the attribute ID
     */
    public int getAttr() {
        return attr;
    }
    
    /**
     * Gets attribute label.
     *
     * @return the attribute label
     */
    public String getLabel() {
        return label;
    }
}
