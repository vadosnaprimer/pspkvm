package javax.microedition.lcdui;

class IMCandidateBar {
    static CNLib cnlib;
    static Image IMimage;
    String pinyin = "";
    int cur_pos = 0;
    int cur_sel = 0;
    char[] cur_candidates = new char[5];

    public IMCandidateBar(int width, int height) {
    }

    private int getCandidateChars(String py, int pos , int num) {
    	int n = 0;
    	char[] cnchars;
    	
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
    	}
    	
    	return n;
    }
    
    public void keyPressed(char c) {
    	if (c >= 'a' && c <= 'z' && pinyin.length() < 6) {
    	    getCandidateChars(pinyin + c, 0, 5);
    	    pinyin = pinyin + c;
           cur_pos = 0;
           cur_sel = 0;
    	}
    }

    public void backspace() {
    	int len = pinyin.length();
    	if (len > 0) {
    	    pinyin = pinyin.substring(0, len - 1);
    	    getCandidateChars(pinyin, 0, 5);    	    
           cur_pos = 0;
           cur_sel = 0;
    	}
    }

    public void paint(Graphics g, int width, int height) {
    	int offset = (width - 90) / 5;
    	int x = 80;
    	g.drawImage(IMimage, 0, 5, Graphics.TOP+Graphics.LEFT);
    	g.drawString(pinyin, 20, 0, Graphics.TOP+Graphics.LEFT);
    	for (int pos = 0; pos < 5; pos++) {
    	    if (cur_candidates[pos] == 0) break;
    	    if (pos == cur_sel) {
    	    	g.setColor(0x88, 0x88, 0);
    	    } else {
    	       g.setColor(0x6699ff);
    	    }    	    
    	    g.fillRect(x, 0, 20, 25);
    	    g.setColor(0x0);
    	    g.drawChar(cur_candidates[pos], x, 0, Graphics.TOP+Graphics.LEFT);
    	    x+=offset;
    	}
    }

    public void prevPage() {
    	cur_pos -= 5;
    	if (cur_pos < 0) cur_pos = 0;
    	getCandidateChars(pinyin, cur_pos, 5);
    }

    public void nextPage() {
    	cur_pos += 5;
    	if (getCandidateChars(pinyin, cur_pos, 5) <= 0) cur_pos -= 5;
    	if (cur_candidates[cur_sel] == 0) cur_sel = 0;
    }

    public void prevChar() {
    	cur_sel --;
    	if (cur_sel < 0) {
    		cur_pos -= 5;
    		if (cur_pos < 0) cur_pos = 0;
    		cur_sel = 4;
    		getCandidateChars(pinyin, cur_pos, 5);
    	}
    }

    public void nextChar() {
    	cur_sel ++;
    	if (cur_sel > 4) {
    		cur_pos += 5;
    		if (getCandidateChars(pinyin, cur_pos, 5) > 0) {
    			cur_sel = 0;
    		} else {
    			cur_pos -= 5;
    			cur_sel = 4;
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
    	pinyin = ""; 	
    	for (int i = 0; i < 5; i++) {
    		cur_candidates[i] = 0;
    	}
    }

    private static byte[] cn = {    (byte)0x89, 
    (byte)0x50, (byte)0x4e, (byte)0x47, (byte)0x0d, (byte)0x0a, (byte)0x1a, (byte)0x0a, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0d, (byte)0x49, 
    (byte)0x48, (byte)0x44, (byte)0x52, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0d, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0d, (byte)0x08, 
    (byte)0x02, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xfd, (byte)0x89, (byte)0x73, (byte)0x2b, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01, 
    (byte)0x73, (byte)0x52, (byte)0x47, (byte)0x42, (byte)0x00, (byte)0xae, (byte)0xce, (byte)0x1c, (byte)0xe9, (byte)0x00, (byte)0x00, (byte)0x00, 
    (byte)0x04, (byte)0x67, (byte)0x41, (byte)0x4d, (byte)0x41, (byte)0x00, (byte)0x00, (byte)0xb1, (byte)0x8f, (byte)0x0b, (byte)0xfc, (byte)0x61, 
    (byte)0x05, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x20, (byte)0x63, (byte)0x48, (byte)0x52, (byte)0x4d, (byte)0x00, (byte)0x00, (byte)0x7a, 
    (byte)0x26, (byte)0x00, (byte)0x00, (byte)0x80, (byte)0x84, (byte)0x00, (byte)0x00, (byte)0xfa, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x80, 
    (byte)0xe8, (byte)0x00, (byte)0x00, (byte)0x75, (byte)0x30, (byte)0x00, (byte)0x00, (byte)0xea, (byte)0x60, (byte)0x00, (byte)0x00, (byte)0x3a, 
    (byte)0x98, (byte)0x00, (byte)0x00, (byte)0x17, (byte)0x70, (byte)0x9c, (byte)0xba, (byte)0x51, (byte)0x3c, (byte)0x00, (byte)0x00, (byte)0x00, 
    (byte)0x33, (byte)0x49, (byte)0x44, (byte)0x41, (byte)0x54, (byte)0x28, (byte)0x53, (byte)0x63, (byte)0xfc, (byte)0xff, (byte)0xff, (byte)0x3f, 
    (byte)0x03, (byte)0x31, (byte)0x00, (byte)0xa8, (byte)0x0e, (byte)0x0d, (byte)0x00, (byte)0x75, (byte)0x61, (byte)0x11, (byte)0x24, (byte)0x4d, 
    (byte)0x1d, (byte)0x1e, (byte)0x9b, (byte)0x21, (byte)0x06, (byte)0x41, (byte)0xad, (byte)0x40, (byte)0xb6, (byte)0x0b, (byte)0x2b, (byte)0x7b, 
    (byte)0xe0, (byte)0xd5, (byte)0xe1, (byte)0xf2, (byte)0x0a, (byte)0x8a, (byte)0x3f, (byte)0x90, (byte)0x43, (byte)0x87, (byte)0x1a, (byte)0xe1, 
    (byte)0x47, (byte)0x35, (byte)0xf3, (byte)0x00, (byte)0x1d, (byte)0x23, (byte)0x69, (byte)0xa6, (byte)0xf7, (byte)0x74, (byte)0x7d, (byte)0x20, 
    (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x49, (byte)0x45, (byte)0x4e, (byte)0x44, (byte)0xae, (byte)0x42, (byte)0x60, (byte)0x82};
    
    static {
    	cnlib = new CNLib();
    	IMimage = Image.createImage(cn,0,cn.length);
    }


}
