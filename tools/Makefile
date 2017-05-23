# NOTE: This Makefile is not required to build the program, for which maven
# is used. Instead, it invokes the program for tests and for transforming the
# output, for example to the lirc.xml file.

MYDIR := $(dir $(firstword $(MAKEFILE_LIST)))

include $(MYDIR)/paths.mk
include $(MYDIR)/java-test-renderer-protocols.mk
include $(MYDIR)/java-test-decoder-protocols.mk

IRP_TRANSMOGRIFIER_JAR := $(IRPHOME)/IrpTransmogrifier-$(VERSION)-jar-with-dependencies.jar

JAVA_PROTOCOL_TEST := JavaTestProtocol

JAVA_RENDERER_CODEDIR := $(JAVA_PROTOCOL_TEST)/src/main/java/org/harctoolbox/renderers
JAVA_RENDERER_TESTDIR := $(JAVA_PROTOCOL_TEST)/src/test/java/org/harctoolbox/renderers
JAVA_DECODER_CODEDIR := $(JAVA_PROTOCOL_TEST)/src/main/java/org/harctoolbox/decoders
JAVA_DECODER_TESTDIR := $(JAVA_PROTOCOL_TEST)/src/test/java/org/harctoolbox/decoders

IRPPROTOCOLS_XML := $(JAVA_PROTOCOL_TEST)/src/main/resources/IrpProtocols.xml

default: $(IRP_TRANSMOGRIFIER_JAR)
	$(IRPTRANSMOGRIFIER) --help

$(IRP_TRANSMOGRIFIER_JAR):
	mvn install -Dmaven.test.skip=true

$(IRP_TRANSMOGRIFIER_JAR)-test:
	mvn install -Dmaven.test.skip=false

apidoc:
	mvn javadoc:javadoc
	$(BROWSE) target/site/apidocs/index.html

all-protocols.xml: $(IRP_TRANSMOGRIFIER_JAR)
	$(IRPTRANSMOGRIFIER) -o $@ code --target xml

lirc.xml: all-protocols.xml ${LIRC_TRANSFORM}
	$(SAXON) -s:$< -xsl:${LIRC_TRANSFORM} -o:$@

javacodetest: javarendertest javadecodertest $(JAVA_PROTOCOL_TEST)/pom.xml $(IRPPROTOCOLS_XML)
	(cd $(JAVA_PROTOCOL_TEST); mvn test)

javarendertest:  javarendercodefiles  javarendertestfiles 
javadecodertest: javadecodercodefiles javadecodertestfiles

javarendercodefiles: $(IRP_TRANSMOGRIFIER_JAR) | $(JAVA_RENDERER_CODEDIR)
	$(IRPTRANSMOGRIFIER) code --directory $(JAVA_RENDERER_CODEDIR) --stdir $(IRPHOME)/st --target java-renderer $(foreach proto,$(JAVA_TEST_RENDERER_PROTOCOLS),"$(proto)")

javarendertestfiles: $(IRP_TRANSMOGRIFIER_JAR) | $(JAVA_RENDERER_TESTDIR)
	$(IRPTRANSMOGRIFIER) code --directory $(JAVA_RENDERER_TESTDIR) --stdir $(IRPHOME)/st --target java-renderer-test $(foreach proto,$(JAVA_TEST_RENDERER_PROTOCOLS), "$(proto)")

javadecodercodefiles: $(IRP_TRANSMOGRIFIER_JAR) | $(JAVA_DECODER_CODEDIR)
	$(IRPTRANSMOGRIFIER) code --directory $(JAVA_DECODER_CODEDIR)  --stdir $(IRPHOME)/st --target java-decoder $(foreach proto,$(JAVA_TEST_DECODER_PROTOCOLS), "$(proto)")

javadecodertestfiles: $(IRP_TRANSMOGRIFIER_JAR) | $(JAVA_DECODER_TESTDIR)
	$(IRPTRANSMOGRIFIER) code --directory $(JAVA_DECODER_TESTDIR)  --stdir $(IRPHOME)/st --target java-decoder-test $(foreach proto,$(JAVA_TEST_DECODER_PROTOCOLS), "$(proto)")

$(JAVA_PROTOCOL_TEST)/pom.xml: $(JAVA_PROTOCOL_TEST)
	cp $(MYDIR)/JavaIrpProtocolTest.pom.xml $@

$(IRPPROTOCOLS_XML): | $(JAVA_PROTOCOL_TEST)/src/main/resources
	cp src/main/resources/IrpProtocols.xml $@

$(JAVA_PROTOCOL_TEST) \
$(JAVA_PROTOCOL_TEST)/src/main/resources \
$(JAVA_RENDERER_CODEDIR) $(JAVA_RENDERER_TESTDIR) $(JAVA_DECODER_CODEDIR) $(JAVA_DECODER_TESTDIR):
	mkdir -p $@

# Only for Unix-like systems	
install: $(IRP_TRANSMOGRIFIER_JAR)
	-mkdir -p $(INSTALLDIR)
	-rm -rf $(INSTALLDIR)
	-mkdir $(INSTALLDIR)
	cp target/IrpTransmogrifier-$(VERSION)-bin.zip $(INSTALLDIR)
	( cd $(INSTALLDIR); jar xf IrpTransmogrifier-$(VERSION)-bin.zip; rm IrpTransmogrifier-$(VERSION)-bin.zip )
	cp tools/Makefile tools/paths.mk $(INSTALLDIR)
	ln -sf $(INSTALLDIR)/irptransmogrifier.sh $(BINLINK)
	echo \#!/bin/sh > $(BROWSELINK)
	echo $(BROWSE) $(INSTALLDIR)/IrpProtocols.html >> $(BROWSELINK)
	chmod +x $(BROWSELINK) $(INSTALLDIR)/irptransmogrifier.sh

uninstall:
	rm -rf $(INSTALLDIR)
	rm $(BINLINK)

test: $(IRP_TRANSMOGRIFIER_JAR)-test javacodetest lirc.xml
	diff lirc.xml lirc.xml.old

clean:
	mvn clean
	rm -f all-protocols.xml lirc.xml
	rm -rf $(JAVA_PROTOCOL_TEST)

.PHONY: clean $(IRP_TRANSMOGRIFIER_JAR)-test
