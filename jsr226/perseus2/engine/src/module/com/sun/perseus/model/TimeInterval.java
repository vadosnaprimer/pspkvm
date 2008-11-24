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
 * A <code>TimeInterval</code> models a specific 'run' of a
 * <code>TimedElement</code>. It has a begin and end time and
 * a list of <code>IntervalTimeInstance</code>s depending on its begin
 * or end time.
 *
 * @version $Id: TimeInterval.java,v 1.3 2006/04/21 06:39:30 st125089 Exp $
 */
public final class TimeInterval {
    /**
     * The interval's begin time.
     */
    Time begin;

    /**
     * The interval's end time
     */
    Time end;

    /**
     * The end of the last simple duration
     */
    Time lastDur;

    /**
     * The list of dependent begin time instances.
     * Contains <code>IntervalTimeInstance</code> objects.
     */
    Vector beginDependents;

    /**
     * The list of end dependents. Contains 
     * <code>IntervalTimeInstance</code> objects.
     */
    Vector endDependents;
     
    /**
     * Creates a new interval with a specific begin and end times.
     * 
     * @param begin the initial begin time. This time should be resolved.
     *        Otherwise, an <code>IllegalStateException</code> is thrown.
     *        Should not be null.
     * @param end the initial end time. Should not be null.
     */
    TimeInterval(final Time begin, final Time end) {
        if (begin == null || end == null) {
            throw new NullPointerException();
        }

        setBegin(begin);
        setEnd(end);
    }

    /**
     * Adds a new <code>IntervalTimeInstance</code> dependent.
     * If <code>timeInstance</code> synchronizes on begin, it is 
     * added to the <code>beginDependent</code> list. Otherwise,
     * it is added to the <code>endDependent</code> list.
     *
     * @param timeInstance the new <code>IntervalTimeInstance</code>.
     *        If null, throws a <code>NullPointerException</code>.
     */
    void addDependent(final IntervalTimeInstance timeInstance) {
        Vector dependents = beginDependents;
        if (!timeInstance.isBeginSync) {
            dependents = endDependents;
        }

        if (dependents == null) {
            dependents = new Vector(1);
            if (timeInstance.isBeginSync) {
                beginDependents = dependents;
            } else {
                endDependents = dependents;
            }
        }

        dependents.addElement(timeInstance);
    }

    /**
     * Removes the input <code>IntervalTimeInstance</code> dependent.
     *
     * @param timeInstance the <code>IntervalTimeInstance</code> to 
     *        remove from this interval. Throws a 
     *        <code>NullPointerException</code> if null.
     */
    void removeDependent(final IntervalTimeInstance timeInstance) {
        Vector dependents = beginDependents;
        if (!timeInstance.isBeginSync) {
            dependents = endDependents;
        }

        if (dependents == null) {
            return;
        }

        dependents.removeElement(timeInstance);
    }

    /**
     * Updates the begin time. Note that an unresolved begin time is 
     * illegal. Trying to set one will cause an exception to be thrown
     * (an IllegalArgumentException).
     * Dependent end conditions are notified of begin time change.
     *
     * @param newBegin the new begin time.
     */
    void setBegin(final Time newBegin) {
        if (!newBegin.isResolved()) {
            throw new IllegalArgumentException("" + newBegin);
        }

        this.begin = newBegin;
        if (beginDependents != null) {
            int n = beginDependents.size();
            for (int i = 0; i < n; i++) {
                ((IntervalTimeInstance) beginDependents.elementAt(i))
                    .onIntervalUpdate();
            }
        }
    }

    /**
     * Updates the end time. Dependent end conditions are notified
     * of the end time change. A 
     *
     * @param newEnd the new end time.
     */
    void setEnd(final Time newEnd) {
        if (newEnd == null) {
            throw new NullPointerException();
        }
        this.end = newEnd;
        if (endDependents != null) {
            int n = endDependents.size();
            for (int i = 0; i < n; i++) {
                ((IntervalTimeInstance) endDependents.elementAt(i))
                    .onIntervalUpdate();
            }
        }
    }

    /**
     * Called when the interval is pruned from a timed element.  The result is
     * that all dependent time instances should be removed from their respective
     * instance lists.
     */
    void prune() {
        int n = beginDependents != null ? beginDependents.size() : 0;
        for (int i = n - 1; i >= 0; i--) {
            IntervalTimeInstance iti = 
                (IntervalTimeInstance) beginDependents.elementAt(i);
            iti.timedElement.removeTimeInstance(iti);
            iti.dispose();
        }

        n = endDependents != null ? endDependents.size() : 0;
        for (int i = n - 1; i >= 0; i--) {
            IntervalTimeInstance iti = 
                (IntervalTimeInstance) endDependents.elementAt(i);
            iti.timedElement.removeTimeInstance(iti);
            iti.dispose();
        }
    }
}

