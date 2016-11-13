#!/bin/sh

JAVA=java
DECODEIRDIR=/usr/local/lib64

IRPHOME="$(dirname -- "$(readlink -f -- "${0}")" )"
JAR=${IRPHOME}/${project.name}-${project.version}-jar-with-dependencies.jar
CONFIG=${IRPHOME}/IrpProtocols.xml

"${JAVA}" -Djava.library.path="${DECODEIRDIR}" -jar "${JAR}" -c "${CONFIG}" \
    code -s --target java \
    --directory ${IRPHOME}/../../JavaIrpProtocolTest/src/main/java/org/harctoolbox/render \
    "$@"
"${JAVA}" -Djava.library.path="${DECODEIRDIR}" -jar "${JAR}" -c "${CONFIG}" \
    code -s --target javangtest \
    --directory ${IRPHOME}/../../JavaIrpProtocolTest/src/test/java/org/harctoolbox/render \
    "$@"
