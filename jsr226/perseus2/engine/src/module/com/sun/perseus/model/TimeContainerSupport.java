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

import java.io.PrintStream;

/**
 *
 * @version $Id: TimeContainerSupport.java,v 1.4 2006/06/29 10:47:35 ln156897 Exp $
 */
public class TimeContainerSupport extends TimedElementSupport {
    /**
     * The container's current simple time
     */
    protected Time simpleTime = Time.UNRESOLVED;

    /**
     * The set of children TimedElementSupport contained
     * in this container.
     */
    protected Vector timedElementChildren = new Vector(5);

    /**
     * Default constructor
     */
    public TimeContainerSupport() {
    }

    /**
     * @return this container's current simple time
     */
    public Time getSimpleTime() {
        return simpleTime;
    }

    /**
     * Resets this container and all its children. This, in effect, moves the
     * container back to the begining of its timeline, i.e., prior to begining
     * the first interval.
     */
    protected void initialize() {
        super.initialize();

        // If a new interval was created, children have already been initialized
        // (see dispatchOnNewInterval). Otherwise, make sure we initialize the 
        // full tree.
        if (currentInterval == null) {
            for (int i = 0; i < timedElementChildren.size(); i++) {
                TimedElementSupport child 
                    = (TimedElementSupport) timedElementChildren.elementAt(i);
                child.initialize();
            }        
        }
    }

    /**
     * When a container resets, it needs to clear all its children which have
     * IntervalTimeInstances on syncBases which are also children 
     * (or descendants) of this container.
     *
     * @see <a href="http://www.w3.org/TR/smil20#smil-timing-Timing-ResetDefaultAttribute">
     *      SMIL 2 Specification, Resetting element state</a>
     */
    void reset() {
        super.reset();

        for (int i = 0; i < timedElementChildren.size(); i++) {
            TimedElementSupport child = 
                (TimedElementSupport) timedElementChildren.elementAt(i);
            child.removeSyncBaseTimesUnder(this);
        }
        
    }


    /**
     * Removes  all <code>IntervalTimeInstance</code>s in the begin and end
     * instance list if the syncBase is a descendant of syncTimeContainer
     *
     * @param syncTimeContainer the container under which times should be 
     *        removed.
     */
    void removeSyncBaseTimesUnder(
            final TimeContainerSupport syncTimeContainer) {
        super.removeSyncBaseTimesUnder(syncTimeContainer);

        for (int i = 0; i < timedElementChildren.size(); i++) {
            TimedElementSupport child = 
                (TimedElementSupport) timedElementChildren.elementAt(i);
            child.removeSyncBaseTimesUnder(syncTimeContainer);
        }        
    }

    /**
     * Samples this time container at the given simple time.
     *
     * @param simpleTime this timed element's simple time.
     */
    void sampleAt(final long simpleTime) {
        setSimpleTime(simpleTime);

        for (int i = 0; i < timedElementChildren.size(); i++) {
            TimedElementSupport child 
                = (TimedElementSupport) timedElementChildren.elementAt(i);
            if (seeking) {
                child.seeking = true;
                child.sample(this.simpleTime);
                child.seeking = false;
            } else {
                child.sample(this.simpleTime);
            }
        }
    }

    /**
     * Dispatches endEvent. As per the SMIL 2 specification, this dispatches
     * an endEvent for the resolved end time, not the observed end
     * time.
     *
     * @param currentTime the current sampling time.
     *
     * @see <a href="http://www.w3.org/TR/smil20/smil-timing.html#Timing-BeginValueSemantics">
     *      SMIL2's Begin value semantics</a>
     */
    void dispatchEndEvent(final Time currentTime) {
        super.dispatchEndEvent(currentTime);

        // Now, force children to end now. Before invoking children, we need 
        // to update the container's simpleTime.
        long time = currentInterval.end.value - currentInterval.begin.value;
        if (simpleDur.isResolved()) {
            if (time > 0) {
                time = time % simpleDur.value;
                if (time == 0) {
                    time = simpleDur.value;
                }
            } 
        }
        
        endChildrenAt(time);
    }

    /**
     * Calls all the registered <code>TimeDependent</code>s so that they
     * are notified of a new TimeInterval creation. When a time container
     * creates a new interval, its children are re-initialized.
     */
    void dispatchOnNewInterval() {
        super.dispatchOnNewInterval();
        
        // Now, re-initialize all children.
        for (int i = 0; i < timedElementChildren.size(); i++) {
            TimedElementSupport child = 
                (TimedElementSupport) timedElementChildren.elementAt(i);
            child.initialize();
        }                
    }

    /**
     * Implementation helper to set the simple time object.
     *
     * @param time the new simple time value
     */
    void setSimpleTime(final long time) {
        if (simpleTime == Time.UNRESOLVED) {
            simpleTime = new Time(time);
        } else {
            simpleTime.value = time;
        }
    }

    /**
     * When a container starts a new iteration, it needs to:
     * - end its children at the end of the previous interval.
     * - reset its children.
     *
     * @param prevIter the last iteration this element was playing.
     * @param curIter the new iteration this element is playing.
     */
    protected void onStartingRepeat(final int prevIter, final int curIter) {
        // First, force ending children at the end of the previous iteration.
        long time = currentInterval.begin.value 
                    + 
                    simpleDur.value * (prevIter + 1);
        
        endChildrenAt(time);

        // Now, reset the children so that they are ready for the next sampling
        // at the begining of current interval.
        for (int i = 0; i < timedElementChildren.size(); i++) {
            TimedElementSupport child = 
                (TimedElementSupport) timedElementChildren.elementAt(i);
            child.initialize();
        }        
        
    }

    /**
     * Implementation helper. Ends all children at the requested input time.
     *
     * @param time the time, in this container's simple time system, at which
     * children should be stopped.
     */
    void endChildrenAt(final long time) {
        setSimpleTime(time);

        // First, end all children. Adding end times does not cause a state
        // transition. To make the children transition to their new state, we 
        // invoke sample in a second iteration (see below).
        for (int i = 0; i < timedElementChildren.size(); i++) {
            TimedElementSupport child 
                = (TimedElementSupport) timedElementChildren.elementAt(i);
            child.end();
        }

        // Now, sample children on the end time
        for (int i = 0; i < timedElementChildren.size(); i++) {
            TimedElementSupport child 
                = (TimedElementSupport) timedElementChildren.elementAt(i);
            child.sample(simpleTime);
        }        
    }

    /**
     * Debug method: Traces the container and all its children.
     */
    /**
     * Traces this viewport tree
     */
    void dump() {
        dump(this, "", System.err);
    }

    /**
     * Debug: traces the input ModelNode, using the input prefix
     * 
     * @param t the node to dump
     * @param prefix the string used to prefix the node information
     * @param out the stream where the node structure is dumped.
     */
    static void dump(final TimedElementSupport t, final String prefix,
                     final PrintStream out) {
        out.println(prefix + t);
        if (t instanceof TimeContainerSupport) {
            TimeContainerSupport tc = (TimeContainerSupport) t;
            for (int i = 0; i < tc.timedElementChildren.size(); i++) {
                TimedElementSupport c 
                    = (TimedElementSupport) tc
                        .timedElementChildren.elementAt(i);
                dump(c, prefix + "+--> ", out);
            }
        }
    }
}
