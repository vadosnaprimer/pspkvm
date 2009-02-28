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

import java.util.Hashtable;
import java.util.Vector;

import org.w3c.dom.events.EventListener;

/**
 * <code>EventSupport</code> assumes two functions. First, it offers
 * a central place for listeners to register for specific event types on
 * specific observers for a particular event propagation phase (capture
 * or bubble). Second, it performs the event dispatching according to the
 * DOM Level 2 Event model of capture, at target and bubbling.
 *
 * @version $Id: EventSupport.java,v 1.9 2006/06/29 10:47:31 ln156897 Exp $
 */
class EventSupport {
    /**
     * One of the values allowed for the phase parameter
     * in the addEventListener method
     */
    public static final int CAPTURE_PHASE = 0;

    /**
     * One of the values allowed for the phase parameter
     * in the addEventListener method
     */
    public static final int BUBBLE_PHASE = 1;

    /**
     * Maps nodes to a map of listeners for a given event type
     */
    protected Hashtable allListeners = new Hashtable();

    /**
     * Temporary array used to dispatch events.
     */
    protected EventListener[] freezeList;

    /**
     * Implementation.
     *
     * @param n the minimal size needed for the returned freezeList
     */
    EventListener[] getFreezeList(final int n) {
        if (freezeList == null || freezeList.length < n) {
            freezeList = new EventListener[n];
        }

        return freezeList;
    }

    /**
     * Removes an event listener from the input handler, for the given
     * input event type and phase.
     *
     * @param handler the node on which the listener was hooked
     * @param type the event type the listener was listening to
     * @param phase the phase the listener was listening to
     * @param listener the listener to be removed
     */
    void removeEventListener(ModelNode handler,
                             final String type,
                             final int phase,
                             final EventListener listener) {
        if (handler == null) {
            throw new NullPointerException();
        }

        if (type == null) {
            throw new NullPointerException();
        }

        if (!(phase == BUBBLE_PHASE || phase == CAPTURE_PHASE)) {
            throw new IllegalArgumentException();
        }

        if (listener == null) {
            throw new NullPointerException();
        }

        // See the SVG 1.1 specification, section 5.6 "The use element".  An
        // element and all its corresponding SVGElementInstance objects share an
        // event listener list.
        if (handler instanceof ElementNodeProxy) {
            handler = ((ElementNodeProxy) handler).proxied;
        }

        Hashtable nodeListeners = (Hashtable) allListeners.get(handler);
        if (nodeListeners == null) {
            return;
        }

        Vector[] evtTypeListeners =
            (Vector[]) nodeListeners.get(type);

        if (evtTypeListeners == null) {
            return;
        }

        Vector phaseListeners = evtTypeListeners[phase];

        // If phaseListeners is null, this means the listener was not registered
        // for that phase. According to the DOM Events Level 2 spec, just ignore
        // the request.
        if (phaseListeners != null) {
            phaseListeners.removeElement(listener);
        }
    }

    /**
     * Adds a event listener on the input handler for the input event type 
     * and phase.
     * 
     * @param handler the node on which the listener is hooked
     * @param type the type of events to listen to. Should not be null
     * @param phase the phase the listener listens to. Should be one
     *        of CAPTURE_PHASE or BUBBLE_PHASE
     * @param listener the listener to hook to the handler
     */
    void addEventListener(ModelNode handler,
                          final String type,
                          final int phase,
                          final EventListener listener) {
        if (handler == null) {
            throw new NullPointerException();
        }

        if (type == null) {
            throw new NullPointerException();
        }

        if (!(phase == CAPTURE_PHASE || phase == BUBBLE_PHASE)) {
            throw new IllegalArgumentException();
        }

        if (listener == null) {
            throw new NullPointerException();
        }

        // See the SVG 1.1 specification, section 5.6 "The use element".  An
        // element and all its corresponding SVGElementInstance objects share an
        // event listener list.
        if (handler instanceof ElementNodeProxy) {
            handler = ((ElementNodeProxy) handler).proxied;
        }

        Hashtable nodeListeners = (Hashtable) allListeners.get(handler);
        if (nodeListeners == null) {
            // Create entry if none exists for this node
            nodeListeners = new Hashtable();
            allListeners.put(handler, nodeListeners);
        }

        Vector[] evtTypeListeners = 
            (Vector[]) nodeListeners.get(type);

        if (evtTypeListeners == null) {
            // Create array for listeners of this event
            // type if none exists
            evtTypeListeners = new Vector[2];
            nodeListeners.put(type, evtTypeListeners);
        }

        Vector phaseListeners = evtTypeListeners[phase];
        if (phaseListeners == null) {
            // Create vector for listeners on that phase
            // if none exists
            phaseListeners = new Vector(1);
            evtTypeListeners[phase] = phaseListeners;
        }

        if (!phaseListeners.contains(listener)) {
            phaseListeners.addElement(listener);
        }
    }

