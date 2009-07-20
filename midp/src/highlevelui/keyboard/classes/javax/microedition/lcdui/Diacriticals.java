/**
 * Diacriticals maps for use by the semichordal board (and others, eventually).
 * Maps unicode characters to the same character with a given diacritical added.
 * Supports all diacritical marks added to Roman bases specified in ENV 1973:1995,
 * Minimum European Subset of ISO/IEC 10646-1. Boards supporting this set are
 * probably useable for the vast majority of European languages.  
 *  
 * TODO: Add support for full set, incl. ENV 1973:1995 for Greek and Cyrillic
 * annotations, and remaining (non-minimal) Roman alphabet additions.
 * 
 */
// breve macron stroke caron ogonek middle_dot, dot_above
package javax.microedition.lcdui;

public class Diacriticals {
		
	// Top layer handler--calls the others within--given
	// a diacritical type, returns the character decorated
	// with it--if valid--or just the same character again
	// otherwise.
	public static char getDiacritical(char a, int d) {
		switch(d) {
			case SC_Keys.GRV: return getGrave(a); 
			case SC_Keys.ACU: return getAcute(a); 
			case SC_Keys.CIR: return getCircumflex(a);
			case SC_Keys.TIL: return getTilde(a); 
			case SC_Keys.DIA: return getDiaeresis(a);
			case SC_Keys.RIN: return getRing(a); 
			case SC_Keys.CED: return getCedille(a);
			case SC_Keys.BRV: return getBreve(a);
			case SC_Keys.MCR: return getMacron(a);
			case SC_Keys.STR: return getStroke(a);
			case SC_Keys.CAR: return getCaron(a);
			case SC_Keys.OGO: return getOgonek(a);
			case SC_Keys.MDT: return getMiddleDot(a);
			case SC_Keys.UDT: return getDotAbove(a);
			default: return a; } }

	// Receive a character, return the same character with
	// an acute accent added, if appropriate. If not appropriate,
	// or we have no map, return the same character back.
	static char getAcute(char a) {
		switch(a) {
			case 'A': return '\u00c1';
			case 'E': return '\u00c9';
			case 'I': return '\u00cd';
			case 'O': return '\u00d3';
			case 'U': return '\u00da';
			case 'Y': return '\u00dd';
			case 'a': return '\u00e1';
			case 'e': return '\u00e9';
			case 'i': return '\u00ed';
			case 'o': return '\u00f3';
			case 'u': return '\u00fa';
			case 'y': return '\u00fd';
			// Add acute to A,a with ring
			case '\u00c5': return '\u01fa';
			case '\u00e5': return '\u01fb';
			// Add acute to O,o with stroke
			case '\u00d8': return '\u01fe'; 
			case '\u00f8': return '\u01ff';
			// Make O,o acute O,o double acute
			case '\u00d3': return '\u0150';
			case '\u00f3': return '\u0151';
			// Make U,u acute U,u double acute
			case '\u00da': return '\u0170';
			case '\u00fa': return '\u0171';
			default: return a; } }

		// Same as for getAcute, but with ring
		static char getRing(char a) {
		switch(a) {
			case 'A': return '\u00c5';
			case 'U': return '\u016e';
			case 'a': return '\u00e5';
			case 'u': return '\u016f';
			// Add ring to A,a acute
			case '\u00c1': return '\u01fa';
			case '\u00e1': return '\u01fb';
			default: return a; } }

		// Same as for getAcute, but with grave
		static char getGrave(char a) {
		switch(a) {
			case 'A': return '\u00c0';
			case 'E': return '\u00c8';
			case 'I': return '\u00cc';
			case 'O': return '\u00d2';
			case 'U': return '\u00d9';
			case 'a': return '\u00e0';
			case 'e': return '\u00e8';
			case 'i': return '\u00ec';
			case 'o': return '\u00f2';
			case 'u': return '\u00f9';
			case 'W': return '\u1e80';
			case 'w': return '\u1e81';
			case 'Y': return '\u1ef2';
			case 'y': return '\u1ef3';
			default: return a; } }
			
