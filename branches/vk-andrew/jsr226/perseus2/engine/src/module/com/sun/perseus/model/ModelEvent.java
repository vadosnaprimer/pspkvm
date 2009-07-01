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
import org.w3c.dom.events.EventTarget;

/**
 * Events happen on a specific target and have a specific type.
 *
 * @see EventListener
 * @see EventSupport
 *
 * @version $Id: ModelEvent.java,v 1.5 2006/06/29 10:47:33 ln156897 Exp $
 */
public class ModelEvent implements Event {
    /**
     * Used when for undefined key char events
     */
    public static final char CHAR_UNDEFINED = 0xFFFF;

    /**
     * The event's type
     */
    protected String type;

    /**
     * The event target
     */
    protected ModelNode target;

    /**
     * The current event target.
     */
    protected EventTarget currentTarget;

    /**
     * The event's anchor is used to ease the implementation 
     * of hyperlinking. 
     *
     * @see EventSupport
     */
    protected Anchor anchor;

    /**
     * Controls whether or not the event's dispatch
     * should be stopped
     */
    protected boolean stopPropagation;

    /**
     * Time stamp. The time is in document simple time, i.e., in the
     * root container's time.
     */
    protected Time eventTime = Time.UNRESOLVED;

    /**
     * Used to provide the repeat iteration in a repeat event
     */
    protected int repeatCount = 0;
     
    /**
     * Used to provide the key character in a key event
     */
    protected char keyChar = CHAR_UNDEFINED;

    /**
     * @param type the event's type. Should not be null.
     * @param target the event's target. Should not be null.
     */
    public ModelEvent(final String type, final ModelNode target) {
        if (type == null) {
            throw new NullPointerException();
        }

        this.type = type;
        
        if (target == null) {
            throw new NullPointerException();
        }

        this.target = target;

        // Set the event time.
        this.eventTime = 
                target.ownerDocument.timeContainerRootSupport.getCurrentTime();
    }

    /**
     * @param type the event's type. Should not be null
     * @param target the event's target. Should not be null.
     * @param keyChar the event's character key
     */
    public ModelEvent(final String type, 
                      final ModelNode target, 
                      final char keyChar) {
        this(type, target);
        this.keyChar = keyChar;
    }

    /**
     * @return this event's type
     */
    public String getType() {
        return type;
    }

    /**
     * @return the key that was stroked.
     */
    public char getKeyChar() {
        return keyChar;
    }

    /**
     * @return this node's target
     */
    public EventTarget getTarget() {
        return target;
    }

    /**
     * @return this node's current target
     */
    public EventTarget getCurrentTarget() {
        return currentTarget;
    }

    /**
     * Stops propagation of the Event during dispatch. See the
     * DOM Level 2 Event model specification for details.
     */
    public void stopPropagation() {
        stopPropagation = true;
    }

    /**
     * @return true if propagation should stop. false otherwise
     */
    public boolean getStopPropagation() {
        return stopPropagation;
    }

    /**
     * @param anchor the Event's associated Anchor
     * @throws IllegalStateException if the anchor property is not null
     */
    public void setAnchor(final Anchor anchor) {
        if (this.anchor == null) {
            this.anchor = anchor;
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * @return the current value of the anchor property
     */
    public Anchor getAnchor() {
        return anchor;
    }
}
