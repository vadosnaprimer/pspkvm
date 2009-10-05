#ifndef _vmsettings_store_h
#define _vmsettings_store_h

#include <commonKNIMacros.h>

// C interface to the (C++) vm properties store.

#ifdef __cplusplus
extern "C" {
#endif

// External (C) set
void vmsettings_set(const jchar* k, unsigned int kl,
	const jchar* v, unsigned int vl);

// External (C) get
void vmsettings_get(const jchar* k, unsigned int kl,
	const jchar** v, unsigned int* vl);

// External (C) commit
void vmsettings_commit();

// The functions below are 8-bit char interfaces--generally useful
// more for the C-layers (like the font manager) that have
// to read these properties--not much good to the Java stuff

// Get an integer or default (if there's no such key or it
// doesn't parse as an int)
int vmsettings_getint(const char* k, int d);

// Returns true if value v exists for key k and v equals cmp	
int vmsettings_key_equals(const char* k, const char* cmp);

// End 8-bit char interfaces

#ifdef __cplusplus
}
#endif

#endif // #ifndef _vmsettings_store_h
