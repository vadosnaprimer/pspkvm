#include <javacall_keypress.h>
#include <javacall_keymap.h>
#include <javacall_devemu.h>

#include <pspthreadman.h>
#include <pspctrl.h>

extern int suspend_key_input;
static int key_thread_id = -1;
static int done = 0;

#define PSP_CTRL_ANALOG_LEFT 	0x10000000
#define PSP_CTRL_ANALOG_RIGHT 	0x20000000
#define PSP_CTRL_ANALOG_UP 		0x40000000
#define PSP_CTRL_ANALOG_DOWN 	0x80000000

// Modifications adding RAW_KEY_STATUS send /AJM
// See also higher JVM modifications filtering these from clients that 
// won't recognize them (see CLayer, CWindow).


#define MULTITASK_KEY (PSP_CTRL_LTRIGGER | PSP_CTRL_RTRIGGER | PSP_CTRL_TRIANGLE)
#define EXIT_KEY (PSP_CTRL_LTRIGGER | PSP_CTRL_RTRIGGER | PSP_CTRL_CROSS)
#define DEBUG_TRACE_KEY (PSP_CTRL_LTRIGGER | PSP_CTRL_RTRIGGER | PSP_CTRL_START)
#define NETWORK_KEY (PSP_CTRL_LTRIGGER | PSP_CTRL_RTRIGGER | PSP_CTRL_SQUARE)
#define SHIFT_KEY1 PSP_CTRL_LTRIGGER
#define SHIFT_KEY2 PSP_CTRL_RTRIGGER

#define REPEAT_THRESHOLD 20
#define REPEAT_THRESHOLD1 5

// Externs for storage of raw analogue values -- exposed in 
// ext/pspkvm/src/native/rawstate.c

extern unsigned char ext_pspkvm_rawstate_lx;
extern unsigned char ext_pspkvm_rawstate_ly;

