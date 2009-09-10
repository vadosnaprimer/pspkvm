package com.sun.midp.appmanager;

import java.io.*;
import javax.microedition.lcdui.CustomItem;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Image;
import com.sun.midp.installer.GraphicalInstaller;
import com.sun.midp.i18n.Resource;
import com.sun.midp.i18n.ResourceConstants;
import javax.microedition.lcdui.Command;
import java.util.TimerTask;
import java.util.Timer;

abstract class WriteableAMSCustomItem extends CustomItem {

	// Field specifiers for reading/writing in a stream
	protected static final int TYPE_FOLDER=0x00;
	protected static final int TYPE_MIDLET=0x01;

	// Standard font for drawing these
	static final Font ICON_FONT = Font.getFont(Font.FACE_SYSTEM,
		Font.STYLE_BOLD, Font.SIZE_SMALL);
	// Folder images
	final static Image folderImg =
		GraphicalInstaller.getImageFromInternalStorage("folder");
	final static Image folderOpenImg =
		GraphicalInstaller.getImageFromInternalStorage("folder_open");

		
	/**
	* The image used to draw background for the midlet representation.
	* IMPL NOTE: it is assumed that background image is larger or equal
	* than all other images that are painted over it
	*/
	protected static final Image ICON_BG =
		GraphicalInstaller.getImageFromInternalStorage("_ch_hilight_bg");
	
	// Cached item height
	protected static final int itemHeight =
		ICON_BG.getHeight() > ICON_FONT.getHeight() ?
		ICON_BG.getHeight() : ICON_FONT.getHeight();

	/**
	* Cached background image width.
	*/
	protected static final int bgIconW = ICON_BG.getWidth();
	
	/**
	* Cached background image height.
	*/
	protected static final int bgIconH = ICON_BG.getHeight();
	
	/**
	* The icon used to display that user attention is requested
	* and that midlet needs to brought into foreground.
	*/
	protected static final Image FG_REQUESTED =
	GraphicalInstaller.getImageFromInternalStorage("_ch_fg_requested");
	
	/**
	* The image used to draw disable midlet representation.
	*/
	protected static final Image DISABLED_IMAGE =
	GraphicalInstaller.getImageFromInternalStorage("_ch_disabled");
	
	/**
	* The color used to draw midlet name
	* for the hilighted non-running running midlet representation.
	*/
	protected static final int ICON_HL_TEXT = 0x000B2876;
	
	/**
	* The color used to draw the shadow of the midlet name
	* for the non hilighted non-running midlet representation.
	*/
	protected static final int ICON_TEXT = 0x003177E2;
	
	/**
	* The color used to draw the midlet name
	* for the non hilighted running midlet representation.
	*/
	protected static final int ICON_RUNNING_TEXT = 0xbb0000;
	
	/**
	* The color used to draw the midlet name
	* for the hilighted running midlet representation.
	*/
	protected static final int ICON_RUNNING_HL_TEXT = 0xff0000;
	
	/**
	* Tha pad between custom item's icon and text
	*/
	protected static final int ITEM_PAD = 2;
	
	/**
	* Cached truncation mark
	*/
	protected static final char truncationMark =
		Resource.getString(ResourceConstants.TRUNCATION_MARK).charAt(0);
	
  /**
	* Gets the preferred height of a midlet representation in
	* the App Selector Screen based on the passed in width.
	* @param width the amount of width available for this Item
	* @return the minimum height of a midlet representation
	*         in the App Selector Screen.
	*/
	protected int getPrefContentHeight(int width) {
		return itemHeight; }

	/**
	* Gets the minimum height of a midlet representation in
	* the App Selector Screen.
	* @return the minimum height of a midlet representation
	*         in the App Selector Screen.
	*/
	protected int getMinContentHeight() {
		return itemHeight; }
		
	/**
	* Repaints MidletCustomItem. Called when internal state changes.
	*/
	public void update() {
		repaint(); }

	// Passthrough constructor
	WriteableAMSCustomItem(String s) {
		super(s); }

	// Write to storage 
	abstract void write(DataOutputStream ostream) throws IOException;
	
	  /** The width of the item */
	int width; // = 0
	/** The height of the item */
	int height; // = 0
	
	/** Cached width of the truncation mark */
	int truncWidth;	
	/** The text of the item */
	char[] text;
	/** Length of the text */
	int textLen;

	/** The owner of this MidletCustomItem */
	AppManagerUI owner; // = null

	/**
	 * The icon to be used to draw this midlet representation.
	 */
	Image icon; // = null
	/** current default command */
	Command default_command; // = null
	
	/** A TimerTask which will repaint scrolling text  on a repeated basis */
	protected TextScrollPainter textScrollPainter;
	
	/**
	* Width of the scroll area for text
	*/
	protected int scrollWidth;
	
	/**
	* If text is truncated
	*/
	boolean truncated;
	
	/**
	* pixel offset to the start of the text field  (for example,  if
	* xScrollOffset is -60 it means means that the text in this
	* text field is scrolled 60 pixels left of the left edge of the
	* text field)
	*/
	protected int xScrollOffset;
	
	/**
	* Helper class used to repaint scrolling text
	* if needed.
	*/
	private class TextScrollPainter extends TimerTask {
		/**
		* Repaint the item text
		*/
		public final void run() {
		    repaintScrollText(); } }
	
	/** True if this MidletCustomItem has focus, and false - otherwise */
	boolean hasFocus; // = false;
	
	/**
	* Start the scrolling of the text
	*/
	protected void startScroll() {
		if (!hasFocus || !truncated) {
			return; }
		stopScroll();
		textScrollPainter = new TextScrollPainter();
		textScrollTimer.schedule(textScrollPainter, SCROLL_DELAY, SCROLL_RATE); }
	
	/**
	* Stop the scrolling of the text
	*/
	protected void stopScroll() {
		if (textScrollPainter == null) {
			return; }
		xScrollOffset = 0;
		textScrollPainter.cancel();
		textScrollPainter = null;
		repaint(bgIconW, 0, width, height); }
	
	/**
	* Called repeatedly to animate a side-scroll effect for text
	*/
	protected void repaintScrollText() {
		if (-xScrollOffset < scrollWidth) {
			xScrollOffset -= SCROLL_SPEED;
			repaint(bgIconW, 0, width, height); }
		else {
		// already scrolled to the end of text
			stopScroll(); } }
			
	/** A Timer which will handle firing repaints of the ScrollPainter */
	protected static Timer textScrollTimer;
	
	/** Text auto-scrolling parameters */
	private static int SCROLL_RATE = 250;
	private static int SCROLL_DELAY = 500;
	private static int SCROLL_SPEED = 10;
	
	static void stopTimer() {
		textScrollTimer.cancel(); }

	/**
	* Sets the owner (AppManagerUI) of this MidletCustomItem
	* @param hs The AppSelector in which this MidletCustomItem is shown
	*/
	void setOwner(AppManagerUI hs) {
		owner = hs; }
	
	/**
	* Sets default <code>Command</code> for this <code>Item</code>.
	*
	* @param c the command to be used as this <code>Item's</code> default
	* <code>Command</code>, or <code>null</code> if there is to
	* be no default command
	*/
	public void setDefaultCommand(Command c) {
		default_command = c;
		super.setDefaultCommand(c); }

}
