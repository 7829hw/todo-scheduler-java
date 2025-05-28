REM run-client.bat  
@echo off
echo Compiling Java files...
javac -encoding UTF-8 *.java
if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b 1
)
echo Starting Calendar Client...
java ProgramApp
pause