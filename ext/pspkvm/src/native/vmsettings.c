/**
 * @file vmsettings.c
 *
 * PSPKVM VMSettings implementation--KNI backend
 */

#include <commonKNIMacros.h>
#include <midpEventUtil.h>
#include <midpString.h>
#include <midpMalloc.h>
#include <midpUtilKni.h>
#include <midpError.h>
#include "vmsettings_store.h"

/**
 * IMPL_NOTE:(Doxy - Change this format) 
 *
 * FUNCTION:      get
 * CLASS:         com.pspkvm.system.VMSettings
 * TYPE:          virtual native function
 * OVERVIEW:      Get a configuration parameter
 * INTERFACE (operand stack manipulation):
 *   parameters:  k key for the param to retrn
 *   returns: the value of the the matching config param (or "" if null)
 */

KNIEXPORT KNI_RETURNTYPE_OBJECT
KNIDECL(com_pspkvm_system_VMSettings_get) {
  KNI_StartHandles(2);
  KNI_DeclareHandle(k);
  KNI_DeclareHandle(v);
  
  KNI_GetParameterAsObject(1, k);
  int kl = KNI_GetStringLength(k);
  jchar* ks = (jchar*) midpMalloc(kl * sizeof(jchar));
  if (ks == NULL) {
    KNI_ThrowNew(midpOutOfMemoryError, NULL); }
  else {
  	KNI_GetStringRegion(k, 0, kl, ks);
  	jchar* vs;
  	unsigned int vl;
  	vmsettings_get(ks, kl, &vs, &vl);
  	KNI_NewString(vs, vl, v); }
	KNI_EndHandlesAndReturnObject(v); }
	
/**
 * IMPL_NOTE:(Doxy - Change this format) 
 *
 * FUNCTION:      commit
 * CLASS:         com.pspkvm.system.VMSettings
 * TYPE:          virtual native function
 * OVERVIEW:      Commit the configuration to storage
 */
KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_pspkvm_system_VMSettings_commit) {
	// Call the C++ code
	vmsettings_commit();
	KNI_ReturnVoid(); }

/**
 * IMPL_NOTE:(Doxy - Change this format) 
 *
 * FUNCTION:      set
 * CLASS:         com.pspkvm.system.VMSettings
 * TYPE:          virtual native function
 * OVERVIEW:      Set a configuration parameter
 * INTERFACE (operand stack manipulation):
 *   parameters:  k key for the param to get
 *   parameters:  v value for the param to get
 */
KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_pspkvm_system_VMSettings_set) {
  KNI_StartHandles(2);
  KNI_DeclareHandle(k);
  KNI_DeclareHandle(v);
  
  KNI_GetParameterAsObject(1, k);
  KNI_GetParameterAsObject(2, v);
  // Create two temporary jchar arrays to copy stuff
  // into the C++ stuff safely.
	jchar *ks, *vs;
	vs = ks = NULL;
  int kl = KNI_GetStringLength(k);
  ks = (jchar*) midpMalloc(kl * sizeof(jchar));
  if (ks == NULL) {
    KNI_ThrowNew(midpOutOfMemoryError, NULL); }
  else {
    KNI_GetStringRegion(k, 0, kl, ks);
		int vl = KNI_GetStringLength(v);
	  vs = (jchar*) midpMalloc(vl * sizeof(jchar));
	  if (vs == NULL) {
	  	midpFree(ks);
	  	ks = NULL;
	    KNI_ThrowNew(midpOutOfMemoryError, NULL); }
	  else {
	    KNI_GetStringRegion(v, 0, vl, vs);
	    vmsettings_set(ks, kl, vs, vl); } }
	if (ks != NULL) {
		midpFree(ks); }
	if (vs != NULL) {
		midpFree(vs); }
  KNI_EndHandles()
	KNI_ReturnVoid(); }

