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

/**
 * A simple Doubly Linked list class, designed to avoid
 * O(n) behaviour on insert and delete.
 *
 * @author <a href="mailto:deweese@apache.org">Thomas DeWeese</a>
 * @version $Id: DoublyLinkedList.java,v 1.2 2006/04/21 06:35:47 st125089 Exp $
 */
public class DoublyLinkedList {

    // ========================================================================
    // Inner static Node class
    // ========================================================================

    /**
     * Basic doubly linked list node interface.
     */
    public static class Node {
        /**
         * This node's next neighbour
         */
        private Node next = null;

        /**
         * This node's previous neighbour
         */
        private Node prev = null;
        
        /**
         * @return this node's next neighbour in the list
         */
        public final Node getNext() { 
            return next; 
        }

        /**
         * @return this node's previous neighbour
         */
        public final Node getPrev() { 
            return prev; 
        }
        
        /**
         * @param newNext the new next node
         */
        protected final void setNext(final Node newNext) { 
            next = newNext; 
        }

        /**
         * @param newPrev the new previous node
         */
        protected final void setPrev(final Node newPrev) { 
            prev = newPrev; 
        }
        
        /**
         * Unlink this node from it's current list...
         */
        protected final void unlink() {
            if (getNext() != null) {
                getNext().setPrev(getPrev());
            }

            if (getPrev() != null) {
                getPrev().setNext(getNext());
            }
            setNext(null);
            setPrev(null);
        }
        
        /**
         * Link this node in, infront of nde (unlinks it's self
         * before hand if needed).
         * @param nde the node to link in before.
         */
        protected final void insertBefore(final Node nde) {
            // Already here...
            if (this == nde) {
                return;
            }
            
            if (getPrev() != null) {
                unlink();
            }

            // Actually insert this node...
            if (nde == null) {
                // empty lst...
                setNext(this);
                setPrev(this);
            } else {
                setNext(nde);
                setPrev(nde.getPrev());
                nde.setPrev(this);
                if (getPrev() != null) {
                    getPrev().setNext(this);
                }
            }
        }
    }

    // ========================================================================
    // End of inner static Node class
    // ========================================================================
    

    /**
     * The list's head. The previous node is the tail of the list
     */
    private Node head = null;

    /**
     * The list's size
     */
    private int  size = 0;
    
    /**
     * Default constructor
     */
    public DoublyLinkedList() {
    }
            
    /**
     * @return the number of elements currently in the list.
     */
    public int getSize() { 
        return size; 
    }

    /**
     * Removes all elements from the list.
     */
    public void empty() {
        while (size > 0) {
            pop();
        }
    }
            
    /**
     * Get the current head element
     * @return The current 'first' element in list.
     */
    public Node getHead() { 
        return head; 
    }

    /**
     * Get the current tail element
     * @return The current 'last' element in list.
     */
    public Node getTail() { 
        if (head != null) {
            return head.getPrev(); 
        } else {
            return null;
        }
    }

    /**
     * Adds <code>nde</code> to the head of the list.
     * In perl this is called an 'unpop'.  <code>nde</code> should
     * not currently be part of any list.
     * @param nde the node to add to the list.
     */
    public void add(final Node nde) {
        nde.insertBefore(head);
        head = nde;
        size++;
    }
        
    /**
     * Removes nde from the list it is part of (should be this
     * one, otherwise results are undefined).  If nde is the
     * current head element, then the next element becomes head,
     * if there are no more elements the list becomes empty.
     * @param nde <code>Node</code> to remove.
     */
    public void remove(final Node nde) {
        if (nde == head) {
            if (head.getNext() == head) {
                head = null;  // Last node...
            } else {
                head = head.getNext();
            }
        }
        nde.unlink();
        size--;
    }

    /**
     * Removes 'head' from list and returns it. Returns null if list is empty.
     * @return current head element, next element becomes head.
     */
    public Node pop() {
        if (head == null) {
            return null;
        }
            
        Node nde = head;
        remove(nde);
        return nde;
    }

    /**
     * Removes 'tail' from list and returns it. Returns null if list is empty.
     * @return current tail element.
     */
    public Node unpush() {
        if (head == null) {
            return null;
        }
            
        Node nde = getTail();
        remove(nde);
        return nde;
    }



    /**
     * @param nde the <code>Node</code> to add to tail of list
     */
    public void push(final Node nde) {
        nde.insertBefore(head);
        if (head == null) {
            head = nde;
        }
        size++;
    }

    /**
     * @param nde the <code>Node</code> to put to the head of list
     */
    public void unpop(final Node nde) {
        nde.insertBefore(head);
        head = nde;
        size++;
    }
}

