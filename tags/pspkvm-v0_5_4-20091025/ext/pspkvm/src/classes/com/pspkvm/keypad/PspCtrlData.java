package com.pspkvm.keypad;

// NB: Not currently used, as far as I know. See PSPCtrlCodes.java

public class PspCtrlData {
  public static int PSP_CTRL_SELECT = 0x000001; 
  public static int PSP_CTRL_START = 0x000008; 
  public static int PSP_CTRL_UP = 0x000010; 
  public static int PSP_CTRL_RIGHT = 0x000020;
  public static int PSP_CTRL_DOWN = 0x000040; 
  public static int PSP_CTRL_LEFT = 0x000080; 
  public static int PSP_CTRL_LTRIGGER = 0x000100; 
  public static int PSP_CTRL_RTRIGGER = 0x000200;
  public static int PSP_CTRL_TRIANGLE = 0x001000; 
  public static int PSP_CTRL_CIRCLE = 0x002000; 
  public static int PSP_CTRL_CROSS = 0x004000; 
  public static int PSP_CTRL_SQUARE = 0x008000;
  public static int PSP_CTRL_HOME = 0x010000; 
  public static int PSP_CTRL_HOLD = 0x020000; 
  
  public static int PSP_CTRL_MODE_DIGITAL = 0;
  public static int PSP_CTRL_MODE_ANALOG = 1;

  public int TimeStamp;
  public int Buttons;
  public int Lx;
  public int Ly;
}
