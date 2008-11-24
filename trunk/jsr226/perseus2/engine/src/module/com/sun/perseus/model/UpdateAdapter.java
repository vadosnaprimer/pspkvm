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

import java.io.InputStream;

/**
 * A simple <code>UpdateListener</code> implementation which can 
 * check whether or not the document loading succeeded.
 *
 * @version $Id: UpdateAdapter.java,v 1.5 2006/04/21 06:39:49 st125089 Exp $
 */
public class UpdateAdapter implements UpdateListener {
    /**
     * Set to true if the load complete method is called
     * Set to false when loadStarting is called.
     */
    protected boolean loadComplete = false;
    
    /**
     * Set to true if the loadingFailed method is called.
     * Set to false when loadStarting is called
     */
    protected boolean loadingFailed = false;

    /**
     * Set to true if the loadStarting method is called
     */
    protected boolean loadStarting = false;

    /**
     * The last recorded loadingFailed exception
     */
    protected Exception loadingFailedException = null;

    /**
     * Invoked when a node's rendering is about to be modified
     *
     * @param node the node which is about to be modified
     */
    public void modifyingNodeRendering(ModelNode node) {
    }

    /**
     * @return true if the loading has failed.
     */
    public boolean hasLoadingFailed() {
        return loadingFailed;
    }
    
    /**
     * Invoked when a node has been inserted into the tree
     *
     * @param node the newly inserted node
     */
    public void nodeInserted(final ModelNode node) {
    }

    /**
     * Invoked when a node is about to be modified
     *
     * @param node the node which is about to be modified
     */
    public void modifyingNode(final ModelNode node) {
    }

    /**
     * Invoked when a node was just modified
     *
     * @param node the node which was modified
     */
    public void modifiedNode(final ModelNode node) {
    }

    /**
     * Invoked when the node has finished loading. 
     *
     * @param node the <code>node</code> for which loading
     *        is complete.
     */
    public void loadComplete(final ModelNode node) {
        if (node instanceof DocumentNode) {
            loadComplete = true;
        }
    }

    /**
     * Invoked when a document error happened before finishing loading.
     *
     * @param documentNode the <code>DocumentNode</code> for which loading
     *        has failed.
     * @param error the exception which describes the reason why loading
     *        failed.
     */
    public void loadingFailed(final DocumentNode documentNode, 
                              final Exception error) {
        loadingFailed = true;
        loadingFailedException = error;
    }

    /**
     * Invoked when the document starts loading
     *
     * @param documentNode the <code>DocumentNode</code> for which loading
     *        is starting
     * @param is the <code>InputStream</code> from which SVG content
     *        is loaded.
     */
    public void loadStarting(final DocumentNode documentNode, 
                             final InputStream is) {
        loadStarting = true;
        loadingFailed = false;
        loadComplete = false;
    }

    /**
     * Invoked when the input node has started loading
     *
     * @param node the <code>ModelNode</code> for which loading
     *        has started.
     */
    public void loadBegun(final ModelNode node) {
    }

    /**
     * Invoked when a string has been appended, during a load
     * phase. This is only used when parsing a document and is
     * used in support of progressive download, like the other
     * loadXXX methods.
     *
     * @param node the <code>ModelNode</code> on which text has been
     *        inserted.
     */
    public void textInserted(final ModelNode node) {
    }

    /**
     * @return true if a document loading started and then failed
     */
    public boolean loadSuccess() {
        System.err.println(">>>>>>>>>>>> loadStarting : " + loadStarting);
        System.err.println(">>>>>>>>>>>> loadComplete : " + loadComplete);
        System.err.println(">>>>>>>>>>>> loadingFailed: " + loadingFailed);

        return loadStarting && loadComplete && !loadingFailed;
    }

    /**
     * @return the last loadingFailed exception
     */
    public Exception getLoadingFailedException() {
        return loadingFailedException;
    }
}
