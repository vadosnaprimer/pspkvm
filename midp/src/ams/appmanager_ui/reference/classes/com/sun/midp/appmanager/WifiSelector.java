package com.sun.midp.appmanager;

import javax.microedition.lcdui.*;
import com.sun.midp.i18n.Resource;
import com.sun.midp.i18n.ResourceConstants;
import javax.microedition.midlet.*;
import com.sun.midp.installer.GraphicalInstaller;

public class WifiSelector extends MIDlet implements CommandListener {
  Display display;
	// Set to signal the connection thread to give up
  boolean connect_stop = false;
	// A list of profiles
  List wifiprof;
  
  // Returns from getConnectState()
	final static int FAILED_RETRIEVING_STATE = -1;
	final static int PSP_NET_APCTL_STATE_DISCONNECTED = 0;
	final static int PSP_NET_APCTL_STATE_SCANNING = 1;
	final static int PSP_NET_APCTL_STATE_JOINING = 2;
	final static int PSP_NET_APCTL_STATE_GETTING_IP = 3;
	final static int PSP_NET_APCTL_STATE_GOT_IP = 4;
	final static int PSP_NET_APCTL_STATE_EAP_AUTH = 5;
	final static int PSP_NET_APCTL_STATE_KEY_EXCHANGE = 6;
	
	final static String SUCCESS_CONNECT = "Successfully connected using profile ";
	final static String FAILED_CONNECT = "Failed to connect using profile ";
	final static String CONNECTING = "Connecting to network"; 
	
	// Icon
	final static Image wifiPtImg =
		GraphicalInstaller.getImageFromInternalStorage("wifipt");
			
	// Helper for attemptConnect()
	void setConnectAlerts() {
		alertConnectOK.setString(SUCCESS_CONNECT +
			wifiprof.getString(wifiprof.getSelectedIndex()));
		alertConnectFail.setString(FAILED_CONNECT +
			wifiprof.getString(wifiprof.getSelectedIndex())); }
  
  // Commands
	Command backCmd =
		new Command(Resource.getString(ResourceConstants.BACK), Command.BACK, 1);
	Command cancelCmd =
		new Command(Resource.getString(ResourceConstants.CANCEL),
			Command.CANCEL, 1);
	Command selectWIFICmd =
		new Command("Select", Command.SCREEN, 1);
	Command getWIFIStatusCmd = 
		new Command("Get WiFi status", Command.SCREEN, 2);
	Command disconnectCmd = 
		new Command("Disconnect", Command.SCREEN, 2);

	// Alerts
	Alert alertConnectOK = new Alert(CONNECTING, "", null, AlertType.INFO);
	Alert alertConnectFail = new Alert(CONNECTING, "", null, AlertType.WARNING);
	Alert alertConnecting = new Alert(CONNECTING, "", null, AlertType.INFO);
	Alert alertNetworkStatus = new Alert("Network status", "", null, AlertType.INFO);

  public WifiSelector() {        
  	alertConnecting.addCommand(cancelCmd);
  	alertConnecting.setCommandListener(this); }

	/**
	* Start.
	*/
	public void startApp() {
		
		String prof;
		int i;
		boolean found = false;
		Alert notFoundAlert = new Alert("Can't find Wifi connections",
			" Please setup Wifi connections in PSP Network Setup",
			null, AlertType.WARNING);
		
		wifiprof = new List("WIFI setup", Choice.IMPLICIT);
		wifiprof.addCommand(backCmd);
		wifiprof.addCommand(getWIFIStatusCmd);
		wifiprof.addCommand(disconnectCmd);
		
		display = Display.getDisplay(this);
		
		for (i = 1; i < 32; i++) {
			prof = lookupWifiProfile(i);
			if (prof == null) {
				continue; }
			wifiprof.append(prof, wifiPtImg);
			found = true; }
		
		wifiprof.setCommandListener(this);
		
		if (found) {
			wifiprof.addCommand(selectWIFICmd);
			wifiprof.setSelectedIndex(0, true);               
			display.setCurrent(wifiprof); }
		else {
			display.setCurrent(notFoundAlert, wifiprof); } }

	/**
	 * Pause; there are no resources that need to be released.
	 */
	public void pauseApp() { }

	/**
	* Destroy cleans up.
	*
	* @param unconditional is ignored; this object always
	* destroys itself when requested.
	*/
	public void destroyApp(boolean unconditional) { }

	public void commandAction(Command c, Displayable s) {
		if (c == getWIFIStatusCmd) {
			launchWiFiStatusAlert();
			return; }
		if (c == disconnectCmd) {
			disconnect();
			return; }
		if (c == cancelCmd) {
			connect_stop = true;
			return; }
		if (c == backCmd) {
			// goto back to the manager midlet
			notifyDestroyed();
			return; }
		if (s == wifiprof &&
			(c == List.SELECT_COMMAND || c == selectWIFICmd)) {
				attemptConnect(); } }
  
  // Called by user to attempt to connect to the selected profile
	void attemptConnect() {
		setConnectAlerts();
    disconnect();
		Thread t = new Thread() {
			public void run() {
				int state = 0, laststate = 0;
		    // Give it half a second for the disconnect to register
				try {
					sleep(500); }
				catch (InterruptedException e) {}
		    connect_stop=false;
				if (connect(wifiprof.getSelectedIndex() + 1) != 0) {
					display.setCurrent(alertConnectFail, wifiprof);
					return; }
				while (!connect_stop) {
					state = getConnectState();
					if (state == 4 || state < 0) {
						break; }
					if (state != laststate) {
						laststate = state;
						alertConnecting.setString("Connecting using profile "
							+ wifiprof.getString(wifiprof.getSelectedIndex())
							+ "\n  (" + stateIntToString(state) +"...)"); }
					try {
						sleep(500); }
					catch (InterruptedException e) { } }
				if (state == 4) {
					display.setCurrent(alertConnectOK, wifiprof); }
				else {                        
					disconnect();
					display.setCurrent(alertConnectFail, wifiprof); } } };
		// End new Thread()
		alertConnecting.setTimeout(Alert.FOREVER);
		alertConnecting.setString("Initializing...");
		display.setCurrent(alertConnecting, wifiprof);
		t.start(); }
 
	// Natives--talk to the platform
	private native String lookupWifiProfile (int index);
	private native int connect (int index);
	private native int getConnectState ();
	private native void disconnect();
			
	// Convenience method--launch the connection status dialog
	void launchWiFiStatusAlert() {
		alertNetworkStatus.setString(com.pspkvm.system.WifiStatus.statusReport());
		alertNetworkStatus.setTimeout(Alert.FOREVER);
		display.setCurrent(alertNetworkStatus, wifiprof); }
 		
	// Translate a received state (from getConnectState()) to a string description
	String stateIntToString(int s) {
		switch(s) {
			case FAILED_RETRIEVING_STATE: return "Failed to retrieve state";
			case PSP_NET_APCTL_STATE_DISCONNECTED: return "Disconnected";
			case PSP_NET_APCTL_STATE_SCANNING: return "Scanning";
			case PSP_NET_APCTL_STATE_JOINING: return "Connecting";
			case PSP_NET_APCTL_STATE_GETTING_IP: return "Requesting IP address";
			case PSP_NET_APCTL_STATE_GOT_IP: return "Received IP address";
			case PSP_NET_APCTL_STATE_EAP_AUTH: return "Authenticating";
			case PSP_NET_APCTL_STATE_KEY_EXCHANGE: return "Exchanging keys";
			default: return "Unknown state"; } }

}
