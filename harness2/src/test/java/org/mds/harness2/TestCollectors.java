package org.mds.harness2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Created by modongsong on 2015/4/23.
 */
public class TestCollectors {
    private final static Logger log = LoggerFactory.getLogger(TestCollectors.class);

    String[] data1 = {"a", "b", "c", "d"};
    String[] data2 = {"A", "B", "C", "D"};

    public void test1() {
        final AtomicInteger index = new AtomicInteger(0);
        Stream.of(data1)
                .map(a -> a + "_" + index.incrementAndGet())
                .flatMap(a -> Stream.of(data2).map(b -> a + "_" + b)).forEach(System.out::println);
    }

    public static void main(String args[]) {
        log.info("1---------");
        new TestCollectors().test1();
    }
}
