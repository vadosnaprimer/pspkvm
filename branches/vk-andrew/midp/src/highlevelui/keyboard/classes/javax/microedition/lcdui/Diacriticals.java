/**
 * Diacriticals maps for use by the semichordal board (and others, eventually).
 * Maps unicode characters to the same character with a given diacritical added.
 * TODO: Widen support to the full recommended European set.  
 */

package javax.microedition.lcdui;

public class Diacriticals {

	/* NB: There are tonnes more...
		Leaving for now. Generally, this class just handles stuff in the unicode set up
		to 0x00ff, and a few others I know are sorta common. See 'TODO' above... this
		is just a placeholder/quicky for now until we can wire in support for full
		modifier handlers, extended character set. Should support most Western European
		languages, anyway. */
		
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
			// Add acute to A with ring
			case '\u00c5': return '\u01fa';
			// Add acute to a with ring
			case '\u00e5': return '\u01fb';
			default: return a; } }

		// Same as for getAcute, but with ring
		static char getRing(char a) {
		switch(a) {
			case 'A': return '\u00c5';
			case 'U': return '\u016e';
			case 'a': return '\u00e5';
			case 'u': return '\u016f';
			// Add ring to A acute
			case '\u00c1': return '\u01fa';
			// Add ring to a acute
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

}
