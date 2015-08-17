package org.mds.harness2.tools.mongo;

import org.mds.harness.common2.runner.dsm.DsmRunnerConfig;

/**
 * @author Dongsong
 */
public class Configuration extends DsmRunnerConfig {

    public String replicaSet = null;

    public String databaseName = null;

    public String username = null;

    public String password = null;

    public boolean dropFirst = true;
    public int dataTypeCount = 50;
    public int dataItemCount = 100000;
    //node: -1; 0: unack; 1: ack
    public int writeMode = -1;
    public int dataDuration = 1800000;
    public int expireTime=30;
    public int groupCount = 100;
}
