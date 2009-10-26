#include <kni.h>
#include <sni.h>
#include <midpError.h>
#include <midpUtilKni.h>
#include <commonKNIMacros.h>
#include <string.h>

// These are storage points the sample loop drops the lx and ly values in 
// on every sample -- see javacall/implementation/psp_mips/keypress.c

unsigned char ext_pspkvm_rawstate_lx = 0;
unsigned char ext_pspkvm_rawstate_ly = 0;

// Native support for the PSPKVM extension
// class com.pspkvm.system.Power.
// All calls (as of this writing) go through the
// PSP SDK's pspctrl.h header.

#include <pspctrl.h>

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_pspkvm_keypad_RawState_getAnalogX) {
	KNI_ReturnInt((int)ext_pspkvm_rawstate_lx); }

KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_pspkvm_keypad_RawState_getAnalogY) {
	KNI_ReturnInt((int)ext_pspkvm_rawstate_ly); }
