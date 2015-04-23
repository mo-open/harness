package org.mds.harness.common2.runner;

import org.mds.harness.common2.config.ConfigurationHelper;
import org.mds.harness.common2.reflect.ReflectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Properties;

/**
 * Created by Randall.mo on 14-4-11.
 */
public class RunnerHelper {
    private final static Logger log = LoggerFactory.getLogger(RunnerHelper.class);
    public final static String[] helpArgs = {"-h", "-H", "help", "-help", "--help"};
    public final static String confFileArg = "-f";
    public final static String indent_1 = "  ";
    public final static String indent_2 = "    ";

    public static void run(String[] args, Class mainClass, Class configClass, String configFile) {
        run("run", true, args, mainClass, configClass, configFile);
    }

    private static boolean hasHelp(String[] args) {
        if (args == null) return false;
        for (String arg : args) {
            if (ArrayUtils.contains(helpArgs, arg)) return true;
        }
        return false;
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

    private static String getRunMethods(String methodName, Class mainClass) {
        String methods = "";
        try {
            for (Method method : mainClass.getDeclaredMethods()) {
                String name = method.getName();
                if (name.startsWith(methodName)) {
                    name = name.substring(methodName.length()).toLowerCase();
                    methods = methods + " " + name;
                }
            }
        } catch (Exception ex) {

        }
        return methods.trim();
    }

    private static void showHelp(String methodName, Class mainClass, Class configClass) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("HELP -- \r\n")
                .append(indent_1).append(confFileArg).append(" configFile\r\n");
        stringBuilder.append(indent_1).append("App Options:\r\n");
        List<String> optionList = ConfigurationHelper.argumentNameList(configClass);
        for (String option : optionList) {
            if (option.equals("runs")) {
                stringBuilder.append(indent_2).append(option).append("   [")
                        .append(getRunMethods(methodName, mainClass)).append("]").append("\r\n");
            } else
                stringBuilder.append(indent_2).append(option).append("\r\n");
        }
        stringBuilder.append(indent_1).append("help args: ");
        for (String arg : helpArgs) {
            stringBuilder.append(arg).append(" ");
        }
        stringBuilder.append("\r\n");
        log.info(stringBuilder.toString());
    }

    public static void run(String methodName, boolean enableMethodSuffix, String[] args, Class mainClass, Class configClass, String configFile) {
        try {
            if (methodName == null || "".equals(methodName)) {
                methodName = "run";
            }
            if (hasHelp(args)) {
                showHelp(methodName, mainClass, configClass);
                return;
            }
            String inputConfigFile = getConfFile(args);
            if (inputConfigFile != null) configFile = inputConfigFile;
            Properties properties = ConfigurationHelper.parseInputArgs(args);
            Object configuration = ConfigurationHelper.loadYAMLConfiguration(configFile, properties, configClass);
            log.info(String.format("Start,Arguments:" + ConfigurationHelper.argumentsString(configuration)));
            Object instance = mainClass.newInstance();
            String methodSuffixes = "";
            try {
                if (enableMethodSuffix)
                    methodSuffixes = ReflectUtils.getField(configuration, "runs").toString();
            } catch (Exception ex) {

            }

            Method beforeRun = null, afterRun = null;
            try {
                beforeRun = mainClass.getDeclaredMethod("beforeRun", new Class[]{configClass});
                afterRun = mainClass.getDeclaredMethod("beforeRun", new Class[]{configClass});
            } catch (Exception ex) {

            }
            if (beforeRun != null) {
                log.info("Before running ..... " + methodName);
                beforeRun.invoke(instance, configuration);
            }

            if (!"".equals(methodSuffixes)) {
                for (String methodSuffix : methodSuffixes.split(",")) {
                    methodName = methodName + methodSuffix.substring(0, 1).toUpperCase() + methodSuffix.substring(1);
                    Method testMothod = mainClass.getDeclaredMethod(methodName, new Class[]{configClass});
                    testMothod.invoke(instance, configuration);
                    Thread.sleep(2);
                }
            } else {
                Method testMothod = mainClass.getDeclaredMethod(methodName, new Class[]{configClass});
                testMothod.invoke(instance, configuration);
            }

            if (afterRun != null) {
                log.info("After running ..... " + methodName);
                afterRun.invoke(instance, configuration);
            }
        } catch (Exception ex) {
            log.error("Failed to run test", ex);
        }
    }
}
