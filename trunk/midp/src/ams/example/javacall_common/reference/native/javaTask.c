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

#include <stdio.h>
#include <string.h>
#include <ctype.h>
#include <midp_logging.h>
#include <midpAMS.h>
#include <suitestore_common.h>
#include <midpMalloc.h>
#include <jvm.h>
#include <jvmspi.h>
#include <findMidlet.h>
#include <midpUtilKni.h>
#include <midp_jc_event_defs.h>
#include <midp_properties_port.h>
#include <javacall_events.h>
#include <javacall_lifecycle.h>
#include <midpStorage.h>
#include <suitestore_task_manager.h>
#include <commandLineUtil.h>
#include <javaTask.h>
#include <exe_entry_point.h>
#include <javacall_lifecycle.h>

static javacall_result midpHandleSetVmArgs(int argc, char* argv);
static javacall_result midpHandleSetHeapSize(midp_event_heap_size heap_size);
static javacall_result midpHandleListMIDlets(void);
static javacall_result midpHandleListStorageNames(void);
static javacall_result midpHandleRemoveMIDlet(midp_event_remove_midletremoveMidletEvent);



/**
 * An entry point of a thread devoted to run java
 */
void JavaTask(void) {
    static unsigned long binaryBuffer[BINARY_BUFFER_MAX_LEN/sizeof(long)];
    midp_jc_event_union *event;
    javacall_bool res = JAVACALL_OK;
    javacall_bool JavaTaskIsGoOn = JAVACALL_TRUE;
    long timeTowaitInMillisec = -1;
    int binaryBufferMaxLen = BINARY_BUFFER_MAX_LEN;
    int outEventLen;
    int heapsize;

    REPORT_CRIT(LC_CORE,"JavaTask() >>\n");

    if (JAVACALL_OK != javacall_initialize_configurations()) {
        REPORT_WARN(LC_CORE,"JavaTask() >> configuration initialize failed.\n");
    }
    
    if (midpInitializeMemory(2*1280*1024+1024*1024) != 0) {
        REPORT_CRIT(LC_CORE,"JavaTask() >> midpInitializeMemory()  Not enough memory.\n");
        return;
    }
    REPORT_INFO(LC_CORE,"JavaTask() >> memory initialized.\n");

    //javacall_global_init();
    javacall_events_init();
    javacall_keymap_init();

    /* Set Java heap size according to system heap size */
    heapsize = javacall_total_heap_size();
    heapsize -= 1024*1024;
    heapsize -= (heapsize/32);
    JVM_SetConfig(JVM_CONFIG_HEAP_CAPACITY, heapsize);
    JVM_SetConfig(JVM_CONFIG_HEAP_MINIMUM, heapsize);
    REPORT_INFO1(LC_CORE,"JavaTask() >> Java heap set to %d bytes.\n", heapsize);

    /* Outer Event Loop */
    while (JavaTaskIsGoOn) {        
        res = javacall_event_receive(timeTowaitInMillisec,
            (unsigned char *)binaryBuffer, binaryBufferMaxLen, &outEventLen);

        if (!JAVACALL_SUCCEEDED(res)) {
            REPORT_ERROR(LC_CORE,"JavaTask() >> Error javacall_event_receive()\n");
            continue;
        }

        event = (midp_jc_event_union *) binaryBuffer;

        switch (event->eventType) {
        case MIDP_JC_EVENT_START_ARBITRARY_ARG:
            REPORT_INFO(LC_CORE, "JavaTask() MIDP_JC_EVENT_START_ARBITRARY_ARG >>\n");
            javacall_lifecycle_state_changed(JAVACALL_LIFECYCLE_MIDLET_STARTED,
                                             JAVACALL_OK);
            JavaTaskImpl(event->data.startMidletArbitraryArgEvent.argc,
                         event->data.startMidletArbitraryArgEvent.argv);

            JavaTaskIsGoOn = JAVACALL_FALSE;
            break;

        case MIDP_JC_EVENT_SET_VM_ARGS:
            REPORT_INFO(LC_CORE, "JavaTask() MIDP_JC_EVENT_SET_VM_ARGS >>\n");
            midpHandleSetVmArgs(event->data.startMidletArbitraryArgEvent.argc,
                                event->data.startMidletArbitraryArgEvent.argv);
            break;

        case MIDP_JC_EVENT_SET_HEAP_SIZE:
            REPORT_INFO(LC_CORE, "JavaTask() MIDP_JC_EVENT_SET_HEAP_SIZE >>\n");
            midpHandleSetHeapSize(event->data.heap_size);
            break;

        case MIDP_JC_EVENT_LIST_MIDLETS:
            REPORT_INFO(LC_CORE, "JavaTask() MIDP_JC_EVENT_LIST_MIDLETS >>\n");
            midpHandleListMIDlets();
            JavaTaskIsGoOn = JAVACALL_FALSE;
            break;

        case MIDP_JC_EVENT_LIST_STORAGE_NAMES:
            REPORT_INFO(LC_CORE, "JavaTask() MIDP_JC_EVENT_LIST_STORAGE_NAMES >>\n");
            midpHandleListStorageNames();
            JavaTaskIsGoOn = JAVACALL_FALSE;
            break;

        case MIDP_JC_EVENT_REMOVE_MIDLET:
            REPORT_INFO(LC_CORE, "JavaTask() MIDP_JC_EVENT_REMOVE_MIDLET >>\n");
            midpHandleRemoveMIDlet(event->data.removeMidletEvent);
            JavaTaskIsGoOn = JAVACALL_FALSE;
            break;


        case MIDP_JC_EVENT_END:
            REPORT_INFO(LC_CORE,"JavaTask() >> MIDP_JC_EVENT_END\n");
            JavaTaskIsGoOn = JAVACALL_FALSE;
            break;

        default:
            REPORT_ERROR(LC_CORE,"Unknown event.\n");
            break;

        } /* end of switch */

        midpFinalizeMemory();

    }   /* end of while 'JavaTaskIsGoOn' */

    javacall_events_finalize();
    javacall_finalize_configurations();

    REPORT_CRIT(LC_CORE,"JavaTask() <<\n");
} /* end of JavaTask */

