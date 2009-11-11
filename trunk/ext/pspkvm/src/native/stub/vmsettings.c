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
  KNI_StartHandles(1);
  KNI_DeclareHandle(v);
  
  KNI_EndHandlesAndReturnObject(v); 
}
	
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
	KNI_ReturnVoid(); 
}

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
	KNI_ReturnVoid(); 
}

