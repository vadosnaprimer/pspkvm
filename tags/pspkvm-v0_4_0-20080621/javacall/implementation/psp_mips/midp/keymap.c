#include <javacall_keymap.h>

#include "pspctrl.h"

#define PSP_CTRL_ANALOG_LEFT 	0x10000000
#define PSP_CTRL_ANALOG_RIGHT 	0x20000000
#define PSP_CTRL_ANALOG_UP 		0x40000000
#define PSP_CTRL_ANALOG_DOWN 	0x80000000

#define PSP_KEY_NUMBER 25

static const javacall_keymap default_keymap[PSP_KEY_NUMBER] = {
       {0,                  JAVACALL_KEY_INVALID, 0},  //dummy
       /** Shift not pressed */
       {PSP_CTRL_UP , JAVACALL_KEY_2, 0},	
	{PSP_CTRL_DOWN , JAVACALL_KEY_8, 0},
	{PSP_CTRL_LEFT , JAVACALL_KEY_4, 0},
	{PSP_CTRL_RIGHT , JAVACALL_KEY_6, 0},
	{PSP_CTRL_SQUARE, JAVACALL_KEY_1, 0},
	{PSP_CTRL_TRIANGLE, JAVACALL_KEY_3, 0},
	{PSP_CTRL_CROSS, JAVACALL_KEY_0, 0},
	{PSP_CTRL_CIRCLE , JAVACALL_KEY_SELECT, 0},
	{PSP_CTRL_SELECT, JAVACALL_KEY_SOFT1, 0},
	{PSP_CTRL_START, JAVACALL_KEY_SOFT2, 0},	
	
       /** Shift pressed */	
	{PSP_CTRL_UP , JAVACALL_KEY_INVALID, 1},	
	{PSP_CTRL_DOWN , JAVACALL_KEY_INVALID, 1},
	{PSP_CTRL_LEFT , JAVACALL_KEY_INVALID, 1},
	{PSP_CTRL_RIGHT , JAVACALL_KEY_INVALID, 1},
	{PSP_CTRL_SQUARE, JAVACALL_KEY_7, 1},
	{PSP_CTRL_TRIANGLE, JAVACALL_KEY_9, 1},	
	{PSP_CTRL_CROSS, JAVACALL_KEY_CLEAR, 1},
	{PSP_CTRL_CIRCLE , JAVACALL_KEY_5, 1},
	{PSP_CTRL_SELECT, JAVACALL_KEY_ASTERISK, 1},
	{PSP_CTRL_START, JAVACALL_KEY_POUND, 1},

	/** Analog */
       {PSP_CTRL_ANALOG_UP , JAVACALL_KEY_UP, 0},
	{PSP_CTRL_ANALOG_DOWN , JAVACALL_KEY_DOWN, 0},
	{PSP_CTRL_ANALOG_LEFT, JAVACALL_KEY_LEFT, 0},
	{PSP_CTRL_ANALOG_RIGHT, JAVACALL_KEY_RIGHT, 0},
	
};

static javacall_key java_key_table[] = {
	JAVACALL_KEY_UP,
	JAVACALL_KEY_DOWN,
	JAVACALL_KEY_LEFT,
	JAVACALL_KEY_RIGHT,
	JAVACALL_KEY_1,
	JAVACALL_KEY_2,
	JAVACALL_KEY_3,
	JAVACALL_KEY_4,
	JAVACALL_KEY_5,
	JAVACALL_KEY_6,
	JAVACALL_KEY_7,
	JAVACALL_KEY_8,
	JAVACALL_KEY_9,
	JAVACALL_KEY_0,
	JAVACALL_KEY_ASTERISK,
	JAVACALL_KEY_POUND,
	JAVACALL_KEY_SELECT,
	JAVACALL_KEY_CLEAR,
	JAVACALL_KEY_SOFT1,
	JAVACALL_KEY_SOFT2,
	JAVACALL_KEY_SPACE,
	JAVACALL_KEY_BACKSPACE
};

static javacall_keymap current_keymap[PSP_KEY_NUMBER];

void javacall_keymap_init() {
    int i;
    for (i = 0; i < PSP_KEY_NUMBER; i++ ) {
        current_keymap[i] = default_keymap[i];
    }
}

int javacall_keymap_size() {
    return PSP_KEY_NUMBER;
}

javacall_keymap* javacall_keymap_current() {
    return &current_keymap[0];
}

javacall_keymap javacall_keymap_set(int iJavakey, int iNativeKey) {
    int i;
    static javacall_keymap prev ={0,0,0};
    //printf("javacall_keymap_set: %d, %d\n", iJavakey, iNativeKey);

    if (iNativeKey >= 0 && iNativeKey < PSP_KEY_NUMBER &&
    	 iJavakey >= 0 && iJavakey < sizeof(java_key_table)/sizeof(javacall_key)) {
        prev = current_keymap[iNativeKey];
        current_keymap[iNativeKey].javaKey = java_key_table[iJavakey];
        current_keymap[iNativeKey].shift = (iNativeKey > 10 && iNativeKey < 21);
    }
    
    return prev;
}

void javacall_keymap_setDefaultKeymap(){
	printf("javacall_keymap_setDefaultKeymap\n");
	javacall_keymap_init();
}

void javacall_keymap_resetKeymap(){
    int i;
    printf("javacall_keymap_resetKeymap\n");
    for (i = 0; i < PSP_KEY_NUMBER; i++ ) {
        current_keymap[i] = default_keymap[i];
        current_keymap[i].javaKey = JAVACALL_KEY_INVALID;
    }
}

int javacall_keymap_getDefaultKey(int iJavakey) {
    int i;
    if (iJavakey >= 0 && iJavakey < javacall_keymap_javakey_number()) {
        for (i = 0; i < PSP_KEY_NUMBER; i++ ) {
            if (default_keymap[i].javaKey== java_key_table[iJavakey]) {
                return i;
            }
        }
    }

    return 0;
}

int javacall_keymap_javakey_number() {
    return sizeof(java_key_table)/sizeof(javacall_key);
}

