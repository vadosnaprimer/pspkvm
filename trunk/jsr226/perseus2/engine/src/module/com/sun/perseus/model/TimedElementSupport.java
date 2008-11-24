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
 * The <code>TimedElementSupport</code> class is the main abstraction of the
 * SMIL timing model implementation.
 *
 * <p>It is responsible for computing the animation's state (inactive, active,
 * post-acitve, frozen) and, when the application is active, what the current
 * simple time is. That simple time is then used by the animation engine to
 * compute animated values. </p>
 * 
 * <p>SMIL has a notion of time containers. A <code>TimedElementSupport</code>
 * is the child of a time container. The relationship is that the element's
 * current time is the container's simple time. In otherwords, sampling a
 * container for a given time (in its own container), yields a simple time for
 * the container. That simple time is the timed element's current time, used to
 * compute the timed element's own simple time. Refer to the SMIL 2
 * specification for an explanation of what the various time semantics are. </p>
 *
 * @version $Id: TimedElementSupport.java,v 1.7 2006/07/13 00:55:58 st125089 Exp $
 */
public class TimedElementSupport {
    /**
     * Last Simple Duration End event type.
     */
    public static final String LAST_DUR_END_EVENT_TYPE = "lastDurEndEvent";

    /**
     * Seek End event type.
     */
    public static final String SEEK_END_EVENT_TYPE = "seekEndEvent";

    /**
     * Seek End event type.
     */
    public static final String SEEK_BEGIN_EVENT_TYPE = "seekBeginEvent";

    /**
     * End event type.
     */
    public static final String END_EVENT_TYPE = "endEvent";

    /**
     * Begin event type.
     */
    public static final String BEGIN_EVENT_TYPE = "beginEvent";

    /**
     * Repeat event type
     */
    public static final String REPEAT_EVENT_TYPE = "repeat";

    /**
     * Used when this <code>TimedElement</code> can be restarted under
     * any circumstance.
     */
    public static final int RESTART_ALWAYS = 1;

    /**
     * Used when this <code>TimedElement</code> can only be restarted when
     * it is not active (i.e., it does not have a current interval.
     */
    public static final int RESTART_WHEN_NOT_ACTIVE = 2;

    /**
     * Used when this <code>TimedElement</code> cannot be restarted under
     * any circumstance
     */
    public static final int RESTART_NEVER = 3;

    /**
     * Used when this <code>TimedElement</code> should not modify the
     * animated value when it is post active.
     */
    public static final int FILL_BEHAVIOR_REMOVE = 1;

    /**
     * Used when this <code>TimedElement</code> should maintain the
     * last value of its simple interval after it becomes inactive.
     */
    public static final int FILL_BEHAVIOR_FREEZE = 2;

    /**
     * State before the element has been initialized
     */
    protected static final int STATE_PRE_INIT = 1;

    /**
     * State where there has never been a current interval
     * (since the last init).
     */
    protected static final int STATE_NO_INTERVAL = 2;

    /**
     * State while the element is waiting to begin
     * the first interval.
     */
    protected static final int STATE_WAITING_INTERVAL_0 = 3;

    /**
     * State while the element is waiting to begin
     * an interval other than the first one
     */
    protected static final int STATE_WAITING_INTERVAL_N = 4;

    /**
     * State while the element is playing the current
     * interval.
     */
    protected static final int STATE_PLAYING = 5;

    /**
     * State when the element performs any fill on the previous
     * interval.
     */
    protected static final int STATE_FILL = 6;

    /**
     * This time value is used to avoid creating Time instances
     * over and over again in animation loops, when dispatching
     * events.
     */
    protected final Time eventTime = new Time(0);

    /**
     * The element's state. One of the STATE_XXX constants.
     */
    int state = STATE_PRE_INIT;

    /**
     * Controls whether the element is in this rare state
     * where it is playing but its last simple duration 
     * is finished. In that state, the element is 'playing',
     * but acting as if frozen.
     *
     * There are situations where the min attribute may cause the 
     * active duration to go beyond the time of the last simple
     * duration instance. In that case, the element is still
     * in the playing state, but we mark it as 'playFill', which
     * means that it should behave almost as if in the fill state.
     */
    boolean playFill = false;

    /**
     * The current iteration (only used while playing
     */
    int curIter = 0;

    /**
     * This element's simple duration. This corresponds to the 'dur'
     * attribute. Note that this is only one of the parameters that
     * defines the actual simple duration.
     *
     * @see #simpleDur
     * @see #computeSimpleDuration
     */
    Time dur = Time.INDEFINITE;

    /**
     * The number of times this element repeats the simple duration.
     * NaN means unspecified.
     */
    float repeatCount = Float.NaN;

    /**
     * repeatDur set a limit on active duration length.
     */
    Time repeatDur = null;

    /**
     * This element's implicit duration
     */
    Time implicitDuration = Time.UNRESOLVED;

    /**
     * min sets a lower limit on the active duration.
     */
    Time min = new Time(0);

    /**
     * max sets an upper limit on the active duration
     */
    Time max = Time.INDEFINITE;

    /**
     * Defines the restart behavior for this element. This should be
     * one of RESTART_ALWAYS, RESTART_WHEN_NOT_ACTIVE, RESTART_NEVER.
     */
    int restart = RESTART_ALWAYS;

    /**
     * Defines the behavior after this element is no longer active.
     * If FILL_BEHAVIOR_FILL, it means the element will keep sampling at the 
     * last simple duration time. If FILL_BEHAVIOR_REMOVE (the default),
     * the effect of the animation is removed when the element is 
     * inactive (i.e., when it no longer has a current interval.
     */
    int fillBehavior = FILL_BEHAVIOR_REMOVE;

    /**
     * A reference to this element's time container. This is initialized
     * anytime the parent is set. This element's current time is the 
     * container's simple time.
     * 
     * @see #setParent
     */
    protected TimeContainerSupport timeContainer;

    /**
     * Keeps a reference to the current <code>TimeInterval</code>
     */
    TimeInterval currentInterval;

    /**
     * Last local time (i.e., within the simple duration) this element
     * was sampled at.
     */
    long lastSampleTime = -1;

    /**
     * The simple duration for the current interval
     * @see #computeSimpleDuration
     */
    Time simpleDur = Time.UNRESOLVED;

    /**
     * Keeps a reference to the interval preceding the current one.
     */
    TimeInterval previousInterval;

    /**
     * Keeps are reference to the last interval that was actually
     * run.
     */
    
    /**
     * List of begin <code>TimeInstance</code>s.
     */
    Vector beginInstances = new Vector(1);

    /**
     * List of end <code>TimeInstance</code>s.
     */
    Vector endInstances = new Vector(1);

    /**
     * Begin <code>TimeCondition</code>s. Should _never_ be null.
     */
    Vector beginConditions = new Vector(1);

    /**
     * End <code>TimeCondition</code>s. Should _never_ be null.
     */
    Vector endConditions = new Vector(1);

    /**
     * List of <code>SyncBaseCondition</code>s depedent on this
     * element's begin condition.
     */
    Vector beginDependents;

    /**
     * List of <code>SyncBaseCondition</code>s dependent on this 
     * element's end condition.
     */
    Vector endDependents;

    /**
     * The associated animation element.
     */
    ModelNode animationElement;

    /**
     * Time dependency cycles detector. This flag is set to true
     * when a timing update starts. There should only be one at
     * a time, and it should always be set back to true after
     * and timing update is complete.
     */
    boolean timingUpdate = false;

    /**
     * The seeking flag is used when seeking to a particular time
     * to prevent generation of beginEvent, endEvent and repeat
     * event.
     */
    boolean seeking = false;

    /**
     * Sets the repeat duration. 
     *
     * @param repeatDur the new repeat duration.
     * @throws IllegalStateException if the element is not in the
     *         STATE_PRE_INIT state.
     */
    public void setRepeatDur(final Time repeatDur) {
        checkPreInit();
        this.repeatDur = repeatDur;
    }

    /**
     * Sets the repeat count.
     *
     * @param repeatCount the new repeat count. Must be greater than
     *        zero.
     * @throws IllegalStateException if the element is not in the
     *         STATE_PRE_INIT state.
     */
    public void setRepeatCount(final float repeatCount) {
        checkPreInit();

        if (repeatCount <= 0) {
            throw new IllegalArgumentException();
        }

        this.repeatCount = repeatCount;
    }

    /**
     * Sets the restart strategy.
     *
     * @param restart the new restart strategy, one of
     *         RESTART_ALWAYS, RESTART_NEVER or RESTART_WHEN_NOT_ACTIVE.
     * @throws IllegalStateException if the element is not in the
     *         STATE_PRE_INIT state.
     */
    public void setRestart(final int restart) {
        checkPreInit();
        switch (restart) {
        case RESTART_ALWAYS:
        case RESTART_NEVER:
        case RESTART_WHEN_NOT_ACTIVE:
            this.restart = restart;
            break;
        default:
            throw new IllegalArgumentException();
        }
    }

