/*
	Helper methods for navigating a doc in a large textbox--paragraph left, paragraph right,
	word left, word right.
	Currently used by semichordal boards--should also be helpful to other virtual boards.
	/AJ Milne
*/ 

package javax.microedition.lcdui;

// We put the metakey mapping/enum in its own class because
// Java 1.4 and below don't do enums. Makes it neater.
// Also--we descend from this (into SC_Keymap and below)
// and it allows them to handle these in their own namespaces.
public class TextFieldNav {

	static boolean isWhiteSpace(char c) {
		return (c <= 0x0020); }
		
	static int findFirstNonWhiteSpaceLeft(String s, int spos) {
		while ((spos > 0) && (isWhiteSpace(s.charAt(spos)))) {
			spos--; }
		return spos; }
		
	static int findFirstWhiteSpaceRight(String s, int spos) {
		int rbound = s.length()-1;
		while ((spos < rbound) && (!isWhiteSpace(s.charAt(spos)))) {
			spos++; }
		// If there's no whitespace at the end, point one past the end.
		return isWhiteSpace(s.charAt(spos)) ? spos : spos+1; }
		
	static int findWordStartLeft(String s, int spos) {
		while ((spos > 0) && (!isWhiteSpace(s.charAt(spos)))) {
			spos--; }
		return (isWhiteSpace(s.charAt(spos)) ? spos + 1 : spos); }
		
	static int findWordStartRight(String s, int spos) {
		int rbound = s.length()-1;
		while ((spos < rbound) && (isWhiteSpace(s.charAt(spos)))) {
			spos++; }
		return spos; }
		
	public static void wordLeft(TextFieldLFImpl t) {
		int spos = t.tf.getCaretPosition();
		String s = t.tf.getString();
		if (spos==0) { 
			return; }
		if (spos==1) {
			t.moveCursor(Canvas.LEFT);
			return; }
		int tgt_pos = findFirstNonWhiteSpaceLeft(s, spos-1);
		tgt_pos = findWordStartLeft(s, tgt_pos);
		t.setCaretPosition(tgt_pos); }

	public static void wordRight(TextFieldLFImpl t) {
		int spos = t.tf.getCaretPosition();
		String s = t.tf.getString();
		int rbound = s.length();
		if (spos>=rbound) { 
			return; }
		int tgt_pos = findFirstWhiteSpaceRight(s, spos);
		tgt_pos = findWordStartRight(s, tgt_pos);
		t.setCaretPosition(tgt_pos); }

	public static void paraLeft(TextFieldLFImpl t) {
		int spos = t.tf.getCaretPosition();
		String s = t.tf.getString();
		if (spos==0) { 
			return; }
		if (spos==1) {
			t.moveCursor(Canvas.LEFT);
			return; }
		int tgt_pos = s.lastIndexOf('\n', spos-2);
		if (tgt_pos == -1) {
			tgt_pos = 0; }
		else {
			tgt_pos++; }
		t.setCaretPosition(tgt_pos); }

	public static void paraRight(TextFieldLFImpl t) {
		int spos = t.tf.getCaretPosition();
		String s = t.tf.getString();
		int rbound = s.length();
		if (spos>=rbound) { 
			return; }
		int tgt_pos = s.indexOf('\n', spos);
		if (tgt_pos == -1) {
			tgt_pos = rbound; }
		else {
			tgt_pos++; }
		t.setCaretPosition(tgt_pos); }

}

