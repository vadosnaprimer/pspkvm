/*
 *
 *
 * Copyright  1990-2007 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, CA 95054 or visit www.sun.com if you need additional
 * information or have any questions.
 */

package com.sun.midp.appmanager;

import javax.microedition.lcdui.*;

import com.sun.midp.configurator.Constants;

import com.sun.midp.installer.*;
import com.sun.midp.main.*;
import com.sun.midp.midletsuite.*;
import com.sun.midp.midlet.MIDletSuite;
import com.sun.midp.io.j2me.push.PushRegistryInternal;

import com.sun.midp.i18n.Resource;
import com.sun.midp.i18n.ResourceConstants;

import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;

import com.sun.j2me.security.AccessController;
import com.sun.midp.security.*;

import com.sun.midp.payment.PAPICleanUp;

import java.io.*;
import javax.microedition.rms.*;
import java.util.*;

/**
 * The Graphical MIDlet selector Screen.
 * <p>
 * It displays a list (or grid to be exact) of currently installed
 * MIDlets/MIDlet suites (including the Installer MIDlet). Each MIDlet or
 * MIDlet suite is represented by an icon with a name under it.
 * An icon from a jad file for the MIDlet/MIDlet suite representation
 * is used if possible, otherwise a default icon is used.
 *
 * There is a a set of commands per MIDlet/MIDlet suite. Note that
 * the set of commands can change depending on the corresponding MIDlet state.
 * For MIDlets/MIDlet suites that are not running the following commands are
 * available:
 * <ul>
 * <li><b>Launch</b>: Launch the MIDlet or the MIDlet Selector
 *      if it is a suite.
 * <li><b>Remove</b>: Remove the MIDlet/MIDlet suite teh user selected
 *      (with confirmation). </li>
 * <li><b>Update</b>: Update the MIDlet/MIDlet suite the user selected.</li>
 * <li><b>Info</b>: Show the user general information
 *    of the selected MIDlet/MIdlet suite. </li>
 * <li><b>Settings</b>: Let the user change the manager's settings.
 * </ul>
 *
 * For MIDlets/MIDlet suites that are running the following commands are
 * available:
 * <ul>
 * <li><b>Bring to foreground</b>: Bring the running MIDlet to foreground
 * <li><b>End</b>: Terminate the running MIDlet
 * <li><b>Remove</b>: Remove the MIDlet/MIDlet suite teh user selected
 *      (with confirmation). </li>
 * <li><b>Update</b>: Update the MIDlet/MIDlet suite the user selected.</li>
 * <li><b>Info</b>: Show the user general information
 *    of the selected MIDlet/MIdlet suite. </li>
 * <li><b>Settings</b>: Let the user change the manager's settings.
 * </ul>
 *
 * Exactly one MIDlet from a MIDlet suite could be run at the same time.
 * Each MIDlet/MIDlet suite representation corresponds to an instance of
 * AMSMidletCustomItem which in turn maintains a reference to a MIDletSuiteInfo
 * object (that contains info about this MIDlet/MIDlet suite).
 * When a MIDlet is launched or a MIDlet form a MIDlet suite is launched
 * the proxy instance in the corresponding AMSMidletCustomItem is set to
 * a running MIDletProxy value. It is set back to null when MIDlet exits.
 *
 * Running midlets can be distinguished from non-running MIdlets/MIDlet suites
 * by the color of their name.
 */
