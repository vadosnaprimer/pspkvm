PSPKVM 0.5.4 release version
24 October 2009
Developers: Sleepper, M@x, Anweifeng, AJ Milne
Testing: Jurgen Konings 
Email: feedback@pspkvm.org, pspkvm@gmail.com 
Project website: 
	http://www.pspkvm.org
	http://www.pspkvm.com 
	http://www.pspkvm.cn
Project home on SF:
	https://sourceforge.net/projects/pspkvm
Project wiki:
	http://sourceforge.net/apps/trac/pspkvm/wiki/WikiStart

	
--------------
General README
--------------

Additional documentation
------------------------

 * See CHANGELOG.TXT for changes in this and previous releases.
 * See BUILDING.TXT for notes on building this software.

---------------------------------
General information
---------------------------------

PSPKVM is a PSP port of Sun's open-source JavaME implementation
phoneMEFeature. The project description below is as it appears on the
phoneME website:

The objective of the phoneME project is to further expand the usage of Java
Platform, Micro Edition (Java ME platform) technology in the mobile handset
market. The project scope includes a focus on the mainstream feature phone
segment with phoneME Feature software, and the emerging advanced phone
segment with phoneME Advanced software. Our goal in making these technologies
available to the Mobile & Embedded Community is to reduce implementation
variation, increase the rate of innovation and enable new devices to leverage
the power of the Java ME platform.
	
The phoneME website is at: https://phoneme.dev.java.net/

The goal of the pspkvm project is to port the phoneMEFeature project to the
Sony PSP game console. If you're seeking a phoneMEAdvanced port for the PSP,
please see the project here: https://sourceforge.net/projects/pspme


--------------------------------
Features
--------------------------------

- Nokia UI APIs (partial)
- WMA1.1(JSR120) stub
- Networking (via the PSP's WiFi connection)
- Java AMS with MVM support
- Several input methods: QWERTY/Abc/Symbol/...
- Directly browse and run from local jad/jar file, and auto-install without interrupting
- JPEG support
- MIDI & Wave audio playback support
- Device emulation. You can choose a device type to emulate for different screen sizes and key codes, either at MIDlet installation time or from the "Select device" menu
- Custom key assignment per application
- JSR75(File Connection)
- Virtual Keyboard Input
- Chinese Input(Pinyin, Stroke)
- JSR179(GPS)
- FreeType2 font support
- JSR 226(SVG)
- JSR 172(Web Service)


--------------------------------
TODO
--------------------------------
- Graphic optimization (High)
- JSR184 (Low)
- MIPS JIT compiler and Interpreter generator (Low)


--------------------------------
Bugs and known issues
--------------------------------
Known issues:

- Not running on 5.02 Gen-A firmware

- javacall_file_truncate isn't ported, so some file truncating operations,
including some RMS operations, may not work as expected

- Multimedia temporary files may be left on your memory stick occasionally,
especially after a VM crash. They're usually not big, but if you want to
delete them manually, go to your PSP's /PSP/GAME/pspkvm/ directory, find
the files whose names look like xxxxxxxx_tmp_mus.mid, and delete them.

- Fileconnection rmdir operation may fail for unknown reasons.

- LocationProvider.getState() will always return AVAILABLE once the GPS
is initialized, so LocationListener.providerStateChanged() doesn't work
as expected.


----------------------------------
Running PSPKM from a binary bundle
----------------------------------

1. Download the correct binary bundle:
- If you have a 1.50 kernel PSP and have installed pspkvm 0.5.3 or a 0.5.4
test version:
   Download pspkvm-bin-x.x.x-OFW-upgrade.zip
- If you want to run pspkvm on custom firmware and have installed pspkvm
0.5.3 or a 0.5.4 test version:
   Download pspkvm-bin-x.x.x-CFW-upgrade.zip
- If you have a 1.50 kernel PSP and haven't installed a recent version:
   Download pspkvm-bin-x.x.x-OFW-allinone.zip
- If you want to run pspkvm on custom firmware and haven't installed
a recent version:
   Download pspkvm-bin-x.x.x-CFW-allinone.zip
   
Note that there are also 0.5.4-allinone-minimal packages. These do not
contain TTF or MIDI files. They are appropriate if you expect to install
your own fonts, or do not intend to use TTF fonts, and do not intend to
use MIDI.

2. Extract the zip to your PSP's /PSP/GAME/ or /PSP/GAME150 folder

--------------------------------
Running tips
--------------------------------

Using the X key for 'accept' instead of O
-----------------------------------------

PSPKVM ships with the O key as its 'accept' button, and X as cancel,
as is common in parts of Asia. If you wish, however, to use X as your
accept button, you may reconfigure this from the GUI. In
System midlets->Configure VM->Keymap settings, select 'Western' under
your default keymap to change this.


Font subsystem and virtual keyboard settings are now in the GUI
---------------------------------------------------------------

In previous versions of pspkvm, you changed your font sizes (and turned
the internal font on and off) and your virtual keyboard settings in
pspkvm.ini. This file no longer controls these settings. You may now use
the Configure VM midlet in the System midlets folder to control these.


To install and run a MIDlet from the memory stick:
--------------------------------------------------

	1) Select "Find Application" in the AMS (the item on the top)
	2) Select "Install from memory stick (ms0:/)"
	3) Browse the file system of your memory stick, and select the jad or jar
