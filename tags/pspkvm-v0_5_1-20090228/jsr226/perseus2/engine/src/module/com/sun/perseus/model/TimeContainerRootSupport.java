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
 * 
 * @version $Id: TimeContainerRootSupport.java,v 1.2 2006/04/21 06:37:20 st125089 Exp $
 */
public class TimeContainerRootSupport extends TimeContainerSupport {
    /**
     * The time at which the document started. This is
     * initially set when animation is started and then
     * modified when the document's timeline is seeked
     * to a particular time.
     */
    protected long beginWallClockTime = -1;

    /**
     * The last sample time.
     */
    protected Time lastSampleTime = Time.UNRESOLVED;

    /**
     * Flags that the seek is backwards
     */
    protected boolean seekingBack;

    /**
     * There is no parent container for this root. The parent is this 
     * node itself.
     * Also, the root time container has a default 0-offset begin condition
     * added to it so that the first interval is created and is
     * [0, INDEFINITE[.
     */
    TimeContainerRootSupport() {
        timeContainer = this;

        // Add a new 0-offset begin condition.
        new OffsetCondition(this, true, 0);
    }

    /**
     * Always throws an exception because a root container should not have
     * a parent container.
     *
     * @param timeContainer time container
     * @throws IllegalArgumentException always thrown for a root time 
     *         container which should not have a parent container.
     */
    protected void setTimeContainer(final TimeContainerSupport timeContainer) {
        throw new IllegalArgumentException();
    }


    /**
     * Helper method. 
     *
     * @return this timed element's root container.
     */
    TimeContainerRootSupport getRootContainer() {
        return this;
    }

    /**
     * Converts the input 'local' time to an absolute wallclock time.
     *
     * @param localTime the time to convert to wallclock time
     * @return the time, converted to wall clock time.
     */
    long toWallClockTime(final long localTime) {
        return beginWallClockTime + localTime;
    }

    /**
     * Dispatches beginEvent. See the <code>TimedElementSupport</code> class
     * method implementation. This class also uses the opportunity to record
     * what the wallclock time is for the begin time so that localTimes can
     * be converted later.
     *
     * @param beginTime the interval begin time
     */
    void dispatchBeginEvent(final Time beginTime) {
        super.dispatchBeginEvent(beginTime);

        beginWallClockTime = System.currentTimeMillis();

        // With the following, beginTime will map to beginWallClockTime,
        // as expected.
        beginWallClockTime -= beginTime.value;
    }

    /**
     * For the root container, there is no container simple duration, so we
     * retun INDEFINITE, no matter what the state is.
     *
     * @return the container's simple duration.
     */
    Time getContainerSimpleDuration() {
        return Time.INDEFINITE;
    }

    /**
     * Converts the input simple time (i.e., a time in the parent container's
     * simple duration) to a root container simple time (i.e., a time
     * in the root time container's simple time interval).
     *
     * @param simpleTime the time in the parent container's simple duration
     * @return a time in the root time container's simple duration (i.e., in 
     *         the root container's simple time interval).
     */
    Time toRootContainerSimpleTime(final Time simpleTime) {
        return simpleTime;
    }


    /**
     * Converts the input simple time (i.e., a time in the parent container's
     * simple duration) to a root container simple time (i.e., a time
     * in the root time container's simple time interval).
     *
     * @param simpleTime the time in the parent container's simple duration
     * @return a time in the root time container's simple duration (i.e., in 
     *         the root container's simple time interval).
     */
    Time toRootContainerSimpleTimeClamp(final Time simpleTime) {
        // Note that no clamping is necessary here because the clamping is
        // done in a timed element, relative to its container's simple time.
        // By the time this is called on the root, the child timed element has
        // already applied the root's constraints.
        return simpleTime;
    }

    /**
     * Converts the input root container simple time (i.e., a time in the root
     * container's simple time interval) to a time in this element's time
     * container simple duration.
     *
     * @param rootSimpleTime the time in the root container's simple duration.
     * @return a simple time in the parent container's simple duration The
     * return value is in the [0, container simple duration] interval.
     *
     */
    Time toContainerSimpleTime(final Time rootSimpleTime) {
        return rootSimpleTime;
    }

    /**
     * Seek to the requested time. The time is assumed to be in 
     * document simple time.
     *
     * @param seekToTime the time to seek to
     */
    void seekTo(final Time seekToTime) {
        if (!seekToTime.isResolved()) {
            throw new IllegalStateException();
        }

        seeking = true;
        seekingBack = !seekToTime.greaterThan(lastSampleTime);
        try {
            sample(seekToTime);
        } finally {
            seeking = false;
        }
    }

    /**
     * Overrides the default sample time to capture the last sample time.
     *
     * @param currentTime the time at which this element should be 
     *        sampled. 
     */
    void sample(final Time currentTime) {
        super.sample(currentTime);
        lastSampleTime = currentTime;
    }
}
