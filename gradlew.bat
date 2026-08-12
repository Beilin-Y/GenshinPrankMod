@echo off
:: -----------------------------------------------------------------------------
:: Gradle start up script for Windows
:: -----------------------------------------------------------------------------

SETLOCAL

set DIRNAME=%~dp0
set APP_BASE_DIR=%DIRNAME%
set GRADLE_HOME=%APP_BASE_DIR%gradle\wrapper
set CLASSPATH=%GRADLE_HOME%\gradle-wrapper.jar

if defined JAVA_HOME (
  set RUNJAVA=%JAVA_HOME%\bin\java
) else (
  set RUNJAVA=java
)

"%RUNJAVA%" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
