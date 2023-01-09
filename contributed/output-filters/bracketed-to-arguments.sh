#!/bin/sh

# Transforms the --raw output to shell arguments,
# something that can be easily read by another program, like broadlink_cli,
# https://github.com/mjg59/python-broadlink

sed -e 's/Freq=[0-9]*Hz//' \
    -e 's/\[//g' \
    -e 's/\]/ /g' \
    -e 's/,/ /g' \
    -e 's/[+-]/ /g'
