#!/bin/bash

cd $(dirname $0)
mvn clean package && mv target/amicus.jar . && chmod +x amicus.jar
