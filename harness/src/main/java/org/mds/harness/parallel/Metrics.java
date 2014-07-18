package org.mds.harness.parallel;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Created by modongsong on 14-6-12.
 */
public class Metrics {
    protected final static Logger log = LoggerFactory.getLogger(Metrics.class);
    private MetricRegistry metrics;
    private Slf4jReporter reporter;
    private volatile boolean enabled = false;
    private volatile boolean started = false;

    private static class MetricsHolder {
        private static String name = "org.mds.harness";
        private static Metrics metics;

        private static class instanceHolder {
            private static Metrics metics = new Metrics(name);
        }

        public static Metrics instance() {
            return instanceHolder.metics;
        }
    }

    private Metrics(String logger) {
        this.metrics = new MetricRegistry();
        this.reporter = Slf4jReporter.forRegistry(metrics)
                .outputTo(LoggerFactory.getLogger(logger))
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
    }

    public static synchronized void initialize(String logger) {
        MetricsHolder.name = logger;
    }

    private static Metrics instance() {
        return MetricsHolder.instance();
    }

    public static Timer timer(Class clz, String name) {
        return instance().metrics.timer(name(clz, name));
    }

    public static Meter meter(Class clz, String name) {
        return instance().metrics.meter(name(clz, name));
    }

    public static synchronized void start(int interval) {
        if (!instance().started)
            instance().reporter.start(interval, TimeUnit.SECONDS);
        instance().enabled = true;
    }

    public static boolean isEnabled() {
        return instance().enabled;
    }
}
