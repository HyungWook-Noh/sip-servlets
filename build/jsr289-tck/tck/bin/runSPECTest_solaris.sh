#!/bin/sh

# Starts the JSR289 TCK SPEC Test.

# Setup the environments before run JSR289 SPEC TCK

. ${TCK_DIRECTORY}/bin/setupENV_solaris.sh
 
cd ${TCK_HOME}
ant run-spec-tests
cd ./bin
