#!/bin/sh

# Warning: This script may overwrite existing ir_*.cpp files!
# Be sure you know what you are doing!

# Set this to the directory containing the IRremote file
IRREMOTE_DIR=../Arduino-IRremote-bm

# Use this line if IRremote branch "beta"
IRREMOTE_SRC=${IRREMOTE_DIR}/src

# ... otherwise this
#IRREMOTE_SRC=${IRREMOTE_DIR}

# If desired, set this to location of the SIL project
# (checkout from git@github.com:bengtmartensson/IRremote-SIL.git)
# If not desired, leave empty or undefined.
SIL_SRC=../IRremote-SIL/

# Normally no need to change after this line

if [ ! $# == 1 ] ; then
    echo "Usage: $0: <protocol-name>"
    exit 1
fi

IRPTRANSMOGRIFIER="tools/irptransmogrifier --url"
GENERATE="${IRPTRANSMOGRIFIER} --loglevel info --logformat %5\$s%n code -s src/main/st"

protocol=$1
cname=`${IRPTRANSMOGRIFIER} --quiet list --cname "${protocol}"`
if [ -z "${cname}" ] ; then
    echo "ERROR: Protocol ${protocol} non-existing, bailing out"
    exit 1
fi

# Generate code, include files, symbols
${GENERATE} --dir ${IRREMOTE_SRC} -t irremote-renderer-cppsymbols -t irremote-renderer-declarations -t irremote-renderer-code  "${protocol}"

# Assemble symbols
cat ${IRREMOTE_SRC}/ir_*.symbs > ${IRREMOTE_SRC}/extra-protocol-symbols.inc

# Assemble symbols
cat ${IRREMOTE_SRC}/ir_*.decl  > ${IRREMOTE_SRC}/extra-protocol-declarations.inc

# Perform SIL tests if SIL_SRC is defined to something non-zero.
if [ -n "${SIL_SRC}" ] ; then
    # Generate SIL test code and Makefile
    ${GENERATE} --dir ${SIL_SRC} -t irremote-renderer-test -t irremote-renderer-test-makefile  "${protocol}"

    # perform SIL test
    cd ${SIL_SRC}
    make -f Makefile.${cname} clean
    make -j -f Makefile.${cname} IRREMOTE_DIR=${IRREMOTE_DIR} decode
fi