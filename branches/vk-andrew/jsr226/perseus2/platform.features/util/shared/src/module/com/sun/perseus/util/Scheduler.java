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

package com.sun.perseus.util;

import com.sun.perseus.util.RunnableQueue.RunnableHandler;

/**
 * This class is used to multiplex Runnables that need to be
 * run at a fixed rate.
 *
 * @version $Id: Scheduler.java,v 1.3 2006/04/21 06:35:54 st125089 Exp $
 */
final class Scheduler {
    static final class Entry {
        /**
         * The entry's associated Runnable.
         */
        Runnable runnable;

        /**
         * The lengths of time, in milliseconds, between consecutive runs of the
         * entry's runnable.
         */
        long interval;

        /**
         * The time (as returned from System.currentTimeMillis) when the 
         * runnable should next run.
         */
        long nextRun;

        /**
         * Whether this entry has been removed. This is used to handle
         * cases where a Runnable removes itself from the queue during
         * the scheduler's run method.
         */
        boolean live = true;

        /**
         * The associated RunnableHandler, which should be notified when 
         * this Runnable is ran.
         */
        RunnableHandler handler;
    }

    /**
     * The associated RunnableQueue.
     */
    RunnableQueue rq;

    /**
     * The current list of Runnables scheduled at a fixed interval.
     */
    Entry[] entries = new Entry[0];

    /**
     * Builds a new Scheduler for the given RunnableQueue.
     *
     * @param rq the associated RunnableQueue. Should not be null.
     */
    Scheduler(final RunnableQueue rq) {
        if (rq == null) {
            throw new NullPointerException();
        }

        this.rq = rq;
    }

    /**
     * Adds a new Runnable to be run at the requested fixed interval.  Note that
     * if the input Runnable is entered multiple times into the scheduler, it
     * will be run once for each interval it is registered with.
     * 
     * @param r the Runnable to run at a fixed rate. Should not be null.
     * @param interal the interval, in milliseconds, between runs of the 
     *        Runnable. Should be strictly positive.
     * @param handler the associated RunHandler, which should be notified
     *        when the Runnable is actually run. May be null.
     */
    public synchronized void add(final Runnable r, 
                                 final long interval,
                                 final RunnableHandler handler) {
        if (r == null || interval <= 0) {
            throw new IllegalArgumentException();
        }

        Entry[] tmpEntries = new Entry[entries.length + 1];
        System.arraycopy(entries, 0, tmpEntries, 0, entries.length);
        Entry newEntry = new Entry();
        newEntry.runnable = r;
        newEntry.interval = interval;
        newEntry.handler = handler;
        tmpEntries[entries.length] = newEntry;
        
        entries = tmpEntries;
    }

    /**
     * Removes a Runnable from the list of Runnables that are scheduled
     * at a fixed interval. If the Runnable was registered multiple times
     * with this scheduler, all instances are removed.
     *
     * @param r the Runnable to removed from the list of Runnables scheduled
     *        at a fixed rate.
     */
    public synchronized void remove(final Runnable r) {
        while (removeImpl(r)) {}
    }

    /**
     * Implementation helper.
     *
     * Removes the input Runnable, and returns true if the Runnable was found.
     *
     * @param r the Runnable to remove.
     * @return true if the Runnable was found.
     */
    private boolean removeImpl(final Runnable r) {
        // First, look for the given entry.
        int i = entries.length + 1;
        for (i = 0; i < entries.length; i++) {
            if (entries[i].runnable == r) {
                break;
            }
        }
        
        if (i < entries.length) {
            // We did find the entry.
            Entry[] tmpEntries = new Entry[entries.length - 1];
            System.arraycopy(entries, 0, tmpEntries, 0, i);
            System.arraycopy(entries, i + 1, tmpEntries, i, entries.length - 1 - i);

            // Mark the entry to be removed as dead, so that it does not get
            // run in the run method, in case a Runnable removes itself during
            // the scheduler's run method.
            entries[i].live = false;

            entries = tmpEntries;            
            return true;
        }

        return false;
    }

    /**
     * @param currentTime the current time, in milliseconds.
     * @return the time until the next scheduled Runnable needs to be run.
     *         Returns -1 if there is no scheduled Runnable.
     */
    public synchronized long nextRun(final long currentTime) {
        if (entries.length > 0) {
            long nextRun = entries[0].nextRun;
            for (int i = 1; i < entries.length; i++) {
                if (entries[i].nextRun < nextRun) {
                    nextRun = entries[i].nextRun;
                }
            }

            nextRun -= currentTime;
            if (nextRun < 0) {
                nextRun = 0;
            }

            return nextRun;
        } else {
            return -1;
        }
    }

    /**
     * Runs the scheduled Runnables that are due or overdue.
     *
     * @param currentTime the currentTime when this runnable is ran.
     */
    public synchronized void run(long currentTime) {
        // We need to keep a local reference to entries in case
        // a Runnable unschedules during its run() method.
        Entry[] lEntries = entries;
        for (int i = 0; i < lEntries.length; i++) {
            if (lEntries[i].nextRun <= currentTime && lEntries[i].live) {
                lEntries[i].runnable.run();
                lEntries[i].handler.runnableInvoked(rq, lEntries[i].runnable);
                lEntries[i].nextRun = currentTime + lEntries[i].interval;
            }
        }
    }
}
