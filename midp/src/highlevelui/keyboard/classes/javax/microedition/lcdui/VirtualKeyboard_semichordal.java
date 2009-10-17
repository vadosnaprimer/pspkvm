/*
 * /AJ Milne--custom semichordal version for PSP platform
 */

package javax.microedition.lcdui;

import com.sun.midp.lcdui.*;
import com.sun.midp.configurator.Constants;
import com.pspkvm.keypad.PSPCtrlCodes;
import com.pspkvm.system.VMSettings;
import com.sun.midp.installer.GraphicalInstaller;

/**
 * Semichordal virtual keyboard, specific to PSP platform, for PSPKVM.
 * @author AJ Milne.
 *  
 * NOTE: The semichordal board is dependant on being able to receive raw key
 * events specific to the PSPKVM virtual machine. Midlet implementations
 * creating and calling this class directly must subclass CLayer and must
 * return true to supportsRawKeyInput() within that subclass in order to
 * receive the raw events in their keyInput(...) method, and should then send
 * raw events to the semichordal board's processRawStroke(...) method. Doing
 * this will result in calls being returned to the virtual keyboard listener's
 * virtualKeyEntered(...) and virtualMetaKeyEntered(...) methods, as with
 * 'reference' virtual boards. See KeyboardLayer_semichordal.java for a
 * typical implementation of this nature. 
 */

class VirtualKeyboard_semichordal extends VirtualKeyboardInterface {

    /** instance of the virtual keyboard listener */
    VirtualKeyboardListener vkl;
    
    // Chordal display state, transient entry state
    int display_chords_mode;
    boolean caps_lock_set, select_on;
		boolean ls_set, rs_set;
    int chordal_offset;
		int live_dpad;
		int symbolDownLast = 0;
		int chordDownLast = 0;
		boolean analog_cursor = false;

		// Display mode enum (for display_chords_mode)
    private final static int last_display_chords_mode = 2;
		private final static int NO_DISP = 0;
		private final static int SM_DISP = 1;
		private final static int LG_DISP = 2;
		
		// Keyboard state--may not need
    int currentKeyboard = 1; // abc
    
    // The board images
    Image c_lock_img, key_bg_img, sel_img,
    	roman_img, greek_img, cyrillic_img, symbols_img;
    // Ordered array of the regular map images--to match
		// the order of mapset
    Image[] map_imgs;
    // Utility font (NB: DO *NOT* call setFont with this 
		// as an argument--use drawUtilityString instead--this
		// is to avoid confusing client code which might get 
		// back a font from getFont() it does not recognize.
		// This object is just used for sizing calls.)
    static final Font utility_font =
			Font.getFont(Font.FACE_UTILITY, Font.STYLE_BOLD, Font.SIZE_SMALL);
		// Colors for drawing the key glyphs
		static final int FONT_BLUE = 0x4040c0;
		static final int FONT_RED = 0xc04040;
		
    // Useful field
    /** Union of all chordal keys */
		static final int CHORDAL_KEYS = PSPCtrlCodes.LTRIGGER | PSPCtrlCodes.RTRIGGER
			| PSPCtrlCodes.UP | PSPCtrlCodes.RIGHT | PSPCtrlCodes.DOWN | PSPCtrlCodes.LEFT;
			
		// The keymaps, including the pointer to the crt_map.
		SC_Keymap crt_map;
		int crt_map_idx;
		// Whether the transient symbols set is up,
		// and whether the symbols map is up.
		boolean symbols_transient, symbols_up;

		final static SC_Keymap roman_map = new SC_Keymap_Roman();
		final static SC_Keymap cyrillic_map = new SC_Keymap_Cyrillic();
		final static SC_Keymap greek_map = new SC_Keymap_Greek();
		final static SC_Keymap[] mapset = {
			roman_map, cyrillic_map, greek_map };
		// Indexes to the maps
		final static int ROMAN_MAP = 0;
		final static int CYRILLIC_MAP = 1;
		final static int GREEK_MAP = 2;
		// The extension/symbol maps--you don't rotate through these,
		// but 'shift' up to them from the corresponding root boards
		final static SC_Keymap roman_sym_map = new SC_Keymap_NumericSymbolic_Roman();

