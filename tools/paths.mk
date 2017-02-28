# External programs

JAVA := java
SAXON_JAR := /usr/local/saxon/saxon9he.jar

IRPHOME := $(abspath $(MYDIR)../target)
IRP_TRANSMOGRIFIER_JAR := $(IRPHOME)/IrpTransmogrifier-0.0.1dev-jar-with-dependencies.jar
IRPPROTOCOLS_XML := $(IRPHOME)/IrpProtocols.xml 
XSLT_DIR := $(IRPHOME)/xslt
#XSLT_DIR := /home/bengt/harctoolbox/IrpTransmogrifier/src/main/xslt
LIRC_TRANSFORM=$(XSLT_DIR)/lirc.xsl

IRPTRANSMOGRIFIER := $(JAVA) -jar $(IRP_TRANSMOGRIFIER_JAR) -c $(IRPPROTOCOLS_XML) --loglevel warning --url-decode
SAXON := $(JAVA) -jar $(SAXON_JAR)