README for Freetype 2.3.9 patchfiles
------------------------------------

AJ Milne, July 2009

This is a script and a small set of replacement files to make it possible to build the FreeType 2.3.9 source release for the pspdev toolchain (and thus linking to it in binaries built for the PSP) on Cygwin. To use it:

run ./pspify.sh from its current location (directly above psp_patches) with a single parameter: the top directory of an unpacked Freetype 2.3.9 distribution.

Thereafter, configure the build from its own root directory by running ./config.psp.sh from there.

Thereafter, run make to build the lib, and make install to install it into the correct location in the toolchain.

So, for example:

(Download the source tarball to this directory)
(cd to this directory--where the README is located)
tar -xvzf freetype-2.3.9.tar.gz
./pspify freetype-2.3.9
cd freetype-2.3.9
./config.psp.sh
make
make install

The script replaces the autofonf.sh script in the source tarball with a slightly customized version which doesn't check the version of libtool. Your libtoolize script should be 2.2.4 or higher--check this yourself by running libtoolize --version. Note that the *last* number reported is the correct version, in Cygwin. The other number is a Cygwin build number. This version string confuses the version checking in the autofonf.sh in the default package--this is why we replace it.

The new autoconf.sh script also copies a customized config.sub into the source's builds/unix directory, to provide definitions for building correctly for the PSP. It does this just *before* the configure step (and after autoconf), so autoconf doesn't overwrite it.

The config.psp.sh just forces a few compiler symbols before calling configure, so the Makefiles get set up with the correct LDFLAGS and CFLAGS configurations for building with psp-gcc and against the libs in the pspdev directories.

Note that though it hasn't been tested as of this writing, these scripts will probably also work on general *nix platforms, too, on which the psp toolchain is installed, including Linuxes.

