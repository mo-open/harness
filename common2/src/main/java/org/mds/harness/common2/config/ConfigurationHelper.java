package org.mds.harness.common2.config;


import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.mds.harness.common2.reflect.ReflectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by Randall.mo on 14-4-11.
 */
public class ConfigurationHelper {
    private static Logger log = LoggerFactory.getLogger(ConfigurationHelper.class);

    public static Object loadConfiguration(String configurationFile, Properties inputProperties, Class configurationClass) throws Exception {
        Properties fileProperties = new Properties();
        try {
            fileProperties.load(ConfigurationHelper.class.getClassLoader().getResourceAsStream(configurationFile));
        } catch (Exception ex) {
            log.error("Failed to load configuration file:{},Error:{}", configurationFile, ex);
        }
        if (inputProperties != null) {
            fileProperties.putAll(inputProperties);
        }

        ObjectMapper mapper = new ObjectMapper().setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            return mapper.readValue(mapper.writeValueAsString(fileProperties), configurationClass);
        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        }
    }

    public static Map<String, Object> convertConfiguration(Object object) throws Exception {
        try {
            return ReflectUtils.readFields(object);
        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        }
    }

    public static Properties parseInputArgs(String args[]) {
        Properties properties = new Properties();
        if (args == null) return properties;
        Arrays.stream(args)
                .map(s -> s.split("="))
                .filter(kv -> kv.length == 2)
                .forEach(kv -> properties.put(kv[0].trim(), kv[1].trim()));
        return properties;
    }

    public static List<String> argumentNameList(Class cls) throws Exception {
        return ReflectUtils.getFieldNames(cls);
    }

    public static String argumentNames(Class cls) throws Exception {
        List<String> fieldNames = ReflectUtils.getFieldNames(cls);
        if (fieldNames == null) return "";
        StringBuilder stringBuilder = new StringBuilder();
        fieldNames.forEach(s -> {
            stringBuilder.append(" ").append(s);
        });

        return stringBuilder.toString();
    }

    public static String argumentsString(Object configuration) throws Exception {
        Map<String, Object> configurationMap = convertConfiguration(configuration);
        StringBuilder sb = new StringBuilder();
        configurationMap.forEach((key, value) -> {
            sb.append(",").append(key).append("=").append(value);
        });

        return sb.toString().substring(1);
    }
}
