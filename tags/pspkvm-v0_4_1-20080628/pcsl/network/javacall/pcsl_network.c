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

/**
 * @file
 *
 * Stub Implementation for pcsl_network.h 
 *
 */

#include <pcsl_network.h>
#include <pcsl_network_notifier.h>
#include <pcsl_memory.h>

#include <javacall_network.h>
#include <javacall_socket.h>

#include <stdio.h>
#include <string.h>

extern int javacall_to_pcsl_result( javacall_result res );


/**
 * See pcsl_network.h for definition.
 */
void pcsl_add_network_notifier(
    void *handle,
    int event) {
    switch (event) {
    case PCSL_NET_CHECK_ACCEPT:
        javacall_server_socket_set_notifier (handle, JAVACALL_TRUE);
        break;
    case PCSL_NET_CHECK_READ:
        break;
    case PCSL_NET_CHECK_WRITE:
        break;
    case PCSL_NET_CHECK_EXCEPTION:
    default:
        return;
    }
}

/**
 * See pcsl_network.h for definition.
 */
void pcsl_remove_network_notifier(
    void *handle,
    int event) {
    switch (event) {
    case PCSL_NET_CHECK_ACCEPT:
        javacall_server_socket_set_notifier (handle, JAVACALL_FALSE);
        break;
    case PCSL_NET_CHECK_READ:
        break;
    case PCSL_NET_CHECK_WRITE:
        break;
    case PCSL_NET_CHECK_EXCEPTION:
    default:
        return;
    }
}

/**
 * See pcsl_network.h for definition.
 */
unsigned int pcsl_network_htonl(
    unsigned int value) {
    return value;
}

/**
 * See pcsl_network.h for definition.
 */
unsigned int pcsl_network_ntohl(
    unsigned int value) {
    return value;
}
    
/**
 * See pcsl_network.h for definition.
 */
unsigned short pcsl_network_htons(
    unsigned short value) {
    return value;
}

/**
 * See pcsl_network.h for definition.
 */
unsigned short pcsl_network_ntohs(
    unsigned short value) {
    return value;
}




/**
 * Performs platform-specific initialization of the networking system.
 * 
 * @return PCSL_NET_SUCCESS upon success;\n 
 *         PCSL_NET_IOERROR for an error
 */
int 
pcsl_network_init(void) {
    javacall_result res;

    res = javacall_network_init_start();

    while (res == JAVACALL_WOULD_BLOCK)
      res = javacall_network_init_finish();

    switch (res) {
    case JAVACALL_OK:
      return PCSL_NET_SUCCESS;
    case JAVACALL_FAIL:
      return PCSL_NET_IOERROR;
    }
    return PCSL_NET_IOERROR;
}



/**
 * See pcsl_network.h for definition.
 */
int
pcsl_network_finalize_start(void) {

        javacall_result res;
    res = javacall_network_finalize_start();

        switch (res) {
                case JAVACALL_OK:
                        return PCSL_NET_SUCCESS;
                case JAVACALL_FAIL:
                        return PCSL_NET_IOERROR;
                case JAVACALL_WOULD_BLOCK:
                        return PCSL_NET_WOULDBLOCK;
    }
    return PCSL_NET_IOERROR;
}

/**
 * See pcsl_network.h for definition.
 */
int
pcsl_network_finalize_finish(void) {

        javacall_result res;
    res = javacall_network_finalize_finish();

        switch (res) {
                case JAVACALL_OK:
                        return PCSL_NET_SUCCESS;
                case JAVACALL_FAIL:
                        return PCSL_NET_IOERROR;
                case JAVACALL_WOULD_BLOCK:
                        return PCSL_NET_WOULDBLOCK;
    }
    return PCSL_NET_IOERROR;
}



/**
 * See pcsl_network.h for definition.
 */
int 
pcsl_network_init_start(void) {
    javacall_result res;

    res = javacall_network_init_start();

    return javacall_to_pcsl_result(res);
}


/**
 * See pcsl_network.h for definition.
 */
int 
pcsl_network_init_finish(void) {
    javacall_result res;

    res = javacall_network_init_finish();

    return javacall_to_pcsl_result(res);
}


/**
 * See pcsl_network.h for definition.
 */
int
pcsl_network_error(void *handle) {
    return javacall_network_error(handle);
}

/**
 * See pcsl_network.h for definition.
 */
int
pcsl_network_getLocalHostName(char *pLocalHost) {
    javacall_result res;

    res = javacall_network_get_local_host_name(pLocalHost);

    return javacall_to_pcsl_result(res);
}


/**
 * See pcsl_network.h for definition.
 */
