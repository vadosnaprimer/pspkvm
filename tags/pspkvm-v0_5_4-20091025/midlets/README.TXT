README.TXT
----------

This is a directory of various test and helper midlets built for testing
and using the PSPKVM system, along with scripts and helper includes to
automate building midlets against the PSPKVM classes. The build system
just uses GNU Make and should work with any JDK you happen to have
around.

To create your Makefile, use top.inc as include. See the hello project as a
template.

The environment variables you'll need to set in your shell to make it work are:

 * $PSPKVM -- the build root of your pspkvm sources

	( eg. export PSPKVM=/pspkvm )
	
 * $JPATH -- the path to your JDK

	( eg. export JPATH=/jdk )

You can set both of these by customizing local.env in this directory
and catting it to your environment with the shell command:

	. local.env
	
... or by just appending the exports in local.env to your .bashrc (in your
home directory) or equivalent, so they get set whenever you start your shell.


LIMITATIONS
-----------

Note that the scripts aren't quite foolproof. This is a work in progress,
and I only put the thing together to speed things along for the relatively
simple midlets I was building for testing various VM features. Known
limitation: Compiled inner classes probably won't get preverified or jarred
up properly--you'll probably have to do this by hand if you go and add those
to midlets built under this system.

If you have issues with midlets built under this system, generally, the
problem is preverification. You can usually fix it just by running
preverify directly (as in, from the midlet directory, run ../preverify.sh
CLASSNAME) then running make again.
