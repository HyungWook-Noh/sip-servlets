@ECHO OFF
REM Starts the JSR289 SPEC test.

REM Setup the environments before run JSR289 SPEC test.
IF NOT EXIST setupENV.bat (
 ECHO "The setupENV.bat does not exist in the current directory."
 GOTO END
)
CALL setupENV.bat

IF %ERRORLEVEL% == 1 GOTO END

CD %TCK_HOME%
SET CLASSPATH=lib\tck-junit.jar;..\sipunit\lib\junit.jar;lib\sigtest.jar
ant run-spec-tests
CD %TCK_HOME%\bin
:END

