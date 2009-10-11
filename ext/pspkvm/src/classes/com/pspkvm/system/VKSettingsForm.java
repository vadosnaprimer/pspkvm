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
		"AWF/Default", "AWF/Chinese", "Danzeff", "QWERTY", "Semichordal", "Sony OSK"};
	static final String[] VKTYPES_V = {
		"awf", "awf_chinese", "danzeff", "qwerty", "semichordal", "osk"};
	static final int VKTYPE_AWF = 0;
	static final int VKTYPE_AWFCHINESE = 1;
	static final int VKTYPE_DANZEFF = 2;
	static final int VKTYPE_QWERTY = 3;
	static final int VKTYPE_SEMICHORDAL = 4;
	static final int VKTYPE_OSK = 5;
	static final int VKTYPE_COUNT = 6;
	static final String VKTYPES_PNAME = "com.pspkvm.inputmethod";

	// Semichordal default map
	static final String SCMAPLABEL="Semichordal default map";
	static final String[] SCMAPS = {"Cyrillic", "Greek", "Roman"};
	static final String[] SCMAPS_V = {"cyrillic", "greek", "roman"};
	static final String SCMAPS_PNAME = "com.pspkvm.virtual_keyboard.default_keymap";

	// General options label
	static final String OPTIONSLABEL = "Options";
	static final String[] OPTIONSNAMES={
		"Autoopen", "Analog cursor" };
	static final String[] OPTIONS_KEYS = {
		"com.pspkvm.virtualkeyboard.autoopen",
		"com.pspkvm.virtualkeyboard.direction" };
		
	// Form controls
	ChoiceGroup boardType, defaultSCMap, optionsGroup;
	// Set all the controls from the config
	void setFromConfig() {
		setFromConfig(boardType, VKTYPES_V, VKTYPES_PNAME);
		setFromConfig(defaultSCMap, SCMAPS_V, SCMAPS_PNAME);
		setMultipleFromConfig(optionsGroup, OPTIONS_KEYS); }
		
	// Write the controls to the config
	public void writeToConfig() {
		writeToConfig(boardType, VKTYPES_V, VKTYPES_PNAME);
		writeToConfig(defaultSCMap, SCMAPS_V, SCMAPS_PNAME);
		writeMultipleToConfig(optionsGroup, OPTIONS_KEYS); }

	// Constructor
	public VKSettingsForm() {
		super(TITLE);
		boardType = new ChoiceGroup(TYPELABEL, Choice.EXCLUSIVE, VKTYPES, null);
		boardType.setLayout(Item.LAYOUT_2);
		append(boardType);
		defaultSCMap = new ChoiceGroup(SCMAPLABEL, Choice.EXCLUSIVE, SCMAPS, null);
		defaultSCMap.setLayout(Item.LAYOUT_2);
		optionsGroup = new ChoiceGroup(OPTIONSLABEL, Choice.MULTIPLE, OPTIONSNAMES, null);
		optionsGroup.setLayout(Item.LAYOUT_2|Item.LAYOUT_NEWLINE_BEFORE);
		append(optionsGroup);
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
