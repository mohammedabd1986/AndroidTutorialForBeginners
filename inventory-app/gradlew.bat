
@ECHO OFF
SETLOCAL

SET DIR=%~dp0
IF "%DIR:~-1%"=="\" SET DIR=%DIR:~0,-1%
SET APP_HOME=%DIR%..
SET APP_BASE_NAME=%~n0
SET APP_NAME=Gradle

SET DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"

SET CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

IF NOT "%JAVA_HOME%"=="" GOTO findJavaFromJavaHome

SET JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
IF "%ERRORLEVEL%" == "0" GOTO execute

ECHO.
ECHO ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
GOTO fail

:findJavaFromJavaHome
SET JAVA_HOME=%JAVA_HOME%"
SET JAVA_EXE=%JAVA_HOME%\bin\java.exe

IF EXIST "%JAVA_EXE%" GOTO execute

ECHO.
ECHO ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
GOTO fail

:execute
SET APP_ARGS=
:argLoop
IF "%~1"=="" GOTO endArgs
SET APP_ARGS=%APP_ARGS% "%~1"
SHIFT
GOTO argLoop
:endArgs

"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% -Dorg.gradle.appname=%APP_BASE_NAME% -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %APP_ARGS%
GOTO end

:fail
EXIT /B 1

:end
ENDLOCAL
