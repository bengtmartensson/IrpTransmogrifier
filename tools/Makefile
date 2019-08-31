# NOTE: This Makefile is not required to build the program, for which maven
# is used. Instead, it invokes the program for tests and for transforming the
# output, for example to the lirc.xml file.

MYDIR := $(dir $(firstword $(MAKEFILE_LIST)))
TOP := $(realpath $(MYDIR)..)

include $(MYDIR)/paths.mk

EXTRACT_VERSION := $(TOP)/tools/extract_project_version.xsl
VERSION := $(shell $(XSLTPROC) $(EXTRACT_VERSION) pom.xml)
IRPHOME := $(TOP)/target
IRPPROTOCOLS_XML := $(IRPHOME)/IrpProtocols.xml
XSLT_DIR := $(IRPHOME)/xslt
LIRC_TRANSFORM=$(XSLT_DIR)/lirc.xsl
IRP_TRANSMOGRIFIER_JAR := $(IRPHOME)/IrpTransmogrifier-$(VERSION)-jar-with-dependencies.jar
IRP_TRANSMOGRIFIER_BIN := $(IRPHOME)/IrpTransmogrifier-$(VERSION)-bin.zip
IRPTRANSMOGRIFIER := $(JAVA) -jar $(IRP_TRANSMOGRIFIER_JAR) --loglevel warning --url-decode

IRPPROTOCOLS_XML := $(JAVA_PROTOCOL_TEST)/src/main/resources/IrpProtocols.xml

GH_PAGES := $(TOP)/gh-pages
ORIGINURL := $(shell git remote get-url origin)

default: $(IRP_TRANSMOGRIFIER_JAR)
	$(IRPTRANSMOGRIFIER) --help

$(IRP_TRANSMOGRIFIER_JAR) $(IRP_TRANSMOGRIFIER_BIN):
	mvn install -Dmaven.test.skip=true

$(IRP_TRANSMOGRIFIER_JAR)-test:
	mvn install -Dmaven.test.skip=false

apidoc: target/site/apidocs
	$(BROWSE) target/site/apidocs/index.html

javadoc: target/site/apidocs

target/site/apidocs:
	mvn javadoc:javadoc

gh-pages: target/site/apidocs
	rm -rf $(GH_PAGES)
	git clone --depth 1 -b gh-pages ${ORIGINURL} ${GH_PAGES}
	( cd ${GH_PAGES} ; \
	cp -rf ../target/site/apidocs/* . ; \
	git add * ; \
	git commit -a -m "Update of API documentation" ; \
	git push )

$(INSTALLDIR):
	mkdir -p $@

tag:
	git checkout master
	git status
	git tag -a Version-$(VERSION) -m "Tagging Version-$(VERSION)"
	git push origin Version-$(VERSION)

# Only for Unix-like systems
install: $(IRP_TRANSMOGRIFIER_BIN) | $(INSTALLDIR)
	-rm -rf $(INSTALLDIR)/*
	( cd $(INSTALLDIR); jar xf $< )
	ln -sf $(INSTALLDIR)/irptransmogrifier.sh $(BINLINK)
	echo \#!/bin/sh > $(BROWSELINK)
	echo $(BROWSE) $(INSTALLDIR)/IrpProtocols.html >> $(BROWSELINK)
	chmod +x $(BROWSELINK) $(INSTALLDIR)/irptransmogrifier.sh

uninstall:
	rm -rf $(INSTALLDIR)
	rm $(BINLINK)

clean:
	mvn clean
	rm -f all-protocols.xml lirc.xml
	rm -rf $(JAVA_PROTOCOL_TEST)
	rm -rf $(GH_PAGES)

.PHONY: clean $(IRP_TRANSMOGRIFIER_JAR)-test
