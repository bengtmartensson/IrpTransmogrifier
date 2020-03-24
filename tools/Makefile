# NOTE: This Makefile is not required to build the program, for which maven
# is used. Instead, it invokes the program for tests and for transforming the
# output, for example to the lirc.xml file.

MYDIR := $(dir $(firstword $(MAKEFILE_LIST)))
TOP := $(realpath $(MYDIR)..)

include $(MYDIR)/paths.mk

PROJECT_NAME := IrpTransmogrifier
PROJECT_NAME_LOWERCASE := $(shell echo $(PROJECT_NAME) | tr A-Z a-z)
EXTRACT_VERSION := $(TOP)/tools/extract_project_version.xsl
VERSION := $(shell $(XSLTPROC) $(EXTRACT_VERSION) pom.xml)
IRPHOME := $(TOP)/target
PROJECT_JAR := $(IRPHOME)/$(PROJECT_NAME)-$(VERSION)-jar-with-dependencies.jar
PROJECT_BIN := $(IRPHOME)/$(PROJECT_NAME)-$(VERSION)-bin.zip

IRPPROTOCOLS_XML := $(IRPHOME)/IrpProtocols.xml
#IRPTRANSMOGRIFIER := $(JAVA) -jar $(PROJECT_JAR) --loglevel warning --url-decode

IRPPROTOCOLS_XML := $(JAVA_PROTOCOL_TEST)/src/main/resources/IrpProtocols.xml
UPLOADDIR_DIR := ftp://bengt-martensson.de/harctoolbox/downloads/

GH_PAGES := $(TOP)/gh-pages
ORIGINURL := $(shell git remote get-url origin)
CHKSUMS := md5 sha1 sha512

SUMS := $(foreach f,$(CHKSUMS),$(PROJECT_BIN).$f)

default: $(PROJECT_JAR)

$(PROJECT_JAR) $(PROJECT_BIN) $(SUMS):
	mvn install -Dmaven.test.skip=true

$(PROJECT_JAR)-test:
	mvn install -Dmaven.test.skip=false

release: push gh-pages tag deploy

version:
	@echo $(VERSION)

setversion:
	mvn versions:set -DnewVersion=$(NEWVERSION)
	git commit -S -m "Set version to $(NEWVERSION)" pom.xml src/main/doc/$(PROJECT_NAME).releasenotes.txt

deploy:
	mvn deploy -P release
	@echo Possibly you need to update harctoolbox.org? At least IrpTransmogrifier.releasenotes.txt?

apidoc: target/site/apidocs
	$(BROWSE) $</index.html

javadoc: target/site/apidocs

target/site/apidocs:
	mvn javadoc:javadoc

push:
	git push

gh-pages: target/site/apidocs
	rm -rf $(GH_PAGES)
	git clone --depth 1 -b gh-pages ${ORIGINURL} ${GH_PAGES}
	( cd ${GH_PAGES} ; \
	cp -rf ../target/site/apidocs/* . ; \
	git add * ; \
	git commit -S -a -m "Update of API documentation" ; \
	git push )

$(INSTALLDIR):
	mkdir -p $@

tag:
	git checkout master
	git status
	git tag -s -a Version-$(VERSION) -m "Tagging Version-$(VERSION)"
	git push origin Version-$(VERSION)

upload-sums: $(SUMS)
	@(cd target ; \
	for file in $(foreach f,$(CHKSUMS),*.$f) ; do \
	echo Uploading $$file... ; \
		curl --netrc --upload-file $$file $(UPLOADDIR_DIR)/$$file;\
	done )

# Only for Unix-like systems
install: $(PROJECT_BIN) | $(INSTALLDIR)
	-rm -rf $(INSTALLDIR)/*
	( cd $(INSTALLDIR); jar xf $< )
	ln -sf $(INSTALLDIR)/$(PROJECT_NAME_LOWERCASE).sh $(BINLINK)
	echo \#!/bin/sh > $(BROWSELINK)
	echo $(BROWSE) $(INSTALLDIR)/IrpProtocols.html >> $(BROWSELINK)
	chmod +x $(BROWSELINK) $(INSTALLDIR)/$(PROJECT_NAME_LOWERCASE).sh

uninstall:
	rm -rf $(INSTALLDIR)
	rm $(BINLINK)

clean:
	mvn clean
	rm -f all-protocols.xml lirc.xml
	rm -rf $(GH_PAGES)

.PHONY: clean $(PROJECT_JAR)-test release