static int KeyThread(SceSize args, void *argp)
{
	SceCtrlData pad;
	unsigned int pspKey = 0, lastPspKey = 0, lastRawPspKey=0;
	int lx, ly;
	static int hold = 0, raw_hold = 0;
	static int repeat_threshold = REPEAT_THRESHOLD, raw_repeat_threshold = REPEAT_THRESHOLD;
	static javacall_key lastPressedJavakey = 0;

	sceCtrlSetSamplingCycle(25);
	sceCtrlSetSamplingMode(PSP_CTRL_MODE_ANALOG);
	while(!done){
		
		//pspDebugScreenSetXY(0, 2);

    sceCtrlReadBufferPositive(&pad, 3);
		if (suspend_key_input) {
			continue;
		}
		
    // Emit the raw key event--if it's changed since the last sample.
    if ((lastRawPspKey != pad.Buttons) ||
				((raw_hold == raw_repeat_threshold)&&(pad.Buttons != 0))) {
			javanotify_key_event(pad.Buttons, JAVACALL_RAW_KEY_STATUS);
			raw_hold = 0;
			lastRawPspKey = pad.Buttons;
			// Set threshold lower if this is a repeat press
			if (raw_hold == raw_repeat_threshold) {
				raw_repeat_threshold = REPEAT_THRESHOLD1; }
			else {
				raw_repeat_threshold = REPEAT_THRESHOLD; } }
		else {
			if (pad.Buttons != 0) {
				raw_hold++; } }

		pspKey = 0;
		lx = pad.Lx;
		ext_pspkvm_rawstate_lx = lx;
		ly = pad.Ly;
		ext_pspkvm_rawstate_ly = ly;
		if (lx < 5) {
			if (ly < 55) {
				pspKey = PSP_CTRL_ANALOG_LEFT | PSP_CTRL_ANALOG_UP;
			} else if (ly > 200) {
				pspKey = PSP_CTRL_ANALOG_LEFT | PSP_CTRL_ANALOG_DOWN;
			} else if (lx < 5) {
				pspKey = PSP_CTRL_ANALOG_LEFT;
			}
		} else if (lx > 251) {
			if (ly < 55) {
				pspKey = PSP_CTRL_ANALOG_RIGHT | PSP_CTRL_ANALOG_UP;
			} else if (ly > 200) {
				pspKey = PSP_CTRL_ANALOG_RIGHT | PSP_CTRL_ANALOG_DOWN;
			} else if (lx > 251) {
				pspKey = PSP_CTRL_ANALOG_RIGHT;
			}
		} else {
			if (ly < 5) {
				pspKey = PSP_CTRL_ANALOG_UP;
			} else if (ly > 251) {
				pspKey = PSP_CTRL_ANALOG_DOWN;
			} else {
				//Staying in center, nothing happen
			}
		}
		

    pspKey |= pad.Buttons;
              
		if (pspKey != lastPspKey || hold == repeat_threshold){
		    int i;
		    int id;
		    int keys;
		    int shift, lastShift; // non-zero if "shift" key is pressed
		    int rotate;
		    javacall_keymap* keymap;

                  if ((pspKey == lastPspKey) && lastPressedJavakey != 0) {
                  	//repeat
                  	javanotify_key_event(lastPressedJavakey, JAVACALL_KEYREPEATED);
                  	hold = 0;
                  	repeat_threshold = REPEAT_THRESHOLD1;
                  	continue;
                  } else {
                     lastPressedJavakey = 0;
		       hold = 0;
		       repeat_threshold = REPEAT_THRESHOLD;
                  }

                  if (lastPspKey == DEBUG_TRACE_KEY) {
                  	if (pspKey == 0) {
		    		lastPspKey = 0;
		    	}
		    	continue;
                  }

		    if (lastPspKey == EXIT_KEY) {
		    	if (pspKey == 0) {
		    		lastPspKey = 0;
		    	}
		    	continue;	    	
		    }
		    
		    if (lastPspKey == MULTITASK_KEY) {
		    	if (pspKey == 0) {
		    		lastPspKey = 0;
		    	}
		    	continue;	    	
		    }

		    if (lastPspKey == NETWORK_KEY) {
		    	if (pspKey == 0) {
		    		lastPspKey = 0;
		    	}
		    	continue;	    	
		    }

		    if (pspKey == NETWORK_KEY) {
		    	javanotify_network_connect();
		    	lastRawPspKey = pad.Buttons;
		    	lastPspKey = pspKey;
		    	continue;
		    }

		    if (pspKey == DEBUG_TRACE_KEY) {
		    	javanotify_key_event(JAVACALL_KEY_DEBUG_TRACE, JAVACALL_KEYPRESSED);
		    	//display_log(1);
		    	lastRawPspKey = pad.Buttons;
		    	lastPspKey = pspKey;
		    	continue;
		    }

		    if (pspKey == EXIT_KEY) {
		    	javanotify_shutdown_current();
		    	lastRawPspKey = pad.Buttons;
		    	lastPspKey = pspKey;
		    	continue;
		    }		    

		    if (pspKey == MULTITASK_KEY) {
		    	javanotify_switch_to_ams();
		    	lastRawPspKey = pad.Buttons;
		    	lastPspKey = pspKey;
		    	continue;
		    }
		    
		    keymap = javacall_keymap_current();
		    keys = javacall_keymap_size();
		    shift = pspKey & (SHIFT_KEY1 | SHIFT_KEY2);
		    lastShift = lastPspKey & (SHIFT_KEY1 | SHIFT_KEY2);
		    rotate = javacall_devemu_get_rotation(javacall_devemu_get_current_device());

		    for (i = 0 ; i < keys; i++) {
		    	//printf("keymap[i].nativekey:%08x, javakey:%d\n", keymap[i].nativeKey, keymap[i].javaKey);
		    	if (keymap[i].javaKey == JAVACALL_KEY_INVALID) {
		    		continue;
		    	}

		    	int javaKey = keymap[i].javaKey;
		    	if (rotate == 90) {
		    		switch (javaKey) {
		    			case JAVACALL_KEY_UP:
		    				javaKey = JAVACALL_KEY_LEFT;
		    				break;
		    			case JAVACALL_KEY_DOWN:
		    				javaKey = JAVACALL_KEY_RIGHT;
		    				break;
		    			case JAVACALL_KEY_LEFT:
		    				javaKey = JAVACALL_KEY_DOWN;
		    				break;
		    			case JAVACALL_KEY_RIGHT:
		    				javaKey = JAVACALL_KEY_UP;
		    				break;
		    			case JAVACALL_KEY_2:
		    				javaKey = JAVACALL_KEY_4;
		    				break;
		    			case JAVACALL_KEY_8:
		    				javaKey = JAVACALL_KEY_6;
		    				break;
		    			case JAVACALL_KEY_4:
		    				javaKey = JAVACALL_KEY_8;
		    				break;
		    			case JAVACALL_KEY_6:
		    				javaKey = JAVACALL_KEY_2;		    				
		    		}
		    	}
		    	int pressed = ((keymap[i].nativeKey & pspKey) && (keymap[i].shift?shift:!shift));
		    	int lastPressed = ((keymap[i].nativeKey& lastPspKey) && (keymap[i].shift?lastShift:!lastShift));
		    	if (pressed!= lastPressed) {
			    	if (pressed) {			    		
			    		javanotify_key_event(javaKey, JAVACALL_KEYPRESSED);
			    		lastPressedJavakey = javaKey;
				    //printf("key %d pressed\n", keymap[i].javaKey);
				} else {
				    	javanotify_key_event(javaKey, JAVACALL_KEYRELEASED);
				    	lastPressedJavakey = 0;
				   	 //printf("key %d released\n", keymap[i].javaKey); 	
				}
			}
		    }
		    
		} else {
		    hold++;
		}
		
		lastPspKey = pspKey;
	}
	return 0;
}

javacall_result javacall_key_init() {
    int thid;

    javacall_print("javacall_key_init\n");
    
    if (key_thread_id >= 0) {
        return JAVACALL_OK;
    }

    done = 0;
    
    thid = sceKernelCreateThread("key_thread", KeyThread,
				     0x11, 0xFA0, PSP_THREAD_ATTR_USER, 0);
    if (thid >= 0) {
    	 sceKernelStartThread(thid, 0, 0);
    	 javacall_print("keythread started\n");
    	 key_thread_id = thid;
        return JAVACALL_OK;
    } else {
        return JAVACALL_FAIL;
    }
}

javacall_result javacall_key_finalize() {
    done = 1;
    if (key_thread_id >= 0) {
        sceKernelWaitThreadEndCB(key_thread_id, NULL);
    }

    key_thread_id = -1;
    return JAVACALL_OK;
}

