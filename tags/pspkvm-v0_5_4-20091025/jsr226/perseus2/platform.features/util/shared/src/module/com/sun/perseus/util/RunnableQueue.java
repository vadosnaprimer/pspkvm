/*
 *
 *
 * Portions Copyright  2000-2007 Sun Microsystems, Inc. All Rights
 * Reserved.  Use is subject to license terms.
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

/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included with this distribution in  *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package com.sun.perseus.util;

import com.sun.perseus.platform.ThreadSupport;

/**
 * This class represents an object which queues Runnable objects for
 * invocation in a single thread.
 *
 * This class is derived from work done in the Batik project but was
 * seriously modified and extended.
 *
 * @version $Id: RunnableQueue.java,v 1.5 2006/04/21 06:35:50 st125089 Exp $
 */
public final class RunnableQueue implements Runnable {
    /**
     * The queue is in the processes of running tasks.
     */
    public static final String RUNNING = "Running";

    /**
     * The queue may still be running tasks but as soon as possible
     * will go to SUSPENDED state.
     */
    public static final String SUSPENDING = "Suspending";

    /**
     * The queue is no longer running any tasks and will not
     * run any tasks until resumeExecution is called.
     */
    public static final String SUSPENDED = "Suspended";

    /**
     * This queue has been interrupted
     */
    public static final String TERMINATED = "Terminated";

    /**
     * The default RunnableQueue instance.
     */
    protected static RunnableQueue defaultQueue;

    /**
     * The Suspension state of this thread.
     */
    protected String state;

    /**
     * Object to synchronize/wait/notify for suspension
     * issues.
     */
    protected Object stateLock = new Object();

    /**
     * The Scheduler which can run Runnables at a fixed 
     * rate.
     */
    protected Scheduler scheduler = new Scheduler(this);

    /**
     * The Runnable objects list, also used as synchoronization point
     * for pushing/poping runables.
     */
    protected DoublyLinkedList list = new DoublyLinkedList();

    /**
     * The object which handles RunnableQueue events.
     */
    protected RunnableQueueHandler queueHandler;

    /**
     * The current thread.
     */
    protected Thread runnableQueueThread;

    /**
     * Used for the RunnableQueue's thread names.
     * @see #createRunnableQueue
     */
    private static int threadCount;

    /**
     * All <code>RunnableQueue</code> instances should be created through
     * the <code>createRunnableQueue</code> method.
     * 
     * @see #createRunnableQueue
     */
    private RunnableQueue() {
    }

    /**
     * Returns the default <code>RunnableQueue</code> instance. This is what
     * should be used in most circumstances. In particular, all document
     * instances which need to process in a seperate thread should share this
     * default RunnableQueue.
     *
     * @return the default <code>RunnableQueue</code> instance.
     */
    public static RunnableQueue getDefault() {
        if (defaultQueue != null) {
            return defaultQueue;
        }

        defaultQueue = RunnableQueue.createRunnableQueue(new VoidQueueHandler());
        defaultQueue.resumeExecution();

        return defaultQueue;
    }

    /**
     * Creates a new RunnableQueue started in a new thread.
     *
     * @param queueHandler the <tt>RunnableQueueHandler</tt> which will be notified
     *        of the <tt>RunnableQueue</tt>'s activity. May be null.
     * @return a RunnableQueue which is garanteed to have entered its
     *         <tt>run()</tt> method.
     */
    public static RunnableQueue createRunnableQueue
        ( RunnableQueueHandler queueHandler) {
        RunnableQueue result = new RunnableQueue();

        // Configure the RunHandler
        if (queueHandler == null) {
            queueHandler = new VoidQueueHandler();
        }
        result.queueHandler = queueHandler;

        // Start the thread. We use the RunnableQueue instance as
        // a lock to synchronize between this method and the
        // run method (called from the RunnableQueue thread)
        synchronized (result) {
            Thread t = new Thread(result, "RunnableQueue-" + threadCount++);
            ThreadSupport.setDaemon(t, true);
            t.setPriority(Thread.MIN_PRIORITY);
            t.start();
            while (result.getThread() == null) {
                try {
                    // See the run() method. It calls notify on 
                    // the RunnableQueue instance to notify us
                    // that the thread has started executing
                    result.wait();
                } catch (InterruptedException ie) {
                }
            }
        }

        // Wait until we get into suspended state. State changes
        // are synchronized with the stateLock lock.
        synchronized (result.stateLock) {
            try {
                while (result.state != SUSPENDED) {
                    result.stateLock.wait();
                }
            } catch (InterruptedException ie) {
            }
        }

        return result;
    }
    
