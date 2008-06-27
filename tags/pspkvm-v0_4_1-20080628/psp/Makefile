ifeq ($(ROOT),)
ROOT = ..
endif

TARGET = pspkvm
OBJS = pspkvm.o vram.o sceUsbGps.o

INCDIR = 
CFLAGS = -G0 -Wall -fno-strict-aliasing -I$(ROOT)/javacall/configuration/phoneMEFeature/psp_mips/output/inc
CXXFLAGS = $(CFLAGS) -fno-exceptions -fno-rtti
ASFLAGS = $(CFLAGS)

ifeq ($(USE_DEBUG), true)
g = _g
cg = _g
CFLAGS += -g
else
ifeq ($(USE_RELEASE), true)
g = _g
cg = _r
CFLAGS += -g
else
g =
cg =
CFLAGS += -O3
endif
endif
LIBDIR =
LDFLAGS =  -L$(ROOT)/midp/build/javacall_psp/output/bin/mips -L$(ROOT)/cldc/build/javacall_psp_gcc/target/bin -L$(ROOT)/pcsl/output/javacall_mips/lib -L$(ROOT)/javacall/configuration/phoneMEFeature/psp_mips/output/lib
LIBS := -lSDL -lSDL_mixer -lpsphprm -lvorbis -lvorbisfile -logg -lpspaudiolib -lpspaudio -lpspgu -lpspusb -lpsprtc -lm
LIBS := -Wl,--start-group -lmidp$(g) -lcldc_vm$(cg) --end-group  -lpcsl_file -lpcsl_print -lpcsl_memory -lpcsl_string -lpcsl_network -ljavacall$(g) $(LIBS) 

EXTRA_TARGETS = EBOOT.PBP
ifeq ($(BUILD_SLIM), true)
PSP_EBOOT_TITLE = pspkvm v0.4.1 OE
#PSP_EBOOT_ICON = java-duke-guitar.png
PSP_FW_VERSION = 352
BUILD_PRX=1
else
PSP_EBOOT_TITLE = pspkvm v0.4.1
#PSP_EBOOT_ICON = java-duke-guitar.png
PSP_FW_VERSION = 150
BUILD_PRX=0
endif

PSP_EBOOT_ICON=icon_kk_2.png

PSPSDK=$(shell psp-config --pspsdk-path)
include build.mak
