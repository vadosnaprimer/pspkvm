/*
 * /AJ Milne--custom semichordal version for PSP platform
 */

package javax.microedition.lcdui;

import com.sun.midp.lcdui.*;
import com.sun.midp.configurator.Constants;
import com.sun.midp.chameleon.skins.DateEditorSkin;
import com.sun.midp.chameleon.skins.ChoiceGroupSkin;
import com.sun.midp.chameleon.layers.PopupLayer;
import com.sun.midp.chameleon.input.*;
import com.sun.midp.main.Configuration;

/**
 * This is a popup layer that handles a sub-popup within the text tfContext
 * @author AJ Milne, based on originals by Amir Uval
 */
class KeyboardLayer_semichordal extends AbstractKeyboardLayer implements CommandListener {

  int symbolDownLast = 0;
	int chordDownLast = 0;
  boolean c_lock = false;
	private Command keyboardClose;
	private Command keyboardHelp;
	private Command keyboardBack;

  /** the instance of the virtual keyboard */
  VirtualKeyboard_semichordal vk = null;

	String layerID = null;

    /**
     * Constructs a text tfContext sub-popup layer, which behaves like a
     * popup-choicegroup, given a string array of elements that constitute
     * the available list of choices to select from.
     *
     * @param tf The TextEditor that triggered this popup layer.
     */
    private KeyboardLayer_semichordal(TextFieldLFImpl tf) throws VirtualKeyboardException {
        super((Image)null, -1); // don't draw a background  

        this.layerID  = "KeyboardLayer";
        tfContext = tf;
        //backupstring is set to original text before the kbd was used
        backupString = tfContext.tf.getString();
        if (vk==null) {
            vk = new VirtualKeyboard_semichordal(0,
						this,getAvailableWidth(),getAvailableHeight()); }

		setBounds();
		setupCommands(); }
		
	/**
     * Setup as a command listener for external events.
     *
     */
	void setupCommands() {
		keyboardClose = new Command("OK", Command.SCREEN, 1);
		keyboardHelp = new Command("Back", Command.SCREEN, 1);
		keyboardBack = new Command("Cancel", Command.EXIT, 1);
		Command commands[]={keyboardClose,/*keyboardHelp,*/keyboardBack};
		setCommandListener(this);
		setCommands(commands); }
		       
	/**
     * Handle a command action.
     *
     * @param cmd The Command to handle
     * @param s   The Displayable with the Command
     */
    public void commandAction(Command cmd, Displayable s) {
    System.out.println("commandAction="+cmd);
        if (cmd == keyboardClose) {
			virtualMetaKeyEntered(VirtualKeyboard_semichordal.CANCEL_COMMAND);
    	}else if (cmd == keyboardHelp) {
	    	virtualMetaKeyEntered(VirtualKeyboard_semichordal.OK_COMMAND);
    	}else if (cmd == keyboardBack) {
	    	System.out.println("commandAction="+cmd);
			virtualMetaKeyEntered(VirtualKeyboard_semichordal.CURSOR_UP_COMMAND);
    	}
	}

    /**
     * Constructs a canvas sub-popup layer, which behaves like a
     * popup-choicegroup, given a string array of elements that constitute
     * the available list of choices to select from.
     *
     * @param canvas The Canvas that triggered this popup layer.
     */
    private KeyboardLayer_semichordal(CanvasLFImpl canvas) throws VirtualKeyboardException {
        super((Image)null, -1); // don't draw a background  

        this.layerID  = "KeyboardLayer";
        tfContext = null;
        cvContext = canvas;
        if (vk==null) {
            vk = new VirtualKeyboard_semichordal(this, getAvailableWidth(),getAvailableHeight());
        }

			setBounds();

			// Command to dismiss
      Command keypadClose = new Command("Close", Command.OK, 1);
      setCommands(new Command[] { keypadClose }); }
       
