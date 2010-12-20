package com.sun.midp.installer;

import java.util.Vector;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import com.sun.midp.i18n.Resource;
import com.sun.midp.i18n.ResourceConstants;
import java.util.Timer;
import java.util.TimerTask;
import javax.microedition.io.ConnectionNotFoundException;

public final class AppGallery extends MIDlet {

    private AppGalleryJarList chooseApp = new AppGalleryJarList("Please select install location", List.IMPLICIT);
    private Display display;
    private SplashScreen splashScreen;

    private Command backCmd =
		new Command(Resource.getString(ResourceConstants.BACK), Command.BACK, 1);
    private Command cancelCmd =
		new Command(Resource.getString(ResourceConstants.CANCEL),
			Command.CANCEL, 1);

    private boolean cancel_conn = false;
    private Timer tickerTimer = new Timer();
         
    public AppGallery() {
    	
    	display = Display.getDisplay(this);
    	splashScreen = new SplashScreen(this);

    	chooseApp.setCommandListener(new CommandListener() {
         	                            			public void commandAction(Command c, Displayable d) {
										System.out.println("commandAction");	
										if (c == cancelCmd) {
											cancel_conn = true;
											return; }
										if (c == backCmd) {
											showFirstPage();
											return;
										}
										if (c == List.SELECT_COMMAND) {
											System.out.println("SELECT");
											int i = ((List)d).getSelectedIndex();
											showInformation(chooseApp.getJarEntry(i)); }
									}
								});
    
    }
    
    public void startApp() {
    	display.setCurrent(splashScreen);
       splashScreen.repaint();
       chooseApp.setTicker(new Ticker(""));
       chooseApp.addCommand(backCmd);
       System.out.println("Start AppGallery");
           
         TickerTimerTask tickerTask = new TickerTimerTask(chooseApp);
         tickerTimer.scheduleAtFixedRate(tickerTask, 0, 1000);
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    	System.out.println("Quit appGallery");
    	tickerTimer.cancel();
    }


    void showFirstPage() {
      String url = "http://pspkvm.org/appgal/appgal.php";

      String attr[] = {"text", "action"};
      final FirstPageHandler handler = new FirstPageHandler("entry", attr) {
         	protected void onElement() {
         	    Vector v = new Vector(2);
         	    v.addElement(values[0]);
         	    v.addElement(values[1]);
         	    elements.addElement(v);
         	}
      };
         new Thread(new AppGalleryLoader(url, handler) {

         	                            protected void onParserDone() {
         	                            	final List list = new List("Welcome", List.IMPLICIT);
         	                            	final Command backCmd;
	
							backCmd = new Command("Back", Command.BACK, 0);
							list.addCommand(backCmd);
							list.setCommandListener(new CommandListener() {
									public void commandAction(Command c, Displayable d) {
										int type = c.getCommandType();
											
										if (c == List.SELECT_COMMAND) {
											int sel = list.getSelectedIndex();											
											String action = (String)((Vector)handler.getElement(sel)).elementAt(1);

											if ("ShowJarList".equals(action)) {
												showJarList(false);
											} else if ("ShowFeatured".equals(action)) {
												showJarList(true);
											} else if ("ShowNews".equals(action)) {
											}
										} else if (c == backCmd) {
											AppGallery.this.notifyDestroyed();
										}
									}
								});

							int i = handler.getElementNum();
							for (int j = 0; j < i; j++) {
								list.append((String)((Vector)handler.getElement(j)).elementAt(0), null);
							}
							
					    		display.setCurrent(list);
         	                            }
         	                      }).start();
    }

    private void showInformation(final JarEntry entry) {
      
      String url = "http://pspkvm.org/appgal/showinfo.php?id="+entry.getID();

      System.out.println("Get information:"+url);
      
      String attr[] = {"description"};
      final FirstPageHandler handler = new FirstPageHandler("info", attr) {
         	protected void onElement() {
         	    elements.addElement(values[0]);
         	}
      };
         new Thread(new AppGalleryLoader(url, handler) {

         	                            protected void onParserDone() {
         	                            	Form form = new Form("Information");
         	                            	Command backCmd;
							Command installCmd;
	
							backCmd = new Command("Back", Command.BACK, 1);
							installCmd = new Command("Install", Command.OK, 0);
							form.addCommand(backCmd);
							form.addCommand(installCmd);
							form.setCommandListener(new CommandListener() {
									public void commandAction(Command c, Displayable d) {
										int type = c.getCommandType();
										Display display = AppGallery.this.display;
											
										switch (type) {
											case Command.BACK:
												display.setCurrent(AppGallery.this.chooseApp);
												break;
											case Command.OK:
												downloadJar(entry);
												break;
										}
									}
								});
							
         	                            	StringItem item = new StringItem("Application name:", entry.getTitle());
         	                            	item.setLayout(Item.LAYOUT_EXPAND);
         	                            	form.append(item);
         	                            	item = new StringItem("Description:", (String)handler.getElement(0));
         	                            	item.setPreferredSize(400, -1);
         	                            	item.setLayout(Item.LAYOUT_NEWLINE_BEFORE|Item.LAYOUT_EXPAND|Item.LAYOUT_CENTER);
         	                            	form.append(item);
         	                            	
					    		display.setCurrent(form);
         	                            }
         	                      }).start();
    }