    /**
     * Sets the maximun active duration for any timed element
     * interval.
     *
     * @param max the new maximum active duration. Should not be
     *        null.
     * @throws IllegalStateException if the element is not in the
     *         STATE_PRE_INIT state.
     */
    public void setMax(final Time max) {
        checkPreInit();
        if (max == null) {
            throw new IllegalArgumentException();
        }
        this.max = max;
    }

    /**
     * Sets the minimum active duration for any timed element interval.
     *
     * @param min the new minimum active duration. Should not be null.
     * @throws IllegalStateException if the element is not in the
     *         STATE_PRE_INIT state.
     */
    public void setMin(final Time min) {
        checkPreInit();
        if (min == null) {
            throw new IllegalArgumentException();
        }
        this.min = min;
    }

    /**
     * Sets the duration for this timed element
     *
     * @param dur the new element duration.
     * @throws IllegalStateException if the element is not in the
     *         STATE_PRE_INIT state.
     */
    public void setDur(final Time dur) {
        checkPreInit();
        this.dur = dur;
    }

    /**
     * Sets the fill state behavior.
     *
     * @param fillBehavior the new fill strategy, one of FILL_BEHAVIOR_FREEZE or
     *        FILL_BEHAVIOR_REMOVE
     * @throws IllegalStateException if the element is not in the 
     *         STATE_PRE_INIT state.
     */
    public void setFillBehavior(final int fillBehavior) {
        checkPreInit();
        switch (fillBehavior) {
        case FILL_BEHAVIOR_FREEZE:
        case FILL_BEHAVIOR_REMOVE:
            this.fillBehavior = fillBehavior;
            break;
        default:
            throw new IllegalArgumentException();
        }
    }

    /**
     * Implementation helper. Throws an exception if the element is
     * not in the STATE_PRE_INIT state.
     *
     * @throws IllegalStateException if the element is not in the
     *         STATE_PRE_INIT state.
     */
    public void checkPreInit() {
        if (state != STATE_PRE_INIT) {
            throw new IllegalStateException();
        }
    }

    /**
     * Calling this method causes a new <code>TimeInstance</code> to
     * be inserted in the begin instance list. The behavior depends
     * on the restart setting.
     */
    public void begin() {
        beginAt(0);
    }

    /**
     * Calling this method causes a new <code>TimeInstance</code> to
     * be inserted in the end instance list. This typically causes
     * the current interval (if any), to be ended.
     */
    public void end() {
        endAt(0);
    }

    /**
     * Calling this method causes a new <code>TimeInstance</code> to
     * be inserted in the begin instance list. The behavior depends
     * on the restart setting.
     *
     * @param offset the begin time inserted into the instance list
     *        is offset by 'offset' milliseconds from the current
     *        time.
     */
    public void beginAt(final long offset) {
        addInstance(true, offset);
    }
   
    /**
     * Calling this method causes a new <code>TimeInstance</code> to
     * be inserted in the end instance list. This typically causes
     * the current interval (if any), to be ended.
     *
     * @param offset the end time inserted into the instance list
     *        is offset by 'offset' milliseconds from the current
     *        time.
     */
    public void endAt(final long offset) {
        addInstance(false, offset);
    }

    /**
     * Adds a new <code>TimedInstance</code> with the specified offset
     * to the begin or end instance list (depending on <code>isBegin</code>
     *
     * @param isBegin if true, the instance is a begin instance
     * @param offset the instance offset from 0.
     */
    void addInstance(final boolean isBegin,
                     final long offset) {
        Time currentTime = getCurrentTime();
        if (!currentTime.isResolved()) {
            // If the current time is not resolved, it means the time
            // container is not active. Therefore, there is no way
            // we can add a new time instance that would be meaningful. 
            // So we do nothing.
            return;
        }

        Time newTime = new Time(currentTime.value + offset);

        TimeInstance newInstance 
            = new TimeInstance(this, newTime, true, isBegin);
    }

    /**
     * Sets this timed element's time container
     *
     * @param timeContainer time container
     */
    protected void setTimeContainer(final TimeContainerSupport timeContainer) {
        if (this.timeContainer != null) {
            this.timeContainer.timedElementChildren.removeElement(this);
        }

        this.timeContainer = timeContainer;
        if (timeContainer != null) {
            timeContainer.timedElementChildren.addElement(this);
            if (timeContainer.state != STATE_PRE_INIT) {
                // This is a live addition of an animation to a container.
                // We need to initialize this timed element support.
                initialize();
            }
        }
    }

    /**
     * @return this TimedElement's container.
     */
    protected TimeContainerSupport getTimeContainer() {
        return timeContainer;
    }

    /**
     * This method is called by the element's time container when it
     * resets, i.e., when the container starts it's simple time again.
     *
     * This has the effect of:
     * 1. clearing all the instance times which have the clearOnReset
     *    flag set to true.
     * 2. compute the first interval.
     */
    protected void initialize() {
        // Clear instances with clearOnReset == true
        reset();

        // Compute the first interval
        if (!checkNewInterval(computeFirstInterval())) {
            state = STATE_NO_INTERVAL;
        }
    }

    /**
     * Calls all the registered <code>TimeDependent</code>s so that they
     * are notified of a new TimeInterval creation.
     */
    void dispatchOnNewInterval() {
        int n = beginDependents == null ? 0 : beginDependents.size();
        for (int i = 0; i < n; i++) {
            ((TimeDependent) beginDependents.elementAt(i)).onNewInterval(this);
        }

        n = endDependents == null ? 0 : endDependents.size();
        for (int i = 0; i < n; i++) {
            ((TimeDependent) endDependents.elementAt(i)).onNewInterval(this);
        }
    }

    /**
     * @param after we are looking for a time greater than or equal
     *        to after.
     * @param instances the Vector containing the TimeInstances to
     *        look up.
     * @return the first value in the given instance list that
     *         starts after the input time.
     */
    Time getTimeAfter(final Time after,
                      final Vector instances) {
        int n = instances.size();
        
        // NOTE: The following _assumes_ that the instances vector
        //       is sorted in increasing time order.
        for (int i = 0; i < n; i++) {
            TimeInstance timeInstance = (TimeInstance) instances.elementAt(i);
            if (timeInstance.time.greaterThan(after)) {
                return timeInstance.time;
            }
        }
        return null;
    }

    /**
     * @param after we are looking for a time greater than or equal
     *        to after.
     * @param instances the Vector containing the TimeInstances to
     *        look up.
     * @return the first value in the given instance list that
     *         starts strictly after the input time.
     */
    Time getTimeAfterStrict(final Time after,
                            final Vector instances) {
        int n = instances.size();
        
        // NOTE: The following _assumes_ that the instances vector
        //       is sorted in increasing time order.
        for (int i = 0; i < n; i++) {
            TimeInstance timeInstance = (TimeInstance) instances.elementAt(i);
            if (!after.greaterThan(timeInstance.time)) {
                return timeInstance.time;
            }
        }
        return null;
    }

