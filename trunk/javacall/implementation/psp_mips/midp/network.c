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

#include "javacall_socket.h"
#include "javacall_network.h"   
#include "javacall_logging.h" 

#include <pspkernel.h>
#include <pspsdk.h>
#include <netdb.h>
#include <sys/socket.h> 
#include <netinet/tcp.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/select.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <pspnet.h>
#include <pspnet_inet.h>
#include <pspnet_apctl.h>
#include <pspnet_resolver.h>
#include <psputility.h>
#include <psputility_netparam.h>
//#define DEBUG_JAVACALL_NETWORK 1

#define BYPASS_CMWAP_GATEWAY 1

struct _DNSHandle;
#define MAX_HOST_LENGTH 256
#define MAX_IPBYTES_SIZE 32
#define DNS_CACHE_SIZE 16

#define PSP_CONSTATE_PROFILE_NAME       0   /*char [64] */ 
#define PSP_CONSTATE_BSSID              1   /*u8 [6] */ 
#define PSP_CONSTATE_SSID               2   /*char [32] */ 
#define PSP_CONSTATE_SSID_LENGTH        3   /*u32*/ 
#define PSP_CONSTATE_SECURITY_TYPE      4   /*u32*/ 
#define PSP_CONSTATE_STRENGTH           5   /*u8*/ 
#define PSP_CONSTATE_CHANNEL            6   /*u8*/ 
#define PSP_CONSTATE_POWER_SAVE         7   /*u8*/ 
#define PSP_CONSTATE_IP                 8   /*char[16] */ 
#define PSP_CONSTATE_SUBNETMASK         9   /*char[16]*/ 
#define PSP_CONSTATE_GATEWAY           10   /*char[16]*/ 
#define PSP_CONSTATE_PRIMDNS           11   /*char[16] */ 
#define PSP_CONSTATE_SECDNS            12   /*char[16] */ 
#define PSP_CONSTATE_USEPROXY          13   /*u32*/ 
#define PSP_CONSTATE_PROXYURL          14   /*char[128] */ 
#define PSP_CONSTATE_PROXYPORT         15   /*u16*/ 
#define PSP_CONSTATE_8021_EAP_TYPE     16   /*u32*/ 
#define PSP_CONSTATE_STARTBROWSER      17   /*u32*/ 
#define PSP_CONSTATE_WIFISP            18   /*u32*/

typedef struct _DNSHandle{
	struct _DNSHandle* next;
	int ok;
	int ip_len;
	int max_ip_len;	
	SceUID res_thread_id;
	int resolver_id;
	javacall_handle monitor_handle;
	char ip[MAX_IPBYTES_SIZE];
	char hostname[MAX_HOST_LENGTH];
} DNSHandle;

typedef struct _DNS_CACHE{
	char hostname[MAX_HOST_LENGTH];
	char ipbytes[32];
	int    len;
	int    hit;
} DNS_CACHE;

static int network_initialized = 0;
static DNSHandle* dnsqueue = NULL;
static DNS_CACHE dns_cache[DNS_CACHE_SIZE]={0};

extern int netDialog(int);
extern void socket_read_buffer_init();

static int isNetConnected() {
	int state;
	sceNetApctlGetState(&state);
#ifdef DEBUG_JAVACALL_NETWORK
    	javacall_printf("isNetConnected: return %d\n", state);
#endif
	
	return state==4?1:0;
}

/**
 * Performs platform-specific initialization of the networking system.
 * Will be called ONCE during VM startup before opening a network connection.
 * 
 * @retval JAVACALL_OK      success
 * @retval JAVACALL_WOULD_BLOCK caller must call the finish function to 
 * 			complete the operation
 * @retval JAVACALL_FAIL    fail
 */