		// Called whenever the keyboard sends 'SWM' (Switch Map)
		void rotate_map() {
			crt_map_idx++;
			if (crt_map_idx >= mapset.length) {
				crt_map_idx = 0; }
			crt_map = mapset[crt_map_idx]; } 
			
		// Called to get the current keymap's name
		String getMapSName() {
			return crt_map.getMapNameShort(); }

		// Called whenever the keyboard sends 'SLK' (Symbol lock)
		void lock_symbols() {
			// TODO: Handle Cyrillic, Greek symbol maps (when we've got these)
			crt_map = roman_sym_map;
			symbols_up = true; 
			symbols_transient=false; }

		// Called whenever the keyboard sends 'SYM' (Transient symbol set)
		void set_symbols_transient() {
			// TODO: Handle Cyrillic, Greek symbol maps (when we've got these)
			crt_map = roman_sym_map;
			symbols_up = true;
			symbols_transient=true; }

		// Called whenever the keyboard sends 'CNC' (Symbol lock)
		void cancel_symbols() {
			symbols_transient=false;
			symbols_up = false;
			crt_map = mapset[crt_map_idx];; }

		// Called (from w/i the board) on all character emits--
		// makes sure the transient board gets killed properly
		void checkTransientCancel(int o) {
			if (!symbols_transient) {
				return; }
			if (!crt_map.cancelsTransient(o)) {
				return; }
			// Symbols board was transient, and the user entered
			// a stroke considered significant enough to cancel it
			// (excludes cursor, display control). Kill the transient board.
			cancel_symbols();
			vkl.repaintVK(); }
			
    /**
     * Virtual Keyboard constructor.
     * 
     * @param vkl the virtual keyboard listener
     * @param displayTextArea flag to indicate whether to display the text area
     */
    public VirtualKeyboard_semichordal(VirtualKeyboardListener vkl,
                           int neededColumns, int neededRows) throws VirtualKeyboardException {
       if ("on".equals(VMSettings.get("com.pspkvm.virtualkeyboard.autoopen"))) {
           USE_VIRTUAL_KEYBOARD_OPEN_AUTO = true;
       }

       String im = VMSettings.get("com.pspkvm.inputmethod");
       if(im != null && im.equals("osk")){
           USE_VIRTUAL_KEYBOARD = false;
           USE_VIRTUAL_KEYBOARD_OPEN_AUTO = false;
           return; }
           
    initDisplayVars();
		currentKeyboard = 0;
    this.vkl = vkl;
		currentKeyboard = 0;
		setup_keymaps(); }
		
	void setup_keymaps() {
		crt_map_idx=ROMAN_MAP;
		String imap = VMSettings.get("com.pspkvm.virtual_keyboard.default_keymap");
    if((imap != null) && (imap.equals("cyrillic"))) {
    	crt_map_idx=CYRILLIC_MAP; }
    if((imap != null) && (imap.equals("greek"))) {
    	crt_map_idx=GREEK_MAP; }
		crt_map = mapset[crt_map_idx];
		symbols_transient=false; }

	/**
	 *	Construct images, soft fonts from the bitstreams in the aux classes
	 */	 	
	void constructImages() {
		c_lock_img = LongArrayHandler.createImage(Imgs_misc.c_lock_seg,
			Imgs_misc.c_lock_segpad);
		sel_img = LongArrayHandler.createImage(Imgs_misc.sel_seg,
			Imgs_misc.sel_segpad);
		key_bg_img = LongArrayHandler.createImage(Imgs_misc.psp_keys_off_seg,
			Imgs_misc.psp_keys_off_segpad);
		roman_img = LongArrayHandler.createImage(Imgs_misc.roman_seg,
			Imgs_misc.roman_segpad);
		greek_img = LongArrayHandler.createImage(Imgs_misc.greek_seg,
			Imgs_misc.greek_segpad);
		cyrillic_img = LongArrayHandler.createImage(Imgs_misc.cyrillic_seg,
			Imgs_misc.cyrillic_segpad);
		symbols_img = LongArrayHandler.createImage(Imgs_misc.symbols_seg,
			Imgs_misc.symbols_segpad);
		// Order of this array must match that of mapset
		map_imgs = new Image[3];
		map_imgs[0] = roman_img;
		map_imgs[1] = cyrillic_img;
		map_imgs[2] = greek_img;
		kheight = key_bg_img.getHeight();
		kwidth = key_bg_img.getWidth(); }

