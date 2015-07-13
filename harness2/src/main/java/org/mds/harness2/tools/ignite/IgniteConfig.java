package org.mds.harness2.tools.ignite;

import org.mds.harness.common2.runner.JMHRunnerConfig;

public class IgniteConfig extends JMHRunnerConfig {
    public int count;
    public int baseCount;
    public int length;
    //String,Object
    public String dataType;
}