/**
 * 
 */
static javacall_result midpHandleSetVmArgs(int argc, char* argv) {
    int used;

    while ((used = JVM_ParseOneArg(argc, argv)) > 0) {
        argc -= used;
        argv += used;
    }
}

/**
 * 
 */
static javacall_result midpHandleSetHeapSize(midp_event_heap_size heap_size) {
    JVM_SetConfig(JVM_CONFIG_HEAP_CAPACITY, heap_size.heap_size);
    JVM_SetConfig(JVM_CONFIG_HEAP_MINIMUM, heap_size.heap_size);
}

/**
 * 
 */
static javacall_result midpHandleListMIDlets() {
    char *argv[3];
    int argc = 0;
    javacall_result res;

    argv[argc++] = "runMidlet";
    argv[argc++] = "internal";
    argv[argc++] = "com.sun.midp.scriptutil.SuiteLister";

    res = JavaTaskImpl(argc, argv);
}

/**
 * 
 */
static javacall_result midpHandleListStorageNames() {
    char *argv[3];
    int argc = 0;
    javacall_result res;

    argv[argc++] = "runMidlet";
    argv[argc++] = "internal";
    /**
     * IMPL_NOTE: introduce an argument for SuiteLister allowing to print
     *            paths to jars only.
     */
    argv[argc++] = "com.sun.midp.scriptutil.SuiteLister";

    res = JavaTaskImpl(argc, argv);
}

/**
 * 
 */
static javacall_result
midpHandleRemoveMIDlet(midp_event_remove_midlet	removeMidletEvent) {
    char *argv[4];
    int argc = 0;
    javacall_result res;

    argv[argc++] = "runMidlet";
    argv[argc++] = "internal";
    argv[argc++] = "com.sun.midp.scriptutil.SuiteRemover";
    argv[argc++] = removeMidletEvent.suiteID;

    res = JavaTaskImpl(argc, argv);
}
