#!/bin/bash
echo "Application start to build ....."
mvn clean package
echo "Application started too run ....."
java -jar target/Player2-1.0-SNAPSHOT.jar