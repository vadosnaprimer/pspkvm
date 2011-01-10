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
//#define DEBUG_JAVACALL_NETWORK 1
#include <fcntl.h>
#include <errno.h>
#include <string.h>
#include <unistd.h>
#include <netdb.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/fd_set.h>
#include <sys/select.h>
#include <sys/time.h>
#include <netinet/in.h>

#include <stdio.h>
#include <stdlib.h>
#include <pspsdk.h>

#include "javacall_socket.h" 
#include "javacall_events.h" 

#define INVALID_SOCKET (-1)

#define SOCKET_ERROR   (-1)

#define MAX_SOCK_NUM 32
#define SOCKET_READ_BUFFER_SIZE (16384)
#ifdef DEBUG_JAVACALL_NETWORK
static int socket_open_count=0;
#endif

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

typedef struct {
    int fd; // INVALID_SOCKET if not used
    unsigned char* pData;
    int writepos;
    int readpos;
    //***
    int status;
    //<0 error
    //=0 EOF
    //>0 ok
    //***
    int pending_socket_event;
#ifdef DEBUG_JAVACALL_NETWORK
    int totalread;
    int totalwrite;
#endif
}read_buffer;

static volatile read_buffer buffer_list[MAX_SOCK_NUM];

#define GET_SOCKET_READ_BUFFER(d, p) \
{ \
int __i__; \
p = NULL; \
for  (__i__ = 0; __i__ < MAX_SOCK_NUM; __i__ ++) { \
	if (buffer_list[__i__].fd == d) {p = &buffer_list[__i__]; break;} \
} \
}

#define GET_FREE_READ_BUFFER(p) \
{ \
int __i__; \
for  (__i__ = 0; __i__ < MAX_SOCK_NUM; __i__ ++) { \
	if (buffer_list[__i__].fd == INVALID_SOCKET) {p = &buffer_list[__i__]; break;} \
} \
}

void socket_read_buffer_init() {
    int i;
    memset(buffer_list, 0, sizeof(buffer_list));
    for (i = 0; i < sizeof(buffer_list)/sizeof(read_buffer); i++) {
    	buffer_list[i].fd = INVALID_SOCKET;
    }
}

static javacall_result socket_open_impl(open_param* arg) {
    struct sockaddr_in* addr = &arg->addr;
    int sockfd = arg->fd;

#ifdef DEBUG_JAVACALL_NETWORK
    javacall_printf("connecting ...\n");
#endif
    int status = connect(sockfd, (struct sockaddr *)addr, sizeof(struct sockaddr_in));

#ifdef DEBUG_JAVACALL_NETWORK
    javacall_printf("connected return %d, errno=%d\n", status, errno);
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
		javacall_printf("Error, could not create thread for open\n");	
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
    javacall_result res = JAVACALL_OK;
#ifdef DEBUG_JAVACALL_NETWORK
    javacall_printf("javacall_socket_open_finish: result is %d\n", ((open_param*)context)->result);
#endif
    if (((open_param*)context)->result) {
    	 res = JAVACALL_FAIL;
    } else {
        volatile read_buffer* p = NULL;
        GET_FREE_READ_BUFFER(p);
        if (p) {
        	
              int descriptorFlags = fcntl((int)handle, F_GETFL, 0);
              fcntl((int)handle, F_SETFL, descriptorFlags | O_NONBLOCK);
   
        	
        	if (p->pData == NULL) {
        	    p->pData = malloc(SOCKET_READ_BUFFER_SIZE);
        	}

        	if (p->pData != NULL) {
#ifdef DEBUG_JAVACALL_NETWORK
                  p->totalread = 0;
                  p->totalwrite = 0;
#endif
        	    p->readpos = 0;
        	    p->writepos = 0;
        	    p->status = 1;
        	    p->fd = handle;
        	    p->pending_socket_event = 0;
#ifdef DEBUG_JAVACALL_NETWORK
        	    socket_open_count++;
                  javacall_printf("javacall_socket_open_finish: %d sockets left\n", socket_open_count);
#endif    
        	} else {
        	    res = JAVACALL_OUT_OF_MEMORY;
        	}
        	
        } else {
            res = JAVACALL_OUT_OF_MEMORY;
        }
    }
    free(context);
    return res;
}