    /**
     * Computes the active end of this <code>TimedElement</code> for
     * the given begin and end time constraints. This accounts for 
     * the other attributes which control or constrain the 
     * active duration.
     *
     * @param begin the input begin time. Should not be unresolved or null.
     * @param end the proposed end time. May be null.
     * @return the computed, constrained end time.
     *
     * @see <a
     * href="http://www.w3.org/TR/smil20/smil-timing.html#Timing-ComputingActiveDur">
     * SMIL 2: Computing the active duration</a>
     */
    Time calculateActiveEnd(final Time begin, final Time end) {
        Time result = end;
        Time pad = null;

        if (begin == null) {
            throw new NullPointerException();
        }
        
        if (!begin.isResolved()) {
            throw new IllegalArgumentException();
        }

        //
        // In the context of this method, end and begin are the time instances
        // passed to this method, and _not_ the begin and end attributes.
        //
        // If endTime is not null, and none of dur, repeatDur, and repeatCount
        // are specified, then the simple duration is indefinite from the simple
        // duration table above (see SMIL 2 spec), and the active duration is
        // defined by the end value, according to the following cases:
        //
        // If end is resolved to a value, then pad = end - B,
        // 
        // else, if end is indefinite, then pad = indefinite,
        // 
        // else, if end is unresolved, then pad is unresolved, and needs to be
        // recomputed when more information becomes available.
        //
        if (end != null 
            && dur == null 
            && repeatDur == null 
            && Float.isNaN(repeatCount)) {
            if (end.isResolved()) {
                pad = new Time(end.value - begin.value);
            } else if (end == Time.INDEFINITE) {
                pad = Time.INDEFINITE;
            } else {
                pad = Time.UNRESOLVED;
            }
        }


        // Else, if no end value is specified, or the end value is specified as
        // indefinite, then the active duration is determined from the
        // Intermediate Active Duration computation given below:
        // 
        // pad = Result from Intermediate Active Duration Computation  
        else if (end == null || end == Time.INDEFINITE) {
            pad = 
                calculateIntermediateActiveDuration(computeSimpleDuration(end));
        }
        
        // Otherwise, an end value not equal to indefinite is specified along
        // with at least one of dur, repeatDur, and repeatCount. Then the pad is
        // the minimum of the result from the Intermediate Active Duration
        // Computation given below and duration between end and the element
        // begin:
        //
        // pad = MIN( Result from Intermediate Active Duration Computation, 
        //            end - B)
        else {
            pad = 
                calculateIntermediateActiveDuration(computeSimpleDuration(end));
            Time pad2 = Time.UNRESOLVED;
            if (end.isResolved()) {
                pad2 = new Time(end.value - begin.value);
            }
            if (pad.greaterThan(pad2)) {
                pad = pad2;
            }
        }

        // Finally, the computed active duration ad is obtained by applying min
        // and max semantics to the preliminary active duration pad. In the
        // following expression, if there is no min value, substitute a value of
        // 0, and if there is no max value, substitute a value of "indefinite":
        // 
        // ad = MIN( max, MAX( min, pad )) 

        // Only do min/max constraint if min < max
        boolean doMinMax = true;
        if (min != null && max != null && !max.greaterThan(min)) {
            doMinMax = false;
        }

        Time ad = null;
        if (pad.isResolved()) {
            ad = new Time(pad.value);
        } else {
            ad = pad;
        }
            
        if (doMinMax) {
            // MAX( min, pad )
            if (min != null && min.greaterThan(ad)) {
                if (min.isResolved()) {
                    ad.value = min.value;
                } else {
                    ad = min;
                }
            }
            
            // MIN( max, MAX( min, pad ))
            if (max != null && !max.greaterThan(ad)) {
                if (max == Time.INDEFINITE) {
                    ad = Time.INDEFINITE;
                } else {
                    if (ad.isResolved()) {
                        ad.value = max.value;
                    } else {
                        ad = new Time(max.value);
                    }
                }
            }
        }

        // We have computed the active duration. Now, offset that
        // duration with the begin time to yield the computed end time.
        if (ad.isResolved()) {
            ad.value += begin.value;
        }

        // Finally, apply the restart behavior. If restart is always
        // we check if there is any end time that is before active
        // end time.
        if (restart == RESTART_ALWAYS) {
            Time restartBegin = getTimeAfterStrict(begin, beginInstances);
            if (restartBegin != null && ad.greaterThan(restartBegin)) {
                ad.value = restartBegin.value;
            }
        }

        return ad;
    }

    /**
     * Intermediate Active Duration Computation, as defined in the 
     * SMIL 2 specifications. 
     *
     * @param p0 the element's simple duration. Should not be null
     * @return the intermediate active duration.
     * @see <a href="http://www.w3.org/TR/smil20/smil-timing.html#q81">
     *                  Intermediate Active Duration Computation</a>
     */
    final Time calculateIntermediateActiveDuration(final Time p0) {
        if (p0 == null) {
            throw new NullPointerException();
        }

        if (p0.isResolved() && p0.value == 0) {
            return new Time(0);
        } else if (repeatDur == null && Float.isNaN(repeatCount)) {
            return p0;
        } else {
            Time p1 = Time.INDEFINITE;
            Time p2 = repeatDur;
            Time iad = Time.UNRESOLVED;
            
            if (!Float.isNaN(repeatCount)) {
                if (p0.isResolved()) {
                    if (repeatCount == Float.MAX_VALUE) {
                        p1 = Time.INDEFINITE;
                    } else {
                        p1 = new Time((long) (p0.value * repeatCount));
                    }
                } else {
                    p1 = p0; // INDEFINITE or UNRESOLVED
                }
            }
            
            if (p2 == null) {
                p2 = Time.INDEFINITE;
            }
            
            iad = Time.INDEFINITE;
            if (!p2.greaterThan(iad)) {
                iad = p2;
            }
            if (!p1.greaterThan(iad)) {
                iad = p1;
            }
            return iad;
        }
    }

    /**
     * Computes the element's simple duration, as defined in the 
     * SMIL 2 specification except that the 'media' value is
     * not supported.
     *
     * For the purpose of computing the simple duration, an unspecified
     * dur or an UNRESOLVED dur yield the same computed simple duration.
     *
     * @param end the interval end time. Can be null or have any Time
     *        value.
     * @return the computed simple duration.
     * @see <a
     * href="http://www.w3.org/TR/smil20/smil-timing.html#Timing-DefiningSimpleDur">
     * Defining the simple duration</a>
     */
    final Time computeSimpleDuration(final Time end) {
        Time implicitDur = getImplicitElementDuration();

        //    dur          | implicit    | repeatDur and  | simpleDuration
        //                 | duration    | repeatCount    |
        //    -------------+-------------+----------------+--------------------
        // 1. unspecified  | ignored     | unspecified,   | indefinite
        //                 |             | end specified  |
        // 2. Clock-value  | ignored     | ignored        | dur or Clock-value
        // 3. indefinite   | ignored     | ignored        | indefinite
        // 4. unspecified  | resolved    | ignored        | implicit duration
        // 5. unspecified  | unresolved  | ignored        | unresolved
        //    -------------+-------------+----------------+--------------------
        
        // First line case
        if (dur == null && repeatDur == null
            && Float.isNaN(repeatCount) && end != Time.UNRESOLVED) {
            return Time.INDEFINITE;
        }

        // Second & Third line case
        if (dur != null && dur != Time.UNRESOLVED) {
            return dur;
        }

        // Fourth line case. If we got to this point, we know 
        // dur is UNRESOLVED or null
        if (implicitDur != Time.UNRESOLVED) {
            return implicitDur;
        } else {
            // Fifth line
            return Time.UNRESOLVED;
        }
    }

    /**
     * @return this element's implicit duration. Can be used for media such
     *         as audio and video where there is a natural, implicit duration.
     */
    protected Time getImplicitElementDuration() {
        return implicitDuration;
    }

    /**
     * @return true if there is at least one event end condition.
     */
    boolean endHasEventConditions() {
        for (int i = 0; i < endConditions.size(); i++) {
            if (endConditions.elementAt(i) instanceof EventBaseCondition) {
                return true;
            }
        }
        return false;
    }

    /**
     * Computes the time of the last simple duration instance
     * for the given time interval.
     *
     * @param ti the TimeInterval instance for which the time of the last
     *        simple duration instance should be computed.
     *
     * @return the input TimeInterval instance.
     */
    TimeInterval computeLastDur(final TimeInterval ti) {
        Time simpleDur = computeSimpleDuration(ti.end);
        if (simpleDur != null) {
            Time iad = calculateIntermediateActiveDuration(simpleDur);
            if (iad.isResolved()) {
                ti.lastDur = new Time(ti.begin.value + iad.value);
                if (ti.lastDur.greaterThan(ti.end)) {
                    ti.lastDur = ti.end;
                }
            } else {
                ti.lastDur = ti.end;
            }
        } else {
            ti.lastDur = ti.end;
        }

        return ti;
    }

    /**
     * The following computes the element's first interval, as defined
     * in the SMIL specification.
     *
     * @return the first time interval
     * @see <a
     * href="http://www.w3.org/TR/smil20/smil-timing.html#Timing-BeginEnd-LC-Start">
     * STARTUP - Getting the first interval</a>
     */
    TimeInterval computeFirstInterval() {
        Time beginAfter = new Time(Long.MIN_VALUE);
        Time parentSimpleEnd = getContainerSimpleDuration();
        
        while (true) { // loop till return
            Time tempBegin = getTimeAfter(beginAfter, beginInstances);
            if (tempBegin == null) {
                return null; // No interval
            }

            if (tempBegin.greaterThan(parentSimpleEnd)) {
                return null; // No interval
            }

            Time tempEnd = null;
            if (endConditions.size() == 0) {
                tempEnd = calculateActiveEnd(tempBegin, null);
            } else {
                tempEnd = getTimeAfter(tempBegin, endInstances);
                if (tempBegin.isSameTime(tempEnd)) {
                    tempEnd = getTimeAfterStrict(tempEnd, endInstances);
                }

                if (tempEnd == null) {
                    if (endHasEventConditions() || endInstances.size() == 0) {
                        tempEnd = Time.UNRESOLVED;
                    } else {
                        return null; // No interval
                    }
                }

                tempEnd = calculateActiveEnd(tempBegin, tempEnd);
            }

            // We have an end - is it after the parent simple begin?
            if (!tempEnd.isResolved() || tempEnd.value > 0) {
                return computeLastDur(new TimeInterval(tempBegin, tempEnd));
            } else {
                beginAfter = tempEnd;
            }
        }
    }

