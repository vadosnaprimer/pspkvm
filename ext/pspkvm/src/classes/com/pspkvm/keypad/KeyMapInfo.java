package com.pspkvm.keypad;

public class KeyMapInfo {

	// Found out how a given PSP control code is going to come back
	// in terms of the J2ME/Javacall standard control
	public static native int getCurrentKeyMapForNativeControl(int pspControlCode, boolean shift);

}