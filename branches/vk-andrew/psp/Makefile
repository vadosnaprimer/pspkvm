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
LIBDIR = /usr/local/pspdev/psp/lib $(ROOT)/midp/build/javacall_psp/output/bin/mips $(ROOT)/cldc/build/javacall_psp_gcc/target/bin $(ROOT)/pcsl/output/javacall_mips/lib $(ROOT)/javacall/configuration/phoneMEFeature/psp_mips/output/lib
#LDFLAGS =  -L$(ROOT)/midp/build/javacall_psp/output/bin/mips -L$(ROOT)/cldc/build/javacall_psp_gcc/target/bin -L$(ROOT)/pcsl/output/javacall_mips/lib -L$(ROOT)/javacall/configuration/phoneMEFeature/psp_mips/output/lib
LIBS := -lSDL -lSDL_mixer -lpsphprm -lvorbis -lvorbisfile -logg -lpspaudiolib -lpspaudio -lpsppower -lpspgu -lpspusb -lpsprtc -lfreetype -lm -Wl,--end-group 
LIBS := -Wl,--start-group -lmidp$(g) -lcldc_vm$(cg) -lpcsl_file -lpcsl_print -lpcsl_memory -lpcsl_string -lpcsl_network -ljavacall$(g) $(LIBS) 

EXTRA_TARGETS = EBOOT.PBP
ifeq ($(BUILD_SLIM), true)
PSP_EBOOT_TITLE = pspkvm v0.5.2 OE
#PSP_EBOOT_ICON = java-duke-guitar.png
PSP_FW_VERSION = 352
BUILD_PRX=1
else
PSP_EBOOT_TITLE = pspkvm v0.5.2
#PSP_EBOOT_ICON = java-duke-guitar.png
PSP_FW_VERSION = 150
BUILD_PRX=0
endif

PSP_EBOOT_ICON=icon_kk_2.png

PSP_LARGE_MEMORY=1

PSPSDK=$(shell psp-config --pspsdk-path)
include build.mak