    /**
     * The following computes the element's next interval, as defined
     * in the SMIL specification.
     *
     * @return the next interval given the current begin and end time instances.
     * @see <a
     * href="http://www.w3.org/TR/smil20/smil-timing.html#Timing-BeginEnd-LC-End">
     * End of an interval</a>
     */
    TimeInterval computeNextInterval() {
        Time curEnd = previousInterval.end;
        Time beginAfter = curEnd;
        Time parentSimpleEnd = getContainerSimpleDuration();
        Time tempBegin = getTimeAfter(beginAfter, beginInstances);

        if (tempBegin == null) {
            return null;
        }
        
        if (tempBegin.greaterThan(parentSimpleEnd)) {
            return null;
        }

        Time tempEnd = null;
        if (endConditions.size() == 0) {
            tempEnd = calculateActiveEnd(tempBegin, null);
            if (endInstances.size() > 0) {
                // There is no end-attribute specified, but there are time 
                // instances. The SMIL Animation specification says that if
                // there are times instances from DOM Events or DOM 
                // beginElementAt calls, they short cut the active duration. 
                // 
                // See: http://www.w3.org/TR/2001/REC-smil-animation-20010904/
                // section 3.3.4.
                //
                Time tempEnd2 = getTimeAfter(tempBegin, endInstances);
                if (tempEnd2 != null && tempEnd2.isResolved()) {
                    tempEnd = tempEnd2;
                }
            }
        } else {
            // We have a begin value - get an end
            tempEnd = getTimeAfter(tempBegin, endInstances);

            if (curEnd.isSameTime(tempEnd)) {
                tempEnd = getTimeAfterStrict(tempEnd, endInstances);
            }

            if (tempEnd == null) {
                if (endHasEventConditions() || endInstances.size() == 0) {
                    tempEnd = Time.UNRESOLVED;
                } else {
                    return null;
                }
            }

            tempEnd = calculateActiveEnd(tempBegin, tempEnd);
        }

        return computeLastDur(new TimeInterval(tempBegin, tempEnd));
    }

    /**
     * Computes the end time corresponding to the input begin time.
     *
     * @param tempBegin the begin time for which to search an end time.
     * @return the computed end time or null if there is no matching end.
     */
    Time computeEndTime(final Time tempBegin) {
        if (endInstances.size() == 0) {
            return calculateActiveEnd(tempBegin, null);
        } else {
            Time tempEnd = getTimeAfter(tempBegin, endInstances);

            if (tempBegin.isSameTime(tempEnd)) {
                // IMPL NOTE : Do this to get a strictly superior end time
                tempEnd = new Time(tempEnd.value + 1);
                tempEnd = getTimeAfter(tempEnd, endInstances);
            }

            if (tempEnd == null) {
                if (endHasEventConditions() || endInstances.size() == 0) {
                    tempEnd = Time.UNRESOLVED;
                } else {
                    return null;
                }
            }

            return calculateActiveEnd(tempBegin, tempEnd);
        }
    }

    /**
     * Resetting the element clears all of its 'clearOnReset' instances from the
     * begin and end list. This includes event based time instances, time
     * instances resulting from begin() or end() calls and resolved
     * IntervalTimeInstance.
     */
    void reset() {
        currentInterval = null;
        simpleDur = Time.UNRESOLVED;
        previousInterval = null;
        curIter = 0;
        state = STATE_PRE_INIT;
        lastSampleTime = -1;

        int n = beginInstances.size();
        for (int i = n - 1; i >= 0; i--) {
            TimeInstance timeInstance 
                = (TimeInstance) beginInstances.elementAt(i);
            if (timeInstance.clearOnReset) {
                beginInstances.removeElementAt(i);
            } else if (timeInstance instanceof IntervalTimeInstance) {
                ((IntervalTimeInstance) timeInstance).syncTime();
            } 
        }
        n = endInstances.size();
        for (int i = n - 1; i >= 0; i--) {
            TimeInstance timeInstance 
                = (TimeInstance) endInstances.elementAt(i);
            if (timeInstance.clearOnReset) {
                endInstances.removeElementAt(i);
            } else if (timeInstance instanceof IntervalTimeInstance) {
                ((IntervalTimeInstance) timeInstance).syncTime();
            } 
        }
    }

    /**
     * Removes all <code>IntervalTimeInstance</code>s in the begin and end
     * instance list if the syncBase is a descendant of syncTimeContainer
     *
     * @param syncTimeContainer the container under which
     *        <code>IntervalTimeInstance</code> should be removed.
     */
    void removeSyncBaseTimesUnder
        (final TimeContainerSupport syncTimeContainer) {
        int n = beginInstances.size();
        for (int i = n - 1; i >= 0; i--) {
            TimeInstance timeInstance 
                = (TimeInstance) beginInstances.elementAt(i);
            if (timeInstance instanceof IntervalTimeInstance) {
                IntervalTimeInstance iti = (IntervalTimeInstance) timeInstance;
                if (iti.syncBase.isDescendant(syncTimeContainer)) {
                    beginInstances.removeElementAt(i);
                    iti.dispose();
                }
            } 
        }
        n = endInstances.size();
        for (int i = n - 1; i >= 0; i--) {
            TimeInstance timeInstance = 
                    (TimeInstance) endInstances.elementAt(i);
            if (timeInstance instanceof IntervalTimeInstance) {
                IntervalTimeInstance iti = (IntervalTimeInstance) timeInstance;
                if (iti.syncBase.isDescendant(syncTimeContainer)) {
                    endInstances.removeElementAt(i);
                    iti.dispose();
                }
            } 
        }
    }

    /**
     * @param parent the time container which might be on the timed element's
     *        parent hierarchy.
     * @return true if this timed element is equal to 'parent' or is a 
     * descendant of 'parent'
     */
    boolean isDescendant(final TimeContainerSupport parent) {
        if (parent == this) {
            return true;
        } else if (timeContainer != this) {
            return timeContainer.isDescendant(parent);
        } else {
            return false;
        }
    }

    /**
     * Dispatches beginEvent. As per the SMIL 2 specification, this dispatches
     * a beginEvent for the resolved begin time, not the observed begin
     * time. It also dispatches any repeat event that might be needed.
     *
     * @param currentTime the current sampling time.
     *
     * @see <a
     * href="http://www.w3.org/TR/smil20/smil-timing.html#Timing-BeginValueSemantics">
     * SMIL2's Begin value semantics</a>
     */
    void dispatchBeginEvent(final Time currentTime) {
        if (animationElement != null) {
            ModelEvent beginEvent = null;
            if (seeking) {
                beginEvent = animationElement.ownerDocument.initEngineEvent
                    (SEEK_BEGIN_EVENT_TYPE, animationElement);
            } else {
                beginEvent = animationElement.ownerDocument.initEngineEvent
                    (BEGIN_EVENT_TYPE, animationElement);
            }

            eventTime.value = currentInterval.begin.value;
            beginEvent.eventTime = toRootContainerSimpleTimeClamp(eventTime);
            animationElement.dispatchEvent(beginEvent);
        }

        dispatchRepeatEvent(currentTime);
    }

    /**
     * Dispatches lastDurEndEvent.
     *
     * @see <a
     * href="http://www.w3.org/TR/smil20/smil-timing.html#smil-timing-Timing-BeginEnd-Restart
     * </a>
     */
    void dispatchLastDurEndEvent() {
        if (animationElement != null) {
            ModelEvent event = animationElement.ownerDocument.initEngineEvent
                (LAST_DUR_END_EVENT_TYPE, animationElement);
            eventTime.value = currentInterval.lastDur.value;
            event.eventTime = toRootContainerSimpleTimeClamp(eventTime);
            animationElement.dispatchEvent(event);
        }
    }

    /**
     * Dispatches seekeEndEvent.
     *
     * @see <a
     * href="http://www.w3.org/TR/smil20/smil-timing.html#smil-timing-Timing-BeginEnd-Restart
     * </a>
     */
    void dispatchSeekEndEvent() {
        if (animationElement != null) {
            ModelEvent event = animationElement.ownerDocument.initEngineEvent
                (SEEK_END_EVENT_TYPE, animationElement);
            animationElement.dispatchEvent(event);
        }
    }

