package org.mds.harness.common2.config;

import com.esotericsoftware.yamlbeans.YamlReader;

import java.io.InputStreamReader;

/**
 * Created by modoso on 15/4/23.
 */
public class YamlBeanTest {
    public static void main(String args[]) throws Exception {
        YamlReader reader = new YamlReader(new InputStreamReader(YamlBeanTest.class.getClassLoader().getResourceAsStream("test.yaml")));
        TestConfig configuration = reader.read(TestConfig.class);
    }
}
