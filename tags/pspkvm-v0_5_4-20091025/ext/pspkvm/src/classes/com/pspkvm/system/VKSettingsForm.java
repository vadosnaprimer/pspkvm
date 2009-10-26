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
		"AWF/Default", "AWF/Chinese", "AWF/Float", "Danzeff", "QWERTY",
		"Semichordal", "Sony OSK", };
	static final String[] VKTYPES_V = {
		"awf", "awf_chinese", "awf_f", "danzeff", "qwerty", "semichordal", "osk" };
	// Ones we need to know about for additional settings
	static final int VKTYPE_SEMICHORDAL = 5;
	static final int VKTYPE_AWFFLOAT = 2;
	static final String VKTYPES_PNAME = "com.pspkvm.inputmethod";

	// Semichordal default map
	static final String SCMAPLABEL="Semichordal default map";
	static final String[] SCMAPS = {"Cyrillic", "Greek", "Roman"};
	static final String[] SCMAPS_V = {"cyrillic", "greek", "roman"};
	static final String SCMAPS_PNAME = "com.pspkvm.virtual_keyboard.default_keymap";

	// AWF/Float default map
	static final String AWFMAPLABEL="AWF/Float default map";
	static final String[] AWFMAPS = {"Roman", "Pinyin", "Stroke"};
	static final String[] AWFMAPS_V = {"roman", "pinyin", "stroke"};
	static final String AWFMAPS_PNAME = "com.pspkvm.virtual_keyboard.awf_smap";

	// General options label
	static final String OPTIONSLABEL = "Options";
	static final String[] OPTIONSNAMES={
		"Autoopen", "Analog cursor" };
	static final String[] OPTIONS_KEYS = {
		"com.pspkvm.virtualkeyboard.autoopen",
		"com.pspkvm.virtualkeyboard.direction" };
		
	// Form controls
	ChoiceGroup boardType, defaultSCMap, defaultAWFMap, optionsGroup;
	// Set all the controls from the config
	void setFromConfig() {
		setFromConfig(boardType, VKTYPES_V, VKTYPES_PNAME);
		setFromConfig(defaultSCMap, SCMAPS_V, SCMAPS_PNAME);
		setFromConfig(defaultAWFMap, AWFMAPS_V, AWFMAPS_PNAME);
		setMultipleFromConfig(optionsGroup, OPTIONS_KEYS); }
		
	// Write the controls to the config
	public void writeToConfig() {
		writeToConfig(boardType, VKTYPES_V, VKTYPES_PNAME);
		writeToConfig(defaultSCMap, SCMAPS_V, SCMAPS_PNAME);
		writeToConfig(defaultAWFMap, AWFMAPS_V, AWFMAPS_PNAME);
		writeMultipleToConfig(optionsGroup, OPTIONS_KEYS); }

	// Constructor
	public VKSettingsForm() {
		super(TITLE);
		boardType = new ChoiceGroup(TYPELABEL, Choice.EXCLUSIVE, VKTYPES, null);
		boardType.setLayout(Item.LAYOUT_2);
		append(boardType);
		defaultSCMap = new ChoiceGroup(SCMAPLABEL, Choice.EXCLUSIVE, SCMAPS, null);
		defaultSCMap.setLayout(Item.LAYOUT_2);
		defaultAWFMap = new ChoiceGroup(AWFMAPLABEL, Choice.EXCLUSIVE, AWFMAPS, null);
		defaultAWFMap.setLayout(Item.LAYOUT_2);
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
				delete(defaultAWFMap);
				sinsert(defaultSCMap, 1);
				return;
			case VKTYPE_AWFFLOAT:
				delete(defaultSCMap);
				sinsert(defaultAWFMap, 1);
				return;
			default:
				delete(defaultAWFMap);
				delete(defaultSCMap); } }

	public void itemStateChanged(Item item) {
		if (item == boardType) {
			updateDisplay(); } }
}
