package com.pspkvm.system;

/*
	PSP-specific extension--provides WiFi and IP network system status. 
*/

public class WifiStatus {

	// WiFi card is powered on
	public static native boolean isPowerOn();
	
	// MAC address of WiFi card
	public static native String getMACAddr();
	
	// Switch state of WiFi card
	public static native boolean isSwitchOn();
	
 	// Base station SSID--in 802.11x wireless, this is its MAC.
	public static native String getBSSID();
	
	// Profile name used to connect
	public static native String getProfileName();
	
	// The SSID of the base 
	public static native String getSSID();
	
	// IP address of the PSP
	public static native String getIP();
	
	// Local net's subnet mask
	public static native String getSubnetMask();
	
	// Default gateway
	public static native String getGateway();
	
	// Primary DNS
	public static native String getPrimaryDNS();
	
	// Secondary DNS
	public static native String getSecondaryDNS();
	
	// Web proxy URL, if assigned
	public static native String getProxyURL();
	
	// Ints
	// Security type--can be:
	// 	PSP_SECURITY_TYPE_NONE
	//	PSP_SECURITY_TYPE_WEP
	//	PSP_SECURITY_TYPE_WPA
	public static native int getSecurityType();
	
	// Signal strength, as percentage (0-100)
	public static native int getSignalStrength();
	
	// Channel in use
	public static native int getChannel();
	
	// Wireless system's power-saving mode 
	public static native int getPowerSave();
	
	// Web proxy should be used
	public static native int getUseProxy();
	
	// Web proxy port (see also getProxyURL)
	public static native int getProxyPort();
	
	// EAP type in use--can be:
	//	PSP_EAP_TYPE_NONE
	//	PSP_EAP_TYPE_EAP_MD5
	public static native int getEAPType();
	
	// Autostart browser on conect
	public static native int getStartBrowser();
	
	// Use WiFi service provider hotspots
	public static native int getUseWiFiSP();
	
	// Status codes
	// All int status functions return -1 if an error occurs
	public static final int PSP_NET_UNKNOWN = -1;
	// Security types
	public static final int PSP_SECURITY_TYPE_NONE = 0;
	public static final int PSP_SECURITY_TYPE_WEP = 1;
	public static final int PSP_SECURITY_TYPE_WPA = 2;
	// EAP types
	public static final int PSP_EAP_TYPE_NONE = 0;
	public static final int PSP_EAP_TYPE_EAP_MD5 = 1;
	// True/False returns
	public static final int PSP_NET_TRUE = 1;
	public static final int PSP_NET_FALSE = 0;
	
	// Helper function--various booleans
	public static String netBoolToString(int i) {
		switch(i) {
			case PSP_NET_UNKNOWN: return "<unknown>";
			case PSP_NET_TRUE: return "yes";
			case PSP_NET_FALSE: return "no";
			default: return "<unknown>"; } }
	
	// Helper function--EA5 mode display
	public static String getEA5ModeStr() {
		int i = getEAPType();
		switch(i) {
			case PSP_NET_UNKNOWN: return "<unknown>";
			case PSP_EAP_TYPE_NONE: return "None";
			case PSP_EAP_TYPE_EAP_MD5: return "EAP-MD5";
			default: return "<unknown>"; } }
	
	// Helper function--security mode display
	public static String getSecurityTypeStr() {
		int i = getSecurityType();
		switch(i) {
			case PSP_NET_UNKNOWN: return "<unknown>";
			case PSP_SECURITY_TYPE_NONE: return "None";
			case PSP_SECURITY_TYPE_WEP: return "WEP";
			case PSP_SECURITY_TYPE_WPA: return "WPA";
			default: return "<unknown>"; } }
	
	// Helper function--signal strength display
	public static String getSignalStrengthStr() {
		int i = getSignalStrength();
		if (i==-1) {
			return "<unknown>"; }
		return Integer.toString(i) + "%"; }
	
	// Helper function--general int display
	static String netIntToString(int i) {
		if (i==-1) {
			return "<unknown>"; }
		return Integer.toString(i); }

	// Convenience method--make a string return into something pretty
	static String formatConfigString(String n, String v) {
		return n + ": " + ((v==null) ? "<unknown>" : v); }
	
	// Convenience method--make a boolean return into something pretty
	static String formatConfigString(String n, boolean b) {
		Boolean bl = new Boolean(b);
		return n + ": " + bl.toString(); }

	// Generate a general network status report 
	public static String statusReport() {
		return
			formatConfigString("WLAN power", isPowerOn()) + "\n" +
			formatConfigString("WLAN switch", isSwitchOn()) + "\n" +
			formatConfigString("MAC", getMACAddr())+ "\n" +
			formatConfigString("Profile", getProfileName())+ "\n" +
			formatConfigString("BSSID", getBSSID())+ "\n" +
			formatConfigString("SSID", getSSID())+ "\n" +
			formatConfigString("Security", getSecurityTypeStr())+ "\n" +
			formatConfigString("Signal Str", getSignalStrengthStr()) + "\n" +
			formatConfigString("Channel", netIntToString(getChannel()))+ "\n" +
			formatConfigString("Power Save", netBoolToString(getPowerSave()))+ "\n" +
			formatConfigString("IP", getIP())+ "\n" + 
			formatConfigString("Subnet", getSubnetMask())+ "\n" +
			formatConfigString("Gateway", getGateway())+ "\n" +
			formatConfigString("DNS 1", getPrimaryDNS())+ "\n" +
			formatConfigString("DNS 2", getSecondaryDNS())+ "\n" +
			formatConfigString("EAP Mode", getEA5ModeStr()); }

}
