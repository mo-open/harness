package org.mds.harness.common2.runner;

import com.alibaba.fastjson.JSON;

import java.util.HashMap;
import java.util.Map;

public abstract class ARunnerConfig {
    protected final static Map<String, String> valueOptions = new HashMap<>();

    public static String valueOptions(String propertyName) {
        return valueOptions.get(propertyName);
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this, true);
    }

    public String defaultConfigFile() {
        return this.getClass().getSimpleName() + ".yml";
    }
}
