/*
 * $LastChangedDate: 2006-03-06 01:36:46 +0900 (鞗? 06 3 2006) $  
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
import com.sun.midp.main.Configuration;

/**
 * This is a popup layer that handles a sub-popup within the text tfContext
 * @author Amir Uval
 */
class KeyboardLayer extends PopupLayer implements VirtualKeyboardListener {
    private int neededColumns = 0;
    private int neededRows = 0;

    /** the instance of the virtual keyboard */
    VirtualKeyboard vk = null;

    String layerID = null;

    /**
     * Constructs a text tfContext sub-popup layer, which behaves like a
     * popup-choicegroup, given a string array of elements that constitute
     * the available list of choices to select from.
     *
     * @param tf The TextEditor that triggered this popup layer.
     */
    private KeyboardLayer(TextFieldLFImpl tf) throws VirtualKeyboardException {
        super((Image)null, -1); // don't draw a background  

        this.layerID  = "KeyboardLayer";
        tfContext = tf;
        //backupstring is set to original text before the kbd was used
        backupString = tfContext.tf.getString();
        if (vk==null) {
            vk = new VirtualKeyboard(0, this,getAvailableWidth(),getAvailableHeight());
        }

		setBounds(vk.kbX, vk.kbY, vk.kbWidth, vk.kbHeight);
	
        //candidateBar = new IMCandidateBar(vk.kbWidth, 25);

		Command keyboardClose = new Command("OK", Command.OK, 1);
		Command keyboardHelp = new Command("Help", Command.EXIT, 1);
		Command commads[]={keyboardClose,keyboardHelp};
		setCommands(commads);
    }       

    /**
     * Constructs a canvas sub-popup layer, which behaves like a
     * popup-choicegroup, given a string array of elements that constitute
     * the available list of choices to select from.
     *
     * @param canvas The Canvas that triggered this popup layer.
     */
    private KeyboardLayer(CanvasLFImpl canvas) throws VirtualKeyboardException {
        super((Image)null, -1); // don't draw a background  

        this.layerID  = "KeyboardLayer";
        tfContext = null;
        cvContext = canvas;
        if (vk==null) {
            prepareKeyMapCanvas();
            vk = new VirtualKeyboard(keys, this, 0, 0);
        }

        //System.out.println("vk.kbX:"+vk.kbX+",vk.kbY:"+vk.kbY+",vk.kbWidth:"+vk.kbWidth+",vk.kbHeight:"+vk.kbHeight);
	setBounds(vk.kbX,vk.kbY,vk.kbWidth,vk.kbHeight);

        Command keypadClose = new Command("Close", Command.OK, 1);
        setCommands(new Command[] { keypadClose });
    }       
    /**
     * Singleton
     */
    private static KeyboardLayer instanceTF = null;
    // Save the Y position for the virtual keyboard.
    // For some reason, failing to set the bound again ( in each getInstance )
    // Causes the Y position to change. 
    private static int instanceTF_Y= 0;
    private static KeyboardLayer instanceCV = null;

    /**
     * Command to dismiss the keypad without selection.
     */ 

    /**
     * constants for setState()
     */
    public static final int NUMERIC = 0;
    public static final int LOWERCASE = 1;
    public static final int UPPERCASE = 2;
    public static final int SYMBOL = 3;
	public static final int PINYIN = 4;

	public static final int KEYBOARD_INPUT_NUMERIC = 0;  //NUMERIC only
    public static final int KEYBOARD_INPUT_ASCII = 1;    //ascii only 
    public static final int KEYBOARD_INPUT_ANY = 2;    //any

    private static final boolean[][] isMap = {
     // |NUMERIC|LOWERCASE|UPPERCASE|SYMBOL|PINYIN
        { true,   false,   false,    false, false }, // KEYBOARD_INPUT_NUMERIC
        { true,   true,    true,     false, false }, // KEYBOARD_INPUT_ASCII
        { true,   true,    true,     true,  true } // KEYBOARD_INPUT_ANY 

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
                case KeyboardLayer.NUMERIC:
                    return "1234";
                case KeyboardLayer.LOWERCASE:
                    return "abcd";
                case KeyboardLayer.UPPERCASE:
                    return "ABCD";
                case KeyboardLayer.SYMBOL:
                    return "Symbol";
				case KeyboardLayer.PINYIN:	
					return "Pinyin";
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
    static KeyboardLayer getInstance(TextFieldLFImpl tf)  
                                                           throws VirtualKeyboardException {
        if ((instanceTF == null) || (instanceTF.tfContext != tf)) {
            instanceTF = new KeyboardLayer(tf);
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
    static KeyboardLayer getInstance(CanvasLFImpl canvas) 
                                                           throws VirtualKeyboardException {
        if ((instanceCV == null) || (instanceCV.cvContext != canvas)) {
            instanceCV = new KeyboardLayer(canvas);
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
             try{
	            vk.traverse(type,code);
         	}catch(IllegalArgumentException e){
		         System.out.println(e);	
		 	}catch(NullPointerException e){
			 	System.out.println(e);	
	 		}catch(StringIndexOutOfBoundsException e){
		 		System.out.println(e);	
 			}	 		
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

    /** Indicates if this popup layer is shown (true) or hidden (false). */
    boolean open = false;

    /** 
     * Indicates if the Keyboard layer was just opened or if it is already open.
     */
    boolean justOpened = false;

    /** the original text field string in case the user cancels */
    String backupString;

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
				case VirtualKeyboard.OK_COMMAND:
					disp.hidePopup(this);
		            open = false;
					break;
				case VirtualKeyboard.CANCEL_COMMAND:
					if (tfContext != null) {
		                tfContext.tf.setString(backupString);
		            }
		            disp.hidePopup(this);
		            open = false;
					break;
				case VirtualKeyboard.INSERT_CHAR_COMMAND:
					int pos = tfContext.tf.getCaretPosition();
					String str=vk.getSelect();
					if(str!=null&&str.length()>0){
						tfContext.tf.insert(str, pos);
						tfContext.tf.getString();
					}
					break;
				case VirtualKeyboard.DEL_CHAR_COMMAND:
					tfContext.keyClicked(InputMode.KEYCODE_CLEAR);
					break;
				case VirtualKeyboard.NODIFY_IM_CHANGE_COMMAND:
					getNextKeyBoard();
					tfContext.notifyModeChanged();
					break;
				case VirtualKeyboard.CURSOR_UP_COMMAND:
					tfContext.moveCursor(Canvas.UP);
					break;
			    case VirtualKeyboard.CURSOR_DOWN_COMMAND:
					tfContext.moveCursor(Canvas.DOWN);
					break;
			    case VirtualKeyboard.CURSOR_LEFT_COMMAND:
					tfContext.moveCursor(Canvas.LEFT);
					break;
			    case VirtualKeyboard.CURSOR_RIGHT_COMMAND:
					tfContext.moveCursor(Canvas.RIGHT);
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
    public void paintTextOnly(Graphics g, int width, int height) {
        if (tfContext != null) {
            tfContext.lPaintContent(g, width, height);
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