		// Same as for getAcute, but with tilde
		static char getTilde(char a) {
		switch(a) {
			case 'A': return '\u00c3';
			case 'N': return '\u00d1';
			case 'I': return '\u0128';
			case 'O': return '\u00d5';
			case 'U': return '\u0168';
			case 'a': return '\u00e3';
			case 'n': return '\u00f1';
			case 'i': return '\u0129';
			case 'o': return '\u00f5';
			case 'u': return '\u0169';
			default: return a; } }
			
		// Same as for getAcute, but with circumflex
		static char getCircumflex(char a) {
		switch(a) {
			case 'A': return '\u00c2';
			case 'E': return '\u00ca';
			case 'I': return '\u00ce';
			case 'O': return '\u00d4';
			case 'U': return '\u00db';
			case 'a': return '\u00e2';
			case 'e': return '\u00ea';
			case 'i': return '\u00ee';
			case 'o': return '\u00f4';
			case 'u': return '\u00fb';
			case 'C': return '\u0108';
			case 'c': return '\u0109';
			case 'G': return '\u011c';
			case 'g': return '\u011d';
			case 'H': return '\u0124';
			case 'h': return '\u0125';
			case 'J': return '\u0134';
			case 'j': return '\u0135';
			case 'S': return '\u015c';
			case 's': return '\u015d';
			case 'W': return '\u0174';
			case 'w': return '\u0175';
			default: return a; } }
			
	// Same as for getAcute, but with diaeresis
	// NB: In Unicode, umlaut and diaeresis are encoded
	// (and thus rendered) identically
	static char getDiaeresis(char a) {
		switch(a) {
			case 'A': return '\u00c4';
			case 'E': return '\u00cb';
			case 'I': return '\u00cf';
			case 'O': return '\u00d6';
			case 'U': return '\u00dc';
			case 'W': return '\u1e84';
			case 'Y': return '\u0178';
			case 'a': return '\u00e4';
			case 'e': return '\u00eb';
			case 'i': return '\u00ef';
			case 'o': return '\u00f6';
			case 'u': return '\u00fc';
			case 'w': return '\u1e85';
			case 'y': return '\u00ff';
			// Add diaeresis to A,a with macron
			case '\u0100': return '\u01de'; 
			case '\u0101': return '\u01df';
			default: return a; } }

		// Same as for getAcute, but with cedille
	static char getCedille(char a) {
		switch(a) {
			case 'C': return '\u00c7';
			case 'c': return '\u00e7';
			case 'G': return '\u0122';
			case 'g': return '\u0123';
			case 'K': return '\u0136';
			case 'k': return '\u0137';
			case 'L': return '\u013b';
			case 'l': return '\u013c';
			case 'N': return '\u0145';
			case 'n': return '\u0146';
			case 'R': return '\u0156';
			case 'r': return '\u0157';
			case 'S': return '\u015e';
			case 's': return '\u015f';
			case 'T': return '\u0162';
			case 't': return '\u0163';
			case 'D': return '\u1e10';
			case 'd': return '\u1e11';
			default: return a; } }
			
	// Same as for getAcute, but with breve
	static char getBreve(char a) {
		switch(a) {
			case 'A': return '\u0102';
			case 'a': return '\u0103';
			case 'E': return '\u0114';
			case 'e': return '\u0115';
			case 'G': return '\u011e';
			case 'g': return '\u011f';
			case 'I': return '\u012c';
			case 'i': return '\u012d';
			case 'O': return '\u014e';
			case 'o': return '\u014f';
			case 'U': return '\u016c';
			case 'u': return '\u016d';
			default: return a; } }

	// Same as for getAcute, but with macron
	static char getMacron(char a) {
		switch(a) {
			case ' ': return '\u00af';
			case 'A': return '\u0100';
			case 'a': return '\u0101';
			case 'E': return '\u0112';
			case 'e': return '\u0113';
			case 'I': return '\u012a';
			case 'i': return '\u012b';
			case 'O': return '\u014c';
			case 'o': return '\u014d';
			case 'U': return '\u016a';
			case 'u': return '\u016b';
			// Add macron to A,a with diaeresis
			case '\u00c4': return '\u01de'; 
			case '\u00e4': return '\u01df';
			// Greek support -- alpha
			case '\u03b1': return '\u1fb1';
			case '\u0391': return '\u1fb9';
			// Greek support -- iota
			case '\u03b9': return '\u1fd1';
			case '\u0399': return '\u1fd9';
			// Greek support -- upsilon
			case '\u03c5': return '\u1fe1';
			case '\u03a5': return '\u1fe9';
			default: return a; } }

