package org.mds.harness.tools.httpbench;


import org.mds.harness.common.perf.PerfConfig;

/**
 * @author Dongsong
 */
public class Configuration extends PerfConfig {
    public String httpURL = "";
    public String host = "";
    public int maxThreshold = 100;
    public int port = 80;
    public int statCount = 10000;
    public int asyncMaxTotal = 1000;
    public int asyncMaxPerRoute = 100;
    public int remapType = 0;
    public boolean parse = false;
    public boolean returnStream = true;
    public int workerCount = 5;
    public int httpThreads = 5;
    public int bufferSize=10000;
    public int parseThreads = 2;
    public int remapThreads = 2;
    public int peakDuration=1000;
    public int commonDuration=100000;
    public int consumerTime=100;
}
