import javax.microedition.lcdui.*;

public class DZTestCanvas extends Canvas {

	// Display colours
	private static final int WHITE = 0xffffff;
	private static final int BLACK = 0x000000;
	private static final int DKBLUE = 0x000040;
	private static final int DKRED = 0x400000;
	private static final int GREY = 0xc0c0c0;
	private static final int BGCOLOR = GREY;
	
	// Strings for the backspace, space key displays
	static final String BSPG = "\ue023";
	static final String SPCG = "\ue02b";
	
	// Utility font--used to draw the key glyphs
	static final Font utility_font =
		Font.getFont(Font.FACE_UTILITY, Font.STYLE_BOLD, Font.SIZE_SMALL);
	
	// Display map
	static final String[] map_0 = {
		BSPG, "m", SPCG, "n", 
		"?", "o", "p", "q",
		"!", "g", "h", "i",
		".", "d", "e", "f",
		",", "a", "b", "c",
		"-", "j", "k", "l",
		"(", "r", "s", "t",
		":", "u", "v", "w",
		")", "x", "y", "z" };
		
	static final String[] map_1 = {
		BSPG, "M", SPCG, "N", 
		"\"", "O", "P", "Q",
		"*", "G", "H", "I",
		"@", "D", "E", "F",
		"^", "A", "B", "C",
		"_", "J", "K", "L",
		"=", "R", "S", "T",
		";", "U", "V", "W",
		"/", "X", "Y", "Z" };
		
	// Display map
	static final String[] map_2 = {
		BSPG, "", SPCG, "5", 
		"", "", "", "6",
		"", "", "", "3",
		"", "", "", "2",
		"", "", "", "1",
		"", "", "", "4",
		"", "", "", "7",
		"", "", "", "8",
		"", "", "0", "9" };

	static final String[] map_3 = {
		BSPG, "", SPCG, "", 
		"+", "\\", "=", "/",
		"-", "[", "_", "]",
		"\"", "<", "'", ">",
		",", "(", ".", ")",
		"!", "{", "?", "}",
		":", "@", ";", "#",
		"~", "$", "`", "%",
		"*", "^", "|", "&" };
		
	static final String[][] maps = {map_0, map_1, map_2, map_3};

	// Letter offsets within key displays
	private final static int[] ltr_x = { 0, -12, 0, 12 };
	private final static int[] ltr_y = { 0, 12, 24, 12 };
	// Key offsets from origin
	private final static int[] key_x = {
		1, 2, 2, 1, 0, 0, 0, 1, 2 };
	private final static int[] key_y = {
		1, 1, 0, 0, 0, 1, 2, 2, 2 };
		
	final static int GRPWIDTH = 40;
	final static int GRPHEIGHT = 40;
	final static int GAPSIZE = 2;
	
	// The current key (in the board)
	int live_key;
	// The current board
	int cboard;
	
	// Get where to start drawing the board--x
	int getXOrig() {
		return getWidth()-(GRPWIDTH*3)-(GAPSIZE*2); }
	// Get where to start drawing the board--y
	int getYOrig() {
		return getHeight()-(GRPHEIGHT*3)-(GAPSIZE*2); }
	
	// Main paint method
	public void paint(Graphics g) {
		g.setColor(BGCOLOR);
		g.fillRect(0, 0, getWidth(), getHeight());
		int x_orig = getXOrig();
		int y_orig = getYOrig();
		for(int i=0; i<9; i++) {
			draw_key(g, maps[cboard], i*4, 
				x_orig + (key_x[i]*(GRPWIDTH+GAPSIZE)),
				y_orig + (key_y[i]*(GRPHEIGHT+GAPSIZE)),
				i == live_key); } }
	
	// Draw a single key
	void draw_key(Graphics g, String[] k, int o, int x, int y, boolean live) {
		g.setColor(live ? DKRED : DKBLUE);
		g.fillRect(x, y, GRPWIDTH, GRPHEIGHT);
		g.setColor(WHITE);
		x+=(GRPWIDTH/2);
		x+=1;
		y+=1;
		for(int i=0; i<4; i++) {
			int w = utility_font.stringWidth(k[o]);
			g.drawUtilityString(k[o], x+ltr_x[i]-(w/2), y+ltr_y[i], g.TOP|g.LEFT);
			o++; } }

	public DZTestCanvas() { cboard=0; live_key=0; }
		
	public void showNotify() {
		repaint(); }
		
	public void sizeChanged(int w, int h) {
		repaint(); }
		
	// Stand-in method -- this won't be the real one
	public void keyPressed(int keycode) {
		if (keycode=='4') {
			cboard--; }
		if (keycode=='6') {
			cboard ++; }
		if (keycode == '2') {
			live_key --; }
		if (keycode == '8') {
			live_key ++; }
		cboard += maps.length;
		cboard %= maps.length;
		live_key += 9;
		live_key %= 9;
		repaint(); }
}
