package org.mds.harness.common2.runner;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.mds.harness.common2.config.ConfigurationHelper;
import org.mds.harness.common2.reflect.ReflectUtils;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Randall.mo on 14-4-11.
 */
public class RunnerHelper {
    private final static Logger log = LoggerFactory.getLogger(RunnerHelper.class);
    public final static String[] helpArgs = {"-h", "-H", "help", "-help", "--help"};

    public final static String indent_1 = "  ";
    public final static String indent_2 = "    ";


    private static boolean hasHelp(String[] args) {
        if (args == null) return false;
        for (String arg : args) {
            if (ArrayUtils.contains(helpArgs, arg)) return true;
        }
        return false;
    }

    private static String getRunMethods(String methodName, Class mainClass) {
        String methods = "";
        try {
            for (Method method : mainClass.getDeclaredMethods()) {
                String name = method.getName();
                if (name.startsWith(methodName)) {
                    name = name.substring(methodName.length());
                    name = name.substring(0, 1).toLowerCase() + name.substring(1);
                    methods = methods + " " + name;
                }
            }
        } catch (Exception ex) {

        }
        return methods.trim();
    }

    private static void showHelp(String methodName, String className) {
        try {
            Class mainClass = Class.forName(className);
            Class configClass = ReflectUtils.getTypeClass(mainClass);

            showHelp(methodName, configClass, configClass);
        } catch (Exception ex) {
            log.error("Failed to show help", ex);
        }
    }

    private static String getOptionValues(String optionName, Class configClass) {
        String value = "";
        try {
            value = (String) MethodUtils.invokeStaticMethod(configClass, "valueOptions", new Object[]{optionName});
        } catch (Exception ex) {
            value = "";
        }
        return value == null ? "" : "[" + value + "]";
    }

    private static void showHelp(String methodName, Class mainClass, Class configClass) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("HELP -- \r\n")
                .append(indent_1).append(ConfigurationHelper.confFileArg).append(" configFile\r\n");
        stringBuilder.append(indent_1).append("App Options:\r\n");
        List<Class> expandFieldClasses = new ArrayList<>();
        expandFieldClasses.add(JMHRunnerConfig.JMHConfig.class);
        List<String> optionList = ConfigurationHelper.argumentNameList(configClass, expandFieldClasses);
        for (String option : optionList) {
            if (option.equals("runs")) {
                stringBuilder.append(indent_2).append(option).append("   [")
                        .append(getRunMethods(methodName, mainClass)).append("]").append("\r\n");
            } else {
                stringBuilder.append(indent_2)
                        .append(option)
                        .append("  ")
                        .append(getOptionValues(option, configClass))
                        .append("\r\n");
            }
        }
        stringBuilder.append(indent_1).append("help args: ");
        for (String arg : helpArgs) {
            stringBuilder.append(arg).append(" ");
        }
        stringBuilder.append("\r\n");
        log.info(stringBuilder.toString());
    }

    private static void invokeCommonRun(String methodName, Class mainClass, Object configuration, String methodSuffixes) throws Exception {
        Object instance = mainClass.newInstance();
        Class configClass = configuration.getClass();
        Method beforeRun = null, afterRun = null;
        try {
            beforeRun = mainClass.getDeclaredMethod("beforeRun", new Class[]{configClass});
            afterRun = mainClass.getDeclaredMethod("afterRun", new Class[]{configClass});
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
    }

    private static void invokeJMH(String methodName, Class mainClass, String[] args, Object configuration, String methodSuffixes) throws Exception {
        String[] marks = methodSuffixes.split(",");
        for (int i = 0; i < marks.length; i++) {
            marks[i] = marks[i].substring(0, 1).toUpperCase() + marks[i].substring(1);
        }
        String benchmarks = mainClass.getName() + ".*" + methodName + "(" + String.join("|", marks) + ")";
        JMHRunnerConfig config = (JMHRunnerConfig) configuration;
        JMHRunnerConfig.JMHConfig jmhConfig = config.jmh();
        Options options = new OptionsBuilder()
                .param("args", String.join(ConfigurationHelper.ARG_SEPARATOR, args))
                .measurementIterations(jmhConfig.iterations)
                .warmupIterations(jmhConfig.wIterations)
                .mode(jmhConfig.mode())
                .threads(jmhConfig.threads)
                .include(benchmarks).forks(jmhConfig.forks).build();
        new Runner(options).run();
    }

    private static void run(Invoker invoker) {
        try {
            if (hasHelp(invoker.args)) {
                showHelp(invoker.methodName, invoker.mainClass, invoker.configClass);
                return;
            }

            Object configuration = ConfigurationHelper.loadConfiguration(invoker.args, invoker.mainClass, invoker.configClass, invoker.configFile);
            log.info(String.format("Start,Arguments:" + ConfigurationHelper.argumentsString(configuration)));


            String methodSuffixes = "";
            try {
                if (invoker.enableMethodSuffix) {
                    methodSuffixes = ReflectUtils.getField(configuration, "runs").toString();
                }
            } catch (Exception ex) {

            }

            if (!invoker.isJMH)
                invokeCommonRun(invoker.methodName, invoker.mainClass, configuration, methodSuffixes);
            else
                invokeJMH(invoker.methodName, invoker.mainClass, invoker.args, configuration, methodSuffixes);
        } catch (Exception ex) {
            log.error("Failed to run test", ex);
        }
    }


    public static Invoker newInvoker() {
        return new Invoker();
    }

    public static class Invoker {
        String methodName = "run";
        String[] args = null;
        boolean enableMethodSuffix = true;
        String mainClassName;
        String configFile;
        Class mainClass;
        Class configClass;
        boolean isJMH = false;

        private Invoker() {

        }

        public Invoker setArgs(String[] args) {
            this.args = args;
            return this;
        }

        public Invoker setMethodName(String methodName) {
            if (methodName != null)
                this.methodName = methodName;
            return this;
        }

        public Invoker enableMethodSuffix(boolean enableMethodSuffix) {
            this.enableMethodSuffix = enableMethodSuffix;
            return this;
        }

        public Invoker setMainClassName(String mainClassName) {
            this.mainClassName = mainClassName;
            try {
                this.mainClass = Class.forName(mainClassName);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            return this;
        }

        public Invoker setConfigFile(String configFile) {
            this.configFile = configFile;
            return this;
        }

        public Invoker setJMH(boolean isJMH) {
            this.isJMH = isJMH;
            return this;
        }

        public Invoker setMainClass(Class mainClass) {
            this.mainClass = mainClass;
            return this;
        }

        public Invoker setConfigClass(Class configClass) {
            this.configClass = configClass;
            return this;
        }

        public void invoke() throws Exception {
            if (this.mainClass == null && this.mainClassName == null) {
                throw new RuntimeException("mainClass or mainClassName must be set");
            }
            if (this.configClass == null) {
                this.configClass = ReflectUtils.getTypeClass(this.mainClass);
            }

            run(this);
        }
    }
}
