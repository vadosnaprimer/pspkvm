/* SC_KeyMap_Cyrillic -- the cyrillic character keymap
*/

package javax.microedition.lcdui;

class SC_Keymap_Cyrillic extends SC_Keymap {

	// Map's name (for display)
	String getMapName() {
		return "\u041a\u0438\u0440\u0438\u0434\u0434\u0438\u0446\u0430"; }

	// Map's name (short)
	String getMapNameShort() {
		return "\u0430\u0431\u0432\u0433"; } 

	// The metakey mapping to the chordal keyboard
	public static final int[] chordal_map_meta = {
			CHR, CHR, CHR, CHR, ENT, CHR, CHR, CHR,
			OK,  BSP, DEL, ESC, NUL, NUL, NUL, NUL,
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
			NUL, SEL, CLK, DSP, NUL, SEL, CLK, DSP };

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

    // The display map--used just to give the user some idea
    // what this chordal combination will do for them. *Not*
    // used in triggering/processing strokes -- see the 
    // chordal_map_meta and chordal_map_char arrays.
    public static final String[] chordal_map_d = {
			SPCG, "\u0430", "\u0438", "\u043e", ENTG, "\u0410", "\u0418", "\u041e",
			OKST, BSPG, DELG, ESCS, " ", " ", " ", " ",
			"\u0435", "\u0451", "\u043d", "\u0442", "\u0415", "\u0401", "\u041d", "\u0422",
			U_ARR, L_ARR, D_ARR, R_ARR, U_DAR, L_DAR, D_DAR, R_DAR,
			"\u0440", "\u0441", "\u0432", "\u043b", "\u0420", "\u0421", "\u0412", "\u041b",
			"'", ";", "_", "/", "\"", ":", "^", "?",
			"\u0434", "\u043f", "\u043a", "\u043c", "\u0414", "\u041f", "\u041a", "\u041c",
			",", "(", ".", "-", "?", ")", SWMS, TABG,
			"\u0437", "\u044f", "\u044a", "\u0431", "\u0417", "\u042f", "\u042a", "\u0411",
			"+", "=", "*", "!", SLKS, "@", "&", "\\",
			"\u0447", "\u0446", "\u0449", "\u0448", "\u0427", "\u0426", "\u0429", "\u0428",
			XGRV, XACU, XCED, XTIL, XDIA, XRIN, XCIR, SYMS,
			"\u0443", "\u0433", "\u0436", "\u0445", "\u0423", "\u0413", "\u0416", "\u0425",
			"<", "`", "$", CPYG, ">", "#", "~", PSTG,
			"\u0444", "\u0439", "\u044e", "\u044c", "\u0424", "\u0419", "\u042e", "\u042c",
			"0", "1", "2", "3", "4", "5", "6", "7",
			"\u044d", "\u044b", "8", "9", "\u042d", "\u042b", "%", "|",
			" ", SELG, CLKG, DSPG, " ", SELG, CLKG, DSPG };

   public static final String[] chordal_map_d_caps_lock = {
			SPCG, "\u0410", "\u0418", "\u041e", ENTG, "\u0430", "\u0438", "\u043e",
			OKST, BSPG, DELG, ESCS, " ", " ", " ", " ",
			"\u0415", "\u0401", "\u041d", "\u0422", "\u0435", "\u0451", "\u043d", "\u0442",
			U_ARR, L_ARR, D_ARR, R_ARR, U_DAR, L_DAR, D_DAR, R_DAR,
			"\u0420", "\u0421", "\u0412", "\u041b", "\u0440", "\u0441", "\u0432", "\u043b",
			"'", ";", "_", "/", "\"", ":", "^", "?",
			"\u0414", "\u041f", "\u041a", "\u041c", "\u0434", "\u043f", "\u043a", "\u043c",
			",", "(", ".", "-", "?", ")", SWMS, TABG,
			"\u0417", "\u042f", "\u042a", "\u0411", "\u0437", "\u044f", "\u044a", "\u0431",
			"+", "=", "*", "!", SLKS, "@", "&", "\\",
			"\u0427", "\u0426", "\u0429", "\u0428", "\u0447", "\u0446", "\u0449", "\u0448",
			XGRV, XACU, XCED, XTIL, XDIA, XRIN, XCIR, SYMS,
			"\u0423", "\u0413", "\u0416", "\u0425", "\u0443", "\u0433", "\u0436", "\u0445",
			"<", "`", "$", CPYG, ">", "#", "~", PSTG,
			"\u0424", "\u0419", "\u042e", "\u042c", "\u0444", "\u0439", "\u044e", "\u044c",
			"0", "1", "2", "3", "4", "5", "6", "7",
			"\u042d", "\u042b", "8", "9", "\u044d", "\u044b", "%", "|",
			" ", SELG, CLKG, DSPG, " ", SELG, CLKG, DSPG };

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
