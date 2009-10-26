import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

public class TPos extends MIDlet implements CommandListener {
    private Command exitCommand;
    private TPosForm tposform;

    public TPos() {
        exitCommand = new Command("Exit", Command.EXIT, 1);
        tposform = new TPosForm("Test form");
        tposform.addCommand(exitCommand);
        tposform.setCommandListener(this); }

    protected void startApp() {
        Display.getDisplay(this).setCurrent(tposform);
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
