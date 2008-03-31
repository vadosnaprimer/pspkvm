pspkvm v0.3.0
31 Mar. 2008
Author: Sleeper, M@x
--------------------------------
Change log
--------------------------------
- Merge the source codes from 2 original project: pspkvm 0.1.0a and pspME 0.2.1 . Please see Features below for details.

--------------------------------
General information
--------------------------------
This software is a PSP porting of SUN open-source JavaME implementation: phoneMEFeature. The words below is copied from phoneME's website to describe the project:

	The objective of the phoneME project is to further expand the usage of Java? Platform, Micro Edition (Java ME platform) technology in the mobile handset market. The project scope includes a focus on the mainstream feature phone segment with phoneME Feature software, and the emerging advanced phone segment with phoneME Advanced software. Our goal in making these technologies available to the Mobile & Embedded Community is to reduce implementation variation, increase the rate of innovation and enable new devices to leverage the power of the Java ME platform.
	
You can also refer to the original website from here: https://phoneme.dev.java.net/

The goal of pspkvm is porting phoneMEFeature to Sony PSP game console. If you're seeking for phoneMEAdvanced porting on PSP, please take a look at the other project here: https://sourceforge.net/projects/pspme

--------------------------------
Fetures
--------------------------------
- Nokia UI APIs (partial)
- WMA1.1(JSR120) stub
- Networking (By PSP's WIFI)
- Java AMS with MVM supporting
- Several input methods: QWERTY/Abc/Symbol/...
- Directly browse and run from local jad/jar file, and auto-install without interrupting
- Jpeg support
- MIDI & Wave audio playback support
- Device emulation. You can choose device type to emulate for different screen sizes and key codes, either at installation time or from "Select device" menu
- Change default key assignment for specific application.

--------------------------------
TODO
--------------------------------
- Graphic optimization (High)
- Game loading time optimization (High)
- Resolve game compatibility regression (High)
- JSR179 implementation (Medium)
- JSR184 (Low)
- MIPS JIT compiler and Interpreter generator (Low)

--------------------------------
Bugs and known issues
--------------------------------
Known issues:
- javacall_file_truncate doesn't ported, so some file truncating operation, such as some RMS operations, may not work as expected
- Multimedia temporary files may leave on your memory stick sometimes, especially after VM crash. They're usually not big, but if you wanna delete the by hands, just goto your PSP's /PSP/GAME/pspkvm/ directory, find the file whose name likes xxxxxxxx_tmp_mus.mid, delete them.

Regrssions (comparing with pspkvm 0.1.0a):
- Game compatibility: Since we changed our code base to phoneME, some games MAY NOT run correctly on pspkvm 0.3.0, which ran well on pspkvm 0.1.0a. Although we tested many contents before this release, we still belive that there must be some app/games have regression. Any bug report of compatibility regressions is welcome, and important!
- CW90 is now supported now, but will come in near future.
- Some Chinese characters cannot be displayed correctly in file selection screen. But all kinds of memory sticks should work now.
- Loading speed: Actually it's a feature not regression :-p There's a speed/space trading inside of the new code. Some games decompress resources from jar when loading; To avoid too much memory consumption for large file decompressing, pspkvm 0.3.0 uses an fix-sized buffer to read and inflate files from jar, which slows down the speed of decompressing if the resource is larger than 32K. But pspkvm 0.1.0a always read-in completely regardless the resource size, and then decompress in memory, so some games loading speed is slower in pspkvm 0.3.0 than pspkvm 0.1.0a. But the good thing is it would never fail for lacking of memory when decompress large resources from jar.

--------------------------------
Run from binary bundle
--------------------------------
1. Download correct binary bundle:
- If you have a 1.50 kernel PSP:
   Download pspkvm-bin-x.x.x-150.zip
- If you want to run pspkvm on 3.xx OE:
   Download pspkvm-bin-x.x.x-slim.zip
2. Extract the zip to PSP's /PSP/GAME/
3. If you want MIDI support, download midi_res.zip and extract it to PSP's /PSP/GAME/pspkvm
4. Here we go!
NOTE: There's also an all-in-one bundle avaiable on download page, which contains MIDP support files. You don't have to download separated midi_res.zip if you choose "all-in-one".

--------------------------------
Building instructions
--------------------------------
For the ones who are interested in our source code:
0. You have to prepare the building enviroment for phoneME at first. Please refer the document from here: https://phoneme.dev.java.net/content/mr2/buildenv_feature.html#win_setup

Now assume you have installed the building environment by following the above instruction. In Cygwin:
1. unzip the source bundle to a directory
2. cd ${your_source_dir}
3. export JDK_DIR=${your_jdk_dir} (example: export JDK_DIR=c:/j2sdk1.4.2_16)
4. ./build-psp-cldc.sh
5. cd psp
Now you should see the success message of phoneME libs building.
6. Make the executable:
- If build on 1.50 kernel:
    make kxploit
    You should get pspkvm and pspkvm% directories in this directory, just copy them to your PSP's /PSP/GAME or /PSP/GAME150 directory.
- If build on 3.xx OE:
    make BUILD_SLIM=true
    You should get EBOOT.PBP in this directory, copy it to your PSP's /PSP/GAME/pspkvm directory
7. Copy "lib" and "appdb" directory from ${your_source_dir}/midp/build/javacall_psp/output/ into PSP's /PSP/GAME/pspkvm
8. unzip midi_res.zip to PSP's /PSP/GAME/pspkvm (If you need MIDI support)

Requirement:
    SDL_mixer
    libvorbis
    libogg
    
--------------------------------
Running tips
--------------------------------
- Run MIDlet from memory stick:
	1) Select "Find Application" item in AMS (the item on the top)
	2) Select "Install from memory stick (ms0:/)"
	3) Browse the file system of your memory stick now, and select jad or jar to run
	4) After selected, the jad/jar will be automatically installed and run. You can choose the installed application from AMS next time you want to run.
	
