package org.mds.harness.common2.runner;

import org.mds.harness.common2.config.ConfigurationHelperTest.TestConfiguration;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Created by Randall.mo on 14-4-18.
 */
public class RunnerHelperTest {
    private static int property1 = 0;
    private static String property2;

    static public class MainClass {
        public void run(TestConfiguration configuration) {
            property1 = configuration.field_1;
        }

        public void start(TestConfiguration configuration) {
            property2 = configuration.field_2;
        }
    }

    @Test
    public void testRun() throws Exception {
        RunnerHelper.newInvoker()
                .setMethodName(null)
                .setMainClass(MainClass.class)
                .setConfigClass(TestConfiguration.class)
                .setConfigFile("test-config.yaml").invoke();

        assertEquals(property1, 1);
        RunnerHelper.newInvoker()
                .setMethodName("start")
                .enableMethodSuffix(false)
                .setMainClass(MainClass.class)
                .setConfigClass(TestConfiguration.class)
                .setConfigFile("test-config.yaml").invoke();
        assertEquals(property2, "2");

        property1 = 0;
        property2 = null;
        for (String arg : RunnerHelper.helpArgs) {
            String[] args = {arg};
            RunnerHelper.newInvoker()
                    .setArgs(args)
                    .setMainClass(MainClass.class)
                    .setConfigClass(TestConfiguration.class)
                    .setConfigFile("test-config.yaml").invoke();
            RunnerHelper.newInvoker()
                    .setMethodName("start")
                    .setArgs(args)
                    .enableMethodSuffix(false)
                    .setMainClass(MainClass.class)
                    .setConfigClass(TestConfiguration.class)
                    .setConfigFile("test-config.yaml").invoke();
            assertEquals(property1, 0);
            assertNull(property2);
        }
    }
}
