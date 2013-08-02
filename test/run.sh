#!/bin/bash

cd ../initial
mvn clean package
./gradlew clean build

cd ../complete
mvn clean package
./gradlew build
