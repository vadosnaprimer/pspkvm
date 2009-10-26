/*
	A really simple text editor / note taker midlet.
	Developed for the PSPKVM, but should work in any ME implementation that provides
	a virtual keyboard.
	AJ Milne for the PSPKVM project.
	
	License is GPL. Free software. Modify, if you wish, but please post your very cool
	enhancements back somewhere we can find 'em; thanx.
*/

import javax.microedition.midlet.*;
import com.sun.midp.midlet.MIDletSuite;
import javax.microedition.lcdui.*;
import java.util.Vector;
import java.io.*;
import javax.microedition.rms.*;
import java.util.*;
import javax.microedition.lcdui.Clipboard;

public class NoteMidlet extends MIDlet implements CommandListener {

	// Add/rename form commands
	static final Command editNameFormCancelCmd = new Command("Cancel", Command.CANCEL, 1);
	static final Command editNameFormConfirmCmd = new Command("OK", Command.OK, 1);

	// Text box (edit content box) commands
	static final Command doneTBCommand = new Command("Done", Command.OK, 1);
	static final Command cancelTBCommand = new Command("Cancel", Command.CANCEL, 1);

	// Main list commands
	static final Command exitCommand = new Command("Exit", Command.EXIT, 1);
	static final Command addCommand = new Command("New note", Command.ITEM, 2);
	static final Command editCommand = new Command("Edit", Command.ITEM, 2);
	static final Command deleteCommand = new Command("Delete", Command.ITEM, 2);
	static final Command renameCommand = new Command("Rename", Command.ITEM, 2);
	static final Command saveCommand = new Command("Save notes", Command.SCREEN, 3);
	static final Command moveUpCommand = new Command("Move up", Command.ITEM, 4);
	static final Command moveDownCommand = new Command("Move down", Command.ITEM, 4);
	static final Command moveTopCommand = new Command("Move to top", Command.ITEM, 5);
	static final Command moveBottomCommand = new Command("Move to bottom", Command.ITEM, 5);
	static final Command setFromClipCommand = new Command("Set from clipboard", Command.ITEM, 3);
	static final Command copyFromNoteCommand = new Command("Copy to clipboard", Command.ITEM, 2);
	
	// Constants for field sizes, permanent storage locations
	static final int MAX_VALUE_SIZE = 8192;
	static final int MAX_NAME_SIZE = 128;
	static final String NOTE_STORE = "notes_storage";
	static final int NOTES_RECORD_ID = 1;
	
	// GUI components, contents, state
	List notes;
	TextField editNameFormTF;
	Form editNameForm;
	Vector noteContent;
	// Set when storage and in-core content are out of synch.
	boolean modified;
	// We use this boolean to determine what 'editNameFormConfirmCommand'
	// coming back means--if it's true, we're doing an add. If it's false,
	// we're doing a rename.
	boolean add_mode;
	
	// Helper/utility for updateListCommands()
	void enableCommand(Command c, boolean e) {
		if (e) {
			notes.addCommand(c);
			return; }
		notes.removeCommand(c); }
	
	// Enables/disables commands that shouldn't always appear...
	void updateListCommands() {
		boolean listHasContent = (notes.size()>0);
		enableCommand(editCommand, listHasContent);
		enableCommand(deleteCommand, listHasContent);
		enableCommand(renameCommand, listHasContent);
		if (listHasContent) {
			notes.setSelectCommand(editCommand); }
		enableCommand(setFromClipCommand, listHasContent);
		enableCommand(moveUpCommand, listHasContent);
		enableCommand(moveDownCommand, listHasContent);
		enableCommand(moveTopCommand, listHasContent);
		enableCommand(moveBottomCommand, listHasContent);
		enableCommand(copyFromNoteCommand, listHasContent);
		enableCommand(saveCommand, modified); }

	// Set the modified flag
	void setModified() {
		modified=true;
		updateListCommands();
		notes.setTitle("Notes (modified)"); }
		
	// Clear the modified flag
	void clearModified() {
		modified=false;
		updateListCommands();
		notes.setTitle("Notes"); }

	// Set up the form for editing names
	void setupeditNameForm() {
		editNameFormTF = new TextField("Name", "new note", MAX_NAME_SIZE, TextField.ANY);
		editNameForm = new Form("Add note");
		editNameForm.append(editNameFormTF);
		editNameForm.addCommand(editNameFormCancelCmd);
		editNameForm.addCommand(editNameFormConfirmCmd);
		editNameForm.setCommandListener(this); }
	
	// Constructor
	public NoteMidlet() {
		setupeditNameForm();
		add_mode=true;
		notes = new List("Notes", Choice.IMPLICIT);
		noteContent = new Vector();
		notes.setCommandListener(this); }
	
