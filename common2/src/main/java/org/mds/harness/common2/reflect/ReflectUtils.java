package org.mds.harness.common2.reflect;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Randall.mo on 14-4-18.
 */
public class ReflectUtils {
    private static Logger log = LoggerFactory.getLogger(ReflectUtils.class);

    /**
     * Set a new value to a static field of the specified class
     *
     * @param cls
     * @param fieldName
     * @param fieldValue
     * @throws Exception
     */
    public static void setStaticField(Class cls, String fieldName, Object fieldValue) throws Exception {
        try {
            Field field = FieldUtils.getField(cls, fieldName, true);

            FieldUtils.writeStaticField(cls, fieldName, fieldValue, true);
        } catch (Throwable ex) {
            String errMsg = String.format("Failed to set static field '%s' of class '%s' : %s", fieldName, cls.getName(), ex);
            throw new Exception(errMsg, ex);
        }
    }

    /**
     * Set a new value to a field of the specified object, including static field
     *
     * @param object
     * @param fieldName
     * @param fieldValue
     * @throws Exception
     */
    public static void setField(Object object, String fieldName, Object fieldValue) throws Exception {
        try {
            FieldUtils.writeField(object, fieldName, fieldValue, true);
        } catch (Throwable ex) {
            String errMsg = String.format("Failed to set field '%s' of class '%s' with value '%s' : %s", fieldName, object.getClass().getName(), fieldValue, ex);
            throw new Exception(errMsg, ex);
        }
    }

    public static Object getField(Object object, String fieldName) throws Exception {
        try {
            return FieldUtils.readField(object, fieldName, true);
        } catch (Throwable ex) {
            String errMsg = String.format("Failed to get field '%s' of class '%s': %s", fieldName, object.getClass().getName(), ex);
            throw new Exception(errMsg, ex);
        }
    }

    /**
     * set multiple static fields, if try to set the field with unknown field name, throw exception
     *
     * @param cls
     * @param values
     * @throws Exception
     */
    public static void setStaticFields(Class cls, Map<String, Object> values) throws Exception {
        setStaticFields(cls, values, true);
    }

    private static void handleException(String errMsg, Throwable ex, boolean failForUnknonw) throws Exception {
        if (failForUnknonw) {
            throw new Exception(errMsg, ex);
        } else {
            log.warn(errMsg);
        }
    }

    /**
     * set multiple static fields
     *
     * @param cls
     * @param values
     * @param failForUnknown if it is false, then ignore the unknown field
     * @throws Exception
     */
    public static void setStaticFields(Class cls, Map<String, Object> values, boolean failForUnknown) throws Exception {
        for (Map.Entry<String, Object> value : values.entrySet()) {
            try {
                FieldUtils.writeStaticField(cls, value.getKey(), value.getValue(), true);
            } catch (Throwable ex) {
                String errMsg = String.format("Failed to set class field '%s' of class '%s' with value '%s' : %s",
                        value.getKey(), cls.getName(), value.getValue(), ex);
                handleException(errMsg, ex, failForUnknown);
            }
        }
    }

