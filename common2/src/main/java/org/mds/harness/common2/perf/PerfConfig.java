package org.mds.harness.common2.perf;

/**
 * Created by Randall.mo on 14-4-18.
 */
public class PerfConfig {
    public int threadCount = 1;
    public int totalCount = 1;
    public int testRounds = 3;
    public String runs = "";
    public int runMode = 1;
    public int interval = 0;
    public int intervalUnit =1000*1000;
    public int batchSize = 100;
    public int output = 0;
    public boolean checkReturn = false;
}
