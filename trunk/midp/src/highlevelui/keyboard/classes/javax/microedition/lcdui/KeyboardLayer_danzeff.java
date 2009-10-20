/**
 * /AJ Milne--Danzeff virtual keyboard clone for the PSP platform.
 *  
 * USAGE NOTE: This layer wraps a Virtualkeyboard_danzeff instance
 * and controls a TextField instance in this context. Midlets wishing to instantiate
 * and use the board directly, especially for use within a Canvas class, do not need
 * to create it through this layer--see notes within the Virtualkeyboard_danzeff
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
import com.sun.midp.chameleon.CWindow;
import java.lang.Thread;

/**
 * This is a popup layer that handles a sub-popup within the text tfContext
 * @author AJ Milne, based on originals by Amir Uval
 */
class KeyboardLayer_danzeff extends AbstractKeyboardLayer implements CommandListener {

	// Commands handled in the menu bar
 	  private Command cmdOK, cmdCancel, cmdClear, cmdCopyAll;

  /** the instance of the virtual keyboard */
  VirtualKeyboard_danzeff vk = null;
  /** Once cell character array--useful for various inserts */
  static char tmpchrarray[];

	String layerID = null;
	/** State--tracks the quadrant we're drawing the board in -- we move it to
	 * keep it in the quadrant furthest from the caret at all times
	 */
	int current_quad;	 		

    /**
     * Constructs a text tfContext sub-popup layer, which behaves like a
     * popup-choicegroup, given a string array of elements that constitute
     * the available list of choices to select from.
     *
     * @param tf The TextEditor that triggered this popup layer.
     */
    private KeyboardLayer_danzeff(TextFieldLFImpl tf) throws VirtualKeyboardException {
	    super((Image)null, -1); // don't draw a background  
	
	    this.layerID  = "KeyboardLayer";
	    tmpchrarray = new char[1];
	    tfContext = tf;
	    // backupString is set to original text before the kbd was used
	    backupString = tfContext.tf.getString();
	    if (vk==null) {
	      vk = new VirtualKeyboard_danzeff(0,
				this,getAvailableWidth(),getAvailableHeight()); }
			current_quad = -1; // Invalid. Forces reset in setBounds.
			setBounds();
			monitorThread=null;
			setupCommands(); }
	
	// Get the quadrant the caret is in
	int getCaretQuad() {
		if (tfContext!=null) {
			return tfContext.getQuadrant(); }
		// For all others, fake it
		return TextFieldLFImpl.QUAD_TOPLFT; }
		
	/**
     * Setup as a command listener for external events.
     *
     */
	void setupCommands() {
		cmdOK = new Command("OK", Command.OK, 1);
		cmdCancel = new Command("Cancel", Command.CANCEL, 2);
		cmdClear = new Command("Clear", Command.HELP, 3);
		cmdCopyAll = new Command("Copy All", Command.HELP, 3);
		Command commands[]={cmdOK, cmdCancel, cmdClear, cmdCopyAll };
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
			if (cmd == cmdCopyAll) {
				virtualMetaKeyEntered(SC_Keys.CAL);
				return; }
			if (cmd == cmdClear) {
				virtualMetaKeyEntered(SC_Keys.CLR);
				return; } }

    /**
     * Constructs a canvas sub-popup layer, which behaves like a
     * popup-choicegroup, given a string array of elements that constitute
     * the available list of choices to select from.
     *
     * @param canvas The Canvas that triggered this popup layer.
     */
    private KeyboardLayer_danzeff(CanvasLFImpl canvas) throws VirtualKeyboardException {
        super((Image)null, -1); // don't draw a background  

        this.layerID  = "KeyboardLayer";
        tmpchrarray = new char[1];
        tfContext = null;
        cvContext = canvas;
        if (vk==null) {
            vk = new VirtualKeyboard_danzeff(this, getAvailableWidth(),getAvailableHeight());
        }
			current_quad = -1; // Invalid. Forces reset in setBounds.
			setBounds();
			// Command to dismiss
      Command keypadClose = new Command("Close", Command.OK, 1);
      setCommands(new Command[] { keypadClose }); }
       
