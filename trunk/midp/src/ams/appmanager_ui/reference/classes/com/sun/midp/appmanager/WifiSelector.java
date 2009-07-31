package com.sun.midp.appmanager;

import javax.microedition.lcdui.*;
import com.sun.midp.i18n.Resource;
import com.sun.midp.i18n.ResourceConstants;
import javax.microedition.midlet.*;

public class WifiSelector extends MIDlet implements CommandListener {
    Display display;

    boolean connect_stop = false;

    List wifiprof;

    Alert alertConnectOK;
    Alert alertConnectFail;
    
    // Returns from getConnectState()
	final static int PSP_NET_APCTL_STATE_DISCONNECTED = 0;
	final static int PSP_NET_APCTL_STATE_SCANNING = 1;
	final static int PSP_NET_APCTL_STATE_JOINING = 2;
	final static int PSP_NET_APCTL_STATE_GETTING_IP = 3;
	final static int PSP_NET_APCTL_STATE_GOT_IP = 4;
	final static int PSP_NET_APCTL_STATE_EAP_AUTH = 5;
	final static int PSP_NET_APCTL_STATE_KEY_EXCHANGE = 6;
    
    Command backCmd =
        new Command(Resource.getString(ResourceConstants.BACK),
                    Command.BACK, 1);

    Command cancelCmd =
        new Command(Resource.getString(ResourceConstants.CANCEL),
                    Command.CANCEL, 1);

    Command selectWIFICmd =
        new Command("Select",
                    Command.SCREEN, 1);

    Alert alertConnecting = new Alert("Connecting to network", "",
    	                                               null,
    	                                               AlertType.INFO);

    public WifiSelector() {        
    	alertConnecting.addCommand(cancelCmd);
    	alertConnecting.setCommandListener(this);
    }

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

            display = Display.getDisplay(this);

            for (i = 1; i < 32; i++) {
               prof = lookupWifiProfile(i);
               if (prof == null) {
               	continue;
               }
               wifiprof.append(prof, null);
               found = true;
            }

            wifiprof.setCommandListener(this);

            if (found) {
               wifiprof.addCommand(selectWIFICmd);
               wifiprof.setSelectedIndex(0, true);               
               display.setCurrent(wifiprof);
            } else {
               display.setCurrent(notFoundAlert, wifiprof);
            }
    }

    /**
     * Pause; there are no resources that need to be released.
     */
    public void pauseApp() {
    }

    /**
     * Destroy cleans up.
     *
     * @param unconditional is ignored; this object always
     * destroys itself when requested.
     */
    public void destroyApp(boolean unconditional) {
    }

    public void commandAction(Command c, Displayable s) {
    	if (c == cancelCmd) {
    	    connect_stop = true;
    	} if (c == backCmd) {
            // goto back to the manager midlet
            notifyDestroyed();
        } else if (s == wifiprof &&
                       (c == List.SELECT_COMMAND || c == selectWIFICmd)) {

    
                    
            alertConnectOK = new Alert("Connecting to network",
    	                                               "Successfully connected using profile " + wifiprof.getString(wifiprof.getSelectedIndex()),
    	                                               null,
    	                                               AlertType.INFO);

            alertConnectFail = new Alert("Connecting to network",
    	                                               "Failed to connect using profile " + wifiprof.getString(wifiprof.getSelectedIndex()),
    	                                               null,
    	                                               AlertType.WARNING);

            if (connect(wifiprof.getSelectedIndex() + 1) != 0) {
            	display.setCurrent(alertConnectFail, wifiprof);
            }
            new Thread() {
            	  public void run() {
            	      int state = 0, laststate = 0;
                    while (!connect_stop) {
                        state = getConnectState();
                    	   if (state == 4 || state < 0) {
                    	   	break;
                    	   }
                    	   
                    	   if (state != laststate) {
                    	   	laststate = state;
                    	       alertConnecting.setString("Connecting using profile "
														 	+ wifiprof.getString(wifiprof.getSelectedIndex())
															+ "\n  (" + stateIntToString(state) +"...)");
                    	   }
                    	   
                    	   try {
                    	       sleep(500);
                    	   } catch (InterruptedException e) {
                    	   }
                    }


                    if (state == 4) {
                        display.setCurrent(alertConnectOK, wifiprof);
                    } else {                        
    	                 disconnect();
                        display.setCurrent(alertConnectFail, wifiprof);
                    }
            	  }
            }.start();
            alertConnecting.setTimeout(Alert.FOREVER);
            display.setCurrent(alertConnecting, wifiprof);
        }
    }

    
    private native String lookupWifiProfile (int index);
    private native int connect (int index);
    private native int getConnectState ();
    private native void disconnect();
    
	// Translate a received state (from getConnectState()) to a string description
	String stateIntToString(int s) {
		switch(s) {
			case PSP_NET_APCTL_STATE_DISCONNECTED: return "Disconnected";
			case PSP_NET_APCTL_STATE_SCANNING: return "Scanning";
			case PSP_NET_APCTL_STATE_JOINING: return "Joining";
			case PSP_NET_APCTL_STATE_GETTING_IP: return "Requesting IP address";
			case PSP_NET_APCTL_STATE_GOT_IP: return "Received IP address";
			case PSP_NET_APCTL_STATE_EAP_AUTH: return "Authenticating";
			case PSP_NET_APCTL_STATE_KEY_EXCHANGE: return "Exchanging keys";
			default: return "Unknown state"; } }
			
}
	