int
pcsl_network_getLocalIPAddressAsString(char *pLocalIPAddress) {
    javacall_result res;

    res = javacall_network_get_local_ip_address_as_string(pLocalIPAddress);

    return javacall_to_pcsl_result(res);
}


/**
 * Translates the given IP address into a host name. 
 *
 * @param ipn Raw IP address to translate
 * @param hostname the host name. The value of <tt>host</tt> is set by
 *             this function.
 * @param pHandle address of variable to receive the handle to for
 *        unblocking the Java thread; this is set
 *        only when this function returns PCSL_NET_WOULDBLOCK.
 * @param pContext address of a pointer variable to receive the context;
 *        this is set only when the function returns PCSL_NET_WOULDBLOCK.
 *
 * @return PCSL_NET_SUCCESS upon success;\n 
 * PCSL_NET_IOERROR if there is a network error;\n 
 */

int
pcsl_network_getHostByAddr_start(int ipn, char *hostname, void **pHandle, void **pContext) {

    javacall_result res;
    res = javacall_network_gethostbyaddr_start(ipn,hostname, pHandle, pContext);

    // Fallback option,
    // If javacall_network_gethostbyaddr_start is not implemented,
    // Use a basic transformation
    if (res==JAVACALL_FAIL) {
        unsigned char ipBytes[4];
        memcpy(ipBytes, &ipn, 4);

        sprintf(hostname,"%d.%d.%d.%d",ipBytes[0],ipBytes[1],ipBytes[2],ipBytes[3]);

        return PCSL_NET_SUCCESS;
    }

    return javacall_to_pcsl_result(res);

}

/**
 * Finishes a pending host name lookup operation.
 * 
 * @param ipn Raw IP address to translate
 * @param hostname the host name. The value of <tt>host</tt> is set by
 *             this function.
 * @param pHandle address of variable to receive the handle to for
 *        unblocking the Java thread; this is set
 *        only when this function returns PCSL_NET_WOULDBLOCK.
 * @param context the context returned by the getHostByAddr_start function
 *
 * @return PCSL_NET_SUCCESS upon success;\n 
 * PCSL_NET_IOERROR if there is a network error;\n 
 */
int
pcsl_network_getHostByAddr_finish(int ipn, char *hostname, void **pHandle, void *context) {
    javacall_result res;

    res = javacall_network_gethostbyaddr_finish(ipn,hostname, pHandle, context);

    return javacall_to_pcsl_result(res);

}


/**
 * See pcsl_network.h for definition.
 */
int
pcsl_network_gethostbyname_start(char *hostname,
    unsigned char *pAddress, int maxLen, int *pLen, void **pHandle, void **pContext) {
    javacall_result res;

    res = javacall_network_gethostbyname_start(hostname,
        pAddress, maxLen, pLen, pHandle, pContext);

    return javacall_to_pcsl_result(res);
}


/**
 * See pcsl_network.h for definition.
 *
 * Since the start function never returns PCSL_NET_WOULDBLOCK, this
 * function should never be called.
 */
int
pcsl_network_gethostbyname_finish(unsigned char *pAddress,
    int maxLen, int *pLen, void *handle, void *context) {
    javacall_result res;

    res = javacall_network_gethostbyname_finish(pAddress, maxLen, pLen, handle, context);

    return javacall_to_pcsl_result(res);
}


/**
 * See pcsl_network.h for definition.
 */
int
pcsl_network_getsockopt(void *handle, int flag, int *pOptval) {
    javacall_result res;

    res = javacall_network_getsockopt(handle, flag, pOptval);

    return javacall_to_pcsl_result(res);
}


/**
 * See pcsl_network.h for definition.
 */
int
pcsl_network_setsockopt(void *handle, int flag, int optval) {
    javacall_result res;

    res = javacall_network_setsockopt(handle, flag, optval);

    return javacall_to_pcsl_result(res);
}


/**
 * See pcsl_network.h for definition.
 */
int pcsl_network_addrToString(unsigned char *ipBytes,
    unsigned short** pResult, int* pResultLen) {

    int i;
    int tempLen;
    char * temp;

    temp = javacall_inet_ntoa (ipBytes);

    tempLen = strlen(temp);
      
    *pResult = (unsigned short*)pcsl_mem_malloc(tempLen *
                                               sizeof (unsigned short));
    if (NULL == *pResult) {
        return PCSL_NET_IOERROR;
    }

    for (i = 0; i < tempLen; i++) {
        (*pResult)[i] = temp[i];
    }

    *pResultLen = tempLen;
    return PCSL_NET_SUCCESS;
}

/**
 * See pcsl_network.h for definition.
 */
char * pcsl_inet_ntoa (void *ipBytes) {
    return javacall_inet_ntoa (ipBytes);
}
