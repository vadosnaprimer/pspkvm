package com.pspkvm.system;

import javax.microedition.lcdui.*;

// Parent class for the settings forms--with some convenient
// methods we can generally use

public class VMSettingsForm extends Form {

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
		setFromVal(c, r, com.pspkvm.system.VMSettings.get(k)); }
		
	// Reverse of above--write config string from control
	void writeToConfig(Choice s, String[] r, String k) {
		com.pspkvm.system.VMSettings.set(k, r[s.getSelectedIndex()]); }

}
