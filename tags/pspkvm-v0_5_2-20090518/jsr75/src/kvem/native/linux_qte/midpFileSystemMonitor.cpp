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

#include <midpFileSystemMonitor.h>
#include <midpMountedRoots.h>

#include <qfileinfo.h>

/**
 * The polling interval for mount checks in millisec.
 * The default value is 2000 for consistency with QT.
 */
#define POLLING_INTERVAL 2000

MidpFileSystemMonitor::MidpFileSystemMonitor() : QObject(0)
{
    startTimer(POLLING_INTERVAL);
}

void MidpFileSystemMonitor::timerEvent(QTimerEvent*)
{
    static QDateTime lm;

    QFileInfo fi("/etc/mtab");
    QDateTime dt = fi.lastModified();

    // determine whether 'lm' is not initialized (a first call of the method)
    if (!lm.isValid()) {
        lm = dt;
    }

    // determine whether the file is changed
    if (dt != lm) {
        lm = dt;
        MidpMountedRoots::update();
    }
}

