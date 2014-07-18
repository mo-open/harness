package org.mds.harness.tools.lang;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class TestToStringBuilder {

    private final static Logger log = LoggerFactory.getLogger(TestToStringBuilder.class);
    public void testMapToString()
    {
        Map<String,String> map=new HashMap();
        map.put("a","a");
        ToStringBuilder toStringBuilder=new ToStringBuilder(map);
        log.info(toStringBuilder.build());
    }

    public static void main(String args[]){
        TestToStringBuilder tester=new TestToStringBuilder();
        tester.testMapToString();
    }
}
