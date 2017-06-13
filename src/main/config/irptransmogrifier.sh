#!/bin/sh

# Wrapper for IrpTransmogrifier

# The command line name to use to invoke java; change if desired.
JAVA=java

IRPHOME="$(dirname -- "$(readlink -f -- "${0}")" )"
JAR=${IRPHOME}/${project.name}-${project.version}-jar-with-dependencies.jar

# STDIR is used to find st files for code generation
STDIR=${IRPHOME}/st
export STDIR

exec "${JAVA}" -jar "${JAR}" "$@"
