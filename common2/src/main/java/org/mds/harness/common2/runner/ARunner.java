package org.mds.harness.common2.runner;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.mds.harness.common2.config.ConfigurationHelper;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@BenchmarkMode(Mode.All)
@State(Scope.Benchmark)
public abstract class ARunner<C extends JMHRunnerConfig> {
    protected final static Logger log = LoggerFactory.getLogger(ARunner.class);

    @Param({""})
    private String args;

    private C config;

    @Setup
    public void init(BenchmarkParams params) {
        try {
            log.info("-----"+args);
            String mainClassName = StringUtils.substringBeforeLast(params.getBenchmark(), ".");
            this.config = ConfigurationHelper.loadConfiguration(this.args, mainClassName);
            log.info("BenchMark configuration: " + this.config);
        } catch (Exception ex) {
            log.error("",ex);
        }
    }
}