javacall_result javacall_network_init_start(void) {
    int err;
    int start = 0;
    
    if (network_initialized) {
    	return JAVACALL_OK;
    }

    
/*
    if((err = pspSdkInetInit())) {
    	javacall_printf(": Error, could not initialise the network %08X\n", err);		
	return JAVACALL_FAIL;
    }
*/
	if((err = sceNetInit(384*1024, 42, 4*1024, 42, 4*1024))) {
    	printf(": Error, could not sceNetInit the network %08X\n", err);		
	return JAVACALL_FAIL;
    }
	if((err = sceNetInetInit())) {
    	printf(": Error, could not sceNetInetInit the network %08X\n", err);		
	return JAVACALL_FAIL;
    }
	if((err = sceNetResolverInit())) {
    	printf(": Error, could not sceNetResolverInit the network %08X\n", err);		
	return JAVACALL_FAIL;
    }	
	if((err = sceNetApctlInit(0x8000, 48))) {
    	printf(": Error, could not sceNetApctlInit the network %08X\n", err);		
	return JAVACALL_FAIL;
    }

    socket_read_buffer_init();
    
    network_initialized = 1;
    return JAVACALL_OK;
}

 static char confname[128];
char* javacall_network_get_profile(int index) {
    if (sceUtilityCheckNetParam(index) == 0) {
    	sceUtilityGetNetParam(index, PSP_NETPARAM_NAME, (netData *)confname);
#ifdef DEBUG_JAVACALL_NETWORK
    	javacall_printf("Networking initializing: Found wifi config (%s)\n", confname);
#endif
    	return confname;
    }

    return NULL;
}

javacall_result javacall_network_connect_profile(int index) {
    int i, err, n = 0;
    for (i = 1; i < 128; i++) {
    	if (sceUtilityCheckNetParam(i) == 0) {
    	    n++;
    	    if (n == index) {
#ifdef DEBUG_JAVACALL_NETWORK
    	        javacall_printf("Connecting to %d ...\n", i);
#endif
               if (err = sceNetApctlConnect(i) != 0) {
#ifdef DEBUG_JAVACALL_NETWORK
		     javacall_printf(": sceNetApctlConnect returns %08X\n", err);
#endif
    	            return JAVACALL_FAIL;
               } else {
                   return JAVACALL_OK; 
               }
    	    }
    	}
    }
    
    return JAVACALL_FAIL;
}

javacall_result javacall_network_connect_state(int* state) {
    int err = sceNetApctlGetState(state);
    if (err != 0)		{		
#ifdef DEBUG_JAVACALL_NETWORK
        javacall_printf(": sceNetApctlGetState returns $%x\n", err);
#endif
        return JAVACALL_FAIL;		
    } else {
#ifdef DEBUG_JAVACALL_NETWORK
        javacall_printf(" sceNetApctlGetState state %d\n", *state);
#endif
        return JAVACALL_OK;
    }
}

void javacall_network_disconnect() {
    sceNetApctlDisconnect();
}

/**
 * Finishes platform-specific initialization of the networking system.
 * The function is invoked be the JVM after receiving JAVACALL_NETWORK_UP
 * notification from the platform.
 * @retval JAVACALL_OK      success
 * @retval JAVACALL_WOULD_BLOCK caller must call the finish function to 
 * 			complete the operation
 * @retval JAVACALL_FAIL    fail
 */
javacall_result javacall_network_init_finish(void) {
    return JAVACALL_OK;
}  
    
/**
 * Performs platform-specific finalization of the networking system.
 * Will be called ONCE during VM shutdown.
 *
 * @retval JAVACALL_WOULD_BLOCK caller must call xxx_finalize_finish
 *         function to complete the operation
 * @retval JAVACALL_OK      success
 * @retval JAVACALL_FAIL    fail
 */
javacall_result javacall_network_finalize_start(void){
    return JAVACALL_OK;
}

/**
 * Finishes platform-specific finalize of the networking system.
 * The function is invoked be the JVM after receiving JAVACALL_NETWORK_DOWN
 * notification from the platform.
 * @retval JAVACALL_OK      success
 * @retval JAVACALL_FAIL    fail
 */
javacall_result javacall_network_finalize_finish(void)
{
    return JAVACALL_FAIL;
}

