/*
 * $LastChangedDate: 2006-03-06 01:36:46 +0900 (8›? 06 3 2006) $  
 *
 * Copyright  1990-2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.microedition.lcdui;

import com.sun.midp.lcdui.*;
import com.sun.midp.configurator.Constants;
import com.sun.midp.chameleon.skins.DateEditorSkin;
import com.sun.midp.chameleon.skins.ChoiceGroupSkin;
import com.sun.midp.chameleon.layers.PopupLayer;
import com.sun.midp.chameleon.input.*;

/**
 * This is a popup layer that handles a sub-popup within the text tfContext
 * @author Amir Uval
 */
class KeyboardLayer_AWF_F extends AbstractKeyboardLayer implements CommandListener {

	private Command keyboardClose;
	private Command keyboardBack;

	/** the instance of the virtual keyboard */
	VirtualKeyboard_AWF_F vk = null;
	
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
	private KeyboardLayer_AWF_F(TextFieldLFImpl tf) throws VirtualKeyboardException {
		super((Image)null, -1); // don't draw a background  
		
		this.layerID  = "KeyboardLayer";
		tfContext = tf;
		// backupString is set to original text before the kbd was used
		backupString = tfContext.tf.getString();
		if (vk==null) {
			vk = new VirtualKeyboard_AWF_F(0, this); }
		current_quad = -1; // Invalid. Forces reset in setBounds.
		setBounds();
		monitorThread=null;
		keyboardClose = new Command("OK", Command.SCREEN, 1);
		keyboardBack = new Command("Back", Command.SCREEN, 1);
		Command commands[]={keyboardClose,keyboardBack};
		setCommands(commands);
		setCommandListener(this); }

	// Help screen content (NB: Not currently used)
	final static String HELPSTR =
		"1.(L \u6216 R)+O: \u5207\u6362\u8f93\u5165\u6cd5. (Switch Input Method)\n" +
		"2.(L \u6216 R)+select: \u529f\u80fd\u540c\u4e0a. (Switch Input Method)\n" +
		"3.(L \u6216 R)+X: \u5220\u9664\u8f93\u5165. (Delete)\n" +
		"4. X: \u62fc\u97f3\u72b6\u6001\u4e0b\u5220\u9664\u8f93\u5165. (Delete PinYin)\n" +
		"5.\u65b9\u5411\u952e\u6216\u6447\u6746: \u79fb\u52a8\u5149\u6807\u6216\u5207\u6362\u952e\u76d8\u533a\u57df.\n" +
		"  Up/Down/Left/Right: Move cursor";

	/**
     * Handle a command action.
     *
     * @param cmd The Command to handle
     * @param s   The Displayable with the Command
     */
    public void commandAction(Command cmd, Displayable s) {
    System.out.println("commandAction="+cmd);
    	if (cmd == keyboardBack) {
				virtualMetaKeyEntered(VirtualKeyboard_AWF_F.CANCEL_COMMAND);
				return; }
      if (cmd == keyboardClose) {
	    	virtualMetaKeyEntered(VirtualKeyboard_AWF_F.OK_COMMAND);
	    	return; }
    	super.commandAction(cmd, s); }

	/**
	* Constructs a canvas sub-popup layer, which behaves like a
	* popup-choicegroup, given a string array of elements that constitute
	* the available list of choices to select from.
	*
	* @param canvas The Canvas that triggered this popup layer.
	*/
	private KeyboardLayer_AWF_F(CanvasLFImpl canvas) throws VirtualKeyboardException {
		super((Image)null, -1); // don't draw a background  
		
		this.layerID  = "KeyboardLayer";
		tfContext = null;
		cvContext = canvas;
		if (vk==null) {
			prepareKeyMapCanvas();
			vk = new VirtualKeyboard_AWF_F(keys, this, 0); }
		current_quad = -1; // Invalid. Forces reset in setBounds.
		setBounds();
		monitorThread=null;
		Command keypadClose = new Command("Close", Command.OK, 1);
		setCommands(new Command[] { keypadClose }); }
       
    /**
     * Singleton
     */
    private static KeyboardLayer_AWF_F instanceTF = null;
    // Save the Y position for the virtual keyboard.
    // For some reason, failing to set the bound again ( in each getInstance )
    // Causes the Y position to change. 
    private static int instanceTF_Y= 0;
    private static KeyboardLayer_AWF_F instanceCV = null;

    /**
     * Command to dismiss the keypad without selection.
     */ 

    private static final boolean[][] isMap = {
     // |NUMERIC|LOWERCASE|UPPERCASE|SYMBOL|PINYIN|STROK
        { true,   false,   false,    false, false , false}, // KEYBOARD_INPUT_NUMERIC
        { true,   true,    true,     true, false , false}, // KEYBOARD_INPUT_ASCII
        { true,   true,    true,     true,  true , true} // KEYBOARD_INPUT_ANY 

    };

