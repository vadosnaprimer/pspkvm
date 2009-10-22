/*
	Abstract superclass for a Danzeff map
*/

package javax.microedition.lcdui;

abstract class DZ_Keymap extends SC_Keys {
	
	abstract	boolean isMeta(int m, int c);

	abstract boolean isChar(int m, int c);

	abstract boolean isNull(int m, int c);
		
	abstract int getMeta(int m, int c);

	abstract char getChar(int m, int c);

	abstract String getDisplay(int m, int c);

}