static javacall_result lookup_dns_cache(char *hostname,
    /*OUT*/ unsigned char *pAddress, int maxLen, /*OUT*/ int *pLen) {
    int i;
    for (i = 0; i < DNS_CACHE_SIZE; i++) {
        if (stricmp(dns_cache[i].hostname, hostname) == 0) {
            if (dns_cache[i].len <= maxLen) {
                memcpy(pAddress, dns_cache[i].ipbytes, dns_cache[i].len);
                *pLen = dns_cache[i].len;
                dns_cache[i].hit++;
#ifdef DEBUG_JAVACALL_NETWORK    
                javacall_printf("dns cache hit\n");
#endif
                return JAVACALL_OK;
            }
        }
    }
#ifdef DEBUG_JAVACALL_NETWORK    
    javacall_printf("dns cache miss\n");
#endif
    return JAVACALL_FAIL;
}

static void add_dns_cache(char* hostname, unsigned char*ipbytes, int len) {
    int i;
    unsigned int mh = (unsigned int)-1;
    int selected = 0;
    
    if (len > MAX_IPBYTES_SIZE) {
    	javacall_printf("too long ipbytes that cannot add into dns cache\n");
    	return;
    }
    
    for (i = 0; i < DNS_CACHE_SIZE; i++) {
        if (dns_cache[i].hostname[0] == '\0') {
            selected = i;
            break;
        } else {
            if (dns_cache[i].hit < mh) {
                mh = dns_cache[i].hit;
                selected = i;
            }
        }
    }
    
    strncpy(dns_cache[selected].hostname, hostname, MAX_HOST_LENGTH);
    memcpy(dns_cache[selected].ipbytes, ipbytes, len);
    dns_cache[selected].len = len;
#ifdef DEBUG_JAVACALL_NETWORK    
    javacall_printf("add to dns cache %d\n", selected);
#endif
}

void monitor_timeout(javacall_handle handle) {
	DNSHandle* p;	
        for (p = dnsqueue; p != 0; p = p->next) {
        	if (p->monitor_handle == handle) {
        		sceNetResolverStop(p->resolver_id);
        		return;
        	}
        }
}

int resolve_thread(SceSize args, void *argp) {
	char buf[1024];
	struct in_addr addr;
       SceUID rid;

       DNSHandle* hdns = *(DNSHandle**)argp;
       u32 err;


       do {

             if (hdns == NULL) {
             		javacall_print("FATAL: resolve_thread: passed DNSHandle* is NULL\n");
             		break;
             }
#if 1
        	/* Create a resolver */		
        	if(sceNetResolverCreate(&rid, buf, sizeof(buf)) < 0)	{
        		javacall_printf("Error creating resolver\n");			
        		break;
        	}
#ifdef DEBUG_JAVACALL_NETWORK        	
        	javacall_printf("Created resolver %08x\n", rid);
#endif	
        	hdns->resolver_id = rid;
#endif
              int retry = 3;
              int ret;
        	do {
        		javacall_handle monitor_handle;
         		if (JAVACALL_FAIL ==
         			javacall_time_initialize_timer(15000, JAVACALL_FALSE, 
         										monitor_timeout, &monitor_handle)) {
        
        			javacall_print("Error, could not create monitor timer\n");
        			ret = -1;
        			break;
         		}
         		hdns->monitor_handle = monitor_handle;
                	
                	/* Resolve a name to an ip address */	
                	ret = sceNetResolverStartNtoA(hdns->resolver_id, hdns->hostname, &addr, 2, 3);
#ifdef DEBUG_JAVACALL_NETWORK
                	javacall_printf("sceNetResolverStartNtoA (%d) return %d\n", hdns->resolver_id, ret);	
#endif
                	
         		javacall_time_finalize_timer(monitor_handle);
        	} while( --retry > 0 && ret < 0);

        	sceNetResolverStop(rid);
        	sceNetResolverDelete(rid);

        	if (ret < 0) {
#ifdef DEBUG_JAVACALL_NETWORK
        		javacall_printf("Error resolving %s\n", hdns->hostname);
#endif
        		break;
        	}
        	
        	int len = sizeof(addr.s_addr);
        	if (len <= hdns->max_ip_len) {
#ifdef DEBUG_JAVACALL_NETWORK
        		javacall_printf("Resolved %s to %s\n", hdns->hostname, inet_ntoa(addr));
#endif
        		hdns->ip_len = len;
        		memcpy(hdns->ip, &addr.s_addr, len);
        		hdns->ok = 1;
        		add_dns_cache(hdns->hostname, hdns->ip, hdns->ip_len);
        		javanotify_socket_event(JAVACALL_EVENT_NETWORK_GETHOSTBYNAME_COMPLETED,
        								hdns, JAVACALL_OK);
        		sceKernelExitDeleteThread(0);
        		return 0;
        	} else {
        		javacall_print("FATAL:Not enough space to put ip string\n");
        		break;
        	}
       } while(0);
       
	javanotify_socket_event(JAVACALL_EVENT_NETWORK_GETHOSTBYNAME_COMPLETED,
        								hdns, JAVACALL_FAIL);

       sceKernelExitDeleteThread(-1);
       	
       return -1;
}

