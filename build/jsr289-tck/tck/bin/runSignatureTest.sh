#!/bin/sh

# Start the JSR289 Signature test.

# Setup the environments before run JSR289 Signature test

. ${TCK_DIRECTORY}/bin/setupENV.sh

# Start the JSR289 Signature Test.
java -cp "${TCK_LIB}/servlet-2_5-api.jar:${REFERENCE_JAR}:${SIGTEST_LIB}" com.sun.tdk.signaturetest.SignatureTest -FileName ${SIGNATURE_FILE_NAME} -apiVersion 2.0 -out ${SIGNATURE_REPORT_NAME} -package javax.servlet.sip -debug -verbose

if [ ! -d "$SIGREPORT_HOME" ]; then
	mkdir -p ${SIGREPORT_HOME}
fi

cp ${SIGNATURE_REPORT_NAME} ${SIGREPORT_HOME}
rm ${SIGNATURE_REPORT_NAME}