    /**
     * Singleton
     */
    private static KeyboardLayer_semichordal instanceTF = null;
    // Save the Y position for the virtual keyboard.
    // For some reason, failing to set the bound again ( in each getInstance )
    // Causes the Y position to change. 
    private static int instanceTF_Y= 0;
    private static KeyboardLayer_semichordal instanceCV = null;

		public int inputState=0;
    /**
     * Sets the state of the keyboard: NUMERIC or ANY
     * Current implementation will set this as the "default" state
     * in which the keyboard opens with.
     * todo/fixme 3: constraints - remove unwanted keys according to
     * the constraints of the TextField.
     * (current state is that the keyboard will display the illegal
     * keys, but the TextField not allow to enter them).     
     * 
     * @param state the state of the keyboard.
     */
    public void setState(int state) {
    		System.out.println("setState->"+state);
			inputState = state;
        if (vk != null) {
			switch(state){
				case KEYBOARD_INPUT_NUMERIC:
					vk.currentKeyboard=NUMERIC;
					break;
				case KEYBOARD_INPUT_ASCII:
					vk.currentKeyboard=LOWERCASE;
					break;
				case KEYBOARD_INPUT_ANY://根据PSPKVM.INI 配置决定默认输入法类型
					if("vk-chinese".equals(Configuration.getProperty("com.pspkvm.inputmethod"))){
						vk.currentKeyboard=PINYIN;
					}else{
						vk.currentKeyboard=LOWERCASE;
					}
					break;
				default:
					vk.currentKeyboard=NUMERIC;
					break;
			}
        }
    }

    public int getState() {           
			return inputState; }

    public String getIMName() {
			if (vk == null) {
				return null; }
			switch(vk.currentKeyboard) {
	      case AbstractKeyboardLayer.NUMERIC:
	        return "1234";
	      case AbstractKeyboardLayer.LOWERCASE:
	        return "abcd";
	      case AbstractKeyboardLayer.UPPERCASE:
	        return "ABCD";
	      case AbstractKeyboardLayer.SYMBOL:
	        return "Symbol";
			    }
			return null; }

    /**
     * get TextField Keyboard layer instance
     * 
     * @param tf TextField look/feel instance for the keyboard layer
     * @return a KeyboardLayer instance.
     */
    static KeyboardLayer_semichordal getInstance(TextFieldLFImpl tf)  
			throws VirtualKeyboardException {
        if ((instanceTF == null) || (instanceTF.tfContext != tf)) {
		      instanceTF = new KeyboardLayer_semichordal(tf);
		      instanceTF_Y = instanceTF.bounds[Y];
		      instanceCV = null; }

        instanceTF.tfContext = tf;
        instanceTF.bounds[Y]= instanceTF_Y;
        return instanceTF; }

    /**
     * get Canvas Keyboard layer instance
     * 
     * @param canvas Canvas look/feel instance for the keypad layer
     * @return a KeyboardLayer instance.
     */
    static KeyboardLayer_semichordal getInstance(CanvasLFImpl canvas) 
                                                           throws VirtualKeyboardException {
        if ((instanceCV == null) || (instanceCV.cvContext != canvas)) {
            instanceCV = new KeyboardLayer_semichordal(canvas);
            instanceTF = null; }

        return instanceCV; }

    /**
     * Initializes the popup layer.
     */
    protected void initialize() {
        super.initialize(); }        

    /**
     * Sets the bounds of the popup layer.
     *
     */
    protected void setBounds() {
    	int w = vk.getWidth();
    	int h = vk.getHeight();
    	int x = getAvailableWidth()-w;
    	int y = getAvailableHeight()-h;
      super.setBounds(x, y, w, h); }

    /**
     * get the height of the Virtual Keyboard.
     * @return the height of the virtual keyboard.
     */ 
    public int getHeight() {
        return vk.getHeight(); }

    /**
     * Declare this layer supports raw key input events
     *
     * @return true if this layer supports handling raw key input events
     */
    public boolean supportsRawKeyInput() {
        return true; }
    