static void* start_lookup_ip(char* hostname, int maxIpLen) {
	//SceUID rid;
	//static char buf[1024];
	SceUID thid = sceKernelCreateThread("resolver_thread", resolve_thread, 0x11, 16 * 1024, PSP_THREAD_ATTR_USER, NULL);	
	if(thid < 0) {	
		javacall_printf("Error, could not create thread\n");	
		return NULL;	
	}
	
	DNSHandle* hdns = (DNSHandle*)malloc(sizeof(DNSHandle));
	if (hdns == NULL) {
		javacall_printf("Error, could not allocate memory for DNSHandle\n");	
		return NULL;	
	}
	hdns->res_thread_id = thid;
#if 0
	/* Create a resolver */		
       if(sceNetResolverCreate(&rid, buf, sizeof(buf)) < 0)	{
       	javacall_printf("Error creating resolver\n");
       	return NULL;
       }
#ifdef DEBUG_JAVACALL_NETWORK        	
        javacall_printf("Created resolver %08x\n", rid);
#endif	
       hdns->resolver_id = rid;
#endif
	strncpy(hdns->hostname, hostname, sizeof(hdns->hostname));
	hdns->hostname[ sizeof(hdns->hostname) - 1 ] = '\0';
	hdns->ok = 0;
	hdns->max_ip_len = maxIpLen;
	hdns->next = dnsqueue;
	dnsqueue = hdns;
	sceKernelStartThread(thid, sizeof(&hdns), &hdns);
	return hdns;
}

static int end_lookup_ip(DNSHandle* handle, char* pAddress, int maxLen, int* pLen) {
	SceUID thid = handle->res_thread_id;
	
	//sceKernelTerminateThread(thid);			
	//sceKernelWaitThreadEndCB(thid, NULL);					
	//sceKernelDeleteThread(thid);
#if 0
	sceNetResolverStop(handle->resolver_id);
       sceNetResolverDelete(handle->resolver_id);
#endif
	int ok = handle->ok;
	if (ok) {
        	*pLen = handle->ip_len;
        	memcpy(pAddress, handle->ip, *pLen);
	}

	if (dnsqueue == handle) {
		dnsqueue = handle->next;
	} else {
        	DNSHandle* p;	
        	for (p = dnsqueue; p != 0; p = p->next) {
        		if (p->next == handle) {
        			p->next = handle->next;
        		}
        	}
	}

	free(handle);	

	return ok?0:-1;
}

/**
 * Initiates lookup of the given host name to find its IP address.
 *
 * @param hostname the host name for which an ip address is needed,
 *        a null-terminated string
 * @param pAddress base of byte array to receive the address
 * @param maxLen size of buffer at pAddress
 * @param pLen number of bytes returned to pAddress, 4 if it's an 
 *        IPv4 address, 16 if it is an IPv6 address
 * @param pHandle address of variable to receive the handle to for
 *        unblocking the Java thread; this is set
 *        only when this function returns JAVACALL_WOULD_BLOCK.
 * @param pContext address of a pointer variable to receive the context;
 *        this is set only when the function returns JAVACALL_WOULD_BLOCK.
 *
 * @retval JAVACALL_OK                  success
 * @retval JAVACALL_FAIL                if there is a network error
 * @retval JAVACALL_INVALID_ARGUMENT    if maxLen is too small to receive the address
 */
