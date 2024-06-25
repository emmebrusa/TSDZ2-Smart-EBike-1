@echo off
setlocal enabledelayedexpansion

if "%~1"=="" (
    echo Usage: cp source_file target_file
    exit /b 1
)
if "%~2"=="" (
    echo Usage: cp source_file target_file
    exit /b 1
)

rem Convert relative paths to absolute paths
for %%I in ("%~1") do set "source_path=%%~fI"
for %%I in ("%~2") do set "target_path=%%~fI"

echo Current Working Directory: %CD%
echo Source Path: !source_path!
echo Target Path: !target_path!

if not exist "!source_path!" (
    echo Source file "!source_path!" does not exist.
    exit /b 1
)

copy "!source_path!" "!target_path!"
if errorlevel 1 (
    echo Error occurred during copying.
) else (
    echo File copied successfully.
)
