@echo off
echo Renders a NEC1 signal with D=12, S=34, F=56; output it as Pronto Hex AND raw
@echo on
call irptransmogrifier render --raw --hex -n "D=12, S=34, F=56" nec1
pause
