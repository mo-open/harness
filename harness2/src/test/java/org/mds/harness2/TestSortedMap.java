package org.mds.harness2;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by modongsong on 2014/10/28.
 */
public class TestSortedMap {
    public static void main(String args[]) {
        SortedMap<String, Double> map = new TreeMap<>();
        map.put("F_1", new Double(0));
        map.put("F_3", new Double(0));
        map.put("F_1_1", new Double(0));
        map.put("F_1_0", new Double(0));
        map.put("F_0", new Double(0));
        map.put("F_4", new Double(0));
        for (String key : map.keySet()) {
            System.out.println(key);
        }
    }
}
