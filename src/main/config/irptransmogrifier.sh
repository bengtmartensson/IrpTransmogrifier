#!/bin/sh

JAVA=java

IRPHOME="$(dirname -- "$(readlink -f -- "${0}")" )"
JAR=${IRPHOME}/${project.name}-${project.version}-jar-with-dependencies.jar
CONFIG=${IRPHOME}/IrpProtocols.xml
STDIR=${IRPHOME}/st
export STDIR

exec "${JAVA}" -jar "${JAR}" -c "${CONFIG}" "$@"
