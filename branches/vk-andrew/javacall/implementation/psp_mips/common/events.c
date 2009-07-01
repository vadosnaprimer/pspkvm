/*
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

#include "pspthreadman.h"
#include "javacall_events.h"
#include <stdlib.h>
#include <string.h>

#ifdef __cplusplus
extern "C" {
#endif

#define NUM_MBX 5
static int _psp_mailboxID[NUM_MBX];
static SceUID _psp_semaID;

#define NEXT(i) (i >= (NUM_MBX - 1) ? 0 : i+1)
static int _mbxid_recv,  _mbxid_send;

/**
 * Waits for an incoming event message and copies it to user supplied
 * data buffer
 * @param waitForever indicate if the function should block forever
 * @param timeTowaitInMillisec max number of seconds to wait
 *              if waitForever is false
 * @param binaryBuffer user-supplied buffer to copy event to
 * @param binaryBufferMaxLen maximum buffer size that an event can be 
 *              copied to.
 *              If an event exceeds the binaryBufferMaxLen, then the first
 *              binaryBufferMaxLen bytes of the events will be copied
 *              to user-supplied binaryBuffer, and JAVACALL_OUT_OF_MEMORY will 
 *              be returned
 * @param outEventLen user-supplied pointer to variable that will hold actual 
 *              event size received
 *              Platform is responsible to set this value on success to the 
 *              size of the event received, or 0 on failure.
 *              If outEventLen is NULL, the event size is not returned.
 * @return <tt>JAVACALL_OK</tt> if an event successfully received, 
 *         <tt>JAVACALL_FAIL</tt> or if failed or no messages are avaialable
 *         <tt>JAVACALL_OUT_OF_MEMORY</tt> If an event's size exceeds the 
 *         binaryBufferMaxLen 
 */
javacall_result javacall_event_receive(
                            long                     timeTowaitInMillisec,
                            /*OUT*/ unsigned char*  binaryBuffer,
                            /*IN*/  int             binaryBufferMaxLen,
                            /*OUT*/ int*            outEventLen){
    void* msg;
    int ret;
    SceUInt to;
    static int busy_count = 0;

    if (busy_count > 40 && timeTowaitInMillisec == 0) {
        timeTowaitInMillisec = 30;
        busy_count = 0;
    }
    
    if (timeTowaitInMillisec < 0) {
    	busy_count = 0;
    	ret = sceKernelReceiveMbxCB(_psp_mailboxID[_mbxid_recv], &msg, NULL);
    } else if (timeTowaitInMillisec == 0) {
       busy_count++;
       to = 0;
       ret = sceKernelPollMbx(_psp_mailboxID[_mbxid_recv], &msg);
    } else {
       busy_count = 0;
       to = timeTowaitInMillisec * 1000;
       ret = sceKernelReceiveMbxCB(_psp_mailboxID[_mbxid_recv], &msg, &to);
    }

    
    if (ret ==0) {
    	int len = *((int*)msg+1);
    	_mbxid_recv = NEXT(_mbxid_recv);

    	if (binaryBufferMaxLen < len) {
           free(msg);
    	    return JAVACALL_OUT_OF_MEMORY;
    	} else {
    	    memcpy(binaryBuffer, (char*)msg+sizeof(int)+sizeof(int), len);
    	    *outEventLen = len;
    	    free(msg);
    	    return JAVACALL_OK;
    	}
    }
    return JAVACALL_FAIL;
}
/**
 * copies a user supplied event message to a queue of messages
 *
 * @param binaryBuffer a pointer to binary event buffer to send
 *        The platform should make a private copy of this buffer as
 *        access to it is not allowed after the function call.
 * @param binaryBufferLen size of binary event buffer to send
 * @return <tt>JAVACALL_OK</tt> if an event successfully sent, 
 *         <tt>JAVACALL_FAIL</tt> or negative value if failed
 */
javacall_result javacall_event_send(unsigned char* binaryBuffer,
                                    int binaryBufferLen){
    char* p = NULL;
    int id;

    p = (char*)malloc(binaryBufferLen+sizeof(int)+sizeof(int));
    if (p) {
    	 *(int*)(p+sizeof(int)) = binaryBufferLen;
    	 memcpy(p+sizeof(int)+sizeof(int), binaryBuffer, binaryBufferLen);
        
        sceKernelWaitSemaCB(_psp_semaID, 1, NULL);
        if (NEXT(_mbxid_send) == _mbxid_recv) {
        	printf("javacall_event_send: discard event\n");
        	sceKernelSignalSema(_psp_semaID, 1);
        	return JAVACALL_FAIL;
        }
        id = _mbxid_send;
        _mbxid_send = NEXT(_mbxid_send);
        sceKernelSignalSema(_psp_semaID, 1);
        
	 sceKernelSendMbx(_psp_mailboxID[id], (void*)p);
        return JAVACALL_OK;
    } else {
        return JAVACALL_FAIL;
    }
    return JAVACALL_FAIL;
}    

//PSPGlobalINFO _psp_global_info;
javacall_result javacall_events_init(void) {
    int i;
    for (i = 0; i < NUM_MBX; i++) {
        _psp_mailboxID[i] = sceKernelCreateMbx("JavaMessagebox", 0, NULL);
    }
    _mbxid_send = _mbxid_recv = 0;
    _psp_semaID = sceKernelCreateSema("JavaEventQMutex", 0, 1, 1, 0);

    return JAVACALL_OK;
}

javacall_result javacall_events_finalize(void) {
    int i;
    for (i = 0; i < NUM_MBX; i++) {
        sceKernelDeleteMbx(_psp_mailboxID[i]);
         _psp_mailboxID[i] = 0;
    }
    sceKernelDeleteSema(_psp_semaID);
    _mbxid_send = _mbxid_recv = 0;
    _psp_semaID = 0;
    return JAVACALL_OK;
}

/*************************** KEYPRESS *************************************/



/***************************** SMS *************************************/
/*                   1. INCOMING SMS CALLBACK                          */
/*                   2. SMS SENDING RESULT CALLBACK                    */
/***********************************************************************/




/*************************** SOCKET *************************************/

/*************************** NETWORK *************************************/


/*************************** PAUSE/RESUME *************************************/

/*************************** TEXTFIELD *************************************/





/*************************** MMAPI *************************************/
/*                   1. END-OF-MEDIA CALLBACK                          */
/*                   2. DURATION UPDATE CALLBACK                       */
/***********************************************************************/

/******************** MMAPI (2) : DURATION UPDATE CALLBACK *************/


    
#ifdef __cplusplus
}
#endif

