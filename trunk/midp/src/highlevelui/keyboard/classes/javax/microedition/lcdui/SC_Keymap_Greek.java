/* SC_KeyMap_Greek -- the greek character keymap
*/

package javax.microedition.lcdui;

class SC_Keymap_Greek extends SC_Keymap {

	// Map's name (for display)
	String getMapName() {
		return "\u0395\u03bb\u03bb\u03b7\u03bd\u03b9\u03ba\u03cc"; }
		
	// Map's name (short)
	String getMapNameShort() {
		return "\u03b1\u03b2\u03b3\u03b4"; } 

  // The map of non-meta characters. Note that where there are 
  // valid meta characters, space is set, but these are just 
  // placeholders (and using space makes the table more legible)
	static final char[] chordal_map_chars = {
		' ', '\u03b1', '\u03bf', ' ', ' ', '\u0391', '\u039f', ' ',
		' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
		'\u03b5', '\u03c9', '\u03c5', '\u03b9', '\u0395', '\u03a9', '\u03a5', '\u0399',
		' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
		'\u03ba', '\u03bc', '\u03c3', '\u03c0', '\u039a', '\u039c', '\u03a3', '\u03a0',
		'\'', ';', '\u03c2', '/', '"', ':', '^', '?',
		'\u03b7', '\u03c4', '\u03bd', '\u03c1', '\u0397', '\u03a4', '\u039d', '\u03a1',
		',', '_', '.', '-', '?', ' ', ' ', ' ',
		'\u03bb', '\u03b4', '\u03b3', '\u03c7', '\u039b', '\u0394', '\u0393', '\u03a7',
		'+', '=', '*', '!', ' ', '@', '&', '\\',
		'\u03b8', '\u03c6', '\u03b2', '\u03be', '\u0398', '\u03a6', '\u0392', '\u039e',
		' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
		'\u03b6', '\u03c8', '0', '1', '\u0396', '\u03a8', '%', '|',
		'<', '`', '$', ' ', '>', '#', '~', ' ',
		'6', '7', '8', '9', '(', ')', ' ', ' ',
		' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
		'2', '3', '4', '5', '[', ']', '{', '}',
		' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' };

  // The map of non-meta characters, w/ caps lock
  // selected. Same as above otherwise
	static final char[] chordal_map_chars_caps_lock = {
		' ', '\u0391', '\u039f', ' ', ' ', '\u03b1', '\u03bf', ' ',
		' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
		'\u0395', '\u03a9', '\u03a5', '\u0399', '\u03b5', '\u03c9', '\u03c5', '\u03b9',
		' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
		'\u039a', '\u039c', '\u03a3', '\u03a0', '\u03ba', '\u03bc', '\u03c3', '\u03c0',
		'\'', ';', '\u03c2', '/', '"', ':', '^', '?',
		'\u0397', '\u03a4', '\u039d', '\u03a1', '\u03b7', '\u03c4', '\u03bd', '\u03c1',
		',', '_', '.', '-', '?', ' ', ' ', ' ',
		'\u039b', '\u0394', '\u0393', '\u03a7', '\u03bb', '\u03b4', '\u03b3', '\u03c7',
		'+', '=', '*', '!', ' ', '@', '&', '\\',
		'\u0398', '\u03a6', '\u0392', '\u039e', '\u03b8', '\u03c6', '\u03b2', '\u03be',
		' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
		'\u0396', '\u03a8', '0', '1', '\u03b6', '\u03c8', '%', '|',
		'<', '`', '$', ' ', '>', '#', '~', ' ',
		'6', '7', '8', '9', '(', ')', ' ', ' ',
		' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
		'2', '3', '4', '5', '[', ']', '{', '}',
		' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' };

	// The metakey mapping to the chordal keyboard
	public static final int[] chordal_map_meta = {
		CHR, CHR, CHR, ACU, ENT, CHR, CHR, ACU,
		OK,  BSP, DEL, ESC, NUL, NUL, NUL, NUL,
		CHR, CHR, CHR, CHR, CHR, CHR, CHR, CHR,
		CUP, CLF, CDN, CRT, CUP, WLF, CDN, WRT,
		CHR, CHR, CHR, CHR, CHR, CHR, CHR, CHR,
		CHR, CHR, CHR, CHR, CHR, CHR, CHR, CHR,
		CHR, CHR, CHR, CHR, CHR, CHR, CHR, CHR,
		CHR, CHR, CHR, CHR, CHR, NUL, SWM, TAB,
		CHR, CHR, CHR, CHR, CHR, CHR, CHR, CHR,
		CHR, CHR, CHR, CHR, SLK, CHR, CHR, CHR,
		CHR, CHR, CHR, CHR, CHR, CHR, CHR, CHR,
		GRV, ACU, CED, TIL, DIA, RIN, CIR, SYM,
		CHR, CHR, CHR, CHR, CHR, CHR, CHR, CHR,
		CHR, CHR, CHR, CPY, CHR, CHR, CHR, PST,
		CHR, CHR, CHR, CHR, CHR, CHR, NUL, NUL,
		PUP, HME, PDN, END, PUP, HME, PDN, END,
		CHR, CHR, CHR, CHR, CHR, CHR, CHR, CHR,
		NUL, SEL, CLK, DSP, NUL, SEL, CLK, DSP };

