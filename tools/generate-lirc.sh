#!/bin/sh

JAVA=java
IRP_TRANSMOGRIFIER_JAR=target/IrpTransmogrifier-0.0.1dev-jar-with-dependencies.jar
SAXON_JAR=/usr/local/saxon/saxon9he.jar
XSLT_DIR=src/main/xslt
IRE_TRANSFORM=${XSLT_DIR}/intro-repeat-ending.xsl
LIRC_TRANSFORM=${XSLT_DIR}/lirc.xsl

#java -jar target/IrpTransmogrifier-0.0.1dev-jar-with-dependencies.jar -c target/IrpProtocols.xml -o  ../harctoolboxbundle/IrScrutinizer/src/main/config/exportformats.d/lirc.xml code -i --target lirc
${JAVA} -jar ${IRP_TRANSMOGRIFIER_JAR} -c target/IrpProtocols.xml     -o all-protocols.xml code --target xml
${JAVA} -jar ${SAXON_JAR} -s:all-protocols.xml -xsl:${IRE_TRANSFORM}  -o:intermediate.xml
${JAVA} -jar ${SAXON_JAR} -s:intermediate.xml  -xsl:${LIRC_TRANSFORM} -o:lirc.xml
