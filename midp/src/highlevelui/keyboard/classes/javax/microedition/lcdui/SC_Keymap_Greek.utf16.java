/* SC_KeyMap_Greek -- the greek character keymap
*/

package javax.microedition.lcdui;

class SC_Keymap_Greek extends SC_Keymap {

	// Map's name (for display)
	String getMapName() {
		return "Ελληνικό"; }
		
	// Map's name (short)
	String getMapNameShort() {
		return "αβγδ"; } 

  // The map of non-meta characters. Note that where there are 
  // valid meta characters, space is set, but these are just 
  // placeholders (and using space makes the table more legible)
	static final char[] chordal_map_chars = {
		' ', 'α', 'ο', ' ', ' ', 'Α', 'Ο', ' ',
		' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
		'ε', 'ω', 'υ', 'ι', 'Ε', 'Ω', 'Υ', 'Ι',
		' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
		'κ', 'μ', 'σ', 'π', 'Κ', 'Μ', 'Σ', 'Π',
		'\'', ';', 'ς', '/', '"', ':', '^', '?',
		'η', 'τ', 'ν', 'ρ', 'Η', 'Τ', 'Ν', 'Ρ',
		',', '_', '.', '-', '?', ' ', ' ', ' ',
		'λ', 'δ', 'γ', 'χ', 'Λ', 'Δ', 'Γ', 'Χ',
		'+', '=', '*', '!', ' ', '@', '&', '\\',
		'θ', 'φ', 'β', 'ξ', 'Θ', 'Φ', 'Β', 'Ξ',
		' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
		'ζ', 'ψ', '0', '1', 'Ζ', 'Ψ', '%', '|',
		'<', '`', '$', ' ', '>', '#', '~', ' ',
		'6', '7', '8', '9', '(', ')', ' ', ' ',
		' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
		'2', '3', '4', '5', '[', ']', '{', '}',
		' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' };

  // The map of non-meta characters, w/ caps lock
  // selected. Same as above otherwise
	static final char[] chordal_map_chars_caps_lock = {
		' ', 'Α', 'Ο', ' ', ' ', 'α', 'ο', ' ',
		' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
		'Ε', 'Ω', 'Υ', 'Ι', 'ε', 'ω', 'υ', 'ι',
		' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
		'Κ', 'Μ', 'Σ', 'Π', 'κ', 'μ', 'σ', 'π',
		'\'', ';', 'ς', '/', '"', ':', '^', '?',
		'Η', 'Τ', 'Ν', 'Ρ', 'η', 'τ', 'ν', 'ρ',
		',', '_', '.', '-', '?', ' ', ' ', ' ',
		'Λ', 'Δ', 'Γ', 'Χ', 'λ', 'δ', 'γ', 'χ',
		'+', '=', '*', '!', ' ', '@', '&', '\\',
		'Θ', 'Φ', 'Β', 'Ξ', 'θ', 'φ', 'β', 'ξ',
		' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
		'Ζ', 'Ψ', '0', '1', 'ζ', 'ψ', '%', '|',
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

	// The display map--used just to give the user some idea
	// what this chordal combination will do for them. *Not*
	// used in triggering/processing strokes -- see the 
	// SC_Keys.chordal_map_meta and chordal_map_char arrays.
	public static final String[] chordal_map_d = {
		SPCG, "α", "ο", XACU, ENTG, "Α", "Ο", XACU,
		OKST, BSPG, DELG, ESCS, " ", " ", " ", " ",
		"ε", "ω", "υ", "ι", "Ε", "Ω", "Υ", "Ι",
		U_ARR, L_ARR, D_ARR, R_ARR, U_ARR, L_ARR, D_ARR, R_ARR,
		"κ", "μ", "σ", "π", "Κ", "Μ", "Σ", "Π",
		"'", ";", "ς", "/", "\"", ":", "^", "?",
		"η", "τ", "ν", "ρ", "Η", "Τ", "Ν", "Ρ",
		",", "_", ".", "-", "?", " ", SWMS, TABG,
		"λ", "δ", "γ", "χ", "Λ", "Δ", "Γ", "Χ",
		"+", "=", "*", "!", SLKS, "@", "&", "\\",
		"θ", "φ", "β", "ξ", "Θ", "Φ", "Β", "Ξ",
		XGRV, XACU, XCED, XTIL, XDIA, XRIN, XCIR, SYMS,
		"ζ", "ψ", "0", "1", "Ζ", "Ψ", "%", "|",
		"<", "`", "$", CPYG, ">", "#", "~", PSTG,
		"6", "7", "8", "9", "(", ")", " ", " ",
		U_DAR, L_DAR, D_DAR, R_DAR, U_DAR, L_DAR, D_DAR, R_DAR,
		"2", "3", "4", "5", "[", "]", "{", "}",
		" ", SELG, CLKG, DSPG, " ", SELG, CLKG, DSPG };

   public static final String[] chordal_map_d_caps_lock = {
		SPCG, "α", "ο", XACU, ENTG, "Α", "Ο", XACU,
		OKST, BSPG, DELG, ESCS, " ", " ", " ", " ",
		"Ε", "Ω", "Υ", "Ι", "ε", "ω", "υ", "ι",
		U_ARR, L_ARR, D_ARR, R_ARR, U_ARR, L_ARR, D_ARR, R_ARR,
		"Κ", "Μ", "Σ", "Π", "κ", "μ", "σ", "π",
		"'", ";", "ς", "/", "\"", ":", "^", "?",
		"Η", "Τ", "Ν", "Ρ", "η", "τ", "ν", "ρ",
		",", "_", ".", "-", "?", " ", SWMS, TABG,
		"Λ", "Δ", "Γ", "Χ", "λ", "δ", "γ", "χ",
		"+", "=", "*", "!", SLKS, "@", "&", "\\",
		"Θ", "Φ", "Β", "Ξ", "θ", "φ", "β", "ξ",
		XGRV, XACU, XCED, XTIL, XDIA, XRIN, XCIR, SYMS,
		"Ζ", "Ψ", "0", "1", "ζ", "ψ", "%", "|",
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
