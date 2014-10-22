package org.mds.harness.tools.mongo;

import org.mds.harness.common.perf.PerfConfig;

/**
 * @author Dongsong
 */
public class Configuration extends PerfConfig {

    String replicaSet = null;

    String databaseName = null;

    String username = null;

    String password = null;

    boolean dropFirst = true;
    int dataTypeCount = 50;
    int dataItemCount = 100000;
    //node: -1; 0: unack; 1: ack
    int writeMode = -1;
    int dataDuration = 1800000;
    int expireTime=30;
    int groupCount = 100;
}
