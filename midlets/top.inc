# General Makefile defines for building midlets

MIDLETROOT=$(PSPKVM)/midlets

# Machine specific
MTD=$(MIDLETROOT)
MIDPOUT=$(PSPKVM)/midp/build/javacall_psp/output

# Generic derived paths
JCPATH=$(MIDPOUT)/classes
JAVAC=$(JPATH)/bin/javac
JC=$(JAVAC) -bootclasspath $(JCPATH) -classpath .
JPREVERIFY=$(MIDPOUT)/bin/mips/preverify
JV=$(JPREVERIFY) -classpath $(JCPATH) -d .
JARTOOL=$(JPATH)/bin/jar
MKJAR=$(MTD)/jar.sh
MKJAD=$(MTD)/mkjad.sh

# Generic rules
%.class : %.java
	$(JC) $<; $(JV) `basename $< .java`

# TODO/FIXME: Handle nested classes; needs foreach on JV, JAR

# Top-level rules--depend on project-specific defines
MANIFEST=$(PRBNAME).jad
JARFILE=$(PRBNAME).jar

# Final jar
$(JARFILE) : $(MANIFEST) $(OBJS)
	$(MKJAR) $(JARFILE) $(MANIFEST) $(OBJS) $(JARDIRS) 

# Manifest
$(MANIFEST) :
	$(MKJAD) "$(PRNAME)" $(ECLASS) "$(DESC)" "$(MIDLET_VERSION)" "$(MIDLET_AUTHOR)" "$(MIDLET_URL)" > $(MANIFEST)

.phony : all clean

clean :
	rm -f $(MANIFEST)
	rm -f $(OBJS)
	rm -f $(JARFILE)
	
all : $(JARFILE)