    /**
     * Runs this queue. Implements the <code>Runnable</code> interface.
     */
    public void run() {
        //
        // This object is used as a lock to synchronize on the
        // queue's thread execution start. 
        //
        synchronized (this) {
            runnableQueueThread = Thread.currentThread();
            // Wake the create method so it knows we are in
            // our run and ready to go.
            notify();
        }

        Link l = null;
        Runnable rable = null, sRable;
        long t = 0;
        long wait = 0;

        try {
            while (!ThreadSupport.isInterrupted(Thread.currentThread())) {
                // Mutex for suspending/resuming work.
                synchronized (stateLock) {
                    if (state != RUNNING) {
                        state = SUSPENDED;

                        // notify suspendExecution in case it is
                        // waiting til we shut down.
                        stateLock.notifyAll();

                        queueHandler.executionSuspended(this);

                        while (state != RUNNING) {
                            state = SUSPENDED;
                            // Wait until resumeExecution called.
                            stateLock.wait(); 
                        }

                        // notify resumeExecution as it waits until 
                        // execution are really resumed
                        stateLock.notifyAll();
                        queueHandler.executionResumed(this);
                    }
                }

                // First, run the Scheduler to take care of all the pending
                // fixed rate Runnables.
                t = System.currentTimeMillis();
                scheduler.run(t);

                synchronized (list) {
                    l = (Link) list.pop();
                    if (l == null) {
                        // Wait until the next scheduled runnable
                        wait = scheduler.nextRun(System.currentTimeMillis());

                        if (wait == 0) {
                            wait = 1;
                        }

                        if (state == SUSPENDING) {
                            continue;
                        }

                        if (wait > 0) {
                            list.wait(wait);
                        } else {
                            // There is no scheduled runnable at this point.
                            list.wait();
                        }

                        continue; // start loop over again...
                    }
                    
                    rable = l.runnable;
                }

                try {
                    rable.run();
                } catch (Exception e) {
                    // Might be nice to notify someone directly.
                    // But this is more or less what Swing does.
                    e.printStackTrace();
                }
                
                if (l.runHandler != null) {
                    l.runHandler.runnableInvoked(this, rable);
                }

                l.unlock();
                rable = null;
            }
        } catch (InterruptedException e) {
            if (this == defaultQueue) {
                defaultQueue = null;
            }
            e.printStackTrace();
        } finally {
            if (this == defaultQueue) {
                defaultQueue = null;
            }
            System.err.println(">>>>>>>>>>>>>> RunnableQueue terminating");
            synchronized (this) {
                runnableQueueThread = null;
            }
            synchronized (stateLock) {
                state = TERMINATED;
                stateLock.notifyAll();
            }
        }
    }

    /**
     * Returns the thread in which the RunnableQueue is currently running.
     * @return null if the RunnableQueue has not entered his
     *         <tt>run()</tt> method.
     */
    public Thread getThread() {
        return runnableQueueThread;
    }

    /**
     * Removes all pending <code>Runnable</code>s.
     */
    public void empty() {
        synchronized (list) {
            list.empty();
        }
    }

    /**
     * @return the number of pending runnables
     */
    public int getSize() {
        synchronized (list) {
            return list.getSize();
        }
    }

    /**
     * @return the next pending runnable
     */
    public Runnable getNextPending() {
        synchronized (list) {
            if (list.getSize() == 0) {
                return null;
            } else {
                return ((Link) list.getHead()).runnable;
            }
        }
    }