to run
	4) After it's been selected, the jad/jar will be automatically installed
and run. You can choose the installed application from the AMS the next time
you want to run it.
	

To install and run a MIDlet from the web:
-----------------------------------------

	1) Select "Find Application" in the AMS (the item on the top)
	2) Select "Install from http"
	3) Enter the address of the jad or jar file to run
	4) After it's been selected, the jad/jar will be automatically installed and
run. You can choose the installed application from the AMS the next time you
want to run it.


Text input:
-----------

The PSP lacks a standard keyboard, but the PSPKVM provides other input methods. In any text input context, you can pop up a menu by pressing Right Soft-button, allowing you to change to another input method. We suggest using "abc" for entering alphabetic characters--this emulates the standard method of entering text on a standard cellphone keypad, allowing the input of English characters by pressing 0~9,#, and *. The phone keypad mapping for the PSP is, by default:
	
	NUM0: Cross
	NUM1: Square
	NUM2: UP
	NUM3: Triangle
	NUM4: LEFT
	NUM5: Shift+Circle
	NUM6: RIGHT
	NUM7: Shift+Square
	NUM8: DOWN
	NUM9: Shift+Triangle
	*:    Shift+SELECT
	#:    Shift+START
	CLEAR: Shift+Cross
	SELECT: Circle
	Left Soft: SELECT
	Right Soft: START
	UP/DOWN/LEFT/RIGHT: Analog joy stick
	(* "Shift" = holding Left Trigger or Right Trigger)
	
In addition to these, PSPKVM provides the following functions hard-mapped to key combinations:

	Left Trigger + Right Trigger + Triangle: Multi-tasking key (Click to return AMS and put MIDlet to background)
	Left Trigger + Right Trigger + Cross:    Exit current running MIDlet

In addition, most MIDlets will invoke your selected 'virtual keyboard' for involved text entry tasks, as these provide for much easier text input. See notes on virtual keyboards below and in the wiki at http://sourceforge.net/apps/trac/pspkvm/wiki/VirtualKeyboards , and additional notes below.

	
Multitasking
------------

In any application, pressing Left Trigger+Right Trigger+Triangle will send the currently running MIDlet to the background, and the AMS screen will be shown. The backgrounded MIDlet is shown highlighted, and you can choose another MIDlet to run without stopping the previous MIDlet. To bring a midlet running in the background to the foreground, just click the MIDlet item from AMS.

	
Connecting to the network
-------------------------

Before a Java MIDlet can use the network, you need to connect your PSP to a WiFi hub. To do this, select "Network Setup" in the AMS (the second item in the AMS MIDlets list). All the WiFi connections you've set up on your PSP will be listed. Select one and wait for an alert popup telling you the connection attempt has been successful. Once the message "Successfully connected by profile xxx" appears, your network is ready, and you can go back to the AMS and run MIDlets that use the network.
	*Tips for 3.xx OE users: you don't have to set up network in the "Network Setup" menu. When the first MIDlet want to access the network, a PSP network setup dialog will popup and prompt you to start a connection. And also, you can press "L+R+Square" at any time to call up the network setup dialog.

	
Selecting a device to emulate
-----------------------------

