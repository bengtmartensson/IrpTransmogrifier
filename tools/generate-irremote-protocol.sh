#!/bin/sh

IRPTRANSMOGRIFIER="tools/irptransmogrifier --url"
GENERATE="${IRPTRANSMOGRIFIER} --loglevel info --logformat %5\$s%n code -s src/main/st"
IRREMOTE_DIR=../Arduino-IRremote
IRREMOTE_SRC=${IRREMOTE_DIR}/src
SIL_SRC=../IRremote-SIL/

protocol=$1
cname=`${IRPTRANSMOGRIFIER} --quiet list --cname "${protocol}"`
if [ -z "${cname}" ] ; then
    echo "ERROR: Protocol ${protocol} non-existing, bailing out"
    exit 1
fi

# Generate code, include files, symbols
${GENERATE} --dir ${IRREMOTE_SRC} -t irremote-renderer-cppsymbols -t irremote-renderer-declarations -t irremote-renderer-code  "${protocol}"

# Generate include files
#${GENERATE} --dir ${IRREMOTE_SRC}/private -t irremote-renderer-cppsymbols -t irremote-renderer-declarations  ${protocol}

# Generate SIL test code and Makefile
${GENERATE} --dir ${SIL_SRC} -t irremote-renderer-test -t irremote-renderer-test-makefile  "${protocol}"

# Assemble symbols
cat ${IRREMOTE_SRC}/ir_*.symbs > ${IRREMOTE_SRC}/private/extra-protocol-symbols.inc

# Assemble symbols
cat ${IRREMOTE_SRC}/ir_*.decl  > ${IRREMOTE_SRC}/private/extra-protocol-declarations.inc

# perform SIL test
cd ${SIL_SRC}
make -f Makefile.${cname} clean
make -j -f Makefile.${cname} decode

#cd ${IRREMOTE_DIR}
#doxygen > /dev/null
