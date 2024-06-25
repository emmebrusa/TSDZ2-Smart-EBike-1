@echo off
if exist "%windir%\syswow64\cmd.exe" goto CYGWIN64

PATH = %PATH%;%~dp0..\tools\cygwin_32\bin;
goto CLEAN

:CYGWIN64
PATH = %PATH%;%~dp0..\tools\cygwin_64\bin;

:CLEAN
del /q main.hex >NUL 2>NUL
make clean || goto FAIL
echo.

:PASS
goto EXIT
:FAIL
echo Cleaning failed!!
pause
:EXIT
exit /b %ERRORLEVEL%


