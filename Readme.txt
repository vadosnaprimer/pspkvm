pspME v0.2.1
8 Feb. 2008
Author: M@x
--------------------------------
Change log
--------------------------------
- Enable browse and select JAR from local file system, don't have to input the full path name of JAR file now.
- A little optimization of RMS speed.
- Separate MIDI config and instrument files from binary bundle to decrease the size, and make you not have to download them for every release.

--------------------------------
General information
--------------------------------
This software is a PSP porting of SUN open-source JavaME implementation: phoneME. The words below is copied from phoneME's website to describe the project:

	The objective of the phoneME project is to further expand the usage of Java? Platform, Micro Edition (Java ME platform) technology in the mobile handset market. The project scope includes a focus on the mainstream feature phone segment with phoneME Feature software, and the emerging advanced phone segment with phoneME Advanced software. Our goal in making these technologies available to the Mobile & Embedded Community is to reduce implementation variation, increase the rate of innovation and enable new devices to leverage the power of the Java ME platform.
	
You can also refer to the original website from here: https://phoneme.dev.java.net/

The goal of pspME is porting phoneME to Sony PSP game console. As the first step, my goal is a phoneMEFeature PSP implementation, which can enable user to run JavaME application on CLDC. In future, if time permit, I'll try to porting phoneMEAdvanced as well.

--------------------------------
Fetures
--------------------------------
- Java AMS with MVM supporting
- Several input methods: QWERTY/Abc/Symbol/...
- Install application from local JAR file
- Jpeg support
- MIDI support

--------------------------------
TODO
--------------------------------
- Play sample audio (High)
- User configurable key map (High)
- Networking (Medium)
- Graphic optimization (Medium)
- JSR184 (Low)
- MIPS JIT compiler and Interpreter generator (Low)

--------------------------------
Bugs and known issues
--------------------------------
- sceIoRename, which is used in javacall_file_rename(), doesn't work properly somehow
- javacall_file_truncate doesn't ported, so some file truncating operation, such as some RMS operations, may not work as expected

--------------------------------
Run from binary bundle
--------------------------------
1. Download pspME-bin-x.x.x.zip
2. Extract the zip to PSP's /PSP/GAME/ or /PSP/GAME150/
3. If you want MIDI support, download midi_res.zip and extract it to PSP's /PSP/GAME/PSPME or /PSP/GAME150/PSPME
4. Here we go!

--------------------------------
Building instructions
--------------------------------
0. You have to prepare the building enviroment for phoneME at first. Please refer the document from here: https://phoneme.dev.java.net/content/mr2/buildenv_feature.html#win_setup

Now assume you have installed the building environment by following the above instruction. In Cygwin:
1. unzip the source bundle to a directory
2. cd ${your_source_dir}
3. export JDK_DIR=${your_jdk_dir} (example: export JDK_DIR=c:/j2sdk1.4.2_16)
4. ./build-psp-cldc.sh
5. cd psp
Now you should see the success message of phoneME libs building.
6. make kxploit
You should get PSPME and PSPME% directories in this directory, just copy them to your PSP's /PSP/GAME or /PSP/GAME150 directory.
7. Copy "lib" and "appdb" directory from ${your_source_dir}/midp/build/javacall_psp/output/ into PSP's /PSP/GAME/PSPME or /PSP/GAME150/PSPME
8. unzip midi_res.zip to PSP's /PSP/GAME/PSPME or /PSP/GAME150/PSPME (If you need MIDI support)

Requirement:
    SDL_mixer
    libvorbis
    libogg
    
--------------------------------
Running tips
--------------------------------
- Install MIDlet from JAR file:
	1) Copy the JAR file to memory stick
	2) In AMS, select the first item "Install Application"
	3) Input the full path name of the JAR file. For example: ms0:/abc.jar, means to install MIDlet from abc.jar which is copied to root directory of memory stick
	4) Click Right Soft-button and select "2 Go to" from the popup menu, then follow the steps on screen
	
- Input text:
	You can't use Qwerty input method on PSP since it's lack of standard keyboard, but there's still other input methods available. In any text input sence, you can popup the menu by Right Soft-button, and change to another input method. I'd suggest to use "abc" to input alphabetics, just like you've done on your phone, input english characters by 0~9,#,* keypad.
	
- Key map:
	Currently, key map is hardcoded and you've to change source code to alter it. By default, the key assignment is blow:
	Left Trigger : Left Soft-button
	Right Trigger: Right Soft-button
	Up/Down/Left/Right: Up/Down/Left/Right
	Select: *
	Start : #
	Triangle: CLEAR
	Circle  : SELECT
	Analog Joy Stick: 	
		UP-LEFT    1
		UP         2
		UP-RIGHT   3
		LEFT       4
		RIGHT      6
		DOWN-LEFT  7
		DOWN       8
		DOWN-RIGHT 9
	CROSS   : 5
	SQUARE  : 0
	Left Trigger + Right Trigger: Multi-tasking key (Click to return AMS and put MIDlet to background)
	
- Multi tasking
	In and application, press Left Trigger + Right Trigger will put current running MIDlet to background, and AMS screen will be shown. The background running MIDlet is shown in high-lighted, and you can choose another MIDlet to run without stop the previous MIDlet. To bring a background running MIDlet to foreground, just click the MIDlet item from AMS is ok.