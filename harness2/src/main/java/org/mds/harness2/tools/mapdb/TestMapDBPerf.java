package org.mds.harness2.tools.mapdb;

import org.mds.harness.common2.perf.PerfTester;
import org.mds.harness.common2.runner.RunnerHelper;
import org.mds.harness2.tools.memcached.TestMemcachedConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mapdb.*;

/**
 * Created by modongsong on 2015/4/22.
 */
public class TestMapDBPerf {
    private final static Logger log = LoggerFactory.getLogger(TestMapDBPerf.class);



    public void runCommon(final TestMapDBConfig config) {


        new PerfTester<PerfTester.SingleTask>("Test MapDB Memory", config)
                .run((conf, index) -> {

                    return 1;
                });

    }


    public static void main(String args[]) throws Exception {
        RunnerHelper.run(args, TestMapDBPerf.class,
                TestMapDBConfig.class,
                "testMapDB.yml");
    }
}
