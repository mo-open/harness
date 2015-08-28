#!/bin/sh

ARGS=($*)
TARGET=${ARGS[0]}
unset ${ARGS[0]}
MAIN_CLASS=org.mds.harness.common2.runner.jmh.JMHRunnerMain

source ./RunnerMain.sh
source ./jmh-functions.sh
main

