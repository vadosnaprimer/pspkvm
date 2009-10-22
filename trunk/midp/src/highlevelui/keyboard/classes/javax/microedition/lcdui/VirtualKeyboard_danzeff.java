/*
 * /AJ Milne--Danzeff clone for PSP platform
 */

package javax.microedition.lcdui;

import com.sun.midp.lcdui.*;
import com.sun.midp.configurator.Constants;
import com.pspkvm.keypad.PSPCtrlCodes;
import com.pspkvm.system.VMSettings;

/**
 * Semichordal virtual keyboard, specific to PSP platform, for PSPKVM.
 * @author AJ Milne.
 *  
 * NOTE: The danzeff board is dependant on being able to receive raw key
 * events specific to the PSPKVM virtual machine. Midlet implementations
 * creating and calling this class directly must subclass CLayer and must
 * return true to supportsRawKeyInput() within that subclass in order to
 * receive the raw events in their keyInput(...) method, and should then send
 * raw events to the danzeff board's processRawStroke(...) method. Doing
 * this will result in calls being returned to the virtual keyboard listener's
 * virtualKeyEntered(...) and virtualMetaKeyEntered(...) methods, as with
 * 'reference' virtual boards. See KeyboardLayer_danzeff.java for a
 * typical implementation of this nature. 
 */

class VirtualKeyboard_danzeff extends VirtualKeyboardInterface {

    /** instance of the virtual keyboard listener */
    VirtualKeyboardListener vkl;
    
    // Select state
    boolean select_on;

		// Keyboard state--may not need
    int currentKeyboard = 1; // abc
    // Current map
    DZ_Keymap crt_map;
    
    // We just have the one keymap right now
		void setupKeymaps() {
			crt_map = new DZ_Keymap_Roman(); }
    
    /**
     * Virtual Keyboard constructor.
     * 
     * @param vkl the virtual keyboard listener
     * @param displayTextArea flag to indicate whether to display the text area
     */
    public VirtualKeyboard_danzeff(VirtualKeyboardListener vkl,
                           int neededColumns, int neededRows) throws VirtualKeyboardException {
       if ("on".equals(VMSettings.get("com.pspkvm.virtualkeyboard.autoopen"))) {
           USE_VIRTUAL_KEYBOARD_OPEN_AUTO = true;
       }
    setupKeymaps();
		live_key = 0;
		cboard = 0;           
		select_on = false;
		lshift = false;
		constructImages();
    this.vkl = vkl; }

  public VirtualKeyboard_danzeff(int kbtype,
		  	VirtualKeyboardListener vkl,
		int w, int h) throws VirtualKeyboardException {
    if ("on".equals(VMSettings.get("com.pspkvm.virtualkeyboard.autoopen"))) {
           USE_VIRTUAL_KEYBOARD_OPEN_AUTO = true;
       }
    setupKeymaps();
		live_key = 0;
		cboard = 0;
		select_on = false;
		lshift = false;
		constructImages();         
		this.vkl = vkl;	}
		
		void checkAnalogStick() {
			int o_live_key = live_key;
			handleStickMovement();
			if (o_live_key != live_key) {
				vkl.repaintVK(); } }

		// Thresholds for when we call the stick left, right, up, down
		final static int LTHRESHOLDA = 35;
		final static int HTHRESHOLDA = 221;
		// Gentler threshold for when we're already off-axis
		final static int LTHRESHOLDB = 75;
		final static int HTHRESHOLDB = 180;
		
		// Matrix is:
		// 4 3 2
		// 5 0 1
		// 6 7 8
		
		//
		void handleStickMovement() {
			int x = com.pspkvm.keypad.RawState.getAnalogX();
			int y = com.pspkvm.keypad.RawState.getAnalogY();
			if (x < LTHRESHOLDA) {
				if (y < LTHRESHOLDB) { live_key = 4; return; }
				if (y > HTHRESHOLDB) { live_key = 6; return; }
				live_key = 5; return; }
			if (x > HTHRESHOLDA) {
				if (y < LTHRESHOLDB) { live_key = 2; return; }
				if (y > HTHRESHOLDB) { live_key = 8; return; }
				live_key = 1; return; }
			// Centered horizontally
			// Get pickier about what counts as up/down
			if (y < LTHRESHOLDA) { live_key = 3; return; }
			if (y > HTHRESHOLDA) { live_key = 7; return; }
			live_key = 0; }

		// TODO: Tighten
		void setLiveMap(int p) {
			int nboard = cboard;
			// Get back to the unshifted board
			nboard &= 2;
			// Now, see if it's shifted now.
			nboard += ((p & PSPCtrlCodes.RTRIGGER)!=0) ? 1 : 0;
			boolean n_lshift = ((p & PSPCtrlCodes.LTRIGGER)!=0);
			if (n_lshift && (!lshift)) {
				// Just toggled symbol board on/off
				nboard += 6;
				nboard %= 4; }
			lshift = n_lshift;
			boolean redraw = (nboard != cboard);
			cboard = nboard;
			if (redraw) {
				vkl.repaintVK(); } }

	// Lookup array to speed along handle_symbol.
	final static int[] of_lkup = { 0, 0, 3, 0, 2, 0, 0, 0, 1 };

	void handle_symbol(int p) {
		// Get the symbol code into the set 1, 2, 4, 8
		// (Forgive the arcane code. Optimizing. See
		// PSPCtrlCodes re the values of the symbol keys
		// in this field.)
		p >>= 12;
		if (p>8) {
			return; }
		int o = of_lkup[p];
		int m = crt_map.getMeta(cboard, (4*live_key)+o);
		if (m==SC_Keys.NUL) {
			return; }
  	if (m==SC_Keys.CHR) {
  		vkl.virtualKeyEntered(EventConstants.PRESSED, crt_map.getChar(cboard, (4*live_key)+o)); 
  		return; }
  	vkl.virtualMetaKeyEntered(m); }

