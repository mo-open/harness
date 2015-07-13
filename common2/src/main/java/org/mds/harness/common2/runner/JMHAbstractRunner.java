package org.mds.harness.common2.runner;

import org.apache.commons.lang3.StringUtils;
import org.mds.harness.common2.config.ConfigurationHelper;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class JMHAbstractRunner<C extends JMHRunnerConfig> {
    protected final static Logger log = LoggerFactory.getLogger(JMHAbstractRunner.class);

    protected C config;

    public void doInit(String args, BenchmarkParams params) {
        try {
            String mainClassName = StringUtils.substringBeforeLast(params.getBenchmark(), ".");
            config = ConfigurationHelper.loadConfiguration(args, mainClassName);
            this.setup(config);
            //log.info("BenchMark configuration: " + this.config);
        } catch (Exception ex) {
            log.error("", ex);
        }
    }

    public C config() {
        return this.config;
    }

    protected abstract void setup(C config);
}
