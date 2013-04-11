@ECHO OFF
REM Start the JSR289 Signature test.

REM  Setup the environments before run JSR289 Signature test.
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

@REM Start the JSR289 Signature Test.
%JAVA_HOME%\jre\bin\java -cp %TCK_LIB%\servlet-2_5-api.jar;%SIGTEST_LIB%;%REFERENCEJAR% com.sun.tdk.signaturetest.SignatureTest -FileName %SIGNATURE_FILE_NAME% -apiVersion 2.0 -out %SIGNATURE_REPORT_NAME% -package javax.servlet.sip -debug -verbose

@REM Copy the signature report to signature report directory. 
IF NOT EXIST %SIGREPORT_HOME% (
 MKDIR %SIGREPORT_HOME%
 GOTO CREATE_REPORT_DIR
)
:CREATE_REPORT_DIR
COPY %SIGNATURE_REPORT_NAME% %SIGREPORT_HOME%
DEL %SIGNATURE_REPORT_NAME%

:END

