package com.pspkvm.system;

import javax.microedition.lcdui.*;

// Settings form for the Virtual Keyboard setup

public class KeymapSettingsForm extends VMSettingsForm {

	// Statics
	static final String TITLE="Virtual keyboard setup";

	// Default keymap 
	static final String DEFKMLABEL="Default keymap";
	static final String[] DEFKMTYPES = {
		"Eastern (O for confirm) (PSPKVM default)",
		"Western (X for confirm)" };
	static final String[] DEFKMTYPES_V = {
		"eastern", "western"};
	static final String DEFKMTYPES_PNAME = "com.pspkvm.default_keymap";

	// Form controls
	ChoiceGroup defaultKeymap;
	// Set all the controls from the config
	void setFromConfig() {
		setFromConfig(defaultKeymap, DEFKMTYPES_V, DEFKMTYPES_PNAME); }
		
	// Write the controls to the config
	public void writeToConfig() {
		writeToConfig(defaultKeymap, DEFKMTYPES_V, DEFKMTYPES_PNAME); }

	// Constructor
	public KeymapSettingsForm() {
		super(TITLE);
		defaultKeymap = new ChoiceGroup(DEFKMLABEL, Choice.EXCLUSIVE, DEFKMTYPES, null);
		defaultKeymap.setLayout(Item.LAYOUT_2);
		append(defaultKeymap);
		setFromConfig(); }
		
}
