package org.mds.harness.common2.config;

import org.mds.harness.common2.perf.PerfConfig;
import org.testng.annotations.Test;

import static org.testng.Assert.*;
import static org.testng.Assert.assertEquals;

/**
 * Created by Randall.mo on 14-4-18.
 */
public class ConfigurationHelperTest {
    static public class TestConfiguration extends PerfConfig{
        public int field_1;
        public String field_2;
        String field_3;
        public int field_4;
    }

    @Test
    public void testLoadConfiguration() throws Exception {
        TestConfiguration configuration = (TestConfiguration) ConfigurationHelper.loadConfiguration("test-config.properties", null, TestConfiguration.class);
        assertEquals(configuration.field_1, 1);
        assertEquals(configuration.field_2, "2");
        assertEquals(configuration.field_3, "3");
        assertEquals(configuration.field_4, 4);
    }

    @Test
    public void testParseInputArgs() throws Exception {

    }
}
