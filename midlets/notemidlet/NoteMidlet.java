/*
	A really simple text editor / note taker midlet.
	Developed for the PSPKVM, but should work in any ME implementation that provides
	a virtual keyboard.
	/ AJ Milne for the PSPKVM project.
*/

import javax.microedition.midlet.*;
import com.sun.midp.midlet.MIDletSuite;
import javax.microedition.lcdui.*;
import java.util.Vector;
import java.io.*;
import javax.microedition.rms.*;
import java.util.*;

public class NoteMidlet extends MIDlet implements CommandListener {

	// Add form commands
	Command addFormCancelCmd, addFormConfirmCmd;

	// Text box commands
	static Command doneTBCommand = new Command("Done", Command.OK, 1);
	static Command cancelTBCommand = new Command("Cancel", Command.CANCEL, 1);

	// Main list commands
	static Command exitCommand = new Command("Exit", Command.EXIT, 1);
	static Command addCommand = new Command("New note", Command.SCREEN, 2);
	static Command editCommand = new Command("Edit", Command.SCREEN, 2);
	static Command deleteCommand = new Command("Delete", Command.SCREEN, 2);
	static Command renameCommand = new Command("Rename", Command.SCREEN, 2);
	static Command saveCommand = new Command("Save notes", Command.SCREEN, 2);
	
	static final int MAX_SIZE = 8192;
	static final String NOTE_STORE = "notes_storage";
	static final int NOTES_RECORD_ID = 1;

	List notes;
	TextField addFormTF;
	TextBox editField;
	Form addForm;
	Vector noteContent;
	boolean modified;

	protected void setupAddForm() {
		addFormTF = new TextField("Name", "new note", 64, TextField.ANY);
		addForm = new Form("Add note");
		addForm.append(addFormTF);
		addFormCancelCmd = new Command("Cancel", Command.CANCEL, 1);
		addFormConfirmCmd = new Command("OK", Command.OK, 1);
		addForm.addCommand(addFormCancelCmd);
		addForm.addCommand(addFormConfirmCmd);
		addForm.setCommandListener(this); }
		
	protected void setupEditBox() {
		editField = new TextBox("Edit field", "", MAX_SIZE, TextField.ANY);
		editField.addCommand(doneTBCommand);
		editField.addCommand(cancelTBCommand);
		editField.setCommandListener(this); }
	
	public NoteMidlet() {
		setupAddForm();
		setupEditBox();
		notes = new List("Notes", Choice.IMPLICIT);
		noteContent = new Vector();
		notes.setCommandListener(this); }
		
	protected void addEditCommands() {
		notes.addCommand(editCommand);
		notes.addCommand(deleteCommand);
		notes.setSelectCommand(editCommand); }
	
	protected void removeEditCommands() {
		notes.removeCommand(editCommand);
		notes.removeCommand(deleteCommand); }
	
	protected void startApp() {
		notes.addCommand(addCommand);
		notes.addCommand(saveCommand);
		notes.addCommand(exitCommand);
		readNotesFromStorage();
		if (notes.size()>0) {
			addEditCommands(); }
		Display.getDisplay(this).setCurrent(notes);
		try{ Thread.currentThread().sleep(2000); }
		catch(Exception e){} }
	
	protected void pauseApp() {}

	protected void destroyApp(boolean bool) {
		if (modified) {
			writeNotesToStorage(); } }
	
	public void commandAction(Command cmd, Displayable disp) {
		if (cmd == addFormCancelCmd) {
			Display.getDisplay(this).setCurrent(notes);
			return; }
		if (cmd == addFormConfirmCmd) {
			addNote();
			launchEdit();
			return; }
		if (cmd == doneTBCommand) {
			editNote();
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
			launchAddForm();
			return; }
		if (cmd == deleteCommand) {
			deleteNote();
			return; }
		if (cmd == exitCommand) {
			destroyApp(false);
			notifyDestroyed(); } }

	void launchAddForm() {
		addFormTF.setString("new note");
		Display.getDisplay(this).setCurrent(addForm); }
		
	void addNote() {
		notes.append(addFormTF.getString(), null);
		noteContent.addElement(new StringBuffer());
		modified=true;
		if (notes.size()==1) {
			addEditCommands(); }
		notes.setSelectedIndex(notes.size()-1, true); }
			
	void editNote() {
		int i = notes.getSelectedIndex();
		StringBuffer b = (StringBuffer)(noteContent.elementAt(i));
		b.setLength(0);
		b.append(editField.getString());
		modified=true; }
			
	void launchEdit() {
		int i = notes.getSelectedIndex();
		StringBuffer b = (StringBuffer)(noteContent.elementAt(i));
		editField.setString(b.toString());
		editField.setTitle(notes.getString(notes.getSelectedIndex()));
		Display.getDisplay(this).setCurrent(editField); }
	
	void deleteNote() {
		int i = notes.getSelectedIndex();
		notes.delete(i);
		noteContent.removeElementAt(i);
		modified=true;
		if (notes.size()==0) {
			removeEditCommands(); } }
			
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
			modified=false; }
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
