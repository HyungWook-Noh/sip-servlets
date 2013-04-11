@ECHO OFF
REM Starts the JSR289 API Test.

REM Setup the environments before run JSR289 TCK Test.
IF NOT EXIST setupENV.bat (
 ECHO "The setupENV.bat does not exist in the current directory"
 GOTO End
)
CALL setupENV.bat

IF %ERRORLEVEL% == 1 GOTO END
 
CD %TCK_HOME%
SET CLASSPATH=lib\tck-junit.jar;..\sipunit\lib\junit.jar;lib\sigtest.jar
ant run-api-tests
CD %TCK_HOME%\bin

:END

