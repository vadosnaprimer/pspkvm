package com.pspkvm.system;

public class Power {
    public static native boolean isPowerOnline();
    public static native boolean isBatteryExist();
    public static native boolean isBatteryCharging();
    public static native boolean isLowBattery();
    public static native int getBatteryLifePercent();
    public static native int getBatteryLifeTime();
    public static native int getBatteryTemp();
    public static native int getBatteryVolt();
    public static native int getCpuClockFrequency();
    public static native int getBusClockFrequency();
    public static native void setCpuClockFrequency(int cpufreq);
    public static native void setBusClockFrequency(int busfreq);
    public static native void SetClockFrequency(int pllfreq, int cpufreq, int busfreq); 		
}

