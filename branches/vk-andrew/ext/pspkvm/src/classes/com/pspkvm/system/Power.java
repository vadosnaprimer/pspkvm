package com.pspkvm.system;

/* PSP-specific: reports power system status */

public class Power {
	
	// Unit is on external power (does not include USB)
	public static native boolean isPowerOnline();
	
	// Battery present
	public static native boolean isBatteryExist();
	
	// Battery is charging (does not include via USB)
	public static native boolean isBatteryCharging();
	
	// Battery charge low
	public static native boolean isLowBattery();
	
	// Battery life as a percentage
	public static native int getBatteryLifePercent();
	public static String getBatteryLifePercentStr() {
		return Integer.toString(getBatteryLifePercent()) + "%"; }
	
	// Battery life as estimated time (minutes)
	public static native int getBatteryLifeTime();
	public static String getBatteryLifeTimeStr() {
		int m = getBatteryLifeTime();
		return Integer.toString(m/60) + ":" + minField(m%60); }
	
	// Battery temperature (degrees Celsius)
	public static native int getBatteryTemp();
	public static String getBatteryTempStr() {
		return Integer.toString(getBatteryTemp()) + "\u00B0C"; }
	
	// Battery output voltage (milliVolts DC)
	public static native int getBatteryVolt();
	public static String getBatteryVoltStr() {
		return Double.toString(((double)getBatteryVolt())/1000.) + " VDC"; }
	
	// CPU frequency (MHz)
	public static native int getCpuClockFrequency();
	
	// Bus frequency (MHz)
	public static native int getBusClockFrequency();
	
	// Not implemented yet:
	/*public static native void setCpuClockFrequency(int cpufreq);
	public static native void setBusClockFrequency(int busfreq);
	public static native void SetClockFrequency(int pllfreq, int cpufreq, int busfreq); */
	
	// Generate a general power system status report
	public static String getPowerSummary() {
		return "Battery: " + getBatteryLifePercentStr() + " / " +
			getBatteryLifeTimeStr() + " remaining\nTemp: " +
			getBatteryTempStr() + "\nVoltage: " + getBatteryVoltStr() +
			"\nExternal power: " + isPowerOnline() +
			" / Battery present: " + isBatteryExist() +
			"\nCharging: " + isBatteryCharging() +
			" / Battery low: " + isLowBattery() +
			"\nCPU clock: " + getCpuClockFrequency() +
			" MHz / Bus clock: " + getBusClockFrequency() + " MHz"; }
	
	// Field format--minutes
	private static String minField(int m) {
		String r = Integer.toString(m);
		if (m<10) {
			return "0" + r; }
		return r; }
	
}
