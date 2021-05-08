#!/bin/bash

cwd=$(cd `dirname $0` && pwd)

app=`ls $cwd/target/test-ebridge-*-fat.jar`
if [ "$app" == "" ]; then
  echo -e "Build the fat jar"
  mvn -f $cwd/pom.xml clean install
fi

app=`ls $cwd/target/test-ebridge-*-fat.jar`
if [ "$app" == "" ]; then
  echo -e "Not built, something wrong"
  return 1
fi

java -jar $app $@


