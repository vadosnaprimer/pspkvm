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

import org.w3c.dom.events.Event;

/**
 * A <code>RepeatCondition</code> generates a <code>TimeInstance</code> 
 * everytime the associated repeat event occurs with the expected 
 * <code>repeatCount</code> value.
 *
 * @version $Id: RepeatCondition.java,v 1.3 2006/06/29 10:47:34 ln156897 Exp $
 */
public final class RepeatCondition extends EventBaseCondition {
    /**
     * The repeatCount which triggers the condition.
     */
    int repeatCount;

    /**
     * @param timedElement the associated <code>TimedElementSupport</code>. 
     *        Should not be null.
     * @param isBegin defines whether this condition is for a begin list.
     * @param eventBaseId the id of the element which generates events this 
     *        listener listens to. should not be null.
     * @param offset offset from the sync base. This means that time instances
     *        synchronized on the syncBase begin or end time are offset by 
     *        this amount.
     * @param repeatCount only when the eventBase generates a repeat event with
     * this repeatCount will a <code>TimeInstance</code> be generated. 
     */
    public RepeatCondition(final TimedElementSupport timedElement,
                           final boolean isBegin,
                           final String eventBaseId,
                           final long offset,
                           final int repeatCount) {
        super(timedElement, 
              isBegin, 
              eventBaseId,
              TimedElementSupport.REPEAT_EVENT_TYPE, 
              offset);

        this.repeatCount = repeatCount;
    }

    /**
     * Implementation of the <code>EventListener</code> interface.
     * This is a simple filtered version of the handleEvent implementation
     * in <code>EventBaseCondition</code>.
     *
     * @param evt the event that occured
     */
    public void handleEvent(final Event evt) {
        if (((ModelEvent) evt).repeatCount == repeatCount) {
            super.handleEvent(evt);
        }
    }

    /**
     * Converts this <code>RepeatCondition</code> to a String trait.
     *
     * @return a string describing this <code>TimeCondition</code>
     */
    protected String toStringTrait() {
        StringBuffer sb = new StringBuffer();

        sb.append(eventBaseId);
        sb.append(".repeat(");
        sb.append(repeatCount);
        sb.append(')');

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
