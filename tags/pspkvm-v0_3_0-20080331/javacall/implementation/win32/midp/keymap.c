#include <javacall_keymap.h>


void javacall_keymap_init() {
}

int javacall_keymap_size() {
    return 0;
}

javacall_keymap* javacall_keymap_current() {
    return NULL;
}

javacall_keymap javacall_keymap_set(int iJavakey, int iNativeKey) {
    int i;
    static javacall_keymap prev ={0,0,0};
    
    return prev;
}

void javacall_keymap_setDefaultKeymap(){
}

void javacall_keymap_resetKeymap(){
}

int javacall_keymap_getDefaultKey(int iJavakey) {
    return 0;
}

int javacall_keymap_javakey_number() {
    return 0;
}