	int kwidth, kheight;

	// Initialize the various display variables--called at construction
	void initDisplayVars() {
		if(VMSettings.get("com.pspkvm.virtualkeyboard.direction").equals("on")){
			analog_cursor=true; }
		chordal_offset=0;
		display_chords_mode=SM_DISP;
		ls_set=false;
		rs_set=false;
		select_on=false;
		caps_lock_set=false;
		live_dpad=0;
		constructImages(); }

  public VirtualKeyboard_semichordal(int kbtype,
		  	VirtualKeyboardListener vkl,
		int w, int h) throws VirtualKeyboardException {
    if ("on".equals(VMSettings.get("com.pspkvm.virtualkeyboard.autoopen"))) {
           USE_VIRTUAL_KEYBOARD_OPEN_AUTO = true;
       }

    String im = VMSettings.get("com.pspkvm.inputmethod");
    if(im != null && im.equals("osk")){
		   USE_VIRTUAL_KEYBOARD = false;
		   USE_VIRTUAL_KEYBOARD_OPEN_AUTO = false;
		   return; }
		   
		initDisplayVars();
		setup_keymaps();

		this.vkl = vkl;	}
		
		/**
		 *	Toggle display of chords
		 *			 
		 *	@ret Whether this leaves the layer visible.		 
		 */
		public boolean toggleDisplayChords() {
			display_chords_mode ++;
			if (display_chords_mode > last_display_chords_mode) {
				display_chords_mode = 0;
				return false; }
			return true; }
			
			/**
			 * Set large chord display.
			 * Called from the keyboard layer handler when it detects its handling
			 * a multiline textfield.			 
			 */
		public void setLargeDisplay() {
			display_chords_mode = LG_DISP; }			 			

		// Raw offsets into chord table, by DPAD offset, starting at centre, then right, then
		// counter clockwise
		static final int[] offsets  =  { 0, 48, 128, 32, 80, 16, 112, 64, 96 };
		 
	 	// Letter offsets within key displays
		private final static int[] ltr_x = { 0, -12, 0, 12 };
		private final static int[] ltr_y = { 0, 12, 24, 12 };
		// Special x for the single-chord display
		private final static int[] ltr_x_wd = { 0, -15, 0, 15 };
		// Key offsets from origin
		private final static int[] key_x = {
			1, 2, 2, 1, 0, 0, 0, 1, 2 };
		private final static int[] key_y = {
			1, 1, 0, 0, 0, 1, 2, 2, 2 };
			
		final static int GRPWIDTH = 44;
		final static int HGRPWIDTH = 22;
		final static int GRPHEIGHT = 42;
		final static int GAPSIZE = 2;

		/**
		 * Method called to paint the 'lg' or halfboard display, when in that mode.
		 * 
		 * @param g -- the graphics context		 		 		
		 */	
		protected void paintLgDisplay(Graphics g) {
			g.setColor(BGCOLOR);
			g.fillRect(0, 0, getWidth(), getHeight());
			g.setColor(DKRED);
			g.fillRect(0, getHeight()-c_lock_img.getHeight(),
				getWidth(), c_lock_img.getHeight()+1);
			for (int idx=0; idx<9; idx++) {
				g.setColor(idx==live_dpad ? DKRED : DKBLUE);
				g.fillRect(key_x[idx]*(GRPWIDTH+GAPSIZE),
					key_y[idx]*(GRPHEIGHT+GAPSIZE),
					GRPWIDTH, GRPHEIGHT); }
			// Draw keys
			int ls_offset = ls_set ? 8 : 0;
			int rs_os = rs_set ? 4 : 0;
			g.setColor(WHITE);
			for (int idx=0; idx<9; idx++) {
				paintChord(g, 
					HGRPWIDTH+(key_x[idx]*(GRPWIDTH+GAPSIZE)), 1+key_y[idx]*(GRPHEIGHT+GAPSIZE),
					offsets[idx]+ls_offset+rs_os); }
			paintMiscState(g); }

