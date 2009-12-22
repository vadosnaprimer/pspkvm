/**
 *
 * Superclass providing basic functionality for CustomItems representing
 * midlets and folders in the AMS.
 * 
 * A lot of the basic fields and all (non I/O, non Folder) stuff came out of
 * AppManagerUI.java, which had an inner class that provided some of this
 * functionality. I basically broke it up, spread it over a descendance tree
 * allowing me to override bits to do the folder items reasonably cleanly.
 * 
 * This class provides an array of convenient services--provides a sort of 
 * framework for painting, a write-to-stream interface, naming, mark/unmark,
 * etc. 
 *  
 * Original code AJ Milne / 2009
 *  
*/

package com.sun.midp.appmanager;

import java.io.*;
import javax.microedition.lcdui.CustomItem;
import javax.microedition.lcdui.Font;
import com.sun.midp.installer.GraphicalInstaller;
import com.sun.midp.i18n.Resource;
import com.sun.midp.i18n.ResourceConstants;
import javax.microedition.lcdui.Command;
import java.util.TimerTask;
import java.util.Timer;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

abstract class AMSCustomItem extends CustomItem implements Sortable {

	// Standard font for drawing these
	static final Font ICON_FONT = Font.getFont(Font.FACE_SYSTEM,
		Font.STYLE_BOLD, Font.SIZE_SMALL);
	// Folder images
	final static Image folderImg =
		GraphicalInstaller.getImageFromInternalStorage("folder");
	final static Image folderOpenImg =
		GraphicalInstaller.getImageFromInternalStorage("folder_open");
	final static Image markedImg =
		GraphicalInstaller.getImageFromInternalStorage("_marked");
	final static int markedImgW = markedImg.getWidth();
	final static int markedImgH = markedImg.getHeight();
	// Static indicating the current rev of the folder system--
	// written at start of stream to allow rev'ing the storage
	// format easier, in case it comes to this
	final static int STORAGE_FORMAT = 1;
		
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
	 * How much to indent each level of the tree
	 */
	protected static final int INDENT_W = 8; 	 	
	
	/** Command object for "Mark". */
	static final Command markCmd =
		new Command(Resource.getString
	 (ResourceConstants.MARK), Command.ITEM, 7);
	/** Command object for "UnMark". */
	static final Command unMarkCmd =
		new Command(Resource.getString
	 (ResourceConstants.UNMARK), Command.ITEM, 7);
	/** Command object for "Rename". */
	static final Command renameCmd =
		new Command(Resource.getString
	 (ResourceConstants.RENAME), Command.ITEM, 7);
		
	/**
	 * Required for Sortable */
	public boolean lessthan(Sortable cmp) {
		if (!(cmp instanceof AMSCustomItem)) {
			return true; }
		return (String.valueOf(text).compareTo(String.valueOf(((AMSCustomItem)cmp).text))<0); }	

	/**
	* Cached truncation mark
	*/
	protected static final char truncationMark =
		Resource.getString(ResourceConstants.TRUNCATION_MARK).charAt(0);
		
	/** Cached width of the truncation mark */
	protected static int truncWidth=-1;
	
	static void initTruncWidth() {
		if (truncWidth != -1) {
			return; }
		truncWidth = ICON_FONT.charWidth(truncationMark); }

	static void initScrollTimer() {
		if (textScrollTimer == null) {
			textScrollTimer = new Timer(); } }
	
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
	* Gets the minimum width of a midlet representation in
	* the App Selector Screen.
	* @return the minimum width of a midlet representation
	*         in the App Selector Screen.
	*/
	protected int getMinContentWidth() {
		return owner.getWidth(); }
	
	/**
	* Gets the preferred width of a midlet representation in
	* the App Selector Screen based on the passed in height.
	* @param height the amount of height available for this Item
	* @return the minimum width of a midlet representation
	*         in the App Selector Screen.
	*/
	protected int getPrefContentWidth(int height) {
		return owner.getWidth(); }

	/**
	* Repaints MidletCustomItem. Called when internal state changes.
	*/
	public void update() {
		repaint(); }
		
	// Passthrough constructor
	AMSCustomItem(String s, AppManagerUI ams, int d) {
		super(s);
		depth=d;
		indent = d * INDENT_W;
		owner=ams;
		initTruncWidth();
		initScrollTimer();
		truncated = false;
		marked=false;
		xScrollOffset = 0;
		if (allowsMark()) {
			addCommand(markCmd); }
		setFixedCommands();
		setItemCommandListener(owner); }
		
	void setDepth(int d) {
		depth = d;
		indent = d * INDENT_W; }

	// Write to storage 
	abstract void write(DataOutputStream ostream) throws IOException;
	
	/** Whether this item is marked */
	boolean marked;

	  /** The width of the item */
	int width; // = 0
	/** The height of the item */
	int height; // = 0
	
	/** The text of the item */
	char[] text;
	/** Length of the text */
	int textLen;

	/** The owner of this MidletCustomItem */
	AppManagerUI owner; // = null

	/** current default command */
	Command default_command; // = null
	
	/** The depth in the tree of this item */
	int depth;
	
	/** The amount to indent this item (due to depth) */
	int indent;	
	