	public int inputState=0;
	public void getNextKeyBoard()
	{
		if(vk!=null){
			int maxInputMode=1;
			if(getState()!=-1){
				maxInputMode=isMap[inputState].length;
				for(int i=vk.currentKeyboard+1;i<=maxInputMode;i++){
					if(isMap[inputState][i%maxInputMode]==true){
						vk.currentKeyboard=i%maxInputMode;
						break;
					}
				}
			}
		}
	}

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
					String m = com.pspkvm.system.VMSettings.get("com.pspkvm.virtual_keyboard.awf_smap");
					vk.currentKeyboard = LOWERCASE;
					if (m.equals("pinyin")) {
						vk.currentKeyboard=PINYIN; }
					else if (m.equals("stroke")) {
						vk.currentKeyboard=STROKE; }
					break;
				default:
					vk.currentKeyboard=NUMERIC;
					break;
			}
			vk.prepareKeyMap();
        }
    }

    public int getState() {           
            return inputState;
    }

    public String getIMName() {
        if (vk != null) {
            switch(vk.currentKeyboard)
            {
                case AbstractKeyboardLayer.NUMERIC:
                    return "1234";
                case AbstractKeyboardLayer.LOWERCASE:
                    return "abcd";
                case AbstractKeyboardLayer.UPPERCASE:
                    return "ABCD";
                case AbstractKeyboardLayer.SYMBOL:
                    return "Symbol";
				case AbstractKeyboardLayer.PINYIN:	
					return "Pinyin";
				case AbstractKeyboardLayer.STROKE:	
					return "Stroke";	
            }
        }
        return null;
    }

    /**
     * get TextField Keyboard layer instance
     * 
     * @param tf TextField look/feel instance for the keyboard layer
     * @return a KeyboardLayer instance.
     */
    static KeyboardLayer_AWF_F getInstance(TextFieldLFImpl tf)  
                                                           throws VirtualKeyboardException {
        if ((instanceTF == null) || (instanceTF.tfContext != tf)) {
            instanceTF = new KeyboardLayer_AWF_F(tf);
            instanceTF_Y = instanceTF.bounds[Y];
            instanceCV = null;
            
        }

        instanceTF.tfContext = tf;
        instanceTF.bounds[Y]= instanceTF_Y;

        return instanceTF;
    }

    /**
     * get Canvas Keyboard layer instance
     * 
     * @param canvas Canvas look/feel instance for the keypad layer
     * @return a KeyboardLayer instance.
     */
    static KeyboardLayer_AWF_F getInstance(CanvasLFImpl canvas) 
                                                           throws VirtualKeyboardException {
        if ((instanceCV == null) || (instanceCV.cvContext != canvas)) {
            instanceCV = new KeyboardLayer_AWF_F(canvas);
            instanceTF = null;
        }

        return instanceCV;
    }

    /**
     * Initializes the popup layer.
     */
    protected void initialize() {
        super.initialize();
    }        

    /**
     * get the height of the Virtual Keyboard.
     * @return the height of the virtual keyboard.
     */ 
    public int getHeight() {
        return vk.kbHeight + 4;
    }

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
		// The two soft buttons go to the menus. Let them.
		if (code == EventConstants.SOFT_BUTTON1) {
			return false; }
		if (code == EventConstants.SOFT_BUTTON2) {
			return false; }
		// Remaining keypress events go through the vk's standard
		// event handler
		if (type == EventConstants.PRESSED ||
			type == EventConstants.RELEASED ||
			type == EventConstants.REPEATED) {
			try{
				vk.traverse(type,code);
			}catch(IllegalArgumentException e){
				System.out.println(e);	
			}catch(NullPointerException e){
				System.out.println(e);	
			}catch(StringIndexOutOfBoundsException e){
				System.out.println(e); } }
		return true; }

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

    /** the list of available keys */
    char[][] keys = null;
    

    /**
     * Prepare key map for Canvas keypad.
     */
    void prepareKeyMapCanvas() {         
        keys = new char[1][];              
         //keys[0] = new char[16]; 
        keys[0] = new char[12]; 
         // game keys A,B,C,D

         keys[0][0] = 'A';
         keys[0][1] = 'B';
         keys[0][2] = 'C';
         keys[0][3] = 'D';
        
         keys[0][4] = '*';
         keys[0][5] = '#'; 
         keys[0][6] = '0'; 

         keys[0][7] = '<';
         keys[0][8] = '>';
         keys[0][9] = '^';
         keys[0][10] = 'v';

         keys[0][11] = ' ';
     }

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

        // c == 0 - Trying to dismiss the virtual keyboard
        // 
        int key = c;

        if (c == 0) {
            Display disp = null;

            if (tfContext != null) {
                disp = tfContext.tf.owner.getLF().lGetCurrentDisplay();
            } else if (cvContext != null) {
                disp = cvContext.currentDisplay;
            }

            if (disp == null) {
                System.out.println("Could not find display - Can't hide popup");
            } else {
                disp.hidePopup(this);
            }

            open = false;
            justOpened = false;
            return;
        }
       
		if (cvContext != null) {
            switch (c) {
                case 'A': key = '1'; break;
                case 'B': key = '3'; break;
                case 'C': key = '7'; break;
                case 'D': key = '9'; break;
                case '<': key = Constants.KEYCODE_LEFT; break;
                case '>': key = Constants.KEYCODE_RIGHT; break;
                case '^': key = Constants.KEYCODE_UP; break;
                case 'v': key = Constants.KEYCODE_DOWN; break;
                case ' ': key = Constants.KEYCODE_SELECT; break;
            }

            if (type == EventConstants.RELEASED) {
                cvContext.uCallKeyReleased(key);

                if (!justOpened) {
                    Display disp = cvContext.currentDisplay;
                    if (disp == null) {
                        System.out.println("Could not find display - Can't hide popup");
                    } else {
                        //FIXME: Add option to automatically remove...
                        if( c == ' ')
                            disp.hidePopup(this);
                    }
                    open = false;
                } else {
                    justOpened = false;
                }
            } else {
                cvContext.uCallKeyPressed(key);
                justOpened = false;
            }   
        }
    }

    /**
     * VirtualKeyboardListener interface
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
		if (tfContext != null) {
			System.out.println("virtualMetaKeyEntered="+metaKey);
			switch (metaKey) {
				case VirtualKeyboard_AWF_F.SEL_COMMAND:
					tfContext.tf.synchSelectionStart();
					vk.select_on = !vk.select_on;
					disp.requestScreenRepaint();
					return;
				case VirtualKeyboard_AWF_F.OK_COMMAND:
					disp.hidePopup(this);
		            open = false;
					break;
				case VirtualKeyboard_AWF_F.CANCEL_COMMAND:
					if (tfContext != null) {
		                tfContext.tf.setString(backupString);
		            }
		            disp.hidePopup(this);
		            open = false;
					break;
				case VirtualKeyboard_AWF_F.INSERT_CHAR_COMMAND:
					eraseSelection();
					int pos = tfContext.tf.getCaretPosition();
					String str=vk.getSelect();
					if(str!=null&&str.length()>0){
						tfContext.tf.insert(str, pos);
						tfContext.tf.getString();
					}
					break;
				case VirtualKeyboard_AWF_F.DEL_CHAR_COMMAND:
					boolean del_more = !vk.select_on;
					eraseSelection();
					if (del_more) {
						tfContext.keyClicked(InputMode.KEYCODE_CLEAR); }
					break;
				case VirtualKeyboard_AWF_F.CPY_COMMAND:
					Clipboard.set(tfContext.tf.getSelection());
					break;
				case VirtualKeyboard_AWF_F.CUT_COMMAND:
					Clipboard.set(tfContext.tf.getSelection());
					eraseSelection();
					break;
				case VirtualKeyboard_AWF_F.PST_COMMAND:
					eraseSelection();
					tfPutString(Clipboard.get(), tfContext);
					break;
				case VirtualKeyboard_AWF_F.NODIFY_IM_CHANGE_COMMAND:
					getNextKeyBoard();
					tfContext.notifyModeChanged();
					break;
				case VirtualKeyboard_AWF_F.CURSOR_UP_COMMAND:
					tfContext.moveCursor(Canvas.UP);
					synchSelectEnd(tfContext.tf);
					break;
			    case VirtualKeyboard_AWF_F.CURSOR_DOWN_COMMAND:
					tfContext.moveCursor(Canvas.DOWN);
					synchSelectEnd(tfContext.tf);
					break;
			    case VirtualKeyboard_AWF_F.CURSOR_LEFT_COMMAND:
					tfContext.moveCursor(Canvas.LEFT);
					synchSelectEnd(tfContext.tf);
					break;
			    case VirtualKeyboard_AWF_F.CURSOR_RIGHT_COMMAND:
					tfContext.moveCursor(Canvas.RIGHT);
					synchSelectEnd(tfContext.tf);
					break;
				default:
					break;
			}
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
    public void paintTextOnly(Graphics g, int width, int height) { }

    public void paintCandidateBar(Graphics g, int width, int height) {
        /** Nothing but a stub */
    }

    public String getTips() {
    	return "L+O: Switch input";
    }

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
					setBounds(aw-w-EPAD, ah-h-EPAD, w, h);
					break;
				case TextFieldLFImpl.QUAD_TOPRGT:
					setBounds(EPAD, ah-h-EPAD, w, h);
					break;
				case TextFieldLFImpl.QUAD_BOTLFT:
					setBounds(aw-w-EPAD, EPAD, w, h);
					break;
				case TextFieldLFImpl.QUAD_BOTRGT:
					setBounds(EPAD, EPAD, w, h);
					break;
				default:
					setBounds(aw-w-EPAD, ah-h-EPAD, w, h); }
			requestFullScreenRepaint(); }

		// Overridden to make sure we get a monitor thread that it
		// monitors the bounds against the content below
		void doThreadJob() {
			setBounds(); }

		boolean needsMonitorThread() {
			return true; }
			
		boolean selectOn() {
			return vk.select_on; }
			
		void setSelectOn(boolean b) {
			vk.select_on = b; }

}
