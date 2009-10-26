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

#ifndef _MIDP_FILESYSTEM_MONITOR_H_
#define _MIDP_FILESYSTEM_MONITOR_H_

#include <midpMountedRoots.h>


/**
 * The class performs monitoring of 'mtab' file to detect
 * mounts/unmounts and sends notifications about them.
 */
class MidpFileSystemMonitor {

public:

    /**
     * The constructor creates an instance of the class.
     */
    MidpFileSystemMonitor(MidpMountedRoots* roots);
    
    /**
     * Performs OS specific checking for a file root mount/unmount.
     */
    static void checkFileSystem();
        
protected:

    /**
     * The timer event handler used to detect mounts/unmounts.
     *
     * It's called periodically by <code>checkFileSystem</code> method
     * in order to determine whether 'mtab' file is changed. If so,
     * calls <code>MidpMountedRoots.update<code> method to update
     * the list of mounted roots.
     */    
    void timerEvent();
    
private:

    /**
     * A pointer to <code>MidpMountedRoots</code> instance to notify it
     * about mount/unmount events.
     */
    MidpMountedRoots* mRoots;    
    
    /**
     * Points to an instance of the class, if it's created.
     */
    static MidpFileSystemMonitor* monitor;
};

#endif /* _MIDP_FILESYSTEM_MONITOR_H_ */
