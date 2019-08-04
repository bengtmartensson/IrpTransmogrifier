@echo off
echo Suck a Lirc config file from the Internet and convert it to IRP
@echo on
call irptransmogrifier lirc --command http://lirc.sourceforge.net/remotes/yamaha/RX-V995
pause
