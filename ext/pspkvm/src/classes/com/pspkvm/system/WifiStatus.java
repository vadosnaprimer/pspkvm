package com.pspkvm.system;

/*
	Utility class--provides WiFi status details via javacall interfaces 
*/

public class WifiStatus {
	
	 	// State stuff we can do queries on
 		// Strings
 		public static native String getBSSID();
		public static native String getProfileName(); 
 		public static native String getSSID();
 		public static native String getIP();
 		public static native String getSubnetMask();
 		public static native String getGateway();
 		public static native String getPrimaryDNS();
 		public static native String getSecondaryDNS();
 		public static native String getProxyURL();
 		// Ints
 		public static native int getSecurityType();
		public static native int getSignalStrength();
		public static native int getChannel();
		public static native int getPowerSave();
		public static native int getUseProxy();
		public static native int getProxyPort();
		public static native int getEAPType();
		public static native int getStartBrowser();
		public static native int getUseWiFiSP();
		
		// Few status codes
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
			
		public static String statusReport() {
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