    /**
     * Schedules the input <code>Runnable</code> at the requested
     * fixed rate. The <code>RunnableQueue</code> offers a 'best' 
     * effort service meaning that it will schedule the <code>Runnable</code>
     * as soon as possible so that the time between the begining of
     * two consecutive runs of the <code>Runnable</code> is as close
     * as possible to the requested rate. Note that a too high rate
     * may cause the rest of the <code>Runnable</code> in the 
     * <code>RunnableQueue</code> to be starved and never get 
     * executed.
     *
     * @param r the <code>Runnable</code> to schedule at a regular
     *        interval. If null, there won't be any <code>Runnable</code>
     *        scheduled and if there was a current one, it won't be executed
     *        any more.
     * @param runHandler the <code>RunnableHandler</code> to notify 
     *        once the <code>Runnable</code> has finished executing.
     *        Should not be null.
     * @param interval the minimum interval between to consecutive 
     *        executions of the input <code>Runnable</code>. The 
     *        value is in milliseconds. 
     *
     * @throws IllegalArgumentException If this parameter is zero or less, 
     *         and <code>r</code> is not null.
     *        
     */
    public void scheduleAtFixedRate(final Runnable r,
                                    final RunnableHandler runHandler,
                                    final long interval) {
        scheduler.add(r, interval, runHandler);

        // In case the queue is running and waiting for an item in the
        // list, notify the list so that we can get the animation loop
        // going. See the run() method.
        synchronized (list) {
            list.notify();
        }
    }

    /**
     * Removes the input <code>Runnable</code> from the list of 
     * Runnables scheduled at a fixed rate. If the Runnable is not
     * currently scheduled at a fixed rate, then this method does
     * nothing. If this Runnable was scheduled multiple times 
     * with this RunnableQueue, then all instances are removed.
     *
     * @param r the Runnable that should no longer be scheduled at a 
     *        fixed rate.
     * @see #scheduleAtFixedRate
     */
    public void unschedule(final Runnable r) {
        scheduler.remove(r);
    }

    /**
     * Schedules the given Runnable object for a later invocation, and
     * returns.
     * An exception is thrown if the RunnableQueue was not started.
     *
     * @param r the <code>Runnable</code> to put at the end of the
     *        execution list.
     * @param runHandler the <code>RunnableHandler</code> to notify 
     *        once the <code>Runnable</code> has finished executing.
     *        Should not be null.
     * @throws IllegalStateException if getThread() is null.
     */
    public void invokeLater(final Runnable r, final RunnableHandler runHandler) {
        if (runnableQueueThread == null) {
            throw new IllegalStateException
                ("RunnableQueue not started or has exited");
        }

        synchronized (list) {
            list.push(new Link(r, runHandler));
            list.notify();
        }
    }

    /**
     * Waits until the given Runnable's <tt>run()</tt> has returned.
     * <em>Note: <tt>invokeAndWait()</tt> must not be called from the
     * current thread (for example from the <tt>run()</tt> method of the
     * argument).
     *
     * @param r the <code>Runnable</code> to put at the end of the 
     *        execution list.
     * @param runHandler the <code>RunnableHandler</code> to notify 
     *        once the <code>Runnable</code> has finished executing.
     *        Should not be null.
     * @throws IllegalStateException if getThread() is null or if the
     *         thread returned by getThread() is the current one.
     * @throws InterruptedException if the thread is interrupted while 
     *         waiting for the input <code>Runnable</code> to complete
     *         its execution.
     */
    public void invokeAndWait(final Runnable r, final RunnableHandler runHandler) 
        throws InterruptedException {

        if (runnableQueueThread == null) {
            throw new IllegalStateException
                ("RunnableQueue not started or has exited");
        }
        if (runnableQueueThread == Thread.currentThread()) {
            throw new IllegalStateException
                ("Cannot be called from the RunnableQueue thread");
        }

        LockableLink l = new LockableLink(r, runHandler);
        synchronized (list) {
            list.push(l);
            list.notify();
        }
        l.lock();
    }


    /**
     * Waits until the given Runnable's <tt>run()</tt> has returned.
     * <em>Note: <tt>safeInvokeAndWait()</tt> may be called from any thread.
     * This method checks if this thread is the update thread, in which case
     * the Runnable is invoked directly. Otherwise, it delegates to the 
     * invokeAndWait method.
     *
     * @param r the <code>Runnable</code> to put at the end of the 
     *        execution list. Should not be null.
     * @param runHandler the <code>RunnableHandler</code> to notify 
     *        once the <code>Runnable</code> has finished executing.
     *        Should not be null.
     * @throws IllegalStateException if getThread() is null or if the
     *         thread returned by getThread() is the current one.
     */
    public void safeInvokeAndWait(final Runnable r, final RunnableHandler runHandler) {
        if (runnableQueueThread == Thread.currentThread()) {
            r.run();
            runHandler.runnableInvoked(this, r);
        }

        try {
            invokeAndWait(r, runHandler);
        } catch (InterruptedException ie) {
            // We are in a bad state because the thread was interrupted while 
            // waiting for the runnable to complete.
            throw new IllegalStateException();
        }
    }


