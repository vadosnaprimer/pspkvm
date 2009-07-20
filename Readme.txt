pspkvm v0.5.2+ (README edited since 0.5.2 release)
20 July 2009
Authors: Sleepper, M@x, Anweifeng, AJ Milne
Email: pspkvm@gmail.com
Project website: 
	http://www.pspkvm.org
	http://www.pspkvm.com 
	http://www.pspkvm.cn
Project home on SF:
	https://sourceforge.net/projects/pspkvm

	 
--------------------------------
Change log (current source)
--------------------------------
- Bugfix for 'Default CPU speed setting doesn't work' (2812036)
- Bugfix for Sony Ericsson 480x272 profile issue (2812032)
- Bugfix for 'QWERTY board opens allcaps' (2806367)
- Bugfix makes available missing top half of Unicode page 0 in built-in font
	(Adds common accents incl. acute, grave, circumflex, umlaut over Roman characters)
- Added most of Unicode page 1 (Latin Extended 1) to built-in font
	(Adds less common accents and characters for most languages using Roman alphabets)
- Added clipboard/text selection support to the virtual keyboards
- Added a semichordal virtual keyboard implementation (fast input, diacritics support)


-----------------------------------
Changes in previous release (0.5.2)
-----------------------------------
- Large memory support
- New Chinese Input Method: Stroke IM
- Chinese localized menu
- Some bug fixes
- New screen resolution: 352x416
- Default CPU speed is configuable by pspkvm.ini
- Support daylight-saving time
- Old QWERTY virtual keyboard is supported again, configurable by pspkvm.ini


--------------------------------
General information
--------------------------------
PSPKVM is a PSP port of Sun's open-source JavaME implementation phoneMEFeature. The project description below is copied from the phoneME website:

	The objective of the phoneME project is to further expand the usage of Java Platform, Micro Edition (Java ME platform) technology in the mobile handset market. The project scope includes a focus on the mainstream feature phone segment with phoneME Feature software, and the emerging advanced phone segment with phoneME Advanced software. Our goal in making these technologies available to the Mobile & Embedded Community is to reduce implementation variation, increase the rate of innovation and enable new devices to leverage the power of the Java ME platform.
	
The phoneME website is here: https://phoneme.dev.java.net/

The goal of the pspkvm project is to port the phoneMEFeature project to the Sony PSP game console. If you're seeking a honeMEAdvanced port for the PSP, please see the project here: https://sourceforge.net/projects/pspme


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
- javacall_file_truncate isn't ported, so some file truncating operations, including some RMS operations, may not work as expected
- Multimedia temporary files may be left on your memory stick occasionally, especially after a VM crash. They're usually not big, but if you want to delete them manually, go to your PSP's /PSP/GAME/pspkvm/ directory, find the files whose names look like xxxxxxxx_tmp_mus.mid, and delete them.
- Fileconnection rmdir operation may fail for unknown reasons.
- LocationProvider.getState() will always return AVAILABLE once the GPS is initialized, so LocationListener.providerStateChanged() doesn't work as expected.


--------------------------------
Run from binary bundle
--------------------------------
1. Download the correct binary bundle:
- If you have a 1.50 kernel PSP and have installed pspkvm 0.4.2:
   Download pspkvm-bin-x.x.x-150-upgrade.zip
- If you want to run pspkvm on 3.xx OE and have installed pspkvm 0.4.2:
   Download pspkvm-bin-x.x.x-OE-upgrade.zip
- If you have a 1.50 kernel PSP and haven't installed pspkvm 0.4.2:
   Download pspkvm-bin-x.x.x-150-allinone.zip
- If you want to run pspkvm on 3.xx OE and haven't installed pspkvm 0.4.2:
   Download pspkvm-bin-x.x.x-OE-allinone.zip
2. Extract the zip to your PSP's /PSP/GAME/ or /PSP/GAME150 folder
3. Here we go!


--------------------------------
Building instructions
--------------------------------
For those interested in the source code:
0. You have to prepare the build environment for phoneME first. Please read the document at https://phoneme.dev.java.net/content/mr2/buildenv_feature.html#win_setup

