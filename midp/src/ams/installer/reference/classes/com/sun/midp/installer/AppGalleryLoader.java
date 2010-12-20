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
    	//private final String url = "http://pspkvm.org/appgal/jarlist.php";
    	private String url;
    	private DefaultHandler parser;

       public AppGalleryLoader(String url, DefaultHandler parser) {
           this.url = url;
           this.parser = parser;
       }

       protected abstract void onParserDone();
       
    	public void run() {
          try {
          	System.out.println("loader.run...");
    		HttpConnection conn = (HttpConnection)Connector.open(url);
    		InputStream in = conn.openInputStream();
    		
    		SAXParserFactory factory = SAXParserFactory.newInstance();
    		//FirstPageHandler pagehandler = new FirstPageHandler();
    			SAXParser saxParser = factory.newSAXParser();
    			saxParser.parse(in,parser);
    		/*
    		int i = pagehandler.getJarEntryNum();
    		while (i-- != 0) {
    			JarEntry e = pagehandler.getJarEntry(i);
    			nextScreen.appendJarList(e);
    	       }*/
    	       onParserDone();
    	    } catch (IOException ioe) {
    	       ioe.printStackTrace();
    	    } catch (SAXException se) {
    		se.printStackTrace();
    	    } catch (ParserConfigurationException pe) {
    	       pe.printStackTrace();
    	    }
    		
    	    //display.setCurrent(nextScreen);
    	}
}
    	
