package javax.microedition.lcdui;

import com.sun.midp.lcdui.*;
import com.sun.midp.configurator.Constants;


public class VirtualKeyboardMap
{
  /**
	*add by an for 
	*fixed virtual keyboard 
	*
	*\u76ee\u524d\u53ef\u80fd\u6709\u4e09\u4e2a\u865a\u62df\u533a\u57df 1\u3002\u4e3b\u952e\u76d8\u533a\u3002 2\u3002\u5019\u9009\u533a\u57df_1 3.\u5019\u9009\u533a\u57df_2
	*
	*\u952e\u76d8\u7684layout\u6709\u4e24\u79cd\u5f62\u5f0f 1.\u4e5d\u5bab\u683c 2\u3002\u7c7b\u4f3cPSP\u53f3\u9762\u952e\u76d8\u5e03\u5c40
	*
	*/
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

	int keyMapW;
	int keyMapH;
	int keyMapX;
	int keyMapY;
	int keyMapType;
	int keyIndex;
	int keyMapOffsetX=2;
	int keyMapOffsetY=2;
		

	String inputKey=new String();
	String keyName[]=new String[10];
	static int curActiveKeymap;
	int curActiveKey;

	public  VirtualKeyboardMap(int W,int H,int X,int Y,int Type,int Index){
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
		System.out.println("VirtualKeyBoardMap QUIT!keyMapH="+keyMapH);
	}
	public static int getActiveKeyMap(){
		return curActiveKeymap;
	}
	public static void setActiveKeyMap(int index){
		curActiveKeymap=index;
	}
	public  int getActiveKey(){
		System.out.println(" curActiveKey="+curActiveKey);
			return curActiveKey;
	}
	public  void setActiveKey(int key){
		if(key>=0&&key<9){
			curActiveKey=key;
		}
		System.out.println(" setActiveKey="+curActiveKey);
	}
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

