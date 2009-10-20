/*
 * $LastChangedDate: 2005-11-21 02:11:20 +0900 (\u9797? 21 11 2005) $  
 *
 * Copyright  1990-2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.microedition.lcdui;

import com.sun.midp.lcdui.*;
import com.sun.midp.configurator.Constants;
import com.sun.midp.chameleon.skins.*;
import com.sun.midp.chameleon.layers.PopupLayer;
import com.pspkvm.midp.lcdui.vk.awf.IMCandidateBar;
import com.pspkvm.system.VMSettings;

/*
	Modified/floated AWF version
	/AJM modified from AWF's original
*/

class VirtualKeyboard_AWF_F extends VirtualKeyboardInterface {

		/** instance of the virtual keyboard listener */
		VirtualKeyboardListener vkl;
		
		// keyboard dimensions
		int kbX, kbY, kbWidth, kbHeight;
		static int kbLayout=0; //W>H or H>W
		
		int fontW;       // width of 'M'
		int fontH;       // height of 'M'
		int buttonW;     // width of keyboard
		int buttonH;     // height of keyboard
		int fontWCenter; // placement of text inside button
		int fontHTop;    // placement of text inside button
		int maxRows;     // horizontal keyboard rows
		int maxColumns;  // vertical keyboard columns
		int fullColumns; // number of columns that are completely full with keys
		
		int currentChar = 0;
		int currentKeyboard = 1; // abc
		
		char itemIndexWhenPressed;
		char PRESS_OUT_OF_BOUNDS = 0;
		
		static final int DISP_W = 200;
		static final int DISP_H = 160;
		
		/** array of all available keys n the keyboard */
		char[][] keys;
		Font f;
		
		boolean isShowHelp=false;
		
		int mainKeyOffsetX=5;
		int mainKeyOffsetY=5;
		
		boolean select_on;
		
		/** Candidate bar */
		IMCandidateBar candidateBar = new IMCandidateBar(20,25);
		
	// Bounds
	void setBounds() {
		kbWidth = DISP_W;
		kbHeight = DISP_H;
		kbX = vkl.getAvailableWidth()-kbWidth;
		kbY = vkl.getAvailableHeight()-kbHeight; }

	/**
	* Virtual Keyboard constructor.
	* 
	* @param keys array of available keys for the keyboard
	* @param vkl the virtual keyboard listener
	* @param displayTextArea flag to indicate whether to display the text area
	*/
	public VirtualKeyboard_AWF_F(char[][] keys, 
		VirtualKeyboardListener vkl,
		int neededColumns, int neededRows) throws VirtualKeyboardException {
		
		if ("on".equals(VMSettings.get("com.pspkvm.virtualkeyboard.autoopen"))) {
			USE_VIRTUAL_KEYBOARD_OPEN_AUTO = true; }
		
		select_on=false;
		PADDING = 2;
		currentKeyboard = 0;
		this.vkl = vkl;
		setBounds();
		
		f = Font.getFont(Font.FACE_SYSTEM, // or SYSTEM
		Font.STYLE_PLAIN, 
		Font.SIZE_SMALL);
		fontW = f.charWidth('M');
		fontH = f.getHeight();
		
		buttonW = fontW * 3;
		buttonH = fontH * 3;
		fontHTop = (buttonH - fontH) / 2 ;
		fontWCenter = buttonW / 2;
		
		maxRows = (kbHeight - PADDING) / (buttonH + PADDING);
		
		maxColumns = 7;  //verify         
		// kbWidth = maxColumns * (buttonW + PADDING) + PADDING + 1;
		kbX = 0;
		
		if (neededRows == 0) {
			int tmpMax = 0; // will hold the longest keyboard.
			for (int i=0; i<keys.length; i++) {
				if (tmpMax < keys[i].length) {
					tmpMax = keys[i].length; } }
			neededRows = (tmpMax + maxColumns - 1) / maxColumns; }
		if (neededRows > maxRows) {
			System.err.println("Keys list is too long for this size of screen.");
			System.err.println("Please split your keyboard array to multiple arrays.");
			throw new VirtualKeyboardException("Keys list is too long for this size of screen."); }
		maxRows = neededRows;
		int neededHeight=0;
	
		neededHeight = maxRows * (buttonH + PADDING) +
			3 * PADDING;
		kbY = vkl.getAvailableHeight()-neededHeight;
	
		this.keys = keys;
		
		currentKeyboard = 0;
		fullColumns = keys[currentKeyboard].length / maxColumns; }

  public VirtualKeyboard_AWF_F(int keyboardType,
		  	VirtualKeyboardListener vkl) throws VirtualKeyboardException {
    if ("on".equals(VMSettings.get("com.pspkvm.virtualkeyboard.autoopen"))) {
           USE_VIRTUAL_KEYBOARD_OPEN_AUTO = true; }

		select_on=false;
		
		f = Font.getFont(Font.FACE_SYSTEM, // or SYSTEM
			Font.STYLE_PLAIN, 
			Font.SIZE_MEDIUM);
		fontW = f.charWidth('M');
		fontH = f.getHeight();
		
		PADDING = 1;
		this.vkl = vkl;
		setBounds();
		
		prepareKeyMap(); }

