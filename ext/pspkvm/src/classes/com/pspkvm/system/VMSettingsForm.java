package com.pspkvm.system;

import javax.microedition.lcdui.*;

// Parent class for the settings forms--with some convenient
// methods we can generally use

public abstract class VMSettingsForm extends Form {

	static final String[] ONOFFCHOICES={"On", "Off"};
	static final String[] ONOFFCHOICES_V={"on", "off"};

	VMSettingsForm(String t) {
		super(t); }

	// Helper for update display
	void sinsert(Item i, int idx) {
		if (find(i)) {
			return; }
		insert(idx, i); }

	// Helper for update display
	boolean find(Item i) {
		int idx=0;
		while(idx<size()) {
			if (get(idx)==i) {
				return true; }
			idx++; }
		return false; }

	// Helper for update display
	void delete(Item i) {
		int idx=0;
		while(idx<size()) {
			if (get(idx)==i) {
				delete(idx);
				return; }
			idx++; } }
	
	// Nice utility method--just finds the index of a String in an
	// array--good for translating the config arrays to controls indices
	static int find(String c, String[] s) {
		for(int i=0; i<s.length; i++) {
			if (s[i]==null) {
				continue; }
			if (s[i].equals(c)) {
				return i; } }
		return -1; }
		
	// 'Nother nice utility--set a Choice control to an index from
	// the config string using array r as a reference
	void setFromVal(Choice c, String[] r, String s) {
		int i = find(s, r);
		if (i==-1) {
			return; }
		if (i>=c.size()) {
			return; }
		c.setSelectedIndex(i, true); }
		
	// Same as above, but get the value from config first
	void setFromConfig(Choice c, String[] r, String k) {
		setFromVal(c, r, VMSettings.get(k)); }
		
	// Reverse of above--write config string from control
	void writeToConfig(Choice s, String[] r, String k) {
		VMSettings.set(k, r[s.getSelectedIndex()]); }

	// Set a textfield from config
	void setFromConfig(TextField t, String k) {
		t.setString(VMSettings.get(k)); }
		
	// Set a textfield from config integer, with default
	void setFromConfig(TextField t, String k, int d) {
		t.setString(Integer.toString(VMSettings.getInt(k, d))); }

	// Write a textfield to config
	void writeToConfig(TextField t, String k) {
		VMSettings.set(k, t.getString()); }
		
	// Write an on/off value to config
	void writeToConfig(boolean b, String k) {
		VMSettings.set(k, b ? "on":"off"); }
		
	// Get a value from config
	boolean onOffFromConfig(String k) {
		String v = VMSettings.get(k);
		if (v==null) {
			return false; }
		return (v.equals("on")); }
		
	// Set a multiple list from a list of config values w/ on/off each
	void writeMultipleToConfig(ChoiceGroup c, String[] k) {
		for(int i=0; i<k.length; i++) {
			writeToConfig(c.isSelected(i), k[i]); } }
			
	// Write a multiple list to a list of config values w/ on/off each
	void setMultipleFromConfig(ChoiceGroup c, String[] k) {
		for(int i=0; i<k.length; i++) {
			c.setSelectedIndex(i, onOffFromConfig(k[i])); } } 
		
	// Write a textfield to config, interpreting as integer
	// Provide min, max, and default for fixes at write
	void writeToConfig(TextField t, String k, int min, int d, int max) {
		try {
			int i = Integer.parseInt(t.getString());
			if (i < min) {
				i = min; }
			if (i > max) {
				i = max; }
			VMSettings.setInt(k, i); }
		catch(Exception e) {
			VMSettings.setInt(k, d); } }
			
	// Validate a numeric textfield
	void validate(TextField t, int min, int d, int max) {
		try {
			int i = Integer.parseInt(t.getString());
			if (i < min) {
				i = min; }
			if (i > max) {
				i = max; }
			t.setString(Integer.toString(i)); }
		catch(Exception e) {
			t.setString(Integer.toString(d)); } }
			
	// Always implement this--needed to write the content to config
	// after changes entered
	public abstract void writeToConfig();
}