javacall_result javacall_network_gethostbyname_start(char *hostname,
    unsigned char *pAddress, int maxLen, /*OUT*/ int *pLen, /*OUT*/ void **pHandle, /*OUT*/ void **pContext) {

    struct in_addr addr;
    (void)pContext;
    static int selected = 0;
    int connected = isNetConnected();

    if (!connected) {
    	if (!selected) {
        	connected = netDialog(0);
        	selected = 1;
        	if (!connected) {
        		return JAVACALL_FAIL;
        	}
    	} else {
    		return JAVACALL_FAIL;
    	}
    } else {
       //netDialog(1);
    }
	

#ifdef DEBUG_JAVACALL_NETWORK
    javacall_printf("javacall_network_gethostbyname_start: %s\n", hostname);
#endif

#ifdef BYPASS_CMWAP_GATEWAY
    if (!strcmp(hostname, "10.0.0.172")) {
#ifdef DEBUG_JAVACALL_NETWORK
        javacall_print("CMWAP gateway - 10.0.0.172 is not supported!\n");
#endif
        return JAVACALL_FAIL;
    }
#endif

    if(sceNetInetInetAton(hostname, &addr) != 0) {
#ifdef DEBUG_JAVACALL_NETWORK
    	javacall_printf("sceNetInetInetAton ok\n");
#endif
    	if (sizeof(addr) <= maxLen) {
           memcpy(pAddress, &addr.s_addr, sizeof(addr));
           *pLen = sizeof(addr);
    	    return JAVACALL_OK;
    	}
    }
#ifdef DEBUG_JAVACALL_NETWORK
        javacall_printf("sceNetInetInetAton fail\n");
#endif
    if (JAVACALL_OK == lookup_dns_cache(hostname, pAddress, maxLen, pLen)) {
#ifdef DEBUG_JAVACALL_NETWORK
    	 javacall_printf("dns cache hit\n");
#endif
        return JAVACALL_OK;
    }
    
#ifdef DEBUG_JAVACALL_NETWORK    
    javacall_printf("dns cache miss\n");
#endif

    *pHandle = start_lookup_ip(hostname, maxLen);
    if (*pHandle) {
        return JAVACALL_WOULD_BLOCK;
    } else {
        return JAVACALL_FAIL;
    }
}  
    
/**
 * Finishes a pending host name lookup operation.
 * 
 * @param pAddress base of byte array to receive the address
 * @param maxLen size of buffer at pAddress
 * @param pLen number of bytes returned to pAddress, 4 if it's an 
 *        IPv4 address, 16 if it is an IPv6 address
 * @param handle the handle returned by the gethostbyname_start function
 * @param context the context returned by the gethostbyname_start function
 *
 * @retval JAVACALL_OK          success
 * @retval JAVACALL_WOULD_BLOCK if the caller must call the finish function again to complete the operation;\n
 * @retval JAVACALL_FAIL        if there is a network error
 */
javacall_result javacall_network_gethostbyname_finish(unsigned char *pAddress,
    int maxLen,int *pLen,void *handle,void *context) {
    
    if (end_lookup_ip(handle, pAddress, maxLen, pLen) == 0) {
#ifdef DEBUG_JAVACALL_NETWORK    
        javacall_printf("javacall_network_gethostbyname_finish ok\n");	
#endif
    	 return JAVACALL_OK;
    } else {
#ifdef DEBUG_JAVACALL_NETWORK    
        javacall_printf("javacall_network_gethostbyname_finish fail\n");
#endif
        return JAVACALL_FAIL;     
    }
}  

/**
 * Gets an option's value for a platform-specific TCP socket or datagram
 *
 * @param handle handle of an open connection
 * @param flag socket option to get. Must be one of the values defined in
 *             <tt>javax.microedition.io.SocketConnection</tt>
 * @param pOptval returns the option's value
 *
 * @retval JAVACALL_OK      success
 * @retval JAVACALL_FAIL    if there is a network error
 */
