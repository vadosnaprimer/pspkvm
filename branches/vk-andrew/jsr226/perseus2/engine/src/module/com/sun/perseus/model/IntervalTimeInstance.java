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
 * This specialized <code>TimeInstance</code> is created by 
 * <code>SyncBaseCondition</code> time conditions.
 *
 * <p><code>IntervalTimeInstance</code> instances can be notified
 * of updates by the <code>TimeCondition</code> which created it.
 * This update can result on a <code>Time</code> update on the
 * instance depending on context.
 *
 * @see <a href="http://www.w3.org/TR/smil20/smil-timing.html#Timing-PropagatingTimes">
 *      SMIL 2 Timing: Propagating changes to time</a>
 *
 * @version $Id: IntervalTimeInstance.java,v 1.3 2006/06/29 10:47:32 ln156897 Exp $
 */
final class IntervalTimeInstance extends TimeInstance {
    /**
     * Offset from the interval sync base.
     */
    long offset;

    /**
     * Associated interval.
     */
    TimeInterval timeInterval;

    /**
     * Controls whether the instance synchronizes on begin or end
     */
    boolean isBeginSync;

    /**
     * Associated syncBase
     */
    TimedElementSupport syncBase;

    /**
     * Builds an instance time for the input <code>TimedElementSupport</code>
     * and time. The constructor will insert the instance
     * automatically into the <code>TimeInterval</code>'s corresponding
     * dependent list
     *
     * @param timedElement the associated <code>TimedElementSupport</code>. 
     *        Should not be null.
     * @param syncBase the <code>TimedElementSupport</code> this time instance
     *        synchronizes with.
     * @param offset this instance offset from the synch base.
     * @param clearOnReset defines whether or not this instance should
     *        be cleared from instance times lists on reset.
     * @param isBegin true if this object is part of the 
     *        timedElement's begin instance list.
     * @param isBeginSync true if this instance is synchronized on the 
     *        interval's begin time. False if this instance is synchronized 
     *        on the interval's end time.
     */
    IntervalTimeInstance(final TimedElementSupport timedElement,
                         final TimedElementSupport syncBase,
                         final long offset,
                         final boolean clearOnReset,
                         final boolean isBegin,
                         final boolean isBeginSync) {
        super(timedElement, 
              getTime(syncBase.currentInterval, 
                      isBeginSync, 
                      offset, 
                      timedElement, 
                      syncBase),
              clearOnReset, 
              isBegin);

        this.offset = offset;
        this.syncBase = syncBase;
        this.timeInterval = syncBase.currentInterval;
        this.isBeginSync = isBeginSync;

        timeInterval.addDependent(this);
    }

    /**
     * Invoke when this instance is no longer in a begin or end instance list,
     * i.e., when it is removed from one of these lists.
     *
     */
    void dispose() {
        timeInterval.removeDependent(this);
    }
    
    /**
     * Implementation helpers. 
     *
     * @param interval the interval to get
     * @param isBeginSync the time to get
     * @param offset offset from the sync base value.
     * @param timedElement the TimedElementSupport to which the interval
     *        belongs
     * @param syncBase the TimedElementSupport with which the timedElement
     *        is synchronized.
     *
     * @return the input interval's begin or end time. This method accounts for
     * the offset from the syncBase and for the time convertion between the
     * syncBase and the instance's timedElement.
     */
    private static Time getTime(final TimeInterval interval,
                                final boolean isBeginSync,
                                final long offset,
                                final TimedElementSupport timedElement,
                                final TimedElementSupport syncBase) {
        if (isBeginSync && !interval.begin.isResolved()) {
            // 'unresolved' and 'indefinite' times are not
            // legal begin time for intervals. This is an illegal
            // state, so we throw an exception.
            throw new IllegalStateException();
        }

        Time t = interval.begin;
        if (!isBeginSync) {
            t = interval.end;
        }

        if (!t.isResolved()) {
            return t;
        } else {
            // Convert the time value from the syncBase's time space
            // to the timedElement time space.
            Time time = new Time(t.value + offset);
            return timedElement.toContainerSimpleTime
                (syncBase.toRootContainerSimpleTime(time));
        }
    }

    /**
     * Used to synchronize this instance's time with its syncBase time. This is 
     * used when resetting timed elements.
     */
    void syncTime() {
        setTime(getTime(timeInterval, isBeginSync, offset, timedElement, 
                        syncBase));
    }

    /**
     * Must be called by the <code>TimeInterval</code>'s begin
     * or end time this instance depends on changes.
     * 
     * <p>This method update this time instance with a new
     * time, accounting for the offset from the sync base.</p>
     *
     */
    void onIntervalUpdate() {
        setTime(getTime(timeInterval, isBeginSync, offset, timedElement, 
                        syncBase));
    }

}

