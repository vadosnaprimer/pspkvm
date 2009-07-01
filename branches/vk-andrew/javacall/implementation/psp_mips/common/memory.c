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

#include "stdio.h"
#include "stdlib.h"
#include "javacall_defs.h"    
#include "javacall_memory.h"
#include "javacall_properties.h"
#include "malloc.h"

static int max_heap_size = 0;

/** 
 * Allocates large memory heap
 * VM will use this memory heap for internal memory allocation/deallocation
 * Will be called ONCE during VM startup!
 * 
 * @param    size required heap size in bytes
 * @param    outSize actual size of memory allocated
 * @return	  a pointer to the newly allocated memory, or <tt>0</tt> if not available
 */
void* javacall_memory_heap_allocate(int size, /*OUT*/ int* outSize) {
	int sz=64*1024*1024;
	int reserved_heap = 512*1024;
	char* tmpp=NULL;
	char* resv=NULL;
	char* pSize=NULL;

	if ((JAVACALL_OK == javacall_get_property("com.pspkvm.setting.reservedCHeapSize", 
		JAVACALL_INTERNAL_PROPERTY, &pSize)) &&
		(pSize != NULL)) {
		int configSize = atoi(pSize) * 1024;
		if (configSize > reserved_heap) {
			reserved_heap = configSize;
		}
	}

	javacall_printf("Reserved C heap size:%d\n", reserved_heap);
	if (NULL == (resv = malloc(reserved_heap))) {
		return NULL;
	}
	
       while(sz > size) {
		if ((tmpp=malloc(sz))!=NULL) {
			javacall_printf("MAX HEAP:%d\n",sz);
			*outSize = sz;
			max_heap_size = sz;
			free(resv);
			return tmpp;
		}
		sz -= 200*1024;
	}

       free(resv);
       return NULL;
}
    
/** 
 * Free large memory heap
 * VM will call this function once when VM is shutdown to free large memory heap 
 * Will be called ONCE during VM shutdown!
 * 
 * @param    heap memory pointer to free
 */
void javacall_memory_heap_deallocate(void* heap) {
    free(heap);
}
    

/** 
 * Allocates memory of the given size from the private JAVACALL memory
 * pool.
 * 
 * @param    size Number of byte to allocate
 * @return	  a pointer to the newly allocated memory
 */
void* /*OPTIONAL*/ javacall_malloc(unsigned int size){ 
    return malloc(size) ;
}

/**
 * Reallocates memory of the given size from the private JAVACALL memory
 * pool.
 *
 * @param size Number of byte to allocate
 * @param ptr	Pointer to previously allocated memory
 * @return a pointer to the reallocated memory or null if memory could not be reallocated
 */
void* /*OPTIONAL*/  javacall_realloc(void* ptr, unsigned int size) {
    return realloc(ptr, size);
}

    
/**
 * Frees memory at the given pointer in the private JAVACALL memory pool.
 * 
 * @param    ptr	Pointer to allocated memory
 */
void  /*OPTIONAL*/ javacall_free(void* ptr) {
    free(ptr);
}

int javacall_total_heap_size() {
    return max_heap_size;
}
    
#ifdef __cplusplus
}
#endif

 