    /**
     * Singleton
     */
    private static KeyboardLayer_danzeff instanceTF = null;
    // Save the Y position for the virtual keyboard.
    // For some reason, failing to set the bound again ( in each getInstance )
    // Causes the Y position to change. 
    private static int instanceTF_Y= 0;
    private static KeyboardLayer_danzeff instanceCV = null;

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
			return "abcd"; }

    /**
     * get TextField Keyboard layer instance
     * 
     * @param tf TextField look/feel instance for the keyboard layer
     * @return a KeyboardLayer instance.
     */
    static KeyboardLayer_danzeff getInstance(TextFieldLFImpl tf)  
			throws VirtualKeyboardException {
        if ((instanceTF == null) || (instanceTF.tfContext != tf)) {
		      instanceTF = new KeyboardLayer_danzeff(tf);
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
    static KeyboardLayer_danzeff getInstance(CanvasLFImpl canvas) 
                                                           throws VirtualKeyboardException {
        if ((instanceCV == null) || (instanceCV.cvContext != canvas)) {
            instanceCV = new KeyboardLayer_danzeff(canvas);
            instanceTF = null; }
        return instanceCV; }

    /**
     * Initializes the popup layer.
     */
    protected void initialize() {
        super.initialize(); }
    
    
    void requestFullScreenRepaint() {
			Display disp = null;
			if (tfContext != null) {
				tfContext.tf.owner.getLF().lGetCurrentDisplay().requestScreenRepaint();
				return; }
			if (cvContext != null) {
				cvContext.currentDisplay.requestScreenRepaint(); } }

    final static int EPAD = 2;
    /**
     * Sets the bounds of the popup layer.
     *
     */
    protected void setBounds() {
    	int upd_caret_quad = getCaretQuad();
    	if (upd_caret_quad == current_quad) {
				return; }
			current_quad = upd_caret_quad;
    	int w = vk.getWidth();
    	int h = vk.getHeight();
    	int aw = getAvailableWidth();
    	int ah = getAvailableHeight();
    	switch(current_quad) {
				case TextFieldLFImpl.QUAD_TOPLFT:
					super.setBounds(aw-w-EPAD, ah-h-EPAD, w, h);
					break;
				case TextFieldLFImpl.QUAD_TOPRGT:
					super.setBounds(EPAD, ah-h-EPAD, w, h);
					break;
				case TextFieldLFImpl.QUAD_BOTLFT:
					super.setBounds(aw-w-EPAD, EPAD, w, h);
					break;
				case TextFieldLFImpl.QUAD_BOTRGT:
					super.setBounds(EPAD, EPAD, w, h);
					break;
				default:
					super.setBounds(aw-w-EPAD, ah-h-EPAD, w, h); }
			requestFullScreenRepaint(); }

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
      return true; }

		// Process a raw event into a keystroke
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
				case SC_Keys.CAL:
					Clipboard.set(tfContext.tf.getString());
					return;
				case SC_Keys.CLR:
					tfContext.tf.setString("");
					tfContext.tf.getString();
					return;
				case SC_Keys.PST:
					eraseSelection();
					tfPutString(Clipboard.get(), tfContext);
					return;
				case SC_Keys.ESC:
          closeKeyEntered(false);
          return;
				case SC_Keys.ENT: 
					// TODO/FIXME--if this is a one line textfield, it would 
					// be nice to call closeKeyEntered(true) here instead.
					if (!tfContext.isMultiLine()) {
						// TODO: Fix. This probably doesn't work for TextField types
						// that do allow \n. Technically, if TextField.ANY is set
						// in the constraint mask, it does.
						return; }
					eraseSelection();
					tfPutString("\n", tfContext);
	      	tfContext.tf.getString();
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
				case SC_Keys.SEL:
					tfContext.tf.synchSelectionStart();
					vk.select_on = !vk.select_on;
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
    
    // Wrapper method--allows entering 'exotic' characters
		// in the 'any' contexts, prevents crashes in constrained
		// contexts due to disallowed input slipping through.
		// NB: A compelling reason for using this is: it's faster.
		// But we really should optimize the actual character screening.
		// (TODO)
		void tfPutKey(char a) {
			if (tfContext == null) {
				return; }
			int c = tfContext.getConstraints();
				switch (c & TextField.CONSTRAINT_MASK) {
				case TextField.PHONENUMBER:
				case TextField.DECIMAL:
				case TextField.NUMERIC:
				case TextField.EMAILADDR:
				case TextField.URL:
					tfContext.uCallKeyPressed(a);
					break;
				default:
				  // We have to use the insert call because
    			// a lot of the more exotic characters won't 
    			// go through on uCallKeyPressed.
					tmpchrarray[0]=a;
    			tfContext.tf.insert(tmpchrarray, 0, 1, tfContext.tf.getCaretPosition());
					tfContext.tf.getString(); } }

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
    	tfPutKey(c);
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
			
		// Overridden to make sure we get a monitor thread that it
		// monitors the bounds against the content below and keeps
		// the analogue stick updated
		void doThreadJob() {
			if (vk!=null) {
				setBounds();
				vk.checkAnalogStick(); } }

		boolean needsMonitorThread() {
			return true; }
}