    int transfDirectionKeyCode(int keyCode){
		int retKeyCode=keyCode;
			switch(keyCode){
					case Constants.KEYCODE_RIGHT:
						retKeyCode=Canvas.KEY_NUM6;//<-
						break;
					case Constants.KEYCODE_LEFT:
						retKeyCode=Canvas.KEY_NUM4;
						break;
					case Constants.KEYCODE_UP:
						retKeyCode=Canvas.KEY_NUM2;
						break;
					case Constants.KEYCODE_DOWN:
						retKeyCode=Canvas.KEY_NUM8;
						break;
					case Canvas.KEY_NUM2:	
						retKeyCode=Constants.KEYCODE_UP;
						break;
					case Canvas.KEY_NUM4:	
						retKeyCode=Constants.KEYCODE_LEFT;
						break;
					case Canvas.KEY_NUM6:	
						retKeyCode=Constants.KEYCODE_RIGHT;
						break;
					case Canvas.KEY_NUM8:	
						retKeyCode=Constants.KEYCODE_DOWN;
						break;			
				}
		return retKeyCode;
	}
    /**
     * traverse the virtual keyboard according to key pressed.
     * 
     * @param type type of keypress
     * @param keyCode key code of key pressed
     */
    void traverse(int type, int keyCode) {
    
	    System.out.println("VirtualK: keyCode="+keyCode);
		if(isShowHelp){//\u4efb\u610f\u952e\u9000\u51fa
			if((keyCode!=EventConstants.SOFT_BUTTON1)
				&&!(EventConstants.SOFT_BUTTON2==keyCode&&type==EventConstants.PRESSED)){
				isShowHelp=false;
				vkl.repaintVK();
				return;
			}
		}
		if(type==EventConstants.RELEASED){
			switch(keyCode){
				case EventConstants.SOFT_BUTTON2:	
					vkl.virtualMetaKeyEntered(OK_COMMAND);
					break;
				case EventConstants.SOFT_BUTTON1:
					isShowHelp=!isShowHelp;
					vkl.repaintVK();
					break;
				default:
					break;
				}	
		}else if(EventConstants.RELEASED!=type){
			if("on".equals(VMSettings.get("com.pspkvm.virtualkeyboard.direction"))){
				//\u5207\u6362\u6447\u6746\u548c\u65b9\u5411\u952e\u7684\u4f5c\u7528
				keyCode=transfDirectionKeyCode(keyCode);
			}
			
			switch(keyCode){
				case Constants.KEYCODE_RIGHT:
				case Constants.KEYCODE_LEFT:
				case Constants.KEYCODE_UP:
				case Constants.KEYCODE_DOWN:
					handleDirectionKey(type,keyCode);
					break;
				case Canvas.KEY_NUM1: //D 
					handleSelectKey(0);
					break;
				case Canvas.KEY_NUM3: //A
					handleSelectKey(1);
					break;
				case Constants.KEYCODE_SELECT: //O
					handleSelectKey(2);
				    break;
				 case Canvas.KEY_NUM0:     //x
				 	handleSelectKey(3);
					break;
				case Canvas.KEY_NUM5://add key7 change input mode
				case Canvas.KEY_STAR://"*" KEY change input mode
					{
						int oldKb=currentKeyboard;
						vkl.virtualMetaKeyEntered(NODIFY_IM_CHANGE_COMMAND);
						if(oldKb!=currentKeyboard){
							prepareKeyMap();
						}
					}
	    	        break;
				case Canvas.KEY_NUM4://<-
					vkl.virtualMetaKeyEntered(CURSOR_LEFT_COMMAND);
					break;
				case Canvas.KEY_NUM6://->
					vkl.virtualMetaKeyEntered(CURSOR_RIGHT_COMMAND);
					break;
				case Canvas.KEY_NUM2://^
					vkl.virtualMetaKeyEntered(CURSOR_UP_COMMAND);
					break;
				case Canvas.KEY_NUM8://V
					vkl.virtualMetaKeyEntered(CURSOR_DOWN_COMMAND);
					break;
				default:
					if (EventConstants.SYSTEM_KEY_CLEAR ==//for c KEY \u589e\u52a0\u5220\u9664\u5feb\u6377\u952e
							KeyConverter.getSystemKey(keyCode)) {
						vkl.virtualMetaKeyEntered(DEL_CHAR_COMMAND);
					}
					break;
			}
			vkl.repaintVK();
		}
 	
    }

   void handleSelectKey(int key){
	   	System.out.println("handleSelectKey->key="+key);
		if(currentKeyboard==AbstractKeyboardLayer.PINYIN){
			int cKM=VirtualKeyboardMap_F.getActiveKeyMap();
			if(key>=3){//clear
				if(kmap[0].getInputKey().length()>0){
					kmap[0].setInputKeyClearOne();
					VirtualKeyboardMap_F.setActiveKeyMap(0);
					kmap[1].setKeyName(candidateBar.findCandidatePY(kmap[0].getInputKey()));
					kmap[2].setKeyName(candidateBar.findCandidateHZ(kmap[1].getKeyName(0)));
				}else{
					vkl.virtualMetaKeyEntered(DEL_CHAR_COMMAND);
				}
			}else{
				int ck=kmap[cKM].getActiveKey();
				kmap[cKM].setActiveKey(ck/3*3+key);
				if(cKM==0){
					if(kmap[cKM].getActiveKey()!=0){
						String str[]=candidateBar.findCandidatePY(kmap[0].getInputKey()+(kmap[0].getActiveKey()+1));
						if(str!=null){
							kmap[0].setInputKey();
							kmap[1].setKeyName(str);
							kmap[1].setActiveKey(0);
							kmap[2].setKeyName(candidateBar.findCandidateHZ(kmap[1].getKeyName(0)));
							kmap[2].setActiveKey(0);
						}
					}else{
						//vkl.virtualMetaKeyEntered(NODIFY_IM_CHANGE_COMMAND);
						//prepareKeyMap();
						VirtualKeyboardMap_F.setActiveKeyMap(2);
						kmap[2].setActiveKey(0);
						kmap[2].setKeyName(new String("\uff0c\u3002? !\n"));
					    
					}
				}else if(cKM==1){
					kmap[2].setKeyName(candidateBar.findCandidateHZ(kmap[1].getKeyName()));
					VirtualKeyboardMap_F.setActiveKeyMap(2);
					kmap[2].setActiveKey(0);
				}else if(cKM==2){//\u9009\u4e2d\u4faf\u9009\u62e9\u5b57\uff0c\u5e76\u67e5\u627e\u8054\u60f3\u5b57
					vkl.virtualMetaKeyEntered(INSERT_CHAR_COMMAND);
					kmap[0].resetInputKey();
					kmap[1].resetKeyName();
					kmap[1].setActiveKey(0);
					if(getSelect()!=null&&getSelect().length()>0){
						char[] hz=candidateBar.findCandidateAssociational(getSelect().charAt(0));
						if(hz!=null&&hz[0]!=0){
							kmap[2].setKeyName(hz);
						}else{
							VirtualKeyboardMap_F.setActiveKeyMap(0);
						}
					}
					kmap[2].setActiveKey(0);
				}
			}
		}else if(currentKeyboard==AbstractKeyboardLayer.STROKE){
			int cKM=VirtualKeyboardMap_F.getActiveKeyMap();
			if(key>=3){//clear
				if(kmap[0].getInputKey().length()>0){
					kmap[0].setInputKeyClearOne();
					VirtualKeyboardMap_F.setActiveKeyMap(0);
					kmap[1].setKeyName(candidateBar.findCandidateStorkHZ(kmap[0].getInputKey()));
				}else{
					vkl.virtualMetaKeyEntered(DEL_CHAR_COMMAND);
				}
			}else{
				int ck=kmap[cKM].getActiveKey();
				kmap[cKM].setActiveKey(ck/3*3+key);
				if(cKM==0){
					if(kmap[cKM].getActiveKey()!=5){
						char str[]=candidateBar.findCandidateStorkHZ(kmap[0].getInputKey()+(kmap[0].getActiveKey()+1));
						if(str!=null && str[0]!= 0){//\u627e\u5230\u7b14\u753b\u4e86
							kmap[0].setInputKey();
							kmap[1].setKeyName(str);
							kmap[1].setActiveKey(0);
						}
					}else{
						VirtualKeyboardMap_F.setActiveKeyMap(1);
						kmap[1].setActiveKey(0);
						kmap[1].setKeyName(new String("\uff0c\u3002? !\n"));
					}
				}else if(cKM==1){//\u9009\u4e2d\u4faf\u9009\u62e9\u5b57\uff0c\u5e76\u67e5\u627e\u8054\u60f3\u5b57
					vkl.virtualMetaKeyEntered(INSERT_CHAR_COMMAND);
					kmap[0].resetInputKey();
					if(getSelect()!=null&&getSelect().length()>0){
						char[] hz=candidateBar.findCandidateAssociational(getSelect().charAt(0));
						if(hz!=null&&hz[0]!=0){
							kmap[1].setKeyName(hz);
						}else{
							VirtualKeyboardMap_F.setActiveKeyMap(0);
						}
					}
					kmap[1].setActiveKey(0);
				}
			}
		}else{
			// Non-Chinese-specific boards: PSP symbol key pressed -- emit character
			if(kmap[1].getKeyName(key)!=null&&kmap[1].getKeyName(key).length()!=0){
				if (kmap[1].getKeyName(key).length()==3) {
					handleEditCommand(key);
					return; }
				kmap[1].setActiveKey(key);
				//kmap[0].setActiveKey(4);//\u4e3b\u952e\u76d8\u5904\u4e0e\u4e2d\u95f4\u4f4d\u7f6e
				vkl.virtualMetaKeyEntered(INSERT_CHAR_COMMAND);
			}
		}
}