static javacall_result readFromBuffer(int handle, unsigned char* pData, int len, int* pBytesRead) {
    int i, bytesToRead = 0;
    volatile read_buffer* p;
    
    GET_SOCKET_READ_BUFFER(handle, p);
    if (p && p->status >= 0) {
    	 
        volatile int readpos = p->readpos;
        volatile int writepos = p->writepos;

        if (p->status == 0 && readpos == writepos) {
        	//EOF
        	*pBytesRead = 0;
        	return JAVACALL_OK;
        }

        if (readpos < writepos) {
        	bytesToRead = writepos - readpos;
        } else if (readpos > writepos) {
              bytesToRead = SOCKET_READ_BUFFER_SIZE - readpos;
        } else {
            //nothing to read, would block...
            p->pending_socket_event = 0;
            return JAVACALL_WOULD_BLOCK;
        }

        if (bytesToRead > len) bytesToRead = len;

        memcpy(pData, p->pData + readpos, bytesToRead);
        readpos += bytesToRead;
        if (readpos >= SOCKET_READ_BUFFER_SIZE) {
            readpos = 0;
        }
        p->readpos = readpos;
        *pBytesRead = bytesToRead;

#ifdef DEBUG_JAVACALL_NETWORK
        javacall_printf("readfrombuffer:%d bytes read\n", bytesToRead);
        p->totalread += bytesToRead;
        javacall_printf("totalread:%d, totalwrite:%d\n", p->totalread, p->totalwrite);
#endif

        return JAVACALL_OK;
    } else {
        return JAVACALL_FAIL;
    }
}

//originally from pcsl_network_bsd (pcsl_socket.c)

static int socket_read_thread(SceSize args, void *argp) {
    int i;
    fd_set set;
    struct timeval tv;
    volatile int sock;

    tv.tv_sec = 1;
    tv.tv_usec = 0;

    while (1) {//TODO: Add exit condition
    
        FD_ZERO(&set);
        
        for (i = 0; i < MAX_SOCK_NUM; i++) {
            sock = buffer_list[i].fd;
            if (sock != INVALID_SOCKET && buffer_list[i].status > 0) {
                FD_SET(sock, &set);
            }
        }
        
        if(select(FD_SETSIZE, &set, NULL, NULL, &tv) >= 0) {
    	
            for(sock = 0; sock < FD_SETSIZE; sock++) {
                 if(FD_ISSET(sock, &set)) {
		   	
		    	 
		    	 volatile read_buffer* p;
		    	 
		        GET_SOCKET_READ_BUFFER(sock, p);
		        if (p != NULL && p->status > 0) {
		        	int status;
		        	int bytesToRead;
		        	volatile int writepos = p->writepos;
		    	       volatile int readpos = p->readpos;
		    	
                            if (writepos < readpos) {
                                bytesToRead = readpos - writepos - 1;
                            } else {
                                bytesToRead = SOCKET_READ_BUFFER_SIZE - writepos;
                                if (readpos == 0) {
                                    bytesToRead --;
                                }
                            }
                            
                            if (bytesToRead > 0) {
        		        	status = recv(sock, p->pData+writepos, bytesToRead, 0);
        		        	if (status > 0) {
        		        	    writepos += status;        		        	    
#ifdef DEBUG_JAVACALL_NETWORK
                                       javacall_printf("recv returns %d bytes\n", status);
                                       p->totalwrite += status;
#endif
        		        	} else if (status == 0) {
        		        	    p->status = 0; //EOF
#ifdef DEBUG_JAVACALL_NETWORK
        		        	    javacall_printf("recv returns EOF\n");
#endif
        		        	}
        
        		        	if (writepos >= SOCKET_READ_BUFFER_SIZE) {
        		        	    writepos = 0;
        		        	}

        		        	p->writepos = writepos;
        		        	
        		        	if (status >= 0 || (status < 0 && errno != EWOULDBLOCK && errno != EINPROGRESS)) {
        		        	    if ((status <= 0) || !p->pending_socket_event) {
                                           if (status > 0) {
        		        	            p->pending_socket_event = 1;
        		        	        }
        		        	        javanotify_socket_event(JAVACALL_EVENT_SOCKET_RECEIVE, sock, status>=0?JAVACALL_OK:JAVACALL_FAIL);
        		        	    }
        		        	}
                            }
		           }
		      }
              }
        } else {
#ifdef DEBUG_JAVACALL_NETWORK
            javacall_printf("select return Fail\n");
#endif
        }
    }
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

    (void)pContext;
    static int read_thread_started = 0;
    
#ifdef DEBUG_JAVACALL_NETWORK
    javacall_printf("javacall_socket_read_start: %d bytes to read\n", len);
#endif

    if (!read_thread_started) {
        SceUID thid = sceKernelCreateThread("socket_read_thread", socket_read_thread, 0x28, 16 *1024, PSP_THREAD_ATTR_USER, NULL);	
        if(thid < 0) {	
        	javacall_printf("Error, could not create thread for read\n");	
        	return JAVACALL_FAIL;	
        }	

    	 read_thread_started = 1;
        sceKernelStartThread(thid, 0, NULL);
    }

    return readFromBuffer((int)handle, pData, len, pBytesRead);
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
    (void)context;
#ifdef DEBUG_JAVACALL_NETWORK
    javacall_printf("javacall_socket_read_finish\n");
#endif

    return javacall_socket_read_start(handle, pData, len, pBytesRead, NULL);
}