	// Same as for getAcute, but with stroke
	static char getStroke(char a) {
		switch(a) {
			case 'O': return '\u00d8';
			case 'o': return '\u00f8';
			case 'D': return '\u0110';
			case 'd': return '\u0111';
			case 'H': return '\u0126';
			case 'h': return '\u0127';
			case 'L': return '\u0141';
			case 'l': return '\u0142';
			case 'T': return '\u0166';
			case 't': return '\u0167';
			case 'G': return '\u01e4';
			case 'g': return '\u01e5';
			// Add stroke to O,o acute
			case '\u00d3': return '\u01fe'; 
			case '\u00f3': return '\u01ff';
			default: return a; } }

	// Same as for getAcute, but with caron
	static char getCaron(char a) {
		switch(a) {
		case ' ': return '\u02c7';
		case 'C': return '\u010c';
		case 'c': return '\u010d';
		case 'D': return '\u010e';
		case 'd': return '\u010f';
		case 'E': return '\u011a';
		case 'e': return '\u011b';
		case 'L': return '\u013d';
		case 'l': return '\u013e';
		case 'N': return '\u0147';
		case 'n': return '\u0148';
		case 'R': return '\u0158';
		case 'r': return '\u0159';
		case 'S': return '\u0160';
		case 's': return '\u0161';
		case 'T': return '\u0164';
		case 't': return '\u0165';
		case 'Z': return '\u017d';
		case 'z': return '\u017e';
		case 'G': return '\u01e6';
		case 'g': return '\u01e7';
		case 'K': return '\u01e8';
		case 'k': return '\u01e9';
		// EZH,ezh
		case '\u01b7': return '\u01ee';
		case '\u0292': return '\u01ef';
		// DZ, dz
		case '\u01f1': return '\u01c4';
		case '\u01f3': return '\u01c6';
		default: return a; } }

	// Same as for getAcute, but with ogonek
	static char getOgonek(char a) {
		switch(a) {
		case ' ': return '\u02db';
		case 'A': return '\u0104';
		case 'a': return '\u0105';
		case 'E': return '\u0118';
		case 'e': return '\u0119';
		case 'I': return '\u012e';
		case 'i': return '\u012f';
		case 'U': return '\u0172';
		case 'u': return '\u0173';
		default: return a; } }

	// Same as for getAcute, but with middle dot
	static char getMiddleDot(char a) {
		switch(a) {
		case ' ': return '\u00b7';
		case 'L': return '\u013f';
		case 'l': return '\u0140';
		default: return a; } }

	// Same as for getAcute, but with dot above
	static char getDotAbove(char a) {
		switch(a) {
		case ' ': return '\u02d9';
		case 'C': return '\u010a';
		case 'c': return '\u010b';
		case 'E': return '\u0116';
		case 'e': return '\u0117';
		case 'G': return '\u0120';
		case 'g': return '\u0121';
		case 'I': return '\u0130';
		case 'i': return '\u0131';
		case 'Z': return '\u017b';
		case 'z': return '\u017c';
		case 'B': return '\u1e02';
		case 'b': return '\u1e03';
		case 'D': return '\u1e0a';
		case 'd': return '\u1e0b';
		case 'F': return '\u1e1e';
		case 'f': return '\u1e1f';
		case 'M': return '\u1e40';
		case 'm': return '\u1e41';
		case 'P': return '\u1e56';
		case 'p': return '\u1e57';
		case 'S': return '\u1e60';
		case 's': return '\u1e61';
		case 'T': return '\u1e6a';
		case 't': return '\u1e6b';
		default: return a; } }

}
