package org.mds.harness2.tools.ignite;

import org.mds.harness.common2.runner.JMHRunnerConfig;

public class IgniteConfig extends JMHRunnerConfig {
    public int count;
    public int baseCount;
    public int length;
    public int maxId=10000;
    //String,Object,indexType
    public int dataType;
    public String cacheConfig="ignite-client.xml";
    public String cacheName="DefaultCache";
}
