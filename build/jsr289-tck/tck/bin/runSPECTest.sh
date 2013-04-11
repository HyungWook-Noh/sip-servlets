#!/bin/sh
# Starts the JSR289 TCK SPEC Test.

# Setup the environments before run JSR289 SPEC TCK

. ${TCK_DIRECTORY}/bin/setupENV.sh
 
cd ${TCK_HOME}
ant run-spec-tests
cd ./bin
