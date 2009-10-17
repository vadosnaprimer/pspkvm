pspkvm v0.5.4 test 10
17 October 2009
Author: Sleepper, M@x, Anweifeng, AJ Milne
Email: pspkvm@gmail.com
Project website: 
	http://www.pspkvm.org
	http://www.pspkvm.com 
	http://www.pspkvm.cn
Project home on SF:
	https://sourceforge.net/projects/pspkvm
Wiki--has most current help in English and Chinese:
	http://sourceforge.net/apps/trac/pspkvm/wiki/WikiStart 

------------------------------
Note--official release pending
------------------------------

Barring any severe bugs reported in this test build, this will in all
probability be our last test build before the official release of 0.5.4.
Thanks to all users who tested and commented and made this release better.
Official release should be Oct. 18, 2009.

---------------------------------------
Changes in release (relative to Test 9)
---------------------------------------

Summary: critical bugfixes, few minor cleanups

 * Bugfix: 'zombie midlet' bug in which marked, removed midlets would reappear
on a subsequent move has been fixed. Loading code has also been modified to allow
loading of the folder system in the AMS even after a crash due to this bug. If
you got locked out of your VM because of this, using this build will let you back
in.

 * Bugfix: missing utility/symbols file no longer brings down the VM when
opening virtual keyboards using it.

 * Bugfix: unused menu items removed from Danzeff board.     

-------------------------------------
Changes in branch (relative to 0.5.3)
-------------------------------------

There's a Danzeff-style virtual keyboard available now. See the VM Configuration
midlet to enable it.

The AMS supports folders.

The semichordal board supports Greek and Cyrillic scripts, and you can customize
which one you want to come up by default in the VM Config midlet.

The semichordal board now uses a custom TrueType utility font (utility.ttf) for its
help display/nav display.

The FreeType font system now supports all three fonts (proportional, monospace,
system) and bold, italic, bold/italic faces for all three, in addition to a
utility font for speeding up certain system GUI displays, a 'fallback font'
useful for large Han (CJK) fonts, and uses the FreeType portable cache manager
to speed rendering and allow loading much larger font files than previously. It
has been tested with 18 MB of fonts loaded with no errors.

There are fixes to network setup that *should* prevent unrecoverable network
dropout issue in previous builds. Generally, reconnecting from the Wifi Manager
midlet should now work. Test users please report if this is the case.

The Danzeff and semichordal boards support a 'smart display' feature, in which
they move out of the way of the active input area in large text boxes.

The Danzeff board display is slightly reduced in size from the previous build.

There are additional icons in the AMS, VM configuration and network setup
screens--the hope is to make thing a little easier on non-English speakers
until we can get some proper translation going here.

Virtual keyboards and Freetype setup are now configured in the GUI, not in the .ini.

 * The X/O keys are now fully switchable system-wide, for Western-style
accept/cancel use. Use the VM Configuration midlet -> Keymap settings menu
to set these keys. The settings take effect immediately after you exit the
configuration midlet.

