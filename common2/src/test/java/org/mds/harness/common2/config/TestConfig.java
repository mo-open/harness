package org.mds.harness.common2.config;

import org.mds.harness.common2.perf.PerfConfig;

/**
 * Created by modoso on 15/4/23.
 */
public class TestConfig {
    public int a;
    public String b;
    public String c;
    public int d;
    public JMH jmh;

    public static class JMH {
        int count;
    }
}
