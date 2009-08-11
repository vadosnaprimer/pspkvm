package com.pspkvm.system;

/* PSP-specific: reports battery lifetime et al */

public class Power {

		// Various boolean status returns
    public static native boolean isPowerOnline();
    public static native boolean isBatteryExist();
    public static native boolean isBatteryCharging();
    public static native boolean isLowBattery();

		// Battery life as a percentage
    public static native int getBatteryLifePercent();
    public static String getBatteryLifePercentStr() {
    	return Integer.toString(getBatteryLifePercent()) + "%"; }

    // Battery life as estimated time
    public static native int getBatteryLifeTime();
    public static String getBatteryLifeTimeStr() {
    	int m = getBatteryLifeTime();
    	return Integer.toString(m/60) + ":" + minField(m%60); }

    // Battery temperature
    public static native int getBatteryTemp();
    public static String getBatteryTempStr() {
			return Integer.toString(getBatteryTemp()) + "\u00B0C"; }

		// Battery output voltage
    public static native int getBatteryVolt();
    public static String getBatteryVoltStr() {
			return Double.toString(((double)getBatteryVolt())/1000.) + " VDC"; }

    public static native int getCpuClockFrequency();
    public static native int getBusClockFrequency();
    /*public static native void setCpuClockFrequency(int cpufreq);
    public static native void setBusClockFrequency(int busfreq);
    public static native void SetClockFrequency(int pllfreq, int cpufreq, int busfreq); */

		// General diagnostics report
		public static String getPowerSummary() {
			return "Battery: " + getBatteryLifePercentStr() + " / " +
				getBatteryLifeTimeStr() + " remaining\nTemp: " +
				getBatteryTempStr() + "\nVoltage: " + getBatteryVoltStr(); }

    // Field prettifier
    private static String minField(int m) {
    	String r = Integer.toString(m);
    	if (m<10) {
    		return "0" + r; }
    	return r; }
    

}
