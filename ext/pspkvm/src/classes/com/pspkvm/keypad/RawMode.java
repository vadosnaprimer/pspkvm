package com.pspkvm.keypad;

public class RawMode {
    public static native void beginRawMode();
    public static native void endRawMode();
    public static native int setSamplingCycle (int cycle);
    public static native int setSamplingMode(int mode);
    public static native int getSamplingCycle ();
    public static native int getSamplingMode();
    public static native void peekBufferPositive (PspCtrlData[] pad_data, int count);
    public static native void peekBufferNegative (PspCtrlData[] pad_data, int count);
    public static native void readBufferPositive (PspCtrlData[] pad_data, int count);
    public static native void readBufferNegative (PspCtrlData[] pad_data, int count);
    public static native void peekLatch (PspCtrlLatch latch_data);
    public static native void readLatch (PspCtrlLatch latch_data);
}

