#!/bin/sh

# Wrapper for IrpTransmogrifier

JAVA=java

IRPHOME="$(dirname -- "$(readlink -f -- "${0}")" )"
JAR=${IRPHOME}/${project.name}-${project.version}-jar-with-dependencies.jar
STDIR=${IRPHOME}/st
export STDIR

exec "${JAVA}" -jar "${JAR}" "$@"