When the user installs a Midlet, a "Device Select" menu will appear. Choose your preferred device size and model here, and press the CIRCLE key to select it. If you want to change the preferred device setting, go back to the AMS and move the highlight onto the MIDlet whose setting you want to change, then select "Select Device" from the menu.
	

Changing the default key assignment for specified midlet
--------------------------------------------------------

If the default key assignment does not fit your game, you can go to the AMS and move the highlight onto the MIDlet whose key assignment you want to change, then select "Select Device" from the menu. Change the key setting to what you want. REMEMBER:"Shift" means Left Trigger or Right Trigger.

	
Changing default JSR75 Fileconnection root directories mapping in pspkvm.ini
----------------------------------------------------------------------------

	The default root directories mapping is as following:
	/root -> ms0:/pspkvm/
	/music -> ms0:/PSP/MUSIC/
	/photo -> ms0:/PSP/PHOTO/
	/video -> ms0:/PSP/VIDEO/
	/recordings -> ms0:/pspkvm/
	/tones -> ms0:/pspkvm/
	/graphics -> ms0:/pspkvm/
	/private -> ms0:/pspkvm_pri/

However, you can change the default mapping in pspkvm.ini. To do this, open pspkvm.ini in the PSPKVM install directory, and change the values in the [jsr75] section. If the value is left blank, the default value is applied.


Selecting a virtual keyboard
----------------------------

When a MIDlet requests text input, generally a 'virtual keyboard' will be popped up. You can choose from a range of keyboards on PSPKVM. See the 'Virtual keyboards' menu in the VM Configuration midlet to choose one. More information on the available boards is in the wiki at http://sourceforge.net/apps/trac/pspkvm/wiki/VirtualKeyboards

	
Default virtual keyboard and Chinese input (AWF virtual keyboard) tips
----------------------------------------------------------------------

	Confirm key (SHIFT+CIRCLE by default)   -> Switch input methods
	"*" (SHIFT+SELECT by default)           -> Switch input methods (same as above)
	2/4/6/8 (UP/LEFT/RIGHT/DOWN by default) -> Move focus on Virtual Keyboard
	Direction Keys (Analog stick by default)-> Move cursor
	CLEAR (SHIFT+CROSS by default)          -> Backspace
	0 (CROSS by default)                    -> Delete Pinyin (in Chinese input method)
	SEL, CPY, and PST (Select, copy, and paste) may be found in the 'Edit' key grouping. Select behaviour is 'latching'--press select once to begin a selection, move the cursor to shape it; the selection will be between wherever you were when you first pressed select and the current position of the cursor. Press SEL again to cancel the selection without editing it/overwriting it/deleting it, etc.
	General information on the virtual keyboards may be found in the wiki at http://sourceforge.net/apps/trac/pspkvm/wiki/VirtualKeyboards

	
Danzeff virtual keyboard input tips
-----------------------------------

	You can enable the semichordal board in the VM Config midlet in the System Midlets folder.
	The analog stick selects your live keygroup. Press the symbol keys to do entry.
	The arrow keys (DPAD) direct the cursor.
	The right shoulder button acts as a shift key.
	The left shoulder button toggles a symbol keypad on and off.
	General information on the virtual keyboards may be found in the wiki at http://sourceforge.net/apps/trac/pspkvm/wiki/VirtualKeyboards

	