static SceUInt socket_write_retey(void *common)
{
	int sock = (int)common;
	javanotify_socket_event(JAVACALL_EVENT_SOCKET_SEND, sock, JAVACALL_OK);
	return 0;
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
    int status, err;
    
#ifdef DEBUG_JAVACALL_NETWORK
    javacall_printf("socket_write_common write %d bytes\n", len);
#endif
    status = send(sockfd, pData, len, 0);
    err = errno;
#ifdef DEBUG_JAVACALL_NETWORK
    javacall_printf("write retrun %d, errno:%d\n", status, status==-1?err:0);
#endif
    if (SOCKET_ERROR == status) {
        if (EWOULDBLOCK == err || EINPROGRESS == err) {
            if (pContext == NULL) {
                //Only retry once. If pContext == NULL, it's a reentry, so returns JAVACALL_FAIL without retrying again.
                return JAVACALL_FAIL;
            }
            SceUID id = sceKernelSetAlarm(300000, socket_write_retey, (void*)sockfd);
	     if (id < 0) {
	       javacall_print("javacall_socket_write_start: sceKernelSetAlarm error!\n");
	       return JAVACALL_FAIL;
	     }
            return JAVACALL_WOULD_BLOCK;
            //return JAVACALL_FAIL; //Hum... I assume "write" would never block... is it correct? -M@x
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
    return javacall_socket_write_start(handle, pData, len, pBytesWritten, NULL); 
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

    volatile read_buffer* p;
    GET_SOCKET_READ_BUFFER(handle, p);
    if (p != NULL) {
        p->status = -1;
        p->fd = INVALID_SOCKET;
#ifdef DEBUG_JAVACALL_NETWORK
        socket_open_count--;
        javacall_printf("javacall_socket_close_start: %d sockets open\n", socket_open_count);
#endif
    }

    int status = close(sockfd);
#ifdef DEBUG_JAVACALL_NETWORK
    javacall_printf("javacall_socket_close return %d\n", status);
#endif

    return JAVACALL_OK; //always return OK
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
    read_buffer* p;
    
    GET_SOCKET_READ_BUFFER(sockfd, p);
    if (p) {
    	 int readpos = p->readpos;
    	 int writepos = p->writepos;
    	 if (writepos >= readpos)  *pBytesAvailable = writepos - readpos;
    	 else *pBytesAvailable = SOCKET_READ_BUFFER_SIZE - readpos;
        return JAVACALL_OK;
    } else {
        return JAVACALL_FAIL;
    }
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


