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
 * <code>SMILSample</code> is the root of the SMIL Timing engine operation.
 * This class simply samples the time graph at the current time where the 
 * current time is controlled by a <code>Clock</code> abstraction.
 *
 * @version $Id: SMILSample.java,v 1.2 2006/04/21 06:38:28 st125089 Exp $
 */
public class SMILSample implements Runnable {
    /**
     * The <code>Clock</code> abstraction is used to encapsulate the
     * notion of time in the SMIL engine. By default, the clock will
     * be the real-world clock. However, some other implementations 
     * might be useful, for example to render a given sequence of times
     * or to force rendering at a fixed frame rate.
     */
    public static interface Clock {
        /**
         * @return the current time, according to the clock.
         */
        long currentTimeMillis();
    }

    /**
     * The <code>DocumentWallClock</code> implementation is used to sample the 
     * time graph according to the perceived user time.
     */
    public static class DocumentWallClock implements Clock {
        /**
         * The associated Document's time support.
         */
        TimeContainerRootSupport tcrs;

        /**
         * Time at which the document was last sampled
         */
        long lastSample = Long.MAX_VALUE;
        
        /**
         * true if the document's clock was started.
         * @see #start
         */
        boolean reset = false;

        /**
         * @param doc the associated Document
         */
        public DocumentWallClock(final DocumentNode doc) {
            this.tcrs = doc.timeContainerRootSupport;
        }

        /**
         * @return the current time. For a DocumentWallClock, this is the 
         *         system's current time minus the document start time so 
         *         that 0 maps to the time the document was started.
         */
        public long currentTimeMillis() {
            if (!reset) {
                long ds = System.currentTimeMillis() - lastSample;
                lastSample += ds;
                return tcrs.lastSampleTime.value + ds;
            } else {
                lastSample = System.currentTimeMillis();
                reset = false;
                if (!tcrs.lastSampleTime.isResolved()) {
                    // We have never sampled the graph, start from 0
                    return 0;
                } else {
                    // We actually paused for a while. We are resuming 
                    // the clock.
                    return tcrs.lastSampleTime.value;
                }
            }
        }

        /**
         * Should be called when the document's time line is ready to
         * begin. As a result, the first time currentTimeMillis is called,
         * the return value should be exactly zero.
         */
        public void start() {
            reset = true;
        }
    }

    /**
     * The document on which sampling is done.
     */
    protected DocumentNode doc;

    /**
     * The root of the time graph to sample.
     */
    protected TimeContainerRootSupport root;

    /**
     * The time at which we are currently sampling.
     */
    protected Time currentTime = new Time(0);

    /**
     * The clock used by this sampler to define the current document time.
     */
    protected Clock clock;

    /**
     * Builds a new time graph sampler which samples the time graph
     * at the current real-world time.
     *
     * @param doc the document, root of the time graph to sample. Should not
     *        be null. The DocumentNode's timeContainerRootSupport should not
     *        be null either.
     * @param clock the clock used to provide the current time. Should not 
     * be null.
     */
    public SMILSample(final DocumentNode doc,
                      final Clock clock) {
        if (doc == null || doc.timeContainerRootSupport == null) {
            throw new NullPointerException();
        }

        if (clock == null) {
            throw new NullPointerException();
        }
         
        this.doc = doc;
        this.root = doc.timeContainerRootSupport;
        this.clock = clock;
    }

    /**
     * <code>Runnable</code> implementation.
     */
    public void run() {
        currentTime.value = clock.currentTimeMillis();
        doc.sample(currentTime);
        // System.err.println("\n\n\n");
        // root.dump();
        doc.applyAnimations();
        doc.applyMedia();
    }
}
