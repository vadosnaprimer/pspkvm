package com.sun.midp.installer;

import java.io.InputStream;
import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.ConnectionNotFoundException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

 abstract class AppGalleryLoader implements Runnable {
    	private String url;
    	private DefaultHandler parser;

       public AppGalleryLoader(String url, DefaultHandler parser) {
           this.url = url;
           this.parser = parser;
       }

       protected abstract void onParserDone();

       protected abstract void onParserException(Exception ex);
       
    	public void run() {
    		HttpConnection conn = null;
    		InputStream in = null;
          try {
          	//com.sun.cldchi.jvm.JVM.setLogChannel(com.sun.cldchi.jvm.JVM.VM_LOGGING_CHANNEL_CONSOLE);
          	System.out.println("Connecting to "+url);
    		conn = (HttpConnection)Connector.open(url);
    		in = conn.openInputStream();
    		
    		SAXParserFactory factory = SAXParserFactory.newInstance();
    		SAXParser saxParser = factory.newSAXParser();
    		saxParser.parse(in,parser);
    	       onParserDone();
    	    } catch (IOException ioe) {
    	       onParserException(ioe);
    	    } catch (SAXException se) {
    		onParserException(se);
    	    } catch (ParserConfigurationException pe) {
    	       onParserException(pe);
    	    } finally {
    	       //com.sun.cldchi.jvm.JVM.setLogChannel(com.sun.cldchi.jvm.JVM.VM_LOGGING_CHANNEL_NONE);
    	       try {
    	         if (in != null) in.close();
    	         if (conn != null) conn.close();
    	       } catch (IOException e) {
    	       }
    	    }
    		
    	    //display.setCurrent(nextScreen);
    	}
}
    	
