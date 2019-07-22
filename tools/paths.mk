# External programs

INSTALLDIR := /usr/local/share/irptransmogrifier
BINLINK := /usr/local/bin/irptransmogrifier
BROWSELINK := /usr/local/bin/irpbrowse

BROWSE := xdg-open
JAVA := java
SAXON_JAR := /opt/saxon/saxon9he.jar
SAXON := $(JAVA) -jar $(SAXON_JAR)
XSLTPROC := xsltproc
