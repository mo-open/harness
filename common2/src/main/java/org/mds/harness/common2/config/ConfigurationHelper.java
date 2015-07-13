package org.mds.harness.common2.config;


import com.esotericsoftware.yamlbeans.YamlReader;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.mds.harness.common2.reflect.ReflectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by Randall.mo on 14-4-11.
 */
public class ConfigurationHelper {
    public final static String ARG_SEPARATOR = "||";
    public final static String confFileArg = "-f";
    public final static String YAML_SUFFIX = ".yaml";
    public final static String YML_SUFFIX = ".yml";
    public final static String CONF_SUFFIX = ".conf";
    public final static String PROPERTY_SUFFIX = ".properties";

    private static Logger log = LoggerFactory.getLogger(ConfigurationHelper.class);

    public static <T> T loadConfiguration(String configurationFile, Properties inputProperties, Class<T> configurationClass) throws Exception {
        Properties fileProperties = new Properties();
        try {
            fileProperties.load(ConfigurationHelper.class.getClassLoader().getResourceAsStream(configurationFile));
        } catch (Exception ex) {
            log.error("Failed to load configuration file:{},Error:{}", configurationFile, ex);
        }
        if (inputProperties != null) {
            fileProperties.putAll(inputProperties);
        }

        return propertiesToBean(fileProperties, configurationClass);
    }

    private static <T> T propertiesToBean(Properties properties, Class<T> configurationClass) throws Exception {
        ObjectMapper mapper = new ObjectMapper().setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            return mapper.readValue(mapper.writeValueAsString(propertiesToMap(properties)), configurationClass);
        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        }
    }

    public static <T> T loadYAMLConfiguration(String configurationFile, Properties inputProperties, Class<T> configurationClass) throws Exception {
        T configuration = null;
        try {
            YamlReader reader = new YamlReader(new InputStreamReader(ConfigurationHelper.class.getClassLoader().getResourceAsStream(configurationFile)));
            configuration = reader.<T>read(configurationClass);
        } catch (Exception ex) {
            log.error("Failed to load configuration file:{},Error:{}", configurationFile, ex);
            throw new Exception(ex.getMessage());
        }
        if (inputProperties != null) {
            setInputPropertiesToConfiguration(configuration, inputProperties);
        }

        return configuration;
    }

    private static Map propertiesToMap(Properties properties) {
        Map<String, Object> result = new HashMap<>();
        properties.entrySet().forEach(entry -> {
            Map<String, Object> curMap = result;
            String key = (String) entry.getKey();
            while (key.contains(".")) {
                String mapKey = StringUtils.substringBefore(key, ".");
                key = StringUtils.substringAfter(key, ".");
                Map<String, Object> map = (Map<String, Object>) curMap.get(mapKey);
                if (map == null) {
                    map = new HashMap();
                    curMap.put(mapKey, map);
                }
                curMap = map;
            }
            curMap.put(key, entry.getValue());
        });
        return result;
    }

    private static void setInputPropertiesToConfiguration(Object object, Properties inputProperties) throws Exception {
        Object inputObject = propertiesToBean(inputProperties, object.getClass());
        inputProperties.entrySet().forEach(entry -> {
            Object fieldObject = inputObject;
            Object originObject = object;
            String fieldName = (String) entry.getKey();
            try {
                while (fieldName.contains(".")) {
                    fieldObject = ReflectUtils.getField(fieldObject, StringUtils.substringBefore(fieldName, "."));
                    originObject = ReflectUtils.getField(originObject, StringUtils.substringBefore(fieldName, "."));
                    fieldName = StringUtils.substringAfter(fieldName, ".");
                }
                Object value = ReflectUtils.getField(fieldObject, fieldName);
                ReflectUtils.setField(originObject, fieldName, value);
            } catch (Exception ex) {
                log.error("Failed to set property: " + entry + ": " + ex);
            }
        });

    }

    public static Map<String, Object> convertConfiguration(Object object) throws Exception {
        try {
            return ReflectUtils.readFields(object);
        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        }
    }

    public static Properties parseInputArgs(String argsString) {
        String[] args = argsString.split(ARG_SEPARATOR);
        return parseInputArgs(args);
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

    public static List<String> argumentNameList(Class cls, List<Class> expandFieldClasses) throws Exception {
        return ReflectUtils.getFieldNames(cls, expandFieldClasses);
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

    private static String getConfFile(String[] args) {
        if (args == null) return null;

        for (int i = 0; i < args.length; i++) {
            if (confFileArg.equals(args[i])) {
                if (i < args.length - 1)
                    return args[i + 1];
                else
                    return null;
            }
        }

        return null;
    }

    private static String getConfigFile(Class mainClass, String fileSuffix) {
        String fileName = mainClass.getSimpleName() + fileSuffix;
        if (new File(fileName).exists()) return fileName;
        fileName = mainClass.getSimpleName().toLowerCase() + fileSuffix;
        if (new File(fileName).exists()) return fileName;
        return null;
    }

    public static <T> T loadConfiguration(String[] args, Class mainClass, Class<T> configClass, String configFile) throws Exception {
        String inputConfigFile = getConfFile(args);
        if (inputConfigFile != null) configFile = inputConfigFile;
        if (configFile == null) configFile = getConfigFile(mainClass, YAML_SUFFIX);
        if (configFile == null) configFile = getConfigFile(mainClass, YML_SUFFIX);
        if (configFile == null) configFile = getConfigFile(mainClass, CONF_SUFFIX);
        if (configFile == null) configFile = getConfigFile(mainClass, PROPERTY_SUFFIX);
        if (configFile == null) {
            throw new Exception("Can not find any configuration file like " + mainClass.getSimpleName() + ".yaml or .yml or .conf or .properties");
        }
        Properties properties = ConfigurationHelper.parseInputArgs(args);
        log.info("Loading Test Configuration file: " + configFile);
        if (configFile.endsWith(YAML_SUFFIX) | configFile.endsWith(YML_SUFFIX))
            return ConfigurationHelper.loadYAMLConfiguration(configFile, properties, configClass);
        else
            return ConfigurationHelper.loadConfiguration(configFile, properties, configClass);
    }

    public static <T> T loadConfiguration(String[] args, Class mainClass, Class<T> configClass) throws Exception {
        return loadConfiguration(args, mainClass, configClass, null);
    }

    public static <T> T loadConfiguration(String args, Class mainClass, Class<T> configClass) throws Exception {
        return loadConfiguration(args.split(ARG_SEPARATOR), mainClass, configClass);
    }

    public static <T> T loadConfiguration(String args, Class mainClass) throws Exception {
        return loadConfiguration(args.split(ARG_SEPARATOR), mainClass, ReflectUtils.getTypeClass(mainClass));
    }

    public static <T> T loadConfiguration(String args, String mainClassName) throws Exception {
        Class mainClass = Class.forName(mainClassName);
        return loadConfiguration(StringUtils.splitByWholeSeparator(args, ARG_SEPARATOR), mainClass, ReflectUtils.getTypeClass(mainClass));
    }
}
