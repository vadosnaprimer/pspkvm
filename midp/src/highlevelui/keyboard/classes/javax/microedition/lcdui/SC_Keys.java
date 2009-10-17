/*
	Field maps, &c for semichordal keyboard implementation.
	Base class--just contains field IDs
	/AJ Milne
*/ 

package javax.microedition.lcdui;

// We put the metakey mapping/enum in its own class because
// Java 1.4 and below don't do enums. Makes it neater.
// Also--we descend from this (into SC_Keymap and below)
// and it allows them to handle these in their own namespaces.
public class SC_Keys {

	// CHR in the metamap means this isn't a metakey, but a character
	public static final int CHR=127;

	// NUL in the metamap means this isn't mapped to anything, and should 
	// be ignored
	public static final int NUL=0;

	// Various control/latch/state/nav keys
	public static final int ENT=1;
	public static final int INS=2;
	public static final int BSP=3;
	public static final int DEL=4;
	public static final int ESC=5;
	public static final int TAB=6;
	public static final int CUP=7;
	public static final int CLF=8;
	public static final int CDN=9;
	public static final int CRT=10;
	public static final int PUP=11;
	public static final int HME=12;
	public static final int PDN=13;
	public static final int END=14;
	public static final int ALT=15;
	public static final int SEL=16;
	public static final int CLK=17;
	public static final int CTL=18;
	public static final int DSP=19;
	public static final int CPY=20;
	public static final int PST=21;
	// Switch map:
	public static final int SWM=22;
	// Overlay symbol map (one stroke):
	public static final int SYM=23;
	// Overlay symbol map (and lock):
	public static final int SLK=24;
	// Cancel (return from locked overlay):
	public static final int CNC=25;
	// Copy all:
	public static final int CAL=26;
	// Clear all:
	public static final int CLR=27;
	// Word left, word right
	public static final int WLF=28;
	public static final int WRT=29;
	// Document start, document end
	public static final int DCS=30;
	public static final int DCE=31;
	
	// Enum for use in uppercase/lowercase/non-case maps
	// in symbol sets where it's inconvenient just to use
	// the range
	public static final int NCS = 0;
	public static final int LCS = 1;
	public static final int UCS = 2;
	
	// Modifier/deadkey metakeys -- add
	// diacritics to preceding letters
	
	// Grave, acute, circumflex, tilde, 
	// diaeresis, ring, cedille/a:
	public static final int GRV=100;
	public static final int ACU=101;
	public static final int CIR=102;
	public static final int TIL=103;
	public static final int DIA=104;
	public static final int RIN=105;
	public static final int CED=106;
	
	// Breve, macron, stroke, caron,
	// ogonek middle dot, upper dot
	public static final int BRV=107;
	public static final int MCR=108;
	public static final int STR=109;
	public static final int CAR=110;
	public static final int OGO=111;
	public static final int MDT=112;
	public static final int UDT=113;
	
	// Ligature
	public static final int LIG=114;
	
	// Some useful strings for the display tables
	// Single arrows
	static final String L_ARR = "\u2190";
	static final String U_ARR = "\u2191";
	static final String R_ARR = "\u2192";
	static final String D_ARR = "\u2193";
	// Double arrows
	static final String L_DAR = "\u219e";
	static final String U_DAR = "\u219f";
	static final String R_DAR = "\u21a0";
	static final String D_DAR = "\u21a1";
	// Deadkey glyphs
	static final String XRIN = "\ue000";
	static final String XDIA = "\ue001";
	static final String XCIR = "\ue002";
	static final String XGRV = "\ue003";
	static final String XACU = "\ue004";
	static final String XCED = "\ue005";
	static final String XTIL = "\ue006";
	static final String XCAR = "\ue007";
	static final String XMCR = "\ue008";
	static final String XBRV = "\ue009";
	static final String XSTR = "\ue00a";
	static final String XOGO = "\ue00b";
	static final String XUDT = "\ue00c";
	static final String XMDT = "\ue00d";
	static final String XLIG = "\ue00e";
	
	// Enter, tab, edit key glyphs
	static final String ENTG = "\ue020";
	static final String TABG = "\ue021";
	static final String DELG = "\ue022";
	static final String BSPG = "\ue023";
	static final String CPYG = "\ue02e\ue02c";
	static final String PSTG = "\ue02e\ue02d";
	
	// Switch mode string (a Roman z next to a Cyrillic zhe)
	static final String SWMS = "z/\u0436";
	// Symbols transient shift string (infinity symbol)
	static final String SYMS = "\u221e";
	// Symbols lock mode string (infinity symbol w/ a hollow up arrow)
	static final String SLKS = "\u221e\ue027";
	// CNC mode string (a next to a hollow down arrow)
	static final String CNCS = "a\ue026";
	
	// Display gliph
	static final String DSPG = "\ue028";
	// Caps lock glyph (lock, up arrow)
	static final String CLKG = "\ue029\ue027";
	// Select glyph
	static final String SELG = "\ue02a";
	// Space glyph
	static final String SPCG = "\ue02b";
	// Escape string (x with a hollow down arrow)
	static final String ESCS = "x\ue026";
}