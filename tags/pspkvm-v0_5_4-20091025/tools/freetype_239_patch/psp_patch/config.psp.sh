#!/bin/sh

   sh autogen.sh
   LDFLAGS="-L$(psp-config --pspsdk-path)/lib -lc -lpspuser" \
   CFLAGS="-G0" \
      ./configure --host psp --prefix=$(psp-config --psp-prefix)

