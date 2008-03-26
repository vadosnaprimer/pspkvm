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

#include "javacall_socket.h" 
#include "javacall_time.h" 
#include "javacall_events.h" 

#define INVALID_SOCKET (-1)

#define SOCKET_ERROR   (-1)

#define socksLength (127)

#define RETRY_TIMES 30

typedef struct blockedSocksBuffer {
    int buff[socksLength]; 
    int retry[socksLength];
    int num;
} blockedSocksBuffer;

static blockedSocksBuffer readsocks;
static blockedSocksBuffer writesocks;
static int maxsockfd;


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

void sockets_init() {
    readsocks.num = 0;
    writesocks.num = 0;
    maxsockfd = 0;
}

inline int addBlockedSock(int sockfd, blockedSocksBuffer* sbuff) {

    if (sbuff->num == socksLength) {
        return -1;
    } else {
        sbuff->buff[sbuff->num] = sockfd;
        sbuff->retry[sbuff->num] = RETRY_TIMES;
        sbuff->num++;
        maxsockfd = (sockfd < maxsockfd) ? maxsockfd : sockfd;
        return 1;
    }
}

inline int delBlockedSock(int sockfd, blockedSocksBuffer* sbuff) {

    int i = 0;
    while(sbuff->buff[i] != sockfd) {
        if (i == sbuff->num) {
            return -1;
        }
        i++;
    }
    memmove(&sbuff->buff[i], &sbuff->buff[i+1], (sbuff->num-i)*sizeof(int));
    sbuff->num--;
    return 1;
}

inline void makefdset(fd_set* set, blockedSocksBuffer* sbuff) {
    FD_ZERO(set);
    int i;
    for (i = 0; i < sbuff->num; i++){
        FD_SET(sbuff->buff[i], set);
    }
}

inline int trySock(int sockfd) {

    fd_set rfds;
    FD_ZERO(&rfds);
    FD_SET(sockfd, &rfds);

    struct timeval tv;
    tv.tv_sec = 0;
    tv.tv_usec = 1; //If timeout is NULL (no timeout), select can block indefinitely

    return select(sockfd+1, NULL, &rfds, NULL, &tv);
}

void startTimerIfNeed();
void stopTimerIfNeed();

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

    int sockfd = socket(PF_INET, SOCK_STREAM, 0);

#ifdef DEBUG_JAVACALL_NETWORK
    javacall_print("javacall_socket_open_start\n");
#endif

    if (sockfd == INVALID_SOCKET) {
        return JAVACALL_FAIL;
    }

    int truebuf = 1;
    int status = setsockopt(sockfd, SOL_SOCKET, SO_REUSEADDR, &truebuf, sizeof(truebuf));

    if (status == -1) {
        close(sockfd);
        return JAVACALL_FAIL;
    }

    //add O_NONBLOCK flag
    int descriptorFlags = fcntl(sockfd, F_GETFL, 0);
    fcntl(sockfd, F_SETFL, descriptorFlags | O_NONBLOCK);

    struct sockaddr_in* addr;
    addr = malloc(sizeof(struct sockaddr_in));
    if (!addr) {
    	close(sockfd);
    	return JAVACALL_OUT_OF_MEMORY;
    }
    addr->sin_family      = AF_INET;
    addr->sin_port        = htons((unsigned short)port);
    memcpy(&addr->sin_addr.s_addr, ipBytes, sizeof(addr->sin_addr.s_addr));

#ifdef DEBUG_JAVACALL_NETWORK
    javacall_printf("connecting ...\n");
#endif
    status = connect(sockfd, (struct sockaddr *)addr, sizeof(struct sockaddr_in));

#ifdef DEBUG_JAVACALL_NETWORK
    javacall_printf("connected return %d\n", status);
#endif

    if (status == 0) {
        *pHandle = (void*)sockfd;
        free(addr);
        return JAVACALL_OK;
    }

    if ((status == SOCKET_ERROR) && (errno == EINPROGRESS || errno == EALREADY)) {
        *pContext = addr;
        *pHandle = (void*)sockfd;

        if (trySock(sockfd)) { //still try just now
            return javacall_socket_open_finish((void*)sockfd, NULL);
        }

        addBlockedSock(sockfd, &writesocks);
        startTimerIfNeed();
#ifdef DEBUG_JAVACALL_NETWORK
        javacall_print("javacall_socket_open_start: block\n");
#endif
        return JAVACALL_WOULD_BLOCK;
    }

    close(sockfd);
    return JAVACALL_FAIL;
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

    int sockfd = (int)handle;
    struct sockaddr_in* addr = (struct sockaddr_in*)context;
    int status;
    int err = 0;
    socklen_t err_size = sizeof(err);

