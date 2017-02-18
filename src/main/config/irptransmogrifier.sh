#!/bin/sh

JAVA=java
#DECODEIRDIR=/usr/local/lib64

IRPHOME="$(dirname -- "$(readlink -f -- "${0}")" )"
JAR=${IRPHOME}/${project.name}-${project.version}-jar-with-dependencies.jar
CONFIG=${IRPHOME}/IrpProtocols.xml
STDIR=${IRPHOME}/st
export STDIR

exec "${JAVA}" -jar "${JAR}" -c "${CONFIG}" "$@"
