#!/bin/sh -x
cd $(dirname $0)

cd ../complete

which mvn
mvn --version
ps -ef | grep java
ls -a
cat .classpath
cat .project

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