	/**
	 *
	 * Call this with the incoming raw stroke--board will process and return
	 * (if appropriate) the processed stroke in its listener's
	 * virtualKeyEntered(...) or virtualMetaKeyEntered(...)
	 * 
	 * @param p a raw stroke received in a keyInput(...) method	 	 
	 */
    public void processRawStroke(int p) {
    	// Handle shoulder button shifts here
    	setLiveMap(p);
    	// Short circuit
    	if (p==0) {
				return; }
    	int s = (p & PSPCtrlCodes.SYMBOLS);
    	int d = (p & PSPCtrlCodes.DPAD_MASK);
    	if (s != 0) {
				handle_symbol(s);
				return; }
			if (d == 0) {
				return; }
			switch(d) {
    		case PSPCtrlCodes.LEFT:
    			vkl.virtualMetaKeyEntered(SC_Keys.CLF);
    			return;
    		case PSPCtrlCodes.RIGHT:
    			vkl.virtualMetaKeyEntered(SC_Keys.CRT);
    			return;
    		case PSPCtrlCodes.UP:
    			vkl.virtualMetaKeyEntered(SC_Keys.CUP);
    			return;
    		case PSPCtrlCodes.DOWN:
    			vkl.virtualMetaKeyEntered(SC_Keys.CDN);
    			return; 
				default: } }
     	
  // Display colours
	private static final int WHITE = 0xffffff;
	private static final int DKGREY = 0x202020;
	private static final int DKRED = 0x400000;
	private static final int GREY = 0xc0c0c0;
	private static final int BGCOLOR = GREY;
	
	// Strings for a few metakey displays
	static final String BSPG = "\ue023";
	static final String SPCG = "\ue02b";
	static final String CPYG = "\ue02e\ue02c";
	static final String PSTG = "\ue02e\ue02d";
	static final String SELG = "\ue02a";
	static final String ENTG = "\ue020";
		// CNC mode string (x in a circle)
	static final String CNCS = "\ue030";
	// OK string (thick hooked arrow)
	static final String OKST = "\ue02f";

	// Utility font--used to draw the key glyphs
	static final Font utility_font =
		Font.getFont(Font.FACE_UTILITY, Font.STYLE_BOLD, Font.SIZE_SMALL);
		
	// Letter offsets within key displays
	private final static int[] ltr_x = { 0, -12, 0, 12 };
	private final static int[] ltr_y = { 0, 12, 24, 12 };
	// Key offsets from origin
	private final static int[] key_x = {
		1, 2, 2, 1, 0, 0, 0, 1, 2 };
	private final static int[] key_y = {
		1, 1, 0, 0, 0, 1, 2, 2, 2 };
		
	final static int GRPWIDTH = 40;
	final static int GRPHEIGHT = 40;
	final static int GAPSIZE = 2;
	
	// The current key (in the board)
	int live_key;
	// The current board
	int cboard;
	// Whether lshift was down on last sample
	boolean lshift;
	
	// Get where to start drawing the board--x
	int getWidth() {
		return (GRPWIDTH*3)+(GAPSIZE*2); }
	// Get where to start drawing the board--y
	int getHeight() {
		return (GRPHEIGHT*3)+(GAPSIZE*3)+sel_img.getHeight(); }
		
	// The board images
  Image sel_img, dnzf_img;
  
	void constructImages() {
		sel_img = LongArrayHandler.createImage(Imgs_misc.sel_seg,
			Imgs_misc.sel_segpad);
		dnzf_img = LongArrayHandler.createImage(Imgs_misc.dnzf_seg,
			Imgs_misc.dnzf_segpad); }

	/**
	 * Display the selection state
	 * @param g the graphics object passed into the paint method
	 */
	void paintSelectionState(Graphics g) {
		if (!select_on) {
			return; }
		g.drawImage(sel_img,
			getWidth()-sel_img.getWidth()-1,
			getHeight()-sel_img.getHeight(),
			g.LEFT|g.TOP); }

	/**
	 * Display misc state info
	 * @param g the graphics object passed into the paint method
	 */
	void paintMiscState(Graphics g) {
		paintSelectionState(g);
		g.drawImage(dnzf_img, 0, getHeight()-sel_img.getHeight(), g.LEFT|g.TOP); }		 		

	// Main paint method
	public void paint(Graphics g) {
		g.setColor(BGCOLOR);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(DKRED);
		g.fillRect(0, getHeight()-sel_img.getHeight(),
			getWidth(), sel_img.getHeight()+1);
		for(int i=0; i<9; i++) {
			draw_key(g, i*4, 
				(key_x[i]*(GRPWIDTH+GAPSIZE)),
				(key_y[i]*(GRPHEIGHT+GAPSIZE)),
				i == live_key); }
		paintMiscState(g); }

	// Draw a single key
	void draw_key(Graphics g, int o, int x, int y, boolean live) {
		g.setColor(live ? DKRED : DKGREY);
		g.fillRect(x, y, GRPWIDTH, GRPHEIGHT);
		g.setColor(WHITE);
		x+=(GRPWIDTH/2);
		x+=1;
		y+=2;
		for(int i=0; i<4; i++) {
			String s = crt_map.getDisplay(cboard, o);
			int w = utility_font.stringWidth(s);
			g.drawUtilityString(s,
				x+ltr_x[i]-(w/2), y+ltr_y[i], g.TOP|g.LEFT);
			o++; } }
}
