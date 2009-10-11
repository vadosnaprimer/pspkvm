package com.pspkvm.keypad;

// Native interface to get hold of the raw control state, when necessary. 
public class RawState {

	// Get analog stick's X param
	public static native int getAnalogX();
	
	// Get analog stick's Y param
	public static native int getAnalogY();

}
