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
 * 
 * This source file is specific for Qt-based configurations.
 */

#ifndef _MIDP_FILESYSTEM_MONITOR_H_
#define _MIDP_FILESYSTEM_MONITOR_H_

#include <qobject.h>

/**
 * The class performs monitoring of 'mtab' file to detect
 * mounts/unmounts and sends notifications about them.
 */
class MidpFileSystemMonitor : public QObject {
public:
    /**
     * The constructor creates an instance of the class.
     * It starts polling with specified polling interval.
     */
    MidpFileSystemMonitor();
    
protected:
    /**
     * The timer event handler used to detect mounts/unmounts.
     * It's called with time interval defined by 'POLLING_INTERVAL'.
     * It checks whether 'mtab' file is changed.
     * If so, posts 'mtabChanged()' to 'QPE/Card' channel.
     */    
    void timerEvent(QTimerEvent*);
};

#endif /* _MIDP_FILESYSTEM_MONITOR_H_ */
