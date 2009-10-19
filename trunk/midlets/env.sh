# Include to set up useful variables for building midlets

MTD=$MIDLETROOT
MIDPOUT=$PSPKVM/midp/build/javacall_psp/output
JCPATH=$MIDPOUT/classes
JAVAC=$JPATH/bin/javac
JC="$JAVAC -bootclasspath $JCPATH -classpath $JCPATH"
JPREVERIFY=$MIDPOUT/bin/mips/preverify
JV="$JPREVERIFY -classpath $JCPATH -d ."
JARTOOL=$JPATH/bin/jar
