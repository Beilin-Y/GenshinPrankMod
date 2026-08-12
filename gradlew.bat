@echo off
setlocal
set DIRNAME=%~dp0
set APP_HOME=%DIRNAME%..\
set JAVA_EXE=%JAVA_HOME%\bin\java.exe
if exist %JAVA_EXE% goto haveJavaHome
set JAVA_EXE=java
:haveJavaHome
"%JAVA_EXE%" -jar "%APP_HOME%gradle\wrapper\gradle-wrapper.jar" %*
