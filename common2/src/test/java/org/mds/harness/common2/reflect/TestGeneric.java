package org.mds.harness.common2.reflect;

import java.lang.reflect.ParameterizedType;
/**
 * Created by modongsong on 2015/7/9.
 */
public class TestGeneric {
    static class ClassA<S>{}

    static class ClassB extends ClassA<Integer>{

    }

    public static void main(String args[]){
        System.out.println(((ParameterizedType)ClassB.class.getGenericSuperclass()).getActualTypeArguments()[0]);
    }
}
