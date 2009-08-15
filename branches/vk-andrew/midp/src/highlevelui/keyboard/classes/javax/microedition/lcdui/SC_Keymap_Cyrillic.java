/* SC_KeyMap_Cyrillic -- the cyrillic character keymap
*/

package javax.microedition.lcdui;

class SC_Keymap_Cyrillic extends SC_Keymap {

	// The metakey mapping to the chordal keyboard
	public static final int[] chordal_map_meta = {
			CHR, CHR, CHR, CHR, ENT, CHR, CHR, CHR,
			INS, BSP, DEL, ESC, NUL, NUL, NUL, NUL,
			CHR, CHR, CHR, CHR, CHR, CHR, CHR, CHR,
			CUP, CLF, CDN, CRT, PUP, HME, PDN, END,
			CHR, CHR, CHR, CHR, CHR, CHR, CHR, CHR,
			CHR, CHR, CHR, CHR, CHR, CHR, CHR, CHR,
			CHR, CHR, CHR, CHR, CHR, CHR, CHR, CHR,
			CHR, CHR, CHR, CHR, CHR, CHR, SWM, TAB,
			CHR, CHR, CHR, CHR, CHR, CHR, CHR, CHR,
			CHR, CHR, CHR, CHR, SLK, CHR, CHR, CHR,
			CHR, CHR, CHR, CHR, CHR, CHR, CHR, CHR,
			GRV, ACU, CED, TIL, DIA, RIN, CIR, SYM,
			CHR, CHR, CHR, CHR, CHR, CHR, CHR, CHR,
			CHR, CHR, CHR, CPY, CHR, CHR, CHR, PST,
			CHR, CHR, CHR, CHR, CHR, CHR, CHR, CHR,
			CHR, CHR, CHR, CHR, CHR, CHR, CHR, CHR,
			CHR, CHR, CHR, CHR, CHR, CHR, CHR, CHR,
			ALT, SEL, CLK, DSP, CTL, SEL, CLK, DSP };

  // The map of non-meta characters. Note that where there are 
  // valid meta characters, space is set, but these are just 
  // placeholders (and using space makes the table more legible)
	static final char[] chordal_map_chars = {
		' ', '\u0430', '\u0438', '\u043e', ' ', '\u0410', '\u0418', '\u041e',
		' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
		'\u0435', '\u0451', '\u043d', '\u0442', '\u0415', '\u0401', '\u041d', '\u0422',
		' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
		'\u0440', '\u0441', '\u0432', '\u043b', '\u0420', '\u0421', '\u0412', '\u041b',
		'\'', ';', '_', '/', '"', ':', '^', '?',
		'\u0434', '\u043f', '\u043a', '\u043c', '\u0414', '\u041f', '\u041a', '\u041c',
		',', '(', '.', '-', '?', ')', ' ', ' ',
		'\u0437', '\u044f', '\u044a', '\u0431', '\u0417', '\u042f', '\u042a', '\u0411',
		'+', '=', '*', '!', ' ', '@', '&', '\\',
		'\u0447', '\u0446', '\u0449', '\u0448', '\u0427', '\u0426', '\u0429', '\u0428',
		' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
		'\u0443', '\u0433', '\u0436', '\u0445', '\u0423', '\u0413', '\u0416', '\u0425',
		'<', '`', '$', ' ', '>', '#', '~', ' ',
		'\u0444', '\u0439', '\u044e', '\u044c', '\u0424', '\u0419', '\u042e', '\u042c',
		'0', '1', '2', '3', '4', '5', '6', '7',
		'\u044d', '\u044b', '8', '9', '\u042d', '\u042b', '%', '|',
		' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' };

  // The map of non-meta characters, w/ caps lock
  // selected. Same as above otherwise
	static final char[] chordal_map_chars_caps_lock = {
		' ', '\u0410', '\u0418', '\u041e', ' ', '\u0430', '\u0438', '\u043e',
		' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
		'\u0415', '\u0401', '\u041d', '\u0422', '\u0435', '\u0451', '\u043d', '\u0442',
		' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
		'\u0420', '\u0421', '\u0412', '\u041b', '\u0440', '\u0441', '\u0432', '\u043b',
		'\'', ';', '_', '/', '"', ':', '^', '?',
		'\u0414', '\u041f', '\u041a', '\u041c', '\u0434', '\u043f', '\u043a', '\u043c',
		',', '(', '.', '-', '?', ')', ' ', ' ',
		'\u0417', '\u042f', '\u042a', '\u0411', '\u0437', '\u044f', '\u044a', '\u0431',
		'+', '=', '*', '!', ' ', '@', '&', '\\',
		'\u0427', '\u0426', '\u0429', '\u0428', '\u0447', '\u0446', '\u0449', '\u0448',
		' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
		'\u0423', '\u0413', '\u0416', '\u0425', '\u0443', '\u0433', '\u0436', '\u0445',
		'<', '`', '$', ' ', '>', '#', '~', ' ',
		'\u0424', '\u0419', '\u042e', '\u042c', '\u0444', '\u0439', '\u044e', '\u044c',
		'0', '1', '2', '3', '4', '5', '6', '7',
		'\u042d', '\u042b', '8', '9', '\u044d', '\u044b', '%', '|',
		' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' };
			
