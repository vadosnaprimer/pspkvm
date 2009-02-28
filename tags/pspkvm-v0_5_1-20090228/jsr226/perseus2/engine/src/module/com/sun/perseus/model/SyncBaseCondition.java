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

import java.util.Vector;

/**
 * An <code>SyncBaseCondition</code> generates a <code>TimeInstance</code>
 * everytime the SyncBase <code>TimedElementSupport</code> generates a new
 * interval (i.e., each time its <code>newInterval</code> method is 
 * called by the <code>TypedElement</code>. A <code>SyncBaseCondition</code>
 * is a time dependent of it's sync base <code>TimedElementSupport</code>
 *
 * @version $Id: SyncBaseCondition.java,v 1.3 2006/04/21 06:39:09 st125089 Exp $
 */
public final class SyncBaseCondition extends TimeCondition 
    implements TimeDependent, IDRef {
    /**
     * The SyncBase identifier
     */
    String syncBaseId;

    /**
     * SyncBase <code>TimedElementSupport</code>
     */
    TimedElementSupport syncBase;
    
    /**
     * True if this condition is on the syncBase's begin.
     */
    boolean isBeginSync;

    /**
     * Offset from the synch base
     */
    long offset;

    /**
     * @param timedElementNode the associated <code>TimedElementNode</code>. 
     *        Should not be null.
     * @param isBegin defines whether this condition is for a begin list.
     * @param syncBaseId identifier of the <code>TimedElementSupport</code> this
     *                 condition is synchronized on. Should not be null.
     * @param isBeginSync true if this condition is on the syncBase's begin
     *                    condition. False if this condition is on the 
     *                    syncBase's end condition.
     * @param offset offset from the sync base. This means that time instances
     *        synchronized on the syncBase begin or end time are offset by 
     *        this amount.
     */
    public SyncBaseCondition(final TimedElementNode timedElementNode,
                             final boolean isBegin,
                             final String syncBaseId,
                             final boolean isBeginSync,
                             final long offset) {
        super(timedElementNode.timedElementSupport, isBegin);

        if (syncBaseId == null) {
            throw new NullPointerException();
        }

        this.syncBaseId = syncBaseId;
        this.isBeginSync = isBeginSync;
        this.offset = offset;

        timedElementNode.ownerDocument.resolveIDRef(this, syncBaseId);
    }

    /**
     * <code>IDRef</code> implementation.
     *
     * @param ref the resolved reference (mapped from the syncBase
     *        id passed to the constructor.
     */
    public void resolveTo(final ElementNode ref) {
        if (!(ref instanceof TimedElementNode)) {
            // The condition is synchronized on an element which does 
            // not have timing. Do nothing, this is _not_ an error.
            return;
        }

        syncBase = ((TimedElementNode) ref).timedElementSupport;

        if (isBeginSync) {
            if (syncBase.beginDependents == null) {
                syncBase.beginDependents = new Vector(1);
            }
            syncBase.beginDependents.addElement(this);
        } else {
            if (syncBase.endDependents == null) {
                syncBase.endDependents = new Vector(1);
            }
            syncBase.endDependents.addElement(this);
        }
    }

    /**
     * Called by the associated sync base when it creates a new 
     * current <code>TimeInterval</code>. Whenever this happens, a
     * new IntervalTimeInstance is added for the sync base's begin or end 
     * interval time (depending on isBeginSync), to the timedElement's
     * begin or end instance list (depending on isBegin).
     *
     * @param syncBase the element which just generated a new interval.
     */
    public void onNewInterval(final TimedElementSupport syncBase) {
        // IntervalTimeInstances are _not_ cleared on reset
        new IntervalTimeInstance(timedElement, syncBase, 
                                 offset, false, isBegin, isBeginSync);
    }

    /**
     * Converts this <code>SyncBaseCondition</code> to a String trait.
     *
     * @return a string describing this <code>TimeCondition</code>
     */
    protected String toStringTrait() {
        StringBuffer sb = new StringBuffer();
        sb.append(syncBaseId);
        sb.append('.');
        if (isBeginSync) {
            sb.append("begin");
        } else {
            sb.append("end");
        }

        if (offset != 0) {
            if (offset > 0) {
                sb.append('+');
            }
            sb.append(offset / 1000f);
            sb.append('s');
        }

        return sb.toString();
    }

}
