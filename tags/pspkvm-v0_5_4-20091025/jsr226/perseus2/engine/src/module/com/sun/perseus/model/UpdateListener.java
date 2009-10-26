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
 * An <code>UpdateListener</code> implementation is responsible for handling
 * updates to a <code>ModelNode</code> tree and to take appropriate actions,
 * like collecting dirty state, dirty area or triggering repaint events.
 *
 * @version $Id: UpdateListener.java,v 1.3 2006/04/21 06:39:51 st125089 Exp $
 */
public interface UpdateListener {
    /**
     * Invoked when a node's rendering is about to be modified
     *
     * @param node the node which is about to be modified
     */
    void modifyingNodeRendering(ModelNode node);

    /**
     * Invoked when a node has been inserted into the tree
     *
     * @param node the newly inserted node
     */
    void nodeInserted(ModelNode node);

    /**
     * Invoked when a node is about to be modified
     *
     * @param node the node which is about to be modified
     */
    void modifyingNode(ModelNode node);

    /**
     * Invoked when a node modification completed
     *
     * @param node the node which was modified
     */
    void modifiedNode(ModelNode node);

    /**
     * Invoked when the input node has finished loading. 
     *
     * @param node the <code>ModelNode</code> for which loading
     *        is complete.
     */
    void loadComplete(ModelNode node);

    /**
     * Invoked when the input node has started loading
     *
     * @param node the <code>ModelNode</code> for which loading
     *        has started.
     */
    void loadBegun(ModelNode node);

    /**
     * Invoked when a string has been appended, during a load
     * phase. This is only used when parsing a document and is
     * used in support of progressive download, like the other
     * loadXXX methods.
     *
     * @param node the <code>ModelNode</code> on which text has been
     *        inserted.
     */
    void textInserted(ModelNode node);

    /**
     * Invoked when a document error happened before finishing loading.
     *
     * @param documentNode the <code>DocumentNode</code> for which loading
     *        has failed.
     * @param error the exception which describes the reason why loading
     *        failed.
     */
    void loadingFailed(DocumentNode documentNode, Exception error);

    /**
     * Invoked when the document starts loading
     *
     * @param documentNode the <code>DocumentNode</code> for which loading
     *        is starting
     * @param is the <code>InputStream</code> from which SVG content
     *        is loaded.
     */
    void loadStarting(DocumentNode documentNode, InputStream is);
}
