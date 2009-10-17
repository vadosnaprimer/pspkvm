package com.pspkvm.system;

/*
	Midlet wrapping live config stuff
	AJ Milne for the PSPKVM project.
	
	License is GPL. Free software. Modify, if you wish, but please post your very cool
	enhancements back somewhere we can find 'em; thanx.
*/

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import com.sun.midp.installer.GraphicalInstaller;

public class VMConfigMidlet extends MIDlet implements CommandListener {

	// Form commands
	static final Command settingsFormCancelCmd = new Command("Cancel", Command.CANCEL, 1);
	static final Command settingsFormConfirmCmd = new Command("OK", Command.OK, 1);
	// Launch config form command
	static final Command launchFormCmd = new Command("Configure", Command.SCREEN, 1);
	static final Command exitCmd = new Command("Exit", Command.EXIT, 1);
	
	// The selector list
	List selector;

	void launchSettingsForm(VMSettingsForm f) {
		f.addCommand(settingsFormCancelCmd);
		f.addCommand(settingsFormConfirmCmd);
		f.setCommandListener(this);
		Display.getDisplay(this).setCurrent(f); }
		
	void setupSelector() {
		selector = new List("VM configuration", Choice.IMPLICIT);
		selector.append("Virtual keyboards",
			GraphicalInstaller.getImageFromInternalStorage("confkb"));
		selector.append("Font subsystem",
			GraphicalInstaller.getImageFromInternalStorage("conffonts"));
		selector.append("Keymap settings",
			GraphicalInstaller.getImageFromInternalStorage("keymaps"));
		selector.addCommand(launchFormCmd);
		selector.addCommand(exitCmd);
		selector.setCommandListener(this); }

	public VMConfigMidlet() {
		setupSelector(); }
		
	protected void startApp() {
		Display.getDisplay(this).setCurrent(selector);
		try{ Thread.currentThread().sleep(2000); }
		catch(Exception e){} }
	
	protected void pauseApp() {}

	protected void destroyApp(boolean bool) {}
			
	public void commandAction(Command cmd, Displayable disp) {
		if (cmd == exitCmd) {
			destroyApp(false);
			notifyDestroyed();
			return; }
		if (cmd == settingsFormCancelCmd) {
			Display.getDisplay(this).setCurrent(selector);
			return; }
		if (cmd == settingsFormConfirmCmd) {
			VMSettingsForm f = (VMSettingsForm)disp;
			f.writeToConfig();
			Display.getDisplay(this).setCurrent(selector);
			return; }
		if ((cmd == launchFormCmd) || (cmd == List.SELECT_COMMAND)) {
			launchConfigForm();
			return; } }
			
	void launchConfigForm() {
		int i = selector.getSelectedIndex();
		switch(i) {
			case 0: 
				launchSettingsForm(new VKSettingsForm());
				return;
			case 1:
				launchSettingsForm(new FontSettingsForm());
				return; 
			case 2:
				launchSettingsForm(new KeymapSettingsForm());
				return; 
			default:
				return; } }

}
