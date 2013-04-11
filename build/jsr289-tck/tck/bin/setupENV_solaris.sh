#!/bin/sh

# This script is used for the environment variables setup for Linux/UNIX

	if [ "${JAVA_HOME}" = "" ] ; then 
	  echo "JAVA_HOME is not set."
    echo "Please set JAVA_HOME to the directory of your local JDK and the JDK version must be 1.5 or later."  
  	exit
	fi		


	if [ "${ANT_HOME}" = "" ] ; then 
			echo "ANT_HOME is not set."
			echo "Please set ANT_HOME to the directory of your local ant and the ant version must be 1.6.5 or later."
			exit
	fi



# Get and Set REFERENCE_JAR from conf/signature.properties file
SIGCONFIG=${TCK_DIRECTORY}/conf/signature.properties
export SIGCONFIG
	if [ ! -f "$SIGCONFIG" ]; then
		echo "The signature.properties does not exist in the conf directory"
		exit
	fi


# set the location of the referenced JSR289 Jar file for signature test. 
REFERENCE_JAR=`awk   'BEGIN{FS="="} {print  $2 }'< ${SIGCONFIG}`
export REFERENCE_JAR
	if [ ! -f "$REFERENCE_JAR" ]; then
		echo "The reference jar: $REFERENCE_JAR does not exist "
		exit
	fi



# Set CLASSPATH for JSR289 TCK.
JSR289TCK_HOME=${TCK_DIRECTORY}/..
export JSR289TCK_HOME
TCK_HOME=${JSR289TCK_HOME}/tck
export TCK_HOME
TCK_LIB=${TCK_HOME}/lib
export TCK_LIB
SIPUNIT_HOME=${JSR289TCK_HOME}/sipunit
export SIPUNIT_HOME
SIPUNIT_LIB=${SIPUNIT_HOME}/lib
export SIPUNIT_LIB
APIJAR="${TCK_LIB}/servlet-2_5-api.jar:${TCK_LIB}/sipservlet-1_1-api.jar"
export APIJAR
REPORT_HOME=${TCK_HOME}/report
export REPORT_HOME
SIGREPORT_HOME=${REPORT_HOME}/sig_report
export SIGREPORT_HOME
SIGTEST_LIB=${TCK_LIB}/sigtest.jar
export SIGTEST_LIB
TCKJUNIT_LIB=${TCK_LIB}/tck-junit.jar
export TCKJUNIT_LIB
JUNIT_LIB=${SIPUNIT_LIB}/junit.jar
export JUNIT_LIB
SIGNATURE_FILE_NAME=${TCK_DIRECTORY}/bin/jsr289_api.sig
export SIGNATURE_FILE_NAME
SIGNATURE_REPORT_NAME=jsr289SignatureReport.txt
export SIGNATURE_REPORT_NAME
CLASSPATH=${JAVA_HOME}/lib/tools.jar:${TCKJUNIT_LIB}:${JUNIT_LIB}:${SIGTEST_LIB}:${TCK_HOME}/conf
export CLASSPATH
PATH=${JAVA_HOME}/jre/bin:${ANT_HOME}/bin:$PATH
export PATH



 