    /**
     * Schedules the given Runnable object for a later invocation, and
     * returns. The given runnable preempts any runnable that is not
     * currently executing (ie the next runnable started will be the
     * one given).  An exception is thrown if the RunnableQueue was
     * not started.  
     *
     * @param r the <code>Runnable</code> to put at the front of the 
     *        execution list.
     * @param runHandler the <code>RunnableHandler</code> to notify 
     *        once the <code>Runnable</code> has finished executing.
     *        Should not be null.
     * @throws IllegalStateException if getThread() is  null.  
     */
    public void preemptLater(final Runnable r, final RunnableHandler runHandler) {
        if (runnableQueueThread == null) {
            throw new IllegalStateException
                ("RunnableQueue not started or has exited");
        }
        synchronized (list) {
            list.unpop(new Link(r, runHandler));
            list.notify();
        }
    }

    /**
     * Waits until the given Runnable's <tt>run()</tt> has returned.
     * The given runnable preempts any runnable that is not currently
     * executing (ie the next runnable started will be the one given).
     * <em>Note: <tt>preemptAndWait()</tt> must not be called from the
     * current thread (for example from the <tt>run()</tt> method of the
     * argument).
     *
     * @param r the <code>Runnable</code> to execute
     * @param runHandler the <code>RunnableHandler</code> to notify 
     *        once the <code>Runnable</code> has finished executing.
     *        Should not be null.
     * @throws IllegalStateException if getThread() is null or if the
     *         thread returned by getThread() is the current one.
     * @throws InterruptedException if the thread is interrupted while
     *         waiting for the completion of the input <code>Runnable</code>
     *         to complete execution.
     */
    public void preemptAndWait(final Runnable r,
                               final RunnableHandler runHandler) 
        throws InterruptedException {

        if (runnableQueueThread == null) {
            throw new IllegalStateException
                ("RunnableQueue not started or has exited");
        }
        if (runnableQueueThread == Thread.currentThread()) {
            throw new IllegalStateException
                ("Cannot be called from the RunnableQueue thread");
        }

        LockableLink l = new LockableLink(r, runHandler);
        synchronized (list) {
            list.unpop(l);
            list.notify();
        }
        l.lock();
    }

    /**
     * @return this queue's state, one of RUNNING, SUSPENDING,
     *         SUSPENDED or TERMINATED
     */
    public String getQueueState() { 
        synchronized (stateLock) {
            return state; 
        }
    }

    /**
     * Suspends the execution of this queue after the current runnable
     * completes.
     * @param waitTillSuspended if true this method will not return
     *        until the queue has suspended (no runnable in progress
     *        or about to be in progress). If resumeExecution is
     *        called while waiting will simply return (this really
     *        indicates a race condition in your code).  This may
     *        return before an associated RunHandler is notified.
     * @throws IllegalStateException if getThread() is null.  
     */
    public void suspendExecution(final boolean waitTillSuspended) {
        if (runnableQueueThread == null) {
            throw new IllegalStateException
                ("RunnableQueue not started or has exited");
        }
        synchronized (stateLock) {
            if (state == SUSPENDED) {
                // already suspended...
                return;
            }

            if (state == RUNNING) {
                state = SUSPENDING;
                synchronized (list) {
                    // Wake up run thread if it is waiting for jobs,
                    // so we go into the suspended case (notifying
                    // run-handler etc...)
                    list.notify();
                }
            }

            if (waitTillSuspended) {
                try {
                    stateLock.wait();
                } catch (InterruptedException ie) { }
            }
        }
    }

