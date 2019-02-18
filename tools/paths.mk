# External programs

VERSION := 0.2.1dev
INSTALLDIR := /usr/local/share/irptransmogrifier
BINLINK := /usr/local/bin/irptransmogrifier
BROWSELINK := /usr/local/bin/irpbrowse

BROWSE := xdg-open	
JAVA := java
SAXON_JAR := /opt/saxon/saxon9he.jar

IRPHOME := $(abspath $(MYDIR)../target)
IRPPROTOCOLS_XML := $(IRPHOME)/IrpProtocols.xml 
XSLT_DIR := $(IRPHOME)/xslt
LIRC_TRANSFORM=$(XSLT_DIR)/lirc.xsl

IRPTRANSMOGRIFIER = $(JAVA) -jar $(IRP_TRANSMOGRIFIER_JAR) --loglevel warning --url-decode
SAXON := $(JAVA) -jar $(SAXON_JAR)
