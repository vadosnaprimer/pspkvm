pspkvm v0.3.2
27 Apr. 2008
Author: Sleepper, M@x
Email: pspkvm@gmail.com
Project website: 
	http://www.pspkvm.com 
	http://www.pspkvm.cn
Project home on SF:
	https://sourceforge.net/projects/pspkvm
	 
	 
--------------------------------
Change log
--------------------------------
- Virtual Keyboard support
- Use "L+R+Square" to call up network connection dialog at any time (slim version only)
- Add pspkvm.ini confituation file
- JSR75 root directories configurable by pspkvm.ini
- Strech small device screen to fit PSP screen
- Support CW90
- Resolve bug: can not enter jad/jar file selector form, if the original jar file direcory is deleted
- Implement microedition.platform property. Now can run Cangshenlu.
- Don't show mess icon in AMS
- Resolve the bug that can't output voice in jstardict

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
- JSR75(File Connection)
- Virtual Keyboard Input

--------------------------------
TODO
--------------------------------
- Graphic optimization (High)
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
- Fileconnection rmdir operation may fail for unknown reasons

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
	*Tips for 3.xx OE users: you don't have to setup network in "Network Setup" menu. When the first your MIDlet want to access wifi network, a PSP network setup dialog will popup and prompt you connecting. And also, you can press "L+R+Square" at any time to call up network setup dialog.
	
- Select device to emulate
	When user select a jad/jar file from memory stick to run, a "Device Select" menu will appear. Choose you preffered device size and model here, and press CIRCLE key to decide. If want to change the preffered device setting, go back to AMS and move the highlight onto the MIDlet whose setting you want to change, then select "Select Device" from menu.
	
- Change default key assignment for specified game
	Default key assignment may not fit your game, you can go to AMS and move the highlight onto the MIDlet whose key assignment you want to change, then select "Select Device" from menu. Change the key setting as what you want. REMEBER:"Shift" means Left Trigger or Right Tirgger.
	
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
	However, you can change the default mapping in pspkvm.ini. Please open pspkvm.ini in PSPKVM's install directory, change the values in [jsr75] section. If the value is left blank, default value is applied.