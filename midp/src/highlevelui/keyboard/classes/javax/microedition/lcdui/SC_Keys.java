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
	
	// Handy enum for uppercase/lowercase/non-case maps
	// in symbol sets where it's inconvenient just to use
	// the range (as in Roman)
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

	
	public static final String helpStr_Eng = "Note that not all commands are available in all boards. See also the menu at the bottom of the screen, and alternate maps (see SWM).\n\nENT: Enter\nINS: Insert mode (toggle)\nBSP: Backspace (delete to left of cursor)\nDEL: Delete (delete to right of cursor)\nESC: Escape (cancels input in main boards)\nTAB: Tab\nPUP: Page up\nHME: Home\nPDN: Page down\nEND: End\nALT: Alt\nSEL: Select (toggle)\nCLK: Caps lock (toggle)\nCTL: Control\nDSP: Display mode (toggle)\nCPY: Copy\nPST: Paste\nSWM: Switch keymap (Cyrillic/English/etc.)\nSYM: Overlay symbol map (for one stroke)\nSLK: Overlay symbol map (and lock)\nCNC: Cancel (returns from overlay)\nCAL: Copy all\nCLR: Clear (all)\n";
	
}