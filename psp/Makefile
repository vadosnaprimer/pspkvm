ifeq ($(ROOT),)
ROOT = ..
endif

TARGET = pspkvm
OBJS = pspkvm.o vram.o sceUsbGps.o pspkvm.map

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
LIBS := -lpspaudiocodec -lpspmpeg -lSDL -lSDL_mixer -lvorbis -lvorbisfile -logg -lfreetype -lm -lpsphprm -lpspaudiolib -lpspaudio -lpspwlan -lpspgu -lpspusb -lpsprtc -Wl,--end-group 
LIBS := -Wl,--start-group -lmidp$(g) -lcldc_vm$(cg) -lstdc++ -lpcsl_file -lpcsl_print -lpcsl_memory -lpcsl_string -lpcsl_network -ljavacall$(g) $(LIBS)

EXTRA_TARGETS = EBOOT.PBP 
ifeq ($(BUILD_SLIM), true)
PSP_EBOOT_TITLE = PSPKVM v0.5.5 Test 4 CFW
PSP_FW_VERSION = 352
BUILD_PRX=1
else
PSP_EBOOT_TITLE = PSPKVM v0.5.5 Test 4 OFW
PSP_FW_VERSION = 150
BUILD_PRX=0
endif

PSP_EBOOT_ICON=icon_kk_2.png

PSP_LARGE_MEMORY=1

PSPSDK=$(shell psp-config --pspsdk-path)
include build.mak

# Hack to make sure the map gets generated so build doesn't get hung up
pspkvm.map :
	touch pspkvm.map

.phony all : EBOOT.PBP