	// Midlet interface method called at start
	protected void startApp() {
		notes.addCommand(addCommand);
		notes.addCommand(saveCommand);
		notes.addCommand(exitCommand);
		readNotesFromStorage();
		if (notes.size()>0) {
			updateListCommands(); }
		Display.getDisplay(this).setCurrent(notes);
		try{ Thread.currentThread().sleep(2000); }
		catch(Exception e){} }
	
	// Midlet interface method called when resuming
	protected void pauseApp() {}

	// Midlet interface method called on destroy
	protected void destroyApp(boolean bool) {
		if (modified) {
			writeNotesToStorage(); } }
			
	// Swap two elements in the list (used by move commands)
	void swapElements(int a, int b) {
		String s = notes.getString(a);
		notes.set(a, notes.getString(b), null);
		notes.set(b, s, null);
		Object sb = noteContent.elementAt(a);
		noteContent.setElementAt(noteContent.elementAt(b), a);
		noteContent.setElementAt(sb, b); }
	
	// Move a note down the list
	void moveUp() {
		if (notes.size()<2) {
			return; }
		int i = notes.getSelectedIndex();
		if (i==0) {
			return; }
		swapElements(i, i-1);
		notes.setSelectedIndex(i-1, true); }
	
	// Move a note up the list
	void moveDown() {
		if (notes.size()<2) {
			return; }
		int i = notes.getSelectedIndex();
		if (i==(notes.size()-1)) {
			return; }
		swapElements(i, i+1);
		notes.setSelectedIndex(i+1, true); }

	// Move a note to the bottom of the list
	void moveToBottom() {
		if (notes.size()<2) {
			return; }
		int i = notes.getSelectedIndex();
		int e = notes.size()-1;
		if (i==e) {
			return; }
		String s = notes.getString(i);
		Object o = noteContent.elementAt(i);
		notes.delete(i);
		noteContent.removeElementAt(i);
		notes.append(s, null);
		noteContent.addElement(o);
		e = notes.size()-1;
		notes.setSelectedIndex(e, true); }

	// Move a note to the top of the list
	void moveToTop() {
		if (notes.size()<2) {
			return; }
		int i = notes.getSelectedIndex();
		if (i==0) {
			return; }
		String s = notes.getString(i);
		Object o = noteContent.elementAt(i);
		notes.delete(i);
		noteContent.removeElementAt(i);
		notes.insert(0, s, null);
		noteContent.insertElementAt(o, 0);
		notes.setSelectedIndex(0, true); }
	
	// Command hander
	public void commandAction(Command cmd, Displayable disp) {
		if (cmd == editNameFormCancelCmd) {
			Display.getDisplay(this).setCurrent(notes);
			return; }
		if (cmd == editNameFormConfirmCmd) {
			if (add_mode) {
				// We're using the form to add a note. Add it,
				// and launch the content editor.
				addNote();
				launchEdit();
				return; }
			// Otherwise, we're in edit territory. Just rename.
			editName();
			Display.getDisplay(this).setCurrent(notes);
			return; }
		if (cmd == doneTBCommand) {
			editNote((TextBox)disp);
			Display.getDisplay(this).setCurrent(notes);
			return; }
		if (cmd == saveCommand) {
			writeNotesToStorage();
			return; }
		if (cmd == cancelTBCommand) {
			Display.getDisplay(this).setCurrent(notes);
			return; }
		if (cmd == editCommand) {
			launchEdit();
			return; }
		if (cmd == addCommand) {
			launchEditNameForm_Add();
			return; }
		if (cmd == moveUpCommand) {
			moveUp();
			return; }
		if (cmd == copyFromNoteCommand) {
			copyFromNote();
			return; }
		if (cmd == setFromClipCommand) {
			setFromClip();
			return; }
		if (cmd == moveDownCommand) {
			moveDown();
			return; }
		if (cmd == moveTopCommand) {
			moveToTop();
			return; }
		if (cmd == moveBottomCommand) {
			moveToBottom();
			return; }
		if (cmd == renameCommand) {
			launchEditNameForm_Rename();
			return; }
		if (cmd == deleteCommand) {
			deleteNote();
			return; }
		if (cmd == exitCommand) {
			destroyApp(false);
			notifyDestroyed(); } }

	// Launch the name setting form (when adding a new note)
	void launchEditNameForm_Add() {
		editNameFormTF.setString("new note");
		add_mode=true;
		Display.getDisplay(this).setCurrent(editNameForm); }