	  /**
	   * paint a single key
	   * 
	   * @param g The graphics context to which to paint
	   * @param x X coordinate of the string (center)
	   * @param y Y coordinate of the string (top)
	   * @param o The offset into the chord of the key
	   */
		void paintIcon(Graphics g, int x, int y, int o, boolean active) {
			String s=crt_map.getDisplayString(caps_lock_set, o);
			g.setColor(active ? FONT_RED : FONT_BLUE );
			int offset = utility_font.stringWidth(s)/2;
			g.drawUtilityString(s, x-offset, y, g.TOP|g.LEFT); }

		void paintIcon(Graphics g, int x, int y, int o) {
			String s=crt_map.getDisplayString(caps_lock_set, o);
			int offset = utility_font.stringWidth(s)/2;
			g.drawUtilityString(s, x-offset, y, g.TOP|g.LEFT); }

		/** Paint the 'simple' unstacked chord displays, in the small graphics context
		 * @param g the graphics context
		 * @param f the soft font
		 * @param x the x coordinate
		 * @param y the y coordinate
		 * @param offset the base offset of the chord
		 */		 		 		 		 		 		 		
		void paintChord(Graphics g, int x, int y, int offset, boolean active) {
			paintIcon(g, x+ltr_x_wd[0], y+ltr_y[0], offset+0, active);
			paintIcon(g, x+ltr_x_wd[1], y+ltr_y[1], offset+1, active);
			paintIcon(g, x+ltr_x_wd[2], y+ltr_y[2], offset+2, active);
			paintIcon(g, x+ltr_x_wd[3], y+ltr_y[3], offset+3, active); }

		void paintChord(Graphics g, int x, int y, int offset) {
			paintIcon(g, x+ltr_x[0], y+ltr_y[0], offset+0);
			paintIcon(g, x+ltr_x[1], y+ltr_y[1], offset+1);
			paintIcon(g, x+ltr_x[2], y+ltr_y[2], offset+2);
			paintIcon(g, x+ltr_x[3], y+ltr_y[3], offset+3); }

    /**
     * paint the virtual keyboard on the screen
     * 
     * @param g The graphics context to paint to
     */
		protected void paint(Graphics g) {
			switch(display_chords_mode) {
				case NO_DISP: return;
				case SM_DISP: paintCurrentChord(g); paintMiscState(g); return;
				case LG_DISP: 
					paintLgDisplay(g);
					return;
				default:
					// Really shouldn't happen
					return; } }
					
		int getWidth() {
			switch(display_chords_mode) {
				case NO_DISP: return 1;
				case SM_DISP: return key_bg_img.getWidth()+8;
				case LG_DISP: default: return (GRPWIDTH*3)+(GAPSIZE*2); } }
			
		int getHeight() {
			switch(display_chords_mode) {
				case NO_DISP: return 1;
				case SM_DISP: return key_bg_img.getHeight()+4+GAPSIZE+c_lock_img.getHeight();
				case LG_DISP: default: return (GRPHEIGHT*3)+(GAPSIZE*3)+c_lock_img.getHeight(); } }
			
		/**
		 * Display caps lock state -- just paint one image or another
		 * @param g the graphics object passed into the paint method
		 */		 		 		
		void paintCapsLockState(Graphics g) {
			if (!caps_lock_set) {
				return; }
			g.drawImage(c_lock_img ,
				getWidth()-c_lock_img.getWidth()-1,
				getHeight()-c_lock_img.getHeight(),
				g.LEFT|g.TOP); }
				
		/**
		 * Display map state -- just paint one image or another
		 * @param g the graphics object passed into the paint method
		 */		 		 		
		void paintKeymapState(Graphics g) {
			g.drawImage(symbols_up ? symbols_img : map_imgs[crt_map_idx],
				0,
				getHeight()-c_lock_img.getHeight(),
				g.LEFT|g.TOP); }
				
		/**
		 * Display the selection state
		 * @param g the graphics object passed into the paint method
		 */
		void paintSelectionState(Graphics g) {
			if (!select_on) {
				return; }
			g.drawImage(sel_img,
				getWidth()-c_lock_img.getWidth()-sel_img.getWidth()-1,
				getHeight()-sel_img.getHeight(),
				g.LEFT|g.TOP); }

