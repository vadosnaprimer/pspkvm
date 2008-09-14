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

#include <midpFileSystemMonitor.h>
#include <pcsl_directory.h>

/** Use NULL to be most consistent with the MIDP and CLDC workspaces. */
#ifndef NULL
#define NULL 0
#endif

/**
 * The system file that stores file system mount table.
 *
 * The value is "/etc/mtab".
 */
#define STATIC_ASCII_LITERAL_SYS_MOUNTS  {'/','e','t','c','/','m','t','a','b','\0'}

PCSL_DEFINE_STATIC_ASCII_STRING_LITERAL_START(pcslSysMounts)
 STATIC_ASCII_LITERAL_SYS_MOUNTS
PCSL_DEFINE_STATIC_ASCII_STRING_LITERAL_END(pcslSysMounts);


MidpFileSystemMonitor* MidpFileSystemMonitor::monitor = NULL;


void MidpFileSystemMonitor::checkFileSystem()
{
    if (MidpFileSystemMonitor::monitor != NULL) {
        MidpFileSystemMonitor::monitor->timerEvent();
    }
}

MidpFileSystemMonitor::MidpFileSystemMonitor(MidpMountedRoots* roots)
        : mRoots(roots) {

    monitor = this;
}

void MidpFileSystemMonitor::timerEvent()
{
    static long last = 0;
    long time;

    if (pcsl_file_get_time(&pcslSysMounts,
            PCSL_FILE_TIME_LAST_MODIFIED, &time) == 0) {

        /* first timer event */
        if (last == 0) {
            last = time;
        }
        /* determine whether the file is changed */
        if (time != last) {
            last = time;
            mRoots->update();
        }
    }
}
