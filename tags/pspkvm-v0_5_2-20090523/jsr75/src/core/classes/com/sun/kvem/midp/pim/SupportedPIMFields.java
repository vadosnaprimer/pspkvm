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

package com.sun.kvem.midp.pim;


/**
 * Index of supported PIM fields and their descriptors.
 */
public class SupportedPIMFields {
    /** List of supported fields for CONTACT type. */
    public static final PIMFieldDescriptor[] CONTACT_LIST_FIELDS = {
        new PIMFieldDescriptor(Contact.NAME, PIMItem.STRING_ARRAY, false, null,
            "PIM.ContactList.Name",
            new String[] {
                "PIM.ContactList.Name.0",
                "PIM.ContactList.Name.1",
                "PIM.ContactList.Name.2",
                "PIM.ContactList.Name.3",
                "PIM.ContactList.Name.4",
            },
	    0L, -1),
        new PIMFieldDescriptor(Contact.ADDR, PIMItem.STRING_ARRAY, false, null,
            "PIM.ContactList.Addr",
            new String[] {
                "PIM.ContactList.Addr.0",
                "PIM.ContactList.Addr.1",
                "PIM.ContactList.Addr.2",
                "PIM.ContactList.Addr.3",
                "PIM.ContactList.Addr.4",
                "PIM.ContactList.Addr.5",
                "PIM.ContactList.Addr.6"
            },
            0x2a8, -1),
        new PIMFieldDescriptor(Contact.EMAIL, PIMItem.STRING, false, null,
			       "PIM.ContactList.Email", 0x2a8, -1),
        new PIMFieldDescriptor(Contact.FORMATTED_NAME, PIMItem.STRING, false,
			       null, "PIM.ContactList.FormattedName", 0L, -1),
        new PIMFieldDescriptor(Contact.FORMATTED_ADDR, PIMItem.STRING, false,
			       null, "PIM.ContactList.FormattedAddr", 
			       0x2a8, -1),
        new PIMFieldDescriptor(Contact.NICKNAME, PIMItem.STRING, false, null,
			       "PIM.ContactList.Nickname", 0L, -1),
        new PIMFieldDescriptor(Contact.NOTE, PIMItem.STRING, false, null,
			       "PIM.ContactList.Note", 0L, -1),
        new PIMFieldDescriptor(Contact.ORG, PIMItem.STRING, false, null,
			       "PIM.ContactList.Org", 0L, -1),
        new PIMFieldDescriptor(Contact.TEL, PIMItem.STRING, false, null,
			       "PIM.ContactList.Tel", 0x3ff, -1),
        new PIMFieldDescriptor(Contact.TITLE, PIMItem.STRING, false, null,
			       "PIM.ContactList.Title", 0L, -1),
        new PIMFieldDescriptor(Contact.UID, PIMItem.STRING, false, null,
			       "PIM.ContactList.UID", 0L, -1),
        new PIMFieldDescriptor(Contact.BIRTHDAY, PIMItem.DATE, false, null,
			       "PIM.ContactList.Birthday", 0L, -1),
        new PIMFieldDescriptor(Contact.REVISION, PIMItem.DATE, false, null,
			       "PIM.ContactList.Revision", 0L, 1),
        new PIMFieldDescriptor(Contact.PHOTO, PIMItem.BINARY, false, null,
			       "PIM.ContactList.Photo", 0L, -1),
        new PIMFieldDescriptor(Contact.CLASS, PIMItem.INT, false, null,
			       "PIM.ContactList.Class", 0L, -1),
        new PIMFieldDescriptor(Contact.PUBLIC_KEY, PIMItem.BINARY, false, null,
			       "PIM.ContactList.PublicKey", 0L, -1),
        new PIMFieldDescriptor(Contact.PUBLIC_KEY_STRING, PIMItem.STRING, false,
			       null, "PIM.ContactList.PublicKeyString", 0L, -1),
        new PIMFieldDescriptor(Contact.URL, PIMItem.STRING, false, null,
			       "PIM.ContactList.URL", 0L, -1)
    };
    
    /** List of supported fields for EVENT type. */
    public static final PIMFieldDescriptor[] EVENT_LIST_FIELDS = {
        new PIMFieldDescriptor(Event.LOCATION, PIMItem.STRING, false, null,
			       "PIM.EventList.Location", 0L, -1),
        new PIMFieldDescriptor(Event.NOTE, PIMItem.STRING, false, null,
			       "PIM.EventList.Note", 0L, -1),
        new PIMFieldDescriptor(Event.SUMMARY, PIMItem.STRING, false, null,
			       "PIM.EventList.Summary", 0L, -1),
        new PIMFieldDescriptor(Event.UID, PIMItem.STRING, false, null,
			       "PIM.EventList.UID", 0L, -1),
        new PIMFieldDescriptor(Event.END, PIMItem.DATE, false, null,
			       "PIM.EventList.End", 0L, -1),
        new PIMFieldDescriptor(Event.REVISION, PIMItem.DATE, false, null,
			       "PIM.EventList.Revision", 0L, 1),
        new PIMFieldDescriptor(Event.START, PIMItem.DATE, false, null,
			       "PIM.EventList.Start", 0L, -1),
        new PIMFieldDescriptor(Event.ALARM, PIMItem.INT, false, null,
			       "PIM.EventList.Alarm", 0L, -1),
        new PIMFieldDescriptor(Event.CLASS, PIMItem.INT, false, null,
			       "PIM.EventList.Class", 0L, -1)
    };
    
    /** List of supported fields for ToDo type. */
    public static final PIMFieldDescriptor[] TODO_LIST_FIELDS = {
        new PIMFieldDescriptor(ToDo.NOTE, PIMItem.STRING, false, null,
			       "PIM.ToDoList.Note", 0L, -1),
        new PIMFieldDescriptor(ToDo.SUMMARY, PIMItem.STRING, false, null,
			       "PIM.ToDoList.Summary", 0L, -1),
        new PIMFieldDescriptor(ToDo.UID, PIMItem.STRING, false, null,
			       "PIM.ToDoList.UID", 0L, -1),
        new PIMFieldDescriptor(ToDo.CLASS, PIMItem.INT, false, null,
			       "PIM.ToDoList.Class", 0L, -1),
        new PIMFieldDescriptor(ToDo.PRIORITY, PIMItem.INT, false, null,
			       "PIM.ToDoList.Priority", 0L, -1),
        new PIMFieldDescriptor(ToDo.COMPLETION_DATE, PIMItem.DATE, false, null,
			       "PIM.ToDoList.CompletionDate", 0L, -1),
        new PIMFieldDescriptor(ToDo.DUE, PIMItem.DATE, false, null,
			       "PIM.ToDoList.Due", 0L, -1),
        new PIMFieldDescriptor(ToDo.REVISION, PIMItem.DATE, false, null,
			       "PIM.ToDoList.Revision", 0L, 1),
        new PIMFieldDescriptor(ToDo.COMPLETED, PIMItem.BOOLEAN, false, null,
			       "PIM.ToDoList.Completed", 0L, -1)
    };
}