		/**
		 * Display misc state info
		 * @param g the graphics object passed into the paint method
		 */
		void paintMiscState(Graphics g) {
			paintCapsLockState(g);
			paintSelectionState(g);
			paintKeymapState(g); }		 		
		
    /**
     * draw the keys the current chord makes available
     * 
     */
    protected void paintCurrentChord(Graphics g) {
    	int h = getHeight();
    	int y = 0;
    	int w = getWidth();
    	int x = 0;
    	g.setColor(WHITE);
    	g.fillRect(x, y, w, h);
			g.setColor(BLACK);
    	g.drawRect(x, y, w, h);
    	y+=3;
    	x+=4;
    	g.drawImage(key_bg_img, x, y, g.TOP|g.LEFT);
    	x += (key_bg_img.getWidth()/2);
    	y+=6;
    	paintChord(g, x, y, chordal_offset, true); }

		/**
		 *	Translate the control state into a DPAD direction for display
		 */		 		 		
		int getLiveDPAD(int p) {
			p = (p & PSPCtrlCodes.DPAD_MASK);
			switch (p) {
				case 0 : return 0;
				case (PSPCtrlCodes.DOWN | PSPCtrlCodes.LEFT): return 6;
				case (PSPCtrlCodes.UP | PSPCtrlCodes.RIGHT): return 2;
				case PSPCtrlCodes.LEFT: return 5;
				case PSPCtrlCodes.UP: return 3;
				case PSPCtrlCodes.RIGHT: return 1;
				case PSPCtrlCodes.DOWN: return 7;
				case (PSPCtrlCodes.UP | PSPCtrlCodes.LEFT): return 4;
				case (PSPCtrlCodes.DOWN | PSPCtrlCodes.RIGHT): return 8;
				default: return 0; } }
		
		/**
		 *	Helper for setDisplayState()		
		 *	Determines if repaint is needed 
		 */		 		 
		protected boolean detectPaintReq(int lc_ch_offset,
			boolean lc_ls_set, boolean lc_rs_set,
			int lc_live_dpad) {
			switch(display_chords_mode) {
				case NO_DISP: 
					return false;
				case SM_DISP:
					return lc_ch_offset != chordal_offset;
				case LG_DISP:
					if (lc_ls_set != ls_set) {
						return true; }
					if (lc_rs_set != rs_set) {
						return true; }
					return (lc_live_dpad != live_dpad);
				default:
					return false; } }

    /**
     * Set the current display state.
     *
     * @param offset -- the current offset into the chord table
     * @param p the raw control state
     * @param caps_lock whether caps lock is set
     * 		      
     * @return true if requires a repaint 
     */
		protected boolean setDisplayState(int lc_ch_offset, int p) {
			boolean lc_ls_set = (p & PSPCtrlCodes.LTRIGGER)!=0;
			boolean lc_rs_set = (p & PSPCtrlCodes.RTRIGGER)!=0;
			int lc_live_dpad = getLiveDPAD(p);
			boolean paint_req = detectPaintReq(lc_ch_offset, lc_ls_set, lc_rs_set,
				lc_live_dpad);
			chordal_offset=lc_ch_offset;
			ls_set=lc_ls_set;
			rs_set=lc_rs_set;
			live_dpad=lc_live_dpad;
			return paint_req; }

	private static final int WHITE = 0xffffff;
	private static final int BLACK = 0x000000;
	private static final int DKBLUE = 0x000040;
	private static final int DKRED = 0x400000;
	private static final int GREY = 0xc0c0c0;
	private static final int BGCOLOR = WHITE;