Now assuming you have installed the build environment by following the above instructions, in Cygwin:
1. Retrieve the source code via svn to a local directory*
2. cd ${your_source_dir}
3. export JDK_DIR=${your_jdk_dir} (example: export JDK_DIR=c:/j2sdk1.4.2_16)
4. ./build-psp-cldc.sh
Now you should see the success message following the building of the phoneME libs.
5. cd psp
6. Make the executable:**
- If building for the 1.50 kernel:
    make kxploit
    You should get pspkvm and pspkvm% directories in this directory, just copy them to your PSP's /PSP/GAME or /PSP/GAME150 directory.
- If building for 3.xx OE:
    make BUILD_SLIM=true
    You should get EBOOT.PBP in this directory, copy it to your PSP's /PSP/GAME/pspkvm directory
7. Copy the "lib" and "appdb" directories from ${your_source_dir}/midp/build/javacall_psp/output/ into your PSP's /PSP/GAME/pspkvm directory
8. unzip midi_res.zip to your PSP's /PSP/GAME/pspkvm directory (If you need MIDI support)

* Note: you may have difficulty building the port with certain directory arrangements, due to Windows-based Java code failing properly to parse Posix-style Cygwin file paths. To avoid/correct this, place your pspkvm installation in a high or root directory, and mount it in Cygwin in such a fashion that the Posix and DOS paths wind up being the same thing. For example, place the pspkvm directory in the root of the C: drive (as in, in C:\pspkvm), then mount this directory on /pspkvm in Cygwin, with a command such as mount c:/pspkvm /pspkvm . Then run build-psp-cldc.sh from /pspkvm within Cywgin--the root directory should default to /pspkvm, and building should proceed correctly.

** Note: you will also need the PSP homebrew development toolchain and the pspsdk to build the binary for the PSP (step 6). As of this writing, both are available from http://ps2dev.org/psp/Tools/Toolchain/.

Requirements (pspsdk-specific libraries, also available from pspdev.org)
    SDL_mixer
    libvorbis
    libogg
    freetype2

Note that the PSPKVM is homebrew, and will not run on recent, stock PSP firmware, as these firmwares only run signed code. To run the PSPKVM binary, you need either a PSP with version 1.5 firmware, or a PSP with custom firmware, as with all homebrew.

    
--------------------------------
Running tips
--------------------------------
- To run a MIDlet from the memory stick:
	1) Select "Find Application" in the AMS (the item on the top)
	2) Select "Install from memory stick (ms0:/)"
	3) Browse the file system of your memory stick, and select the jad or jar to run
	4) After it's been selected, the jad/jar will be automatically installed and run. You can choose the installed application from the AMS the next time you want to run it.
	
- Text input:
	The PSP lacks a standard keyboard, but the PSPKVM provides other input methods. In any text input context, you can pop up a menu by pressing Right Soft-button, allowing you to change to another input method. We suggest using "abc" for entering alphabetic characters--this enulates the standard method of entering text on a standard cellphone keypad, allowing the input of English characters by pressing 0~9,#, and *. The phone keypad mapping for the PSP is, by default:
	
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

	In addition, most MIDlets will invoke your selected 'virtual keyboard' for involved text entry tasks, as these provide for much easier text input. See notes on virtual keyboards below.
	
- Multitasking
	In any application, pressing Left Trigger+Right Trigger+Triangle will send the currently running MIDlet to the background, and the AMS screen will be shown. The backgrounded MIDlet is shown highlighted, and you can choose another MIDlet to run without stopping the previous MIDlet. To bring a midlet running in the background to the foreground, just click the MIDlet item from AMS.
iy
	Note that currently we only allow two MIDlets to run at the same time; more will be allowed in the future.
	
- Connect to network
	Before a Java MIDlet can use the network, you need to connect your PSP to a WiFi hub. To do this, select "Network Setup" in the AMS (the second item in the AMS MIDlets list). All the WiFi connections you've set up on your PSP will be listed. Select one and wait for an alert popup telling you the connection attempt has been successful. Once the message "Successfully connected by profile xxx" appears, your network is ready, and you can go back to the AMS and run MIDlets that use the network.
	*Tips for 3.xx OE users: you don't have to set up network in the "Network Setup" menu. When the first MIDlet want to access the network, a PSP network setup dialog will popup and prompt you to start a connection. And also, you can press "L+R+Square" at any time to call up the network setup dialog.
	