//pcsl_network_getsockopt
javacall_result /*OPTIONAL*/ javacall_network_getsockopt(javacall_handle handle, javacall_socket_option flag, /*OUT*/ int *pOptval) {

    int level = SOL_SOCKET;
    int optname;
    socklen_t optsize = sizeof(optname);
    struct linger lbuf ;
    void * opttarget = (void *) pOptval ;

    int fd = (int)handle;
    switch (flag) { 
    case JAVACALL_SOCK_DELAY: /* DELAY */
        level = IPPROTO_TCP;
        optname = TCP_NODELAY;
        break;
    case JAVACALL_SOCK_LINGER: /* LINGER */
        opttarget = (void *) &lbuf ;
        optsize = sizeof (struct linger);
        optname = SO_LINGER;
        break;
    case JAVACALL_SOCK_KEEPALIVE: /* KEEPALIVE */
        optname = SO_KEEPALIVE;
        break;
    case JAVACALL_SOCK_RCVBUF: /* RCVBUF */
        optname = SO_RCVBUF;
        break;
    case JAVACALL_SOCK_SNDBUF: /* SNDBUF */
        optname = SO_SNDBUF;
        break;
    default:
        return JAVACALL_FAIL;
    }

    if (getsockopt(fd, level,  optname, opttarget, &optsize) == 0 ) {
    	 if (optname == TCP_NODELAY) {
    	     if (*pOptval == 0) {
    	         *pOptval = 1;
    	     } else {
    	         *pOptval =0;
    	     }
    	 }

        if (optname == SO_LINGER) {
            /* If linger is on return the number of seconds. */
            *pOptval = (lbuf.l_onoff == 0 ? 0 : lbuf.l_linger) ;
        }
        return JAVACALL_OK;
    } else {
        return JAVACALL_FAIL;
    }
}
    
/**
 * Sets an option's value for a platform-specific TCP socket or datagram
 *
 * @param handle handle of an open connection
 * @param flag socket option to set. Must be one of the values defined in
 *             <tt>javax.microedition.io.SocketConnection</tt>
 * @param optval the value to be set for this option
 *
 * @retval JAVACALL_OK                  success
 * @retval JAVACALL_FAIL                if there is a network error
 * @retval JAVACALL_INVALID_ARGUMENT    if the platform did not accept the value for the option changed
 */
//pcsl_network_setsockopt
javacall_result /*OPTIONAL*/ javacall_network_setsockopt(javacall_handle handle, javacall_socket_option flag, int optval) {

    int    level = SOL_SOCKET;
    int    optsize =  sizeof(optval);
    int    optname;
    struct linger lbuf ;
    void * opttarget = (void *) & optval ;

    int fd = (int)handle;
    
#ifdef DEBUG_JAVACALL_NETWORK
    javacall_printf("javacall_network_setsockopt(flag=0x%x, optval=0x%x)\n",  flag, optval);
#endif

    switch (flag) { 
    case JAVACALL_SOCK_DELAY: /* DELAY */
        level = IPPROTO_TCP;
        optname = TCP_NODELAY;
        if (optval == 0) {
            optval = 1;
        } else {
            optval = 0;
        }
        break;
    case JAVACALL_SOCK_LINGER: /* LINGER */
        opttarget = (void *) &lbuf ;
        optsize = sizeof (struct linger);
        optname = SO_LINGER;
        if (optval == 0) {
            lbuf.l_onoff = 0;
            lbuf.l_linger = 0;
        } else {
            lbuf.l_onoff = 1;
            lbuf.l_linger = optval;
        }
        break;
    case JAVACALL_SOCK_KEEPALIVE: /* KEEPALIVE */
        optname = SO_KEEPALIVE;
        break;
    case JAVACALL_SOCK_RCVBUF: /* RCVBUF */
        optname = SO_RCVBUF;
        if (optval < 8192) optval = 8192;
        break;
    case JAVACALL_SOCK_SNDBUF: /* SNDBUF */
        optname = SO_SNDBUF;
        if (optval < 8192) optval = 8192;
        break;
    default:
        return JAVACALL_INVALID_ARGUMENT;
    }

    if (setsockopt(fd, level,  optname, opttarget, optsize) != 0) {
#ifdef DEBUG_JAVACALL_NETWORK
    javacall_printf("setsockopt fail.\n");
#endif
        return JAVACALL_FAIL;
    }

    return JAVACALL_OK;
}