	// Breakout handling for edit commands
	void handleEditCommand(int k) {
		switch(k) {
			case 0: vkl.virtualMetaKeyEntered(CUT_COMMAND); break;
			case 1: vkl.virtualMetaKeyEntered(CPY_COMMAND); break;
			case 2: vkl.virtualMetaKeyEntered(PST_COMMAND); break;
			case 3: vkl.virtualMetaKeyEntered(SEL_COMMAND); } }

   void handleDirectionKey(int type, int keyCode){
	   	 	System.out.println("handleDirectionKey-key="+keyCode);
		if(currentKeyboard==AbstractKeyboardLayer.PINYIN){	
			int cAKM=VirtualKeyboardMap_F.getActiveKeyMap();
			int curAK=kmap[cAKM].getActiveKey();
			switch(keyCode){
				case Constants.KEYCODE_LEFT:
					{
						String tmpKname;
						if(cAKM==2){//
							tmpKname=kmap[1].getKeyName(1);
							if(tmpKname!=null&&tmpKname.length()!=0){//pinYin is need select
								VirtualKeyboardMap_F.setActiveKeyMap(1);
							}else{//only one or none
								VirtualKeyboardMap_F.setActiveKeyMap(0);
							}
						}else if(cAKM==0){
								tmpKname=kmap[2].getKeyName(0);
								if(tmpKname!=null&&tmpKname.length()>0&&tmpKname.charAt(0)!=0){
									VirtualKeyboardMap_F.setActiveKeyMap(2);
								}
						}else{
							VirtualKeyboardMap_F.setActiveKeyMap((cAKM+2)%3);
						}
					}
					break;
				case Constants.KEYCODE_RIGHT:
					if(cAKM==0){//number keymap next is pinyin or char.
						String tmpKname=kmap[1].getKeyName(1);
						if(tmpKname!=null&&tmpKname.length()!=0){//pinYin is need select
							VirtualKeyboardMap_F.setActiveKeyMap(1);
						}else{//only one or none
							tmpKname=kmap[2].getKeyName(0);
							if(tmpKname!=null&&tmpKname.length()>0&&tmpKname.charAt(0)!=0){
								VirtualKeyboardMap_F.setActiveKeyMap(2);
							}
						}
					}else {
						VirtualKeyboardMap_F.setActiveKeyMap((cAKM+1)%3);
					}
					break;
				case Constants.KEYCODE_UP:	
					if(curAK/3==0){//cur keymap top
						if(cAKM==2&&(kmap[1].getKeyName(0)!=null&&kmap[1].getKeyName(0).length()>0)){//focus pinyin keymap
							kmap[cAKM].setKeyName(candidateBar.prevPage());
						}else{
							// do nothing
							if(cAKM==0){
								kmap[cAKM].setActiveKey(6);
							}
						}
					}else{
						kmap[cAKM].setActiveKey(curAK-3);
					}
					break;
				case Constants.KEYCODE_DOWN:
				    String curKname=kmap[cAKM].getKeyName(curAK/3*3+3);
					System.out.println("handleDirectionKey"+curKname);
					if(curKname!=null&&curKname.length()>0&&curKname.charAt(0)!=0){
						kmap[cAKM].setActiveKey(curAK+3);
					}else{
						if(cAKM==2&&(kmap[1].getKeyName(0)!=null&&kmap[1].getKeyName(0).length()>0)){
							kmap[cAKM].setKeyName(candidateBar.nextPage());
							kmap[2].setActiveKey(0);
						}else{
						// do nothing 
							if(cAKM==0&&curKname==null){
								kmap[0].setActiveKey(0);
							}
						}
					}
					break;
				default:
					break;
			}
		}else if(currentKeyboard==AbstractKeyboardLayer.STROKE){	
			int cAKM=VirtualKeyboardMap_F.getActiveKeyMap();
			int curAK=kmap[cAKM].getActiveKey();
			switch(keyCode){
				case Constants.KEYCODE_RIGHT:
				case Constants.KEYCODE_LEFT:
					{
						String tmpKname;
						if(cAKM==0){
								tmpKname=kmap[1].getKeyName(0);
								if(tmpKname!=null&&tmpKname.length()>0&&tmpKname.charAt(0)!=0){
									VirtualKeyboardMap_F.setActiveKeyMap(1);
								}
						}else{
							VirtualKeyboardMap_F.setActiveKeyMap(0);
						}
					}
					break;
				case Constants.KEYCODE_UP:	
					if(curAK/3==0){//cur keymap top
						if(cAKM==1){
							if(kmap[1].getKeyName(6)!=null&&kmap[1].getKeyName(6).length()>0&&kmap[1].getKeyName(6).charAt(0)!=0){//focus pinyin keymap
								kmap[cAKM].setActiveKey(6);
							}
							else{
								if(kmap[1].getKeyName(3)!=null&&kmap[1].getKeyName(3).length()>0&&kmap[1].getKeyName(3).charAt(0)!=0){//focus pinyin keymap
									kmap[cAKM].setActiveKey(3);
								}
							}
						}else if(cAKM==0){
							kmap[cAKM].setActiveKey(3);
						}
					}else{
						kmap[cAKM].setActiveKey(curAK-3);
					}
					break;
				case Constants.KEYCODE_DOWN:
				    String curKname=kmap[cAKM].getKeyName(curAK/3*3+3);
					System.out.println("handleDirectionKey"+curKname);
					if(curKname!=null&&curKname.length()>0&&curKname.charAt(0)!=0){
						kmap[cAKM].setActiveKey(curAK+3);
					}else{
						kmap[cAKM].setActiveKey(0);
					}
					break;
				default:
					break;
			}
		}
		else{
			kmap[0].handleKeyPressedMsg(type, keyCode);
			kmap[1].setKeyName(kmap[0].getKeyName());
   		}
}

