package com.pspkvm.system;

import javax.microedition.lcdui.*;

// Settings form for the Virtual Keyboard setup

public class VKSettingsForm extends VMSettingsForm
	implements ItemStateListener {

	// Statics
	static final String TITLE="Virtual keyboard setup";

	// Virtual keyboard type
	static final String TYPELABEL="Type";
	static final String[] VKTYPES = {
		"AWF/Default", "AWF/Chinese", "QWERTY", "Sony OSK", "Semichordal"};
	static final String[] VKTYPES_V = {
		"awf", "awf_chinese", "qwerty", "osk", "semichordal"};
	static final int VKTYPE_AWF = 0;
	static final int VKTYPE_AWFCHINESE = 1;
	static final int VKTYPE_QWERTY = 2;
	static final int VKTYPE_OSK = 3;
	static final int VKTYPE_SEMICHORDAL = 4;
	static final int VKTYPE_COUNT = 5;
	static final String VKTYPES_PNAME = "com.pspkvm.inputmethod";

	// Semichordal default map
	static final String SCMAPLABEL="Semichordal default map";
	static final String[] SCMAPS = {"Cyrillic", "Greek", "Roman"};
	static final String[] SCMAPS_V = {"cyrillic", "greek", "roman"};
	static final String SCMAPS_PNAME = "com.pspkvm.virtual_keyboard.default_keymap";

	// Autoopen
	static final String AOPENLABEL="Autoopen";
	static final String[] ONOFFCHOICES={"On", "Off"};
	static final String[] ONOFFCHOICES_V={"on", "off"};
	static final String AOPEN_PNAME = "com.pspkvm.virtualkeyboard.autoopen";
	
	// Analog direction control
	static final String ANADIRECTIONLABEL="Analog cursor";
	// Uses onoff choices from above
	static final String ANADIRECTION_PNAME = "com.pspkvm.virtualkeyboard.direction";

	// Form controls
	ChoiceGroup boardType, defaultSCMap, autoOpen, anaDirection;

	// Set all the controls from the config
	void setFromConfig() {
		setFromConfig(boardType, VKTYPES_V, VKTYPES_PNAME);
		setFromConfig(defaultSCMap, SCMAPS_V, SCMAPS_PNAME);
		setFromConfig(autoOpen, ONOFFCHOICES_V, AOPEN_PNAME);
		setFromConfig(anaDirection, ONOFFCHOICES_V, ANADIRECTION_PNAME); }
		
	// Write the controls to the config
	public void writeToConfig() {
		writeToConfig(boardType, VKTYPES_V, VKTYPES_PNAME);
		writeToConfig(defaultSCMap, SCMAPS_V, SCMAPS_PNAME);
		writeToConfig(autoOpen, ONOFFCHOICES_V, AOPEN_PNAME);
		writeToConfig(anaDirection, ONOFFCHOICES_V, ANADIRECTION_PNAME); }

	// Constructor
	public VKSettingsForm() {
		super(TITLE);
		boardType = new ChoiceGroup(TYPELABEL, Choice.POPUP, VKTYPES, null);
		append(boardType);
		defaultSCMap = new ChoiceGroup(SCMAPLABEL, Choice.POPUP, SCMAPS, null);
		autoOpen = new ChoiceGroup(AOPENLABEL, Choice.POPUP, ONOFFCHOICES, null);
		append(autoOpen);
		anaDirection = new ChoiceGroup(ANADIRECTIONLABEL, Choice.POPUP, ONOFFCHOICES, null);
		append(anaDirection);
		setItemStateListener(this);
		setFromConfig();
		updateDisplay(); }
		
	// Synchs the visible fields according to which keyboard is now default
	void updateDisplay() {
		int i = boardType.getSelectedIndex();
		switch (i) {
			case VKTYPE_SEMICHORDAL:
				sinsert(defaultSCMap, 1);
				return;
			default:
				delete(defaultSCMap); } }

	public void itemStateChanged(Item item) {
		if (item == boardType) {
			updateDisplay(); } }

}

