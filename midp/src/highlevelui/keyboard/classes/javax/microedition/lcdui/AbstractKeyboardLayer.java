package javax.microedition.lcdui;

import com.sun.midp.chameleon.layers.PopupLayer;
import com.pspkvm.system.VMSettings;
import com.sun.midp.chameleon.input.*;

abstract class AbstractKeyboardLayer extends PopupLayer implements VirtualKeyboardListener {

	/**
	 * constants for setState()
	 */
	public static final int NUMERIC = 0;
	public static final int LOWERCASE = 1;
	public static final int UPPERCASE = 2;
	public static final int SYMBOL = 3;
	public static final int PINYIN = 4;
	public static final int STROKE = 5;
	
	public static final int KEYBOARD_INPUT_NUMERIC = 0;  //NUMERIC only
	public static final int KEYBOARD_INPUT_ASCII = 1;    //ascii only 
	public static final int KEYBOARD_INPUT_ANY = 2;    //any
	
	/** Indicates if this popup layer is shown (true) or hidden (false). */
	public boolean open = false;
	
	/** 
	* Indicates if the Keyboard layer was just opened or if it is already open.
	*/
	public boolean justOpened = false;
	
	AbstractKeyboardLayer(Image bgImage, int bgColor) {
		super(bgImage, bgColor); }

    /**
     * Sets the state of the keyboard: NUMERIC or ANY
     * Current implementation will set this as the "default" state
     * in which the keyboard opens with.
     * todo 3: constraints - remove unwanted keys according to
     * the constraints of the TextField.
     * (current state is that the keyboard will display the illegal
     * keys, but the TextField not allow to enter them).
     * 
     * @param state the state of the keyboard.
     */
    abstract public void setState(int state);
    
    abstract public int getState();

    abstract public int getHeight();

    static AbstractKeyboardLayer getVKInstance(TextFieldLFImpl tf)  
                                                           throws VirtualKeyboardException {
        String im = VMSettings.get("com.pspkvm.inputmethod");
        if(im != null && im.equals("qwerty")){
            return KeyboardLayer_qwert.getInstance(tf); }
        if(im != null && im.equals("semichordal")){
            return KeyboardLayer_semichordal.getInstance(tf); }
        if(im != null && im.equals("danzeff")){
            return KeyboardLayer_danzeff.getInstance(tf); }
        if(im != null && im.equals("awf_f")){
            return KeyboardLayer_AWF_F.getInstance(tf); }
				return KeyboardLayer_awf.getInstance(tf);
    }

    /**
     * get Canvas Keyboard layer instance
     * 
     * @param canvas Canvas look/feel instance for the keypad layer
     * @return a KeyboardLayer instance.
     */
    static AbstractKeyboardLayer getVKInstance(CanvasLFImpl canvas) 
                                                           throws VirtualKeyboardException {
        String im = VMSettings.get("com.pspkvm.inputmethod");
        if(im != null && im.equals("qwerty")){
            return KeyboardLayer_qwert.getInstance(canvas); }
        if(im != null && im.equals("semichordal")){
            return KeyboardLayer_semichordal.getInstance(canvas); }
        if(im != null && im.equals("danzeff")){
            return KeyboardLayer_danzeff.getInstance(canvas); }
        if(im != null && im.equals("awf_f")){
            return KeyboardLayer_AWF_F.getInstance(canvas); }
				return KeyboardLayer_awf.getInstance(canvas);
    }
    
  // Generally helpful method for inserting strings in textfields
  // (as in, from the clipboard)
	void tfPutString(String s, TextFieldLFImpl tfContext) {
		if (tfContext == null) {
			return; }
		int c = tfContext.getConstraints();
			switch (c & TextField.CONSTRAINT_MASK) {
			case TextField.PHONENUMBER:
			case TextField.DECIMAL:
			case TextField.NUMERIC:
			case TextField.EMAILADDR:
			case TextField.URL:
				char[] a = s.toCharArray();
				for (int i = 0; i < a.length; i++) {
					tfContext.uCallKeyPressed(a[i]); }
				break;
			default:
			  // We have to use the insert call because
  			// a lot of the more exotic characters won't 
  			// go through on uCallKeyPressed.
  			tfContext.tf.insert(s, tfContext.tf.getCaretPosition());
				tfContext.tf.getString(); } }
				
	// Command reflection framework -- used to allow direct command
	// delivery to the textfield (instead of going through the 
	// various input contexts)
		/*
		Set up the 'reflection' commands--commands that go straight back
		to the textbox's listener itself.
	*/
	void setupCommandReflections(TextFieldLFImpl t) {
		if (!(t.tf.owner instanceof TextBox)) {
			creflectors = new CommandReflector[0];
			return; }
		TextBox b = (TextBox)(t.tf.owner);
		creflectors = new CommandReflector[b.numCommands];
		for(int i=0; i<b.numCommands; i++) {
			creflectors[i] = new CommandReflector(b.commands[i], b.listener, b);
			if (creflectors[i].loc.getCommandType()==Command.CANCEL) {
				cancel_below = creflectors[i]; }
			if (creflectors[i].loc.getCommandType()==Command.OK) {
				ok_below = creflectors[i]; } } }

	// Cached 'okay' and 'cancel' commands for signalling all the way down to
	// the underlying box, if we find them.
	CommandReflector ok_below = null, cancel_below = null;

	// Command reflector--binds a local command to a remote command to fire
	// into a given listener, referencing a given displayable.
	// Used to provide local commands that effectively fire down into
	// the TextBox below an implicitly invoked virtual keyboard.
	class CommandReflector {
		Command loc, rem;
		CommandListener lis;
		Displayable dis;
		CommandReflector(Command c, CommandListener l, Displayable d) {
			lis = l;
			rem = c;
			dis=d;
			loc = new Command("-> "+c.getLabel(), c.getLongLabel(),
				c.getCommandType(), c.getPriority()); }
		void reflect() {
			closeKeyEntered(true);
			lis.commandAction(rem, dis); } }
			
	void closeKeyEntered(boolean ok_sent) { }

	CommandReflector[] creflectors;
				
	// Call from a command event handler.
	// Returns true if it reflects something down.
	boolean reflectCommand(Command c) {
		for(int i=0; i<creflectors.length; i++) {
			if (creflectors[i].loc == c) {
				creflectors[i].reflect();
				return true; } }
		return false; }
  
}
