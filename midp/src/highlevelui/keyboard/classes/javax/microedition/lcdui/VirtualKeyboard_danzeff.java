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
    
    // Chordal display state, transient entry state
    boolean caps_lock_set, select_on;

		// Keyboard state--may not need
    int currentKeyboard = 1; // abc
    
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
		live_key = 0;
		cboard = 0;           
		select_on = false;
		lshift = false;
    this.vkl = vkl; }

  public VirtualKeyboard_danzeff(int kbtype,
		  	VirtualKeyboardListener vkl,
		int w, int h) throws VirtualKeyboardException {
    if ("on".equals(VMSettings.get("com.pspkvm.virtualkeyboard.autoopen"))) {
           USE_VIRTUAL_KEYBOARD_OPEN_AUTO = true;
       }
		live_key = 0;
		cboard = 0;
		select_on = false;
		lshift = false;           
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
			
		boolean handleMetaKey(int o) {
			if (o==0) {
				// Delete
				vkl.virtualMetaKeyEntered(SC_Keys.BSP);
				return true; }
			if (o==2) {
				// Space
				vkl.virtualKeyEntered(EventConstants.PRESSED, ' ');
				return true; }
			if (cboard != 2) {
				return false; }
			// Cboard 2 has a few
			switch(o) {
				case 6:
					vkl.virtualMetaKeyEntered(SC_Keys.CPY);
					return true;
				case 10:
					vkl.virtualMetaKeyEntered(SC_Keys.PST);
					return true;
				case 14:
					vkl.virtualMetaKeyEntered(SC_Keys.SEL);
					return true;
				case 18:
					vkl.virtualMetaKeyEntered(SC_Keys.ENT);
					return true;
				default:
					return false; } }

		void handleVKPress(int o) {
			int offset = (4*live_key)+o;
			// Special handling for certain offsets
			if (handleMetaKey(offset)) {
				return; }
	  	String s = maps[cboard][offset];
			if (s.length()==0) {
				return; }
			vkl.virtualKeyEntered(EventConstants.PRESSED, s.charAt(0)); }
			
		void setLiveMap(int p) {
			int nboard = cboard;
			if ((nboard%2)==1) {
				// Get back to the unshifted board
				nboard--; }
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
    	if ((p & PSPCtrlCodes.CROSS)!=0) {
    		handleVKPress(2);
    		return; }
    	if ((p & PSPCtrlCodes.TRIANGLE)!=0) {
    		handleVKPress(0);
    		return; }
    	if ((p & PSPCtrlCodes.SQUARE)!=0) {
    		handleVKPress(1);
    		return; }
    	if ((p & PSPCtrlCodes.CIRCLE)!=0) {
    		handleVKPress(3);
    		return; }
    	if ((p & PSPCtrlCodes.LEFT)!=0) {
    		vkl.virtualMetaKeyEntered(SC_Keys.CLF);
    		return; }
    	if ((p & PSPCtrlCodes.RIGHT)!=0) {
    		vkl.virtualMetaKeyEntered(SC_Keys.CRT);
    		return; }
    	if ((p & PSPCtrlCodes.UP)!=0) {
    		vkl.virtualMetaKeyEntered(SC_Keys.CUP);
    		return; }
    	if ((p & PSPCtrlCodes.DOWN)!=0) {
    		vkl.virtualMetaKeyEntered(SC_Keys.CDN);
    		return; } }
     	
  // Display colours
	private static final int WHITE = 0xffffff;
	private static final int BLACK = 0x000000;
	private static final int DKBLUE = 0x000040;
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
	
	// Utility font--used to draw the key glyphs
	static final Font utility_font =
		Font.getFont(Font.FACE_UTILITY, Font.STYLE_BOLD, Font.SIZE_SMALL);
	
	// Display map
	static final String[] map_0 = {
		BSPG, "m", SPCG, "n", 
		"?", "o", "p", "q",
		"!", "g", "h", "i",
		".", "d", "e", "f",
		",", "a", "b", "c",
		"-", "j", "k", "l",
		"(", "r", "s", "t",
		":", "u", "v", "w",
		")", "x", "y", "z" };
		
	static final String[] map_1 = {
		BSPG, "M", SPCG, "N", 
		"\"", "O", "P", "Q",
		"*", "G", "H", "I",
		"@", "D", "E", "F",
		"^", "A", "B", "C",
		"_", "J", "K", "L",
		"=", "R", "S", "T",
		";", "U", "V", "W",
		"/", "X", "Y", "Z" };
		
	// Display map
	static final String[] map_2 = {
		BSPG, "", SPCG, "5", 
		"", "", CPYG, "6",
		"", "", PSTG, "3",
		"", "", SELG, "2",
		"", "", ENTG, "1",
		"", "", "", "4",
		"", "", "", "7",
		"", "", "", "8",
		"", "", "0", "9" };

	static final String[] map_3 = {
		BSPG, "", SPCG, "", 
		"+", "\\", "=", "/",
		"-", "[", "_", "]",
		"\"", "<", "'", ">",
		",", "(", ".", ")",
		"!", "{", "?", "}",
		":", "@", ";", "#",
		"~", "$", "`", "%",
		"*", "^", "|", "&" };
		
	static final String[][] maps = {map_0, map_1, map_2, map_3};

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
		return (GRPHEIGHT*3)+(GAPSIZE*2); }
	
	// Main paint method
	public void paint(Graphics g) {
		g.setColor(BGCOLOR);
		g.fillRect(0, 0, getWidth(), getHeight());
		for(int i=0; i<9; i++) {
			draw_key(g, maps[cboard], i*4, 
				(key_x[i]*(GRPWIDTH+GAPSIZE)),
				(key_y[i]*(GRPHEIGHT+GAPSIZE)),
				i == live_key); } }
	
	// Draw a single key
	void draw_key(Graphics g, String[] k, int o, int x, int y, boolean live) {
		g.setColor(live ? DKRED : DKBLUE);
		g.fillRect(x, y, GRPWIDTH, GRPHEIGHT);
		g.setColor(WHITE);
		x+=(GRPWIDTH/2);
		x+=1;
		y+=2;
		for(int i=0; i<4; i++) {
			int w = utility_font.stringWidth(k[o]);
			g.drawUtilityString(k[o], x+ltr_x[i]-(w/2), y+ltr_y[i], g.TOP|g.LEFT);
			o++; } }

}
