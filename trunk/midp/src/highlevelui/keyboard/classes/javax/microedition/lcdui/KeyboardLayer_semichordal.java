/**
 * /AJ Milne--custom semichordal virtual keyboard implementation for the PSP platform.
 *  
 * USAGE NOTE: This layer wraps a Virtualkeyboard_semichordal instance
 * and controls a TextField instance in this context. Midlets wishing to instantiate
 * and use the board directly, especially for use within a Canvas class, do not need
 * to create it through this layer--see notes within the Virtualkeyboard_semichordal
 * re doing this correctly. 
 * 
 * NOTE ALSO that the TextField, TextFieldLFImpl and TextBoxLFImpl classes within
 * the VM provided within the PSPKVM implement supersets of the standard interfaces,
 * in order to provide selection handling within text fields, and this layer
 * depends on these additions to the interface.     
 */

package javax.microedition.lcdui;

import com.sun.midp.lcdui.*;
import com.sun.midp.chameleon.input.*;
import com.sun.midp.chameleon.MIDPWindow;

/**
 * This is a popup layer that handles a sub-popup within the text tfContext
 * @author AJ Milne, based on originals by Amir Uval
 */
class KeyboardLayer_semichordal extends AbstractKeyboardLayer implements CommandListener {

	// Commands handled in the menu bar
  private Command cmdOK, cmdCancel, cmdToggleDisplay;

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
			setupCommands();
			if (tfContext.isMultiLine() &&
				(getAvailableWidth()>=vk.getWidth()) &&
				(getAvailableHeight()>=vk.getHeight())) {
					// If it's a multiline text field, and if there's room,
					// start with the large display.
				vk.setLargeDisplay(); } }
		
	/**
     * Setup as a command listener for external events.
     *
     */
	void setupCommands() {
		cmdOK = new Command("OK", Command.OK, 1);
		cmdCancel = new Command("Cancel", Command.CANCEL, 2);
		cmdToggleDisplay = new Command("Toggle Display", Command.HELP, 3);
		Command commands[]={cmdOK,cmdCancel,cmdToggleDisplay};
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
      if (cmd == cmdOK) {
				closeKeyEntered(true);
				return; }
      if (cmd == cmdCancel) {
				closeKeyEntered(false);
				return; }
      if (cmd == cmdToggleDisplay) {
				virtualMetaKeyEntered(SC_Keys.DSP); } }

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
				case KEYBOARD_INPUT_ANY:
						vk.currentKeyboard=LOWERCASE;
					break;
				default:
					vk.currentKeyboard=NUMERIC;
					break; } } }

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
     * @return true except for keys heading for the menus (Soft 1 and 2)     
     */
    public boolean keyInput(int type, int code) {

    	if ((tfContext == null) && (cvContext == null)) {
				return true; }
      if (type == EventConstants.RAWKEYSTATE) {
      		processRawStroke(code);
					return true; }
      // The two soft buttons go to the menus. Let them.
      if (code == EventConstants.SOFT_BUTTON1) {
				return false; }
      if (code == EventConstants.SOFT_BUTTON2) {
				return false; }
			// Remaining keypress events go through the vk's standard
			// event handler
			if (((MIDPWindow)owner).systemMenuUp()) {
				// Annoyingly, these arrive even when the system menu is up. Ignore them.
				return true; }
			vk.processStandardKeyEvent(type, code);
      return true; }

		// Process a raw event into a keystroke--masking for upstrokes,
		// chordal shifts sans strikes
		void processRawStroke(int p) {
			if (((MIDPWindow)owner).systemMenuUp()) {
				// Annoyingly, these arrive even when the system menu is up. Ignore them.
				return; }
			if (tfContext == null) {
				return; }
			vk.processRawStroke(p); }
				
		/**
		 *	Helper for various actions that move the cursor
		 *	without changing the select state
		 */		 		 		
		void synchSelectEnd(TextField tf) {
			if (!vk.select_on) {
				return; }
			tf.synchSelectionEnd(); }

		/**
		 *	Helper for various actions that erase the current selection
		 */
		void eraseSelection() {
			if (!vk.select_on) {
				return; }
			if (tfContext==null) {
				return; }
			vk.select_on=false;
			repaintVK();
			if (tfContext.tf.getSelectionLength()==0) {
				// Nothing to do
				return; }
			// Do the delete
			int a = tfContext.tf.getSelectionLow();
			tfContext.tf.deleteSelection();
			tfContext.setCaretPosition(a);
			tfContext.lRequestPaint(); }

		/**
		 * VirtualKeyboardListener interface		
		 * Process incoming metakeys
		 * @param m the metakey pressed
		 */		 		 		
		public void virtualMetaKeyEntered(int m) {
			if (tfContext == null) {
				return; }
			Display disp = tfContext.tf.owner.getLF().lGetCurrentDisplay();
			switch(m) {
				case SC_Keys.CPY:
					Clipboard.set(tfContext.tf.getSelection());
					return;
				case SC_Keys.PST:
					eraseSelection();
					tfContext.tf.insert(Clipboard.get(), tfContext.tf.getCaretPosition());
					tfContext.tf.getString();
					return;
				case SC_Keys.ESC: 
          closeKeyEntered(false);
          return;
				case SC_Keys.ENT: 
					// TODO/FIXME--if this is a one line textfield, it would 
					// be nice to call closeKeyEntered(true) here instead.
					if (!tfContext.isMultiLine()) {
						return; }
					eraseSelection();
					tfContext.tf.insert("\n", tfContext.tf.getCaretPosition());
	      	tfContext.tf.getString();
					return;
        case SC_Keys.CLK:
          vk.caps_lock_set = ! vk.caps_lock_set;
          repaintVK();
          return;
        case SC_Keys.CLF:
          tfContext.moveCursor(Canvas.LEFT);
          synchSelectEnd(tfContext.tf);
          return;
        case SC_Keys.CRT:
					tfContext.moveCursor(Canvas.RIGHT);
          synchSelectEnd(tfContext.tf);
          return;
        case SC_Keys.CDN:
					tfContext.moveCursor(Canvas.DOWN);
          synchSelectEnd(tfContext.tf);
          return;
        case SC_Keys.CUP:
					tfContext.moveCursor(Canvas.UP);
          synchSelectEnd(tfContext.tf);
          return;
        case SC_Keys.HME:
        	tfContext.setCaretPosition(0);
        	tfContext.lRequestPaint();
        	synchSelectEnd(tfContext.tf);
        	return;
        case SC_Keys.END:
        	tfContext.setCaretPosition(tfContext.tf.buffer.length());
        	tfContext.lRequestPaint();
        	synchSelectEnd(tfContext.tf);
        	return;
        case SC_Keys.BSP: {
        	boolean del_more = !vk.select_on;
					eraseSelection();
					if (!del_more) {
						return; }
          tfContext.keyClicked(InputMode.KEYCODE_CLEAR);
					return; }
        case SC_Keys.DEL: {
        	boolean del_more = !vk.select_on;
					eraseSelection();
					if (!del_more) {
						return; }
					tfContext.moveCursor(Canvas.RIGHT);
          tfContext.keyClicked(InputMode.KEYCODE_CLEAR);
					return; }
				case SC_Keys.DSP:
					setVisible(vk.toggleDisplayChords());
					disp.requestScreenRepaint();
					return;
				case SC_Keys.SEL:
					tfContext.tf.synchSelectionStart();
					vk.select_on = !vk.select_on;
					disp.requestScreenRepaint();
					return;
				case SC_Keys.GRV:
				case SC_Keys.ACU:
				case SC_Keys.CIR:
				case SC_Keys.TIL:
				case SC_Keys.DIA:
				case SC_Keys.RIN:
				case SC_Keys.CED:
					addDiacritical(m);
					return;
        default:
        	return; } }        	
        	
		/**
		 * Called to add a diacritical to the character before the
		 * 		 selection, when the diacritical modifier metakeys are struck.
		 * @param d the diacritical key pressed (from SC_Keys ... GRV et al)
		 */		 		 
		void addDiacritical(int d) {
			if (tfContext == null) {
				return; }
			int p = tfContext.tf.getCaretPosition();
			if (p==0) {
				// You have to be pointed just after 
				// the character you're going to decorate
				return; }
			char a = tfContext.tf.getString().charAt(p-1);
			// Annoyingly, to use insert, you need either a String or a char[]...
			// so far as I can see.
			char[] aa = new char[1];
			aa[0] = Diacriticals.getDiacritical(a, d);
			tfContext.keyClicked(InputMode.KEYCODE_CLEAR);
			tfContext.tf.insert(aa, 0, 1, tfContext.tf.getCaretPosition());
			tfContext.tf.getString(); }

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
    public void virtualKeyEntered(int type, char c) {
    	eraseSelection();
    	if (tfContext != null) {
				tfContext.uCallKeyPressed(c);
      	tfContext.tf.getString(); }
      if (cvContext != null) {
				cvContext.uCallKeyPressed(c); } }

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
        /** Stub. TODO/FIXME: should we just paint the minimal selection? */
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