	// Help screen content
	final static String[] helpStr = {"  help:",
		"1.(L \u6216 R)+O: \u5207\u6362\u8f93\u5165\u6cd5. (Switch Input Method)",
		"2.(L \u6216 R)+select: \u529f\u80fd\u540c\u4e0a. (Switch Input Method)",
		"3.(L \u6216 R)+X: \u5220\u9664\u8f93\u5165. (Delete)",
		"4. X: \u62fc\u97f3\u72b6\u6001\u4e0b\u5220\u9664\u8f93\u5165. (Delete PinYin)",
		"5.\u65b9\u5411\u952e\u6216\u6447\u6746: \u79fb\u52a8\u5149\u6807\u6216\u5207\u6362\u952e\u76d8\u533a\u57df.",
		"  Up/Down/Left/Right: Move cursor"};

	// Pain the help screen
	void paintHelp(Graphics g) {
		g.setFont(f);
		g.setColor(TEXT_COLOR);
		for(int i=0;i<helpStr.length;i++) {
			g.drawString(helpStr[i],
				5,10+i*fontH,
				g.LEFT|g.TOP); } }

	/**
	 * paint the virtual keyboard on the screen
	 * 
	 * @param g The graphics context to paint to
	 */
	protected void paint(Graphics g) {
		g.setFont(f);
		// g.translate(0-g.getTranslateX(),0-g.getTranslateY());
		g.setColor(LIGHT_GRAY);
		g.fillRect(0,0,kbWidth,kbHeight);
		drawBorder(g,0,0,kbWidth-1,kbHeight-1);
		// g.translate(3-g.getTranslateX(),3-g.getTranslateY());
		// g.translate(0-g.getTranslateX(),0-g.getTranslateY());
		if(isShowHelp==true){
			// Paint help
			paintHelp(g);
			return; }
		if(currentKeyboard==AbstractKeyboardLayer.PINYIN){
			// Paint Pinyin board
			for(int i=0;i<3;i++){
				if(kmap[i]!=null) {
					kmap[i].drawKeyboardCN(g,true); } }
			return; }
		if(currentKeyboard==AbstractKeyboardLayer.STROKE){
			// Paint stroke board
			kmap[0].drawKeyboardCN(g,false);
			kmap[1].drawKeyboardCN(g,false);
			return; }
		// Paint default boards
		kmap[0].drawKeyboard(g);
		kmap[1].drawKeyboard(g); }

    /**
     * draw a border
     * 
     * @param g The graphics context to paint to
     * @param x1 x-coordinate of the button's location
     * @param y1 y-coordinate of the button's location
     * @param x2 the x-coordinate at the width of the border
     * @param y2 the y-coordinate at the height of the border
     */
    private void drawBorder(Graphics g, int x1, int y1, int x2, int y2) {

        g.setColor(GRAY);
        g.drawLine(x1+2,y1+2,x1+2,y2-3);    // left
        g.drawLine(x1+2,y1+2,x2-2,y1+2);    // top
        g.drawLine(x1+2,y2-1,x2-1,y2-1);    // bottom
        g.drawLine(x2-1,y1+2,x2-1,y2-1);    // right
        g.setColor(WHITE);
        g.drawRect(x1+1,y1+1,x2-x1-3,y2-y1-3);
    }

    /**
     * draw a sunken border
     * 
     * @param g The graphics context to paint to
     * @param x1 x-coordinate of the button's location
     * @param y1 y-coordinate of the button's location
     * @param x2 the x-coordinate at the width of the border
     * @param y2 the y-coordinate at the height of the border
     */
    private void drawSunkedBorder(Graphics g, int x1, int y1, int x2, int y2) {

        g.setColor(WHITE);
        g.fillRect(x1+2,y1+2,x2-x1-2,y2-y1-2);

        g.setColor(GRAY);
        g.drawLine(x1+2,y1+2,x1+2,y2-2);    //left
        g.drawLine(x1+2,y1+2,x2-2,y1+2);    //top
        g.setColor(DARK_GRAY);
        g.drawLine(x1+3,y1+3,x1+3,y2-3);    //left
        g.drawLine(x1+3,y1+3,x2-3,y1+3);    //top

        g.setColor(LIGHT_GRAY);
        g.drawLine(x1+3,y2-2,x2-2,y2-2);    //bottom
        g.drawLine(x2-2,y1+3,x2-2,y2-2);    //right
    }
    
   
    /**
     * Helper function to determine the itemIndex at the x,y position
     *
     * @param x,y   pointer coordinates in menuLayer's space (0,0 means left-top
     *      corner) both value can be negative as menuLayer handles the pointer
     *      event outside its bounds
     * @return menuItem's index since 0, or PRESS_OUT_OF_BOUNDS, PRESS_ON_TITLE
     *
     */
    private boolean isKeyAtPointerPosition(int x, int y) {
        int ret=-1;
        int tmpX,tmpY,tmp;
         for (int i=0; i<maxRows; i++) {
            for (int j=0; j<maxColumns; j++) {
                tmp = i * maxColumns + j;
                if (tmp >= keys[currentKeyboard].length) {
                    // no more chars to draw
                    break;
                }

                tmpX=x-(j * (PADDING+buttonW) + PADDING);
                tmpY=y-(i * (PADDING+buttonH) + PADDING);

                if( (tmpX>=0)&&(tmpY>=0) &&(tmpX<buttonW) && (tmpY< buttonH)) {
                    currentChar = tmp;
                    return true;
                }

            }
         }
        return false;
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
	System.out.println("VirtualK: Constants.KEYCODE_SELECT="+Constants.KEYCODE_SELECT+"x="+x+"y="+y);
        switch (type) {
        case EventConstants.PRESSED:
           

            // dismiss the menu layer if the user pressed outside the menu
            if ( isKeyAtPointerPosition(x, y)) {
             // press on valid key
                traverse(type,Constants.KEYCODE_SELECT);
                vkl.repaintVK();
               
            }
            break;
        case EventConstants.RELEASED:
                if ( isKeyAtPointerPosition(x, y)) {
                    traverse(type,Constants.KEYCODE_SELECT);
                    vkl.repaintVK();
               
            }

            break;
        }
        // return true always as menuLayer will capture all of the pointer inputs
        return true;  
    }

    
    // ********* attributes ********* //
    private final static int WHITE = 0xffffff;

