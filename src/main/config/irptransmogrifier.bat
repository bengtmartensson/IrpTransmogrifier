@echo off

REM Wrapper for IrpTransmogrifier as a command line program for Windows/MSDOS.

REM The command line name to use to invoke java.exe, fix if desired.
set JAVA=java

REM Where the files are located, change if desired
set APPLICATIONHOME=%~dp0

REM Normally no need to change the rest of the file
set JAR=%APPLICATIONHOME%\IrpTransmogrifier-${project.version}-jar-with-dependencies.jar

"%JAVA%" -jar "%JAR%" %*
