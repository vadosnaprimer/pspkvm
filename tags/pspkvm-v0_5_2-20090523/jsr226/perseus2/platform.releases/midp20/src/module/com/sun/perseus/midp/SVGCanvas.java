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

package com.sun.perseus.midp;

import com.sun.perseus.builder.ModelBuilder;

import com.sun.perseus.model.SimpleCanvasManager;
import com.sun.perseus.model.CanvasUpdateListener;
import com.sun.perseus.model.DocumentNode;
import com.sun.perseus.model.ModelEvent;
import com.sun.perseus.model.ModelNode;
import com.sun.perseus.model.SMILSample;
import com.sun.perseus.model.Time;

import com.sun.perseus.j2d.RenderGraphics;

import com.sun.perseus.util.SVGConstants;
import com.sun.perseus.util.RunnableQueue;

import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;

import javax.microedition.m2g.SVGEventListener;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;

import com.sun.pisces.PiscesRenderer;
import com.sun.pisces.RendererBase;
import com.sun.pisces.NativeSurface;
import com.sun.pisces.GraphicsSurfaceDestination;

/**
 * This class provides support for an LCDUI Canvas extension which can display
 * an SVG Document.
 *
 * @version $Id: SVGCanvas.java,v 1.16 2006/04/21 06:40:56 st125089 Exp $
 */