    // blue scheme
    private final static int DARK_GRAY = 0x666699; // dark blue
    private final static int GRAY = 0x3366cc;//0x8DB0D9;
    private final static int LIGHT_GRAY = 0x6699ff;//0xccccff //0xc0c0c0;//
    private final static int TEXT_COLOR = 0xCCFFFF;//0xccccff

    /** padding between rows of buttons */
    private int PADDING;
    /** padding used by the meta keys */


    final static int OK_COMMAND= 0;  //update all input and close vk
    final static int CANCEL_COMMAND= 1;//cancel all inout and close vk
    final static int INSERT_CHAR_COMMAND = 2;
    final static int DEL_CHAR_COMMAND = 3;
    final static int NODIFY_IM_CHANGE_COMMAND = 4;
	final static int CURSOR_UP_COMMAND = 7;
    final static int CURSOR_DOWN_COMMAND = 8;
    final static int CURSOR_LEFT_COMMAND = 9;
    final static int CURSOR_RIGHT_COMMAND = 10;
    
    // Editing commands
    final static int CPY_COMMAND = 11;
    final static int PST_COMMAND = 12;
    final static int SEL_COMMAND = 13;
    final static int CUT_COMMAND = 14;

	//
	private VirtualKeyboardMap_F kmap[]=new VirtualKeyboardMap_F[3];
	String numericMap[]=
		{"1","2","3","4","50","6","7","8","9", "Edit"};
	String CnMap[]=
		{"?","abc","def","ghi","jkl","mno","pqrs","tuv","wxyz"};
	String abcMap[]=
		{"_@/-","abc.","def\n","ghi?","jkl,","mno!","pqrs","tuv ","wxyz", "Edit"};
	String ABCMap[]=
		{"_@/-","ABC.","DEF\n","GHI?","JKL,","MNO!","PQRS","TUV ","WXYZ", "Edit"};
	String symbolMap[]=
		{"[^]","~@:#","<$>%","(_)+",",.?!","\"\\;'","{|}-","=/0`","& \n", "Edit"};
	String strokeMap[]=
		{"\u4e00","\u4e28","\u4e3f","\u4e36","\u30d5","?"};
		
	// Pixels to take from right, give to left in certain boards
	// where that helps
	static final int LSHIFT = 13; 

	void prepareKeyMap(){
		
		int kmapW,kmpaH,kmap2X,kmap2Y,kmap2H;	
		
		mainKeyOffsetX=3;
		mainKeyOffsetY=3;
		kmpaH=kbHeight-mainKeyOffsetY;
		if(kmpaH>240){
			kmpaH=120; }

		if(kbHeight>=kbWidth||kbWidth<230){//need reset kmap2
			kbLayout=1;
			
			kmapW=kbWidth/2-PADDING;
			kmap2X=mainKeyOffsetX+kmapW;
			kmap2Y=mainKeyOffsetY+kmpaH/2;
			kmap2H=kmpaH*3/4;
		}else{
			kbLayout=0;
		
			kmapW=kbWidth/3-PADDING;
			kmap2X=mainKeyOffsetX+kmapW*2;
			kmap2Y=mainKeyOffsetY;
			kmap2H=kmpaH;
		}
		VirtualKeyboardMap_F.setActiveKeyMap(0);
		switch(currentKeyboard){
			case 4:
				// Pin-Yin board
				kmap[0]=new VirtualKeyboardMap_F(kmapW,kmpaH,
											mainKeyOffsetX,mainKeyOffsetY,VirtualKeyboardMap_F.KEYMAP_LAYOUT_TYPE_3,0);
				kmap[0].setKeyName(CnMap);
				kmap[1]=new VirtualKeyboardMap_F(kmapW,kmpaH/2,
										mainKeyOffsetX+kmapW,mainKeyOffsetY+kmpaH/6,
										VirtualKeyboardMap_F.KEYMAP_LAYOUT_TYPE_3,1);
				kmap[2]=new VirtualKeyboardMap_F(kmapW,kmap2H,
										kmap2X,kmap2Y,
										VirtualKeyboardMap_F.KEYMAP_LAYOUT_TYPE_3,2);
				break;
			case 0:
			case 1:
			case 2:
			case 3:
				// Roman and numeric/symbol boards
				kmap[0]=new VirtualKeyboardMap_F(kmapW+LSHIFT,kmpaH,mainKeyOffsetX,mainKeyOffsetY,VirtualKeyboardMap_F.KEYMAP_LAYOUT_TYPE_10,0);
				if(currentKeyboard==0){
					kmap[0].setKeyName(numericMap);
				}else if(currentKeyboard==1){
					kmap[0].setKeyName(abcMap);
				}else if(currentKeyboard==2){
					kmap[0].setKeyName(ABCMap);
				}
				else if(currentKeyboard==3){
					kmap[0].setKeyName(symbolMap);
				}
				kmap[0].curActiveKey=4;
				kmap[1]=new VirtualKeyboardMap_F(kmapW-LSHIFT,kmpaH/2,
										mainKeyOffsetX+kmapW+LSHIFT,mainKeyOffsetY+kmpaH/6,
										VirtualKeyboardMap_F.KEYMAP_LAYOUT_TYPE_4,1);
				kmap[1].setKeyName(kmap[0].getKeyName());
				kmap[2]=null;
				break;
			case 5:
				// Stroke board
				kmap[0]=new VirtualKeyboardMap_F(kmapW,kmpaH,
											mainKeyOffsetX,mainKeyOffsetY+kmpaH/3,
											VirtualKeyboardMap_F.KEYMAP_LAYOUT_TYPE_3,0);
				kmap[0].setKeyName(strokeMap);
				kmap[1]=new VirtualKeyboardMap_F(kmapW,kmpaH,
										mainKeyOffsetX+kmapW,mainKeyOffsetY,
										VirtualKeyboardMap_F.KEYMAP_LAYOUT_TYPE_3,1);
				break;
				
			default:
				break;
		}
	}

