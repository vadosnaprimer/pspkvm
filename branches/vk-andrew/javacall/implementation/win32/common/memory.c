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

/**
 * @file
 *
 *
 */

#include <stdio.h>
#include <stdlib.h>

#include "javacall_memory.h"

static int max_heap_size = 0;

/**
 * Allocates large memory heap
 * VM will use this memory heap for internal memory allocation/deallocation
 * Will be called ONCE during VM startup!
 *
 * @param    size required heap size in bytes
 * @param    outSize actual size of memory allocated
 * @return  a pointer to the newly allocated memory,
 *          or <tt>NULL</tt> if not available
 */
void* javacall_memory_heap_allocate(int size, int* outSize) {
	int sz=64*1024*1024;
	char* tmpp=NULL;
       while(sz > size) {
		if ((tmpp=malloc(sz))!=NULL) {
			printf("MAX HEAP:%d\n",sz);
			*outSize = sz;
			max_heap_size = sz;
			return tmpp;
		}
		sz -= 200*1024;
	}
       return NULL;
}

/**
 * Free large memory heap
 * VM will call this function once when VM is shutdown to free the memory heap
 * Will be called ONCE during VM shutdown!
 *
 * @param    heap memory pointer to free
 */
void javacall_memory_heap_deallocate(void* heap) {

    free(heap);
}

/**
 * Allocates memory of the given size from the private JAVACALL memory
 * pool. If memory could not be reallocated function returns null,
 * in this case old pointer is not released.
 *
 * @param size Number of byte to allocate
 * @return a pointer to the newly allocated memory
 */
void* javacall_malloc(unsigned int size) {
    return malloc(size);
}

/**
 * Reallocates memory of the given size from the private JAVACALL memory
 * pool.
 *
 * @param ptr	Pointer to previously allocated memory
 * @param size Number of byte to allocate
 * @return a pointer to the reallocated memory or null if memory could not be reallocated
 */
void* javacall_realloc(void* ptr, unsigned int size) {
    return realloc(ptr, size);
}


/**
 * Frees memory at the given pointer in the private JAVACALL memory pool.
 *
 * @param ptr pointer to allocated memory
 */
void  javacall_free(void* ptr) {
    free(ptr);
}

int javacall_total_heap_size() {
    return max_heap_size;
}