    /**
     * Dispatches endEvent. As per the SMIL 2 specification, this dispatches an
     * endEvent for the resolved end time, not the observed end time.
     *
     * @param currentTime the current sampling time.
     *
     * @see <a
     * href="http://www.w3.org/TR/smil20/smil-timing.html#Timing-BeginValueSemantics">
     * SMIL2's Begin value semantics</a>
     */
    void dispatchEndEvent(final Time currentTime) {
        if ((!seeking || state == STATE_PLAYING)
            && animationElement != null) {
            ModelEvent endEvent = animationElement.ownerDocument.initEngineEvent
                (END_EVENT_TYPE, animationElement);
            if (seeking) {
                // We are seeking to a new currentTime and the interval
                // ends during the seek interval and the element was
                // active at the time of seek. The time for the end
                // event should be the time _before_ the seek
                eventTime.value = getRootContainer().lastSampleTime.value;
                endEvent.eventTime = eventTime;
            } else {
                eventTime.value = currentInterval.end.value;
                endEvent.eventTime = toRootContainerSimpleTimeClamp(eventTime);
            }
            
            animationElement.dispatchEvent(endEvent);
        }
        dispatchRepeatEvent(currentTime);
    }

    /**
     * Helper method. 
     *
     * @return this timed element's root container.
     */
    TimeContainerRootSupport getRootContainer() {
        return timeContainer.getRootContainer();
    }

    /**
     * Dispatches repeatEvent. As per the SMIL 2 specification, this dispatches
     * a repeatEvent for the resolved end time, not the observed repeat
     * time(s).
     *
     * @see <a
     * href="http://www.w3.org/TR/smil20/smil-timing.html#Timing-BeginValueSemantics">
     * SMIL2's Begin value semantics</a>
     */
    void dispatchRepeatEvent(final Time currentTime) {
        if (!seeking && simpleDur.isResolved() 
            && simpleDur.value > 0) {
            int prevIter = curIter;

            Time maxTime = currentTime;
            if (currentTime.greaterThan(currentInterval.end)) {
                maxTime = currentInterval.end;
            }

            curIter = (int) ((maxTime.value - currentInterval.begin.value) 
                             / 
                             simpleDur.value);

            if (curIter < 0) {
                curIter = 0;
            }

            if (maxTime.isSameTime(currentInterval.end)
                && 
                ((maxTime.value - currentInterval.begin.value) 
                 % 
                 simpleDur.value 
                 == 0)) {
                curIter--;
            }

            if (animationElement != null) {
                for (int i = prevIter; i < curIter; i++) {
                    ModelEvent repeatEvent 
                        = animationElement.ownerDocument.initEngineEvent
                        (REPEAT_EVENT_TYPE, animationElement);
                    
                    // The event time is the time at which the repeat happened,
                    // not the time at which it was detected (that would be
                    // currentTime).  So we have to compute the repeat time here
                    Time repeatTime = eventTime;
                    repeatTime.value = currentInterval.begin.value
                        + (i + 1) * simpleDur.value;
                    repeatTime = toRootContainerSimpleTimeClamp(repeatTime);
                    repeatEvent.eventTime = repeatTime;
                    repeatEvent.repeatCount = i + 1;
                    animationElement.dispatchEvent(repeatEvent);
                }
            }

            if (prevIter != curIter) {
                onStartingRepeat(prevIter, curIter);
            }
        }
    }

    /**
     * To be overridden by extensions (TimeContainerSupport) to do specific
     * processing when a new iteration is started.
     *
     * @param prevIter the last iteration this element was playing.
     * @param curIter the new iteration this element is playing.
     */
    protected void onStartingRepeat(final int prevIter, final int curIter) {
    }

    /**
     * Checks if the input interval is non null. If it is, that interval
     * becomes the new currentInterval and the element sets its state 
     * accordingly.
     *
     * @param interval the interval to check.
     * @return true if the candidate interval has become the current interval.
     */
    boolean checkNewInterval(final TimeInterval interval) {
        if (interval == null) {
            return false;
        } else {
            currentInterval = interval;
            simpleDur = computeSimpleDuration(currentInterval.end);

            // Transition to the current state
            // Note that we move to the 'playing' stage only in the
            // sample method, where event dispatching is done as well.
            if (previousInterval == null) {
                state = STATE_WAITING_INTERVAL_0;
            } else {
                state = STATE_WAITING_INTERVAL_N;
            }

            // Notify dependents of the new interval
            dispatchOnNewInterval();
            return true;
        }
    }

    /**
     * Helper method to find the active interval, or next interval, for
     * the requested time.
     *
     * @param seekToTime the time we are seeking the active or next interval
     *        for.
     * @return the <code>TimeInterval</code> corresponding to
     *         <code>seekToTime</code>
     */
    TimeInterval seekToInterval(final Time seekToTime) {
        previousInterval = null;
        TimeInterval interval = computeFirstInterval();
        if (interval == null) {
            return null;
        }

        while (interval != null && seekToTime.greaterThan(interval.end)) {
            previousInterval = interval;
            interval = computeNextInterval();
        }

        return interval;
    }

    /**
     * Helper method.
     *
     * @return true if the root time container is flagged as seeking back.
     */
    boolean isSeekingBack() {
        return getRootContainer().seekingBack;
    }

