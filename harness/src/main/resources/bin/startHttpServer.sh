#!/bin/sh

#nohup java -server -Xmx6G -Xms6G -Xmn2G -XX:+DisableExplicitGC \
#      -XX:SurvivorRatio=1 -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled \
#      -XX:+UseCMSCompactAtFullCollection -XX:CMSMaxAbortablePrecleanTime=500 \
#      -XX:CMSPermGenSweepingEnabled -XX:+CMSClassUnloadingEnabled -XX:+PrintClassHistogram \
#      -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintHeapAtGC -Xloggc:gc.log \
#      -Djava.ext.dirs=lib com.test.server.HttpChunkedServer 8000 >server.out 2>&1 &

JAVA_OPTS=" -server -Xms512m -Xmx1024m -Xss256k -XX:MaxNewSize=64m"
#CMS GC
#JAVA_OPTS=" -XX:+UseParNewGC -XX:ParallelGCThreads=8 -XX:+UseConcMarkSweepGC $JAVA_OPTS"
#G1 GC
#Base parameters
JAVA_OPTS=" -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:MaxGCPauseMillis=50 -XX:GCPauseIntervalMillis=200 $JAVA_OPTS"
JAVA_VERSION="1.7"

test_classpath="conf"
for libFile in lib/*
do
   test_classpath="${test_classpath}:${libFile}"
done

java -classpath ${test_classpath} ${JAVA_OPTS} org.mds.harness.server.HttpServer $*

