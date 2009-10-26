import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

public class DZTest extends MIDlet implements CommandListener {
    private Command exitCommand;
    private DZTestCanvas socanvas;

    public DZTest() {
        exitCommand = new Command("Exit", Command.EXIT, 1);
        socanvas = new DZTestCanvas();
        socanvas.addCommand(exitCommand);
        socanvas.setCommandListener(this);
				socanvas.repaint(); }

    protected void startApp() {
        Display.getDisplay(this).setCurrent(socanvas);
        socanvas.repaint();
				try{ Thread.currentThread().sleep(2000); }
				catch(Exception e){} }

    protected void pauseApp() {}
    protected void destroyApp(boolean bool) {}

    public void commandAction(Command cmd, Displayable disp) {
        if (cmd == exitCommand) {
            destroyApp(false);
            notifyDestroyed();
        }
    }
}
