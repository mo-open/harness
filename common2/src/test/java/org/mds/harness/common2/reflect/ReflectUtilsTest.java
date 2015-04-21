package org.mds.harness.common2.reflect;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;
import static org.testng.Assert.assertEquals;

/**
 * Created by Randall.mo on 14-4-18.
 */
public class ReflectUtilsTest {

    static class ReflectObjectForTest {
        private int privateField;
        public int publicField;
        private static int privateStaticField;
        public static int publicStaticField;
    }

    @org.testng.annotations.Test
    public void testSetStaticFields() throws Exception {
        //single set
        assertNotEquals(ReflectObjectForTest.privateStaticField, 1);
        assertNotEquals(ReflectObjectForTest.publicStaticField, 2);
        ReflectUtils.setStaticField(ReflectObjectForTest.class, "privateStaticField", 1);
        ReflectUtils.setStaticField(ReflectObjectForTest.class, "publicStaticField", 2);
        assertEquals(ReflectObjectForTest.privateStaticField, 1);
        assertEquals(ReflectObjectForTest.publicStaticField, 2);

        //multiple set
        Map<String, Object> values = new HashMap();
        values.put("privateStaticField", 10);
        values.put("publicStaticField", 20);
        ReflectUtils.setStaticFields(ReflectObjectForTest.class, values);
        assertEquals(ReflectObjectForTest.privateStaticField, 10);
        assertEquals(ReflectObjectForTest.publicStaticField, 20);

        values.clear();
        values.put("privateStaticField", 100);
        values.put("publicStaticField", 200);
        values.put("unknown", 200);

        boolean hasException = false;
        try {
            ReflectUtils.setStaticFields(ReflectObjectForTest.class, values);
        } catch (Exception ex) {
            hasException = true;
        }
        assertTrue(hasException);

        hasException = false;
        try {
            ReflectUtils.setStaticFields(ReflectObjectForTest.class, values, false);
        } catch (Exception ex) {
            hasException = true;
        }
        assertEquals(ReflectObjectForTest.privateStaticField, 100);
        assertEquals(ReflectObjectForTest.publicStaticField, 200);
        assertFalse(hasException);

        //read static fields
        values = ReflectUtils.readStaticFields(ReflectObjectForTest.class);
        assertEquals(values.get("privateStaticField"), 100);
        assertEquals(values.get("publicStaticField"), 200);

        //test map
        values.put("privateField", 100);
        values.put("publicField", 200);
        values.put("unknown", 200);
        hasException = false;
        try {
            ReflectUtils.map(ReflectObjectForTest.class, values);
        } catch (Exception ex) {
            hasException = true;
        }
        assertTrue(hasException);

        hasException = false;
        ReflectObjectForTest newO = null;
        try {
            newO = (ReflectObjectForTest) ReflectUtils.map(ReflectObjectForTest.class, values, false);
        } catch (Exception ex) {
            hasException = true;
        }
        assertFalse(hasException);
        assertNotNull(newO);
        assertEquals(ReflectObjectForTest.privateStaticField, 100);
        assertEquals(ReflectObjectForTest.publicStaticField, 200);
        assertEquals(newO.privateField, 100);
        assertEquals(newO.publicField, 200);
    }

    @org.testng.annotations.Test
    public void testSetFields() throws Exception {
        ReflectObjectForTest o = new ReflectObjectForTest();
        //single set
        assertNotEquals(o.privateField, 1);
        assertNotEquals(o.publicField, 2);
        ReflectUtils.setField(o, "privateField", 1);
        ReflectUtils.setField(o, "publicField", 2);
        assertEquals(o.privateField, 1);
        assertEquals(o.publicField, 2);

        //multiple set
        Map<String, Object> values = new HashMap();
        values.put("privateField", 10);
        values.put("publicField", 20);
        ReflectUtils.setFields(o, values);
        assertEquals(o.privateField, 10);
        assertEquals(o.publicField, 20);

        values.clear();
        values.put("privateField", 100);
        values.put("publicField", 200);
        values.put("unknown", 200);

        boolean hasException = false;
        try {
            ReflectUtils.setFields(o, values);
        } catch (Exception ex) {
            hasException = true;
        }
        assertTrue(hasException);

        hasException = false;
        try {
            ReflectUtils.setFields(o, values, false);
        } catch (Exception ex) {
            hasException = true;
        }
        assertEquals(o.privateField, 100);
        assertEquals(o.publicField, 200);
        assertFalse(hasException);

        //read fields
        values = ReflectUtils.readFields(o);
        assertEquals(values.get("privateField"), 100);
        assertEquals(values.get("publicField"), 200);
    }

    @org.testng.annotations.Test
    public void testGetFieldNames() throws Exception {
        List<String> fieldNames=ReflectUtils.getFieldNames(ReflectObjectForTest.class);
        assertTrue(fieldNames.contains("privateStaticField"));
        assertTrue(fieldNames.contains("publicStaticField"));
        assertTrue(fieldNames.contains("privateField"));
        assertTrue(fieldNames.contains("publicField"));
    }
}
