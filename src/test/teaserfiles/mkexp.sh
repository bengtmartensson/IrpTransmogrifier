#!/bin/sh

expfile=$1


rm "${expfile}"
make MAKE_EXP=1 "${expfile}"
