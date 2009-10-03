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

#ifdef __cplusplus
}
#endif

#endif // #ifndef _vmsettings_store_h
