package com.pspkvm.midp.lcdui.vk.awf;

import javax.microedition.lcdui.Graphics;

public class IMCandidateBar {
    static CNLib cnlib=new CNLib();
	static Associational ass=new Associational();
	static strokeLib slib=new strokeLib();
	final static int MAX_CHARS_NUM=6;
	final static int MAX_STROKE_CHARS_NUM=9;
    String pinyin = null;
    int cur_pos = 0;
    int cur_sel = 0;
    char[] cur_candidates = new char[MAX_CHARS_NUM];
	char []cur_stroke= new char[MAX_STROKE_CHARS_NUM];

    public IMCandidateBar(int width, int height) {
    }

    private int getCandidateChars(String py, int pos , int num) {
    	int n = 0;
    	char[] cnchars;
    	System.out.println("getCandidateChars py="+py);
    	int total = cnlib.getNumOfChineseCharacters(py);
    	if (total <= pos) {
    		return 0;
    	} else if (pos + num > total) {
    	       cnchars = cnlib.parse(py, pos, total - pos);
    	} else {
    		cnchars = cnlib.parse(py, pos, num);
    	}
    	
    	for (int i = 0; i < num; i++) {
    	    if (pos + i >= total) {
    	        cur_candidates[i] = 0;
    	    } else {
    	        cur_candidates[i] = cnchars[i];
    	        n++;
    	    }
	    System.out.println("getCandidateChars cur_candidates[i]="+cur_candidates[i]);
    	}
    	
    	return n;
    }
    
    public boolean keyPressed(char c) {
    	if (c >= 'a' && c <= 'z' && pinyin.length() < 6) {
    	    getCandidateChars(pinyin + c, 0, MAX_CHARS_NUM);
    	    pinyin = pinyin + c;
           cur_pos = 0;
           cur_sel = 0;
           return true;
    	} else {
    	    return false;
    	}
    }
    public char[] findCandidateHZ(String py) {
		if(py!=null&&py.length()>0){
			pinyin=py;
			getCandidateChars(pinyin, 0, MAX_CHARS_NUM);
			cur_pos = 0;
			cur_sel = 0;
		}
		return cur_candidates;
    }
    public void backspace() {
    	int len = pinyin.length();
    	if (len > 0) {
    	    pinyin = pinyin.substring(0, len - 1);
    	    getCandidateChars(pinyin, 0, MAX_CHARS_NUM);    	    
           cur_pos = 0;
           cur_sel = 0;
    	}
    }

    public void paint(Graphics g, int width, int height) {
    }

    public char[] prevPage() {
    	cur_pos -= MAX_CHARS_NUM;
    	if (cur_pos < 0) cur_pos = 0;
    	getCandidateChars(pinyin, cur_pos, MAX_CHARS_NUM);
		return cur_candidates;
    }

    public char[] nextPage() {
    	cur_pos += MAX_CHARS_NUM;
    	if (getCandidateChars(pinyin, cur_pos, MAX_CHARS_NUM) <= 0) cur_pos -= MAX_CHARS_NUM;
    	if (cur_candidates[cur_sel] == 0) cur_sel = 0;
		return cur_candidates;
    }

    public void prevChar() {
    	cur_sel --;
    	if (cur_sel < 0) {
    		cur_pos -= MAX_CHARS_NUM;
    		if (cur_pos < 0) cur_pos = 0;
    		cur_sel = (MAX_CHARS_NUM-1);
    		getCandidateChars(pinyin, cur_pos, MAX_CHARS_NUM);
    	}
    }

    public void nextChar() {
    	cur_sel ++;
    	if (cur_sel > (MAX_CHARS_NUM-1)) {
    		cur_pos += MAX_CHARS_NUM;
    		if (getCandidateChars(pinyin, cur_pos, MAX_CHARS_NUM) > 0) {
    			cur_sel = 0;
    		} else {
    			cur_pos -= MAX_CHARS_NUM;
    			cur_sel = (MAX_CHARS_NUM-1);
    		}    		
    	} else if (cur_candidates[cur_sel] == 0) {
    	    cur_sel --;
    	}
    }

    public String getSelected() {
    	return new String(cur_candidates, cur_sel, 1);    	
    }

    public void reset() {
    	cur_pos = 0;
    	cur_sel = 0;
    	pinyin = null; 	
    	for (int i = 0; i < MAX_CHARS_NUM; i++) {
    		cur_candidates[i] = 0;
    	}
    }
//Æ´ÒôÏà¹Ø²Ù×÷

 public String[] findCandidatePY(String keyNum) {
		 if(keyNum!=null){
		 	System.out.println("findCandidatePY="+keyNum);
			if(keyNum.length()>0&&keyNum.length()<=6){
				return cnlib.getPinYin(keyNum);
			}
	 	}
		return null;
    }

 public char[] findCandidateAssociational(char ch) {
	 	char [] assCh=ass.getAssociatioalByChar(ch);
		for(int j=0;j<MAX_CHARS_NUM;j++){
			if(assCh!=null && j< (assCh.length-1)){
				cur_candidates[j] = assCh[j+1];
			}else{
				cur_candidates[j]=0;
			}
		}
		return cur_candidates;
 	}

 public char[] findCandidateStorkHZ(String stroke) {
 	//stroke="1";
		if(stroke!=null){
			char temHZ[]= slib.getHzByStrokeString(stroke);
			if(temHZ==null) return null;
			for (int i = 0; i < MAX_STROKE_CHARS_NUM; i++) {
	    	    if (i < temHZ.length) {
	    	        cur_stroke[i] = temHZ[i];
	    	    } else {
	    	        cur_stroke[i] = 0;
	    	    }
			}
		}
		return cur_stroke;
    }
}

