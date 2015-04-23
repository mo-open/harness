package org.mds.harness.common2.config;

import org.mds.harness.common2.perf.PerfConfig;
import org.testng.annotations.Test;

import java.util.Properties;

import static org.testng.Assert.*;
import static org.testng.Assert.assertEquals;

/**
 * Created by Randall.mo on 14-4-18.
 */
public class ConfigurationHelperTest {
    static public class TestConfiguration extends PerfConfig {
        public int field_1;
        public String field_2;
        public String field_3;
        public int field_4;
        public int[] field_5;
        public SubConfig subConfig;
    }

    static public class SubConfig {
        public int f1;
        public String f2;
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
    public void testLoadConfigurationWithInput() throws Exception {
        Properties inputProperties = new Properties();
        inputProperties.setProperty("field_1", "9");

        TestConfiguration configuration = (TestConfiguration) ConfigurationHelper.loadConfiguration("test-config.properties", inputProperties, TestConfiguration.class);
        assertEquals(configuration.field_1, 9);
        assertEquals(configuration.field_2, "2");
        assertEquals(configuration.field_3, "3");
        assertEquals(configuration.field_4, 4);
    }

    @Test
    public void testLoadYAMLConfiguration() throws Exception {
        TestConfiguration configuration = (TestConfiguration) ConfigurationHelper.loadYAMLConfiguration("test-config.yaml", null, TestConfiguration.class);
        assertEquals(configuration.field_1, 1);
        assertEquals(configuration.field_2, "2");
        assertEquals(configuration.field_3, "3");
        assertEquals(configuration.field_4, 4);
        assertEquals(configuration.field_5[0],1);
        assertEquals(configuration.field_5[1],2);
        assertEquals(configuration.subConfig.f1,1);
        assertEquals(configuration.subConfig.f2,"2");
    }

    @Test
    public void testLoadYAMLConfigurationWithInput() throws Exception {
        Properties inputProperties = new Properties();
        inputProperties.setProperty("field_1", "9");

        TestConfiguration configuration = (TestConfiguration) ConfigurationHelper.loadYAMLConfiguration("test-config.yaml", inputProperties, TestConfiguration.class);
        assertEquals(configuration.field_1, 9);
        assertEquals(configuration.field_2, "2");
        assertEquals(configuration.field_3, "3");
        assertEquals(configuration.field_4, 4);
        assertEquals(configuration.field_5[0],1);
        assertEquals(configuration.field_5[1],2);
        assertEquals(configuration.subConfig.f1,1);
        assertEquals(configuration.subConfig.f2,"2");
    }

    @Test
    public void testParseInputArgs() throws Exception {

    }
}
