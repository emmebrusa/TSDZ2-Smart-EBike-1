@ECHO OFF

SET version=20.1C.6-0
SET settings_date=%1

SET release_folder=%~dp0releases
SET backup_folder=%~dp0releases\backup

CD src
CALL clean.bat      || GOTO :EXIT
CALL compile.bat    || GOTO :EXIT

ECHO Copying firmware to release folder.
ECHO %release_folder%\TSDZ2-%version%-PROGRAM.hex
MKDIR "%release_folder%" >NUL 2>NUL
COPY ..\bin\main.hex "%release_folder%\TSDZ2-%version%.hex"
MKDIR "%backup_folder%" >NUL 2>NUL
COPY ..\bin\main.hex "%backup_folder%\TSDZ2-%settings_date%.hex" >NUL 2>NUL

echo Press any key to flash... (Ctl+C to stop)
pause > nul
CALL flash.bat
echo press any key to close...
pause > nul

:EXIT
EXIT