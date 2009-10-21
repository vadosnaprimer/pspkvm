#include <kni.h>
#include <sni.h>
#include <midpError.h>
#include <midpUtilKni.h>
#include <commonKNIMacros.h>
#include <string.h>
#include "javacall_keymap.h"

// Native support for the PSPKVM extension
// class com.pspkvm.keypad.KeyMapInfo.

#define PSP_KEY_NUMBER 25

// Found out how a given PSP control code is going to come back
// in terms of the J2ME/Javacall standard control
KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_pspkvm_keypad_KeyMapInfo_getCurrentKeyMapForNativeControl) {
	int pspctrl = (int)KNI_GetParameterAsInt(1);
	int shift = (int)KNI_GetParameterAsInt(2);
	if (shift !=0) {
		shift = 1; }
	javacall_keymap* c = javacall_keymap_current();
	int i;
	for(i=0; i <PSP_KEY_NUMBER; i++) {
		if (c[i].nativeKey == (unsigned int)pspctrl) {
			if (c[i].shift == shift) {
				KNI_ReturnInt(c[i].javaKey); } } }
	KNI_ReturnInt(JAVACALL_KEY_INVALID); }