    /**
     * Resumes the execution of this queue.
     * @throws IllegalStateException if getThread() is null.
     */
    public void resumeExecution() {
        if (runnableQueueThread == null) {
            throw new IllegalStateException
                ("RunnableQueue not started or has exited");
        }

        synchronized (stateLock) {
            if (state != RUNNING) {
                state = RUNNING;
                stateLock.notifyAll(); // wake it up.
                try {
                    // Wait until we have really resumed
                    stateLock.wait(); 
                } catch (InterruptedException ie) {
                    // The calling thread was interrupted
                }
            }
        }
    }

    /**
     * This interface must be implemented by an object which wants to
     * be notified of Runnable execution.
     */
    public interface RunnableHandler {
        /**
         * Called when the given Runnable has just been invoked and
         * has returned.
         *
         * @param rq the <code>RunnableQueue</code> on which the 
         *        <code>Runnable</code> was just invoked.
         * @param r the <code>Runnable</code> that just 
         *        executed.
         */
        void runnableInvoked(RunnableQueue rq, Runnable r);
    }

    /**
     * This interface must be implemented by an object which wants to 
     * be notified of the RunnableQueue's execution activity (resumed
     * or suspended.
     */
    public interface RunnableQueueHandler {
        /**
         * Called when the execution of the queue has been suspended.
         *
         * @param rq the <code>RunnableQueue</code> whose execution was
         *        suspended.
         */
        void executionSuspended(RunnableQueue rq);

        /**
         * Called when the execution of the queue has been resumed.
         *
         * @param rq the <code>RunnableQueue</code> whose execution has
         *        resumed.
         */
        void executionResumed(RunnableQueue rq);
    }

    /**
     * This implementation of the RunnableQueueHandler is used for the 
     * default RunnableQueue.
     */
    public static class VoidQueueHandler implements RunnableQueueHandler {
        /**
         * Called when the execution of the queue has been suspended.
         *
         * @param rq the <code>RunnableQueue</code> whose execution was
         *        suspended.
         */
        public void executionSuspended(RunnableQueue rq) {
            // Do nothing.
        }

        /**
         * Called when the execution of the queue has been resumed.
         *
         * @param rq the <code>RunnableQueue</code> whose execution has
         *        resumed.
         */
        public void executionResumed(RunnableQueue rq) {
            // Do nothing.
        }
    }

    /**
     * To store a Runnable.
     */
    protected static class Link extends DoublyLinkedList.Node {
        /**
         * The Runnable.
         */
        protected Runnable runnable;

        /**
         * The RunnableHandler.
         */
        protected RunnableHandler runHandler;

        /**
         * Creates a new link.
         *
         * @param r the <code>Runnable</code> this link is associated with.
         * @param runHandler the <code>RunnableHandler</code> to notify when
         *        the <code>Runnable</code> has bee executed.
         */
        public Link(final Runnable r, final RunnableHandler runHandler) {
            runnable = r;
            this.runHandler = runHandler;
        }

        /**
         * unlock link and notify locker.  
         * Basic implementation does nothing.
         *
         * @throws InterruptedException if the unlocking thread is interrupted
         *         while waiting for a notification from the locking thread
         */
        public void unlock() throws InterruptedException { return; }
    }

    /**
     * To store a Runnable with an object waiting for him to be executed.
     */
    protected static class LockableLink extends Link {

        /**
         * Whether this link is actually locked.
         */
        protected boolean locked;

        /**
         * Creates a new link.
         *
         * @param r the link's associated <code>Runnable</code>
         * @param runHandler the <code>RunnableHandler</code> to notify when
         *        the <code>Runnable</code> has bee executed.
         */
        public LockableLink(final Runnable r, final RunnableHandler runHandler) {
            super(r, runHandler);
        }

        /**
         * Locks this link.
         *
         * @throws InterruptedException if the thread is interrupted
         *         while waiting for a notification from the unlocking
         *         thread.
         */
        public synchronized void lock() throws InterruptedException {
            locked = true;
            notify();
            wait();
        }

        /**
         * Unlocks this link.
         *
         * @throws InterruptedException if the thread is interrupted while 
         *         waiting for the link to unlock
         */
        public synchronized void unlock() throws InterruptedException {
            while (!locked) {
                // Wait until lock is called...
                wait();
            }
            // Wake the locking thread...
            notify();
        }
    }
}
