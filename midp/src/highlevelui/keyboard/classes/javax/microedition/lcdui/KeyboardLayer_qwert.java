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
import com.pspkvm.midp.lcdui.vk.qwert.IMCandidateBar;


/**
 * This is a popup layer that handles a sub-popup within the text tfContext
 * @author Amir Uval
 */
class KeyboardLayer_qwert extends AbstractKeyboardLayer {
    private int neededColumns = 0;
    private int neededRows = 0;

    /** the instance of the virtual keyboard */
    VirtualKeyboard_qwert vk = null;

    String layerID = null;

    /**
     * Constructs a text tfContext sub-popup layer, which behaves like a
     * popup-choicegroup, given a string array of elements that constitute
     * the available list of choices to select from.
     *
     * @param tf The TextEditor that triggered this popup layer.
     */
    private KeyboardLayer_qwert(TextFieldLFImpl tf) throws VirtualKeyboardException {
        super((Image)null, -1); // don't draw a background  

        this.layerID  = "KeyboardLayer";
        tfContext = tf;
        //backupstring is set to original text before the kbd was used
        backupString = tfContext.tf.getString();
        if (vk==null) {
            prepareKeyMapTextField(getAvailableWidth(), getAvailableHeight());
            vk = new VirtualKeyboard_qwert(keys, this, true, neededColumns, neededRows);
        }

	setBounds(vk.kbX, vk.kbY-25, vk.kbWidth, vk.kbHeight + 25);
	
        //candidateBar = new IMCandidateBar(vk.kbWidth, 25);

        Command keyboardClose = new Command("Close", Command.OK, 1);
        setCommands(new Command[] { keyboardClose });
    }       

    /**
     * Constructs a canvas sub-popup layer, which behaves like a
     * popup-choicegroup, given a string array of elements that constitute
     * the available list of choices to select from.
     *
     * @param canvas The Canvas that triggered this popup layer.
     */
    private KeyboardLayer_qwert(CanvasLFImpl canvas) throws VirtualKeyboardException {
        super((Image)null, -1); // don't draw a background  

        this.layerID  = "KeyboardLayer";
        tfContext = null;
        cvContext = canvas;
        if (vk==null) {
            prepareKeyMapCanvas();
            vk = new VirtualKeyboard_qwert(keys, this, false, 0, 0);
        }

        //System.out.println("vk.kbX:"+vk.kbX+",vk.kbY:"+vk.kbY+",vk.kbWidth:"+vk.kbWidth+",vk.kbHeight:"+vk.kbHeight);
	setBounds(vk.kbX,vk.kbY,vk.kbWidth,vk.kbHeight);

        Command keypadClose = new Command("Close", Command.OK, 1);
        setCommands(new Command[] { keypadClose });
    }       
    /**
     * Singleton
     */
    private static KeyboardLayer_qwert instanceTF = null;
    // Save the Y position for the virtual keyboard.
    // For some reason, failing to set the bound again ( in each getInstance )
    // Causes the Y position to change. 
    private static int instanceTF_Y= 0;
    private static KeyboardLayer_qwert instanceCV = null;

