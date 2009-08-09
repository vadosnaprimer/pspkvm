package com.sun.midp.appmanager;

/*
	Utility class--provides WiFi status details via javacall interfaces 
*/

class PSPWifiStatus {
	
	 	// State stuff we can do queries on--TODO: Move somewhere cleaner
 		// Strings
 		static native String getBSSID();
		static native String getProfileName(); 
 		static native String getSSID();
 		static native String getIP();
 		static native String getSubnetMask();
 		static native String getGateway();
 		static native String getPrimaryDNS();
 		static native String getSecondaryDNS();
 		static native String getProxyURL();
 		// Ints
 		static native int getSecurityType();
		static native int getSignalStrength();
		static native int getChannel();
		static native int getPowerSave();
		static native int getUseProxy();
		static native int getProxyPort();
		static native int getEAPType();
		static native int getStartBrowser();
		static native int getUseWiFiSP();
		
		// Few status codes
		// All int status functions return -1 if an error occurs
		static final int PSP_NET_UNKNOWN = -1;
		// Security types
		static final int PSP_SECURITY_TYPE_NONE = 0;
		static final int PSP_SECURITY_TYPE_WEP = 1;
		static final int PSP_SECURITY_TYPE_WPA = 2;
		// EAP types
		static final int PSP_EAP_TYPE_NONE = 0;
		static final int PSP_EAP_TYPE_EAP_MD5 = 1;
		// True/False returns
		static final int PSP_NET_TRUE = 1;
		static final int PSP_NET_FALSE = 0;
		
		// Helper function--various booleans
		static String netBoolToString(int i) {
			switch(i) {
				case PSP_NET_UNKNOWN: return "<unknown>";
				case PSP_NET_TRUE: return "yes";
				case PSP_NET_FALSE: return "no";
				default: return "<unknown>"; } }
		
		// Helper function--EA5 mode display
		static String getEA5ModeStr() {
			int i = getEAPType();
			switch(i) {
				case PSP_NET_UNKNOWN: return "<unknown>";
				case PSP_EAP_TYPE_NONE: return "None";
				case PSP_EAP_TYPE_EAP_MD5: return "EAP-MD5";
				default: return "<unknown>"; } }
		
		// Helper function--security mode display
		static String getSecurityTypeStr() {
			int i = getSecurityType();
			switch(i) {
				case PSP_NET_UNKNOWN: return "<unknown>";
				case PSP_SECURITY_TYPE_NONE: return "None";
				case PSP_SECURITY_TYPE_WEP: return "WEP";
				case PSP_SECURITY_TYPE_WPA: return "WPA";
				default: return "<unknown>"; } }
		
		// Helper function--signal strength display
		static String getSignalStrengthStr() {
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
			
		static String statusReport() {
			return
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
