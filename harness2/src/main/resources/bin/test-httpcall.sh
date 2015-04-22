#!/bin/sh

#JAVA_OPTS=" -server -Xmx3g -Xms3g -Xss256k -XX:MaxNewSize=128m -Dhttp.maxConnections=100"
#JAVA_OPTS=" -XX:+UseParNewGC -XX:ParallelGCThreads=8 -XX:+UseConcMarkSweepGC $JAVA_OPTS"
#JAVA_VERSION="1.7"

#G1 GC
#Base parameters
JAVA_OPTS=" -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:MaxGCPauseMillis=50 -XX:GCPauseIntervalMillis=200 $JAVA_OPTS"

test_classpath="conf"
for libFile in lib/*
do
   test_classpath="${test_classpath}:${libFile}"
done

java -classpath ${test_classpath} ${JAVA_OPTS} org.mds.harness2.tools.httpbench.HttpBench $*