class AppManagerUI extends Form
    implements ItemCommandListener, CommandListener {

    /** Constant for the discovery application class name. */
    private static final String DISCOVERY_APP =
        "com.sun.midp.installer.DiscoveryApp";

    /** Constant for the certificate manager class name */
    private static final String CA_MANAGER =
        "com.sun.midp.appmanager.CaManager";

    /** Constant for the graphical installer class name. */
    private static final String INSTALLER =
        "com.sun.midp.installer.GraphicalInstaller";

    private static final String WIFI_SELECTOR_APP =
    	 "com.sun.midp.appmanager.WifiSelector";

    private static final String SETTINGS_STORE = "amssettings";

    private static final int LASTPLAY_MIDLET_RECORD_ID = 1;
    // A stream containing the folders
    private static final int FOLDER_STREAM_RECORD_ID = 2;
    // How many records we need
    private static final int RECORDS_NEEDED = 2;



    /** Command object for "Exit" command for splash screen. */
    private Command exitCmd =
        new Command(Resource.getString(ResourceConstants.EXIT),
                    Command.BACK, 1);

    /** Command object for "Launch" install app. */
    private Command launchInstallCmd =
        new Command(Resource.getString(ResourceConstants.LAUNCH),
                    Command.ITEM, 1);

    /** Command object for "Launch" CA manager app. */
    private Command launchCaManagerCmd =
        new Command(Resource.getString(ResourceConstants.LAUNCH),
                    Command.ITEM, 1);
    /** Command object for "Info". */
    private Command infoCmd =
        new Command(Resource.getString(ResourceConstants.INFO),
                    Command.ITEM, 2);
    /** Command object for "Remove". */
    private Command removeCmd =
        new Command(Resource.getString(ResourceConstants.REMOVE),
                    Command.ITEM, 3);
    /** Command object for "Update". */
    private Command updateCmd =
        new Command(Resource.getString(ResourceConstants.UPDATE),
                    Command.ITEM, 4);
    /** Command object for "Application settings". */
    private Command appSettingsCmd =
        new Command(Resource.
                    getString(ResourceConstants.APPLICATION_SETTINGS),
                    Command.ITEM, 5);
    /** Command object for "Select device". */
    private Command deviceSettingCmd =
        new Command("Select device", Command.ITEM, 6);

    /** Command object for "Create folder". */
    private Command createFolderCmd =
        new Command("Create folder", Command.SCREEN, 7);

    /** Command object for "Cancel" command for the remove form. */
    private Command cancelCmd =
        new Command(Resource.getString(ResourceConstants.CANCEL),
                    Command.CANCEL, 1);
    /** Command object for "Remove" command for the remove form. */
    private Command removeOkCmd =
        new Command(Resource.getString(ResourceConstants.REMOVE),
                    Command.SCREEN, 1);

    /** Command object for "Back" command for back to the AppSelector. */
    Command backCmd =
        new Command(Resource.getString(ResourceConstants.BACK),
                    Command.BACK, 1);

    /** Command object for "Yes" command. */
    private Command runYesCmd = new Command(Resource.getString
                                            (ResourceConstants.YES),
                                            Command.OK, 1);

    /** Command object for "No" command. */
    private Command runNoCmd = new Command(Resource.getString
                                           (ResourceConstants.NO),
                                           Command.BACK, 1);

    /** Display for the Manager MIDlet. */
    ApplicationManager manager;

    /** MIDlet Suite storage object. */
    private MIDletSuiteStorage midletSuiteStorage;

    /** Display for the Manager MIDlet. */
    Display display; // = null

    /** Keeps track of when the display last changed, in milliseconds. */
    private long lastDisplayChange;

    /** MIDlet to be removed after confirmation screen was accepted */
    private RunningMIDletSuiteInfo removeMsi;

    /** last Item that was selected */
    private RunningMIDletSuiteInfo lastSelectedMsi;
    
    void setLastSelectedMsi(RunningMIDletSuiteInfo rmsi) {
			lastSelectedMsi = rmsi; }

    /**
     * There are several Application Manager
     * midlets from the same "internal" midlet suite
     * that should not be running in the background.
     * appManagerMidlet helps to destroy them
     * (see AMSMidletCustomItem.showNotify).
     */
    MIDletProxy appManagerMidlet;

    /** UI used to display error messages. */
    private DisplayError displayError;

    /** True, if the CA manager is included. */
    private boolean caManagerIncluded;

    private MIDletSwitcher midletSwitcher;

    private boolean first;
    
  // The folders
  AMSFolderCustomItem rootFolder, currentFolder;
  AMSSystemFolderCustomItem systemFolder;
  
  AMSMidletCustomItem find(MIDletProxy p) {
  	AMSMidletCustomItem r = systemFolder.find(p);
  	if (r!=null) {
			return r; }
		return rootFolder.find(p); }
  
  /* Aux method for first run of the new folders framework --
  		Inits the root folder from the current list of items */
  void initRootFolder() {
  	rootFolder=AMSFolderCustomItem.createRoot(this); }

	// Write folders to the permanent store -- note that initSettings
	// should have ensured there's a record no. 2 in which to put them.
	void writeFolders() {
		RecordStore settings=null;
		try {
			ByteArrayOutputStream bas = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bas);
			settings = RecordStore.openRecordStore(SETTINGS_STORE, true);
			rootFolder.write(dos);
			byte[] bstream = bas.toByteArray();
			settings.setRecord(FOLDER_STREAM_RECORD_ID,
				bstream, 0, bstream.length); }
		catch (RecordStoreException e) {
			/* TODO */ }
		catch (IOException e) {
			/* TODO */ }
		finally {
			if (settings != null) {
			try {
				settings.closeRecordStore(); }
			catch (RecordStoreException e) {
				/* TODO */ } } } }
    
  /* Builds the folder structure from the records store */
  void readFolders() {
  	RecordStore settings=null;
		try {
			settings = RecordStore.openRecordStore(SETTINGS_STORE, false);
			if (settings.getNumRecords() < 2) {
				initRootFolder();
				return; }
			byte[] data = settings.getRecord(FOLDER_STREAM_RECORD_ID);
			if (data==null) {
				initRootFolder();
				return; }
			ByteArrayInputStream das = new ByteArrayInputStream(data);
			DataInputStream di = new DataInputStream(das);
			rootFolder=AMSFolderCustomItem.readRoot(di, this);
			currentFolder=rootFolder;
		} catch (RecordStoreException e) {
			// TODO: Warn
			initRootFolder();
		} catch (IOException e) {
			// TODO: Warn
			initRootFolder();
		} finally {
		if (settings != null) {
			try {
				settings.closeRecordStore();
			} catch (RecordStoreException e) {
		/* Ignore */ } } } }
		
	// Create the folder system
	void createFolders() {
		systemFolder = AMSSystemFolderCustomItem.createSystemRoot(this);
		appendFolder(systemFolder);
		readFolders();
		appendFolder(rootFolder); }
		
	// Special method for appending folders--makes sure
	// if they're already open, we also append their contents
	void appendFolder(AMSFolderCustomItem f) {
		append(f);
		f.insertContents(); }

	// Special method for inserting folders--makes sure
	// if they're already open, we also insert their contents
	void insertFolderAt(int p, AMSFolderCustomItem f) {
		insertAt(p, f);
		f.insertContents(); }

    /**
     * Creates and populates the Application Selector Screen.
     * @param manager - The application manager that invoked it
     * @param displayError - The UI used to display error messages
     * @param display - The display instance associated with the manager
     * @param first - true if this is the first time AppSelector is being
     *                shown
     * @param ms - MidletSuiteInfo that should be selected. For the internal
     *             suites midletToRun should be set, for the other suites
     *             suiteId is enough to find the corresponding item.
     */
  AppManagerUI(ApplicationManager manager, Display display,
               DisplayError displayError, boolean first,
               MIDletSuiteInfo ms) {
      super(null);

      this.first = first;
      caManagerIncluded = false;        
      this.manager = manager;
      this.display = display;
      this.displayError = displayError;

      midletSwitcher = new MIDletSwitcher(this, manager, display);
      midletSuiteStorage = MIDletSuiteStorage.getMIDletSuiteStorage();
      initSettings();
      setTitle(Resource.getString(ResourceConstants.AMS_MGR_TITLE));
      createFolders();
      // updateContent(false);
      addCommand(exitCmd);
      setCommandListener(this);

      if (first) {
          display.setCurrent(this);

          new Thread(new Runnable() {
						public void run() {
      				updateContent(true); } }).start();
          return; }

      // if a MIDlet was just installed
      // getLastInstalledMidletItem() will return AMSMidletCustomItem
      // corresponding to this suite, then we have to prompt
      // the user if he want to launch a midlet from the suite.
      /*AMSMidletCustomItem mci = getLastInstalledMidletItem();
      if (mci != null) {
          askUserIfLaunchMidlet();
          return; } 
      display.setCurrent(this);

      if (ms != null) {
          // Find item to select
          if (ms.suiteId == MIDletSuite.INTERNAL_SUITE_ID) {
              for (int i = 0; i < size(); i++) {
                  AMSMidletCustomItem mi = (AMSMidletCustomItem)get(i);
                  if ((mi.msi.suiteId == MIDletSuite.INTERNAL_SUITE_ID)
                      && (mi.msi.midletToRun.equals(ms.midletToRun))) {
                      display.setCurrentItem(mi);
                      break; } }
            return; }
              for (int i = 0; i < size(); i++) {
                  AMSMidletCustomItem mi = (AMSMidletCustomItem)get(i);
                  if (mi.msi.suiteId == ms.suiteId) {
                      display.setCurrentItem(mi);
                      break;
                  }
          }
        return; }
          int suiteId = getLastPlayedMIDlet();
          if (suiteId != MIDletSuite.UNUSED_SUITE_ID) {
              for (int i = 0; i < size(); i++) {
                  AMSMidletCustomItem mi = (AMSMidletCustomItem)get(i);
                  if (mi.msi.suiteId == suiteId) {
                      display.setCurrentItem(mi);
                      break;
                  }
              }
  }*/
}

    protected void sizeChanged(int w, int h) {
    	/* TODO: Think about this. I'm not sure
    			I want to have to deal with the possibilities
    			of the folder being closed above it 
    	 if (first) {
            int suiteId = getLastPlayedMIDlet();
            if (suiteId != MIDletSuite.UNUSED_SUITE_ID) {
                for (int i = 0; i < size(); i++) {
                    AMSMidletCustomItem mi = (AMSMidletCustomItem)get(i);
                    if (mi.msi.suiteId == suiteId) {
                        display.setCurrentItem(mi);
                        break;
                    }
                }
            }
            first = false;
        }*/
    }

    /**
     * Called when midlet selector needed.
     *
     * @param onlyFromLaunchedList true if midlet should
     *        be selected from the list of already launched midlets,
     *        if false then possibility to launch midlet is needed.
     */
    public void showMidletSwitcher(boolean onlyFromLaunchedList) {
        if (onlyFromLaunchedList && midletSwitcher.hasItems()) {
            display.setCurrent(midletSwitcher);
        } else {
            display.setCurrent(this);
        }
    }

    /**
     * Called to determine MidletSuiteInfo of the last selected Item.
     *
     * @return last selected MidletSuiteInfo
     */
    public RunningMIDletSuiteInfo getSelectedMIDletSuiteInfo() {
        return lastSelectedMsi;
    }

    /**
     * Respond to a command issued on any Screen.
     *
     * @param c command activated by the user
     * @param s the Displayable the command was on.
     */
    public void commandAction(Command c, Displayable s) {
    
        if (c == exitCmd) {
            if (s == this) {
                manager.shutDown();
            }
            return;
        }

        // for the rest of the commands
        // we will have to request AppSelector to be displayed
        if (c == removeOkCmd) {

            // suite to remove was set in confirmRemove()
            try {
                remove(removeMsi);
            } catch (Throwable t) {
                if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                    Logging.report(Logging.WARNING, LogChannels.LC_AMS,
                                   "Throwable in removeSuitee");
                }
            }
            return;

        } else if (c == cancelCmd) {

            // null out removeMsi in remove confirmation screen
            removeMsi = null;

        } else if (c == runYesCmd) {

            // user decided run the midlet suite after installation
            AMSMidletCustomItem mciToRun = getLastInstalledMidletItem();
            if (mciToRun != null) {
                display.setCurrentItem(mciToRun);
                launchMidlet(mciToRun.msi);
                return;
            }

        } else if (c == runNoCmd) {

            /*
             * user decided not to run the newly installed midlet suite
             *
             * if a MIDlet was just installed
             * displayLastInstalledMidlet() will return true and
             * make "this" visible with
             * the right MIDlet icon hilighted.
             */
            if (displayLastInstalledMidlet()) {
                // Last installed midlet was set as the current item
                return;
            }

        } else if (c != backCmd) {
            return;
        }

        // for back we just need to display AppSelector
        display.setCurrent(this);
    }

		/**
		* Respond to a command issued on an Item in AppSelector
		*
		* @param c command activated by the user
		* @param item the Item the command was on.
		*/
		public void commandAction(Command c, Item item) {
			// Folder-specific commands
			if (c == AMSFolderCustomItem.openFolderCmd) {
				((AMSFolderCustomItem)item).setOpen();
				return; }
			if (c == AMSFolderCustomItem.closeFolderCmd) {
				((AMSFolderCustomItem)item).setClosed();
				return; }

			// Midlet-specific commands
			if (!(item instanceof AMSMidletCustomItem)) {
				// Rest of this is legacy code--expects MidletCustomItem
				return; }
			RunningMIDletSuiteInfo msi = ((AMSMidletCustomItem)item).msi;
			if (msi == null) {
				return; }
		
			if (c == launchInstallCmd) {
				manager.installSuite();
				return; }
			if (c == launchCaManagerCmd) {
				manager.launchCaManager();
				return; }
			if (c == AMSMidletCustomItem_WifiManager.launchWifiSetupCmd) {
				manager.launchWifiManager();
				return; }
			if (c == AMSMidletCustomItem.launchCmd) {
				launchMidlet(msi);
				return; }
			if (c == infoCmd) {
				try {
					AppInfo appInfo = new AppInfo(msi.suiteId);
					appInfo.addCommand(backCmd);
					appInfo.setCommandListener(this);
					display.setCurrent(appInfo); }
				catch (Throwable t) {
					displayError.showErrorAlert(msi.displayName, t, null, null); }
				return; }
			if (c == removeCmd) {
				confirmRemove(msi);
				return; }
			if (c == updateCmd) {
				if (!isInstallerRunning()) {
					manager.updateSuite(msi);
					display.setCurrent(this); }
				else {
					String alertMessage = Resource.getString(
					ResourceConstants.AMS_MGR_INSTALLER_IS_RUNNING);
					displayError.showErrorAlert(null, null,
					Resource.getString(ResourceConstants.ERROR),
					alertMessage); }
				return; }
			if (c == appSettingsCmd) {
				try {
					AppSettings appSettings = new AppSettings(msi.suiteId, display,
					displayError, this);
					display.setCurrent(appSettings); }
				catch (Throwable t) {
					displayError.showErrorAlert(msi.displayName, t, null, null); }
				return; }
		if (c == AMSMidletCustomItem.fgCmd) {
			
			manager.moveToForeground(msi);
			display.setCurrent(this);
			
			} else if (c == AMSMidletCustomItem.endCmd) {
			manager.exitMidlet(msi);
			display.setCurrent(this);
			
			} else if (c == deviceSettingCmd) {
			try {
			DeviceSetting devSetting = new DeviceSetting(msi.suiteId, display, this);
			display.setCurrent(devSetting);
			} catch (Throwable t) {
			displayError.showErrorAlert(msi.displayName, t, null, null); } } }

    /**
     * Called when a new midlet was launched.
     *
     * @param midlet proxy of a newly added MIDlet
     */
    void notifyMidletStarted(MIDletProxy midlet) {
        String midletClassName = midlet.getClassName();

        if (midletClassName.equals(manager.getClass().getName())) {
            return; }

        if (midlet.getSuiteId() == MIDletSuite.INTERNAL_SUITE_ID &&
                !midletClassName.equals(DISCOVERY_APP) &&
                !midletClassName.equals(INSTALLER) &&
                !midletClassName.equals(CA_MANAGER)) {
            appManagerMidlet = midlet;
            return; }
	      AMSMidletCustomItem m = find(midlet);
	      if (m==null) {
					return; }
				m.updateDisplay(); }

    /**
     * Called when state of a running midlet was changed.
     *
     * @param midlet proxy of a newly added MIDlet
     */
		void notifyMidletStateChanged(MIDletProxy midlet) {
		AMSMidletCustomItem mci = rootFolder.find(midlet);
			if (mci.msi.proxy == midlet) {
				mci.updateDisplay(); } }

    /**
     * Called when a running midlet exited.
     *
     * @param midlet proxy of a newly added MIDlet
     */
    void notifyMidletExited(MIDletProxy midlet) {
        String midletClassName = midlet.getClassName();

        //Return to app selector, then set device to default
        GraphicalInstaller.setDeviceToRun(-1, null);

        if (midlet.getSuiteId() == MIDletSuite.INTERNAL_SUITE_ID &&
                !midletClassName.equals(DISCOVERY_APP) &&
                !midletClassName.equals(INSTALLER) &&
                !midletClassName.equals(CA_MANAGER)) {
            appManagerMidlet = null;
            display.setCurrent(this);
            return; }

				AMSMidletCustomItem ci = find(midlet);
		    if (ci==null) {
						return; }
				ci.updateDisplay();
	      midletSwitcher.remove(ci.msi);
	      ci.msi.proxy = null;

        if (removeMsi != null && removeMsi.equals(midlet)) {
        	// TODO: Fix. This won't work
          remove(removeMsi); }

        /*
         * When the Installer midlet quits
         * (it is removed from the running apps list)
         * this is a good time to see if any new MIDlet suites
         * were added
         * Also the CA manager could have disabled a MIDlet.
         */
        if (INSTALLER.equals(midletClassName)) {
        	// TODO: Add the new midlet -- this won't work
            updateContent(true);
            /*
            * After a MIDlet suite is successfully installed on the
            * device, ask the user whether or not to launch
            * a MIDlet from the suite.
            */
          AMSMidletCustomItem mci = getLastInstalledMidletItem();
          if (mci != null) {
              askUserIfLaunchMidlet();
              return; }
	          display.setCurrent(this);
            return; }
	          
        if (CA_MANAGER.equals(midletClassName)) {
            updateContent(true);
						ci.update(); }

        Displayable cur = display.getCurrent();
        if (cur != null && cur instanceof Alert) {
        	   //Please don't disappear too fast...
        	   try {
                Thread.sleep(3000);
        	   } catch (InterruptedException e) { } }
        
        display.setCurrent(this); }


    /**
     * Called when a midlet could not be launched.
     *
     * @param suiteId suite ID of the MIDlet
     * @param className class name of the MIDlet
     * @param errorCode error code
     * @param errorDetails error code details
     */
    void notifyMidletStartError(int suiteId, String className, int errorCode,
                                String errorDetails) {
        Alert a;
        String errorMsg;

        switch (errorCode) {
        case Constants.MIDLET_SUITE_NOT_FOUND:
            errorMsg = Resource.getString(
                ResourceConstants.AMS_MIDLETSUITELDR_MIDLETSUITE_NOTFOUND);
            break;

        case Constants.MIDLET_CLASS_NOT_FOUND:
            errorMsg = Resource.getString(
              ResourceConstants.AMS_MIDLETSUITELDR_CANT_LAUNCH_MISSING_CLASS);
            break;

        case Constants.MIDLET_INSTANTIATION_EXCEPTION:
            errorMsg = Resource.getString(
              ResourceConstants.AMS_MIDLETSUITELDR_CANT_LAUNCH_ILL_OPERATION);
            break;

        case Constants.MIDLET_ILLEGAL_ACCESS_EXCEPTION:
            errorMsg = Resource.getString(
              ResourceConstants.AMS_MIDLETSUITELDR_CANT_LAUNCH_ILL_OPERATION);
            break;

        case Constants.MIDLET_OUT_OF_MEM_ERROR:
            errorMsg = Resource.getString(
                ResourceConstants.AMS_MIDLETSUITELDR_QUIT_OUT_OF_MEMORY);
            break;

        case Constants.MIDLET_RESOURCE_LIMIT:
        case Constants.MIDLET_ISOLATE_RESOURCE_LIMIT:
            errorMsg = Resource.getString(
                ResourceConstants.AMS_MIDLETSUITELDR_RESOURCE_LIMIT_ERROR);
            break;

        case Constants.MIDLET_ISOLATE_CONSTRUCTOR_FAILED:
            errorMsg = Resource.getString(
                ResourceConstants.AMS_MIDLETSUITELDR_CANT_EXE_NEXT_MIDLET);
            break;

        case Constants.MIDLET_SUITE_DISABLED:
            errorMsg = Resource.getString(
                ResourceConstants.AMS_MIDLETSUITELDR_MIDLETSUITE_DISABLED);
            break;

        case Constants.MIDLET_INSTALLER_RUNNING:
            String[] values = new String[1];
            values[0] = className;
            errorMsg = Resource.getString(
                           ResourceConstants.AMS_MGR_UPDATE_IS_RUNNING,
                           values);
            break;

        default:
            errorMsg = Resource.getString(
                ResourceConstants.AMS_MIDLETSUITELDR_UNEXPECTEDLY_QUIT);
        }

        if (errorDetails != null) {
             errorMsg += "\n\n" + errorDetails;
        }

        displayError.showErrorAlert(null, null,
                                    Resource.getString
                                    (ResourceConstants.EXCEPTION),
                                    errorMsg);
    }

    // ------------------------------------------------------------------

    /**
     * Read in and create a MIDletInfo for newly added MIDlet suite and
     * check enabled state of currently added MIDlet suites.
     * ('Kay--not really--still working on the newly added part)     
     */
    private void updateContent(boolean loadIcon) {
    	systemFolder.updateDisplay();
    	rootFolder.updateDisplay();
			try {
				Thread.sleep(50); }
			catch (InterruptedException e) {} }
    
    /* private void oldUpdateContent(boolean loadIcon) {
			
        int[] suiteIds;
        RunningMIDletSuiteInfo msi = null;
        boolean newlyAdded;

        suiteIds = midletSuiteStorage.getListOfSuites();

        // Add the Installer as the first installed midlet
        if (size() > 0) {
            msi = ((AMSMidletCustomItem)get(0)).msi;
        }

        if (msi == null || msi.midletToRun == null ||
            !msi.midletToRun.equals(DISCOVERY_APP)) {

            msi = new RunningMIDletSuiteInfo(MIDletSuite.INTERNAL_SUITE_ID,
                DISCOVERY_APP,
                Resource.getString(ResourceConstants.INSTALL_APPLICATION),
                true) {
                    public boolean equals(MIDletProxy midlet) {
                        if (super.equals(midlet)) {
                            return true;
                        }

                        // there is one exception when 2 midlets belong to the
                        // same icon: Discovery app & Graphical installer.
                        // Graphical Installer can be launched by Discover app
                        // or when MIdlet update is needed.
                        // In such cases we simply need to set the proxy on
                        // corresponding icon (AMSMidletCustomItem).
                        // Note that when Discovery app exits and
                        // Installer is launched
                        // notifyMidletExited() will not find corresponding
                        // icon in the list of AMSMidletCustomItems.
                        // (that midlet exit will be ignored).
                        return (INSTALLER.equals(midlet.getClassName()));
                    }
                };

            append(msi);
        }

       // Wifi selector
        if (size() > 1) {
            msi = ((AMSMidletCustomItem)get(1)).msi;
        }
 
        if (msi == null || msi.midletToRun == null ||
            !msi.midletToRun.equals(WIFI_SELECTOR_APP)) {
        
            msi = new RunningMIDletSuiteInfo(MIDletSuite.INTERNAL_SUITE_ID,
                    WIFI_SELECTOR_APP,
                    "Network Setup",
                    true);
            append(msi);
        }

        if (caManagerIncluded) {
            // Add the CA manager as the second installed midlet
            if (size() > 2) {
                msi = ((AMSMidletCustomItem)get(2)).msi;
            }

            if (msi == null || msi.midletToRun == null ||
                !msi.midletToRun.equals(CA_MANAGER)) {
                msi = new RunningMIDletSuiteInfo(MIDletSuite.INTERNAL_SUITE_ID,
                  CA_MANAGER,
                  Resource.getString(ResourceConstants.CA_MANAGER_APP), true);
                append(msi);
            }
        }

        
        // Add the rest of the installed midlets
        for (int lowest, i = 0; i < suiteIds.length; i++) {

            lowest = i;

            for (int k = i + 1; k < suiteIds.length; k++) {
                if (suiteIds[k] < suiteIds[lowest]) {
                    lowest = k;
                }
            }

            try {
                MIDletSuiteInfo temp =
                    midletSuiteStorage.getMIDletSuiteInfo(suiteIds[lowest]);

                RunningMIDletSuiteInfo suiteInfo =
                    new RunningMIDletSuiteInfo(temp, midletSuiteStorage, loadIcon);

                newlyAdded = true;
                for (int k = 0; k < size(); k++) {
                    AMSMidletCustomItem mci = (AMSMidletCustomItem)get(k);

                    if (suiteIds[lowest] == mci.msi.suiteId) {
                        newlyAdded = false;
                        boolean isEnabled = suiteInfo.enabled;

                        if (mci.msi.enabled != isEnabled) {
                            mci.msi.enabled = isEnabled;

                            // MIDlet suite being enabled
                            if (isEnabled) {
                                mci.setDefaultCommand(launchCmd);
                            } else { // MIDlet suite is being disabled

                                if (mci.msi.proxy == null) { // Not running
                                    mci.removeCommand(launchCmd);
                                }

                                // running MIDlets will continue to run
                                // even when disabled
                            }
                        }

                        // Update all information about the suite;
                        // if the suite's icon was changed, reload it.
                        String oldIconName = mci.msi.iconName;
                        int oldNumberOfMidlets = mci.msi.numberOfMidlets;
                        MIDletProxy oldProxy = mci.msi.proxy;

                        midletSwitcher.update(mci.msi, suiteInfo);
                        mci.msi = suiteInfo;
                        mci.msi.proxy = oldProxy;

                        if ((suiteInfo.iconName != null &&
                                !suiteInfo.iconName.equals(oldIconName)) ||
                            (suiteInfo.iconName == null &&
                                suiteInfo.numberOfMidlets != oldNumberOfMidlets)
                        ) {
                            mci.msi.icon = null;
                            mci.msi.loadIcon(midletSuiteStorage);
                            mci.icon = mci.msi.icon;
                            try {
                               Thread.sleep(50);
                            } catch (InterruptedException e) {
                            }
                        }

                        break;
                    }
                }

                if (newlyAdded) {
                    append(suiteInfo);
                }

            } catch (Exception e) {
                // move on to the next suite
            }

            suiteIds[lowest] = suiteIds[i];
        }
    } */

    /**
     * Appends a AMSMidletCustomItem to the App Selector Screen
     *
     * @param suiteInfo the midlet suite info
     *                  of the recently started midlet
     * // TODO: Remove entirely, migrate additional cmd setting bits into various
     * // AMSCustomItem implementations.
     */
    private void append_trash(RunningMIDletSuiteInfo suiteInfo) {
        /*AMSMidletCustomItem ci = new AMSMidletCustomItem(suiteInfo, this);

        if (suiteInfo.midletToRun != null &&
            suiteInfo.midletToRun.equals(DISCOVERY_APP)) {
            // setDefaultCommand will add default command first
            ci.setDefaultCommand(launchInstallCmd);
        } else if (caManagerIncluded && suiteInfo.midletToRun != null &&
            suiteInfo.midletToRun.equals(CA_MANAGER)) {
            // setDefaultCommand will add default command first
            ci.setDefaultCommand(launchCaManagerCmd);
        } else if (suiteInfo.midletToRun != null &&
            suiteInfo.midletToRun.equals(WIFI_SELECTOR_APP)) {
            ci.setDefaultCommand(AMSMidletCustomItem_WifiManager.launchWifiSetupCmd);
        } else {
            ci.addCommand(infoCmd);
            ci.addCommand(removeCmd);
            ci.addCommand(updateCmd);
            ci.addCommand(appSettingsCmd);
            ci.addCommand(deviceSettingCmd);

            if (suiteInfo.enabled) {
                // setDefaultCommand will add default command first
                ci.setDefaultCommand(launchCmd);
            }
        }

        ci.setItemCommandListener(this);
        append(ci);*/
    }

    /**
     * Removes a midlet from the App Selector Screen
     *
     * @param suiteInfo the midlet suite info of a recently removed MIDlet
     */
    private void remove(RunningMIDletSuiteInfo suiteInfo) {
        RunningMIDletSuiteInfo msi;

        if (suiteInfo == null) {
            // Invalid parameter, should not happen.
            return;
        }

        // the last item in AppSelector is time
        for (int i = 0; i < size(); i++) {
            msi = (RunningMIDletSuiteInfo)((AMSMidletCustomItem)get(i)).msi;
            if (msi == suiteInfo) {
            	  PAPICleanUp.removeMissedTransaction(suiteInfo.suiteId);
	          if (msi.proxy != null) {
                   msi.proxy.destroyMidlet();
                }

                try {
                    midletSuiteStorage.remove(suiteInfo.suiteId);
                } catch (Throwable t) {
                    if (t instanceof MIDletSuiteLockedException) {
                        String[] val = new String[1];
                        val[0] = suiteInfo.displayName;
                        displayError.showErrorAlert(suiteInfo.displayName,
                            null,
                            Resource.getString(ResourceConstants.ERROR),
                            Resource.getString(
                                ResourceConstants.AMS_MGR_REMOVE_LOCKED_SUITE,
                                    val),
                            this);
                    } else {
                        displayError.showErrorAlert(suiteInfo.displayName,
                            t,
                            Resource.getString(ResourceConstants.ERROR),
                            null, this);
                    }

                    return;
                }

                try {
                    PushRegistryInternal.unregisterConnections(
                        suiteInfo.suiteId);
                } catch (Throwable t) {
                    // Intentionally ignored: suite has been removed already,
                    // we can't do anything meaningful at this point.
                }

                delete(i);
                removeMsi = null;
                break;
            }
        }

        display.setCurrent(this);
    }

    /**
     * Alert the user that an action was successful.
     * @param successMessage message to display to user
     */
    private void displaySuccessMessage(String successMessage) {
        Image icon;
        Alert successAlert;

        icon = GraphicalInstaller.getImageFromInternalStorage("_dukeok8");

        successAlert = new Alert(null, successMessage, icon, null);

        successAlert.setTimeout(GraphicalInstaller.ALERT_TIMEOUT);

        // We need to prevent "flashing" on fast development platforms.
        while (System.currentTimeMillis() - lastDisplayChange <
               GraphicalInstaller.ALERT_TIMEOUT);

        display.setCurrent(successAlert, this);
        lastDisplayChange = System.currentTimeMillis();
    }

    /**
     * Confirm the removal of a suite.
     *
     * @param suiteInfo information for suite to remove
     */
    private void confirmRemove(RunningMIDletSuiteInfo suiteInfo) {
        Form confirmForm;
        StringBuffer temp = new StringBuffer(40);
        Item item;
        String extraConfirmMsg;
        String[] values = new String[1];
        MIDletSuiteImpl midletSuite = null;

        try {
            midletSuite = midletSuiteStorage.getMIDletSuite(suiteInfo.suiteId,
                                                            false);
            confirmForm = new Form(null);

            confirmForm.setTitle(Resource.getString
                                 (ResourceConstants.AMS_CONFIRMATION));

            if (suiteInfo.hasSingleMidlet()) {
                values[0] = suiteInfo.displayName;
            } else {
                values[0] =
                    midletSuite.getProperty(MIDletSuiteImpl.SUITE_NAME_PROP);
            }

            item = new StringItem(null, Resource.getString(
                       ResourceConstants.AMS_MGR_REMOVE_QUE,
                       values));
            item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
            confirmForm.append(item);

            extraConfirmMsg =
                PAPICleanUp.checkMissedTransactions(midletSuite.getID());
            if (extraConfirmMsg != null) {
                temp.setLength(0);
                temp.append(" \n");
                temp.append(extraConfirmMsg);
                item = new StringItem(null, temp.toString());
                item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
                confirmForm.append(item);
            }

            extraConfirmMsg = midletSuite.getProperty("MIDlet-Delete-Confirm");
            if (extraConfirmMsg != null) {
                temp.setLength(0);
                temp.append(" \n");
                temp.append(extraConfirmMsg);
                item = new StringItem(null, temp.toString());
                item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
                confirmForm.append(item);
            }

            if (!suiteInfo.hasSingleMidlet()) {
                temp.setLength(0);
                temp.append(Resource.getString
                            (ResourceConstants.AMS_MGR_SUITE_CONTAINS));
                temp.append(": ");
                item = new StringItem(temp.toString(), "");
                item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
                confirmForm.append(item);
                appendMIDletsToForm(midletSuite, confirmForm);
            }

            String[] recordStores =
                midletSuiteStorage.listRecordStores(suiteInfo.suiteId);
            if (recordStores != null) {
                temp.setLength(0);
                temp.append(Resource.getString
                            (ResourceConstants.AMS_MGR_SUITE_RECORD_STORES));
                temp.append(": ");
                item = new StringItem(temp.toString(), "");
                item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
                confirmForm.append(item);
                appendRecordStoresToForm(recordStores, confirmForm);
            }

            temp.setLength(0);
            temp.append(" \n");
            temp.append(Resource.getString
                        (ResourceConstants.AMS_MGR_REM_REINSTALL, values));
            item = new StringItem("", temp.toString());
            confirmForm.append(item);
        } catch (Throwable t) {
            displayError.showErrorAlert(suiteInfo.displayName, t,
                                   Resource.getString
                                   (ResourceConstants.AMS_CANT_ACCESS),
                                   null);
            return;
        } finally {
            if (midletSuite != null) {
                midletSuite.close();
            }
        }

        confirmForm.addCommand(cancelCmd);
        confirmForm.addCommand(removeOkCmd);
        confirmForm.setCommandListener(this);
        removeMsi = suiteInfo;
        display.setCurrent(confirmForm);
    }

    /**
     * Appends a names of all the MIDlets in a suite to a Form, one per line.
     *
     * @param midletSuite information of a suite of MIDlets
     * @param form form to append to
     */
    private void appendMIDletsToForm(MIDletSuiteImpl midletSuite, Form form) {
        int numberOfMidlets;
        MIDletInfo midletInfo;
        StringItem item;

        numberOfMidlets = midletSuite.getNumberOfMIDlets();
        for (int i = 1; i <= numberOfMidlets; i++) {
            midletInfo = new MIDletInfo(
                             midletSuite.getProperty("MIDlet-" + i));

            item = new StringItem(null, midletInfo.name);
            item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
            form.append(item);
        }
    }

    /**
     * Appends names of the record stores owned by the midlet suite
     * to a Form, one per line.
     *
     * @param recordStores list of the record store names
     * @param form form to append to
     */
    private void appendRecordStoresToForm(String[] recordStores, Form form) {
        StringItem item;

        for (int i = 0; i < recordStores.length; i++) {
            item = new StringItem(null, recordStores[i]);
            item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
            form.append(item);
        }
    }

    /**
     * Open the settings database and retrieve an id of the midlet suite
     * that was installed last.
     *
     * @return ID of the midlet suite that was installed last or
     * MIDletSuite.UNUSED_SUITE_ID.
     */
    private int getLastInstalledMIDlet() {
        ByteArrayInputStream bas;
        DataInputStream dis;
        byte[] data;
        RecordStore settings = null;
        int ret = MIDletSuite.UNUSED_SUITE_ID;

        try {
            settings = RecordStore.
                       openRecordStore(GraphicalInstaller.SETTINGS_STORE,
                                       false);

            /** we should be guaranteed that this is always the case! */
            if (settings.getNumRecords() > 0) {

                data = settings.getRecord(
                           GraphicalInstaller.SELECTED_MIDLET_RECORD_ID);

                if (data != null) {
                    bas = new ByteArrayInputStream(data);
                    dis = new DataInputStream(bas);
                    ret = dis.readInt();
                }
            }

        } catch (RecordStoreException e) {
            // ignore
        } catch (IOException e) {
            // ignore
        } finally {
            if (settings != null) {
                try {
                    settings.closeRecordStore();
                } catch (RecordStoreException e) {
                    // ignore
                }
            }
        }

        return ret;
    }


    /**
     * Initialize the settings database if it doesn't exist. This may create
     * two entries. The first will be for the download url, the second will
     * be for storing the storagename of the currently selected midlet
     * <p>
     * Method requires com.sun.midp.ams permission.
     */
    public static void initSettings() {
        AccessController.checkPermission(Permissions.AMS_PERMISSION_NAME);

        try {
            RecordStore settings = RecordStore.
                                   openRecordStore(SETTINGS_STORE, true);

            try {
                while (settings.getNumRecords() < RECORDS_NEEDED) {
                    settings.addRecord(null, 0, 0);
                }
            } finally {
                settings.closeRecordStore();
            }

        } catch (Exception e) {
            if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                Logging.report(Logging.WARNING, LogChannels.LC_AMS,
                               "initSettings  throw an Exception");
            }
        }
    }

    private static Exception saveLastPlayedMIDlet(int curMidlet) {
        Exception ret = null;

        AccessController.checkPermission(Permissions.AMS_PERMISSION_NAME);

        try {
            String temp;
            ByteArrayOutputStream bas;
            DataOutputStream dos;
            byte[] data;
            RecordStore settings;

            bas = new ByteArrayOutputStream();
            dos = new DataOutputStream(bas);
            settings = RecordStore.openRecordStore(SETTINGS_STORE, false);

            // Save the current midlet even if its id is
            // MIDletSuite.UNUSED_SUITE_ID. Otherwise in SVM mode
            // the last installed midlet will be always highlighted
            // because its id is recorded in this RMS record.
            bas.reset();

            dos.writeInt(curMidlet);
            data = bas.toByteArray();
            settings.setRecord(LASTPLAY_MIDLET_RECORD_ID,
                               data, 0, data.length);

            settings.closeRecordStore();
            dos.close();
        } catch (Exception e) {
            ret = e;
        }

        return ret;
    }

    private static int getLastPlayedMIDlet() {
        ByteArrayInputStream bas;
        DataInputStream dis;
        byte[] data;
        RecordStore settings = null;
        int ret = MIDletSuite.UNUSED_SUITE_ID;

        try {
            settings = RecordStore.
                       openRecordStore(SETTINGS_STORE,
                                       false);

            /** we should be guaranteed that this is always the case! */
            if (settings.getNumRecords() > 0) {

                data = settings.getRecord(LASTPLAY_MIDLET_RECORD_ID);

                if (data != null) {
                    bas = new ByteArrayInputStream(data);
                    dis = new DataInputStream(bas);
                    ret = dis.readInt();
                }
            }

        } catch (RecordStoreException e) {
            // ignore
        } catch (IOException e) {
            // ignore
        } finally {
            if (settings != null) {
                try {
                    settings.closeRecordStore();
                } catch (RecordStoreException e) {
                    // ignore
                }
            }
        }

        return ret;
    }

    /**
     * Finds a AMSMidletCustomItem corresponding to the last installed
     * midlet suite.
     * @return the midlet custom item if it was found, null otherwise
     */
    private AMSMidletCustomItem getLastInstalledMidletItem() {
        int installedMidlet = getLastInstalledMIDlet();

        if (installedMidlet != MIDletSuite.UNUSED_SUITE_ID &&
                installedMidlet != MIDletSuite.INTERNAL_SUITE_ID) {
              return rootFolder.find(installedMidlet);
        }
        return null;
    }

    /**
     * Displayas AppManagerUI with a recently installed midlet hilighted.
     * @return true if display.setCurrentItem() was called,
     *              false - otherwise
     */
    private boolean displayLastInstalledMidlet() {
        AMSMidletCustomItem ci = getLastInstalledMidletItem();

        if (ci != null) {
            display.setCurrentItem(ci);
            return true;
        }

        return false;
    }

    /**
     * Launches the midlet suite described by the given MIDletSuiteInfo.
     * @param msi a structure with information about the midlet suite
     * that must be launched
     */
    private void launchMidlet(RunningMIDletSuiteInfo msi) {
        saveLastPlayedMIDlet(msi.suiteId);
        
        if (msi.hasSingleMidlet()) {
            String title = new String("Launching MIDlet...");
            StringItem msg = new StringItem(msi.displayName, "");

            Form f = new Form(title);
            msg.setLayout(Item.LAYOUT_CENTER);
            f.append(msg);
        
            display.setCurrent(f);
            
            manager.launchSuite(msi, msi.midletToRun);
            //display.setCurrent(this);
        } else {
            try {
                new MIDletSelector(msi, display, this, manager);
            } catch (Throwable t) {
                displayError.showErrorAlert(msi.displayName, t,
                                            null, null); } } }

    /**
     * Prompts the user to specify whether to launch a midlet from
     * the midlet suite that was just installed.
     */
    private void askUserIfLaunchMidlet() {
        // user decided run the midlet suite after installation
        AMSMidletCustomItem mciToRun = getLastInstalledMidletItem();
        if (mciToRun != null) {
            display.setCurrentItem(mciToRun);
            launchMidlet(mciToRun.msi);            
        }
    }

    /**
     * Checks if the installer is currently running.
     *
     * @return true if the installer or discovery application is running,
     *         false otherwise
     */
    private boolean isInstallerRunning() {
    	return systemFolder.isInstallerRunning(); }
    
    // Handy method for folders doing their thing--get the 
    // index of an object
    int getIndexOf(Item o) {
    	int c = size();
    	for(int i=0; i<c; i++) {
				Item t = get(i);
				if (o==t) {
					return i; } }
			return -1; }
			
		// Slightly safer insert/add method--makes the inserts from
		// the folder system a bit cleaner.
		void insertAt(int p, Item i) {
			if (p<0) {
				return; }
			if (p>=size()) {
				append(i);
				return; }
			insert(p, i); }

    /**
     * Called by Manager when destroyApp happens to clean up data.
     * Timer that shedules scrolling text repainting should be
     * canceled when AMS MIDlet is about to be destroyed to avoid
     * generation of repaint events.
     */
    void cleanUp() {
       AMSCustomItem.stopTimer(); }

}