    /**
     * Handles key event in the open popup.
     *
     * @param type - The type of this key event (pressed, released)
     * @param code - The code of this key event
     * @return true always, since popupLayers swallow all key events
     */
    public boolean keyInput(int type, int code) {

    	if ((tfContext == null) && (cvContext == null)) {
				return true; }
      if (type == EventConstants.RAWKEYSTATE) {
      	processRawStroke(code);
      	return true; }
      if ((type == EventConstants.PRESSED ||
				type == EventConstants.RELEASED ||
				type == EventConstants.REPEATED)) {
				handleStandardKeyEvent(type, code); }
			return true; }
        
    // We do handle a very few standard key events using the
    // standard events. Not many.
		void handleStandardKeyEvent(int type, int code) {
		  if (type == EventConstants.RELEASED && code == EventConstants.SOFT_BUTTON2) {
		  	// Means close the keyboard.
        closeKeyEntered(true); } }
    
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
			
		// Process a raw event into a keystroke--masking for upstrokes,
		// chordal shifts sans strikes
		void processRawStroke(int p) {
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
				tfContext.uCallKeyPressed(SC_Keys.chordal_map_chars[offset]);
        tfContext.tf.getString();
				return; }
			if (SC_Keys.isMeta(offset)) {
				virtualChordalMetaEntered(SC_Keys.chordal_map_meta[offset]); } }

		// Process metakeys
		void virtualChordalMetaEntered(int m) {
			if (tfContext == null) {
				return; }
			Display disp = tfContext.tf.owner.getLF().lGetCurrentDisplay();
			switch(m) {
				case SC_Keys.ESC: 
          closeKeyEntered(false);
          return;
				case SC_Keys.ENT: 
					// TODO/FIXME--if this is a one line textfield, it would 
					// be nice to call closeKeyEntered(true) here instead.
					tfContext.tf.insert("\n", tfContext.tf.getCaretPosition());
	      	tfContext.tf.getString();
					return;
        case SC_Keys.CLK:
          c_lock = ! c_lock;
          vk.setCapsLockState(c_lock);
          repaintVK();
          return;
        case SC_Keys.CLF:
          tfContext.moveCursor(Canvas.LEFT);
          return;
        case SC_Keys.CRT:
					tfContext.moveCursor(Canvas.RIGHT);
          return;
        case SC_Keys.CDN:
					tfContext.moveCursor(Canvas.DOWN);
          return;
        case SC_Keys.CUP:
					tfContext.moveCursor(Canvas.UP);
          return;
        case SC_Keys.BSP:
          tfContext.keyClicked(InputMode.KEYCODE_CLEAR);
					return;
        case SC_Keys.DEL:
					tfContext.moveCursor(Canvas.RIGHT);
          tfContext.keyClicked(InputMode.KEYCODE_CLEAR);
					return;
				case SC_Keys.DSP:
					setVisible(vk.toggleDisplayChords());
					disp.requestScreenRepaint();
					return;   
        default:
        	return; } }
	  
	 // Called to close the thing
	 void closeKeyEntered(boolean ok_sent) {
      Display disp = null;
      if (tfContext != null) {
        disp = tfContext.tf.owner.getLF().lGetCurrentDisplay();
        if (!ok_sent) {
					tfContext.tf.setString(backupString); } }
      else if (cvContext != null) {
      	disp = cvContext.currentDisplay; }
      if (disp == null) {
          System.out.println("Could not find display - Can't hide popup"); }
			else {
          disp.hidePopup(this); }
      open = false;
      justOpened = false; }
      
    // Update the chord map
    void updateChordMap(int p) {
			int r = getChordOffset(p);
			boolean shift_set = ((p & PSPCtrlCodes.RTRIGGER)!=0);
			if (c_lock) {
				shift_set = !shift_set; }
			if (shift_set) {
				r += 4; }
			if (vk.setDisplayState(r, p, c_lock)) {
				repaintVK(); } }

		// Translate chordal downstroke to key
		int getCharOffset(int p) {
			int r = getChordOffset(p);
			boolean shift_set = ((p & PSPCtrlCodes.RTRIGGER)!=0);
			if (c_lock) {
				shift_set = !shift_set; }
			if (shift_set) {
				r += 4; }
			if ((p & PSPCtrlCodes.TRIANGLE)!=0) {
				return r; }
			if ((p & PSPCtrlCodes.SQUARE)!=0) {
				return r+1; }
			if ((p & PSPCtrlCodes.CROSS)!=0) {
				return r+2; }
			return r+3; }

    /**
     * Handle input from a pen tap. Parameters describe
     * the type of pen event and the x,y location in the
     * layer at which the event occurred. Important : the
     * x,y location of the pen tap will already be translated
     * into the coordinate space of the layer.
     *
     * @param type the type of pen event
     * @param x the x coordinate of the event
     * @param y the y coordinate of the event
     */
    public boolean pointerInput(int type, int x, int y) {
        return vk.pointerInput(type,x,y);  
    }

    /**
     * Paints the body of the popup layer.
     *
     * @param g The graphics context to paint to
     */
    public void paintBody(Graphics g) {
        vk.paint(g);
    }


    // ********** package private *********** //

    /** Text field look/feel context */
    TextFieldLFImpl tfContext = null;

    /** Canvas look/feel context */
    CanvasLFImpl cvContext = null;

    /** the original text field string in case the user cancels */
    String backupString;

    /**
     * VirtualKeyboardListener interface
     *
     * MIDlet that wants the receive events from the virtual
     * keyboard needs to implement this interface, and register as
     * a listener.
     * @param c char selected by the user from the virtual keyboard
     *
     */
    public void virtualKeyEntered(int type, char c) { /* STUB */ }

    /**
     * VirtualKeyboardListener interface
     * NOTE: Only still present because the command listener routes through it.
     * TODO/FIXME: Remove/stubify; integrate into command listener.		      
     * 
     * @param metaKey special keys: 0=ok; 1=cancel
     *
     */
    public void virtualMetaKeyEntered(int metaKey) {
        Display disp;

        if (tfContext != null) {
            disp = tfContext.tf.owner.getLF().lGetCurrentDisplay();
        } else if (cvContext != null) {
            disp = cvContext.currentDisplay;
        } else {
            return;
        }
		if (tfContext == null) {
			return; }
		System.out.println("virtualMetaKeyEntered="+metaKey);
		switch (metaKey) {
			case VirtualKeyboard_semichordal.OK_COMMAND:
				disp.hidePopup(this);
	            open = false;
				break;
			case VirtualKeyboard_semichordal.CANCEL_COMMAND:
				if (tfContext != null) {
	                tfContext.tf.setString(backupString);
	            }
	            disp.hidePopup(this);
	            open = false;
				break;
			case VirtualKeyboard_semichordal.CURSOR_UP_COMMAND:
				tfContext.moveCursor(Canvas.UP);
				break;
			default:
				break;
		}
      // comment - customer may want backspace event also for Canvas.
      // in this case, we should handle this case here (else.. cvContext.keyPress(..))
    }

    /**
     * paint text only
     * 
     * @param g The graphics context to paint to
     * @param width width of screen to paint
     * @param height height of screen to paint
     */
    public void paintTextOnly(Graphics g, int width, int height) {
        if (tfContext != null) {
            tfContext.lPaintContent(g, width, height);
        }        
    }

    public void paintCandidateBar(Graphics g, int width, int height) {
        /** Stub */
    }
    
    /**
     * get available width
     * 
     * @return the available width.
     */
    public int getAvailableWidth() {
        if (tfContext != null) {
            return tfContext.tf.owner.getWidth();
        } else if (cvContext != null) {
            return cvContext.owner.getWidth();
        }
        return 0;
    }

    /**
     * get available height
     * 
     * @return the available height.
     */
    public int getAvailableHeight() {
        if (tfContext != null) {
            return tfContext.tf.owner.getHeight();
        } else if (cvContext != null) {
            return cvContext.owner.getHeight();
        }
        return 0;
    }

    /**
     * repaint the virtual keyboard.
     */
    public void repaintVK() {
        requestRepaint(); }

}
