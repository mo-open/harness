#!/bin/sh

ARGS=($*)
TARGET=${ARGS[0]}
unset ${ARGS[0]}
MAIN_CLASS=org.mds.harness.common2.runner.dsm.DsmRunnerMain

source ./RunnerMain.sh
source ./dsm-functions.sh
main
