/**
 * /AJ Milne--virtual keyboard 'meta' class--provides much of extended
 * functionality for the 'wheel' virtual keyboards with rich editing
 * functions--done as superclass to avoid duplicated code. 
 *  
 * NOTE that the TextField, TextFieldLFImpl and TextBoxLFImpl classes within
 * the VM provided within the PSPKVM implement supersets of the standard interfaces,
 * in order to provide selection handling within text fields, and this layer
 * depends on these additions to the interface.
 *       
 */

package javax.microedition.lcdui;

import com.sun.midp.lcdui.*;
import com.sun.midp.chameleon.input.*;
import com.sun.midp.chameleon.MIDPWindow;
import com.sun.midp.chameleon.CWindow;

/**
 * This is a popup layer that handles a sub-popup within the text tfContext
 * @author AJ Milne, based on originals by Amir Uval
 */
abstract class KeyboardLayer_RichEditing extends AbstractKeyboardLayer {

	/** State--tracks the quadrant we're drawing the board in -- we move it to
	 * keep it in the quadrant furthest from the caret at all times
	 */
	int current_quad;	 		

	// Passthrough constructor
	KeyboardLayer_RichEditing(Image bgImage, int bgColor) {
		super(bgImage, bgColor); }

    /**
     * Sets the bounds of the popup layer.
     *
     */
    protected void setBounds() {
    	setBounds(false); }

		/**
		 * Sets the bounds of the popup layer. Set schange
		 * to indicate the size of the layer has changed
		 * and you need to force this operation.
		 * Otherwise, it only moves the board if the caret
		 * has moved to a new quadrant.		 		 		  		 
		 *
		 */
		protected void setBounds(boolean schange) {
			int upd_caret_quad = getCaretQuad();
			if ((upd_caret_quad == current_quad) && (!schange)) {
				return; }
			current_quad = upd_caret_quad;
			int w = getVKWidth();
			int h = getVKHeight();
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

		abstract int getVKHeight();
		abstract int getVKWidth();

		boolean cached_ok, cached_cancel;
		
		void clearCachedCommands() {
			cached_ok=false;
			cached_cancel=false; }

    void handleCachedCommand() {
    	if (cached_ok) {
				if (ok_below!=null) {
					closeKeyEntered(true);
					ok_below.reflect();
					return; }
				closeKeyEntered(true);
				return; }
			// cached_cancel
			if (cancel_below!=null) {
				closeKeyEntered(true);
				cancel_below.reflect();
				return; }
			closeKeyEntered(false); }

		/**
		 * Called to combine the two characters before the cursor into
		 * a ligature
		 * @param d the diacritical key pressed (from SC_Keys ... GRV et al)
		 */		 		 
		void addLigature() {
			if (tfContext == null) {
				return; }
			int p = tfContext.tf.getCaretPosition();
			if (p<=1) {
				// You have to be pointed just after 
				// the characters you're going to combine
				return; }
			char a = tfContext.tf.getString().charAt(p-2);
			char b = tfContext.tf.getString().charAt(p-1);
			char c = Diacriticals.getLigature(a, b);
			if (c==Diacriticals.NOLIGATURE) {
				return; }
			// Delete the existing characters, place the ligature in:
			tfContext.keyClicked(InputMode.KEYCODE_CLEAR);
			tfContext.keyClicked(InputMode.KEYCODE_CLEAR);
			tfPutKey(c); }

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
			char c = Diacriticals.getDiacritical(a, d);
			tfContext.keyClicked(InputMode.KEYCODE_CLEAR);
			tfPutKey(c); }

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
				cached_cancel=true;
        return;
			case SC_Keys.ENT: 
				eraseSelection();
				tfPutString("\n", tfContext);
				return;
			case SC_Keys.OK:
				cached_ok=true;
				return;
      case SC_Keys.CLF:
        tfContext.moveCursor(Canvas.LEFT);
        synchSelectEnd(tfContext.tf);
        return;
      case SC_Keys.WLF:
      	TextFieldNav.wordLeft(tfContext);
        synchSelectEnd(tfContext.tf);
        return;
      case SC_Keys.CRT:
				tfContext.moveCursor(Canvas.RIGHT);
        synchSelectEnd(tfContext.tf);
        return;
      case SC_Keys.WRT:
      	TextFieldNav.wordRight(tfContext);
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
      case SC_Keys.PUP:
      	tfContext.setCaretPosition(0);
      	tfContext.lRequestPaint();
      	synchSelectEnd(tfContext.tf);
      	return;
      case SC_Keys.PDN:
      	tfContext.setCaretPosition(tfContext.tf.buffer.length());
      	tfContext.lRequestPaint();
      	synchSelectEnd(tfContext.tf);
      	return;
      case SC_Keys.HME:
      	TextFieldNav.paraLeft(tfContext);
      	synchSelectEnd(tfContext.tf);
      	return;
      case SC_Keys.END:
      	TextFieldNav.paraRight(tfContext);
      	synchSelectEnd(tfContext.tf);
      	return;
      case SC_Keys.BSP: {
      	boolean del_more = !selectOn();
				eraseSelection();
				if (!del_more) {
					return; }
        tfContext.keyClicked(InputMode.KEYCODE_CLEAR);
				return; }
      case SC_Keys.DEL: {
      	boolean del_more = !selectOn();
				eraseSelection();
				if (!del_more) {
					return; }
				tfContext.moveCursor(Canvas.RIGHT);
        tfContext.keyClicked(InputMode.KEYCODE_CLEAR);
				return; }
			case SC_Keys.SEL:
				tfContext.tf.synchSelectionStart();
				setSelectOn(!selectOn());
				disp.requestScreenRepaint();
				return;
			case SC_Keys.GRV:
			case SC_Keys.ACU:
			case SC_Keys.CIR:
			case SC_Keys.TIL:
			case SC_Keys.DIA:
			case SC_Keys.RIN:
			case SC_Keys.CED:
			case SC_Keys.BRV:
			case SC_Keys.MCR:
			case SC_Keys.STR:
			case SC_Keys.CAR:
			case SC_Keys.OGO:
			case SC_Keys.MDT:
			case SC_Keys.UDT:
				addDiacritical(m);
				return;
			case SC_Keys.LIG:
				addLigature();
				return;
      default:
      	return; } }        	

    /**
     * paint text only
     * 
     * @param g The graphics context to paint to
     * @param width width of screen to paint
     * @param height height of screen to paint
     */
    public void paintTextOnly(Graphics g, int width, int height) { }

    public void paintCandidateBar(Graphics g, int width, int height) { }

    /**
     *	Overridden to clear the 
     *	cached commands     
     */
		public void addNotify() {
			clearCachedCommands();
			super.addNotify(); }

}
