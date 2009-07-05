package javax.microedition.lcdui;

import javax.microedition.lcdui.Image;
import java.util.Hashtable;
import javax.microedition.lcdui.Graphics;

/**
 * A soft font implementation -- stores fonts as predrawn images, hashed on
 * character values. Note that currently the height and space width are
 * hard-coded, and it does not do kerning. It's (so far) just used for
 * fast displays. You need to init the hash, too (see SFontInit_.. classes
 * in this package for examples).  
*/

public class SFont {
	public Hashtable imgs;
	private final static int SPCWDTH=5;
	
	public SFont() {
		imgs = new Hashtable(); }

	public int charWidth(char c) {
		if (c==' ') {
			return SPCWDTH; }
		Image i = (Image)(imgs.get(new Character(c)));
		if (i==null) { return 0; }
		return i.getWidth(); }
		
	public int stringWidth(String s) {
		int l = 0;
		for (int i=0; i<s.length(); i++) {
			l += charWidth(s.charAt(i));
			l++; }
		if (l>0) { l--; }
		return l; }
		
	public void drawChar(Graphics g, int x, int y, char c) {
		_drawChar(g, x, y, c); }
			
	private int _drawChar(Graphics g, int x, int y, char c) {
		if (c==' ') {
			return SPCWDTH; }
		Image i = (Image)(imgs.get(new Character(c)));
		if (i==null) { 
			return 0; }
		g.drawImage(i, x, y, g.TOP|g.LEFT);
		return i.getWidth(); }

	public void drawString(Graphics g, int x, int y, String s) {
		for(int i=0; i<s.length(); i++) {
			x+=_drawChar(g, x, y, s.charAt(i));
			x++; } }
			
	public int getHeight() {
		return 12; }

}