///////////////////////////////////////////////////////////////////////

/**
 * Gets the IP address of the local socket endpoint.
 *
 * @param handle handle of an open connection
 * @param pAddress base of byte array to receive the address
 *
 * @retval JAVACALL_OK      success
 * @retval JAVACALL_FAIL    if there was an error
 */
javacall_result /*OPTIONAL*/ javacall_socket_getlocaladdr(
    javacall_handle handle,
    char *pAddress) {
    javacall_print("javacall: not implemented : javacall_socket_getlocaladdr\n");
    return JAVACALL_FAIL;
}

/**
 * Gets the IP address of the remote socket endpoint.
 *
 * @param handle handle of an open connection
 * @param pAddress base of byte array to receive the address
 *
 * @retval JAVACALL_OK      success
 * @retval JAVACALL_FAIL    if there was an error
 */
javacall_result /*OPTIONAL*/ javacall_socket_getremoteaddr(
    void *handle,
    char *pAddress)
{
    javacall_print("javacall: not implemented! : javacall_socket_getremoteaddr\n");
    return JAVACALL_FAIL;
}

/**
 * Gets the port number of the local socket endpoint.
 *
 * @param handle handle of an open connection
 * @param pPortNumber returns the local port number
 *
 * @retval JAVACALL_OK      success
 * @retval JAVACALL_FAIL    if there was an error
 */
javacall_result /*OPTIONAL*/ javacall_socket_getlocalport(javacall_handle handle,int *pPortNumber) {
    javacall_print("javacall: not implemented : javacall_socket_getlocalport\n");
    return JAVACALL_FAIL;
}

/**
 * Gets the port number of the remote socket endpoint.
 *
 * @param handle handle of an open connection
 * @param pPortNumber returns the local port number
 *
 * @retval JAVACALL_OK      success
 * @retval JAVACALL_FAIL    if there was an error
 */
javacall_result javacall_socket_getremoteport(
    void *handle,
    int *pPortNumber)
{
    javacall_print("javacall: not implemented : javacall_socket_getremoteport\n");
    return JAVACALL_FAIL;
}

/**
 * Gets the string representation of the local device's IP address.
 * This function returns dotted quad IP address as a string in the
 * output parameter and not the host name.
 *
 * @param pLocalIPAddress base of char array to receive the local
 *        device's IP address
 *
 * @retval JAVACALL_OK      success
 * @retval JAVACALL_FAIL    if there is a network error
 */
javacall_result /*OPTIONAL*/ javacall_network_get_local_ip_address_as_string(/*OUT*/ char *pLocalIPAddress) {
    javacall_print("javacall: not implemented : javacall_network_get_local_ip_address_as_string\n");
    return JAVACALL_FAIL;
}

/**
 * Translates the given IP address into a host name.
 *
 * @param ipn Raw IP address to translate
 * @param hostname the host name. The value of <tt>host</tt> is set by
 *             this function.
 * @param pHandle address of variable to receive the handle to for
 *        unblocking the Java thread; this is set
 *        only when this function returns JAVACALL_WOULD_BLOCK.
 * @param pContext address of a pointer variable to receive the context;
 *        this is set only when the function returns JAVACALL_WOULD_BLOCK.
 *
 * @retval JAVACALL_OK          success
 * @retval JAVACALL_FAIL        if there is a network error
 * @retval JAVACALL_WOULD_BLOCK if the caller must call the finish function to complete the operation
 */
javacall_result /*OPTIONAL*/ javacall_network_gethostbyaddr_start(int ipn,
    char *hostname, /*OUT*/ javacall_handle* pHandle, void **pContext) {

    /*
     * Note there is no spec requirement to look up the the address to
     * to get a hostname. It is not a good idea to perform the lookup,
     * since this function is called EVERY time
     * a datagram is received and on a cell phone network this lookup
     * can take some time and really effect performance for receiving
     * datagrams.
     */
    javacall_print("javacall: not implemented : javacall_network_gethostbyaddr_start\n");
    return JAVACALL_FAIL;
}

/**
 * See javacall_network.h for definition.
 */
javacall_result javacall_server_socket_set_notifier(javacall_handle handle, javacall_bool set) {
    return JAVACALL_FAIL;
}

