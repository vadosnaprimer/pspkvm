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
 * <p>The <code>CanvasUpdateListener</code> should be implemented by classes
 * which want to listen to updates done by the <code>CanvasManager</code> on
 * a <code>RenderGraphics</code>. Typically, this is done so that an application
 * can listen to updates done on an offscreen buffer so that offscreen can be
 * redisplayed on screen.
 *
 * @version $Id: CanvasUpdateListener.java,v 1.3 2006/04/21 06:36:35 st125089 Exp $
 */
public interface CanvasUpdateListener {
    /**
     * Called by a <code>CanvasManager</code> whenever an update has
     * been done on its associated <code>RenderGraphics</code>
     *
     * @param canvasManager the <code>CanvasManager</code> which just completed
     *        an update.
     */
    void updateComplete(Object canvasManager);

    /**
     * Called by the <code>CanvasManager</code> when the initial load is
     * complete.
     *
     * @param e if not null, it means that the initial load failed due to
     *          this exception.
     */
    void initialLoadComplete(Exception e);
    
}
