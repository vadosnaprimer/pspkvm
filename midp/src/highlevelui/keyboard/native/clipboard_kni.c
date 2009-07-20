/**
 * @file clipboard_kni.c
 *
 * PSPKVM clipboard implementation -- native backend
 */

#include <commonKNIMacros.h>
#include <midpEventUtil.h>
#include <midpString.h>
#include <midpMalloc.h>
#include <midpUtilKni.h>
#include <midpError.h>

// Persistent static global variables for clipboard content
jchar* clipboardStr=NULL;
int clipboardSize=0;

/**
 * IMPL_NOTE:(Doxy - Change this format) 
 *
 * FUNCTION:      set
 * CLASS:         javax.microedition.lcdui.Clipboard
 * TYPE:          virtual native function
 * OVERVIEW:      Set the clipboard.
 * INTERFACE (operand stack manipulation):
 *   parameters:  str the value to set
 */
KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(javax_microedition_lcdui_Clipboard_set) {

	int strLen;

  KNI_StartHandles(1);
  KNI_DeclareHandle(str);

  KNI_GetParameterAsObject(1, str);
  strLen = KNI_GetStringLength(str);
  if (clipboardStr != NULL) {
		midpFree(clipboardStr);
		clipboardSize=0; }
  clipboardStr = (jchar*) midpMalloc(strLen * sizeof(jchar));
  if (clipboardStr == NULL) {
      KNI_ThrowNew(midpOutOfMemoryError, NULL);
  } else {
      KNI_GetStringRegion(str, 0, strLen, clipboardStr);
			clipboardSize=strLen; }

  KNI_EndHandles();
  KNI_ReturnVoid(); }


/**
 * IMPL_NOTE:(Doxy - Change this format) 
 *
 * FUNCTION:      get
 * CLASS:         javax.microedition.lcdui.Clipboard
 * TYPE:          virtual native function
 * OVERVIEW:      Get the clipboard.
 * INTERFACE (operand stack manipulation):
 *   returns: the content of the clipboard (or "" if null)
 */
KNIEXPORT KNI_RETURNTYPE_OBJECT
KNIDECL(javax_microedition_lcdui_Clipboard_get) {
  jchar dummy;  

  KNI_StartHandles(1);
  KNI_DeclareHandle(r);
  
	// Suspect KNI_NewString might flip out over a null--
	// this avoids that.
  if (clipboardSize==0) {
		KNI_NewString(&dummy, 0, r); }
	else {
		KNI_NewString(clipboardStr, clipboardSize, r); }

	KNI_EndHandlesAndReturnObject(r); }
