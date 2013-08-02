#!/bin/bash

cd $(dirname $0)
cd ../initial
mvn clean package
./gradlew clean build

cd ../complete
mvn clean package
./gradlew build
