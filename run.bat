@echo off
rem 
rem  
rem 
rem  
rem 
setlocal enabledelayedexpansion
cd /d "%~dp0"

set "JAVAC=javac"
set "JAVA=javaw"

where javac >nul 2>nul
if not errorlevel 1 goto found

if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javac.exe" (
    set "JAVAC=%JAVA_HOME%\bin\javac.exe"
    set "JAVA=%JAVA_HOME%\bin\javaw.exe"
    goto found
)

for /d %%d in ("C:\Program Files\Microsoft\jdk*") do (
    if exist "%%~d\bin\javac.exe" (
        set "JAVAC=%%~d\bin\javac.exe"
        set "JAVA=%%~d\bin\javaw.exe"
        goto found
    )
)
for /d %%d in ("C:\Program Files\Eclipse Adoptium\jdk*") do (
    if exist "%%~d\bin\javac.exe" (
        set "JAVAC=%%~d\bin\javac.exe"
        set "JAVA=%%~d\bin\javaw.exe"
        goto found
    )
)
for /d %%d in ("C:\Program Files\Java\jdk*") do (
    if exist "%%~d\bin\javac.exe" (
        set "JAVAC=%%~d\bin\javac.exe"
        set "JAVA=%%~d\bin\javaw.exe"
        goto found
    )
)

echo.
echo   STARFALL needs a Java JDK, and none was found.
echo.
echo   Install the free Microsoft Build of OpenJDK 21
echo   directly into this folder with this command:
echo.
echo       winget install Microsoft.OpenJDK.21
echo.
echo   Then open a NEW terminal and double-click run.bat again.
echo.
pause
exit /b 1

:found
if not exist "out\starfall\Main.class" goto build
goto run

:build
echo Compiling STARFALL...
if not exist "out" mkdir "out"
"%JAVAC%" -encoding UTF-8 -d out -sourcepath src src\starfall\Main.java
if errorlevel 1 (
    echo.
    echo   Compilation failed. See the messages above.
    pause
    exit /b 1
)

:run
start "" "%JAVA%" -cp out starfall.Main
exit /b 0
