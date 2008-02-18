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
            wifiprof = new List("WIFI setup", List.EXCLUSIVE);
            wifiprof.addCommand(backCmd);

            display = Display.getDisplay(this);

            for (i = 1; i < 128; i++) {
               prof = lookupWifiProfile(i);
               if (prof == null) {
               	continue;
               }
               wifiprof.append(prof, null);
            }

            if (i > 1) {
               wifiprof.addCommand(selectWIFICmd);
               wifiprof.setSelectedIndex(0, true);
            }           

            wifiprof.setCommandListener(this);
            
            display.setCurrent(wifiprof);
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
        } else if (c == selectWIFICmd) {

    
                    
            alertConnectOK = new Alert("Connecting to network",
    	                                               "Successfully connected by profile " + wifiprof.getString(wifiprof.getSelectedIndex()),
    	                                               null,
    	                                               AlertType.INFO);

            alertConnectFail = new Alert("Connecting to network",
    	                                               "Failed connect to profile " + wifiprof.getString(wifiprof.getSelectedIndex()),
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
                    	   	/*
                    	   	String[] stateString = {
                    	   		new String("Connecting to WLAN..."),
					new String("Configuring to WLAN..."),
					new String("Obtaining IP..."),
					new String("Connected OK to " + wifiprof.getString(wifiprof.getSelectedIndex()))
                    	   	}*/
                    	   	laststate = state;
                    	       alertConnecting.setString("Connecting by profile " + wifiprof.getString(wifiprof.getSelectedIndex()) + " Step " + state);
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

    
}
