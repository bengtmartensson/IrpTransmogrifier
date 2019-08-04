@echo off
REM Demo of IrpTransmogrifier version 0.2.0

echo Welcome to the interactive demo of IrpTransmogrifier.
echo It is assumed that the command "irptransmogrifier" starts the program.
pause

echo First let's check that java is working.

java -version
if errorlevel 1 goto javabroken
echo
echo Java seems to be working!
pause
echo Now let's try the short help
pause
@echo on
call irptransmogrifier help --short
@echo off
pause

echo So, IrpTransmogrifier takes exactly one command, one of the words listed
echo in


exit

javabroken:
echo Your java installation is broken
pause