#ifdef DEBUG_JAVACALL_NETWORK
    javacall_printf("javacall_socket_open_finish: handle=%d, context=%p\n", sockfd, addr);
#endif
    if (addr != NULL) {
        status = connect(sockfd, (struct sockaddr*)addr, sizeof(struct sockaddr_in));
        if ((status == 0) || (errno == EISCONN)) {
            delBlockedSock(sockfd, &writesocks);
            stopTimerIfNeed();
            free(context);
#ifdef DEBUG_JAVACALL_NETWORK
            javacall_print("javacall_socket_open_finish: ok\n");
#endif
            return JAVACALL_OK;
        }
    
        if ((status == SOCKET_ERROR) && (errno == EINPROGRESS || errno == EALREADY)) {
#ifdef DEBUG_JAVACALL_NETWORK
    	     javacall_print("javacall_socket_open_finish: block\n");
#endif
            return JAVACALL_WOULD_BLOCK;
        } else {
            //error
#ifdef DEBUG_JAVACALL_NETWORK
            javacall_print("javacall_socket_open_finish: failed with error:%d\n", errno);
#endif
            delBlockedSock(sockfd, &writesocks);
            stopTimerIfNeed();
            free(context);
            close(sockfd);    
            return JAVACALL_FAIL;
        }
    } else {
        //Call from open_start, before being blocked and timer started
        status = getsockopt(sockfd, SOL_SOCKET, SO_ERROR, &err, &err_size);
    
        if (err == 0 && status == 0) {
            return JAVACALL_OK;
        } else {
            close(sockfd);    
            return JAVACALL_FAIL;
        }
    }

}

//originally from pcsl_network_bsd (pcsl_socket.c)
javacall_result socket_read_common(void *handle, unsigned char *pData, int len, int *pBytesRead) {

    int sockfd = (int) handle;
#ifdef DEBUG_JAVACALL_NETWORK
    javacall_printf("socket_read_common read %d bytes\n", len);
#endif

    int status = recv(sockfd, pData, len, 0);
#ifdef DEBUG_JAVACALL_NETWORK
    javacall_printf("read return %d, errono:%d\n", status, status==-1?errno:0);
#endif
    if (SOCKET_ERROR == status) {
        if (EWOULDBLOCK == errno || EINPROGRESS == errno) {
            return JAVACALL_WOULD_BLOCK;
        } else if (EINTR == errno) {
            return JAVACALL_INTERRUPTED;
         } else {
            return JAVACALL_FAIL;
        }
    }

    *pBytesRead = status;
    return JAVACALL_OK;
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
    javacall_result status;
    status = socket_read_common(handle, pData, len, pBytesRead);

    if (status == JAVACALL_WOULD_BLOCK) {
        addBlockedSock((int)handle, &readsocks);
        startTimerIfNeed();
        *pContext = NULL;
    }

    return status;
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

    javacall_result status = socket_read_common(handle, pData, len, pBytesRead);
    
    if (status != JAVACALL_WOULD_BLOCK) {
        delBlockedSock((int)handle, &readsocks);
        stopTimerIfNeed();
    }

    return status;
}

