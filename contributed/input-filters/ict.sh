#! /bin/sh

JAVA=java
HERE="$(dirname $(dirname -- "$(readlink -f -- "${0}")" ) )"
IRSCUTINIZER_JAR="${HERE}/../../IrScrutinizer/target/IrScrutinizer-jar-with-dependencies.jar"
MAINCLASS=org.harctoolbox.irscrutinizer.importer.IctImporter

exec "${JAVA}" -cp "${IRSCUTINIZER_JAR}" ${MAINCLASS} "$@"
