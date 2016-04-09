#!/bin/sh

ARGS=($*)
TARGET=${ARGS[0]}
unset ${ARGS[0]}
MAIN_CLASS=org.mds.harness.common2.runner.dsm.DsmRunnerMain

source ./RunnerMain.sh
source ./dsm-functions.sh

if [[ "$TARGET" = "" ]]; then
    echo "Please specify the target, optionals: "
    showTargets "./dsm-functions.sh"
    exit 1
fi

main