    /**
     * This method is typically called by this element's time container
     * when it samples.
     *
     * Note that if this element is not in the waiting or playing
     * state, this does nothing. This method assumes that successive
     * calls are made with increasing time values.
     *
     * @param currentTime the time at which this element should be 
     *        sampled. 
     */
    void sample(final Time currentTime) {
        // Handle state transitions if there is a current interval
        boolean endOfInterval = false;
        boolean seekBack = false;

        switch (state) {
        default:
            return;
        case STATE_FILL:
            if (seeking && isSeekingBack()) {
                seekBack = true;
            }
            break;
        case STATE_WAITING_INTERVAL_0:
            if (currentTime.greaterThan(currentInterval.begin)) {
                // This element has begun between the previous sampling
                // and now. Dispatch the beginEvent.
                dispatchBeginEvent(currentTime);
                
                if (currentTime.greaterThan(currentInterval.end)) {
                    // This element has also ended between the 
                    // previous sampling and now. Dispatch the endEvent
                    dispatchLastDurEndEvent();
                    dispatchEndEvent(currentTime);
                    endOfInterval = true;
                }

                state = STATE_PLAYING;
                playFill = false;
            }
            break;
        case STATE_WAITING_INTERVAL_N:
            if (currentTime.greaterThan(currentInterval.begin)) {
                // This element has begun between the previous sampling
                // and now. Dispatch the beginEvent.
                dispatchBeginEvent(currentTime);
                
                if (currentTime.greaterThan(currentInterval.end)) {
                    // This element has also ended between the 
                    // previous sampling and now. Dispatch the endEvent
                    dispatchLastDurEndEvent();
                    dispatchEndEvent(currentTime);
                    endOfInterval = true;
                }

                state = STATE_PLAYING;
                playFill = false;
            } else {
                if (seeking && isSeekingBack()) {
                    seekBack = true;
                }
            }
            break;
        case STATE_PLAYING:
            if (currentTime.greaterThan(currentInterval.lastDur)) {
                if (!playFill) {
                    dispatchLastDurEndEvent();
                    playFill = true;
                }
            }

            if (currentTime.greaterThan(currentInterval.end)) {
                // This element has also ended between the 
                // previous sampling and now. Dispatch the endEvent
                dispatchEndEvent(currentTime);
                endOfInterval = true;
            } else {
                if (!seeking) {
                    dispatchRepeatEvent(currentTime);
                } else if (currentInterval.begin.greaterThan(currentTime)) {
                    // We are seeking backwards and the current interval
                    // began during the seek interval. We need to 
                    // dispatch and end event at the time before the 
                    // seek.
                    dispatchLastDurEndEvent();
                    dispatchEndEvent(currentTime);
                    endOfInterval = true;
                    seekBack = true;
                }
            }
            break;
        }

        // If we just finished an interval,
        // we need to compute the new interval now
        if (endOfInterval) {
            previousInterval = currentInterval;
            currentInterval = null;

            // If we cannot restart, we just go to the fill state
            if (restart == RESTART_NEVER) {
                state = STATE_FILL;
                playFill = false;
            } else {
                TimeInterval nextInterval = null;
                if (!seekBack) {
                    nextInterval = computeNextInterval();
                } else {
                    nextInterval = seekToInterval(currentTime);
                }
                
                if (!checkNewInterval(nextInterval)) {
                    // There is no next interval, we move to the 
                    // fill state.
                    state = STATE_FILL;
                    playFill = false;
                }
            }
             
            // This recursive call is needed in case we sample so far ahead 
            // that we are skipping several intervals.
            // For example if the intervals are:
            // [0, 1000[
            // [2000, 3000[
            // [5000, 6500[
            // [8000, 1000[
            // and we sample at 0 and then 7000
            sample(currentTime);
            return;
        } else if (seekBack) {
            // If we are in the FILL state and we cannot restart, then we stay
            // in FILL state if seeking to after the previous interval's
            // end. Otherwise, we move to the STATE_NO_INTERVAL state.
            if (state == STATE_FILL && restart == RESTART_NEVER) {
                if (previousInterval.end.greaterThan(currentTime)) {
                    dispatchSeekEndEvent();
                    state = STATE_NO_INTERVAL;
                    playFill = false;
                    return;
                } 
            } else {
                TimeInterval seekInterval = seekToInterval(currentTime);
                
                // If the seek interval is the same as the current interval, we
                // do not need to do further sampling because we were in, or
                // have already moved to, the right interval.
                if (seekInterval == null 
                    || currentInterval == null 
                    || !(seekInterval.begin.isSameTime(currentInterval.begin)
                         &&
                         seekInterval.end.isSameTime(currentInterval.end))) {
                    
                    // We need to check if there is an interval active at the
                    // time we are seeking to.
                    if (!checkNewInterval(seekInterval)) {
                        // There is no currentInterval. 
                        // Switch to the right state 
                        if (previousInterval != null) {
                            state = STATE_FILL;
                            playFill = false;
                        } else {
                            dispatchSeekEndEvent();
			    state = STATE_NO_INTERVAL;
                            playFill = false;
                            return;
                        }
                    } else {
                        // There is an interval at the time we are 
                        // seeking to. End the current interval (which
                        // we know is different because of the previous if
                        // statement) and sample again to have the new current
                        // interval kick-in.
                        dispatchSeekEndEvent();
                        sample(currentTime);
                        return;
                    }
                }
            }
        }
        
        // 
        // Different behaviors depending on the state. At this point, we
        // should only be in one of the following states:
        // - STATE_FILL
        // - STATE_PLAYING
        // - STATE_WAITING_INTERVAL_0
        // - STATE_WAITING_INTERVAL_N
        // 
        long localTime = 0;
        switch (state) {
        default:
            // This should _never_ happen, given this method's code.
            // This is extremely hard to test (would require changing
            // state during the execution of this method) and does 
            // not need to be covered by the test suite. 
            throw new IllegalStateException();
        case STATE_WAITING_INTERVAL_N:
        case STATE_FILL:
            if (fillBehavior == FILL_BEHAVIOR_FREEZE) {
                // If we are in STATE_WAITING_INTERVAL_N or STATE_FILL,
                // it means that we had a previous interval. Therefore,
                // previousInterval is guaranteed to be not null.
                localTime = previousInterval.lastDur.value 
                            - previousInterval.begin.value;
            } else {
                return;
            }
            break;
        case STATE_WAITING_INTERVAL_0:
            return;
        case STATE_PLAYING:
            // If we are still in the PLAYING state but we are past the
            // end of the last simple duration end, make sure we sample at
            // that last simple duration time.
            if (!playFill) {
                localTime = currentTime.value - currentInterval.begin.value;
            } else {
                localTime = currentInterval.lastDur.value 
                    - currentInterval.begin.value;
            }
            break;
        }

        // If we get to this point, it means we were able to
        // compute a localTime we need to sample at. We are
        // either playing the animation or we are in a frozen
        // state. Compute the simple time from the localTime.
        if (simpleDur.isResolved()) {
            localTime = localTime % simpleDur.value;
            if (state != STATE_PLAYING || playFill) {
                // If we are not playing, it means we are frozen
                // (either STATE_WAITING_N or STATE_FILL).
                //
                // Make sure we sample on the end of the simple time 
                // if we froze at the end of the interval.
                // 
                // Note that we do not need to test if the value
                // was 0 before %. Because we are _not_ playing, it
                // means we are _not_ on the first value of the 
                // interval, therefore, we never run into the condition
                // where localTime would be 0 before % and therefore,
                // we do not need to test for that condition
                //
                if (localTime == 0) {
                    // Freeze on the last value in the simple duration
                    // interval only when we freeze.
                    localTime = simpleDur.value;
                }
            }
        }

        // At this point, we have computed our simple time.
        // Ready to connect with the animation engine.
        sampleAt(localTime);

        // Keep the last simpleTime this element was sampled at
        lastSampleTime = localTime;
    }

    /**
     * Samples this element at the given simple time.
     * Should be overridden for specific behaviors.
     *
     * @param simpleTime this timed element simple time.
     */
    void sampleAt(final long simpleTime) {
    }

    /**
     * Called when this element is the target of an hyperlink. The 
     * behavior is that defined in the SMIL 2 specification.
     * 
     * @see <a
     * href="http://www.w3.org/TR/smil20/smil-timing.html#Timing-HyperlinksAndTiming">
     * SMIL 2 Specification</a>
     */
    public void activate() {
        // 1. If the target element is active, seek the document time back to
        // the begin time of the current interval for the element.
        if (state == STATE_PLAYING) {
            Time seekToTime = new Time(currentInterval.begin.value);
            seekToTime = toRootContainerSimpleTimeClamp(seekToTime);

            if (seekToTime.isResolved()) {
                seekTo(seekToTime);
                return;
            }
        }

        //
        // 2. Else if the target element begin time is resolved (i.e. there is
        // at least one interval defined for the element), seek the document
        // time (forward or back, as needed) to the begin time of the first
        // interval for the target element. Note that the begin time may be
        // resolved as a result of an earlier hyperlink, DOM or event
        // activation. Once the begin time is resolved (and until the element is
        // reset, e.g. when the parent repeats), hyperlink traversal always
        // seeks. For a discussion of "reset", see Resetting element state. Note
        // also that for an element begin to be resolved, the begin time of all
        // ancestor elements must also be resolved.
        //
        TimeInterval firstInterval = computeFirstInterval();
        if (firstInterval != null) {
            Time seekToTime = new Time(firstInterval.begin.value);

            seekToTime = toRootContainerSimpleTimeClamp(seekToTime);

            if (seekToTime.isResolved()) {
                seekTo(seekToTime);
                return;
            }
        }

        // 3. Else (i.e. there are no defined intervals for the element), the
        // target element begin time must be resolved. This may require seeking
        // and/or resolving ancestor elements as well. This is done by recursing
        // from the target element up to the closest ancestor element that has a
        // resolved begin time (again noting that for an element to have a
        // resolved begin time, all of its ancestors must have resolved begin
        // times). Then, the recursion is "unwound", and for each ancestor in
        // turn (beneath the resolved ancestor) as well as the target element,
        // the following steps are performed:
        //
        //     1. If the element begin time is resolved, seek the document time
        //     (forward or back, as needed) to the begin time of the first
        //     interval for the target element.
        //
        //     2. Else (if the begin time is not resolved), just resolve the
        //     element begin time at the current time on its parent time
        //     container (given the current document position). Disregard the
        //     sync-base or event base of the element, and do not
        //     "back-propagate" any timing logic to resolve the element, but
        //     rather treat it as though it were defined with begin="indefinite"
        //     and just resolve begin time to the current parent time. This
        //     should create an interval and propagate to time dependents.
        //
        seekTo(Time.UNRESOLVED);
    }

    /**
     * Implementation helper method for seekTo behavior.
     *
     * @param seekToTime the time to seek to. 
     */
    void seekTo(final Time seekToTime) {
        if (seekToTime.isResolved()) {
            // Now, move up the container graph.
            timeContainer.seekTo(seekToTime);
        } else {
            // 3. Else (i.e. there are no defined intervals for the element),
            // the target element begin time must be resolved. This may require
            // seeking and/or resolving ancestor elements as well. This is done
            // by recursing from the target element up to the closest ancestor
            // element that has a resolved begin time (again noting that for an
            // element to have a resolved begin time, all of its ancestors must
            // have resolved begin times).
            if (timeContainer.state != STATE_PLAYING) {
                TimeInterval firstContainerInterval 
                    = timeContainer.computeFirstInterval();
                if (firstContainerInterval == null) {
                    timeContainer.seekTo(Time.UNRESOLVED);
                } else {
                    Time parentSeekTo 
                        = new Time(firstContainerInterval.begin.value);
                    parentSeekTo 
                        = timeContainer
                        .toRootContainerSimpleTimeClamp(parentSeekTo);
                    timeContainer.seekTo(parentSeekTo);
                }
            }

            // Then, the recursion is "unwound", and for each ancestor in turn
            // (beneath the resolved ancestor) as well as the target element,
            // the following steps are performed:
            //
            //     1. If the element begin time is resolved, seek the document
            //     time (forward or back, as needed) to the begin time of the
            //     first interval for the target element.
            //
            //     2. Else (if the begin time is not resolved), just resolve the
            //     element begin time at the current time on its parent time
            //     container (given the current document position). Disregard
            //     the sync-base or event base of the element, and do not
            //     "back-propagate" any timing logic to resolve the element, but
            //     rather treat it as though it were defined with
            //     begin="indefinite" and just resolve begin time to the current
            //     parent time. This should create an interval and propagate to
            //     time dependents.
            //
            TimeInterval firstInterval = computeFirstInterval();
            if (firstInterval == null) {
                begin();
                firstInterval = computeFirstInterval();
            }
            

            Time goToTime = new Time(firstInterval.begin.value);
            if (goToTime.value < 0) {
                goToTime.value = 0;
            }

            timeContainer.seekTo(toRootContainerSimpleTimeClamp(goToTime));
        }
    }