Note that for midlets whose keymaps or device settings you've customized, you'll
have to set the accept key yourself in the Device Settings screen if you wish
them to change too (and if you haven't changed them already). Set 'Select' to
'Cross' and 'Num0' to 'Circle' to emulate the default Western layout, in this
case. Midlets whose device settings you have not customized will use the default
ones, and so will pick up the change without additional tweaking.

Note also that the old com.pspkvm.acceptcross setting for native dialogs is
gone. The setting in the VM config midlet will affect these dialogs, the AMS,
and the default keymap setting. Set it to 'Western' to use X=accept, to 'Eastern'
to use O=accept.

 * Keystroke handling in the Danzeff board has been tightened for faster
response.

 * The 'smart display' feature in the semichordal and Danzeff boards, wherein the
board displays move to stay out of your way when you're typing, has been optimized,
streamlined, and some bugs have been removed.

 * Live display in the semichordal board now places the small layout correctly.

 * The semichordal display has been greatly reduced in size, and its appearance
brought more into line with the Danzeff display. Status display for the live
keymap has also been added (Cyrillic/Greek/Roman).

 * The Danzeff board now has a status bar which displays selection state.

 * The diacritics (accents) deadkey glyphs for the semichordal display have been
modified to make them easier to read.

 * Bugfix: smart display-capable boards now move properly on all key entries.

 * Bugfix: the Danzeff board now displays correctly when the system is using
the internal  font.

 * Bugfix: thread shutdown for the Danzeff board's analog stick monitor has been
fixed; threads previously remained around when the board was absent. They now
start when the board appears, and exit when the board disappears.

 * Bugfix: prevented all virtual boards from crashing constrained fields
(numerical, URL, etc.) due to pasting in of text containing constrained
characters. Internal interfaces now sift input correctly.

 * Renamed utility.ttf to utility.sym so people stop assuming they can just 
delete it.

 * Bugfix: the Danzeff board should now work correctly with 'use internal font'
turned on. 

----------------
Note re building
----------------

You must link against a newer version of FreeType than is typically
available in pspsdk  toolchains to build this project. See the
tools/freetype_239_patch directory (README.TXT within) for a patch to add the
FreeType 2.3.9 distribution to your toolchain. 
	 
--------------------------------
General information
--------------------------------
PSPKVM is a PSP port of Sun's open-source JavaME implementation phoneMEFeature. The project description below is as it appears on the phoneME website:

The objective of the phoneME project is to further expand the usage of Java Platform, Micro Edition (Java ME platform) technology in the mobile handset market. The project scope includes a focus on the mainstream feature phone segment with phoneME Feature software, and the emerging advanced phone segment with phoneME Advanced software. Our goal in making these technologies available to the Mobile & Embedded Community is to reduce implementation variation, increase the rate of innovation and enable new devices to leverage the power of the Java ME platform.
	
The phoneME website is at: https://phoneme.dev.java.net/

The goal of the pspkvm project is to port the phoneMEFeature project to the Sony PSP game console. If you're seeking a phoneMEAdvanced port for the PSP, please see the project here: https://sourceforge.net/projects/pspme


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
Extract the .tgz to your PSP's /PSP/GAME/ or /PSP/GAME150 folder.

--------------------------------
Building instructions
--------------------------------

These are brief notes. Note that additional building instructions can be found in the
Wiki at:
	http://sourceforge.net/apps/trac/pspkvm/wiki/BuildingPSPKVM

For those interested in the source code:

1. You have to prepare the build environment for phoneME first. Please read the document at https://phoneme.dev.java.net/content/mr2/buildenv_feature.html#win_setup*

Now assuming you have installed the build environment by following the above instructions, in Cygwin:
2. Retrieve the source code via svn to a local directory**
3. cd ${your_source_dir}
4. export JDK_DIR=${your_jdk_dir} (example: export JDK_DIR=c:/j2sdk1.4.2_16)
5. ./build-psp-cldc.sh
Now you should see the success message following the building of the phoneME libs.
6. cd psp
7. Make the executable:***
- If building for the 1.50 kernel:
    make kxploit
    You should get pspkvm and pspkvm% directories in this directory, just copy them to your PSP's /PSP/GAME or /PSP/GAME150 directory.
- If building for 3.xx OE:
    make BUILD_SLIM=true
    This will build an EBOOT.PBP binary in this directory. Copy it to your PSP's /PSP/GAME/pspkvm directory.
8. Copy the "lib" and "appdb" directories from ${your_source_dir}/midp/build/javacall_psp/output/ into your PSP's /PSP/GAME/pspkvm directory
9. unzip midi_res.zip to your PSP's /PSP/GAME/pspkvm directory (If you need MIDI support)

* Note: you do not necessarily have to use the prepared Cygwin distribution suggested by these instructions, but this is probably the easiest approach if you do not already have a Cygwin distribution installed. If you wish to attempt to build the project with your current Cygwin installation, however, you will probably at least have to roll back your make binary to version 3.80 (assuming you have a more recent version), as make 3.81 and later do not handle DOS-style paths correctly in makefiles, and the phoneME build system requires a make that does.

** Note: you may have difficulty building the port with certain directory arrangements, due to Windows-based Java code failing properly to parse Posix-style Cygwin file paths. To avoid/correct this, place your pspkvm installation in a high or root directory, and mount it in Cygwin in such a fashion that the Posix and DOS paths wind up being the same thing. For example, place the pspkvm directory in the root of the C: drive (as in, in C:\pspkvm), then mount this directory on /pspkvm in Cygwin, with a command such as mount c:/pspkvm /pspkvm . Then run build-psp-cldc.sh from /pspkvm within Cywgin--the project root directory should default to /pspkvm, and building should proceed correctly.

*** Note: you will also need the PSP homebrew development toolchain and the pspsdk to build the binary for the PSP (step 7). As of this writing, both are available from http://ps2dev.org/psp/Tools/Toolchain/.

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
	
For additional help, see the Wiki at:

	http://sourceforge.net/apps/trac/pspkvm/wiki/WikiStart

Fonts included in this package

This test package ships with Deja Vu and Firefly Sung fonts installed for demonstration purposes. these may be replaced if you'd prefer other fonts, or wish to support languages these do not.

