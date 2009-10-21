/*
	Abstract class for a single keymap -- see specific implementations 
	SC_KeyMap_Roman and SC_Keymap_Cyrillic
*/

package javax.microedition.lcdui;

abstract class SC_Keymap extends SC_Keys {

	// Get the character at a given position
	abstract char getOutputChar(boolean caps_lock, int offset);
	// Is the character at this offset lowercase alpha?
	abstract boolean isLCAlpha(boolean caps_lock, int offset);
	// Is the character at this offset uppercase alpha?
	abstract boolean isUCAlpha(boolean caps_lock, int offset);
	// Get the metakey at a given position (may be CHR, or NUL)
	abstract int getMetaKey(int offset);
	// Is this the lshifted equivalent of a ushifted key?
	abstract String getDisplayString(boolean caps_lock, int offset);
	// Is there a character at this position?
	abstract boolean isChar(int p);		
	// Is there a null at this position?
	abstract boolean isNull(int p);
	// Is there a metakey at this position?
	abstract boolean isMeta(int p);
	// Does this key cancel a transient map?
	boolean cancelsTransient(int o) {
		return true; }
	// Long form map name
	abstract String getMapName();
	// Short form map name
	abstract String getMapNameShort();
}
