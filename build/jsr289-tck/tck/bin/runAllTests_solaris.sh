#!/bin/sh

# Starts the JSR289 TCK Tests which include Signature test,API tests and SPEC tests.
# Setup the environments before run JSR289 TCK.

. ${TCK_DIRECTORY}/bin/setupENV_solaris.sh

# Start JSR289 Signatre Test.
java -cp "${TCK_LIB}/servlet-2_5-api.jar:${REFERENCE_JAR}:${SIGTEST_LIB}" com.sun.tdk.signaturetest.SignatureTest -FileName ${SIGNATURE_FILE_NAME} -apiVersion 2.0 -out ${SIGNATURE_REPORT_NAME} -package javax.servlet.sip -debug -verbose

# Copy the Signature test report to signature report directory.        
if [ ! -d "$SIGREPORT_HOME" ]; then
	mkdir -p ${SIGREPORT_HOME}
fi

cp ${SIGNATURE_REPORT_NAME} ${SIGREPORT_HOME}
rm ${SIGNATURE_REPORT_NAME}
 
cd ${TCK_HOME}
ant run-tck
cd ./bin