class SVGCanvas
    extends Canvas implements CanvasUpdateListener {
    /**
     * Color used to clear the canvas' background.
     */
    public static final int CLEAR_COLOR = 0xffffffff;

    /**
     * Initial state.
     */
    public final static int STATE_STOPPED = 1;

    /**
     * Playing state, i.e., playing animations and repainting buffer.
     */
    public final static int STATE_PLAYING = 2;

    /**
     * Paused state, i.e., repainting buffer but no longer advancing the 
     * time.
     */
    public final static int STATE_PAUSED = 3;

    /**
     * SMIL Animation's frame length, in milliseconds
     */
    public static final int SMIL_ANIMATION_FRAME_LENGTH = 1000;

    /**
     * Last x position on a pointer pressed event.
     */
    protected int lastX;

    /**
     * Last y position on a pointer pressed event.
     */
    protected int lastY;

    /**
     * True if the last pointer event was a pointer pressed event.
     */
    protected boolean lastWasPressed;
    
    /**
     * The current player state.
     */
    protected int state = STATE_STOPPED;

    /**
     * The <code>SimpleCanvasManager</code> manages the area where the SVG
     * content is rendered.
     */
    protected SimpleCanvasManager canvasManager;

    /**
     * This component displays a DocumentNode object, which
     * is built from the URI
     */
    protected DocumentNode documentNode;

    /**
     * Offscreen image
     */
    protected NativeSurface offscreen;

    /**
     * Used to blit the offscreen onto the graphics destination.
     */
    protected GraphicsSurfaceDestination gsd;

    /**
     * Offscreen width
     */
    protected int offscreenWidth;

    /**
     * Offscreen height
     */
    protected int offscreenHeight;

    /**
     * The PiscesRenderer associated with the offscreen.
     */
    protected PiscesRenderer pr;

    /**
     * RenderGraphics used to draw into the current offscreen
     */
    protected RenderGraphics rg;

    /**
     * The associated SVGEventListener.
     */
    protected SVGEventListener svgEventListener;

    /**
     * The RunnableQueue is the _only_ valid way to access the
     * model tree. No access to the model should be done other
     * than from the RunnableQueue thread.
     */
    protected RunnableQueue updateQueue = null;

    /**
     * The animation sampler, which runs animations in the update thread.
     */
    protected SMILSample smilSample = null;

    /**
     * The animation clock.
     */
    protected SMILSample.DocumentWallClock clock = null;

    /**
     * The time increment for the animation.
     */
    protected float timeIncrement = 0.1f;

    /**
     * The last mouse event target.
     */
    protected ModelNode lastMouseTarget = null;

    /**
     * Boolean flag used to control when the SVGCanvas ignores a 
     * canvas manager update because it asked for a a full paint
     * in response to a prior repaint. This avoid queuing an extra
     * initial repaint() when building a new offscreen buffer.
     */
    private boolean ignoreCanvasUpdate = false;

    /**
     * @param documentNode the documentNode this component will render. The input
     *        DocumentNode must be fully loaded before this method is called.
     *        Note: if the DocumentNode already has an associated RunnableQueue,
     *        it is simply replaced. It is the responsibility of the caller to 
     *        stop that RunnableQueue if need be.
     * @throws IllegalArgumentException see {@link #setURI setURI}.
     */
    public SVGCanvas(final DocumentNode documentNode) {
        if (documentNode == null) {
            throw new NullPointerException();
        }

        if (!documentNode.isLoaded()) {
            throw new IllegalStateException();
        }

        this.documentNode = documentNode;

        // Set-up RunnableQueue
        updateQueue = RunnableQueue.getDefault();
        documentNode.setUpdateQueue(updateQueue);

        // Hook in the SimpleCanvasManager after creating the offscreen buffer.
        buildOffscreen(1, 1);
        canvasManager = new SimpleCanvasManager(rg,
                                                documentNode, 
                                                this);
        canvasManager.turnOff(); // disabled until we call play or pause.
        documentNode.setRunnableHandler(canvasManager);

        // Create a SMILSample instance that will be scheduled with the 
        // RunnableQueue whenever the component plays.
        clock = new SMILSample.DocumentWallClock(documentNode);
        smilSample = new SMILSample(documentNode, clock);

        // Initialize the timing engine.
        documentNode.initializeTimingEngine();

        // Apply animations at time 0
        documentNode.sample(new Time(0));
        documentNode.applyAnimations();
    }

    /**
     * @see javax.microedition.lcdui.Canvas#paint
     */
    protected void paint(final Graphics g) {    
        checkOffscreen();
        int x = g.getClipX();
        int y = g.getClipY();
        int w = g.getClipWidth();
        int h = g.getClipHeight();

        synchronized (canvasManager.lock) {
            if (x!= 0 
                || 
                y != 0 
                || 
                w != documentNode.getWidth()
                ||
                h != documentNode.getHeight()) {
                // The repaint area is not exactly the same as the viewport area
                // so we need to clear the background first.
                g.setColor(CLEAR_COLOR);
                g.fillRect(x, y, w, h);
            }

            if (gsd == null) {
                gsd = new GraphicsSurfaceDestination(g);
            }
            gsd.drawSurface(offscreen, 0, 0, 0, 0, offscreenWidth, offscreenHeight, 1);
            canvasManager.consume();
        }
    }

    /**
     * Checks if the offscreen buffer needs to be built or rebuilt.
     */
    protected void checkOffscreen() {
        if (offscreen == null) {
            // This is the very first time we build an offscreen.
            buildOffscreen(getWidth(), getHeight());
        } else {            
            // Check that the offscreen is large enough for the current size.
            int width = getWidth();
            int height = getHeight();

            // We use an offscreen size with is the smallest of the viewport
            // size and the canvas size. 
            if (width > documentNode.getWidth()) {
                width = documentNode.getWidth();
            }

            if (height > documentNode.getHeight()) {
                height = documentNode.getHeight();
            }

            if (width != offscreenWidth
                ||
                height != offscreenHeight) {
                buildOffscreen(width, height);
            }
        }
    }

    /**
     * The offscreen buffer has the size of the component. This method
     * is called in the MIDP painting thread.
     *
     * @param width the requested minimum buffer width
     * @param height the requested minimum buffer height
     */
    protected void buildOffscreen(final int width, 
                                  final int height) {
        if (width > 0 && height > 0) {
            // We build an offscreen of the requested size.
            offscreen = new NativeSurface(width, height);
            offscreenWidth = width;
            offscreenHeight = height;
        } else {
            // This is a degenerate case, just build with 1x1 offscreen
            if (offscreenWidth == 1 && offscreenHeight == 1) {
                return;
            }

            offscreen = new NativeSurface(1, 1);
            offscreenWidth = 1;
            offscreenHeight = 1;
        }

        // Build a new PiscesRenderer for the new rendering surface.
        pr = new PiscesRenderer(offscreen,
                                offscreenWidth, offscreenHeight, 0, offscreenWidth, 1,
                                RendererBase.TYPE_INT_ARGB);
        
        // Build a corresponding RenderGraphics
        rg = new RenderGraphics(pr, offscreenWidth, offscreenHeight);
        
        if (canvasManager != null) {
            // We need to force painting the offscreen buffer.
            // Offscreen buffer rendering happens in the update 
            // thread.
            try {
                updateQueue.invokeAndWait(new Runnable() {
                        public void run() {
                            synchronized (canvasManager.lock) {
                                // Automatically adjust the SVG image's viewport size.
                                documentNode.setSize(width, height);
                                
                                // Switch the SimpleCanvasManager to the new RenderGraphics
                                canvasManager.setRenderGraphics(rg);
                                
                                // Set the consumed flag to true to force painting 
                                // immediately.
                                canvasManager.consume();
                            }

                            // Now, update the new canvas.
                            // We set the ignoreCanvasUpdate flag to true so that the 
                            // canvas update does not trigger a repaint() request.
                            ignoreCanvasUpdate = true;
                            canvasManager.updateCanvas();
                            ignoreCanvasUpdate = false;
                        }
                    }, null);
            } catch (InterruptedException ie) {
                // This is a serious error, because it means the 
                // default Runnable Queue thread has been 
                // interrupted.                    
                ie.printStackTrace();
            }
        } else {
            pr.setColor(255, 255, 255);
            pr.clearRect(0, 0, offscreenWidth, offscreenHeight);
        }
    }

    // ========================================================================
    // CanvasUpdateListener implementation
    // ========================================================================

    /**
     * Invoked by the <code>SimpleCanvasManager</code> when it is done updating the
     * canvas. This is used during the progressive rendering loading phase and
     * when a Runnable has been invoked on the RunnableQueue associated with the
     * SVG image. This method is called in the RunnableQueue thread.
     *
     * @param canvasManager the <code>SimpleCanvasManager</code> which is reporting
     *        the update.
     */
    public void updateComplete(final Object canvasManager) {
        if (!ignoreCanvasUpdate) {
            repaint(0, 0, documentNode.getWidth(), documentNode.getHeight());
        } 
    }

    /**
     * Called by the <code>SimpleCanvasManager</code> when the initial load is
     * complete. This method is called in the RunnableQueue thread.
     *
     * @param e if not null, it means that the initial load failed due to
     *          this exception.
     */
    public void initialLoadComplete(final Exception e) {
        if (e != null) {
            e.printStackTrace();
        }
    }

    // ========================================================================

    /**
     * Event Listeners used to turn MIDP Events into DOM Events. It also
     * switches between the MIDP event thread and the document's update
     * thread (i.e., the <code>RunnableQueue</code>'s thread.
     */

    /**
     * Invoked when a mouse button has been pressed on a component.
     * @param x the x-axis coordinate of the pointer event
     * @param y the y-axis coordinate of the pointer event
     */
    protected void pointerPressed(final int x, final int y) {
        if (svgEventListener != null) {
            svgEventListener.pointerPressed(x, y);
        }

        lastX = x;
        lastY = y;
        lastWasPressed = true;

        float[] pt = {x, y};
        dispatchPointerEvent(SVGConstants.SVG_MOUSEDOWN_EVENT_TYPE, pt);
    }
    
    /**
     * Invoked when a mouse button has been released on a component.
     * @param x the x-axis coordinate of the pointer event
     * @param y the y-axis coordinate of the pointer event
     */
    protected void pointerReleased(final int x, final int y) {
        if (svgEventListener != null) {
            svgEventListener.pointerReleased(x, y);
        }

        float[] pt = {x, y};
        dispatchPointerEvent(SVGConstants.SVG_MOUSEUP_EVENT_TYPE, pt);

        if (lastWasPressed && lastX == x && lastY == y) {
            dispatchPointerEvent(SVGConstants.SVG_CLICK_EVENT_TYPE, pt);
        }

        lastWasPressed = false;
    }

    /**
     * Dispatches a mouse event to the DOM tree.
     *
     * @param eventType the DOM event type.
     * @param pt the mouse event coordinates.
     */
    protected void dispatchPointerEvent(final String eventType,
                                        final float[] pt) {
        if (state == STATE_STOPPED) {
            return;
        }

        invokeLater(new Runnable() {
                public void run() {
                    ModelNode target = documentNode.nodeHitAt(pt);
                    if (target == null) {
                        target = documentNode;
                    }
                    
                    // If the target is different from the lastMouseTarget
                    // dispatch a 'mouseout' event to the lastMouseTarget
                    // and dispatch a 'mouseover' to the new target
                    if (lastMouseTarget != target) {
                        if (lastMouseTarget != null && lastMouseTarget != documentNode) {
                            ModelEvent e = 
                                new ModelEvent(SVGConstants.SVG_MOUSEOUT_EVENT_TYPE, 
                                               lastMouseTarget);
                            documentNode.dispatchEvent(e);
                        }
                        ModelEvent e = 
                            new ModelEvent(SVGConstants.SVG_MOUSEOVER_EVENT_TYPE, 
                                           target);
                        documentNode.dispatchEvent(e);
                        lastMouseTarget = target;
                    }
                    
                    // Map the event type
                    // Build the DOM Event
                    ModelEvent evt = new ModelEvent(eventType, target);
                    
                    // Dispatch to the target tree
                    documentNode.dispatchEvent(evt);
                }
            });
    }
          

    /**
     * Invoked when a key has been pressed.
     * @param keyCode the code of the event key
     */ 
    protected void keyPressed(int keyCode) {
        if (svgEventListener != null) {
            svgEventListener.keyPressed(keyCode);
        }
        dispatchKeyEvent(SVGConstants.SVG_KEYDOWN_EVENT_TYPE,
                         keyCode);
    }

    /**
     * Dispatches a key event to the DOM tree.
     *
     * @param eventType the DOM event type.
     * @param keyCode the key code.
     */
    protected void dispatchKeyEvent(final String eventType,
                                    final int keyCode) {
        Runnable r = new Runnable() {
                public void run() {
                    documentNode.dispatchEvent
                        (new ModelEvent(eventType,
                                        documentNode, (char) keyCode));
                }
            };

        if (state != STATE_STOPPED) {
            invokeLater(r);
        }
    }
          
    /**
     * Invoked when a key has been released.
     * @param keyCode the code of the event key
     */ 
    protected void keyReleased(int keyCode) {
        if (svgEventListener != null) {
            svgEventListener.keyReleased(keyCode);
        }
        dispatchKeyEvent(SVGConstants.SVG_KEYUP_EVENT_TYPE,
                         keyCode);
    }

    /**
     * Invoked when the component's size changes.
     *
     * @param w the new width
     * @param h the new height
     */
    protected void sizeChanged(final int w, final int h) {
        if (svgEventListener != null) {
            svgEventListener.sizeChanged(w, h);
        }
    }

    /**
     * Invoked when the component is hidden.
     */
    protected void hideNotify() {
        if (svgEventListener != null) {
            svgEventListener.hideNotify();
        }
    }
    
    /**
     * Invoked when the component is shown.
     */
    protected void showNotify() {
        if (svgEventListener != null) {
            svgEventListener.showNotify();
        }
    }
    
    // ========================================================================

    /**
     * Associate the specified <code>SVGEventListener</code> with this
     * <code>SVGAnimator</code>.
     *
     * @param svgEventListener the SVGEventListener that will receive
     *        events forwarded by this <code>SVGAnimator</code>. If null,
     *        events will not be forwarded by the <code>SVGAnimator</code>.
     */
    public void setSVGEventListener(SVGEventListener svgEventListener) {
        this.svgEventListener = svgEventListener;
    }

    /**
     * Set the time increment to be used for animation rendering.
     *
     * @param timeIncrement the minimal period of time, in seconds, that
     *         should elapse between frame. Must be greater than zero.
     * @throws IllegalArgumentException if timeIncrement is less than or equal to
     *         zero.
     * @see #getTimeIncrement
     */
    public void setTimeIncrement(float timeIncrement) {
        if (timeIncrement <= 0) {
            throw new IllegalArgumentException();
        }

        this.timeIncrement = timeIncrement;

        if (state == STATE_PLAYING) {
            updateQueue.unschedule(smilSample);            
            updateQueue.scheduleAtFixedRate(smilSample, canvasManager, (long) (1000 * timeIncrement));
        }
    }

    /**
     * Get the current time increment for animation rendering. The
     * SVGAnimator increments the SVG document's current time by this amount
     * upon each rendering. The default value is 0.1 (100 milliseconds).
     *
     * @return the current time increment, in seconds, used for animation
     *         rendering.
     * @see #setTimeIncrement
     */
    public float getTimeIncrement() {
        return timeIncrement;
    }

    /**
     * Transition this <code>SVGAnimator</code> to the <i>playing</i>
     * state. In the <i>playing</i> state, both Animation and SVGImage
     * updates cause rendering updates. Note that in the playing state,
     * when the document's current time changes, the animator will seek
     * to the new time, and continue to play animations from this place.
     *
     * @throws IllegalStateException if the animator is not currently in
     *         the <i>stopped</i> or <i>paused</i> state.
     */
    public void play() {
        if (state == STATE_PLAYING) {
            throw new IllegalStateException
                (Messages.formatMessage(Messages.ERROR_INVALID_STATE,
                                        new Object[] {getClass().getName(),
                                                      stateToString(),
                                                      "play()",
                                                      "stopped, paused"}));
        }

        // Mark the document as playing.
        updateQueue.preemptLater(new Runnable() {
                public void run() {
                    documentNode.setPlaying(true);
                }
            }, canvasManager);

        // Now, schedule the SMILSampler
        clock.start();
        updateQueue.scheduleAtFixedRate(smilSample, canvasManager, (long) (1000 * timeIncrement));

        state = STATE_PLAYING;

        // Turn on any updates to the offscreen canvas.
        canvasManager.turnOn();
    }

    /**
     * Transition this <code>SVGAnimator</code> to the <i>paused</i> state.
     * The <code>SVGAnimator</code> stops advancing the document's current time
     * automatically (see the SVGDocument's setCurrentTime method). In consequence,
     * animation playback will be paused until another call to the <code>play</code> method
     * is made, at which points animations will resume from the document's current
     * time. SVGImage updates (through API calls) cause a rendering update
     * while the <code>SVGAnimator</code> is in the <i>paused</i> state.
     *
     * @throws IllegalStateException if the animator is not in the <i>playing</i>
     *         state.
     */
    public void pause() {
        if (state != STATE_PLAYING) {
            throw new IllegalStateException
                (Messages.formatMessage(Messages.ERROR_INVALID_STATE,
                                        new Object[] {getClass().getName(),
                                                      stateToString(),
                                                      "pause()",
                                                      "playing"}));
        }

        state = STATE_PAUSED;
        
        // Mark the document as _not_ playing.
        updateQueue.preemptLater(new Runnable() {
                public void run() {
                    documentNode.setPlaying(false);
                }
            }, canvasManager);

        // Remove the SMILSampler
        updateQueue.unschedule(smilSample);

        // Turn on any updates to the offscreen canvas.
        canvasManager.turnOn();

    }

    /**
     * Transition this <code>SVGAnimator</code> to the <i>stopped</i> state.
     * In this state, no rendering updates are performed.
     *
     * @throws IllegalStateException if the animator is not in the <i>playing</i>
     *         or <i>paused</i> state.
     */
    public void stop() {
        if (state == STATE_STOPPED) {
            throw new IllegalStateException
                (Messages.formatMessage(Messages.ERROR_INVALID_STATE,
                                        new Object[] {getClass().getName(),
                                                      stateToString(),
                                                      "stop()",
                                                      "paused, playing"}));
        }
        
        state = STATE_STOPPED;
        
        // Remove the SMILSampler
        updateQueue.unschedule(smilSample);

        // Mark the document as _not_ playing.
        documentNode.setPlaying(false);

        // To unlock the canvasManager if it is waiting on the 
        // consumed flag.
        canvasManager.consume();

        // Turn off any updates to the offscreen canvas.
        canvasManager.turnOff();
    }

    /**
     * Invoke the Runnable in the Document update thread and 
     * return only after this Runnable has finished.
     *
     * @param runnable the new Runnable to invoke.
     * @throws InterruptedException if the current thread is waiting,
     * sleeping, or otherwise paused for a long time and another thread
     * interrupts it.
     * @throws NullPointerException if <code>runnable</code> is null.
     * @throws IllegalStateException if the animator is in the <i>stopped</i> state.
     */
    void invokeAndWait(Runnable runnable) throws InterruptedException {
        if (runnable == null) {
            throw new NullPointerException();
        }
        
        if (state == STATE_STOPPED) {
            throw new IllegalStateException
                (Messages.formatMessage(Messages.ERROR_INVALID_STATE,
                                        new Object[] {getClass().getName(),
                                                      stateToString(),
                                                      "invokeAndWait()",
                                                      "paused, playing"}));
        }

        updateQueue.invokeAndWait(runnable, canvasManager);
    }

    /**
     * Schedule execution of the input Runnable in the update thread at a later time.
     *
     * @param runnable the new Runnable to execute in the Document's update
     * thread when time permits.
     * @throws NullPointerException if <code>runnable</code> is null.
     * @throws IllegalStateException if the animator is in the <i>stopped</i> state.
     */
    void invokeLater(Runnable runnable) {
        if (runnable == null) {
            throw new NullPointerException();
        }
        
        if (state == STATE_STOPPED) {
            throw new IllegalStateException
                (Messages.formatMessage(Messages.ERROR_INVALID_STATE,
                                        new Object[] {getClass().getName(),
                                                      stateToString(),
                                                      "invokeLater()",
                                                      "paused, playing"}));
        }

        updateQueue.invokeLater(runnable, canvasManager);
    }

    /**
     * Helper method. Converts the current state to a String.
     */
    String stateToString() {
        switch (state) {
        case STATE_PLAYING:
            return "playing";
        case STATE_PAUSED:
            return "paused";
        case STATE_STOPPED:
        default:
            return "stopped";
        }
    }

}
