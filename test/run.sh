#!/bin/sh -x
cd $(dirname $0)

cd ../complete

which javac
javac -version
which mvn
mvn --version
env
echo $JAVA_HOME
ls -l `which javac`
ls $JAVA_HOME/bin

dpkg -l | grep openjdk

uname -a

lsb_release -a

mvn -e clean package
ret=$?
if [ $ret -ne 0 ]; then
exit $ret
fi
rm -rf target

./gradlew build
ret=$?
if [ $ret -ne 0 ]; then
exit $ret
fi
rm -rf build

cd ../initial

mvn clean package
ret=$?
if [ $ret -ne 0 ]; then
exit $ret
fi
rm -rf target

./gradlew build
ret=$?
if [ $ret -ne 0 ]; then
exit $ret
fi
rm -rf build

exit