- Select device to emulate
	When the user selects a jad/jar file from the memory stick to run, a "Device Select" menu will appear. Choose your preferred device size and model here, and press the CIRCLE key to select it. If you want to change the preferred device setting, go back to the AMS and move the highlight onto the MIDlet whose setting you want to change, then select "Select Device" from the menu.
	
- Change default key assignment for specified game
	If the default key assignment does not fit your game, you can go to the AMS and move the highlight onto the MIDlet whose key assignment you want to change, then select "Select Device" from the menu. Change the key setting to what you want. REMEMBER:"Shift" means Left Trigger or Right Trigger.
	
- Change defualt JSR75 Fileconnection root directories mapping in pspkvm.ini.
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
	
- Default Virtual Keyboard and Chinese Input tips:
	Confirm key (SHIFT+CIRCLE by default)   -> Switch input methods
	"*" (SHIFT+SELECT by default)           -> Switch input methods (same as above)
	2/4/6/8 (UP/LEFT/RIGHT/DOWN by default) -> Move focus on Virtual Keyboard
	Direction Keys (Analog stick by default)-> Move cursor
	CLEAR (SHIFT+CROSS by default)          -> Backspace
	0 (CROSS by default)                    -> Delete Pinyin (in Chinese input method)
	SEL, CPY, and PST (Select, copy, and paste) may be found in the 'Edit' key grouping. Select behaviour is 'latching'--press select once to begin a selection, move the cursor to shape it; the selection will be between wherever you were when you first pressed select and the current position of the cursor. Press SEL again to cancel the selection without editing it/overwriting it/deleting it, etc.
	
- Semichordal virtual keyboard input tips

	Set com.pspkvm.inputmethod = vk-semichordal in pspkvm.ini to enable the semichordal board.
	D-pad (arrow) and shoulder buttons set the 'live' chord; right-pad (triangle, square, circle, and cross keys) enter characters/execute editing functions. For alphabetic characters, the right shoulder acts as a shift key.
	Characters are generally grouped according to their frequency of usage, with the more commonly used ones (using English character frequencies) in the easier positions.
	'Toggle display mode' in the right hand menu, and the DSP key (Left-shoulder-up-right DPAD-circle) toggle between display modes, two of which offer instant feedback on which keys are currently 'live'. The 'large' display shows half the board at once (the half corresponding to the current position of the left shoulder button), and highlights your live chord. The 'small' display shows the currently live chord only. You can also hide the display entirely if you know the board well enough. Feedback in all the displays is instant--as you shift on the chordal keys, the display will change to show you what's live. Caps lock and selection status are shown in both the large and small displays.
	BSP and DEL (Backspace and delete) are on the centre chord (no D-Pad buttons pressed), with the left shoulder button down.
	Simple cursor controls (arrow keys) are on the left chord with the left shoulder button down (Left-shoulder-left-DPAD). Additional cursor controls (HME, END) are on the bottom left with left shoulder (Left-shoulder-down-left-DPAD). Turn on the large display (use DSP or 'Toggle display mode' in the menu to find them initially, until you get used to them.
	SEL (select) is latching. Press it once to turn select mode on; the selection will be between the current cursor and where you where when you pressed it. Press it a second time to kill the selection without doing anything with it. Editing 'on top' of a selection (copying into it, or entering text with the selection live, or hitting delete or backspace) will also kill the selection, as in normal word processing usage.
	DEL deletes to the right of the cursor, BSP to the left, as in normal word processing usage.
	For accents and other diacritical marks, enter the base character, and then the diacritical. As in, for e with an acute accent, enter an e, and then press the acute accent diacritical key--this is the key (on Left-shoulder-top-left-dpad-square) with the accent over an 'x'. All the diacritical keys are currently in this area--acute, grave, circumflex, umlaut/diaeresis, tilde, ring, and cedilla are currently supported, and can also be 'stacked' for characters for which this is supported in the Unicode character set (ie. multiple accents on the same character).
	The clipboard is supported in the semichordal board--see CPY and PST (copy and paste).

	- Use FreeType2 font files
	You can have 3 type of fonts: System, Proportional and Monospace. Copy your .ttf files into ms0:/PSP/GAME/PSPKVM and rename them as below:
	System font: sys.ttf
	Proportional font: pro.ttf
	Monospace font: mono.ttf
	If pro.ttf or mono.ttf is not found, sys.ttf will be used automatically; If none of the three font files are found, the internal built-in font will be used.
