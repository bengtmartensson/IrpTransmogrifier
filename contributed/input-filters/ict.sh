#! /bin/sh

JAVA=java
#IRSCUTINIZER_JAR=/usr/local/share/irscrutinizer/IrScrutinizer-jar-with-dependencies.jar
IRSCUTINIZER_JAR=/home/bengt/harctoolbox/harctoolboxbundle/target/IrScrutinizer-jar-with-dependencies.jar
MAINCLASS=org.harctoolbox.irscrutinizer.importer.IctImporter

exec "${JAVA}" -cp "${IRSCUTINIZER_JAR}" ${MAINCLASS} "$@"