	static final int[] chordal_map_case = {
		NCS, LCS, LCS, NCS, NCS, UCS, UCS, NCS,
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
		NCS, NCS, NCS, NCS, NCS, NCS, NCS, NCS,
		NCS, NCS, NCS, NCS, NCS, NCS, NCS, NCS,
		NCS, NCS, NCS, NCS, NCS, NCS, NCS, NCS,
		NCS, NCS, NCS, NCS, NCS, NCS, NCS, NCS,
		NCS, NCS, NCS, NCS, NCS, NCS, NCS, NCS };

	// Helper for drawing -- maps out metakeys matched high
	// and low -- this one maps those that are low, with high right
	// above
	public static final boolean[] ls_matched_meta = {
		false, false, false, true, false, false, false, false,
		false, false, false, false, false, false, false, false,
		false, false, false, false, false, false, false, false,
		true,  true,  true,  true,  false, false, false, false,
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
		true,  true,  true,  true,  false, false, false, false,
		false, false, false, false, false, false, false, false,
		false, true,  true,  true,  false, false, false, false };
			
	// Helper for drawing -- opposite for above--metakeys
	// that are high, but matched below.
	public static final boolean[] us_matched_meta = {
		false, false, false, false, false, false, false, true,
		false, false, false, false, false, false, false, false,
		false, false, false, false, false, false, false, false,
		false, false, false, false, true,  true,  true,  true,
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
		false, false, false, false, true,  true,  true,  true,
		false, false, false, false, false, false, false, false,
		false, false, false, false, false, true,  true,  true };

	// The display map--used just to give the user some idea
	// what this chordal combination will do for them. *Not*
	// used in triggering/processing strokes -- see the 
	// SC_Keys.chordal_map_meta and chordal_map_char arrays.
	public static final String[] chordal_map_d = {
		SPCG, "\u03b1", "\u03bf", XACU, ENTG, "\u0391", "\u039f", XACU,
		OKST, BSPG, DELG, ESCS, " ", " ", " ", " ",
		"\u03b5", "\u03c9", "\u03c5", "\u03b9", "\u0395", "\u03a9", "\u03a5", "\u0399",
		U_ARR, L_ARR, D_ARR, R_ARR, U_ARR, L_ARR, D_ARR, R_ARR,
		"\u03ba", "\u03bc", "\u03c3", "\u03c0", "\u039a", "\u039c", "\u03a3", "\u03a0",
		"'", ";", "\u03c2", "/", "\"", ":", "^", "?",
		"\u03b7", "\u03c4", "\u03bd", "\u03c1", "\u0397", "\u03a4", "\u039d", "\u03a1",
		",", "_", ".", "-", "?", " ", SWMS, TABG,
		"\u03bb", "\u03b4", "\u03b3", "\u03c7", "\u039b", "\u0394", "\u0393", "\u03a7",
		"+", "=", "*", "!", SLKS, "@", "&", "\\",
		"\u03b8", "\u03c6", "\u03b2", "\u03be", "\u0398", "\u03a6", "\u0392", "\u039e",
		XGRV, XACU, XCED, XTIL, XDIA, XRIN, XCIR, SYMS,
		"\u03b6", "\u03c8", "0", "1", "\u0396", "\u03a8", "%", "|",
		"<", "`", "$", CPYG, ">", "#", "~", PSTG,
		"6", "7", "8", "9", "(", ")", " ", " ",
		U_DAR, L_DAR, D_DAR, R_DAR, U_DAR, L_DAR, D_DAR, R_DAR,
		"2", "3", "4", "5", "[", "]", "{", "}",
		" ", SELG, CLKG, DSPG, " ", SELG, CLKG, DSPG };

   public static final String[] chordal_map_d_caps_lock = {
		SPCG, "\u03b1", "\u03bf", XACU, ENTG, "\u0391", "\u039f", XACU,
		OKST, BSPG, DELG, ESCS, " ", " ", " ", " ",
		"\u0395", "\u03a9", "\u03a5", "\u0399", "\u03b5", "\u03c9", "\u03c5", "\u03b9",
		U_ARR, L_ARR, D_ARR, R_ARR, U_ARR, L_ARR, D_ARR, R_ARR,
		"\u039a", "\u039c", "\u03a3", "\u03a0", "\u03ba", "\u03bc", "\u03c3", "\u03c0",
		"'", ";", "\u03c2", "/", "\"", ":", "^", "?",
		"\u0397", "\u03a4", "\u039d", "\u03a1", "\u03b7", "\u03c4", "\u03bd", "\u03c1",
		",", "_", ".", "-", "?", " ", SWMS, TABG,
		"\u039b", "\u0394", "\u0393", "\u03a7", "\u03bb", "\u03b4", "\u03b3", "\u03c7",
		"+", "=", "*", "!", SLKS, "@", "&", "\\",
		"\u0398", "\u03a6", "\u0392", "\u039e", "\u03b8", "\u03c6", "\u03b2", "\u03be",
		XGRV, XACU, XCED, XTIL, XDIA, XRIN, XCIR, SYMS,
		"\u0396", "\u03a8", "0", "1", "\u03b6", "\u03c8", "%", "|",
		"<", "`", "$", CPYG, ">", "#", "~", PSTG,
		"6", "7", "8", "9", "(", ")", " ", " ",
		U_DAR, L_DAR, D_DAR, R_DAR, U_DAR, L_DAR, D_DAR, R_DAR,
		"2", "3", "4", "5", "[", "]", "{", "}",
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
