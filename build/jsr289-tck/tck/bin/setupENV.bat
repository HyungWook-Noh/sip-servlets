@ECHO OFF
REM  This script is used for the environment variables setup for Windows
REM  installations of the JSR289 TCK.

SET ERRORLEVEL=0

@REM Check JAVA_HOME
IF "%JAVA_HOME%" == "" (
  ECHO JAVA_HOME is not set.
  ECHO Please set JAVA_HOME to the directory of your local JDK and the JDK version must be 1.5 or later.  
  GOTO EXIT
)

@REM Check ANT_HOME
IF "%ANT_HOME%" == "" ( 
  ECHO ANT_HOME is not set.
  ECHO Please set ANT_HOME to the directory of your local ant and the ant version must be 1.6.5 or later.
  GOTO EXIT
)

@REM Get and Set REFERENCEJAR from conf\signature.properties file
SET SIGCONFIG=..\conf\signature.properties
IF NOT EXIST %SIGCONFIG% (
  ECHO "The signature.properties does not exist in the conf directory"
  GOTO EXIT
)
FOR /f "tokens=2 delims== " %%j in ('more %SIGCONFIG%') do (  
  SET REFERENCEJAR=%%j   
)

@REM Set CLASSPATH for JSR289 TCK.
SET JSR289TCK_HOME=..\..
SET TCK_HOME=%JSR289TCK_HOME%\tck
SET TCK_LIB=%TCK_HOME%\lib
SET SIPUNIT_HOME=%JSR289TCK_HOME%\sipunit
SET SIPUNIT_LIB=%SIPUNIT_HOME%\lib
SET APIJAR=%TCK_LIB%\servlet-2_5-api.jar;%TCK_LIB%\sipservlet-1_1-api.jar
SET REPORT_HOME=%TCK_HOME%\report
SET SIGREPORT_HOME=%REPORT_HOME%\sig_report
SET SIGTEST_LIB=%TCK_LIB%\sigtest.jar
SET TCKJUNIT_LIB=%TCK_LIB%\tck-junit.jar
SET JUNIT_LIB=%SIPUNIT_LIB%\junit.jar
SET SIGNATURE_FILE_NAME=jsr289_api.sig
SET SIGNATURE_REPORT_NAME=jsr289SignatureReport.txt
SET CLASSPATH=%TCKJUNIT_LIB%;%JUNIT_LIB%;%SIGTEST_LIB%;%TCK_HOME%\conf 
SET PATH=%JAVA_HOME%/jre/bin;%ANT_HOME%/bin;%PATH%

GOTO RUN

:EXIT
 PAUSE
 SET ERRORLEVEL=1

:RUN
