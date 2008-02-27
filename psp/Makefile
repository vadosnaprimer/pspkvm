ifeq ($(ROOT),)
ROOT = ..
endif

TARGET = pspkvm
OBJS = pspkvm.o

INCDIR = 
CFLAGS = -G0 -Wall -I$(ROOT)/javacall/configuration/phoneMEFeature/psp_mips/output/inc
CXXFLAGS = $(CFLAGS) -fno-exceptions -fno-rtti
ASFLAGS = $(CFLAGS)

ifeq ($(USE_DEBUG), true)
g = _g
CFLAGS += -g
else
g =
CFLAGS += -O2
endif
LIBDIR =
LDFLAGS =  -L$(ROOT)/midp/build/javacall_psp/output/bin/mips -L$(ROOT)/cldc/build/javacall_psp_gcc/target/bin -L$(ROOT)/pcsl/output/javacall_mips/lib -L$(ROOT)/javacall/configuration/phoneMEFeature/psp_mips/output/lib
LIBS := -lSDL -lSDL_mixer -lpsphprm -lvorbis -lvorbisfile -logg -lpspaudiolib -lpspaudio -lpspgu -l psprtc -lm
LIBS := -Wl,--start-group -lmidp$(g) -lcldc_vm$(g) --end-group  -lpcsl_file -lpcsl_print -lpcsl_memory -lpcsl_string -lpcsl_network -ljavacall$(g) $(LIBS) 

EXTRA_TARGETS = EBOOT.PBP
PSP_EBOOT_TITLE = pspkvm v0.3.0
#PSP_EBOOT_ICON = java-duke-guitar.png

PSPSDK=$(shell psp-config --pspsdk-path)
include build.mak