    /**
     * read static fields, including private and public
     *
     * @param cls
     * @return
     * @throws Exception
     */
    public static Map<String, Object> readStaticFields(Class cls) throws Exception {
        try {
            List<Field> fields = FieldUtils.getAllFieldsList(cls);
            Map<String, Object> fieldValues = new HashMap();
            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers())) {
                    fieldValues.put(field.getName(), FieldUtils.readStaticField(field, true));
                }
            }
            return fieldValues;
        } catch (Exception ex) {
            String errMsg = String.format("Failed to read static fields of class '%s' : %s", cls.getName(), ex);
            throw new Exception(errMsg, ex);
        }
    }

    public static Map<String, Object> readFields(Object object) throws Exception {
        try {
            List<Field> fields = FieldUtils.getAllFieldsList(object.getClass());
            Map<String, Object> fieldValues = new HashMap();
            for (Field field : fields) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    fieldValues.put(field.getName(), FieldUtils.readField(field, object, true));
                }
            }
            return fieldValues;
        } catch (Exception ex) {
            String errMsg = String.format("Failed to read fields of class '%s' : %s", object.getClass().getName(), ex);
            throw new Exception(errMsg, ex);
        }
    }

    public static Map<String, Object> readAllFields(Object object) throws Exception {
        try {
            List<Field> fields = FieldUtils.getAllFieldsList(object.getClass());
            Map<String, Object> fieldValues = new HashMap();
            for (Field field : fields) {
                fieldValues.put(field.getName(), FieldUtils.readField(field, object, true));
            }
            return fieldValues;
        } catch (Exception ex) {
            String errMsg = String.format("Failed to read fields of class '%s' : %s", object.getClass().getName(), ex);
            throw new Exception(errMsg, ex);
        }
    }

    public static Object map(Class cls, Map values) throws Exception {
        return mapObject(cls, values, true);
    }

    public static Object map(Class cls, Map values, boolean failForUnknown) throws Exception {
        return mapObject(cls, values, failForUnknown);
    }

    private static Object mapObject(Class cls, Map<String, Object> values, boolean failForUnknown) throws Exception {
        Object object;
        try {
            object = cls.newInstance();
        } catch (Exception ex) {
            throw new Exception("Failed to create new instance of class " + cls.getName(), ex);
        }
        for (Map.Entry<String, Object> value : values.entrySet()) {
            try {
                FieldUtils.writeField(object, value.getKey(), value.getValue(), true);
            } catch (Throwable ex) {
                String errMsg = String.format("Failed to set object field '%s' of class '%s' with value '%s' : %s",
                        value.getKey(), object.getClass().getName(), value.getValue(), ex);
                handleException(errMsg, ex, failForUnknown);
            }
        }
        return object;
    }

    public static void setFields(Object object, Map<String, Object> values) throws Exception {
        setFields(object, values, true);
    }

    public static void setFields(Object object, Map<String, Object> values, boolean failForUnknown) throws Exception {
        for (Map.Entry<String, Object> value : values.entrySet()) {
            try {
                FieldUtils.writeField(object, value.getKey(), value.getValue(), true);
            } catch (Throwable ex) {
                if (failForUnknown) {
                    String errMsg = String.format("Failed to set object field '%s' of class '%s' with value '%s' : %s",
                            value.getKey(), object.getClass().getName(), value.getValue(), ex);
                    throw new Exception(errMsg, ex);
                }
            }
        }
    }

    public static List<String> getFieldNames(Class cls) throws Exception {
        return getFieldNames(cls, null, null);
    }

    public static List<String> getFieldNames(Class cls, String parent, List<Class> expandFieldClasses) throws Exception {
        try {
            List<Field> fields = FieldUtils.getAllFieldsList(cls);
            List<String> fieldNames = new ArrayList();
            for (Field field : fields) {
                if (Modifier.isFinal(field.getModifiers())) continue;
                String fullFieldName = field.getName();
                if (parent != null && !parent.isEmpty()) {
                    fullFieldName = parent + "." + fullFieldName;
                }
                if (expandFieldClasses != null && expandFieldClasses.contains(field.getType())) {
                    fieldNames.addAll(getFieldNames(field.getType(), fullFieldName, expandFieldClasses));
                    continue;
                }

                fieldNames.add(fullFieldName);
            }
            return fieldNames;
        } catch (Throwable ex) {
            String errMsg = String.format("Failed to get all fields of class '%s': %s", cls.getName(), ex);
            throw new Exception(errMsg, ex);
        }
    }

    public static List<String> getFieldNames(Class cls, List<Class> expandFieldClasses) throws Exception {
        return getFieldNames(cls, null, expandFieldClasses);
    }

    public static <T> Class<T> getTypeClass(Class mainClass) throws ClassNotFoundException {
        String configClassName = ((ParameterizedType) mainClass.getGenericSuperclass()).getActualTypeArguments()[0].getTypeName();
        return (Class<T>) Class.forName(configClassName);
    }
}