	String getSelect(){
		if(currentKeyboard==AbstractKeyboardLayer.PINYIN){
			return kmap[2].getKeyName();
		}else{
			return kmap[1].getKeyName();
		}
	}

}

class VirtualKeyboardMap_F
{
	final static int KEYMAP_LAYOUT_TYPE_10 = 0;//\u4e5d\u5bab\u683c
	final static int KEYMAP_LAYOUT_TYPE_L = 1;//\u5217\u8868\u5f62\u5f0f
	final static int KEYMAP_LAYOUT_TYPE_4 = 2; //4\u952e\u5f62\u5f0f
	final static int KEYMAP_LAYOUT_TYPE_3 = 3;//\u4e5d\u5bab\u683c\u5f62\u5f0f\u4f46\u662f\u6574\u884c\u79fb\u52a8
	
	// ********* attributes ********* //
	private final static int WHITE = 0xffffff;
	private final static int COLOR_RED = 0x601010;
	
	// blue scheme
	private final static int DARK_GRAY = 0x666699; // dark blue
	private final static int GRAY = 0x3366cc;//0x8DB0D9;//
	private final static int LIGHT_GRAY = 0x6699ff;//0xccccff
	private final static int TEXT_COLOR = 0xCCFFFF;//0xccccff
	
	int buttonW;     // width of keyboard
	int buttonH;     // height of keyboard
	int fontWCenter; // placement of text inside button
	int fontHTop;    // placement of text inside button
	
	int keyMapW, keyMapH, keyMapX, keyMapY, keyMapType, keyIndex;
	int keyMapOffsetX=2;
	int keyMapOffsetY=2;
	
	String inputKey=new String();
	String keyName[]=new String[10];
	static int curActiveKeymap;
	int curActiveKey;

	// Constructor
	public VirtualKeyboardMap_F(int W,int H,int X,int Y,int Type,int Index){
		keyMapW=W;
		keyMapH=H;
		keyMapX=X;
		keyMapY=Y;
		keyMapType=Type;
		keyIndex=Index;

		buttonW=(keyMapW-6)/3;
		buttonH=(keyMapH-4)/3;
		if (Type==KEYMAP_LAYOUT_TYPE_10) {
			buttonH=(keyMapH-4)/4; }
		
		fontWCenter=buttonW/2;
		fontHTop=buttonH/2;
		System.out.println("VirtualKeyboardMap_F QUIT!keyMapH="+keyMapH);
	}
	public static int getActiveKeyMap(){
		return curActiveKeymap;
	}
	public static void setActiveKeyMap(int index){
		curActiveKeymap=index; }

	public  int getActiveKey(){
		System.out.println(" curActiveKey="+curActiveKey);
			return curActiveKey; }

	public  void setActiveKey(int key){
		if(key>=0&&key<9){
			curActiveKey=key; }
		System.out.println(" setActiveKey="+curActiveKey); }

	public void setKeyName(String name[]){
		if(name!=null){
			for(int i=0;i<10&&i<name.length;i++)
				keyName[i]=name[i];
		}else{
			for(int i=0;i<keyName.length;i++){
				keyName[i]="";
			}
		}
	}

	public void setKeyName(char name[]){
		if(name!=null){
			for(int i=0;i<keyName.length;i++){
				keyName[i]="";
			}
			for(int i=0;i<name.length;i++){
				if(name[i]!=0){
					keyName[i]=new String(new char[]{name[i]});
				}else{
					keyName[i]="";
				}
			}
		}
	}
	public void setKeyName(String name){
		if(name!=null){
			for(int i=0;i<keyName.length;i++){
				keyName[i]="";
			}
			if (name.compareTo("Edit")==0) {
				keyName[0]="CUT";
				keyName[1]="CPY";
				keyName[2]="PST";
				keyName[3]="SEL";
				return; }
			for(int i=0;i<name.length();i++){
				keyName[i]=name.substring(i,i+1);
			}
		}
	}
	public String getKeyName(int index){
		if(keyName!=null&&index<9)
{
			return keyName[index];
		}
		return null;
	}
	public String getKeyName(){
		System.out.println("curActiveKey="+curActiveKey+"getKeyName="+keyName[curActiveKey]);
		return keyName[curActiveKey];
	}
	public String getInputKey(){
		System.out.println("getInputKey="+inputKey);
		return inputKey;
	}
	public void resetKeyName(){
		for(int i=0;i<keyName.length;i++){
				keyName[i]="";
		}
		curActiveKey=0;
	}
	public void resetInputKey(){
		inputKey="";
	}
	
	public void setInputKey(){
		inputKey+=(curActiveKey+1);
	}
	public void setInputKey(int key){
		inputKey+=(key+1);
	}
	public void setInputKeyClearOne(){
		int endIndex=inputKey.length()-1;
		if(endIndex>=0){
			inputKey=inputKey.substring(0,endIndex);
		}else{
			inputKey="";
		};
	}
	
    /**
     * draw a border
     * 
     * @param g The graphics context to paint to
     * @param x1 x-coordinate of the button's location
     * @param y1 y-coordinate of the button's location
     * @param x2 the x-coordinate at the width of the border
     * @param y2 the y-coordinate at the height of the border
     */
    private void drawBorder(Graphics g, int x1, int y1, int x2, int y2) {

        g.setColor(GRAY);
        g.drawLine(x1+2,y1+2,x1+2,y2-3);    // left
        g.drawLine(x1+2,y1+2,x2-2,y1+2);    // top
        g.drawLine(x1+2,y2-1,x2-1,y2-1);    // bottom
        g.drawLine(x2-1,y1+2,x2-1,y2-1);    // right
        g.setColor(WHITE);
        g.drawRect(x1+1,y1+1,x2-x1-3,y2-y1-3);
    }
    /**
     * draw a button
     * 
     * @param g The graphics context to paint to
     * @param x x-coordinate of the button's location
     * @param y y-coordinate of the button's location
     * @param w the width of the button
     * @param h the height of the button
     */
    private void drawButton(Graphics g, int x, int y, int w, int h) {
	    w-=2;
		h-=2;
        g.setColor(GRAY);
        g.drawLine(x+1,y+h-1,x+w,y+h-1);    //bottom
        g.drawLine(x+w-1,y+1,x+w-1,y+h);    //right

        g.setColor(DARK_GRAY);
        g.drawLine(x,y+h,x+w,y+h);    //bottom
        g.drawLine(x+w,y,x+w,y+h);    //right

        g.setColor(WHITE);
        g.drawLine(x,y,x+w-1,y);
        g.drawLine(x,y,x,y+h-1);

    }

