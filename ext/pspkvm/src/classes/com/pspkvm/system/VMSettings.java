package com.pspkvm.system;

import java.util.*;
import java.io.*;

/*
	This is a settings implementation allowing us to set/get settings
	that are VM-specific--fonts, virtual keyboards, etc. */

public class VMSettings {

	// Public accessor
	public static native String get(String k);

	// Public accessor
	public static native void set(String k, String v);

	// Public accessor (call before VM closes
	// to commit changes)
	public static native void commit();

	// Public accessor
	public static int getInt(String k, int d) {
		String v = get(k);
		try {
			return Integer.parseInt(v); }
		catch(Exception e) {
			return d; } }

	// Public accessor
	public static void setInt(String k, int v) {
		set(k, Integer.toString(v)); }
		
}
