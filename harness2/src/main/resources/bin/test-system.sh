#!/bin/sh

JAVA_OPTS=" -Xms256m -Xmx512m -Xss256k -XX:MaxNewSize=64m"
JAVA_OPTS=" -XX:+UseParNewGC -XX:ParallelGCThreads=8 -XX:+UseConcMarkSweepGC $JAVA_OPTS"
JAVA_VERSION="1.6"

test_classpath="conf"
for libFile in lib/*
do
   test_classpath="${test_classpath}:${libFile}"
done

java -classpath ${test_classpath} ${JAVA_OPTS} org.mds.harness2.tools.lang.TestSystem $*