	/** A TimerTask which will repaint scrolling text on a repeated basis */
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
	protected static Timer textScrollTimer=null;
	
	/** Text auto-scrolling parameters */
	private static int SCROLL_RATE = 250;
	private static int SCROLL_DELAY = 500;
	private static int SCROLL_SPEED = 10;
	
	static void stopTimer() {
		if (textScrollTimer==null) {
			return; }
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
		
	/**
	* On size change event we define the item's text
	* according to item's new width
	* @param w The current width of this Item
	* @param h The current height of this Item
	*/
	protected void sizeChanged(int w, int h) {
		stopScroll();
		width = w;
		height = h;
		int widthForText = w - ITEM_PAD - ICON_BG.getWidth();
		int nameWidth = ICON_FONT.charsWidth(text, 0, textLen);
		scrollWidth = nameWidth - widthForText + w/5;
		truncated = nameWidth > widthForText; }

	/**
	* Handles traversal out. This method is called when this
	* MidletCustomItem looses focus.
	*/
	protected void traverseOut() {
		hasFocus = false;
		stopScroll(); }
		
	/**
	* Handles traversal.
	* @param dir The direction of traversal (Canvas.UP, Canvas.DOWN,
	*            Canvas.LEFT, Canvas.RIGHT)
	* @param viewportWidth The width of the viewport in the AppSelector
	* @param viewportHeight The height of the viewport in the AppSelector
	* @param visRect_inout The return array that tells AppSelector
	*        which portion of the MidletCustomItem has to be made visible
	* @return true if traversal was handled in this method
	*         (this MidletCustomItem just got focus or there was an
	*         internal traversal), otherwise false - to transfer focus
	*         to the next item
	*/
	protected boolean traverse(int dir,
		int viewportWidth, int viewportHeight,
		int visRect_inout[]) {
		// entirely visible and hasFocus
		if (!hasFocus) {
			hasFocus = true; }
		visRect_inout[0] = 0;
		visRect_inout[1] = 0;
		visRect_inout[2] = width;
		visRect_inout[3] = height;
		startScroll();
		return false; }

	/**
	* Paints the content of a midlet representation or folder in
	* the App Selector Screen.
	* Note that icon representing that foreground was requested
	* is painted on top of the existing icon.
	* @param g The graphics context where painting should be done
	* @param w The width available to this Item
	* @param h The height available to this Item
	*/
	protected void paint(Graphics g, int w, int h) {
		int cX = g.getClipX();
		int cY = g.getClipY();
		int cW = g.getClipWidth();
		int cH = g.getClipHeight();
		
		if ((cW + cX) > bgIconW) {
			if (text != null && h > ICON_FONT.getHeight()) {
				setLabelColor(g);
				g.setFont(ICON_FONT);
				boolean truncate = (xScrollOffset == 0) && truncated;
				g.clipRect(bgIconW + ITEM_PAD + indent, 0,
					truncate ? w - truncWidth - bgIconW - 2 * ITEM_PAD :
					w - bgIconW - 2 * ITEM_PAD, h);
				g.drawChars(text, 0, textLen,
					bgIconW + ITEM_PAD + indent + xScrollOffset, (h - ICON_FONT.getHeight())/2,
					Graphics.LEFT | Graphics.TOP);
				g.setClip(cX, cY, cW, cH);
				if (truncate) {
					g.drawChar(truncationMark, w - truncWidth,
						(h - ICON_FONT.getHeight())/2, Graphics.LEFT | Graphics.TOP); } } }
			
		if (cX < bgIconW) {
			g.clipRect(indent, 0, bgIconW, h);
			if (hasFocus) {
				g.drawImage(ICON_BG, 0 + indent, (h - bgIconH)/2,
					Graphics.TOP | Graphics.LEFT); }
			drawIcons(g); }
		if (marked) {
			g.setClip(cX, cY, cW, cH);
			g.drawImage(markedImg, w-markedImgW, h - markedImgH,
				Graphics.TOP | Graphics.LEFT); }
		g.setClip(cX, cY, cW, cH); }

	/* Override--different implementation for folders and midlets */
	abstract void setLabelColor(Graphics g);

	/* Override--different implementation for folders and midlets */
	abstract void drawIcons(Graphics g);
	
	/* Override--call to add/remove commands according to current state */
	abstract void updateCommands();
		
	void mark() {
		marked=true; }

	void unMark() {
		marked=false; }

	/* General call -- calls all the display update code */
	void updateDisplay() {
		setBaseCommands();
		updateCommands(); }
	
	void setBaseCommands() {
		if (!allowsMark()) {
			return; }
		if (marked) {
			removeCommand(markCmd);
			addCommand(unMarkCmd);
			return; }
		removeCommand(unMarkCmd);
		addCommand(markCmd); }

	boolean allowsMark() {
		return true; }
		
	void setFixedCommands() {
		addCommand(renameCmd); }
		
	void setText(String s) {
		text = s.toCharArray();
		textLen = text.length; }
		
	/* Hide this item in the AMS UI */
	void hide() {
		int idx = owner.getIndexOf(this);
		if (idx==-1) {
			return; }
		owner.delete(idx); }
		
}
