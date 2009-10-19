README.TXT

This is a directory of scripts and helper includes to automate building
midlets against the PSPKVM classes. The build system just uses GNU Make
and should work with any JDK you happen to have around.

To create your Makefile, use top.inc as include. See the hello project as a
template.

The environment variables you'll need to set in your shell to make it work are:

 * $PSPKVM -- the build root of your pspkvm sources

	( eg. export PSPKVM=/pspkvm )
	
 * $JPATH -- the path to your JDK

  ( eg. export JPATH=/jdk )

You can set both of these by customizing local.env in this directory and catting it to your environment with the shell command:

	. local.env
	
... or by just appending the exports in local.env to your .bashrc (in your
home directory) or equivalent, so they get set whenever you start your shell.