    private void showJarList(boolean featured) {
    	  final String SHOWJAR_URL = "http://pspkvm.org/appgal/jarlist.php";
    	  final String SHOWFEATURED_URL = "http://pspkvm.org/appgal/jarlist.php?featured=1";
    	  
         String attr[] = {"id", "name", "title", "smalldesc", "filesize"};

         chooseApp.reset();
         
         final FirstPageHandler handler = new FirstPageHandler("midlet", attr) {
         	protected void onElement() {
         	    elements.addElement(new JarEntry(Integer.parseInt(values[0]), 
          	                                                 values[1], 
          	                                                 values[2],
          	                                                 values[3],
          	                                                 values[4]));
         	}
         };

         String url;
         if (featured) url = SHOWFEATURED_URL;
         else url = SHOWJAR_URL;
         
         new Thread(new AppGalleryLoader(url, handler) {

         	                            protected void onParserDone() {
         	                            	int i = handler.getElementNum();
         	                            	AppGalleryJarList nextScreen = AppGallery.this.chooseApp;

					    		while (i-- != 0) {
					    			JarEntry e = (JarEntry)handler.getElement(i);
					    			nextScreen.appendJarList(e);
					    	       }
					    		display.setCurrent(nextScreen);
         	                            }
         	                      }).start();
    }

    private void downloadJar(JarEntry entry) {
      try {
      	 String url = "http://pspkvm.org/appgal/test.php?name="+entry.getName();

      	 System.out.println("platformRequest to:"+url);
        if (platformRequest(url)) {
             System.out.println("Quit appGallery to install...");
             notifyDestroyed();             
        }
      } catch (ConnectionNotFoundException e) {
        e.printStackTrace();
      }
    }

    private final class TickerTimerTask extends TimerTask {
        private Ticker ticker;
        private AppGalleryJarList appList;
        
        /**
         * Create a new timeout task
         */
        TickerTimerTask(AppGalleryJarList appList) {
             this.ticker = appList.getTicker();
             this.appList = appList;
        }

        /**
         * Switch to the the App Manager Screen.
         */
        public void run() {
           String message;
           int i = appList.getSelectedIndex();
           if (i >= 0) {
             message = appList.getJarEntry(i).getSmallDesc();
             if (message == null) message = "";
             ticker.setString(message);
           }
        }
    } // TimeoutTask
  
}

final class SplashScreen extends Canvas {

    /** Splash screen image */
    private Image splashScreen;
    private AppGallery appgal;


    /**
     * Creates a Splash screen.
     * @param display - the display associated wit the Manager midlet
     * @param nextScreen - the screen to be displayed after timeout
     */
    SplashScreen(AppGallery appGallery) {
        appgal = appGallery;

        setFullScreenMode(true);
    }

    /**
     * Paint splash screen.
     *
     * @param g Graphics instance to paint on
     */
    public void paint(Graphics g) {

        // White background
        g.setColor(255, 255, 0);
        g.fillRect(0, 0, getWidth(), getHeight());
        System.out.println("paint");
        g.setColor(0,0,0);
        System.out.println("draw string");
        
        g.drawString("Loading...", 0, 0, Graphics.LEFT | Graphics.TOP);
        System.out.println("Loading from jarlist");
        
        if (splashScreen != null) {
            g.drawImage(splashScreen, 0, 0, Graphics.LEFT | Graphics.TOP);
        }
    }

    /**
     * Override showNotify to set a timer task so that Splash screen
     * automatically switches to the App Manager Screen.
     */
    public void showNotify() {
         System.out.println("Splash showNotify");
         appgal.showFirstPage();
    }

    /**
     * This method is called when available area of 
     * the Displayable has been changed.
     */
    protected  void  sizeChanged(int w, int h) {
        splashScreen = 
            GraphicalInstaller.getImageFromInternalStorage("splash_screen_" 
            + getWidth() + "x" + getHeight());
        
    }

    /**
     * Override hideNotify  to cancel timer task.
     */
    public void hideNotify() {
    }

}