Semichordal virtual keyboard input tips
---------------------------------------

	You can enable the semichordal board in the VM Config midlet in the System Midlets folder.
	D-pad (arrow) and shoulder buttons set the 'live' chord; right-pad (triangle, square, circle, and cross keys) enter characters/execute editing functions. For alphabetic characters, the right shoulder acts as a shift key.
	Characters are generally grouped according to their frequency of usage, with the more commonly used ones (using English character frequencies for the Roman board, Russian for Cyrillic, and modern Greek for Greek) in the easier positions.
	'Toggle display mode' in the right hand menu, and the display key (Left-shoulder-up-right DPAD-circle) toggle between display modes, two of which offer instant feedback on which keys are currently 'live'. The 'large' display shows half the board at once (the half corresponding to the current position of the left shoulder button), and highlights your live chord. The 'small' display shows the currently live chord only. You can also hide the display entirely if you know the board well enough. Feedback in all the displays is instant--as you shift on the chordal keys, the display will change to show you what's live. Caps lock and selection status are shown in both the large and small displays.
	The backspace and delete keys are on the centre chord (no D-Pad buttons pressed), with the left shoulder button down.
	Simple cursor controls (arrow keys) are on the left chord with the left shoulder button down (Left-shoulder-left-DPAD). Additional cursor controls (analagous to Home and End) are on the bottom left with left shoulder (Left-shoulder-down-left-DPAD). Turn on the large display (use the display key or 'Toggle display mode' in the menu to find them initially, until you get used to them.
	The select key is latching. Press it once to turn select mode on; the selection will be between the current cursor and where you where when you pressed it. Press it a second time to kill the selection without doing anything with it. Editing 'on top' of a selection (copying into it, or entering text with the selection live, or hitting delete or backspace) will also kill the selection, as in normal word processing usage.
	The delete key deletes to the right of the cursor, backspace to the left, as in normal word processing usage.
	For accents and other diacritic marks, enter the base character, and then the diacritic. As in, for e with an acute accent, enter an e, and then press the acute accent diacritical key--this is the key (on Left-shoulder-top-left-dpad-square) with the accent over an 'x'. All the diacritic keys are currently in this area--acute, grave, circumflex, umlaut/diaeresis, tilde, ring, and cedilla are currently supported, and can also be 'stacked' for characters for which this is supported in the Unicode character set (ie. multiple accents on the same character).
	The clipboard is supported in the semichordal board.
	See the wiki at https://sourceforge.net/apps/trac/pspkvm/wiki/UsingTheSemichordalBoard for more information on using the semichordal board.
	
	
Using FreeType2 font files
--------------------------

	You can have 13 fonts installed: System, proportional and monospace, each in normal, italic, bold, and bold-italic, and a fallback font. Copy your .ttf files into ms0:/PSP/GAME/PSPKVM and rename them as below:
	System font: sys.ttf, sys_i.ttf, sys_b.ttf, sys_ib.ttf (normal, italic, bold, italic-bold)
	Proportional font: prop.ttf, prop_i.ttf, prop_b.ttf, prop_ib.ttf (normal, italic, bold, italic-bold)
	Monospace font: mono.ttf, mono_i.ttf, mono_b.ttf, mono_ib.ttf (normal, italic, bold, italic-bold)
	Fallback font: fallback.ttf (this is a good place for a large pan-Unicode or Han character font --since these tend not to have italic and bold faces anyway--its glyphs will fill in when none are found in the above fonts)
	Generally, the system will fall back intelligently if some of these are missing. If you have 	only one font, just call it sys.ttf. If no fonts are present, the internal built-in font will be used.
	Note that there's a difference between how the system falls back through the system, proportional, and monospace fonts, and how it falls back to the fallback font. It falls back per font through system, proportional, and monospace, and it falls back per glyph to the fallback font. That is: at startup, it will assign a font file to each of main twelve slots, attempting to assign the best match for each from the existing files you provide. Then during rendering, if a glyph is not found for a given character in the proper font file, it will attempt to find it in the fallback font, if it is present.
	The first set set of three fonts and four weights/styles corresponds to the J2ME MIDP API specification--midlet authors writing code for J2ME devices will frequently write code with the expectation that these are the available fonts in the system.
	Midlet authors also can specify three font sizes when drawing fonts--small, medium, and large. You can specify the pixel sizes of these in the 'Font subystem' dialogue in the VM configuration midlet.
	You can also turn off FreeType2 rendering entirely in the Font subsystem dialogue, by specifying 'Use internal font' in the VM Settings midlet. If you do this, a non-scalable built-in bitmap font will be used for drawing text.
	For additional information on font support, see the wiki at http://sourceforge.net/apps/trac/pspkvm/wiki/ConfiguringFourteenFont


Note re running homewbrew on your PSP
-------------------------------------

Note that the PSPKVM is homebrew, and will not run on recent, stock PSP firmware, as these firmwares only run signed code. To run the PSPKVM binary, you need either a PSP with version 1.5 firmware, or a PSP with custom firmware, as with all homebrew. See also the notes in the Wiki at http://sourceforge.net/apps/trac/pspkvm/wiki/RunningPSPHomebrew
