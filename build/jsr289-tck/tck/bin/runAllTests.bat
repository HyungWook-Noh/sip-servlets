@ECHO OFF
REM Starts the JSR289 TCK Tests which include Signature test,API tests and SPEC tests.

REM Setup the environments before run JSR289 TCK.
IF NOT EXIST setupENV.bat (
 ECHO "The setupENV.bat does not exist in the current directory"
 GOTO END
)
CALL setupENV.bat

IF %ERRORLEVEL% == 1 GOTO END

IF NOT EXIST %REFERENCEJAR% (
  ECHO "The %REFERENCEJAR% does not exist."
  GOTO END
)

@REM Start JSR289 Signatre Test.
%JAVA_HOME%\jre\bin\java -cp %TCK_LIB%\servlet-2_5-api.jar;%SIGTEST_LIB%;%REFERENCEJAR% com.sun.tdk.signaturetest.SignatureTest -FileName %SIGNATURE_FILE_NAME% -apiVersion 2.0 -out %SIGNATURE_REPORT_NAME% -package javax.servlet.sip -debug -verbose

@REM Copy the Signature test report to signature report directory. 
IF NOT EXIST %SIGREPORT_HOME% (
 MKDIR %SIGREPORT_HOME%
 GOTO CREATE_REPORT_DIR
)
:CREATE_REPORT_DIR

COPY %SIGNATURE_REPORT_NAME% %SIGREPORT_HOME%
DEL %SIGNATURE_REPORT_NAME%
 
CD %TCK_HOME%
SET CLASSPATH=lib\tck-junit.jar;..\sipunit\lib\junit.jar;lib\sigtest.jar;.\conf
ant run-tck
CD %TCK_HOME%\bin

:END