    /**
     * Debug. Traces all the registered listeners
     */
    /*
    public void dumpListeners() {
        Iterator iter = allListeners.keySet().iterator();
        while (iter.hasNext()) {
            Object node = iter.next();
            Hashtable nodeListeners = (Hashtable) allListeners.get(node);
            Iterator iter2 = nodeListeners.keySet().iterator();
            System.out.println("Listeners for " + node);
            while (iter2.hasNext()) {
                Object type = iter2.next();
                System.out.println("----> " + type);
                Vector[] evtTypeListeners = (Vector[]) nodeListeners.get(type);
                if (evtTypeListeners == null) {
                    System.out.println("       +--> (null)");
                } else {
                    System.out.println("       +--> [capture]");
                    Vector captureListeners = evtTypeListeners[CAPTURE_PHASE];
                    if (captureListeners == null) {
                        System.out.println("              +--> null");
                    } else {
                        int n = captureListeners.size();
                        for (int i = 0; i < n; i++) {
                            System.out.println("              +--> " 
                                               + captureListeners.elementAt(i));
                        }
                    }

                    System.out.println("       +--> [bubble]");
                    Vector bubbleListeners = evtTypeListeners[BUBBLE_PHASE];
                    if (bubbleListeners == null) {
                        System.out.println("              +--> null");
                    } else {
                        int n = bubbleListeners.size();
                        for (int i = 0; i < n; i++) {
                            System.out.println("              +--> " 
                                               + bubbleListeners.elementAt(i));
                        }
                    }

                }

            }
        }
    }
    */

    /**
     * Dispatches the input event to the listeners, performing a 
     * capture and bubble phase, as defined by the DOM Level 2
     * event model.
     *
     * @param evt the event to dispatch
     */
    public void dispatchEvent(final ModelEvent evt) {
        if (evt == null) {
            return;
        }

        // Note that an event type cannot be null,
        // see the Event class.
        String type = evt.getType();

        ModelNode target = (ModelNode) evt.getTarget();

        // CAPTURE PHASE
        // ====================================================================
        // Now, perform the capture phase
        // We go from the root (last in the dynasty array)
        // to the last element (immediate parent).
        if (target.parent != null) {
            fireCapture(target.parent, evt);
        }
        
        // AT TARGET PHASE
        // ====================================================================
        if (!evt.getStopPropagation()) {
            fireEventListeners(target, evt, CAPTURE_PHASE);
            fireEventListeners(target, evt, BUBBLE_PHASE);
        }

        // BUBBLE PHASE
        // ====================================================================
        if (target.parent != null) {
            fireBubble(target.parent, evt);
        }
    }

    /**
     * Fires the bubble event listeners on the input target. This starts
     * by firing this node's listeners, then the parent bubble listeners and 
     * so on, up to the tree's root which has no parent.
     *
     * @param currentTarget the node on which the event should be dispatched
     *        unless the event propagation has been stopped.
     * @param evt the event to dispatch.
     */
    protected void fireBubble(final ModelNode currentTarget,
                              final ModelEvent evt) {
        if (evt.getStopPropagation()) {
            return;
        }

        fireEventListeners(currentTarget, evt, BUBBLE_PHASE);

        if (currentTarget.parent != null) {
            fireBubble(currentTarget.parent, evt);
        }
    }

    /**
     * Fires the capture event listeners on the input target. This starts
     * by firing the node's parent capture listeners. As a result of this
     * recursive behavior, the listeners on the tree root (which has no parent)
     * are fired first, then listeners down the tree are fired.
     *
     * @param currentTarget the node on which the event should be dispatched
     *        unless the event propagation has been stopped.
     * @param evt the event to dispatch.
     */
    protected void fireCapture(final ModelNode currentTarget,
                               final ModelEvent evt) {
        if (currentTarget.parent != null) {
            fireCapture(currentTarget.parent, evt);
        }
        
        if (!evt.getStopPropagation()) {
            fireEventListeners(currentTarget, evt, CAPTURE_PHASE);
        }
    }

    /**
     * Fires the event listeners attached to the input current target
     * @param currentTarget the node on which the event is currently flowing
     * @param evt the event to propagate
     * @param phase defines whether the event is propagating in the capture
     *        or bubble phase. One of CAPTURE_PHASE or BUBBLE_PHASE.
     */
    protected void fireEventListeners(final ModelNode currentTarget,
                                      final ModelEvent evt,
                                      final int phase) {
        // Update Href. Remember that the Event's anchor is sticky and 
        // cannot be changed once set
        if (evt.getAnchor() == null && currentTarget instanceof Anchor) {
            evt.setAnchor((Anchor) currentTarget);
        }
 
        Vector listeners = getEventListeners(currentTarget,
                                             phase,
                                             evt);
        if (listeners == null) {
            return;
        }

        // We use a copy of the list because event listeners may be removed
        // during event dispatching.
        int n = listeners.size();
        EventListener[] freezeList = getFreezeList(n);
        listeners.copyInto(freezeList);

        evt.currentTarget = currentTarget;

        for (int i = 0; i < n; i++) {
            // The DOM Level 2 specification requires that an event listener
            // never be called after it has been removed.
            if (listeners.contains(freezeList[i])) {
                freezeList[i].handleEvent(evt);
            }
        }
    }

    /**
     * @param node the ModelNode on which listeners are seeked
     * @param phase the event propagation phase for which listeners are
     *        seeked.
     * @param evt the event
     * @return a Vector of EventListener registered on the input node,
     *         for the given phase and the given event type.
     */
    protected Vector getEventListeners(ModelNode node,
                                       final int phase,
                                       final ModelEvent evt) {
        // See the SVG 1.1 specification, section 5.6 "The use element".  An
        // element and all its corresponding SVGElementInstance objects share an
        // event listener list.
        if (node instanceof ElementNodeProxy) {
            node = ((ElementNodeProxy) node).proxied;
        }

        Hashtable nodeListeners = (Hashtable) allListeners.get(node);
        if (nodeListeners == null) {
            return null;
        }

        Vector[] evtTypeListeners = (Vector[]) nodeListeners.get(evt.getType());
        if (evtTypeListeners == null) {
            return null;
        }

        return evtTypeListeners[phase];
    }
}

