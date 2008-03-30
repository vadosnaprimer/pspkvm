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

#ifdef __cplusplus
extern "C" {
#endif
#define DEBUG_JAVACALL_NETWORK 1
#include <fcntl.h>
#include <errno.h>
#include <string.h>
#include <unistd.h>
#include <netdb.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/fd_set.h>
#include <sys/time.h>
#include <netinet/in.h>

#include <stdio.h>
#include <stdlib.h>
#include <pspsdk.h>

#include "javacall_socket.h" 
#include "javacall_events.h" 

#define INVALID_SOCKET (-1)

#define SOCKET_ERROR   (-1)


/* Connect to an access point */
int connect_to_apctl(int config){
	int err;	
	int stateLast = -1;	
	/* Connect using the first profile */	
	err = sceNetApctlConnect(config);	
	if (err != 0)	{		
		javacall_printf(": sceNetApctlConnect returns %08X\n", err);		
		return 0;	
	}	
	javacall_printf("Connecting...\n");	
	while (1)	{		
		int state;
		err = sceNetApctlGetState(&state);
		if (err != 0)		{		
			javacall_printf(": sceNetApctlGetState returns $%x\n", err);
			break;		
		}		
		if (state > stateLast) {
			javacall_printf("  connection state %d of 4\n", state);
			stateLast = state;		
		}		
		if (state == 4)
			break;  // connected with static IP		
		// wait a little before polling again		
		sceKernelDelayThread(50*1000); // 50ms	
	}	
	javacall_printf(": Connected!\n");	
	if(err != 0)	{
		return 0;	
	}	
	return 1;
}

typedef struct {
    struct sockaddr_in addr;
    int fd;
    int result;
}open_param;

typedef struct {
    int fd;
    unsigned char *pData;
    int len;
    int bytesRead;
}read_param;

static javacall_result socket_open_impl(open_param* arg) {
    struct sockaddr_in* addr = &arg->addr;
    int sockfd = arg->fd;

#ifdef DEBUG_JAVACALL_NETWORK
    javacall_printf("connecting ...\n");
#endif
    int status = connect(sockfd, (struct sockaddr *)addr, sizeof(struct sockaddr_in));

#ifdef DEBUG_JAVACALL_NETWORK
    javacall_printf("connected return %d\n", status);
#endif

    arg->result = status;

    if (status == 0) {
        return JAVACALL_OK;
    }

    close(sockfd);
    return JAVACALL_FAIL;
}

static int socket_open_thread(SceSize args, void *argp) {
	javacall_result ret;
	open_param* arg = *(open_param**)argp;
	javacall_handle fd = (javacall_handle)arg->fd;
	ret = socket_open_impl(arg);
	javanotify_socket_event(JAVACALL_EVENT_SOCKET_SEND, fd, ret);
	sceKernelExitDeleteThread(ret);

	return 0;
}

/**
 * Initiates the connection of a client socket.
 *
 * @param ipBytes IP address of the local device in the form of byte array
 * @param port number of the port to open
 * @param pHandle address of variable to receive the handle; this is set
 *        only when this function returns JAVACALL_WOULD_BLOCK or JAVACALL_OK.
 * @param pContext address of a pointer variable to receive the context;
 *        this is set only when the function returns JAVACALL_WOULDBLOCK.
 *
 * @retval JAVACALL_OK          success
 * @retval JAVACALL_FAIL        if there was an IO error   
 * @retval JAVACALL_WOULD_BLOCK  if the caller must call the finish function to complete the operation
 * @retval JAVACALL_CONNECTION_NOT_FOUND when there was some other error (Connection not found exception case)
 */
javacall_result javacall_socket_open_start(unsigned char *ipBytes, int port,
                                           void **pHandle, void **pContext) {
       (void)pContext;
       int truebuf = 1;
       int sockfd = socket(PF_INET, SOCK_STREAM, 0);

#ifdef DEBUG_JAVACALL_NETWORK
       javacall_print("javacall_socket_open_start\n");
#endif
   
       if (sockfd == INVALID_SOCKET) {
           return JAVACALL_FAIL;
       }
   
       int status = setsockopt(sockfd, SOL_SOCKET, SO_REUSEADDR, &truebuf, sizeof(truebuf));
   
       if (status == -1) {
           close(sockfd);
           return JAVACALL_FAIL;
       }
   
       open_param* param;
       param = malloc(sizeof(open_param));
       if (!param) {
       	close(sockfd);
       	return JAVACALL_OUT_OF_MEMORY;
       }
       struct sockaddr_in* addr;
       addr = &param->addr;
       addr->sin_family      = AF_INET;
       addr->sin_port        = htons((unsigned short)port);
       memcpy(&addr->sin_addr.s_addr, ipBytes, sizeof(addr->sin_addr.s_addr));
       param->fd = sockfd;
       

	SceUID thid = sceKernelCreateThread("socket_open_thread", socket_open_thread, 0x11,  8*1024, PSP_THREAD_ATTR_USER, NULL);	
	if(thid < 0) {	
		javacall_printf("Error, could not create thread\n");	
		close(sockfd);
		free(param);
		return JAVACALL_FAIL;	
	}	
	
	sceKernelStartThread(thid, sizeof(&param), &param);
	*pHandle = (void*)sockfd;
	*pContext = param;
	return JAVACALL_WOULD_BLOCK;
}

/**
 * Finishes a pending open operation.
 *
 * @param handle the handle returned by the open_start function
 * @param context the context returned by the open_start function
 *
 * @retval JAVACALL_OK          success
 * @retval JAVACALL_FAIL        if an error occurred  
 * @retval JAVACALL_WOULD_BLOCK  if the caller must call the finish function again to complete the operation
 */
javacall_result javacall_socket_open_finish(void *handle, void *context) {
#ifdef DEBUG_JAVACALL_NETWORK
    javacall_printf("javacall_socket_open_finish: result is %d\n", ((open_param*)context)->result);
#endif
    if (((open_param*)context)->result) {
    	 return JAVACALL_FAIL;
    } else {
        return JAVACALL_OK;
    }
}

//originally from pcsl_network_bsd (pcsl_socket.c)

static int socket_read_thread(SceSize args, void *argp) {
    read_param* param = *(read_param**)argp;
    javacall_handle sockfd = (javacall_handle)param->fd;
    
#ifdef DEBUG_JAVACALL_NETWORK
    javacall_printf("socket  read %d bytes\n", param->len);
#endif

    int status = recv(param->fd ,param->pData , param->len, 0);
#ifdef DEBUG_JAVACALL_NETWORK
    javacall_printf("read return %d, errono:%d\n", status, status==-1?errno:0);
#endif
    param->bytesRead = status;
    javanotify_socket_event(JAVACALL_EVENT_SOCKET_RECEIVE, sockfd, status>=0?JAVACALL_OK:JAVACALL_FAIL);
    sceKernelExitDeleteThread(0);

    return 0;
}

/**
 * Initiates a read from a platform-specific TCP socket.
 *  
 * @param handle handle of an open connection
 * @param pData base of buffer to receive read data
 * @param len number of bytes to attempt to read
 * @param pBytesRead returns the number of bytes actually read; it is
 *        set only when this function returns JAVACALL_OK
 * @param pContext address of pointer variable to receive the context;
 *        it is set only when this function returns JAVACALL_WOULDBLOCK
 * 
 * @retval JAVACALL_OK          success
 * @retval JAVACALL_FAIL        if there was an error   
 * @retval JAVACALL_WOULD_BLOCK  if the operation would block
 * @retval JAVACALL_INTERRUPTED for an Interrupted IO Exception
 */
javacall_result javacall_socket_read_start(void *handle,unsigned char *pData,int len, 
                                           int *pBytesRead, void **pContext) {

#ifdef DEBUG_JAVACALL_NETWORK
    javacall_printf("javacall_socket_read_start\n");
#endif

    int descriptorFlags = fcntl(handle, F_GETFL, 0);
fcntl(handle, F_SETFL, descriptorFlags | O_NONBLOCK);
    int status = recv(handle ,pData , len, 0);

    descriptorFlags = fcntl(handle, F_GETFL, 0);
fcntl(handle, F_SETFL, descriptorFlags & ~O_NONBLOCK);

#ifdef DEBUG_JAVACALL_NETWORK
    javacall_printf("read return %d, errono:%d\n", status, status==-1?errno:0);
#endif

    if (status >= 0) {
        *pBytesRead = status;
        return JAVACALL_OK;
    } else {
        if (errno != EWOULDBLOCK && errno != EINPROGRESS) {
#ifdef DEBUG_JAVACALL_NETWORK
    javacall_printf("reading error\n");
#endif

             return JAVACALL_FAIL;
        }
    }

    read_param* param;
    param = malloc(sizeof(read_param));
    if (!param) {
     	return JAVACALL_OUT_OF_MEMORY;
    }
    param->fd = (int)handle;
    param->len = len;
    param->pData = malloc(len);
    if (!param->pData) {
    	free(param);
     	return JAVACALL_OUT_OF_MEMORY;
    }
    param->bytesRead = 0;

    SceUID thid = sceKernelCreateThread("socket_read_thread", socket_read_thread, 0x11, 64 *1024, PSP_THREAD_ATTR_USER, NULL);	
    if(thid < 0) {	
	javacall_printf("Error, could not create thread\n");	
	free(param->pData);
	free(param);
	return JAVACALL_FAIL;	
    }	
	
    sceKernelStartThread(thid, sizeof(&param), &param);
    *pContext = param;
	
    return JAVACALL_WOULD_BLOCK;
}

/**
 * Finishes a pending read operation.
 *
 * @param handle handle of an open connection
 * @param pData base of buffer to receive read data
 * @param len number of bytes to attempt to read
 * @param pBytesRead returns the number of bytes actually read; it is
 *        set only when this function returns JAVACALL_OK
 * @param context the context returned by read_start
 * 
 * @retval JAVACALL_OK          success
 * @retval JAVACALL_FAIL        if there was an error   
 * @retval JAVACALL_WOULD_BLOCK  if the caller must call the finish function again to complete the operation
 * @retval JAVACALL_INTERRUPTED for an Interrupted IO Exception
 */
javacall_result javacall_socket_read_finish(void *handle,unsigned char *pData,int len,int *pBytesRead,void *context) {
    (void)handle;
    read_param* param = context;
    if (param->bytesRead> 0) {
        memcpy(pData, param->pData, param->bytesRead);
    }
    *pBytesRead = param->bytesRead;
    free(param->pData);
    free(param);
#ifdef DEBUG_JAVACALL_NETWORK
    javacall_printf("javacall_socket_read_finish: %d bytes read\n", *pBytesRead);
#endif
    return *pBytesRead>=0?JAVACALL_OK:JAVACALL_FAIL;
}

/**
 * Initiates a write to a platform-specific TCP socket.
 *
 * @param handle handle of an open connection
 * @param pData base of buffer containing data to be written
 * @param len number of bytes to attempt to write
 * @param pBytesWritten returns the number of bytes written after
 *        successful write operation; only set if this function returns
 *        JAVACALL_OK
 * @param pContext address of a pointer variable to receive the context;
 *        it is set only when this function returns JAVACALL_WOULDBLOCK
 *
 * @retval JAVACALL_OK          success
 * @retval JAVACALL_FAIL        if there was an error   
 * @retval JAVACALL_WOULD_BLOCK  if the operation would block
 * @retval JAVACALL_INTERRUPTED for an Interrupted IO Exception
 */
javacall_result javacall_socket_write_start(void *handle,char *pData,int len,int *pBytesWritten,void **pContext) {

    int sockfd = (int) handle;
#ifdef DEBUG_JAVACALL_NETWORK
    javacall_printf("socket_write_common write %d bytes\n", len);
#endif
    int status = send(sockfd, pData, len, 0);
#ifdef DEBUG_JAVACALL_NETWORK
    javacall_printf("write retrun %d, errno:%d\n", status, status==-1?errno:0);
#endif
    if (SOCKET_ERROR == status) {
        if (EWOULDBLOCK == errno || EINPROGRESS == errno) {
            //return JAVACALL_WOULD_BLOCK;
            return JAVACALL_FAIL; //Hum... I assume "write" would never block... is it correct? -M@x
        } else if (EINTR == errno) {
            return JAVACALL_INTERRUPTED;
        } else {
            return JAVACALL_FAIL;
        }
    }

    *pBytesWritten = status;
    return JAVACALL_OK;                                    
}
    
/**
 * Finishes a pending write operation.
 *
 * @param handle handle of an open connection
 * @param pData base of buffer containing data to be written
 * @param len number of bytes to attempt to write
 * @param pBytesWritten returns the number of bytes written after
 *        successful write operation; only set if this function returns
 *        JAVACALL_OK
 * @param context the context returned by write_start
 *
 * @retval JAVACALL_OK          success
 * @retval JAVACALL_FAIL        if there was an error   
 * @retval JAVACALL_WOULD_BLOCK  if the caller must call the finish function again to complete the operation
 * @retval JAVACALL_INTERRUPTED for an Interrupted IO Exception
 */
javacall_result javacall_socket_write_finish(void *handle,char *pData,int len,int *pBytesWritten,void *context) {
    return JAVACALL_FAIL; //Since write_start never blocking, write_finish should never be invoked
}
    
/**
 * Initiates the closing of a platform-specific TCP socket.
 *
 * @param handle handle of an open connection
 * @param pContext address of a pointer variable to receive the context;
 *        it is set only when this function returns JAVACALL_WOULDBLOCK
 *
 * @retval JAVACALL_OK          success
 * @retval JAVACALL_FAIL        if there was an error
 * @retval JAVACALL_WOULD_BLOCK  if the operation would block 
 */
javacall_result javacall_socket_close_start(void *handle,void **pContext) {

    int sockfd = (int)handle;
#ifdef DEBUG_JAVACALL_NETWORK
    javacall_printf("javacall_socket_close\n");
#endif
    int status = close(sockfd);
#ifdef DEBUG_JAVACALL_NETWORK
    javacall_printf("javacall_socket_close return %d\n", status);
#endif
    if (status == 0) {
        return JAVACALL_OK;
    } 

    return JAVACALL_FAIL;
}
    
/**
 * Initiates the closing of a platform-specific TCP socket.
 *
 * @param handle handle of an open connection
 * @param context the context returned by close_start
 *
 * @retval JAVACALL_OK          success
 * @retval JAVACALL_FAIL        if there was an error   
 * @retval JAVACALL_WOULD_BLOCK  if the caller must call the finish function again to complete the operation
 */
javacall_result javacall_socket_close_finish(void *handle,void *context) {

    //no need in finish: start never blocks
    return JAVACALL_OK;
}

 
/******************************************************************************
 ******************************************************************************
 ******************************************************************************
    OPTIONAL FUNCTIONS
 ******************************************************************************
 ******************************************************************************
 ******************************************************************************/
    
/**
 * @defgroup OptionalTcpSocket Optional client socket API
 * @ingroup Network
 * @{
 */

/**
 * Gets the number of bytes available to be read from the platform-specific
 * socket without causing the system to block.
 *
 * @param handle handle of an open connection
 * @param pBytesAvailable returns the number of available bytes
 *
 * @retval JAVACALL_OK      success
 * @retval JAVACALL_FAIL    if there was an error 
 */
javacall_result /* OPTIONAL*/ javacall_socket_available(javacall_handle handle,int *pBytesAvailable) {
#ifdef DEBUG_JAVACALL_NETWORK
    javacall_printf("javacall_socket_available\n");
#endif
    int sockfd = (int)handle;  
    int ret;
    fd_set  fdR;  
    struct timeval tv;
    tv.tv_sec = 0;
    tv.tv_usec = 0; 


    FD_ZERO(&fdR);  
    FD_SET(sockfd, &fdR);
    ret = select(sockfd + 1, &fdR, NULL, NULL, &tv);
    
#ifdef DEBUG_JAVACALL_NETWORK
    javacall_printf("javacall_socket_available: select return %d\n", ret);
#endif

    switch (ret) {  
                case -1:  
                        return JAVACALL_FAIL;  
                case 0:  
                        *pBytesAvailable = 0;
                        return JAVACALL_OK;
                default:  
                        if (FD_ISSET(sockfd, &fdR)) {
                        	*pBytesAvailable = 1;
                        	return JAVACALL_OK;
                        }  
    }  

    return JAVACALL_FAIL;
}
    
/**
 * Shuts down the output side of a platform-specific TCP socket.
 * Further writes to this socket are disallowed.
 *
 * Note: a function to shut down the input side of a socket is
 * explicitly not provided.
 *
 * @param handle handle of an open connection
 *
 * @retval JAVACALL_OK      success
 * @retval JAVACALL_FAIL    if there was an error
 */
javacall_result /*OPTIONAL*/ javacall_socket_shutdown_output(javacall_handle handle) {
    javacall_print("javacall_socket_shutdown_output stub out\n");
    return JAVACALL_OK;
}

javacall_result /*OPTIONAL*/ javacall_server_socket_open_start(
        int port,
        void **pHandle,
        void **pContext)
{
    return JAVACALL_FAIL;
}

/**
 *  * See pcsl_network.h for definition.
 *   */
javacall_result javacall_server_socket_open_finish(void *handle,void *context) {

    return JAVACALL_FAIL;
}


/**
 *  * See javacall_socket.h for definition.
 *   */
javacall_result /*OPTIONAL*/ javacall_server_socket_accept_start(
         javacall_handle handle,
         javacall_handle *pNewhandle) {

     return JAVACALL_FAIL;
}

/**
 *  * See javacall_socket.h for definition.
 *   */
javacall_result /*OPTIONAL*/ javacall_server_socket_accept_finish(
        javacall_handle handle,
        javacall_handle *pNewhandle) {

    return JAVACALL_FAIL;
}


#ifdef __cplusplus
}
#endif


