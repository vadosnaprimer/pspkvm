/*
	Helper methods for navigating a doc in a textbox or textfield--provides paragraph left,
	paragraph right, word left, word right.
	Currently used by semichordal boards--should also be helpful to other virtual boards.
	/AJ Milne
*/ 

package javax.microedition.lcdui;

// Note: all methods are static.
// TODO: Ensure this works correctly for text written right to left.
public class TextFieldNav {

	// Internal helper--determines if this char is whitespace
	// (also treats ASCII control characters as whitespace,
	// as in java.lang.String.trim())
	static boolean isWhiteSpace(char c) {
		return (c <= 0x0020); }
		
	// Internal helper for word left--get us into a word
	// of it if we're somewhere in the space to its right--
	// returns the input position if we're already in a word.
	static int findFirstNonWhiteSpaceLeft(String s, int spos) {
		while ((spos > 0) && (isWhiteSpace(s.charAt(spos)))) {
			spos--; }
		return spos; }
		
	// Internal helper for word right--get us into the whitespace
	// to the right of a word (or point one past the end
	// at the end of the text)
	static int findFirstWhiteSpaceRight(String s, int spos) {
		int rbound = s.length()-1;
		while ((spos < rbound) && (!isWhiteSpace(s.charAt(spos)))) {
			spos++; }
		// If there's no whitespace at the end, point one past the end.
		return isWhiteSpace(s.charAt(spos)) ? spos : spos+1; }
		
	// Internal helper for word left--assuming we're in a word (on a
	// non-whitespace character), find the beginning of the word.
	static int findWordStartLeft(String s, int spos) {
		while ((spos > 0) && (!isWhiteSpace(s.charAt(spos)))) {
			spos--; }
		return (isWhiteSpace(s.charAt(spos)) ? spos + 1 : spos); }
		
	// Internal helper for word right--starting from the whitespace
	// to the left of a word, find the start of the word.
	static int findWordStartRight(String s, int spos) {
		int rbound = s.length()-1;
		while ((spos < rbound) && (isWhiteSpace(s.charAt(spos)))) {
			spos++; }
		return spos; }
		
	// Move to the beginning of the next word to the left,
	// or the beginning of the text, if this is the first word.
	public static void wordLeft(TextFieldLFImpl t) {
		int spos = t.tf.getCaretPosition();
		String s = t.tf.getString();
		if (spos==0) { 
			return; }
		if (spos==1) {
			t.moveCursor(Canvas.LEFT);
			return; }
		spos = findFirstNonWhiteSpaceLeft(s, spos-1);
		spos = findWordStartLeft(s, spos);
		t.setCaretPosition(spos);
		t.lRequestPaint(); }

	// Move to the beginning of the next word to the right,
	// or the end of the text, if this is the last word.
	public static void wordRight(TextFieldLFImpl t) {
		int spos = t.tf.getCaretPosition();
		String s = t.tf.getString();
		int rbound = s.length();
		if (spos>=rbound) { 
			return; }
		spos = findFirstWhiteSpaceRight(s, spos);
		spos = findWordStartRight(s, spos);
		t.setCaretPosition(spos);
   	t.lRequestPaint(); }

	// Move to the beginning of the next paragraph to the left,
	// or the beginning of the text, if this is the first paragraph. 
	public static void paraLeft(TextFieldLFImpl t) {
		int spos = t.tf.getCaretPosition();
		String s = t.tf.getString();
		if (spos==0) { 
			return; }
		if (spos==1) {
			t.moveCursor(Canvas.LEFT);
			return; }
		spos = s.lastIndexOf('\n', spos-2);
		if (spos == -1) {
			spos = 0; }
		else {
			spos++; }
		t.setCaretPosition(spos);
   	t.lRequestPaint(); }

	// Move to the beginning of the next paragraph to the right,
	// or the end of the text, if this is the last paragraph. 
	public static void paraRight(TextFieldLFImpl t) {
		int spos = t.tf.getCaretPosition();
		String s = t.tf.getString();
		int rbound = s.length();
		if (spos>=rbound) { 
			return; }
		spos = s.indexOf('\n', spos);
		if (spos == -1) {
			spos = rbound; }
		else {
			spos++; }
		t.setCaretPosition(spos);
   	t.lRequestPaint(); }

}