    /**
     * Should be called whenever there is a change in the begin time instance 
     * list.
     */
    private void reEvaluateBeginTime() {
        switch (state) {
        case STATE_WAITING_INTERVAL_0:
            {
                TimeInterval curInt = computeFirstInterval();
                if (curInt != null) {
                    currentInterval.setBegin(curInt.begin);
                    currentInterval.setEnd(curInt.end);
                    computeLastDur(currentInterval);
                } else {
                    // There no first interval any more, after
                    // the begin instance has been changed.
                    // We move back to the no interval state
                    pruneCurrentInterval();
                }
            }
            break;
        case STATE_WAITING_INTERVAL_N:
            {
                TimeInterval curInt = computeNextInterval();
                if (curInt != null) {
                    currentInterval.setBegin(curInt.begin);
                    currentInterval.setEnd(curInt.end);
                    computeLastDur(currentInterval);
                } else {
                    pruneCurrentInterval();
                }
            }
            
        default:
            return;
        }
    }
    
    /**
     * Recomputes the end time. This only does something if the 
     * element is playing or waiting to play, because it may update
     * the currentInterval's end time.
     */
    void reEvaluateEndTime() {
        switch (state) {
        case STATE_WAITING_INTERVAL_0:
        case STATE_WAITING_INTERVAL_N:
        case STATE_PLAYING:
            // We are dealing with a new end time and the animation is
            // playing. Re-evaluate the end time and update the 
            // current interval.
            //
            // Note that we cannot simply do a computeNextInterval as this
            // might yield a begin time different from the one on the 
            // playing interval.
            //
            Time newEnd = computeEndTime(currentInterval.begin);

            // It is unclear what should happen here if newEnd is null.
            // This could happen if a list of end times was empty and
            // a SyncBaseCondition, for example, inserts a new end instance
            // that is _before_ the currentInterval's begin time. In that
            // situation, should we just ignore newEnd or should we 
            // prune the currentInterval (if WAITING_0 or WAITING_N) and
            // stop it (if PLAYING)?
            //
            // Pending further investigation of this corner case, we simply
            // ignore a null newEnd
            if (newEnd != null) {
                currentInterval.setEnd(newEnd);
                computeLastDur(currentInterval);
            }
            break;
        default:
            break;
        }
    }

    /**
     * Helper method invoked when the current interval becomes invalid
     * after re-evaluation of the begin and end instances. 
     *
     * @see <a href="file:///D:/work/doc/specs/smil20.html#smil-timing-q86">
     *      SMIL 2: Principles for building and pruning intervals</a>
     */
    private void pruneCurrentInterval() {
        TimeInterval prunedInterval = currentInterval;
        currentInterval = null;
        if (previousInterval != null) {
            state = STATE_FILL;
        } else {
            state = STATE_NO_INTERVAL;
        }

        prunedInterval.prune();
    }

    /**
     * This method inserts the input time instance in the instance list
     *
     * @param newInstance the instance to reposition in its instance list
     * @see #addTimeInstance
     * @see #onTimeInstanceUpdate
     */
    private void insertTimeInstance(final TimeInstance newInstance) {
        Vector instances = beginInstances;
        if (!newInstance.isBegin) {
            instances = endInstances;
        }

        int n = instances.size();
        int i = 0;
        Time newTime = newInstance.time;
        for (i = 0; i < n; i++) {
            TimeInstance timeInstance 
                = (TimeInstance) instances.elementAt(i);
            if (!newTime.greaterThan(timeInstance.time)) {
                instances.insertElementAt(newInstance, i);
                break;
            }
        }

        if (i == n) {
            instances.addElement(newInstance);
        }
    }

    /**
     * Called to add a new <code>TimeInstance</code> to the begin or end
     * instance list (depending on the new instance's <code>isBegin</code>
     * setting).
     *
     * This is also called as a result of invoking beginAt or endAt.
     *
     * @param newInstance the new <code>TimeInstance</code> to add to the 
     *        list. Should not be null.
     * @see #beginAt
     * @see #endAt
     * @see <a
     * href="http://www.w3.org/TR/smil20/smil-timing.html#Timing-BeginEnd-Restart">
     * SMIL 2, Interaction with restart semantic</a>
     */
    void addTimeInstance(final TimeInstance newInstance) {
        if (timingUpdate) {
            // We are in a timing dependency cycle. Break now.
            return;
        }
        
        timingUpdate = true;

        try {
            insertTimeInstance(newInstance);
            
            // Do not handle instance time list changes if we are 
            // initializing the time graph.
            if (state == STATE_PRE_INIT) {
                return;
            }
            
            if (!newInstance.isBegin) {
                reEvaluateEndTime();
                return;
            }
            
            // Impact the current interval if needed
            switch (state) {
            case STATE_NO_INTERVAL:
                checkNewInterval(computeFirstInterval());
                break;
            case STATE_FILL:
                if (restart != RESTART_NEVER) {
                    checkNewInterval(computeNextInterval());
                } 
                // Else, don't do anything: this element cannot
                // restart.
                break;
            case STATE_WAITING_INTERVAL_0:
                {
                    TimeInterval curInt = computeFirstInterval();
                    if (curInt != null) {
                        currentInterval.setBegin(curInt.begin);
                        currentInterval.setEnd(curInt.end);
                        computeLastDur(currentInterval);
                    }
                }
                break;
            case STATE_WAITING_INTERVAL_N:
                {
                    TimeInterval curInt = computeNextInterval();
                    // We just added a new time instance. It is not possible
                    // that adding a time instance would make 
                    // computeNextInterval return a null interval here. 
                    // So we do not test for a null condition because it 
                    // can't happen. If it happens,
                    // it means there is a bug in computeNextInterval or that
                    // the beginInstance list was messed with, which should not
                    // happen.
                    currentInterval.setBegin(curInt.begin);
                    currentInterval.setEnd(curInt.end);
                    computeLastDur(currentInterval);
                } 
                break;
            case STATE_PLAYING:
                if (newInstance.isBegin) {
                    if (restart == RESTART_ALWAYS) {
                        if (currentInterval.end
                                .greaterThan(newInstance.time)
                            &&
                            newInstance.time
                                .greaterThan(currentInterval.begin)) {
                            // Update the current interval end time with this
                            // new begin time.
                            currentInterval.setEnd(newInstance.time);
                            computeLastDur(currentInterval);
                            
                            // Next interval will be computed in the next call
                            // to sample();
                        }
                        // Ignore the new instance if it if falls after or
                        // before the current interval
                    }
                    // Ignore the new instance if we cannot restart this
                    // animation.
                } 
                break;
            default:
                throw new IllegalStateException();
            }
        } finally {
            timingUpdate = false;
        }
    }

    /**
     * Called by <code>TimeInstance</code>s when they are updated.
     * In response, the TimedElement may recompute its current
     * interval. 
     *
     * @param timeInstance the <code>TimeInstance</code> that was just 
     *        updated.
     *
     * @throws NullPointerException if timeInstance is null.
     *
     * @see <a
     * href="http://www.w3.org/TR/smil20/smil-timing.html#Timing-SemanticsOfTimingModel">
     * SMIL 2: Building the instance times lists</a>
     */
    void onTimeInstanceUpdate(final TimeInstance timeInstance) {
        if (timingUpdate) {
            // We are in a timing dependency cycle, break now.
            return;
        }

        timingUpdate = true;

        try {
            // First, reposition the instance in the instance list
            if (timeInstance.isBegin) {
                beginInstances.removeElement(timeInstance);
            } else {
                endInstances.removeElement(timeInstance);
            }
            insertTimeInstance(timeInstance);
            
            if (state == STATE_PRE_INIT) {
                return;
            }
            
            if (timeInstance.isBegin) {
                // We are dealing with a begin instance change.
                reEvaluateBeginTime();
            } else {
                // We are dealing with an end time instance.
                reEvaluateEndTime();
            }
        } finally {
            timingUpdate = false;
        }
    }

