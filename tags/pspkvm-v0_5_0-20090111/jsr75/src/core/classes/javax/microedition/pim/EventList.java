/*
 *   
 *
 * Portions Copyright  2000-2007 Sun Microsystems, Inc. All Rights
 * Reserved.  Use is subject to license terms.
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
 
/*
 * Copyright (C) 2002-2003 PalmSource, Inc.  All Rights Reserved.
 */

package javax.microedition.pim;

import java.util.Enumeration;

/**
 * This class is defined by the JSR-75 specification
 * <em>PDA Optional Packages for the J2ME&trade; Platform</em>
 */
// JAVADOC COMMENT ELIDED
public interface EventList extends PIMList {
    // JAVADOC COMMENT ELIDED
    public static final int STARTING = 0;
    // JAVADOC COMMENT ELIDED
    public static final int ENDING = 1;
    // JAVADOC COMMENT ELIDED
    public static final int OCCURRING = 2;

    // JAVADOC COMMENT ELIDED
    public abstract Event createEvent();

    // JAVADOC COMMENT ELIDED
    public abstract Event importEvent(Event item);

    // JAVADOC COMMENT ELIDED
    public abstract void removeEvent(Event item) throws PIMException;

    // JAVADOC COMMENT ELIDED
    public abstract Enumeration items(int searchType, long startDate,
				      long endDate, boolean initialEventOnly)
        throws PIMException;
    
    // JAVADOC COMMENT ELIDED
    public abstract int[] getSupportedRepeatRuleFields(int frequency);
    
}