    /**
     * Command to dismiss the keypad without selection.
     */ 

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
        if (vk != null) { 
            switch (state) {
            	case KEYBOARD_INPUT_NUMERIC:
                  vk.currentKeyboard = NUMERIC;
                  break;
              case KEYBOARD_INPUT_ASCII:
              case KEYBOARD_INPUT_ANY:
                  vk.currentKeyboard = LOWERCASE;
                  break;
            }
        }
    }

    public int getState() {
        if (vk != null) {            
            return vk.currentKeyboard;
        }
        return -1;
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
    static KeyboardLayer_qwert getInstance(TextFieldLFImpl tf)  
                                                           throws VirtualKeyboardException {
        if ((instanceTF == null) || (instanceTF.tfContext != tf)) {
            instanceTF = new KeyboardLayer_qwert(tf);
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
    static KeyboardLayer_qwert getInstance(CanvasLFImpl canvas) 
                                                           throws VirtualKeyboardException {
        if ((instanceCV == null) || (instanceCV.cvContext != canvas)) {
            instanceCV = new KeyboardLayer_qwert(canvas);
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
     * Sets the bounds of the popup layer.
     *
     * @param x the x-coordinate of the popup layer location
     * @param y the y-coordinate of the popup layer location
     * @param w the width of this popup layer in open state
     * @param h the height of this popup layer in open state
     */
    public void setBounds(int x, int y, int w, int h) {
        super.setBounds(x, y, w, h);
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

        if ((type == EventConstants.PRESSED ||
             type == EventConstants.RELEASED ||
             type == EventConstants.REPEATED) && 
            (tfContext != null || 
             cvContext != null)) {
            vk.traverse(type,code);
        }
        return true;
    }


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

    /** Candidate bar */
    IMCandidateBar candidateBar = null;

    /** the original text field string in case the user cancels */
    String backupString;

    /** the list of available keys */
    char[][] keys = null;


    private void prepareKeyMapTextFieldFull() {
    	 System.out.println("prepareKeyMapTextFieldFull");
        keys = new char[4][];

        // numerals
        keys[0] = new char[23]; // numerals
        for (char i=0; i<10; i++) {  // 0..9
            keys[0][i] = (char)(i+48);
        }
        keys[0][10] = '=';
        keys[0][11] = '+';
        keys[0][12] = '-';
        keys[0][13] = '*';
        keys[0][14] = '/';
        keys[0][15] = '.';
        keys[0][16] = ',';
        keys[0][17] = '$';
        keys[0][18] = '%';
        keys[0][19] = '^';
        keys[0][20] = '#';
        keys[0][21] = '_';
        keys[0][22] = ' ';

        // Roman, lower case
        keys[1] = new char[51]; // numerals
        keys[1][0] = 'q';
        keys[1][1] = 'w';
        keys[1][2] = 'e';
        keys[1][3] = 'r';
        keys[1][4] = 't';
        keys[1][5] = 'y';
        keys[1][6] = 'u';
        keys[1][7] = 'i';
        keys[1][8] = 'o';
        keys[1][9] = 'p';
        keys[1][10] = '1';
        keys[1][11] = '2';
        keys[1][12] = '3';
        keys[1][13] = '0';
        keys[1][14] = '!';
        keys[1][15] = '@';
        keys[1][16] = '#';
        keys[1][17] = 'a';
        keys[1][18] = 's';
        keys[1][19] = 'd';
        keys[1][20] = 'f';
        keys[1][21] = 'g';
        keys[1][22] = 'h';
        keys[1][23] = 'j';
        keys[1][24] = 'k';
        keys[1][25] = 'l';
        keys[1][26] = ';';
        keys[1][27] = '4';
        keys[1][28] = '5';
        keys[1][29] = '6';
        keys[1][30] = '_';
        keys[1][31] = '$';
        keys[1][32] = '%';
        keys[1][33] = '^';
        keys[1][34] = 'z';
        keys[1][35] = 'x';
        keys[1][36] = 'c';
        keys[1][37] = 'v';
        keys[1][38] = 'b';
        keys[1][39] = 'n';
        keys[1][40] = 'm';
        keys[1][41] = ',';
        keys[1][42] = '.';
        keys[1][43] = ' ';// space
        keys[1][44] = '7';
        keys[1][45] = '8';
        keys[1][46] = '9';
        keys[1][47] = '?';
        keys[1][48] = '&';
        keys[1][49] = '*';
        keys[1][50] = '/';
        
        // Roman, upper case
        keys[2] = new char[51]; // numerals
        keys[2][0] = 'Q';
        keys[2][1] = 'W';
        keys[2][2] = 'E';
        keys[2][3] = 'R';
        keys[2][4] = 'T';
        keys[2][5] = 'Y';
        keys[2][6] = 'U';
        keys[2][7] = 'I';
        keys[2][8] = 'O';
        keys[2][9] = 'P';
        keys[2][10] = '1';
        keys[2][11] = '2';
        keys[2][12] = '3';
        keys[2][13] = '0';
        keys[2][14] = '!';
        keys[2][15] = '@';
        keys[2][16] = '#';
        keys[2][17] = 'A';
        keys[2][18] = 'S';
        keys[2][19] = 'D';
        keys[2][20] = 'F';
        keys[2][21] = 'G';
        keys[2][22] = 'H';
        keys[2][23] = 'J';
        keys[2][24] = 'K';
        keys[2][25] = 'L';
        keys[2][26] = ';';
        keys[2][27] = '4';
        keys[2][28] = '5';
        keys[2][29] = '6';
        keys[2][30] = '_';
        keys[2][31] = '$';
        keys[2][32] = '%';
        keys[2][33] = '^';
        keys[2][34] = 'Z';
        keys[2][35] = 'X';
        keys[2][36] = 'C';
        keys[2][37] = 'V';
        keys[2][38] = 'B';
        keys[2][39] = 'N';
        keys[2][40] = 'M';
        keys[2][41] = ',';
        keys[2][42] = '.';
        keys[2][43] = ' ';// space
        keys[2][44] = '7';
        keys[2][45] = '8';
        keys[2][46] = '9';
        keys[2][47] = '?';
        keys[2][48] = '&';
        keys[2][49] = '*';
        keys[2][50] = '/';

        // Symbol
        keys[3] = new char[25]; // numerals
        for (char i=0; i<15; i++) {  // !../
            keys[3][i] = (char)(i+33);
        }
        for (char i=0; i<7; i++) {  // :..@
            keys[3][i+15] = (char)(i+58);
        }
        keys[3][22] = '~'; // space
        keys[3][23] = '^'; // space
        keys[3][24] = ' '; // space

        neededColumns = 17;
        neededRows = 3;
    }


    private void prepareKeyMapTextFieldMed() {
    	System.out.println("prepareKeyMapTextFieldMed");
        keys = new char[4][];

        // numerals
        keys[0] = new char[23]; // numerals
        for (char i=0; i<10; i++) {  // 0..9
            keys[0][i] = (char)(i+48);
        }
        keys[0][10] = '=';
        keys[0][11] = '+';
        keys[0][12] = '-';
        keys[0][13] = '*';
        keys[0][14] = '/';
        keys[0][15] = '.';
        keys[0][16] = ',';
        keys[0][17] = '$';
        keys[0][18] = '%';
        keys[0][19] = '^';
        keys[0][20] = '#';
        keys[0][21] = '_';
        keys[0][22] = ' ';

        // Roman, lower case
        keys[1] = new char[30]; // numerals
        keys[1][0] = 'q';
        keys[1][1] = 'w';
        keys[1][2] = 'e';
        keys[1][3] = 'r';
        keys[1][4] = 't';
        keys[1][5] = 'y';
        keys[1][6] = 'u';
        keys[1][7] = 'i';
        keys[1][8] = 'o';
        keys[1][9] = 'p';
        keys[1][10] = 'a';
        keys[1][11] = 's';
        keys[1][12] = 'd';
        keys[1][13] = 'f';
        keys[1][14] = 'g';
        keys[1][15] = 'h';
        keys[1][16] = 'j';
        keys[1][17] = 'k';
        keys[1][18] = 'l';
        keys[1][19] = ';';
        keys[1][20] = 'z';
        keys[1][21] = 'x';
        keys[1][22] = 'c';
        keys[1][23] = 'v';
        keys[1][24] = 'b';
        keys[1][25] = 'n';
        keys[1][26] = 'm';
        keys[1][27] = ',';
        keys[1][28] = '.';
        keys[1][29] = ' '; // space

        // Roman, upper case
        keys[2] = new char[30]; // numerals
        keys[2][0] = 'Q';
        keys[2][1] = 'W';
        keys[2][2] = 'E';
        keys[2][3] = 'R';
        keys[2][4] = 'T';
        keys[2][5] = 'Y';
        keys[2][6] = 'U';
        keys[2][7] = 'I';
        keys[2][8] = 'O';
        keys[2][9] = 'P';
        keys[2][10] = 'A';
        keys[2][11] = 'S';
        keys[2][12] = 'D';
        keys[2][13] = 'F';
        keys[2][14] = 'G';
        keys[2][15] = 'H';
        keys[2][16] = 'J';
        keys[2][17] = 'K';
        keys[2][18] = 'L';
        keys[2][19] = ';';
        keys[2][20] = 'Z';
        keys[2][21] = 'X';
        keys[2][22] = 'C';
        keys[2][23] = 'V';
        keys[2][24] = 'B';
        keys[2][25] = 'N';
        keys[2][26] = 'M';
        keys[2][27] = ',';
        keys[2][28] = '.';
        keys[2][29] = ' '; // space

        // Symbol
        keys[3] = new char[25]; // numerals
        for (char i=0; i<15; i++) {  // !../
            keys[3][i] = (char)(i+33);
        }
        for (char i=0; i<7; i++) {  // :..@
            keys[3][i+15] = (char)(i+58);
        }
        keys[3][22] = '~'; // space
        keys[3][23] = '^'; // space
        keys[3][24] = ' '; // space

        neededColumns = 10;
        neededRows = 3;
    }

    
    private void prepareKeyMapTextFieldSmall() {
    	System.out.println("prepareKeyMapTextFieldSmall");
        keys = new char[4][];

        // numerals
        keys[0] = new char[23]; // numerals
        for (char i=0; i<10; i++) {  // 0..9
            keys[0][i] = (char)(i+48);
        }
        keys[0][10] = '=';
        keys[0][11] = '+';
        keys[0][12] = '-';
        keys[0][13] = '*';
        keys[0][14] = '/';
        keys[0][15] = '.';
        keys[0][16] = ',';
        keys[0][17] = '$';
        keys[0][18] = '%';
        keys[0][19] = '^';
        keys[0][20] = '#';
        keys[0][21] = '_';
        keys[0][22] = ' ';

        // Roman, lower case
        keys[1] = new char[27]; // numerals
        for (char i=0; i<26; i++) {  // a..z
            keys[1][i] = (char)(i+97);
        }
        keys[1][26] = ' '; // space

        // Roman, upper case
        keys[2] = new char[27]; // numerals
        for (char i=0; i<26; i++) {  // A..Z
            keys[2][i] = (char)(i+65);
        }
        keys[2][26] = ' '; // space

        // Symbol
        keys[3] = new char[25]; // numerals
        for (char i=0; i<15; i++) {  // !../
            keys[3][i] = (char)(i+33);
        }
        for (char i=0; i<7; i++) {  // :..@
            keys[3][i+15] = (char)(i+58);
        }
        keys[3][22] = '~'; // space
        keys[3][23] = '^'; // space
        keys[3][24] = ' '; // space

        neededColumns = 0;
        neededRows = 0;
    }
    
    /**
     * Prepare key map
     */
    private void prepareKeyMapTextField(int w, int h) throws VirtualKeyboardException{
        System.out.println("prepareKeyMapTextField:"+w+","+h);
        if (w >= 470) {
        	prepareKeyMapTextFieldFull();
        } else if (w >= 230) {
        	prepareKeyMapTextFieldMed();
        } else if (w >= 166) {
        	prepareKeyMapTextFieldSmall();
        } else {
        	throw new VirtualKeyboardException("Screen's too small to display virtual keyboard");
        }
    }

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


         /*for (char i=0; i<4; i++) {   
             keys[0][i] = (char)(65+i);
         }*/
         /*
        for (char i=4, j=0; i<14; i++, j++) {  // 0..9
             keys[0][i] = (char)(j+48);
         }
         keys[0][14] = '*';
         keys[0][15] = '#';              
         */

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

        
            
        if (tfContext != null) {	    
            if (type != EventConstants.RELEASED) {
            	boolean processed = false;
            	if (candidateBar != null) {
            	    processed = candidateBar.keyPressed(c);
            	}

            	if (!processed) {
            			eraseSelection();
                  tfContext.uCallKeyPressed(c);
                  tfContext.tf.getString();
                  return;
              }
              repaintVK();
            }              
        } else if (cvContext != null) {

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
	 *	Helper for various actions that move the cursor
	 *	without changing the select state
	 */		 		 		
		void synchSelectEnd(TextField tf) {
			if (!vk.select_active) {
				return; }
			tf.synchSelectionEnd(); }

		/**
		 *	Helper for various actions that erase the current selection
		 */
		void eraseSelection() {
			if (!vk.select_active) {
				return; }
			if (tfContext==null) {
				return; }
			vk.select_active=false;
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


        if (tfContext != null && candidateBar != null) {
            switch (metaKey) {
                  case VirtualKeyboard_qwert.CNINPUT_SELECT_META_KEY:
            	      int pos = tfContext.tf.getCaretPosition();
            	      tfContext.tf.insert(candidateBar.getSelected(), pos);
            	      tfContext.tf.getString();
            	      candidateBar.reset();
            	      break;
            	    case VirtualKeyboard_qwert.CURSOR_UP_META_KEY:
            	      candidateBar.prevPage();
            	    break;
            	    case VirtualKeyboard_qwert.CURSOR_LEFT_META_KEY:
            	      candidateBar.prevChar();
            	      break;
            	    case VirtualKeyboard_qwert.CURSOR_RIGHT_META_KEY:
            	      candidateBar.nextChar();
            	      break;
            	    case VirtualKeyboard_qwert.CURSOR_DOWN_META_KEY:
            	      candidateBar.nextPage();
            	      break;
             }
             repaintVK();
        } else if (tfContext != null) {
            switch (metaKey) {
            	    case VirtualKeyboard_qwert.CURSOR_LEFT_META_KEY:
            	      tfContext.moveCursor(Canvas.LEFT);
            	      synchSelectEnd(tfContext.tf);
            	      disp.requestScreenRepaint();
            	      break;
            	    case VirtualKeyboard_qwert.CURSOR_RIGHT_META_KEY:
            	      tfContext.moveCursor(Canvas.RIGHT);
            	      synchSelectEnd(tfContext.tf);
            	      disp.requestScreenRepaint();
            	      break;
             }
             repaintVK();
        }

        if (metaKey == vk.OK_META_KEY) {
            // ok   
            disp.hidePopup(this);
            open = false;

        } else if (metaKey == vk.CANCEL_META_KEY) {
            // cancel
            if (tfContext != null) {
                tfContext.tf.setString(backupString);
            }
            disp.hidePopup(this);
            open = false;

        } else if (metaKey == vk.BACKSPACE_META_KEY) {
        		
            if (candidateBar != null) {
            	  candidateBar.backspace();
            } else if (tfContext != null) {
            		boolean del_more = !vk.select_active;
            		eraseSelection();
                if (del_more) { 
									tfContext.keyClicked(InputMode.KEYCODE_CLEAR); }
            }
        } else if (metaKey == vk.SEL_META_KEY) {
        	if (tfContext != null) {
        		tfContext.tf.synchSelectionStart();
        		vk.select_active = !vk.select_active; }

        } else if (metaKey == vk.CPY_META_KEY) {
        	if (tfContext != null) {
        		Clipboard.set(tfContext.tf.getSelection()); }
        		
        } else if (metaKey == vk.PST_META_KEY) {
        	if (tfContext != null) {
        		eraseSelection();
        		tfPutString(Clipboard.get(), tfContext); }

        } else if (metaKey == vk.IM_CHANGED_KEY) {
            if (tfContext != null) {
                 tfContext.notifyModeChanged();
            }
        } else if (metaKey == vk.CNINPUT_META_KEY) {
            if (candidateBar == null) {
            	candidateBar = new IMCandidateBar(vk.kbWidth, 25);
            	vk.candidateFieldHeight = 25;
            	setBounds(vk.kbX, vk.kbY-25, vk.kbWidth, vk.kbHeight + 25);
            	disp.requestScreenRepaint();
            } else {
              candidateBar = null;
              vk.candidateFieldHeight = 0;
              setBounds(vk.kbX, vk.kbY-25, vk.kbWidth, vk.kbHeight);
              disp.requestScreenRepaint();
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
    public void paintTextOnly(Graphics g, int width, int height) {
        if (tfContext != null) {
            tfContext.lPaintContent(g, width, height);
        }        
    }

    public void paintCandidateBar(Graphics g, int width, int height) {
        if (candidateBar != null) {
            candidateBar.paint(g, width, height);
        }
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
        requestRepaint();
    }

}
