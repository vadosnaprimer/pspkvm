/*
	The default Latin/Roman alphabet map for the Danzeff
	virtual keyboard
*/

package javax.microedition.lcdui;

class DZ_Keymap_Roman extends DZ_Keymap {

	// Display map--no shift
	static final String[] dmap_0 = {
		BSPG, "m", SPCG, "n", 
		"?", "o", "p", "q",
		"!", "g", "h", "i",
		".", "d", "e", "f",
		",", "a", "b", "c",
		"-", "j", "k", "l",
		"(", "r", "s", "t",
		":", "u", "v", "w",
		")", "x", "y", "z" };
	
	// Metakey map--no shift
	static final int[] mmap_0 = {
		BSP, CHR, CHR, CHR, 
		CHR, CHR, CHR, CHR,
		CHR, CHR, CHR, CHR,
		CHR, CHR, CHR, CHR,
		CHR, CHR, CHR, CHR,
		CHR, CHR, CHR, CHR,
		CHR, CHR, CHR, CHR,
		CHR, CHR, CHR, CHR,
		CHR, CHR, CHR, CHR };
	
	// Char map--no shift
	static final char[] cmap_0 = {
		' ', 'm', ' ', 'n', 
		'?', 'o', 'p', 'q',
		'!', 'g', 'h', 'i',
		'.', 'd', 'e', 'f',
		',', 'a', 'b', 'c',
		'-', 'j', 'k', 'l',
		'(', 'r', 's', 't',
		':', 'u', 'v', 'w',
		')', 'x', 'y', 'z' };
	
	// Display map--rshift
	static final String[] dmap_1 = {
		BSPG, "M", SPCG, "N", 
		"\"", "O", "P", "Q",
		"*", "G", "H", "I",
		"@", "D", "E", "F",
		"^", "A", "B", "C",
		"_", "J", "K", "L",
		"=", "R", "S", "T",
		";", "U", "V", "W",
		"/", "X", "Y", "Z" };
		
	// Metakey map--rshift
	static final int[] mmap_1 = {
		BSP, CHR, CHR, CHR, 
		CHR, CHR, CHR, CHR,
		CHR, CHR, CHR, CHR,
		CHR, CHR, CHR, CHR,
		CHR, CHR, CHR, CHR,
		CHR, CHR, CHR, CHR,
		CHR, CHR, CHR, CHR,
		CHR, CHR, CHR, CHR,
		CHR, CHR, CHR, CHR };
	
	// Char map--rshift
	static final char[] cmap_1 = {
		' ', 'M', ' ', 'N', 
		'"', 'O', 'P', 'Q',
		'*', 'G', 'H', 'I',
		'@', 'D', 'E', 'F',
		'^', 'A', 'B', 'C',
		'_', 'J', 'K', 'L',
		'=', 'R', 'S', 'T',
		';', 'U', 'V', 'W',
		'/', 'X', 'Y', 'Z' };
		
	// Display map--lshift
	static final String[] dmap_2 = {
		BSPG, "", SPCG, "5", 
		"", "", CPYG, "6",
		"", "", PSTG, "3",
		"", "", SELG, "2",
		"", "", ENTG, "1",
		"", "", "", "4",
		"", "", "", "7",
		OKST, "", CNCS, "8",
		"", "", "0", "9" };

	// Metakey map--lshift
	static final int[] mmap_2 = {
		BSP, NUL, CHR, CHR, 
		NUL, NUL, CPY, CHR,
		NUL, NUL, PST, CHR,
		NUL, NUL, SEL, CHR,
		NUL, NUL, ENT, CHR,
		NUL, NUL, NUL, CHR,
		NUL, NUL, NUL, CHR,
		OK,  CHR, ESC, CHR,
		NUL, NUL, CHR, CHR };
	
	// Char map--lshift
	static final char[] cmap_2 = {
		' ', NUL, ' ', '5', 
		' ', ' ', ' ', '6',
		' ', ' ', ' ', '3',
		' ', ' ', ' ', '2',
		' ', ' ', ' ', '1',
		' ', ' ', ' ', '4',
		' ', ' ', ' ', '7',
		' ', ' ', ' ', '8',
		' ', ' ', '0', '9' };

	// Display map--both shifts
	static final String[] dmap_3 = {
		BSPG, "", SPCG, "", 
		"+", "\\", "=", "/",
		"-", "[", "_", "]",
		"\"", "<", "'", ">",
		",", "(", ".", ")",
		"!", "{", "?", "}",
		":", "@", ";", "#",
		"~", "$", "`", "%",
		"*", "^", "|", "&" };
		
	// Metakey map--both shifts
	static final int[] mmap_3 = {
		BSP, NUL, CHR, NUL, 
		CHR, CHR, CHR, CHR,
		CHR, CHR, CHR, CHR,
		CHR, CHR, CHR, CHR,
		CHR, CHR, CHR, CHR,
		CHR, CHR, CHR, CHR,
		CHR, CHR, CHR, CHR,
		CHR, CHR, CHR, CHR,
		CHR, CHR, CHR, CHR };
	
	// Char map--both shifts
	static final char[] cmap_3 = {
		' ', ' ', ' ', ' ', 
		'+', '\\', '=', '/',
		'-', '[', '_', ']',
		'"', '<', '\'', '>',
		',', '(', '.', ')',
		'!', '{', '?', '}',
		':', '@', ';', '#',
		'~', '$', '`', '%',
		'*', '^', '|', '&' };

	static final String[][] dmaps = {dmap_0, dmap_1, dmap_2, dmap_3};
	static final char[][] cmaps = {cmap_0, cmap_1, cmap_2, cmap_3};
	static final int[][] mmaps = { mmap_0, mmap_1, mmap_2, mmap_3};
	
	boolean isMeta(int m, int c) {
		return ((mmaps[m][c] != CHR) && (mmaps[m][c] != NUL)); }

	boolean isChar(int m, int c) {
		return (mmaps[m][c] == CHR); }

	boolean isNull(int m, int c) {
		return (mmaps[m][c] == NUL); }
		
	int getMeta(int m, int c) {
		return mmaps[m][c]; }

	char getChar(int m, int c) {
		return cmaps[m][c]; }

	String getDisplay(int m, int c) {
		return dmaps[m][c]; }

}
