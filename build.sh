#!/bin/bash

cd $(dirname $0)
rm src/main/resources/typeSystems/*
cp TypeSystems/*TypeSystem.xml src/main/resources/typeSystems/
mvn clean package && mv target/amicus.jar . && chmod +x amicus.jar