    /**
     * draw a beveled button
     * 
     * @param g The graphics context to paint to
     * @param x x-coordinate of the button's location
     * @param y y-coordinate of the button's location
     * @param w the width of the button
     * @param h the height of the button
     */
    private void drawBeveledButton(Graphics g, int x, int y, int w, int h) {
	    w-=2;
		h-=2;
        g.setColor(GRAY);
        g.drawLine(x+1,y+h-1,x+w,y+h-1);    //bottom
        g.drawLine(x+w-1,y+1,x+w-1,y+h);    //right

        g.setColor(WHITE);
        g.drawLine(x,y+h,x+w,y+h);    //bottom
        g.drawLine(x+w,y,x+w,y+h);    //right

        g.setColor(GRAY);
        g.drawLine(x,y,x+w-1,y);
        g.drawLine(x,y,x,y+h-1);

        g.setColor(WHITE);
        g.drawLine(x+1,y+1,x+w-2,y+1);
        g.drawLine(x+1,y+1,x+1,y+h-2);

    }
	 /**
     * draw a beveled button
     * 
     * @param g The graphics context to paint to
     * @param x x-coordinate of the button's location
     * @param y y-coordinate of the button's location
     * @param w the width of the button
     * @param h the height of the button
     */
    private void drawPressedButton(Graphics g, int x, int y, int w, int h) {
	    w-=2;
		h-=2;
		g.setColor(COLOR_RED);
		g.fillRect(x,y,w,h);
        g.setColor(WHITE);
        g.drawLine(x+1,y+h-1,x+w,y+h-1);    //bottom
        g.drawLine(x+w-1,y+1,x+w-1,y+h);    //right

        g.setColor(DARK_GRAY);
        g.drawLine(x,y+h,x+w,y+h);    //bottom
        g.drawLine(x+w,y,x+w,y+h);    //right

        g.setColor(GRAY);
        g.drawLine(x,y,x+w-1,y);
        g.drawLine(x,y,x,y+h-1);

    }
	public void drawKeyboardCN(Graphics g,boolean isPY) {
		System.out.println("drawKeyboardCN");
		int col=3;
		if(keyName==null){
			return;
		}
		for (int i=0; i<keyName.length; i++) {
			if(keyName[i]!=null&&(!keyName[i].equals(""))){
				if((i/col)==(curActiveKey/col) && curActiveKeymap==keyIndex){
						drawButton(g,
									(i%col)*(buttonW)+keyMapX,
									(i/col)*(buttonH)+keyMapY,
									buttonW, 
									buttonH);
			            g.drawImage(keyGuidImage[i%3],
									(i%col)*(buttonW)+keyMapX+3,
									(i/col)*(buttonH)+keyMapY+3,
									g.LEFT|g.TOP);
				}else{
						drawBeveledButton(g,
										(i%col)*(buttonW)+keyMapX,
										(i/col)*(buttonH)+keyMapY,
										buttonW, 
										buttonH);
				}
				if(keyName[i]!=null){
					g.setColor(TEXT_COLOR);
					String str=keyName[i].replace('\n',(char)0x2518);
					//str=str.replace(' ' ,(char)0x2581);
		            g.drawString(str,//(keyName[i],
								(i%col)*(buttonW)+fontWCenter+keyMapX,
								(i/col)*(buttonH)+buttonH+keyMapY-3,
								g.HCENTER|g.BOTTOM);
				}
			}
		}	
		if(keyIndex==0){
			if(isPY){
				drawBeveledButton(g,keyMapX+buttonW*3+buttonW/3,
								keyMapY,
								buttonW*2, buttonH/2);
				g.setColor(COLOR_RED);
				if(inputKey!=null){
		            g.drawString("in: "+inputKey,
								 keyMapX+buttonW*4,
								 keyMapY+buttonH/8,
								g.LEFT|g.TOP);
				}
			}else{
				drawBeveledButton(g,keyMapX+buttonW/3,
								keyMapY-buttonH,
								buttonW*2, buttonH/2);
				g.setColor(COLOR_RED);
				if(inputKey!=null){
					int begin=(inputKey.length()>6)?(inputKey.length()-6):0;
					String strokeStr=inputKey.substring(begin);
					strokeStr=strokeStr.replace('1', '\u4e00');
					strokeStr=strokeStr.replace('2', '\u4e28');
					strokeStr=strokeStr.replace('3', '\u4e3f');
					strokeStr=strokeStr.replace('4', '\u4e36');
					strokeStr=strokeStr.replace('5', '\u30d5');
		            g.drawString("in: "+strokeStr,
								 keyMapX+buttonW/2,
								 keyMapY+buttonH/8-buttonH,
								g.LEFT|g.TOP);
				}
			}
		}
	} 

