/*
 * /AJ Milne--custom semichordal version for PSP platform
 */

package javax.microedition.lcdui;

import com.sun.midp.lcdui.*;
import com.sun.midp.configurator.Constants;
import com.sun.midp.main.Configuration;

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
    Image c_lock_img, key_bg_img, key_bg_img_on, sel_img;
    // The soft fonts
    SFont sfont_red, sfont_blue;

    /**
     * Virtual Keyboard constructor.
     * 
     * @param vkl the virtual keyboard listener
     * @param displayTextArea flag to indicate whether to display the text area
     */
    public VirtualKeyboard_semichordal(VirtualKeyboardListener vkl,
                           int neededColumns, int neededRows) throws VirtualKeyboardException {
       if ("true".equals(Configuration.getProperty("com.pspkvm.virtualkeyboard.autoopen"))) {
           USE_VIRTUAL_KEYBOARD_OPEN_AUTO = true;
       }

       String im = Configuration.getProperty("com.pspkvm.inputmethod");
       if(im != null && im.equals("sony-osk")){
           USE_VIRTUAL_KEYBOARD = false;
           USE_VIRTUAL_KEYBOARD_OPEN_AUTO = false;
           return; }
           
    initDisplayVars();    
		currentKeyboard = 0;
    this.vkl = vkl;
		currentKeyboard = 0; }
		
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
		key_bg_img_on = LongArrayHandler.createImage(Imgs_misc.psp_keys_on_seg,
			Imgs_misc.psp_keys_on_segpad);
		sfont_blue = new SFont();
		sfont_red = new SFont();
		SFontInit_blue.initFont(sfont_blue.imgs);
		SFontInit_red.initFont(sfont_red.imgs); }

	// Initialize the various display variables--called at construction
	void initDisplayVars() {
		if(Configuration.getProperty("com.pspkvm.virtualkeyboard.direction").equals("true")){
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
		if ("true".equals(Configuration.getProperty("com.pspkvm.virtualkeyboard.autoopen"))) {
		   USE_VIRTUAL_KEYBOARD_OPEN_AUTO = true; }
		
		String im = Configuration.getProperty("com.pspkvm.inputmethod");
		if(im != null && im.equals("sony-osk")){
		   USE_VIRTUAL_KEYBOARD = false;
		   USE_VIRTUAL_KEYBOARD_OPEN_AUTO = false;
		   return; }
		   
		initDisplayVars();
				
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
			
		final static int lg_dim_width = 210;
		final static int lg_dim_height = 150;
		/**
		 * Method called to paint the 'lg' or halfboard display, when in that mode.
		 * 
		 * @param g -- the graphics context		 		 		
		 */		
		protected void paintLgDisplay(Graphics g) {
			g.setColor(WHITE);
			g.fillRect(0, 0, lg_dim_width, lg_dim_height);
			for (int idx=0; idx<9; idx++) {
				g.drawImage(idx==live_dpad ? key_bg_img_on : key_bg_img,
					keypad_posn_x[idx], keypad_posn_y[idx], g.TOP|g.LEFT); }
			// Draw keys
			int ls_offset = ls_set ? 8 : 0;
			for (int idx=0; idx<9; idx++) {
				paintChordStacked(g, (idx==live_dpad) && (!rs_set) ? sfont_red : sfont_blue,
					keypad_posn_x[idx], keypad_posn_y[idx], offsets[idx]+ls_offset);
				paintChordStacked(g, (idx==live_dpad) && (rs_set) ? sfont_red : sfont_blue,
					keypad_posn_x[idx], keypad_posn_y[idx]-9, offsets[idx]+ls_offset+4); }
			g.setColor(BLK);
			g.drawRect(0, 0, lg_dim_width, lg_dim_height);
			paintMiscState(g); }

		/**
		 *	Method called to paint a key in a 'stacked' array--in the large graphics context
		 *	Logic is involved to avoid painting duplicate keys when they're the same shifted and
		 *	not, and for alpha characters, where the shift is pretty obvious, and doesn't need
		 *	to be displayed at the same time.
		 *	
		 * @param g the graphics context
		 * @param f the soft font to use
		 * @param x the x coordinate
		 * @param y the y coordinate
		 * @param o the offset into the key array		 		 		 		 		 		 		 
		 */
		void paintKeyStacked(Graphics g, SFont f, int x, int y, int o) {
			if (SC_Keys.ls_matched_meta[o]) {
				if (rs_set) {
					return; }
				y-=4; }
			else if (SC_Keys.us_matched_meta[o]) {
				if (!rs_set) {
					return; }
				y+=5; }
			else if (SC_Keys.isLCAlpha(caps_lock_set, o)) {
				if (rs_set ^ caps_lock_set) {
					return; }
				// !(rs_set ^ caps_lock_set). UC won't be printed.
				if (caps_lock_set) {
					y+=5; }
				else {
					y-=4; } }
			else if (SC_Keys.isUCAlpha(caps_lock_set, o)) {
				if (!(rs_set ^ caps_lock_set)) {
					return; }
				// (rs_set ^ caps_lock_set). LC won't be printed.
				if (caps_lock_set) {
					y-=4; }
				else {
					y+=5; } }
			String s=SC_Keys.getChordalMapDisplay(caps_lock_set)[o];
			int offset = sfont_blue.stringWidth(s)/2;
			f.drawString(g, x-offset, y, s); }
			
		/** Paint the 'stacked' chord displays in the large graphics context
		 * Most of the logic is in paintKeyStacked
		 * @param g the graphics context
		 * @param f the soft font
		 * @param x the x coordinate
		 * @param y the y coordinate
		 * @param offset the base offset of the chord
		 */
		void paintChordStacked(Graphics g, SFont f, int x, int y, int offset) {
			x+= 28;
			y+= 10;
			paintKeyStacked(g, f, x+ltr_x[0], y+ltr_y[0], offset+0);
			paintKeyStacked(g, f, x+ltr_x[1], y+ltr_y[1], offset+1);
			paintKeyStacked(g, f, x+ltr_x[2], y+ltr_y[2], offset+2);
			paintKeyStacked(g, f, x+ltr_x[3], y+ltr_y[3], offset+3); }

		/** Paint the 'simple' unstacked chord displays, in the small graphics context
		 * @param g the graphics context
		 * @param f the soft font
		 * @param x the x coordinate
		 * @param y the y coordinate
		 * @param offset the base offset of the chord
		 */		 		 		 		 		 		 		
		void paintChord(Graphics g, SFont f, int x, int y, int offset) {
			paintKey(g, f, x+ltr_x[0], y+ltr_y[0], offset+0);
			paintKey(g, f, x+ltr_x[1], y+ltr_y[1], offset+1);
			paintKey(g, f, x+ltr_x[2], y+ltr_y[2], offset+2);
			paintKey(g, f, x+ltr_x[3], y+ltr_y[3], offset+3); }

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
			return lg_dim_width+1; }
			
		int getHeight() {
			return lg_dim_height+1; }
			
	  /**
	   * paint a single key from the live chord
	   * 
	   * @param g The graphics context to which to paint
	   * @param x X coordinate of the string (center)
	   * @param y Y coordinate of the string (top)
	   * @param o The offset into the chord of the key
	   */
		void paintKey(Graphics g, SFont f, int x, int y, int o) {
			String s=SC_Keys.getChordalMapDisplay(caps_lock_set)[o];
			int offset = sfont_blue.stringWidth(s)/2;
			f.drawString(g, x-offset, y, s); }
		
		/**
		 * Display caps lock state -- just paint one image or another
		 * @param g the graphics object passed into the paint method
		 */		 		 		
		void paintCapsLockState(Graphics g) {
			if (!caps_lock_set) {
				return; }
			g.drawImage(c_lock_img ,
				getWidth()-c_lock_img.getWidth()-1,
				getHeight()-c_lock_img.getHeight()-1,
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
				getHeight()-sel_img.getHeight()-1,
				g.LEFT|g.TOP); }

		/**
		 * Display misc state info
		 * @param g the graphics object passed into the paint method
		 */
		void paintMiscState(Graphics g) {
			paintCapsLockState(g);
			paintSelectionState(g); }		 		

		// TODO: Allow them to toggle it up to TL
		
		// Letter positions within chord displays
		private final static int IMGPAD = 1;
		private final static int[] ltr_x = { 0, -17, 0, 17 };
		private final static int[] ltr_y = { 0, 12, 24, 12 };
		// Keypad positions in the main display (generated)
		final static int[] keypad_posn_x = { 76, 153, 128, 76, 25, 1, 25, 76, 128 };
		final static int[] keypad_posn_y = { 53, 53, 16, 2, 16, 53, 89, 103, 89 };
		// Raw offsets into chord table, by DPAD offset, starting at centre, then right, then
		// counter clockwise
		static final int[] offsets  =  { 0, 48, 128, 32, 80, 16, 112, 64, 96 };

    /**
     * draw the keys the current chord makes available
     * 
     */
    protected void paintCurrentChord(Graphics g) {
    	int h = key_bg_img.getHeight()+4+IMGPAD+c_lock_img.getHeight();
    	int y = getHeight()-h-1;
    	int w = key_bg_img.getWidth()+8;
    	int x = getWidth()-w-1;
    	g.setColor(WHITE);
    	g.fillRect(x, y, w, h);
			g.setColor(BLK);
    	g.drawRect(x, y, w, h);
    	y+=3;
    	x+=4;
    	g.drawImage(key_bg_img, x, y, g.TOP|g.LEFT);
    	x += (key_bg_img.getWidth()/2);
    	y+=6;
    	paintChord(g, sfont_blue, x, y, chordal_offset); }
			
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

    // ********* attributes ********* //

    private final static int WHITE = 0xffffff;
    private final static int BLK = 0x000000;
    
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
			int chordDownNow = p & PSPCtrlCodes.CHORDAL_KEYS;
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
			if (SC_Keys.isChar(offset)) {
				vkl.virtualKeyEntered(EventConstants.PRESSED,
					SC_Keys.getChordalMapChars(caps_lock_set)[offset]);
				return; }
			if (SC_Keys.isMeta(offset)) {
				vkl.virtualMetaKeyEntered(SC_Keys.chordal_map_meta[offset]); } }

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
