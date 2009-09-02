#!/bin/sh

# Kludgy method of dealing with encodings the compiler is choking on...

$JDK_DIR/bin/native2ascii.exe -encoding UnicodeLittle \
midp/src/highlevelui/keyboard/classes/javax/microedition/lcdui/SC_Keymap_Cyrillic.utf16.java \
midp/src/highlevelui/keyboard/classes/javax/microedition/lcdui/SC_Keymap_Cyrillic.java 

$JDK_DIR/bin/native2ascii.exe -encoding UnicodeLittle \
midp/src/highlevelui/keyboard/classes/javax/microedition/lcdui/SC_Keymap_NumericSymbolic_Roman.utf16.java \
midp/src/highlevelui/keyboard/classes/javax/microedition/lcdui/SC_Keymap_NumericSymbolic_Roman.java 

$JDK_DIR/bin/native2ascii.exe -encoding UnicodeLittle \
midp/src/highlevelui/keyboard/classes/javax/microedition/lcdui/SC_Keymap_Greek.utf16.java \
midp/src/highlevelui/keyboard/classes/javax/microedition/lcdui/SC_Keymap_Greek.java 