	public void drawKeyboard(Graphics g) {
		System.out.println("drawKeyboard:"+keyIndex);
		if(keyName==null){
			return;
		}
		int pos[][];
		//keymap layout
		int pos9[][]={
				{0,0},{1,0},{2,0},
				{0,1},{1,1},{2,1},
				{0,2},{1,2},{2,2}};
		int pos10[][]={
				{0,0},{1,0},{2,0},
				{0,1},{1,1},{2,1},
				{0,2},{1,2},{2,2},
				      {1,3}};
		int pos4[][]={
					{0,1},	{1,0},  {2,1},
					        {1,2}};
		int pos6[][]={
					{0,0},
					{0,1},
					{0,2},
					{0,3},
					{0,4},
					{0,5}};
		int pos5[][]={
					{1,0},	
			  {0,1},{1,1},{2,1},
					{1,2}};
	 	switch(keyMapType){
			case KEYMAP_LAYOUT_TYPE_10:  // = 0;//\u4e5d\u5bab\u683c
				{
					pos=pos10;
				}
				break;
			case KEYMAP_LAYOUT_TYPE_L: //= 1;//\u5217\u8868\u5f62\u5f0f
				{
					pos=pos6;
				}
				break;
			case KEYMAP_LAYOUT_TYPE_4: //= 2; //4\u952e\u5f62\u5f0f
				{
					pos=pos4;
				}
				break;
			default:
				{
					pos=pos9;
				}
			break;
		}
		for (int i=0; i<keyName.length; i++) {
			if(keyName[i]!=null&&(!keyName[i].equals(""))){
				if(KEYMAP_LAYOUT_TYPE_10==keyMapType){
					if(i==curActiveKey){
						drawPressedButton(g,pos[i][0] *(buttonW)+keyMapX,
										pos[i][1] * (buttonH)+keyMapY,
										buttonW, buttonH);
					}else{
						drawBeveledButton(g,pos[i][0] * (buttonW)+keyMapX,
										pos[i][1]* (buttonH) +keyMapY,
										buttonW, buttonH);
					}
				}else if(KEYMAP_LAYOUT_TYPE_4==keyMapType){
					drawButton(g,pos[i][0] *(buttonW)+keyMapX,
							pos[i][1] * (buttonH)+keyMapY,
							buttonW, buttonH);
		            g.drawImage(keyGuidImage[i%4],
							pos[i][0] * (buttonW)+keyMapX+3,
							pos[i][1] * (buttonH)+keyMapY+3,
							g.LEFT|g.TOP);
				}
				g.setColor(TEXT_COLOR);
					String str=keyName[i].replace('\n',(char)0x2518);
					//str=str.replace(' ' ,(char)0x2581);
		            g.drawString(str,//(keyName[i]
					pos[i][0] * (buttonW)  + fontWCenter+keyMapX,
					pos[i][1] * (buttonH)  + buttonH+keyMapY-3,
					g.HCENTER|g.BOTTOM);
				if(KEYMAP_LAYOUT_TYPE_4==keyMapType){//\u5237\u6309\u952e\u6307\u793a\u56fe\u6807
			            g.drawImage(keyGuidImage[i%4],
									pos[i][0] * (buttonW)+keyMapX+3,
									pos[i][1] * (buttonH)+keyMapY+3,
									g.LEFT|g.TOP);
				}
			}
		}
	}
	public  boolean handleKeyPressedMsg(int type, int keyCode) {
		
		System.out.println("handleKeyPressedMsg: keyCode="+keyCode+"curActiveKey="+curActiveKey);
		boolean ret=false;
		switch(keyCode){
			case Constants.KEYCODE_RIGHT:
			    handleKeyMsgR();
			    break;
			case Constants.KEYCODE_LEFT:
			    handleKeyMsgL();
			    break;
			case Constants.KEYCODE_UP:
			    handleKeyMsgU();
			    break;
			case Constants.KEYCODE_DOWN:
			    handleKeyMsgD();
			    break;
			}
		return true;
	}

	private  void handleKeyMsgR(){
		switch(keyMapType){
			case KEYMAP_LAYOUT_TYPE_10:
					if (curActiveKey==9) {
						return; }
					if((curActiveKey+1)%3==0){
						curActiveKey-=2;
					}else{
						curActiveKey++;
					}
					curActiveKey=(curActiveKey+9)%9;
				break;
			case KEYMAP_LAYOUT_TYPE_L:
				if(curActiveKey!=6&&keyName!=null){
					if(keyName[curActiveKey+1]!=null){
						curActiveKey++;
					}
				}
				break;
			case KEYMAP_LAYOUT_TYPE_4:
				break;
			case KEYMAP_LAYOUT_TYPE_3:
				break;
		}
	}
 private  void handleKeyMsgL(){
		switch(keyMapType){
			case KEYMAP_LAYOUT_TYPE_10:
					if (curActiveKey==9) {
						return; }
					if((curActiveKey)%3==0){
						curActiveKey+=2;
					}else{
						curActiveKey--;
					}
					curActiveKey=(curActiveKey+9)%9;
				break;
			case KEYMAP_LAYOUT_TYPE_L:
				if(curActiveKey!=0){
					curActiveKey--;
				}
				break;
			case KEYMAP_LAYOUT_TYPE_4:
				break;
		}
	}
 private void handleKeyMsgD(){
		switch(keyMapType){
			case KEYMAP_LAYOUT_TYPE_3:
			case KEYMAP_LAYOUT_TYPE_10:
				handleKeyMsgD_10Pad();
				return;
			case KEYMAP_LAYOUT_TYPE_L:
				if(curActiveKey!=6&&keyName!=null){
					if(keyName[curActiveKey]!=null){
						curActiveKey++;
						curActiveKey=(curActiveKey+6)%6;
					}
				}
				break;
			case KEYMAP_LAYOUT_TYPE_4:
				break;
		}
	}
	
	// Specific handler for the 10-key pad; it's
	// a bit more involved
	private void handleKeyMsgD_10Pad() {
		switch (curActiveKey) {
			case 0:
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
				curActiveKey+=3;
				return;
			case 6:
			case 7:
			case 8:
				curActiveKey=9;
				return;
			case 9:
				curActiveKey=1; } }
				
	// Specific handler for the 10-key pad; it's
	// a bit more involved
	private void handleKeyMsgU_10Pad() {
		switch(curActiveKey) {
			case 0:
			case 1:
			case 2:
				curActiveKey=9;
				return;
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
				curActiveKey-=3;
				return; 
			case 9:
				curActiveKey=7; } }

 private void handleKeyMsgU() {
		switch(keyMapType){
			case KEYMAP_LAYOUT_TYPE_3:
				curActiveKey-=3;
				curActiveKey=(curActiveKey+9)%9;
				break;
			case KEYMAP_LAYOUT_TYPE_10:
				handleKeyMsgU_10Pad();
				return;
			case KEYMAP_LAYOUT_TYPE_L:
				if(curActiveKey!=0){
					curActiveKey--;
				}
				break;
			case KEYMAP_LAYOUT_TYPE_4:
				break;
		}
	}

	Image keyGuidImage[] = {
		Image.createImage(AWFImages.pspKeyD,0,AWFImages.pspKeyD.length),
		Image.createImage(AWFImages.pspKeyA,0,AWFImages.pspKeyA.length),
		Image.createImage(AWFImages.pspKeyO,0,AWFImages.pspKeyO.length),
		Image.createImage(AWFImages.pspKeyX,0,AWFImages.pspKeyX.length) };
}
