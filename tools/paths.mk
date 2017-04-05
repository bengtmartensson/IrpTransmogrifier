# External programs

VERSION := 0.0.1dev
INSTALLDIR := /usr/local/share/irptransmogrifier
BINLINK := /usr/local/bin/irptransmogrifier	

BROWSE := xdg-open	
JAVA := java
SAXON_JAR := /usr/local/saxon/saxon9he.jar

IRPHOME := $(abspath $(MYDIR)../target)
IRPPROTOCOLS_XML := $(IRPHOME)/IrpProtocols.xml 
XSLT_DIR := $(IRPHOME)/xslt
LIRC_TRANSFORM=$(XSLT_DIR)/lirc.xsl

IRPTRANSMOGRIFIER = $(JAVA) -jar $(IRP_TRANSMOGRIFIER_JAR) -c $(IRPPROTOCOLS_XML) --loglevel warning --url-decode
SAXON := $(JAVA) -jar $(SAXON_JAR)