  // Map of upper-case/lower-case/non-case sensitive chars
	static final int[] chordal_map_case = {
		NCS, LCS, LCS, LCS, NCS, UCS, UCS, UCS,
		NCS, NCS, NCS, NCS, NCS, NCS, NCS, NCS,
		LCS, LCS, LCS, LCS, UCS, UCS, UCS, UCS,
		NCS, NCS, NCS, NCS, NCS, NCS, NCS, NCS,
		LCS, LCS, LCS, LCS, UCS, UCS, UCS, UCS,
		NCS, NCS, NCS, NCS, NCS, NCS, NCS, NCS,
		LCS, LCS, LCS, LCS, UCS, UCS, UCS, UCS,
		NCS, NCS, NCS, NCS, NCS, NCS, NCS, NCS,
		LCS, LCS, LCS, LCS, UCS, UCS, UCS, UCS,
		NCS, NCS, NCS, NCS, NCS, NCS, NCS, NCS,
		LCS, LCS, LCS, LCS, UCS, UCS, UCS, UCS,
		NCS, NCS, NCS, NCS, NCS, NCS, NCS, NCS,
		LCS, LCS, LCS, LCS, UCS, UCS, UCS, UCS,
		NCS, NCS, NCS, NCS, NCS, NCS, NCS, NCS,
		LCS, LCS, LCS, LCS, UCS, UCS, UCS, UCS,
		NCS, NCS, NCS, NCS, NCS, NCS, NCS, NCS,
		LCS, LCS, NCS, NCS, UCS, UCS, NCS, NCS,
		NCS, NCS, NCS, NCS, NCS, NCS, NCS, NCS };

	// Helper for drawing -- maps out metakeys matched high
	// and low -- this one maps those that are low, with high right
	// above
	public static final boolean[] ls_matched_meta = {
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, true,  true,  true,  false, false, false, false };
			
	// Helper for drawing -- opposite for above--metakeys
	// that are high, but matched below.
	public static final boolean[] us_matched_meta = {
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false, 
			false, false, false, false, false, false, false, false,
			false, false, false, false, false, true,  true,  true };

    // The display map--used just to give the user some idea
    // what this chordal combination will do for them. *Not*
    // used in triggering/processing strokes -- see the 
    // SC_Keys.chordal_map_meta and chordal_map_char arrays.
    // The diacritical keys all display 'representative' characters--
    // these are the unicode escapes in 12th line of array.
    public static final String[] chordal_map_d = {
			" ", "\u0430", "\u0438", "\u043e", "ENT", "\u0410", "\u0418", "\u041e",
			"INS", "BSP", "DEL", "ESC", " ", " ", " ", " ",
			"\u0435", "\u0451", "\u043d", "\u0442", "\u0415", "\u0401", "\u041d", "\u0422",
			"\u2191", "\u2190", "\u2193", "\u2192", "PUP", "HME", "PDN", "END",
			"\u0440", "\u0441", "\u0432", "\u043b", "\u0420", "\u0421", "\u0412", "\u041b",
			"'", ";", "_", "/", "\"", ":", "^", "?",
			"\u0434", "\u043f", "\u043a", "\u043c", "\u0414", "\u041f", "\u041a", "\u041c",
			",", "(", ".", "-", "?", ")", "SWM", "TAB",
			"\u0437", "\u044f", "\u044a", "\u0431", "\u0417", "\u042f", "\u042a", "\u0411",
			"+", "=", "*", "!", "SLK", "@", "&", "\\",
			"\u0447", "\u0446", "\u0449", "\u0448", "\u0427", "\u0426", "\u0429", "\u0428",
			"\ue003", "\ue004", "\ue005", "\ue006", "\ue001", "\ue000", "\ue002", "SYM",
			"\u0443", "\u0433", "\u0436", "\u0445", "\u0423", "\u0413", "\u0416", "\u0425",
			"<", "`", "$", "CPY", ">", "#", "~", "PST",
			"\u0444", "\u0439", "\u044e", "\u044c", "\u0424", "\u0419", "\u042e", "\u042c",
			"0", "1", "2", "3", "4", "5", "6", "7",
			"\u044d", "\u044b", "8", "9", "\u042d", "\u042b", "%", "|",
			"ALT", "SEL", "CLK", "DSP", "CTL", "SEL", "CLK", "DSP" };

