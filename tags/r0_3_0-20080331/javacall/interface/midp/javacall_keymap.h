#ifndef __JAVACALL_KEYMAP_H
#define __JAVACALL_KEYMAP_H

#ifdef __cplusplus
extern "C" {
#endif

#include "javacall_defs.h"
#include <javacall_keypress.h>

typedef struct {
	unsigned int nativeKey;
	javacall_key javaKey;
	int shift;
} javacall_keymap;

void javacall_keymap_init();
int javacall_keymap_size();
javacall_keymap* javacall_keymap_current();
javacall_keymap javacall_keymap_set(int iJavakey, int iNativekey);
void javacall_keymap_setDefaultKeymap();
void javacall_keymap_resetKeymap();
int javacall_keymap_getDefaultKey(int iJavakey);
int javacall_keymap_javakey_number();

#ifdef __cplusplus
}
#endif

#endif  
