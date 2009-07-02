/*
	Useful constants/utils for handling raw PSP control input
	/AJ Milne
*/ 

package javax.microedition.lcdui;

// TODO/FIXME: Put this somewhere more sensible/obviously proprietary/
// platform-specific. For now, it goes in the lcdui with the
// virtual keyboard which happens to use it. / AJM 
public class PSPCtrlCodes {

	/** Select button. */
	public static final int SELECT     = 0x000001;
	/** Start button. */
	public static final int START      = 0x000008;
	/** Up D-Pad button. */
	public static final int UP         = 0x000010;
	/** Right D-Pad button. */
	public static final int RIGHT			= 0x000020;
	/** Down D-Pad button. */
	public static final int DOWN			= 0x000040;
	/** Left D-Pad button. */
	public static final int LEFT      	= 0x000080;
	/** Left trigger. */
	public static final int LTRIGGER   = 0x000100;
	/** Right trigger. */
	public static final int RTRIGGER   = 0x000200;
	/** Triangle button. */
	public static final int TRIANGLE   = 0x001000;
	/** Circle button. */
	public static final int CIRCLE     = 0x002000;
	/** Cross button. */
	public static final int CROSS      = 0x004000;
	/** Square button. */
	public static final int SQUARE     = 0x008000;
	/** Home button. In user mode this bit is set if the exit dialog is visible. */
	public static final int HOME       = 0x010000;
	/** Hold button. */
	public static final int HOLD       = 0x020000;
	/** Music Note button. */
	public static final int NOTE       = 0x800000;
	/** Screen button. */
	public static final int SCREEN     = 0x400000;
	/** Volume up button. */
	public static final int VOLUP      = 0x100000;
	/** Volume down button. */
	public static final int VOLDOWN    = 0x200000;
	/** Wlan switch up. */
	public static final int WLAN_UP    = 0x040000;
	/** Remote hold position. */
	public static final int REMOTE     = 0x080000;	
	/** Disc present. */
	public static final int DISC       = 0x1000000;
	/** Memory stick present. */
	public static final int MS         = 0x2000000;

	/** Union of all symbols */
	public static final int SYMBOLS = SQUARE | CIRCLE | TRIANGLE | CROSS;
	/** Union of all chordal keys */
	public static final int CHORDAL_KEYS = LTRIGGER | RTRIGGER | UP | RIGHT | DOWN | LEFT;
	/** Union of all the DPAD keys */
	public static final int DPAD_MASK = UP | LEFT | RIGHT | DOWN;
}
