/* SC_KeyMap_Roman -- the roman character keymap
*/

package javax.microedition.lcdui;

class SC_Keymap_Roman extends SC_Keymap {

	// Map's name (for display)
	String getMapName() {
		return "Roman"; }

	// Map's name (short)
	String getMapNameShort() {
		return "abcd"; } 

  // The map of non-meta characters. Note that where there are 
  // valid meta characters, space is set, but these are just 
  // placeholders (and using space makes the table more legible)
	static final char[] chordal_map_chars = {
		' ', 'e', 't', 's', ' ', 'E', 'T', 'S',
		' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
		'o', 'a', 'u', 'i', 'O', 'A', 'U', 'I',
		' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
		'n', 'p', 'd', 'h', 'N', 'P', 'D', 'H',
		'\'', ';', '_', '/', '"', ':', '^', '?',
		'l', 'w', 'v', 'm', 'L', 'W', 'V', 'M',
		',', 'q', '.', '-', '?', 'Q', ' ', ' ',
		'f', 'g', 'c', 'r', 'F', 'G', 'C', 'R',
		'+', '=', '*', '!', ' ', '@', '&', '\\',
		'b', 'j', 'y', 'k', 'B', 'J', 'Y', 'K',
		' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
		'z', 'x', '0', '1', 'Z', 'X', '%', '|',
		'<', '`', '$', ' ', '>', '#', '~', ' ',
		'6', '7', '8', '9', '(', ')', ' ', ' ',
		' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
		'2', '3', '4', '5', '[', ']', '{', '}',
		' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' };

  // The map of non-meta characters, w/ caps lock
  // selected. Same as above otherwise
	static final char[] chordal_map_chars_caps_lock = {
		' ', 'E', 'T', 'S', ' ', 'e', 't', 's',
		' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
		'O', 'A', 'U', 'I', 'o', 'a', 'u', 'i',
		' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
		'N', 'P', 'D', 'H', 'n', 'p', 'd', 'h',
		'\'', ';', '_', '/', '"', ':', '^', '?',
		'L', 'W', 'V', 'M', 'l', 'w', 'v', 'm',
		',', 'Q', '.', '-', '?', 'q', ' ', ' ',
		'F', 'G', 'C', 'R', 'f', 'g', 'c', 'r',
		'+', '=', '*', '!', ' ', '@', '&', '\\',
		'B', 'J', 'Y', 'K', 'b', 'j', 'y', 'k',
		' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
		'Z', 'X', '0', '1', 'z', 'x', '%', '|',
		'<', '`', '$', ' ', '>', '#', '~', ' ',
		'6', '7', '8', '9', '(', ')', ' ', ' ',
		' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
		'2', '3', '4', '5', '[', ']', '{', '}',
		' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' };

	// The metakey mapping to the chordal keyboard
	public static final int[] chordal_map_meta = {
			CHR, CHR, CHR, CHR, ENT, CHR, CHR, CHR,
			OK,  BSP, DEL, ESC, NUL, NUL, NUL, NUL,
			CHR, CHR, CHR, CHR, CHR, CHR, CHR, CHR,
			CUP, CLF, CDN, CRT, CUP, WLF, CDN, WRT,
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
			CHR, CHR, CHR, CHR, CHR, CHR, NUL, NUL,
			PUP, HME, PDN, END, PUP, HME, PDN, END,
			CHR, CHR, CHR, CHR, CHR, CHR, CHR, CHR,
			NUL, SEL, CLK, DSP, NUL, SEL, CLK, DSP };
			
	// Helper for drawing -- maps out metakeys matched high
	// and low -- this one maps those that are low, with high right
	// above
	public static final boolean[] ls_matched_meta = {
			false, false, false, false, false, false, false, false,
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
			false, false, false, false, false, false, false, false,
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
			SPCG, "e", "t", "s", ENTG, "E", "T", "S",
			OKST, BSPG, DELG, ESCS, " ", " ", " ", " ",
			"o", "a", "u", "i", "O", "A", "U", "I",
			U_ARR, L_ARR, D_ARR, R_ARR, U_ARR, L_ARR, D_ARR, R_ARR,
			"n", "p", "d", "h", "N", "P", "D", "H",
			"'", ";", "_", "/", "\"", ":", "^", "?",
			"l", "w", "v", "m", "L", "W", "V", "M",
			",", "q", ".", "-", "?", "Q", SWMS, TABG,
			"f", "g", "c", "r", "F", "G", "C", "R",
			"+", "=", "*", "!", SLKS, "@", "&", "\\",
			"b", "j", "y", "k", "B", "J", "Y", "K",
			XGRV, XACU, XCED, XTIL, XDIA, XRIN, XCIR, SYMS,
			"z", "x", "0", "1", "Z", "X", "%", "|",
			"<", "`", "$", CPYG, ">", "#", "~", PSTG,
			"6", "7", "8", "9", "(", ")", " ", " ",
			U_DAR, L_DAR, D_DAR, R_DAR, U_DAR, L_DAR, D_DAR, R_DAR,
			"2", "3", "4", "5", "[", "]", "{", "}",
			" ", SELG, CLKG, DSPG, " ", SELG, CLKG, DSPG };

   public static final String[] chordal_map_d_caps_lock = {
			SPCG, "E", "T", "S", ENTG, "e", "t", "s",
			OKST, BSPG, DELG, ESCS, " ", " ", " ", " ",
			"O", "A", "U", "I", "o", "a", "u", "i",
			U_ARR, L_ARR, D_ARR, R_ARR, U_ARR, L_ARR, D_ARR, R_ARR,
			"N", "P", "D", "H", "n", "p", "d", "h",
			"'", ";", "_", "/", "\"", ":", "^", "?",
			"L", "W", "V", "M", "l", "w", "v", "m",
			",", "Q", ".", "-", "?", "q", SWMS, TABG,
			"F", "G", "C", "R", "f", "g", "c", "r",
			"+", "=", "*", "!", SLKS, "@", "&", "\\",
			"B", "J", "Y", "K", "b", "j", "y", "k",
			XGRV, XACU, XCED, XTIL, XDIA, XRIN, XCIR, SYMS,
			"Z", "X", "0", "1", "z", "x", "%", "|",
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
		if (!isChar(offset)) {
			return false; }
		char c = caps_lock ?
			chordal_map_chars_caps_lock[offset] :
			chordal_map_chars[offset];
		if (c > 'z') {
			return false; }
		return (c >= 'a'); }

	// Is the character at this offset uppercase alpha?
	boolean isUCAlpha(boolean caps_lock, int offset) {
		if (!isChar(offset)) {
			return false; }
		char c = caps_lock ?
			chordal_map_chars_caps_lock[offset] :
			chordal_map_chars[offset];
		if (c > 'Z') {
			return false; }
		return (c >= 'A'); }

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