   public static final String[] chordal_map_d_caps_lock = {
			" ", "\u0410", "\u0418", "\u041e", "ENT", "\u0430", "\u0438", "\u043e",
			"INS", "BSP", "DEL", "ESC", " ", " ", " ", " ",
			"\u0415", "\u0401", "\u041d", "\u0422", "\u0435", "\u0451", "\u043d", "\u0442",
			"\u2191", "\u2190", "\u2193", "\u2192", "PUP", "HME", "PDN", "END",
			"\u0420", "\u0421", "\u0412", "\u041b", "\u0440", "\u0441", "\u0432", "\u043b",
			"'", ";", "_", "/", "\"", ":", "^", "?",
			"\u0414", "\u041f", "\u041a", "\u041c", "\u0434", "\u043f", "\u043a", "\u043c",
			",", "(", ".", "-", "?", ")", "SWM", "TAB",
			"\u0417", "\u042f", "\u042a", "\u0411", "\u0437", "\u044f", "\u044a", "\u0431",
			"+", "=", "*", "!", "SLK", "@", "&", "\\",
			"\u0427", "\u0426", "\u0429", "\u0428", "\u0447", "\u0446", "\u0449", "\u0448",
			"\ue003", "\ue004", "\ue005", "\ue006", "\ue001", "\ue000", "\ue002", "SYM",
			"\u0423", "\u0413", "\u0416", "\u0425", "\u0443", "\u0433", "\u0436", "\u0445",
			"<", "`", "$", "CPY", ">", "#", "~", "PST",
			"\u0424", "\u0419", "\u042e", "\u042c", "\u0444", "\u0439", "\u044e", "\u044c",
			"0", "1", "2", "3", "4", "5", "6", "7",
			"\u042d", "\u042b", "8", "9", "\u044d", "\u044b", "%", "|",
			"ALT", "SEL", "CLK", "DSP", "CTL", "SEL", "CLK", "DSP" };

	// Get the character at a given position
	char getOutputChar(boolean caps_lock, int offset) {
		return (caps_lock ?
			chordal_map_chars_caps_lock[offset] :
			chordal_map_chars[offset]); }

	// Is the character at this offset lowercase alpha?
	boolean isLCAlpha(boolean caps_lock, int offset) {
		int case_ind = chordal_map_case[offset];
		if (case_ind == NCS) {
			return false; }
		boolean r = (case_ind == LCS);
		if (caps_lock) {
			r = !r; }
		return r; }

	// Is the character at this offset uppercase alpha?
	boolean isUCAlpha(boolean caps_lock, int offset) {
		int case_ind = chordal_map_case[offset];
		if (case_ind == NCS) {
			return false; }
		boolean r = (case_ind == UCS);
		if (caps_lock) {
			r = !r; }
		return r; }

	// Get the metakey at a given position (may be CHR, or NUL)
	int getMetaKey(int offset) {
		return chordal_map_meta[offset]; }
		
	// Is this the lshifted equivalent an ushifted key?
	boolean isLSMatchedMeta(int offset) {
		return ls_matched_meta[offset]; }

	// Is this the ushifted equivalent of an lshifted key?
	boolean isUSMatchedMeta(int offset) {
		return us_matched_meta[offset]; }

	// Get the display string at this location
	String getDisplayString(boolean caps_lock, int offset) {
		return (caps_lock ? 
			chordal_map_d_caps_lock[offset] :
			chordal_map_d[offset]); }
			
	// Is there a character at this position?
	boolean isChar(int p) {
		return (chordal_map_meta[p]==CHR); }

	// Is there a null at this position?
	boolean isNull(int p) {
		return (chordal_map_meta[p]==NUL); }
	
	// Is there a metakey at this position?
	boolean isMeta(int p) {
		return ((chordal_map_meta[p]!=NUL) && (chordal_map_meta[p]!=CHR)); }
}
