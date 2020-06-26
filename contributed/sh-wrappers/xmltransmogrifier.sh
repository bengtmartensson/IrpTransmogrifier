#!/bin/sh

HERE="$(dirname -- "$(readlink -f -- "${0}")" )"

# Adjust to your liking
JAVA=java
JAR=${HERE}/../../target/IrpTransmogrifier-*-jar-with-dependencies.jar


${JAVA} -cp ${JAR} org.harctoolbox.xml.XmlTransmogrifier "$@"
