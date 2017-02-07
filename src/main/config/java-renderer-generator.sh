#!/bin/sh

JAVA=java

IRPHOME="$(dirname -- "$(readlink -f -- "${0}")" )"
JAR=${IRPHOME}/${project.name}-${project.version}-jar-with-dependencies.jar
CONFIG=${IRPHOME}/IrpProtocols.xml

"${JAVA}" -jar "${JAR}" -c "${CONFIG}" \
    code --target java-renderer \
    --directory ${IRPHOME}/../../JavaIrpProtocolTest/src/main/java/org/harctoolbox/render \
    "$@"
"${JAVA}" -jar "${JAR}" -c "${CONFIG}" \
    code --target java-renderer-test \
    --directory ${IRPHOME}/../../JavaIrpProtocolTest/src/test/java/org/harctoolbox/render \
    "$@"