    /**
     * Called to remove a <code>TimeInstance</code> when a 
     * <code>TimeInterval</code> is pruned. This causes a re-evaluation
     * of the current interval's begin or end time.
     *
     * @param timeInstance the instance to remove from its instance list.
     */
    void removeTimeInstance(final TimeInstance timeInstance) {
        if (timeInstance.isBegin) {
            beginInstances.removeElement(timeInstance);
        } else {
            endInstances.removeElement(timeInstance);
        }
        
        if (timingUpdate) {
            return;
        }
        
        timingUpdate = true;
        try {
            if (state != STATE_PRE_INIT) {
                if (timeInstance.isBegin) {
                    reEvaluateBeginTime();
                } else {
                    reEvaluateEndTime();
                }
            }
        } finally {
            timingUpdate = false;
        }
    }

    /**
     * Called by <code>TimeCondition</code>s when they are constructed.
     *
     * @param newCondition the new <code>TimeCondition</code> to add to the 
     *        list. Should not be null.
     */
    void addTimeCondition(final TimeCondition newCondition) {
        if (newCondition.isBegin) {
            beginConditions.addElement(newCondition);
        } else {
            endConditions.addElement(newCondition);
        }
    }

    /**
     * @param eventCondition the <code>EventBaseCondition</code> to check
     *        against.
     * @return true if this <code>TimedElement</code> has a begin condition
     *         corresponding to the input <code>EventBaseCondition</code>
     */
    boolean hasBeginCondition(final EventBaseCondition eventCondition) {
        int n = beginConditions.size();
        for (int i = 0; i < n; i++) {
            TimeCondition condition = 
                (TimeCondition) beginConditions.elementAt(i);
            if (condition instanceof EventBaseCondition) {
                EventBaseCondition beginCondition = 
                    (EventBaseCondition) condition;
                if (beginCondition.eventType.equals(eventCondition.eventType)
                    &&
                    beginCondition.eventBase == eventCondition.eventBase) {
                    return true;
                }
                                                    
            }
        }

        return false;
    }

    /**
     * @return this <code>TimedElement</code>'s current time.
     */
    Time getCurrentTime() {
        return timeContainer.getSimpleTime();
    }

    /**
     * Converts the input 'local' time to an absolute wallclock time.
     *
     * @param localTime the time to convert to wallclock time
     * @return the time converted to an absolute wallclock time.
     */
    long toWallClockTime(final long localTime) {
        return timeContainer.toWallClockTime(localTime);
    }

    /**
     * Converts the input simple time (i.e., a time in the parent container's
     * simple duration) to a root container simple time (i.e., a time
     * in the root time container's simple time interval). Note that this method
     * mutates the input time value. If the associated time container does not 
     * have a current interval, this method returns Time.UNRESOLVED.
     *
     * @param simpleTime the time in the parent container's simple duration.
     *        Should not be null. If simpleTime is Time.UNRESOLVED, the returned
     *        time is UNRESOLVED. If simpleTime is Time.INDEFINITE, the returned
     *        time is INDEFINITE.
     * @return a time in the root time container's simple duration (i.e., in 
     *         the root container's simple time interval).
     */
    Time toRootContainerSimpleTime(Time simpleTime) {
        if (timeContainer.currentInterval == null) {
            return Time.UNRESOLVED;
        }

        if (!simpleTime.isResolved()) {
            return simpleTime;
        }

        // Convert to the container's container simple time

        // Account for repeat behavior. This yields a time
        // in the [0, ActiveTime[ time system
        if (timeContainer.simpleDur.isResolved()) {
            simpleTime.value += 
                timeContainer.curIter * timeContainer.simpleDur.value;
        }

        // By adding the timeContainer's begin value, we are
        // translating the simpleTime to a time in the container's
        // container simple duration [0, simpleDur[ interval.
        simpleTime.value += timeContainer.currentInterval.begin.value;

        // Now, move the conversion one level up.
        return timeContainer.toRootContainerSimpleTime(simpleTime);
    }

    /**
     * Same definition and behavior as toRootContainerSimpleTime, except that
     * this method clamps values to the container's simple duration. This 
     * means that a value smaller than zero is converted to 0 and a value
     * greater than the simple duration is reduced to the simple duration.
     *
     * @param simpleTime the time in the parent container's simple duration.
     *        Should not be null. If simpleTime is Time.UNRESOLVED, the returned
     *        time is UNRESOLVED. If simpleTime is Time.INDEFINITE, the returned
     *        time is INDEFINITE.
     * @return a time in the root time container's simple duration (i.e., in 
     *         the root container's simple time interval).
     */
    Time toRootContainerSimpleTimeClamp(final Time simpleTime) {
        if (timeContainer.currentInterval == null) {
            return Time.UNRESOLVED;
        }

        if (!simpleTime.isResolved()) {
            return simpleTime;
        }

        // Clamp to zero.
        if (simpleTime.value < 0) {
            simpleTime.value = 0;
        }

        // Convert to the container's _container_ simple time

        // Account for repeat behavior. This yields a time
        // in the [0, ActiveTime[ time system
        if (timeContainer.simpleDur.isResolved()) {
            // Clamp to the simple duration.
            if (simpleTime.value > timeContainer.simpleDur.value) {
                simpleTime.value = timeContainer.simpleDur.value;
            }
            simpleTime.value += 
                timeContainer.curIter * timeContainer.simpleDur.value;
        } else {
            // Still clamp to the container's active duration.
            if (timeContainer.currentInterval.end.isResolved()) {
                long maxDur = timeContainer.currentInterval.end.value 
                    - timeContainer.currentInterval.begin.value;
                if (simpleTime.value > maxDur) {
                    simpleTime.value = maxDur;
                }
            }
        }

        // By adding the timeContainer's begin value, we are
        // translating the simpleTime to a time in the container's
        // container simple duration [0, simpleDur[ interval.
        simpleTime.value += timeContainer.currentInterval.begin.value;

        // Now, move the conversion one level up.
        return timeContainer.toRootContainerSimpleTimeClamp(simpleTime);
    }

    /**
     * Converts the input root container simple time (i.e., a time in the root
     * container's simple time interval) to a time in this element's time
     * container simple duration. Note that this method mutates the input
     * argument. If the associated time container does not 
     * have a current interval, this method returns Time.UNRESOLVED.
     *
     * @param simpleTime the time in the root container's simple duration.  If
     * simpleTime is Time.UNRESOLVED, the returned time is UNRESOLVED. If
     * simpleTime is Time.INDEFINITE, the returned time is INDEFINITE.
     *
     * @return a simple time in the parent container's simple duration The
     * return value is in the [0, container simple duration] interval.
     */
    Time toContainerSimpleTime(Time simpleTime) {
        if (timeContainer.currentInterval == null) {
            return Time.UNRESOLVED;
        }

        if (!simpleTime.isResolved()) {
            return simpleTime;
        }

        // Convertion to the container's container simple time
        simpleTime = timeContainer.toContainerSimpleTime(simpleTime);
        
        // Account for the container's begin value
        simpleTime.value -= timeContainer.currentInterval.begin.value;

        // Account for repeat behavior
        if (timeContainer.simpleDur.isResolved()) {
            simpleTime.value -= 
                timeContainer.curIter * timeContainer.simpleDur.value;
        }

        return simpleTime;
    }

    /**
     * @return the container's simple duration.
     */
    Time getContainerSimpleDuration() {
        return timeContainer.simpleDur;
    }

    /**
     * Debug helper.
     * @return a description of this object.
     */
    public String toString() {
        String str = "[Animation: " + animationElement + "] [" 
                        + getStateString() + "]";
        switch (state) {
        case STATE_WAITING_INTERVAL_N:
        case STATE_WAITING_INTERVAL_0:
        case STATE_PLAYING:
            str += "[" 
                + currentInterval.begin 
                + ", " 
                + currentInterval.end 
                /*
                + ", "
                + currentInterval.lastDur
                */
                + "]";
            break;
        default:
            break;
        }
        return str;
    }

    /**
     * Debug helper. Converts the state to a string.
     *
     * @return a string describing the element's state.
     */
    String getStateString() {
        switch (state) {
        case STATE_PRE_INIT:
            return "PRE_INIT";
        case STATE_WAITING_INTERVAL_0:
            return "WAITING_INTERVAL_0";
        case STATE_WAITING_INTERVAL_N:
            return "WAITING_INTERVAL_N";
        case STATE_NO_INTERVAL:
            return "NO_INTERVAL";
        case STATE_FILL:
            return "FILL";
        case STATE_PLAYING:
            return "PLAYING";
        default:
            return "UNKNOWN";
        }
    }
}

