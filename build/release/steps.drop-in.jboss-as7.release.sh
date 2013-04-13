BUILD_DIR=./sip-servlets-bootstrap/release/target/mss-as7
#
# set AS7_TAG as name of the AS7 release/tag
AS7_TAG=jboss-as-7.1.1.Final
VERSION=1.8.0-SNAPSHOT

rm -rf $BUILD_DIR
mkdir -p $BUILD_DIR

#
# get as7 release (trunk could be used too, nightly build is not enough as it does not include artifacts
#   needed to built our extension)
## 
## to get nightly
##wget https://ci.jboss.org/jenkins/job/JBoss-AS-7.x-latest/lastSuccessfulBuild/artifact/build/target/jboss-as-7.x.zip
##unzip jboss-as-7.x.zip
## to get trunk and build
##git clone https://github.com/jbossas/jboss-as.git
##cd jboss-as
##mvn clean install -DskipTests
#
# must be in sync with
wget -nc http://download.jboss.org/jbossas/7.1/jboss-as-7.1.1.Final/jboss-as-7.1.1.Final.zip -O $BUILD_DIR/../../jboss-as-7.1.1.Final.zip
unzip $BUILD_DIR/../../jboss-as-7.1.1.Final.zip -d $BUILD_DIR

#
# get sipservlets repo and build, including as7 abstraction layer
#git clone https://code.google.com/p/sipservlets/
#cd sipservlets
#git checkout trunk
#mvn clean install -Pas7

#
# build sip-servlets-as7-drop-in modules and install in AS7
mvn clean install -f ./containers/sip-servlets-as7-drop-in/jboss-as-mobicents/pom.xml

mvn clean package -f ./containers/sip-servlets-as7-drop-in/build-mobicents-modules/pom.xml
#
# modules installation
cp -pr ./containers/sip-servlets-as7-drop-in/build-mobicents-modules/target/$AS7_TAG/modules/org/mobicents/ $BUILD_DIR/$AS7_TAG/modules/org/
rm ./sip-servlets-examples/click-to-call/src/main/sipapp/WEB-INF/jboss-web.xml
mvn clean install war:inplace -f ./sip-servlets-examples/click-to-call/pom.xml
cp -pr ./sip-servlets-examples/click-to-call/target/click-to-call-servlet-*.war $BUILD_DIR/$AS7_TAG/standalone/deployments
cp $BUILD_DIR/../../../src/site/resources/click2call-dar.properties $BUILD_DIR/$AS7_TAG/standalone/configuration/dars/mobicents-dar.properties
cp $BUILD_DIR/mss-sip-stack.properties $BUILD_DIR/$AS7_TAG/standalone/configuration/

cd $BUILD_DIR/$AS7_TAG
patch -p0 --verbose < ../../../../../containers/sip-servlets-as7-drop-in/patches/patch.standalone.sip.dropin.xml

#
# Configure jboss-as-web module
patch -p0 --verbose < ../../../../../containers/sip-servlets-as7-drop-in/patches/patch.jboss-as-web.module.xml

#
# Configure jboss-as-ee module
patch -p0 --verbose < ../../../../../containers/sip-servlets-as7-drop-in/patches/patch.jboss-as-ee.module.xml

cd ..
zip -r ../../mss-$VERSION-$AS7_TAG-$(date '+%s').zip $AS7_TAG

#
# Run AS7
#export JBOSS_HOME=$BUILD_DIR/$AS7_TAG
#./bin/standalone.sh -c standalone-sip.xml -Djavax.servlet.sip.dar=file:///tmp/mobicents-dar.properties