	// Launch the name setting form (when renaming a note)
	void launchEditNameForm_Rename() {
		editNameFormTF.setString(notes.getString(notes.getSelectedIndex()));
		add_mode=false;
		Display.getDisplay(this).setCurrent(editNameForm); }

	// Edit the name of a note
	void editName() {
		notes.set(notes.getSelectedIndex(), editNameFormTF.getString(), null);
		setModified(); }

	// Copy to the clipboard from a note
	void copyFromNote() {
		StringBuffer sb = (StringBuffer)(noteContent.elementAt(notes.getSelectedIndex()));
		Clipboard.set(sb.toString()); }
		
	// Error string
	static final String CLIP_EMPTY = "The clipboard is empty";

	// Set the content of the current note from the clipboard
	void setFromClip() {
		String s = Clipboard.get();
		if (s.length()==0) {
			Alert a = new Alert(null, CLIP_EMPTY, null, AlertType.ERROR);
			Display.getDisplay(this).setCurrent(a, notes);
			return; }
		StringBuffer sb = (StringBuffer)(noteContent.elementAt(notes.getSelectedIndex()));
		sb.setLength(0);
		sb.append(s);
		setModified(); }

	// Add a note
	void addNote() {
		notes.append(editNameFormTF.getString(), null);
		noteContent.addElement(new StringBuffer());
		setModified();
		if (notes.size()==1) {
			updateListCommands(); }
		notes.setSelectedIndex(notes.size()-1, true); }

	// Edit a note
	void editNote(TextBox t) {
		int i = notes.getSelectedIndex();
		StringBuffer b = (StringBuffer)(noteContent.elementAt(i));
		b.setLength(0);
		b.append(t.getString());
		setModified(); }

	// Lunch the content editing field
	void launchEdit() {
		// Get current content
		int i = notes.getSelectedIndex();
		StringBuffer b = (StringBuffer)(noteContent.elementAt(i));
		// Set up editing box
		TextBox editValueField = new TextBox("Edit field", "", MAX_VALUE_SIZE, TextField.ANY);
		editValueField.addCommand(doneTBCommand);
		editValueField.addCommand(cancelTBCommand);
		editValueField.setCommandListener(this);
		editValueField.setString(b.toString());
		editValueField.setTitle(notes.getString(notes.getSelectedIndex()));
		Display.getDisplay(this).setCurrent(editValueField); }
	
	// Delete a note
	void deleteNote() {
		int i = notes.getSelectedIndex();
		notes.delete(i);
		noteContent.removeElementAt(i);
		setModified();
		if (notes.size()==0) {
			updateListCommands(); } }
			
	// Write the notes to storage
	void writeNotesToStorage() {
		RecordStore noteStore = null;
		try {
			ByteArrayOutputStream bas = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bas);
			noteStore = RecordStore.openRecordStore(NOTE_STORE, true);
			int s = notes.size();
			dos.writeInt(s);
			for(int i = 0; i<s; i++) {
				dos.writeUTF(notes.getString(i));
				StringBuffer b = (StringBuffer)(noteContent.elementAt(i));
				dos.writeUTF(b.toString()); }
			byte[] bstream = bas.toByteArray();
			if (noteStore.getNumRecords()==0) {
				noteStore.addRecord(bstream, 0, bstream.length); }
			else {
				noteStore.setRecord(NOTES_RECORD_ID,
					bstream, 0, bstream.length); }
			clearModified(); }
		catch (RecordStoreException e) {
			/* TODO */ }
		catch (IOException e) {
			/* TODO */ }
		finally {
			if (noteStore != null) {
			try {
				noteStore.closeRecordStore(); }
			catch (RecordStoreException e) {
			/* TODO */ } } } }
			
	// Read the notes from storage
	void readNotesFromStorage() {
		RecordStore noteStore = null;
		try {
			noteStore = RecordStore.openRecordStore(NOTE_STORE, false);
			if (noteStore==null) {
				return; }
			byte[] bstream = noteStore.getRecord(NOTES_RECORD_ID);
			if (bstream == null) {
				return; }
			ByteArrayInputStream bis = new ByteArrayInputStream(bstream);
			DataInputStream dis = new DataInputStream(bis);
			int s = dis.readInt();
			for(int i=0; i<s; i++) {
				String n = dis.readUTF();
				String v = dis.readUTF();
				notes.append(n, null);
				noteContent.addElement(new StringBuffer(v)); } }
		catch (RecordStoreException e) {
			/* TODO */ }
		catch (IOException e) {
			/* TODO */ }
		finally {
			if (noteStore != null) {
			try {
				noteStore.closeRecordStore(); }
			catch (RecordStoreException e) {
			/* TODO */ } } } }
}