  /**
   *	Call this with incoming standard key events. Currently,
   *	it just processes analog stick commands, since these are the
   *	only meaningful standard events not handled by the popup menus.   
   */
   public void processStandardKeyEvent(int type, int code) {
		if (!analog_cursor) {
			return; }
   	if (type == EventConstants.RELEASED) {
		 	// Ignore
			 return; }
		switch (code) {
			// NB: Not quite sure how the DPAD keys get filtered out
			// in this.
			case Constants.KEYCODE_LEFT:
				// Analog left
				vkl.virtualMetaKeyEntered(SC_Keys.CLF);
				return; 
			case Constants.KEYCODE_RIGHT:
				// Analog right
				vkl.virtualMetaKeyEntered(SC_Keys.CRT);
				return;
			case Constants.KEYCODE_UP:
				// Analog up
				vkl.virtualMetaKeyEntered(SC_Keys.CUP);
				return;
			case Constants.KEYCODE_DOWN:
				// Analog down
				vkl.virtualMetaKeyEntered(SC_Keys.CDN);
				return; } }

	/**
	 *
	 * Call this with the incoming raw stroke--board will process and return
	 * (if appropriate) the processed stroke in its listener's
	 * virtualKeyEntered(...) or virtualMetaKeyEntered(...)
	 * 
	 * @param p a raw stroke received in a keyInput(...) method	 	 
	 */
    public void processRawStroke(int p) {
			updateChordMap(p);
			int chordDownNow = p & CHORDAL_KEYS;
			int symbolDownNow = p & PSPCtrlCodes.SYMBOLS;
			boolean process_stroke = true;
			if ((chordDownNow != chordDownLast) && (symbolDownNow == symbolDownLast)) {
				// They're just shifting on the chordal keys. Ignore.
				process_stroke = false; }
			if (symbolDownNow == 0) {
				// Upstroke
				process_stroke = false; }
			// Reset previous state
			chordDownLast = chordDownNow;
			symbolDownLast = symbolDownNow;
			if (!process_stroke) {
				// Nothing to process.
				return; }
			// Meaningful stroke. Process it.
			int offset = getCharOffset(p);
			if (crt_map.isChar(offset)) {
				vkl.virtualKeyEntered(EventConstants.PRESSED,
					crt_map.getOutputChar(caps_lock_set, offset));
				checkTransientCancel(offset);
				return; }
			if (crt_map.isMeta(offset)) {
				boolean old_symbols_transient = symbols_transient; 
				vkl.virtualMetaKeyEntered(crt_map.getMetaKey(offset));
				if (old_symbols_transient) {
					checkTransientCancel(offset); } } }

    /**
     * Update the chord map displayed
     */     
    void updateChordMap(int p) {
			int r = getChordOffset(p);
			boolean shift_set = ((p & PSPCtrlCodes.RTRIGGER)!=0);
			if (shift_set) {
				r += 4; }
			if (setDisplayState(r, p)) {
				vkl.repaintVK(); } }

		// Translate chordal downstroke to key
		int getCharOffset(int p) {
			int r = getChordOffset(p);
			boolean shift_set = ((p & PSPCtrlCodes.RTRIGGER)!=0);
			if (shift_set) {
				r += 4; }
			if ((p & PSPCtrlCodes.TRIANGLE)!=0) {
				return r; }
			if ((p & PSPCtrlCodes.SQUARE)!=0) {
				return r+1; }
			if ((p & PSPCtrlCodes.CROSS)!=0) {
				return r+2; }
			return r+3; }
			
		// Translate the DPAD state into a table offset
    int getDPadOffset(int p) {
			// Mask out everything but the dpad
			p = p & PSPCtrlCodes.DPAD_MASK;
			switch (p) {
				case 0 : return 0;
				case (PSPCtrlCodes.DOWN | PSPCtrlCodes.LEFT): return 112;
				case (PSPCtrlCodes.UP | PSPCtrlCodes.RIGHT): return 128;
				case PSPCtrlCodes.LEFT: return 16;
				case PSPCtrlCodes.UP: return 32;
				case PSPCtrlCodes.RIGHT: return 48;
				case PSPCtrlCodes.DOWN: return 64;
				case (PSPCtrlCodes.UP | PSPCtrlCodes.LEFT): return 80;
				case (PSPCtrlCodes.DOWN | PSPCtrlCodes.RIGHT): return 96;
				default: return 0; } }
				
		// Get the current left-hand chord offset value
		int getChordOffset(int p) {
			int r = getDPadOffset(p);
			if ((p & PSPCtrlCodes.LTRIGGER)!=0) { r+= 8; }
			return r; }

}
