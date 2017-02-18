#!/bin/sh

JAVA=java
IRP_TRANSMOGRIFIER_JAR=target/IrpTransmogrifier-0.0.1dev-jar-with-dependencies.jar
SAXON_JAR=/usr/local/saxon/saxon9he.jar
XSLT_DIR=src/main/xslt
LIRC_TRANSFORM=${XSLT_DIR}/lirc.xsl

${JAVA} -jar ${IRP_TRANSMOGRIFIER_JAR} -c target/IrpProtocols.xml     -o all-protocols.xml code --target xml
${JAVA} -jar ${SAXON_JAR} -s:all-protocols.xml  -xsl:${LIRC_TRANSFORM} -o:lirc.xml