/**
* See javacall_network.h for definition.
*/ 
int javacall_network_error(void * handle) {
    return 0;
}

/**
 * Finishes a pending host name lookup operation.
 *
 * @param ipn Raw IP address to translate
 * @param hostname the host name. The value of <tt>host</tt> is set by
 *             this function.
 * @param pHandle address of variable to receive the handle to for
 *        unblocking the Java thread; this is set
 *        only when this function returns JAVACALL_WOULD_BLOCK.
 * @param context the context returned by the getHostByAddr_start function
 *
 * @retval JAVACALL_OK          success
 * @retval JAVACALL_FAIL        if there is a network error
 * @retval JAVACALL_WOULD_BLOCK if the caller must call the finish function again to complete the operation
 */
javacall_result /*OPTIONAL*/ javacall_network_gethostbyaddr_finish(int ipn,
    char *hostname, /*OUT*/ javacall_handle* pHandle, void *context) {
    javacall_print("javacall: not implemented : javacall_network_gethostbyaddr_finish\n");
    return JAVACALL_FAIL;
}

/**
 * Gets the http / https proxy address. This method is
 * called when the <tt>com.sun.midp.io.http.proxy</tt> or
 <tt>com.sun.midp.io.https.proxy</tt> internal property
 * is retrieved.
 *
 * @param pHttpProxy base of char array to receive the hostname followed
 *          by ':' and port. - ex) webcache.thecompany.com:8080.
 *          Size of the pHttpProxy should be (MAX_HOST_LENGTH + 6).
 * @param pHttpsProxy base of char array to receive the hostname followed
 *          by ':' and port. - ex) webcache.thecompany.com:8080.
 *          Size of the pHttpsProxy should be (MAX_HOST_LENGTH + 6).
 *
 * @retval JAVACALL_OK      success
 * @retval JAVACALL_FAIL    if there is a network error
 */
javacall_result /*OPTIONAL*/ javacall_network_get_http_proxy(/*OUT*/ char *pHttpProxy, /*OUT*/ char *pHttpsProxy) {
    u32 isUseProxy;
    u16 port;
    static char url[MAX_HOST_LENGTH];
#ifdef DEBUG_JAVACALL_NETWORK    
    javacall_print("javacall_network_get_http_proxy\n");
#endif
    if (sceNetApctlGetInfo(PSP_CONSTATE_USEPROXY, &isUseProxy) || !isUseProxy) {
#ifdef DEBUG_JAVACALL_NETWORK    
    	 javacall_print("javacall_network_get_http_proxy not set\n");
#endif
        return JAVACALL_FAIL;
    }

    if (sceNetApctlGetInfo(PSP_CONSTATE_PROXYURL, url) ||
    	 sceNetApctlGetInfo(PSP_CONSTATE_PROXYPORT, &port)) {
#ifdef DEBUG_JAVACALL_NETWORK    
    	 javacall_print("javacall_network_get_http_proxy failed to get url or port\n");
#endif
        return JAVACALL_FAIL;
    }

    javacall_printf("url:%s:%d\n", url, (unsigned int)port);
    
    if (pHttpProxy) {
    	snprintf(pHttpProxy, MAX_HOST_LENGTH+6 , "%s:%d", url, (unsigned int)port);
    }

    if (pHttpsProxy) {
    	return JAVACALL_FAIL;
    }
#ifdef DEBUG_JAVACALL_NETWORK    
    javacall_print("javacall_network_get_http_proxy sucess\n");
#endif
    return JAVACALL_OK;
}

javacall_result /*OPTIONAL*/ javacall_network_get_local_host_name(/*OUT*/ char *pLocalHost) {
    javacall_print("javacall: not implemented : javacall_network_get_local_host_name \n");
    return JAVACALL_FAIL;
}

/**
    ??????????????????? undefined
 */
char* javacall_inet_ntoa(void *address) {
    javacall_print("javacall: not implemented : javacall_inet_ntoa\n");
    return NULL; //win32: return inet_ntoa(*((struct in_addr*)address));
}
 
/** @} */
#ifdef __cplusplus
}
#endif

