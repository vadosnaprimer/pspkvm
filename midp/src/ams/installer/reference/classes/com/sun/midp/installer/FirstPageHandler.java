
package com.sun.midp.installer;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.util.*;


abstract class FirstPageHandler extends DefaultHandler
{
    protected int no_element;
    private String value;
    protected Vector elements;

    private String attributes[];
    private String element;

    protected String values[];

    public FirstPageHandler (String element, String attributes[])
    {
        no_element = 0;
        elements = new Vector(10);
        this.element = element;
        this.attributes = attributes;
        values = new String[attributes.length];
    }

  public void startDocument() throws SAXException {
      System.out.println("startDocument");
  }

  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
  {
      System.out.println("startElement: qName="+qName);
      value = "";
  }

  public void characters(char[] ch, int start, int length) throws SAXException
  {
      System.out.println("characters");
      try {
      for (int i = start; i < length; i++) {
          if (ch[i] != '\t' && ch[i] != '\n') {
              value += ch[i];
          }
      }
      } catch (Exception e) {
          e.printStackTrace();
      }
  }

  public void endElement(String uri, String localName, String qName) throws SAXException
  {
      System.out.println("endElement: qName="+qName);
      System.out.println("endElement: value="+value);
      
      if (!processAttributes(qName)) {
      	 if(element.equals(qName))
        {
          no_element++;
          onElement();
        }
      }

  }

  public void endDocument() throws SAXException
  {
      System.out.println("endDocument");
  }

  protected abstract void onElement();

  private boolean processAttributes(String attr) {
      int i;
      for (i = 0; i < attributes.length; i++) {
          if (attributes[i].equals(attr)) {
              values[i] = value;
              return true;
          }
      }

      return false;
  }

  public int getElementNum()
  {
      return no_element;
  }

  public Object getElement(int index)
  {
      return elements.elementAt(index);
  }
}

