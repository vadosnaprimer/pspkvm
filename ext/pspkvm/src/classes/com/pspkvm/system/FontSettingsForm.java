package com.pspkvm.system;

import javax.microedition.lcdui.*;

// Settings form for the Virtual Keyboard setup

public class FontSettingsForm extends VMSettingsForm {
	
	// Statics
	static final String TITLE="Font subsystem setup";

	// Use internal font
	static final String USEINTERNAL_LABEL="Use internal font";
	static final String USEINTERNAL_PNAME = "com.pspkvm.font.internal";
	// Sizes
	static final String LGFSIZE_LABEL="Lg font size";
	static final String LGFSIZE_PNAME = "com.pspkvm.fontsize.large";
	static final String MDFSIZE_LABEL="Md font size";
	static final String MDFSIZE_PNAME = "com.pspkvm.fontsize.med";
	static final String SMFSIZE_LABEL="Sm font size";
	static final String SMFSIZE_PNAME = "com.pspkvm.fontsize.small";
	static final String[] FSIZES = {
		"10","11","12","13","14","15","16","17","18","19",
		"20","21","22","24","26","28","30" };
	// Warning
	static final String RESTART_MESSAGE = "Font subsystem configuration changes will not take effect until after you restart PSPKVM.";

	// Form controls
	ChoiceGroup useInternal, fsSmall, fsMed, fsLarge;

	// Set all the controls from the config
	void setFromConfig() {
		setFromConfig(useInternal, ONOFFCHOICES_V, USEINTERNAL_PNAME);
		setFromConfig(fsSmall, FSIZES, SMFSIZE_PNAME);
		setFromConfig(fsMed, FSIZES, MDFSIZE_PNAME);
		setFromConfig(fsLarge, FSIZES, LGFSIZE_PNAME); }
		
	// Write the controls to the config
	public void writeToConfig() {
		writeToConfig(useInternal, ONOFFCHOICES_V, USEINTERNAL_PNAME);
		writeToConfig(fsSmall, FSIZES, SMFSIZE_PNAME);
		writeToConfig(fsMed, FSIZES, MDFSIZE_PNAME);
		writeToConfig(fsLarge, FSIZES, LGFSIZE_PNAME); }

	// Constructor
	public FontSettingsForm() {
		super(TITLE);
		useInternal =
			new ChoiceGroup(USEINTERNAL_LABEL, Choice.EXCLUSIVE, ONOFFCHOICES, null);
		append(useInternal);
		fsLarge =
			new ChoiceGroup(LGFSIZE_LABEL, Choice.POPUP, FSIZES, null);
		fsLarge.setLayout(Item.LAYOUT_2);
		append(fsLarge);
		fsMed =
			new ChoiceGroup(MDFSIZE_LABEL, Choice.POPUP, FSIZES, null);
		fsMed.setLayout(Item.LAYOUT_2);
		append(fsMed);
		fsSmall =
			new ChoiceGroup(SMFSIZE_LABEL, Choice.POPUP, FSIZES, null);
		fsSmall.setLayout(Item.LAYOUT_2|Item.LAYOUT_NEWLINE_AFTER);
		append(fsSmall);
		append(RESTART_MESSAGE);
		setFromConfig(); }
}
