@echo off

PATH = %~dp0..\tools\tool-stm8flash;C:\STMicroelectronics\st_toolset\stvp;%PROGRAMFILES%\STMicroelectronics\st_toolset\stvp;%PROGRAMFILES(x86)%\STMicroelectronics\st_toolset\stvp;%PATH%;

:FLASH
STVP_CmdLine -BoardName=ST-LINK -ProgMode=SWIM -Port=USB -Device=STM8S105x6 -FileProg=../bin/main.hex -FileData=data_empty.hex -verbose -no_loop -verif -no_warn_protect
if errorlevel 1 goto STM8FLASH
goto PASS

:STM8FLASH
echo.
echo Flashing using STVP failed. Press key to try STM8FLASH tool..
pause > nul
make clear_eeprom
make flash
if errorlevel 1 goto FAIL

:PASS
echo.
echo Flash successful
goto EXIT
:FAIL
echo.
echo Flashing error!!, press key to try again
pause > nul
goto FLASH
pause
:EXIT
@echo on
exit /b %ERRORLEVEL%