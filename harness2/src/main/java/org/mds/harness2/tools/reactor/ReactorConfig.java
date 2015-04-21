package org.mds.harness2.tools.reactor;

import org.mds.harness.common2.perf.PerfConfig;

/**
 * Created by modongsong on 2015/1/28.
 */
public class ReactorConfig extends PerfConfig {
    //1:event_loop,2:ring_buffer,3:thread_pool
    public int reactorType = 1;
    public int eventCount = 1;
    public int handleTime = 0;
}