- Input text:
	You can't use Qwerty input method on PSP since it's lack of standard keyboard, but there's still other input methods available. In any text input sence, you can popup the menu by Right Soft-button, and change to another input method. I'd suggest to use "abc" to input alphabetics, just like you've done on your phone, input english characters by 0~9,#,* keypad.
	
- Key map:
	By default, the key assignment is blow:
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
	
	Left Trigger + Right Trigger + Triangle: Multi-tasking key (Click to return AMS and put MIDlet to background)
	Left Trigger + Right Trigger + Cross:    Exit current running MIDlet
	
- Multi tasking
	In and application, press Left Trigger+Right Trigger+Triangle will put current running MIDlet to background, and AMS screen will be shown. The background running MIDlet is shown in high-lighted, and you can choose another MIDlet to run without stop the previous MIDlet. To bring a background running MIDlet to foreground, just click the MIDlet item from AMS is ok.
	Note that currently we only enable run 2 MIDlet to run at same time, will allow more in future.
	
- Connect to network
	Before a Java MIDlet wants to use network, you need to connect your PSP to network at first. To setup this connection, please select "Network Setup" in AMS(the 2nd item in AMS MIDlets list), then all the wifi connections you've setup in PSP will be listed. Select one and until an alert popup to tell you if it's succeeded. Once it says "Successfully connected by profile xxx", that means your network is setup, and back to AMS and run you MIDlet which uses network.
	*Tips for 3.xx OE users: you don't have to setup network in "Network Setup" menu. When the first your MIDlet want to access wifi network, a PSP network setup dialog will popup and prompt you connecting. But it only appears at the fisrt the Java VM want to access network, if you disconnected network after setup, you'll have to re-setup network manually by "Network Setup" menu.
	
- Select device to emulate
	When user select a jad/jar file from memory stick to run, a "Device Select" menu will appear. Choose you preffered device size and model here, and press CIRCLE key to decide. If want to change the preffered device setting, go back to AMS and move the highlight onto the MIDlet whose setting you want to change, then select "Select Device" from menu.
	
- Change default key assignment for specified game
	Default key assignment may not fit your game, you can go to AMS and move the highlight onto the MIDlet whose key assignment you want to change, then select "Select Device" from menu. Change the key setting as what you want. REMEBER:"Shift" means Left Trigger or Right Tirgger.