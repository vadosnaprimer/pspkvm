package javax.microedition.lcdui;

import com.sun.midp.chameleon.layers.PopupLayer;
import com.sun.midp.main.Configuration;

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
    	super(bgImage, bgColor);
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
    abstract public void setState(int state);
    
    abstract public int getState();

    abstract public int getHeight();

    static AbstractKeyboardLayer getVKInstance(TextFieldLFImpl tf)  
                                                           throws VirtualKeyboardException {
        String im = Configuration.getProperty("com.pspkvm.inputmethod");
        if(im != null && im.equals("vk-qwert")){
            return KeyboardLayer_qwert.getInstance(tf);
        } else {
            return KeyboardLayer_awf.getInstance(tf);
        }
    }

    /**
     * get Canvas Keyboard layer instance
     * 
     * @param canvas Canvas look/feel instance for the keypad layer
     * @return a KeyboardLayer instance.
     */
    static AbstractKeyboardLayer getVKInstance(CanvasLFImpl canvas) 
                                                           throws VirtualKeyboardException {
        String im = Configuration.getProperty("com.pspkvm.inputmethod");
        if(im != null && im.equals("vk-qwert")){
            return KeyboardLayer_qwert.getInstance(canvas);
        } else {
            return KeyboardLayer_awf.getInstance(canvas);
        }
    }
}
