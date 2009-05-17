package com.pspkvm.system;

public class Wlan {
    public static native boolean isPowerOn();
    public static native void getEtherAddr(byte[] addr);
    public static native int getSwitchState();
}

