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
class KeyboardLayer_danzeff extends KeyboardLayer_RichEditing implements CommandListener {

	// Commands handled in the menu bar
 	  private Command cmdOK, cmdCancel, cmdClear, cmdCopyAll;

  /** the instance of the virtual keyboard */
  VirtualKeyboard_danzeff vk = null;

	String layerID = null;

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
				return; }
			super.commandAction(cmd, s); }

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

    /**
     * get the height of the Virtual Keyboard.
     * @return the height of the virtual keyboard.
     */ 
    public int getHeight() {
        return vk.getHeight(); }

		int getVKHeight() {
			return vk.getHeight(); }

		int getVKWidth() {
			return vk.getWidth(); }

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
			if (cached_ok || cached_cancel) {
				handleCachedCommand();
				return true; }
      // The two soft buttons go to the menus. Let them.
      if (code == EventConstants.SOFT_BUTTON1) {
				return false; }
      if (code == EventConstants.SOFT_BUTTON2) {
				return false; }
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
     * Paints the body of the popup layer.
     *
     * @param g The graphics context to paint to
     */
    public void paintBody(Graphics g) {
        vk.paint(g);
    }

		// Overridden to make sure we get a monitor thread that it
		// monitors the bounds against the content below and keeps
		// the analogue stick updated
		void doThreadJob() {
			if (vk!=null) {
				setBounds();
				vk.checkAnalogStick(); } }

		boolean needsMonitorThread() {
			return true; }

		boolean selectOn() {
			return vk.select_on; }
			
		void setSelectOn(boolean b) {
			vk.select_on = b; }

}
