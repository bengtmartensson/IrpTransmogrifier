#!/bin/sh

TESTPROJECT=../../JavaIrpProtocolTest

cd `dirname $0`
rm -f ${TESTPROJECT}/src/*/java/org/harctoolbox/*/*

./generate-java-renderer-tests.sh
./generate-java-decoder-tests.sh


cd ${TESTPROJECT}
mvn test