//from pcsl_socket.c
static int socket_write_common(
        void *handle,
        char *pData,
        int len,
        int *pBytesWritten)
{
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
            return JAVACALL_WOULD_BLOCK;
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

    javacall_result status = socket_write_common(handle, pData, len, pBytesWritten);

    if (status == JAVACALL_WOULD_BLOCK) {
        addBlockedSock((int)handle, &writesocks);
        startTimerIfNeed();
        *pContext = NULL;
    }

    return status;                                        
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

    javacall_result status = socket_write_common(handle, pData, len, pBytesWritten);

    if (status != JAVACALL_WOULD_BLOCK) {
        delBlockedSock((int)handle, &writesocks);
        stopTimerIfNeed();
    }

    return status;
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

  NOTIFICATION FUNCTIONS
  - - - -  - - - - - - -  
  The following functions are implemented by Sun.
  Platform is required to invoke these function for each occurence of the
  undelying event.
  The functions need to be executed in platform's task/thread

 ******************************************************************************
 ******************************************************************************
 ******************************************************************************/
 
/**
 * @defgroup Notification functions 
 * @ingroup Socket
 * @{
 */
 
/**
 * A callback function to be called for notification of non-blocking 
 * socket related events, such as a socket completing opening or , 
 * closing socket remotely, disconnected by peer or data arrived on 
 * the socket indication.
 * The platform will invoke the call back in platform context for
 * each socket related occurrence. 
 *
 * @param type type of indication: Either
 *          JAVACALL_EVENT_SOCKET_OPEN_COMPLETED
 *          JAVACALL_EVENT_SOCKET_CLOSE_COMPLETED
 *          JAVACALL_EVENT_SOCKET_RECEIVE
 *          JAVACALL_EVENT_SOCKET_SEND
 *          JAVACALL_EVENT_SOCKET_REMOTE_DISCONNECTED
 *          JAVACALL_EVENT_NETWORK_GETHOSTBYNAME_COMPLETED  
 * @param handle handle of socket related to the notification
 * @param operation_result <tt>JAVACALL_OK</tt> if operation 
 *        completed successfully, 
 *        <tt>JAVACALL_FAIL</tt> or negative value on failure
 */
void javanotify_on_nonblocking_socket(
                                    javacall_socket_callback_type type, 
                                    javacall_handle socket_handle,
                                    javacall_result operation_result){
    javanotify_socket_event(type, socket_handle, operation_result);
}

void timer_socket_callback() {

    struct timeval tv;
    tv.tv_sec = 0;
    tv.tv_usec = 0; 

    fd_set readfds;
    fd_set writefds;
    makefdset(&readfds,  &readsocks);
    makefdset(&writefds, &writesocks);

    int status = select(maxsockfd+1, &readfds, &writefds, NULL, &tv);
    if (status == 0) {
        return;
    }
    int i;    
    for (i = 0; i < readsocks.num; i++) {
        if (FD_ISSET(readsocks.buff[i], &readfds)) {
            javanotify_on_nonblocking_socket(JAVACALL_EVENT_SOCKET_RECEIVE, (javacall_handle)readsocks.buff[i], JAVACALL_OK);
        } else if (readsocks.retry[i]-- <= 0) {
            javanotify_on_nonblocking_socket(JAVACALL_EVENT_SOCKET_RECEIVE, (javacall_handle)readsocks.buff[i], JAVACALL_FAIL);
        }
    }
    for (i = 0; i < writesocks.num; i++) {
        if (FD_ISSET(writesocks.buff[i], &writefds)) {
            javanotify_on_nonblocking_socket(JAVACALL_EVENT_SOCKET_SEND, (javacall_handle)writesocks.buff[i], JAVACALL_OK);
        } else if (writesocks.retry[i]-- <= 0) {
            javanotify_on_nonblocking_socket(JAVACALL_EVENT_SOCKET_SEND, (javacall_handle)readsocks.buff[i], JAVACALL_FAIL);
        }
    }
 
    /*
        JAVACALL_EVENT_SOCKET_OPEN_COMPLETED            =1000,
        JAVACALL_EVENT_SOCKET_CLOSE_COMPLETED           =1001,
        JAVACALL_EVENT_SOCKET_RECEIVE                   =1002,
        JAVACALL_EVENT_SOCKET_SEND                      =1003,
        JAVACALL_EVENT_SOCKET_REMOTE_DISCONNECTED       =1004,
        JAVACALL_EVENT_NETWORK_GETHOSTBYNAME_COMPLETED  =1005
    } javacall_socket_callback_type;
    */
}

static javacall_handle timer;

void startTimerIfNeed() {
    if (timer == 0) {
        javacall_time_initialize_timer(1000 /*msec*/, JAVACALL_TRUE /*cyclic*/, timer_socket_callback, &timer);
    }
} 

void stopTimerIfNeed() {
    if (readsocks.num == 0 &&  writesocks.num == 0) {
        javacall_time_finalize_timer(timer);
        timer = 0;
    }
}

/** @} */
    
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
    javacall_print("javacall_socket_available stub out\n");
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


