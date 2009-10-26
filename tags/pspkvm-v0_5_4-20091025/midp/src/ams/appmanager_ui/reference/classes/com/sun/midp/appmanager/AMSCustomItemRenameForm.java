/*
 * Modified TextBox simplifying renaming AMSCustomItem instances. 
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
 * Original code AJ Milne / 2009
 *  
 **/

package com.sun.midp.appmanager;

import javax.microedition.lcdui.*;

class AMSCustomItemRenameForm extends TextBox {

	static final String RENAMING=("Renaming ");
	static final int MAX_SIZE = 64;
	static final Command doneCmd = new Command("Done", Command.OK, 1);
	static final Command cancelCmd = new Command("Cancel", Command.CANCEL, 1);

	AMSCustomItem target;
	boolean createFolder;
		
	void commitChanges() {
		target.setText(getString());
		if (createFolder) {
			AMSFolderCustomItem f = (AMSFolderCustomItem)target; 
			f.parent.append(f);
			f.parent.sort();
			// If the parent is open, make sure the folder gets visibly added
			f.parent.updateContentDisplay();
			// If it's not yet, open it.
			f.parent.setOpen(); } }

	boolean getCreateFolder() {
		return createFolder; }
	
	AMSCustomItemRenameForm(CommandListener l, AMSCustomItem t, boolean nf) {
		super(RENAMING + String.valueOf(t.text), String.valueOf(t.text), MAX_SIZE, TextField.ANY);
		target=t;
		createFolder = nf;
		addCommand(doneCmd);
		addCommand(cancelCmd);
		setCommandListener(l); }
}
  