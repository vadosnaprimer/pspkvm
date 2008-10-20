/**
 *  This file handles all the PSP-specific kernel setup and exit stuff.
 *
 *  Is there some general interest for this file, so that we can place it
 *  somewhere in the compiler toolchain include path? 
 *
 *  Usage: Simply add 
 *            -DMODULE_NAME="your-module-name" psp-setup.c
 *         to the LFLAGS or LDFLAGS of your project, so that this file is
 *         compiled in when gcc collects and links the final ELF binary.
 *
 *  Options:
 *         -DMODULE_NAME="name" -- set the name (default NONAME)
 *         -DMODULE_ATTR=0      -- module attributes (default 0)
 *         -DVERSION_MAJOR=1    -- version 1.x (default 1)
 *         -DVERSION_MINOR=0    -- version x.0 (default 0)
 *
 *  Note:  The linker flags and library lists need to be placed after this
 *         entry on the LFLAG or LDFLAGS command line, otherwise gcc won't
 *         be able to to resolve all symbols.
 */

#include <pspkerneltypes.h>
#include <pspuser.h>

#if !defined(MODULE_NAME)
	#define MODULE_NAME NONAME
#endif


#if !defined(MODULE_VERSION_MAJOR)
	#define MODULE_VERSION_MAJOR 1
#endif


#if !defined(MODULE_VERSION_MINOR)
	#define MODULE_VERSION_MINOR 0
#endif


#if !defined(MODULE_ATTR)
	#define MODULE_ATTR 0
#endif


#define __stringify(s)	__tostring(s)
#define __tostring(s)	#s

PSP_MODULE_INFO(__stringify(MODULE_NAME), MODULE_ATTR, MODULE_VERSION_MAJOR, MODULE_VERSION_MINOR);


static
int exit_callback (int arg1, int arg2, void *common)
{
	sceKernelExitGame();
	return 0;
}


static
int update_thread (SceSize args, void *argp)
{
	int cbid = sceKernelCreateCallback("Exit Callback", exit_callback, NULL);
	sceKernelRegisterExitCallback(cbid);
	sceKernelSleepThreadCB();
	return 0;
}


static void setup_callbacks (void) __attribute__((constructor));
static void setup_callbacks (void)
{
	int id;

	if ((id = sceKernelCreateThread("update_thread", update_thread, 0x11, 0xFA0, 0, 0)) >= 0)
		sceKernelStartThread(id, 0, 0);
}



static void back_to_kernel (void) __attribute__((destructor));
static void back_to_kernel (void)
{
	sceKernelExitGame();
}

